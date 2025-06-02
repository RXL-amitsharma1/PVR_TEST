$(function (e) {
    var init = function () {
        init_table();
    };

    var init_table = function () {
        $('#dashboardList').DataTable({
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
            initComplete: function () {
                //Toggle the action buttons on the action item list.
                actionButton('#dashboardList');
            },

            "ajax": {
                "url": listUrl,
                "dataSrc": ""
            },
            "aaSorting": [[0, "asc"]],
            "bLengthChange": true,
            "stateSave": true,
            "stateDuration": -1,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "pagingType": "full_numbers",
            "iDisplayLength": 10,

            "aoColumns": [
                {
                    "mData": "label",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "owner"
                },
                {
                    "mData": "dashboardType"
                },
                {
                    "mData": "sharedWith"
                },
                {
                    "mData": "sharedWithGroup"
                },
                {
                    "mData": null,
                    "bSortable": false,
                    "mRender": function (data, type, row) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="dashboard" data-instanceid="' + row.id + '" data-instancename="' + row.label + '">' + $.i18n._('delete') + '</a></li> \
                               <li role="presentation"><a class="work-flow-edit" role="menuitem" href="' + dasboardURL + '?id=' + row.id + '" >' + $.i18n._('setup') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        });
    };

    init();
});