package com.rxlogix.config

import com.rxlogix.enums.AuthorizationTypeEnum
import com.rxlogix.enums.DistributionChannelEnum
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit

@DirtyCheck
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['configuration', 'periodicReportConfiguration', 'icsrProfileConfiguration', 'icsrReportConfiguration'])
class IcsrTemplateQuery extends TemplateQuery{
    static auditable = [ignore:["displayMedDraVersionNumber","dateRangeInformationForTemplateQuery","queryLevel","headerDateRange","draftOnly","headerProductSelection"]]

    AuthorizationTypeEnum authorizationType
    String productType
    Integer icsrMsgType
    DistributionChannelEnum distributionChannelName
    Integer orderNo = 0
    EmailConfiguration emailConfiguration
    boolean isExpedited


        static propertiesToUseWhileCopying = ['template', 'query', 'queryLevel', 'header', 'title', 'footer', 'granularity', 'headerProductSelection', 'headerDateRange', 'draftOnly', 'privacyProtected', 'blindProtected','displayMedDraVersionNumber','authorizationType','productType','dueInDays','icsrMsgType','distributionChannelName','orderNo','isExpedited']

    static mapping = {
        table name: "ICSR_TEMPLT_QUERY"
        authorizationType column: "AUTHORIZATION_TYPE"
        productType column: "PRODUCT_TYPE"
        icsrMsgType column: "ICSR_MSG_TYPE"
        distributionChannelName column: "DIST_CHANNEL_NAME"
        orderNo column: "ORDER_NO"
        emailConfiguration column: "EMAIL_CONFIGURATION_ID"
        isExpedited column: "IS_EXPEDITED"
        version false
    }

    static constraints = {
        authorizationType nullable: true
        productType nullable: true
        dueInDays (blank: false, nullable: false, validator: { val, obj ->
            if (obj.report instanceof IcsrProfileConfiguration && val < 1) {
                return "com.rxlogix.config.configuration.dueInDays.positiveNumber"
            }
        })
        icsrMsgType (nullable: false, blank: false)
        distributionChannelName nullable: true
        orderNo nullable: true
        draftOnly(nullable: true)
        headerDateRange(nullable:true)
        queryLevel(nullable: true)
        emailConfiguration(nullable: true)
    }

    String getInstanceIdentifierForAuditLog() {
        return "ICSR Template Query (ID:${id})"
    }

}
