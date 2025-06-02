var controllerName = "";

$(function () {

    controllerName = $("#controllerName").val();

    $(document).on('click', '.validate-copy-paste', function (evt) {
        var currentQEV = $(this).closest('.toAddContainerQEV')[0];
        var copyAndPasteModalId = $(currentQEV).find('.copyAndPasteModal').attr('id');
        var qevId = copyAndPasteModalId.slice(copyAndPasteModalId.indexOf('templateQuery'));
        if (controllerName === "autoReasonOfDelay") {
            qevId = copyAndPasteModalId.slice(copyAndPasteModalId.indexOf('queryRCA'));
        }
        var pasteContent = $(currentQEV).find('.copyPasteContent').val();
        var delimiter = getCopyAndPasteDelimiter(currentQEV);
        var selectedField = getCurrentSelectedField(currentQEV);

        if (delimiter != null) {
            if (delimiter == '|') {
                delimiter = '\\|'
            }
            pasteContent = pasteContent.replace(new RegExp('\\s*' + delimiter + '\\s*', 'g'), ';');
        }

        $.ajax({
            url: validateValue,
            type: "POST",
            mimeType: "multipart/form-data",
            data: {
                "selectedField": selectedField,
                "qevId": qevId,
                "values": pasteContent
            },
            dataType: 'json'
        })
            .done(function (resp) {
                var data = resp;
                if (data.success) {
                    showImportedValues(currentQEV, data, qevId, true);
                }
            })
            .fail(function (e) {
                alert(e);
            });
    });

    $(document).on('keyup change', 'input[name$=copyPasteValue], .expressionValueText', function (evt) {
        if (!$(this).val()) {
            var currentQEV = $(this).closest('.toAddContainerQEV')[0];
            if (currentQEV && $(currentQEV).length > 0) resetCopyPastField(currentQEV);
        }
    });

    $(document).on('click', '.confirm-paste', function (evt) {
        evt.preventDefault();
        var currentQEV = $(this).closest('.toAddContainerQEV')[0];

        var editorField = getValueTextFromExpression(currentQEV);
        var pasteContent = $(currentQEV).find('.copyPasteContent').val();
        var delimiter = getCopyAndPasteDelimiter(currentQEV);
        var selectedField = getCurrentSelectedField(currentQEV);

        if (delimiter != null) {
            if (delimiter == '|') {
                delimiter = '\\|'
            }
            pasteContent = pasteContent.replace(new RegExp('\\s*' + delimiter + '\\s*', 'g'), ';');
        }

        if (pasteContent != null && !_.isEmpty(pasteContent)) {
            $(editorField).val(pasteContent);
            $(currentQEV).find('.isFromCopyPaste').val('true');
            if ($(editorField).attr("name").indexOf("qev") == -1) {
                var nameParts = $($(editorField).parent().parent().find("[name*=qev]")[0]).attr("name").split(".");
                nameParts[(nameParts[0]).indexOf("qev") > -1 ? 1 : 2] = "copyPasteValue";
                $(editorField).attr("name", nameParts.join("."));
            }
            updateAJAXValuesCopyPaste(currentQEV);
        } else {
            resetCopyPastField(currentQEV)
        }

    });

    function resetCopyPastField(currentQEV) {
        $(getValueTextFromExpression(currentQEV)).val("");
        $(currentQEV).find('.isFromCopyPaste').val('false');
        $(currentQEV).find("#selectSelect, select.expressionValueSelectNonCache, select.expressionValueSelectAuto").val("").trigger('change');
        updateAJAXValues(currentQEV);
    }

    function getCurrentSelectedField(container) {
        return $(getFieldFromExpression(container)).select2("val");
    }

    // helper functions for the GUI
    function getCopyAndPasteDelimiter(container) {
        var selectedValue = $(container).find('input:radio[name^=delimiter]:checked').val();
        if (selectedValue === 'none') {
            return null;
        } else if (selectedValue === 'others') {
            var value = $(container).find('.c_n_p_other_delimiter').val();
            if (_.isEmpty(value)) {
                return null;
            } else {
                return value;
            }
        } else {
            return selectedValue;
        }
    }

    function updateAJAXValuesCopyPaste(container) {
        var field = $(getFieldFromExpression(container)).select2("val");
        var selectValue = getValueSelectFromExpression(container);
        $(selectValue).empty();
        showHideValue(EDITOR_TYPE_TEXT, container);
    }

    $(document).on('click', '.cancel', function (evt) {
        evt.preventDefault();
        var currentQEV = $(this).closest('.toAddContainerQEV')[0];
        $(currentQEV).find('.confirm-paste').show();
        $(currentQEV).find('.import-values').hide();
    });

    $(document).on('click', '.import-values', function (evt) {
        evt.preventDefault();
        var currentQEV = $(this).closest('.toAddContainerQEV')[0];
        var copyAndPasteModalId = $(currentQEV).find('.copyAndPasteModal').attr('id');
        var qevId = copyAndPasteModalId.slice(copyAndPasteModalId.indexOf('templateQuery'));
        if ($(currentQEV).find(':file').val()) {
            var file = $(currentQEV).find('#file_input');
            var jForm = new FormData();

            jForm.append("file", file.get(0).files[0]);
            jForm.append("qevId", qevId);
            jForm.append("selectedField", $(getFieldFromExpression(currentQEV)).select2("val"));
            $.ajax({
                url: importExcel,
                type: "POST",
                data: jForm,
                mimeType: "multipart/form-data",
                contentType: false,
                cache: false,
                processData: false,
                dataType: 'json'
            })
                .done(function (data) {
                    $response = data;
                    if ($response.success) {
                        showImportedValues(currentQEV, $response, qevId);
                    } else {
                        $(currentQEV).find('#noDataInExcel').show().text($response.message);
                        setTimeout(function () {
                            $(currentQEV).find("#noDataInExcel").hide();
                        }, 10000);
                    }
                });
        }
    });
    //};

    var showImportedValues = function (currentQEV, $response, qevId, validate) {
        if (validate) {
            $(currentQEV).find("#importValuesTemplateContainer").html($response.uploadedValues);
            var importValueModal = $('#importValueModal' + qevId);
            importValueModal.modal('show');
            var invalidValuesContainer = importValueModal.find('.invalidValuesContainer');
            confirmImportValues(currentQEV, importValueModal);
            importValueModal.find('#showWarnings').on('click', function () {
                if (invalidValuesContainer.attr('hidden')) {
                    invalidValuesContainer.removeAttr('hidden');
                } else {
                    invalidValuesContainer.attr('hidden', 'hidden');
                }
            });
        } else {
            $(currentQEV).find('.copyPasteContent').val($response.uploadedValues);
        }
        //Select comma(';) as a delimiter
        $(currentQEV).find('.copyAndPasteModal input:radio[name^=delimiter][value=' + '\\;' + ']').prop('checked', true);
        $(currentQEV).find('.confirm-paste').show();
        $(currentQEV).find('.import-values').hide();
    };

    var confirmImportValues = function (currentQEV, importValueModal) {
        importValueModal.off('click.import-values').on('click.import-values', '.confirm-import', function (evt) {
            evt.preventDefault();
            $(currentQEV).find('.copyPasteContent').val(validValues);

        });
    };

});

function getIsFromCopyPasteValue(container) {
    return $(container).find('.isFromCopyPaste').val() == 'true';
}

function getCopyPasteIcon(div) {
    return $(div).find('.copy-n-paste');
}

function addCopyPasteModal(toAdd, templateQueryNamePrefix, count) {
    var modalId = 'copyAndPasteModal' + templateQueryNamePrefix.replace(".", "") + 'qev' + count;
    $(toAdd).find('.copy-n-paste').attr('data-target', '#' + modalId).show();
    $(toAdd).find('#copyAndPasteModal').attr('id', modalId);
    $(toAdd).find('input:radio[id=delimiter_none]').attr('checked', 'checked');
    $(toAdd).find('input:radio[name^=delimiter]').attr('name', 'delimiter' + templateQueryNamePrefix.replace(".", "") + 'qev' + count);

    var currentModal = $('#' + modalId);
    fileChange(currentModal);
    fileSelect(modalId);
//Due to query/copyPasteModal.js had to put down off
    $(currentModal).off('show.bs.modal').on('show.bs.modal', function () {
        var currentQEV = $(this).closest('.toAddContainerQEV')[0];
        var valContainer = $(currentQEV).find('.expressionsNoPad:visible')[0];
        var pasteContent = $($(valContainer).find('#selectValue')).val(); // get text value
        if (getFieldIsValidatableFromExpression(currentQEV)) {
            currentModal.find('.validate-copy-paste').removeAttr('disabled');
        } else {
            $('.validate-copy-paste').attr('disabled', 'disabled');
        }
        if (pasteContent == undefined) {
            pasteContent = "";
            var list = $($(valContainer).find('#selectSelect')).val(); // get normal select value
            if (list == undefined) {
                // get none cache selectable value
                list = $($(this).closest('.queryBlankContainer').find('select.expressionValueSelectNonCache')[0]).val();
            }
            if (list == undefined) {
                // get auto complete value
                list = $($(this).closest('.queryBlankContainer').find('input.expressionValueSelectAuto')[0]).val();
            }
            if (list != undefined) {
                $.each(list, function () {
                    if (!(this instanceof HTMLElement))
                        pasteContent += this + ";"
                });
                pasteContent = pasteContent.substring(0, pasteContent.length - 1);
            }
        }

        if (pasteContent != undefined && !_.isEmpty(pasteContent)) {
            $(this).find('input:radio[name^=delimiter][value=";"]').prop('checked', true);
        } else {
            $(this).find('input:radio[name^=delimiter][value="none"]').prop('checked', true);
        }
        $(currentQEV).find('.copyPasteContent').val(pasteContent);
    });
}

var fileChange = function (currentModal) {
    currentModal.on('change', ':file', function () {
        var input = $(this);
        var numFiles = input.get(0).files ? input.get(0).files.length : 0;
        var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        var validExts = new Array(".xlsx", ".xls");
        var fileExt = label.substring(label.lastIndexOf('.'));
        if (numFiles > 0) {
            currentModal.find('.confirm-paste').hide();
            currentModal.find('.import-values').show();
            if (validExts.indexOf(fileExt.toLowerCase()) < 0) {
                currentModal.find('#fileFormatError').show();
                currentModal.find('.import-values').attr('disabled', 'disabled');
            } else {
                currentModal.find('#fileFormatError').hide();
                currentModal.find('.import-values').removeAttr('disabled');
                var currentQEV = $(this).closest('.toAddContainerQEV')[0];
            }
        } else {
            currentModal.find('#fileFormatError').hide();
            currentModal.find('.import-values').hide();
            currentModal.find('.confirm-paste').show();
        }
        input.trigger('fileselect', [numFiles, label]);
    });
};

var fileSelect = function (modalId) {
    $("#" + modalId + ' :file').on('fileselect', function (event, numFiles, label) {
        var input = $(this).parents('.input-group').find(':text');
        var log = numFiles > 0 ? label : "";
        if (input.length) {
            input.val(log);
        }
    });
};

