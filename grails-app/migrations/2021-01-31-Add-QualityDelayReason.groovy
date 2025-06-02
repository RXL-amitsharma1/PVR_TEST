databaseChangeLog = {

    changeSet(author: "SachinS (generated)", id: "310120211001-1") {
        createTable(tableName: "QUALITY_DELAY_REASON") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUALITY_DELAY_REASONPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "LATE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ROOT_CAUSE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "RESPONSIBLE_PARTY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CORRECTIVE_ACTION_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CORRECTIVE_DATE", type: "TIMESTAMP(6)") {
                constraints(nullable: "false")
            }

            column(name: "PREVENTATIVE_ACTION_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PREVENTATIVE_DATE", type: "TIMESTAMP(6)") {
                constraints(nullable: "false")
            }

            column(name: "IS_PRIMARY", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "INVESTIGATION", type: "varchar2(32000 char)") {
                constraints(nullable: "true")
            }

            column(name: "SUMMARY", type: "varchar2(32000 char)") {
                constraints(nullable: "true")
            }

            column(name: "ACTIONS", type: "varchar2(32000 char)") {
                constraints(nullable: "true")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "TIMESTAMP(6)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "TIMESTAMP(6)") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "SachinS (generated)", id: "310120211002-2") {
        createTable(tableName: "QUALITY_CASE_DELAY_REASONS") {
            column(name: "QUALITY_CASE_ID", type: "number(19,0)")
            column(name: "QUALITY_DELAY_REASON_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "SachinS (generated)", id: "310120211003-3") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_CASE_ID", baseTableName: "QUALITY_CASE_DELAY_REASONS", constraintName: "FK4F45B41C6458487", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_CASE_DATA", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "310120211004-4") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_DELAY_REASON_ID", baseTableName: "QUALITY_CASE_DELAY_REASONS", constraintName: "FK4G45A41C6458465", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_DELAY_REASON", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "310120211005-5") {
        createTable(tableName: "QUALITY_SUB_DELAY_REASONS") {
            column(name: "QUALITY_SUBMISSION_ID", type: "number(19,0)")
            column(name: "QUALITY_DELAY_REASON_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "SachinS (generated)", id: "310120211006-6") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_SUBMISSION_ID", baseTableName: "QUALITY_SUB_DELAY_REASONS", constraintName: "FK4H45E41C6458475", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_SUBMISSION", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "310120211007-7") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_DELAY_REASON_ID", baseTableName: "QUALITY_SUB_DELAY_REASONS", constraintName: "FK4G65A715F07FED5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_DELAY_REASON", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "310120211008-8") {
        createTable(tableName: "QUALITY_SAMPL_DELAY_REASONS") {
            column(name: "QUALITY_SAMPLING_ID", type: "number(19,0)")
            column(name: "QUALITY_DELAY_REASON_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "SachinS (generated)", id: "310120211009-9") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_SAMPLING_ID", baseTableName: "QUALITY_SAMPL_DELAY_REASONS", constraintName: "FK4J65A985F07WEG5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_SAMPLING", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "310120212001-1") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_DELAY_REASON_ID", baseTableName: "QUALITY_SAMPL_DELAY_REASONS", constraintName: "FK4U65A995FQ7FEH5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_DELAY_REASON", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "310120212001-2") {
        dropColumn(columnName: "QUALITY_ISSUE_TYPE", tableName: "QUALITY_CASE_DATA")
        dropColumn(columnName: "ROOT_CAUSE", tableName: "QUALITY_CASE_DATA")
        dropColumn(columnName: "RESPONSIBLE_PARTY", tableName: "QUALITY_CASE_DATA")
        dropColumn(columnName: "QUALITY_ISSUE_TYPE", tableName: "QUALITY_SUBMISSION")
        dropColumn(columnName: "ROOT_CAUSE", tableName: "QUALITY_SUBMISSION")
        dropColumn(columnName: "RESPONSIBLE_PARTY", tableName: "QUALITY_SUBMISSION")
        dropColumn(columnName: "QUALITY_ISSUE_TYPE", tableName: "QUALITY_SAMPLING")
        dropColumn(columnName: "ROOT_CAUSE", tableName: "QUALITY_SAMPLING")
        dropColumn(columnName: "RESPONSIBLE_PARTY", tableName: "QUALITY_SAMPLING")
    }
}