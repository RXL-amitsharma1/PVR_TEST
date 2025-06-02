<%@ page import="com.rxlogix.config.ExecutedConfiguration; com.rxlogix.enums.ReportFormatEnum" %>

<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.icsrReport.title" /></title>

    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/report/icsrReport.js"/>
    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/workFlow.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>

    <g:javascript>
        var icsrReportsListUrl = "${createLink(controller: 'reportResultRest', action:'showExecutedICSRReports')}";
        var showReportUrl = "${createLink(controller: 'report', action: 'showIcsrReport')}";
        var updateStatusUrl = "${createLink(controller: 'report', action: 'updateStatus')}";
        var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest',action: 'index')}";
        var getSharedWith = "${createLink(controller: 'reportResultRest', action: 'getSharedWithUsers')}";
        var getEmailTo = "${createLink(controller: 'reportResultRest', action: 'getEmailToUsers')}";
        var targetStatesAndApplicationsUrl= "${createLink(controller: "periodicReport", action: "targetStatesAndApplications")}";
        var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest',action: 'save')}";
        var addEmailConfiguration="${createLink(controller: "report",action: "addEmailConfiguration")}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var checkDeleteForAllAllowedURL = "${createLink(controller: 'report', action: 'checkDeleteForAllAllowed')}";
        var LINKS = {
            toShare : "${createLink(controller: 'report', action: 'share')}",
            toEmail : "${createLink(controller: 'report', action: 'email')}"
        };
    </g:javascript>
    <script>
        $(function () {
            //Initiate the datatable
            icsrReport.icsrReportList.init_icsr_report_table();
        })
    </script>
</head>
<body>
<div class="content ">
    <div class="container ">
        <div>
            <rx:container title="${message(code: message(code:"app.label.myInbox"))}" options="${true}" filterButton="true" >

        <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfiguration}" var="theInstance"/>
        <div class="pv-caselist">
        <table id="icsrReportsList" class="table table-striped pv-list-table dataTable no-footer" data-i-display-length="50"
               data-a-length-menu="[[50, 100, 200, 500], [50, 100, 200, 500]]" width="100%">
            <thead>
            <tr>
                <th class="reportNameColumn"><g:message code="app.label.reportName"/></th>
                <th class="reportDescriptionColumn"><g:message code="app.label.description"/></th>
                <th><g:message code="app.label.version"/></th>
                <th><g:message code="app.label.owner"/></th>
                <th><g:message code="app.label.generatedOn"/></th>
                <th><g:message code="app.label.tag"/></th>
                <th><g:message code="app.periodicReport.executed.workflowState.label"/></th>
                <th><g:message code="app.label.icsr.error"/></th>
                <th><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
        </div>
        <g:form controller="report" data-evt-sbt='{"method": "submitForm", "params": []}'>
            <g:hiddenField name="executedConfigId"/>

            <g:render template="includes/sharedWithModal"/>
            <g:render template="includes/emailToModal"/>
        </g:form>

        <g:render template="/query/workflowStatusJustification"
                  model="[tableId: 'rxTableReports', isPeriodicReport: false]"/>
        <g:render template="/icsr/includes/icsrErrorModal"/>
        <g:render template="/email/includes/copyPasteEmailModal"/>
    </rx:container>
        </div>
    </div>
</div>
</body>
