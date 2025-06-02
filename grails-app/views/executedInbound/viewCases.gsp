<%@ page import="com.rxlogix.config.ExecutedConfiguration; com.rxlogix.enums.ReportFormatEnum" %>

<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.pvcentral.inbound.viewCases.title" /></title>

    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/inboundCompliance/resultInboundList.js"/>
    <g:javascript>
        var showReportUrl = "${createLink(controller: 'inboundComplianceRest', action: 'result')}?id=${params.id}";
    </g:javascript>
</head>
<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: message(code:"app.pvcentral.inbound.viewCases.label"))}" options="${true}">
                <div class="pv-caselist">
                    <table id="resultTableReports" class="table table-striped pv-list-table dataTable no-footer" data-i-display-length="50" data-a-length-menu="[[50, 100, 200, 500], [50, 100, 200, 500]]" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.sender.name" /></th>
                            <th><g:message code="app.label.queryName" /></th>
                            <th><g:message code="app.label.criteria.name" /></th>
                            <th><g:message code="app.caseNumber.label" /></th>
                            <th><g:message code="app.label.version" /></th>
                            <th><g:message code="app.label.sender.receipt.date"/></th>
                            <th><g:message code="app.label.safety.receipt.date"/></th>
                            <th><g:message code="app.label.case.creation.date"/></th>
                            <th><g:message code="app.label.days.to.process"/></th>
                            <th><g:message code="app.label.status" /></th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>
