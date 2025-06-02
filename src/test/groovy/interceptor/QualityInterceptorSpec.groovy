package interceptor

import com.rxlogix.CustomMessageService
import grails.testing.web.interceptor.InterceptorUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

class QualityInterceptorSpec extends Specification implements InterceptorUnitTest<QualityInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test quality interceptor matching"() {
        when: "A quality request matches the interceptor"
        withRequest(controller: "quality")

        then: "The interceptor does match"
        interceptor.doesMatch()
        
        when: "A issue request matches the interceptor"
        withRequest(controller: "dashboard", action: 'index')

        then: "The interceptor does not match"
        !interceptor.doesMatch()
    }


    void "Test quality interceptor in case quality module is not enabled"() {

        given:
        grailsApplication.config.pv.app.pvquality.enabled = false
        grailsApplication.config.pv.app.pvcentral.enabled = false
        interceptor.setConfiguration(grailsApplication.config)
        def customMessageServiceMock = new MockFor(CustomMessageService)
        customMessageServiceMock.demand.getMessage(0..1) { String code -> code }
        interceptor.customMessageService = customMessageServiceMock.proxyInstance()

        when:
        withRequest(controller: "quality")

        then:
        !interceptor.before() && response.redirectUrl == '/dashboard' && flash.error
    }


    void "Test quality interceptor in case quality module is enabled"() {
        given:
        grailsApplication.config.pv.app.pvquality.enabled = true
        interceptor.setConfiguration(grailsApplication.config)

        when:
        withRequest(controller: "quality")

        then:
        interceptor.before()
    }
}
