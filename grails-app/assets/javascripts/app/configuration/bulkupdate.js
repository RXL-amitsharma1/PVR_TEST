DATE_RANGE_ENUM = {
    CUSTOM: 'CUSTOM',
    CUMULATIVE: 'CUMULATIVE',
    RELATIVE: 'RELATIVE'
};
X_OPERATOR_ENUMS = {
    LAST_X_DAYS: 'LAST_X_DAYS',
    LAST_X_WEEKS: 'LAST_X_WEEKS',
    LAST_X_MONTHS: 'LAST_X_MONTHS',
    LAST_X_YEARS: 'LAST_X_YEARS',
    NEXT_X_DAYS: 'NEXT_X_DAYS',
    NEXT_X_WEEKS: 'NEXT_X_WEEKS',
    NEXT_X_MONTHS: 'NEXT_X_MONTHS',
    NEXT_X_YEARS: 'NEXT_X_YEARS'
};
var table;
$(function () {
    var tableFilter = {};
    var advancedFilter = false;
    var allTableParams = []

    $(".selectlist.timezone.timezone-select-div  .dropdown-menu").css("width", "300");
    bindSelect2WithUrl($("#reportingDestination"), reportingDestinationsUrl, null, false).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });
    $.ajax({
        type: "GET",
        url: selectTemplatesUrl,
        dataType: 'json'
    })
        .done(function (result) {
            var options = "";
            _.each(result, function (data_item) {
                options += "<option value='" + data_item.id + "'>" + replaceBracketsAndQuotes(data_item.reportName) + "</option>\n";
            });
            $("#templates").html(options);
            $("#templates").select2().val('').trigger('change');
            $("#templates").select2().on("select2:open", function (e) {
                var searchField = $('.select2-dropdown .select2-search__field');
                if (searchField.length) {
                    searchField[0].focus();
                }
            });
        });
    table = $('#rxTableBulkSheduling').DataTable({
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
        //"sPaginationType": "bootstrap",
        "stateSave": true,
        "stateDuration": -1,
        "language": {
            "search": '',
            "searchPlaceholder": $.i18n._("fieldprofile.search.label")
        },
        "customProcessing": true, //handled using processing.dt event
        "serverSide": true,
        "ajax": {
            "url": listUrl,
            "type": "POST",
            "dataSrc": "data",
            "data": function (d) {
                d.tableFilter = JSON.stringify(tableFilter);
                d.advancedFilter = advancedFilter;
                d.searchString = d.search.value;
                // d.sharedwith = $('#sharedWith').val();
                if (d.order.length > 0) {
                    d.direction = d.order[0].dir;
                    //Column header mData value extracting
                    d.sort = d.columns[d.order[0].column].data;
                }

                allTableParams = d;
            }
        },
        "aaSorting": [],
        "order": [[9, "desc"]],
        columnDefs: [
            {width: "20", targets: 0},
            {width: "50", targets: 4},
            {width: "25", targets: 8},
            {width: "100", targets: 9},
            {width: "100", targets: 10}
        ],
        "bLengthChange": true,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "pagination": true,
        "iDisplayLength": 50,

        drawCallback: function (settings) {
            showTotalPage(settings.json.recordsFiltered);
            pageDictionary($('#rxTableBulkSheduling_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);

        },
        "aoColumns": [
            {
                "sClass": "dataTableColumnCenter isTemplateCell",
                "mData": "isTemplate", "bSortable": false,
                mRender: function (data, type, row) {
                    return (data ? formStatusCell("template") : (row.status ? formStatusCell("scheduled") : formStatusCell("unscheduled")))
                }
            }, {
                "sClass": "reportNameCell",
                "mData": "reportName",
                mRender: function (data, type, row) {
                    return formReportNameCell(row.id, data, row.isPublisherReport);
                }
            }, {
                "bSortable": false,
                "sClass": "configurationTemplateCell",
                "mData": "configurationTemplate"
            },
            {
                "sClass": "productCell",
                "mData": "productSelection",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return formProductCell(row.id, row.productsJson, row.groupsJson, row.productSelection)
                }
            },
            {
                "sClass": "periodicReportTypeCell",
                "mData": "periodicReportType",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return formPeriodicReportTypeCell(row.id, row.periodicReportType, data)
                }
            },
            {
                "sClass": "dateRangeTypeCell",
                "mData": "dateRangeType",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return formDateRangeTypeCell(row.id, row.dateRangeType, data, row.dateRangeStartAbsolute, row.dateRangeEndAbsolute, row.relativeDateRangeValue);
                }
            },
            {
                "sClass": "primaryReportingDestinationCell",
                "mData": "primaryReportingDestination",
                mRender: function (data, type, row) {
                    return formPrimaryReportingDestinationCell(row.id, data);
                }
            },
            {
                "sClass": "schedulerCell",
                "bSortable": false,
                "mData": "scheduleDateJSON",
                mRender: function (data, type, row) {
                    return formSchedulerCell(row.id, row.schedulerJSON, data)
                }
            },
            {
                "sClass": "dueInDaysCell dataTableColumnCenter",
                "mData": "dueInDays",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return formDueInDaysCell(row.id, data);
                }
            },
            {
                "mData": "nextRunDate",
                "sClass": "dataTableColumnCenter forceLineWrapDate nextRunDateCell"
            },
            // {
            //     "sClass": "statusCell dataTableColumnCenter",
            //     "mData": "status",
            //     "mRender": function (data, type, row) {
            //         return formStatusCell(data);
            //     }
            // },
            {
                "mData": null,
                "sClass": "dataTableColumnCenter",
                "bSortable": false,
                "mRender": function (data, type, row) {
                    return formMenuCell(data["id"], data["status"], data["reportName"], data["isTemplate"]);
                }
            }
        ],
        initComplete: function () {
            loadTableOption('#rxTableBulkSheduling');
            removeOptionColumn();
            var topControls = $(".topControls");
            $('#rxTableBulkSheduling_wrapper').find('.dt-search').before(topControls);
            topControls.show();
        }
    }).on('draw.dt', function () {
    }).on('xhr.dt', function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json)
    });

    var filter_data = [
        {
            label: $.i18n._("app.advancedFilter.template"),
            type: 'boolean',
            name: 'isTemplate'
        }, {
            label: $.i18n._("app.advancedFilter.reportName"),
            type: 'text',
            name: 'reportName'
        }, {
            label: $.i18n._("app.advancedFilter.configurationTemplate"),
            type: 'select2-id',
            name: 'configurationTemplate',
            ajax: {
                url: selectTemplatesUrl,
                data_handler: function (data) {
                    return pvr.filter_util.build_options(data, 'id', 'reportName', true);
                },
                error_handler: function (data) {
                    console.log(data);
                }
            }
        }, {
            label: $.i18n._("app.advancedFilter.product"),
            type: 'text',
            name: 'productSelection'
        }, {
            label: $.i18n._("app.advancedFilter.aggregateReportType"),
            type: 'select2-enum',
            name: 'periodicReportType',
            data_type: 'PeriodicReportTypeEnum',
            data: periodicReportTypes
        }, {
            label: $.i18n._("app.advancedFilter.dateRangeType"),
            type: 'select2-enum',
            name: 'globalDateRangeInformation.dateRangeEnum',
            data_type: 'DateRangeEnum',
            data: dateRanges
        },
        {
            label: $.i18n._("app.advancedFilter.primaryReportingDestination"),
            type: 'text',
            name: 'primaryReportingDestination'
        },
        {
            label: $.i18n._("app.advancedFilter.dueInDays"),
            type: 'number',
            name: 'dueInDays'
        },
        {
            label: $.i18n._("app.advancedFilter.nextRunDateStart"),
            type: 'date-range',
            group: 'nextRunDate',
            group_order: 1
        },
        {
            label: $.i18n._("app.advancedFilter.nextRunDatedEnd"),
            type: 'date-range',
            group: 'nextRunDate',
            group_order: 2
        }, {
            label: $.i18n._("app.advancedFilter.status"),
            type: 'select2-manual',
            name: 'status',
            data: statuses
        }
    ];

    pvr.filter_util.construct_right_filter_panel({
        table_id: '#rxTableBulkSheduling',
        container_id: 'config-filter-panel',
        filter_defs: filter_data,
        column_count: 1,
        done_func: function (filter) {
            tableFilter = filter;
            advancedFilter = true;
            table.ajax.reload(function (data) {
            }, false).draw();
        }
    });

    $("select[data-name=isTemplate]").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $("select[data-name=configurationTemplate]").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $("select[data-name=periodicReportType]").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $("select[data-name='globalDateRangeInformation.dateRangeEnum']").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $("select[data-name=status]").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });


    //=====================edit reportName cell=============================
    $(document).on("click", "td.reportNameCell", function (e) {
        var $this = $(this);
        var id = $this.find(".reportName").attr("data-id");
        var oldVal = $this.find(".reportName").text();
        var isPublisherReport = !!$this.find("sup").text();

        var $textEditDiv = $("#reportNameEditDiv");
        showEditDiv($this, $textEditDiv, $textEditDiv.find('.newVal'));
        $textEditDiv.find('.newVal').val(oldVal).focus();
        $textEditDiv.find(".saveButton").one('click', function (e) {
            var newVal = $textEditDiv.find('.newVal').val();
            if ((newVal !== oldVal) && newVal && newVal.trim().length > 0) {
                ajaxCall(editFieldUrl, {reportName: newVal, id: id},
                    function (result) {
                        $this.html(formReportNameCell(id, newVal, isPublisherReport));
                    },
                    function (err) {
                        $this.html(formReportNameCell(id, oldVal, isPublisherReport));
                    });
            } else
                $this.html(formReportNameCell(id, oldVal, isPublisherReport));
            $(".popupBox").hide();
        });
    });

    function removeOptionColumn() {
        var tableColumns = $('#tableColumns');
        tableColumns.find("tbody tr:first").remove();
    }

    function formReportNameCell(id, text, isPublisherReport) {
        var ico = "";
        if (isPublisherReport) ico = '<sup type="' + $.i18n._('publisherReport') + '" style="font-weight: bold;">PVP</sup>';
        return "<span class='reportName' data-id='" + id + "'>" + encodeToHTML(text) + "</span>" + ico;
    }

    //=====================edit dueInDays cell=============================
    $(document).on("click", "td.dueInDaysCell", function (e) {
        var $this = $(this);
        var id = $this.find(".dueInDays").attr("data-id");
        var oldVal = $this.find(".dueInDays").text();
        var $textEditDiv = $("#daysDueEditDiv");
        showEditDiv($this, $textEditDiv, $textEditDiv.find('.newVal'));
        $textEditDiv.find('.newVal').val(oldVal).focus();

        $textEditDiv.find(".saveButton").one('click', function (e) {
            var newVal = $textEditDiv.find('.newVal').val();
            if ((newVal !== oldVal) && newVal && newVal.trim().length > 0 && !Number.isNaN(newVal)) {
                ajaxCall(editFieldUrl, {dueInDays: newVal, id: id},
                    function (result) {
                        $this.html(formDueInDaysCell(id, newVal));
                    },
                    function (err) {
                        $this.html(formDueInDaysCell(id, oldVal));
                    });
            } else
                $this.html(formDueInDaysCell(id, oldVal));
            $(".popupBox").hide();
        });
    });

    function formDueInDaysCell(id, text) {
        return "<span class='dueInDays' data-id='" + id + "'>" + text + "</span>";
    }

    //=====================edit ReportingDestination cell=============================
    $(document).on("click", "td.primaryReportingDestinationCell", function (e) {
        var $this = $(this);
        var id = $this.find(".primaryReportingDestination").attr("data-id");
        var oldVal = $this.find(".primaryReportingDestination").text();
        var $textEditDiv = $("#primaryReportingDestinationEditDiv");
        showEditDiv($this, $textEditDiv);
        $('#reportingDestination').append(new Option(oldVal,oldVal, true, true));
        $('#reportingDestination').trigger({
                type: 'select2:select',
                params: {
                    data: oldVal
                }
        });
        $('#reportingDestination').focus();
        $textEditDiv.find(".saveButton").one('click', function (e) {
            var newVal = $textEditDiv.find('#reportingDestination').val();
            if ((newVal !== oldVal) && newVal) {
                ajaxCall(editFieldUrl, {primaryReportingDestination: newVal, id: id},
                    function (result) {
                        $this.html(formPrimaryReportingDestinationCell(id, newVal));
                    },
                    function (err) {
                        $this.html(formPrimaryReportingDestinationCell(id, oldVal));
                    });
            } else
                $this.html(formPrimaryReportingDestinationCell(id, oldVal));
            $(".popupBox").hide();
        });
    });

    function formPrimaryReportingDestinationCell(id, text) {
        return "<span class='primaryReportingDestination' data-id='" + id + "'>" + text + "</span>";
    }

    //===================edit periodicReportType cell=========================
    $(document).on("click", "td.periodicReportTypeCell", function (e) {
        var $this = $(this);
        var id = $this.find(".periodicReportType").attr("data-id");
        var oldVal = $this.find(".periodicReportType").attr("data-value");
        var oldLabel = $this.find(".periodicReportType").text();
        var $textEditDiv = $("#periodicReportTypeEditDiv");
        var $select = $textEditDiv.find('#periodicReportTypeSelect');
        showEditDiv($this, $textEditDiv, $select);

        $select.val(oldVal).focus();
        $textEditDiv.find(".saveButton").one('click', function (e) {
            var newVal = $select.val();
            var newLabel = $select.find("option:selected").text();
            if ((newVal !== oldVal) && newVal) {
                ajaxCall(editFieldUrl, {periodicReportType: newVal, id: id},
                    function (result) {
                        $this.html(formPeriodicReportTypeCell(id, newVal, newLabel));
                    },
                    function (err) {
                        $this.html(formPeriodicReportTypeCell(id, oldVal, oldLabel));
                    });
            } else {
                $this.html(formPeriodicReportTypeCell(id, oldVal, oldLabel));
            }
            $(".popupBox").hide();
        });
    });

    function formPeriodicReportTypeCell(id, value, label) {
        return "<span class='periodicReportType' data-id='" + id + "' data-value='" + value + "'>" + label + "</span>";
    }

    //===================edit dateRangeType cell=========================
    $(document).on("click", "td.dateRangeTypeCell", function (e) {
        var $this = $(this);

        var id = $this.find(".dateRangeType").attr("data-id");
        var oldVal = $this.find(".dateRangeType").attr("data-value");
        var oldRelativeDateRangeValue = $this.find(".dateRangeType").attr("data-relativeDateRangeValue");
        var oldDateRangeStartAbsolute = $this.find(".dateRangeType").attr("data-dateRangeStartAbsolute");
        var oldDateRangeEndAbsolute = $this.find(".dateRangeType").attr("data-dateRangeEndAbsolute");
        var oldLabel = $this.find(".dateRangeType").text();
        var $globalDateRangeDiv = $("#dateRangeTypeEditDiv");
        var $select = $globalDateRangeDiv.find("#globalDateRangeInformation\\.dateRangeEnum").focus();
        if (oldDateRangeStartAbsolute && oldDateRangeStartAbsolute !== 'null') $globalDateRangeDiv.find("#globalDateRangeInformation\\.dateRangeStartAbsolute").val(oldDateRangeStartAbsolute).trigger('change');
        if (oldDateRangeEndAbsolute && oldDateRangeEndAbsolute !== 'null') $globalDateRangeDiv.find("#globalDateRangeInformation\\.dateRangeEndAbsolute").val(oldDateRangeEndAbsolute).trigger('change');
        if (oldRelativeDateRangeValue && oldRelativeDateRangeValue !== 'null') $globalDateRangeDiv.find("#globalDateRangeInformation\\.relativeDateRangeValue").val(oldRelativeDateRangeValue);
        showEditDiv($this, $globalDateRangeDiv, $select);
        $select.on("change", function (e) {
            globalDateRangeChangedAction($globalDateRangeDiv);
        });
        $select.val(oldVal).trigger('change');

        $globalDateRangeDiv.find(".saveButton").one('click', function (e) {
            var newVal = $select.val();
            var newLabel = $select.find("option:selected").text();
            var newRelativeDateRangeValue = parseInt($globalDateRangeDiv.find("#globalDateRangeInformation\\.relativeDateRangeValue").val());
            var newDateRangeStartAbsolute = $globalDateRangeDiv.find("#globalDateRangeInformation\\.dateRangeStartAbsolute").val();
            var newDateRangeEndAbsolute = $globalDateRangeDiv.find("#globalDateRangeInformation\\.dateRangeEndAbsolute").val();
            if (validateDateRange(newVal, newRelativeDateRangeValue, newDateRangeStartAbsolute, newDateRangeEndAbsolute)) {
                ajaxCall(editFieldUrl, {
                        id: id,
                        "globalDateRangeInformation.dateRangeEnum": newVal,
                        "globalDateRangeInformation.relativeDateRangeValue": newRelativeDateRangeValue,
                        "globalDateRangeInformation.dateRangeStartAbsolute": newDateRangeStartAbsolute,
                        "globalDateRangeInformation.dateRangeEndAbsolute": newDateRangeEndAbsolute
                    },
                    function (result) {
                        $this.html(formDateRangeTypeCell(id, newVal, newLabel, newDateRangeStartAbsolute, newDateRangeEndAbsolute, newRelativeDateRangeValue));
                    },
                    function (err) {
                        $this.html(formDateRangeTypeCell(id, oldVal, oldLabel, oldDateRangeStartAbsolute, oldDateRangeEndAbsolute, oldRelativeDateRangeValue));
                    });
            }
            $(".popupBox").hide();
        });
    });

    function formDateRangeTypeCell(id, value, label, start, end, relative) {
        var x = "", date = "",
            startDate = moment.utc(start).format(DEFAULT_DATE_DISPLAY_FORMAT),
            endDate = moment.utc(end).format(DEFAULT_DATE_DISPLAY_FORMAT),
            X_OPERATOR_ENUMS_KEYS = Object.keys(X_OPERATOR_ENUMS);

        if (X_OPERATOR_ENUMS_KEYS.includes(value)) {
            x = ", X = " + relative;
        }
        if (value.indexOf("CUSTOM") > -1) {
            date = " (" + startDate + " - " + endDate + ")";
        }
        return "<span class='dateRangeType'" +
            " data-id='" + id + "'" +
            " data-value='" + value + "'" +
            " data-relativeDateRangeValue='" + relative + "'" +
            " data-dateRangeStartAbsolute='" + start + "'" +
            " data-dateRangeEndAbsolute='" + end + "'>" +
            label + x + date + "</span>";
    }

    function validateDateRange(newVal, newRelativeDateRangeValue, newDateRangeStartAbsolute, newDateRangeEndAbsolute) {
        var X_OPERATOR_ENUMS_KEYS = Object.keys(X_OPERATOR_ENUMS);
        if (!newVal) return false;
        if (newRelativeDateRangeValue === 0) return false;
        if ((X_OPERATOR_ENUMS_KEYS.includes(newVal)) && (!newRelativeDateRangeValue || Number.isNaN(newRelativeDateRangeValue))) return false;
        if (newVal.indexOf("CUSTOM") > -1 && (!newDateRangeStartAbsolute || newDateRangeStartAbsolute === 'null' || !newDateRangeEndAbsolute || newDateRangeEndAbsolute === 'null')) return false;
        return true
    }


    //===================edit products cell=========================
    $(document).on("click", "td.productCell", function (e) {
        var $this = $(this);
        var id = $this.find(".products").attr("data-id");
        var oldProductsVal = $this.find(".products").attr("data-products-value");
        var oldGroupsVal = $this.find(".products").attr("data-groups-value");
        var oldLabel = $this.find(".products").text();
        $('#productSelection').val('');
        $('#productGroupSelection').val('');
        $("#showProductSelection").off();
        $("#productModal").modal("show");
        clearAllText(PRODUCT_DICTIONARY);
        clearDicGroupText(PRODUCT_DICTIONARY);
        $(".searchProducts").val("");
        $(".dicUlFormat ").html("");
        var productDictionaryObj = getDictionaryObject(PRODUCT_DICTIONARY);
        productDictionaryObj.resetDictionaryList();
        if (oldGroupsVal && oldGroupsVal !== 'null') {
            $('#productGroupSelection').val(oldGroupsVal);
            $.each(JSON.parse(oldGroupsVal), function (key, value) {
                productDictionaryObj.addToValuesDicGroup({name: value.name, id: value.id});
            });
            productDictionaryObj.setProductGroupText(true);
        }

        if (oldProductsVal && oldProductsVal !== 'null') {
            $('#productSelection').val(oldProductsVal);
            var loadDataSource = 'pva';
            var loadValues = JSON.parse(oldProductsVal);
            productDictionaryObj.setValues(loadValues);
            productDictionaryObj.setValuesPva(loadValues);
            productDictionaryObj.setColumnViewText(loadValues, loadDataSource);
        }
        if ((oldProductsVal && oldProductsVal !== 'null') || (oldGroupsVal && oldGroupsVal !== 'null')) {
            showDictionaryValues(document.getElementById("showProductSelection"), productDictionaryObj.getValues(), PRODUCT_DICTIONARY, productDictionaryObj.getValuesDicGroup(), productDictionaryObj.getLevels(), true);
        }
        enableDisableProductGroupButtons();

        if ($('#productGroupSelection').data('hide-dictionary-group')) {
            $("#productModal").find('.dictionary-group').hide();
        } else {
            $("#productModal").find('.dictionary-group').show();
        }

        var observer = new MutationObserver(function (mutationsList, observer) {
            var mutation = mutationsList[0];
            if (mutation.type === "childList" || mutation.type === "subtree") {
                var newProductsVal = $("#productSelection").val();
                var newGroupsVal = $("#productGroupSelection").val();
                if ((newProductsVal !== oldProductsVal) || (newGroupsVal !== oldGroupsVal)) {
                    ajaxCall(editFieldUrl, {
                            productSelection: newProductsVal,
                            productGroupSelection: newGroupsVal,
                            id: id
                        },
                        function (result) {
                            var newLabel = $("#showProductSelection").text();
                            $this.html(formProductCell(id, newProductsVal, newGroupsVal, newLabel));
                        },
                        function (err) {
                            $this.html(formProductCell(id, oldProductsVal, oldGroupsVal, oldLabel));
                        });
                } else {
                    $this.html(formProductCell(id, oldProductsVal, oldGroupsVal, oldLabel));
                }

                observer.disconnect();
            }
        });

        observer.observe($("#showProductSelection")[0], {childList: true, subtree: true});
    });

    function formProductCell(id, productsValue, groupsValue, label) {
        return "<span  class='products' data-id='" + id + "' data-products-value='" + productsValue + "' data-groups-value='" + encodeToHTML(groupsValue) + "'>" + label + "</span>";
    }

    //===================edit scheduler cell=========================
    $(document).on("click", "td.schedulerCell", function (e) {
        var $this = $(this);

        var id = $this.find(".scheduler").attr("data-id");
        var oldVal = $this.find(".scheduler").attr("data-value");
        var oldLabel = $this.find(".scheduler").text();
        var $schedulerEditDiv = $("#schedulerEditDiv");
        showEditDiv($this, $schedulerEditDiv);
        $schedulerEditDiv.find('#myDatePicker').datepicker('setRestrictedDates', []);
        $schedulerEditDiv.find('.repeat-end').find('.end-on-date').datepicker('setRestrictedDates', []);
        var schedulerInfoJSON = parseServerJson(oldVal);
        if (schedulerInfoJSON.recurrencePattern.indexOf("WEEKLY") < 0)
            highlightCurrentDayForWeeklyFrequency(userTimeZone);
        $schedulerEditDiv.scheduler('value', schedulerInfoJSON);
        $('#configSelectedTimeZone').val(schedulerInfoJSON.timeZone.name);


        $schedulerEditDiv.find(".saveButton").one('click', function (e) {
            var newVal = $schedulerEditDiv.find('#scheduleDateJSON').val();
            if ((newVal !== oldVal) && newVal) {
                ajaxCall(editFieldUrl, {scheduleDateJSON: newVal, id: id},
                    function (result) {
                        $this.html(formSchedulerCell(id, result.data.json, result.data.label));
                    },
                    function (err) {
                        $this.html(formSchedulerCell(id, oldVal, oldLabel));
                    });
            } else {
                $this.html(formSchedulerCell(id, oldVal, oldLabel));
            }
            $schedulerEditDiv.hide()
        });
    });

    function formSchedulerCell(id, value, label) {
        return "<span  class='scheduler' data-id='" + id + "' data-value='" + value + "'>" + label + "</span>";
    }

    //====== run button ======
    $(document).on("click", "a.run", function (e) {
        var $this = $(this);
        var id = $this.attr("data-id");
        if (!$this.attr("disabled"))
            ajaxCall(CONFIGURATION.runUrl, {id: id},
                function (result) {
                    var $row = $this.closest("tr");
                    $this.closest("td").html(formMenuCell(id, true, $row.find(".reportName").text(), $row.find(".isTemplateCell").html().indexOf("copy") > -1));
                    $row.find(".nextRunDateCell").text(result.data.nextRunDate);
                    $row.find(".isTemplateCell").html(formStatusCell("scheduled"));
                }, function (err) {
                });
    });

    function formStatusCell(data) {
        return (data === "template" ? "<span class='fa fa-copy fa-lg' title='" + $.i18n._("app.advancedFilter.template") + "'></span>" :
            (data === "scheduled" ? "<span class='fa fa-play-circle fa-lg green' style='color:green' title='" + $.i18n._("Scheduled") + "'></span>" :
                    "<span class='fa fa-stop-circle fa-lg' title='" + $.i18n._("notscheduled") + "'></span>"
            ));
    }

    //====== unschedule button ======
    $(document).on("click", "a.unschedule", function (e) {
        var $this = $(this);
        var id = $this.attr("data-id");
        ajaxCall(CONFIGURATION.unscheduleUrl, {id: id},
            function (result) {
                var $row = $this.closest("tr");
                $this.closest("td").html(formMenuCell(id, false, $row.find(".reportName").text(), $row.find(".isTemplateCell").html().indexOf("copy") > -1));
                $row.find(".nextRunDateCell").text('');
                $row.find(".isTemplateCell").html(formStatusCell("unscheduled"));
            }, function (err) {
            });
    });

    $(document).on("change", "#templates", function (e) {
        $("button.create").prop("disabled", !$("#templates").val());
    });
    //====== copy button ======
    $(document).on("click", "a.copy", function (e) {
        var $this = $(this);
        $this.prop("disabled", false);
        var id = $this.attr("data-id");
        createReport(id, $this);
    });

    //====== create from template button ======
    $(document).on("click", "button.create", function (e) {
        var $this = $("#templates");
        $this.prop("disabled", false);
        var id = $this.val();
        createReport(id, $this);
    });

    function createReport(id, element) {
        showLoader();
        $.ajax({
            type: "GET",
            url: CONFIGURATION.copyUrl,
            data: {id: id},
            dataType: 'json'
        })
            .done(function (result) {
                element.prop("disabled", false);
                table.row.add(result.data).draw();
                hideLoader();
            })
            .fail(function (err) {
                errorNotification((err.responseJSON.message ? err.responseJSON.message : "") +
                    (err.responseJSON.stackTrace ? "<br>" + err.responseJSON.stackTrace : ""));
                element.prop("disabled", false);
                hideLoader();
            });
    }

    function formMenuCell(id, isSheduled, reportName, isTemplate) {
        var actionButton = '<div class="btn-group dropdown" align="center">';
        if (isSheduled) {
            actionButton += '<a class="btn btn-success btn-xs unschedule"  href="javascript:void(0)" data-id="' + id + '">' + $.i18n._("app.executionStatus.unschedule.label") + '</a>';
        } else {
            actionButton += '<a class="btn btn-success btn-xs run" href="#"  data-id="' + id + '" ' + (isTemplate ? 'disabled="disabled"' : '') + '>' + $.i18n._('run') + '</a>';
        }
        actionButton += '<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + CONFIGURATION.viewUrl + '/' + id + '">' + $.i18n._('view') + '</a></li> \
                            <li role="presentation"><a role="menuitem"  href="' + CONFIGURATION.editUrl + '/' + id + '">' + $.i18n._('edit') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="javascript:void(0)" class="copy" data-id="' + id + '">' + $.i18n._('copy') + '</a></li> \
                            <li role="presentation"><a role="menuitem" class="delete"  data-id="' + id + '" data-instancename="' + replaceBracketsAndQuotes(reportName) + '" href="javascript:void(0)">' + $.i18n._('delete') + '</a></li> \
                        </ul> \
                    </div>';
        return actionButton;
    }

    //==============delete ========
    $(document).on("click", "a.delete", function (e) {
        var $this = $(this);
        var id = $this.attr("data-id");
        var name = $this.attr("data-instancename");
        var modal = $('#deleteModal');
        modal.off();
        modal.modal("show");

        var deleteAlert = $('#deleteModal #deleteDlgErrorDiv');
        deleteAlert.hide();
        $("#deleteJustification").val("");
        var deleteForAllAllowed = false;
        var instanceType = $.i18n._($.i18n._('configuration'));
        modal.find(".btn").removeAttr("disabled", "disabled");
        modal.find('#deleteForAllAllowed').hide();

        modal.find('#deleteModalLabel').text("");
        modal.find('#deleteModalLabel').text($.i18n._('modal.delete.title', instanceType));

        var nameToDeleteLabel = $.i18n._('deleteThis', instanceType);
        modal.find('#nameToDelete').text("");
        modal.find('#nameToDelete').text(nameToDeleteLabel);

        modal.find('.description').empty();
        modal.find('.description').text(name);
        modal.find("#deleteButton").off().on("click", function () {
            var confirmation = $("#deleteJustification").val();
            if (confirmation != "" && confirmation.trim().length > 0) {
                modal.modal("hide");
                ajaxCall(CONFIGURATION.deleteUrl, {id: id, confirmation: confirmation},
                    function (result) {
                        var $row = $this.closest("tr");
                        table.row($row).remove().draw();
                    }, function (err) {
                    });
            } else {
                deleteAlert.show();
            }
        });
    });

    //==============common ========
    function ajaxCall(url, data, success, error) {
        showLoader();
        $.ajax({
            type: "POST",
            url: url,
            data: data,
            dataType: 'json'
        })
            .done(function (result) {
                success(result);
                hideLoader();
            })
            .fail(function (err) {
                errorNotification((err.responseJSON.message ? err.responseJSON.message : "") +
                    (err.responseJSON.stackTrace ? "<br>" + err.responseJSON.stackTrace : ""));
                error(err);
                hideLoader();
                window.scrollTo(0, 0);
            });
    }

    $(document).mouseup(function (e) {
        var elementClasses = $(e.target).attr("class");
        if (elementClasses && elementClasses.indexOf("select2") > -1) return;
        var container = $(".popupBox");
        if (!container.is(e.target) && container.has(e.target).length === 0) {
            container.hide();
            $(".saveButton").off();
        }
    });

    $(document).on("click", ".popupBox .cancelButton", function () {
        $(".popupBox").hide();
        $(".saveButton").off();
    });

    $('#file_input').on('change', function (evt, numFiles, label) {
        $(".btn").attr('disabled', 'disabled');
        $("#file_name").val($('#file_input').get(0).files[0].name);
        if ($("#file_name").val() !== "") {
            $("#excelImportForm").trigger('submit');
        }
    });

    $(document).on("click", ".export", function () {
        // $(".btn").attr('disabled', 'disabled');
        $(this).find("input").val(JSON.stringify(tableFilter));

        $("#excelExportForm").trigger('submit');

    });
    $(document).on("click", ".bulkJustification", function () {

        $("#reportJustificationModal .save").off().one("click", function () {
            $("#reportJustificationModal").modal("hide");
            $(".bulkJustification").attr("data-value", $("#reportJustification").val());
            $.ajax({
                url: justificationAjaxUrl,
                data: {bulkJustification: $("#reportJustification").val()},
                dataType: 'html'
            })
                .done(function (result) {
                    if (result) {
                        $(".bulkJustification").find("i").removeClass("fa-comment-o").addClass("fa-commenting-o");
                    } else {
                        $(".bulkJustification").find("i").removeClass("fa-commenting-o").addClass("fa-comment-o");
                    }
                })
                .fail(function (err) {
                    var responseText = err.responseText;
                    var responseTextObj = JSON.parse(responseText);
                    alert(responseTextObj.message);
                });
        });
        $(".forReport").hide();
        $(".forBulk").show();
        $("#reportJustification").val($(".bulkJustification").attr("data-value"));
        $("#reportJustificationModal").modal("show");
    });

    $(document).on("click", "#exportBulk", function () {
        var form = $("#excelExportForm");
        form.find("input").detach();
        for (var x in allTableParams) {
            form.append(
                "<input type='hidden' name='" +
                x +
                "' value='" +
                allTableParams[x] +
                "'>"
            );
        }
        form.submit();
    });
});
