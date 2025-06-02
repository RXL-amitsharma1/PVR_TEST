<%@ page import="grails.util.Holders;com.rxlogix.enums.CommentTypeEnum;com.rxlogix.user.User;com.rxlogix.config.ExecutedPeriodicReportConfiguration;com.rxlogix.enums.ReportExecutionStatusEnum" %>
<g:if test="${configType && Holders.config.getProperty('report.edit.show',Boolean)}">
    <g:set var="userService" bean="userService"/>
    <g:set var="isEditable" value="${configurationInstance.isEditableBy(userService.getCurrentUser())}"/>
        <span class="editConfigLink pull-right">
        <g:if test="${isEditable}">
            <g:if test="${configType ==  com.rxlogix.config.ConfigTypes.CONFIGURATION}">
                <a href="#" class="ic-sm pv-ic pv-ic-hover" data-toggle="modal" data-target="#editConfigModal">
                    <i class="md md-pencil" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'default.button.edit.label')}"></i>
                </a>
            </g:if>
            <g:if test="${configType == com.rxlogix.config.ConfigTypes.PERIODIC_REPORT_CONFIGURATION}">
                <a href="#" class="ic-sm pv-ic pv-ic-hover" data-toggle="modal" data-target="#editConfigModal">
                    <i class="md md-pencil" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'default.button.edit.label')}"></i>
                </a>
            </g:if>
        </g:if>
    </span>
</g:if>

<g:showIfDmsServiceActive>
    <i class="pull-right md md-cloud md-lg rxmain-dropdown-settings"
       data-toggle="modal" data-target="#sendToDmsModal" data-action="sendToDms"
       data-id="${executedConfigurationInstance.id}"
       data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.reportActionType.SEND_TO_DMS')}"></i>
</g:showIfDmsServiceActive>

%{--<i id="saveButton" class="pull-right md md-file-download md-lg rxmain-dropdown-settings"
   data-toggle="modal" data-target="#saveAsModal" data-action="viewMultiTemplateReport"
   data-id="${executedConfigurationInstance.id}"
   data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'default.button.saveReportAs.label')}"></i>--}%
<span class="pull-right">
<a href="#" class="ic-sm pv-ic pv-ic-hover">
<i id="saveButton" class="glyphicon glyphicon-download-alt" style="font-size: 15px;"
   data-toggle="modal" data-target="#saveAsModal" data-action="viewMultiTemplateReport"
   data-id="${executedConfigurationInstance.id}"
   data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'default.button.saveReportAs.label')}">
</i>
</a>
</span>


%{--<a href="#" class="pull-right  rxmain-dropdown-settings commentModalTrigger"
   data-owner-id="${executedConfigurationInstance.id}" data-comment-type="${CommentTypeEnum.EXECUTED_CONFIGURATION}"
   data-toggle="modal" data-target="#commentModal">
    <g:renderAnnotateIcon comments="${executedConfigurationComments}"
                          style="font-size: 16px;margin-top:2px;padding-left: 5px " data-placement="left"
                          title="${message(code: "report.annotaion")}"/>
</a>--}%
<span class="pull-right">
<a href="#" class="max-width-configurtaion-comment commentModalTrigger ic-sm pv-ic pv-ic-hover"
   data-owner-id="${executedConfigurationInstance.id}" data-comment-type="${CommentTypeEnum.EXECUTED_CONFIGURATION}"
   data-toggle="modal" data-target="#commentModal">
    <g:renderAnnotateIcon comments="${executedConfigurationComments}"
                          data-placement="left"
                          title="${message(code: "report.annotaion")}"/>
</a>
</span>
<sec:ifAnyGranted roles="ROLE_CONFIGURATION_VIEW,ROLE_PERIODIC_CONFIGURATION_VIEW,ROLE_CASE_SERIES_VIEW">
<span class="pull-right">
    <a href="#" class="dropdown-toggle waves-effect waves-light"  data-toggle="dropdown" aria-expanded="false" >
        <i class="md md-share  ic-sm pv-ic pv-ic-hover" style="width: 20px" title="${message(code: "app.label.deliveryOptions")}"></i>
        <span class="sr-only">Toggle Dropdown</span>
    </a>
    <ul class="dropdown-menu dropdown-menu-right" role="menu" >
        <li role="presentation"><a role="menuitem" href="" id="${executedConfigurationInstance.id}" data-toggle="modal" data-target="#sharedWithModal"><g:message code="app.label.share"/></a>
        </li>
        <li role="presentation"><a role="menuitem" href="" id="${executedConfigurationInstance.id}" data-toggle="modal" data-target="#emailToModal"><g:message code="app.label.emailTo"/></a>
        </li>
    </ul>
</span>
</sec:ifAnyGranted>
<g:if test="${executedConfigurationInstance.instanceOf(ExecutedPeriodicReportConfiguration)}">

    <g:if test="${(executedConfigurationInstance.status != ReportExecutionStatusEnum.GENERATED_CASES) && !executedConfigurationInstance.hasReachedToFinal()}">
        <sec:ifAnyGranted roles="ROLE_PERIODIC_CONFIGURATION_VIEW">
            <span class="addSectionLink">
                <a data-url="${createLink(controller: 'periodicReport', action: "addSection", id: executedConfigurationInstance.id)}"
                   href="#" data-toggle="modal" data-target="#addSectionModal">
                    <i id="addButton" class="pull-right md md-add md-lg rxmain-dropdown-settings"
                       data-tooltip="tooltip" data-placement="bottom"
                       title="${message(code: "app.label.add.section")}"></i></a>
            </span>
        </sec:ifAnyGranted>
    </g:if>
</g:if>
<g:else>
    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <span class="addSectionLink pull-right">
            <a data-url="${createLink(controller: 'configuration', action: "addSection", id: executedConfigurationInstance.id)}"
               href="#" class="ic-sm pv-ic pv-ic-hover" data-toggle="modal" data-target="#addSectionModal">
                %{--<i id="addButton"
                   class="pull-right md md-add md-lg rxmain-dropdown-settings"
                   data-tooltip="tooltip"
                   data-placement="bottom"
                   title="${message(code: "app.label.add.section")}"></i>--}%
                <i id="addButton"
                   class="md md-add"
                   data-tooltip="tooltip"
                   data-placement="bottom"
                   title="${message(code: "app.label.add.section")}"></i>
            </a>
        </span>
    </sec:ifAnyGranted>
</g:else>
<button class="pull-right actionItemModalIcon" title="${message(code:"ownership.actionitem.label")}" data-exconfig-id="${executedConfigurationInstance.id}" style="width:70px;"></button>
<sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
    <a href="#" style="color: black;font-weight: bold;margin-right: 4px; margin-top: 3px" title="${message(code:"app.label.action.item.create")}" class="listMenuOptions createActionItem pull-right" data-exconfig-id="${executedConfigurationInstance.id}">+AI</a>
</sec:ifAnyGranted>
<button class="btn btn-default btn-xs pull-right workflowButtonEtalon"   title="${message(code:"app.label.workflow.appName")}" style="display:none;min-width: 100px; margin-left: 10px; margin-right: 10px" data-executed-config-id="${executedConfigurationInstance.id}" data-initial-state= "${executedConfigurationInstance.workflowState.name}" data-evt-clk='{"method": "openStateHistoryModal", "params": []}'>${executedConfigurationInstance.workflowState.name}</button>
<span class="workflowButtonContainer">
</span>
%{--Below form will be used by dms modal--}%
<g:form controller="report">
    <g:hiddenField name="executedConfigId"/>
    <g:render template="/report/includes/sendToDmsModal"/>
</g:form>

<script>
    var actionItemHostPage = "report";
    var hasAccessOnActionItem = false;
    <sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
    hasAccessOnActionItem = true;
    </sec:ifAnyGranted>
    $(document).on("click", ".createActionItem", function () {
        actionItem.actionItemModal.set_executed_report_id(${executedConfigurationInstance.id});
        actionItem.actionItemModal.init_action_item_modal(false, ${executedConfigurationInstance.instanceOf(ExecutedPeriodicReportConfiguration)?"PERIODIC_REPORT":"ADHOC_REPORT"});
    });

    $(document).on("click ", ".actionItemModalIcon", function () {
        actionItem.actionItemModal.set_executed_report_id(${executedConfigurationInstance.id});
        actionItem.actionItemModal.view_action_item_list(hasAccessOnActionItem, false, ${executedConfigurationInstance.instanceOf(ExecutedPeriodicReportConfiguration)?"PERIODIC_REPORT":"ADHOC_REPORT"});
    });
    var actionItemStatus = "${executedConfigurationInstance.getActionItemStatus()}"
    var clazz
    if (actionItemStatus) {
        switch (actionItemStatus) {
            case 'OVERDUE':
                clazz = "btn btn-danger btn-xs";
                break;
            case 'WAITING':
                clazz = "btn btn-warning btn-xs";
                break;
            default:
                clazz = "btn btn-success btn-xs";
                break;
        }
        $(".actionItemModalIcon").addClass(clazz);
        $(".actionItemModalIcon").text($.i18n._('app.actionItemGroupState.' + actionItemStatus));
    } else {
        $(".actionItemModalIcon").hide();
    }
    <g:if test ="${executedConfigurationInstance.instanceOf(ExecutedPeriodicReportConfiguration)}">
    var periodicActions = "${executedConfigurationInstance.workflowState?.reportActionsAsList?.join(",")}";
    initAllowedActions(periodicActions);

    function initAllowedActions(periodicActions) {
        $(".workflowButtonContainer").empty();
        var workflowButton = $(".workflowButtonEtalon").clone();
        workflowButton.removeClass("workflowButtonEtalon");
        workflowButton.addClass("workflowButton");
        workflowButton.show();
        $(".workflowButtonContainer").append(workflowButton);
        if (periodicActions) {
            var hasGeneratedCasesData =${executedConfigurationInstance.hasGeneratedCasesData};

            var actionButton = '<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret dropdown-toggle"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right action-menu" role="menu" style="min-width: 80px !important; font-size: 12px;"> ';

            var actionsObj = periodicActions.split(',');
            for (var index = 0; index < actionsObj.length; index++) {
                var localizedVal = $.i18n._("workFlowState.reportActionType." + actionsObj[index]);
                if (hasGeneratedCasesData == true && actionsObj[index] != "MARK_AS_SUBMITTED")
                    actionButton = actionButton + '<li role="presentation" class="generateCases"><a href="#" data-evt-clk=\'{\"method\": \"generateReport\", \"params\": []}\' url="' + periodicReportConfig.generateDraftUrl + '?id=${executedConfigurationInstance.id}&reportAction=' + actionsObj[index] + '" role="menuitem" class="listMenuOptions generateCases"  >' + localizedVal + '</a></li>';
                if (actionsObj[index] == 'MARK_AS_SUBMITTED')
                    actionButton = actionButton + '<li role="presentation" class="stateSpecificActions"><a role="menuitem" class="listMenuOptions markAsSubmitted" data-toggle="modal" id="${executedConfigurationInstance.id}" data-target="#reportSubmissionModal" href="#" data-url="' + periodicReportConfig.markAsSubmittedUrl + '?id=${executedConfigurationInstance.id}">' + localizedVal + '</a></li>'
            }
            actionButton += "</ul>"
            $(".workflowButton").wrap($('<div class="btn-group dropdown pull-right m-r-10" align="center"></div> '));
            $(".workflowButton").removeClass("pull-right");
            $(".workflowButton").css("margin-right", 0);
            $(".workflowButton").after($(actionButton));
        }
    }
    var generateReport = function (el) {
        var urlToHit = $(el).attr("url")
        $.ajax({
            url: urlToHit,
            success: function (result) {
                if (result.warning) {
                    warningNotification(result.message);
                    return
                }
                successNotification(result.message);
                // setTimeout(
                //     function () {
                //         location.reload();
                //     }, 1000);
            },
            error: function (err) {
                errorNotification("Server Error!");
            }
        });
    };
    $(function () {
        periodicReport.periodicReportList.submission_modal_after_load();
    });

    $(document).on("data-clk", function (event, elem) {
        const elemClkData = JSON.parse(elem.attributes["data-evt-clk"].value)
        const methodName = elemClkData.method;
        const params = elemClkData.params;
        if (methodName == "generateReport") {
            generateReport(elem);
        }
    });
    </g:if>
</script>