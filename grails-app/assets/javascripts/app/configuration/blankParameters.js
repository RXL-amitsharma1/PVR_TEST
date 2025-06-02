// AJAX Implementation
// These fields are used to store AJAX
var AJAXKeywordList;
var AJAXValueSampleList = [];
var AJAXOperatorStringList;
var AJAXOperatorNumList;
var AJAXOperatorDateList;
var AJAXOperatorValuelessList;
// We have 7 AJAX calls, so we update values after the 6th is complete
var AJAXFinished = 3;
var AJAXCount = 0;
// ---------------------------END AJAX
// CONSTANTS
var RF_TYPE_TEXT = 'text';
var RF_TYPE_STRING = 'string';
var RF_TYPE_DATE = 'date';
var RF_TYPE_NUMBER = 'number';
var RF_TYPE_PART_DATE = 'partialDate';
var RF_TYPE_AUTOCOMPLETE = 'autocomplete';
var RF_TYPE_NOCACHE_DROP_DOWN = 'nocachedropdown';

var EDITOR_TYPE_TEXT = 0;
var EDITOR_TYPE_DATE = 1;
var EDITOR_TYPE_SELECT = 2;
var EDITOR_TYPE_NONE = 3;
var EDITOR_TYPE_AUTOCOMPLETE = 4;
var EDITOR_TYPE_NONECACHE_SELECT = 5;
var DIC_EVENT = "EVENT";
var DIC_PRODUCT = "PRODUCT";
var DIC_STUDY = "STUDY";
var valueOfSelectedFields = "";

// DATE OPERATOR CONSTANTS - these are the names, not values of the QueryOperator enum.
var LAST_X_DATE_OPERATORS = ['LAST_X_DAYS', 'LAST_X_WEEKS', 'LAST_X_MONTHS', 'LAST_X_YEARS'];
var NEXT_X_DATE_OPERATORS = ['NEXT_X_DAYS', 'NEXT_X_WEEKS', 'NEXT_X_MONTHS', 'NEXT_X_YEARS'];
var RELATIVE_DATE_OPERATORS = ['YESTERDAY', 'LAST_WEEK', 'LAST_MONTH', 'LAST_YEAR', 'TOMORROW', 'NEXT_WEEK', 'NEXT_MONTH', 'NEXT_YEAR'];
var IS_EMPTY_OPERATORS = ['IS_EMPTY', 'IS_NOT_EMPTY'];
var EQUALS_OPERATORS = ['EQUALS', 'NOT_EQUAL'];
var CONTAINS_OPERATORS = ['CONTAINS', 'DOES_NOT_CONTAIN', 'ADVANCE_CONTAINS'];

$(function () {

    $.each($('.doneLoading'), function () {
        $(this).hide();
    });
    $.each($('.loading'), function () {
        $(this).show();
    });
    $('#selectDate').datepicker({
        allowPastDates: true,
        textAllowed: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });


    $.each($('.customDatePicker'), function () {
        $(this).datepicker({
            allowPastDates: true,
            textAllowed: true,
            date: $(this).find(".reassessDate").val(),
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });
    });

    $.each($('.templtDatePicker'), function () {
        $(this).datepicker({
            allowPastDates: true,
            textAllowed: true,
            date: $(this).find(".templtReassessDate").val(),
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });
    });

    $('.errorMessageOperator').hide();
    getAJAXValues();
    // $('#selectOperator').select2('destroy');

    if ($('#blankValuesJSON').val() != '') {
    }
    //Fixed for Update button visible before ajax call completes.
    $(document).ajaxStop(function () {
        //Taken from https://stackoverflow.com/questions/3709597/wait-until-all-jquery-ajax-requests-are-done
        if ($.active == 0) {
            $('.report-edit-button').removeClass('hide');
        }
    });

    $(document).on('mouseover', ".iPopover", function () {
        var popoverContainer = $('.popover-content');
        var content = popoverContainer.html();
        if (content && content.length > 0) {
            content = decodeFromHTML(content);
            popoverContainer.html(content);
        }
    });

});

function getFieldType(field) {
    var type = '';
    if (field.data('isnoncacheselectable')) {
        type = RF_TYPE_NOCACHE_DROP_DOWN;
    } else if (field.data('isautocomplete')) {
        type = RF_TYPE_AUTOCOMPLETE;
    } else {
        switch (field.data('datatype')) {
            case 'java.lang.String':
                if (field.data('istext')) {
                    type = RF_TYPE_TEXT;
                } else {
                    type = RF_TYPE_STRING;
                }
                break;
            case 'java.util.Date':
                type = RF_TYPE_DATE;
                break;
            case 'java.lang.Number':
                type = RF_TYPE_NUMBER;
                break;
            case 'com.rxlogix.config.PartialDate':
                type = RF_TYPE_PART_DATE;
                break;
        }
    }
    return type;
}

function showHideSpecialFields() {
    if (typeof configurationPOIInputsParamsUrl != 'undefined') {
        $.ajax({
            type: "GET",
            url: configurationPOIInputsParamsUrl,
            dataType: 'json'
        })
            .done(function (result) {
                if (result.data) {
                    showPOIHideValue(result.data);
                } else {
                    showPOIHideValue(new Array());
                }
            });
    } else {
        showPOIHideValue(new Array());
    }
}

function showPOIHideValue(specialValues) {
    $.each($(".globalQueryWrapper input[name$='.specialKeyValue']"), function (index, obj) {
        if ($(obj).val() !== "") {
            if ($.inArray($(obj).val(), specialValues) !== -1) {
                $(obj).closest(".toAddContainerQEV").hide();
            } else {
                $(obj).closest(".toAddContainerQEV").show();
                specialValues.push($(obj).val());
            }
        }
    });
    $.each($(".templateQuery-div:visible input[name$='.specialKeyValue']"), function (index, obj) {
        if ($(obj).val() !== "") {
            if ($.inArray($(obj).val(), specialValues) !== -1) {
                $(obj).closest(".toAddContainerQEV").hide();
            } else {
                $(obj).closest(".toAddContainerQEV").show();
                specialValues.push($(obj).val());
            }
        }
    });
    $(".templateQuery-div:visible input[name^='poiInput['].inputPOIKey").each(function (index) {
        if ($.inArray($(this).val(), specialValues) !== -1) {
            $(this).closest(".poiInputValueContainer").hide();
        } else {
            $(this).closest(".poiInputValueContainer").show();
            specialValues.push($(this).val());
        }
    });
}

function getAJAXValues() {
    getDateOperatorsAJAX();
    getNumOperatorsAJAX();
    getStringOperatorsAJAX();
    getValuelessOperatorsAJAX();
}

function getDateOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: dateOperatorsUrl,
        dataType: 'json'
    })
        .done(function (result) {
            AJAXOperatorDateList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            } else {
                AJAXCount++;
            }
        });
}

function getNumOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: numOperatorsUrl,
        dataType: 'json'
    })
        .done(function (result) {
            AJAXOperatorNumList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            } else {
                AJAXCount++;
            }
        });
}

function getStringOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: stringOperatorsUrl,
        dataType: 'json'
    })
        .done(function (result) {
            AJAXOperatorStringList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            } else {
                AJAXCount++;
            }
        });
}

function getValuelessOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: valuelessOperatorsUrl,
        dataType: 'json'
    })
        .done(function (result) {
            AJAXOperatorValuelessList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            } else {
                AJAXCount++;
            }
        });
}

// On success, call these update methods to fill in UI values
function updateAJAXFields(container) {
    var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find(":selected"));
    var op = getOperatorFromExpression(container);
    $(op).empty();

    switch (selectedFieldType) {
        case RF_TYPE_DATE:
            $.each(AJAXOperatorDateList, function (display, value) {
                $(op).append($("<option></option>").attr("value", this.value).text(this.display));
            });
            break;
        case RF_TYPE_NUMBER:
            $.each(AJAXOperatorNumList, function (display, value) {
                $(op).append($("<option></option>").attr("value", this.value).text(this.display));
            });
            break;
        case RF_TYPE_NOCACHE_DROP_DOWN: // NonCacheSelect uses same operators as string
        case RF_TYPE_AUTOCOMPLETE: // Autocomplete uses same operators as string
        case RF_TYPE_STRING: // string and text
            $.each(AJAXOperatorStringList, function (display, value) {
                $(op).append($("<option></option>").attr("value", this.value).text(this.display));
            });
            break;
        case RF_TYPE_PART_DATE:
            $.each(AJAXOperatorDateList, function (display, value) {
                $(op).append($("<option></option>").attr("value", this.value).text(this.display));
            });
            break;
    }
}

function updateAJAXOperators(container, value) {
    var field = $(getFieldFromExpression(container)).select2("val");
    var operator = $(getOperatorFromExpression(container))[0].value;
    var isFromCopyPaste = getIsFromCopyPasteValue(container);
    var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find(":selected"));
    // var expression = container.parentElement

    if (_.contains(IS_EMPTY_OPERATORS, operator)) {
        // if (expression.expressionIndex != null) {
        //     backboneExpressions.at(expression.expressionIndex).set("value", value);
        // }
        showHideValue(EDITOR_TYPE_NONE, container);
    } else if (selectedFieldType == RF_TYPE_DATE) {
        if (_.contains(LAST_X_DATE_OPERATORS, operator) || _.contains(NEXT_X_DATE_OPERATORS, operator)) {
            showHideValue(EDITOR_TYPE_TEXT, container);
        } else if (_.contains(RELATIVE_DATE_OPERATORS, operator)) {
            // if (expression.expressionIndex != null) {
            //     backboneExpressions.at(expression.expressionIndex).set("value", value);
            // }
            showHideValue(EDITOR_TYPE_NONE, container);
        } else {
            showHideValue(EDITOR_TYPE_DATE, container);
        }
    } else if (selectedFieldType == RF_TYPE_STRING) {
        if (isFromCopyPaste) {
            showHideValue(EDITOR_TYPE_TEXT, container)
        } else if ((_.contains(EQUALS_OPERATORS, operator)) && AJAXValueSampleList[field] && AJAXValueSampleList[field].length > 0) {
            showHideValue(EDITOR_TYPE_SELECT, container);
        } else {
            showHideValue(EDITOR_TYPE_TEXT, container);
        }
    } else if (selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN) {
        if (isFromCopyPaste) {
            showHideValue(EDITOR_TYPE_TEXT, container)
        } else if ((_.contains(EQUALS_OPERATORS, operator))) {
            showHideValue(EDITOR_TYPE_NONECACHE_SELECT, container);
        } else {
            showHideValue(EDITOR_TYPE_TEXT, container);
        }
    } else if (selectedFieldType == RF_TYPE_AUTOCOMPLETE) {
        if (isFromCopyPaste) {
            showHideValue(EDITOR_TYPE_TEXT, container)
        } else if ((_.contains(EQUALS_OPERATORS, operator))) {
            showHideValue(EDITOR_TYPE_AUTOCOMPLETE, container);
        } else {
            showHideValue(EDITOR_TYPE_TEXT, container);
        }
    } else if (selectedFieldType == RF_TYPE_PART_DATE) {
        if (_.contains(RELATIVE_DATE_OPERATORS, operator)) {
            // if (expression.expressionIndex != null) {
            //     backboneExpressions.at(expression.expressionIndex).set("value", value);
            // }
            showHideValue(EDITOR_TYPE_NONE, container);
        } else {
            showHideValue(EDITOR_TYPE_TEXT, container);
        }
    } else {
        showHideValue(EDITOR_TYPE_TEXT, container);
    }
}

// only execute this after operator values have been assigned
function updateAJAXValues(container) {
    var field = $(getFieldFromExpression(container)).select2("val");
    var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find("[selected]"));
    var selectValue = getValueSelectFromExpression(container);
    var isFromCopyPaste = getIsFromCopyPasteValue(container);
    var operator = $(getOperatorFromExpression(container))[0].value;
    $(selectValue).empty();
    if (isFromCopyPaste || _.contains(CONTAINS_OPERATORS, operator)) {
        showHideValue(EDITOR_TYPE_TEXT, container);
    } else if (selectedFieldType != RF_TYPE_DATE) {
        if (selectedFieldType == RF_TYPE_AUTOCOMPLETE) {
            showHideValue(EDITOR_TYPE_AUTOCOMPLETE, container);
        } else if (selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN) {
            showHideValue(EDITOR_TYPE_NONECACHE_SELECT, container);
        } else {

            if ((_.contains(EQUALS_OPERATORS, operator))) {
                loadValues(field, function (values) {
                    if (values && values.length !== 0) {
                        $.each(values, function () {
                            $(selectValue).append($("<option></option>").attr("value", this).text(this));
                        });
                        showHideValue(EDITOR_TYPE_SELECT, container);
                    } else {
                        if (getFieldDictionaryFromExpression(container) === DIC_EVENT) {
                            getEventSearchIconFromExpression(container).show();
                        }
                        showHideValue(EDITOR_TYPE_TEXT, container);
                    }
                });
            } else {
                if (getFieldDictionaryFromExpression(container) === DIC_EVENT) {
                    getEventSearchIconFromExpression(container).show();
                }
                showHideValue(EDITOR_TYPE_TEXT, container);
            }
        }
    }
}

function loadValues(field, callback) {
    $.ajax({
        type: "GET",
        url: possibleValuesUrl,
        async: false,
        data: {
            lang: userLocale,
            field: field
        },
        dataType: 'json'
    })
        .done(function (result) {
            AJAXValueSampleList[field] = result;
            callback(result);
        })
        .fail(function (err) {
            console.log(err);
            alert('errr');
        });
}


// need to grab and set our next operators and values from field values. This should only run once on page load
function updateInitialAJAXLists() {
    $.each($('#templateQueriesContainer').find('.expression'), function () {
        var container = $(this).find('.toAddContainerQEV')[0];
        updateAJAXFields(container);
        updateAJAXValues(container);
        updateAJAXOperators(container);
    });

    //TODO  need to check after merge Added for global Query wrapper and also need to make re-usable code
    var promises = [];
    $.each($('.globalQueryWrapper'), function () {
        var templateQueryNamePrefix = "";
        var $selectedField;

        var count0 = 0;

        $.each($(this).find('.queryExpressionValues .queryBlankContainer'), function () {
            // For normal query blanks
            var it = this;
            if ($(it).hasClass('toAddContainerQEV')) {
                $(it).removeAttr('id');
                $(getFieldFromExpression(it)).attr('readonly', true).trigger('change');

                promises.push(setFieldSelect($(getFieldFromExpression(it)), it.querySelector('input.qevReportField').value, count0, null, function (count) {
                    initFieldDescriptionPopup(getFieldFromExpression(it));

                    updateAJAXFields(it);
                    var operator = it.querySelector('input.qevOperator').value;
                    $(getOperatorFromExpression(it)).val(it.querySelector('input.qevOperator').value).trigger('change');
                    updateAJAXOperators(it, it.querySelector('input.qevOperator').value);
                    updateAJAXValues(it);
                    $(getOperatorFromExpression(it)).attr('readonly', true).trigger('change');

                    $(getValueDateFromExpression(it)).datepicker({
                        textAllowed: true,
                        allowPastDates: true,
                        momentConfig: {
                            culture: userLocale,
                            format: DEFAULT_DATE_DISPLAY_FORMAT
                        }
                    });
                    $(getFieldFromExpression(it)).attr('name', 'qev[' + count + '].field');
                    $(getOperatorFromExpression(it)).attr('name', 'qev[' + count + '].operator');
                    $(getKeyFromExpression(it)).attr('name', 'qev[' + count + '].key');
                    $(getSpecialKeyValueFromExpression(it)).attr('name', 'qev[' + count + '].specialKeyValue');
                    $($(it).find('.isFromCopyPaste')).attr('name', 'qev[' + count + '].isFromCopyPaste');
                    var that = it;

                    var field = $(getFieldFromExpression(it)).select2('val');
                    var selectedFieldType = getFieldType($(getFieldFromExpression(it)).find(":selected"));

                    if (selectedFieldType != RF_TYPE_DATE) {
                        if (_.contains(EQUALS_OPERATORS, operator) && (selectedFieldType == RF_TYPE_AUTOCOMPLETE)) {
                            $selectedField = $(getValueSelectAutoFromExpression(it));
                            $selectedField.select2({
                                minimumInputLength: 2,
                                separator: ";",
                                multiple: true,
                                closeOnSelect: false,
                                ajax: {
                                    delay: 300,
                                    dataType: "json",
                                    url: selectAutoUrl,
                                    data: function (params) {
                                        var field = $(getFieldFromExpression(that)).select2('val');
                                        return {
                                            field: field,
                                            term: params.term,
                                            max: params.page * 10,
                                            lang: userLocale
                                        };
                                    },
                                    processResults: function (data, params) {
                                        var more = (params.page * 10) <= data.length;
                                        return {
                                            results: data,
                                            pagination: {
                                                more: more
                                            }
                                        };
                                    }
                                }
                            });
                            upgradeMultiSelect2($selectedField, selectAutoUrl, $(getFieldFromExpression(that)));
                            var convertedValue = splitStringListIntoArray(it.querySelector('input.qevValue').value);
                            _.each(convertedValue, function (item) {
                                if (!$selectedField.find(`option[value="${item}"]`).length) {
                                    $selectedField.append(new Option(item, item))
                                }
                            });
                            $selectedField.val(convertedValue).trigger('change');
                            addCopyPasteModal(it, templateQueryNamePrefix, count);
                        } else if (_.contains(EQUALS_OPERATORS, operator) && (selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN)) {
                            $selectedField = $(getValueSelectNonCacheFromExpression(it));
                            $selectedField.select2({
                                minimumInputLength: 0,
                                separator: ";",
                                multiple: true,
                                closeOnSelect: false,
                                ajax: {
                                    delay: 300,
                                    dataType: "json",
                                    url: selectNonCacheUrl,
                                    data: function (params) {
                                        var field = $(getFieldFromExpression(that)).select2('val');
                                        return {
                                            field: field,
                                            term: params.term,
                                            max: 30,
                                            lang: userLocale,
                                            page: params.page

                                        };
                                    },
                                    processResults: function (data, params) {
                                        var more = (30 == data.length);
                                        return {
                                            results: data,
                                            pagination: {
                                                more: more
                                            }
                                        };
                                    }
                                }
                            });
                            upgradeMultiSelect2($selectedField, selectNonCacheUrl, $(getFieldFromExpression(that)));
                            var convertedValue = splitStringListIntoArray(it.querySelector('input.qevValue').value);
                            _.each(convertedValue, function (item) {
                                if (!$selectedField.find(`option[value="${item}"]`).length) {
                                    $selectedField.append(new Option(item, item))
                                }
                            });
                            $selectedField.val(convertedValue).trigger('change');
                            addCopyPasteModal(it, templateQueryNamePrefix, count);
                        } else {
                            if (_.contains(EQUALS_OPERATORS, operator) && AJAXValueSampleList[field] && (AJAXValueSampleList[field].length != 0)) {
                                $selectedField = $(getValueSelectFromExpression(it));
                                $selectedField.select2({closeOnSelect: false});
                                upgradeMultiSelect2($selectedField, null, null);
                                $selectedField.val(splitStringListIntoArray(it.querySelector('input.qevValue').value)).trigger('change');
                                addCopyPasteModal(it, templateQueryNamePrefix, count);
                            } else {
                                $selectedField = $(getValueTextFromExpression(it));
                                $selectedField.val(it.querySelector('input.qevValue').value);
                                addCopyPasteModal(it, templateQueryNamePrefix, count);
                            }
                        }
                        if (getIsFromCopyPasteValue(it)) {
                            var copyPasteValue = $(getValueTextFromExpression(it));
                            copyPasteValue.val(it.querySelector('input.qevValue').value);
                            copyPasteValue.attr('name', 'qev[' + count + '].copyPasteValue');
                        }
                    } else if (selectedFieldType == RF_TYPE_DATE) {
                        $selectedField = $(getDateParameterFieldBasedOnOperatorSelected(it));
                        $selectedField.val(it.querySelector('input.qevValue').value);
                    } else {
                        console.log('Error: should have returned one of four value types!');
                    }

                    $selectedField.attr('name', 'qev[' + count + '].value');
                }))
            };
            // For custom SQL blanks
            if ($(it).hasClass('customSQLValueContainer')) {
                $(getKeyFromExpression(this)).attr('name', 'qev[' + count0 + '].key');
                $(getSQLValueFromExpression(this)).attr('name', 'qev[' + count0 + '].value');
                $(getSQLKeyFromExpression(this)).text(this.querySelector('input.qevKey').value);
                $(getSQLValueFromExpression(this)).val(this.querySelector('input.qevValue').value);
            }

            count0++;
        });
    });

    $.each($('.templateQuery-div'), function () {
        var templateQueryNamePrefix = getTQNum(this);
        var $selectedField;

        var count0 = 0;

        $.each($(this).find('.queryExpressionValues .queryBlankContainer'), function () {
            // For normal query blanks
            var it = this;
            if ($(it).hasClass('toAddContainerQEV')) {
                $(it).removeAttr('id');
                $(getFieldFromExpression(it)).attr('readonly', true).trigger('change');

                promises.push(setFieldSelect($(getFieldFromExpression(it)), it.querySelector('input.qevReportField').value, count0, null, function (count) {
                    initFieldDescriptionPopup(getFieldFromExpression(it));

                    updateAJAXFields(it);
                    var operator = it.querySelector('input.qevOperator').value;
                    $(getOperatorFromExpression(it)).val(it.querySelector('input.qevOperator').value).trigger('change');
                    updateAJAXOperators(it, it.querySelector('input.qevOperator').value);
                    updateAJAXValues(it);
                    $(getOperatorFromExpression(it)).removeAttr('id').attr('readonly', true).trigger('change');
                    if (!$(getOperatorFromExpression(it)).hasClass("select2-hidden-accessible")) {
                        $(getOperatorFromExpression(it)).select2();
                    }

                    $(getValueDateFromExpression(it)).datepicker({
                        allowPastDates: true,
                        textAllowed: true,
                        momentConfig: {
                            culture: userLocale,
                            format: DEFAULT_DATE_DISPLAY_FORMAT
                        }
                    });

                    $(getFieldFromExpression(it)).attr('name', templateQueryNamePrefix + '.qev[' + count + '].field');
                    $(getOperatorFromExpression(it)).attr('name', templateQueryNamePrefix + '.qev[' + count + '].operator');
                    $(getKeyFromExpression(it)).attr('name', templateQueryNamePrefix + '.qev[' + count + '].key');
                    $(getSpecialKeyValueFromExpression(it)).attr('name', templateQueryNamePrefix + '.qev[' + count + '].specialKeyValue');
                    $($(it).find('.isFromCopyPaste')).attr('name', templateQueryNamePrefix + '.qev[' + count + '].isFromCopyPaste');

                    var that = it;

                    var field = $(getFieldFromExpression(it)).select2('val');
                    var selectedFieldType = getFieldType($(getFieldFromExpression(it)).find(":selected"));

                    if (selectedFieldType != RF_TYPE_DATE) {
                        if (_.contains(EQUALS_OPERATORS, operator) && (selectedFieldType == RF_TYPE_AUTOCOMPLETE)) {
                            $selectedField = $(getValueSelectAutoFromExpression(it));
                            $selectedField.select2({
                                separator: ";",
                                minimumInputLength: 2,
                                multiple: true,
                                closeOnSelect: false,
                                ajax: {
                                    delay: 300,
                                    dataType: "json",
                                    url: selectAutoUrl,
                                    data: function (params) {
                                        var field = $(getFieldFromExpression(that)).select2('val');
                                        return {
                                            field: field,
                                            term: params.term,
                                            max: params.page * 10,
                                            lang: userLocale
                                        };
                                    },
                                    processResults: function (data, params) {
                                        var more = (params.page * 10) <= data.length;
                                        return {
                                            results: data,
                                            pagination: {
                                                more: more
                                            }
                                        };
                                    }
                                }
                            });
                            upgradeMultiSelect2($selectedField, selectAutoUrl, $(getFieldFromExpression(that)));
                            var convertedValue = splitStringListIntoArray(it.querySelector('input.qevValue').value);
                            _.each(convertedValue, function (item) {
                                if (!$selectedField.find(`option[value="${item}"]`).length) {
                                    $selectedField.append(new Option(item, item))
                                }
                            });
                            $selectedField.val(convertedValue).trigger('change');
                            addCopyPasteModal(it, templateQueryNamePrefix, count);
                        } else if (_.contains(EQUALS_OPERATORS, operator) && (selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN)) {
                            $selectedField = $(getValueSelectNonCacheFromExpression(it));
                            $selectedField.select2({
                                separator: ";",
                                minimumInputLength: 0,
                                multiple: true,
                                closeOnSelect: false,
                                ajax: {
                                    delay: 300,
                                    dataType: "json",
                                    url: selectNonCacheUrl,
                                    data: function (params) {
                                        var field = $(getFieldFromExpression(that)).select2('val');
                                        return {
                                            field: field,
                                            term: params.term,
                                            max: 30,
                                            lang: userLocale,
                                            page: params.page
                                        };
                                    },
                                    processResults: function (data, params) {
                                        var more = (30 == data.length);
                                        return {
                                            results: data,
                                            pagination: {
                                                more: more
                                            }
                                        };
                                    }
                                }
                            });
                            upgradeMultiSelect2($selectedField, selectNonCacheUrl, $(getFieldFromExpression(that)));
                            var convertedValue = splitStringListIntoArray(it.querySelector('input.qevValue').value);
                            _.each(convertedValue, function (item) {
                                if (!$selectedField.find(`option[value="${item}"]`).length) {
                                    $selectedField.append(new Option(item, item))
                                }
                            });
                            $selectedField.val(convertedValue).trigger('change');
                            addCopyPasteModal(it, templateQueryNamePrefix, count);
                        } else {
                            if (_.contains(EQUALS_OPERATORS, operator) && AJAXValueSampleList[field] && (AJAXValueSampleList[field].length != 0)) {
                                $selectedField = $(getValueSelectFromExpression(it));
                                $selectedField.select2({closeOnSelect: false});
                                upgradeMultiSelect2($selectedField, null, null);
                                $selectedField.val(splitStringListIntoArray(it.querySelector('input.qevValue').value)).trigger('change');
                                addCopyPasteModal(it, templateQueryNamePrefix, count);
                            } else {
                                $selectedField = $(getValueTextFromExpression(it));
                                $selectedField.val(it.querySelector('input.qevValue').value);
                                addCopyPasteModal(it, templateQueryNamePrefix, count);
                            }
                        }
                        if (getIsFromCopyPasteValue(it)) {
                            var copyPasteValue = $(getValueTextFromExpression(it));
                            copyPasteValue.val(it.querySelector('input.qevValue').value);
                            copyPasteValue.attr('name', templateQueryNamePrefix + '.qev[' + count + '].copyPasteValue');
                        }
                    } else if (selectedFieldType == RF_TYPE_DATE) {
                        $selectedField = $(getDateParameterFieldBasedOnOperatorSelected(it));
                        $selectedField.val(it.querySelector('input.qevValue').value);
                    } else {
                        console.log('Error: should have returned one of four value types!');
                    }

                    $selectedField.attr('name', templateQueryNamePrefix + '.qev[' + count + '].value');
                    if ((typeof qbeForm != 'undefined') && qbeForm) {
                        var operatorLabel = "";
                        if ($(getOperatorFromExpression(it)).select2("val") != "EQUALS") operatorLabel = " (" + $(getOperatorFromExpression(it)).select2("data").text + ")";
                        const fieldFromExpressionData = $(getFieldFromExpression(it)).select2("data");
                        $(it).prepend("<label>" + (_.isArray(fieldFromExpressionData) ? fieldFromExpressionData[0] : fieldFromExpressionData).text + operatorLabel + "</label><br>");
                        $(it).find(".expressionsNoPad.col-xs-3").removeClass("col-xs-3").addClass("col-xs-10");
                    }
                }));
            }
            // For custom SQL blanks
            if ($(this).hasClass('customSQLValueContainer')) {
                $(getKeyFromExpression(this)).attr('name', templateQueryNamePrefix + '.qev[' + count0 + '].key');
                $(getSQLValueFromExpression(this)).attr('name', templateQueryNamePrefix + '.qev[' + count0 + '].value');
                $(getSQLKeyFromExpression(this)).text(this.querySelector('input.qevKey').value);
                $(getSQLValueFromExpression(this)).val(this.querySelector('input.qevValue').value);
            }
            count0++;
        });

        var count1 = 0;
        $.each($(this.querySelector('div.templateSQLValues')).find('.customSQLValueContainer'), function () {
            $(getKeyFromExpression(this)).attr('name', templateQueryNamePrefix + '.tv[' + count1 + '].key');
            $(getSQLValueFromExpression(this)).attr('name', templateQueryNamePrefix + '.tv[' + count1 + '].value');
            $(getSQLKeyFromExpression(this)).text(this.querySelector('input.qevKey').value);
            $(getSQLValueFromExpression(this)).val(this.querySelector('input.qevValue').value);

            count1++;
        });

        var count2 = 0;
        $.each($(this.querySelector('div.templateSQLValues')).find('.poiInputValueContainer'), function () {
            $(getPOIKeyTextFromExpression(this)).text(this.querySelector('input.poiKey').value);
            $(getPOIKeyFromExpression(this)).attr('name', 'poiInput[' + count0 + '].key').val(this.querySelector('input.poiKey').value);
            $(getPOIValueFromExpression(this)).attr('name', 'poiInput[' + count0 + '].value').val(this.querySelector('input.poiValue').value);
            count2++;
        });
        rearrangePOIInputsCount();
        if ((typeof qbeForm != 'undefined') && qbeForm) {
            $(".selectTemplate").attr("readonly", true);
            $(this).find(".templateWrapperRow").after($(this).find(".queryStructure"))
            $($(this).find(".queryWrapperRow .row")[0]).hide();
        }
        $.when.apply($, promises)
            .done(showHideSpecialFields);
    });

    $.each($('.loading'), function () {
        $(this).hide();
    });
    $.each($('.doneLoading'), function () {
        $(this).show();
    });

    AJAXCount = -1;

    if ($('#queryBlanks').val() == 'true') {
        $('.selectQuery').trigger('select2:select');
    }
    if ($('#templateBlanks').val() == 'true') {
        $('.selectTemplate').trigger('select2:select');
    }

    if ($("#globalQueryBlanks").val() == "true") {
        $("#globalQuery").trigger('change');
    }

    var templateQueryFieldToUpdate = $(".templateQueryFieldToUpdate"),
        name = templateQueryFieldToUpdate.attr("type-name"),
        index = templateQueryFieldToUpdate.val();
    $("#templateQueries\\[" + index + "\\]\\." + name).trigger('change');


}

// Use select2 change event for IE11 in templateQueries.js
function selectTemplateOnChange(templateDropdownWidget, cioms1Id) {
    var templateContainer = getTemplateContainer(templateDropdownWidget);
    var templateValues = getTemplateValues(templateContainer);
    var templateWrapperRow = getTemplateWrapperRow(templateDropdownWidget);
    if (isForIcsrProfile == "true" || isForIcsrReport == "true") {
        $(templateWrapperRow).find("input[id$=blindProtected]").prop("checked", false);
        $(templateWrapperRow).find("input[id$=privacyProtected]").prop("checked", false);
    } else {
        if ($(templateDropdownWidget).val() && (($(templateDropdownWidget).val() == cioms1Id) || ($(templateDropdownWidget).val() == medWatchId))) {
            $(templateWrapperRow).find('.ciomsProtectedArea').removeAttr("hidden");
            if (isUserBlinded) {
                $(templateWrapperRow).find("input[id$=blindProtected]").prop("checked", true);
                $(templateWrapperRow).find("input[id$=blindProtected]").prop("disabled", true);
            }
            if (isUserRedacted) {
                $(templateWrapperRow).find("input[id$=privacyProtected]").prop("checked", true);
                $(templateWrapperRow).find("input[id$=privacyProtected]").prop("disabled", true);
            }
        } else {
            $(templateWrapperRow).find("input[id$=blindProtected]").prop("checked", false);
            $(templateWrapperRow).find("input[id$=privacyProtected]").prop("checked", false);
            $(templateWrapperRow).find('.ciomsProtectedArea').attr("hidden", "hidden");
        }
    }
    $(templateValues).empty();
    getGranularity($(templateDropdownWidget).val(), templateContainer);
    showHideReasssesDate($(templateDropdownWidget).val(), templateContainer);
    getCustomSQLValuesForTemplateAJAX($(templateDropdownWidget).val(), templateValues);
    getPOIInputsForTemplateAJAX($(templateDropdownWidget).val(), templateValues);
    if ($(templateDropdownWidget).val() != '') {
        $(templateWrapperRow).find('.templateViewButton').attr('href', templateViewUrl + '/' + $(templateDropdownWidget).val());
        $(templateWrapperRow).find('.templateViewButton').removeClass('hide');
        $(templateWrapperRow).find('.newTemplateDiv').addClass('hide');
    } else {
        $(templateWrapperRow).find('.templateViewButton').addClass('hide');
        $(templateWrapperRow).find('.newTemplateDiv').removeClass('hide');
    }
}

// Use select2 change event for IE11 in templateQueries.js
function selectQueryOnChange(selectContainer) {
    var queryContainer = getQueryWrapperRow(selectContainer);
    var expressionValues = getExpressionValues(queryContainer);
    var queryWrapperRow = getQueryWrapperRow(selectContainer);
    var globalqueryWrapperRow = getGlobalQueryWrapperRow(selectContainer);
    $(expressionValues).empty();
    if (getAJAXCount() == -1) {
        if ($(selectContainer).val() != '' && $(selectContainer).val() != null) {
            $(queryWrapperRow).find('.queryViewButton').attr('href', queryViewUrl + '/' + $(selectContainer).val());
            $(queryWrapperRow).find('.queryViewButton').removeClass('hide');
            $(queryWrapperRow).find('.newQuery').addClass('hide');
            $(globalqueryWrapperRow).find('.queryViewButton').attr('href', queryViewUrl + '/' + $(selectContainer).val());
        } else {
            $(queryWrapperRow).find('.queryViewButton').addClass('hide');
            $(queryWrapperRow).find('.newQuery').removeClass('hide');
        }
        getBlankValuesForQueryAJAX($(selectContainer).val(), expressionValues, (getTQNum(expressionValues) + "."));
        getCustomSQLValuesForQueryAJAX($(selectContainer).val(), expressionValues, (getTQNum(expressionValues) + "."));
        getBlankValuesForQuerySetAJAX($(selectContainer).val(), expressionValues, (getTQNum(expressionValues) + "."));
    } else {
        $(selectContainer).val(null).trigger('change');
    }
}

$(document).on('change', '.expressionField', function () {
    var container = this.parentElement.parentElement;
    var expression = container.parentElement;

    var field = $(this)[0].value;
    updateAJAXFields(container);

    updateAJAXValues(container);

    // Clear select values
    $(getValueSelectFromExpression(container)).val('').trigger('change');
    $(getValueSelectAutoFromExpression(container)).val('').trigger('change');
    $(getValueSelectNonCacheFromExpression(container)).val('').trigger('change');
    $(getValueTextFromExpression(container)).val('');

    // Remove error outline if we have a value
    if (field != '') {
        $(this.parentElement).removeClass('has-error');
    }
});

$(document).on('change', '.expressionOp', function () {
    var container = this.parentElement.parentElement;
    var expression = container.parentElement;
    var value = $(this)[0].value;

    updateAJAXOperators(container, value);

    // Clear select values
    $(getValueSelectFromExpression(container)).val('').trigger('change');
    $(getValueSelectAutoFromExpression(container)).val('').trigger('change');
    $(getValueSelectNonCacheFromExpression(container)).val('').trigger('change');
    $(getValueTextFromExpression(container)).val('');
});

$(document).on('change', '.expressionValueSelect', function () {
    var valueOfSelectedField = $(this).val()
    isQueryEmpty(valueOfSelectedField);
    $(this).parent().removeClass('has-error');
});

$(document).on('change', '.expressionValueText', function () {
    var valueOfSelectedField = $(this).val()
    isQueryEmpty(valueOfSelectedField);
    $(this).parent().removeClass('has-error');
});

$(document).on('change', '.expressionValueDate', function () {
    var valueOfSelectedField = $(this).find('.expressionValueDateInput').val()
    isQueryEmpty(valueOfSelectedField);
    $(this).parent().removeClass('has-error');
});


function showHideValue(selectedType, div) {
    switch (selectedType) {
        case EDITOR_TYPE_DATE:
            // Show datepicker
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).show();
            $(getCopyPasteButtonFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_SELECT:
            // Show select
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).show();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_NONECACHE_SELECT:
            // Show autocomplete
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).show();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_AUTOCOMPLETE:
            // Show autocomplete
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).show();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_NONE:
            // Hide All
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_TEXT:
        default:
            // Show text field
            $(getValueTextFromExpression(div).parentElement).show();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
    }
}

function getBlankValuesForQueryAJAX(queryId, queryContainer, namePrefix) {
    $(queryContainer).closest('.templateQuery-div').find('.showCustomReassess').hide();
    $.ajax({
        type: "GET",
        url: blankValuesForQueryUrl + "?queryId=" + queryId,
        dataType: 'json'
    })
        .done(function (result) {
            if (result.length > 0) {
                appendNewQEVContainers(result, queryContainer, namePrefix, 0);
            }
            showHideSpecialFields();
            setValidQueries(queryId, queryContainer);
            $(queryContainer).trigger("loadBlankAndCustomSqlFieldsComplete");
        })
        .fail(function () {
            console.log('Error retrieving parameters');
        });
}

function getGranularity(templateId, templateContainer) {
    $.ajax({
        type: "GET",
        url: granularityForTemplateUrl + "?templateId=" + templateId,
        dataType: 'json',
    })
        .done(function (result) {
            if (result.granularity) {
                $(templateContainer).find(".granularityDiv").show();
                $(templateContainer).find(".granularitySelect").attr("disabled", false);
            } else {
                $(templateContainer).find(".granularityDiv").hide();
                $(templateContainer).find(".granularitySelect").attr("disabled", "disabled");
            }
        })
        .fail(function () {
            console.log('Error retrieving granularity');
        });
}

function showHideReasssesDate(templateId, templateContainer) {
    $.ajax({
        type: "GET",
        url: reassessDateForTemplateUrl + "?templateId=" + templateId,
    })
        .done(function (result) {
            if (result === 'true') {
                $(templateContainer).find('.templtDatePicker').datepicker({
                    allowPastDates: true,
                    date: $(templateContainer).find(".templtReassessDate").val(),
                    momentConfig: {
                        culture: userLocale,
                        format: DEFAULT_DATE_DISPLAY_FORMAT
                    }
                });
                $(templateContainer).find(".templtReassessDateDiv").show();
            } else {
                $(templateContainer).find(".templtReassessDateDiv").hide();
                $(templateContainer).find(".templtReassessDate").val('');
            }
        })
        .fail(function () {
            console.log('Error retrieving reassess date');
        });
}

function getCustomSQLValuesForTemplateAJAX(templateId, templateContainer) {
    $.ajax({
        type: "GET",
        url: customSQLValuesForTemplateUrl + "?templateId=" + templateId,
        dataType: 'json'
    })
        .done(function (result) {
            if (result.length > 0) {
                appendNewCSQLContainers(result, templateContainer, 'tv', (getTQNum(templateContainer) + "."), 0);
            }
            $(templateContainer).trigger("loadCustomSQLValuesForTemplateComplete");
        })
        .fail(function () {
            console.log('Error retrieving custom SQL parameters');
        });
}

function getPOIInputsForTemplateAJAX(templateId, templateContainer) {
    $.ajax({
        type: "GET",
        url: poiInputsForTemplateUrl + "?templateId=" + templateId,
        dataType: 'json'
    })
        .done(function (result) {
            if (result.data.length > 0) {
                appendNewPOIContainers(result.data, templateContainer)
            }
            rearrangePOIInputsCount();
            showHideSpecialFields();
        })
        .fail(function (e) {
            console.log('Error retrieving POI parameters parameters');
        });
}

function getCustomSQLValuesForQueryAJAX(queryId, queryContainer, namePrefix) {
    $.ajax({
        type: "GET",
        url: customSQLValuesForQueryUrl + "?queryId=" + queryId,
        dataType: 'json'
    })
        .done(function (result) {
            if (result.length > 0) {
                appendNewCSQLContainers(result, queryContainer, 'qev', namePrefix, 0);
            }
            setValidQueries(queryId, queryContainer);
            $(queryContainer).trigger("loadBlankAndCustomSqlFieldsComplete");
        })
        .fail(function () {
            console.log('Error retrieving custom SQL parameters');
        });
}

function getBlankValuesForQuerySetAJAX(queryId, queryContainer, namePrefix) {
    $.ajax({
        type: "GET",
        url: blankValuesForQuerySetUrl + "?queryId=" + queryId,
        dataType: 'json'
    })
        .done(function (result) {
            if (result.length > 0) {
                var count = 0;
                var validQueries = [];
                $.each(result, function (index, query) {
                    // Add each query name
                    var queryName = query[0].queryName;
                    var container = createSingleQueryContainer(queryContainer, queryName);

                    validQueries.push(query[0].queryId);
                    if (query[0].type == "QUERY_BUILDER") {
                        count = appendNewQEVContainers(query, container, namePrefix, count);
                    } else if (query[0].type == "CUSTOM_SQL") {
                        count = appendNewCSQLContainers(query, container, 'qev', namePrefix, count);
                    }
                });
                setValidQueries(validQueries, queryContainer);
                $(queryContainer).trigger("loadBlankAndCustomSqlFieldsComplete");
            }
            showHideSpecialFields();
        })
        .fail(function () {
            console.log('Error retrieving set parameters');
        });
}

function setValidQueries(queryIds, queryContainer) {
    $(queryContainer.parentElement).find('.validQueries').val(queryIds);
}

function createSingleQueryContainer(wholeContainer, queryName) { // for query set
    var singleContainer = document.createElement('div');
    $(singleContainer).append('<div>' + queryName + ':</div>');
    wholeContainer.appendChild(singleContainer);
    return singleContainer;
}

function appendNewQEVContainers(result, queryContainer, namePrefix, count) { // for create page
//    var count = 0;
    var promises = [];
    var reassessListednessDate = null;
    var customReassessListedness = false;
    _.each(result, function (it) {
        if (it.customReassessDate) {
            reassessListednessDate = it.reassessListednessDate;
            customReassessListedness = true;
            result.splice(result.indexOf(it), 1);
        }
    });
    var showCustomReassess = $(queryContainer).closest('.templateQuery-div').find('.showCustomReassess');
    if (customReassessListedness && !reassessListednessDate) {
        showCustomReassess.find('.customDatePicker').datepicker({
            allowPastDates: true,
            date: $(showCustomReassess).find(".reassessDate").val(),
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });
        showCustomReassess.show();
    }
    _.each(result, function (it) {
        var toAdd = cloneAddContainer();
        queryContainer.appendChild(toAdd);
        toAdd['qevId'] = it.id;
        toAdd['key'] = it.key;
        $(getKeyFromExpression(toAdd)).attr('name', namePrefix + 'qev[' + count + '].key');
        $(getSpecialKeyValueFromExpression(toAdd)).attr('name', namePrefix + 'qev[' + count + '].specialKeyValue');
        $(getKeyFromExpression(toAdd)).val(it.key);
        $(getSpecialKeyValueFromExpression(toAdd)).val(it.specialKeyValue);
        var $field = $(getFieldFromExpression(toAdd));
        $field.select2();
        $field.attr('readonly', true);
        promises.push(setFieldSelect($field, it.field, count, queryContainer, function (_count) {
            initFieldDescriptionPopup($field);
            updateAJAXFields(toAdd);
            var $op = $(getOperatorFromExpression(toAdd));
            $op.removeAttr('id')
            .select2()
            .val(it.operator).trigger('change')
            .attr('readonly', true)
            updateAJAXOperators(toAdd, it.operator);
            if (it.operator != CONTAINS_OPERATORS) {
                updateAJAXValues(toAdd);
            }

            var $valueSelect = $(getValueSelectFromExpression(toAdd));
            $valueSelect.select2({closeOnSelect: false});
            upgradeMultiSelect2($valueSelect, null, null);
            var $autocompleteSelect = $(getValueSelectAutoFromExpression(toAdd));
            $autocompleteSelect.select2({
                minimumInputLength: 2,
                separator: ";",
                multiple: true,
                closeOnSelect: false,
                ajax: {
                    quietMillis: 300,
                    dataType: "json",
                    url: selectAutoUrl,
                    data: function (params) {
                        var field = $(getFieldFromExpression(toAdd)).select2('val');
                        return {
                            field: field,
                            term: params.term,
                            max: params.page * 10,
                            lang: userLocale
                        };
                    },
                    processResults: function (data, params) {
                        var more = (params.page * 10) <= data.length;
                        return {
                            results: data,
                            pagination: {
                                more: more
                            }
                        };
                    }
                }
            });
            upgradeMultiSelect2($autocompleteSelect, selectAutoUrl, $(getFieldFromExpression(toAdd)));
            var $nonCacheSelect = $(getValueSelectNonCacheFromExpression(toAdd));
            $nonCacheSelect.select2({
                minimumInputLength: 0,
                separator: ";",
                multiple: true,
                closeOnSelect: false,
                ajax: {
                    delay: 300,
                    dataType: "json",
                    url: selectNonCacheUrl,
                    data: function (params) {
                        var field = $(getFieldFromExpression(toAdd)).select2('val');
                        return {
                            field: field,
                            term: params.term,
                            max: 30,
                            lang: userLocale,
                            page: params.page
                        };
                    },
                    processResults: function (data, params) {
                        var more = (30 == data.length);
                        return {
                            results: data,
                            pagination: {
                                more: more
                            }
                        };
                    }
                }
            });
            upgradeMultiSelect2($nonCacheSelect, selectNonCacheUrl, $(getFieldFromExpression(toAdd)));
            $(getValueDateFromExpression(toAdd)).datepicker({
                allowPastDates: true,
                textAllowed: true,
                date: null,
                momentConfig: {
                    culture: userLocale,
                    format: DEFAULT_DATE_DISPLAY_FORMAT
                }
            });
            $field.attr('name', namePrefix + 'qev[' + _count + '].field');
            $op.attr('name', namePrefix + 'qev[' + _count + '].operator');
            $($(toAdd).find('.isFromCopyPaste')).attr('name', namePrefix + 'qev[' + _count + '].isFromCopyPaste');

            var field = $field.select2('val');
            var selectedFieldType = getFieldType($field.find(":selected"));

            if (selectedFieldType != RF_TYPE_DATE) {
                if (selectedFieldType == RF_TYPE_AUTOCOMPLETE) {
                    $autocompleteSelect.attr('name', namePrefix + 'qev[' + _count + '].value');
                    $(getValueTextFromExpression(toAdd)).attr('name', namePrefix + 'qev[' + _count + '].copyPasteValue');
                    addCopyPasteModal(toAdd, namePrefix, _count);
                } else if (selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN) {
                    $nonCacheSelect.attr('name', namePrefix + 'qev[' + _count + '].value');
                    $(getValueTextFromExpression(toAdd)).attr('name', namePrefix + 'qev[' + _count + '].copyPasteValue');
                    addCopyPasteModal(toAdd, namePrefix, _count);
                } else {
                    if (AJAXValueSampleList[field] && AJAXValueSampleList[field].length > 0) {
                        $valueSelect.attr('name', namePrefix + 'qev[' + _count + '].value');
                        $(getValueTextFromExpression(toAdd)).attr('name', namePrefix + 'qev[' + _count + '].copyPasteValue');
                        addCopyPasteModal(toAdd, namePrefix, _count);
                    } else {
                        $(getValueTextFromExpression(toAdd)).attr('name', namePrefix + 'qev[' + _count + '].value');
                        addCopyPasteModal(toAdd, namePrefix, _count);
                    }
                }
                var valueOfSelectedField = $("input[name='" + namePrefix + "qev[" + _count + "].value'").val();
                isQueryEmpty(valueOfSelectedField);
            } else if (selectedFieldType == RF_TYPE_DATE) {
                $(getDateParameterFieldBasedOnOperatorSelected(toAdd)).attr('name', namePrefix + 'qev[' + _count + '].value');
                $(toAdd).find('.copy-n-paste').hide();
                var valueOfSelectedField = $("input[name='" + namePrefix + "qev[" + _count + "].value'").val();
                isQueryEmpty(valueOfSelectedField);
            } else {
                console.log('Naming Error: should have returned one of four value types!');
            }

        }));
        count++;

    });
    $.when.apply($, promises)
        .done(showHideSpecialFields);
    $('[data-toggle="popover"]').popover();
    return count;
}

function appendNewCSQLContainers(result, valuesContainer, type, namePrefix, count) {
//    var count = 0;
    _.each(result, function (it) {
        if (it.key != null) {
            var toAdd = cloneCustomSQLValueContainer();
            valuesContainer.appendChild(toAdd);
            toAdd['qevId'] = it.id;
            toAdd['key'] = it.key;
            $(getKeyFromExpression(toAdd)).attr('name', namePrefix + '' + type + '[' + count + '].key');
            $(getKeyFromExpression(toAdd)).val(it.key);
            $(getSQLKeyFromExpression(toAdd)).text(it.key);
            $(getSQLValueFromExpression(toAdd)).attr('name', namePrefix + '' + type + '[' + count + '].value');

            count++;
        }
    });
    return count;
}

function appendNewPOIContainers(result, valuesContainer) {
    var count = 0;
    _.each(result, function (it) {
        var toAdd = clonePOIInputValueContainer();
        valuesContainer.appendChild(toAdd);
        toAdd['key'] = it;
        $(getPOIKeyTextFromExpression(toAdd)).text(it);
        $(getPOIKeyFromExpression(toAdd)).attr('name', 'poiInput[' + count + '].key').val(it);
        $(getPOIValueFromExpression(toAdd)).attr('name', 'poiInput[' + count + '].value');
        count++;
    });
    return count;
}

function getTQNum(queryContainer) {
    var $current = $(queryContainer).closest('.templateQuery-div');
    return $current.attr('id');
}

function cloneAddContainer() {
    var baseContainer = document.getElementById('toAddContainerQEV');
    // $('#selectField').select2("destroy");
    // $('#selectOperator').select2("destroy");
    // $('#selectSelect').select2("destroy");
    var containerToAdd = baseContainer.cloneNode(true);
    containerToAdd.removeAttribute('id');
    return containerToAdd;
}

function cloneCustomSQLValueContainer() {
    var baseContainer = document.getElementById('customSQLValueContainer');
    var containerToAdd = baseContainer.cloneNode(true);
    containerToAdd.removeAttribute('id');
    return containerToAdd;
}

function clonePOIInputValueContainer() {
    var baseContainer = document.getElementById('poiInputValueContainer');
    var containerToAdd = baseContainer.cloneNode(true);
    containerToAdd.removeAttribute('id');
    return containerToAdd;
}

function getTemplateWrapperRow(element) {
    return $(element).closest('.templateWrapperRow');
}

function getQueryWrapperRow(element) {
    return $(element).closest('.queryWrapperRow');
}

function getGlobalQueryWrapperRow(element) {
    return $(element).closest('.globalqueryWrapperRow');
}

function getTemplateContainer(element) {
    return $(element).closest('.templateContainer');
}

function getTemplateValues(element) {
    return $(element[0]).find('.templateSQLValues')[0];
}

function getExpressionValues(element) {
    return $(element[0]).find('.queryExpressionValues')[0];
}

function getFieldFromExpression(container) {
    return container.querySelector('select.expressionField');
}

function getFieldIsValidatableFromExpression(container) {
    return $(getFieldFromExpression(container)).find('option:selected').attr('data-validatable') == 'true';
}

function getFieldDictionaryFromExpression(container) {
    return $(getFieldFromExpression(container)).find('option:selected').attr('data-dictionary');
}

function getFieldLevelFromExpression(container) {
    return $(getFieldFromExpression(container)).find('option:selected').attr('data-level');
}

function getOperatorFromExpression(container) {
    return container.querySelector('select.expressionOp');
}

function getValueTextFromExpression(container) {
    return $(container).find('.expressionValueText')[0];
}

function getValueSelectFromExpression(container) {
    var list = $(container).find(".expressionValueSelect");
    if (list.length > 1) {
        return list[1];
    }
    return list[0];
}

function getValueSelectAutoFromExpression(container) {
    var list = $(container).find(".expressionValueSelectAuto");
    if (list.length > 1) {
        return list[1];
    }
    return list[0];
}

function getValueSelectNonCacheFromExpression(container) {
    var list = $(container).find(".expressionValueSelectNonCache");
    if (list.length > 1) {
        return list[1];
    }
    return list[0];
}


function getValueDateFromExpression(container) {
    return $(container).find('.expressionValueDate')[0];
}

function getCopyPasteButtonFromExpression(container) {
    return $(container).find('.copy-n-paste')[0];
}

function getValueDateInputFromExpression(container) {
    return $(container).find('.expressionValueDateInput')[0];
}

function getEventSearchIconFromExpression(container) {
    return $(container).find(".fa-search");
}

function getKeyFromExpression(container) {
    return container.querySelector('input.qevKey');
}

function getValueMultiselectInputFromExpression(container) {
    return container.querySelector('input.qevValue');
}

function getSQLKeyFromExpression(container) {
    return container.querySelector('.inputSQLKey');
}

function getSQLValueFromExpression(container) {
    return container.querySelector('input.inputSQLValue');
}

function getSpecialKeyValueFromExpression(container) {
    return container.querySelector('input.qevSpecialKeyValue');
}

function getPOIKeyTextFromExpression(container) {
    return container.querySelector('p.inputPOIKeyText');
}

function getPOIKeyFromExpression(container) {
    return container.querySelector('input.inputPOIKey');
}

function getPOIValueFromExpression(container) {
    return container.querySelector('input.inputPOIValue');
}


function getAJAXCount() {
    return AJAXCount;
}

function initFieldDescriptionPopup(selectElement) {
    var popover = $(selectElement).parent().find('[data-toggle="popover"]');
    var content = $(selectElement).find('option:selected').attr("data-description");
    if (content) {
        popover.show();
        popover.attr("data-content", content);
    } else {
        popover.hide();
    }
    popover.removeAttr("id");
}

// Multiselect Select2
// Returns a string if there is one value, or an array of strings if there are multiple values delimited by semicolons (';')
function splitStringListIntoArray(oldValue) {
    // var pos = oldValue.indexOf(';');
    // var posStart = 0;
    // var posEnd = pos;
    // var newValues;
    // if (pos != -1) {
    //     newValues = [];
    //     while (pos != -1) {
    //         newValues.push(oldValue.substring(posStart, posEnd));
    //         oldValue = oldValue.substring(posEnd + 2);
    //         pos = oldValue.indexOf(';');
    //         posEnd = pos;
    //     }
    //     newValues.push(oldValue);
    // } else {
    //     newValues = oldValue;
    // }

    if (oldValue) {
        return oldValue.split(";");
    }
}

function convertArrayToMultiselectString(value) {
    var result = value;
    if (Array.isArray(value)) {
        var concatValues = '';
        for (var i = 0; i < value.length; i++) {
            if (value != '') {
                if (i != 0) {
                    // Multiselect Select2
                    concatValues += ';' + value[i];
                } else {
                    concatValues += value[i];
                }
            }
        }
        result = concatValues;
    }

    return result;
}

function setMultiselectValues() {
    var $valueSelect;
    var $valueSelectAuto;
    var $valueSelectNonCache;
    var $valueMultiselect;
    var $valueText;
    $.each($('.templateQuery-div, .globalQueryWrapper'), function () {
        $.each($(this).find('.toAddContainerQEV'), function () {
            var selectedFieldType = getFieldType($(getFieldFromExpression(this)).find(":selected"));
            $valueSelect = $(getValueSelectFromExpression(this));
            $valueSelectAuto = $(getValueSelectAutoFromExpression(this));
            $valueSelectNonCache = $(getValueSelectNonCacheFromExpression(this));
            $valueText = $(getValueTextFromExpression(this));

            switch (selectedFieldType) {
                case RF_TYPE_AUTOCOMPLETE: // Autocomplete uses same operators as string
                    $valueMultiselect = $(getValueMultiselectInputFromExpression(this));
                    $valueMultiselect.attr('name', $valueSelectAuto.attr('name'));
                    $valueSelectAuto.removeAttr('name');
                    $valueMultiselect.val(convertArrayToMultiselectString($valueSelectAuto.select2('val')));
                    break;
                case RF_TYPE_NOCACHE_DROP_DOWN: // Autocomplete uses same operators as string
                    $valueMultiselect = $(getValueMultiselectInputFromExpression(this));
                    $valueMultiselect.attr('name', $valueSelectNonCache.attr('name'));
                    $valueSelectNonCache.removeAttr('name');
                    if ($valueSelectNonCache.hasClass("select2-hidden-accessible")) {
                        $valueMultiselect.val(convertArrayToMultiselectString($valueSelectNonCache.select2('val')));
                    }
                    break;
                case RF_TYPE_STRING: // string and text
                    $valueMultiselect = $(getValueMultiselectInputFromExpression(this));
                    $valueMultiselect.attr('name', $valueSelect.attr('name'));
                    $valueSelect.removeAttr('name');
                    if (getIsFromCopyPasteValue(this)) {
                        $valueMultiselect.val(convertArrayToMultiselectString($valueText.val()));
                    } else {
                        $valueMultiselect.val(convertArrayToMultiselectString($valueSelect.val()));
                    }
                    break;
                default:
                    break;
            }
        });
    });
}

//Need to be consistent with updateAJAXOperators logic
function getDateParameterFieldBasedOnOperatorSelected(container) {
    var operator = $(getOperatorFromExpression(container)).val();
    if (_.contains(LAST_X_DATE_OPERATORS, operator) || _.contains(NEXT_X_DATE_OPERATORS, operator)) {
        return getValueTextFromExpression(container);
    }
    return getValueDateInputFromExpression(container);
}

function rearrangePOIInputsCount() {
    $("input[name^='poiInput['].inputPOIValue").each(function (index) {
        $(this).attr("name", "poiInput[" + index + "].value");
        $(this).closest(".poiInputValueContainer").find("input[name$='].key']").attr("name", "poiInput[" + index + "].key");
    });
}

function setFieldSelect($field, field, count, queryContainer, fun) {
    return $.ajax({
        type: "GET",
        url: reportFieldsForQueryUrl + "?name=" + field,
        dataType: 'json',
    })
        .done(function (result) {
            $field.append($('<option></option>')
                .attr("value", result.name)
                .attr("selected", true)
                .attr("data-value", result.name)
                .attr("data-dictionary", result.dictionary)
                .attr("data-level", result.level)
                .attr("data-validatable", result.validatable)
                .attr("data-isAutocomplete", result.isAutocomplete)
                .attr("data-isNonCacheSelectable", result.isNonCacheSelectable)
                .attr("data-dataType", result.dataType)
                .attr("data-description", result.description)
                .text(result.displayText));
            $field.val(result.name).trigger("change");
            fun(count);
            $(queryContainer).trigger("loadBlankAndCustomSqlFieldsComplete");
        });
}

function isQueryEmpty(valueOfSelectedField) {
    valueOfSelectedFields = valueOfSelectedField;

}

