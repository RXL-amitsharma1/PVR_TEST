package interceptor


import grails.config.Config
import grails.core.support.GrailsConfigurationAware

class QualityInterceptor implements GrailsConfigurationAware {

    def customMessageService
    Boolean isPvQualityEnabled = false
    Boolean isPvCentralEnabled = false

    QualityInterceptor() {
        match(controller: "quality", action: '*')
                .except(action: "importExcel")
    }

    void setConfiguration(Config cfg) {
        isPvQualityEnabled = cfg.getProperty('pv.app.pvquality.enabled', Boolean, false)
        isPvCentralEnabled = cfg.getProperty('pv.app.pvcentral.enabled', Boolean, false)
    }


    boolean before() {
        // when module enabled then only allow to access.
        if (!isPvQualityEnabled && !isPvCentralEnabled) {
            flash.error = customMessageService.getMessage('pv.app.quality.not.enabled')
            redirect(controller: 'dashboard')
            return false
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}