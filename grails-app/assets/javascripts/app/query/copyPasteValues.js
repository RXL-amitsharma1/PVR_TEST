(function () {
    $("toAddContainer").ready(function () {
        var copyAndPasteDialog = $('#copyAndPasteModal');
        var $toAddContainer;

        // helper functions for the GUI
        var getCopyAndPasteDelimiter = function () {
            var selectedValue = $('#copyAndPasteModal input:radio[name^=delimiter]:checked').val();

            if (selectedValue === "none") {
                return null;
            } else if (selectedValue === "others") {
                var value = $('#copyAndPasteModal .c_n_p_other_delimiter').val();

                if (_.isEmpty(value)) {
                    return null;
                } else {
                    return value;
                }
            } else {
                return selectedValue;
            }
        };

        var resetDelimiterToNone = function (container) {
            $("input[name=delimiter][value='none']").prop('checked', true);
        };

        var resetDelimiterToSemicolon = function (container) {
            $("input[name=delimiter][value=';']").prop('checked', true);
        };

        function getCurrentPastedContent() {
            return $("#copyAndPasteModal .copyPasteContent").val();
        }

        function getCurrentSelectedField(container) {
            return $(getFieldFromExpression(container)).select2("val");
        }

        function getCurrentSelectedOption(container) {
            return $(getFieldFromExpression(container)).find(":selected");
        }

        function isFieldValidatable(container) {
            return getFieldIsValidatableFromExpression(container)
        }

        function setPasteContent(content) {
            $("#copyAndPasteModal .copyPasteContent").val(content);
        }

        var confirmCopyAndPasteDialog = function () {
            copyAndPasteDialog.off('click.confirm').on('click.confirm', '.confirm-paste', function (evt) {
                evt.preventDefault();
                var confirmButton = $(this);
                confirmButton.parentsUntil('.modal').parent().modal('hide');
                var pastedContent = getCurrentPastedContent();
                var selectedField = getCurrentSelectedField($toAddContainer);
                if (selectedField !== 'Select Field') {
                    $($toAddContainer).trigger("copyAndPaste:paste", [pastedContent]);
                }
                $(getValueTextFromExpression($toAddContainer)).attr("copyAndPasteWithDelimiter", "");
            })
        };

        var importValuesFromExcel = function () {
            copyAndPasteDialog.off('click.import').on('click.import', '.import-values', function (evt) {
                evt.preventDefault();
                if (copyAndPasteDialog.find(':file').val()) {
                    var file = $('#file_input').val();
                    var jForm = new FormData();
                    jForm.append("file", $('#file_input').get(0).files[0]);
                    jForm.append("selectedField", getCurrentSelectedField($toAddContainer));
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
                                showImportedValues($response);
                            } else {
                                $('#noDataInExcel').show().text($response.message);
                                setTimeout(function () {
                                    $("#noDataInExcel").hide();
                                }, 10000);
                            }
                        });
                }
            });
        };

        var showImportedValues = function ($response, validate) {
            if (validate) {
                $("#importValuesTemplateContainer").html($response.uploadedValues);
                $(".importValueModal").modal('show');
                confirmImportValues();
                $('#showWarnings').on('click', function () {
                    if ($('.invalidValuesContainer').attr('hidden')) {
                        $('.invalidValuesContainer').removeAttr('hidden');
                    } else {
                        $('.invalidValuesContainer').attr('hidden', 'hidden');
                    }
                });
            } else {
                setPasteContent($response.uploadedValues);
            }
            //Select comma(',') as a delimiter
            $('#copyAndPasteModal input:radio[name^=delimiter][value=' + '\\;' + ']').prop('checked', true);
            $('.confirm-paste').show();
            $('.import-values').hide();
        };

        var confirmImportValues = function () {
            $('.importValueModal').off('click.import-values').on('click.import-values', '.confirm-import', function (evt) {
                evt.preventDefault();
                setPasteContent(validValues);
            });
        };

        var initialize = function () {

            $(document).on("copyAndPaste:paste", function (evt, data) {
                var editorField = getValueTextFromExpression($toAddContainer);
                var delimiter = getCopyAndPasteDelimiter();
                var selectedField = getCurrentSelectedField($toAddContainer);

                if (delimiter != null) {
                    if (delimiter == '|') {
                        delimiter = '\\|'
                    }
                    data = data.replace(new RegExp('\\s*' + delimiter + '\\s*', 'g'), ';');
                }

                if (data != null && !_.isEmpty(data)) {
                    if (delimiter)
                        $(editorField).attr("copyAndPasteWithDelimiter", "true");
                    else
                        $(editorField).attr("copyAndPasteWithDelimiter", "false");
                    $(editorField).val(data).trigger('change'); //require to rebuild query JSON
                    $($toAddContainer).trigger('query.builder.updateAJAXValues');
                } else {
                    $(editorField).closest('.toAddContainer').find(".expressionOp").trigger("change");
                }
            });

            copyAndPasteDialog.on('show.bs.modal', function (evt) {
                var selectedField = getCurrentSelectedField($toAddContainer);
                var value = "";
                if (isFieldValidatable($toAddContainer)) {
                    $('.validate-copy-paste').removeAttr('disabled');
                } else {
                    $('.validate-copy-paste').attr('disabled', 'disabled');
                }
                if ($(getValueTextFromExpression($toAddContainer)).attr("copyAndPasteWithDelimiter") === "true") {
                    resetDelimiterToSemicolon($toAddContainer);
                    value = getCorrectSelectValueFromContainer($toAddContainer);
                    value = $(value).val();
                } else {
                    resetDelimiterToNone(copyAndPasteDialog);
                    if ($toAddContainer.find("#showValue").is(":visible")) {
                        value = getValueTextFromExpression($toAddContainer);
                    } else {
                        value = getCorrectSelectValueFromContainer($toAddContainer);
                        if ($(value).attr("name").indexOf("selectSelect") > -1)
                            resetDelimiterToSemicolon($toAddContainer);
                    }
                    value = $(value).val();
                }

                if (_.isArray(value)) {
                    value = _.filter(value, function (v) {
                        return !_.isEmpty(v)
                    }).join(";");
                    resetDelimiterToSemicolon($toAddContainer);
                }

                setPasteContent(value);
            });

            copyAndPasteDialog.on('change', ':file', function () {
                var input = $(this);
                var numFiles = input.get(0).files ? input.get(0).files.length : 0;
                var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
                var validExts = new Array(".xlsx", ".xls");
                var fileExt = label.substring(label.lastIndexOf('.'));
                if (numFiles > 0) {
                    $('.confirm-paste').hide();
                    $('.import-values').show();
                    if (validExts.indexOf(fileExt.toLowerCase()) < 0) {
                        $('#fileFormatError').show();
                        $('.import-values').attr('disabled', 'disabled');
                    } else {
                        $('#fileFormatError').hide();
                        $('.import-values').removeAttr('disabled');
                    }
                } else {
                    $('#fileFormatError').hide();
                    $('.import-values').hide();
                    $('.confirm-paste').show();
                }
                input.trigger('fileselect', [numFiles, label]);
            });

            $(':file').on('fileselect', function (event, numFiles, label) {
                var input = $(this).parents('.input-group').find(':text');
                var log = numFiles > 0 ? label : "";

                if (input.length) {
                    input.val(log);
                }
            });

            $('.showValue.showSelect').popover({
                content: function () {
                    return $(this).find("#selectValue").val()
                },
                show: true, trigger: 'hover'
            });

            $(document).on('click', '.validate-copy-paste', function (evt) {
                var pasteContent = getCurrentPastedContent();
                var delimiter = getCopyAndPasteDelimiter();
                var selectedField = getCurrentSelectedField($toAddContainer);

                if (delimiter != null) {
                    if (delimiter == '|') {
                        delimiter = '\\|'
                    }
                    pasteContent = pasteContent.replace(new RegExp('\\s*' + delimiter + '\\s*', 'g'), ';');
                }
                var isFaersQueryTarget = false
                 if (typeof FAERS_QUERY_TARGET_ENUM_VAL !== 'undefined') {
                    isFaersQueryTarget = $('#queryTarget').val() === FAERS_QUERY_TARGET_ENUM_VAL;
                 }

                $.ajax({
                    url: validateValue,
                    type: "POST",
                    mimeType: "multipart/form-data",
                    data: {
                        "selectedField": selectedField,
                        "values": pasteContent,
                        "isFaersTarget": isFaersQueryTarget
                    },
                    dataType: 'json'
                })
                    .done(function (resp) {
                        var data = resp;
                        if (data.success) {
                            showImportedValues(data, true);
                        }
                    })
                    .fail(function (e) {
                        alert(e);
                    });
            });

            copyAndPasteDialog.on('hidden.bs.modal', function (evt) {
                $toAddContainer = undefined;
            });

            confirmCopyAndPasteDialog();
            importValuesFromExcel();

            $('#queryBuilder, #advancedCustomExpression').on('click', '.modal-link', function (evt) {
                evt.preventDefault();
                $toAddContainer = $(this).closest('div .toAddContainer');
                var selectedFieldType = getFieldType(getCurrentSelectedOption($toAddContainer));
                if (!(selectedFieldType === RF_TYPE_DATE ||
                    selectedFieldType === RF_TYPE_PART_DATE))
                    copyAndPasteDialog.modal('show');
            })
        };

        initialize();
    })
})();