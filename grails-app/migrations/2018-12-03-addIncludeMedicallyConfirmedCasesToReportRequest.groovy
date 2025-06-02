databaseChangeLog = {
    changeSet(author: "sargam (generated)", id: "1542792854519-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'INCL_MEDICAL_CONFIRM_CASES')
            }
        }
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "NUMBER(1)", defaultValue: 0){
                constraints(nullable: "false")
            }
        }
    }
}