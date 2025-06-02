<%@ page import="com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.config.publisher.GanttItem" %>
<tr class="ganttItemRow">
    <td>
        <span class='table-remove md md-close pv-cross ganttSectionRemove'></span>
        <span class='table-add md md-arrow-up pv-cross ganttSectionUp'></span>
        <span class='table-add md md-arrow-down pv-cross ganttSectionDown'></span>
    </td>
    <td><input name="itemName" required class="form-control" value="${item.name}"></td>
    <td>
        <g:set var="users" value="${User.findAllByEnabled(true).sort { it.username }}"/>
        <g:set var="userGroups" value="${UserGroup.findAllByIsDeleted(false).sort { it.name }}"/>
        <select name="assignedTo" class="form-control select2-box taskTemplateField">
            <option value="" data-blinded="false">${message(code: 'com.rxlogix.config.TaskTemplate.assignToOwner')}</option>
            <g:if test="${userGroups}">
                <optgroup label="${g.message(code: 'user.group.label')}" data-blinded="false">
                    <g:each in="${userGroups}" var="userGroup">
                        <option value="${userGroup.getReportRequestorKey()}" ${item.assignedGroupToId == userGroup.id ? "selected" : ""} data-blinded="${userGroup.isBlinded}">${userGroup.getReportRequestorValue()}</option>
                    </g:each>
                </optgroup>
            </g:if>
            <optgroup label="${g.message(code: 'user.label')}" data-blinded="false">
                <g:each in="${users}" var="user">
                    <option value="${user.getReportRequestorKey()}" ${item.assignedToId == user.id ? "selected" : ""} data-blinded="${user.isBlinded}">${user.getReportRequestorValue()}</option>
                </g:each>
            </optgroup>
        </select>
    </td>
    <td><input type="number" required min="1" name="itemDuration" class="form-control" value="${item.duration}"></td>
    <td class="itemConditionTypeCell">
        <g:select name="itemConditionType" from="${com.rxlogix.config.publisher.GanttItem.ConditionType.i18List}" value="${item.completeConditionType}"
                  class="form-control multipleSelect2 itemConditionType" optionKey="name" optionValue="display"/>
    </td>
    <td>
        <g:render template="includes/select" model="[itemState: item.reportWorkflowState, list: reportWorkflowList, name: 'reportWorkflowList']"/>
        <g:render template="includes/select" model="[itemState: item.reportState, list: reportStateList, name: 'reportStateList']"/>
        <g:render template="includes/select" model="[itemState: item.publisherSectionWorkflowState, list: publisherSectionWorkflowList, name: 'publisherSectionWorkflowList']"/>
        <g:render template="includes/select" model="[itemState: item.publisherSectionState, list: publisherSectionStateList, name: 'publisherSectionStateList']"/>
        <g:render template="includes/select" model="[itemState: item.publisherState, list: publisherStateList, name: 'publisherStateList']"/>
        <g:render template="includes/select" model="[itemState: item.publisherWorkflowState, list: publisherWorkflowList, name: 'publisherWorkflowList']"/>

        <input name="advancedCondition" class="form-control advancedCondition conditionField" value="${item.advanced}" style="${item.advanced ? "" : "display:none"}"/>
        <input type="hidden" class="ganttTaskType" name="ganttTaskType" value="${item.taskType?.name()}">
        <input type="hidden" name="ganttCondition" value="${item.completeCondition}">
    </td>
</tr>