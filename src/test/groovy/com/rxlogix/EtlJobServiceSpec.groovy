package com.rxlogix

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class EtlJobServiceSpec extends Specification implements DataTest, ServiceUnitTest<EtlJobService> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
    }

    void "test Reccurence pattern update for etl "() {
        expect:
        service.getRecurrenceForETL(reccurencePattern) == expectedResult
        where:
        reccurencePattern                   || expectedResult
        "FREQ=DAILY;COUNT=1;INTERVAL=16;"   || "FREQ=DAILY;INTERVAL=16;"
        "FREQ=DAILY;COUNT=1223;INTERVAL=6;" || "FREQ=DAILY;INTERVAL=6;"
        "FREQ=DAILY;INTERVAL=6;COUNT=1223"  || "FREQ=DAILY;INTERVAL=6;"
        "FREQ=WEEKLY;INTERVAL=6;COUNT=1;"   || "FREQ=WEEKLY;INTERVAL=6;"
        "FREQ=DAILY;INTERVAL=6;COUNT=1"     || "FREQ=DAILY;INTERVAL=6;"
        "FREQ=DAILY;COUNT=1INTERVAL=6;"     || "FREQ=DAILY;COUNT=1INTERVAL=6;"
    }
}
