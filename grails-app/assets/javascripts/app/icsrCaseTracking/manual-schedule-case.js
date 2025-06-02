var isJapan = false;
var isDevice = false;
$(function () {
    var templateQueryObj = $("form#addToScheduleManualForm #templateQueryId");
    bindSelect2WithUrl(templateQueryObj, templateQueryListUrl + '?profileId=0', null, true).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });
    bindSelect2WithUrl($("form#addToScheduleManualForm #caseNumberWithVersion"), caseNumberWithVersionListUrl, null, true, null, 3).on("change", function (e) {
        initializeProdutDropDown();
        initializeApprovalNumberDropDown();
    }).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });
    bindSelect2WithUrl($("form#addToScheduleManualForm #profileId"), configurationsListUrl, null, true).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    }).on("change", function (e) {
        var data = $(this).select2('val');
        var profileData = $(this).select2('data');
        if (profileData && profileData !== undefined && profileData.length) {
            isJapan = profileData[0].isJapan;
            isDevice = profileData[0].isDevice;
        } else {
            isJapan = false;
            isDevice = false;
        }
        templateQueryObj.select2('destroy');
        if (data) {
            bindSelect2WithUrl(templateQueryObj, templateQueryListUrl + '?profileId=' + data, null, true).on("select2:open", function (e) {
                var searchField = $('.select2-dropdown .select2-search__field');
                if (searchField.length) {
                    searchField[0].focus();
                }
            });
        } else {
            bindSelect2WithUrl(templateQueryObj, templateQueryListUrl + '?profileId=0', null, true).on("select2:open", function (e) {
                var searchField = $('.select2-dropdown .select2-search__field');
                if (searchField.length) {
                    searchField[0].focus();
                }
            });
        }
        if(data){
            $.get(authorizationListUrl + '?profileId=' + data, null, function (response) {
                if (response.items.length === 1) {
                    /*If selected profile has authorization type selected,
                    then it should come on the authorization type dropdown.
                     Also it should be disabled.*/
                    var item = response.items[0];
                    $("form#addToScheduleManualForm #authorizationType").select2({
                        'data': [{
                            id: item.id,
                            text: item.text
                        }]
                    }).val(item.id).trigger('change');
                    if (isJapan == true) {
                        $("form#addToScheduleManualForm #authorizationTypeSpan").show();
                    } else {
                        $("form#addToScheduleManualForm #authorizationTypeSpan").hide();
                    }
                    $("#authorizationType").siblings('.warning-message').remove();
                    $("#authorizationType").attr('readonly', true);
                } else {
                    initializeAuthorizationDropDown();
                }
            });

        } else {
            initializeAuthorizationDropDown();
            showWarning("#authorizationType", $.i18n._("icsr.case.tracking.schedule.req.ProdType"));
        }

        initializeProdutDropDown();
        initializeApprovalNumberDropDown();
    });
    var manualCaseAddModal = $("#addToScheduleManual");
    manualCaseAddModal.on('hidden.bs.modal', function (e) {
        $('#addToManualCaseDlgErrorDiv').hide();
    });
    initializeProdutDropDown();
    initializeAuthorizationDropDown();
    initializeApprovalNumberDropDown();

    $("[data-evt-clk]").on('click', function(e) {
        e.preventDefault();
        const eventData = JSON.parse($(this).attr("data-evt-clk"));
        const methodName = eventData.method;
        const params = eventData.params;

        if (methodName == "showWarning") {
            showWarning(...params);
        }else if (methodName == "validateValues") {
            validateValues();
        }
    });

    $("[data-evt-onkeyup]").on('onkeyup', function() {
        const eventData = JSON.parse($(this).attr("data-evt-onkeyup"));
        const methodName = eventData.method;
        const params = eventData.params;

        if(methodName == 'checkDecimal') {
            var elem = $(this);
            checkDecimal(elem);
        }
    });
});

var initializeAuthorizationDropDown = function () {
    $("form#addToScheduleManualForm #authorizationType").val('').trigger('change');
    if (isJapan == true) {
        $("form#addToScheduleManualForm #authorizationTypeSpan").show();
    } else {
        $("form#addToScheduleManualForm #authorizationTypeSpan").hide();
    }
    var profileId = $("form#addToScheduleManualForm #profileId").select2('val');
    if (profileId && profileId.trim() !== "") {
        removeWarning("#authorizationType");
    } else {
        showWarning("#authorizationType", $.i18n._("icsr.case.tracking.schedule.req.ProdType"));
    }
    bindSelect2WithUrl($("form#addToScheduleManualForm #authorizationType"), authorizationListUrl + '?profileId=' +profileId, null, true).on("change", function (e) {
        initializeApprovalNumberDropDown();
    }).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });
};

var checkIfCaseProfileAreDeviceReportable = function () {
    var caseNumberAndVersionNumber = $("form#addToScheduleManualForm #caseNumberWithVersion").val();
    var profileId = $("form#addToScheduleManualForm #profileId").val();
    if (caseNumberAndVersionNumber != "" && caseNumberAndVersionNumber != undefined && profileId != "" && profileId != undefined) {
        $.ajax({
            url: checkAvailableDevice,
            data: {caseAndVersion: caseNumberAndVersionNumber, profileId: profileId},
            method: 'POST',
            dataType: 'json'
        })
            .done(function (result) {
                showDeviceReportingDropDown(result.caseNumber, result.versionNumber);
            })
            .fail(function (err) {
                $("form#addToScheduleManualForm #deviceIdDiv").hide();
            });
    } else {
        $("form#addToScheduleManualForm #deviceIdDiv").hide();
    }
};

var initializeProdutDropDown=function () {
    $("form#addToScheduleManualForm #deviceId").val('').trigger('change');
    if (isJapan == true || isDevice == true) {
        $("form#addToScheduleManualForm #deviceNumberSpan").show();
    } else {
        $("form#addToScheduleManualForm #deviceNumberSpan").hide();
    }
    var caseNumberAndVersionNumber = $("form#addToScheduleManualForm #caseNumberWithVersion").select2('val');
    var profileId = $("form#addToScheduleManualForm #profileId").select2('val');
    if (caseNumberAndVersionNumber && profileId && caseNumberAndVersionNumber.trim() !== "" && profileId.trim() !== "") {
        removeWarning("#deviceId");
    } else {
        showWarning("#deviceId", $.i18n._("icsr.case.tracking.schedule.req.prodName"));
    }
    bindSelect2WithUrl($("form#addToScheduleManualForm #deviceId"), deviceListUrl + '?caseVersionNumber=' + encodeURIComponent(caseNumberAndVersionNumber) + '&profileId=' + profileId, null, true).on("change", function (e) {
        initializeApprovalNumberDropDown();
    }).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });
};

var showDeviceReportingDropDown = function (caseNumber, versionNumber) {
    bindSelect2WithUrl($("form#addToScheduleManualForm #deviceId"), deviceListUrl + '?caseNumber=' + caseNumber + '&version=' + versionNumber, null, true).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });
    $("form#addToScheduleManualForm #deviceIdDiv").show();
    };

var initializeApprovalNumberDropDown = function() {
    $("form#addToScheduleManualForm #approvalNumber").val('').trigger('change');
    if (isJapan==true) {
        $("form#addToScheduleManualForm #approvalNumberSpan").show();
    } else {
        $("form#addToScheduleManualForm #approvalNumberSpan").hide();
    }
    var caseNumberAndVersionNumber = $("form#addToScheduleManualForm #caseNumberWithVersion").select2('val');
    var profileId = $("form#addToScheduleManualForm #profileId").select2('val');
    var authId = $("form#addToScheduleManualForm #authorizationType").select2('val');
    var prodHashCode = $("form#addToScheduleManualForm #deviceId").select2('val');
    if (caseNumberAndVersionNumber && profileId && authId && prodHashCode && caseNumberAndVersionNumber.trim() !== "" && profileId.trim() !== "" && authId.trim() !== "" && prodHashCode.trim() !== "") {
        removeWarning("#approvalNumber");
    } else {
        showWarning("#approvalNumber", $.i18n._("icsr.case.tracking.schedule.req.ApprovalNum"));
    }
    bindSelect2WithUrl($("form#addToScheduleManualForm #approvalNumber"), approvalListUrl + '?caseVersionNumber=' + encodeURIComponent(caseNumberAndVersionNumber) + '&profileId=' + profileId + '&authId=' + authId + '&prodHashCode=' + prodHashCode, null, true).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });
};

var checkDecimal = function (textbox) {
    var val = textbox.value;
    if (textbox.value.indexOf(".") !== -1) {
        textbox.value = val.split(".").join("");
    }
    if (textbox.value.indexOf("-") !== -1) {
        textbox.value = Math.abs(textbox.value);
    }
};

function validateValues() {
    var formId = $("form#addToScheduleManualForm");
    var caseNumber = $("form#addToScheduleManualForm #caseNumberWithVersion").val();
    var profileName = $("form#addToScheduleManualForm #profileId").val();
    var templateName = $("form#addToScheduleManualForm #templateQueryId").val();
    var dueIn = $("form#addToScheduleManualForm #dueInDays").val();
    if (caseNumber != "" && profileName != "" && templateName != "" && dueIn != "" && caseNumber.trim().length > 0 && profileName.trim().length > 0 && templateName.trim().length > 0 && dueIn.trim().length > 0  && checkAuthType() && checkProduct() && checkApprovalNumber()) {
        showLoader();
        formId.trigger('submit');
    } else {
        $('#addToManualCaseDlgErrorDiv').show();
    }
}

function checkAuthType() {
    var authId = $("form#addToScheduleManualForm #authorizationType").select2('val');
    if ((authId == null || authId.length == 0) && isJapan == true) {
        return false;
    } else {
        return true;
    }
}

function checkProduct() {
    var prodHashCode = $("form#addToScheduleManualForm #deviceId").select2('val');
    if ((prodHashCode == null || prodHashCode.length == 0) && (isDevice == true || isJapan == true)) {
        return false;
    } else {
        return true;
    }
}

function checkApprovalNumber() {
    var apporvalNumber = $("form#addToScheduleManualForm #approvalNumber").select2('val');
    if ((apporvalNumber == null || apporvalNumber.length == 0) && isJapan == true) {
        return false;
    } else {
        return true;
    }
}

function showWarning(selector, message) {
    var formElement = $(selector);
    var warningMessage = '<div class="warning-message" style="color:black; font-size:12px;">' + message + '</div>';
    if (!formElement.siblings('.warning-message').length) {
        formElement.after(warningMessage);
    }
    formElement.attr('readonly',true);
}

function removeWarning(selector) {
    var formElement = $(selector);
    formElement.attr('readonly',false);
    formElement.siblings('.warning-message').remove();
}