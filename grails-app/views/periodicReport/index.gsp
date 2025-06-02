<%@ page import="com.rxlogix.enums.PeriodicReportTypeEnum; com.rxlogix.config.PeriodicReportConfiguration; com.rxlogix.config.ReportConfiguration; grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
   <title>
       <g:if test="${params.pvp}"><g:message code="app.PvpPeriodicReportLibrary.title"/></g:if>
       <g:else><g:message code="app.PeriodicReportLibrary.title"/></g:else>
   </title>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/configuration/periodicConfiguration.js"/>
    <g:javascript>
        var CONFIGURATION = {
             listUrl: "${createLink(controller: 'periodicReportConfigurationRest', action: 'index')}",
             deleteUrl: "${createLink(controller: 'periodicReport', action: 'delete')}",
             editUrl: "${createLink(controller: 'periodicReport', action: 'edit')}",
             viewUrl: "${createLink(controller: 'periodicReport', action: 'view')}",
             copyUrl: "${createLink(controller: 'periodicReport', action: 'copy')}",
             runUrl:"${createLink(controller: 'periodicReport', action: 'runOnce')}"
        };
        var toFavorite = "${createLink(controller: 'configuration', action: 'favorite')}";
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:PeriodicReportConfiguration.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
        var hasAccessOnActionItem = "${hasAccessOnActionItem}";
        var isPublisherReport = sessionStorage.getItem("module")=="pvp";
        var isPriorityRoleEnable = ${SpringSecurityUtils.ifAnyGranted("ROLE_RUN_PRIORITY_RPT")};
    </g:javascript>
    <style>
</style>
</head>
<body>
<div class="content">
    <div class="container">
        <div class="pv-caselist">
            <g:render template="/includes/layout/flashErrorsDivs"/>
<rx:container title="${message(code: "app.PeriodicReportLibrary.label")}" options="true" filterButton="true">

    <div class="topControls">
        <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: PeriodicReportConfiguration.name]"/>
        <div class="filterDiv">
            <g:select placeholder="Document Type" name="typeFilter" noSelection="['':'']" from="${com.rxlogix.enums.PeriodicReportTypeEnum.asList}" optionKey="key"
                      value="" class="form-control"/>
        </div>
        <div class="checkbox checkbox-primary pvpOnly" style="text-align: center;float: right; display: none">
            <g:checkBox id="allReportsFilter" name="allReportsFilter"/>
            <label style="font-weight: bold" for="allReportsFilter"><g:message code="app.label.allReportsFilter"/></label>
        </div>
    </div>

    <table id="rxTableConfiguration" class="table table-striped pv-list-table dataTable no-footer" width="100%">
        <thead class="filter-head">
        <tr>
            <th style="font-size: 16px; "><span style="top:4px;" class="glyphicon glyphicon-star" ></span></th>
            <th style="min-width: 170px" class="reportNameColumn"><g:message code="app.label.reportName"/></th>
            <th style="min-width: 170px" class="reportDescriptionColumn"><g:message code="app.label.description"/></th>
            <th style="width: 50px"><g:message code="app.label.runTimes"/></th>
            <th style="min-width: 90px"><g:message code="app.periodicReport.executed.reportingDestination.label"/></th>
            <th style="min-width: 90px"><g:message code="app.label.tag"/></th>
            <th style="width: 70px"><g:message code="app.label.qc" default="QCed"/></th>
            <th style="width: 140px;"><g:message code="app.label.dateCreated"/></th>
            <th style="width: 140px;"><g:message code="app.label.dateModified"/></th>
            <th style="width: 90px;"><g:message code="app.label.owner"/></th>
            <th style="width: 70px;"><g:message code="app.label.action"/></th>
        </tr>
        </thead>
    </table>

    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>
</rx:container>
        </div>
    </div>
</div>
</body>
