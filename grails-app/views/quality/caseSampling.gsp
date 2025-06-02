<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.PvqTypeEnum; groovy.json.JsonOutput;grails.plugin.springsecurity.SpringSecurityUtils" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="qualityService" bean="qualityService"/>
    <title>
        <g:message code="app.label.quality.title"/> ${qualityService.getLabelForType(params.dataType)}
    </title>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
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

    <style>
    .glyphicon-filter {padding: 3px 9px;}
    .md-list{padding: 0px 7px; font-size: 1.4em;}
    .glyphicon-filter:hover {background: rgba(255, 255, 255, 0.3);}
    .md-list:hover {background: rgba(255, 255, 255, 0.3);}
    .rxmain-dropdown-settings {line-height: 0.9em;}
    .dt-container {
        overflow-x: auto;
    }
    tr.highlight-red td {
        background-color: red;
    !important;
    }

    tr.highlight-green td {
        background-color: #419641;
    !important;
    }

    tr.highlight-blue td {
        background-color: #b4e5f5;
    !important;
    }
    #case-sampling-list_wrapper > .dt-layout-row:first-child {
        margin-top : 0px;
    }
    #case-sampling-list_wrapper .dt-layout-table {
        overflow-x: auto;
        width: 100%;
        height: calc(100vh - 202px) !important;
    }
    #case-sampling-list_wrapper .dt-layout-cell.dt-start {
        height: 50px !important;
    }
    .filter-panel .caseNum-filter {
        display: inline-block;
    }
    .filter-panel .caseNum-filter + .copy-n-paste {
        position: absolute !important;
        top: 5px;
        right: 9px;
    }

    table th {
        text-align: center !important;
        padding: 2px 4px !important;
    }

    table td {
        font-size: 13px !important;
        vertical-align: middle !important;
        padding: 2px 4px !important;
        line-height: 1.4 !important;
    }
    </style>
    <script>
        var caseSamplingDataUrl = "${caseSamplingDataUrl}";
        var serverUrl = "";
    </script>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${qualityService.getLabelForType(params.dataType)}" options="${true}" filterButton="${true}" customButtons="${g.render(template: "includes/shareTemplate", model:[dataType: params.dataType])}">
                <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigurationInstance}"
                          var="theInstance"/>
                <g:render template="/includes/layout/inlineAlerts"/>
                <div class="alert alert-danger hide">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                    <span id="pageErrorMessage"></span>
                </div>
                <div class="alert alert-success" style="display: none">
                    <a href="#" class="close" data-evt-clk='{"method" : "addClassHide", "params" : [".alert-success"]}' aria-label="close">&times;</a>
                    <span id="successNotification"></span>
                </div>
                <div>
                    <g:render template="includes/case_sampling_list"
                              model="[columnNameList: moduleColumnList.fieldName, dataList: dataList]"/>
                </div>
            </rx:container>
        </div>
    </div>
</div>
<input type="hidden" name="dataType" id="dataType" value="${params.dataType}">
<input type="hidden" name="commentDataType" id="commentDataType"  value="${ PvqTypeEnum.SAMPLING.name()}">
<g:javascript type="text/javascript">
    var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest',action: 'workFlowForQuality')}"
    var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest',action: 'saveQualityWorkFlow')}";
    var errorTypeListText = "${errorTypeList}";
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
    var caserecordajaxurl = "${createLink(controller: 'quality', action: 'qualitySamplingAjax')}?dataType=${params.dataType}";
    var caseDataLinkUrl = '${createLink(controller: 'quality', action: 'caseForm')}?type=${params.dataType}';
    var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
    var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
    var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
    var reportURL = "${createLink(controller: "report", action: "showFirstSection")}";
    var addToManualUrl = "${createLink(controller: "quality", action: "addToManual")}";
    var addToManualDataType = "${com.rxlogix.enums.PvqTypeEnum.CASE_QUALITY}";
    var fetchUsersUrl = '${createLink(controller: 'quality', action: 'fetchUsers')}';
    var fetchUsersAndGroupsUrl = '${createLink(controller: 'quality', action: 'fetchUsersAndGroups')}';
    var qualityPriorityTagsUrl = '${createLink(controller: 'quality', action: 'getQualityPriorityList')}';
    var updatePriorityUrl = '${createLink(controller: 'quality', action: 'updatePriority')}';
    var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'indexPvq')}";
    var updateAssignedOwnerUrl = '${createLink(controller: 'quality', action: 'updateAssignedOwner')}';
    var editRole=false;
    var manualEntryType = "${com.rxlogix.enums.QualityEntryTypeEnum.MANUAL.getValue()}";
    var fetchCriteriaForManualErrorUrl = "${createLink(controller: 'quality', action: 'fetchCriteriaForManualError')}";
    var fetchCommentsUrl = "${createLink(controller: 'commentRest', action: 'loadComments')}";
    var deleteCommentsUrl = "${createLink(controller: 'commentRest', action: 'delete')}";
    var saveCommentsUrl = "${createLink(controller: 'commentRest', action: 'save')}";
    var downloadSourceDocumentsUrl = '${createLink(controller: 'quality', action: 'downloadSourceDocuments')}';
    var fetchSourceDocumentsUrl = '${createLink(controller: 'quality', action: 'fetchSourceDocuments')}';
    var viewSourceDocumentsEnabled = ${grailsApplication.config.dataSources.safetySource ? "true" : "false"};
    var deleteCasesUrl = '${createLink(controller: 'quality', action: 'deleteCases')}?type=${params.dataType}';
    var deleteFieldCaseMsgUrl = '${createLink(controller: 'quality', action: 'displayFieldLevelMsg')}?type=${params.dataType}';
    var fetchIssueNumberCaseUrl = "${createLink(controller: 'issue', action: 'fetchIssueNumberCase' )}";
    var fetchDataIssueUrl = "${createLink(controller: 'issue', action: 'fetchDataIssue' )}";
    var updateQualityCapaUrl = "${createLink(controller: "issue", action: "updateCapaForQuality")}";
    var createCapaAttachmentUrl="${createLink(controller: 'issueRest', action: 'createCapaAttachment')}";
    var updateCapaAttachmentUrl="${createLink(controller: 'issueRest', action: 'updateCapaAttachment')}";
    var isAppliedAdvancedFilter = false;
    var showCriteriaForManualError=false;
    var fetchErrorTypesUrl = "${createLink(controller: 'quality', action: 'fetchQualityModuleErrorsList')}";
    var fetchFilterPanelDataUrl = '${createLink(controller: 'quality', action: 'getFilterData')}';
    <sec:ifAnyGranted roles="ROLE_PVQ_EDIT">
        editRole=true;
    </sec:ifAnyGranted>
    var getIssueNumberUrl = "${createLink(controller: 'issue', action: 'getIssueNumber' )}";
    var createCapaUrl = '${createLink(controller: 'issue', action: 'createCapaForQuality')}'
    var updateQualityIssueTypeUrl = '${createLink(controller: 'quality', action: 'updateQualityIssueType')}';
    var updateRootCauseUrl = '${createLink(controller: 'quality', action: 'updateRootCause')}';
    var updateResponsiblePartyUrl = '${createLink(controller: 'quality', action: 'updateResponsibleParty')}';
    var workflowStatesUrl = "${createLink(controller: 'workflowState',action: 'list')}";
    var issuesList = ${raw(JsonOutput.toJson(qualityIssues))};
    var rootCauseList = ${raw(JsonOutput.toJson(rootCauses))};
    var responsiblePartyList = ${raw(JsonOutput.toJson(responsibleParties))};
    var viewType = '${viewType}';
    var correctiveActionList = ${raw(JsonOutput.toJson(correctiveActions))};
    var preventativeActionList = ${raw(JsonOutput.toJson(preventativeActions))};
    var lateList =  issuesList;
    var saveAllRcaForQualityCaseUrl = "${createLink(controller: 'quality', action: 'saveAllRcasForCase')}";
    var getAllRcasForQualityCaseUrl = "${createLink(controller: 'quality', action: 'getAllRcasForCase')}";
    var EMPTY_LABEL="${ViewHelper.getEmptyLabel()}";
    var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
    var importExcel="${createLink(controller: 'quality', action: 'importExcel')}";
    var validateValue="${createLink(controller: 'query', action: 'validateValue')}";
    var downloadAllAttachmentUrl="${createLink(controller: 'issue', action: 'downloadAllAttachment')}";
    var downloadAttachmentUrl = "${createLink(controller: 'issue', action: 'downloadAttachment')}";
    var removeIssueAttachmentsUrl = "${createLink(controller: 'issueRest', action: 'removeAttachments')}";
    var possibleValuesUrl = "${createLink(controller: 'quality', action: 'fieldPossibleValues')}";
    var AttachmentSizeLimit = ${grailsApplication.config.grails.controllers.attachment.maxFilSize};
    var getActionItemChangesHistoryUrl = "${createLink(controller: 'actionItem', action: 'getActionItemChangesHistory')}";
    var getSelectAllUrl = "${createLink(controller: 'quality', action: 'getSelectAll')}";
    var isPvqRole = ${SpringSecurityUtils.ifAnyGranted("ROLE_USER_GROUP_RCA_PVQ")};
    var MY_GROUPS_VALUE = 'MY_GROUPS';
    var sharedWithUserListUrl = "${createLink(controller: 'userRest', action: 'sharedWithUserListPvq')}";
    var sharedWithGroupListUrl = "${createLink(controller: 'userRest', action: 'sharedWithGroupListPvq')}";
    var sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
</g:javascript>
<asset:javascript src="app/workFlow.js"/>
<asset:javascript src="app/quality/common.js"/>
<asset:javascript src="app/quality/case_sampling_list.js"/>
<asset:javascript src="app/quality/qualityLateProcessing.js"/>
<g:render template="/includes/widgets/errorTemplate"/>
<g:render template="/actionItem/includes/actionItemModal"/>
<g:render template="/caseList/includes/addQualityComment" model="[caseSeriesId: 0]"/>
<g:render template="includes/sourceDocuments"/>
<g:render template="/query/copyPasteModal" model="[isPVQ: true]"/>
<div id="errorTypeModal" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h5 class="modal-title modalHeader"><g:message code="qualityModule.manualAdd.errorType.title.label"/></h5>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="mandatoryDlgErrorDiv" style="display: none">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <i class="fa fa-check"></i> <g:message code="app.error.fill.all.required"/>
                </div>
                <div class="container">
                    <div class="alert alert-success" style="display: none">
                    </div>
                    <label class=""><g:message code="qualityModule.manualAdd.errorType.label"/></label>
                    <input type="text" class="form-control" id="type_fld"/>
                    <label class=""><g:message code="qualityAlert.additionalDetails"/></label>
                    <input type="text" class="form-control" id="additionalDetails_fld"/>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey okButton" ><g:message code="default.button.ok.label"/></button>
                <button type="button" class="btn pv-btn-grey " data-dismiss="modal"><g:message code="default.button.close.label"/></button>
            </div>
        </div>

    </div>
</div>
<g:render template="includes/errorExport"/>
<g:render template="includes/ignoreConfirmationModal" />
<g:render template="/query/workflowStatusJustification" model="[tableId:'case-sampling-list']"/>
<g:render template="includes/viewQualityDelayReasonModal" />
<g:render template="/includes/widgets/deleteRecord"/>
<g:render template="/includes/widgets/infoTemplate"/>
<g:render template="includes/assignedToPVQFilter"/>
<form style="display: none" id="excelExport" method="post" action="${createLink(controller: 'quality', action: 'exportToExcelQualitySampling')}">
    <input name="dataType" value="${params.dataType}">
    <input name="data" id="excelData">
</form>
<form style="display: none" id="changeView" method="post" action="${createLink(controller: 'quality', action: 'caseSampling')}">
    <input type="hidden" name="viewOption" id="viewOption" value="ICV">
    <input name="dataType" value="${params.dataType}">
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