<%@ page import="com.rxlogix.config.CognosReport" %>
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<div class="row">

  <div class="col-md-6">

    <div class="form-group">
      <label class="col-md-${column1Width} control-label" for="name">
        <g:message code="cognosReport.name.label"/><span class="required-indicator">*</span>
      </label>

      <div class="col-md-${column2Width} ${hasErrors(bean: cognosReportInstance, field: 'name', 'has-error')}">
        <g:textField name="name"
                     value="${cognosReportInstance?.name}"
                     placeholder="${message(code: 'cognosReport.name.label')}"
                     class="form-control"/>
      </div>
    </div>

    <div class="form-group">
      <label class="col-md-${column1Width} control-label" for="url">
        <g:message code="cognosReport.url.label"/><span class="required-indicator">*</span>
      </label>

      <div class="col-md-${column2Width} ${hasErrors(bean: cognosReportInstance, field: 'url', 'has-error')}">
          <g:textArea name="url" value="${cognosReportInstance?.url}"
                      rows="5" cols="40" maxlength="${CognosReport.constrainedProperties.url.maxSize}"
                      placeholder="${message(code: 'cognosReport.url.label')}"
                      class="form-control"/>
          <small class="text-muted">Max: ${CognosReport.constrainedProperties.url.maxSize} characters</small>
      </div>
    </div>

    <div class="form-group">
      <label class="col-md-${column1Width} control-label" for="description">
        <g:message code="cognosReport.description.label"/>
      </label>

      <div class="col-md-${column2Width}">
        <g:textArea name="description" value="${cognosReportInstance?.description}"
                    rows="5" cols="40" maxlength="${CognosReport.constrainedProperties.description.maxSize}"
                    placeholder="${message(code: 'cognosReport.description.label')}"
                    class="form-control"/>
        <small class="text-muted">Max: ${CognosReport.constrainedProperties.description.maxSize} characters</small>
      </div>
    </div>

  </div>
</div>