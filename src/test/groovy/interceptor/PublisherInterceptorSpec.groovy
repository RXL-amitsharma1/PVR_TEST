package interceptor

import com.rxlogix.CustomMessageService
import grails.testing.web.interceptor.InterceptorUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

class PublisherInterceptorSpec extends Specification implements InterceptorUnitTest<PublisherInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test publisher interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"gantt")

        then:"The interceptor does match"
            interceptor.doesMatch()

        when: "A issue request matches the interceptor"
        withRequest(controller: "publisherCommonParameter")

        then: "The interceptor does match"
        interceptor.doesMatch()

        when: "A issue request matches the interceptor"
        withRequest(controller: "publisherTemplate")

        then: "The interceptor does match"
        interceptor.doesMatch()

        when: "A issue request matches the interceptor"
        withRequest(controller: "pvp")

        then: "The interceptor does match"
        interceptor.doesMatch()

        when: "A issue request matches the interceptor"
        withRequest(controller: "wopi")

        then: "The interceptor does match"
        interceptor.doesMatch()

        when: "A issue request matches the interceptor"
        withRequest(controller: "dashboard", action: 'index')

        then: "The interceptor does not match"
        !interceptor.doesMatch()
    }

    void "Test publisher interceptor if publisher module is not enabled"() {

        given:
        grailsApplication.config.pv.app.pvpublisher.enabled = false
        interceptor.setConfiguration(grailsApplication.config)
        def customMessageServiceMock = new MockFor(CustomMessageService)
        customMessageServiceMock.demand.getMessage(0..1) { String code -> code }
        interceptor.customMessageService = customMessageServiceMock.proxyInstance()

        when:
        withRequest(controller: "publisherCommonParameter")

        then:
        !interceptor.before() && response.redirectUrl == '/dashboard' && flash.error
    }


    void "Test publisher interceptor if publisher module is enabled"() {
        given:
        grailsApplication.config.pv.app.pvpublisher.enabled = true
        interceptor.setConfiguration(grailsApplication.config)

        when:
        withRequest(controller: "publisherCommonParameter")

        then:
        interceptor.before()
    }
}
