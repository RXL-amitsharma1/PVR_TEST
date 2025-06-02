<%@ page import="com.rxlogix.localization.Localization" %>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="code"><g:message code="app.label.localization.code"/> <span class="required-indicator">*</span></label>
        <g:textField name="code" value="${locInstance?.code}" class="form-control" maxlength="${Localization.constrainedProperties.code.maxSize}" readonly="${editMode}"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="locale"><g:message code="app.label.localization.locale"/> <span class="required-indicator">*</span></label>
        <g:textField name="locale" value="${locInstance?.locale}" class="form-control" maxlength="${Localization.constrainedProperties.locale.maxSize}" readonly="${editMode}"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="text"><g:message code="app.label.localization.text"/> <span class="required-indicator">*</span></label>
        <g:textArea name="text" value="${locInstance?.text}"
                    rows="5" cols="40" maxlength="${Localization.constrainedProperties.text.maxSize}"
                    class="form-control"/>
    </div>
</div>