<%@ page import="com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.EditReport.title"/></title>
    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";

        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList', params: [showXMLSpecific: true])}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}";
        var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
        var reportingDestinationsUrl = "${createLink(controller: 'queryRest', action: 'getIcsrReportingDestinations')}";

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
        var importExcel="${createLink(controller: 'configuration', action: 'importExcel')}";
        var validateValue="${createLink(controller: 'configuration', action: 'validateValue')}";
        var dmsFoldersUrl="${createLink(controller: 'periodicReport', action: 'getDmsFolders')}";
        var cioms1Id = "${ReportTemplate.cioms1Id()}";
        var medWatchId = "${ReportTemplate.medWatchId()}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var hasConfigTemplateCreatorRole="${hasConfigTemplateCreatorRole}";
        var fetchDateRangeTypesUrl = "${createLink(controller: 'configurationRest', action: 'fetchDateRangeTypesForDatasource')}";
        var fetchEvaluateCaseDatesUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDatesForDatasource',params: [showAllversions:true])}";
        var fetchEvaluateCaseDateForSubmissionUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDateSubmissionForDatasource')}";
        var isForIcsrProfile = "${false}";
        var isForIcsrReport = "${true}";

        var LABELS = {
            labelShowAdavncedOptions : "${message(code:'add.header.title.and.footer')}",
            labelHideAdavncedOptions : "${message(code:'hide.header.title.and.footer')}"
        };
        var fetchDateRangeTypesUrl = "${createLink(controller: 'configurationRest', action: 'fetchDateRangeTypesForDatasource')}"
        var fetchEvaluateCaseDatesUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDatesForDatasource',params: [showAllversions:true])}";
        var fetchEvaluateCaseDateForSubmissionUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDateSubmissionForDatasource')}";
        var referenceProfileListUrl ="${createLink(controller: 'icsrProfileConfigurationRest', action: 'getReferenceProfileList')}";
        var referenceProfileTextUrl ="${createLink(controller: 'icsrProfileConfigurationRest', action: 'getIcsrProfileDescription')}";

    </g:javascript>

    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/dateRange.js"/>
    <asset:javascript src="app/configuration/icsrReport.js"/>
    <asset:javascript src="app/configuration/templateQueries.js"/>
    <asset:javascript src="app/query/queryValueSelect2.js"/>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:javascript src="app/configuration/emailConfiguration.js"/>
    <g:showIfDmsServiceActive>
        <asset:javascript src="app/configuration/dmsConfiguration.js"/>
    </g:showIfDmsServiceActive>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div >
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5>${message(code: "app.label.editReport")} </h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div class="hide-space">


    <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

    <form id="configurationForm" name="configurationForm" method="post" autocomplete="off" data-evt-sbt='{"method": "onFormSubmit", "params": []}'>

        <g:render template="form" model="[configurationInstance: configurationInstance, sourceProfiles: sourceProfiles]"/>
        <g:hiddenField name="editable" id="editable" value="true"/>
        <g:hiddenField name="id" value="${configurationInstance?.id}"/>
        <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="editPage"/>
        <g:if test="${params.fromTemplate}">
            <g:hiddenField name="fromTemplate"  value="true"/>
        </g:if>

        %{--BEGIN: Button Bar  ==============================================================================================================--}%
        <div class="pull-right rxmain-container-top m-r-20">
        <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate}">
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "beforePeriodicFormSubmitWarningIfAny", "params": ["${createLink(action: "disable")}"]}' id="disabledButton">${message(code:"default.button.unschedule.label")}</button>
        </g:if>
        <g:else>
            <button type="button" class="btn btn-primary report-edit-button hide" data-evt-clk='{"method": "beforePeriodicFormSubmitWarningIfAny", "params": ["${createLink(controller: "icsrReport", action: "run")}", true]}' id="editRunButton">${message(code: 'default.button.run.label')}</button>
        </g:else>
        <button type="button" class="btn btn-primary report-edit-button hide" data-evt-clk='{"method": "beforePeriodicFormSubmitWarningIfAny", "params": ["${createLink(controller: "icsrReport", action: "update")}"]}' id="editUpdateButton">${message(code: 'default.button.update.label')}</button>
        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["icsrReport", "index"]}' id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
        %{--END: Button Bar  ================================================================================================================--}%
        <input name="blankValuesJSON" id="blankValuesJSON" value="${configurationInstance?.blankValuesJSON}" hidden="hidden"/>
    </form>

    <div>
        %{--<!-- Render the templateQuery template (_templateQuery.gsp) hidden so we can clone it -->--}%
        <g:render template='/templateQuery/templateQuery' model="['templateQueryInstance':null,'i':'_clone','hidden':true, isForPeriodicReport: true]"/>
        %{--</tbody>--}%

        <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"/></div>
        <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"/></div>
        <div class="expression" hidden="hidden"><g:render template="/query/poiInputValue"/></div>
    </div>
        </div>
    </div>
</div>
<g:render template="/includes/widgets/warningTemplate"/>
</body>