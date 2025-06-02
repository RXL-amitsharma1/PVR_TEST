<%@ page import="com.rxlogix.enums.FrequencyEnum; com.rxlogix.config.ExecutionStatus; com.rxlogix.util.FilterUtil" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ExecutionStatus.title"/></title>
    <asset:javascript src="/app/utils/pvr-common-util.js"/>
    <asset:javascript src="/app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <g:javascript>
        var executionStatusUrl = "${createLink(controller: 'icsrCaseTrackingRest', action: 'executionStatus')}";
        var killExecutionUrl = "${createLink(controller: 'icsrCaseTrackingRest', action: 'killCaseExecution')}";
        var showConfigUrl = "${createLink(controller: 'executedIcsrProfile', action: 'view')}";
    </g:javascript>
    <asset:javascript src="/app/icsr/executionStatus.js"/>
    <asset:stylesheet src="/executionStatus.css"/>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs"/>
            <rx:container title="${message(code: "app.label.ExecutionStatus")}" options="${true}" customButtons="${g.render(template: "includes/customHeaderButtons",model: [status:status])}">

                <div class="pv-caselist">
                    <table id="rxTableReportsExecutionStatus" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.reportName"/></th>
                            <th><g:message code="app.label.caseNum"/></th>
                            <th><g:message code="app.label.version"/></th>
                            <th><g:message code="app.label.executionStatus"/></th>
                            <th><g:message code="app.label.runDate"/></th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>