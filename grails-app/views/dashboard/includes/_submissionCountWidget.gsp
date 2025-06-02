<%@ page import="com.rxlogix.enums.EtlStatusEnum" %>
<div class="container-fluid grid-stack-item-content rx-widget">
    <div class="row rxmain-container-header rx-widget-header">
        <g:link controller="quality" action="submissionQuality"
                class="rxmain-container-header-label rx-widget-title"><g:message code="default.button.addSubmissionCountByErrorWidget.label"/></g:link>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>
    <div class="row rx-widget-content" >
        <div class="quality-chart-container" id="qualityChart${index}" style="width: 100%;height: 100%">
        </div>
    </div>
</div>
<script>
    $(function () {
        var loadData = function () {
            $.ajax({
                "url": qualitySubmissionCountUrl,
                dataType: 'json'
            }).done(function (data) {
                console.log(data);
                var errorNameList = data.errorNames;
                var errorTotalCountList = data.caseCountValues;
                var errorTotalCountJson = [];
                for (var i = 0; i < errorTotalCountList.length; i++) {
                    errorTotalCountJson.push({
                        y: parseInt(errorTotalCountList[i]),
                        color: '#' + Math.random().toString(16).substr(2, 6)
                    });
                }


                var chart = new Highcharts.Chart({
                    chart: {
                        renderTo: 'qualityChart${index}',
                        type: 'column',
                        plotBorderWidth: 1,
                        zoomType: 'y'
                    },
                    exporting: {
                        enabled: true
                    },
                    credits: {
                        enabled: false
                    },
                    title: {
                        text: '',
                        align: 'left'
                    },
                    subtitle: {
                        text: ''
                    },
                    xAxis: {
                        categories: errorNameList,
                        tickLength: 0
                    },
                    yAxis: {
                        min: 0,
                        title: {
                            text: '${message(code: "chart.label.yAxis.caseCount")}'
                        },
                        lineWidth: 1
                    },
                    plotOptions: {
                        bar: {
                            stacking: 'normal',
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: true,
                                formatter: function () {
                                    return this.y;
                                }
                            }
                        }
                    },
                    tooltip: {
                        borderRadius: 10,
                        borderWidth: 2,
                        shadow: true,
                        formatter: function () {
                            var s = [];
                            var current = this.point.index;
                            $.each(this.series.chart.series, function (i, series) {
                                s.push('<span style="fill:' + series.color + ';font-weight:bold;">' + series.name + ' </span><span style="fill: rgb(0, 0, 0); font-weight: bold;"/>: ' +
                                    series.processedYData[current] + '<span>');
                            });
                            return '<span style="font-weight: bold;">' + this.x + '</span>' + '<br/>' + s.join('<br/>')
                        }
                    },
                    series: [{
                        showInLegend: false,
                        name: 'Case Count',
                        color: '#333333',
                        data: errorTotalCountJson
                    }]
                });
                var chartContainer=$("#qualityChart${index}")
                chartContainer.highcharts().setSize(chartContainer.innerWidth(), chartContainer.innerHeight()-25);
            });
        };
        $('#refresh-widget${index}').hide();
        loadData();
    });
</script>