var formattedData = [];
var LATE_IDENTIFIER = "late";
var ROOT_CAUSE_IDENTIFIER = "rootCause";
var ROOT_CAUSE_SUB_IDENTIFIER = "rootCauseSub";
var ROOT_CAUSE_CLASS_IDENTIFIER = "rootCauseClass";
var RESP_PARTY_IDENTIFIER = "respParty";
var PVC = 'PVC'
var PVQ = 'PVQ'
var PVC_Inbound = 'PVC_Inbound'
var hidden = true
$(function () {
    var mappingTable;
    var dataKey;
    var mappingUrl;
    var isWarningModalOpen = false;
    initRodSelectionButtons();
    $(document).on("preInit.dt", function () {
        $("#rodTable_wrapper").find(".dt-search input[type='search']").attr("maxlength", 2000);
    });

    var init_mapping_table = function (tableIdentifier) {
        hiddenCheck = $("#hiddenList").is(':checked');
        if ($.fn.DataTable.isDataTable("#rodTable")) {
            $("#rodTable").DataTable().destroy();
        }
        var tableDataUrl;
        var deleteLabel;
        var deleteAction;
        if (tableIdentifier === LATE_IDENTIFIER) {
            tableDataUrl = getLateDataList;
            deleteLabel = 'app.fixed.template.issueType';
            deleteAction = 'deleteLate';
            $(".mappingColumn").html($.i18n._('app.fixed.template.rootCause'));
        } else if (tableIdentifier === ROOT_CAUSE_IDENTIFIER) {
            tableDataUrl = getRootCauseDataList;
            deleteLabel = 'app.fixed.template.rootCause';
            deleteAction = 'deleteRootCause';
            $(".mappingColumn").html($.i18n._('app.fixed.template.responsibleParty'));
        } else if (tableIdentifier === ROOT_CAUSE_SUB_IDENTIFIER) {
            tableDataUrl = getRootCauseSubCategoryList;
            deleteLabel = 'app.fixed.template.rootCauseSub';
            deleteAction = 'deleteRootCauseSub';
        } else if (tableIdentifier === ROOT_CAUSE_CLASS_IDENTIFIER) {
            tableDataUrl = getRootCauseClassList;
            deleteLabel = 'app.fixed.template.rootCauseClass';
            deleteAction = 'deleteRootCauseClass';
        } else {
            tableDataUrl = getRespPartyDataList;
            deleteLabel = 'app.fixed.template.responsibleParty';
            deleteAction = 'deleteResponsibleParty';
        }
        mappingTable = $("#rodTable").DataTable({
            "layout": {
                topStart: null,
                topEnd: { search: { placeholder: "Search" } },
                bottomStart: [
                    "pageLength",
                    "info",
                    {
                        paging: {
                            type: "full_numbers",
                        },
                    },
                ],
                bottomEnd: null,
            },
            language: { search: ''},
            initComplete: function () {
                actionButton("#rodTable");
                hideLoader();
            },
            drawCallback: function (settings) {
                $(".rod-unhide").parents("tr").css("color", "#b2acac");
            },

            "ajax": {
                "url": tableDataUrl + '?hidden=' + hiddenCheck,
                "dataSrc": function (data) {
                    if (tableIdentifier === LATE_IDENTIFIER) {
                        transformLateDataJson(data);
                    } else if (tableIdentifier === ROOT_CAUSE_IDENTIFIER) {
                        transformRootCauseDataJson(data);
                    } else if (tableIdentifier === ROOT_CAUSE_SUB_IDENTIFIER) {
                        transformRootCauseSubDataJson(data);
                    } else if (tableIdentifier === ROOT_CAUSE_CLASS_IDENTIFIER) {
                        transformRootCauseClassDataJson(data);
                    } else {
                        transformResponsiblePartyJson(data);
                    }
                    return formattedData;
                }
            },

            "aaSorting": [],
            "order": [[1, "asc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aoColumns": [
                {
                    "mData": "id",
                    "visible": false,
                    "mRender": function (data, type, row) {
                        return '<span id="lateId">' + row.id + '</span>';
                    }
                },
                {
                    "mData": "name",
                    "width": "20%",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "type",
                    "visible": (tableIdentifier === LATE_IDENTIFIER),
                    mRender: function (data, type, row) {
                        if (tableIdentifier === LATE_IDENTIFIER) {
                            return encodeToHTML($.i18n._(data));
                        }
                        return null;
                    }

                },
                {
                    "mData": "mapping",
                    "visible": ((tableIdentifier !== RESP_PARTY_IDENTIFIER) && (tableIdentifier !== ROOT_CAUSE_SUB_IDENTIFIER) && (tableIdentifier !== ROOT_CAUSE_CLASS_IDENTIFIER)),
                    mRender: function (data, type, row) {
                        if ((tableIdentifier !== RESP_PARTY_IDENTIFIER) && (tableIdentifier !== ROOT_CAUSE_SUB_IDENTIFIER) && (tableIdentifier !== ROOT_CAUSE_CLASS_IDENTIFIER)) {
                            return encodeToHTML(data);
                        }
                        return null;
                    }
                }, {
                    "mData": "rootCauseSub",
                    "visible": (tableIdentifier === ROOT_CAUSE_IDENTIFIER),
                    mRender: function (data, type, row) {
                        if (tableIdentifier === ROOT_CAUSE_IDENTIFIER) {
                            return encodeToHTML(data);
                        }
                        return null;
                    }
                }, {
                    "mData": "rootCauseClass",
                    "visible": (tableIdentifier === LATE_IDENTIFIER),
                    mRender: function (data, type, row) {
                        if (tableIdentifier === LATE_IDENTIFIER) {
                            return encodeToHTML(data);
                        }
                        return null;
                    }
                },
                {
                    "mData": "ownerApp",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": null,
                    "bSortable":false,
                    "mRender": function (data, type, row) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs rod-show" role="menuitem" href="javascript:void(0)" data-id="' + row.id + '" data-label="' + row.name + '" data-late-type="' + row.typeId + '" data-hidden-date="' + row.hiddenDate + '"data-toggle="modal" data-target="#rodModal" data-app-type=' + row.ownerApp + '>' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="rod-edit" role="menuitem" href="javascript:void(0)" data-id="' + row.id + '" data-label="' + row.name + '" data-late-type="' + row.typeId + '" data-hidden-date="' + row.hiddenDate + '" data-toggle="modal" data-target="#rodModal" data-app-type=' + row.ownerApp + '>' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit hide-delete" role="menuitem" href="#" data-toggle="modal" data-action=' + deleteAction + ' \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._(deleteLabel) + '" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.name) + '" data-app-type=' + row.ownerApp + '>' + $.i18n._('delete') + '</a></li>';
                        if (data.hiddenDate) {
                            actionButton = actionButton + '<li role="presentation"><a class="rod-unhide hide" role="menuitem" href="#" </a></li>';
                        }
                        actionButton = actionButton + '</ul></div>';
                        return actionButton;
                    }
                }
            ]
        });
        return mappingTable;
    };

    init_mapping_table(LATE_IDENTIFIER);

    function transformLateDataJson(data) {
        formattedData = [];

        for (var i in data.lateJson) {
            var lateObj = JSON.parse(data.lateJson[i]);
            var row = {};
            var lateId = lateObj.id;
            row.id = lateId;
            row.type = lateObj.type;
            row.typeId = lateObj.typeId;
            row.name = lateObj.textDesc;
            var rootCauseData = [];
            var rootCauseClass = [];
            for (var j in data.rootCauseJson) {
                var rootCauseObj = JSON.parse(data.rootCauseJson[j]);
                for (var k in lateObj.linkedIds) {
                    if (parseInt(lateObj.linkedIds[k]) === rootCauseObj.id) {
                        rootCauseData.push(rootCauseObj.textDesc);
                    }
                }
            }
            for (var j in data.rootCauseClassJson) {
                var rootCauseClassObj = JSON.parse(data.rootCauseClassJson[j]);
                for (var k in lateObj.rootCauseClassIds) {
                    if (parseInt(lateObj.rootCauseClassIds[k]) === rootCauseClassObj.id) {
                        rootCauseClass.push(rootCauseClassObj.textDesc);
                    }
                }
            }
            row.rootCauseClass = rootCauseClass;
            row.mapping = rootCauseData;
            row.ownerApp = lateObj.ownerApp;
            row.hiddenDate = lateObj.hiddenDate;
            formattedData.push(row);
        }
    }

    function transformRootCauseDataJson(data) {
        formattedData = [];
        var mappingData = [];

        for (var i in data.rootCauseJson) {
            var rootCauseObj = JSON.parse(data.rootCauseJson[i]);
            var row = {};
            var rootCauseId = rootCauseObj.id;
            row.id = rootCauseId;
            row.name = rootCauseObj.textDesc;
            var responsiblePartyData = [];
            var rootCauseSub = [];
            for (var j in data.responsiblePartyJson) {
                var responsiblePartyObj = JSON.parse(data.responsiblePartyJson[j]);
                for (var k in responsiblePartyObj.linkIds) {
                    if (parseInt(responsiblePartyObj.linkIds[k]) === rootCauseId) {
                        responsiblePartyData.push(responsiblePartyObj.textDesc);
                    }
                }
            }
            for (var j in data.rootCauseSubJson) {
                var rootCauseSubObj = JSON.parse(data.rootCauseSubJson[j]);
                for (var k in rootCauseSubObj.linkIds) {
                    if (parseInt(rootCauseSubObj.linkIds[k]) === rootCauseId) {
                        rootCauseSub.push(rootCauseSubObj.textDesc);
                    }
                }
            }
            row.rootCauseSub = rootCauseSub
            row.mapping = responsiblePartyData;
            row.ownerApp = rootCauseObj.ownerApp;
            row.hiddenDate = rootCauseObj.hiddenDate;
            formattedData.push(row);
        }
    }

    function transformResponsiblePartyJson(data) {
        formattedData = [];
        for (var i in data.responsiblePartyJson) {
            var row = {};
            var responsiblePartyObj = JSON.parse(data.responsiblePartyJson[i]);
            row.id = responsiblePartyObj.id;
            row.name = responsiblePartyObj.textDesc;
            row.ownerApp = responsiblePartyObj.ownerApp;
            row.hiddenDate = responsiblePartyObj.hiddenDate;
            formattedData.push(row);
        }
    }

    function transformRootCauseSubDataJson(data) {
        formattedData = [];
        for (var i in data.rootCauseSubCategoryJson) {
            var row = {};
            var obj = JSON.parse(data.rootCauseSubCategoryJson[i]);
            row.id = obj.id;
            row.name = obj.textDesc;
            row.ownerApp = obj.ownerApp;
            row.hiddenDate = obj.hiddenDate;
            formattedData.push(row);
        }
    }

    function transformRootCauseClassDataJson(data) {
        formattedData = [];
        for (var i in data.rootCauseClassJson) {
            var row = {};
            var obj = JSON.parse(data.rootCauseClassJson[i]);
            row.id = obj.id;
            row.name = obj.textDesc;
            row.ownerApp = obj.ownerApp;
            row.hiddenDate = obj.hiddenDate;
            formattedData.push(row);
        }
    }

    $("#createButton").on('click', function(e){
        $("#warningNote").show();
        $("#errorDiv").hide();
        $(".modalError").hide();
        var labelElement = $("#label");
        labelElement.val('');
        labelElement.prop('maxlength', 2000);
        $("#submitButton").text($.i18n._('save'));
        var mappingElement = $("#mapping");
        mappingElement.attr('disabled', false);
        var hideOption = $("#hide");
        hideOption.attr("disabled", false);
        hideOption.attr("checked", true);
        mappingElement.empty();
        labelElement.prop('readonly', false);
        var ownerAppElement = $("#ownerApp");
        ownerAppElement.val(PVC);
        var pvqOwnerAppOption = $("#ownerApp option[value=" + PVQ + "]");
        var inboundOwnerAppOption = $("#ownerApp option[value=" + PVC_Inbound + "]");
        pvqOwnerAppOption.attr('disabled', false);
        inboundOwnerAppOption.attr('disabled', false);
        ownerAppElement.attr('disabled', false);
        $("#cancelButtonForView").hide($.i18n._('update'));
        $("#cancelButtonForView").attr("hidden", true);
        $("#submitButton").show($.i18n._('Save'));
        $("#cancelButton").show($.i18n._('update'));
        $(".rootCauseSubDiv").hide();
        $(".rootCauseClassDiv").hide();
        if ($("#showLate").hasClass('active')) {
            $(".rootCauseClassDiv").show();
            dataKey = 'rootCauseJson';
            mappingElement.val(null).trigger('change');
            mappingUrl = getLateMapping;
            $("#rodModal").find('.rxmain-container-header-label').first().text($.i18n._('app.lateMapping.create'));
            $("label[for='mapping']").text($.i18n._('app.rootCause.name'));
            initializeMappingSelect(mappingUrl, mappingElement, dataKey, ownerAppElement.val());
            populateLateTypeSelect(ownerAppElement.val(), null, false);
            $("#rootCauseClass").val(null).trigger('change');
            $("#rootCauseClass").attr('disabled', false);
            initializeMappingSelect(getRootCauseClassList, $("#rootCauseClass"), "rootCauseClassJson", ownerAppElement.val());
            $("#lateType").attr('disabled', false);
            if (ownerAppElement.val() == PVC_Inbound) ownerAppElement.val(PVC);
            inboundOwnerAppOption.attr('disabled', true);
            $(".lateTypeDiv").show();
            $(".mappingDiv").show();
        } else if ($("#showRootCause").hasClass('active')) {
            $(".rootCauseSubDiv").show();
            dataKey = 'responsiblePartyJson';
            mappingElement.val(null).trigger('change');
            $("#rootCauseSub").val(null).trigger('change');
            mappingUrl = getRootCauseMapping;
            $("#rodModal").find('.rxmain-container-header-label').first().text($.i18n._('app.rootCauseMapping.create'));
            $("label[for='mapping']").text($.i18n._('app.responsibleParty.name'));
            initializeMappingSelect(mappingUrl, mappingElement, dataKey, ownerAppElement.val());
            $("#rootCauseSub").attr('disabled', false);
            initializeMappingSelect(getRootCauseSubCategoryList, $("#rootCauseSub"), "rootCauseSubCategoryJson", ownerAppElement.val());
            $(".mappingDiv").show();
            $(".lateTypeDiv").hide();
        } else {
            if ($("#showRootCauseClass").hasClass('active') || $("#showRootCauseSub").hasClass('active')) {
                if (ownerAppElement.val() == PVQ) ownerAppElement.val(PVC);
                pvqOwnerAppOption.attr('disabled', true);
            }
            mappingElement.attr('disabled', true);
            $("#rodModal").find('.rxmain-container-header-label').first().text($.i18n._('app.' + getLabel() + '.create'));
            $(".mappingDiv").hide();
            $(".lateTypeDiv").hide();
        }
    });

    function getLabel() {
        if ($("#showRootCauseSub").hasClass('active')) return "rootcauseSub";
        if ($("#showRootCauseClass").hasClass('active')) return "rootCauseClass";
        return "resp.party"
    }

    function initializeMappingSelect(mappingUrl, mappingElement, dataKey, ownerApp) {
        $.ajax({
            url: mappingUrl + '?hidden=' + hidden,
            data: {ownerApp: ownerApp},
            dataType: 'json'
        })
            .done(function (data) {
                var mappingData = data[dataKey];
                var results = [];
                for (var opt in mappingData) {
                    var mappingObj = JSON.parse(mappingData[opt]);
                    var optionObj = {};
                    optionObj.id = mappingObj.id;
                    optionObj.text = mappingObj.textDesc;
                    optionObj.hiddenDate = mappingObj.hiddenDate;
                    if (optionObj.hiddenDate) {
                        optionObj.disabled = true;
                    }
                    results.push(optionObj);
                }
                mappingElement.select2({
                    multiple: true,
                    data: results,
                    formatResult: function (option) {
                        if (option.disabled) {
                            return '<span style="color: gray;">' + option.text + '</span>';
                        } else {
                            return option.text;
                        }
                    }
                });
            });
    }

    $(document).on('change', "#ownerApp", function (e) {
        if ($(this).val() == "PVQ") {
            $(".rootCauseSubDiv").hide();
            $(".rootCauseClassDiv").hide();
        } else {
            if ($("#showLate").hasClass('active')) {
                $(".rootCauseClassDiv").show();
            }
            if ($("#showRootCause").hasClass('active')) {
                $(".rootCauseSubDiv").show();
            }
        }
    });

    $(document).on('click', ".rod-edit", function (e) {
        $(".modalError").hide();
        $("#warningNote").show();
        var labelElement = $("#label");
        labelElement.val($(this).data('label'));
        labelElement.prop("readonly", false);
        var mappingElement = $("#mapping");
        mappingElement.attr("disabled", false);
        var ownerAppElement = $("#ownerApp");
        ownerAppElement.attr("disabled", false);
        var hideOption = $("#hide");
        hideOption.attr("disabled", false);
        $("#ownerApp option[value=" + PVC_Inbound + "]").attr('disabled', false);
        if ($(this).data('hiddenDate')) {
            hideOption.prop("checked", false);
        } else {
            hideOption.prop('checked', true);
        }
        ownerAppElement.val($(this).data('app-type')).attr("disabled", true);
        var linkId = $(this).data('id');
        $("#objectId").val(linkId);
        var submitButtonElement = $("#submitButton");
        submitButtonElement.text($.i18n._('update'));
        submitButtonElement.show($.i18n._('update'));
        $("#cancelButton").show($.i18n._('update'));
        var cancelButtonViewElement = $("#cancelButtonForView");
        cancelButtonViewElement.hide($.i18n._('update'));
        cancelButtonViewElement.attr("hidden", true);
        $(".rootCauseSubDiv").hide();
        $(".rootCauseClassDiv").hide();
        if ($("#showLate").hasClass('active')) {
            if (ownerAppElement.val() == PVC || ownerAppElement.val() == PVC_Inbound) {
                $(".rootCauseClassDiv").show();
                $("#rootCauseClass").attr('disabled', false);
                populateMappingSelect(getRootCauseClassList, $("#rootCauseClass"), "rootCauseClassJson", linkId, ownerAppElement.val());
            }
            dataKey = 'rootCauseJson';
            $("#rodModal").find('.rxmain-container-header-label').first().text($.i18n._('app.lateMapping.edit'));
            $("label[for='mapping']").text($.i18n._('app.rootCause.name'));
            mappingUrl = getLateMapping;
            populateMappingSelect(mappingUrl, mappingElement, dataKey, linkId, ownerAppElement.val());
            var typeElement = $("#lateType");
            if (ownerAppElement.val() == PVC_Inbound) {
                populateLateTypeSelect($(this).data('app-type'), $(this).data('late-type'), true);
                typeElement.val($(this).data('late-type'));
            } else {
                populateLateTypeSelect($(this).data('app-type'), $(this).data('late-type'), false);
                typeElement.attr('disabled', false);
            }
            $(".lateTypeDiv").show();
            $(".mappingDiv").show();
        } else if ($("#showRootCause").hasClass('active')) {
            if (ownerAppElement.val() == PVC) {
                $(".rootCauseSubDiv").show();
                $("#rootCauseSub").attr('disabled', false);
                populateMappingSelect(getRootCauseSubCategoryList, $("#rootCauseSub"), "rootCauseSubCategoryJson", linkId, ownerAppElement.val());
            }
            dataKey = 'responsiblePartyJson';
            $("#rodModal").find('.rxmain-container-header-label').first().text($.i18n._('app.rootCauseMapping.edit'));
            $("label[for='mapping']").text($.i18n._('app.responsibleParty.name'));
            mappingUrl = getRootCauseMapping;
            populateMappingSelect(mappingUrl, mappingElement, dataKey, linkId, ownerAppElement.val());
            $(".lateTypeDiv").hide();
            $(".mappingDiv").show();
        } else {
            mappingElement.attr('disabled', true);
            $("#rodModal").find('.rxmain-container-header-label').first().text($.i18n._('app.' + getLabel() + '.edit'));
            $(".mappingDiv").hide();
            $(".lateTypeDiv").hide();
        }
    });

    $(document).on('click', ".rod-show", function (e) {
        $(".modalError").hide();
        $("#warningNote").hide();
        var labelElement = $("#label");
        labelElement.val($(this).data('label'));
        labelElement.prop("readonly", true);
        var mappingElement = $("#mapping");
        mappingElement.attr("disabled", true);
        var ownerAppElement = $("#ownerApp");
        ownerAppElement.attr("disabled", true);
        var hideOption = $("#hide");
        hideOption.attr("disabled", true);
        if ($(this).data('hiddenDate')) {
            hideOption.prop("checked", false);
        } else {
            hideOption.prop('checked', true);
        }
        ownerAppElement.val($(this).data('app-type'));
        var linkId = $(this).data('id');
        $("#objectId").val(linkId);
        var dataKey;
        var submitButtonElement = $("#submitButton");
        submitButtonElement.hide($.i18n._('update'));
        submitButtonElement.attr("hidden", true);
        var cancelButtonElement = $("#cancelButton");
        cancelButtonElement.hide($.i18n._('update'));
        cancelButtonElement.attr("hidden", true);

        $("#cancelButtonForView").show($.i18n._('Close'));
        $(".rootCauseSubDiv").hide();
        $(".rootCauseClassDiv").hide();
        if ($("#showLate").hasClass('active')) {
            $(".rootCauseClassDiv").show();
            dataKey = 'rootCauseJson';
            $("#rodModal").find('.rxmain-container-header-label').first().text($.i18n._('app.lateMapping.view'));
            $("label[for='mapping']").text($.i18n._('app.rootCause.name'));
            populateMappingSelect(getLateMapping, mappingElement, dataKey, linkId, ownerAppElement.val());
            populateLateTypeSelect($(this).data('app-type'), $(this).data('late-type'), true);
            $("#rootCauseClass").attr('disabled', true);
            populateMappingSelect(getRootCauseClassList, $("#rootCauseClass"), "rootCauseClassJson", linkId, ownerAppElement.val());
            $(".lateTypeDiv").show();
            $(".mappingDiv").show();
        } else if ($("#showRootCause").hasClass('active')) {
            $(".rootCauseSubDiv").show();
            dataKey = 'responsiblePartyJson';
            $("#rodModal").find('.rxmain-container-header-label').first().text($.i18n._('app.rootCauseMapping.view'));
            $("label[for='mapping']").text($.i18n._('app.responsibleParty.name'));
            $(".mappingDiv").show();
            $(".lateTypeDiv").hide();
            populateMappingSelect(getRootCauseMapping, mappingElement, dataKey, linkId, ownerAppElement.val());
            $("#rootCauseSub").attr('disabled', true);
            populateMappingSelect(getRootCauseSubCategoryList, $("#rootCauseSub"), "rootCauseSubCategoryJson", linkId, ownerAppElement.val());
        } else {
            mappingElement.attr('disabled', true);
            $("#rodModal").find('.rxmain-container-header-label').first().text($.i18n._('app.' + getLabel() + '.view'));
            $(".mappingDiv").hide();
            $(".lateTypeDiv").hide();
        }
    });

    $(".alert-close").click(function(){
        $(".modalError").hide();
    });

    function populateMappingSelect(mappingUrl, mappingElement, dataKey, linkId, ownerApp){
        var selectedOpts = [];
        var hidden = true;
        $.ajax({
            url: mappingUrl + '?hidden=' + hidden,
            data: {ownerApp: ownerApp},
            dataType: 'json'
        })
            .done(function (data) {
                var mappingData = data[dataKey];
                var selected;
                var results = [];
                for (var opt in mappingData) {
                    var mappingObj = JSON.parse(mappingData[opt]);
                    selected = false;
                    for (var i in mappingObj.linkIds) {
                        if (mappingObj.linkIds[i] == linkId) {
                            selected = true;
                        }
                    }
                    var optionObj = {};
                    optionObj.id = mappingObj.id;
                    optionObj.text = mappingObj.textDesc;
                    optionObj.hiddenDate = mappingObj.hiddenDate;
                    if (selected) {
                        selectedOpts.push(mappingObj.id);
                    }
                    if (optionObj.hiddenDate) {
                        optionObj.disabled = true;
                    }
                    results.push(optionObj);
                }
                mappingElement.select2({
                    multiple: true,
                    data: results
                });
                mappingElement.select2({
                    multiple: true,
                    data: results,
                    formatResult: function (option) {
                        if (option.disabled) {
                            return '<span style="color: gray;">' + option.text + '</span>';
                        } else {
                            return option.text;
                        }
                    }
                });

                mappingElement.val(selectedOpts).trigger('change.select2');
            });
    }


    $("#hiddenList").on("change", function () {
        showLoader();
        if ($("#showLate").hasClass('active')) {
            init_mapping_table(LATE_IDENTIFIER);
        } else if ($("#showRootCause").hasClass('active')) {
            init_mapping_table(ROOT_CAUSE_IDENTIFIER);

        } else if ($("#showRespParty").hasClass('active')) {
            init_mapping_table(RESP_PARTY_IDENTIFIER);

        } else if ($("#showRootCauseSub").hasClass('active')) {
            init_mapping_table(ROOT_CAUSE_SUB_IDENTIFIER);

        } else if ($("#showRootCauseClass").hasClass('active')) {
            init_mapping_table(ROOT_CAUSE_CLASS_IDENTIFIER);

        }

    });

    $("#submitButton").on('click', function (e) {
        var saveRodUrl;
        var dataObj = {};
        dataObj.label = $("#label").val();
        if ((dataObj.label == "") || (/;|#|<|>|'|"/.test(dataObj.label))) {
            $(".modalError").show();
            return;
        }
        dataObj.ownerApp = $("#ownerApp").val();
        dataObj.id = $("#objectId").val();
        dataObj.hide = $("#hide").is(':checked');
        if ($("#showLate").hasClass('active')) {
            dataObj.mapping = $("#mapping").val();
            dataObj.type = $("#lateType").val();
            dataObj.rootCauseClass = $("#rootCauseClass").val();
            dataObj.active = 'showLate'
            saveRodUrl = saveLateUrl;
        } else if ($("#showRootCause").hasClass('active')) {
            dataObj.mapping = $("#mapping").val();
            dataObj.rootCauseSub = $("#rootCauseSub").val();
            dataObj.active = 'showRootCause';
            saveRodUrl = saveRootCauseUrl;
        } else if ($("#showRootCauseSub").hasClass('active')) {
            dataObj.active = 'showRootCauseSub';
            saveRodUrl = saveRootCauseSubUrl;
        } else if ($("#showRootCauseClass").hasClass('active')) {
            dataObj.active = 'showRootCauseClass';
            saveRodUrl = saveRootCauseClassUrl;
        } else {
            dataObj.active = 'showResponsibleParty';
            saveRodUrl = saveRespPartyUrl;
        }
        if (dataObj.hide == false) {
            $.ajax({
                url: hideWarningUrl,
                data: dataObj,
                method: 'POST'
            })
                .done(function (result) {
                    if (result == 'true') {
                        $('#warningModal .description').text($.i18n._('app.RCAModule.autoRCA.warning'));
                        $('#warningModal').modal('show');
                        $('#warningButton').off('click').on('click', function () {
                            $('#warningModal').modal('hide');
                            isWarningModalOpen = false;
                            saveData(saveRodUrl, dataObj);
                        });
                    } else {
                        saveData(saveRodUrl, dataObj);
                    }
                });
        } else {
            saveData(saveRodUrl, dataObj);
        }

    });

    function saveData(saveRodUrl, dataObj) {
        if (!isWarningModalOpen) { // Perform the AJAX call and page reload only if the warning modal is not open
            $.ajax({
                url: saveRodUrl,
                data: dataObj,
                method: 'POST'
            })
                .done(function (result) {
                    window.location.reload();
                })
                .fail(function (errorResponse) {
                    if (errorResponse.status === 409) {
                        showInlineNotification(
                            'danger',
                            $.i18n._('app.rod.mapping.record.exists.error', dataObj.label, dataObj.ownerApp),
                            '#rodModal'
                        );
                    }
                })
        }
    }


    function initRodSelectionButtons() {
        var buttonHtml = '<div class="rxmain-container-row">' +
            '<div class="col-lg-12">' +
            '<ul class="nav nav-tabs" role="tablist" style="height: 52px;">' +
            '<li role="presentation" id="showLate" class="active"><a href="javascript:void(0)" class="tab-ref" aria-controls="overviewTab" role="tab" data-toggle="tab" aria-expanded="true">' +
            $.i18n._('app.fixed.template.issueType') +
            '</a>' +
            '</li>' +
            '<li role="presentation"  id="showRootCause"><a href="javascript:void(0)" class="tab-ref" aria-controls="publisherTab" role="tab" data-toggle="tab" aria-expanded="false">' +
            $.i18n._('app.fixed.template.rootCause') +
            '</a>' +
            '</li>' +
            '<li role="presentation"  id="showRootCauseClass"><a href="javascript:void(0)" class="tab-ref" aria-controls="publisherTab" role="tab" data-toggle="tab" aria-expanded="false">' +
            $.i18n._('app.fixed.template.rootCauseClass') +
            '</a>' +
            '</li>'
            + '<li role="presentation"  id="showRootCauseSub"><a href="javascript:void(0)" class="tab-ref" aria-controls="publisherTab" role="tab" data-toggle="tab" aria-expanded="false">' +
            $.i18n._('app.fixed.template.rootCauseSub') +
            '</a>' +
            '</li>' +
            '<li role="presentation" id="showRespParty"><a href="javascript:void(0)" class="tab-ref" aria-controls="sectionsTab" role="tab" data-toggle="tab" aria-expanded="false">' +
            $.i18n._('app.fixed.template.responsibleParty') +
            '</a>' +
            '</li>';
        if (isAdmin) {
            buttonHtml += '<li role="presentation" id="rcaMandatory"><a href="javascript:void(0)" class="tab-ref" aria-controls="sectionsTab" role="tab" data-toggle="tab" aria-expanded="false">' +
                $.i18n._('app.label.rca.mandatory.fields') +
                '</a>' +
                '</li>';
        }
        buttonHtml += '</ul></div>' +
            '</div>';
        $("div.case-quality-datatable-toolbar").css("width", "100%").html(buttonHtml);
    }


    $(document).on('click', "#showLate", function (e) {
        showLoader();
        $(".active").removeClass("active");
        $("#showLate").addClass("active");
        $(".tab-ref").attr("aria-expanded", false);
        $("#showLate a").attr("aria-expanded", true);
        $('#createButton').show();
        init_mapping_table(LATE_IDENTIFIER);
        $("#rcaTab").hide();
        $("#hiddenList").attr("disabled", false)
    });

    $(document).on('click', "#showRootCause", function (e) {
        showLoader();
        $(".active").removeClass("active");
        $("#showRootCause").addClass("active");
        $(".tab-ref").attr("aria-expanded", false);
        $("#showRootCause a").attr("aria-expanded", true);
        $('#createButton').show();
        init_mapping_table(ROOT_CAUSE_IDENTIFIER);
        $("#rcaTab").hide();
        $("#hiddenList").attr("disabled", false)
    });
//-----------------++++++++===tab for RCA Mapping+++++===----------------->
    $(document).on('click', "#rcaMandatory", function (e) {
        $(".active").removeClass("active");
        $("#rcaMandatory").addClass("active");
        $(".tab-ref").attr("aria-expanded", false);
        $("#rcaMandatory a").attr("aria-expanded", true);
        $('#createButton').hide();
        $('#rodTable_wrapper').hide();
        $("#rcaTab").show();
        $("#hiddenList").attr("disabled", true)
    });
    $(document).on('click', "#showRespParty", function (e) {
        showLoader();
        $(".active").removeClass("active");
        $("#showRespParty").addClass("active");
        $(".tab-ref").attr("aria-expanded", false);
        $("#showRespParty a").attr("aria-expanded", true);
        $('#createButton').show();
        init_mapping_table(RESP_PARTY_IDENTIFIER);
        $("#rcaTab").hide();
        $("#hiddenList").attr("disabled", false)
    });

    $(document).on('click', "#showRootCauseSub", function (e) {
        showLoader();
        $(".active").removeClass("active");
        $("#showRootCauseSub").addClass("active");
        $(".tab-ref").attr("aria-expanded", false);
        $("#showRootCauseSub a").attr("aria-expanded", true);
        $('#createButton').show();
        init_mapping_table(ROOT_CAUSE_SUB_IDENTIFIER);
        $("#rcaTab").hide();
        $("#hiddenList").attr("disabled", false)
    });

    $(document).on('click', "#showRootCauseClass", function (e) {
        showLoader();
        $(".active").removeClass("active");
        $("#showRootCauseClass").addClass("active");
        $(".tab-ref").attr("aria-expanded", false);
        $("#showRootCauseClass a").attr("aria-expanded", true);
        $('#createButton').show();
        init_mapping_table(ROOT_CAUSE_CLASS_IDENTIFIER);
        $("#rcaTab").hide();
        $("#hiddenList").attr("disabled", false)
    });

    $(document).on('change', "#ownerApp", function (e) {
        $("#mapping").empty();
        initializeMappingSelect(mappingUrl, $("#mapping"), dataKey, $("#ownerApp").val());
        populateLateTypeSelect($("#ownerApp").val(), null, false);
        if($('#showRootCause').hasClass('active') && $("#ownerApp").val()!=PVQ){
            $('#rootCauseSub').empty();
            initializeMappingSelect(getRootCauseSubCategoryList, $("#rootCauseSub"), "rootCauseSubCategoryJson",$("#ownerApp").val());
        }
        $("#rootCauseSub").val("").trigger("change");
        $("#rootCauseClass").val("").trigger("change");
    });

    function populateLateTypeSelect(ownerApp, selectedLate, disabled) {
        var lateTypeElement = $("#lateType");
        $.ajax({
            url: getLateTypeUrl + '?hidden=' + hidden,
            data: {ownerApp: ownerApp},
            dataType: 'json'
        })
            .done(function (data) {
                var results = [];
                for (var opt in data) {
                    var mappingObj = data[opt];
                    var optionObj = {};
                    optionObj.id = mappingObj.id;
                    optionObj.text = mappingObj.text;
                    results.push(optionObj);
                    if (selectedLate === null) {
                        selectedLate = mappingObj.id;
                    }
                }
                lateTypeElement.select2({
                    data: results,
                    dropdownParent: $(document).find("#rodModal")
                });
                lateTypeElement.val(selectedLate).trigger('change.select2');
                lateTypeElement.attr('disabled', disabled);
            });
    }

});