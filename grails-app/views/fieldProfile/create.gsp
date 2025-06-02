<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'app.create.field.profile.label')}"/>
    <title><g:message code="app.fieldProfile.create.title"/></title>
    <g:javascript>
    var FIELDPROFILE = {
        ajaxReportFieldByGroupUrl: "${createLink(action: 'ajaxReportFieldByGroup')}"
    }
    var method = '${actionName}';
    </g:javascript>
    <asset:javascript src="vendorUi/jquery/jquery.ui.widget.js"/>
    <asset:javascript src="vendorUi/jquery/jquery-picklist.js"/>
    <asset:stylesheet href="vendorUi/jquery-picklist.css"/>
    <asset:javascript src="app/fieldProfile/field-profile.js"/>

    <style>
    body.modal-open {
        overflow: hidden;
        position: fixed;
        width: 100%;
    }
    </style>
</head>
<body>
<div class="content">
    <div class="container ">
        <rx:container title="${message(code: "app.create.field.profile.label")}">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${fieldProfileInstance}" var="theInstance"/>
            <g:form method="post" action="save" class="form-horizontal" name="fieldProfileForm">
                <g:hiddenField name="checkedParams" value="${checkBoxParameter?.join(',') ?: ''}"/>
                <g:render template="form" model="[fieldProfileInstance: fieldProfileInstance, reportFieldGroupList: reportFieldGroupList]"/>
                <div class="buttonBar text-right">
                    <button name="edit" class="btn btn-primary" id="saveButton">
                        <span class="glyphicon glyphicon-ok icon-white"></span>
                        ${message(code: 'default.button.save.label')}
                    </button>
                    <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["fieldProfile", "index"]}'
                            id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                </div>
            </g:form>
        </rx:container>
    </div>
</div>
</body>
</html>
