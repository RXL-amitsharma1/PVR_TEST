//= require vendorUi/gridstack/gridstack-all

var selectedWidgets = [];
var CHART_WIDGET_ACTIONS = { RUN: "RUN", REFRESH: "REFRESH", SAVE: "SAVE" };
$(function () {
  function saveWidgets() {
    if (isEditable) {
      var serializedData = _.map(
        $(".grid-stack > .grid-stack-item:visible"),
        function (el) {
          return {
            id: parseInt($(el).attr("gs-id")),
            x: parseInt($(el).attr("gs-x")),
            y: parseInt($(el).attr("gs-y")),
            width: parseInt($(el).attr("gs-w")),
            height: parseInt($(el).attr("gs-h")),
          };
        },
        this
      );
      $.ajax({
        type: "POST",
        url: CONFIGURATION.updateWidgetsUrl,
        dataType: "json",
        data: {
          items: JSON.stringify(serializedData, null, "    "),
          id: $("#dashboardId").val(),
        },
      }).fail(function (err) {
        console.log(err);
      });
    }
  }

  $(".rx-widget-menu-content").menu({
    select: function (event, ui) {
      if (isEditable) {
        var url = ui.item.data("url");
        var params = ui.item.data("params");
        if (CONFIGURATION[url]) {
          params.id = $("#dashboardId").val();
          window.location.href = CONFIGURATION[url] + "?" + $.param(params);
        }
      }
    },
  });

  var gridstackOptions = {
    column: 12,
    resizable: { autoHide: true, handles: "all" },
    margin: "10px 5px",
    draggable: {
      handle: ".rx-widget-header",
    },
    cellHeight: 60,
  };
  var gridstack = GridStack.init(gridstackOptions);

  $(".remove-widget").on("click", function () {
    if (isEditable) {
      var widget = $(this).closest(".grid-stack-item");
      var url = $(this).data("url");
      var params = $(this).data("params");
      params.id = $("#dashboardId").val();
      if (CONFIGURATION[url]) {
        $.ajax({
          type: "POST",
          url: CONFIGURATION[url],
          dataType: "json",
          data: params,
        })
          .done(function () {
            const id = $(widget).attr("gs-id");
            const widgetToRemove = gridstack
              .getGridItems()
              .find((widgetEl) => $(widgetEl).attr("gs-id") === id);
            gridstack.removeWidget(widgetToRemove);
          })
          .fail(function (err) {
            console.log(err);
          });
      }
    }
  });

  gridstack.on("resizestart", function () {
    $("#mainDropdownMenu").closest(".open").removeClass("open");
  });

  gridstack.on("change", function (event, gridItem) {
    for (let i = 0; i < gridItem.length; i++) {
      let item = gridItem[i];
      let chartContainer = $(item.el).find(".chart-container"),
          chart = chartContainer.find(".tabChart");
      if (chart.length && chart.highcharts()) {
        chart.highcharts().reflow();
      }

      $(".widget-calendar").each(function () {
        var calendar = $(this);
        calendar.fullCalendar(
            "option",
            "contentHeight",
            calendar.parent().height() - 150
        );
      });

      $(item.el).find(".chartData").trigger("change");
    }
    saveWidgets();
  });

  $(".chart-container").each(function (index, element) {
    var container = $(this);
    reloadChartWidgetData(container);
  });
  $(".chart-container").on("reload", function (event, message) {
    var container = $(this);
    reloadChartWidgetData(container);
  });
  $(".chart-container").on("pushNotification", function (event, message) {
    var container = $(this);
    var widgetTitle = container.data("widgetTitle");
    // TODO when executed configuration will contain configuration ID, change this hack
    if (
      message &&
      message.message &&
      message.message.indexOf(widgetTitle) > -1
    ) {
      reloadChartWidgetData(container);
      container.parent().find(".chart-running").hide();
      container.parent().find(".refresh-widget").show();
      container.parent().find(".edit-widget").show();
      container.parent().find("#runningMessage").hide();
      return false;
    }
  });

  function reloadChartWidgetData(container) {
    $.ajax({
      type: "GET",
      url: CONFIGURATION.getChartDataUrl,
      dataType: "json",
      data: {
        widgetId: container.data("widgetId"),
        id: $("#dashboardId").val(),
      },
    })
      .fail(function (err) {
        console.log("Error on " + CONFIGURATION.getChartDataUrl);
        console.log(err);
      })
      .done(function (data) {
        if (data.sectionName !== null) {
          var headerElement = container
            .parent()
            .find(".rxmain-container-header-label.rx-widget-title");
          headerElement.prop("title", encodeToHTML(data.sectionName));
        }
        var isPVCModule = $("#isPVCModule").val();
        if (isPVCModule == "true") {
          container
            .parent()
            .find(".titleLink")
            .attr(
              "href",
              "/reports/advancedReportViewer/show?id=" + data.reportResultId
            );
        } else {
          container
            .parent()
            .find(".titleLink")
            .attr("href", "/reports/report/show?id=" + data.reportResultId);
        }
        container
          .parent()
          .find(".exportUrl")
          .attr(
            "data-content",
            location.protocol +
              "//" +
              location.hostname +
              (location.port ? ":" + location.port : "") +
              "/reports/advancedReportViewer/viewWidget/" +
              data.reportWidgetId +
              "?frame=true"
          );
        container
          .parent()
          .find(".export-type-list")
          .html(exportLinksList(data.reportResultId));
        container.find(".highcharts-container").show();
        container.data("widgetTitle", encodeToHTML(data.title));

        $.ajax({
          url:
            "/reports/advancedReportViewer/view?id=" +
            data.reportResultId +
            "&widget=true" +
            "&index=" +
            container.data("index") +
            "&hideTable=" +
            data.hideTable +
            "&displayLength=" +
            data.displayLength +
            "&showIndicators=" +
            data.showIndicators,
          dataType: "html",
        }).done(function (result) {
          container.empty().append(result);
          container.append(
            "<div style='width: 100%;text-align: right;' class='chartRunDate'>" +
              $.i18n._("app.dashboard.generated.label") +
              " " +
              data.runDate +
              "</div>"
          );
        });
      });
  }

  function exportLinksList(reportId) {
    var html =
      "<li><a href='/reports/report/show?id=" +
      reportId +
      "&outputFormat=PDF&paperReport=false' ><i class='md md-file-pdf'></i>" +
      $.i18n._("saveas.pdf") +
      "</a></li>";
    html +=
      "<li><a href='/reports/report/show?id=" +
      reportId +
      "&outputFormat=XLSX&paperReport=false' ><i class='md md-file-excel'></i>" +
      $.i18n._("saveas.excel") +
      "</a></li>";
    html +=
      "<li><a href='/reports/report/show?id=" +
      reportId +
      "&outputFormat=DOCX&paperReport=false' ><i class='md md-file-pdf'></i>" +
      $.i18n._("saveas.word") +
      "</a></li>";
    html +=
      "<li><a href='/reports/report/show?id=" +
      reportId +
      "&outputFormat=PPTX&paperReport=false' ><i class='md md-file-pdf'></i>" +
      $.i18n._("saveas.powerpoint") +
      "</a></li>";
    return html;
  }

  $(document).on("click", ".dashboardSummaryContent", function () {
    var url;
    if ($(this).hasClass("reportRequestSummary")) url = reportRequestIndexPage;
    if ($(this).hasClass("actionItemSummary")) url = actonItemIndexPage;
    if ($(this).hasClass("aggregateSummary")) url = aggregateExecutedIndexPage;
    if ($(this).hasClass("adhocSummary")) url = adhocExecutedIndexPage;
    document.location.href = url;
  });

  $(document).on("click", ".editTitle", function () {
    $(".page-title").focus();
  });

  $(document).on("blur", ".page-title", function () {
    if (isEditable) {
      $.ajax({
        type: "GET",
        url:
          updateLabelUrl +
          "?id=" +
          $("#dashboardId").val() +
          "&label=" +
          encodeURIComponent($(".page-title").text()),
        dataType: "json",
      })
        .fail(function (err) {
          console.log("Error on " + CONFIGURATION.getChartDataUrl);
          console.log(err);
          $(".errorContent").text(err.responseJSON.message);
          $(".errorDiv").show();
          setTimeout(function () {
            $(".errorDiv").hide();
          }, 2000);
          window.location.reload();
        })
        .done(function (data) {
          $(".successContent").text($.i18n._("dashboard.label.rename.success"));
          $(".successDiv").show();
          setTimeout(function () {
            $(".successDiv").hide();
          }, 2000);
          window.location.reload();
        });
    }
  });

  $(document).on("keydown", ".page-title", function (event) {
    if (event.keyCode == 13) {
      event.preventDefault();
      $(".page-title").blur();
    }
  });

  $(document).on("click", ".submitSaveRefreshModal", function (event) {
    submitRefreshModal(CHART_WIDGET_ACTIONS.SAVE);
  });

  $(document).on("click", ".submitRunRefreshModal", function (event) {
    submitRefreshModal(CHART_WIDGET_ACTIONS.RUN);
  });

  $(document).on("click", ".widgetCheckbox", function (event) {
    var checkboxElement = $(this);
    if (checkboxElement.is(":checked")) {
      selectedWidgets.push(checkboxElement.data("widgetId"));
    } else {
      for (var iter = 0; iter < selectedWidgets.length; iter++) {
        if (selectedWidgets[iter] === checkboxElement.data("widgetId")) {
          selectedWidgets.splice(iter, 1);
          break;
        }
      }
    }
    console.log(selectedWidgets);
  });
});

function submitRefreshModal(action, id) {
  var container = $("#" + $("#selectedWidget").val());
  var data = $("#chartWidgetRefreshForm").serialize() + "&act=" + action;
  if (id) data = "&act=" + action + "&id=" + id;
  $.ajax({
    url: updateSectionAndRunAjaxUrl,
    method: "POST",
    data: data,
    dataType: "json",
  })
    .done(function (response) {
      if (action !== CHART_WIDGET_ACTIONS.SAVE) {
        container.parent().find(".chart-running").show();
        container.parent().find(".refresh-widget").hide();
        container.parent().find(".edit-widget").hide();
        container.parent().find("#runningMessage").show();
      }
    })
    .fail(function (err) {
      console.log(err.responseJSON.stackTrace);
      $("#warningModal .description").text($.i18n._("schedule.warning.msg"));
      $("#warningModal").modal("show");
    });
}

$(function () {
  //initSelect2ForEmailUsers("#emailUsers");
  $(document).on("click", ".excelWidgetExport", function (e) {
    exportFunc("XLSX");
  });

  $(document).on("click", ".pdfWidgetExport", function (e) {
    exportFunc("PDF");
  });

  $(document).on("click", ".docxWidgetExport", function (e) {
    exportFunc("DOCX");
  });

  $(document).on("click", ".pptxWidgetExport", function (e) {
    exportFunc("PPTX");
  });
});

function exportFunc(outputFormat) {
  var data = createParametersForExport(outputFormat);
  $("#data").val(JSON.stringify(data));
  $("#exportFormId").trigger("submit");
}

function createParametersForExport(outputFormat) {
  var data = {};
  data["outputFormat"] = outputFormat;
  data["selectedWidgets"] = selectedWidgets;
  return data;
}

function copyToClipboard(selector) {
  let inputElement = $(selector);
  if (inputElement.length) {
    inputElement.select();
    navigator.clipboard.writeText(inputElement.val()).catch(err => {
      console.error("Failed to copy to clipboard:", err);
    });
  }
}