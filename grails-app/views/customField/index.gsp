<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.customField.title"/></title>
    <asset:javascript src="app/customFieldList.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
</head>
<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${customFieldInstance}" var="theInstance"/>

            <rx:container title="${message(code:"app.label.customField.appName")}">
    <div class="body">
        <div id="action-list-conainter" class="list">

            <div class="pull-right" style="cursor: pointer; text-align: right; position: relative;margin-right:15px;">
                <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["customField", "create"]}'>
                    <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.custom.field.create')}" style="color: #353d43;"></i>
                </a>

            </div>

            <div class="pv-caselist">
                <table id="customFieldList" class="table table-striped pv-list-table dataTable no-footer no-hyphens">
                    <thead>
                        <tr>
                            <th><g:message default="Name" code="app.label.name"/></th>
                            <th><g:message default="Description" code="app.label.description"/></th>
                            <th><g:message default="Field" code="app.label.customField.field"/></th>
                            <th width="150"><g:message default="Last Updated" code="app.label.reportFooter.lastUpdated"/></th>
                            <th width="150"><g:message default="Modified By" code="app.label.modifiedBy"/></th>
                            <th width="90"><g:message default="Action" code="app.label.action"/></th>
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