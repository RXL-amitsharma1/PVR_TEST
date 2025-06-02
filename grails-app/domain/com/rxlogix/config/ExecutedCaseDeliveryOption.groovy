package com.rxlogix.config

class ExecutedCaseDeliveryOption extends BaseDeliveryOption {

    static belongsTo = [executedCaseSeries : ExecutedCaseSeries]

    static mapping = {
        table name: "EX_CASE_DELIVERY"
        additionalAttachments column:"ADDITIONAL_ATTACHMENTS"
        executedCaseSeries column: "EXECUTED_CASE_ID"
        sharedWith joinTable: [name: "EX_CASE_DELIVERIES_SHRD_WTHS", column: "SHARED_WITH_ID", key: "EX_DELIVERY_ID"], indexColumn: [name:"SHARED_WITH_IDX"]
        sharedWithGroup joinTable: [name: "EX_CASE_DELIVERIES_SHRD_W_GRPS", column: "SHARED_WITH_GROUP_ID", key: "EX_DELIVERY_ID"], indexColumn: [name:"SHARED_WITH_GROUP_IDX"]
        emailToUsers joinTable: [name: "EX_CASE_DELIVERIES_EMAIL_USERS", column: "EMAIL_USER", key: "EX_DELIVERY_ID"], indexColumn: [name:"EMAIL_USER_IDX"]
        attachmentFormats joinTable: [name: "EX_CASE_DELIVERIES_RPT_FORMATS", column: "RPT_FORMAT", key: "EX_DELIVERY_ID"], indexColumn: [name:"RPT_FORMAT_IDX"]
        oneDriveFormats joinTable: [name: "EX_CASE_DELIVERIES_OD_FORMATS", column: "OD_FORMAT", key: "EX_DELIVERY_ID"], indexColumn: [name:"OD_FORMAT_IDX"]
    }

    @Override
    public String toString() {
        super.toString()
    }
}
