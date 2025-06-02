<%@ page import="com.rxlogix.enums.ReportSubmissionStatusEnum; com.rxlogix.util.ViewHelper; com.rxlogix.config.ExecutedIcsrReportConfiguration" %>
<html>
<head>
    <asset:javascript src="vendorUi/datatables/custom.jquery.dataTables.columnFilter.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>

    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/icsrReport.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/workFlow.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <script>
        var workflowJustificationUrl = "${createLink(controller: 'workflowJustificationRest', action: 'index')}";
        var workflowJustificationConfirnUrl = "${createLink(controller: 'workflowJustificationRest', action: 'save')}";
        var periodicReportConfig = {
            generateDraftUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "generateDraft")}",
            markAsSubmittedUrl: "${createLink(controller: "reportSubmission", action: "loadIcsrReportSubmissionForm")}",
            reportSubmitUrl: "${createLink(controller: "reportSubmission", action: "submitReport")}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}",
            viewCasesUrl: "${createLink(controller: "caseList", action: "index")}",
            icsrReportsListUrl: "${createLink(controller: "icsrReportConfigurationRest", action: "reportsList")}",
            targetStatesAndApplicationsUrl: "${createLink(controller: "periodicReport", action: "targetStatesAndApplications")}",
            updateReportStateUrl: "${createLink(controller: "periodicReport", action: "updateReportState")}",
            reportViewUrl: "${createLink(controller: "icsrReport", action: "showResult")}",
            generateXML: "${createLink(controller: "report", action: "showXml")}",
            configurationViewUrl: "${createLink(controller: "icsrReport", action: "viewExecutedConfig")}",
            reportingDestinationsUrl: "${createLink(controller: 'queryRest', action: 'getIcsrReportingDestinations')}",
            stateListUrl: "${createLink(controller: 'workflowJustification',action:'getStateListAdhoc')}"
        };
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
        var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'index')}";
        $(function () {
            icsrReport.icsrReportList.init_icsr_report_table();
        });
        var addEmailConfiguration = "${createLink(controller: "report",action: "addEmailConfiguration")}";
        var getSharedWith = "${createLink(controller: 'reportResultRest', action: 'getSharedWithUsers')}";
        var toArchive = "${createLink(controller: 'report', action: 'archive')}";
        var toFavorite = "${createLink(controller: 'report', action: 'favorite')}";

        var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
        var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var hasDmsIntegration = false;
        var dmsFoldersUrl = "${createLink(controller: 'periodicReport', action: 'getDmsFolders')}";
        var addDmsConfiguration = "${createLink(controller: "report",action: "addDmsConfiguration")}";
        var checkDeleteForAllAllowedURL = "${createLink(controller: 'report', action: 'checkDeleteForAllAllowed')}";
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:ExecutedIcsrReportConfiguration.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
        var hasAccessOnActionItem = false;
        var comparisonUrl = "${createLink(controller: "comparison", action: "createCopy")}";
        <sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
        hasAccessOnActionItem = true;
        </sec:ifAnyGranted>
        var APP_ASSETS_PATH = '${request.contextPath}/assets/';
        var isAdmin = false;
        <sec:ifAnyGranted roles="ROLE_ADMIN">
        isAdmin = true;
        </sec:ifAnyGranted>
    </script>
    <g:showIfDmsServiceActive>
        <asset:javascript src="app/configuration/dmsConfiguration.js"/>
        <g:javascript>
            hasDmsIntegration = true;
        </g:javascript>
    </g:showIfDmsServiceActive>

    <meta name="layout" content="main"/>
    <title><g:message code="app.icsrReports.title"/></title>
    <style>
    .dt-layout-row:first-child > .col-xs-7:first-child{
        width: 95%;
    }
    </style>
</head>

<body>
<div class="col-md-12">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfiguration}" var="theInstance"/>

    <rx:container title="${message(code: "app.icsrReports.generated.tittle")}" options="true" filterButton="true">
        <div class="topControls">
            <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: ExecutedIcsrReportConfiguration.name]"/>
            <div class="filterDiv">
                <g:select name="submissionFilter" from="${ViewHelper.getReportSubmissionStatusEnumI18n()}"
                      optionKey="name" optionValue="display" value="" noSelection="['': '']"
                      class="form-control"/>
            </div>
            <g:render template="/includes/widgets/archiveFilter"/>

        </div>
        <div id="icsr-list-conainter" class="list pv-caselist">

                        <table id="icsrReportList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%;">
                            <thead>
                            <tr>
                                <th></th>
                                <th><g:message code="app.periodicReport.executed.reportType.label"/></th>
                        <th style="font-size: 16px"><span class="glyphicon glyphicon-star"></span></th>
                        <th><g:message code="app.periodicReport.executed.reportName.label"/></th>
                        <th><g:message code="app.periodicReport.executed.version.label"/></th>
                        <th><g:message code="app.periodicReport.executed.recipient.label"/></th>
                        <th><g:message code="app.periodicReport.executed.sender.label"/></th>
                        <th><g:message code="app.label.Generated.Date"/></th>
                        <th><g:message code="app.periodicReport.executed.dateModified.label"/></th>
                        <th><g:message code="app.periodicReport.executed.reportOwner.label"/></th>
                        <th><g:message code="app.periodicReport.executed.actions.label"/></th>
                    </tr>
                    </thead>
                </table>

        </div>

    <g:form controller="report" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>
    <g:form controller="report" data-evt-sbt='{"method": "submitForm", "params": []}'>

        <g:hiddenField name="executedConfigId"/>

        <g:render template="/report/includes/sharedWithModal"/>
        <g:render template="/report/includes/emailToModal"/>
        <g:render template="/report/includes/sendToDmsModal"/>

    </g:form>
    <g:render template="/query/workflowStatusJustification"
              model="[tableId: 'icsrReportList', isPeriodicReport: false]"/>
</rx:container>
<g:render template="/email/includes/copyPasteEmailModal"/>
<g:render template="/includes/widgets/confirmation"/>
<g:render template="/includes/widgets/reportSubmission"/>
<g:render template="/actionItem/includes/actionItemModal" model="[]" />

</div>
</body>
</html>
