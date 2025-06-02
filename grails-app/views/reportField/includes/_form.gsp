<%@ page import="com.rxlogix.config.ReportField" %>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="name"><g:message code="report.field.name.label"/> <span class="required-indicator">*</span></label>
        <g:textField name="name" value="${reportFieldInstance?.name}" class="form-control" maxlength="${ReportField.constrainedProperties.name.maxSize}"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.description"/> <span class="required-indicator">*</span></label>
        <g:textField name="description" value="${reportFieldInstance?.description}" class="form-control" maxlength="${ReportField.constrainedProperties.description.maxSize}"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="fieldGroupId"><g:message code="report.fieldGroup.label"/> <span class="required-indicator">*</span>
        </label>
        <select name="fieldGroup" id="fieldGroupId" class="form-control">
            <g:each in="${fieldGroup}" var="group">
                <option value="${group.name}" ${group.name == reportFieldInstance?.fieldGroup?.name ? "selected" : ""}>${g.message(code: "app.reportFieldGroup.${group?.name}")}</option>
            </g:each>
        </select>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-1">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="querySelectable" value="${reportFieldInstance?.querySelectable}"/>
            <label for="querySelectable">
                <g:message code="app.label.reportField.querySelectable"/>
            </label>
        </div>
    </div>

    <div class="col-lg-2">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="templateCLLSelectable" value="${reportFieldInstance?.templateCLLSelectable}"/>
            <label for="templateCLLSelectable">
                <g:message code="app.label.reportField.templateCLLSelectable"/>
            </label>
        </div>
    </div>

    <div class="col-lg-2">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="templateDTRowSelectable" value="${reportFieldInstance?.templateDTRowSelectable}"/>
            <label for="templateDTRowSelectable">
                <g:message code="app.label.reportField.templateDTRowSelectable"/>
            </label>
        </div>
    </div>

    <div class="col-lg-2">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="templateDTColumnSelectable" value="${reportFieldInstance?.templateDTColumnSelectable}"/>
            <label for="templateDTColumnSelectable">
                <g:message code="app.label.reportField.templateDTColumnSelectable"/>
            </label>
        </div>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-2">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="override" value="${reportFieldInstance?.override}" checked="true"/>
            <label for="override">
                <g:message code="app.label.reportField.override"/>
            </label>
        </div>
    </div>
</div>

