package com.rxlogix.signal


import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovyx.net.http.Method
import spock.lang.Ignore
import spock.lang.Specification

class SignalIntegrationApiServiceSpec extends Specification implements DataTest, ServiceUnitTest<SignalIntegrationApiService> {

    def setup() {

    }
    @Ignore
    void "test postData"() {
        given:
        String baseUrl = "http://localhost:8080/reports"
        final String SAVE_SIGNAL_REPORT = "/signal/api/signalReport"
        String data = "Test data"
        when:
        Map postData = service.postData(baseUrl, SAVE_SIGNAL_REPORT, data, Method.POST)
        then:
        postData.status == 404
    }
    @Ignore
    void "test postCallback"() {
        given:
        String baseUrl = "http://localhost:8080/reports"
        Map data = [body: "Test data"]
        when:
        Map postData = service.postCallback(baseUrl, data, Method.POST)
        then:
        postData.status == 404
    }

}
