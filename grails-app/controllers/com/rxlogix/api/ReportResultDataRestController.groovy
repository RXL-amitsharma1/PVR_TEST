package com.rxlogix.api
import com.rxlogix.config.ReportResult
import com.rxlogix.config.ReportResultData
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured('permitAll')
class ReportResultDataRestController extends RestfulController {
    def springSecurityService

    ReportResultDataRestController() {
        super(ReportResultData)
    }

    def show() {
        render(text: "{\"data\":${ReportResult.get(params.id).data?.decryptedValue}}", contentType: "application/json" ) // this will produce gzip'ed output
    }
}
