package com.rxlogix.user

import com.rxlogix.Constants
import com.rxlogix.config.ReportField

import com.rxlogix.hibernate.EscapedILikeExpression
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.web.context.request.RequestContextHolder

@CollectionSnapshotAudit
class FieldProfile {
    static auditable = true
    @AuditEntityIdentifier
    String name
    String description
    boolean isDeleted = false

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        table name: "FIELD_PROFILE"
        name column: "NAME"
        description column: "DESCRIPTION"
        isDeleted column: "IS_DELETED"
    }


    static constraints = {
        name(nullable: false, maxSize: 30, unique: true)
        description(nullable: true, maxSize: 1000)

        createdBy(nullable: false, maxSize: 255)
        modifiedBy(nullable: false, maxSize: 255)
    }


    static namedQueries = {
        fetchAllFieldProfileBySearchString { String search ->
            if (search) {
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            eq('isDeleted', false)
        }
    }

    String toString() {
        "[FieldProfile = " +
                " id->${id}" +
                " name->${name}]";
    }

    Map<String, Map> appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        def params = RequestContextHolder?.requestAttributes?.params ?: [:]
        if (newValues && (oldValues == null)) {
            newValues.put("blindedFields", newFields(params, "blindedReportFields")?.join(", ") ?: "")
            newValues.put("protectedFields", newFields(params, "protectedReportFields")?.join(", ") ?: "")
            newValues.put("hiddenFields", newFields(params, "hiddenReportFields")?.join(", ") ?: "")
        }
        if (newValues && oldValues) {
            //Blinded Fields
            List<String> newBlindedFields = newFields(params, "blindedReportFields")
            List<String> oldBlindedFields = oldFields(this, "isBlinded")
            if (((newBlindedFields - oldBlindedFields) + (oldBlindedFields - newBlindedFields)).size() > 0) {
                newValues.put("blindedFields", newBlindedFields?.join(", ") ?: "")
                oldValues.put("blindedFields", oldBlindedFields?.join(", ") ?: "")
            }

            //Redacted Fields
            List<String> newProtectedFields = newFields(params, "protectedReportFields")
            List<String> oldProtectedFields = oldFields(this, "isProtected")
            if (((newProtectedFields - oldProtectedFields) + (oldProtectedFields - newProtectedFields)).size() > 0) {
                newValues.put("protectedFields", newProtectedFields?.join(", ") ?: "")
                oldValues.put("protectedFields", oldProtectedFields?.join(", ") ?: "")
            }

            //Hidden Fields
            List<String> newHiddenFields = newFields(params, "hiddenReportFields")
            List<String> oldHiddenFields = oldFields(this, "isHidden")
            if (((newHiddenFields - oldHiddenFields) + (oldHiddenFields - newHiddenFields)).size() > 0) {
                newValues.put("hiddenFields", newHiddenFields?.join(", ") ?: "")
                oldValues.put("hiddenFields", oldHiddenFields?.join(", ") ?: "")
            }
        }

        return [newValues: newValues, oldValues: oldValues]
    }

    private List<String> newFields(Map params, String paramName) {
        List<String> fields = []
        FieldProfile.withNewSession {
            List<Long> fieldIds = params[paramName]?.split(",").findAll { it?.isLong() }?.collect { it as Long }
            if (fieldIds) {
                List<ReportField> reportFields = []
                fieldIds.collate(Constants.MAX_LIST_SIZE_DB).each {
                    reportFields += ReportField.findAllByIdInList(it)
                }
                fields = reportFields.collect { it.displayName ?: it.name }
            }
        }
        return fields
    }


    private List<String> oldFields(FieldProfile fieldProfile, String flagType) {
        FieldProfileFields.withNewSession {
            return FieldProfileFields.findAllWhere([fieldProfile: fieldProfile, (flagType): true])?.collect { it.reportField.displayName ?: it.reportField.name }
        }
    }

    static List<Map> fetchReportFields(FieldProfile fieldProfile, String groupName) {
        List<Map> allFields = FieldProfileFields.createCriteria().list {
            eq("fieldProfile", fieldProfile)
            createAlias("reportField", "rf")
            createAlias("rf.fieldGroup", "fg", CriteriaSpecification.LEFT_JOIN)
            eq("fg.name", groupName)
            projections {
                property("rf.id", "id")
                property("rf.name", "name")
                property("isBlinded", "isBlinded")
                property("isProtected", "isProtected")
                property("isHidden", "isHidden")
            }
            order("rf.name", "asc")
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
        }

        return allFields
    }

    static List<String> fetchReportFieldGroups(FieldProfile fieldProfile) {
        List<String> allGroups = FieldProfileFields.createCriteria().list {
            eq("fieldProfile", fieldProfile)
            createAlias("reportField", "rf")
            createAlias("rf.fieldGroup", "fg", CriteriaSpecification.LEFT_JOIN)
            projections {
                distinct("fg.name")
            }
            order("fg.name", "asc")
        }

        return allGroups
    }
}
