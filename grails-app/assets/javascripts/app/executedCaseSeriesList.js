var exCaseSeries = exCaseSeries || {};

exCaseSeries.caseList = (function () {

    var ex_case_series_table;

    //The function for initializing the case tables.
    var init_ex_case_list_table = function () {
        var tableFilter = {};
        var advancedFilter = false;


        //Initialize the data table
        ex_case_series_table = $("#exCaseSeriesList").DataTable({
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
            initComplete: function () {

                //Toggle the action buttons on the case series.
                actionButton('#exCaseSeriesList');
                initSharedWithFilter("exCaseSeriesList", ex_case_series_table);
                initArchiveFilter(ex_case_series_table);
                $("#exCaseSeriesList").on("click", ".favorite", function () {
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
                    d.includeArchived = $("#includeArchived").is(":checked");
                    d.tableFilter = tableFilter;
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
            "columnDefs": [
                {width: "25", targets: 0}
            ],
            "bLengthChange": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "pagination": true,
            "iDisplayLength": 10,

            drawCallback: function (settings) {
                $('.reloaderBtn').removeClass('glyphicon-refresh-animate');
                pageDictionary($('#exCaseSeriesList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                getReloader($('#exCaseSeriesList_info'), $("#exCaseSeriesList"));
                initEvtClk();
            },
            "aoColumns": [
                {
                    "data": "isFavorite",
                    "sClass": "dataTableColumnCenter",
                    "asSorting": ["asc"],
                    "render": renderFavoriteIcon
                },
                {
                    "mData": "seriesName",
                    "mRender": function (data, type, row) {
                        var arch = row.isArchived ? '<span class="glyphicon glyphicon-text-background" title="' + $.i18n._('labelArchive') + '"></span> ' : '';
                        return arch + '<span class="seriesName three-row-dot-overflow">' + encodeToHTML(row.seriesName) + '</span>';
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
                },
                {
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
                            <a class="btn btn-success btn-xs" href="' + showURL + '/' + row.id + '" data-value="' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="' + viewCasesURL + '?cid=' + row.id + '" data-value="' + row.id + '">' + $.i18n._('caseSeries.viewCases') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="#" data-toArchived="' +!row.isArchived+ '" data-url="' + toArchive + '?id=' + row.id + '" data-evt-clk=\'{"method": "exCaseSeriesCaseListConfirmArchive", "params": []}\' >' + (row.isArchived ? $.i18n._('labelUnArchive') : $.i18n._('labelArchive')) + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                        data-target="#deleteModal" data-deleteforallallowed="true" data-instancetype="' + $.i18n._('caseSeries') + '" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(data["seriesName"]) + '">' + $.i18n._('labelDelete') + '</a></li> \
                                <li role="presentation" class="stateSpecificActions"><a href="#" role="menuitem"  id="' + row.id + '" data-toggle="modal" data-target="#sharedWithModal">' + $.i18n._('labelShare') + '</a></li>\
                                <li role="presentation" class="stateSpecificActions"><a href="#" role="menuitem" id="' + row.id + '" data-toggle="modal" data-target="#emailToModal">' + $.i18n._('labelEmailTo') + '</a></li>'
                        if (row.associatedSpotfireFile && dataAnalysisRoleGranted != "false") {
                            actionButton += '<li role="presentation" class="stateSpecificActions"><a href="' + spotfireFileViewUrl + '?fileName=' + libraryRoot + "/" + encodeURI(row.associatedSpotfireFile) + '" role="menuitem" id="' + row.id + '">' + $.i18n._('view.dataAnalysis.file') + '</a></li>';
                        }
                        actionButton += '</ul></div>';
                        return actionButton;
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#exCaseSeriesList tbody tr').each(function () {
                    $(this).find('td:eq(4)').attr('nowrap', 'nowrap');
                });
            }, 100)
            updateTitleForThreeRowDotElements();
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json);
        });

        $('#exCaseSeriesList').on('mouseover', 'tr', function () {
            $('.popoverMessage').popover({
                placement: 'right',
                trigger: 'hover focus',
                viewport: '#exCaseSeriesList',
                html: true
            });
        });

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
                    label: $.i18n._("app.advancedFilter.version"),
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
                table_id: '#exCaseSeriesList',
                container_id: 'case-series-list-filter-panel',
                filter_defs: filter_data,
                column_count: 1,
                done_func: function (filter) {
                    tableFilter = _.isEmpty(filter) ? "" : JSON.stringify(filter);
                    advancedFilter = true;
                    var dataTable = $('#exCaseSeriesList').DataTable();
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

        init_filter();
        loadTableOption('#exCaseSeriesList');
        return ex_case_series_table;
    };

    var confirmArchive = function (toArchive, url) {
        var confirmationModal = $("#confirmationModal");
        confirmationModal.modal("show");
        confirmationModal.find('.modalHeader').html($.i18n._('app.archive.confirmation.title'));
        confirmationModal.find('.okButton').html($.i18n._('yes'));
        if (toArchive)
            confirmationModal.find('.confirmationMessage').html($.i18n._('app.archive.confirmation.toArchive.case'));
        else
            confirmationModal.find('.confirmationMessage').html($.i18n._('app.archive.confirmation.fromArchive.case'));
        confirmationModal.find('.okButton').off().on('click', function () {
            document.location.href = url;
        });
    };

    return {
        init_ex_case_list_table: init_ex_case_list_table,
        confirmArchive: confirmArchive
    }

})();

let initEvtClk = function () {
    $("[data-evt-clk]").on('click', function(e) {
        e.preventDefault();
        const eventData = JSON.parse($(this).attr("data-evt-clk"));
        const methodName = eventData.method;
        const params = eventData.params;

        if(methodName == 'exCaseSeriesCaseListConfirmArchive') {
            var toArchived = $(this).attr('data-toArchived');
            var url = $(this).attr('data-url')
            exCaseSeries.caseList.confirmArchive(toArchived, url);
        }
    });
}

