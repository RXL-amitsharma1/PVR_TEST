$(function () {
    var editingRow;
    var fetchButtonClicked = false;
    $(document).on("click", ".addParameter", function () {
        var clone = $(".parameterTemplateRow").clone();
        $(".parameterTable").append(clone);
        clone.removeClass("parameterTemplateRow");
        clone.show();
    });

    $(document).on("keyup", "[name=parameters\\.value]", function () {
        var row = $(this).closest("tr");
        var text_length = row.find("[name=parameters\\.value]").val().length;
        if (text_length === 0) {
            row.find("[name=hidden]").attr("disabled", true);
            row.find("[name=hidden]").attr("checked", false);
        } else if (text_length > 0) {
            row.find("[name=hidden]").removeAttr("disabled");
        }
    });

    $(document).on("click", ".addQuestAnswer", function () {
        var clone = $($(".answerValue")[0]).clone();
        clone.find(".questValue").val("");
        clone.find(".questAnswer").val("");
        $(".questTable").append(clone);
    });

    $(document).on("click", ".removeQuestAnswer", function () {
        var row = $(this).closest("tr");
        if ($(".answerValue").length > 1) {
            row.remove()
        } else {
            row.find(".questValue").val("");
            row.find(".questAnswer").val("");
        }
    });

    $(document).on("click", ".removeParameter", function () {
        $(this).closest("tr").remove()
    });

    $(document).on("click", ".questSave", function () {
        editingRow.find("[name=parameters\\.title]").val($("#question").val());
        result = [];
        $(".answerValue").each(function () {
            $this = $(this);
            if ($this.find(".questAnswer").val() != "") {
                result.push({answer: $this.find(".questAnswer").val(), value: $this.find(".questValue").val()})
            }
        });
        editingRow.find("[name=parameters\\.value]").val(JSON.stringify(result));
        if (JSON.stringify(result).length > 0)
            editingRow.find("[name=hidden]").removeAttr("disabled");
        $("#questModal").modal("hide");
    });

    $(document).on("click", ".editQuestButton", function () {
        editingRow = $(this).closest("tr");
        $("#question").val(editingRow.find("[name=parameters\\.title]").val());
        $(".answerValue:not(:first)").remove();
        $(".answerValue:first").val("");
        $(".questValue:first").val("");
        $(".questAnswer:first").val("");
        $(".questValue").find(".questValue").val("");
        $(".questAnswer").find(".questAnswer").val("");

        var json = editingRow.find("[name=parameters\\.value]").val();
        if (json && json !== "") {
            var data = JSON.parse(json);
            for (var i = 0; i < data.length; i++) {
                var row;
                if (i > 0) {
                    row = $($(".answerValue")[0]).clone();
                    $(".questTable").append(row);
                } else {
                    row = $(".answerValue")
                }
                row.find(".questValue").val(data[i].value);
                row.find(".questAnswer").val(data[i].answer);
            }
        }
        $("#questModal").modal("show");
    });

    $(document).on("click", ".parameterTypeSelect", function () {
        var row = $(this).closest("tr");
        row.find("[name=hidden]").attr("disabled", true);
        row.find("[name=hidden]").attr("checked", false);
        if ($(this).val() === "QUESTIONNAIRE") {
            row.find("[name=parameters\\.value]").hide();
            row.find(".editQuestButton").show();
        } else {
            row.find("[name=parameters\\.value]").show();
            row.find(".editQuestButton").hide();
        }
    });

    $(document).on("change", ".parameterTypeSelect", function () {
        var row = $(this).closest("tr");
        if ($(this).val() === "QUESTIONNAIRE") {
            row.find("[name=parameters\\.value]").hide();
            row.find(".editQuestButton").show();
            row.find("[name=parameters\\.value]").val("");
        } else {
            row.find("[name=parameters\\.value]").val("");
            row.find("[name=parameters\\.value]").show();
            row.find(".editQuestButton").hide();
        }
    });

    $('[name=file]').on('change', function (evt, numFiles, label) {
        $("#lockCode").remove();
        $('#fetchButton').show();
        $('#fetchButtonOneDrive').hide();
        $(".oneDriveEditorLink").remove();
        fetchButtonClicked = false;
        $("#file_name").val($.map($('[name=file]')[0].files, function (val) {
            return val.name;
        }).join(";"));
        if ($("#file_name").val())
            $("#fetchButton").attr('disabled', false);
        else
            $("#fetchButton").attr('disabled', true);
    });

    $('#file_name').on('change', function (e) {
        showLoader();
        if (!validateTemplateFileSize($('#file_input').get(0).files[0].size)) {
            return false;
        }
        var jForm = new FormData($('#createPublisherForm')[0]);
        $("#file_name").val($('#file_input').get(0).files[0].name);
        jForm.append("file", $('#file_input').get(0).files[0]);
        $.ajax({
            url: fetchParametersUrl,
            type: "POST",
            data: jForm,
            mimeType: "multipart/form-data",
            contentType: false,
            cache: false,
            processData: false,
            dataType: 'json'
        })
            .done(function (_data) {
                fillParams(JSON.parse(_data));
                hideLoader();
            })
            .fail(function (e) {
                removeParametersTableRows();
                $('#errorDiv p').text(JSON.parse(e.responseText)["error"]);
                $("#errorDiv").show();
                hideLoader();
            });
    });

    $('#fetchButton').on('click', function (evt) {
        $("#warningDiv").hide();
        $('.docWarning').html('').hide();
        showLoader();
        fetchButtonClicked = true;
        if (!validateTemplateFileSize($('#file_input').get(0).files[0].size)) {
            return false;
        }
        setHiddenCheckBoxValues();
        var jForm = new FormData($('#createPublisherForm')[0]);
        if ($('#id').val()) {
            jForm = new FormData($('#updatePublisherForm')[0]);
        }
        $("#file_name").val($('#file_input').get(0).files[0].name);
        jForm.append("file", $('#file_input').get(0).files[0]);
        $.ajax({
            url: fetchParametersUrl,
            type: "POST",
            data: jForm,
            mimeType: "multipart/form-data",
            contentType: false,
            cache: false,
            processData: false,
            dataType: 'json'
        })
            .done(function (_data) {
                fillParams(JSON.parse(_data));
                hideLoader();
            })
            .fail(function (e) {
                removeParametersTableRows();
                if (e.responseText) {
                    $('.docWarning').html(JSON.parse(e.responseText)["error"]).show();
                    $("#warningDiv").show();
                } else {
                    $("#file_input").val("");
                    $("#file_name").val("");
                    $('#errorDiv p').text($.i18n._('error.file.reload'));
                    $("#errorDiv").show();
                }
                hideLoader();
            });
    });

    $('#updateButton').on('click', function (evt) {
        setHiddenCheckBoxValues();
        var jForm = new FormData($('#updatePublisherForm')[0]);
        saveUpdateTemplate(updatePublisherTemplateUrl, jForm);
    })

    $('#saveButton').on('click', function (evt) {
        setHiddenCheckBoxValues();
        var jForm = new FormData($('#createPublisherForm')[0]);
        saveUpdateTemplate(savePublisherTemplateUrl, jForm);
    })

    function saveUpdateTemplate(url, jForm) {
        if ($('#file_input').get(0) && $('#file_input').get(0).files[0]) {
            if (!validateTemplateFileSize($('#file_input').get(0).files[0].size)) {
                return false;
            }
            if (!checkIfFetchButtonClicked()) {
                return false;
            }
            $("#file_name").val($('#file_input').get(0).files[0].name);
            jForm.append("file", $('#file_input').get(0).files[0]);
        }
        $.ajax({
            url: url,
            type: "POST",
            data: jForm,
            mimeType: "multipart/form-data",
            contentType: false,
            cache: false,
            processData: false,
            dataType: 'json'
        })
            .done(function (_data) {
                $(window).off('beforeunload');
                window.location.href = listPublisherTemplatesUrl + "?message=" + JSON.parse(_data)["message"];
            })
            .fail(function (e) {
                $('#errorDiv p').text(JSON.parse(e.responseText)["error"][0]);
                var errors = JSON.parse(e.responseText)["error"];
                for (var i = 1; i < errors.length; i++) {
                    $('#errorDiv p').append("<br>");
                    $('#errorDiv p').append(JSON.parse(e.responseText)["error"][i]);
                }
                $("#errorDiv").show();
            });
    }

    function setHiddenCheckBoxValues() {
        var hidden = $("[name='parameters.hidden']");
        hidden.value = "";
        var checkboxes = $("[name='hidden']");
        for (var i = 1; i < checkboxes.length; i++) {
            if (checkboxes[i].checked)
                hidden.value += '1' + "_";
            else
                hidden.value += '0' + "_";
        }
        $("[name='parameters.hidden']").val(hidden.value);
    }

    function validateTemplateFileSize(size) {
        var maxSizeInMB = pubWordTemplateSizeLimit / 1048576;
        if (size > pubWordTemplateSizeLimit) {
            $('#errorDiv p').text($.i18n._('publisher.large.file.error') + " " + maxSizeInMB + " MB.");
            $("#errorDiv").show();
            return false;
        }
        return true;
    }

    function checkIfFetchButtonClicked() {
        if (!fetchButtonClicked) {
            $('#errorDiv p').text($.i18n._('publisher.fetch.button.not.clicked.error'));
            $("#errorDiv").show();
            return false;
        }
        return true;
    }

    $(document).on('click', '.oneDriveEditorLink', function (evt) {
        $("#fetchButtonOneDrive").attr('disabled', false);
        $('#fetchButton').hide();
        $('#fetchButtonOneDrive').show();
    });

    $('#fetchButtonOneDrive').on('click', function (evt) {
        $.ajax({
            url: fetchParametersOneDriveUrl,
            data: {lockCode: $("#lockCode").val()},
            dataType: 'json'
        })
            .done(function (_data) {
                fillParams(_data);
            })
            .fail(function (e) {
                $("#errorDiv").text(e.responseText);
                $("#errorDiv").show();
            });
    });

    function fillParams(dataList) {
        var data = dataList[0];
        var showWarning = false;
        if (!data) {
            showInvalidParam(dataList[2]);
            return false;
        }
        removeParametersTableRows();
        for (var i in data) {
            var clone = $(".parameterTemplateRow").clone();
            $(".parameterTable").append(clone);
            clone.removeClass("parameterTemplateRow");
            clone.find("[name='parameters.name']").val(data[i].name);
            clone.find("[name='parameters.title']").val(data[i].name);
            clone.find("[name='parameters.description']").val(data[i].description);
            clone.find("[name='parameters.type']").val(data[i].type['name']);
            if (dataList[1]) {
                if (dataList[1][i] == 1) {
                    clone.find("[class='parameterTemplateRowCheckbox']").addClass("success-row");
                    showWarning = true;
                } else if (dataList[1][i] == -1) {
                    clone.find("[class='parameterTemplateRowCheckbox']").addClass("danger-row");
                    showWarning = true;
                }
            }
            clone.find("[name='parameters.value']").val(data[i].value);
            clone.find("[name='parameters.type']").trigger('click');
            clone.show();
            if (showWarning) {
                $(".parametersWarning").html($.i18n._('publisher.template.include.remove.parameter.warning')).show();
                $("#warningDiv").show();
            }
            updateParameterValueAutoComplete()
            showInvalidParam(dataList[2]);
        }
    }

    function showInvalidParam(data) {
        $(".docWarning").html('').hide();
        if (data && data.length > 0) {
            var errorMsg = $.i18n._('publisher.invalid.template.parameter.error') + " - ";
            for (var i in data) {
                errorMsg = errorMsg + '"' + data[i].name + '", ';
            }
            errorMsg = errorMsg.replace(/,\s*$/, "");
            $(".docWarning").html(errorMsg).show();
            $("#warningDiv").show();
        }
    }

    $('[name="pubDocTemplateButton"]').on('click', function () {
        $(".errorContent").html('');
        $(".errorDiv").hide();
    });

    $('[name="pubDocWarningButton"]').on('click', function () {
        $(".warningContent").html('');
        $(".warningDiv").hide();
    });

    function removeParametersTableRows() {
        $(".parameterTable tbody").find("tr:visible").remove();
    }

    $('#clearParamButton').on('click', function (evt) {
        if (confirm($.i18n._('publisher.clear.button.confirmation'))) {
            $(".parameterTable tbody").find("tr:visible").remove();
        }
    });

    function updateParameterValueAutoComplete() {
        var paramValueAutoComplete = ["$report.section1.table", "$report.section1.data", "$report.section1.chart", "$report.section1.cell[row;column]", "$report.source[].content", "$report.source[].data", "$report.source[].img", "$report.source[].img[pageN-pageM]", "$report.source[].cell[row;column]",
            "$previous.section1.table", "$previous.section1.data", "$previous.section1.chart", "$previous.section1.cell[row;column]", "$report.section1.range[row;column][row;column]", "$previous.source[].content", "$previous.source[].data", "$report.source[].range[row;column][row;column]",
            "$previous.doc.text[\"begin str\";\"end str\"]", "$previous.doc.text(\"begin str\";\"end str\")", "$previous.doc.paragraph[\"begin str\";\"end str\"]", "$previous.doc.paragraph(\"begin str\";\"end str\")",
            "$previous.doc.section[\"begin str\";\"end str\")"];

        $("[name=parameters\\.value]").each(function () {
            $(this).autocomplete({
                source: paramValueAutoComplete, minLength: 0, open: function () {
                    $("ul.ui-menu").width($(this).innerWidth())
                }
            });
        });
    }

});