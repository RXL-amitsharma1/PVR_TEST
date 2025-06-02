$(function() {

    $(document).find("select[name='caseSeriesDateRangeInformation.dateRangeEnum']").on("change", function (e) {
        globalDateRangeChangedAction(document);
    }).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    }).select2().trigger('change');

    bindQuerySelect2($("#globalQuery")).on("change", function (e) {
        selectGlobalQueryOnChange(this);
    }).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $(document).find("#dateRangeType").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $(".globalQueryWrapper").find(".expressionField,.expressionOp,.expressionValueSelect").select2();

    $('.datepicker.expressionValueDate').datepicker({
        allowPastDates: true,
        textAllowed: true,
        momentConfig: {
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
});

function bindQuerySelect2(selector) {
    return bindSelect2WithUrl(selector, querySearchUrl, queryNameUrl, true);
}

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
    }
}

function globalDateRangeChangedAction(currentDocument) {
    var valueChanged = $(currentDocument).find("#caseSeriesDateRangeInformation\\.dateRangeEnum").val();
    if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
        initializeGlobalDatePickersForEdit(currentDocument);
        $(currentDocument).find('#datePickerFromDiv').show();
        $(currentDocument).find('#datePickerToDiv').show();
        $(currentDocument).find('#caseSeriesDateRangeInformation\\.relativeDateRangeValue').hide();

    } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
        $(currentDocument).find('#datePickerFromDiv').hide();
        $(currentDocument).find('#datePickerToDiv').hide();
        $(currentDocument).find('#caseSeriesDateRangeInformation\\.relativeDateRangeValue').hide();

    } else {
        if (_.contains(X_OPERATOR_ENUMS, valueChanged)) {
            $(currentDocument).find('#caseSeriesDateRangeInformation\\.relativeDateRangeValue').show();

        } else {
            $(currentDocument).find('#caseSeriesDateRangeInformation\\.relativeDateRangeValue').hide();
        }
        $(currentDocument).find('#datePickerFromDiv').hide();
        $(currentDocument).find('#datePickerToDiv').hide();
    }
}

//TODO will refactor and make reuseable code fpr periodic as well
function initializeGlobalDatePickersForEdit(currentDocument) {
    var from = null;
    var to = null;

    if ($(currentDocument).find('#caseSeriesDateRangeInformation\\.dateRangeStartAbsolute').val()) {
        from = $(currentDocument).find('#caseSeriesDateRangeInformation\\.dateRangeStartAbsolute').val();
    }
    if ($(currentDocument).find('#caseSeriesDateRangeInformation\\.dateRangeEndAbsolute').val()) {
        to = $(currentDocument).find('#caseSeriesDateRangeInformation\\.dateRangeEndAbsolute').val();
    }

    $(currentDocument.getElementById('datePickerFromDiv')).datepicker({
        allowPastDates: true,
        date: from,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $(currentDocument.getElementById('datePickerToDiv')).datepicker({
        allowPastDates: true,
        date: to,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

}
