package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.util.DbUtil
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class CustomSQLTemplate extends ReportTemplate {
    static auditable = [ignore:["factoryDefault","editable"]]

    String customSQLTemplateSelectFrom
    String customSQLTemplateWhere
    String columnNamesList
    CustomSQLTemplate drillDownTemplate
    String drillDownFilerColumns
    String drillDownField
    static hasMany = [customSQLValues: CustomSQLValue]

    static mapping = {
        autoTimestamp false
        tablePerHierarchy false

        table name: "SQL_TEMPLT"
        customSQLValues joinTable: [name:"SQL_TEMPLTS_SQL_VALUES", column: "SQL_VALUE_ID", key:"SQL_TEMPLT_ID",], cascade: 'all-delete-orphan'

        customSQLTemplateSelectFrom column: "SELECT_FROM_STMT", sqlType: DbUtil.longStringType
        customSQLTemplateWhere column: "WHERE_STMT", sqlType: DbUtil.longStringType
        columnNamesList column: "COLUMN_NAMES", sqlType: DbUtil.stringType

        drillDownField column: "DRILL_DOWN_FIELD"
        drillDownTemplate column: "DRILL_DOWN_ID"
        drillDownFilerColumns column: "DRILL_DOWN_FILER"
    }

    static constraints = {
        customSQLTemplateSelectFrom(nullable: false, blank: false, maxSize: 45000, validator: { val, obj ->  //business validation within preValidateTemplate method of TemplateController
            if (val && val.toLowerCase() ==~ Constants.SQL_DML_PATTERN_REGEX) {
                return "com.rxlogix.config.query.customSQLQuery.invalid"
            }
        })

        customSQLTemplateWhere(nullable: true, maxSize: 45000, validator: {val, obj ->
            if (val && val.toLowerCase() ==~ Constants.SQL_DML_SELECT_PATTERN_REGEX) {
                return "com.rxlogix.config.query.customSQLQuery.invalid"
            }
        })

        hasBlanks(validator: { val, obj ->
            if (obj && (obj.isDirty("customSQLTemplateSelectFrom") || obj.isDirty("customSQLTemplateWhere"))) {
                // for update only
                boolean oldValue = obj.getPersistentValue("hasBlanks")
                if ((oldValue || val) && obj.templateService && !obj.templateService.isTemplateUpdateable(obj)) {
                    return "app.template.update.fail.blanks"
                }
            }
        })

        columnNamesList(nullable: false, maxSize: 16384)
        drillDownTemplate(nullable: true)
        drillDownFilerColumns(nullable: true,maxSize: 4000)
        drillDownField(nullable: true,maxSize: 4000)
    }

    @Override
    transient List<String> getFieldsToValidate(){
        return this.getClass().getSuperclass().declaredFields
                .collectMany { !it.synthetic ? [it.name] : [] } + this.getClass().declaredFields
                .collectMany { !it.synthetic ? [it.name] : [] }
    }

    static String getSqlQueryToValidate(CustomSQLTemplate template){
        String query = "${template?.customSQLTemplateSelectFrom} where 1=2"
        if (template?.customSQLTemplateWhere) {
            query += " ${template?.customSQLTemplateWhere?.trim()}"
        }
        return query
    }

    @Override
    public String toString() {
        super.toString()
    }

}
