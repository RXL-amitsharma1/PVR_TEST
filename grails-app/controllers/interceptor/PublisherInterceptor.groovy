package interceptor

import grails.config.Config
import grails.core.support.GrailsConfigurationAware

class PublisherInterceptor implements GrailsConfigurationAware {

    def customMessageService
    Boolean isPublisherEnabled = false

    PublisherInterceptor() {
        match(controller: "gantt", action: '*')
        match(controller: "publisherCommonParameter", action: '*')
        match(controller: "publisherTemplate", action: '*')
        match(controller: "pvp", action: '*')
        match(controller: "wopi", action: '*')
    }

    void setConfiguration(Config cfg) {
        isPublisherEnabled = cfg.getProperty('pv.app.pvpublisher.enabled', Boolean, false)
    }

    boolean before() {
        // if module is enabled only then allow to access.
        if (!isPublisherEnabled) {
            flash.error = customMessageService.getMessage('pv.app.publisher.not.enabled')
            redirect(controller: 'dashboard')
            return false
        }
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
