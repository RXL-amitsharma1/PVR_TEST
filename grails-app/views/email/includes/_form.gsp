<%@ page import="com.rxlogix.config.Email" %>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="email"><g:message code="app.label.email.email" /><span class="required-indicator">*</span></label>
        <input type="email" id="email" name="email" value="${email?.email}"  class="form-control emailField" maxlength="${Email.constrainedProperties.email.maxSize}"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.email.description" /><span class="required-indicator">*</span></label>
        <input id="description" name="description" value="${email?.description}"  class="form-control emailField" maxlength="${Email.constrainedProperties.description.maxSize}"/>
    </div>
</div>
