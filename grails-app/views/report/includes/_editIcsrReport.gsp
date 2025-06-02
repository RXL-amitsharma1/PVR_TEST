<%@ page import="com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.EditReport.title"/></title>
    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";

        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList', params: [showXMLSpecific: true])}";
        var reportingDestinationsUrl = "${createLink(controller: 'queryRest', action: 'getIcsrReportingDestinations')}";

        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";

        var importExcel="${createLink(controller: 'configuration', action: 'importExcel')}";
        var validateValue="${createLink(controller: 'configuration', action: 'validateValue')}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var hasConfigTemplateCreatorRole="${hasConfigTemplateCreatorRole}";
        var fetchDateRangeTypesUrl = "${createLink(controller: 'configurationRest', action: 'fetchDateRangeTypesForDatasource')}";
        var fetchEvaluateCaseDatesUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDatesForDatasource',params: [showAllversions:true])}";
        var fetchEvaluateCaseDateForSubmissionUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDateSubmissionForDatasource')}";
        var isForIcsrProfile = "${false}";

        var fetchDateRangeTypesUrl = "${createLink(controller: 'configurationRest', action: 'fetchDateRangeTypesForDatasource')}"
        var fetchEvaluateCaseDatesUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDatesForDatasource',params: [showAllversions:true])}";
        var fetchEvaluateCaseDateForSubmissionUrl = "${createLink(controller: 'configurationRest', action: 'fetchEvaluateCaseDateSubmissionForDatasource')}";

    </g:javascript>
    <asset:javascript src="app/configuration/icsrReport.js"/>
    <asset:javascript src="app/configuration/templateQueries.js"/>
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