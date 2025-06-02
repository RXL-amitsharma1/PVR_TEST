<html>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/publisher/ganttList.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <title>
        <g:if test="${params.pvp}"><g:message code="app.Pvpgantt.list.title"/></g:if>
        <g:else><g:message code="app.gantt.list.title"/></g:else>
    </title>
    <style>
    .top {
        padding-right: 60px;
        padding-left: 133px;
    }
    </style>
</head>
<body>
<script>
    var ganttListUrl="${createLink(controller: "gantt", action: "list")}"
</script>
<div class="content">
    <div class="container">
        <div class="row">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${gantt}" var="theInstance"/>
<rx:container title="${message(code:"app.label.gantt.appName")}">


    <div class="pull-right" style="cursor: pointer; text-align: right; position: relative; margin-top: -37px; margin-right: 15px">
        <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["gantt", "create"]}'>
            <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.report.request.create')}" style="color: #353d43;"></i>
        </a>
    </div>



            <div class="basicDataTable pv-caselist">
                <table id="ganttList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>

                            <th><g:message code="app.label.gantt.name"/></th>
                            <th><g:message code="app.label.email.lastUpdated"/></th>
                            <th><g:message code="app.label.email.modifiedBy"/></th>
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