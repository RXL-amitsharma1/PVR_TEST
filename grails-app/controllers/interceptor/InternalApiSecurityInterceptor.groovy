package interceptor

import com.rxlogix.Constants
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic


@CompileStatic
class InternalApiSecurityInterceptor {

    SpringSecurityService springSecurityService

    InternalApiSecurityInterceptor() {
        match(controller: "actionItemRest|caseSeriesRest|configurationRest|executedCaseSeriesRest|fieldProfileRest|notificationRest|periodicReportConfigurationRest|reportFieldRest|reportRequestRest|reportResultDataRest|reportResultRest|reportSubmissionRest|reportTemplateRest|userGroupRest|commentRest|productDictionary|eventDictionary|studyDictionary|dictionaryGroup|icsrReportConfigurationRest|unitConfigurationRest|icsrCaseTrackingRest|icsrProfileConfigurationRest", action: "*")
        match(controller: "queryRest", action: "*")
                .except(action: "getQueryNameDescription")
        match(controller: "userRest", action: "*").except(action: "keepAlive")
        match(controller: "notificationRest", action: "*")
    }

    boolean before() {
        if (!springSecurityService.isLoggedIn()) {
            render([(Constants.SESSION_TIME_OUT): true] as JSON)
            return false
        }
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
