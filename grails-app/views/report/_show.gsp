<%@ page import="com.rxlogix.config.ExecutedPeriodicReportConfiguration; grails.util.Holders;org.springframework.web.util.HtmlUtils;com.rxlogix.config.IcsrProfileConfiguration; com.rxlogix.config.ExecutedIcsrReportConfiguration; com.rxlogix.config.ExecutedIcsrProfileConfiguration; com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.config.IcsrReportConfiguration; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.enums.PeriodicReportTypeEnum; com.rxlogix.config.ExecutedCustomSQLTemplate; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.config.ExecutedCaseLineListingTemplate; com.rxlogix.enums.ReportFormatEnum; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.config.ExecutedConfiguration; com.rxlogix.config.ReportResult; com.rxlogix.config.ReportField; com.rxlogix.config.ReportTemplate; com.rxlogix.config.TemplateSet;" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.viewResult.title"/></title>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:stylesheet src="rowGroup.dataTables.min.css"/>
    <style>
    .popover{min-width : 50% !important;}
    </style>
</head>

<body>
<g:render template="/oneDrive/downloadModal"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highstock.js"/>
<asset:javascript src="vendorUi/highcharts/modules/annotations.js"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-more.js"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-3d.js"/>
<asset:javascript src="vendorUi/highcharts/modules/map.js"/>
<asset:javascript src="vendorUi/highcharts/modules/world.js"/>
<asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/solid-gauge.js"/>
<asset:javascript src="vendorUi/highcharts/plugins/grouped-categories-1.3.2.js"/>
<asset:javascript src="vendorUi/fuelux/fuelux.min.js"/>
<g:set var="interactiveType" value="${((reportResult?.executedTemplateQuery?.executedTemplate?.templateType == TemplateTypeEnum.DATA_TAB)||(reportResult?.executedTemplateQuery?.executedTemplate?.templateType == TemplateTypeEnum.CASE_LINE)||(reportResult?.executedTemplateQuery?.executedTemplate?.templateType == TemplateTypeEnum.CUSTOM_SQL)||(reportResult?.executedTemplateQuery?.executedTemplate?.templateType == TemplateTypeEnum.NON_CASE))}"/>
<g:set var="interactiveView" value="${interactiveType && reportResult?.executedTemplateQuery?.executedTemplate?.interactiveOutput}"/>
<g:set var="interactiveView" value="${params.showInteractive!=null?params.boolean("showInteractive"):interactiveView}"/>
<g:set var="userService" bean="userService"/>
<g:set var="currentUser" value="${userService.currentUser}"/>
<g:if test="${!interactiveView}">
    <style>
    .pv-caselist .pv-list-table {
        table-layout: fixed !important;
    }
    html {overflow-y : hidden;}
    </style>

</g:if>
<script>
    var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";
    var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
    var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}";
    var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
    var queryEditUrl = "${createLink(controller: 'query', action: 'edit')}";
    var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
    var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";

    var stringOperatorsUrl = "${createLink(controller: 'query', action: 'getStringOperators')}";
    var numOperatorsUrl = "${createLink(controller: 'query', action: 'getNumOperators')}";
    var dateOperatorsUrl = "${createLink(controller: 'query', action: 'getDateOperators')}";
    var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
    var keywordsUrl = "${createLink(controller: 'query', action: 'getAllKeywords')}";
    var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
    var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";

    var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
    var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
    var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
    var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";
    var granularityForTemplateUrl = "${createLink(controller: 'template', action: 'granularityForTemplate')}";
    var reassessDateForTemplateUrl = "${createLink(controller: 'template', action: 'reassessDateForTemplate')}";
    var poiInputsForTemplateUrl = "${createLink(controller: 'template', action: 'poiInputsForTemplate')}";
    var addNewSectionUrl = "${isPeriodicReport ? createLink(controller: 'periodicReport', action: 'saveSection'):createLink(controller: 'configuration', action: 'saveSection')}";
    var cioms1Id = "${ReportTemplate.cioms1Id()}";
    var medWatchId = "${ReportTemplate.medWatchId()}";
    var addDmsConfiguration = "${createLink(controller: "report",action: "addDmsConfiguration",params: [showSections:true])}";
    var dmsFoldersUrl="${createLink(controller: 'periodicReport', action: 'getDmsFolders')}";
    var reportResultLatestCommentUrl = "${createLink(controller: 'commentRest', action: 'getReportResultChartAnnotation')}";
    var LABELS = {
        labelShowAdavncedOptions: "${message(code: 'add.header.title.and.footer')}",
        labelHideAdavncedOptions: "${message(code: 'hide.header.title.and.footer')}"
    };
    var reportId = ${params.id};
    var interactiveView = ${interactiveView};
    var reportResultId = ${reportResult?.id?:"null"};
    var isInDraftMode = ${params.boolean('isInDraftMode')?:false};
    var filename = '${filename}';
    var isForIcsrProfile = '${isForIcsrProfile ?: false}';
    var isForIcsrReport = '${isForIcsrReport ?: false}';
    var latestComment = '${latestComment?(HtmlUtils.htmlEscape(latestComment)):''}';
    var removeSectionUrl = "${createLink(controller: 'periodicReport', action: 'removeSection')}";
    var editConfigUrl = "${createLink(controller: 'report', action: 'editConfig', params: params)}";
    var getSharedWith = "${createLink(controller: 'reportResultRest', action: 'getSharedWithUsers')}";
    var addEmailConfiguration="${createLink(controller: "report",action: "addEmailConfiguration")}";
    var fullPage=true;
    var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest',action: 'save')}";
    var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest',action: 'index')}";
    var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
    var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'index')}";
    var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
    var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
    var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
    var periodicReportConfig = {
        reportSubmitUrl: "${createLink(controller: "reportSubmission", action: "submitReport")}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}",
        reportingDestinationsUrl: "${createLink(controller: 'queryRest', action: 'getReportingDestinations')}",
        generateDraftUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "generateDraft")}",
        markAsSubmittedUrl: "${createLink(controller: "reportSubmission", action: "loadReportSubmissionForm")}",
    }
    var showpage = true;
    var isUserBlinded = ${currentUser.isBlinded};
    var isUserRedacted = ${currentUser.isProtected};
</script>
<g:if test="${!interactiveView}">
    <style>
        #reportDiv table td:first-child, #reportDiv table td:nth-child(3){
            width: 10px;
        }

        #reportDiv table td:nth-child(2) table{
            width: 100% !important;
        }

    </style>
</g:if>
<div class="content ">
    <div class="container ">
        <div>
<g:set var="reportFieldMasterCaseNumber" value="${ReportField.findByNameAndIsDeleted(caseNumberFieldName, false)?.id}"/>
<rx:container title="${message(code: 'app.label.report')}: ${applyCodec(encodeAs:'HTML',executedConfigurationInstance.reportName)}"
              customButtons="${g.render(template: "/report/customHeaderButtons",
                      model:[ executedConfigurationInstance: executedConfigurationInstance,
                              configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, configType: configType])}">
    <g:if test="${warningMsg}">
    <div class="alert alert-warning alert-dismissible forceLineWrap alert-warning-flash warningFlashMsg" role="alert" style="display: block">
        <i class="fa fa-warning"></i>
        <span id="flashWarningContent">${warningMsg}</span>
        <button type="button" class="close" id="warningFlashMsg" data-dismiss="alert">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
    </div>
    </g:if>
    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigurationInstance}" var="theInstance"/>
    <g:render template="/includes/layout/inlineAlerts"/>

    <g:if test="${!isLargeReportResult}">
        <g:set var="isPaperReport" value="${executedConfigurationInstance.instanceOf(ExecutedIcsrReportConfiguration) || executedConfigurationInstance.instanceOf(ExecutedIcsrProfileConfiguration)}"/>
        <div class="row ">
            <div class="col-md-10">
        <div class="btn-group">
            <g:link class="btn btn-default waves-effect waves-light"
                    action="criteria" params="[id: executedConfigurationInstance.id, isInDraftMode: params.isInDraftMode, isPaperReport: isPaperReport]">
                <i class="md md-description icon-white"></i>
                <g:message code="app.label.reportCriteria"/>
            </g:link>
        </div>
        <g:if test="${isPeriodicReport && executedConfigurationInstance.hasGeneratedCasesData && (executedConfigurationInstance.status in [ReportExecutionStatusEnum.GENERATED_DRAFT, ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT, ReportExecutionStatusEnum.SUBMITTED])}">
            <div class="btn-group">
                <%
                    boolean isDraftButton = (executedConfigurationInstance.status == ReportExecutionStatusEnum.GENERATED_DRAFT || params.boolean('isInDraftMode'))
                    boolean canShowFinal = (executedConfigurationInstance.status in [ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT, ReportExecutionStatusEnum.SUBMITTED] && executedConfigurationInstance.finalLastRunDate != null)
                    boolean shouldShowDropdown = (isDraftButton && canShowFinal) || (!isDraftButton)
                %>
                <button type="button" class="btn btn-default dropdown-toggle waves-effect waves-light"
                        data-toggle="dropdown" aria-expanded="false">
                    <i class="md md-assignment icon-white"></i>
                    <span>
                        ${message(code: isDraftButton ? 'app.periodicReport.executed.draft.label' : 'app.periodicReport.executed.final.label')}
                    </span>
                    <span class="caret"></span>
                </button>
                <g:if test="${shouldShowDropdown}">
                    <ul class="dropdown-menu" role="menu">
                        <g:if test="${isDraftButton}">
                            <g:if test="${canShowFinal}">
                                <li><g:link action="showFirstSection"
                                            params="[id: executedConfigurationInstance.id, isInDraftMode: false]">
                                    ${message(code: 'app.periodicReport.executed.final.label')}
                                </g:link></li>
                            </g:if>
                        </g:if>
                        <g:else>
                            <li><g:link action="showFirstSection"
                                        params="[id: executedConfigurationInstance.id, isInDraftMode: true]">
                                ${message(code: 'app.periodicReport.executed.draft.label')}
                            </g:link></li>
                        </g:else>
                    </ul>
                </g:if>
            </div>
        </g:if>
        <div class="btn-group">
            <button type="button" class="btn btn-default dropdown-toggle waves-effect waves-light"
                    data-toggle="dropdown" aria-expanded="false">
                <i class="md md-list icon-white"></i>
                <g:if test="${reportType == ReportTypeEnum.MULTI_REPORT}">
                    <span>${message(code: 'app.label.entire.report')} </span>
                </g:if>
                <g:else>
                    <span>${message(code: 'app.label.section', args: [""])}
                        <g:renderDynamicReportName executedConfiguration="${executedConfigurationInstance}" max="50"
                                                   executedTemplateQuery="${reportResult.executedTemplateQuery}" /></span>
                </g:else>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu break-long-word-list section-scroll" role="menu">
                <g:if test="${isPeriodicReport  && executedConfigurationInstance.hasGeneratedCasesData}">
                    <li><g:link controller="caseList" action="index" fragment="caseSeries"
                                params="[id: executedConfigurationInstance.id, isInDraftMode: params.isInDraftMode]">${message(code: 'app.caseSeries.label')}</g:link></li>
                    <li><g:link controller="caseList" action="index" fragment="openCases"
                                params="[id: executedConfigurationInstance.id, isInDraftMode: params.isInDraftMode]">${message(code: 'app.openCases.label')}</g:link></li>
                    <g:if test="${hasRemovedCases}">
                        <li><g:link controller="caseList" action="index" fragment="removedCases"
                                    params="[id: executedConfigurationInstance.id, isInDraftMode: params.isInDraftMode]">${message(code: 'app.removedCases.label')}</g:link></li>
                    </g:if>
                    <li>
                        <g:if test="${executedConfigurationInstance?.cumulativeCaseSeries}">
                            <g:link controller="caseList" action="index" id="${executedConfigurationInstance.id}"
                                    params="[cumulativeType: true, isInDraftMode: params.isInDraftMode]">
                                <g:message code="app.view.cumulative.cases"/>
                            </g:link>
                        </g:if>
                    </li>
                    <li class="divider"></li>
                </g:if>
                <g:set var="sections" value="${executedConfigurationInstance.fetchExecutedTemplateQueriesByCompletedStatus()}"/>
                <g:if test="${(sections.size() > 1)  && !sections.find{sec-> !sec.isVisible()}}">
                    <li><g:link action="viewMultiTemplateReport"
                                params="[id: executedConfigurationInstance.id, isInDraftMode: params.isInDraftMode]"><g:message code="app.label.entire.report"/></g:link></li>
                </g:if>
                <g:each var="executedTemplateQuery"
                        in="${sections}">
                    <g:if test="${executedTemplateQuery.isVisible()}">
                    <li><g:link action="show" params="[id: params.boolean('isInDraftMode')?(executedTemplateQuery.draftReportResult?.id?:executedTemplateQuery.reportResult?.id):executedTemplateQuery.reportResult?.id, isInDraftMode: params.isInDraftMode]">
                        <g:renderDynamicReportName executedConfiguration="${executedConfigurationInstance}" max="120"
                                                   executedTemplateQuery="${executedTemplateQuery}" />
                    </g:link></li>
                    </g:if>
                </g:each>
            </ul>
        </div>
        <div class="btn-group">
            <g:set var="isPaperReport" value="${false}"/>
            <rx:showXMLOption>
                <g:set var="isPaperReport" value="${executedConfigurationInstance.instanceOf(IcsrReportConfiguration) || executedConfigurationInstance.instanceOf(IcsrProfileConfiguration)}"/>
                <g:if test="${!ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.XML, reportType)}">
                    <g:link class="btn btn-default waves-effect waves-light"
                            action="${action}"
                            params="[id: params.id, outputFormat: ReportFormatEnum.XML.name()]"
                            data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.e2b.r2.xml')}">
                        <asset:image src="r2-xml-icon.png" class="xml-icon" height="16" width="16"/>
                    </g:link>
                </g:if>
                <g:if test="${!ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.XML, reportType)}">
                    <g:link class="btn btn-default waves-effect waves-light"
                            action="${action}"
                            params="[id: params.id, outputFormat: ReportFormatEnum.R3XML.name()]"
                            data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.e2b.r3.xml')}">
                        <asset:image src="xml-icon.png" class="xml-icon" height="16" width="16"/>
                    </g:link>
                </g:if>
            </rx:showXMLOption>
            <a href="javascript:void(0)"
               data-url="${createLink(controller: 'report', action: action, params: [id: params.id, outputFormat: ReportFormatEnum.PDF.name(), isInDraftMode: params.isInDraftMode, paperReport: isPaperReport, includeCaseNumber: includeCaseNumber], absolute: true)}"
               data-name="${executedConfigurationInstance.reportName}.PDF"
               class="downloadUrl btn btn-default waves-effect waves-light ${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.PDF, reportType)? 'disabled' : ''}"
               data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.pdf')}">
                <asset:image src="pdf-icon.png" class="pdf-icon" height="16" width="16"/>
            </a>
            <a href="#"
               data-url="${createLink(controller: 'report', action: action, params: [id: params.id, outputFormat: ReportFormatEnum.XLSX.name(), isInDraftMode: params.isInDraftMode, includeCaseNumber: includeCaseNumber], absolute: true)}"
               data-name="${executedConfigurationInstance.reportName}.XLSX"
               class="downloadUrl btn btn-default waves-effect waves-light  ${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.XLSX, reportType)? 'disabled' : ''}"
               data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.excel')}">
                <asset:image src="excel.gif" class="excel-icon" height="16" width="16"/>
            </a>
            <a href="#"
               data-url="${createLink(controller: 'report', action: action, params: [id: params.id, outputFormat: ReportFormatEnum.DOCX.name(), isInDraftMode: params.isInDraftMode, includeCaseNumber: includeCaseNumber], absolute: true)}"
               data-name="${executedConfigurationInstance.reportName}.DOCX"
               class="downloadUrl btn btn-default waves-effect waves-light  ${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.DOCX, reportType)? 'disabled' : ''}"
               data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.word')}">
                <asset:image src="word-icon.png" class="word-icon" height="16" width="16"/>
            </a>
            <a href="#"
               data-url="${createLink(controller: 'report', action: action, params: [id: params.id, outputFormat: ReportFormatEnum.PPTX.name(), isInDraftMode: params.isInDraftMode, includeCaseNumber: includeCaseNumber], absolute: true)}"
               data-name="${executedConfigurationInstance.reportName}.PPTX"
               class="downloadUrl btn btn-default waves-effect waves-light  ${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.PPTX, reportType)? 'disabled' : ''}"
               data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.powerpoint')}">
                <asset:image src="powerpoint-icon.png" class="powerpoint-icon" height="16" width="16"/>
            </a>
            <g:if test="${isNuprCsv}">
                <a href="#"
                   data-url="${createLink(controller: 'report', action: action, params: [id: params.id, outputFormat: ReportFormatEnum.CSV.name(), isInDraftMode: params.isInDraftMode, includeCaseNumber: includeCaseNumber], absolute: true)}"
                   data-name="${executedConfigurationInstance.reportName}.CSV"
                   class="downloadUrl btn btn-default waves-effect waves-light  ${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.CSV, reportType)? 'disabled' : ''}"
                   data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.csv')}">
                    <asset:image src="csv-icon.png" class="xml-icon" height="16" width="16"/>
                </a>
            </g:if>
            <g:if test="${(reportType != ReportTypeEnum.MULTI_REPORT) && interactiveType}">
                <g:if test="${interactiveView}">
                    <a href="${createLink(controller: 'report', action: 'show', params: [id: params.id, showInteractive: false, isInDraftMode: params.isInDraftMode], absolute: true)}"
                       class="btn btn-default waves-effect waves-light"
                       title="${message(code: 'app.reportOutput.standard')}">
                        <asset:image src="icons/html-icon.png" class="html-icon" height="16" width="16"/>
                    </a>

                </g:if>
                <g:else>
                    <a href="${createLink(controller: 'report', action: 'show', params: [id: params.id, showInteractive: true, isInDraftMode: params.isInDraftMode], absolute: true)}"
                       class="btn btn-default waves-effect waves-light"
                       title="${message(code: 'app.reportOutput.interactive')}">
                        <asset:image src="icons/html-icon.png" class="html-icon" height="16" width="16"/>
                    </a>
                </g:else>
            </g:if>
            <g:link class="btn btn-default waves-effect waves-light  btn-seperator" url="#"
                    data-toggle="modal" data-target="#saveAsModal" data-action="${action}"
                    data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'default.button.saveAs.label')}">
                <span class="glyphicon glyphicon-download-alt icon-white" style="font-size: smaller"></span>
            </g:link>

<!----------------------------------=========----------------button code merged--------------====================------------------------------->
            <a href="#" class="min-width-section-comment btn btn-default commentModalTrigger ${(reportType == ReportTypeEnum.MULTI_REPORT && executedConfigurationInstance?.getSelectedColumnsForSection()?.collect {
                it.id
            }?.contains(reportFieldMasterCaseNumber))|| (reportType == ReportTypeEnum.REPORT && ((executedTemplateInstance instanceof ExecutedCaseLineListingTemplate && executedTemplateInstance.getAllSelectedFieldsInfo()?.reportField?.collect {
                it.id
            }?.contains(reportFieldMasterCaseNumber)) || executedTemplateInstance instanceof ExecutedCustomSQLTemplate)) ? '': 'btn-radius'}" data-owner-id="${reportType == ReportTypeEnum.MULTI_REPORT ?executedConfigurationInstance.id:reportResult?.id}" data-comment-type="${reportType == ReportTypeEnum.MULTI_REPORT ? CommentTypeEnum.EXECUTED_CONFIGURATION:CommentTypeEnum.REPORT_RESULT}"
               data-toggle="modal" data-target="#commentModal">
                <g:renderAnnotateIcon comments="${comments}" title="${message(code: (reportType == ReportTypeEnum.MULTI_REPORT ? "report.annotaion" : "section.annotaion"))}"/>
            </a>
            <g:render template="/includes/widgets/commentsWidget"/>
            <sec:ifAnyGranted roles="ROLE_PVC_EDIT">
                <g:if test="${reportResult?.executedTemplateQuery?.executedTemplate?.name == Holders.config.pvcModule.late_processing_template || reportResult?.executedTemplateQuery?.executedTemplate?.name == Holders.config.pvcModule.inbound_processing_template}">
                    <a target="_blank" href="${createLink(controller: "advancedReportViewer", action: "view", params: [id: reportResult?.id])}" class="btn btn-default commentModalTrigger" title="${message(code:"app.reasonOfDelay.edit", default:"Edit Reason of Delay")}"><i class="md md-table-edit icon-white"></i>
                    </a>
                </g:if>
            </sec:ifAnyGranted>
            <g:if test="${reportType == ReportTypeEnum.MULTI_REPORT}">
                <g:if test="${executedConfigurationInstance?.getSelectedColumnsForSection()?.collect {
                    it.id
                }?.contains(reportFieldMasterCaseNumber)}">
                    <g:link id="multiReportData" class="btn btn-default waves-effect waves-light btn-radius" url="#"
                            data-toggle="modal" data-target="#clipBoardModel" data-type="multi-report"
                            data-tooltip="tooltip" data-placement="bottom" title="${message(code: "copy.case.numbers")}">
                        <g:hiddenField name="elementId" value="${params.id}"/>
                        <i class="md md-content-copy icon-white"></i>
                    </g:link>
                </g:if>
            </g:if>
            <g:elseif
                    test="${reportType == ReportTypeEnum.REPORT && ((executedTemplateInstance instanceof ExecutedCaseLineListingTemplate && executedTemplateInstance.getAllSelectedFieldsInfo()?.reportField?.collect {
                        it.id
                    }?.contains(reportFieldMasterCaseNumber)) || executedTemplateInstance instanceof ExecutedCustomSQLTemplate)}">
                <g:link id="reportData" class="btn btn-default waves-effect waves-light btn-radius" url="#"
                        data-toggle="modal" data-target="#clipBoardModel" data-type="report"
                        data-tooltip="tooltip" data-placement="bottom" title="${message(code: "copy.case.number")}">
                    <g:hiddenField name="elementId" value="${params.id}"/>
                    <i class="md md-content-copy icon-white"></i>
                </g:link>
            </g:elseif>
            <g:render template="/includes/widgets/clipBoardModel"/>
<!-----------------------======================--------------code end-----------------====================----------------------------->
            <g:render template="saveAsModal"
                      model="[action: action,
                              executedConfigurationInstance: executedConfigurationInstance,
                              executedTemplateInstance: executedTemplateInstance,
                              reportType: reportType,
                              ciomsITemplateId: ciomsITemplateId]"/>
        </div>
            </div>
            <g:if test="${showNuprCaseNumCheckbox}">
                <div class="btn-group col-md-2 text-end" style="text-align: right">
                    <g:checkBox id="includeCaseNumber"
                                name="includeCaseNumber"
                                value="${true}"
                                checked="${includeCaseNumber}"
                                data-evt-change='{"method": "showHideCaseNumberColumn", "params": []}'/>
                    <label for="includeCaseNumber"><g:message code="app.label.include.casenumber"/></label>
                </div>
            </g:if>
        </div>
        <g:if test="${!interactiveView}">
            %{--Div that will be refreshed with a report to show on screen--}%
            <div id="reportDiv" class="reportDiv"  style="min-height: 140px; max-height: 460px;"></div>
        </g:if>
    </g:if>
    <g:render template="/configuration/includes/addSection"/>
    <g:render template="/advancedReportViewer/includes/addTemplateSection"/>
    <g:render template="/advancedReportViewer/includes/confirmation"/>
    <g:render template="/email/includes/copyPasteEmailModal" />
    <g:render template="includes/columnLegend"
              model="[executedConfigurationInstance: executedConfigurationInstance]"/>
</rx:container>
    <g:if test="${interactiveView}">
        <div id="interactiveReportDiv" class="reportDiv"  style="width: 100%"></div>
    </g:if>
<asset:javascript src="app/report/caseSeries.js"/>
<asset:javascript src="app/query/queryValueSelect2.js"/>
<asset:javascript src="app/editModal.js"/>
<asset:javascript src="app/report/show.js"/>
<g:showIfDmsServiceActive>
    <asset:javascript src="app/configuration/dmsConfiguration.js"/>
</g:showIfDmsServiceActive>
<asset:javascript src="app/configuration/configurationCommon.js"/>
<asset:stylesheet src="rowGroup.dataTables.min.css"/>
<asset:javascript src="app/report/advancedViewerChart.js"/>
<asset:javascript src="app/report/advancedViewer.js"/>
<asset:javascript src="app/configuration/copyPasteValues.js"/>
<asset:javascript src="app/commonGeneratedReportsActions.js"/>
<asset:javascript src="app/configuration/deliveryOption.js"/>
<asset:javascript src="app/workFlow.js"/>
<asset:javascript src="app/actionItem/actionItemModal.js"/>
<asset:javascript src="app/periodicReport.js"/>
<asset:javascript src="app/utils/pvr-common-util.js"/>
<asset:javascript src="app/utils/pvr-filter-util.js"/>

<asset:javascript src="datatables/extendedDataTable.js"/>
<asset:stylesheet src="datatables/extendedDataTable.css"/>

<asset:javascript src="datatables/dataTables.columnResize.js"/>
<asset:stylesheet src="datatables/dataTables.columnResize.css"/>

<asset:javascript src="datatables/dataTables.fixedHeader.js"/>
<asset:stylesheet src="datatables/dataTables.fixedHeader.css"/>

        </div>

    </div>
</div>
<form id="exportForm" method="post">
    <input type="hidden" name="filter" value="${params.filter}">
    <input type="hidden" name="direction" id="direction" >
    <input type="hidden" name="sort" id="sort" >
    <input type="hidden" name="searchData" id="searchData" >
    <input type="hidden" name="globalSearch" id="globalSearch" >
    <input type="hidden" name="rowIdFilter" id="rowIdFilter" >
    <input type="hidden" name="dynamic" value="true" >
</form>
<g:form controller="report" data-evt-sbt='{"method": "submitForm", "params": []}'>
    <g:hiddenField name="executedConfigId" value="${executedConfigurationInstance.id}"/>
    <g:render template="includes/sharedWithModal" />
    <g:render template="includes/emailToModal" />
</g:form>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/copyPasteModal"/>
<asset:javascript src="/plugin/dictionary/dictionaryMultiSearch.js"/>
<g:render template="/query/workflowStatusJustification" model="[tableId:'_report']"/>
<g:render template="/actionItem/includes/actionItemModal" model="[]" />
<g:render template="/includes/widgets/reportSubmission"/>
<g:render template="/includes/widgets/errorTemplate" model="[messageBody:message(code: 'app.dms.config.error'), errorModalId:'dmsErrorModal']"/>
</body>
</html>
