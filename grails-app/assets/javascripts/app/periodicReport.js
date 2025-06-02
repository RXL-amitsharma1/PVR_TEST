REPORT_ACTION_TYPE = {
  GENERATE_DRAFT: "GENERATE_DRAFT",
  GENERATE_CASES: "GENERATE_CASES",
  GENERATE_CASES_DRAFT: "GENERATE_CASES_DRAFT",
  GENERATE_FINAL: "GENERATE_FINAL",
  GENERATE_CASES_FINAL: "GENERATE_CASES_FINAL",
  MARK_AS_SUBMITTED: "MARK_AS_SUBMITTED",
  SEND_TO_DMS: "SEND_TO_DMS",
};

EX_CONFIGURATION_STATUS_ENUM = {
  GENERATED_CASES: "GENERATED_CASES",
  GENERATING_DRAFT: "GENERATING_DRAFT",
  GENERATED_DRAFT: "GENERATED_DRAFT",
  GENERATING_FINAL_DRAFT: "GENERATING_FINAL_DRAFT",
  GENERATED_FINAL_DRAFT: "GENERATED_FINAL_DRAFT",
  SUBMITTED: "SUBMITTED",
  COMPLETED: "COMPLETED",
};
ACTION_ITEM_GROUP_STATE_ENUM = {
  WAITING: "WAITING",
  OVERDUE: "OVERDUE",
  CLOSED: "CLOSED",
};
var periodicReport = periodicReport || {};

var moretext = $.i18n._("moretext");
var lesstext = $.i18n._("lesstext");

periodicReport.periodicReportList = (function () {
  var tableFilter = {};
  var advancedFilter = false;

  var initSpecialActions = function (container) {
    container.on("click", ".generateDraft", function () {
      generateReport(
        $(this).data("exconfigId"),
        REPORT_ACTION_TYPE.GENERATE_DRAFT
      );
    });
    container.on("click", ".generateFinalDraft", function () {
      generateReport(
        $(this).data("exconfigId"),
        REPORT_ACTION_TYPE.GENERATE_FINAL
      );
    });
    container.on("click", ".generateCasesDraft", function () {
      generateReport(
        $(this).data("exconfigId"),
        REPORT_ACTION_TYPE.GENERATE_CASES_DRAFT
      );
    });
    container.on("click", ".generateCasesFinalDraft", function () {
      generateReport(
        $(this).data("exconfigId"),
        REPORT_ACTION_TYPE.GENERATE_CASES_FINAL
      );
    });
    container.on("click", ".generateCases", function () {
      generateReport(
        $(this).data("exconfigId"),
        REPORT_ACTION_TYPE.GENERATE_CASES
      );
    });
    container.on("click", ".createActionItem", function () {
      actionItem.actionItemModal.set_executed_report_id(
        $(this).data("exconfigId")
      );
      actionItem.actionItemModal.init_action_item_modal(
        false,
        PERIODIC_REPORT,
        hasAccessOnActionItem
      );
    });
    //Show the draft modal window.
    submission_modal_after_load();
  };

  //The function for initializing the action item data tables.
  var init_periodic_report_table = function () {
    //Initialize the data table
    var periodic_report_table = $("#periodicReportList")
      .DataTable({
        layout: {
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
        //"sPaginationType": "bootstrap",
        stateSave: true,
        stateDuration: -1,

        initComplete: function () {
          $("#periodic-list-conainter").on("click", ".favorite", function () {
            changeFavoriteState(
              $(this).data("exconfigId"),
              $(this).hasClass("glyphicon-star-empty"),
              $(this)
            );
          });

          //Toggle the action buttons on the action item list.
          actionButton("#periodicReportList");

          initSpecialActions($("#periodic-list-conainter"));

          $("#periodicReportList").on(
            "click ",
            ".actionItemModalIcon",
            function () {
              actionItem.actionItemModal.set_executed_report_id(
                $(this).data("exconfigId")
              );
              actionItem.actionItemModal.view_action_item_list(
                hasAccessOnActionItem,
                false,
                PERIODIC_REPORT
              );
            }
          );
          $("#periodicReportList").on("click ", ".morelink", function () {
            if ($(this).hasClass("less")) {
              $(this).removeClass("less");
              $(this).html(moretext);
            } else {
              $(this).addClass("less");
              $(this).html(lesstext);
            }
            $(this).parent().prev().toggle();
            $(this).prev().toggle();
            return false;
          });
          initArchiveFilter(periodic_report_table);
          initSharedWithFilter("periodicReportList", periodic_report_table);
          $("#submissionFilter").select2({
            placeholder: $.i18n._("app.advancedFilter.status"),
            allowClear: true,
          });
          $("#allReportsFilter, #typeFilter").on("change", function (e) {
            periodic_report_table.draw();
          });
          var sessionStorageSubmissionFilterVariableName =
            window.location.pathname.replace(/\//g, "") + ".submissionFilter";
          var submissionFilterSelect = $("#submissionFilter");
          submissionFilterSelect.on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
              searchField[0].focus();
            }
          }).on("change", function (e) {
            sessionStorage.setItem(
              sessionStorageSubmissionFilterVariableName,
              submissionFilterSelect.val()
            );
            periodic_report_table.draw();
          });
          if (
            sessionStorage.getItem(sessionStorageSubmissionFilterVariableName)
          ) {
            submissionFilterSelect.val(sessionStorage.getItem(sessionStorageSubmissionFilterVariableName));
          }
          setTimeout(function () {
            if (
              sessionStorage.getItem(
                sessionStorageSubmissionFilterVariableName
              ) ||
              sessionStorage.getItem(sessionStorageSharedWithVariableName)
            )
              periodic_report_table.draw();
          }, 100);
        },

        customProcessing: true, //handled using processing.dt event
        serverSide: true,
        ajax: {
          url: periodicReportConfig.reportsListUrl,
          type: "POST",
          dataSrc: "data",
          data: function (d) {
            d.searchString = d.search.value;
            d.tableFilter = JSON.stringify(tableFilter);
            d.advancedFilter = advancedFilter;
            if (d.order.length > 0) {
              d.direction = d.order[0].dir;
              d.sharedwith = $("#sharedWithFilterControl").val();
              d.includeArchived = $("#includeArchived").is(":checked");
              d.submission = $("#submissionFilter").val();
              //Column header data value extracting
              d.sort = d.columns[d.order[0].column].data;
              d.pvp = periodicReportConfig.publisher;
              d.allReportsForPublisher = $("#allReportsFilter").is(":checked");
              d.periodicReportType = $("#typeFilter").val();
            }
          },
        },
        rowId: "id",
        aaSorting: [],
        order: [
          [2, "asc"],
          [11, "desc"],
        ],
        columnDefs: [
          { orderable: false, targets: [0, 7, 8, 13, 15] },
          { width: "25", targets: 2 },
          { width: "70", targets: 14 },
          { width: "85", targets: 13 },
        ],
        lengthChange: true,
        pageLength: 10,
        aLengthMenu: [
          [10, 25, 50, 100],
          [10, 25, 50, 100],
        ],
        pagination: true,
        iDisplayLength: 10,

        drawCallback: function (settings) {
          pageDictionary(
            $("#periodicReportList_wrapper")[0],
            settings.aLengthMenu[0][0],
            settings.json.recordsFiltered
          );
          getReloader($("#periodicReportList_info"), $("#periodicReportList"));
        },
        columns: (function () {
          var columns = [
            {
              data: null,
              visible: false,
              render: function (data, type, row) {
                return "<span>" + row.id + "</span>";
              },
            },
            {
              data: "type",
              visible: false,
            },
            {
              data: "isFavorite",
              sClass: "dataTableColumnCenter",
              asSorting: ["asc"],
              bSortable: true,
              render: renderFavoriteIcon,
            },
            {
              data: "reportName",
              render: function (data, type, row) {
                var ico = "";
                if (
                  row.isPublisherReport &&
                  ($("r#allReportsFilter").is(":checked") ||
                    !periodicReportConfig.publisher)
                )
                  ico =
                    '<sup type="' +
                    $.i18n._("publisherReport") +
                    '" style="font-weight: bold;">PVP</sup>';
                var arch = row.isArchived
                  ? ' <span class="glyphicon glyphicon-text-background" title="' +
                    $.i18n._("labelArchive") +
                    '"></span> '
                  : "";
                var content =
                  "<div class='three-row-dot-overflow' >" +
                  arch +
                  ('<a  href="' +
                    periodicReportConfig.reportViewUrl +
                    "?id=" +
                    row.id +
                    "&isInDraftMode=" +
                    row.isInDraftMode +
                    '">' +
                    encodeToHTML(row.reportName) +
                    "</a>") +
                  ico +
                  "</div>";
                return content;
              },
            },
            {
              data: "versionName",
              visible: false,
            },
            {
              data: "license",
              visible: false,
              bSortable: false,
              mRender: function (data, type, row) {
                var text = data == null ? "" : encodeToHTML(data);
                return '<div class="three-row-dot-overflow">' + text + "</div>";
              },
            },
            {
              data: "version",
            },
            {
              data: "productSelection",
              visible: false,
            },
            {
              data: "date_range",
              sClass: "mw-100",
            },
            {
              data: "primaryReportingDestination",
              mRender: function (data, type, row) {
                // var text ="<span style='border: #AAAAAA solid 1px;border-radius: 2px;background: #0fef20'>"+ data+ "(P) </span>";
                var text =
                  data +
                  "<B title='" +
                  $.i18n._("app.advancedFilter.primaryReportingDestination") +
                  "'> (P) </b>";
                if (row.otherReportingDestinations) {
                  if (row.otherReportingDestinations.startsWith('[') && row.otherReportingDestinations.endsWith(']')) {
                    text += ", " + row.otherReportingDestinations.substring(1, row.otherReportingDestinations.length - 1).replace(data + ', ', "");
                  } else {
                    text += ", " + row.otherReportingDestinations.replace(data + ', ', "");
                  }
                }
                return "<div class='three-row-dot-overflow'>" + text + "</div>";
              },
            },
            {
              data: "dueDate",
              sClass: "mw-100",
              render: function (data, type, row) {
                var clazz = "";
                if (row.indicator == "red")
                  clazz = 'class="roundLabel label-danger text-white"';
                if (row.indicator == "yellow")
                  clazz = 'class="roundLabel label-primary"';
                if (data) {
                  return (
                    "<span " +
                    clazz +
                    ">" +
                    moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) +
                    "</span>"
                  );
                }
                return "";
              },
            },
            {
              data: "lastUpdated",
              aTargets: ["lastUpdated"],
              sClass: "dataTableColumnCenter",
              mRender: function (data, type, full) {
                return moment
                  .utc(data)
                  .tz(userTimeZone)
                  .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
              },
            },
            {
              data: "user",
            },
            {
              data: "tags",
              bSortable: false,
              aTargets: ["tags"],
              render: function (data, type, full) {
                var tags = data ? encodeToHTML(data) : "";
                return "<div class='three-row-dot-overflow'>" + tags + "</div>";
              },
            },
            {
              data: "state",
              render: function (data, type, row) {
                return (
                  '<button class="btn btn-default btn-xs btn-table-col-state" data-executed-config-id= "' +
                  row.id +
                  '" data-initial-state= "' +
                  row.state +
                  '" data-evt-clk=\'{\"method\": \"openStateHistoryModal\", \"params\": []}\' title="' + row.state + '">' +
                  row.state +
                  "</button>"
                );
              },
            },
            {
              data: null,
              sClass: "dataTableColumnCenter",
              render: function (data, type, row) {
                var creationAction = null;
                var clazz = "";
                if (row.actionItemStatus) {
                  switch (row.actionItemStatus) {
                    case ACTION_ITEM_GROUP_STATE_ENUM.OVERDUE:
                      clazz = "btn btn-danger btn-xs";
                      break;
                    case ACTION_ITEM_GROUP_STATE_ENUM.WAITING:
                      clazz = "btn btn-warning btn-xs";
                      break;
                    default:
                      clazz = "btn btn-success btn-xs";
                      break;
                  }
                  creationAction =
                    '<button class="' +
                    clazz +
                    ' actionItemModalIcon" data-exconfig-id="' +
                    row.id +
                    '" style="width:70px;">' +
                    $.i18n._(
                      "app.actionItemGroupState." + row.actionItemStatus
                    ) +
                    "</button>";
                }
                return creationAction;
              },
            },
            {
              data: null,
              bSortable: false,
              sClass: "dataTableColumnCenter",
              render: function (data, type, row) {
                //Following code dynamically create the action buttons based on the conditions.
                var actionButton =
                  '<div class="btn-group dropdown dataTableHideCellContent reportActionsDiv" align="center"> ';
                actionButton +=
                  '<a class="btn btn-success btn-xs" href="' +
                  periodicReportConfig.reportViewUrl +
                  "?id=" +
                  row.id +
                  "&isInDraftMode=" +
                  row.isInDraftMode +
                  '">' +
                  $.i18n._("view") +
                  "</a>";
                actionButton +=
                  '<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret dropdown-toggle"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right action-menu" role="menu" style="min-width: 80px !important; font-size: 12px;"> ';
                if (row.hasGeneratedCasesData == true) {
                  actionButton =
                    actionButton +
                    '<li role="presentation"><a role="menuitem" class="listMenuOptions" href="' +
                    periodicReportConfig.viewCasesUrl +
                    "?id=" +
                    row.id +
                    "&isInDraftMode=" +
                    row.isInDraftMode +
                    '">' +
                    $.i18n._("periodicReport.view.case.series") +
                    "</a></li>";
                }
                if (row.actions) {
                  var actionsObj = row.actions.split(",");
                  for (var index = 0; index < actionsObj.length; index++) {
                    var localizedVal = $.i18n._(
                      "workFlowState.reportActionType." + actionsObj[index]
                    );
                    if (
                      row.hasGeneratedCasesData == true &&
                      actionsObj[index] == REPORT_ACTION_TYPE.GENERATE_CASES
                    )
                      actionButton =
                        actionButton +
                        '<li role="presentation" class="generateCases"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions generateCases" data-exconfig-id="' +
                        row.id +
                        '" >' +
                        localizedVal +
                        "</a></li>";
                    if (
                      row.hasGeneratedCasesData == true &&
                      actionsObj[index] == REPORT_ACTION_TYPE.GENERATE_DRAFT
                    )
                      actionButton =
                        actionButton +
                        '<li role="presentation" class="stateSpecificActions"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions generateDraft" data-exconfig-id="' +
                        row.id +
                        '" >' +
                        localizedVal +
                        "</a></li>";
                    if (
                      row.hasGeneratedCasesData == true &&
                      actionsObj[index] == REPORT_ACTION_TYPE.GENERATE_FINAL
                    )
                      actionButton =
                        actionButton +
                        '<li role="presentation" class="stateSpecificActions"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions generateFinalDraft" data-exconfig-id="' +
                        row.id +
                        '">' +
                        localizedVal +
                        "</a></li>";
                    if (
                      row.hasGeneratedCasesData == true &&
                      actionsObj[index] ==
                        REPORT_ACTION_TYPE.GENERATE_CASES_FINAL
                    )
                      actionButton =
                        actionButton +
                        '<li role="presentation" class="stateSpecificActions"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions generateCasesFinalDraft" data-exconfig-id="' +
                        row.id +
                        '">' +
                        $.i18n._(
                          "workFlowState.reportActionType.GENERATE_CASES_FINAL"
                        ) +
                        "</a></li>";
                    if (
                      row.hasGeneratedCasesData == true &&
                      actionsObj[index] ==
                        REPORT_ACTION_TYPE.GENERATE_CASES_DRAFT
                    )
                      actionButton =
                        actionButton +
                        '<li role="presentation" class="stateSpecificActions"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions generateCasesDraft" data-exconfig-id="' +
                        row.id +
                        '" >' +
                        $.i18n._(
                          "workFlowState.reportActionType.REFRESH_GENERATE_CASES_DRAFT"
                        ) +
                        "</a></li>";
                    if (
                      hasDmsIntegration &&
                      actionsObj[index] == REPORT_ACTION_TYPE.SEND_TO_DMS
                    )
                      actionButton =
                        actionButton +
                        '<li role="presentation" class="stateSpecificActions"><a href="javascript:void(0)" role="menuitem" class="sendToDms" id="' +
                        row.id +
                        '" data-toggle="modal" data-id="' +
                        row.id +
                        '" data-target="#sendToDmsModal">' +
                        $.i18n._("labelSendToDms") +
                        "</a></li>";
                    if (
                      actionsObj[index] == REPORT_ACTION_TYPE.MARK_AS_SUBMITTED
                    )
                      // we are using modal data load feature view url mentioned on the link itself
                      actionButton =
                        actionButton +
                        '<li role="presentation" class="stateSpecificActions"><a role="menuitem" class="listMenuOptions markAsSubmitted" data-toggle="modal" id="' +
                        row.id +
                        '" data-target="#reportSubmissionModal" href="#" data-url="' +
                        periodicReportConfig.markAsSubmittedUrl +
                        "?id=" +
                        row.id +
                        '">' +
                        localizedVal +
                        "</a></li>";
                  }
                }
                actionButton = actionButton + '<li class="divider"></li>';
                if (hasAccessOnActionItem) {
                  actionButton =
                    actionButton +
                    '<li role="presentation" class="stateSpecificActions"><a href="#" role="menuitem" class="listMenuOptions createActionItem" data-exconfig-id="' +
                    row.id +
                    '">' +
                    $.i18n._(
                      "workFlowState.reportActionType.CREATE_ACTION_ITEM"
                    );
                  +"</a></li>";
                }
                actionButton =
                  actionButton +
                  '<li role="presentation" class="stateSpecificActions"><a href="#" role="menuitem"  id="' +
                  row.id +
                  '" data-toggle="modal" data-target="#sharedWithModal">' +
                  $.i18n._("labelShare") +
                  "</a></li>";
                actionButton =
                  actionButton +
                  '<li role="presentation" class="stateSpecificActions"><a href="#" role="menuitem" id="' +
                  row.id +
                  '" data-toggle="modal" data-target="#emailToModal">' +
                  $.i18n._("labelEmailTo") +
                  "</a></li>";
                actionButton =
                  actionButton +
                  '<li role="presentation"><a role="menuitem" href="#" data-toArchived="' +!row.isArchived+ '" data-url="' + toArchive + '?id=' + row.id + '" data-evt-clk=\'{"method": "periodicReportListConfirmArchive", "params": []}\'  >' +
                  (row.isArchived
                    ? $.i18n._("labelUnArchive")
                    : $.i18n._("labelArchive")) +
                  "</a></li> ";
                actionButton =
                  actionButton +
                  '<li role="presentation"><a role="menuitem" href="#" data-toggle="modal" data-deleteforallallowed="true" data-target="#deleteModal" data-controller="report" data-action="delete" data-instancetype="' +
                  $.i18n._("configuration") +
                  '" data-instanceid="' +
                  row.id +
                  '" data-instancename="' +
                  replaceBracketsAndQuotes(data["reportName"]) +
                  '">' +
                  $.i18n._("labelDelete") +
                  "</a></li>";
                if (typeof isAdmin !== "undefined" && isAdmin)
                  actionButton =
                    actionButton +
                    '<li role="presentation"><a href="' +
                    periodicReportConfig.comparisonUrl +
                    "?id=" +
                    row.id +
                    '" role="menuitem"  >' +
                    $.i18n._("comparison.run") +
                    "</a></li>";
                actionButton = actionButton + "</ul></div>";

                //Return the action buttons.
                return actionButton;
              },
            },
          ];
          if (periodicReportConfig.publisher) {
            columns.splice(14, 0, {
              data: "publisher",
              render: function (data, type, row) {
                if (data) {
                  return (
                    '<a class="btn btn-success btn-xs" href="' +
                    periodicReportConfig.reportViewUrl +
                    "?id=" +
                    row.id +
                    "&isInDraftMode=" +
                    row.isInDraftMode +
                    '" >' +
                    $.i18n._("Completed") +
                    "</a> "
                  );
                } else {
                  return (
                    '<a class="btn btn-warning btn-xs" href="' +
                    periodicReportConfig.reportViewUrl +
                    "?id=" +
                    row.id +
                    "&isInDraftMode=" +
                    row.isInDraftMode +
                    '" >' +
                    $.i18n._("InProgress") +
                    "</a> "
                  );
                }
              },
            });
            columns.splice(14, 0, {
              data: "contributor",
            });
          }
          return columns;
        })(),
      })
      .on("draw.dt", function () {
        updateTitleForThreeRowDotElements();
      })
      .on("xhr.dt", function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
      });

    var init_filter = function () {
      var filter_data = [
        {
          label: $.i18n._("app.advancedFilter.reportName"),
          type: "text",
          name: "reportName",
          maxlength: 500,
        },
        {
          label: $.i18n._("app.advancedFilter.version"),
          type: "natural-number",
          name: "numOfExecutions",
        },
        {
          label: $.i18n._("app.advancedFilter.email"),
          type: "text",
          name: "email",
        },
        {
          label: $.i18n._("app.advancedFilter.primaryReportingDestination"),
          type: "text",
          name: "primaryReportingDestination",
          maxlength: 255,
        },
        {
          label: $.i18n._("app.advancedFilter.dueDateStart"),
          type: "date-range",
          group: "dueDate",
          group_order: 1,
        },
        {
          label: $.i18n._("app.advancedFilter.dueDateEnd"),
          type: "date-range",
          group: "dueDate",
          group_order: 2,
        },
        {
          label: $.i18n._("app.advancedFilter.dateModifiedStart"),
          type: "date-range",
          group: "lastUpdated",
          group_order: 1,
        },
        {
          label: $.i18n._("app.advancedFilter.dateModifiedEnd"),
          type: "date-range",
          group: "lastUpdated",
          group_order: 2,
        },
        {
          label: $.i18n._("app.advancedFilter.reportOwner"),
          type: "id",
          name: "owner",
        },
        {
          label: $.i18n._("app.advancedFilter.tag"),
          type: "select2-multi-id",
          name: "tag",
          ajax: {
            url: "/reports/tag",
            data_handler: function (data) {
              return pvr.filter_util.build_options(data, "id", "name", false);
            },
            error_handler: function (data) {
              console.log(data);
            },
          },
        },
        {
          label: $.i18n._("app.advancedFilter.state"),
          type: "select2-id",
          name: "workflowState",
          ajax: {
            url: "/reports/workflowState/list",
            data_handler: function (data) {
              return pvr.filter_util.build_options(
                data,
                "workflowStateId",
                "name",
                true
              );
            },
            error_handler: function (data) {
              console.log(data);
            },
          },
        },
      ];

      pvr.filter_util.construct_right_filter_panel({
        table_id: "#periodicReportList",
        container_id: "config-filter-panel",
        filter_defs: filter_data,
        column_count: 1,
        done_func: function (filter) {
          tableFilter = filter;
          advancedFilter = true;
          var dataTable = $("#periodicReportList").DataTable();
          dataTable.ajax.reload(function (data) {}, false).draw();
        },
      });
      bindSelect2WithUrl(
        $("select[data-name=owner]"),
        ownerListUrl,
        ownerValuesUrl,
        true
      ).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
          searchField[0].focus();
        }
      });

      $("select[data-name=workflowState]").select2().on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
          searchField[0].focus();
        }
      });
    };

    init_filter();

    loadTableOption("#periodicReportList");
    return periodic_report_table;
  };

  var generateReport = function (exConfigId, reportAction) {
    if (exConfigId != undefined && exConfigId != "") {
      highlightRow(exConfigId);
      var urlToHit =
        periodicReportConfig.generateDraftUrl +
        "?id=" +
        exConfigId +
        "&reportAction=" +
        reportAction;
      $.ajax({
        url: urlToHit,
        dataType: "json",
      })
        .done(function (result) {
          if (result.warning) {
            warningNotification(result.message);
            return;
          }
          successNotification(result.message);
          setTimeout(function () {
            reloadData(exConfigId);
          }, 1000);
        })
        .fail(function (err) {
          errorNotification("Server Error!");
        });
    }
  };

  var confirmArchive = function (toArchive, url) {
    var confirmationModal = $("#confirmationModal");
    confirmationModal.modal("show");
    confirmationModal
      .find(".modalHeader")
      .html($.i18n._("app.archive.confirmation.title"));
    confirmationModal.find(".okButton").html($.i18n._("yes"));
    if (toArchive)
      confirmationModal
        .find(".confirmationMessage")
        .html($.i18n._("app.archive.confirmation.toArchive"));
    else
      confirmationModal
        .find(".confirmationMessage")
        .html($.i18n._("app.archive.confirmation.fromArchive"));
    confirmationModal
      .find(".okButton")
      .off()
      .on("click", function () {
        document.location.href = url;
      });
  };

  var reloadData = function (rowId, resetPagination) {
    if (resetPagination != true) {
      resetPagination = false;
    }
    var t = $("#periodicReportList");
    if (t.length === 0) location.reload();
    var dataTable = t.DataTable();
    dataTable.ajax.reload(function () {
      highlightRow(rowId);
    }, resetPagination);
  };

  var highlightRow = function (rowId) {
    if (rowId != undefined && rowId != "") {
      var dataTable = $("#periodicReportList").DataTable();
      $(dataTable
        .row("#" + rowId)
        .node())
        .addClass("flash-row");
    }
  };

  var submission_modal_after_load = function () {
    var reportSubmissionModal = $("#reportSubmissionModal");
    reportSubmissionModal
      .on("shown.bs.modal", function (e) {
        var rowId = $(e.relatedTarget).parents("tr").attr("id");
        showModalLoader(reportSubmissionModal);
        reportSubmissionModal
          .find(".modal-content:first")
          .load($(e.relatedTarget).data("url"), function () {
            reportSubmissionModal
              .find(".modal-content:first")
              .removeClass("rxloading");
            hideModalLoader(reportSubmissionModal);
            var submissionDate = reportSubmissionModal
              .find('.datepicker input[name="submissionDate"]')
              .val();
            $(".timezone-container").css("margin-top", "8px");
            $("#submissionDateDiv").datepicker({
              allowPastDates: true,
              date: submissionDate,
              twoDigitYearProtection: true,
              culture: userLocale,
              momentConfig: {
                format: DEFAULT_DATE_DISPLAY_FORMAT,
              },
            });
            var dueDate = reportSubmissionModal
              .find('.datepicker input[name="dueDate"]')
              .val();
            $("#addDueDateDiv").datepicker({
              allowPastDates: true,
              date: dueDate,
              twoDigitYearProtection: true,
              culture: userLocale,
              momentConfig: {
                format: DEFAULT_DATE_DISPLAY_FORMAT,
              },
            });
            $("[name=file]").on("change", function (evt, numFiles, label) {
              $("#file_name").val(
                $.map($("[name=file]")[0].files, function (val) {
                  return val.name;
                }).join(";")
              );
            });

            function checkLate() {
              if (
                typeof isRodProcessingEnabled != "undefined" &&
                isRodProcessingEnabled
              ) {
                var dueDate = moment(
                  $("#dueDate").val(),
                  DEFAULT_DATE_DISPLAY_FORMAT
                );
                var submissionDate = moment(
                  $("#submissionDate").val(),
                  DEFAULT_DATE_DISPLAY_FORMAT
                );
                if (submissionDate.isAfter(dueDate)) {
                  $(".late").show();
                  $("#late").val("LATE").trigger("change");
                } else {
                  $(".late").hide();
                  $("#late").val("NOT_LATE").trigger("change");
                }
              }
            }

            $(document).on(
              "changed.fu.datepicker dateClicked.fu.datepicker",
              "#addDueDateDiv, #submissionDateDiv",
              function () {
                checkLate();
              });
            checkLate();
            buildReportingDestinationsSelectBox(
              reportSubmissionModal.find("[name=reportingDestinations]"),
              periodicReportConfig.reportingDestinationsUrl,
              reportSubmissionModal.find(
                "input[name='primaryReportingDestination']"
              ),
              false
            )
              .on("change", function () {
                var val = $(this).select2("val");
                if (val) {
                  $.ajax({
                    url: "/reports/reportSubmissionRest/getEmails",
                    data: { destinations: val },
                    dataType: "json",
                  }).done(function (data) {
                    if (data && data.emails) {
                      var emails = reportSubmissionModal
                        .find(".emailUsers")
                        .select2("val");
                      if (!emails || (emails.length == 0)) emails = data.emails;
                      else emails = emails.concat(data.emails);
                      reportSubmissionModal
                        .find(".emailUsers")
                        .select2("val", emails);
                    }
                    if (
                      data &&
                      data.templates &&
                        (data.templates.length > 0) &&
                        (typeof templateContent !== "undefined")
                    ) {
                      templateContent = data.templates;
                      setTemplateContent(templateContent[0].id);
                      $("select[name=emailConfiguration\\.to]").select2(
                        "val",
                        reportSubmissionModal.find(".emailUsers").select2("val")
                      );
                      $("#saveEmailConfiguration").trigger("click");
                    }
                  });
                }
              })
              .trigger("change");
            $(document).on("change", "#publisherDocument", function () {
              var destination = $(this)
                .find("option:selected")
                .attr("data-destination");
              var select = reportSubmissionModal.find(
                "[name=reportingDestinations]"
              );
              select
                .val(destination ? destination.split(";") : "")
                .trigger("change");
            });
            reportSubmissionModal
              .find("button.submit-draft")
              .on("click", function () {
                var jForm = new FormData(reportSubmissionModal.find("form")[0]);
                var configData = new FormData(
                  $("#emailConfiguration").closest("form")[0]
                );
                var fData = Array.from(configData.entries());
                for (var i in fData) {
                  if (!jForm.get(fData[i][0])) {
                    jForm.append(fData[i][0], fData[i][1]);
                  }
                }
                showLoader();
                $.ajax({
                  url: periodicReportConfig.reportSubmitUrl,
                  type: "POST",
                  data: jForm,
                  mimeType: "multipart/form-data",
                  contentType: false,
                  cache: false,
                  processData: false,
                  dataType: "json",
                })
                  .done(function (resp) {
                    hideLoader();
                    var result =
                      typeof resp === "string" ? JSON.parse(resp) : resp;
                    reportSubmissionModal.modal("hide");
                    successNotification(result.message);
                    if (
                      typeof isRodProcessingEnabled != "undefined" &&
                      isRodProcessingEnabled &&
                      isPvp &&
                      result.capa &&
                      result.capa.length > 0
                    ) {
                      var capa = JSON.parse(result.capa);
                      if (capa.length > 0) {
                        for (var i = 0; i < capa.length; i++) {
                          var link =
                            capaCreateUrl +
                            "?configurationId=" +
                            result.configurationId +
                            "&submissionId=" +
                            capa[i].id;
                          $("#capaLinkList").append(
                            '<br><a target="_blank" href="' +
                              link +
                              '"  >Initiate CAPA process for ' +
                              capa[i].destination +
                              " >>></a>"
                          );
                        }
                        $("#capaOfferModal").modal("show");
                      }
                    }
                    reloadData(rowId);
                  })
                  .fail(function (err) {
                    hideLoader();
                    var responseText = err.responseText;
                    var responseTextObj = JSON.parse(responseText);
                    if (responseTextObj.errors != undefined) {
                      $.each(responseTextObj.errors, function (index, e) {
                        var field = reportSubmissionModal
                          .find("form")
                          .find('[name="' + e + '"]');
                        if (field != undefined) {
                          field.parent().addClass("has-error");
                        }
                      });
                    }
                    if (
                      reportSubmissionModal.find("form").find(".has-error")
                        .length == 0
                    ) {
                      var errorPanel = $("#error-panel");
                      errorPanel.html("");
                      if (
                        responseTextObj.errorMsg &&
                        responseTextObj.errorMsg.length > 0
                      ) {
                          errorPanel.html("");
                          errorPanel.append('<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>');
                          responseTextObj.errorMsg.forEach(function (item) {
                              errorPanel.append(item + "<br/>");
                          });
                      } else {
                        errorPanel.append(responseTextObj.defaultMsg);
                      }

                      errorPanel.css("display", "block");
                      errorPanel.removeClass("hide");
                    }
                  });
              });
          });
      })
      .on("hidden.bs.modal", function () {
        reportSubmissionModal.removeData("bs.modal");
      });
  };

  return {
    initSpecialActions: initSpecialActions,
    init_periodic_report_table: init_periodic_report_table,
    confirmArchive: confirmArchive,
    submission_modal_after_load: submission_modal_after_load,
  };
})();

$(document).on("data-clk", function (event, elem) {
  var elemClkData = JSON.parse(elem.attributes["data-evt-clk"].value)
  const methodName = elemClkData.method;
  const params = elemClkData.params;

  if (methodName == 'periodicReportListConfirmArchive') {
    var toArchived = elem.attributes['data-toArchived'].value;
    var url = elem.attributes['data-url'].value;
    periodicReport.periodicReportList.confirmArchive(toArchived, url);
  }
})
