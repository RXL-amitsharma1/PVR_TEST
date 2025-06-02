var caseListFilter = {};
var actionPlanTable;
var createActionPlanTable;
$(function () {
  if ($(".actionPlanWidgetSearchForm").length == 0) return;
  var topValues = 5;
  var periods;
  var previousNumber = 2;
  var pvc = typeof actionPlanType !== "undefined" && actionPlanType == "PVC";
  var isWidget = $(".grid-stack-item-content").length > 0;
  var groupingLabel1 = $.i18n._("app.responsibleParty.name");
  var groupingLabel2 = pvc
    ? $.i18n._("app.rc.name")
    : $.i18n._("app.observation.name");
  var groupingLabel3;

  if (isWidget) {
    $("#periodsNumber").val("1");
    previousNumber = 1;
    formTableHeader();
    $(".notForWidget").hide();
    $(".forWidget").show();
  }
  $("#dateRangeFrom")
    .parent()
    .datepicker({
      date: $("#dateRangeFrom").val(),
      allowPastDates: true,
      momentConfig: {
        format: DEFAULT_DATE_DISPLAY_FORMAT,
      },
    });
  $("#dateRangeTo")
    .parent()
    .datepicker({
      date: $("#dateRangeTo").val(),
      allowPastDates: true,
      momentConfig: {
        format: DEFAULT_DATE_DISPLAY_FORMAT,
      },
    });

  //we need to add 'break-long-word-list' class after select2 init, cause source select classes do not shared to select2
  $(".select2-box-break-long-word").select2().addClass("break-long-word-list");

  $("#dateRangeType")
    .on("change", function () {
      if ($(this).val() == "CUSTOM") {
        $(".customDateRange").show();
        $("#lastX").hide();
        $("#dateRangeTypeLabel").hide();
      } else {
        $(".customDateRange").hide();
        $("#lastX").show();
        $("#dateRangeTypeLabel").show();
      }
      $("#dateRangeTypeLabel").text($(this).select2("data")[0].text);
    })
    .trigger("change");

  $("#summaryDate")
    .parent()
    .datepicker({
      allowPastDates: true,
      momentConfig: {
        format: DEFAULT_DATE_DISPLAY_FORMAT,
      },
    });

  function formTableHeader() {
    $(".clonnedHeader").detach();
    for (var i = 1; i < previousNumber; i++) {
      var clone = $(".topToClone").clone();
      clone
        .removeClass("topToClone")
        .removeClass("topCloneAfter")
        .addClass("clonnedHeader");
      $(".topCloneAfter").after(clone);
      clone = $(".toClone").clone();
      clone
        .removeClass("toClone")
        .removeClass("toCloneAfter")
        .addClass("clonnedHeader");
      $(".toCloneAfter").after(clone);
    }
  }

  createActionPlanTable = function (periodsNumber) {
    if (periodsNumber) previousNumber = periodsNumber;
    if (actionPlanTable) {
      actionPlanTable.clear();
      actionPlanTable.destroy();
      $("#actionPlatTable tbody").empty();
      formTableHeader();
    }
    var groupping = getGroupingParams();
    topValues = parseInt($("#topValues").val());
    var tableSettings = {
      layout: {
        topStart: null,
        topEnd: { search: { placeholder: "Search" } },
        bottomStart: ["pageLength", "info"],
        bottomEnd: null,
      },
      language: { search: ''},
      bFilter: false,
      stateSave: false,
      stateDuration: -1,
      serverSide: false,
      ajax: {
        url: listUrl,
        dataSrc: "data",
        data: function (d) {
          var params = $("#criteriaForm").serializeArray();
          for (var i = 0; i < params.length; i++) {
            if (d[params[i].name])
              d[params[i].name] = d[params[i].name] + ";" + params[i].value;
            else d[params[i].name] = params[i].value;
          }
        },
      },
      aaSorting: pvc ? [3] : [4],
      order: pvc ? [[3, "desc"]] : [[4, "desc"]],
      bLengthChange: false,
      bInfo: false,
      pagination: false,
      bPaginate: false,
      iDisplayLength: 100000,

      drawCallback: function (settings) {
        if (topValues && actionPlanTable) {
          var num = 0;
          var rows = actionPlanTable.rows()[0];
          for (var i in rows) {
            var $row = actionPlanTable.rows(rows[i]).nodes().to$();
            if (num >= topValues) $row.hide();
            else $row.show();
            num++;
          }
        }
        if (settings.json) {
          periods = settings.json.periods;
          $(".lastLabel").html(
            " (" + periods["from0"] + " - " + periods["to0"] + ")"
          );
          for (var i = 0; i < previousNumber; i++) {
            $($(".previousLabel")[i]).html(
              " (" +
                periods["from" + (i + 1)] +
                " - " +
                periods["to" + (i + 1)] +
                ")"
            );
          }
          $(".createActionItem")
            .attr("data-dateRangeFrom", periods["from0"])
            .attr("data-dateRangeTo", periods["to0"]);
        }
      },
      rowGroup: {
        dataSrc: groupping.groupingParams,
        startRender: groupping.groupingParams
          ? function (rows, group, level) {
              if (
                topValues &&
                ((level == 1 && groupping.groupingParams.length == 2) ||
                  (level == 2 && groupping.groupingParams.length == 3))
              ) {
                var num = 0;
                for (var i in rows[0]) {
                  var $row = actionPlanTable.rows(rows[0][i]).nodes().to$();
                  if (num >= topValues) $row.hide();
                  else $row.show();
                  num++;
                }
              }
              var style = "background: #cccccc !important;";
              if (level == 1)
                var style =
                  "background: #eeeee !important;font-weight: normal;";
              if (level == 2)
                var style =
                  "background: #efefef !important;font-weight: normal;";
              return $("<tr/>").append(
                '<td colspan="9999" style="' +
                  style +
                  '">' +
                  (level == 0
                    ? groupingLabel1
                    : level == 1
                    ? groupingLabel2
                    : groupingLabel3) +
                  ": " +
                  group +
                  "</td>"
              );
            }
          : null,
        endRender: null,
      },
      columnDefs: [
        {
          targets: groupping.hidden,
          visible: false,
        },
      ],

      orderFixed: groupping.orderFix,
      aoColumns: getColumns(),
      initComplete: function (settings) {
        initActionItems();
        hideLoader();
      },
    };
    actionPlanTable = $("#actionPlatTable")
      .DataTable(tableSettings)
      .on("draw.dt", function () {
        $(".redCell").parent().addClass("danger");
        $(".greenCell").parent().addClass("success");
      })
      .on("xhr.dt", function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
      });
  };

  function getColumns() {
    if (!pvc) {
      var columns = [
        { mData: "responsibleParty", sClass: "actionPlanStickyColumn" },
        { mData: "observation", sClass: "actionPlanStickyColumn" },
        { mData: "errorType", sClass: "actionPlanStickyColumn" },
        { mData: "priority", sClass: "actionPlanStickyColumn" },
      ];
      for (var i = 0; i <= previousNumber; i++) {
        columns = columns.concat([
          {
            mData: "lastNumber" + i,
            sClass: "to-center",
            mRender: numberRenderer,
          },
          {
            mData: "lastVendor" + i,
            sClass: "to-center",
            mRender: percentRenderer,
          },
          {
            mData: "lastIssue" + i,
            sClass: "to-center",
            mRender: percentRenderer,
          },
          {
            mData: "lastObservation" + i,
            sClass: "to-center",
            mRender: percentRenderer,
          },
          {
            mData: "lastPriority" + i,
            sClass: "to-center",
            mRender: percentRenderer,
          },
          {
            mData: "completed" + i,
            sClass: "to-center",
            mRender: actionItemRenderer,
          },
          {
            mData: "lastToPrevious" + i,
            sClass: "to-center",
            mRender: lastToPreviousRenderer,
            visible: i < previousNumber,
          },
        ]);
      }

      columns = columns.concat([
        {
          mData: "",
          sClass: "to-center",
          bSortable: false,
          mRender: function (data, type, row) {
            return (
              '<a href="javascript:void(0)" data-parentEntityKey="' +
              row.responsiblePartyId +
              "_@_" +
              row.observationCode +
              "_@_" +
              row.errorType +
              "_@_" +
              row.priority +
              '" class="btn btn-success createActionItem" title="' +
              $.i18n._("qualityModule.createAI.label") +
              '">' +
              $.i18n._("app.label.AI") +
              "</a>"
            );
          },
        },
      ]);
      return columns;
    } else {
      var columns = [
        { mData: "responsibleParty", sClass: "actionPlanStickyColumn" },
        { mData: "rc", sClass: "actionPlanStickyColumn" },
        { mData: "destination", sClass: "actionPlanStickyColumn" },
      ];
      for (var i = 0; i <= previousNumber; i++) {
        columns = columns.concat([
          {
            mData: "lastNumber" + i,
            sClass: "to-center",
            mRender: numberRenderer,
          },
          {
            mData: "lastDestination" + i,
            sClass: "to-center",
            mRender: percentRenderer,
          },
          {
            mData: "lastRc" + i,
            sClass: "to-center",
            mRender: percentRenderer,
          },
          {
            mData: "lastVendor" + i,
            sClass: "to-center",
            mRender: percentRenderer,
          },
          {
            mData: "completed" + i,
            sClass: "to-center",
            mRender: actionItemRenderer,
          },
          {
            mData: "lastToPrevious" + i,
            sClass: "to-center",
            mRender: lastToPreviousRenderer,
            visible: i < previousNumber,
          },
        ]);
      }
      columns = columns.concat([
        {
          mData: "",
          sClass: "to-center",
          bSortable: false,
          mRender: function (data, type, row) {
            return (
              '<a href="javascript:void(0)" data-parentEntityKey="' +
              row.responsiblePartyId +
              "_@_" +
              row.rcCode +
              "_@_" +
              row.destination +
              '" class="btn btn-success createActionItem" title="' +
              $.i18n._("qualityModule.createAI.label") +
              '">' +
              $.i18n._("app.label.AI") +
              "</a>"
            );
          },
        },
      ]);
      return columns;
    }
  }

  function actionItemRenderer(data, type, row, settings) {
    var label = "";
    var index = getPeriodIndex(settings);
    if (row["total" + index] == 0) return "0";
    if (row["overdue" + index] > 0) label = '<span class="redCell"></span>';
    else if (row["completed" + index] == 100)
      label = '<span class="greenCell"></span>';
    var parentEntityKey = pvc
      ? row.responsiblePartyId + "_@_" + row.rcCode + "_@_" + row.destination
      : row.responsiblePartyId +
        "_@_" +
        row.observationCode +
        "_@_" +
        row.errorType +
        "_@_" +
        row.priority;
    return (
      label +
      '<a href="javascript:void(0)" data-parentEntityKey="' +
      parentEntityKey +
      '" ' +
      'data-index="' +
      index +
      '" class="actionItemModalIcon" >' +
      data +
      "% (of " +
      row["total" + index] +
      ")</a>"
    );
  }

  function greenPercent(data, type, row) {
    if (data > 0) return '<span class="redCell"></span>' + data + "%";
    return data;
  }

  function lastToPreviousRenderer(data, type, row, settings) {
    var index = getPeriodIndex(settings);
    if (data > 0)
      return (
        '<span class="fa fa-long-arrow-up redCell"></span>' +
        (data > 1000 ? ">1000" : data) +
        "%"
      );
    if (data == 0 && row["lastNumber" + index] > 0)
      return '<span class="redCell"></span>0%';
    if (data == 0 && row["lastNumber" + index] == 0)
      return '<span class="greenCell"></span>0%';
    return '<span class="fa fa-long-arrow-down greenCell"></span>' + data + "%";
  }

  $("#exportToExcel").on("click", function () {
    exportToExcel();
  });

  function getFormData($form) {
    var unindexed_array = $form.serializeArray();
    var indexed_array = {};

    _.each(unindexed_array, function (n, i) {
      indexed_array[n['name']] = n['value'];
    });

    return indexed_array;
  }

  function exportToExcel() {
    var data = getFormData($("#criteriaForm"));

    $('#exportWarning .description').show();
    $('#exportWarning #warningType').hide();
    $('#exportWarning').modal('show');
    $('#exportWarningOkButton').off('click').click(function () {
      $('#exportWarning').modal('hide');
      var jForm = new FormData();
      jForm.append("data", JSON.stringify(data))
      jForm.append("async", true)
      $.ajax({
        url: $("#criteriaForm").attr("data-url"),
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
  }

  $("#applyFilter").on("click", function () {
    var errorMessage = validateInput($("#criteriaForm"));

    if ($("#dateRangeType").val() === "CUSTOM") {
      if (
        !moment(
          $("#dateRangeFrom").val(),
          DEFAULT_DATE_DISPLAY_FORMAT,
          true
        ).isValid()
      ) {
        errorMessage += $.i18n._("app.actionPlan.date.from.invalid") + "<br>";
      }
      if (
        !moment(
          $("#dateRangeTo").val(),
          DEFAULT_DATE_DISPLAY_FORMAT,
          true
        ).isValid()
      ) {
        errorMessage += $.i18n._("app.actionPlan.date.to.invalid") + "<br>";
      }
    }

    if (errorMessage) {
      $("#errorModal .description").html(errorMessage);
      $("#errorModal").modal("show");
      return;
    }
    showLoader();
    previousNumber = $("#periodsNumber").val();
    createActionPlanTable();
  });

  function getGroupingParams() {
    var result = {};
    var grouping = $("#groupBy").val();
    var groupingParams = [];
    if (grouping == "responsible_observation" || grouping == "responsible_rc") {
      if (grouping == "responsible_observation") {
        result.groupingParams = ["responsibleParty", "observation"];
        groupingLabel1 = $.i18n._("app.responsibleParty.name");
        groupingLabel2 = $.i18n._("app.observation.name");
      } else {
        result.groupingParams = ["responsibleParty", "rc"];
        groupingLabel1 = $.i18n._("app.responsibleParty.name");
        groupingLabel2 = $.i18n._("app.rc.name");
      }
      result.orderFix = [
        [0, "asc"],
        [1, "asc"],
      ];
      result.hidden = [0, 1];
    } else if (
      grouping == "observation_issue" ||
      grouping == "rc_destination"
    ) {
      if (grouping == "observation_issue") {
        result.groupingParams = ["observation", "errorType"];
        groupingLabel1 = $.i18n._("app.observation.name");
        groupingLabel2 = $.i18n._("app.errorType.name");
      } else {
        result.groupingParams = ["rc", "destination"];
        groupingLabel1 = $.i18n._("app.rc.name");
        groupingLabel2 = $.i18n._("app.destination.name");
      }
      result.orderFix = [
        [1, "asc"],
        [2, "asc"],
      ];
      result.hidden = [1, 2];
    } else if (
      grouping == "observation_responsible" ||
      grouping == "rc_responsible"
    ) {
      if (grouping == "observation_responsible") {
        result.groupingParams = ["observation", "responsibleParty"];
        groupingLabel1 = $.i18n._("app.observation.name");
        groupingLabel2 = $.i18n._("app.responsibleParty.name");
      } else {
        result.groupingParams = ["rc", "responsibleParty"];
        groupingLabel1 = $.i18n._("app.rc.name");
        groupingLabel2 = $.i18n._("app.responsibleParty.name");
      }
      result.orderFix = [
        [1, "asc"],
        [0, "asc"],
      ];
      result.hidden = [1, 0];
    } else if (grouping == "priority_issue") {
      result.groupingParams = ["priority", "errorType"];
      groupingLabel1 = $.i18n._("app.priority.name");
      groupingLabel2 = $.i18n._("app.errorType.name");
      result.orderFix = [
        [3, "asc"],
        [2, "asc"],
      ];
      result.hidden = [2, 3];
    } else if (grouping == "observation_priority_issue") {
      result.groupingParams = ["observation", "priority", "errorType"];
      groupingLabel1 = $.i18n._("app.observation.name");
      groupingLabel2 = $.i18n._("app.priority.name");
      groupingLabel3 = $.i18n._("app.errorType.name");
      result.orderFix = [
        [1, "asc"],
        [3, "asc"],
        [2, "asc"],
      ];
      result.hidden = [1, 2, 3];
    } else if (grouping == "responsible_priority") {
      result.groupingParams = ["responsibleParty", "priority"];
      groupingLabel1 = $.i18n._("app.responsibleParty.name");
      groupingLabel2 = $.i18n._("app.priority.name");
      result.orderFix = [
        [0, "asc"],
        [3, "asc"],
      ];
      result.hidden = [0, 3];
    } else {
      result.groupingParams = null;
      groupingLabel1 = "";
      groupingLabel2 = "";
      result.orderFix = null;
      result.hidden = null;
    }
    return result;
  }

  function percentRenderer(v) {
    return "" + v + "%";
  }

  function numberRenderer(data, type, row, settings) {
    var period = getPeriodIndex(settings);
    var params = pvc
      ? ' data-destination="' +
        row.destination +
        '" data-rcCode="' +
        row.rcCode +
        '" '
      : ' data-observation="' +
        row.observationCode +
        '" data-priority="' +
        row.priority +
        '" data-errorType="' +
        row.errorType +
        '" ';
    var ref =
      data > 0
        ? '<a href="javascript:void(0)" class="caseListLink"  data-responsibleParty="' +
          row.responsiblePartyId +
          '" data-period="' +
          period +
          '" ' +
          params +
          ">" +
          data +
          "</a>"
        : data;
    if (data > 0) return '<span class="redCell"></span>' + ref;
    return '<span class="greenCell"></span>' + ref;
  }

  function getPeriodIndex(settings) {
    var col = settings.col;
    var field = settings.settings.aoColumns[col].mData;
    return field.replace(/[a-zA-Z]/g, "");
  }

  function initActionItems() {
    $(document).on("click", ".actionItemModalIcon", function (e) {
      e.preventDefault();
      var index = $(this).attr("data-index");
      actionItem.actionItemModal.set_parent_entity_key(
        $(this).attr("data-parentEntityKey"),
        periods["from" + index],
        periods["to" + index]
      );
      actionItem.actionItemModal.view_action_item_list(
        hasAccessOnActionItem,
        false,
        "ACTION_PLAN"
      );
    });

    $(document).on("click", ".createActionItem", function () {
      actionItem.actionItemModal.init_action_item_modal(
        false,
        "ACTION_PLAN",
        this
      );
    });

    //Click event bind to the action item view option.
    $(document).on("click", ".action-item-view", function () {
      var actionItemId = $(this).attr("data-value");
      actionItem.actionItemModal.view_action_item(
        actionItemId,
        "ACTION_PLAN",
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
        "ACTION_PLAN",
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

  $(document).on("click", ".createNewSummary", function (event) {
    $(".createNewSummary").attr("disabled", true);
    $.ajax({
      url: actionPlanSaveLinkUrl,
      dataType: "html",
      data: {
        from: periods["from" + caseListFilter.period],
        to: periods["to" + caseListFilter.period],
        parentEntityKey: pvc
          ? caseListFilter.responsiblePartyId +
            "_@_" +
            caseListFilter.rcCode +
            "_@_" +
            caseListFilter.destination
          : caseListFilter.responsiblePartyId +
            "_@_" +
            caseListFilter.observationCode +
            "_@_" +
            caseListFilter.errorType +
            "_@_" +
            caseListFilter.priority,
      },
    }).done(function (data) {
      loadSummaries();
      $(".saveSummaryButton").prop("disabled", false);
    });
  });
  $(document).on("click", ".caseListLink", function (event) {
    caseListFilter.observation = $(this).attr("data-observation");
    caseListFilter.responsibleParty = $(this).attr("data-responsibleParty");
    caseListFilter.period = $(this).attr("data-period");
    caseListFilter.errorType = $(this).attr("data-errorType");
    caseListFilter.destination = $(this).attr("data-destination");
    caseListFilter.rcCode = $(this).attr("data-rcCode");
    caseListFilter.issueType = $(this).attr("data-issueType");
    caseListFilter.priority = $(this).attr("data-priority");
    $("#caseListModal").modal("show");
    showLoader();
    loadSummaries();
    caseListTable.ajax.reload();
  });

  function loadSummaries() {
    $.ajax({
      url: actionPlanIndexLinkUrl,
      dataType: "html",
      data: {
        all: $("#summaryPeriod2").is(":checked"),
        from: periods["from" + caseListFilter.period],
        to: periods["to" + caseListFilter.period],
        parentEntityKey: pvc
          ? caseListFilter.responsiblePartyId +
            "_@_" +
            caseListFilter.rcCode +
            "_@_" +
            caseListFilter.destination
          : caseListFilter.responsiblePartyId +
            "_@_" +
            caseListFilter.observationCode +
            "_@_" +
            caseListFilter.errorType +
            "_@_" +
            caseListFilter.priority,
      },
    }).done(function (data) {
      var select = $("#actionPlanSummaries");
      select.empty();
      $(".createNewSummary").attr("disabled", false);
      for (var i in data) {
        if (data[i].current) {
          $(".createNewSummary").attr("disabled", true);
          $(".saveSummaryButton").prop("disabled", false);
        }
        select.append(
          "<option value='" + data[i].id + "'>" + data[i].date + "</option>"
        );
      }
      select.select2().trigger("change");
    });
  }

  $(document).on("change", ".summaryPeriodRadio", function () {
    loadSummaries();
  });
  $(document).on("click", ".saveSummaryButton", function () {
    $.ajax({
      url: actionPlanUpdateLinkUrl,
      type: "POST",
      dataType: "json",
      data: {
        id: $("#currentSummary").val(),
        text: $("#summaryText").val(),
      },
    })
      .done(function (data) {
        if (data.success) {
          $(".modalSuccess").removeClass("hide");
          setTimeout(function () {
            $(".modalSuccess").addClass("hide");
          }, 2000);
          $(".deleteSummary").prop("disabled", false);
        } else {
          $(".modalError").text(data.msg);
          $(".modalError").removeClass("hide");
          setTimeout(function () {
            $(".modalError").addClass("hide");
          }, 2000);
        }
      })
      .fail(function (data) {
        $(".modalError").text(data.msg);
        $(".modalError").removeClass("hide");
        setTimeout(function () {
          $(".modalError").addClass("hide");
        }, 2000);
      });
  });

  $(document).on("click", ".deleteSummary", function () {
    var confirmationModal = $("#confirmationModal");
    confirmationModal.modal("show");
    confirmationModal.find(".modalHeader").html($.i18n._("delete.confirm"));
    confirmationModal
      .find(".confirmationMessage")
      .html($.i18n._("qualityModule.manualRemove.warning.label"));
    confirmationModal.find(".okButton").html($.i18n._("ok"));
    confirmationModal
      .find(".okButton")
      .off()
      .on("click", function () {
        $.ajax({
          url: actionPlanDeleteLinkUrl + "?id=" + $("#currentSummary").val(),
          dataType: "html",
        }).done(function (data) {
          loadSummaries();
          $("#confirmationModal").modal("hide");
          $(".saveSummaryButton, .deleteSummary").prop("disabled", true);
        });
      });
  });
  $(document).on("change", "#actionPlanSummaries", function () {
    if ($(this).val()) {
      $.ajax({
        url: actionPlanViewLinkUrl + "?id=" + $(this).val(),
        dataType: "json",
      }).done(function (data) {
        $("#summaryText").val(data.text);
        $("#summaryDate").html(data.from + " - " + data.to);
        $("#currentSummary").val(data.id);
        $(".saveSummaryButton").prop("disabled", false);
      });
    } else {
      $("#summaryText").val("");
      $("#summaryDate").html("");
      $("#currentSummary").val("");
      $(".saveSummaryButton, .deleteSummary").prop("disabled", true);
    }
  });
  var caseListTable = $("#caseListTable").DataTable({
    layout: {
      topStart: null,
      topEnd: "search",
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
    bFilter: false,
    stateSave: false,
    stateDuration: -1,
    serverSide: false,
    ajax: {
      url: caseListUrl,
      dataSrc: "data",
      data: function (d) {
        var params = $("#criteriaForm").serializeArray();
        for (var i = 0; i < params.length; i++) {
          if (d[params[i].name])
            d[params[i].name] = d[params[i].name] + ";" + params[i].value;
          else d[params[i].name] = params[i].value;
        }
        for (var attrname in caseListFilter) {
          d[attrname] = caseListFilter[attrname];
        }
      },
    },
    aaSorting: [0],
    bLengthChange: true,
    aLengthMenu: [
      [10, 20, 50, 100],
      [10, 20, 50, 100],
    ],
    pagination: true,
    iDisplayLength: 10,

    drawCallback: function (settings) {
      hideLoader();
    },
    aoColumns: [
      {
        mData: "caseNumber",
        mRender: function (data, type, row) {
          return (
            '<a href="' +
            caseDataLinkUrl +
            "?caseNumber=" +
            row.caseNumber +
            "&versionNumber=" +
            row.caseVersion +
            "&id=" +
            row.id +
            "&type=" +
            row.observation +
            '" target="_blank">' +
            row.caseNumber +
            "</a></span>"
          );
        },
      },
      { mData: "caseVersion" },
      {
        mData: "rootCause",
        mRender: function (data, type, row) {
          return (
            "<span>" +
            row.rootCause +
            "</span><br><span>" +
            (pvc ? row.rootCauseClass : row.responsibleParty) +
            "</span>"
          );
        },
      },
      {
        visible: pvc,
        mData: "rootCause",
        mRender: function (data, type, row) {
          return pvc
            ? "<span>" +
                row.rootCauseSub +
                "</span><br><span>" +
                row.responsibleParty +
                "</span>"
            : "";
        },
      },
      {
        mData: "preventativeAction",
        mRender: function (data, type, row) {
          return (
            "<span>" +
            row.correctiveAction +
            "</span><br><span>" +
            row.preventativeAction +
            "</span>"
          );
        },
      },
      {
        mData: "preventativeAction",
        mRender: function (data, type, row) {
          return (
            "<span>" +
            row.correctiveDate +
            "</span><br><span>" +
            row.preventativeDate +
            "</span>"
          );
        },
      },
      { mData: "investigation" },
      { mData: "summary" },
      { mData: "actions" },
      {
        mData: "primary",
        mRender: function (data, type, row) {
          return data == 1 ? $.i18n._("yes") : $.i18n._("no");
        },
      },
      { mData: "workFlowState" },
      { mData: "assignedTo" },
    ],
  });

  $("#workflowFilter").on("select2:select", function (e) {
    var value = $("#workflowFilter").select2("val");
    if (value.length > 0) {
      var id = e.params.data.id;
      if (
        id == "" ||
        id == "final" ||
        id == "notFinal" ||
        value[0] == "" ||
        value[0] == "final" ||
        value[0] == "notFinal"
      ) {
        $("#workflowFilter").val(id).trigger("change");
      }
    }
  });
  createActionPlanTable();

  $("[data-evt-change]").on('change', function() {
    const eventData = JSON.parse($(this).attr("data-evt-change"));
    const methodName = eventData.method;
    const params = eventData.params;
    // Call the method from the eventHandlers object with the params
    if (methodName == 'updateMaxInputValue') {
      updateMaxInputValue();
    }
  });

});

function reloadActionPlanTable() {
  actionPlanTable.ajax.reload();
}

function validateInput(parent) {
  var topValuesInput = parseInt(parent.find("#topValues").val());
  var periodsNumberInput = parseInt(parent.find("#periodsNumber").val());
  var lastXInput = parseInt(parent.find("#lastX").val());
  var isCustomDateRange = parent.find("#dateRangeType").val() === "CUSTOM";
  var errorMessage = "";
  if (
    !checkValidValues(topValuesInput) ||
    !checkValidValues(periodsNumberInput) ||
    (!isCustomDateRange && !checkValidValues(lastXInput))
  ) {
    errorMessage += " " + $.i18n._("app.actionPlan.constraints");
  }

  if (isCustomDateRange) {
    var start = moment(parent.find("#dateRangeFrom").val());
    var end = moment(parent.find("#dateRangeTo").val());
    if (!(start && end && start <= end)) {
      errorMessage += " " + $.i18n._("app.actionPlan.startEndDate");
    }
    if (end.diff(start, "days") > 366)
      errorMessage += " " + $.i18n._("app.actionPlan.interval");
  }
  if (
    parent.find("#dateRangeType").val() == "LAST_X_DAYS" &&
    (parent.find("#lastX").val() > 366 || parent.find("#lastX").val() < 1)
  )
    errorMessage += " " + $.i18n._("app.actionPlan.interval");
  if (
    parent.find("#dateRangeType").val() == "LAST_X_WEEKS" &&
    (parent.find("#lastX").val() > 53 || parent.find("#lastX").val() < 1)
  )
    errorMessage += " " + $.i18n._("app.actionPlan.interval");
  if (
    parent.find("#dateRangeType").val() == "LAST_X_MONTHS" &&
    (parent.find("#lastX").val() > 12 || parent.find("#lastX").val() < 1)
  )
    errorMessage += " " + $.i18n._("app.actionPlan.interval");
  if (
    parent.find("#periodsNumber").val() > 5 ||
    parent.find("#periodsNumber").val() < 1
  )
    errorMessage += " " + $.i18n._("app.actionPlan.periods");
  if (
    parent.find("#topValues").val() > 999 ||
    parent.find("#topValues").val() < 1
  )
    errorMessage += " " + $.i18n._("app.actionPlan.showTop");

  return errorMessage;
}

function updateMaxInputValue() {
  var selectElement = $("#dateRangeType");
  var inputElement = $("#lastX");
  var selectedValue = selectElement.val();
  var maxValue;
  switch (selectedValue) {
    case "LAST_X_DAYS":
      maxValue = 366;
      break;
    case "LAST_X_WEEKS":
      maxValue = 53;
      break;
    case "LAST_X_MONTHS":
      maxValue = 12;
      break;
    default:
      maxValue = 12;
  }
  inputElement.attr("max", maxValue);
}

function checkValidValues(inputElement) {
  if (isNaN(inputElement) || inputElement < 1) {
    return false;
  }
  return true;
}
