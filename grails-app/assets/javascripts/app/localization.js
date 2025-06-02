$(function () {
    if ($('#rxTableLocalizationList').is(":visible")) {
        var table = $('#rxTableLocalizationList').DataTable({

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
                "url": LOCALIZATION.listUrl,
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            rowId: "locUniqueId",
            "aaSorting": [],
            autoWidth: false,
            "order": [[0, "asc"]],
            "bLengthChange": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "pagination": true,
            "iDisplayLength": 50,

            drawCallback: function (settings) {
                showTotalPage(settings.json.recordsFiltered);
                pageDictionary($('#rxTableLocalizationList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                {
                    "mData": "code",
                    width: "35%",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "locale",
                    width: "10%",
                    mRender: function (data, type, row) {
                        return (data == null) ? '' : encodeToHTML(data);
                    }
                },
                {
                     "mData": "text",
                     width: "47%",
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
                            var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center">  \
                        <a class="btn btn-success btn-xs" href="' + LOCALIZATION.viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + LOCALIZATION.editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                        </ul> \
                    </div>';
                        return actionButton;
                    }
                }
            ]
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        if (LOCALIZATION.preSearchString) {
            table.search(LOCALIZATION.preSearchString).draw();
            $('#rxTableLocalizationList_filter input').val(LOCALIZATION.preSearchString);
        }

        actionButton('#rxTableLocalizationList');
        loadTableOption('#rxTableLocalizationList');
    }
});