// AJAX Implementation
// These fields are used to store AJAX
var AJAXKeywordList;
var AJAXValueSampleList = [];
var AJAXValueExtraList;
var AJAXOperatorStringList;
var AJAXOperatorNumList;
var AJAXOperatorDateList;
var AJAXOperatorValuelessList;
var AJAXEmbaseCombinedList;
var AJAXEmbasePhraseList;
var AJAXEmbaseExactList;
// We have 5 AJAX calls, so we update values after the 4th is complete
var AJAXFinished = 6;
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
var RF_TYPE_EMBASE_PHRASE = 'embasePhrase'
var RF_TYPE_EMBASE_EXACT = 'embaseExact'
var RF_TYPE_EMBASE_COMBINED = 'embaseCombined'

var EDITOR_TYPE_TEXT = 0;
var EDITOR_TYPE_DATE = 1;
var EDITOR_TYPE_SELECT = 2;
var EDITOR_TYPE_NONE = 3;
var EDITOR_TYPE_AUTOCOMPLETE = 4;
var EDITOR_TYPE_RPT_FIELD = 5;
var EDITOR_TYPE_NONECACHE_SELECT = 6;

// DATE OPERATOR CONSTANTS - these are the names, not values of the QueryOperator enum.
var LAST_X_DATE_OPERATORS = ['LAST_X_DAYS', 'LAST_X_WEEKS', 'LAST_X_MONTHS', 'LAST_X_YEARS'];
var NEXT_X_DATE_OPERATORS = ['NEXT_X_DAYS', 'NEXT_X_WEEKS', 'NEXT_X_MONTHS', 'NEXT_X_YEARS'];
var RELATIVE_DATE_OPERATORS = ['YESTERDAY', 'LAST_WEEK', 'LAST_MONTH', 'LAST_YEAR', 'TOMORROW', 'NEXT_WEEK', 'NEXT_MONTH', 'NEXT_YEAR'];
var IS_EMPTY_OPERATORS = ['IS_EMPTY', 'IS_NOT_EMPTY'];
var EQUALS_OPERATORS = ['EQUALS', 'NOT_EQUAL'];

// Extra values
// Re-assess Listedness
var REASSESS_LISTEDNESS_FIELD = 'dvListednessReassessQuery';
var REASSESS_LISTEDNESS_FIELD_J = 'dvListednessReassessQueryJ';
var RLDS_KEY = 'RLDS';
var RLDS_OPDS = 'RLDS_OPDS';

// END CONSTANTS

// Blank Parameters Implementation
var hasBlanks = false;
var keys = [];
// -----------END BLANK PARAMETERS

// Tabs
var activeTab; // current visible tab

// These fields reference the values we use when we add expressions
var $selectedField;
var $selectedOperator;
var $selectedValue;
var $selectDate;
var $selectSelect;
var $selectSelectField;
// This is the group we will add new expressions to
var selectedGroup;
var builderAll;
var builderSubgroup;
var $queryJSON;
// Drag and drop
var dragSourceEl;
var draggingFromGroup;

// setBuilder
var $selectedQuery;
var selectedSetGroup;
var setBuilderAll;
var setBuilderSubgroup;
var $setJSON;

// Tabs
var $queryBuilder;
var $queryBuilderTab;
var $queryBuilderLink;
var $setBuilder;
var $setBuilderTab;
var $setBuilderLink;
var $customSQL;
var $customSQLTab;
var $customSQLLink;

// Backbone
// Definitions
var Expressions;
// setBuilder
var Sets;
var Expression;
var ExpressionList;
// setBuilder
var SetList;
// Variables
// This Backbone collection is used to store our Backbone expressions
var backboneExpressions;
// We render on initialize, so this variable is not called explicitly
var expressionList;
// setBuilder
// This Backbone collection is used to store our Backbone expressions
var backboneSets;
// We render on initialize, so this variable is not called explicitly
var setList;
// Updated based on the query target dropdown value
var isFaersTarget = (typeof isTargetValueFaers !== 'undefined') ? isTargetValueFaers : false;


$(function () {
    // Page startup -------------------------------------------------------------------------------------------- Startup

    $('#customDatePicker').datepicker({
        allowPastDates: true,
        date: $("[name='reassessListednessDate']").val(),
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    })

    $("[name='reassessListedness']").on('change', function () {
        if ($(this).val() === 'CUSTOM_START_DATE') {
            $('#customDateSelector').show();
        } else $('#customDateSelector').hide();
    })

    // These fields reference the values we use when we add expressions
    $selectedField = $('#selectDataSourceField');
    $selectedOperator = $('#selectOperator');
    $selectedValue = $('#selectValue');
    $selectDate = $('#selectDate');
    $selectSelect = $('#selectSelect');
    $selectSelectField = $('#selectRptField');
    // This is the group we will add new expressions to
    builderAll = document.getElementById('builderAll');

    $queryJSON = $('#queryJSON');
    // setBuilder
    $selectedQuery = $('#selectQuery');
    setBuilderAll = document.getElementById('setBuilderAll');

    $setJSON = $('#setJSON');
    // Tabs
    $queryBuilder = $('#queryBuilder');
    $queryBuilderTab = $('#queryBuilderTab');
    $queryBuilderLink = $('#queryBuilderLink');
    $setBuilder = $('#setBuilder');
    $setBuilderTab = $('#setBuilderTab');
    $setBuilderLink = $('#setBuilderLink');
    $customSQL = $('#customSQL');
    $customSQLTab = $('#customSQLTab');

    $customSQLLink = $('#customSQLLink');

    // Re-assess Listedness
    $('#reassessListedness').select2();

    $('#sourceProfile').select2();

    $queryBuilder.on('change', '#sourceProfile', function () {
        loadReportFieldOptions(queryReportFieldsOptsBySource, false, $(this).val(), [$selectedField, $selectSelectField], function () {
            showLoader();
        }, function () {
            hideLoader();
        }, function () {
            getSelect2TreeView($selectSelectField);
            getSelect2TreeView($selectedField);
        });
    });

    if (typeof FAERS_QUERY_TARGET_ENUM_VAL !== 'undefined') {
        $('#queryTarget').on('change', function () {
            isFaersTarget = $(this).val() === FAERS_QUERY_TARGET_ENUM_VAL;
        });
    }

    // Backbone is used to easily model the expressions we use ------------------------------------------------ BACKBONE

    Expressions = Backbone.Collection.extend({});

    // setBuilder
    Sets = Backbone.Collection.extend({});


    Expression = Backbone.Model.extend({
        /*
         model does not explicitly state its attributes, so here it is with some sample values:
         field: "countryOfIncident",
         op: 'CONTAINS',
         value: 'United'
         */
    });

    // --------------------------------------------------------------------------------------------------------- END TAB

    // ----------------------------------------------------------------------------------------------------- END STARTUP

    // AJAX Calls to get data ------------------------------------------------------------------------------------- AJAX

    function getExtraValuesAJAX() {
        $.ajax({
            type: "GET",
            url: extraValuesUrl,
            dataType: 'json',
            data: {
                lang: userLocale
            }
        })
            .done(function (result) {
                AJAXValueExtraList = result;
                if (AJAXCount == AJAXFinished) {
                    updateInitialAJAXLists();
                } else {
                    AJAXCount++;
                }
            });
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

    function getKeywordsAJAX() {
        $.ajax({
            type: "GET",
            url: keywordsUrl,
            dataType: 'json'
        })
            .done(function (result) {
                AJAXKeywordList = result;
                if (AJAXCount == AJAXFinished) {
                    updateInitialAJAXLists();
                } else {
                    AJAXCount++;
                }
            });
    }

    function getEmbaseOperatorsAJAX() {
        $.ajax({
            type: "GET",
            url: embaseOperatorsURL,
            dataType: 'json'
        })
            .done(function (result) {
                AJAXEmbaseCombinedList = result;
                AJAXEmbasePhraseList = [result[0]];
                AJAXEmbaseExactList = [result[1]];
                if (AJAXCount == AJAXFinished) {
                    updateInitialAJAXLists();
                }
                else {
                    AJAXCount++;
                }
            });
    }

    function getAJAXValues() {
        getDateOperatorsAJAX();
        getNumOperatorsAJAX();
        getStringOperatorsAJAX();
        getValuelessOperatorsAJAX();
        getKeywordsAJAX();
        getExtraValuesAJAX();
        getEmbaseOperatorsAJAX();
    }

    function clearValues(container) {
        // Clear select values
        $(getValueSelectFromExpression(container)).val(null).trigger('change');
        $(getValueSelectAutoFromExpression(container)).val(null).trigger('change');
        $(getValueSelectNonCacheFromExpression(container)).val(null).trigger('change');
        $(getReportFiledSelectFromExpression(container)).val(null).trigger('change');
        $(getValueTextFromExpression(container)).val('');
    }

    // On success, call these update methods to fill in UI values)
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
            case RF_TYPE_NOCACHE_DROP_DOWN:
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
            case RF_TYPE_EMBASE_PHRASE:
                $.each(AJAXEmbasePhraseList, function (display, value) {
                    $(op).append($("<option></option>").attr("value", this.value).text(this.display));
                });
                break;
            case RF_TYPE_EMBASE_EXACT:
                $.each(AJAXEmbaseExactList, function (display, value) {
                    $(op).append($("<option></option>").attr("value", this.value).text(this.display));
                });
                break;
            case RF_TYPE_EMBASE_COMBINED:
                $.each(AJAXEmbaseCombinedList, function (display, value) {
                    $(op).append($("<option></option>").attr("value", this.value).text(this.display));
                });
                break;
        }
    }

    function updateAJAXOperators(container, value) {
        var field = $(getFieldFromExpression(container)).select2("val");
        var operator = $(getOperatorFromExpression(container))[0].value;
        var expression = container.parentElement;
        var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find(":selected"));

        if (_.contains(IS_EMPTY_OPERATORS, operator)) {
            if ((expression != undefined) && (expression.expressionIndex != null)) {
                backboneExpressions.at(expression.expressionIndex).set("value", value);
            }
            showHideValue(EDITOR_TYPE_NONE, container);
            return;
        }

        if ((selectedFieldType == RF_TYPE_DATE || selectedFieldType == RF_TYPE_PART_DATE) && _.contains(RELATIVE_DATE_OPERATORS, operator)) {
            if ((expression != undefined) && (expression.expressionIndex != null)) {
                backboneExpressions.at(expression.expressionIndex).set("value", value);
            }
            showHideValue(EDITOR_TYPE_NONE, container);
            return;
        }

        if (selectedFieldType == RF_TYPE_DATE && (_.contains(LAST_X_DATE_OPERATORS, operator) || _.contains(NEXT_X_DATE_OPERATORS, operator))) {
            showHideValue(EDITOR_TYPE_TEXT, container);
            $(getCopyPasteButtonFromExpression(container).parentElement).hide();
            return;
        }

        if ($(container).find('.rptFieldInput').is(':checked')) {
            showHideValue(EDITOR_TYPE_RPT_FIELD, container);
        } else if ($(container).find('.poiInput').is(':checked')) {
            showHideValue(EDITOR_TYPE_TEXT, container);
        } else if (selectedFieldType == RF_TYPE_DATE) {
            showHideValue(EDITOR_TYPE_DATE, container);
        } else if (selectedFieldType == RF_TYPE_STRING) {
            if (isCopyPasteField(container)) {
                showHideValue(EDITOR_TYPE_TEXT, container)
            } else if ((_.contains(EQUALS_OPERATORS, operator)) && AJAXValueSampleList[field] && AJAXValueSampleList[field].length > 0) {
                showHideValue(EDITOR_TYPE_SELECT, container);
            } else {
                showHideValue(EDITOR_TYPE_TEXT, container);
            }
        } else if (selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN) {
            if (isCopyPasteField(container)) {
                showHideValue(EDITOR_TYPE_TEXT, container)
            } else if ((_.contains(EQUALS_OPERATORS, operator))) {
                showHideValue(EDITOR_TYPE_NONECACHE_SELECT, container);
            } else {
                showHideValue(EDITOR_TYPE_TEXT, container);
            }
        } else if (selectedFieldType == RF_TYPE_AUTOCOMPLETE) {
            if (isCopyPasteField(container)) {
                showHideValue(EDITOR_TYPE_TEXT, container)
            } else if ((_.contains(EQUALS_OPERATORS, operator))) {
                showHideValue(EDITOR_TYPE_AUTOCOMPLETE, container);
            } else {
                showHideValue(EDITOR_TYPE_TEXT, container);
            }
        } else if (selectedFieldType == RF_TYPE_PART_DATE) {
            showHideValue(EDITOR_TYPE_TEXT, container);
        } else {
            showHideValue(EDITOR_TYPE_TEXT, container);
        }
    }

    // only execute this after operator values have been assigned
    function updateAJAXValues(container) {
        var field = $(getFieldFromExpression(container)).select2("val");
        setFieldTitleOnHover(container);
        var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find(":selected"));
        var selectValue = getValueSelectFromExpression(container);
        $(selectValue).empty();
        if ($(container).find('.poiInput').is(':checked')) {
            showHideValue(EDITOR_TYPE_TEXT, container);
        } else if ($(container).find('.rptFieldInput').is(':checked')) {
            showHideValue(EDITOR_TYPE_RPT_FIELD, container);
        } else if (isCopyPasteField(container)) {
            showHideValue(EDITOR_TYPE_TEXT, container)
        } else if (selectedFieldType != RF_TYPE_DATE) {
            if (selectedFieldType == RF_TYPE_AUTOCOMPLETE) {
                showHideValue(EDITOR_TYPE_AUTOCOMPLETE, container);
            } else if (selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN) {
                showHideValue(EDITOR_TYPE_NONECACHE_SELECT, container);
            } else {
                loadValues(field, function (values) {
                    if (values && values.length !== 0) {
                        $.each(values, function () {
                            $(selectValue).append($("<option></option>").attr("value", this).text(this));
                        });
                        showHideValue(EDITOR_TYPE_SELECT, container);
                    } else {
                        showHideValue(EDITOR_TYPE_TEXT, container);
                    }
                })
            }
        } else {
            showHideValue(EDITOR_TYPE_DATE, container);
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

    function updateExtraValues(container) {
        var reassessListednessDSValue = getExtraValueRLDSSelectFromContainer(container);
        if (reassessListednessDSValue) {
            if (AJAXValueExtraList[REASSESS_LISTEDNESS_FIELD] && AJAXValueExtraList[REASSESS_LISTEDNESS_FIELD].length > 0) {
                $.each(AJAXValueExtraList[REASSESS_LISTEDNESS_FIELD], function () {
                    if (typeof this === 'string') {
                        $(reassessListednessDSValue).append($("<option></option>").attr("value", this).text(this));
                    } else {
                        $(reassessListednessDSValue).append($("<option></option>").attr("value", this.sheetName).attr("data-hasChildrenFlag", this.hasChildrenFlag).text(this.sheetName));
                    }
                });
            }
            if (AJAXValueExtraList[REASSESS_LISTEDNESS_FIELD_J] && AJAXValueExtraList[REASSESS_LISTEDNESS_FIELD_J].length > 0) {
                $.each(AJAXValueExtraList[REASSESS_LISTEDNESS_FIELD_J], function () {
                    $(reassessListednessDSValue).append($("<option></option>").attr("value", this).text(this));
                });
            }
        }

        // RLDS is set here as there is an issue trying to give it a value when we build it from JSON.
        var expression = $(container).closest('.expression')[0];
        if (expression) {
            var model = backboneExpressions.at(expression.expressionIndex);
            // Multiselect Select2
            if (model) {
                var RLDSValue = model.get('RLDS');
                if (RLDSValue) {
                    $(reassessListednessDSValue).val(RLDSValue.split(';')).trigger('change');
                    updateOnPrimaryDatasheetCheckbox(reassessListednessDSValue.parent());
                }
            }
        }
    }

    // need to grab and set our next operators and values from field values. This should only run once on page load
    function updateInitialAJAXLists() {
        if (queryType === 'QUERY_BUILDER') {
            updateExtraValues(document.getElementById('toAddExtraValues'));
            $.each($($('.builderAll')[0]).find('.expression'), function () {
                var container = $(this).find('.toAddContainer')[0];
                updateAJAXFields(container);
                updateAJAXValues(container);
                $(getOperatorFromExpression(container)).val(backboneExpressions.at($(this)[0].expressionIndex).get('op'));
                updateAJAXOperators(container);
                updateExtraValues(container);
                var visible = getCorrectSelectValueFromContainer(container);
                var oldValue;
                var convertedValue;
                if (visible === getValueTextFromExpression(container)) {
                    $(getValueTextFromExpression(container)).val(backboneExpressions.at($(this)[0].expressionIndex).get('value'));
                } else if (visible === getValueSelectFromExpression(container)) {
                    // Multiselect Select2
                    oldValue = backboneExpressions.at($(this)[0].expressionIndex).get('value');
                    convertedValue = oldValue.split(";");
                    if (convertedValue) {
                        if (!convertedValue[0].trim()) {
                            convertedValue = null; // This removes the blank artifact if you split a blank value into [""]
                        }
                    }
                    $(getValueSelectFromExpression(container)).val(convertedValue);
                } else if (visible === getValueSelectAutoFromExpression(container)) {
                    // Multiselect Select2
                    oldValue = backboneExpressions.at($(this)[0].expressionIndex).get('value');
                    convertedValue = oldValue.split(";");
                    if (convertedValue) {
                        if (!convertedValue[0].trim()) {
                            convertedValue = null; // This removes the blank artifact if you split a blank value into [""]
                        }
                    }
                    var arrayData = [];
                    _.each(convertedValue, function (item) {
                        arrayData.push({id: item, text: item});
                    });
                    $(getValueSelectAutoFromExpression(container)).val(convertedValue);
                } else if (visible === getValueSelectNonCacheFromExpression(container)) {
                    // Multiselect Select2
                    oldValue = backboneExpressions.at($(this)[0].expressionIndex).get('value');
                    convertedValue = oldValue.split(";");
                    if (convertedValue) {
                        if (!convertedValue[0].trim()) {
                            convertedValue = null; // This removes the blank artifact if you split a blank value into [""]
                        }
                    }
                    var arrayDataNonCache = [];
                    _.each(convertedValue, function (item) {
                        $(visible).append(new Option(item, item))
                        arrayDataNonCache.push({id: item, text: item});
                    });
                    $(visible).val(convertedValue)
                } else if (visible === getReportFiledSelectFromExpression(container)) {
                    $(getReportFiledSelectFromExpression(container)).val(backboneExpressions.at($(this)[0].expressionIndex).get('value'));
                } else {
                    $(getValueDateInputFromExpression(container)).val(backboneExpressions.at($(this)[0].expressionIndex).get('value'));
                }
            });
        }

        $.each($('.loading'), function () {
            $(this).hide();
        });
        $.each($('.doneLoading'), function () {
            $(this).show();
        });

        // setBuilder
        if (!editable) {
            $('#toAddContainer').parent().hide();
            $('#toAddContainerSet').parent().hide();
        }

        AJAXCount = 0;

        // Re-assess Listedness
        showHideReassessListedness(builderAll);
    }

    function getReportFieldDicType() {
        return $('#selectDataSourceField option:selected').attr('data-dictionary');
    }

    loadReportFieldOptions(queryDefaultReportFieldsOpts, true, '', [$("#selectDataSourceField"), $selectSelectField], function () {
            showLoader();
            $('div.tab-content').hide();
        }, function () {
            hideLoader();
            $('div.tab-content').show();
        },
        function () {
            ExpressionList = Backbone.View.extend({
                initialize: function () {
                    // this should build from previously saved data if it exists. this view is only used for initial rendering.
                    var JSONQuery = $queryJSON.val();
                    if (!$('#reassessListednessDSSelect').hasClass('select2-hidden-accessible')) {
                        $('#reassessListednessDSSelect').select2();
                    }
                    if (JSONQuery == null || JSONQuery == '') {
                        selectedGroup = createGroup();
                        builderAll.appendChild(selectedGroup);
                    } else {
                        buildQueryFromJSON(JSONQuery);
                        selectedGroup = $(builderAll).find('.group')[0];
                    }

                    addSubgroup(builderAll);
                    builderSubgroup = getSubgroup(builderAll);

                    $(selectedGroup).addClass('selectedGroup');

                    $("#queryBuilder").on('query.builder.updateAJAXValues', function (evt) {
                        var container = evt.target;
                        updateAJAXValues(container)
                    });

                    // this isn't necessary, but doing it to test if we get the same result as we saved
                    $queryJSON.val(printAll());
                }
            });

            // setBuilder
            SetList = Backbone.View.extend({
                initialize: function () {
                    // this should build from previously saved data if it exists. this view is only used for initial rendering.
                    var JSONQuery = $('#setJSON').val();

                    if (JSONQuery == null || JSONQuery == '') {
                        selectedSetGroup = createGroup();
                        setBuilderAll.appendChild(selectedSetGroup);
                    } else {
                        buildSetFromJSON(JSONQuery);
                        selectedSetGroup = $(setBuilderAll).find('.group')[0];
                    }

                    addSubgroup(setBuilderAll);
                    setBuilderSubgroup = getSubgroup(setBuilderAll);

                    $(selectedSetGroup).addClass('selectedGroup');

                    // this isn't necessary, but doing it to test if we get the same result as we saved
                    $setJSON.val(printSet());
                }
            });

            // ---------------------------------------------------------------------------------------------------- END BACKBONE

            // View page
            if (!editable) {
                $('#customSQLQuery').attr('disabled', true);
                $('#toAddContainer :input').attr('disabled', 'disabled');
                $('#toAddContainerSet :input').attr('disabled', 'disabled');
                $('.expressionsNoPad i').attr('data-target', ' ');
                // Re-assess Listedness
                $('#reassessListedness').attr('disabled', true);
                $('#reassessListednessDSSelect').prop("disabled", true);
                $('#reassessForProduct').attr('disabled', true);
            } else {
                // Show custom tab if this is a custom SQL Query
                switch (queryType) {
                    case 'QUERY_BUILDER':
                        break;
                    case 'SET_BUILDER':
                        setActiveTabSet();
                        break;
                    case 'CUSTOM_SQL':
                        setActiveTabCustom();
                        break;
                }
            }

            // Manage tabs
            if (disableInactiveTabs) {
                switch (queryType) {
                    case 'QUERY_BUILDER':
                        // Query tab set to active by default in GSP
                        disableTabLink($setBuilderLink);
                        disableTabLink($customSQLLink);
                        break;
                    case 'SET_BUILDER':
                        setActiveTabSet();
                        disableTabLink($queryBuilderLink);
                        disableTabLink($customSQLLink);
                        break;
                    case 'CUSTOM_SQL':
                        setActiveTabCustom();
                        disableTabLink($queryBuilderLink);
                        disableTabLink($setBuilderLink);
                        break;
                }
            }

            // Tab -------------------------------------------------------------------------------------------------------------

            activeTab = queryType;
            $('#queryType').val(queryType);

            $('a[data-toggle="tab"]').on('click', function (e) {
                activeTab = $(e.target).attr('aria-controls');
                $('#queryType').val(activeTab);
            });


            getAJAXValues();

            $('#showDate').hide();
            $('#showSelect').hide();
            $('#showSelectAuto').hide();
            $('#showSelectNonCache').hide();
            $('#showSelectRpField').hide();
            $('#errorMessageOperator').hide();

            // This Backbone collection is used to store our Backbone expressions
            backboneExpressions = new Expressions();

            // setBuilder
            // This Backbone collection is used to store our Backbone expressions
            backboneSets = new Expressions();

            expressionList = new ExpressionList();
            setList = new SetList();

            // Initialize various components
            getSelect2TreeView($("#selectDataSourceField"));
            getSelect2TreeView($('#selectRptField'));
            $selectedField.on("select2:open", function (e) {
                var searchField = $('.select2-dropdown .select2-search__field');
                if (searchField.length) {
                    searchField[0].focus();
                }
            }).on("change", function () {
                var title = $(this).find('option:selected').text();
                $(this).attr("title", title);
                var content = $(this).find('option:selected').attr("data-description");
                if (content)
                    $('#selectPopover').attr("data-content", content);
                else
                    $('#selectPopover').attr("data-content", $.i18n._("app.query.field.no.description"));
            });
            $('#selectSelect').select2({closeOnSelect: false});
            upgradeQueryMultiSelect2('#selectSelect', null);
            $('#selectSelectAuto').select2({
                minimumInputLength: 2,
                multiple: true,
                separator: ";",
                closeOnSelect: false,
                ajax: {
                    delay: 300,
                    dataType: "json",
                    url: selectAutoUrl,
                    data: function (params) {
                        var container = $(this).closest('.toAddContainer')[0];
                        var field = $(getFieldFromExpression(container)).select2('val');
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
            upgradeQueryMultiSelect2('#selectSelectAuto', selectAutoUrl);
            $('#selectSelectNonCache').select2({
                minimumInputLength: 0,
                multiple: true,
                separator: ";",
                closeOnSelect: false,
                ajax: {
                    delay: 300,
                    dataType: "json",
                    url: selectNonCacheUrl,
                    data: function (params) {
                        var container = $(this).closest('.toAddContainer')[0];
                        var field = $(getFieldFromExpression(container)).select2('val');
                        return {
                            field: field,
                            term: params.term,
                            max: 30,
                            lang: userLocale,
                            page: params.page,
                            isFaersTarget: isFaersTarget
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

            upgradeQueryMultiSelect2('#selectSelectNonCache', selectNonCacheUrl);

            $('#reassessListednessDSSelect').select2();
            $('#selectDate').datepicker({
                allowPastDates: true,
                textAllowed: true,
                momentConfig: {
                    culture: userLocale,
                    format: DEFAULT_DATE_DISPLAY_FORMAT
                }
            });
            $('#queryLevel').select2();

            $.each($('.doneLoading'), function () {
                $(this).hide();
            });

            // setBuilder
            bindQuerySelect2($selectedQuery).on("select2:open", function (e) {
                var searchField = $('.select2-dropdown .select2-search__field');
                if (searchField.length) {
                    searchField[0].focus();
                }
            });


            // -------------------------------------------------------------------------------------------------------- END AJAX

            // Change events are captured by classname ---------------------------------------------------------------- onChange


            $('#mainContent').on('change', '.expressionField', function () {
                var container = $(this).closest('.toAddContainer')[0];
                var expression = $(container).closest('.expression')[0];

                var field = $(this)[0].value;
                updateAJAXFields(container);
                updateAJAXValues(container);

                if (field === REASSESS_LISTEDNESS_FIELD || field === REASSESS_LISTEDNESS_FIELD_J) {
                    addShowRLDS(container, null, field);
                    // Re-assess Listedness
                    showHideReassessListedness(builderAll, true);
                } else {
                    var RLDSContainer = getExtraValueRLDSSelectFromContainer(container);
                    if (RLDSContainer.length > 0) {
                        $(RLDSContainer[0].parentElement).remove();
                    }
                    showHideReassessListedness(builderAll);
                }

                // show dictionary search icon
                if (typeof getReportFieldDicType() != 'undefined') {
                    showHideDictionaryIcon(getReportFieldDicType(), "#toAddContainer");
                }

                // Update backbone model
                if (expression) {
                    if ((expression != undefined) && (expression.expressionIndex != null)) {
                        backboneExpressions.at(expression.expressionIndex).set("field", field);
                        backboneExpressions.at(expression.expressionIndex).set("op", $(getOperatorFromExpression(container)).val());
                        var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find(":selected"));
                        if(selectedFieldType == RF_TYPE_DATE || selectedFieldType == RF_TYPE_PART_DATE){
                            backboneExpressions.at(expression.expressionIndex).set("value", $(getValueDateInputFromExpression(container))[0].value);
                        } else {
                            backboneExpressions.at(expression.expressionIndex).set("value", "");
                        }

                        if (field === REASSESS_LISTEDNESS_FIELD || field === REASSESS_LISTEDNESS_FIELD_J) {
                            backboneExpressions.at(expression.expressionIndex).set(RLDS_KEY, "");
                            backboneExpressions.at(expression.expressionIndex).set(RLDS_OPDS, null);
                        } else {
                            backboneExpressions.at(expression.expressionIndex).unset(RLDS_KEY);
                            backboneExpressions.at(expression.expressionIndex).unset(RLDS_OPDS);
                        }
                    }
                }

                clearValues(container);

                // Remove error outline if we have a value
                if (field != '') {
                    $(this.parentElement).removeClass('has-error');
                }

                $queryJSON.val(printAll());
            });

            $('#mainContent').on('change', '.expressionOp', function () {
                var container = $(this).closest('.toAddContainer');
                var expression = container.closest('.expression')[0];
                var value = $(this)[0].value;

                updateAJAXOperators(container, value);

                // Update backbone model
                if (expression && (expression != undefined) && (expression.expressionIndex != null)) {
                    backboneExpressions.at(expression.expressionIndex).set("op", value);

                    var valueless = false;
                    $.each(AJAXOperatorValuelessList, function (i, obj) {
                        if (obj != null) {
                            if (obj.value == value) {
                                valueless = true;
                                return false;
                            }
                        }
                    });


                    if (valueless) {
                        backboneExpressions.at(expression.expressionIndex).set("value", value);
                    } else {
                        backboneExpressions.at(expression.expressionIndex).set("value", "");
                    }
                }

                clearValues(container);

                $queryJSON.val(printAll());
            });

            $('#mainContent').on('change', '.expressionValueText', function () {
                var container = $(this).closest('.toAddContainer');
                var expression = container.closest('.expression')[0];
                // Update backbone model
                if (expression) {
                    if ((expression != undefined) && (expression.expressionIndex != null)) {
                        backboneExpressions.at(expression.expressionIndex).set("value", $(getValueTextFromExpression(container))[0].value);
                    }
                }

                // Remove error outline if we have a value
                if ($(this)[0].value != '') {
                    $(this.parentElement).removeClass('has-error');
                }

                $queryJSON.val(printAll());
            });

            // Multiselect implementation
            $('#mainContent').on('change', '.expressionValueSelect', function () {
                var container = $(this).closest('.toAddContainer');
                var expression = $(container).closest('.expression')[0];

                var value = $(getValueSelectFromExpression(container)).select2('val');
                if (Array.isArray(value)) {
                    value = $.grep(value, function (item) {
                        if (item != "") {
                            return item;
                        }
                    });
                    if (value.length == 0) {
                        value = "";
                    }
                }

                // Update backbone model
                if (expression) {
                    if ((expression != undefined) && (expression.expressionIndex != null)) {
                        if (Array.isArray(value)) {
                            var concatValues = '';
                            for (var i = 0; i < value.length; i++) {
                                if (i != 0) {
                                    // Multiselect Select2
                                    concatValues += ';' + value[i];
                                } else {
                                    concatValues += value[i];
                                }
                            }
                            value = concatValues;
                        }
                        backboneExpressions.at(expression.expressionIndex).set("value", value);
                    }
                }

                $queryJSON.val(printAll());
            });

            // select implementation
            $('#mainContent').on('change', '.expressionValueRptField', function () {
                var container = $(this).closest('.toAddContainer');
                var expression = $(container).closest('.expression')[0];

                var value = $(getReportFiledSelectFromExpression(container)).select2('val');
                if (Array.isArray(value)) {
                    value = $.grep(value, function (item) {
                        if (item != "") {
                            return item;
                        }
                    });
                    if (value.length == 0) {
                        value = "";
                    }
                }

                // Update backbone model
                if (expression) {
                    if ((expression != undefined) && (expression.expressionIndex != null)) {
                        if (Array.isArray(value)) {
                            var concatValues = '';
                            for (var i = 0; i < value.length; i++) {
                                if (i != 0) {
                                    // Multiselect Select2
                                    concatValues += ';' + value[i];
                                } else {
                                    concatValues += value[i];
                                }
                            }
                            value = concatValues;
                        }
                        backboneExpressions.at(expression.expressionIndex).set("value", value);
                    }
                }

                $queryJSON.val(printAll());
            });
            // Multiselect implementation
            $('#mainContent').on('change', '.expressionValueSelectAuto', function () {
                var container = $(this).closest('.toAddContainer');
                var expression = $(container).closest('.expression')[0];

                var value = $(getValueSelectAutoFromExpression(container)).select2('val');
                if (Array.isArray(value)) {
                    value = $.grep(value, function (item) {
                        if (item != "") {
                            return item;
                        }
                    });
                    if (value.length == 0) {
                        value = "";
                    }
                }

                // Update backbone model
                if (expression) {
                    if ((expression != undefined) && (expression.expressionIndex != null)) {
                        if (Array.isArray(value)) {
                            var concatValues = '';
                            for (var i = 0; i < value.length; i++) {
                                if (i != 0) {
                                    // Multiselect Select2
                                    concatValues += ';' + value[i];
                                } else {
                                    concatValues += value[i];
                                }
                            }
                            value = concatValues;
                        }
                        backboneExpressions.at(expression.expressionIndex).set("value", value);
                    }
                }

                $queryJSON.val(printAll());
            });

            // Multiselect implementation
            $('#mainContent').on('change', '.expressionValueSelectNonCache', function () {
                var container = $(this).closest('.toAddContainer');
                var expression = $(container).closest('.expression')[0];

                var value = $(getValueSelectNonCacheFromExpression(container)).select2('val');
                if (Array.isArray(value)) {
                    value = $.grep(value, function (item) {
                        if (item != "") {
                            return item;
                        }
                    });
                    if (value.length == 0) {
                        value = "";
                    }
                }

                // Update backbone model
                if (expression) {
                    if ((expression != undefined) && (expression.expressionIndex != null)) {
                        if (Array.isArray(value)) {
                            var concatValues = '';
                            for (var i = 0; i < value.length; i++) {
                                if (i != 0) {
                                    // Multiselect Select2
                                    concatValues += ';' + value[i];
                                } else {
                                    concatValues += value[i];
                                }
                            }
                            value = concatValues;
                        }
                        backboneExpressions.at(expression.expressionIndex).set("value", value);
                    }
                }

                $queryJSON.val(printAll());
            });

            // Multiselect implementation
            $('#mainContent').on('change', '.reassessListednessDS', function () {
                var container = $(this).closest('.toAddContainer');
                var expression = $(container).closest('.expression')[0];

                var value = $(getExtraValueRLDSSelectFromContainer(container)).val();
                if (Array.isArray(value)) {
                    value = $.grep(value, function (item) {
                        if (item != "") {
                            return item;
                        }
                    });
                    if (value.length == 0) {
                        value = "";
                    }
                }

                // Update backbone model
                if (expression) {
                    if ((expression != undefined) && (expression.expressionIndex != null)) {
                        if (Array.isArray(value)) {
                            var concatValues = '';
                            for (var i = 0; i < value.length; i++) {
                                if (i != 0) {
                                    // Multiselect Select2
                                    concatValues += ';' + value[i];
                                } else {
                                    concatValues += value[i];
                                }
                            }
                            value = concatValues;
                        }
                        backboneExpressions.at(expression.expressionIndex).set(RLDS_KEY, value);
                        updateOnPrimaryDatasheetCheckbox(container);
                        backboneExpressions.at(expression.expressionIndex).set(RLDS_OPDS, getOnPrimaryDatasheetValue(container));
                    }
                }

                $queryJSON.val(printAll());
            });

            $("#mainContent").on("change", "#onPrimaryDatasheet", function (e) {
                var container = $(this).closest('.toAddContainer');
                var expression = $(container).closest('.expression')[0];
                if ((expression != undefined) && (expression.expressionIndex != null)) {
                    backboneExpressions.at(expression.expressionIndex).set(RLDS_OPDS, getOnPrimaryDatasheetValue(container));
                }
                $queryJSON.val(printAll());
            });
            $(".addContainerTopmost").on("change", ".reassessListednessDS", function (e) {
                updateOnPrimaryDatasheetCheckbox($(this).closest('.toAddContainer'));

            });
            // setBuilder
            $('#mainContent').on('change', '.expressionQuery', function () {
                var container = this.parentElement.parentElement;
                var expression = container.parentElement;

                var queryId = $(this)[0].value;

                // Update backbone model
                if ((expression != undefined) && (expression.expressionIndex != null)) {
                    backboneSets.at(expression.expressionIndex).set("query", queryId);
                }

                if (queryId != '') {
                    $(this.parentElement).removeClass("has-error");
                }
                $queryJSON.val(printAll());
            });

            // setBuilder
            $('#mainContent').on('change', '.keywordExpression', function () {
                var expression = this.parentElement;
                var expressionGroup = expression.parentElement;

                // check it we have more than one expression
                var expressionsNum = getExpressionOrGroupCountFromGroup(expressionGroup);

                var changed = false;

                // No need to do anything if the change is between 2 expressions
                if (expressionsNum > 2) {
                    // Compare to the first expression's keyword if it is not the first keyword
                    // keywordIndex and firstExpressionIndex should have a value since expressionCount > 1
                    var firstExpressionIndex = getFirstChildWithClassnameIndexFromElement('expression', expressionGroup);
                    var keywordIndex = getKeywordIndexFromElement(expressionGroup.children[firstExpressionIndex]);


                    if (this !== expressionGroup.children[firstExpressionIndex].children[keywordIndex]) {
                        if ($(this).val() != $(expressionGroup.children[firstExpressionIndex].children[keywordIndex]).val()) {
                            changed = true;
                        }
                    }
                    // Compare to the second if we are changing the first expression's keyword. We should not need to compare the case when we are comparing the last keyword, because the keyword select box isn't present.
                    else {
                        var secondExpressionIndex = getSecondChildWithClassnameIndexFromElement('expression', expressionGroup);
                        // If we have one expression and the rest are groups, use the first group's index instead of the second expression
                        if (secondExpressionIndex == -1) {
                            secondExpressionIndex = getFirstChildWithClassnameIndexFromElement('group', expressionGroup);
                        }
                        var secondKeywordIndex = getKeywordIndexFromElement(expressionGroup.children[secondExpressionIndex]);
                        if ($(this).val() != $(expressionGroup.children[secondExpressionIndex].children[secondKeywordIndex]).val()) {
                            changed = true;
                        }
                    }

                    if (changed) {
                        var expressionIndex = getChildIndexFromParent(expression, expressionGroup);

                        // second half
                        var secondHalfGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroup, expressionIndex + 1, getLastExpressionOrGroupIndexFromGroup(expressionGroup));
                        expressionGroup.insertBefore(secondHalfGroup, expressionGroup.children[expressionIndex].nextSibling);

                        // first half
                        var firstHalfGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroup, 0, expressionIndex);
                        expressionGroup.insertBefore(firstHalfGroup, expressionGroup.firstChild);

                        // alter first half's keyword
                        var expressionKeyword = expression.children[getKeywordIndexFromElement(expression)];
                        addKeyword(firstHalfGroup, expressionKeyword.value, 'keywordGroup');

                        cleanUpKeywords(firstHalfGroup);
                        cleanUpKeywords(secondHalfGroup);
                        cleanUpKeywordGroups(expressionGroup);
                    }
                }

                printToJSON();
            });

            $('#mainContent').on('change', '.keywordGroup', function () {
                var expressionGroup = this.parentElement;
                var expressionGroupContainer = expressionGroup.parentElement;

                // check it we have more than one expression or group
                var expressionsNum = getExpressionOrGroupCountFromGroup(expressionGroupContainer);

                var changed = false;

                // No need to do anything if the change is between 2 expressions
                if (expressionsNum > 2) {
                    // Compare to the first group's keyword if it is not the first keyword
                    // keywordIndex and firstGroupIndex should have a value since expressionCount > 1
                    var firstGroupIndex = getFirstChildWithClassnameIndexFromElement('group', expressionGroupContainer);
                    var keywordIndex = getKeywordIndexFromElement(expressionGroupContainer.children[firstGroupIndex]);

                    if (this !== expressionGroupContainer.children[firstGroupIndex].children[keywordIndex]) {
                        if ($(this).val() != $(expressionGroupContainer.children[firstGroupIndex].children[keywordIndex]).val()) {
                            changed = true;
                        }
                    }
                    // Compare to the second if we are changing the first group's keyword. We should not need to compare the case when we are comparing the last keyword, because the keyword select box isn't present.
                    else {
                        var secondGroupIndex = getSecondChildWithClassnameIndexFromElement('group', expressionGroupContainer);
                        var secondKeywordIndex = getKeywordIndexFromElement(expressionGroupContainer.children[secondGroupIndex]);
                        if ($(this).val() != $(expressionGroupContainer.children[secondGroupIndex].children[secondKeywordIndex]).val()) {
                            changed = true;
                        }
                    }

                    if (changed) {
                        var groupIndex = getChildIndexFromParent(expressionGroup, expressionGroupContainer);

                        // second half
                        var secondHalfGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroupContainer, groupIndex + 1, getLastExpressionOrGroupIndexFromGroup(expressionGroupContainer));
                        expressionGroupContainer.insertBefore(secondHalfGroup, expressionGroupContainer.children[groupIndex].nextSibling);

                        // first half
                        var firstHalfGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroupContainer, 0, groupIndex);
                        expressionGroupContainer.insertBefore(firstHalfGroup, expressionGroupContainer.firstChild);

                        // alter first half's keyword
                        var expressionKeyword = expressionGroup.children[getKeywordIndexFromElement(expressionGroup)];
                        addKeyword(firstHalfGroup, expressionKeyword.value, 'keywordGroup');

                        cleanUpKeywordGroups(firstHalfGroup);
                        cleanUpKeywordGroups(secondHalfGroup);
                        cleanUpKeywordGroups(expressionGroupContainer);
                    }
                }

                printToJSON();
            });

            // ---------------------------------------------------------------------------------------------------- END ONCHANGE

            // On click methods ---------------------------------------------------------------------------------------- onClick

            $('#mainContent').on('click', '.removeExpression', function () {
                var expressionGroup = this.parentElement.parentElement;
                var affectIndex = this.parentElement.expressionIndex;
                backboneExpressions.remove(backboneExpressions.at(affectIndex));
                backboneSets.remove(backboneSets.at(affectIndex));
                checkIndexes(affectIndex);
                $(this.parentElement).remove();

                if (expressionGroup.children.length > 0) {
                    cleanUpKeywords(expressionGroup);
                }

                // Re-assess Listedness
                showHideReassessListedness(builderAll);

                printToJSON();
            });

            $('#mainContent').on('click', '.removeGroup', function () {
                var groupContainer = this.parentElement;
                var topmostContainer = groupContainer.parentElement;
                var removedExpressions = $(groupContainer).find('.expression');
                var affectIndex;
                for (var i = 0; i < removedExpressions.length; i++) {
                    affectIndex = removedExpressions[i].expressionIndex;
                    backboneExpressions.remove(backboneExpressions.at(affectIndex));
                    checkIndexes(affectIndex);
                }
                $(this.parentElement).remove();

                if (groupContainer.children.length > 0) {
                    cleanUpKeywordGroups(groupContainer);
                    cleanUpKeywords(groupContainer);
                }

                cleanUpKeywordGroups(topmostContainer);
                cleanUpKeywords(topmostContainer);

                // Re-assess Listedness
                showHideReassessListedness(builderAll);

                printToJSON();
            });


            $("body").on("click", ".rptFieldInput, .poiInput", function () {
                var container = $(this).closest(".toAddContainer");
                if ($(this).prop('checked')) {
                    if ($(this).hasClass('rptFieldInput')) {
                        container.find('.poiInput').prop('checked', false);
                    } else {
                        container.find('.rptFieldInput').prop('checked', false);
                    }
                }
                updateAJAXValues(container);
            });

            // Add button to add a new expression
            $('#addExpression').on('click', function () {
                $('#errorMessageOperator').hide();
                var $addExpression = true;
                if ($selectedField.val() === '') {
                    $selectedField.parent().addClass('has-error');
                    $addExpression = false;
                }

                if ($addExpression) {
                    var baseContainer = document.getElementById('toAddContainer');
                    var field = $(getFieldFromExpression(baseContainer)).select2("val");
                    var selectedFieldType = getFieldType($(getFieldFromExpression(baseContainer)).find(":selected"));
                    var op = $(getOperatorFromExpression(baseContainer))[0].value;
                    var value;
                    var arrayData = []; // This is used for autocomplete fields
                    var arrayDataNonCache = []; // This is used for NonCachecomplete fields
                    var isDate = false;
                    if ($($(baseContainer).find('.expressionValueText')[0]).is(":visible")) {
                        if (selectedFieldType == RF_TYPE_DATE) {
                            value = $($(baseContainer).find('.expressionValueText')[0])[0].value;
                            if (!_.isEmpty(value) && !Number.isNaN(value)) {
                                var isPOIInput = $(baseContainer).find('.poiInput').is(':checked');
                                if (value <= 0 || (!isPOIInput && isNaN(value))) {
                                    $('#errorMessageOperator').show();
                                    return false;
                                }
                            }
                        } else {
                            value = $($(baseContainer).find('.expressionValueText')[0])[0].value;
                        }
                    } else if ($($(baseContainer).find('.expressionValueSelect')[0]).is(":visible")) {
                        value = $(getValueSelectFromExpression(baseContainer)).select2("val");
                        if (Array.isArray(value)) {
                            value = $.grep(value, function (item) {
                                return item != ""
                            });
                        }
                    } else if ($($(baseContainer).find('.expressionValueRptField')[0]).is(":visible")) {
                        value = $(getReportFiledSelectFromExpression(baseContainer)).select2("val");
                        if (Array.isArray(value)) {
                            value = $.grep(value, function (item) {
                                return item != ""
                            });
                        }
                    } else if ($($(baseContainer).find('.expressionValueSelectAuto')[0]).is(":visible")) {
                        value = $(getValueSelectAutoFromExpression(baseContainer)).select2("val");
                        if (Array.isArray(value)) {
                            value = $.grep(value, function (item) {
                                return item != ""
                            });
                            _.each(value, function (item) {
                                arrayData.push({id: item, text: item});
                            });
                        }
                    } else if ($($(baseContainer).find('.expressionValueSelectNonCache')[0]).is(":visible")) {
                        value = $(getValueSelectNonCacheFromExpression(baseContainer)).select2("val");
                        if (Array.isArray(value)) {
                            value = $.grep(value, function (item) {
                                return item != ""
                            });
                            _.each(value, function (item) {
                                arrayDataNonCache.push({id: item, text: item});
                            });
                        }
                    } else if ($($(baseContainer).find('.expressionValueDateInput')[0]).is(":visible")) {
                        value = $($(baseContainer).find('.expressionValueDateInput')[0])[0].value;
                        isDate = true;
                    } else {
                        value = $($(baseContainer).find('.expressionOp')[0])[0].value;
                    }

                    //Ensure valid input for Last X types of values
                    if (op.search('LAST_X') > -1) {
                        var checkedNumber = checkNumberFields(value, baseContainer);
                        if (!checkedNumber) {
                            return false;
                        }
                    }

                    // destroy select2 as it has issues when cloning, reinitialize after
                    $('#selectDataSourceField').select2("destroy");
                    $('#selectSelect').select2("destroy");
                    $('#selectRptField').select2("destroy");
                    $(getExtraValueRLDSSelectFromContainer(baseContainer)).select2("destroy");
                    $('#selectSelectAuto').select2("destroy");
                    $('#selectSelectNonCache').select2("destroy");

                    var containerToAdd = baseContainer.cloneNode(true);
                    $(containerToAdd).removeAttr("id");
                    $(getValueDateFromExpression(containerToAdd)).datepicker({
                        allowPastDates: true,
                        textAllowed: true,
                        momentConfig: {
                            culture: userLocale,
                            format: DEFAULT_DATE_DISPLAY_FORMAT
                        }
                    });

                    if (isDate) {
                        $(getValueDateFromExpression(containerToAdd)).datepicker('setDate', value);
                    }
                    if (value == null || value == '') {
                        $(getValueDateInputFromExpression(containerToAdd)).val('');
                    }
                    addOnchangeMethodsToDatepickerElement(getValueDateFromExpression(containerToAdd));

                    var extraValues = {};

                    // Re-assess Listedness
                    var RLDSSelect = getExtraValueRLDSSelectFromContainer(baseContainer);
                    if (RLDSSelect) {
                        var RLDSValue = $(RLDSSelect).val();
                        if (Array.isArray(RLDSValue)) {
                            RLDSValue = $.grep(RLDSValue, function (item) {
                                return item != ""
                            });
                        }
                        extraValues[RLDS_KEY] = RLDSValue;
                        extraValues[RLDS_OPDS] = getOnPrimaryDatasheetValue(baseContainer);
                    }

                    var expressionGroup;
                    if ($(builderAll).find('.selectedGroup').length > 0) {
                        expressionGroup = selectedGroup;
                    } else {
                        var groupList = $(builderAll).find('.group');
                        if (groupList.length == 0) {
                            expressionGroup = createGroup();
                            builderAll.insertBefore(expressionGroup, builderAll.children[0]);
                        } else {
                            expressionGroup = groupList[0];
                        }

                        selectedGroup = expressionGroup;
                        $(selectedGroup).addClass('selectedGroup');
                    }

                    // The expression is added to the last existing keyword, or at the beginning if no previous keywords exist. Expressions should always appear before groups.
                    // check it we have more than one expression
                    var lastExpression = getLastChildWithClassnameIndexFromElement('expression', expressionGroup);

                    if (lastExpression > -1) {
                        expressionGroup.insertBefore(createExpressionFromInput(containerToAdd, field, op, value, extraValues), expressionGroup.children[lastExpression].nextSibling);
                    } else {
                        if (expressionGroup.children.length > 0) {
                            expressionGroup.insertBefore(createExpressionFromInput(containerToAdd, field, op, value, extraValues), expressionGroup.children[0]);
                        }
                    }

                    $(getFieldFromExpression(containerToAdd)).removeAttr('id');
                    $(getFieldFromExpression(containerToAdd)).select2();
                    $(getValueSelectFromExpression(containerToAdd)).removeAttr('id');
                    $(getValueSelectFromExpression(containerToAdd)).select2({closeOnSelect: false});
                    upgradeQueryMultiSelect2(getValueSelectFromExpression(containerToAdd), null);
                    $(getReportFiledSelectFromExpression(containerToAdd)).removeAttr('id');
                    $(getReportFiledSelectFromExpression(containerToAdd)).select2();
                    $(getExtraValueRLDSSelectFromContainer(containerToAdd)).removeAttr('id');;
                    $(getExtraValueRLDSSelectFromContainer(containerToAdd)).select2();
                    getSelect2TreeView($("#selectDataSourceField"));
                    getSelect2TreeView($(expressionGroup).find(".expressionField"));
                    $('#selectSelect').select2({closeOnSelect: false});
                    upgradeQueryMultiSelect2('#selectSelect', null);
                    getSelect2TreeView($('#selectRptField'));
                    $(getExtraValueRLDSSelectFromContainer(baseContainer)).select2();
                    $(getValueSelectAutoFromExpression(containerToAdd)).removeAttr('id');
                    $(getValueSelectAutoFromExpression(containerToAdd)).select2({
                        minimumInputLength: 2,
                        multiple: true,
                        separator: ";",
                        closeOnSelect: false,
                        ajax: {
                            delay: 300,
                            dataType: "json",
                            url: selectAutoUrl,
                            data: function (params) {
                                var container = $(this).closest('.toAddContainer')[0];
                                var field = $(getFieldFromExpression(container)).select2("val");
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
                    upgradeQueryMultiSelect2(getValueSelectAutoFromExpression(containerToAdd), selectAutoUrl);
                    $(getValueSelectAutoFromExpression(containerToAdd)).val(arrayData);
                    $(getValueSelectNonCacheFromExpression(containerToAdd)).removeAttr('id');

                    $(getValueSelectNonCacheFromExpression(containerToAdd)).select2({
                        minimumInputLength: 0,
                        multiple: true,
                        separator: ";",
                        closeOnSelect: false,
                        ajax: {
                            delay: 300,
                            dataType: "json",
                            url: selectNonCacheUrl,
                            data: function (params) {
                                var container = $(this).closest('.toAddContainer')[0];
                                var field = $(getFieldFromExpression(container)).select2("val");
                                return {
                                    field: field,
                                    term: params.term,
                                    max: 30,
                                    lang: userLocale,
                                    page: params.page,
                                    isFaersTarget: isFaersTarget
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
                    upgradeQueryMultiSelect2(getValueSelectNonCacheFromExpression(containerToAdd), selectNonCacheUrl);

                    $('#selectSelectAuto').select2({
                        minimumInputLength: 2,
                        separator: ";",
                        closeOnSelect: false,
                        multiple: true,
                        ajax: {
                            delay: 300,
                            dataType: "json",
                            url: selectAutoUrl,
                            data: function (params) {
                                var container = $(this).closest('.toAddContainer')[0];
                                var field = $(getFieldFromExpression(container)).select2("val");
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
                    upgradeQueryMultiSelect2('#selectSelectAuto', selectAutoUrl);
                    $('#selectSelectAuto').val(null).trigger('change');

                    $('#selectSelectNonCache').select2({
                        minimumInputLength: 0,
                        separator: ";",
                        multiple: true,
                        closeOnSelect: false,
                        ajax: {
                            delay: 300,
                            dataType: "json",
                            url: selectNonCacheUrl,
                            data: function (params) {
                                var container = $(this).closest('.toAddContainer')[0];
                                var field = $(getFieldFromExpression(container)).select2("val");
                                return {
                                    field: field,
                                    term: params.term,
                                    max: 30,
                                    lang: userLocale,
                                    page: params.page,
                                    isFaersTarget: isFaersTarget
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
                    upgradeQueryMultiSelect2('#selectSelectNonCache', selectNonCacheUrl);
                    $('#selectSelectNonCache').val(null).trigger('change');

                    // This removes the empty value "" that gets appended when we destroy and then reinitialize this as select2.
                    var selectArray = $('#selectSelect').select2("val");
                    for (var i = selectArray.length; i--;) {
                        if (selectArray[i] === "") {
                            selectArray.splice(i, 1);
                        }
                    }

                    $('#selectSelect').val(selectArray).trigger('change');

                    cleanUpKeywords(expressionGroup);

                    printToJSON();

                    // Re-assess Listedness
                    showHideReassessListedness(builderAll);
                    $(containerToAdd).find('[data-toggle="popover"]').removeAttr("id");
                    $(containerToAdd).find('#addExpression').remove();
                    $('[data-toggle="popover"]').popover()
                }
            });

            // setBuilder
            // Add button to add a new expression
            $('#addQuery').on('click', function () {
                var hasValue = true;
                if (!$selectedQuery.val()) {
                    $selectedQuery.parent().addClass('has-error');
                    hasValue = false;
                }

                if (hasValue) {
                    var baseContainer = document.getElementById('toAddContainerSet');
                    var queryValue = $selectedQuery.select2("val");

                    // destroy select2 as it has issues when cloning, reinitialize after
                    $selectedQuery.select2("destroy");

                    var containerToAdd = baseContainer.cloneNode(true);
                    $(containerToAdd).removeAttr("id");

                    var expressionGroup;
                    if ($(setBuilderAll).find('.selectedGroup').length > 0) {
                        expressionGroup = selectedSetGroup;
                    } else {
                        var groupList = $(setBuilderAll).find('.group');
                        if (groupList.length == 0) {
                            expressionGroup = createGroup();
                            setBuilderAll.insertBefore(expressionGroup, setBuilderAll.children[0]);
                        } else {
                            expressionGroup = groupList[0];
                        }

                        selectedSetGroup = expressionGroup;
                        $(selectedSetGroup).addClass('selectedGroup');
                    }


                    // The expression is added to the last existing keyword, or at the beginning if no previous keywords exist. Expressions should always appear before groups.
                    // check it we have more than one expression
                    var lastExpression = getLastChildWithClassnameIndexFromElement('expression', expressionGroup);

                    if (lastExpression > -1) {
                        expressionGroup.insertBefore(createSetExpressionFromInput(containerToAdd, queryValue), expressionGroup.children[lastExpression].nextSibling);
                    } else {
                        if (expressionGroup.children.length > 0) {
                            expressionGroup.insertBefore(createSetExpressionFromInput(containerToAdd, queryValue), expressionGroup.children[0]);
                        }
                    }

                    bindQuerySelect2($(getQueryFromExpression(containerToAdd)));
                    bindQuerySelect2($selectedQuery);

                    cleanUpKeywords(expressionGroup);

                    $('#setJSON').val(printSet());
                }
            });


            // Below methods are unused but kept for posterity
            /**

             // Add a new group in topmost container
             $(document).on('click', '.containerAddGroup', function () {
             var expressionGroup = document.getElementById('builderAll');

             var lastExpression = getLastExpressionOrGroupIndexFromGroup(expressionGroup);

             if (lastExpression == -1) {
             expressionGroup.insertBefore(createGroup(), expressionGroup.children[0]);
             }
             else {
             expressionGroup.insertBefore(createGroup(), expressionGroup.children[lastExpression].nextSibling);
             }

             cleanUpKeywordGroups(expressionGroup);
             cleanUpKeywords(expressionGroup);

             $('#post-data').val(printAll());
             });

             // Add a new group
             $(document).on('click', '.addGroup', function () {
             var expressionGroup = this.parentElement;

             var lastExpression = getLastExpressionOrGroupIndexFromGroup(expressionGroup);

             if (lastExpression == -1) {
             expressionGroup.insertBefore(createGroup(), expressionGroup.children[0]);
             }
             else {
             expressionGroup.insertBefore(createGroup(), expressionGroup.children[lastExpression].nextSibling);
             }

             cleanUpKeywordGroups(expressionGroup);
             cleanUpKeywords(expressionGroup);

             $('#post-data').val(printAll());
             });

             */

            // ----------------------------------------------------------------------------------------------------- END ONCLICK
        });

    $(document).on('mouseover', ".iPopover", function () {
        var popoverContainer = $('.popover-content');
        var content = decodeFromHTML(popoverContainer.html());
        if (content && content.length > 0) {
            content = decodeFromHTML(content);
            popoverContainer.html(content);
        }
    });

    $(document).on("data-clk", function (event, elem) {
        const elemClkData = JSON.parse(elem.attributes["data-evt-clk"].value)
        const methodName = elemClkData.method;
        const params = elemClkData.params;
        if (methodName == 'showWarning') {
            return showWarning(...params);
        }
    });

    $("[data-evt-sbt]").on('submit', function() {
        const eventData = JSON.parse($(this).attr("data-evt-sbt"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'finalizeForm') {
            return finalizeForm();
        }
    });

});

// JSON building and parsing ---------------------------------------------------------------------------------- JSON

function printAll() {
    hasBlanks = false;
    keys = [];

    var container = $(document).find('.builderAll')[0];

    var result = '{"all":{"containerGroups":[' + printRecurJSON(container);

    if (hasBlanks) {
        result += ',"blankParameters":['
        for (var i = 0; i < keys.length; i++) {
            if (i > 0) {
                result += ',';
            }
            result += '{' + keys[i] + '}';
        }
        result += ']';
    }

    result += '}';

//        try {
//            JSON.parse(result);
//            console.log(true);
//        } catch (e) {
//            console.log(false);
//        }

    $('#hasBlanksQuery').val("" + hasBlanks);

    return result;
}

// Helper method, shouldn't be called directly
function printRecurJSON(expressionGroup) {
    var tempResult = " ";
    var keywordValueForGroup;
    var lastExpressionOrGroupIndex = getLastExpressionOrGroupIndexFromGroup(expressionGroup);

    var firstExpressionIndex = -1;
    var firstGroupIndex = -1;

    var needComma = false;

    for (var i = 0; i < expressionGroup.children.length; i++) {
        if (expressionGroup.children[i].classList.contains('expression')) {
            var tempExpression = backboneExpressions.at(expressionGroup.children[i].expressionIndex);
            var op = tempExpression.get('op');
            if (_.contains(IS_EMPTY_OPERATORS, op) || _.contains(RELATIVE_DATE_OPERATORS, op)) {
                tempExpression.set('value', op);
            } else {
                tempExpression.set('value', tempExpression.get('value'));
            }
            tempResult += '{"index":"' + expressionGroup.children[i].expressionIndex
                + '","field":"' + tempExpression.get('field')
                + '","op":"' + op
                + '","value":"' + tempExpression.get('value')
                + '"';

            var copyAndPasteWithDelimiter = $(expressionGroup.children[i]).find(".expressionValueText").attr("copyAndPasteWithDelimiter");
            if (!_.isEmpty(copyAndPasteWithDelimiter))
                tempResult += ',"copyAndPasteWithDelimiter":"' + copyAndPasteWithDelimiter + '"';

            // Extra values
            // Re-assess Listedness
            if (tempExpression.has(RLDS_KEY)) {
                tempResult += ',"RLDS":"' + tempExpression.get(RLDS_KEY) + '","RLDS_OPDS":"' + tempExpression.get(RLDS_OPDS) + '"';
            }
            //TODO CUSTOM_INPUT LOGIC
            if (tempExpression.get('value') == null || $.trim(tempExpression.get('value')) == '' || tempExpression.get('value').match(/^\&/)) {
                hasBlanks = true;
                keys.push('"key":' + (keys.length + 1)
                    + ',"field":"' + tempExpression.get('field')
                    + '","op":"' + op
                    + '","value":"' + tempExpression.get('value')
                    + '"');
                tempResult += ',"key":"' + keys.length + '"';
            }
            tempResult += '}';
            if (firstExpressionIndex == -1) {
                firstExpressionIndex = i;
            }
            needComma = true;
        } else if (expressionGroup.children[i].classList.contains('group')) {
            tempResult += '{"expressions":[' + printRecurJSON(expressionGroup.children[i]);
            if (firstGroupIndex == -1) {
                firstGroupIndex = i;
            }
            needComma = true;
        }
        if (needComma && i < lastExpressionOrGroupIndex) {
            tempResult += ", ";
            needComma = false;
        }
    }
    // One keyword per group, if we have a keyword
    if (firstExpressionIndex != -1) {
        var keywordIndex = getKeywordIndexFromElement(expressionGroup.children[firstExpressionIndex]);
        if (keywordIndex != -1) {
            keywordValueForGroup = expressionGroup.children[firstExpressionIndex].children[keywordIndex].value;
        }
        if (keywordValueForGroup) {
            return tempResult + '],"keyword":"' + keywordValueForGroup + '"}';
        }
    } else if (firstExpressionIndex == -1 && firstGroupIndex != -1) {
        var keywordIndex = getKeywordIndexFromElement(expressionGroup.children[firstGroupIndex]);
        if (keywordIndex != -1) {
            keywordValueForGroup = expressionGroup.children[firstGroupIndex].children[keywordIndex].value;
        }
        if (keywordValueForGroup) {
            return tempResult + '],"keyword":"' + keywordValueForGroup + '"}';
        }
    }
    return tempResult + ' ] } ';
}

function buildQueryFromJSON(JSONQuery) {
    var parsedJSON = JSON.parse(JSONQuery.replaceAll("\\", "\\\\"));

    // all is the upper container just for holding the json object.
    var all = parsedJSON["all"];

    // this is the list of all the groups to render in builderAll, the topmost level
    var containerGroups = all["containerGroups"];

    var containerKeyword = all["keyword"];

    containerGroups.forEach(function (group) {
        var addGroup = createGroup();

        var lastExpressionOrGroupIndex = getLastExpressionOrGroupIndexFromGroup(builderAll);
        if (lastExpressionOrGroupIndex != -1) {
            builderAll.insertBefore(addGroup, builderAll.children[lastExpressionOrGroupIndex].nextSibling);
        } else {
            builderAll.insertBefore(addGroup, builderAll.firstChild);
        }

        group.expressions.forEach(function (nextGroup) {
            parseGroup(addGroup, nextGroup, group.keyword);
        });

        if (containerKeyword) {
            addKeyword(addGroup, containerKeyword, 'keywordGroup');
        }

        cleanUpKeywords(addGroup);
        cleanUpKeywordGroups(addGroup);
    });

    cleanUpKeywordGroups(builderAll);
    $('[data-toggle="popover"]').popover();
}

// Helper method, shouldn't be called directly
function parseGroup(groupElement, groupJSON, keyword) {
    if (groupJSON.expressions) {
        var addGroup = createGroup();

        var lastExpressionOrGroupIndex = getLastExpressionOrGroupIndexFromGroup(groupElement);
        if (lastExpressionOrGroupIndex != -1) {
            groupElement.insertBefore(addGroup, groupElement.children[lastExpressionOrGroupIndex].nextSibling);
        } else {
            groupElement.insertBefore(addGroup, groupElement.firstChild);
        }

        groupJSON.expressions.forEach(function (group) {
            parseGroup(addGroup, group, groupJSON.keyword);
        });

        if (keyword) {
            addKeyword(addGroup, keyword, 'keywordGroup');
        }

        cleanUpKeywords(addGroup);
        cleanUpKeywordGroups(addGroup);
    } else {
        var expression = document.createElement('div');
        expression.className = 'expression';
        var container = document.getElementById('toAddContainer').cloneNode(true);
        $(container).removeAttr("id");
        $(getValueDateFromExpression(container)).datepicker({
            allowPastDates: true,
            textAllowed: true,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });
        addOnchangeMethodsToDatepickerElement(getValueDateFromExpression(container));
        expression.appendChild(container);

        var value;
        if (_.contains(IS_EMPTY_OPERATORS, groupJSON.op)) {
            value = groupJSON.op;
        } else {
            value = groupJSON.value;
        }

        if (isSpecialKeyValue(value)) {
            $(getPOICheckBoxFromExpression(container)).attr('checked', 'checked');
        }

        if (isReportFieldKeyValue(value)) {
            $(getRptInputCheckBoxFromExpression(container)).attr('checked', 'checked');
        }

        var backboneExpression = new Expression({field: groupJSON.field, op: groupJSON.op, value: value});
        if (RLDS_KEY in groupJSON) {
            addShowRLDS(container, groupJSON[RLDS_KEY], null);
            $(getExtraValueRLDSSelectFromContainer(container)).val(groupJSON[RLDS_KEY]).trigger('change');
            setOnPrimaryDatasheetValue(container, groupJSON[RLDS_OPDS])
            backboneExpression.set(RLDS_KEY, groupJSON[RLDS_KEY]);
            backboneExpression.set(RLDS_OPDS, (_.isEmpty(groupJSON[RLDS_OPDS]) ? false : groupJSON[RLDS_OPDS]));
        }
        backboneExpressions.add(backboneExpression);

        // text values
        getValueTextFromExpression(expression).value = value;
        if (groupJSON.copyAndPasteWithDelimiter)
            getValueTextFromExpression(expression).setAttribute("copyAndPasteWithDelimiter", "true");
        // select2 values
        var $field = $(getFieldFromExpression(container));
        $field.removeAttr('id');
        $field.val(groupJSON.field);

        $field.select2();
        var popover = $field.parent().find('[data-toggle="popover"]');
        var content = $field.find('option:selected').attr("data-description");
        if (content)
            popover.attr("data-content", content);
        else
            popover.attr("data-content", $.i18n._("app.query.field.no.description"));
        popover.removeAttr("id");
        var dictionary = $field.find('option:selected').attr("data-dictionary");
        showHideDictionaryIcon(dictionary, container)
        $(getValueSelectFromExpression(container)).removeAttr('id');
        $(getValueSelectFromExpression(container)).select2({closeOnSelect: false});
        upgradeQueryMultiSelect2(getValueSelectFromExpression(container), null);
        $(getReportFiledSelectFromExpression(container)).removeAttr('id');
        $(getReportFiledSelectFromExpression(container)).select2();
        var autocompleteSelect = getValueSelectAutoFromExpression(container);
        $(autocompleteSelect).removeAttr('id');
        $(autocompleteSelect).select2({
            minimumInputLength: 2,
            separator: ";",
            multiple: true,
            closeOnSelect: false,
            ajax: {
                delay: 300,
                dataType: "json",
                url: selectAutoUrl,
                data: function (params) {
                    var container = $(this).closest('.toAddContainer')[0];
                    var field = $(getFieldFromExpression(container)).select2("val");
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
        upgradeQueryMultiSelect2(autocompleteSelect, selectAutoUrl);
        var nonCacheSelect = getValueSelectNonCacheFromExpression(container);
        $(nonCacheSelect).removeAttr('id');
        $(nonCacheSelect).select2({
            minimumInputLength: 0,
            separator: ";",
            multiple: true,
            closeOnSelect: false,
            ajax: {
                delay: 300,
                dataType: "json",
                url: selectNonCacheUrl,
                data: function (params) {
                    var container = $(this).closest('.toAddContainer')[0];
                    var field = $(getFieldFromExpression(container)).select2("val");
                    return {
                        field: field,
                        term: params.term,
                        max: 30,
                        lang: userLocale,
                        page: params.page,
                        isFaersTarget: isFaersTarget
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
        upgradeQueryMultiSelect2(nonCacheSelect, selectNonCacheUrl);
        if (keyword) {
            addKeyword(expression, keyword, 'keywordExpression');
        }
        if (editable) {
            addButtonRemove(expression, 'removeExpression');
        }
        expression['expressionIndex'] = backboneExpressions.indexOf(backboneExpression);

        if (editable) {
            addDragListeners(expression);
            expression["draggable"] = true;
        }


        var lastExpressionIndex = getLastChildWithClassnameIndexFromElement('expression', groupElement);
        if (lastExpressionIndex != -1) {
            groupElement.insertBefore(expression, groupElement.children[lastExpressionIndex].nextSibling);
        } else {
            groupElement.insertBefore(expression, groupElement.firstChild);
        }
    }
}

// Below methods are unused but kept for posterity
/**
 function isJsonString(str) {
        try {
            JSON.parse(str);
        } catch (e) {
            return false;
        }
        return true;
    }

 */

// setBuilder
function printToJSON() {
    if (activeTab === 'QUERY_BUILDER') {
        $queryJSON.val(printAll());
    } else if (activeTab === 'SET_BUILDER') {
        $setJSON.val(printSet());
    }
}

function printSet() {
    hasBlanks = false;
    keys = [];

    var container = setBuilderAll;

    var result = '{ "all": { "containerGroups": [ ' + printRecurJSONSet(container);

    result += '}';
    return result;
}

// Helper method, shouldn't be called directly
function printRecurJSONSet(expressionGroup) {
    var tempResult = " ";
    var keywordValueForGroup;
    var lastExpressionOrGroupIndex = getLastExpressionOrGroupIndexFromGroup(expressionGroup);

    var firstExpressionIndex = -1;
    var firstGroupIndex = -1;

    var needComma = false;

    for (var i = 0; i < expressionGroup.children.length; i++) {
        if (expressionGroup.children[i].classList.contains('expression')) {
            var tempExpression = backboneSets.at(expressionGroup.children[i].expressionIndex);
            tempResult += ' { "index": "' + expressionGroup.children[i].expressionIndex
                + '", "query": "' + tempExpression.get('query')
                + '"';
            tempResult += ' } ';
            if (firstExpressionIndex == -1) {
                firstExpressionIndex = i;
            }
            needComma = true;
        } else if (expressionGroup.children[i].classList.contains('group')) {
            tempResult += ' { "expressions": [' + printRecurJSONSet(expressionGroup.children[i]) + ' ';
            if (firstGroupIndex == -1) {
                firstGroupIndex = i;
            }
            needComma = true;
        }
        if (needComma && i < lastExpressionOrGroupIndex) {
            tempResult += ", ";
            needComma = false;
        }
    }
    // One keyword per group, if we have a keyword
    if (firstExpressionIndex != -1) {
        var keywordIndex = getKeywordIndexFromElement(expressionGroup.children[firstExpressionIndex]);
        if (keywordIndex != -1) {
            keywordValueForGroup = expressionGroup.children[firstExpressionIndex].children[keywordIndex].value;
        }
        if (keywordValueForGroup) {
            return tempResult + '] , "keyword": "' + keywordValueForGroup + '" } ';
        }
    } else if (firstExpressionIndex == -1 && firstGroupIndex != -1) {
        var keywordIndex = getKeywordIndexFromElement(expressionGroup.children[firstGroupIndex]);
        if (keywordIndex != -1) {
            keywordValueForGroup = expressionGroup.children[firstGroupIndex].children[keywordIndex].value;
        }
        if (keywordValueForGroup) {
            return tempResult + '] , "keyword": "' + keywordValueForGroup + '" } ';
        }
    }
    return tempResult + ' ] } ';
}

function buildSetFromJSON(JSONQuery) {
    var parsedJSON = JSON.parse(JSONQuery);

    // all is the upper container just for holding the json object.
    var all = parsedJSON["all"];

    // this is the list of all the groups to render in setBuilderAll, the topmost level
    var containerGroups = all["containerGroups"];

    var containerKeyword = all["keyword"];

    containerGroups.forEach(function (group) {
        var addGroup = createGroup();

        var lastExpressionOrGroupIndex = getLastExpressionOrGroupIndexFromGroup(setBuilderAll);
        if (lastExpressionOrGroupIndex != -1) {
            setBuilderAll.insertBefore(addGroup, setBuilderAll.children[lastExpressionOrGroupIndex].nextSibling);
        } else {
            setBuilderAll.insertBefore(addGroup, setBuilderAll.firstChild);
        }

        group.expressions.forEach(function (nextGroup) {
            parseSetGroup(addGroup, nextGroup, group.keyword);
        });

        if (containerKeyword) {
            addKeyword(addGroup, containerKeyword, 'keywordGroup');
        }

        cleanUpKeywords(addGroup);
        cleanUpKeywordGroups(addGroup);
    });

    cleanUpKeywordGroups(setBuilderAll);
}

// Helper method, shouldn't be called directly
function parseSetGroup(groupElement, groupJSON, keyword) {
    if (groupJSON.expressions) {
        var addGroup = createGroup();

        var lastExpressionOrGroupIndex = getLastExpressionOrGroupIndexFromGroup(groupElement);
        if (lastExpressionOrGroupIndex != -1) {
            groupElement.insertBefore(addGroup, groupElement.children[lastExpressionOrGroupIndex].nextSibling);
        } else {
            groupElement.insertBefore(addGroup, groupElement.firstChild);
        }

        groupJSON.expressions.forEach(function (group) {
            parseSetGroup(addGroup, group, groupJSON.keyword);
        });

        if (keyword) {
            addKeyword(addGroup, keyword, 'keywordGroup');
        }

        cleanUpKeywords(addGroup);
        cleanUpKeywordGroups(addGroup);
    } else {
        var expression = document.createElement('div');
        expression.className = 'expression';
        var container = document.getElementById('toAddContainerSet').cloneNode(true);
        $(container).removeAttr("id");
        expression.appendChild(container);

        var setExpression = new Expression({query: groupJSON.query});
        backboneSets.add(setExpression);

        // select2 values
        var $query = $(getQueryFromExpression(container));
        $query.attr("data-value", groupJSON.query);
        $query.val(groupJSON.query);

        bindQuerySelect2($query);

        if (keyword) {
            addKeyword(expression, keyword, 'keywordExpression');
        }
        if (editable) {
            addButtonRemove(expression, 'removeExpression');
        }
        expression['expressionIndex'] = backboneSets.indexOf(setExpression);

        if (editable) {
            addDragListeners(expression);
            expression["draggable"] = true;
        }


        var lastExpressionIndex = getLastChildWithClassnameIndexFromElement('expression', groupElement);
        if (lastExpressionIndex != -1) {
            groupElement.insertBefore(expression, groupElement.children[lastExpressionIndex].nextSibling);
        } else {
            groupElement.insertBefore(expression, groupElement.firstChild);
        }
    }
}

// -------------------------------------------------------------------------------------------------------- END JSON

// Drag and Drop ------------------------------------------------------------------------------------- Drag and Drop

function handleDragStart(e) {
    this.style.opacity = '0.4';
    dragSourceEl = this;

    // set drag from group
    draggingFromGroup = this.parentElement;

//        e.dataTransfer.effectAllowed = 'move';
    // we don't actually use any dataTransfer.
    // we drag the expressionIndex, the index of the model in our list of expressions, called expressions
//        e.dataTransfer.setData('application/x-moz-node', this.getElementsByClassName('toAddContainer')[0]);

    // Target (this) element is the source node.
    if (activeTab === 'QUERY_BUILDER') {
        $(builderSubgroup).show();
    } else if (activeTab === 'SET_BUILDER') {
        $(setBuilderSubgroup).show();
    }
}

function handleDragOver(e) {
    if (e.preventDefault) {
        e.preventDefault(); // Necessary. Allows us to drop.
    }

//        e.dataTransfer.dropEffect = 'move';  // See the section on the DataTransfer object.

    return false;
}

function handleDragEnter(e) {
    // this or e.target is the current hover target.
    this.classList.add('over');
    if (draggingFromGroup == null) {
        draggingFromGroup = this;
    }
    if (!draggingFromGroup.contains(this)) {
        var subgroups = $(draggingFromGroup).find('.subgroup');
        for (var i = 0; i < subgroups.length; i++) {
            $(subgroups[i]).hide();
        }
    }

    if ($(this).hasClass('group')) {
        $(getSubgroup(this)).show();
        draggingFromGroup = this;
    }
}

function handleDragLeave(e) {
    this.classList.remove('over');  // this or e.target is previous target element.
}

function handleDrop(e) {
    // this or e.target is current target element.

    if (e.stopPropagation) {
        e.stopPropagation(); // Stops some browsers from redirecting.
    }

    // Don't do anything if dropping the same expression we're dragging.
    if (dragSourceEl != this) {
        var swapElement;
        if (activeTab === 'QUERY_BUILDER') {
            swapElement = $(this).find('.toAddContainer')[0];
            this.insertBefore($(dragSourceEl).find('.toAddContainer')[0], this.children[0]);
        } else if (activeTab === 'SET_BUILDER') {
            swapElement = $(this).find('.toAddContainerSet')[0];
            this.insertBefore($(dragSourceEl).find('.toAddContainerSet')[0], this.children[0]);
        }
        dragSourceEl.insertBefore(swapElement, dragSourceEl.children[0]);

        // Swap the expression indexes
        var tempIndex = dragSourceEl.expressionIndex;
        dragSourceEl.expressionIndex = this.expressionIndex;
        this.expressionIndex = tempIndex;
    }

    $(getSubgroup(this.parentElement)).hide();

    return false;
}

function handleDragEnd(e) {
    // this or e.target is the source node.
    this.style.opacity = '1';

    [].forEach.call($('.expression'), function (col) {
        col.classList.remove('over');
    });
    [].forEach.call($('.group'), function (col) {
        col.classList.remove('over');
    });
    [].forEach.call($('.subgroup'), function (col) {
        col.classList.remove('over');
        $(col).hide();
    });
    if (activeTab === 'QUERY_BUILDER') {
        $(builderSubgroup).hide();
    } else if (activeTab === 'SET_BUILDER') {
        $(setBuilderSubgroup).hide();
    }

    $(getSubgroup(draggingFromGroup)).hide();

    printToJSON();
}

function handleDropIntoGroup(e) {
    // this or e.target is current target element.

    if (e.stopPropagation) {
        e.stopPropagation(); // Stops some browsers from redirecting.
    }

    var oldGroup = dragSourceEl.parentElement;

    // Don't do anything if dropping the expression into its own group
    if (oldGroup != this) {
        var addAfterIndex = getLastChildWithClassnameIndexFromElement('expression', this);
        var existingKeywordValue;
        if (addAfterIndex != -1) {
            this.insertBefore(dragSourceEl, this.children[addAfterIndex].nextSibling);
            var existingKeywordIndex = getKeywordIndexFromElement(this.children[addAfterIndex]);
            if (existingKeywordIndex != -1) {
                existingKeywordValue = this.children[addAfterIndex].children[existingKeywordIndex].value;
            }
        } else {
            this.insertBefore(dragSourceEl, this.firstChild);
        }

        // Need to preserve keyword integrity, so replace dropped element keyword with drop-into-group's keyword.
        if (existingKeywordValue) {
            var currentKeywordIndex = getKeywordIndexFromElement(dragSourceEl);
            if (currentKeywordIndex != -1) {
                dragSourceEl.children[currentKeywordIndex].value = existingKeywordValue;
            }
        }

        // clean up keywords in the old and the droppedInto groups
        cleanUpKeywords(oldGroup);
        cleanUpKeywordGroups(oldGroup);
        cleanUpKeywords(this);
    }

    $(getSubgroup(this)).hide();

    return false;
}

function handleDropIntoSubgroup(e) {
    if (e.stopPropagation) {
        e.stopPropagation(); // Stops some browsers from redirecting.
    }

    var oldGroup = dragSourceEl.parentElement;
    var expressionGroup = this.parentElement;

    var newGroup = createGroup();
    newGroup.insertBefore(dragSourceEl, newGroup.firstChild);
    var addAfterIndex = getLastExpressionOrGroupIndexFromGroup(expressionGroup);
    if (addAfterIndex != -1) {
        expressionGroup.insertBefore(newGroup, expressionGroup.children[addAfterIndex].nextSibling);
    } else {
        expressionGroup.insertBefore(newGroup, expressionGroup.firstChild);
    }

    cleanUpKeywords(oldGroup);
    cleanUpKeywordGroups(oldGroup);
    cleanUpKeywords(expressionGroup);
    cleanUpKeywordGroups(expressionGroup);
    // This takes care of the case when we drag an expression with a keyword.
    cleanUpKeywords(newGroup);

    $(this).hide();

    if ((addAfterIndex !== -1) && $(expressionGroup).hasClass("builderAll")) {
        var rootGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroup, 0, 1);
        $(expressionGroup).prepend(rootGroup);
    }

    return false;
}

function handleClickGroup(e) {
    if (activeTab === 'QUERY_BUILDER') {
        $(selectedGroup).removeClass('selectedGroup');
        selectedGroup = this;
        $(selectedGroup).addClass('selectedGroup');
    } else if (activeTab === 'SET_BUILDER') {
        $(selectedSetGroup).removeClass('selectedGroup');
        selectedSetGroup = this;
        $(selectedSetGroup).addClass('selectedGroup');
    }
}

// We allow only expressions to be draggable
function addDragListeners(element) {
    element.addEventListener('dragstart', handleDragStart, false);
    element.addEventListener('dragover', handleDragOver, false);
    element.addEventListener('dragenter', handleDragEnter, false);
    element.addEventListener('dragleave', handleDragLeave, false);
    element.addEventListener('drop', handleDrop, false);
    element.addEventListener('dragend', handleDragEnd, false);
}

function addGroupDragListeners(element) {
    element.addEventListener('dragover', handleDragOver, false);
    element.addEventListener('dragenter', handleDragEnter, true);
    element.addEventListener('dragleave', handleDragLeave, false);
    element.addEventListener('drop', handleDropIntoGroup, false);
    // true is needed here to capture this click event instead of bubble up
    element.addEventListener('click', handleClickGroup, true);
}

function addSubgroupDragListeners(element) {
    element.addEventListener('dragover', handleDragOver, false);
    element.addEventListener('dragenter', handleDragEnter, false);
    element.addEventListener('dragleave', handleDragLeave, false);
    element.addEventListener('drop', handleDropIntoSubgroup, true);
}

// ----------------------------------------------------------------------------------------------- END DRAG AND DROP

// Helper methods ----------------------------------------------------------------------------------- HELPER METHODS

// We need two onChange methods because fuelux datepicker has two fields that need to react to change events
function addOnchangeMethodsToDatepickerElement(datepicker) {
    $(datepicker).on('changed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
        var container = this.parentElement.parentElement;
        var expression = container.parentElement.parentElement;

        if ((expression != undefined) && (expression.expressionIndex != null)) {
            backboneExpressions.at(expression.expressionIndex).set("value", $(getValueDateInputFromExpression(container))[0].value);
        }
        $queryJSON.val(printAll());
    });
    $(getValueDateInputFromExpression(datepicker.parentElement.parentElement)).on('change', function () {
        var container = datepicker.parentElement.parentElement;
        var expression = container.parentElement.parentElement;

        if ((expression != undefined) && (expression.expressionIndex != null)) {
            backboneExpressions.at(expression.expressionIndex).set("value", $(getValueDateInputFromExpression(container))[0].value);
        }
        $queryJSON.val(printAll());
    })
}

// Moves this group and all its siblings into a new group one level above it
function encapsulateFromIndexToIndexInNewGroup(expressionGroup, fromIndex, toIndex) {
    // Create a new group to move the elements into
    var newGroup = createGroup();

    if (expressionGroup.children.length < toIndex) {
        // alert('toIndex is too big!');
    }
    if (expressionGroup.children.length < fromIndex) {
        // alert('fromIndex is too small!');
    }

    // Traverse in reverse order
    for (var i = toIndex; i >= fromIndex; i--) {
        if ($(expressionGroup.children[i]).hasClass('expression') || $(expressionGroup.children[i]).hasClass('group')) {
            if (newGroup.firstChild) {
                newGroup.insertBefore(expressionGroup.children[i], newGroup.firstChild);
            } else {
                newGroup.appendChild(expressionGroup.children[i]);
            }
        }
    }

    return newGroup;
}

function createGroup() {
    var div = document.createElement('div');
    div.className = 'group';
    if (editable) {
        addButtonRemove(div, 'removeGroup');
    }
//        addButtonAddGroup(div);
    if (editable) {
        addGroupDragListeners(div);
    }
    addSubgroup(div);

    return div;
}

// Button added to expression to remove itself
function addButtonRemove(element, removeClassName) {
    var button = document.createElement('button');
    var text = document.createTextNode("x");
    button.appendChild(text);
    button.className = removeClassName + ' btn btn-default btn-xs';
    element.appendChild(button);
}

// Adds keyword to expression OR group, depending on classname.
function addKeyword(element, keywordValue, keywordClassName) {
    var keyword = document.createElement('select');
    var option = document.createElement('option');
    if (activeTab == 'QUERY_BUILDER') {
        option.text = 'and';
        keyword.add(option);
        option = document.createElement('option');
        option.text = 'or';
        keyword.add(option);
        $(keyword).val(keywordValue);
    } else if (activeTab === 'SET_BUILDER') {
        option.text = 'intersect';
        keyword.add(option);
        option = document.createElement('option');
        option.text = 'union';
        keyword.add(option);
        option = document.createElement('option');
        option.text = 'minus';
        keyword.add(option);
        $(keyword).val(keywordValue);
        keyword.classList.add('extra-width');
    }
    keyword.classList.add(keywordClassName);
    keyword.classList.add('form-control');
    element.appendChild(keyword);
    if (!editable) {
        keyword['disabled'] = 'disabled';
    }
}

function addSubgroup(element) {
    var subgroup = document.createElement('div');
    subgroup.className = 'subgroup';
    if (editable) {
        addSubgroupDragListeners(subgroup);
    }
    $(subgroup).hide();
    element.appendChild(subgroup);
}

// Extra Values
// Re-assess Listedness
function addShowRLDS(element, value, field) {
    var RLDSBaseContainer = document.getElementById('reassessListednessDS');
    $('#reassessListednessDSSelect').select2("destroy");
    var cloneNode = RLDSBaseContainer.cloneNode(true);
    $('#reassessListednessDSSelect').select2();
    $(cloneNode).removeAttr("id");
    var select = getExtraValueRLDSSelectFromContainer(cloneNode);
    if (value) {
        // Multiselect Select2
        $(select).val(value.split(';'));
    }
    $(select).select2();
    $(getExtraValues(element)).append(cloneNode);
    if (!editable) {
        select['disabled'] = 'disabled';
    }
    if ((field == REASSESS_LISTEDNESS_FIELD_J || field == REASSESS_LISTEDNESS_FIELD) && $(getExtraValues(element))[0].childElementCount != 1) {
        $(cloneNode).hide();
    } else {
        $(cloneNode).css("display","block");
        $(cloneNode).find('#reassessListednessDSSelect').trigger("change");
    }
}

// This is run after we do an operation that can mess up keywords at a multi-group level.
function cleanUpKeywordGroups(groupContainer) {
    // check it we have more than one group
    var groupsNum = 0;
    var lastGroupIndex = -1;
    for (var i = 0; i < groupContainer.children.length; i++) {
        if ($(groupContainer.children[i]).hasClass('group')) {
            groupsNum++;
            lastGroupIndex = i;
        }
    }

    // delete the last keyword for this group if we have one
    if (groupsNum > 0) {
        var keywordIndex = getKeywordIndexFromElement(groupContainer.children[lastGroupIndex]);

        if (keywordIndex > -1) {
            $(groupContainer.children[lastGroupIndex].children[keywordIndex]).remove();
        }
    }

    // delete the last expression's keyword if it exists
    if (groupsNum > 0) {
        var keywordIndex = getKeywordIndexFromElement(groupContainer.children[lastGroupIndex]);

        // delete branch
        if (keywordIndex > -1) {
            $(groupContainer.children[lastGroupIndex].children[keywordIndex]).remove();
        }
        // add branch - add a keyword to the second to last expression if we have 2 or more (because we added one)
        // if we only have 2 expressions, set keyword to AND
        // we only add if we don't have a keyword already

        // get the first group's keyword index
        var firstGroupIndex = -1;
        var groupCount = 0;
        while (firstGroupIndex == -1) {
            if ($(groupContainer.children[groupCount]).hasClass('group')) {
                firstGroupIndex = groupCount;
            } else {
                groupCount++;
            }
        }
        var firstChildKeywordIndex = getKeywordIndexFromElement(groupContainer.children[firstGroupIndex]);

        // Add a keyword to the first group if we don't already have one. This can occur after a group keyword change
        if (firstChildKeywordIndex == -1) {
            if (groupsNum == 2) {
                if (activeTab === 'QUERY_BUILDER') {
                    addKeyword(groupContainer.children[firstGroupIndex], 'and', 'keywordGroup');
                } else if (activeTab === 'SET_BUILDER') {
                    addKeyword(groupContainer.children[firstGroupIndex], 'intersect', 'keywordGroup');
                }
            } else if (groupsNum > 2) {
                // add keyword to #1 with value from 2nd group
                var secondChildKeywordIndex = getKeywordIndexFromElement(groupContainer.children[firstGroupIndex].nextSibling);

                addKeyword(groupContainer.children[firstGroupIndex],
                    $(groupContainer.children[firstGroupIndex].nextSibling.children[secondChildKeywordIndex]).val(), 'keywordGroup');
            }
        }

        if (groupsNum > 2) {
            // Only add a keyword if we don't already have one
            var checkKeywordIndex = getKeywordIndexFromElement(groupContainer.children[lastGroupIndex - 1]);

            // We can assume that (lastGroupIndex - 1) will always return an expressionGroup, since expressions are always displayed before groups
            if (checkKeywordIndex == -1) {
                // preserve the keyword value so that we properly do not need to create a new group here
                addKeyword(groupContainer.children[lastGroupIndex - 1], $(groupContainer.children[firstGroupIndex].children[firstChildKeywordIndex]).val(), 'keywordGroup');
            }
        }
    } else {
        // Remove the last keyword if we only have one group
        var keywordIndex = getKeywordIndexFromElement(groupContainer);
        if (keywordIndex > -1 && lastGroupIndex != -1) {
            $(groupContainer.children[lastGroupIndex].children[keywordIndex]).remove();
        }
    }
}

// This should be executed after we do an operation that can mess up keywords within a group.
function cleanUpKeywords(expressionGroup) {
    // check it we have more than one expression
    var lastExpressionIndex = getLastChildWithClassnameIndexFromElement('expression', expressionGroup);

    // Also need to check for any groups, since we can have keywords between the last expression and the first group
    var firstGroupIndex = getFirstChildWithClassnameIndexFromElement('group', expressionGroup);

    // delete the last expression's keyword if it exists
    if (lastExpressionIndex > -1) {
        // we can use getElementsByClassName here since expression is the last step of the heirarchy
        var keywordList = $(expressionGroup.children[lastExpressionIndex]).find('.keywordExpression');

        // delete branch
        if (keywordList.length != 0 && firstGroupIndex == -1) {
            $(keywordList[0]).remove();
        }
        // add branch - add a keyword the second to last expression if we have 2 or more (because we added one)
        // if we only have 2 expressions, set keyword to AND
        // we only add if we don't have a keyword already
        if (lastExpressionIndex == 1) {
            var firstKeywordList = $(expressionGroup.children[0]).find('.keywordExpression');

            if (firstKeywordList.length == 0) {
                if (activeTab === 'QUERY_BUILDER') {
                    addKeyword(expressionGroup.children[0], 'and', 'keywordExpression');
                } else if (activeTab === 'SET_BUILDER') {
                    addKeyword(expressionGroup.children[0], 'intersect', 'keywordExpression');
                }
            }
        } else if (lastExpressionIndex > 1) {
            var secondToLastKeywordList = $(expressionGroup.children[lastExpressionIndex - 1]).find('.keywordExpression');

            if (secondToLastKeywordList.length == 0) {
                // preserve the keyword value so that we properly do not need to create a new group here
                addKeyword(expressionGroup.children[lastExpressionIndex - 1],
                    $($(expressionGroup.children[0]).find('.keywordExpression')[0]).val(), 'keywordExpression');
            }
        }

        // If we have a group after our expressions, make sure we have a keyword with the last expression
        if (firstGroupIndex != -1) {
            if (keywordList.length == 0) {
                // preserve the keyword value so that we properly do not need to create a new group here
                // only if we have a keyword here already
                var toAddTo = $(expressionGroup.children[0]).find('.keywordExpression')[0];
                if (toAddTo) {
                    addKeyword(expressionGroup.children[lastExpressionIndex], $($(expressionGroup.children[0]).find('.keywordExpression')[0]).val(), 'keywordExpression');
                } else {
                    if (activeTab === 'QUERY_BUILDER') {
                        addKeyword(expressionGroup.children[lastExpressionIndex], 'and', 'keywordExpression');
                    } else if (activeTab === 'SET_BUILDER') {
                        addKeyword(expressionGroup.children[lastExpressionIndex], 'intersect', 'keywordExpression');
                    }
                }
            }
        }
    }
}

function createExpressionFromInput(toAddContainer, field, op, value, extraValues) {
    var expressionValue = value;
    if (Array.isArray(value)) {
        expressionValue = '';
        for (var i = 0; i < value.length; i++) {
            if (i != 0) {
                // Multiselect Select2
                expressionValue += ';' + value[i];
            } else {
                expressionValue += value[i];
            }
        }
    }

    var data = new Expression({field: field, op: op, value: expressionValue});

    // Extra values
    // Re-assess Listedness
    if (RLDS_KEY in extraValues) {
        var RLDSValue = extraValues[RLDS_KEY];
        $(getExtraValueRLDSSelectFromContainer(toAddContainer)).val(RLDSValue);
        setOnPrimaryDatasheetValue(toAddContainer, extraValues[RLDS_OPDS]);
        updateOnPrimaryDatasheetCheckbox(toAddContainer);
        var RLDSMultiselectValue = RLDSValue;
        if (Array.isArray(RLDSValue)) {
            RLDSMultiselectValue = '';
            for (var i = 0; i < RLDSValue.length; i++) {
                if (i != 0) {
                    // Multiselect Select2
                    RLDSMultiselectValue += ';' + RLDSValue[i];
                } else {
                    RLDSMultiselectValue += RLDSValue[i];
                }
            }
        }
        data.set(RLDS_KEY, RLDSMultiselectValue);
        data.set(RLDS_OPDS, extraValues[RLDS_OPDS]);
    }
    backboneExpressions.add(data);

    var div = document.createElement('div');
    $(getFieldFromExpression(toAddContainer)).val(field);
    $(getOperatorFromExpression(toAddContainer)).val(op);

    var selectValue = getDestroyedSelectValueFromContainer(toAddContainer);
    $(selectValue).val(value);

    div.appendChild(toAddContainer);
    if (editable) {
        addButtonRemove(div, 'removeExpression');
    }
    div.className = 'expression';

    if (editable) {
        div["draggable"] = true;
        addDragListeners(div);
    }
    div['expressionIndex'] = backboneExpressions.indexOf(data);
    return div;
}

// setBuilder
function createSetExpressionFromInput(toAddContainer, queryValue) {
    var data = new Expression({query: queryValue});
    backboneSets.add(data);

    var div = document.createElement('div');

    var selectQuery = getDestroyedQuerySelectValueFromContainer(toAddContainer);
    $(selectQuery).val(queryValue);

    div.appendChild(toAddContainer);
    if (editable) {
        addButtonRemove(div, 'removeExpression');
    }
    div.className = 'expression';
    if (editable) {
        div["draggable"] = true;
        addDragListeners(div);
    }
    div['expressionIndex'] = backboneSets.indexOf(data);
    return div;
}

// Tabs
function setActiveTabSet() {
    $queryBuilder.removeClass('active');
    $queryBuilderTab.removeClass('active');
    $setBuilder.addClass('active');
    $setBuilderTab.addClass('active');
}

function setActiveTabCustom() {
    $queryBuilder.removeClass('active');
    $queryBuilderTab.removeClass('active');
    $customSQL.addClass('active');
    $customSQLTab.addClass('active');
}

function disableTabLink($element) {
    $element.removeAttr('data-toggle');
    $element.addClass('noClick');
}

// Below methods are unused but kept for posterity
/**

 // Button to add a new group
 function addButtonAddGroup(element) {
        var button = document.createElement('button');
        var text = document.createTextNode("+GROUP");
        button.appendChild(text);
        button.className = 'addGroup btn btn-default btn-xs';
        element.appendChild(button);
    }

 */

// ---------------------------------------------------------------------------------------------- END HELPER METHODS

// More helper methods, available in a global scope ---------------------------------------------- GLOBAL HELPER METHODS

function getDestroyedSelectValueFromContainer(container) {
    // Select2 is currently destroyed
    var field = $(getFieldFromExpression(container)).val();
    var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find(":selected"));
    var $op = $(getOperatorFromExpression(container))[0].value;
    if ($(container).find('.rptFieldInput').is(':checked')) {
        return getReportFiledSelectFromExpression(container);
    } else if (isCopyPasteField(container) || $(container).find('.poiInput').is(':checked')) {
        return getValueTextFromExpression(container)
    } else if (selectedFieldType == RF_TYPE_DATE) {
        return getValueDateFromExpression(container);
    } else if (selectedFieldType == RF_TYPE_STRING) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectFromExpression(container);
        }
    } else if (selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectNonCacheFromExpression(container);
        }
    } else if (selectedFieldType == RF_TYPE_AUTOCOMPLETE) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectAutoFromExpression(container);
        }
    }
    return getValueTextFromExpression(container);
}

// setBuilder
function getDestroyedQuerySelectValueFromContainer(container) {
    // Select2 is currently destroyed
    return getQueryFromExpression(container);
}

function isCopyPasteField(container) {
    var $showValue = $(container).find("#showValue")
    return !_.isEmpty($showValue.find(".expressionValueText").attr("copyAndPasteWithDelimiter"))
}

function getCorrectSelectValueFromContainer(container) {
    var field = $(getFieldFromExpression(container)).select2("val");
    var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find(":selected"));
    var $op = $(getOperatorFromExpression(container))[0].value;
    if ($(container).find('.rptFieldInput').is(':checked')) {
        return getReportFiledSelectFromExpression(container);
    }
    if (isCopyPasteField(container) || $(container).find('.poiInput').is(':checked') || (getReportFieldDicType())) {
        return getValueTextFromExpression(container)
    } else if (selectedFieldType == RF_TYPE_DATE) {
        return getValueDateFromExpression(container);
    } else if (selectedFieldType == RF_TYPE_STRING) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectFromExpression(container);
        }
    } else if (selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectNonCacheFromExpression(container);
        }
    } else if (selectedFieldType == RF_TYPE_AUTOCOMPLETE) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectAutoFromExpression(container);
        }
    }
    return getValueTextFromExpression(container);
}

function showHideValue(selectedType, div) {
    $(getCopyPasteButtonFromExpression(div).parentElement).show();
    switch (selectedType) {
        case EDITOR_TYPE_DATE:
            // Show datepicker
            $(getCopyPasteButtonFromExpression(div).parentElement).hide();
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).show();
            $(getReportFiledSelectFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_SELECT:
            // Show select
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).show();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            $(getReportFiledSelectFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_AUTOCOMPLETE:
            // Show autocomplete
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).show();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            $(getReportFiledSelectFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_NONECACHE_SELECT:
            // Show autocomplete
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).show();
            $(getValueDateFromExpression(div).parentElement).hide();
            $(getReportFiledSelectFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_NONE:
            // Hide All
            $(getCopyPasteButtonFromExpression(div).parentElement).hide();
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            $(getReportFiledSelectFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_RPT_FIELD:
            // Hide All
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            $(getReportFiledSelectFromExpression(div).parentElement).show();
            break;
        case EDITOR_TYPE_TEXT:
        default:
            // Show text field
            $(getValueTextFromExpression(div).parentElement).show();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            $(getReportFiledSelectFromExpression(div).parentElement).hide();
            break;
    }
}

function getFieldFromExpression(container) {
    return $(container).find('select.expressionField')[0];
}

function getOperatorFromExpression(container) {
    return $(container).find('.expressionOp')[0];
}

function getValueTextFromExpression(container) {
    return $(container).find('.expressionValueText')[0];
}

function getFieldIsValidatableFromExpression(container) {
    return $(getFieldFromExpression(container)).find('option:selected').attr('data-validatable') == 'true';
}

function getCopyPasteButtonFromExpression(container) {
    return $(container).find('.copy-n-paste')[0];
}

function setFieldTitleOnHover(container) {
    var title = $(getFieldFromExpression(container)).find('option:selected').text();
    var field = $(container).find('div.expressionsNoPadFirst')[0];
    $(field).attr('title', title);
}

function getPOICheckBoxFromExpression(container) {
    return $(container).find('.poiInput')[0];
}

function getRptInputCheckBoxFromExpression(container) {
    return $(container).find('.rptFieldInput')[0];
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

// Extra Values
function getExtraValues(container) {
    return $(container).find('.extraValues');
}

// Re-assess Listedness
function getExtraValueRLDSSelectFromContainer(container) {
    return $(container).find('select.reassessListednessDS');
}

function getValueDateFromExpression(container) {
    return $(container).find('.expressionValueDate')[0];
}

function getValueDateInputFromExpression(container) {
    return $(container).find('.expressionValueDateInput')[0];
}

function getReportFiledSelectFromExpression(container) {
    return $(container).find('.expressionValueRptField')[0];
}

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
            case 'com.rxlogix.embaseOperators.embaseExact':
                type= RF_TYPE_EMBASE_EXACT
                break;
            case 'com.rxlogix.embaseOperators.embasePhrase':
                type= RF_TYPE_EMBASE_PHRASE
                break;
            case 'com.rxlogix.embaseOperators.embaseCombined':
                type= RF_TYPE_EMBASE_COMBINED
                break;
        }
    }
    return type;
}


// Returns the direct child index from parent, -1 if it is not in the list of parent's children
function getChildIndexFromParent(child, parent) {
    return Array.prototype.indexOf.call(parent.children, child);
}

// Returns the first (and should be only) keyword index from the direct children of the element or -1 if it doesn't exist
function getKeywordIndexFromElement(element) {
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass('keywordExpression') || $(element.children[i]).hasClass('keywordGroup')) {
            return i;
        }
    }
    return -1;
}

// Returns the first child index with given classname from the children of the given element or -1 if it doesn't exist
function getFirstChildWithClassnameIndexFromElement(classname, element) {
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass(classname)) {
            return i;
        }
    }
    return -1;
}

// Returns the second child index with given classname from the children of the given element or -1 if it doesn't exist
function getSecondChildWithClassnameIndexFromElement(classname, element) {
    var first = true;
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass(classname)) {
            if (first) {
                first = false;
            } else {
                return i;
            }
        }
    }
    return -1;
}

// Returns the last child index with given classname children of the given element or -1 if it doesn't exist
function getLastChildWithClassnameIndexFromElement(classname, element) {
    var lastIndex = -1;
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass(classname)) {
            lastIndex = i;
        }
    }
    return lastIndex;
}

// Returns the last expression or group index from the children of the given element or -1 if it doesn't exist
function getLastExpressionOrGroupIndexFromGroup(element) {
    var lastIndex = -1;
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass('expression') || $(element.children[i]).hasClass('group')) {
            lastIndex = i;
        }
    }
    return lastIndex;
}

// Returns the total number of expressions and groups from the children of the given element
function getExpressionOrGroupCountFromGroup(element) {
    var count = 0;
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass('expression') || $(element.children[i]).hasClass('group')) {
            count++;
        }
    }
    return count;
}

function getSubgroup(expressionGroup) {
    for (var i = 0; i < expressionGroup.children.length; i++) {
        if ($(expressionGroup.children[i]).hasClass('subgroup')) {
            return expressionGroup.children[i];
        }
    }
    return null;
}

// Called when deleting - need to update expressionIndex since we edited the Backbone Collection
function checkIndexes(affectIndex) {
    var allExpr = $(document).find('.expression');

    for (var i = 0; i < allExpr.length; i++) {
        if (affectIndex < allExpr[i].expressionIndex) {
            allExpr[i].expressionIndex--;
        }
    }
}

// setBuilder
function getQueryFromExpression(container) {
    return $(container).find('.expressionQuery')[0];
}

function removeEmptyGroups(builder) {
    $.each($(builder).find('.group'), function () {
        // this will be window if we concurrently modify this list and the element does not exist anymore.
        if (this !== window) {
            if ($(this).find('.expression').length == 0) {
                var parent = this.parentElement;
                $(this).remove();
                cleanUpKeywordGroups(parent);
            }
        }
    });
}

// Extra Values
// Re-assess Listedness
function showHideReassessListedness(builder, beforeAdd) {
    var showExtraOptions = false;

    $.each($(builder).find('select.expressionField'), function () {
        if ($(this).val() == REASSESS_LISTEDNESS_FIELD || $(this).val() == REASSESS_LISTEDNESS_FIELD_J) {
            showExtraOptions = true;
        }
    });

    if (beforeAdd) {
        showExtraOptions = true;
    }

    if (showExtraOptions) {
        $('#extraOptions').show();
    } else {
        $('#extraOptions').hide();
    }
}

// ------------------------------------------------------------------------------------------- END GLOBAL HELPER METHODS

// Validation ----------------------------------------------------------------------------------------------- VALIDATION

$('#mainContent').on('change', '#name', function () {
    if ($(this).val().trim().length > 0) {
        $('#name').parent().removeClass('has-error');
    }
});

// Re-assess Listedness
function sendReassessListedness() {
    var $reassessListedness = $('#reassessListedness');
    if (activeTab !== 'QUERY_BUILDER' || !$reassessListedness.is(":visible")) {
        $reassessListedness.removeAttr('name');
    }
}

function finalizeForm() {

    var checkedNumber = checkAllNumberFields();
    if (!checkedNumber) {
        return false;
    }

    sendReassessListedness();

    if (activeTab === 'QUERY_BUILDER') {
        $('#setJSON').removeAttr('name');
        removeEmptyGroups(builderAll);
    } else if (activeTab === 'SET_BUILDER') {
        $('#queryJSON').removeAttr('name');
        $('#hasBlanksQuery').val("" + false);
        removeEmptyGroups(setBuilderAll);
    }

    printToJSON();

    return true;
}

// ------------------------------------------------------------------------------------------------------ END VALIDATION


function bindQuerySelect2(selector) {
    return bindSelect2WithUrl(selector, querySearchUrl, queryNameUrl, false);
}

function checkNumberFields(theValue, theBaseContainer) {
    var validNumber = true;
    if (!Number.isNaN(theValue)) {
        if (!_.isEmpty(theValue) && !isPositiveInteger(theValue)) {
            validNumber = false;
            $(theBaseContainer).find('.errorMessageOperator').show();
        } else {
            $(theBaseContainer).find('.errorMessageOperator').hide();
        }
    }

    return validNumber;
}

function checkAllNumberFields() {
    var validNumber = true;

    $.each($('.builderAll #selectValue'), function () {
        var op = $(this).parents().eq(3).find("#selectOperator")[0].value;
        if (op.search('LAST_X') > -1) {
            if (!Number.isNaN(this.value)) {
                if (!_.isEmpty(this.value) && !isPositiveInteger(this.value)) {
                    validNumber = false;
                    $(this).parents().eq(2).find('.errorMessageOperatorInline').show();
                } else {
                    $(this).parents().eq(2).find('.errorMessageOperatorInline').hide();
                }
            }
        }
    });


    return validNumber;
}


function showPOIInputParameter(theBaseContainer) {
    showHideValue(EDITOR_TYPE_TEXT, theBaseContainer)
}

function isSpecialKeyValue(val) {
    return val && val.match(/^\&/) //TODO need to make regex less code in future
}

function isReportFieldKeyValue(val) {
    return val && val.match(/^\#_/) //TODO need to make regex less code in future
}


function showWarning(actionUrl) {
    $("form#queryForm").attr("action", actionUrl);
    $('#warningModal .description').text($.i18n._("query.edit.warning"));
    $('#warningModal').modal('show');
    $('#warningButton').off('click').on('click', function () {
        if (finalizeForm())
            $('#warningButton').attr("disabled", true);
        $("form#queryForm").trigger('submit');
    });
}

function upgradeQueryMultiSelect2(select2Selector, url) {
    var cnt = $(select2Selector).closest('.toAddContainer')[0];
    var fieldControl = $(getFieldFromExpression(cnt));
    upgradeMultiSelect2(select2Selector, url, fieldControl);
}

function showHideDictionaryIcon(type, container) {
    if (type) {
        $(container).find('#searchEvents').hide();
        $(container).find('#searchProducts').hide();
        $(container).find('#searchStudies').hide();
        if (type.toLowerCase() == EVENT_DICTIONARY) $(container).find('#searchEvents').show();
        if (type.toLowerCase() == PRODUCT_DICTIONARY) $(container).find('#searchProducts').show();
        if (type.toLowerCase() == STUDY_DICTIONARY) $(container).find('#searchStudies').show();
    } else {
        $(container).find('#searchEvents').hide();
        $(container).find('#searchProducts').hide();
        $(container).find('#searchStudies').hide();
    }
}

function getOnPrimaryDatasheetValue(container) {
    return $(container).find('#onPrimaryDatasheet').is(":checked");
}

//set Re-assess on Primary Datasheet checkbox in container according to the value
function setOnPrimaryDatasheetValue(container, value) {
    var val = !!value
    if (typeof value === "string") {
        val = (value == "true")
    }
    $(container).find('#onPrimaryDatasheet').prop("checked", val);
}

//show or hide Primary Datasheet checkbox based on selected datasheet
function updateOnPrimaryDatasheetCheckbox(container) {
    var hasChildrenFlag = $(container).find("#reassessListednessDSSelect").find(":selected").attr("data-hasChildrenFlag");
    if (!hasChildrenFlag) {
        $(container).find(".onPrimaryDatasheet").show();
    } else {
        $(container).find(".onPrimaryDatasheet").hide();
        $(container).find("#onPrimaryDatasheet").prop('checked', false);
    }
}