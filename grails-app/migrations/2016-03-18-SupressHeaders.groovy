databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1458278228345-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DTAB_TEMPLT', columnName: 'SUPRESS_HEADERS')
            }
        }
        addColumn(tableName: "DTAB_TEMPLT") {
            column(name: "SUPRESS_HEADERS", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update DTAB_TEMPLT set SUPRESS_HEADERS = 0;")
        addNotNullConstraint(tableName: "DTAB_TEMPLT", columnName: "SUPRESS_HEADERS")
    }

    changeSet(author: "anurag (generated)", id: "3006202028345-2") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "ROWS_RF_INFO_LIST_ID", tableName: "DTAB_TEMPLT")
    }
}
