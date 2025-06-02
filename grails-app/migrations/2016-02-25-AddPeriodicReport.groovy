databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1456362752500-1") {
        createTable(tableName: "EX_GLOBAL_DATE_RANGE_INFO") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_GLOBAL_DATPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "timestamp")

            column(name: "DATE_RNG_ENUM", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "timestamp")

            column(name: "EXECUTED_AS_OF", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "number(10,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-2") {
        createTable(tableName: "EX_PERIODIC_EX_QUERY_VALUES") {
            column(name: "EX_PERIODIC_GLOBAL_QUERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_VALUE_ID", type: "number(19,0)")

            column(name: "EX_QUERY_VALUE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-3") {
        createTable(tableName: "EX_PERIODIC_GLOBAL_QUERY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_PERIODIC_GPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_GLOBAL_DATE_RANGE_INFO_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-4") {
        createTable(tableName: "GLOBAL_DATE_RANGE_INFO") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "GLOBAL_DATE_RPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "timestamp")

            column(name: "DATE_RNG_END_DELTA", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_ENUM", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "timestamp")

            column(name: "DATE_RNG_START_DELTA", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "number(10,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-5") {
        createTable(tableName: "PERIODIC_GLOAL_QUERY_VALUES") {
            column(name: "PERIODIC_GLOBAL_QUERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "number(19,0)")

            column(name: "QUERY_VALUE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-6") {
        createTable(tableName: "PERIODIC_GLOBAL_QUERY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PERIODIC_GLOBPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "GLOBAL_DATA_RANGE_INFO_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SUPER_QUERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-10") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "class", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_RCONFIG delivery set class = 'com.rxlogix.config.ExecutedConfiguration'")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "class")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-11") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "EX_PER_REP_GLOBAL_QUERY_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-12") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "INCLUDE_PREV_MISS_CASES", type: "number(1,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-13") {
        addColumn(tableName: "RCONFIG") {
            column(name: "class", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
        sql("update RCONFIG delivery set class = 'com.rxlogix.config.Configuration'")
        addNotNullConstraint(tableName: "RCONFIG", columnName: "class")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-14") {
        addColumn(tableName: "RCONFIG") {
            column(name: "GENERATE_CASE_SERIES", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update RCONFIG delivery set GENERATE_CASE_SERIES = 0")
        addNotNullConstraint(tableName: "RCONFIG", columnName: "GENERATE_CASE_SERIES")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-15") {
        addColumn(tableName: "RCONFIG") {
            column(name: "INCLUDE_PREV_MISS_CASES", type: "number(1,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-16") {
        addColumn(tableName: "RCONFIG") {
            column(name: "PERIODIC_REP_GLOBAL_QUERY_ID", type: "number(19,0)")
        }
    }


    changeSet(author: "sachinverma (generated)", id: "1456362752500-24") {
        dropForeignKeyConstraint(baseTableName: "EX_RCONFIG", constraintName: "FKC472BE88EA03BB10")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-25") {
        dropForeignKeyConstraint(baseTableName: "RCONFIG", constraintName: "FK689172149644252D")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-28") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_PERIODIC_EX_QUERY_VALUES", constraintName: "FK7639AD0DB741B4E0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-29") {
        addForeignKeyConstraint(baseColumnNames: "EX_GLOBAL_DATE_RANGE_INFO_ID", baseTableName: "EX_PERIODIC_GLOBAL_QUERY", constraintName: "FK61CF9DA4DD804A16", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_GLOBAL_DATE_RANGE_INFO", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-30") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_ID", baseTableName: "EX_PERIODIC_GLOBAL_QUERY", constraintName: "FK61CF9DA4E54B0969", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-31") {
        addForeignKeyConstraint(baseColumnNames: "EX_PER_REP_GLOBAL_QUERY_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE887BACFDB7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_PERIODIC_GLOBAL_QUERY", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-32") {
        addForeignKeyConstraint(baseColumnNames: "ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKAF86CB111C399E1C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-33") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "PERIODIC_GLOAL_QUERY_VALUES", constraintName: "FKC4FB830740891065", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-34") {
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_DATA_RANGE_INFO_ID", baseTableName: "PERIODIC_GLOBAL_QUERY", constraintName: "FK8B72C30ADB414AF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "GLOBAL_DATE_RANGE_INFO", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-35") {
        addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "PERIODIC_GLOBAL_QUERY", constraintName: "FK8B72C301CE47A1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-36") {
        addForeignKeyConstraint(baseColumnNames: "PERIODIC_REP_GLOBAL_QUERY_ID", baseTableName: "RCONFIG", constraintName: "FK689172145BBDBD3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PERIODIC_GLOBAL_QUERY", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456362752500-37") {
        addForeignKeyConstraint(baseColumnNames: "ID", baseTableName: "SHARED_WITH", constraintName: "FKA1C93860E19CFD44", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SHARED_WITH", referencesUniqueColumn: "false")
    }
}
