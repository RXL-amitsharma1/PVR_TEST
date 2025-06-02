databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1523359836678-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'SOURCE_PROFILE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SOURCE_PROFILE", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_RCONFIG set SOURCE_PROFILE = (select id from SOURCE_PROFILE where IS_CENTRAL = 1);")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "SOURCE_PROFILE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1523359836678-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'SOURCE_PROFILE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "SOURCE_PROFILE", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update RCONFIG set SOURCE_PROFILE = (select id from SOURCE_PROFILE where IS_CENTRAL = 1);")
        addNotNullConstraint(tableName: "RCONFIG", columnName: "SOURCE_PROFILE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1523359836678-3") {
        addForeignKeyConstraint(baseColumnNames: "SOURCE_PROFILE", baseTableName: "EX_RCONFIG", constraintName: "FK_d2tbjvggeqjb891vbevlmrkxg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SOURCE_PROFILE", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1523359836678-4") {
        addForeignKeyConstraint(baseColumnNames: "SOURCE_PROFILE", baseTableName: "RCONFIG", constraintName: "FK_qi1sr4k95ehbmipmyl5h0ku1g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SOURCE_PROFILE", referencesUniqueColumn: "false")
    }

}
