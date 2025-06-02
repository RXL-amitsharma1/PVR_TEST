databaseChangeLog = {
    changeSet(author: "anurag", id: "2021051415222-1") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "JOB_EXECUTION_HISTORY")
            }
        }
        createTable(tableName: "JOB_EXECUTION_HISTORY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "JOB_EXECUTION_HISTORYPK")
            }

            column(name: "JOB_TITLE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "JOB_START_RUN_DATE", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "JOB_END_RUN_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }

            column(name: "JOB_RUN_STATUS", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "JOB_RUN_REMARKS", type: "clob") {
                constraints(nullable: "true")
            }

            column(name: "CREATED_BY", type: "varchar2(20 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }
}