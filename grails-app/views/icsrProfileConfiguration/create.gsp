<%@ page import="com.rxlogix.config.ReportTemplate" %>
<head>
    <meta name="layout" content="main"/>
    <g:set var="titleName" value="${message(code: "app.icsrProfileConf.name.label")}"/>
    <title><g:message code="default.create.title" args="[titleName]"/></title>
    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";

        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList', params: [showXMLSpecific: true])}";
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
        var validateValue="${createLink(controller: 'configuration', action: 'validateValue')}";
        var cioms1Id = "${ReportTemplate.cioms1Id()}";
        var medWatchId = "${ReportTemplate.medWatchId()}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var hasConfigTemplateCreatorRole="${hasConfigTemplateCreatorRole}";
        var fetchEmailConfigurationUrl = "${createLink(controller: 'icsrProfileConfiguration', action: 'fetchEmailConfiguration')}";
        var isForIcsrProfile = "${isForIcsrProfile}";
        var fetchCalenderNamesUrl = "${createLink(controller: 'icsrProfileConfigurationRest', action: 'getIcsrCalendarNames')}";
        var ruleEvaluationListUrl = "${createLink(controller: 'icsrProfileConfigurationRest', action: 'getRuleEvaluationList')}";
        var ruleEvaluationValueUrl = "${createLink(controller: 'icsrProfileConfigurationRest', action: 'getRuleEvaluationValue')}";
        var isForIcsrReport = "${false}";
        var profileId = "${configurationInstance.id}";

        var LABELS = {
            labelShowAdavncedOptions : "${message(code:'add.description.label')}",
            labelHideAdavncedOptions : "${message(code:'hide.description.label')}"
        }
    </g:javascript>

    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/configuration/templateQueries.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/query/queryValueSelect2.js"/>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:javascript src="app/configuration/emailConfiguration.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <g:javascript>
        var ICSRPROFILECONF = {
             searchDataBasedOnParam: "${createLink(controller: 'unitConfigurationRest', action: 'searchDataBasedOnParam')}"
        }
    </g:javascript>
    <asset:javascript src="app/configuration/icsrProfile.js"/>
</head>
<body>
<div>
    <div class="col-sm-12">
        <div class="page-title-box">
            <div class="fixed-page-head">
                <div class="page-head-lt">
                    <div class="col-md-12">
                        <h5>${message(code: "app.label.icsr.profile.conf.icsrProfileConfiguration")} </h5>
                    </div>
                </div>

            </div>
        </div>
    </div>
</div>
<div class="rxmain-container rxmain-container-top">
%{--    <rx:container >--}%
        <div class="col-md-12 m-t-0">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

            <g:form name="icsrProfileConfigurationForm" method="post" action="save" autocomplete="off" data-isForIcsrProfile="${isForIcsrProfile}" data-evt-sbt='{"method": "onFormSubmit", "params": []}'>

                <g:render template="form" model="[configurationInstance:configurationInstance,fieldProfiles: fieldProfiles]"/>
                <g:hiddenField name="templateBlanks" id="templateBlanks" value="${templateBlanks}"/>
                <g:hiddenField name="queryBlanks" id="queryBlanks" value="${queryBlanks}"/>

                <div class="button mt-10 m-r-20 m-b-10 pull-right">
                    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "formSubmit", "params": []}' id="saveButton">${message(code: 'default.button.save.label')}</button>
                    <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["icsrProfileConfiguration", "index"]}'
                            id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                </div>
                <input name="blankValuesJSON" id="blankValuesJSON" value="${configurationInstance?.blankValuesJSON}"
                       hidden="hidden"/>
            </g:form>
        </div>

        <div>
            %{--<!-- Render the templateQuery template (_templateQuery.gsp) hidden so we can clone it -->--}%
            <g:render template='/templateQuery/templateQuery'
                      model="['templateQueryInstance': null, 'i': '_clone', 'hidden': true,  isForPeriodicReport: false, isForIcsrReport: false, isForIcsrProfile: true]"/>

            <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"/></div>

            <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"/></div>
            <div class="expression" hidden="hidden"><g:render template="/query/poiInputValue"/></div>
        </div>
   %{-- </rx:container>--}%
<g:render template="/includes/widgets/warningTemplate"/>
</body>
