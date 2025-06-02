<html>
<head>
    <meta name="layout" content="main"/>
    <script type="application/javascript">
        var submittedCasesConfig = {
            submitted_cases_list_url: "${createLink(controller: "reportSubmissionRest", action: "casesList", id: reportSubmission.id)}"
        };
        var dateRangeType = "${reportSubmission.executedReportConfiguration.dateRangeType.toString()}";
    </script>
    <asset:javascript src="app/submittedCasesList.js"/>
    <asset:javascript src="app/submittedCases.js"/>
</head>

<body>
<rx:container title="${message(code: "app.label.reportSubmission.cases", args: [reportSubmission.executedReportConfiguration.reportName, reportSubmission.reportingDestination])}">
    <div class="body">
        <div id="action-list-conainter" class="list pv-caselist">
            <div>
                <table id="submittedCasesList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                    <thead>
                    <tr>
                        <th><g:message code="app.label.reportSubmission.cases.caseType"/></th>
                        <th><g:message code="app.label.reportSubmission.cases.caseNumber"/></th>
                        <th><g:message code="app.label.reportSubmission.cases.versionNumber"/></th>
                        <th><g:message code="app.label.reportSubmission.cases.eventSequenceNumber"/></th>
                        <th><g:message code="app.label.reportSubmission.cases.eventReceiptDate"/></th>
                        <th><g:message code="app.label.reportSubmission.cases.eventPreferredTerm"/></th>
                        <th><g:message code="app.label.reportSubmission.cases.eventSeriousness"/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</rx:container>

</body>
</html>