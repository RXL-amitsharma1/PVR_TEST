databaseChangeLog = {

    changeSet(author: "Shubham (generated)", id: "2960211999021-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CUSTOM_RPT_FIELD', columnName: 'DEFAULT_EXPRESSION_NEW')
            }
        }
        addColumn(tableName: "CUSTOM_RPT_FIELD") {
            column(name: "DEFAULT_EXPRESSION_NEW", type: "VARCHAR2(4000)", defaultValue: " "){
                constraints(nullable: "false")
            }
        }
        sql("update CUSTOM_RPT_FIELD set DEFAULT_EXPRESSION_NEW=dbms_lob.substr(DEFAULT_EXPRESSION,4000,1)")

        dropColumn(columnName: "DEFAULT_EXPRESSION", tableName: "CUSTOM_RPT_FIELD")

        sql("alter table CUSTOM_RPT_FIELD rename column DEFAULT_EXPRESSION_NEW to DEFAULT_EXPRESSION")


    }

    changeSet(author: "Shubham (generated)", id: "2960211999021-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_FIELD_INFO', columnName: 'CUSTOM_EXPRESSION_NEW')
            }
        }
        addColumn(tableName: "RPT_FIELD_INFO") {
            column(name: "CUSTOM_EXPRESSION_NEW", type: "VARCHAR2(4000)", defaultValue: " "){
                constraints(nullable: "true")
            }
        }

        sql("update RPT_FIELD_INFO set CUSTOM_EXPRESSION_NEW=dbms_lob.substr(CUSTOM_EXPRESSION,4000,1)")

        dropColumn(columnName: "CUSTOM_EXPRESSION", tableName: "RPT_FIELD_INFO")

        sql("alter table RPT_FIELD_INFO rename column CUSTOM_EXPRESSION_NEW to CUSTOM_EXPRESSION")
    }

    changeSet(author: "Shubham (generated)", id: "2960211999021-9") {
        update(tableName: "LOCALIZATION", whereClause: "CODE = 'com.rxlogix.config.CustomReportField.defaultExpression.maxSize.exceeded' and loc='*'"){
            column(name: "TEXT", value: "Expression cannot be greater than 4000 characters")
        }

        update(tableName: "LOCALIZATION", whereClause: "CODE = 'com.rxlogix.commandObjects.CustomReportFieldCO.defaultExpression.maxSize.exceeded' and loc='*'"){
            column(name: "TEXT", value: "Expression cannot be greater than 4000 characters")
        }

        update(tableName: "LOCALIZATION", whereClause: "CODE = 'com.rxlogix.config.CustomReportField.defaultExpression.maxSize.exceeded' and loc='ja'"){
            column(name: "TEXT", value: "式は4000文字を超えることはできません。")
        }

        update(tableName: "LOCALIZATION", whereClause: "CODE = 'com.rxlogix.commandObjects.CustomReportFieldCO.defaultExpression.maxSize.exceeded' and loc='ja'"){
            column(name: "TEXT", value: "式は4000文字を超えることはできません。")
        }
    }
}