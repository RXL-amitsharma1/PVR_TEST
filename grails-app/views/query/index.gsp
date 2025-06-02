<%@ page import="com.rxlogix.config.SuperQuery; grails.plugin.springsecurity.SpringSecurityUtils" %>
<%@ page import="com.rxlogix.util.FilterUtil; com.rxlogix.enums.QueryTypeEnum" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.QueryLibrary.title"/></title>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/query/query.js"/>
    <g:javascript>
        var listQueriesUrl = "${createLink(controller: 'queryRest', action: 'list')}"
        var queryDeleteUrl = "${createLink(controller: 'query', action: 'delete')}"
        var queryEditUrl = "${createLink(controller: 'query', action: 'edit')}"
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}"
        var queryCopyUrl = "${createLink(controller: 'query', action: 'copy')}"
        var queryPreviewUrl = "${createLink(controller: 'caseSeries', action: 'preview')}"
        var runUrl = "${createLink(controller: 'configuration', action: 'create')}"
        var toFavorite = "${createLink(controller: 'query', action: 'favorite')}";
        var queryTypes = JSON.parse("${FilterUtil.buildEnumOptions(QueryTypeEnum)}");
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:SuperQuery.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
    </g:javascript>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs"/>
<rx:container title="${message(code: "app.QueryLibrary.label")}" options="${true}" filterButton="true">
    <div class="topControls" style="float: right;text-align: right;display: none">
        <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: SuperQuery.name]"/>
    </div>
    <div class="pv-caselist">
        <table id="rxTableQueries" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th style="font-size: 16px; width: 4%;"><span style="top:4px;" class="glyphicon glyphicon-star" ></span></th>
                <th style="width: 100px;"><g:message code="app.label.type"/></th>
                <th style="min-width: 200px"><g:message code="app.label.queryName"/></th>
                <th style="min-width: 200px"><g:message code="app.label.description"/></th>
                <th style="width: 70px"><g:message code="app.label.qc" default="QCed"/></th>
                <th style="width: 60px;"><g:message code="app.label.usage"/></th>
                <th style="width: 100px;"><g:message code="app.label.owner"/></th>
                <th style="width: 150px;"><g:message code="app.label.dateCreated"/></th>
                <th style="width: 150px;"><g:message code="app.label.dateModified"/></th>
                <th style="width: 150px;"><g:message code="app.label.lastExecuted"/></th>
                <th style="min-width: 150px"><g:message code="app.label.tag"/></th>
                <th style="width: 80px;"><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
    </div>
</rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>

        </div>
    </div>
</div>
</body>
