$(function () {
    var tableFilter = {};
    var advancedFilter = false;

    var init_table = function () {
        var table = $('#rxTableQueries').DataTable({
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
                "url": listQueriesUrl,
                "contentType": "application/x-www-form-urlencoded",
                "type": "POST",
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    d.advancedFilter = advancedFilter;
                    d.tableFilter = JSON.stringify(tableFilter);

                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sharedwith = $('#sharedWithFilterControl').val();
                        //Column header mData value extracting
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            "aaSorting": [],
            "order": [[0, "asc"], [8, "desc"]],
            "bLengthChange": true,
            "aLengthMenu": [[10, 50, 100, 200, 500], [10, 50, 100, 200, 500]],
            "pagination": true,
            "iDisplayLength": 10,

            drawCallback: function (settings) {
                pageDictionary($('#rxTableQueries_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "autoWidth": false,
            "aoColumns": [
                //Don't Change mData labels as we are using it for our sorting parameter name for sorting data should be property name
                {
                "data": "isFavorite",
                "sClass": "dataTableColumnCenter",
                "asSorting": [ "asc" ],
                "render": renderFavoriteIcon
            },
                {
                    "mData": "queryType",
                    "bSortable": true
                },
                {
                    "mData": "name",
                    mRender: function (data, type, row) {
                        return "<div class='three-row-dot-overflow'>" + encodeToHTML(data) + "</div>";
                    }
                },
                {
                    "mData": "description",
                    mRender: function (data, type, row) {
                        var text = (data == null) ? '' : encodeToHTML(data);
                        return '<div class="three-row-dot-overflow">'
                            + text +
                            '</div>';
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
                    "mData": "checkUsage",
                    "aTargets": ["checkUsage"],
                    "bSortable": false,
                    "sClass": "dataTableColumnCenter"
                },
                {"mData": "owner.fullName"},
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
                {
                    "mData": "lastExecuted",
                    "aTargets": ["lastExecuted"],
                    "sClass": "dataTableColumnCenter forceLineWrapDate",
                    "mRender": function (data, type, full) {
                        if (data != null) {
                            return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                        } else {
                            return "";
                        }
                    }
                },
                {
                    "mData": "tags",
                    "bSortable": false,
                    "aTargets": ["tags"],
                    "mRender": function (data, type, full) {
                        var tags = data ? encodeToHTML(data) : '';
                        return tags;
                    }
                },
                {
                    "mData": null,
                    "sClass": "dt-center",
                    "bSortable": false,
                    "aTargets": ["id"],
                    "mRender": function (data, type, full) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="' + runUrl + '/?selectedQuery=' + data["id"] + '">' + $.i18n._('run') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" href="' + queryViewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="' + queryEditUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="' + queryPreviewUrl + '?selectedQuery=' + data["id"] + '">' + $.i18n._('preview') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="' + queryCopyUrl + '/' + data["id"] + '">' + $.i18n._('copy') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                        data-target="#deleteModal" data-instancetype="' + $.i18n._('query') + '" data-instanceid="' + data["id"] + '" data-instancename="' + replaceBracketsAndQuotes(data["name"]) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ],
            initComplete: function () {
                initSharedWithFilter("rxTableQueries", table);
                            $("#rxTableQueries").on("click", ".favorite", function () {
                changeFavoriteState($(this).data('exconfigId'), $(this).hasClass("glyphicon-star-empty"), $(this));
            });

            }
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('.dropdown-toggle').on('click', function () {
                    setTimeout(function () {
                        var e = $(".dt-scroll-body")[0];
                        if (e && e.scrollHeight > e.clientHeight) {
                            $(e).find('table tr:last').after('<tr class="temprow"><td style="height:' +
                                (e.scrollHeight - e.clientHeight) + 'px; border-top:none " colspan="12">&nbsp;</td></tr>');
                        }
                    }, 200);
                });
                $('.dropdown').on('hide.bs.dropdown', function () {
                    $('.temprow').remove();
                });
            }, 100);
            updateTitleForThreeRowDotElements();
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json);
        });
        actionButton('#rxTableQueries');
        loadTableOption('#rxTableQueries');
    };

    var init_filter = function () {
        var filter_data = [
            {
                label: $.i18n._("app.advancedFilter.type"),
                type: 'select2-enum',
                name: 'queryType',
                data_type: 'QueryTypeEnum',
                data: queryTypes
            },
            {
                label: $.i18n._("app.advancedFilter.queryName"),
                type: 'text',
                name: 'name',
                maxlength : 1000
            },
            {
                label: $.i18n._("app.advancedFilter.description"),
                type: 'text',
                name: 'description',
                maxlength : 1000

            },
            {
                label: $.i18n._("app.advancedFilter.tag"),
                type: 'select2-multi-id',
                name: 'tag',
                ajax: {
                    url: '/reports/tag',
                    data_handler: function (data) {
                        return pvr.filter_util.build_options(data, 'id', 'name', false);
                    },
                    error_handler: function (data) {
                        console.log(data);
                    }
                }
            },
            {
                label: $.i18n._("app.advancedFilter.qualityChecked"),
                type: 'boolean',
                name: 'qualityChecked'
            },
            {
                label: $.i18n._("app.advancedFilter.dateCreatedStart"),
                type: 'date-range',
                group: 'dateCreated',
                group_order: 1
            },
            {
                label: $.i18n._("app.advancedFilter.dateCreatedEnd"),
                type: 'date-range',
                group: 'dateCreated',
                group_order: 2
            },
            {
                label: $.i18n._("app.advancedFilter.dateModifiedStart"),
                type: 'date-range',
                group: 'lastUpdated',
                group_order: 1
            },
            {
                label: $.i18n._("app.advancedFilter.dateModifiedEnd"),
                type: 'date-range',
                group: 'lastUpdated',
                group_order: 2
            },
            {
                label: $.i18n._("app.advancedFilter.dateLastExecutedStart"),
                type: 'date-range',
                group: 'lastExecuted',
                group_order: 1
            },
            {
                label: $.i18n._("app.advancedFilter.dateLastExecutedEnd"),
                type: 'date-range',
                group: 'lastExecuted',
                group_order: 2
            },
            {
                label: $.i18n._("app.advancedFilter.owner"),
                type: 'id',
                name: 'owner'
            }
        ];

        pvr.filter_util.construct_right_filter_panel({
            table_id: '#rxTableQueries',
            container_id: 'config-filter-panel',
            filter_defs: filter_data,
            column_count: 1,
            done_func: function (filter) {
                tableFilter = filter;
                advancedFilter = true;
                var dataTable = $('#rxTableQueries').DataTable();
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

        $("select[data-name=qualityChecked]").select2().on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });

        $("select[data-name=queryType]").select2().on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });
    };

    var init = function () {
        init_table();
        init_filter();
    };

    init();
});

