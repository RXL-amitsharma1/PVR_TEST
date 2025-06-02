package com.rxlogix.config

import com.rxlogix.user.User
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.multitenancy.Tenants
import grails.plugins.orm.auditable.AuditEntityIdentifier

@DirtyCheck
abstract class BaseInboundCompliance implements Serializable{
    @AuditEntityIdentifier
    String senderName
    String description
    List<Tag> tags
    boolean isDisabled = false

    //Report related info fields.
    DateRangeType dateRangeType
    boolean excludeNonValidCases = true
    boolean excludeDeletedCases = true
    boolean suspectProduct = false
    boolean executing = false
    boolean qualityChecked = false
    String productSelection
    String productGroupSelection
    String studySelection
    User owner
    String blankValuesJSON

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    Set<ParameterValue> poiInputsParameterValues
    SourceProfile sourceProfile
    Long tenantId

    boolean isDeleted=false
    boolean isMultiIngredient = false
    boolean includeWHODrugs = false
    int numOfICExecutions = 0

    static hasMany = [tags: Tag, poiInputsParameterValues: ParameterValue]

    static constraints = {
        senderName(nullable: false, blank: false, maxSize: 555)
        description(nullable: true, maxSize: 4000)
        tags(nullable: true, blank: true)
        lastUpdated(nullable: true)
        dateRangeType(nullable: true)
        productSelection(nullable: true)
        productGroupSelection(nullable: true)
        studySelection(nullable: true)
        blankValuesJSON(nullable: true, maxSize: 8192)
        numOfICExecutions(nullable: false)
    }

    static mapWith = "none"

    static mapping = {
        senderName column: "SENDER_NAME"
        description column: "DESCRIPTION"
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        productGroupSelection column: "PRODUCT_GROUP_SELECTION", sqlType: DbUtil.longStringType
        isDeleted column: "IS_DELETED"
        studySelection column: "STUDY_SELECTION", sqlType: DbUtil.longStringType
        dateRangeType column: "DATE_RANGE_TYPE"
        owner column: "PVUSER_ID", cascade: 'none'
        excludeNonValidCases column: "EXCLUDE_NON_VALID_CASES"
        excludeDeletedCases column: "EXCLUDE_DELETED_CASES"
        suspectProduct column: "SUSPECT_PRODUCT"
        executing column:"EXECUTING"
        qualityChecked column: "QUALITY_CHECKED"
        sourceProfile column: "SOURCE_PROFILE"
        tenantId column: "TENANT_ID"
        isDisabled column: "IS_DISABLED"
        blankValuesJSON column: "BLANK_VALUES", sqlType: DbUtil.longStringType
        isMultiIngredient column: "IS_MULTI_INGREDIENT"
        numOfICExecutions column: "NUM_OF_IC_EXECUTIONS"
        includeWHODrugs column: "INCLUDE_WHO_DRUGS"
    }

    boolean isEditableBy(User currentUser) {
        return (currentUser.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == currentUser?.id || currentUser.isAdmin() || owner.id in currentUser.getUserTeamIds())))
    }

    transient String getValidProductGroupSelection() {
        if (productGroupSelection && productGroupSelection != "[]") {
            return productGroupSelection
        }
        return null
    }

    @Override
    public String toString() {
        return "$senderName"
    }
}
