<%@ page import="com.rxlogix.config.WorkflowState;com.rxlogix.enums.WorkflowConfigurationTypeEnum; com.rxlogix.util.FilterUtil; com.rxlogix.enums.PeriodicReportTypeEnum;com.rxlogix.enums.PriorityEnum;" %>

<head>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/reportRequestList.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/workFlow.js"/>
    <g:javascript>
        var reportRequestListUrl = "${createLink(controller: 'reportRequestRest', action: 'index')}";
        var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest', action: 'reportRequest')}";
        var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest', action: 'saveReportRequest')}";
        var sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithFilterList', params: [clazz:'com.rxlogix.config.ReportRequest'])}";
        var sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
        var actionItemStatusForReportRequestUrl = "${createLink(controller: "reportRequest", action: "actionItemStatusForReportRequest")}";
        var isReportRequest = true;
        var finalStates = "${finalWorkflowStates}"
    </g:javascript>

    <meta name="layout" content="main"/>
    <title><g:message code="app.task.reportRequestList.title"/></title>
    <script>
        $(function () {
            //Initiate the datatable
            reportRequest.reportRequestList.init_report_request_table();
        })
    </script>
    <style>
    #reportRequestList_wrapper>.top>div{
        margin-top: -37px;
    }

    #statusFilter.form-control + .select2 {
        width: 20%;
    }
    #reportRequestList_wrapper .dt-layout-row:first-child .dt-layout-cell{
        width: 80%;
    }
    .select2-container {
        text-align: left;
    }
    </style>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${reportRequestInstance}" var="theInstance"/>

            <rx:container title="${message(code: "reportRequest.label")}"  filterButton="${true}" options="${true}">
    <div class="body">
        <div id="report-request-conainter" class="list pv-caselist">

                <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_CRUD">
                    <div class="pull-right" id="createActionItemObj" style="cursor: pointer; text-align: right; position: relative;">
                        <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["reportRequest", "create"]}'>
                            <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.report.request.create')}" style="color: #353d43;"></i>
                        </a>

                    </div>
                </sec:ifAnyGranted>



        <div class="topControls" style="float: right;text-align:right;display: none">

                <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_CRUD">
                    <form id="excelImportForm" name="excelImportForm" action="${createLink(controller: 'reportRequest', action: 'importExcel')}" enctype="multipart/form-data" method="post" style="float: left;">
                        <div class="input-group add-margin pull-right" style="float: left">
                            <input type="text" class="form-control" id="file_name" readonly>
                            <label class="input-group-btn">
                                <span class="btn btn-primary  inputbtn-height">
                                    <i class="fa fa-upload"></i>
                                    <input type="file" id="file_input" name="file" accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                           style="display: none;">
                                </span>
                            </label>
                        </div>

                    </form>
                        <form id="excelExportForm" action="${createLink(controller: 'reportRequest', action: 'exportToExcel')}" style="float: left; position: relative;">
                            <input name="tableFilter" id="tableFilter_ex" type="hidden">
                            <input name="searchString" id="searchString_ex" type="hidden">
                            <input name="statusFilter" id="statusFilter_ex" type="hidden">
                            <input name="advancedFilter" id="advancedFilter_ex" type="hidden">
                            <input name="sharedwith" id="sharedwith_ex" type="hidden">
                            <button class="btn btn-primary export" style="height: 25px"><i class="fa fa-download" title="${message(code:'app.label.exportTo')} ${message(code:'app.reportFormat.XLSX')}"></i></button>
                        </form>

            </sec:ifAnyGranted>
            <select class="assignedToFilterControl form-control"  id="assignedToFilterControl" data-placeholder="${message(code:'app.label.action.item.assigned.to')}" name="assignedToFilterControl" value="" style="text-align:left;width: 100px;float: left;margin-right: 5px !important;margin-left: 5px !important;"></select>
                <g:select id="statusFilter" name="configurationTypeEnum" placeholder="${message(code:'app.label.report.request.status')}"
                          from="${WorkflowState.getAllWorkFlowStatesForType(WorkflowConfigurationTypeEnum.REPORT_REQUEST).sort { it.name }}"
                          optionValue="name" optionKey="id"
                          noSelection="${['': '']}" class="form-control" style="text-align:left;min-width: 150px;"></g:select>

        </div>
            <div class="pv-caselist">
                <table id="reportRequestList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                    <tr>
                        <th><g:message code="app.label.report.request.id"/></th>
                        <th style="min-width: 250px"><g:message code="app.label.report.request.name"/></th>
                        <th><g:message default="Assigned To" code="app.label.action.item.assigned.to"/></th>
                        <th style="min-width: 250px"><g:message default="Description" code="app.label.action.item.description"/></th>
                        <th><g:message default="Due Date" code="app.label.action.item.due.date"/></th>
                        <th><g:message default="Priority" code="app.label.action.item.priority"/></th>
                        <th><g:message default="Status" code="app.label.action.item.status"/></th>
                        <th><g:message default="Request Date" code="app.widget.button.ReportRequestDate.label"/></th>
                        <th><g:message default="Request Type" code="app.widget.button.ReportRequestType.label"/></th>
                        <th><g:message default="Action" code="app.label.action.item.action"/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
<g:render template="/query/workflowStatusJustification" model="[tableId: 'reportRequestList', isPeriodicReport: false]"/>

        </div>
    </div>
</div>
<g:render template="/includes/widgets/warningTemplate"/>
</body>
