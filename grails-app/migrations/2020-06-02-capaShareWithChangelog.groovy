databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "2020060266665-1") {
        createTable(tableName: "CAPA8D_SHARED_WITHS") {
            column(name: "CAPA_8D_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "number(19,0)")

            column(name: "SHARED_WITH_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "anurag (generated)", id: "2020060266665-2") {
        createTable(tableName: "CAPA8D_SHARED_W_GRPS") {
            column(name: "CAPA_8D_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_GROUP_ID", type: "number(19,0)")

            column(name: "SHARED_WITH_GROUP_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "anurag (generated)", id: "2020060266665-3") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "CAPA8D_SHARED_WITHS", constraintName: "FKDDB957714F5A069B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag (generated)", id: "2020060266665-4") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_GROUP_ID", baseTableName: "CAPA8D_SHARED_W_GRPS", constraintName: "FKDAA1D8725D0E08F9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

}
