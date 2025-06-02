$(function () {
    var tableFilter = {};
    var advancedFilter = false;

    var init_table = function () {
        var table = $('#icsrReportsList').DataTable({
            "layout": {
                topStart: null,
                topEnd: {search: {placeholder: $.i18n._("fieldprofile.search.label")}},
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
                "url": icsrReportsListUrl,
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
            "order": [[0, "asc"], [8, "desc"]],
            columnDefs: [
                { orderable : false, targets: 0 },
                {width: "25", targets: 0}
            ],

            "bLengthChange": true,
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "pagination": true,
            "iDisplayLength": 50,

            drawCallback: function (settings) {
                pageDictionary($('#icsrReportsList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
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
                    "mData": "primaryReportingDestination"
                },
                {
                    "mData": "tags",
                    "bSortable": false,
                    "aTargets": ["tags"],
                    "mRender": function (data, type, full) {
                        var tags = data ? encodeToHTML(data) : '';
                        return "<div class='three-row-dot-overflow'>"+tags+"</div>";
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
                    "sClass": "dataTableColumnCenter nowrap",
                    "mRender": function (data, type, full) {
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    }
                },
                {
                    "mData": "lastUpdated",
                    "aTargets": ["lastUpdated"],
                    "sClass": "dataTableColumnCenter nowrap",
                    "mRender": function (data, type, full) {
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    }
                },
                {"mData": "createdBy"},
                {
                    "mData": null,
                    "bSortable": false,
                    "sClass": "dt-center",
                    "aTargets": ["id"],
                    "mRender": function (data, type, full) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="btn btn-success btn-xs" data-evt-clk=\'{\"method\": \"disableEventBinding\", \"params\":[\"' + LINKS.runUrl + '/' + data["id"] + '?isPriorityReport=false\"]}\'>' + $.i18n._('run') + '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + LINKS.viewUrl + '/' + data["id"] + '">' + $.i18n._('view') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="' + LINKS.editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="' + LINKS.copyUrl + '/' + data["id"] + '">' + $.i18n._('copy') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                data-target="#deleteModal" data-instancetype="' + $.i18n._('configuration') + '" data-instanceid="' + data["id"] + '" data-instancename="' + replaceBracketsAndQuotes(data["reportName"]) + '">' + $.i18n._('delete') + '</a></li>';
                        if(isPriorityRoleEnable) {
                            actionButton = actionButton + '<li role="presentation"><a role="menuitem" data-evt-clk=\'{\"method\": \"disableEventBinding\", \"params\":[\"' + LINKS.runUrl + '/' + data["id"] + '?isPriorityReport=true\"]}\'>' + $.i18n._('prioritize.report') + '</a></li>';
                        }
                        actionButton = actionButton + '</ul> \
                    </div>';
                        return actionButton;
                    }
                }
            ],
            initComplete: function () {
                initSharedWithFilter("icsrReportsList", table);
                $("#icsrReportsList").on("click", ".favorite", function () {
                    changeFavoriteState($(this).data('exconfig-id'), $(this).hasClass("glyphicon-star-empty"), $(this));
                });

            }
        }).on('draw.dt', function () {
            updateTitleForThreeRowDotElements();
            eventBindingClk();
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        actionButton('#icsrReportsList');
        loadTableOption('#icsrReportsList');
    };

    var init_filter = function () {
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
                maxlength: 200
            },
            {
                label: $.i18n._("app.advancedFilter.runTimes"),
                type: 'natural-number',
                name: 'numOfExecutions'
            },
            {
                label: $.i18n._("app.advancedFilter.primaryReportingDestination"),
                type: 'text',
                name: 'primaryReportingDestination',
                maxlength: 255
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
                label: $.i18n._("app.advancedFilter.owner"),
                type: 'id',
                name: 'owner'
            }
        ];

        pvr.filter_util.construct_right_filter_panel({
            table_id: '#icsrReportsList',
            container_id: 'config-filter-panel',
            filter_defs: filter_data,
            column_count: 1,
            done_func: function (filter) {
                tableFilter = filter;
                advancedFilter = true;
                var dataTable = $('#icsrReportsList').DataTable();
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

    var init = function () {
        init_table();
        init_filter();
    };

    init();

    let eventBindingClk = (function () {
        $("[data-evt-clk]").on('click', function (e) {
            e.preventDefault();
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
