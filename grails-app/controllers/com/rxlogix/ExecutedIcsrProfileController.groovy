package com.rxlogix

import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ReportResult
import com.rxlogix.user.User
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class ExecutedIcsrProfileController {

    def dynamicReportService
    def userService

    @Secured(['ROLE_ICSR_PROFILE_VIEWER'])
    def index() { }

    @Secured(['ROLE_ICSR_PROFILE_VIEWER'])
    def view(ExecutedIcsrProfileConfiguration executedConfiguration) {
        if (!executedConfiguration) {
            notFound()
            return
        }
        render(view: "show", model: [templateQueries: executedConfiguration.executedTemplateQueries, icsrProfileConfInstance: executedConfiguration,
                                             isExecuted     : true])
    }

    @Secured(['ROLE_ICSR_PROFILE_VIEWER'])
    def showResult(Long id) {
        ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = ExecutedIcsrProfileConfiguration.read(id)
        if (!executedIcsrProfileConfiguration) {
            notFound()
            return
        }
        // Reports not view able by a user will not be delivered to the inbox
        User currentUser = userService.getUser()
        if (!executedIcsrProfileConfiguration?.isViewableBy(currentUser)) {
            String reportName = dynamicReportService.getReportName(executedIcsrProfileConfiguration, false, params)
            flash.warn = message(code: "app.userPermission.message", args: [reportName, message(code: "app.label.report")])
            render(view: "index")
            return
        }
        params.exIcsrProfileId = executedIcsrProfileConfiguration.id
        forward(controller: 'icsrProfileConfiguration', action: 'viewCases')
    }

    @Secured(['ROLE_ICSR_PROFILE_VIEWER'])
    def show(ReportResult reportResult) {
        if (!reportResult) {
            notFound()
            return
        }
        boolean isInDraftMode = false
        String reportName = dynamicReportService.getReportName(reportResult, isInDraftMode, params)
        // Reports not view able by a user will not be delivered to the inbox
        User currentUser = userService.getUser()
        if (!reportResult?.isViewableBy(currentUser)) {
            flash.warn = message(code: "app.userPermission.message", args: [reportName, message(code: "app.label.report")])
            render(view: "index", model: [executedConfigurationInstance: reportResult.executedTemplateQuery.executedConfiguration,
                                          executedTemplateInstance     : reportResult.executedTemplateQuery.executedTemplate])
            return
        }
        ExecutedTemplateQuery executedTemplateQuery = reportResult.executedTemplateQuery
        params.exIcsrProfileId = executedTemplateQuery.executedConfigurationId
        forward(controller: 'icsrProfileConfiguration', action: 'viewCases')
    }

    protected void notFound() {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.executed.icsr.profile.configuration.menuItem'), params.id])
        redirect action: "index", method: "GET"
    }
}
