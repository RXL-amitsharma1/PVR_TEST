$(function () {
    bindQuerySelect2($("#globalQuery")).on("change", function (e) {
        selectGlobalQueryOnChange(this);
    }).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });
    $(".globalQueryWrapper").find(".expressionField,.expressionOp,.expressionValueSelect").select2();
    $(".select2-box").select2({
        placeholder: $.i18n._('selectOne')
    });
    $(".select2-box-disabled").select2();
    $('select.recipientOrg').on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    }).on('change', function () {
        getUnitConfigurationBasedOnOrg($(this).val(), 'Recipient')
    }).trigger('change');

    $('select.senderOrg').on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    }).on('change', function () {
        getUnitConfigurationBasedOnOrg($(this).val(), 'Sender')
    }).trigger('change');

    $('#autoGenerate').on('change', function () {
        $("input[name=localCpRequired]").prop('checked', false);
        $("input[name=includeOpenCases]").prop('checked', false);
        if ($(this).prop('checked')) {
            $("#localCpRequired").attr("disabled", "disabled");
            $("#includeOpenCases").attr("disabled", "disabled");
        }else {
            $("#localCpRequired").removeAttr("disabled");
            $("#includeOpenCases").removeAttr("disabled");
        }
    });

    $('#includeOpenCases').on('change', function () {
        $("input[name=localCpRequired]").prop('checked', false);
        $("input[name=autoGenerate]").prop('checked', false);
        if ($(this).prop('checked')) {
            $("#localCpRequired").attr("disabled", "disabled");
            $("#autoGenerate").attr("disabled", "disabled");
        }else {
            $("#localCpRequired").removeAttr("disabled");
            $("#autoGenerate").removeAttr("disabled");
        }
    });

    $('#localCpRequired').on('change', function () {
        $("input[name=includeOpenCases]").prop('checked', false);
        $("input[name=autoGenerate]").prop('checked', false);
        if ($(this).prop('checked')) {
            $("#includeOpenCases").attr("disabled", "disabled");
            $("#autoGenerate").attr("disabled", "disabled");
        }else {
            $("#includeOpenCases").removeAttr("disabled");
            $("#autoGenerate").removeAttr("disabled");
        }
    });

    $('#isJapanProfile').on('change', function () {
        $("input[name=deviceReportable]").prop('checked', false);
        $("input[name=awareDate]").prop('checked', false);
        if ($(this).prop('checked')) {
            $("input[name=awareDate]").removeAttr("disabled");
            $("#deviceReportable").attr("disabled", "disabled");
        }else {
            $("input[name=awareDate]").attr("disabled", "disabled");
            $("#deviceReportable").removeAttr("disabled");
        }
        updateRuleEvaluationDropDown(true);
        updateAuthorizationMandatoryMark();
    });

    $('#deviceReportable').on('change', function () {
        $("input[name=isJapanProfile]").prop('checked', false);
        if ($(this).prop('checked')) {
            $("#isJapanProfile").attr("disabled", "disabled");
        }else {
            $("#isJapanProfile").removeAttr("disabled");
        }
    });

    updateRuleEvaluationDropDown(false);
    showHideSubmissionDateFromDiv();
    checkUncheckSchedulingCheckBox();
    updateSubmissionDateFromSelectBox();
    updateAuthorizationMandatoryMark();

    $('#autoSubmit').on('change', function () {
        showHideSubmissionDateFromDiv();
    })

    ShowHideAutoScheduleFUPFromDiv();

    ShowHideCalendarNameFromDiv();

    $('#adjustDueDate').on('change', function () {
        ShowHideCalendarNameFromDiv();
    })

    ShowHideAdjustmentDateOptionsFromDiv();

    $('#adjustDueDate').on('change', function () {
        ShowHideAdjustmentDateOptionsFromDiv();
    })

    ShowHideAdjustmentDateFromDiv();

    $('#adjustDueDate').on('change', function () {
        if ($("#adjustDueDate").is(":checked")) {
            ShowHideAdjustmentDateFromDiv();
        }else {
            $("#dueDateAdjustmentDiv").css('display','none');
        }
    })

    $('#dueDateOptionsEnum').on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    }).on('change', function () {
        ShowHideAdjustmentDateFromDiv1();
    })
    bindMultipleSelect2WithUrl($("#calenderNameControl"), fetchCalenderNamesUrl, true,'',null,$("#calenderNameControl").attr('data-value'));

    /*$("#authorizationTypeId").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });*/

    $("select[name='e2bDistributionChannel.reportFormat']").select2({
        placeholder: $.i18n._('selectOne'),
        allowClear: true
    }).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $("select[name='e2bDistributionChannel.outgoingFolder']").select2({
        placeholder: $.i18n._('selectOne'),
        allowClear: true
    }).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $("select[name='e2bDistributionChannel.incomingFolder']").select2({
        placeholder: $.i18n._('selectOne'),
        allowClear: true
    }).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });

    $("#authorizationTypeId").select2({
        placeholder: $.i18n._('selectOne')
    });

    $("[data-evt-clk]").on('click', function(e) {
        e.preventDefault();
        const eventData = JSON.parse($(this).attr("data-evt-clk"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'formSubmit') {
            formSubmit();
        }
    });

    $("[data-evt-change]").on('change', function() {
        const eventData = JSON.parse($(this).attr("data-evt-change"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'updateHiddenField') {
            var value = $(this).val();
            updateHiddenField(value);
        }
    });
});

function checkUncheckSchedulingCheckBox() {
    $("input[name=autoScheduling]").on('change', function () {
        if($(this).is(':checked') ){
            $("input[name=manualScheduling]").prop('checked', false);
            ShowHideAutoScheduleFUPFromDiv();
        }
    });
    $("input[name=manualScheduling]").on('change', function () {
        if($(this).is(':checked') ){
            $("input[name=autoScheduling]").prop('checked', false);
            ShowHideAutoScheduleFUPFromDiv();
        }
        else {
            ShowHideAutoScheduleFUPFromDiv();
        }
    });
}

function updateAuthorizationMandatoryMark() {
    if ($('#isJapanProfile').is(':checked')) {
        $('#authorizationSpan').show();
    } else {
        $('#authorizationSpan').hide();
    }
}

function showHideSubmissionDateFromDiv() {
    if ($("#autoSubmit").is(":checked")) {
        list = [];
        var templateQueryList = $("#templateQueryList").find(".templateQuery-div");
        for (var i = 0; i < templateQueryList.length; i++) {
            if($("#templateQueries\\[" + i + "\\]\\.dynamicFormEntryDeleted").val() == 'false'){
                list.push($("#templateQueries\\[" + i + "\\]\\.distributionChannelName").val());
            }
        }
        if (list.includes(e2bDistFolder)) {
            $("#submissionDateFromDiv2").css('display','block');
            $("#submissionDateFromDiv1").css('display','none');
            updateSubmissionDateFromSelectBox();
        } else {
            $("#submissionDateFromDiv1").css('display','block');
            $("#submissionDateFromDiv2").css('display','none');
            updateSubmissionDateFromSelectBox();
        }

    } else {
        $("#submissionDateFromDiv1").css('display','none');
        $("#submissionDateFromDiv2").css('display','none');
        $("#submissionDateFrom").val(null);
    }
}

function updateSubmissionDateFromSelectBox() {
    var value = $("#submissionDateFrom").val();
    list = [];
    var templateQueryList = $("#templateQueryList").find(".templateQuery-div");
    for (var i = 0; i < templateQueryList.length; i++) {
        if($("#templateQueries\\[" + i + "\\]\\.dynamicFormEntryDeleted").val() == 'false'){
            list.push($("#templateQueries\\[" + i + "\\]\\.distributionChannelName").val());
        }
    }
    if (list.includes(e2bDistFolder)) {
        $('select[name="submissionDateFrom2"]').val(value).trigger('change').on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });
    } else {
        $('select[name="submissionDateFrom1"]').val(value).trigger('change').on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });
    }
}

function updateRuleEvaluationDropDown(isJapanCheckboxChanged) {
    $("#ruleEvaluation").val('');
    if ($('#isJapanProfile').is(':checked')) {
        bindSelect2WithUrl($("#ruleEvaluation"), ruleEvaluationListUrl + '?isJapanProfile=' + true, isJapanCheckboxChanged ? null : ruleEvaluationValueUrl, true).on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });
    } else {
        bindSelect2WithUrl($("#ruleEvaluation"), ruleEvaluationListUrl + '?isJapanProfile=' + false, isJapanCheckboxChanged ? null : ruleEvaluationValueUrl, true).on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });
    }
}

function updateHiddenField(value) {
    $("#submissionDateFrom").val(value);
}

function ShowHideAutoScheduleFUPFromDiv() {
    if ($("#manualScheduling").is(":checked")) {
        $("#autoScheduleFUPReport").attr("disabled",false);
    } else {
        $("#autoScheduleFUPReport").attr("disabled",true);
        $("input[name=autoScheduleFUPReport]").prop('checked', false);
    }
}

function ShowHideCalendarNameFromDiv() {
    if ($("#adjustDueDate").is(":checked")) {
        $("#calendarName").css('display','block');
    } else {
        $("#calendarName").css('display','none');
    }
}

function ShowHideAdjustmentDateOptionsFromDiv() {
    if ($("#adjustDueDate").is(":checked")) {
        $("#dueDateOptionsDiv").css('display','block');
    } else {
        $("#dueDateOptionsDiv").css('display','none');
        $("#dueDateAdjustmentDiv").css('display','none');
        $("#dueDateOptionsEnum").val('');
    }
}

function ShowHideAdjustmentDateFromDiv() {
    if($("#dueDateOptionsEnum").val() != ""){
        if ($("#dueDateOptionsEnum").val() == 'DO_NOT_ADJUST') {
            $("#dueDateAdjustmentDiv").css('display','none');
        } else {
            $("#dueDateAdjustmentDiv").css('display','block');
        }
    }else {
        $("#dueDateAdjustmentDiv").css('display','none');
    }
}

function ShowHideAdjustmentDateFromDiv1() {
    if ($("#dueDateOptionsEnum").val() == 'DO_NOT_ADJUST') {
        $("#dueDateAdjustmentDiv").css('display','none');
        $("#dueDateAdjustmentEnum").val('').trigger("change");
    } else {
        $("#dueDateAdjustmentDiv").css('display','block');
        $("#dueDateAdjustmentEnum").on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });
    }
}


function selectGlobalQueryOnChange(selectContainer) {
    var queryContainer = $(".globalQueryWrapper");
    var expressionValues = getExpressionValues(queryContainer);
    $(expressionValues).empty();
    if (getAJAXCount() == -1) {
        getBlankValuesForQueryAJAX($(selectContainer).val(), expressionValues, '');
        getCustomSQLValuesForQueryAJAX($(selectContainer).val(), expressionValues, '');
        getBlankValuesForQuerySetAJAX($(selectContainer).val(), expressionValues, '');
    } else {
        $(selectContainer).val(null).trigger('change');
    }
}


function getUnitConfigurationBasedOnOrg(id, orgType) {
    if (!id) {
        return
    }
    $.ajax({
        'url': ICSRPROFILECONF.searchDataBasedOnParam,
        'data': {
            'id': id
        },
        'success': function (data) {
            if (orgType === 'Recipient') {
                $("#recipientType").val(data.organizationType.name).trigger('change');
                $("#recipientCountry").val(data.organizationCountry).trigger('change');
            } else if (orgType === 'Sender') {
                $("#senderType").val(data.organizationType.name).trigger('change');
            }
        },
        'error': function (request, error) {
            alert("Request: " + JSON.stringify(request));
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

function formSubmit() {
     $("#saveButton").attr('disabled', 'disabled');
     $("#editUpdateButton").attr('disabled', 'disabled');
     $('form[name="icsrProfileConfigurationForm"]').trigger('submit');
}
