<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'accessControlGroup.label')}"/>
    <title><g:message code="default.create.title" args="[entityName]"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.accessControlGroup")}">

    <h1 class="page-header"><g:message code="default.create.label" args="[entityName]"/></h1>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${acgInstance}" var="theInstance"/>

    <g:form method="post" action="save" class="form-horizontal">
        <g:render template="form" model="[acgInstance: acgInstance]"/>

        <div class="buttonBar">
            <button name="edit" class="btn btn-primary">
                <span class="glyphicon glyphicon-ok icon-white"></span>
                ${message(code: 'default.button.save.label')}
            </button>
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["accessControlGroup", "index"]}'
                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
    </g:form>

</rx:container>

</body>
</html>
