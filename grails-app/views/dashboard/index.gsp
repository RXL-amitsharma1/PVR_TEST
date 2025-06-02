<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.config.ReportTemplate; com.rxlogix.config.Dashboard; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DashboardEnum; com.rxlogix.enums.NotificationApp; com.rxlogix.enums.WidgetTypeEnum; com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.enums.ReportFormatEnum; com.rxlogix.config.SourceProfile; com.rxlogix.ChartOptionsUtils" contentType="text/html;charset=UTF-8" %>
<head>
    <meta name="layout" content="main"/>
    <title>${title}</title>

    <g:set var="userService" bean="userService"/>
    <g:set var="isEditable" value="${dashboard.isEditableBy(userService.getCurrentUser())}"/>
    <asset:stylesheet src="vendorUi/gridstack/gridstack.min.css"/>
    <asset:stylesheet src="list/list.css"/>
    <asset:stylesheet src="dashboard/dashboard.css"/>
    <asset:stylesheet src="dashboardpvr.css"/>
    <asset:stylesheet src="vendorUi/fullcalendar/fullcalendar.min.css"/>
    <asset:stylesheet src="vendorUi/fullcalendar/fullcalendar.print.css" media="print"/>
    <asset:stylesheet src="rowGroup.dataTables.min.css"/>
    <style>
    .chart-container {
        position: relative;
    }

    #chartConfigurationModal i.fa {
        cursor: pointer;
    }

    div.templateQuery-div:hover {
        background-color: #FFFFFF;
    }
    #rxTableConfiguration_wrapper > .dt-layout-row:first-child {
        margin-top:0px;
        padding-right:0px;
    }

    #dataAnalysisTable_wrapper > .dt-layout-row:first-child {
        margin-top:0px;
        padding-right:0px;
    }
    .rx-widget {
        .dt-layout-row:first-child {
            margin: 10px 0 0 0;
        }

    }
    </style>
    <g:javascript>
        var actionItemHostPage = "dashboard";
        var spotfireFilesListUrl = "${createLink(controller: 'dataAnalysis', action: 'list')}";
        var indexReportUrl = "${createLink(controller: 'reportResultRest', action: 'latestAdhocReport')}";
        var periodicReportUrl = "${createLink(controller: 'periodicReportConfigurationRest', action: 'latestPeriodicReport')}";
        var controllerName = '${ViewHelper.isPvqModule(request) ? "quality" : "dashboard"}';
        var actionItemUrl = "${createLink(controller: 'actionItemRest', action: (ViewHelper.isPvqModule(request) ? 'indexPvq' : 'index'))}";
        var showReportUrl = "${createLink(controller: 'report', action: 'criteria')}";
        var updateStatusUrl = "${createLink(controller: 'report', action: 'updateStatus')}";
        var deleteReport = "${createLink(controller: 'report', action: 'deleteReport', params: [relatedPage: related])}";
        var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest', action: 'index')}";
        var getSharedWith = "${createLink(controller: 'reportResultRest', action: 'getSharedWithUsers')}";
        var getEmailTo = "${createLink(controller: 'reportResultRest', action: 'getEmailToUsers')}";
        var targetStatesAndApplicationsUrl= "${createLink(controller: "periodicReport", action: "targetStatesAndApplications")}";
        var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest', action: 'save')}";
        var addEmailConfiguration="${createLink(controller: "report", action: "addEmailConfiguration")}";
        var markAsSubmittedUrl="${createLink(controller: "reportSubmission", action: "loadReportSubmissionForm")}";
        var hasDmsIntegration = false;
        var dmsFoldersUrl="${createLink(controller: 'periodicReport', action: 'getDmsFolders')}";
        var addDmsConfiguration="${createLink(controller: "report", action: "addDmsConfiguration")}";
        var reportingDestinationsUrl="${createLink(controller: 'queryRest', action: 'getReportingDestinations')}";
        var reportSubmitUrl="${createLink(controller: "reportSubmission", action: "submitReport")}${_csrf ? ("?" + _csrf?.parameterName + "=" + _csrf?.token) : ""}";
        var periodicReportConfig = {
            generateDraftUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "generateDraft")}",
            markAsSubmittedUrl: "${createLink(controller: "reportSubmission", action: "loadReportSubmissionForm")}",
            reportSubmitUrl: "${createLink(controller: "reportSubmission", action: "submitReport")}${_csrf ? ("?" + _csrf?.parameterName + "=" + _csrf?.token) : ""}",
            viewCasesUrl: "${createLink(controller: "caseList", action: "index")}",
            reportsListUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "reportsList")}",
            targetStatesAndApplicationsUrl: "${createLink(controller: "periodicReport", action: "targetStatesAndApplications")}",
            updateReportStateUrl: "${createLink(controller: "periodicReport", action: "updateReportState")}",
            reportViewUrl: "${createLink(controller: "pvp", action: "sections")}",
            configurationViewUrl: "${createLink(controller: "periodicReport", action: "viewExecutedConfig")}",
            reportingDestinationsUrl: "${createLink(controller: 'queryRest', action: 'getReportingDestinations')}",
            stateListUrl: "${createLink(controller: 'workflowJustificationRest', action: 'getStateListAdhoc')}",
            downloadPublisherFileURL: "${createLink(controller: 'pvp', action: 'downloadPublisherReport', absolute: true)}",
        }
        var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
        var executedCaseSeriesShowURL = "${createLink(controller: 'executedCaseSeries', action: 'show')}";
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
        var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'index')}";
        var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";

        // Calendar specific URLs
        var eventsUrl = "${createLink(controller: "calendar", action: "events")}";
        var createReportRequestUrl = "${createLink(controller: 'reportRequest', action: 'create')}";
        var deleteActionItemUrl = "${createLink(controller: 'actionItem', action: 'delete')}";
        var reportRequestShowURL = "${createLink(controller: 'reportRequest', action: 'show')}";
        var executedReportShowURL = "${createLink(controller: 'report', action: 'showFirstSection')}";
        var adhocReportShowURL = "${createLink(controller: 'configuration', action: 'view')}";
        var periodicReportShowURL = "${createLink(controller: 'periodicReport', action: 'view')}";
        var icsrReportShowURL = "${createLink(controller: "icsrReport", action: "view")}";

        var adhocReportsSummaryUrl="${createLink(controller: 'dashboard', action: 'getAdhocSummary')}";
        var aggregateReportsSummaryUrl="${createLink(controller: 'dashboard', action: 'getAggregateSummary')}";
        var actionItemSummaryUrl="${createLink(controller: 'dashboard', action: 'getActionItemSummary')}";
        var advancedReportRequestUrl="${createLink(controller: 'dashboard', action: 'getAdvancedReportRequest')}";
        var reportRequestSummaryUrl="${createLink(controller: 'dashboard', action: 'getReportRequestSummary')}";
        var etlUrl="${createLink(controller: 'dashboard', action: 'etl')}";
        var configurationsListUrl = "${createLink(controller: 'configurationRest', action: 'index', params: [mixedTypes: true, showSections: false])}";
        var toFavorite = "${createLink(controller: 'configuration', action: 'favorite')}";
        var isPriorityRoleEnable = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_RUN_PRIORITY_RPT")};
        var LINKS = {
            toPDF : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.PDF])}",
            toExcel : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.XLSX])}",
            toWord : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.DOCX])}",
            toPowerPoint : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.PPTX])}",
            toShare : "${createLink(controller: 'report', action: 'share')}",
            toEmail : "${createLink(controller: 'report', action: 'email')}"
        };
        var AGG_CONFIGURATION = {
             deleteUrl: "${createLink(controller: 'periodicReport', action: 'delete')}",
             editUrl: "${createLink(controller: 'periodicReport', action: 'edit')}",
             viewUrl: "${createLink(controller: 'periodicReport', action: 'view')}",
             copyUrl: "${createLink(controller: 'periodicReport', action: 'copy')}",
             runUrl:"${createLink(controller: 'periodicReport', action: 'runOnce')}"
        }
        var CONFIGURATION = {
             listUrl: "${createLink(controller: 'configurationRest', action: 'index', params: [mixedTypes: true, showSections: true])}",
             addWidgetUrl:"${createLink(controller: ViewHelper.isPvqModule(request) ? 'quality' : 'dashboard', action: 'addReportWidget')}",
             removeWidgetUrl: "${createLink(controller: 'dashboard', action: 'removeReportWidgetAjax')}",
             refreshWidgetUrl: "${createLink(controller: 'dashboard', action: 'refreshReportWidgetAjax')}",
             updateWidgetsUrl: "${createLink(controller: 'dashboard', action: 'updateReportWidgetsAjax')}",
             getChartDataUrl: "${createLink(controller: 'dashboard', action: 'getChartWidgetDataAjax')}",
             templateTypes: "${TemplateTypeEnum.DATA_TAB.key}, ${TemplateTypeEnum.NON_CASE.key}",
             deleteUrl: "${createLink(controller: 'configuration', action: 'delete')}",
             editUrl: "${createLink(controller: 'configuration', action: 'edit')}",
             viewUrl: "${createLink(controller: 'configuration', action: 'view')}",
             copyUrl: "${createLink(controller: 'configuration', action: 'copy')}",
             runUrl:"${createLink(controller: 'configuration', action: 'runOnce')}",
        };

        var adhocExecutedIndexPage="${createLink(controller: 'report', action: 'index')}";
        var aggregateExecutedIndexPage="${createLink(controller: 'periodicReport', action: 'reports')}";
        var actonItemIndexPage="${createLink(controller: 'actionItem', action: 'index')}";
        var reportRequestIndexPage="${createLink(controller: 'reportRequest', action: 'index')}";
        var qualityCaseCountUrl="${createLink(controller: 'quality', action: 'ajaxCaseCount')}";
        var qualityCaseDataCountUrl="${createLink(controller: 'quality', action: 'ajaxCaseDataCount')}";
        var qualitySubmissionCountUrl="${createLink(controller: 'quality', action: 'ajaxSubmissionCount')}";
        var productCountUrl="${createLink(controller: 'quality', action: 'ajaxProductsCount')}";
        var entrySiteCountUrl="${createLink(controller: 'quality', action: 'ajaxEntrySiteCount')}";
        var errorCountUrl="${createLink(controller: 'quality', action: 'ajaxTop20ErrorsCount')}";
        var latestQualityIssuesUrl="${createLink(controller: 'quality', action: 'ajaxLatestQualityIssuesUrl')}";
        var caseReportTypeCountUrl="${createLink(controller: 'quality', action: 'ajaxCaseReportTypeCount')}";
        var updateLabelUrl="${createLink(action: 'updateLabel')}";
        var showPvpReportUrl = "${createLink(controller: 'pvp', action: 'sections')}";
        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}?isQueryTargetReports=true";
        var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action: 'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var reportConfigurationUrl = "${createLink(controller: 'dashboard', action: 'reportConfiguration')}";
        var updateSectionAndRunAjaxUrl = "${createLink(controller: 'configuration', action: 'updateSectionAndRunAjax')}";
        var importExcel="${createLink(controller: 'configuration', action: 'importExcel')}";
        var validateValue="${createLink(controller: 'configuration', action: 'validateValue')}";
        var delimiter = null;
        if (controllerName ==='quality') {
            document.title = 'PV Quality - Dashboard';
        }
        var isEditable=${isEditable}
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";
        var poiInputsForTemplateUrl = "${createLink(controller: 'template', action: 'poiInputsForTemplate')}";
        var cioms1Id = "${com.rxlogix.config.ReportTemplate.cioms1Id()}";
        var medWatchId = "${ReportTemplate.medWatchId()}";
        var baseUrlAdhoc = "${createLink(controller: 'configuration', action: 'view')}";
        var baseUrlAggregate = "${createLink(controller: 'periodicReport', action: 'view')}";
    </g:javascript>
    <g:showIfDmsServiceActive>
        <asset:javascript src="app/configuration/dmsConfiguration.js"/>
        <g:javascript>
            hasDmsIntegration = true;
        </g:javascript>
    </g:showIfDmsServiceActive>
</head>

<body>

<input type="hidden" id="dashboardId" value="${dashboard.id}">
<!-- Page-Title -->
<div class="row">
    <div class="col-sm-12">
        <div class="page-title-box">
            <div class="fixed-page-head" style="z-index: 1000">
                <g:if test="${isEditable}">
                    <div class="page-head-lt">
                        <h5 class="page-title inline-b mt-5" contenteditable="true"
                            maxlength="${Dashboard.constrainedProperties.label.maxSize}"
                            style="min-width: 100px; max-width: 100%">${dashboard.label}</h5>
                    </div>

                    <div class="page-head-rt">
                        <div class="pull-right pt-0">
                            <g:if test="${dashboard.dashboardType in [DashboardEnum.PVR_USER, DashboardEnum.PVQ_USER, DashboardEnum.PVC_USER]}">
                                <a href="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="dashboard"
                                   data-action="removeDashboard" style="color: #000000"
                                   data-instanceid="${dashboard.id}" data-instancename="${dashboard.label}"
                                   title="${message(code: 'default.button.remove.label')}"><i
                                        class="md md-trash-can md-lg pv-ic"></i>
                                </a>
                                <a href="javascript:void(0)" data-toggle='modal' data-target='#dashboardModal'
                                   data-id="${dashboard.id}" class="dashboard-edit" style="color: #000000"
                                   title="${message(code: 'default.button.settings')}"><i
                                        class="md md-cogs md-lg pv-ic"></i>
                                </a>
                            </g:if>
                            <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                                <g:if test="${dashboard.dashboardType in [DashboardEnum.PVC_PUBLIC, DashboardEnum.PVR_PUBLIC, DashboardEnum.PVQ_PUBLIC]}">
                                    <a href="#" data-toggle="modal" data-target="#deleteModal"
                                       data-instancetype="dashboard" data-action="removeDashboard"
                                       style="color: #000000"
                                       data-instanceid="${dashboard.id}" data-instancename="${dashboard.label}"
                                       title="${message(code: 'default.button.remove.label')}"><i
                                            class="md md-trash-can md-lg pv-ic"></i>
                                    </a>
                                    <a href="javascript:void(0)" data-toggle='modal' data-target='#dashboardModal'
                                       data-id="${dashboard.id}" class="dashboard-edit" style="color: #000000"
                                       title="${message(code: 'default.button.settings')}">
                                        <i class="md md-settings md-lg pv-ic"></i>
                                    </a>
                                </g:if>
                            </sec:ifAnyGranted>



                            <g:if test="${ViewHelper.isPvqModule(request)}">
                                <!--<div class="btn-group dropdown" style="margin-right: 15px;margin-top:2px">
                                    <a class="btn btn-success btn-xs excelWidgetExport" name="excel" href="javascript:void(0)" ><g:message
                                    code="quality.shareDropDown.excexcel.menu"/></a>
                                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">
                                            <span class="caret"></span>
                                            <span class="sr-only"></span>
                                        </button>
                                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">
                                            <li role="presentation"><a role="menuitem" href="" data-toggle="modal" name="Email" data-target="#emailToModal">
                                <g:message code="app.reportFormat.PDF"/></a></li>
                                            <li role="presentation"><a id="caseNumCopyBt" href="" role="menuitem" data-toggle="modal" data-target="#copyCaseNumModal">
                                <g:message code="app.reportFormat.DOCX"/></a></li>
                                            <li role="presentation"><a id="caseNumCopyBtns" href="" role="menuitem" data-toggle="modal" data-target="#copyCaseNumModal">
                                <g:message code="app.reportFormat.PPTX"/></a></li>
                                        </ul>
                                    </div>-->
                            </g:if>
                            <a data-toggle="dropdown"><span class="dropdown-toggle " data-toggle="tooltip"
                                                            data-placement="bottom"
                                                            title="${message(code: 'app.label.dashboard.add.widget')}"
                                                            id="mainDropdownMenu" style="cursor: pointer">
                                <i class="md md-table-plus md-lg pv-ic"></i></span></a>

                            <div class="dropdown-menu rx-widget-menu" aria-labelledby="mainDropdownMenu"
                                 style="margin-top: 0px;">
                                <ul class="rx-widget-menu-content" style="width: auto;">
                                    <g:if test="${ViewHelper.isPvqModule(request)}">
                                        <sec:ifAnyGranted
                                                roles="ROLE_PERIODIC_CONFIGURATION_VIEW,ROLE_CONFIGURATION_VIEW">
                                            <li data-toggle="modal" data-target="#addWidgetModal">
                                                <div><i class="fa fa-bar-chart-o"></i> ${message(code: 'app.label.reports')}
                                                </div>
                                            </li>
                                        </sec:ifAnyGranted>
                                        <li data-url="addWidgetUrl"
                                            data-params='{"widgetType": "${WidgetTypeEnum.QUALITY_ACTION_ITEMS.name()}"}'>
                                            <div><i class="fa fa-bell"></i> ${message(code: 'default.button.addactionItemsWidget.label')}
                                            </div>
                                        </li>
                                        <li data-url="addWidgetUrl"
                                            data-params='{"widgetType": "${WidgetTypeEnum.QUALITY_ACTION_ITEMS_SUMMARY.name()}"}'>
                                            <div><i class="fa fa-info-circle"></i> ${message(code: 'app.widget.button.actionItem.label')}
                                            </div>
                                        </li>
                                        <li data-url="addWidgetUrl"
                                            data-params='{"widgetType": "${WidgetTypeEnum.QUALITY_CASE_COUNT.name()}"}'>
                                            <div><i class="fa fa-info-circle"></i> ${message(code: 'app.widget.button.quality.caseCount.label')}
                                            </div>
                                        </li>
                                        <li data-url="addWidgetUrl"
                                            data-params='{"widgetType": "${WidgetTypeEnum.QUALITY_PRODUCT_COUNT.name()}"}'>
                                            <div><i class="fa fa-info-circle"></i> ${message(code: 'app.widget.button.quality.productCount.label')}
                                            </div>
                                        </li>
                                        <li data-url="addWidgetUrl"
                                            data-params='{"widgetType": "${WidgetTypeEnum.QUALITY_ENTRYSITE_COUNT.name()}"}'>
                                            <div><i class="fa fa-info-circle"></i> ${message(code: 'app.widget.button.quality.entrySiteCount.label')}
                                            </div>
                                        </li>
                                        <li data-url="addWidgetUrl"
                                            data-params='{"widgetType": "${WidgetTypeEnum.QUALITY_ERROR_COUNT.name()}"}'>
                                            <div><i class="fa fa-info-circle"></i> ${message(code: 'app.widget.button.quality.top20ErrorsCount.label')}
                                            </div>
                                        </li>
                                        <li data-url="addWidgetUrl"
                                            data-params='{"widgetType": "${WidgetTypeEnum.QUALITY_LATEST_ISSUES.name()}"}'>
                                            <div><i class="fa fa-info-circle"></i> ${message(code: 'app.widget.button.quality.latestIssues.label')}
                                            </div>
                                        </li>
                                        <li data-url="addWidgetUrl"
                                            data-params='{"widgetType": "${WidgetTypeEnum.QUALITY_CASE_REPORT_TYPE.name()}"}'>
                                            <div><i class="fa fa-info-circle"></i> ${message(code: 'app.widget.button.quality.caseReportTypeCount.label')}
                                            </div>
                                        </li>
                                        <li data-url="addWidgetUrl"
                                            data-params='{"widgetType": "${WidgetTypeEnum.ACTION_PLAN_PVQ.name()}"}'>
                                            <div><i class="fa fa-info-circle"></i> ${message(code: 'app.actionPlan.actionPlan')}
                                            </div>
                                        </li>
                                    </g:if>
                                    <g:else>
                                        <sec:ifAnyGranted
                                                roles="ROLE_PERIODIC_CONFIGURATION_VIEW,ROLE_CONFIGURATION_VIEW">
                                            <li data-toggle="modal" data-target="#addWidgetModal">
                                                <div><i class="fa fa-bar-chart-o"></i> ${message(code: 'app.label.reports')}
                                                </div>
                                            </li>
                                            <li data-url="addWidgetUrl"
                                                data-params='{"widgetType": "${WidgetTypeEnum.LAST_REPORTS.name()}"}'>
                                                <div><i class="fa fa-table"></i> ${message(code: 'default.button.addLastReportsWidget.label')}
                                                </div>
                                            </li>

                                        </sec:ifAnyGranted>
                                        <sec:ifAnyGranted
                                                roles="ROLE_PERIODIC_CONFIGURATION_CRUD,ROLE_CONFIGURATION_CRUD">
                                            <li data-url="addWidgetUrl"
                                                data-params='{"widgetType": "${WidgetTypeEnum.CONFIGURATIONS.name()}"}'>
                                                <div><i class="md md-animation"></i> ${message(code: 'app.case.series.library.label')}
                                                </div>
                                            </li>

                                        </sec:ifAnyGranted>
                                        <sec:ifAnyGranted roles="ROLE_CONFIGURATION_VIEW">
                                            <li data-url="addWidgetUrl"
                                                data-params='{"widgetType": "${WidgetTypeEnum.ADHOC_REPORTS_SUMMARY.name()}"}'>
                                                <div><i class="md md-grid"></i> ${message(code: 'app.widget.button.adhoc.label')}
                                                </div>
                                            </li>
                                        </sec:ifAnyGranted>

                                        <sec:ifAnyGranted roles="ROLE_PERIODIC_CONFIGURATION_VIEW">
                                            <li data-url="addWidgetUrl"
                                                data-params='{"widgetType": "${WidgetTypeEnum.AGGREGATE_REPORTS_SUMMARY.name()}"}'>
                                                <div><i class="md md-call-merge"></i> ${message(code: 'app.widget.button.aggregate.label')}
                                                </div>
                                            </li>
                                        </sec:ifAnyGranted>

                                        <sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
                                            <li data-url="addWidgetUrl"
                                                data-params='{"widgetType": "${WidgetTypeEnum.ACTION_ITEMS.name()}"}'>
                                                <div><i class="fa fa-bell"></i> ${message(code: 'default.button.addactionItemsWidget.label')}
                                                </div>
                                            </li>
                                            <li data-url="addWidgetUrl"
                                                data-params='{"widgetType": "${WidgetTypeEnum.ACTION_ITEMS_SUMMARY.name()}"}'>
                                                <div><i class="fa fa-info-circle"></i> ${message(code: 'app.widget.button.actionItem.label')}
                                                </div>
                                            </li>
                                        </sec:ifAnyGranted>

                                        <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_VIEW">
                                            <li data-url="addWidgetUrl"
                                                data-params='{"widgetType": "${WidgetTypeEnum.REPORT_REQUEST_SUMMARY.name()}"}'>
                                                <div><i class="md md-clipboard-alert"></i> ${message(code: 'app.widget.button.reportRequest.label')}
                                                </div>
                                            </li>
                                            <li data-url="addWidgetUrl"
                                                data-params='{"widgetType": "${WidgetTypeEnum.ADVANCED_REPORT_REQUEST.name()}"}'>
                                                <div><i class="md md-clipboard-text"></i> ${message(code: 'app.widget.button.advancedReportRequest.label')}
                                                </div>
                                            </li>
                                        </sec:ifAnyGranted>

                                        <sec:ifAnyGranted roles="ROLE_CALENDAR">
                                            <li data-url="addWidgetUrl"
                                                data-params='{"widgetType": "${WidgetTypeEnum.CALENDAR.name()}"}'>
                                                <div><i class="fa fa-calendar"></i> ${message(code: 'default.button.addCalendarWidget.label')}
                                                </div>
                                            </li>
                                        </sec:ifAnyGranted>
                                        <g:if test="${!SourceProfile.findByIsCentral(true)?.sourceName?.equals("PVCM")}">
                                            <li data-url="addWidgetUrl"
                                                data-params='{"widgetType": "${WidgetTypeEnum.ETL.name()}"}'>
                                                <div><i class="fa fa-calendar-check-o"></i> ${message(code: 'app.widget.button.etl.label')}
                                                </div>
                                            </li>
                                        </g:if>
                                        <sec:ifAnyGranted roles="ROLE_PERIODIC_CONFIGURATION_CRUD">
                                            <rx:showPVPModule>
                                                <li data-url="addWidgetUrl"
                                                    data-params='{"widgetType": "${WidgetTypeEnum.ADVANCED_PUBLISHER.name()}"}'>
                                                    <div><i class="fa fa-tasks"></i> ${message(code: 'app.widget.button.advancedPublisher.label')}
                                                    </div>
                                                </li>
                                                <li data-url="addWidgetUrl"
                                                    data-params='{"widgetType": "${WidgetTypeEnum.COMPLIANCE_PUBLISHER.name()}"}'>
                                                    <div><i class="fa fa-area-chart"></i> ${message(code: 'app.widget.button.compliancePublisher.label')}
                                                    </div>
                                                </li>
                                            </rx:showPVPModule>
                                        </sec:ifAnyGranted>
                                        <sec:ifAnyGranted roles="ROLE_DATA_ANALYSIS">
                                            <li data-toggle="modal" data-target="#addDataAnalysisModal">
                                                <div><i class="fa fa-pie-chart"></i> ${message(code: 'app.label.dataAnalysis')}
                                                </div>
                                            </li>
                                        </sec:ifAnyGranted>
                                        <g:if test="${grailsApplication.config.getProperty('show.xml.option', Boolean) && grailsApplication.config.getProperty('icsr.profiles.execution', Boolean)}">
                                            <sec:ifAnyGranted roles="ROLE_ICSR_PROFILE_EDITOR,ROLE_ICSR_PROFILE_VIEWER">
                                                <li data-url="addWidgetUrl"
                                                    data-params='{"widgetType": "${WidgetTypeEnum.ICSR_TRACKING.name()}"}'>
                                                    <div><i class="md md-grid"></i> ${message(code: 'app.label.view.cases')}
                                                    </div>
                                                </li>
                                            </sec:ifAnyGranted>
                                        </g:if>
                                    </g:else>
                                    <g:if test="${ViewHelper.isPvcModule(request)}">
                                        <li data-url="addWidgetUrl"
                                            data-params='{"widgetType": "${WidgetTypeEnum.ACTION_PLAN_PVC.name()}"}'>
                                            <div><i class="fa fa-info-circle"></i> ${message(code: 'app.actionPlan.actionPlan')}
                                            </div>
                                        </li>
                                    </g:if>
                                </ul>
                            </div>
                        </div>
                    </div>
                </g:if>
                <g:else>
                    <div class="page-head-lt">
                        <h4 class="page-title">${dashboard.label}</h4>
                    </div>
                </g:else>
            </div>

            <div class="alert alert-danger alert-dismissible forceLineWrap errorDiv" role="alert" hidden="hidden"
                 style="margin-top: 3%">
                <p class="errorContent"></p>
            </div>

            <div class="alert alert-success alert-dismissible forceLineWrap successDiv" role="alert" hidden="hidden"
                 style="margin-top: 3%">
                <p class="successContent"><g:message code="app.label.saved"/></p>
            </div>
        </div>
    </div>
</div>

<div class="p-error"><g:render template="/includes/layout/flashErrorsDivs"/></div>
<div class="p-error"><g:render template="/includes/layout/inlineAlerts"/></div>

<div class="content">
    <div class="container pv-dashboard">
        <div id="rx-widgets">
            <div class="grid-stack">
                <g:each status="index" in="${widgets}" var="widget">
                    <g:render template="includes/widget" model="[index: index, widget: widget]"/>
                </g:each>
            </div>
        </div>
    </div>
</div>
<!-- end row -->
<form>
    <g:render template="/includes/widgets/deleteRecord"/>
</form>
<form style="display: none" id="excelExport" action="${createLink(controller: 'actionItem', action: 'exportToExcelForAI')}">
    <input name="singleActionItemId" id="excelData" >
</form>
<form style="display: none" id="excelExportForm" action="${createLink(controller: 'actionItem', action: 'exportToExcelForAI')}">
</form>
<div class="modal fade" id="exportUrlModal" data-backdrop="static" tabindex="-1" role="dialog"
     aria-labelledby="warningModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title">Direct URL to widget</h4>
            </div>

            <div class="modal-body">
                <input class="form-control exportUrlField" id="exportUrlField" style="width: 100%" readonly>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-evt-clk='{"method" : "copyToClipboard", "params" : ["#exportUrlField"]}'
                    data-dismiss="modal">Copy To Clipboard</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<g:render template="addWidgetModal" model="[:]"/>
<g:render template="includes/addDataAnalysisModal" model="[:]"/>
<g:render template="/actionItem/includes/actionItemModal" model="[]"/>
<g:render template="/includes/widgets/infoTemplate"
          model="[messageBody: message(code: 'app.widget.chart.isSceduled.message')]"/>
<g:render template="/query/workflowStatusJustification" model="[tableId: 'rxTableReports', isPeriodicReport: true]"/>

<asset:javascript src="vendorUi/fullcalendar/fullcalendar.min.js"/>
<asset:javascript src="vendorUi/fullcalendar/fullcalendar-lang-all.js"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highstock.js"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-more.js"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-3d.js"/>
<asset:javascript src="vendorUi/highcharts/modules/no-data-to-display.js"/>
<asset:javascript src="vendorUi/highcharts/modules/map.js"/>
<asset:javascript src="vendorUi/highcharts/modules/world.js"/>
<asset:javascript src="vendorUi/highcharts/modules/annotations.js"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/solid-gauge.js"/>
<asset:javascript src="vendorUi/highcharts/plugins/grouped-categories-1.3.2.js"/>
<asset:javascript src="vendorUi/counterup/jquery.counterup.min.js"/>
<asset:javascript src="app/dashboard/dashboard.js"/>
<asset:javascript src="app/dashboard/addWidgetModal.js"/>
<asset:javascript src="app/actionItem/actionItemModal.js"/>
<asset:javascript src="app/calendar.js"/>
<asset:javascript src="app/workFlow.js"/>
<asset:javascript src="app/report/advancedViewerChart.js"/>
<asset:javascript src="app/report/advancedViewer.js"/>
<asset:javascript src="app/actionPlan.js"/>
<asset:javascript src="app/configuration/configurationCommon.js"/>
<asset:javascript src="app/configuration/deliveryOption.js"/>
<asset:javascript src="app/commonGeneratedReportsActions.js"/>
<asset:javascript src="app/periodicReport.js"/>
<asset:javascript src="app/dataTablesActionButtons.js"/>
<asset:javascript src="app/utils/pvr-common-util.js"/>
<asset:javascript src="app/utils/pvr-filter-util.js"/>

<asset:javascript src="datatables/extendedDataTable.js"/>
<asset:stylesheet src="datatables/extendedDataTable.css"/>

<asset:javascript src="datatables/dataTables.columnResize.js"/>
<asset:stylesheet src="datatables/dataTables.columnResize.css"/>

<asset:javascript src="datatables/dataTables.fixedHeader.js"/>
<asset:stylesheet src="datatables/dataTables.fixedHeader.css"/>

<script>periodicReport.periodicReportList.initSpecialActions($(".sectionsActions"));</script>
</div>
<form style="display: none" id="exportFormId" method="post"
      action="${createLink(controller: 'dashboard', action: 'exportWidget')}">
    <input name="data" id="data">
</form>
<g:render template="/includes/layout/dashboardModal"/>
<g:render template="/dashboard/includes/chartWidgetRefreshModal"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/copyPasteModal"/>
<g:render template="/email/includes/copyPasteEmailModal"/>
<g:render template="/periodicReport/includes/submissionCapaModal"/>
<g:render template="/configuration/includes/emailConfiguration"/>
<div id="emailToModal" style="display: none"></div>

<div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"/></div>

<div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"/></div>

<div class="expression" hidden="hidden"><g:render template="/query/poiInputValue"/></div>
<asset:javascript src="app/addTemplateSection.js"/>
<g:render template="/advancedReportViewer/includes/addTemplateSection"/>
<asset:javascript src="app/query/queryValueSelect2.js"/>
<asset:javascript src="app/configuration/blankParameters.js"/>
<asset:javascript src="app/configuration/copyPasteValues.js"/>
<g:if test="${showVersionModal || (systemNotificationList?.size()>0)}">

    <div class="modal fade" id="versionNotificationModal" tabindex="-1" role="dialog">
        <div class="modal-dialog " role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title"><g:message code="app.label.systemNotification.systemNotifications"/></h4>
                </div>

                <div class="modal-body ">
                    <g:if test="${showVersionModal}">
                        <div class="panel panel-default">
                            <div class="panel-heading"><b><g:message code="app.label.whatsNewReminder.title"/></b></div>
                            <div class="panel-body" style="border: 1px solid #ccc; border-radius: 0 0 10px 10px;"><g:message code="app.label.whatsNewReminder.message"/>
                                <br>
                                <a href="${createLink(controller: 'localizationHelpMessage', action: 'readReleaseNotes')}" class="btn btn-primary "><g:message code="app.label.newRelease.view"/></a>
                            </div>
                        </div>

                    </g:if>
                    <g:if test="${systemNotificationList?.size() > 0}">
                        <g:each in="${systemNotificationList}" var="notification">
                            <div class="panel panel-default">
                                <div class="panel-heading"><b>${notification.title}</b></div>
                                <div class="panel-body" style="border: 1px solid #ccc; border-radius: 0 0 10px 10px;">${raw(notification.description)}

                                    <g:if test="${notification.details}">
                                        <br>
                                        <a href="${createLink(controller: 'localizationHelpMessage', action: 'viewSystemNotification')}?id=${notification.id}" class="btn btn-primary "><g:message code="app.label.systemNotification.viewDetails"/></a>
                                    </g:if>
                                </div>
                            </div>
                        </g:each>
                    </g:if>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-primary remindLater" data-dismiss="modal"><g:message
                            code="app.label.whatsNewReminder.later"/></button>
                    <button type="button" class="btn btn-primary  dontShow" data-dismiss="modal"><g:message
                            code="app.label.whatsNewReminder.dont"/></button>
                    <a href="${createLink(controller: 'localizationHelpMessage', action: 'readReleaseNotes')}"
                       class="btn btn-primary "><g:message code="app.label.whatsNewReminder.view"/></a>
                </div>
            </div>
        </div>
    </div>
    <script>
        $(function () {
            $("#versionNotificationModal").modal("show");
            $(document).on("click", ".dontShow", function () {
                $.ajax({
                    url: "${createLink(controller: 'localizationHelpMessage', action:'dontShow')}",
                    dataType: 'json'
                })
                    .fail(function (err) {
                        console.log(err);
                    });
            });

            $(document).on("click", ".remindLater", function () {
                $.ajax({
                    url: "${createLink(controller: 'localizationHelpMessage', action:'remindLater')}",
                    dataType: 'json'
                })
                    .fail(function (err) {
                        console.log(err);
                    });

            });
        });
    </script>
</g:if>
</body>
</html>