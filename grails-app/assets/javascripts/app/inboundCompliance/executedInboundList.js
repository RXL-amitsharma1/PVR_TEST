$(function () {
    if ($('#rxTableReports').is(":visible")) {

        var table = $('#rxTableReports').DataTable({
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
            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,
            "ajax": {
                "url": indexReportUrl,
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            rowId: "rxTableReportUniqueId",
            "aaSorting": [],
            "order": [[3, "desc"]],
            "bLengthChange": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "pagination": true,
            "iDisplayLength": 10,

            drawCallback: function (settings) {
                pageDictionary($('#rxTableReports_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                {
                    "mData": "senderName",
                    "aTargets": ["senderName"],
                    "mRender": function (data, type, row) {
                        var link = viewExConfigUrl + '/' + row.id;
                        data = encodeToHTML(data);
                        return '<a href=' + link + '>' + data + '</a>'
                    }
                },
                {
                    "mData": "description",
                    mRender: function (data, type, row) {
                        return (data == null) ? data : encodeToHTML(data);
                    }
                },
                {"mData": "owner"},
                {
                    "mData": "dateCreated",
                    "aTargets": ["dateCreated"],
                    "sClass": "dataTableColumnCenter forceLineWrapDate mw-120",
                    "mRender": function (data, type, full) {
                        var dateCreated = Date.parse(data);
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT)
                    }
                },
                {
                    "mData": "tags",
                    "bSortable": false,
                    "aTargets": ["tags"],
                    "mRender": function (data, type, full) {
                        return data ? encodeToHTML(data) : '';
                    }
                },
                {
                    "mData": null,
                    "bSortable": false,
                    "sClass": "dataTableColumnCenter",
                    "aTargets": ["id"],
                    "mRender": function (data, type, row) {

                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                                                <a class="btn btn-success btn-xs" href="' + showReportUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a> \
                                                <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                                <span class="caret"></span> \
                                                <span class="sr-only">Toggle Dropdown</span> \
                                                </button> \
                                                <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                                <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                                        data-target="#deleteModal" data-instancetype="' + $.i18n._('configuration') + '" data-instanceid="' + data["id"] + '" data-instancename="' + replaceBracketsAndQuotes(data["senderName"]) + '">' + $.i18n._('delete') + '</a></li> ';
                                            actionButton += ' </ul></div>';
                        return actionButton;
                    }

                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#rxTableReports tbody tr').each(function () {
                    /*$(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                    $(this).find('td:eq(3)').attr('nowrap', 'nowrap');*/
                });
            }, 100);
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        actionButton('#rxTableReports');
        loadTableOption('#rxTableReports');
    }
});