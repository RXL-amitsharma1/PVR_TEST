<%@ page import="com.rxlogix.user.UserGroup; com.rxlogix.enums.EmailTemplateTypeEnum" %>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="title"><g:message code="app.label.localizationHelp.releaseNotes.title"/></label>
        <input name="title" id="title" value="${instance?.title}" class="form-control " maxlength="255">
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="userGroups"><g:message code="app.label.systemNotification.groupsOnly"/></label>

        <g:set var="userGroups" value="${com.rxlogix.user.UserGroup.findAllByIsDeleted(false).sort { it.name }}"/>
        <select name="userGroups" id="userGroups" class="form-control select2-box taskTemplateField" multiple placeholder="All">
            <g:each in="${userGroups}" var="userGroup">
                <option value="${userGroup.id}" ${instance?.userGroups?.find { it.id == userGroup.id } ? "selected" : ""}>${userGroup.name}</option>
            </g:each>
        </select>
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.localizationHelp.releaseNotes.description"/></label>
        <textarea id="description" name="description" class="form-control richEditor">${raw(instance?.description)}</textarea>
    </div>
</div><div class="row form-group">
    <div class="col-lg-6">
        <label for="details"><g:message code="app.label.releaseNotesItem.learn"/></label>
        <textarea id="details" name="details" class="form-control richEditor">${raw(instance?.details)}</textarea>
    </div>
</div>

<asset:javascript src="/vendorUi/tinymce771/tinymce.min.js"/>
<asset:javascript src="/app/helpMessageEditor.js"/>

