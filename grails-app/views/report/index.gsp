<%@ page import="com.rxlogix.config.ExecutedConfiguration; com.rxlogix.enums.ReportFormatEnum" %>

<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.MyInbox.title" /></title>

    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/report/report.js"/>
    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:javascript src="app/workFlow.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>

    <g:javascript>
        var indexReportUrl = "${createLink(controller: 'reportResultRest', action:'executedPeriodicReports')}";
        var showReportUrl = "${createLink(controller: 'report', action: 'showFirstSection')}";
        var comparisonUrl= "${createLink(controller: "comparison", action: "createCopy")}";
        var updateStatusUrl = "${createLink(controller: 'report', action: 'updateStatus')}";
        var workflowJustificationUrl="${createLink(controller: 'workflowJustificationRest',action: 'index')}"
        var getSharedWith = "${createLink(controller: 'reportResultRest', action: 'getSharedWithUsers')}";
        var getEmailTo = "${createLink(controller: 'reportResultRest', action: 'getEmailToUsers')}";
        var targetStatesAndApplicationsUrl= "${createLink(controller: "periodicReport", action: "targetStatesAndApplications")}";
        var workflowJustificationConfirnUrl="${createLink(controller: 'workflowJustificationRest',action: 'save')}";
        var addEmailConfiguration="${createLink(controller: "report",action: "addEmailConfiguration")}";
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
        var listActionItemUrl = "${createLink(controller: 'actionItemRest', action: 'index')}";
        var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
        var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var checkDeleteForAllAllowedURL = "${createLink(controller: 'report', action: 'checkDeleteForAllAllowed')}";
        var hasDmsIntegration = false;
        var dmsFoldersUrl = "${createLink(controller: 'periodicReport', action: 'getDmsFolders')}";
        var addDmsConfiguration = "${createLink(controller: "report",action: "addDmsConfiguration")}";
        var LINKS = {
            toPDF : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.PDF], absolute: true)}",
            toExcel : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.XLSX], absolute: true)}",
            toWord : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.DOCX], absolute: true)}",
            toPowerPoint : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormatEnum.PPTX], absolute: true)}",
            toShare : "${createLink(controller: 'report', action: 'share')}",
            toEmail : "${createLink(controller: 'report', action: 'email')}",
            toArchive : "${createLink(controller: 'report', action: 'archive')}"
        }
         var toFavorite = "${createLink(controller: 'report', action: 'favorite')}";
         var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:ExecutedConfiguration.name])}";
         var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
         var hasAccessOnActionItem = false;
        <sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
            hasAccessOnActionItem = true;
        </sec:ifAnyGranted>
        var isAdmin = false;
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            isAdmin = true;
        </sec:ifAnyGranted>
    </g:javascript>
    <g:showIfDmsServiceActive>
        <asset:javascript src="app/configuration/dmsConfiguration.js"/>
        <g:javascript>
            hasDmsIntegration = true;
        </g:javascript>
    </g:showIfDmsServiceActive>
</head>
<body>
<div class="content">
    <div class="container">
        <div>
     <input type="hidden" value="${params.reportName}" id="reportName">
     <input type="hidden" value="${params.forPvq=="true"?"true":"false"}" id="forPvq">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfiguration}" var="theInstance"/>

            <rx:container title="${message(code: message(code:"app.label.myInbox"))}" options="${true}" filterButton="true" >
                <div class="topControls">
                    <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: ExecutedConfiguration.name]"/>
                    <g:render template="/includes/widgets/archiveFilter"/>
                </div>
        <div class="pv-caselist">
        <table id="rxTableReports" class="table table-striped pv-list-table dataTable no-footer" data-i-display-length="50" data-a-length-menu="[[50, 100, 200, 500], [50, 100, 200, 500]]" width="100%">
         <thead>
             <tr>
                 <th style="font-size: 16px; width: 4%;"><span style="top:4px;" class="glyphicon glyphicon-star" ></span></th>
                 <th style="min-width: 200px" class="reportNameColumn"><g:message code="app.label.reportName" /></th>
                 <th style="min-width: 200px" class="reportDescriptionColumn"><g:message code="app.label.description" /></th>
                 <th style="width: 70px"><g:message code="app.label.version" /></th>
                 <th style="width: 100px;"><g:message code="app.label.owner" /></th>
                 <th style="width: 150px;"><g:message code="app.label.generatedOn" /></th>
                 <th style="min-width: 100px"><g:message code="app.label.tag" /></th>
                 <th  style="width: 100px"><g:message code="app.periodicReport.executed.workflowState.label"/></th>
                 <th style="width: 0px;"></th>
                 <th style="width: 70px;"></th>
                 <th class="col-min-60" style="width: 80px;"><g:message code="app.label.action"/></th>
             </tr>
         </thead>
        </table>

        <g:form controller="report" data-evt-sbt='{"method": "submitForm", "params": []}'>
            <g:hiddenField name="relatedReports" id="relatedReports" value="${related}" />
            <g:hiddenField name="executedConfigId" />

            <g:render template="includes/sharedWithModal" />
            <g:render template="includes/emailToModal" />
            <g:render template="/report/includes/sendToDmsModal"/>
            <g:render template="/includes/widgets/errorTemplate" model="[messageBody:message(code: 'app.dms.config.error'), errorModalId:'dmsErrorModal']"/>
        </g:form>

        <g:render template="/query/workflowStatusJustification" model="[tableId:'rxTableReports', isPeriodicReport: false]"/>
    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>
  <g:render template="/actionItem/includes/actionItemModal" model="[]" />
  <g:render template="/includes/widgets/confirmation"/>
  <g:render template="/email/includes/copyPasteEmailModal" />
  <g:render template="/oneDrive/downloadModal"/>
   </rx:container>
</body>
        </div>
        </div>
    </div>
</div>
</body>
