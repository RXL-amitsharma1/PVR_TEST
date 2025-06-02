<%@ page contentType="text/html;charset=UTF-8" %>


<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.PvqTypeEnum; com.rxlogix.enums.StatusEnum; groovy.json.JsonOutput; grails.plugin.springsecurity.SpringSecurityUtils" %>
<html>
<head>
    <title><g:message code="app.quality.Submission.title"/></title>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highstock.js"/>
    <asset:stylesheet src="quality.css"/>

    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/quality/copyPasteValues.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:javascript src="datatables/dataTables.info.js"/>

    <asset:javascript src="datatables/extendedDataTable.js"/>
    <asset:stylesheet src="datatables/extendedDataTable.css"/>

    <asset:javascript src="datatables/dataTables.columnResize.js"/>
    <asset:stylesheet src="datatables/dataTables.columnResize.css"/>

    <asset:javascript src="datatables/dataTables.fixedHeader.js"/>
    <asset:stylesheet src="datatables/dataTables.fixedHeader.css"/>

    <meta name="layout" content="main"/>
    <style>
    .glyphicon-filter {padding: 3px 9px;}
    .md-list{padding: 0px 7px; font-size: 1.4em;}
    .glyphicon-filter:hover {background: rgba(255, 255, 255, 0.3);}
    .md-list:hover {background: rgba(255, 255, 255, 0.3);}
    .rxmain-dropdown-settings {line-height: 0.9em;}
    .dt-container {
        overflow-x: auto;
    }
    #rxTableQualityReports_wrapper > .dt-layout-row:first-child {
        margin-top : 0px;
    }
    .filter-panel .caseNum-filter {
        display: inline-block;
    }
    .filter-panel .caseNum-filter + .copy-n-paste {
        position: absolute !important;
        top: 5px;
        right: 9px;
    }
    .dt-layout-row:first-child {
        padding-right: initial !important;
    }
    #rxTableQualityReports_wrapper {
        overflow-x: initial !important;
    }
    #rxTableQualityReports_wrapper .dt-layout-table {
        overflow-x: auto !important;
        height: calc(100vh - 188px) !important;
    }
    </style>
</head>

<body>
<g:if test="${!moduleColumnList}">
    <g:message code="qualityModule.noDataFound.page"/>
</g:if>
<g:else>
    <rx:container title="${message(code: message(code: "app.label.submission.quality.chart"))}" options="${false}">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigurationInstance}"
                  var="theInstance"/>
        <g:render template="/includes/layout/inlineAlerts"/>
        <div class="alert alert-danger hide">
            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
            <strong><g:message code="default.system.error.message"/></strong> <span id="errorNotification"></span>
        </div>
        <div class="alert alert-danger hide">
            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
            <span id="pageErrorMessage"></span>
        </div>

        <div class="alert alert-success" style="display: none">
            <a href="#" class="close" data-evt-clk='{"method" : "addClassHide", "params" : [".alert-success"]}' aria-label="close">&times;</a>
            <span id="successNotification"></span>
        </div>
    %{--commented as currently this feature does not provide an ability to add issue at submission level--}%
%{--        <g:render template="includes/qualityChart" />--}%
        <div class="contentBox contentBoxContainer row">
            <div id="qualityChart"></div>
        </div>
    </rx:container>

    <rx:container title="${message(code: message(code: "app.label.submission.quality"))}" options="${true}" filterButton="${true}" customButtons="${g.render(template: "includes/shareTemplate", model:[ dataType: PvqTypeEnum.SUBMISSION_QUALITY.name()])}">
        <div>
            <g:render template="includes/qualityTableTemplate" model="[dataType: PvqTypeEnum.SUBMISSION_QUALITY.name()]"/>
        </div>
    </rx:container>
    <input type="hidden" name="commentDataType" id="commentDataType"  value="${PvqTypeEnum.SUBMISSION_QUALITY.name()}">
    <input type="hidden" name="dataType" id="dataType" value="${com.rxlogix.enums.PvqTypeEnum.SUBMISSION_QUALITY}">
    <input type="hidden" name="qualityScreen" id="qualityScreen" value="true">
    <g:render template="includes/errorExport"/>
    <g:render template="includes/adHocAlert"/>
    <g:render template="/actionItem/includes/actionItemModal"/>
    <g:render template="/caseList/includes/addQualityComment" model="[caseSeriesId: 0]"/>
    <g:render template="includes/sourceDocuments"/>
    <g:render template="/query/copyPasteModal" model="[isPVQ: true]"/>
    <g:javascript type="text/javascript">
    var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest',action: 'workFlowForQuality')}"
    var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest',action: 'saveQualityWorkFlow')}";
    var columnNameList = ${raw(JsonOutput.toJson(moduleColumnList.fieldName))};
    var columnUiStackMapping = ${raw(JsonOutput.toJson(columnUiStackMapping))};
    var columnTypeList = ${raw(JsonOutput.toJson(moduleColumnList.fieldType))};
    var columnLabelList = ${raw(JsonOutput.toJson(moduleColumnList.fieldLabel))};
    var reportOtherFieldName = ${raw(JsonOutput.toJson(reportOtherColumnList.fieldName))};
    var reportOtherFieldType = ${raw(JsonOutput.toJson(reportOtherColumnList.fieldType))};
    var reportOtherFieldLabel = ${raw(JsonOutput.toJson(reportOtherColumnList.fieldLabel))};
    var reportSelectableFields = ${raw(JsonOutput.toJson(reportOtherColumnList.selectable))};
    var standardReportSelectableFields = ${raw(JsonOutput.toJson(moduleColumnList.selectable))};
    var reportNonCacheFields = "${reportOtherColumnList.nonCache}";
    var caserecordajaxurl = "${createLink(controller: 'quality', action: 'qualitySubmissionAjax')}";
    var displayData = ${(queryNameList && queryTotalCountList)};
    var queryNameList = ${raw(queryNameList.toString())};
    var commaSeparatedQueryTotalCountList = "${queryTotalCountList?.join(',')}";
    var chartLabelYAxis = "${message(code: "chart.label.yAxis.caseCount")}";
    var checkCaseNumUrl = "${createLink(controller: 'quality', action: 'checkCaseNum')}";
    var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
    var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
    var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'indexPvq')}";
    var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
    var singleCIOMSLink = '${createLink(controller: 'report', action: 'exportSingleCIOMS')}';
    var fetchErrorTypesUrl = "${createLink(controller: 'quality', action: 'fetchQualityModuleErrorsList')}";
    var updatePriorityUrl = '${createLink(controller: 'quality', action: 'updatePriority')}';
    var caseDataLinkUrl = '${createLink(controller: 'quality', action: 'caseForm')}?type=${com.rxlogix.enums.PvqTypeEnum.SUBMISSION_QUALITY}';
    var fetchUsersUrl = '${createLink(controller: 'quality', action: 'fetchUsers')}';
    var fetchUsersAndGroupsUrl = '${createLink(controller: 'quality', action: 'fetchUsersAndGroups')}';
    var qualityPriorityTagsUrl = '${createLink(controller: 'quality', action: 'getQualityPriorityList')}';
    var fetchIssueNumberCaseUrl = "${createLink(controller: 'issue', action: 'fetchIssueNumberCase' )}";
    var fetchDataIssueUrl = "${createLink(controller: 'issue', action: 'fetchDataIssue' )}";
    var updateQualityCapaUrl = "${createLink(controller: "issue", action: "updateCapaForQuality")}";
    var createCapaAttachmentUrl="${createLink(controller: 'issueRest', action: 'createCapaAttachment')}";
    var updateCapaAttachmentUrl="${createLink(controller: 'issueRest', action: 'updateCapaAttachment')}";
    var updateErrorTypeUrl = "${createLink(controller: 'quality', action: 'updateErrorType')}";
    var editRole=false;
    var manualEntryType = "${com.rxlogix.enums.QualityEntryTypeEnum.MANUAL.getValue()}";
    var fetchCriteriaForManualErrorUrl = "${createLink(controller: 'quality', action: 'fetchCriteriaForManualError')}";
    var fetchCommentsUrl = "${createLink(controller: 'commentRest', action: 'loadComments')}";
    var deleteCommentsUrl = "${createLink(controller: 'commentRest', action: 'delete')}";
    var saveCommentsUrl = "${createLink(controller: 'commentRest', action: 'save')}";
    var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'indexPvq')}";
    var deleteCasesUrl = '${createLink(controller: 'quality', action: 'deleteCases')}?type=${com.rxlogix.enums.PvqTypeEnum.SUBMISSION_QUALITY}';
    var deleteFieldCaseMsgUrl = '${createLink(controller: 'quality', action: 'displayFieldLevelMsg')}?type=${com.rxlogix.enums.PvqTypeEnum.SUBMISSION_QUALITY}';
    var fetchFilterPanelDataUrl = '${createLink(controller: 'quality', action: 'getFilterData')}';
    var filterPanelData = '${filterPanelData}';
    var importExcel="${createLink(controller: 'quality', action: 'importExcel')}";
    var validateValue="${createLink(controller: 'query', action: 'validateValue')}";
    <sec:ifAnyGranted roles="ROLE_PVQ_EDIT">
        editRole=true;
    </sec:ifAnyGranted>
    var qualityPriorityTagsUrl = '${createLink(controller: 'quality', action: 'getQualityPriorityList')}';
    var updateAssignedOwnerUrl = '${createLink(controller: 'quality', action: 'updateAssignedOwner')}';
    var fetchUsersUrl = '${createLink(controller: 'quality', action: 'fetchUsers')}';
    var getIssueNumberUrl = "${createLink(controller: 'issue', action: 'getIssueNumber' )}";
    var createCapaUrl = '${createLink(controller: 'issue', action: 'createCapaForQuality')}';
    var viewCriteriaUrl = '${createLink(controller: 'configuration', action: 'view')}';
    var updateQualityIssueTypeUrl = '${createLink(controller: 'quality', action: 'updateQualityIssueType')}';
    var updateRootCauseUrl = '${createLink(controller: 'quality', action: 'updateRootCause')}';
    var downloadSourceDocumentsUrl = '${createLink(controller: 'quality', action: 'downloadSourceDocuments')}';
    var fetchSourceDocumentsUrl = '${createLink(controller: 'quality', action: 'fetchSourceDocuments')}';
    var workflowStatesUrl = "${createLink(controller: 'workflowState',action: 'list')}";
    var viewSourceDocumentsEnabled = ${grailsApplication.config.dataSources.safetySource ? "true" : "false"};
    var showCriteriaForManualError=true;
    var updateResponsiblePartyUrl = '${createLink(controller: 'quality', action: 'updateResponsibleParty')}';
    var issuesList = ${raw(JsonOutput.toJson(qualityIssues))};
    var rootCauseList = ${raw(JsonOutput.toJson(rootCauses))};
    var responsiblePartyList = ${raw(JsonOutput.toJson(responsibleParties))};
    var viewType = '${viewType}';
    var isAppliedAdvancedFilter = false;
    var correctiveActionList = ${raw(JsonOutput.toJson(correctiveActions))};
    var preventativeActionList = ${raw(JsonOutput.toJson(preventativeActions))};
    var lateList =  issuesList;
    var saveAllRcaForQualityCaseUrl = "${createLink(controller: 'quality', action: 'saveAllRcasForCase')}";
    var getAllRcasForQualityCaseUrl = "${createLink(controller: 'quality', action: 'getAllRcasForCase')}";
    var EMPTY_LABEL="${ViewHelper.getEmptyLabel()}";
    var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
    var downloadAllAttachmentUrl="${createLink(controller: 'issue', action: 'downloadAllAttachment')}";
    var downloadAttachmentUrl = "${createLink(controller: 'issue', action: 'downloadAttachment')}";
    var removeIssueAttachmentsUrl = "${createLink(controller: 'issueRest', action: 'removeAttachments')}";
    var possibleValuesUrl = "${createLink(controller: 'quality', action: 'fieldPossibleValues')}";
    var AttachmentSizeLimit = ${grailsApplication.config.grails.controllers.attachment.maxFilSize}
    var getSelectAllUrl = "${createLink(controller: 'quality', action: 'getSelectAll')}";;
    var getActionItemChangesHistoryUrl = "${createLink(controller: 'actionItem', action: 'getActionItemChangesHistory')}";
    var isPvqRole = ${SpringSecurityUtils.ifAnyGranted("ROLE_USER_GROUP_RCA_PVQ")};
    var MY_GROUPS_VALUE = 'MY_GROUPS';
    var sharedWithUserListUrl = "${createLink(controller: 'userRest', action: 'sharedWithUserListPvq')}";
    var sharedWithGroupListUrl = "${createLink(controller: 'userRest', action: 'sharedWithGroupListPvq')}";
    var sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";

    </g:javascript>
    <asset:javascript src="app/workFlow.js"/>
    <asset:javascript src="app/quality/common.js"/>
    <asset:javascript src="app/quality/qualityLateProcessing.js"/>
    <asset:javascript src="app/quality/quality.js"/>
</g:else>

<g:render template="/includes/widgets/errorTemplate"/>
<g:render template="includes/ignoreConfirmationModal" />
<g:render template="/query/workflowStatusJustification" model="[tableId:'rxTableQualityReports']"/>
<g:render template="includes/viewQualityDelayReasonModal" />
<g:render template="/includes/widgets/deleteRecord"/>
<g:render template="/includes/widgets/infoTemplate"/>
<g:render template="includes/assignedToPVQFilter"/>
<form style="display: none" id="excelExport" method="post" action="${createLink(controller: 'quality', action: 'exportToExcelQualitySubmission')}">
    <input name="data" id="excelData">
</form>
<form style="display: none" id="changeView" method="post" action="${createLink(controller: 'quality', action: 'submissionQuality')}">
    <input type="hidden" name="viewOption" id="viewOption" value="ICV">
</form>
<g:form name="attachForm" method="post">
        <input type="hidden" name="selectAll" id="selectAll1">
        <input type="hidden" name="selectedIds" id="selectedIds">
        <input type="hidden" name="capaInstanceId" id="capaInstanceId">
</g:form>
<g:render template="/includes/widgets/warningTemplate" model="[messageBody: message(code: 'app.pvc.export.warn'), warningModalId:'exportWarning', warningButtonId:'exportWarningOkButton', queryType: '']"/>
<g:render template="/includes/widgets/infoTemplate" model="[messageBody: message(code: 'app.reasonOfDelay.bulkUpdateMaxRowsWarning'), warningModalId:'bulkUpdateMaxRowsWarning', title: message(code: 'app.label.warning')]"/>
</body>
</html>


