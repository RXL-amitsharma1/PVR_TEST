package com.rxlogix.config

import com.rxlogix.hibernate.EscapedILikeExpression

class ResultInboundCompliance implements Serializable{

    Long tenantId
    String senderName
    Date senderReceiptDate
    Date safetyRecieptDate
    Long daysToProcess
    Long ruleId
    String status
    String caseNum
    String criteriaName
    Long exInboundComplianceId
    Long versionNum
    String errorDetails
    Date caseCreationDate



    static mapping = {
        datasource "pva"
        table name: "VW_PVR_INB_COMP_DETAILS"
        cache: "read-only"
        version false
        id column: "CIC_REC_NUM"
        tenantId column: "TENANT_ID"
        senderName column: "SENDER_NAME"
        senderReceiptDate column: "SENDER_RECIEPT_DATE"
        safetyRecieptDate column: "SAFETY_RECIEPT_DATE"
        daysToProcess column: "DAYS_TO_PROCESS"
        ruleId column: "RULE_ID"
        status column: "STATUS"
        caseNum column: "CASE_NUM"
        criteriaName column: "CRITERIA_NAME"
        exInboundComplianceId column: "EX_INBOUND_COMPLIANCE_ID"
        errorDetails column: "ERROR_DETAILS"
        versionNum column: "VERSION_NUM"
        caseCreationDate column:"CASE_CREATION_DATE"
    }

    static constraints = {
        exInboundComplianceId(nullable: false)
    }

    static namedQueries = {

        getAllResultByIdAndBySearchString { Long id, String search ->
            eq('exInboundComplianceId', id)
            if (search) {
                List queryList = SuperQuery.fetchAllIdsByName(search).list()
                or {
                    iLikeWithEscape('senderName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('caseNum', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('criteriaName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('status', "${EscapedILikeExpression.escapeString(search)}%")
                    if (queryList) {
                        queryList.collate(999).each { list ->
                            'in'('ruleId', list)
                        }
                    }
                }
            }
        }

        getAllResultByIdAndErrorDetails { Long id ->
            eq('exInboundComplianceId', id)
            isNotNull('errorDetails')

        }

    }


}
