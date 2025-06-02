<%@ page import="com.rxlogix.Constants; groovy.json.JsonOutput; com.rxlogix.enums.IcsrCaseStateEnum; com.rxlogix.util.FilterUtil; com.rxlogix.config.IcsrProfileConfiguration; grails.plugin.springsecurity.SpringSecurityUtils" %>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'iscr.case.tracking.label')}"/>
    <title><g:message code="app.icsr.case.tracking.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:stylesheet src="executionStatus.css"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:javascript src="datatables/dataTables.info.js"/>
    <asset:javascript src="app/standardJustification.js"/>

    <asset:javascript src="datatables/extendedDataTable.js"/>
    <asset:stylesheet src="datatables/extendedDataTable.css"/>

    <asset:javascript src="datatables/dataTables.columnResize.js"/>
    <asset:stylesheet src="datatables/dataTables.columnResize.css"/>

    <asset:javascript src="datatables/dataTables.fixedHeader.js"/>
    <asset:stylesheet src="datatables/dataTables.fixedHeader.css"/>

    <g:javascript>
        var icsrCaseTrackingListUrl = "${createLink(controller: 'icsrCaseTrackingRest', action: 'index', params: [caseNumber: caseNumber, versionNumber: versionNumber, exIcsrProfileId: exIcsrProfileId, exIcsrTemplateQueryId: exIcsrTemplateQueryId])}";
        var generatePDF= "${createLink(controller: "report", action: "downloadPdf")}";
        var generateCioms = "${createLink(controller: "report", action: "drillDown")}";
        var generateBatchXML = "${createLink(controller: 'icsrCaseTrackingRest', action: 'downloadBatchXML')}";
        var generateBulkXML = "${createLink(controller: 'icsrCaseTrackingRest', action: 'downloadBulkXML')}";
        var APP_ASSETS_PATH='${request.contextPath}/assets/';
        var markAsSubmittedUrl= "${createLink(controller: "icsrProfileConfiguration", action: "loadIcsrReportSubmissionForm")}";
        var markAsSubmittedUrlNew= "${createLink(controller: "icsrProfileConfiguration", action: "loadIcsrReportSubmission")}";
        var bulkMarkAsSubmittedURL= "${createLink(controller: "icsrProfileConfiguration", action: "loadBulkIcsrReportSubmissionForm")}";
        var caseSubmitUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "submitIscrCase")}";
        var statusSubmitUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "updateIcsrCaseStatus")}";
        var icsrProfileViewUrl = "${createLink(controller: "executedIcsrProfile", action: "view")}";
        var submissionHistoryCase= "${createLink(controller: 'icsrCaseTrackingRest', action: 'caseHistory')}";
        var transmitCase = "${createLink(controller: 'icsrCaseTrackingRest', action: 'transmitCase')}";
        var checkPreviousVersionUrl = "${createLink(controller: 'icsrCaseTrackingRest', action: 'checkPreviousVersionIsTransmitted')}";
        var checkPreviousVersionForAllCasesUrl = "${createLink(controller: 'icsrCaseTrackingRest', action: 'checkPreviousVersionIsTransmittedForAll')}";
        var showReportUrl = "${createLink(controller: 'icsr', action: 'showReport')}";
        var caseErrorDetails= "${createLink(controller: 'icsrCaseTrackingRest', action: 'getErrorDetails')}";
        var caseHistoryData= "${createLink(controller: 'icsrCaseTrackingRest', action: 'caseAllReceipentHistory')}";
        var viewDownloadPage = "${createLink(controller: "report", action: "showFirstSection")}";
        var listE2BStatuses = "${createLink(controller: "icsrCaseTrackingRest", action: "listE2BStatuses")}";
        var listProfiles = "${createLink(controller: "icsrProfileConfiguration", action: "listProfiles")}";
        var bulkTransmissionUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "bulkTransmitCases")}";
        var bulkCaseSubmitUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "bulkSubmitIscrCase")}";
        var generateCaseData = "${createLink(controller: "icsr", action: "generateCaseData")}";
        var generatedCaseDataScheduled = "${createLink(controller: "icsr", action: "generatedCaseDataScheduled")}";
        var deleteCaseUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "deleteCase")}";
        var saveLocalCp = "${createLink(controller: "icsrCaseTrackingRest", action: "saveLocalCp")}";
        var checkFileExistUrl = "${createLink(controller: "icsrCaseTrackingRest", action:'checkFileExist')}";
        var downloadDocFileUrl = "${createLink(controller: "icsrCaseTrackingRest", action:'downloadDocFile')}";
        var downloadAckFileUrl = "${createLink(controller: "icsrCaseTrackingRest", action:'downloadAckFile')}";
        var markAsNullifiedUrl = "${createLink(controller: "icsrCaseTrackingRest", action: 'nullifyReport')}";
        var deviceListUrl = "${createLink(controller: "icsrCaseTrackingRest", action: 'listDevices')}";
        var authorizationListUrl = "${createLink(controller: "icsrCaseTrackingRest", action: 'listAuthorizationType')}";
        var listAuthorizationType = "${createLink(controller: "icsrCaseTrackingRest", action: 'listAuthorizationTypeForFilter')}";
        var approvalListUrl = "${createLink(controller: "icsrCaseTrackingRest", action: 'listApprovalNumber')}";
        var downloadMergedPdfUrl = "${createLink(controller: "icsrCaseTrackingRest", action:'downloadMergedPdf')}";
        var bulkDownloadReportsUrl = "${createLink(controller: "icsrCaseTrackingRest", action: 'prepareBulkDownloadIcsrReports')}";
        var downloadReportUrl = "${createLink(controller:"icsr", action:"downloadReport")}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var regenerateCaseUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "regenerateCase")}";
        var bulkRegenerateCaseUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "bulkRegenerateCase")}";
        var standardJustificationsUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "listStandardJustification")}";
        var isICSRDistAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ICSR_DISTRIBUTION_ADMIN")};
        var isICSRDist = ${SpringSecurityUtils.ifAnyGranted("ROLE_ICSR_DISTRIBUTION")};
        var actionDropdownMap = ${raw(JsonOutput.toJson(Constants.icsrActionDropdownMap))};
    </g:javascript>
    <asset:javascript src="app/icsrCaseTracking/icsr-case-tracking.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <style>
    #rxTableIcsrCaseTracking_wrapper > .dt-layout-row:first-child {
        margin-top: 0px;
    }

    #rxTableIcsrCaseTracking_wrapper > .dt-layout-row:first-child .dt-layout-cell.dt-end {
        margin-top: -37px;
        margin-right: 80px;
    }

    #rxTableIcsrCaseTracking_wrapper .col-date {
        max-width: 107px !important;
        min-width: 107px !important;
    }

    #rxTableIcsrCaseTracking_wrapper .col-case-num {
        max-width: 104px !important;
        min-width: 104px !important;
    }

    #icsrCaseStateDropDown .select2.select2-container {
        width: 60% !important;
    }

    .dt-layout-table {
        overflow-x: auto;
        height: calc(100vh - 188px) !important;
    }

    .dt-layout-cell.dt-start {
        height: 46px !important;
    }

    tr td .ico-dots {
        background: white;
    }
    tr:hover td .ico-dots {
        background: rgb(240, 249, 253) !important;
    }
    tr:nth-of-type(odd) td .ico-dots {
        background: #f4f5f7;
    }
    .datatable-scroll-box {
        overflow-x: auto;
    }
    .dt-action-btn {
        background-color: #3579ba !important;
        border-color: #3579ba !important;
    }
    .dt-action-btn:hover {
        background-color: #3579ba !important;
        border-color: #3579ba !important;
    }
</style>
</head>

<body>
<div class="col-md-12">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${icsrCaseTrackingInstanceList}" var="theInstance"/>
    <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="WarningDiv" style="display: none">
        <button type="button" class="close WarningDivclose">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <p></p>
    </div>
    <rx:container title="${message(code: "app.label.icsr.case.tracking")}" options="true" filterButton="true" customButtons="${g.render(template: "includes/customHeaderButtons")}">
    <div class="pv-caselist">
        %{--<button type="button" class="btn btn-primary bulkTransmitButton" data-toggle="modal" data-target="#bulkTransmitJustification">${message(code: 'bulk.transmit.button.label')}</button>
        <button type="button" class="btn btn-primary bulkSubmitButton" data-toggle="modal" data-target="#bulkReportSubmissionModal">${message(code: 'bulk.submit.button.label')}</button>--}%
    <table id="rxTableIcsrCaseTracking" class="table table-striped pv-list-table dataTable no-footer" width="100%" style="    min-height: 60px;">
        <thead>
        <tr>
            <th><input id="icsrProfileSelectAll" type="checkbox"  style="cursor: pointer"/></th>
            <th class="col-min-100" data-id="caseNumber" data-type="text"><g:message code="icsr.case.tracking.case.number.label"/></th>
            <th class="col-min-50" style="text-align: center" data-id="versionNumber" data-type="number"><g:message code="icsr.case.tracking.version"/></th>
            <th class="col-min-70" data-id="recipient" data-type="text"><g:message code="icsr.case.tracking.recipient"/></th>
            <th class="col-min-80" data-id="followupInfo" data-type="text"><g:message code="app.label.followUpInfo"/></th>
            <th class="col-min-60" data-id="followupNumber" data-type="text"><g:message code="app.label.followUpType"/></th>
            <th class="col-min-120" data-id="caseReceiptDate" data-type="date"><g:message code="icsr.case.tracking.caseReceiptDate"/></th>
            <th class="col-min-130 text-center" data-id="safetyReceiptDate" data-type="date"><g:message code="icsr.case.tracking.safetyReceiptDate"/></th>

            <th class="col-min-70" data-id="productName" data-type="text"><g:message code="icsr.case.tracking.productName"/></th>
            <th class="col-min-70" data-id="profileName" data-type="text"><g:message code="icsr.case.tracking.profile"/></th>
            <th class="col-min-70" data-id="reportForm" data-type="disabled"><g:message code="icsr.case.tracking.reportForm"/></th>
            <th class="col-min-100" data-id="awareDate" data-type="date"><g:message code="icsr.case.tracking.awareDate"/></th>
            <th class="col-min-70" data-id="authorizationType" data-type="text"><g:message code="icsr.case.tracking.authorization.type"/></th>
            <th class="col-min-70" data-id="approvalNumber" data-type="text"><g:message code="icsr.profile.manual.approval.number"/></th>
            <th class="col-min-70" data-id="dueInDays" data-type="number"><g:message code="icsr.case.tracking.dueInDays"/></th>
            <th class="col-min-110 text-center" data-id="scheduledDate" data-type="date"><g:message code="icsr.case.tracking.scheduledDate"/></th>
            <th class="col-min-120 text-center" data-id="dueDate" data-type="date"><g:message code="icsr.case.tracking.dueDate"/></th>
            <th class="col-min-110 text-center" data-id="generationDate" data-type="date"><g:message code="icsr.case.tracking.generationDate"/></th>
            <th class="col-min-130 text-center" data-id="transmissionDate" data-type="date"><g:message code="icsr.case.tracking.transmissionDate"/></th>
            <th class="col-min-110 text-center" data-id="submissionDate" data-type="date"><g:message code="icsr.case.tracking.submissionDate"/></th>
            <th class="col-min-110 text-center" data-id="submissionDatePreferredTime" data-type="date"><g:message code="icsr.case.tracking.submissionDatePreferredTime"/></th>
            <th class="col-min-110 text-center" data-id="modifiedDate" data-type="date"><g:message code="icsr.case.tracking.modifiedDate"/></th>
            <th class="col-min-60" style="text-align: center" data-id="e2BStatus" data-type="text"><g:message code="icsr.case.tracking.state"/></th>
            <th class="col-min-100" style="text-align: center"><g:message code="app.label.action"/></th>
        </tr>
        </thead>
    </table>
    </div>
    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>
</rx:container>
<g:render template="/includes/widgets/reportSubmission"/>
<g:render template="/includes/widgets/reportSubmissionHistory"/>
<g:render template="/includes/widgets/bulkReportSubmission"/>
<g:render template="/includes/widgets/infoTemplate" model="[messageBody: message(code: 'app.icsrTracking.bulkUpdateMaxRowsWarning'), warningModalId:'bulkUpdateMaxRowsWarning', title: message(code: 'app.label.warning')]"/>
<g:form controller="icsr" name="emailForm">
    <g:hiddenField name="exIcsrTemplateQueryId"/>
    <g:hiddenField name="caseNumber"/>
    <g:hiddenField name="versionNumber"/>
    <g:render template="/report/includes/emailToModal"
              model="['isIcsrViewTracking': isIcsrViewTracking, forClass: IcsrProfileConfiguration]"/>
</g:form>
<g:render template="submissionHistoryCase"/>
<g:render template="errorDetails"/>
<g:render template="caseHistoryDetails"/>
<g:render template="transmitJustification"/>
<g:render template="regenerateConfirmationModal"/>
<g:render template="bulkTransmitJustification"/>
<g:render template="bulkRegenerateModal"/>
<g:render template="nullificationJustification"/>
<g:render template="/email/includes/copyPasteEmailModal"/>
<g:render template="/icsrProfileConfiguration/icsrCaseStateDropDown" model="[status: status]" />
<g:render template="/includes/widgets/deleteCaseConfirmationModal" />
<g:render template="/includes/widgets/confirmation"/>
<sec:ifAnyGranted roles="ROLE_ICSR_DISTRIBUTION">
    <g:render template="includes/manualAddCase"/>
    <asset:javascript src="app/icsrCaseTracking/manual-schedule-case.js"/>
</sec:ifAnyGranted>
</div>
</body>
</html>
