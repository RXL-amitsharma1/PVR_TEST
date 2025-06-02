<html>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/dashboardDictionaryList.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <title><g:message code="app.dashboardLibrary.title"/></title>
</head>
<body>
<g:javascript>
      var listUrl= "${createLink(controller: 'dashboardDictionary', action: 'list')}";
      var dasboardURL = "${createLink(controller: 'dashboard', action: 'index')}";
</g:javascript>
<style>

div.dt-layout-cell.dt-end {
    margin-top:-30px;
    margin-right:5px;
}
</style>
<div class="content">
    <div class="container">
        <div><g:render template="/includes/layout/flashErrorsDivs" bean="${dashboard}" var="theInstance"/>

            <rx:container title="${message(code:"app.DashboardLibrary.label")}">
                <div class="row" style="margin-bottom: 5px;">

                    <form action="importJson" method="post">
                        <div class="col-md-6">
                            <textarea class="form-control" style="height: 25px;" name="json"></textarea>
                        </div>
                        <div class="col-md-1">
                            <button type="submit" class="btn btn-xs btn-primary"><g:message code="controlPanel.upload.json.files"/></button>
                        </div>

                    </form>
                </div>
            <div class="pull-right" style="cursor: pointer; text-align: right; position: relative; margin-top: -67px; margin-right: 15px">
                <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["dashboardDictionary", "create"]}'>
                    <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.dashboard.create')}" style="color: #353d43;"></i>
                </a>

            </div>
            <div class="pv-caselist basicDataTable">
                <table id="dashboardList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>
                            <th><g:message default="Label" code="app.label.dashboardDictionary.label"/></th>
                            <th><g:message default="Owner" code="app.label.dashboardDictionary.owner"/></th>
                            <th><g:message default="Type" code="app.label.dashboardDictionary.type"/></th>
                            <th><g:message default="Shared With Users" code="app.label.dashboardDictionary.sharedWithUsers"/></th>
                            <th><g:message default="Shared With Groups" code="app.label.dashboardDictionary.sharedWithGroups"/></th>
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