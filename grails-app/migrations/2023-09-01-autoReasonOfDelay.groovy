databaseChangeLog = {


    changeSet(author: "sergey", id: "202309011817-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUERY_RCA', columnName: 'SUMMARY')
            }
        }
        addColumn(tableName: "QUERY_RCA") {
            column(name: "SUMMARY", type: "VARCHAR2(32000)")
            column(name: "ACTIONS", type: "VARCHAR2(32000)")
            column(name: "INVESTIGATION", type: "VARCHAR2(32000)")
            column(name: "SUMMARY_SQL", type: "VARCHAR2(32000)")
            column(name: "ACTIONS_SQL", type: "VARCHAR2(32000)")
            column(name: "INVESTIGATION_SQL", type: "VARCHAR2(32000)")
        }
    }
}