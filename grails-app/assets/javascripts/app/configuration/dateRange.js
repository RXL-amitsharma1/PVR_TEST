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
var controllerName = "";
$(function () {
     $('#dateRangeType').select2();
     $('.evaluateDateAs').select2();

    controllerName = $("#controllerName").val();

    function init() {

        initializeAsOfDate();

        if (document.getElementById("evaluateDateAs") && document.getElementById("evaluateDateAs").value === "VERSION_ASOF") {
            $('#asOfVersionDatePicker').show();
        } else {
            $('#asOfVersionDatePicker').hide();
        }

        var isFromTemplate = ($("#fromTemplate").val() === "true");
        var includeLockedVersionCheckedOrignal = $('#includeLockedVersion').prop("checked");
        if (!isFromTemplate) {
            $('#evaluateDateAsNonSubmission').select2().on("change", function (e) {
                checkForDatePickerValue(includeLockedVersionCheckedOrignal);
            }).on("select2:open", function (e) {
                var searchField = $('.select2-dropdown .select2-search__field');
                if (searchField.length) {
                    searchField[0].focus();
                }
            });

            $('#evaluateDateAsSubmissionDate').select2().on("change", function (e) {
                checkDateRangeType(includeLockedVersionCheckedOrignal);
            }).on("select2:open", function (e) {
                var searchField = $('.select2-dropdown .select2-search__field');
                if (searchField.length) {
                    searchField[0].focus();
                }
            });
        }

        var $useCaseSeries = $("#useCaseSeries");
        if ($useCaseSeries.length > 0)
            bindSelect2WithUrl($useCaseSeries, executedCaseSeriesListUrl, executedCaseSeriesItemUrl, true).on("select2:open", function (e) {
                var searchField = $('.select2-dropdown .select2-search__field');
                if (searchField.length) {
                    searchField[0].focus();
                }
            }).on("change", function (e) {
                var evaluateDateAsNonSubmission = $('#evaluateDateAsNonSubmission');
                var evaluateDateAsSubmissionDate = $('#evaluateDateAsSubmissionDate');
                if ($(this).val() != null) {
                    $(this).parent().width('90%');
                    $('div.viewCaseLink').show();
                    evaluateDateAsNonSubmission.val(evaluateDateAsNonSubmission.find('option:first-child').val()).trigger('change');
                    evaluateDateAsSubmissionDate.val(evaluateDateAsSubmissionDate.find('option:first-child').val()).trigger('change');
                    evaluateDateAsNonSubmission.prop("disabled", true);
                    evaluateDateAsSubmissionDate.prop("disabled", true);
                } else {
                    $(this).parent().width('100%');
                    $('div.viewCaseLink').hide();
                    evaluateDateAsNonSubmission.prop("disabled", isFromTemplate);
                    evaluateDateAsSubmissionDate.prop("disabled", isFromTemplate);
                }
            }).trigger('change');

        $('.viewCaseLink').on('click', function () {
            window.open(caseListUrl + '?cid=' + $('#useCaseSeries').val(), '_blank')
        });

        $(document).on('change', '#dateRangeType', function () {
            checkDateRangeType(includeLockedVersionCheckedOrignal);
        });

        var dataSourceElement = $("[name='sourceProfile.id']");

        function fetchDateRangeTypes() {
            $.ajax({
                url: fetchDateRangeTypesUrl,
                data: {
                    dataSourceId: dataSourceElement.val(),
                    userSpecificDateRangeType: (typeof userSpecificDateRange !== 'undefined')
                },
                dataType: 'json'
            })
                .done(function (result) {
                    repopulateSelectBox($("#dateRangeType"), result).on("select2:open", function (e) {
                        var searchField = $('.select2-dropdown .select2-search__field');
                        if (searchField.length) {
                            searchField[0].focus();
                        }
                    }).on("change", function (e) {
                        checkDateRangeType(includeLockedVersionCheckedOrignal);
                    }).trigger('change');
                });
        }

        function fetchEvaluateCaseDates() {
            $.ajax({
                url: fetchEvaluateCaseDatesUrl,
                data: {dataSourceId: dataSourceElement.val()},
                dataType: 'json'
            })
                .done(function (result) {
                    repopulateSelectBox($("#evaluateDateAsNonSubmission"), result).on("change", function (e) {
                        checkForDatePickerValue(includeLockedVersionCheckedOrignal);
                    }).trigger('change');
                });
        }

        function fetchEvaluateCaseDateForSubmission() {
            $.ajax({
                url: fetchEvaluateCaseDateForSubmissionUrl,
                data: {dataSourceId: dataSourceElement.val()},
                dataType: 'json'
            })
                .done(function (result) {
                    repopulateSelectBox($("#evaluateDateAsSubmissionDate"), result).on("change", function (e) {
                        checkDateRangeType(includeLockedVersionCheckedOrignal);
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

        dataSourceElement.on("change", function (e) {
            fetchDateRangeTypes();
            fetchEvaluateCaseDates();
            fetchEvaluateCaseDateForSubmission();
        }).trigger('change');

        checkDateRangeInEdit(document);
        checkDateRangeType();
        checkSuspectProduct(false);
        checkHeaderProductSelection();
    }

    if ((typeof dashboardWidget == 'undefined') || !dashboardWidget) {
        init();
    }
});

function initializeAsOfDate() {
    var asOfDate = null;

    if ($("#asOfVersionDate").val()) {
        asOfDate = $("#asOfVersionDate").val();
    }

    $('#asOfVersionDatePicker').datepicker({
        allowPastDates: true,
        date: asOfDate,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
}

function checkForDatePickerValue(includeLockedVersionCheckedVal) {
    var value = $("#evaluateDateAsNonSubmission").val();
    checkDateRangeType(includeLockedVersionCheckedVal);
    if (value && $("#evaluateDateAsNonSubmission option").length > 0) {
        if (value.toLowerCase().indexOf('as') != -1) {
            $('#asOfVersionDatePicker').show();
        } else {
            $('#asOfVersionDatePicker').hide();
        }

    } else {
        $('#asOfVersionDatePicker').hide();
    }
}

function checkDateRangeInEdit(currentDocument) {
    var component = $(currentDocument).find(".dateRange");
    _.each(component, function (dateRangeDiv, index) {

        if (index < component.length - 1) {
            dateRangeChangedAction(currentDocument, parseInt(index));
        }
    })
}

function checkDateRangeType(includeLockedVersionCheckedVal) {
    if ($('#dateRangeType option:selected').html() === DATE_RANGE_TYPE.SUBMISSION_DATE) {
        $("#evaluateDateAs").val($("#evaluateDateAsSubmissionDate").val());
        checkIncludeLockedVersions($("#evaluateDateAs").val(), includeLockedVersionCheckedVal);
        $("#evaluateDateAsDiv").hide();
        $("#evaluateDateAsSubmissionDateDiv").show();
        $('#asOfVersionDatePicker').hide();
    } else {
        $("#evaluateDateAs").val($("#evaluateDateAsNonSubmission").val());
        checkIncludeLockedVersions($("#evaluateDateAs").val(), includeLockedVersionCheckedVal);
        $("#evaluateDateAsDiv").show();
        $("#evaluateDateAsSubmissionDateDiv").hide();
    }
}

function checkIncludeLockedVersions(lockedvalue, includeLockedVersionCheckedVal) {
    if ((lockedvalue.toLowerCase().indexOf('latest') != -1) || (lockedvalue.toLowerCase().indexOf('all_') != -1)) {
        $('#includeLockedVersion').removeAttr("disabled");
        if (includeLockedVersionCheckedVal == false) {
            $('#includeLockedVersion').prop("checked", false);
        }
    } else if ((lockedvalue.toLowerCase().indexOf('per') != -1) || (lockedvalue.toLowerCase().indexOf('as') != -1)) {
        $('#includeLockedVersion').prop("checked", true)
            .attr("disabled", true);
    }
}

// Use select2 change event for IE11 in templateQueries.js
function daraRangeEnumClassOnChange(dateRangeContainer) {
    var elementId = (dateRangeContainer.id);
    var index = elementId.replace(/[^0-9\.]+/g, "");
    //$('\#templateQueries\[0\].datePickerFromDiv').datepicker('setDate', val1);
    dateRangeChangedAction(document, parseInt(index))
}

function dateRangeChangedAction(currentDocument, index) {
    var datePickerFromDiv = $(currentDocument.getElementById('templateQueries[' + index + '].datePickerFromDiv'));
    var datePickerToDiv = $(currentDocument.getElementById('templateQueries[' + index + '].datePickerToDiv'));
    var relativeDateRangeValue = $(currentDocument.getElementById('templateQueries[' + index + '].dateRangeInformationForTemplateQuery.relativeDateRangeValue'));
    var valueChanged = "";

    if (controllerName === "autoReasonOfDelay") {
        datePickerFromDiv = $(currentDocument.getElementById('queriesRCA[' + index + '].datePickerFromDiv'));
        datePickerToDiv = $(currentDocument.getElementById('queriesRCA[' + index + '].datePickerToDiv'));
        relativeDateRangeValue = $(currentDocument.getElementById('queriesRCA[' + index + '].dateRangeInformationForQueryRCA.relativeDateRangeValue'));
        valueChanged = currentDocument.getElementById('queriesRCA[' + index + '].dateRangeInformationForQueryRCA.dateRangeEnum').value;
    } else {
        valueChanged = currentDocument.getElementById('templateQueries[' + index + '].dateRangeInformationForTemplateQuery.dateRangeEnum').value;
    }

    if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
        initializeDatePickersForEdit(currentDocument, index);
        datePickerFromDiv.show();
        datePickerToDiv.show();
        relativeDateRangeValue.hide();
    } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
        datePickerFromDiv.hide();
        datePickerToDiv.hide();
        relativeDateRangeValue.hide();
    } else {
        if (_.contains(X_OPERATOR_ENUMS, valueChanged)) {
            relativeDateRangeValue.show();
        } else {
            relativeDateRangeValue.hide();
        }
        datePickerFromDiv.hide();
        datePickerToDiv.hide();
    }
}

function initializeDatePickersForEdit(currentDocument, index) {
    var dateRangeStart = null;
    var dateRangeEnd = null;
    var dateRangeStartAbsoluteId = 'templateQueries[' + index + '].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute';
    var dateRangeEndAbsoluteId = 'templateQueries[' + index + '].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute';
    var datePickerFromDivId = 'templateQueries[' + index + '].datePickerFromDiv';
    var datePickerToDivId = 'templateQueries[' + index + '].datePickerToDiv';
    if (controllerName === "autoReasonOfDelay") {
        dateRangeStartAbsoluteId = 'queriesRCA[' + index + '].dateRangeInformationForQueryRCA.dateRangeStartAbsolute';
        dateRangeEndAbsoluteId = 'queriesRCA[' + index + '].dateRangeInformationForQueryRCA.dateRangeEndAbsolute';
        datePickerFromDivId = 'queriesRCA[' + index + '].datePickerFromDiv';
        datePickerToDivId = 'queriesRCA[' + index + '].datePickerToDiv';
    }
    var dateRangeStartAbsolute = currentDocument.getElementById(dateRangeStartAbsoluteId);
    var dateRangeEndAbsolute = currentDocument.getElementById(dateRangeEndAbsoluteId);

    if (dateRangeStartAbsolute.value) {
        dateRangeStart = dateRangeStartAbsolute.value;
    }

    if (dateRangeEndAbsolute.value) {
        dateRangeEnd = dateRangeEndAbsolute.value;
    }

    $(currentDocument.getElementById(datePickerFromDivId)).datepicker({
        allowPastDates: true,
        date: dateRangeStart,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $(currentDocument.getElementById(datePickerToDivId)).datepicker({
        allowPastDates: true,
        date: dateRangeEnd,
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
