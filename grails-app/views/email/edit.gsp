<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.email.appName")}"/>
    <title><g:message code="app.emailConfiguration.edit.title"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.editemail")}" bean="${email}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${email}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="email" action="update" method="post">
            <g:hiddenField name="id" value="${email.id}"/>

            <g:render template="includes/form" model="['email': email]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:hiddenField name="version" id="version" value="${email?.version}"/>
                        <g:actionSubmit class="btn btn-primary" action="update"
                                        value="${message(code: 'default.button.update.label')}"/>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["email", "index"]}'
                                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>

</body>
</html>