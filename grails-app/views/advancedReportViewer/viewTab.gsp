<%@ page import="com.rxlogix.enums.ReportFormatEnum; com.rxlogix.util.ViewHelper; groovy.json.JsonOutput" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title><g:message code="app.viewResult.title"/></title>

    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highstock.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/modules/map.js"/>
    <asset:javascript src="vendorUi/highcharts/modules/world.js"/>
    <asset:javascript src="vendorUi/highcharts/modules/no-data-to-display.js"/>
    <asset:javascript src="vendorUi/highcharts/plugins/grouped-categories-1.3.2.js"/>
    <asset:javascript src="app/report/advancedViewerChart.js"/>
</head>

<body>

<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <div class="row" style="padding-top: 0">
                <div class="col-md-9">
                    <label class="rxmain-container-header-label ">${message(code: 'app.label.report')}: ${applyCodec(encodeAs: 'HTML', reportName)}</label>

                </div>
                <div class="col-md-3" style="text-align: right">
                    <g:link class="btn btn-success btn-xs  ${template.isNotExportable(ReportFormatEnum.PDF) ? 'disabled' : ''}"
                            controller="report" action="show" params="[id: params.id, outputFormat: ReportFormatEnum.PDF.name(), isInDraftMode: params.isInDraftMode, paperReport: false]"
                            data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.pdf')}">
                        <span class="fa fa-file-pdf-o"></span></g:link>

                    <g:link class="btn btn-success btn-xs  ${template.isNotExportable(ReportFormatEnum.XLSX) ? 'disabled' : ''}"
                            controller="report" action="show" params="[id: params.id, outputFormat: ReportFormatEnum.XLSX.name(), isInDraftMode: params.isInDraftMode, paperReport: false]"
                            data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.excel')}">
                        <span class="fa fa-file-excel-o"></span></g:link>

                    <g:link class="btn btn-success btn-xs  ${template.isNotExportable(ReportFormatEnum.DOCX) ? 'disabled' : ''}"
                            controller="report" action="show" params="[id: params.id, outputFormat: ReportFormatEnum.DOCX.name(), isInDraftMode: params.isInDraftMode, paperReport: false]"
                            data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.word')}">
                        <span class="fa fa-file-word-o"></span></g:link>

                    <g:link class="btn btn-success btn-xs  ${template.isNotExportable(ReportFormatEnum.PPTX) ? 'disabled' : ''}"
                            controller="report" action="show" params="[id: params.id, outputFormat: ReportFormatEnum.PPTX.name(), isInDraftMode: params.isInDraftMode, paperReport: false]"
                            data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.powerpoint')}">
                        <span class="fa fa-file-powerpoint-o"></span></g:link>
                    </button>
                </div>
            </div>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <g:render template="tabContent" model="${pageScope.variables}"/>
        </div>
    </div>
</div>

</body>
</html>
