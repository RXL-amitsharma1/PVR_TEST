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
var icsrReport = icsrReport || {};

var moretext = $.i18n._("moretext");
var lesstext = $.i18n._("lesstext");

function testMethod(btn) {
  // alert($(btn).attr('exconfig-id'))
  var urlToHit =
    periodicReportConfig.generateXML +
    "?id=" +
    $(btn).attr("exconfig-id") +
    "&outputFormat=XML";
  // var urlToHit = periodicReportConfig.generateXML + "?id=25483";
  $.ajax({
    url: urlToHit,
    dataType: "json",
  })
    .done(function (result) {
      console.log("success is called");
      if (result.warning) {
        warningNotification(result.message);
        return;
      }
      // window.location = 'reports';
      successNotification(result.message);
      setTimeout(function () {
        reloadData(exConfigId);
      }, 1000);
    })
    .fail(function (err) {
      errorNotification("Server Error!");
    });
}
icsrReport.icsrReportList = (function () {
  //ICSR Report table.
  var icsr_report_table;
  var tableFilter = {};
  var advancedFilter = false;

  //The function for initializing the ICSR Report data tables.
  var init_icsr_report_table = function () {
    //Initialize the data table
    icsr_report_table = $("#icsrReportList")
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
          $("#icsr-list-conainter").on("click", ".favorite", function () {
            changeFavoriteState(
              $(this).data("exconfig-id"),
              $(this).hasClass("glyphicon-star-empty"),
              $(this)
            );
          });

          //Toggle the action buttons on the action item list.
          actionButton("#icsrReportList");

          $("#icsr-list-conainter").on("click", ".r2xml", function () {
            // generateReport($(this).data('exconfigId'), REPORT_ACTION_TYPE.GENERATE_DRAFT)
            // alert($(this).attr("exconfig-id"));
            // generateXML($(this).data('exconfigId'),'r2');
          });

          $("#icsr-list-conainter").on("click", ".generateDraft", function () {
            generateReport(
              $(this).data("exconfigId"),
              REPORT_ACTION_TYPE.GENERATE_DRAFT
            );
          });
          $("#icsr-list-conainter").on(
            "click",
            ".generateFinalDraft",
            function () {
              generateReport(
                $(this).data("exconfigId"),
                REPORT_ACTION_TYPE.GENERATE_FINAL
              );
            }
          );
          $("#icsr-list-conainter").on(
            "click",
            ".generateCasesDraft",
            function () {
              generateReport(
                $(this).data("exconfigId"),
                REPORT_ACTION_TYPE.GENERATE_CASES_DRAFT
              );
            }
          );
          $("#icsr-list-conainter").on(
            "click",
            ".generateCasesFinalDraft",
            function () {
              generateReport(
                $(this).data("exconfigId"),
                REPORT_ACTION_TYPE.GENERATE_CASES_FINAL
              );
            }
          );
          $("#icsr-list-conainter").on("click", ".generateCases", function () {
            generateReport(
              $(this).data("exconfigId"),
              REPORT_ACTION_TYPE.GENERATE_CASES
            );
          });
          $("#icsr-list-conainter").on(
            "click",
            ".createActionItem",
            function () {
              actionItem.actionItemModal.set_executed_report_id(
                $(this).data("exconfigId")
              );
              actionItem.actionItemModal.init_action_item_modal(
                false,
                PERIODIC_REPORT,
                hasAccessOnActionItem
              );
            }
          );
          //Show the draft modal window.
          submission_modal_after_load();

          $("#icsrReportList").on(
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
          $("#icsrReportList").on("click ", ".morelink", function () {
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
          initArchiveFilter(icsr_report_table);
          initSharedWithFilter("icsrReportList", icsr_report_table);
          $("#submissionFilter")
            .select2({
              placeholder: $.i18n._("app.advancedFilter.state"),
              allowClear: true,
            })
            .on("change", function () {
              icsr_report_table.draw();
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
            icsr_report_table.draw();
          });
          if (sessionStorage.getItem(sessionStorageSubmissionFilterVariableName)) {
            submissionFilterSelect.val(sessionStorage.getItem(sessionStorageSubmissionFilterVariableName)).trigger("change");
          }
          setTimeout(function () {
            if (sessionStorage.getItem(sessionStorageSubmissionFilterVariableName) || sessionStorage.getItem(sessionStorageSharedWithVariableName))
                icsr_report_table.draw();
          }, 100);
        },

        customProcessing: true, //handled using processing.dt event
        serverSide: true,
        ajax: {
          url: periodicReportConfig.icsrReportsListUrl,
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
            }
          },
        },
        rowId: "id",
        order: [
          [2, "asc"],
          [8, "desc"],
        ],
        columnDefs: [
          { orderable: false, targets: [0, 5, 6, 7, 10] },
          { width: "25", targets: 2 },
          { width: "70", targets: 4 },
        ],
        aLengthMenu: [
          [10, 25, 50, 100],
          [10, 25, 50, 100],
        ],
        lengthChange: true,
        pagination: true,
        iDisplayLength: 10,

        drawCallback: function (settings) {
          pageDictionary(
            $("#icsrReportList_wrapper")[0],
            settings.aLengthMenu[0][0],
            settings.json.recordsFiltered
          );
          getReloader($("#icsrReportList_info"), $("#icsrReportList"));
        },
        columns: [
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
              var arch = row.isArchived
                ? '<span class="glyphicon glyphicon-text-background" title="' +
                  $.i18n._("labelArchive") +
                  '"></span> '
                : "";
              return (
                arch +
                '<a href="' +
                periodicReportConfig.reportViewUrl +
                "?id=" +
                row.id +
                '" >' +
                encodeToHTML(row.reportName) +
                "</a>"
              );
            },
          },
          {
            data: "version",
          },

          {
            data: "recipient",
            mRender: $.fn.dataTable.render.text(),
          },
          {
            data: "sender",
            mRender: $.fn.dataTable.render.text(),
          },
          {
            data: "dateCreated",
            aTargets: ["dateCreated"],
            sClass: "dataTableColumnCenter",
            mRender: function (data, type, full) {
              return moment
                .utc(data)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
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
            mRender: $.fn.dataTable.render.text(),
          },
          {
            mData: null,
            bSortable: false,
            sClass: "dataTableColumnCenter",
            aTargets: ["id"],
            mRender: function (data, type, row) {
              var actionButton =
                '<div class="btn-group dropdown dataTableHideCellContent" align="center">' +
                '<a class="btn btn-success btn-xs" href="' +
                periodicReportConfig.configurationViewUrl +
                "/" +
                data["id"] +
                '">' +
                $.i18n._("view") +
                "</a>";
              if (typeof isAdmin !== "undefined" && isAdmin) {
                actionButton +=
                  '<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                             <li role="presentation"><a href="' +
                  comparisonUrl +
                  "?id=" +
                  row.id +
                  '" role="menuitem"  >' +
                  $.i18n._("comparison.run") +
                  "</a></li>\
                             </ul>";
              }
              actionButton += "</div>";
              return actionButton;
            },
          },
        ],
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
          maxlength: 200,
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
        table_id: "#icsrReportList",
        container_id: "config-filter-panel",
        filter_defs: filter_data,
        column_count: 1,
        done_func: function (filter) {
          tableFilter = filter;
          advancedFilter = true;
          var dataTable = $("#icsrReportList").DataTable();
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

    loadTableOption("#icsrReportList");
    return icsr_report_table;
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

  var generateXML = function (id, type) {
    if (exConfigId != undefined && exConfigId != "") {
      highlightRow(exConfigId);
      var urlToHit =
        periodicReportConfig.generateDraftUrl +
        "?id=" +
        exConfigId +
        "&reportAction=" +
        reportAction;
      $.ajax({
        dataType: "json",
        url: urlToHit,
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
    var dataTable = $("#icsrReportList").DataTable();
    dataTable.ajax.reload(function () {
      highlightRow(rowId);
    }, resetPagination);
  };

  var highlightRow = function (rowId) {
    if (rowId != undefined && rowId != "") {
      var dataTable = $("#icsrReportList").DataTable();
      dataTable
        .row("#" + rowId)
        .nodes()
        .to$()
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
            hideModalLoader(reportSubmissionModal);
            buildReportingDestinationsSelectBox(
              reportSubmissionModal.find("[name=reportingDestinations]"),
              periodicReportConfig.reportingDestinationsUrl,
              reportSubmissionModal.find(
                "input[name='primaryReportingDestination']"
              ),
              false
            );
            var submissionDate = reportSubmissionModal
              .find('.datepicker input[name="submissionDate"]')
              .val();
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
            $("#dueDateDiv").datepicker({
              allowPastDates: true,
              date: dueDate,
              twoDigitYearProtection: true,
              culture: userLocale,
              momentConfig: {
                format: DEFAULT_DATE_DISPLAY_FORMAT,
              },
            });
            reportSubmissionModal
              .find("button.submit-draft")
              .on("click", function () {
                $.ajax({
                  url: periodicReportConfig.reportSubmitUrl,
                  method: "POST",
                  data: reportSubmissionModal.find("form").serialize(),
                  dataType: "json",
                })
                  .done(function (result) {
                    reportSubmissionModal.modal("hide");
                    reloadData(rowId);
                    successNotification(result.message);
                  })
                  .fail(function (err) {
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
                      reportSubmissionModal
                        .find("form")
                        .find(".has-error")
                        .size() == 0
                    ) {
                      var errorPanel = $("#error-panel");
                      errorPanel.html("");
                      responseTextObj.errorMsg.forEach(function (item) {
                        errorPanel.append(item + "<br/>");
                      });

                      errorPanel.css("display", "block");
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
    init_icsr_report_table: init_icsr_report_table,
    confirmArchive: confirmArchive,
  };
})();
