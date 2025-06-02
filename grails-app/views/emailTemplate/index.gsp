<html>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/emailTemplateList.js"/>

    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <title><g:message code="app.emailTemplateConfiguration.list.title"/></title>
    <g:javascript>
       var listUrl="${createLink(controller: 'emailTemplate', action: 'list')}";
    </g:javascript>
</head>
<body>
<div class="content">
    <div class="container">
        <div><g:render template="/includes/layout/flashErrorsDivs" bean="${emailTemplate}" var="theInstance"/>
            <rx:container title="${message(code:"app.label.emailTemplate.appName")}">
                <div class="body">
        <div id="action-list-conainter" class="list">

                <div class="pull-right" style="cursor: pointer; text-align: right; position: relative;margin-right:15px;">
                    <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["emailTemplate", "create"]}'>
                        <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.email.template.create')}" style="color: #353d43;"></i>
                    </a>
                </div>

            <div class="pv-caselist">
                <table id="emailTemplateList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>
                            <th><g:message default="Name" code="app.label.name"/></th>
                            <th><g:message default="Description" code="app.label.emailConfiguration.subject"/></th>
                            <th width="120"><g:message default="Owner" code="app.label.owner"/></th>
                            <th width="100"><g:message default="Type" code="app.label.type"/></th>
                            <th width="120"><g:message default="Last Updated" code="app.label.email.lastUpdated"/></th>
                            <th width="120"><g:message default="Modified By" code="app.label.email.modifiedBy"/></th>
                            <th width="80"><g:message default="Action" code="app.label.action"/></th>
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