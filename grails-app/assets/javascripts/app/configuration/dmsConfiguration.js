var DEFAULT_DMS_OPTIONS = {
    PAGE_ORIENTATION: 'PORTRAIT',
    PAPER_SIZE: 'LETTER',
    SENSITIVITY_LABEL: 'SENSITIVE',
    SHOW_PAGE_NUMBERING: true,
    EXCLUDE_CRITERIA_SHEET: false,
    EXCLUDE_APPENDIX: false,
    EXCLUDE_COMMENTS: false,
    EXCLUDE_LEGEND: false,
    SHOW_COMPANY_LOGO: true,
    NO_DOCUMENTATION_ON_NO_DATA: false

};
$(function () {
    dmsConfig();
    $('#sendToDmsModal').on('show.bs.modal', function (e) {
        var executedConfigId = $(e.relatedTarget).data('id');
        $('#executedConfigId').val(executedConfigId);

        $.ajax({
            url: addDmsConfiguration,
            data: {id: executedConfigId},
            dataType: 'json'
        })
            .done(function (result) {
                unbindEvents();
                $("#dmsConfigContainer").html(result);
                dmsConfig();
            });


    })

    $("[data-evt-clk]").on('click', function(e) {
        e.preventDefault();
        const eventData = JSON.parse($(this).attr("data-evt-clk"));
        const methodName = eventData.method;
        const params = eventData.params;
        if (methodName == "clearSubfolders") {
            var id = $(this).attr("data-id");
            var folder = $(this).attr("data-folder");
            clearSubfolders(id, folder);
        }else if(methodName == "getFolders") {
            var id = $(this).attr("data-id");
            var folder = $(this).attr("data-folder");
            var subfolderPath = folder + $(this).attr("data-subfolders");
            getFolders(subfolderPath, id);
        }
    });
});

function unbindEvents() {
    var dmsConfiguration = $('#dmsConfiguration');
    $(dmsConfiguration).find('#resetDmsConfiguration').off();
    $(dmsConfiguration).find('#cancelDmsConfiguration').off();
    $('.showDmsConfigurationDlg').off();
}

function dmsConfig() {
    var dmsConfiguration = $('#dmsConfiguration');
    var noDocumentOnNoData = $("input[name=dmsConfiguration\\.noDocumentOnNoData]");
    var folder = $("input[name=dmsConfiguration\\.folder]");
    var name = $("input[name=dmsConfiguration\\.name]");
    var description = $("textarea[name=dmsConfiguration\\.description]");
    var tag = $("input[name=dmsConfiguration\\.tag]");
    var pageOrientation = $("select[name=dmsConfiguration\\.pageOrientation]");
    var paperSize = $("select[name=dmsConfiguration\\.paperSize]");
    var sensitivityLabel = $("select[name=dmsConfiguration\\.sensitivityLabel]");
    var showPageNumbering = $("input[name=dmsConfiguration\\.showPageNumbering]");
    var excludeCriteriaSheet = $("input[name=dmsConfiguration\\.excludeCriteriaSheet]");
    var excludeAppendix = $("input[name=dmsConfiguration\\.excludeAppendix]");
    var excludeComments = $("input[name=dmsConfiguration\\.excludeComments]");
    var excludeLegend = $("input[name=emailConfiguration\\.excludeLegend]");
    var showCompanyLogo = $("input[name=dmsConfiguration\\.showCompanyLogo]");

    var noDocumentOnNoDataValue = $("input[name=noDocumentOnNoDataValue]");
    var folderValue = $("input[name=folderValue]");
    var nameValue = $("input[name=nameValue]");
    var descriptionValue = $("input[name=descriptionValue]");
    var tagValue = $("input[name=tagValue]");
    var pageOrientationValue = $("input[name=pageOrientationValue]");
    var paperSizeValue = $("input[name=paperSizeValue]");
    var sensitivityLabelValue = $("input[name=sensitivityLabelValue]");
    var showPageNumberingValue = $("input[name=showPageNumberingValue]");
    var excludeCriteriaSheetValue = $("input[name=excludeCriteriaSheetValue]");
    var excludeAppendixValue = $("input[name=excludeAppendixValue]");
    var excludeCommentsValue = $("input[name=excludeCommentsValue]");
    var excludeLegendValue = $("input[name=excludeLegendValue]");
    var showCompanyLogoValue = $("input[name=showCompanyLogoValue]");

    $('#cancelDmsConfiguration').on('click', function (e) {

        folder.val(folderValue.val());
        initFolders();
        name.val(nameValue.val());
        description.val(descriptionValue.val());
        tag.val(tagValue.val());
        pageOrientation.val(pageOrientationValue.val());
        paperSize.val(paperSizeValue.val());
        sensitivityLabel.val(sensitivityLabelValue.val());

        noDocumentOnNoData.prop('checked', noDocumentOnNoDataValue.val() == "true");
        showPageNumbering.prop('checked', showPageNumberingValue.val() == "true");
        excludeCriteriaSheet.prop('checked', excludeCriteriaSheetValue.val() == "true");
        excludeAppendix.prop('checked', excludeAppendixValue.val() == "true");
        excludeComments.prop('checked', excludeCommentsValue.val() == "true");
        excludeLegend.prop('checked', excludeLegendValue.val() == "true");
        showCompanyLogo.prop('checked', showCompanyLogoValue.val() == "true");

        $(this).attr('data-dismiss', 'modal');
    });


    $('#saveDmsConfiguration').on('click', function (e) {
        folderValue.val(folder.val());
        nameValue.val(name.val());
        descriptionValue.val(description.val());
        tagValue.val(tag.val());
        pageOrientationValue.val(pageOrientation.val());
        paperSizeValue.val(paperSize.val());
        sensitivityLabelValue.val(sensitivityLabel.val());
        $(this).attr('data-dismiss', 'modal');
    });


    $('.showDmsConfigurationDlg').on('click', function (e) {
        checkDmsFolders("").then(success => {
            if (($(".dmsEnabled").length == 0 || $(".dmsEnabled").is(":checked")) && success) {
                dmsConfiguration.modal("show");
                name.prop("placeholder", $("input[name=reportName]").val());
                description.prop("placeholder", $("textarea[name=description]").val());
                var t = $("#tags").val();
                tag.prop("placeholder", t ? t : "");
                initFolders();
            } else {
                $('#dmsErrorModal').modal('show');
            }
        }).catch(error => {
            console.error("An error occurred:", error);
        });
    });

    $(dmsConfiguration).find('#resetDmsConfiguration').on('click', function (e) {
        noDocumentOnNoData.val("");
        folder.val("");
        getFolders("", "0");
        name.val("");
        description.val("");
        tag.val("");
        pageOrientation.val(DEFAULT_DMS_OPTIONS.PAGE_ORIENTATION);
        paperSize.val(DEFAULT_DMS_OPTIONS.PAPER_SIZE);
        sensitivityLabel.val(DEFAULT_DMS_OPTIONS.SENSITIVITY_LABEL);

        showPageNumbering.prop('checked', DEFAULT_DMS_OPTIONS.SHOW_PAGE_NUMBERING);
        excludeCriteriaSheet.prop('checked', DEFAULT_DMS_OPTIONS.EXCLUDE_CRITERIA_SHEET);
        excludeAppendix.prop('checked', DEFAULT_DMS_OPTIONS.EXCLUDE_APPENDIX);
        excludeComments.prop('checked', DEFAULT_DMS_OPTIONS.EXCLUDE_COMMENTS);
        excludeLegend.prop('checked', DEFAULT_DMS_OPTIONS.EXCLUDE_LEGEND);
        showCompanyLogo.prop('checked', DEFAULT_DMS_OPTIONS.SHOW_COMPANY_LOGO);
        noDocumentOnNoData.prop('checked', DEFAULT_DMS_OPTIONS.NO_DOCUMENTATION_ON_NO_DATA);
        initFolders();

    });

}


function initFolders() {
    var f = $("input[name=dmsConfiguration\\.folder]").val();
    if (f) {
        getFolders("", "0", f.split("/"), 0);
    } else {
        getFolders("", "0");
    }
}

function getFolders(folder, place, path, i) {
    var id = $('#executedConfigId').val();
    $.ajax({
        url: dmsFoldersUrl,
        type: 'get',
        data: {folder: folder, id: id},
        dataType: 'json'
    })
        .fail(function (err) {
            console.log(err);
        })
        .done(function (data) {
            var id = drawFolders(folder, data, place);
            $(".dmsDropDown.open").removeClass("open");
            if (path && (i < path.length - 1)) {
                getFolders(folder + "/" + path[i + 1], id, path, i + 1);
            }
        });
}

function checkDmsFolders(folder) {
    var id = $('#executedConfigId').val();
    return new Promise((resolve) => {
        $.ajax({
            url: dmsFoldersUrl,
            type: 'get',
            data: { folder: folder, id: id },
            dataType: 'json'
        })
            .done(function (data) {
                resolve(true); // Success case
            })
            .fail(function (err) {
                resolve(false); // Failure case
            });
    });
}

function drawFolders(folder, subfolders, place) {
    var id = new Date().getTime();
    var folderLabel = (folder ? folder : $.i18n._('app.label.dms.root'));
    if (folderLabel.indexOf("/") > -1) folderLabel = folderLabel.substr(folderLabel.lastIndexOf("/"));
    var content = '<span class="dmsFolder" data-id="'+id+'" data-folder="'+folder+'" data-evt-clk=\'{"method": "clearSubfolders", "params": [\"'+id +'\",\"'+folder+ '\"]}\' >' + folderLabel + '</span>';

    if (subfolders && subfolders.length > 0) {
        content += '<span class="dropdown dmsDropDown">' +
            '<button class="btn btn-default btn-xs nextDmsFolder dropdown-toggle" id="' + id + '" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">' +
            '<span class="glyphicon glyphicon-chevron-right"></span>' +
            '</button>' +
            '<ul class="dropdown-menu" aria-labelledby="' + id + '" style="max-height: 200px;overflow: auto;">';
        for (var i = 0; i < subfolders.length; i++) {
            content += '<li><a href="#" data-id="'+id+'" data-folder="'+folder+'" data-subfolders="'+subfolders[i]+'" data-evt-clk=\'{"method": "getFolders", "params": []}\' data-place="' + id + '">' + subfolders[i] + '</a></li>'
        }
        content += '</ul></span>';
    }
    content += '<span id="place_' + id + '"></span>';
    $("#place_" + place).html(content);
    $("input[name=dmsConfiguration\\.folder]").val(folder);
    return id;
}

function clearSubfolders(id, folder) {
    $("#place_" + id).html("");
    $("input[name=dmsConfiguration\\.folder]").val(folder);
}

