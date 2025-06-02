<!doctype html>
<head>


    <meta name="layout" content="main"/>
    <g:set var="entityName" value="Case Series" />
    <title><g:message code="app.caseSeries.create.title"/></title>

    <g:javascript>
        var spotfireCheck = "${grailsApplication.config.pvreports.generateSpotfireWithoutProduct}";
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}";
        var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var queryEditUrl = "${createLink(controller: 'query', action: 'edit')}";
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action: 'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";
        var granularityForTemplateUrl = "${createLink(controller: 'template', action: 'granularityForTemplate')}";
        var reassessDateForTemplateUrl = "${createLink(controller: 'template', action: 'reassessDateForTemplate')}";
        var poiInputsForTemplateUrl = "${createLink(controller: 'template', action: 'poiInputsForTemplate')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var importExcel="${createLink(controller: 'configuration', action: 'importExcel')}";
        var validateValue="${createLink(controller: 'configuration', action: 'validateValue')}";
        var hasConfigTemplateCreatorRole="${hasConfigTemplateCreatorRole}";
    </g:javascript>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/dateRange.js"/>
    <asset:javascript src="app/query/queryValueSelect2.js"/>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/emailConfiguration.js"/>
    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:javascript src="app/caseSeries.js"/>
    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/dataAnalysis/caseDataAnalysis.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>



</head>

<body>
<div class="content ">
    <div class="container">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5>${message(code: "app.caseSeries.title")}</h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div>

%{--<rx:renderHeaderTitle code="app.caseSeries.title" />--}%
<div class="mt-30">
<g:render template="/includes/layout/flashErrorsDivs" bean="${seriesInstance}" var="theInstance"/>
<g:form name="configurationForm" class="field-enter-submit-off" method="post" action="save"   data-evt-sbt='{"method": "onFormSubmit", "params": []}'>

    <g:render template="includes/form" model="['mode':'create', seriesInstance:seriesInstance]" />
    <g:hiddenField name="editable" id="editable" value="true"/>

    <div class="button mt-10 m-b-10 pull-right">
        <button type="button" class="btn btn-primary report-create-button" data-evt-clk='{"method": "beforeCaseSeriesFormSubmitWarningIfAny", "params": ["${createLink(action: "saveAndRun")}"]}' id="saveAndRunButton">${message(code: 'default.button.saveAndRun.label')}</button>
        <button type="button" class="btn btn-primary" data-evt-clk='{"method": "beforeCaseSeriesFormSubmitWarningIfAny", "params": ["${createLink(action: "save")}"]}' id="saveButton">${message(code: 'default.button.save.label')}</button>
        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["caseSeries", "index"]}'
                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
    </div>
</g:form>
</div>
<div>

    <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"/></div>

    <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"/></div>
    <div class="expression" hidden="hidden"><g:render template="/query/poiInputValue"/></div>
</div>
        </div>
    </div>
</div>
<g:render template="/includes/widgets/warningTemplate"/>
</body>
</html>