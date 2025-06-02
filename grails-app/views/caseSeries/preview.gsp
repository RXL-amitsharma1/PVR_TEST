<!doctype html>
<head>
    <g:set var="entityName" value="Query Result" />
    <title><g:message code="default.preview.label" args="[entityName]"/></title>
    <meta name="layout" content="main"/>

    <g:javascript>
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
    </g:javascript>

    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/dateRange.js"/>
    <asset:javascript src="app/caseSeries.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/query/queryValueSelect2.js"/>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>
</head>

<body>
<div class="col-md-12">
    <rx:container title="${message(code: 'app.label.previewQueryResult')}">

        <g:render template="/includes/layout/flashErrorsDivs" bean="${seriesInstance}" var="theInstance"/>

        <g:form method="post" action="updatePreview" class="form-horizontal"  data-evt-sbt='{"method": "onFormSubmit", "params": []}'>

            <g:render template="includes/reportConfigurationSection" model="[seriesInstance: seriesInstance]"/>
            <g:hiddenField name="editable" id="editable" value="true"/>
            <g:hiddenField name="seriesId" value="${seriesInstance.id}" id="seriesId"/>
            <g:hiddenField name="version" id="version" value="${seriesInstance?.version}"/>

            <div class="button">
                <button name="save" class="btn btn-primary">
                    ${message(code: 'default.button.preview.label')}
                </button>
                <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["query", "index"]}'
                        id="cancelButton">${message(code: "default.button.cancel.label")}</button>
            </div>
        </g:form>
        <div>
            <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"/></div>
            <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"/></div>
            <div class="expression" hidden="hidden"><g:render template="/query/poiInputValue"/></div>
        </div>
    </rx:container>
</div>

</body>
</html>