<%@ page import="com.rxlogix.enums.ReportFormatEnum" %>
<span class="d-inline pull-right" style="margin-right: 10px;">
    <a href="javascript:void(0);" class='ic-sm pv-ic pv-ic-hover dropdown-toggle' data-toggle="dropdown" title="${message(code: 'app.label.exportTo')}"><i class='md md-export'></i></a>
    <ul class="dropdown-menu export-type-list" id="exportTypes" >
        <li><g:link class="exportButton ${template.isNotExportable(ReportFormatEnum.PDF) ? 'disabled' : ''}"
                    controller="report" action="show" params="[id: reportResultId, outputFormat: ReportFormatEnum.PDF.name(), isInDraftMode: params.isInDraftMode, paperReport: false]"
                    data-tooltip="tooltip" data-placement="bottom">
            <i class="md md-file-pdf"></i> ${message(code: 'save.as.pdf')}</g:link></li>
        <li><g:link class="exportButton ${template.isNotExportable(ReportFormatEnum.XLSX) ? 'disabled' : ''}"
                    controller="report" action="show" params="[id: reportResultId, outputFormat: ReportFormatEnum.XLSX.name(), isInDraftMode: params.isInDraftMode, paperReport: false]"
                    data-tooltip="tooltip" data-placement="bottom" >
            <i class="md md-file-excel"></i> ${message(code: 'save.as.excel')}</g:link></li>
        <li><g:link class="exportButton ${template.isNotExportable(com.rxlogix.enums.ReportFormatEnum.DOCX) ? 'disabled' : ''}"
                    controller="report" action="show" params="[id: reportResultId, outputFormat: ReportFormatEnum.DOCX.name(), isInDraftMode: params.isInDraftMode, paperReport: false]"
                    data-tooltip="tooltip" data-placement="bottom">
            <i class="md md-file-word"></i> ${message(code: 'save.as.word')}</g:link></li>
        <li><g:link class="exportButton ${template.isNotExportable(ReportFormatEnum.PPTX) ? 'disabled' : ''}"
                    controller="report" action="show" params="[id: reportResultId, outputFormat: ReportFormatEnum.PPTX.name(), isInDraftMode: params.isInDraftMode, paperReport: false]"
                    data-tooltip="tooltip" data-placement="bottom">
            <i class="md md-file-powerpoint"></i> ${message(code: 'save.as.powerpoint')}</g:link></li>

    </ul>
</span>
<div id="searchtTable" class="d-inline pull-right" style="margin-right: 15px;">
    <label class="m-l-10">${message(code: 'default.button.search.label')}</label>
    <input type="search" class="form-control d-initial width-auto input-sm globalSearch"  placeholder="Search">
</div>