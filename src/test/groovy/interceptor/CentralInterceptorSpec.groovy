package interceptor

import com.rxlogix.CustomMessageService
import grails.testing.web.interceptor.InterceptorUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

class CentralInterceptorSpec extends Specification implements InterceptorUnitTest<CentralInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test central interceptor matching"() {
        when: "A central request matches the interceptor"
        withRequest(controller: "central")

        then: "The interceptor does match"
        interceptor.doesMatch()
    }

    void "Test central interceptor in dashboard module is not enabled"() {

        given:
        grailsApplication.config.pv.app.pvcentral.enabled = false
        interceptor.setConfiguration(grailsApplication.config)
        def customMessageService = new MockFor(CustomMessageService)
        customMessageService.demand.getMessage { String code ->
            return code
        }
        interceptor.customMessageService = customMessageService.proxyInstance()

        when:
        withRequest(controller: "central")

        then:
        !interceptor.before() && response.redirectUrl == '/dashboard' && flash.error
    }


    void "Test central interceptor in dashboard module is enabled"() {
        given:
        grailsApplication.config.pv.app.pvcentral.enabled = true
        interceptor.setConfiguration(grailsApplication.config)
        when:
        withRequest(controller: "central")

        then:
        interceptor.before()
    }
}
