$(function () {
  var tableFilter = {};
  var advancedFilter = false;
  var table = $("#rxTableInboundExecutionStatus").DataTable({
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
      initComplete: function () {
        initExecutionStatusDropDown("INBOUND_COMPLIANCE");
        initConfigurationTypeDropDown("rxTableInboundExecutionStatus");
        var table = $('#rxTableInboundExecutionStatus_wrapper')
        var dtSearch = $(".dt-search");
        var searchDiv = table.find('.dt-search')
        table.find('.topControls').before(dtSearch);
        searchDiv.attr("class", "dt-search pull-right");
        searchDiv.parent().attr("class", "col-xs-11 pull-right mb-10");
        searchDiv.parent().attr("style", "white-space: nowrap;");

        getReloader(
          $("#reportExecutionStatusDropDown"),
          $("#rxTableInboundExecutionStatus")
        );
        var inboundExecutionStatusDropDown = $("#reportExecutionStatusControl"); // For Dropdown -> each State
        inboundExecutionStatusDropDown.select2();
        inboundExecutionStatusDropDown.on("change", function () {
          checkStatus(this);
        });
      },
      customProcessing: true, //handled using processing.dt event
      serverSide: true,
      ajax: {
        url: inboundStatusUrl,
        dataSrc: "data",
        data: function (d) {
          d.tableFilter = JSON.stringify(tableFilter); // For Searching Feature
          d.searchString = d.search.value;
          if (d.order.length > 0) {
            d.direction = d.order[0].dir;
            d.sort = d.columns[d.order[0].column].data;
          }
          if ($('select[name="submissionFilter"]').length > 0) {
            d.status = $('select[name="submissionFilter"]').val();
          }
        },
      },
      aaSorting: [],
      order: [[2, "desc"]],
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
          $("#rxTableInboundExecutionStatus_wrapper")[0],
          settings.aLengthMenu[0][0],
          settings.json.recordsFiltered
        );
      },
      aoColumns: [
        {
          mData: "senderName",
          aTargets: ["senderName"],
          mRender: function (data, type, row) {
            var link;
            if (row.executionStatus == EXECUTION_STATUS_ENUM.COMPLETED) {
              link = viewExecutedConfigUrl + "/" + row.id;
            } else {
              link = viewUrl + "/" + row.inboundComplianceId;
            }
            if (data) {
              data = data
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;");
            }
            return "<a href=" + link + ">" + data + "</a>";
          },
        },
        {
          mData: "version",
          sClass: "dataTableColumnCenter",
        },
        {
          mData: "lastUpdated",
          aTargets: ["lastUpdated"],
          sClass: "dataTableColumnCenter forceLineWrapDate",
          mRender: function (data, type, full) {
            return moment
              .utc(data)
              .tz(userTimeZone)
              .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
          },
        },
        { mData: "owner" },
        {
          mData: "runDate",
          aTargets: ["runDate"],
          sClass: "dataTableColumnCenter forceLineWrapDate",
          mRender: function (data, type, row) {
            if (data == null) return null;
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
            if (data == null) return null;
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
          mData: "executionStatus",
          aTargets: ["executionStatus"],
          sClass: "dataTableColumnCenter",
          bSortable: false,
          mRender: function (data, type, row) {
            if (data == EXECUTION_STATUS_ENUM.GENERATING) {
              return (
                '<div><i class="fa fa-spinner fa-spin fa-lg es-generating popoverMessage" data-content="' +
                $.i18n._("Generating") +
                '"></i></div>'
              );
            } else if (data == EXECUTION_STATUS_ENUM.COMPLETED) {
              return (
                '<i class="fa fa-check-circle-o fa-lg es-completed popoverMessage" data-content="' +
                $.i18n._("Completed") +
                '"/>'
              );
            } else {
              var errorTime = moment
                .utc(row.dateCreated)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
              var message = "Occurred on: " + errorTime;
              message +=
                "<br> Entity ID: " +
                row.id +
                " (Error : " +
                row.messageTitle +
                " )";
              var details;
              details = row.status + " Details: " + row.message;
              return (
                '<div> <i class="fa fa-exclamation-circle fa-lg es-error popoverMessage" title="' +
                $.i18n._("Error") +
                '" data-content="' +
                message +
                ' ">' +
                '<a class="errorDetail" href="' +
                executionErrorUrl +
                "?id=" +
                row.id +
                '">' +
                " View Details </a></div>"
              );
            }
            return data;
          },
        },
      ],
    })
    .on("draw.dt", function () {
      setTimeout(function () {
        $("#rxTableInboundExecutionStatus tbody tr").each(function () {
          $(this).find("td:eq(4)").attr("nowrap", "nowrap");
        });
      }, 100);
    })
    .on("xhr.dt", function (e, settings, json, xhr) {
      checkIfSessionTimeOutThenReload(e, json);
    });

  $("#rxTableInboundExecutionStatus").on("mouseover", "tr", function () {
    $(".popoverMessage").popover({
      placement: "left",
      trigger: "hover focus",
      viewport: "#rxTableInboundExecutionStatus",
      html: true,
    });
  });

  actionButton("#rxTableInboundExecutionStatus");
  loadTableOption("#rxTableInboundExecutionStatus");
  $(".outside").hide();
});

function checkStatus(elem) {
  if (elem.value == EXECUTION_STATUS_DROP_DOWN_ENUM.GENERATING) {
    $('input[data-name="runDuration"]').attr("disabled", "disabled");
  } else {
    $('input[data-name="runDuration"]').removeAttr("disabled");
  }
  $("#rxTableInboundExecutionStatus")
    .DataTable()
    .order([[2, "desc"]])
    .draw();
}

function getReloader(toolbar, tableName) {
  var reloader =
    '<span title="Refresh" class="glyphicon reloaderBtn glyphicon-refresh" style="top:3px;"></span>';
  reloader = $(reloader);
  $(toolbar).append(reloader);
  if (tableName != undefined) {
    $(".reloaderBtn").on("click", function () {
      $(".reloaderBtn").addClass("glyphicon-refresh-animate");
      $(tableName).DataTable().draw();
    });
  }
}
