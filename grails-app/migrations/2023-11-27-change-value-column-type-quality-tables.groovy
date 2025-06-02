databaseChangeLog = {
    changeSet(author: "riya", id: "202311281645-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'VALUE_NEW')
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "VALUE_NEW", type: "CLOB", defaultValue: " "){
                constraints(nullable: "true")
            }
        }

        sql("update QUALITY_CASE_DATA set VALUE_NEW=TO_CLOB(VALUE)")

        dropColumn(columnName: "VALUE", tableName: "QUALITY_CASE_DATA")

        sql("alter table QUALITY_CASE_DATA rename column VALUE_NEW to VALUE")
    }

    changeSet(author: "riya", id: "202311281645-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'VALUE_NEW')
            }
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "VALUE_NEW", type: "CLOB", defaultValue: " "){
                constraints(nullable: "true")
            }
        }
        sql("update QUALITY_SUBMISSION set VALUE_NEW=TO_CLOB(VALUE)")

        dropColumn(columnName: "VALUE", tableName: "QUALITY_SUBMISSION")

        sql("alter table QUALITY_SUBMISSION rename column VALUE_NEW to VALUE")
    }

    changeSet(author: "riya", id: "202311281645-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'VALUE_NEW')
            }
        }
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "VALUE_NEW", type: "CLOB", defaultValue: " "){
                constraints(nullable: "true")
            }
        }
        sql("update QUALITY_SAMPLING set VALUE_NEW=TO_CLOB(VALUE)")

        dropColumn(columnName: "VALUE", tableName: "QUALITY_SAMPLING")

        sql("alter table QUALITY_SAMPLING rename column VALUE_NEW to VALUE")
    }
}
