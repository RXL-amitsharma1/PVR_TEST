databaseChangeLog = {
    changeSet(author: "forxsv (generated)", id: "1520079429070-1") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table not exists") {
            tableExists(tableName: "CASE_DATA_QUALITY")
        }
        sql("drop table CASE_DATA_QUALITY cascade constraints")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-2") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table not exists") {
            tableExists(tableName: "QUALITY_CASE_META_DATA")
        }
        sql("drop table QUALITY_CASE_META_DATA cascade constraints")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-3") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table not exists") {
            tableExists(tableName: "CAPA_8D")
        }
        sql("drop table CAPA_8D cascade constraints")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-4") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table not exists") {
            tableExists(tableName: "capa_8d_action_item")
        }
        sql("drop table capa_8d_action_item cascade constraints")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-5") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table not exists") {
            tableExists(tableName: "capa_8d_pvuser")
        }
        sql("drop table capa_8d_pvuser cascade constraints")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-6") {
        createTable(tableName: "CASE_DATA_QUALITY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CASE_DATA_QUAPK")
            }
            column(name: "CASE_NUMBER", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ERROR_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATA_TYPE", type: "varchar2(255 char)")

            column(name: "CREATED_PVUSER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "VALUE", type: "blob") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-8") {
        addForeignKeyConstraint(baseColumnNames: "CREATED_PVUSER_ID", baseTableName: "CASE_DATA_QUALITY", constraintName: "FK_836a1mvnek04cccs1kc93t8hi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-9") {
        createTable(tableName: "QUALITY_CASE_META_DATA") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUALITY_CASE_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "DATA_TYPE", type: "varchar2(255 char)")

            column(name: "action_item_id", type: "number(19,0)")

            column(name: "CASE_NUMBER", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "CASE_VERSION", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "QCOMMENT", type: "long")

            column(name: "justrication", type: "varchar2(4000 char)")

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "ERROR_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_IGNORED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-10") {
        addForeignKeyConstraint(baseColumnNames: "action_item_id", baseTableName: "QUALITY_CASE_META_DATA", constraintName: "FK_5cvp3u42mvclp0mnr8a0em139", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-11") {
        createTable(tableName: "capa_8d") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "capa_8dPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "approved_by_id", type: "number(19,0)")

            column(name: "capa_number", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "category", type: "varchar2(255 char)")

            column(name: "comments", type: "varchar2(2000 char)")

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "varchar2(2000 char)")

            column(name: "initiator_id", type: "number(19,0)")

            column(name: "is_deleted", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "issue_type", type: "varchar2(255 char)")

            column(name: "last_status_changed", type: "timestamp")

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "remarks", type: "raw(255)")

            column(name: "root_cause", type: "varchar2(2000 char)")

            column(name: "team_lead_id", type: "number(19,0)")

            column(name: "verification_results", type: "varchar2(2000 char)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-12") {
        createTable(tableName: "capa_8d_action_item") {
            column(name: "capa8d_corrective_actions_id", type: "number(19,0)")

            column(name: "action_item_id", type: "number(19,0)")

            column(name: "capa8d_preventive_actions_id", type: "number(19,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-13") {
        createTable(tableName: "capa_8d_pvuser") {
            column(name: "capa8d_team_members_id", type: "number(19,0)")

            column(name: "user_id", type: "number(19,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-14") {
        addForeignKeyConstraint(baseColumnNames: "approved_by_id", baseTableName: "capa_8d", constraintName: "FK_mjypte89rrc4686q334k5ldso", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-15") {
        addForeignKeyConstraint(baseColumnNames: "initiator_id", baseTableName: "capa_8d", constraintName: "FK_tcbehequsrhibqhak32twiq3e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-16") {
        addForeignKeyConstraint(baseColumnNames: "team_lead_id", baseTableName: "capa_8d", constraintName: "FK_v3n135sy0vktq5vtl4bhcrit", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-17") {
        addForeignKeyConstraint(baseColumnNames: "action_item_id", baseTableName: "capa_8d_action_item", constraintName: "FK_1ev17eo1g95qx41ld2houhk1h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-18") {
        addForeignKeyConstraint(baseColumnNames: "capa8d_corrective_actions_id", baseTableName: "capa_8d_action_item", constraintName: "FK_4aot1qcigrder2ekjrivgqhc6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "capa_8d", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-19") {
        addForeignKeyConstraint(baseColumnNames: "capa8d_preventive_actions_id", baseTableName: "capa_8d_action_item", constraintName: "FK_nmk6x1g9u9e9qc2gi3tfevekr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "capa_8d", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-20") {
        addForeignKeyConstraint(baseColumnNames: "capa8d_team_members_id", baseTableName: "capa_8d_pvuser", constraintName: "FK_y359x7a5t0f7f2yvg308vk0v", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "capa_8d", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1520079429070-21") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "capa_8d_pvuser", constraintName: "FK_85rypg0749biyau88gp8erqi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

}
