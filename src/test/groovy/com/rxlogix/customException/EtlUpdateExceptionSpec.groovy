package com.rxlogix.customException

import spock.lang.Specification

class EtlUpdateExceptionSpec extends Specification {

    def "test EtlUpdateException"() {
        given: EtlUpdateException etlUpdateException

        when: etlUpdateException = new EtlUpdateException()

        then: etlUpdateException instanceof EtlUpdateException
    }

    def "test EtlUpdateExceptionMessage"() {
        given: EtlUpdateException etlUpdateException

        when: etlUpdateException = new EtlUpdateException("Test Message")

        then: etlUpdateException.getMessage() == "Test Message"
    }
}