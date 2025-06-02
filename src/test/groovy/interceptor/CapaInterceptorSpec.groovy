package interceptor

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class CapaInterceptorSpec extends Specification implements InterceptorUnitTest<CapaInterceptor> {

    void setup() {
        grailsApplication.config.pv.app.pvcentral.enabled = false
        grailsApplication.config.show.pvc.module = false
        grailsApplication.config.pv.app.pvquality.enabled = false
        grailsApplication.config.show.pvq.module = false

        interceptor.setConfiguration(grailsApplication.config)
    }

    void "test interceptor matching"() {
        when:
        withRequest(controller: "capa")

        then:
        interceptor.doesMatch()
    }
    void "Access denied when no module is enabled"() {
        when:
        withRequest(controller: "capa")

        then:
        !interceptor.before() && response.redirectedUrl == '/errors/forbidden'
    }

    void "Access  when all module is enabled"() {
        grailsApplication.config.pv.app.pvcentral.enabled = true
        grailsApplication.config.show.pvc.module = true
        grailsApplication.config.pv.app.pvquality.enabled = true
        grailsApplication.config.show.pvq.module = true
        when:
        withRequest(controller: "capa")

        then:
        !interceptor.before() && response.redirectedUrl == '/errors/forbidden'
    }

    void "Access granted when pvcentral and show.pvc.module are true"() {
        given:
        grailsApplication.config.pv.app.pvcentral.enabled = true
        grailsApplication.config.show.pvc.module = true
        interceptor.setConfiguration(grailsApplication.config)

        when:
        withRequest(controller: "capa")

        then:
        interceptor.before()
    }

    void "Access granted when pvquality and show.pvq.module are true"() {
        given:
        grailsApplication.config.pv.app.pvquality.enabled = true
        grailsApplication.config.show.pvq.module = true
        interceptor.setConfiguration(grailsApplication.config)

        when:
        withRequest(controller: "capa")

        then:
        interceptor.before()
    }

    void "Access denied when only pvcentral is true and show.pvc.module is false"() {
        given:
        grailsApplication.config.pv.app.pvcentral.enabled = true
        grailsApplication.config.show.pvc.module = false
        interceptor.setConfiguration(grailsApplication.config)

        when:
        withRequest(controller: "capa")

        then:
        !interceptor.before() && response.redirectedUrl == '/errors/forbidden'
    }

    void "Access denied when only show.pvc.module is true and pvcentral is false"() {
        given:
        grailsApplication.config.pv.app.pvcentral.enabled = false
        grailsApplication.config.show.pvc.module = true
        interceptor.setConfiguration(grailsApplication.config)

        when:
        withRequest(controller: "capa")

        then:
        !interceptor.before() && response.redirectedUrl == '/errors/forbidden'
    }

    void "Access denied when only pvquality is true and show.pvq.module is false"() {
        given:
        grailsApplication.config.pv.app.pvquality.enabled = true
        grailsApplication.config.show.pvq.module = false
        interceptor.setConfiguration(grailsApplication.config)

        when:
        withRequest(controller: "capa")

        then:
        !interceptor.before() && response.redirectedUrl == '/errors/forbidden'
    }

    void "Access denied when only show.pvq.module is true and pvquality is false"() {
        given:
        grailsApplication.config.pv.app.pvquality.enabled = false
        grailsApplication.config.show.pvq.module = true
        interceptor.setConfiguration(grailsApplication.config)

        when:
        withRequest(controller: "capa")

        then:
        !interceptor.before() && response.redirectedUrl == '/errors/forbidden'
    }
}
