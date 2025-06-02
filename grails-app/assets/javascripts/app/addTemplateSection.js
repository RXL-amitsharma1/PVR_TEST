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
        dataType: "json",
        url: poiInputsForTemplateUrl + "?templateId=" + templateId,
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

function bindAddSectionTemplateSelect2(selector) {
    return bindSelect2WithUrl(selector, templateSearchUrl, templateNameUrl, false);
}


function onFormSubmit() {
    return isReportTemplateSelected();
}

function isReportTemplateSelected() {
    var selectedReportTemplate = $("#addTemplateSectionModal").find("select[id$='template.id']");
    if (!selectedReportTemplate.val()) {
        selectedReportTemplate.parent().find('.requiredTemplate').addClass('has-error').show();
        return false
    }
    return true
}

$(function () {
    var addSectionModal = $("#addTemplateSectionModal");
    // $("#eventModal .fa-pencil-square-o.copy-n-paste").hide();
    $(".removeSectionIconBtn").off().on('click', function () {
        var sectionId = $(this).data('id');
        var sectiontitle = $(this).data('instancename');
        var confirmationModal = $("#deleteConfirmation");
        confirmationModal.modal("show");
        confirmationModal.find('.modalHeader').html($.i18n._('delete.confirm'));
        confirmationModal.find('.confirmationMessage').html($.i18n._('delete.section.confirm'));
        confirmationModal.find('.description').empty();
        confirmationModal.find('.description').text(sectiontitle);
        confirmationModal.find('.okButton').off().on('click', function () {
            $.ajax({
                type: "POST",
                url: removeSectionUrl + "?id=" + sectionId,
                dataType: 'json'
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
            $(".showHea¡derFooterArea").on('click', showHeaderFooterTitle);
            var selectedReportTemplate = addSectionModal.find("select[id$='template.id']");
            bindAddSectionTemplateSelect2(selectedReportTemplate).on('change', function () {
                selectedReportTemplate.parent().find('.requiredTemplate').hide();
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
                getCustomSQLValuesForTemplateAJAXModal($(this).val());
                getPOIInputsForTemplateAJAXModal($(this).val());
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
                    url: $("#submitUrl").val(),
                    data: addSectionModal.find('form').serialize(),
                    dataType: 'json'
                })
                    .done(function (response) {
                        $("#addTemplateSectionModal").modal('hide').data('bs.modal', null);
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
                            alert(responseTextObj.message);
                        }
                    })
            });
        });

    });

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