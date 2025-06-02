package com.rxlogix.co

import grails.validation.Validateable

class SaveCaseSeriesFromSpotfireCO implements Validateable{

    String user
    String seriesName
    String caseNumbers
    boolean isTemporary = false
    Long tenantId

    static constraints = {
        user nullable: false, blank: false
        seriesName nullable: false, blank: false
        caseNumbers nullable: false, blank: false
        tenantId nullable: true
    }

    Set<String> generateSetForCaseNumbers() {
        return caseNumbers? caseNumbers.tokenize(',') : []
    }

}
