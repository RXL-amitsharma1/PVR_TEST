<%@ page import="com.rxlogix.enums.PeriodicReportTypeEnum; grails.converters.JSON" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:link controller="pvp" action="reports"
                class="rxmain-container-header-label rx-widget-title"><g:message
                code="app.widget.button.compliancePublisher.label"/></g:link>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div class="row rx-widget-content nicescroll">
        <div class="publisherSummary">
            <span class="publisherWidgetTitle${index}"><span
                    class="rrTitleContent">${message(code: "app.widget.button.compliancePublisher.label")}</span><span
                    class="fa fa-edit rrTitleIcon"></span>
            </span>
            <span class="publisherWidgetSearchForm${index}" style="display: none">
                <div class="alert alert-danger alert-dismissible forceLineWrap errorDiv" role="alert" hidden="hidden">
                    <button type="button" class="close" data-dismiss="alert">
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

                <div><input class="form-control" width="100%" maxlength="255" name="title"
                            placeholder="${message(code: "placeholder.templateQuery.title")}">
                </div>

                <div class="row">
                    <div class="col-sm-4">
                        <b><g:message code="app.label.reportSubmission.destinations"/>:</b>

                        <input id="reportingDestinations${index}" name="reportingDestinations" class="form-control"
                               multiple="multiple"/>
                    </div>

                    <div class="col-sm-4">
                        <b class="publisherWidget"><g:message code="app.widget.button.quality.product.label"/>:</b>
                        <input id="product${index}" name="product" class="form-control" multiple="multiple"/>
                    </div>

                    <div class="col-sm-4">
                        <b class="publisherWidget"><g:message code="app.label.reportType"/>:</b>
                        <g:select id="periodicReportType${index}" name="periodicReportType"
                                  from="${PeriodicReportTypeEnum.asList}" multiple="true" optionKey="key"
                                  class="form-control select2"/>
                    </div>
                </div>

                <div class="row fuelux" style="margin-top: 3px">
                    <div class="col-sm-2" style="margin-top: 7px;">
                        <b><g:message default="Due Date Range"
                                      code="app.widget.button.compliancePublisher.dueDateRange"/>:</b>
                    </div>

                    <div class="col-sm-3">
                        <div class="datepicker input-group">
                            <input id="dueDateRangeFrom${index}" name="dueDateRangeFrom" class="form-control"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>

                    <div class="col-sm-1" style="text-align: center;margin-top: 7px;"><b>-</b></div>

                    <div class="col-sm-3">
                        <div class="datepicker input-group">
                            <input id="dueDateRangeTo${index}" name="dueDateRangeTo" class="form-control"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>

                </div>

                <div class="row">
                    <div class="col-sm-2" style="margin-top: 7px;">
                        <b><g:message code="app.widget.button.compliancePublisher.groupBy" default="Group By"/>:</b>
                    </div>

                    <div class="col-sm-4">
                        <select id="groupBy${index}" name="groupBy" class="form-control select2">
                            <option value="product_type"><g:message
                                    code="app.widget.button.compliancePublisher.product_type"/></option>
                            <option value="product_destination"><g:message
                                    code="app.widget.button.compliancePublisher.product_destination"/></option>
                            <option value="destination_type"><g:message
                                    code="app.widget.button.compliancePublisher.destination_type"/></option>
                            <option value="destination_product"><g:message
                                    code="app.widget.button.compliancePublisher.destination_product"/></option>
                            <option value="type_product"><g:message
                                    code="app.widget.button.compliancePublisher.type_product"/></option>
                            <option value="type_destination"><g:message
                                    code="app.widget.button.compliancePublisher.type_destination"/></option>
                        </select>
                    </div>
                    %{--                    <div class="col-sm-4">--}%
                    %{--                        <b class="publisherWidget"><g:message code="app.widget.button.compliancePublisher.chartType" default="Chart Type"/>:</b>--}%
                    %{--                        <select id="chartType${index}" name="chartType" class="form-control select2">--}%
                    %{--                            <option value="column"><g:message code="app.widget.button.compliancePublisher.column" default="Column Chart"/></option>--}%
                    %{--                            <option value="bar"><g:message code="app.widget.button.compliancePublisher.bar" default="Bar Chart"/></option>--}%
                    %{--                        </select>--}%
                    %{--                    </div>--}%
                    %{--                    <div class="col-sm-4">--}%
                    %{--                        <b class="publisherWidget"><g:message code="app.widget.button.compliancePublisher.display" default="Display"/>:</b>--}%
                    %{--                        <select id="display${index}" name="display" class="form-control select2" multiple>--}%
                    %{--                            <option value="complianceRate"><g:message code="app.widget.button.compliancePublisher.complianceRate" default="Compliance Rate"/></option>--}%
                    %{--                            <option value="totalSubmissions"><g:message code="app.widget.button.compliancePublisher.totalSubmissions" default="Total Submissions"/></option>--}%
                    %{--                        </select>--}%
                    %{--                    </div>--}%
                </div>

                <div class="row">
                    <div class="col-sm-12">
                        <g:if test="${isEditable}">
                            <button class="btn btn-primary savePublisherWidget">
                                ${message(code: "default.button.save.label")}
                            </button>
                        </g:if>
                        <button class="btn btn-primary publisherWidgetHideButton${index}">
                            ${message(code: "app.label.hideOptions")}
                        </button>
                    </div>
                </div>
            </span>
        </div>

        <div id="chart${index}" style="width: 100%; height: 300px"></div>

        <div class="pv-caselist">
            <table id="rxTablePublisher${index}" class="table table-striped pv-list-table dataTable no-footer"
                   width="100%">
                <thead>
                <tr>
                    <th class="groupingColumn${index}"></th>
                    <th><g:message code="app.widget.button.compliancePublisher.late"/></th>
                    <th><g:message code="app.widget.button.compliancePublisher.total"/></th>
                    <th><g:message code="app.widget.button.compliancePublisher.complianceRate"/></th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</div>
<input type="hidden" id="widgetSettings${index}" value="${widget.reportWidget.settings}"/>
<script>
    var workflowJustificationPublisherUrl = "${createLink(controller: 'workflowJustificationRest', action: 'index')}";
    var workflowJustificationConfirmPublisherUrl = "${createLink(controller: 'workflowJustificationRest', action: 'save')}";
    var ACTION_ITEM_GROUP_STATE_ENUM = {
        WAITING: 'WAITING',
        OVERDUE: 'OVERDUE',
        CLOSED: 'CLOSED'
    };
    $(function () {

        var tableSortColumnNumber${index} = 0;
        var tableShowNumber${index} = 5;
        var tableSortColumnOrder${index} = 'asc';
        var publisherTable;

        function setValues() {
            var settingsString = $("#widgetSettings${index}").val();
            if (settingsString) {
                var settings = JSON.parse(settingsString);
                var $container = $(".publisherWidgetSearchForm${index}");
                if (settings.title) {
                    $('.publisherWidgetTitle${index} .rrTitleContent').html(encodeToHTML(settings.title));
                }
                for (var fieldName in settings) {
                    $container.find('input[name=' + fieldName + ']').val(settings[fieldName]).trigger("change");
                    $container.find('select[name=' + fieldName + ']').val(settings[fieldName]).trigger("change");
                }
                tableSortColumnNumber${index} = (settings.sort ? settings.sort : 0);
                tableShowNumber${index} = (settings.show ? settings.show : 5);
                tableSortColumnOrder${index} = (settings.order ? settings.order : "asc");
            }
        }

        var loadData = function () {
            $.ajax({
                "url": "${createLink(controller: 'dashboard', action: 'getAdvancedPublisher')}",
                "dataType": 'json'
            }).done(function (data) {
                bindMultipleSelect2WithUrl($("#reportingDestinations${index}"), "${createLink(controller: 'queryRest', action: 'getReportingDestinations')}", true);
                bindMultipleSelect2WithUrl($("#product${index}"), "${createLink(controller: 'periodicReportConfigurationRest', action: 'getReportingProducts')}", true, false, false, null, 3);
                $("#groupBy${index}").select2();
                $("#periodicReportType${index}").select2();
                $(".publisherWidgetSearchForm${index} .datepicker").datepicker({
                    allowPastDates: true,
                    twoDigitYearProtection: true,
                    date: null,
                    momentConfig: {
                        culture: userLocale,
                        format: DEFAULT_DATE_DISPLAY_FORMAT
                    }
                });
                setValues();
                var settings = getCurrentSettings();
                publisherTable = initTable();

                $(".publisherWidget input").on('click', function () {
                    var settings = getCurrentSettings();
                    $("#widgetSettings${index}").val(JSON.stringify(settings));
                    publisherTable.ajax.reload();
                });
            });
        };


        $(".publisherWidgetTitle${index}").on('click', function () {
            $(".publisherWidgetSearchForm${index}").show();
            $(".publisherWidgetTitle${index}").hide();
            $(this).closest(".rx-widget-content").find(".dataTables_length").parent().show();
        });

        $(".publisherWidgetHideButton${index}").on('click', function () {
            $(".publisherWidgetSearchForm${index}").hide();
            $(".publisherWidgetTitle${index}").show();
            $(this).closest(".rx-widget-content").find(".dataTables_length").parent().hide();
        });

        function reload() {
            var settings = getCurrentSettings();
            $("#widgetSettings${index}").val(JSON.stringify(settings));
            publisherTable.ajax.reload();
        }

        $(".savePublisherWidget").on('click', function () {
            var $container = $(this).parent();
            var settings = getCurrentSettings();
            var settingsString = JSON.stringify(settings);
            $("#widgetSettings${index}").val(settingsString);
            $.ajax({
                url: "${createLink(controller: 'dashboard', action: 'updateWidgetSettings')}",
                type: 'post',
                data: {id:${widget.reportWidget.id}, data: settingsString},
                dataType: 'json'
            })
                .done(function (data) {
                    $container.find(".successDiv").show();
                    reload()
                    setTimeout(function () {
                        $container.find(".successDiv").hide();
                    }, 2000);
                    $('.publisherWidgetTitle${index} .rrTitleContent').html(encodeToHTML(settings.title));
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
            $(".publisherWidgetSearchForm${index}").find("input, select").each(function (i) {
                $this = $(this);
                settings[$this.attr("name")] = $this.val();

            });
            settings.sort = tableSortColumnNumber${index};
            settings.show = tableShowNumber${index};
            settings.order = tableSortColumnOrder${index};
            return settings;
        }

        function initTable() {
            return $("#rxTablePublisher${index}").DataTable({

                //  "sPaginationType": "bootstrap",
                "stateSave": false,
                "iDisplayLength": tableShowNumber${index},
                "bLengthChange": true,
                "aLengthMenu": [5, 10, 20, 50],
                "ajax": {
                    "url": "${createLink(controller: 'periodicReportConfigurationRest', action: 'compliancePublisherWidgetSearch')}",
                    "dataSrc": "data",
                    "data": function (d) {
                        d.wFilter = $("#widgetSettings${index}").val();
                    }
                },
                fnInitComplete: function () {
                    $(".publisherWidgetSearchForm${index}").parent().parent().find(".dataTables_length").parent().hide();

                },
                "drawCallback": function (settings) {

                    var response = settings.json;
                    if (response) {
                        $(".groupingColumn${index}").text(response.label);
                        createChart(response);
                    }

                },
                "searching": false,
                "aoColumns": [
                    {
                        "data": "name"
                    }, {
                        "data": "late"
                    }, {
                        "data": "total"
                    }, {
                        "data": "rate"
                    }
                ]
            });
        }

        $(document).on("click", ".rrStatusBtn${index}", function () {
            var statusObject = $(this);
            var modal = $("#workflowStatusJustification");
            reloadData = function (rowId, resetPagination, tableId, response) {
                reportReqestTable.ajax.reload();
                $.ajax({
                    "url": reportRequestSummaryUrl,
                    "dataType": 'json'
                }).done(function (data) {
                    for (var i in ["user", "priority", "due"]) {
                        for (var j in data.result[i]) {
                            $('.' + i + '_' + data.result[i] + "_val").html(data.result[i][j]);
                        }
                    }
                    for (var i in data.result.status) {
                        $('.status_' + data.result.status[i].id + "_val").html(data.result.status[i].count);
                    }
                });
            };
            doAction = function (rowId, tableId, actionClassName) {
            };
            modal.find(".confirm-workflow-justification").attr("data-evt-clk",
                JSON.stringify({
                    method: "confirmJustification",
                    params: [`rxTableReportRequests${index}`, workflowJustificationConfirmPublisherUrl]
                })
            );
            modal.find(".required-indicator").show();
            openStateHistoryModal(this, workflowJustificationPublisherUrl);
        });

        function createChart(data) {
            var series = [];
            series = series.concat(data.subgroups);
            series.push({
                name: "${message(code:'app.widget.button.compliancePublisher.ontimeSubmissions')}",
                type: 'line',
                data: data.ontime,
                tooltip: {
                    pointFormatter: function () {
                        return "${message(code:'app.widget.button.compliancePublisher.ontimeSubmissions')} " + data.ontime[this.index] + " (" + data.rate[this.index] + "%)";
                    }
                }
            });

            Highcharts.chart('chart${index}', {
                chart: {
                    type: 'column'
                },
                title: {
                    text: ''
                },
                xAxis: {
                    categories: data.X
                },
                yAxis: {
                    min: 0,
                    title: {
                        text: '${message(code:"app.widget.button.compliancePublisher.totalSubmissions")}'
                    },
                    stackLabels: {
                        enabled: true,
                        style: {
                            fontWeight: 'bold',
                            color: ( // theme
                                Highcharts.defaultOptions.title.style &&
                                Highcharts.defaultOptions.title.style.color
                            ) || 'gray'
                        }
                    }
                },
                legend: {
                    align: 'right',
                    x: -30,
                    verticalAlign: 'top',
                    y: 25,
                    floating: true,
                    backgroundColor:
                        Highcharts.defaultOptions.legend.backgroundColor || 'white',
                    borderColor: '#CCC',
                    borderWidth: 1,
                    shadow: false
                },
                tooltip: {
                    headerFormat: '<b>{point.x}</b><br/>',
                    pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
                },
                plotOptions: {
                    column: {
                        stacking: 'normal',
                        dataLabels: {
                            enabled: true
                        }
                    }
                },
                series: series
            });
        }

        $('#refresh-widget${index}').hide();
        loadData();

    });
</script>