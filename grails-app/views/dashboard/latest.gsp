<%@ page import="com.rxlogix.enums.NotificationApp; com.rxlogix.enums.WidgetTypeEnum; com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.enums.ReportFormatEnum; com.rxlogix.ChartOptionsUtils" contentType="text/html;charset=UTF-8" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.MyInbox.title"/></title>
    <asset:javascript src="app/dashboard/addWidgetModal.js"/>
    <asset:javascript src="app/dashboard/dashboard.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:javascript src="vendorUi/fullcalendar/fullcalendar.min.js"/>
    <asset:javascript src="vendorUi/fullcalendar/fullcalendar-lang-all.js"/>

    <asset:javascript src="app/calendar.js"/>
    <asset:javascript src="app/workFlow.js"/>

    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highstock.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/modules/no-data-to-display.js"/>
    <asset:javascript src="vendorUi/highcharts/plugins/grouped-categories-1.3.2.js"/>
    <asset:javascript src="vendorUi/gridstack/gridstack.min.js"/>
    <asset:javascript src="vendorUi/gridstack/gridstack.jQueryUI.min.js"/>
    <asset:stylesheet src="vendorUi/gridstack.min.css"/>
    <asset:stylesheet src="dashboard.css"/>
    <asset:stylesheet src="vendorUi/fullcalendar/fullcalendar.min.css"/>
    <asset:stylesheet src="vendorUi/fullcalendar/fullcalendar.print.css" media="print"/>
    <asset:javascript src="app/dashboard/latest.js"/>
    <g:javascript>
        var indexReportUrl = "${createLink(controller: 'reportResultRest', action: 'latestAdhocReport')}";
        var periodicReportUrl = "${createLink(controller: 'periodicReportConfigurationRest', action: 'latestPeriodicReport')}";
        var actionItemUrl = "${createLink(controller: 'actionItemRest', action: 'index')}";
        var showReportUrl = "${createLink(controller: 'report', action: 'criteria')}";
        var updateStatusUrl = "${createLink(controller: 'report', action: 'updateStatus')}";
        var deleteReport = "${createLink(controller: 'report', action: 'deleteReport', params: [relatedPage: related])}";
        var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest', action: 'index')}"
        var getSharedWith = "${createLink(controller: 'reportResultRest', action: 'getSharedWithUsers')}";
        var getEmailTo = "${createLink(controller: 'reportResultRest', action: 'getEmailToUsers')}";
        var targetStatesAndApplicationsUrl= "${createLink(controller: "periodicReport", action: "targetStatesAndApplications")}";
        var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest', action: 'save')}";
        var addEmailConfiguration="${createLink(controller: "report", action: "addEmailConfiguration")}";

        var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
        var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'index')}";
        var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";

        // Calendar specific URLs
        var eventsUrl = "${createLink(controller: "calendar", action: "events")}";
        var createReportRequestUrl = "${createLink(controller: 'reportRequest', action: 'create')}";
        var deleteActionItemUrl = "${createLink(controller: 'actionItem', action: 'delete')}";
        var reportRequestShowURL = "${createLink(controller: 'reportRequest', action: 'show')}";
        var executedReportShowURL = "${createLink(controller: 'report', action: 'showFirstSection')}";
        var adhocReportShowURL = "${createLink(controller: 'configuration', action: 'view')}";
        var periodicReportShowURL = "${createLink(controller: 'periodicReport', action: 'view')}";
        var icsrReportShowURL = "${createLink(controller: "icsrReport", action: "view")}";

        var adhocReportsSummaryUrl="${createLink(controller: 'dashboard', action: 'getAdhocSummary')}";
        var aggregateReportsSummaryUrl="${createLink(controller: 'dashboard', action: 'getAggregateSummary')}";
        var actionItemSummaryUrl="${createLink(controller: 'dashboard', action: 'getActionItemSummary')}";
        var reportRequestSummaryUrl="${createLink(controller: 'dashboard', action: 'getReportRequestSummary')}";

        var LINKS = {
            toPDF : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.PDF])}",
            toExcel : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.XLSX])}",
            toWord : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.DOCX])}",
            toPowerPoint : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.PPTX])}",
            toShare : "${createLink(controller: 'report', action: 'share')}",
            toEmail : "${createLink(controller: 'report', action: 'email')}"
        }
        var CONFIGURATION = {
             listUrl: "${createLink(controller: 'configurationRest', action: 'index', params: [mixedTypes: true])}",
             addWidgetUrl:"${createLink(controller: 'dashboard', action: 'addReportWidget')}",
             removeWidgetUrl: "${createLink(controller: 'dashboard', action: 'removeReportWidgetAjax')}",
             updateWidgetsUrl: "${createLink(controller: 'dashboard', action: 'updateReportWidgetsAjax')}",
             getChartDataUrl: "${createLink(controller: 'dashboard', action: 'getChartWidgetDataAjax')}",
             templateType: "${TemplateTypeEnum.DATA_TAB.key}"
        }

        var adhocExecutedIndexPage="${createLink(controller: 'report', action: 'index')}";
        var aggregateExecutedIndexPage="${createLink(controller: 'periodicReport', action: 'reports')}";
        var actonItemIndexPage="${createLink(controller: 'actionItem', action: 'index')}";
        var reportRequestIndexPage="${createLink(controller: 'reportRequest', action: 'index')}";
        var rowNum=${params.rowNum ?: '5'}
    </g:javascript>
</head>

<body>

<!-- Page-Title -->

<div class="container-fluid">


    <%@ page import="com.rxlogix.enums.ReportFormatEnum" %>
        <div id="container" class="row rx-widget-content nicescroll">
            <div class="pv-caselist">
                <table id="rxTableLastReports" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                    <thead>
                    <tr>
                        <th><g:message code="app.label.reportType"/></th>
                        <th class="reportNameColumn"><g:message code="app.label.reportName"/></th>
                        <th align="center"><g:message code="app.label.version"/></th>
                        <th align="center" class="reportDescriptionColumn"><g:message code="app.label.description"/></th>
                        <th align="center"><g:message code="app.label.generatedOn"/></th>
                        <th align="center"><g:message code="app.periodicReport.executed.workflowState.label"/></th>
                        <th align="center"><g:message code="app.label.action.item.table.column"/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    <g:render template="/query/workflowStatusJustification" model="[tableId:'rxTableReports', isPeriodicReport: true]"/>
    <!-- end row -->
    <g:render template="/includes/widgets/deleteRecord"/>

    <g:render template="/actionItem/includes/actionItemModal" model="[]"/>
</div>

</body>
