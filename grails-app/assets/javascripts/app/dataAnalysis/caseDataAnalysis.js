function createFileName() {
    var productSelectionNames = _.map(getProductFamily(), function (x) {
        return x.name
    }).join("_");
    var productGroupSelectionNames = _.map(getProductGroup(), function (x) {
        return x.name.replace(/\(.*?\)/g, '')
    }).join("_");
    var type = $('input[name="type"]:checked').val();
    var configName = ($("#seriesName").length > 0 ? $("#seriesName").val() : $("input[name=reportName]").val());
    var name = configName + "_" + productSelectionNames + "_" + productGroupSelectionNames + "_" + type;
    var fileName = name.replace(new RegExp(" ", 'g'), "-");
    return fileName.replace(/[^a-zA-Z0-9_-]/g, '');
}

function getProductFamily() {
    var productSelection = $("#productSelection").val();
    if (productSelection) {
        var productSelectionJSON = JSON.parse(productSelection);
        for (var prod in productSelectionJSON) {
            if (productSelectionJSON[prod].length != 0) {
                var data = productSelectionJSON[prod];
            }
        }
        return data
    }
    return null
}

function getProductGroup() {
    var productGroupSelection = $("#productGroupSelection").val();
    if (productGroupSelection) {
        var productGroupSelectionJSON = JSON.parse(productGroupSelection);
        if (productGroupSelectionJSON.length != 0) {
            var data = productGroupSelectionJSON;
        }
        return data
    }
    return null
}

function enableSpotfire() {
    var disabled = true;
    if (spotfireCheck == "true") disabled = false;
    if (getProductFamily()) disabled = false;
    if (getProductGroup()) disabled = false;
    if (disabled) {
        $(".spotfireElement").prop('disabled', disabled);
        $("#generateSpotfireCheckbox").prop("checked", false);
        $("#fullFileName").val("");
    } else {
        $("#generateSpotfireCheckbox").prop('disabled', false);
    }
}

function prepareGenerateSpotfireSettings() {
    if ($("#generateSpotfireCheckbox").is(":checked")) {
        var settings = {};
        settings.fullFileName = $("#fullFileName").val();
        settings.type = $('input[name="type"]:checked').val();
        $("#generateSpotfire").val(JSON.stringify(settings));
    } else {
        $("#generateSpotfire").val("");
    }
}

$(function () {
    $(document).on("change", "#productSelection", "#productGroupSelection", function () {
        enableSpotfire();
        if ($("#generateSpotfireCheckbox").is(":checked")) {
            $("#fullFileName").val(createFileName());
            prepareGenerateSpotfireSettings();
        }
    });
    var settings = $("#generateSpotfire").val();
    if (settings && settings.length > 0) {
        var settingsJSON = JSON.parse(settings);
        $("#generateSpotfireCheckbox").prop("checked", true);
        $('input[name="type"][value="' + settingsJSON.type + '"]').prop("checked", true);
        $("#fullFileName").val(settingsJSON.fullFileName);
        $(".spotfireElement").prop('disabled', false);
    } else {
        $(".spotfireElement").prop('disabled', true);
    }
    if ($('input[name="type"]:checked').length == 0)
        $($('input[name="type"]')[0]).prop("checked", true);
    enableSpotfire();

    $(document).on("change", "#seriesName, input[name=type],input[name=reportName]", function () {
        if ($("#generateSpotfireCheckbox").is(":checked")) {
            $("#fullFileName").val(createFileName());
            prepareGenerateSpotfireSettings();
        }
    });
    $(document).on("change", "#generateSpotfireCheckbox", function () {
        if ($("#generateSpotfireCheckbox").is(":checked")) {
            $(".spotfireElement").prop('disabled', false);
            $("#fullFileName").val(createFileName());
        } else {
            $(".spotfireElement").prop('disabled', true);
            $("#generateSpotfireCheckbox").prop('disabled', false);
            $("#fullFileName").val("");
        }
        prepareGenerateSpotfireSettings();
    });


});