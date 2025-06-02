$(function () {
    var advancedFilter = false;
    var tableFilter = {};
    var table = $('#rxTableIcsrrProfileConfiguration').DataTable({
        "layout": {
            topStart: null,
            topEnd: {search: {placeholder: $.i18n._("fieldprofile.search.label")}},
            bottomStart: ['pageLength','info', {
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
            "url": ICSRPROFILECONF.listUrl,
            "type": "POST",
            "dataSrc": "data",
            "data": function (d) {
                d.searchString = d.search.value;
                d.tableFilter = JSON.stringify(tableFilter);
                d.advancedFilter = advancedFilter;
                if (d.order.length > 0) {
                    d.direction = d.order[0].dir;
                    d.sort = d.columns[d.order[0].column].data;
                }
            }
        },
        rowId: "IcsrProfileConfUniqueId",
        "aaSorting": [],
        "order": [[9, "desc"]],
        "bLengthChange": true,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "pagination": true,
        "iDisplayLength": 50,
        "columnDefs": [
            {"visible": false, "targets": [1]}
        ],
        drawCallback: function (settings) {
            pageDictionary($('#rxTableIcsrrProfileConfiguration_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
        },
        "aoColumns": [
            {
                "mData": "reportName",
                mRender: function (data, type, row) {
                    return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            },
            {
                "mData": "description",
                mrender: function (data, type, row) {
                    return '<span>' + row.description ? encodeToHTML(row.description) : "" + '</span>';
                }
            },
            {
                "mData": "senderOrganization",
                mRender: function (data, type, row) {
                    return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            },
            {
                "mData": "senderType",
                mRender: function (data, type, row) {
                    return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            },
            {
                "mData": "recipientOrganization",
                mRender: function (data, type, row) {
                    return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            },
            {
                "mData": "recipientType",
                mRender: function (data, type, row) {
                    return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            },
            {
                "mData": "qualityChecked",
                "sClass": "dataTableColumnCenter",
                "mRender": function (data, type, full) {
                    return data == true ? $.i18n._("yes") : "";
                }
            },
            {
                "mData": "isEnabled",
                "sClass": "dataTableColumnCenter",
                "mRender": function (data, type, row) {
                    return row.isDisabled == true ? $.i18n._("Yes") : "No";
                }
            },
            {
                "mData": "dateCreated",
                "aTargets": ["dateCreated"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {
                "mData": "lastUpdated",
                "aTargets": ["lastUpdated"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {"mData": "createdBy"},
            {
                "mData": null,
                "sClass": "dt-center",
                "bSortable": false,
                "aTargets": ["id"],
                "mRender": function (data, type, full) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center">  \
                                                    <a class="btn btn-success btn-xs" href="' + ICSRPROFILECONF.viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a> \
                                                    <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                                        <span class="caret"></span> \
                                                        <span class="sr-only">Toggle Dropdown</span> \
                                                    </button> \
                                                    <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                                        <li role="presentation"><a role="menuitem" href="' + ICSRPROFILECONF.editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                                                        <li role="presentation"><a role="menuitem" href="' + ICSRPROFILECONF.copyUrl + '/' + data["id"] + '">' + $.i18n._('copy') + '</a></li> \
                                                        <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                                            data-target="#deleteModal" data-instancetype="' + $.i18n._('icsrProfileConfiguration') + '" data-instanceid="' + data["id"] + '" data-instancename="' + replaceBracketsAndQuotes(data["reportName"]) + '">' + $.i18n._('delete') + '</a></li> \
                                                    </ul> \
                                                </div>';
                    return actionButton;
                }
            }
        ]
    }).on('draw.dt', function () {
        setTimeout(function () {
            $('#rxTableIcsrrProfileConfiguration tbody tr').each(function () {
            });
        }, 100);
    }).on('xhr.dt', function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json)
    });
    actionButton('#rxTableIcsrrProfileConfiguration');
    loadTableOption('#rxTableIcsrrProfileConfiguration');

    var init_filter = function () {
        var filter_data = [
            {
                label: $.i18n._("app.advancedFilter.icsrProfileName"),
                type: 'text',
                name: 'reportName',
                maxlength: 500
            },
            {
                label: $.i18n._("app.advancedFilter.description"),
                type: 'text',
                name: 'description',
                maxlength: 4000
            },
            {
                label: $.i18n._("app.advancedFilter.senderOrganization"),
                type: 'text',
                name: 'senderOrganization.unitName',
                maxlength: 255
            },
            {
                label: $.i18n._("app.advancedFilter.senderType"),
                type: 'text',
                name: 'senderOrganizationType.name',
                maxlength: 255
            },
            {
                label: $.i18n._("app.advancedFilter.recipientOrganization"),
                type: 'text',
                name: 'recipientOrganization.unitName',
                maxlength: 255
            },{
                label: $.i18n._("app.advancedFilter.recipientType"),
                type: 'text',
                name: 'recipientOrganizationType.name',
                maxlength: 255
            },
            {
                label: $.i18n._("app.advancedFilter.qualityChecked"),
                type: 'boolean',
                name: 'qualityChecked'
            },
            {
                label: $.i18n._("app.advancedFilter.disabled"),
                type: 'boolean',
                name: 'isDisabled'
            },{
                label: $.i18n._("app.advancedFilter.dateCreatedStart"),
                type: 'date-range',
                group: 'dateCreated',
                group_order: 1
            },{
                label: $.i18n._("app.advancedFilter.dateCreatedEnd"),
                type: 'date-range',
                group: 'dateCreated',
                group_order: 2
            },{
                label: $.i18n._("app.advancedFilter.dateModifiedStart"),
                type: 'date-range',
                group: 'lastUpdated',
                group_order: 1
            },{
                label: $.i18n._("app.advancedFilter.dateModifiedEnd"),
                type: 'date-range',
                group: 'lastUpdated',
                group_order: 2
            },{
                label: $.i18n._("app.advancedFilter.owner"),
                type: 'id',
                name: 'owner'
            }
        ];

        pvr.filter_util.construct_right_filter_panel({
            table_id: '#rxTableIcsrrProfileConfiguration',
            container_id: 'config-filter-panel',
            filter_defs: filter_data,
            column_count: 1,
            done_func: function (filter) {
                tableFilter = filter;
                advancedFilter = true;
                var dataTable = $('#rxTableIcsrrProfileConfiguration').DataTable();
                dataTable.ajax.reload(function (data) {
                }, false).draw();
            }
        });
        bindSelect2WithUrl($("select[data-name=owner]"), ownerListUrl, ownerValuesUrl, true).on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });

        $("select[data-name=isDisabled]").select2().on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });
    };

    var init = function () {
        // init_table();
        init_filter();
    };

    init();

});










