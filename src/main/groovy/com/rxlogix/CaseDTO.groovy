package com.rxlogix

import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil

class CaseDTO {

    String caseUniqueId
    String caseNumber
    Long caseId
    String versionNumber
    String type
    String productFamily
    String eventPI
    String seriousness
    String outcome
    String listedness
    String causality
    String comments
    String justification
    Date lockedDate
    String eventSequenceNumber
    Date eventReceiptDate
    String eventPreferredTerm
    String eventSeriousness

    List<String> caseSeriesTags
    List<String> globalTags
    boolean isManuallyAdded = false
    boolean isNewCase = false
    boolean isMovedFromOpen = false
    boolean higherVersionExists = false

    static
    final Map<String, String> sqlFieldsMapping = [caseUniqueId: 'CASE_LIST_UNQ_ID', caseNumber: 'case_num', versionNumber: 'version_num', type: 'report_type', productFamily: 'NAME', eventPI: 'primary_event', seriousness: 'seriousness_flag', outcome: 'evt_outcome', listedness: 'listedness', causality: 'reportability', comments: 'comments', justification: 'justification', lockedDate: 'case_locked_date',caseId: 'case_id',caseSeriesTags:'alert_tag_text',globalTags:'global_tag_text',eventSequenceNumber:'ae_rec_num',eventReceiptDate:'event_receipt_date',eventPreferredTerm:'mdr_ae_pt',eventSeriousness:'flag_serious']

    Map asMap() {
        Map m = MiscUtil.getObjectProperties(this, ['caseUniqueId', 'caseNumber', 'versionNumber', 'type', 'productFamily', 'eventPI', 'seriousness', 'outcome', 'listedness', 'causality', 'comments', 'justification', 'lockedDate', 'isManuallyAdded', 'isNewCase', 'isMovedFromOpen', 'higherVersionExists','caseId','caseSeriesTags','globalTags','eventSequenceNumber','eventReceiptDate','eventPreferredTerm','eventSeriousness'])
        m.lockedDate = lockedDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        m.eventReceiptDate = eventReceiptDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        return  m
    }

    static getSortKey(String field) {
        return sqlFieldsMapping.get(field) ?: sqlFieldsMapping.caseNumber
    }

    CaseDTO() {

    }

    CaseDTO(def resultSet) {
        caseSeriesTags = resultSet.alert_tag_text != null ? resultSet.alert_tag_text.tokenize(",") : []
        globalTags = resultSet.global_tag_text != null ? resultSet.global_tag_text.tokenize(",") : []
        caseUniqueId = resultSet.CASE_UNIQUE_ID
        caseId = resultSet.case_id
        caseNumber = resultSet.case_num
        versionNumber = resultSet.version_num
        type = resultSet.report_type
        outcome = resultSet.evt_outcome
        productFamily = resultSet.NAME
        eventPI = resultSet.primary_event
        seriousness = resultSet.seriousness_flag
        listedness = resultSet.listedness
        causality = resultSet.reportability
        eventSequenceNumber=resultSet.ae_rec_num
        eventReceiptDate=resultSet.event_receipt_date ?: null
        eventPreferredTerm=resultSet.mdr_ae_pt
        eventSeriousness=resultSet.flag_serious
        comments = ((resultSet.comments && resultSet.comments != 'null') ? resultSet.comments : "")
        justification = ((resultSet.justification && resultSet.justification != 'null') ? resultSet.justification : "")
        isManuallyAdded = resultSet.added_manual_flag ? true : false
        lockedDate = resultSet.case_locked_date ?: null
        try {
            isMovedFromOpen = resultSet.unlocked_to_locked_flag ? true : false
        } catch (java.sql.SQLException e) {
            isMovedFromOpen = false
        }
        try {
            higherVersionExists = resultSet.higher_version_flag ? true : false
        } catch (java.sql.SQLException e) {
            higherVersionExists = false
        }

        try {
            isNewCase = resultSet.new_case_flag ? true : false
        } catch (java.sql.SQLException e) {
            isNewCase = false
        }
    }

}