<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.PvqTypeEnum; groovy.json.JsonOutput; grails.plugin.springsecurity.SpringSecurityUtils" %>
<html>
<head>
    <title><g:message code="app.quality.Quality.title"/></title>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highstock.js"/>
    <asset:stylesheet src="quality.css"/>

    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/actionItem/actionItemList.js"/>
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

    table th{
        text-align: center !important;
        padding: 2px 4px !important;
    }
    table td {
        font-size: 13px !important;
        vertical-align: middle !important;
        padding: 2px 4px !important;
        line-height: 1.4 !important;
    }
    .dt-layout-row:first-child {
        padding-right: initial !important;
    }
    #rxTableQualityReports_wrapper {
        overflow-x: hidden !important;
    }
    #rxTableQualityReports_wrapper .dt-layout-table {
        overflow-x: auto !important;
        height: calc(100vh - 188px) !important;
    }
    </style>
</head>

<body>
<div class="content ">
    <div class="container ">
<g:if test="${!moduleColumnList}">
    <g:message code="qualityModule.noDataFound.page"/>
</g:if>
<g:else>
    <rx:container title="${message(code: message(code: "app.label.case.data.quality.chart"))}" options="${false}">
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

        <g:render template="includes/qualityChart" />

    </rx:container>

    <rx:container id="qualityTableContainer" title="${message(code: message(code: "app.label.case.data.quality"))}"  options="${true}" filterButton="${true}"  customButtons="${g.render(template: "includes/shareTemplate", model:[ dataType: PvqTypeEnum.CASE_QUALITY.name()])}">
        <div>
            <g:render template="includes/qualityTableTemplate" model="[act: 'caseDataQuality']"/>
        </div>
    </rx:container>
    <input type="hidden" name="dataType" id="dataType"  value="${PvqTypeEnum.CASE_QUALITY.name()}">
    <input type="hidden" name="commentDataType" id="commentDataType"  value="${PvqTypeEnum.CASE_QUALITY.name()}">
    <input type="hidden" name="qualityScreen" id="qualityScreen" value="true">
    <g:render template="/includes/widgets/errorTemplate"/>
    <g:render template="includes/adHocAlert"/>
    <g:render template="includes/sourceDocuments"/>
    <g:render template="includes/errorExport"/>
    <g:render template="/actionItem/includes/actionItemModal"/>
    <g:render template="/query/copyPasteModal" model="[isPVQ: true]"/>
    <g:render template="/caseList/includes/addQualityComment" model="[caseSeriesId: 0]"/>
    <g:javascript type="text/javascript">
    var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest',action: 'workFlowForQuality')}";
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
    var caserecordajaxurl = "${createLink(controller: 'quality', action: 'qualityDataAjax')}";
    var displayData = ${(queryNameList && queryTotalCountList)};
    //var queryNameList = ${raw(queryNameList.toString())};
    var commaSeparatedQueryTotalCountList = "${queryTotalCountList?.join(',')}";
    var chartLabelYAxis = "${message(code: "chart.label.yAxis.caseCount")}";
    var checkCaseNumUrl = "${createLink(controller: 'quality', action: 'checkCaseNum')}";
    var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
    var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
    var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
    var singleCIOMSLink = '${createLink(controller: 'report', action: 'exportSingleCIOMS')}';
    var updatePriorityUrl = '${createLink(controller: 'quality', action: 'updatePriority')}';
    var qualityPriorityTagsUrl = '${createLink(controller: 'quality', action: 'getQualityPriorityList')}';
    var caseDataLinkUrl = '${createLink(controller: 'quality', action: 'caseForm')}?type=${com.rxlogix.enums.PvqTypeEnum.CASE_QUALITY}';
    var reportshowfirstsectionurl = '${createLink(controller: 'report', action: 'showFirstSection')}';
    var updateAssignedOwnerUrl = '${createLink(controller: 'quality', action: 'updateAssignedOwner')}';
    var fetchUsersUrl = '${createLink(controller: 'quality', action: 'fetchUsers')}';
    var fetchUsersAndGroupsUrl = '${createLink(controller: 'quality', action: 'fetchUsersAndGroups')}';
    var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'indexPvq')}";
    var fetchErrorTypesUrl = "${createLink(controller: 'quality', action: 'fetchQualityModuleErrorsList')}";
    var updateErrorTypeUrl = "${createLink(controller: 'quality', action: 'updateErrorType')}";
    var editRole=false;
    var manualEntryType = "${com.rxlogix.enums.QualityEntryTypeEnum.MANUAL.getValue()}";
    var fetchCriteriaForManualErrorUrl = "${createLink(controller: 'quality', action: 'fetchCriteriaForManualError')}";
    var fetchCommentsUrl = "${createLink(controller: 'commentRest', action: 'loadComments')}";
    var deleteCommentsUrl = "${createLink(controller: 'commentRest', action: 'delete')}";
    var saveCommentsUrl = "${createLink(controller: 'commentRest', action: 'save')}";
    var downloadSourceDocumentsUrl = '${createLink(controller: 'quality', action: 'downloadSourceDocuments')}';
    var fetchSourceDocumentsUrl = '${createLink(controller: 'quality', action: 'fetchSourceDocuments')}';
    var viewSourceDocumentsEnabled = ${grailsApplication.config.dataSources.safetySource ? "true" : "false"};
    var fetchQualityIssueTypeUrl = '${createLink(controller: 'quality', action: 'fetchQualityIssueType')}';
    var updateQualityIssueTypeUrl = '${createLink(controller: 'quality', action: 'updateQualityIssueType')}';
    var updateRootCauseUrl = '${createLink(controller: 'quality', action: 'updateRootCause')}';
    var updateResponsiblePartyUrl = '${createLink(controller: 'quality', action: 'updateResponsibleParty')}';
    var workflowStatesUrl = "${createLink(controller: 'workflowState',action: 'list')}";
    var issuesList = ${raw(JsonOutput.toJson(qualityIssues))};
    var rootCauseList = ${raw(JsonOutput.toJson(rootCauses))};
    var responsiblePartyList = ${raw(JsonOutput.toJson(responsibleParties))};
    var correctiveActionList = ${raw(JsonOutput.toJson(correctiveActions))};
    var preventativeActionList = ${raw(JsonOutput.toJson(preventativeActions))};
    var viewType = '${viewType}';
    var fetchFilterPanelDataUrl = '${createLink(controller: 'quality', action: 'getFilterData')}';
    <sec:ifAnyGranted roles="ROLE_PVQ_EDIT">
        editRole=true;
    </sec:ifAnyGranted>
    var getIssueNumberUrl = "${createLink(controller: 'issue', action: 'getIssueNumber' )}";
    var createCapaUrl = '${createLink(controller: 'issue', action: 'createCapaForQuality')}';
    var viewCriteriaUrl = '${createLink(controller: 'configuration', action: 'view')}';
    var deleteCasesUrl = '${createLink(controller: 'quality', action: 'deleteCases')}?type=${com.rxlogix.enums.PvqTypeEnum.CASE_QUALITY}';
    var deleteFieldCaseMsgUrl = '${createLink(controller: 'quality', action: 'displayFieldLevelMsg')}?type=${com.rxlogix.enums.PvqTypeEnum.CASE_QUALITY}';
    var showCriteriaForManualError=true;
    var isAppliedAdvancedFilter = false;
    var lateList =  issuesList;
    var saveAllRcaForQualityCaseUrl = "${createLink(controller: 'quality', action: 'saveAllRcasForCase')}";
    var fetchIssueNumberCaseUrl = "${createLink(controller: 'issue', action: 'fetchIssueNumberCase' )}";
    var fetchDataIssueUrl = "${createLink(controller: 'issue', action: 'fetchDataIssue' )}";
    var updateQualityCapaUrl = "${createLink(controller: "issue", action: "updateCapaForQuality")}";
    var createCapaAttachmentUrl="${createLink(controller: 'issueRest', action: 'createCapaAttachment')}";
    var updateCapaAttachmentUrl="${createLink(controller: 'issueRest', action: 'updateCapaAttachment')}";
    var getAllRcasForQualityCaseUrl = "${createLink(controller: 'quality', action: 'getAllRcasForCase')}";
    var EMPTY_LABEL="${ViewHelper.getEmptyLabel()}";
    var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
    var importExcel="${createLink(controller: 'quality', action: 'importExcel')}";
    var validateValue="${createLink(controller: 'query', action: 'validateValue')}";
    var downloadAllAttachmentUrl="${createLink(controller: 'issue', action: 'downloadAllAttachment')}";
    var downloadAttachmentUrl = "${createLink(controller: 'issue', action: 'downloadAttachment')}";
    var removeIssueAttachmentsUrl = "${createLink(controller: 'issueRest', action: 'removeAttachments')}";
    var possibleValuesUrl = "${createLink(controller: 'quality', action: 'fieldPossibleValues')}";
    var getSelectAllUrl = "${createLink(controller: 'quality', action: 'getSelectAll')}";
    var AttachmentSizeLimit = ${grailsApplication.config.grails.controllers.attachment.maxFilSize};
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
        <g:render template="includes/ignoreConfirmationModal" />
        <g:render template="/query/workflowStatusJustification" model="[tableId:'rxTableQualityReports']"/>
        <g:render template="includes/viewQualityDelayReasonModal" />
        <g:render template="/includes/widgets/deleteRecord"/>
        <g:render template="/includes/widgets/infoTemplate"/>
        <g:render template="includes/assignedToPVQFilter"/>
<form style="display: none" id="excelExport" method="post" action="${createLink(controller: 'quality', action: 'exportToExcelCaseDatQuality')}">
    <input name="data" id="excelData">
</form>
<form style="display: none" id="changeView" method="post" action="${createLink(controller: 'quality', action: 'caseDataQuality')}">
    <input type="hidden" name="viewOption" id="viewOption" value="ICV">
</form>
        </div>
</div>
<g:form name="attachForm" method="post">
        <input type="hidden" name="selectAll" id="selectAll1">
        <input type="hidden" name="selectedIds" id="selectedIds">
        <input type="hidden" name="capaInstanceId" id="capaInstanceId">
</g:form>
`<g:render template="/includes/widgets/warningTemplate" model="[messageBody: message(code: 'app.pvc.export.warn'), warningModalId:'exportWarning', warningButtonId:'exportWarningOkButton', queryType: '']"/>
`<g:render template="/includes/widgets/infoTemplate" model="[messageBody: message(code: 'app.reasonOfDelay.bulkUpdateMaxRowsWarning'), warningModalId:'bulkUpdateMaxRowsWarning', title: message(code: 'app.label.warning')]"/>
</body>
</html>


