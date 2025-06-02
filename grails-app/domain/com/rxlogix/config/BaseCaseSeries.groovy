package com.rxlogix.config


import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.user.User
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.multitenancy.Tenants
import grails.plugins.orm.auditable.AuditEntityIdentifier

@DirtyCheck
abstract class BaseCaseSeries {
    //Case Series related params
    @AuditEntityIdentifier
    String seriesName
    String description
    List<Tag> tags
    String scheduleDateJSON
    Date nextRunDate
    boolean isEnabled = true
    String configSelectedTimeZone = "UTC"

    //Report related info fields.
    DateRangeType dateRangeType
    Date asOfVersionDate
    EvaluateCaseDateEnum evaluateDateAs
    boolean excludeFollowUp = false
    boolean includeLockedVersion = true
    boolean includeAllStudyDrugsCases = false
    boolean excludeNonValidCases = true
    boolean excludeDeletedCases = true
    boolean suspectProduct = false
    boolean executing = false
    boolean qualityChecked = false
    String productSelection
    String productGroupSelection
    String studySelection
    String eventSelection
    String eventGroupSelection
    User owner
    Integer numExecutions = 0
    Locale locale = new Locale('en')

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    Long tenantId

    boolean isDeleted = false
    boolean isMultiIngredient = false
    boolean includeWHODrugs = false

    static hasMany = [tags: Tag]

    static constraints = {
        seriesName(nullable: false, blank: false, maxSize: 555)
        description(nullable: true, maxSize: 4000)
        tags(nullable: true, blank: true)
        eventSelection(nullable: true)
        eventGroupSelection(nullable: true)
        lastUpdated(nullable: true)
        dateRangeType(nullable: true)
        productSelection(nullable: true)
        productGroupSelection(nullable: true)
        studySelection(nullable: true)
        evaluateDateAs(nullable: true)
        asOfVersionDate(nullable: true, validator: { val, obj ->
            if (obj.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF && !val) {
                return "version.date.not.null"
            }
        })
        nextRunDate(nullable: true)
        scheduleDateJSON(nullable: true, maxSize: 1024, validator: { val, obj ->
            if (val && !MiscUtil.validateScheduleDateJSON(val)) {
                return "com.rxlogix.config.weekly.dow.required"
            }
        })
    }

    static mapWith = "none"

    static mapping = {
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        productGroupSelection column: "PRODUCT_GROUP_SELECTION", sqlType: DbUtil.longStringType
        isDeleted column: "IS_DELETED"
        studySelection column: "STUDY_SELECTION", sqlType: DbUtil.longStringType
        eventSelection column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
        eventGroupSelection column: "EVENT_GROUP_SELECTION", sqlType: DbUtil.longStringType
        dateRangeType column: "DATE_RANGE_TYPE"
        owner column: "PVUSER_ID", cascade: 'none'
        asOfVersionDate column: "AS_OF_VERSION_DATE"
        evaluateDateAs column: "EVALUATE_DATE_AS"
        description column: "DESCRIPTION"
        numExecutions column: "NUM_EXECUTIONS"
        excludeFollowUp column: "EXCLUDE_FOLLOW_UP"
        includeLockedVersion column: "INCLUDE_LOCKED_VERSION"
        includeAllStudyDrugsCases column:"INCL_ALL_STUD_DRUG_CASES"
        excludeNonValidCases column: "EXCLUDE_NON_VALID_CASES"
        excludeDeletedCases column: "EXCLUDE_DELETED_CASES"
        suspectProduct column: "SUSPECT_PRODUCT"
        locale column:"LANG_ID"
        executing column:"EXECUTING"
        qualityChecked column: "QUALITY_CHECKED"
        tenantId column: "TENANT_ID"
        scheduleDateJSON column: "SCHEDULE_DATE"
        nextRunDate column: "NEXT_RUN_DATE"
        isEnabled column: "IS_ENABLED"
        configSelectedTimeZone column: "SELECTED_TIME_ZONE"
        isMultiIngredient column: "IS_MULTI_INGREDIENT"
        includeWHODrugs column: "INCLUDE_WHO_DRUGS"
    }

    transient String getValidProductGroupSelection() {
        if (productGroupSelection && productGroupSelection != "[]") {
            return productGroupSelection
        }
        return null
    }

    transient String getValidEventGroupSelection() {
        if (eventGroupSelection && eventGroupSelection != "[]") {
            return eventGroupSelection
        }
        return null
    }

    boolean isEditableBy(User currentUser) {
        return (currentUser.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == currentUser?.id || currentUser.isAdmin() || owner.id in currentUser.getUserTeamIds())))
    }

    @Override
    public String toString() {
        return "$seriesName - $owner"
    }

}
