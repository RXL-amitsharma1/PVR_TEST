// TODO: POMI: This agency stuff doesn't do anything right now.
var controllerName = ""
var pageOpenDateTime = new Date();
$(function () {
    $("#sourceProfile").select2();
    $("#tenantDropDown").select2();
    $('#advancedOptions').hide();
    $('#agencyPartnerSelection').hide();
    $('#draft').parent().hide();
    $('#dateRangeType').select2();
    $("#agencyPartnerTags").select2({
        placeholder: "Select Agency / Partner",
        allowClear: true,
        width: "100%",
        tags: ["Alvogen", "EMA", "FDA", "PMDA"]
    });
    $('#isPeriodic').on('click', function () {
        var $this = $(this);
        if ($this.is(':checked')) {
            $('#agencyPartnerSelection').show();
            $('#draft').parent().show();
        } else {
            $('#agencyPartnerSelection').hide();
            $('#draft').parent().hide();
        }
    });

    $("#isTemplate").on("change", function () {
        if ($("#isTemplate").is(":checked")) {
            $("#suspectProduct").removeAttr("disabled");
            $("#includeAllStudyDrugsCases").removeAttr("disabled");
            $("input[id$='headerProductSelection']").removeAttr("disabled");
        } else {
            checkProductDependedntCheckboxes("#suspectProduct");
            checkProductDependedntCheckboxes("#includeAllStudyDrugsCases");
            checkProductDependedntCheckboxes("input[id$='headerProductSelection']")
        }
    });

    $("#errorflashmessage").on('click', function () {
        $(".errorflashmessage").hide();
    });

    $("#successFlashMsg").on('click', function () {
        $(".successFlashMsg").hide();
    });

    $("#warningFlashMsg").on('click', function () {
        $(".warningFlashMsg").hide();
    });

    $("#errorFlashMsg").on('click', function () {
        $(".errorFlashMsg").hide();
    });

    showHideExcludeFollowUpCheckBox();
    showHideIncludeNonSignificantFollowUpCheckBox();

    $(document).on("click", ".addReason", function () {
        addReason();
    });
    if ($(".addReason").length > 0) {
        addReason();
    }
    $(".alert-success .close").on('click', function() {
        $('.alert-success').hide();
    });

    $('.alert').on('close.bs.alert', function (event) {
        event.preventDefault();
        $('#editConfigErrorDiv').hide();
    });
    controllerName = $("#controllerName").val();

    $(document).on("click", "#copyBtn", function () {
        $("#copyBtn").attr('disabled', 'disabled');
    });

    $(document).on("data-clk", function (event, elem) {
        event.preventDefault();
        var elemClkData = JSON.parse(elem.attributes["data-evt-clk"].value)
        const methodName = elemClkData.method;
        const params = elemClkData.params;
        // Call the method from the eventHandlers object with the params
        switch (methodName) {
            case "beforeConfigurationFormSubmitWarningIfAny":
                beforeConfigurationFormSubmitWarningIfAny(...params);
                break;
            case "beforePeriodicFormSubmitWarningIfAny":
                beforePeriodicFormSubmitWarningIfAny(...params);
                break;
            case "beforeCaseSeriesFormSubmitWarningIfAny":
                beforeCaseSeriesFormSubmitWarningIfAny(...params);
                break;
            case "removeReason":
                removeReason();
                break;
            case "hideShowContent":
                hideShowContent(elem);
                break;
            case "saveAsEditConfig":
                saveAsEditConfig(...params);
                break;
            case "saveEditConfig":
                saveEditConfig(...params);
                break;
            case "showEmailConfiguration":
                showEmailConfiguration(...params);
                break;
            case "modalHide":
                $(params[0]).modal('hide');
                break;
        }
    });

    $("[data-evt-sbt]").on('submit', function() {
        const eventData = JSON.parse($(this).attr("data-evt-sbt"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'onFormSubmit') {
            var isForIcsrProfile = $(this).attr("data-isForIcsrProfile");
            return onFormSubmit(isForIcsrProfile);
        }
    });

    $("[data-evt-change]").on('change', function() {
        const eventData = JSON.parse($(this).attr("data-evt-change"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'showHideIncludeNonSignificantFollowUpCheckBox') {
            showHideIncludeNonSignificantFollowUpCheckBox();
        }
    });

    $('form#configurationForm').on('submit', function () {
        $('input[id$=blindProtected]').prop('disabled', false);
        $('input[id$=privacyProtected]').prop('disabled', false);
    });

});

var reasonResponseCounter = 0;

function addReason(responsible, reason) {
    reasonResponseCounter++
    $reasonResponsibleContainer = $(".reasonResponsibleContainer");
    var $rowDiv = $("<div class='row reasonResponsibleRow'></div>");
    if ($reasonResponsibleContainer.find(".reasonResponsibleRow").length == 0) {
        $rowDiv.append(" <div class=\"col-md-1\"><label style=\"font-size: 19px;margin-left: 20px;\"><span title=\"Most Important Reason\" class=\"fa fa-exclamation-circle\"></span></label></div>");
    } else {
        $rowDiv.append(" <div class=\"col-md-1\"><label style=\"font-size: 17px;margin-left: 20px; cursor: pointer\"><span title=\"Remove Reason\" class=\"fa fa-remove removeReason\" data-evt-clk=\'{\"method\": \"removeReason\", \"params\": []}\'>' ></span></label></div>");
    }
    var $responsibleDiv = $("<div class=\"col-md-5\"></div>");
    var $select = $reasonResponsibleContainer.find(".responsible");
    var $responsible = $select.clone();
    $responsible.removeClass("responsible");
    $responsible.attr("id", "responsible" + reasonResponseCounter);
    $responsibleDiv.append($responsible);
    $rowDiv.append($responsibleDiv);

    var $reasonDiv = $("<div class=\"col-md-6\"></div>");
    $select = $reasonResponsibleContainer.find(".reason");
    var $reason = $select.clone();
    $reason.removeClass("reason");
    $reason.attr("id", "reason" + reasonResponseCounter);
    $reasonDiv.append($reason);
    $rowDiv.append($reasonDiv);
    $reasonResponsibleContainer.append($rowDiv);

    $responsible.select2();
    $reason.select2();
    if (responsible) $responsible.select2("val", responsible);
    if (reason) $reason.select2("val", reason);
}

function removeReason(el) {
    $(el).closest(".row").remove()
}

function showDictionaryWidget(selector, allowClear) {
    var id = selector.attr("id");
    var type = selector.attr("data-type");
    var multiple = selector.attr("multiple");
    selector.select2({

        allowClear: (allowClear == null ? true : allowClear),
        width: "100%",
        formatNoMatches: function (term) {
            var create = "";

            create = "<a  id='addNew" + id + "' class='btn btn-success'> " + $.i18n._('create') + "</a>";

            return "<input readonly='readonly' class='form-control' id='newTerm" + id + "' value='" + term + "'>" + create;
        }
    }).parent().find(".select2-drop").on("click", "#addNew" + id, function () {
        var newTerm = $("#newTerm" + id).val();

        newTerm = encodeToHTML(newTerm);
        $.ajax({
            url: addDictionaryValueUrl,
            data: {term: newTerm, type: type},
            dataType: 'json'
        })
            .fail(function (err) {
                console.log(err);
            })
            .done(function (data) {
                $("<option>" + newTerm + "</option>").appendTo(selector);
                if (multiple) {
                    var vals = selector.select2("val");
                    vals.push(newTerm);
                    selector.val(vals).trigger('change');
                } else {
                    selector.val(newTerm).trigger('change');
                }
                selector.select2("close");		// close the dropdown
            })


    });
}


function advancedOptions(e) {
    if (e == 1) {
        $('#showOptions').hide();
        $('#advancedOptions').slideDown();
    } else {
        $('#advancedOptions').hide();
        $('#showOptions').show();
    }
}

function hideShowContent(e) {
    var getContent = $(e).parent().parent().find('.rxmain-container-content');
    var display = true;
    if ($(getContent).hasClass('rxmain-container-hide')) {
        display = false;
    }

    var getIcon;
    if (display) {
        getIcon = $(e).parent().find('i');
        $(getIcon).removeClass('fa-caret-down').addClass('fa-caret-right').trigger("classAdded");
        $(getContent).removeClass('rxmain-container-show').addClass('rxmain-container-hide');
    } else {
        getIcon = $(e).parent().find('i');
        $(getIcon).removeClass('fa-caret-right').addClass('fa-caret-down').trigger("classAdded");
        $(getContent).removeClass('rxmain-container-hide').addClass('rxmain-container-show');
        setSelect2InputWidth($(getContent).find("#emailUsers"))
    }
}

function onFormSubmit(isForIcsrProfile) {
    if (typeof setMultiselectValues != 'undefined') {
        setMultiselectValues();
    }
    setProductEventDictionary(); // && setBlankValues() && validateBlankValues();
    if (isForIcsrProfile) {
        return true;
    }
    var checkedNumber = checkNumberFields();
    if (checkedNumber) {
        $("#warningButton").attr('disabled', 'disabled');
        $("#saveAndRunButton").attr('disabled', 'disabled');
        $("#saveButton").attr('disabled', 'disabled');
        $("#disabledButton").attr('disabled', 'disabled');
        $("#editRunButton").attr('disabled', 'disabled');
        $("#editUpdateButton").attr('disabled', 'disabled');
    }
    $('#warningModal').modal('hide');
    return checkedNumber;
}

function beforeConfigurationFormSubmitWarningIfAny(actionUrl, checkReportExecutionStartDate) {
    var errorMessage = '';
    var cumulativeDateRangeCheck = false;
    var reportId = $("#reportId").val();
    //  TODO for now commented out check for startDate.
    // if (checkReportExecutionStartDate) {
    //     var selectedDate = $("div.scheduler #myDatePicker").datepicker("getDate");
    //     var todayDate = new Date();
    //     selectedDate.setSeconds(1);
    //     todayDate.setSeconds(0);
    //     todayDate.setMinutes(0);
    //     todayDate.setHours(0);
    //     if (selectedDate.getTime() < todayDate.getTime()) {
    //         alert($.i18n._('configuration.execution.start.date.error'));
    //         return false
    //     }
    // }

    if (controllerName !== "autoReasonOfDelay") {
        if (($('#productSelection').val() == '') && ($("#productGroupSelection").val() == "" || $("#productGroupSelection").val() == "[]") && ($('#studySelection').val() == '')) {
            errorMessage = $.i18n._('productAndStudiePageSelectionError');
        }
    }

    if (reportId) {
        if (actionUrl.indexOf('?') < 0) {
            actionUrl = actionUrl + "?reportId=" + reportId;
        } else {
            actionUrl = actionUrl + "&reportId=" + reportId;
        }
    }
    if ((typeof pvqType != 'undefined') && pvqType) {
        errorMessage = checkScheduledTime();
        showWarningOrSubmit(actionUrl, errorMessage);
        return;
    }
    errorMessage += checkCumulativeDateRange();
    if (controllerName !== "autoReasonOfDelay") {
        errorMessage += checkProductEnabledCheckboxes();
    }
    errorMessage += checkScheduledTime();
    errorMessage += checkQuerySelection();
    showWarningOrSubmit(actionUrl, errorMessage)
}

function beforePeriodicFormSubmitWarningIfAny(actionUrl, checkReportExecutionStartDate) {
    var showWarn = function () {
        // if (checkReportExecutionStartDate) {
        //     var selectedDate = $("div.scheduler #myDatePicker").datepicker("getDate");
        //     var todayDate = new Date();
        //     selectedDate.setSeconds(1);
        //     todayDate.setSeconds(0);
        //     todayDate.setMinutes(0);
        //     todayDate.setHours(0);
        //     if (selectedDate.getTime() < todayDate.getTime()) {
        //         alert($.i18n._('configuration.execution.start.date.error'));
        //         return false
        //     }
        // }
        var errorMessage = '';
        var reportId = $("#reportId").val();
        $("[name='parameterValue']").css("border", "1px solid #ccc");
        $("[name='parameterValue']").each(function () {
            var value = $(this).val();
            if (value.trim().indexOf("$eval") == 0)
                if (((value.indexOf("$report") > -1) || (value.indexOf("$previous") > -1)) &&
                    !(value.trim().indexOf("$eval") == 0) &&

                    !(
                        /^\$report\.section[0-9]+\.table$/.test(value) ||
                        /^\$report\.section[0-9]+\.data$/.test(value) ||
                        /^\$report\.section[0-9]+\.chart$/.test(value) ||
                        /^\$report\.source[0-9]+\.content$/.test(value) ||
                        /^\$report\.source[0-9]+\.data$/.test(value) ||
                        /^\$report\.section[0-9]+\.cell\[[0-9]+;[0-9]+\]$/.test(value) ||
                        /^\$report\.source[0-9]+\.cell\[[0-9]+;[0-9]+\]$/.test(value) ||
                        /^\$report\.source[0-9]+\.img$/.test(value) ||
                        /^\$report\.source[0-9]+\.img\[[0-9]+;[0-9]+\]$/.test(value) ||
                        /^\$previous\.section[0-9]+\.table$/.test(value) ||
                        /^\$previous\.section[0-9]+\.data$/.test(value) ||
                        /^\$previous\.section[0-9]+\.chart$/.test(value) ||
                        /^\$previous\.source[0-9]+\.content$/.test(value) ||
                        /^\$previous\.source[0-9]+\.data$/.test(value) ||
                        /^\$previous\.section[0-9]+\.cell\[[0-9]+;[0-9]+\]$/.test(value) ||
                        /^\$previous\.source[0-9]+\.cell\[[0-9]+;[0-9]+\]$/.test(value) ||
                        /^\$previous\.doc\.text[\[(]".*";".*"[\])]$/.test(value) ||
                        /^\$previous\.doc\.section[\[(]".*";".*"[\])]$/.test(value) ||
                        /^\$previous\.doc\.paragraph[\[(]".*";".*"[\])]$/.test(value)

                    )) {
                    $(this).css("border", "1px solid #ff0000");
                    errorMessage = "error"
                }
        });

        if (errorMessage != '') {
            $("#errorModal").modal("show");
            return false;
        }

        errorMessage += checkProductEnabledCheckboxes();
        errorMessage += checkCumulativeDateRange();
        errorMessage += checkScheduledTime();
        if (reportId) {
            if (actionUrl.indexOf('?') < 0) {
                actionUrl = actionUrl + "?reportId=" + reportId;
            } else {
                actionUrl = actionUrl + "&reportId=" + reportId;
            }
        }
        if (module == "pvp") {
            var source = validateAdditionalSourceForm()
            var template = validateTemplateForm()
            if (!source || !template) return false;
        }

        showWarningOrSubmit(actionUrl, errorMessage)
    }
    if ($("#reportJustificationModal").length > 0) {
        $("#reportJustificationModal .save").off().one("click", function () {
            $("#reportJustificationModal").modal("hide");
            showWarn();
        });
        $("#reportJustificationModal").modal("show");
    } else {
        showWarn();
    }
}

function beforeCaseSeriesFormSubmitWarningIfAny(actionUrl) {
    prepareGenerateSpotfireSettings();
    var errorMessage = '';
    if ($('#productSelection').val() == '' && ($("#productGroupSelection").val() == "" || $("#productGroupSelection").val() == "[]") && $('#studySelection').val() == '') {
        errorMessage = $.i18n._('productAndStudiePageSelectionError');
    }
    errorMessage += checkCumulativeDateRange();
    errorMessage += checkScheduledTime();
    showWarningOrSubmit(actionUrl, errorMessage)

}

//check if user have set run date before current to show warning message
function checkScheduledTime() {
    if ($("#scheduleDateJSON").val()) {
        var startDateTime = JSON.parse($("#scheduleDateJSON").val()).startDateTime;
        var deltaInMillis = (pageOpenDateTime - moment(startDateTime).valueOf());
        if (deltaInMillis > 60 * 60 * 1000) { //more then 1h before page open date/time
            return ' ' + $.i18n._('runDateBeforeCurrent');
        }
    }
    return '';
}

function checkCumulativeDateRange() {
    var CUMULATIVE = "CUMULATIVE";
    var cumulativeDateRangeCheck = false;
    $.each($('select[name*="dateRangeEnum"]'), function () {
        var sel = $(this);
        if ((sel.attr("name").indexOf("globalDateRangeInformation") > -1 ||
            sel.closest(".templateQuery-div").is(":visible")) && sel.select2('val') == CUMULATIVE) {
            cumulativeDateRangeCheck = true;
        }
    });
    if (cumulativeDateRangeCheck) {
        return ' ' + $.i18n._('cumulativeDateRangeError');
    }
    return '';
}

function checkQuerySelection() {
    var globalQevParamLength = $("#globalQueryWrapper").find($("[name*=qev][name*=value]")).length;
    if (globalQevParamLength > 0) {

        var value = "";
        var isAtLeastOneValueExist = false;
        for (var m = 0; m < globalQevParamLength; m++) {
            var tagName = $("[name='qev[" + m + "].value']")[0].nodeName.toLowerCase();
            var value = "";
            if (tagName == "select") {
                value = $("select[name='qev[" + m + "].value'").val();
            } else {
                value = $("input[name='qev[" + m + "].value'").val();
            }
            if (value != "" && value != undefined) {
                isAtLeastOneValueExist = true;
                break;
            }
        }
        if (!isAtLeastOneValueExist) {
            return '' + $.i18n._('app.select.template');
        }
    }
    var templateQuerylength = $("[id^=templateQuery]").find(".templateQuery-div").length;
    for (var i = 0; i < templateQuerylength; i++) {
        var qevParamLength = $("[name*=templateQuery" + i + "\\.][name*=qev][name*=value]").length;
        if (qevParamLength > 0) {
            var isAtLeastOneValueExist = false;
            for (var j = 0; j < qevParamLength; j++) {
                var tagName = $("[name^='templateQuery" + i + ".qev[" + j + "].value']")[0].nodeName.toLowerCase();
                var value = "";
                if (tagName == "select") {
                    value = $("select[name='templateQuery" + i + ".qev[" + j + "].value'").val();
                } else {
                    value = $("input[name='templateQuery" + i + ".qev[" + j + "].value'").val();
                }
                if (value != "" && value != undefined) {
                    isAtLeastOneValueExist = true;
                    break;
                }
            }
            if (!isAtLeastOneValueExist) {
                return '' + $.i18n._('app.select.template');
            }
        }
    }

    return '';
}

function checkProductEnabledCheckboxes() {
    if ($('#productSelection').val() == '' && ($("#productGroupSelection").val() == "" || $("#productGroupSelection").val() == "[]") && !$("#isTemplate").is(":checked") &&
        ($("#suspectProduct").is(":checked") || $("#includeAllStudyDrugsCases").is(":checked") || $("input[id$='headerProductSelection']").is(":checked"))) {
        var result = [];
        if ($("#suspectProduct").is(":checked")) result.push(" '" + $.i18n._("app.configuration.suspectProduct") + "'");
        if ($("#includeAllStudyDrugsCases").is(":checked")) result.push(" '" + $.i18n._("app.configuration.includeAllStudyDrugsCases") + "'");
        if ($("#headerProductSelection").is(":checked")) result.push("' " + $.i18n._("app.configuration.headerProductSelection") + "'");
        return ' ' + $.i18n._("app.configuration.checkbox.warning") + result.join(", ");
    }
    return '';
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

function showHideExcludeFollowUpCheckBox() {
    if ($("#includeNonSignificantFollowUp").prop('checked')) {
        $("#excludeFollowUp").attr('disabled', 'disabled');
    } else {
        $("#excludeFollowUp").attr('disabled', false);
    }
}

function showHideIncludeNonSignificantFollowUpCheckBox() {
    if ($("#excludeFollowUp").prop('checked')) {
        $("#includeNonSignificantFollowUp").attr('disabled', 'disabled');
    } else {
        $("#includeNonSignificantFollowUp").attr('disabled', false);
    }
}

function saveAsEditConfig(actionUrl, checkReportExecutionStartDate) {
    updateOrSaveModalConfig(actionUrl, checkReportExecutionStartDate);
}

function saveEditConfig(actionUrl, checkReportExecutionStartDate) {
    updateOrSaveModalConfig(actionUrl, checkReportExecutionStartDate);
}

function updateOrSaveModalConfig(actionUrl, checkReportExecutionStartDate) {
    var errorMessage = '';
    var reportId = $("#reportId").val();
    if (($('#productSelection').val() == '') && ($("#productGroupSelection").val() == "" || $("#productGroupSelection").val() == "[]") && ($('#studySelection').val() == '')) {
        errorMessage = $.i18n._('productAndStudiePageSelectionError');
    }
    if (reportId) {
        actionUrl = actionUrl + "?reportId=" + reportId;
    }
    errorMessage += checkCumulativeDateRange();
    errorMessage += checkProductEnabledCheckboxes();
    errorMessage += checkScheduledTime();
    showWarningOrSubmitConfig(actionUrl, errorMessage)
}

function showWarningOrSubmitConfig(actionUrl, errorMessage) {
    var formId = $("form#configurationForm");
    formId.attr("action", actionUrl);
    if (errorMessage != '') {
        $('#warningModal .description').text(errorMessage);
        $('#warningModal').modal('show');
        $('#warningButton').off('click').on('click', function () {
            $('#warningModal').modal('hide');
            //Save As & Run
            ajaxCallAction(actionUrl);
        });
    } else {
        ajaxCallAction(actionUrl);
    }
}

function ajaxCallAction(actionUrl) {
    $.ajax({
        type: 'POST',
        url: actionUrl,
        data: $('#configurationForm').serialize(),
        dataType: 'json',
        async: true
    })
        .done(function (data) {
            $('#editConfigErrorDiv').hide();
            $("#editConfigModal").modal('hide');
            if (actionUrl.indexOf("updateAdhocAjaxCall") > -1 && data.additionalData != '') {
                $("#version").val(parseInt(data.additionalData));
            }
        })
        .fail(function (jqXHR) {
            var messages = jQuery.parseJSON(jqXHR.responseText);
            var msgString = '';
            if ((messages.message).indexOf(";") == -1) {
                msgString = '<ul><li>' + messages.message + '</li></ul>';
            } else {
                var msgArrayStr = messages.message;
                var msgArray = msgArrayStr.split(";");
                for (var i = 0; i < msgArray.length; i++) {
                    msgString = msgString + '<ul><li>' + msgArray[i] + '</li></ul>';
                }
            }

            $('#editConfigErrorDiv').html('');
            $('#editConfigErrorDiv').show();
            $('#editConfigErrorDiv').append('<button type="button" class="close" data-dismiss="alert">\n' +
                '<span aria-hidden="true">&times;</span>\n' +
                '<span class="sr-only"><g:message code="default.button.close.label"/></span>\n' +
                '</button>');
            $('#editConfigErrorDiv').append(msgString);
            if (actionUrl.indexOf("updateAdhocAjaxCall") > -1 && messages.additionalData != '') {
                $("#version").val(parseInt(messages.additionalData));
            }
            $('#editConfigErrorDiv').show();
        });
}



