<%@ page import="grails.converters.JSON" %>
<asset:javascript src="app/rxTitleOptions.js"/>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <g:link controller="reportRequest" action="index"
                title="${message(code: 'app.widget.button.advancedReportRequest.label')}"
                class="rxmain-container-header-label rx-widget-title"><g:message
                code="app.widget.button.advancedReportRequest.label"/></g:link>
        <i class="pull-right dropdown-toggle md md-list md-lg rxmain-dropdown-settings" id="dropdownMenu1"
           data-toggle="dropdown"></i>

        <div class="pull-right dropdown-menu" aria-labelledby="dropdownMenu1">
            <div class="rxmain-container-dropdown">
                <table id="tableColumns" class="table table-condensed rxmain-dropdown-settings-table">
                    <thead>
                    <tr>
                        <th><g:message code='app.label.name'/></th>
                        <th><g:message code='app.label.show'/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div class="row rx-widget-content nicescroll pv-caselist">
        <div class="reportRequestSummary">
            <span class="reportRequestWidgetTitle${index}"><span
                    class="rrTitleContent">${message(code: "app.widget.reportRequest.no.title")}</span><span
                    class="fa fa-edit rrTitleIcon"></span></span>
            <span class="reportRequestWidgetSearchForm${index}" style="display: none">
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

                <div><input class="form-control" width="100%" maxlength="255" name="rrTitle"
                            placeholder="${message(code: "placeholder.templateQuery.title")}"></div>

                <div class="row">
                    <div class="col-sm-12">
                        <b><g:message code="user.label"/>:</b>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="user_owner${index}" name="user_owner"/>
                            <label for="user_owner${index}" id="user_owner_lbl${index}"><g:message
                                    code="app.widget.reportRequest.owner"/></label>
                        </span>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="user_requested${index}" name="user_requested"/>
                            <label for="user_requested${index}" id="user_requested_lbl${index}"><g:message
                                    code="app.widget.reportRequest.requested"/></label>
                        </span>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="user_requestedGroup${index}" name="user_requestedGroup"/>
                            <label for="user_requestedGroup${index}" id="user_requestedGroup_lbl${index}"><g:message
                                    code="app.widget.reportRequest.requestedGroup"/></label>
                        </span>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="user_assigned${index}" name="user_assigned"/>
                            <label for="user_assigned${index}" id="user_assigned_lbl${index}"><g:message
                                    code="app.widget.reportRequest.assigned"/></label>
                        </span>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="user_assignedGroups${index}" name="user_assignedGroups"/>
                            <label for="user_assignedGroups${index}" id="user_assignedGroups_lbl${index}"><g:message
                                    code="app.widget.reportRequest.assignedGroups"/></label>
                        </span>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="user_all${index}" name="user_all"/>
                            <label for="user_all${index}"><g:message code="app.widget.reportRequest.all"/></label>
                        </span>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-12">
                        <b><g:message code="app.label.report.request.priority"/>:</b>
                        <span class="priorityDiv${index}" style="margin-left: 15px;">
                        </span>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="priority_all${index}" name="priority_all"/>
                            <label for="priority_all${index}"><g:message code="app.widget.reportRequest.all"/></label>
                        </span>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-12">
                        <b><g:message code="app.report.request.dueDate.label"/>:</b>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="due_overdue${index}" name="due_overdue"/>
                            <label for="due_overdue${index}" id="due_overdue_lbl${index}"><g:message
                                    code="app.widget.overdue"/></label>
                        </span>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="due_today${index}" name="due_today"/>
                            <label for="due_today${index}" id="due_today_lbl${index}"><g:message
                                    code="app.widget.dueToday"/></label>
                        </span>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="due_tomorrow${index}" name="due_tomorrow"/>
                            <label for="due_tomorrow${index}" id="due_tomorrow_lbl${index}"><g:message
                                    code="app.widget.dueTomorrow"/></label>
                        </span>
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="due_five${index}" name="due_five"/>
                            <label for="due_five${index}" id="due_five_lbl${index}"><g:message
                                    code="app.widget.due5"/></label>
                        </span>
                        <span class="checkbox checkbox-primary reportRequestWidget">
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
                        <span class="checkbox checkbox-primary reportRequestWidget">
                            <input type="checkbox" id="status_all${index}" name="status_all"/>
                            <label for="status_all${index}"><g:message code="app.widget.reportRequest.all"/></label>
                        </span>
                    </div>
                </div>
                <g:if test="${isEditable}">
                    <button class="btn btn-primary saveReportRequestWidget">
                        ${message(code: "default.button.save.label")}
                    </button>
                </g:if>
                <button class="btn btn-primary reportRequestWidgetHideButton${index}">
                    ${message(code: "app.label.hideOptions")}
                </button>

            </span>
        </div>
        <table id="rxTableReportRequests${index}" class="table table-striped pv-list-table dataTable no-footer"
               width="100%">
            <thead>
            <tr>
                <th class="col-min-65"><g:message default="Request Id" code="app.label.report.request.id"/></th>
                <th><g:message default="Report Name" code="app.label.report.request.name"/></th>
                <th><g:message default="Assigned To" code="app.label.action.item.assigned.to"/></th>
                <th><g:message default="Description" code="app.label.action.item.description"/></th>
                <th><g:message default="Due Date" code="app.label.action.item.due.date"/></th>
                <th><g:message default="Priority" code="app.label.action.item.priority"/></th>
                <th><g:message default="Status" code="app.label.action.item.status"/></th>
                <th><g:message default="Request Date" code="app.widget.button.ReportRequestDate.label"/></th>
                <th><g:message default="Request Type" code="app.widget.button.ReportRequestType.label"/></th>
            </tr>
            </thead>
        </table>
    </div>
</div>
<input type="hidden" id="widgetSettings${index}" value="${widget.reportWidget.settings}"/>
<script>
    var workflowJustificationRRUrl = "${createLink(controller: 'workflowJustificationRest', action: 'reportRequest')}";
    var workflowJustificationConfirmRRUrl = "${createLink(controller: 'workflowJustificationRest', action: 'saveReportRequest')}";

    $(function () {

        var tableSortColumnNumber${index} = 0;
        var tableShowNumber${index} = 5;
        var tableSortColumnOrder${index} = 'asc';
        var reportReqestTable;

        function createRow(status, code) {
            return '<span class="checkbox checkbox-primary reportRequestWidget">' +
                '<input type="checkbox" id="' + code + '_' + status.id + '${index}" name="' + code + '_' + status.id + '" />' +
                '<label for="' + code + '_' + status.id + '${index}">' + status.title + ' (<span class="' + code + '_' + status.id + '_val">' + status.count + '</span>)' + '</label></span>';
        }

        function setCheckboxes() {
            var settingsString = $("#widgetSettings${index}").val();
            if (settingsString) {
                var settings = JSON.parse(settingsString);
                var $container = $(".reportRequestWidgetSearchForm${index}");
                if (settings.title) {
                    $container.find('input[name=rrTitle]').val(encodeToHTML(settings.title));
                    $('.reportRequestWidgetTitle${index} .rrTitleContent').html(encodeToHTML(settings.title));
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
                "url": advancedReportRequestUrl,
                "dataType": 'json'
            }).done(function (data) {
                var statuses = data.result.status;
                var statusesDiv = "";
                for (var i = 0; i < statuses.length; i++) {
                    statusesDiv += createRow(statuses[i], "status");
                }
                $(".statusDiv${index}").html(statusesDiv);

                var priority = data.result.priority;
                statusesDiv = "";
                for (var i = 0; i < priority.length; i++) {
                    statusesDiv += createRow(priority[i], "priority");
                }
                $(".priorityDiv${index}").html(statusesDiv);

                var user = data.result.user;
                appendToLabel($("#user_assigned_lbl${index}"), user.assigned);
                appendToLabel($("#user_assignedGroups_lbl${index}"), user.assignedGroup);
                appendToLabel($("#user_requested_lbl${index}"), user.requested);
                appendToLabel($("#user_requestedGroup_lbl${index}"), user.requestedGroup);
                appendToLabel($("#user_owner_lbl${index}"), user.owner);

                var due = data.result.due;
                appendToLabel($("#due_overdue_lbl${index}"), due.overdue);
                appendToLabel($("#due_today_lbl${index}"), due.today);
                appendToLabel($("#due_tomorrow_lbl${index}"), due.tomorrow);
                appendToLabel($("#due_five_lbl${index}"), due.five);

                setCheckboxes();
                var settings = getCurrentSettings();
                checkAllCheckbox(settings);

                reportReqestTable = initTable();

                $(".reportRequestWidget input").on('click', function () {
                    removeCheckboxIfAllClicked($(this));
                    var settings = getCurrentSettings();
                    checkAllCheckbox(settings);
                    $("#widgetSettings${index}").val(JSON.stringify(settings));
                    reportReqestTable.ajax.reload();
                });
            });
        };

        function removeCheckboxIfAllClicked($current) {
            var $container = $(".reportRequestWidgetSearchForm${index}");
            if ($current.attr("name") && $current.attr("name").indexOf("_all") > 0) {
                var type = $current.attr("name").split("_")[0];
                $container.find("input[name^=" + type + "]").each(function () {
                    if ($(this).attr("name") !== type + "_all")
                        $(this).prop('checked', false);
                })
            }
        }

        function checkAllCheckbox(settings) {
            var $container = $(".reportRequestWidgetSearchForm${index}");
            var types = ["user", "priority", "due", "status"];
            for (var i in types) {
                var type = types[i];
                if (!settings[type] || settings[type].length === 0) {
                    $container.find('input[name=' + type + "_all]").prop('checked', true);
                } else {
                    $container.find('input[name=' + type + "_all]").prop('checked', false);
                }
            }
        }

        $(".reportRequestWidgetTitle${index}").on('click', function () {
            $(".reportRequestWidgetSearchForm${index}").show();
            $(".reportRequestWidgetTitle${index}").hide();
            $(this).closest(".rx-widget-content").find(".dataTables_length").parent().show();
        });

        $(".reportRequestWidgetHideButton${index}").on('click', function () {
            $(".reportRequestWidgetSearchForm${index}").hide();
            $(".reportRequestWidgetTitle${index}").show();
        });


        $(".saveReportRequestWidget").on('click', function () {
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
                    $('.reportRequestWidgetTitle${index} .rrTitleContent').html(encodeToHTML(settings.title));
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
            $(".reportRequestWidgetSearchForm${index}").find("input").each(function (i) {
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
            var rrtable = $("#rxTableReportRequests${index}").DataTable({

                //"sPaginationType": "bootstrap",
                "layout": {
                    topStart: null,
                    topEnd: {search: {placeholder: 'Search'}},
                    bottomStart: ['pageLength', 'info', {
                        paging: {
                            type: 'full_numbers'
                        }
                    }],
                    bottomEnd: null,
                },
                language: { search: ''},
                "stateDuration": -1,
                "bAutoWidth": false,
                "stateSave": false,
                "customProcessing": true, //handled using processing.dt event
                "serverSide": true,
                "iDisplayLength": tableShowNumber${index},
                "bLengthChange": true,
                "aLengthMenu": [[5, 10, 15, 20], [5, 10, 15, 20]],
                "pagination": true,
                "pagingType": "full_numbers",

                drawCallback: function (settings) {
                    pageDictionary($('#rxTableReportRequests${index}_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                    hideLoader();
                },
                "ajax": {
                    "url": "${createLink(controller: 'reportRequestRest', action: 'widgetSearch')}",
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
                "aaSorting": [[tableSortColumnNumber${index}, tableSortColumnOrder${index}]],
                "searching": false,
                "tableName": "reportRequestTable",
                "aoColumns": [
                    {
                        "mData": "reportRequestId",
                        "mRender": function (data, type, row) {
                            return '<span id="reportRequestId">' + row.reportRequestId + '</span>';
                        }
                    },
                    {
                        "mData": "requestName",
                        "mRender": function (data, type, row) {
                            return '<a href="${g.createLink(controller: "reportRequest", action: "show")}?id=' + row.reportRequestId + '">' + encodeToHTML(data) + '</a>';
                        }
                    },
                    {
                        "mData": "assignedTo",
                        "bSortable": false,
                        "mRender": $.fn.dataTable.render.text()
                    },
                    {
                        "mData": "description",
                        "mRender": function (data, type, row) {
                            if (!_.isEmpty(data)) {
                                var val = (data.length > 50 ? (data.substring(0, 50) + "...") : data);
                                return '<span title="' + data + '">' + encodeToHTML(val) + '</span>';
                            } else {
                                return ""
                            }
                        }
                    },
                    {
                        "mData": "dueDate",
                        "aTargets": ["dueDate"],
                        "sClass": "dataTableColumnCenter",
                        "mRender": function (data, type, full) {
                            return data ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                        }
                    },
                    {
                        "mData": "priority"
                    },
                    {
                        "mData": "status",
                        "bSortable": false,
                        "mRender": function (data, type, row) {
                            return '<button class="btn btn-default btn-xs rrStatusBtn${index}" style="min-width: 100px" data-reportRequest-id= "' + row.reportRequestId + '" data-initial-state= "' + row.status + '" >' + row.status + '</button>';
                        }
                    },
                    {
                        "mData": "dateCreated",
                        "visible": false,
                        "aTargets": ["dateCreated"],
                        "sClass": "dataTableColumnCenter",
                        "mRender": function (data, type, full) {
                            return data ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                        }
                    },
                    {
                        "mData": "reportRequestType",
                        "visible": false
                    }
                ]
            });
            var Id = "#rxTableReportRequests${index}";
            loadTableOption(Id);
            return rrtable
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
                    params: [`rxTableReportRequests${index}`, workflowJustificationConfirmRRUrl]
                })
            );
            modal.find(".required-indicator").show();
            openStateHistoryModal(this, workflowJustificationRRUrl);
        });

        $('#refresh-widget${index}').hide();
        loadData();
    });
</script>