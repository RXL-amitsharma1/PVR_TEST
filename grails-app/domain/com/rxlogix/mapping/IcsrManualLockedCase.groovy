package com.rxlogix.mapping

class IcsrManualLockedCase implements Serializable {

    Long caseId
    Long versionNumber
    Integer tenantId
    String profileName
    Long exIcsrTemplateQueryId
    String caseNumber
    Integer dueInDays
    Boolean isExpedited
    Integer flagNullification
    Long processedReportId
    Long icsrTemplateQueryId
    Long profileId
    Long approvalId
    Long reportCategoryId
    Long authorizationTypeId
    Boolean isPMDA
    String prodHashCode

    static constraints = {
    }

    static mapping = {
        datasource "pva"
        table "VW_ICSR_ACTIVE_MANUAL_CASES"
        version false
        id composite: ['caseId', 'versionNumber', 'exIcsrTemplateQueryId', 'tenantId']
        versionNumber column: "VERSION_NUM"
        tenantId column: 'TENANT_ID', name: 'tenantId'
        caseId column: 'CASE_ID'
        profileName column: 'PARTNER_NAME'
        exIcsrTemplateQueryId column: 'SECTION_ID'
        caseNumber column: 'CASE_NUM'
        dueInDays column: 'ACTUAL_TIMEFRAME_DAYS'
        isExpedited column: 'ACTUAL_FLAG_EXPEDITED'
        flagNullification column: 'FLAG_NULLIFICATION'
        processedReportId column: 'PROCESSED_REPORT_ID'
        icsrTemplateQueryId column: 'ORIGINAL_SECTION_ID'
        profileId column: 'PROFILE_ID'
        approvalId column: 'AUTH_ID'
        reportCategoryId column: 'RPT_CATEGORY_ID'
        authorizationTypeId column: 'AUTHORIZATION_TYPE_ID'
        isPMDA column: 'FLAG_PMDA'
        prodHashCode colummn: 'PROD_HASH_CODE'
    }
}
