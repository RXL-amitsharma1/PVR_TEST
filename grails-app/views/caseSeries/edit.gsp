<!doctype html>
<head>
    <g:set var="entityName" value="Case Series" />
    <title><g:message code="app.caseSeries.edit.title"/></title>
    <meta name="layout" content="main"/>

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
    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/dateRange.js"/>
    <asset:javascript src="app/caseSeries.js"/>
    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/emailConfiguration.js"/>
    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/query/queryValueSelect2.js"/>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:javascript src="app/dataAnalysis/caseDataAnalysis.js"/>
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
                                <h5>${message(code: "app.label.editCaseSeries")} </h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div class="m-t-30">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${seriesInstance}" var="theInstance"/>

            <g:form method="post"  name="configurationForm" action="update" data-evt-sbt='{"method": "onFormSubmit", "params": []}'>

                <g:render template="includes/form" model="['mode':'edit', seriesInstance:seriesInstance]" />
                <g:hiddenField name="editable" id="editable" value="true"/>
                <g:hiddenField name="version" id="version" value="${seriesInstance?.version}"/>

                <div class="button text-right m-t-10 m-b-10">
                    <g:if test="${seriesInstance?.isEnabled && seriesInstance?.nextRunDate}">
                        <button type="button" class="btn pv-btn-grey"
                                data-evt-clk='{"method": "beforeCaseSeriesFormSubmitWarningIfAny", "params": ["${createLink(action: "disable")}"]}'
                                id="disableButton">${message(code: "default.button.unschedule.label")}</button>
                    </g:if>
                    <g:else>
                        <button type="button" class="btn btn-primary report-create-button"
                                data-evt-clk='{"method": "beforeCaseSeriesFormSubmitWarningIfAny", "params": ["${createLink(action: "updateAndRun")}"]}'
                                id="saveAndRunButton">${message(code: 'default.button.run.label')}</button>
                    </g:else>
                    <button type="button" class="btn btn-primary" data-evt-clk='{"method": "beforeCaseSeriesFormSubmitWarningIfAny", "params": ["${createLink(action: "update")}"]}' id="saveButton">${message(code: 'default.button.update.label')}</button>
                    <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["caseSeries", "index"]}'
                            id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                </div>
            </g:form>
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