
databaseChangeLog = {

    changeSet(author: "sargam", id: "1523257222357-1") {
        createTable(tableName: "REPORT_EXECUTION_KILL_REQUEST") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ReportExecutionKillRequestPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            
            column(name: "EXECUTION_STATUS_ID", type: "number(19,0)")

            column(name: "KILL_STATUS", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }
}