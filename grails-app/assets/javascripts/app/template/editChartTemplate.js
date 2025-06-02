/**
 * Created by gologuzov on 22.08.16.
 */
$(function () {
    var chartWarningShown = false;
    var templateType = $('#templateType').val();

    if (templateType == 'DATA_TAB' || templateType == 'NON_CASE') {
        var templateId = $('#templateId').val();

        var chartDefaultOptions = JSON.parse($("input[name=chartDefaultOptions]").val() || "{}");
        var chartCustomOptions = JSON.parse($("input[name=chartCustomOptions]").val() || "{}");
        var config = $.isEmptyObject(chartCustomOptions) ? chartDefaultOptions : chartCustomOptions;

        var container = $("#easychart-preview");
        container.data('config', config);
        var easyChart = new ec({
            element: container.get(0),
            config: config,
            dataTab: false,
            showLogo: false,
            themesTab: true,
            events: {
                configUpdate: function(e) {
                    var configUpdated = {};
                    $("#chartCustomOptions").val(JSON.stringify(easyChart.getConfig()));
                }
            }
        });

        if (!viewOnly) {
            customizePresets(easyChart);
            customizeTemplates(easyChart);
            customizeOptions(easyChart);
        }

        if ($('#showChartSheet').prop('checked')) {
            showEasyChart(container, templateType);
        }

        $("#showChartSheet").on('change', function(){
            if (this.checked) {
                showEasyChart(container, templateType);
                showWarningModalAndCheck();
            } else {
                hideEasyChart(container);
            }
        });
        if (typeof editPage != 'undefined' && editPage) {

            if (($("#showChartSheet").is(":checked")) && (!$("#chartExportAsImage").is(":checked"))) {
                chartWarningShown = hasComplexChartOptions();
            }
        }

        function hasComplexChartOptions() {
            var result = false
            var elements = $("select[name$=valuesChartType]");
            for (var i = 0; i < elements.length; i++) {
                if ($(elements[i]).val() != "") {
                    return true;
                }
            }
            elements = $("select[name$=percentageChartType]");
            for (var i = 0; i < elements.length; i++) {
                if ((!$(elements[i]).attr("disabled")) && ($(elements[i]).val() != "")) {
                    return true;
                }
            }
            if ($(".sqlBox").val() && $(".sqlBox").val().indexOf("CHART_COLUMN_P_") > -1) return true;
            return false;
        }

        $(document).on("change", "select[name$=valuesChartType], select[name$=percentageChartType], .sqlBox", function () {
            showWarningModalAndCheck();
        });

        function showWarningModalAndCheck() {
            if (($("#showChartSheet").is(":checked")) && (!$("#chartExportAsImage").is(":checked")) && !chartWarningShown && hasComplexChartOptions()) {
                chartWarningShown = true;
                $("#chartExportAsImage").prop("checked", true);
                $("#chartExportAsImageWarning").modal("show");
                $("#chartExportAsImageWarning").find("#warningButton").detach();
                $("#chartExportAsImageWarning").find(".pv-btn-grey").text("ok");
            }
        }
    }

    function showEasyChart(container, templateType) {
        if (!viewOnly) {
            _.observe(newRowList, function (item, index) {
                easyChart.setData(generateChartData(templateType));
            });
            _.observe(columnSetList, function (item, index) {
                easyChart.setData(generateChartData(templateType));
            });
            _.observe(measureList, function (item, index) {
                easyChart.setData(generateChartData(templateType));
            });
        }
        easyChart.setData(generateChartData(templateType));
        container.show();
        $(".chartSettingsHeader").show();
    }

    function hideEasyChart(container) {
        if (!viewOnly) {
            _.unobserve(newRowList);
            _.unobserve(columnSetList);
            _.unobserve(measureList);
        }
        container.hide();
        $(".chartSettingsHeader").hide();
    }

    function randomBetween(min, max) {
        if (min < 0) {
            return Math.floor(min + Math.random() * (Math.abs(min) + max));
        } else {
            return Math.floor(min + Math.random() * max);
        }
    }

    function generateChartData(templateType) {
        var data = [];
        var series = [""];
        if (templateType == 'DATA_TAB') {
            updateChartColumnsData(data, series);
            if (newRowList.length > 0) {
                updateChartRowsData(data, series);
            }else{
                updateChartWithoutRowsData(data, series);
            }
        } else {
            data = generateDefaultData();
        }
        return data;
    }

    function updateChartColumnsData(data, series) {
        measureList.forEach(function (measureSet, index) {
            if (columnSetList && columnSetList.length > index && columnSetList[index].length > 0) {
                columnSetList[index].forEach(function (column) {
                    for (var i = 0; i < 3; i++) {
                        measureSet.forEach(function (measure) {
                            if (measure) {
                                series.push(column.text + (i + 1) + "(" + measure.name + ")");
                            }
                        });
                    }
                });
            } else {
                measureSet.forEach(function (measure) {
                    if (measure) {
                        series.push(measure.name);
                    }
                });
            }
        });
        data.push(series);
    }

    function updateChartRowsData(data, series) {
        if (newRowList.length > 0) {
            for (var i = 0; i < 5; i++) {
                var item = [];
                for (var j = 0; j < series.length; j++) {
                    if (j == 0) {
                        item.push(newRowList.slice(-1).pop().text + (i + 1));
                    } else {
                        item.push(randomBetween(0, 20));
                    }
                }
                data.push(item);
            }
        }
    }

    function updateChartWithoutRowsData(data, series) {
        for (var i = 0; i < 1; i++) {
            var item = [];
            for (var j = 0; j < series.length; j++) {
                item.push(randomBetween(0, 20));
            }
            data.push(item);
        }
    }

    function generateDefaultData() {
        var data = [];
        var series = ["", "Column"];
        data.push(series);
        for (var i = 0; i < 5; i++) {
            var item = [];
            for (var j = 0; j < series.length; j++) {
                if (j == 0) {
                    item.push("Row" + (i + 1));
                } else {
                    item.push(randomBetween(0, 20));
                }
            }
            data.push(item);
        }
        return data;
    }

    function customizePresets(easyChart) {
        var presets = easyChart.getPresets();
        presets.credits = {enabled: false};
        easyChart.setPresets(presets);
    }

    function customizeTemplates(easyChart) {
        var excludedIds = ["polar", "combinationCharts", "dashboard"];
        var templates = easyChart.getTemplates();
        easyChart.setTemplates(templates.filter(function (value) {return (excludedIds.indexOf(value.id) < 0) }));
    }

    function customizeOptions(easyChart) {
        var excludedIds = ["colorsAndBorders", "valueLabels", "credits", "tooltip", "exporting"];
        var customChartTypes = "[\"line\", \"spline\", \"column\", \"bar\", \"area\", \"areaspline\", \"pie\", \"arearange\", \"areasplinerange\", \"columnrange\", \"scatter\"]";

        var options = easyChart.getOptions();
        options = options.filter(function (value) {return (excludedIds.indexOf(value.id) < 0) });
        options[0].panes[0].options[0].values = customChartTypes;
        easyChart.setOptions(options);
    }
});
