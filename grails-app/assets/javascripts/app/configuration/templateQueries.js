var e2bDistPVGateway = "PV_GATEWAY";
var e2bDistFolder = "EXTERNAL_FOLDER";
var e2bDistPaperMail = "PAPER_MAIL";
var e2bDistEmail = "EMAIL";
var rowIndex = 0;
var templateQueryChildCount =0

// Ensure global function is properly declared
/* global bindSelect2WithUrl, querySearchUrl, queryNameUrl, templateSearchUrl, templateNameUrl */

function bindQuerySelect2(selector) {
    return bindSelect2WithUrl(selector, querySearchUrl, queryNameUrl, true);
}

function bindTemplateSelect2(selector) {
    return bindSelect2WithUrl(selector, templateSearchUrl, templateNameUrl, false);
}

$(function() {

    templateQueryChildCount = $("#templateQueryList").attr("data-counter");
    var deletedTemplateQuery;
    var sectionDiv = $("#templateQueryList");
    
    var withTitle = function (label) {
        return '<span title="' + label.text + '">' + label.text + '</span>';
    }
    var init = function () {
        if (templateQueryChildCount == 0) { // for create page
            addTemplateQueryLineItem();
        } else {
            // initialize select2 if any
            /* eslint-disable vue/no-ref-object-reactivity-loss */
            $.each($("#templateQueryList .selectTemplate"), function (index, item) {
                var $item = jQuery(item)
                bindTemplateSelect2($item).on("select2:select select2:unselect", function () {
                    selectTemplateOnChange(this, cioms1Id);
                    updateAttachmentTable();
                }).on("initCompete", function () {
                    updateAttachmentTable();
                });
            });
            $.each($("#templateQueryList .selectQuery"), function (index, item) {
                var $item = jQuery(item)
                bindQuerySelect2($item).on("select2:select select2:unselect", function () {
                    selectQueryOnChange(this);
                })
            });
            /* eslint-enable vue/no-ref-object-reactivity-loss */

            var selectQueryLevel = $("#templateQueryList").find(".selectQueryLevel").select2();
            $("#templateQueryList").find(".dateRangeEnumClass").select2();
            if (selectQueryLevel.prop("readonly")) {
                selectQueryLevel.prop("disabled", true);
            }
            $("#templateQueryList").find(".dateRangeEnumClass").select2().on("change", function (e) {
                daraRangeEnumClassOnChange(this);
            })
            $("#templateQueryList").find(".expressionField").select2();
            $("#templateQueryList").find(".expressionOp").select2();
            $("#templateQueryList").find(".expressionValueSelect").select2({separator: ";",templateSelection: withTitle});
            $("#templateQueryList").find(".authorizationType").select2({templateSelection: withTitle});
            $("#templateQueryList").find(".msgType").select2();
            $("#templateQueryList").find(".distributionChannelName").select2()
        }

        //bind click event on delete buttons
        $("#templateQueryList").on('click', '.templateQueryDeleteButton', function () {
            deletedTemplateQuery = $(this).attr('data-id');
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

            if ($('.templateQuery-div:visible').length == 1) { // only one templateQuery left
                removeSectionCloseButton($('.templateQuery-div:visible'));
            }
            rearrangePOIInputsCount();
            showHideSpecialFields();
            updateAttachmentTable(deletedTemplateQuery);
            updateTemplateQueryLineItem(deletedTemplateQuery);
            if (typeof list != 'undefined') {
                delete list[deletedTemplateQuery];
                if (list.includes(e2bDistPVGateway) || list.includes(e2bDistFolder)) {
                    $(".e2BConfiguration").show();
                    $(".e2bDistOutgoingFolder").show();
                    $(".e2bDistIncomingFolder").show();
                    $(".e2bDistReportFormat .required-indicator").show();
                } else if (list.includes(e2bDistEmail)) {
                    $(".e2BConfiguration").show();
                    $(".e2bDistOutgoingFolder").hide();
                    $(".e2bDistIncomingFolder").hide();
                    $(".e2bDistReportFormat .required-indicator").hide();
                } else {
                    $(".e2BConfiguration").hide();
                }
            }
            updateNumbers();
        });

        if ($('.templateQuery-div:visible').length == 1) { // only one templateQuery left
            removeSectionCloseButton($('.templateQuery-div:visible'));
        }

        $(".addTemplateQueryLineItemButton").on('click', addTemplateQueryLineItem);
        $(".copyTemplateQueryLineItemButton").on('click', copyTemplateQueryLineItem);
        $(".showHeaderFooterArea").on('click', showHeaderFooterTitle);
        $(".footerSelect").each(function () {
            var $input = $(this);
            $input.autocomplete({
                classes: {"ui-autocomplete": "template-footer-autocomplete"},
                source: $input.data("select"), minLength: 0,
                open: function () {
                    $(".template-footer-autocomplete").css("max-width", $input.outerWidth());
                }
            });
        });
        updateNumbers();
        $(".collapseSectionForFromTemplate").click();
    };

    var updateNumbers = function () {
        $.each($('.templateQuery-div:visible'), function (index) {
            $(this).find('.sectionCounterNumber').html((index + 1));
        });
    }

    var addTemplateQueryLineItem = function () {
        createTemplateQueryLineItem(false);
    };

    var copyTemplateQueryLineItem = function () {
        createTemplateQueryLineItem(true);
    };

    var createTemplateQueryLineItem = function (copyValuesFromPreviousSection) {
        var clone = $("#templateQuery_clone").clone();
        var htmlId = 'templateQueries[' + templateQueryChildCount + '].';

        var template = clone.find("select[id$=template]");
        var templateQueryDeleteButton = clone.find("i[id$=deleteButton]");
        var query = clone.find("select[id$=query]");
        var operator = clone.find("select[id$=operator]");
        var queryLevel = clone.find("select[id$=queryLevel]");
        var dateRangeEnum = clone.find("select[id$=dateRangeInformationForTemplateQuery\\.dateRangeEnum]");

        var datePickerFromDiv = clone.find("div[id$=datePickerFromDiv]");
        var datePickerToDiv = clone.find("div[id$=datePickerToDiv]");

        var dateRangeStartAbsolute = clone.find("input[id$=dateRangeInformationForTemplateQuery\\.dateRangeStartAbsolute]");
        var dateRangeEndAbsolute = clone.find("input[id$=dateRangeInformationForTemplateQuery\\.dateRangeEndAbsolute]");

        var relativeDateRangeValue = clone.find("input[id$=dateRangeInformationForTemplateQuery\\.relativeDateRangeValue]");

        var header = clone.find("input[id$=header]");
        var footer = clone.find("textarea[id$=footer]");
        var title = clone.find("input[id$=title]");
        var headerProductSelection = clone.find("input[id$=headerProductSelection]");
        var headerDateRange = clone.find("input[id$=headerDateRange]");
        var draftOnly = clone.find("input[id$=draftOnly]");
        var userGroup = clone.find("input[id$=userGroup]");
        var granularity = clone.find("select[id$=granularity]");
        var templtReassessDate = clone.find("input[id$=templtReassessDate]");
        var reassessListednessDate = clone.find("input[id$=reassessListednessDate]");
        var blindProtected = clone.find("input[id$=blindProtected]");
        var privacyProtected = clone.find("input[id$=privacyProtected]");
        var displayMedDraVersionNumber = clone.find("input[id$=displayMedDraVersionNumber]");
        var authorizationType = clone.find("select[id$=authorizationType]");
        var dueInDays = clone.find("input[id$=dueInDays]");
        var isExpedited = clone.find("input[id$=isExpedited]:checkbox");
        var msgType = clone.find("select[id$=msgType]");
        var distributionChannelName = clone.find("select[id$=distributionChannelName]");
        var subject = clone.find("input[id$=emailConfiguration\\.subject]");
        var to = clone.find("input[id$=emailConfiguration\\.to]");
        var cc = clone.find("input[id$=emailConfiguration\\.cc]");
        var body = clone.find("input[id$=emailConfiguration\\.body]");
        var deliveryReceipt = clone.find("input[id$=emailConfiguration\\.deliveryReceipt]");
        var showEmailConfiguration = clone.find("span[id=showEmailConfiguration-_clone]");
        var issueType = clone.find("input[id$=issueType]");
        var rootCause = clone.find("input[id$=rootCause]");
        var priority = clone.find("select[id$=priority]");
        var responsibleParty = clone.find("input[id$=responsibleParty]");
        var assignedToUser = clone.find("select[id$=assignedToUser]");
        var assignedToGroup = clone.find("select[id$=assignedToGroup]");
        var actions = clone.find("textarea[id$=actions]");
        var summary = clone.find("textarea[id$=summary]");
        var investigation = clone.find("textarea[id$=investigation]");
        var summarySql = clone.find("textarea[id$=summarySql]");
        var actionsSql = clone.find("textarea[id$=actionsSql]");
        var investigationSql = clone.find("textarea[id$=investigationSql]");

        blindProtected.prop("checked", false);
        privacyProtected.prop("checked", false);
        //cloning the hidden fields
        clone.find("input[id$=publisherTemplateSectionParameterValue]")
            .attr('id', htmlId + 'publisherTemplateSectionParameterValue')
            .attr('name', htmlId + 'publisherTemplateSectionParameterValue');
        clone.find("input[id$=userGroup]")
            .attr('id', htmlId + 'userGroup')
            .attr('name', htmlId + 'userGroup');
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

        template.attr('id', htmlId + 'template')
            .attr('name', htmlId + 'template');

        query.attr('id', htmlId + 'query')
            .attr('name', htmlId + 'query');

        operator.attr('id', htmlId + 'operator')
            .attr('name', htmlId + 'operator');

        queryLevel.attr('id', htmlId + 'queryLevel')
            .attr('name', htmlId + 'queryLevel');

        templateQueryDeleteButton.attr('id', htmlId + 'deleteButton')
            .attr('name', htmlId + 'deleteButton')
            .attr('data-id',templateQueryChildCount);

        relativeDateRangeValue.attr('id', htmlId + 'dateRangeInformationForTemplateQuery\.relativeDateRangeValue')
            .attr('name', htmlId + 'dateRangeInformationForTemplateQuery\.relativeDateRangeValue');

        dateRangeEnum.attr('id', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeEnum')
            .attr('name', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeEnum');

        datePickerFromDiv.attr('id', htmlId + 'datePickerFromDiv');
        datePickerToDiv.attr('id', htmlId + 'datePickerToDiv');

        dateRangeStartAbsolute.attr('id', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeStartAbsolute')
            .attr('name', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeStartAbsolute');

        dateRangeEndAbsolute.attr('id', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeEndAbsolute')
            .attr('name', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeEndAbsolute');

        header.attr('id', htmlId + 'header')
            .attr('name', htmlId + 'header');
        footer.attr('id', htmlId + 'footer')
            .attr('name', htmlId + 'footer');
        footer.autocomplete({
            classes: {"ui-autocomplete": "template-footer-autocomplete"},
            source: footer.data("select")
        });
        title.attr('id', htmlId + 'title')
            .attr('name', htmlId + 'title');
        headerProductSelection.attr('id', htmlId + 'headerProductSelection')
            .attr('name', htmlId + 'headerProductSelection');
        headerDateRange.attr('id', htmlId + 'headerDateRange')
            .attr('name', htmlId + 'headerDateRange');
        draftOnly.attr('id', htmlId + 'draftOnly')
            .attr('name', htmlId + 'draftOnly');
        //dueInDays.attr('id', htmlId + 'dueInDays')
         //    .attr('name', htmlId + 'dueInDays');
        userGroup.attr('id', htmlId + 'userGroup')
            .attr('name', htmlId + 'userGroup');
        granularity.attr('id', htmlId + 'granularity')
            .attr('name', htmlId + 'granularity');
        templtReassessDate.attr('id', htmlId + 'templtReassessDate')
            .attr('name', htmlId + 'templtReassessDate');
        reassessListednessDate.attr('id', htmlId + 'reassessListednessDate')
            .attr('name', htmlId + 'reassessListednessDate');
        blindProtected.attr('id', htmlId + 'blindProtected')
            .attr('name', htmlId + 'blindProtected');
        privacyProtected.attr('id', htmlId + 'privacyProtected')
            .attr('name', htmlId + 'privacyProtected');
        displayMedDraVersionNumber.attr('id', htmlId + 'displayMedDraVersionNumber')
            .attr('name', htmlId + 'displayMedDraVersionNumber');

        authorizationType.attr('id', htmlId + 'authorizationType')
            .attr('name', htmlId + 'authorizationType');

        dueInDays.attr('id', htmlId + 'dueInDays')
            .attr('name', htmlId + 'dueInDays');

        isExpedited.attr('id', htmlId + 'isExpedited')
            .attr('name', htmlId + 'isExpedited');

        msgType.attr('id', htmlId + 'msgType')
            .attr('name', htmlId + 'msgType');

        distributionChannelName.attr('id', htmlId + 'distributionChannelName').attr("data-idx", templateQueryChildCount)
            .attr('name', htmlId + 'distributionChannelName');

        subject.attr('id', htmlId + 'emailConfiguration.subject')
            .attr('name', htmlId + 'emailConfiguration.subject');

        to.attr('id', htmlId + 'emailConfiguration.to')
            .attr('name', htmlId + 'emailConfiguration.to');

        cc.attr('id', htmlId + 'emailConfiguration.cc')
            .attr('name', htmlId + 'emailConfiguration.cc');

        body.attr('id', htmlId + 'emailConfiguration.body')
            .attr('name', htmlId + 'emailConfiguration.body');

        deliveryReceipt.attr('id', htmlId + 'emailConfiguration.deliveryReceipt')
            .attr('name', htmlId + 'emailConfiguration.deliveryReceipt');
        issueType.attr('id', htmlId + 'issueType')
            .attr('name', htmlId + 'issueType');
        rootCause.attr('id', htmlId + 'rootCause')
            .attr('name', htmlId + 'rootCause');
        priority.attr('id', htmlId + 'priority')
            .attr('name', htmlId + 'priority');
        responsibleParty.attr('id', htmlId + 'responsibleParty')
            .attr('name', htmlId + 'responsibleParty');
        assignedToUser.attr('id', htmlId + 'assignedToUser')
            .attr('name', htmlId + 'assignedToUser');
        assignedToGroup.attr('id', htmlId + 'assignedToGroup')
            .attr('name', htmlId + 'assignedToGroup');
        actions.attr('id', htmlId + 'actions').attr('name', htmlId + 'actions');
        summary.attr('id', htmlId + 'summary').attr('name', htmlId + 'summary');
        investigation.attr('id', htmlId + 'investigation').attr('name', htmlId + 'investigation');
        investigationSql.attr('id', htmlId + 'investigationSql').attr('name', htmlId + 'investigationSql');
        actionsSql.attr('id', htmlId + 'actionsSql').attr('name', htmlId + 'actionsSql');
        summarySql.attr('id', htmlId + 'summarySql').attr('name', htmlId + 'summarySql');
        showEmailConfiguration.attr('id', 'showEmailConfiguration-'+templateQueryChildCount).attr("data-idx", templateQueryChildCount).attr("data-evt-clk", '{"method": "showEmailConfiguration", "params": []}')

        clone.attr('id', 'templateQuery' + templateQueryChildCount);

        /* Use select2 change event for IE11 */
        bindTemplateSelect2(template).on("select2:select select2:unselect", function (e) {
            selectTemplateOnChange(this, cioms1Id);
            updateAttachmentTable();
        });

        // Use select2 change event for IE11
        bindQuerySelect2(query).on("select2:select select2:unselect", function (e) {
            selectQueryOnChange(this);
        });
        operator.select2({templateSelection: withTitle});
        queryLevel.select2();
        // Use select2 change event for IE11
        dateRangeEnum.select2().on("change", function (e) {
            daraRangeEnumClassOnChange(this);
        });

        authorizationType.select2({templateSelection: withTitle});
        distributionChannelName.select2();

        $("#templateQueryList").append(clone);

        if (copyValuesFromPreviousSection) {

            var lastVisibleTemplateQueryChildIndex = 0;
            for (var i = 0; i < templateQueryChildCount; i++) {
                if ($("#templateQueries\\[" + i + "\\]\\.dynamicFormEntryDeleted").val() == "false")
                    lastVisibleTemplateQueryChildIndex = i;
            }
            var currentIndex = templateQueryChildCount;

            var queryContainer = getQueryWrapperRow(query);
            var expressionValues = getExpressionValues(queryContainer);
            $(expressionValues).on("loadBlankAndCustomSqlFieldsComplete", function () {
                copyBlankAndCustomSqlFields(lastVisibleTemplateQueryChildIndex, currentIndex);
            });

            var templateContainer = getTemplateContainer(template);
            var templateValues = getTemplateValues(templateContainer);
            $(templateValues).on("loadCustomSQLValuesForTemplateComplete", function () {
                copyBlankAndCustomSqlFields(lastVisibleTemplateQueryChildIndex, currentIndex);
            });

            var previousSectionPrefix = "templateQueries\\[" + lastVisibleTemplateQueryChildIndex + "\\]\\.";

            template.attr('data-value', $("#" + previousSectionPrefix + "template").val()).trigger("change");
            template.one("change",function(){
                $(templateValues).off("loadCustomSQLValuesForTemplateComplete")
                template.trigger("select2:select");
            });

            query.attr('data-value', $("#" + previousSectionPrefix + "query").val()).trigger("change");
            query.one("initCompete", function () {
                setTimeout(() => {
                    selectQueryOnChange(query);
                }, 0);
            }).one('select2:select select2:unselect', function () {
                $(expressionValues).off("loadBlankAndCustomSqlFieldsComplete");
            });
            operator.val($("#" + previousSectionPrefix + "operator").val()).trigger("change");
            queryLevel.val($("#" + previousSectionPrefix + "queryLevel").val()).trigger("change");
            dateRangeEnum.val($("#" + previousSectionPrefix + "dateRangeInformationForTemplateQuery\\.dateRangeEnum").val()).trigger("change");
            dateRangeStartAbsolute.val($("#" + previousSectionPrefix + "dateRangeInformationForTemplateQuery\\.dateRangeStartAbsolute").val());
            dateRangeEndAbsolute.val($("#" + previousSectionPrefix + "dateRangeInformationForTemplateQuery\\.dateRangeEndAbsolute").val());
            relativeDateRangeValue.val($("#" + previousSectionPrefix + "dateRangeInformationForTemplateQuery\\.relativeDateRangeValue").val());

            header.val($("#" + previousSectionPrefix + "header").val());
            footer.val($("#" + previousSectionPrefix + "footer").val());
            title.val($("#" + previousSectionPrefix + "title").val());
            headerProductSelection.prop("checked", $("#" + previousSectionPrefix + "headerProductSelection").prop("checked"));
            headerDateRange.prop("checked", $("#" + previousSectionPrefix + "headerDateRange").prop("checked"));
            draftOnly.prop("checked", $("#" + previousSectionPrefix + "draftOnly").prop("checked"));
            granularity.val($("#" + previousSectionPrefix + "granularity").val());
            templtReassessDate.val($("#" + previousSectionPrefix + "templtReassessDate").val()).trigger("change");
            reassessListednessDate.val($("#" + previousSectionPrefix + "reassessListednessDate").val()).trigger("change");
            blindProtected.prop("checked", $("#" + previousSectionPrefix + "blindProtected").prop("checked"));
            privacyProtected.prop("checked", $("#" + previousSectionPrefix + "privacyProtected").prop("checked"));
            displayMedDraVersionNumber.prop("checked", $("#" + previousSectionPrefix + "displayMedDraVersionNumber").prop("checked"));
            authorizationType.val($("#" + previousSectionPrefix + "authorizationType").val()).trigger("change");
            dueInDays.val($("#" + previousSectionPrefix + "dueInDays").val());
            issueType.val($("#" + previousSectionPrefix + "issueType").val());
            responsibleParty.val($("#" + previousSectionPrefix + "responsibleParty").val());
            rootCause.val($("#" + previousSectionPrefix + "rootCause").val());
            priority.val($("#" + previousSectionPrefix + "priority").val());
            actions.val($("#" + previousSectionPrefix + "actions").val());
            summary.val($("#" + previousSectionPrefix + "summary").val());
            investigation.val($("#" + previousSectionPrefix + "investigation").val());
            summarySql.val($("#" + previousSectionPrefix + "summarySql").val());
            investigationSql.val($("#" + previousSectionPrefix + "investigationSql").val());
            actionsSql.val($("#" + previousSectionPrefix + "actionsSql").val());
            assignedToUser.val($("#" + previousSectionPrefix + "assignedToUser").val());
            assignedToGroup.val($("#" + previousSectionPrefix + "assignedToGroup").val());
            assignedToUser.attr("data-value", $("#" + previousSectionPrefix + "assignedToUser").val());
            assignedToGroup.attr("data-value", $("#" + previousSectionPrefix + "assignedToGroup").val());
            if ($('#' + previousSectionPrefix + 'isExpedited:checkbox').is(':checked'))
                isExpedited.attr('checked', 'checked');
            msgType.val($("#" + previousSectionPrefix + "msgType").val()).trigger("change");
            distributionChannelName.val($("#" + previousSectionPrefix + "distributionChannelName").val()).trigger("change");
            subject.val($("#" + previousSectionPrefix + "emailConfiguration\\.subject").val());
            to.val($("#" + previousSectionPrefix + "emailConfiguration\\.to").val());
            cc.val($("#" + previousSectionPrefix + "emailConfiguration\\.cc").val());
            body.val($("#" + previousSectionPrefix + "emailConfiguration\\.body").val());
            deliveryReceipt.val($("#" + previousSectionPrefix + "emailConfiguration\\.deliveryReceipt").val());
            if (subject.val() != "" || body.val() != "" || to.val() != "" || cc.val() != "" || deliveryReceipt.val() != "") {
                $('#showEmailConfiguration-'+templateQueryChildCount+' img').attr({
                    src: '/reports/assets/icons/email-secure.png',
                    title: $.i18n._('app.configuration.addEmailConfigurationEdited.label')
                });
            } else {
                $('#showEmailConfiguration-'+templateQueryChildCount+' img').attr({
                    src: '/reports/assets/icons/email.png',
                    title: $.i18n._('app.configuration.addEmailConfiguration.label')
                });
            }
        }
        rootCause.closest(".rcaSection").trigger("update");
        clone.show();

        if (templateQueryChildCount != 0) {
            clone.find(".showHeaderFooterArea").on('click', showHeaderFooterTitle);
        }
        templateQueryChildCount++;

        if ($('.templateQuery-div:visible').length > 1) {
            $.each($('.templateQuery-div:visible'), function() {
                $($(this).find('.templateQueryDeleteButton')).closest("div").show();
            })
        }
        if(copyValuesFromPreviousSection){
            updateAttachmentTable();
        }

        if (distributionChannelName.val() == e2bDistPVGateway || distributionChannelName.val() == e2bDistFolder) {
            $(".e2BConfiguration").show();
            $(".e2bDistOutgoingFolder").show();
            $(".e2bDistIncomingFolder").show();
            $(".e2bDistReportFormat .required-indicator").show();
        } else if (distributionChannelName.val() == e2bDistEmail) {
            $(".e2BConfiguration").show();
            $(".e2bDistOutgoingFolder").hide();
            $(".e2bDistIncomingFolder").hide();
            $(".e2bDistReportFormat .required-indicator").hide();
        }else {
            $(".e2BConfiguration").hide();
        }
        updateNumbers();
    };

    var copyBlankAndCustomSqlFields = function (lastVisibleTemplateQueryChildIndex, currentIndex) {
        var blankAndCustomSqlFields = $("[name^='templateQuery" + lastVisibleTemplateQueryChildIndex + ".'][name$='].value']," +
            "[name^='templateQuery" + lastVisibleTemplateQueryChildIndex + ".'][name$='].copyPasteValue']");
        _.each(blankAndCustomSqlFields, function (it) {
            var current = $(it);
            var nameAttributeValueForCopyField = "templateQuery" + currentIndex + current.attr("name").substring(current.attr("name").indexOf("."));
            var fieldToCopy = $("[name='" + nameAttributeValueForCopyField + "']");
            if (current.val() && (fieldToCopy.length === 1 && _.isEmpty(fieldToCopy.val()))) {

                if (current.hasClass("expressionValueSelectAuto")) {
                    let val = Array.isArray(current.val()) ? current.val() : current.val().split(";")
                    _.each(val,function(singeValue){
                        fieldToCopy
                            .append(new Option(singeValue,singeValue))
                            .val(val)
                            .trigger('change');
                    });
                } else if (current.hasClass("expressionValueSelectNonCache")) {
                    let val = Array.isArray(current.val()) ? current.val() : current.val().split(";")
                    _.each(val,function(singeValue){
                        fieldToCopy
                            .append(new Option(singeValue,singeValue))
                            .val(val)
                            .trigger('change');
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

    /* Logic for hiding and showing the date picker and text box for relative date input for show and hide of Contents */
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
    $("#templateQueryList").on("click", ".createTemplateQueryButton", function () {
        var el = $(this);
        var url = el.attr("data-url");
        var message = el.attr("data-message");
        url += (url.indexOf("?") > -1) ? "&templateQueryIndex=" : "?templateQueryIndex=";
        url += getTemplateQueryIndex(el);
        showWarningOrSubmit(url, message);
    });

    function getTemplateQueryIndex(el) {
        var $currentTemplateQuery = $(el).closest(".templateQuery-div");
        var num = 0;
        var templateQueryList = $("#templateQueryList").find(".templateQuery-div");
        for (var i = 0; i < templateQueryList.length; i++) {
            if ($(templateQueryList[i]).is($currentTemplateQuery)) return num;
            if ($(templateQueryList[i]).find("input[id$=dynamicFormEntryDeleted]").val() == "false") num++;
        }
        return num;
    }

    $("#saveDistributionEmailConfiguration").on('click', function () {
        $("#body").val(tinyMCE.activeEditor.getContent());
        if (!validateModalOnSubmit()) {
            return false;
        }

        var body = $("#body");
        $("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.subject").val($("#subject").val());
        $("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.to").val($("#emailConfiguration\\.to").val());
        $("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.cc").val($("#emailConfiguration\\.cc").val());
        $("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.body").val(body.val());
        $("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.deliveryReceipt").val($("#deliveryReceiptCheckbox").prop('checked'));

        if ($("#subject").val() != "" || $("#body").val() != "" || $("#emailConfiguration\\.to").val() != null || $("#emailConfiguration\\.cc").val() != null || $("#deliveryReceipt").is(':checked') || (tinyMCE.activeEditor && tinyMCE.activeEditor.getContent().trim() !== "")) {
            $('#showEmailConfiguration-' + rowIndex + ' img').attr({
                src: '/reports/assets/icons/email-secure.png',
                title: $.i18n._('app.configuration.addEmailConfigurationEdited.label')
            });
        } else {
            $('#showEmailConfiguration-' + rowIndex + ' img').attr({
                src: '/reports/assets/icons/email.png',
                title: $.i18n._('app.configuration.addEmailConfiguration.label')
            });
        }

        $(this).attr('data-dismiss', 'modal');

        resetEmailModalInputs();
    });

    function validateModalOnSubmit() {
        var validate = false;
        $("div.alert").hide();
        $("#emailConfiguration\\.to").parent().removeClass('has-error');
        $("#subject").parent().removeClass('has-error');
        $("#body").parent().removeClass('has-error');
        var body = $("#body")
        if($("#emailConfiguration\\.to").val() != null && $("#subject").val().trim() != "" && body.val().trim() != "") {
            validate = true
        } else if($("#emailConfiguration\\.to").val() == null && $("#emailConfiguration\\.cc").val() == null && $("#subject").val().trim() == "" && body.val().trim() == ""){
            validate = true
        } else {
            $("div.alert").show();
            if ($("#emailConfiguration\\.to").val() == null) {
                $("#emailConfiguration\\.to").parent().addClass('has-error');
            }else{
                $("#emailConfiguration\\.to").parent().removeClass('has-error');
            }
            if ($("#subject").val() == "") {
                $("#subject").parent().addClass('has-error');
            }else{
                $("#subject").parent().removeClass('has-error');
            }
            if ($("#body").val() == "") {
                body.parent().addClass('has-error');
            }else{
                body.parent().removeClass('has-error');
            }
        }
        return validate;
    }

    $("#resetDistributionEmailConfiguration").on('click', function (e) {
        resetEmailModalInputs();

    });
    if ((typeof dashboardWidget == 'undefined') || !dashboardWidget) {
        sectionDiv.sortable({
            axis: 'y',
            cursor: "move",
            tolerance: 'pointer',
            handle: ".grab-icon",
            revert: 'invalid',
            placeholder: 'templateQuery-placeholder',
            forceHelperSize: true,
            //https://stackoverflow.com/questions/10637095/jquery-ui-sortable-tolerance-option-not-working-as-expected
            sort: function (event, ui) {
                var that = $(this),
                    w = ui.helper.outerHeight();
                that.children().each(function () {
                    if ($(this).hasClass('ui-sortable-helper') || $(this).hasClass('ui-sortable-placeholder') || $(this).hasClass('templateQuery-placeholder'))
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
            $("#templateQueryList").children('div[id^="templateQuery"]').each(function (i) {
                $(this).find('[name*="templateQueries"], [id*="templateQueries"]').each(function () {
                    // Update the 'templateQueries[0]' part of the name attribute to contain the latest count
                    if (typeof $(this).attr('name') !== 'undefined')
                        $(this).attr('name', $(this).attr('name').replace(/templateQueries\[\d+\]/, 'templateQueries[' + i + ']'));
                    if (typeof $(this).attr('id') !== 'undefined')
                        $(this).attr('id', $(this).attr('id').replace(/templateQueries\[\d+\]/, 'templateQueries[' + i + ']'));
                });
                //handling parameters values like templateQuery2.qev[0].value
                $(this).find('[name^="templateQuery"]', '[id^="templateQuery"]').each(function () {
                    // Update the 'templateQueries[0]' part of the name attribute to contain the latest count
                    if (typeof $(this).attr('name') !== 'undefined')
                        $(this).attr('name', $(this).attr('name').replace(/templateQuery\d+/, 'templateQuery' + i));
                    if (typeof $(this).attr('id') !== 'undefined')
                        $(this).attr('id', $(this).attr('id').replace(/templateQuery\d+/, 'templateQuery' + i));
                });
                $(this).attr('id', $(this).attr('id').toString().replace(/templateQuery\d+/, 'templateQuery' + i));
            });
            updateNumbers();
        });
    }

    $("#templateQueryList").on('click', "[data-evt-clk]", function (e) {
        e.preventDefault();
        const eventData = JSON.parse($(this).attr("data-evt-clk"));
        const methodName = eventData.method;
        const params = eventData.params;
        if (methodName == "viewEmailConfiguration") {
            viewEmailConfiguration(...params);
        }else if(methodName == "showEmailConfiguration") {
            var index = $(this).attr("data-idx");
            showEmailConfiguration(index);
        }
    });

    $("#templateQueryList").on('change', "[data-evt-change]", function () {
        const eventData = JSON.parse($(this).attr("data-evt-change"));
        const methodName = eventData.method;
        const params = eventData.params;
        var index = $(this).attr("data-idx");
        // Call the method from the eventHandlers object with the params
        if (methodName == 'distributionChannelChanged') {
            distributionChannelChanged(index);
        }
    });
});

function distributionChannelChanged(index){
    list = [];
    var templateQueryList = $("#templateQueryList").find(".templateQuery-div");
    for (var i = 0; i < templateQueryList.length; i++) {
        if($("#templateQueries\\[" + i + "\\]\\.dynamicFormEntryDeleted").val() == 'false'){
            list.push($("#templateQueries\\[" + i + "\\]\\.distributionChannelName").val());
        }
    }
    if (list.includes(e2bDistPVGateway) || list.includes(e2bDistFolder)) {
        $(".e2BConfiguration").show();
        $(".e2bDistOutgoingFolder").show();
        $(".e2bDistIncomingFolder").show();
        $(".e2bDistReportFormat .required-indicator").show();
    } else if (list.includes(e2bDistEmail)) {
        $(".e2BConfiguration").show();
        $(".e2bDistOutgoingFolder").hide();
        $(".e2bDistIncomingFolder").hide();
        $(".e2bDistReportFormat .required-indicator").hide();
    } else {
        $(".e2BConfiguration").hide();
    }
    $("#submissionDateFrom").val(null);
    var originalValue = $("#submissionDateFrom").data("original-value");
    if(originalValue) {
        $("#submissionDateFrom").removeData("original-value").removeAttr("data-original-value");
        $("#submissionDateFrom").val(originalValue);
    }
    showHideSubmissionDateFromDiv();
    if ($("#templateQueries\\[" + index + "\\]\\.distributionChannelName").val() == "EMAIL") {
        $('#showEmailConfiguration-'+index+' img').attr({
            src: '/reports/assets/icons/email.png',
            title: $.i18n._('app.configuration.addEmailConfiguration.label')
        });
        $("#showEmailConfiguration-"+index).show();
    }else {
        $("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.subject").val("");
        $("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.to").val("");
        $("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.cc").val("");
        $("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.body").val("");
        $("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.deliveryReceipt").val($("#deliveryReceiptCheckbox").removeProp('checked'));
        $("#showEmailConfiguration-" + index).hide();
    }
}

function showEmailConfiguration(index){
    if(index !== undefined) {
        rowIndex = index;
        resetEmailModalInputs();
        $("#subject").val($("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.subject").val());
        if ($("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.to").val()){
            $("#emailConfiguration\\.to").val($("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.to").val().split(',')).trigger('change');
        }
        if ($("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.cc").val()){
            $("#emailConfiguration\\.cc").val($("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.cc").val().split(',')).trigger('change');
        }
        $("#body").val(tinyMCE.activeEditor.setContent($("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.body").val()));
        $("#deliveryReceiptCheckbox").prop($("#templateQueries\\[" + index + "\\]\\.emailConfiguration\\.deliveryReceipt").prop('checked'));
        $("#emailConfigurationDistributionChannel").modal('show');
    }
}

$(document).on('shown.bs.modal', '#emailConfigurationDistributionChannel', function (event) {
    var emailConfigModal = $(event.target);
    setSelect2InputWidth($(emailConfigModal).find("select[id='emailConfiguration.to']"));
    setSelect2InputWidth($(emailConfigModal).find("select[id='emailConfiguration.cc']"));
});

function resetEmailModalInputs() {
    $("div.alert").hide();
    $("#emailConfiguration\\.to").parent().removeClass('has-error');
    $("#subject").parent().removeClass('has-error');
    $("#body").parent().addClass('has-error');
    $("#emailConfiguration\\.to").val("").trigger('change');
    $("#emailConfiguration\\.cc").val("").trigger('change');
    $("#subject").val("");
    $("#body").val(tinyMCE.activeEditor.setContent(""));
    $("#deliveryReceiptCheckbox").removeProp('checked');
}

function viewEmailConfiguration(id) {
    rowIndex = parseInt(id.replace("showEmailConfiguration-", ""));
    $("#subject").val($("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.subject").val());
    if ($("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.to").val()){
        $("#emailConfiguration\\.to").val($("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.to").val().split(',')).trigger('change');
    }
    if ($("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.cc").val()){
        $("#emailConfiguration\\.cc").val($("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.cc").val().split(',')).trigger('change');
    }
    $("#body").val(tinyMCE.activeEditor.setContent($("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.body").val()));
    $("#deliveryReceiptCheckbox").prop($("#templateQueries\\[" + rowIndex + "\\]\\.emailConfiguration\\.deliveryReceipt").prop('checked'));
    $("#emailConfigurationDistributionChannel").modal('show');
    $("#subject").attr("disabled", "disabled");
    $("#emailConfiguration\\.to").attr("disabled", "disabled");
    $("#emailConfiguration\\.cc").attr("disabled", "disabled");
    tinymce.activeEditor.mode.set('readonly');
    $("#deliveryReceiptCheckbox").attr("disabled", "disabled");
}

function updateAttachmentTable(deletedTemplateQuery) {
    if (typeof formAttachmentTable === "function") {
        formAttachmentTable(deletedTemplateQuery);
    }
}

function updateTemplateQueryLineItem(deletedTemplateQuery){
    $(".templateQueryDeleteButton").each(function () {
        var id = parseInt($(this).attr('data-id'));
        if(id > parseInt(deletedTemplateQuery)){
            $(this).attr('data-id',(id-1));
        }
    });

}

$(function () {
    if(typeof isForIcsrProfile !== 'undefined' && isForIcsrProfile == "true"){
        distributionChannelChanged();
    }
});