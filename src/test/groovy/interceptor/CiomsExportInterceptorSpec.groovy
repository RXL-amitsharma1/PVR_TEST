package interceptor

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class CiomsExportInterceptorSpec extends Specification implements InterceptorUnitTest<CiomsExportInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test ciomsExport interceptor not matching"() {
        when: "A request matches the interceptor"
        withRequest(controller: "report")

        then: "The interceptor doesn't match"
        !interceptor.doesMatch()
    }

    void "Test ciomsExport interceptor matching"() {
        when: "A request matches the interceptor"
        withRequest(controller: "report", action: 'exportSingleCIOMS')

        then: "The interceptor does match"
        interceptor.doesMatch()
    }

    void "Test params query handling to convert into params"() {

        given:
        params.query = '{"casenumber":"16US00007747","version":2,"blinded":false,"privacy":true}'.bytes.encodeAsBase64().toString()

        when:
        withRequest(controller: "report", action: 'exportSingleCIOMS')

        then:
        interceptor.before()
        params.caseNumber == '16US00007747'
        params.versionNumber == 2
        params.blinded == false
        params.privacy == true


    }

    void "Test params default params for blinded and privcay via config"() {

        given:
        config.ciomsI.blinded.flag = true
        config.ciomsI.privacy.flag = false
        when:
        withRequest(controller: "report", action: 'exportSingleCIOMS')

        then:
        interceptor.before()
        params.blinded == true
        params.privacy == false

    }

}
