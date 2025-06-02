<%@ page import="com.rxlogix.enums.ReportSubmissionStatusEnum; com.rxlogix.util.ViewHelper; com.rxlogix.config.ExecutedPeriodicReportConfiguration" %>
<html>
<head>
    <title><g:message code="app.periodicReports.title"/></title>
    <meta name="layout" content="main"/>
    <asset:javascript src="vendorUi/datatables/custom.jquery.dataTables.columnFilter.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>

    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/periodicReport.js"/>
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
            comparisonUrl: "${createLink(controller: "comparison", action: "createCopy")}",
            generateDraftUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "generateDraft")}",
            markAsSubmittedUrl: "${createLink(controller: "reportSubmission", action: "loadReportSubmissionForm")}",
            reportSubmitUrl: "${createLink(controller: "reportSubmission", action: "submitReport")}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}",
            viewCasesUrl: "${createLink(controller: "caseList", action: "index")}",
            reportsListUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "reportsList")}",
            targetStatesAndApplicationsUrl: "${createLink(controller: "periodicReport", action: "targetStatesAndApplications")}",
            updateReportStateUrl: "${createLink(controller: "periodicReport", action: "updateReportState")}",
            reportViewUrl: "${createLink(controller: "report", action: "showFirstSection")}",
            configurationViewUrl: "${createLink(controller: "periodicReport", action: "viewExecutedConfig")}",
            reportingDestinationsUrl: "${createLink(controller: 'queryRest', action: 'getReportingDestinations')}",
            stateListUrl: "${createLink(controller: 'workflowJustificationRest',action:'getStateListAdhoc')}",
        };
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
        var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'index')}";
        $(function () {
            periodicReport.periodicReportList.init_periodic_report_table();
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
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:ExecutedPeriodicReportConfiguration.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
        var hasAccessOnActionItem = false;
        <sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
        hasAccessOnActionItem = true;
        </sec:ifAnyGranted>
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
    <style>
    .dt-layout-row:first-child > .col-xs-7:first-child{
        width: 95%;
    }
    </style>

</head>

<body>
<div class="content">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfiguration}" var="theInstance"/>

            <rx:container title="${message(code: "app.periodicReports.generated.tittle")}" options="true" filterButton="true">


    <div class="body">
        <div id="periodic-list-conainter" class="list pv-caselist">
            <div class="topControls">
                <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: ExecutedPeriodicReportConfiguration.name]"/>
                <div class="filterDiv">
                    <g:select name="submissionFilter" from="${ViewHelper.getReportSubmissionStatusEnumI18n()}" placeholder="${message(code:'report.submission.status')}"
                              optionKey="name" optionValue="display" value="" noSelection="['':'']"
                              class="form-control"/>
                </div>
                <g:render template="/includes/widgets/archiveFilter"/>
            </div>
                <table id="periodicReportList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%;">
                    <thead>
                    <tr>
                        <th></th>
                        <th><g:message code="app.periodicReport.executed.reportType.label"/></th>
                        <th style="font-size: 16px;width: 4%;"><span style="top:4px;" class="glyphicon glyphicon-star"></span></th>
                        <th style="width: 200px"><g:message code="app.periodicReport.executed.reportName.label"/></th>
                        <th style="width: 50px"><g:message code="app.label.versionName"/></th>
                        <th><g:message default="License" code="app.label.reportSubmission.license"/></th>
                        <th><g:message code="app.periodicReport.executed.version.label"/></th>
                        <th><g:message code="app.periodicReport.executed.productName.label"/></th>
                        <th><g:message code="app.periodicReport.executed.reportPeriod.label"/></th>
                        <th style="width: 110px"><g:message code="app.label.reportingDestinations"/></th>
                        <th style="width: 110px"><g:message code="app.periodicReport.executed.daysLeft.label"/></th>
                        <th style="width: 150px;"><g:message code="app.periodicReport.executed.dateModified.label"/></th>
                        <th style="width: 90px;"><g:message code="app.periodicReport.executed.reportOwner.label"/></th>
                        <th style="width: 110px"><g:message code="app.label.tag" /></th>
                        <th style="width: 80px;"><g:message code="app.periodicReport.executed.workflowState.label"/></th>
                        <th style="width: 80px;"></th>
                        <th class="col-min-60" style="width: 70px;"><g:message code="app.periodicReport.executed.actions.label"/></th>
                    </tr>
                    </thead>
                </table>

        </div>
    </div>
    <g:form controller="report" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>
    <g:form controller="report" data-evt-sbt='{"method": "submitForm", "params": []}'>

        <g:hiddenField name="executedConfigId"/>

        <g:render template="/report/includes/sharedWithModal"/>
        <g:render template="/report/includes/emailToModal"/>
        <g:render template="/report/includes/sendToDmsModal"/>
        <g:render template="/includes/widgets/errorTemplate" model="[messageBody:message(code: 'app.dms.config.error'), errorModalId:'dmsErrorModal']"/>

    </g:form>
    <g:render template="/query/workflowStatusJustification"
              model="[tableId: 'periodicReportList', isPeriodicReport: true]"/>
</rx:container>
<g:render template="/email/includes/copyPasteEmailModal"/>
<g:render template="/includes/widgets/confirmation"/>
<g:render template="/includes/widgets/reportSubmission"/>
<g:render template="/actionItem/includes/actionItemModal" model="[]" />
        </div>
    </div>
</div>
<g:render template="includes/submissionCapaModal"/>
</body>
</html>
