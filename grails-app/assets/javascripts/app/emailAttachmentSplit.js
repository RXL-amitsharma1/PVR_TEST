$(function () {

    $(".emailAttachmentCheckbox").on("change", function () {
        emailAttachmentCheckboxChange();
    });

    function emailAttachmentCheckboxChange() {
        if ($("#emailAttachmentCheckboxAll").is(":checked")) {
            $("#emailAttachmentAll").show();
            $("#emailAttachmentSplit").hide();
            additionalAttachments = [];
            $("#emailAttachmentsConfig").val("");
            $(".attachmentTableRow").remove();
        } else {
            $("#emailAttachmentAll").hide();
            $("#emailAttachmentSplit").show();
            $("input[name=deliveryOption\\.attachmentFormats]").attr("checked", false);
        }
    }

    emailAttachmentCheckboxChange();
    $("#emailAttachmentSplitFormat").select2({allowClear: true});
    $(".showAttachmentModalButton").on("click", function () {
        $("#addAttachmentModal").modal("show");
        var content = "";
        var sections = $('.templateQuery-div:visible').find("select.selectTemplate");
        for (var i = 0; i < sections.length; i++) {
            var data = $(sections[i]).select2("data");
            var sectionsData =  data[0].configureAttachments ? data[0].configureAttachments : data[0].text;
            content += " <div class=\"checkbox checkbox-primary\">\n" +
                "<input type='checkbox' class='attachmentCheckbox' id='attachmentCheckbox" + i + "' value='" + i + "'>" +
                "<label for='attachmentCheckbox" + i + "'>" +
                $.i18n._("app.configuration.section") + " " + (i + 1) + " " + sectionsData + "</label></div> ";
        }
        $("#attachmentModalContent").html(content);
        $(".attachmentFormatModal").prop("checked", false);
    });


    $(document).on("click", ".removeAttachmentTableRow", function () {
        var id = $(this).attr("data-id");
        additionalAttachments = _.filter(additionalAttachments, function (a) {
            return a.id != id
        });
        formAttachmentTable();
        $("#emailAttachmentsConfig").val(JSON.stringify(additionalAttachments));
    });
    $("#saveAttachmentButton").on("click", function () {
        var id = 0;
        if (additionalAttachments.length > 0)
            id = _.max(additionalAttachments, function (a) {
                return a.id;
            }).id + 1;
        var sections = [];
        $("#attachmentModalContent").find(".attachmentCheckbox").each(function () {
            if ($(this).is(":checked")) sections.push($(this).val());
        });
        var formats = [];
        $("#addAttachmentModal").find(".attachmentFormat").each(function () {
            if ($(this).is(":checked")) formats.push($(this).val());
        });
        if ((sections.length > 0) && (formats.length > 0))
            additionalAttachments.push({id: id, sections: sections, formats: formats});
        else {
            $(".additionalAttachmentsDialogError").show();
            return;
        }
        $("#emailAttachmentsConfig").val(JSON.stringify(additionalAttachments));
        formAttachmentTable();
        $("#addAttachmentModal").modal("hide");
    });
    $("#cancelAttachmentButton").on("click", function () {
        $("#attachmentModalDialogError").hide();
    });


});
var additionalAttachments = null;

function formAttachmentTable(deletedTemplateQuery) {
    if (additionalAttachments == null) {
        var att = $("#emailAttachmentsConfig").val();
        if (att != null && att != '')
            additionalAttachments = JSON.parse(att);
        else
            additionalAttachments = [];
    }
    var sections = $('.templateQuery-div:visible').find("select.selectTemplate");
    var table = $(".emailAttachmentsTable");
    $(".attachmentTableRow").remove();
    var newAdditionalAttachments = [];
    if (sections.length < 2) {
        $("#emailAttachmentCheckboxAll").trigger('click');
        $(".emailAttachmentCheckbox").attr("disabled", true);
        return;
    } else {
        $(".emailAttachmentCheckbox").attr("disabled", false);
    }
    var changed = false;
    for (var i = 0; i < additionalAttachments.length; i++) {
        var sec = [];
        for (var j = 0; j < additionalAttachments[i].sections.length; j++) {
            if (typeof(deletedTemplateQuery) == "undefined")
                sec.push(additionalAttachments[i].sections[j])
            else if(parseInt(additionalAttachments[i].sections[j]) != parseInt(deletedTemplateQuery))
                sec.push(additionalAttachments[i].sections[j] < parseInt(deletedTemplateQuery) ? additionalAttachments[i].sections[j] :additionalAttachments[i].sections[j]-1);
            else
                changed = true;
        }
        if (sec.length > 0) {
            newAdditionalAttachments.push({id: additionalAttachments[i].id, sections: sec, formats: additionalAttachments[i].formats})
        }
    }
    additionalAttachments = newAdditionalAttachments;
    if(!$.isEmptyObject(additionalAttachments)){
        $("#emailAttachmentsConfig").val(JSON.stringify(additionalAttachments));
    }
    if (changed) $(".additionalAttachmentsWarning").show();
    for (var i = 0; i < additionalAttachments.length; i++) {
        var sectionLabel = "";
        for (var j = 0; j < additionalAttachments[i].sections.length; j++) {
            var data = $(sections[additionalAttachments[i].sections[j]]).select2("data");
            //Handling null values for data when select2 is not yet initialized for the template
            if(data != null){
                var sectionsData =  data[0].configureAttachments ? data[0].configureAttachments : data[0].text;
                sectionLabel += $.i18n._("app.configuration.section") + " " + (1 + parseInt(additionalAttachments[i].sections[j])) + ": " + sectionsData + "<br>";
            }
        }
        var row = "<tr class='attachmentTableRow'><td><span class='table-remove md md-close pv-cross removeAttachmentTableRow' data-id='" + additionalAttachments[i].id + "'></span></td>\n" +
            "<td>" + additionalAttachments[i].formats.join(",") + "</td><td>" + sectionLabel + "</td></tr>";
        table.append(row);
    }
}
