<%@ page import="com.rxlogix.config.IcsrReportConfiguration; com.rxlogix.config.ExecutedConfiguration; com.rxlogix.enums.ReportFormatEnum; grails.plugin.springsecurity.SpringSecurityUtils" %>

<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.IcsrReportLibrary.title" /></title>

    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/icsrConfiguration.js"/>
    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/workFlow.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>

    <g:javascript>
        var icsrReportsListUrl = "${createLink(controller: 'icsrReportConfigurationRest', action:'index')}";
        var showReportUrl = "${createLink(controller: 'report', action: 'showIcsrReport')}";
        var updateStatusUrl = "${createLink(controller: 'report', action: 'updateStatus')}";
        var workflowJustificationUrl="${createLink(controller: 'workflowJustification',action: 'index')}";
        var getSharedWith = "${createLink(controller: 'reportResultRest', action: 'getSharedWithUsers')}";
        var getEmailTo = "${createLink(controller: 'reportResultRest', action: 'getEmailToUsers')}";
        var targetStatesAndApplicationsUrl= "${createLink(controller: "periodicReport", action: "targetStatesAndApplications")}";
        var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustification',action: 'save')}";
        var addEmailConfiguration="${createLink(controller: "report",action: "addEmailConfiguration")}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var checkDeleteForAllAllowedURL = "${createLink(controller: 'report', action: 'checkDeleteForAllAllowed')}";
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:com.rxlogix.config.IcsrReportConfiguration.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
        var toFavorite = "${createLink(controller: 'icsrReport', action: 'favorite')}";
        var LINKS = {
            toShare : "${createLink(controller: 'report', action: 'share')}",
            toEmail : "${createLink(controller: 'report', action: 'email')}",
            deleteUrl: "${createLink(controller: 'icsrReport', action: 'delete')}",
             editUrl: "${createLink(controller: 'icsrReport', action: 'edit')}",
             viewUrl: "${createLink(controller: 'icsrReport', action: 'view')}",
             copyUrl: "${createLink(controller: 'icsrReport', action: 'copy')}",
             runUrl:"${createLink(controller: 'icsrReport', action: 'runOnce')}"
        };
        var isPriorityRoleEnable = ${SpringSecurityUtils.ifAnyGranted("ROLE_RUN_PRIORITY_RPT")};
    </g:javascript>
</head>
<body>
<div class="col-md-12">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfiguration}" var="theInstance"/>

    <rx:container title="${message(code: message(code:"app.IcsrReportLibrary.label"))}" options="${true}" filterButton="true" >
        <div class="topControls" style="float: right;text-align: right;display: none">
            <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: IcsrReportConfiguration.name]"/>
        </div>
    <div class="pv-caselist">
        <table id="icsrReportsList" class="table table-striped pv-list-table dataTable no-footer" data-i-display-length="50"
               data-a-length-menu="[[50, 100, 200, 500], [50, 100, 200, 500]]" width="100%">
            <thead>
            <tr>
                <th style="font-size: 16px"><span class="glyphicon glyphicon-star" ></span></th>
                <th style="min-width: 140px" class="reportNameColumn"><g:message code="app.label.reportName"/></th>
                <th style="min-width: 140px" class="reportDescriptionColumn"><g:message code="app.label.description"/></th>
                <th style="max-width: 40px"><g:message code="app.label.runTimes"/></th>
                <th style="min-width: 80px"><g:message code="app.periodicReport.executed.reportingDestination.label"/></th>
                <th style="min-width: 75px"><g:message code="app.label.tag"/></th>
                <th style="max-width: 50px"><g:message code="app.label.qc" default="QCed"/></th>
                <th style="width: 140px;"><g:message code="app.label.dateCreated"/></th>
                <th style="width: 140px;"><g:message code="app.label.dateModified"/></th>
                <th style="width: 90px;"><g:message code="app.label.owner"/></th>
                <th style="width: 70px;"><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
    </div>
    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>
</rx:container>

</div>
</body>
