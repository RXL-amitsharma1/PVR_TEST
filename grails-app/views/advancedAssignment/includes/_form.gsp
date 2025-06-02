<%@ page import="com.rxlogix.util.ViewHelper" %>
<g:if test="${mode != 'create'}">
    <g:hiddenField name="id" value="${advancedAssignmentInstance?.id}" />
</g:if>
<g:set var="assignedUserName" value="${ownerUsername}"/>
<g:set var="assignedUserId" value="${ownerUserId}"/>
<div class="row form-group">
    <div class="col-lg-3">
        <label for="name"><g:message code="app.label.workflow.name" /><span class="required-indicator">*</span></label>
        <g:textField name="name" class="form-control" maxlength="255" value="${advancedAssignmentInstance?.name}"/>
    </div>
    <div class="col-lg-2">
        <label for="category"><g:message code="app.label.category" /></label>
        <g:select id="category" name="category" optionKey="name" optionValue="display"
                  from="${ViewHelper.getAdvancedAssignmentEnumI18n()}" value="${advancedAssignmentInstance?.category?.name()}"
                  noSelection="['':'-Select Category-']" class="form-control workflowRuleField"/>
    </div>
    <div class="col-lg-2">
        <label for="assignedUsername"><g:message code="app.label.owner" /></label>
        <g:textField id="assignedUsername" name="assignedUsername" class="form-control"
                     value="${assignedUserName}" readonly="true"/>
        <input hidden name="assignedUser" id="assignedUser" value="${assignedUserId}"/>
    </div>

    <div class="col-lg-4">
        <label for="description"><g:message code="app.label.description" /></label>
        <g:textField id="description" name="description" class="form-control" maxlength="4000" value="${advancedAssignmentInstance?.description}"/>
    </div>
</div>

<sec:ifAnyGranted roles="ROLE_QUALITY_CHECK">
    <div class="row form-group">
        <div class="col-lg-2">
            <label></label>
            <div class="checkbox checkbox-primary checkbox-inline">
                <g:checkBox id="qualityCheck"
                        name="qualityChecked" value="${advancedAssignmentInstance?.qualityChecked}"/>
                <label for="qualityChecked">
                    <g:message code="app.label.qualityChecked"/>
                </label>
            </div>
        </div>
    </div>
</sec:ifAnyGranted>

<div class="row form-group">
    <div class="col-lg-11">
        <g:textArea rows="10" cols="5" placeholder="${message(code: 'app.advanced.assignment.query.placeholder')}"
                    id="assignmentQuery" name="assignmentQuery" class="form-control" maxlength="32000" value="${advancedAssignmentInstance?.assignmentQuery}"/>
    </div>
</div>

<div class="row form-group">
    <div class="bs-callout bs-callout-info">
        <h5><g:message code="app.label.notes" />:</h5>
        <h5><g:message code="app.advanced.assignment.sql.warning"/></h5>
        <h5><g:message code="app.advanced.assignment.example.label"/></h5>
        <div class="text-muted col-lg-5"><pre>UPDATE table_name SET column1 = value1 WHERE condition</pre></div>
    </div>
</div>