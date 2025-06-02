<html>
<head>
    <asset:javascript src="app/workflowStateList.js"/>
    <asset:javascript src="app/workflowState.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.workflowState.title"/></title>
    <style>
    </style>
</head>
<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${workflowStateInstance}" var="theInstance"/>
            <rx:container title="${message(code: 'app.label.workflow.State.index')}">
                <div class="pv-caselist">
                <div class="pull-right" style="cursor: pointer; text-align: right; position: relative;">
                    <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["workflowState", "create"]}'>
                        <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.workflow.state.create')}" style="color: #353d43;"></i>
                    </a>
                </div>

                <table id="workflowStateList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>
                            <th></th>
                            <th><g:message default="Name" code="app.label.workflow.name"/></th>
                            <th><g:message default="Description" code="app.label.workflow.description"/></th>
                            <th><g:message default="Display" code="app.label.workflow.display"/></th>
                            <th><g:message default="Final State" code="app.label.workflow.finalState"/></th>
                            <th><g:message default="Action" code="app.label.action"/></th>
                        </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
        </div>
    </div>
</div>
</body>
</html>