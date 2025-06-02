databaseChangeLog = {

    changeSet(author: "sergey khovrachev (generated)", id: "1555662378671-1") {
        createTable(tableName: "REPORT_REQUEST_DESTS") {
            column(name: "REPORT_REQUEST_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_DESTINATION", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1555662378673-2") {
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "due_in_to_ha", type: "number(10,0)")
            column(name: "ibd", type: "timestamp")
            column(name: "inn", type: "varchar2(255 char)")
            column(name: "primary_reporting_destination", type: "varchar2(255 char)")
            column(name: "product_lead", type: "varchar2(255 char)")
            column(name: "reporting_period_end", type: "timestamp")
            column(name: "reporting_period_start", type: "timestamp")
            column(name: "requestor_notes", type: "varchar2(4000 char)")
            column(name: "linked_configurations", type: "varchar2(4000 char)")
            column(name: "linked_generated_reports", type: "varchar2(4000 char)")
            column(name: "days_to_dlp", type: "number(10,0)")
            column(name: "frequency", type: "varchar2(255 char)")
            column(name: "frequencyx", type: "number(19,0)")
            column(name: "occurrences", type: "number(19,0)")
            column(name: "parent_report_request", type: "number(10,0)")
            column(name: "cur_prd_due_date", type: "timestamp")
            column(name: "due_date_for_distribution", type: "timestamp")
            column(name: "due_date_to_ha_criteria", type: "timestamp")
            column(name: "period_covered_by_report", type: "varchar2(255 char)")
            column(name: "psr_type_file", type: "varchar2(255 char)")
            column(name: "dosage", type: "varchar2(255 char)")
            column(name: "master_planning_request", type: "number(1,0)")
            column(name: "previous_period_start", type: "timestamp")
            column(name: "previous_period_end", type: "timestamp")
            column(name: "previous_psr_type_file", type: "varchar2(255 char)")
        }
    }


    changeSet(author: "sergey khovrachev (generated)", id: "1555662378671-19") {
        dropNotNullConstraint(columnDataType: "varchar2(4000 char)", columnName: "description", tableName: "REPORT_REQUEST")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1556798586560-1") {
        createTable(tableName: "USER_DICT") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "USER_DICTPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "CREATED_BY", type: "varchar2(255 char)")
            column(name: "DATE_CREATED", type: "timestamp")
            column(name: "description", type: "varchar2(200 char)")
            column(name: "is_deleted", type: "number(1,0)")
            column(name: "LAST_UPDATED", type: "timestamp")
            column(name: "MODIFIED_BY", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "1557818134542-1") {
        createTable(tableName: "country_approve") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "country_approPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "approval_date", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "approver_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "app_comment", type: "varchar2(4000 char)")

            column(name: "country", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "approval_function", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "is_deleted", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "year", type: "number(10,0)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "sergey khovrachev  (generated)", id: "1557818134542-154") {
        addForeignKeyConstraint(baseColumnNames: "approver_id", baseTableName: "country_approve", constraintName: "FK_s0cigy8cv6gabkd3fj2x8tvwk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }
}







