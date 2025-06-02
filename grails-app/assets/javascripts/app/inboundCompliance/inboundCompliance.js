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
DATE_RANGE_TYPE = {
    SUBMISSION_DATE: $.i18n._('app.dataRangeType.submissionDate')
};

$(function () {
    var dataSourceElement = $("[name='sourceProfile.id']");

    if ($("#configurationForm").length == 1) //create or edit page
        init();
    fetchDateRangeTypes();

    function init() {
        $(document).find("select[name='globalDateRangeInbound.dateRangeEnum']").on("change", function (e) {
            globalDateRangeChangedActions(document);
        }).select2().trigger('change');
    }

    //Handling for creating new query
    $("#queryComplianceList").on("click", ".createQueryRCAButton", function () {
        var el = $(this);
        var url = el.attr("data-url");
        var message = el.attr("data-message");
        url += (url.indexOf("?") > -1) ? "&queryComplianceIndex=" : "?queryComplianceIndex=";
        url += getqueryComplianceIndex(el);
        showWarningOrSubmit(url, message);
    });

    function getqueryComplianceIndex(el) {
        var $currentQueryRCA = $(el).closest(".templateQuery-div");
        var num = 0;
        var queryComplianceList = $("#queryComplianceList").find(".templateQuery-div");
        for (var i = 0; i < queryComplianceList.length; i++) {
            if ($(queryComplianceList[i]).is($currentQueryRCA)) return num;
            if ($(queryComplianceList[i]).find("input[id$=dynamicFormEntryDeleted]").val() == "false") num++;
        }
        return num;
    }

    function showWarningOrSubmit(actionUrl, errorMessage) {
        //create new action
        var formId = $("form#configurationForm");
        if (typeof isForIcsrProfile !== 'undefined' && isForIcsrProfile == "true")
            formId = $("form#icsrProfileConfigurationForm");
        formId.attr("action", actionUrl);
        if (errorMessage != '') {
            $('#warningModal .description').text(errorMessage);
            $('#warningModal').modal('show');
            $('#warningButton').off('click').on('click', function () {
                formId.trigger('submit');
            });
        } else {
            formId.trigger('submit');
        }
    }

    //For Select2 library
    $(".select2-box").select2({
        placeholder: "-Select One-"
    });

    function fetchDateRangeTypes() {
        $.ajax({
            url: fetchDateRangeTypesUrl,
            dataType: 'json',
            data: {
                dataSourceId: dataSourceElement.val(),
                userSpecificDateRangeType: (typeof userSpecificDateRange !== 'undefined')
            }
        })
            .done(function (result) {
                repopulateSelectBox($("#dateRangeType"), result).on("change", function (e) {
                    //checkDateRangeType(false);
                }).trigger('change');
            });
    }

    function repopulateSelectBox(element, optionsData) {
        var elementValue = element.data('value');
        element.select2('destroy').empty().trigger("change");
        var optionData;
        var iter;
        for (iter = 0; iter < optionsData.length; iter++) {
            optionData = optionsData[iter];
            var option = new Option(optionData.display, optionData.name);
            if (optionData.name == elementValue) {
                option.selected = true;
            }
            element.append(option);
        }
        return element.select2();
    }

});


function globalDateRangeChangedActions(currentDocument) {
    var valueChanged = $(currentDocument).find("#globalDateRangeInbound\\.dateRangeEnum").val();
    if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
        $(currentDocument).find('#datePickerFromDiv').show();
        $(currentDocument).find('#datePickerToDiv').show();
        $(currentDocument).find('#globalDateRangeInbound\\.relativeDateRangeValue').hide();
        initializeGlobalDatePickersForEdit(currentDocument);

    } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
        $(currentDocument).find('#datePickerFromDiv').hide();
        $(currentDocument).find('#datePickerToDiv').hide();
        $(currentDocument).find('#globalDateRangeInbound\\.relativeDateRangeValue').hide();

    } else {
        if (_.contains(X_OPERATOR_ENUMS, valueChanged)) {
            $(currentDocument).find('#globalDateRangeInbound\\.relativeDateRangeValue').show();

        } else {
            $(currentDocument).find('#globalDateRangeInbound\\.relativeDateRangeValue').hide();
        }
        $(currentDocument).find('#datePickerFromDiv').hide();
        $(currentDocument).find('#datePickerToDiv').hide();
    }
}

function initializeGlobalDatePickersForEdit(currentDocument) {
    var from = null;
    var to = null;
    if ($(currentDocument).find('#globalDateRangeInbound\\.dateRangeStartAbsolute').val()) {
        from = $(currentDocument).find('#globalDateRangeInbound\\.dateRangeStartAbsolute').val();
    }
    if ($(currentDocument).find('#globalDateRangeInbound\\.dateRangeEndAbsolute').val()) {
        to = $(currentDocument).find('#globalDateRangeInbound\\.dateRangeEndAbsolute').val();
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

function checkNumberFields() {
    var validNumber = true;
    $.each($('.relativeDateRangeValue'), function () {
        if (!Number.isNaN(this.value)) {
            if (!isPositiveInteger(this.value)) {
                validNumber = false;
                $(this).parent().addClass('has-error');
                $(this).parent().find('.notValidNumberErrorMessage').show();
            } else {
                $(this).parent().removeClass('has-error');
                $(this).parent().find('.notValidNumberErrorMessage').hide();
            }
        }
    });


    $.each($('.queryExpressionValues #selectValue'), function (i, value) {

        var op = $(".toAddContainerQEV .expressionOp")[i].value;

        if (op.search('LAST_X') > -1) {
            if (!isEmpty(this.value)) {
                if (!isPositiveInteger(this.value)) {
                    validNumber = false;
                    $(value).parents().eq(1).find('.errorMessageOperator').show();
                } else {
                    $(value).parents().eq(1).find('.errorMessageOperator').hide();
                }
            }
        }
    });

    return validNumber;
}