var SAMPLING_ERROR_TYPE = "sampling";
var table;
var coldefs = [];
var tableFilter = {};
var advancedFilter = false;
var fieldTypeMapping = {};
var selectedCases = [];
var selectedIds = [];
var optionsremovecoldatacol = [];
var errorTypeList = []
var totalFilteredRecord;
var selectAll = false;
var pageDataTable;
var columnUiStackRevertedMapping = {}
var DUEIN_DATE_FORMAT = "DD-MMM-YYYY";
$(function () {
    init_filter_data();
    for (var i = 0; i < columnUiStackMapping.length; i++) {
        for (var j = 0; j < columnUiStackMapping[i].length; j++) {
            columnUiStackRevertedMapping[columnUiStackMapping[i][j]] = i + 1;
        }
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
    const linkFilter = getUrlParameter('linkFilter');
    const hasLargeIds = getUrlParameter('hasLargeIds');

    if(hasLargeIds){
        $("#pageErrorMessage").parent().removeClass("hide");
        $("#pageErrorMessage").html($.i18n._('linkFilter.largeRecords.error'))
    }


    var init = function () {
        createColDefs();
        if (errorTypeListText) {
            errorTypeList = errorTypeListText.replace('[', '').replace(']', '').split(",");
        }
        table = $('#case-sampling-list').ExtendedDataTable({
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
                    stateDataKey: 'pvqCaseSamplingTableStateKey'
                },
                autoWidth: false,
                fixedHeader: {
                    isEnabled: true
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
                initComplete: function () {
                    $('#rxTableSpoftfireFiles tbody tr').each(function () {
                        $(this).find('td:eq(1)').attr('nowrap', 'nowrap');
                        $(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                        $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
                    });
                    initComment();
                    initActionItems();
                    initExpandHandler(table);
                    initCreateIssue();
                    initViewCriteria();
                    initViewCriteriaForManualError();
                    initReasonOfDelay();
                },
                "ajax": {
                    "url": caserecordajaxurl,
                    "type": "POST",
                    "dataSrc": function (res) {
                        totalFilteredRecord = res["recordsFiltered"];
                        recordsTotal=res.recordsTotal
                        return res["aaData"];
                    },
                    "data": function (d) {
                        d.refreshChart = false,
                            d.searchString = d.search.value;
                        if (d.order.length > 0) {
                            d.direction = d.order[0].dir;
                            d.sort = d.columns[d.order[0].column].data;
                        }
                        d.viewType = viewType;
                        if(isPvqRole &&!$('#assignedToFilter').val()){
                            $('#assignedToFilter').val(MY_GROUPS_VALUE);
                        }
                        d.assignedToFilter = $('#assignedToFilter').val();
                        if(linkFilter){
                            d.linkFilter =  JSON.stringify(linkFilter);
                        }
                        setExternalSearchCriteria(d);
                        lastTableFilter = d;
                    }
                },
                "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
                "bLengthChange": true,
                "iDisplayLength": 50,
                "pagination": true,
                drawCallback: function (settings) {
                    pageDictionary($('#case-sampling-list_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                    if (selectedIds) {
                        for (var i = 0; i < selectedIds.length; i++) {
                            $(".selectCheckbox[_id=" + selectedIds[i] + "]").attr("checked", true);
                        }
                    }
                },
                "aoColumns": coldefs
            }
        ).on('draw.dt', function () {
            updateTitleForThreeRowDotElements();
        });
        initSharedWithFilter("table", table, '210px');
        $($(".rxmain-dropdown-settings-table-enabled")[0]).hide();
        pageDataTable = table;
        loadTableOption('#case-sampling-list');
        removeOptionColumn();
        $('#case-sampling-list').on('click', '.manualAdd', function () {
            var row = $(this).attr("data-row");
            confirmAddToManual(row)
        });

        function confirmAddToManual(row) {
            var confirmationModal = $("#errorTypeModal");
            confirmationModal.modal("show");
            $("#errorTypeModal .alert").hide();
            confirmationModal.find('.okButton').off().on('click', function () {
                $(this).prop("disabled", "disabled");
                var par = JSON.parse(row.replace(/_~_/g, "'"));
                var errorType = $('#type_fld').val();
                var additionalDetails = $('#additionalDetails_fld').val();
                par.additionalDetails = additionalDetails;
                par.errorType = errorType;
                par.dataType = addToManualDataType;
                if (additionalDetails && errorType && additionalDetails.length > 0 && errorType.length > 0) {
                    $.ajax({
                        method: 'POST',
                        url: addToManualUrl,
                        data: par,
                        dataType: 'html'
                    })
                        .done(function (data) {
                            $("#errorTypeModal .alert").html($.i18n._('qualityModule.manualAdd.success')).show();
                        })
                        .fail(function (data) {
                            alert($.i18n._('Error') + " : " + data.responseText);
                        });
                } else {
                    alert($.i18n._('qualityModule.manualAdd.error'));
                }
            }).prop("disabled", false);
        }
    };
    init();
});

function removeOptionColumn() {
    var tableColumns = $('#tableColumns');
    tableColumns.find("tbody tr:first").remove();
}

function createTableForExport() {
    var metadata = {
        "sheetName": "Cases",
        "columns": []
    };
    var outData = [];
    var data = table.rows({filter: 'applied'}).data();
    if (data && data.length > 0) {
        for (var i = 1; i < 13; i++) {
            metadata.columns.push({"title": $(table.column(i).header()).html(), width: "20"})
        }
        for (var i = 0; i < data.length; i++) {
            var add = [];
            var row = [];
            for (var x in data[i].additionalData) {
                add.push(x + ":" + data[i].additionalData[x])
            }
            row.push(data[i].caseNumber);
            row.push(data[i].hcp);
            row.push(data[i].countryOfIncidence);
            row.push(data[i].dataEntrySite);
            row.push(data[i].assignedTo);
            row.push(moment(new Date(data[i].caseReceiptDate)).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT));
            row.push(data[i].caseReportType);
            row.push(data[i].age);
            row.push(moment(new Date(data[i].dob)).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT));
            row.push(data[i].patInfoGenderId);
            row.push(data[i].productNames);
            row.push(data[i].preferredTerms);
            row.push(add.join(","));
            row.push(data[i].comment);
            outData.push(row);
        }
    }
    return {metadata: metadata, data: outData}
}

/*function initCaseNumModal() {
    var data = table.columns(1, {search: 'applied'}).data()[0];
    var txt = "";
    for (var i = 0; i < data.length; i++) {
        if (i > 0) txt += ",";
        txt += data[i];
    }
    $('#caseNumberContainer').html(txt);
}*/

function setExternalSearchCriteria(d) {
    d.search = [];
    if (tableFilter && Object.keys(tableFilter).length > 0) {
        if (tableFilter["errorType"]) {
            d.search.push({name: "errorType", value: tableFilter["errorType"].value});
        }
        $.each(tableFilter, function (itmname, obj) {
            obj["dType"] = fieldTypeMapping[itmname];
        })
        d["advanceFilter"] = JSON.stringify(tableFilter);
    }

}