databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "221220200121-1") {
        createTable(tableName: "AUTO_ASSIGNMENT") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "AUTO_ASSIGNMENT_ID")
            }

            column(name: "CASE_NUMBER", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "CASE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "PROCESSED_REPORT_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "TENANT_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "MODULE_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_RULE_ID", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

        }
    }
}

