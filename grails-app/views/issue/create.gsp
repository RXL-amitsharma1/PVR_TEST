<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.ReasonOfDelayAppEnum;" %>
<html>
<head>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <g:if test="${type == ReasonOfDelayAppEnum.PVC.name()}">
        <title><g:message code="app.central.title.central.issue.create" /></title>
    </g:if>
    <g:else>
        <title><g:message code="app.quality.title.quality.issue.create"/></title>
    </g:else>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/quality/capa_edit.js"/>
    <g:javascript>
        var AttachmentSizeLimit = ${grailsApplication.config.grails.controllers.attachment.maxFilSize};
        var attachmentParametersUrl = "${createLink(controller: 'issueRest', action: 'attachmentParameters')}";
        var deleteTempFiles = "${createLink(controller:'issueRest' , action:'deleteTempFiles')}";
        var removeIssueAttachmentsUrl = "${createLink(controller: 'issueRest', action: 'removeAttachments')}";
        var fetchDataIssueUrl = "${createLink(controller: 'issue', action: 'fetchDataIssue' )}";
        var downloadAttachmentUrl = "${createLink(controller: 'issue', action: 'downloadAttachment')}";
    </g:javascript>
    <style>
    #corrective-actions_wrapper > .dt-layout-row:first-child {
        margin-top : 0px;
        padding-right: 0px;
        .dt-layout-cell {
            float: none;
        }
    }
    #preventive-actions_wrapper > .dt-layout-row:first-child {
        margin-top : 0px;
        padding-right: 0px;
        .dt-layout-cell {
            float: none;
        }
    }
</style>
</head>

<body>
<g:if test="${type == ReasonOfDelayAppEnum.PVC.name()}">
    <g:set var="issueControlller" value="pvcIssue" />
</g:if>
<g:else>
    <g:set var="issueControlller" value="issue" />
</g:else>
<div class="col-md-12">
<g:render template="/includes/layout/flashErrorsDivs" bean="${capaInstance}" var="theInstance"/>
    <div class="alert alert-danger alert-dismissible attachSizeExceed m-t-4" role="alert" hidden="hidden">
        <button type="button" class="close" id="attachSizeExceed">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <div id="message"></div>
    </div>
<g:form name="capa-8d-form" method="post" controller="${issueControlller}" action="save" enctype="multipart/form-data">
    <g:hiddenField name="ownerType" value="${type}"/>
    <g:hiddenField name="AttachJson" id="AttachJson"/>
    <g:render template="/issue/includes/capa_8d_form" model="[capaInstance: capaInstance, type: type]"/>
    <div class="button" style="text-align: right">
        <button type="submit" class="btn btn-primary report-create-button" id="saveButton">${message(code: 'default.button.save.label')}</button>
        <button type="reset" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["${issueControlller}", "index"]}'
                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
    </div>
</g:form>
<g:render template="/actionItem/includes/actionItemModal"/>
</div>
</body>
</html>