<%@ page import="com.rxlogix.enums.ReportFormatEnum" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.viewResult.title"/></title>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highstock.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/modules/no-data-to-display.js"/>
    <asset:javascript src="vendorUi/highcharts/plugins/grouped-categories-1.3.2.js"/>
    <asset:javascript src="app/report/advancedViewerChart.js"/>
    <asset:javascript src="app/report/advancedViewer.js"/>
    <asset:javascript src="vendorUi/fuelux/fuelux.min.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="datatables/dataTables.info.js"/>

    <asset:javascript src="datatables/extendedDataTable.js"/>
    <asset:stylesheet src="datatables/extendedDataTable.css"/>
    <asset:javascript src="datatables/dataTables.columnResize.js"/>
    <asset:stylesheet src="datatables/dataTables.columnResize.css"/>
    <asset:javascript src="datatables/dataTables.fixedHeader.js"/>
    <asset:stylesheet src="datatables/dataTables.fixedHeader.css"/>

    <style>
#table_filter{display:none;}
.pv-caselist .dt-container {
    overflow-x: auto;
}
#exportTypes.export-type-list {
    left: initial;
    right: 0;
}
div.dt-container div.dt-layout-cell.dt-end {
    margin-bottom: 0 !important;
}
.dt-layout-row:first-child {
    padding-right: initial !important;
}
[id$="_wrapper"] {
    overflow-x: hidden !important;
}
[id$="_wrapper"] .dt-layout-table {
    overflow-x: auto !important;
    height: calc(100vh - 210px) !important;
}
</style>

</head>

<body>
<div class="report-breadcrums">
    <div class=" breadcrumbsDiv ">
        <g:if test="${breadcrumbs}">
            <g:each in="${breadcrumbs}" var="tpl" status="i">
                ${raw(i > 0 ? "-&gt;" : "")}
                <span>
                    <g:if test="${i < breadcrumbs.size() - 1}">
                        <a href="javascript:void(0)" class="backLink" data-id="${tpl.id}" data-href="${createLink(controller: tpl.controller, action: tpl.action)}">${tpl.name}</a>
                    </g:if>
                    <g:else>
                        ${tpl.name}
                    </g:else>
                </span>
            </g:each>
        </g:if>
    </div>
</div>
<div class="content m-t-5">
    <div class="container">

        <g:render template="cllContent" model="${pageScope.variables}"/>

        <form id="exportForm" method="post">
            <input type="hidden" name="filter" value="${params.filter}">
            <input type="hidden" name="direction" id="direction" >
            <input type="hidden" name="sort" id="sort" >
            <input type="hidden" name="searchData" id="searchData" >
            <input type="hidden" name="globalSearch" id="globalSearch" >
            <input type="hidden" name="rowIdFilter" id="rowIdFilter" >
            <input type="hidden" name="dynamic" value="true" >
        </form>
    </div>
</div>
</body>
</html>