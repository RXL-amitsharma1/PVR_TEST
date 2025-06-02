<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.reportFooter.title"/></title>
    <asset:javascript src="app/reportFooterList.js"/>
    <asset:javascript src="app/reportFooter.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
</head>
<body>
<div class="content">
    <div class="container">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${reportFooterInstance}" var="theInstance"/>
            <rx:container title="${message(code: "app.label.reportFooter.appName")}">
                <div class="body">
        <div id="action-list-conainter" class="list">

                <div class="pull-right" style="cursor: pointer; text-align: right; position: relative;margin-right:15px;">
                    <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["reportFooter", "create"]}'>
                        <i class="md-plus" data-tooltip="tooltip" data-placement="bottom"
                           title="${message(code: 'app.label.footer.configuration.create')}" style="color: #353d43;"></i>
                    </a>
                </div>

                <div class="pv-caselist">
                    <table id="reportFooterList" class="table table-striped pv-list-table dataTable no-footer">
                        <thead>
                        <tr>
                            <th></th>
                            <th><g:message default="Footer text" code="app.label.reportFooter.footer"/></th>
                            <th><g:message default="Description" code="app.label.reportFooter.description"/></th>
                            <th><g:message default="Last Updated" code="app.label.reportFooter.lastUpdated"/></th>
                            <th><g:message default="Modified By" code="app.label.reportFooter.modifiedBy"/></th>
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