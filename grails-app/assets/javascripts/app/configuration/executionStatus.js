$(function () {

  $("[data-evt-clk]").on('click', function(e) {
    e.preventDefault();
    const eventData = JSON.parse($(this).attr("data-evt-clk"));
    const methodName = eventData.method;
    const params = eventData.params;

    if (methodName == "showDeletedError") {
      // Call the method from the eventHandlers object with the params
      showDeletedError();
    }
  });

  var isAdmin = $("#isAdmin").val() === "true";
  var tableFilter = {};
  var advancedFilter = false;
  var table = $("#rxTableReportsExecutionStatus")
    .DataTable({
      layout: {
        topStart: null,
        topEnd: {search: {placeholder: 'Search'}},
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
        initSharedWithFilter("rxTableReportsExecutionStatus", table, "99%");
        var topControls = $(".topControls");
        $('#rxTableBulkSheduling_wrapper').find('.dt-search').before(topControls);
        topControls.show();
        var sharedWithFilter = $("#sharedWithFilter");
        var configTypeDiv = $("#configTypeDiv");
        var executionStatus = $("#reportExecutionStatusDropDown");
        var wrapper = $("#rxTableReportsExecutionStatus_wrapper")
        wrapper.parent().parent().find(".rxmain-container-content")
            .append(wrapper.parent())

        initExecutionStatusDropDown(isICSRProfile ? "ICSR_PROFILE" : "REPORTS");
        initConfigurationTypeDropDown(
          "rxTableReportsExecutionStatus",
          sharedWithFilter
        );
        wrapper
            .find(".topControls")
            .append(sharedWithFilter)
            .append(executionStatus)
            .append(configTypeDiv)
        wrapper
            .find(".dt-search")
            .parent()
            .removeClass("col-xs-4")

        getReloader(
            executionStatus,
          $("#rxTableReportsExecutionStatus")
        );

        var executionStatusDropDown = $("#reportExecutionStatusControl");

        var searchDiv = $('#rxTableReportsExecutionStatus_wrapper').find('.dt-search').parent()
        searchDiv.attr("class", "col-xs-11 pull-right mb-10");

        // sharedWithFilter.find("label").detach();
        executionStatusDropDown.select2();
        executionStatusDropDown.on("change", function () {
          checkStatus(this);
        });
      },
      customProcessing: true, //handled using processing.dt event
      serverSide: true,
      ajax: {
        url: executionStatusUrl + "?isICSRProfile=" + isICSRProfile,
        type: "POST",
        dataSrc: "data",
        data: function (d) {
          d.tableFilter = JSON.stringify(tableFilter);
          d.advancedFilter = advancedFilter;
          d.sharedwith = $("#sharedWithFilterControl").val();
          d.searchString = d.search.value;
          if (d.order.length > 0) {
            d.direction = d.order[0].dir;
            //Column header mData value extracting
            d.sort = d.columns[d.order[0].column].data;
          }
          if ($('select[name="submissionFilter"]').length > 0) {
            d.status = $('select[name="submissionFilter"]').val();
          }
        },
      },
      aaSorting: [],
      //we would need to change in checkStatus function as well while changing default order
      order: [[5, "desc"]],
      bLengthChange: true,
      aLengthMenu: [
        [50, 100, 200, 500],
        [50, 100, 200, 500],
      ],
      pagination: true,
      iDisplayLength: 50,

      drawCallback: function (settings) {
        $(".reloaderBtn").removeClass("glyphicon-refresh-animate");
        pageDictionary(
          $("#rxTableReportsExecutionStatus_wrapper")[0],
          settings.aLengthMenu[0][0],
          settings.json.recordsFiltered
        );
      },
      aoColumns: [
        {
          mData: "reportName",
          aTargets: ["reportName"],
          mRender: function (data, type, row) {
            var link;
            if (row.executionStatus == EXECUTION_STATUS_ENUM.SCHEDULED) {
              link = ShowScheduledConfigUrl + "/" + row.id;
            } else if (
              row.executionStatus == EXECUTION_STATUS_ENUM.COMPLETED ||
              row.executionStatus == EXECUTION_STATUS_ENUM.WARN
            ) {
              link = viewResultURL + "/" + row.exeutionStId;
            } else {
              link = ShowConfigUrl + "/" + row.exeutionStId;
            }
            if (data) {
              data = encodeToHTML(data);
            }
            if(row.isDeleted){
              return '<a href="javascript:void(0);" data-evt-clk=\'{\"method\": \"showDeletedError\", \"params\":[]}\' >' + data + '</a>';
            } else {
              return '<a href='+link+'>'+data+'</a>';
            }
          },
        },
        {
          mData: "periodicReportType",
          aTargets: ["periodicReportType"],
          sClass: "dataTableColumnCenter",
          visible: false,
        },
        {
          mData: "version",
          sClass: "dataTableColumnCenter",
        },
        {
          mData: "executionStatus",
          aTargets: ["executionStatus"],
          sClass: "dataTableColumnCenter",
          bSortable: false,
          mRender: function (data, type, row) {
            var priorityReport = "";
            if (row.isPriorityReport) {
              priorityReport =
                '<i class="es-priorityReport popoverMessage" data-content="' +
                $.i18n._("prioritized") +
                '"><img src="/reports/assets/icons/priority.svg" /></i>';
            } else if (
              row.executionStatus == EXECUTION_STATUS_ENUM.BACKLOG &&
              row.isPriorityReport == false &&
              row.isEntityTypeCanBePriority != "" &&
              row.isEntityTypeCanBePriority == true &&
              isPriorityRoleEnable
            ) {
              priorityReport =
                '<i class="updatePriority es-priorityReport popoverMessage e-status" data-configid="' +
                row.id +
                '" data-exstatus="' +
                row.executionStatus +
                '" data-exstatusid="' +
                row.exeutionStId +
                '" data-actionUrl="' +
                updatePriorityUrl +
                '" data-content="' +
                $.i18n._("prioritize") +
                '"><img src="/reports/assets/icons/priority.svg" /></i>';
            }
            if (row.executionStatus == EXECUTION_STATUS_ENUM.BACKLOG) {
              return (
                priorityReport +
                '<i class="fa fa-clock-o fa-lg es-scheduled popoverMessage" data-content="' +
                $.i18n._("Scheduled") +
                '"></i><span class="glyphicon glyphicon-stop popoverMessage" data-content="' +
                $.i18n._("app.executionStatus.unschedule.label") +
                '" data-id="' +
                row.exeutionStId +
                '" data-actionUrl="' +
                removeFromBacklogUrl +
                '"></span>'
              );
            } else if (row.executionStatus == EXECUTION_STATUS_ENUM.SCHEDULED) {
              return (
                priorityReport +
                '<i class="fa fa-clock-o fa-lg es-scheduled popoverMessage" data-content="' +
                $.i18n._("Scheduled") +
                '"></i><span class="glyphicon glyphicon-stop popoverMessage ' +
                (row.periodicReportType.trim() ? "aggregate" : "") +
                '" data-content="' +
                $.i18n._("app.executionStatus.unschedule.label") +
                '" data-id="' +
                row.id +
                '" data-actionUrl="' +
                unscheduleUrl +
                '"></span>'
              );
            } else if (
              row.executionStatus == EXECUTION_STATUS_ENUM.GENERATING
            ) {
              return (
                "<div>" +
                priorityReport +
                '<i class="fa fa-spinner fa-spin fa-lg es-generating popoverMessage" data-content="' +
                $.i18n._("Generating") +
                '"></i><span class="glyphicon glyphicon-stop" data-id="' +
                row.exeutionStId +
                '" data-actionUrl="' +
                killExecutionUrl +
                '"></span></div>'
              );
            } else if (
              row.executionStatus == EXECUTION_STATUS_ENUM.DELIVERING
            ) {
              return (
                '<i class="fa fa-spinner fa-spin fa-lg es-delivering popoverMessage" data-content="' +
                $.i18n._("Delivering") +
                '"/>'
              );
            } else if (row.executionStatus == EXECUTION_STATUS_ENUM.COMPLETED) {
              return priorityReport + '<i class="fa fa-check-circle-o fa-lg' + (row.isDeleted ? ' es-completed-deleted ' : ' es-completed ') + 'popoverMessage" data-content="' + (row.isDeleted ? $.i18n._('Deleted') : $.i18n._('Completed')) + '"/>';
            } else {
              if (
                row.executionStatus == EXECUTION_STATUS_ENUM.ERROR ||
                row.executionStatus == EXECUTION_STATUS_ENUM.WARN
              ) {
                var errorTime = moment
                  .utc(row.dateCreated)
                  .tz(userTimeZone)
                  .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                var message = "Occurred on: " + errorTime;
                message +=
                  "<br> Entity ID: " +
                  row.id +
                  " (Error : " +
                  row.errorTitle +
                  ")";
                var details;
                details = row.executionStatus + " Details: " + row.errorMessage;

                if (row.executionStatus == EXECUTION_STATUS_ENUM.ERROR) {
                  return (
                    "<div>" +
                    priorityReport +
                    '<i class="fa fa-exclamation-circle fa-lg es-error popoverMessage" title="' +
                    $.i18n._("Error") +
                    '" data-content="' +
                    message +
                    ' ">' +
                    '<a class="errorDetail" href="' +
                    executionErrorUrl +
                    "?id=" +
                    row.exeutionStId +
                    '">' +
                    " View Details </a></div>"
                  );
                } else if (row.executionStatus == EXECUTION_STATUS_ENUM.WARN) {
                  return (
                    "<div>" +
                    priorityReport +
                    '<i class="fa fa-exclamation-circle fa-lg es-warn popoverMessage" title="' +
                    $.i18n._("Error") +
                    '" data-content="' +
                    message +
                    ' ">' +
                    '<a class="errorDetail" href="' +
                    executionErrorUrl +
                    "?id=" +
                    row.exeutionStId +
                    '">' +
                    " View Details </a></div>"
                  );
                }
              }
            }

            return data;
          },
        },
        {
          mData: "owner",
          mRender: $.fn.dataTable.render.text(),
        },
        {
          mData: "runDate",
          aTargets: ["runDate"],
          sClass: "dataTableColumnCenter forceLineWrapDate",
          mRender: function (data, type, row) {
            return moment
              .utc(data)
              .tz(userTimeZone)
              .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
          },
        },
        {
          mData: "executionTime",
          aTargets: ["executionTime"],
          sClass: "dataTableColumnCenter",
          mRender: function (data, type, row) {
            var duration = moment.duration(parseInt(data, 10));
            var addZero = function (v) {
              return Math.floor(v);
            };
            var time = "";
            if (duration.days() == 1) {
              time += addZero(duration.days()) + $.i18n._("day");
            } else if (duration.days() != 0) {
              time += addZero(duration.days()) + $.i18n._("days");
            }
            if (duration.hours() == 1) {
              time += " " + addZero(duration.hours()) + $.i18n._("hour");
            } else if (duration.hours() != 0) {
              time += " " + addZero(duration.hours()) + $.i18n._("hours");
            }
            if (duration.minutes() == 1) {
              time += " " + addZero(duration.minutes()) + $.i18n._("minute");
            } else if (duration.minutes() != 0) {
              time += " " + addZero(duration.minutes()) + $.i18n._("minutes");
            }
            if (duration.seconds() == 1 && duration.milliseconds() == 0) {
              time += " " + addZero(duration.seconds()) + $.i18n._("second");
            } else if (
              duration.seconds() != 0 &&
              duration.milliseconds() != 0
            ) {
              time +=
                " " +
                Math.round(
                  duration.seconds() + duration.milliseconds() / 1000
                ) +
                $.i18n._("seconds");
            } else if (
              duration.seconds() == 0 &&
              duration.milliseconds() != 0
            ) {
              time +=
                " " +
                Math.round(duration.milliseconds() / 1000) +
                $.i18n._("second");
            }
            return time;
          },
        },
        {
          mData: "frequency",
          sClass: "dataTableColumnCenter",
          mRender: function (data, type, row) {
            return $.i18n._("app.frequency." + data);
          },
        },
        {
          mData: "sharedWith",
          aTargets: ["sharedWith"],
          sClass: "dataTableColumnCenter",
          mRender: function (data, type, row) {
            var users = "";
            var total = 0;
            if (data) {
              total = data.length >= 0 ? data.length : 0;
              users = data.join(", ");
            }
            return (
              '<a href="#" class="popoverMessage" title="" data-content="' +
              encodeToHTML(users) +
              '">' +
              total +
              $.i18n._("users") +
              "</a>"
            );
          },
        },
        {
          mData: "deliveryMedia"
        },
        {
          mData: "runDate",
          aTargets: ["runDate"],
          bVisible: false, // this is a hack for date filter
          sClass: "dataTableColumnCenter",
          mRender: function (data, type, full) {
            return moment.utc(data).tz(userTimeZone).format("L");
          },
        },
      ],
    })
    .on("draw.dt", function () {
      setTimeout(function () {
        $("#rxTableReportsExecutionStatus tbody tr").each(function () {
          $(this).find("td:eq(4)").attr("nowrap", "nowrap");
        });
      }, 100);
    })
    .on("xhr.dt", function (e, settings, json, xhr) {
      checkIfSessionTimeOutThenReload(e, json);
    });

  $("#rxTableReportsExecutionStatus").on("mouseover", "tr", function () {
    $(".popoverMessage").popover({
      placement: "right",
      trigger: "hover focus",
      viewport: "#rxTableReportsExecutionStatus",
      html: true,
    });
  });

  $("#rxTableReportsExecutionStatus").on(
    "click",
    ".glyphicon-stop",
    function () {
      var thiz = $(this);
      var executeTask = function () {
        $.ajax({
          url: thiz.attr("data-actionUrl"),
          data: {
            id: thiz.data("id"),
            auditLogJustification: $("#reportJustification").val(),
          },
          dataType: "json",
        })
          .done(function (result) {
            if (result.success) {
              setTimeout(function () {
                $("#rxTableReportsExecutionStatus").DataTable().draw();
              }, 1500);
            }
          })
          .fail(function (err) {
            var responseText = err.responseText;
            var responseTextObj = JSON.parse(responseText);
            alert(responseTextObj.message);
          });
      };
      if (thiz.hasClass("aggregate")) {
        $("#reportJustificationModal .save")
          .off()
          .one("click", function () {
            $("#reportJustificationModal").modal("hide");
            executeTask();
          });
        $(".forReport").hide();
        $(".forUnschedule").show();
        $("#reportJustificationModal").modal("show");
      } else {
        if (confirm($.i18n._("cancel.execution"))) executeTask();
      }
    }
  );

  $("#rxTableReportsExecutionStatus").on(
    "click",
    ".updatePriority",
    function () {
      if (confirm($.i18n._("confirmation.for.priority"))) {
        $.ajax({
          url: $(this).attr("data-actionUrl"),
          data: {
            configId: $(this).data("configid"),
            exStatus: $(this).data("exstatus").toUpperCase(),
            exStatusId: $(this).data("exstatusid"),
          },
          dataType: "json",
        })
          .done(function (result) {
            if (result.success) {
              setTimeout(function () {
                $("#rxTableReportsExecutionStatus").DataTable().draw();
              }, 1500);
            }
          })
          .fail(function (err) {
            var responseText = err.responseText;
            var responseTextObj = JSON.parse(responseText);
            alert(responseTextObj.message);
          });
      }
    }
  );

  actionButton("#rxTableReportsExecutionStatus");
  loadTableOption("#rxTableReportsExecutionStatus");
  $(".outside").hide();

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
        name: "reportVersion",
      },
      {
        label: $.i18n._("app.advancedFilter.owner"),
        type: "id",
        name: "owner",
      },
      {
        label: $.i18n._("app.advancedFilter.RunDateStart"),
        type: "date-range",
        group: "nextRunDate",
        group_order: 1,
      },
      {
        label: $.i18n._("app.advancedFilter.RunDateEnd"),
        type: "date-range",
        group: "nextRunDate",
        group_order: 2,
      },
      {
        label: $.i18n._("app.advancedFilter.runDurationFrom"),
        type: "number-range",
        group: "runDuration",
        group_order: 1,
      },
      {
        label: $.i18n._("app.advancedFilter.runDurationTo"),
        type: "number-range",
        group: "runDuration",
        group_order: 2,
      },
      {
        label: $.i18n._("app.advancedFilter.frequency"),
        type: "select2-enum",
        name: "frequency",
        data_type: "FrequencyEnum",
        data: frequencyType,
      },
      {
        label: $.i18n._("app.advancedFilter.periodicReportType"),
        type: "text",
        name: "periodicReportType",
        maxlength: 255,
      },
    ];

    pvr.filter_util.construct_right_filter_panel({
      table_id: "#rxTableReportsExecutionStatus",
      container_id: "config-filter-panel",
      filter_defs: filter_data,
      column_count: 1,
      done_func: function (filter) {
        tableFilter = filter;
        advancedFilter = true;
        var dataTable = $("#rxTableReportsExecutionStatus").DataTable();
        dataTable.ajax.reload(function (data) {}, false).draw();
      },
    });
    bindSelect2WithUrl(
      $("select[data-name=owner]"),
      ownerListUrl,
      ownerValuesUrl,
      true
    );
  };

  init_filter();
});

function checkStatus(elem) {
  if (elem.value == EXECUTION_STATUS_DROP_DOWN_ENUM.SCHEDULED) {
    $('input[data-name="runDuration"]').attr("disabled", "disabled");
    $('input[data-name="reportVersion"]').attr("disabled", "disabled");
    $('input[data-name="periodicReportType"]').attr("disabled", "disabled");
    $('select[data-name="frequency"]').attr("disabled", "disabled");
  } else {
    $('input[data-name="runDuration"]').removeAttr("disabled");
    $('input[data-name="reportVersion"]').removeAttr("disabled");
    $('input[data-name="periodicReportType"]').removeAttr("disabled");
    $('select[data-name="frequency"]').removeAttr("disabled");
  }

  $("#rxTableReportsExecutionStatus")
    .DataTable()
    .order([[5, "desc"]])
    .draw();
}

function showDeletedError() {
  $('#WarningDiv').show();
  $('#WarningDiv p').text($.i18n._('app.view.deleted.report'));
  $('.WarningDivclose').on('click', function () {
    $('#WarningDiv').hide();
  });
}

function getReloader(toolbar, tableName) {
  var reloader =
    '<span title="Refresh" class="glyphicon reloaderBtn glyphicon-refresh"></span>';
  reloader = $(reloader);
  $(toolbar).append(reloader);
  if (tableName != undefined) {
    $(".reloaderBtn").on("click", function () {
      $(".reloaderBtn").addClass("glyphicon-refresh-animate");
      $(tableName).DataTable().draw();
    });
  }
}
