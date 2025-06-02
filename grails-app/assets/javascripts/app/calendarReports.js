$(function () {
  var tableFilter = {};
  var advancedFilter = false;
  var allTableParams;
  var init_table = function () {
    var table = $("#rxTableConfiguration")
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
        customProcessing: true, //handled using processing.dt event
        serverSide: true,
        ajax: {
          url: listUrl,
          dataSrc: "data",
          data: function (d) {
            d.tableFilter = JSON.stringify(tableFilter);
            d.advancedFilter = advancedFilter;
            d.searchString = d.search.value;
            d.sharedwith = $("#sharedWithFilterControl").val();
            d.pvp = isPublisherReport;
            d.allReportsForPublisher = $("#allReportsFilter").is(":checked");
            d.nextOnlyFilter = $("#nextOnlyFilter").is(":checked");
            d.showReportRequestFilter = $("#showReportRequestFilter").is(
              ":checked"
            );
            d.showReportFilter = $("#showReportFilter").is(":checked");
            if (d.order.length > 0) {
              d.direction = d.order[0].dir;
              //Column header mData value extracting
              d.sort = d.columns[d.order[0].column].data;
            }
            allTableParams = d;
          },
        },
        aaSorting: [6],
        order: [6, "asc"],
        bLengthChange: true,
        aLengthMenu: [
          [50, 100, 200, 500],
          [50, 100, 200, 500],
        ],
        pagination: true,
        iDisplayLength: 50,

        drawCallback: function (settings) {
          pageDictionary(
            $("#rxTableConfiguration_wrapper")[0],
            settings.aLengthMenu[0][0],
            settings.json.recordsFiltered
          );
        },
        aoColumns: [
          //Don't Change mData labels as we are using it for our sorting parameter name for sorting data should be property name
          {
            mData: "reportType",
          },
          {
            mData: "product",
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
              if (row.isReportRequest)
                ico =
                  '<sup type="' +
                  $.i18n._("reportRequest") +
                  '" style="font-weight: bold;">RR</sup>';
              return encodeToHTML(data) + ico;
            },
          },
          {
            mData: "startDate",
            sClass: "dataTableColumnCenter forceLineWrapDate",
            mRender: function (data, type, full) {
              return moment
                .utc(data)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_DISPLAY_FORMAT);
            },
          },
          {
            mData: "endDate",
            sClass: "dataTableColumnCenter forceLineWrapDate",
            mRender: function (data, type, full) {
              return moment
                .utc(data)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_DISPLAY_FORMAT);
            },
          },
          {
            mData: "dueDate",
            sClass: "dataTableColumnCenter forceLineWrapDate",
            mRender: function (data, type, full) {
              return moment
                .utc(data)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_DISPLAY_FORMAT);
            },
          },
          {
            mData: "runDate",
            sClass: "dataTableColumnCenter forceLineWrapDate",
            mRender: function (data, type, full) {
              return moment
                .utc(data)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
            },
          },
          {
            mData: "destinations",
          },
          {
            mData: "contributors",
            sClass: "contributorsTableCell",
            mRender: function (data, type, full) {
              if (full.isReportRequest) {
                return data;
              } else {
                return formContributorCell(
                  full.configParamsId,
                  full.primaryId,
                  full.contributorsId,
                  full.contributors
                );
              }
            },
          },
          {
            mData: "comment",
            mRender: function (data, type, row) {
              var commentedIcon = "fa-comment-o";
              if (row.comment) {
                commentedIcon = "fa-commenting-o commentPopoverMessage";
              }
              return (
                '<a  href="javascript:void(0)" class="commentModalTrigger" ' +
                'data-owner-id="' +
                (row.isReportRequest
                  ? row["configId"]
                  : row["configParamsId"]) +
                '" data-comment-type="' +
                (row.isReportRequest ? "SCHEDULER_RR" : "SCHEDULER") +
                '" data-toggle="modal" data-target="#commentModal" >' +
                '<span class="annotationPopover"><i class=" fa ' +
                commentedIcon +
                ' " data-content="' +
                row["comment"] +
                '" data-placement="left" style="z-index: 99999" title="' +
                $.i18n._("app.caseList.comment") +
                '"></i></span></a>'
              );
            },
          },
          {
            mData: null,
            bSortable: false,
            sClass: "dt-center",
            aTargets: ["id"],
            mRender: function (data, type, full) {
              var actionButton =
                '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="btn btn-success btn-xs"  data-evt-clk=\'{"method": "disableEventBinding", "params":[\"'+(full.isReportRequest ? viewRrUrl : viewUrl)+ '/'+full["configId"] +'?isPriorityReport=false\"]}\' >' +
                $.i18n._("view") +
                '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                        <li role="presentation"><a  role="menuitem" href="' +
                (full.isReportRequest ? editRrUrl : editUrl) +
                "/" +
                full["configId"] +
                '?isPriorityReport=false">' +
                $.i18n._("edit") +
                "</a></li> \
                         </ul> \
                    </div>";
              return actionButton;
            },
          },
        ],
        initComplete: function () {
          var topControls = $(".topControls");
          $('#rxTableConfiguration_wrapper').find('.dt-search').before(topControls);
          topControls.show();
          $(
            "#allReportsFilter, #nextOnlyFilter, #showReportRequestFilter, #showReportFilter"
          ).on("change", function (e) {
            table.draw();
          });
          bindPopOverEvents($(".commentPopoverMessage"));
        },
      })
      .on("draw.dt", function () {
        addReadMoreButton(".comment", 70);
        eventBindingClk();
      })
      .on("xhr.dt", function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
      });
    actionButton("#rxTableConfiguration");
    loadTableOption("#rxTableConfiguration");
  };

  var init_filter = function () {
    var filter_data = [
      {
        label: $.i18n._("app.advancedFilter.periodicReportType"),
        type: "select2-enum",
        name: "reportType",
        data_type: "PeriodicReportTypeEnum",
        data: reportTypes,
      },
      {
        label: $.i18n._("app.advancedFilter.product"),
        type: "text",
        name: "product",
        maxlength: 255,
      },
      {
        label: $.i18n._("app.advancedFilter.reportName"),
        type: "text",
        name: "reportName",
        maxlength: 500,
      },
      {
        label: $.i18n._("filter.reportingPeriodStartFrom"),
        type: "date-range",
        group: "startDate",
        group_order: 1,
      },
      {
        label: $.i18n._("filter.reportingPeriodStartTo"),
        type: "date-range",
        group: "startDate",
        group_order: 2,
      },
      {
        label: $.i18n._("filter.reportingPeriodEndFrom"),
        type: "date-range",
        group: "endDate",
        group_order: 1,
      },
      {
        label: $.i18n._("filter.reportingPeriodEndTo"),
        type: "date-range",
        group: "endDate",
        group_order: 2,
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
        label: $.i18n._("app.advancedFilter.RunDateStart"),
        type: "date-range",
        group: "runDate",
        group_order: 1,
      },
      {
        label: $.i18n._("app.advancedFilter.RunDateEnd"),
        type: "date-range",
        group: "runDate",
        group_order: 2,
      },
      {
        label: $.i18n._("app.destination.name"),
        type: "text",
        name: "destinations",
        maxlength: 255,
      },
    ];

    pvr.filter_util.construct_right_filter_panel({
      table_id: "#rxTableConfiguration",
      container_id: "config-filter-panel",
      filter_defs: filter_data,
      column_count: 1,
      done_func: function (filter) {
        tableFilter = filter;
        advancedFilter = true;
        var dataTable = $("#rxTableConfiguration").DataTable();
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
  $(document).on("click", ".export", function () {
    var form = $("#excelExportForm");
    form.find("input").detach();
    for (var x in allTableParams) {
      form.append(
        "<input type='hidden' name='" +
          x +
          "' value='" +
          allTableParams[x] +
          "'>"
      );
    }
    form.submit();
  });
  var init = function () {
    init_table();
    init_filter();
    buildReportingDestinationsSelectBox(
      $("select[name='publisherContributors']"),
      publisherContributorsUrl,
      $("input[name='primaryPublisherContributor']"),
      true,
      userValuesUrl
    );
  };
  if ($("#contributorsEditDiv").length > 0) {
    $(document).on("click", "td.contributorsTableCell", function (e) {
      var $this = $(this);
      var oldVal = [];
      var id = $this.find(".contributorCell").attr("data-configParamsId");
      if (!id) return;
      var oldPrimaryId = $this.find(".contributorCell").attr("data-primaryId");
      if (oldPrimaryId) oldVal.push(oldPrimaryId);
      var oldContributorsId = $this
        .find(".contributorCell")
        .attr("data-contributorsId");
      if (oldContributorsId)
        oldVal = oldVal.concat(oldContributorsId.split(","));
      var oldLabel = $this.find(".contributorCell").text();
      var $textEditDiv = $("#contributorsEditDiv");
      var $select = $("select[name=publisherContributors]");
      var $primaryInput = $("input[name=primaryPublisherContributor]");
      showEditDiv($this, $textEditDiv, $select);
      $primaryInput.val(oldPrimaryId);
      $select.val(oldVal).trigger('change').focus();
      $textEditDiv.find(".saveButton").one("click", function (e) {
        showLoader();
        $.ajax({
          type: "GET",
          url: editContributorsdUrl,
          data: {
            primaryId: $primaryInput.val(),
            contributorsId: $select.select2("val"),
            configParamsId: id,
          },
          dataType: "json",
        })
          .done(function (result) {
            $this.html(
              formContributorCell(
                result.configParamsId,
                result.primaryId,
                result.contributorsId,
                result.contributors
              )
            );
            hideLoader();
          })
          .fail(function (err) {
            alert("Unexpected server error! " + err);
            error(err);
            hideLoader();
          });
        $(".popupBox").hide();
      });
    });
    $(document).on("click", ".popupBox .cancelButton", function () {
      $(".popupBox").hide();
      $(".saveButton").off();
    });
  }

  function formContributorCell(
    configParamsId,
    primaryId,
    contributorsId,
    label
  ) {
    return (
      "<span class='contributorCell' data-configParamsId='" +
      configParamsId +
      "'  data-primaryId='" +
      primaryId +
      "' data-contributorsId='" +
      contributorsId +
      "' >" +
      label +
      " <i class='fa fa-edit'></i></span>"
    );
  }

  init();

  let eventBindingClk = (function () {
    $("[data-evt-clk]").on('click', function (e) {
      e.preventDefault();
      const eventData = JSON.parse($(this).attr("data-evt-clk"));
      const methodName = eventData.method;
      const params = eventData.params;
      if (methodName == "disableEventBinding") {
        var eventElement = $(this);
        disableEventBinding(eventElement, params);
      }
    });
  });

});

function disableEventBinding(eventElement, params) {
  $(eventElement).on("click", function (e) {
    if ($(this).attr("disabled") == "disabled") {
      e.preventDefault();
    }
  });
  $(eventElement).attr("disabled", "disabled");
  location.href = params[0];
}
