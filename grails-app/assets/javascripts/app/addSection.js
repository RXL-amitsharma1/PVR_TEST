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

var showHeaderFooterTitle = function () {
    var element = $(this);
    var headerFooterDiv = ($(element).parent().parent().parent());
    var advancedOptionDiv = headerFooterDiv.find('.headerFooterArea');
    advancedOptionDiv.toggle();

    if (advancedOptionDiv.is(':visible')) {
        $(this).text(LABELS.labelHideAdavncedOptions)
    } else {
        $(this).text(LABELS.labelShowAdavncedOptions)
    }
};

function getCustomSQLValuesForTemplateAJAXModal(templateId) {
    $.ajax({
        type: "GET",
        url: customSQLValuesForTemplateUrl + "?templateId=" + templateId,
        dataType: 'json'
    })
        .done(function (result) {
            var templateValuesContainer = $("#templateValuesContainer");
            templateValuesContainer.html("");
            if (result.length > 0) {
                appendNewCSQLContainers(result, templateValuesContainer[0], 'tv', "", 0);
            }
        })
        .fail(function () {
            console.log('Error retrieving custom SQL parameters');
        });
}

function getPOIInputsForTemplateAJAXModal(templateId) {
    $.ajax({
        type: "GET",
        url: poiInputsForTemplateUrl + "?templateId=" + templateId,
        dataType: "json"
    })
        .done(function (result) {
            var templateValuesContainer = $("#templateValuesContainer");
            templateValuesContainer.html("");
            if (result.data.length > 0) {
                appendNewPOIContainers(result.data, templateValuesContainer[0]);
            }
            rearrangePOIInputsCount();
            showHideSpecialFields();
        })
        .fail(function () {
            console.log('Error retrieving POI parameters');
        });
}

function bindAddActionQuerySelect2(selector) {
    return bindSelect2WithUrl(selector, querySearchUrl, queryNameUrl, true);
}

function bindAddSectionTemplateSelect2(selector) {
    return bindSelect2WithUrl(selector, templateSearchUrl, templateNameUrl, false);
}


function onFormSubmit() {
    setMultiselectValues();
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
    return checkedNumber
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
            if (!isEmpty(this.value) && !Number.isNaN(this.value)) {
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

$(function () {
    var addSectionModal = $("#addSectionModal");
    $("#eventModal .fa-pencil-square-o.copy-n-paste").hide();
    $(".removeIconBtn").off().on('click', function () {
        var sectionId = $(this).data('id');
        var sectiontitle = $(this).data('instancename');
        var confirmationModal = $("#confirmationModal");
        confirmationModal.modal("show");
        confirmationModal.find('.modalHeader').html($.i18n._('delete.confirm'));
        confirmationModal.find('.confirmationMessage').html($.i18n._('delete.section.confirm'));
        confirmationModal.find('.description').empty();
        confirmationModal.find('.description').text(sectiontitle);
        confirmationModal.find('.okButton').off().on('click', function () {
            $.ajax({
                type: "POST",
                url: removeSectionUrl + "?id=" + sectionId,
                dataType: "json"
            })
                .done(function (response) {
                    confirmationModal.modal('hide').data('bs.modal', null);
                    reloadPageAndShowResponseAlerts(response, null, function () {
                        location.reload(true);
                    });
                })
                .fail(function (error) {
                    var responseText = error.responseText;
                    var responseTextObj = JSON.parse(responseText);
                    if (responseTextObj.errors != undefined) {
                        alert("Sorry! Validation error");
                    } else {
                        alert(responseTextObj.message);
                    }
                })
        });
    });

    addSectionModal.on("show.bs.modal", function (e) {
        if (addSectionModal.is(':visible')) {
            return
        }

        showLoader();
        addSectionModal.find(".modal-content:first").load($(e.relatedTarget).data('url'), function () {
            hideLoader();
            addSectionModal.find('form').find('.selectQueryLevel').select2({
                dropdownParent: addSectionModal
            });
            addSectionModal.find('.periodicReport').find('.dateRangeEnumClass').select2();
            addSectionModal.find('.adhocReport').find('.dateRangeEnumClass').select2({
                dropdownParent: addSectionModal
            }).on('change', function () {
                dateRangeChangedAction(document);
            });

            $(".showHea¡derFooterArea").on('click', showHeaderFooterTitle);
            var selectedReportTemplate = addSectionModal.find("select[id$='template.id']");
            bindAddSectionTemplateSelect2(selectedReportTemplate).on('change', function () {
                selectedReportTemplate.parent().find('.requiredTemplate').hide();
                if (isForIcsrProfile == "true" || isForIcsrReport == "true") {
                    addSectionModal.find("#blindProtected").prop("checked", false);
                    addSectionModal.find("#privacyProtected").prop("checked", false);
                } else {
                    if ($(this).val() && (($(this).val() == cioms1Id) || ($(this).val() == medWatchId))) {
                        addSectionModal.find('.ciomsProtectedArea').removeAttr("hidden");
                        if (isUserBlinded) {
                            addSectionModal.find("#blindProtected").prop("checked", true);
                            addSectionModal.find("#blindProtected").prop("disabled", true);
                        }
                        if (isUserRedacted) {
                            addSectionModal.find("#privacyProtected").prop("checked", true);
                            addSectionModal.find("#privacyProtected").prop("disabled", true);
                        }
                    } else {
                        addSectionModal.find("#blindProtected").prop("checked", false);
                        addSectionModal.find("#privacyProtected").prop("checked", false);
                        addSectionModal.find('.ciomsProtectedArea').attr("hidden", "hidden");
                    }
                }
                getCustomSQLValuesForTemplateAJAXModal($(this).val());
                getPOIInputsForTemplateAJAXModal($(this).val());
                var templateContainer = getTemplateContainer(selectedReportTemplate);
                getGranularity($(selectedReportTemplate).val(), templateContainer);
                showHideReasssesDate($(this).val(), $(this).closest(".templateQuery-div"));
            });

            bindAddActionQuerySelect2(addSectionModal.find("[id$='query.id']")).on('change', function () {
                var selectQueryValuesContainer = document.getElementById("templateQueryValuesContainer");
                selectQueryValuesContainer.innerHTML = "";
                getBlankValuesForQueryAJAX($(this).val(), selectQueryValuesContainer, "");
                getCustomSQLValuesForQueryAJAX($(this).val(), selectQueryValuesContainer, "");
                getBlankValuesForQuerySetAJAX($(this).val(), selectQueryValuesContainer, "");
            });
            $(".showHeaderFooterArea").on('click', showHeaderFooterTitle);
            $(".footerSelect").each(function () {
                $(this).autocomplete({source: $(this).data("select"), minLength: 0});
            });
            $('.submit-section').on('click', function () {
                if (!onFormSubmit()) {
                    return
                }
                $.ajax({
                    type: "POST",
                    url: addNewSectionUrl,
                    data: addSectionModal.find('form').serialize(),
                    dataType: "json"
                })
                    .done(function (response) {
                        $("#addSectionModal").modal('hide').data('bs.modal', null);
                        reloadPageAndShowResponseAlerts(response, null, function () {
                            location.reload(true);
                        });
                    })
                    .fail(function (error) {
                        var responseText = error.responseText;
                        var responseTextObj = JSON.parse(responseText);
                        if (responseTextObj.errors != undefined) {
                            $.each(responseTextObj.errors, function (index, e) {
                                var field = addSectionModal.find('form').find('[name^="' + e + '"]');
                                if (field != undefined) {
                                    field.parent().addClass('has-error');
                                }
                            });
                        }
                        if (addSectionModal.find('form').find('.has-error').length == 0) {
                            $("#addSectionModal").find("#errorNotification").html(responseTextObj.message);
                            $("#addSectionModal").find(".alert-danger").removeClass("hide");
                            setTimeout(function() {
                                $("#addSectionModal").find(".alert-danger").addClass("hide");
                            }, 2000);
                        }
                    })
            });
        });

    });

    function dateRangeChangedAction(currentDocument) {
        var datePickerFromDiv = $(currentDocument.getElementById('executedDatePickerFromDiv'));
        var datePickerToDiv = $(currentDocument.getElementById('executedDatePickerToDiv'));
        var relativeDateRangeValue = $(currentDocument.getElementById('executedDateRangeInformationForTemplateQuery.relativeDateRangeValue'));

        var valueChanged = $(currentDocument.getElementById('executedDateRangeInformationForTemplateQuery.dateRangeEnum')).select2('val');
        if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
            datePickerFromDiv.show();
            datePickerToDiv.show();
            initializeDatePickersForEdit(currentDocument);
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

    function initializeDatePickersForEdit(currentDocument) {
        var dateRangeStart = null;
        var dateRangeEnd = null;

        var dateRangeStartAbsolute = currentDocument.getElementById('executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute');
        var dateRangeEndAbsolute = currentDocument.getElementById('executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute');
        if (dateRangeStartAbsolute.value) {
            dateRangeStart = dateRangeStartAbsolute.value;
        }

        if (dateRangeEndAbsolute.value) {
            dateRangeEnd = dateRangeEndAbsolute.value;
        }

        $(currentDocument.getElementById('executedDatePickerFromDiv')).datepicker({
            allowPastDates: true,
            date: dateRangeStart,
            twoDigitYearProtection: true,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });

        $(currentDocument.getElementById('executedDatePickerToDiv')).datepicker({
            allowPastDates: true,
            date: dateRangeEnd,
            twoDigitYearProtection: true,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });
    }

    $("[data-evt-sbt]").on('submit', function() {
        const eventData = JSON.parse($(this).attr("data-evt-sbt"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'onFormSubmit') {
            return onFormSubmit();
        }
    });
});