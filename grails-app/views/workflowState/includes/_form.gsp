<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.config.WorkflowState; com.rxlogix.enums.ReportActionEnum; com.rxlogix.Constants" %>
<script>

    sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
    sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";

    $(function () {
        $("#reportActionTable").find('[id^=s2id_executorsControl]').find(".select2-input").val($.i18n._("any.user"));
        $(".executorsControl").each(function (index) {
            $(this).attr("data-placeholder", $.i18n._("any.user"));
        });
        bindShareWith($('.executorsControl'), sharedWithListUrl, sharedWithValuesUrl, "450px");
        $(document).on("click", ".reportAction_checkbox", function () {
            checkEnabledActions();
        });

        function checkEnabledActions() {
            $(".reportActionRow").each(function (index) {
                if ($(this).find(".reportAction_checkbox").is(':checked'))
                    $(this).find(".executorsControl").attr("readonly", false);
                else {
                    var $select = $(this).find(".executorsControl");
                    $select.val(null).trigger('change');
                    $select.attr("readonly", true);
                }
            });
        }

        checkEnabledActions();
    });

</script>

<g:if test="${'mode' != 'create'}">
    <g:hiddenField name="id" value="${workflowStateInstance?.id}" />
</g:if>

<div class="row form-group">
    <div class="col-lg-3">
        <label for="name"><g:message code="app.label.workflow.name" /><span class="required-indicator">*</span></label>
        <g:textField name="name" maxlength="${WorkflowState.NAME_MAX_LENGTH}" value="${workflowStateInstance?.name}"
        class="form-control workflowStateField "/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-2">
        <span class="checkbox checkbox-primary workflowStateField">
            <g:checkBox name="display" class="workflowStateField checkbox" checked="${workflowStateInstance?.display}"/>
            <label for="display"><g:message code="app.label.workflow.display"/></label>
        </span>
    </div>

    <div class="col-lg-2">
        <span class="checkbox checkbox-primary workflowStateField">
            <g:checkBox name="finalState" value="${workflowStateInstance?.finalState}" class="workflowStateField checkbox"
                        checked="${workflowStateInstance?.finalState}"/>
            <label for="finalState"><g:message code="app.label.workflow.finalState"/></label>
        </span>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.workflow.description" /></label>
        <g:textField name="description" maxlength="${WorkflowState.constrainedProperties.description.maxSize}" value="${workflowStateInstance?.description}" class="form-control workflowStateField"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <table class="table" id="reportActionTable">
            <tr>
                <th width="80px"><g:message code="default.button.enable.label"/></th>
                <th width="180px"><g:message code="app.label.workflow.actions"/></th>
                <th width="*"><g:message code="app.label.workflow.rule.can.execute"/></th>
            </tr>
            <g:each var="reportAction" in="${ViewHelper.getReportActionEnum()}">
                <g:set var="actionForType" value="${workflowStateInstance?.reportActions?.find { it.reportAction.name() == reportAction.name }}"/>
                <tr class="reportActionRow">
                    <td><span class="checkbox checkbox-primary workflowStateField">
                        <input type="checkbox" id="reportAction_enabled_${reportAction.name}" class="reportAction_checkbox workflowStateField" name="reportAction_enabled_${reportAction.name}" ${(actionForType ? "checked" : "")}/>
                        <label for="reportAction_enabled_${reportAction.name}"></label>
                    </span></td>
                    <td>${reportAction.display}</td>
                    <td>

                        <g:set var="executorGroupValue" value="${actionForType?.executorGroups?.collect { Constants.USER_GROUP_TOKEN + it.id }}"/>
                        <g:set var="executorUserValue" value="${actionForType?.executors?.collect { Constants.USER_TOKEN + it.id }}"/>
                        <g:set var="executorValue" value="${((executorGroupValue ?: []) + (executorUserValue ?: []))?.join(";")}"/>
                        <select class="executorsControl form-control workflowStateField" id="executorsControl_${reportAction.name}" name="canExecute_${reportAction.name}" data-value="${executorValue}"></select>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>
</div>