<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <asset:javascript src="app/WorkflowRuleList.js"/>
    <asset:javascript src="app/WorkflowRule.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.workflowRule.title"/></title>
    <script>
        var names =${raw(com.rxlogix.util.ViewHelper.getWorkflowConfigurationTypeI18nAsMap())};
    </script>
</head>
<body>
<div class="content">
    <div class="container">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${workflowRuleInstance}" var="theInstance"/>

            <rx:container title="${message(code: 'app.label.workflow.rule.appName')}">


                <div class="pv-caselist">
                    <div class="pull-right" style="cursor: pointer; text-align: right; position: relative;">
                        <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["workflowRule", "create"]}'>
                            <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.workflow.rule.create')}" style="color: #353d43;"></i>
                        </a>
                    </div>
                <table id="workflowRuleList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>
                            <th></th>
                            <th><g:message default="Name" code="app.label.name"/></th>
                            <th><g:message default="Report Type" code="app.label.workflow.reportType"/></th>
                            <th><g:message default="Final State" code="app.label.workflow.rule.initialState"/></th>
                            <th><g:message default="Final State" code="app.label.workflow.rule.targetState"/></th>
                            <th><g:message default="Description" code="app.label.description"/></th>
                            <th><g:message default="Action" code="app.label.action"/></th>
                        </tr>
                    </thead>
                </table>
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