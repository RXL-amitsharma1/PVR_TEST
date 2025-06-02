<%@ page import="com.rxlogix.enums.EmailTemplateTypeEnum" %>

<div class="row form-group">
    <div class="col-lg-2">
        <label for="releaseNumber"><g:message code="app.label.localizationHelp.releaseNotes.releaseNumber"/><span
                class="required-indicator">*</span></label>
        <input name="releaseNumber" id="releaseNumber" value="${instance?.releaseNumber}" required class="form-control " maxlength="255">
    </div>
    <div class="col-lg-4">
        <label for="title"><g:message code="app.label.localizationHelp.releaseNotes.title"/></label>
        <input name="title" id="title" value="${instance?.title}" class="form-control " maxlength="255">
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.localizationHelp.releaseNotes.description"/></label>
        <textarea id="description" name="description" class="form-control richEditor">${raw(instance?.description)}</textarea>
    </div>
</div>

<asset:javascript src="/vendorUi/tinymce771/tinymce.min.js"/>
<asset:javascript src="/app/helpMessageEditor.js"/>

