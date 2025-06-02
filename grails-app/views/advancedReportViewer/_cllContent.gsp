<%@ page import="com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.util.ViewHelper; groovy.json.JsonOutput" %>
<g:set var="index" value="${index?:"0"}"/>
<style>
.dt-paging-button {
    margin: 0 !important;
    padding: 0 !important;
}

table.pvtUi {
    margin: 0 auto;
}
.chartSelected{
    -webkit-filter: drop-shadow( -3px -3px 2px rgba(0, 0, 0, .7));
    filter: drop-shadow( -3px -3px 2px rgba(0, 0, 0, .7));
}
.chartNotSelected{
    opacity: 0.3;
}
.dt-layout-row:first-child{
    margin-top:0;
    padding-right: 0px;
}
.dt-container {
    overflow-x: auto;
}
.dataTables_wrapper {
    overflow-x: auto;
}
.paginate_button {
    margin: 0 !important;
    padding: 0 !important;
}
table th{
    text-align: center !important;
    padding: 2px 4px !important;
}

table td {
    cursor: default;
    font-size: 13px !important;
    vertical-align: middle !important;
    padding: 2px 4px !important;
    line-height: 1.4 !important;
}
</style>
<asset:javascript src="/app/utils/pvr-common-util.js"/>
<asset:javascript src="/app/utils/pvr-filter-util.js"/>
<asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
<asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
<g:render template="warn" model="[warn:warn]"/>

<g:if test="${widget && !params.showpage}">
<g:if test="${params.boolean("showIndicators")}">
    <div id="indicator${index}" class="indicator" style="width: 100%">

    </div>
</g:if>
<g:else>
    <div id="chart${index}" class="tabChart" style="width: 100%; position: relative;"></div>
</g:else>

<div id="tableDiv${index}" class="pv-caselist"  style="${(params.boolean("showIndicators") || hideTable) ? 'display:none' : ''} ">
    <table id="table${index}" class="table chartWidgetTableDiv table-striped display order-column list-table pv-list-table dataTable no-footer">
    </table>
</div>
</g:if>
<g:else>

<g:if test="${showChartSheet}">
    <rx:container id="qualityTableContainer" title="${message(code: 'app.label.chart', default:"Chart")}: ${sectionName ?: ""}"  >
    <div id="chart${index}" class="tabChart" style="width: 100%; position: relative;height: 400px"></div>
        </rx:container>
</g:if>
<div id="tableDiv${index}" class="pv-caselist basicDataTable"  style="${(params.boolean("showIndicators") || hideTable) ? 'display:none' : ''} ">
<rx:container id="qualityTableContainer" title="${message(code: 'app.label.reportRequestField.section')}:  ${sectionName ?: ""}"  options="${true}"  customButtons="${g.render(template: "includes/interactiveTableButtons", model:pageScope.variables )}">
    <table id="table${index}" class="table chartWidgetTableDiv table-striped display order-column list-table pv-list-table dataTable no-footer">
    </table>
</rx:container>
</div>
</g:else>

<form id="drillDownForm${index}" method="get" action="${createLink(controller: "advancedReportViewer", action: (drilldownView?:"viewCll"))}">
    <input id="drillDownFormId${index}" type="hidden" name="id">
    <input type="hidden" name="parentId" value="${reportResultId}">
    <input id="drillDownFormField${index}" type="hidden" name="field">
    <input id="drillDownFormFilter${index}" type="hidden" name="filter">
</form>
<form id="saveAllForm${index}" method="post" action="${createLink(controller: "advancedReportViewer", action: "saveViewSettings")}">
    <input name="id" type="hidden" value="${params.id}">
    <input id="settings${index}" type="hidden" name="settings">
    <input name="filter" type="hidden" value="${params.filter}">
    <input name="cell" type="hidden" value="${params.cell}">
    <input name="reportResultId" type="hidden" value="${reportResultId}">
</form>

<form id="backForm${index}" method="get" >
    <input id="backFormId${index}" name="id" type="hidden" >
    <input id="backFormFilter${index}" name="filter" type="hidden" >
    <input name="back" type="hidden" value="true">
</form>

<script>
    var rowIdFilter${index};
    var widgetType${index} = "cll";
    var config${index} = {
        rowIdFilter:null,
        header: ${raw(JsonOutput.toJson(header))},
        rowColumns: ${raw(JsonOutput.toJson(rowColumns))},
        serviceColumns: ${raw(JsonOutput.toJson(serviceColumns))},
        groupColumns: ${raw(JsonOutput.toJson(groupColumns))},
        stacked:${raw(JsonOutput.toJson(stacked))},
        fieldTypeMap: ${raw(JsonOutput.toJson(fieldTypeMap))},
        getChartDataUrl: "${createLink(controller: 'advancedReportViewer', action: 'getChartWidgetDataAjax')}?id=${params.id}&click=clickCallBack${index}",
        showChartSheet:${params.boolean("showIndicators")?false:(showChartSheet?:false)},
        showIndicators:${""+!!params.boolean("showIndicators")},
        fieldsCodeNameMap:${raw(JsonOutput.toJson(fieldsCodeNameMap))},
        drillDownFilerColumns: ${raw(JsonOutput.toJson(drillDownFilerColumns))},
        sectionId:${sectionId},
        externalFilter: ${raw(params.filter?:"null")},
        ciomsLink: "${createLink(controller: 'report', action: 'drillDown')}",
        suppressLabels: ${raw(JsonOutput.toJson(suppressLabels))},
        EMPTY_LABEL: "${ViewHelper.getEmptyLabel()}",
        index:${index},
        widget:${widget?:false},
        templateHeader:${raw(JsonOutput.toJson(templateHeader))},
        reportResultId: ${reportResultId},
        reportrecordajaxurl: "${createLink(controller: 'advancedReportViewer', action: (template?.templateType == com.rxlogix.enums.TemplateTypeEnum.CASE_LINE?'cllAjax':'notCllAjax'))}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}",
        getSimilarCasesUrl: "${createLink(controller: 'advancedReportViewer', action: 'getSimilarCases')}",
        caseFormUrl: "${createLink(controller: 'advancedReportViewer', action: 'caseForm')}"
    };

</script>
<g:if test="${params.back}">
    <script>
        config${index}.externalFilter = JSON.parse(sessionStorage.getItem("breadcrumbs_${sectionId}_${reportResultId}"));
    </script>
</g:if>
<g:else>
    <script>
        sessionStorage.setItem("breadcrumbs_${sectionId}_${reportResultId}", JSON.stringify(config${index}.externalFilter))
    </script>
</g:else>
<script>
    var createdTable${index} = initAdvancedViewerCLL(config${index});

    function clickCallBack${index}(e) {
        e.stopPropagation();
        var pie = (e.point? (e.point.series.type=='pie'):(e.xAxis[0].axis.series[0].type=='pie'));
        var id = (e.point ? e.point : e.yAxis[0].axis.series[0].data[Math.round(e.xAxis[0].value)]).rowId;
        $("#chart${index} .chartSelected").removeClass("chartSelected");
        $("#chart${index} .chartNotSelected").removeClass("chartNotSelected");
        if (config${index}.rowIdFilter == id) {
            config${index}.rowIdFilter = null;
        } else {
            config${index}.rowIdFilter = id;
            if(!pie) {
                var yAxis = e.point ? e.point.series.chart.yAxis : e.yAxis
                $("#chart${index} .highcharts-point").addClass("chartNotSelected");
                for (var i in yAxis) {
                    var series = yAxis[i].series ? yAxis[i].series : yAxis[i].axis.series;
                    for (var j in series) {
                        var point = series[j].data[e.point ? e.point.x : Math.round(e.xAxis[0].value)];
                        if (point && point.graphic && point.graphic.element) {
                            point.graphic.element.classList.add("chartSelected")
                        }
                    }
                }
                $("#chart${index} .chartSelected").removeClass("chartNotSelected");
            }
        }
        createdTable${index}.reportDataTable.draw();
    }
</script>
