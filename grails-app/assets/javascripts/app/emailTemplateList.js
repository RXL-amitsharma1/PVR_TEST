$(function () {

    var email_template_table = $("#emailTemplateList").DataTable({
        "layout": {
            topStart: null,
            topEnd: {search: {placeholder: 'Search'}},
            bottomStart: ['pageLength','info', {
                paging: {
                    type: 'full_numbers'
                }
            }],
            bottomEnd: null,
        },
        language: { search: ''},
        //"sPaginationType": "bootstrap",
        initComplete: function () {
            actionButton('#emailList');
        },

        "ajax": {
            "url": listUrl,
            "dataSrc": ""
        },
        "aaSorting": [[4, "desc"]],
        "bLengthChange": true,
        "pagination": true,
        "pagingType": "full_numbers",
        "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
        "iDisplayLength": 10,
        "stateSave": true,
        "stateDuration": -1,
        "aoColumns": [
            {
                "mData": "name",
                mRender: function (data, type, row) {
                    return encodeToHTML(data);
                }
            },
            {
                "mData": "description",
                mRender: function (data, type, row) {
                    return encodeToHTML(data);
                }
            },
            {
                "mData": "owner",
                "mRender": $.fn.dataTable.render.text()
            },
            {
                "mData": "type",
                "mRender": $.fn.dataTable.render.text()
            },
            {
                "mData": "lastUpdated",
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {
                "mData": "modifiedBy",
                "mRender": $.fn.dataTable.render.text()
            },
            {
                "bSortable": false,
                "mData": null,
                "mRender": function (data, type, row) {
                    var actionButton = '<div class="btn-group dropdown" align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('emailTemplate') + '" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(encodeToHTML(row.name)) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }
        ]
    });

});