<%@ page import="com.rxlogix.config.WorkflowState; com.rxlogix.util.FilterUtil; com.rxlogix.enums.PeriodicReportTypeEnum; com.rxlogix.config.PeriodicReportConfiguration; com.rxlogix.config.ReportConfiguration; grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.aggregate.report.calendar.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/calendarReports.js"/>
    <g:javascript>
        var listUrl="${createLink(controller: "calendar", action: "reportsList")}";
        var isPublisherReport = sessionStorage.getItem("module")=="pvp";
        var editUrl= "${createLink(controller: 'periodicReport', action: 'edit')}";
        var editRrUrl= "${createLink(controller: 'reportRequest', action: 'edit')}";
        var viewUrl = "${createLink(controller: 'periodicReport', action: 'view')}";
        var viewRrUrl = "${createLink(controller: 'reportRequest', action: 'show')}";
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:PeriodicReportConfiguration.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
        var hasAccessOnActionItem = "${hasAccessOnActionItem}";
        var publisherContributorsUrl = "${createLink(controller: 'userRest', action: 'getPublisherContributors')}";
        var userValuesUrl = "${createLink(controller: 'userRest', action: 'userListValue')}";
        var editContributorsdUrl = "${createLink(controller: 'calendar', action: 'updateContributors')}";

        var reportTypes = JSON.parse("${FilterUtil.buildTemplateTypeEnumOptions(PeriodicReportTypeEnum)}");
        $.each(JSON.parse("${reportRequestTypes}"), function (index, type) {
            if (!reportTypes.find(function(reportType){return reportType.key === type.name})) {
                reportTypes.push({key: type.name, value: type.name});
            }
        });
        pvr.common_util.sortByTextField(reportTypes, 'value');

    </g:javascript>
</head>
<body>
<div class="content">
    <div class="container">
        <div class="pv-caselist">
            <g:render template="/includes/layout/flashErrorsDivs"/>
<rx:container title="${message(code: "app.aggregate.report.calendar")}" options="true" filterButton="true">
    <div class="body">
        <div id="report-request-conainter" class="list pv-caselist">


    <div class="pull-right"  style="cursor: pointer; text-align: right; position: relative;">
        <form id="excelExportForm" action="${createLink(controller: 'calendar', action: 'exportToExcel')}" >
%{--            <button type="button" class="btn btn-primary export"><g:message code="app.label.exportTo"/> <g:message code="app.reportFormat.XLSX"/></button>--}%
        </form>
        <a href="#" class="ic-sm pv-ic pv-ic-hover export" >
            <i class="md-download" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.ActionItem.export.label')}" style="color: #353d43;"></i>
        </a>

    </div>
    <div class="topControls">
            <div class="checkbox checkbox-primary pvpOnly" style="text-align: center;float: right; display: none">
                <g:checkBox id="allReportsFilter" name="allReportsFilter"/>
                <label style="font-weight: bold" for="allReportsFilter"><g:message code="app.label.allReportsFilter"/></label>
            </div>
            <div class="checkbox checkbox-primary" style="padding-top: 4px; text-align: center;float: right">
                <g:checkBox id="nextOnlyFilter" name="nextOnlyFilter"/>
                <label style="font-weight: bold" for="nextOnlyFilter"><g:message default="Show next scheduled reports only" code="app.label.nextOnlyFilter"/></label>
            </div>
        <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_VIEW">
            <div class="checkbox checkbox-primary" style="padding-top: 4px; text-align: center;float: right">
                <g:checkBox id="showReportRequestFilter" name="showReportRequestFilter"/>
                <label style="font-weight: bold" for="showReportRequestFilter"><g:message default="Show Report Requests" code="app.label.showReportRequestFilter"/></label>
            </div>
        </sec:ifAnyGranted>
            <div class="checkbox checkbox-primary" style="padding-top: 4px; text-align: center;float: right">
                <g:checkBox id="showReportFilter" checked="true" name="showReportFilter"/>
                <label style="font-weight: bold" for="showReportFilter"><g:message default="Show Reports" code="app.label.showReportFilter"/></label>
            </div>
    </div>
    <div class="pv-caselist">
    <table id="rxTableConfiguration" class="table table-striped pv-list-table dataTable no-footer" width="100%">
        <thead class="filter-head">
        <tr>
            <th class="reportNameColumn"><g:message code="app.label.reportType"/></th>
            <th class="reportDescriptionColumn"><g:message code="app.label.reportSubmission.cases.productName"/></th>
            <th><g:message code="app.label.reportName"/></th>
            <th><g:message code="app.label.reportRequest.reportingPeriodStart"/></th>
            <th><g:message code="app.label.reportRequest.reportingPeriodEnd"/></th>
            <th><g:message code="app.label.reportSubmission.dueDate"/></th>
            <th><g:message code="app.label.runDate"/></th>
            <th><g:message code="app.label.reportingDestinations"/></th>
            <th><g:message code="app.publisher.publisherContributors"/></th>
            <th><g:message code="quality.capa.comments.label"/></th>
            <th style="width: 80px;"><g:message code="app.label.action"/></th>
        </tr>
        </thead>
    </table>
    </div>
    <sec:ifAnyGranted roles="ROLE_PERIODIC_CONFIGURATION_CRUD">
    <div id="contributorsEditDiv" class="popupBox" style="position: absolute; width: 300px; display: none">
        <div class="destinations">
        <g:hiddenField name="primaryPublisherContributor" value=""/>
        <g:select name="publisherContributors" value="" class="form-control" multiple="multiple" from="${[]}"/>
        </div>
        <div style="margin-top: 10px; width: 100%; text-align: right;">
            <button class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
            <button class="btn pv-btn-grey cancelButton"><g:message code="default.button.cancel.label"/></button>
        </div>
    </div>
    </sec:ifAnyGranted>
        </div>
    </div>
</rx:container>
        </div>
    </div>
</div>
<g:render template="/includes/widgets/commentsWidget"/>

</body>
