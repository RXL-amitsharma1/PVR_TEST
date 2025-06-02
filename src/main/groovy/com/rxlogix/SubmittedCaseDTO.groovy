package com.rxlogix

import com.rxlogix.util.MiscUtil
import groovy.sql.GroovyRowResult


class SubmittedCaseDTO {

    String caseType
    String caseNumber
    String productName
    String studyName
    String versionNumber
    String eventSequenceNumber
    Date eventReceiptDate
    String eventPreferredTerm
    String eventSeriousness

    static
    final Map<String, String> sqlFieldsMapping = [caseType: 'RPT_INIT_FOLLOWUP', caseNumber: 'CASE_NUM', productName: 'PRODUCT_NAME', studyName: 'STUDY_NAME', versionNumber:'VERSION_NUM', eventSequenceNumber:'AE_REC_NUM',eventReceiptDate:'EVENT_RECEIPT_DATE',eventPreferredTerm:'MDR_AE_PT',eventSeriousness:'FLAG_SERIOUS',count:'cnt', totalCount:'total_count']

    Map asMap() {
        return MiscUtil.getObjectProperties(this, ['caseType', 'caseNumber', 'productName', 'studyName','versionNumber','eventSequenceNumber','eventReceiptDate','eventPreferredTerm','eventSeriousness'])
    }

    static getSortKey(String field) {
        return sqlFieldsMapping.get(field) ?: sqlFieldsMapping.caseNumber
    }

    SubmittedCaseDTO() {

    }

    SubmittedCaseDTO(def resultSet) {
        caseType = resultSet.RPT_INIT_FOLLOWUP
        caseNumber = resultSet.CASE_NUM
        productName = resultSet.PRODUCT_NAME
        studyName = resultSet.STUDY_NAME
        versionNumber = resultSet.VERSION_NUM
        eventSequenceNumber=resultSet.AE_REC_NUM
        eventReceiptDate=resultSet.EVENT_RECEIPT_DATE
        eventPreferredTerm=resultSet.MDR_AE_PT
        eventSeriousness=resultSet.FLAG_SERIOUS
    }

}