$(function () {
    var tableFilter = {};
    var advancedFilter = false;

    var init_table = function () {
        var table = $('#rxTableTemplates').DataTable({
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
            "stateSave": true,
            "stateDuration": -1,
            //"sPaginationType": "bootstrap",

            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,
            "ajax": {
                "url": TEMPLATE.listUrl,
                "contentType": "application/x-www-form-urlencoded",
                "type": "POST",
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    d.tableFilter = JSON.stringify(tableFilter);
                    d.advancedFilter = advancedFilter;
                    d.sharedwith = $('#sharedWithFilterControl').val()
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        //Column header mData value extracting
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            "aaSorting": [],
            "order": [[0, "asc"], [9, "desc"]],
            "bLengthChange": true,
            "bAutoWidth": false,
            "columnDefs": [
                {"width": "4%", "targets": 0},
                {"width": "7%", "targets": 1},
                {"width": "8%", "targets": 2},
                {"width": "10%", "targets": 3},
                {"width": "13%", "targets": 4},
                {"width": "7%", "targets": 5},
                {"width": "6%", "targets": 6},
                {"width": "10%", "targets": 7},
                {"width": "10%", "targets": 8},
                {"width": "10%", "targets": 9},
                {"width": "10%", "targets": 10},
                {"width": "5%", "targets": 11}

            ],
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "pagination": true,
            "iDisplayLength": 50,

            drawCallback: function (settings) {
                pageDictionary($('#rxTableTemplates_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                //Don't Change mData labels as we are using it for our sorting parameter name for sorting data should be property name
                {
                    "data": "isFavorite",
                    "sClass": "dataTableColumnCenter",
                    "asSorting": ["asc"],
                    "bSortable": true,
                    "render": renderFavoriteIcon
                },
                {
                    "mData": "templateType",
                    "bSortable": true
                },
                {
                    "mData": "category",
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
                    "visible": false,
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
                    "visible": false,
                    "mRender": function (data, type, full) {
                        var tags = data ? encodeToHTML(data) : '';
                        return "<div class='three-row-dot-overflow'>" + tags + "</div>";
                    }
                },
                {
                    "mData": null,
                    "sClass": "dt-center",
                    "bSortable": false,
                    "aTargets": ["id"],
                    "mRender": function (data, type, full) {
                        var runUrl = (data.templateType === 'ICSR XML') ? TEMPLATE.runIcsrUrl : TEMPLATE.runUrl;
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="' + runUrl + '/?selectedTemplate=' + data["id"] + '">' + $.i18n._('run') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" href="' + TEMPLATE.viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="' + TEMPLATE.editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="' + TEMPLATE.copyUrl + '/' + data["id"] + '">' + $.i18n._('copy') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                        data-target="#deleteModal" data-instancetype="' + $.i18n._('template') + '"  data-instanceid="' + data["id"] + '" data-instancename="' + replaceBracketsAndQuotes(data["name"]) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ],
            initComplete: function () {
                initSharedWithFilter("rxTableTemplates", table);
                $("#rxTableTemplates").on("click", ".favorite", function () {
                    changeFavoriteState($(this).data('exconfig-id'), $(this).hasClass("glyphicon-star-empty"), $(this));
                });
            }
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('.dropdown-toggle').on('click', function () {
                    setTimeout(function () {
                        var e = $(".dt-scroll-body")[0];
                        if (e && e.scrollHeight > e.clientHeight) {
                            $(e).find('table tr:last').after('<tr class="temprow"><td style="height:' +
                                (e.scrollHeight - e.clientHeight) + 'px; border-top:none " colspan="13">&nbsp;</td></tr>');
                        }
                    }, 200);
                });
                $('.dropdown').on('hide.bs.dropdown', function () {
                    $('.temprow').remove();
                });
            }, 100)
            updateTitleForThreeRowDotElements();
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json);
        });
        actionButton('#rxTableTemplates');
        loadTableOption('#rxTableTemplates');
    };

    var init_filter = function () {
        var filter_data = [
            {
                label: $.i18n._("app.advancedFilter.type"),
                type: 'select2-enum',
                name: 'templateType',
                data_type: 'TemplateTypeEnum',
                data: TEMPLATE.templateTypes
            },
            {
                label: $.i18n._("app.advancedFilter.category"),
                type: 'select2-id',
                name: 'category',
                ajax: {
                    url: '/reports/lookup?name=category',
                    data_handler: function (data) {
                        return pvr.filter_util.build_options(data, 'id', 'name', true);
                    },
                    error_handler: function (data) {
                        console.log(data);
                    }
                }
            },
            {
                label: $.i18n._("app.advancedFilter.templateName"),
                type: 'text',
                name: 'name',
                maxlength: 1000
            },
            {
                label: $.i18n._("app.advancedFilter.description"),
                type: 'text',
                name: 'description',
                maxlength: 1000
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
                label: $.i18n._("app.label.qc"),
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
            table_id: '#rxTableTemplates',
            container_id: 'config-filter-panel',
            filter_defs: filter_data,
            column_count: 1,
            done_func: function (filter) {
                tableFilter = filter;
                advancedFilter = true;
                var dataTable = $('#rxTableTemplates').DataTable();
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

        $("select[data-name=templateType]").select2().on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });

        $("select[data-name=category]").select2().on("select2:open", function (e) {
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
