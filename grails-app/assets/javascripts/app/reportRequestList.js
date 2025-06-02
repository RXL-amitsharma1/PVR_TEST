var reportRequest = reportRequest || {}

reportRequest.reportRequestList = (function () {
    var tableFilter = {};
    var advancedFilter = false;
    var sessionStorageAssignedToVariableName = window.location.pathname.replace(/\//g, "") + ".assignedTo";

    var report_request_table;
    var initAssignedToFilter = function () {
        var sharedWith = $('#assignedToFilterControl');
        var topControls = $(".topControls");
        $('#reportRequestList_wrapper').find('.dt-search').before(topControls);
        topControls.show();
        bindShareWith(sharedWith, sharedWithListUrl, sharedWithValuesUrl, '250px', true).on("change", function (e) {
            sessionStorage.setItem(sessionStorageAssignedToVariableName, $('#assignedToFilterControl').val());
            report_request_table.draw();
        });
        $("#statusFilter").select2({placeholder: $.i18n._("selectOne"), allowClear: true}).on("change", function () {
            var dataTable = $('#reportRequestList').DataTable();
            dataTable.ajax.reload(function (data) {
            }, false).draw();
        });
    };

    var init_report_request_table = function () {
        $('#assignedToFilterControl').val(sessionStorage.getItem(sessionStorageAssignedToVariableName) ? sessionStorage.getItem(sessionStorageAssignedToVariableName) : "");

        //Initialize the datatable
        report_request_table = $("#reportRequestList").DataTable({
            "layout": {
                topStart: null,
                topEnd: {search: {placeholder: $.i18n._("fieldprofile.search.label")}},
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
            initComplete: function() {

                initAssignedToFilter();
                //Toggle the action buttons on the action item list.
                actionButton('#reportRequestList');
            },
            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,
            "ajax": {
                "url": reportRequestListUrl,
                "dataSrc": "data",
                "type": 'POST',
                "data": function (d) {
                    d.searchString = d.search.value;
                    if ($("#statusFilter").val() != "") {
                        if (!tableFilter || tableFilter == "") tableFilter = {};
                        tableFilter.workflowState = {"type": "id", "name": "workflowState", "value": $("#statusFilter").val()};
                        advancedFilter = true;
                    } else {
                        delete tableFilter.workflowState;
                    }
                    d.tableFilter = JSON.stringify(tableFilter);
                    d.advancedFilter = advancedFilter;
                    d.sharedwith = $('#assignedToFilterControl').val();
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        //Column header mData value extracting
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },

            "aaSorting": [[4, "desc"]],
            "bLengthChange": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "pagination": true,
            "iDisplayLength": 10,

            drawCallback: function (settings) {
                pageDictionary($('#reportRequestList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                updateTitleForThreeRowDotElements();
            },
            "aoColumns": [
                {
                    "mData": "reportRequestId",
                    "mRender" : function(data, type, row) {
                        return '<span id="reportRequestId">'+row.reportRequestId+'</span>';
                    }
                },
                {
                    "mData": "requestName",
                    mRender: function (data, type, row) {
                        return "<div class='three-row-dot-overflow'>" + encodeToHTML(data) + "</div>";
                    }
                },
                {
                    "mData": "assignedTo",
                    "mRender": $.fn.dataTable.render.text()
                },
                {
                    "mData" : "description",
                    "mRender": function (data, type, row) {
                        if(!_.isEmpty(data)) {
                            var val = (data.length > 50 ? (data.substring(0, 50) + "...") : data);
                            return '<span title="' + replaceBracketsAndQuotes(data) + '">' + replaceBracketsAndQuotes(val) + '</span>';
                        }
                        return ""
                    }
                },
                {
                    "mData": "dueDate",
                    "aTargets": ["dueDate"],
                    "sClass": "dataTableColumnCenter mw-100",
                    "mRender": function (data, type, full) {
                        return data ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                    }
                },
                {
                    "mData" : "priority",
                    "mRender": function (data, type, row) {
                        return '<span>' + encodeToHTML(row.priority) + '</span>';
                    }
                },
                {
                    "mData": "status",
                    "bSortable": true,
                    "mRender": function (data, type, row) {
                        return '<button class="btn btn-default btn-xs btn-table-col-state" data-reportRequest-id= "' + row.reportRequestId + '" data-initial-state= "' + row.status + '" data-evt-clk=\'{\"method\": \"openStateHistoryModal\", \"params\": []}\' title="' + row.status + '">' + row.status + '</button>';
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
                    "mData" : "reportRequestType",
                    "visible": false,
                    "mRender": $.fn.dataTable.render.text()
                },
                {
                    "sClass": "mw-100",
                    "mData": null,
                    "bSortable": false,
                    "mRender" : function(data, type, row) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id='+row.reportRequestId+'" data-value="'+row.reportRequestId+'">' +$.i18n._('view')+'</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style=" font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id=' + row.reportRequestId + '" data-value="' + row.reportRequestId + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="copy?id=' + row.reportRequestId + '">' + $.i18n._('copy') + '</a></li> ';
                                if(row.aggregate){
                                    actionButton+= '<li role="presentation"><a class="work-flow-edit" role="menuitem" href="copyNext?id=' + row.reportRequestId + '">Create for next period</a></li> \
                                        <li role="presentation"><a class="work-flow-edit" role="menuitem" href="createReport?id=' + row.reportRequestId + '&configurationType=PERIODIC_REPORT">' + $.i18n._('reportRequest.aggregate.label') + '</a></li> '
                                }else{
                                    actionButton+='<li role="presentation"><a class="work-flow-edit" role="menuitem" href="createReport?id=' + row.reportRequestId + '&configurationType=ADHOC_REPORT">' + $.i18n._('reportRequest.adhoc.label') + '</a></li> ' ;
                                }
                                actionButton+='<li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('reportRequest') + '" data-instanceid="' + row.reportRequestId + '" data-instancename="' + replaceBracketsAndQuotes(row.requestName) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        });
        var init_import_export = function () {
            $('#file_input').on('change', function (evt, numFiles, label) {
                $(".btn").attr('disabled', 'disabled');
                $("#file_name").val($('#file_input').get(0).files[0].name);
                if ($("#file_name").val() !== "") {
                    $("#excelImportForm").trigger('submit');
                }
            });

            $(document).on("click", ".export", function () {
                $(this).attr('disabled', 'disabled');
                $("#searchString_ex").val($(".dataTables_filter").find("input").val());
                if ($("#statusFilter_ex").val() != "") {
                    if (!tableFilter || tableFilter == "") tableFilter = {};
                    tableFilter.workflowState = {
                        "type": "id",
                        "name": "workflowState",
                        "value": $("#statusFilter").val()
                    };
                    advancedFilter = true;
                } else {
                    delete tableFilter.workflowState;
                }
                $("#tableFilter_ex").val(JSON.stringify(tableFilter));
                $("#advancedFilter_ex").val(advancedFilter);
                $("#sharedwith_ex").val($('#assignedToFilterControl').val());
                $("#excelExportForm").trigger('submit');
            });
        };
        init_import_export();
        loadTableOption('#reportRequestList');
        init_table_filter();
        return report_request_table;
    };
    var init_table_filter = function () {
        var filter_data = [
            {
                label: $.i18n._("app.advancedFilter.requestId"),
                type: 'natural-number',
                name: 'id'
            },
            {
                label: $.i18n._("app.advancedFilter.requestSummary"),
                type: 'text',
                name: 'reportName',
                maxlength: 255
            },
            {
                label: $.i18n._("app.advancedFilter.description"),
                type: 'text',
                name: 'description',
                maxlength: 4000
            },
            {
                label: $.i18n._("app.advancedFilter.dueDateStart"),
                type: 'date-range',
                group: 'dueDate',
                group_order: 1
            },
            {
                label: $.i18n._("app.advancedFilter.dueDateEnd"),
                type: 'date-range',
                group: 'dueDate',
                group_order: 2
            },
            {
                label: $.i18n._("app.advancedFilter.priority"),
                type: 'select2-id',
                name: 'priority',
                ajax: {
                    url: '/reports/reportRequestType/listPriority',
                    data_handler: function (data) {
                        return pvr.filter_util.build_options(data, 'id', 'name', true);
                    },
                    error_handler: function (data) {
                        console.log(data);
                    }
                }
            }
        ];

        pvr.filter_util.construct_right_filter_panel({
            table_id: '#reportRequestList',
            container_id: 'config-filter-panel',
            filter_defs: filter_data,
            column_count: 1,
            done_func: function (filter) {
                tableFilter = filter;
                advancedFilter = true;
                var dataTable = $('#reportRequestList').DataTable();
                dataTable.ajax.reload(function (data) {
                }, false).draw();
            }
        });
    };

    return {
        init_report_request_table : init_report_request_table
    }

})();