<%@ page import="com.rxlogix.enums.ReportFormatEnum" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
            <g:link controller="report" title="${message(code: 'default.button.addLastReportsWidget.label')}" action="index"
            class="rxmain-container-header-label rx-widget-title m-0">${message(code: 'default.button.addLastReportsWidget.label')}</g:link>
            <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div id="container${index}" class="row rx-widget-content pv-caselist panel">
        <table id="rxTableLastReports${index}" class="table table-striped pv-list-table dataTable no-footer" width="100%" >
            <thead>
                <tr>
                    <th style="min-width: 80px;height: 21px;"><g:message code="app.label.reportType"/></th>
                    <th style="min-width: 100px;height: 21px;" class="reportNameColumn"><g:message code="app.label.reportName"/></th>
                    <th style="width: 80px;height: 21px;" align="center"><g:message code="app.label.version"/></th>
                    <th style="min-width: 100px;height: 21px;" align="center" class="reportDescriptionColumn"><g:message code="app.label.description"/></th>
                    <th style="width: 150px;height: 21px;" align="center"><g:message code="app.label.generatedOn"/></th>
                    <th style="width: 100px;height: 21px;" align="center"><g:message code="app.periodicReport.executed.workflowState.label"/></th>
                    <th style="width: 100px;height: 21px;" align="center"><g:message code="app.label.action.item.table.column"/></th>
                </tr>
            </thead>
        </table>
    </div>

    <script>
        ACTION_ITEM_GROUP_STATE_ENUM = {
            WAITING:'WAITING',
            OVERDUE:'OVERDUE',
            CLOSED:'CLOSED'
        };
        var refresh${index};

        $(function () {
            refresh${index} = function(){
                loadData(refreshData);
            };
            var refreshData = function (dataSet) {
                var datatable = $('#rxTableLastReports${index}').dataTable().api();
                datatable.clear();
                datatable.rows.add(dataSet);
                datatable.draw();
            };

            var loadData = function (processData) {
                var adhocRequest = $.ajax({
                    "url": indexReportUrl,
                    "data": { length: 4, start: 0, pvp: module == 'pvp' },
                    "dataType": 'json'
                });
                var aggregateRequest = $.ajax({
                    "url": periodicReportUrl,
                    "data": { pvp: module == 'pvp', length: 4, start: 0 },
                    "dataType": 'json'
                });

                $.when(adhocRequest, aggregateRequest).done(function (adhocResponse, aggregateResponse) {
                    var dataSet = adhocResponse[0]['aaData'];
                    for (var i = 0; i < dataSet.length; i++) {
                        dataSet[i].configurationType = "adhoc";
                    }

                    var aggregateData = aggregateResponse[0]['aaData'];
                    for (var i = 0; i < aggregateData.length; i++) {
                        aggregateData[i]['configurationType'] = "aggregate";
                        aggregateData[i]['owner'] = aggregateData[i]['user'];
                        aggregateData[i]['numOfExecutions'] = aggregateData[i]['version'];
                    }

                    dataSet = dataSet.concat(aggregateData);
                    dataSet = dataSet.sort(function (a, b) {
                        return moment.utc(b.dateCreated).diff(moment.utc(a.dateCreated));
                    });
                    dataSet = dataSet.slice(0, 4);
                    processData(dataSet);
                }).fail(function (jqXHR, textStatus, errorThrown) {
                    console.error("Request Failed!", textStatus, errorThrown);
                }).always(function () {
                    hideLoader();
                });
            };
            var initTable = function (dataSet) {
                $('#rxTableLastReports${index}').DataTable({
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
                    fnInitComplete: function () {
                        $($("#rxTableLastReports${index}_wrapper")).find(">:first-child").css({"display": "none"});
                    },
                    language: { search: ''},
                    "customProcessing": true, //handled using processing.dt event
                    "serverSide": false,
                    "searching": false,
                    "paging": false,
                    "info": false,
                    "data": dataSet,
                    "beforeSend" : showDataTableLoader($('#rxTableLastReports${index}')),
                    "success" : hideDataTableLoader($('#rxTableLastReports${index}')),
                    "order": [[4, "desc"]],
                    "columns": [
                        {
                            "data": "configurationType",
                            "bSortable": false,
                            "render": function (data, type, row) {
                                return $.i18n._(data);
                            }
                        }, {
                            "data": "reportName",
                            "render": function (data, type, row) {
                                var link = "" + ((module == 'pvp') ? showPvpReportUrl : showReportUrl) + '/' + row.id;
                                data = encodeToHTML(data);
                                return '<a href=' + link + '>' + data + '</a>'
                            }
                        },
                        {
                            "data": "numOfExecutions",
                            "className": "dataTableColumnCenter"
                        },
                        {
                            "data": "description",
                            "bSortable": false,
                            "render": function (data, type, row) {
                                return (data == null) ? data : encodeToHTML(data);
                            }
                        },
                        {
                            "data": "dateCreated",
                            "className": "dataTableColumnCenter forceLineWrapDate",
                            "render": function (data, type, full) {
                                var dateCreated = Date.parse(data);
                                return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT)
                            }
                        },
                        {
                            "mData": "state",
                            "bSortable": false,
                            "className": "dataTableColumnCenter",
                            "mRender": function (data, type, row) {
                                return '<button class="btn btn-default btn-xs" style="min-width: 100px" data-executed-type="'+row.configurationType+'" data-executed-config-id= "'+row.id+'" data-initial-state= "'+row.state+'" data-evt-clk=\'{\"method\": \"prepareAndOpenStateHistoryModal\", \"params\": []}\'>'+row.state+'</button>\
                                    <div style="display:none">\
                                    <a class="markAsSubmitted_'+ row.id+'" data-toggle="modal"  data-id="'+row.id+'"  data-target="#reportSubmissionModal" data-url="' + markAsSubmittedUrl + "?id=" + row.id + '">sub</a>\
                                    <a class="sendToDms_'+ row.id+'" role="menuitem" id="'+row.id+'" data-toggle="modal" data-id="'+row.id+'" data-target="#sendToDmsModal">dms</a>\
                                    </div>';
                            }
                        },
                        {
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
                                    creationAction = '<button class="'+clazz+' actionItemModalIcon" data-exconfig-type="'+(row.configurationType=='adhoc'?ADHOC_REPORT:PERIODIC_REPORT)+'" data-exconfig-id="'+ row.id+'" style="width:70px;">'+$.i18n._('app.actionItemGroupState.'+row.actionItemStatus) +'</button>';
                                }else {
                                    <sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
                                        creationAction='<a href="#" role="menuitem" class="btn btn-default btn-xs listMenuOptions createActionItem" data-exconfig-type="'+(row.configurationType=='adhoc'?ADHOC_REPORT:PERIODIC_REPORT)+'" data-exconfig-id="'+ row.id+'" style="width:65px;">' + $.i18n._("workFlowState.reportActionType.CREATE_ACTION_ITEM") + '</a>';
                                    </sec:ifAnyGranted>
                                }
                                return creationAction;
                            }
                        }
                    ]
                }).on('draw.dt', function () {
                    setTimeout(function () {
                        $('#rxTableLastReports${index} tbody tr').each(function () {
                            $(this).find('td:eq(4)').attr('nowrap', 'nowrap');
                        });
                    }, 100);
                    initEvtClk();
                }).on('xhr.dt', function (e, settings, json, xhr) {
                    checkIfSessionTimeOutThenReload(e, json);
                });
            };

            let initEvtClk = function () {
                $(`#rxTableLastReports${index}`).on('click', '[data-evt-clk]', function() {
                    const eventData = JSON.parse($(this).attr("data-evt-clk"));
                    const methodName = eventData.method;
                    const params = eventData.params;

                    if(methodName == 'prepareAndOpenStateHistoryModal') {
                        var elem = $(this);
                        prepareAndOpenStateHistoryModal(elem);
                    }
                });
            }

            loadData(initTable);

            $('#refresh-widget${index}').hide();

            $("#rxTableLastReports${index}").on("click", ".createActionItem", function () {
                actionItem.actionItemModal.set_executed_report_id($(this).data('exconfigId'));
                actionItem.actionItemModal.init_action_item_modal(false,$(this).data('exconfigType'));
            });

            $("#rxTableLastReports${index}").on("click ", ".actionItemModalIcon", function() {
                actionItem.actionItemModal.set_executed_report_id($(this).data('exconfigId'));
                actionItem.actionItemModal.view_action_item_list(hasAccessOnActionItem,false,$(this).data('exconfigType'));
            });
        });

        function prepareAndOpenStateHistoryModal(btn) {
            var statusObject = $(btn);
            var modal = $("#workflowStatusJustification");
            var execType = statusObject.attr("data-executed-type");
            reloadData = function (rowId, resetPagination, tableId, response) {
                doAction(rowId, tableId, response.action);
                refresh${index}();
            };
            doAction = function (rowId, tableId, actionClassName) {
                if (actionClassName) {
                    var dataTable = $("#" + tableId).find("." + actionClassName + "_" + rowId).trigger('click');
                }
            };
            modal.find(".confirm-workflow-justification").attr("data-evt-clk", '{"method": "confirmJustification", "params": ["rxTableLastReports${index}"]}');
            if (execType === "aggregate") {
                modal.find(".required-indicator").show();
            } else {
                modal.find(".required-indicator").hide();
            }
            openStateHistoryModal(btn);
        }
    </script>
</div>

<g:form controller="report" data-evt-sbt='{"method": "submitForm", "params": []}'>
    <g:hiddenField name="executedConfigId"/>
    <g:render template="/report/includes/sendToDmsModal"/>
</g:form>
<g:render template="/includes/widgets/reportSubmission"/>