<%@ page import="com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.config.CaseLineListingTemplate; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper; com.google.gson.annotations.Until; com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.enums.ReportFormatEnum" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.viewCriteria.title"/></title>
    <style>
    .sectionsHeader {
        background-image: none;
        background-color: #D2D2D2;
    }

    td.remove {
        text-align: center;
        vertical-align: middle;
    }

    span.addSectionLink {
        margin-left: 10px;
    }

    span.addSectionLink img {
        height: 24px;
    }

    td.remove .removeIconBtn {
        cursor: pointer;
    }

    .removeIconBtn {
        cursor: pointer;
    }

    </style>
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
        var configurationPOIInputsParamsUrl = "${createLink(controller: 'configurationRest', action: 'getConfigurationPOIInputsParams',id: executedConfigurationInstance.id)}";
        var addNewSectionUrl = "${createLink(controller: 'periodicReport', action: 'saveSection')}";
        var removeSectionUrl = "${createLink(controller: 'periodicReport', action: 'removeSection')}";
        var cioms1Id = "${ReportTemplate.cioms1Id()}";
        var medWatchId = "${ReportTemplate.medWatchId()}";
        var validateValue="${createLink(controller: 'configuration', action: 'validateValue')}";

        var LABELS = {
            labelShowAdavncedOptions: "${message(code: 'add.header.title.and.footer')}",
            labelHideAdavncedOptions: "${message(code: 'hide.header.title.and.footer')}"
        }
        $(function ()
        {
            $("#successFlashMsg").on('click', function () {
                $(".successFlashMsg").hide();
            });
        });
    </script>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:javascript src="app/query/queryValueSelect2.js"/>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/addSection.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
</head>

<body>

<g:set var="column1Width" value="3"/>
<g:set var="column2Width" value="9"/>

<rx:container title="${message(code: 'app.label.report')}: ${applyCodec(encodeAs:'HTML',executedConfigurationInstance.reportName)}  ${params.boolean('isInDraftMode')?('('+message(code:'app.periodicReport.executed.draft.label')+')'):''}"
              options="${false}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigurationInstance}" var="theInstance"/>
    <g:render template="/includes/layout/inlineAlerts"/>
    <g:render template="includes/periodicReportCriteriaBody" model="${model}"/>
    <g:render template="/periodicReport/includes/addSection"/>
    <g:render template="/includes/widgets/confirmation"/>
    <g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"/>
    <g:render template="/includes/widgets/saveCaseSeries"/>
    <g:render template="/includes/widgets/commentsWidget"/>
    <g:render template="/oneDrive/downloadModal"/>
</rx:container>
</body>
