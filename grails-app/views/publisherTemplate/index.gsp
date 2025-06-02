<html>
<head>
    <meta name="layout" content="main"/>
    <script>
        var listPublisherTemplateUrl="${createLink(controller: 'publisherTemplate', action: 'list')}";
    </script>
    <asset:javascript src="app/publisher/publisherTemplateList.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <title><g:message code="app.PublisherTemplate.list.title"/></title>
</head>
<body>
<div class="content">
    <div class="container">
        <div class="row">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
<rx:container title="${message(code:"app.label.PublisherTemplate.appName")}">
    <div class="body">
        <div id="action-list-conainter" class="list pv-caselist">


            <div class="pull-right" style="cursor: pointer; text-align: right; position: relative; margin-top: -37px; margin-right: 15px">
                <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["publisherTemplate", "create"]}'>
                    <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.report.request.create')}" style="color: #353d43;"></i>
                </a>
            </div>

            <div class="pv-caselist basicDataTable">
                <table id="publisherTemplateList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>
                            <th></th>
                            <th><g:message code="app.label.name"/></th>
                            <th><g:message code="app.label.description"/></th>
                            <th><g:message default="Quality Checked" code="app.label.qualityChecked"/></th>
                            <th><g:message default="Date Modified" code="app.label.dateModified"/></th>
                            <th><g:message default="Modified By" code="app.label.modifiedBy"/></th>
                            <th><g:message default="Action" code="app.label.action"/></th>
                        </tr>
                    </thead>
                </table>
            </div>

</rx:container>
        </div>
    </div>
</div>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
</body>
</html>