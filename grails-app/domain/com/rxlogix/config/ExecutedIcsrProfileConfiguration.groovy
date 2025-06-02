package com.rxlogix.config

import com.rxlogix.enums.IcsrProfileSubmissionDateOptionEnum
import com.rxlogix.enums.IcsrProfileDueDateAdjustmentEnum
import com.rxlogix.enums.IcsrProfileDueDateOptionsEnum


import com.rxlogix.enums.TitleEnum
import com.rxlogix.user.FieldProfile
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.hibernate.EscapedILikeExpression
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Restrictions
import com.rxlogix.enums.IcsrRuleEvaluationEnum
@DirtyCheck
@CollectionSnapshotAudit
class ExecutedIcsrProfileConfiguration extends ExecutedReportConfiguration {

    TitleEnum recipientTitle
    String recipientFirstName
    String recipientMiddleName
    String recipientLastName
    String recipientDept
    String recipientOrganizationName
    String recipientTypeName
    Integer recipientTypeId
    String recipientCountry
    String receiverId
    String recipientPartnerRegWith
    String recipientType
    String recipientAddress1
    String recipientAddress2
    String recipientState
    String recipientPostcode
    String recipientCity
    String recipientPhone
    String recipientFax
    String recipientEmail
    boolean comparatorReporting = false
    boolean autoTransmit = false
    boolean autoSubmit = false
    Boolean adjustDueDate = false
    boolean autoScheduleFUPReport = false
    boolean awareDate = false
    boolean multipleReport = false
    boolean  isJapanProfile = false
    boolean isProductLevel = false
    TitleEnum senderTitle
    String senderFirstName
    String senderMiddleName
    String senderLastName
    String senderOrganizationName
    String senderTypeName
    Integer senderTypeId
    String senderDept
    String senderCountry
    String senderId
    String senderPartnerRegWith
    String assignedValidation
    String address1
    String address2
    String city
    String state
    String postalCode
    String phone
    String email
    String fax
    String xsltName
    DistributionChannel e2bDistributionChannel
    FieldProfile fieldProfile
    boolean needPaperReport = false
    IcsrProfileSubmissionDateOptionEnum submissionDateFrom
    Boolean includeProductObligation = false
    Boolean includeStudyObligation = false
    Boolean includeNonReportable = false
    Boolean includeOpenCases = false
    String allowedAttachments
    String senderCompanyName
    String recipientCompanyName
    String senderUnitOrganizationName
    String recipientUnitOrganizationName
    String preferredTimeZone
    String holderId
    String senderHolderId
    String xmlVersion
    String xmlEncoding
    String xmlDoctype
    String unitAttachmentRegId
    String recipientPrefLanguage
    String senderPrefLanguage

    IcsrProfileDueDateAdjustmentEnum dueDateAdjustmentEnum
    IcsrProfileDueDateOptionsEnum dueDateOptionsEnum
    Set<Long> calendars = []


    Boolean autoScheduling = false
    Boolean autoGenerate = true
    Boolean localCpRequired = false
    Boolean manualScheduling = false
    Boolean deviceReportable = false

    static hasMany = [authorizationTypes: Long]

    static transients = ['ruleEvaluation']

    static mapping = {
        recipientTitle column : "RECIPIENT_TITLE"
        recipientFirstName column : "RECIPIENT_FIRST_NAME"
        recipientMiddleName column : "RECIPIENT_MIDDLE_NAME"
        recipientLastName column : "RECIPIENT_LAST_NAME"
        recipientDept column: "RECIPIENT_DEPT"
        recipientOrganizationName column: "RECIPIENT_ORG_NAME"
        recipientTypeName column: "RECIPIENT_TYPE_NAME"
        recipientTypeId column: "RECIPIENT_TYPE_ID"
        recipientCountry column: "RECIPIENT_COUNTRY"
        receiverId column: "RECEIVER_ID"
        recipientPartnerRegWith column: "RECIPIENT_PARTNER_REG_WITH"
        recipientType column: "RECIPIENT_TYPE"
        recipientAddress1 column: "RECIPIENT_ADDRESS1"
        recipientAddress2 column: "RECIPIENT_ADDRESS2"
        recipientState column: "RECIPIENT_STATE"
        recipientPostcode column: "RECIPIENT_POST_CODE"
        recipientCity column: "RECIPIENT_CITY"
        recipientPhone column: "RECIPIENT_PHONE"
        recipientFax column: "RECIPIENT_FAX"
        recipientEmail column: "RECIPIENT_EMAIL"
        comparatorReporting column: "COMPARATOR_REPORTING"
        adjustDueDate column: "ADJUST_DUE_DATE"
        autoTransmit column: "AUTO_TRANSMIT"
        autoSubmit column: "AUTO_SUBMIT"
        autoScheduleFUPReport column: "AUTO_SCHEDULE_FUP_REPORT"
        awareDate column: 'AWARE_DATE'
        multipleReport column: 'MULTI_REPORT'
        isJapanProfile column: 'JAPAN_PROFILE'
        isProductLevel column: 'PRODUCT_LEVEL'
        senderTitle column : "SENDER_TITLE"
        senderFirstName column : "SENDER_FIRST_NAME"
        senderMiddleName column : "SENDER_MIDDLE_NAME"
        senderLastName column : "SENDER_LAST_NAME"
        senderOrganizationName column: "SENDER_ORG_NAME"
        senderTypeName column: "SENDER_TYPE_NAME"
        senderTypeId column: "SENDER_TYPE_ID"
        senderDept column: "SENDER_DEPT"
        senderCountry column: "SENDER_COUNTRY"
        senderId column: "SENDER_ID"
        senderPartnerRegWith column: "SENDER_PARTNER_REG_WITH"
        assignedValidation column: "ASSIGNED_VALIDATION"
        address1 column: "ADDRESS1"
        address2 column: "ADDRESS2"
        city column: "CITY"
        state column: "STATE"
        postalCode column: "POSTAL_CODE"
        phone column: "PHONE"
        email column: "EMAIL"
        fax column: "FAX"
        xsltName column: "XSLT_NAME"
        e2bDistributionChannel column: "EX_EB_DISTRIBUTION_CHANNEL_ID"
        fieldProfile column: "FIELD_PROFILE_ID"
        needPaperReport column: "NEED_PAPER_REPORT"
        submissionDateFrom column: "SUBMISSION_DATE_FROM"
        includeProductObligation column: "INCLUDE_PRODUCT_OBLIGATION"
        includeStudyObligation column: "INCLUDE_STUDY_OBLIGATION"
        dueDateAdjustmentEnum column: "DUE_DATE_ADJUSTMENT"
        dueDateOptionsEnum column: "DUE_DATE_ADJUSTMENT_OPTIONS"
        calendars joinTable: [name: "EX_CONFIG_CALENDAR", column: "CALENDAR_ID", key: "EX_RCONFIG_ID"]

        allowedAttachments column: "ALLOWED_ATTACHMENTS"
        recipientCompanyName column: "RECEIVER_COMPANY_NAME"
        senderCompanyName column: "SENDER_COMPANY_NAME"
        senderUnitOrganizationName column: "RECEIVER_UNIT_ORG_NAME"
        recipientUnitOrganizationName column: "SENDER_UNIT_ORG_NAME"
        autoScheduling column: "AUTO_SCHEDULING"
        autoGenerate column: "AUTO_GENERATE"
        localCpRequired column: "LOCAL_CP_REQUIRED"
        manualScheduling column: "MANUAL_SCHEDULING"
        deviceReportable column: "DEVICE_REPORTABLE"
        preferredTimeZone column: "PREFERRED_TIME_ZONE"
        includeNonReportable column: 'INCLUDE_NON_REPORTABLE'
        holderId column: "HOLDER_ID"
        senderHolderId column: "SENDER_HOLDER_ID"
        includeOpenCases column: 'INCLUDE_OPEN_CASES'
        xmlVersion column: 'XML_VERSION'
        xmlEncoding column: 'XML_ENCODING'
        xmlDoctype column: 'XML_DOCTYPE'
        unitAttachmentRegId column: 'UNIT_ATTACHMENT_REG_ID'
        recipientPrefLanguage column: 'RECEIVER_PREF_LANGUAGE'
        senderPrefLanguage column: 'SENDER_PREF_LANGUAGE'
        authorizationTypes joinTable: [name: "EX_ICSR_PROFILE_AUTH_TYPE", column: "AUTHORIZATION_ID", key: "EX_RCONFIG_ID"]
    }

    static constraints = {
        recipientOrganizationName(nullable: true)
        recipientTypeName(nullable: true)
        recipientTypeId(nullable: true)
        recipientCountry(nullable: true)
        receiverId(nullable: true)
        recipientPartnerRegWith(nullable: true)
        recipientTitle(nullable: true)
        recipientFirstName(nullable: true)
        recipientMiddleName(nullable: true)
        recipientLastName(nullable: true)
        recipientDept(nullable: true)
        recipientType(nullable: true)
        recipientAddress1(nullable: true)
        recipientAddress2(nullable: true)
        recipientState(nullable: true)
        recipientPostcode(nullable: true)
        recipientCity(nullable: true)
        recipientPhone(nullable: true)
        recipientFax(nullable: true)
        recipientEmail(nullable: true)
        senderTitle(nullable: true)
        senderFirstName(nullable: true)
        senderMiddleName(nullable: true)
        senderLastName(nullable: true)
        senderOrganizationName(nullable: true)
        senderTypeName(nullable: true)
        senderTypeId(nullable: true)
        senderDept(nullable: true)
        senderCountry(nullable: true)
        senderId(nullable: true)
        senderPartnerRegWith(nullable: true)
        assignedValidation(nullable: true)
        address1(nullable: true)
        address2(nullable: true)
        city(nullable: true)
        state(nullable: true)
        postalCode(nullable: true)
        phone(nullable: true)
        email(nullable: true)
        fax(nullable: true)
        executedDeliveryOption(nullable: true)
        e2bDistributionChannel(nullable: true)
        fieldProfile nullable: true
        isDisabled bindable: true
        submissionDateFrom(nullable: true)
        includeProductObligation(nullable: true)
        includeStudyObligation(nullable: true)
        allowedAttachments(nullable: true)
        recipientCompanyName(nullable: true)
        senderCompanyName(nullable: true)
        senderUnitOrganizationName(nullable: true)
        recipientUnitOrganizationName(nullable: true)
        dueDateAdjustmentEnum(nullable: true)

        adjustDueDate(nullable:true)
        dueDateOptionsEnum(nullable: true)
        autoScheduling(nullable: true)
        autoGenerate(nullable: true)
        awareDate(nullable: true)
        multipleReport(nullable: true)
        isJapanProfile(nullable: true)
        isProductLevel(nullable: true)
        manualScheduling(nullable: true)
        workflowState(nullable: true)
        deviceReportable(nullable: true)
        preferredTimeZone(nullable: true)
        includeNonReportable(nullable: true)
        holderId(nullable: true, maxSize: 200)
        senderHolderId(nullable: true, maxSize: 200)
        includeOpenCases(nullable: true)
        xmlVersion(nullable: true)
        xmlEncoding(nullable: true)
        xmlDoctype(nullable: true)
        unitAttachmentRegId(nullable: true)
        recipientPrefLanguage(nullable: true)
        senderPrefLanguage(nullable: true)
    }

    static namedQueries = {

        countAllIcsrProfileConfBySearchString { String search, User currentUser ->
            projections {
                countDistinct("id")
            }
            getAllIcsrProfileConfBySearchStringQuery(search, currentUser)
        }
        getAllIcsrProfileConfBySearchString { String search, User currentUser ->
            projections {
                distinct('id')
                property("numOfExecutions")
                property("reportName")
                property("description")
                property("senderOrganizationName")
                property("senderTypeName")
                property("recipientOrganizationName")
                property("recipientTypeName")
                property("dateCreated")
                'owner' {
                    property("fullName", "fullName")
                }
                property("lastUpdated")
            }
            getAllIcsrProfileConfBySearchStringQuery(search, currentUser)
        }
        getAllIcsrProfileConfBySearchStringQuery { String search, User currentUser ->
               if (search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('recipientOrganizationName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('senderOrganizationName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('senderTypeName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('recipientTypeName', "%${EscapedILikeExpression.escapeString(search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                    }
                }
            }
            eq('isDeleted', false)
            ownedByAndSharedWithUser(currentUser, currentUser.isAdmin(), false)
        }

        ownedByAndSharedWithUser { User currentUser, Boolean isAdmin, Boolean includeArchived ->
            createAlias('executedDeliveryOption', 'exd', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
            eq("isDeleted", false)

            if (!isAdmin) {
                or {
                    currentUser.getUserTeamIds().collate(999).each { 'in'('owner.id', it) }
                    eq('owner.id', currentUser?.id)
                    'in'('sw.id', currentUser.id)
                    if (UserGroup.fetchAllUserGroupByUser(currentUser)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(currentUser).id)
                    }
                }
            }
        }
    }

    transient boolean getIsDisabled() {
        return !this.isEnabled
    }

    @Override
    public List<ExecutedTemplateQuery> getExecutedTemplateQueriesForProcessing() {
        return executedTemplateQueries.findAll { it }.sort { it.dueInDays }
    }

    boolean isPMDAReport() {
        if ((recipientCountry == "JAPAN" || recipientCountry == "日本") && (recipientType == "Regulatory Authority" || recipientType == "規制当局")) {
            return true
        }
        return false
    }

    transient String getSenderIdentifier() {
        return senderCompanyName ?: senderId
    }

    String getRuleEvaluation() {
        if (deviceReportable) return IcsrRuleEvaluationEnum.DEVICE_REPORTING
        if (isProductLevel) return IcsrRuleEvaluationEnum.PRODUCT_LEVEL
        if (multipleReport) return IcsrRuleEvaluationEnum.CLINICAL_RESEARCH_MEASURE_REPORT
        return null
    }


}
