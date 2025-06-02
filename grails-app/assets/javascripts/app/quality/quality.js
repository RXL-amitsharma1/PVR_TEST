var queryTotalCountList = commaSeparatedQueryTotalCountList.split(",");
var queryTotalCountJson = [];
var errorTypeColNo = 9;
var selectedEvent = [];
var queryNameList = [];
var queryNames = [];
var coldefs = [];
var chartItemColor = {};
var filterFromDate = "";
var filterTillDate = "";
var optionsremovecoldatacol = [];
var tableFilter = {};
var advancedFilter = false;
var fieldTypeMapping = {};
var selectedCases = [];
var selectedIds = [];
var errorTypeList;
var selectAll = false;
var totalFilteredRecord;
var isFilterApplied = false;
var previousTriage = 0;
var isAppliedTriage = false;
var originalCaseNumForSearch = '';
var pageDataTable;
var columnUiStackRevertedMapping = {}
var DUEIN_DATE_FORMAT = "DD-MMM-YYYY";
for (var i = 0; i < queryTotalCountList.length; i++) {
    queryTotalCountJson.push({
        y: parseInt(queryTotalCountList[i]),
        color: '#' + Math.random().toString(16).substr(2, 6)
    });
}
var table;
var chart;
var refreshChart = true
var emailToModalShow = false;
var nPoints = 0;
var scrollbar = false;
$(function () {
    if (columnNameList) {
        for (var i = 0; i < columnUiStackMapping.length; i++) {
            for (var j = 0; j < columnUiStackMapping[i].length; j++) {
                columnUiStackRevertedMapping[columnUiStackMapping[i][j]] = i + 1;
            }
        }
        init_filter_data();
        pageDataTable = applyDataTable('#rxTableQualityReports');
        loadTableOption('#rxTableQualityReports');
        removeOptionColumn();
        displayChart();
        //Ad hoc Alert JS
        $('#spinner').hide();
        var executedTimer;
        $("form#adHocAlert #masterCaseNum")
            .focusout(function () {
                checkCaseNumber();
            }).on('keyup', function (event) {
            clearTimeout(executedTimer);
            executedTimer = setTimeout(function () {
                if (event.which == 13) {
                    event.preventDefault();
                }
                checkCaseNumber();
            }, 2000);
        });
        $("#issueCategory").select2();
    }

    $(document).on("savedAI", ".creationButton", function () {
        table.ajax.reload();
    });

    $(document).on("updatedAI", ".update-action-item", function () {
        table.ajax.reload();
    });

    $(window).on('resize', function () {
        resetTableScroll();
    });

    $(document).on("data-clk", function (event, elem) {
        const elemClkData = JSON.parse(elem.attributes["data-evt-clk"].value)
        const methodName = elemClkData.method;
        const params = elemClkData.params;

        if (methodName == "updateAdHocField") {
            updateAdHocField();
        }
    });

    $("[data-evt-sbt]").on('submit', function() {
        const eventData = JSON.parse($(this).attr("data-evt-sbt"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'saveAdHocAlert') {
            return saveAdHocAlert();
        }
    });
});

function bindFilterApplyEvent() {
    $('#config-filter-panel').find('.apply-button').on('click', function (evt) {
        isFilterApplied = true;
    });
}

function resetTableScroll() {
    const tableBodyScroll = $('#rxTableQualityReports_wrapper').find('.dt-scroll-body');
    if (tableBodyScroll.length > 0 && tableBodyScroll.scrollLeft() > 0) {
        tableBodyScroll.scrollLeft(0);
    }
}

function createTableForExport() {
    var metadata = {
        "sheetName": "Cases",
        "columns": []
    };
    var showIgnoredErrors = $('#showIgnoredErors').is(':checked');
    var outData = [];
    var alldata = table.rows({filter: 'applied'}).data();
    var data = _.filter(alldata, function (row) {
        var ind = row[0].indexOf("id=") + 4;
        var id = row[0].substring(ind, row[0].indexOf('"', ind));
        return $("#" + id).is(":checked")
    });
    if (data && data.length > 0) {
        for (var i = 0; i < data[0].length; i++) {
            if ((i > 0 && i < 11) || (i === 12) || ((i === 13) && showIgnoredErrors)) {
                metadata.columns.push({"title": $(table.column(i).header()).html(), width: "20"})
            }
        }
        for (var i = 0; i < data.length; i++) {
            var row = []
            for (var j = 0; j < data[i].length; j++) {
                if (j == 1) {
                    var start = data[i][j].indexOf(">") + 1;
                    var end = data[i][j].indexOf("<", start);
                    row.push(data[i][j].substring(start, end));
                }
                if ((j > 1 && j < 11) || (j === 12) || ((j === 13) && showIgnoredErrors)) {
                    row.push(data[i][j]);
                }
            }
            outData.push(row);
        }
    }
    return {metadata: metadata, data: outData}
}

function checkCaseNumber() {
    var masterCaseNumberField = $('form#adHocAlert #masterCaseNum');
    var caseNum = masterCaseNumberField.val();
    if (caseNum  && caseNum.trim() !== "") {
        if (caseNum === masterCaseNumberField.attr('data-prev-value')) {
            return;
        }
        masterCaseNumberField.attr('data-prev-value', caseNum);
        caseNum = caseNum.trim();
        var encodedUrl = encodeURI(checkCaseNumUrl + '?caseNumber=' + caseNum);
        $('#spinner').show();
        $.ajax({
            url: encodedUrl,
            dataType: 'json'
        })
            .done(function (data) {
                //sometimes on loosing focus after typing one character changes in case no is not reflecting or reflecting late
                var origCaseNo = $('form#adHocAlert #masterCaseNum').val();
                if (data.caseNumber !== undefined && origCaseNo && origCaseNo.trim() == data.caseNumber) {
                    $('.errorMessageDiv').hide();
                    enableAdHocFields();
                    originalCaseNumForSearch = data.caseNumber;
                    if (originalCaseNumForSearch) {
                        $('form#adHocAlert #masterCaseNum').val(originalCaseNumForSearch.trim());
                    }
                    $('form#adHocAlert #masterCaseReceiptDate').val(data.caseReceiptDate);
                    $('form#adHocAlert #masterRptTypeId').val(data.reportType);
                    $('form#adHocAlert #masterVersionNum').val(data.masterVersionNum);
                    $('form#adHocAlert #masterCountryIdSelect').val(data.country);
                    $('form#adHocAlert #masterPrimProdName').val(data.masterPrimProdName);
                    $('form#adHocAlert #masterSiteId').val(data.masterSiteId);
                    $('form#adHocAlert #priority').val('');
                    $('form#adHocAlert #masterCaseReceiptDate').removeAttr('disabled').attr('readonly', true);
                } else {
                    disableAdHocFields();
                    $('form#adHocAlert #masterSiteId').attr('disabled', 'disabled').val('');
                    $('form#adHocAlert #priorityManualObservation').val("Assign Priority").trigger('change');
                    $('.errorMessageDiv').show();
                }
                $('#spinner').hide();
            });
    } else {
        masterCaseNumberField.attr('data-prev-value', caseNum);
        disableAdHocFields();
        $('form#adHocAlert #masterSiteId').attr('disabled', 'disabled').val('');
        $('form#adHocAlert #priorityManualObservation').val("Assign Priority").trigger('change');
        $('.errorMessageDiv').hide();
    }
}

function enableAdHocFields() {
    $('form#adHocAlert #masterCaseReceiptDate').removeAttr('disabled').attr('readonly', true);
    $('form#adHocAlert #masterRptTypeId').removeAttr('disabled').attr('readonly', true);
    $('form#adHocAlert #masterCountryIdSelect').removeAttr('disabled').attr('readonly', true);
    $('form#adHocAlert #masterSiteId').removeAttr('disabled').attr('readonly', true);
    $('form#adHocAlert #masterPrimProdName').removeAttr('disabled').attr('readonly', true);
    $('form#adHocAlert #comment').removeAttr('disabled');
    $('form#adHocAlert .primaryButton').removeAttr('disabled');
}

function disableAdHocFields() {
    $('form#adHocAlert #masterCaseReceiptDate').attr('disabled', 'disabled').val('');
    $('form#adHocAlert #masterRptTypeId').attr('disabled', 'disabled').val('');
    $('form#adHocAlert #masterCountryIdSelect').attr('disabled', 'disabled').val('');
    $('form#adHocAlert #masterSiteSelect').attr('disabled', 'disabled').val('');
    $('form#adHocAlert #masterPrimProdName').attr('disabled', 'disabled').val('');
    $('form#adHocAlert .primaryButton').attr('disabled', 'disabled');
}

function updateAdHocField() {
    $("#adAlertType").val($("#dataType").val());
    $('.errorMessageDiv').hide();
    $('#saveAlertDiv').show();
    $('#closeAlertDiv').hide();
    clearAdhocField();
    $('form#adHocAlert #priorityManualObservation').removeAttr('disabled');
    $('form#adHocAlert #masterCaseNum').removeAttr('disabled');
    $('form#adHocAlert #priorityManualObservation').val("Assign Priority").trigger('change');
    disableAdHocFields();
    $('form#adHocAlert #comment').removeAttr('disabled').attr('readonly', false);
}

function clearAdhocField() {
    $('form#adHocAlert #masterCaseNum').val('');
    $('form#adHocAlert #masterCaseReceiptDate').val('');
    $('form#adHocAlert #masterRptTypeId').val('');
    $('form#adHocAlert #masterCountryIdSelect').val('');
    $('form#adHocAlert #masterSiteId').val('');
    $('form#adHocAlert #masterPrimProdName').val('');
    $('form#adHocAlert #comment').val('');
}

function fetchQualityObservation(id, caseNum) {
    $('#adHocAlertModal').modal("show");
    $('#saveAlertDiv').hide();
    $('#closeAlertDiv').show();
    $('.errorMessageDiv').hide();
    $("#adAlertType").val($("#dataType").val());
    clearAdhocField();
    enableAdHocFields();
    $('form#adHocAlert #masterCaseNum').attr('disabled', 'disabled').val('');
    $('form#adHocAlert #priorityManualObservation').attr('disabled', 'disabled').attr('readonly', true);
    $('form#adHocAlert #comment').attr('readonly', true);
    $('form#adHocAlert #masterCaseNum').val(caseNum);
    if (caseNum && caseNum.trim() !== "") {
        caseNum = caseNum.trim();
        var dataType = $("#dataType").val();
        $('#spinner').show();
        $.ajax({
            url: fetchCriteriaForManualErrorUrl + '?caseNumber=' + caseNum + '&id=' + id + '&dataType=' + dataType + '',
            dataType: 'json'
        })
            .done(function (data) {
                if (data.masterCaseNum !== undefined) {
                    $('.errorMessageDiv').hide();
                    $('form#adHocAlert #masterCaseReceiptDate').val(data.masterCaseReceiptDate);
                    $('form#adHocAlert #masterRptTypeId').val(data.masterRptTypeId);
                    $('form#adHocAlert #masterVersionNum').val(data.masterVersionNum);
                    $('form#adHocAlert #masterCountryIdSelect').val(data.masterCountryId);
                    $('form#adHocAlert #masterPrimProdName').val(data.masterPrimProdName);
                    $('form#adHocAlert #masterSiteId').val(data.masterSiteId);
                    $('form#adHocAlert #comment').val(data.comment);
                    $('form#adHocAlert #priorityManualObservation').val(data.priority).trigger('change');
                } else {
                    disableAdHocFields();
                    $('.errorMessageDiv').show();
                }
                $('#spinner').hide();
            });
    }
}

function saveAdHocAlert() {
    var currentCaseNumInput = $('form#adHocAlert #masterCaseNum').val();
    var status = false;
    var inputModifiedAfterSearch = true;
    var errorTypeValue = $("#comment").val();
    if (/;|#|<|>|'|"/.test(errorTypeValue)) {
        //Single or double quotes not allowed in qualityIssueDescription
        $("#adHocAlertModal").find('.alert-danger').removeClass('hide');
        $("#adHocAlertModal").find('.errorMessageSpan').html($.i18n._('qualityModule.qualityObservationDetails.validation'));
        return status;
    }
    if (originalCaseNumForSearch && currentCaseNumInput && currentCaseNumInput.trim() == originalCaseNumForSearch.trim()) {
        inputModifiedAfterSearch = false;
    }
    if ($('form#adHocAlert #masterCaseNum').val() && $('form#adHocAlert #masterCaseReceiptDate').val() && $('form#adHocAlert #masterRptTypeId').val() && $('form#adHocAlert #masterCountryIdSelect').val() && !inputModifiedAfterSearch) {
        $('form#adHocAlert .primaryButton').attr('disabled', 'disabled');
        $('#adHocAlertModal').modal('hide');
        $('form#adHocAlert #masterCountryId').val($('form#adHocAlert #masterCountryIdSelect').val())
        status = true;
    }
    return status
}

$.fn.dataTableExt.afnFiltering.push(
    function (oSettings, aData, iDataIndex) {
        var fromText = $('#qualityFrom input').val();
        var toText = $('#qualityTo input').val();
        if (!fromText && !toText) {
            return true
        }
        var fromDate = moment(fromText).toDate();
        var toDate = moment(toText).toDate();
        var colDate = moment(aData[3].split(" ")[0]);
        if (fromText && fromDate instanceof Date && colDate.isBefore(fromText)) {
            return false
        }
        if (toText && toDate instanceof Date && colDate.isAfter(toText)) {
            return false
        }
        return true
    }
);

var initializeDatePickers = function (table) {
    $('#qualityFrom').datepicker({
        allowPastDates: true,
        date: null,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    }).on('click', function () {

    }).on('changed.fu.datepicker', function (evt, date) {

    }).focusout(function (evt, date) {
        var txtFromDate = $('#qualityFrom input').val();
        if (filterFromDate != txtFromDate) {
            filterFromDate = txtFromDate;
            table.draw();
        }
    }).on('keyup', function (e) {

    });

    $('#qualityTo').datepicker({
        allowPastDates: true,
        date: null,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    }).focusout(function (evt, date) {
        var txtToDate = $('#qualityTo input').val();
        if (filterTillDate != txtToDate) {
            filterTillDate = txtToDate;
            table.draw();
        }
    }).on('click', function () {
    }).on('changed.fu.datepicker', function (evt, date) {
    }).on('keyup', function (e) {
    });
};

function redrawGraph(table) {
    var data = table.columns(errorTypeColNo, {search: 'applied'}).data();
    var m = {};
    $.each(data, function (d, d2) {
        $.each(d2, function (d3, d4) {
            if (d4 in m) {
                m[d4] = m[d4] + 1;
            } else {
                m[d4] = 1;
            }
        })
    });
    var queryTotalCountJson = [];
    for (var i = 0; i < queryNameList.length; i++) {
        queryTotalCountJson.push({
            y: isNaN(parseInt(m[queryNameList[i]])) ? 0 : parseInt(m[queryNameList[i]]),
            color: '#' + Math.random().toString(16).substr(2, 6)
        });
    }
    chart.series[0].setData(queryTotalCountJson);
}

prepareChart = function (chartData) {
    var queryTotalCountJson = [];
    queryNames = [];

    if (chartData) {
        for (var i = 0; i < chartData.length; i++) {
            var color = "";
            if (chartItemColor[chartData[i].errorType]) {
                color = chartItemColor[chartData[i].errorType];
            } else {
                color = '#' + Math.random().toString(16).substr(2, 6)
                chartItemColor[chartData[i].errorType] = color;
            }

            queryTotalCountJson.push({
                y: chartData[i].count,
                color: color
            });
            queryNames.push(chartData[i].errorType);

        }

        if (queryNames.length > 19) {
            nPoints = 19;
            scrollbar = true;
        } else {
            nPoints = queryNames.length - 1;
            scrollbar = false;
        }

        refreshChart = true;

        chart.series[0].xAxis.setExtremes(0, nPoints);

        chart.series[0].xAxis.update({
            max: nPoints,
            type: 'category',
            categories: queryNames,
            fixedPosition: true,
            tickLength: 0,
            min: 0,
            scrollbar: {
                enabled: scrollbar,
            }
        });
        chart.series[0].setData(queryTotalCountJson);

    }
    refreshChart = true;
};

filterErrorType = function (selectedQuery) {
    var d = chart.series[0].xAxis.series[0].data;
    if (d) {
        for (var i = 0; i < d.length; i++) {
            if (d[i].category == selectedQuery) {
                d[i].select(true);
                d[i].selected = true;
            }
        }
    }
    selectedEvent = [];
    if (selectedQuery != "") {
        selectedEvent.push(selectedQuery);
    }
    refreshChart = false;
}
function getUrlParameter(name) {
    const url = new URL(window.location.href);
    const param = url.searchParams.get(name);
    try {
        return param ? JSON.parse(param) : null;
    } catch (e) {
        return param;
    }
}

// Set linkFilter from URL
const linkFilter = getUrlParameter('linkFilter');
const hasLargeIds = getUrlParameter('hasLargeIds');

if(hasLargeIds){
    $("#pageErrorMessage").parent().removeClass("hide");
    $("#pageErrorMessage").html($.i18n._('linkFilter.largeRecords.error'))
}

function applyDataTable(tableName) {
    createColDefs();
    table = $('#rxTableQualityReports').ExtendedDataTable({
        //dom: 'l<"case-quality-datatable-toolbar">frtlip',
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

        stateSaving: {
            isEnabled: true,
            stateDataKey: 'pvqCaseDataQualityTableStateKey'
        },
        autoWidth: false,
        fixedHeader: {
            isEnabled: true,
            topOffset: 70
        },
        colResize: {
            isEnabled: true,
            isResizable: function (column) {
                if (column.idx === 0) {
                    return false;
                }
                return true;
            }
        },

        language: { search: ''},
        "stateDuration": -1,
        "searching": false,
        "processing": true,
        "customProcessing": true,
        "serverSide": true,
        "ajax": {
            "url": caserecordajaxurl,
            "type": "POST",
            "timeout": 120000,
            "dataSrc": function (res) {
                prepareChart(res["chartData"]);
                totalFilteredRecord = res["recordsFiltered"]
                recordsTotal=res.recordsTotal
                return res["aaData"];
            },
            "data": function (d) {
                d.refreshChart = refreshChart;
                d.searchString = d.search.value;
                if (d.order.length > 0) {
                    d.direction = d.order[0].dir;
                    d.sort = d.columns[d.order[0].column].data;
                }
                setExternalSearchCriteria(d);
                d.filterByErrorType = true;
                if(isPvqRole &&!$('#assignedToFilter').val()){
                    $('#assignedToFilter').val(MY_GROUPS_VALUE);
                }
                d.assignedToFilter = $('#assignedToFilter').val();
                //If advance filter is applied or triage action is changed, ignore the selected error types on chart
                if (isAppliedAdvancedFilter || isAppliedTriage) {
                    d.search.push({name: "errorType", value: ''});
                    d.filterByErrorType = false;
                    unselectAllChartCategories();
                }
                if(linkFilter){
                    d.linkFilter =  JSON.stringify(linkFilter);
                }
                lastTableFilter = d;
            },
            "complete": function () {
                //Update both to false
                isAppliedAdvancedFilter = false;
                isAppliedTriage = false;
                //initEitPriority();
                initChangeErrorType();
            },
        },
        initComplete: function () {
            $('#rxTableQualityReports').removeClass('hide');
            $('#rxTableQualityReports').show();

            $('#rxTableQualityReports_filter input[type="search"]').val("");
            var $divToolbar = $('<div class="toolbarDiv col-xs-9"></div>');
            var $rowDiv = $('<div class="row"></div>');
            $divToolbar.append($rowDiv);

            initializeDatePickers(table);

            // $('#rxTableQualityReports tbody tr').each(function () {
            //     $(this).find('td:eq(1)').attr('nowrap', 'nowrap');
            // });
            initActionItems();
            initCreateIssue();
            initViewCriteria();
            initViewCriteriaForManualError();
            initExpandHandler(table);
            initComment();
            initAnnotation();
            initReasonOfDelay();
            //bindErrorTypeChangeEvent();
        },

        "order": {
            data: 'masterCaseNum',
            dir: 'asc'
        },
        "aoColumns": coldefs,
        "aLengthMenu": [[10, 50, 100, 200], [10, 50, 100, 200]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "pagination": true,
        drawCallback: function (settings) {
            pageDictionary($('#rxTableQualityReports_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            if (selectedIds) {
                for (var i = 0; i < selectedIds.length; i++) {
                    $(".selectCheckbox[_id=" + selectedIds[i] + "]").attr("checked", true);
                }
            }
        }
    }).on('draw.dt', function () {
        updateTitleForThreeRowDotElements();
    }).on('column-visibility.dt', function () {
        resetTableScroll();
    });

    $('#dateFilterFrom').on('changed.fu.datepicker', function () {
        //table.draw();
    });
    $('#dateFilterTo').on('changed.fu.datepicker', function () {
        //table.draw();
    });
    initSharedWithFilter("table",table, '210px');
    $($(".rxmain-dropdown-settings-table-enabled")[0]).hide();
    return table;
}

function unselectAllChartCategories() {
    if (selectedEvent.length > 0) {
        var itms = chart.series && chart.series[0] && chart.series[0].data ? chart.series[0].data : undefined;
        if (itms) {
            $.each(itms, function (index, itm) {
                if (itm.selected) {
                    itm.select(false);
                }
            })
        }
    }
    selectedEvent = [];
}

function setExternalSearchCriteria(d) {
    var fromText = $('#qualityFrom input').val();
    var toText = $('#qualityTo input').val();
    d.search = [];
    if (fromText && Date.parse(fromText)) {
        d.search.push({name: "receiptDateFrom", value: fromText});
    }
    if (toText && Date.parse(toText)) {
        d.search.push({name: "receiptDateTo", value: toText});
    }

    if (tableFilter && Object.keys(tableFilter).length > 0) {
        $.each(tableFilter, function (itmname, obj) {
            obj["dType"] = fieldTypeMapping[itmname];
        });
        d["advanceFilter"] = JSON.stringify(tableFilter);
    }
    /*
      * If filter is applied,put it in search params,
      *
     */
    if (selectedEvent.length > 0) {
        d.search.push({name: "errorType", value: selectedEvent.join("/,")});
    }
}

function displayChart() {
    if ($('#qualityChart').is(":visible")) {
        Highcharts.seriesTypes.column.prototype.getExtremesFromAll = true;
        chart = new Highcharts.Chart({
            chart: {
                renderTo: 'qualityChart',
                type: 'column',
                plotBorderWidth: 1,
                height: 350,
                zoomType: 'y',
                events: {
                    load: function (event) {
                        setTimeout(function () {
                            window.dispatchEvent(new Event('resize'));
                        }, 100);
                    },
                    click: function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        if (!e.point && e.target.innerHTML != "Reset zoom") {
                            var xseries = {};
                            xseries.category = e.ctrlKey ? "" : e.yAxis[0].axis.series[0].data[Math.round(e.xAxis[0].value)].category;
                            xseries.series = this.series[0];
                            let point = e.yAxis[0].axis.series[0].data[Math.round(e.xAxis[0].value)]
                            point.select(!point.selected, e.ctrlKey)
                            selectChartItemsFilter(xseries, e.ctrlKey);
                            table.draw();
                        }
                    }
                }
            },
            exporting: {
                enabled: true
            },
            credits: {
                enabled: false
            },
            title: {
                text: '',
                align: 'left'
            },
            subtitle: {
                text: ''
            },
            xAxis: {
                categories: queryNames,
                tickLength: 0,
                min: 0,
                crosshair: true
                /*scrollbar: {
                    enabled: true
                }*/
            },
            yAxis: {
                min: 0,
                title: {
                    text: chartLabelYAxis
                },
                lineWidth: 1,
                crosshair: true
            },
            /*scrollbar: {
                enabled: true
            },*/
            plotOptions: {
                series: {
                    allowPointSelect: true,
                    point: {
                        events: {
                            click: function (event) {
                                selectChartItemsFilter(this, event.ctrlKey);
                                table.draw();
                            }
                        }
                    },
                    states: {
                        inactive: {
                            enabled: false
                        }
                    }
                },
                bar: {
                    stacking: 'normal',
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        formatter: function () {
                            return this.y;
                        }
                    }
                }
            },
            tooltip: {
                borderRadius: 10,
                borderWidth: 2,
                shadow: true,
                formatter: function () {
                    var s = [];
                    var current = this.point.index;
                    $.each(this.series.chart.series, function (i, series) {
                        s.push('<span style="fill:' + series.color + ';font-weight:bold;">' + series.name + ' </span><span style="fill: rgb(0, 0, 0); font-weight: bold;"/>: ' +
                            series.processedYData[current] + '<span>');
                    });
                    return '<span style="font-weight: bold;">' + this.x + '</span>' + '<br/>' + s.join('<br/>')
                }
            },
            series: [{
                dataSorting: {
                    enabled: true
                },
                showInLegend: false,
                name: 'Case Count',
                color: '#333333',
                data: queryTotalCountJson

            }]
        });
    }
}

selectChartItemsFilter = function (xseries, ctrlKey) {
    isFilterApplied = false;
    if (!ctrlKey) {
        //If clicked on any chart category and there is not any category already clicked on
        var sameCategorySelected = false;
        if (selectedEvent.length == 0) {
            selectedEvent.push(xseries.category);
        } else {
            //If already any chart category is selected and now same category is clicked on
            if (selectedEvent.length == 1) {
                if (selectedEvent[0] == xseries.category) {
                    selectedEvent = [];
                    sameCategorySelected = true;
                }
            } else {
                for (var index = 0; index < selectedEvent.length; index++) {
                    //If already some chart categories are selected and now any one of them is again clicked on
                    if (selectedEvent[index] == xseries.category) {
                        sameCategorySelected = true;
                    }
                }
            }
            //If different category selected , make it selected, unselect all previously selected categories
            if (!sameCategorySelected) {
                selectedEvent = [];
                selectedEvent.push(xseries.category);
            } else {
                selectedEvent = [];
            }
        }
    } else {
        selectedEvent = [];
        if (xseries.series.data) {
            var clickedCatSelected = false;
            for (var i = 0; i < xseries.series.data.length; i++) {
                if (xseries.series.data[i].selected) {
                    if (xseries.series.data[i].category == xseries.category) {
                        clickedCatSelected = true
                    } else {
                        selectedEvent.push(xseries.series.data[i].category)
                    }
                }
            }
            if (!clickedCatSelected) {
                selectedEvent.push(xseries.category);
            }
        }
    }
    refreshChart = false;
};
