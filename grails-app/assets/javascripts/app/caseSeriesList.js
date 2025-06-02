var caseSeries = caseSeries || {};

$(function () {
    var case_series_table;
    var tableFilter = {};
    var advancedFilter = false;

    //The function for initializing the case tables.
    var init_case_list_table = function () {
        //Initialize the data table
        case_series_table = $("#caseSeriesList").DataTable({
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
            initComplete: function () {

                //Toggle the action buttons on the case series.
                actionButton('#caseSeriesList');
                initSharedWithFilter("caseSeriesList", case_series_table);
                $("#caseSeriesList").on("click", ".favorite", function () {
                    changeFavoriteState($(this).data('exconfigId'), $(this).hasClass("glyphicon-star-empty"), $(this));
                });
            },
            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,
            "ajax": {
                "url": listCaseSeries,
                "type": "POST",
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    d.tableFilter = JSON.stringify(tableFilter);
                    d.advancedFilter = advancedFilter;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        //Column header mData value extracting
                        d.sort = d.columns[d.order[0].column].data;
                        d.sharedwith = $('#sharedWithFilterControl').val();
                    }
                }
            },
            rowId: "caseUniqueId",
            "aaSorting": [],
            //"aaSorting": [ [0,'asc'], [1,'asc'] ],
            "order": [[0, "asc"], [7, "desc"]],
            columnDefs: [
                {width: "25",targets: 0}
            ],
            "bLengthChange": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "pagination": true,
            "iDisplayLength": 10,

            drawCallback: function (settings) {
                pageDictionary($('#caseSeriesList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },
            "aoColumns": [
                {
                    "data": "isFavorite",
                    "sClass": "dataTableColumnCenter",
                    "asSorting": [ "asc" ],
                    "render": renderFavoriteIcon
                },
                {
                    "mData": "seriesName",
                    "mRender": function (data, type, row) {
                        return '<span class="seriesName three-row-dot-overflow">' + encodeToHTML(row.seriesName) + '</span>';
                    }
                },
                {
                    "mData": "description",
                    "mRender": function (data, type, row) {
                        return '<span class="description three-row-dot-overflow">' + (row.description ? encodeToHTML(row.description) : "") + '</span>';
                    }
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
                    "mData": "numExecutions",
                    "sClass": "dataTableColumnCenter"
                }, {
                    "mData": "qualityChecked",
                    "sClass": "dataTableColumnCenter",
                    "mRender": function (data, type, full) {
                        return data == true ? $.i18n._("yes") : "";
                    }
                },
                {
                    "sClass": "nowrap",
                    "mData": "dateCreated",
                    "mRender": function (data, type, row) {
                        return '<span>' + row.dateCreated ? moment.utc(row.dateCreated).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "" + '</span>';
                    }
                },
                {
                    "sClass": "nowrap",
                    "mData": "lastUpdated",
                    "mRender": function (data, type, row) {
                        return '<span>' + row.lastUpdated ? moment.utc(row.lastUpdated).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "" + '</span>';
                    }
                },
                {
                    "mData": "owner",
                    "mRender": function (data, type, row) {
                        return '<span>' + row.owner.fullName ? row.owner.fullName : "" + '</span>';
                    }
                },
                {
                    "mData": null,
                    "bSortable": false,
                    'width': "10%",
                    "mRender": function (data, type, row) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs"  data-evt-clk=\'{\"method\": \"disableEventBinding\", \"params\":[\"'+runNowUrl+ '/'+row.id+'\"]}\'>' + $.i18n._('run') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" href="' + showURL + '/' + row.id + '" data-value="' + row.id + '">' + $.i18n._('view') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit " role="menuitem" href="' + editURL + '/' + row.id + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="menuitem " role="menuitem" href="' + copyUrl + '/' + row.id + '" data-value="' + row.id + '">' + $.i18n._('copy') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                        data-target="#deleteModal" data-instancetype="' + $.i18n._('caseSeries') + '" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(data["seriesName"]) + '">' + $.i18n._('labelDelete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#caseSeriesList tbody tr').each(function () {
                    $(this).find('td:eq(4)').attr('nowrap', 'nowrap');
                });
            }, 100)
            updateTitleForThreeRowDotElements();
            eventBindingClk();
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json);
        });


        $('#caseSeriesList').on('mouseover', 'tr', function () {
            $('.popoverMessage').popover({
                placement: 'right',
                trigger: 'hover focus',
                viewport: '#caseSeriesList',
                html: true
            });
        });
        loadTableOption('#caseSeriesList');
        return case_series_table;

    };

    var init_filter = function () {
        var filter_data = [
            {
                label: $.i18n._("app.advancedFilter.caseSeriesName"),
                type: 'text',
                name: 'seriesName',
                maxlength: 555
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
                name: 'email'
            },
            {
                label: $.i18n._("app.advancedFilter.runTimes"),
                type: 'natural-number',
                name: 'numExecutions'
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
            table_id: '#caseSeriesList',
            container_id: 'case-series-list-filter-panel',
            filter_defs: filter_data,
            column_count: 1,
            done_func: function (filter) {
                tableFilter = filter;
                advancedFilter = true;
                var dataTable = $('#caseSeriesList').DataTable();
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

        $("select[data-name=qualityChecked]").select2();
    };

    var init = function () {
        init_case_list_table();
        init_filter();
    };

    init();

    let eventBindingClk = (function () {
        $("[data-evt-clk]").on('click', function(e) {
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

