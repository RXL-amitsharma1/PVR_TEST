<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.reportFooter.title"/></title>
    <asset:javascript src="app/odataConfigList.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
<g:javascript>
var odataConfigListUrl="${createLink( action: 'odataSourceList')}"
</g:javascript>
</head>
<body>

<rx:container title="${message(code:"app.odataConfig.title")}">
    <div class="body">
        <div id="action-list-conainter" class="list pv-caselist">

            <g:render template="/includes/layout/flashErrorsDivs"/>

            <div class="navScaffold">
                <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["controlPanel", "odataConfig"]}' id="createBtn">
                    <span class="glyphicon glyphicon-plus icon-white"></span>
                    <g:message code="default.button.create.label" />
                </button>
            </div>

            <div>
                <table id="odataSourcesList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>
                            <th><g:message code="app.label.odataSource.name"/></th>
                            <th><g:message code="app.label.odataSource.url"/></th>
                            <th><g:message code="app.label.odataSource.login"/></th>
                            <th><g:message code="app.label.reportFooter.lastUpdated"/></th>
                            <th><g:message default="Modified By" code="app.label.reportFooter.modifiedBy"/></th>
                            <th><g:message default="Action" code="app.label.action"/></th>
                        </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</rx:container>
<g:form controller="${controller}">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
</body>
</html>