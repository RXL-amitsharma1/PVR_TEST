<%@ page import="com.rxlogix.enums.EmailTemplateTypeEnum" %>

<div class="row form-group">
    <input type="hidden" id="releaseNotesId" name="releaseNotesId" value="${instance.releaseNotes.id}">
    <div class="col-lg-6">
        <label for="title"><g:message code="app.label.releaseNotesItem.title"/></label>
        <input name="title" id="title" value="${instance?.title}" class="form-control releaseNotesItemTitle" required maxlength="255">
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="summary"><g:message code="app.label.releaseNotesItem.summary"/></label>
        <textarea id="summary" name="summary" class="form-control richEditor">${raw(instance?.summary)}</textarea>
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="shortDescription"><g:message code="app.label.releaseNotesItem.shortDescription"/></label>
        <textarea id="shortDescription" name="shortDescription" class="form-control richEditor">${raw(instance?.shortDescription)}</textarea>
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.releaseNotesItem.learn"/></label>
        <textarea id="description" name="description" class="form-control richEditor">${raw(instance?.description)}</textarea>
    </div>
</div>

<asset:javascript src="/vendorUi/tinymce771/tinymce.min.js"/>
<asset:javascript src="/app/helpMessageEditor.js"/>

