package com.rxlogix.config

import com.rxlogix.LibraryFilter
import com.rxlogix.OrderByUtil
import com.rxlogix.enums.IcsrCaseStateEnum
import com.rxlogix.enums.IcsrCaseStatusEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.util.FilterTranslatableUtil
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order

class IcsrCaseTracking implements  Serializable {

    Long exIcsrProfileId
    Long exIcsrTemplateQueryId
    Long queryId
    Long templateId
    Long profileId
    String caseNumber
    Long versionNumber
    String e2BStatus
    String profileName
    Date generationDate
    Date scheduledDate
    Date submissionDate
    Date caseReceiptDate
    Date safetyReceiptDate
    Date dueDate
    String productName
    String eventPreferredTerm
    String susar
    Long followupNumber
    Date transmissionDate
    Date modifiedDate
    String followupInfo
    String downgrade
    Integer dueInDays
    Date awareDate
    String ackFileName
    Boolean isGenerated
    Long tenantId
    String recipient
    Long caseId
    Integer flagLocalCp
    Long processedReportId
    Boolean flagLocalCpRequired
    Boolean flagAutoGenerate
    String submissionFormDesc
    Date preferredDateTime
    String timeZoneOffset
    Boolean flagCaseLocked
    String localReportMessage
    Boolean manualFlag
    Long authId
    Boolean isExpedited
    Long authorizationTypeId
    Long reportCategoryId
    Long originalSectionId
    Boolean flagPmda
    Long authIdInt
    String authorizationType
    String approvalNumber
    Boolean regenerateFlag
    Integer reportType
    String prodHashCode
    Boolean isReport
    String sectionTitle
    String templateName

    static mapping = {
        datasource "pva"
        table name: "vw_e2b_case_tracking_details"
        version false
        id composite: ['exIcsrProfileId', 'exIcsrTemplateQueryId', 'caseNumber', 'versionNumber']
        exIcsrProfileId column: 'OBJECT_ID'
        exIcsrTemplateQueryId column: 'SECTION_ID'
        queryId column: 'SCHDEULING_CRITERIA_ID'
        templateId column: 'TEMPLATE_ID'
        caseNumber column: 'CASE_NUM'
        versionNumber column: 'VERSION_NUM'
        profileId column: 'PROFILE_ID'
        e2BStatus column: 'E2B_STATUS'
        profileName column: 'PARTNER_NAME'
        generationDate column: 'GENERATION_DATE'
        scheduledDate column: 'SCHEDULED_DATE'
        submissionDate column: 'SUBMISSION_DATE'
        caseReceiptDate column: 'CASE_REPT_DATE'
        safetyReceiptDate column: 'SAFETY_REPT_DATE'
        dueDate column: 'DUE_DATE'
        productName column: 'PRODUCT_NAME'
        eventPreferredTerm column: 'EVENT_NAME'
        susar column: 'SUSAR'
        followupNumber column: 'FOLLOWUP_NUM'
        transmissionDate column: 'TRANSMISSION_DATE'
        modifiedDate column: 'MODIFIED_DATE'
        followupInfo column: 'FOLLOWUP_INFO'
        downgrade column: 'DOWNGRADE'
        dueInDays column: 'DUE_IN_DAYS'
        awareDate column: 'AWARE_DATE'
        ackFileName column: 'ACK_FILE_NAME'
        recipient column: 'PARTNER_RECIPIENT_NAME'
        isGenerated column: 'IS_GENERATED'
        tenantId column: 'TENANT_ID'
        caseId column: 'CASE_ID'
        processedReportId column: 'PROCESSED_REPORT_ID'
        flagLocalCp column: 'FLAG_LOCAL_CP'
        flagLocalCpRequired column: 'FLAG_LCP_GEN'
        flagAutoGenerate column: 'FLAG_AUTO_GEN'
        submissionFormDesc column: 'SUBMISSION_FORM_DESC'
        preferredDateTime column: 'SUBMISSION_DATE_LOCAL'
        timeZoneOffset column: 'AGENCY_TZ_OFFSET'
        flagCaseLocked column: 'FLAG_CASE_LOCKED'
        localReportMessage column: 'LOCAL_REPORT_MESSAGE'
        manualFlag column: 'MANUAL_FLAG'
        authId column: 'AUTH_ID'
        isExpedited column: 'FLAG_EXPEDITED'
        authorizationTypeId column: 'AUTHORIZATION_TYPE_ID'
        approvalNumber column: 'APPROVAL_NUMBER'
        reportCategoryId column: 'RPT_CATEGORY_ID'
        originalSectionId column: 'ORIGINAL_SECTION_ID'
        authorizationType column: 'AUTHORIZATION_TYPE'
        flagPmda column: 'FLAG_PMDA'
        authIdInt column: 'AUTH_ID_OF_INT'
        regenerateFlag column: 'FLAG_REGENERATE'
        reportType column: 'PMDA_RPT_TYPE'
        prodHashCode colummn: 'PROD_HASH_CODE'
        isReport column: 'IS_REPORT'
        sectionTitle column: 'SECTION_TITLE'
        templateName column: 'TEMPLATE_NAME'
    }

    static constraints = {

    }


    static namedQueries = {

        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        getAllByFilter { LibraryFilter filter, String caseNumber = null, Long versionNumber = null, Long exIcsrProfileId = null, Long exIcsrTemplateQueryId = null, String icsrCaseStateEnum = null, List<Long> profileIds, def searchData, boolean isAdmin, String sort, String direction = "asc" ->
            if (sort == 'profileName') {
                order(OrderByUtil.concatOrderIgnoreCase(['profileName', 'manualFlag', 'downgrade', 'sectionTitle'], direction))
            } else {
                order(new Order("${sort}", "${direction.toLowerCase()}" == "asc").ignoreCase())
            }
            if (filter.search) {
                or {
                    iLikeWithEscape('caseNumber', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('profileName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('approvalNumber', "%${EscapedILikeExpression.escapeString(filter.search)}%")

// TODO: uncomment after performance optimization (after discussions only caseNumber/profileName/approvalNumber is used for now)
//
//                    iLikeWithEscape('recipient', "%${EscapedILikeExpression.escapeString(filter.search)}%")
//                    iLikeWithEscape('profileName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
//
//                    List<String> authorizationTypeValues = FilterTranslatableUtil.getFilterValuesByInputValue('icsrTrackingAuthorizationType', filter.search)
//                    if (!authorizationTypeValues?.isEmpty()) {
//                        'in'('authorizationType', authorizationTypeValues)
//                    }
//
//                    List<String> followupInfoValues = FilterTranslatableUtil.getFilterValuesByInputValue('icsrTrackingFollowupInfo', filter.search)
//                    if (!followupInfoValues?.isEmpty()) {
//                        'in'('followupInfo', followupInfoValues)
//                    }


                }
            }
            if (filter.advancedFilterCriteria) {
                filter.advancedFilterCriteria.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }
            if (searchData) {
                searchData.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }

            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }

            if (caseNumber) {
                eq('caseNumber', caseNumber)
            }

            if (!isAdmin) {
                profileIds.collate(999).each {
                    'in'('profileId', it)
                }
            }

            if (exIcsrProfileId) { //TODO need to do better handling
                eq('exIcsrProfileId', exIcsrProfileId)
            }

            if (exIcsrTemplateQueryId) {
                eq('exIcsrTemplateQueryId', exIcsrTemplateQueryId)
            }

            if(versionNumber) {
                eq('versionNumber', versionNumber)
            }

            if(filter.icsrWidgets) {
                'in'('e2BStatus', ["SCHEDULED", "GENERATED"])
            }else {
                if(icsrCaseStateEnum) {
                    if (icsrCaseStateEnum == IcsrCaseStateEnum.READY_FOR_LOCAL_CP.toString()) {
                        eq('e2BStatus',IcsrCaseStateEnum.SCHEDULED.toString())
                        eq('flagCaseLocked', true)
                        eq('flagLocalCpRequired', true)
                        isNull('flagLocalCp')
                    } else if (icsrCaseStateEnum != 'SCHEDULED' && icsrCaseStateEnum != 'SUBMISSION_NOT_REQUIRED' ) {
                        if(icsrCaseStateEnum == 'PENDING_ACTIONS'){
                            'in'('e2BStatus',['SCHEDULED','GENERATED','GENERATION_ERROR','TRANSMISSION_ERROR','TRANSMITTED','TRANSMITTED_ATTACHMENT','PARSER_REJECTED','COMMIT_REJECTED'])
                        }
                        else if(icsrCaseStateEnum == 'OVERDUE_AND_DUE_SOON'){
                            isNull('submissionDate')
                            lt('dueDate', new Date() + 2)
                        } else {
                            eq('e2BStatus', icsrCaseStateEnum)
                        }
                    }
                    else if(icsrCaseStateEnum == 'SUBMISSION_NOT_REQUIRED'){
                        'in'('e2BStatus',['SUBMISSION_NOT_REQUIRED','SUBMISSION_NOT_REQUIRED_FINAL'])
                    }
                    else {
                        'in'('e2BStatus', ["SCHEDULED", "MANUAL"])
                    }
                }
            }

        }

        getAllTransmittingCases {
            'in'('e2BStatus', [IcsrCaseStateEnum.TRANSMITTING.toString(), IcsrCaseStateEnum.TRANSMITTING_ATTACHMENT.toString()])
        }

        fetchAllPostiveAckXMLCases {
            eq('e2BStatus', IcsrCaseStateEnum.COMMIT_RECEIVED.toString())
        }

        getAllTransmittedCases {
            'in'('e2BStatus', [IcsrCaseStateEnum.TRANSMITTED.toString(), IcsrCaseStateEnum.TRANSMITTED_ATTACHMENT.toString()])
        }

    }

    Boolean isJapanProfile() {
        if ((reportType == -1 || reportType == null)){
            return false
        } else {
            return true
        }
    }

    String uniqueIdentifier() {
        return "${exIcsrProfileId}**${exIcsrTemplateQueryId}**${caseNumber}**${versionNumber}"
    }
}
