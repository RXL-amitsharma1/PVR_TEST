databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1483874430835-1") {
        createTable(tableName: "EX_CASE_SERIES") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CASE_SERIEPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "AS_OF_VERSION_DATE", type: "timestamp")

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RANGE_TYPE", type: "varchar2(255 char)")

            column(name: "DESCRIPTION", type: "clob")

            column(name: "ERROR_DATE", type: "timestamp")

            column(name: "EVALUATE_DATE_AS", type: "varchar2(255 char)")

            column(name: "EVENT_SELECTION", type: "clob")

            column(name: "EXCLUDE_FOLLOW_UP", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "EXCLUDE_NON_VALID_CASES", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_CS_DATE_RANGE_INFO_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_SUPER_QUERY_ID", type: "number(19,0)")

            column(name: "EXECUTION_STATUS", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "INCLUDE_LOCKED_VERSION", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_TEMPORARY", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp")

            column(name: "MESSAGE", type: "clob")

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "NUM_EXECUTIONS", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "PVUSER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_SELECTION", type: "clob")

            column(name: "SERIES_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "STACK_TRACE", type: "clob")

            column(name: "STUDY_SELECTION", type: "clob")

            column(name: "SUSPECT_PRODUCT", type: "number(1,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-2") {
        createTable(tableName: "EX_CASE_SERIES_QUERY_VALUES") {
            column(name: "EX_CASE_SERIES_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "number(19,0)")

            column(name: "QUERY_VALUE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-3") {
        createTable(tableName: "EX_CS_DATE_RANGE_INFO") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CASE_SERIRPK")
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

    changeSet(author: "sachinverma (generated)", id: "1483874430835-4") {
        addColumn(tableName: "CASE_SERIES") {
            column(name: "NUM_EXECUTIONS", type: "number(10,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update CASE_SERIES set NUM_EXECUTIONS = 1");
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-5") {
        sql("insert into EX_CASE_SERIES (ID, VERSION, AS_OF_VERSION_DATE, CREATED_BY,DATE_CREATED, DATE_RANGE_TYPE, DESCRIPTION, ERROR_DATE, EVALUATE_DATE_AS, EVENT_SELECTION, EXCLUDE_FOLLOW_UP," +
                "EXCLUDE_NON_VALID_CASES, EX_CS_DATE_RANGE_INFO_ID, EX_SUPER_QUERY_ID, EXECUTION_STATUS, INCLUDE_LOCKED_VERSION, IS_DELETED, IS_TEMPORARY, LAST_UPDATED, MODIFIED_BY, " +
                "NUM_EXECUTIONS, PVUSER_ID, PRODUCT_SELECTION, SERIES_NAME, STACK_TRACE, STUDY_SELECTION, SUSPECT_PRODUCT) select ID, VERSION, AS_OF_VERSION_DATE, CREATED_BY,DATE_CREATED, DATE_RANGE_TYPE, DESCRIPTION, ERROR_DATE_CREATED, EVALUATE_DATE_AS, EVENT_SELECTION, EXCLUDE_FOLLOW_UP," +
                "EXCLUDE_NON_VALID_CASES, CASE_SERIES_DATE_RANGE_INFO_ID, NULL, EXECUTION_STATUS, INCLUDE_LOCKED_VERSION, IS_DELETED, IS_TEMPORARY, LAST_UPDATED, MODIFIED_BY, " +
                "NUM_EXECUTIONS, PVUSER_ID, PRODUCT_SELECTION, SERIES_NAME, STACK_TRACE, STUDY_SELECTION, SUSPECT_PRODUCT from CASE_SERIES;")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-6") {
        sql("insert into EX_CS_DATE_RANGE_INFO ( ID, VERSION, DATE_RNG_END_ABSOLUTE, DATE_RNG_ENUM, DATE_RNG_START_ABSOLUTE, RELATIVE_DATE_RNG_VALUE ) " +
                " select ID, VERSION, DATE_RNG_END_ABSOLUTE, DATE_RNG_ENUM, DATE_RNG_START_ABSOLUTE, RELATIVE_DATE_RNG_VALUE from CASE_SERIRES_DATE_RANGE_INFO;")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-43") {
        dropColumn(columnName: "ERROR_DATE_CREATED", tableName: "CASE_SERIES")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-44") {
        dropColumn(columnName: "EXECUTION_STATUS", tableName: "CASE_SERIES")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-45") {
        dropColumn(columnName: "IS_TEMPORARY", tableName: "CASE_SERIES")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-46") {
        dropColumn(columnName: "MESSAGE", tableName: "CASE_SERIES")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-47") {
        dropColumn(columnName: "STACK_TRACE", tableName: "CASE_SERIES")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-48") {
        dropColumn(columnName: "SUBMISSION_DATE_BACK_UP", tableName: "RPT_SUBMISSION")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-49") {
        dropForeignKeyConstraint(baseTableName: "EX_RCONFIG", constraintName: "FKC472BE881441117B")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-50") {
        dropForeignKeyConstraint(baseTableName: "EX_RCONFIG", constraintName: "FKC472BE887F6E20B9")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-51") {
        dropForeignKeyConstraint(baseTableName: "EX_RCONFIG", constraintName: "FKC472BE88C502EF3D")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-52") {
        addForeignKeyConstraint(baseColumnNames: "USED_CASE_SERIES_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE881441117B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_CASE_SERIES", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-53") {
        addForeignKeyConstraint(baseColumnNames: "CASE_SERIES_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE887F6E20B9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_CASE_SERIES", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-54") {
        addForeignKeyConstraint(baseColumnNames: "CUM_CASE_SERIES_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE88C502EF3D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_CASE_SERIES", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-55") {
        dropForeignKeyConstraint(baseTableName: "RCONFIG", constraintName: "FK68917214E9622A71")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-56") {
        addForeignKeyConstraint(baseColumnNames: "USE_CASE_SERIES_ID", baseTableName: "RCONFIG", constraintName: "FK68917214E9622A71", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_CASE_SERIES", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-32") {
        addForeignKeyConstraint(baseColumnNames: "EX_CS_DATE_RANGE_INFO_ID", baseTableName: "EX_CASE_SERIES", constraintName: "FKFC2478DA8106F5AF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_CS_DATE_RANGE_INFO", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-33") {
        addForeignKeyConstraint(baseColumnNames: "EX_SUPER_QUERY_ID", baseTableName: "EX_CASE_SERIES", constraintName: "FKFC2478DAE99F4CAD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-34") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_CASE_SERIES", constraintName: "FKFC2478DA49425289", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-35") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "EX_CASE_SERIES_QUERY_VALUES", constraintName: "FK386883DECF70AFD4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-36") {
        sql("delete from localization");
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-37") {
        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "LANG_ID", type: "varchar2(255 char)")
        }
        sql("update CASE_SERIES set LANG_ID = 'en'")
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-39") {
        dropIndex(tableName: 'RPT_FIELD', indexName: 'NAME_UNIQ_1438219978330')
    }

    changeSet(author: "sachinverma (generated)", id: "1483874430835-40") {
        sql("delete from SOURCE_COLUMN_MASTER where LANG_ID is null")
    }
}
