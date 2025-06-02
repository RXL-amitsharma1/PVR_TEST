let ecvapt = (function () {
    let percentage = function (evt) {
        return Math.abs(evt.value) + "%";
    };

    let seriesCategory = function (event) {
        let yValue = (this && this.point && typeof this.point.y !== "undefined") ? this.point.y : 0;
        return "<b>" + ((event && event.series && event.series.name) ? event.series.name : "Unknown") +
            ", age " + ((event && event.series && event.series.category) ? event.series.category : "Unknown") + "</b><br/>" +
            "Population: " + (typeof Highcharts !== "undefined" && Highcharts.numberFormat
                ? Highcharts.numberFormat(Math.abs(yValue), 0)
                : yValue);
    };

    let colorOpacity = function (colorIndex) {
        return (typeof Highcharts !== "undefined" && Highcharts.Color)
            ? Highcharts.Color(Highcharts.getOptions().colors[colorIndex]).setOpacity(0.3).get()
            : "#CCCCCC"; // Fallback color
    };

    let color = function (colorIndex) {
        return (typeof Highcharts !== "undefined" && Highcharts.getOptions)
            ? Highcharts.getOptions().colors[colorIndex]
            : "#000000"; // Fallback color
    };

    let backgroundColor = function () {
        return (typeof Highcharts !== "undefined" && Highcharts.theme && Highcharts.theme.legendBackgroundColor)
            ? Highcharts.theme.legendBackgroundColor
            : "#FFFFFF";
    };

    let textColor = function () {
        return (typeof Highcharts !== "undefined" && Highcharts.theme && Highcharts.theme.textColor)
            ? Highcharts.theme.textColor
            : "black";
    };

    let renderPath = function () {
        if (!this || !this.renderer || !this.series || !this.series[2]) return;

        this.renderer.path(["M", -8, 0, "L", 8, 0, "M", 0, -8, "L", 8, 0, 0, 8])
            .attr({
                "stroke": "#303030",
                "stroke-linecap": "round",
                "stroke-linejoin": "round",
                "stroke-width": 2,
                "zIndex": 10
            })
            .translate(190, 26)
            .add(this.series[2].group);

        this.renderer.path([
            "M", -8, 0, "L", 8, 0,
            "M", 0, -8, "L", 8, 0, 0, 8,
            "M", 8, -8, "L", 16, 0, 8, 8
        ])
            .attr({
                "stroke": "#303030",
                "stroke-linecap": "round",
                "stroke-linejoin": "round",
                "stroke-width": 2,
                "zIndex": 10
            })
            .translate(190, 61)
            .add(this.series[2].group);

        this.renderer.path([
            "M", 0, 8, "L", 0, -8,
            "M", -8, 0, "L", 0, -8, 8, 0
        ])
            .attr({
                "stroke": "#303030",
                "stroke-linecap": "round",
                "stroke-linejoin": "round",
                "stroke-width": 2,
                "zIndex": 10
            })
            .translate(190, 96)
            .add(this.series[2].group);
    };

    let labelWidthHeight = function (labelWidth, labelHeight) {
        return {x: 200 - (labelWidth / 2 || 0), y: 180};
    };

    return {
        percentage: percentage,
        seriesCategory: seriesCategory,
        colorOpacity: colorOpacity,
        color: color,
        backgroundColor: backgroundColor,
        textColor: textColor,
        renderPath: renderPath,
        labelWidthHeight: labelWidthHeight
    };
})();

let fetchKeyForFunction = function (value, event) {
    if (typeof value !== "string") return null;

    if (value.includes('this.series.name')) {
        return ecvapt.seriesCategory(event);
    } else if (value.includes('Math.abs')) {
        return ecvapt.percentage(event);
    } else if (value.includes('Highcharts.Color')) {
        let index = parseInt(value.substring(66, 67), 10) || 0;
        return ecvapt.colorOpacity(index);
    } else if (value.includes('Highcharts.getOptions')) {
        let index = parseInt(value.substring(49, 50), 10) || 0;
        return ecvapt.color(index);
    } else if (value.includes('Highcharts.theme.legendBackgroundColor')) {
        return ecvapt.backgroundColor();
    } else if (value.includes('Highcharts.theme.textColor')) {
        return ecvapt.textColor();
    } else if (value.includes('this.renderer.path')) {
        return ecvapt.renderPath();
    } else if (value.includes('200 - labelWidth')) {
        return ecvapt.labelWidthHeight();
    }
};