package com.rxlogix.dto.caseSeries.integration

import grails.validation.Validateable

class CaseSeriesDTO implements Validateable {

    Long caseSeriesId
    List<String> sharedWithUsers
    List<String> sharedWithGroups
    boolean isTemporary = false
}
