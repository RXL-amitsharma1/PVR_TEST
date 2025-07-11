$(function () {
    var tableFilter = {};
    var advancedFilter = false;

    var init = function () {
        var init_table = function () {
            var table = $('#rxTableConfiguration').DataTable({
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
                "stateSave": true,
                "stateDuration": -1,
                "customProcessing": true, //handled using processing.dt event
                "serverSide": true,
                "ajax": {
                    "url": CONFIGURATION.listUrl,
                    "contentType": "application/x-www-form-urlencoded",
                    "type": "POST",
                    "dataSrc": "data",
                    "data": function (d) {
                        d.tableFilter = JSON.stringify(tableFilter);
                        d.advancedFilter = advancedFilter;
                        d.searchString = d.search.value;
                        d.sharedwith = $('#sharedWithFilterControl').val();
                        if (d.order.length > 0) {
                            d.direction = d.order[0].dir;
                            //Column header mData value extracting
                            d.sort = d.columns[d.order[0].column].data;
                        }
                    }
                },
                "aaSorting": [],
                "order": [[0, "asc"], [7, "desc"]],
                "bLengthChange": true,
                "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
                "pagination": true,
                "pagingType": "full_numbers",
                "iDisplayLength": 50,
                drawCallback: function (settings) {
                    pageDictionary($('#rxTableConfiguration_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                },


            columnDefs: [
                    {width: "25", targets: 0}
                ],
                "aoColumns": [
                    //Don't Change mData labels as we are using it for our sorting parameter name for sorting data should be property name
                    {
                        "data": "isFavorite",
                        "sClass": "dataTableColumnCenter",
                        "asSorting": ["asc"],
                        "render": renderFavoriteIcon
                    },
                    {
                        "mData": "reportName",
                        mRender: function (data, type, row) {
                            var content = encodeToHTML(data);
                            return "<div class='three-row-dot-overflow' >" + content + "</div>";
                        }
                    },
                    {
                        "mData": "description",
                        mRender: function (data, type, row) {
                            var content = (data == null) ? '' : encodeToHTML(data);
                            return "<div class='three-row-dot-overflow'>" + content + "</div>";
                        }
                    },
                    {
                        "mData": "numOfExecutions",
                        "sClass": "dataTableColumnCenter"
                    },
                    {
                        "mData": "tags",
                        "bSortable": false,
                        "aTargets": ["tags"],
                        "mRender": function (data, type, full) {
                            var tags = data ? encodeToHTML(data) : '';
                            return "<div class='three-row-dot-overflow'>" + tags + "</div>";
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
                        "mData": "createdBy",
                        "mRender": $.fn.dataTable.render.text()
                    },
                    {
                        "mData": null,
                        "bSortable": false,
                        "sClass": "dt-center",
                        "aTargets": ["id"],
                        "mRender": function (data, type, full) {
                            var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="btn btn-success btn-xs"  data-evt-clk=\'{\"method\": \"disableEventBinding\", \"params\":[\"'+CONFIGURATION.runUrl+ '/'+ data["id"]+ '?isPriorityReport=false\"]}\'>' + $.i18n._('run') + '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + CONFIGURATION.viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="' + CONFIGURATION.editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="' + CONFIGURATION.copyUrl + '/' + data["id"] + '">' + $.i18n._('copy') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                data-target="#deleteModal" data-instancetype="' + $.i18n._('configuration') + '" data-instanceid="' + data["id"] + '" data-instancename="' + replaceBracketsAndQuotes(data["reportName"]) + '">' + $.i18n._('delete') + '</a></li>';
                            if(isPriorityRoleEnable) {
                                actionButton = actionButton + '<li role="presentation"><a role="menuitem" href="' + CONFIGURATION.runUrl + '/' + data["id"] + '?isPriorityReport=true">' + $.i18n._('prioritize.report') + '</a></li>';
                            }
                            actionButton = actionButton + '</ul> \
                        </div>';
                            return actionButton;
                        }
                    }
                ],
                initComplete: function () {
                    initSharedWithFilter("rxTableConfiguration", table);
                    $("#rxTableConfiguration").on("click", ".favorite", function () {
                        changeFavoriteState($(this).data('exconfig-id'), $(this).hasClass("glyphicon-star-empty"), $(this));
                    });
                }
            }).on('draw.dt', function () {
                updateTitleForThreeRowDotElements();
                eventBindingClk();
            }).on('xhr.dt', function (e, settings, json, xhr) {
                checkIfSessionTimeOutThenReload(e, json)
            });
            actionButton('#rxTableConfiguration');
            loadTableOption('#rxTableConfiguration');
        };

        var init_table_filter = function () {
            var filter_data = [
                {
                    label: $.i18n._("app.advancedFilter.reportName"),
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
                    label: $.i18n._("app.advancedFilter.email"),
                    type: 'text',
                    name: 'email',
                    maxlength: 200 //in accordance with com.rxlogix.config.Email.constrainedProperties.email.maxSize
                },
                {
                    label: $.i18n._("app.advancedFilter.runTimes"),
                    type: 'natural-number',
                    name: 'numOfExecutions'
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
                    },
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
                    label: $.i18n._("app.advancedFilter.owner"),
                    type: 'id',
                    name: 'owner'
                }
            ];

            pvr.filter_util.construct_right_filter_panel({
                table_id: '#rxTableConfiguration',
                container_id: 'config-filter-panel',
                filter_defs: filter_data,
                column_count: 1,
                done_func: function (filter) {
                    tableFilter = filter;
                    advancedFilter = true;
                    var dataTable = $('#rxTableConfiguration').DataTable();
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
        };

        init_table();
        init_table_filter();
    };

    init();

    let eventBindingClk = (function () {
        $("[data-evt-clk]").on('click', function (e) {
            const eventData = JSON.parse($(this).attr("data-evt-clk"));
            const methodName = eventData.method;
            const params = eventData.params;
            if (methodName == "disableEventBinding") {
                var eventElement = $(this);
                disableEventBinding(eventElement, params);
            }
        });
    });
});

function disableEventBinding(eventElement, params) {
    $(eventElement).on('click', function (e) {
        if ($(this).attr("disabled") == "disabled") {
            e.preventDefault();
        }
    });
    $(eventElement).attr("disabled", "disabled");
    location.href = params[0];
}