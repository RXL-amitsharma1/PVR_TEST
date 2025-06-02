<%@ page import="grails.converters.JSON" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:link controller="pvp" action="reports"
                class="rxmain-container-header-label rx-widget-title"><g:message
                code="app.widget.button.advancedPublisher.label"/></g:link>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div class="row rx-widget-content nicescroll">
        <div class="publisherSummary">
            <span class="publisherWidgetTitle${index}"><span
                    class="rrTitleContent">${message(code: "app.widget.reportRequest.no.title")}</span><span
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

                <div><input class="form-control" maxlength="255" width="100%" name="rrTitle"
                            placeholder="${message(code: "placeholder.templateQuery.title")}">
                </div>

                <div class="row">
                    <div class="col-sm-12">
                        <b><g:message code="user.label"/>:</b>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="user_owner${index}" name="user_owner"/>
                            <label for="user_owner${index}" id="user_owner_lbl${index}"><g:message
                                    code="app.widget.reportRequest.owner"/></label>
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="user_assigned${index}" name="user_assigned"/>
                            <label for="user_assigned${index}" id="user_assigned_lbl${index}"><g:message
                                    code="app.widget.reportRequest.assigned"/></label>
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="user_all${index}" name="user_all"/>
                            <label for="user_all${index}"><g:message code="app.widget.reportRequest.all"/></label>
                        </span>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-12">
                        <b><g:message code="app.label.publisher.stage" default="Publisher Stage"/>:</b>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="stage_new${index}" name="stage_new"/>
                            <label for="stage_new${index}"><g:message code="app.label.publisher.new"/></label>
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="stage_sections${index}" name="stage_sections"/>
                            <label for="stage_sections${index}"><g:message code="app.label.publisher.sections"/></label>
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="stage_publishing${index}" name="stage_publishing"/>
                            <label for="stage_publishing${index}"><g:message
                                    code="app.label.publisher.publishing"/></label>
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="stage_published${index}" name="stage_published"/>
                            <label for="stage_published${index}"><g:message
                                    code="app.label.publisher.published"/></label>
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="stage_all${index}" name="stage_all"/>
                            <label for="stage_all${index}"><g:message code="app.widget.reportRequest.all"/></label>
                        </span>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-12">
                        <b><g:message code="app.report.request.dueDate.label"/>:</b>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="due_overdue${index}" name="due_overdue"/>
                            <label for="due_overdue${index}" id="due_overdue_lbl${index}"><g:message
                                    code="app.widget.overdue"/></label>
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="due_today${index}" name="due_today"/>
                            <label for="due_today${index}" id="due_today_lbl${index}"><g:message
                                    code="app.widget.dueToday"/></label>
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="due_tomorrow${index}" name="due_tomorrow"/>
                            <label for="due_tomorrow${index}" id="due_tomorrow_lbl${index}"><g:message
                                    code="app.widget.dueTomorrow"/></label>
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="due_five${index}" name="due_five"/>
                            <label for="due_five${index}" id="due_five_lbl${index}"><g:message
                                    code="app.widget.due5"/></label>
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="due_all${index}" name="due_all"/>
                            <label for="due_all${index}"><g:message code="app.widget.reportRequest.all"/></label>
                        </span>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-12">
                        <b><g:message code="app.label.report.request.status"/>:</b>
                        <span class="statusDiv${index}" style="margin-left: 15px;">
                        </span>
                        <span class="checkbox checkbox-primary publisherWidget">
                            <input type="checkbox" id="status_all${index}" name="status_all"/>
                            <label for="status_all${index}"><g:message code="app.widget.reportRequest.all"/></label>
                        </span>
                    </div>
                </div>
                <g:if test="${isEditable}">
                    <button class="btn btn-primary savePublisherWidget">
                        ${message(code: "default.button.save.label")}
                    </button>
                </g:if>
                <button class="btn btn-primary publisherWidgetHideButton${index}">
                    ${message(code: "app.label.hideOptions")}
                </button>

            </span>
        </div>

        <div class="pv-caselist">
            <table id="rxTablePublisher${index}" class="table table-striped pv-list-table dataTable no-footer"
                   width="100%">
                <thead>
                <tr>

                    <th><g:message code="app.label.reportName"/></th>
                    <th><g:message code="app.periodicReport.executed.dateModified.label"/></th>
                    <th><g:message code="app.label.reportingDestinations"/></th>
                    <th><g:message code="app.periodicReport.executed.daysLeft.label"/></th>
                    <th><g:message code="app.periodicReport.executed.workflowState.label"/></th>
                    <th><g:message default="Status" code="app.label.action.app.name"/></th>
                    <th><g:message code="app.label.PublisherTemplate.publisher"/></th>
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

        function createRow(status, code) {
            return '<span class="checkbox checkbox-primary publisherWidget">' +
                '<input type="checkbox" id="' + code + '_' + status.id + '${index}" name="' + code + '_' + status.id + '" />' +
                '<label for="' + code + '_' + status.id + '${index}">' + status.title + '</label></span>';
        }

        function setCheckboxes() {
            var settingsString = $("#widgetSettings${index}").val();
            if (settingsString) {
                var settings = JSON.parse(settingsString);
                var $container = $(".publisherWidgetSearchForm${index}");
                if (settings.title) {
                    $container.find('input[name=rrTitle]').val(encodeToHTML(settings.title));
                    $('.publisherWidgetTitle${index} .rrTitleContent').html(encodeToHTML(settings.title));
                }
                for (var i in settings) {
                    for (var j in settings[i]) {
                        $container.find('input[name=' + i + '_' + settings[i][j] + "]").prop('checked', true);
                    }
                }

                tableSortColumnNumber${index} = (settings.sort ? settings.sort : 0);
                tableShowNumber${index} = (settings.show ? settings.show : 5);
                tableSortColumnOrder${index} = (settings.order ? settings.order : "asc");

            }
        }

        function appendToLabel($label, val) {
            $label.html($label.text() + " (<span id='" + $label.attr("id") + "_val'>" + val + "</span>)");
        }

        var loadData = function () {
            $.ajax({
                "url": "${createLink(controller: 'dashboard', action: 'getAdvancedPublisher')}",
                "dataType": 'json'
            }).done(function (data) {
                var statuses = data.result.status;
                var statusesDiv = "";
                for (var i = 0; i < statuses.length; i++) {
                    statusesDiv += createRow(statuses[i], "status");
                }
                $(".statusDiv${index}").html(statusesDiv);

                setCheckboxes();
                var settings = getCurrentSettings();
                checkAllCheckbox(settings);

                publisherTable = initTable();

                $(".publisherWidget input").on('click', function () {
                    removeCheckboxIfAllClicked($(this));
                    var settings = getCurrentSettings();
                    checkAllCheckbox(settings);
                    $("#widgetSettings${index}").val(JSON.stringify(settings));
                    publisherTable.ajax.reload();
                });
            });
        };

        function removeCheckboxIfAllClicked($current) {
            var $container = $(".publisherWidgetSearchForm${index}");
            if ($current.attr("name") && $current.attr("name").indexOf("_all") > 0) {
                var type = $current.attr("name").split("_")[0];
                $container.find("input[name^=" + type + "]").each(function () {
                    if ($(this).attr("name") !== type + "_all")
                        $(this).prop('checked', false);
                })
            }
        }

        function checkAllCheckbox(settings) {
            var $container = $(".publisherWidgetSearchForm${index}");
            var types = ["user", "stage", "due", "status"];
            for (var i in types) {
                var type = types[i];
                if (!settings[type] || settings[type].length === 0) {
                    $container.find('input[name=' + type + "_all]").prop('checked', true);
                } else {
                    $container.find('input[name=' + type + "_all]").prop('checked', false);
                }
            }
        }

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


        $(".savePublisherWidget").on('click', function () {
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
            $(".publisherWidgetSearchForm${index}").find("input").each(function (i) {
                $this = $(this);
                if ($this.attr("name") === "rrTitle")
                    settings.title = $this.val();
                else if ($this.is(':checked')) {
                    var checked = $this.attr("name").split("_");
                    if (checked[1] !== 'all') {
                        if (!settings[checked[0]]) settings[checked[0]] = [];
                        settings[checked[0]].push(checked[1])
                    }
                }
                settings.sort = tableSortColumnNumber${index};
                settings.show = tableShowNumber${index};
                settings.order = tableSortColumnOrder${index};
            });
            return settings;
        }

        function initTable() {
            return $("#rxTablePublisher${index}").DataTable({

                // "sPaginationType": "bootstrap",
                "stateSave": false,
                "processing": true,
                "serverSide": true,
                "iDisplayLength": tableShowNumber${index},
                "bLengthChange": true,
                "aLengthMenu": [5, 10, 20, 50],
                "ajax": {
                    "url": "${createLink(controller: 'periodicReportConfigurationRest', action: 'advancedPublisherWidgetSearch')}",
                    "dataSrc": "data",
                    "data": function (d) {
                        //   d.length=5;
                        tableShowNumber${index} = d.length;
                        d.searchString = d.search.value;
                        d.wFilter = $("#widgetSettings${index}").val();
                        if (d.order.length > 0) {
                            d.direction = d.order[0].dir;
                            tableSortColumnOrder${index} = d.direction;
                            //Column header mData value extracting
                            tableSortColumnNumber${index} = d.order[0].column;
                            d.sort = d.columns[d.order[0].column].data;
                        }
                    }
                },
                fnInitComplete: function () {
                    $(".publisherWidgetSearchForm${index}").parent().parent().find(".dataTables_length").parent().hide();
                },
                "aaSorting": [[tableSortColumnNumber${index}, tableSortColumnOrder${index}]],
                "searching": false,
                "aoColumns": [
                    {
                        "data": "reportName",
                        "render": function (data, type, row) {
                            var ico = "";
                            var ban = ((row.casesOnly) ? '<span  style="margin-left: 5px;"  class="fa fa-ban" title="' + $.i18n._('publisherCasesOnly') + '" ></span>' : "");
                            return (ban ? (encodeToHTML(row.reportName) + " v." + row.version + ban) : ('<a  href="${createLink(controller: "pvp", action: "sections")}' + '?id=' + row.id + '">' + encodeToHTML(row.reportName) + " v." + row.version + '</a>'));
                        }
                    }, {
                        "data": "lastUpdated",
                        "aTargets": ["lastUpdated"],
                        "sClass": "dataTableColumnCenter",
                        "mRender": function (data, type, full) {
                            return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                        }
                    },
                    {
                        "data": "primaryReportingDestination",
                        mRender: function (data, type, row) {
                            // var text ="<span style='border: #AAAAAA solid 1px;border-radius: 2px;background: #0fef20'>"+ data+ "(P) </span>";
                            var text = data + "<B title='" + $.i18n._('app.advancedFilter.primaryReportingDestination') + "'> (P) </b>";
                            if (row.otherReportingDestinations) {
                                text += ", " + row.otherReportingDestinations
                            }
                            return text;
                        }
                    },
                    {
                        "data": "dueDate",
                        "render": function (data, type, row) {
                            var clazz = "";
                            if (row.indicator == "red") clazz = 'class="label-danger text-white"';
                            if (row.indicator == "yellow") clazz = 'class="label-primary"';
                            if (data) {
                                return '<span style="padding: 1px 4px 3px 4px;"' + clazz + '>' + moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) + "</span>";
                            }
                            return ""
                        }
                    },
                    {
                        "bSortable": false,
                        "data": "state",
                        "render": function (data, type, row) {
                            return '<button class="btn btn-default btn-xs" style="min-width: 100px" data-executed-config-id= "' + row.id + '" data-initial-state= "' + row.state + '" data-evt-clk=\'{\"method\": \"openStateHistoryModal\", \"params\": []}\'>' + row.state + '</button>';
                        }
                    },
                    {
                        "bSortable": false,
                        "data": null,
                        "sClass": "dataTableColumnCenter",
                        "render": function (data, type, row) {
                            var creationAction = null;
                            var clazz = "";
                            if (row.actionItemStatus) {
                                switch (row.actionItemStatus) {
                                    case ACTION_ITEM_GROUP_STATE_ENUM.OVERDUE:
                                        clazz = "btn btn-danger btn-xs";
                                        break;
                                    case ACTION_ITEM_GROUP_STATE_ENUM.WAITING:
                                        clazz = "btn btn-warning btn-xs";
                                        break;
                                    default:
                                        clazz = "btn btn-success btn-xs";
                                        break;
                                }
                                creationAction = '<button class="' + clazz + ' actionItemModalIcon" data-exconfig-id="' + row.id + '" style="width:70px;">' + $.i18n._('app.actionItemGroupState.' + row.actionItemStatus) + '</button>';
                            }
                            return creationAction;
                        }
                    }, {
                        "bSortable": false,
                        "data": "publisher",
                        "render": function (data, type, row) {
                            if (row.casesOnly)
                                return '<span class="fa fa-ban" title="' + $.i18n._('publisherCasesOnly') + '" ></span>';
                            if (data) {
                                return '<a class="btn btn-success btn-xs downloadUrl" ' +
                                    'data-name="' + row.publisherName + '" ' +
                                    'data-id="' + row.id + '" ' +
                                    'data-url="${createLink(controller: 'pvp', action: 'downloadPublisherReport', absolute: true)}?id=' + data + '"' +
                                    'href="javascript:void(0)"><span class="fa fa-download" title="Download"></span></a>' +
                                    '<a class="btn btn-success btn-xs" href="${createLink(controller: "pvp", action: "sections")}?id=' + row.id + '" >' + $.i18n._("Completed") + '</a> ';
                            } else {
                                return '<a class="btn btn-warning btn-xs" href="${createLink(controller: "pvp", action: "sections")}?id=' + row.id + '" >' + $.i18n._("InProgress") + '</a> ';
                            }
                        }
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

        $('#refresh-widget${index}').hide();
        loadData();
    });
</script>