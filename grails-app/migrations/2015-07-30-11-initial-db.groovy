databaseChangeLog = {

  changeSet(author: "prakriti (generated)", id: "1438219980719-1") {
    createTable(tableName: "ACCESS_CONTROL_GROUP") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACCESS_CONTROPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "DESCRIPTION", type: "varchar2(200 char)")

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "LDAP_GROUP_NAME", type: "varchar2(30 char)") {
        constraints(nullable: "false")
      }

      column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "NAME", type: "varchar2(30 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-2") {
    createTable(tableName: "ARGUS_COLUMN_MASTER") {
      column(name: "REPORT_ITEM", type: "varchar2(80 char)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ARGUS_COLUMN_PK")
      }

      column(name: "COLUMN_NAME", type: "varchar2(40 char)") {
        constraints(nullable: "false")
      }

      column(name: "COLUMN_TYPE", type: "varchar2(1 char)") {
        constraints(nullable: "false")
      }

      column(name: "CONCATENATED_FIELD", type: "varchar2(1 char)")

      column(name: "LM_DECODE_COLUMN", type: "varchar2(40 char)")

      column(name: "LM_JOIN_COLUMN", type: "varchar2(40 char)")

      column(name: "LM_JOIN_EQUI_OUTER", type: "varchar2(1 char)")

      column(name: "LM_TABLE_NAME_ATM_ID", type: "varchar2(40 char)")

      column(name: "PRIMARY_KEY_ID", type: "number(19,0)")

      column(name: "TABLE_NAME_ATM_ID", type: "varchar2(40 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-3") {
    createTable(tableName: "ARGUS_TABLE_MASTER") {
      column(name: "TABLE_NAME", type: "varchar2(40 char)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ARGUS_TABLE_MPK")
      }

      column(name: "CASE_JOIN_ORDER", type: "number(10,0)")

      column(name: "CASE_JOIN_EQUI_OUTER", type: "varchar2(1 char)")

      column(name: "TABLE_ALIAS", type: "varchar2(5 char)") {
        constraints(nullable: "false")
      }

      column(name: "TABLE_TYPE", type: "varchar2(1 char)") {
        constraints(nullable: "false")
      }

      column(name: "VERSIONED_DATA", type: "varchar2(1 char)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-4") {
    createTable(tableName: "AUDIT_LOG") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "AUDIT_LOGPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "CATEGORY", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "DESCRIPTION", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "USERNAME", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-5") {
    createTable(tableName: "AUDIT_LOG_FIELD_CHANGE") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "AUDIT_LOG_FIEPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "AUDIT_LOG_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "ENTITY_ID", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "ENTITY_NAME", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "FIELD_NAME", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "NEW", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "ORIGINAL", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-6") {
    createTable(tableName: "CASE_COLUMN_JOIN_MAPPING") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CASE_COLUMN_JPK")
      }

      column(name: "COLUMN_NAME", type: "varchar2(40 char)") {
        constraints(nullable: "false")
      }

      column(name: "MAP_COLUMN_NAME", type: "varchar2(40 char)") {
        constraints(nullable: "false")
      }

      column(name: "MAP_TABLE_NAME_ATM_ID", type: "varchar2(40 char)") {
        constraints(nullable: "false")
      }

      column(name: "TABLE_NAME_ATM_ID", type: "varchar2(40 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-7") {
    createTable(tableName: "CATEGORY") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CATEGORYPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "DEFAULT_NAME", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "NAME", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-8") {
    createTable(tableName: "CLL_TEMPLT") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CLL_TEMPLTPK")
      }

      column(name: "COLUMNS_RF_INFO_LIST_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "COL_SHOW_TOTAL", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "GROUPING_RF_INFO_LIST_ID", type: "number(19,0)")

      column(name: "PAGE_BREAK_BY_GROUP", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "RENAME_GROUPING", type: "clob")

      column(name: "RENAME_ROW_COLS", type: "clob")

      column(name: "ROW_COLS_RF_INFO_LIST_ID", type: "number(19,0)")

      column(name: "SUPPRESS_COLUMN_LIST", type: "varchar2(255 char)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-9") {
    createTable(tableName: "COGNOS_REPORT") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "COGNOS_REPORTPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "DESCRIPTION", type: "varchar2(1000 char)")

      column(name: "IS_DELETED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "NAME", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "URL", type: "varchar2(1000 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-10") {
    createTable(tableName: "DATE_RANGE") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DATE_RANGEPK")
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

  changeSet(author: "prakriti (generated)", id: "1438219980719-11") {
    createTable(tableName: "DELIVERIES_EMAIL_USERS") {
      column(name: "DELIVERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "EMAIL_USER", type: "varchar2(255 char)")

      column(name: "EMAIL_USER_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-12") {
    createTable(tableName: "DELIVERIES_RPT_FORMATS") {
      column(name: "DELIVERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "RPT_FORMAT", type: "varchar2(255 char)")

      column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-13") {
    createTable(tableName: "DELIVERIES_SHARED_WITHS") {
      column(name: "DELIVERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "SHARED_WITH_ID", type: "number(19,0)")

      column(name: "SHARED_WITH_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-14") {
    createTable(tableName: "DELIVERY") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DELIVERYPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-15") {
    createTable(tableName: "DTAB_MEASURE") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DTAB_MEASUREPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "CUSTOM_EXPRESSION", type: "varchar2(255 char)")

      column(name: "FROM_DATE", type: "timestamp")

      column(name: "TO_DATE", type: "timestamp")

      column(name: "DTAB_TEMPLT_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "date_range_count", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "NAME", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "PERCENTAGE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "SHOW_SUBTOTALS", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "SHOW_TOTAL", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "SHOW_TOTAL_AS_COLS", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "SHOW_TOTAL_ROWS", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "MEASURE_TYPE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "MEASURES_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-16") {
    createTable(tableName: "DTAB_TEMPLT") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DTAB_TEMPLTPK")
      }

      column(name: "COLUMNS_RF_INFO_LIST_ID", type: "number(19,0)")

      column(name: "ROWS_RF_INFO_LIST_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "SHOW_TOTAL_CUMULATIVE_CASES", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "SHOW_TOTAL_INTERVAL_CASES", type: "number(1,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-17") {
    createTable(tableName: "EX_CLL_TEMPLT") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CLL_TEMPLTPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-18") {
    createTable(tableName: "EX_CUSTOM_SQL_QUERY") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CUSTOM_QUERYPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-19") {
    createTable(tableName: "EX_CUSTOM_SQL_TEMPLT") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CUSTOM_TEMPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-20") {
    createTable(tableName: "EX_DATE_RANGE") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_DATE_RANGEPK")
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

  changeSet(author: "prakriti (generated)", id: "1438219980719-21") {
    createTable(tableName: "EX_DELIVERIES_EMAIL_USERS") {
      column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "EMAIL_USER", type: "varchar2(255 char)")

      column(name: "EMAIL_USER_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-22") {
    createTable(tableName: "EX_DELIVERIES_RPT_FORMATS") {
      column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "RPT_FORMAT", type: "varchar2(255 char)")

      column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-23") {
    createTable(tableName: "EX_DELIVERIES_SHARED_WITHS") {
      column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "SHARED_WITH_ID", type: "number(19,0)")

      column(name: "SHARED_WITH_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-24") {
    createTable(tableName: "EX_DELIVERY") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_DELIVERYPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-25") {
    createTable(tableName: "EX_DTAB_TEMPLT") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_DTAB_TEMPLPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-26") {
    createTable(tableName: "EX_EXPRESSION") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_EXPRESSIONPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "OPERATOR", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "RPT_FIELD_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "VALUE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-27") {
    createTable(tableName: "EX_NCASE_SQL_TEMPLT") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_NCASE_SQL_PK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-28") {
    createTable(tableName: "EX_QUERY") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERYPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-29") {
    createTable(tableName: "EX_QUERY_EXP") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERY_EXPPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-30") {
    createTable(tableName: "EX_QUERY_SET") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERY_SETPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-31") {
    createTable(tableName: "EX_QUERY_VALUE") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERY_VALUPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-32") {
    createTable(tableName: "EX_RCONFIG") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_RCONFIGPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "ADJUST_PER_SCHED_FREQUENCY", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "AS_OF_VERSION_DATE", type: "timestamp")

      column(name: "BLANK_VALUES", type: "clob")

      column(name: "SELECTED_TIME_ZONE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "DATE_RANGE_TYPE", type: "varchar2(255 char)")

      column(name: "DESCRIPTION", type: "varchar2(200 char)")

      column(name: "EVALUATE_DATE_AS", type: "varchar2(255 char)")

      column(name: "EVENT_SELECTION", type: "clob")

      column(name: "EXCLUDE_FOLLOWUP", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "EXCLUDE_NON_VALID_CASES", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "INCLUDE_LOCKED_VERSION", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_DELETED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_ENABLED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_PUBLIC", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp")

      column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "NEXT_RUN_DATE", type: "timestamp")

      column(name: "NUM_OF_EXECUTIONS", type: "number(10,0)") {
        constraints(nullable: "false")
      }

      column(name: "PVUSER_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "PRODUCT_SELECTION", type: "clob")

      column(name: "REPORT_NAME", type: "varchar2(200 char)") {
        constraints(nullable: "false")
      }

      column(name: "SCHEDULE_DATE", type: "varchar2(1024 char)")

      column(name: "STUDY_SELECTION", type: "clob")

      column(name: "TOTAL_EXECUTION_TIME", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-33") {
    createTable(tableName: "EX_RCONFIGS_TAGS") {
      column(name: "EXC_RCONFIG_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "TAG_ID", type: "number(19,0)")

      column(name: "TAG_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-34") {
    createTable(tableName: "EX_SQL_VALUE") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_SQL_VALUEPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-35") {
    createTable(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES") {
      column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "EX_QUERY_VALUE_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-36") {
    createTable(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES") {
      column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "EX_TEMPLT_VALUE_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-37") {
    createTable(tableName: "EX_TEMPLT_QUERY") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_TEMPLT_QUEPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "EX_DATE_RANGE_INFO_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "EX_QUERY_ID", type: "number(19,0)")

      column(name: "EX_TEMPLT_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "FOOTER", type: "varchar2(255 char)")

      column(name: "HEADER", type: "varchar2(255 char)")

      column(name: "HEADER_DATE_RANGE", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "QUERY_LEVEL", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "TITLE", type: "varchar2(255 char)")

      column(name: "EX_TEMPLT_QUERY_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-38") {
    createTable(tableName: "EX_TEMPLT_VALUE") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_TEMPLT_VALPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-39") {
    createTable(tableName: "EXPRESSION") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EXPRESSIONPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "OPERATOR", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "REPORT_FIELD_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "VALUE", type: "varchar2(255 char)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-40") {
    createTable(tableName: "NONCASE_SQL_TEMPLT") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "NONCASE_SQL_TPK")
      }

      column(name: "COL_NAME_LIST", type: "varchar2(2048 char)") {
        constraints(nullable: "false")
      }

      column(name: "NON_CASE_SQL", type: "clob")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-41") {
    createTable(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES") {
      column(name: "NONCASE_SQL_TEMPLT_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "SQL_VALUE_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-42") {
    createTable(tableName: "NOTIFICATION") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "NOTIFICATIONPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "LVL", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "MESSAGE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "USER_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "Chetan (generated)", id: "1457116850232-2") {
    addColumn(tableName: "NOTIFICATION") {
      column(name: "APP_NAME", type: "varchar2(255 char)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-43") {
    createTable(tableName: "PARAM") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PARAMPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "LOOKUP", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "VALUE", type: "varchar2(255 char)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-44") {
    createTable(tableName: "PREFERENCE") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PREFERENCEPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "LOCALE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "TIME_ZONE", type: "varchar2(255 char)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-45") {
    createTable(tableName: "PVUSER") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PVUSERPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "ACCOUNT_EXPIRED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "ACCOUNT_LOCKED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "BAD_PASSWORD_ATTEMPTS", type: "number(10,0)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "ENABLED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "PASSWORD_EXPIRED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "PREFERENCE_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "USERNAME", type: "varchar2(30 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-46") {
    createTable(tableName: "PVUSERS_ROLES") {
      column(name: "role_id", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "user_id", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-47") {
    createTable(tableName: "QUERIES_QRS_EXP_VALUES") {
      column(name: "QUERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "QUERY_EXP_VALUE_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-48") {
    createTable(tableName: "QUERY") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERYPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-49") {
    createTable(tableName: "QUERY_EXP_VALUE") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_EXP_VALPK")
      }

      column(name: "OPERATOR_ID", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "REPORT_FIELD_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-50") {
    createTable(tableName: "QUERY_SET") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_SETPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-51") {
    createTable(tableName: "QUERY_SETS_SUPER_QRS") {
      column(name: "QUERY_SET_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "SUPER_QUERY_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-52") {
    createTable(tableName: "QUERY_VALUE") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_VALUEPK")
      }

      column(name: "SUPER_QUERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-53") {
    createTable(tableName: "RCONFIG") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RCONFIGPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "ADJUST_PER_SCHED_FREQUENCY", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "AS_OF_VERSION_DATE", type: "timestamp")

      column(name: "AS_OF_VERSION_DATE_DELTA", type: "number(10,0)") {
        constraints(nullable: "false")
      }

      column(name: "BLANK_VALUES", type: "clob")

      column(name: "SELECTED_TIME_ZONE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "DATE_RANGE_TYPE", type: "varchar2(255 char)")

      column(name: "DELIVERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "DESCRIPTION", type: "varchar2(200 char)")

      column(name: "EVALUATE_DATE_AS", type: "varchar2(255 char)")

      column(name: "EVENT_SELECTION", type: "clob")

      column(name: "EXCLUDE_FOLLOWUP", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "EXCLUDE_NON_VALID_CASES", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "INCLUDE_LOCKED_VERSION", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_DELETED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_ENABLED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_PUBLIC", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp")

      column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "NEXT_RUN_DATE", type: "timestamp")

      column(name: "NUM_OF_EXECUTIONS", type: "number(10,0)") {
        constraints(nullable: "false")
      }

      column(name: "PVUSER_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "PRODUCT_SELECTION", type: "clob")

      column(name: "REPORT_NAME", type: "varchar2(200 char)") {
        constraints(nullable: "false")
      }

      column(name: "SCHEDULE_DATE", type: "varchar2(1024 char)")

      column(name: "STUDY_SELECTION", type: "clob")

      column(name: "TOTAL_EXECUTION_TIME", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-54") {
    createTable(tableName: "RCONFIGS_TAGS") {
      column(name: "RCONFIG_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "TAG_ID", type: "number(19,0)")

      column(name: "TAG_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-55") {
    createTable(tableName: "ROLE") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ROLEPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "AUTHORITY", type: "varchar2(50 char)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "DESCRIPTION", type: "varchar2(200 char)")

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-56") {
    createTable(tableName: "RPT_ERROR") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_ERRORPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "RCONFIG_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "EX_STATUS", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "MESSAGE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "RPT_RESULT_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-57") {
    createTable(tableName: "RPT_FIELD") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FIELDPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "ARGUS_COLUMN_MASTER_ID", type: "varchar2(80 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATA_TYPE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "date_format", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "DESCRIPTION", type: "varchar2(255 char)")

      column(name: "RPT_FIELD_GROUP_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_TEXT", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "LIST_DOMAIN_CLASS", type: "varchar2(255 char)")

      column(name: "NAME", type: "varchar2(128 char)") {
        constraints(nullable: "false")
      }

      column(name: "QUERY_SELECTABLE", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "TEMPLT_CLL_SELECTABLE", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "TEMPLT_DTCOL_SELECTABLE", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "TEMPLT_DTROW_SELECTABLE", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "TRANSFORM", type: "varchar2(255 char)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-58") {
    createTable(tableName: "RPT_FIELD_GROUP") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FIELD_GROPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "NAME", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-59") {
    createTable(tableName: "RPT_FIELD_INFO") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FIELD_INFPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "ARGUS_NAME", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "BLINDED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "COMMA_SEPARATED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "CUSTOM_EXPRESSION", type: "varchar2(255 char)")

      column(name: "DATASHEET", type: "varchar2(255 char)")

      column(name: "RENAME_VALUE", type: "varchar2(255 char)")

      column(name: "RPT_FIELD_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "RF_INFO_LIST_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "sort", type: "varchar2(255 char)")

      column(name: "SORT_LEVEL", type: "number(10,0)") {
        constraints(nullable: "false")
      }

      column(name: "STACK_ID", type: "number(10,0)") {
        constraints(nullable: "false")
      }

      column(name: "SUPPRESS_REPEATING", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "RF_INFO_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-60") {
    createTable(tableName: "RPT_FIELD_INFO_LIST") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FIELD_LSTPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-61") {
    createTable(tableName: "RPT_RESULT") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_RESULTPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "RPT_RESULT_DATA_ID", type: "number(19,0)")

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)")

      column(name: "EX_STATUS", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "FILTER_VERSION_TIME", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "FREQUENCY", type: "varchar2(255 char)")

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "QUERY_ROWS", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "QUERY_TIME", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "REASSESS_TIME", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "REPORT_ROWS", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "REPORT_TIME", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "RUN_DATE", type: "timestamp")

      column(name: "SCHEDULED_PVUSER_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "SEQUENCE", type: "number(10,0)") {
        constraints(nullable: "false")
      }

      column(name: "TEMPLT_QUERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "TOTAL_TIME", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "VERSION_ROWS", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "FILTERED_VERSION_ROWS", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "VERSION_TIME", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-62") {
    createTable(tableName: "RPT_RESULT_DATA") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_RESULT_DAPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "CROSS_TAB_SQL", type: "clob")

      column(name: "GTT_SQL", type: "clob")

      column(name: "HEADER_SQL", type: "clob")

      column(name: "QUERY_SQL", type: "clob")

      column(name: "REPORT_SQL", type: "clob")

      column(name: "VALUE", type: "blob")

      column(name: "VERSION_SQL", type: "clob")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-63") {
    createTable(tableName: "RPT_TEMPLT") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_TEMPLTPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "CATEGORY_ID", type: "number(19,0)")

      column(name: "CREATED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "DESCRIPTION", type: "varchar2(200 char)")

      column(name: "factory_default", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "HASBLANKS", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_DELETED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_PUBLIC", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
        constraints(nullable: "false")
      }

      column(name: "NAME", type: "varchar2(200 char)") {
        constraints(nullable: "false")
      }

      column(name: "ORIG_TEMPLT_ID", type: "number(10,0)") {
        constraints(nullable: "false")
      }

      column(name: "PV_USER_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "REASSESS_LISTEDNESS", type: "varchar2(255 char)")

      column(name: "TEMPLATE_TYPE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-64") {
    createTable(tableName: "RPT_TEMPLTS_TAGS") {
      column(name: "RPT_TEMPLT_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "TAG_ID", type: "number(19,0)")

      column(name: "TAG_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-65") {
    createTable(tableName: "SHARED_WITH") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SHARED_WITHPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_DELETED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "STATUS", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "RPT_USER_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-66") {
    createTable(tableName: "SQL_QRS_SQL_VALUES") {
      column(name: "SQL_QUERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "SQL_VALUE_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-67") {
    createTable(tableName: "SQL_QUERY") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_QUERYPK")
      }

      column(name: "QUERY", type: "clob") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-68") {
    createTable(tableName: "SQL_TEMPLT") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_TEMPLTPK")
      }

      column(name: "COLUMN_NAMES", type: "varchar2(2048 char)") {
        constraints(nullable: "false")
      }

      column(name: "SELECT_FROM_STMT", type: "clob")

      column(name: "WHERE_STMT", type: "clob")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-69") {
    createTable(tableName: "SQL_TEMPLT_VALUE") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_TEMPLT_VAPK")
      }

      column(name: "FIELD", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-70") {
    createTable(tableName: "SQL_TEMPLTS_SQL_VALUES") {
      column(name: "SQL_TEMPLT_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "SQL_VALUE_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-71") {
    createTable(tableName: "SQL_VALUE") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_VALUEPK")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-72") {
    createTable(tableName: "SUPER_QRS_TAGS") {
      column(name: "SUPER_QUERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "TAG_ID", type: "number(19,0)")

      column(name: "TAG_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-73") {
    createTable(tableName: "SUPER_QUERY") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SUPER_QUERYPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "QUERY", type: "clob")

      column(name: "CREATED_BY", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "DESCRIPTION", type: "varchar2(200 char)")

      column(name: "factory_default", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "HAS_BLANKS", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_DELETED", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "IS_PUBLIC", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "NAME", type: "varchar2(200 char)") {
        constraints(nullable: "false")
      }

      column(name: "ORIG_QUERY_ID", type: "number(10,0)") {
        constraints(nullable: "false")
      }

      column(name: "QUERY_TYPE", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-74") {
    createTable(tableName: "TAG") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TAGPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "NAME", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-75") {
    createTable(tableName: "TEMPLT_QRS_QUERY_VALUES") {
      column(name: "TEMPLT_QUERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "QUERY_VALUE_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-76") {
    createTable(tableName: "TEMPLT_QRS_TEMPLT_VALUES") {
      column(name: "TEMPLT_QUERY_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "TEMPLT_VALUE_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-77") {
    createTable(tableName: "TEMPLT_QUERY") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TEMPLT_QUERYPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "CREATED_BY", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "DATE_CREATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "DATE_RANGE_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "FOOTER", type: "varchar2(255 char)")

      column(name: "HEADER", type: "varchar2(255 char)")

      column(name: "HEADER_DATE_RANGE", type: "number(1,0)") {
        constraints(nullable: "false")
      }

      column(name: "INDX", type: "number(10,0)") {
        constraints(nullable: "false")
      }

      column(name: "LAST_UPDATED", type: "timestamp") {
        constraints(nullable: "false")
      }

      column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "SUPER_QUERY_ID", type: "number(19,0)")

      column(name: "QUERY_LEVEL", type: "varchar2(255 char)") {
        constraints(nullable: "false")
      }

      column(name: "RCONFIG_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "RPT_TEMPLT_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "TITLE", type: "varchar2(255 char)")

      column(name: "TEMPLT_QUERY_IDX", type: "number(10,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-78") {
    createTable(tableName: "TEMPLT_VALUE") {
      column(name: "id", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TEMPLT_VALUEPK")
      }

      column(name: "RPT_TEMPLT_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-79") {
    createTable(tableName: "VALUE") {
      column(name: "ID", type: "number(19,0)") {
        constraints(nullable: "false", primaryKey: "true", primaryKeyName: "VALUEPK")
      }

      column(name: "VERSION", type: "number(19,0)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-80") {
    createTable(tableName: "VALUES_PARAMS") {
      column(name: "VALUE_ID", type: "number(19,0)") {
        constraints(nullable: "false")
      }

      column(name: "PARAM_ID", type: "number(19,0)")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-81") {
    addPrimaryKey(columnNames: "role_id, user_id", constraintName: "PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-180") {
    createIndex(indexName: "LDAP_GRP_NM_1438219978296", tableName: "ACCESS_CONTROL_GROUP", unique: "true") {
      column(name: "LDAP_GROUP_NAME")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-181") {
    createIndex(indexName: "NAME_uniq_1438219978299", tableName: "ACCESS_CONTROL_GROUP", unique: "true") {
      column(name: "NAME")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-182") {
    createIndex(indexName: "NAME_uniq_1438219978311", tableName: "CATEGORY", unique: "true") {
      column(name: "NAME")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-183") {
    createIndex(indexName: "USERNAME_uniq_1438219978325", tableName: "PVUSER", unique: "true") {
      column(name: "USERNAME")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-184") {
    createIndex(indexName: "AUTHORITY_uniq_1438219978328", tableName: "ROLE", unique: "true") {
      column(name: "AUTHORITY")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-185") {
    createIndex(indexName: "NAME_uniq_1438219978330", tableName: "RPT_FIELD", unique: "true") {
      column(name: "NAME")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-186") {
    createIndex(indexName: "NAME_uniq_1438219978331", tableName: "RPT_FIELD_GROUP", unique: "true") {
      column(name: "NAME")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-187") {
    createIndex(indexName: "NAME_uniq_1438219978336", tableName: "TAG", unique: "true") {
      column(name: "NAME")
    }
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-188") {
    createSequence(sequenceName: "hibernate_sequence")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-82") {
    addForeignKeyConstraint(baseColumnNames: "LM_TABLE_NAME_ATM_ID", baseTableName: "ARGUS_COLUMN_MASTER", constraintName: "FKEB1F49C0B3FC82A2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "ARGUS_TABLE_MASTER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-83") {
    addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "ARGUS_COLUMN_MASTER", constraintName: "FKEB1F49C0780789E0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "ARGUS_TABLE_MASTER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-84") {
    addForeignKeyConstraint(baseColumnNames: "AUDIT_LOG_ID", baseTableName: "AUDIT_LOG_FIELD_CHANGE", constraintName: "FKE6EEDBF4378D1321", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "AUDIT_LOG", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-85") {
    addForeignKeyConstraint(baseColumnNames: "MAP_TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK5D4E9D538627F9BD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "ARGUS_TABLE_MASTER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-86") {
    addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK5D4E9D53780789E0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "ARGUS_TABLE_MASTER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-87") {
    addForeignKeyConstraint(baseColumnNames: "COLUMNS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKFF1A7F18134C62A8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-88") {
    addForeignKeyConstraint(baseColumnNames: "GROUPING_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKFF1A7F18BD6C5BCE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-89") {
    addForeignKeyConstraint(baseColumnNames: "ID", baseTableName: "CLL_TEMPLT", constraintName: "FKFF1A7F1888B11947", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-90") {
    addForeignKeyConstraint(baseColumnNames: "ROW_COLS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKFF1A7F181F8A7B03", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-91") {
    addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "DELIVERIES_SHARED_WITHS", constraintName: "FK8A606F804F5A069A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-92") {
    addForeignKeyConstraint(baseColumnNames: "DTAB_TEMPLT_ID", baseTableName: "DTAB_MEASURE", constraintName: "FK15CDC3B03CA2389B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-93") {
    addForeignKeyConstraint(baseColumnNames: "COLUMNS_RF_INFO_LIST_ID", baseTableName: "DTAB_TEMPLT", constraintName: "FK56FDF7AA134C62A8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-94") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "DTAB_TEMPLT", constraintName: "FK56FDF7AA88B11947", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-95") {
    addForeignKeyConstraint(baseColumnNames: "ROWS_RF_INFO_LIST_ID", baseTableName: "DTAB_TEMPLT", constraintName: "FK56FDF7AA8B2AD4A4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-96") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_CLL_TEMPLT", constraintName: "FK2E5A6E24DD5143B7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CLL_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-97") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_CUSTOM_SQL_QUERY", constraintName: "FK6868619585D555A4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-98") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_CUSTOM_SQL_TEMPLT", constraintName: "FKA8E47D0F45CA0634", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-99") {
    addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_DELIVERIES_SHARED_WITHS", constraintName: "FKFF2253F44F5A069A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-100") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_DTAB_TEMPLT", constraintName: "FKFBBEA1EE75C67E6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-101") {
    addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_ID", baseTableName: "EX_EXPRESSION", constraintName: "FKE1256DA433249131", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-102") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_NCASE_SQL_TEMPLT", constraintName: "FK598CD07AE962AD04", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "NONCASE_SQL_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-103") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_QUERY", constraintName: "FKD58EFA1CC2B269C5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-104") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_QUERY_EXP", constraintName: "FK2338C95AB9F42CCA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_EXP_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-105") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_QUERY_SET", constraintName: "FK2338FB9F122EBC73", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_SET", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-106") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_QUERY_VALUE", constraintName: "FK3910F16ECC50540", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-107") {
    addForeignKeyConstraint(baseColumnNames: "EX_DELIVERY_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE88EA03BB10", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_DELIVERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-108") {
    addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE8849425289", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-109") {
    addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "EX_RCONFIGS_TAGS", constraintName: "FKB43FD90D9621E7DC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TAG", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-110") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_SQL_VALUE", constraintName: "FK347FC2F48612CE4D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-111") {
    addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK3D9BA761B741B4E0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-112") {
    addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK3D9BA7612F961DEA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-113") {
    addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK8603209D2F961DEA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-114") {
    addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK8603209D96233108", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-115") {
    addForeignKeyConstraint(baseColumnNames: "EX_DATE_RANGE_INFO_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKAF86CB11B0D7610F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_DATE_RANGE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-116") {
    addForeignKeyConstraint(baseColumnNames: "EX_QUERY_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKAF86CB11E54B0969", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-117") {
    addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKAF86CB112983C79B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-118") {
    addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKAF86CB117A63277E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-119") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "EX_TEMPLT_VALUE", constraintName: "FKAFC443BA9517E052", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-120") {
    addForeignKeyConstraint(baseColumnNames: "REPORT_FIELD_ID", baseTableName: "EXPRESSION", constraintName: "FKB1E57E98BF61BBB3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-121") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "NONCASE_SQL_TEMPLT", constraintName: "FK324FC22F88B11947", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-122") {
    addForeignKeyConstraint(baseColumnNames: "NONCASE_SQL_TEMPLT_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FK347A93EEC7D47154", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "NONCASE_SQL_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-123") {
    addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FK347A93EE74267B2C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-124") {
    addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "NOTIFICATION", constraintName: "FKAD9970EB8987134F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-125") {
    addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "PVUSER", constraintName: "FK8D677AD1431B0D8F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PREFERENCE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-126") {
    addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "PVUSERS_ROLES", constraintName: "FKCC5B17C0E45C4F6F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ROLE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-127") {
    addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "PVUSERS_ROLES", constraintName: "FKCC5B17C08987134F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-128") {
    addForeignKeyConstraint(baseColumnNames: "QUERY_EXP_VALUE_ID", baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FK6E9963CA8744C51", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_EXP_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-129") {
    addForeignKeyConstraint(baseColumnNames: "QUERY_ID", baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FK6E9963CA7D28A09C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-130") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "QUERY", constraintName: "FK49D20A8E4C81486", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-131") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "QUERY_EXP_VALUE", constraintName: "FK72CE41B88393D6C1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-132") {
    addForeignKeyConstraint(baseColumnNames: "REPORT_FIELD_ID", baseTableName: "QUERY_EXP_VALUE", constraintName: "FK72CE41B8BF61BBB3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-133") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "QUERY_SET", constraintName: "FK3A78FC2BE4C81486", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-134") {
    addForeignKeyConstraint(baseColumnNames: "QUERY_SET_ID", baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FKB3E3F577707264C7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_SET", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-135") {
    addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FKB3E3F5771CE47A1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-136") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "QUERY_VALUE", constraintName: "FK8052FEFA71B325AC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-137") {
    addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "QUERY_VALUE", constraintName: "FK8052FEFA1CE47A1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-138") {
    addForeignKeyConstraint(baseColumnNames: "DELIVERY_ID", baseTableName: "RCONFIG", constraintName: "FK689172149644252D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DELIVERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-139") {
    addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "RCONFIG", constraintName: "FK6891721449425289", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-140") {
    addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RCONFIGS_TAGS", constraintName: "FK3334B3999621E7DC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TAG", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-141") {
    addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FKF5E4423F2983C79B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-142") {
    addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_ERROR", constraintName: "FKF5E4423F2F961DEA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-143") {
    addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FKF5E4423FA2AF25DE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-144") {
    addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_ID", baseTableName: "RPT_ERROR", constraintName: "FKF5E4423F8DD35DC3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_RESULT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-145") {
    addForeignKeyConstraint(baseColumnNames: "ARGUS_COLUMN_MASTER_ID", baseTableName: "RPT_FIELD", constraintName: "FKF5EE11312A698EE5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "REPORT_ITEM", referencedTableName: "ARGUS_COLUMN_MASTER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-146") {
    addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_GROUP_ID", baseTableName: "RPT_FIELD", constraintName: "FKF5EE113191B256C0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD_GROUP", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-147") {
    addForeignKeyConstraint(baseColumnNames: "RF_INFO_LIST_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FKEA2F1AFCE6F8CF0A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-148") {
    addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FKEA2F1AFC33249131", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-149") {
    addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FKDC1C5EA62F961DEA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-150") {
    addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_DATA_ID", baseTableName: "RPT_RESULT", constraintName: "FKDC1C5EA662F025B0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_RESULT_DATA", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-151") {
    addForeignKeyConstraint(baseColumnNames: "SCHEDULED_PVUSER_ID", baseTableName: "RPT_RESULT", constraintName: "FKDC1C5EA641CBD977", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-152") {
    addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FKDC1C5EA68C474AC5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TEMPLT_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-153") {
    addForeignKeyConstraint(baseColumnNames: "CATEGORY_ID", baseTableName: "RPT_TEMPLT", constraintName: "FKDF8342E5B66401D8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CATEGORY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-154") {
    addForeignKeyConstraint(baseColumnNames: "PV_USER_ID", baseTableName: "RPT_TEMPLT", constraintName: "FKDF8342E54DC36216", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-155") {
    addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RPT_TEMPLTS_TAGS", constraintName: "FK3180E3AA9621E7DC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TAG", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-156") {
    addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "SHARED_WITH", constraintName: "FKA1C938602983C79B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-157") {
    addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "SHARED_WITH", constraintName: "FKA1C9386073121E06", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-158") {
    addForeignKeyConstraint(baseColumnNames: "SQL_QUERY_ID", baseTableName: "SQL_QRS_SQL_VALUES", constraintName: "FK599A5B91826CA7CC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-159") {
    addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_QRS_SQL_VALUES", constraintName: "FK599A5B9174267B2C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-160") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "SQL_QUERY", constraintName: "FK4B824AD7E4C81486", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-161") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "SQL_TEMPLT", constraintName: "FK2907BC0D88B11947", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-162") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "SQL_TEMPLT_VALUE", constraintName: "FK71F2BA9F8393D6C1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-163") {
    addForeignKeyConstraint(baseColumnNames: "SQL_TEMPLT_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FK17A4B0CCBDF35FC6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-164") {
    addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FK17A4B0CC74267B2C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-165") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "SQL_VALUE", constraintName: "FK4BBFC3808393D6C1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-166") {
    addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "SUPER_QRS_TAGS", constraintName: "FK9D7F1E4A9621E7DC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TAG", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-167") {
    addForeignKeyConstraint(baseColumnNames: "CREATED_BY", baseTableName: "SUPER_QUERY", constraintName: "FK5014CAC4E3EFCE4E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-168") {
    addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKB366EAE940891065", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-169") {
    addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKB366EAE98C474AC5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TEMPLT_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-170") {
    addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FKC9A04E158C474AC5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TEMPLT_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-171") {
    addForeignKeyConstraint(baseColumnNames: "TEMPLT_VALUE_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FKC9A04E1587A11E63", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-172") {
    addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK50866F05A8615829", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DATE_RANGE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-173") {
    addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK50866F05A2AF25DE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-174") {
    addForeignKeyConstraint(baseColumnNames: "RPT_TEMPLT_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK50866F05B1CB5D01", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-175") {
    addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK50866F051CE47A1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-176") {
    addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "TEMPLT_VALUE", constraintName: "FK50C3E7AE71B325AC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "VALUE", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-177") {
    addForeignKeyConstraint(baseColumnNames: "RPT_TEMPLT_ID", baseTableName: "TEMPLT_VALUE", constraintName: "FK50C3E7AEB1CB5D01", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-178") {
    addForeignKeyConstraint(baseColumnNames: "PARAM_ID", baseTableName: "VALUES_PARAMS", constraintName: "FK36882F43C9663BB3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM", referencesUniqueColumn: "false")
  }

  changeSet(author: "prakriti (generated)", id: "1438219980719-179") {
    addForeignKeyConstraint(baseColumnNames: "VALUE_ID", baseTableName: "VALUES_PARAMS", constraintName: "FK36882F431DA5B73A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "VALUE", referencesUniqueColumn: "false")
  }

}
