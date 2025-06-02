REPORT_STATUS_ENUM = {
  NEW: "NEW",
  NON_REVIEWED: "NON_REVIEWED",
  REVIEWED: "REVIEWED",
};
ACTION_ITEM_GROUP_STATE_ENUM = {
  WAITING: "WAITING",
  OVERDUE: "OVERDUE",
  CLOSED: "CLOSED",
};

$(function () {
  var INVALID_DATE = "Invalid Date";
  var tableFilter = {};
  var advancedFilter = false;

  function addReportNameParamTableFilter() {
    var reportName = $("#reportName").val();
    if (reportName) {
      if (!tableFilter) {
        tableFilter = {};
      }
      tableFilter.reportName = {
        type: "manual",
        name: "reportName",
        value: reportName,
      };
    }
  }

  var table = $("#rxTableReports")
    .DataTable({
      //"sPaginationType": "bootstrap",
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
      language: { search: ''},
      stateSave: true,
      stateDuration: -1,
      initComplete: function () {
        initSharedWithFilter("rxTableReports", table);
        initArchiveFilter(table);
        $("#rxTableReports").on("click", ".createActionItem", function () {
          actionItem.actionItemModal.set_executed_report_id(
            $(this).data("exconfigId")
          );
          actionItem.actionItemModal.init_action_item_modal(
            false,
            ADHOC_REPORT
          );
        });

        $("#rxTableReports").on("click ", ".actionItemModalIcon", function () {
          actionItem.actionItemModal.set_executed_report_id(
            $(this).data("exconfigId")
          );
          actionItem.actionItemModal.view_action_item_list(
            hasAccessOnActionItem,
            false,
            ADHOC_REPORT
          );
        });
        $("#rxTableReports").on("click", ".favorite", function () {
          changeFavoriteState(
            $(this).data("exconfig-id"),
            $(this).hasClass("glyphicon-star-empty"),
            $(this)
          );
        });
      },
      customProcessing: true, //handled using processing.dt event
      serverSide: true,
      ajax: {
        url: indexReportUrl,
        dataSrc: "data",
        contentType: "application/x-www-form-urlencoded",
        type: "POST",
        data: function (d) {
          addReportNameParamTableFilter();
          d.tableFilter = JSON.stringify(tableFilter);
          d.advancedFilter = advancedFilter;
          d.searchString = d.search.value;
          d.forPvq = $("#forPvq").val();
          d.sharedwith = $("#sharedWithFilterControl").val();
          d.includeArchived = $("#includeArchived").is(":checked");
          if (d.order.length > 0) {
            d.direction = d.order[0].dir;
            //Column header mData value extracting
            d.sort = d.columns[d.order[0].column].data;
          }

          if ($('input[name="relatedReports"]').length > 0) {
            d.status = $('input[name="relatedReports"]:checked').val();
          }
        },
      },
      aaSorting: [],
      order: [
        [0, "asc"],
        [5, "desc"],
      ],
      bLengthChange: true,
      pagination: true,

      drawCallback: function (settings) {
        initEvtChange();
        pageDictionary(
          $("#rxTableReports_wrapper")[0],
          settings.aLengthMenu[0][0],
          settings.json.recordsFiltered
        );
        getReloader($(".dataTables_info"), $("#rxTableReports"));
      },
      columnDefs: [
        { width: "25", targets: 0 },
        { width: 65, targets: 9 },
        { width: 100, targets: 10 },
        { orderable: false, targets: 0 },
      ],
      aoColumns: [
        {
          data: "isFavorite",
          sClass: "dataTableColumnCenter",
          asSorting: ["asc"],
          bSortable: true,
          render: renderFavoriteIcon,
        },
        {
          mData: "reportName",
          aTargets: ["reportName"],
          mRender: function (data, type, row) {
            var arch = row.isArchived
              ? '<span class="glyphicon glyphicon-text-background" title="' +
                $.i18n._("labelArchive") +
                '"></span> '
              : "";
            var link = showReportUrl + "/" + row.id;
            var content = encodeToHTML(data);
            content =
              "<div class='three-row-dot-overflow' >" + content + "</div>";
            return arch + "<a href=" + link + ">" + content + "</a>";
          },
        },
        {
          mData: "description",
          visible: $("#forPvq").val() != "true",
          mRender: function (data, type, row) {
            var content = data == null ? "" : encodeToHTML(data);
            return "<div class='three-row-dot-overflow'>" + content + "</div>";
          },
        },
        {
          mData: "numOfExecutions",
          aTargets: ["numOfExecutions"],
          sClass: "dataTableColumnCenter",
        },
        { mData: "owner" },

        {
          mData: "dateCreated",
          aTargets: ["dateCreated"],
          sClass: "dataTableColumnCenter forceLineWrapDate nowrap",
          mRender: function (data, type, full) {
            var dateCreated = Date.parse(data);
            return moment
              .utc(data)
              .tz(userTimeZone)
              .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
          },
        },
        {
          mData: "tags",
          visible: $("#forPvq").val() != "true",
          bSortable: false,
          aTargets: ["tags"],
          mRender: function (data, type, full) {
            var tags = data ? encodeToHTML(data) : "";
            return "<div class='three-row-dot-overflow'>" + tags + "</div>";
          },
        },
        {
          mData: "state",
          visible: $("#forPvq").val() != "true",
          mRender: function (data, type, row) {
              var stateName = row.state;
              // Truncate state name if it exceeds 30 characters
              var truncatedStateName = stateName.length > 30 ? stateName.substring(0, 27) + '...' : stateName;

              return ('<button class="btn btn-default btn-xs" style="min-width: 100px" data-executed-config-id= "' +
              row.id + '" data-initial-state= "' + row.state + '" data-evt-clk=\'{\"method\": \"openStateHistoryModal\", \"params\": []}\' title="' + stateName + '">' + truncatedStateName + "</button>");
          },
        },
        {
          mData: "dateCreated",
          aTargets: ["dateCreated"],
          bVisible: false, // this is a hack for date filter
          mRender: function (data, type, full) {
            return moment.utc(data).tz(userTimeZone).format("L");
          },
        },
        {
          data: null,
          sClass: "dataTableColumnCenter",
          bSortable: false,
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
                $.i18n._("app.actionItemGroupState." + row.actionItemStatus) +
                "</button>";
            }
            return creationAction;
          },
        },
        {
          mData: null,
          bSortable: false,
          sClass: "dataTableColumnCenter",
          aTargets: ["id"],
          mRender: function (data, type, row) {
            var actionButton =
              '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                    <a class="btn btn-success btn-xs" href="' +
              showReportUrl +
              "/" +
              data["id"] +
              '">' +
              $.i18n._("view") +
              '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" href="" id="' +
              row.id +
              '" data-toggle="modal" data-target="#sharedWithModal">' +
              $.i18n._("labelShare") +
              '</a></li> \
                                <li role="presentation"><a role="menuitem" href="" id="' +
              row.id +
              '" data-toggle="modal" data-target="#emailToModal">' +
              $.i18n._("labelEmailTo") +
              "</a></li> ";
            if (hasDmsIntegration)
              actionButton =
                actionButton +
                '<li role="presentation" class="stateSpecificActions"><a href="#" class="sendToDms" role="menuitem" data-id="' +
                row.id +
                '" data-toggle="modal" data-target="#sendToDmsModal">' +
                $.i18n._("labelSendToDms") +
                "</a></li>";
            if (hasAccessOnActionItem)
              actionButton =
                actionButton +
                '<li role="presentation" class="stateSpecificActions"><a href="#" role="menuitem" class="listMenuOptions createActionItem" data-exconfig-id="' +
                row.id +
                '">' +
                $.i18n._("workFlowState.reportActionType.CREATE_ACTION_ITEM") +
                "</a></li>";
            actionButton +=
              ' <li role="presentation"><a role="menuitem" href="javascript:void(0)" class="downloadUrl" data-name="' +
              encodeToHTML(row.reportName) +
              '.docx" data-url="' +
              LINKS.toWord +
              "&id=" +
              row.id +
              '">' +
              $.i18n._("labelExportTo", "Word") +
              '</a></li> \
                                <li role="presentation"><a role="menuitem" href="javascript:void(0)" class="downloadUrl" data-name="' +
              encodeToHTML(row.reportName) +
              '.xlsx" data-url="' +
              LINKS.toExcel +
              "&id=" +
              row.id +
              '">' +
              $.i18n._("labelExportTo", "Excel") +
              '</a></li> \
                                <li role="presentation"><a role="menuitem" href="javascript:void(0)" class="downloadUrl" data-name="' +
              encodeToHTML(row.reportName) +
              '.pdf" data-url="' +
              LINKS.toPDF +
              "&id=" +
              row.id +
              '">' +
              $.i18n._("labelExportTo", "PDF") +
              '</a></li> \
                                <li role="presentation"><a role="menuitem" href="javascript:void(0)" class="downloadUrl" data-name="' +
              encodeToHTML(row.reportName) +
              '.pptx" data-url="' +
              LINKS.toPowerPoint +
              "&id=" +
              row.id +
              '">' +
              $.i18n._("labelExportTo", "PowerPoint") +
              '</a></li> \
                                <li role="presentation"><a role="menuitem" href="#" data-toArchived="' +!row.isArchived+ '" data-url="' + LINKS.toArchive + '?id=' + row.id + '" data-evt-clk=\'{"method": "confirmArchive", "params": []}\' >' +
              (row.isArchived
                ? $.i18n._("labelUnArchive")
                : $.i18n._("labelArchive")) +
              '</a></li> \
                                <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                        data-target="#deleteModal" data-deleteforallallowed="true" data-instancetype="' +
              $.i18n._("configuration") +
              '" data-instanceid="' +
              row.id +
              '" data-instancename="' +
              replaceBracketsAndQuotes(data["reportName"]) +
              '">' +
              $.i18n._("labelDelete") +
              "</a></li> ";

            if (typeof isAdmin !== "undefined" && isAdmin && $("#forPvq").val() != "true")
              actionButton +=
                '<li role="presentation"><a href="' +
                comparisonUrl +
                "?id=" +
                row.id +
                '" role="menuitem"  >' +
                $.i18n._("comparison.run") +
                "</a></li>";
            actionButton += " </ul></div>";
            return actionButton;
          },
        },
      ],
    })
    .on("draw.dt", function () {
      updateTitleForThreeRowDotElements();
    })
    .on("xhr.dt", function (e, settings, json, xhr) {
      checkIfSessionTimeOutThenReload(e, json);
    });

  actionButton("#rxTableReports");
  loadTableOption("#rxTableReports");

  $(".outside").hide();

  var init_table_filter = function () {
    var filter_data = [
      {
        label: $.i18n._("app.advancedFilter.reportName"),
        type: "text",
        name: "reportName",
        value: $("#reportName").val() || "",
        disabled: !!$("#reportName").val(),
        maxlength: 500,
      },
      {
        label: $.i18n._("app.advancedFilter.description"),
        type: "text",
        name: "description",
        maxlength: 4000,
      },
      {
        label: $.i18n._("app.advancedFilter.email"),
        type: "text",
        name: "email",
      },
      {
        label: $.i18n._("app.advancedFilter.version"),
        type: "natural-number",
        name: "numOfExecutions",
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
        label: $.i18n._("app.advancedFilter.dateGeneratedStart"),
        type: "date-range",
        group: "dateCreated",
        group_order: 1,
      },
      {
        label: $.i18n._("app.advancedFilter.dateGeneratedEnd"),
        type: "date-range",
        group: "dateCreated",
        group_order: 2,
      },
      {
        label: $.i18n._("app.advancedFilter.owner"),
        type: "id",
        name: "owner",
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
      table_id: "#rxTableReports",
      container_id: "config-filter-panel",
      filter_defs: filter_data,
      column_count: 1,
      done_func: function (filter) {
        tableFilter = filter;
        advancedFilter = true;
        var dataTable = $("#rxTableReports").DataTable();
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

  init_table_filter();
});

$(document).on("data-clk", function (event, elem) {
  var elemClkData = JSON.parse(elem.attributes["data-evt-clk"].value)
  const methodName = elemClkData.method;
  const params = elemClkData.params;

  if(methodName == 'confirmArchive') {
    var toArchived =elem.attributes['data-toArchived'].value;
    var url = elem.attributes['data-url'].value;
    confirmArchive(toArchived, url);
  }
})


let initEvtChange = (function () {
  $("[data-evt-change]").on('change', function(e) {
    e.preventDefault();
    const eventData = JSON.parse($(this).attr("data-evt-change"));
    const methodName = eventData.method;
    const params = eventData.params;

    if(methodName == 'checkStatus') {
      var elem = $(this);
      checkStatus(elem);
    }
  });
});

function confirmArchive(toArchive, url) {
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
}

function getRadios(toolbar) {
  var currentPage = window.location.pathname;
  var radios = '<div class="col-xs-5 radioButtonGroup">';
  radios =
    radios +
    '<label class="no-bold add-cursor"><input value="new" id="indexRadio" type="radio" name="relatedReports" data-evt-change=\'{\"method\": \"checkStatus\", \"params\": []}\' checked /> ' +
    $.i18n._("labelNew") +
    "</label>";
  radios =
    radios +
    '<label class="no-bold add-cursor radioAll"><input value="archived" id="archivedRadio" type="radio" name="relatedReports" data-evt-change=\'{\"method\": \"checkStatus\", \"params\": []}\' /> ' +
    $.i18n._("labelReviewed") +
    "</label>";
  radios =
    radios +
    '<label class="no-bold add-cursor radioAll"><input value="underReview" id="listAllRadio" type="radio" name="relatedReports" data-evt-change=\'{\"method\": \"checkStatus\", \"params\": []}\' /> ' +
    $.i18n._("labelNonReviewed") +
    "</label>";
  radios = $(radios);
  $(toolbar).append(radios);
}

function updateStatus(newStatus, rowId) {
  $.ajax({
    type: "GET",
    url: updateStatusUrl,
    data: { id: rowId, reportStatus: newStatus },
    dataType: "json",
  }).done(function (data) {
    console.log("Load was performed.");
  });
}

function checkStatus(elem) {
  $("#rxTableReports")
    .DataTable()
    .order([[4, "desc"]])
    .draw();
}
