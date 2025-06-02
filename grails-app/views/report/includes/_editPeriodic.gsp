<%@ page import="com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.EditReport.title"/></title>
    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var userSpecificDateRange = "${true}";
        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}?isQueryTargetReports=true";
        var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var reportConfigurationUrl = "${createLink(controller: 'dashboard', action: 'reportConfiguration')}";
        var updateSectionAndRunAjaxUrl = "${createLink(controller: 'configuration', action: 'updateSectionAndRunAjax')}";
        var reportingDestinationsUrl = "${createLink(controller: 'queryRest', action: 'getReportingDestinations')}";
        var publisherContributorsUrl = "${createLink(controller: 'userRest', action: 'getPublisherContributors')}";
        var userValuesUrl = "${createLink(controller: 'userRest', action: 'userListValue')}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";

        var importExcel="${createLink(controller: 'configuration', action: 'importExcel')}";
        var validateValue="${createLink(controller: 'configuration', action: 'validateValue')}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var hasConfigTemplateCreatorRole="${hasConfigTemplateCreatorRole}";
        var isForIcsrProfile = "${false}";

        var fetchDateRangeTypesUrl = "${createLink(controller: 'configurationRest', action: 'fetchDateRangeTypesForDatasource')}";
        var fetchEvaluateCaseDatesUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDatesForDatasource',params: [showAllversions:true])}";
        var fetchEvaluateCaseDateForSubmissionUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDateSubmissionForDatasource')}";

    </g:javascript>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/configuration/dateRange.js"/>
    <asset:javascript src="app/configuration/templateQueries.js"/>
    <asset:javascript src="app/configuration/periodicReport.js"/>
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
        <g:if test="${isEditUsingModal != undefined && isEditUsingModal}">
            <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="editConfigErrorDiv" hidden="true">
            </div>
        </g:if>
        <div class="hide-space">
        <g:if test="${isEditUsingModal == undefined || !isEditUsingModal}">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>
        </g:if>
    <form id="configurationForm" class="mt-40" name="configurationForm" method="post" autocomplete="off" data-evt-sbt='{"method": "onFormSubmit", "params": []}'>

        <g:render template="/periodicReport/form" model="[configurationInstance: configurationInstance]"/>
        <g:hiddenField name="editable" id="editable" value="true"/>
        <g:hiddenField name="id" value="${configurationInstance?.id}"/>
        <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="editPage"/>
        <g:if test="${params.fromTemplate}">
            <g:hiddenField name="fromTemplate"  value="true"/>
        </g:if>

        %{--BEGIN: Button Bar  ==============================================================================================================--}%
        <div class="pull-right rxmain-container-top m-r-20">
            <g:if test="${editable != undefined && !editable}">
                <button type="button" class="btn btn-primary report-edit-button" data-evt-clk='{"method": "saveAsEditConfig", "params": ["${createLink(controller: "periodicReport", action: "ajaxPeriodicSaveAsAndRun")}"]}' id="editUpdateButton">${message(code:"default.button.saveAsAndRun.label")}</button>
            </g:if>
            <g:else>
                <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate}">
                    <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "beforePeriodicFormSubmitWarningIfAny", "params": ["${createLink(controller: "periodicReport", action: "disable")}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}"]}' id="disabledButton">${message(code:"default.button.unschedule.label")}</button>
                </g:if>
                <g:else>
                    <button type="button" class="btn btn-primary report-edit-button hide" data-evt-clk='{"method": "beforePeriodicFormSubmitWarningIfAny", "params": ["${createLink(controller: "periodicReport", action: "run")}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}", true]}' id="editRunButton">${message(code: 'default.button.run.label')}</button>
                </g:else>
                <g:if test="${isEditUsingModal != undefined && isEditUsingModal}">
                    <button type="button" class="btn btn-primary report-edit-button hide" data-evt-clk='{"method": "saveEditConfig", "params": ["${createLink(controller: "periodicReport", action: "updatePeriodicAjaxCall")}"]}' id="editUpdateButton">${message(code: 'default.button.update.label')}</button>
                </g:if>
                <g:else>
                    <button type="button" class="btn btn-primary report-edit-button hide" data-evt-clk='{"method": "beforePeriodicFormSubmitWarningIfAny", "params": ["${createLink(controller: "periodicReport", action: "update")}"]}' id="editUpdateButton">${message(code: 'default.button.update.label')}</button>
                </g:else>
            </g:else>
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["periodicReport", "index"]}' id="cancelButton">${message(code: "default.button.cancel.label")}</button>
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
