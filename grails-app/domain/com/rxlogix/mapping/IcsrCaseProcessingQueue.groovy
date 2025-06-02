package com.rxlogix.mapping

import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.enums.IcsrCaseStatusEnum
import com.rxlogix.util.DbUtil
import grails.gorm.MultiTenant

class IcsrCaseProcessingQueue implements MultiTenant<IcsrCaseProcessingQueue>, Serializable {

    BigDecimal id
    BigDecimal caseQueueId
    String caseNumber
    Long caseId
    Long executionId
    Long versionNumber
    IcsrCaseStatusEnum status
    Integer tenantId
    Long profileId
    Long executedTemplateQueryId
    String errorMessage
    String errorCause
    String executedOn
    boolean isLocked
    Boolean deletePreview = false
    Date dateCreated
    Date lastUpdated
    Integer srcCaseId
    Integer srcTenantId
    Integer srcVersionNumber
    Integer affFlag
    Integer flagUiNillif = 0
    boolean isReportable = 1
    Date caseModificationDate
    Long approvalId = -1
    Long reportCategoryId = -1
    Boolean isPMDA = false
    Boolean flagLocalLocked = false
    Boolean flagJapanProfile = false
    String prodHashCode

    IcsrCaseProcessingQueue() {
    }

    IcsrCaseProcessingQueue(IcsrCaseQueue caseQueue, IcsrProfileConfiguration profileConfiguration, String prodHashCode = "-1", approvalId = -1, reportCategoryId = -1, IcsrCaseStatusEnum status = IcsrCaseStatusEnum.START) {
        this.caseQueueId = caseQueue.id
        this.caseId = caseQueue.caseId
        this.caseNumber = caseQueue.caseNumber
        this.versionNumber = caseQueue.versionNumber
        this.tenantId = caseQueue.tenantId
        this.profileId = profileConfiguration.id
        this.isLocked = caseQueue.isLocked
        this.srcCaseId = caseQueue.srcCaseId
        this.srcTenantId = caseQueue.srcTenantId
        this.srcVersionNumber = caseQueue.srcVersionNumber
        this.affFlag = caseQueue.affFlag
        this.status = status
        this.prodHashCode = prodHashCode
        this.isReportable = caseQueue.isReportable
        this.caseModificationDate = caseQueue.caseModificationDate
        this.isPMDA = profileConfiguration.isPMDAReport()
        this.approvalId = approvalId
        this.reportCategoryId = reportCategoryId
        this.flagLocalLocked = caseQueue.flagLocalLocked
        this.flagJapanProfile = profileConfiguration.isJapanProfile
    }


    static constraints = {
        caseQueueId(nullable: true)
        executionId(nullable: true)
        executedTemplateQueryId(nullable: true)
        errorMessage(nullable: true)
        errorCause(nullable: true)
        executedOn(nullable: true)
        deletePreview(nullable: true)
        srcCaseId(nullable: true)
        srcTenantId(nullable: true)
        srcVersionNumber(nullable: true)
        affFlag(nullable: true)
        flagUiNillif(nullable:true)
        caseModificationDate(nullable: true)
        approvalId(nullable: true)
        reportCategoryId(nullable: true)
        isPMDA(nullable: true)
        flagLocalLocked(nullable: true)
        flagJapanProfile(nullable: true)
        prodHashCode(nullable: true)
    }

    static mapping = {
        datasource "pva"
        table "PVR_ICSR_PROFILE_QUEUE"
        version false
        id column: "PIPQ_REC_NUM", type: "big_decimal", generator: 'native', params: [sequence: 'SEQ_PVR_ICSR_PROFILE_QUEUE']
        caseQueueId column: "PIQ_REC_NUM", type: "big_decimal"
        caseId column: 'CASE_ID'
        caseNumber column: "CASE_NUM"
        versionNumber column: "VERSION_NUM"
        status column: "STATUS"
        isLocked column: 'ICSR_LOCKED_FLAG'
        executionId column: 'EXECUTION_ID'
        executedTemplateQueryId column: 'SECTION_ID'
        errorMessage column: 'ERROR_MESSAGE'
        errorCause column: 'ERROR_CAUSE', sqlType:  DbUtil.longStringType
        executedOn column: "SERVER_NAME"
        deletePreview column: "DELETE_PREVIEW"
        lastUpdated column: "DATE_LAST_UPDATED"
        tenantId column: 'TENANT_ID', name: 'tenantId'
        srcCaseId column: 'SRC_2_CASE_ID'
        srcTenantId column: 'SRC_2_TENANT_ID'
        srcVersionNumber column: 'SRC_2_VERSION_NUM'
        affFlag column: 'AFF_FLAG'
        flagUiNillif column: 'FLAG_NULLIFICATION'
        caseModificationDate column: 'CASE_UPDATE_DATE'
        approvalId column: 'AUTH_ID'
        reportCategoryId column: 'RPT_CATEGORY_ID'
        isPMDA column: 'FLAG_PMDA'
        flagLocalLocked column: 'ICSR_LOCAL_LOCKED_FLAG'
        flagJapanProfile column: 'FLAG_JPN_PROFILE'
        prodHashCode colummn: 'PROD_HASH_CODE'
    }

}