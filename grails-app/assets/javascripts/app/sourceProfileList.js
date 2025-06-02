var sourceProfile = sourceProfile || {};

sourceProfile.sourceProfileList = (function () {

    var source_profile_table;

    //The function for initializing the source profile data tables.
    var init_source_profile_table = function (url) {

        //Initialize the datatable
        source_profile_table = $("#sourceProfileList").DataTable({
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
                actionButton('#sourceProfileList');
            },

            "ajax": {
                "url": url,
                "dataSrc": ""
            },

            "aaSorting": [[0, "asc"]],
            "bLengthChange": true,


            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "iDisplayLength": 10,

            "aoColumns": [
                {
                    "mData": "sourceProfileId",
                    "visible": false,
                    "mRender": function (data, type, row) {
                        return '<span id="actionItemId">' + row.sourceProfileId + '</span>';
                    }
                },
                {
                    "mData": "sourceId",
                    "sClass": "dataTableColumnCenter"
                },
                {
                    "mData": "sourceName",
                    "sClass": "dataTableColumnCenter",
                    "mRender": function (data, type, row) {
                        return '<span>' + encodeToHTML(row.sourceName) + '</span>';
                    }
                },
                {
                    "mData": "sourceAbbrev",
                    "sClass": "dataTableColumnCenter"
                },
                {
                    "mData": "isCental",
                    "sClass": "dataTableColumnCenter"
                },
                {
                    "mData": null,
                    "bSortable": false,
                    "mRender": function (data, type, row) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id=' + row.sourceProfileId + '" data-value="' + row.sourceProfileId + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id=' + row.sourceProfileId + '" data-value="' + row.sourceProfileId + '">' + $.i18n._('edit') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                var lastTh = $('.pv-caselist.basicDataTable table th:last-child');
                lastTh.removeClass('sorting');
                lastTh.off();
            }, 100);
        });
        return source_profile_table;
    };
    return {
        init_source_profile_table: init_source_profile_table
    }

})();