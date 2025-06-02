databaseChangeLog = {

    changeSet(author: "RishabhJ", id: "202110111310-1") {
        createTable(tableName: "ACTION_ITEM_EMAIL_PREF") {

            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTION_ITEM_EMAIL_PREFPK")
            }

            column(name: "PREFERENCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATE_EMAILS", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "UPDATE_EMAILS", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "JOB_EMAILS", type: "number(1,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "RishabhJ", id: "202110111310-2") {
        createTable(tableName: "REPORT_REQUEST_EMAIL_PREF") {

            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "REPORT_REQUEST_EMAIL_PREFPK")
            }

            column(name: "PREFERENCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATE_EMAILS", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "UPDATE_EMAILS", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "DELETE_EMAILS", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "WORKFLOW_UPDATE_EMAILS", type: "number(1,0)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "RishabhJ", id: "202110111310-6") {
        addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "ACTION_ITEM_EMAIL_PREF", constraintName: "FK_ACTION_ITEM_PREF_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PREFERENCE", referencesUniqueColumn: "false")
    }

    changeSet(author: "RishabhJ", id: "202110111310-7") {
        addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "REPORT_REQUEST_EMAIL_PREF", constraintName: "FK_REPORT_REQ_PREF_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PREFERENCE", referencesUniqueColumn: "false")
    }

    changeSet(author: "RishabhJ", id: "202110111310-5") {
        sql("INSERT into ACTION_ITEM_EMAIL_PREF (ID, PREFERENCE_ID, CREATE_EMAILS, UPDATE_EMAILS, JOB_EMAILS) select HIBERNATE_SEQUENCE.nextval, ID, 1, 1, 1 from PREFERENCE")
        sql("INSERT into REPORT_REQUEST_EMAIL_PREF (ID, PREFERENCE_ID, CREATE_EMAILS, UPDATE_EMAILS, DELETE_EMAILS, WORKFLOW_UPDATE_EMAILS) select HIBERNATE_SEQUENCE.nextval, ID, 1, 1, 1, 1 from PREFERENCE")
    }

}