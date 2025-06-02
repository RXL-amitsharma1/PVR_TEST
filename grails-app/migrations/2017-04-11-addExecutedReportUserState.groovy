databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1491923170339-1") {
        createTable(tableName: "EX_RCONFIG_USER_STATE") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_RCONFIG_USPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ARCHIVED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_USER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "1491923170339-60") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "EX_RCONFIG_USER_STATE", constraintName: "FK57208114CE8FBC87", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1491923170339-61") {
        addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "EX_RCONFIG_USER_STATE", constraintName: "FK5720811473121E06", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1492506715656-1") {
        createTable(tableName: "EX_CASE_SERIES_USER_STATE") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CASE_SERUSPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_CASE_SERIES_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ARCHIVED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_USER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "1492506715656-60") {
        addForeignKeyConstraint(baseColumnNames: "EX_CASE_SERIES_ID", baseTableName: "EX_CASE_SERIES_USER_STATE", constraintName: "FK9FDD7F822805D4B4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_CASE_SERIES", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1492506715656-61") {
        addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "EX_CASE_SERIES_USER_STATE", constraintName: "FK9FDD7F8273121E06", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

}

