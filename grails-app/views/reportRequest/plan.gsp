<%@ page import="groovy.json.JsonOutput; com.rxlogix.config.ReportRequestField; com.rxlogix.config.WorkflowState;com.rxlogix.enums.WorkflowConfigurationTypeEnum; com.rxlogix.util.FilterUtil; com.rxlogix.enums.PeriodicReportTypeEnum;com.rxlogix.enums.PriorityEnum;" %>

<head>
    <asset:javascript src="jquery-ui/jquery-ui.min.js"/>
    <asset:javascript src="handlebar/handlebars-v4.0.11.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>

    <asset:javascript src="app/reportRequestPlanList.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="datatables/dataTables.treeGrid.js"/>
    <asset:javascript src="app/workFlow.js"/>
    <g:javascript>
        var reportRequestListUrl = "${createLink(controller: 'reportRequestRest', action: 'plan')}";
        var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest', action: 'reportRequest')}";
        var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest', action: 'saveReportRequest')}";
        var sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithFilterList', params: [clazz: 'com.rxlogix.config.ReportRequest'])}";
        var sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
        var actionItemStatusForReportRequestUrl = "${createLink(controller: "reportRequest", action: "actionItemStatusForReportRequest")}";
        var reportRedirectURL = "${createLink(controller: 'report', action: 'showFirstSection')}";
        var isReportRequest = true;
        var finalStates = "${finalWorkflowStates}"
    </g:javascript>

    <meta name="layout" content="main"/>
    <title>
        <g:if test="${params.pvp}"><g:message code="app.task.PvpreportRequestList.title"/></g:if>
        <g:else><g:message code="app.task.reportRequestList.title"/></g:else>
    </title>
    <script>
        var customFields = ${raw(JsonOutput.toJson(customFields))}
        $(function () {
            //Initiate the datatable
            reportRequest.reportRequestList.init_report_request_table();
        })
    </script>
    <style>
    tr.odd {
        background-color: #ffffff !important;
    }
    tr.odd.treegrid-inner-row,
    tr.even.treegrid-inner-row {
        background-color: #efefef !important;
        font-style: italic;
    }

    tr.odd.over, tr.even.over {
        background-color: lawngreen !important;
    }

    #reportRequestList_wrapper .dt-layout-row:first-child .dt-layout-cell {
        width: 80%;
        margin-top: -38px;
    }
    #statusFilter.form-control + .select2 {
        width: 20%;
    }
    .select2-container {
        text-align: left;
    }

    </style>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" bean="${reportRequestInstance}" var="theInstance"/>

<rx:container title="Aggregate Reports Planning" filterButton="true" options="true">
    <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_CRUD">
        <div class="pull-right" id="createActionItemObj" style="cursor: pointer; text-align: right; position: relative; margin-top: -38px; margin-right: 45px">
            <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["reportRequest", "create"]}'>
                <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.report.request.create')}" style="color: #353d43;"></i>
            </a>

        </div>
    </sec:ifAnyGranted>

    <div class="topControls" style="float: right;text-align: right;display: none">
        <g:select id="statusFilter" name="configurationTypeEnum"
                  from="${WorkflowState.getAllWorkFlowStatesForType(WorkflowConfigurationTypeEnum.REPORT_REQUEST).sort { it.name }}"
                  optionValue="name" optionKey="id"
                  noSelection="${['': '']}" class="form-control" style="min-width: 200px;margin-right:5px !important;float: right; text-align: left"/>
        <select class="assignedToFilterControl form-control" id="assignedToFilterControl" name="assignedToFilterControl"
                data-placeholder="${message(code:'app.label.action.item.assigned.to')}" value="" style="text-align:left;min-width: 100px;float: right"></select>
    </div>


            <div class="pv-caselist">
                <table id="reportRequestList" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                    <thead>
                    <tr style="background: #CCCCCC;">
                        <th></th>
                        <th>ID</th>
                        <th><g:message code="app.label.report.request.name"/></th>
                        <th><g:message default="INN" code="app.label.reportRequest.inn"/></th>
                        <th><g:message code="app.productDictionary.label"/></th>
                        <th><g:message code="app.label.reportingDestinations"/></th>
                        <th><g:message code="app.label.reportRequest.psrTypeFile"/></th>
                        <th><g:message code="app.label.reportRequest.masterPlanningRequest"/></th>
                        <th><g:message code="app.label.reportRequest.reportingPeriodStart"/></th>
                        <th><g:message code="app.label.reportRequest.reportingPeriodEnd"/></th>
                        <th><g:message code="app.label.reportRequest.dueDateToHa2"/></th>
                        <g:each in="${ReportRequestField.findAllByIsDeletedAndShowInPlan(false, true).sort { it.id }}" var="f">
                            <th>${f.label}</th>
                            <g:if test="${f.fieldType == ReportRequestField.Type.CASCADE}">
                                <th>${f.secondaryLabel}</th>
                            </g:if>
                        </g:each>
                        <th><g:message code="app.label.reportRequest.request.type"/></th>
                        <th><g:message code="app.label.reportRequest.drugCode"/></th>
                        <th><g:message default="Priority" code="app.label.action.item.priority"/></th>
                        <th><g:message code="app.report.request.dueDate.label"/></th>
                        <th><g:message code="app.label.reportRequest.occurrences"/></th>
                        <th><g:message code="app.label.reportRequest.reports"/></th>
                        <th><g:message code="app.label.report.request.request.by"/></th>
                        <th><g:message code="app.label.report.request.status"/></th>
                        <th class="col-min-60"><g:message code="app.label.action"/></th>
                    </tr>
                    </thead>
                </table>
            </div>

</rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
<g:render template="/query/workflowStatusJustification" model="[tableId: 'reportRequestList', isPeriodicReport: true]"/>

<g:render template="/includes/widgets/warningTemplate"/>
</body>
