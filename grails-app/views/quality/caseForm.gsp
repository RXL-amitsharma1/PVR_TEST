<%@ page import="com.rxlogix.enums.PvqTypeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.QualityEntryTypeEnum; com.rxlogix.enums.QualityIssueTypeEnum; groovy.json.JsonOutput; grails.plugin.springsecurity.SpringSecurityUtils" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.quality.task.caseform.title"/></title>
    <style>
    .dt-container {
        overflow-x: auto;
        margin-right: 5px !important;
      }
    .case-form-assigned-to-box .select2-container {
        max-width: 170px;
        min-width: 120px;
        width: initial !important;
        margin-top: -5px !important;
    }
    .issueListContentDiv.resizable {
        max-width: calc(100% - 8px) !important;
        min-width: 150px;
        min-height: 125px;
    }
    #issuesTable_wrapper .dt-scroll-head {
        margin-top: 40px !important;
    }
    #allIssuesTable_wrapper .dt-scroll-head {
        margin-top: 40px !important;
    }
    .quality-case-form-header-bar {
        z-index: 6;
        background-color: #FFFFFF;
        padding: 4px 52px 0 0;
        position: sticky;
        top: 70px;
    }
    .case-form-header-icon-control {
        margin: 0 8px 8px 4px;
        max-width: 12px !important;
    }
    .case-form-header-icon-control i {
        color: black;
    }
    #showHideAttachmentsSpinner {
        position: absolute;
        right: 4px;
        background-color: #FFFFFF;
    }
    .issueListContentDiv.resizable .ui-icon-gripsmall-diagonal-se,
    .caseContentTable .resizable .ui-icon-gripsmall-diagonal-se {
        z-index: 99999 !important;
        background-image: url('/reports/assets/ui-icons_444444_256x240.png') !important;
    }

    .attachmentContent .rxmain-container {
        height: 100%;
    }
    .attachmentContent .rxmain-container .rxmain-container-inner {
        height: 100%;
    }
    .attachmentContent .rxmain-container .rxmain-container-inner .rxmain-container-content {
        height: calc(100% - 31px);
    }
    .attachmentContent iframe {
        height: calc(100% - 38px)
    }
    </style>
</head>

<body>
<g:set var="qualityService" bean="qualityService"/>
<asset:javascript src="app/rxTitleOptions.js"/>
<div class="content">
    <div class="container">
        <div class="row quality-case-form-header-bar">
            <div class="col-md-1" style="min-width: fit-content">
                <label style="padding-left: 5px;"><g:message code="app.caseNumber.label"/>:</label>
                <a href='${createLink(controller: 'report', action: 'drillDown')}?caseNumber=${params.caseNumber}&versionNumber=${params.versionNumber}' target="_blank">${caseNumber} (${versionNumber})</a>
            </div>
            <g:if test="${type==PvqTypeEnum.SUBMISSION_QUALITY.name()}">
                <div class="col-md-1" style="min-width: fit-content">
                    <label><g:message code="app.queryLevel.SUBMISSION"/>:</label> ${agency} (${submissionIdentifier})
                </div>
            </g:if>
            <g:else>

            </g:else>

            <div class="col-md-1" style="min-width: fit-content">
                <label><g:message code="app.label.quality.totalQualityIssues"/>:</label> <span id="totalLabel"></span>
                <g:renderInlineSpinner id="totalQualityIssuesSpinner" />
            </div>
            <div class="col-md-1-half" style="min-width: fit-content">
                <label><g:message code="app.label.state"/>:</label>
                <g:if test="${isFinalState}">
                    <button id="case-form-state" class="btn btn-default btn-xs button-ellipses" disabled="disabled" style="min-width: 80px" data-dataType="${type}" data-quality-data-id= ${id} data-initial-state="${state}" data-evt-clk='{"method": "openStateHistoryModal", "params": []}' title="<g:message code="app.label.quality.case.final.state"/>">${state}</button>
                </g:if>
                <g:else>
                    <button id="case-form-state" class="btn btn-default btn-xs button-ellipses" style="min-width: 80px" data-dataType="${type}" data-quality-data-id= ${id} data-initial-state="${state}" data-evt-clk='{"method": "openStateHistoryModal", "params": []}' title="${state}">${state}</button>
                </g:else>
            </div>

            <div class="col-md-2 case-form-assigned-to-box" style="min-width: fit-content">
                <label><g:message code="app.label.assignedToGroup"/>:</label>
                <select id="case-form-assignedToUserGroup"
                        class='form-control'
                    ${isFinalState?"disabled":""}
                        name='assignedToUserGroup'
                        data-value='${assignedToUserGroup}' value='${assignedToUserGroup}' data-submissionIdentifier='${submissionIdentifier}'
                        data-version-number='${versionNumber}' data-case-number='${caseNumber}'
                        data-error-type='${errorType}' data-dataType='${type}'></select>
            </div>

            <div class="col-md-2 case-form-assigned-to-box" style="min-width: fit-content">
                <label><g:message code="app.label.assignedToUser"/>:</label>

                    <select id="case-form-assignedToUser"
                            class='form-control'
                        ${isFinalState?"disabled":""}
                            name='assignedToUser'
                            data-value='${assignedToUser}' value='${assignedToUser}' data-submissionIdentifier='${submissionIdentifier}'
                            data-version-number='${versionNumber}' data-case-number='${caseNumber}'
                            data-error-type='${errorType}' data-dataType='${type}'></select>
            </div>

            <div style="position: absolute; right: 12px; height: 26px;">
                <div class="case-form-header-icon-control col-md-1-half">
                    <a href="javascript:void(0)" class="showHideEmptyFields">
                        <i class="fa fa-eye" aria-hidden="true" title="${message(code: 'app.label.quality.showEmptyFields')}"></i>
                    </a>
                </div>
                <div class="case-form-header-icon-control col-md-1-half">
                    <a href="javascript:void(0)" class="showHideAttachments">
                        <i class="fa fa-paperclip" aria-hidden="true" title="${message(code: 'app.label.quality.showHideAttachments')}"></i>
                    </a>
                </div>
                <g:renderInlineSpinner id="showHideAttachmentsSpinner" />
            </div>

        </div>
        <div class="row mt-10" style="padding-bottom: 5px; margin-right: 5px">
            <div class="alert alert-success" style="display: none">
                <a href="#" class="close" id="closeSuccessButton" data-dismiss="alert" aria-label="close">&times;</a>
                <span id="successNotification"></span>
            </div>
            <div class="col-md-12 case-form-container" style="margin-left: 5px">
            <table class="caseContentTable" style="width: 100%; padding: 5px; margin-top: 5px; margin-bottom: 5px">
                <tr>
                    <td class="resizable" style="width:50%;vertical-align: top;padding-right: 3px;;">
                        <div class=" caseContent" style="width: 100%; height: 100%;overflow-y: auto;overflow-x: hidden">
                            <g:render template="/quality/includes/caseFormCaseInfo" />
                        </div>
                    </td>
                    <td class="resizable">
                        <div class="attachmentContent"  style="width: 100%; height: 100%;padding-left: 3px;">
                            <g:render template="/quality/includes/caseFormAttachment" />
                        </div>
                    </td>
                </tr>

        </table>
        </div>
        </div>
        <div class="col-md-12 case-form-container" style="margin-top: 5px; padding-bottom: 5px; padding-top: 5px">
        <div class="row pv-caselist" style="margin-left: 2px; margin-right: 2px">

                <div class="rxmain-container rxmain-container-top" style="margin-bottom:5px;">
                    <div class="rxmain-container-inner">
                        <div class="rxmain-container-row rxmain-container-header">
                            <i class="fa fa-caret-right fa-lg click issueListDiv qualityIssuesList"></i>
                            <label class="rxmain-container-header-label click issueListDiv qualityIssuesList">
                                <g:message code="app.label.quality.qualityIssuesList"/>
                            </label>
                            <i class="pull-right dropdown-toggle md md-list md-lg rxmain-dropdown-settings" id="dropdownMenu1" data-toggle="dropdown" style="line-height: 1.0em !important;"></i>
                            <div class="pull-right dropdown-menu" aria-labelledby="dropdownMenu1">
                                <div class="rxmain-container-dropdown">
                                    <div>
                                        <table id="tableColumns" class="table table-condensed rxmain-dropdown-settings-table">
                                            <thead>
                                            <tr>
                                                <th>
                                                    <g:message code="app.label.name"/></th>
                                                <th>
                                                    <g:message code="app.label.show"/></th>
                                            </tr>
                                            </thead>
                                        </table>
                                    </div>
                                </div>
                            </div>
                            <i class="pull-right fa fa-filter lib-filter pt-3" id="caseFilter" style="padding-right: 10px; cursor: pointer;"></i>
                            <div class="pull-right" style="cursor: pointer; text-align: right; position: relative">
                                <i class="glyphicon glyphicon-download-alt  excelExportCaseForm pull-right" style="margin-right:10px; line-height: 1.2em;" title="<g:message code="quality.shareDropDown.excexcel.menu"/>" name="Excel" id="Excel"></i>
                            </div>
                            <sec:ifAnyGranted roles="ROLE_ADMIN">
                                <i class="glyphicon glyphicon-trash pull-right deleteErrorsCaseForm pt-3" id="caseDelete" style="margin-right:10px; cursor: pointer;" title="<g:message code="default.button.delete.label"/>"></i>
                            </sec:ifAnyGranted>
                        </div>

                        <div class="rxmain-container-content rxmain-container-hide issueListContentDiv resizable" style="padding: 0 0 15px 0 !important; overflow-x: hidden; overflow-y: hidden">

                <table id="issuesTable" class="table table-striped pv-list-table dataTable no-footer dropdown-outside">
                    <thead>
                    <tr>
                        <th class="stickyHeader" style="vertical-align: middle;text-align: center; max-width: 50px"> <div class="checkbox checkbox-primary">
                        <g:checkBox name="selectAll" checked="false"/>
                            <label for="selectAll"></label>
                        </div></th>
                        <th class="stickyHeader text-center"><g:message code="app.label.quality.monitoringType"/></th>
                        <th class="stickyHeader text-center"><g:message code="app.label.quality.issueDescription"/></th>
                        <th class="stickyHeader text-center"><g:message code="app.label.quality.fieldName"/></th>
                        <th class="stickyHeader text-center"><g:message code="app.label.quality.value"/></th>
                        <th class="stickyHeader text-center"><g:message code="app.label.quality.fieldLocation"/></th>
                        <th class="stickyHeader text-center"><g:message code="app.label.quality.source"/></th>
                        <th class="stickyHeader text-center"><g:message code="app.label.icsr.profile.conf.dueInDays"/></th>
                        <th class="stickyHeader text-center"><g:message code="app.label.action.item.priority"/></th>
                        <th class="stickyHeader text-center">
                            <g:message code="quality.capa.issueType.label"/><br>
                            <g:message code="quality.capa.rootCause.label"/><br>
                            <g:message code="quality.capa.responsibleParty.label"/>
                        </th>
                        <th class="stickyHeader"></th>
                        <th class="commentIssueHeader text-center">
                            <g:message code="${message(code: 'comment.textData.label')}"/><br/>
                            <g:message code="${message(code: 'app.label.quality.issue')}"/>
                        </th>
                        <th class="stickyHeader text-center" style="width: 100px;"><g:message code="app.label.action"/></th>
                    </tr>
                    </thead>
                </table>
                        </div>
                    </div>
                </div>
        </div>
        <div class="row pv-caselist" style="margin-left: 2px; margin-right: 2px">
            <div class="rxmain-container rxmain-container-top">
                <div class="rxmain-container-inner">
                    <div class="rxmain-container-row rxmain-container-header">
                        <i class="fa fa-caret-right fa-lg click issueListDiv allQualityIssuesList" ></i>
                        <label class="rxmain-container-header-label click issueListDiv allQualityIssuesList">
                            <g:message code="app.label.quality.all.qualityIssuesList"/>
                        </label>
                        <i class="pull-right dropdown-toggle md md-list md-lg rxmain-dropdown-settings" id="dropdownMenu2" data-toggle="dropdown" style="line-height: 1.0em !important;"></i>
                        <div class="pull-right dropdown-menu" aria-labelledby="dropdownMenu1">
                            <div class="rxmain-container-dropdown">
                                <div>
                                    <table id="tableColumns2" class="table table-condensed rxmain-dropdown-settings-table">
                                        <thead>
                                        <tr>
                                            <th>
                                                <g:message code="app.label.name"/></th>
                                            <th>
                                                <g:message code="app.label.show"/></th>
                                        </tr>
                                        </thead>
                                    </table>
                                </div>
                            </div>
                        </div>
                        <div class="pull-right" style="cursor: pointer; text-align: right; position: relative">
                            <i class="glyphicon glyphicon-download-alt  excelExportCaseForm pull-right" style="margin-right:10px; line-height: 1.2em" title="<g:message code="quality.shareDropDown.excexcel.menu"/>" name="Excel" id="allExcel"></i>
                        </div>
                    </div>

                    <div class="rxmain-container-content rxmain-container-hide issueListContentDiv resizable" style="padding: 0 0 15px 0 !important; overflow-x: hidden; overflow-y: hidden">
                        <table id="allIssuesTable" class="table table-striped pv-list-table dataTable no-footer">
                            <thead>
                            <tr>
                                <th class="stickyHeader" style="vertical-align: middle;text-align: center; max-width: 50px"> <div class="checkbox checkbox-primary">
                                    <g:checkBox name="selectAll2" checked="false"/>
                                    <label for="selectAll2"></label>
                                </div></th>
                                <th class="stickyHeader text-center" style="min-width: 130px"><g:message code="app.label.quality.monitoringType"/></th>
                                <th class="stickyHeader text-center" style="min-width: 300px"><g:message code="app.label.quality.issueDescription"/></th>
                                <th class="stickyHeader text-center" style="min-width: 100px"><g:message code="app.label.quality.fieldName"/></th>
                                <th class="stickyHeader text-center" style="min-width: 175px"><g:message code="app.label.quality.value"/></th>
                                <th class="stickyHeader text-center" style="min-width: 190px"><g:message code="app.label.quality.fieldLocation"/></th>
                                <th class="stickyHeader text-center" style="min-width: 45px"><g:message code="app.label.quality.source"/></th>
                                <th class="stickyHeader text-center" style="min-width: 100px"><g:message code="app.label.icsr.profile.conf.dueInDays"/></th>
                                <th class="stickyHeader text-center" style="min-width: 80px"><g:message code="app.label.action.item.priority"/></th>
                                <th class="stickyHeader text-center" style="min-width: 100px">
                                    <g:message code="quality.capa.issueType.label"/><br>
                                    <g:message code="quality.capa.rootCause.label"/><br>
                                    <g:message code="quality.capa.responsibleParty.label"/>
                                </th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        </div>
    </div>
</div>
<input type="hidden" name="commentDataType" id="commentDataType"  value="${params.type}">
<input type="hidden" name="submissionIdentifier" id="submissionIdentifier"  value="${params.submissionIdentifier}">
<g:javascript type="text/javascript">
    var CASE_QUALITY = '${CommentTypeEnum.CASE_QUALITY.name()}';
    var SUBMISSION_QUALITY =  '${CommentTypeEnum.SUBMISSION_QUALITY.name()}';
    var SAMPLING =  '${CommentTypeEnum.SAMPLING.name()}';
    var downloadUrl = "${createLink(controller: "quality", action: "viewSourceDocument")}";
    var issueListUrl = "${createLink(controller: "quality", action: "issueList")}?caseNumber=${params.caseNumber}&versionNumber=${params.versionNumber}&submissionIdentifier=${params.submissionIdentifier}&type=${params.type}";
    var allIssueListUrl = "${createLink(controller: "quality", action: "getAllQualityIssues")}?caseNumber=${params.caseNumber}&versionNumber=${params.versionNumber}&type=${params.type}";
    var updateFieldErrorURL = "${createLink(controller: "quality", action: "updateFieldError")}";
    var caseNumber = '${params.caseNumber}';
    var versionNumber = '${params.versionNumber}';
    var assignedToId = '${raw(JsonOutput.toJson(assignedToId))}';
    var isFinalState = '${raw(JsonOutput.toJson(isFinalState))}';
     var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest',action: 'workFlowForQuality')}"
    var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest',action: 'saveQualityWorkFlow')}";
    var updateAssignedOwnerUrl = '${createLink(controller: 'quality', action: 'updateAssignedOwner')}';
    var errorTypeListText = "${errorTypeList}";
    var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
    var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
    var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
    var reportURL = "${createLink(controller: "report", action: "showFirstSection")}";
    var addToManualUrl = "${createLink(controller: "quality", action: "addToManual")}";
    var addToManualDataType = "${com.rxlogix.enums.PvqTypeEnum.CASE_QUALITY}";
    var fetchUsersUrl = '${createLink(controller: 'quality', action: 'fetchUsers')}';
    var qualityPriorityTagsUrl = '${createLink(controller: 'quality', action: 'getQualityPriorityList')}';
    var updatePriorityUrl = '${createLink(controller: 'quality', action: 'updatePriority')}';
    var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'indexPvq')}";
    var editRole=false;
    var manualEntryType = "${com.rxlogix.enums.QualityEntryTypeEnum.MANUAL.getValue()}";
    var fetchCriteriaForManualErrorUrl = "${createLink(controller: 'quality', action: 'fetchCriteriaForManualError')}";
    var fetchCommentsUrl = "${createLink(controller: 'commentRest', action: 'loadComments')}";
    var deleteCommentsUrl = "${createLink(controller: 'commentRest', action: 'delete')}";
    var saveCommentsUrl = "${createLink(controller: 'commentRest', action: 'save')}";
    var getIssueNumberUrl = "${createLink(controller: 'issue', action: 'getIssueNumber' )}";
    var createCapaUrl = '${createLink(controller: 'issue', action: 'createCapaForQuality')}'
    var updateQualityIssueTypeUrl = '${createLink(controller: 'quality', action: 'updateQualityIssueType')}';
    var updateRootCauseUrl = '${createLink(controller: 'quality', action: 'updateRootCause')}';
    var updateResponsiblePartyUrl = '${createLink(controller: 'quality', action: 'updateResponsibleParty')}';
    var viewSourceDocumentsEnabled = false;
    var showCriteriaForManualError=false;
    var issuesList = ${raw(JsonOutput.toJson(qualityIssues))};
    var rootCauseList = ${raw(JsonOutput.toJson(rootCauses))};
    var responsiblePartyList = ${raw(JsonOutput.toJson(responsibleParties))};
    var viewType = 'ICV';
    var correctiveActionList = ${raw(JsonOutput.toJson(correctiveActions))};
    var preventativeActionList = ${raw(JsonOutput.toJson(preventativeActions))};
    var lateList =  issuesList;
    var errorListUrl = "${createLink(controller: "quality", action: "issueList")}?caseNumber=${params.caseNumber}&versionNumber=${params.versionNumber}&type=${params.type}&errorList=${true}";
    var fieldNameListUrl = "${createLink(controller: "quality", action: "issueList")}?caseNumber=${params.caseNumber}&versionNumber=${params.versionNumber}&type=${params.type}&fieldName=${true}";
    var fieldLocationListUrl = "${createLink(controller: "quality", action: "issueList")}?caseNumber=${params.caseNumber}&versionNumber=${params.versionNumber}&type=${params.type}&fieldLocation=${true}";
    var qualityPriorityTagsUrl = '${createLink(controller: 'quality', action: 'getQualityPriorityList')}';
    var saveAllRcaForQualityCaseUrl = "${createLink(controller: 'quality', action: 'saveAllRcasForCase')}";
    var getAllRcasForQualityCaseUrl = "${createLink(controller: 'quality', action: 'getAllRcasForCase')}";
    var getQualityIssuesListUrl = "${createLink(controller: 'quality', action: 'getQualityIssuesMap')}";
    var getRootCausesListUrl = "${createLink(controller: 'quality', action: 'getRootCausesMap')}";
    var getResponsiblePartiesListUrl = "${createLink(controller: 'quality', action: 'getResponsiblePartiesMap')}";
    var EMPTY_LABEL="${ViewHelper.getEmptyLabel()}";
    var deleteCasesUrl = '${createLink(controller: 'quality', action: 'deleteCases')}?type=${params.type}';
    var deleteFieldCaseMsgUrl = '${createLink(controller: 'quality', action: 'displayFieldLevelMsg')}?type=${params.type}';
    var getAttachmentsByCaseNoUrl = "${createLink(controller: "quality", action: "getAttachmentsByCaseNo")}?caseNumber=${params.caseNumber}&versionNumber=${params.versionNumber}&type=${params.type}";
    var fetchIssueNumberCaseUrl = "${createLink(controller: 'issue', action: 'fetchIssueNumberCase' )}";
    var fetchDataIssueUrl = "${createLink(controller: 'issue', action: 'fetchDataIssue' )}";
    var updateQualityCapaUrl = "${createLink(controller: "issue", action: "updateCapaForQuality")}";
    var createCapaAttachmentUrl="${createLink(controller: 'issueRest', action: 'createCapaAttachment')}";
    var updateCapaAttachmentUrl="${createLink(controller: 'issueRest', action: 'updateCapaAttachment')}";
    var downloadAllAttachmentUrl="${createLink(controller: 'issue', action: 'downloadAllAttachment')}";
    var downloadAttachmentUrl = "${createLink(controller: 'issue', action: 'downloadAttachment')}";
    var removeIssueAttachmentsUrl = "${createLink(controller: 'issueRest', action: 'removeAttachments')}";
    var AttachmentSizeLimit = ${grailsApplication.config.grails.controllers.attachment.maxFilSize};
    var sharedWithUserListUrl = "${createLink(controller: 'userRest', action: 'sharedWithUserListPvq')}";
    var sharedWithGroupListUrl = "${createLink(controller: 'userRest', action: 'sharedWithGroupListPvq')}";
    var sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";

    <sec:ifAnyGranted roles="ROLE_PVQ_EDIT">
        editRole=true;
    </sec:ifAnyGranted>
</g:javascript>
<asset:stylesheet src="quality.css"/>
<asset:javascript src="vendorUi/fuelux/fuelux.min.js"/>
<asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
<asset:javascript src="app/configuration/configurationCommon.js"/>
<asset:javascript src="app/quality/case_form.js"/>
<asset:javascript src="app/quality/common.js"/>
<asset:javascript src="app/actionItem/actionItemModal.js"/>
<asset:javascript src="app/workFlow.js"/>
<asset:javascript src="app/quality/qualityLateProcessing.js"/>
<asset:javascript src="app/utils/pvr-common-util.js"/>
<asset:javascript src="app/utils/pvr-filter-util.js"/>
<asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
<asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
<asset:javascript src="datatables/dataTables.info.js"/>
<g:render template="/includes/widgets/deleteRecord"/>
<g:render template="/includes/widgets/infoTemplate"/>
<g:render template="/actionItem/includes/actionItemModal"/>
<g:render template="/caseList/includes/addQualityComment" model="[caseSeriesId: 0]"/>
<g:render template="includes/sourceDocuments"/>
<g:render template="/includes/widgets/deleteRecord"/>

<div class="modal fade" id="qualityIssueModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"> <g:message code="app.label.quality.addQualityIssue"/></h4>
            </div>
            <input type="hidden" id="errorId"  />
            <div class="modal-body" style="max-height: 500px;">
                <div class="alert alert-danger hide">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                    <span class="errorMessageSpan"></span>
                </div>
                <div class="row form-group">
                    <div class="col-md-6" style="padding-left: 20px">
                        <div class="form-group">
                            <label>
                                <g:message code="qualityAlert.priority"/>
                            </label>
                            <g:hiddenField name="selectedId" value="${id}"/>
                            <div>
                            <select class="form-control" name="issuePriority" id="issuePriority">
                            </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label><g:message code="app.label.quality.value"/></label>
                            <div><g:textField name="fieldValueLabel" disabled="disabled" class="form-control" required="required"/>
                                <input type="hidden" id="fieldValue" name="fieldValue" />
                            </div>
                        </div>
                        <div class="form-group">
                            <label><g:message code="quality.capa.issueType.label"/></label>

                            <div><g:select name="qualityIssueTypeId" class="form-control" optionKey="id" optionValue="textDesc" from="${qualityIssueTypes}" /></div>
                        </div>
                    </div>

                    <div class="col-md-6" style="padding-left: 20px">

                        <div class="form-group">
                            <label><g:message code="app.label.quality.monitoringType"/></label>
                            <div>
                                <g:textField name="dataType" value = "${params.type}" readonly = "true" disabled="disabled" class="form-control" required="required"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <label><g:message code="app.label.quality.fieldName"/></label>

                            <div><g:textField name="fieldNameLabel" disabled="disabled" class="form-control"
                                              required="required"/><input type="hidden" id="fieldName" name="fieldName" /></div>
                        </div>

                        <div class="form-group">
                            <label><g:message code="app.label.quality.fieldLocation"/></label>

                            <div><g:textField name="fieldLocationLabel" disabled="disabled" class="form-control"
                                              required="required" value=""/><input type="hidden" id="fieldLocation" name="fieldLocation" /></div>
                        </div>

                    </div>
                </div>

                <div class="row form-group">
                    <div class="form-group" style="padding-left: 20px">
                        <label style="padding: 5px;"><g:message code="app.label.quality.issueDescription"/></label>
                        <div>
                            <g:select name="errorCommentSelect"
                                      from="['', message(code: 'app.label.quality.errorComment1'),
                                             message(code: 'app.label.quality.errorComment2'),
                                             message(code: 'app.label.quality.errorComment3')]"
                                      class="form-control"/>
                        </div>
                        <div><textArea id="errorTypeTextArea" name="errorTypeTextArea" rows="6" cols="60"  maxlength="255" class="form-control"></textArea></div>
                    </div>
                </div>
                <div class="bs-callout bs-callout-info">
                    <h5><g:message code="app.label.note" /> : <g:message code="app.pvq.qualityIssueDescription.validation.note" /></h5>
                </div>
            </div>
            <div class="modal-footer">
                <div class="buttons creationButtons col-md-12">
                    <g:if test="${isFinalState}">
                        <button class="btn primaryButton btn-primary qualityIssueSaveDisabled" name="submit" disabled="disabled" title="<g:message code="app.label.quality.case.final.state"/>"
                        >${message(code: 'default.button.save.label')}</button>
                    </g:if>
                    <g:else>
                        <button class="btn primaryButton btn-primary qualityIssueSave" name="submit"
                        >${message(code: 'default.button.save.label')}</button>
                    </g:else>
                    <button type="button" class="btn btn-default"  data-dismiss="modal">
                        <g:message code="default.button.cancel.label"/>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<g:render template="includes/errorExport"/>
<g:render template="includes/createIssue"/>
<g:render template="includes/ignoreConfirmationModal" />
<g:render template="/query/workflowStatusJustification" model="[tableId:'issuesTable']"/>
<g:render template="includes/viewQualityDelayReasonModal" />
<g:form name="attachForm" method="post">
    <input type="hidden" name="selectAll" id="selectAll1">
    <input type="hidden" name="selectedIds" id="selectedIds">
    <input type="hidden" name="capaInstanceId" id="capaInstanceId">
</g:form>
<form style="display: none" id="excelExport" method="post" action="${createLink(controller: 'quality', action: 'exportToExcelCaseForm')}?caseNumber=${params.caseNumber}&versionNumber=${params.versionNumber}&type=${params.type}">
    <input name="data" id="excelData">
</form>
</body>
</html>