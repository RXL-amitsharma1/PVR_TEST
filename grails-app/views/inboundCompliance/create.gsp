<%@ page import="com.rxlogix.enums.DateRangeValueEnum; groovy.json.JsonOutput; com.rxlogix.config.ReportTemplate; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.pvcentral.inbound.compliance.title"/></title>
    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var userSpecificDateRange = "${true}";
        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}?isQueryTargetReportsedit=true";
        var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
        var reportingDestinationsUrl = "${createLink(controller: 'queryRest', action: 'getReportingDestinations')}";

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
        var isForIcsrProfile = "${false}";

        var fetchDateRangeTypesUrl = "${createLink(controller: 'configurationRest', action: 'fetchDateRangeTypesForDatasource')}";
        var fetchEvaluateCaseDatesUrl = "${createLink(controller: 'autoReasonOfDelay', action: 'fetchEvaluateCaseDatesForDatasource')}";
        var fetchEvaluateCaseDateForSubmissionUrl = "${createLink(controller: 'autoReasonOfDelay', action: 'fetchEvaluateCaseDateSubmissionForDatasource')}";
        var publisherContributorsUrl = "${createLink(controller: 'userRest', action: 'getPublisherContributors')}";
        var userValuesUrl = "${createLink(controller: 'userRest', action: 'userListValue')}";
        var fetchCalenderNamesUrl = "${createLink(controller: 'icsrProfileConfigurationRest', action: 'getIcsrCalendarNames')}";
        var LABELS = {
            labelShowAdavncedOptions : "${message(code:'add.header.title.and.footer')}",
            labelHideAdavncedOptions : "${message(code:'hide.header.title.and.footer')}"
        }

    </g:javascript>
    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/inboundCompliance/inboundCompliance.js"/>
    <asset:javascript src="app/configuration/periodicReport.js"/>
    <asset:javascript src="app/inboundCompliance/queriesCompliance.js"/>
    <asset:javascript src="app/query/queryValueSelect2.js"/>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
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
                                <h5><g:message code="app.pvcentral.inbound.createCompliance.label"/></h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div class="hide-space mt-30">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

            <form id="configurationForm" name="configurationForm" method="post" autocomplete="off" data-evt-sbt='{"method": "onFormSubmit", "params": []}'>

                <g:render template="includes/form" model="[configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, isInboundCompliance: true]"/>
                <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="createPage"/>
                <g:hiddenField name="queryBlanks" id="queryBlanks" value="${queryBlanks}"/>
                <g:hiddenField name="editable" id="editable" value="true"/>

                %{--BEGIN: Button Bar  ==============================================================================================================--}%
                <div class="row">
                    <div class="col-md-12">
                        <div class="pull-right rxmain-container-top m-r-20">
                            <button type="button" class="btn btn-primary" data-evt-clk='{"method": "beforeConfigurationFormSubmitWarningIfAny", "params": ["${createLink(controller: "inboundCompliance", action: "save")}"]}' id="editUpdateButton">${message(code: 'default.button.save.label')}</button>
                            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["inboundCompliance", "list"]}' id="cancelButton">${message(code: "default.button.cancel.label")}</button>

                        </div>
                    </div>
                </div>

            </form>

            <div>
                <g:render template='includes/queryCompliance' model="['queryComplianceInstance':null, 'i':'_clone', 'hidden':true, 'isInboundCompliance' : true]"/>

                <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"/></div>
                <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"/></div>
                <div class="expression" hidden="hidden"><g:render template="/query/poiInputValue"/></div>
            </div>


        </div>
    </div>
</div>

<g:render template="/includes/widgets/warningTemplate"/>

</body>