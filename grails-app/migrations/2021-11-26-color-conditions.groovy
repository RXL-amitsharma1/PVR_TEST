databaseChangeLog = {

    changeSet(author: "sergey (generated)", id: "202111261100-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "DTAB_MEASURE", columnName: "COLOR_CONDITIONS")
            }
        }
        addColumn(tableName: "DTAB_MEASURE") {
            column(name: "COLOR_CONDITIONS", type: "clob")
        }
    }

    changeSet(author: "sergey (generated)", id: "202111261100-2") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RPT_FIELD_INFO", columnName: "COLOR_CONDITIONS")
            }
        }
        addColumn(tableName: "RPT_FIELD_INFO") {
            column(name: "COLOR_CONDITIONS", type: "clob")
        }
    }
}