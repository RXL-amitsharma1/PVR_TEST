package com.rxlogix.co

import spock.lang.Specification

class SaveCaseSeriesFromSpotfireCOSpec extends Specification {

    def "Test for generateSetForCaseNumbers"() {
        given:
        SaveCaseSeriesFromSpotfireCO spotfireCO = new SaveCaseSeriesFromSpotfireCO([user: "Admin", seriesName: "Test CS",caseNumbers: "US1002345667,AB3215660098"])
        when:
        Set<String> caseNumbers = spotfireCO.generateSetForCaseNumbers()
        then:
        caseNumbers.size() == 2
    }

    def "Test for generateSetForCaseNumbers when no case number is there"() {
        given:
        SaveCaseSeriesFromSpotfireCO spotfireCO = new SaveCaseSeriesFromSpotfireCO([user: "Admin", seriesName: "Test CS",caseNumbers: ""])
        when:
        Set<String> caseNumbers = spotfireCO.generateSetForCaseNumbers()
        then:
        caseNumbers.size() == 0
    }
}
