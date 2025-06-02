$(function () {

    if($("#configurationForm").length == 1) //create or edit page
        init();

    function init() {
        $(document).find("select[name='globalDateRangeInformation.dateRangeEnum']").on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        }).on("change", function (e) {
            globalDateRangeChangedAction(document);
        }).select2().trigger('change');

        bindQuerySelect2($("#globalQuery")).on("select2:select select2:unselect", function (e) {
            selectGlobalQueryOnChange(this);
        }).on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });

        bindSelect2WithUrl($("input[name='referenceProfile']"), referenceProfileListUrl, referenceProfileTextUrl, true).on("change", function (e) {
            var data = $(this).select2('data');
            if (data) {
                $('select.recipientOrg').val(data.recipientId).trigger('change');
                $('select.senderOrg').val(data.senderId).trigger('change');
            }
        });

        $(".globalQueryWrapper").find(".expressionField,.expressionOp,.expressionValueSelect").select2();
        $(document).find("select[name='periodReportType']").select2();
        // Add Report Name autofilling for new reports only (without report name)
        if (!$("input[name='reportName']").val()) {
            $(document).on('click', '.addAllProducts', function () {
                autoFillReportName();
            });

            $("#productSelection").on('change', function () {
                autoFillReportName();
            });

            $("select[name='periodicReportType']").on('change', function () {
                autoFillReportName();
            });

            $("input[name='reportName']").on('change', function () {
                $(this).data("changed", true);
            });

            autoFillReportName();
        }

        $('select.recipientOrg, select.senderOrg').select2({
            placeholder: $.i18n._('selectOne'),
            allowClear: true
        }).on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });
    }

    $("#generateCaseSeries").on('click', function () {
        showGenerateDraftCheckbox()
    });

    function showGenerateDraftCheckbox() {
        if ($("#generateCaseSeries").is(':checked'))
            $(".generateDraft").show();
        else
            $(".generateDraft").hide();
    }

    showGenerateDraftCheckbox();
});


function selectGlobalQueryOnChange(selectContainer) {
    var queryContainer = $(".globalQueryWrapper");
    var expressionValues = getExpressionValues(queryContainer);
    var queryWrapperRow = getGlobalQueryWrapperRow(selectContainer);
    $(expressionValues).empty();
    if (getAJAXCount() == -1) {
        if($(selectContainer).val() != '') {
            $(queryWrapperRow).find('.queryViewButton').attr('href', queryViewUrl+'/'+$(selectContainer).val());
            $(queryWrapperRow).find('.queryViewButton').removeClass('hide');
        } else {
            $(queryWrapperRow).find('.queryViewButton').addClass('hide');
        }
        getBlankValuesForQueryAJAX($(selectContainer).val(), expressionValues, '');
        getCustomSQLValuesForQueryAJAX($(selectContainer).val(), expressionValues, '');
        getBlankValuesForQuerySetAJAX($(selectContainer).val(), expressionValues, '');
    } else {
        $(selectContainer).val(null).trigger('change');
    }
}


function globalDateRangeChangedAction(currentDocument) {
    var valueChanged = $(currentDocument).find("#globalDateRangeInformation\\.dateRangeEnum").val();
    if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
        $(currentDocument).find('#datePickerFromDiv').show();
        $(currentDocument).find('#datePickerToDiv').show();
        $(currentDocument).find('#globalDateRangeInformation\\.relativeDateRangeValue').hide();
        initializeGlobalDatePickersForEdit(currentDocument);

    } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
        $(currentDocument).find('#datePickerFromDiv').hide();
        $(currentDocument).find('#datePickerToDiv').hide();
        $(currentDocument).find('#globalDateRangeInformation\\.relativeDateRangeValue').hide();

    } else {
        if (_.contains(X_OPERATOR_ENUMS, valueChanged)) {
            $(currentDocument).find('#globalDateRangeInformation\\.relativeDateRangeValue').show();

        } else {
            $(currentDocument).find('#globalDateRangeInformation\\.relativeDateRangeValue').hide();
        }
        $(currentDocument).find('#datePickerFromDiv').hide();
        $(currentDocument).find('#datePickerToDiv').hide();
    }
}

function initializeGlobalDatePickersForEdit(currentDocument) {
    var from = null;
    var to = null;
    if ($(currentDocument).find('#globalDateRangeInformation\\.dateRangeStartAbsolute').val()) {
        from = $(currentDocument).find('#globalDateRangeInformation\\.dateRangeStartAbsolute').val();
    }
    if ($(currentDocument).find('#globalDateRangeInformation\\.dateRangeEndAbsolute').val()) {
        to = $(currentDocument).find('#globalDateRangeInformation\\.dateRangeEndAbsolute').val();
    }

    $($(currentDocument).find('#datePickerFromDiv')).datepicker({
        allowPastDates: true,
        date: from,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $($(currentDocument).find('#datePickerToDiv')).datepicker({
        allowPastDates: true,
        date: to,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
}

function autoFillReportName() {

        // var reportNameLength = 200;
    // var d = new Date();
    var n = Math.round(Math.random()*1000000000);
        var reportName = 'RXLOGIX_' + n;
        // alert(reportName);

        $("input[name='reportName']").val(reportName);

}




