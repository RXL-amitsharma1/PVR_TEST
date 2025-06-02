databaseChangeLog = {
    changeSet(id: "202502251244-1", author: "rxl-shivamg1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'CONSIDER_ONLY_POI')
            }
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'STUDY_MEDICATION_TYPE')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "CONSIDER_ONLY_POI", type: "NUMBER(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
            column(name: "STUDY_MEDICATION_TYPE", type: "NUMBER(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }
}
