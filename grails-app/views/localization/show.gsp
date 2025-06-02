<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.localization")}"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
    <asset:javascript src="app/localization.js"/>
</head>
<body>
<div class="content ">
    <div class="container ">
        <rx:container title="${message(code: "default.show.label", args:[entityName])}">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${locInstance}" var="theInstance"/>
            <div class="container-fluid">
                <%@ page import="com.rxlogix.localization.Localization" %>
                <g:hiddenField name="id" value="${locInstance.id}"/>
                <div class="row form-group">
                <div class="col-lg-6">
                    <label for="code"><g:message code="app.label.localization.id"/></label>
                    <div id="locId">${locInstance?.id}</div>
                </div>
            </div>

                <div class="row form-group">
                    <div class="col-lg-6">
                        <label for="code"><g:message code="app.label.localization.code"/></label>
                        <div id="code">${locInstance?.code}</div>
                    </div>
                </div>

                <div class="row form-group">
                    <div class="col-lg-6">
                        <label for="locale"><g:message code="app.label.localization.locale"/></label>
                        <div id="locale">${locInstance?.locale}</div>
                    </div>
                </div>

                <div class="row form-group">
                    <div class="col-lg-6">
                        <label for="text"><g:message code="app.label.localization.text"/></label>
                        <div id="text">${locInstance?.text}</div>
                    </div>
                </div>

                <div class="row form-group">
                    <div class="col-lg-6">
                        <label for="relevance"><g:message code="app.label.localization.relevance"/></label>
                        <div id="relevance">${locInstance?.relevance}</div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-xs-12">
                        <div class="pull-right">
                            <g:link controller="localization" action="edit" id="${locInstance.id}"
                                    class="btn btn-primary"><g:message
                                    code="default.button.edit.label"/></g:link>
                            <button type="button" class="btn btn-default" data-evt-clk='{"method": "goToUrl", "params": ["localization", "index"]}'
                                    id="listButton">${message(code: "app.label.localization.list")}</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="horizontalRuleFull"></div>
            <div class="greyText">
                <div>
                    <small class="text-muted">
                        <span id="dateCreated-label" class="property-label">
                            <g:message code="app.label.dateCreated"/>:
                        </span>
                        <span class="property-value" aria-labelledby="dateCreated-label">
                            <g:render template="/includes/widgets/dateDisplayWithTimezone" model="[date:locInstance?.dateCreated]"/>
                        </span>
                        <span id="lastUpdated-label" class="property-label" style="margin-left: 50px;">
                            <g:message code="app.label.modifiedDate"/>:
                        </span>
                        <span class="property-value" aria-labelledby="lastUpdated-label">
                            <g:render template="/includes/widgets/dateDisplayWithTimezone" model="[date:locInstance?.lastUpdated]"/>
                        </span>
                    </small>
                </div>
            </div>
        </rx:container>
    </div>
</div>
</body>
</html>