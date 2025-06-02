databaseChangeLog = {
    changeSet(author: "vinay (generated)", id: "1582194885687-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ICSR_TEMPLT_QUERY', columnName: 'IS_EXPEDITED')
            }
        }
        addColumn(tableName: "ICSR_TEMPLT_QUERY") {
            column(name: "IS_EXPEDITED", type: "NUMBER(1)", defaultValue: 0){
                constraints(nullable: "false")
            }
        }
    }
}