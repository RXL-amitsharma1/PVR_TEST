$(function () {

    if($("#configurationForm").length == 1) //create or edit page
        init();

    function init() {
        $(document).find("select[name='globalDateRangeInformationAutoROD.dateRangeEnum']").on("change", function (e) {
            globalDateRangeChangedAction(document);
        }).select2().trigger('change');
    }

});




function globalDateRangeChangedAction(currentDocument) {
    var valueChanged = $(currentDocument).find("#globalDateRangeInformationAutoROD\\.dateRangeEnum").val();
    if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
        $(currentDocument).find('#datePickerFromDiv').show();
        $(currentDocument).find('#datePickerToDiv').show();
        $(currentDocument).find('#globalDateRangeInformationAutoROD\\.relativeDateRangeValue').hide();
        initializeGlobalDatePickersForEdit(currentDocument);

    } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
        $(currentDocument).find('#datePickerFromDiv').hide();
        $(currentDocument).find('#datePickerToDiv').hide();
        $(currentDocument).find('#globalDateRangeInformationAutoROD\\.relativeDateRangeValue').hide();

    } else {
        if (_.contains(X_OPERATOR_ENUMS, valueChanged)) {
            $(currentDocument).find('#globalDateRangeInformationAutoROD\\.relativeDateRangeValue').show();

        } else {
            $(currentDocument).find('#globalDateRangeInformationAutoROD\\.relativeDateRangeValue').hide();
        }
        $(currentDocument).find('#datePickerFromDiv').hide();
        $(currentDocument).find('#datePickerToDiv').hide();
    }
}

function initializeGlobalDatePickersForEdit(currentDocument) {
    var from = null;
    var to = null;
    if ($(currentDocument).find('#globalDateRangeInformationAutoROD\\.dateRangeStartAbsolute').val()) {
        from = $(currentDocument).find('#globalDateRangeInformationAutoROD\\.dateRangeStartAbsolute').val();
    }
    if ($(currentDocument).find('#globalDateRangeInformationAutoROD\\.dateRangeEndAbsolute').val()) {
        to = $(currentDocument).find('#globalDateRangeInformationAutoROD\\.dateRangeEndAbsolute').val();
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
