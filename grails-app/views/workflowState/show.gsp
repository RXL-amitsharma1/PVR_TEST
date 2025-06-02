<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'app.label.workflow.appName')}"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
    <script type="text/javascript">
        $(function() {
            $('.workflowStateField').attr("disabled", true);
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
<rx:container title="${message(code: 'app.label.workflow.appName')}">
        <g:render template="includes/form" model="['mode':'show', workflowStateInstance:workflowStateInstance]" />
        <div class="buttonBar text-right">
            <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["workflowState", "edit", {"id": ${workflowStateInstance.id}}]}'
                    id="${workflowStateInstance.id}"><g:message code="default.button.edit.label"/></button>
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["workflowState", "index"]}'
                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
</rx:container>

        </div>
    </div>
</div>
</body>
</html>