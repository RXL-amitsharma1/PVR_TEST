package interceptor

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic

@CompileStatic
class CognosReportInterceptor implements GrailsConfigurationAware {

    Boolean isCongnosEnabled

    CognosReportInterceptor() {
        match(controller: "cognosReport", action: "*")
    }

    void setConfiguration(Config cfg) {
        isCongnosEnabled = cfg.getProperty('cognosReport.view.enabled', Boolean, false)
    }

    boolean before() {
        if (!isCongnosEnabled) {
            redirect(controller: 'dashboard', action: 'index')
            return false
        }
        true
    }
}
