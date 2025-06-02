databaseChangeLog = {
    changeSet(author: "sachinverma (generated)", id: "2019101822440-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RPT_RESULT_DATA", columnName: "IS_ENCRYPTED")
            }
        }
        addColumn(tableName: "RPT_RESULT_DATA") {
            column(name: "IS_ENCRYPTED", type: "number(1,0)")
        }
        sql("UPDATE RPT_RESULT_DATA SET IS_ENCRYPTED=0");
        addNotNullConstraint(tableName: "RPT_RESULT_DATA", columnName: "IS_ENCRYPTED", columnDataType: "number(1,0)")
    }
}