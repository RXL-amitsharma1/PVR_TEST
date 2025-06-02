databaseChangeLog = {

    changeSet(author: "sargamsachdeva (generated)", id: "1538574964531-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'INCL_NON_SIGNIFICANT_FOLLOWUP')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "INCL_NON_SIGNIFICANT_FOLLOWUP", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE RCONFIG SET INCL_NON_SIGNIFICANT_FOLLOWUP = 0;")
        addNotNullConstraint(tableName: "RCONFIG", columnName: "INCL_NON_SIGNIFICANT_FOLLOWUP")
    }

    changeSet(author: "sargamsachdeva (generated)", id: "1538574964531-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'INCL_NON_SIGNIFICANT_FOLLOWUP')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "INCL_NON_SIGNIFICANT_FOLLOWUP", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_RCONFIG set INCL_NON_SIGNIFICANT_FOLLOWUP = 0;")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "INCL_NON_SIGNIFICANT_FOLLOWUP")
    }
}