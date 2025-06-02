<head>
    <meta name="layout" content="main">
    <title><g:message code="app.unitConfiguration.title"/></title>
    <style>
        table.dataTable thead tr > th{
            text-align: center;
        }
        table.dataTable tbody {
            text-align: center;
        }
    </style>
    <script>
        $(document).find('#createButton')[0].title=$.i18n._('app.label.icsr.organization.configuration.create');
    </script>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/configuration/unitConfiguration.js"/>
    <g:javascript>
        var UNITCONFIGURATION = {
             listUrl: "${createLink(controller: 'unitConfigurationRest', action: 'list')}",
             editUrl: "${createLink(controller: 'unitConfiguration', action: 'edit')}",
             viewUrl: "${createLink(controller: 'unitConfiguration', action: 'show')}"
        }
        var getAllowedAttachments = "${createLink(controller: 'unitConfigurationRest', action: 'getAllowedAttachments')}";
    </g:javascript>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs"/>
            <rx:container title="${message(code: "app.unitConfiguration.label.myInbox")}" options="true" customButtons="${g.render(template: "includes/customHeaderButtons")}">

                <div class="pv-caselist">
                    <table id="rxTableUnitConfiguration" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                        <thead>
                            <tr>
                                <th><g:message code="app.unitConfiguration.label.unitName"/></th>
                                <th><g:message code="app.unitConfiguration.label.unitType"/></th>
                                <th><g:message code="app.unitConfiguration.label.organizationType"/></th>
                                <th><g:message code="app.unitConfiguration.label.unitRegisteredID"/></th>
                                <th><g:message code="app.unitConfiguration.label.unitRetired"/></th>
                                <th><g:message code="app.label.dateCreated"/></th>
                                <th><g:message code="app.label.dateModified"/></th>
                                <th><g:message code="app.icsrProfileConf.label.profile.owner"/></th>
                                <th class="pv-col-sm"><g:message code="app.label.action"/></th>
                            </tr>
                        </thead>
                    </table>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>
