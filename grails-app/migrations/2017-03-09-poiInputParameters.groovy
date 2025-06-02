databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1489069892186-1") {
        createTable(tableName: "EX_RCONFIGS_POI_PARAMS") {
            column(name: "EXC_RCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PARAM_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1489069892186-2") {
        createTable(tableName: "RCONFIGS_POI_PARAMS") {
            column(name: "RCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PARAM_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1489069892186-49") {
        addForeignKeyConstraint(baseColumnNames: "EXC_RCONFIG_ID", baseTableName: "EX_RCONFIGS_POI_PARAMS", constraintName: "FK424D772FFD37330A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1489069892186-50") {
        addForeignKeyConstraint(baseColumnNames: "PARAM_ID", baseTableName: "EX_RCONFIGS_POI_PARAMS", constraintName: "FK424D772F8393D6C1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1489069892186-52") {
        addForeignKeyConstraint(baseColumnNames: "PARAM_ID", baseTableName: "RCONFIGS_POI_PARAMS", constraintName: "FKB69F38BB8393D6C1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1489069892186-53") {
        addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RCONFIGS_POI_PARAMS", constraintName: "FKB69F38BB1F4CA38A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
    }

}
