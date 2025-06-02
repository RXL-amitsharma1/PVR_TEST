package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.user.User
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.ViewHelper
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.multitenancy.Tenants
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.util.Holders
@DirtyCheck
abstract class BaseConfiguration implements Serializable{
    @AuditEntityIdentifier
    String reportName
    User owner
    String scheduleDateJSON
    Date nextRunDate
    //Date and time the report was run; the start of the run; does not take into account the duration of the run.
    Date lastRunDate
    String description
    boolean isDeleted = false
    boolean isEnabled = true
    List<Tag> tags
    DateRangeType dateRangeType
    String pvqType

    String productSelection
    String productGroupSelection
    String studySelection
    String configSelectedTimeZone = "UTC"
    Boolean isPublisherReport = false
    Boolean removeOldVersion=false

    Date asOfVersionDate
    EvaluateCaseDateEnum evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
    boolean excludeFollowUp = false
    boolean includeLockedVersion = true
    boolean adjustPerScheduleFrequency = true
    boolean excludeNonValidCases = true
    boolean excludeDeletedCases = true
    boolean includeAllStudyDrugsCases = false
    boolean suspectProduct = false
    boolean limitPrimaryPath = false
    boolean includeMedicallyConfirmedCases = false
    boolean qualityChecked=false
    boolean includeNonSignificantFollowUp = false

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    int numOfExecutions = 0
    long totalExecutionTime = 0 // this will be in milliseconds
    String blankValuesJSON

    def configurationService
    Set<ParameterValue> poiInputsParameterValues
    SourceProfile sourceProfile

    boolean signalConfiguration
    DmsConfiguration dmsConfiguration
    Boolean isPriorityReport = false

    Long tenantId
    boolean isMultiIngredient = false
    boolean includeWHODrugs = false

    static mapWith = "none"

    static hasMany = [tags: Tag, poiInputsParameterValues: ParameterValue]

    static mapping = {
        reportName column: "REPORT_NAME"
        owner column: "PVUSER_ID", cascade: 'none'
        scheduleDateJSON column: "SCHEDULE_DATE"
        nextRunDate column: "NEXT_RUN_DATE"
        lastRunDate column: "LAST_RUN_DATE"
        description column: "DESCRIPTION"
        isDeleted column: "IS_DELETED"
        isEnabled column: "IS_ENABLED"
        dateRangeType column: "DATE_RANGE_TYPE"
        configSelectedTimeZone column: "SELECTED_TIME_ZONE"
        asOfVersionDate column: "AS_OF_VERSION_DATE"
        evaluateDateAs column: "EVALUATE_DATE_AS"
        excludeFollowUp column: "EXCLUDE_FOLLOWUP"
        includeMedicallyConfirmedCases column: "INCL_MEDICAL_CONFIRM_CASES"
        qualityChecked column: "QUALITY_CHECKED"
        includeLockedVersion column: "INCLUDE_LOCKED_VERSION"
        adjustPerScheduleFrequency column: "ADJUST_PER_SCHED_FREQUENCY"
        excludeNonValidCases column: "EXCLUDE_NON_VALID_CASES"
        excludeDeletedCases column: "EXCLUDE_DELETED_CASES"
        includeAllStudyDrugsCases column: "INCL_ALL_STUD_DRUG_CASES"
        numOfExecutions column: "NUM_OF_EXECUTIONS"
        totalExecutionTime column: "TOTAL_EXECUTION_TIME"
        suspectProduct column: "SUSPECT_PRODUCT"
        pvqType column: "PVQ_TYPE"
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        productGroupSelection column: "PRODUCT_GROUP_SELECTION", sqlType: DbUtil.longStringType
        studySelection column: "STUDY_SELECTION", sqlType: DbUtil.longStringType
        blankValuesJSON column: "BLANK_VALUES", sqlType: DbUtil.longStringType
        sourceProfile column: "SOURCE_PROFILE"
        includeNonSignificantFollowUp column: "INCL_NON_SIGNIFICANT_FOLLOWUP"
        dmsConfiguration column: "DMS_CONFIGURATION_ID"
        tenantId column: "TENANT_ID"
        isPublisherReport column: "IS_PUBLISHER_REPORT"
        isPriorityReport column: "IS_PRIORITY_REPORT"
        removeOldVersion column: "REMOVE_OLD_VERSION"
        isMultiIngredient column: "IS_MULTI_INGREDIENT"
        includeWHODrugs column: "INCLUDE_WHO_DRUGS"
    }

    static constraints = {
        reportName(nullable: false, maxSize: 500)
        description(nullable: true, maxSize: 4000)
        nextRunDate(nullable: true)
        lastRunDate(nullable: true)
        removeOldVersion(nullable: true)

        scheduleDateJSON(nullable: true, maxSize: 1024, validator: { val, obj ->
            if (val) {
                if (!MiscUtil.validateRecurrence(val)) {
                    return "com.rxlogix.config.weekly.dow.required"
                } else if (MiscUtil.isScheduleDateJSONEmpty(val)) {
                    return "com.rxlogix.config.scheduler.date.time.required"
                }
            }
        })

        blankValuesJSON(nullable: true, maxSize: 8192)
        lastUpdated(nullable: true)
        tags(nullable: true, blank: true)
        dateRangeType(nullable: true)
        productSelection(nullable:true)
        studySelection(nullable:true)
        evaluateDateAs(nullable:false)
        numOfExecutions(min:0, nullable:false)
        createdBy(nullable: false, maxSize: 255)
        modifiedBy(nullable: false, maxSize: 255)
        includeMedicallyConfirmedCases(nullable: false)
        pvqType(nullable: true)
        asOfVersionDate(nullable: true, validator: { val, obj ->
            if (obj.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF && !val) {
                return "version.date.not.null"
            }
        })
        dmsConfiguration(nullable: true)
        productGroupSelection(nullable: true)
        sourceProfile(validator: {val, obj ->
            if(val && val.sourceId == Constants.PVSIGNAL_EMBASE_SOURCE_ID){
                return "embase.report.source.selection"
            }
        })
    }

    // this has the logic for version SQL
    def getAsOfVersionDateCustom(def isExecuted) {
        if (this.asOfVersionDate) {
            return asOfVersionDate
        } else if (evaluateDateAs == EvaluateCaseDateEnum.VERSION_PER_REPORTING_PERIOD) {
            return null
        } else if (evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION) {
            if (isExecuted) {
                return new Date()
            } else
                return RelativeDateConverter.calculateLatestVersion()
        } else {
            return null
        }
    }

    boolean isEditableBy(User currentUser) {
        return (currentUser.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == currentUser?.id || currentUser.isAdmin() || owner.id in currentUser.getUserTeamIds())))
    }

    Integer getExpectedExecutionTime() {
        if (totalExecutionTime && numOfExecutions >= 1) {
            return (totalExecutionTime).div(numOfExecutions)
        }
        return 0
    }

    def getUserService(){
        return Holders.getApplicationContext().getBean("userService")
    }

    boolean hasAuthorityToRunAsTemplateReport(User currentUser) {
        return !currentUser.getAuthorities()?.find { it.authority in [Constants.Roles.CONFIG_TMPLT_CREATOR, Constants.Roles.ADMIN, Constants.Roles.SUPER_ADMIN] }
    }

    abstract String getUsedEventSelection();
    abstract String getUsedEventGroupSelection();
    abstract List<Date> getReportMinMaxDate();
    transient abstract boolean isRunning();

    transient String getValidProductGroupSelection() {
        if (productGroupSelection && productGroupSelection != "[]") {
            return productGroupSelection
        }
        return null
    }

    transient String getUsedValidEventGroupSelection(){
        if (usedEventGroupSelection && usedEventGroupSelection != "[]") {
            return usedEventGroupSelection
        }
        return null
    }

    public String toString() {
        return "$reportName - $numOfExecutions"
    }

    transient String getNameWithDescription() {
        return "$reportName - $owner"
    }

    transient String getProductsString() {
        return productSelection ? ViewHelper.getDictionaryValues(productSelection, DictionaryTypeEnum.PRODUCT) : ""
    }

    transient String getStudiesString() {
        return studySelection ? ViewHelper.getDictionaryValues(studySelection, DictionaryTypeEnum.STUDY) : ""
    }
}

