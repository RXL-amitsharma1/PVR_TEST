databaseChangeLog = {

    changeSet(author: "Sherry (generated)", id: "1446338246367-1") {
        createTable(tableName: "EX_TEMPLT_SET") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_TEMPLT_SETPK")
            }
        }
    }

    changeSet(author: "Sherry (generated)", id: "1446338246367-2") {
        createTable(tableName: "EX_TEMPLT_SET_CLL") {
            column(name: "EX_TEMPLT_SET_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_CLL_TEMPLT_ID", type: "number(19,0)")

            column(name: "EX_CLL_TEMPLT_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "Sherry (generated)", id: "1446338246367-3") {
        createTable(tableName: "TEMPLT_SET") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TEMPLT_SETPK")
            }

            column(name: "EXCLUDE_EMPTY_SECTIONS", type: "number(1,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Sherry (generated)", id: "1446338246367-4") {
        createTable(tableName: "TEMPLT_SET_CLL") {
            column(name: "TEMPLT_SET_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CLL_TEMPLT_ID", type: "number(19,0)")

            column(name: "CLL_TEMPLT_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "Sherry (generated)", id: "1446338246367-9") {
        addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_TEMPLT_SET", constraintName: "FK9AF470EB9C3FCD05", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TEMPLT_SET", referencesUniqueColumn: "false")
    }

    changeSet(author: "Sherry (generated)", id: "1446338246367-10") {
        addForeignKeyConstraint(baseColumnNames: "EX_CLL_TEMPLT_ID", baseTableName: "EX_TEMPLT_SET_CLL", constraintName: "FK385837AF3997143", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_CLL_TEMPLT", referencesUniqueColumn: "false")
    }

    changeSet(author: "Sherry (generated)", id: "1446338246367-11") {
        addForeignKeyConstraint(baseColumnNames: "ID", baseTableName: "TEMPLT_SET", constraintName: "FK6BB481DF88B11947", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
    }

    changeSet(author: "Sherry (generated)", id: "1446338246367-12") {
        addForeignKeyConstraint(baseColumnNames: "CLL_TEMPLT_ID", baseTableName: "TEMPLT_SET_CLL", constraintName: "FK97FEAEA346D1FA5E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CLL_TEMPLT", referencesUniqueColumn: "false")
    }
}
