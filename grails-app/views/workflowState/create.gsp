<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="Workflow State" />
    <title><g:message code="app.workflowState.create.title"/></title>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <rx:container title="${message(code: 'app.label.workflow.state.create')}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${workflowStateInstance}" var="theInstance"/>

    <g:form method="post" action="save" class="form-horizontal">

        <g:render template="includes/form" model="['mode':'create', workflowStateInstance:workflowStateInstance]" />

        <div class="buttonBar text-right">
            <button name="edit" class="btn btn-primary">
                ${message(code: 'default.button.save.label')}
            </button>
            <button type="button" class="btn btn-default" data-evt-clk='{"method": "goToUrl", "params": ["workflowState", "index"]}'
                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
    </g:form>

</rx:container>
        </div>
    </div>
</div>
</body>
</html>