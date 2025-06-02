package com.rxlogix.config

import com.rxlogix.enums.AuthorizationTypeEnum
import com.rxlogix.enums.DistributionChannelEnum

class ExecutedIcsrTemplateQuery extends ExecutedTemplateQuery{

    AuthorizationTypeEnum authorizationType
    String productType
    Integer dueInDays
    Integer icsrMsgType
    String icsrMsgTypeName
    DistributionChannelEnum distributionChannelName
    Integer orderNo
    EmailConfiguration emailConfiguration
    boolean isExpedited

    static mapping = {
        table name: "EX_ICSR_TEMPLT_QUERY"
        authorizationType column: "AUTHORIZATION_TYPE"
        productType column: "PRODUCT_TYPE"
        dueInDays column: "DUE_DAYS"
        icsrMsgType column: "ICSR_MSG_TYPE"
        icsrMsgTypeName column: "ICSR_MSG_TYPE_NAME"
        distributionChannelName column: "DIST_CHANNEL_NAME"
        orderNo column: "ORDER_NO"
        emailConfiguration column: "EMAIL_CONFIGURATION_ID"
        isExpedited column: "IS_EXPEDITED"
        version false
    }

    static constraints = {
        authorizationType nullable: true
        productType nullable: true
        dueInDays nullable: true
        icsrMsgType nullable: false
        icsrMsgTypeName nullable: false
        distributionChannelName nullable: true
        orderNo nullable: true
        emailConfiguration(nullable: true)
    }

    String getInstanceIdentifierForAuditLog() {
        return "Executed ICSR Template Query (ID:${id})"
    }
}
