<%@ page import="com.rxlogix.config.ReportFooter" %>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="footer"><g:message code="app.label.reportFooter.footer" /><span class="required-indicator">*</span></label>
        <g:textArea name="footer" value="${reportFooterInstance?.footer}"  class="form-control reportFooterField" maxlength="${ReportFooter.constrainedProperties.footer.maxSize}"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.reportFooter.description" /></label>
        <g:textArea name="description" value="${reportFooterInstance?.description}" class="form-control reportFooterField" maxlength="${ReportFooter.constrainedProperties.description.maxSize}"/>
    </div>
</div>
