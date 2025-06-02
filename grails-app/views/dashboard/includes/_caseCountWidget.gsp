<%@ page import="com.rxlogix.enums.PvqTypeEnum" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <a href="javascript:void(0)" class="rxmain-container-header-label rx-widget-title" name="caseCountTitle${index}"
           data-url="${createLink(controller: 'quality', action: 'redirectFromWidget')}">
            <g:message code="default.button.addCaseCountByErrorWidget.label"/>
        </a>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div class="row rx-widget-content nicescroll">
        <div class="caseCount${index}">
            <span class="caseCountWidgetTitle${index}"><span
                    class="ccTitleContent">${message(code: "app.widget.reportRequest.no.title")}</span><span
                    class="fa fa-edit rrTitleIcon"></span></span>
            <span class="caseCountWidgetSearchForm${index}" style="display: none">
                <div class="alert alert-danger alert-dismissible forceLineWrap errorDiv" role="alert" hidden="hidden">
                    <button type="button" class="close" name="caseCountCloseButton${index}">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>

                    <p class="errorContent"></p>
                </div>

                <div class="alert alert-success alert-dismissible forceLineWrap successDiv" id="successDivId"
                     role="alert" hidden="hidden">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>

                    <p><g:message code="app.label.saved"/></p>
                </div>

                <div><input class="form-control" width="100%" maxlength="255" name="ccTitle"
                            placeholder="${message(code: "placeholder.templateQuery.title")}"></div>

                <div class="row">
                    <div class="col-sm-12">
                        <b><g:message code="qualityModule.manualAdd.errorType.label"/>:</b>
                        <span class="checkbox checkbox-primary caseCountWidget${index}">
                            <input type="checkbox" id="quality_case${index}" name="quality_case"/>
                            <label for="quality_case${index}" id="quality_case_lbl${index}"><g:message
                                    code="app.label.case.data.quality"/></label>
                        </span>
                        <span class="checkbox checkbox-primary caseCountWidget${index}">
                            <input type="checkbox" id="quality_submission${index}" name="quality_submission"/>
                            <label for="quality_submission${index}" id="quality_submission_lbl${index}"><g:message
                                    code="app.label.submission.quality"/></label>
                        </span>
                    </div>
                </div>
                <button class="btn btn-primary saveCaseCountWidget${index}">
                    ${message(code: "default.button.save.label")}
                </button>
                <button class="btn btn-primary caseCountWidgetHideButton${index}">
                    ${message(code: "app.label.hideOptions")}
                </button>
            </span>
        </div>

        <div class="quality-chart-container " id="qualityChart${index}" style="width: 100%;  position: relative;height: calc(100% - 25px);!important;"></div>

    </div>
</div>
<input type="hidden" id="widgetSettings${index}" value="${widget.reportWidget.settings}"/>
<input type="hidden" id="dataType${index}" value="ALL">
<input type="hidden" class="chartData" id="chartData${index}">
<script>
    var selectedType = '';
    $(function () {
        let chartData;
        $(function () {
            $('[name="caseCountCloseButton${index}"]').on('click', function () {
                var $container = $(".caseCountWidgetSearchForm${index}");
                $container.find(".errorContent").html('');
                $container.find(".errorDiv").hide();
            })
        });

        $(function () {
            $('[name="caseCountTitle${index}"]').on('click', function () {
                var redirectUrl = $(this).attr("data-url");
                $(this).attr('href', redirectUrl + '?dataType=' + $("#dataType${index}").val());
            });
        })

        function loadFilter() {
            setCheckboxes();
            $(".caseCountWidget${index} input").on('click', function () {
                var settings = getCurrentSettings();
                $("#widgetSettings${index}").val(JSON.stringify(settings));
                loadData(getDataType());

            });
        }

        function checkByDefault(type) {
            var $container = $(".caseCountWidgetSearchForm${index}");
            $container.find('input[name=' + type + "_case]").prop('checked', true);
            $container.find('input[name=' + type + "_submission]").prop('checked', true);
        }

        function setCheckboxes() {
            var settingsString = $("#widgetSettings${index}").val();
            if (settingsString) {
                var settings = JSON.parse(settingsString);
                var $container = $(".caseCountWidgetSearchForm${index}");
                if (settings.title) {
                    $container.find('input[name=ccTitle]').val(settings.title);
                    $('.caseCountWidgetTitle${index} .ccTitleContent').html(encodeToHTML(settings.title));
                }
                for (var i in settings) {
                    for (var j in settings[i]) {
                        $container.find('input[name=' + i + '_' + settings[i][j] + "]").prop('checked', true);
                    }
                }
            } else {
                checkByDefault('quality');
            }
        }

        $('#refresh-widget${index}').hide();
        loadFilter();
        loadData(getDataType());

        function loadData(fromDate, qualityDataType) {
            if (!qualityDataType) {
                qualityDataType = getDataType();
            }
            $.ajax({
                "url": qualityCaseCountUrl + "?dataType=" + qualityDataType,
                "dataType": 'json'
            }).done(function (data) {
                chartData = data;
                renderChart();
            });
        };

        $(document).on("change", "#chartData${index}", function () {
            renderChart()
        });

        function renderChart() {
            var errorNameList = chartData.errorNameList;
            var errorTotalCountList = chartData.errorTotalCountList;
            var errorTotalCountJson = [];
            for (var i = 0; i < errorTotalCountList.length; i++) {
                errorTotalCountJson.push({
                    y: parseInt(errorTotalCountList[i]),
                    color: '#' + Math.random().toString(16).substr(2, 6)
                });
            }


            var chart = new Highcharts.Chart({
                chart: {
                    renderTo: 'qualityChart${index}',
                    type: 'column',
                    plotBorderWidth: 1,
                    zoomType: 'y'
                },
                exporting: {
                    enabled: true
                },
                credits: {
                    enabled: false
                },
                title: {
                    text: '',
                    align: 'left'
                },
                subtitle: {
                    text: ''
                },
                xAxis: {
                    categories: errorNameList,
                    labels: {
                        formatter: function () {
                            const label = this.value;
                            const maxChars = 15;
                            if (label.length > maxChars) {
                                return label.substring(0, maxChars) + '...';
                            }
                            return label;
                        }
                    }
                },
                yAxis: {
                    min: 0,
                    title: {
                        text: '${message(code: "chart.label.yAxis.caseCount")}'
                    },
                    lineWidth: 1
                },
                plotOptions: {
                    bar: {
                        stacking: 'normal',
                        cursor: 'pointer',
                        dataLabels: {
                            enabled: true,
                            formatter: function () {
                                return this.y;
                            }
                        }
                    }
                },
                tooltip: {
                    borderRadius: 10,
                    borderWidth: 2,
                    shadow: true,
                    formatter: function () {
                        var s = [];
                        var current = this.point.index;
                        $.each(this.series.chart.series, function (i, series) {
                            s.push('<span style="fill:' + series.color + ';font-weight:bold;">' + series.name + ' </span><span style="fill: rgb(0, 0, 0); font-weight: bold;"/>: ' +
                                series.processedYData[current] + '<span>');
                        });
                        return '<span style="font-weight: bold;">' + this.x + '</span>' + '<br/>' + s.join('<br/>')
                    }
                },
                series: [{
                    showInLegend: false,
                    name: 'Case Count',
                    color: '#333333',
                    data: errorTotalCountJson
                }]
            });
        }

        $('#refresh-widget${index}').hide();
        loadData(getDataType());

        $(".caseCountWidgetTitle${index}").on('click', function () {
            $(".caseCountWidgetSearchForm${index}").show();
            $(".caseCountWidgetTitle${index}").hide();
            $(this).closest(".rx-widget-content").find(".dataTables_length").parent().show();
        });

        $(".caseCountWidgetHideButton${index}").on('click', function () {
            $(".caseCountWidgetSearchForm${index}").hide();
            $(".caseCountWidgetTitle${index}").show();
            $(this).closest(".rx-widget-content").find(".dataTables_length").parent().hide();
        });

        $(".saveCaseCountWidget${index}").on('click', function () {
            if (checkIfDataTypeSelected()) {
                return;
            }
            var $container = $(this).parent();
            var settings = getCurrentSettings();
            var settingsString = JSON.stringify(settings);
            $("#widgetSettings${index}").val(settingsString);
            getDataType();
            $.ajax({
                url: "${createLink(controller: 'dashboard', action: 'updateWidgetSettings')}",
                type: 'post',
                data: {id:${widget.reportWidget.id}, data: settingsString},
                dataType: 'html'
            })
                .done(function (data) {
                    $container.find(".successDiv").show();
                    setTimeout(function () {
                        $container.find(".successDiv").hide();
                        $(".caseCountWidgetSearchForm${index}").hide();
                        $(".caseCountWidgetTitle${index}").show();
                    }, 1000);
                    $('.caseCountWidgetTitle${index} .ccTitleContent').html(encodeToHTML(settings.title));
                })
                .fail(function (err) {
                        var mess = (err.responseJSON.message ? err.responseJSON.message : "") +
                            (err.responseJSON.stackTrace ? "\n" + err.responseJSON.stackTrace : "");
                        $container.find(".errorContent").html(mess);
                        $container.find(".errorDiv").show();
                    }
                );
            $('#caseCount${index} .highcharts-container').hide();
        });

        function getCurrentSettings() {
            var settings = {};
            $(".caseCountWidgetSearchForm${index}").find("input").each(function (i) {
                $this = $(this);
                if ($this.attr("name") === "ccTitle") {
                    settings.title = $this.val();
                } else if ($this.is(':checked')) {
                    var checked = $this.attr("name").split("_");
                    if (checked[1] !== 'all') {
                        if (!settings[checked[0]]) settings[checked[0]] = [];
                        settings[checked[0]].push(checked[1])
                    }
                }
            });
            return settings;
        }

        function getDataType() {
            var qualityDataType = 'NONE';
            var caseData = document.getElementById('quality_case${index}').checked;
            var submissionData = document.getElementById('quality_submission${index}').checked;
            if (caseData && submissionData) {
                qualityDataType = 'ALL';
            } else {
                if (caseData) {
                    qualityDataType = '${PvqTypeEnum.CASE_QUALITY.name()}';
                } else if (submissionData) {
                    qualityDataType = '${PvqTypeEnum.SUBMISSION_QUALITY.name()}';
                }
            }
            $("#dataType${index}").val(qualityDataType);
            return qualityDataType;
        }

        function checkIfDataTypeSelected() {
            var $container = $(".caseCountWidgetSearchForm${index}");
            $container.find(".errorContent").html('');
            $container.find(".errorDiv").hide();
            var caseData = document.getElementById('quality_case${index}').checked;
            var submissionData = document.getElementById('quality_submission${index}').checked;
            if (!caseData && !submissionData) {
                var mess = 'Please select atleast one Error Type';
                $container.find(".errorContent").html(mess);
                $container.find(".errorDiv").show();
                return true;
            }
            return false;
        }
    });
</script>