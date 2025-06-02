package com.rxlogix.user

import com.rxlogix.config.ReportField
import grails.plugins.orm.auditable.SectionModuleAudit
import org.apache.commons.lang.builder.HashCodeBuilder

@SectionModuleAudit(parentClassName = ["fieldProfile"])
class FieldProfileFields implements Serializable {
    private static final long serialVersionUID = 1

    FieldProfile fieldProfile
    ReportField reportField
    boolean isBlinded = false
    boolean isProtected = false
    boolean isHidden = false

    static belongsTo = [fieldProfile: FieldProfile]

    static mapping = {
        id composite: ['fieldProfile', 'reportField']
        version false
        table name: "FIELD_PROFILE_FIELDS"
        fieldProfile column: "FIELD_PROFILE_ID"
        reportField column: "REPORT_FIELD_ID"
        isBlinded column: "IS_BLINDED"
        isProtected column: "IS_PROTECTED"
        isHidden column: "IS_HIDDEN"
    }

    static constraints = {
        fieldProfile nullable: false
        reportField nullable: false
        isBlinded nullable: false
        isProtected nullable: false
        isHidden nullable: false
    }

    @Override
    boolean equals(other) {
        if (other == null || other.getClass() != FieldProfileFields) {
            return false
        }

        other.fieldProfile?.id == fieldProfile?.id && other.reportField?.id == reportField?.id
    }

    @Override
    int hashCode() {
        def builder = new HashCodeBuilder()
        if (fieldProfile) {
            builder.append(fieldProfile.id)
        }
        if (reportField) {
            builder.append(reportField.id)
        }
        builder.toHashCode()
    }

    String toString() {
        return fieldProfile.name
    }
}