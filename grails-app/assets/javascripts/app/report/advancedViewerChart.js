function loadChart(getChartDataUrl, index, resize) {
  $.ajax({
    type: "GET",
    url: getChartDataUrl
  })
    .fail(function (err) {
      console.log("Error on " + getChartDataUrl);
      console.log(err);
    })
    .done(function (_data) {
      _inx = index ? index : 0
      $scrpt = '<script>' + 'chartOptions[' + _inx + "] = " + _data + ';</script>';
      $("body").append($scrpt);
      let data = chartOptions[_inx];
      var chart = $("#chart" + index);
      var container = $("#chart" + index).closest(".chart-container");
      if ((typeof fullPage != "undefined" && fullPage) || data.type == "Map") {
        if (data.type == "Map")
          chart.highcharts("Map", data.options);
        else
          chart.highcharts(data.options);
        if (data.latestComment != null && data.latestComment.length > 0) {
          addAnnotation(data.latestComment, chart.highcharts());
        }
        chart.closest(".grid-stack-item").trigger("resize");
        if (typeof fullPage != "undefined" && fullPage) initZoom(chart, data.options, data.latestComment)
        return;
      }
      var iterCount = 0;
      var countPoints = function (categories) {
        if (categories.length > 0) {
          for (var i = 0; i < categories.length; i++) {
            if (categories[i].categories) countPoints(categories[i].categories);
            else iterCount++;
          }
          return iterCount;
        }
        return 0;
      };
      var heigth = 500;
      var width = chart.innerWidth();
      if (container.length > 0) {
        heigth = container.innerHeight();
        width = container.innerWidth();
      }
      var chartOption = data.options.chart;
      var fraction = 30;
      if (chartOption.type == "column") {
        if (
          !data.options.plotOptions.series ||
          !data.options.plotOptions.series.stacking
        )
          for (var j = 1; j < data.options.series.length; j++) {
            if (!data.options.series[j].type) fraction = fraction + 20;
          }
      }
      var maxPoints = chartOption.inverted
        ? Math.ceil(heigth / fraction)
        : Math.ceil(width / fraction);
      if (
        _.find(
          [
            "line",
            "spline",
            "column",
            "bar",
            "area",
            "areaspline",
            "arearange",
            "areasplinerange",
            "columnrange",
            "scatter",
          ],
          function (it) {
            return it == data.options.chart.type;
          }
        )
      ) {
        if (data.options.xAxis[0].categories) {
          var pointsNumber = countPoints(data.options.xAxis[0].categories, 0);
          if (maxPoints <= pointsNumber) {
            data.options.scrollbar = { enabled: true };
            for (var i = 0; i < data.options.xAxis.length; i++) {
              data.options.xAxis[i].min = 0;
              data.options.xAxis[i].max = maxPoints;
            }
          }
        }
      }
      chart.highcharts(data.options);
      if (data.latestComment != null && data.latestComment.length > 0) {
        addAnnotation(data.latestComment, chart.highcharts());
      }
      chart.closest(".grid-stack-item").trigger("resize");
    });
}

function initZoom(chart, chartOptions, latestComment) {
  createChartZoom(chart);
  $(document).on("click", ".h-zoomIn, .h-zoomOut,.w-zoomIn, .w-zoomOut", function () {
        var chartContainer = $(".highcharts-container"),
            chart = chartContainer.parent();
        if (chart.length && chart.highcharts()) {
          var h = 0,
              w = 0;
          if ($(this).hasClass("h-zoomIn")) h = 70;
          if ($(this).hasClass("h-zoomOut")) h = -70;

          if ($(this).hasClass("w-zoomIn")) w = 70;
          if ($(this).hasClass("w-zoomOut")) w = -70;
          chart.height(chart.height() + h);
          chart.width(chart.width() + w);
          chart.highcharts().destroy();
          if (chartOptions.chart.map == "custom/world") {
            chart.highcharts("Map", chartOptions);
          } else {
            chart.highcharts(chartOptions);
          }
          if (latestComment != null && latestComment.length > 0) {
            addAnnotation(latestComment, chart.highcharts());
          }
          createChartZoom(chart);

        }
      }
  );
}

function createChartZoom(chart) {
  chart.parent().css("overflow", "auto");
  if (chart.length > 0) {
    let h = chart.height() - 30;
    chart.append(
        "<i class='h-zoomIn glyphicon glyphicon-zoom-in' title='Zoom In' style='padding: 0;padding-left: 5px;padding-right: 4px;font-size:18px;position: absolute; top: 10px;left:0'></i>"
    );
    chart.append(
        "<div class=' glyphicon glyphicon-resize-vertical'  style='position: absolute;font-size:18px; top: 32px;left:5px'></div>"
    );
    chart.append(
        "<i class='h-zoomOut glyphicon glyphicon-zoom-out' title='Zoom Out' style='padding: 0;padding-left: 4px;padding-right: 5px;font-size:18px; position: absolute; top: 55px;left:0'></i>"
    );
    chart.append(
        "<i class=' w-zoomIn glyphicon glyphicon-zoom-in' title='Zoom In' style='padding: 0;padding-left: 5px;padding-right: 4px;font-size:18px;position: absolute; top: " + h + "px;left:0'></i>"
    );
    chart.append(
        "<span  style='padding: 0;left: 25px;padding-right: 4px;font-size:18px;position: absolute; top: " + (h - 5) + "px;'><i class=' glyphicon glyphicon-resize-horizontal' ></i></span>"
    );
    chart.append(
        "<i class=' w-zoomOut glyphicon glyphicon-zoom-out' title='Zoom Out' style='padding: 0;left: 45px;padding-right: 4px;font-size:18px;position: absolute; top: " + h + "px;'></i>"
    );
  }
}

function resizeToFrame(index) {
  var chart = $("#chart" + index);
  chart.highcharts(data.options);
  var heigth = $("#tableDiv" + index).is(":hidden")
    ? window.innerHeight
    : Math.round(window.innerHeight * 0.6);
  if (chart.highcharts())
    chart.highcharts().setSize(window.innerWidth - 25, heigth);
}

function addAnnotation(latestComment, highcharts) {
  var chartObj = highcharts;
  var annotationXValue = 0.9 * chartObj.chartWidth;
  var annotationYValue = 0.1 * chartObj.chartHeight;
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

function createIndicator(label, value, indicator) {
  $(indicator).append(
    '<div class="widget-simple-chart text-center card-box pv-inner-shadow-box">' +
      '<h5 class="text-muted">' +
      label +
      "</h5>" +
      '<h1 class="text-blue p-t-0" >' +
      value +
      "</h1></div>"
  );
}
