$(function () {
  var id = reportId;
  showDataTableLoader($(".reportDiv"));
  if (interactiveView && reportResultId) {
    $("#interactiveReportDiv").load("/reports/advancedReportViewer/view?id=" + reportResultId + "&widget=true&showpage=true", function () {
      hideDataTableLoader($(".reportDiv"));
    });
  } else {
    $.ajax({
      url:
        "/reports/report/getReportAsHTMLString?isInDraftMode=" + isInDraftMode,
      data: "filename=" + encodeURIComponent(filename) + "&id=" + id,
      dataType: "html",
    })
      .done(function (result) {
        $("#reportDiv").empty().append(result);
        $(".jrPage tr").css("height", "");
        $(".column-title").attr("title", function () {
          return $("#" + this.id + "Legend").val();
        });
        if (latestComment !== null && latestComment.length > 0) {
          ensureHighchartsRendered(displayAnnotation);
        }

        setTimeout(function () {
          if($(".highcharts-container").length>0) {
            initZoom($(".highcharts-container").parent(), staticHtmlChartOptions, latestComment);
          }
        }, 1)

        setGridHeight();
      })
      .always(function () {
        hideDataTableLoader($("#reportDiv"));
      })
      .fail(function (xhr, ajaxOptions, thrownError) {
        $("#reportDiv")
          .empty()
          .append(
            "Error occurred while loading html output. Please contact administartor"
          );
      });
  }
});

$(window).resize(function () {
  if (!interactiveView) setGridHeight();
});

function setGridHeight() {
  if (!($("#reportDiv").length > 0)) {
    return;
  }
  var screenHeight = parseInt($(window).height());
  var gridHeight = screenHeight - parseInt($("#reportDiv").offset().top);
  $("#reportDiv").css({ "max-height": gridHeight + "px" });
  $("html").addClass("overflow-y-hidden");
  var repoPS = new PerfectScrollbar("#reportDiv");
  $("#reportDiv").scroll();
  var paginatorWidth = 0;
  $("#reportDiv").on("ps-scroll-x", function () {
    var $w = $(this);
    var $paginator = $(".dataTables_paginate").parent().parent();
    if (!paginatorWidth) {
      paginatorWidth = $paginator.width() + 1;
      $paginator.css({
        position: "absolute",
        width: "" + paginatorWidth + "px",
      });
    }
    $paginator.css("left", $w.scrollLeft());
  });
}

$(window).on("resize", function () {
  setGridHeight();
});

function ensureHighchartsRendered(callback) {
  var checkInterval = setInterval(function () {
    if ($(".highcharts-container").length > 0) {
      clearInterval(checkInterval);
      callback();
    }
  }, 100); // Check every 100 milliseconds
}

function displayAnnotation() {
  var chartObj = $(".highcharts-container").parent().highcharts();
  if (
    typeof latestComment != "undefined" &&
    latestComment != "" &&
    typeof chartObj != "undefined"
  ) {
    var annotationXValue = 0.2 * chartObj.chartWidth;
    var annotationYValue = 0.5 * chartObj.chartHeight;
    var labelsObj = {};
    var point = {};
    point.x = annotationXValue;
    point.y = annotationYValue;
    labelsObj.text = latestComment;
    labelsObj.point = point;
    var styleObj = {};
    styleObj.width = 200;
    labelsObj.style = styleObj;
    var labels = [];
    labels.push(labelsObj);
    var labelOptions = {};
    labelOptions.backgroundColor = "rgba(236,236,236,1)";
    labelOptions.borderWidth = 0;
    labelOptions.borderRadius = 40;
    var annotationObj = {};
    annotationObj.labelOptions = labelOptions;
    annotationObj.id = "current";
    annotationObj.labels = labels;
    annotationObj.draggable = "xy";
    chartObj.addAnnotation(annotationObj);
  }
}

 $("[data-evt-change]").on('change', function() {
      const eventData = JSON.parse($(this).attr("data-evt-change"));
      const methodName = eventData.method;
      const params = eventData.params;
      // Call the method from the eventHandlers object with the params
      if (methodName == 'showHideCaseNumberColumn') {
          showHideCaseNumberColumn();
          return;
      }
    });

function showHideCaseNumberColumn() {
    let currentUrl = new URL(window.location.href);
    if (document.getElementById('includeCaseNumber').checked) {
        currentUrl.searchParams.set('includeCaseNumber', 'true');
    } else {
        currentUrl.searchParams.set('includeCaseNumber', 'false');
    }
    window.location.href = currentUrl.toString();
  }
