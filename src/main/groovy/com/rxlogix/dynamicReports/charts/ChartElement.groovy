package com.rxlogix.dynamicReports.charts

/**
 * Created by gologuzov on 30.11.15.
 */
class ChartElement implements ChartGenerator {
    def options
    private def series = [:]
    boolean showPercentages
    String yAxisPercentageTitle
    String drillDownUrlTemplate
    String latestComment
    Boolean isMap = false

    ChartElement(options, String latestComment) {
        this.options = options
        this.latestComment = latestComment
    }

    void setTitle(String title) {
        if (!options.title) {
            options.title = [:]
        }
        options.title.text = title
    }

    void setOnClick(String clickFunction){
        if (!options.plotOptions) { options.plotOptions = [:]}
        if (!options.plotOptions.series) {options.plotOptions.series = [:]}
        if (!options.plotOptions.series.point) {options.plotOptions.series.point = [:]}
        if (!options.plotOptions.series.point.events) {options.plotOptions.series.point.events = [:]}
        options.plotOptions.series.point.events.click = clickFunction
        if (!options.chart) {options.chart = [:]}
        if (!options.chart.events) { options.chart.events = [:]}
        options.chart.events.click = clickFunction
        if (options.chart.type == "pie") {
            options.plotOptions.series.allowPointSelect = true
            options.plotOptions.series.marker = [states: [hover: [enabled: false], select: [enabled: true, radius: 10]]]
        } else {
            options.plotOptions.series.allowPointSelect = false
        }
    }

    void addSerie(String name, String type = null, Boolean isPercentageColumn = false, String label = null) {
        def serie = [name: name, isPercentageColumn: isPercentageColumn, data: []]
        String lbl=(label==null?name:label.trim())
        if (type) {
            if (isPercentageColumn) {
                serie.type = type
                serie.zIndex = 10
                serie.yAxis = 1
            } else {
                serie.type = type
            }
        }
        if (!options.series) {
            options.series = []
        }
        options.series.add(serie)
        series[name] = serie
    }

    void setShowLegend(Boolean showLegend) {
        options.plotOptions?.series?.showInLegend = showLegend
    }

    void addValue(String serieName, List<String> labels, Number value, String columnName, Object rowId, String cell = null) {
        def categories = options.xAxis[0].categories
        if (!categories) {
            categories = options.xAxis[0].categories = []
        }

        buildCategoriesTree(categories, labels)
        def lastLabel = []
        if(labels && labels.size() > 0){
            lastLabel = labels?.last()
        }
        def item = [name: lastLabel, y: value, showPercentages: series[serieName].isPercentageColumn, cell: cell, rowId: rowId]
        if (drillDownUrlTemplate != null && columnName != null && rowId != null) {
            item.href = "${drillDownUrlTemplate}?columnName=${columnName}&rowId=${rowId}&count=${value}"
        }
        series[serieName].data.add(item)
    }

    private void buildCategoriesTree(def categories, List<String> labels) {
        if(labels && labels.size() > 0) {
            def label = labels.first()
            def child = categories.find {
                (it instanceof String) ? label.equals(it) : label.equals(it.name)
            }
            if (!child) {
                if (labels && labels.size() > 1) {
                    child = [name: label, categories: []]
                } else {
                    child = label
                }
                categories.add(child)
            }
            if (labels.size() > 1) {
                buildCategoriesTree(child?.categories, labels.tail())
            }
        }
    }

    void setYAxisTitle(String title) {
        if (!options.yAxis[0]) {
            options.yAxis[0] = [:]
        }
        if (!options.yAxis[0].title) {
            options.yAxis[0].title = [:]
        }
        options.yAxis[0].title.text = title;
    }

    void setYAxisPercentageTitle(String title) {
        if (title) {
            if (options.yAxis.size() < 2) {
                if (!options.yAxis[0]) {
                    options.yAxis[0] = [:]
                }
                options.yAxis[1] = addPercentageAxy(title)
            } else {
                if (!options.yAxis[1].title)
                    options.yAxis[1] = addPercentageAxy(title)
            }
        }
    }

    void setReversedStacks(Boolean value) {
        if (!options.yAxis[0]) {
            options.yAxis[0] = [:]
        }
        options.yAxis[0].reversedStacks = value
    }

    Map addPercentageAxy(String name) {
        return [
                title   : [text: name],
                labels  : [format: '{value} %'],
                opposite: true,
                min     : 0,

        ]
    }

    @Override
    def generateChart(Boolean forPdf) {
        if(isMap) return options
        options.plotOptions.series.dataLabels.formatter = "function () {\n" +
                "        var label = this.point.y;\n" +
                "        if (this.point.showPercentages) {\n" +
                "            label = ''+ this.point.y.toFixed(2) + '%';\n" +
                "        }" +
                "        if (this.point.href) { \n" +
                "            label = '<a href=\"' + this.point.href + '\">' + label + '</a>';\n" +
                "        }\n" +
                "        return label;" +
                "    }";

        if (!(options.chart.type in ["pie", "scatter"])) {
            options.xAxis?.each {
                it.crosshair = true
            }
            options.yAxis?.each {
                it.crosshair = true
            }
            if (!options.tooltip) options.tooltip = [:]
            options.tooltip.formatter = """  function () {
                return this.points.reduce(function (s, point) {
                    return s + '<br/>' + point.series.name + ': ' +  
                    (point.point.showPercentages? (point.y.toFixed(2) + '%'): point.y );
                        }, '<b>' + this.x + '</b>');
                }"""
            options.tooltip.shared = true

            if (options.xAxis[0].categories && (options.xAxis[0].categories[0] instanceof Map) && options.xAxis[0]?.categories[0]?.categories) {
                options.xAxis?.each {
                    it.labels = [rotation: options.chart.inverted ? 0 : 90]
                }

                //autorotation is not working correctly, below code defines if we need rotate label or not
            } else if (forPdf && !options.chart.inverted && options.xAxis[0].categories instanceof List) {
                int maxLettersForPdf = 165
                int maxForElement = maxLettersForPdf / options.xAxis[0].categories.size()
                if (options.xAxis[0].categories.find { it.toString().length() > maxForElement }) {
                    options.xAxis?.each {
                        it.labels = [rotation: -30]
                    }
                }
            }
        }
        return options
    }

    @Override
    String getLatestComment(){
        return latestComment
    }
}
