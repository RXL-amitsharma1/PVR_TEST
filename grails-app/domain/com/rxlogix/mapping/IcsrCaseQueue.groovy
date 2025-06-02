package com.rxlogix.mapping

import com.rxlogix.enums.IcsrCaseStatusEnum
import grails.gorm.MultiTenant

class IcsrCaseQueue implements MultiTenant<IcsrCaseQueue>, Serializable {

    BigDecimal id
    Long caseId
    String caseNumber
    Long versionNumber
    IcsrCaseStatusEnum status
    Integer tenantId
    boolean isLocked
    Integer srcCaseId
    Integer srcTenantId
    Integer srcVersionNumber
    Integer affFlag
    boolean isReportable
    Date caseModificationDate
    Boolean flagLocalLocked
    Boolean flagLocalProcessing

    static constraints = {
        status nullable: true
        srcCaseId nullable: true
        srcTenantId nullable: true
        srcVersionNumber nullable: true
        affFlag nullable: true
        caseModificationDate nullable: true
        flagLocalLocked nullable: true
        flagLocalProcessing nullable: true
    }

    static mapping = {
        datasource "pva"
        table "PVR_ICSR_QUEUE"
        version false
        id column: "PIQ_REC_NUM", type: "big_decimal", generator: "assigned"
        caseNumber column: "CASE_NUM"
        versionNumber column: "VERSION_NUM"
        status column: "STATUS"
        isLocked column: 'ICSR_LOCKED_FLAG'
        tenantId column: 'TENANT_ID', name: 'tenantId'
        caseId column: 'CASE_ID'
        srcCaseId column: 'SRC_2_CASE_ID'
        srcTenantId column: 'SRC_2_TENANT_ID'
        srcVersionNumber column: 'SRC_2_VERSION_NUM'
        affFlag column: 'AFF_FLAG'
        isReportable column : 'IS_REPORTABLE'
        caseModificationDate column: 'CASE_UPDATE_DATE'
        flagLocalLocked column: 'ICSR_LOCAL_LOCKED_FLAG'
        flagLocalProcessing column: 'LOCAL_PROCESSING_FLAG'

    }
}