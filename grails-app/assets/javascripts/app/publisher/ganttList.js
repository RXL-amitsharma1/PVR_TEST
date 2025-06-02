$(function () {

    $("#ganttList").DataTable({
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
        initComplete: function () {
            //Toggle the action buttons on the action item list.
            actionButton('#ganttList');
        },
        drawCallback: function (settings) {
            if (settings && settings.json)
                pageDictionary($('#ganttList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
        },
        "ajax": {
            "url": ganttListUrl,
            "dataSrc": "data"
        },
        "stateSave": true,
        "stateDuration": -1,
        "aaSorting": [[1, "desc"]],
        "pagination": true,
        "bLengthChange": true,
        "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
        "iDisplayLength": 10,
        "aoColumns": [
            {
                "mData": "name"
            },
            {
                "mData": "lastUpdated",
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {
                "mData": "modifiedBy"
            },
            {
                "mData": null,
                "mRender": function (data, type, row) {
                    var actionButton = '<div class="btn-group dropdown " align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('email') + '" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.name) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }
        ]
    });
});