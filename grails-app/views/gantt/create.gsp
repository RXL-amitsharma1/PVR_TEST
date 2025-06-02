<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.gantt.appName")}"/>
    <title><g:message code="app.gantt.create.title"/></title>
    <asset:javascript src="app/publisher/ganttTemplate.js"/>
</head>

<body>

<rx:container title="${message(code: "app.label.edit.gantt")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${gantt}" var="theInstance"/>
    <div class="container-fluid">
        <g:form method="post" action="save" class="form-horizontal" name="form">

            <g:render template="includes/form" model="[gantt: gantt]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn btn-primary" action="save"
                                        value="${message(code: 'default.button.save.label')}"/>
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