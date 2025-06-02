$(function () {
    if ($('#rxTableReportField').is(":visible")) {
        var table = $('#rxTableReportField').DataTable({
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
                "url": REPORTFIELD.listUrl,
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            rowId: "reportFieldUniqueId",
            "aaSorting": [],
            "order": [[0, "asc"]],
            "bLengthChange": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "pagination": true,
            "iDisplayLength": 50,

            drawCallback: function (settings) {
                showTotalPage(settings.json.recordsFiltered);
                pageDictionary($('#rxTableReportField_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                {
                    "mData": "name",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "fieldGroup",
                    mRender: function (data, type, row) {
                        return (data == null) ? '' : encodeToHTML(data);
                    }
                },
                {
                    "mData": null,
                    "sClass":"dt-center",
                    "bSortable": false,
                    "aTargets": ["id"],
                    "mRender": function (data, type, full) {
                        if (REPORTFIELD.isCreatedByUserList.includes(data["id"])) {
                            var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center">  \
                        <a class="btn btn-success btn-xs" href="' + REPORTFIELD.viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + REPORTFIELD.editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                data-target="#deleteModal" data-instancetype="' + $.i18n._('Report Field') + '" data-instanceid="' + data["id"] + '" data-instancename="' + replaceBracketsAndQuotes(encodeToHTML(data["name"])) + '">' + $.i18n._('delete') + '</a></li> \
                        </ul> \
                    </div>';
                        }

                        else {
                            var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center">  \
                        <a class="btn btn-success btn-xs" href="' + REPORTFIELD.viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + REPORTFIELD.editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                            \
                        </ul> \
                    </div>';
                        }
                        return actionButton;
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#rxTableReportField tbody tr').each(function () {
                    $(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                    $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
                });
            }, 100);
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        actionButton('#rxTableReportField');
        loadTableOption('#rxTableReportField');
    }

    $("#fieldGroupId").select2();
});