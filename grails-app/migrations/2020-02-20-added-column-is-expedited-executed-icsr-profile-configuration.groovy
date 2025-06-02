databaseChangeLog = {
    changeSet(author: "vinay", id: "1582194937352-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_ICSR_TEMPLT_QUERY", columnName: "IS_EXPEDITED")
            }
        }
        addColumn(tableName: "EX_ICSR_TEMPLT_QUERY") {
            column(name: "IS_EXPEDITED", type: "NUMBER(1)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }
}