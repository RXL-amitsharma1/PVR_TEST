<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${rx.renderRRSettingsEntityName(type:params.type)}"/>
    <title><g:message code="default.edit.title" args="[entityName]"/></title>
    <asset:javascript src="app/reportRequestTypeEdit.js"/>
</head>

<body>
<div class="content">
    <div class="container ">
<rx:container title="${message(code: "default.edit.label", args: [entityName])}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${reportRequestTypeInstance}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="reportRequestType" action="update" method="post">
            <g:hiddenField name="id" value="${reportRequestTypeInstance.id}"/>

            <g:render template="includes/form" model="['reportTemplateInstance': reportRequestTypeInstance]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:hiddenField name="version" id="version" value="${reportRequestTypeInstance?.version}"/>
                        <g:actionSubmit class="btn btn-primary" action="update"
                                        value="${message(code: 'default.button.update.label')}"/>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["reportRequestType", "index"]}'
                                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>
 </div>
</div>
</body>
</html>