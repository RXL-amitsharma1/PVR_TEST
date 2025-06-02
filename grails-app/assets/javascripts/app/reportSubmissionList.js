var reportSubmission = reportSubmission || {};

reportSubmission.reportSubmissionsList = (function () {
  //Action item table.
  var report_submission_table;

  var initializeFilters = function (table) {
    $("#submissionStatus").on("change", function () {
      table.draw();
    });
    $(document).on("keyup", "#caseSeriesSearchDiv", function (e) {
      if (e.keyCode === 13) {
        $(this).blur();
      }
    });
    $(document).on("blur", "#caseSeriesSearchDiv", function () {
      table.draw();
    });
  };

  //The function for initializing the action item data tables.
  var init_report_submission_table = function (url) {
    var tableFilter = {};
    var advancedFilter = false;
    //Initialize the datatable
    var columnsList = [
      {
        mData: "reportType",
        sClass: "dataTableColumnCenter",
      },
      {
        mData: "productSelection",
        mRender: function (data, type, row) {
          var text = data == null ? "" : encodeToHTML(data);
          return '<div class="three-row-dot-overflow">' + text + "</div>";
        },
      },
      {
        mData: "reportName",
        mRender: function (data, type, row) {
          var ico = "";
          if (
            row.isPublisherReport &&
            ($("#allReportsFilter").is(":checked") || !isPublisherReport)
          )
            ico =
              '<sup type="' +
              $.i18n._("publisherReport") +
              '" style="font-weight: bold;">PVP</sup>';
          var content =
            '<a href="' +
            reportSubmissionConfig.viewExecutedReportUrl +
            "?id=" +
            row.exConfigId +
            '">' +
            encodeToHTML(row.reportName) +
            "</a>" +
            ico;
          return "<div class='three-row-dot-overflow'>" + content + "</div>";
        },
      },
      {
        mData: "pvrDateRangeStart",
        bSortable: false,
        mRender: function (data, type, row) {
          if (row.pvrDateRangeStart && row.pvrDateRangeEnd) {
            return $.i18n._(
              "app.reportSubmission.prDateRange",
              moment
                .utc(row.pvrDateRangeStart)
                .format(DEFAULT_DATE_DISPLAY_FORMAT),
              moment
                .utc(row.pvrDateRangeEnd)
                .format(DEFAULT_DATE_DISPLAY_FORMAT)
            );
          }
          return "";
        },
      },
      {
        mData: "reportingDestination",
        mRender: function (data, type, full) {
          return "<div class='three-row-dot-overflow'>" + data + "</div>";
        },
      },
      {
        mData: "isPrimaryDestination",
        sClass: "dataTableColumnCenter",
        mRender: function (data, type, row) {
          if (data == true) {
            return $.i18n._("yes.abbreviated");
          }
          return $.i18n._("no.abbreviated");
        },
      },
      {
        mData: "reportSubmissionStatus",
        mRender: function (data, type, row) {
          return $.i18n._("app.reportSubmissionStatus." + data);
        },
      },
      {
        mData: "submissionDate",
        sClass: "dataTableColumnCenter nowrap",
        mRender: function (data, type, row) {
          return data
            ? moment
                .utc(data)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT)
            : "";
        },
      },
      {
        mData: "dueDate",
        sClass: "dataTableColumnCenter",
        mRender: function (data, type, row) {
          return data ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
        },
      },
      {
        mData: "license",
        visible: false,
        bSortable: false,
        mRender: function (data, type, row) {
          var text = data == null ? "" : encodeToHTML(data);
          return '<div class="three-row-dot-overflow">' + text + "</div>";
        },
      },
    ];
    if (isRodProcessingEnabled)
      columnsList.push(
        {
          mData: "late",
          sClass: "dataTableColumnCenter",
          mRender: function (data, type, row) {
            if (row.late && !row.late.isInTime) {
              return (
                $.i18n._("no.abbreviated") +
                ' <span class="fa fa-info-circle" style="cursor:pointer" data-evt-clk=\'{"method": "showLate", "params": [\"'+row.late.late +'\",\"' +JSON.stringify(row.late.lateReasons).replace(/\"/g, "&#34;") +'\",\"' +row.capa +'\",\"' +row.modifiedBy +'\",\"' +row.lastUpdated+ '\",\"' +row.exConfigId+ '\",\"' +row.id+ '\"]}\' ></span>'
              );
            }
            return $.i18n._("yes.abbreviated");
          },
        },
        {
          mData: "attacments",
          bSortable: false,
          visible: true,
          mRender: function (data, type, row) {
            if (data && data.length > 0) {
              var out = "";
              for (var i = 0; i < data.length; i++) {
                out +=
                  '<a href="' +
                  reportSubmissionConfig.attachment_download_url +
                  "?id=" +
                  data[i].id +
                  '">' +
                  data[i].name +
                  "</a><br>";
              }
              return out;
            }
            return "";
          },
        }
      );
    columnsList.push({
      mData: null,
      bSortable: false,
      sClass: "dataTableColumnCenter",
      mRender: function (data, type, row) {
        var rpt =
          '<a href="' +
          reportSubmissionConfig.viewReportResultUrl +
          "?id=" +
          row.exConfigId +
          "&isInDraftMode=" +
          row.isInDraftMode +
          '">' +
          $.i18n._("qualityModule.viewReport.label") +
          "</a>";
        if (row.reportSubmissionStatus == "SUBMITTED") {
          return (
            '<a href="' +
            reportSubmissionConfig.viewSubmittedCaseSeries +
            "?id=" +
            row.id +
            "&isInDraftMode=" +
            row.isInDraftMode +
            '">' +
            $.i18n._("caseSeries.viewCases") +
            "</a><br>" +
            rpt
          );
        }
        return rpt;
      },
    });
    report_submission_table = $("#reportSubmissionsList")
      .DataTable({
        //"sPaginationType": "bootstrap",
        layout: {
          topStart: null,
          topEnd: { search: { placeholder: $.i18n._("app.generatedAdhocReports.search.label") } },
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
          $("#allReportsFilter").on("change", function (e) {
            report_submission_table.draw();
          });

          var topControls = $(".topControls");
          $('#reportSubmissionsList_wrapper').find('.dt-search').before(topControls);
          topControls.show();
          $("#submissionStatusEnum").select2({
              placeholder: $.i18n._("app.advancedFilter.status"),
              allowClear: true,
            })
            .on("select2:open", function (e) {
              var searchField = $('.select2-dropdown .select2-search__field');
              if (searchField.length) {
                searchField[0].focus();
              }
            })
            .on("change", function () {
              report_submission_table.draw();
            });
          $(".outside").hide();
          initializeFilters(report_submission_table);
        },

        customProcessing: true, //handled using processing.dt event
        serverSide: true,
        ajax: {
          url: url,
          type: "POST",
          dataSrc: "data",
          data: function (d) {
            d.searchString = d.search.value;
            d.tableFilter = JSON.stringify(tableFilter);
            d.advancedFilter = advancedFilter;
            if (d.order.length > 0) {
              d.direction = d.order[0].dir;
              //Column header mData value extracting
              d.sort = d.columns[d.order[0].column].data;
            }

            if (
              $("#submissionStatus").length > 0 &&
              $("#submissionStatus").val() != ""
            ) {
              d.status = $("#submissionStatus").val();
            }
            if ($("#caseSeriesSearchDiv").val() != "") {
              d.caseSeriesSearch = $("#caseSeriesSearchDiv").val();
            }
            d.pvp = isPublisherReport;
            d.allReportsForPublisher = $("#allReportsFilter").is(":checked");
          },
        },

        aaSorting: [],
        order: [[7, "desc"]],
        aoColumnDefs: [
          {
            bSortable: false,
            aTargets: isRodProcessingEnabled ? [1, 9] : [1],
          },
        ],
        bLengthChange: true,
        aLengthMenu: [
          [10, 25, 50, 100],
          [10, 25, 50, 100],
        ],
        pagination: true,
        iDisplayLength: 10,

        drawCallback: function (settings) {
          pageDictionary(
            $("#reportSubmissionsList_wrapper")[0],
            settings.aLengthMenu[0][0],
            settings.json.recordsFiltered
          );
          getReloader(
            $("#reportSubmissionsList_info"),
            $("#reportSubmissionsList")
          );
        },
        aoColumns: columnsList,
      })
      .on("draw.dt", function () {
        setTimeout(function () {
          $("#reportSubmissionsList tbody tr").each(function () {
            $(this).find("td:eq(7)").attr("nowrap", "nowrap");
          });
        }, 100);
        updateTitleForThreeRowDotElements();
        initEvtClk();
      })
      .on("xhr.dt", function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
      });

    var init_filter = function () {
      var filter_data = [
        {
          label: $.i18n._("app.advancedFilter.reportName"),
          type: "text",
          name: "report.reportName",
        },
        {
          label: $.i18n._("app.advancedFilter.destination"),
          type: "text",
          name: "reportingDestination",
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
          label: $.i18n._("app.advancedFilter.submissionDateStart"),
          type: "date-range",
          group: "submissionDate",
          group_order: 1,
        },
        {
          label: $.i18n._("app.advancedFilter.submissionDateEnd"),
          type: "date-range",
          group: "submissionDate",
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
      ];

      pvr.filter_util.construct_right_filter_panel({
        table_id: "#reportSubmissionsList",
        container_id: "config-filter-panel",
        filter_defs: filter_data,
        column_count: 1,
        done_func: function (filter) {
          tableFilter = filter;
          advancedFilter = true;
          var dataTable = $("#reportSubmissionsList").DataTable();
          dataTable.ajax.reload(function (data) {}, false).draw();
        },
      });
    };

    loadTableOption("#reportSubmissionsList");
    init_filter();
    return report_submission_table;
  };
  return {
    init_report_submission_table: init_report_submission_table,
  };
})();

let initEvtClk = (function () {
  $("[data-evt-clk]").on('click', function(e) {
    e.preventDefault();
    const eventData = JSON.parse($(this).attr("data-evt-clk"));
    const methodName = eventData.method;
    const params = eventData.params;

    if(methodName == 'showLate') {
      showLate(...params);
    }
  });
});

function showLate(
  late,
  reason,
  capa,
  modifiedBy,
  lastUpdated,
  configurationId,
  submissionId
) {
  var reasonList = JSON.parse(reason);
  $("#id").val(submissionId);
  $("#late").val(late);
  $("#modifiedBy").html(modifiedBy ? modifiedBy : "");
  $("#lastUpdated").html(lastUpdated ? lastUpdated : "");
  $(".reasonResponsibleRow").remove();

  for (var i = 0; i < reasonList.length; i++) {
    addReason(reasonList[i].responsible, reasonList[i].reason);
  }
  if (!_.isEmpty(capa) && capa != "null") {
    $("#capa").html(
      "<a href='" +
        reportSubmissionConfig.capaUrl +
        "?id=" +
        capa +
        "'>Open CAPA >>></a>"
    );
  } else {
    var link =
      reportSubmissionConfig.createCapaUrl +
      "?configurationId=" +
      configurationId +
      "&submissionId=" +
      submissionId;
    $("#capa").html(
      '<br><a href="' +
        link +
        '" class="btn btn-default okButton" >Initiate CAPA process </a>'
    );
  }
  $("#lateModalDiv").modal("show");
}
