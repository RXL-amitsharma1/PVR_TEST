<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'app.label.workflow.rule.appName')}"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
    <asset:javascript src="app/WorkflowRuleEdit.js"/>
    <script type="text/javascript">
        $(function() {
            $('.workflowRuleField').attr("disabled", true);
        })
    </script>
</head>

<body><div class="content">
    <div class="container">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5 class="page-header-settings"><g:message code="default.show.label" args="[entityName]"/></h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div  class="settings-content">
<rx:container title="${message(code: 'app.label.workflow.rule.appName')}">


        <g:render template="includes/form" model="[mode: 'show', workflowRuleInstance: workflowRuleInstance, initialStates: initialStates, targetStates: targetStates, advancedAssignmentList: advancedAssignmentList]"/>


        <div class="buttonBar text-right">
            <g:link controller="workflowRule" action="edit" id="${workflowRuleInstance.id}"
                    class="btn btn-primary"><g:message code="default.button.edit.label"/></g:link>
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["workflowRule", "index"]}'
                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
</rx:container>
        </div>
    </div>
</div>
</body>
</html>