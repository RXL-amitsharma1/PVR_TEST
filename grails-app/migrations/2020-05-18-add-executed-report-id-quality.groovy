databaseChangeLog = {
    changeSet(author: "shikhars", id: "180520202020") {
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "EXEC_REPORT_ID", type: "NUMBER(19,0)")
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "EXEC_REPORT_ID", type: "NUMBER(19,0)")
        }
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "EXEC_REPORT_ID", type: "NUMBER(19,0)")
        }
    }
}