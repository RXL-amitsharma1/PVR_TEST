databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1457021672112-1") {
        createTable(tableName: "EX_RCONFIG_REPORT_DESTS") {
            column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_DESTINATION", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1457021672112-2") {
        createTable(tableName: "RCONFIG_REPORT_DESTS") {
            column(name: "RCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_DESTINATION", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1457021672112-4") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "DUE_IN_DAYS", type: "number(10,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1457021672112-5") {
        addColumn(tableName: "RCONFIG") {
            column(name: "DUE_IN_DAYS", type: "number(10,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1457021672112-14") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "EX_RCONFIG_REPORT_DESTS", constraintName: "FK99285B7DB58176EC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1457021672112-15") {
        addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RCONFIG_REPORT_DESTS", constraintName: "FKAF0ECB71EFCEDEEF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
    }
}
