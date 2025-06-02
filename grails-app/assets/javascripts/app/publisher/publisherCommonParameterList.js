$(function () {


    var table = $("#publisherCommonParameterList").DataTable({

        // "sPaginationType": "bootstrap",
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
        "language": {
            "search": '',
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },

        initComplete: function () {
            //Toggle the action buttons on the action item list.
            actionButton('#publisherCommonParameterList');
        },

        "ajax": {
            "url": "list",
            "dataSrc": ""
        },
        "stateSave": true,
        "stateDuration": -1,
        "aaSorting": [[1, "asc"]],
        "bLengthChange": true,
        "iDisplayLength": 10,
        drawCallback: function (settings) {
            var api = this.api();
            pageDictionary($('#publisherCommonParameterList_wrapper')[0], settings.aLengthMenu[0][0],  api.rows().count());
        },

        "aoColumns": [
            {
                "mData": "id",
                "visible": false,
                "mRender": function (data, type, row) {
                    return '<span id="actionItemId">' + row.id + '</span>';
                }
            },
            {
                "mData": "name"
            },
            {
                "mData": "description",
                mRender: function (data, type, row) {
                    return encodeToHTML(data);
                }
            }, {
                "mData": "value",
                mRender: function (data, type, row) {
                    return encodeToHTML(data);
                }
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
                "sClass": "mw-100",
                "mData": null,
                "mRender": function (data, type, row) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs btn-left-round" href="show?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs btn-right-round dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('publisher.PublisherCommonParameter') + '" data-instanceid="' + row.id + '" data-instancename="' + row.name + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }
        ]
    });

});