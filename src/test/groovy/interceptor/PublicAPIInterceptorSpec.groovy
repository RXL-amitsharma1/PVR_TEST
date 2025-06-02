package interceptor

import com.rxlogix.CustomMessageService
import grails.testing.web.interceptor.InterceptorUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

class PublicAPIInterceptorSpec extends Specification implements InterceptorUnitTest<PublicAPIInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test publicAPI interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(uri : "/public/api/userRest/fetchUserDetail")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }

    void "Test PublicAPI interceptor if public token is present"(){
        given:
        def PUBLIC_API_TOKEN = "PVR_PUBLIC_TOKEN"
        Holders.config.rxlogix.pvreports.publicApi.token = 'zn9MrreyDiATUdoUs/FMmw70qMDExQOya/9LFs1uE5lCp2eCxNeOZCdTgubUCdbYWpLu3bRJRL5zD79iOm+sewLbXnt9r1KbSBNJhWd9BKhbGFhpYPVodA5J7P87aUnXfHLSSXB1F5xTJkCjyMszHA=='
        request.addHeader(PUBLIC_API_TOKEN, 'zn9MrreyDiATUdoUs/FMmw70qMDExQOya/9LFs1uE5lCp2eCxNeOZCdTgubUCdbYWpLu3bRJRL5zD79iOm+sewLbXnt9r1KbSBNJhWd9BKhbGFhpYPVodA5J7P87aUnXfHLSSXB1F5xTJkCjyMszHA==')
        when:
        interceptor.before()

        then:
        "it returns true"
    }

    void "Test PublicAPI interceptor if public token is not present or is invalid"(){
        given:
        def PUBLIC_API_TOKEN = "PVR_PUBLIC_TOKEN"
        Holders.config.rxlogix.pvreports.publicApi.token = 'zn9MrreyDiATUdoUs/FMmw70qMDExQOya/9LFs1uE5lCp2eCxNeOZCdTgubUCdbYWpLu3bRJRL5zD79iOm+sewLbXnt9r1KbSBNJhWd9BKhbGFhpYPVodA5J7P87aUnXfHLSSXB1F5xTJkCjyMszHA=='
        request.addHeader(PUBLIC_API_TOKEN, 'abcd')
        def customMessageService = new MockFor(CustomMessageService)
        customMessageService.demand.getMessage { String code ->
            return code
        }
        interceptor.customMessageService = customMessageService.proxyInstance()
        when:
        interceptor.before()

        then:
        "it returns false"
    }
}
