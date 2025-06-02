$(function () {

    var setting = {}

    $(document).on("change", "#showChartSheet", function () {
        if ($(this).is(":checked")) {
            $(".specialChartSettingsDiv").show();
        } else {
            $(".specialChartSettingsDiv").hide();
        }
    });
    $(document).on("change", ".valuesChartType, .chartColumnLabel", function () {
        updateSettings();
    });
    $(document).on("change keyup paste", ".sqlBox", function () {
        var sql = $(this).val();
        var start = sql.indexOf("CHART_COLUMN_");
        var newList = [];
        while (start > -1) {
            var end = sql.indexOf(" ", start);
            if (sql.charAt(start - 1) == '"') end = sql.indexOf('"', start);
            var name = sql.substring(start, end);
            addVariable(name);
            newList.push(name);
            start = sql.indexOf("CHART_COLUMN_", start + 1);
        }
        var variables = _.keys(setting)
        for (var i = 0; i < variables.length; i++) {
            if (_.indexOf(newList, variables[i]) == -1) removeVariable(variables[i]);
        }
        updateSettings();
    });

    function removeVariable(name) {
        $(".oneSpecialSettings[variableName='" + name + "']").remove();
    }

    function showVariable(name, type, axisLabel) {
        var label = getLabel(name);
        var $clone = $(".specialSettingsToClone").clone();
        $clone.removeClass("specialSettingsToClone").addClass("oneSpecialSettings").attr("variableName", name);
        $clone.find(".chartColumnName").text(label);
        $clone.find(".chartColumnLabel").val(axisLabel);
        if (type) $clone.find(".valuesChartType").val(type);
        $(".specialChartSettingsDiv").append($clone).show();
        $clone.show();
    }

    function addVariable(name) {
        if (!name) return
        if (setting[name]) return
        showVariable(name, "", getLabel(name))
    }

    function getLabel(name) {
        return (name.indexOf("CHART_COLUMN_P_") > -1 ? name.substring(15) : name.substring(13));
    }

    function loadSettings() {
        if ($("#specialChartSettings").val())
            setting = JSON.parse($("#specialChartSettings").val());
        else
            setting = {}
        var variables = _.keys(setting)
        for (var i = 0; i < variables.length; i++) {
            showVariable(variables[i], setting[variables[i]].type, setting[variables[i]].label);
        }
    }

    function updateSettings() {
        setting = {}
        $(".oneSpecialSettings").each(function () {
            $this = $(this)
            setting[$this.attr("variableName")] = {
                type: $this.find(".valuesChartType").val(),
                label: $this.find(".chartColumnLabel").val()
            }
        });
        $("#specialChartSettings").val(JSON.stringify(setting));
    }

    loadSettings();
});