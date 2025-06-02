<%@ page import="com.rxlogix.config.EmailTemplate; com.rxlogix.enums.EmailTemplateTypeEnum" %>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="name"><g:message code="app.label.name" /><span class="required-indicator">*</span></label>
        <input id="name" name="name" value="${emailTemplate?.name}"  class="form-control" maxlength="${EmailTemplate.constrainedProperties.name.maxSize}"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.emailConfiguration.subject" /></label>
        <input id="description" name="description" value="${emailTemplate?.description}"  class="form-control" maxlength="${EmailTemplate.constrainedProperties.description.maxSize}"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="type"><g:message code="app.label.type" /></label>
        <select name="type" id="type" class="form-control">
            <option ${EmailTemplateTypeEnum.PUBLIC==emailTemplate?.type?"selected":""} value="${EmailTemplateTypeEnum.PUBLIC}"><g:message code="${EmailTemplateTypeEnum.PUBLIC.getI18nKey()}"/></option>
            <option ${EmailTemplateTypeEnum.USER==emailTemplate?.type?"selected":""} value="${EmailTemplateTypeEnum.USER}"><g:message code="${EmailTemplateTypeEnum.USER.getI18nKey()}"/></option>
        </select>
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="to"><g:message code="app.label.emailConfiguration.to"  default="To"/></label>
        <g:select  placeholder=" "
          name="to" id="to"
          from="${[]}"
          data-value="${emailTemplate?.to}"
          class="form-control emailUsers" multiple="true" maxlength="${EmailTemplate.constrainedProperties.to.maxSize}"
          data-options-url="${createLink(controller: 'email', action: 'allEmailsForCC', params: [emails: emailTemplate?.to])}"/>
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.emailConfiguration.cc" /></label>
        <g:select placeholder=" "
          name="cc" id="cc"
          from="${[]}"
          data-value="${emailTemplate?.cc}"
          class="form-control emailUsers" multiple="true" maxlength="${EmailTemplate.constrainedProperties.cc.maxSize}"
          data-options-url="${createLink(controller: 'email', action: 'allEmailsForCC', params: [emails: emailTemplate?.cc])}"/>
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="name"><g:message code="com.rxlogix.config.EmailTemplate.body.label" default="Email Body" /><span class="required-indicator">*</span></label>
        <textarea id="body" name="body"  class="form-control richEditor">${emailTemplate?.body?.encodeAsHTML()}</textarea>
    </div>
</div>
<script>
    var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
</script>
<asset:javascript src="/vendorUi/tinymce771/tinymce.min.js"/>
<asset:javascript src="/app/emailTemplateEditor.js"/>
<asset:javascript src="/app/configuration/deliveryOption.js"/>
