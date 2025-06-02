<%@ page import="com.rxlogix.config.ExecutedConfiguration; com.rxlogix.enums.ReportFormatEnum" %>

<head>
    <meta name="layout" content="main"/>
    <style>
    .es-error {
        color: #d9534f;
    }
    .es-completed {
        color: #5cb85c;
    }
    </style>
    <title><g:message code="app.pvcentral.inbound.compliance.title" /></title>

    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/inboundCompliance/executedInboundList.js"/>
    <g:javascript>
        var indexReportUrl = "${createLink(controller: 'inboundComplianceRest', action:'generatedList')}";
        var viewExConfigUrl = "${createLink(controller: 'executedInbound', action: 'viewExecutedConfig')}";
        var showReportUrl = "${createLink(controller: 'executedInbound', action: 'showReport')}";
        var deleteUrl = "${createLink(controller: 'report', action: 'showIcsrReport')}";
    </g:javascript>
</head>
<body>
    <div class="content">
        <div class="container">
            <div>
                <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>
                <rx:container title="${message(code: message(code:"app.pvcentral.inbound.compliance.label"))}" options="${true}" >
                    <div class="pv-caselist">
                        <table id="rxTableReports" class="table table-striped pv-list-table dataTable no-footer" data-i-display-length="50" data-a-length-menu="[[50, 100, 200, 500], [50, 100, 200, 500]]" width="100%">
                            <thead>
                                <tr>
                                    <th><g:message code="app.label.sender.name" /></th>
                                    <th><g:message code="app.label.description" /></th>
                                    <th><g:message code="app.label.owner" /></th>
                                    <th><g:message code="app.label.generatedOn" /></th>
                                    <th><g:message code="app.label.tag" /></th>
                                    <th><g:message code="app.label.action"/></th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <g:form controller="${controller}" method="delete">
                        <g:render template="/includes/widgets/deleteRecord"/>
                    </g:form>
                    <g:render template="/includes/widgets/confirmation"/>
                </rx:container>
            </div>
        </div>
    </div>
</body>
