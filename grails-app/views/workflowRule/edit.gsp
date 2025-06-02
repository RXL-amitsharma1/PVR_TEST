<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'app.label.workflow.rule.appName')}"/>
    <title><g:message code="app.workflowRule.edit.title"/></title>
    <asset:javascript src="app/WorkflowRuleEdit.js"/>
</head>

<body><div class="content">
    <div class="container">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5 class="page-header-settings"><g:message code="default.edit.label" args="[entityName]"/></h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div  class="settings-content">
<rx:container title="${message(code: 'app.label.workflow.rule.appName')}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${workflowRuleInstance}" var="theInstance"/>

    <g:form action="update" class="form-horizontal">
        <g:render template="includes/form"
                  model="[mode: 'show', workflowRuleInstance: workflowRuleInstance, initialStates: initialStates, targetStates: targetStates]"/>

        <div class="buttonBar text-right">
            <button name="edit" class="btn btn-primary">
                <span><g:message code="default.button.update.label"/></span>
            </button>
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["workflowRule", "index"]}'
                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
    </g:form>
</rx:container>
        </div>
    </div>
</div>
</body>
</html>