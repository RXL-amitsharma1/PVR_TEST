<%@ page import="com.rxlogix.config.ReportTemplate; com.rxlogix.util.FilterUtil;com.rxlogix.enums.*" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.TemplateLibrary.title"/></title>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/template/template.js"/>
    <g:javascript>
        var TEMPLATE = {
            listUrl: "${createLink(controller: 'reportTemplateRest', action: 'list')}",
            editUrl: "${createLink(controller: 'template', action: 'edit')}",
            viewUrl: "${createLink(controller: 'template', action: 'view')}",
            copyUrl: "${createLink(controller: 'template', action: 'copy')}",
            deleteUrl: "${createLink(controller: 'template', action: 'delete')}",
            runUrl: "${createLink(controller: 'configuration', action: 'create')}",
            runIcsrUrl: "${createLink(controller: 'icsrReport', action: 'create')}",
            templateTypes: JSON.parse("${FilterUtil.buildTemplateTypeEnumOptions(TemplateTypeEnum)}")
        };
        var toFavorite = "${createLink(controller: 'template', action: 'favorite')}";
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:ReportTemplate.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
    </g:javascript>
    <style>
    #rxTableTemplates_wrapper>.dt-layout-row:first-child{
        margin-top: -38px;
        position: relative;
    }
    </style>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs"/>
            <rx:container title="${message(code: "app.TemplateLibrary.label")}" options="${true}" filterButton="true">
                <div class="topControls" style="float: right;text-align: right;display: none">
                    <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: ReportTemplate.name]"/>
                </div>


    <div class="pv-caselist">
        <table id="rxTableTemplates" class="able table-striped pv-list-table dataTable no-footer">
            <thead class="filter-head">
            <tr>
                <th style="font-size: 16px"><span class="glyphicon glyphicon-star" ></span></th>
                <th width="4%"><g:message code="app.label.type"/></th>
                <th width="8%"><g:message code="app.label.category"/></th>
                <th width="9%"><g:message code="app.label.templateName"/></th>
                <th width="11%"><g:message code="app.label.description"/></th>
                <th width="6%"><g:message code="app.label.qc"  default="QCed"/></th>
                <th width="4%"><g:message code="app.label.usage"/></th>
                <th width="10%"><g:message code="app.label.owner"/></th>
                <th width="10%"><g:message code="app.label.dateCreated"/></th>
                <th width="10%"><g:message code="app.label.dateModified"/></th>
                <th width="10%"><g:message code="app.label.lastExecuted"/></th>
                <th width="8%"><g:message code="app.label.tag"/></th>
                <th style="min-width: 80px;"><g:message code="app.label.action"/></th>
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
