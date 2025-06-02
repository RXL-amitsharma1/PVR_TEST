package com.reports
import com.rxlogix.util.ViewHelper

class RxContainerTagLib {
    static namespace = "rx"

    def container = { attrs, body ->
        def title = attrs.title ? attrs.title : ''
        def options = attrs.options
        def closeButton = attrs.closeButton
        def customButtons = attrs.customButtons
        def filterButton = attrs.filterButton
        def columnFilters = attrs.columnFilters
        def isAuditReport=attrs.isAuditReport

        out << buildContainer(title, options, body, closeButton, customButtons, filterButton, columnFilters, isAuditReport)
    }

    def buildContainer(title, options, body, closeButton, customButtons, filterButton = null, columnFilters = null, isAuditReport = null) {
        def content = '<div class="rxmain-container">' +
                '<div class="rxmain-container-inner">' +
                '<div class="rxmain-container-row rxmain-container-header">' +
                '<div class="dropdown">' +
                '<label class="rxmain-container-header-label" style="display: inline" title="' + "${title}" + '">'
        content += "${title.length() > 110 ? title.substring(0,110) + '...' : title}"
        content += '</label>'
        if (closeButton) {
            content += '<i class="pull-right md md-close md-lg rxmain-dropdown-settings" data-dismiss="modal"></i>'
        }

        if (options) {
            content += '<i class="pull-right dropdown-toggle md md-list md-lg rxmain-dropdown-settings" id="dropdownMenu1" data-toggle="dropdown" title="' + ViewHelper.getMessage('app.label.dropdown') + '"></i>' +
                    '<div class="pull-right dropdown-menu" aria-labelledby="dropdownMenu1">' +
                    '<div class="rxmain-container-dropdown">' +
                    '<div>' +
                    '<table id="tableColumns" class="table table-condensed rxmain-dropdown-settings-table">' +
                    '<thead>' +
                    '<tr>' +
                    '<th>'
            content += message(code: 'app.label.name')
            content += '</th>' +
                    '<th>'
            content += message(code: 'app.label.show')
            content += '</th>' +
                    '</tr>' +
                    '</thead>' +
                    '</table>' +
                    '</div>' +
                    '</div>' +
                    '</div>'
        }
        if (filterButton) {
            content += '<i class="pull-right fa fa-filter lib-filter rxmain-dropdown-settings pt-3 p-r-5" title="' + ViewHelper.getMessage('app.label.enable.filter') + '"></i>'
        }
        if (columnFilters) {
            content += '<i class="pull-right md-lg md-filter rxmain-dropdown-settings column-filter-toggle" title="Column Level Filters"></i>'
        }
        if (customButtons) {
            content += customButtons;
        }
        if (isAuditReport) {
            content += '<span tabindex="0" class="pull-right pos-rel m-r-15" style="cursor: pointer" title="' + ViewHelper.getMessage('app.label.exportTo') + '">' +
                    '                    <span class="dropdown-toggle exportPanel" data-toggle="dropdown" >' +
                    '                        <i class="md md-export md-lg blue-1 font-22 lh-1"></i>' +
                    '                        <span class="caret hidden"></span>' +
                    '                    </span>' +
                    '                    <ul class="dropdown-menu export-type-list" id="auditReportPopUp" style="min-width: 150px;padding:10px ">' +
                    '                        <strong class="font-12 title-spacing">Excel</strong>' +
                    '                        <li>' +
                    '                            <a href="/reports/auditLogEvent/generateAuditLogReportFile" style="margin-right: 20px; text-transform: none;" class="exportAudit">' +
                    '                                <img src="/reports/assets/excel.gif" class="excel-icon" height="16" width="16"> ' + message(code: 'auditLog.label.details') +
                    '                            </a>' +
                    '                        </li>' +
                    '                        <li>' +
                    '                            <a href="/reports/auditLogEvent/exportExcel" style="margin-right: 20px; text-transform: none;" class="exportAudit"' +
                    '                                  >' +
                    '                                <img src="/reports/assets/excel.gif" class="excel-icon" height="16" width="16"> ' + message(code: 'auditLog.label') +
                    '                            </a>' +
                    '                        </li>' +
                    '                        <strong class="font-12 title-spacing">PDF</strong>' +
                    '                        <li>' +
                    '                            <a href="/reports/auditLogEvent/exportPdf" style="margin-right: 20px; text-transform: none;" class="exportAudit">' +
                    '                                <img src="/reports/assets/pdf-icon.png" class="pdf-icon" height="16" width="16"> ' + message(code: 'auditLog.label') +
                    '                            </a>' +
                    '                        </li>' +
                    '                    </ul>' +
                    '                </span>'

        }
        content += '</div>' +
                '</div>' +
                '<div class="rxmain-container-content">'
        content += "${body()}"
        content += '</div>' +
                '</div>' +
                '</div>'
    }
    def search = { attrs, body ->
        def searchID = attrs.searchID ? attrs.searchID : ''
        def placeholder = attrs.placeholder
        def JSONUrl = attrs.JSONUrl

        out << """
            <input type="hidden" id="${searchID}_JSONUrl" value="${JSONUrl}"/>
            <input type="hidden" id="${searchID}.id" name="${searchID}.id"  />
            <div class="right-inner-addon">
                <i class="glyphicon glyphicon-search"></i>
                <input type="search" class="form-control" id="${searchID}" placeholder="${placeholder}"/>
            </div>
        """
    }

}
