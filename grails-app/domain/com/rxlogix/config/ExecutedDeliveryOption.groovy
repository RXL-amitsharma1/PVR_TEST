package com.rxlogix.config

class ExecutedDeliveryOption extends BaseDeliveryOption {
    static belongsTo = [executedConfiguration : ExecutedReportConfiguration]

    static fetchMode = [sharedWith: "eager", sharedWithGroup: 'eager']

    static mapping = {
        table name: "EX_DELIVERY"
        executedConfiguration column: "EXECUTED_CONFIGURATION_ID"
        additionalAttachments column:"ADDITIONAL_ATTACHMENTS"
        sharedWith joinTable: [name: "EX_DELIVERIES_SHARED_WITHS", column: "SHARED_WITH_ID", key: "EX_DELIVERY_ID"], indexColumn: [name:"SHARED_WITH_IDX"]
        sharedWithGroup joinTable: [name: "EX_DELIVERIES_SHARED_WITH_GRPS", column: "SHARED_WITH_GROUP_ID", key: "EX_DELIVERY_ID"], indexColumn: [name:"SHARED_WITH_GROUP_IDX"]
        emailToUsers joinTable: [name: "EX_DELIVERIES_EMAIL_USERS", column: "EMAIL_USER", key: "EX_DELIVERY_ID"], indexColumn: [name:"EMAIL_USER_IDX"]
        attachmentFormats joinTable: [name: "EX_DELIVERIES_RPT_FORMATS", column: "RPT_FORMAT", key: "EX_DELIVERY_ID"], indexColumn: [name:"RPT_FORMAT_IDX"]
        oneDriveFormats joinTable: [name: "EX_DELIVERIES_OD_FORMATS", column: "OD_FORMAT", key: "EX_DELIVERY_ID"], indexColumn: [name:"OD_FORMAT_IDX"]
    }

    @Override
    public String toString() {
        super.toString()
    }
}
