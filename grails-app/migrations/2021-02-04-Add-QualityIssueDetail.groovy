databaseChangeLog = {

    changeSet(author: "SachinS (generated)", id: "202102046001-1") {
        createTable(tableName: "QUALITY_ISSUE_DETAIL") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUALITY_ISSUE_DETAILPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
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

            column(name: "INVESTIGATION", type: "varchar2(8000 char)") {
                constraints(nullable: "true")
            }

            column(name: "SUMMARY", type: "varchar2(8000 char)") {
                constraints(nullable: "true")
            }

            column(name: "ACTIONS", type: "varchar2(8000 char)") {
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

    changeSet(author: "SachinS (generated)", id: "202102046002-2") {
        createTable(tableName: "QUALITY_CASE_ISSUE_DETAILS") {
            column(name: "QUALITY_CASE_ID", type: "number(19,0)")
            column(name: "QUALITY_ISSUE_DETAIL_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "SachinS (generated)", id: "202102046003-3") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_CASE_ID", baseTableName: "QUALITY_CASE_ISSUE_DETAILS", constraintName: "FK4F45B41C6458784", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_CASE_DATA", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "202102046004-4") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_ISSUE_DETAIL_ID", baseTableName: "QUALITY_CASE_ISSUE_DETAILS", constraintName: "FK4G45A41C6458564", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_ISSUE_DETAIL", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "202102046005-5") {
        createTable(tableName: "QUALITY_SUB_ISSUE_DETAILS") {
            column(name: "QUALITY_SUBMISSION_ID", type: "number(19,0)")
            column(name: "QUALITY_ISSUE_DETAIL_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "SachinS (generated)", id: "202102046006-6") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_SUBMISSION_ID", baseTableName: "QUALITY_SUB_ISSUE_DETAILS", constraintName: "FK4H45E41C6458574", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_SUBMISSION", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "202102046007-7") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_ISSUE_DETAIL_ID", baseTableName: "QUALITY_SUB_ISSUE_DETAILS", constraintName: "FK4G65A715F07F5DE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_ISSUE_DETAIL", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "202102046008-8") {
        createTable(tableName: "QUALITY_SAMPL_ISSUE_DETAILS") {
            column(name: "QUALITY_SAMPLING_ID", type: "number(19,0)")
            column(name: "QUALITY_ISSUE_DETAIL_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "SachinS (generated)", id: "202102046009-9") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_SAMPLING_ID", baseTableName: "QUALITY_SAMPL_ISSUE_DETAILS", constraintName: "FK4J65A985F07W5GE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_SAMPLING", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "202102047001-1") {
        addForeignKeyConstraint(baseColumnNames: "QUALITY_ISSUE_DETAIL_ID", baseTableName: "QUALITY_SAMPL_ISSUE_DETAILS", constraintName: "FK4U65A995FQ7F5HE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_ISSUE_DETAIL", referencesUniqueColumn: "false")
    }

    changeSet(author: "SachinS (generated)", id: "202102047002-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'QUALITY_ISSUE_TYPE_ID')
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "QUALITY_ISSUE_TYPE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "SachinS (generated)", id: "202102047003-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'QUALITY_ISSUE_TYPE_ID')
            }
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "QUALITY_ISSUE_TYPE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "SachinS (generated)", id: "202102047004-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'QUALITY_ISSUE_TYPE_ID')
            }
        }
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "QUALITY_ISSUE_TYPE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
}