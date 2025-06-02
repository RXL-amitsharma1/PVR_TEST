<%@ page import="com.rxlogix.config.Configuration; com.rxlogix.config.ReportConfiguration; grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ReportLibrary.title"/></title>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/configuration/configuration.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <g:javascript>
        var CONFIGURATION = {
             listUrl: "${createLink(controller: 'configurationRest', action: 'list')}",
             deleteUrl: "${createLink(controller: 'configuration', action: 'delete')}",
             editUrl: "${createLink(controller: 'configuration', action: 'edit')}",
             viewUrl: "${createLink(controller: 'configuration', action: 'view')}",
             copyUrl: "${createLink(controller: 'configuration', action: 'copy')}",
             runUrl:"${createLink(controller: 'configuration', action: 'runOnce')}"
        };
        var toFavorite = "${createLink(controller: 'configuration', action: 'favorite')}";
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:Configuration.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
        var isPriorityRoleEnable = ${SpringSecurityUtils.ifAnyGranted("ROLE_RUN_PRIORITY_RPT")};
    </g:javascript>
</head>

<body>
<div class="content">
    <div class="container ">
        <div class="pv-caselist">
            <g:render template="/includes/layout/flashErrorsDivs"/>
<rx:container title="${message(code: "app.ReportLibrary.label")}" options="true" filterButton="true">
    <div class="topControls" style="float: right;text-align: right;display: none">
    <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: Configuration.name]"/>
    </div>
    <table id="rxTableConfiguration" class="table table-striped pv-list-table dataTable no-footer">
        <thead class="filter-head">
        <tr>
            <th style="font-size: 16px; width: 4%;"><span style="top:4px;" class="glyphicon glyphicon-star" ></span></th>
            <th style="min-width: 200px" class="reportNameColumn"><g:message code="app.label.reportName"/></th>
            <th style="min-width: 200px" class="reportDescriptionColumn"><g:message code="app.label.description"/></th>
            <th style="width: 70px"><g:message code="app.label.runTimes"/></th>
            <th style="min-width: 150px"><g:message code="app.label.tag"/></th>
            <th style="width: 70px"><g:message code="app.label.qc" default="QCed"/></th>
            <th style="width: 150px;"><g:message code="app.label.dateCreated"/></th>
            <th style="width: 150px;"><g:message code="app.label.dateModified"/></th>
            <th style="width: 100px;"><g:message code="app.label.owner"/></th>
            <th style="width: 80px;"><g:message code="app.label.action"/></th>
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
