package interceptor

import grails.config.Config
import grails.core.support.GrailsConfigurationAware

class CentralInterceptor implements GrailsConfigurationAware {

    def customMessageService

    Boolean isPvCentralEnabled

    CentralInterceptor() {
        match(controller: "central", action: '*')
    }

    void setConfiguration(Config cfg) {
        isPvCentralEnabled = cfg.getProperty('pv.app.pvcentral.enabled', Boolean, false)
    }


    boolean before() {
        // when module enabled then only allow to access.
        if (!isPvCentralEnabled) {
            flash.error = customMessageService.getMessage('pv.app.central.not.enabled')
            redirect(controller: 'dashboard')
            return false
        }
        return true
    }

}
