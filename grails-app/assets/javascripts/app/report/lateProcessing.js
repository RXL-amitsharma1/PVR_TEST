ACTION_ITEM_GROUP_STATE_ENUM = {
  WAITING: "WAITING",
  OVERDUE: "OVERDUE",
  CLOSED: "CLOSED",
};
BULK_UPDATE_MAX_ROWS = 500;
ROD_FIELD_ENUM = {
  ISSUE_TYPE: "Issue_Type",
  ROOT_CAUSE: "Root_Cause",
  ROOT_CAUSE_CLASS: "Root_Cause_Class",
  ROOT_CAUSE_SUB_CAT: "Root_Cause_Sub_Cat",
  RESP_PARTY: "Resp_Party",
  CORRECTIVE_ACTION: "Corrective_Action",
  PREVENTIVE_ACTION: "Preventive_Action",
  CORRECTIVE_DATE: "Corrective_Date",
  PREVENTIVE_DATE: "Preventive_Date",
  INVESTIGATION: "Investigation",
  SUMMARY: "Summary",
  ACTIONS: "Actions",
};

var reportDataTable;
var tableDataAjax = [];
var columns = [];
var groupColumnsIndexes = [];
var lateId = "";
var rootCauseId = "";
var result = [];
var finalResult = [];
var metadataMasterCaseId;
var metadataProcessedReportId;
var metadataTenantId;
var selectedIds = [];
var selectAll;
var selectedAttachIds = [];
var capaInsId;
var nonEditableFields = [];
var correctiveDate = false;
var preventiveDate = false;
var recordsTotal = 0
var appliedPageFilter = {};
const linkFilter = getUrlParameter('linkFilter');
const hasLargeIds = getUrlParameter('hasLargeIds');
function getUrlParameter(name) {
  const url = new URL(window.location.href);
  const param = url.searchParams.get(name);
  try {
    return param ? JSON.parse(param) : null;
  } catch (e) {
    return param;
  }
}

$(function (event) {
  showDataTableLoader($("#tableDiv"));

  if (rowColumns.length > 0) {
    columns.push({
      className: "details-control",
      width: "50px",
      orderable: false,
      data: null,
      defaultContent: "<span class='glyphicon glyphicon-triangle-right'></span>",
    });
  }
  for (var i in header) {
    if (
      header[i] === "cllRowId" ||
      header[i] === "actionItemStatus" ||
      header[i] === "latestComment" ||
      header[i] === "workFlowState" ||
      header[i] === "finalState" ||
      header[i] === "assignedToUser" ||
      header[i] === "assignedToUserId" ||
      header[i] === "assignedToGroup" ||
      header[i] === "assignedToGroupId" ||
      header[i] === "dueInDays" ||
      header[i] === "indicator" ||
      header[i] === "hasAttachments" ||
      header[i] === "hasIssues" ||
      _.find(serviceColumns, function (it) {
        return it == header[i];
      })) {
      continue;
    }

    if (i == 0) {
      columns.push({
        title: '<div class="checkbox checkbox-primary"> ' +
          '<input type="checkbox" name="selectAll" style="margin-right: 4px; margin-top: 0; width: 16px; height: 16px;" class="selectAllCheckbox">' +
          '<label for="selectAll"></label>' +
          "</div>",
        orderable: false,
        width: '25px',
        className:"nowrap",
        render: function (data, type, row, meta) {
          return (
            '<div class="checkbox checkbox-primary"> ' +
            '<input type="checkbox" class="selectCheckbox"  style="margin-right: 1px; margin-top: 0; width: 16px; height: 16px;"  name="selected" data-tenant-id="' +
            row["masterEnterpriseId"] +
            '" data-case-id="' +
            row["masterCaseId"] +
            '" data-processed-report-id="' +
            row["vcsProcessedReportId"] +
            '" data-cll-row-id="' +
            row["cllRowId"] +
            '" data-case-num="' +
            row["masterCaseNum"] +
            '" data-version-num="' +
            row["masterVersionNum"] +
            '" data-sender-id="' +
            row["pvcIcSenderId"] +
            '" data-report-des="' +
            row["reportsAgencyId"] +
            '" data-due-date="' +
            row["reportsDueDate"] +
            '" data-submit-date="' +
            row["reportsDateSubmitted"] +
            '" data-root-cause="' +
            row["pvcLcpRootCause"] +
            '" data-responsible-party="' +
            row["pvcLcpRespParty"] +
            '"/> ' +
            "<label></label></div>"
          );
        },
      });
    }
    if (!_.contains(rowColumns, header[i])) {
      if (header[i] == "vcsProcessedReportId" || header[i] == "masterEnterpriseId" || header[i] == "masterCaseId" || header[i] == "pvcLcpId" || header[i] == "pvcLcpFlagPrimary") {
        //doing nothing
      } else {
        var inlineFilterType = {type: "text"}
        if (fieldTypeMap[header[i]] === "Number") inlineFilterType = {type: "number"}
        if (fieldTypeMap[header[i]] === "Date") inlineFilterType = {type: "date-range"}
        var col = {
          title: fieldsCodeNameMap[header[i]],
          mData: header[i],
          fieldName: header[i],
          inlineFilter: inlineFilterType,
          stackId: stacked[header[i]],
          width: '110px',
          sClass: "dataTableColumnCenter",
          render: function (data, type, row, meta) {
            return renderCell(data, row, meta.settings.aoColumns[meta.col].fieldName);
          }
        }
        columns.push(col)
      }
    }
  }

  columns.push({
    "className": 'pv-col-sm dataTableColumnCenter',
    title: $.i18n._('app.advancedFilter.dueIn'),
    width: "110px",
    mData: "dueInDays",
    inlineFilter: {type: "date-range"},
    render: function (data, type, row) {
      var clazz = "";
      if (row.indicator == "red") clazz = 'class="label-danger lbl-badge lbl-badge-danger"';
      if (row.indicator == "yellow") clazz = 'class="label-primary lbl-badge lbl-badge-warning"';
      return '<div style="height: 22px"><span ' + clazz + '>' + moment(row.dueInDays).format(DEFAULT_DATE_DISPLAY_FORMAT) + "</span></div>";
    }
  });
  columns.push({
    "className": 'pv-col-sm dataTableColumnCenter',
    title: $.i18n._('app.advancedFilter.assignedGroup'),
    width: "120px",
    mData: "assignedToGroup",
    inlineFilter: {type: "text"},
    stackId: 10001,
    render: function (data, type, row) {
      return formAssignedToUserGroupCell(row)
    }
  });
  columns.push({
    "className": 'pv-col-sm dataTableColumnCenter',
    title: $.i18n._('app.advancedFilter.assignedUser'),
    width: "120px",
    mData: "assignedToUser",
    inlineFilter: {type: "text"},
    stackId: 10001,
    render: function (data, type, row) {
      return formAssignedToUserCell(row)
    }
  });
  columns.push({
    className: "pv-col-sm",
    title: $.i18n._('app.advancedFilter.state'),
    mData: "workFlowState",
    width: "110px",
    inlineFilter: {type: "text"},
    stackId: 10002,
    render: function (data, type, row) {
      return '<div><button type="button" class="btn lbl-badge-default btn-xs workflowButton" style="min-width: 80px;margin-bottom: 2px" data-tenant-id="' + row['masterEnterpriseId'] + '" data-cll-row-id ="' + row['cllRowId'] + '"  data-case-id="' + row['masterCaseId'] + '" data-sender-id="' + row['pvcIcSenderId'] + '" data-version-num="' + row['masterVersionNum'] + '" data-processed-report-id="' + row['vcsProcessedReportId'] + '" data-initial-state= "' + row.workFlowState + '" data-evt-clk=\'{\"method\": \"openStateHistoryModal\", \"params\": []}\'>' + row.workFlowState + '</button></div>';
    }
  });
  columns.push({
    "className": 'pv-col-sm dataTableColumnCenter',
    title: null,
    width: "110px",
    mData: "actionItemStatus",
    stackId: 10002,
    bSortable: false,
    render: function (data, type, row) {
      var clazz = "";
      switch (row.actionItemStatus) {
        case ACTION_ITEM_GROUP_STATE_ENUM.OVERDUE:
          clazz = "btn lbl-badge-danger btn-xs";
          break;
        case ACTION_ITEM_GROUP_STATE_ENUM.WAITING:
          clazz = "btn lbl-badge-waiting btn-xs";
          break;
        case ACTION_ITEM_GROUP_STATE_ENUM.CLOSED:
          clazz = "btn lbl-badge-success btn-xs";
          break;
        default:
          clazz = null;
          break;
      }
      return (clazz === null) ? '<div style="height: 24px"></div>' : '<button type="button" class="' + clazz + ' btn-round actionItemModalIcon" data-tenant-id=' + row['masterEnterpriseId'] + ' ' +
          ' data-case-id=' + row['masterCaseId'] + '  data-processed-report-id=' + row['vcsProcessedReportId'] + ' data-sender-id=' + row['pvcIcSenderId'] + ' data-version-num=' + row['masterVersionNum'] + ' data-is-inbound=' + row['isInbound'] + ' data-configuration-id=' + configurationId + ' ' +
          'style="width:80px;margin-top: 2px">' + $.i18n._('app.actionItemGroupState.' + row.actionItemStatus) + '</button>';
    }
  });
    columns.push({
        sClass: "dataTableColumnCenter",
        visible:false,
        title: '<div ><i class="fa fa-paperclip fa-flip-horizontal" title="' + $.i18n._('lateProcessing.attachments') + '"></i></div><div class=\'pv-stacked-row\'><i class="fa fa-ticket" title="' + $.i18n._('qualityModule.issue.label') + '"></i></div>',
        customMenuLabel: $.i18n._('lateProcessing.attachments') + "<br>" + $.i18n._("qualityModule.issue.label"),
        width: "30px",
        mData:"hasAttachments",
        bSortable: false,
        render: function (data, type, row) {
            var attWidth = "style=\"width: 31px;margin-bottom: 2px\"";
            var attachment = $.i18n._('no');
            if (row['hasAttachments'] === 'true') {
                attachment = $.i18n._('yes');
                attWidth="";
            }
            issWidth="style=\"width: 34px;margin-top: 2px\"";
            var issue = $.i18n._('no');
            if (row['hasIssues'] === 'true') {
                issue = $.i18n._('yes')
                issWidth=""
            }
            attachment = '<a href="javascript:void(0)" '+attWidth+' class="uploadFile btn lbl-badge-default btn-xs" style="margin-bottom: 2px" data-viewMode="' + row['finalState'] + '">' + attachment + '</a>'
            issue = '<a href="javascript:void(0)" '+issWidth+' class="createIssue btn lbl-badge-default btn-xs" style="margin-top: 2px" data-viewMode="' + row['finalState'] + '" data-tenant-id="' + row['masterEnterpriseId'] + '" data-processed-report-id="' + row['vcsProcessedReportId'] + '" data-case-id="' + row['masterCaseId'] + '" data-case-num="' + row['masterCaseNum'] + '" data-report-des="' + row['reportsAgencyId'] + '" data-due-date="' + row['reportsDueDate'] + '" data-submit-date="' + row['reportsDateSubmitted'] + '" data-sender-id="' + row['pvcIcSenderId'] + '" data-version-num="' + row['masterVersionNum'] + '" data-is-inbound="' + row['isInbound'] + '">' +
                issue + "</a>";
            return '<div style=\'display: inline-flex;\'>' + attachment + '</div><div class=\'pv-stacked-row\'>' + issue + '</i></div>';
        }
    });

  columns.push({
    title: "&nbsp;",
    width: "30px",
    mData:"menu",
    bSortable: false,
    render: function (data, type, row) {
      var commentedIcon = "fa-comment-o";
      var meteData =
        "<input type='hidden' class='isRowUpdated' name='isRowUpdated' value='false'><input type='hidden' name='reportId' value='" +
        row["vcsProcessedReportId"] +
        "'/>" +
        "<input type='hidden' name='cllRowId' value='" +
        row["cllRowId"] +
        "'/>" +
        "<input type='hidden' name='enterpriseId' value='" +
        (row["masterEnterpriseId"] ? row["masterEnterpriseId"].trim() : "") +
        "'/>" +
        "<input type='hidden' name='caseId' value='" +
        (row["masterCaseId"] ? row["masterCaseId"].trim() : "") +
        "'/>" +
        "<input type='hidden' name='caseNum' value='" +
        (row["masterCaseNum"] ? row["masterCaseNum"].trim() : "") +
        "'/>" +
        "<input type='hidden' name='versionNumber' value='" +
        (row["masterVersionNum"] ? row["masterVersionNum"].trim() : "") +
        "'/>" +
        "<input type='hidden' name='senderId' value='" +
        (row["pvcIcSenderId"] ? row["pvcIcSenderId"] : "") +
        "'/>" +
        "<input type='hidden' name='isInbound' value='" +
        (row["isInbound"] ? row["isInbound"] : "") +
        "'/>" +
        "<input type='hidden' name='pvcLcpId' value='" +
        (row["pvcLcpId"] ? row["pvcLcpId"].trim() : "") +
        "'/>" +
        "<input type='hidden' name='pvcLcpFlagPrimary' value='" +
        (row["pvcLcpFlagPrimary"] ? row["pvcLcpFlagPrimary"].trim() : "") +
        "'/>";

      // var icons = "<span class=\"pv-grp-btn btn-group pull-right\">";
      var displayAlways = false;
      var icons = "";
      if (row["actionItemStatus"]) {
        displayAlways = true;
      }
      if (row["finalState"] !== "true") {
        if (row["hasIssues"] !== "false") {
          displayAlways = true;
          icons +=
            '<a href="javascript:void(0)" class="createIssue btn btn-success pv-btn-badge" data-tenant-id="' +
            row["masterEnterpriseId"] +
            '" data-processed-report-id="' +
            row["vcsProcessedReportId"] +
            '" data-case-id="' +
            row["masterCaseId"] +
            '" data-case-num="' +
            row["masterCaseNum"] +
            '" data-report-des="' +
            row["reportsAgencyId"] +
            '" data-due-date="' +
            row["reportsDueDate"] +
            '" data-submit-date="' +
            row["reportsDateSubmitted"] +
            '" data-sender-id="' +
            row["pvcIcSenderId"] +
            '" data-version-num="' +
            row["masterVersionNum"] +
            '" data-is-inbound="' +
            row["isInbound"] +
            '">' +
            '<i class="fa fa-ticket " data-placement="left" style="" title="' +
            $.i18n._("qualityModule.createIssue.label") +
            '"></i><span class="badge custom-badge badge-xs badge-pink"></span></a>';
        }
        if (row["hasAttachments"] === "true") {
          displayAlways = true;
          icons +=
            '<a href="javascript:void(0)" class="uploadFile pv-btn-badge btn btn-success" data-viewMode="' +
            row["finalState"] +
            '"><i class="fa fa-paperclip fa-flip-horizontal" data-placement="left" style="" title="' +
            $.i18n._("lateProcessing.attachments") +
            '"></i><span class="badge custom-badge badge-xs badge-pink"></span></a>';
        }
        if (
          (row["pvcLcpLate"] != null && row["pvcLcpLate"].length > 0) ||
          (row["pvcIcFlagLate"] != null && row["pvcIcFlagLate"].length > 0)
        ) {
          displayAlways = true;
          icons +=
            '<a href="#" class="reasonOfDelayModalBtn pv-btn-badge btn btn-success" data-viewMode="' +
            row["finalState"] +
            '"><i class="fa fa-pencil" data-placement="left" style="" title="' +
            $.i18n._("lateProcessing.viewallRCAs") +
            '"></i><span class="badge custom-badge badge-xs badge-pink"></span></a>';
        }
      }

      if (row["latestComment"] !== null) {
        displayAlways = true;
        commentedIcon = "fa-commenting-o commentPopoverMessage";
        icons +=
          '<a href="#" data-owner-id="' +
          row["cllRowId"] +
          '" data-comment-type="DRILLDOWN_RECORD" data-toggle="modal" data-target="#commentModal" data-viewmode="' +
          row["finalState"] +
          '" class="btn btn-success pv-btn-badge commentModalTrigger m-w-160">' +
          '<span class="annotationPopover"><i class=" fa ' +
          commentedIcon +
          ' " data-content="' +
          row["latestComment"] +
          '" data-placement="left" style="z-index: 99999" title="' +
          $.i18n._("app.caseList.comment") +
          '"><span class="badge custom-badge badge-xs badge-pink"></span></i></span></a>';
      }
      icons +=
        '<a href="javascript:void(0)" class="btn btn-success dropdown-toggle view-hover" data-toggle="dropdown" aria-expanded="true" data-viewMode="' +
        row["finalState"] +
        '"><span><i class="md-dots-vertical" data-placement="left" style="" title=""></i></span></a>';
      icons +=
        '<ul class="dropdown-menu dropdown-menu-right sec-value action-list-width" role="menu">' +
        '<li><a role="menuitem" href="javascript:void(0)" class="createActionItem" data-cll-row-id="' +
        row["cllRowId"] +
        '" data-tenant-id="' +
        row["masterEnterpriseId"] +
        '" data-case-id="' +
        row["masterCaseId"] +
        '" data-processed-report-id="' +
        row["vcsProcessedReportId"] +
        '" data-sender-id="' +
        row["pvcIcSenderId"] +
        '" data-version-num="' +
        row["masterVersionNum"] +
        '" data-is-inbound="' +
        row["isInbound"] +
        '" data-configuration-id=' +
        configurationId +
        ">" +
        $.i18n._("workFlowState.reportActionType.CREATE_ACTION_ITEM") +
        "</a></li>" +
        '<li><a role="menuitem" href="javascript:void(0)" class="createIssue" data-tenant-id="' +
        row["masterEnterpriseId"] +
        '" data-processed-report-id="' +
        row["vcsProcessedReportId"] +
        '" data-case-id="' +
        row["masterCaseId"] +
        '" data-case-num="' +
        row["masterCaseNum"] +
        '" data-report-des="' +
        row["reportsAgencyId"] +
        '" data-due-date="' +
        row["reportsDueDate"] +
        '" data-submit-date="' +
        row["reportsDateSubmitted"] +
        '" data-root-cause="' +
        row["pvcLcpRootCause"] +
        '" data-responsible-party="' +
        row["pvcLcpRespParty"] +
        '" data-sender-id="' +
        row["pvcIcSenderId"] +
        '" data-version-num="' +
        row["masterVersionNum"] +
        '" data-is-inbound="' +
        row["isInbound"] +
        '">' +
        $.i18n._("qualityModule.createIssue.label") +
        "</a></li>" +
        '<li><a role="menuitem" href="javascript:void(0)" class="uploadFile" data-viewMode="' +
        row["finalState"] +
        '">' +
        $.i18n._("lateProcessing.attachments") +
        "</a></li>" +
        '<li><a role="menuitem" href="#" class="commentModalTrigger" data-owner-id="' +
        row["cllRowId"] +
        '" data-comment-type="DRILLDOWN_RECORD" data-toggle="modal" data-target="#commentModal" data-viewmode="' +
        row["finalState"] +
        '">' +
        $.i18n._("app.caseList.comment") +
        "</a></li>" +
        '<li><a role="menuitem" class="reasonOfDelayModalBtn" data-viewmode="' +
        row["finalState"] +
        '">' +
        $.i18n._("lateProcessing.viewallRCAs") +
        "</a></li>" +
        "</ul>";

      var spanContainer = displayAlways
        ? '<span class="display-flex btn-group pull-right">'
        : '<span class="pv-grp-btn btn-group pull-right">';
      return meteData + spanContainer + icons + "</span>";
    },
  });
  function initActionItems() {
    $(document).on("click", ".createActionItem", function () {
      actionItem.actionItemModal.init_action_item_modal(
        false,
        "DRILLDOWN_RECORD",
        this,
        selectedIds
      );
    });

    $(document).on("click", ".actionItemModalIcon", function (e) {
      e.preventDefault();
      if ($(this).data("isInbound")) {
        actionItem.actionItemModal.set_in_drilldown_record_data(
          $(this).data("case-id"),
          $(this).data("version-num"),
          $(this).data("tenant-id"),
          $(this).data("sender-id")
        );
        actionItem.actionItemModal.view_action_item_list(
          hasAccessOnActionItem,
          false,
          IN_DRILLDOWN_RECORD
        );
      }
      actionItem.actionItemModal.set_drilldown_record_data(
        $(this).data("case-id"),
        $(this).data("processed-report-id"),
        $(this).data("tenant-id"),
        $(this).data("configuration-id")
      );
      actionItem.actionItemModal.view_action_item_list(
        hasAccessOnActionItem,
        false,
        DRILLDOWN_RECORD
      );
    });
  }

  l1: for (var i = 0; i < groupColumns.length; i++) {
    for (var j = 0; j < columns.length; j++) {
      if (columns[j].mData == groupColumns[i]) {
        groupColumnsIndexes.push(j);
        continue l1;
      }
    }
  }

  function renderCell(val, row, fieldName) {
    var value = nvl(val);
    if (fieldTypeMap[fieldName] == "Date") {
      if (!value.indexOf) return EMPTY_LABEL;
      if (value.indexOf("+") > 0) value = value.split("+")[0];
      value = (value === EMPTY_LABEL) ? nvl("") : '<span class="date-min-100">'+moment(value).format(DEFAULT_DATE_DISPLAY_FORMAT)+"</span>";
    }
    if (fieldName == "masterCaseNum") {
        value = "<a href='" + caseFormUrl + "?caseNumber=" + val + "&versionNumber=" + row.masterVersionNum + "' style='word-break: keep-all;'>" + val + "</a>"
    }
    if(fieldName == "pvcCapaIssueNumber"){
      return "<div class='five-row-dot-overflow'>" + value + "</div>";
    }
    return "<div class='two-row-dot-overflow'>" + value + "</div>";
  }

  var rowGroup = null;
  var columnDefs = null;
  var order = null;
  if (groupColumns.length > 0) {
    rowGroup = {
      dataSrc: groupColumns,
    };
    columnDefs = [
      {
        targets: groupColumnsIndexes,
        visible: false,
      },
    ];
    order = _.map(groupColumnsIndexes, function (num) {
      return [num, "asc"];
    });
  }

  if(hasLargeIds){
    $("#pageErrorMessage").parent().removeClass("hide");
    $("#pageErrorMessage").html($.i18n._('linkFilter.largeRecords.error'))
  }

  reportDataTable = $("#table").ExtendedDataTable({
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

    searchCallback: function () {
      if (selectAll === true || !_.isEmpty(selectedIds)) {
        resetSelection();
      }
    },

    stateSaving: {
      isEnabled: true,
      stateDataKey: 'pvcLateProcessingTableStateKey'
    },
    autoWidth: false,
    fixedHeader: {
      isEnabled: true,
      topOffset: 70
    },
    colResize: {
      isEnabled: true,
      isResizable: function (column) {
        if (column.idx === 0) {
          return false;
        }
        return true;
      }
    },

    inlineFilterConfig: {
      callback: function () {
        resetSelection();
      }
    },
    rowGroup: rowGroup,
    columnDefs: columnDefs,
    aLengthMenu: [
      [50, 100, 200, 500],
      [50, 100, 200, 500],
    ],
    pagination: true,
    iDisplayLength: 50,

        drawCallback: function (settings) {
            if (settings.json.warning) {
                $(".loadingWarning").show();
            }
            if (settings.json.internalError) {
                $("#pageErrorMessage").parent().removeClass("hide");
                $("#pageErrorMessage").html($.i18n._('oneDrive.serverError'))
            }
            showTotalPage(recordCount);
            pageDictionary($('#table_wrapper')[0], settings.aLengthMenu[0][0], recordCount);
            initActionItems();
            if (selectedIds) {
                for (var i = 0; i < selectedIds.length; i++) {
                    $(".selectCheckbox[data-cll-row-id=" + selectedIds[i].cllRowId + "]").attr("checked", true);
                }
            }
            updateSelectAllCheckboxState();
            hideLoader();
        },
        "rowCallback": function (row, data, index) {
            $(row).find('i[c]').each(function () {
                var color = $(this).attr("c")
              $(this).closest("td").attr("style", "background: "+color +" !important;");
            });
        },
        "searching": false,
        "serverSide": true,
        "processing": true,
        "ajax": {
            "url": reportrecordajaxurl,
            "type": "POST",
            "data":  function(d) {
                d.filterData =  JSON.stringify(externalFilter);
                d.reportResultId = reportResultId;
                d.globalSearch = $(".globalSearch").val();
                if(isRcaRole &&!$('#assignedToFilter').val()){
                  $('#assignedToFilter').val(MY_GROUPS_VALUE);
                }
                d.assignedToFilter = $('#assignedToFilter').val();
                if(linkFilter){
                  d.linkFilter =  JSON.stringify(linkFilter);
                }

                if (cllRecordId) d.cllRecordId = cllRecordId;
                appliedPageFilter = d;
            },
            "dataSrc": function(res) {
                tableDataAjax = [];
                recordsTotal=res.recordsTotal
                    recordCount = res.recordsFiltered;
                for (var i in res.aaData) {
                    var rowAjax = {}
                    for (var j in res.aaData[i]) {
                        rowAjax[header[j]] = res.aaData[i][j];
                    }
                    if (rowAjax.pvcIcSenderId && rowAjax.masterVersionNum && !rowAjax.vcsProcessedReportId) {
                        rowAjax.isInbound = true;
                        rowAjax.vcsProcessedReportId = -1;
                    }
                    else if (!rowAjax.pvcIcSenderId && rowAjax.vcsProcessedReportId) {
                        rowAjax.pvcIcSenderId = -1;
                        rowAjax.isInbound = false;
                    }
                    tableDataAjax.push(rowAjax);
                }
                return tableDataAjax;
            },
            "error": function(e){
                alert("Error in fetching CLL information");
                console.log(e);
            },
            "complete": function(){
                hideDataTableLoader($("#tableDiv"))
            }
        },
        columns: columns,
        initComplete: function () {
            initCreateIssue();
            bindPopOverEvents($('.commentPopoverMessage'));
            initCaseNumberBulkSearch();
            $('#assignedToFilter').on('change', function () {
              if (selectAll === true || !_.isEmpty(selectedIds)) {
                resetSelection();
              }
            });
        },
    }).on('draw.dt', function () {
      updateTitleForThreeRowDotElements();
  });

  loadTableOption('#table');

    initSharedWithFilter("table", reportDataTable, '210px');
    $($(".rxmain-dropdown-settings-table-enabled")[0]).hide();
  $("#similarCasesCheckbox").closest(".dropdown").css("display", "flow-root")
  var executeColumnSearch;
  $(document).on(
    "keyup clear click",
    ".globalSearch",
    function () {
      clearTimeout(executeColumnSearch);
      executeColumnSearch = setTimeout(function () {
        localReportDataTableFilter = []
        $(".columnSearchCll").each(function () {
          if (!_.isEmpty($(this).val())) {
            localReportDataTableFilter.push({field: $(this).attr("data-column"), value: $(this).val()})
          }
        });
        reportDataTable.draw();
      }, 1500);
    }
  );

  $(".columnSearchCll").on("click", function (e) {
    e.stopPropagation();
  });

  $("#table tbody").on("click", "td.details-control", function () {
    var tr = $(this).closest("tr");
    var row = reportDataTable.row(tr);

    if (row.child.isShown()) {
      // This row is already open - close it
      row.child.hide();
      tr.removeClass("shown");
      $(this).html("<span class='glyphicon glyphicon-triangle-right'></span>");
    } else {
      // Open this row
      row.child(spanRowFormat(row.data())).show();
      tr.addClass("shown");
      $(this).html("<span class='glyphicon glyphicon-triangle-bottom'></span>");
    }
  });

  function spanRowFormat(d) {
    // `d` is the original data object for the row
    var result =
      '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px; border-bottom: 1px solid #ccc;border-top: 1px solid #ccc; width: 100%">';
    for (var i in rowColumns) {
      result +=
        "<tr>" +
        '<td nowrap="true" style="width:0%;"><b>' +
        fieldsCodeNameMap[rowColumns[i]] +
        ":</b></td>" +
        '<td style="text-align: left;">' +
        d[rowColumns[i]] +
        "</td>" +
        "</tr>";
    }
    result += "</table>";
    return result;
  }

  function nvl(val) {
    if (typeof val == "undefined" || val === null || val === "")
      return EMPTY_LABEL;
    return val;
  }

  $(document).on("change", ".editLate", function () {
    updateEditRootCause($("#reasonOfDelayModalForm"));
  });

  function updateEditRootCause(container) {
    var lateValue = $("select.editLate").select2("val");
    if (lateValue == "") return;
    var rootCauseOption = "<option value='' > </option>";
    var rootCauseClassOption = "<option value='' > </option>";
    var late = _.find(JSON.parse(lateList), function (e) {
      return e.id == lateValue;
    });
    var rootCauseJson = JSON.parse(rootCauseList);
    var rootCauseClassJson = JSON.parse(rootCauseClassList);
    for (var j = 0; j < rootCauseJson.length; j++) {
      if (_.indexOf(late.rootCauseIds, rootCauseJson[j]["id"]) > -1) {
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
    for (var j = 0; j < rootCauseClassJson.length; j++) {
      if (_.indexOf(late.rootCauseClassIds, rootCauseClassJson[j]["id"]) > -1) {
        if (rootCauseClassJson[j]["hiddenDate"] == null) {
          rootCauseClassOption =
            rootCauseClassOption +
            "<option value='" +
            rootCauseClassJson[j]["id"] +
            "' >" +
            rootCauseClassJson[j]["textDesc"] +
            "</option>";
        }
      }
    }
    container.find("[name=late]").val(lateValue);
    container
      .find("select.editRootCause")
      .html(rootCauseOption)
      .select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")})
      .trigger("change");
    container
      .find("select.editrootCauseClass")
      .html(rootCauseClassOption)
      .select2({allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")})
      .trigger("change");
  }

    $(document).on("click", ".exportButton", function (e) {
        e.preventDefault();
        e.stopPropagation();
        var recordsNumber = recordsTotal;
        if (selectedIds && selectedIds.length > 0) recordsNumber = selectedIds.length
        var href = $(this).attr("href")
        if (((href.indexOf("PDF") > -1) && (recordsNumber > maxRowsPdf)) ||
            ((href.indexOf("XLSX") > -1) && (recordsNumber > maxRowsExcel)) ||
            ((href.indexOf("DOCX") > -1) && (recordsNumber > maxRowsPdf)) ||
            ((href.indexOf("PPTX") > -1) && (recordsNumber > maxRowsPptx))) {
            $('#exportWarning .description').hide();
            $('#exportWarning #warningType').show();
            $('#exportWarningOkButton').off('click').hide();
            $('#exportWarning').modal('show');
        } else if (recordsNumber > 1000) {
            $('#exportWarning .description').show();
            $('#exportWarning #warningType').hide();
            $('#exportWarning').modal('show');
            $('#exportWarningOkButton').off('click').click(function () {
                $('#exportWarning').modal('hide');
                doExport(href, true)
            }).show();
        } else {
            doExport(href, false)
        }
    });

    function doExport(href, backgrount) {
        $("#exportForm").attr("action", href);
        if (!_.isEmpty(appliedPageFilter)) {
            $("#direction").val(appliedPageFilter.direction);
            $("#sort").val(appliedPageFilter.sort);
            $("#globalSearch").val(appliedPageFilter.globalSearch);
            $("#searchData").val(appliedPageFilter.tableFilter);
            var cllRowIds = []
            _.map(selectedIds, function (e) {
                cllRowIds.push(e.cllRowId)
            });
            $("#rowIdFilter").val(cllRowIds);
            $("#assignedToFilterInput").val(appliedPageFilter.assignedToFilter);
        }
        if (backgrount) {
            var jForm = new FormData($("#exportForm")[0]);
            jForm.append("async", true)
            $.ajax({
                url: href,
                type: "POST",
                data: jForm,
                mimeType: "multipart/form-data",
                contentType: false,
                cache: false,
                processData: false,
                success: function (data) {
                    successNotification($.i18n._('lateProcessing.export'))
                }
            });

        } else {
            $("#exportForm").submit();
        }
    }

  $(document).on("change", ".editRootCause", function () {
    var el = $(this);
    var respPartyOption = "<option value='' > </option>";
    var rootCauseSubCategoryOption = "<option value='' > </option>";
    var respPartyJson = JSON.parse(responsiblePartyList);
    var rootCauseSubCategoryJson = JSON.parse(rootCauseSubCategoryList);
    var rootCause = _.find(JSON.parse(rootCauseList), function (e) {
      return e.id == el.val();
    });
    for (var j = 0; j < respPartyJson.length; j++) {
      if (
        rootCause &&
        _.indexOf(rootCause.responsiblePartyIds, respPartyJson[j]["id"]) > -1
      ) {
        if (respPartyJson[j]["hiddenDate"] == null) {
          respPartyOption =
            respPartyOption +
            "<option value='" +
            respPartyJson[j]["id"] +
            "' >" +
            respPartyJson[j]["textDesc"] +
            "</option>";
        }
      }
    }
    for (var j = 0; j < rootCauseSubCategoryJson.length; j++) {
      if (
        rootCause &&
        _.indexOf(
          rootCause.rootCauseSubCategoryIds,
          rootCauseSubCategoryJson[j]["id"]
        ) > -1
      ) {
        if (rootCauseSubCategoryJson[j]["hiddenDate"] == null) {
          rootCauseSubCategoryOption =
            rootCauseSubCategoryOption +
            "<option value='" +
            rootCauseSubCategoryJson[j]["id"] +
            "' >" +
            rootCauseSubCategoryJson[j]["textDesc"] +
            "</option>";
        }
      }
    }

    var editResponsibleParty = el
      .closest("tr")
      .find("select.editresponsibleParty");
    editResponsibleParty
      .html(respPartyOption)
      .select2({ allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")})
      .trigger("change");
    var editRootCauseSubCategory = el
      .closest("tr")
      .find("select.editrootCauseSubCategory");
    editRootCauseSubCategory
      .html(rootCauseSubCategoryOption)
      .select2({ allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")})
      .trigger("change");
  });

  $(".updateData").on("change", function () {
    $(this).closest("tr").find("input.isRowUpdated").val(true);
  });

  $(document).on("click", "#commentModal .close", function () {
    if (typeof commentUpdated !== "undefined" && commentUpdated)
      reloadRodTable($.i18n._("comment.success"));
  });
  $(document).on("change", "#reasonOfDelayModalForm .workflow", function () {
    if ($(this).val()) $(".justificationDiv").show();
    else $(".justificationDiv").hide();
  });

  function showEmptyLateError(label) {
    var reasonOfDelayModal = $("#reasonOfDelayModalId");
    reasonOfDelayModal.find(".alert-danger").removeClass("hide");
    reasonOfDelayModal.find(".errorMessageSpan").html(label);
    setTimeout(function () {
      reasonOfDelayModal.find(".alert-danger").addClass("hide");
    }, 2000);
  }

  $("#reasonOfDelayModalId .alert-danger .close").on("click", function () {
    $("#reasonOfDelayModalId .alert-danger").addClass("hide");
  });

  $("#reasonOfDelayModalId").on("hide.bs.modal", function () {
    reloadRodTable("null");
  });

  $(".saveReasonsOfDelay").on("click", function (e) {
    if (!$('select[name="baseLate"]').val()) {
      showEmptyLateError($.i18n._("pvc.late.null"));
    } else {
      var reasonOfDelayModal = $("#reasonOfDelayModalId");
      _.each(ROD_FIELD_ENUM, function (item) {
        reasonOfDelayModal.find("." + item).attr("disabled", false);
      });
      var data = $("#reasonOfDelayModalForm").serializeArray();
      data.push({ name: "reportResultId", value: reportResultId });
            data.push({name: "selectedIds", value: JSON.stringify(selectedIds)});

      $.ajax({
        aysnc: false,
        method: "POST",
        url: saveDelayReasonData,
        data: data,
        beforeSend: function () {
          showLoader();
        },
      })
        .done(function (data) {
          hideLoader();
          var errorMessageSpan = reasonOfDelayModal.find(".errorMessageSpan");
          if (data != undefined && data.length > 0) {
            errorMessageSpan.parent().removeClass("hide");
            errorMessageSpan.html(data);
            setTimeout(function () {
              reasonOfDelayModal.find(".alert-danger").addClass("hide");
            }, 15000);
            if (nonEditableFields.length > 0) {
              for (var m = 0; m < nonEditableFields.length; m++) {
                reasonOfDelayModal
                  .find("." + nonEditableFields[m].name)
                  .attr("disabled", "disabled");
              }
            }
            enableDisableDateFields(
              ".editPreventativeAction",
              "preventiveDate",
              preventiveDate
            );
            enableDisableDateFields(
              ".editCorrectiveAction",
              "correctiveDate",
              correctiveDate
            );
          } else if (data == undefined || data.length == 0) {
            $("#reasonOfDelayModalId").modal("hide");
            reloadRodTable($.i18n._("rod.success"));
          }
        })
        .fail(function (err) {
          hideLoader();
          var responseText = err.responseText;
          var responseTextObj = JSON.parse(responseText);
          if (responseTextObj.message != undefined) {
            $("#pageErrorMessage").parent().removeClass("hide");
            $("#pageErrorMessage").html(responseTextObj.message);
          } else {
            $("#pageErrorMessage").parent().removeClass("hide");
            $("#pageErrorMessage").html("Failed due to some unknown reason!");
          }
          if (
            responseTextObj.errorRows &&
            responseTextObj.errorRows.length > 0
          ) {
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
            }
          }
          $("#reasonOfDelayModalId").modal("hide");
        });
    }
  });

  $(".backLink").on("click", function () {
    $("#backForm").attr("action", $(this).attr("data-href"));
    $("#backFormId").val($(this).attr("data-id"));
    $("#backFormFilter").val(
      sessionStorage.getItem(
        "breadcrumbs_" + sectionId + "_" + $(this).attr("data-id")
      )
    );
    $("#backForm").attr(
      "method",
      $("#backFormFilter").val().length > 1000 ? "post" : "get"
    );
    $("#backForm").submit();
  });

  $(".table-add").on("click", function () {
    if (!$('select[name="baseLate"]').val()) {
      showEmptyLateError($.i18n._("pvc.late.null"));
    } else {
      var $row = createRow(0, "I"); //update the flag with I which means we have inserted new row
      $("#reasonOfDelayBody").append($row);
      $row.find(".select2-box").select2({ allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
      updateEditRootCause($row);
      $("#reasonOfDelayModalId")
        .find(".editPreventativeAction,.editCorrectiveAction")
        .trigger("change");
      updatePrimary();
      if (nonEditableFields.length > 0) {
        for (var m = 0; m < nonEditableFields.length; m++) {
          $("#reasonOfDelayModalId")
            .find("." + nonEditableFields[m].name)
            .attr("disabled", "disabled");
        }
      }
    }
  });

  $(document).on("click", ".table-remove", function () {
    var tr = $(this).parents("tr");
    tr.find('input[name="flagIUD"]').val("D"); //update the flag with D which means we have removed that row
    tr.addClass("hide");
    updatePrimary();
  });

  function updatePrimary() {
    if (
      $("#reasonOfDelayBody")
        .find("tr:visible")
        .find(".flagPrimaryRadio:checked").length == 0
    ) {
      $("#reasonOfDelayBody")
        .find("tr:visible:first")
        .find(".flagPrimaryRadio")
        .trigger("click");
    }
  }

  $(document).on("click", ".flagPrimaryRadio", function () {
    $(".flagPrimaryInput").val("false");
    $(this).closest("td").find(".flagPrimaryInput").val("true");
  });

  function createLateSelect() {
    var lateJson = JSON.parse(lateList);
    var lateSelect =
      '<select class="editLate updateData form-control ' +
      ROD_FIELD_ENUM.ISSUE_TYPE +
      ' select2-box "  name="baseLate"><option value="" >' +
      $.i18n._("selectOne") +
      "</option>";
    for (var i = 0; i < lateJson.length; i++) {
      if (lateJson[i]["hiddenDate"] == null) {
        lateSelect =
          lateSelect +
          '<option value="' +
          lateJson[i]["id"] +
          '" >' +
          lateJson[i]["textDesc"] +
          "</option>";
      }
    }
    lateSelect += "</select>";
    $(".lateSelectDiv").html(lateSelect);
    var el = $(".lateSelectDiv").find("select");
    el.select2({ allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody") });
    el.trigger("change");
  }

  function createRow(
    pvcLcpId,
    flagIUD,
    primaryFlag,
    lateValue,
    rootCauseValue,
    rootCauseClassValue,
    responsiblePartyValue,
    rootCauseSubCategoryValue,
    correctiveActionValue,
    preventiveActionValue,
    correctiveDateValue,
    preventiveDateValue,
    investigation,
    summary,
    actions
  ) {
    var modalBody =
      '<tr><td><span class="table-remove glyphicon glyphicon-remove"><input type="hidden" name="pvcLcpId" value="' +
      pvcLcpId +
      '" /><input type="hidden" name="flagIUD" value="' +
      flagIUD +
      '" /></span></td>';
    if (primaryFlag == "1") {
      modalBody =
        modalBody +
        '<td><input type="hidden" name="flagPrimary" class="flagPrimaryInput" value="true"><input type="radio" class="flagPrimaryRadio" name="flagPrimaryRadio" checked="checked"';
    } else {
      modalBody =
        modalBody +
        '<td><input type="hidden" name="flagPrimary" class="flagPrimaryInput" value="false"><input type="radio" value="false" class="flagPrimaryRadio" name="flagPrimaryRadio"';
    }
    modalBody = modalBody + "/></td><td>";
    var lateJson = JSON.parse(lateList);
    var rootCauseIds;
    var rootCauseClassIds;
    var lateId;
    var lateSelect =
      '<select class="editLate updateData form-control ' +
      ROD_FIELD_ENUM.ISSUE_TYPE +
      ' select2-box " name="baseLate"><option value="" >' +
      $.i18n._("selectOne") +
      "</option>";
    for (var i = 0; i < lateJson.length; i++) {
      if (lateJson[i]["textDesc"] === lateValue) {
        rootCauseIds = lateJson[i]["rootCauseIds"];
        rootCauseClassIds = lateJson[i]["rootCauseClassIds"];
        lateId = lateJson[i]["id"];
        lateSelect =
          lateSelect +
          '<option value="' +
          lateJson[i]["id"] +
          '" selected>' +
          lateJson[i]["textDesc"] +
          "</option>";
      } else if (lateJson[i]["hiddenDate"] == null) {
        lateSelect =
          lateSelect +
          '<option value="' +
          lateJson[i]["id"] +
          '" >' +
          lateJson[i]["textDesc"] +
          "</option>";
      }
    }
    lateSelect += "</select>";
    if (primaryFlag == "1") {
      $(".lateSelectDiv").html(lateSelect);
      $(".lateSelectDiv").find("select").select2({ allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
    }
    if (
      pvcLcpId != 0 &&
      primaryFlag != 1 &&
      !rootCauseValue &&
      !responsiblePartyValue &&
      !rootCauseSubCategoryValue &&
      !rootCauseClassValue &&
      !correctiveActionValue &&
      !preventiveActionValue &&
      !correctiveDateValue &&
      !preventiveDateValue &&
      !investigation &&
      !summary &&
      !actions
    )
      return;
    modalBody =
      modalBody + '<input  type="hidden" value="' + lateId + '" name="late">';

    modalBody =
      modalBody +
      '<select class="editRootCause updateData ' +
      ROD_FIELD_ENUM.ROOT_CAUSE +
      ' select2-box col-md-12" name="rootCause"><option value="" > </option>';
    var selectHtml = "";
    var rootCauseJson = JSON.parse(rootCauseList);
    var responsiblePartyIds;
    var rootCauseSubCategoryIds;
    for (var i = 0; i < rootCauseJson.length; i++) {
      if (_.indexOf(rootCauseIds, rootCauseJson[i]["id"]) > -1) {
        if (rootCauseJson[i]["textDesc"] === rootCauseValue) {
          responsiblePartyIds = rootCauseJson[i]["responsiblePartyIds"];
          rootCauseSubCategoryIds = rootCauseJson[i]["rootCauseSubCategoryIds"];
          selectHtml +=
            '<option value="' +
            rootCauseJson[i]["id"] +
            '" selected>' +
            rootCauseJson[i]["textDesc"] +
            "</option>";
        } else if (rootCauseJson[i]["hiddenDate"] == null) {
          selectHtml +=
            '<option value="' +
            rootCauseJson[i]["id"] +
            '" >' +
            rootCauseJson[i]["textDesc"] +
            "</option>";
        }
      }
    }
    if (!selectHtml) {
      responsiblePartyIds = [-1];
      rootCauseSubCategoryIds = [-1];
      selectHtml = "<option value='null' > </option>";
    }
    modalBody = modalBody + selectHtml + "</select>";
    modalBody += createDependentSelect(
      "rootCauseClass",
      rootCauseClassList,
      rootCauseClassIds,
      rootCauseClassValue,
      ROD_FIELD_ENUM.ROOT_CAUSE_CLASS
    );
    modalBody += "</td><td>";
    modalBody += createDependentSelect(
      "rootCauseSubCategory",
      rootCauseSubCategoryList,
      rootCauseSubCategoryIds,
      rootCauseSubCategoryValue,
      ROD_FIELD_ENUM.ROOT_CAUSE_SUB_CAT
    );
    modalBody += createDependentSelect(
      "responsibleParty",
      responsiblePartyList,
      responsiblePartyIds,
      responsiblePartyValue,
      ROD_FIELD_ENUM.RESP_PARTY
    );

    modalBody += "</td>";

    modalBody =
      modalBody +
      '<td><select class="editCorrectiveAction updateData ' +
      ROD_FIELD_ENUM.CORRECTIVE_ACTION +
      ' select2-box col-md-12" name="correctiveAction"><option value=\'\' ></option>';
    var correctiveActJson = JSON.parse(correctiveActionList);
    for (var i = 0; i < correctiveActJson.length; i++) {
      if (correctiveActJson[i]["textDesc"] === correctiveActionValue) {
        modalBody =
          modalBody +
          "<option value='" +
          correctiveActJson[i]["id"] +
          "' selected>" +
          correctiveActJson[i]["textDesc"] +
          "</option>";
      } else {
        modalBody =
          modalBody +
          "<option value='" +
          correctiveActJson[i]["id"] +
          "' >" +
          correctiveActJson[i]["textDesc"] +
          "</option>";
      }
    }
    modalBody = modalBody + "</select>";

    modalBody =
      modalBody +
      '<select class="editPreventativeAction updateData ' +
      ROD_FIELD_ENUM.PREVENTIVE_ACTION +
      ' select2-box col-md-12" name="preventativeAction" ><option value=\'\' ></option>';
    var preventiveActJson = JSON.parse(preventativeActionList);
    for (var i = 0; i < preventiveActJson.length; i++) {
      if (preventiveActJson[i]["textDesc"] === preventiveActionValue) {
        modalBody =
          modalBody +
          "<option value='" +
          preventiveActJson[i]["id"] +
          "' selected>" +
          preventiveActJson[i]["textDesc"] +
          "</option>";
      } else {
        modalBody =
          modalBody +
          "<option value='" +
          preventiveActJson[i]["id"] +
          "' >" +
          preventiveActJson[i]["textDesc"] +
          "</option>";
      }
    }
    modalBody = modalBody + "</select></td>";
    modalBody = modalBody + "<td class='dateRow'></td>";
    modalBody =
      modalBody +
      "<td><textarea style='resize: auto;' rows='3' class= " +
      ROD_FIELD_ENUM.INVESTIGATION +
      " ' form-control ' name='investigation' maxlength='32000' >" +
      (investigation
        ? investigation
            .replace(/"/gi, "&quot;")
            .replace(/</gi, "&lt;")
            .replace(/>/gi, "&gt;")
        : "") +
      "</textarea></td>" +
      "<td><textarea style='resize: auto;' class= " +
      ROD_FIELD_ENUM.SUMMARY +
      " 'form-control ' rows='3' name='summary' maxlength='32000' >" +
      (summary
        ? summary
            .replace(/"/gi, "&quot;")
            .replace(/</gi, "&lt;")
            .replace(/>/gi, "&gt;")
        : "") +
      "</textarea></td>" +
      "<td><textarea style='resize: auto;' class= " +
      ROD_FIELD_ENUM.ACTIONS +
      " 'form-control ' rows='3' name='actions' maxlength='32000' >" +
      (actions
        ? actions
            .replace(/"/gi, "&quot;")
            .replace(/</gi, "&lt;")
            .replace(/>/gi, "&gt;")
        : "") +
      "</textarea></td></tr>";

    var out = $(modalBody);
    var dateRow = out.find(".dateRow");
    dateRow.append(
      createDate("correctiveDate", ROD_FIELD_ENUM.CORRECTIVE_DATE)
    );
    dateRow.append(
      createDate("preventiveDate", ROD_FIELD_ENUM.PREVENTIVE_DATE)
    );
    out.find(".datepicker").datepicker({
      allowPastDates: true,
      momentConfig: {
        format: DEFAULT_DATE_DISPLAY_FORMAT,
      },
    });
    if (correctiveDateValue)
      dateRow.find("[name=correctiveDate]").val(correctiveDateValue);
    if (preventiveDateValue)
      dateRow.find("[name=preventiveDate]").val(preventiveDateValue);
    out.find("textarea").each(function () {
      initTexareaRemainingChar($(this));
    });
    return out;
  }

  $(document).on("change", ".editPreventativeAction", function () {
    enableDisableDateFields(this, "preventiveDate", preventiveDate);
  });
  $(document).on("change", ".editCorrectiveAction", function () {
    enableDisableDateFields(this, "correctiveDate", correctiveDate);
  });

  function createDependentSelect(name, list, ids, value, fieldName) {
    var modalBody =
      '<select class="edit' +
      name +
      " updateData " +
      fieldName +
      ' select2-box col-md-12" name="' +
      name +
      '"><option value="" > </option>';
    var respPartyJson = JSON.parse(list);
    selectHtml = "";
    for (var i = 0; i < respPartyJson.length; i++) {
      if (_.indexOf(ids, respPartyJson[i]['id']) > -1) {
        if (respPartyJson[i]["textDesc"] === value) {
          selectHtml +=
            '<option value="' +
            respPartyJson[i]["id"] +
            '" selected>' +
            respPartyJson[i]["textDesc"] +
            "</option>";
        } else if (respPartyJson[i]["hiddenDate"] == null) {
          selectHtml +=
            '<option value="' +
            respPartyJson[i]["id"] +
            '" >' +
            respPartyJson[i]["textDesc"] +
            "</option>";
        }
      }
    }

    return modalBody + selectHtml + "</select>";
  }

  function enableDisableDateFields(select, controlName, editable) {
    var control = $(select)
      .closest("tr")
      .find("[name=" + controlName + "]");
    var disable = $(select).select2("val") == "" ? true : editable;
    var value = disable
      ? ""
      : control.val() == ""
      ? moment().tz(userTimeZone).format(DEFAULT_DATE_DISPLAY_FORMAT)
      : control.val();
    control.attr("disabled", disable);
    control.val(value);
    control.parent().find("button").attr("disabled", disable);
  }

  function createDate(name, fieldName) {
    var div = $(
      '<div class="fuelux "><div class="datepicker pastDateNotAllowed toolbarInline" >' +
        '<div class="input-group">' +
        '<input   class="form-control fuelux date ' +
        fieldName +
        '" name="' +
        name +
        '" value=""/>' +
        "</div></div></div>"
    );
    div.find(".input-group").append($($(".dataPickerDropdownDiv")[0]).clone());
    return div;
  }

  function copyCaseValues(from, to) {
    to.find("[name=reportId]").val(from.find("[name=reportId]").val());
    to.find("[name=enterpriseId]").val(from.find("[name=enterpriseId]").val());
    to.find("[name=caseId]").val(from.find("[name=caseId]").val());
    to.find("[name=versionNumber]").val(
      from.find("[name=versionNumber]").val()
    );
    to.find("[name=caseNumber]").val(from.find("[name=caseNum]").val());
    to.find("[name=senderId]").val(from.find("[name=senderId]").val());
    to.find("[name=isInbound]").val(from.find("[name=isInbound]").val());
    to.find("[name=cllRowId]").val(from.find("[name=cllRowId]").val());
  }

  $(document).on("click", ".reasonOfDelayModalBtn", function (evt) {
    evt.preventDefault();
    var viewMode = $(this).attr("data-viewMode") == "true" || !isPvcEditor;
    var row = $(this).closest("tr");
    $("#otherCaseValues").empty();
    var mainCaseValues = $("#mainCaseValues");
    $(".selectCheckbox:checked").each(function (index, el) {
      var r = $(el).closest("tr");
      if (!r.is(row)) {
        var caseDiv = $("#mainCaseValues").clone();
        copyCaseValues(r, caseDiv);
        $("#otherCaseValues").append(caseDiv);
      }
    });
    copyCaseValues(row, mainCaseValues);

    $.ajax({
      aysnc: false,
      method: "POST",
      url: getAllRcasForCaseUrl,
      data: {
        caseId: row.find("[name=caseId]").val(),
        enterpriseId: row.find("[name=enterpriseId]").val(),
        senderId: row.find("[name=senderId]").val(),
        reportId: row.find("[name=reportId]").val(),
        versionNumber: row.find("[name=versionNumber]").val(),
        masterVersionNum: row.find("[name=versionNumber]").val(),
        isInbound: row.find("[name=isInbound]").val(),
        selectedIds:
          selectedIds && selectedIds.length > 0
            ? JSON.stringify(selectedIds)
            : "",
      },
      beforeSend: function () {
        showLoader();
      },
    })
      .done(function (response) {
        var resultObj = response.dataList;
        nonEditableFields = response.nonEditableList;
        var mandatoryFields = response.mandatoryList;
        $("#reasonOfDelayBody").empty();
        var modal = $("#reasonOfDelayBody").closest(".modal-body");
        let userSelect = modal.find("[name=assignedToUser]");
        let groupSelect = modal.find("[name=assignedToGroup]");
        if (userSelect.hasClass('select2-hidden-accessible')) {
          userSelect.select2("destroy");
          groupSelect.select2("destroy");
        }
        userSelect.attr("data-value", response.assignedToUser).val(response.assignedToUser)
        groupSelect.attr("data-value", response.assignedToGroup).val(response.assignedToGroup)
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
        $(".justificationDiv").hide();
        $(".justificationDiv input").val("");
        var select = modal.find(".workflow");
        select.empty();
        select.val("");
        if (response.workflow) {
          modal.find(".workflowCurrentId").val(response.workflow.id);
          select.append(
            "<option selected value=''>" + response.workflow.name + "</option>"
          );
          for (var i = 0; i < response.workflowList.length; i++) {
            select.append(
              "<option value='" +
                response.workflowList[i].id +
                "'>" +
                response.workflowList[i].name +
                "</option>"
            );
          }
          modal.find(".workflow").val(response.workflow.id).trigger("change");
          modal.find(".workflow").parent().show();
          modal.find(".noworkflow").hide();
        } else {
          modal.find(".workflow").parent().hide();
          modal.find(".noworkflow").show();
        }
        if (resultObj.length == 0) {
          createLateSelect();
        } else {
          var flagIUD = "U"; //update the flag with U which means we have updated row
          for (var m = 0; m < resultObj.length; m++) {
            var $row = createRow(
              resultObj[m].pvcLcpId,
              flagIUD,
              resultObj[m].primaryFlag,
              resultObj[m].lateValue,
              resultObj[m].rootCauseValue,
              resultObj[m].rootCauseClassValue,
              resultObj[m].responsiblePartyValue,
              resultObj[m].rootCauseSubCategoryValue,
              resultObj[m].correctiveActionValue,
              resultObj[m].preventiveActionValue,
              resultObj[m].correctiveDate,
              resultObj[m].preventiveDate,
              resultObj[m].investigation,
              resultObj[m].summary,
              resultObj[m].actions
            );
            if ($row) {
              $("#reasonOfDelayBody").append($row);
              $row.find(".select2-box").select2({ allowClear: true, placeholder: '', dropdownParent: $(".reasonOfDelayModalBody")});
            }
          }
        }
        if (viewMode) {
          switchToViewMode(true);
        } else {
          var lock = lockObjects(row.find("[name=cllRowId]").val());
          if (lock.locked) {
            $("#reasonOfDelayModalId.errorMessageSpan").html(
              $.i18n._("lateProcessing.locked") + " (" + lock.name + ")"
             );
            $("#reasonOfDelayModalId.alert-danger").removeClass("hide");
          } else {
            $("#reasonOfDelayModalId.errorMessageSpan").html("");
            $("#reasonOfDelayModalId.alert-danger").addClass("hide");
          }
          switchToViewMode(lock.locked);
        }
        $("#reasonOfDelayModalId")
          .find(".editPreventativeAction,.editCorrectiveAction")
          .trigger("change");
        $("#reasonOfDelayModalId").find(".workflowAssignTo").hide();
        if ($("#similarCasesCheckbox").is(":checked")) {
          $("#reasonOfDelayModalId").find(".workflowAssignTo").show();
        }
        if (nonEditableFields.length > 0) {
          for (var m = 0; m < nonEditableFields.length; m++) {
            $("#reasonOfDelayModalId")
              .find("." + nonEditableFields[m].name)
              .attr("disabled", "disabled");
            if (nonEditableFields[m].name == ROD_FIELD_ENUM.CORRECTIVE_DATE)
              correctiveDate = true;
            if (nonEditableFields[m].name == ROD_FIELD_ENUM.PREVENTIVE_DATE)
              preventiveDate = true;
          }
        }
        $("#reasonOfDelayModalId").find(".required-indicator").addClass("hide");
        if (mandatoryFields.length > 0) {
          for (var m = 0; m < mandatoryFields.length; m++) {
            $("#reasonOfDelayModalId")
              .find(".required-indicator." + mandatoryFields[m].name)
              .removeClass("hide");
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
    const selector = '.reasonOfDelayModalBody select.select2-box, .reasonOfDelayModalFormHeader select:not([name=workflowRule])';
    $(document).off('select2:open', selector);
    $(document).on('select2:open', selector, function (e) {
        const $selectContainer = $(this).next('.select2.select2-container');
        const selectOffset = $selectContainer.offset();
        const $openedContainer = $('.select2-container--open:not(.select2-container--below)');
        $openedContainer.find('.select2-dropdown').offset({left: selectOffset.left});
    });
  }

  function switchToViewMode(viewMode) {
    if (viewMode) {
      $("#reasonOfDelayModalForm")
        .find("input, button, select, textarea")
        .attr("disabled", true);
      $("#reasonOfDelayModalForm").find(".table-add, .table-remove").hide();
      $(".saveReasonsOfDelay").hide();
    } else {
      $("#reasonOfDelayModalForm")
        .find("input, button, select, textarea")
        .attr("disabled", false);
      $("#reasonOfDelayModalForm").find(".table-add, .table-remove").show();
      $(".saveReasonsOfDelay").show();
    }
  }

  var lock;

  $("#reasonOfDelayModalId").on("hide.bs.modal", function () {
    unlock();
  });

  function unlock() {
    if (lock) {
      var ids = getIdsForLock(lock);
      lock = null;
      $.ajax({
        url: unlockRcaUrl,
        data: {
          ids: ids,
        },
      });
    }
  }

  function reLockObject() {
    if (lock) {
      $.ajax({
        url: lockRcaUrl,
        data: {
          ids: getIdsForLock(lock),
        },
      }).done(function () {
        setTimeout(reLockObject, 15000);
      });
    }
  }

  function lockObjects(id) {
    lock = id;
    var jqXHR = $.ajax({
      url: lockRcaUrl,
      data: {
        ids: getIdsForLock(id),
      },
      async: false,
    });
    setTimeout(reLockObject, 5000);
    return JSON.parse(jqXHR.responseText);
  }

  function getIdsForLock(id) {
    return selectedIds && selectedIds.length > 0
      ? _.map(selectedIds, function (e) {
          return e.cllRowId;
        }).join(";")
      : id;
  }

  $(window).on("beforeunload", function (event) {
    unlock();
  });

  $(document).on("click", "#attachmentCloseButton", function () {
    $("#attachmentPageErrorMessage").parent().addClass("hide");
  });

  $(document).on("click", ".removeAttachment", function () {
    showLoader();
    $.ajax({
      url: removeAttachmentsUrl + "?attachmentId=" + $(this).attr("data-id"),
    })
      .done(function () {
        $("#uploadFileModalId").modal("hide");
        reloadRodTable($.i18n._("attachment.success"));
      })
      .fail(function (e) {
        alert("Unexpected error occurred uploading file");
        console.log(e);
      });
  });

  $(document).on("click", ".uploadFile", function () {
    var viewMode = $(this).attr("data-viewMode") == "true" || !isPvcEditor;
    var row = $(this).closest("tr");
    $("#vcsProcessedReportIdModalAtt").val(row.find("[name=reportId]").val());
    $("#masterEnterpriseIdModalAtt").val(row.find("[name=enterpriseId]").val());
    $("#masterCaseIdModalAtt").val(row.find("[name=caseId]").val());
    $("#masterCaseVersionModalAtt").val(row.find("[name=versionNumber]").val());
    $("#masterSenderIdModalAtt").val(row.find("[name=senderId]").val());
    if (!$("#attachmentPageErrorMessage").parent().hasClass("hide")) {
      $("#attachmentPageErrorMessage").parent().addClass("hide");
    }
    var isInbound = false;
    if (
      $("#masterSenderIdModalAtt").val() != "" &&
      $("#vcsProcessedReportIdModalAtt").val() == ""
    ) {
      isInbound = true;
    }
    $("#file_name").val("");
    $.ajax({
      aysnc: false,
      // method: 'POST',
      url: getAllAttachmentsUrl,
      data: {
        masterCaseId: $("#masterCaseIdModalAtt").val(),
        masterVersionNum: $("#masterCaseVersionModalAtt").val(),
        senderId: $("#masterSenderIdModalAtt").val(),
        tenantId: $("#masterEnterpriseIdModalAtt").val(),
        processedReportId: $("#vcsProcessedReportIdModalAtt").val(),
        isInbound: isInbound,
      },
      beforeSend: function () {
        showLoader();
      },
    })
      .done(function (resultObj) {
        var html = "";

        $("#attachmentsList").empty();
        for (var m = 0; m < resultObj.length; m++) {
          html += ((viewMode || (selectedIds && selectedIds.length>0))
            ? ""
            : "<a class='removeAttachment' data-id='" + resultObj[m].id + "' href='javascript:void(0)' style='color: red'><span class='glyphicon glyphicon-remove'></span> </a> ");
          html +=
            "<a href='" +
            downloadAttachmentsUrl +
            "?id=" +
            resultObj[m].id +
            "'><span class='glyphicon glyphicon-download'></span> " +
            resultObj[m].name +
            "</a> (" +
            $.i18n._("lateProcessing.added") +
            resultObj[m].createdBy +
            " " +
            resultObj[m].dateCreated +
            ")<br>";
            }
                if(!viewMode && (selectedIds && selectedIds.length>0))html+="<span class='small' style='color:#999999'>"+$.i18n._('attachment.remove.support')+"</span>"

        $("#attachmentsList").html(html);
        $("#uploadFileModalId")
          .find("[name=selectedJson]")
          .val(JSON.stringify(selectedIds));
        if (selectedIds && selectedIds.length > 1) {
          $("#attachmentsList").hide();
        } else {
          $("#attachmentsList").show();
        }
        if (viewMode) {
          $("#uploadFileModalId").find(".viewMode").hide();
        } else {
          $("#uploadFileModalId").find(".viewMode").show();
        }
        $("#uploadFileModalId").modal("show");
      })
      .always(function () {
        hideLoader();
      });
  });

  $(document).on("click", ".attachmentFormSubmit", function (e) {
    if (!validateAttachmentFileSize($("#file_input2").get(0).files[0].size)) {
      return false;
    }
      var btn = $(this);
      btn.attr("disabled", "disabled");
      if (!$("#file_name").val()) {
      $("#attachmentPageErrorMessage").parent().removeClass("hide");
      $("#attachmentPageErrorMessage").html(
        $.i18n._("attachment.cannot.empty")
      );
      return;
    }
    $("#file_name").val($("#file_input2").get(0).files[0].name);
    var jForm = new FormData();
    var fileNumber = 0;
    $.each($("#file_input2")[0].files, function (i, value) {
      jForm.append("file[" + i + "]", value);
      fileNumber++;
    });
    jForm.append("fileNumber", fileNumber);
    jForm.append("processedReportId", $("#vcsProcessedReportIdModalAtt").val());
    jForm.append("tenantId", $("#masterEnterpriseIdModalAtt").val());
    jForm.append("masterCaseId", $("#masterCaseIdModalAtt").val());
    jForm.append("masterVersionNum", $("#masterCaseVersionModalAtt").val());
    jForm.append("senderId", $("#masterSenderIdModalAtt").val());
    jForm.append("selectedJson", $("#selectedJson").val());
    $.ajax({
      url: fileUploadUrl,
      type: "POST",
      data: jForm,
      mimeType: "multipart/form-data",
      contentType: false,
      cache: false,
      processData: false,
    })
      .done(function (data) {
          btn.attr("disabled", false);
          if (data == "ok") {
              $("#uploadFileModalId").modal("hide");
              reloadRodTable($.i18n._('attachment.success'));
          } else {
              $("#attachmentPageErrorMessage").parent().removeClass('hide');
              $("#attachmentPageErrorMessage").html($.i18n._('attachmentFinalStateError'));
          }
      })
      .fail(function (e) {
        alert("Unexpected error occurred uploading file");
        console.log(e);
        btn.attr("disabled", false);
      });
  });

  $("[name=file]").on("change", function (evt, numFiles, label) {
    $("#file_name").val(
      $.map($("[name=file]")[0].files, function (val) {
        return val.name;
      }).join(";")
    );
  });

  function initCreateIssue() {
    $(document).on("change", "#attach_file_input", function () {
      $(".btn").removeAttr("disabled");
      $("#attach_file_name").val($("#attach_file_input").get(0).files[0].name);
    });
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
        var container = $(this).closest(".attachmnetcontainer");
        var jForm = new FormData();
        if (container.find("#attach_file_input").val()) {
          jForm.append(
            "file",
            container.find("#attach_file_input").get(0).files[0]
          );
          jForm.append(
            "filename_attach",
            container.find(".filename_attach").val()
          );
        }
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
        jForm.append(
          "verificationResults",
          $("#verificationResultsPopup").val()
        );
        jForm.append("comments", $("#commentsPopup").val());
        jForm.append("masterCaseId", metadataMasterCaseId);
        jForm.append("processedReportId", metadataProcessedReportId);
        jForm.append("masterVersionNum", metadataVersionNum);
        jForm.append("senderId", metadataSenderId);
        jForm.append("tenantId", metadataTenantId);
        jForm.append(
          "selectedIds",
          selectedIds && selectedIds.length > 1
            ? JSON.stringify(selectedIds)
            : ""
        );
        $.ajax({
          url: createCapaUrl,
          async: false,
          type: "POST",
          data: jForm,
          mimeType: "multipart/form-data",
          contentType: false,
          cache: false,
          processData: false,
        })
          .done(function (data) {
            if (data === "alreadyExist") {
              $("#issueNumberDlgErrorDiv").show();
              $(".issueNumberBlankError").css("display", "none");
              $(".issueNumberUniqueError").css("display", "block");
            } else {
              $("#createRodIssue").modal("hide");
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
    })

    $(document).on("click", ".createIssue", function (e) {
      $("#attachmentList").html("");
      $("#icons").html("");
      $("#attach_file_name").val(null);
      $(".filename_attach").val(null);
      var viewMode = !isPvcEditor;
      metadataMasterCaseId = $(this).data("case-id");
      metadataProcessedReportId = $(this).data("processed-report-id");
      metadataTenantId = $(this).data("tenant-id");
      var issueCaseNum = $(this).data("case-num");
      var issueReportingDest = $(this).data("report-des");
      var issueDueDate =
        $(this).data("due-date") !== undefined &&
        $(this).data("due-date").length > 0
          ? moment($(this).data("due-date")).format(DEFAULT_DATE_DISPLAY_FORMAT)
          : EMPTY_LABEL;
      var issueSubmissionDate =
        $(this).data("submit-date") !== undefined &&
        $(this).data("submit-date").length > 0
          ? moment($(this).data("submit-date")).format(
              DEFAULT_DATE_DISPLAY_FORMAT
            )
          : EMPTY_LABEL;
      var issueRootCause =
        $(this).data("root-cause") !== undefined
          ? $(this).data("root-cause")
          : EMPTY_LABEL;
      var issueResponsibleParty =
        $(this).data("responsible-party") !== undefined
          ? $(this).data("responsible-party")
          : EMPTY_LABEL;
      metadataMasterCaseId = $(this).data("case-id");
      metadataProcessedReportId = $(this).data("processed-report-id");
      metadataTenantId = $(this).data("tenant-id");
      metadataVersionNum = $(this).data("version-num");
      metadataSenderId = $(this).data("sender-id");
      $("#updateCapaIssue").hide();
      $("#updateAttach").hide();
      $("#createCapaIssue").show();
      $("#createAttach").show();
      $("#issueNumber").attr("readonly", false);
      issueTable.clear();
      var createRodIssueModal = $("#createRodIssue");
      createRodIssueModal.find("textField, textArea").val("");
      createRodIssueModal.find("select").val("").trigger("change");
      createRodIssueModal
        .find("#issueNumber , #issueTypePopup , #categoryPopup , #remarksPopup")
        .val("");
      createRodIssueModal.find(".createCapaIssue").attr("disabled", false);
      createRodIssueModal.find("#createAttach").attr("disabled", true);
      createRodIssueModal.find("#descSizeExceed").hide();
      createRodIssueModal.find(".attachSizeExceed").hide();
      createRodIssueModal.find(".issueErrorDiv").hide();
      $.ajax({
        data: {
          masterCaseId: metadataMasterCaseId,
          tenantId: metadataTenantId,
          processedReportId: metadataProcessedReportId,
          masterVersionNum: metadataVersionNum,
          senderId: metadataSenderId,
        },
        url: fetchIssueNumberUrl,
      })
        .done(function (data) {

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
      })
        .done(function (data) {
          createRodIssueModal.find("#issueNumber").val(data);
          if (viewMode) {
            createRodIssueModal
              .find("textField , select, textArea")
              .attr("disabled", true);
            $(".createCapaIssue").hide();
            createRodIssueModal
              .find(
                "#issueNumber , #issueTypePopup , #categoryPopup , #remarksPopup"
              )
              .attr("readonly", "readonly");
          }
        })
        .fail(function (data) {
          alert($.i18n._("Error") + " : " + data.responseText);
        });
      if (selectedIds.length > 0) {
        $.ajax({
          data: {
            selectedIds: JSON.stringify(selectedIds),
          },
          type: "POST",
          url: getCapaDescUrl,
        })
          .done(function (data) {
            if (data && data.length > 32000) {
              createRodIssueModal
                .find(".createCapaIssue")
                .attr("disabled", true);
              createRodIssueModal.find("#descSizeExceed").show();
            }
            createRodIssueModal.find("#descriptionPopup").val(data);
            createRodIssueModal.modal("show");
          })
          .fail(function (data) {
            alert($.i18n._("Error") + " : " + data.responseText);
          });
      } else {
        var descriptionData =
          "" +
          $.i18n._("case.number.label") +
          ": " +
          issueCaseNum +
          "  \n" +
          $.i18n._("reporting.destination.label") +
          ": " +
          issueReportingDest +
          "" +
          "\n" +
          $.i18n._("due.date.label") +
          ": " +
          issueDueDate +
          " \n" +
          $.i18n._("submission.date.label") +
          ": " +
          issueSubmissionDate +
          "" +
          "\n" +
          $.i18n._("root.cause.label") +
          ": " +
          issueRootCause +
          "\n" +
          $.i18n._("responsible.party.label") +
          ": " +
          issueResponsibleParty +
          "";
        createRodIssueModal.find("#descriptionPopup").val(descriptionData);
        createRodIssueModal.modal("show");
      }
    });

    populateCapaUsersList();
  }

  function populateCapaUsersList(option) {
    $.ajax({
      url: fetchUsersUrl,
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

    function addRemoveSelectedIds(value, removeIfExist) {
        var cllRowId = value.cllRowId;
        var idx = _.findIndex(selectedIds, function (it) {
            return (it.cllRowId == cllRowId);
        })
        if ((!removeIfExist) && (idx < 0)) {
            if (selectedIds.length == BULK_UPDATE_MAX_ROWS) {
                $("#bulkUpdateMaxRowsWarning").modal("show");
                return false;
            } else {
                selectedIds.push(value);
            }
        }
        if (removeIfExist && (idx > -1)) selectedIds.splice(idx, 1);
        return true;
    }

    reportDataTable.on('click', "input.selectCheckbox", function () {
        var isChecked = $(this).prop("checked");
        var value = getSelectValue(this)
        if (isChecked) addRemoveSelectedIds(value, false)
        if (!isChecked) addRemoveSelectedIds(value, true)
        if (isChecked && $('#similarCasesCheckbox').prop("checked")) {
            var caseNum = $(this).data('case-num');
            var versionNum = $(this).data('version-num');
            addSimilarCasesInSelectedIds(caseNum, versionNum, function (similarIds) {
                fetchSelectedFromSimilarCases(similarIds)
            });
        }
        $(this).trigger('tableSelectedRowsCountChanged', [(selectedIds ? selectedIds.length : 0)]);
    });

  function updateSelectAllCheckboxState() {
    if (selectAll) {
      const allPageItemsSelected = $('input.selectCheckbox:not(:checked)').length === 0;
      $('input.selectAllCheckbox').prop('checked', allPageItemsSelected);
    }
  }

  function initCaseNumberBulkSearch() {
    let caseNumFilterInput = $("input.inline-filter-input[data-name=masterCaseNum]");
    caseNumFilterInput.attr("style", "width:calc(100% - 36px) !important;");
    caseNumFilterInput.after("<i class='fa fa-pencil-square-o copyAndPasteCaseNumIcon' data-toggle=\"modal\" data-target=\"#copyAndPasteModal\" style=' cursor: pointer;padding: 2px;' ></i>")
  }

  $(document).on("click", ".copyAndPasteCaseNumIcon", function (e) {
    $("#copyAndPasteModal .copyPasteContent").val($(this).parent().find("input").val());
  });

  $(document).on("copyAndPaste:paste", function (e, v) {
    $(".copyAndPasteCaseNumIcon").parent().find("input").val(v).trigger("input");
  });

    function updateSelectedCheckboxes() {
        _.each($("input.selectCheckbox"), function (el) {
            var elementRowId = $(el).attr("data-cll-row-id");
            if (_.findIndex(selectedIds, function (it) {
                return (it.cllRowId == elementRowId);
            }) > -1)
                $(el).prop("checked", true).trigger("change");
            else
                $(el).prop("checked", false).trigger("change");
        });
    }

    function getSelectValue(val) {
        var jsonObj = {};
        var isChecked = $(val).prop("checked");
        jsonObj.caseId = $(val).attr("data-case-id");
        jsonObj.tenantId = $(val).attr("data-tenant-id");
        jsonObj.processedReportId = $(val).attr("data-processed-report-id");
        jsonObj.cllRowId = $(val).attr("data-cll-row-id");
        jsonObj.caseNum = $(val).attr("data-case-num");
        jsonObj.versionNum = $(val).attr("data-version-num");
        jsonObj.senderId = $(val).attr("data-sender-id");
        jsonObj.issueReportingDest = $(val).attr("data-report-des");
        jsonObj.issueDueDate = $(val).attr("data-due-date") !== undefined ? $(val).attr("data-due-date") : "";
        jsonObj.issueSubmissionDate = $(val).attr("data-submit-date") !== undefined ? $(val).attr("data-submit-date") :"";
        jsonObj.issueRootCause = $(val).attr("data-root-cause");
        jsonObj.issueResponsibleParty = $(val).attr("data-responsible-party");
    return jsonObj;
  }

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

  $("#table").on("draw.dt", function (e) {
      updateSelectedCheckboxes();
  });
    $(document).on("change", 'input.selectAllCheckbox', function (e) {
        selectedIds = [];
        if ($(this).is(":checked")) {
            selectAll = true;
            if ($(".dt-paging-button[data-dt-idx=0]").length > 0) {
                addSimilarCasesInSelectedIds("", "", function (similarIds) {
                    fetchSelectedFromSimilarCases(similarIds)
                });
            } else {
                $(".selectCheckbox").each(function (index) {
                    var value = getSelectValue($(this))
                    addRemoveSelectedIds(value, false)
                });
            }
        } else {
            selectAll = false;
        }
        updateSelectedCheckboxes();
        $(this).trigger('tableSelectedRowsCountChanged', [(selectedIds ? selectedIds.length : 0)]);
    })

    function resetSelection() {
        $('#table thead th .selectAllCheckbox').prop('checked', false).trigger('change');
        selectedIds = [];
        $('#table').trigger('tableSelectedRowsCountChanged', [0]);
    }

    function fetchSelectedFromSimilarCases(similarIds) {
        if (similarIds.length > 0) {
            for (var i = 0; i < similarIds.length; i++) {
                if (!addRemoveSelectedIds(similarIds[i], false)) break;
            }
        }
        updateSelectedCheckboxes();
    }



  $(document).on("click", ".closeAssignToModal", function () {
    reloadRodTable($.i18n._("assigned.success"));
    selectedIds = [];
  });

  $(document).on("data-clk", function (event, elem) {
    const elemClkData = JSON.parse(elem.attributes["data-evt-clk"].value)
    const methodName = elemClkData.method;
    const params = elemClkData.params;

    if(methodName == "fetchIssueData") {
      var issueNumber = elem.attributes["data-issueNumber"].value;
      fetchIssueData(issueNumber);
    }
  });
});

function addSimilarCasesInSelectedIds(caseNum, versionNum, callback) {
  var similarIds = [];
  showLoader();
  $.ajax({
    url: getSimilarCasesUrl,
    type: "POST",
    data: {
      filterData: JSON.stringify(externalFilter),
      reportResultId: reportResultId,
      caseNum: caseNum,
      versionNum: versionNum,
      globalSearch: appliedPageFilter.globalSearch || null,
      tableFilter: appliedPageFilter.tableFilter || null,
      assignedToFilter: appliedPageFilter.assignedToFilter || null,
      direction: appliedPageFilter.direction || null,
      sort: appliedPageFilter.sort || null,
      start: appliedPageFilter.start || 0,
      linkFilter: JSON.stringify(linkFilter)|| null
    },
  })
    .done(function (aaData) {
      var selectedCllIds = [];
      _.each(selectedIds, function (element) {
        selectedCllIds.push(element.cllRowId);
      });

      for (var i in aaData) {
        if (!selectedCllIds.includes(aaData[i][header.indexOf("cllRowId")])) {
          var jsonObj = {};
          jsonObj.caseId = aaData[i][header.indexOf("masterCaseId")];
          jsonObj.tenantId = aaData[i][header.indexOf("masterEnterpriseId")];
          jsonObj.processedReportId =
            aaData[i][header.indexOf("vcsProcessedReportId")];
          jsonObj.cllRowId = aaData[i][header.indexOf("cllRowId")];
          jsonObj.caseNum = aaData[i][header.indexOf("masterCaseNum")];
          jsonObj.versionNum = aaData[i][header.indexOf("masterVersionNum")];
          jsonObj.senderId = aaData[i][header.indexOf("pvcIcSenderId")];
          jsonObj.issueReportingDest =
            aaData[i][header.indexOf("reportsAgencyId")];
          jsonObj.issueDueDate =
            aaData[i][header.indexOf("reportsDueDate")] !== undefined
              ? aaData[i][header.indexOf("reportsDueDate")]
              : "";
          jsonObj.issueSubmissionDate =
            aaData[i][header.indexOf("reportsDateSubmitted")] !== undefined
              ? aaData[i][header.indexOf("reportsDateSubmitted")]
              : "";
          jsonObj.issueRootCause = aaData[i][header.indexOf("pvcLcpRootCause")]
            ? aaData[i][header.indexOf("pvcLcpRootCause")]
            : "";
          jsonObj.issueResponsibleParty = aaData[i][
            header.indexOf("pvcLcpRespParty")
          ]
            ? aaData[i][header.indexOf("pvcLcpRespParty")]
            : "";
          similarIds.push(jsonObj);
        }
      }
    })
    .always(function () {
      hideLoader();
      callback(similarIds);
      $("#table").trigger('tableSelectedRowsCountChanged', [(selectedIds ? selectedIds.length : 0)]);
    })
    .fail(function (data) {
      hideLoader();
      console.log("Error", data);
    });
}

function updateAssignedToValue(event, newVal, newLabel) {
  if (selectedIds && selectedIds.length > 1) {
    showLoader();
  }
  var $this = $(event);
  if ($("#similarCasesCheckbox").prop("checked") && selectedIds.length === 0) {
    var checkbox = $(event).closest("tr").find("input.selectCheckbox");
    var caseNum = $(checkbox).data("case-num");
    var versionNum = $(checkbox).data("version-num");
    addSimilarCasesInSelectedIds(caseNum, versionNum, function (similarIds) {
      if (similarIds.length > 1) {
        selectedIds = similarIds;
        if (similarIds.length === 2)
          $("#assignSimilarCases")
            .find(".bodyContent")
            .html($.i18n._("app.ROD.one.similar.case"));
        else
          $("#assignSimilarCases")
            .find(".bodyContent")
            .html(
              $.i18n._("there.are") +
                " " +
                (similarIds.length - 1) +
                " " +
                $.i18n._("ROD.multiple.similar.submissions")
            );
        $("#assignSimilarCases").modal("show");
        $("#submitAssignTo").off().on("click", function () {
          var assignToModal = $("#assignSimilarCases");
          var element = assignToModal.find(".editAssignedTo");
          if ($("#currentSubmission").is(":checked") && !$("#allSubmissions").is(":checked")) {
            selectedIds = [];
          }
          updateAssignedOwner(event,newVal, newLabel);
        });
      } else updateAssignedOwner(event,newVal, newLabel);
    });
  } else updateAssignedOwner(event,newVal, newLabel);
}

function updateAssignedOwner(event, newVal, newLabel) {
  var $this = $(event);
  let isUser = $this.hasClass("editAssignedToUser");
  let assignedToUser = isUser ? (newVal ?? "") : ""
  let assignedToGroup = !isUser ? (newVal ?? "") : ""
  let field = isUser ? "User_" : "UserGroup_"
  $.ajax({
    url: updateAssignedOwnerUrl,
    method: "POST",
    data: {
      masterCaseId: $this.attr("data-case-id"),
      processedReportId: $this.attr("data-processed-report-id"),
      field: field,
      assignedToUser: assignedToUser,
      assignedToGroup: assignedToGroup,
      tenantId: $this.attr("data-tenant-id"),
      senderId: $this.attr("data-sender-id"),
      masterVersionNum: $this.attr("data-version-num"),
      masterCaseNum: $this.attr("data-case-num"),
      reportResultId: reasonOfDelayId,
      selectedJson: JSON.stringify(selectedIds),
    },
  })
    .done(function (data) {
      $this.attr('data-value', newVal);
      $this.html(newLabel);
      $("#pageErrorMessage").parent().addClass("hide");
      if ((selectedIds.length > 1)) {
            reloadRodTable($.i18n._('assigned.success'));
        } else {
            $("#pageSuccessMessage").html($.i18n._('assigned.success'))
            $("#pageSuccessMessage").parent().show();
            setTimeout(function () {
                $("#pageSuccessMessage").parent().hide();
            }, 10000)
        }
    })
    .fail(function (data) {
      hideLoader();
      $("#pageErrorMessage").html(data.responseText);
      $("#pageErrorMessage").parent().removeClass("hide");
      setTimeout(function () {
        $("#pageErrorMessage").parent().addClass("hide");
      }, 10000)
    });
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
  if (!isPvcEditor) {
    $("#updateCapaIssue").hide();
    $("#updateAttach").show();
  }
  $.ajax({
    url: fetchDataIssueUrl,
    data: {
      issueNumber: issueNumber,
    },
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
        .select2({})
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
  })
    .done(function (data) {
      var attachmentList = data.capaData.attachments;
      var iconhtml = "";
      var attachmentListHtml = "";
      var checkRow = 0;
      for (var i = 0; i < attachmentList.length; i++) {
        if (attachmentList[i].isDeleted == false) {
          checkRow = 1;
          iconhtml = '<div class="m-t-25">' +
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
          if (!isPvcEditor) {
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
            '<i id="saveButton" class="glyphicon glyphicon-download-alt theme-color v-a-initial"></i>' +
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
      if(checkRow == 0){
          $(".attachments").addClass("hidden");
          $(".removes").addClass("hidden");
      }
    })
    .fail(function (data) {
      alert($.i18n._("Error") + " : " + data.responseText);
    });
  $(".filename_attach").val(null);
  $("#attach_file_name").val(null);
  $("#attach_file_input").val(null);
}

$(document).on("change", "#attach_file_input", function () {
  $(".btn").removeAttr("disabled");
  $("#attach_file_name").val($("#attach_file_input").get(0).files[0].name);
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
    $("#selectAll").val(selectAll);
    $("#selectedIds").val(selectedAttachIds);
    $("#capaInstanceId").val(capaInsId);
    var formId = $("#attachForm");
    formId.attr("action", downloadAllAttachmentUrl);
    formId.submit();
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

function removeAttachments(ids) {
  var deleteJustification = $("#deleteJustification").val();
  var deleteValidationAlert = $("#deleteDlgErrorDiv");
  if (!deleteJustification.trim()) {
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
    }).done(function () {
      $("#deleteModal").modal("hide");
      fetchAttachment(issueNumber);
    });
  }
}

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

$(document).on("click", "#createAttach", function (e) {
  if (
    !validateAttachmentFileSize($("#attach_file_input").get(0).files[0].size)
  ) {
    return false;
  }
  $("#issueNumber").val().trim();
  var container = $(this).closest(".attachmnetcontainer");
  if (!validateAttachmentFileNameSize(container.find(".filename_attach"))) {
    return false;
  }
  var jForm = new FormData();
  if (container.find("#attach_file_input").val()) {
    jForm.append("file", container.find("#attach_file_input").get(0).files[0]);
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
    processData: false,
  })
    .done(function (data) {
      if (data == "success") {
        fetchAttachment($("#issueNumber").val());
      }
    })
    .fail(function (data) {
      alert($.i18n._("Error") + " : " + data.responseText);
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

$(document).on("click", "#attachSizeExceed", function (e) {
  $(".attachSizeExceed").hide();
});

$(document).on("click", "#issueErrorDiv", function (e) {
  $(".issueErrorDiv").hide();
});

$(document).on("click", "#updateAttach", function (e) {
  if (
    !validateAttachmentFileSize($("#attach_file_input").get(0).files[0].size)
  ) {
    return false;
  }
  $("#issueNumber").val().trim();
  var container = $(this).closest(".attachmnetcontainer");
  var jForm = new FormData();
  if (container.find("#attach_file_input").val()) {
    jForm.append("file", container.find("#attach_file_input").get(0).files[0]);
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
  })
    .done(function (data) {
      if (data == "success") {
        fetchAttachment($("#issueNumber").val());
      }
    })
    .fail(function (data) {
      alert($.i18n._("Error") + " : " + data.responseText);
    });
});

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
  jForm.append("masterCaseId", metadataMasterCaseId);
  jForm.append("processedReportId", metadataProcessedReportId);
  jForm.append("tenantId", metadataTenantId);
  jForm.append("masterVersionNum", metadataVersionNum);
  jForm.append("senderId", metadataSenderId);
  $.ajax({
    url: updateCapaUrl,
    async: false,
    type: "POST",
    data: jForm,
    mimeType: "multipart/form-data",
    contentType: false,
    cache: false,
    processData: false,
  })
    .done(function (data) {
      $("#createRodIssue").modal("hide");
      reloadRodTable($.i18n._("capa.update.success"));
    })
    .fail(function (data) {
      alert($.i18n._("Error") + " : " + data.responseText);
    });
});
var timer = true

function reloadRodTable(txt) {
  showLoader();
  if (!txt || txt != "null") {
    $("#pageSuccessMessage").html(txt ? txt : "Success");
    $("#pageSuccessMessage").parent().show();
    timer =setTimeout(function () {
      $("#pageSuccessMessage").parent().hide();
    }, 10000);
  }
  reportDataTable.ajax.reload(null, false);
}


$(document).on("click", ".rodSuccessAlert .close", function (e) {
    $(this).parent().hide();
    if (timer) {
        clearTimeout(timer);
        timer = 0;
    }
});
$(document).on("click", ".assignedWithCell", function (e) {
  var $this = $(this);
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
    let userValue = $(this).closest("td").find(".editAssignedToUser").attr("data-value")
    $('#assignedWithEdit').attr("data-extraParam", JSON.stringify({user: userValue}));
    bindShareWith($('#assignedWithEdit'), sharedWithGroupListUrl, sharedWithValuesUrl, "100%", true);
  }
  var $assignedWithEditDiv = $("#assignedWithEditDiv");
        showEditDiv($this, $assignedWithEditDiv, $assignedWithEditDiv.find('#assignedWithEdit'), 100, true);
        $assignedWithEditDiv.find('#assignedWithEdit').val(oldVal).change();
        $('#assignedWithEdit').val(oldVal);
        $assignedWithEditDiv.find(".saveButton").off().on('click', function (e) {
            var $assignedWithEdit = $assignedWithEditDiv.find('#assignedWithEdit');
          var newVal = $assignedWithEdit.val();
          var newLabel = $assignedWithEdit.select2('data') && $assignedWithEdit.select2('data')[0] ? $assignedWithEdit.select2('data')[0].text : "&nbsp;&nbsp;&nbsp;&nbsp;...&nbsp;&nbsp;&nbsp;";
          if ((newVal !== oldVal)||(selectedIds.length > 1)) {
                updateAssignedToValue($this,newVal, newLabel )
            }
            $assignedWithEditDiv.hide()
        });
});

function formAssignedWithCell(row, assignedName, assignedId, selector) {

  var userAndGroupValue = [{"id": assignedId, "username": assignedName}];
  if (row['finalState'] == "true")
    return assignedName ?? "";
  else
    return "<span class='assignedWithCol assignedWithCell editAssignedTo " + selector + "' style='cursor:pointer;display: block; width: 100%' data-id='" + row.id + "' data-value='" + assignedId + "' data-usergroups='" + JSON.stringify(userAndGroupValue) + "' data-tenant-id='" + row['masterEnterpriseId'] + "' data-case-id='" + row['masterCaseId'] + "' data-processed-report-id='" + row['vcsProcessedReportId'] + "' data-sender-id='" + row['pvcIcSenderId'] + "' data-version-num='" + row['masterVersionNum'] + "' data-is-inbound='" + row['isInbound'] + "' data-case-num='" + row['masterCaseNum'] + "'>" + (assignedName ? assignedName : "&nbsp;&nbsp;&nbsp;&nbsp;...&nbsp;&nbsp;&nbsp;") + "</span>";
}

function formAssignedToUserCell(row) {
  return formAssignedWithCell(row, row.assignedToUser, row.assignedToUserId, "editAssignedToUser")
}

function formAssignedToUserGroupCell(row) {
  return formAssignedWithCell(row, row.assignedToGroup, row.assignedToGroupId, "editAssignedToUserGroup")
}
