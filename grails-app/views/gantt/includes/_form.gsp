<%@ page import="com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.config.publisher.GanttItem" %>
<div class="alert alert-danger " id="errorMessage" style="display: none">
    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
    <span><g:message code="app.label.gantt.fillCondition.error"/></span>
</div>
<input type="hidden" name="isTemplate" value="true">

<div class="row form-group">
    <div class="col-lg-6">
        <label for="name"><g:message code="app.label.gantt.name"/><span class="required-indicator">*</span></label>
        <input required id="name" name="name" value="${gantt?.name}" class="form-control"/>
    </div>
</div>
<div class="row ">
    <div class="col-lg-2">
        <label for="defaultReportDuration"><g:message code="app.label.gantt.defaultReportDuration"/><span class="required-indicator">*</span>
        </label>
        <input style="width: 100px" type="number" id="defaultReportDuration" name="defaultReportDuration" value="${gantt?.defaultReportDuration ?: "4"}" class="form-control"/>
    </div>
    <div class="col-lg-2">
        <label for="defaultSectionDuration"><g:message code="app.label.gantt.defaultSectionDuration"/><span class="required-indicator">*</span>
        </label>
        <input style="width: 100px" type="number" id="defaultSectionDuration" name="defaultSectionDuration" value="${gantt?.defaultSectionDuration ?: "4"}" class="form-control"/>
    </div>

    <div class="col-lg-2">
        <label for="defaultFullDuration"><g:message code="app.label.gantt.defaultFullDuration"/><span class="required-indicator">*</span>
        </label>
        <input style="width: 100px" type="number" id="defaultFullDuration" name="defaultFullDuration" value="${gantt?.defaultFullDuration ?: "4"}" class="form-control"/>
    </div>

    <div class="col-lg-2">
        <label for="defaultSubmissionDuration"><g:message code="app.label.gantt.defaultSubmissionDuration"/><span class="required-indicator">*</span>
        </label>
        <input style="width: 100px" type="number" id="defaultSubmissionDuration" name="defaultSubmissionDuration" value="${gantt?.defaultSubmissionDuration ?: "2"}" class="form-control"/>
    </div>
    <div class="col-lg-2">
        <label for="defaultAiDuration"><g:message code="app.label.gantt.defaultAiDuration"/><span class="required-indicator">*</span>
        </label>

        <input style="width: 100px" type="number" required id="defaultAiDuration" name="defaultAiDuration" value="${gantt?.defaultAiDuration ?: "2"}" class="form-control"/>
    </div>

</div>
<table width="100%" class="table ganttTable" style="margin-top: 20px">
    <thead>
    <tr>
        <th width="80px"></th>
        <th width="350px"><label><g:message code="app.label.gantt.item.name"/></label></th>
        <th width="350px"><label><g:message code="actionItem.assigned.to.label"/></label></th>
        <th width="100px"><label><g:message code="app.label.gantt.item.duration"/></label></th>
        <th width="350px"><label><g:message code="app.label.gantt.item.conditionType"/></label></th>
        <th width="*"><label><g:message code="app.label.gantt.item.condition"/></label></th>

    </tr>
    </thead>
    <tbody class="attachmentSectionsTable">
    <tr class="ganttRowTemplate ganttItemRow" style="display: none">
        <td>
            <span class='table-remove md md-close pv-cross ganttSectionRemove'></span>
            <span class='table-add md md-arrow-up pv-cross ganttSectionUp'></span>
            <span class='table-add md md-arrow-down pv-cross ganttSectionDown'></span>
            <input class="form-control" name="ganttSectionId" type="hidden" value="0">
        </td>
        <td><input name="itemName" required class="form-control" value="-"></td>
        <td>
            <g:set var="users" value="${User.findAllByEnabled(true).sort { it.username }}"/>
            <g:set var="userGroups" value="${UserGroup.findAllByIsDeleted(false).sort { it.name }}"/>
            <select name="assignedTo" class="form-control select2-box taskTemplateField">
                <option value="" data-blinded="false">${message(code: 'com.rxlogix.config.TaskTemplate.assignToOwner')}</option>
                <g:if test="${userGroups}">
                    <optgroup label="${g.message(code: 'user.group.label')}" data-blinded="false">
                        <g:each in="${userGroups}" var="userGroup">
                            <option value="${userGroup.getReportRequestorKey()}" data-blinded="${userGroup.isBlinded}">${userGroup.getReportRequestorValue()}</option>
                        </g:each>
                    </optgroup>
                </g:if>
                <optgroup label="${g.message(code: 'user.label')}" data-blinded="false">
                    <g:each in="${users}" var="user">
                        <option value="${user.getReportRequestorKey()}" data-blinded="${user.isBlinded}">${user.getReportRequestorValue()}</option>
                    </g:each>
                </optgroup>
            </select>
        </td>
        <td><input type="number" required value="2" min="1" name="itemDuration" class="form-control"></td>
        <td>
            <g:select name="itemConditionType" from="${GanttItem.ConditionType.i18List}"
                      class="form-control itemConditionType" optionKey="name" optionValue="display"/>
        </td>
        <td>
            <g:select name="reportWorkflowList" from="${reportWorkflowList}"
                      multiple="multiple" class="form-control reportWorkflowList conditionField" optionKey="name" optionValue="display"/>
            <g:select name="publisherSectionWorkflowList" from="${publisherSectionWorkflowList}"
                      multiple="multiple" class="form-control publisherSectionWorkflowList conditionField" optionKey="name" optionValue="display"/>
            <g:select name="publisherWorkflowList" from="${publisherWorkflowList}"
                      multiple="multiple" class="form-control publisherWorkflowList conditionField" optionKey="name" optionValue="display"/>
            <g:select name="reportStateList" from="${reportStateList}"
                      multiple="multiple" class="form-control reportStateList conditionField" optionKey="name" optionValue="display"/>
            <g:select name="publisherStateList" from="${publisherStateList}"
                      multiple="multiple" class="form-control publisherStateList conditionField" optionKey="name" optionValue="display"/>
            <g:select name="publisherSectionStateList" from="${publisherSectionStateList}"
                      multiple="multiple" class="form-control publisherSectionStateList conditionField" optionKey="name" optionValue="display"/>
            <input name="advancedCondition" class="form-control advancedCondition conditionField"/>
            <input type="hidden" name="ganttCondition">
            <input type="hidden" class=ganttTaskType" name="ganttTaskType">
        </td>
    </tr>
    </tbody>
</table>

<table width="100%" class="table reportStageTable">
    <thead>
    <tr>
        <th width="80px"><span class="table-add glyphicon glyphicon-plus reportStageAdd"></span></th>
        <th width="350px"><label><g:message code="app.label.gantt.stage.reportStage"/></label></th>
        <th width="350px"></th>
        <th width="100px"></th>
        <th width="300px"></th>
        <th width="*"></th>
    </tr>
    </thead>
    <tbody class="attachmentSectionsTable">
    <g:each var="item" in="${gantt?.getReportStage()}" status="i">
        <g:render template="includes/rowTemplate" model="${pageScope.variables + [item: item]}"/>
    </g:each>
    </tbody>
</table>



<table width="100%" class="table pubSectStageTable">
    <thead>
    <tr>
        <th width="80px"><span class="table-add glyphicon glyphicon-plus pubSectStageAdd"></span></th>
        <th width="350px"><label><g:message code="app.label.gantt.stage.pubSectStage"/></label></th>
        <th width="350px"></th>
        <th width="100px"></th>
        <th width="300px"></th>
        <th width="*"></th>
    </tr>
    </thead>
    <tbody class="attachmentSectionsTable">
    <g:each var="item" in="${gantt?.getPubSectionStage()}" status="i">
        <g:render template="includes/rowTemplate" model="${pageScope.variables + [item: item]}"/>
    </g:each>
    </tbody>
</table>

<table width="100%" class="table publishingStageTable">
    <thead>
    <tr>
        <th width="80px"><span class="table-add glyphicon glyphicon-plus publishingStageAdd"></span></th>
        <th width="350px"><label><g:message code="app.label.gantt.stage.publishingStage"/></label></th>
        <th width="350px"></th>
        <th width="100px"></th>
        <th width="300px"></th>
        <th width="*"></th>
    </tr>
    </thead>
    <tbody class="publishingStageTable">
    <g:each var="item" in="${gantt?.getFullPublisherStage()}" status="i">
        <g:render template="includes/rowTemplate" model="${pageScope.variables + [item: item]}"/>
    </g:each>
    </tbody>
</table>
