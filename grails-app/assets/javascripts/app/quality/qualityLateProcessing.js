var columns = [];
var result = [];
var nonEditableFields = [];
var currentRcaId;
var dataType = "";
var correctiveDate = false;
var preventiveDate = false;
ROD_FIELD_ENUM = {
    ISSUE_TYPE: 'Issue_Type',
    ROOT_CAUSE: 'Root_Cause',
    ROOT_CAUSE_CLASS: 'Root_Cause_Class',
    ROOT_CAUSE_SUB_CAT: 'Root_Cause_Sub_Cat',
    RESP_PARTY: 'Resp_Party',
    CORRECTIVE_ACTION: 'Corrective_Action',
    PREVENTIVE_ACTION: 'Preventive_Action',
    CORRECTIVE_DATE: 'Corrective_Date',
    PREVENTIVE_DATE: 'Preventive_Date',
    INVESTIGATION: 'Investigation',
    SUMMARY: 'Summary',
    ACTIONS: 'Actions'
}

function initReasonOfDelay() {

    $(document).on("click", ".reasonOfDelayModalBtn", function (evt) {
        evt.preventDefault();
        var viewMode = $(this).attr("data-viewMode") == "true";
        dataType = $(this).attr("data-dataType");
        $("#reasonOfDelayModalId").find(".editLate").trigger("change");
        $(".lateSelectDiv").find("select").select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
        updatePrimary();
        var el = $(this);
        currentRcaId = el.attr("data-id");
        showResizableModal("#reasonOfDelayModalId", 1300);
        $.ajax({
            aysnc: false,
            method: 'POST',         // POST for handling large amount of data
            url: getAllRcasForQualityCaseUrl,
            data: {
                "id": currentRcaId,
                "dataType": dataType,
                "selectedIds": ((selectedIds && selectedIds.length > 0) ? selectedIds.join(";") : null),
                selectedCases: ((selectedCases && selectedCases.length > 0) ? JSON.stringify(selectedCases) : "")
            },
            dataType: 'json',
            beforeSend: function () {
                showLoader();
            },
        })
            .done(function (resultObjMap) {
                var resultObj = resultObjMap['data'];
                nonEditableFields = resultObjMap['nonEditableList'];
                var mandatoryFields = resultObjMap['mandatoryList'];
                var selectedLate = resultObjMap['qualityIssueTypeId'];
                $("#reasonOfDelayBody").empty();
                var modal = $("#reasonOfDelayBody").closest(".modal-body");
                let userSelect = modal.find("[name=assignedToUser]");
                let groupSelect = modal.find("[name=assignedToUserGroup]");
                if (userSelect.hasClass('select2-hidden-accessible')) {
                    userSelect.select2("destroy");
                    groupSelect.select2("destroy");

                }
                userSelect.attr("data-value", resultObjMap.assignedToUser).val(resultObjMap.assignedToUser)
                groupSelect.attr("data-value", resultObjMap.assignedToUserGroup).val(resultObjMap.assignedToUserGroup)
                bindShareWith(userSelect, sharedWithUserListUrl, sharedWithValuesUrl, "100%", true, $(".reasonOfDelayModalBody"), "placeholder.selectUsers").on("change", function () {
                    groupSelect.attr("data-extraParam", JSON.stringify({user: $(this).val()}));
                    groupSelect.data('select2').results.clear()
                });
                bindShareWith(groupSelect, sharedWithGroupListUrl, sharedWithValuesUrl, "100%", true, $(".reasonOfDelayModalBody"), "placeholder.selectGroup").on("change", function () {
                    userSelect.attr("data-extraParam", JSON.stringify({userGroup: $(this).val()}));
                    userSelect.data('select2').results.clear()

                });
                groupSelect.attr("data-extraParam", JSON.stringify({user: userSelect.attr("data-value")}));
                userSelect.attr("data-extraParam", JSON.stringify({userGroup: groupSelect.attr("data-value")}));
                $(".justificationDiv").hide()
                $(".justificationDiv input").val("");
                var select = modal.find(".workflow");
                select.empty();
                select.val("");
                // Enabled viewMode conditionally to resolve issue https://rxlogixdev.atlassian.net/browse/PVR-68619
                if(resultObjMap.containsFinalState) {
                    viewMode = true;
                }
                else {
                    viewMode = false;
                }
                if (resultObjMap.workflow) {
                    modal.find(".workflowCurrentId").val(resultObjMap.workflow.id);
                    select.append("<option selected value=''>" + resultObjMap.workflow.name + "</option>")
                    for (var i = 0; i < resultObjMap.workflowList.length; i++) {
                        select.append("<option value='" + resultObjMap.workflowList[i].id + "'>" + resultObjMap.workflowList[i].name + "</option>")
                    }
                    modal.find(".workflow").val(resultObjMap.workflow.id).trigger('change');
                    modal.find(".workflow").parent().show();
                    modal.find(".noworkflow").hide();
                } else {
                    modal.find(".workflow").parent().hide();
                    modal.find(".noworkflow").show();
                }


                if (resultObj.length == 0) {
                    createDefaultLateSelect(selectedLate);
                } else {
                    for (var m = 0; m < resultObj.length; m++) {
                        var $row = createRow(resultObj[m].id, resultObj[m].primaryFlag, resultObj[m].lateValue, resultObj[m].rootCauseValue
                            , resultObj[m].responsiblePartyValue, resultObj[m].correctiveActionValue, resultObj[m].preventiveActionValue,
                            resultObj[m].correctiveDate, resultObj[m].preventiveDate, resultObj[m].investigation, resultObj[m].summary, resultObj[m].actions);
                        if ($row) {
                            $("#reasonOfDelayBody").append($row);
                            $row.find(".select2-box,.editRootCause,.editResponsibleParty,.editPreventativeAction,.editCorrectiveAction").select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
                        }
                    }
                }
                if (viewMode) {
                    $("#reasonOfDelayModalForm").find("input, button, select, textarea").attr("disabled", true);
                    $("#reasonOfDelayModalForm").find(".table-add, .table-remove").hide();
                    $(".saveReasonsOfDelay").hide();
                } else {
                    $("#reasonOfDelayModalForm").find("input, button, select, textarea").attr("disabled", false);
                    $("#reasonOfDelayModalForm").find(".table-add, .table-remove").show();
                    $(".saveReasonsOfDelay").show();
                }
                $("#reasonOfDelayModalId").find(".editPreventativeAction,.editCorrectiveAction").trigger("change");
                if (nonEditableFields.length > 0) {
                    for (var m = 0; m < nonEditableFields.length; m++) {
                        $("#reasonOfDelayModalId").find('.' + nonEditableFields[m].name).attr("disabled", "disabled");
                    }
                }
                $("#reasonOfDelayModalId").find('.required-indicator').addClass('hide');
                if (mandatoryFields.length > 0) {
                    for (var m = 0; m < mandatoryFields.length; m++) {
                        $("#reasonOfDelayModalId").find('.required-indicator.' + mandatoryFields[m].name).removeClass('hide');
                    }
                }

                bindSelectRenderEvents();
                showResizableModal("#reasonOfDelayModalId", 1300);
            })
            .always(function () {
                hideLoader();
            });
    });

    function bindSelectRenderEvents() {
        const selector = '.reasonOfDelayModalBody select.updateData, .reasonOfDelayModalFormHeader select:not([name=workflowRule])';
        $(document).off('select2:open', selector);
        $(document).on('select2:open', selector, function (e) {
            const $selectContainer = $(this).next('.select2.select2-container');
            const selectOffset = $selectContainer.offset();
            const $openedContainer = $('.select2-container--open:not(.select2-container--below)');
            $openedContainer.find('.select2-dropdown').offset({left: selectOffset.left});
        });
    }

    function showEmptyLateError(label) {
        var reasonOfDelayModal = $("#reasonOfDelayModalId");
        reasonOfDelayModal.find('.alert-danger').removeClass('hide');
        reasonOfDelayModal.find('.errorMessageSpan').html(label);
        setTimeout(function () {
            reasonOfDelayModal.find('.alert-danger').addClass('hide');
        }, 2000)
    }


    $('.saveReasonsOfDelay').on('click', function (e) {
        if (!$('select[name="baseLate"]').val()) {
            showEmptyLateError($.i18n._('pvc.issueType.null'));
        } else {
            var reasonOfDelayModal = $("#reasonOfDelayModalId");
            _.each(ROD_FIELD_ENUM, function (item) {
                reasonOfDelayModal.find("." + item).attr("disabled", false);
            });
            var data = $("#reasonOfDelayModalForm").serializeArray();
            data.push({name: "currentRcaId", value: currentRcaId});
            data.push({name: "selectedIds", value: selectedIds});
            data.push({name: "dataType", value: dataType});
            data.push({name: "selectedCases", value: JSON.stringify(selectedCases)});
            showLoader();
            $.ajax({
                aysnc: false,
                method: 'POST',
                url: saveAllRcaForQualityCaseUrl,
                data: data,
                dataType: 'json'
            })
                .done(function (data) {
                    //updateIssueType(selectedIds, getPrimaryIssueTypeDetailMap(data));

                    if (typeof initCaseFormAssignedTo != "undefined") {
                        var selectedUser = $("#reasonOfDelayModalForm [name=assignedToUser]").val();
                        var selectedUserGroup = $("#reasonOfDelayModalForm [name=assignedToUserGroup]").val();
                        $('#case-form-assignedToUserGroup').attr("data-value", selectedUserGroup).val(selectedUserGroup)
                        $('#case-form-assignedToUser').attr("data-value", selectedUser).val(selectedUser)
                        initCaseFormAssignedTo();
                    }
                    var selectedWorkflow = $("#reasonOfDelayModalForm .workflow").find("option:selected").text();
                    $(".topWorkflow").attr("data-initial-state", selectedWorkflow).text(selectedWorkflow);
                    var errorMessage = data.errorMessage;
                    var errorMessageSpan = reasonOfDelayModal.find('.errorMessageSpan')
                    if (errorMessage != undefined && errorMessage.length > 0) {
                        errorMessageSpan.parent().removeClass('hide');
                        errorMessageSpan.html(errorMessage);
                        setTimeout(function () {
                            reasonOfDelayModal.find('.alert-danger').addClass('hide');
                        }, 15000);
                        if (nonEditableFields.length > 0) {
                            for (var m = 0; m < nonEditableFields.length; m++) {
                                reasonOfDelayModal.find('.' + nonEditableFields[m].name).attr("disabled", "disabled");
                            }
                        }
                        enableDisableDateFields(".editPreventativeAction", "preventiveDate", preventiveDate);
                        enableDisableDateFields(".editCorrectiveAction", "correctiveDate", correctiveDate);
                    } else if (errorMessage == undefined || errorMessage.length == 0) {
                        $("#reasonOfDelayModalId").modal('hide');
                        reloadHeaderData();
                        reloadRodTable($.i18n._('rootCause.update.success'));
                    }
                    hideLoader();
                })
                .fail(function (err) {
                    hideLoader();
                    var responseText = err.responseText;
                    var responseTextObj = JSON.parse(responseText);
                    var errorMessageSpan = reasonOfDelayModal.find('.errorMessageSpan')
                    if (responseTextObj.message != undefined) {
                        errorMessageSpan.parent().removeClass('hide');
                        errorMessageSpan.html(responseTextObj.message);
                    } else {
                        errorMessageSpan.parent().removeClass('hide');
                        errorMessageSpan.html("Failed due to some unknown reason!");
                    }
                    setTimeout(function () {
                        reasonOfDelayModal.find('.alert-danger').addClass('hide');
                    }, 2000)
                    if (nonEditableFields.length > 0) {
                        for (var m = 0; m < nonEditableFields.length; m++) {
                            reasonOfDelayModal.find('.' + nonEditableFields[m].name).attr("disabled", "disabled");
                        }
                    }
                    if (responseTextObj.errorRows && responseTextObj.errorRows.length > 0) {
                        $(".workflowButton").parent().css("border", '');
                        for (var i in responseTextObj.errorRows) {
                            $("button[data-quality-data-id='" + responseTextObj.errorRows[i] + "']").parent().css({"border": "1px solid red"});
                        }
                    }
                });
        }
    });

    function nvl(val) {
        if ((typeof val == 'undefined') || (val === null) || (val === "")) return EMPTY_LABEL;
        return val
    }

    $(document).on('change', ".editLate", function () {
        var el = $(this);
        if (!el.val() || el.val() == "") return;
        var rootCauseOption = "<option value='' > </option>";
        var late = _.find(JSON.parse(lateList), function (e) {
            return e.id == el.select2("val");
        });
        var rootCauseJson = JSON.parse(rootCauseList);
        for (var j = 0; j < rootCauseJson.length; j++) {
            if (_.indexOf(late.rootCauseIds, rootCauseJson[j]['id']) > -1) {
                if (rootCauseJson[j]['hiddenDate'] == null) {
                    rootCauseOption = rootCauseOption + "<option value='" + rootCauseJson[j]['id'] + "' >" + rootCauseJson[j]['textDesc'] + "</option>";
                }
            }
        }
        if (!rootCauseOption) rootCauseOption = "<option value='' > </option>";
        $("#reasonOfDelayModalForm").find("[name=late]").val(el.select2("val"));
        var editRootCause = $("#reasonOfDelayModalForm").find("select.editRootCause");
        editRootCause.select2('destroy');
        editRootCause.html(rootCauseOption);
        editRootCause.select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
        editRootCause.trigger("change");
    });

    $(document).on('change', ".editRootCause", function () {

        var el = $(this);
        var respPartyOption = "<option value='' > </option>";
        var respPartyJson = JSON.parse(responsiblePartyList);
        var rootCause = _.find(JSON.parse(rootCauseList), function (e) {
            return e.id == el.val()
        });
        if (rootCause) {
            for (var j = 0; j < respPartyJson.length; j++) {
                if (_.indexOf(rootCause.responsiblePartyIds, respPartyJson[j]['id']) > -1) {
                    if (respPartyJson[j]['hiddenDate'] == null) {
                        respPartyOption = respPartyOption + "<option value='" + respPartyJson[j]['id'] + "' >" + respPartyJson[j]['textDesc'] + "</option>";
                    }
                }
            }
        }
        if (!respPartyOption) respPartyOption = "<option value='' > </option>";
        var editResponsibleParty = el.closest("tr").find("select.editResponsibleParty")
        editResponsibleParty.select2('destroy');
        editResponsibleParty.html(respPartyOption);
        editResponsibleParty.select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
        editResponsibleParty.trigger("change")
    });

    $(".updateData").on("change", function () {
        $(this).closest("tr").find("input.isRowUpdated").val(true);
    });

    $(".backLink").on("click", function () {
        $("#backForm").attr("action", $(this).attr("data-href"));
        $("#backFormId").val($(this).attr("data-id"));
        $("#backFormFilter").val(sessionStorage.getItem("breadcrumbs_" + sectionId + "_" + $(this).attr("data-id")));
        $("#backForm").trigger('submit');
    });

    $('.table-add').on('click', function () {
        var reasonOfDelayModal = $("#reasonOfDelayModalId");
        var $row = createRow(0);
        $row.find("select").select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
        if (!$('select[name="baseLate"]').val()) {
            reasonOfDelayModal.find('.alert-danger').removeClass('hide');
            reasonOfDelayModal.find('.errorMessageSpan').html($.i18n._('qualityModule.issueType.null'));
            setTimeout(function () {
                reasonOfDelayModal.find('.alert-danger').addClass('hide');
            }, 2500)
        } else {
            $("#reasonOfDelayBody").append($row);
            if ($('select[name="baseLate"]').val()) {
                populateRootCauses($('select[name="baseLate"]').val());
            }
            updatePrimary();
            if (nonEditableFields.length > 0) {
                for (var m = 0; m < nonEditableFields.length; m++) {
                    $("#reasonOfDelayModalId").find('.' + nonEditableFields[m].name).attr("disabled", "disabled");
                    if (nonEditableFields[m].name == ROD_FIELD_ENUM.CORRECTIVE_DATE) correctiveDate = true;
                    if (nonEditableFields[m].name == ROD_FIELD_ENUM.PREVENTIVE_DATE) preventiveDate = true;
                }
            }
        }
        $row.find(".editPreventativeAction,.editCorrectiveAction").select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")}).trigger("change");
    });

    function populateRootCauses(baseLateValue) {
        var rootCauseOption = "<option value=''> </option>";
        var late = _.find(JSON.parse(lateList), function (e) {
            return e.id == baseLateValue;
        });
        var rootCauseJson = JSON.parse(rootCauseList);
        for (var j = 0; j < rootCauseJson.length; j++) {
            if (_.indexOf(late.rootCauseIds, rootCauseJson[j]['id']) > -1) {
                if (rootCauseJson[j]['hiddenDate'] == null) {
                    rootCauseOption = rootCauseOption + "<option value='" + rootCauseJson[j]['id'] + "' >" + rootCauseJson[j]['textDesc'] + "</option>";
                }
            }
        }
        if (!rootCauseOption) rootCauseOption = "<option value='' > </option>";
        var editRootCause = $("#reasonOfDelayModalForm").find("select.editRootCause").last();
        editRootCause.select2('destroy');
        editRootCause.html(rootCauseOption);
        editRootCause.select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
        editRootCause.trigger("change");
    }

    $(document).on('click', '.table-remove', function () {
        var tr = $(this).parents('tr');
        tr.detach();
        updatePrimary();

    });

    function updatePrimary() {
        if ($("#reasonOfDelayBody").find('tr:visible').find(".flagPrimaryRadio:checked").length == 0) {
            $("#reasonOfDelayBody").find('tr:visible:first').find(".flagPrimaryRadio").trigger('click');
        }
    }

    $(document).on("click", ".flagPrimaryRadio", function () {
        $(".flagPrimaryInput").val("false");
        $(this).closest("td").find(".flagPrimaryInput").val("true")
    });

    function createDefaultLateSelect(lateValue) {
        var lateJson = JSON.parse(lateList);
        var lateSelect = '<select class="editLate updateData form-control ' + ROD_FIELD_ENUM.ISSUE_TYPE + ' select2-box " name="baseLate"><option value="" >' + $.i18n._('selectOne') + '</option>';
        for (var i = 0; i < lateJson.length; i++) {
            if (lateJson[i]['id'] === lateValue) {
                lateSelect = lateSelect + '<option value="' + lateJson[i]["id"] + '" selected>' + lateJson[i]["textDesc"] + '</option>';
            } else if (lateJson[i]['hiddenDate'] == null) {
                lateSelect = lateSelect + '<option value="' + lateJson[i]["id"] + '" >' + lateJson[i]["textDesc"] + '</option>';
            }
        }
        lateSelect += "</select>";
        $(".lateSelectDiv").html(lateSelect);
        var el = $(".lateSelectDiv").find("select");
        el.select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
    }

    function createRow(pvcLcpId, primaryFlag, lateValue, rootCauseValue, responsiblePartyValue, correctiveActionValue, preventiveActionValue, correctiveDateValue, preventiveDateValue, investigation, summary, actions) {
        var modalBody = '<tr><td><span class="table-remove glyphicon glyphicon-remove"><input type="hidden" name="pvcLcpId" value="' + pvcLcpId + '"></td>';
        if (primaryFlag == "1") {
            modalBody = modalBody + '<td><input type="hidden" name="flagPrimary" class="flagPrimaryInput" value="true"><input type="radio" class="flagPrimaryRadio" name="flagPrimaryRadio" checked="checked"';
        } else {
            modalBody = modalBody + '<td><input type="hidden" name="flagPrimary" class="flagPrimaryInput" value="false"><input type="radio" value="false" class="flagPrimaryRadio" name="flagPrimaryRadio"';
        }
        modalBody = modalBody + '/></td><td>';
        var lateJson = JSON.parse(lateList);
        var rootCauseIds;
        var lateId;
        var lateSelect = '<select class="editLate updateData form-control ' + ROD_FIELD_ENUM.ISSUE_TYPE + ' select2-box " name="baseLate"><option value="">' + $.i18n._('selectOne') + '</option>'
        for (var i = 0; i < lateJson.length; i++) {
            if (i == 0) rootCauseIds = lateJson[i]['rootCauseIds'];
            if (lateJson[i]['id'] === lateValue) {
                rootCauseIds = lateJson[i]['rootCauseIds'];
                lateId = lateJson[i]["id"]
                lateSelect = lateSelect + '<option value="' + lateJson[i]["id"] + '" selected>' + lateJson[i]["textDesc"] + '</option>';
            } else if (lateJson[i]['hiddenDate'] == null) {
                lateSelect = lateSelect + '<option value="' + lateJson[i]["id"] + '" >' + lateJson[i]["textDesc"] + '</option>';
            }
        }
        lateSelect += "</select>"
        if (primaryFlag == "1") {
            $(".lateSelectDiv").html(lateSelect);
            $(".lateSelectDiv").find("select").select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
        }
        if ((pvcLcpId != 0) && !rootCauseValue && !responsiblePartyValue && !correctiveActionValue && !preventiveActionValue && !correctiveDateValue && !preventiveDateValue && !investigation && !summary && !actions) {
            return
        }

        modalBody = modalBody + '<input  type="hidden" value="' + lateId + '" name="late">';

        modalBody = modalBody + '<select class="editRootCause updateData ' + ROD_FIELD_ENUM.ROOT_CAUSE + ' col-md-12" name="rootCause"><option value="" > </option>';
        var selectHtml = ""
        var rootCauseJson = JSON.parse(rootCauseList);
        var responsiblePartyIds
        for (var i = 0; i < rootCauseJson.length; i++) {
            if (_.indexOf(rootCauseIds, rootCauseJson[i]['id']) > -1) {
                if (i == 0) responsiblePartyIds = lateJson[i]['rootCauseIds'];
                if (rootCauseJson[i]['id'] === rootCauseValue) {
                    responsiblePartyIds = rootCauseJson[i]['responsiblePartyIds'];
                    selectHtml += '<option value="' + rootCauseJson[i]["id"] + '" selected>' + rootCauseJson[i]["textDesc"] + '</option>';
                } else if (rootCauseJson[i]['hiddenDate'] == null) {
                    selectHtml += '<option value="' + rootCauseJson[i]["id"] + '" >' + rootCauseJson[i]["textDesc"] + '</option>';
                }
            }
        }
        if (!selectHtml) {
            responsiblePartyIds = [-1];
            selectHtml = "<option value='' > </option>";
        }
        modalBody = modalBody + selectHtml + '</select>';

        modalBody = modalBody + '<select class="editResponsibleParty updateData ' + ROD_FIELD_ENUM.RESP_PARTY + ' col-md-12" name="responsibleParty"><option value="" > </option>';
        var respPartyJson = JSON.parse(responsiblePartyList);
        selectHtml = "";
        for (var i = 0; i < respPartyJson.length; i++) {
            if (_.indexOf(responsiblePartyIds, respPartyJson[i]['id']) > -1) {
                if (respPartyJson[i]['id'] === responsiblePartyValue) {
                    selectHtml += '<option value="' + respPartyJson[i]["id"] + '" selected>' + respPartyJson[i]["textDesc"] + '</option>';
                } else if (respPartyJson[i]['hiddenDate'] == null) {
                    selectHtml += '<option value="' + respPartyJson[i]["id"] + '" >' + respPartyJson[i]["textDesc"] + '</option>';
                }

            }
        }
        if (!selectHtml) {
            selectHtml = "<option value='' > </option>";
        }
        modalBody = modalBody + selectHtml + '</select></td>';

        modalBody = modalBody + '<td><select class="editCorrectiveAction updateData ' + ROD_FIELD_ENUM.CORRECTIVE_ACTION + ' col-md-12" name="correctiveAction"><option value=\'\' ></option>';
        var correctiveActJson = JSON.parse(correctiveActionList);
        for (var i = 0; i < correctiveActJson.length; i++) {
            if (correctiveActJson[i]['id'] === correctiveActionValue) {
                modalBody = modalBody + "<option value='" + correctiveActJson[i]['id'] + "' selected>" + correctiveActJson[i]['textDesc'] + "</option>";
            } else {
                modalBody = modalBody + "<option value='" + correctiveActJson[i]['id'] + "' >" + correctiveActJson[i]['textDesc'] + "</option>";
            }
        }
        modalBody = modalBody + '</select>';

        modalBody = modalBody + '<select class="editPreventativeAction updateData ' + ROD_FIELD_ENUM.PREVENTIVE_ACTION + ' col-md-12" name="preventativeAction"><option value=\'\' ></option>';
        var preventiveActJson = JSON.parse(preventativeActionList);
        for (var i = 0; i < preventiveActJson.length; i++) {
            if (preventiveActJson[i]['id'] === preventiveActionValue) {
                modalBody = modalBody + "<option value='" + preventiveActJson[i]['id'] + "' selected>" + preventiveActJson[i]['textDesc'] + "</option>";
            } else {
                modalBody = modalBody + "<option value='" + preventiveActJson[i]['id'] + "' >" + preventiveActJson[i]['textDesc'] + "</option>";
            }
        }
        modalBody = modalBody + "</select></td>";
        modalBody = modalBody + "<td class='dateRow'></td>";
        modalBody = modalBody + "<td><textarea style='resize: auto;' rows='3' class= " + ROD_FIELD_ENUM.INVESTIGATION + " 'form-control' maxlength='32000' name='investigation'>" + (investigation ? investigation.replace(/"/gi, "&quot;").replace(/</gi, "&lt;").replace(/>/gi, "&gt;") : "") + "</textarea></td>" +
            "<td><textarea style='resize: auto;' class= " + ROD_FIELD_ENUM.SUMMARY + " 'form-control' rows='3' maxlength='32000' name='summary'>" + (summary ? summary.replace(/"/gi, "&quot;").replace(/</gi, "&lt;").replace(/>/gi, "&gt;") : "") + "</textarea></td>" +
            "<td><textarea style='resize: auto;' class= " + ROD_FIELD_ENUM.ACTIONS + " 'form-control' rows='3' maxlength='32000' name='actions'>" + (actions ? actions.replace(/"/gi, "&quot;").replace(/</gi, "&lt;").replace(/>/gi, "&gt;") : "") + "</textarea></td></tr>";

        var out = $(modalBody);
        var dateRow = out.find(".dateRow")
        dateRow.append(createDate("correctiveDate", ROD_FIELD_ENUM.CORRECTIVE_DATE));
        dateRow.append(createDate("preventiveDate", ROD_FIELD_ENUM.PREVENTIVE_DATE));
        out.find(".datepicker").datepicker({
            allowPastDates: true,
            momentConfig: {
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });
        if (correctiveDateValue)
            dateRow.find("[name=correctiveDate]").val(correctiveDateValue)
        if (preventiveDateValue)
            dateRow.find("[name=preventiveDate]").val(preventiveDateValue)
        out.find("textarea").each(function(){initTexareaRemainingChar($(this))})
        return out;
    }

    $(document).on("change", ".editPreventativeAction", function () {
        enableDisableDateFields(this, "preventiveDate", preventiveDate);
    });
    $(document).on("change", ".editCorrectiveAction", function () {
        enableDisableDateFields(this, "correctiveDate", correctiveDate);
    });

    function enableDisableDateFields(select, controlName, editable) {
        var control = $(select).closest("tr").find("[name=" + controlName + "]");
        var disable = ($(select).select2("val") == "") ? true : editable;
        var value = disable ? "" : (control.val() == "" ? moment().tz(userTimeZone).format(DEFAULT_DATE_DISPLAY_FORMAT) : control.val());
        control.attr("disabled", disable);
        control.val(value);
        control.parent().find('button').attr("disabled", disable);
    }

    function createDate(name, fieldName) {
        var div = $('<div class="fuelux "><div class="datepicker pastDateNotAllowed toolbarInline" >' +
            '<div class="input-group">' +
            '<input   class="form-control fuelux date ' + fieldName + '" name="' + name + '" value=""/>'
            + '</div></div></div>')
        div.find(".input-group").append($($(".dataPickerDropdownDiv")[0]).clone())
        return div;
    }

    $('[name=file]').on('change', function (evt, numFiles, label) {
        $("#file_name").val($.map($('[name=file]')[0].files, function (val) {
            return val.name;
        }).join(";"));
    });

    function getPrimaryIssueTypeDetailMap(primaryIssueTypeIdsMap) {
        var primaryIssueTypeDetailMap = [];
        primaryIssueTypeDetailMap['qualityIssueType'] = EMPTY_LABEL;
        primaryIssueTypeDetailMap['rootCause'] = EMPTY_LABEL;
        primaryIssueTypeDetailMap['responsibleParty'] = EMPTY_LABEL;
        var lateJson = JSON.parse(lateList);
        if (primaryIssueTypeIdsMap['issueTypeId']) {
            for (var i = 0; i < lateJson.length; i++) {
                if (lateJson[i]['id'] === primaryIssueTypeIdsMap['issueTypeId']) {
                    primaryIssueTypeDetailMap['qualityIssueType'] = lateJson[i]["textDesc"];
                }
            }
            var rootCauseJson = JSON.parse(rootCauseList);
            for (var i = 0; i < rootCauseJson.length; i++) {
                if (rootCauseJson[i]['id'] === primaryIssueTypeIdsMap['rootCauseId']) {
                    primaryIssueTypeDetailMap['rootCause'] = rootCauseJson[i]["textDesc"];
                }
            }
            var respPartyJson = JSON.parse(responsiblePartyList);
            for (var i = 0; i < respPartyJson.length; i++) {
                if (respPartyJson[i]['id'] === primaryIssueTypeIdsMap['responsiblePartyId']) {
                    primaryIssueTypeDetailMap['responsibleParty'] = respPartyJson[i]["textDesc"];
                }
            }
        }
        return primaryIssueTypeDetailMap;
    }

    $(document).on("change", "#reasonOfDelayModalForm .workflow", function () {
        if ($(this).val())
            $(".justificationDiv").show();
        else
            $(".justificationDiv").hide();
    });
}
