<%@ page import="com.rxlogix.dynamicReports.OutputBuilder; com.rxlogix.util.MiscUtil; com.rxlogix.config.ReportResult; com.rxlogix.util.ViewHelper; com.rxlogix.enums.ReportFormatEnum; com.rxlogix.enums.ReasonOfDelayFieldEnum; groovy.json.JsonOutput; grails.plugin.springsecurity.SpringSecurityUtils" %>
<g:set var="userService" bean="userService"/>
<html>
<head>
    <meta name="layout" content="main"/>

    <title><g:message code="app.viewResult.title"/></title>
    <asset:stylesheet src="rowGroup.dataTables.min.css"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:javascript src="app/actionItem/actionItemList.js"/>
    <asset:javascript src="app/workFlow.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="datatables/dataTables.info.js"/>

    <asset:javascript src="datatables/extendedDataTable.js"/>
    <asset:stylesheet src="datatables/extendedDataTable.css"/>

    <asset:javascript src="datatables/dataTables.columnResize.js"/>
    <asset:stylesheet src="datatables/dataTables.columnResize.css"/>

    <asset:javascript src="datatables/dataTables.fixedHeader.js"/>
    <asset:stylesheet src="datatables/dataTables.fixedHeader.css"/>
    <g:javascript>
        var saveDelayReasonData = "${createLink(controller: 'advancedReportViewer', action: 'saveDelayReasonData')}";
        var getAllRcasForCaseUrl = "${createLink(controller: 'advancedReportViewer', action: 'getAllRcasForCase')}";
        var lockRcaUrl = "${createLink(controller: 'advancedReportViewer', action: 'lockRca')}";
        var unlockRcaUrl = "${createLink(controller: 'advancedReportViewer', action: 'unlockRca')}";
        var uploadAttachmentsUrl = '${createLink(controller: 'advancedReportViewer', action: 'uploadAttachment')}';
        var downloadAttachmentsUrl = '${createLink(controller: 'advancedReportViewer', action: 'downloadAttachment')}';
        var removeAttachmentsUrl = '${createLink(controller: 'advancedReportViewer', action: 'removeAttachments')}';
        var getAllAttachmentsUrl = '${createLink(controller: 'advancedReportViewer', action: 'getAllAttachments')}';
        var runConfigurationUrl = "${createLink(controller: 'configuration', action: 'runOnce', params: ['id': configurationId])}";
        var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
        var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
        var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'indexDrilldown')}";
        var getIssueNumberUrl = "${createLink(controller: 'issue', action: 'getIssueNumber' )}";
        var createCapaUrl = '${createLink(controller: 'issue', action: 'createCapaForReasonOfDelay')}';
        var fetchUsersUrl = '${createLink(controller: 'issue', action: 'fetchUsers')}';
        var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest',action: 'workFlowForReasonOfDelay')}"
        var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest',action: 'savePVCWorkFlow')}";
        var sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
        var sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
        var updateAssignedOwnerUrl = '${createLink(controller: 'advancedReportViewer', action: 'updateAssignedOwner')}';
        var caseFormUrl = '${createLink(controller: 'advancedReportViewer', action: 'caseForm')}';
        var fetchIssueNumberUrl = "${createLink(controller: 'issue', action: 'fetchIssueNumber' )}";
        var fetchDataIssueUrl = "${createLink(controller: 'issue', action: 'fetchDataIssue' )}";
        var updateCapaUrl = "${createLink(controller: "issue", action: "updateCapaForReasonOfDelay")}";
        var reportrecordajaxurl = "${createLink(controller: 'advancedReportViewer', action: 'cllAjax')}";
        var getSimilarCasesUrl = "${createLink(controller: 'advancedReportViewer', action: 'getSimilarCases')}";
        var getCapaDescUrl = "${createLink(controller: 'issue', action: 'getRODCapaDescription' )}";
        var removeIssueAttachmentsUrl = "${createLink(controller: 'issueRest', action: 'removeAttachments')}";
        var downloadAllAttachmentUrl="${createLink(controller: 'issue', action: 'downloadAllAttachment')}";
        var validateValue="${createLink(controller: 'query', action: 'validateValue')}";
        var importExcel="${createLink(controller: 'quality', action: 'importExcel')}";
        var reasonOfDelayId="${MiscUtil.getReasonOfDelayId(reportResultId)}";
        var MY_GROUPS_VALUE = 'MY_GROUPS';

    </g:javascript>
    <g:if test="${isInbound}">
    <g:javascript>
     var sharedWithUserListUrl = "${createLink(controller: 'userRest', action: 'sharedWithUserListPvcInb')}";
        var sharedWithGroupListUrl = "${createLink(controller: 'userRest', action: 'sharedWithGroupListPvcInb')}";
    </g:javascript>
    </g:if>
    <g:else>
        <g:javascript>
        var sharedWithUserListUrl = "${createLink(controller: 'userRest', action: 'sharedWithUserListPvc')}";
        var sharedWithGroupListUrl = "${createLink(controller: 'userRest', action: 'sharedWithGroupListPvc')}";
        </g:javascript>
    </g:else>
    <asset:javascript src="app/rxTitleOptions.js"/>
    <style>
    .dt-paging-button {
        margin: 0 !important;
        padding: 0 !important;
    }

    #tableDiv .dt-container {
        overflow-x: auto;
    }
    table.pvtUi {
        margin: 0 auto;
    }

    .qualityShowHideCellContent {
        display: none !important;
    }

    tr:hover .qualityShowHideCellContent {
        display: inline-block !important;
    }
    table th{
        text-align: center !important;
        padding: 2px 4px !important;
    }
    table td {
        cursor: default;
        font-size: 13px !important;
        vertical-align: middle !important;
        padding: 2px 4px !important;
        line-height: 1.4 !important;
    }
    @media (min-width: 768px) {
        .modal-xl {
            width: 100%;
            max-width:1500px;
        }
    }
   .commentModalTrigger{
        position: unset !important;
    }

    .popover-content{
        color: #000 !important;
    }

    .view-hover:hover .dropdown-menu {
        display: block !important;
    }

    .custom-badge {
        position: initial !important;
        margin-top: -15px !important;
        margin-left: 10px !important;
    }

    .display-flex {
        display: flex;
    }

    .display-flex .btn {
        padding: 3px 6px;
    }
    .reasonOfDelayModalBody .select2-dropdown {
        position: fixed;
    }
    .reasonOfDelayModalBody .textAreaCharCounterWrapper {
        margin-bottom: 4px;
    }
    .reasonOfDelayModalBody .textAreaCharCounterWrapper .textAreaCharCounter {
        margin-top: -4px;
    }
    .dt-layout-row:first-child {
        padding-right: initial !important;
        margin-top: 0 !important;
    }
    #table_wrapper {
        overflow-x: hidden !important;
    }
    #table_wrapper .dt-layout-table {
        overflow-x: auto !important;
        height: calc(100vh - 210px) !important;
    }
    #reasonOfDelayModalId.modal .select2-container--default .select2-results > .select2-results__options {
        padding-bottom: 20px !important;
    }
    #reasonOfDelayModalId .modal-content {
        min-width: 840px;
    }
    #reasonOfDelayBody textarea[name=investigation],
    #reasonOfDelayBody textarea[name=summary],
    #reasonOfDelayBody textarea[name=actions] {
        width: 100%;
    }
</style>

</head>

<body>
<g:render template="/actionItem/includes/actionItemModal"/>
<g:render template="/query/workflowStatusJustification" model="[tableId:'table']"/>
<div class="report-breadcrums">
    <div class=" breadcrumbsDiv ">
        <g:if test="${breadcrumbs}">
            <g:each in="${breadcrumbs}" var="tpl" status="i">
                ${raw(i > 0 ? "-&gt;" : "")}
                <span>
                    <g:if test="${i < breadcrumbs.size() - 1}">
                        <a href="javascript:void(0)" class="backLink" data-id="${tpl.id}" data-href="${createLink(controller: tpl.controller, action: tpl.action)}">${tpl.name}</a>
                    </g:if>
                    <g:else>
                        ${tpl.name}
                    </g:else>
                </span>
            </g:each>
        </g:if>
    </div>
</div>
<div class="content m-t-5">
    <div class="container">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${seriesInstance}" var="theInstance"/>
<rx:container id="qualityTableContainer" title="${message(code: 'app.label.report')}: ${applyCodec(encodeAs: 'HTML', reportName)} ${templateName ? ("(" + templateName + ")") : ""}"  options="${true}"  customButtons="${g.render(template: "tableButtons", model:pageScope.variables )}">
    <div class="alert alert-danger hide">
        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
        <span id="pageErrorMessage"></span>
    </div>
    <div class="alert rodSuccessAlert alert-success" style="display: none">
        <a href="#" class="close"aria-label="close">&times;</a>
        <span id="pageSuccessMessage"></span>
    </div>
    <div class="alert loadingWarning alert-warning alert-dismissible forceLineWrap" role="alert" style="display: none">
        <button type="button" class="close" data-evt-clk='{"method" : "addClassHide", "params" : [".forceLineWrap"]}'>
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <i class="fa fa-warning"></i>
        <g:message code="app.reasonOfDelay.loading.warn" default="Report data is being processed, please reload the page after a while... "/>
    </div>
    <form id="viewDelayReasonFormId">
        <div id="tableDiv" class="pv-caselist">
            <table id="table" class="table table-striped display order-column list-table pv-list-table dataTable no-footer">
            </table>
        </div>
    </form>
    <form id="backForm" method="get" >
        <input id="backFormId" name="id" type="hidden">
        <input id="backFormFilter" name="filter" type="hidden" >
        <input name="back" type="hidden" value="true">
    </form>
    <form id="exportForm" method="post">
        <input type="hidden" name="filter" value="${params.filter}">
        <input type="hidden" name="direction" id="direction" >
        <input type="hidden" name="sort" id="sort" >
        <input type="hidden" name="searchData" id="searchData" >
        <input type="hidden" name="globalSearch" id="globalSearch" >
        <input type="hidden" name="assignedToFilter" id="assignedToFilterInput" >
        <input type="hidden" name="dynamic" value="true" >
        <input type="hidden" name="rowIdFilter" id="rowIdFilter" >
    </form>
</rx:container>
    <div class="modal fade" id="informationModalId"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="informationModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="informationModalLabel">${title ?: 'Warning'}</h4>
            </div>

            <div class="modal-body">
                <div id="informationType">${informationType ?: g.message(code: 'app.label.warning.modal')}</div>

                <p></p>

                <div class="description" style="font-weight:bold;">${messageBody ?: ''}</div>

                <div class="extramessage"></div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-dismiss="modal"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div>
            <div class="modal fade resizableModal" id="reasonOfDelayModalId"  tabindex="-1" role="dialog" >
                <div class="modal-dialog modal-lg"  style="width:1300px;" role="document">
                    <div class="modal-content">
                        <div class="modal-header"  style="cursor: move">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                            <h4 class="modal-title" id="reasonOfDelayModalLabel"><g:message code="app.reasonOfDelay.title"/></h4>
                        </div>

                        <div class="modal-body" style="width: 100%; height: calc(100% - 85px);">
                            <div class="alert alert-danger hide">
                                <a href="#" class="close" aria-label="close">&times;</a>
                                <span class="errorMessageSpan"></span>
                            </div>
                            <div class="row">
                                <div class="col-md-4 workflowAssignTo" style="display: none; white-space: nowrap;">
                                    <div class="alert alert-warning alert-dismissible" role="alert">
                                        <g:message code="app.ROD.assignTo.warning"/>
                                    </div>
                                </div>
                            </div>
                            <form id="reasonOfDelayModalForm">
                            <div class="row reasonOfDelayModalFormHeader">
                                <div class="col-md-2 ">
                                    <label><g:message code="app.pvc.late"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Issue_Type.toString()}">*</span></label>
                                    <div class="lateSelectDiv"></div>
                                </div>
                                <div class="col-md-2">
                                    <label><g:message code="app.label.assignedToGroup"/></label>
                                    <select class='editAssignedToGroup col-md-12 form-control' name='assignedToGroup' value=''></select>
                                </div>
                                <div class="col-md-2">
                                    <label><g:message code="app.label.assignedToUser"/></label>
                                    <select class='editAssignedToUser col-md-12 form-control' name='assignedToUser' value=''></select>
                                </div>
                                <div class="col-md-2">
                                    <label><g:message code="app.pcv.workflowState"/></label>
                                    <input type="hidden" class="workflowCurrentId" name="workflowCurrentId"/>
                                    <select class="form-control workflow" name="workflowRule"><option></option></select>
                                </div>
                                <div class="col-md-6 justificationDiv" style="display: none">
                                    <label><g:message code="app.label.justification"/></label>
                                    <input class="form-control justification" name="justification" maxlength="255">
                                </div>
                                <div class="col-md-8 noworkflow" style="display: none">
                                    <label><g:message code="app.pcv.workflowState"/></label>
                                    <div><g:message code="app.pcv.differentRows"/></div>
                                </div>
                            </div>
                            <div class="row reasonOfDelayModalBody">
                                <div class="col-md-12">
                                    <div id="mainCaseValues" style="display: none">
                                        <input type="hidden" name="versionNumber" >
                                        <input type="hidden" name="caseId" >
                                        <input type="hidden" name="senderId" >
                                        <input type="hidden" name="enterpriseId" >
                                        <input type="hidden" name="reportId" >
                                        <input type="hidden" name="caseNumber" >
                                        <input type="hidden" name="isInbound" >
                                        <input type="hidden" name="cllRowId" >
                                    </div><div id="otherCaseValues" style="display: none"></div>
                                    <table id="reasonOfDelayListTable" class="table">
                                        <thead>
                                        <tr>
                                            <th style="width: 4%;"><span class="table-add glyphicon glyphicon-plus"></span></th>
                                            <th style="width: 4%;"><label><g:message code="app.pvc.pri.sec"/></label></th>
                                            <th style="width: 13%;"><label><g:message code="app.pvc.rootcause"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Root_Cause.toString()} ">*</span><br><g:message code="app.pvc.RootCauseClass"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Root_Cause_Class.toString()} ">*</span></label></th>
                                            <th style="width: 13%;"><label><g:message code="app.pvc.RootCauseSubCategory"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Root_Cause_Sub_Cat.toString()} ">*</span><br><g:message code="app.pvc.ResponsibleParty"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Resp_Party.toString()} ">*</span></label></th>
                                            <th style="width: 13%;"><label><g:message code="app.pvc.CorrectiveAction"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Corrective_Action.toString()} ">*</span><br><g:message code="app.pvc.PreventiveAction"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Preventive_Action.toString()} ">*</span></label></th>
                                            <th style="width: 14%;"><label><g:message code="app.pvc.CorrectiveDate"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Corrective_Date.toString()} ">*</span><br><g:message code="app.pvc.PreventiveDate"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Preventive_Date.toString()} ">*</span></label></th>
                                            <th style="width: 13%;"><label><g:message code="app.pvc.investigation"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Investigation.toString()} ">*</span></label></th>
                                            <th style="width: 13%;"><label><g:message code="app.pvc.summary"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Summary.toString()} ">*</span></label></th>
                                            <th style="width: 13%;"><label><g:message code="app.pvc.actions"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Actions.toString()} ">*</span></label></th>
                                        </tr>
                                        </thead>
                                        <tbody id="reasonOfDelayBody"></tbody>
                                    </table>
                                </div>
                            </div>
                            </form>
                        </div>

                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                            <button type="button" class="btn btn-primary saveReasonsOfDelay" ><g:message code="default.button.save.label"/></button>
                        </div>
                    </div><!-- /.modal-content -->
                </div><!-- /.modal-dialog -->
            </div>


<div class="modal fade" id="uploadFileModalId"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="uploadFileModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <g:form id="attachmentForm">
                <input type="hidden" name="processedReportId" id="vcsProcessedReportIdModalAtt">
                <input type="hidden" name="tenantId" id="masterEnterpriseIdModalAtt">
                <input type="hidden" name="masterCaseId" id="masterCaseIdModalAtt">
                <input type="hidden" name="masterCaseVersion" id="masterCaseVersionModalAtt">
                <input type="hidden" name="masterSenderId" id="masterSenderIdModalAtt">
                <input type="hidden" name="selectedJson" id="selectedJson" value="">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="uploadFileModalLabel"><g:message code="app.pvc.attachments"/></h4>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger hide">
                    <a href="#" id="attachmentCloseButton" class="close" aria-label="close">&times;</a>
                    <span id="attachmentPageErrorMessage"></span>
                </div>
                <div class="row">
                    <div class="alert alert-danger alert-dismissible attachSizeExceed" role="alert" hidden="hidden">
                        <button type="button" class="close" id="attachSizeExceed">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <div id="message"></div>
                    </div>
                    <div class="row">
                        <div class="col-md-12" id="attachmentsList">

                        </div>
                    </div>

                    <div class="col-md-12 viewMode" style="margin-top: 15px;">
                        <div class="input-group" >
                            <input type="text" class="form-control" id="file_name" readonly>
                            <label class="input-group-btn">
                                <span class="btn btn-primary">
                                    <g:message code="app.label.attach"/>
                                    <input type="file" id="file_input2" name="file" multiple  style="display: none;">
                                </span>
                            </label>
                        </div>
                    </div>
                </div>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="default.button.close.label"/></button>
                <button type="button" class="btn btn-primary viewMode attachmentFormSubmit"><g:message code="app.button.add"/></button>
            </div>
        </g:form>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div>

<script>
    var fileUploadUrl = "${createLink(controller: 'advancedReportViewer', action: 'uploadAttachment')}";
    var removeAttachmentsUrl = "${createLink(action: "removeAttachments", controller: "advancedReportViewer")}";
    var lateList = ${raw(JsonOutput.toJson(lateList))};
    var rootCauseList = ${raw(JsonOutput.toJson(rootCauseList))};
    var responsiblePartyList = ${raw(JsonOutput.toJson(responsiblePartyList))};
    var rootCauseClassList = ${raw(JsonOutput.toJson(rootCauseClassList))};
    var rootCauseSubCategoryList = ${raw(JsonOutput.toJson(rootCauseSubCategoryList))};
    var correctiveActionList = ${raw(JsonOutput.toJson(correctiveActionList))};
    var preventativeActionList = ${raw(JsonOutput.toJson(preventativeActionList))};
    var header = ${raw(JsonOutput.toJson(header))};
    var rowColumns = ${raw(JsonOutput.toJson(rowColumns))};
    var serviceColumns = ${raw(JsonOutput.toJson(serviceColumns))};
    var groupColumns = ${raw(JsonOutput.toJson(groupColumns))};
    var stacked = ${raw(JsonOutput.toJson(stacked?:[:]))};
    var fieldTypeMap = ${raw(JsonOutput.toJson(fieldTypeMap))};
    var sectionId=${sectionId};
    var externalFilter = ${raw(params.filter?:"null")};
    var externalFilterCode = ${raw(params.filterCodes?:"null")};
    var fieldsCodeNameMap = ${raw(JsonOutput.toJson(fieldsCodeNameMap))}
    var reportResultId = ${reportResultId};
    var configurationId = ${configurationId};
    var EMPTY_LABEL="&nbsp;";
    var isRcaRole = ${SpringSecurityUtils.ifAnyGranted("ROLE_USER_GROUP_RCA")};
    var isPvcEditor = ${SpringSecurityUtils.ifAnyGranted("ROLE_PVC_EDIT")};
    var MY_GROUPS_VALUE = 'MY_GROUPS';
    var cllRecordId =${params.cllRecordId?:"null"};
    var createCapaAttachmentUrl="${createLink(controller: 'issueRest', action: 'createCapaAttachmentforROD')}";
    var updateCapaAttachmentUrl="${createLink(controller: 'issueRest', action: 'updateCapaAttachment')}";
    var downloadAttachmentUrl = "${createLink(controller: 'issue', action: 'downloadAttachment')}";
    var AttachmentSizeLimit = ${grailsApplication.config.grails.controllers.attachment.maxFilSize};
    var updateUserPreferensesUrl = "${createLink(controller: 'preference', action: 'updateUserPreferences')}";
    var maxRowsExcel=${OutputBuilder.XLSX_MAX_ROWS_PER_SHEET};
    var maxRowsPptx=${grailsApplication.config.pvreports.pptx.max.records};
    var maxRowsPdf=${OutputBuilder.PDF_MAX_PAGES*8};
</script>
<g:if test="${params.back}">
    <script>
        externalFilter = JSON.parse(sessionStorage.getItem("breadcrumbs_${sectionId}_${reportResultId}"));
    </script>
</g:if>
<g:else>
    <script>
        sessionStorage.setItem("breadcrumbs_${sectionId}_${reportResultId}", JSON.stringify(externalFilter))
    </script>
</g:else>

<input type="hidden" id="rodTablePreference" value="${userService.getUserPreferences("rodTablePreference")}">
<asset:javascript src="app/report/lateProcessing.js"/>
<asset:javascript src="app/quality/copyPasteValues.js"/>
    </div>
</div>
<g:form name="attachForm" method="post">
          <input type="hidden" name="selectAll" id="selectAll">
           <input type="hidden" name="selectedIds" id="selectedIds">
           <input type="hidden" name="capaInstanceId" id="capaInstanceId">
</g:form>
<g:render template="/quality/includes/assignedToModal"/>
<g:render template="/query/copyPasteModal"/>
<g:render template="/includes/widgets/commentsWidget"/>
<g:render template="/advancedReportViewer/createIssue"/>
<g:render template="/includes/widgets/deleteRecord"/>
<g:render template="/includes/widgets/infoTemplate"/>
<g:render template="includes/assignSimilarCases"/>
<g:render template="/includes/widgets/warningTemplate" model="[messageBody: message(code: 'app.pvc.export.warn'), warningModalId:'exportWarning', warningButtonId:'exportWarningOkButton', queryType: message(code: 'app.pvc.export.warn_limit', args: [OutputBuilder.XLSX_MAX_ROWS_PER_SHEET,(OutputBuilder.PDF_MAX_PAGES*8),grailsApplication.config.pvreports.pptx.max.records]) ]"/>
<g:render template="assignedToFilter" model="[breadcrumbs: breadcrumbs]"/>
<div style="display: none"><g:render template="/includes/widgets/datePickerTemplate"/></div>
<g:render template="/includes/widgets/infoTemplate" model="[messageBody: message(code: 'app.reasonOfDelay.bulkUpdateMaxRowsWarning'), warningModalId:'bulkUpdateMaxRowsWarning', title: message(code: 'app.label.warning')]"/>
</body>
</html>
