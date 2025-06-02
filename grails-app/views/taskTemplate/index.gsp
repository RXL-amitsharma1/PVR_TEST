<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <asset:javascript src="app/taskTemplateList.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.tasktemplate.title"/></title>
    <script>

        $(function() {
            taskTemplate.taskTemplateList.init_task_template_table('list');
        })

    </script>
<style>
.task-template-table .pv-list-table tbody tr:last-child > td:last-child{
    width: 100px;
}
div.dt-container {
    div.dt-layout-cell.dt-end {
        margin-top: -75px;
    }
}
.dt-container>.dt-layout-table{
    margin-top: 45px;
}
</style>
</head>
<body>
<div class="content">
    <div class="container">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${taskTemplateInstance}" var="theInstance"/>
            <rx:container title="Task Template">
            <div class="navScaffold">
                <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["taskTemplate", "create"]}' id="createButton">
                    <span class="glyphicon glyphicon-plus icon-white"></span>
                    <g:message code="default.button.create.label" /> <g:message code="app.TaskTemplateTypeEnum.REPORT_REQUEST" />
                </button>
                <a class="btn btn-primary" href="${createLink(controller: 'taskTemplate', action: 'create')}?type=${com.rxlogix.enums.TaskTemplateTypeEnum.AGGREGATE_REPORTS}">
                    <span class="glyphicon glyphicon-plus icon-white"></span>
                    <g:message code="default.button.create.label" /> <g:message code="app.TaskTemplateTypeEnum.AGGREGATE_REPORTS" />
                </a>
                <a class="btn btn-primary" href="${createLink(controller: 'taskTemplate', action: 'create')}?type=${com.rxlogix.enums.TaskTemplateTypeEnum.PUBLISHER_SECTION}">
                    <span class="glyphicon glyphicon-plus icon-white"></span>
                    <g:message code="default.button.create.label" /> <g:message code="app.TaskTemplateTypeEnum.PUBLISHER_SECTION" />
                </a>
            </div>


            <div class="pv-caselist task-template-table">
                <table id="taskTemplateList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                    <tr>
                        <th></th>
                        <th><g:message code="app.label.task.templateName"/></th>
                        <th><g:message code="app.label.type"/></th>
                        <th></th>
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