$(function () {
  $(".select2-box").select2();
  $("#actionItemModal")
    .find("#description")
    .on("keyup", function () {
      var text_length = $("#actionItemModal").find("#description").val().length;
      var text_remaining = 4000 - text_length;
      $("#remainingChar")
        .show()
        .html(text_remaining + "/4000 " + $.i18n._("app.characters.remaining"));
    });
  $("#actionItemModal")
    .find("#comment")
    .on("keyup", function () {
      var text_length = $("#actionItemModal").find("#comment").val().length;
      var text_remaining = 4000 - text_length;
      $("#remainingChar2")
        .show()
        .html(text_remaining + "/4000 " + $.i18n._("app.characters.remaining"));
    });
  $(document).on(
    "click",
    ".consultationButtons, .creationButton, .close-action-item-model",
    function () {
      $("#remainingChar").hide();
      $("#remainingChar2").hide();
    }
  );
});
/**
 * If using with the report request application, it will require reportRequestActionItem.js
 * If using with the periodic report application, it will require periodicReportActionItem.js
 **/
var actionItem = actionItem || {};

actionItem.actionItemModal = (function () {
  var calledFromList;
  var executedReportId;
  var cllRowId;
  var tenantId;
  var masterCaseId;
  var configurationId;
  var caseIds;
  var tenantIds;
  var processedReportIds;
  var parentEntityKey;
  var dateRangeFrom;
  var dateRangeTo;
  var actionItemModalObj;
  var sectionId;
  var publisherId;
  var senderId;
  var masterVersionNum;
  var isInbound;

  //The function for initializing the action item modal.
  var init_action_item_modal = function (
    isCalledFromList,
    callingAppName,
    clickedEvent,
    selectedIds
  ) {
    $("#linkConfigDiv").hide();
    $("#linkDrilldownDiv").hide();
    actionItemModalObj = $("#actionItemModal");
    calledFromList = isCalledFromList;
    actionItemModalObj.modal("show");
    actionItemModalObj.find(".consultationButtons").addClass("hide");
    actionItemModalObj.find(".creationButton").removeClass("hide");

    //Enable all the form elements
    toggle_element_disable(actionItemModalObj, false);

    //Reset the action item modal.
    clear_action_item_modal(actionItemModalObj);

    //Set the formatted creation date
    var dateCreated = moment(new Date())
      .tz(userTimeZone)
      .locale(userLocale)
      .format(DATEPICKER_FORMAT_AM_PM);
    actionItemModalObj.find(".dateCreated").html(dateCreated);
    actionItemModalObj.find("input[name=dateCreated]").val(dateCreated);
    //This will make the due date, completion date pickers with back dates not allowed.
    initializePastDatesNotAllowedDatePicker();

    if (typeof callingAppName != "undefined" && callingAppName == PR_Calendar) {
      actionItemModalObj.find("#dueDate").val($("#dueDateHidden").val());
    } else {
      actionItemModalObj.find("#dueDate").val("");
    }
    actionItemModalObj.find(".excelExport").addClass("hide");
    actionItemModalObj.find("#completionDate").val("");
    actionItemModalObj.find("#creationScreenButton").attr("disabled", false);
    actionItemModalObj.find("#assignedToName").val("");

    $.each(actionItemModalObj.find(".select2-box"), function () {
      $(this).select2({
        dropdownParent: actionItemModalObj
      });
    });

    actionItemModalObj.find("#assignedTo").on("change", function () {
      var assignedToName = $(this).find("option:selected").text();
      actionItemModalObj
        .find("#assignedToName")
        .val(assignedToName)
        .trigger("change");
    });

    //Need to freeze the action category as Report Request in case its called from that app.
    if (
      typeof callingAppName != "undefined" &&
      callingAppName == REPOERT_REQUEST
    ) {
      actionItemModalObj
        .find("#actionCategory")
        .val(ACTION_ITEM_CATEGORY_ENUM.REPORT_REQUEST)
        .trigger("change");
    }

    //Need to freeze the action category as Quality Module in case its called from that app.
    var caseNumber = "";
    var caseVersion = "";
    var errorType = "";
    var dataType = "";
    var parentEntityKey = "";
    var _id = '';

    //Hide Associated Case No label on create
    actionItemModalObj.find("#associatedCaseNoDiv").hide();
    actionItemModalObj.find("#associatedIssueNoDiv").hide();
    if (
      typeof callingAppName != "undefined" &&
      callingAppName == QUALITY_MODULE
    ) {
      var category = $(clickedEvent).attr("data-actionType");
      if (!category) {
        actionItemModalObj
          .find("#actionCategory")
          .val(ACTION_ITEM_CATEGORY_ENUM.QUALITY_MODULE)
          .trigger("change");
      } else {
        actionItemModalObj
          .find("#actionCategory")
          .val(ACTION_ITEM_CATEGORY_ENUM[category])
          .trigger("change");
      }
      caseNumber = $(clickedEvent).attr("data-caseNumber");
      caseVersion = $(clickedEvent).attr("data-caseVersion");
      errorType = $(clickedEvent).attr("data-errorType");
      dataType = $(clickedEvent).attr("data-dataType");
      _id = $(clickedEvent).attr('data-id');
    }
    if (
      typeof callingAppName != "undefined" &&
      (callingAppName == QUALITY_MODULE_CAPA ||
        callingAppName == PV_CENTRAL_CAPA)
    ) {
      var category = $(clickedEvent).attr("data-actionType");
      actionItemModalObj
        .find("#actionCategory")
        .val(ACTION_ITEM_CATEGORY_ENUM[category])
        .trigger("change");
      actionItemModalObj.find("#actionCategory").attr("disabled", true);
    }

    //Need to set the action category as Periodic Report in case its called from that app.
    if (
      typeof callingAppName != "undefined" &&
      callingAppName == PERIODIC_REPORT
    ) {
      actionItemModalObj
        .find("#actionCategory")
        .val(ACTION_ITEM_CATEGORY_ENUM.PERIODIC_REPORT)
        .trigger("change");
    }
    if (
      typeof callingAppName != "undefined" &&
      callingAppName == ADHOC_REPORT
    ) {
      actionItemModalObj
        .find("#actionCategory")
        .val(ACTION_ITEM_CATEGORY_ENUM.ADHOC_REPORT)
        .trigger("change");
    }
    if (
      typeof callingAppName != "undefined" &&
      callingAppName == DRILLDOWN_RECORD
    ) {
      isInbound = $(clickedEvent).data("is-inbound");
      tenantId = $(clickedEvent).data("tenant-id");
      masterCaseId = $(clickedEvent).data("case-id");
      senderId = $(clickedEvent).data("sender-id");
      masterVersionNum = $(clickedEvent).data("version-num");
      processedReportId = $(clickedEvent).data("processed-report-id");
      configurationId = $(clickedEvent).data("configuration-id");
      if (isInbound) {
        actionItemModalObj
          .find("#actionCategory")
          .val(ACTION_ITEM_CATEGORY_ENUM.IN_DRILLDOWN_RECORD)
          .trigger("change");
        callingAppName = IN_DRILLDOWN_RECORD;
      } else {
        actionItemModalObj
          .find("#actionCategory")
          .val(ACTION_ITEM_CATEGORY_ENUM.DRILLDOWN_RECORD)
          .trigger("change");
      }
    }
    if (typeof callingAppName != "undefined" && callingAppName == ACTION_PLAN) {
      parentEntityKey = $(clickedEvent).attr("data-parentEntityKey");
      dateRangeFrom = $(clickedEvent).attr("data-dateRangeFrom");
      dateRangeTo = $(clickedEvent).attr("data-dateRangeTo");
    }
    //Bind the click event on save button click
    actionItemModalObj
      .find(".save-action-item")
      .off()
      .click(function () {
        actionItemModalObj.find(".save-action-item").attr("disabled", true);
        if (calledFromList) {
          save_action_item(calledFromList, actionItemModalObj, callingAppName);
        } else {
          $("#actionItemForm").find("#parentEntityKey").val(parentEntityKey);
          $("#actionItemForm").find("#dateRangeFrom").val(dateRangeFrom);
          $("#actionItemForm").find("#dateRangeTo").val(dateRangeTo);
          if (
            typeof callingAppName != "undefined" &&
            callingAppName == REPOERT_REQUEST
          ) {
            var isValid = validateContent();
            if (isValid) {
              //Call the add action item code.
              reportRequest.reportRequestActionItems.add_action_item_to_request(
                actionItemModalObj
              );

              actionItemModalObj.modal("hide");
              $("#actionItemModal").data("modal", null);
              clear_action_item_modal(actionItemModalObj);
              $("#actionItemModal")
                .find(".alert-danger")
                .css("display", "none");
            } else {
              $("#actionItemModal")
                .find(".alert-danger")
                .css("display", "block");
              actionItemModalObj.find(".save-action-item").attr("disabled", false);
            }
          } else if (
            typeof callingAppName != "undefined" &&
            callingAppName == QUALITY_MODULE
          ) {
            if (selectedIds.length > 0 && selectedIds.length != 0) {
              $.each(selectedIds, function (index, id) {
                var input = $("input[_id=" + id + "]");
                $("#actionItemForm")
                  .find("#qualityId")
                  .val(id);
                $("#actionItemForm").find("#dataTypeAi").val(dataType);
                $("#actionItemForm").find("#appType").val(callingAppName);
                save_action_item(
                  null,
                  actionItemModalObj,
                  callingAppName,
                  null,
                  selectedIds.length == index + 1
                );
              });
            } else {
             var id = $(clickedEvent).attr("data-id");
              $("#actionItemForm").find("#qualityId").val(_id);
              $("#actionItemForm").find("#caseNumber").val(caseNumber);
              $("#actionItemForm").find("#caseVersion").val(caseVersion);
              $("#actionItemForm").find("#errorType").val(errorType);
              $("#actionItemForm").find("#dataTypeAi").val(dataType);
              $("#actionItemForm").find("#appType").val(callingAppName);
              $("#actionItemForm").find("#qualityId").val(id);
              save_action_item(
                null,
                actionItemModalObj,
                callingAppName,
                clickedEvent
              );
            }
          } else if (
            typeof callingAppName != "undefined" &&
            (callingAppName == QUALITY_MODULE_CAPA ||
              callingAppName == PV_CENTRAL_CAPA)
          ) {
            $("#actionItemForm").find("#appType").val(callingAppName);
            $("#actionItemForm").find("#capaId").val(capaId);
            save_action_item(
              null,
              actionItemModalObj,
              callingAppName,
              clickedEvent
            );
          } else if (
            typeof callingAppName != "undefined" &&
            (callingAppName == DRILLDOWN_RECORD ||
              callingAppName == IN_DRILLDOWN_RECORD)
          ) {
            if (selectedIds.length > 0 && selectedIds.length != 0) {
              $.each(selectedIds, function (index, jsonObject) {
                $("#actionItemForm").find("#tenantId").val(jsonObject.tenantId);
                $("#actionItemForm")
                  .find("#masterCaseId")
                  .val(jsonObject.caseId);
                $("#actionItemForm")
                  .find("#masterVersionNum")
                  .val(jsonObject.versionNum);
                $("#actionItemForm").find("#senderId").val(jsonObject.senderId);
                $("#actionItemForm")
                  .find("#processedReportId")
                  .val(jsonObject.processedReportId);
                $("#actionItemForm")
                  .find("#configurationId")
                  .val(configurationId);
                $("#actionItemForm").find("#appType").val(callingAppName);
                save_action_item(
                  null,
                  actionItemModalObj,
                  callingAppName,
                  null,
                  selectedIds.length == index + 1
                );
              });
            } else {
              $("#actionItemForm").find("#tenantId").val(tenantId);
              $("#actionItemForm").find("#masterCaseId").val(masterCaseId);
              $("#actionItemForm")
                .find("#processedReportId")
                .val(processedReportId);
              $("#actionItemForm")
                .find("#configurationId")
                .val(configurationId);
              $("#actionItemForm").find("#senderId").val(senderId);
              $("#actionItemForm").find("#isInbound").val(isInbound);
              $("#actionItemForm")
                .find("#masterVersionNum")
                .val(masterVersionNum);
              $("#actionItemForm").find("#appType").val(callingAppName);
              save_action_item(
                null,
                actionItemModalObj,
                callingAppName,
                null,
                true
              );
            }
          } else if (typeof callingAppName != "undefined") {
            $("#actionItemForm")
              .find("#executedReportId")
              .val(executedReportId);
            $("#actionItemForm").find("#sectionId").val(sectionId);
            $("#actionItemForm").find("#publisherId").val(publisherId);
            $("#actionItemForm").find("#appType").val(callingAppName);
            save_action_item(null, actionItemModalObj, callingAppName);
          }
        }
      });
    //Bind the click event on save button click
    actionItemModalObj
      .find(".close-action-item-model")
      .on("click", function () {
        bind_close_model_event(actionItemModalObj);
      });
    initTexareaRemainingChar($("#actionItemModal").find("#description"));
    initTexareaRemainingChar($("#actionItemModal").find("#comment"));
  };

  var save_action_item = function (
    calledFromList,
    actionItemModalObj,
    callingAppName,
    clickedEvent,
    reloadRod
  ) {
    if (!validateBeforeSave(true)) {
      actionItemModalObj.find(".save-action-item").attr("disabled", false);
      return;
    }

    var parameters = actionItemModalObj.find("#actionItemForm").serialize();
    if (
      actionItemModalObj.find("#actionCategory").attr("disabled") != undefined
    ) {
      parameters =
        parameters +
        "&actionCategory=" +
        actionItemModalObj.find("#actionCategory").select2("val");
    }
    if(typeof actionItemHostPage != 'undefined' && actionItemHostPage == "dashboard"){
            parameters = parameters + '&aIHostPage=' + actionItemHostPage
        }
        var assignedToUser = actionItemModalObj.find("#assignedToName").val();

    if (!assignedToUser) {
      var assignedToData = actionItemModalObj
        .find("#assignedTo")
        .select2("data");
      assignedToUser = assignedToData ? assignedToData.text : "";
    }
    $.ajax({
      url: saveActionItemUrl,
      type: "post",
      data: parameters,
    })
      .fail(function (err) {
        actionItemModalObj.find(".save-action-item").attr("disabled", false);
        $("#actionItemModal .alert-danger").removeClass("hide");
        var fields = err.responseJSON.message;
        for (var index = 0; index < fields.length; index++) {
          var field = fields[index];
          $("#" + field).attr("style", "border-color:#a94442");
        }
        $(".alert-danger").show();
      })
      .done(function (data) {
        //Common
        actionItemModalObj.modal("hide");
        clear_action_item_modal(actionItemModalObj);

        if (calledFromList) {
          $(".alert-success").removeClass("hide");
          if (
            typeof callingAppName != "undefined" &&
            callingAppName == PR_Calendar
          ) {
            $("#calendar").fullCalendar("refetchEvents");
          } else {
            actionItem.actionItemList.reload_action_item_table();
          }
          //Show the notification on the app screen that action item is created.

          $("#successNotification").html(
            $.i18n._("actionItem.create.success", encodeToHTML(assignedToUser))
          );
          $(".alert-danger").addClass("hide");
        } else {
          if (
            typeof callingAppName != "undefined" &&
            (callingAppName == PERIODIC_REPORT ||
              callingAppName == ADHOC_REPORT)
          ) {
            $("form").detach();
            location.reload();
          }
          if (
            typeof callingAppName != "undefined" &&
            callingAppName == QUALITY_MODULE
          ) {
            $(".creationButton").trigger("savedAI");
            reloadRodTable($.i18n._("ai.create.success"));
          }
          if (callingAppName == ACTION_PLAN) {
            reloadActionPlanTable();
          }
          if (
            typeof callingAppName != "undefined" &&
            (callingAppName == QUALITY_MODULE_CAPA ||
              callingAppName == PV_CENTRAL_CAPA)
          ) {
            reloadCapaActionList($.i18n._("ai.create.success"));
          }
          if (
            typeof callingAppName != "undefined" &&
            (callingAppName == DRILLDOWN_RECORD ||
              callingAppName == IN_DRILLDOWN_RECORD)
          ) {
            showLoader();
            if (typeof reloadRod != "undefined" && reloadRod == true) {
              reloadRodTable($.i18n._("ai.success"));
              hideLoader();
            }
          }
        }
      });
  };

  var bind_close_model_event = function (actionItemModalObj) {
    //Hide the action item model
    actionItemModalObj.modal("hide");

    //Clear the action item modal
    clear_action_item_modal(actionItemModalObj);
    $(".alert-danger").hide();
    if (calledFromList) {
      //Clear up the old error notifications and applied css.
      $("#actionItemForm")
        .find(".form-control")
        .attr(
          "style",
          "background-color: #fff;background-image: none;border: 1px solid #ccc"
        );
    }
  };

  var clear_action_item_modal = function (actionItemModalObj) {
    actionItemModalObj.find("#actionItemForm")[0].reset();
    actionItemModalObj.find("#actionCategory").trigger("change");
    actionItemModalObj.find("#actionCategory").val(null).trigger("change");
    actionItemModalObj.find("#assignedTo").val(null).trigger("change");
    actionItemModalObj.find("#priority").val(null).trigger("change");
    actionItemModalObj.find('#rptRequestId').val('');
    clearFormInputsChangeFlag(actionItemModalObj.find("#actionItemForm")[0]);
    $("#fieldErrorMessage").show();
    $("#otherErrorMessage").hide();
    $(".update-action-item").prop("disabled", false);
    $(".alert-danger").hide();
    actionItemModalObj.find("#completionDate").removeAttr("style","border-color:#a94442");
        initTexareaRemainingChar($("#actionItemModal").find("#description"));
    initTexareaRemainingChar($("#actionItemModal").find("#comment"));
  };

  //Function for toggling the element disabling. Based on the passed argument the elements will be disabled.
  var toggle_element_disable = function (actionItemModalObj, disable) {
    actionItemModalObj.find(".form-control").attr("disabled", disable);
    actionItemModalObj.find(".actionItemRadios ").attr("disabled", disable);
  };

  //This method fills up the action item modal elements value.
  var fillUpActionItemModal = function (modalObj, actionItemResult) {
    modalObj.find("#aiVersion").val(actionItemResult.version);
    modalObj
      .find("#description")
      .val(decodeFromHTML(actionItemResult.description));
    modalObj.find("#comment").val(decodeFromHTML(actionItemResult.comment));
    modalObj
      .find("#actionCategory")
      .val(actionItemResult.actionCategory)
      .trigger("change");
    modalObj.find("#priority").val(actionItemResult.priority).trigger("change");
    modalObj.find("#status").val(actionItemResult.status).trigger("change");
    modalObj
      .find("#assignedTo")
      .val(actionItemResult.assignedToId)
      .trigger("change");
    modalObj.find("#createdBy").html(actionItemResult.createdBy);
    modalObj
      .find("#completionDate")
      .val(
        actionItemResult.completionDate
          ? moment(actionItemResult.completionDate).format(
              DEFAULT_DATE_DISPLAY_FORMAT
            )
          : ""
      );
    modalObj
      .find("#dueDate")
      .val(
        actionItemResult.dueDate
          ? moment(actionItemResult.dueDate).format(DEFAULT_DATE_DISPLAY_FORMAT)
          : ""
      );
    modalObj.find("#dateCreated").html(actionItemResult.dateCreated);

    //Hide div if No Case No is associated with this action item
    if (
      actionItemResult.associatedCaseNumber &&
      actionItemResult.associatedCaseNumber != ""
    ) {
      modalObj
        .find("#associatedCaseNumber")
        .html(
          "<a href='" +
            caseDataLinkAiUrl +
            "?type=" +
            actionItemResult.associatedCaseDataType +
            "&caseNumber=" +
            actionItemResult.associatedCaseNumber +
            "&id=" +
            actionItemResult.associatedId +
            "&versionNumber=" +
            actionItemResult.associatedCaseVersion +
            "' " +
            ">" +
            actionItemResult.associatedCaseNumber +
            "</a> "
        );
      modalObj.find("#associatedCaseNoDiv").show();
    } else {
      modalObj.find("#associatedCaseNoDiv").hide();
    }

    //Hide div if No Case No is associated with this action item
    if (
      actionItemResult.associatedIssueNumber &&
      actionItemResult.associatedIssueNumber != ""
    ) {
      if (
        actionItemResult.appType ===
        $.i18n._("app.actionItemAppType.PV_CENTRAL_CAPA")
      ) {
        modalObj
          .find("#associatedIssueNumber")
          .html(
            "<a href='" +
              viewPVCIssueNumberLinkUrl +
              "/" +
              actionItemResult.associatedIssueId +
              "' " +
              ">" +
              actionItemResult.associatedIssueNumber +
              "</a> "
          );
      } else {
        modalObj
          .find("#associatedIssueNumber")
          .html(
            "<a href='" +
              viewIssueNumberLinkUrl +
              "/" +
              actionItemResult.associatedIssueId +
              "' " +
              ">" +
              actionItemResult.associatedIssueNumber +
              "</a> "
          );
      }
      modalObj.find("#associatedIssueNoDiv").show();
    } else {
      modalObj.find("#associatedIssueNoDiv").hide();
    }

    if (actionItemResult.executedReportConfigurationId) {
      modalObj
        .find("#link")
        .attr(
          "href",
          modalObj.find("#link").attr("data-base-url") +
            "/" +
            actionItemResult.executedReportConfigurationId
        );
      modalObj
        .find("#link")
        .html(encodeToHTML(actionItemResult.executedReportConfigurationName));
      modalObj.find("#link").css("word-wrap", "break-word");
      modalObj.find("#linkDiv").show();
    } else {
      modalObj.find("#linkDiv").hide();
    }
    if (actionItemResult.configurationId) {
      // For Reason of Delay - Adhoc Report
      actionItemResult.actionCategory == PERIODIC_REPORT
        ? configLink.setAttribute("data-base-url", baseUrlAggregate)
        : configLink.setAttribute("data-base-url", baseUrlAdhoc);
      modalObj
        .find("#configLink")
        .attr(
          "href",
          modalObj.find("#configLink").attr("data-base-url") +
            "/" +
            actionItemResult.configurationId
        );
      modalObj
        .find("#configLink")
        .html(encodeToHTML(actionItemResult.configurationName));
      modalObj.find("#configLink").css("word-wrap", "break-word");
      modalObj.find("#linkConfigDiv").show();
    } else {
      modalObj.find("#linkConfigDiv").hide();
    }
    if (actionItemResult.reportRequestId) {
      modalObj
        .find("#requestLink")
        .attr(
          "href",
          modalObj.find("#requestLink").attr("data-base-url") +
            "/" +
            actionItemResult.reportRequestId
        );
      modalObj.find("#rptRequestId").val(actionItemResult.reportRequestId);
      modalObj
        .find("#requestLink")
        .html(encodeToHTML(actionItemResult.reportRequestName));
      modalObj.find("#requestLink").css("word-wrap", "break-word");
      modalObj.find("#linkRequestDiv").show();
    } else {
      modalObj.find("#linkRequestDiv").hide();
    }
    if (actionItemResult.drilldownRecordId) {
      modalObj
        .find("#drilldowLink")
        .attr(
          "href",
          modalObj.find("#drilldowLink").attr("data-base-url") +
            "/" +
            actionItemResult.drilldownReportId +
            "?cllRecordId=" +
            actionItemResult.drilldownRecordId
        );
      modalObj
        .find("#drilldowLink")
        .html(
          encodeToHTML(actionItemResult.drilldownReportName) +
            " - " +
            actionItemResult.drilldownRecordCaseNum
        );
      modalObj.find("#drilldowLink").css("word-wrap", "break-word");
      modalObj.find("#linkDrilldownDiv").show();
    } else {
      modalObj.find("#linkDrilldownDiv").hide();
    }
  };

  //Function for action item view screen.
  var fillActionItemModalElements = function (
    actionItemId,
    actionItemModalObj
  ) {
    $.ajax({
      url: viewActionItemUrl,
      data: "actionItemId=" + actionItemId,
      dataType: "json",
    })
      .fail(function (err) {
        console.log(err);
      })
      .done(function (data) {
        // dates received here should be in UTC as it is being fetched from database, so converting it to localized format
        data.dateCreated = data.dateCreated
          ? moment
              .utc(data.dateCreated)
              .tz(userTimeZone)
              .format(DATEPICKER_FORMAT_AM_PM)
          : "";

        fillUpActionItemModal(actionItemModalObj, data);
      });
  };

  var delete_action_item = function (
    actionItemId,
    calledFromList,
    callingAppName
  ) {
    var modal = $("#deleteModal");
    modal.modal("show");
    $("#deleteDlgErrorDiv").hide();
    modal
      .find("#deleteModalLabel")
      .text(
        $.i18n._("modal.delete.title", $.i18n._("app.label.action.app.name"))
      );
    modal
      .find("#nameToDelete")
      .text($.i18n._("deleteThis", $.i18n._("app.label.action.app.name")));
    $("#deleteButton").off();
    $("#deleteButton").on("click", function () {
      if (!$("#deleteJustification").val().trim()) {
        $("#deleteDlgErrorDiv").show();
      } else {
        $("#deleteDlgErrorDiv").hide();
        $.ajax({
          url: deleteActionItemUrl,
          method: "post",
          data:
            "actionItemId=" +
            actionItemId +
            "&deleteJustification=" +
            $("#deleteJustification").val(),
          dataType: "html",
        })
          .fail(function (err) {
            console.log(err);

            if (calledFromList) {
              $(".alert-success").addClass("hide");
              $("#errorNotification").html(
                $.i18n._("app.label.action.app.delete.fail")
              );
              $(".alert-danger").removeClass("hide");
            }
          })
          .done(function (data) {
            if (calledFromList) {
              if (
                typeof callingAppName != "undefined" &&
                callingAppName == PR_Calendar
              ) {
                $("div[id^=calendar]").fullCalendar("refetchEvents");
                $("#actionItemModal").modal("hide");
              } else {
                actionItem.actionItemList.reload_action_item_table();
              }
              $(".alert-success").removeClass("hide");
              $("#successNotification").html(
                $.i18n._("app.label.action.app.delete.success")
              );
              $(".alert-danger").addClass("hide");
              // location.reload();
            } else if (
              typeof callingAppName != "undefined" &&
              (callingAppName == QUALITY_MODULE_CAPA ||
                callingAppName == PV_CENTRAL_CAPA)
            ) {
              reloadCapaActionList(
                $.i18n._("app.label.action.app.delete.success")
              );
            } else if (
              typeof callingAppName != "undefined" &&
              callingAppName == DRILLDOWN_RECORD
            ) {
              reloadRodTable($.i18n._("ai.success"));
            }
          });

        modal.modal("hide");
        $("#deleteJustification").val("");
      }
    });
  };

  var view_action_item_list = function (
    hasAccessOnActionItem,
    calledFromList,
    callingAppName,
    qualityDetails
  ) {
    //Show modal
    var actionItemListModalObj = $("#actionItemListModal");
    var buildSearchData = function (d) {
      //d.length = 5;
      d.start = 0;
      if (d.order.length > 0) {
        d.direction = d.order[0].dir;
        //Column header mData value extracting
        d.sort = d.columns[d.order[0].column].data;
      }
      if (qualityDetails === undefined) {
        if (callingAppName === DRILLDOWN_RECORD) {
          d.masterCaseId = masterCaseId;
          d.processedReportId = processedReportId;
          d.configurationId = configurationId;
          d.tenantId = tenantId;
        } else if (callingAppName === IN_DRILLDOWN_RECORD) {
          d.masterCaseId = masterCaseId;
          d.tenantId = tenantId;
          d.masterVersionNum = masterVersionNum;
          d.senderId = senderId;
        } else {
          d.executedReportId = executedReportId;
          d.sectionId = sectionId;
          d.publisherId = publisherId;
        }
      } else {
        d.caseNumber = qualityDetails.caseNumber;
        d.errorType = qualityDetails.errorType;
        d.dataType = qualityDetails.dataType;
      }
      d.parentEntityKey = parentEntityKey;
      d.from = dateRangeFrom;
      d.to = dateRangeTo;
      d.filterType = "Executed Report Related Action Items";
    };
    actionItemListModalObj.off().one("shown.bs.modal", function (event) {
      if ($.fn.DataTable.isDataTable("#actionItemList")) {
        $("#actionItemList").DataTable().destroy();
      }
      var dataTable = $("#actionItemList").DataTable({
        customProcessing: true, //handled using processing.dt event
        serverSide: true,
        searching: false,
        paging: false,
        info: false,
        ajax: {
          url: listActionItemUrl,
          data: buildSearchData,
        },
        order: [
          [1, "asc"],
          [2, "desc"],
        ],
        columns: [
          {
            mData: "assignedTo",
            bSortable: false,
          },
          {
            data: "description",
            render: function (data, type, row) {
              var truncatedDes = "";
              if (row.description.length > 30) {
                truncatedDes = row.description.substring(0, 29) + "...";
              } else {
                truncatedDes = row.description;
              }

              return (
                '<a href="#" data-evt-clk=\'{\"method\": \"edit_action_item\", \"params\": [\"' + hasAccessOnActionItem + '\",\"' + row.actionItemId + '\",' + calledFromList + ',\"'+ callingAppName + '\", null]}\' >' +
                encodeToHTML(truncatedDes) +
                "</a>"
              );
            },
          },
          {
            data: "dueDate",
            className: "dataTableColumnCenter",
            render: function (data, type, full) {
              return data
                ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT)
                : "";
            },
          },
          {
            data: "priority",
            render: function (data, type, full) {
              var prioritySpan = document.createElement("span");
              prioritySpan.classList.add("label");
              switch (data) {
                case "HIGH":
                  prioritySpan.classList.add("label-danger");
                  break;
                case "MEDIUM":
                  prioritySpan.classList.add("label-warning");
                  break;
                default:
                  prioritySpan.classList.add("label-default");
                  break;
              }
              prioritySpan.appendChild(document.createTextNode(data));
              return prioritySpan.outerHTML;
            },
          },
          {
            mData: "status",
            aTargets: ["status"],
            mRender: function (data, type, full) {
              return data ? $.i18n._("status_enum." + data) : "";
            },
          },
        ],
      });
      //Bind the click event on save button click
      actionItemListModalObj
        .find(".close-action-item-model")
        .on("click", function () {
          actionItemListModalObj.modal("hide");
        });
    }).on('draw.dt', function () {
      initEvtClk();
    });
    actionItemListModalObj.modal("show");
  };

  var view_action_item = function (actionItemId) {
    //Show modal
    var actionItemModalObj = $("#actionItemModal");
    actionItemModalObj.modal("show");

    //Toggle the modal window buttons.
    actionItemModalObj.find(".excelExport").removeClass("hide");
    actionItemModalObj.find("#creationScreenButton").addClass("hide");
    actionItemModalObj.find("#editScreenButton").addClass("hide");
    actionItemModalObj.find("#viewScreenButton").removeClass("hide");

    //Fill up the action item modal elements.
    fillActionItemModalElements(actionItemId, actionItemModalObj);

    toggle_element_disable(actionItemModalObj, true);

    actionItemModalObj.find("#actionItemId").val(actionItemId);

    //Bind the click event on save button click
    actionItemModalObj
      .find(".close-action-item-model")
      .on("click", function () {
        bind_close_model_event(actionItemModalObj);
      });
    //Make the datepicker.
    actionItemModalObj.find("#dueDateDiv").datepicker({
      allowPastDates: false,
      momentConfig: {
        format: DEFAULT_DATE_DISPLAY_FORMAT,
      },
    });
    actionItemModalObj.find("#completionDateDiv").datepicker({
      allowPastDates: true,
      momentConfig: {
        format: DEFAULT_DATE_DISPLAY_FORMAT,
      },
    });
    actionItemModalObj.find("#dueDate").siblings("div.input-group-btn").find(".btn.dropdown-toggle").attr('disabled', 'disabled');
        actionItemModalObj.find("#completionDate").siblings("div.input-group-btn").find(".btn.dropdown-toggle").attr('disabled', 'disabled');
    };

    var edit_action_item = function (hasAccessOnActionItem, actionItemId, isCalledFromList, callingAppName, index, clickedEvent) {

        var actionItemModalObj = $('#actionItemModal');
        actionItemModalObj.modal('show');
        $("#actionItemModal").find(".alert-danger").css("display", "none");

        var actionItemListModalObj = $('#actionItemListModal');
        actionItemListModalObj.modal('hide');

        //Toggle the modal window buttons.
        actionItemModalObj.find('.excelExport').addClass('hide');
        actionItemModalObj.find('#creationScreenButton').addClass('hide');
        actionItemModalObj.find('#editScreenButton').removeClass('hide');
        actionItemModalObj.find('#viewScreenButton').addClass('hide');
        actionItemModalObj.find('#deleteActionItem').addClass('hide');
        actionItemModalObj.find("#dueDate").siblings("div.input-group-btn").find(".btn.dropdown-toggle").removeAttr('disabled');
        actionItemModalObj.find("#completionDate").siblings("div.input-group-btn").find(".btn.dropdown-toggle").removeAttr('disabled');
        initializePastDatesNotAllowedDatePicker();

        $.each(actionItemModalObj.find(".select2-box"), function () {
          $(this).select2({
            dropdownParent: actionItemModalObj
          });
        });

        //Make the datepicker.
        actionItemModalObj.find('#dueDateDiv').datepicker({
            allowPastDates: false,
            momentConfig: {
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
    });

    actionItemModalObj.find("#completionDateDiv").datepicker({
      allowPastDates: true,
      momentConfig: {
        format: DEFAULT_DATE_DISPLAY_FORMAT,
      },
    });

    if (isCalledFromList || actionItemId) {
      //Fill up the action item modal elements.
      fillActionItemModalElements(actionItemId, actionItemModalObj);
    } else {
      if (
        typeof callingAppName != "undefined" &&
        callingAppName == REPOERT_REQUEST
      ) {
        var actionItemResult = {
          comment: document.getElementById("actionItems[" + index + "].comment")
            .value,
          description: document.getElementById(
            "actionItems[" + index + "].description"
          ).value,
          actionCategory: document.getElementById(
            "actionItems[" + index + "].actionCategory"
          ).value,
          priority: document.getElementById(
            "actionItems[" + index + "].priority"
          ).value,
          status: document.getElementById("actionItems[" + index + "].status")
            .value,
          assignedToId: document.getElementById(
            "actionItems[" + index + "].assignedTo"
          ).value,
          createdBy: $("#loggedInUser").html(),
          completionDate: document.getElementById(
            "actionItems[" + index + "].completionDate"
          ).value,
          dateCreated: $("#currentDate").val(),
          dueDate: document.getElementById("actionItems[" + index + "].dueDate")
            .value,
        };

        //This method fills up the action item modal elements value.
        fillUpActionItemModal(actionItemModalObj, actionItemResult);
      }
    }

    //Enable all the elements.
    toggle_element_disable(actionItemModalObj, false);

    actionItemModalObj.find("#actionItemId").val(actionItemId);
    actionItemModalObj.find("#index").val(index);

    //Bind the click event on save button click
    actionItemModalObj
      .find(".close-action-item-model")
      .on("click", function () {
        bind_close_model_event(actionItemModalObj);
      });

    //If the method is not called from the list view then we need to do following operations.
    if (!isCalledFromList) {
      if (
        typeof callingAppName != "undefined" &&
        callingAppName == REPOERT_REQUEST
      ) {
        var description = decodeFromHTML(
          $("#actionItemModal").find("#description").val()
        );
        $("#actionItemModal").find("#description").val(description);
        //Freeze the action category with default selected value as 'Report Request'
        actionItemModalObj
          .find("#actionCategory")
          .val(actionItemModalObj.find("#actionCategory").val())
          .trigger("change");

        //Bind the event on the update button click.
        actionItemModalObj.find(".update-action-item").off("click");

        actionItemModalObj.find(".update-action-item").on("click", function () {
          var actionItemObjId = actionItemModalObj.find("#actionItemId").val();

          if (actionItemObjId) {
            update_action_item(
              isCalledFromList,
              actionItemModalObj,
              callingAppName,
              undefined,
              index
            );
          } else {
            var isValid = validateContent();
            var message = $.i18n._("app.actionItem.fill.all.fields");
            if (isValid) {
              var indexVal = actionItemModalObj.find("#index").val();
              //  Call the add action item code.
              reportRequest.reportRequestActionItems.modifyAddedActionItem(
                indexVal,
                actionItemModalObj
              );
              actionItemModalObj.modal("hide");
              $("#actionItemModal").data("modal", null);
              clear_action_item_modal(actionItemModalObj);
              $("#actionItemModal")
                .find(".alert-danger")
                .css("display", "none");
            } else {
              showErrorInActionItem(message);
            }
          }
        });
      } else if (
        typeof callingAppName != "undefined" &&
        (callingAppName == PERIODIC_REPORT || callingAppName == ADHOC_REPORT)
      ) {
        actionItemModalObj.find("#actionCategory").attr("disabled", true);

        //Bind the event on the update button click.
        actionItemModalObj.find(".update-action-item").on("click", function () {
          update_action_item(
            isCalledFromList,
            actionItemModalObj,
            callingAppName
          );
        });
      } else if (
        typeof callingAppName != "undefined" &&
        (callingAppName == QUALITY_MODULE ||
          callingAppName == QUALITY_MODULE_CAPA ||
          callingAppName == PV_CENTRAL_CAPA ||
          callingAppName == ACTION_PLAN ||
          callingAppName == DRILLDOWN_RECORD ||
          callingAppName == IN_DRILLDOWN_RECORD)
      ) {
        actionItemModalObj.find("#actionCategory").attr("disabled", true);
        actionItemModalObj.find(".update-action-item").off("click");
        //Bind the event on the update button click.
        actionItemModalObj.find(".update-action-item").on("click", function () {
          update_action_item(
            isCalledFromList,
            actionItemModalObj,
            callingAppName,
            clickedEvent
          );
        });
      }
    }
    toggle_actionItemModal_fields(hasAccessOnActionItem);
    initTexareaRemainingChar($("#actionItemModal").find("#description"));
    initTexareaRemainingChar($("#actionItemModal").find("#comment"));
  };
  var toggle_actionItemModal_fields = function (hasAccessOnActionItem) {
    if (hasAccessOnActionItem)
      $("#actionItemModal .needsActionItemRole").prop("disabled", false);
    else $("#actionItemModal .needsActionItemRole").prop("disabled", true);
  };

  var showErrorInActionItem = function (message) {
    $(".alert-danger").alert("close");
    if (message != undefined && message != "")
      $(".action-item-modal-body").prepend(
        '<div class="alert alert-danger alert-dismissable">' +
          '<button type="button" class="close" ' +
          'data-dismiss="alert" aria-hidden="true">' +
          "&times;" +
          "</button>" +
          message +
          "</div>"
      );
  };

  var update_action_item = function (
    isCalledFromList,
    actionItemModalObj,
    callingAppName,
    clickedEvent,
    index
  ) {
    if (!validateBeforeSave()) {
      actionItemModalObj.find(".update-action-item").attr("disabled", false);
      return;
    }

    var parameters = actionItemModalObj.find("#actionItemForm").serialize();
    var assignedToData = actionItemModalObj.find("#assignedTo").select2("data");
    var assignedToUser = assignedToData ? assignedToData[0].text : "";

    if (
      actionItemModalObj.find("#actionCategory").attr("disabled") != undefined
    ) {
      parameters =
        parameters +
        "&actionCategory=" +
        actionItemModalObj.find("#actionCategory").select2("val");
    }
      if(typeof actionItemHostPage != 'undefined' && actionItemHostPage == "dashboard"){
          parameters = parameters + '&aIHostPage=' + actionItemHostPage
      }
    $.ajax({
      url: updateActionItemUrl,
      type: "post",
      data: parameters,
      dataType: "html",
    })
      .fail(function (err) {
        $("#actionItemModal .alert-danger").removeClass("hide");
        actionItemModalObj.find(".save-action-item").attr("disabled", false);
        if (err.status === 400) {
          var fields = err.responseJSON.message;
          for (var index = 0; index < fields.length; index++) {
            var field = fields[index];
            $("#" + field).attr("style", "border-color:#a94442");
          }
          $("#fieldErrorMessage").show();
          $(".alert-danger").show();
          setTimeout(function () {
            for (var index = 0; index < fields.length; index++) {
              var field = fields[index];
              $("#" + field).attr("style", "none");
            }
              $("#fieldErrorMessage").hide();
              $(".alert-danger").hide();
          }, 2000);
        } else {
          $("#fieldErrorMessage").hide();
          $("#otherErrorMessage").html(err.responseJSON.message);
          $("#otherErrorMessage").show();
          $(".alert-danger").show();
          $(".update-action-item").prop("disabled", true);
        }
      })
      .done(function (data) {
        actionItemModalObj.modal("hide");
        actionItem.actionItemModal.clear_action_item_modal(actionItemModalObj);
        if (
          typeof actionItemHostPage != "undefined" &&
          actionItemHostPage == "report"
        ) {
          $("form").detach();
          location.reload();
        }
        if (isCalledFromList) {
          if (
            typeof callingAppName != "undefined" &&
            callingAppName == PR_Calendar
          ) {
            $("#calendar").fullCalendar("refetchEvents");
          } else {
            actionItem.actionItemList.reload_action_item_table();
          }
          $(".alert-success").removeClass("hide");
          $("#successNotification").html(
            $.i18n._("actionItem.update.success", encodeToHTML(assignedToUser))
          );
          $(".alert-danger").addClass("hide");
        } else {
          if (
            typeof callingAppName != "undefined" &&
            (callingAppName == DRILLDOWN_RECORD ||
              callingAppName == IN_DRILLDOWN_RECORD)
          ) {
            reloadRodTable($.i18n._("ai.success"));
          } else if (
            typeof callingAppName != "undefined" &&
            (callingAppName == QUALITY_MODULE_CAPA ||
              callingAppName == PV_CENTRAL_CAPA)
          ) {
            reloadCapaActionList($.i18n._("ai.success"));
          } else if (
            typeof callingAppName != "undefined" &&
            callingAppName == ACTION_PLAN
          ) {
            reloadActionPlanTable();
          } else if (
            typeof callingAppName != "undefined" &&
            callingAppName == QUALITY_MODULE
          ) {
            $(".update-action-item").trigger("updatedAI");
            reloadRodTable($.i18n._("ai.success"));
          } else if (
            typeof callingAppName == "undefined" ||
            (typeof callingAppName != "undefined" &&
              (callingAppName != REPOERT_REQUEST ||
                callingAppName != ADHOC_REPORT))
          ) {
            if (location.href.indexOf("pvp") > -1) {
              $("form").detach();
              location.reload();
            }
            $("#success").html(data.message);
            window.scrollTo(0, 0);
            $(".reportActionItemDiv")
              .find(".panelDiv[data-id=" + index + "]")
              .find(".assignedTo")
              .html("Assigned to: " + encodeToHTML(assignedToUser));
            $(".reportActionItemDiv")
              .find(".panelDiv[data-id=" + index + "]")
              .find(".text-muted")
              .html(" with due date on : " + data.actionItemId.dueDate);
            $(".reportActionItemDiv")
              .find(".panelDiv[data-id=" + index + "]")
              .find('span[id="actionItems[' + index + '].display"]')
              .html(encodeToHTML(data.actionItemId.description));
            var statusString = $(".reportActionItemDiv")
              .find(".panelDiv[data-id=" + index + "]")
              .find(".statusString");
            statusString.attr(
              "style",
              getStatusColor(data.actionItemId.status)
            );
            statusString.html(data.actionItemId.status);
          }
        }
      });
  };

  var getStatusColor = function (status) {
    var styleAttr = "";
    if (status == STATUS_ENUM.OPEN) {
      styleAttr = "color:blue";
    } else if (
      status == STATUS_ENUM.IN_PROGRESS ||
      status == STATUS_ENUM.NEED_CLARIFICATION
    ) {
      styleAttr = "color:orange";
    } else if (status == STATUS_ENUM.CLOSED) {
      styleAttr = "color:green";
    }
    return styleAttr;
  };

  var set_executed_report_id = function (passedId) {
    executedReportId = passedId;
  };
  var set_section_id = function (passedId) {
    sectionId = passedId;
  };

  var set_publisher_id = function (passedId) {
    publisherId = passedId;
  };

  var set_drilldown_record_data = function (
    caseId,
    reportId,
    tenant,
    configId
  ) {
    masterCaseId = caseId;
    processedReportId = reportId;
    configurationId = configId;
    tenantId = tenant;
  };

  var set_in_drilldown_record_data = function (
    caseId,
    versionNum,
    tenant,
    sender
  ) {
    masterCaseId = caseId;
    tenantId = tenant;
    masterVersionNum = versionNum;
    senderId = sender;
  };

  var set_parent_entity_key = function (parentEntityKeyValue, from, to) {
    parentEntityKey = parentEntityKeyValue;
    dateRangeFrom = from;
    dateRangeTo = to;
  };

  return {
    init_action_item_modal: init_action_item_modal,
    clear_action_item_modal: clear_action_item_modal,
    bind_close_model_event: bind_close_model_event,
    toggle_element_disable: toggle_element_disable,
    delete_action_item: delete_action_item,
    view_action_item: view_action_item,
    edit_action_item: edit_action_item,
    update_action_item: update_action_item,
    set_executed_report_id: set_executed_report_id,
    view_action_item_list: view_action_item_list,
    set_drilldown_record_data: set_drilldown_record_data,
    set_in_drilldown_record_data: set_in_drilldown_record_data,
    set_parent_entity_key: set_parent_entity_key,
    set_section_id: set_section_id,
    set_publisher_id: set_publisher_id,
  };
})();

$(document).on("click", "#excelSingleAI", function (e) {
  var actionItemId = $("#actionItemModal").find("#actionItemId").val();
  $("#excelData").val(actionItemId);
  $("#excelExport").trigger("submit");
});

function validateContent() {
  var valid = false;
  var actionItemModal = $("#actionItemModal");
  actionItemModal.find("#completionDate").removeAttr("style","border-color:#a94442");
  var dueDate = actionItemModal.find("#dueDate").val();
  var assignedToName = actionItemModal.find("#assignedTo").val() ?? actionItemModal.find("#assignedTo").attr("data-value");
  var completionDate = actionItemModal.find("#completionDate").val();
  var status = actionItemModal.find("#status").val();
  var description = actionItemModal.find("#description").val();
  var priority = actionItemModal.find("#priority").val();

  if (dueDate && assignedToName && description) {
    valid = !(status == "CLOSED" && !completionDate);
  }
  if (status == "CLOSED" && !completionDate) {
    actionItemModal.find('#completionDate').attr("style", "border-color:#a94442");
  }
  if (!priority || !dueDate || !assignedToName || !description) valid = false;

  return valid;
}

function validateBeforeSave(isCreateMode) {
  var isValid = true;
  if (!validateActionItemDates(isCreateMode)) {
    isValid = false;
  }
  if (!validateActionItemRequired()) {
    isValid = false;
  }
  if (isValid) {
    $("#actionItemModal .alert-danger").addClass("hide");
  }
  return isValid;
}

function validateActionItemDates(isCreateMode) {
  var alertDanger = $("#actionItemModal .alert-danger");
  alertDanger.find("#otherErrorMessage").hide();
    $('#completionDate').attr("style", "none");

  var actionItemModal = $("#actionItemModal");
  var dueDate = actionItemModal.find("#dueDate").val();
  var completionDate = actionItemModal.find("#completionDate").val();

  var validationMessage = "";
  var validationMessageDelimiter = "<br>";

  if (
    dueDate &&
    !moment(dueDate, DEFAULT_DATE_DISPLAY_FORMAT, true).isValid()
  ) {
    validationMessage +=
      $.i18n._("due.date.format.error") + validationMessageDelimiter;
  } else if (isCreateMode) {
    if (
      dueDate &&
      moment(dueDate, DEFAULT_DATE_DISPLAY_FORMAT) < moment().startOf("day")
    ) {
      validationMessage +=
        $.i18n._("due.date.before.now.error") + validationMessageDelimiter;
    }
  }

  if (
    completionDate &&
    !moment(completionDate, DEFAULT_DATE_DISPLAY_FORMAT, true).isValid()
  ) {
    validationMessage +=
      $.i18n._("completion.date.format.error") + validationMessageDelimiter;
  } else if (isCreateMode) {
    if (
      completionDate &&
      moment(completionDate, DEFAULT_DATE_DISPLAY_FORMAT) <
        moment().startOf("day")
    ) {
      validationMessage +=
        $.i18n._("completion.date.before.now.error") +
        validationMessageDelimiter;
    }
  }

  if (validationMessage) {
    alertDanger.removeClass("hide");
    alertDanger.find("#otherErrorMessage").html(validationMessage).show();
    alertDanger.show();
    return false;
  }

  return true;
}

function validateActionItemRequired() {
  var alertDanger = $("#actionItemModal .alert-danger");
  alertDanger.find("#fieldErrorMessage").hide();
    $('#completionDate').attr("style", "none");

  var actionItemModal = $("#actionItemModal");
  var category = actionItemModal.find("#actionCategory").val();
  var dueDate = actionItemModal.find("#dueDate").val();
  var priority = actionItemModal.find("#priority").val();
  var assignedTo = actionItemModal.find("#assignedTo").val();
  var description = actionItemModal.find("#description").val();
  var completionDate = actionItemModal.find('#completionDate').val();
    var status = actionItemModal.find('#status').val();
    if(status=="CLOSED" && !completionDate){
       actionItemModal.find('#completionDate').attr("style", "border-color:#a94442");
        alertDanger.removeClass('hide');
        alertDanger.find('#fieldErrorMessage').show();
        alertDanger.show();
        return false;
    }if (!category || !dueDate || !priority || !assignedTo || !description) {
    alertDanger.removeClass("hide");
    alertDanger.find("#fieldErrorMessage").show();
    alertDanger.show();
    return false;
  }
  return true;
}

$(function () {
  $(document).on("click", ".alert-close", function () {
    $(this).parent().hide();
  });
});


var initEvtClk = function () {
  $("[data-evt-clk]").on('click', function(e) {
    e.preventDefault();
    const eventData = JSON.parse($(this).attr("data-evt-clk"));
    const methodName = eventData.method;
    const params = eventData.params;

    if(methodName == 'edit_action_item') {
      actionItem.actionItemModal.edit_action_item(...params);
    }
  });
}