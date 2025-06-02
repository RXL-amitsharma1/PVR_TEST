var CUSTOM_PERIOD_COUNT = "CUSTOM_PERIOD_COUNT";
var LAST_TYPE_TOKEN = "LAST_";
var NEXT_TYPE_TOKEN = "NEXT_";
var CASE_LIST = "CASE_LIST";

var viewOnly = ($('#editable').val() === 'false');
var measureList = [];
var numColMeas;
var inputMeasureNameDefaultValue;

var measureIndexList = [];

$(function () {
    var templateType = $('#templateType').val();

    if (templateType == 'DATA_TAB') {
        var templateId = $('#templateId').val();
        var measureSelected;

        // initialize all select2 for measures
        for (var index = 0; index < numColMeas; index++) {
            initMeasureSelect2(index);
        }

        // For edit and view page
        if ($("#JSONMeasures").val()) {
            var measureString = $("#JSONMeasures").val();
            if (measureString) {
                measureList = JSON.parse(measureString);
                // For each measure set
                $.each(measureList, function (colMesaIndex, measures) {
                    var measuresContainer = $(".measuresContainer")[colMesaIndex];
                    var maxIndex = 0;
                    $.each(measures, function (measIndex) {
                        var measureDiv = addMeasureDiv(this, measuresContainer, measIndex);
                        // initialize select2 and date pickers
                        initDateRangeCountSelect2('colMeas' + colMesaIndex + '-meas' + measIndex);
                        initDatePickers('colMeas' + colMesaIndex + '-meas' + measIndex);
                        initSortIcons(measureDiv, colMesaIndex, measIndex);
                        bindSelect2WithUrl($('#colMeas' + colMesaIndex + '-meas' + measIndex+"-drillDown"), specificCllTemplateSearchUrl, templateNameUrl, true);
                        maxIndex = measIndex;
                    });
                    measureIndexList.push(maxIndex);
                });
                checkMeasureSorting();
            }
        }

        function getMeasureSequence(measure) {
            var measuresNodeList = $($(document).find(".measuresContainer")[0]).find(".measureName");
            var measures = Array.prototype.slice.call(measuresNodeList);
            return measures.indexOf(measure);
        }

        function showMeasureOptions(open, measureOptions) {
            if (open) {
                $($(document).find('.columnRenameArea')[0]).hide();
                $($(document).find('.measureOptions')[0]).slideDown();
            } else {
                $(measureOptions).hide();
            }
        }

        $(document).on('click', '.removeMeasure', function () {
            var measure = this.parentElement;
            var measureName = $(measure).find(".measureName")[0];
            var colMeasIndex = $(this).closest('.columnMeasureSet').attr('sequence');
            var measureIndex = $(this).closest('.validMeasure').attr('sequence');
            if (measureName.classList.contains("columnSelected")) {
                $('#colMeas' + colMeasIndex + '-meas' + measureIndex).remove();
            }
            console.log("[removeMeasure] colMeasIndex= " + colMeasIndex + " measureIndex= " + measureIndex);
            if (measureList[colMeasIndex] && measureList[colMeasIndex][measureIndex]) {
                measureList[colMeasIndex][measureIndex] = null;
            }
            $(measure).remove();
            updateColorCellConditionsAfterDelete();
        });

        $(document).on('click', '.validMeasure', function (e) {
            var container = getMeasureOptionsDiv(this);
            if ($(container).is(":hidden")) {
                inputMeasureNameDefaultValue = $(this).find(".measureName").text();
                showMeasureOptions(true);
                $('.measureOptions').hide();
                $(container).slideDown();
                $(container).find(".fieldSectionHeader").text( $(container).find(".inputMeasureName").val() + $.i18n._("settings"))
                $.each($(document).find('.columnSelected'), function () {
                    this.classList.remove('columnSelected');
                });
                this.classList.add("columnSelected");

                var countType = $(container).find('select')[0].value;
                if (countType === CUSTOM_PERIOD_COUNT) {
                    showDatePicker(true, container);
                    setDatePicker(container);
                } else {
                    showDatePicker(false, container);
                }
                if (countType.indexOf(LAST_TYPE_TOKEN) > -1) {
                    container.find(".measureRelativeDateRangeValue").parent().show();
                } else {
                    container.find(".measureRelativeDateRangeValue").parent().hide();
                }
                var drillDownSelect = container.find('.drilldownTemplate');
                if (countType !== "PERIOD_COUNT") {
                    drillDownSelect.val("").trigger('change');
                    drillDownSelect.attr("disabled", true);
                } else {
                    drillDownSelect.attr("disabled", false);
                }
                var id = $(container).attr('id');
                var drillDownValue=$("#"+id+ "-drillDown").val();
                if(drillDownValue!= '' && drillDownValue!= null) {
                    var isExecuted = $("#isExecuted").val();
                    if(isExecuted == "true"){
                        $('.templateViewButton').attr('href', executedtmpltViewUrl+'/'+drillDownValue);
                    }else{
                        $('.templateViewButton').attr('href', templateViewUrl+'/'+drillDownValue);
                    }
                    $('.templateViewButton').removeClass('hide');
                } else {
                    $('.templateViewButton').addClass('hide');
                    var isChecked = $("#drillDownToCaseList").prop("checked");
                    if (!viewOnly && isChecked) {
                        $(".drilldownTemplate").attr('disabled', true);
                        $(".drilldownTemplate").parent().removeClass('add-cursor');
                    }
                }

                if (viewOnly) {
                    $("input").prop("disabled", true);
                    $("button:not([data-dismiss=modal])").prop("disabled", true);
                    $("textarea").prop("disabled", true);
                    $("select").prop("disabled", true).attr("readonly", true);
                    $.each($(".percentageOption").find(".no-bold"), function () {
                        this.classList.remove("add-cursor");
                    });
                    $(".showColorConditionModal, .addParameter, .sectionRemove,.colorConditionUp,.colorConditionDown,.iconHelp,.removeOneCondition").addClass("hidden");
                }

                if ($(".viewButtons").attr('disabled')) {
                    $(".viewButtons").removeAttr("disabled", "disabled");
                }

                var measureName = $(container).find('input')[0].value;
                if (measureName.indexOf("COMPLIANCE_RATE") > -1) {
                    $("select[name=" + $(container)[0].id + "-percentageOption][value!='NO_PERCENTAGE']").attr('disabled', true);
                }

                if(measureName == CASE_LIST || viewOnly) {
                    $(".percentageOption select").attr("disabled",true)
                } else {
                    $(".percentageOption select").attr("disabled",false)
                }
                $('.percentageOption select').trigger("change")
            }
        });

        $(document).on('change', '.inputMeasureName', function () {
            var names = $(this).attr('name').split('-');
            var colMeasId = names[0];
            var measureSeq = names[1].slice(-1); // get the last character
            var measure = $('#' + colMeasId + '_template').find('.validMeasure[sequence=' + measureSeq + ']');
            if (!$(this).val().trim()) {
                $(this).val(inputMeasureNameDefaultValue);
            }
            $(measure).find('.measureName')[0].innerHTML = $(this).val();
            $(this).find(".fieldSectionHeader").text( $(this).val() + $.i18n._("settings"))
        });

        $(document).on('click', '.showCustomExpression_measure', function () {
            var container = $(this).closest('.measureOptions');
            var textArea = $(container).find('.customExpressionArea')[0];
            if (textArea.hasAttribute('hidden')) {
                $(textArea).removeAttr('hidden');
            } else {
                $(textArea).attr('hidden', 'hidden');
            }
        });

        $(document).on('click', '.closeMeasureOptions', function () {
            showMeasureOptions(false, $(this).closest('.measureOptions'));
            $(document).find('.columnSelected')[0].classList.remove('columnSelected');
            measureSelected = null;
        });

        $(document).on('click', 'input[type="submit"]', function () {
            var validIndex = [];
            $.each($('.columnMeasureSet'), function () {
                if ($(this).attr('sequence') != "") {
                    validIndex.push($(this).attr('sequence'));
                }
            });
            $('#validColMeasIndex').val(validIndex);
        });

        $(document).on('change', '.showTopX', function () {
            var names = $(this).attr('name').split('-');
            var colMeasId = names[0];
            var measIndex = names[1].slice(-1); // get the last character
            var measure = $('#' + colMeasId + '_template').find('.validMeasure[sequence=' + measIndex + ']');
            var measureSortIcon = $(measure).find(".measureSortIcon")[0];
            var optionId = 'colMeas' + colMeasId + '-meas' + measIndex;
            var measureSortField = $("#" + optionId).find(".measureSort");

            $(".showTopX").not($(this)).prop('checked', false);
            var topXCount = $("#" + colMeasId + "-meas" + measIndex + "-topXCount");
            $(".topXCount").not(topXCount).val("").prop('disabled', true);
            topXCount.prop('disabled', !this.checked);
            if (this.checked) {
                topXCount.val("10");
                if (measureSortIcon.classList.contains('sortDisabled')) {
                    clearSorting(measure);
                    removeSortIcon(measureSortIcon.classList);
                    addDescIcon(measureSortIcon.classList);
                    measureSortIcon.style.opacity = 1.0;
                    measureSortField.val(SORT_DESCENDING);
                }
            } else {
                topXCount.val("");
            }
        });
        $(document).on('change', '.showTopColumn', function () {
            var names = $(this).attr('name').split('-');
            var colMeasId = names[0];
            var measIndex = names[1].slice(-1); // get the last character
            var topColumnX = $("#" + colMeasId + "-meas" + measIndex + "-topColumnX");
            var topColumnType = $("#" + colMeasId + "-meas" + measIndex + "-topColumnType");
            topColumnX.prop('disabled', !this.checked);
            topColumnType.prop('disabled', !this.checked);
            if (this.checked) {
                topColumnX.val("10");
                $(".showTopColumn").not($(this)).prop('checked', false);
                $(".topColumnX").not(topColumnX).prop('disabled', true).val("");
                $(".topColumnType").not(topColumnType).prop('disabled', true);
            } else {
                topColumnX.val("");
            }
        });
        $(document).on('click', '.measureSortIcon', handleMeasureSortIconClick);
        $(document).on('change', '.percentageOption select', function(){
            var val = $(this).val();
            $(this).closest(".measureOptions").find(".percentageChartType").attr("disabled",val === "NO_PERCENTAGE");

            if ($(this).closest(".measureOptionsBorder").find(".measureType").val().indexOf("COMPLIANCE_RATE") > -1) {
                $(this).closest(".measureOptions").find(".overridePercentageAxisLabel").attr("disabled", false);
                $(this).closest(".measureOptions").find(".overrideValueAxisLabel").attr("disabled", true);
            } else {
                $(this).closest(".measureOptions").find(".overridePercentageAxisLabel").attr("disabled", val === "NO_PERCENTAGE");
                $(this).closest(".measureOptions").find(".overrideValueAxisLabel").attr("disabled", false);
            }
        });

        // Adding an observer to handle adding/removing rows
        var observer = new MutationObserver(function (mutations) {
            mutations.forEach(function (mutation) {
                checkMeasureSorting();
            });
        });
        observer.observe($("#rowsContainer")[0], {childList: true});
    }

    $(document).on('change', '.overridePercentageAxisLabel, .overrideValueAxisLabel', function () {
        $(this).closest(".valueAxisLabelDiv").find(".form-control").attr("disabled", !$(this).is(":checked"));
    });

    $(document).on("click", ".colorParameterTemplateRow", function () {
        var table = $(".colorParameterTable tbody")
        table.find(".colorParameterTemplateRow").removeClass("selectedRow");
        $(this).addClass("selectedRow");

    });

    $(document).on("click", ".colorConditionDown,.colorConditionUp", function () {
        var table = $(this).closest(".d-t-border").find(".colorParameterTable tbody")
        table.find(".orTr").detach();
        var row = table.find(".selectedRow");
        if ($(this).is(".colorConditionUp")) {
            row.insertBefore(row.prev());
        } else {
            row.insertAfter(row.next());
        }
        var rows = table.find(".colorParameterTemplateRow").each(function (index) {
            if (index > 0)
                $(this).before($(createOrRow()));
        });

        updateMeasureColorConditionJson(this);
    });

    $(document).on("click", ".sectionRemove", function (e) {
        var p = $(this).closest("table")
        var tr = $(this).closest("tr")
        var prev = tr.prev()
        if (prev.text() == "Else") {
            prev.detach()
        } else {
            var next = tr.next()
            if (next.text() == "Else") next.detach()
        }
        tr.detach();
        updateMeasureColorConditionJson(p);
    });

    $(document).on("click", ".removeOneCondition", function (e) {
        var conditioSpan = $(this).closest(".conditionAndAnd")
        var prev = conditioSpan.prev()
        if (prev.text() == "AND") {
            prev.detach()
        } else {
            var next = conditioSpan.next()
            if (next.text() == "AND") next.detach()
        }
        conditioSpan.detach();
        updateMeasureColorConditionJson(this);
    });

    $(document).on("change", ".colorPicker,.iconPicker", function (e) {
        updateMeasureColorConditionJson(this);
    });
    $(document).on("change", ".iconPicker", function (e) {
        updateMeasureColorConditionJson(this);
    });

    $(document).on("click", ".addParameter", function (e) {
        var clone = $($(".colorParameterTemplateRow")[0]).clone();
        clone.show();
        var tbody = $(this).closest(".colorConditionsCantainer").find("tbody");
        if (tbody.children().length > 0)
            tbody.append(createOrRow());
        tbody.append(clone);
        clone.find(".showColorConditionModal").trigger('click');
        clone.hide();
        var openCloseIcon = $(this).closest(".rxmain-container-header").find(".openCloseIcon");
        if (openCloseIcon.hasClass("fa-caret-right")) openCloseIcon.trigger('click');
    });

    $(document).on('click', '.iconHelp', function () {
        var modal = $("#helpIconModal");
        modal.modal("show");
    });

    $(document).on('click', '.colorTextStyle', function () {
        if ($(this).hasClass("enabled")) {
            $("#colorTextInput").trigger('click');
            addSelectedColorToText();
        }
    });

    $(document).on('click', '.colorBgStyle', function () {
        if ($(this).hasClass("enabled")) {
            $("#colorBgInput").trigger('click');
            addSelectedColorToBg();
        }
    });

    $(document).on('change', '.cellFormattingEdit', function () {
        var checkHTML = function (html) {
            var doc = document.createElement('div');
            doc.innerHTML = html;
            return (doc.innerHTML === html);
        }
        var val = $(".cellFormattingEdit").val()
        if (checkHTML(val)) {
            $(this).css("border", "0");
            $(".addColorCondition").attr("disabled", false);
            $("#cellFormattingModal").val(val);
            $(".errorWrongFormat").hide();
        } else {
            $(this).css("border", "1px solid red");
            $(".errorWrongFormat").show();
            $(".addColorCondition").attr("disabled", true);
        }
    });

    $(document).on('click', '.htmlStyle', function () {
        if(!$(".errorWrongFormat").is(":visible")) {
            switchHtmlEditor(!$(".cellFormattingEdit").is(":visible"));
        }
    });

    function switchHtmlEditor(turnHtmlOn) {
        if (turnHtmlOn) {
            $('.boldStyle,.underlineStyle,.italicStyle,.colorTextStyle').css("color", "#cccccc !important").addClass("disabled").removeClass("enabled")
            $(".sampleLabel").hide();
            $(".cellFormattingEdit").show().val($("#cellFormattingModal").val());
        } else {
            $(".cellFormattingEdit").hide();
            $(".sampleLabel").css("display", "table-cell");
            $('.boldStyle,.underlineStyle,.italicStyle,.colorTextStyle').css("color", "#000000").addClass("enabled").removeClass("disabled")
            updateSampleLabel();
        }
    }

    $(document).on('click', '.clearStyle', function () {
        $("#cellFormattingModal").val("$value");
        $("#cellColorModal").val("");
        $(".cellFormattingEdit").val("$value").trigger("change");
        updateSampleLabel();
    });

    $(document).on('change', '#colorTextInput', function () {
        addSelectedColorToText();
    });

    $(document).on('change', '#colorBgInput', function () {
        if ($(".colorBgStyle").hasClass("enabled")) {
            addSelectedColorToBg();
        }
    });

    $(document).on('click', '.boldStyle,.underlineStyle,.italicStyle', function () {
        if ($(this).hasClass("enabled")) {
            var val = $("#cellFormattingModal").val();
            if ($(this).hasClass("boldStyle")) val = addTag("b", val);
            if ($(this).hasClass("underlineStyle")) val = addTag("u", val);
            if ($(this).hasClass("italicStyle")) val = addTag("i", val);
            $("#cellFormattingModal").val(val);
            updateSampleLabel();
        }
    });

    function updateSampleLabel() {
        executeUpdateSampleLabel($("#cellFormattingModal").val(), $("#cellColorModal").val(), $(".sampleLabel"));
        executeUpdateSampleLabel($("#cellFormattingModal").val(), $("#cellColorModal").val(), $(".cellFormattingEdit"));
    }

    function addSelectedColorToText() {
        if ($(".colorTextStyle").hasClass("enabled")) {
            var val = $("#cellFormattingModal").val();
            var color = $("#colorTextInput").val();
            val = addTag("font color='" + color + "'", val);
            $("#cellFormattingModal").val(val);
            updateSampleLabel();
        }
    }

    function addSelectedColorToBg(){
        var bgColor = $("#colorBgInput").val()
        $("#cellColorModal").val(bgColor);
        updateSampleLabel();
    }

    function addTag(tag, val) {
        var result
        var tagname = tag.split(" ")[0];
        if (!val) {
            result = "<" + tag + ">$value</" + tagname + ">";
        } else {
            if (val.indexOf("<" + tagname) > -1) {
                if (tagname == "font") {
                    var re = new RegExp('<' + tagname + ' [^>]*>');
                    result = val.replace(re, "").replaceAll("</" + tagname + ">", "");
                    result = "<" + tag + ">" + result + "</" + tagname + ">";
                } else {
                    result = val.replaceAll("<" + tagname + ">", "").replaceAll("</" + tagname + ">", "");
                }
            } else {
                result = "<" + tag + ">" + val + "</" + tagname + ">";
            }
        }
        return result;
    }

    $(document).on('click', '.removeColorConditionModal', function () {
        $(this).closest(".row").detach();
    });

    $(document).on('click', '.addColorConditionModal', function () {
        cloneRow()
    });

    function cloneRow() {
        var clone = $(".firstColorConditionRowModal").clone();
        clone.removeClass("firstColorConditionRowModal");
        clone.find("i").removeClass("md-plus").removeClass("addColorConditionModal").addClass("md-close").addClass("removeColorConditionModal");
        clone.find(".colorConditionValueSelect").val("");
        $(".firstColorConditionRowModal").parent().append(clone);
        return clone;
    }

    $(document).on('click', '.showColorConditionModal', function () {
        var templateType = $('#templateType').val()
        var container = $(this).closest("tr").find(".conditionsSpan");
        if (templateType === "CASE_LINE") {
            createColorConditionsModalForCLL(container);
        } else if (templateType === "DATA_TAB") {
            createColorConditionsModalForDT(container);
        }
    });

    function createColorConditionsModalForCLL(container) {

        $(".colorConditionValueSelect").val("");
        $(".colorConditionOperatorSelect").val("");
        $(".cellFormattingEdit").show().val("");
        switchHtmlEditor(false);
        updateColorConditionSelectForCLL();
        $(".addColorCondition").off().one("click", function () {
            addColorClicked(container);
        });
        $(".cancelColorCondition").off().one("click", function () {
            cancelColorClicked(container);
        });
        applyCurrentSettings(container);
    }

    function applyCurrentSettings(container) {
        var settings = container.closest("tr").find(".colorConditionRowJson").val();
        $(".colorConditionRowModal").not(".firstColorConditionRowModal").detach();
        $(".colorConditionFieldSelect").val($(".colorConditionFieldSelect option:first").val());
        $(".colorConditionOperatorSelect").val($(".colorConditionOperatorSelect option:first").val());
        $(".colorConditionValueSelect").val("");
        $("#target").val($("#target option:first").val());
        $("#cellColorModal").val("");
        $("#cellFormattingModal").val("$value");
        if (settings) {
            var json = JSON.parse(settings);
            $("#cellColorModal").val(json.color);
            $("#cellFormattingModal").val(json.icon);
            for (var i = 0; i < json.conditions.length; i++) {
                var row;
                if (i == 0) {
                    row = $(".firstColorConditionRowModal");
                } else {
                    row = cloneRow();
                }
                //this if required to support legacy configurations
                if (json.conditions[i].field && (json.conditions[i].field.indexOf("GP")>-1) && json.conditions[i].field.split("-").length < 5) json.conditions[i].field = json.conditions[i].field + "-1"
                row.find(".colorConditionFieldSelect").val(json.conditions[i].field);
                row.find(".colorConditionOperatorSelect").val(json.conditions[i].operator);
                row.find(".colorConditionValueSelect").val(json.conditions[i].value);
            }
        }
        updateSampleLabel()
    }


    function createColorConditionsModalForDT(container) {
        if (!measureList) return
        $(".colorConditionValueSelect").val("")
        $(".colorConditionOperatorSelect").val("");
        $(".cellFormattingEdit").show().val("");
        switchHtmlEditor(false);
        updateColorConditionSelectForDT();
        $(".addColorCondition").off().one("click", function () {
            addColorClicked(container);
        });
        $(".cancelColorCondition").off().one("click", function () {
            cancelColorClicked(container);
        });
        applyCurrentSettings(container);
    }

    createTablesForSavedColorConditions();
});

function updateColorConditionSelectForCLL() {
    var $select = $(".colorConditionFieldSelect");
    $select.empty();
    $(".fieldInfo").each(function () {
        var field = $(this).find(".columnName").text();
        var selected = $(this).hasClass("columnSelected");
        if (field) {
            $select.append("<option " + (selected ? "selected" : "") + " value='" + field + "'>" + field + "</option>");
        }
    });
    return $select;
}

function updateColorConditionSelectForDT(all) {
    var $select = $(".colorConditionFieldSelect");
    $select.empty();


    $(".rowsContainer .fieldInfo").each(function () {
        var field = $(this);
        if (field.attr("fieldname"))
            $select.append("<option value='ROW_" + field.attr("fieldid") + "'>" + field.attr("fieldname") + "</option>");
    });
    var localSetIndex = -1;
    $(".columnMeasureSet").each(function (setIndex) {
        if (!measureList[setIndex]) return;
        localSetIndex++
        var measureSet = $(this);
        var measuresElements = measureSet.find(".validMeasure")

        var label = ""
        measureSet.find(".fieldInfo").each(function () {
            var field = $(this);

            label += ((label ? "|" : "") + field.attr("fieldname"))
        });
        var localMeasureIndex = -1;
        for (var i = 0; i < measureList[setIndex].length; i++) {
            if (!measureList[setIndex][i]) continue;
            localMeasureIndex++;

            if (($(this).find(".columnSelected").length > 0) || all) { //all is using for delete purpose only
                var selected = $(measuresElements[i]).hasClass("columnSelected");
                var measureCode = (measureList[setIndex][i].id ? measureList[setIndex][i].id : measureList[setIndex][i].type);
                var countType = $("#colMeas" + setIndex + "-meas" + i + "-dateRangeCount").val();
                if (countType == "PERIOD_COUNT") countType = "1"; else if (countType == "CUMULATIVE_COUNT") countType = "2"; else countType = "3";
                var value = "GP-" + measureCode + "-0-" + localSetIndex + "-" + countType

                var label = "Set #" + (localSetIndex + 1) + " " + $(measuresElements[localMeasureIndex]).find(".measureName").text()
                $select.append("<option " + (selected ? "selected" : "") + " value='" + value + "'>" + label + "</option>");
                if ($("input[name='colMeas" + setIndex + "-meas" + i + "-showTotal']").is(":checked")) {
                    var value = "GP-" + measureCode + "-2-" + localSetIndex + "-" + countType
                    $select.append("<option value='" + value + "'>" + label + " Total Column</option>");
                }
                var perc = $("select[name='colMeas" + setIndex + "-meas" + i + "-percentageOption']:checked").val()
                if (perc == "BY_TOTAL") {
                    var value = "GP-" + measureCode + "-1-" + localSetIndex + "-" + countType
                    $select.append("<option value='" + value + "'>" + label + " % By Total Column</option>");
                }
                if (perc == "BY_SUBTOTAL") {
                    var value = "GP-" + measureCode + "-1-" + localSetIndex + "-" + countType
                    $select.append("<option value='" + value + "'>" + label + " % By Total Subtotal Column</option>");
                }
                if (perc == "INTERVAL_TO_CUMULATIVE") {
                    var value = "GP-" + measureCode + "-1-" + localSetIndex + "-" + countType
                    $select.append("<option value='" + value + "'>" + label + " % Interval/Cumulative Column</option>");
                }
            }
        }
    });
    return $select;

}
function getOneColorConditionJsonFromModal() {
    var row = {
        conditions: [],
        color: $("#cellColorModal").val(),
        icon: $("#cellFormattingModal").val()
    };
    $(".colorConditionRowModal").each(function () {
        row.conditions.push(
            {
                field: $(this).find(".colorConditionFieldSelect").val(),
                fieldLabel: $(this).find(".colorConditionFieldSelect").find("option:selected").text(),
                operator: $(this).find(".colorConditionOperatorSelect").val(),
                operatorLabel: $(this).find(".colorConditionOperatorSelect").find("option:selected").text(),
                value: $(this).find(".colorConditionValueSelect").val()
            }
        );
    });
    return row;
}

function updateMeasureColorConditionJson(element) {
    var result = [];
    $(element).closest(".colorConditionsCantainer").find(".colorParameterTable tbody tr").each(function () {
        var stringValue = $(this).find(".colorConditionRowJson").val();
        if (stringValue) {
            row = JSON.parse(stringValue)
            if (row.conditions && row.conditions.length > 0) {
                result.push(row);
            }
        }
    });
    $(element).closest(".colorConditionsCantainer").find(".colorConditionsJson").val(JSON.stringify(result)).trigger("change");
}

function cancelColorClicked(container) {
    var tr = $(container).closest("tr");
    if (tr.find(".conditionsSpan").text() == "?") tr.find(".sectionRemove").trigger('click');
}

function addColorClicked(container) {
    var rowJson = getOneColorConditionJsonFromModal();
    var tr = container.closest("tr");
    tr.closest("tr").find(".colorConditionRowJson").val(JSON.stringify(rowJson));
    createConditionRow(container, rowJson);
    executeUpdateSampleLabel(rowJson.icon, rowJson.color, container.find(".rowFormatSample"))
    updateMeasureColorConditionJson(container);
    $("#colorConditionModal").modal("hide");
    tr.show();
}

function updateColorCellConditionsAfterDelete() {
    var templateType = $('#templateType').val()
    var select;
    if (templateType === "CASE_LINE") {
        select = updateColorConditionSelectForCLL();
    } else if (templateType === "DATA_TAB") {
        select = updateColorConditionSelectForDT(true);
    }
    var existingFields = [];
    select.find("option").each(function () {
        existingFields.push($(this).attr("value"));
    });


    $(".colorConditionsCantainer").each(function () {
        var container = $(this);
        var colorConditionsJsonInput = container.find(".colorConditionsJson");
        var conditionString = colorConditionsJsonInput.val();
        if (conditionString) {
            var conditionJson = JSON.parse(conditionString);
            for (var i = 0; i < conditionJson.length; i++) {
                for (var j = 0; j < conditionJson[i].conditions.length; j++) {
                    if (existingFields.indexOf(conditionJson[i].conditions[j].field) === -1) {
                        conditionJson[i].conditions.splice(j, 1);
                        j--;
                    }
                }
                if (conditionJson[i].conditions.length == 0) {
                    conditionJson.splice(i, 1);
                    i--;
                }
            }
            colorConditionsJsonInput.val(JSON.stringify(conditionJson)).trigger("change");
        }
    });
    createTablesForSavedColorConditions();
}

function createConditionRow(container, rowJson) {
    var conditions = rowJson.conditions
    container.empty();
    for (var i = 0; i < conditions.length; i++) {
        if (i > 0) container.append(createAndForConditionSpan());
        container.append(createConditionSpan(conditions[i]))
    }
    var sample = container.closest("tr").find(".rowFormatSample")
    executeUpdateSampleLabel(rowJson.icon, rowJson.color, sample)
    container.closest("tr").find(".colorConditionRowJson").val(JSON.stringify(rowJson));
}

function executeUpdateSampleLabel(format, color, container) {
    var val = format;
    val = val.replaceAll("$value", "AaBbCcYyZz");
    if (color) {
        container.css("background-color", color);
    } else {
        container.css("background-color", "#ffffff");
    }
    container.html(val);
}

function createTablesForSavedColorConditions() {
    $(".colorConditionsJson").each(function () {
        var jsonString = $(this).val();
        var $table = $(this).parent().find("tbody");
        $table.empty();
        if (!jsonString) return
        var rowTemplate = $(this).parent().find(".colorParameterTemplateRow");
        var json = JSON.parse(jsonString);
        for (var rowIndex = 0; rowIndex < json.length; rowIndex++) {
            var clone = rowTemplate.clone()
            createConditionRow(clone.find(".conditionsSpan"),json[rowIndex]);
            if (rowIndex > 0) $table.append(createOrRow());
            $table.append(clone);
            clone.show();
        }
    });
}

function createOrRow() {
    return "<tr class='orTr'><td colSpan='4'><b>Else</b></td></tr>";
}

function createAndForConditionSpan() {
    return "<span  style=\"background: #eeeeee;padding: 3px;border-radius: 4px; margin-left:5px; border: #cccccc solid 1px;\">AND</span>"
}

function createConditionSpan(json) {
    return "<span class=\"conditionAndAnd\">\n" +
        "                                <span style=\"background: #efefef;padding: 3px;border-radius: 4px;border: #cccccc solid 1px;\">\n" +
        "                                <input type='hidden' class='oneConditionJson' value='" + JSON.stringify(json) + "'>" +
        "                                    <span class='condField'>" + json.fieldLabel + "</span>\n" +
        "                                    <span class='condOperator'>" + json.operatorLabel + "</span>\n" +
        "                                    <span class='condValue'>" + json.value + "</span>\n" +
        "                                </span>\n" +
        "                                </span>"
}

function initMeasureSelect2(index) {
    $("#selectMeasure" + index).select2({}).on("select2:select", function (e) {
        const added = e.params.data
        if (added.text == 'Select Measure') {
            $(this).parent().addClass('has-error');
        } else {
            $(this).parent().removeClass('has-error');
            if (measureIndexList[index] > -1) {
                measureIndexList[index]++;
            } else {
                measureIndexList[index] = 0;
            }
            var measuresContainer = $($(this).closest('.columnMeasureSet')).find(".measuresContainer");
            var measureInfo = {
                name: added.text,
                type: added.id
            };
            addMeasureDiv(measureInfo, measuresContainer, measureIndexList[index]);

            var optionId = 'colMeas' + index + '-meas' + measureIndexList[index];
            addMeasureOptionsDiv(optionId, added);
            if (!measureList[index]) {
                measureList[index] = [];
            }
            var measure = added;
            measure.name = added.text;
            measureList[index].push(measure);
            checkMeasureSorting();
        }
    }).on("select2:open", function () {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
        $('#selectMeasure' + index).val('').trigger('change');
    });
    if (viewOnly) {
        $('#selectMeasure' + index).attr('readonly', true)
    }
}

function addMeasureDiv(measureInfo, container, measIndex) {
    var measureDiv = createMeasureDiv(measureInfo, measIndex);
    $(container).append(measureDiv);
    return measureDiv;
}

function addMeasureOptionsDiv(optionId, measure) {
    var cloned = $('#colMeas-meas').clone();
    $(cloned).attr('id', optionId);

    $($(cloned).find('.measureType')[0]).attr('id', optionId + '-type')
        .attr('name', optionId + '-type')
        .val(measure.id);

    $($(cloned).find('.inputMeasureName')[0]).attr('id', optionId + '-name')
        .attr('name', optionId + '-name')
        .val(measure.text);

    $($(cloned).find('select')[0]).attr('id', optionId + '-dateRangeCount')
        .attr('name', optionId + '-dateRangeCount');

    $($(cloned).find('.measureRelativeDateRangeValue')[0]).attr('id', optionId + '-relativeDateRangeValue')
        .attr('name', optionId + '-relativeDateRangeValue');

    // date pickers
    $($(cloned).find('#colMeas-meas-datePickerFrom')[0]).attr('id', optionId + '-datePickerFrom')
        .attr('name', optionId + '-datePickerFrom');
    $($(cloned).find('#colMeas-meas-datePickerTo')[0]).attr('id', optionId + '-datePickerTo')
        .attr('name', optionId + '-datePickerTo');
    $($(cloned).find('#colMeas-meas-customPeriodFrom')[0]).attr('id', optionId + '-customPeriodFrom')
        .attr('name', optionId + '-customPeriodFrom');
    $($(cloned).find('#colMeas-meas-customPeriodTo')[0]).attr('id', optionId + '-customPeriodTo')
        .attr('name', optionId + '-customPeriodTo');

    // radio group
    $($(cloned).find('#colMeas-meas-percentageOption')).attr('id', optionId + '-percentageOption')
        .attr('name', optionId + '-percentageOption');

    $($(cloned).find('#colMeas-meas-showTotal')[0]).attr('id', optionId + '-showTotal')
        .attr('name', optionId + '-showTotal');
    $($(cloned).find('#colMeas-meas-showTotalLabel')[0]).attr('id', optionId + '-showTotalLabel')
        .attr('for', optionId + '-showTotal');

    $($(cloned).find('#colMeas-meas-showTopX')[0]).attr('id', optionId + '-showTopX')
        .attr('name', optionId + '-showTopX');
    $($(cloned).find('#colMeas-meas-showTopXLabel')[0]).attr('id', optionId + '-showTopXLabel')
        .attr('for', optionId + '-showTopX');
    $($(cloned).find('#colMeas-meas-topXCount')[0]).attr('id', optionId + '-topXCount')
        .attr('name', optionId + '-topXCount');
    $($(cloned).find('#colMeas-meas-topColumnX')[0]).attr('id', optionId + '-topColumnX')
        .attr('name', optionId + '-topColumnX');
    $($(cloned).find('#colMeas-meas-topColumnType')[0]).attr('id', optionId + '-topColumnType')
        .attr('name', optionId + '-topColumnType');
    $($(cloned).find('#colMeas-meas-showTopColumn')[0]).attr('id', optionId + '-showTopColumn')
        .attr('name', optionId + '-showTopColumn');
    $($(cloned).find('#colMeas-meas-showTopColumnLabel')[0]).attr('id', optionId + '-showTopColumnLabel')
        .attr('for', optionId + '-showTopColumn');

    $($(cloned).find('#colMeas-meas-sort')[0]).attr('id', optionId + '-sort')
        .attr('name', optionId + '-sort');

    $($(cloned).find('#colMeas-meas-sortLevel')[0]).attr('id', optionId + '-sortLevel')
        .attr('name', optionId + '-sortLevel');

    $($(cloned).find('#colMeas-meas-drillDown')[0]).attr('id', optionId + '-drillDown')
        .attr('name', optionId + '-drillDown');

    $($(cloned).find('[name=colMeas-meas-percentageChartType]')[0]).attr('id', optionId + '-percentageChartType')
        .attr('name', optionId + '-percentageChartType');

    $($(cloned).find('[name=colMeas-meas-valuesChartType]')[0]).attr('id', optionId + '-valuesChartType')
        .attr('name', optionId + '-valuesChartType');

    $($(cloned).find('[name=colMeas-meas-percentageAxisLabel]')[0]).attr('id', optionId + '-percentageAxisLabel')
        .attr('name', optionId + '-percentageAxisLabel');

    $($(cloned).find('[name=colMeas-meas-colorConditions]')[0]).attr('id', optionId + '-colorConditions')
        .attr('name', optionId + '-colorConditions');

    $($(cloned).find('[name=colMeas-meas-valueAxisLabel]')[0]).attr('id', optionId + '-valueAxisLabel')
        .attr('name', optionId + '-valueAxisLabel');

    $($(cloned).find('[name=colMeas-meas-overrideValueAxisLabel]')[0]).attr('id', optionId + '-overrideValueAxisLabel')
        .attr('name', optionId + '-overrideValueAxisLabel');

    $($(cloned).find('[id=colMeas-meas-overrideValueAxisLabelLabel]')[0]).attr('id', optionId + '-overrideValueAxisLabelLabel')
        .attr('for', optionId + '-overrideValueAxisLabel');

    $($(cloned).find('[name=colMeas-meas-overridePercentageAxisLabel]')[0]).attr('id', optionId + '-overridePercentageAxisLabel')
        .attr('name', optionId + '-overridePercentageAxisLabel');

    $($(cloned).find('[id=colMeas-meas-overridePercentageAxisLabelLabel]')[0]).attr('id', optionId + '-overridePercentageAxisLabelLabel')
        .attr('for', optionId + '-overridePercentageAxisLabel');

    $('#measureOptionsArea').append(cloned);

    // set default value for custom date and relativeDateRangeValue
    setCustomPeriod(new Date(), null, $('#' + optionId + '-customPeriodFrom'));
    setCustomPeriod(null, new Date(), $('#' + optionId + '-customPeriodTo'));
    $('#' + optionId + '-relativeDateRangeValue').val("1");

    // initialize select2
    initDateRangeCountSelect2(optionId);

    // initialize date pickers
    initDatePickers(optionId);

    initTopXPane(measure.id, cloned)

    bindSelect2WithUrl(cloned.find(".drilldownTemplate"), specificCllTemplateSearchUrl, templateNameUrl, true);
}

function updatePercentageOptions(optionId, val) {
    var percentageSelect = $('#' + optionId + '-percentageOption');
    var intToCumOption = percentageSelect.find("option[value=INTERVAL_TO_CUMULATIVE]");
    intToCumOption.hide();
    if (val === "CUMULATIVE_COUNT") {
        intToCumOption.show();
    } else if (percentageSelect.val() == "INTERVAL_TO_CUMULATIVE") {
        percentageSelect.val("NO_PERCENTAGE")
    }
}
function initDateRangeCountSelect2(optionId) {
    var select = $('#' + optionId + '-dateRangeCount');
    $.data(select[0], 'current', select.val());
    select.select2({}).on("change", function (e) {
        var selectedValue = $(e.target).val();
        if ((selectedValue == CUSTOM_PERIOD_COUNT) || (selectedValue.indexOf(LAST_TYPE_TOKEN) > -1) || (selectedValue.indexOf(NEXT_TYPE_TOKEN) > -1)) {
            var prefix = $(this).attr("id").substring(0, $(this).attr("id").indexOf("-"));
            var measures = $("select[id^=" + prefix + "]");
            for (var i = 0; i < measures.length; i++) {
                if (measures[i] != this) {
                    var val = $(measures[i]).val();
                    if (val && ((val == CUSTOM_PERIOD_COUNT) || (val.indexOf(LAST_TYPE_TOKEN) > -1) || (val.indexOf(NEXT_TYPE_TOKEN) > -1))) {
                        $(this).select2("val", $.data(this, 'current'));
                        $("#warningModal").modal("show");
                        return false;
                    }
                }
            }
        }

        $.data(this, 'current', selectedValue);
        var container = $(this).closest('.measureOptions');
        if (selectedValue == CUSTOM_PERIOD_COUNT) {
            showDatePicker(true, container);
            setDatePicker(container);
        } else {
            showDatePicker(false, container);
        }
        if ((selectedValue.indexOf(LAST_TYPE_TOKEN) > -1) || (selectedValue.indexOf(NEXT_TYPE_TOKEN) > -1)) {
            container.find(".measureRelativeDateRangeValue").parent().show();
        } else {
            container.find(".measureRelativeDateRangeValue").parent().hide();
        }

        var drillDownSelect = $('#' + optionId + '-drillDown');
        if (selectedValue !== "PERIOD_COUNT") {
            drillDownSelect.val("");
            drillDownSelect.attr("disabled", true);
        } else {
            drillDownSelect.attr("disabled", false);
        }
        updatePercentageOptions(optionId, selectedValue)
    });
    updatePercentageOptions(optionId, select.val());
}

function initDatePickers(optionId) {
    var hiddenInputFrom = $('#' + optionId + '-customPeriodFrom');
    var hiddenInputTo = $('#' + optionId + '-customPeriodTo');

    $('#' + optionId + '-datePickerFrom').datepicker({
        allowPastDates: true,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    }).on('changed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
        // when input is changed directly
        setCustomPeriod(date, null, hiddenInputFrom);
    });

    $('#' + optionId + '-datePickerTo').datepicker({
        allowPastDates: true,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    }).on('changed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
        // when input is changed directly
        setCustomPeriod(null, date, hiddenInputTo);
    });
}

function initSortIcons(measureDiv, colMesaIndex, measIndex) {
    var measureInfo = measureList[colMesaIndex][measIndex];
    if (isSortableMeasure(measureInfo)) {
        var optionId = 'colMeas' + colMesaIndex + '-meas' + measIndex;
        var measureSortField = $("#" + optionId).find(".measureSort");
        var measureSortIcon = $(measureDiv).find(".measureSortIcon")[0];
        if (measureInfo.sort && measureInfo.sort != SORT_DISABLED) {
            removeSortIcon(measureSortIcon.classList);
            measureSortIcon.style.opacity = 1.0;
            if (measureInfo.sort == SORT_ASCENDING) {
                addAscIcon(measureSortIcon.classList);
            } else {
                addDescIcon(measureSortIcon.classList);
            }
        }
        measureSortField.val(measureInfo.sort);
    }
}

function setCustomPeriod(dateFrom, dateTo, hiddenInput) {
    if (dateFrom) {
        $(hiddenInput).val(moment(dateFrom).format(DEFAULT_DATE_DISPLAY_FORMAT));
    }
    if (dateTo) {
        $(hiddenInput).val(moment(dateTo).format(DEFAULT_DATE_DISPLAY_FORMAT));
    }
}

function showDatePicker(isCustomPeriod, container) {
    if (isCustomPeriod) {
        $($(container).find('.customPeriodDatePickers')[0]).show();
    } else {
        $($(container).find('.customPeriodDatePickers')[0]).hide();
    }
}

function setDatePicker(container) {
    var datePickerFrom = $(container).find('.datepicker')[0];
    var datePickerTo = $(container).find('.datepicker')[1];

    var dateFrom = $($(container).find('.customPeriodFrom')[0]).val();
    if (!dateFrom) {
        dateFrom = moment(new Date()).format(DEFAULT_DATE_DISPLAY_FORMAT);
        $($(container).find('.customPeriodFrom')[0]).val(dateFrom);
    }

    var dateTo = $($(container).find('.customPeriodTo')[0]).val();
    if (!dateTo) {
        dateTo = moment(new Date()).format(DEFAULT_DATE_DISPLAY_FORMAT);
        $($(container).find('.customPeriodTo')[0]).val(dateTo)
    }

    $(datePickerFrom).datepicker('setDate', dateFrom);
    $(datePickerTo).datepicker('setDate', dateTo);
}

function createMeasureDiv(measureInfo, measIndex) {
    var measureName = measureInfo.name;
    var measureDiv = document.createElement("div");
    measureDiv.style.float = "left";
    measureDiv.classList.add("validMeasure");
    measureDiv.classList.add("defaultHeaderBackground");
    $(measureDiv).attr('sequence', measIndex);
    if (!viewOnly) {
        var closeIcon = document.createElement("i");
        closeIcon.classList.add("fa");
        closeIcon.classList.add("fa-times");
        closeIcon.classList.add("removeMeasure");
        closeIcon.classList.add("add-cursor");
        measureDiv.appendChild(closeIcon);
    }
    if (isSortableMeasure(measureInfo)) {
        var measureSortOrderSeq = document.createElement("div");
        measureSortOrderSeq.classList.add("measureSortOrderSeq");
        measureDiv.appendChild(measureSortOrderSeq);
    }
    var measure = document.createElement("div");
    measure.classList.add("measureName");
    measure.classList.add("add-cursor");
    measure.innerHTML = measureName;
    measureDiv.appendChild(measure);
    if (isSortableMeasure(measureInfo)) {
        var sortIcon = document.createElement("i");
        sortIcon.classList.add("fa");
        sortIcon.classList.add("fa-sort");
        sortIcon.classList.add("fa-lg");
        sortIcon.classList.add("measureSortIcon");
        sortIcon.classList.add("sortDisabled");
        measureDiv.appendChild(sortIcon);
    }
    return measureDiv;
}

function getMeasureSequence(index, measureDiv) {
    var measuresNodeList = $($(".measuresContainer")[index]).find(".measureName");
    var measures = Array.prototype.slice.call(measuresNodeList);
    return measures.indexOf(measureDiv);
}

function getMeasureOptionsDiv(measureDiv) {
    var colMeasDiv = $(measureDiv).closest('.columnMeasureSet');
    var colMeasIndex = $(colMeasDiv).attr('sequence');

    var measureDiv = $(measureDiv).closest(".validMeasure");
    var measureIndex = $(measureDiv).attr('sequence');

    return $('#colMeas' + colMeasIndex + '-meas' + measureIndex)
}

function setValidMeasureIndexList() {
    $.each($('.columnMeasureSet'), function () {
        var colMeasIndex = $(this).attr('sequence');
        var validIndex = [];
        $.each($(this).find('.validMeasure'), function () {
            validIndex.push($(this).attr('sequence'));
        });
        console.log('valid measure index:', colMeasIndex, validIndex);

        $('#colMeas' + colMeasIndex + '-validMeasureIndex').val(validIndex);
    });
}

function initTopXPane(measureType, container) {
    if (measureType == CASE_LIST) {
        $($(container).find('.topXPane')[0]).hide();
    } else {
        $($(container).find('.topXPane')[0]).show();
    }
}

function handleMeasureSortIconClick() {
    if (!viewOnly) {
        var measure = this.parentElement;
        var measureName = $(measure).find(".measureName")[0];
        var colMeasIndex = $(this).closest('.columnMeasureSet').attr('sequence');
        var measureIndex = $(this).closest('.validMeasure').attr('sequence');
        var optionId = 'colMeas' + colMeasIndex + '-meas' + measureIndex;
        var measureSortField = $("#" + optionId).find(".measureSort");
        $.each($('.columnSelected'), function () {
            this.classList.remove('columnSelected');
        });
        measure.classList.add('columnSelected');

        clearSorting(measure);

        if (this.classList.contains('sortDisabled')) {
            removeSortIcon(this.classList);
            addAscIcon(this.classList);
            this.style.opacity = 1.0;
            measureSortField.val(SORT_ASCENDING);
        } else if (this.classList.contains('sortAscending')) {
            removeAscIcon(this.classList);
            addDescIcon(this.classList);
            this.style.opacity = 1.0;
            measureSortField.val(SORT_DESCENDING);
        } else if (this.classList.contains('sortDescending')) {
            removeDescIcon(this.classList);
            addSortIcon(this.classList);
            this.style.opacity = 0.3;
            measureSortField.val(SORT_DISABLED);
        }
    }
}

function isSortableMeasure(measureInfo) {
    return measureInfo.type !== CASE_LIST;
}

function clearSorting(excludes) {
    $(".validMeasure").not(excludes).each(function () {
        clearMeasureSorting(this);
    });
}

function clearMeasureSorting(measure) {
    var colMeasIndex = $(measure).closest('.columnMeasureSet').attr('sequence');
    var measureIndex = $(measure).closest('.validMeasure').attr('sequence');
    var optionId = 'colMeas' + colMeasIndex + '-meas' + measureIndex;
    var measureSortField = $("#" + optionId).find(".measureSort");
    var measureSortIcon = $(measure).find(".measureSortIcon")[0];
    if (measureSortIcon) {
        removeAscIcon(measureSortIcon.classList);
        removeDescIcon(measureSortIcon.classList);
        addSortIcon(measureSortIcon.classList);
        measureSortIcon.style.opacity = 0.3;
        measureSortField.val(SORT_DISABLED);
    }
}

function enableMeasureSorting(enable) {
    $(".validMeasure").each(function () {
        var measureSortIcon = $(this).find(".measureSortIcon")[0];
        if (measureSortIcon) {
            if (!enable) {
                clearMeasureSorting(this);
                removeSortIcon(measureSortIcon.classList);
                removeAscIcon(measureSortIcon.classList);
                removeDescIcon(measureSortIcon.classList);
            } else if (
                !measureSortIcon.classList.contains('sortDisabled') &&
                !measureSortIcon.classList.contains('sortAscending') &&
                !measureSortIcon.classList.contains('sortDescending')
            ) {
                addSortIcon(measureSortIcon.classList);
                clearMeasureSorting(this);
            }
        }
    });
}

function enableMeasureTopX(enable) {
    if (!enable) {
        $(".showTopX").prop('checked', false).prop('disabled', true);
        $(".topXCount").val("").prop('disabled', true);
    } else {
        $(".showTopX").prop('disabled', false);
    }
}

function checkMeasureSorting() {
    if ($("#rowsContainer").children().length > 1) {
        enableMeasureSorting(false);
        enableMeasureTopX(false);
    } else {
        enableMeasureSorting(true);
        enableMeasureTopX(true);
    }
}