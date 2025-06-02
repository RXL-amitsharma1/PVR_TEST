<%@ page import="grails.util.Holders; com.rxlogix.user.User; com.rxlogix.enums.ReasonOfDelayAppEnum; groovy.json.JsonOutput; com.rxlogix.config.ReportTemplate; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="${configurationInstance?.pvqType?"app.configuration.autorca.title":"app.CreateReport.label"}"/></title>

    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var userSpecificDateRange = "${true}";
        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}?isQueryTargetReports=true";
        var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
        var reportingDestinationsUrl = "${createLink(controller: 'queryRest', action: 'getReportingDestinations')}";
        var queryEditUrl = "${createLink(controller: 'query', action: 'edit')}";

        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";

        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";
        var granularityForTemplateUrl = "${createLink(controller: 'template', action: 'granularityForTemplate')}";
        var reassessDateForTemplateUrl = "${createLink(controller: 'template', action: 'reassessDateForTemplate')}";
        var poiInputsForTemplateUrl = "${createLink(controller: 'template', action: 'poiInputsForTemplate')}";
        var caseListUrl = "${createLink(controller: 'caseList', action: 'index')}";
        var importExcel="${createLink(controller: 'configuration', action: 'importExcel')}";
        var validateValue="${createLink(controller: 'configuration', action: 'validateValue')}";
        var cioms1Id = "${ReportTemplate.cioms1Id()}";
        var medWatchId = "${ReportTemplate.medWatchId()}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var dmsFoldersUrl="${createLink(controller: 'periodicReport', action: 'getDmsFolders')}";
        var executedCaseSeriesListUrl = "${createLink(controller: 'executedCaseSeriesRest', action: 'getExecutedCaseSeriesList')}";
        var executedCaseSeriesItemUrl = "${createLink(controller: 'executedCaseSeriesRest', action: 'getExecutedCaseSeriesItem')}";
        var hasConfigTemplateCreatorRole="${hasConfigTemplateCreatorRole}";
        var isForIcsrProfile = "${false}";
        var isForIcsrReport = "${false}";

        var fetchDateRangeTypesUrl = "${createLink(controller: 'configurationRest', action: 'fetchDateRangeTypesForDatasource')}"
        var fetchEvaluateCaseDatesUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDatesForDatasource',params: [showAllversions:true])}";
        var fetchEvaluateCaseDateForSubmissionUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDateSubmissionForDatasource')}";
        var LABELS = {
            labelShowAdavncedOptions : "${message(code:'add.header.title.and.footer')}",
            labelHideAdavncedOptions : "${message(code:'hide.header.title.and.footer')}"
        }
        var sharedWithUserListUrl = "${createLink(controller: 'userRest', action: 'sharedWithUserListPvq')}";
        var sharedWithGroupListUrl = "${createLink(controller: 'userRest', action: 'sharedWithGroupListPvq')}";
        var sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
         var pvqType=false;
        <g:if test="${configurationInstance.pvqType}">
            <g:set var="reportExecutorService" bean="reportExecutorService"/>
            pvqType=true;
            var issueTypeList = ${raw(JsonOutput.toJson(reportExecutorService.getLateListForOwnerApp(ReasonOfDelayAppEnum.PVQ, true)?.collect { [name: it.textDesc?.replaceAll("<","&lt;")?.replaceAll(">","&gt;"), hidden: !!it.hiddenDate, id: it.id, rootCauseIds: it.rootCauseIds?.join(";")] }))};
            var rootCauseList = ${raw(JsonOutput.toJson(reportExecutorService.getRootCauseList(ReasonOfDelayAppEnum.PVQ, true)?.collect { [name: it.textDesc?.replaceAll("<","&lt;")?.replaceAll(">","&gt;"), hidden: !!it.hiddenDate, id: it.id, responsiblePartyIds: it.responsiblePartyIds?.join(";")] }))};
            var responsiblePartyList = ${raw(JsonOutput.toJson(reportExecutorService.getResponsiblePartyList(ReasonOfDelayAppEnum.PVQ, true)?.collect { [name: it.textDesc?.replaceAll("<","&lt;")?.replaceAll(">","&gt;"), hidden: !!it.hiddenDate, id: it.id] }))};
            var assignedToUserList=${raw(JsonOutput.toJson(com.rxlogix.user.User.findAllByEnabled(true).collect { [id: it.id, name: it.fullNameAndUserName?.replaceAll("<","&lt;")?.replaceAll(">","&gt;")] }))};
            var assignedToGroupList=${raw(JsonOutput.toJson(com.rxlogix.user.UserGroup.findAllByIsDeleted(false).collect { [id: it.id, name: it.name?.replaceAll("<","&lt;")?.replaceAll(">","&gt;")] }))};
        </g:if>
    </g:javascript>
    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/configuration/templateQueries.js"/>
    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/dateRange.js"/>
    <asset:javascript src="app/configuration/periodicReport.js"/>
    <asset:javascript src="app/query/queryValueSelect2.js"/>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:javascript src="app/configuration/emailConfiguration.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <g:showIfDmsServiceActive>
        <asset:javascript src="app/configuration/dmsConfiguration.js"/>
    </g:showIfDmsServiceActive>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <style>
    #eventModal .modal-content{padding-top:0px!important;padding-bottom:0px!important;}
    </style>
    <g:if test="${configurationInstance.pvqType}">
        <asset:javascript src="app/configuration/pvqAutoRca.js"/>
    </g:if>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5>${message(code: configurationInstance?.pvqType?"app.configuration.pvqcfg.title.create":"app.configuration.title")} </h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div class="mt-30 mainContentDiv">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

            <form id="configurationForm" class="field-enter-submit-off" name="configurationForm" method="post" autocomplete="off" data-evt-sbt='{"method": "onFormSubmit", "params": []}'>

                <g:render template="form" model="[configurationInstance: configurationInstance, sourceProfiles: sourceProfiles]"/>

                <g:hiddenField name="templateBlanks" id="templateBlanks" value="${templateBlanks}"/>
                <g:hiddenField name="queryBlanks" id="queryBlanks" value="${queryBlanks}"/>
                <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="createPage"/>
                <g:hiddenField name="editable" id="editable" value="true"/>
                %{--BEGIN: Button Bar  ==============================================================================================================--}%
                <div class="mt-10 m-r-20 m-b-10">
                    <div style="text-align: right">
                        <button type="button" class="btn btn-primary" data-evt-clk='{"method": "beforeConfigurationFormSubmitWarningIfAny", "params": ["${createLink(action: "run")}", true]}' id="saveAndRunButton">${message(code: 'default.button.saveAndRun.label')}</button>
                        <button type="button" class="btn btn-primary" data-evt-clk='{"method": "beforeConfigurationFormSubmitWarningIfAny", "params": ["${createLink(action: "save")}"]}' id="saveButton">${message(code: 'default.button.save.label')}</button>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["configuration", "${configurationInstance.pvqType?"listPvqCfg":"index"}"]}' id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                </div>
                </div>
                %{--END: Button Bar  ================================================================================================================--}%

                <input name="blankValuesJSON" id="blankValuesJSON" value="${configurationInstance?.blankValuesJSON}" hidden="hidden"/>
        </form>

            <div>
                %{--<!-- Render the templateQuery template (_templateQuery.gsp) hidden so we can clone it -->--}%
                <g:render template='/templateQuery/templateQuery' model="['templateQueryInstance':null,'i':'_clone','hidden':true, 'theInstance':configurationInstance]"/>

                <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"/></div>
                <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"/></div>
                <div class="expression" hidden="hidden"><g:render template="/query/poiInputValue"/></div>
            </div>
        </div>
    </div>
</div>
<g:render template="/configuration/includes/customExpressionModal"/>
<g:render template="/includes/widgets/warningTemplate"/>
</body>
