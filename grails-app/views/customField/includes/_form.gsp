<%@ page import="com.rxlogix.config.CustomReportField; com.rxlogix.ReportFieldService" %>


<div class="row form-group">
    <div class="col-lg-6">
        <label for="reportField"><g:message code="app.label.customField.field" /> <span class="required-indicator">*</span></label>
        <select name="reportFieldId" id="reportField" class="form-control selectField">
            <option value="-1" default><g:message code="dataTabulation.select.field" /></option>
            <g:each in="${fields}" var="group">
                <optgroup label="${g.message(code: "app.reportFieldGroup.${group.text}")}">
                    <g:each in="${group.children}" var="field">
                        <g:set var="sourceColumn" value="${field.getSourceColumn(selectedLocale)}"/>
                        <option argusName="${sourceColumn?.tableName?.tableAlias}.${sourceColumn?.columnName}"
                                reportFieldName="${field.name}" templateCLLSelectable="${field.templateCLLSelectable}"
                                templateDTRowSelectable="${field.templateDTRowSelectable}" templateDTColumnSelectable="${field.templateDTColumnSelectable}" fieldGroup="${field.fieldGroup?.name}"
                                description='${message(code: ("app.reportField."+field.name+".label.description"), default: "")}'
                                value="${field.id}" ${customFieldInstance?.reportField?.name==field.name?"selected":""}><g:message code="app.reportField.${field.name}"/></option>
                    </g:each>
                </optgroup>
            </g:each>
        </select>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="fieldGroupId"><g:message code="app.label.customField.fieldGroup" /> <span class="required-indicator">*</span></label>
        <select  name="fieldGroupId"  id="fieldGroupId" class="form-control">
        <g:each in="${fieldGroup}" var="group">
            <option value="${group.name}" ${group.name==customFieldInstance?.fieldGroup?.name?"selected":""}>${g.message(code: "app.reportFieldGroup.${group.name}")}</option>
        </g:each>
        </select>
    </div>
</div>


<div class="row form-group">

    <div class="col-lg-2">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="templateCLLSelectable" value="${customFieldInstance?.templateCLLSelectable}"/>
            <label for="templateCLLSelectable">
                <g:message code="app.label.customField.templateCLLSelectable"/>
            </label>
        </div>
    </div>

    <div class="col-lg-2">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="templateDTRowSelectable" value="${customFieldInstance?.templateDTRowSelectable}"/>
            <label for="templateDTRowSelectable">
                <g:message code="app.label.customField.templateDTRowSelectable"/>
            </label>
        </div>
    </div>

    <div class="col-lg-2">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="templateDTColumnSelectable" value="${customFieldInstance?.templateDTColumnSelectable}"/>
            <label for="templateDTColumnSelectable">
                <g:message code="app.label.customField.templateDTColumnSelectable"/>
            </label>
        </div>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="customName"><g:message code="app.label.name" /> <span class="required-indicator">*</span></label>
        <g:textField name="customName" value="${customFieldInstance?.customName}"  class="form-control" maxlength="255" />
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="customDescription"><g:message code="app.label.description" /></label>
        <g:textField name="customDescription" value="${customFieldInstance?.customDescription}" class="form-control" maxlength="${CustomReportField.constrainedProperties.customDescription.maxSize}"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="defaultExpression"><g:message code="app.template.customExpression" /> <span class="required-indicator">*</span></label>
        <g:textArea name="defaultExpression" value="${customFieldInstance?.defaultExpression}" class="form-control"  maxlength="${CustomReportField.constrainedProperties.defaultExpression.maxSize}"/>
    </div>
</div>
