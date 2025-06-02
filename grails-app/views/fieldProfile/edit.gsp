<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'app.field.profile.label')}"/>
    <title><g:message code="app.fieldProfile.edit.title" args="[entityName]"/></title>
    <g:javascript>
        var FIELDPROFILE = {
            ajaxReportFieldByGroupUrl: "${createLink(action: 'ajaxReportFieldByGroup')}"
        }
        var blindedLabel = "${g.message(code: 'app.template.blinded')}";
        var protectedLabel = "${g.message(code: 'app.template.protected')}";
        var hiddenLabel = "${g.message(code: 'app.template.hidden')}";
        var disabled = false;
        var loadGroupDataUrl = '${createLink(controller: 'fieldProfile', action: 'loadFieldProfileData')}';
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
        <div>
            <rx:container title="${message(code: "app.edit.field.profile.label")}">
                <g:render template="/includes/layout/flashErrorsDivs" bean="${fieldProfileInstance}" var="theInstance"/>
                <g:form method="put" action="update" class="form-horizontal" name="fieldProfileForm">
                    <g:hiddenField name="id" class="fieldProfileId" value="${fieldProfileInstance?.id}"/>
                    <g:hiddenField name="version" value="${fieldProfileInstance?.version}"/>
                    <g:hiddenField name="checkedParams" value="${checkBoxParameter?.join(',') ?: ''}"/>
                    <g:render template="form" model="[checkBoxParameter: checkBoxParameter, fieldProfileInstance: fieldProfileInstance, reportFieldGroupList: reportFieldGroupList]"/>
                    <div class="buttonBar text-right">
                        <button name="edit" class="btn btn-primary" id="saveButton">
                            <span class="glyphicon glyphicon-ok icon-white"></span>
                            ${message(code: 'default.button.update.label')}
                        </button>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["fieldProfile", "index"]}'
                                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </g:form>
            </rx:container>
        </div>
    </div>
</div>
</body>
</html>
