package com.rxlogix.dto.caseSeries.integration

import com.rxlogix.enums.EvaluateCaseDateEnum
import grails.validation.Validateable

class ExecutedCaseSeriesDTO implements Validateable {
    String seriesName
    String description
    String dateRangeType
    Date asOfVersionDate
    EvaluateCaseDateEnum evaluateDateAs
    boolean excludeFollowUp = false
    boolean includeLockedVersion = true
    boolean includeAllStudyDrugsCases = false
    boolean excludeNonValidCases = true
    boolean excludeDeletedCases = true
    boolean isTemporary = false
    boolean suspectProduct = false
    String productSelection
    String productGroupSelection
    String studySelection
    String eventSelection
    String eventGroupSelection
    String ownerName
    Long globalQueryId
    ExecutedDateRangeInfoDTO executedCaseSeriesDateRangeInformation
    List<QueryValueListDTO> executedGlobalQueryValueLists
    List<String> sharedWithUsers
    List<String> sharedWithGroups
    String callbackURL
    Long tenantId
    boolean isMultiIngredient = false
    boolean includeWHODrugs = false
}
