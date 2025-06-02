<%@ page import="com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.ResponsibleParty; com.rxlogix.enums.ReasonOfDelayAppEnum; com.rxlogix.util.ViewHelper; com.rxlogix.enums.PvqTypeEnum; groovy.json.JsonOutput" %>
<style>
.dataTables_scrollHeadInner .topheader{
    border-left: 1px solid #eee;
    border-right: 1px solid #eee;
    text-align: center;
    height: 20px !important;
}
.dataTables_scrollBody tr th.sorting_desc:after {
    content: "" !important;
}
.dataTables_scrollBody tr th.sorting_asc:after {
    content: "" !important;
}
.rxmain-container-content{
    padding: 0 !important;
}
.actionPlanStickyColumn{
    position: -webkit-sticky !important;
    position: sticky !important;
    left: 0px;
    background-color: #FFFFFF;
    z-index: 100;
}
.pv-caselist #actionPlatTable_wrapper .dt-layout-table .table > tbody > tr > td.danger {
    background: rgba(239, 83, 80, 0.15) !important;
}

.pv-caselist #actionPlatTable_wrapper .dt-layout-table .table > tbody > tr > td.success {
    background: rgba(0, 177, 157, 0.15) !important;
}
#actionPlatTable_wrapper .dt-layout-table .topheader {
    border-left: 1px solid #eee;
    border-right: 1px solid #eee;
    text-align: center;
    height: 20px !important;
}
</style>
<g:set var="qualityService" bean="qualityService"/>
<g:set var="reportExecutorService" bean="reportExecutorService"/>
<g:render template="/includes/widgets/errorTemplate"/>
<div style="width: 100%; background: #eeeeee;padding: 10px" class="actionPlanWidgetSearchForm">
    <form id="criteriaForm" data-url="${createLink(controller: "${module}", action: "exportActionPlanToExcel")}">
        <i class="md md-export md-lg blue-1 font-22 lh-1" id="exportToExcel" data-tooltip="tooltip" data-placement="bottom" title="Export To Excel" style="position: absolute;right: 15px;margin-top: -32px;cursor: pointer"></i>
        <div class="alert alert-danger alert-dismissible forceLineWrap errorDiv" role="alert" hidden="hidden">
            <button type="button" class="close" name="latestQualityIssuesCloseButton${index}">
                <span aria-hidden="true">&times;</span>
                <span class="sr-only"><g:message code="default.button.close.label"/></span>
            </button>
            <p class="errorContent"></p>
        </div>
        <div class="alert alert-success alert-dismissible forceLineWrap successDiv"  role="alert" hidden="hidden">
            <button type="button" class="close" data-dismiss="alert">
                <span aria-hidden="true">&times;</span>
                <span class="sr-only"><g:message code="default.button.close.label"/></span>
            </button>
            <p ><g:message code="app.label.saved"/></p>
        </div>
        <div class="row">
            <div class="col-md-3">
                    <label><g:message default="Detection Date Range" code="app.actionPlan.DueDateRange"/>:</label>
                    <g:select id="dateRangeType" name="dateRangeType"
                              from="${ViewHelper.getDateRangeFoActionPlan()}"
                              optionKey="id"
                              optionValue="display"
                              value="${com.rxlogix.enums.DateRangeEnum.LAST_X_MONTHS}"
                              class="form-control select2-box"/>
            </div>
            <div class="col-md-2">
                <div class="row">
                    <div class="col-sm-6">
                        <label><g:message code="app.label.view.case.late"/>:</label>
                        <g:select id="lateFilter" name="lateFilter" multiple="true"
                                  from="${reportExecutorService.getLateListForOwnerApp(ReasonOfDelayAppEnum.PVC,false)}"
                                  optionKey="id"
                                  optionValue="textDesc"
                                  value=""
                                  class="form-control select2-box-break-long-word"/>
                    </div>
                    <div class="col-sm-6">
                        <label><g:message code="app.pvc.rootcause"/>:</label>
                        <g:select id="rcFilter" name="rcFilter" multiple="true"
                                  from="${reportExecutorService.getRootCauseList(ReasonOfDelayAppEnum.PVC)}"
                                  optionKey="id"
                                  optionValue="textDesc"
                                  value=""
                                  class="form-control select2-box-break-long-word"/>
                    </div>
                </div>
            </div>
            <div class="col-md-2">
                <label><g:message code="app.pvc.RootCauseClass"/>:</label>
                <g:select id="classFilter" name="classFilter" multiple="true"
                          from="${reportExecutorService.getRootCauseClassList()}"
                          optionKey="id"
                          optionValue="textDesc"
                          value=""
                          class="form-control select2-box-break-long-word"/>
            </div>
            <div class="col-md-2">
                <label><g:message code="app.pvc.import.destination"/>:</label>
                <g:select id="destinationFilter" name="destinationFilter" multiple="true"
                          from="${qualityService.getAgenciesNames()}"
                          optionKey="name"
                          optionValue="name"
                          value=""
                          class="form-control select2-box-break-long-word"/>
            </div>
            <div class="col-md-2">
                <label><g:message code="app.actionPlan.GroupBy" default="Group By"/>:</label>

                    <select id="groupBy" name="groupBy" class="form-control select2-box ">
                        <option value="responsible_rc" selected><g:message code="app.actionPlan.groupping.responsible_rc"/></option>
                        <option value="no_grouping"><g:message code="app.actionPlan.groupping.no_grouping"/></option>
                        <option value="rc_responsible"><g:message code="app.actionPlan.groupping.rc_responsible"/></option>
                        <option value="rc_destination"><g:message code="app.actionPlan.groupping.rc_destination"/></option>
                    </select>
            </div>
            <div class="col-md-1">
                <label><g:message code="app.actionPlan.showTop"/></label>
                <input id="topValues" name="topValues" value="5" type="number" class="form-control" min="1" max="999">
            </div>
        </div>
        <div class="row">
            <div class="col-md-3">
            <div class="row fuelux ">
                <label id="dateRangeTypeLabel"></label>
                <input name="lastX" id="lastX" value="1" type="number" min="1" max="366" required class="form-control">

                <div class=" customDateRange" style="display: none">
                    <div class="row">
                        <div class="col-sm-6">
                            <g:message code="app.dateFilter.from"/><span class="required-indicator">*</span>
                            <div class="datepicker input-group">
                                <input id="dateRangeFrom" name="dateRangeFrom"
                                       value="${new Date().minus(30).format(DateUtil.DATEPICKER_FORMAT)}"
                                       class="form-control"/>
                                <g:render template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                        <div class="col-sm-6">
                            <g:message code="app.dateFilter.to"/><span class="required-indicator">*</span>
                            <div class="datepicker input-group">
                                <input id="dateRangeTo" name="dateRangeTo"
                                       value="${new Date().format(DateUtil.DATEPICKER_FORMAT)}"
                                       class="form-control"/>
                                <g:render template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
            </div>
            <div class="col-md-2">
                <label><g:message code="app.pvc.RootCauseSubCategory"/>:</label>
                <g:select id="subFilter" name="subFilter" multiple="true"
                          from="${reportExecutorService.getRootCauseSubCategoryList()}"
                          optionKey="id"
                          optionValue="textDesc"
                          value=""
                          class="form-control select2-box-break-long-word"/>
            </div>
            <div class="col-md-2">
                <label><g:message code="quality.capa.responsibleParty.label"/>:</label>
                <g:select id="responsiblePartyFilter" name="responsiblePartyFilter" multiple="true"
                          from="${qualityService.getResponsiblePartyList(ReasonOfDelayAppEnum.PVC)}"
                          optionKey="id"
                          optionValue="textDesc"
                          value=""
                          class="form-control select2-box-break-long-word"/>
            </div>
            <div class="col-md-2">
                <label><g:message code="app.label.workflow.appName"/>:</label>
                <g:select id="workflowFilter" name="workflowFilter"  multiple="true"
                          from="${qualityService.listWorkflowStates(ReasonOfDelayAppEnum.PVC) }"
                          optionKey="value"
                          optionValue="label"
                          value=""
                          class="form-control select2-box-break-long-word"/>
            </div>
            <div class="col-md-2">
                <label><g:message code="app.actionPlan.primaryOnly"/></label>
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="primaryOnly"/>
                    <label for="primaryOnly">

                    </label>
                </div>
            </div>
            <div class="col-md-1">
                <label><g:message code="app.actionPlan.previousPeriods"/></label>
                <input id="periodsNumber" name="periodsNumber" value="2" type="number" class="form-control" min="1" max="5">

            </div>
        </div>
        <div style="width: 100%; margin-top: 10px; text-align: right"  class="notForWidget">
            <button type="button" id="applyFilter" class="btn btn-primary"><g:message code="app.pvc.import.Apply"/></button>
%{--            <button type="button" id="exportToExcel" class="btn btn-primary"><g:message code="quality.export.to.excel.menu"/></button>--}%
        </div>
        <div style="width: 100%; margin-top: 10px; display: none" class="forWidget">
            <g:if test="${isEditable}">
                <button type="button" class="btn btn-primary saveActionPlanWidget">${message(code: "default.button.save.label")}</button>
            </g:if>
            <button type="button" class="btn btn-primary actionPlanWidgetHideButton">${message(code: "app.label.hideOptions")}</button>
        </div>
    </form>
</div>

<div class="pv-caselist" style="padding: 10px">
    <table id="actionPlatTable" class="table table-striped pv-list-table dataTable no-footer" width="100%">
        <thead>
        <tr>
            <th rowspan="2" class="actionPlanStickyColumn" style="min-width: 80px"><g:message code="quality.capa.responsibleParty.label"/></th>
            <th rowspan="2" class="actionPlanStickyColumn" style="min-width: 80px"><g:message code="app.pvc.rootcause"/></th>
            <th rowspan="2" class="actionPlanStickyColumn" style="min-width: 80px"><g:message code="app.pvc.import.destination"/></th>
            <th colspan="6" class="topheader"><g:message code="app.actionPlan.LastPeriod"/><span class="lastLabel"></span></th>
            <th colspan="6" class="topheader topCloneAfter topToClone"><g:message code="app.actionPlan.PreviousPeriod"/><span class="previousLabel"></span></th>
            <th colspan="6" class="topheader clonnedHeader "><g:message code="app.actionPlan.PreviousPeriod"/><span class="previousLabel"></span></th>
            <th rowspan="2"><g:message code="app.label.action"/></th>
        </tr>
        <tr>
            <th><g:message code="app.actionPlan.NumberSubmissionReports"/></th>
            <th><g:message code="app.actionPlan.pod"/></th>
            <th><g:message code="app.actionPlan.porc"/></th>
            <th><g:message code="app.actionPlan.pcv"/></th>
            <th><g:message code="app.actionPlan.CorectPreventActions"/></th>
            <th><g:message code="app.actionPlan.cpp"/></th>
            <th class="toClone"><g:message code="app.actionPlan.NumberSubmissionReports"/></th>
            <th class="toClone"><g:message code="app.actionPlan.pod"/></th>
            <th class="toClone"><g:message code="app.actionPlan.porc"/></th>
            <th class="toClone"><g:message code="app.actionPlan.pcv"/></th>
            <th class="toClone"><g:message code="app.actionPlan.CorectPreventActions"/></th>
            <th class="toClone toCloneAfter"><g:message code="app.actionPlan.cpp"/></th>
            <th class="clonnedHeader"><g:message code="app.actionPlan.NumberSubmissionReports"/></th>
            <th class="clonnedHeader"><g:message code="app.actionPlan.pod"/></th>
            <th class="clonnedHeader"><g:message code="app.actionPlan.porc"/></th>
            <th class="clonnedHeader"><g:message code="app.actionPlan.pcv"/></th>
            <th class="clonnedHeader"><g:message code="app.actionPlan.CorectPreventActions"/></th>
            <th class="clonnedHeader"><g:message code="app.actionPlan.cpp"/></th>
        </tr>

        </thead>
        <tbody>
        </tbody>
    </table>
</div>

<g:javascript type="text/javascript">

    var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
    var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
    var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
    var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'indexByParentEntityKey')}";
    var listUrl = "${createLink(controller: 'central', action: 'actionPlanList')}";
    var caseListUrl = "${createLink(controller: 'central', action: 'actionPlanCaseList')}";
    var caseDataLinkUrl = "${createLink(controller: 'advancedReportViewer', action: 'caseForm')}";
    var actionPlanIndexLinkUrl = "${createLink(controller: 'actionPlanSummaryRest', action: 'index')}";
    var actionPlanSaveLinkUrl = "${createLink(controller: 'actionPlanSummaryRest', action: 'save')}";
    var actionPlanUpdateLinkUrl = "${createLink(controller: 'actionPlanSummaryRest', action: 'update')}";
    var actionPlanDeleteLinkUrl = "${createLink(controller: 'actionPlanSummaryRest', action: 'delete')}";
    var actionPlanViewLinkUrl = "${createLink(controller: 'actionPlanSummaryRest', action: 'summary')}";
    var actionPlanType = "PVC";
</g:javascript>

<form style="display: none" id="excelExport" method="post" action="${createLink(controller: 'central', action: 'exportToExcelActionPlan')}">
    <input name="data" id="excelData">
</form>