databaseChangeLog = {

    changeSet(author: "Ankita (generated)", id: "201906071604-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CUSTOM_RPT_FIELD', columnName: 'DEFAULT_EXPRESSION_NEW')
            }
        }
        addColumn(tableName: "CUSTOM_RPT_FIELD") {
            column(name: "DEFAULT_EXPRESSION_NEW", type: "VARCHAR2(32000)", defaultValue: " "){
                constraints(nullable: "false")
            }
        }
        sql("update CUSTOM_RPT_FIELD set DEFAULT_EXPRESSION_NEW=DEFAULT_EXPRESSION")

        dropColumn(columnName: "DEFAULT_EXPRESSION", tableName: "CUSTOM_RPT_FIELD")

        sql("alter table CUSTOM_RPT_FIELD rename column DEFAULT_EXPRESSION_NEW to DEFAULT_EXPRESSION")

    }

    changeSet(author: "Ankita (generated)", id: "201906071604-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_FIELD_INFO', columnName: 'CUSTOM_EXPRESSION_NEW')
            }
        }
        addColumn(tableName: "RPT_FIELD_INFO") {
            column(name: "CUSTOM_EXPRESSION_NEW", type: "VARCHAR2(32000 char)", defaultValue: " "){
                constraints(nullable: "true")
            }
        }

        sql("update RPT_FIELD_INFO set CUSTOM_EXPRESSION_NEW=CUSTOM_EXPRESSION")

        dropColumn(columnName: "CUSTOM_EXPRESSION", tableName: "RPT_FIELD_INFO")

        sql("alter table RPT_FIELD_INFO rename column CUSTOM_EXPRESSION_NEW to CUSTOM_EXPRESSION")

       }


    changeSet(author: "Ankita (generated)", id: "201905281857-3") {
        update(tableName: "LOCALIZATION", where: "CODE = 'com.rxlogix.config.CustomReportField.defaultExpression.maxSize.exceeded' and loc='*'"){
            column(name: "TEXT", value: "Expression cannot be greater than 32000 characters")
        }
    }

    changeSet(author: "Ankita (generated)", id: "201905281857-4") {
        update(tableName: "LOCALIZATION", where: "CODE = 'com.rxlogix.commandObjects.CustomReportFieldCO.defaultExpression.maxSize.exceeded' and loc='*'"){
            column(name: "TEXT", value: "Expression cannot be greater than 32000 characters")
        }
    }

    changeSet(author: "Ankita (generated)", id: "201905281857-5") {
        update(tableName: "LOCALIZATION", where: "CODE = 'com.rxlogix.config.CustomReportField.defaultExpression.maxSize.exceeded' and loc='ja'"){
            column(name: "TEXT", value: "式は32000文字を超えることはできません。")
        }
    }

    changeSet(author: "Ankita (generated)", id: "201905281857-6") {
        update(tableName: "LOCALIZATION", where: "CODE = 'com.rxlogix.commandObjects.CustomReportFieldCO.defaultExpression.maxSize.exceeded' and loc='ja'"){
            column(name: "TEXT", value: "式は32000文字を超えることはできません。")
        }
    }

}
