package com.rxlogix.config

import com.rxlogix.commandObjects.CaseSubmissionCO
import com.rxlogix.enums.IcsrCaseStateEnum
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils

class IcsrCaseSubmission implements Serializable{

    Long e2bProcessId
    Long tenantId
    Long caseId
    String caseNumber
    Long versionNumber
    String e2bStatus
    Date lastUpdateDate
    Date submissionDate
    Date transmissionDate
    Date transmittedDate
    String partnerName
    Long schedulingCriteriaId
    String reportDestination
    Long exIcsrTemplateQueryId
    String parsingErrorTxt
    String ackFileName
    Date ackReceiveDate
    String lastUpdatedBy
    Long processedReportId
    String comments
    String commentsJ
    byte[] submissionDocument
    String submissionFilename
    Date localDateTime
    String timeZoneOffset
    String attachmentAckFileName
    Date dateTransmissionAttach
    Date dateTransmittedAttach
    Date dateAckRecievedAttach


    static mapping = {
        datasource "pva"
        table name: "PVR_E2B_CASE_SUBMISSION_HIST"
        cache: "read-only"
        version false
        id composite: ['tenantId', 'caseNumber', 'versionNumber', 'partnerName', 'exIcsrTemplateQueryId', 'e2bStatus','lastUpdateDate']
        e2bProcessId column: 'E2B_PROCESS_ID'
        tenantId column: 'TENANT_ID'
        caseId column: 'CASE_ID'
        caseNumber column: 'CASE_NUM'
        versionNumber column: 'VERSION_NUM'
        e2bStatus column: 'E2B_STATUS'
        lastUpdateDate column: 'LAST_UPDATE_DATE'
        submissionDate column: 'SUBMISSION_DATE'
        transmissionDate column: 'TRANSMISSION_DATE'
        transmittedDate column: 'TRANSMITTED_DATE'
        partnerName column: 'PARTNER_NAME'
        schedulingCriteriaId column: 'SCHDEULING_CRITERIA_ID'
        reportDestination column: 'PARTNER_RECIPIENT_NAME'
        exIcsrTemplateQueryId column: 'SECTION_ID'
        parsingErrorTxt column: 'PARSING_ERROR_TXT'
        lastUpdatedBy column: 'LAST_UPDATED_BY'
        ackFileName column: 'ACK_FILE_NAME'
        ackReceiveDate column: 'ACK_DATE'
        processedReportId column: 'PROCESSED_REPORT_ID'
        comments column: 'COMMENTS'
        commentsJ column: 'COMMENTS_J'
        submissionDocument column: 'SUBMISSION_DOCUMENT'
        submissionFilename column: 'SUBMISION_FILENAME'
        localDateTime column: 'DATE_SUBMISSION_LOCAL'
        timeZoneOffset column: 'AGENCY_TZ_OFFSET'
        attachmentAckFileName column: 'ATTACH_ACK_FILE_NAME'
        dateTransmissionAttach column: 'DATE_TRANSMISSION_ATTACH'
        dateTransmittedAttach column: 'DATE_TRANSMITTED_ATTACH'
        dateAckRecievedAttach column: 'DATE_ACK_RECIEVED_ATTACH'
    }

    static namedQueries = {
        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        fetchIcsrCaseSubmissionByCaseNoAndVersionNo { String profileName, Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber ->
            projections {
                property("processedReportId")
            }
            eq('caseNumber', caseNumber)
            eq('versionNumber', versionNumber)
            eq('partnerName', profileName)
            eq('exIcsrTemplateQueryId', exIcsrTemplateQueryId)
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
            maxResults(1)
        }

        fetchIcsrErrorDetailsByCaseNoAndVersionNo { String profileName, Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber, String status ->
            eq('caseNumber', caseNumber)
            eq('versionNumber', versionNumber)
            eq('partnerName', profileName)
            eq('exIcsrTemplateQueryId', exIcsrTemplateQueryId)
            eq('e2bStatus', status)
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
            order('lastUpdateDate','desc')
            maxResults(1)
        }

        fetchIfTransmitted { Long tenantId, CaseSubmissionCO caseSubmissionCO ->
            eq('caseNumber', caseSubmissionCO.caseNumber)
            eq('versionNumber', caseSubmissionCO.versionNumber)
            eq('exIcsrTemplateQueryId', caseSubmissionCO.queryId as Long)
            searchByTenant(tenantId)
            eq('e2bStatus', IcsrCaseStateEnum.TRANSMITTED.toString())
        }

    }

    String toString() {
        return caseNumber
    }

}
