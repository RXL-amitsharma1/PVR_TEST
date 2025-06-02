databaseChangeLog = {

    changeSet(author: "gunjan", id: "202412171134-1") {
        createTable(tableName: "PVC_EMAIL_PREF") {

            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PVC_EMAIL_PREFPK")
            }

            column(name: "PREFERENCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ASSIGNED_TO_ME", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "ASSIGNED_TO_MY_GROUP", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_STATE_CHANGES", type: "number(1,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "gunjan", id: "202412171134-2") {
        createTable(tableName: "PVQ_EMAIL_PREF") {

            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PVQ_EMAIL_PREFPK")
            }

            column(name: "PREFERENCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ASSIGNED_TO_ME", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "ASSIGNED_TO_MY_GROUP", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_STATE_CHANGES", type: "number(1,0)") {
                constraints(nullable: "false")
            }
        }
    }


    changeSet(author: "gunjan", id: "202412171134-3") {
        addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "PVC_EMAIL_PREF", constraintName: "FK_PVC_EMAIL_PREF_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PREFERENCE", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "PVQ_EMAIL_PREF", constraintName: "FK_PVQ_EMAIL_PREF_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PREFERENCE", referencesUniqueColumn: "false")

    }


    changeSet(author: "gunjan", id: "202412171134-4") {
        sql("INSERT into PVC_EMAIL_PREF (ID, PREFERENCE_ID, ASSIGNED_TO_ME, ASSIGNED_TO_MY_GROUP, WORKFLOW_STATE_CHANGES) select HIBERNATE_SEQUENCE.nextval, ID, 1, 1, 1 from PREFERENCE")
        sql("INSERT into PVQ_EMAIL_PREF (ID, PREFERENCE_ID, ASSIGNED_TO_ME, ASSIGNED_TO_MY_GROUP, WORKFLOW_STATE_CHANGES) select HIBERNATE_SEQUENCE.nextval, ID, 1, 1, 1 from PREFERENCE")

    }

    changeSet(author: "gunjan", id: "202412171134-5") {
        createTable(tableName: "JOB_RUN_TRACKER_PVC_PVQ") {

            column(name: "JOB_NAME", type: "varchar2(100)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "JOB_RUN_TRACKER_PK")
            }

            column(name: "LAST_RUN_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
        }

    }

    changeSet(author: "gunjan", id: "202412171135-6") {
        sql("INSERT INTO JOB_RUN_TRACKER_PVC_PVQ (JOB_NAME, LAST_RUN_DATE, DATE_CREATED, LAST_UPDATED) VALUES ('PVCEmailNotificationJob', SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP) ")
        sql("INSERT INTO JOB_RUN_TRACKER_PVC_PVQ (JOB_NAME, LAST_RUN_DATE, DATE_CREATED, LAST_UPDATED) VALUES ('PVQEmailNotificationJob', SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP) ")
    }


}