$(function () {
    // var isAdmin = ($('#isAdmin').val() === 'true');
    var tableFilter = {};
    var advancedFilter = false;
    var table = $('#rxTableCaseSerisExecutionStatus').DataTable({
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
        //"sPaginationType": "bootstrap",
        "stateSave": true,
        "stateDuration": -1,
        initComplete: function () {
            getReloader("rxTableCaseSerisExecutionStatus");
        },
        "customProcessing": true, //handled using processing.dt event
        "serverSide": true,
        "ajax": {
            "url": caseSeriesScheduledUrl,
            "dataSrc": "data",
            "data": function (d) {
                d.tableFilter = JSON.stringify(tableFilter);
                d.advancedFilter = advancedFilter;
                d.sharedwith = $('#sharedWithFilterControl').val();
                d.searchString = d.search.value;
                if (d.order.length > 0) {
                    d.direction = d.order[0].dir;
                    //Column header mData value extracting
                    d.sort = d.columns[d.order[0].column].data;
                }
                if ($('select[name="submissionFilter"]').length > 0) {
                    d.status = $('select[name="submissionFilter"]').val();
                }
            }
        },
        "aaSorting": [],
        //we would need to change in checkStatus function as well while changing default order
        "order": [[4, "desc"]],
        "bLengthChange": true,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "pagination": true,
        "iDisplayLength": 50,

        drawCallback: function (settings) {
            $('.reloaderBtn').removeClass('glyphicon-refresh-animate');
            pageDictionary($('#rxTableCaseSerisExecutionStatus_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            $('#rxTableCaseSerisExecutionStatus_wrapper').find(".dt-layout-row:first").addClass("searchToolbar top");
        },
        "aoColumns": [
            {
                "mData": "seriesName",
                "aTargets": ["seriesName"],
                "mRender": function (data, type, row) {
                    var link = ShowScheduledConfigUrl + '/' + row.id;
                    if (data) {
                        data = data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                    }
                    return '<a href=' + link + '>' + data + '</a>'
                }
            },
            {
                "mData": "version",
                "sClass": "dataTableColumnCenter"
            },
            {
                "mData": "executionStatus",
                "aTargets": ["executionStatus"],
                "sClass": "dataTableColumnCenter",
                "bSortable": false,
                "mRender": function (data, type, row) {
                    if (row.executionStatus == EXECUTION_STATUS_ENUM.BACKLOG) {
                        return '<i class="fa fa-clock-o fa-lg es-scheduled popoverMessage" data-content="' + $.i18n._('Scheduled') + '"></i> <span class="glyphicon glyphicon-stop popoverMessage" data-content="' + $.i18n._('app.executionStatus.unschedule.label') + '" data-id="' + row.exeutionStId + '" data-actionUrl="' + removeFromBacklogUrl + '"></span>'
                    } else if (row.executionStatus == EXECUTION_STATUS_ENUM.SCHEDULED) {
                        return '<i class="fa fa-clock-o fa-lg es-scheduled popoverMessage" data-content="' + $.i18n._('Scheduled') + '"></i> <span class="glyphicon glyphicon-stop popoverMessage" data-content="' + $.i18n._('app.executionStatus.unschedule.label') + '" data-id="' + row.id + '" data-actionUrl="' + unscheduleUrl + '"></span>'
                    } else if (row.executionStatus == EXECUTION_STATUS_ENUM.GENERATING) {
                        return '<div><i class="fa fa-spinner fa-spin fa-lg es-generating popoverMessage" data-content="' + $.i18n._('Generating') + '"></i><span class="glyphicon glyphicon-stop" data-id="' + row.exeutionStId + '" data-actionUrl="' + killExecutionUrl + '"></span></div>'
                    } else if (row.executionStatus == EXECUTION_STATUS_ENUM.DELIVERING) {
                        return '<i class="fa fa-spinner fa-spin fa-lg es-delivering popoverMessage" data-content="' + $.i18n._('Delivering') + '"/>'
                    } else if (row.executionStatus == EXECUTION_STATUS_ENUM.COMPLETED) {
                        return '<i class="fa fa-check-circle-o fa-lg es-completed popoverMessage" data-content="' + $.i18n._('Completed') + '"/>'
                    } else {
                        if (row.executionStatus == EXECUTION_STATUS_ENUM.ERROR || row.executionStatus == EXECUTION_STATUS_ENUM.WARN) {
                            var errorTime = moment.utc(row.dateCreated).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                            var message = 'Occurred on: ' + errorTime;
                            message += '<br> Entity ID: ' + row.id + ' (Error : ' + row.errorTitle + ')';
                            var details;
                            details = row.executionStatus + ' Details: ' + row.errorMessage;

                            if (row.executionStatus == EXECUTION_STATUS_ENUM.ERROR) {
                                return '<div> <i class="fa fa-exclamation-circle fa-lg es-error popoverMessage" title="' + $.i18n._('Error') + '" data-content="' + message + ' ">' +
                                    '<a class="errorDetail" href="' + executionErrorUrl + '?id=' + row.exeutionStId + '">' + ' View Details </a></div>'
                            } else if (row.executionStatus == EXECUTION_STATUS_ENUM.WARN) {
                                return '<div> <i class="fa fa-exclamation-circle fa-lg es-warn popoverMessage" title="' + $.i18n._('Error') + '" data-content="' + message + ' ">' +
                                    '<a class="errorDetail" href="' + executionErrorUrl + '?id=' + row.exeutionStId + '">' + ' View Details </a></div>'
                            }
                        }
                    }

                    return data
                }
            },
            {"mData": "owner"},
            {
                "mData": "runDate",
                "aTargets": ["runDate"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, row) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {
                "mData": "frequency",
                "bSortable": false,
                "sClass": "dataTableColumnCenter",
                "mRender": function (data, type, row) {
                    return $.i18n._('app.frequency.' + data);
                }
            },
            {
                "mData": "sharedWith",
                "bSortable": false,
                "aTargets": ["sharedWith"],
                "sClass": "dataTableColumnCenter",
                "mRender": function (data, type, row) {
                    var users = "";
                    var total = 0;
                    if (data) {
                        total = data.length >= 0 ? data.length : 0;
                        users = data.join(", ")
                    }
                    return '<a href="#" class="popoverMessage" title="" data-content="' + users + '">' + total + $.i18n._('users') + '</a>'
                }
            },
            {
                "mData": "deliveryMedia",
                "bSortable": false
            }
        ]

    }).on('draw.dt', function () {
        setTimeout(function () {
            $('#rxTableCaseSerisExecutionStatus tbody tr').each(function () {
                $(this).find('td:eq(4)').attr('nowrap', 'nowrap');
            });
        }, 100)
    }).on('xhr.dt', function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
    });

    $('#rxTableCaseSerisExecutionStatus').on('mouseover', 'tr', function () {
        $('.popoverMessage').popover({
            placement: 'right',
            trigger: 'hover focus',
            viewport: '#rxTableReportsExecutionStatus',
            html: true
        });
    });

    $('#rxTableCaseSerisExecutionStatus').on('click', '.glyphicon-stop', function () {
        if (confirm($.i18n._('cancel.execution'))) {
            $.ajax({
                url: $(this).attr('data-actionUrl'),
                data: {id: $(this).data('id')},
                dataType: 'json'
            })
                .done(function (result) {
                    if (result.success) {
                        setTimeout(function () {
                            $("#rxTableCaseSerisExecutionStatus").DataTable().draw();
                        }, 1500)
                    }
                })
                .fail(function (err) {
                    var responseText = err.responseText;
                    var responseTextObj = JSON.parse(responseText);
                    alert(responseTextObj.message);
                });
        }
    });

    actionButton('#rxTableCaseSerisExecutionStatus');
    $('.outside').hide();

    /*var init_filter = function () {
        var filter_data = [
            {
                label: $.i18n._("app.advancedFilter.reportName"),
                type: 'text',
                name: 'reportName'
            },
            {
                label: $.i18n._("app.advancedFilter.version"),
                type: 'number',
                name: 'reportVersion'
            },
            {
                label: $.i18n._("app.advancedFilter.owner"),
                type: 'id',
                name: 'owner'
            },
            {
                label: $.i18n._("app.advancedFilter.RunDateStart"),
                type: 'date-range',
                group: 'nextRunDate',
                group_order: 1
            },
            {
                label: $.i18n._("app.advancedFilter.RunDateEnd"),
                type: 'date-range',
                group: 'nextRunDate',
                group_order: 2
            },
            {
                label: $.i18n._("app.advancedFilter.runDurationFrom"),
                type: 'number-range',
                group: 'runDuration',
                group_order: 1
            },
            {
                label: $.i18n._("app.advancedFilter.runDurationTo"),
                type: 'number-range',
                group: 'runDuration',
                group_order: 2
            },
            {
                label: $.i18n._("app.advancedFilter.frequency"),
                type: 'select2-enum',
                name: 'frequency',
                data_type: 'FrequencyEnum',
                data: frequencyType
            },
            {
                label: $.i18n._("app.advancedFilter.periodicReportType"),
                type: 'text',
                name: 'periodicReportType'
            }

        ];

        pvr.filter_util.construct_right_filter_panel({
            table_id: '#rxTableReportsExecutionStatus',
            container_id: 'config-filter-panel',
            filter_defs: filter_data,
            column_count: 1,
            done_func: function (filter) {
                tableFilter = filter;
                advancedFilter = true;
                var dataTable = $('#rxTableReportsExecutionStatus').DataTable();
                dataTable.ajax.reload(function (data) {
                }, false).draw();
            }
        });
        bindSelect2WithUrl($("select[data-name=owner]"), ownerListUrl, ownerValuesUrl, true);
    };*/

    // init_filter();
});


function checkStatus(elem) {
    if (elem.value == EXECUTION_STATUS_DROP_DOWN_ENUM.SCHEDULED) {
        $('input[data-name="runDuration"]').attr("disabled", "disabled");
        $('input[data-name="reportVersion"]').attr("disabled", "disabled");
        $('input[data-name="periodicReportType"]').attr("disabled", "disabled");
        $('select[data-name="frequency"]').attr("disabled", "disabled");
    } else {
        $('input[data-name="runDuration"]').removeAttr("disabled");
        $('input[data-name="reportVersion"]').removeAttr("disabled");
        $('input[data-name="periodicReportType"]').removeAttr("disabled");
        $('select[data-name="frequency"]').removeAttr("disabled");
    }

    $('#rxTableCaseSerisExecutionStatus').DataTable().order([[5, 'desc']]).draw();
}

function getReloader(tableName) {
    var searchDiv = $('#' + tableName + '_filter').parent();
    var reloader = '<span title="Refresh" class="glyphicon reloaderBtn glyphicon-refresh"></span>';
    searchDiv.append($(reloader));
    if (tableName != undefined) {
        $('.reloaderBtn').on('click', function () {
            $('.reloaderBtn').addClass('glyphicon-refresh-animate');
            $('#' + tableName).DataTable().draw();
        });
    }
}