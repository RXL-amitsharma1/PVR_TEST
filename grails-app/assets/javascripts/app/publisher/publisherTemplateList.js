$(function () {

    var init = function () {
        init_table();
    };

    var init_table = function () {
        var table = $("#publisherTemplateList").DataTable({

            // "sPaginationType": "bootstrap",
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
            "language": {
                "search": '',
                "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                "searchPlaceholder": $.i18n._("fieldprofile.search.label")
            },

            initComplete: function () {
                //Toggle the action buttons on the action item list.
                actionButton('#publisherTemplateList');
            },

            "ajax": {
                "url": listPublisherTemplateUrl,
                "dataSrc": function (res) {
                    totalFilteredRecord = res["recordsFiltered"];
                    return res["aaData"];
                },
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        //Column header mData value extracting
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            "stateSave": true,
            "stateDuration": -1,
            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,
            "searching": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "pagination": true,
            "aaSorting": [[4, "desc"]],

            drawCallback: function (settings) {
                pageDictionary($('#publisherTemplateList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
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
                },
                {
                    "mData": "qualityChecked",
                    "mRender": function (data, type, full) {
                        return data ? $.i18n._('yes') : " "
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
                    "mData": null,
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
                                    data-target="#deleteModal" data-instancetype="Publisher Template" data-instanceid="' + row.id + '" data-instancename="' + row.name + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#publisherTemplateList tbody tr').each(function () {
                });
            }, 100);
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        loadTableOption('#publisherTemplateList');
    }
    init();
});