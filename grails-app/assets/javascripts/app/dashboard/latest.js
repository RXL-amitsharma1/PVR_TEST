ACTION_ITEM_GROUP_STATE_ENUM = {
    WAITING: 'WAITING',
    OVERDUE: 'OVERDUE',
    CLOSED: 'CLOSED'
};
var refresh;
$(function () {
    refresh = function () {
        loadData(refreshData);
    };
    var refreshData = function (dataSet) {
        var datatable = $('#rxTableLastReports').dataTable().api();
        datatable.clear();
        datatable.rows.add(dataSet);
        datatable.draw();
    };

    var loadData = function (processData) {
        $.ajax({
            "url": indexReportUrl,
            "data": {
                length: rowNum,
                start: 0
            }
        }).done(function (adhocData) {
            var dataSet = adhocData['aaData'];
            for (var i = 0; i < dataSet.length; i++) {
                dataSet[i].configurationType = "adhoc";
            }

            $.ajax({
                "url": periodicReportUrl,
                "data": {
                    length: rowNum,
                    start: 0
                }
            }).done(function (aggregateData) {
                for (var i = 0; i < aggregateData['aaData'].length; i++) {
                    aggregateData['aaData'][i]['configurationType'] = "aggregate";
                    aggregateData['aaData'][i]['owner'] = aggregateData['aaData'][i]['user'];
                    aggregateData['aaData'][i]['numOfExecutions'] = aggregateData['aaData'][i]['version'];
                }
                dataSet = dataSet.concat(aggregateData['aaData']);
                dataSet = dataSet.sort(function (a, b) {
                    return moment.utc(b.dateCreated).diff(moment.utc(a.dateCreated))
                });
                dataSet = dataSet.slice(0, rowNum);

            }).fail(function () {
                console.log("User has no permissions to view aggregate reports!");
            }).always(function () {
                processData(dataSet);
                hideLoader();
            });
        });
    };
    var initTable = function (dataSet) {
        $('#rxTableLastReports').DataTable({
            "customProcessing": true, //handled using processing.dt event
            "serverSide": false,
            "searching": false,
            "paging": false,
            "info": false,
            "data": dataSet,
            "order": [[4, "desc"]],
            "columnDefs": [

                {"width": '50', "targets": 0},
                {"width": '100%', "targets": 1},
                {"width": '20', "targets": 2},
                {"width": '100%', "targets": 3},
                {"width": '100', "targets": 4},
                {"width": '80', "targets": 5},
                {"width": '45', "targets": 6}
            ],
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
                        var link = showReportUrl + '/' + row.id;
                        data = data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
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
                        return (data == null) ? data : data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                    }
                },
                {
                    "data": "dateCreated",
                    "className": "dataTableColumnCenter",
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
                        var actionButton = '<div class="btn-group dropdown status-dropdown-menu" align="center"> \
                                        <button  title="' + row.state + '" id=justificationState' + row.id + ' value=' + row.state + ' type="button" style="width:80px;overflow: hidden; margin-left: 5px;" class="btn pv-btn-grey btn-xs selected-status" data-toggle="dropdown" data-evt-clk=\'{\"method\": \"loadStates\", \"params\":[]}\'> \
                                        <span class="initialState" id=' + row.id + ' data-executedType="' + row.configurationType + '" data-executedConfigId="' + row.id + '">' + row.state + '</span> \
                                        </button> \
                                        <ul id="statusOptions" class="dropdown-menu dropdown-menu-right status" role="menu" style="min-width: 80px !important; font-size: 12px;"></ul> \
                                        </div>';
                        return actionButton;
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
                            creationAction = '<button class="' + clazz + ' actionItemModalIcon" data-exconfig-type="' + (row.configurationType == 'adhoc' ? ADHOC_REPORT : PERIODIC_REPORT) + '" data-exconfig-id="' + row.id + '" style="width:70px;">' + $.i18n._('app.actionItemGroupState.' + row.actionItemStatus) + '</button>';
                        } else {
                            creationAction = '<a href="#" role="menuitem" class="btn pv-btn-grey btn-xs listMenuOptions createActionItem" data-exconfig-type="' + (row.configurationType == 'adhoc' ? ADHOC_REPORT : PERIODIC_REPORT) + '" data-exconfig-id="' + row.id + '" style="width:65px;">' + $.i18n._("workFlowState.reportActionType.CREATE_ACTION_ITEM") + '</a>';
                        }
                        return creationAction;
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#rxTableLastReports tbody tr').each(function () {
                    $(this).find('td:eq(4)').attr('nowrap', 'nowrap');
                });
            }, 100)
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json);
        });
    };

    loadData(initTable);

    $('#refresh-widget').hide();

    $("#rxTableLastReports").on("click", ".createActionItem", function () {
        actionItem.actionItemModal.set_executed_report_id($(this).data('exconfig-id'));
        actionItem.actionItemModal.init_action_item_modal(false, $(this).data('exconfig-type'));
    });

    $("#rxTableLastReports").on("click ", ".actionItemModalIcon", function () {
        actionItem.actionItemModal.set_executed_report_id($(this).data('exconfig-id'));
        actionItem.actionItemModal.view_action_item_list(hasAccessOnActionItem, false, $(this).data('exconfig-type'));
    });


});

function loadStates(ele) {
    var id = ele.id;
    var initialStateObj = $('#' + id).find('.initialState');
    var execId = initialStateObj.attr('data-executedConfigId');
    var execType = initialStateObj.attr('data-executedType');
    var dataRow = $(this).parents('tr');
    var initialState = initialStateObj.html();
    var modal = $("#workflowStatusJustification");
    var params = {initialState: initialState, executedReportConfiguration: execId};
    reloadData = refresh;
    $.ajax({
        url: targetStatesAndApplicationsUrl,
        data: params,
        dataType: 'json'
    })
        .done(function (data) {
            modal.find(".confirm-paste").attr("data-evt-clk", '{"method": "confirmJustification", "params": ["rxTableLastReports"]}');
            if (execType == "aggregate")
                modal.find(".required-indicator").show();
            else
                modal.find(".required-indicator").hide();
            fillUpStatus(data, params, $('.status-dropdown-menu').find(".dropdown-menu"));
        })
        .fail(function (err) {
            console.log(err);
        });
}