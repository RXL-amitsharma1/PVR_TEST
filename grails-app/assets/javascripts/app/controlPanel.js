$(function () {

    $('#inbound-initial-conf-form').on('submit', function (event) {
        if (!updateCheckboxValue()) {
            event.preventDefault();
        }
    });

    function updateCheckboxValue() {
        var checkbox = document.getElementById('caseDateLogic');
        var hiddenInput = document.getElementById('caseDateLogicValue');
        hiddenInput.value = checkbox.checked ? 'true' : 'false';
        return true;
    }

    $('#pvaEntityModal').on('hidden.bs.modal', function (e) {
        $(this)
            .find("input,textarea")
            .val('')
            .end()
            .find("input[type=checkbox]")
            .prop("checked", "")
            .end()
            .find("input[type=checkbox][id='readEntity']")
            .prop("checked", "checked")
            .end();
    });

    $("#mirrorLdapValues,#refreshCache,#encryptReportData,#decryptReportData,#uploadButton").on('click', function () {
        showSpinnerMessage();
    });

    $("#hideVersion").on('click', function () {
        $.ajax({
            url: ($(this).is(':checked') ? saveDefaultUiUrl : removeDefaultUiUrl),
            type: 'get',
            dataType: 'json'
        })
            .fail(function (err) {
                alert($.i18n._('Error') + " : " + err);
            })
            .done(function (data) {
                sessionStorage.removeItem("DataTables_openCaseList_/reports/caseList/index");
                sessionStorage.removeItem("DataTables_removedCaseList_/reports/caseList/index");
                sessionStorage.removeItem("DataTables_caseList_/reports/caseList/index");
                $('#warningModal').modal('show');
            });
    });

    $(document).on('click', '#exportToExcel', function () {
        $("#exportStatusLoading").show();
        $.ajax({
            type: "get",
            url: exportToExcelUrl + "?qced=" + $("#exportQced").is(':checked'),
            dataType: 'json'
        })
            .done(function (resp) {
                var result = resp.data;
                var content = $.i18n._('controlPanel.export.complete.label') + "<br>";
                for (var i = 0; i < result.files.length; i++) {
                    content += "<a href='" + getExportFileUrl + "?fileName=" + result.files[i] + "&part=" + (i + 1) + "'>"
                        + $.i18n._('controlPanel.export.download.link') +
                        (result.files.length > 1 ? ($.i18n._('controlPanel.export.part.label') + i) : "") +
                        "</a><br><br>";
                }

                if (result.errors.length > 0)
                    content += $.i18n._('controlPanel.export.error.label') + "<br><br>";
                for (var i = 0; i < result.errors.length; i++) {
                    content += result.errors[i].type + " " + result.errors[i].id + " " + result.errors[i].name + " " + "<br>" +
                        "<div style='display:none' id='err" + i + "'>" + result.errors[i].error + "</div><a data-id='"+i+"' class='btn-link add-cursor exportToExcelError'>" + $.i18n._('controlPanel.export.show.label') + "</a><br><br>";
                }
                $("#exportStatus").html(content);
                $("#exportStatus").show();
                $("#exportStatusLoading").hide();
            })
            .fail(function (e) {
                $("#exportStatus").html(e.responseText);
                $("#exportStatus").show();
                $("#exportStatusLoading").hide();
            });
    });

    $(document).on('click', '.exportToExcelError', function () {
        $("#err" + $(this).attr("data-id")).toggle();
    });

    $(document).on('click', '#updateMedDra', function () {
        $(".btn").attr('disabled', 'disabled');
        var changeList = validateAndGetAllChanges(1);

        if (changeList)
            $.ajax({
                type: "post",
                url: medDraUpdateUrl,
                data: {"data": JSON.stringify(changeList)},
                dataType: 'json'
            })
                .done(function (result) {
                    $(".btn").removeAttr('disabled');
                    $("#errorDiv").hide();
                    var divContent = "<h3>" + $.i18n._('controlPanel.medDra.success.text') + "</h3><br>";
                    $(".modal-title").text($.i18n._('controlPanel.medDra.success'));
                    divContent += formModal(result);
                    $("#jsondata").val(JSON.stringify(result));
                    $("#usageContent").html(divContent);
                    $('#usageModal').modal('show');
                    var rows = $TABLE.find('table').find("tr");
                    for (var i = 1; i < rows.length; i++) {
                        if (!$(rows[i]).hasClass("hide"))
                            $(rows[i]).detach();
                    }
                    $('.table-add').trigger('click');
                })
                .fail(function (e) {
                    $(".btn").removeAttr('disabled');
                    $("#errorDiv").text(e.responseText);
                    $("#errorDiv").show();
                });
    });

    $(document).on('click', '.checkAllUsages', function () {
        $(".btn").attr('disabled', 'disabled');
        var changeList = validateAndGetAllChanges(0);

        if (changeList)
            $.ajax({
                type: "post",
                url: medDraAllUsageCheckUrl,
                data: {"data": JSON.stringify(changeList)},
                dataType: 'json'
            })
                .done(function (result) {
                    $(".btn").removeAttr('disabled');
                    $("#errorDiv").hide();
                    var divContent = formModal(result);
                    $("#jsondata").val(JSON.stringify(result));
                    $(".modal-title").text($.i18n._('controlPanel.medDra.usage'));
                    $("#usageContent").html(divContent);
                    $('#usageModal').modal('show');
                })
                .fail(function (e) {
                    $(".btn").removeAttr('disabled');
                    $("#errorDiv").text(e.responseText);
                    $("#errorDiv").show();
                });
    });

    $(document).on('click', '.toExcel', function () {
        createExcelExportData();
        var DefaultTable = document.getElementById('export');
        var workbook = XLSX.utils.book_new();
        var worksheet = XLSX.utils.table_to_sheet(DefaultTable);
        // add the modified worksheet to the workbook
        XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1');
        // then generate the Excel file in binary format
        var excelFile = XLSX.write(workbook, {bookType: 'xlsx', bookSST: false, type: 'binary'});
        var blob = new Blob([stringToArrayBuffer(excelFile)], {type: 'application/octet-stream'});
        var link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'usage.xlsx';
        link.trigger('click');
    });

    function stringToArrayBuffer(excelFile) {
        if (excelFile && typeof excelFile === 'string') {
            var buffer = new ArrayBuffer(excelFile.length);
            var view = new Uint8Array(buffer);
            for (var i = 0; i < excelFile.length; ++i) view[i] = excelFile.charCodeAt(i) & 0xFF;
            return buffer;
        } else {
            return new ArrayBuffer(0);
        }
    }


    $(document).on('click', '.toPdf', function () {
        createPdfExportData();
        var element = $('#export').html()
        $.ajax({
            url: downloadPdfUrl,
            type: "POST",
            data: {"data": element},
            dataType: 'json',
            xhrFields: {
                responseType: 'blob' // Treat the response as a binary blob
            }
        })
            .done(function (data, status, xhr) {
                // Create a blob URL and initiate download
                var blob = new Blob([data], {type: 'application/pdf'});
                var a = document.createElement('a');
                a.href = URL.createObjectURL(blob);
                a.download = 'usage.pdf';
                a.trigger('click');
            })
            .fail(function (err) {
            });
    });

    function createPdfExportData() {
        var result = JSON.parse($("#jsondata").val());
        var content = "<div id='export'>";
        content += appendPdfExportRows($.i18n._('controlPanel.medDra.query'), result.query, queryLink);
        content += appendPdfExportRows($.i18n._('controlPanel.medDra.template'), result.template, templateLink);
        content += appendPdfExportRows($.i18n._('controlPanel.medDra.adhoc'), result.adhoc, adhocLink);
        content += appendPdfExportRows($.i18n._('controlPanel.medDra.aggregate'), result.aggregate, aggregateLink);
        content += appendPdfExportRows($.i18n._('controlPanel.medDra.caseSeries'), result.caseSeries, caseSeriesLink);
        content += "</div>";
        $("#tableWrap").html(content);
    }

    function createExcelExportData() {
        var result = JSON.parse($("#jsondata").val());
        var content = "<table id='export'>";
        content += appendExcelExportRows($.i18n._('controlPanel.medDra.query'), result.query, queryLink);
        content += appendExcelExportRows($.i18n._('controlPanel.medDra.template'), result.template, templateLink);
        content += appendExcelExportRows($.i18n._('controlPanel.medDra.adhoc'), result.adhoc, adhocLink);
        content += appendExcelExportRows($.i18n._('controlPanel.medDra.aggregate'), result.aggregate, aggregateLink);
        content += appendExcelExportRows($.i18n._('controlPanel.medDra.caseSeries'), result.caseSeries, caseSeriesLink);
        content += "</table>";
        $("#tableWrap").html(content);
    }

    function appendExcelExportRows(type, data, link) {
        var content = "";
        if (data && data.length > 0) {
            for (var i = 0; i < data.length; i++) {
                content += '<tr><td>' + type + '</td><td>' + data[i].id + '</td><td>' + data[i].name + '</td><td>' + link + data[i].id + '</td></tr>';
            }
        }
        return content;
    }

    function appendPdfExportRows(type, data, link) {
        var content = "<h3>" + type + "</h3>";
        if (data && data.length > 0) {
            for (var i = 0; i < data.length; i++) {
                content += '<p>' + type + ':' + data[i].name + ' (id:' + data[i].id + ') ' + link + data[i].id + '</p>';
            }
        }
        return content;
    }

    $(document).on('click', '.checkUsage', function () {
        $(".btn").attr('disabled', 'disabled');
        var tds = $(this).parent().parent().find("td");
        if ($(tds[2]).text() != '?' && $(tds[3]).text() != '?')
            $.ajax({
                type: "post",
                url: medDraUsageCheckUrl + "?level=" + $(tds[1]).find("select").find(':selected').attr('data-id') + "&old=" + $(tds[2]).text() + "&new=" + $(tds[3]).text(),
                dataType: 'json'
            })
                .done(function (result) {
                    $(".btn").removeAttr('disabled');
                    $("#errorDiv").hide();
                    var divContent = formModal(result);
                    $("#jsondata").val(JSON.stringify(result));
                    $(".modal-title").text($.i18n._('controlPanel.medDra.usage'));
                    $("#usageContent").html(divContent);
                    $('#usageModal').modal('show');
                })
                .fail(function (e) {
                    $(".btn").removeAttr('disabled');
                    $("#errorDiv").text(e.responseText);
                    $("#errorDiv").show();
                });
    });

    function validateAndGetAllChanges(fromfunction) {
        var rows = $TABLE.find('table').find("tr");
        var changeList = [];

        for (var i = 1; i < rows.length; i++) {
            if (!$(rows[i]).hasClass("hide")) {
                var tds = $(rows[i]).find("td");
                var elem = {
                    "level": $(tds[1]).find("select").find(':selected').attr('data-id'),
                    "from": $(tds[2]).text(),
                    "to": $(tds[3]).text()
                };
                changeList.push(elem);
                if (elem.from == "" || (elem.to == "" && fromfunction) || elem.from.indexOf("?") > -1 || elem.to.indexOf("?") > -1) {   //distinguish between updateMedDra and bulk checkUsage button.
                    $(rows[i]).css("border", "red solid 2px");
                    setTimeout(function () {
                        $(rows[i]).css("border", "none");
                    }, 5000);
                    $(".btn").removeAttr('disabled');
                    $("#errorDiv").hide();
                    return null
                }
            }
        }
        return changeList
    }

    function formModal(result) {
        var divContent = appendBlock($.i18n._('controlPanel.medDra.query'), result.query, queryLink);
        divContent += appendBlock($.i18n._('controlPanel.medDra.template'), result.template, templateLink);
        divContent += appendBlock($.i18n._('controlPanel.medDra.adhoc'), result.adhoc, adhocLink);
        divContent += appendBlock($.i18n._('controlPanel.medDra.aggregate'), result.aggregate, aggregateLink);
        divContent += appendBlock($.i18n._('controlPanel.medDra.caseSeries'), result.caseSeries, caseSeriesLink);
        return divContent;
    }

    function appendBlock(title, data, link) {
        var content = '<div class="row">\
            <div class="col-md-12">\
                <h3 class="sectionHeader">' + title + '</h3>\
                <div class="twoColumns">';
        if (data && data.length > 0) {
            for (var i = 0; i < data.length; i++) {
                content += '<div class="forceLineWrap">\
                  <a target="_blank" href="' + link + data[i].id + '">' + data[i].name + '</a></div>';
            }
        } else {
            content += $.i18n._('controlPanel.medDra.nothing');
        }
        content += "</div></div></div>";
        return content;
    }


    var $TABLE = $('#table');

    $('.table-add').on('click', function () {
        var $clone = $TABLE.find('tr.hide').clone(true).removeClass('hide table-line');
        $TABLE.find('table').append($clone);
    });

    $('.table-remove').on('click', function () {
        $(this).parents('tr').detach();
    });

    $('#file_input').on('change', function (evt, numFiles, label) {
        $(".btn").attr('disabled', 'disabled');
        $("#file_name").val($('#file_input').get(0).files[0].name);
        var jForm = new FormData();
        jForm.append("file", $('#file_input').get(0).files[0]);
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
                $(".btn").removeAttr('disabled');
                $("#errorDiv").hide();
                if (data.success) {
                    showImportedValues(data.uploadedValues);
                } else {
                    $("#errorDiv").text(data.message);
                    $("#errorDiv").show();
                    $('#file_input').val('');
                    $("#file_name").val('');
                }
            })
            .fail(function (e) {
                $(".btn").removeAttr('disabled');
                $("#errorDiv").text(e.responseText);
                $("#errorDiv").show();
            });
    });

    function showImportedValues(data) {
        var rows = $TABLE.find('table').find("tr");
        for (var i = 1; i < rows.length; i++) {
            if (!$(rows[i]).hasClass("hide"))
                $(rows[i]).detach();
        }
        for (var i = 0; i < data.length; i++) {
            var $clone = $TABLE.find('tr.hide').clone(true).removeClass('hide table-line');
            $TABLE.find('table').append($clone);
            var tds = $clone.find("td");
            $(tds[1]).find("select").val(data[i].level);
            $(tds[2]).text(data[i].old);
            $(tds[3]).text(data[i].new);
        }

    }

    //-----odataConfiguration
    var $PVATABLE = $('#pvatable');
    var olddEntity;
    $("#pvaTableName").select2();
    var d = $('#settings').val();
    var pvaConfigurationJSON = JSON.parse(d ? d : "{}");

    renderPvaTable();

    function renderPvaTable() {
        $PVATABLE.find("tr").each(function () {
            if (!($(this).hasClass("hide") || $(this).hasClass("pvaheader"))) $(this).detach();
        });
        for (var entity in pvaConfigurationJSON) {
            createPvaTableRow(entity, pvaConfigurationJSON[entity])
        }
    }

    function createPvaTableRow(entityName, entityDescription) {
        var clone = $PVATABLE.find('tr.hide').clone(true).removeClass('hide table-line');
        var td = clone.find("td");
        $(td[1]).html(entityName);
        $(td[2]).html(entityDescription.tableName);
        $(td[3]).html(entityDescription.description);
        $PVATABLE.find('table').append(clone);
    }


    $('.pvatable-add').on('click', function () {
        olddEntity = null;
        $("#pvaTableName").select2();
        $("#pvaEntityName").val("");
        $("#pvaDescription").val("");
        $("#limitQuery").val("");
        $("#updateEntity").prop("checked", false);
        $("#createEntity").prop("checked", false);
        $("#deleteEntity").prop("checked", false);
        $("#pvaEntityModal").modal("show");
    });

    $('.editPvaTable').on('click', function () {
        var entityName = $($(this).parents('tr').find("td")[1]).html();
        olddEntity = entityName;
        var entity = pvaConfigurationJSON[entityName];
        $("#pvaTableName").val(entity.tableName).trigger("change");
        $("#pvaEntityName").val(entityName);
        $("#pvaDescription").val(entity.description);
        $("#limitQuery").val(entity.limitQuery);
        if (entity["update"] === true)
            $("#updateEntity").prop("checked", true);
        else
            $("#updateEntity").prop("checked", false);
        if (entity["create"] === true)
            $("#createEntity").prop("checked", true);
        else
            $("#createEntity").prop("checked", false);
        if (entity["delete"] === true)
            $("#deleteEntity").prop("checked", true);
        else
            $("#deleteEntity").prop("checked", false);
        var jsonString = JSON.stringify(entity.fields);
        jsonString = jsonString.substring(1, jsonString.length - 1);
        jsonString = jsonString.replace(/,/g, ",\n");
        $("#fields").val(jsonString);
        $("#pvaEntityModal").modal("show");
    });

    $('.pvatable-remove').on('click', function () {
        var entity = $($(this).parents('tr').find("td")[1]).html();
        delete pvaConfigurationJSON[entity];
        $(this).parents('tr').detach();
        $("#settings").val(JSON.stringify(pvaConfigurationJSON));
    });

    $('.updateEntity').on('click', function () {
        var tableName = $("#pvaTableName").val();
        if (tableName === "") {
            showAlert($.i18n._("app.odataConfig.empty.table"));
            return
        }
        var entityName = $("#pvaEntityName").val();
        if (entityName === "") {
            showAlert($.i18n._("app.odataConfig.empty.fieldName"));
            return
        }
        var description = $("#pvaDescription").val();
        var limitQuery = $("#limitQuery").val();
        var fields = $("#fields").val();
        if (fields === "") {
            showAlert($.i18n._("app.odataConfig.empty.field"));
            return
        }
        var hasId = false;
        try {
            fields = JSON.parse("{" + fields + "}");
            for (var t in fields) {
                if (t == "ID") hasId = true;
                var row = fields[t];
                if (!_.isString(row)) {
                    throw Exception();
                }
            }
        } catch (e) {
            showAlert($.i18n._('app.odataConfig.wrong.field'));
            return
        }
        if (olddEntity != entityName) {
            delete pvaConfigurationJSON[olddEntity];
        }
        pvaConfigurationJSON[entityName] = {
            "tableName": tableName,
            "description": description,
            "limitQuery": limitQuery,
            "fields": fields,
            "update": $("#updateEntity").is(":checked"),
            "create": $("#createEntity").is(":checked"),
            "delete": $("#deleteEntity").is(":checked")
        };
        if ((pvaConfigurationJSON[entityName].update || pvaConfigurationJSON[entityName].update) && !hasId) {
            showAlert($.i18n._("app.odataConfig.noID"));
            return
        }
        renderPvaTable();
        olddEntity == null;
        $("#pvaEntityModal").modal("hide");
        $("#settings").val(JSON.stringify(pvaConfigurationJSON));
    });

    $('#getDsTableFields').on('click', function () {
        $("#getDsTableFields").attr('disabled', 'disabled');
        $.ajax({
            url: getDsTableFields + "?dsName=" + $("#dsName").val() + "&tableName=" + $("#pvaTableName").val(),
            dataType: 'json'
        })
            .done(function (data) {
                $("#getDsTableFields").removeAttr('disabled');
                var jsonString = JSON.stringify(data.fields);
                jsonString = jsonString.substring(1, jsonString.length - 1);
                jsonString = jsonString.replace(/,/g, ",\n");
                $("#fields").val(jsonString)
                $("#pvaEntityName").val(data.entity)
            })
            .fail(function (e) {
                $(".btn").removeAttr('disabled');
                $("#errorDiv").text(e.responseText);
                $("#errorDiv").show();
            });
    });


    function showAlert(message) {
        alert(message);
    }

    $(document).on("click", "#saveDmsSettings", function () {
        $.ajax({
            url: saveDmsSettingsUrl,
            type: 'post',
            data: {dmsSettings: $("#dmsSettings").val()},
            dataType: 'json'
        })
            .fail(function (err) {
                var message = (err.responseJSON.message ? err.responseJSON.message : "");
                if (message != undefined && message != "")
                    $("#errorDmsDiv").html(
                        '<div class="alert alert-danger alert-dismissable">' +
                        '<button type="button" class="close" ' +
                        'data-dismiss="alert" aria-hidden="true">' +
                        '&times;' +
                        '</button>' +
                        message +
                        '</div>'
                    ).show();
            })
            .done(function (data) {
                $("#errorDmsDiv").html(
                    '<div class="alert alert-success alert-dismissable">' +
                    '<button type="button" class="close" ' +
                    'data-dismiss="alert" aria-hidden="true">' +
                    '&times;' +
                    '</button>' +
                    data.data +
                    '</div>'
                ).show();
            });
    });

    $(document).on("click", "#testDmsSettings", function () {
        $.ajax({
            url: testDmsSettingsUrl,
            type: 'get',
            dataType: 'json'
        })
            .fail(function (err) {
                errorNotification((err.responseJSON.message ? err.responseJSON.message : ""));
            })
            .done(function (data) {
                alert(data.message);
            });
    });

    $(document).on("keyup", ".dmsSettings", function () {
        $("#saveDmsSettings").prop('disabled', false);
        $("#testDmsSettings").prop('disabled', true);
    });


    function uploadJSONFile(file_name, file_input) {
        $(".btn").attr('disabled', 'disabled');
        $(file_name).val($(file_input).get(0).files[0].name);
        var jForm = new FormData();
        jForm.append("file", $(file_input).get(0).files[0]);
        return jForm;
    }

    function importJSON(url, jForm, file_input) {
        $.ajax({
            url: url,
            type: "POST",
            data: jForm,
            contentType: false,
            cache: false,
            processData: false,
            dataType: 'json'
        })
            .done(function (data) {
                $(".btn").removeAttr('disabled');
                if (data.data.success) {
                    messageOnJSONUpload("#successDivJsonUpload", data.data.success, " alert-success")
                    if (!data.data.failure && $("#errorDivJsonUpload").is(":visible")) {
                        $("#errorDivJsonUpload").hide()
                    }
                }
                if (data.data.failure) {
                    messageOnJSONUpload("#errorDivJsonUpload", data.data.failure, " alert-danger")
                    if (!data.data.success && $("#successDivJsonUpload").is(":visible")) {
                        $("#successDivJsonUpload").hide()
                    }
                }
            })
            .fail(function (err) {
                $(".btn").removeAttr('disabled');
                messageOnJSONUpload("#errorDivJsonUpload", err.responseJSON.message, " alert-danger")
            })
            .always(function () {
                $(file_input).val("");
            })
    }

    $("body").on("change", "#queries_json_file_input", function () {
        var file_input = "#queries_json_file_input";
        var jForm = uploadJSONFile("#queries_json", file_input);
        importJSON(importQueriesJson, jForm, file_input);
    });

    $("body").on("change", "#templates_json_file_input", function () {
        var file_input = "#templates_json_file_input";
        var jForm = uploadJSONFile("#templates_json", file_input);
        importJSON(importTemplatesJson, jForm, file_input);
    });
    $("body").on("change", "#configurations_json_file_input", function () {
        var file_input = "#configurations_json_file_input";
        var jForm = uploadJSONFile("#configurations_json", file_input);
        importJSON(importConfigurationsJson, jForm, file_input);
    });
    $("body").on("change", "#dashboards_json_file_input", function () {
        var file_input = "#dashboards_json_file_input";
        var jForm = uploadJSONFile("#dashboards_json", file_input);
        importJSON(importDashboardsJson, jForm, file_input);
    });

    var asOfDate = null;
    if ($("#startDate").val()) {
        asOfDate = $("#startDate").val();
        asOfDate = asOfDate ? moment(asOfDate).format(DEFAULT_DATE_DISPLAY_FORMAT) : ''
    }

    var today = new Date();
    var tomorrow = new Date();
    tomorrow.setDate(today.getDate() + 1);

    $('#startDateDiv').datepicker({
        allowPastDates: true,
        date: asOfDate,
        restricted: [{
            from: tomorrow,
            to: Infinity
        }],
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    var select = $("#reportField");
    $("#reportField").select2();
    getSelect2TreeView(select);


    $('#config_file_input').on('change', function (evt, numFiles, label) {
        $("#config_file_name").val($('#config_file_input').get(0).files[0].name);
    });
    $('#config_first_file').on('change', function (evt, numFiles, label) {
        $("#config1_file_name").val($('#config_first_file').get(0).files[0].name);
    });
    $('#config_second_file').on('change', function (evt, numFiles, label) {
        $("#config2_file_name").val($('#config_second_file').get(0).files[0].name);
    });


    $(document).on("click", "#importTechnicalConfig", function () {

        var form = new FormData($("#importConfigForm")[0]);
        $.ajax({
            url: pvadminURL + "/config/import",
            method: "POST",
            dataType: 'json',
            data: form,
            processData: false,
            contentType: false
        })
            .done(function (result) {
                if (result.status == 'true') {
                    $.Notification.notify('success', 'top-right', "Success", result.message, {autoHideDelay: 10000});
                    $("#config_file_input").val('');
                    $("#config_file_name").val('');

                } else {
                    $.Notification.notify('error', 'top-right', "Error", result.message, {autoHideDelay: 2000});

                }
            })
            .fail(function (er) {
                }
            );

    });
    $('[name=excelFile]').on('change', function () {
        $("#excel-filename").val($.map($('[name=excelFile]')[0].files, function (val) {
            return val.name;
        }).join(";"));
    });
});