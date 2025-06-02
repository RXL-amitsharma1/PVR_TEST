<html>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/emailList.js"/>
    <asset:javascript src="app/email.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <title><g:message code="app.emailConfiguration.list.title"/></title>
</head>
<body>
<div class="content">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${email}" var="theInstance"/>

            <rx:container title="${message(code:"app.label.email.appName")}">
                <div class="body">
        <div id="action-list-conainter" class="list">

                <div class="pull-right" style="cursor: pointer; text-align: right; position: relative;margin-right:15px;">
                    <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["email", "create"]}'>
                        <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.email.configuration.create')}" style="color: #353d43;"></i>
                    </a>
                </div>
            <div class="pv-caselist">
                <table id="emailList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>
                            <th></th>
                            <th><g:message default="Email text" code="app.label.email.email"/></th>
                            <th><g:message default="Description" code="app.label.email.description"/></th>
                            <th><g:message default="Last Updated" code="app.label.email.lastUpdated"/></th>
                            <th><g:message default="Modified By" code="app.label.email.modifiedBy"/></th>
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