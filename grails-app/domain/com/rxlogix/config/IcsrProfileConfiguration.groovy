package com.rxlogix.config

import com.rxlogix.LibraryFilter
import com.rxlogix.OrderByUtil
import com.rxlogix.enums.*
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.util.ViewHelper
import com.rxlogix.user.FieldProfile
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.MiscUtil
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.util.Holders
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order

@CollectionSnapshotAudit
class IcsrProfileConfiguration extends ReportConfiguration {
    static auditable = [ignore: ["excludeFollowUp", "limitPrimaryPath", "evaluateDateAs", "qbeForm", "isTemplate", "generateDraft", "sourceProfile", "includeAllStudyDrugsCases", "includeLockedVersion", "adjustPerScheduleFrequency", "isPublisherReport", "needPaperReport", "removeOldVersion", "generateCaseSeries", "includeMedicallyConfirmedCases", "signalConfiguration", "comparatorReporting", "includeNonSignificantFollowUp", "isPriorityReport", "executing", "totalExecutionTime", "numOfExecutions", "nextRunDate", "isEnabled", "lastUpdated"]]
    UnitConfiguration recipientOrganization
    boolean comparatorReporting = false
    boolean autoTransmit = false
    boolean autoSubmit = false
    Boolean adjustDueDate = false
    boolean autoScheduleFUPReport = false
    boolean awareDate = false
    boolean needPaperReport = false
    UnitConfiguration senderOrganization
    String assignedValidation
    DistributionChannel e2bDistributionChannel
    FieldProfile fieldProfile
    IcsrProfileSubmissionDateOptionEnum submissionDateFrom
    IcsrProfileDueDateOptionsEnum dueDateOptionsEnum
    IcsrProfileDueDateAdjustmentEnum dueDateAdjustmentEnum
    Set<Long> calendars = []


    Boolean includeProductObligation = false
    Boolean includeStudyObligation = false
    Boolean autoScheduling = false
    Boolean autoGenerate = true
    Boolean localCpRequired = false
    Boolean manualScheduling = false
    Boolean deviceReportable = false
    Boolean includeNonReportable = false
    Boolean includeOpenCases = false
    Boolean multipleReport = false
    Boolean isJapanProfile = false
    Boolean isProductLevel = false
    boolean excludeDeletedCases = false

    transient boolean mandatoryAudit = false
    static transients = ['ruleEvaluation']


    public IcsrProfileConfiguration() {
        this.isEnabled = false
    }

    static List<String> propertiesToUseWhileCopying = ['description', 'isDeleted', 'tags', 'dateRangeType', 'sourceProfile', 'productSelection', 'studySelection', 'eventSelection','productGroupSelection','eventGroupSelection', 'configSelectedTimeZone', 'asOfVersionDate', 'evaluateDateAs', 'excludeFollowUp', 'includeAllStudyDrugsCases', 'adjustPerScheduleFrequency', 'suspectProduct', 'globalQuery', 'generateCaseSeries','generateDraft', 'excludeNonValidCases', 'limitPrimaryPath', 'blankValuesJSON', 'includeMedicallyConfirmedCases', 'dueInDays','recipientOrganization','comparatorReporting','autoTransmit','autoSubmit' ,'autoScheduling','autoGenerate' ,'senderOrganization','assignedValidation','includeLockedVersion','needPaperReport', 'fieldProfile','tenantId','adjustDueDate','dueDateOptionsEnum','dueDateAdjustmentEnum','includeProductObligation','includeStudyObligation','autoScheduleFUPReport','localCpRequired', 'deviceReportable', 'includeNonReportable', 'includeOpenCases' ,'submissionDateFrom', 'isMultiIngredient', 'includeWHODrugs', 'awareDate', 'multipleReport', 'isJapanProfile', 'isProductLevel']

    static hasMany = [authorizationTypes: Long]

    static mapping = {
        recipientOrganization column: "RECIPIENT_ORG_ID"
        comparatorReporting column: "COMPARATOR_REPORTING"
        autoTransmit column: "AUTO_TRANSMIT"
        adjustDueDate column: "ADJUST_DUE_DATE"

        autoSubmit column: "AUTO_SUBMIT"
        autoScheduleFUPReport column: "AUTO_SCHEDULE_FUP_REPORT"
        awareDate column: 'AWARE_DATE'
        senderOrganization column: "SENDER_ORG_ID"
        assignedValidation column: "ASSIGNED_VALIDATION"
        e2bDistributionChannel column: "EB_DISTRIBUTION_CHANNEL_ID"
        needPaperReport column: "NEED_PAPER_REPORT"
        fieldProfile column: "FIELD_PROFILE_ID"
        submissionDateFrom column: "SUBMISSION_DATE_FROM"
        includeProductObligation column: "INCLUDE_PRODUCT_OBLIGATION"
        includeStudyObligation column: "INCLUDE_STUDY_OBLIGATION"
        dueDateOptionsEnum column: "DUE_DATE_ADJUSTMENT_OPTIONS"
        dueDateAdjustmentEnum column: "DUE_DATE_ADJUSTMENT"
        calendars joinTable: [name: "CONFIG_CALENDAR", column: "CALENDAR_ID", key: "RCONFIG_ID"]
        autoScheduling column: 'AUTO_SCHEDULING'
        autoGenerate column: 'AUTO_GENERATE'
        localCpRequired column: 'LOCAL_CP_REQUIRED'
        manualScheduling column: 'MANUAL_SCHEDULING'
        deviceReportable column: 'DEVICE_REPORTABLE'
        includeNonReportable column: 'INCLUDE_NON_REPORTABLE'
        includeOpenCases column: 'INCLUDE_OPEN_CASES'
        authorizationTypes joinTable: [name: "ICSR_PROFILE_AUTH_TYPE", column: "AUTHORIZATION_ID", key: "RCONFIG_ID"]
        multipleReport column: 'MULTI_REPORT'
        isJapanProfile column: 'JAPAN_PROFILE'
        isProductLevel column: 'PRODUCT_LEVEL'
    }

    static constraints = {
        reportName(validator: {val, obj ->
            if (!val || !val.trim()) {
                return "com.rxlogix.config.Configuration.reportName.nullable"
            }
            // Check for invalid/dangerous content
            if (!MiscUtil.validateContent(val)) {
                return "com.rxlogix.config.Configuration.reportName.invalid.content"
            }
            //Name is unique to user
            if (!obj.id || obj.isDirty("reportName") || obj.isDirty("owner")) {
                long count = IcsrProfileConfiguration.createCriteria().count{
                    ilike('reportName', "${val}")
                    eq('isDeleted', false)
                    if (obj.id){ne('id', obj.id)}
                }
                if (count) {
                    return "com.rxlogix.config.configuration.name.unique.per.user"
                }
            }
        })
        recipientOrganization(nullable: false)
        senderOrganization(nullable: false)
        assignedValidation(nullable: true)
        deliveryOption(nullable: true)
        e2bDistributionChannel(nullable: true)
        fieldProfile(nullable: true)
        isDisabled(bindable: true)
        submissionDateFrom(nullable: true)
        includeProductObligation(nullable: true)
        includeStudyObligation(nullable: true)
        dueDateAdjustmentEnum(nullable: true)
        dueDateOptionsEnum(nullable: true)
        adjustDueDate(nullable: true)
        autoScheduling(nullable: true)
        autoGenerate(nullable: true)
        awareDate(nullable: true)
        manualScheduling(nullable: true)
        includeNonReportable(nullable: true)
        includeOpenCases(nullable: true)
        multipleReport(nullable: true)
        isJapanProfile(nullable: true)
        isProductLevel(nullable: true)
        // ICSR Profile can't have nextRunDate/scheduleDateJSON as it has automatic execution
        nextRunDate(nullable: true, validator: { val ->
            if(val){
                return false
            }
        })
        scheduleDateJSON(nullable: true, validator: { val ->
            if(val){
                return false
            }
        })
        autoScheduling(validator: { val, obj ->
            if(val && obj.manualScheduling) {
                return "com.rxlogix.config.configuration.auto.manual.both.cannot.be.true"
            }
        })
        deviceReportable(nullable: true)
        authorizationTypes(nullable: false, validator: { val, obj ->
            if (obj.isJapanProfile) {
                if (val.size() == 0) {
                    return "com.rxlogix.config.IcsrProfileConfiguration.authorizationTypes.nullable"
                } else if (val.size() > 1) {
                    return "com.rxlogix.config.configuration.authorization.should.be.single.for.japan.profile"
                }
            }
            if (val.size() > 2) {
                return "com.rxlogix.config.configuration.invalid.authorization.types"
            }
            def icsrService = Holders.applicationContext.getBean("icsrProfileConfigurationService")
            if (icsrService.hasIssuesInAuthorizationType(val)) {
                return "com.rxlogix.config.configuration.invalid.authorization.types"
            }
        })
        description(validator: { val, obj ->
            if (!MiscUtil.validateContent(val)) {
                return "com.rxlogix.config.Configuration.description.invalid.content"
            }
        })

    }

    static namedQueries = {

        listOfEligibleForExecute { List include, List exclude ->
            eq 'isEnabled', true
            eq 'isDeleted', false
            eq 'manualScheduling', false
            eq 'autoScheduling', true
            if (exclude) {
                not {
                    'in'('id', exclude)
                }
            }
            if (include) {
                'in'('id', include)
            }
        }

        getAllSearchByReportName { String search ->
            if (search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            isNotNull('reportName')
            eq('isDeleted', false)
            eq('isEnabled', true)
        }

        getAllActiveIcsrProfiles { String search ->
            eq('isEnabled', true)
            getAllIcsrProfileConfBySearchString(search)
        }

        getAllIcsrProfileConfBySearchString { String search ->
            if (search) {
                iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
            eq('isDeleted', false)
            order('reportName', 'asc')
        }

        fetchAllIcsrProfileForReciver { String receiverId, String search ->
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property('reportName', 'reportName')
                property("id", "id")
                property("numOfExecutions", "numOfExecutions")
            }
            if (search) {
                iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
            eq('isDeleted', false)
            'recipientOrganization' {
                eq('unitRegisteredId', receiverId)
            }
            gt('numOfExecutions', 0)
            order('reportName', 'asc')
        }

        fetchAllForIcsrReport{ String search ->
            gt('numOfExecutions',0)
            getAllIcsrProfileConfBySearchString(search)
        }

        fetchAllProfileIds { User currentUser, Boolean isAdmin, Boolean includeArchived ->
            projections {
                distinct('id')
            }
            ownedByAndSharedWithUser(currentUser, isAdmin, includeArchived)
        }

        ownedByAndSharedWithUser { User currentUser, Boolean isAdmin, Boolean includeArchived ->
            if (!isAdmin) {
                createAlias('deliveryOption', 'do', CriteriaSpecification.LEFT_JOIN)
                createAlias('do.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
                createAlias('do.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
                eq("isDeleted", false)

                or {
                    currentUser.getUserTeamIds().collate(999).each { 'in'('owner.id', it) }
                    eq('owner.id', currentUser.id)
                    'in'('sw.id', currentUser.id)
                    if (UserGroup.countAllUserGroupByUser(currentUser)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(currentUser).id)
                    }
                }
            }
        }

        fetchByProfileName { User currentUser, Boolean isAdmin, Boolean includeArchived, String term = '' ->
            projections {
                distinct('id')
                property("reportName")
            }
            if(term){
                iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(term)}%")
            }
            ownedByAndSharedWithUser(currentUser, isAdmin, includeArchived)
        }

        getAllIdsByFilter { LibraryFilter filter, boolean showXMLOption = false, String sortBy = null, String sortDirection = "asc"  ->
            projections {
                distinct('id')
                property("dateCreated")
                property("lastUpdated")
                property("numOfExecutions")
                property("reportName")
                property("description")
                property("qualityChecked")
                property("isEnabled")

                property("senderOrganization.unitName", "senderOrganizationName")
                property("recipientOrganization.unitName", "recipientOrganizationName")
                property("senderOrganizationType.name", "senderType")
                property("recipientOrganizationType.name", "recipientType")

                'owner' {
                    property("fullName", "ownerFullName")
                }
            }

            getAllRecordsByFilter(filter)

            if(!showXMLOption){
                templateQueries{
                    template{
                        ne("templateType", TemplateTypeEnum.ICSR_XML)
                    }
                }
            }
            if (sortBy) {
                Order orderBy
                if (sortBy == 'qualityChecked' || sortBy == 'isEnabled') {
                    orderBy = OrderByUtil.booleanOrder(sortBy, sortDirection)
                } else {
                    String property = sortBy
                    if (sortBy == 'senderOrganization') {
                        property = 'senderOrganizationName'
                    } else if (sortBy == 'recipientOrganization') {
                        property = 'recipientOrganizationName'
                    } else if (sortBy == 'owner.fullName') {
                        property = 'ownerFullName'
                    }
                    orderBy = new Order("${property}", "${sortDirection}" == "asc").ignoreCase()
                }
                order(orderBy)
            }
        }

        getAllRecordsByFilter { LibraryFilter filter ->
            createAlias("senderOrganization", "senderOrganization", CriteriaSpecification.LEFT_JOIN)
            createAlias("recipientOrganization", "recipientOrganization", CriteriaSpecification.LEFT_JOIN)
            createAlias("senderOrganization.organizationType", "senderOrganizationType", CriteriaSpecification.LEFT_JOIN)
            createAlias("recipientOrganization.organizationType", "recipientOrganizationType", CriteriaSpecification.LEFT_JOIN)
            if (filter.search) {
                or {
                    iLikeWithEscape('reportName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    iLikeWithEscape('senderOrganization.unitName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('recipientOrganization.unitName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('senderOrganizationType.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('recipientOrganizationType.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    if (filter.search == "qced") {
                        eq('qualityChecked', true)
                    }
                }
            }
            if (filter.advancedFilterCriteria) {
                filter.advancedFilterCriteria.each {closure ->
                    closure.delegate = delegate
                    closure.call()
                }
            }
            if (filter.templateTypes) {
                templateQueries {
                    'template' {
                        'in'('templateType', filter.templateTypes)
                        if (filter.showChartSheet != null) {
                            eq('showChartSheet', filter.showChartSheet)
                        }
                    }
                }
            }
            ownedByAndSharedWithUser(filter.user, SpringSecurityUtils.ifAnyGranted("ROLE_ICSR_DISTRIBUTION_ADMIN"), false)
            eq('isDeleted', false)

            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                eq('tenantId', (Tenants.currentId() as Long))
            }
        }

        countRecordsByFilter { LibraryFilter filter, boolean showXMLOption = false ->
            projections {
                countDistinct("id")
            }

            getAllRecordsByFilter(filter)

            if(!showXMLOption){
                templateQueries{
                    template{
                        ne("templateType", TemplateTypeEnum.ICSR_XML)
                    }
                }
            }
        }

        fetchAllAwareDateProfileId {
            projections {
                distinct('id')
            }
            eq('isDeleted', false)
            eq('isEnabled', true)
            eq('awareDate', true)
        }
    }

    transient boolean setIsDisabled(boolean isEnabled) {
        this.isEnabled = !isEnabled
    }

    transient boolean getIsDisabled() {
        !this.isEnabled
    }

    boolean hasAnyE2BDistributionChannel() {
        return templateQueries.any { it.distributionChannelName == DistributionChannelEnum.PV_GATEWAY || it.distributionChannelName == DistributionChannelEnum.EXTERNAL_FOLDER}
    }

    boolean hasEmailAsE2BDistributionChannel() {
        return templateQueries.any { it.distributionChannelName == DistributionChannelEnum.EMAIL}
    }

    @Override
    String getConfigType() {
        return ConfigTypes.ICSR_PROFILE_CONFIGURATION
    }

    boolean isPMDAReport() {
        if ((recipientOrganization.organizationCountry == "JAPAN" || recipientOrganization.organizationCountry == '日本') && (recipientOrganization.organizationType.name == "Regulatory Authority" || recipientOrganization.organizationType.name == "規制当局")) {
            return true
        }
        return false
    }

    String getRuleEvaluation() {
        if (deviceReportable) return IcsrRuleEvaluationEnum.DEVICE_REPORTING
        if (isProductLevel) return IcsrRuleEvaluationEnum.PRODUCT_LEVEL
        if (multipleReport) return IcsrRuleEvaluationEnum.CLINICAL_RESEARCH_MEASURE_REPORT
        return null
    }

    void setRuleEvaluation(String value) {
        deviceReportable = value == IcsrRuleEvaluationEnum.DEVICE_REPORTING.toString()
        isProductLevel = value == IcsrRuleEvaluationEnum.PRODUCT_LEVEL.toString()
        multipleReport = value == IcsrRuleEvaluationEnum.CLINICAL_RESEARCH_MEASURE_REPORT.toString()
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && oldValues) {
            newValues.put("authorizationTypes", authorizationTypes?.join(", "))
            withNewSession {
                IcsrProfileConfiguration cfg = IcsrProfileConfiguration.read(id);
                oldValues.put("authorizationTypes", cfg?.authorizationTypes?.collect { it.toString() }?.join(", "))
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }
}