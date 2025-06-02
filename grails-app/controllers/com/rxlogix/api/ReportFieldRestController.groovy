package com.rxlogix.api

import com.rxlogix.config.ReportField
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured('permitAll')
class ReportFieldRestController implements SanitizePaginationAttributes {

    def reportFieldService

    def index() {
        sanitize(params)
        params.sort == "dateCreated" ? params.sort = "name" : params.sort
        Integer reportFieldQueryCount, totalReportFieldCount
        List<Map> reportFields = []
        (reportFields,reportFieldQueryCount,totalReportFieldCount) = reportFieldService.fetchReportField(params)
        render([aaData: reportFields, recordsTotal: totalReportFieldCount, recordsFiltered: reportFieldQueryCount] as JSON)
    }
}
