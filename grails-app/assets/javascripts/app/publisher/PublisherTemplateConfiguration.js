$(function () {
    var compositeValue = ["$"];
    var targetInput

    function renderCompositValue() {
        $("#parameterValueResult").val(compositeValue.join(""));
    }

    function setCompositeValuePart(index, part) {
        if (compositeValue.length <= index) compositeValue.push(part);
        if (compositeValue.length > index) compositeValue[index] = part;
    }

    function updateForNumber(N, val) {
        var maxNum = 5
        var selector = ""
        for (var i = N + 1; i <= maxNum; i++) {
            if (selector) selector += ",";
            selector += "#composer" + i;
        }
        $(selector).attr("disabled", true).val("");
        if (N < 4) {
            $(".composerParams").hide();
        }
        if (val != "") {
            $("#composer" + (N + 1)).attr("disabled", false);
            compositeValue = compositeValue.slice(0, N + 1);
        } else {
            compositeValue = compositeValue.slice(0, N);
        }
        renderCompositValue()
    }

    $(document).on("click", ".composerButton", function () {
        $(".unableToParseWarning").hide();
        updateForNumber(1, "");
        $("#composer1").val("");
        $("#composerModal").modal("show");
        targetInput = $(this).parent().find("textarea");
        var currentValue = targetInput.val();
        if (currentValue && currentValue.indexOf("$") == 0) {
            try {
                var currentValuePart = currentValue.split(".");
                if (currentValuePart.length > 0) {
                    $("#composer1").val(currentValuePart[0].substring(1, currentValuePart[0].length));
                    updateComposite1();
                }
                if (currentValuePart.length > 1) {
                    var type = "doc";
                    var sectionName;
                    if (currentValuePart[1].indexOf("section") == 0) {
                        type = "section";
                        sectionName = currentValuePart[1].substring(7, currentValuePart[1].length);
                        if (sectionName[0] == "[") sectionName = sectionName.substring(1, sectionName.length - 1);
                    } else if (currentValuePart[1].indexOf("source") == 0) {
                        type = "source";
                        sectionName = currentValuePart[1].substring(6, currentValuePart[1].length);
                        if (sectionName[0] == "[") sectionName = sectionName.substring(1, sectionName.length - 1);
                    }
                    $("#composer2").val(type);
                    updateComposite2();
                    if (sectionName) {
                        $("#composer3").val("[" + sectionName + "]");
                        updateComposite3();
                    } else {
                        setCompositeValuePart(3, "")
                    }
                }
                if (currentValuePart.length > 2) {
                    var type;
                    var params;
                    if (currentValuePart[2].indexOf("[") > 0) {
                        type = currentValuePart[2].substring(0, currentValuePart[2].indexOf("["));
                        params = currentValuePart[2].substring(currentValuePart[2].indexOf("[") + 1, currentValuePart[2].length - 1);
                    } else if (currentValuePart[2].indexOf("(") > 0) {
                        type = currentValuePart[2].substring(0, currentValuePart[2].indexOf("("));
                        params = currentValuePart[2].substring(currentValuePart[2].indexOf("(") + 1, currentValuePart[2].length - 1);
                    } else {
                        type = currentValuePart[2];
                    }
                    $("#composer4").val(type);
                    updateComposite4();
                    if (type == "cell") {
                        params = params.split(";");
                        $("#composer5").val(params[0]);
                        $("#composer6").val(params[1]);
                        $("#composer6").trigger("change");
                    }
                    if (type == "range") {
                        params = params.split("][");
                        $("#composer7").val(params[0].split(";")[0]);
                        $("#composer8").val(params[0].split(";")[1]);
                        $("#composer9").val(params[1].split(";")[0]);
                        $("#composer10").val(params[1].split(";")[1]);
                        $("#composer10").trigger("change");
                    }

                    if (type == "text" || type == "paragraph" || type == "bookmark") {
                        params = params.split('";"');
                        $("#composer11").val(params[0].substring(1));
                        $("#composer12").val(params[1].substring(0, params[1].length - 1));
                        $("#includingStartsFrom").prop("checked", currentValuePart[2].indexOf("[") > -1);
                        $("#includingEndsWith").prop("checked", currentValuePart[2].indexOf("]") > -1);
                        $("#composer11").trigger("change");
                    }
                    if (type == "img") {
                        if (params) {
                            params = params.split('-');
                            $("#composer13").val(params[0]);
                            $("#composer14").val(params[1]);
                        } else {
                            $("#composer13").val("");
                            $("#composer14").val("");
                        }
                        $("#composer14").trigger("change");
                    }
                }
            } catch (e) {
                $(".unableToParseWarning").show();
                updateForNumber(1, "")
            }
        }
    });
    $(document).on("change", "#composer1", function () {
        updateComposite1();
    });

    function updateComposite1(select) {
        var val = $("#composer1").val()
        if (val == "report") {
            setCompositeValuePart(1, "report.");
            $("option[value=doc]").hide();
        }
        if (val == "previous") {
            setCompositeValuePart(1, "previous.");
            $("option[value=doc]").show();
        }
        updateForNumber(1, val)
    }

    $(document).on("change", "#composer2", function () {
        updateComposite2();
    });

    function updateComposite2() {
        var val = $("#composer2").val()
        if (val == "section") {
            setCompositeValuePart(2, "section");
            $("option[type=section]").show();
            $("option[type=source]").hide();
            $("option[type=doc]").hide();
        }
        if (val == "source") {
            setCompositeValuePart(2, "source");
            $("option[type=section]").hide();
            $("option[type=doc]").hide();
            $("option[type=source]").show();
        }
        if (val == "doc") {
            setCompositeValuePart(2, "doc");
            $("option[type=section]").hide();
            $("option[type=source]").hide();
            $("option[type=doc]").show();
        }
        updateForNumber(2, val);
        initComposer3();
    }

    function initComposer3() {
        var select = $("#composer3");
        select.empty();
        select.append("<option value=''></option>");
        var value = $("#composer2").val();
        if (value == "doc") {
            select.empty();
            select.append("<option value=''> - </option>");
            $("#composer4").attr("disabled", false);
            setCompositeValuePart(3, "")
        }
        if (value == "source") {
            var localSources = []
            $("input[name=attachmentName]").each(function (index) {
                var name = $(this).val();
                if (name) {
                    var fileType = $(this).closest("tr").find(".fileType").val();
                    localSources.push(name);
                    select.append("<option fileType='" + fileType + "' value='[" + name + "]'>" + name + "</option>");
                }
            });
            var commonPublisherSources = $("#commonPublisherSources").val();
            if (commonPublisherSources) {
                var sources = commonPublisherSources.split("@");
                for (var i = 0; i < sources.length; i++) {
                    if (localSources.indexOf(sources[i]) == -1)
                        select.append("<option value='[" + sources[i] + "]'>" + sources[i] + " (common) </option>");
                }
            }
        }
        if (value == "section") {
            if ($(".templateWrapperRow").length > 0) {
                $(".templateWrapperRow").each(function (index) {
                    var templateSelect = $(this).find("input.selectTemplate")
                    if (templateSelect.attr("name").indexOf("clone") == -1) {
                        var label = $(this).find("[name$=title]").val();
                        if (!label) label = templateSelect.select2("data").text;
                        select.append("<option value='[" + (index + 1) + "]'>" + label + "</option>");
                    }
                });
            } else {
                $("#reportTab table tbody tr  td:first-child a").each(function (index) {
                    var label = $(this).text()
                    if (label) {
                        select.append("<option value='[" + (index + 1) + "]'>" + label.trim() + "</option>");
                    }
                });

            }
        }
    }

    $(document).on("change", "#composer3", function () {
        updateComposite3();
    });

    function updateComposite3() {
        var val = $("#composer3").val();
        var fileType = $("#composer3  option:selected").attr("fileType");
        if (fileType) {
            $("#composer4 option[sourceType]").hide();
            $('#composer4 option[sourceType*="' + fileType + '"]').show();
        }
        setCompositeValuePart(3, val);
        updateForNumber(3, val)
    }

    $(document).on("change", "#composer4", function () {
        updateComposite4();
    });

    function updateComposite4() {
        var val = $("#composer4").val();
        setCompositeValuePart(4, "." + val);
        updateForNumber(4, val);
        showRangeParams(val);
    }

    function showRangeParams(type) {
        $(".composerParams").hide();
        if (type == "cell") {
            $("#composerCellParams").show();
            $("#composer5").trigger("change");
        } else if (type == "range") {
            $("#composerRangeParams").show();
            $("#composer7").trigger("change");
        } else if (type == "text" || type == "paragraph" || type == "bookmark") {
            $("#composerTextParams").show();
            $("#composer11").trigger("change");
        } else if (type == "img") {
            $("#composerImgParams").show();
            $("#composer13").trigger("change");
        } else {
            compositeValue = compositeValue.slice(0, 5);
            renderCompositValue();
        }
    }

    $(document).on("change", "#composer5, #composer6", function () {
        var row = $("#composer5").val();
        var column = $("#composer6").val();
        var val = ""
        if (row && column) val = "[" + row + ";" + column + "]"
        setCompositeValuePart(5, val);
        updateForNumber(5, val);
    });
    $(document).on("change", "#composer13, #composer14", function () {
        var from = $("#composer13").val();
        var to = $("#composer14").val();
        var val = ""
        if (from && to) val = "[" + from + "-" + to + "]"
        setCompositeValuePart(5, val);
        updateForNumber(5, val);
    });
    $(document).on("change", "#composer7, #composer8,#composer9, #composer10", function () {
        var from_row = $("#composer7").val();
        var from_column = $("#composer8").val();
        var to_row = $("#composer9").val();
        var to_column = $("#composer10").val();
        var val = ""
        if (from_row && from_column && to_row && to_column) val = "[" + from_row + ";" + from_column + "][" + to_row + ";" + to_column + "]"
        setCompositeValuePart(5, val);
        updateForNumber(5, val);
    });
    $(document).on("change", "#composer11, #composer12,#includingStartsFrom, #includingEndsWith", function () {
        var from = $("#composer11").val();
        var from_inc = $("#includingStartsFrom").is(":checked");
        var to = $("#composer12").val();
        var to_inc = $("#includingEndsWith").is(":checked");
        var val = ""
        if (from && to) val = (from_inc ? "[" : "(") + '"' + from + '";"' + to + '"' + (to_inc ? "]" : ")")
        setCompositeValuePart(5, val);
        updateForNumber(5, val);
    });
    $(document).on("click", "#composerSave", function () {
        targetInput.val($("#parameterValueResult").val());
        $("#composerModal").modal("hide");
    });
    $(document).on("change", ".publisherTemplateSelect", function () {
        var $table = $(this).closest(".publisherContainer").find(".table"),
            $tableBody = $table.find("tbody");
        if ($(this).val() != "") {
            $.ajax({
                url: publisherTemplateParametersUrl + "?id=" + $(this).val(),
                type: 'get',
                dataType: 'json'
            })
                .fail(function (err) {
                    alert("Something went wrong!");
                })
                .done(function (data) {
                    var parameters = data;

                    $tableBody.find(".parameterRow").remove();
                    for (var i = 0; i < parameters.length; i++) {

                        if (parameters[i].hidden)
                            var row = " <tr class=\"parameterRow\" style='display: none'>\n"
                        else
                            var row = " <tr class=\"parameterRow composerButtonContainer\">\n"
                        row += "                                    <td>" + parameters[i].name + "</td>\n" +
                            "                                    <td>" + parameters[i].title + " <span class=\"fa fa-info-circle\" title=\"" + (parameters[i].description ? parameters[i].description : "") + "\"></span></td>\n" +
                            "                                    <td><textarea style='height: 35px;' name='parameterValue' class='form-control " + (parameters[i].type.name === "QUESTIONNAIRE" ? "editQuest" : "") + "'>"
                            + ((parameters[i].value && parameters[i].type.name !== "QUESTIONNAIRE") ? parameters[i].value : "") + "</textarea>" +
                            " <span class=\"composerButton\" >" + $.i18n._('compose.parameter') + "</span>" +
                            "</td>" +
                            "                                    <input type='hidden' name='parameterName' value='" + parameters[i].name + "'/>" +
                            "                                    <input type='hidden' class='parameterTitle'  value='" + parameters[i].title + "'/>" +
                            "                                    <input type='hidden' class='parameterValue'  value='" + escape(parameters[i].value) + "'/>" +
                            "                                </tr>";
                        $tableBody.append(row);
                        $table.show();
                        updateParameterValueAutoComplete();
                    }
                });
        } else {
            $table.hide();
        }
    });

    $(".updatebtn").on("click", function () {
        $(".crossDiv").hide()
    });
    $(".cross").on("click", function () {
        $(".crossDiv").hide()
    });

    $(document).on("click", ".removeButton", function () {
        $(this).closest("tr").find(".removeDownloadButton").hide();

    });

    var editingParamInput;

    $(document).on("change", ".questAnswers", function () {
        $(".questValue").val($(this).val());
    });

    $(document).on("click", ".questSave", function () {
        editingParamInput.val($(".questValue").val());
        $("#questModal").modal("hide");
    });

    $(document).on("click", ".editQuest", function () {
        editingParamInput = $(this);
        $(".questQuestion").html(editingParamInput.closest("tr").find(".parameterTitle").val());
        var answers = editingParamInput.closest("tr").find(".parameterValue").val();
        answers = unescape(answers);
        var $select = $(".questAnswers");
        $select.find("option").remove();
        $(".answer").html("");
        if (!_.isEmpty(answers)) {
            var jsonAnswers = JSON.parse(answers);
            for (var i = 0; i < jsonAnswers.length; i++) {
                var opt = $("<option>" + jsonAnswers[i].answer + "</option>");
                opt.attr("value", jsonAnswers[i].value);
                opt.appendTo($select);
            }
            $select.trigger("change");
            $("#questModal").modal("show");
        }
    });

    $('#configurationForm, .updatePublisherSectionForm').on('submit', function () {
        $(".publisherParameterInput").each(function (index) {
            var container = $(this).closest(".publisherContainer");
            var json = createParamsJson(container);
            $(this).val(JSON.stringify(json));
        });
    });

    function createParamsJson(container) {
        var templateId = $(container).find(".publisherTemplateSelect").length > 0 ? $(container).find(".publisherTemplateSelect").val() : 0
        var result = {templateId: templateId, parameterValues: {}},
            $table = $(container).find(".table"),
            $parameterName = $table.find("[name=parameterName]"),
            $parameterValue = $table.find("[name=parameterValue]");
        for (var i = 0; i < $parameterName.length; i++) {
            result.parameterValues[$($parameterName[i]).val()] = $($parameterValue[i]).val();
        }
        return result;
    }

    $(".publisherParameterInput").each(function (index) {
        if ($(this).val() != "") {
            $(this).closest(".publisherContainer").find(".table").show()
        }
    });

    $(document).on("click", ".publisherSectionAddTemlate", function () {
        var clone = $(".publisherSectionRowTemplate").clone().show();
        clone.removeClass("publisherSectionRowTemplate");
        clone.addClass("templateTableRow");
        $(".publisherSectionsTable").append(clone);
        bindMultipleSelect2WithUrl(clone.find(".publisherReportingDestinations"), reportingDestinationsUrl, false, false, null);
        bindSelect2WithUrl(clone.find(".publisherTemplateSelect"), PVPTemplateSearchUrl, PVPTemplateNameUrl, false);
        bindSelect2WithUrl(clone.find("[name=publisherSectionTaskTemplate]"), PVPTaskTemplateSearchUrl, PVPTaskTemplateNameUrl, true);
    });

    $(document).on("click", ".attachmentSectionAdd", function () {
        var clone = $(this).closest("table").find(".rowTemplate").clone().show();
        clone.removeClass("rowTemplate");
        $(this).closest("table").find("tbody").append(clone);
    });

    $(document).on("change", ".fileType", function () {
        if ($(this).val() == "WORD") {
            $(this).closest("tr").find(".file_input").attr("accept", ".docx")
        } else if ($(this).val() == "EXCEL") {
            $(this).closest("tr").find(".file_input").attr("accept", ".xlsx")
        } else if ($(this).val() == "PDF") {
            $(this).closest("tr").find(".file_input").attr("accept", ".pdf")
        } else if ($(this).val() == "IMAGE") {
            $(this).closest("tr").find(".file_input").attr("accept", ".jpg, .jpeg,.png, tiff, gif,bmp")
        }
        if ($(this).val() == "XML") {
            $(this).closest("tr").find(".file_input").attr("accept", ".xml")
        }
        if ($(this).val() == "JSON") {
            $(this).closest("tr").find(".file_input").attr("accept", ".json")
        }
    })

    $(document).on("change", ".fileSource", function () {
        $(this).closest("tr").find(".attachmentPath").hide();
        $(this).closest("tr").find(".attachmentOneDriveDiv").hide();
        $(this).closest("tr").find(".fileName").parent().hide();
        $(this).closest("tr").find(".attachmentScript").hide();
        if ($(this).val() == "FILE") {
            $(this).closest("tr").find(".fileName").parent().show();
        } else if ($(this).val() == "ONEDRIVE") {
            $(this).closest("tr").find(".attachmentOneDriveDiv").show();
        } else if ($(this).val() == "SERVICE") {
            $(this).closest("tr").find(".attachmentScript").show();
        } else {
            $(this).closest("tr").find(".attachmentPath").show();
        }
    });

    $(".fileType").trigger("change");
    $(".fileSource").trigger("change");

    $(document).on("change", ".file_input", function () {
        $(this).closest("div").find(".fileName").val($(this).get(0).files[0].name);
        $(".btn").attr('disabled', false);
    });
    $(document).on("click", '.fetchButton', function (evt) {
        var container = $(this).closest(".publisherContainer");
        var jForm = new FormData();
        jForm.append("file", container.find('.file_input').get(0).files[0]);
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
                container.find(".parameterTable tbody").find("tr:visible").remove();
                var data = JSON.parse(_data);
                for (var i in data[0]) {
                    var clone = $($(".parameterTemplateRow")[0]).clone();
                    container.find(".parameterTable").append(clone);
                    clone.removeClass("parameterTemplateRow");
                    clone.find("[name='parameterName']").val(data[0][i].name);
                    clone.find(".parametersNameSpan").html(data[0][i].name);
                    clone.find("[name='parameterValue']").attr("placeholder", data[0][i].description);
                    clone.show();
                }
                $(".fetchParametersError").hide();
                updateParameterValueAutoComplete();
                showInvalidParam(data[2]);
            })
            .fail(function (e) {
                $("#errorDiv").text(e.responseText);
                $("#errorDiv").show();
                if (JSON.parse(e.responseText)["error"]) {
                    $(".fetchParametersError p").text(JSON.parse(e.responseText)["error"]);
                    $(".fetchParametersError").show();
                    container.find(".parameterTable tbody").find("tr:visible").remove();
                }
            });
    });

    function showInvalidParam(data) {
        if (data && data.length > 0) {
            var errorMsg = $.i18n._('publisher.invalid.template.parameter.error') + " - ";
            for (var i in data) {
                errorMsg = errorMsg + '"' + data[i].name + '", ';
            }
            errorMsg = errorMsg.replace(/,\s*$/, "");
            $(".fetchParametersError p").text(errorMsg);
            $(".fetchParametersError").show();
        } else $(".fetchParametersError").hide();
    }

    $(document).on("click", '[name="pubDocErrorClose"]', function () {
        $(".fetchParametersError p").text('');
        $(".fetchParametersError").hide();
    });

    $(document).on("click", ".publisherSectionAddFile", function () {
        var clone = $(".publisherSectionRowFile").clone().show();
        clone.removeClass("publisherSectionRowFile");
        clone.addClass("templateTableRow");
        $(".publisherSectionsTable").append(clone);
        bindMultipleSelect2WithUrl(clone.find(".publisherReportingDestinations"), reportingDestinationsUrl, false, false, null);
        bindSelect2WithUrl(clone.find(".publisherTemplateSelect"), PVPTemplateSearchUrl, PVPTemplateNameUrl, false);
        bindSelect2WithUrl(clone.find("[name=publisherSectionTaskTemplate]"), PVPTaskTemplateSearchUrl, PVPTaskTemplateNameUrl, true);
    });

    $(document).on("click", ".removeParameter, .publisherSectionRemove, .attachmentSectionRemove, attachmentSectionRemove", function () {
        if ($(this).is(".publisherSectionRemove, .attachmentSectionRemove")) {
            var removeSection = $(this);
            $('#publisherWarningModal #warningType').text($.i18n._('publisher.section.template.remove.warning'));
            $('#publisherWarningModal').modal('show');
            $('#publisherWarningButton').off().on('click', function (e) {
                e.preventDefault();
                removeSection.closest("tr").remove();
                $('#publisherWarningModal').modal('hide');
            });
        } else
            $(this).closest("tr").remove();
    });

    $(document).on("click", ".addParameter", function () {
        var clone = $($(".parameterTemplateRow")[0]).clone();
        $(".parameterTable").append(clone);
        clone.removeClass("parameterTemplateRow");
        clone.show();
        updateParameterValueAutoComplete();
    });
    $(document).on("click", ".publisherSectionDown,.publisherSectionUp", function () {
        var moveSection = $(this);
        $('#publisherWarningModal #warningType').text($.i18n._('publisher.section.template.move.warning'));
        $('#publisherWarningModal').modal('show');
        $('#publisherWarningButton').off().on('click', function (e) {
            e.preventDefault();
            var row = moveSection.parents("tr:first");
            if (moveSection.is(".publisherSectionUp")) {
                if (row.prev().is(':visible'))
                    row.insertBefore(row.prev());
            } else {
                row.insertAfter(row.next());
            }
            $('#publisherWarningModal').modal('hide');
        });
    });
    $(document).on("click", ".testRest", function () {
        var $tr = $(this).closest("tr");
        var script = $tr.find("[name=attachmentScript]").val();
        var fileType = $tr.find("[name=attachmentFileType]").val();
        var name = $tr.find("[name=attachmentName]").val();
        $.ajax({
            url: testScriptUrl,
            data: {script: script, fileType: fileType, name: name},
            type: 'POST',
            dataType: 'json'
        })
            .fail(function (err) {
                alert("Something went wrong!");
            })
            .done(function (data) {
                $("#testLog").val(data)
                $("#testRequestModal").modal("show")
            });
    });

    updateParameterValueAutoComplete();

    $("[data-evt-sbt]").on('submit', function() {
        const eventData = JSON.parse($(this).attr("data-evt-sbt"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'validateAdditionalSourceForm') {
            return validateAdditionalSourceForm();
        }
    });
});

function updateParameterValueAutoComplete() {
    var paramValueAutoComplete = ["$report.section1.table", "$report.section1.data", "$report.section1.chart", "$report.section1.cell[row;column]", "$report.source[].content", "$report.source[].data", "$report.source[].img", "$report.source[].img[pageN-pageM]", "$report.source[].cell[row;column]",
        "$previous.section1.table", "$previous.section1.data", "$previous.section1.chart", "$previous.section1.cell[row;column]", "$report.section1.range[row;column][row;column]", "$previous.source[].content", "$previous.source[].data", "$report.source[].range[row;column][row;column]",
        "$previous.doc.text[\"begin str\";\"end str\"]", "$previous.doc.text(\"begin str\";\"end str\")", "$previous.doc.paragraph[\"begin str\";\"end str\"]", "$previous.doc.paragraph(\"begin str\";\"end str\")",
        "$previous.doc.section[\"begin str\";\"end str\")"];

    $("[name=parameterValue], .pendingInput").each(function () {
        $(this).autocomplete({
            source: paramValueAutoComplete, minLength: 0, open: function () {
                $("ul.ui-menu").width($(this).innerWidth())
            }
        });
    });
}

function validateTemplateForm() {
    var values = []
    result = true
    $(".templateTable .sourceErrorBorder").removeClass("sourceErrorBorder");
    $(".templateError").hide();
    $(".templateTable tbody .templateTableRow").each(function () {

        var input = $(this).find("input[name=publisherSectionName]");
        if (input.val() == "") {
            input.addClass("sourceErrorBorder");
            result = false;

        } else {
            if (values.indexOf(input.val()) > -1) {
                input.addClass("sourceErrorBorder");
                result = false;
            }
            values.push(input.val())
        }

        if (($(this).find(".publisherTemplateSelect").length > 0) && ($(this).find(".publisherTemplateSelect").select2("val") == "")) {
            $(this).find(".publisherTemplateSelect").addClass("sourceErrorBorder");
            result = false;
        }
        if (($(this).find(".fileName").length > 0) && ($(this).find(".fileName").val() == "")) {
            $(this).find(".fileName").addClass("sourceErrorBorder");
            result = false;
        }

    })
    if (!result) $(".templateError").show();
    return result
}

function validateAdditionalSourceForm() {
    var values = []
    result = true
    $(".attachmentTable .sourceErrorBorder").removeClass("sourceErrorBorder");
    $(".additionalSourceError").hide();
    $(".attachmentTable tr:not(.rowTemplate)").each(function () {

        var input = $(this).find("input[name=attachmentName]");
        if (input.val() == "") {
            input.addClass("sourceErrorBorder");
            result = false;

        } else {
            if (values.indexOf(input.val()) > -1) {
                input.addClass("sourceErrorBorder");
                result = false;
            }
            values.push(input.val())
        }
        var type = $(this).find("[name=attachmentFileSource]").val();
        if ((type == "FILE") && ($(this).find(".fileName").val() == "")) {
            $(this).find(".fileName").addClass("sourceErrorBorder");
            result = false;
        } else if ((type == "ONEDRIVE") && ($(this).find("input[name=oneDriveFolderName]").val() == "")) {
            $(this).find("input[name=oneDriveFolderName]").addClass("sourceErrorBorder");
            result = false;
        } else if ((type == "SERVICE") && ($(this).find("[name=attachmentScript]").val() == "")) {
            $(this).find("[name=attachmentScript]").addClass("sourceErrorBorder");
            result = false;
        } else if (((type == "FOLDER") || (type == "HTTP") || (type == "HTTPS")) && ($(this).find("input[name=attachmentPath]").val() == "")) {
            $(this).find("input[name=attachmentPath]").addClass("sourceErrorBorder");
            result = false;
        }
    })
    if (!result) $(".additionalSourceError").show();
    return result
}