var reportRequest = reportRequest || {}

reportRequest.reportRequestList = (function () {
    var tableFilter = {};
    var advancedFilter = false;
    var sessionStorageAssignedToVariableName = window.location.pathname.replace(/\//g, "") + ".assignedTo";

    var report_request_table;
    var initAssignedToFilter = function () {
        var topControls = $(".topControls");
        $('#reportRequestList_wrapper').find('.dt-search').before(topControls);
        topControls.show();
        var sharedWith = $('#assignedToFilterControl');

        bindShareWith(sharedWith, sharedWithListUrl, sharedWithValuesUrl, '250px', true).on("change", function (e) {
            sessionStorage.setItem(sessionStorageAssignedToVariableName, $('#assignedToFilterControl').val());
            report_request_table.draw();
        });
        $("#statusFilter").select2({
            placeholder: $.i18n._("app.advancedFilter.state"),
            allowClear: true
        }).on("change", function () {
            var dataTable = $('#reportRequestList').DataTable();
            dataTable.ajax.reload(function (data) {
            }, false).draw();
        });
    };

    var init_report_request_table = function () {
        var aoColumns = [
            {
                title: '',
                target: 0,
                className: 'treegrid-control',
                data: function (item) {
                    if (item.children) {
                        return '<span class="fa fa-caret-right" style="font-size:20px;margin-left: 10px !important; cursor: pointer"><\/span>';
                    }
                    return '';
                }
            },
            {
                "bSortable": false,
                "mData": "reportRequestId",
                "mRender": function (data, type, row) {
                    var icon = (row.parentReportRequest) ? "<span class='fa fa-unlink unlink' style='cursor: pointer'></span> " : "";
                    return icon + '<span id="reportRequestId">' + row.reportRequestId + '</span>';
                }
            },
            {
                "visible": false,
                "mData": "requestName",
                mRender: function (data, type, row) {
                    return encodeToHTML(data);
                }
            },
            {
                "mData": "inn",
                "sClass": "dataTableColumnCenter"
            },
            {
                "mData": "productSelection",
                "bSortable": false,
                "sClass": "dataTableColumnCenter"
            },
            {
                "mData": "recipient",
                "sClass": "dataTableColumnCenter",
                "mRender": function (data, type, row) {
                    return encodeToHTML(data);
                }
            },
            {
                "mData": "psrTypeFile",
                "sClass": "dataTableColumnCenter"
            },
            {
                "mData": "masterPlanningRequest",
                "sClass": "dataTableColumnCenter",
                "bSortable": false,
                "mRender": function (data, type, row) {
                    var val = '<span class="masterPlanningRequest" style="display: none">' + encodeToHTML(data) + '</span>';
                    return val + (data ? $.i18n._('yes.abbreviated') : $.i18n._('no.abbreviated'));
                }
            },
            {
                "mData": "reportingPeriodStart",
                "sClass": "dataTableColumnCenter",
                "bSortable": false,
                "mRender": function (data, type, full) {
                    return data ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                }
            },
            {
                "mData": "reportingPeriodEnd",
                "sClass": "dataTableColumnCenter",
                "bSortable": false,
                "mRender": function (data, type, full) {
                    return data ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                }
            },
            {
                "mData": "dueDateToHa",
                "sClass": "dataTableColumnCenter",
                "bSortable": false,
                "mRender": function (data, type, full) {
                    return data ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                }
            }
        ];
        aoColumns = aoColumns.concat(customFields);
        aoColumns = aoColumns.concat([
            {
                "visible": false,
                "mData": "reportRequestType",
                "bSortable": false,
                "sClass": "dataTableColumnCenter"
            },
            {
                "visible": false,
                "mData": "drugCode",
                "sClass": "dataTableColumnCenter"
            },
            {
                "visible": false,
                "mData" : "priority",
                "mRender": function (data, type, row) {
                    return '<span>' + encodeToHTML(row.priority) + '</span>';
                }
            },
            {
                "mData": "dueDate",
                "visible": false,
                "aTargets": ["dueDate"],
                "sClass": "dataTableColumnCenter",
                "mRender": function (data, type, full) {
                    return data ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                }
            },
            {
                "visible": false,
                "mData": "occurrences",
                "sClass": "dataTableColumnCenter"
            },
            {
                "mData": "linkedGeneratedReports",
                "sClass": "dataTableColumnCenter",
                "bSortable": false,
                "mRender": function (data, type, full) {
                    if (data) {
                        try {
                            var reports = JSON.parse(data);
                            var out = "";
                            for (var i = 0; i < reports.length; i++) {
                                out += '<a href = "' + reportRedirectURL + '?id=' + reports[i].id + '" > ' + reports[i].name + ' </a><br>'
                            }
                            return out;
                        } catch (e) {
                            console.log(e)
                        }
                    }
                    return "";
                }
            }, {
                "mData": "requesters",
                "bSortable": false,
                "sClass": "dataTableColumnCenter",
                "mRender": function (data, type, row) {
                    return encodeToHTML(data);
                }
            },
            {
                "mData": "status",
                "sClass": "dataTableColumnCenter",
                "bSortable": false,
                "mRender": function (data, type, row) {
                    return '<button class="btn btn-default btn-xs btn-table-col-state" data-reportRequest-id= "' + row.reportRequestId + '" data-initial-state= "' + row.status + '" data-evt-clk=\'{\"method\": \"openStateHistoryModal\", \"params\": []}\' title="' + row.status + '">' + row.status + '</button>';
                }
            },
            {
                "mData": null,
                "bSortable": false,
                "mRender": function (data, type, row) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id=' + row.reportRequestId + '" data-value="' + row.reportRequestId + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style=" font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id=' + row.reportRequestId + '" data-value="' + row.reportRequestId + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="copy?id=' + row.reportRequestId + '">' + $.i18n._('copy') + '</a></li> ';
                    if (row.aggregate) {
                        actionButton += '<li role="presentation"><a class="work-flow-edit" role="menuitem" href="copyNext?id=' + row.reportRequestId + '">Create for next period</a></li> \
                                        <li role="presentation"><a class="work-flow-edit" role="menuitem" href="createReport?id=' + row.reportRequestId + '&configurationType=PERIODIC_REPORT">' + $.i18n._('reportRequest.aggregate.label') + '</a></li> '
                    } else {
                        actionButton += '<li role="presentation"><a class="work-flow-edit" role="menuitem" href="createReport?id=' + row.reportRequestId + '&configurationType=ADHOC_REPORT">' + $.i18n._('reportRequest.adhoc.label') + '</a></li> ';
                    }
                    actionButton += '<li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('reportRequest') + '" data-instanceid="' + row.reportRequestId + '" data-instancename="' + encodeToHTML(row.requestName) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }
        ]);


        $('#assignedToFilterControl').val(sessionStorage.getItem(sessionStorageAssignedToVariableName) ? sessionStorage.getItem(sessionStorageAssignedToVariableName) : "");

        //Initialize the datatable
        report_request_table = $("#reportRequestList").DataTable({
            deferRender: false,
            layout: {
                topStart: null,
                topEnd: { search: { placeholder: "Search" } },
                bottomStart: [
                    "pageLength",
                    "info",
                    {
                        paging: {
                            type: "full_numbers",
                        },
                    },
                ],
                bottomEnd: null,
            },
            language: { search: ''},
            initComplete: function () {

                initAssignedToFilter();
                //Toggle the action buttons on the action item list.
                actionButton('#reportRequestList');
                loadTableOption('#reportRequestList');
            },
            // "processing": true,
            "serverSide": true,
            //  "sPaginationType": "bootstrap",
            "paging": false,
            "ordering": false,
            "info": false,
            createdRow: function (row, data, dataIndex, cells) {
                if (!data.children) {
                    $(row).attr('draggable', 'true');
                }
            },


            drawCallback: function () {
                // Add HTML5 draggable event listeners to each row
                rows = document.querySelectorAll('#reportRequestList tbody tr');
                [].forEach.call(rows, function (row) {
                    row.addEventListener('dragstart', handleDragStart, false);
                    row.addEventListener('dragenter', handleDragEnter, false);
                    row.addEventListener('dragover', handleDragOver, false);
                    row.addEventListener('dragleave', handleDragLeave, false);
                    row.addEventListener('drop', handleDrop, false);
                    row.addEventListener('dragend', handleDragEnd, false);
                });
                hideLoader();
            },
            "ajax": {
                "url": reportRequestListUrl,
                "type": 'POST',
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    if ($("#statusFilter").val() != "") {
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
                    tableFilter.isRequestsPlan = {
                        "type": "manual",
                        "name": "isRequestsPlan",
                        "value": "true"
                    };
                    d.tableFilter = JSON.stringify(tableFilter);
                    d.advancedFilter = advancedFilter;
                    d.sharedwith = $('#assignedToFilterControl').val();
                    // if (d.order && d.order.length > 0) {
                    //     d.direction = d.order[0].dir;
                    //     //Column header mData value extracting
                    //     d.sort = d.columns[d.order[0].column].data;
                    // }
                }
            },
            'treeGrid': {
                'left': 0,
                // 'icon':'<span class="fa fa-remove"></span>',
                'expandIcon': '<span class="fa fa-caret-right" style="font-size: 20px;margin-left: 10px !important; cursor: pointer"><\/span>',
                'collapseIcon': '<span class="fa fa-caret-down" style="font-size:20px;margin-left: 10px !important;cursor: pointer"><\/span>'
            },
            columnDefs: [
                {width: "25", targets: 0},
                {width: "70", targets: 18}
            ],
            // "aaSorting": [[0, "desc"]],
            "bLengthChange": false,
            "scrollX": true,
            //"iDisplayLength": 10,
            "aoColumns": aoColumns
        });

        //-------------------------------------drag and drop block----------------------------

        var sourceId = null;
        var rows = [];

        function handleDragStart(e) {
            this.style.opacity = '0.4';
            srcTable = this.parentNode.parentNode.id;
            sourceId = $(this.closest('tr')).find("#reportRequestId").html();
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', e.target.outerHTML);

        }

        function handleDragOver(e) {
            if (e.preventDefault) {
                e.preventDefault(); // Necessary. Allows us to drop.
            }
            e.dataTransfer.dropEffect = 'move';
            var r = $(this.closest('tr'));
            if ((sourceId != r.find("#reportRequestId").html()) && (r.find(".masterPlanningRequest").text() == "true"))
                $(this.closest('tr')).addClass("over");
            return false;
        }

        function handleDragEnter(e) {
            var currentTable = this.parentNode.parentNode.id;
            var r = $(this.closest('tr'));
            if ((sourceId != r.find("#reportRequestId").html()) && (r.find(".masterPlanningRequest").text() == "true"))
                $(this.closest('tr')).addClass("over");

        }

        function handleDragLeave(e) {
            this.classList.remove('over');
        }

        function handleDrop(e) {
            if (e.stopPropagation) {
                e.stopPropagation(); // stops the browser from redirecting.
            }
            var r = $(this.closest('tr'));
            if ((sourceId != r.find("#reportRequestId").html()) && (r.find(".masterPlanningRequest").text() == "true"))
                setAsParent($(this.closest('tr')).find("#reportRequestId").html(), sourceId);
            return false;
        }

        function handleDragEnd(e) {
            this.style.opacity = '1.0';
            [].forEach.call(rows, function (row) {
                row.classList.remove('over');
                row.style.opacity = '1.0';
            });
        }

        function setAsParent(parentId, childId) {
            showLoader();
            $.ajax({
                url: "/reports/reportRequest/setAsParent",
                data: {parentId: parentId, childId: childId}
            })
                .fail(function (err) {
                    console.log(err);
                })
                .done(function (data) {
                    hideLoader();
                    report_request_table.ajax.reload(function (data) {
                    }, false).draw();
                })

        }

        function removeParent(id) {
            showLoader();
            $.ajax({
                url: "/reports/reportRequest/removeParent",
                data: {id: id}
            })
                .fail(function (err) {
                    console.log(err);
                })
                .done(function (data) {
                    hideLoader();
                    report_request_table.ajax.reload(function (data) {
                    }, false).draw();
                })
        }

        $(document).on("click", ".unlink", function () {
            removeParent($(this.closest('tr')).find("#reportRequestId").html());
        });
        init_table_filter();
        return report_request_table;
    };
    var init_table_filter = function () {
        var filter_data = [
            {
                label: $.i18n._("app.advancedFilter.requestId"),
                type: 'natural-number',
                name: 'id'
            }, {
                label: "Master Planning Request",
                type: 'boolean',
                name: 'masterPlanningRequest'
            },
            {
                label: "Request Type",
                type: 'select2-id',
                name: 'reportRequestType',
                ajax: {
                    url: '/reports/reportRequestType/list',
                    data_handler: function (data) {
                        return pvr.filter_util.build_options(data, 'id', 'name', true);
                    },
                    error_handler: function (data) {
                        console.log(data);
                    }
                }
            },
            {
                label: $.i18n._("app.advancedFilter.requestSummary"),
                type: 'text',
                name: 'reportName',
                maxlength: 555
            },
            {
                label: "INN",
                type: 'text',
                name: 'inn',
                maxlength: 255

            },
            {
                label: "Drug Code",
                type: 'text',
                name: 'drugCode',
                maxlength: 255
            },
            {
                label: "Product",
                type: 'text',
                name: 'productSelection'
            },
            {
                label: "Primary Destination",
                type: 'text',
                name: 'primaryReportingDestination',
                maxlength: 255
            },
            {
                label: "Reporting Period Start (From)",
                type: 'date-range',
                group: 'reportingPeriodStart',
                group_order: 1
            },
            {
                label: "Reporting Period Start (To)",
                type: 'date-range',
                group: 'reportingPeriodStart',
                group_order: 2
            },
            {
                label: "Reporting Period End (From)",
                type: 'date-range',
                group: 'reportingPeriodEnd',
                group_order: 1
            },
            {
                label: "Reporting Period End (To)",
                type: 'date-range',
                group: 'reportingPeriodEnd',
                group_order: 2
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
        init_report_request_table: init_report_request_table
    }

})();