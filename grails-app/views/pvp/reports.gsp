<%@ page import="com.rxlogix.enums.ReportSubmissionStatusEnum; com.rxlogix.util.ViewHelper; com.rxlogix.config.ExecutedPeriodicReportConfiguration" %>
<html>
<head>
    <g:set var="applicationSettingsService" bean="applicationSettingsService"/>

    <asset:javascript src="vendorUi/datatables/custom.jquery.dataTables.columnFilter.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>

    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/periodicReport.js"/>
    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/workFlow.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <g:if test="${applicationSettingsService.hasDmsIntegration()}">
        <asset:javascript src="app/configuration/dmsConfiguration.js"/>
    </g:if>
    <asset:stylesheet src="copyPasteModal.css"/>
    <g:javascript>
        var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest', action: 'index')}";
        var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest', action: 'save')}";
    </g:javascript>
    <script>
        var periodicReportConfig = {
            comparisonUrl: "${createLink(controller: "comparison", action: "createCopy")}",
            generateDraftUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "generateDraft")}",
            markAsSubmittedUrl: "${createLink(controller: "reportSubmission", action: "loadReportSubmissionForm")}",
            reportSubmitUrl: "${createLink(controller: "reportSubmission", action: "submitReport")}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}",
            viewCasesUrl: "${createLink(controller: "caseList", action: "index")}",
            reportsListUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "reportsList")}",
            targetStatesAndApplicationsUrl: "${createLink(controller: "periodicReport", action: "targetStatesAndApplications")}",
            updateReportStateUrl: "${createLink(controller: "periodicReport", action: "updateReportState")}",
            reportViewUrl: "${createLink(controller: "pvp", action: "sections")}",
            configurationViewUrl: "${createLink(controller: "periodicReport", action: "viewExecutedConfig")}",
            reportingDestinationsUrl: "${createLink(controller: 'queryRest', action: 'getReportingDestinations')}",
            stateListUrl: "${createLink(controller: 'workflowJustificationRest',action:'getStateListAdhoc')}",
            downloadPublisherFileURL: "${createLink(controller: 'pvp', action: 'downloadPublisherReport', absolute: true)}",
            publisher: true
        };

        var pvpFullDocumentUrl = "${createLink(controller: "pvp", action: "pvpFullDocuments")}";
        var pvpSectionsUrl = "${createLink(controller: "pvp", action: "pvpSections")}";
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
        var hasDmsIntegration = ${applicationSettingsService.hasDmsIntegration()};
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
<style>
.basicDataTable .top {
    padding-right: 60px;
    padding-left: 133px;
}
</style>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.pv.publisher"/> - <g:message code="app.label.aggregateReport"/></title>
</head>

<body>
<div class="content">
    <div class="container">
        <div class="row">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfiguration}" var="theInstance"/>
<rx:container title="${message(code: "app.periodicReports.generated.tittle")}" options="true" filterButton="true">
    <div class="topControls" style="float: right;text-align: right;display: none">
        <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: ExecutedPeriodicReportConfiguration.name]"/>

        <g:select placeholder="Document Type" name="typeFilter" noSelection="['': 'Document Type']" from="${com.rxlogix.enums.PeriodicReportTypeEnum.asList}" optionKey="key"
                  value="" class="form-control" style="float: right; margin-right: 5px !important;" />
        <g:select name="submissionFilter" from="${ViewHelper.getReportSubmissionStatusEnumI18n()}"
                  optionKey="name" optionValue="display" value="" noSelection="['': 'Submission State']"
                  class="form-control" style="text-align: center; width: 150px;float: right; margin-right: 5px !important; margin-left: 5px  !important"/>
        <div class="checkbox checkbox-primary" style="padding-top: 3px; text-align: center;float: right">
            <g:checkBox id="allReportsFilter" name="allReportsFilter"/>
            <label for="allReportsFilter" style="font-weight: bold"><g:message code="app.label.allReportsFilter"/></label>
        </div>
        <g:render template="/includes/widgets/archiveFilter"/>
    </div>
            <div class="pv-caselist basicDataTable">
                <table id="periodicReportList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%;">
                    <thead>
                    <tr>
                        <th></th>
                        <th><g:message code="app.periodicReport.executed.reportType.label"/></th>
                        <th style="font-size: 16px"><span class="glyphicon glyphicon-star"></span></th>
                        <th><g:message code="app.periodicReport.executed.reportName.label"/></th>
                        <th><g:message code="app.label.versionName"/></th>
                        <th><g:message default="License" code="app.label.reportSubmission.license"/></th>
                        <th><g:message code="app.periodicReport.executed.version.label"/></th>
                        <th><g:message code="app.periodicReport.executed.productName.label"/></th>
                        <th><g:message code="app.periodicReport.executed.reportPeriod.label"/></th>
                        <th><g:message code="app.label.reportingDestinations"/></th>
                        <th><g:message code="app.periodicReport.executed.daysLeft.label"/></th>
                        <th><g:message code="app.periodicReport.executed.dateModified.label"/></th>
                        <th><g:message code="app.periodicReport.executed.reportOwner.label"/></th>
                        <th><g:message code="app.label.tag" /></th>
                        <th><g:message code="app.publisher.publisherContributor"/></th>
                        <th><g:message code="app.label.PublisherTemplate.publisher"/></th>
                        <th><g:message code="app.periodicReport.executed.workflowState.label"/></th>
                        <th></th>
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
        <g:render template="/report/includes/pvpEmailToModal"/>
        <g:render template="/report/includes/sendToDmsModal"/>

    </g:form>
    <g:render template="/query/workflowStatusJustification"
              model="[tableId: 'periodicReportList', isPeriodicReport: true]"/>
</rx:container>
        </div>
    </div>
</div>
<g:render template="/email/includes/copyPasteEmailModal"/>
<g:render template="/includes/widgets/confirmation"/>
<g:render template="/includes/widgets/reportSubmission"/>
<g:render template="/actionItem/includes/actionItemModal" model="[]" />

<g:render template="/oneDrive/downloadModal"/>
<g:render template="/periodicReport/includes/submissionCapaModal"/>
</body>
</html>