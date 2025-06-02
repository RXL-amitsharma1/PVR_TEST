package interceptor

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic

@CompileStatic
class HealthInterceptor implements GrailsConfigurationAware {

    String healthCheckToken

    HealthInterceptor() {
        match(controller: "health", action: "*").excludes(controller: 'health', action: 'ping')
    }

    void setConfiguration(Config cfg) {
        healthCheckToken = cfg.getProperty('health.check.token', String)
    }

    boolean before() {
        healthCheckToken ? (params.id == healthCheckToken) : true
    }
}
