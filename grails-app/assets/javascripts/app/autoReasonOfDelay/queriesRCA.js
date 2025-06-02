var rowIndex = 0;
var SQL_DML_PATTERN_REGEX = /((^|(.*?(\s|\(|\))))(insert|use|alter|desc|create|drop|delete|update)\s.*?$)|.*?;.*?$/


$(function() {

    var queryRCAChildCount = $("#queryRCAList").attr("data-counter");
    var deletedQueryRCA;
    var sectionDiv = $("#queryRCAList");

    var withTitle = function (label) {
        return '<span title="' + label.text + '">' + label.text + '</span>';
    }

    // Use select2 change event for IE11 in queriesRCA.js
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
        var lateJson = JSON.parse(lateList);
        var rootCauseJson = JSON.parse(rootCauseList);
        var rootCauseClassJson = JSON.parse(rootCauseClassList);
        var respPartyJson = JSON.parse(responsiblePartyList);
        var rootCauseSubCategoryJson = JSON.parse(rootCauseSubCategoryList);
        var lateSelect = '<option value="" >' + $.i18n._('selectOne') + '</option>';
        var rootCauseOption = '<option value="" >' + $.i18n._('selectOne') + '</option>';
        var rootCauseClassOption = '<option value="" >' + $.i18n._('selectOne') + '</option>';
        var respPartyOption = "<option value='' >"+$.i18n._('selectOne')+"</option>";
        var rootCauseSubCategoryOption = "<option value='' >"+$.i18n._('selectOne')+"</option>";
        if (queryRCAChildCount == 0) { // for create page
            addQueryRCALineItem();
        } else {
            // initialize select2 if any
            $.each($("#queryRCAList .selectQuery"), function (index, item) {
                    bindQuerySelect2($(item)).on("select2:select select2:unselect", function (e) {
                        selectQueryOnChange(this);
                    })
                }
            )
            $("#queryRCAList").find(".dateRangeEnumClass").select2().on("select2:select", function (e) {
                daraRangeEnumClassOnChange(this);
            });
            $("#queryRCAList").find(".expressionField").select2();
            $("#queryRCAList").find(".expressionOp").select2();
            $("#queryRCAList").find(".expressionValueSelect").select2({separator: ";"});
            var rootCauseIds
            var rootCauseClassIds
            var responsiblePartyIds
            var rootCauseSubCategoryIds
            $("#queryRCAList").find('.templateQuery-div').each(function(i, row) {
                var lateValue = parseInt($(row).find(".selectLateId").val(), 10);
                lateSelect = "<option value='' >"+$.i18n._('selectOne')+"</option>";
                for (var j = 0; j < lateJson.length; j++) {
                    if (lateJson[j]['id'] === lateValue) {
                        rootCauseIds = lateJson[j]['rootCauseIds'];
                        rootCauseClassIds = lateJson[j]['rootCauseClassIds'];
                        lateSelect += '<option value="' + lateJson[j]["id"] + '" selected>' + lateJson[j]["textDesc"] + '</option>';
                    } else if (lateJson[j]["hiddenDate"] == null) {
                        lateSelect += '<option value="' + lateJson[j]["id"] + '">' + lateJson[j]["textDesc"] + '</option>';
                    }
                }
                $(row).find(".selectLateId").html(lateSelect);
                $(row).find(".selectLateId").select2();

            var rootCauseValue = parseInt($(row).find(".selectRootCauseId").val(), 10);
                rootCauseOption = "<option value='' >"+$.i18n._('selectOne')+"</option>";
            for (var i = 0; i < rootCauseJson.length; i++) {
                if (_.indexOf(rootCauseIds, rootCauseJson[i]['id']) > -1) {
                    if (rootCauseJson[i]['id'] === rootCauseValue) {
                        responsiblePartyIds = rootCauseJson[i]['responsiblePartyIds'];
                        rootCauseSubCategoryIds = rootCauseJson[i]['rootCauseSubCategoryIds'];
                        rootCauseOption += '<option value="' + rootCauseJson[i]["id"] + '"selected>' + rootCauseJson[i]["textDesc"] + '</option>';
                    }else if(rootCauseJson[i]["hiddenDate"] == null) {
                        rootCauseOption += '<option value="' + rootCauseJson[i]["id"] + '">' + rootCauseJson[i]["textDesc"] + '</option>';
                    }
                }
            }
                $(row).find(".selectRootCauseId").html(rootCauseOption);
                $(row).find(".selectRootCauseId").select2();

            var rootCauseClassValue = parseInt($(row).find(".selectRootCauseClassId").val(), 10);
                rootCauseClassOption = "<option value='' >"+$.i18n._('selectOne')+"</option>";
            for (var i = 0; i < rootCauseClassJson.length; i++) {
                if (_.indexOf(rootCauseClassIds, rootCauseClassJson[i]['id']) > -1) {
                    if (rootCauseClassJson[i]['id'] === rootCauseClassValue) {
                        rootCauseClassOption += '<option value="' + rootCauseClassJson[i]["id"] + '"selected>' + rootCauseClassJson[i]["textDesc"] + '</option>';
                    } else if(rootCauseClassJson[i]["hiddenDate"] == null) {
                        rootCauseClassOption += '<option value="' + rootCauseClassJson[i]["id"] + '">' + rootCauseClassJson[i]["textDesc"] + '</option>';
                    }
                }
            }
                $(row).find(".selectRootCauseClassId").html(rootCauseClassOption);
                $(row).find(".selectRootCauseClassId").select2();


            var rootCauseSubCategoryValue = parseInt($(row).find(".selectRootCauseSubCategoryId").val(), 10);
                rootCauseSubCategoryOption = "<option value='' >"+$.i18n._('selectOne')+"</option>";
            for (var i = 0; i < rootCauseSubCategoryJson.length; i++) {
                if (_.indexOf( rootCauseSubCategoryIds, rootCauseSubCategoryJson[i]['id']) > -1) {
                    if (rootCauseSubCategoryJson[i]['id'] === rootCauseSubCategoryValue ) {
                        rootCauseSubCategoryOption += '<option value="' + rootCauseSubCategoryJson[i]["id"] + '"selected>' + rootCauseSubCategoryJson[i]["textDesc"] + '</option>';
                    } else if(rootCauseSubCategoryJson[i]["hiddenDate"] == null) {
                        rootCauseSubCategoryOption += '<option value="' + rootCauseSubCategoryJson[i]["id"] + '">' + rootCauseSubCategoryJson[i]["textDesc"] + '</option>';
                    }
                }
            }
                $(row).find(".selectRootCauseSubCategoryId").html(rootCauseSubCategoryOption);
                $(row).find(".selectRootCauseSubCategoryId").select2();

            var respPartyValue = parseInt($(row).find(".selectResponsiblePartyId").val(), 10);
                respPartyOption = "<option value='' >"+$.i18n._('selectOne')+"</option>";
            for (var i = 0; i < respPartyJson.length; i++) {
                if (_.indexOf( responsiblePartyIds, respPartyJson[i]['id']) > -1) {
                    if (respPartyJson[i]['id'] === respPartyValue ) {
                        respPartyOption += '<option value="' + respPartyJson[i]["id"] + '"selected>' + respPartyJson[i]["textDesc"] + '</option>';
                    } else if(respPartyJson[i]["hiddenDate"] == null) {
                        respPartyOption += '<option value="' + respPartyJson[i]["id"] + '">' + respPartyJson[i]["textDesc"] + '</option>';
                    }
                }
            }
                $(row).find(".selectResponsiblePartyId").html(respPartyOption);
                $(row).find(".selectResponsiblePartyId").select2();

        });

            $("#queryRCAList .templateQuery-div").each(function () {
                initAssignedToValues($(this));
                enableDisableAssignedTo($(this).find(".sameAsRespParty"));
            });
        }

        //bind click event on delete buttons
        $("#queryRCAList").on('click', '.templateQueryDeleteButton', function () {
            deletedQueryRCA = $(this).attr('data-id');
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

            if ($('.templateQuery-div:visible').length == 1) { // only one QueryRCA left
                removeSectionCloseButton($('.templateQuery-div:visible'));
            }
            updateQueryRCALineItem(deletedQueryRCA);
            if (typeof list != 'undefined') {
                delete list[deletedQueryRCA];
            }
        });

        if ($('.templateQuery-div:visible').length == 1) { // only one QueryRCA left
            removeSectionCloseButton($('.templateQuery-div:visible'));
        }

        $(".addQueryRCALineItemButton").on('click', addQueryRCALineItem);
        $(".copyQueryRCALineItemButton").on('click', copyQueryRCALineItem);
    };

    var addQueryRCALineItem = function () {
        createQueryRCALineItem(false);
    };

    var copyQueryRCALineItem = function () {
        createQueryRCALineItem(true);
    };

    var createQueryRCALineItem = function (copyValuesFromPreviousSection) {
        var clone = $("#queryRCA_clone").clone();
        var htmlId = 'queriesRCA[' + queryRCAChildCount + '].';

        var queryRCADeleteButton = clone.find("i[id$=deleteButton]");
        var query = clone.find("select[id$=query]");
        var operator = clone.find("select[id$=operator]");

        var dateRangeEnum = clone.find("select[id$=dateRangeInformationForQueryRCA\\.dateRangeEnum]");
        var datePickerFromDiv = clone.find("div[id$=datePickerFromDiv]");
        var datePickerToDiv = clone.find("div[id$=datePickerToDiv]");

        var dateRangeStartAbsolute = clone.find("input[id$=dateRangeInformationForQueryRCA\\.dateRangeStartAbsolute]");
        var dateRangeEndAbsolute = clone.find("input[id$=dateRangeInformationForQueryRCA\\.dateRangeEndAbsolute]");

        var relativeDateRangeValue = clone.find("input[id$=dateRangeInformationForQueryRCA\\.relativeDateRangeValue]");

        var lateId = clone.find("select[id$=lateId]");
        var rootCauseId = clone.find("select[id$=rootCauseId]");
        var rootCauseCustomExp = clone.find("input[id$=rcCustomExpression]");
        var rootCauseClassId = clone.find("select[id$=rootCauseClassId]");
        var rootCauseClassCustomExp = clone.find("select[id$=rcClassCustomExp]");
        var rootCauseSubCategoryId = clone.find("select[id$=rootCauseSubCategoryId]");
        var rootCauseSubCategoryCustomExp = clone.find("select[id$=rcSubCatCustomExp]");
        var responsiblePartyId = clone.find("select[id$=responsiblePartyId]");
        var responsiblePartyCustomExp = clone.find("input[id$=rpCustomExpression]");
        var assignedToUser = clone.find("select[id$=assignedToUser]");
        var assignedToGroup = clone.find("select[id$=assignedToGroup]");
        var sameAsRespParty = clone.find("input[id$=sameAsRespParty]");
        var actions = clone.find("textarea[id$=actions]");
        var summary = clone.find("textarea[id$=summary]");
        var investigation = clone.find("textarea[id$=investigation]");
        var summarySql = clone.find("textarea[id$=summarySql]");
        var actionsSql = clone.find("textarea[id$=actionsSql]");
        var investigationSql = clone.find("textarea[id$=investigationSql]");

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

        queryRCADeleteButton.attr('id', htmlId + 'deleteButton')
            .attr('name', htmlId + 'deleteButton')
            .attr('data-id',queryRCAChildCount);

        relativeDateRangeValue.attr('id', htmlId + 'dateRangeInformationForQueryRCA\.relativeDateRangeValue')
            .attr('name', htmlId + 'dateRangeInformationForQueryRCA\.relativeDateRangeValue');

        dateRangeEnum.attr('id', htmlId + 'dateRangeInformationForQueryRCA\.dateRangeEnum')
            .attr('name', htmlId + 'dateRangeInformationForQueryRCA\.dateRangeEnum');

        datePickerFromDiv.attr('id', htmlId + 'datePickerFromDiv');
        datePickerToDiv.attr('id', htmlId + 'datePickerToDiv');

        dateRangeStartAbsolute.attr('id', htmlId + 'dateRangeInformationForQueryRCA\.dateRangeStartAbsolute')
            .attr('name', htmlId + 'dateRangeInformationForQueryRCA\.dateRangeStartAbsolute');

        dateRangeEndAbsolute.attr('id', htmlId + 'dateRangeInformationForQueryRCA\.dateRangeEndAbsolute')
            .attr('name', htmlId + 'dateRangeInformationForQueryRCA\.dateRangeEndAbsolute');

        lateId.attr('id', htmlId + 'lateId')
            .attr('name', htmlId + 'lateId');

        rootCauseId.attr('id', htmlId + 'rootCauseId')
            .attr('name', htmlId + 'rootCauseId');

        rootCauseCustomExp.attr('id', htmlId + 'rcCustomExpression')
            .attr('name', htmlId + 'rcCustomExpression');

        rootCauseClassId.attr('id', htmlId + 'rootCauseClassId')
            .attr('name', htmlId + 'rootCauseClassId');

        rootCauseClassCustomExp.attr('id', htmlId + 'rcClassCustomExp')
            .attr('name', htmlId + 'rcClassCustomExp');

        rootCauseSubCategoryId.attr('id', htmlId + 'rootCauseSubCategoryId')
            .attr('name', htmlId + 'rootCauseSubCategoryId');

       rootCauseSubCategoryCustomExp.attr('id', htmlId + 'rcSubCatCustomExp')
            .attr('name', htmlId + 'rcSubCatCustomExp');

        responsiblePartyId.attr('id', htmlId + 'responsiblePartyId')
            .attr('name', htmlId + 'responsiblePartyId');

        responsiblePartyCustomExp.attr('id', htmlId + 'rpCustomExpression')
            .attr('name', htmlId + 'rpCustomExpression');

        assignedToUser.attr('id', htmlId + 'assignedToUser')
            .attr('name', htmlId + 'assignedToUser');
        assignedToGroup.attr('id', htmlId + 'assignedToGroup')
            .attr('name', htmlId + 'assignedToGroup');
        sameAsRespParty.attr('id', htmlId + 'sameAsRespParty')
            .attr('name', htmlId + 'sameAsRespParty');
        sameAsRespParty.parent().find("label").attr("for",htmlId + 'sameAsRespParty' );
        actions.attr('id', htmlId + 'actions').attr('name', htmlId + 'actions');
        summary.attr('id', htmlId + 'summary').attr('name', htmlId + 'summary');
        investigation.attr('id', htmlId + 'investigation').attr('name', htmlId + 'investigation');
        investigationSql.attr('id', htmlId + 'investigationSql').attr('name', htmlId + 'investigationSql');
        actionsSql.attr('id', htmlId + 'actionsSql').attr('name', htmlId + 'actionsSql');
        summarySql.attr('id', htmlId + 'summarySql').attr('name', htmlId + 'summarySql');
        clone.attr('id', 'queryRCA' + queryRCAChildCount);

        // Use select2 change event for IE11
        bindQuerySelect2(query).on("select2:select", function (e) {
            selectQueryOnChange(this);
        });

        operator.select2();

        // Use select2 change event for IE11
        dateRangeEnum.select2().on("select2:select", function (e) {
            daraRangeEnumClassOnChange(this);
        });

        lateId.select2();
        rootCauseId.select2();
        rootCauseCustomExp.parents(".templateQuery-div").find(".hideShowRCField").addClass('hidden');
        rootCauseClassId.select2();
        rootCauseSubCategoryId.select2();
        responsiblePartyId.select2();
        responsiblePartyCustomExp.parents(".templateQuery-div").find(".hideShowRPField").addClass('hidden');
        assignedToUser.select2();


        $("#queryRCAList").append(clone);

        if (copyValuesFromPreviousSection) {

            var lastVisibleQueryRCAChildIndex = 0;
            for (var i = 0; i < queryRCAChildCount; i++) {
                if ($("#queriesRCA\\[" + i + "\\]\\.dynamicFormEntryDeleted").val() == "false")
                    lastVisibleQueryRCAChildIndex = i;
            }
            var currentIndex = queryRCAChildCount;

            var queryContainer = getQueryWrapperRow(query);
            var expressionValues = getExpressionValues(queryContainer);
            $(expressionValues).on("loadBlankAndCustomSqlFieldsComplete", function () {
                copyBlankAndCustomSqlFields(lastVisibleQueryRCAChildIndex, currentIndex);
            });

            var previousSectionPrefix = "queriesRCA\\[" + lastVisibleQueryRCAChildIndex + "\\]\\.";

            query.attr("data-value", $("#" + previousSectionPrefix + "query").attr("data-value")).trigger("change");
            query.one("change",function(){$(expressionValues).off("loadBlankAndCustomSqlFieldsComplete")});
            operator.val($("#" + previousSectionPrefix + "operator").val()).trigger("change");
            dateRangeEnum.val($("#" + previousSectionPrefix + "dateRangeInformationForQueryRCA\\.dateRangeEnum").val()).trigger("change");
            dateRangeStartAbsolute.val($("#" + previousSectionPrefix + "dateRangeInformationForQueryRCA\\.dateRangeStartAbsolute").val());
            dateRangeEndAbsolute.val($("#" + previousSectionPrefix + "dateRangeInformationForQueryRCA\\.dateRangeEndAbsolute").val());
            relativeDateRangeValue.val($("#" + previousSectionPrefix + "dateRangeInformationForQueryRCA\\.relativeDateRangeValue").val());

            lateId.val($("#" + previousSectionPrefix + "lateId").val()).trigger('change');
            updateEditRootCause(lateId.parents(".templateQuery-div"), lateId.val());
            if ($("#" + previousSectionPrefix + "rootCauseId").hasClass('hidden')) {
                rootCauseCustomExp.val($("#" + previousSectionPrefix + "rcCustomExpression").val()).trigger('change');
            } else {
                rootCauseId.val($("#" + previousSectionPrefix + "rootCauseId").val()).trigger('change');
                updateEditResponsibleParty(rootCauseId.parents(".templateQuery-div"), rootCauseId.val());
            }
            if ($("#" + previousSectionPrefix + "rootCauseClassId").hasClass('hidden')) {
                rootCauseClassCustomExp.val($("#" + previousSectionPrefix + "rcClassCustomExp").val()).trigger('change');
            } else {
                rootCauseClassId.val($("#" + previousSectionPrefix + "rootCauseClassId").val()).trigger('change');
            }
            if ($("#" + previousSectionPrefix + "rootCauseSubCategoryId").hasClass('hidden')) {
                rootCauseClassCustomExp.val($("#" + previousSectionPrefix + "rcSubCatCustomExp").val()).trigger('change');
            } else {
                rootCauseSubCategoryId.val($("#" + previousSectionPrefix + "rootCauseSubCategoryId").val()).trigger('change');
            }
            if ($("#" + previousSectionPrefix + "responsiblePartyId").hasClass('hidden')) {
                responsiblePartyCustomExp.val($("#" + previousSectionPrefix + "rpCustomExpression").val()).trigger('change');
            } else {
                responsiblePartyId.val($("#" + previousSectionPrefix + "responsiblePartyId").val()).trigger('change');
            }
            sameAsRespParty.attr("checked", $("#" + previousSectionPrefix + "sameAsRespParty").is(":checked")).trigger("change");
            assignedToUser.val($("#" + previousSectionPrefix + "assignedToUser").val());
            assignedToGroup.val($("#" + previousSectionPrefix + "assignedToGroup").val());
            assignedToUser.attr("data-value", $("#" + previousSectionPrefix + "assignedToUser").val());
            assignedToGroup.attr("data-value", $("#" + previousSectionPrefix + "assignedToGroup").val());
            actions.val($("#" + previousSectionPrefix + "actions").val());
            summary.val($("#" + previousSectionPrefix + "summary").val());
            investigation.val($("#" + previousSectionPrefix + "investigation").val());
            summarySql.val($("#" + previousSectionPrefix + "summarySql").val());
            investigationSql.val($("#" + previousSectionPrefix + "investigationSql").val());
            actionsSql.val($("#" + previousSectionPrefix + "actionsSql").val());
            showHideCustomSql([rootCauseCustomExp, rootCauseClassCustomExp, rootCauseClassCustomExp, responsiblePartyCustomExp, summarySql, investigationSql, actionsSql]);
        }
        initAssignedToValues(clone);

        function showHideCustomSql(sqlFields) {
            for (var i = 0; i < sqlFields.length; i++) {
                var sqlField = sqlFields[i];

                if (sqlField.val()) {
                    var parentDiv = sqlField.parent()
                    parentDiv.find(".inputField").addClass('hidden');
                    parentDiv.find(".customField").removeClass('hidden');
                }
            }
        }

        clone.show();

        queryRCAChildCount++;

        if ($('.templateQuery-div:visible').length > 1) {
            $.each($('.templateQuery-div:visible'), function() {
                // $($(this).find('.queryRCADeleteButton')).show();
                $($(this).find('.templateQueryDeleteButton')).closest("div").show();
            })
        }

    };

    var copyBlankAndCustomSqlFields = function (lastVisibleQueryRCAChildIndex, currentIndex) {
        var blankAndCustomSqlFields = $("[name^='queryRCA" + lastVisibleQueryRCAChildIndex + ".'][name$='].value']," +
            "[name^='queryRCA" + lastVisibleQueryRCAChildIndex + ".'][name$='].copyPasteValue']");
        _.each(blankAndCustomSqlFields, function (it) {
            var current = $(it);
            var nameAttributeValueForCopyField = "queryRCA" + currentIndex + current.attr("name").substring(current.attr("name").indexOf("."));
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
    $("#queryRCAList").on("click", ".createQueryRCAButton", function () {
        var el = $(this);
        var url = el.attr("data-url");
        var message = el.attr("data-message");
        url += (url.indexOf("?") > -1) ? "&queryRCAIndex=" : "?queryRCAIndex=";
        url += getQueryRCAIndex(el);
        showWarningOrSubmit(url, message);
    });

    function getQueryRCAIndex(el) {
        var $currentQueryRCA = $(el).closest(".templateQuery-div");
        var num = 0;
        var queryRCAList = $("#queryRCAList").find(".templateQuery-div");
        for (var i = 0; i < queryRCAList.length; i++) {
            if ($(queryRCAList[i]).is($currentQueryRCA)) return num;
            if ($(queryRCAList[i]).find("input[id$=dynamicFormEntryDeleted]").val() == "false") num++;
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
            placeholder: 'queryRCA-placeholder',
            forceHelperSize: true,
            //https://stackoverflow.com/questions/10637095/jquery-ui-sortable-tolerance-option-not-working-as-expected
            sort: function (event, ui) {
                var that = $(this),
                    w = ui.helper.outerHeight();
                that.children().each(function () {
                    if ($(this).hasClass('ui-sortable-helper') || $(this).hasClass('ui-sortable-placeholder') || $(this).hasClass('queryRCA-placeholder'))
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
            $("#queryRCAList").children('div[id^="queryRCA"]').each(function (i) {
                $(this).find('[name*="queriesRCA"], [id*="queriesRCA"]').each(function () {
                    // Update the 'queriesRCA[0]' part of the name attribute to contain the latest count
                    if (typeof $(this).attr('name') !== 'undefined')
                        $(this).attr('name', $(this).attr('name').replace(/queriesRCA\[\d+\]/, 'queriesRCA[' + i + ']'));
                    if (typeof $(this).attr('id') !== 'undefined')
                        $(this).attr('id', $(this).attr('id').replace(/queriesRCA\[\d+\]/, 'queriesRCA[' + i + ']'));
                });
                //handling parameters values like queryRCA2.qev[0].value
                $(this).find('[name^="queryRCA"]', '[id^="queryRCA"]').each(function () {
                    // Update the 'queriesRCA[0]' part of the name attribute to contain the latest count
                    if (typeof $(this).attr('name') !== 'undefined')
                        $(this).attr('name', $(this).attr('name').replace(/queryRCA\d+/, 'queryRCA' + i));
                    if (typeof $(this).attr('id') !== 'undefined')
                        $(this).attr('id', $(this).attr('id').replace(/queryRCA\d+/, 'queryRCA' + i));
                });
                $(this).attr('id', $(this).attr('id').toString().replace(/queryRCA\d+/, 'queryRCA' + i));
            });
        });
    }

    $(document).on('click', ".saveModal", function () {
        $(".customExpressionErrorDiv").hide();
        if(validateCustomExpression($(".customExpressionQuery").val().toLowerCase())){
            var container = $(".containerName").val();
            var fieldName = $(".fieldName").val();
            var customExpressionQuery = $(".customExpressionQuery").val().replace(/\n/g, " ");
            if(customExpressionQuery != "") {
                if(fieldName == 'rcCustomExpression') {
                    handleFieldWithCustom($("#"+container), "selectRootCauseId", "hideShowRCField");
                }else if(fieldName == 'rcSubCatCustomExp') {
                    handleFieldWithCustom($("#"+container), "selectRootCauseSubCategoryId", "hideShowRCSCField");
                }else if(fieldName == 'rcClassCustomExp') {
                    handleFieldWithCustom($("#"+container), "selectRootCauseClassId", "hideShowRCCField");
                }else if(fieldName == 'rpCustomExpression') {
                    handleFieldWithCustom($("#"+container), "selectResponsiblePartyId", "hideShowRPField");
                }else if(fieldName == 'customSqlInputInvestigation') {
                    handleFieldWithCustom($("#"+container), "inputInvestigation", "hideShowFieldInvestigation");
                }else if(fieldName == 'customSqlInputSummary') {
                    handleFieldWithCustom($("#"+container), "inputSummary", "hideShowFieldSummary");
                }else if(fieldName == 'customSqlInputActions') {
                    handleFieldWithCustom($("#"+container), "inputActions", "hideShowFieldActions");
                }
                $("#"+container).find('.'+fieldName).val(customExpressionQuery);
                $(".customExpressionQuery").val('');
                $(".customExpressionModal").modal('hide');
            } else {
                $(".customExpressionErrorDiv").html(
                    '<button type="button" class="close closeError" ' +
                    'data-dismiss="alert" aria-hidden="true">' +
                    '&times;' +
                    '</button>' +
                    $.i18n._("modal.blank.textarea.save.error")
                );
                $(".customExpressionErrorDiv").show();
            }
        }else{
            $(".customExpressionErrorDiv").show();
        }
    });

    function handleFieldWithCustom(element, className, hideShowFieldClass) {
        element.find("."+className).val(null).trigger("change");
        element.find("."+className).addClass('hidden');
        element.find("."+hideShowFieldClass).removeClass('hidden');
    }

    function validateCustomExpression(query){
        return !(SQL_DML_PATTERN_REGEX.test(query));
    }

    $(".customExpressionQuery").attr("maxlength", 32000);

    function initAssignedToValues(container) {

        let userSelect = $(container).find(".selectAssignedToUser")
        let groupSelect = $(container).find(".selectAssignedToGroup")
        if (userSelect.hasClass('select2-hidden-accessible')) userSelect.select2("destroy");
        if (groupSelect.hasClass('select2-hidden-accessible')) groupSelect.select2("destroy");

        bindShareWith(userSelect, sharedWithUserListUrl, sharedWithValuesUrl, "100%", true, $('body'), "placeholder.selectUsers"
        ).on("change", function () {
            groupSelect.attr("data-extraParam", JSON.stringify({user: $(this).val()}));
            groupSelect.data('select2').results.clear()
        });
        bindShareWith(groupSelect, sharedWithGroupListUrl, sharedWithValuesUrl, "100%", true, $('body'), "placeholder.selectGroup"
        ).on("change", function () {
            userSelect.attr("data-extraParam", JSON.stringify({userGroup: $(this).val()}));
            userSelect.data('select2').results.clear()
        });

        userSelect.attr("data-extraParam", JSON.stringify({userGroup: groupSelect.attr("data-value")}));
        groupSelect.attr("data-extraParam", JSON.stringify({user: userSelect.attr("data-value")}));
    }

    function enableDisableAssignedTo(checkbox) {
        let container = $(checkbox).closest(".templateQuery-div");
        if ($(checkbox).is(":checked")) {
            container.find(".selectAssignedToUser").attr("disabled", true)
            container.find(".selectAssignedToGroup").attr("disabled", true)
        } else {
            container.find(".selectAssignedToUser").attr("disabled", false)
            container.find(".selectAssignedToGroup").attr("disabled", false)
        }
    }

    $(document).on("change", ".sameAsRespParty", function () {
        enableDisableAssignedTo($(this));
    });
});




function bindQuerySelect2(selector) {
    return bindSelect2WithUrl(selector, querySearchUrl, queryNameUrl, true);
}

function updateQueryRCALineItem(deletedQueryRCA){
    $(".templateQueryDeleteButton").each(function () {
        var id = parseInt($(this).attr('data-id'));
        if(id > parseInt(deletedQueryRCA)){
            $(this).attr('data-id',(id-1));
        }
    });

}

$(document).on('change', ".editLate", function () {
    updateEditRootCause($(this).parents(".templateQuery-div"), $(this).val());
});

function updateEditRootCause(container, lateValue) {
    var rootCauseOption = "<option value='' >"+$.i18n._('selectOne')+"</option>";
    var rootCauseClassOption = "<option value='' >"+$.i18n._('selectOne')+"</option>";
    if (lateValue != "") {
        var late = _.find(JSON.parse(lateList), function (e) {
            return e.id == lateValue;
        });
        var rootCauseJson = JSON.parse(rootCauseList);
        var rootCauseClassJson = JSON.parse(rootCauseClassList);

        for (var j = 0; j < rootCauseJson.length; j++) {
            if (_.indexOf(late.rootCauseIds, rootCauseJson[j]['id']) > -1) {
                if (rootCauseJson[j]['hiddenDate']) {
                    continue
                } else {
                    rootCauseOption = rootCauseOption + "<option value='" + rootCauseJson[j]['id'] + "' >" + rootCauseJson[j]['textDesc'] + "</option>";
                }
            }
        }
        for (var j = 0; j < rootCauseClassJson.length; j++) {
            if (_.indexOf(late.rootCauseClassIds, rootCauseClassJson[j]['id']) > -1) {
                if (rootCauseClassJson[j]['hiddenDate']) {

                }else{
                    rootCauseClassOption = rootCauseClassOption + "<option value='" + rootCauseClassJson[j]['id'] + "' >" + rootCauseClassJson[j]['textDesc'] + "</option>";
                }
            }
        }
    }
    container.find("[name=late]").val(lateValue);
    container.find("select.editRootCause").select2('destroy').html(rootCauseOption).select2({allowClear: true}).trigger("change");
    container.find("select.editRootCauseClass").select2('destroy').html(rootCauseClassOption).select2({allowClear: true}).trigger("change");

}


$(document).on('change', ".editRootCause", function () {
    updateEditResponsibleParty($(this).parents(".templateQuery-div"), $(this).val());
});
function updateEditResponsibleParty(container, rootCauseValue){

    var respPartyOption = "<option value='' >"+$.i18n._('selectOne')+"</option>";
    var rootCauseSubCategoryOption = "<option value='' >"+$.i18n._('selectOne')+"</option>";
    var respPartyJson = JSON.parse(responsiblePartyList);
    var rootCauseSubCategoryJson = JSON.parse(rootCauseSubCategoryList);
    var rootCause = _.find(JSON.parse(rootCauseList), function (e) {
        return e.id == rootCauseValue
    });
    for (var j = 0; j < respPartyJson.length; j++) {
        if (rootCause && (_.indexOf(rootCause.responsiblePartyIds, respPartyJson[j]['id']) > -1)) {
            if (respPartyJson[j]['hiddenDate'] == null) {
                respPartyOption = respPartyOption + "<option value='" + respPartyJson[j]['id'] + "' >" + respPartyJson[j]['textDesc'] + "</option>";
            }
        }
    }
    for (var j = 0; j < rootCauseSubCategoryJson.length; j++) {
        if (rootCause && (_.indexOf(rootCause.rootCauseSubCategoryIds, rootCauseSubCategoryJson[j]['id']) > -1)) {
            if (rootCauseSubCategoryJson[j]['hiddenDate'] == null) {
                rootCauseSubCategoryOption = rootCauseSubCategoryOption + "<option value='" + rootCauseSubCategoryJson[j]['id'] + "' >" + rootCauseSubCategoryJson[j]['textDesc'] + "</option>";
            }
        }
    }
    var editResponsibleParty = container.find("select.editResponsibleParty")
    editResponsibleParty.select2('destroy').html(respPartyOption).select2({allowClear: true}).trigger("change");
    var editRootCauseSubCategory = container.find("select.editRootCauseSubCategory")
    editRootCauseSubCategory.select2('destroy').html(rootCauseSubCategoryOption).select2({allowClear: true}).trigger("change");
}

$(document).on('click', ".pencilOptionSelectedInRootCause", function () {
    var container = $(this).parents(".templateQuery-div");
    $(".customExpressionErrorDiv").hide();
    if (container.find(".hideShowRCField").hasClass('hidden')) {
        container.find(".rcCustomExpression").val('');
        $(".containerName").val(container[0].id);
        $(".fieldName").val('rcCustomExpression');
        $(".customExpressionQuery").val('');
        $(".customExpressionModal").modal('show');
    }else{
        $(".containerName").val(container[0].id);
        $(".fieldName").val('rcCustomExpression');
        $(".customExpressionQuery").val(container.find('.rcCustomExpression').val());
        $(".customExpressionModal").modal('show');
    }
});

$(document).on('click', ".editInvestigationCustomSq", function () {
    initModal(this, "Investigation");
});

$(document).on('click', ".editSummaryCustomSq", function () {
    initModal(this, "Summary");
});

$(document).on('click', ".editActionsCustomSq", function () {
    initModal(this, "Actions");
});

$(document).on('click', ".changeTextToInputInvestigation", function () {
    initClear(this, "Investigation")
});

$(document).on('click', ".changeTextToInputActions", function () {
    initClear(this, "Actions")
});

$(document).on('click', ".changeTextToInputSummary", function () {
    initClear(this, "Summary")
});

function initModal(icon, field) {
    var container = $(icon).parents(".templateQuery-div");
    $(".customExpressionErrorDiv").hide();
    if (container.find(".hideShowField" + field).hasClass('hidden')) {
        container.find(".customSqlInput" + field).val('');
        $(".containerName").val(container[0].id);
        $(".fieldName").val('customSqlInput' + field);
        $(".customExpressionQuery").val('');
        $(".customExpressionModal").modal('show');
    } else {
        $(".containerName").val(container[0].id);
        $(".fieldName").val('customSqlInput' + field);
        $(".customExpressionQuery").val(container.find('.customSqlInput' + field).val());
        $(".customExpressionModal").modal('show');
    }
}

function initClear(icon, field) {
    var container = $(icon).parents(".templateQuery-div");
    if (container.find(".input" + field).hasClass('hidden')) {
        container.find(".customSqlInput" + field).val('');
        container.find(".hideShowField" + field).addClass('hidden');
        container.find(".input" + field).removeClass('hidden');
    }
}

$(document).on('click', ".changeRCTextToSelect", function () {
    var container = $(this).parents(".templateQuery-div");
    if (container.find(".selectRootCauseId").hasClass('hidden')) {
        container.find(".rcCustomExpression").val('');
        container.find(".hideShowRCField").addClass('hidden');
        container.find(".selectRootCauseId").val(null).trigger("change");
        container.find(".selectRootCauseId").removeClass('hidden');
    }
});

$(document).on('click', ".pencilOptionSelectedInRespParty", function () {

    var container = $(this).parents(".templateQuery-div");
    $(".customExpressionErrorDiv").hide();
    if (container.find(".hideShowRPField").hasClass('hidden')) {
        container.find(".rpCustomExpression").val('');
        $(".containerName").val(container[0].id);
        $(".fieldName").val('rpCustomExpression');
        $(".customExpressionQuery").val('');
        $(".customExpressionModal").modal('show');
    }else{
        $(".containerName").val(container[0].id);
        $(".fieldName").val('rpCustomExpression');
        $(".customExpressionQuery").val(container.find('.rpCustomExpression').val());
        $(".customExpressionModal").modal('show');

    }
});

$(document).on('click', ".changeRPTextToSelect", function () {
    var container = $(this).parents(".templateQuery-div");
    if (container.find(".selectResponsiblePartyId").hasClass('hidden')) {
        container.find(".rpCustomExpression").val('');
        container.find(".hideShowRPField").addClass('hidden');
        container.find(".selectResponsiblePartyId").val(null).trigger("change");
        container.find(".selectResponsiblePartyId").removeClass('hidden');
    }
});


$(document).on('click', ".pencilOptionSelectedInRCSubCat", function () {
    var container = $(this).parents(".templateQuery-div");
    $(".customExpressionErrorDiv").hide();
    if (container.find(".hideShowRCSCField").hasClass('hidden')) {
        container.find(".rcSubCatCustomExp").val('');
        $(".containerName").val(container[0].id);
        $(".fieldName").val('rcSubCatCustomExp');
        $(".customExpressionQuery").val('');
        $(".customExpressionModal").modal('show');
    }else{
        $(".containerName").val(container[0].id);
        $(".fieldName").val('rcSubCatCustomExp');
        $(".customExpressionQuery").val(container.find('.rcSubCatCustomExp').val());
        $(".customExpressionModal").modal('show');
    }
});

$(document).on('click', ".changeRCSCTextToSelect", function () {
    var container = $(this).parents(".templateQuery-div");
    if (container.find(".selectRootCauseSubCategoryId").hasClass('hidden')) {
        container.find(".rcSubCatCustomExp").val('');
        container.find(".hideShowRCSCField").addClass('hidden');
        container.find(".selectRootCauseSubCategoryId").val(null).trigger("change");
        container.find(".selectRootCauseSubCategoryId").removeClass('hidden');
    }
});

$(document).on('click', ".pencilOptionSelectedInRCClass", function () {

    var container = $(this).parents(".templateQuery-div");
    $(".customExpressionErrorDiv").hide();
    if (container.find(".hideShowRCCField").hasClass('hidden')) {
        container.find(".rcClassCustomExp").val('');
        $(".containerName").val(container[0].id);
        $(".fieldName").val('rcClassCustomExp');
        $(".customExpressionQuery").val('');
        $(".customExpressionModal").modal('show');
    }else{
        $(".containerName").val(container[0].id);
        $(".fieldName").val('rcClassCustomExp');
        $(".customExpressionQuery").val(container.find('.rcClassCustomExp').val());
        $(".customExpressionModal").modal('show');

    }
});

$(document).on('click', ".changeRCCTextToSelect", function () {
    var container = $(this).parents(".templateQuery-div");
    if (container.find(".selectRootCauseClassId").hasClass('hidden')) {
        container.find(".rcClassCustomExp").val('');
        container.find(".hideShowRCCField").addClass('hidden');
        container.find(".selectRootCauseClassId").val(null).trigger("change");
        container.find(".selectRootCauseClassId").removeClass('hidden');
    }
});