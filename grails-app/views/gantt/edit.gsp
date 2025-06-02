<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.gantt.appName")}"/>
    <title><g:message code="app.gantt.edit.title"/></title>
    <asset:javascript src="app/publisher/ganttTemplate.js"/>
</head>

<body>

<rx:container title="${message(code: "app.label.edit.gantt")}" bean="${gantt}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${gantt}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="gantt" action="update" method="post" name="form">
            <g:hiddenField name="id" value="${gantt.id}"/>

            <g:render template="includes/form" model="['gantt': gantt]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:hiddenField name="version" id="version" value="${gantt?.version}"/>
                        <g:actionSubmit class="btn btn-primary" action="update"
                                        value="${message(code: 'default.button.update.label')}"/>
                        <button type="button" class="btn btn-default" data-evt-clk='{"method": "goToUrl", "params": ["gantt", "index"]}'
                                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>

</body>
</html>