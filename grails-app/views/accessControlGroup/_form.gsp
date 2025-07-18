<%@ page import="com.rxlogix.user.AccessControlGroup" %>
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<div class="row">

  <div class="col-md-6">

    <div class="form-group">
      <label class="col-md-${column1Width} control-label" for="name">
        <g:message code="accessControlGroup.name.label"/><span class="required-indicator">*</span>
      </label>

      <div class="col-md-${column2Width}">
        <g:textField name="name" maxlength="${AccessControlGroup.constrainedProperties.name.maxSize}"
                     value="${acgInstance?.name}"
                     placeholder="${message(code: 'accessControlGroup.name.label')}"
                     class="form-control"/>
      </div>
    </div>

    <div class="form-group">
      <label class="col-md-${column1Width} control-label" for="ldapGroupName">
        <g:message code="accessControlGroup.ldapGroupName.label"/><span class="required-indicator">*</span>
      </label>

      <div class="col-md-${column2Width}">
        <g:textField name="ldapGroupName" maxlength="${AccessControlGroup.constrainedProperties.ldapGroupName.maxSize}"
                     value="${acgInstance?.ldapGroupName}"
                     placeholder="${message(code: 'accessControlGroup.ldapGroupName.label')}"
                     class="form-control"/>
      </div>
    </div>

    <div class="form-group">
      <label class="col-md-${column1Width} control-label" for="description">
        <g:message code="accessControlGroup.description.label"/>
      </label>

      <div class="col-md-${column2Width}">
        <g:textArea name="description" value="${acgInstance?.description}"
                    rows="5" cols="40" maxlength="${AccessControlGroup.constrainedProperties.description.maxSize}"
                    placeholder="${message(code: 'accessControlGroup.description.label')}"
                    class="form-control"/>
        <small class="text-muted">Max: ${AccessControlGroup.constrainedProperties.description.maxSize} characters</small>
      </div>
    </div>

  </div>
</div>