var rowIndex = 0;
var SQL_DML_PATTERN_REGEX = /((^|(.*?(\s|\(|\))))(insert|use|alter|desc|create|drop|delete|update)\s.*?$)|.*?;.*?$/


$(function() {

    var queryComplianceChildCount = $("#queryComplianceList").attr("data-counter");
    var deletedQueryCompliance;
    var sectionDiv = $("#queryComplianceList");

    var withTitle = function (label) {
        return '<span title="' + label.text + '">' + label.text + '</span>';
    }

    // Use select2 change event for IE11 in queriesCompliance.js
    function selectQueryOnChange(selectContainer) {
        var queryContainer = getQueryWrapperRow(selectContainer);
        var expressionValues = getExpressionValues(queryContainer);
        var queryWrapperRow = getQueryWrapperRow(selectContainer);
        $(expressionValues).empty();
        if (getAJAXCount() == -1) {
            if($(selectContainer).val() != '' && $(selectContainer).val() != null) {
                $(queryWrapperRow).find('.queryViewButton').attr('href', queryViewUrl+'/'+$(selectContainer).val());
                $(queryWrapperRow).find('.queryViewButton').removeClass('hide');
                $(queryWrapperRow).find('.newQuery').addClass('hide');
            } else {
                $(queryWrapperRow).find('.queryViewButton').addClass('hide');
                $(queryWrapperRow).find('.newQuery').removeClass('hide');
            }
            getBlankValuesForQueryAJAX($(selectContainer).val(), expressionValues,(getTQNum(expressionValues)+"."));
            getCustomSQLValuesForQueryAJAX($(selectContainer).val(), expressionValues, (getTQNum(expressionValues)+"."));
            getBlankValuesForQuerySetAJAX($(selectContainer).val(), expressionValues,(getTQNum(expressionValues)+"."));
        } else {
            $(selectContainer).val('').trigger('change');
        }
    }

    var init = function () {
        if (queryComplianceChildCount == 0) { // for create page
            addQueryComplianceLineItem();
        } else {
            // initialize select2 if any
            $.each($("#queryComplianceList .selectQuery"), function (index, item) {
                bindQuerySelect2($(item)).on("select2:select select2:unselect", function (e) {
                selectQueryOnChange(this);
                });
            });

            $("#queryComplianceList").find(".expressionField").select2();
            $("#queryComplianceList").find(".expressionOp").select2();
            $("#queryComplianceList").find(".expressionValueSelect").select2({separator: ";"});
        }

        //bind click event on delete buttons
        $("#queryComplianceList").on('click', '.templateQueryDeleteButton', function () {
            deletedQueryCompliance = $(this).attr('data-id');
            //find the parent div
            var parentEl = $(this).parents(".templateQuery-div");
            //find the deleted hidden input
            var delInput = parentEl.find("input[id$=dynamicFormEntryDeleted]");
            //set the deletedFlag to true
            delInput.attr('value', 'true');
            // set relativeDateRangeValue to 1
            if(parentEl.find('.relativeDateRangeValue').length > 0){
                parentEl.find('.relativeDateRangeValue')[0].value = 1;
            }

            //hide the div
            parentEl.hide();

            if ($('.templateQuery-div:visible').length == 1) { // only one QueryCompliance left
                removeSectionCloseButton($('.templateQuery-div:visible'));
            }
            updateQueryComplianceLineItem(deletedQueryCompliance);
            if (typeof list != 'undefined') {
                delete list[deletedQueryCompliance];
            }
        });

        if ($('.templateQuery-div:visible').length == 1) { // only one QueryCompliance left
            removeSectionCloseButton($('.templateQuery-div:visible'));
        }

        $(".addQueryComplianceLineItemButton").on('click', addQueryComplianceLineItem);
        $(".copyQueryComplianceLineItemButton").on('click', copyQueryComplianceLineItem);
    };

    var addQueryComplianceLineItem = function () {
        createQueryComplianceLineItem(false);
    };

    var copyQueryComplianceLineItem = function () {
        createQueryComplianceLineItem(true);
    };

    var createQueryComplianceLineItem = function (copyValuesFromPreviousSection) {
        var clone = $("#queryCompliance_clone").clone();
        var htmlId = 'queriesCompliance[' + queryComplianceChildCount + '].';

        var queryComplianceDeleteButton = clone.find("i[id$=deleteButton]");
        var query = clone.find("select[id$=query]");
        var operator = clone.find("select[id$=operator]");

        var criteriaName = clone.find("input[id$=criteriaName]");
        var allowedTimeframe = clone.find("input[id$=allowedTimeframe]");

        //cloning the hidden fields
        clone.find("input[id$=version]")
            .attr('id', htmlId + 'version')
            .attr('name', htmlId + 'version');
        clone.find("input[id$=id]")
            .attr('id', htmlId + 'id')
            .attr('name', htmlId + 'id');
        clone.find("input[id$=dynamicFormEntryDeleted]")
            .attr('id', htmlId + 'dynamicFormEntryDeleted')
            .attr('name', htmlId + 'dynamicFormEntryDeleted');
        clone.find("input[id$=new]")
            .attr('id', htmlId + 'new')
            .attr('name', htmlId + 'new')
            .attr('value', 'true');
        clone.find(".validQueries")
            .attr('id', htmlId + 'validQueries')
            .attr('name', htmlId + 'validQueries');

        query.attr('id', htmlId + 'query')
            .attr('name', htmlId + 'query');

        operator.attr('id', htmlId + 'operator')
            .attr('name', htmlId + 'operator');

        queryComplianceDeleteButton.attr('id', htmlId + 'deleteButton')
            .attr('name', htmlId + 'deleteButton')
            .attr('data-id',queryComplianceChildCount);

        criteriaName.attr('id', htmlId + 'criteriaName')
            .attr('name', htmlId + 'criteriaName');

        allowedTimeframe.attr('id', htmlId + 'allowedTimeframe')
            .attr('name', htmlId + 'allowedTimeframe');

        clone.attr('id', 'queryCompliance' + queryComplianceChildCount);

        // Use select2 change event for IE11
        bindQuerySelect2(query).on("change", function (e) {
            selectQueryOnChange(this);
        });
        operator.select2({templateSelection: withTitle});

        $("#queryComplianceList").append(clone);

        if (copyValuesFromPreviousSection) {

            var lastVisibleQueryComplianceChildIndex = 0;
            for (var i = 0; i < queryComplianceChildCount; i++) {
                if ($("#queriesCompliance\\[" + i + "\\]\\.dynamicFormEntryDeleted").val() == "false")
                    lastVisibleQueryComplianceChildIndex = i;
            }
            var currentIndex = queryComplianceChildCount;

            var queryContainer = getQueryWrapperRow(query);
            var expressionValues = getExpressionValues(queryContainer);
            $(expressionValues).on("loadBlankAndCustomSqlFieldsComplete", function () {
                copyBlankAndCustomSqlFields(lastVisibleQueryComplianceChildIndex, currentIndex);
            });

            var previousSectionPrefix = "queriesCompliance\\[" + lastVisibleQueryComplianceChildIndex + "\\]\\.";

            query.attr("data-value", $("#" + previousSectionPrefix + "query").val()).trigger("change");
            query.one("change",function(){$(expressionValues).off("loadBlankAndCustomSqlFieldsComplete")});
            operator.val($("#" + previousSectionPrefix + "operator").val()).trigger("change");

            criteriaName.val($("#" + previousSectionPrefix + "criteriaName").val());
            allowedTimeframe.val($("#" + previousSectionPrefix + "allowedTimeframe").val());
        }

        clone.show();

        queryComplianceChildCount++;

        if ($('.templateQuery-div:visible').length > 1) {
            $.each($('.templateQuery-div:visible'), function() {
                // $($(this).find('.queryComplianceDeleteButton')).show();
                $($(this).find('.templateQueryDeleteButton')).closest("div").show();
            })
        }

    };

    var copyBlankAndCustomSqlFields = function (lastVisibleQueryComplianceChildIndex, currentIndex) {
        var blankAndCustomSqlFields = $("[name^='queryCompliance" + lastVisibleQueryComplianceChildIndex + ".'][name$='].value']," +
            "[name^='queryCompliance" + lastVisibleQueryComplianceChildIndex + ".'][name$='].copyPasteValue']");
        _.each(blankAndCustomSqlFields, function (it) {
            var current = $(it);
            var nameAttributeValueForCopyField = "queryCompliance" + currentIndex + current.attr("name").substring(current.attr("name").indexOf("."));
            if (current.val()) {
                var fieldToCopy = $("[name='" + nameAttributeValueForCopyField + "']");

                if (current.hasClass("expressionValueSelectAuto")) {
                    var selectAjaxValues = [];
                    _.each(current.val().split(";"),function(singeValue){
                        selectAjaxValues.push({id: singeValue, text: singeValue});
                        fieldToCopy.select2("data", selectAjaxValues);
                    });
                } else if (current.hasClass("expressionValueSelectNonCache")) {
                    var selectAjaxValues = [];
                    _.each(current.val().split(";"),function(singeValue){
                        selectAjaxValues.push({id: singeValue, text: singeValue});
                        fieldToCopy.select2("data", selectAjaxValues);
                    });
                } else{
                    fieldToCopy.val(current.val()).trigger("change");
                }
                if (current.hasClass("expressionValueText")) {
                    fieldToCopy = $("[name='" + nameAttributeValueForCopyField.replace("value", "copyPasteValue") + "']");
                    var currentQEV = fieldToCopy.closest('.toAddContainerQEV')[0];
                    if (currentQEV) {
                        fieldToCopy.val(current.val());
                        $(currentQEV).find('.isFromCopyPaste').val('true');
                        showHideValue(EDITOR_TYPE_TEXT, currentQEV);
                    }
                }
            }
        });
    };

    var removeSectionCloseButton = function (parentDiv) {
        var removeButtons = $(parentDiv).find("i.templateQueryDeleteButton").closest("div");
        $(removeButtons).hide();
    };

    //Logic for hiding and showing the date picker and text box for relative date input for show and hide of Contents
    var showHeaderFooterTitle = function () {
        var element = $(this);
        var headerFooterDiv = ($(element).parent().parent().parent());
        var advancedOptionDiv = headerFooterDiv.find('.headerFooterArea');
        advancedOptionDiv.toggle();

        if (advancedOptionDiv.is(':visible')) {
            $(this).text(LABELS.labelHideAdavncedOptions)
        } else {
            $(this).text(LABELS.labelShowAdavncedOptions)
        }
    };

    init();
    $("#queryComplianceList").on("click", ".createQueryComplianceButton", function () {
        var el = $(this);
        var url = el.attr("data-url");
        var message = el.attr("data-message");
        url += (url.indexOf("?") > -1) ? "&queryComplianceIndex=" : "?queryComplianceIndex=";
        url += getQueryComplianceIndex(el);
        showWarningOrSubmit(url, message);
    });

    function getQueryComplianceIndex(el) {
        var $currentQueryCompliance = $(el).closest(".templateQuery-div");
        var num = 0;
        var queryComplianceList = $("#queryComplianceList").find(".templateQuery-div");
        for (var i = 0; i < queryComplianceList.length; i++) {
            if ($(queryComplianceList[i]).is($currentQueryCompliance)) return num;
            if ($(queryComplianceList[i]).find("input[id$=dynamicFormEntryDeleted]").val() == "false") num++;
        }
        return num;
    }

    if ((typeof dashboardWidget == 'undefined') || !dashboardWidget) {
        sectionDiv.sortable({
            axis: 'y',
            cursor: "move",
            tolerance: 'pointer',
            handle: ".grab-icon",
            revert: 'invalid',
            placeholder: 'queryCompliance-placeholder',
            forceHelperSize: true,
            //https://stackoverflow.com/questions/10637095/jquery-ui-sortable-tolerance-option-not-working-as-expected
            sort: function (event, ui) {
                var that = $(this),
                    w = ui.helper.outerHeight();
                that.children().each(function () {
                    if ($(this).hasClass('ui-sortable-helper') || $(this).hasClass('ui-sortable-placeholder') || $(this).hasClass('queryCompliance-placeholder'))
                        return true;
                    // If overlap is more than half of the dragged item
                    var dist = Math.abs(ui.position.top - $(this).position().top),
                        before = ui.position.top > $(this).position().top;
                    if ((w - dist) > (w / 2) && (dist < w)) {
                        if (before)
                            $('.ui-sortable-placeholder', that).insertBefore($(this));
                        else
                            $('.ui-sortable-placeholder', that).insertAfter($(this));
                        return false;
                    }
                });
            }
        }).on("sortupdate", function (event, ui) {
            $("#queryComplianceList").children('div[id^="queryCompliance"]').each(function (i) {
                $(this).find('[name*="queriesCompliance"], [id*="queriesCompliance"]').each(function () {
                    // Update the 'queriesCompliance[0]' part of the name attribute to contain the latest count
                    if (typeof $(this).attr('name') !== 'undefined')
                        $(this).attr('name', $(this).attr('name').replace(/queriesCompliance\[\d+\]/, 'queriesCompliance[' + i + ']'));
                    if (typeof $(this).attr('id') !== 'undefined')
                        $(this).attr('id', $(this).attr('id').replace(/queriesCompliance\[\d+\]/, 'queriesCompliance[' + i + ']'));
                });
                //handling parameters values like queryCompliance2.qev[0].value
                $(this).find('[name^="queryCompliance"]', '[id^="queryCompliance"]').each(function () {
                    // Update the 'queriesCompliance[0]' part of the name attribute to contain the latest count
                    if (typeof $(this).attr('name') !== 'undefined')
                        $(this).attr('name', $(this).attr('name').replace(/queryCompliance\d+/, 'queryCompliance' + i));
                    if (typeof $(this).attr('id') !== 'undefined')
                        $(this).attr('id', $(this).attr('id').replace(/queryCompliance\d+/, 'queryCompliance' + i));
                });
                $(this).attr('id', $(this).attr('id').toString().replace(/queryCompliance\d+/, 'queryCompliance' + i));
            });
        });
    }

    $(document).on('click', ".saveModal", function () {
        $(".customExpressionErrorDiv").hide();
        if(validateCustomExpression($(".customExpressionQuery").val().toLowerCase())){
            var container = $(".containerName").val();
            var fieldName = $(".fieldName").val();
            var customExpressionQuery = $(".customExpressionQuery").val().replace(/\n/g, " ");
            $("#"+container).find('.'+fieldName).val(customExpressionQuery);
            $(".customExpressionQuery").val('');
            $(".customExpressionModal").modal('hide');
        }else{
            $(".customExpressionErrorDiv").show();
        }
    });

    function validateCustomExpression(query){
        return !(SQL_DML_PATTERN_REGEX.test(query));
    }

});

function bindQuerySelect2(selector) {
    return bindSelect2WithUrl(selector, querySearchUrl, queryNameUrl, true);
}

function updateQueryComplianceLineItem(deletedQueryCompliance){
    $(".templateQueryDeleteButton").each(function () {
        var id = parseInt($(this).attr('data-id'));
        if(id > parseInt(deletedQueryCompliance)){
            $(this).attr('data-id',(id-1));
        }
    });

}