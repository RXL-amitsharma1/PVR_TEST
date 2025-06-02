package com.rxlogix.config

import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit

@DirtyCheck
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['caseSeries'])
class CaseDeliveryOption extends BaseDeliveryOption {
    static auditable =  true
    static belongsTo = [caseSeries: CaseSeries]

    static mapping = {
        table name: "CASE_DELIVERY"
        caseSeries column: "CASE_ID"
        additionalAttachments column:"ADDITIONAL_ATTACHMENTS"
        sharedWith joinTable: [name: "CASE_DELIVERIES_SHARED_WITHS", column: "SHARED_WITH_ID", key: "DELIVERY_ID"], indexColumn: [name: "SHARED_WITH_IDX"]
        sharedWithGroup joinTable: [name: "CASE_DELIVERIES_SHARED_W_GRPS", column: "SHARED_WITH_GROUP_ID", key: "DELIVERY_ID"], indexColumn: [name: "SHARED_WITH_GROUP_IDX"]
        emailToUsers joinTable: [name: "CASE_DELIVERIES_EMAIL_USERS", column: "EMAIL_USER", key: "DELIVERY_ID"], indexColumn: [name: "EMAIL_USER_IDX"]
        attachmentFormats joinTable: [name: "CASE_DELIVERIES_RPT_FORMATS", column: "RPT_FORMAT", key: "DELIVERY_ID"], indexColumn: [name: "RPT_FORMAT_IDX"]
        oneDriveFormats joinTable: [name: "CASE_DELIVERIES_OD_FORMATS", column: "OD_FORMAT", key: "EX_DELIVERY_ID"], indexColumn: [name:"OD_FORMAT_IDX"]
    }

    @Override
    public String toString() {
        super.toString()
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && oldValues) {
            newValues.put("emailToUsers", emailToUsers?.join(";"))
            newValues.put("attachmentFormats", attachmentFormats?.collect { it.toString() }?.join(";"))
            withNewSession {
                CaseDeliveryOption bdo = CaseDeliveryOption.read(id);
                oldValues.put("emailToUsers", bdo?.emailToUsers?.join(";"))
                oldValues.put("attachmentFormats", bdo?.attachmentFormats?.collect { it.toString() }?.join(";"))
            }
        }

        return [newValues: newValues, oldValues: oldValues]
    }
}
