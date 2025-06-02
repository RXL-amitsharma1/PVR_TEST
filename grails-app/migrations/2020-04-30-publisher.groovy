databaseChangeLog = {

    changeSet(author: "sergey khovrachev (generated)", id: "1556174806980-1") {
        createTable(tableName: "CONFIGURATION_ATTACH") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CONFIGURATIONPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATA", type: "long raw")

            column(name: "DATE_CREATED", type: "timestamp") {
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

            column(name: "configuration_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ext", type: "varchar2(255 char)")
            column(name: "user_group_id", type: "number(19,0)")
            column(name: "sort_number", type: "number(10,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1556174806980-2") {
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "user_group_id", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1556174806980-3") {
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "user_group_id", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1560935501123-155") {
        addForeignKeyConstraint(baseColumnNames: "user_group_id", baseTableName: "CONFIGURATION_ATTACH", constraintName: "FK_dwfewrtgdcajkuiluil", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }


    changeSet(author: "sergey khovrachev (generated)", id: "1556174806980-156") {
        addForeignKeyConstraint(baseColumnNames: "user_group_id", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK_cr9r6g67gjeth2920v2l6yk2g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "1556174806980-165") {
        addForeignKeyConstraint(baseColumnNames: "user_group_id", baseTableName: "TEMPLT_QUERY", constraintName: "FK_8nmopt1e7m0bny5hfoybj8ykv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "1556174806980-154") {
        addForeignKeyConstraint(baseColumnNames: "configuration_id", baseTableName: "CONFIGURATION_ATTACH", constraintName: "FK_5cmnwyccvvtcex3hjbvn3u2tc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "1556193328139-1") {
        createTable(tableName: "EX_CONFIGURATION_ATTACH") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CONFIGURATPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "configuration_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATA", type: "long raw")

            column(name: "DATE_CREATED", type: "timestamp") {
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

            column(name: "ext", type: "varchar2(255 char)")
            column(name: "user_group_id", type: "number(19,0)")
            column(name: "sort_number", type: "number(10,0)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "sergey khovrachev  (generated)", id: "1556193328139-152") {
        addForeignKeyConstraint(baseColumnNames: "configuration_id", baseTableName: "EX_CONFIGURATION_ATTACH", constraintName: "FK_5u2sa6cypktxx1i9p4nwjwoeh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }
    changeSet(author: "sergey khovrachev (generated)", id: "1560935501123-1551") {
        addForeignKeyConstraint(baseColumnNames: "user_group_id", baseTableName: "EX_CONFIGURATION_ATTACH", constraintName: "FK_dur36jyvvv475h8rf2ctr3fov", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "15585280591233-1") {
        createTable(tableName: "publisher_tpl") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "publisher_tplPK")
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

            column(name: "description", type: "varchar2(4000 char)")

            column(name: "is_deleted", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "template", type: "long raw") {
                constraints(nullable: "false")
            }

            column(name: "file_name", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "155852805128-2") {
        createTable(tableName: "publisher_tpl_prm") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "publisher_tpl_prm_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "varchar2(4000 char)")

            column(name: "name", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "template_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "title", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "varchar2(4000 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1558528051238-157") {
        addForeignKeyConstraint(baseColumnNames: "template_id", baseTableName: "publisher_tpl_prm", constraintName: "FK_n896sduyg6cuskw34yimye9mt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "publisher_tpl", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1560161632641-1") {
        createTable(tableName: "PUBLISHER_EX_TMPLT") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PUBLISHER_EX_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATA", type: "long raw")

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "num_of_execution", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "report_status", type: "varchar2(255 char)")
            column(name: "publisher_cfg_sec_id", type: "number(19,0)")
            column(name: "EXECUTION_STATUS", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "1561114815307-1") {
        createTable(tableName: "PUBLISHER_REPORT") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PUBLISHER_REPPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATA", type: "long raw")

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "ex_config_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "num_of_execution", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "COMMNT", type: "varchar2(4000 char)")
            column(name: "is_deleted", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "owner_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "state", type: "varchar2(255 char)")
            column(name: "published", type: "number(1,0)")
            column(name: "MODIFIED_BY", type: "varchar2(255 char)")
            column(name: "CREATED_BY", type: "varchar2(255 char)")
            column(name: "LAST_UPDATED", type: "timestamp")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "1561114815307-157") {
        addForeignKeyConstraint(baseColumnNames: "ex_config_id", baseTableName: "PUBLISHER_REPORT", constraintName: "FK_ikaaf7ckm9yk1fbn5e4wmo438", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }


    changeSet(author: "sergey khovrachev (generated)", id: "1561121891275-162") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "PUBLISHER_REPORT", constraintName: "FK_oei24q4lm9ghe77hdx141v15g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }
    changeSet(author: "sergey khovrachev (generated)", id: "1564651591436-1") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "IS_PUBLISHER_REPORT", type: "number(1,0)")
        }
        sql("update EX_RCONFIG set IS_PUBLISHER_REPORT = 0;")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "IS_PUBLISHER_REPORT")
    }


    changeSet(author: "sergey khovrachev (generated)", id: "1564651591436-2") {
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_PUBLISHER_REPORT", type: "number(1,0)")
        }
        sql("update RCONFIG set IS_PUBLISHER_REPORT = 0;")
        addNotNullConstraint(tableName: "RCONFIG", columnName: "IS_PUBLISHER_REPORT")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-1") {
        createTable(tableName: "PUBLISHER_CFG_SECT") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PUBLISHER_CFGPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_group_id", type: "number(19,0)")

            column(name: "configuration_id", type: "number(19,0)")

            column(name: "CREATED_BY", type: "varchar2(255 char)")

            column(name: "DATE_CREATED", type: "timestamp")

            column(name: "destination", type: "varchar2(255 char)")

            column(name: "due_date", type: "timestamp")

            column(name: "executed_configuration_id", type: "number(19,0)")

            column(name: "filename", type: "varchar2(255 char)")

            column(name: "LAST_UPDATED", type: "timestamp")

            column(name: "LOCKED_USER_ID", type: "number(19,0)")

            column(name: "MODIFIED_BY", type: "varchar2(255 char)")

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "publisher_template_id", type: "number(19,0)")

            column(name: "sort_number", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "template_file_data", type: "long raw")
            column(name: "draft_workflow_state_id", type: "number(19,0)")
            column(name: "final_workflow_state_id", type: "number(19,0)")
            column(name: "reviewer_id", type: "number(19,0)")
            column(name: "approver_id", type: "number(19,0)")
            column(name: "author_id", type: "number(19,0)")
            column(name: "due_in_days", type: "number(10,0)")
            column(name: "lock_code", type: "varchar2(1050 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1569503978767-2") {
        createTable(tableName: "publisher_cfg_sect_param_val") {
            column(name: "PUBLISHER_CFG_SECT_ID", type: "number(19,0)")
            column(name: "PARAMETER_VALUES_STRING", type: "varchar2(255 char)")
            column(name: "PARAMETER_VALUES_IDX", type: "varchar2(255 char)")
            column(name: "PARAMETER_VALUES_ELT", type: "clob")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-3") {
        addColumn(tableName: "ACTION_ITEM") {
            column(name: "PUBLISHER_SECTION_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-4") {
        addColumn(tableName: "COMMENT_TABLE") {
            column(name: "PUBLISHER_SECTION_ID", type: "number(19,0)")
        }
    }
    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-173") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_ikdhik5cy283cqwgct30vcgbf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-174") {
        addForeignKeyConstraint(baseColumnNames: "configuration_id", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_7gl0ykbilaknds6ym12eyrfe6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-175") {
        addForeignKeyConstraint(baseColumnNames: "executed_configuration_id", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_joud8m2f943708er7lk5m44me", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-176") {
        addForeignKeyConstraint(baseColumnNames: "LOCKED_USER_ID", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_cr856ou97qe11sqc4qdylo9tt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-177") {
        addForeignKeyConstraint(baseColumnNames: "publisher_template_id", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_gihvpwnbg9vm6faiy95y0e6hj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "publisher_tpl", referencesUniqueColumn: "false")
    }


    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-179") {
        addForeignKeyConstraint(baseColumnNames: "publisher_cfg_sec_id", baseTableName: "PUBLISHER_EX_TMPLT", constraintName: "FK_gayna1x4l849ycjjj9keqlvwv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PUBLISHER_CFG_SECT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-170") {
        addForeignKeyConstraint(baseColumnNames: "PUBLISHER_SECTION_ID", baseTableName: "COMMENT_TABLE", constraintName: "FK_afie4ecsffxa6db3nutq0bx3p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PUBLISHER_CFG_SECT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1569503976543-168") {
        addForeignKeyConstraint(baseColumnNames: "PUBLISHER_SECTION_ID", baseTableName: "ACTION_ITEM", constraintName: "FK_qejf4fwi5new3719l80t3hw7v", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PUBLISHER_CFG_SECT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "1570775758993-1") {
        addColumn(tableName: "WORKFLOW_JUSTIFICATION") {
            column(name: "PUBLISHER_SECTION", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "1570775758993-182") {
        addForeignKeyConstraint(baseColumnNames: "PUBLISHER_SECTION", baseTableName: "WORKFLOW_JUSTIFICATION", constraintName: "FK_mj9lpuoqcd3xo91v8cvxpigjq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PUBLISHER_CFG_SECT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571045540259-174") {
        addForeignKeyConstraint(baseColumnNames: "draft_workflow_state_id", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_s5mvc8v4rhsn1smxbcujnwl4x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571045540259-175") {
        addForeignKeyConstraint(baseColumnNames: "final_workflow_state_id", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_a73lh2h2j14csruqb53u16q66", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
    }


    changeSet(author: "sergey khovrachev (generated)", id: "1571045540259-178") {
        addForeignKeyConstraint(baseColumnNames: "reviewer_id", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_sdfndfjnjfhgkwwe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571045540259-191") {
        addForeignKeyConstraint(baseColumnNames: "approver_id", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_sdfnewrwfrhgkwwe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571045540259-179") {
        addForeignKeyConstraint(baseColumnNames: "author_id", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_asq232e4frhljksm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }


    changeSet(author: "sergey (generated)", id: "1579506117765-2") {
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "DUE_DAYS", type: "number(10,0)")
        }
    }

    changeSet(author: "sergey (generated)", id: "1579506117765-3") {
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "DUE_DAYS", type: "number(10,0)")
        }
    }
    changeSet(author: "sergey (generated)", id: "1579506117765-4") {
        sql("update TEMPLT_QUERY tq set tq.DUE_DAYS = (select icsr.DUE_DAYS from ICSR_TEMPLT_QUERY icsr where icsr.id=tq.id ) ")
        dropColumn(columnName: "DUE_DAYS", tableName: "ICSR_TEMPLT_QUERY")
    }

    changeSet(author: "sergey (generated)", id: "1580468591981-2") {
        createTable(tableName: "publisher_log") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "publisher_logPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "LOG", type: "clob")

            column(name: "publisher_ex_tpl_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "sergey (generated)", id: "1580468591981-187") {
        createIndex(indexName: "pb_ex_tpl_id_u_1580468576163", tableName: "publisher_log", unique: "true") {
            column(name: "publisher_ex_tpl_id")
        }
    }
    changeSet(author: "sergey (generated)", id: "1580468591981-157") {
        addForeignKeyConstraint(baseColumnNames: "publisher_ex_tpl_id", baseTableName: "publisher_log", constraintName: "FKWdeEfFRtyyhyhy0x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PUBLISHER_EX_TMPLT", referencesUniqueColumn: "false")
    }
//----------one Drive----------------------------


    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-1") {
        createTable(tableName: "CASE_DELIVERIES_OD_FORMATS") {
            column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "OD_FORMAT", type: "varchar2(255 char)")

            column(name: "OD_FORMAT_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-2") {
        createTable(tableName: "DELIVERIES_OD_FORMATS") {
            column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "OD_FORMAT", type: "varchar2(255 char)")

            column(name: "OD_FORMAT_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-3") {
        createTable(tableName: "EX_CASE_DELIVERIES_OD_FORMATS") {
            column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "OD_FORMAT", type: "varchar2(255 char)")

            column(name: "OD_FORMAT_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-4") {
        createTable(tableName: "EX_DELIVERIES_OD_FORMATS") {
            column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "OD_FORMAT", type: "varchar2(255 char)")

            column(name: "OD_FORMAT_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-5") {
        createTable(tableName: "one_drive_user_settings") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "one_drive_usePK")
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

            column(name: "is_deleted", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "last_refresh", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "refresh_token", type: "varchar2(4000 char)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-6") {
        addColumn(tableName: "CASE_DELIVERY") {
            column(name: "one_drive_folder_id", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-6-2") {
        addColumn(tableName: "CASE_DELIVERY") {
            column(name: "one_drive_site_id", type: "varchar2(1000 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-7") {
        addColumn(tableName: "CASE_DELIVERY") {
            column(name: "one_drive_folder_name", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-8") {
        addColumn(tableName: "CASE_DELIVERY") {
            column(name: "one_drive_user_settings_id", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-9") {
        addColumn(tableName: "DELIVERY") {
            column(name: "one_drive_folder_id", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-9-2") {
        addColumn(tableName: "DELIVERY") {
            column(name: "one_drive_site_id", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-10") {
        addColumn(tableName: "DELIVERY") {
            column(name: "one_drive_folder_name", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-11") {
        addColumn(tableName: "DELIVERY") {
            column(name: "one_drive_user_settings_id", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-12") {
        addColumn(tableName: "EX_CASE_DELIVERY") {
            column(name: "one_drive_folder_id", type: "varchar2(255 char)")
        }
    }
    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-12-2") {
        addColumn(tableName: "EX_CASE_DELIVERY") {
            column(name: "one_drive_site_id", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-13") {
        addColumn(tableName: "EX_CASE_DELIVERY") {
            column(name: "one_drive_folder_name", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-14") {
        addColumn(tableName: "EX_CASE_DELIVERY") {
            column(name: "one_drive_user_settings_id", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-15") {
        addColumn(tableName: "EX_DELIVERY") {
            column(name: "one_drive_folder_id", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-15-2") {
        addColumn(tableName: "EX_DELIVERY") {
            column(name: "one_drive_site_id", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-16") {
        addColumn(tableName: "EX_DELIVERY") {
            column(name: "one_drive_folder_name", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-17") {
        addColumn(tableName: "EX_DELIVERY") {
            column(name: "one_drive_user_settings_id", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-186") {
        addForeignKeyConstraint(baseColumnNames: "one_drive_user_settings_id", baseTableName: "CASE_DELIVERY", constraintName: "FK_7626b0qh6u12urfbmvuhl8j35", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "one_drive_user_settings", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-188") {
        addForeignKeyConstraint(baseColumnNames: "one_drive_user_settings_id", baseTableName: "DELIVERY", constraintName: "FK_1v57gsojkmoge7ni7xkuqtpe9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "one_drive_user_settings", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-189") {
        addForeignKeyConstraint(baseColumnNames: "one_drive_user_settings_id", baseTableName: "EX_CASE_DELIVERY", constraintName: "FK_r1baaxyjh69puca1dgqwyet4i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "one_drive_user_settings", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-190") {
        addForeignKeyConstraint(baseColumnNames: "one_drive_user_settings_id", baseTableName: "EX_DELIVERY", constraintName: "FK_g0xb6i87p1fledp1ytd2hkgdi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "one_drive_user_settings", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1571737260211-193") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "one_drive_user_settings", constraintName: "FK_s7hed86m7jxtsgeq2ljyb2n9w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "1571816806763-1") {
        addColumn(tableName: "one_drive_user_settings") {
            column(name: "access_token", type: "varchar2(4000 char)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "sergey khovrachev  (generated)", id: "1571816806763-206") {
        createIndex(indexName: "user_id_uniq_1571816798643", tableName: "one_drive_user_settings", unique: "true") {
            column(name: "user_id")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1556902193915-1") {
        addColumn(tableName: "RPT_SUBMISSION") {
            column(name: "late", type: "varchar2(255 char)")
        }
    }


    changeSet(author: "sergey khovrachev (generated)", id: "1556784120603-1") {
        createTable(tableName: "SUBMISSION_ATTACH") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SUBMISSION_ATPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATA", type: "long raw")

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "ext", type: "varchar2(255 char)")

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "submission_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "1556784120603-162") {
        addForeignKeyConstraint(baseColumnNames: "submission_id", baseTableName: "SUBMISSION_ATTACH", constraintName: "FK_6btp2wi1ce3tlkyhvymiriawp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_SUBMISSION", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1557909387614-1") {
        createTable(tableName: "report_submission_late_reason") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "report_submisPK")
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

            column(name: "is_primary", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "reason", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "report_submission_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "responsible", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1557909387614-159") {
        addForeignKeyConstraint(baseColumnNames: "report_submission_id", baseTableName: "report_submission_late_reason", constraintName: "FK_9cgsvelnk47jaxjw7nkruly2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_SUBMISSION", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1556634616505-1") {
        addColumn(tableName: "capa_8d") {
            column(name: "configuration_id", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1556634616505-2") {
        addColumn(tableName: "capa_8d") {
            column(name: "submission_id", type: "number(19,0)")
        }
    }
    changeSet(author: "sergey khovrachev (generated)", id: "1556634616505-153") {
        addForeignKeyConstraint(baseColumnNames: "configuration_id", baseTableName: "capa_8d", constraintName: "FK_do0rdlvsuouyyb2rg0yh05ao4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "1556634616505-154") {
        addForeignKeyConstraint(baseColumnNames: "submission_id", baseTableName: "capa_8d", constraintName: "FK_82dce1gvk6f27mmwccs1sj6mn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_SUBMISSION", referencesUniqueColumn: "false")
    }

//    changeSet(author: "sergey khovrachev  (generated)", id: "202006101000-3") {
//        addColumn(tableName: "EX_RCONFIG") {
//            column(name: "INN", type: "varchar2(255 char)")
//            column(name: "drug_code", type: "varchar2(255 char)")
//        }
//    }
//
//    changeSet(author: "sergey khovrachev  (generated)", id: "202006101000-4") {
//        addColumn(tableName: "RCONFIG") {
//            column(name: "INN", type: "varchar2(255 char)")
//            column(name: "drug_code", type: "varchar2(255 char)")
//        }
//    }

}







