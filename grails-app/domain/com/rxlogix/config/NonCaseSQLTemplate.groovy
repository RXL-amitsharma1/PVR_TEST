package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.util.DbUtil
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class NonCaseSQLTemplate extends ReportTemplate {
    static auditable = [ignore:["factoryDefault","editable"]]
    String nonCaseSql
    String columnNamesList
    boolean usePvrDB = false
    String chartCustomOptions
    NonCaseSQLTemplate drillDownTemplate
    String drillDownFilerColumns
    String drillDownField
    String specialChartSettings
    Boolean chartExportAsImage

    static hasMany = [customSQLValues: CustomSQLValue]

    static mapping = {
        autoTimestamp false
        tablePerHierarchy false
        customSQLValues joinTable: [name:"NONCASE_SQL_TEMPLTS_SQL_VALUES", column: "SQL_VALUE_ID", key:"NONCASE_SQL_TEMPLT_ID"], cascade: 'all-delete-orphan'

        table name: "NONCASE_SQL_TEMPLT"
        nonCaseSql sqlType: DbUtil.longStringType, column: "NON_CASE_SQL"
        columnNamesList column: "COL_NAME_LIST"
        usePvrDB column: "USE_PVR_DB"
        chartCustomOptions column: "CHART_CUSTOM_OPTIONS", sqlType: DbUtil.longStringType
        specialChartSettings  column: "SPECIAL_SETTINGS"
        chartExportAsImage column: "EXPORT_AS_IMAGE"
        drillDownField column: "DRILL_DOWN_FIELD"
        drillDownTemplate column: "DRILL_DOWN_ID"
        drillDownFilerColumns column: "DRILL_DOWN_FILER"
    }

    static constraints = {
        nonCaseSql(nullable: false, blank:false, maxSize: 45000, validator: {val, obj -> //Increase the maxSize from 12294 to 45000.
            if (val && val.toLowerCase() ==~ Constants.SQL_DML_PATTERN_REGEX) {
                return "com.rxlogix.config.query.customSQLQuery.invalid"
            }
        }) //business validation within preValidateTemplate method of TemplateController

        hasBlanks(validator: { val, obj ->
            if (obj && obj.isDirty("nonCaseSql")) { // for update only
                boolean oldValue = obj.getPersistentValue("hasBlanks")
                if ((oldValue || obj?.hasBlanks) && (obj.templateService?.getUsagesCount(obj) || obj.templateService?.getUsagesCount(obj))) {
                    return "app.template.update.fail.blanks"
                }
            }
        })

        columnNamesList(nullable: false)
        chartCustomOptions(nullable: true)
        drillDownTemplate(nullable: true)
        drillDownFilerColumns(nullable: true, maxSize: 4000)
        drillDownField(nullable: true, maxSize: 4000)
        specialChartSettings(nullable: true)
        chartExportAsImage(nullable: true)
    }

    @Override
    transient List<String> getFieldsToValidate(){
        return this.getClass().getSuperclass().declaredFields
                .collectMany { !it.synthetic ? [it.name] : [] } + this.getClass().declaredFields
                .collectMany { !it.synthetic ? [it.name] : [] }
    }


    static String getSqlQueryToValidate(NonCaseSQLTemplate template){
        return "select * from (${template.nonCaseSql}) where 1=2"
    }

    @Override
    public String toString() {
        super.toString()
    }
}
