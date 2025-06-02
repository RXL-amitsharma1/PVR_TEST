databaseChangeLog = {
    changeSet(author: "anurag", id: "2021051415221-1") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "AUTO_REASON_OF_DELAY")
            }
        }
        createTable(tableName: "AUTO_REASON_OF_DELAY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "AUTO_REASON_OF_DELAYPK")
            }

            column(name: "DATE_RANGE_TYPE", type: "varchar2(255 char)")

            column(name: "GLOBAL_DATE_RANGE_AUTOROD_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "SOURCE_PROFILE", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PVUSER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "NEXT_RUN_DATE", type: "timestamp")

            column(name: "LAST_RUN_DATE", type: "timestamp")

            column(name: "SCHEDULE_DATE", type: "varchar2(1024 char)")

            column(name: "SELECTED_TIME_ZONE", type: "varchar2(255 char)", defaultValue: "UTC") {
                constraints(nullable: "false")
            }

            column(name: "IS_ENABLED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "BLANK_VALUES", type: "clob")

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

    changeSet(author: "anurag", id: "2021051415221-2") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "QUERY_RCA")
            }
        }
        createTable(tableName: "QUERY_RCA") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_RCAPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SUPER_QUERY_ID", type: "number(19,0)")

            column(name: "LATE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "ROOT_CAUSE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "RESPONSIBLE_PARTY_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO_USER", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO_USER_GROUP", type: "number(19,0)") {
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

            column(name: "AUTO_REASON_OF_DELAY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_RCA_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "anurag", id: "2021051415221-3") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "DATE_RANGE_QUERYRCA")
            }
        }

        createTable(tableName: "DATE_RANGE_QUERYRCA") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DATE_RANGE_QUERYRCAPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "timestamp")

            column(name: "DATE_RNG_ENUM", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "timestamp")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_RCA_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anurag", id: "2021051415221-4") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "GLOBAL_DATE_RANGE_AUTOROD")
            }
        }

        createTable(tableName: "GLOBAL_DATE_RANGE_AUTOROD") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "GLOBAL_DATE_RANGEPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "timestamp")

            column(name: "DATE_RNG_ENUM", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "timestamp")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "number(10,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anurag", id: "2021051415221-5") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "QRS_RCA_QUERY_VALUES")
            }
        }

        createTable(tableName: "QRS_RCA_QUERY_VALUES") {
            column(name: "QUERY_RCA_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "number(19,0)")

            column(name: "QUERY_VALUE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "anurag", id: "2021051415221-6") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "QRS_RCA_QUERY_VALUES", constraintName: "FKB366EAE940891165", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag", id: "2021051415221-7") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_RCA_ID", baseTableName: "QRS_RCA_QUERY_VALUES", constraintName: "FKB366EAE98C475AC5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUERY_RCA", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag", id: "2021051415221-8") {
        addForeignKeyConstraint(baseColumnNames: "AUTO_REASON_OF_DELAY_ID", baseTableName: "QUERY_RCA", constraintName: "FK50866F05B2AF25DE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "AUTO_REASON_OF_DELAY", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag", id: "2021051415221-9") {
        addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "QUERY_RCA", constraintName: "FK50866F051CE57A1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag", id: "2021051415221-10") {
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_DATE_RANGE_AUTOROD_ID", baseTableName: "AUTO_REASON_OF_DELAY", constraintName: "FK68917214ADB415AF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "GLOBAL_DATE_RANGE_AUTOROD", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag", id: "2021052015221-11") {
        createTable(tableName: "AUTO_ROD_PVUSER") {
            column(name: "AUTO_REASON_OF_DELAY_ID", type: "number(19,0)")

            column(name: "EMAIL_USER", type: "varchar2(255 char)")

            column(name: "EMAIL_USERX", type: "number(19,0)")

        }
    }

    changeSet(author: "anurag", id: "202105312043-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUERY_RCA', columnName: 'RC_CUSTOM_EXPRESSION')
            }
        }
        addColumn(tableName: "QUERY_RCA") {
            column(name: "RC_CUSTOM_EXPRESSION", type: "varchar2(4000 char)")
        }

        addColumn(tableName: "QUERY_RCA") {
            column(name: "RP_CUSTOM_EXPRESSION", type: "varchar2(4000 char)")
        }
    }

    changeSet(author: "anurag", id: "202106021551-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUERY_RCA', columnName: 'SAME_AS_RESP_PARTY')
            }
        }
        addColumn(tableName: "QUERY_RCA") {
            column(name: "SAME_AS_RESP_PARTY", type: "number(1,0)", defaultValue: 0)
        }
    }

    changeSet(author: "anurag", id: "202107161744-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUTO_REASON_OF_DELAY', columnName: 'EXECUTING')
            }
        }
        addColumn(tableName: "AUTO_REASON_OF_DELAY") {
            column(name: "EXECUTING", type: "number(1,0)", defaultValue: 0)
        }
    }

    changeSet(author: "anurag", id: "202107201551-15") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUERY_RCA', columnName: 'RC_CLASSIFICATION_ID')
            }
        }
        addColumn(tableName: "QUERY_RCA") {
            column(name: "RC_CLASSIFICATION_ID", type: "varchar2(255 char)")
            column(name: "RC_CLASS_CUSTOM_EXP", type: "varchar2(4000 char)")
            column(name: "RC_SUB_CATEGORY_ID", type: "varchar2(255 char)")
            column(name: "RC_SUB_CAT_CUSTOM_EXP", type: "varchar2(4000 char)")
        }
    }

}