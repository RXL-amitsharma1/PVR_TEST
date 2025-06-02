databaseChangeLog = {
    changeSet(author: "anurag", id: "202001065997-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "NEED_PAPER_REPORT")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "NEED_PAPER_REPORT", type: "NUMBER(1)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }
}