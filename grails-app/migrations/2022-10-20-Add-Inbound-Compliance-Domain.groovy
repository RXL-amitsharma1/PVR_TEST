databaseChangeLog = {
    changeSet(author: "anurag (generated)", id: "202210201055-1") {
        createTable(tableName: "INBOUND_INITIAL_CONF") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "INBOUND_INITIAL_CONFPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FIELD_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "START_DATE", type: "timestamp")

            column(name: "IS_IC_INITIALIZE", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anurag", id: "202210201055-2") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "INBOUND_COMPLIANCE")
            }
        }
        createTable(tableName: "INBOUND_COMPLIANCE") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "INBOUND_COMPLIANCEPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_SELECTION", type: "clob")

            column(name: "PRODUCT_GROUP_SELECTION", type: "clob")

            column(name: "STUDY_SELECTION", type: "clob")

            column(name: "DATE_RANGE_TYPE", type: "varchar2(255 char)")

            column(name: "GLOBAL_DATA_RANGE_INBOUND_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SOURCE_PROFILE", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SUSPECT_PRODUCT", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "EXCLUDE_NON_VALID_CASES", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DISABLED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "SENDER_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "varchar2(4000 char)")

            column(name: "QUALITY_CHECKED", type: "number(1,0)")

            column(name: "IS_TEMPLATE", type: "number(1,0)")

            column(name: "PVUSER_ID", type: "number(19,0)") {
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

            column(name: "EXECUTING", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "TENANT_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "IS_IC_INITIALIZE", type: "number(1,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "202210201055-3") {
        createTable(tableName: "INBOUND_COMPLIANCE_TAGS") {
            column(name: "INBOUND_COMPLIANCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "number(19,0)")

            column(name: "INBOUND_COM_TAG_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "anurag (generated)", id: "202210201055-4") {
        createTable(tableName: "INBOUND_POI_PARAMS") {
            column(name: "INBOUND_COMPLIANCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PARAM_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "anurag", id: "202210201055-5") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "QUERY_COMPLIANCE")
            }
        }
        createTable(tableName: "QUERY_COMPLIANCE") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_COMPLIANCEPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CRITERIA_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "SUPER_QUERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ALLOWED_TIMEFRAME", type: "number(19,0)") {
                constraints(nullable: "false")
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

            column(name: "INBOUND_COMPLIANCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_COMPLIANCE_IDX", type: "number(10,0)")

            column(name: "INDX", type: "number(10,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anurag", id: "202210201055-6") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "QRS_COMPLIANCE_QUERY_VALUES")
            }
        }

        createTable(tableName: "QRS_COMPLIANCE_QUERY_VALUES") {
            column(name: "QUERY_COMPLIANCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "number(19,0)")

            column(name: "QUERY_VALUE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "anurag (generated)", id: "202210201055-7") {
        createTable(tableName: "GLOBAL_DATE_RANGE_INFO_INBOUND") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "GLOBAL_DATE_RIIPK")
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

    changeSet(author: "anurag (generated)", id: "202210201055-8") {
        createTable(tableName: "EX_INBOUND_COMPLIANCE") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_INBOUND_COMPLIANCEPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_SELECTION", type: "clob")

            column(name: "PRODUCT_GROUP_SELECTION", type: "clob")

            column(name: "STUDY_SELECTION", type: "clob")

            column(name: "DATE_RANGE_TYPE", type: "varchar2(255 char)")

            column(name: "EX_GLOBAL_DATA_RANGE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SOURCE_PROFILE", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SUSPECT_PRODUCT", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "EXCLUDE_NON_VALID_CASES", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DISABLED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "SENDER_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "varchar2(4000 char)")

            column(name: "QUALITY_CHECKED", type: "number(1,0)")

            column(name: "IS_TEMPLATE", type: "number(1,0)")

            column(name: "PVUSER_ID", type: "number(19,0)") {
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

            column(name: "EXECUTING", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "TENANT_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "IS_IC_INITIALIZE", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_STATUS", type: "varchar2(20 char)") {
                constraints(nullable: "false")
            }

            column(name: "INBOUND_COMPLIANCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "202210201055-9") {
        createTable(tableName: "EX_INBOUND_COMPLIANCE_TAGS") {
            column(name: "EX_INBOUND_COMPLIANCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "number(19,0)")

            column(name: "TAG_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "anurag (generated)", id: "202210201055-10") {
        createTable(tableName: "EX_INBOUND_POI_PARAMS") {
            column(name: "EX_INBOUND_COMPLIANCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PARAM_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "anurag", id: "202210201055-11") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "EX_QUERY_COMPLIANCE")
            }
        }
        createTable(tableName: "EX_QUERY_COMPLIANCE") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERY_COMPLIANCEPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CRITERIA_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ALLOWED_TIMEFRAME", type: "number(19,0)") {
                constraints(nullable: "false")
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

            column(name: "REPORT_RESULT_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "EX_INBOUND_COMPLIANCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_COMPLIANCE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "anurag (generated)", id: "202210201055-12") {
        createTable(tableName: "EX_QRS_COMPLIANCE_QUERY_VALUES") {
            column(name: "EX_QUERY_COMPLIANCE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_VALUE_ID", type: "number(19,0)")

            column(name: "EX_QUERY_VALUE_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "anurag (generated)", id: "202210201055-13") {
        createTable(tableName: "EX_GLOBAL_DATA_RANGE_INBOUND") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_GLOBAL_DATE_RIIPK")
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

    changeSet(author: "anurag", id: "202210201055-14") {
        addForeignKeyConstraint(baseColumnNames: "EX_GLOBAL_DATA_RANGE_ID", baseTableName: "EX_INBOUND_COMPLIANCE", constraintName: "FK68917215ADB415CF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_GLOBAL_DATA_RANGE_INBOUND", referencesUniqueColumn: "false")
    }


    changeSet(author: "anurag", id: "202210201055-15") {
        addForeignKeyConstraint(baseColumnNames: "INBOUND_COMPLIANCE_ID", baseTableName: "QUERY_COMPLIANCE", constraintName: "FK50867F05A2AF25DE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "INBOUND_COMPLIANCE", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag", id: "202210201055-16") {
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_DATA_RANGE_INBOUND_ID", baseTableName: "INBOUND_COMPLIANCE", constraintName: "FK68917214ADB415CF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "GLOBAL_DATE_RANGE_INFO_INBOUND", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag", id: "202210201055-17") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "INBOUND_COMPLIANCE_TAGS", constraintName: "FK3344B3999621E7DC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TAG", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag", id: "202210201055-18") {
        addForeignKeyConstraint(baseColumnNames: "EX_INBOUND_COMPLIANCE_ID", baseTableName: "EX_QUERY_COMPLIANCE", constraintName: "FKAF87CB112983C79B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_INBOUND_COMPLIANCE", referencesUniqueColumn: "false")
    }

    changeSet(author: "anurag", id: "202210201055-19") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "EX_INBOUND_COMPLIANCE_TAGS", constraintName: "FK3344B3999621E7DD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TAG", referencesUniqueColumn: "false")
    }

}