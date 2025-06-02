package com.rxlogix.dto.reports.integration

import com.rxlogix.config.ExecutedGlobalDateRangeInformation
import com.rxlogix.enums.EvaluateCaseDateEnum
import grails.validation.Validateable

class ExecutedConfigurationDTO implements Validateable {
    String reportName
    String description
    String productSelection
    String productGroupSelection
    String studySelection
    String eventSelection
    String eventGroupSelection
    String ownerName
    String dateRangeType
    Date asOfVersionDate
    ExecutedGlobalDateRangeInformation executedGlobalDateRangeInformation
    EvaluateCaseDateEnum evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
    boolean excludeFollowUp = false
    boolean includeLockedVersion = true
    boolean excludeNonValidCases = true
    boolean excludeDeletedCases = true
    boolean includeMedicallyConfirmedCases = false
    boolean suspectProduct = false
    boolean limitPrimaryPath = false
    Long pvrCumulativeCaseSeriesId
    Long pvrCaseSeriesId
    List<ExecutedTemplateQueryDTO> executedTemplateQueryDTOList = []
    List<String> sharedWithUsers = []
    List<String> sharedWithGroups = []
    String callbackURL
    Long tenantId
    Date executedETLDate
    boolean isMultiIngredient = false
    boolean includeWHODrugs = false
    boolean includeAllStudyDrugsCases = false

    //PVS use only
    boolean considerOnlyPoi = false
    boolean studyMedicationType = false
}
