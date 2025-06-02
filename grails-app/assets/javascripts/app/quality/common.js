ACTION_ITEM_GROUP_STATE_ENUM = {
  WAITING: "WAITING",
  OVERDUE: "OVERDUE",
  CLOSED: "CLOSED",
};
BULK_UPDATE_MAX_ROWS = 500;
var commentUpdated = false;
var capaQualityObjId;
var isIssueTypeChanged = false;
var isRootCauseChanged = false;
var rootCauseIds;
var responsiblePartyIds;
var respPartyArray = [];
var rootCauseArray = [];
var issueTypeArray = [];
var userGroupArray = [];
var userArray = [];
var usersAndGroupArray = [];
var workFlowArray = [];
var priorityArray = [];
var selectAll;
var selectedAttachIds = [];
var capaInsId;
var recordsTotal = 0
var lastTableFilter;
var qualityDelayReasonRender = function (data, type, row) {
  return (
    '<span><a href="#" class="reasonOfDelayModalBtn" data-id="' +
    row.id +
    '" data-dataType="' +
    row.dataType +
    '" data-caseNumber="' +
    row.caseNumber +
    '" data-viewMode="' +
    row.isFinalState +
    '" ' +
    '<span><i class="fa fa-pencil pv-ic pv-ic-hover"' +
    'data-placement="left" ' +
    'style="font-size:17px; cursor: pointer" ' +
    'title="' +
    $.i18n._("app.quality.edit.rootCause") +
    '"></i></span></a></span>'
  );
};

var renderCommentsField = function (data, type, row) {
  var icon = "fa-comment-o qualityShowHideCellContent";
  if (data && data.length > 0) {
    var icon = "fa-commenting-o commentPopoverMessage showPopover ";
  } else {
    data = "";
  }

  return (
    '<div class="annotationPopover" ><i class=" fa ' +
    icon +
    ' addComment"' +
    'data-content="' +
    data +
    '" ' +
    'data-caseNumber="' +
    row.caseNumber +
    '" ' +
    'data-errorType="' +
    row.errorType +
    '" ' +
    "data-id=" +
    row.id +
    " " +
    'style="font-size:17px; cursor: pointer" ' +
    'data-placement="left" ' +
    'title="' +
    $.i18n._("app.caseList.comment") +
    '"></i></div>'
  );
};

function init_filter_data() {
  $.ajax({
    url: fetchFilterPanelDataUrl,
    data: {
      "dataType": $("#dataType").val(),
    },
  }).done(function (data) {
    errorTypeList = data.errorTypeList;
    var responsiblePartyList = data.responsiblePartyList;
    var rootCauseList = data.rootCauseList;
    var issueTypeList = data.issueList;
    var userGroupList = data.userGroupList;
    var userList = data.userList;
    var workflowStateList = data.workflowStateList;
    var qualityPriorityList = data.qualityPriorityList;
    var uniqueRespParty = [];
    for (var j = 0; j < responsiblePartyList.length; j++) {
      if (responsiblePartyList[j].id && !uniqueRespParty[$.trim(responsiblePartyList[j].id)]) {
        var respParty = {};
        respParty["key"] = $.trim(responsiblePartyList[j].id);
        respParty["value"] = $.trim(responsiblePartyList[j].textDesc);
        respPartyArray.push(respParty);
        uniqueRespParty[$.trim(responsiblePartyList[j].id)] = 1;
      }
    }
    var uniqueRootCause = [];
    for (var j = 0; j < rootCauseList.length; j++) {
      if (rootCauseList[j].id && !uniqueRootCause[$.trim(rootCauseList[j].id)]) {
        var rootCause = {};
        rootCause["key"] = $.trim(rootCauseList[j].id);
        rootCause["value"] = $.trim(rootCauseList[j].textDesc);
        rootCauseArray.push(rootCause);
        uniqueRootCause[$.trim(rootCauseList[j].id)] = 1;
      }
    }
    var uniqueIssueType = [];
    for (var j = 0; j < issueTypeList.length; j++) {
      if (issueTypeList[j].id && !uniqueIssueType[$.trim(issueTypeList[j].id)]) {
        var issueType = {};
        issueType["key"] = $.trim(issueTypeList[j].id);
        issueType["value"] = $.trim(issueTypeList[j].textDesc);
        issueTypeArray.push(issueType);
        uniqueIssueType[$.trim(issueTypeList[j].id)] = 1;
      }
    }
    var uniqueGroupList = [];
    for (var j = 0; j < userGroupList.length; j++) {
      if (userGroupList[j].id && !uniqueGroupList[$.trim(userGroupList[j].id)]) {
        var userGroup = {};
        userGroup["key"] = $.trim(userGroupList[j].id);
        userGroup["value"] = $.trim(userGroupList[j].fullName);
        userGroupArray.push(userGroup);
        usersAndGroupArray.push(userGroup);
        uniqueGroupList[$.trim(userGroupList[j].id)] = 1;
      }
    }
    var uniqueUserList = [];
    for (var j = 0; j < userList.length; j++) {
      if (userList[j].id && !uniqueUserList[$.trim(userList[j].id)]) {
        var user = {};
        user["key"] = $.trim(userList[j].id);
        user["value"] = $.trim(userList[j].fullName);
        userArray.push(user);
        usersAndGroupArray.push(user);
        uniqueUserList[$.trim(userList[j].id)] = 1;
      }
    }
    var uniqueWorkFlowList = [];
    for (var j = 0; j < workflowStateList.length; j++) {
      if (workflowStateList[j].id && !uniqueWorkFlowList[$.trim(workflowStateList[j].id)]) {
        var workFlow = {};
        workFlow["key"] = $.trim(workflowStateList[j].id);
        workFlow["value"] = $.trim(workflowStateList[j].name);
        workFlowArray.push(workFlow);
        uniqueWorkFlowList[$.trim(workflowStateList[j].id)] = 1;
      }
    }
    var uniquePriorityList = [];
    for (var j = 0; j < qualityPriorityList.length; j++) {
      if (qualityPriorityList[j] && !uniquePriorityList[$.trim(qualityPriorityList[j])]) {
        var priority = {};
        priority["key"] = $.trim(qualityPriorityList[j]);
        priority["value"] = $.trim(qualityPriorityList[j]);
        priorityArray.push(priority);
        uniquePriorityList[$.trim(qualityPriorityList[j].id)] = 1;
      }
    }
    init_table_filter();
    populateManualObservationPrioritySelect();
  });
}

function populateManualObservationPrioritySelect() {
  var prioritySelect = $("#priorityManualObservation");
  if (prioritySelect.length > 0) {
    prioritySelect.select2({
      placeholder: "Assign Priority",
      dropdownParent: $(document).find("#adHocAlertModal")
    });
    var placeHolder = new Option();
    prioritySelect.append(placeHolder);
    for (var optionIter in priorityArray) {
      var optionData = priorityArray[optionIter]["key"];
      var option = new Option(optionData);
      prioritySelect.append(option);
    }
  }
}

function addComment(caseNumber, caseVersion, errorType, content, id, viewmode) {
  var form = $("form[name=updateCaseNumberCommentForm]");
  if (form.length > 0) {
    form.parent().append(form.html());
    form.remove();
  }
  $(".commentErrorDiv").hide();
  $("#comments").val("");
  hideCommentForm();
  showComments(id, viewmode);
  $(".hideCommentForm").on("click", function () {
    hideCommentForm();
  });
  var dataType = $("#dataType").val();
  var confirmationModal = $("#addCaseNumberComment");
  confirmationModal.modal("show");
  if (viewmode === "true") {
    $("#addCaseNumberComment").find("div.btn-add-comment").hide();
  } else if (viewmode === "false") {
    $("#addCaseNumberComment").find("div.btn-add-comment").show();
  }
  confirmationModal
    .find(".saveComment")
    .off()
    .on("click", function () {
      commentUpdated = true;
      var addCommentButton = $(this);
      addCommentButton.attr("disabled", true);
      $.ajax({
        url: saveCommentsUrl,
        method: "POST",
        data: {
          ownerId: id,
          commentType: $("#commentDataType").val(),
          "comment.textData": $("textarea[name=comments]").val(),
          multipleIds: ((selectedIds && selectedIds.length > 0) ? selectedIds.join(",") : ""),
        },
        dataType: "json",
      })
        .done(function (result) {
          if (result.success) {
            showComments(id);
            $("textarea[name=comments]").val("");
            $(".saveComment").attr("disabled", true);
            table.draw();
          } else {
            if (result.errors != undefined) {
              $.each(result.errors, function (index, e) {
                var field = form.find('[name="' + e + '"]');
                if (field != undefined) {
                  field.parent().addClass("has-error");
                }
              });
            }
            if (form.find(".has-error").length == 0) {
              var errorMessage = confirmationModal.find("#errormessage");
              $(".commentErrorDiv").show();
              errorMessage.html(result.msg);
            }
          }
          addCommentButton.attr("disabled", false);
        })
        .fail(function () {
          alert("Sorry! System level error");
        });
    });
}

function showComments(id, viewmode) {
  var dataType = $("#dataType").val();
  $.ajax({
    url: fetchCommentsUrl,
    data: {
      ownerId: id,
      commentType: $("#commentDataType").val(),
    },
    method: "POST",
    dataType: "html",
  })
    .done(function (result) {
      $("#commentsList").html(result);
      if (viewmode === "true") {
        $("#commentsList").find(".delete").hide();
      } else {
        $("#commentsList").find(".delete").show();
        $("#commentsList")
          .find(".delete")
          .on("click", function (e) {
            deleteComment(id, $(this).data("id"));
          });
      }
    })
    .fail(function () {
      alert("Sorry! System level error");
    });
}

function deleteComment(id, commentId) {
  var dataType = $("#dataType").val();
  commentUpdated = true;
  $.ajax({
    url: deleteCommentsUrl,
    data: {
      ownerId: id,
      commentType: $("#commentDataType").val(),
      "comment.id": commentId,
      multipleIds: ((selectedIds && selectedIds.length > 0) ? selectedIds.join(",") : ""),
    },
    method: "POST",
    dataType: "json",
  })
    .done(function (result) {
      if (result.success) {
        showComments(id);
        table.draw();
      } else {
        alert(result.msg);
      }
    })
    .fail(function () {
      alert("Sorry! System level error");
    });
}

function saveComments(id) {
  var form = $("form[name=updateCaseNumberCommentForm]");
  $.ajax({
    url: saveCommentsUrl,
    method: "POST",
    data: form.serialize(),
    dataType: "json",
  })
    .done(function (result) {
      if (result.success) {
        showComments(id);
      } else {
        if (result.errors != undefined) {
          $.each(result.errors, function (index, e) {
            var field = form.find('[name="' + e + '"]');
            if (field != undefined) {
              field.parent().addClass("has-error");
            }
          });
        }
        if (form.find(".has-error").length == 0) {
          alert(result.msg);
        }
      }
    })
    .fail(function () {
      alert("Sorry! System level error");
    });
}

$("#commentErrorDiv").on("click", function () {
  $(".commentErrorDiv").hide();
});

function hideCommentForm() {
  var commentModal = $("#addCaseNumberComment");
  commentModal.find("div.add-comment-component").hide();
  commentModal.find("div.btn-add-comment").show();
  $("textarea[name=comments]").val("");
}

function showCommentForm() {
  var commentModal = $("#addCaseNumberComment");
  commentModal.find("div.add-comment-component").show();
  commentModal.find("div.btn-add-comment").hide();
  $(".saveComment").attr("disabled", true);
}

$(document).on("keyup change", "#comments", function () {
  if ($("#comments").val() != "") {
    $(".saveComment").attr("disabled", false);
  } else {
    $(".saveComment").attr("disabled", true);
  }
});

$(document).on("click", "#addCaseNumberComment .successMsg", function () {
  if (typeof commentUpdated !== "undefined" && commentUpdated)
    reloadRodTable($.i18n._("comment.success"));
});

function initActionItems() {
  $(document).on("click", ".createActionItem", function () {
    actionItem.actionItemModal.init_action_item_modal(
      false,
      "QUALITY_MODULE",
      this,
      selectedIds
    );
  });

  $(document).on("click", ".createCorrectivePreventiveAction", function () {
    actionItem.actionItemModal.init_action_item_modal(
      false,
      "QUALITY_MODULE",
      this,
      selectedIds
    );
  });

  //Click event bind to the action item view option.
  $(document).on("click", ".action-item-view", function () {
    var actionItemId = $(this).attr("data-value");
    actionItem.actionItemModal.view_action_item(
      actionItemId,
      "QUALITY_MODULE",
      this
    );
  });

  //Click event bind to the edit icon.
  $(document).on("click", ".action-item-edit", function () {
    var actionItemId = $(this).attr("data-value");
    actionItem.actionItemModal.edit_action_item(
      hasAccessOnActionItem,
      actionItemId,
      null,
      "QUALITY_MODULE",
      null,
      this
    );
  });
  var actionItemModalObj = $("#actionItemModal");

  //Bind the click event on update button click
  actionItemModalObj.find(".edit-action-item").on("click", function () {
    //Toggle the modal window buttons.
    actionItemModalObj.find("#creationScreenButton").addClass("hide");
    actionItemModalObj.find("#editScreenButton").removeClass("hide");
    actionItemModalObj.find("#viewScreenButton").addClass("hide");

    //Enable all the elements.
    actionItem.actionItemModal.toggle_element_disable(
      actionItemModalObj,
      false
    );

    //Enable the datepicker
    actionItemModalObj.find(".datepicker").datepicker("enable");
  });
}

function initComment() {
  $(".commentPopoverMessage").popover({
    trigger: "hover focus",
    html: true,
  });
  $(document).on("click", ".addComment", function (e) {
    e.preventDefault();
    var el = $(this);
    commentUpdated = false;
    addComment(
      el.attr("data-caseNumber"),
      el.attr("data-caseVersion"),
      el.attr("data-errorType"),
      el.attr("data-content"),
      el.attr("data-id"),
      el.attr("data-viewmode")
    );
  });
}

function initAnnotation() {
  $(".commentPopoverMessage").popover({
    trigger: "hover focus",
    html: true,
  });
  $(document).on("click", ".addAnnotation", function () {
    var confirmationModal = $("#addCaseNumberComment");
    confirmationModal.modal("show");
    confirmationModal
      .find(".add-comment-to-case")
      .off()
      .on("click", function () {
        var chartObj = $("#qualityChart").highcharts();
        chartObj.removeAnnotation("current");
        var annotationXValue = 0.9 * chartObj.chartWidth;
        var annotationYValue = 0.1 * chartObj.chartHeight;
        var annotationObj = {};
        var point = {};
        point.x = annotationXValue;
        point.y = annotationYValue;
        annotationObj.text = $("#comments").val();
        annotationObj.point = point;
        var styleObj = {};
        styleObj.width = 200;
        annotationObj.style = styleObj;
        var labels = [];
        labels.push(annotationObj);
        var labelOptions = {};
        labelOptions.backgroundColor = "rgba(236,236,236,1)";
        labelOptions.borderWidth = 0;
        labelOptions.borderRadius = 40;
        var annotationAddObj = {};
        annotationAddObj.labelOptions = labelOptions;
        annotationAddObj.id = "current";
        annotationAddObj.labels = labels;
        annotationAddObj.draggable = "xy";
        var styleObj = {};
        styleObj.width = 200;
        annotationObj.style = styleObj;
        chartObj.addAnnotation(annotationAddObj);
        confirmationModal.modal("hide");
      });
  });
}

function initCreateIssue() {
  $(document).on("click", ".createCapaIssue", function () {
    var create = true;
    if (create) {
      $("#errormessage").html("");
      var errorMessage = "";

      if (!$("#issueNumber").val().trim()) {
        $("#issueNumberDlgErrorDiv").show();
        $(".issueNumberBlankError").css("display", "block");
        $(".issueNumberUniqueError").css("display", "none");
        create = false;
      }

      if ($("#issueTypePopup").val().length > 255) {
        errorMessage += $.i18n._("issueType.maxSize.exceeded") + "<br>";
      }

      if ($("#categoryPopup").val().length > 255) {
        errorMessage += $.i18n._("category.maxSize.exceeded") + "<br>";
      }

      if ($("#remarksPopup").val().length > 255) {
        errorMessage += $.i18n._("remark.maxSize.exceeded") + "<br>";
      }

      if ($("#issueNumber").val().length > 200) {
        errorMessage += $.i18n._("issueNumber.maxSize.exceeded") + "<br>";
      }

      if ($("#rootCausePopup").val().length > 2000) {
        errorMessage += $.i18n._("rootCause.maxSize.exceeded") + "<br>";
      }

      if ($("#verificationResultsPopup").val().length > 2000) {
        errorMessage += $.i18n._("verification.maxSize.exceeded") + "<br>";
      }

      if ($("#commentsPopup").val().length > 2000) {
        errorMessage += $.i18n._("comments.maxSize.exceeded") + "<br>";
      }

      if (errorMessage !== "") {
        $(".issueErrorDiv").show();
        $("#errormessage").html(errorMessage);
        create = false;
      }
    }
    if (create) {
      var jForm = new FormData();
      jForm.append("issueNumber", $("#issueNumber").val());
      jForm.append("issueType", $("#issueTypePopup").val());
      jForm.append("category", $("#categoryPopup").val());
      jForm.append("remarks", $("#remarksPopup").val());
      jForm.append("approvedBy", $("#approvedByPopup").val());
      jForm.append("initiator", $("#initiatorPopup").val());
      jForm.append("teamLead", $("#teamLeadPopup").val());
      jForm.append(
        "teamMembers",
        $("#teamMembersPopup")
          .val()
          .filter(function (v) {
            return v !== "";
          })
          .toString()
      );
      jForm.append("description", $("#descriptionPopup").val());
      jForm.append("rootCause", $("#rootCausePopup").val());
      jForm.append("verificationResults", $("#verificationResultsPopup").val());
      jForm.append("comments", $("#commentsPopup").val());
      jForm.append("qualityDataType", $("#dataType").val());
      jForm.append("rowId", capaQualityObjId);
      jForm.append("selectedIds", JSON.stringify(selectedIds));
      $.ajax({
        url: createCapaUrl,
        async: false,
        type: "POST",
        data: jForm,
        mimeType: "multipart/form-data",
        contentType: false,
        cache: false,
        processData: false,
        dataType: "html",
      })
        .done(function (data) {
          if (data === "alreadyExist") {
            $("#issueNumberDlgErrorDiv").show();
            $(".issueNumberBlankError").css("display", "none");
            $(".issueNumberUniqueError").css("display", "block");
          } else {
            $("#createQualityIssue").modal("hide");
            reloadRodTable($.i18n._("capa.success"));
          }
        })
        .fail(function (data) {
          alert($.i18n._("Error") + " : " + data.responseText);
        });
    }
  });
  var issueTable = $("#issue-number").DataTable({
    pageLength: false,
    info: false,
    search: false,
    paging: false,
  });
  $(document).on("click", "#createIssue", function (e) {
    e.preventDefault();
    $("#attachmentList").html("");
    $("#icons").html("");
    $("#file_name1").val(null);
    $(".filename_attach").val(null);
    var viewMode = !editRole;
    capaQualityObjId = $(this).attr("data-id");
    $("#updateCapaIssue").hide();
    $("#updateAttach").hide();
    $("#createCapaIssue").show();
    $("#createAttach").show();
    $("#issueNumber").attr("readonly", false);
    $("#issue-number").DataTable().clear();
    var createQualityIssueModal = $("#createQualityIssue");
    $(createQualityIssueModal).on("shown.bs.modal", function () {
      setSelect2InputWidth($(this).find("#teamMembersPopup"));
      $(this).find("#approvedByPopup").select2({
        dropdownParent: this
      });
      $(this).find("#initiatorPopup").select2({
        dropdownParent: this
      });
      $(this).find("#teamLeadPopup").select2({
        dropdownParent: this
      });
    });
    createQualityIssueModal.find("textField, textArea").val("");
    createQualityIssueModal.find("select").val("").trigger("change");
    createQualityIssueModal
      .find("#issueNumber , #issueTypePopup , #categoryPopup , #remarksPopup")
      .val("");
    createQualityIssueModal.find(".createCapaIssue").attr("disabled", false);
    createQualityIssueModal.find("#createAttach").attr("disabled", true);
    createQualityIssueModal.find("#descSizeExceed").hide();
    createQualityIssueModal.find(".attachSizeExceed").hide();
    createQualityIssueModal.find(".issueErrorDiv").hide();
    $.ajax({
      data: {
        qualityDataType: $("#dataType").val(),
        rowId:capaQualityObjId
      },
      url: fetchIssueNumberCaseUrl,
      dataType: "json",
    })
      .done(function (data) {
        issueTable.clear();
        for (var i in data) {
          if (data[i] != null) {
            var issueNumLink =
              "<a href ='#' data-issueNumber='"+data[i]+"' data-evt-clk=\'{\"method\": \"fetchIssueData\", \"params\": []}\'>" +
              data[i] +
              "</a>";
            issueTable.row.add([issueNumLink]);
          }
        }
        issueTable.draw();
        $("#issue-number_wrapper").find(".dt-layout-row:first").hide();
        $("#issue-number_wrapper").find(".dt-layout-row:last").hide();
      })
      .fail(function (data) {
        alert($.i18n._("Error") + " : " + data.responseText);
      });
    $.ajax({
      url: getIssueNumberUrl,
      dataType: "html",
    })
      .done(function (data) {
        $("#createQualityIssue").find("#issueNumber").val(data);
        if (viewMode) {
          createQualityIssueModal
            .find("textField , select, textArea")
            .attr("disabled", true);
          $(".createCapaIssue").hide();
          createQualityIssueModal
            .find(
              "#issueNumber , #issueTypePopup , #categoryPopup , #remarksPopup"
            )
            .attr("readonly", "readonly");
        }
      })
      .fail(function (data) {
        alert($.i18n._("Error") + " : " + data.responseText);
      });
    capaQualityObjId = $(this).attr("data-id");
    $("#createQualityIssue").find(".alert-success").hide();
    $("#createQualityIssue").modal("show");
  });
  populateCapaUsersList();
}

function fetchIssueData(issueNumber) {
  if (issueNumber) {
    $("#issueNumber").val(issueNumber);
    $("#createCapaIssue").hide();
    $("#createAttach").hide();
    $("#updateCapaIssue").show();
    $("#updateAttach").show();
    $("#updateAttach").attr("disabled", true);
    $("#issueNumber").attr("readonly", "readonly");
  }
  if (!editRole) {
    $("#updateCapaIssue").hide();
    $("#updateAttach").hide();
  }
  $.ajax({
    url: fetchDataIssueUrl,
    data: {
      issueNumber: issueNumber,
    },
    dataType: "json",
  })
    .done(function (data) {
      capaInsId = data.capaData.id;
      $("#issueTypePopup").val(data.capaData.issueType);
      $("#categoryPopup").val(data.capaData.category);
      $("#remarksPopup").val(data.capaData.remarks);
      $("#approvedByPopup").val(data.capaData.approvedId).trigger("change");
      $("#initiatorPopup").val(data.capaData.initiatorId).trigger("change");
      $("#teamLeadPopup").val(data.capaData.teamLeadId).trigger("change");
      var teamMembersPopup = [];
      for (var i in data.capaData.teamMembers) {
        teamMembersPopup.push(data.capaData.teamMembers[i].id);
      }
      $("#teamMembersPopup")
        .val(teamMembersPopup)
        .trigger("change");
      $("#descriptionPopup").val(data.capaData.description);
      $("#rootCausePopup").val(data.capaData.rootCause);
      $("#verificationResultsPopup").val(data.capaData.verificationResults);
      $("#commentsPopup").val(data.capaData.comments);
      fetchAttachment(issueNumber);
    })
    .fail(function (data) {
      alert($.i18n._("Error") + " : " + data.responseText);
    });
}

function fetchAttachment(issueNumber) {
  $.ajax({
    url: fetchDataIssueUrl,
    data: {
      issueNumber: issueNumber,
    },
    dataType: "json",
  })
    .done(function (data) {
      var attachmentList = data.capaData.attachments;
      var iconhtml = "";
      var attachmentListHtml = "";
      var checkRow = 0;
      for (var i = 0; i < attachmentList.length; i++) {
        if (attachmentList[i].isDeleted == false) {
          iconhtml =
            '<div class="m-t-25">' +
            '<a href="javascript:void(0);" class="attachments">' +
            '<i class="glyphicon glyphicon-download-alt theme-color v-a-initial">' +
            "</i>" +
            "</a>" +
            '<a href="javascript:void(0);" class="removes">' +
            '<i class="md-lg md-close theme-color p-l-5 v-a-initial">' +
            "</i>" +
            "</a>" +
            "</div>";
          $("#icons").html(iconhtml);
          if (!editRole) {
            $("#icons").hide();
          }
          attachmentListHtml =
            attachmentListHtml +
            '<tr class="actionTableRow"> ' +
            '<td style="vertical-align: middle;text-align: left; min-width: 30px"> ' +
            "<div> " +
            '<input type="checkbox" _id=' +
            attachmentList[i].id +
            ' class="selectCheckbox1"  name="selected" /> ' +
            "</div> " +
            "</td> " +
            "<td>" +
            attachmentList[i].filename +
            "</td>" +
            "<td>" +
            attachmentList[i].createdBy +
            "</td>" +
            "<td>" +
            moment
              .utc(attachmentList[i].dateCreated)
              .tz(userTimeZone)
              .format(DEFAULT_DATE_DISPLAY_FORMAT) +
            "</td> " +
            "<td>" +
            '<a href="' +
            downloadAttachmentUrl +
            "?id=" +
            attachmentList[i].id +
            '">' +
            '<i id="saveButton" class="glyphicon glyphicon-download-alt theme-color"></i>' +
            "</a>" +
            '<a href="javascript:void(0)" class="remove" _attachid=' +
            attachmentList[i].id +
            " >" +
            '<i class="md-lg md-close theme-color v-a-initial p-l-5"></i>' +
            "</a>" +
            "</td> " +
            "</tr>";
        }
      }
      $("#attachmentList").html(attachmentListHtml);
    })
    .fail(function (data) {
      alert($.i18n._("Error") + " : " + data.responseText);
    });
  $(".filename_attach").val(null);
  $("#file_name1").val(null);
  $("#file_input_attach").val(null);
}

function removeAttachments(ids) {
  var deleteJustificationField = $("#deleteJustification");
  var deleteJustification = deleteJustificationField.val()
    ? deleteJustificationField.val().trim()
    : "";
  var deleteValidationAlert = $("#deleteDlgErrorDiv");
  if (!deleteJustification) {
    deleteValidationAlert.show();
  } else {
    deleteValidationAlert.hide();
    var issueNumber = $("#issueNumber").val().trim();
    $.ajax({
      type: "POST",
      async: false,
      url:
        removeIssueAttachmentsUrl +
        "?selectedIds=" +
        ids +
        "&selectAll=" +
        selectAll +
        "&deleteJustification=" +
        deleteJustification +
        "&issueNumber=" +
        issueNumber +
        (capaInsId ? "&capaId=" + capaInsId : ""),
      success: function () {
        $("#deleteModal").modal("hide");
        fetchAttachment(issueNumber);
        deleteJustificationField.val("");
      $("#successMessage").html($.i18n._('quality.attachment.removed'));
                $(".actionItemSuccess").show();
                setTimeout(function () {
                    $(".actionItemSuccess").hide();
                }, 3000);
            },
    });
  }
}

$(document).on("click", "#createAttach", function (e) {
  if (
    !validateAttachmentFileSize($("#file_input_attach").get(0).files[0].size)
  ) {
    return false;
  }
  $("#issueNumber").val().trim();
  var container = $(this).closest(".attachmnetcontainer");
  if (!validateAttachmentFileNameSize(container.find(".filename_attach"))) {
    return false;
  }
  var jForm = new FormData();
  if (container.find("#file_input_attach").val()) {
    jForm.append("file", container.find("#file_input_attach").get(0).files[0]);
    jForm.append("filename_attach", container.find(".filename_attach").val());
  }
  jForm.append("issueNumber", $("#issueNumber").val());
  $.ajax({
    url: createCapaAttachmentUrl,
    async: false,
    type: "POST",
    data: jForm,
    mimeType: "multipart/form-data",
    contentType: false,
    cache: false,
    processData: false
  })
    .done(function (data) {
      if (data == "success") {
        fetchAttachment($("#issueNumber").val());
      }
    })
    .fail(function (data) {
      showResponseErrorModal(data);
    });
  $(".attachment-button").attr("disabled", true);
});

function validateAttachmentFileSize(size) {
  if (size > AttachmentSizeLimit) {
    $(".attachSizeExceed").show();
    $("#message").html($.i18n._("issue.Attachment.data.maxSize"));
    return false;
  }
  $(".attachSizeExceed").hide();
  return true;
}

function validateAttachmentFileNameSize(AttachmentFileContainer) {
  if (AttachmentFileContainer.val().length > 250) {
    $(".attachSizeExceed").show();
    $("#message").html($.i18n._("issue.Attachment.file.name.maxSize"));
    return false;
  }
  $(".attachSizeExceed").hide();
  return true;
}

$("#attachSizeExceed").on("click", function () {
  $(".attachSizeExceed").hide();
});

$("#issueErrorDiv").on("click", function () {
  $(".issueErrorDiv").hide();
});

$(document).on("click", "#updateAttach", function (e) {
  if (
    !validateAttachmentFileSize($("#file_input_attach").get(0).files[0].size)
  ) {
    return false;
  }
  $("#issueNumber").val().trim();
  var container = $(this).closest(".attachmnetcontainer");
  var jForm = new FormData();
  if (container.find("#file_input_attach").val()) {
    jForm.append("file", container.find("#file_input_attach").get(0).files[0]);
    jForm.append("filename_attach", container.find(".filename_attach").val());
  }
  jForm.append("issueNumber", $("#issueNumber").val());
  $.ajax({
    url: updateCapaAttachmentUrl,
    async: false,
    type: "POST",
    data: jForm,
    mimeType: "multipart/form-data",
    contentType: false,
    cache: false,
    processData: false,
    dataType: "text",
  })
    .done(function (data) {
      if (data == "success") {
        fetchAttachment($("#issueNumber").val());
      }
    })
    .fail(function (data) {
       showResponseErrorModal(data);
    });
});

function showResponseErrorModal(responseData) {
    var errorMessage = $.i18n._('Error') + ': ' + responseData.responseText;
    var errorModal = $("#errorModal");
    if (errorModal.length > 0) {
        errorModal.find('.description').html(errorMessage);
        errorModal.modal('show');
    } else {
        alert(errorMessage);
    }
}

$(document).on("change", "#file_input_attach", function () {
  $(".btn").removeAttr("disabled");
  $("#file_name1").val($("#file_input_attach").get(0).files[0].name);
});
$(document)
  .on("click", ".selectAllCheckbox1", function (e) {
    if ($(".selectAllCheckbox1").is(":checked")) {
      $(".selectCheckbox1").prop("checked", true);
    }
  })
  .on("click", "input.selectAllCheckbox1", function (e) {
    if ($(this).is(":checked")) {
      selectAll = true;
      $(".selectCheckbox1").prop("checked", true);
    } else {
      selectAll = false;
      $(".selectCheckbox1").prop("checked", false);
    }
  });

$(document).on("change", "input.selectCheckbox1", function () {
  var isChecked = $(this).prop("checked");
  var id = $(this).attr("_id");
  var idx = findIdx(selectedAttachIds, id);
  if (isChecked) {
    if (idx < 0) {
      if (id) {
        selectedAttachIds.push(id);
      }
    }
  } else {
    if (idx >= 0) {
      selectedAttachIds.splice(idx, 1);
    }
    if (selectAll == true) {
      $("#selectAll1").prop("checked", false);
      selectAll = false;
    }
  }

});

$(document).on("click", ".attachments", function () {
  if (selectedAttachIds.length > 0 || selectAll) {
    $("#selectAll1").val(selectAll);
    $("#selectedIds").val(selectedAttachIds);
    $("#capaInstanceId").val(capaInsId);
    var formId = $("#attachForm");
    formId.attr("action", downloadAllAttachmentUrl);
    formId.trigger("submit");
  } else {
    $("#warningModal .description").text(
      $.i18n._("qualityModule.capa.attachment.download")
    );
    $("#warningModal").modal("show");
    $("#warningButton")
      .off("click")
      .on("click", function () {
        $("#warningModal").modal("hide");
      });
  }
});

$(document).on("click", ".removes", function () {
  showLoader();
  if (selectedAttachIds.length > 0 || selectAll) {
    var modal = $("#deleteModal");
    modal.modal("show");
    $("#deleteDlgErrorDiv").hide();
    modal
      .find("#deleteModalLabel")
      .text($.i18n._("modal.delete.title", $.i18n._("app.label.attachment")));
    modal.find("#nameToDelete").text($.i18n._("deleteAllAttachments"));
    $("#deleteButton").off();
    hideModalLoader($("#createRodIssue").modal());
    $("#deleteButton").on("click", function () {
      removeAttachments(selectedAttachIds);
    });
  } else {
    $("#warningModal .description").text(
      $.i18n._("qualityModule.capa.attachment.delete")
    );
    $("#warningModal").modal("show");
    hideModalLoader($("#createRodIssue").modal());
    $("#warningButton")
      .off("click")
      .on("click", function () {
        $("#warningModal").modal("hide");
      });
  }
});

$(document).on("click", ".remove", function () {
  var attachId = $(this).attr("_attachid");
  showLoader();
  var modal = $("#deleteModal");
  modal.modal("show");
  $("#deleteDlgErrorDiv").hide();
  modal
    .find("#deleteModalLabel")
    .text($.i18n._("modal.delete.title", $.i18n._("app.label.attachment")));
  modal
    .find("#nameToDelete")
    .text($.i18n._("deleteThis", $.i18n._("app.label.attachment")));
  $("#deleteButton").off();
  hideModalLoader($("#createRodIssue").modal());
  $("#deleteButton").on("click", function () {
    removeAttachments(attachId);
  });
});

function findIdx(arItems, searchValue) {
  var found = false;
  var return_idx = -1;
  for (var i = 0; i < arItems.length && !found; i++) {
    if (arItems[i] == searchValue) {
      found = true;
    }
  }
  if (found) {
    return_idx = i - 1;
  } else {
    return_idx = -1;
  }
  return return_idx;
}

$(document).on("click", ".updateCapaIssue", function () {
  $("#issueNumber").val().trim();
  var jForm = new FormData();
  jForm.append("issueNumber", $("#issueNumber").val());
  jForm.append("issueType", $("#issueTypePopup").val());
  jForm.append("category", $("#categoryPopup").val());
  jForm.append("remarks", $("#remarksPopup").val());
  jForm.append("approvedBy", $("#approvedByPopup").val());
  jForm.append("initiator", $("#initiatorPopup").val());
  jForm.append("teamLead", $("#teamLeadPopup").val());
  jForm.append("teamMembers", $("#teamMembersPopup").val().toString());
  jForm.append("description", $("#descriptionPopup").val());
  jForm.append("rootCause", $("#rootCausePopup").val());
  jForm.append("verificationResults", $("#verificationResultsPopup").val());
  jForm.append("comments", $("#commentsPopup").val());
  jForm.append("qualityDataType", $("#dataType").val());
  jForm.append("rowId", capaQualityObjId);
  $.ajax({
    url: updateQualityCapaUrl,
    async: false,
    type: "POST",
    data: jForm,
    mimeType: "multipart/form-data",
    contentType: false,
    cache: false,
    processData: false,
  })
    .done(function (data) {
      $("#createQualityIssue").modal("hide");
      reloadRodTable($.i18n._("capa.update.success"));
    })
    .fail(function (data) {
      alert($.i18n._("Error") + " : " + data.responseText);
    });
});

function initViewCriteria() {
  $(document).on("click", "#viewCriteria", function () {
    var reportId = $(this).data("reportid");
    var criteriaUrl = "../report/criteria/";
    window.open(criteriaUrl + reportId, "_blank");
  });
}

function initViewCriteriaForManualError() {
  $(document).on("click", "#viewCriteriaForManualError", function () {
    var el = $(this);
    var caseNum = el.attr("data-caseNumber");
    var id = el.attr("data-id");
    fetchQualityObservation(id, caseNum);
  });
}

function exportToExcel() {
    var data = createParametersExcelEmail();
    if ((selectedIds.length === 0) && recordsTotal > 1000) {
        $('#exportWarning .description').show();
        $('#exportWarning #warningType').hide();
        $('#exportWarning').modal('show');
        $('#exportWarningOkButton').off('click').click(function () {
            $('#exportWarning').modal('hide');
            var jForm = new FormData();
            jForm.append("data", JSON.stringify(data))
            jForm.append("dataType", $("#dataType").val())
            jForm.append("async", true)
            $.ajax({
              url: $("#excelExport").attr("action"),
              type: "POST",
              data: jForm,
              mimeType: "multipart/form-data",
              contentType: false,
              cache: false,
              processData: false,
            }).done(function (data) {
                successNotification($.i18n._('lateProcessing.export'), true)
            });

        }).show();
    } else {
        $("#excelData").val(JSON.stringify(data));
        $("#excelExport").submit();
    }
}

function createParametersExcelEmail() {
  var data = {};

  if (selectAll == true) {
    var d = {};
    setExternalSearchCriteria(d);
    data["selectAll"] = "true";
    if (d.search) {
      data["search"] = JSON.stringify(d.search);
    }
    if (d.advanceFilter) {
      data["advanceFilter"] = JSON.stringify(d.advanceFilter);
    }
  } else {
    data["selectAll"] = "false";
    data["selectedIds"] = selectedIds;
  }
  return data;
}

function createCaseData() {
  if (!validateShareTemplateEmails()) {
    return false;
  }
  var data = createParametersExcelEmail();
  $("#casesData").val(JSON.stringify(data));
  return true;
}

function validateShareTemplateEmails() {
  var emailRequiredAlert = $("#email-required-alert");
  emailRequiredAlert.hide();
  if (!$("#emailUsers").val()) {
    emailRequiredAlert.show();
    return false;
  }
  return true;
}

function toTable(res) {
  var out = "<table><tr>";
  for (var i in res.metadata.columns) {
    out += "<td><b>" + res.metadata.columns[i].title + "</b></td>";
  }
  out += "</tr>";
  for (var i in res.data) {
    out += "<tr>";
    for (var j in res.data[i]) {
      out += "<td>" + res.data[i][j] + "</td>";
    }
    out += "</tr>";
  }
  out += "</table>";
  return out;
}

function updateQualityIssueTypeValue(e) {
  var valueText = "";
  if (e.val() == "-1") {
    valueText = "-1";
  } else {
    var qualtyIssues = JSON.parse(issuesList);
    for (var i = 0; i < qualtyIssues.length; i++) {
      if (qualtyIssues[i]["id"] == e.val()) {
        valueText = qualtyIssues[i]["textDesc"];
      }
    }
  }
  if (valueText.length > 0) {
    $.ajax({
      url: updateQualityIssueTypeUrl,
      data: {
        id: e.data("id"),
        caseNumber: $(this).data("case-number"),
        errorType: $(this).data("error-type"),
        value: valueText,
        dataType: $(this).attr("data-dataType"),
      },
      dataType: "json",
    }).fail(function (data) {
      alert($.i18n._("Error") + " : " + data.responseText);
    });
  }
}

$(document).on("click", ".deleteErrors, .deleteErrorsCaseForm", function () {
  if (selectedIds && selectedIds.length > 0) {
    var modal = $("#deleteModal");
    modal.modal("show");
    $("#deleteDlgErrorDiv").hide();
    modal
      .find("#deleteModalLabel")
      .text($.i18n._("modal.delete.title", $.i18n._("cases")));
    modal
      .find("#nameToDelete")
      .text($.i18n._("deleteThese", $.i18n._("cases")));
    $("#deleteButton").off();
    var data;
    if ($(this).hasClass("deleteErrorsCaseForm")) {
      data = { "selectedIds[]": selectedIds.join(";") };
    } else if ($(this).hasClass("deleteErrors")) {
      data = selectAll
        ? { selectAll: "true" }
        : { "selectedIds[]": selectedIds.join(";") };
      setExternalSearchCriteria(data);
      if (data.search) {
        data["search"] = JSON.stringify(data.search);
      }
      if (data.advanceFilter) {
        data["advanceFilter"] = JSON.stringify(data.advanceFilter);
      }
    } else {
      data = createParametersExcelEmail();
    }
    showLoader();
    $.ajax({
      url: deleteFieldCaseMsgUrl,
      method: "POST",
      data: data,
      dataType: "json",
    })
      .done(function (data) {
        if (data == "true") {
          modal
            .find("#displayFieldLevelCaseMsg")
            .text($.i18n._("fieldLevelMsg"));
        } else {
          modal.find("#displayFieldLevelCaseMsg").text("");
        }
        hideLoader();
      })
      .fail(function (data) {
        alert($.i18n._("Error") + " : " + data.responseText);
        hideLoader();
      });

    $("#deleteButton").on("click", function () {
      if (!$("#deleteJustification").val().trim()) {
        $("#deleteDlgErrorDiv").show();
      } else {
        $("#deleteDlgErrorDiv").hide();
        showLoader();
        data.justification = $("#deleteJustification").val();
        $.ajax({
          url: deleteCasesUrl,
          method: "POST",
          data: data,
        })
          .done(function (response) {
            reloadPageAndShowResponseAlerts(response, null, function () {
              location.reload(true);
            });
          })
          .fail(function (data) {
            alert($.i18n._("Error") + " : " + data.responseText);
          });
      }
    });
  } else {
    $("#errorExportModal")
      .find(".messageBody")
      .html($.i18n._("qualityModule.deleteCase.null"));
    $("#errorExportModal").modal("show");
  }
});

function populateCapaUsersList(option) {
  $.ajax({
    url: fetchUsersUrl,
    dataType: "json",
  })
    .done(function (data) {
      var capaUsersSelect = $("optgroup.capaUsers");
      for (var optionIter in data) {
        var optionData = data[optionIter];
        var option = new Option(optionData.fullName, optionData.userId);
        capaUsersSelect.append(option);
      }
    })
    .fail(function (data) {
      console.log("Error", data);
    });
}

$(function () {
  //initSelect2ForEmailUsers("#emailUsers");
  $(document).on("click", ".excelExport", function (e) {
    exportToExcel();
  });
  $("#copyCaseNumModal").on("show.bs.modal", function (e) {
    initCaseNumModal();
  });
  $(document).on("click", ".actionItemModalIcon", function (e) {
    showActionItemsModal($(this));
  });

  initViewSourceDocuments();

  $("#workflowSelect").select2();

  $(document).on("change", ".editIssueType", function () {
    var row = $(this).closest("tr");
    var el = $(this);
    isIssueTypeChanged = true;
    /*
            Update issueType in database
         */
    updateQualityIssueTypeValue(el);
    var validRootCauseIds = ["-1"];
    if (el.val() != "-1") {
      var late = _.find(JSON.parse(issuesList), function (e) {
        return e.id == el.val();
      });
      validRootCauseIds = late.rootCauseIds;
    }
    var rootCauseJson = JSON.parse(rootCauseList);
    var rootCauseOptionSelect = "";
    var rootCauseOption =
      rootCauseOption +
      "<option value='-1'>" +
      "Select Root Cause" +
      "</option>";
    for (var j = 0; j < rootCauseJson.length; j++) {
      if (_.indexOf(validRootCauseIds, rootCauseJson[j]["id"]) > -1) {
        if (rootCauseJson[j]["hiddenDate"] == null) {
          rootCauseOption =
            rootCauseOption +
            "<option value='" +
            rootCauseJson[j]["id"] +
            "' >" +
            rootCauseJson[j]["textDesc"] +
            "</option>";
        }
      }
    }
    row.find(".editIssueType").val(el.val());
    row
      .find(".editRootCause")
      .html(rootCauseOptionSelect + rootCauseOption + "")
      .trigger("change");
    row.find(".select2-box").attr("placeholder", "Select Root Cause");
    row.find(".select2-box").select2({ allowClear: true });
    /*
            Set Responsible Party select box to empty as user needs
            to select any root cause to populate it again
         */
    var responsiblePartyOption =
      "<option value='-1'>" + "Select Responsible Party" + "</option>";
    row
      .find(".editResponsibleParty")
      .html(responsiblePartyOption)
      .trigger("change");
    row.find(".select2-box").select2({ allowClear: true });

    isIssueTypeChanged = false;
  });

  $(document).on("change", ".editRootCause", function () {
    /*
            If this on-change event is generated due to change in this select box and
            is not generated due to updating of rootCause select box in
            issueType on-change event method
        */
    if (!isIssueTypeChanged) {
      var row = $(this).closest("tr"); // get the row
      var el = $(this);
      /*
                Update rootCause in database
             */
      //updateRootCauseValue(el);
      var validResponsiblePartyIds = ["-1"];
      if (el.val() != "-1") {
        var selectedRootCause = _.find(JSON.parse(rootCauseList), function (e) {
          return e.id == el.val();
        });
        if (selectedRootCause) {
          validResponsiblePartyIds = selectedRootCause.responsiblePartyIds;
        }
      }
      isRootCauseChanged = true;
      var responsibleParties = JSON.parse(responsiblePartyList);
      var rootCause = _.find(JSON.parse(rootCauseList), function (e) {
        return e.id == el.val();
      });
      var responsiblePartyOption = "<option value='' > </option>";
      for (var j = 0; j < responsibleParties.length; j++) {
        if (
          _.indexOf(validResponsiblePartyIds, responsibleParties[j]["id"]) > -1
        ) {
          if (responsibleParties[j]["hiddenDate"] == null) {
            responsiblePartyOption =
              responsiblePartyOption +
              "<option value='" +
              responsibleParties[j]["id"] +
              "' >" +
              responsibleParties[j]["textDesc"] +
              "</option>";
          }
        }
      }
      row.find(".editRootCause").val(el.val());
      row
        .find(".editResponsibleParty")
        .html(responsiblePartyOption)
        .trigger("change");
      row.find(".select2-box").select2({ allowClear: true });
      isRootCauseChanged = false;
    }
  });

  $(document).on("change", ".editResponsibleParty", function () {
    /*
            If this on-change event is generated due to change in this select box
            and not generated due to updating of responsibleParty select box in
            issueType & rootCause on-change event methods
         */
    if (!isIssueTypeChanged && !isRootCauseChanged) {
      var row = $(this).closest("tr"); // get the row
      var el = $(this);
      //updateResponsiblePartyValue(el);
      row.find(".editResponsibleParty").val(el.val());
    }
  });

  $(document).on("click", ".rptField-select2", function () {
    var field = $(this);
    $(field).select2({
      minimumInputLength: 0,
      separator: ";",
      multiple: true,
      closeOnSelect: false,
      ajax: {
        dataType: "json",
        url: possibleValuesUrl,
        data: function (params) {
          return {
            field: field.data("name"),
            term: params.term,
            lang: userLocale,
            page: params.page,
            max: 30,
          };
        },
        processResults: function (data) {
          var isMore = 30 == data.length;
          return {
            results: data,
            pagination: {
              more: isMore,
            },
          };
        },
      },
    });
  });

  $(document).on("data-clk", function (event, elem) {
    const elemClkData = JSON.parse(elem.attributes["data-evt-clk"].value)
    const methodName = elemClkData.method;
    const params = elemClkData.params;

    if (methodName == "showCommentForm") {
      // Call the method from the eventHandlers object with the params
      showCommentForm();
    } else if(methodName == "fetchIssueData") {
      var issueNumber = elem.attributes["data-issueNumber"].value;
      fetchIssueData(issueNumber);
    }
  });

  $("[data-evt-sbt]").on('submit', function() {
    const eventData = JSON.parse($(this).attr("data-evt-sbt"));
    const methodName = eventData.method;
    const params = eventData.params;
    // Call the method from the eventHandlers object with the params
    if (methodName == 'createCaseData') {
      return createCaseData();
    }
  });
});

var initViewSourceDocuments = function () {
  $(document).on("click", ".viewSourceDocuments", function (e) {
    $.ajax({
      url:
        fetchSourceDocumentsUrl +
        "?caseNumber=" +
        $(this).attr("data-caseNumber") +
        "?versionNumber=" +
          $(this).attr("data-versionNumber"),
      dataType: "json",
    })
      .done(function (data) {
        var out = "";
        if (!data || data.length == 0) out = $.i18n._("qualityModule.noFiles");
        else
          for (var i in data) {
            out +=
              "<a href='" +
              downloadSourceDocumentsUrl +
              "?id=" +
              encodeURIComponent(data[i].id) +
              "&caseNumber=" +
              $(this).attr("data-caseNumber") +
              "&caseVersion=" +
              $(this).attr("data-caseVersion") +
              "'><span class='fa fa-download'></span></a> " +
              data[i].fileName +
              " (" +
              data[i].notes +
              ")<br>";
          }
        $("#sourceDocumentsList").html(out);
        $("#sourceDocumentsModal").modal("show");
      })
      .fail(function (data) {
        console.log("Error", data);
      });
  });
};

var initExpandHandler = function (table) {
  var format = function (d) {
    if (_.isUndefined(d)) return "";

    var additionalData = d["additionalDetails"];
    if (
      _.isUndefined(additionalData) ||
      _.isNull(additionalData) ||
      _.isEmpty(additionalData)
    )
      return "";

    var narrative = "";

    var dataPairArray = additionalData.split("!@@\n##!");
    var preparedAdditionalData = {};
    _.each(dataPairArray, function (pairString) {
      var idx = pairString.indexOf(":");
      if (idx !== -1) {
        preparedAdditionalData[pairString.substr(0, idx)] = pairString.substr(
          idx + 1
        );
      } else {
        preparedAdditionalData[pairString] = "";
      }
    });

    var format_additional_data = function (add_data) {
      if (add_data === 0) {
        return "<tr><td colspan='6'></td></tr>";
      } else {
        var pairs = _.pairs(add_data);
        var len = _.size(pairs) + 3 - (_.size(pairs) % 3);
        var output = "<tr>";

        for (var i = 1; i <= len; i++) {
          if (i % 3 === 1 && i != 1 && i != len) {
            output += "</tr><tr>";
          }

          if (i <= pairs.length) {
            output +=
              '<td class="as-bold" style="font-weight: bold;width: 15%;"><b>' +
              pairs[i - 1][0] +
              ':</b></td><td style="width: 18%;">' +
              replaceAndFormatRows(pairs[i - 1][1]) +
              "</td>";
          } else {
            output += "<td></td><td></td>";
          }

          if (i == len) {
            output += "</tr>";
          }
        }

        return output;
      }
    };

    narrative = preparedAdditionalData["Case Narrative"];
    delete preparedAdditionalData["Case Narrative"];

    var content =
      '<table cellpadding="6" cellspacing="0" border="0" style="padding-left:50px;max-width: 80%; width: 80%;">' +
      format_additional_data(preparedAdditionalData);

    if (
      !_.isUndefined(narrative) &&
      !_.isNull(narrative) &&
      !_.isEmpty(narrative)
    ) {
      content +=
        '<tr><td class="as-bold" colspan="6" style="font-weight: bolder;">Narrative</td></tr>' +
        '<tr><td colspan="6">' +
        replaceAndFormatRows(narrative) +
        "</td></tr>";
    }

    content += "</table>";
    return content;
  };

  $(document).on("click", "#selectAll", function (evt) {
      selectedIds = [];
      selectedCases = [];
      const $selectAll = $(this);
      if ($(this).is(":checked")) {
        //selectAll = true;
        if ($(".dt-paging-button").length > 5) {
          fetchSelectedCasesFromServer(function () {
            $selectAll.trigger('tableSelectedRowsCountChanged', [(selectedIds ? selectedIds.length : 0)]);
          });
        } else {
          // selectAll=true;
          $(".selectCheckbox").prop("checked", true).trigger("change");
        }
      } else {
        $(".selectCheckbox").prop("checked", false);
      }
      $selectAll.trigger('tableSelectedRowsCountChanged', [(selectedIds ? selectedIds.length : 0)]);
    });

    function fetchSelectedCasesFromServer(callback) {
        showLoader();
        $('#exportWarning').modal('hide');
        var jForm = new FormData();
        jForm.append("dataType", $("#dataType").val())
        jForm.append("async", true)
        for (var k in lastTableFilter) {
            jForm.append(k, lastTableFilter[k]);
        }
        $.ajax({
            url: getSelectAllUrl,
            type: "POST",
            data: jForm,
            mimeType: "multipart/form-data",
            contentType: false,
            cache: false,
            processData: false,
            success: function (_data) {

                selectedIds = [];
                selectedCases = [];
                var data = JSON.parse(_data)
                for (var i = 0; i < data.length; i++) {
                    if (selectedIds.length == BULK_UPDATE_MAX_ROWS) {
                        $("#bulkUpdateMaxRowsWarning").modal("show");
                        break;
                    }
                    selectedIds.push(data[i].id);
                    selectedCases.push({
                        caseNumber: data[i].caseNumber,
                        caseVersion: data[i].caseVersion,
                        errorType: data[i].errorType,
                        submissionIdentifier: data[i].submissionIdentifier,
                        id: data[i].id
                    })

                }
                hideLoader();
                updateSelectedCheckboxes();
                if ($.isFunction(callback)) {
                  callback();
                }
            },
            error: function (data) {
                console.log("Error fetching data", data);
                hideLoader();
                alert("Sorry! System level error")
            }
        });
    }

    function updateSelectedCheckboxes() {
        for (var i = 0; i < selectedIds.length; i++) {
            if (!$(".selectCheckbox[_id=" + selectedIds[i] + "]").is(":checked")) $(".selectCheckbox[_id=" + selectedIds[i] + "]").click();

        }

  }

  table.on("click", "td.expand-row .glyphicon", function (evt) {
    evt.preventDefault();
    var tr = $(this).closest("tr");
    var row = table.row(tr);
    var idx = row.index();
    if (row.child.isShown()) {
      // This row is already open - close it
      row.child.hide();
      var td = tr.find("td i:first-child");
      var fixedColumnRow = $(document).find(
        ".DTFC_Cloned tbody tr[data-dt-row=" +
          row.index() +
          '][class*="padding"]'
      );
      fixedColumnRow.remove();
      td.removeClass("glyphicon-triangle-bottom");
      td.addClass("glyphicon-triangle-right");
      tr.removeClass("shown");
    } else {
      // Open this row
      row.child(format(row.data())).show();
      var rowHeight = $(row.child()).height();
      var fixedColumnRow = $(document).find(
        ".DTFC_Cloned tbody tr[data-dt-row=" + idx + "]"
      );
      fixedColumnRow.after(
        "<tr class='padding' data-dt-row='" +
          idx +
          "' style='height:" +
          rowHeight +
          "px'><td colspan='2'></td></tr>"
      );
      var td = tr.find("td i:first-child");
      td.removeClass("glyphicon-triangle-right");
      td.addClass("glyphicon-triangle-bottom");
      tr.addClass("shown");
    }

    return false;
  });

  table.on("click", "label.selected", function () {
    var row = $(this).closest("tr");
    var labelFor = $(this).attr("for");
    if (labelFor) {
      var itm = row.find("input[name='" + labelFor + "']");
      var val = $(itm).prop("checked");
      $(itm).prop("checked", !val).trigger("change");
    }
  updateSelectedCheckboxes();
    });

  table.on("change", "input.selectCheckbox", function () {
    var isChecked = $(this).prop("checked");
    var id = $(this).attr("_id");
    var caseNumber = $(this).attr("_caseNumber");
    var caseVersion = $(this).attr("_caseVersion");
    var submissionIdentifier = $(this).attr("_submissionIdentifier");
    const errorType = $(this).attr("_errorType");

    var idx = findIdx(selectedIds, id);
    var idx_cn = _.findIndex(selectedCases, function (it) {
      return ((it.caseNumber == caseNumber) && (it.caseVersion = caseVersion)&& (it.submissionIdentifier = submissionIdentifier));
    });
    if (isChecked) {
      if (idx < 0) {
        if (id) {
          if (selectedIds.length == BULK_UPDATE_MAX_ROWS) {
            $("#bulkUpdateMaxRowsWarning").modal("show");
            return false;
          } else {
            selectedIds.push(id);
            selectedCases.push({caseNumber: caseNumber, caseVersion: caseVersion, id: id, errorType: errorType, submissionIdentifier:submissionIdentifier})
          }
        }
      }
    } else {
      if (idx >= 0) {
        selectedIds.splice(idx, 1);

        selectedCases.splice(idx, 1);
      }
      if ($("#selectAll").is(":checked")) {
        $("#selectAll").prop("checked", false);

      }
    }

    $(this).trigger('tableSelectedRowsCountChanged', [(selectedIds ? selectedIds.length : 0)]);

  });
};

function findIdx(arItems, searchValue) {
  var found = false;
  var return_idx = -1;
  for (var i = 0; i < arItems.length && !found; i++) {
    if (arItems[i] == searchValue) {
      found = true;
    }
  }
  if (found) {
    return_idx = i - 1;
  } else {
    return_idx = -1;
  }
  return return_idx;
}

function findInArray(arItems, searchValue) {
  var found = false;
  for (var i = 0; i < arItems.length && !found; i++) {
    if (arItems[i] == searchValue) {
      found = true;
    }
  }
  return found;
}

priorityRender = function (data, type, row) {
  if (row.isFinalState)
    return row.priority
  else
    return "<span class='editPriority' style='cursor: pointer;display: block; width: 100%' data-value='" + row.priority + "' data-id='" + row.id + "' data-case-number='" + row.caseNumber + "' data-error-type='" + row.errorType + "'  data-dataType='" + row.dataType + "'>" + (row.priority != "-1" ? row.priority : "...") + "</span>";
};

actionItemStatusRender = function (data, type, row) {
  var creationAction = "&nbsp;";
  var clazz = "";
  if (row.actionItemStatus) {
    switch (row.actionItemStatus) {
      case ACTION_ITEM_GROUP_STATE_ENUM.OVERDUE:
        clazz = "btn btn-danger btn-xs";
        break;
      case ACTION_ITEM_GROUP_STATE_ENUM.WAITING:
        clazz = "btn btn-warning btn-xs";
        break;
      case ACTION_ITEM_GROUP_STATE_ENUM.CLOSED:
        clazz = "btn btn-success btn-xs";
        break;
      default:
        clazz = null;
        break;
    }
    creationAction = (clazz === null) ? '&nbsp;' : '<button class="' + clazz + ' btn-round actionItemModalIcon" data-case-number="' + row.caseNumber + '" data-dataType="' + row.dataType + '" data-error-type="' + row.errorType + '" style="width:70px;">' + $.i18n._('app.actionItemGroupState.' + row.actionItemStatus) + '</button>';
  }
  return creationAction;
};

buttonGroupRender = function (data, type, row) {
  var viewCriteriaButton = "";
  var actionButton = "";
  var commentedIconDropdown = "fa-comment-o";
  if (row.comment !== null) {
    commentedIconDropdown = "fa-commenting-o";
  }
  if(data.entryType === manualEntryType && showCriteriaForManualError){
    viewCriteriaButton = '<a href="javascript:void(0)" id="viewCriteriaForManualError" class="" data-caseNumber="'+data.caseNumber+'" data-reportId="'+data.executedReportId+'" data-id="'+data.id+'" ><span><i class="fa fa-info-circle" data-placement="left"></i></span> '+$.i18n._('qualityModule.viewCriteria.label')+'</a>'
  }else if(data.entryType !== manualEntryType){
    viewCriteriaButton = '<a href="javascript:void(0)" id="viewCriteria" class="" data-reportId="'+data.executedReportId+'"><span><i class="fa fa-info-circle" data-placement="left"></i></span> '+$.i18n._('qualityModule.viewCriteria.label')+'</a>'
  }
  actionButton += '<button class="btn btn-success dropdown-toggle" type="button" data-toggle="dropdown">\n' +
      '    <span class="md-dots-vertical" data-placement="left"></span></button>\n' +
      '    <ul class="dropdown-menu">\n'
  if (!row.isFinalState) {
    actionButton += '  <li><a href="javascript:void(0)" data-caseNumber="' + data.caseNumber + '" data-caseVersion="' + (data.consolidatedVersion ? data.consolidatedVersion : data.masterVersionNum) + '" data-id="' + row.id + '" data-dataType="' + data.dataType + '" data-errorType="' + data.errorType + '" class="createActionItem">' + $.i18n._('app.label.action.app.name') + '</a></li>\n' +
        '  <li><a href="#" id="createIssue" class="pv-btn-badge" data-id="' + data.id + '" ><i class="fa fa-ticket" data-placement="left"></i>  ' + $.i18n._('qualityModule.createIssue.label') + '</a></li>\n'
  }
  actionButton += '  <li><a href="javascript:void(0)" class="reasonOfDelayModalBtn" data-caseNumber="' + row.caseNumber + '" data-dataType="' + row.dataType + '" data-id="' + row.id + '" data-viewmode="' + row.isFinalState + '"><span><i class="fa fa-pencil" data-placement="left"></i></span> ' + $.i18n._('app.quality.edit.rootCause') + '</a></li>\n' +
      '  <li>' + viewCriteriaButton + '</li>\n' +
      '  <li><a href="#" data-content="' + escapeHTML(row.comment) + '" data-caseNumber="' + row.caseNumber + '" data-errorType="' + row.errorType + '" data-id="' + row.id + '" data-viewmode="' + row.isFinalState + '" class="addComment pv-btn-badge"><i class=" fa ' + commentedIconDropdown + ' " data-content="' + escapeHTML(row.comment) + '" data-placement="left"></i><span></span> ' + $.i18n._('app.caseList.comment') + '</a></li>\n' +
      '</ul>\n' +
      '</div>';

  return "<div style='display: flex; justify-content: center'><span class=\"pv-grp-btn btn-group dropdown\" style='width: 20px'>" + actionButton + '</span></div>';
};

function getSelectedRowsCount() {
  var result = "";
  var recCount = 0;
  if (selectAll == true) {
    recCount = totalFilteredRecord;
  } else {
    recCount = selectedIds.length;
  }
  if (recCount > 0) {
    result += recCount;
    if (result == 1) {
      result += " " + $.i18n._("sng.row.count");
    } else {
      result += " " + $.i18n._("multi.row.count");
    }
    result += " " + $.i18n._("label.selected");
  }
  return result;
}

function showActionItemsModal(clickedButton) {
  var qualityDetails = {};
  qualityDetails["dataType"] = clickedButton.attr("data-dataType");
  qualityDetails["caseNumber"] = clickedButton.data("case-number");
  qualityDetails["errorType"] = clickedButton.data("error-type");
  actionItem.actionItemModal.view_action_item_list(
    hasAccessOnActionItem,
    false,
    QUALITY_MODULE,
    qualityDetails
  );
}

var init_table_filter = function () {
  var filter_data = [];
  filter_data.push({
    label: "Case Number",
    type: "multi-varchar",
    name: "masterCaseNum",
    filter_group: "standard",
  });

  function filterFieldsAttr(
    i,
    fieldNameList,
    fieldLabelList,
    fieldTypeList,
    filter_group,
    selectableFieldsList
  ) {
    var d = {};
    d["name"] = $.trim(fieldNameList[i]);
    d["label"] = $.trim(fieldLabelList[i]);
    d["filter_group"] = filter_group;
    var fieldType = $.trim(fieldTypeList[i]);
    fieldTypeMapping[d["name"]] = fieldType;
    if (fieldType == "DATE" || fieldType == "TIMESTAMP") {
      d["type"] = "date";
        var labelFrom = d["label"] + $.i18n._("app.advancedFilter.from");
        var labelTo = d["label"] + $.i18n._("app.advancedFilter.to");

        filter_data.push({
            label: labelFrom,
            type: 'date-range',
            name: d["name"],
            group: d["name"],
            group_order: 1,
            filter_group: filter_group
        });

        filter_data.push({
            label: labelTo,
            type: 'date-range',
            name: d["name"],
            group: d["name"],
            group_order: 2,
            filter_group: filter_group
        });
      //d["group"] = d["name"];
      //d["group_order"] = group_order;
    } else if (fieldType == "NUMBER") {
      d["type"] = "number";
    } else if ($.trim(selectableFieldsList[i]) === "true") {
      d["type"] = "rptField-select2";
    } else {
      d["type"] = "text";
    }
    if(d["type"] != "date"){
      filter_data.push(d);
    }
  }

  for (var i= 0; i < columnNameList.length; i++) {
    var fname = $.trim(columnNameList[i]);
    if (fname !== "masterCaseReceiptDate" && fname !== 'masterCaseNum') {
      filterFieldsAttr(i, columnNameList, columnLabelList, columnTypeList, "standard", standardReportSelectableFields);
    }
  }

  filter_data.push({
    label: $.i18n._("app.advancedFilter.priority"),
    type: "select2-manual",
    name: "priority",
    data: priorityArray,
    multiple: true,
    filter_group: "standard",
  });
  filter_data.push({
    label: $.i18n._("app.advancedFilter.assigned"),
    type: "select2-users",
    name: "assignedTo",
    data: usersAndGroupArray,
    multiple: true,
    filter_group: "standard",
  });
  filter_data.push({
    label: $.i18n._("app.advancedFilter.assigned"),
    type: "select2-users",
    name: "assignedTo",
    data: usersAndGroupArray,
    multiple: true,
    filter_group: "standard",
  });

  filter_data.push({
    label: $.i18n._("workFlowState"),
    type: "select2-manual",
    name: "workflowState",
    data: workFlowArray,
    multiple: true,
    filter_group: "standard",
  });

  filter_data.push({
    label: $.i18n._("app.errorType.name"),
    type: "select2-enum",
    data: _.map(errorTypeList || [], function (item) {
      const val = $.trim(item);
      return {key: val, value: val};
    }),
    name: "errorType",
    multiple: true
  });

  filter_data.push({
    label: $.i18n._("app.fixed.template.issueType"),
    type: "select2-manual",
    data: issueTypeArray,
    name: "qualityIssueType",
    multiple: true,
    filter_group: "standard",
  });

  filter_data.push({
    label: $.i18n._("app.advancedFilter.workflowGroup"),
    type: "select2-manual",
    name: "workflowGroup",
    data: [
      {
        key: "final",
        value: $.i18n._("qualityModule.allFinal.label"),
        selected: true,
        placeholder: $.i18n._("qualityModule.allInWorkflow.label"),
      },
      { key: "all", value: $.i18n._("qualityModule.allStates.label") },
    ],
    filter_group: "standard",
  });

  filter_data.push({
    label: $.i18n._("app.fixed.template.rootCause"),
    type: "select2-manual",
    name: "rootCause",
    data: rootCauseArray,
    multiple: true,
    filter_group: "standard",
  });

  filter_data.push({
    label: $.i18n._("app.fixed.template.respParty"),
    type: "select2-manual",
    name: "responsibleParty",
    data: respPartyArray,
    multiple: true,
    filter_group: "standard",
  });
  filter_data.push({
    label: $.i18n._("app.advancedFilter.dueIn") + $.i18n._("app.advancedFilter.from"),
    type: "date-range",
    name: "dueDate",
    group: "dueDate",
    group_order: 1,
    filter_group: "standard",
  });
  filter_data.push({
    label: $.i18n._("app.advancedFilter.dueIn") + $.i18n._("app.advancedFilter.to"),
    type: "date-range",
    name: "dueDate",
    group: "dueDate",
    group_order: 2,
    filter_group: "standard"
  });
  filter_data.push({
    label: $.i18n._("app.advancedFilter.dateCreated") + $.i18n._("app.advancedFilter.from"),
    type: "date-range",
    name: "dateCreated",
    group: "dateCreated",
    group_order: 1,
    filter_group: "standard",
  });
  filter_data.push({
    label: $.i18n._("app.advancedFilter.dateCreated") + $.i18n._("app.advancedFilter.to"),
    type: 'date-range',
    name: 'dateCreated',
    group: 'dateCreated',
    group_order: 2,
    filter_group: 'standard'
  });

  for (var i = 0; i < reportOtherFieldName.length; i++) {
    var fname = $.trim(reportOtherFieldName[i]);
    if (fname != "" && fname != "masterCaseReceiptDate") {
      filterFieldsAttr(i, reportOtherFieldName, reportOtherFieldLabel, reportOtherFieldType, "additional", reportSelectableFields);
    }
  }

  pvr.filter_util.construct_right_filter_panel({
    table_id: "#rxTableQualityReports",
    container_id: "config-filter-panel",
    filter_defs: filter_data,
    column_count: 2,
    panel_width: 800,
    filter_group: [
      { key: "standard", label: "" },
      { key: "additional", label: "" },
    ],
    done_func: function (filter) {
      tableFilter = filter;
      advancedFilter = true;
      table.draw();
      pvr.filter_util.update_filter_icon_state($('#' + table.table().container().id), filter);
    },
  });
  $("#config-filter-panel")
    .find(".panel-body")
    .css({ "max-height": "520px", "overflow-y": "scroll" });
  //bindSelect2WithUrl($("input[data-name=owner]"), ownerListUrl, ownerValuesUrl, true);
};

function initCaseNumModal() {
  var txt = _.uniq(
    _.map(selectedCases, function (it) {
      return it.caseNumber;
    })
  ).join(",");
  $("#caseNumberContainer").val(txt);
}

function initChangeErrorType() {
  $.ajax({
    url: fetchErrorTypesUrl,
    data: { dataType: $("#dataType").val() },
    dataType: "json",
  })
    .done(function (dataList) {
      var $select = $("#editErrorTypeSelect");
      if ($select.length > 0) {
        $select.empty();
        let errorTypeSelectOptionsContent = '';
        for (var dataOption in dataList) {
          errorTypeSelectOptionsContent += ("<option value='" + escapeHTML(dataList[dataOption]) + "'>" + escapeHTML(dataList[dataOption]) + "</option>");
        }
        //using of Function here as snyk blocker workaround
        $select.html(errorTypeSelectOptionsContent);
      }
    })
    .fail(function (data) {
      console.log("Error fetching error type data", data);
    });
}

function removeOptionColumn() {
  var tableColumns = $("#tableColumns");
  tableColumns.find("tbody tr:first").remove();
  for (var i = 0; i < optionsremovecoldatacol.length; i++) {
    tableColumns.find("tbody tr").each(function (r) {
      if (
        $(this).find("td").eq(1).find("input").attr("data-columns") ==
        optionsremovecoldatacol[i]
      ) {
        this.remove();
        return false;
      }
    });
  }
}

function reloadRodTable(txt) {
    var successNotificationAlert = $("#successNotification");
    var notificationAlertTimer = successNotificationAlert.attr('data-alert-timer');
    if (notificationAlertTimer) {
        clearTimeout(notificationAlertTimer);
    }
    successNotificationAlert.html(txt ? txt : "Success")
    successNotificationAlert.parent().show();
    successNotificationAlert.attr('data-alert-timer', setTimeout(function () {
        $("#successNotification").parent().hide();
    }, 30000));
    pageDataTable.ajax.reload(null, false);
}

function reloadHeaderData() {
  var form = $("#reasonOfDelayModalForm");

  // Updated State
  var newState = form.find(".workflow option:selected").text();
  var stateObj = $("#case-form-state");
  if (stateObj.val() !== newState) {
    stateObj.attr("data-initial-state", newState);
    stateObj.html(newState);
  }

  // Updated Assigned To
  var newAssignedTo = form.find("select.editAssignedTo").val();
  var assignedToObj = $("#case-form-assignedTo");
  if (assignedToObj.val() !== newAssignedTo) {
    assignedToObj.val(newAssignedTo).trigger("change");
  }
}

$(document).on("click", ".editPriority, .editErrorType", function (e) {
    var $this = $(this);
    var oldVal = $this.attr("data-value")??"";
    var isPriorityControl = $this.hasClass("editPriority");

    var $editDiv = isPriorityControl ? $("#priorityEditDiv") : $("#errorTypeEditDiv");
    var editSelect = isPriorityControl ? $editDiv.find('#priorityEdit') : $editDiv.find('#editErrorTypeSelect');
    showEditDiv($this, $editDiv, editSelect, 85, true);
    if (oldVal) editSelect.val(oldVal).change();
    $editDiv.find(".saveButton").unbind().one('click', function (e) {
        var newVal = editSelect.val();
        if (newVal !== oldVal) {
            $.ajax({
                url: isPriorityControl ? updatePriorityUrl : updateErrorTypeUrl,
                data: {
                    'caseNumber': $this.data('case-number'),
                    "errorType": $this.data('error-type'),
                    "value": newVal,
                    "dataType": $this.attr("data-dataType"),
                    "id": $this.attr("data-id"),
                    "selectedIds": ((selectedIds && selectedIds.length > 0) ? selectedIds.join(";") : "")
                },
                success: function (data) {
                    reloadRodTable();
                },
                error: function (data) {
                    alert($.i18n._('Error') + " : " + data.responseText);

                }
            });
        }
        $editDiv.hide()
        reloadRodTable();
    });
});
$(document).on("click", ".assignedWithCell", function (e) {
  var $this = $(this);
  let field = "User_";
  var id = $this.attr("data-id");
  var oldVal = $this.attr("data-value")??"";
  if ($('#assignedWithEdit').hasClass('select2-hidden-accessible')) {
    $('#assignedWithEdit').select2("destroy");
  }
  $('#assignedWithEdit').attr("data-value", oldVal)
  $('#assignedWithEdit').val(oldVal)
  if ($this.hasClass("editAssignedToUser")) {
    let groupValue = $(this).closest("td").find(".editAssignedToUserGroup").attr("data-value")
    $('#assignedWithEdit').attr("data-extraParam", JSON.stringify({userGroup: groupValue}));
    bindShareWith($('#assignedWithEdit'), sharedWithUserListUrl, sharedWithValuesUrl, "100%", true);
  } else {
    field = "UserGroup_";
    let userValue = $(this).closest("td").find(".editAssignedToUser").attr("data-value")
    $('#assignedWithEdit').attr("data-extraParam", JSON.stringify({user: userValue}));
    bindShareWith($('#assignedWithEdit'), sharedWithGroupListUrl, sharedWithValuesUrl, "100%", true);
  }


  var $assignedWithEditDiv = $("#assignedWithEditDiv");
  showEditDiv($this, $assignedWithEditDiv, $assignedWithEditDiv.find("#assignedWithEdit"), 85, true);
  $assignedWithEditDiv.find("#assignedWithEdit").val(oldVal).trigger("change");
    $("#assignedWithEdit").val(oldVal);
    $assignedWithEditDiv.find(".saveButton").off().on("click", function (e) {
      var $assignedWithEdit = $assignedWithEditDiv.find("#assignedWithEdit");
      var newVal = $assignedWithEdit.val();
      var newLabel = $assignedWithEdit.select2("data")[0]
        ? $assignedWithEdit.select2("data")[0].text
        : "...";
      var data = dataObj($this, newVal);
      data.field=field
      if ((newVal !== oldVal)||(selectedIds.length > 1)) {
        ajaxCall(
          updateAssignedOwnerUrl,
          data,
          function (result) {
            $this.attr("data-value", newVal);
            $this.text(newLabel);
            if (selectedIds.length > 1)
              reloadRodTable($.i18n._("assigned.success"));
            var $hiddenElement = $(
              '<div class="hidden-element" style="white-space: nowrap"></div>'
            )
              .text(newLabel)
              .appendTo($this);
            var textWidth = $hiddenElement.width();
            $hiddenElement.remove();

            var containerWidth = $this.parent().parent().width();
            if (textWidth > containerWidth) {
              $this.find(".ico-dots").css("display", "inline-block");
              $this.find(".ico-dots").attr("title", newLabel);
            } else {
              $this.find(".ico-dots").css("display", "none");
            }
          },
          function (err) {
            errorNotificationForQuality(
              $.i18n._("Error") + " : " + err.responseText
            );
          }
        );
      }
      $assignedWithEditDiv.hide();
    });
});

function ajaxCall(url, data, success, error) {
  showLoader();
  $.ajax({
    type: "POST",
    url: url,
    data: data,
    dataType: "html",
  })
    .done(function (result) {
      success(result);
      hideLoader();
    })
    .fail(function (err) {
      errorNotificationForQuality(
          $.i18n._("Error") + " : " + err.responseText
      );
      hideLoader();
      window.scrollTo(0, 0);
    });
}

function dataObj(element, newVal) {
  var data = {
    caseNumber: element.attr("data-case-number"),
    errorType: element.data("error-type"),
    value: newVal !== null && newVal.length > 0 ? newVal : null,
    dataType: element.attr("data-dataType"),
    version: element.data("version-number"),
    submissionIdentifier: element.attr('data-submissionIdentifier'),
    selectedCases:
      selectedCases && selectedCases.length > 0
        ? JSON.stringify(selectedCases)
        : "",
  };
  return data;
}

function formAssignedWithCell(row, assignedName, assignedId, selector) {
  //Todo: Check for final state, consolidatedCaseNum, editAssignedTo, updateData impact
  if (!row.consolidatedCaseNum) {
    return "";
  }
  var userAndGroupValue = [{ id: assignedId, username: assignedName }];
  if (row.isFinalState)
    return assignedName ;
  else
    return "<span class='assignedWithCol assignedWithCell updateData "+selector+"' style='cursor:pointer;display: block; width: 100%' data-id='" + row.id + "' data-value='" + assignedId + "' data-usergroups='" + JSON.stringify(userAndGroupValue) + "' data-case-number='"+row.caseNumber+"' data-error-type='"+row.errorType+"'  data-dataType='"+row.dataType+"' data-submissionIdentifier='"+row.submissionIdentifier+"' data-version-number='"+row.masterVersionNum+"'>" + (assignedName?assignedName:"...") + "</span>";
}
function formAssignedToUserCell(row) {
  return formAssignedWithCell(row, row.assignedToUser, row.assignedToUserId, "editAssignedToUser")
 }
 function formAssignedToUserGroupCell(row) {
  return formAssignedWithCell(row, row.assignedToUserGroup, row.assignedToUserGroupId, "editAssignedToUserGroup")
 }
$("#copyCaseNumModal").on("hidden.bs.modal", function () {
  $("#caseNumberContainer").val("");
});

$("#emailToModal").on("hidden.bs.modal", function () {
  $("#emailUsers").val(null).trigger("change");
  $('#emailToModal input[name="subject"]').val("");
  $('#emailToModal textarea[name="body"]').val("");
  $("#emailToModal .alert-danger").addClass("hide");
}).on("shown.bs.modal", function () {
  setSelect2InputWidth($("#emailUsers"));
});

$("#emailToModal .alert-danger .close").on("click", function () {
  $("#emailToModal .alert-danger").addClass("hide");
});

$('#emailToModal form[name="emailToModalModal"]').on("submit", function (event) {
    var emailUsersVal = $("#emailUsers").val();
    var subjectVal = $('#emailToModal input[name="subject"]').val();
    var bodyVal = $('#emailToModal textarea[name="body"]').val();

    if (!emailUsersVal || !subjectVal || !bodyVal) {
      event.preventDefault();
      $("#emailToModal .errorMessageSpan").text(
        $.i18n._("app.actionItem.fill.all.fields")
      );
      $("#emailToModal .alert-danger").removeClass("hide");
    }
  });

function renderCell(row, filedName) {
  if (filedName == "masterCaseNum") {
    return "<a href='" + caseDataLinkUrl + "&caseNumber=" + row.masterCaseNum + "&versionNumber=" + row.masterVersionNum + "&submissionIdentifier=" + row.submissionIdentifier + "&errorType=" + row.errorType + "&state=" + row.state + "&id=" + row.id + "' " +
        "target = '_blank'>" + (row.consolidatedCaseNum ? row.consolidatedCaseNum : "&nbsp;") + "</a> ";
  }
  if (columnTypeList[columnNameList.indexOf(filedName)] == "DATE") {
    if (row[filedName] && row.consolidatedCaseNum) {
      var d = new Date(row[filedName]);
      return moment(d).format(DEFAULT_DATE_DISPLAY_FORMAT);
    } else {
      return '&nbsp;';
    }
  }
  if (!row.consolidatedCaseNum) {
    return '&nbsp;';
  }
  return '<div class="one-row-dot-overflow">' + (row[filedName] ? row[filedName] : "&nbsp;") + '</div>';
}

function createColDefs() {
  coldefs = [];
  var coldefSel = {
    "data": null,
    "name": 'select',
    "bSortable": false,
    className: "expand-row",
    width: '25px'
  };

  coldefSel["mRender"] = function (data, type, row, meta) {
    var strPrm = "<div class='checkbox checkbox-primary' style='margin-top: 5px !important;'>";
    strPrm = strPrm + "<input type='checkbox' class='selectCheckbox' style='display:none' name='selected" + meta.row + "' _caseNumber='" + data.caseNumber + "' _caseVersion='" + data.masterVersionNum+ "' _submissionIdentifier='" + data.submissionIdentifier + "' _errorType='" + data.errorType + "' _id='" + data.id + "' ";
    if (findInArray(selectedIds, data.id)) {
      strPrm += "checked=true ";
    }
    strPrm = strPrm + "/>";
    strPrm = strPrm + "<label class= 'selected' for='selected" + meta.row + "'> <i class='glyphicon glyphicon-triangle-right' style='margin-left: 15px;'></i></label>"
    strPrm = strPrm + "</div>";
    return strPrm;
  }
  coldefs.push(coldefSel);

  $.each(columnNameList, function (index, val) {
    var coldef = {};
    val = $.trim(val);
    coldef["data"] = val;
    coldef["name"] = val;
    coldef["sClass"] = "text-center";
    coldef["bSortable"] = true;
    coldef["visible"] = true;
    coldef["stackId"] = columnUiStackRevertedMapping[val];
    coldef["width"] = '110px';
    coldef["mRender"] = function (data, type, row) {
      return renderCell(row, val);
    }
    coldefs.push(coldef);
  });
  coldefs.push({
    "data": 'errorType',
    "name": 'errorType',
    "bSortable": false,
    "visible": true,
    width: '120px',
    "mRender": function (data, type, row) {
      if (row.entryType === "M") {
        if (!row.isFinalState)
          return "<span class='editErrorType' style='cursor:pointer; display: block; width: 100%' data-value='" + row.errorType + "' data-id='" + row.id + "' data-case-number='" + row.caseNumber + "' data-error-type='" + row.errorType + "'  data-dataType='" + row.dataType + "'>" + row.errorType + "</span>";
      }
      return '<div class="one-row-dot-overflow">' + data + '</div>';
    }
  });

  coldefs.push({
    "data": 'issueTypeDetail',
    "name": 'issueTypeDetail',
    "sClass": "text-center",
    "bSortable": false,
    "visible": true,
    width: '110px',

    "mRender": function (data, type, row) {
      const value = _.isEmpty(row.issueTypeDetail.qualityIssueType) ? EMPTY_LABEL : row.issueTypeDetail.qualityIssueType;
      return '<div class="one-row-dot-overflow">' + value + '</div>';
    }
  });

  coldefs.push({
    "data": 'rootCauseDetail',
    "name": 'rootCauseDetail',
    "sClass": "text-center",
    "bSortable": false,
    width: '110px',
    stackId: 1001,
    "visible": true,
    "mRender": function (data, type, row) {
      const value = _.isEmpty(row.issueTypeDetail.rootCause) ? EMPTY_LABEL : row.issueTypeDetail.rootCause;
      return '<div class="one-row-dot-overflow">' + value + '</div>';
    }
  });

  coldefs.push({
    "data": 'responsiblePartyDetail',
    "name": 'responsiblePartyDetail',
    "sClass": "text-center",
    "bSortable": false,
    width: '110px',
    stackId: 1001,
    "visible": true,
    "mRender": function (data, type, row) {
      const value = _.isEmpty(row.issueTypeDetail.responsibleParty) ? EMPTY_LABEL : row.issueTypeDetail.responsibleParty;
      return '<div class="one-row-dot-overflow">' + value + '</div>';
    }
  });

  coldefs.push({
    "data": 'dateCreated',
    "name": 'dateCreated',
    width: '110px',
    "sClass": "text-center",
    "bSortable": true,
    stackId: 1002,
    "visible": true,
    "mRender": function (data, type, row) {
      if (!row.consolidatedCaseNum) {
        return '';
      }
      return '<div class="one-row-dot-overflow edt-stacked-render-data">' + moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) + '</div>';
    }
  });

  coldefs.push({
    "data": 'priority',
    "name": 'priority',
    "sClass": "text-center",
    "bSortable": false,
    width: '110px',
    stackId: 1002,
    visible: true,
    "mRender": priorityRender
  });

  coldefs.push({
    "data": 'dueIn',
    "name": 'dueIn',
    width: '120px',
    "bSortable": true,
    "visible": true,
    class: "text-center",
    "mRender": function (data, type, row) {
      if (!row.consolidatedCaseNum) {
        return '';
      }
      var clazz = "";
      if (row.indicator == "red") clazz = 'class="label-danger text-white lbl-badge"';
      if (row.indicator == "yellow") clazz = 'class="label-primary" style="padding: 2px;"';
      return '<span ' + clazz + '>' + moment(row.dueIn).tz(userTimeZone).format(DUEIN_DATE_FORMAT) + "</span>";
    }
  });

  coldefs.push({
    "data": 'assignedToUserGroup',
    "name": 'assignedToUserGroup',
    "sClass": "text-center",
    "bSortable": false,
    width: '110px',
    stackId: 1003,
    visible: true,
    "mRender": function (data, type, row) {
      return '<div class="one-row-dot-overflow">' + formAssignedToUserGroupCell(row) + '</div>';
    }
  });

  coldefs.push({
    "data": 'assignedToUser',
    "name": 'assignedToUser',
    "sClass": "text-center",
    "bSortable": false,
    width: '110px',
    stackId: 1003,
    visible: true,
    "mRender": function (data, type, row) {
      return '<div class="one-row-dot-overflow">' + formAssignedToUserCell(row) + '</div>';
    }
  });

  coldefs.push({
    "data": 'state',
    "name": 'state',
    "sClass": "text-center",
    "bSortable": false,
    "visible": true,
    width: '100px',
    stackId: 1004,
    "mRender": function (data, type, row) {
      if (!row.consolidatedCaseNum) {
        return '';
      }
      if (row.isFinalState)
        return '<button class="btn btn-default btn-xs workflowButton" disabled="disabled" title="' + row.state + '" style="min-width: 10px" data-dataType="' + row.dataType + '" data-quality-data-id= "' + row.id + '" data-initial-state= "' + row.state + '" data-evt-clk=\'{\"method\": \"openStateHistoryModal\", \"params\": []}\'>' + row.state + '</button>';
      else
        return '<button class="btn btn-default btn-xs workflowButton" style="min-width: 10px" title="' + row.state + '" data-dataType="' + row.dataType + '" data-quality-data-id= "' + row.id + '" data-initial-state= "' + row.state + '" data-evt-clk=\'{\"method\": \"openStateHistoryModal\", \"params\": []}\'>' + row.state + '</button>';
    }
  });

  coldefs.push({
    "data": "ai",
    "name": "ai",
    "sClass": "text-center",
    stackId: 1004,
    "bSortable": false,
    "visible": true,
    width: '100px',
    "mRender": actionItemStatusRender
  });
  coldefs.push({
    "data": 'comm',
    "name": 'comm',
    width: '50px',
    "sClass": "text-center",
    "bSortable": true,
    "visible": true,
    "customMenuLabel": $.i18n._("lateProcessing.comments.label") + "<br>" + $.i18n._("qualityModule.issue.label"),
    "mRender": function (data, type, row) {
      issWidth = "style=\"width: 34px;margin-top: 2px\"";
      var issue = $.i18n._('no');
      if (row['hasIssues'] === 'true') {
        issue = $.i18n._('yes')
        issWidth = ""
      }

      issue = '<a href="#" id="createIssue" class="btn lbl-badge-default btn-xs" style="margin-top: 2px"  data-id="' + row.id + '" >' + issue + '</a>';

      var comment
      if (row.comment !== null) {
        comment = '<a href="#" data-content="' + escapeHTML(row.comment) + '" data-caseNumber="' + row.caseNumber + '" data-errorType="' + row.errorType + '" data-id="' + row.id + '" data-viewmode="' + row.isFinalState + '" class="addComment  btn lbl-badge-default btn-xs"><i class=" fa fa-commenting-o commentPopoverMessage " data-content="' + escapeHTML(row.comment) + '" data-placement="left" style="" title="' + $.i18n._('app.caseList.comment') + '"></i></a></>';
      } else {
        comment = '<a href="#" data-content="' + escapeHTML(row.comment) + '" data-caseNumber="' + row.caseNumber + '" data-errorType="' + row.errorType + '" data-id="' + row.id + '" data-viewmode="' + row.isFinalState + '" class="addComment  btn lbl-badge-default btn-xs"><i class=" fa fa-comment-o" data-content="' + escapeHTML(row.comment) + '" data-placement="left"></i></a>'
      }


      return '<div>' + comment + '</div><div class=\'pv-stacked-row\'>' + issue + '</i></div>';
    }
  });
  coldefs.push({
    "data": null,
    "name": null,
    "bSortable": false,
    "visible": true,
    "mRender": buttonGroupRender,
    width: '70px'
  });
}
