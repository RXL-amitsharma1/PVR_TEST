<%@ page import="com.rxlogix.config.WorkflowRule; com.rxlogix.enums.ReportActionEnum; com.rxlogix.enums.ConfigurationTypeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.Constants; com.rxlogix.enums.AssignmentRuleEnum" %>
<g:if test="${'mode' != 'create'}">
    <g:hiddenField name="id" value="${workflowRuleInstance?.id}" />
</g:if>
<g:hiddenField name="actionToExecute" value="${params.action}"/>

<div class="row form-group">
    <div class="col-lg-3">
        <label for="name"><g:message code="app.label.workflow.name" /><span class="required-indicator">*</span></label>
        <g:textField name="name" maxlength="${WorkflowRule.NAME_MAX_LENGTH}" value="${workflowRuleInstance?.name}"
        class="form-control workflowRuleField "/>
    </div>
    <div class="col-lg-3">
        <label for="name"><g:message code="app.label.workflow.reportType" /><span class="required-indicator">*</span></label>
        <g:select id="configurationTypeEnum" name="configurationTypeEnum"
                  from="${ViewHelper.getWorkflowConfigurationTypeI18n()}"
                  optionValue="display" optionKey="name"
                  value="${workflowRuleInstance?.configurationTypeEnum}"
                  noSelection="['':message(code:'reportType.noSelection.selectPrompt')]" class="form-control workflowRuleField"/>
    </div>
    <div class="col-lg-2">
        <label for="initialState"><g:message code="app.label.workflow.rule.initialState" />
            <span class="required-indicator">*</span></label>
       <g:select id="initialState" name="initialState" value="${workflowRuleInstance?.initialState?.id}"
                 from="${initialStates}" optionKey="id" optionValue="name"
                 noSelection="['': message(code:'initialState.noSelection.selectPrompt')]" class="form-control workflowRuleField"/>
    </div>
    <div class="col-lg-2">
        <label for="targetState"><g:message code="app.label.workflow.rule.targetState" />
            <span class="required-indicator">*</span></label>
        <g:select id="targetState" name="targetState" value="${workflowRuleInstance?.targetState?.id}"
                  from="${targetStates}" optionKey="id" optionValue="name"
                  noSelection="['': message(code:'targetState.noSelection.selectPrompt')]" class="form-control workflowRuleField"/>
    </div>
    <div class="col-lg-2">
        <div class="checkbox checkbox-primary">
            <g:checkBox name="needApproval" class="workflowRuleField" value="${workflowRuleInstance?.needApproval}"/>
            <label for="needApproval">
                <b><g:message code="app.label.workflow.rule.needApproval"/></b>
            </label>
        </div>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-3">
        <label for="description"><g:message code="app.label.workflow.description" /></label>
        <g:textField name="description" maxlength="${WorkflowRule.constrainedProperties.description.maxSize}" value="${workflowRuleInstance?.description}"
             class="form-control workflowRuleField"/>
    </div>
    <div class="col-lg-3">
        <label for="description"><g:message code="app.label.workflow.rule.defaultReportAction" /></label>
        <g:select id="defaultReportAction" name="defaultReportAction" value="${workflowRuleInstance?.defaultReportAction?.key}"
                  optionValue="display" from="${ViewHelper.getReportActionEnum()}" optionKey="name"
                  noSelection="['': message(code: 'workflowRule.defaultAction.noSelection.selectPrompt')]" class="form-control workflowRuleField"/>
    </div>
    <div class="col-lg-2">
        <div class="col-xs-12">
            <script>
                sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
                sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
                $(function () {
                    bindShareWith($('.executorsControl'), sharedWithListUrl, sharedWithValuesUrl, "100%");
                    $('#s2id_executorsControl').find(".select2-input").val($.i18n._("any.user"));
                    $('.executorsControl').attr("data-placeholder", $.i18n._("any.user"));
                });
            </script>
            <label><g:message code="app.label.workflow.rule.can.execute"/></label>
            <g:set var="executorValue" value="${(workflowRuleInstance?.executorGroups?.collect { Constants.USER_GROUP_TOKEN + it.id } + workflowRuleInstance?.executors?.collect { Constants.USER_TOKEN + it.id })?.join(";")}"/>
            <select class="executorsControl form-control workflowRuleField" id="executorsControl" name="canExecute" data-value="${executorValue}"></select>
        </div>
    </div>
    <div class="col-lg-2 pvcPvqRule">
        <label for="autoAssignmentRule"><g:message code="app.label.workflow.rule.autoAssignmentRule" />
            <span class="required-indicator">*</span></label>
        <div class="radio radio-primary">
            <input class="autoAssignmentRuleRadio workflowRuleField" id="BASIC_RULE" type="radio" name="assignmentRule_val">
            <label for="BASIC_RULE"><g:message code="app.assignmentRule.BASIC_RULE" /></label>
        </div>
        <div class="basicRuleOption" style="padding-left:25px; ${workflowRuleInstance?.assignmentRule == 'ADVANCED_RULE' ? 'display:none;' : ''}">
            <div class="checkbox checkbox-primary">
                <g:checkBox id="assignToUserGroup"
                            name="assignToUserGroup"
                            class="assignToUserGroupField workflowRuleField basicRuleOptionCheckbox"
                            value="${workflowRuleInstance?.assignToUserGroup}"
                            checked="${workflowRuleInstance?.assignToUserGroup}"/>
                <label for="assignToUserGroup"><g:message code="app.label.workflow.rule.assignToUserGroup" />
                </label>
            </div>

            <div class="checkbox checkbox-primary">
                <g:checkBox id="autoAssignToUsers"
                            name="autoAssignToUsers"
                            class="autoAssignToUsersField workflowRuleField basicRuleOptionCheckbox"
                            value="${workflowRuleInstance?.autoAssignToUsers}"
                            checked="${workflowRuleInstance?.autoAssignToUsers}"/>
                <label for="autoAssignToUsers"><g:message code="app.label.workflow.rule.autoAssignToUsers" /></label>
            </div>
        </div>
        <div class="radio radio-primary">
            <input class="autoAssignmentRuleRadio workflowRuleField" id="ADVANCED_RULE" type="radio" name="assignmentRule_val">
            <label for="ADVANCED_RULE"><g:message code="app.assignmentRule.ADVANCED_RULE" /></label>
        </div>
        <g:select id="advancedAssignment" name="advancedAssignment" value="${workflowRuleInstance?.advancedAssignment?.id}"
                  from="${advancedAssignmentList}" optionKey="id" optionValue="name" style="${workflowRuleInstance?.assignmentRule == 'ADVANCED_RULE' ?: 'display:none;'}"
                  noSelection="${['': message(code: 'select.one')]}" class="form-control advancedAssignmentField workflowRuleField"></g:select>
        <g:hiddenField name="assignmentRule" class="assignmentRule" value="${workflowRuleInstance?.assignmentRule}"/>
    </div>
    <div class="col-lg-2 pvcPvqRule">
        <div class="col-xs-12">
            <script>
                sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithWorkflowRuleList')}";
                sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
                $(function () {
                    bindShareWith($('.assignedToUsersControl'), sharedWithListUrl, sharedWithValuesUrl, "100%");
                    $('#s2id_assignedToUsersControl').find(".select2-input").val($.i18n._("any.user"));
                    $('.assignedToUsersControl').attr("data-placeholder", $.i18n._("any.user"));
                });
            </script>
            <label><g:message code="app.label.workflow.rule.assignedTo"/></label>
            <g:set var="userValue" value="${(workflowRuleInstance?.assignedToUserGroup?.collect { Constants.USER_GROUP_TOKEN + it.id })?.join(';')}"/>
            <select class="assignedToUsersControl form-control workflowRuleField" id="assignedToUsersControl" name="assignedTo" data-value="${userValue}"></select>
        </div>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-3 " >
        <div class="form-inline">
            <label for="autoExecuteInDays"><g:message code="app.label.workflow.rule.autoExecuteInDays_strt"/></label>

            <div class="form-group">
                <input class="autoExecuteInDays form-control workflowRuleField" type="number" min="1" max="${WorkflowRule.constrainedProperties.autoExecuteInDays.max}" id="autoExecuteInDays" name="autoExecuteInDays" value="${workflowRuleInstance?.autoExecuteInDays}" style="width: 60px; margin-right: 20px; margin-left: 20px;">
            </div>
            <label for="autoExecuteInDays"><g:message code="app.label.workflow.rule.autoExecuteInDays_end"/></label>
        </div>
        <div class="checkbox checkbox-primary">
            <g:checkBox name="autoExecuteExcludeWeekends" class="autoExecuteExcludeWeekends workflowRuleField" value="${workflowRuleInstance?.autoExecuteExcludeWeekends}"/>
            <label for="autoExecuteExcludeWeekends">
                <b><g:message code="app.label.workflow.rule.excludeWeekends"/></b>
            </label>
        </div>
    </div>

    <div class="col-lg-3">
    </div>
    <div class="col-lg-3 pvcPvqRule">
        <div class="form-inline mb-5">
            <label for="dueInDays"><g:message code="app.label.workflow.rule.due.in"/></label>

            <div class="form-group">
                <input class="dueInDays form-control workflowRuleField" type="number" min="1" max="${WorkflowRule.constrainedProperties.dueInDays.max}" id="dueInDays" name="dueInDays" value="${workflowRuleInstance?.dueInDays}" style="width: 60px; margin-right: 20px; margin-left: 20px;">
            </div>
            <label for="dueInDays"><g:message code="app.label.workflow.rule.days"/></label>
        </div>
        <div class="checkbox checkbox-primary">
            <g:checkBox name="excludeWeekends" class="excludeWeekends workflowRuleField" value="${workflowRuleInstance?.excludeWeekends}"/>
            <label for="excludeWeekends">
                <b><g:message code="app.label.workflow.rule.excludeWeekends"/></b>
            </label>
        </div>
    </div>
</div>