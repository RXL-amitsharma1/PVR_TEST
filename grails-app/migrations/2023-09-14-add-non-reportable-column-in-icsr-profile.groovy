databaseChangeLog = {

    changeSet(author: "ShubhamRx (generated)", id: "202309141625-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'INCLUDE_NON_REPORTABLE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "INCLUDE_NON_REPORTABLE", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "202309141638-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'INCLUDE_NON_REPORTABLE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "INCLUDE_NON_REPORTABLE", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }
}