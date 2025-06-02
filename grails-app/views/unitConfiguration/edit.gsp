<%@ page import="com.rxlogix.config.UnitConfiguration" contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="${message(code: "app.unitConfiguration.title")}"/></title>
    <asset:javascript src="app/configuration/unitConfiguration.js"/>
    <g:javascript>
        var getAllowedAttachments = "${createLink(controller: 'unitConfigurationRest', action: 'getAllowedAttachments')}";
        var checkXsltIsHl7Url = "${createLink(controller: 'unitConfiguration', action: 'checkXsltIsHl7')}"
    </g:javascript>
    <style>
    .form-horizontal .form-group {
        margin-left: -5px;
        margin-right: -5px;
    }
    .form-group .col-md-2{
        width: 20%;
    }
    </style>
</head>

<body>
<g:set var="entityName" value="${message(code: "app.unitConfiguration.label.myInbox")}"/>
    <g:render template="/includes/layout/flashErrorsDivs" bean="${unitConfigurationInstance}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="unitConfiguration" action="update" method="post" autocomplete="off">
            <g:hiddenField name="id" value="${unitConfigurationInstance?.id}"/>

            <g:render template="includes/form" model="[unitConfigurationInstance: unitConfigurationInstance, icsrRecipientTypeList: icsrRecipientTypeList]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:hiddenField name="version" id="version" value="${unitConfigurationInstance?.version}"/>
                        <g:actionSubmit class="btn btn-primary" action="update"
                                        value="${message(code: 'default.button.update.label')}"/>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["unitConfiguration", "index"]}'
                                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</body>
</html>
