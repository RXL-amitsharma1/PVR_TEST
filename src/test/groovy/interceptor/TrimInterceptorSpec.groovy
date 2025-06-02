package interceptor

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class TrimInterceptorSpec extends Specification implements InterceptorUnitTest<TrimInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test trim interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"configuration")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
