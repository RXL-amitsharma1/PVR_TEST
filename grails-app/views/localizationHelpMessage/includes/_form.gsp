<%@ page import="com.rxlogix.enums.EmailTemplateTypeEnum" %>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="localizationId"><g:message code="app.label.localizationHelp.label"/></label>
        <select type="hidden" name="localizationId" id="localizationId" data-value="${localization?.id}" class="form-control "></select>
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="localizationId"><g:message code="app.label.localizationHelp.helpContent"/></label>
        <textarea id="message" name="message" class="form-control richEditor">${raw(helpMessage?.message)}</textarea>
    </div>
</div>
<script>
    var localizationDataUrl = "${createLink(controller: 'localizationHelpMessage', action: 'localizationList')}";
    var localizationTextUrl = "${createLink(controller: 'localizationHelpMessage', action: 'localizationValue')}";

</script>
<asset:javascript src="/vendorUi/tinymce771/tinymce.min.js"/>
<asset:javascript src="/app/helpMessageEditor.js"/>

