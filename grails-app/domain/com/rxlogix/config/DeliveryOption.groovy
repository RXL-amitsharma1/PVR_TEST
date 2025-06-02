package com.rxlogix.config

import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['configuration','periodicReportConfiguration'])
class DeliveryOption extends BaseDeliveryOption {
    static auditable =  [ignore:['report']]
    List<User> executableBy = []
    List<UserGroup> executableByGroup = []
    static belongsTo = [report: ReportConfiguration]
    static hasMany = [sharedWith: User, sharedWithGroup: UserGroup, emailToUsers: String, attachmentFormats: ReportFormatEnum,executableBy:User,executableByGroup:UserGroup]
    static List propertiesToUseWhileCopying = ['emailToUsers','attachmentFormats','additionalAttachments', 'oneDriveFolderName', 'oneDriveFolderId', 'oneDriveSiteId', 'oneDriveUserSettings', 'oneDriveFormats']
    static mapping = {
        table name: "DELIVERY"
        report column: "REPORT_ID"
        additionalAttachments column:"ADDITIONAL_ATTACHMENTS"
        sharedWith joinTable: [name: "DELIVERIES_SHARED_WITHS", column: "SHARED_WITH_ID", key: "DELIVERY_ID"], indexColumn: [name:"SHARED_WITH_IDX"]
        sharedWithGroup joinTable: [name: "DELIVERIES_SHARED_WITH_GRPS", column: "SHARED_WITH_GROUP_ID", key: "DELIVERY_ID"], indexColumn: [name:"SHARED_WITH_GROUP_IDX"]
        executableBy joinTable: [name: "DELIVERIES_EXECUTABLE", column: "EXECUTABLE_ID", key: "DELIVERY_ID"], indexColumn: [name:"EXECUTABLE_IDX"]
        executableByGroup joinTable: [name: "DELIVERIES_EXECUTABLE_GRPS", column: "EXECUTABLE_GROUP_ID", key: "DELIVERY_ID"], indexColumn: [name:"EXECUTABLE_GROUP_IDX"]
        emailToUsers joinTable: [name: "DELIVERIES_EMAIL_USERS", column: "EMAIL_USER", key: "DELIVERY_ID"], indexColumn: [name: "EMAIL_USER_IDX"]
        attachmentFormats joinTable: [name: "DELIVERIES_RPT_FORMATS", column: "RPT_FORMAT", key: "DELIVERY_ID"], indexColumn: [name: "RPT_FORMAT_IDX"]
        oneDriveFormats joinTable: [name: "DELIVERIES_OD_FORMATS", column: "OD_FORMAT", key: "EX_DELIVERY_ID"], indexColumn: [name: "OD_FORMAT_IDX"]
    }

    @Override
    public String toString() {
        super.toString()
    }
    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && oldValues) {
            newValues.put("emailToUsers", emailToUsers?.join(";"))
            newValues.put("attachmentFormats", attachmentFormats?.collect{it.toString()}?.join(";"))
            withNewSession {
                DeliveryOption bdo = DeliveryOption.read(id);
                oldValues.put("emailToUsers", bdo?.emailToUsers?.join(";"))
                oldValues.put("attachmentFormats", bdo?.attachmentFormats?.collect{it.toString()}?.join(";"))
            }
        }

        return [newValues: newValues, oldValues: oldValues]
    }
}
