import com.rxlogix.config.WorkflowState
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup

databaseChangeLog = {


    changeSet(author: "sergey khovrachev (generated)", id: "20200511120201-1") {
        addColumn(tableName: "CONFIGURATION_ATTACH") {
            column(name: "PATH", type: "varchar2(4000 char)")
        }
        addColumn(tableName: "CONFIGURATION_ATTACH") {
            column(name: "SOURCE", type: "varchar2(255 char)")
        }
        addColumn(tableName: "CONFIGURATION_ATTACH") {
            column(name: "TYPE", type: "varchar2(4000 char)")
        }
        addColumn(tableName: "CONFIGURATION_ATTACH") {
            column(name: "OD_FOLDER_NAME", type: "varchar2(255 char)")
        }
        addColumn(tableName: "CONFIGURATION_ATTACH") {
            column(name: "OD_FOLDER_ID", type: "varchar2(255 char)")
        }
        addColumn(tableName: "CONFIGURATION_ATTACH") {
            column(name: "OD_SITE_ID", type: "varchar2(255 char)")
        }
        addColumn(tableName: "CONFIGURATION_ATTACH") {
            column(name: "OD_SETTINGS_ID", type: "NUMBER")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "20200511120200-2") {
        addColumn(tableName: "EX_CONFIGURATION_ATTACH") {
            column(name: "PATH", type: "varchar2(4000 char)")
        }
        addColumn(tableName: "EX_CONFIGURATION_ATTACH") {
            column(name: "SOURCE", type: "varchar2(255 char)")
        }
        addColumn(tableName: "EX_CONFIGURATION_ATTACH") {
            column(name: "TYPE", type: "varchar2(4000 char)")
        }
        addColumn(tableName: "EX_CONFIGURATION_ATTACH") {
            column(name: "OD_FOLDER_NAME", type: "varchar2(255 char)")
        }
        addColumn(tableName: "EX_CONFIGURATION_ATTACH") {
            column(name: "OD_FOLDER_ID", type: "varchar2(255 char)")
        }
        addColumn(tableName: "EX_CONFIGURATION_ATTACH") {
            column(name: "OD_SITE_ID", type: "varchar2(255 char)")
        }
        addColumn(tableName: "EX_CONFIGURATION_ATTACH") {
            column(name: "OD_SETTINGS_ID", type: "NUMBER")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "20200522100600-1") {
        createTable(tableName: "GANTT") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "GANTTPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "IS_TEMPLATE", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "AI_DURATION", type: "number(19,0)")

            column(name: "SUBMISSION_DURATION", type: "number(19,0)")

            column(name: "EX_CONFIG_ID", type: "number(19,0)")

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
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
    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-3") {
        addForeignKeyConstraint(baseColumnNames: "EX_CONFIG_ID", baseTableName: "GANTT", constraintName: "FK_dsjkbsadqwjdbcdAakf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }


    changeSet(author: "sergey khovrachev (generated)", id: "20200522100603-2") {
        createTable(tableName: "GANTT_ITEM") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "GANTTITEMPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "sort_index", type: "number(19,0)")

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DURATION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "COMPLETE", type: "number(19,0)")

            column(name: "BINDING_ID", type: "number(19,0)")

            column(name: "TYPE", type: "varchar2(255 char)")
            column(name: "CONDITION_TYPE", type: "varchar2(255 char)")

            column(name: "GANTT_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ASSIGNED_ID", type: "number(19,0)")

            column(name: "CONDITION", type: "varchar2(4000 char)")

            column(name: "START_DATE", type: "timestamp")

            column(name: "END_DATE", type: "timestamp")

        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100603-4") {
        addForeignKeyConstraint(baseColumnNames: "GANTT_ID", baseTableName: "GANTT_ITEM", constraintName: "FK_GANTT_ID_GANTT_ITEM", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "GANTT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100603-5") {
        addForeignKeyConstraint(baseColumnNames: "ASSIGNED_ID", baseTableName: "GANTT_ITEM", constraintName: "FK_GANTT_ID_ASSIGNED_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-6") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "GANTT_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-7") {
        addForeignKeyConstraint(baseColumnNames: "GANTT_ID", baseTableName: "EX_RCONFIG", constraintName: "FK_GANTT_ID_EX_RCONFIG", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "GANTT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-8") {
        addColumn(tableName: "RCONFIG") {
            column(name: "GANTT_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-9") {
        addForeignKeyConstraint(baseColumnNames: "GANTT_ID", baseTableName: "RCONFIG", constraintName: "FK_GANTT_ID_RCONFIG", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "GANTT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-10") {
        addColumn(tableName: "GANTT_ITEM") {
            column(name: "ASSIGNED_GROUP_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-11") {
        addForeignKeyConstraint(baseColumnNames: "ASSIGNED_GROUP_ID", baseTableName: "GANTT_ITEM", constraintName: "FK_AS_GR_ID_GANTT_ITEM", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-12") {
        addColumn(tableName: "GANTT_ITEM") {
            column(name: "PARENT", type: "varchar2(255 char)")
            column(name: "DEPEND", type: "varchar2(255 char)")
            column(name: "UUID", type: "varchar2(255 char)")
        }
    }
    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-14") {
        addColumn(tableName: "ACTION_ITEM") {
            column(name: "DEPEND", type: "varchar2(255 char)")
            column(name: "START_DATE", type: "timestamp")
        }
    }
    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-15") {
        addColumn(tableName: "ACTION_ITEM") {
            column(name: "UUID", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-16") {
        addColumn(tableName: "EX_CONFIGURATION_ATTACH") {
            column(name: "SCRIPT", type: "clob")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-17") {
        addColumn(tableName: "CONFIGURATION_ATTACH") {
            column(name: "SCRIPT", type: "clob")
        }
    }

    changeSet(author: "sergey khovrachev   (generated)", id: "20200522100600-18") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "configuration_id", tableName: "EX_CONFIGURATION_ATTACH")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-19") {
        addColumn(tableName: "PUBLISHER_CFG_SECT") {
            column(name: "pending_variable", type: "varchar2(255 char)")
            column(name: "pending_manual", type: "varchar2(255 char)")
            column(name: "pending_comment", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-20") {
        addColumn(tableName: "PUBLISHER_EX_TMPLT") {
            column(name: "last_updated", type: "timestamp")
            column(name: "created_by", type: "varchar2(255 char)")
            column(name: "modified_by", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "20200522100600-21") {
        addColumn(tableName: "ACTION_ITEM") {
            column(name: "PUBLISHER_REPORT_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "20200522100600-22") {
        addForeignKeyConstraint(baseColumnNames: "PUBLISHER_REPORT_ID", baseTableName: "ACTION_ITEM", constraintName: "FK_PUBLISHER_REPORT_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PUBLISHER_REPORT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "20200522100600-23") {
        addColumn(tableName: "PUBLISHER_REPORT") {
            column(name: "due_date", type: "timestamp")
            column(name: "lock_code", type: "varchar2(1050 char)")
            column(name: "destination", type: "varchar2(255 char)")
            column(name: "reviewer_id", type: "number(19,0)")
            column(name: "approver_id", type: "number(19,0)")
            column(name: "author_id", type: "number(19,0)")
            column(name: "LOCKED_USER_ID", type: "number(19,0)")
            column(name: "assigned_to_group_id", type: "number(19,0)")
            column(name: "workflow_state_id", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "20200522100600-24") {
        addForeignKeyConstraint(baseColumnNames: "reviewer_id", baseTableName: "PUBLISHER_REPORT", constraintName: "FK_reviewer_id", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "approver_id", baseTableName: "PUBLISHER_REPORT", constraintName: "FK_approver_id", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "author_id", baseTableName: "PUBLISHER_REPORT", constraintName: "FK_author_id", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "LOCKED_USER_ID", baseTableName: "PUBLISHER_REPORT", constraintName: "FK_LOCKED_USER_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "PUBLISHER_REPORT", constraintName: "FK_assigned_to_group_id", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "PUBLISHER_REPORT", constraintName: "FK_workflow_state_id", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-25") {
        addColumn(tableName: "WORKFLOW_JUSTIFICATION") {
            column(name: "PUBLISHER_REPORT_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev  (generated)", id: "20200522100600-26") {
        addForeignKeyConstraint(baseColumnNames: "PUBLISHER_REPORT_ID", baseTableName: "WORKFLOW_JUSTIFICATION", constraintName: "FK_wj_PUBLISHER_REPORT_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PUBLISHER_REPORT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "20200522100600-27") {
        addColumn(tableName: "COMMENT_TABLE") {
            column(name: "PUBLISHER_REPORT_ID", type: "number(19,0)")
        }
    }
    changeSet(author: "sergey khovrachev (generated)", id: "20200522100600-28") {
        addForeignKeyConstraint(baseColumnNames: "PUBLISHER_REPORT_ID", baseTableName: "COMMENT_TABLE", constraintName: "FK_ct_PUBLISHER_REPORT_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PUBLISHER_REPORT", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "20200522100600-29") {
        addColumn(tableName: "GANTT") {
            column(name: "default_Section_Duration", type: "number(19,0)")
            column(name: "default_Full_Duration", type: "number(19,0)")
            column(name: "default_Report_Duration", type: "number(19,0)")
        }
    }

    changeSet(author: "sergey khovrachev (generated)", id: "20200522100600-30") {
        addColumn(tableName: "GANTT_ITEM") {
            column(name: "template", type: "number(1,0)")
        }
    }
    changeSet(author: "sergey khovrachev (generated))", id: "20200522100600-33") {
        dropForeignKeyConstraint(baseTableName: "PUBLISHER_CFG_SECT", constraintName: "FK_s5mvc8v4rhsn1smxbcujnwl4x")
    }
    changeSet(author: "sergey khovrachev (generated))", id: "20200522100600-31") {
        dropColumn(columnName: "STATUS", tableName: "PUBLISHER_REPORT")
        dropColumn(columnName: "STATE", tableName: "PUBLISHER_REPORT")
    }

    changeSet(author: "sergey khovrachev (generated))", id: "20200522100600-32") {
        dropColumn(columnName: "draft_workflow_state_id", tableName: "PUBLISHER_CFG_SECT")
    }

    changeSet(author: "sergey khovrachev (generated)", id: "20201027100600-1") {
        createTable(tableName: "publisher_cmn_prm") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "publisher_cmn_prmPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
          column(name: "DESCRIPTION", type: "varchar2(4000 char)")
          column(name: "VALUE", type: "varchar2(4000 char)")

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }



            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
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
}







