<%@ page import="com.rxlogix.enums.ReportActionEnum; com.rxlogix.config.publisher.GanttItem; com.rxlogix.config.publisher.PublisherReport; com.rxlogix.config.publisher.PublisherExecutedTemplate;com.rxlogix.util.DateUtil; com.rxlogix.enums.ActionItemGroupState; com.rxlogix.enums.StatusEnum; com.rxlogix.config.ActionItem; com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.enums.ReportSubmissionStatusEnum; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.config.CaseLineListingTemplate; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper; com.google.gson.annotations.Until; com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.enums.ReportFormatEnum" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.pvp.viewCriteria.title"/></title>
    <style>
    .btn-round {
        border-radius: 20px !important;
        margin-top: 2px;
    }

    .btn-left-round {
        border-radius: 20px 0 0 20px !important;
    }

    .btn-right-round {
        border-radius: 0 20px 20px 0 !important;
    }

    </style>
    <script>
        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}";
        var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
        var queryEditUrl = "${createLink(controller: 'query', action: 'edit')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";

        var stringOperatorsUrl = "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl = "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl = "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl = "${createLink(controller: 'query', action: 'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var allFieldsUrl = "${createLink(controller: 'query', action: 'getAllFields')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var listPublisherActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'listForPublisher')}?id=${params.id}";

        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";
        var poiInputsForTemplateUrl = "${createLink(controller: 'template', action: 'poiInputsForTemplate')}";
        var configurationPOIInputsParamsUrl = "${createLink(controller: 'configurationRest', action: 'getConfigurationPOIInputsParams',id: executedConfigurationInstance.id)}";
        var addNewSectionUrl = "${createLink(controller: 'periodicReport', action: 'saveSection')}";
        var removeSectionUrl = "${createLink(controller: 'periodicReport', action: 'removeSection')}";
        var cioms1Id = "${ReportTemplate.cioms1Id()}";
        var medWatchId = "${ReportTemplate.medWatchId()}";
        var validateValue = "${createLink(controller: 'configuration', action: 'validateValue')}";

        var LABELS = {
            labelShowAdavncedOptions: "${message(code: 'add.header.title.and.footer')}",
            labelHideAdavncedOptions: "${message(code: 'hide.header.title.and.footer')}"
        }
        hasAccessOnActionItem = true;
        var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
        var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
        var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'index')}";
        var updateAssignedToUrl = "${createLink(controller: 'pvp', action: 'updateAssignedTo')}";
        var updateDestinationUrl = "${createLink(controller: 'pvp', action: 'updateDestination')}";
        var updateDueUrl = "${createLink(controller: 'pvp', action: 'updateDue')}";
        var updateCommentUrl = "${createLink(controller: 'pvp', action: 'updateComment')}";
        var removeSectiontUrl = "${createLink(controller: 'pvp', action: 'removeSection')}";
        var updateNameUrl = "${createLink(controller: 'pvp', action: 'updateName')}";
        var changeSortOrderUrl = "${createLink(controller: 'pvp', action: 'changeSortOrder')}";
        var removePublisherReportUrl = "${createLink(controller: 'pvp', action: 'removePublisherReport')}";
        var listPublisherExecutedTemplatesUrl = "${createLink(controller: 'pvp', action: 'listPublisherExecutedTemplates')}";
        var listPublisherExecutedLogUrl = "${createLink(controller: 'pvp', action: 'listPublisherExecutedLogUrl')}";
        var setAsFinalURL = "${createLink(controller: 'pvp', action: 'setAsFinal')}";
        var downloadURL = "${createLink(controller: 'pvp', action: 'downloadPublisherExecutedTemplate', absolute: true)}";
        var publishURL = "${createLink(controller: 'pvp', action: 'publish')}";
        var editOfficeURL = "${createLink(controller: 'wopi', action: 'edit')}";
        var viewOfficeURL = "${createLink(controller: 'wopi', action: 'pdf')}";
        var restoreDraftURL = "${createLink(controller: 'pvp', action: 'restoreDraft')}";
        var fetchPendingParametersURL = "${createLink(action: 'fetchPendingParameters')}";
        var saveParamsAndGenerateURL = "${createLink(action: 'saveParamsAndGenerateURL')}";
        var updateAuthorUrl = "${createLink(controller: 'pvp', action: 'updateAuthor')}";
        var updateReviewerUrl = "${createLink(controller: 'pvp', action: 'updateReviewer')}";
        var updateApproverUrl = "${createLink(controller: 'pvp', action: 'updateApprover')}";
        var generateAllDraftURL = "${createLink(controller: 'pvp', action: 'generateAllDraft')}";
        var publisherTemplateParametersUrl = "${createLink(controller: 'publisherTemplate', action: 'getTemplateParameters')}";
        var testScriptUrl = "${createLink(controller: 'publisherTemplate', action: 'testScript')}";
        var workflowJustificationUrl = "${createLink(controller: 'workflowJustificationRest', action: 'publisherSection')}";
        var workflowJustificationConfirnUrl = "${createLink(controller: 'workflowJustificationRest', action: 'savePublisherSection')}";
        var reportId = ${params.id};
        var fetchParametersUrl = "${createLink(controller: 'publisherTemplate', action: 'fetchParameters')}";
        var ganttUrl = "${createLink(controller: 'gantt', action: 'singleGanttAjax')}?id=${params.id}";
        var changeDependenceUrl = "${createLink(controller: 'gantt', action: 'changeDependence')}";
        var backUrl = "${createLink(controller: 'pvp', action: 'sections', absolute: true)}?id=${params.id}";
        var getAllowedSectionsUrl = "${createLink(controller: 'pvp', action: 'getSectionsInfoList')}?id=${params.id}";
        var PVPTemplateSearchUrl="${createLink(controller: 'publisherTemplate', action: 'getPublisherTemplateList')}";
        var PVPTemplateNameUrl="${createLink(controller: 'publisherTemplate', action: 'getTemplateNameDescription')}";
        var PVPTaskTemplateSearchUrl="${createLink(controller: 'taskTemplate', action: 'ajaxGetPublisherSectionTemplates')}";
        var PVPTaskTemplateNameUrl="${createLink(controller: 'taskTemplate', action: 'ajaxGetPublisherSectionTemplatesName')}";
        var addEmailConfiguration="${createLink(controller: "report",action: "addEmailConfiguration")}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        <g:applyCodec encodeAs="none">
        var TASK_TYPES = ${GanttItem.TaskType.list().encodeAsJSON()};
        var destinationList = ['${executedConfigurationInstance.getAllReportingDestinations().join("','")}'];
        var selectedTab = "${params.selectedTab?:""}";
        var periodicReportConfig = {
            reportSubmitUrl: "${createLink(controller: "reportSubmission", action: "submitReport")}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}",
            reportingDestinationsUrl: "${createLink(controller: 'queryRest', action: 'getReportingDestinations')}",
            generateDraftUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "generateDraft")}",
            markAsSubmittedUrl: "${createLink(controller: "reportSubmission", action: "loadReportSubmissionForm")}",
        }
        </g:applyCodec>
    </script>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:stylesheet src="jsgantt.css"/>
    <asset:stylesheet src="publisher.css"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:javascript src="app/periodicReport.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/pvpsections.js"/>
    <asset:javascript src="app/periodicReport.js"/>
    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <asset:javascript src="app/addSection.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:javascript src="app/publisher/PublisherTemplateConfiguration.js"/>
    <asset:javascript src="app/workFlow.js"/>
    <asset:javascript src="/gantt/jsgantt.js"/>
    <asset:javascript src="/app/publisher/gantt.js"/>
</head>

<body>

<g:set var="column1Width" value="3"/>
<g:set var="column2Width" value="9"/>
<g:set var="isFinal" value="${(executedConfigurationInstance.status in[ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT,ReportExecutionStatusEnum.COMPLETED,ReportExecutionStatusEnum.SUBMITTED])}"/>


<div class="row topHeaderRow">
    <div class="col-md-2 w-12-percentage" style="padding-left: 10px ">
        <label><g:message code="app.label.reportName"/></label><br>
        ${executedConfigurationInstance.reportName}

    </div>
    <div class="col-md-1 w-12-percentage">
        <label><g:message code="app.periodicReport.executed.productName.label"/></label><br>
        ${ViewHelper.getDictionaryValues(executedConfigurationInstance.productSelection?:"", DictionaryTypeEnum.PRODUCT)}

    </div>
    <div class="col-md-1 w-12-percentage">
        <label><g:message code="app.periodicReport.executed.reportPeriod.label"/></label><br>
        <div>
            <g:renderShortFormattedDate date="${executedConfigurationInstance.executedGlobalDateRangeInformation?.dateRangeStartAbsolute}"/>
            -
            <g:renderShortFormattedDate date="${executedConfigurationInstance.executedGlobalDateRangeInformation?.dateRangeEndAbsolute}"/>
        </div>

    </div>
    <div class="col-md-1 w-10-percentage">
        <label><g:message code="app.label.reportingDestinations"/></label><br>
        ${([executedConfigurationInstance.primaryReportingDestination]+executedConfigurationInstance.getReportingDestinations())?.join(", ")}

    </div>
    <div class="col-md-1 w-10-percentage">
        <label><g:message code="app.periodicReport.executed.daysLeft.label"/></label><br>
        <span class="roundLabel ${ViewHelper.getDueDateCssClass(executedConfigurationInstance)}">
            <g:renderShortFormattedDate date="${executedConfigurationInstance.dueDate}"/></span>

    </div>
    <div class="col-md-1  w-10-percentage">
        <label><g:message code="app.periodicReport.executed.workflowState.label"/></label><br>
        <button class="btn btn-default btn-xs btn-round" style="min-width: 100px ;     border-radius: 10px;" data-executed-config-id="${executedConfigurationInstance.id}" data-initial-state="${executedConfigurationInstance.workflowState?.name}" data-evt-clk='{"method": "openStateHistoryModal", "params": ["${this}", "${createLink(controller: "workflowJustificationRest", action: "index")}", "${createLink(controller: "workflowJustificationRest", action: "save")}", "_page"]}'>${executedConfigurationInstance.workflowState?.name}</button>

    </div>
    <div class="col-md-1 sectionsActions w-10-percentage">
        <label><g:message code="app.periodicReport.executed.actions.label"/></label><br>
        <div class="btn-group dropdown" align="center">
            <a href="javascript:void(0)" class="btn btn-success btn-xs btn-left-round  "><g:message code="app.label.publisher.overview.allowedAction"/></a>
            <button type="button" class="btn btn-default btn-xs btn-right-round dropdown-toggle" data-toggle="dropdown">
                <span class="caret"></span>
                <span class="sr-only">Toggle Dropdown</span>
            </button>
            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">
                <g:each var="action" in="${executedConfigurationInstance.workflowState?.reportActionsAsList}">
                    <g:if test="${(executedConfigurationInstance.hasGeneratedCasesData && action == ReportActionEnum.GENERATE_CASES)}">
                        <li role="presentation" class="generateCases"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions generateCases" data-exconfig-id="${executedConfigurationInstance.id}">${message(code: action.i18nKey)}</a>
                        </li>
                    </g:if>
                    <g:if test="${(executedConfigurationInstance.hasGeneratedCasesData && action == ReportActionEnum.GENERATE_DRAFT)}">
                        <li role="presentation" class="stateSpecificActions"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions generateDraft" data-exconfig-id="${executedConfigurationInstance.id}">${message(code: action.i18nKey)}</a>
                        </li>
                    </g:if>
                    <g:if test="${(executedConfigurationInstance.hasGeneratedCasesData && action == ReportActionEnum.GENERATE_FINAL)}">
                        <li role="presentation" class="stateSpecificActions"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions generateFinalDraft" data-exconfig-id="${executedConfigurationInstance.id}">${message(code: action.i18nKey)}</a>
                        </li>
                    </g:if>
                    <g:if test="${(executedConfigurationInstance.hasGeneratedCasesData && action == ReportActionEnum.GENERATE_CASES_FINAL)}">
                        <li role="presentation" class="stateSpecificActions"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions generateCasesFinalDraft" data-exconfig-id="${executedConfigurationInstance.id}">${message(code: action.i18nKey)}</a>
                        </li>
                    </g:if>
                    <g:if test="${(executedConfigurationInstance.hasGeneratedCasesData && action == ReportActionEnum.GENERATE_CASES_DRAFT)}">
                        <li role="presentation" class="stateSpecificActions"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions generateCasesDraft" data-exconfig-id="${executedConfigurationInstance.id}">${message(code: action.i18nKey)}</a>
                        </li>
                    </g:if>
                    <g:if test="${(action == ReportActionEnum.MARK_AS_SUBMITTED)}">
                        <li role="presentation" class="stateSpecificActions"><a role="menuitem" class="listMenuOptions markAsSubmitted" data-toggle="modal" data-target="#reportSubmissionModal" href="#" data-url="${createLink(controller: "reportSubmission", action: "loadReportSubmissionForm")}?id=${executedConfigurationInstance.id}">${message(code: action.i18nKey)}</a>
                        </li>
                    </g:if>
                </g:each>

            </ul>
        </div>

    </div>
    <div class="col-md-1  w-10-percentage">
        <label><g:message code="app.periodicReport.executed.reportOwner.label"/></label><br>
        ${executedConfigurationInstance.owner?.fullName}

    </div>
    <div class="col-md-2  w-12-percentage">
        <label><g:message code="app.publisher.publisherContributors"/></label><br>
        ${executedConfigurationInstance.primaryPublisherContributor?.fullName}(P)
        ${executedConfigurationInstance.publisherContributors?.findAll{it.id!=executedConfigurationInstance.primaryPublisherContributor?.id}?.collect { it.fullName }.join(", ")}

    </div>
</div>
<g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigurationInstance}" var="theInstance"/>
<g:render template="/includes/layout/inlineAlerts"/>
<div style="margin-left: 10px">
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <ul class="nav nav-tabs" role="tablist" style="margin-top: -7px;  height: 40px;">
                <li role="presentation"><a href="#overviewTab" id="overviewTabLink" class="tab-ref" aria-controls="overviewTab" role="tab" data-toggle="tab"><g:message code="app.label.publisher.overview.overview"/></a>
                </li>
                <li role="presentation"><a href="#publisherTab" id="publisherTabLink" class="tab-ref" aria-controls="publisherTab" role="tab" data-toggle="tab"><g:message code="app.label.PublisherTemplate.ready"/></a>
                </li>
                <li role="presentation"><a href="#sectionsTab" id="sectionsTabLink" class="tab-ref" aria-controls="sectionsTab" role="tab" data-toggle="tab"><g:message code="app.label.publisher.overview.Sections"/></a>
                </li>
                <li role="presentation"><a href="#attachmentTab" id="attachmentTabLink" class="tab-ref" aria-controls="attachmentTab" role="tab" data-toggle="tab"><g:message code="app.label.PublisherTemplate.additionalSources"/></a>
                </li>
                <li role="presentation"><a href="#reportTab" id="reportTabLink" class="tab-ref" aria-controls="reportTab" role="tab" data-toggle="tab"><g:message code="app.label.publisher.overview.generatedreport"/></a>
                </li>
                <g:if test="${executedConfigurationInstance.associatedSpotfireFile}">
                    <li role="presentation"><a href="#spotfireTab" id="spotfireTabLink" class="tab-ref" aria-controls="spotfireTab" role="tab" data-toggle="tab"><g:message code="app.label.dataAnalysis"/></a>
                    </li>
                </g:if>
                <g:if test="${grailsApplication.config.pv.app.pvpublisher.gantt.enabled}">
                    <li role="presentation"><a href="#ganttTab" id="ganttTabLink" class="tab-ref" aria-controls="ganttTab" role="tab" data-toggle="tab"><g:message code="app.label.publisher.overview.planGanttChart"/></a>
                    </li>
                </g:if>
                <li role="presentation"><a href="#aiTab" id="aiTabLink" class="tab-ref" aria-controls="aiTab" role="tab" data-toggle="tab"><g:message code="app.label.publisher.overview.actionItems"/></a>
                </li>
            </ul>

        </div>

        <div class="rxmain-container-content row" style="margin: 0">
            <div class="tab-content">
                <g:render template="includes/overview"/>
                <g:render template="includes/publisherTab"/>
                <g:render template="includes/sectionsTab"/>
                <g:render template="includes/attachmentTab"/>
                <g:render template="includes/reportTab"/>
                <g:if test="${executedConfigurationInstance.associatedSpotfireFile}">
                    <g:render template="includes/spotfireTab"/>
                </g:if>
                <g:render template="includes/ganttTab"/>
                <g:render template="includes/aiTab"/>
            </div>
        </div>
    </div>



    <g:render template="/periodicReport/includes/addSection"/>
    <g:render template="/includes/widgets/confirmation"/>
    <g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"/>




    <div id="nameEditDiv" class="popupBox" style="position: absolute; display: none; width: 20%">
        <input name="nameInput" id="nameInput" class="form-control nameInput " maxlength="255"/>


        <div style="margin-top: 10px; width: 100%; text-align: right;">
            <button type="button" class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
            <button type="button" class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
        </div>
    </div>

    <div id="destinationEditDiv" class="popupBox" style="position: absolute; display: none; width: 20%">
        <input type="hidden" name="destinationSelect" class="form-control destinationSelect"/>


        <div style="margin-top: 10px; width: 100%; text-align: right;">
            <button type="button" class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
            <button type="button" class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
        </div>
    </div>

    <div id="dueEditDiv" class="popupBox" style="position: absolute; display: none; width: 20%">
        <div class="fuelux">
            <div>
                <div class="datepicker pastDateNotAllowed toolbarInline">
                    <div class="input-group">
                        <g:textField id="dueDate" class="form-control fuelux date dueInput" name="dueDate" value=""/>
                        <g:render id="dueDateIn" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                    </div>
                </div>
            </div>
        </div>


        <div style="margin-top: 10px; width: 100%; text-align: right;">
            <button type="button" class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
            <button type="button" class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
        </div>
    </div>

    <div id="pcommentEditDiv" class="popupBox" style="position: absolute; display: none; width: 20%">
        <textarea class="form-control pcommentInput" id="pcommentInput"></textarea>

        <div style="margin-top: 10px; width: 100%; text-align: right;">
            <button type="button" class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
            <button type="button" class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
        </div>
    </div>


    <div id="userGroupEditDiv" class="popupBox" style="position: absolute; display: none; width: 20%">
        <g:set var="userGroups" value="${com.rxlogix.user.UserGroup.findAllByIsDeleted(false).sort {it.name.toLowerCase()}}"/>
        <select name="queryTemplateUserGroupSelect" class="form-control queryTemplateUserGroupSelect">
            <g:if test="${userGroups}">
                <option id="0">${defaultSharedWith}</option>
                <g:each in="${userGroups}" var="userGroup">
                    <option value="${userGroup.id}" data-blinded="${userGroup.isBlinded}">${userGroup.getReportRequestorValue()}</option>
                </g:each>
            </g:if>
        </select>
        <div style="margin-top: 10px; width: 100%; text-align: right;">
            <button type="button" class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
            <button type="button" class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
        </div>
    </div>
    <div id="userEditDiv" class="popupBox" style="position: absolute; display: none; width: 20%">
        <g:set var="users" value="${com.rxlogix.user.User.findAllByEnabled(true).sort { it.fullName }}"/>
        <select name="userSelect" class="form-control userSelectSelect">
            <g:each in="${users}" var="user">
                <option value="${user.id}" data-blinded="${user.isBlinded}">${user.getReportRequestorValue()}</option>
            </g:each>
        </select>
        <div style="margin-top: 10px; width: 100%; text-align: right;">
            <button type="button" class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
            <button type="button" class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
        </div>
    </div>

    <g:render template="/includes/widgets/commentsWidget"/>
    <g:render template="/actionItem/includes/actionItemModal" model="[]"/>
    <g:render template="/query/workflowStatusJustification" model="[tableId: '', isPeriodicReport: true]"/>
    <g:render template="/oneDrive/downloadModal"/>
    <div id="historyModal" class="modal fade" role="dialog">
        <div class="modal-dialog  modal-lg">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h5 class="modal-title modalHeader"><g:message code="app.label.PublisherTemplate.history.modal.title"/></h5>
                </div>

                <div class="modal-body">
                    <div class="historyModalBody">

                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>

                </div>
            </div>

        </div>
    </div>
    <div id="publisherLogModal" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h5 class="modal-title modalHeader"><g:message code="app.label.PublisherTemplate.history.modal.title"/></h5>
                </div>

                <div class="modal-body">
                    <textarea class=" form-control publisherLogModalBody" style="width: 100%;height: 500px;white-space: pre; overflow-wrap: normal;overflow-x: scroll;">

                    </textarea>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>

                </div>
            </div>

        </div>
    </div>
    <div id="uploadModal" class="modal fade" role="dialog">
        <div class="modal-dialog modal-lg">

            <!-- Modal content-->
            <div class="modal-content">
                <form enctype="multipart/form-data" method="post" action="${createLink(controller: 'pvp', action: 'uploadDocument')}${_csrf ? ("?" + _csrf?.parameterName + "=" + _csrf?.token) : ""}">

                    <div class="modal-header">
                        <button type="button" class="close danger" data-dismiss="modal">&times;</button>
                        <h5 class="modal-title modalHeader"><g:message code="app.label.PublisherTemplate.upload.modal.title"/></h5>
                    </div>

                    <div class="modal-body">
                        <div class="uploadModalBody">
                            <input type="hidden" id="uploadSectionId" name="id" value="">
                            <input type="hidden" id="uploadSectionType" name="type" value="">
                            <input type="hidden" id="reportId" name="reportId" value="${params.id}">
                            <g:render template="/configuration/includes/publisherFileSection" model="[publisherParameterName:'publisherSectionTemplate']"/>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="submit" class="btn btn-default " data-evt-clk='{"method": "showLoader", "params": []}'><g:message code="app.label.PublisherTemplate.upload.modal.button"/></button>
                        <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>
                    </div>
                </form>
            </div>

        </div>
    </div>

    <div id="publishModal" class="modal fade" role="dialog">
        <div class="modal-dialog modal-lg">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <label class="modal-title modalHeader"><g:message code="app.label.PublisherTemplate.createFull" default="Generate Full Document"/></label>
                </div>

                <div class="modal-body">
                    <div class="row">
                        <div class="alert alert-danger alert-dismissible forceLineWrap fullGenerationError" role="alert" hidden="hidden">
                            <button type="button" class="close" name="fullGenErrorClose">
                                <span aria-hidden="true">&times;</span>
                                <span class="sr-only"><g:message code="default.button.close.label"/></span>
                            </button>
                            <p class="errorContent"></p>
                        </div>
                    </div>
                    <div class="row m-t-5">
                        <div class="col-md-6">
                            <label><g:message code="app.label.name"/><span class="required-indicator">*</span></label>
                            <input name="name" class="form-control name" value="${executedConfigurationInstance.reportName}" maxlength="4000">
                            <label class="m-t-10"><g:message code="app.label.publisher.overview.destination"/><span class="required-indicator">*</span></label>
                            <input name="fullReportDestinations" value="" class="form-control fullReportDestinations" multiple="multiple" style="width: 93%;">
                            <a href="javascript:void(0);" class="ic-sm pv-ic pv-ic-hover" style="padding-bottom: 5px"><i class="md-lg md-filter destinationFilter pv-cross" title="${message(code:  "app.label.template.xml.filterBy") + ' ' + message(code: "app.label.country")}"></i></a>
                        </div>
                        <div class="col-md-6">
                            <label><g:message code="comment.textData.label"/><span class="required-indicator">*</span></label>
                            <textarea name="comment" class="form-control publishComment" maxlength="4000" style="height: 85px;"></textarea>
                        </div>
                    </div>
                    <div class="row pv-caselist m-t-10">
                    <table class="table table-striped pv-list-table dataTable no-footer" id="fullGenerationTable">
                        <thead>
                        <th width="30%"><label><g:message code="app.label.name"></g:message></label></th>
                        <th width="30%"><label><g:message code="app.label.publisher.overview.destination"></g:message></label></th>
                        <th width="25%"><label><g:message code="app.periodicReport.executed.workflowState.label"></g:message></label></th>
                        <th width="15%"></th>
                        </thead>
                    </table>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                    <button type="button" class="btn btn-primary submitPublish"><g:message code="app.label.PublisherTemplate.createFull" default="Generate Full Document"/></button>
                </div>
            </div>

        </div>
    </div>

    <div id="publishReportModal" class="modal fade" role="dialog">
        <div class="modal-dialog">
            <form id="publishReportForm" enctype="multipart/form-data" method="post" action="${createLink(controller: 'pvp', action: 'updatePublisherReport')}${_csrf ? ("?" + _csrf?.parameterName + "=" + _csrf?.token) : ""}">

                <!-- Modal content-->
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <label class="modal-title modalHeader"><g:message code="app.label.PublisherTemplate.createFull" default="Generate Full Document"/></label>
                    </div>

                    <div class="modal-body">
                        <div class="row">
                            <div class="alert alert-danger alert-dismissible forceLineWrap publishReportError" role="alert" hidden="hidden">
                                <button type="button" class="close" name="publishReportErrorClose">
                                    <span aria-hidden="true">&times;</span>
                                    <span class="sr-only"><g:message code="default.button.close.label"/></span>
                                </button>
                                <p class="errorContent"></p>
                            </div>
                        </div>

                        <label><g:message code="app.label.name"/><span class="required-indicator">*</span></label>
                        <input name="name" class="form-control name" value="${executedConfigurationInstance.reportName}" maxlength="255">

                        <label class="m-t-10"><g:message code="app.label.PublisherTemplate.chooseFile" default="Choose File"/><span class="required-indicator">*</span></label>
                        <input type="hidden" name="reportId" value="${params.id}" id="publishReportName">
                        <div class="input-group" style="width: 100%;">
                            <input type="text" class="form-control" id="file_name2" readonly>
                            <label class="input-group-btn">
                                <span class="btn btn-primary inputbtn-height" style="border-radius:0px 16px 16px 0px;">
                                    <g:message code="scheduler.select"/>
                                    <input type="file" id="file_input2" name="file" accept=".docx" style="display: none;">
                                </span>

                            </label>
                        </div>

                        <label class="m-t-10"><g:message code="comment.textData.label"/><span class="required-indicator">*</span></label>
                        <textarea name="comment" class="form-control comment" id="publishReportComment" maxlength="4000"></textarea>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                        <button type="button" class="btn btn-primary publishReportSubmit"><g:message code="app.label.PublisherTemplate.upload.modal.button"/></button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <div id="publishReportUpdateModal" class="modal fade" role="dialog">
        <div class="modal-dialog">
            <form id="publishReportUpdateForm" enctype="multipart/form-data" method="post" action="${createLink(controller: 'pvp', action: 'updatePublisherReport')}${_csrf ? ("?" + _csrf?.parameterName + "=" + _csrf?.token) : ""}">

                <!-- Modal content-->
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <label class="modal-title modalHeader"><g:message code="app.update.button.label"/></label>
                    </div>

                    <div class="modal-body">
                        <div class="row">
                            <div class="alert alert-danger alert-dismissible forceLineWrap publishReportUpdateError" role="alert" hidden="hidden">
                                <button type="button" class="close" name="publishReportUpdateErrorClose">
                                    <span aria-hidden="true">&times;</span>
                                    <span class="sr-only"><g:message code="default.button.close.label"/></span>
                                </button>
                                <p class="errorContent"></p>
                            </div>
                        </div>

                        <label><g:message code="app.label.PublisherTemplate.chooseFile" default="Choose File"/><span class="required-indicator">*</span></label>
                        <input type="hidden" name="reportId" value="${params.id}">
                        <input type="hidden" name="id" value="${params.id}">

                        <div class="input-group" style="width: 100%;">
                            <input type="text" class="form-control" id="file_name3" readonly>
                            <label class="input-group-btn">
                                <span class="btn btn-primary inputbtn-height" style="border-radius:0px 16px 16px 0px;">
                                    <g:message code="scheduler.select"/>
                                    <input type="file" id="file_input3" name="file" accept=".docx" style="display: none;">
                                </span>
                            </label>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                        <button type="button" class="btn btn-primary publishReportUpdateSubmit"><g:message code="app.label.PublisherTemplate.upload.modal.button"/></button>
                    </div>
                </div>
            </form>
        </div>
    </div>



    <div id="publisherTemplateModal" class="modal fade" role="dialog">
        <div class="modal-dialog  modal-lg">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h5 class="modal-title modalHeader"><g:message code="app.label.PublisherTemplate.appName"/></h5>
                </div>

                <div class="modal-body">
                    <ul class="nav nav-tabs" role="tablist" style="border-bottom:none">
                        <li role="presentation" class="active"><a href="#addTemplate" class="addLibraryLink" aria-controls="addTemplate" role="tab" data-toggle="tab"><g:message code="app.label.PublisherTemplate.libraryTemplate"/></a>
                        </li>
                        <li role="presentation"><a href="#addFile" class="addFileLink" aria-controls="addFile" role="tab" data-toggle="tab"><g:message code="app.label.PublisherTemplate.uploadedTemplate"/></a>
                        </li>
                    </ul>
                    <div class="tab-content">
                        <div role="tabpanel" class="tab-pane active" id="addTemplate">
                            <form method="post" class="updatePublisherSectionForm" action="${createLink(controller: 'pvp', action: 'updatePublisherTemplate')}${_csrf ? ("?" + _csrf?.parameterName + "=" + _csrf?.token) : ""}">
                                <div id="publisherTemplateModalContent">

                                </div>
                                <div style="width: 100%;text-align: right">
                                <button type="submit" class="btn btn-primary "><g:message code="default.button.update.label"/></button>
                                <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>
                                </div>
                            </form>
                        </div>
                        <div role="tabpanel" class="tab-pane" id="addFile">
                            <form method="post" enctype="multipart/form-data" class="updatePublisherSectionForm" action="${createLink(controller: 'pvp', action: 'updatePublisherTemplate')}${_csrf ? ("?" + _csrf?.parameterName + "=" + _csrf?.token) : ""}">
                                <div id="publisherFileModalContent">

                                </div>
                                <div style="width: 100%;text-align: right">
                                <button type="submit" class="btn btn-primary "><g:message code="default.button.update.label"/></button>
                                <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>

            </div>

        </div>
    </div>

    <div id="fillParametersModal" class="modal fade" role="dialog">
        <div class="modal-dialog  modal-lg">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h5 class="modal-title modalHeader"><g:message code="app.label.warning"/></h5>
                </div>

                <form method="post" class="updatePublisherSectionForm" data-evt-sbt='{"method": "showLoader", "params": []}'
                      action="${createLink(controller: 'pvp', action: 'updatePublisherTemplateAndGenerate')}${_csrf ? ("?" + _csrf?.parameterName + "=" + _csrf?.token) : ""}">
                    <div class="modal-body">
                        <b><g:message code="app.label.PublisherTemplate.emptyParameters"/></b>
                        <div id="fillParametersModalTemplateDiv">
                            <div id="fillParametersModalContent">
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="submit" class="btn btn-default btn-primary"><g:message code="app.label.PublisherTemplate.generate"/></button>
                        <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <div id="pendingModal" class="modal fade" role="dialog">
        <div class="modal-dialog  modal-lg">
            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h5 class="modal-title modalHeader"><g:message code="app.label.PublisherTemplate.generate.pendingParameters"/></h5>
                </div>
                <div class="modal-body">
                    <div class="list">
                        <ul class="nav nav-tabs" role="tablist">
                            <li role="presentation" class='active'><a href="#variablesTab" aria-controls="variablesTab" role="tab" data-toggle="tab"><g:message code="app.label.PublisherTemplate.pending.parameters"/><span class="pendingTotalParameters"></span>
                            </a></li>
                            <li role="presentation"><a href="#manualTab" aria-controls="manualTab" role="tab" data-toggle="tab"><g:message code="app.label.PublisherTemplate.manual"/><span class="pendingTotalManual"></span>
                            </a></li>
                            <li role="presentation"><a href="#commentTab" aria-controls="commentTab" role="tab" data-toggle="tab"><g:message code="app.label.PublisherTemplate.pending.comments"/><span class="pendingTotalComments"></span>
                            </a></li>
                        </ul>
                        <!-- Tab panes -->
                        <div class="tab-content">
                            <div role="tabpanel" class="tab-pane active" id="variablesTab">
                                <table class="table">
                                    <thead>
                                    <tr>
                                        <th><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.name"/></th>
                                        <th style="min-width: 220px !important;"><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.exactValue"/> <span class="glyphicon glyphicon-question-sign modal-link-" style="cursor:pointer" data-toggle="modal" data-target="#publisherHelpModal"></span></th>

                                    </tr>
                                    </thead>
                                    <tbody id="pendingParameterTable">
                                    </tbody>
                                </table>
                                <button type="button" class="btn btn-primary saveParamsAndGenerate"><g:message code="app.label.PublisherTemplate.generate.continue" default="Continue Generation"/></button>
                            </div>
                            <div role="tabpanel" class="tab-pane" id="manualTab">
                            </div>
                            <div role="tabpanel" class="tab-pane" id="commentTab">
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>

                </div>
            </div>

        </div>
    </div>
    <g:render template="/email/includes/copyPasteEmailModal"/>
    <g:render template="/configuration/includes/publisherQuestModal"/>
    <g:render template="/includes/widgets/warningTemplate"/>
    <g:render template="/includes/widgets/reportSubmission"/>
    <g:render template="/publisherTemplate/includes/publisherHelp" />
    <g:render template="/periodicReport/includes/submissionCapaModal"/>
    <g:render template="/configuration/includes/emailConfiguration" />
    <div id="emailToModal" style="display: none"></div>
</div>
</div>
<g:render template="/pvp/includes/composer"/>
</body>
