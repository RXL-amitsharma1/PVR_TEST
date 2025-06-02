var rptRequestId;
PVQ_TYPE_ENUM = {
  CASE_QUALITY: "CASE_QUALITY",
  SUBMISSION_QUALITY: "SUBMISSION_QUALITY",
  SAMPLING: "SAMPLING",
};

function showOpenActionItemsModal(
  actionUrl,
  errorMessage,
  tableId,
  parameters,
  rowId
) {
  $("#warningModal .description").text(errorMessage);
  $("#warningModal").modal("show");
  $("#warningButton")
    .off("click")
    .on("click", function () {
      confirmClosureOfReportRequest(actionUrl, parameters, rowId, tableId);
    });
}

var confirmClosureOfReportRequest = function (
  actionUrl,
  parameters,
  rowId,
  tableId
) {
  $.ajax({
    url: actionUrl,
    type: "post",
    data: parameters,
    dataType: "json",
    beforeSend: function () {
      $("#saveButton").attr("disabled", "disabled");
    },
  })
    .done(function (data) {
      var workflowStatusErrorAlert = $("#workflow-status-error-alert");
      workflowStatusErrorAlert.hide();
      workflowStatusErrorAlert.find(".alert-text").html('');
      $("#warningModal").modal("hide");
      $("#workflowStatusJustification").modal("hide");
      var cllRowId = $("#cllRowId").val();
      if (cllRowId != undefined && cllRowId != "") {
        reloadRodTable($.i18n._("wf.success"));
      } else if (tableId == "_page") {
        $("form").detach();
        location.reload();
      } else if (tableId == "_report") {
        updateReportState(data);
      } else {
        reloadData(rowId, true, tableId, data);
        if (location.pathname.indexOf("caseForm") == -1)
          successNotification(data.message, true);
      }
    })
    .fail(function (err) {
      var responseText = err.responseText;
      var responseTextObj = JSON.parse(responseText);
      hideLoader();
      var workflowStatusErrorAlert = $("#workflow-status-error-alert");
      if (responseTextObj.message != undefined) {
          workflowStatusErrorAlert.find(".alert-text").html(responseTextObj.message);
          workflowStatusErrorAlert.show();
      } else {
          workflowStatusErrorAlert.find(".alert-text").html($.i18n._('Error') + " : " + responseText);
          workflowStatusErrorAlert.show();
      }
      if (responseTextObj.errorRows && responseTextObj.errorRows.length > 0) {
        $(".workflowButton").parent().css("border", "");
        for (var i in responseTextObj.errorRows) {
          $(
            "button[data-case-id='" +
              responseTextObj.errorRows[i].masterCaseId +
              "']" +
              "[data-tenant-id='" +
              responseTextObj.errorRows[i].tenantId +
              "']" +
              "[data-processed-report-id='" +
              responseTextObj.errorRows[i].processedReportId +
              "']"
          )
            .parent()
            .css({ border: "1px solid red" });
          $(
            "button[data-quality-data-id='" +
              responseTextObj.errorRows[i] +
              "']"
          )
            .parent()
            .css({ border: "1px solid red" });
        }
      }
    })
    .always(function () {
      $("#saveButton").removeAttr("disabled");
    });
};
var confirmJustification = function (tableId, confirmJustificationURL) {
  showLoader();
  var wid = $("#workflowRuleId").val(
    $("#workflowSelect > option:selected").attr("ruleId")
  );
  var parameters = $("#justificationForm").serialize();
  parameters += "&password=" + encodeURIComponent($("#password-input").val());
  if (typeof selectedIds !== "undefined") {
    parameters += "&selectedJson=" + JSON.stringify(selectedIds);
  }
  if (
    typeof selectedCases !== "undefined" &&
    selectedCases &&
    selectedCases.length > 0
  ) {
    parameters += "&selectedCasesJson=" + JSON.stringify(selectedCases);
  }
  var rowId = $("#reportId").val();

  var currentConfirmJustificationURL = workflowJustificationConfirnUrl;
  if (confirmJustificationURL) {
    currentConfirmJustificationURL = confirmJustificationURL;
  }

  var errMessage = "";

  if (
    typeof isReportRequest !== "undefined" &&
    finalStates.includes($("#workflowSelect option:selected").text())
  ) {
    $.ajax({
      url: actionItemStatusForReportRequestUrl,
      data: { id: rptRequestId },
      dataType: "json",
    }).done(function (data) {
      if (data.actionItemStatus == "true") {
        errMessage = $.i18n._("reportRequest.actionItem.warning.label");
        showOpenActionItemsModal(
          currentConfirmJustificationURL,
          errMessage,
          tableId,
          parameters,
          rowId
        );
      } else {
        confirmClosureOfReportRequest(
          currentConfirmJustificationURL,
          parameters,
          rowId,
          tableId
        );
      }
      hideLoader();
    });
  } else {
    confirmClosureOfReportRequest(
      currentConfirmJustificationURL,
      parameters,
      rowId,
      tableId
    );
    hideLoader();
  }
};
var updateReportState = function (data) {
  successNotification(data.message);
  hideLoader();
  $(".workflowButton").text(data.worflowName);
  $(".workflowButton").attr("data-initial-state", data.worflowName);
    initAllowedActions(data.actions);
    if (data.action) {
        $("." + data.action).click();
    }
};
var reloadData = function (rowId, resetPagination, tableId, response) {
  if (resetPagination != true) {
    resetPagination = false;
  }
  if (!tableId || tableId == "") {
    hideLoader();
    $("#workflowStatusJustification").remove();
    location.reload();
    return;
  }
  if (tableId === "issuesTable") {
    $("form").detach();
    window.location.reload();
  } else {
    var dataTable = $("#" + tableId).DataTable();
    dataTable.ajax.reload(function () {
      //highlightRow(rowId);
      doAction(rowId, tableId, response.action)
    }, resetPagination);
  }
  hideLoader();
};

var highlightRow = function (rowId, tableId) {
  if (rowId != undefined && rowId != "") {
    var dataTable = $("#" + tableId).DataTable();
    dataTable
      .row("#" + rowId)
      .nodes()
      .to$()
      .addClass("flash-row");
  }
};

var doAction = function (rowId, tableId, actionClassName) {
  if (actionClassName) {
    var actionItem = $("#" + tableId).find("tr[id="+rowId+"]").find("." + actionClassName);
    if (actionItem.length === 0 && rowId !== null && actionClassName=="markAsSubmitted") {
            var url = periodicReportConfig.markAsSubmittedUrl + "?id=" + rowId;
            var tempLink = $('<a>', {
                role: 'menuitem',
                class: 'listMenuOptions markAsSubmitted',
                'data-toggle': 'modal',
                id: rowId,
                'data-target': '#reportSubmissionModal',
                href: '#',
                'data-url': url
            }).appendTo('body');

            setTimeout(function() {
                tempLink.trigger('click');
                tempLink.remove();
            }, 0);
        }
        actionItem.trigger("click");
  }
};

function closeJustificationModal() {
  var workflowStatusErrorAlert = $("#workflow-status-error-alert");
  workflowStatusErrorAlert.hide();
  workflowStatusErrorAlert.find(".alert-text").html('');
  $("div[id^='workflowStatusJustification']").modal("hide");
  $("#workflowSelect").html("");
}

function openStateHistoryModal(
  btn,
  workflowJustificationSpecialUrl,
  workflowJustificationConfirmUrl,
  tableId
) {
  $(btn).attr("disabled", "disabled");
  var statusObject = $(btn);
  var executedConfigId = statusObject.attr("data-executed-config-id");
  var dataType = statusObject.attr("data-dataType");
  var qualityDataId = statusObject.attr("data-quality-data-id");
  var reportRequestId = statusObject.attr("data-reportRequest-id");
  var caseId = statusObject.attr("data-case-id");
  var processReportId = statusObject.attr("data-processed-report-id");
  var tenantId = statusObject.attr("data-tenant-id");
  var cllRowId = statusObject.attr("data-cll-row-id");
  var senderId = statusObject.attr("data-sender-id");
  var versionNum = statusObject.attr("data-version-num");
  var publisherSectionId = statusObject.attr("data-publisherSection-id");
  var publisherDocumentId = statusObject.attr("data-publisherDocument-id");
  var publisherDocumentType = statusObject.attr("data-publisherDocument-type");
  if (typeof isReportRequest !== "undefined") {
    rptRequestId = reportRequestId;
  }
  if (publisherSectionId || publisherDocumentId) {
    $("#forAllDiv").show();
  }
  var oldState = statusObject.attr("data-initial-state");
  var justificationModal = $("#workflowStatusJustification");

  $("#workflowJustificationTable").find("tr:has(td)").remove();
  var currentWorkflowJustificationUrl = workflowJustificationUrl;
  if (workflowJustificationSpecialUrl) {
    currentWorkflowJustificationUrl = workflowJustificationSpecialUrl;
  }
  $.ajax({
    type: "GET",
    url: currentWorkflowJustificationUrl,
    data: {
      initialState: oldState,
      executedReportConfigurationId: executedConfigId,
      reportRequestId: reportRequestId,
      qualityData: qualityDataId,
      dataType: dataType,
      caseId: caseId,
      processReportId: processReportId,
      tenantId: tenantId,
      cllRowId: cllRowId,
      sectionId: publisherSectionId,
      reportId: publisherDocumentId,
      publisherDocumentType: publisherDocumentType,
      senderId: senderId,
      versionNum: versionNum,
    },
    dataType: "json",
  })
    .done(function (result) {
      var dropdown = $("#workflowSelect");
      var textArea = $(".description-wrapper #description");
      var actionItems = result.actionItems;
      var workflowStatusAiWarningAlert = $("#workflow-status-ai-warning-alert");
      if(actionItems && actionItems!=="null" && actionItems!=="0") {
          workflowStatusAiWarningAlert.find(".alert-text").html($.i18n._('workFlow.actionItem.warning'));
          workflowStatusAiWarningAlert.show();
      } else {
          workflowStatusAiWarningAlert.hide();
      }
      dropdown.empty();
      textArea.val("");
      var reportID = result.reportId;
      var fromState = result.currentState.id;
      var justifications = result.workflowJustificationList;
      var ruleList = result.stateList.rules;
      var needApproval = result.stateList.needApproval;
      if ($.isEmptyObject(ruleList)) {
        dropdown.attr("disabled", "disabled");
        dropdown.append("<option disabled selected>No next state</option>");
        $(".description-wrapper").hide();
        $("#saveButton").hide();
      } else {
        for (var ruleId in ruleList) {
          var state = ruleList[ruleId];
          var option = $("<option></option>");
          option.attr("ruleId", ruleId);
          option.attr("value", state.id);
          if (needApproval && needApproval[ruleId])
            option.attr("needApproval", needApproval[ruleId]);
          option.html(state.name);
          dropdown.append(option);
        }
        dropdown.find("option").first().attr("selected", "selected");
        dropdown.removeAttr("disabled");
        $(".description-wrapper").show();
        $("#saveButton").show();
      }
      $(dropdown).on("change", function () {
        var needApproval = $("#workflowSelect > option:selected").attr(
          "needApproval"
        );
        if (needApproval === "true") {
          $("#needApprovalDiv").show();
          $("#needApprovalDiv .password-input").val("");
        } else $("#needApprovalDiv").hide();
      });
      dropdown.trigger("change");
      justificationModal.modal("show");
      $("#reportId").val(executedConfigId);
      $("#wfReportRequestId").val(reportRequestId);
      $("#publisherSectionId").val(publisherSectionId);
      $("#publisherDocumentId").val(publisherDocumentId);
      $("#publisherDocumentType").val(publisherDocumentType);
      $("#fromState").val(fromState);
      justificationModal.find("#dataType").val(dataType);
      if (dataType === PVQ_TYPE_ENUM.CASE_QUALITY) {
        $("#qualityCaseDataId").val(qualityDataId);
      } else if (dataType === PVQ_TYPE_ENUM.SUBMISSION_QUALITY) {
        $("#qualitySubmissionId").val(qualityDataId);
      } else {
        $("#qualitySamplingId").val(qualityDataId);
      }
      $("#caseId").val(caseId);
      $("#processReportId").val(processReportId);
      $("#ddCllMetaDataTenantId").val(tenantId);
      $("#cllRowId").val(cllRowId);
      $("#inCaseId").val(caseId);
      $("#inMetaDataTenantId").val(tenantId);
      $("#inVersionNum").val(versionNum);
      $("#inSenderId").val(senderId);
      if (!qualityDataId && !caseId) {
        $("#assignmentHistroyRow").hide();
      }
      if ($.isEmptyObject(justifications)) {
        var tr =
          '<tr><td colspan="6" style="text-align: center">'+ $.i18n._("app.workflow.no.state.change") +'</td></tr>';
        $("#workflowJustificationTable").append(tr);
      } else {
        $.each(justifications, function (index, v) {
          v.justification = encodeToHTML(v.justification);
          var assignData;
          if (v.assignedToUser) {
            assignData = v.assignedToUser.username;
          } else if (v.assignedToUserGroup) {
            assignData = v.assignedToUserGroup.name;
          } else {
            assignData = $.i18n._("app.select.notassigned");
          }
          var tr =
            "<tr id=tr" +
            i +
            '><td style="text-align: center">' +
            v.fromState +
            "</td>" +
            '<td style="text-align: center">' +
            v.toState +
            "</td>" +
            '<td style="text-align: center">' +
            v.dateCreated +
            "</td>" +
            '<td style="text-align: center">' +
            v.routedBy +
            "</td>" +
            (qualityDataId || caseId
              ? '<td style="text-align: center">' + assignData + "</td>"
              : "") +
            '<td style="text-align: center;word-break: break-word">' +
            v.justification +
            "</td></tr>";
          $("#workflowJustificationTable").append(tr);
        });
      }
      if (workflowJustificationConfirmUrl) {
        justificationModal
          .find(".confirm-workflow-justification")
          .attr(
            "onclick",
            "confirmJustification('" +
              tableId +
              "','" +
              workflowJustificationConfirmUrl +
              "');"
          );
      }
    })
    .fail(function (err) {
      var responseText = err.responseText;
      var responseTextObj = JSON.parse(responseText);
      hideLoader();
      if (responseTextObj.message) {
        return;
      }
    })
    .always(function (data) {
      $(btn).removeAttr("disabled");
    });
}

$(function () {
  //preventing autofilling from browser password manager
  $(document).on("keyup paste cut", ".password-input", function () {
    var $this = $(this);
    if ($this.val() === "") {
      $this.attr("type", "text");
      var container = $("#password-input-div");
      container.html(
        '<input name="' +
          new Date().getTime() +
          '" id="password-input" class="form-control password-input" type="text">'
      );
      $(".password-input").focus();
    } else $this.attr("type", "password");
  });

    $(document).on("data-clk", function (event, elem) {
        var elemClkData = JSON.parse(elem.attributes["data-evt-clk"].value)
        const methodName = elemClkData.method;
        const params = elemClkData.params;
        switch (methodName) {
            case "closeJustificationModal":
                closeJustificationModal();
                break;
            case "openStateHistoryModal":
                if(params.length === 0) {
                    openStateHistoryModal(elem);
                }else {
                    openStateHistoryModal(...params);
                }
                break;
            case "confirmJustification":
                confirmJustification(...params);
                break;
        }
    });

});
