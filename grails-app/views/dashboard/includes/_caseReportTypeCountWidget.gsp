<%@ page import="com.rxlogix.enums.PvqTypeEnum" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <a href="javascript:void(0)" class="rxmain-container-header-label rx-widget-title"
           name="reportTypeCountTitle${index}"
           data-url="${createLink(controller: 'quality', action: 'redirectFromWidget')}">
            <g:message code="app.widget.button.quality.caseReportTypeCount.label"/>
        </a>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div class="row rx-widget-content nicescroll">
        <div class="reportTypeCount${index}">
            <span class="reportTypeCountWidgetTitle${index}"><span
                    class="rtcTitleContent">${message(code: "app.widget.reportRequest.no.title")}</span><span
                    class="fa fa-edit rrTitleIcon"></span></span>
            <span class="reportTypeCountWidgetSearchForm${index}" style="display: none">
                <div class="alert alert-danger alert-dismissible forceLineWrap errorDiv" role="alert" hidden="hidden">
                    <button type="button" class="close" name="reportTypeCountCloseButton${index}">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>

                    <p class="errorContent"></p>
                </div>

                <div class="alert alert-success alert-dismissible forceLineWrap successDiv" role="alert"
                     hidden="hidden">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>

                    <p><g:message code="app.label.saved"/></p>
                </div>

                <div><input class="form-control" width="100%" maxlength="255" name="rtcTitle"
                            placeholder="${message(code: "placeholder.templateQuery.title")}"></div>

                <div class="row">
                    <div class="col-sm-12">
                        <b><g:message code="qualityModule.manualAdd.errorType.label"/>:</b>
                        <span class="checkbox checkbox-primary reportTypeCountWidget${index}">
                            <input type="checkbox" id="quality_case${index}" name="quality_case"/>
                            <label for="quality_case${index}" id="quality_case_lbl${index}"><g:message
                                    code="app.label.case.data.quality"/></label>
                        </span>
                        <span class="checkbox checkbox-primary reportTypeCountWidget${index}">
                            <input type="checkbox" id="quality_submission${index}" name="quality_submission"/>
                            <label for="quality_submission${index}" id="quality_submission_lbl${index}"><g:message
                                    code="app.label.submission.quality"/></label>
                        </span>
                    </div>
                </div>
                <button class="btn btn-primary saveReportTypeCountWidget${index}">
                    ${message(code: "default.button.save.label")}
                </button>
                <button class="btn btn-primary reportTypeCountWidgetHideButton${index}">
                    ${message(code: "app.label.hideOptions")}
                </button>
            </span>
        </div>

        <div class="row rx-widget-content">
            <div class="btn-group-sm" data-toggle="buttons">
                <label class="btn btn-sm btn-primary interval${index}" data-type="5day">
                    <input type="radio" name="interval${index}" id="option1" autocomplete="off"><g:message
                        code="app.widget.button.quality.5day.label"/>
                </label>
                <label class="btn btn-sm  btn-primary interval${index}" data-type="week">
                    <input type="radio" name="interval${index}" id="option2" data-type="week"
                           autocomplete="off"><g:message code="app.widget.button.quality.week.label"/>
                </label>
                <label class="btn btn-sm  btn-primary interval${index}" data-type="month">
                    <input type="radio" name="interval${index}" id="option3" autocomplete="off"><g:message
                        code="app.widget.button.quality.month.label"/>
                </label>
                <label class="btn btn-sm  btn-primary interval${index}" data-type="year">
                    <input type="radio" name="interval${index}" id="option4" autocomplete="off"> <g:message
                        code="app.widget.button.quality.year.label"/>
                </label>
                <label class="btn btn-sm  btn-primary interval${index}" data-type="5year">
                    <input type="radio" name="interval${index}" id="option5" autocomplete="off"><g:message
                        code="app.widget.button.quality.5year.label"/>
                </label>
                <label class="btn btn-sm  btn-primary active interval${index}" data-type="">
                    <input type="radio" name="interval${index}" id="option6" autocomplete="off" checked><g:message
                        code="app.widget.button.quality.all.label"/>
                </label>
            </div>
            <div class="quality-chart-container " id="caseReportTypeCount${index}" style="width: 100%;  position: relative;height: calc(100% - 25px);!important;"></div>
        </div>
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
            $('[name="reportTypeCountCloseButton${index}"]').on('click', function () {
                var $container = $(".reportTypeCountWidgetSearchForm${index}");
                $container.find(".errorContent").html('');
                $container.find(".errorDiv").hide();
            })
        });

        $(function () {
            $('[name="reportTypeCountTitle${index}"]').on('click', function () {
                var redirectUrl = $(this).attr("data-url");
                $(this).attr('href', redirectUrl + '?dataType=' + $("#dataType${index}").val());
            });
        })

        function loadFilter() {
            setCheckboxes();
            $(".reportTypeCountWidget${index} input").on('click', function () {
                var settings = getCurrentSettings();
                $("#widgetSettings${index}").val(JSON.stringify(settings));
                loadData(getDate(selectedType), getToDate(selectedType), getDataType());
            });
        }

        function checkByDefault(type) {
            var $container = $(".reportTypeCountWidgetSearchForm${index}");
            $container.find('input[name=' + type + "_case]").prop('checked', true);
            $container.find('input[name=' + type + "_submission]").prop('checked', true);
        }

        function setCheckboxes() {
            var settingsString = $("#widgetSettings${index}").val();
            if (settingsString) {
                var settings = JSON.parse(settingsString);
                var $container = $(".reportTypeCountWidgetSearchForm${index}");
                if (settings.title) {
                    $container.find('input[name=rtcTitle]').val(settings.title);
                    $('.reportTypeCountWidgetTitle${index} .rtcTitleContent').html(encodeToHTML(settings.title));
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
        loadData(getDate(selectedType), getToDate(selectedType), getDataType());

        $(document).on("click", ".interval${index}", function () {
            var type = $(this).attr("data-type");
            selectedType = type;
            $('#caseReportTypeCount${index} .highcharts-container').hide();
            loadData(getDate(type), getToDate(type), getDataType());
        })


        function loadData(fromDate, toDate, qualityDataType) {
            if (!qualityDataType) {
                qualityDataType = getDataType();
            }
            var fromToDate = null;
            if (fromDate)
                fromToDate = "?from=" + fromDate;
            if (toDate)
                fromToDate += "&to=" + toDate;
            $.ajax({
                "url": caseReportTypeCountUrl + (fromToDate ? (fromToDate + "&dataType=" + qualityDataType) : "?dataType=" + qualityDataType),
                "dataType": 'json'
            }).done(function (data) {
                chartData = data;
                renderChart();

            });
        }
        $(document).on("change", "#chartData${index}", function () {
            renderChart()
        });

        function renderChart() {
            $('#caseReportTypeCount${index} .highcharts-container').show();
            Highcharts.chart('caseReportTypeCount${index}', {
                chart: {
                    type: 'bar'
                },
                title: {
                    text: ''
                },
                subtitle: {
                    text: ''
                },
                xAxis: {
                    categories: chartData.fieldNameList,
                    title: {
                        text: '<g:message code="app.widget.button.quality.caseReportType.label"/>'
                    },
                    labels: {
                        formatter: function () {
                            const label = this.value;
                            const maxChars = 20;
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
                        text: '${message(code: "chart.label.yAxis.caseCount")}',
                        align: 'high'
                    },
                    labels: {
                        overflow: 'justify'
                    }
                },
                tooltip: {
                    valueSuffix: ''
                },
                plotOptions: {
                    series: {
                        stacking: 'normal'
                    }
                },
                legend: {
                    enabled: false // This hides the legend
                },
                credits: {
                    enabled: false
                },
                series: chartData.errors
            });
        }
        function getDate(type) {
            if (!type || type == "") return null;
            var d = new Date();
            if (type == "5day") {
                d.setDate(d.getDate() - 5);
            } else if (type == "week") {
                // set to Monday of this week
                d.setDate(d.getDate() - (d.getDay() + 6) % 7);
                // set to previous Monday
                d.setDate(d.getDate() - 7);
            } else if (type == "month") {
                d = new Date(d.getFullYear(), d.getMonth() - 1, 1);
            } else if (type == "year") {
                d = new Date(d.getFullYear() - 1, 0, 1);
            } else if (type == "5year") {
                d = new Date(d.getFullYear() - 5, 0, 1);
            }
            return d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate();
        }

        function isLeapYear(year) {
            return (((year % 4 === 0) && (year % 100 !== 0)) || (year % 400 === 0));
        }

        function daysInMonth(year, month) {
            return [31, (isLeapYear(year) ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
        }

        function getToDate(type) {
            if (!type || type == "") return null;
            var d = new Date();
            if (type == "5day") {
                d = new Date();
            } else if (type == "week") {
                // set to Monday of this week
                d.setDate(d.getDate() - (d.getDay() + 6) % 7);
                // set to previous Monday
                d.setDate(d.getDate() - 1);
            } else if (type == "month") {
                d = new Date(d.getFullYear(), d.getMonth() - 1, daysInMonth(d.getFullYear(), d.getMonth() - 1));
            } else if (type == "year") {
                d = new Date(d.getFullYear() - 1, 11, 31);
            } else if (type == "5year") {
                d = new Date(d.getFullYear() - 1, 11, 31);
            }
            return d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate();
        }

        $(".reportTypeCountWidgetTitle${index}").on('click', function () {
            $(".reportTypeCountWidgetSearchForm${index}").show();
            $(".reportTypeCountWidgetTitle${index}").hide();
            $(this).closest(".rx-widget-content").find(".dataTables_length").parent().show();
        });

        $(".reportTypeCountWidgetHideButton${index}").on('click', function () {
            $(".reportTypeCountWidgetSearchForm${index}").hide();
            $(".reportTypeCountWidgetTitle${index}").show();
            $(this).closest(".rx-widget-content").find(".dataTables_length").parent().hide();
        });

        $(".saveReportTypeCountWidget${index}").on('click', function () {
            if (checkIfDataTypeSelected()) {
                return;
            }
            var $container = $(this).parent();
            var settings = getCurrentSettings();
            var settingsString = JSON.stringify(settings);
            $("#widgetSettings${index}").val(settingsString);
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
                        $(".reportTypeCountWidgetSearchForm${index}").hide();
                        $(".reportTypeCountWidgetTitle${index}").show();
                    }, 1000);
                    $('.reportTypeCountWidgetTitle${index} .rtcTitleContent').html(encodeToHTML(settings.title));
                })
                .fail(function (err) {
                        var mess = (err.responseJSON.message ? err.responseJSON.message : "") +
                            (err.responseJSON.stackTrace ? "\n" + err.responseJSON.stackTrace : "");
                        $container.find(".errorContent").html(mess);
                        $container.find(".errorDiv").show();
                    }
                );
        });

        function getCurrentSettings() {
            var settings = {};
            $(".reportTypeCountWidgetSearchForm${index}").find("input").each(function (i) {
                $this = $(this);
                if ($this.attr("name") === "rtcTitle") {
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
            var $container = $(".reportTypeCountWidgetSearchForm${index}");
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