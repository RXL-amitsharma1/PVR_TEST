databaseChangeLog = {
    changeSet(author: "anurag", id: "20210831204600-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUTO_REASON_OF_DELAY', columnName: 'EVALUATE_DATE_AS')
            }
        }
        addColumn(tableName: "AUTO_REASON_OF_DELAY") {
            column(name: "EVALUATE_DATE_AS", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
}