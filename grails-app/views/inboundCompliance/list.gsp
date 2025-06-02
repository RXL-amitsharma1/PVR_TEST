<head>
    <meta name="layout" content="main">
    <title><g:message code="app.pvcentral.inbound.reportLibrary.title"/></title>
    <style>
    table.dataTable thead tr > th{
        text-align: center;
    }
    table.dataTable tbody {
        text-align: center;
    }
    </style>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/inboundCompliance/inboundList.js"/>
    <g:javascript>
         var listUrl = "${createLink(controller: 'inboundComplianceRest', action: 'list')}";
         var viewUrl = "${createLink(controller: 'inboundCompliance', action: 'view')}";
         var editUrl = "${createLink(controller: 'inboundCompliance', action: 'edit')}";
         var deleteUrl = "${createLink(controller: 'inboundCompliance', action: 'delete')}";
         var initializeUrl = "${createLink(controller: 'inboundCompliance', action: 'initialize')}";
    </g:javascript>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs"/>
            <rx:container title="${message(code: "app.pvcentral.inbound.reportLibrary.label")}" options="${true}">
                <div class="pv-caselist">
                    <table id="rxTableInboundCompliance" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                        <thead>
                        <tr>
                            <th style="min-width: 200px"><g:message code="app.label.sender.name"/></th>
                            <th style="min-width: 200px"><g:message code="app.label.description"/></th>
                            <th  style="min-width: 200px"><g:message code="app.label.tag"/></th>
                            <th style="width: 70px"><g:message code="app.label.qc" default="QCed"/></th>
                            <th  style="width: 150px;"><g:message code="app.label.dateCreated"/></th>
                            <th style="width: 150px;"><g:message code="app.label.dateModified"/></th>
                            <th style="width: 150px;"><g:message code="app.label.owner"/></th>
                            <th  style="width: 70px;" class="pv-col-sm"><g:message code="app.label.action"/></th>
                        </tr>
                        </thead>
                    </table>
                    <g:form controller="${controller}" method="delete">
                        <g:render template="/includes/widgets/deleteRecord"/>
                    </g:form>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>