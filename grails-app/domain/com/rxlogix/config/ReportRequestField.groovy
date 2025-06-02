package com.rxlogix.config

import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class ReportRequestField {
    static auditable =  true
    @AuditEntityIdentifier
    String name
    String label
    String secondaryLabel
    Integer index
    Integer width=3
    Section section
    Type fieldType
    String allowedValues
    String secondaryAllowedValues
    String jscript
    Boolean masterPlanningRequest
    Boolean disabled
    Boolean showInPlan
    ReportRequestType reportRequestType

    boolean isDeleted = false

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "Application"
    String modifiedBy = "Application"

    static constraints = {
        index nullable: true, min: 0, max: 999999999
        allowedValues nullable: true, maxSize: 4000, validator: { val, obj ->
            if (obj.fieldType in [Type.SELECT, Type.LIST, Type.CASCADE] && !val) {
                return "com.rxlogix.config.ReportRequestField.allowedValues.blank"
            }
        }
        section nullable: true
        width nullable: true, min: 1, max: 12
        secondaryAllowedValues nullable: true
        secondaryLabel nullable: true
        label nullable: false, maxSize: 255
        disabled nullable: true
        jscript nullable: true, maxSize: 4000
        showInPlan nullable: true
        reportRequestType nullable: true
    }

    static mapping = {
        table name: "REPORT_REQUEST_FIELD"
        name column: "NAME"
        label column: "LABEL"
        index column: "INDX"
        isDeleted column: "IS_DELETED"
        fieldType column: "TYPE"
        allowedValues column: "ALLOWED_VALUES"
        masterPlanningRequest column: "MASTER_PLANNING"
        section column: "SECTION"
        width column: "WIDTH"
        jscript column: "JSCRIPT"
        disabled column: "DISABLED"
        secondaryAllowedValues column: "SEC_ALLOWED_VALUES"
        secondaryLabel column: "SEC_LABEL"
        showInPlan column: "SHOW_IN_PLAN"
        reportRequestType column: "RR_TYPE_ID"
    }

    Map toMap() {
        [
                id         : id,
                name       : name,
                label      : label,
                index      : index,
                type       : ViewHelper.getMessage(fieldType.i18nKey),
                section    : ViewHelper.getMessage((section ?: Section.ADDITIONAL).i18nKey),
                lastUpdated: lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                modifiedBy : modifiedBy
        ]
    }

    static enum Type {
        SELECT,
        LIST,
        DATE,
        STRING,
        TEXTAREA,
        LONG,
        BOOLEAN,
        CASCADE

        public getI18nKey() {
            return "app.ReportRequestField.Type.${this.name()}"
        }

        static getI18List() {
            return values().collect {
                [name: it.name(), display: ViewHelper.getMessage(it.getI18nKey())]
            }
        }
    }

    static enum Section {
        BASE,
        SELECTION,
        SCHEDULE,
        ADDITIONAL,
        COMMENTS

        public getI18nKey() {
            return "app.ReportRequestField.section.${this.name()}"
        }

        static getI18List() {
            return values().collect {
                [name: it.name(), display: ViewHelper.getMessage(it.getI18nKey())]
            }
        }
    }
}
