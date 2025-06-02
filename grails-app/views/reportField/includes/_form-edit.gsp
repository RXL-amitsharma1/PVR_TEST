<div class="row form-group">
    <div class="col-lg-6">
        <label><g:message code="report.field.name.label"/></label>
        <br/>
        <g:if test="${!isCentralField}">
        <a href="${createLink(controller: 'localization', action: 'search', params: [q: reportFieldInstance?.name])}">${reportFieldInstance?.name}</a>
        </g:if>
        <g:else>
            <span>${reportFieldInstance?.name}</span>
        </g:else>

        <g:hiddenField name="name" value="${reportFieldInstance?.name}" class="form-control"/>

    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.description"/> <span class="required-indicator">*</span></label>
        <g:textField name="description" value="${reportFieldInstance?.description}" class="form-control"/>
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
            <g:checkBox name="override" value="${reportFieldInstance?.override}"/>
            <label for="override">
                <g:message code="app.label.reportField.override"/>
            </label>
        </div>
    </div>
</div>

