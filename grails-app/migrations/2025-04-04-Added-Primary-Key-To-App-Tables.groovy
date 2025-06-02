databaseChangeLog = {

    changeSet(author: "rxl-shivamg1", id: "202504041215-1") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "ALLOWED_ATTACHMENTS")
            and {
                columnExists(tableName: "ALLOWED_ATTACHMENTS", columnName: "UNITI_CONFIG_ID")
                columnExists(tableName: "ALLOWED_ATTACHMENTS", columnName: "ATTACHMENT_ID")
            }
            not {
                primaryKeyExists(tableName: "ALLOWED_ATTACHMENTS")
            }
        }
        sql("DELETE FROM ALLOWED_ATTACHMENTS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY UNITI_CONFIG_ID, ATTACHMENT_ID ORDER BY ROWID) AS row_num " +
            "FROM ALLOWED_ATTACHMENTS) WHERE row_num > 1)")
        addPrimaryKey(columnNames: "UNITI_CONFIG_ID, ATTACHMENT_ID", constraintName: "ALLOWED_ATTACHMENTS_PK", tableName: "ALLOWED_ATTACHMENTS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504041215-2") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "AUDIT_LOG_BK")
            and {
                columnExists(tableName: "AUDIT_LOG_BK", columnName: "ID")
            }
            not {
                primaryKeyExists(tableName: "AUDIT_LOG_BK")
            }
        }
        addPrimaryKey(columnNames: "ID", constraintName: "AUDIT_LOG_BK_PK", tableName: "AUDIT_LOG_BK")
    }

    changeSet(author: "rxl-shivamg1", id: "202504041215-3") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "AUDIT_LOG_FIELD_CHANGE_BK")
            and {
                columnExists(tableName: "AUDIT_LOG_FIELD_CHANGE_BK", columnName: "ID")
            }
            not {
                primaryKeyExists(tableName: "AUDIT_LOG_FIELD_CHANGE_BK")
            }
        }
        addPrimaryKey(columnNames: "ID", constraintName: "AUDIT_LOG_FIELD_CHANGE_BK_PK", tableName: "AUDIT_LOG_FIELD_CHANGE_BK")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-4") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "AUTO_ROD_PVUSER")
            and {
                columnExists(tableName: "AUTO_ROD_PVUSER", columnName: "AUTO_REASON_OF_DELAY_ID")
                columnExists(tableName: "AUTO_ROD_PVUSER", columnName: "EMAIL_USER")
            }
            not {
                primaryKeyExists(tableName: "AUTO_ROD_PVUSER")
            }
        }
        sql("DELETE FROM AUTO_ROD_PVUSER WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY AUTO_REASON_OF_DELAY_ID, EMAIL_USER ORDER BY ROWID) AS row_num " +
            "FROM AUTO_ROD_PVUSER) WHERE row_num > 1)")
        sql("DELETE FROM AUTO_ROD_PVUSER WHERE AUTO_REASON_OF_DELAY_ID IS NULL OR EMAIL_USER IS NULL")
        addNotNullConstraint(tableName: "AUTO_ROD_PVUSER", columnName: "AUTO_REASON_OF_DELAY_ID")
        addNotNullConstraint(tableName: "AUTO_ROD_PVUSER", columnName: "EMAIL_USER")
        addPrimaryKey(columnNames: "AUTO_REASON_OF_DELAY_ID, EMAIL_USER", constraintName: "AUTO_ROD_PVUSER_PK", tableName: "AUTO_ROD_PVUSER")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-5") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CAPA8D_SHARED_WITHS")
            and {
                columnExists(tableName: "CAPA8D_SHARED_WITHS", columnName: "CAPA_8D_ID")
                columnExists(tableName: "CAPA8D_SHARED_WITHS", columnName: "SHARED_WITH_ID")
            }
            not {
                primaryKeyExists(tableName: "CAPA8D_SHARED_WITHS")
            }
        }
        sql("DELETE FROM CAPA8D_SHARED_WITHS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY CAPA_8D_ID, SHARED_WITH_ID ORDER BY ROWID) AS row_num " +
            "FROM CAPA8D_SHARED_WITHS) WHERE row_num > 1)")
        sql("DELETE FROM CAPA8D_SHARED_WITHS WHERE SHARED_WITH_ID IS NULL")
        addNotNullConstraint(tableName: "CAPA8D_SHARED_WITHS", columnName: "SHARED_WITH_ID")
        addPrimaryKey(columnNames: "CAPA_8D_ID, SHARED_WITH_ID", constraintName: "CAPA8D_SHARED_W_PK", tableName: "CAPA8D_SHARED_WITHS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-6") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CAPA8D_SHARED_W_GRPS")
            and {
                columnExists(tableName: "CAPA8D_SHARED_W_GRPS", columnName: "CAPA_8D_ID")
                columnExists(tableName: "CAPA8D_SHARED_W_GRPS", columnName: "SHARED_WITH_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "CAPA8D_SHARED_W_GRPS")
            }
        }
        sql("DELETE FROM CAPA8D_SHARED_W_GRPS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY CAPA_8D_ID, SHARED_WITH_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM CAPA8D_SHARED_W_GRPS) WHERE row_num > 1)")
        sql("DELETE FROM CAPA8D_SHARED_W_GRPS WHERE SHARED_WITH_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "CAPA8D_SHARED_W_GRPS", columnName: "SHARED_WITH_GROUP_ID")
        addPrimaryKey(columnNames: "CAPA_8D_ID, SHARED_WITH_GROUP_ID", constraintName: "CAPA8D_SHARED_W_GRPS_PK", tableName: "CAPA8D_SHARED_W_GRPS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-7") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CAPA_8D_ACTION_ITEM")
            and {
                columnExists(tableName: "CAPA_8D_ACTION_ITEM", columnName: "ACTION_ITEM_ID")
                columnExists(tableName: "CAPA_8D_ACTION_ITEM", columnName: "CAPA8D_CORRECTIVE_ACTIONS_ID")
            }
            not {
                or {
                    tableExists(tableName: "CAPA8D_CORRECTIVE")
                    tableExists(tableName: "CAPA8D_PREVENTIVE")
                    primaryKeyExists(tableName: "CAPA8D_CORRECTIVE")
                    primaryKeyExists(tableName: "CAPA8D_PREVENTIVE")
                }
            }
        }
        sql("CREATE TABLE CAPA8D_CORRECTIVE AS (SELECT ACTION_ITEM_ID AS CORRECTIVE_ACTION_ID, CAPA8D_CORRECTIVE_ACTIONS_ID AS CAPA8D_ID FROM CAPA_8D_ACTION_ITEM WHERE CAPA8D_CORRECTIVE_ACTIONS_ID IS NOT NULL)")
        sql("DELETE FROM CAPA8D_CORRECTIVE WHERE ROWID IN (" +
                "SELECT ROWID FROM (" +
                "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY CORRECTIVE_ACTION_ID, CAPA8D_ID ORDER BY ROWID) AS row_num " +
                "FROM CAPA8D_CORRECTIVE) WHERE row_num > 1)")
        sql("DELETE FROM CAPA8D_CORRECTIVE WHERE CORRECTIVE_ACTION_ID IS NULL OR CAPA8D_ID IS NULL")
        addNotNullConstraint(tableName: "CAPA8D_CORRECTIVE", columnName: "CORRECTIVE_ACTION_ID")
        addNotNullConstraint(tableName: "CAPA8D_CORRECTIVE", columnName: "CAPA8D_ID")
        
        sql("CREATE TABLE CAPA8D_PREVENTIVE AS (SELECT ACTION_ITEM_ID AS PREVENTIVE_ACTION_ID, CAPA8D_PREVENTIVE_ACTIONS_ID AS CAPA8D_ID FROM CAPA_8D_ACTION_ITEM WHERE CAPA8D_PREVENTIVE_ACTIONS_ID IS NOT NULL)")
        sql("DELETE FROM CAPA8D_PREVENTIVE WHERE ROWID IN (" +
                "SELECT ROWID FROM (" +
                "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY PREVENTIVE_ACTION_ID, CAPA8D_ID ORDER BY ROWID) AS row_num " +
                "FROM CAPA8D_PREVENTIVE) WHERE row_num > 1)")
        sql("DELETE FROM CAPA8D_PREVENTIVE WHERE PREVENTIVE_ACTION_ID IS NULL OR CAPA8D_ID IS NULL")
        addNotNullConstraint(tableName: "CAPA8D_PREVENTIVE", columnName: "PREVENTIVE_ACTION_ID")
        addNotNullConstraint(tableName: "CAPA8D_PREVENTIVE", columnName: "CAPA8D_ID")
        
        addPrimaryKey(columnNames: "CORRECTIVE_ACTION_ID, CAPA8D_ID", constraintName: "CAPA8D_CORRECTIVE_PK", tableName: "CAPA8D_CORRECTIVE")
        addPrimaryKey(columnNames: "PREVENTIVE_ACTION_ID, CAPA8D_ID", constraintName: "CAPA8D_PREVENTIVE_PK", tableName: "CAPA8D_PREVENTIVE")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-8") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CAPA_8D_PVUSER")
            and {
                columnExists(tableName: "CAPA_8D_PVUSER", columnName: "CAPA8D_TEAM_MEMBERS_ID")
                columnExists(tableName: "CAPA_8D_PVUSER", columnName: "USER_ID")
            }
            not {
                primaryKeyExists(tableName: "CAPA_8D_PVUSER")
            }
        }
        sql("DELETE FROM CAPA_8D_PVUSER WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY CAPA8D_TEAM_MEMBERS_ID, USER_ID ORDER BY ROWID) AS row_num " +
            "FROM CAPA_8D_PVUSER) WHERE row_num > 1)")
        sql("DELETE FROM CAPA_8D_PVUSER WHERE CAPA8D_TEAM_MEMBERS_ID IS NULL OR USER_ID IS NULL")
        addNotNullConstraint(tableName: "CAPA_8D_PVUSER", columnName: "CAPA8D_TEAM_MEMBERS_ID")
        addNotNullConstraint(tableName: "CAPA_8D_PVUSER", columnName: "USER_ID")
        addPrimaryKey(columnNames: "CAPA8D_TEAM_MEMBERS_ID, USER_ID", constraintName: "CAPA_8D_PVUSER_PK", tableName: "CAPA_8D_PVUSER")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-9") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CASE_DELIVERIES_EMAIL_USERS")
            and {
                columnExists(tableName: "CASE_DELIVERIES_EMAIL_USERS", columnName: "DELIVERY_ID")
                columnExists(tableName: "CASE_DELIVERIES_EMAIL_USERS", columnName: "EMAIL_USER")
            }
            not {
                primaryKeyExists(tableName: "CASE_DELIVERIES_EMAIL_USERS")
            }
        }
        sql("DELETE FROM CASE_DELIVERIES_EMAIL_USERS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DELIVERY_ID, EMAIL_USER ORDER BY ROWID) AS row_num " +
            "FROM CASE_DELIVERIES_EMAIL_USERS) WHERE row_num > 1)")
        sql("DELETE FROM CASE_DELIVERIES_EMAIL_USERS WHERE EMAIL_USER IS NULL")
        addNotNullConstraint(tableName: "CASE_DELIVERIES_EMAIL_USERS", columnName: "EMAIL_USER")
        addPrimaryKey(columnNames: "DELIVERY_ID, EMAIL_USER", constraintName: "CASE_DELVRS_EMAIL_USRS_PK", tableName: "CASE_DELIVERIES_EMAIL_USERS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-10") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CASE_DELIVERIES_OD_FORMATS")
            and {
                columnExists(tableName: "CASE_DELIVERIES_OD_FORMATS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "CASE_DELIVERIES_OD_FORMATS", columnName: "OD_FORMAT")
            }
            not {
                primaryKeyExists(tableName: "CASE_DELIVERIES_OD_FORMATS")
            }
        }
        sql("DELETE FROM CASE_DELIVERIES_OD_FORMATS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, OD_FORMAT ORDER BY ROWID) AS row_num " +
            "FROM CASE_DELIVERIES_OD_FORMATS) WHERE row_num > 1)")
        sql("DELETE FROM CASE_DELIVERIES_OD_FORMATS WHERE OD_FORMAT IS NULL")
        addNotNullConstraint(tableName: "CASE_DELIVERIES_OD_FORMATS", columnName: "OD_FORMAT")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, OD_FORMAT", constraintName: "CASE_DELIVERIES_OD_FORMATS_PK", tableName: "CASE_DELIVERIES_OD_FORMATS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-11") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CASE_DELIVERIES_RPT_FORMATS")
            and {
                columnExists(tableName: "CASE_DELIVERIES_RPT_FORMATS", columnName: "DELIVERY_ID")
                columnExists(tableName: "CASE_DELIVERIES_RPT_FORMATS", columnName: "RPT_FORMAT")
            }
            not {
                primaryKeyExists(tableName: "CASE_DELIVERIES_RPT_FORMATS")
            }
        }
        sql("DELETE FROM CASE_DELIVERIES_RPT_FORMATS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DELIVERY_ID, RPT_FORMAT ORDER BY ROWID) AS row_num " +
            "FROM CASE_DELIVERIES_RPT_FORMATS) WHERE row_num > 1)")
        sql("DELETE FROM CASE_DELIVERIES_RPT_FORMATS WHERE RPT_FORMAT IS NULL")
        addNotNullConstraint(tableName: "CASE_DELIVERIES_RPT_FORMATS", columnName: "RPT_FORMAT")
        addPrimaryKey(columnNames: "DELIVERY_ID, RPT_FORMAT", constraintName: "CASE_DELIVERIES_RPT_FORMATS_PK", tableName: "CASE_DELIVERIES_RPT_FORMATS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-12") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CASE_DELIVERIES_SHARED_WITHS")
            and {
                columnExists(tableName: "CASE_DELIVERIES_SHARED_WITHS", columnName: "DELIVERY_ID")
                columnExists(tableName: "CASE_DELIVERIES_SHARED_WITHS", columnName: "SHARED_WITH_ID")
            }
            not {
                primaryKeyExists(tableName: "CASE_DELIVERIES_SHARED_WITHS")
            }
        }
        sql("DELETE FROM CASE_DELIVERIES_SHARED_WITHS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DELIVERY_ID, SHARED_WITH_ID ORDER BY ROWID) AS row_num " +
            "FROM CASE_DELIVERIES_SHARED_WITHS) WHERE row_num > 1)")
        sql("DELETE FROM CASE_DELIVERIES_SHARED_WITHS WHERE SHARED_WITH_ID IS NULL")
        addNotNullConstraint(tableName: "CASE_DELIVERIES_SHARED_WITHS", columnName: "SHARED_WITH_ID")
        addPrimaryKey(columnNames: "DELIVERY_ID, SHARED_WITH_ID", constraintName: "CASE_DELVS_SHRD_W_PK", tableName: "CASE_DELIVERIES_SHARED_WITHS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-13") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CASE_DELIVERIES_SHARED_W_GRPS")
            and {
                columnExists(tableName: "CASE_DELIVERIES_SHARED_W_GRPS", columnName: "DELIVERY_ID")
                columnExists(tableName: "CASE_DELIVERIES_SHARED_W_GRPS", columnName: "SHARED_WITH_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "CASE_DELIVERIES_SHARED_W_GRPS")
            }
        }
        sql("DELETE FROM CASE_DELIVERIES_SHARED_W_GRPS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DELIVERY_ID, SHARED_WITH_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM CASE_DELIVERIES_SHARED_W_GRPS) WHERE row_num > 1)")
        sql("DELETE FROM CASE_DELIVERIES_SHARED_W_GRPS WHERE SHARED_WITH_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "CASE_DELIVERIES_SHARED_W_GRPS", columnName: "SHARED_WITH_GROUP_ID")
        addPrimaryKey(columnNames: "DELIVERY_ID, SHARED_WITH_GROUP_ID", constraintName: "CASE_DELVS_SHRD_W_GRPS_PK", tableName: "CASE_DELIVERIES_SHARED_W_GRPS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-14") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CASE_SERIES_QUERY_VALUES")
            and {
                columnExists(tableName: "CASE_SERIES_QUERY_VALUES", columnName: "CASE_SERIES_ID")
                columnExists(tableName: "CASE_SERIES_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "CASE_SERIES_QUERY_VALUES")
            }
        }
        sql("DELETE FROM CASE_SERIES_QUERY_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY CASE_SERIES_ID, QUERY_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM CASE_SERIES_QUERY_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM CASE_SERIES_QUERY_VALUES WHERE QUERY_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "CASE_SERIES_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
        addPrimaryKey(columnNames: "CASE_SERIES_ID, QUERY_VALUE_ID", constraintName: "CASE_SERIES_QRS_VAL_PK", tableName: "CASE_SERIES_QUERY_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-15") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CASE_SERIES_TAGS")
            and {
            }
                columnExists(tableName: "CASE_SERIES_TAGS", columnName: "CASE_SERIES_ID")
                columnExists(tableName: "CASE_SERIES_TAGS", columnName: "TAG_ID")
            not {
                primaryKeyExists(tableName: "CASE_SERIES_TAGS")
            }
        }
        sql("DELETE FROM CASE_SERIES_TAGS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY CASE_SERIES_ID, TAG_ID ORDER BY ROWID) AS row_num " +
            "FROM CASE_SERIES_TAGS) WHERE row_num > 1)")
        sql("DELETE FROM CASE_SERIES_TAGS WHERE TAG_ID IS NULL")
        addNotNullConstraint(tableName: "CASE_SERIES_TAGS", columnName: "TAG_ID")
        addPrimaryKey(columnNames: "CASE_SERIES_ID, TAG_ID", constraintName: "CASE_SERIES_TAGS_PK", tableName: "CASE_SERIES_TAGS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-16") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CLL_TEMPLATES_QRS_EXP_VALUES")
            and {
                columnExists(tableName: "CLL_TEMPLATES_QRS_EXP_VALUES", columnName: "CLL_TEMPLATE_ID")
                columnExists(tableName: "CLL_TEMPLATES_QRS_EXP_VALUES", columnName: "QUERY_EXP_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "CLL_TEMPLATES_QRS_EXP_VALUES")
            }
        }
        sql("DELETE FROM CLL_TEMPLATES_QRS_EXP_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY CLL_TEMPLATE_ID, QUERY_EXP_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM CLL_TEMPLATES_QRS_EXP_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM CLL_TEMPLATES_QRS_EXP_VALUES WHERE QUERY_EXP_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "CLL_TEMPLATES_QRS_EXP_VALUES", columnName: "QUERY_EXP_VALUE_ID")
        addPrimaryKey(columnNames: "CLL_TEMPLATE_ID, QUERY_EXP_VALUE_ID", constraintName: "CLL_TEMPLT_QRS_EXP_VAL_PK", tableName: "CLL_TEMPLATES_QRS_EXP_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504041215-17") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "CONFIG_CALENDAR")
            and {
                columnExists(tableName: "CONFIG_CALENDAR", columnName: "RCONFIG_ID")
                columnExists(tableName: "CONFIG_CALENDAR", columnName: "CALENDAR_ID")
            }
            not {
                primaryKeyExists(tableName: "CONFIG_CALENDAR")
            }
        }
        sql("DELETE FROM CONFIG_CALENDAR WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCONFIG_ID, CALENDAR_ID ORDER BY ROWID) AS row_num " +
            "FROM CONFIG_CALENDAR) WHERE row_num > 1)")
        addPrimaryKey(columnNames: "RCONFIG_ID, CALENDAR_ID", constraintName: "CONFIG_CALENDAR_PK", tableName: "CONFIG_CALENDAR")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-18") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DASHBOARD_RWIDGET")
            and {
                columnExists(tableName: "DASHBOARD_RWIDGET", columnName: "DASHBOARD_WIDGETS_ID")
                columnExists(tableName: "DASHBOARD_RWIDGET", columnName: "REPORT_WIDGET_ID")
            }
            not {
                primaryKeyExists(tableName: "DASHBOARD_RWIDGET")
            }
        }
        sql("DELETE FROM DASHBOARD_RWIDGET WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DASHBOARD_WIDGETS_ID, REPORT_WIDGET_ID ORDER BY ROWID) AS row_num " +
            "FROM DASHBOARD_RWIDGET) WHERE row_num > 1)")
        sql("DELETE FROM DASHBOARD_RWIDGET WHERE DASHBOARD_WIDGETS_ID IS NULL OR REPORT_WIDGET_ID IS NULL")
        addNotNullConstraint(tableName: "DASHBOARD_RWIDGET", columnName: "DASHBOARD_WIDGETS_ID")
        addNotNullConstraint(tableName: "DASHBOARD_RWIDGET", columnName: "REPORT_WIDGET_ID")
        addPrimaryKey(columnNames: "DASHBOARD_WIDGETS_ID, REPORT_WIDGET_ID", constraintName: "DASHBOARD_RWIDGET_PK", tableName: "DASHBOARD_RWIDGET")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-19") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DASHBOARD_SHARED_WITHS")
            and {
                columnExists(tableName: "DASHBOARD_SHARED_WITHS", columnName: "DASHBOARD_ID")
                columnExists(tableName: "DASHBOARD_SHARED_WITHS", columnName: "SHARED_WITH_ID")
            }
            not {
                primaryKeyExists(tableName: "DASHBOARD_SHARED_WITHS")
            }
        }
        sql("DELETE FROM DASHBOARD_SHARED_WITHS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DASHBOARD_ID, SHARED_WITH_ID ORDER BY ROWID) AS row_num " +
            "FROM DASHBOARD_SHARED_WITHS) WHERE row_num > 1)")
        sql("DELETE FROM DASHBOARD_SHARED_WITHS WHERE SHARED_WITH_ID IS NULL")
        addNotNullConstraint(tableName: "DASHBOARD_SHARED_WITHS", columnName: "SHARED_WITH_ID")
        addPrimaryKey(columnNames: "DASHBOARD_ID, SHARED_WITH_ID", constraintName: "DASHBOARD_SHARED_W_PK", tableName: "DASHBOARD_SHARED_WITHS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-20") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DASHBOARD_SHARED_W_GRPS")
            and {
                columnExists(tableName: "DASHBOARD_SHARED_W_GRPS", columnName: "DASHBOARD_ID")
                columnExists(tableName: "DASHBOARD_SHARED_W_GRPS", columnName: "SHARED_WITH_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "DASHBOARD_SHARED_W_GRPS")
            }
        }
        sql("DELETE FROM DASHBOARD_SHARED_W_GRPS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DASHBOARD_ID, SHARED_WITH_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM DASHBOARD_SHARED_W_GRPS) WHERE row_num > 1)")
        sql("DELETE FROM DASHBOARD_SHARED_W_GRPS WHERE SHARED_WITH_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "DASHBOARD_SHARED_W_GRPS", columnName: "SHARED_WITH_GROUP_ID")
        addPrimaryKey(columnNames: "DASHBOARD_ID, SHARED_WITH_GROUP_ID", constraintName: "DASHBOARD_SHARED_W_GRPS_PK", tableName: "DASHBOARD_SHARED_W_GRPS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504041215-21") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DATABASECHANGELOG")
            and {
                columnExists(tableName: "DATABASECHANGELOG", columnName: "ID")
                columnExists(tableName: "DATABASECHANGELOG", columnName: "AUTHOR")
                columnExists(tableName: "DATABASECHANGELOG", columnName: "FILENAME")
            }
            not {
                primaryKeyExists(tableName: "DATABASECHANGELOG")
            }
        }
        sql("DELETE FROM DATABASECHANGELOG WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY ID, AUTHOR, FILENAME ORDER BY ROWID) AS row_num " +
            "FROM DATABASECHANGELOG) WHERE row_num > 1)")
        addPrimaryKey(columnNames: "ID, AUTHOR, FILENAME", constraintName: "DATABASECHANGELOG_PK", tableName: "DATABASECHANGELOG")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-22") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DELIVERIES_EMAIL_USERS")
            and {
                columnExists(tableName: "DELIVERIES_EMAIL_USERS", columnName: "DELIVERY_ID")
                columnExists(tableName: "DELIVERIES_EMAIL_USERS", columnName: "EMAIL_USER")
            }
            not {
                primaryKeyExists(tableName: "DELIVERIES_EMAIL_USERS")
            }
        }
        sql("DELETE FROM DELIVERIES_EMAIL_USERS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DELIVERY_ID, EMAIL_USER ORDER BY ROWID) AS row_num " +
            "FROM DELIVERIES_EMAIL_USERS) WHERE row_num > 1)")
        sql("DELETE FROM DELIVERIES_EMAIL_USERS WHERE EMAIL_USER IS NULL")
        addNotNullConstraint(tableName: "DELIVERIES_EMAIL_USERS", columnName: "EMAIL_USER")
        addPrimaryKey(columnNames: "DELIVERY_ID, EMAIL_USER", constraintName: "DELIVERIES_EMAIL_USERS_PK", tableName: "DELIVERIES_EMAIL_USERS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-23") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DELIVERIES_EXECUTABLE")
            and {
                columnExists(tableName: "DELIVERIES_EXECUTABLE", columnName: "DELIVERY_ID")
                columnExists(tableName: "DELIVERIES_EXECUTABLE", columnName: "EXECUTABLE_ID")
            }
            not {
                primaryKeyExists(tableName: "DELIVERIES_EXECUTABLE")
            }
        }
        sql("DELETE FROM DELIVERIES_EXECUTABLE WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DELIVERY_ID, EXECUTABLE_ID ORDER BY ROWID) AS row_num " +
            "FROM DELIVERIES_EXECUTABLE) WHERE row_num > 1)")
        sql("DELETE FROM DELIVERIES_EXECUTABLE WHERE EXECUTABLE_ID IS NULL")
        addNotNullConstraint(tableName: "DELIVERIES_EXECUTABLE", columnName: "EXECUTABLE_ID")
        addPrimaryKey(columnNames: "DELIVERY_ID, EXECUTABLE_ID", constraintName: "DELIVERIES_EXECUTABLE_PK", tableName: "DELIVERIES_EXECUTABLE")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-24") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DELIVERIES_EXECUTABLE_GRPS")
            and {
                columnExists(tableName: "DELIVERIES_EXECUTABLE_GRPS", columnName: "DELIVERY_ID")
                columnExists(tableName: "DELIVERIES_EXECUTABLE_GRPS", columnName: "EXECUTABLE_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "DELIVERIES_EXECUTABLE_GRPS")
            }
        }
        sql("DELETE FROM DELIVERIES_EXECUTABLE_GRPS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DELIVERY_ID, EXECUTABLE_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM DELIVERIES_EXECUTABLE_GRPS) WHERE row_num > 1)")
        sql("DELETE FROM DELIVERIES_EXECUTABLE_GRPS WHERE EXECUTABLE_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "DELIVERIES_EXECUTABLE_GRPS", columnName: "EXECUTABLE_GROUP_ID")
        addPrimaryKey(columnNames: "DELIVERY_ID, EXECUTABLE_GROUP_ID", constraintName: "DELIVERIES_EXECUTABLE_GRPS_PK", tableName: "DELIVERIES_EXECUTABLE_GRPS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-25") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DELIVERIES_OD_FORMATS")
            and {
                columnExists(tableName: "DELIVERIES_OD_FORMATS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "DELIVERIES_OD_FORMATS", columnName: "OD_FORMAT")
            }
            not {
                primaryKeyExists(tableName: "DELIVERIES_OD_FORMATS")
            }
        }
        sql("DELETE FROM DELIVERIES_OD_FORMATS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, OD_FORMAT ORDER BY ROWID) AS row_num " +
            "FROM DELIVERIES_OD_FORMATS) WHERE row_num > 1)")
        sql("DELETE FROM DELIVERIES_OD_FORMATS WHERE OD_FORMAT IS NULL")
        addNotNullConstraint(tableName: "DELIVERIES_OD_FORMATS", columnName: "OD_FORMAT")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, OD_FORMAT", constraintName: "DELIVERIES_OD_FORMATS_PK", tableName: "DELIVERIES_OD_FORMATS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-26") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DELIVERIES_RPT_FORMATS")
            and {
                columnExists(tableName: "DELIVERIES_RPT_FORMATS", columnName: "DELIVERY_ID")
                columnExists(tableName: "DELIVERIES_RPT_FORMATS", columnName: "RPT_FORMAT")
            }
            not {
                primaryKeyExists(tableName: "DELIVERIES_RPT_FORMATS")
            }
        }
        sql("DELETE FROM DELIVERIES_RPT_FORMATS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DELIVERY_ID, RPT_FORMAT ORDER BY ROWID) AS row_num " +
            "FROM DELIVERIES_RPT_FORMATS) WHERE row_num > 1)")
        sql("DELETE FROM DELIVERIES_RPT_FORMATS WHERE RPT_FORMAT IS NULL")
        addNotNullConstraint(tableName: "DELIVERIES_RPT_FORMATS", columnName: "RPT_FORMAT")
        addPrimaryKey(columnNames: "DELIVERY_ID, RPT_FORMAT", constraintName: "DELIVERIES_RPT_FORMATS_PK", tableName: "DELIVERIES_RPT_FORMATS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-27") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DELIVERIES_SHARED_WITHS")
            and {
                columnExists(tableName: "DELIVERIES_SHARED_WITHS", columnName: "DELIVERY_ID")
                columnExists(tableName: "DELIVERIES_SHARED_WITHS", columnName: "SHARED_WITH_ID")
            }
            not {
                primaryKeyExists(tableName: "DELIVERIES_SHARED_WITHS")
            }
        }
        sql("DELETE FROM DELIVERIES_SHARED_WITHS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DELIVERY_ID, SHARED_WITH_ID ORDER BY ROWID) AS row_num " +
            "FROM DELIVERIES_SHARED_WITHS) WHERE row_num > 1)")
        sql("DELETE FROM DELIVERIES_SHARED_WITHS WHERE SHARED_WITH_ID IS NULL")
        addNotNullConstraint(tableName: "DELIVERIES_SHARED_WITHS", columnName: "SHARED_WITH_ID")
        addPrimaryKey(columnNames: "DELIVERY_ID, SHARED_WITH_ID", constraintName: "DELIVERIES_SHARED_W_PK", tableName: "DELIVERIES_SHARED_WITHS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-28") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DELIVERIES_SHARED_WITH_GRPS")
            and {
                columnExists(tableName: "DELIVERIES_SHARED_WITH_GRPS", columnName: "DELIVERY_ID")
                columnExists(tableName: "DELIVERIES_SHARED_WITH_GRPS", columnName: "SHARED_WITH_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "DELIVERIES_SHARED_WITH_GRPS")
            }
        }
        sql("DELETE FROM DELIVERIES_SHARED_WITH_GRPS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DELIVERY_ID, SHARED_WITH_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM DELIVERIES_SHARED_WITH_GRPS) WHERE row_num > 1)")
        sql("DELETE FROM DELIVERIES_SHARED_WITH_GRPS WHERE SHARED_WITH_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "DELIVERIES_SHARED_WITH_GRPS", columnName: "SHARED_WITH_GROUP_ID")
        addPrimaryKey(columnNames: "DELIVERY_ID, SHARED_WITH_GROUP_ID", constraintName: "DELIVERIES_SHARED_WITH_GRPS_PK", tableName: "DELIVERIES_SHARED_WITH_GRPS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-29") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DICT_GRP_DATA_SRC")
            and {
                columnExists(tableName: "DICT_GRP_DATA_SRC", columnName: "DICTIONARY_GROUP_ID")
                columnExists(tableName: "DICT_GRP_DATA_SRC", columnName: "DATA_SRC_NAME")
            }
            not {
                primaryKeyExists(tableName: "DICT_GRP_DATA_SRC")
            }
        }
        sql("DELETE FROM DICT_GRP_DATA_SRC WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DICTIONARY_GROUP_ID, DATA_SRC_NAME ORDER BY ROWID) AS row_num " +
            "FROM DICT_GRP_DATA_SRC) WHERE row_num > 1)")
        sql("DELETE FROM DICT_GRP_DATA_SRC WHERE DATA_SRC_NAME IS NULL")
        addNotNullConstraint(tableName: "DICT_GRP_DATA_SRC", columnName: "DATA_SRC_NAME")
        addPrimaryKey(columnNames: "DICTIONARY_GROUP_ID, DATA_SRC_NAME", constraintName: "DICT_GRP_DATA_SRC_PK", tableName: "DICT_GRP_DATA_SRC")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-30") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DICT_GRP_SHARED_WITHS")
            and {
                columnExists(tableName: "DICT_GRP_SHARED_WITHS", columnName: "DICTIONARY_GROUP_ID")
                columnExists(tableName: "DICT_GRP_SHARED_WITHS", columnName: "SHARED_WITH_ID")
            }
            not {
                primaryKeyExists(tableName: "DICT_GRP_SHARED_WITHS")
            }
        }
        sql("DELETE FROM DICT_GRP_SHARED_WITHS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DICTIONARY_GROUP_ID, SHARED_WITH_ID ORDER BY ROWID) AS row_num " +
            "FROM DICT_GRP_SHARED_WITHS) WHERE row_num > 1)")
        sql("DELETE FROM DICT_GRP_SHARED_WITHS WHERE SHARED_WITH_ID IS NULL")
        addNotNullConstraint(tableName: "DICT_GRP_SHARED_WITHS", columnName: "SHARED_WITH_ID")
        addPrimaryKey(columnNames: "DICTIONARY_GROUP_ID, SHARED_WITH_ID", constraintName: "DICT_GRP_SHARED_W_PK", tableName: "DICT_GRP_SHARED_WITHS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-31") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DICT_GRP_SHARED_WITH_GRPS")
            and {
                columnExists(tableName: "DICT_GRP_SHARED_WITH_GRPS", columnName: "DICTIONARY_GROUP_ID")
                columnExists(tableName: "DICT_GRP_SHARED_WITH_GRPS", columnName: "SHARED_WITH_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "DICT_GRP_SHARED_WITH_GRPS")
            }
        }
        sql("DELETE FROM DICT_GRP_SHARED_WITH_GRPS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DICTIONARY_GROUP_ID, SHARED_WITH_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM DICT_GRP_SHARED_WITH_GRPS) WHERE row_num > 1)")
        sql("DELETE FROM DICT_GRP_SHARED_WITH_GRPS WHERE SHARED_WITH_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "DICT_GRP_SHARED_WITH_GRPS", columnName: "SHARED_WITH_GROUP_ID")
        addPrimaryKey(columnNames: "DICTIONARY_GROUP_ID, SHARED_WITH_GROUP_ID", constraintName: "DICT_GRP_SHARED_WITH_GRPS_PK", tableName: "DICT_GRP_SHARED_WITH_GRPS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-32") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DTAB_COL_MEAS_MEASURES")
            and {
                columnExists(tableName: "DTAB_COL_MEAS_MEASURES", columnName: "DTAB_COL_MEAS_ID")
                columnExists(tableName: "DTAB_COL_MEAS_MEASURES", columnName: "MEASURE_ID")
            }
            not {
                primaryKeyExists(tableName: "DTAB_COL_MEAS_MEASURES")
            }
        }
        sql("DELETE FROM DTAB_COL_MEAS_MEASURES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DTAB_COL_MEAS_ID, MEASURE_ID ORDER BY ROWID) AS row_num " +
            "FROM DTAB_COL_MEAS_MEASURES) WHERE row_num > 1)")
        sql("DELETE FROM DTAB_COL_MEAS_MEASURES WHERE MEASURE_ID IS NULL")
        addNotNullConstraint(tableName: "DTAB_COL_MEAS_MEASURES", columnName: "MEASURE_ID")
        addPrimaryKey(columnNames: "DTAB_COL_MEAS_ID, MEASURE_ID", constraintName: "DTAB_COL_MEAS_MEASURES_PK", tableName: "DTAB_COL_MEAS_MEASURES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-33") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "DTAB_TEMPLTS_COL_MEAS")
            and {
                columnExists(tableName: "DTAB_TEMPLTS_COL_MEAS", columnName: "DTAB_TEMPLT_ID")
                columnExists(tableName: "DTAB_TEMPLTS_COL_MEAS", columnName: "COLUMN_MEASURE_ID")
            }
            not {
                primaryKeyExists(tableName: "DTAB_TEMPLTS_COL_MEAS")
            }
        }
        sql("DELETE FROM DTAB_TEMPLTS_COL_MEAS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY DTAB_TEMPLT_ID, COLUMN_MEASURE_ID ORDER BY ROWID) AS row_num " +
            "FROM DTAB_TEMPLTS_COL_MEAS) WHERE row_num > 1)")
        sql("DELETE FROM DTAB_TEMPLTS_COL_MEAS WHERE COLUMN_MEASURE_ID IS NULL")
        addNotNullConstraint(tableName: "DTAB_TEMPLTS_COL_MEAS", columnName: "COLUMN_MEASURE_ID")
        addPrimaryKey(columnNames: "DTAB_TEMPLT_ID, COLUMN_MEASURE_ID", constraintName: "DTAB_TEMPLTS_COL_MEAS_PK", tableName: "DTAB_TEMPLTS_COL_MEAS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-34") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_CASE_DELIVERIES_EMAIL_USERS")
            and {
                columnExists(tableName: "EX_CASE_DELIVERIES_EMAIL_USERS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "EX_CASE_DELIVERIES_EMAIL_USERS", columnName: "EMAIL_USER")
            }
            not {
                primaryKeyExists(tableName: "EX_CASE_DELIVERIES_EMAIL_USERS")
            }
        }
        sql("DELETE FROM EX_CASE_DELIVERIES_EMAIL_USERS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, EMAIL_USER ORDER BY ROWID) AS row_num " +
            "FROM EX_CASE_DELIVERIES_EMAIL_USERS) WHERE row_num > 1)")
        sql("DELETE FROM EX_CASE_DELIVERIES_EMAIL_USERS WHERE EMAIL_USER IS NULL")
        addNotNullConstraint(tableName: "EX_CASE_DELIVERIES_EMAIL_USERS", columnName: "EMAIL_USER")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, EMAIL_USER", constraintName: "EX_CASE_DELV_EML_USRS_PK", tableName: "EX_CASE_DELIVERIES_EMAIL_USERS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-35") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_CASE_DELIVERIES_OD_FORMATS")
            and {
                columnExists(tableName: "EX_CASE_DELIVERIES_OD_FORMATS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "EX_CASE_DELIVERIES_OD_FORMATS", columnName: "OD_FORMAT")
            }
            not {
                primaryKeyExists(tableName: "EX_CASE_DELIVERIES_OD_FORMATS")
            }
        }
        sql("DELETE FROM EX_CASE_DELIVERIES_OD_FORMATS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, OD_FORMAT ORDER BY ROWID) AS row_num " +
            "FROM EX_CASE_DELIVERIES_OD_FORMATS) WHERE row_num > 1)")
        sql("DELETE FROM EX_CASE_DELIVERIES_OD_FORMATS WHERE OD_FORMAT IS NULL")
        addNotNullConstraint(tableName: "EX_CASE_DELIVERIES_OD_FORMATS", columnName: "OD_FORMAT")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, OD_FORMAT", constraintName: "EX_CASE_DLV_OD_FRMTS_PK", tableName: "EX_CASE_DELIVERIES_OD_FORMATS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-36") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_CASE_DELIVERIES_RPT_FORMATS")
            and {
                columnExists(tableName: "EX_CASE_DELIVERIES_RPT_FORMATS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "EX_CASE_DELIVERIES_RPT_FORMATS", columnName: "RPT_FORMAT")
            }
            not {
                primaryKeyExists(tableName: "EX_CASE_DELIVERIES_RPT_FORMATS")
            }
        }
        sql("DELETE FROM EX_CASE_DELIVERIES_RPT_FORMATS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, RPT_FORMAT ORDER BY ROWID) AS row_num " +
            "FROM EX_CASE_DELIVERIES_RPT_FORMATS) WHERE row_num > 1)")
        sql("DELETE FROM EX_CASE_DELIVERIES_RPT_FORMATS WHERE RPT_FORMAT IS NULL")
        addNotNullConstraint(tableName: "EX_CASE_DELIVERIES_RPT_FORMATS", columnName: "RPT_FORMAT")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, RPT_FORMAT", constraintName: "EX_CASE_DLV_RPT_FRMAS_PK", tableName: "EX_CASE_DELIVERIES_RPT_FORMATS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-37") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_CASE_DELIVERIES_SHRD_WTHS")
            and {
                columnExists(tableName: "EX_CASE_DELIVERIES_SHRD_WTHS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "EX_CASE_DELIVERIES_SHRD_WTHS", columnName: "SHARED_WITH_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_CASE_DELIVERIES_SHRD_WTHS")
            }
        }
        sql("DELETE FROM EX_CASE_DELIVERIES_SHRD_WTHS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, SHARED_WITH_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_CASE_DELIVERIES_SHRD_WTHS) WHERE row_num > 1)")
        sql("DELETE FROM EX_CASE_DELIVERIES_SHRD_WTHS WHERE SHARED_WITH_ID IS NULL")
        addNotNullConstraint(tableName: "EX_CASE_DELIVERIES_SHRD_WTHS", columnName: "SHARED_WITH_ID")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, SHARED_WITH_ID", constraintName: "EX_CASE_DLV_SHRD_WTHS_PK", tableName: "EX_CASE_DELIVERIES_SHRD_WTHS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-38") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_CASE_DELIVERIES_SHRD_W_GRPS")
            and {
                columnExists(tableName: "EX_CASE_DELIVERIES_SHRD_W_GRPS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "EX_CASE_DELIVERIES_SHRD_W_GRPS", columnName: "SHARED_WITH_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_CASE_DELIVERIES_SHRD_W_GRPS")
            }
        }
        sql("DELETE FROM EX_CASE_DELIVERIES_SHRD_W_GRPS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, SHARED_WITH_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_CASE_DELIVERIES_SHRD_W_GRPS) WHERE row_num > 1)")
        sql("DELETE FROM EX_CASE_DELIVERIES_SHRD_W_GRPS WHERE SHARED_WITH_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "EX_CASE_DELIVERIES_SHRD_W_GRPS", columnName: "SHARED_WITH_GROUP_ID")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, SHARED_WITH_GROUP_ID", constraintName: "EX_CASE_DLV_SHRD_W_GRPS_PK", tableName: "EX_CASE_DELIVERIES_SHRD_W_GRPS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-39") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_CASE_SERIES_QUERY_VALUES")
            and {
                columnExists(tableName: "EX_CASE_SERIES_QUERY_VALUES", columnName: "EX_CASE_SERIES_ID")
                columnExists(tableName: "EX_CASE_SERIES_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_CASE_SERIES_QUERY_VALUES")
            }
        }
        sql("DELETE FROM EX_CASE_SERIES_QUERY_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_CASE_SERIES_ID, QUERY_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_CASE_SERIES_QUERY_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM EX_CASE_SERIES_QUERY_VALUES WHERE QUERY_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "EX_CASE_SERIES_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
        addPrimaryKey(columnNames: "EX_CASE_SERIES_ID, QUERY_VALUE_ID", constraintName: "EX_CASE_SERIES_QUERY_VALUES_PK", tableName: "EX_CASE_SERIES_QUERY_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-40") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_CASE_SERIES_TAGS")
            and {
                columnExists(tableName: "EX_CASE_SERIES_TAGS", columnName: "EX_CASE_SERIES_ID")
                columnExists(tableName: "EX_CASE_SERIES_TAGS", columnName: "TAG_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_CASE_SERIES_TAGS")
            }
        }
        sql("DELETE FROM EX_CASE_SERIES_TAGS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_CASE_SERIES_ID, TAG_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_CASE_SERIES_TAGS) WHERE row_num > 1)")
        sql("DELETE FROM EX_CASE_SERIES_TAGS WHERE TAG_ID IS NULL")
        addNotNullConstraint(tableName: "EX_CASE_SERIES_TAGS", columnName: "TAG_ID")
        addPrimaryKey(columnNames: "EX_CASE_SERIES_ID, TAG_ID", constraintName: "EX_CASE_SERIES_TAGS_PK", tableName: "EX_CASE_SERIES_TAGS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504041215-41") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_CONFIG_CALENDAR")
            and {
                columnExists(tableName: "EX_CONFIG_CALENDAR", columnName: "EX_RCONFIG_ID")
                columnExists(tableName: "EX_CONFIG_CALENDAR", columnName: "CALENDAR_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_CONFIG_CALENDAR")
            }
        }
        sql("DELETE FROM EX_CONFIG_CALENDAR WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_RCONFIG_ID, CALENDAR_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_CONFIG_CALENDAR) WHERE row_num > 1)")
        addPrimaryKey(columnNames: "EX_RCONFIG_ID, CALENDAR_ID", constraintName: "EX_CONFIG_CALENDAR_PK", tableName: "EX_CONFIG_CALENDAR")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-42") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_DELIVERIES_EMAIL_USERS")
            and {
                columnExists(tableName: "EX_DELIVERIES_EMAIL_USERS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "EX_DELIVERIES_EMAIL_USERS", columnName: "EMAIL_USER")
            }
            not {
                primaryKeyExists(tableName: "EX_DELIVERIES_EMAIL_USERS")
            }
        }
        sql("DELETE FROM EX_DELIVERIES_EMAIL_USERS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, EMAIL_USER ORDER BY ROWID) AS row_num " +
            "FROM EX_DELIVERIES_EMAIL_USERS) WHERE row_num > 1)")
        sql("DELETE FROM EX_DELIVERIES_EMAIL_USERS WHERE EMAIL_USER IS NULL")
        addNotNullConstraint(tableName: "EX_DELIVERIES_EMAIL_USERS", columnName: "EMAIL_USER")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, EMAIL_USER", constraintName: "EX_DLV_EMAIL_USRS_PK", tableName: "EX_DELIVERIES_EMAIL_USERS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-43") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_DELIVERIES_OD_FORMATS")
            and {
                columnExists(tableName: "EX_DELIVERIES_OD_FORMATS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "EX_DELIVERIES_OD_FORMATS", columnName: "OD_FORMAT")
            }
            not {
                primaryKeyExists(tableName: "EX_DELIVERIES_OD_FORMATS")
            }
        }
        sql("DELETE FROM EX_DELIVERIES_OD_FORMATS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, OD_FORMAT ORDER BY ROWID) AS row_num " +
            "FROM EX_DELIVERIES_OD_FORMATS) WHERE row_num > 1)")
        sql("DELETE FROM EX_DELIVERIES_OD_FORMATS WHERE OD_FORMAT IS NULL")
        addNotNullConstraint(tableName: "EX_DELIVERIES_OD_FORMATS", columnName: "OD_FORMAT")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, OD_FORMAT", constraintName: "EX_DLV_OD_FRMATS_PK", tableName: "EX_DELIVERIES_OD_FORMATS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-44") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_DELIVERIES_RPT_FORMATS")
            and {
                columnExists(tableName: "EX_DELIVERIES_RPT_FORMATS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "EX_DELIVERIES_RPT_FORMATS", columnName: "RPT_FORMAT")
            }
            not {
                primaryKeyExists(tableName: "EX_DELIVERIES_RPT_FORMATS")
            }
        }
        sql("DELETE FROM EX_DELIVERIES_RPT_FORMATS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, RPT_FORMAT ORDER BY ROWID) AS row_num " +
            "FROM EX_DELIVERIES_RPT_FORMATS) WHERE row_num > 1)")
        sql("DELETE FROM EX_DELIVERIES_RPT_FORMATS WHERE RPT_FORMAT IS NULL")
        addNotNullConstraint(tableName: "EX_DELIVERIES_RPT_FORMATS", columnName: "RPT_FORMAT")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, RPT_FORMAT", constraintName: "EX_DLV_RPT_FRMATS_PK", tableName: "EX_DELIVERIES_RPT_FORMATS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-45") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_DELIVERIES_SHARED_WITHS")
            and {
                columnExists(tableName: "EX_DELIVERIES_SHARED_WITHS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "EX_DELIVERIES_SHARED_WITHS", columnName: "SHARED_WITH_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_DELIVERIES_SHARED_WITHS")
            }
        }
        sql("DELETE FROM EX_DELIVERIES_SHARED_WITHS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, SHARED_WITH_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_DELIVERIES_SHARED_WITHS) WHERE row_num > 1)")
        sql("DELETE FROM EX_DELIVERIES_SHARED_WITHS WHERE SHARED_WITH_ID IS NULL")
        addNotNullConstraint(tableName: "EX_DELIVERIES_SHARED_WITHS", columnName: "SHARED_WITH_ID")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, SHARED_WITH_ID", constraintName: "EX_DLV_SHRD_W_PK", tableName: "EX_DELIVERIES_SHARED_WITHS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-46") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_DELIVERIES_SHARED_WITH_GRPS")
            and {
                columnExists(tableName: "EX_DELIVERIES_SHARED_WITH_GRPS", columnName: "EX_DELIVERY_ID")
                columnExists(tableName: "EX_DELIVERIES_SHARED_WITH_GRPS", columnName: "SHARED_WITH_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_DELIVERIES_SHARED_WITH_GRPS")
            }
        }
        sql("DELETE FROM EX_DELIVERIES_SHARED_WITH_GRPS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_DELIVERY_ID, SHARED_WITH_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_DELIVERIES_SHARED_WITH_GRPS) WHERE row_num > 1)")
        sql("DELETE FROM EX_DELIVERIES_SHARED_WITH_GRPS WHERE SHARED_WITH_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "EX_DELIVERIES_SHARED_WITH_GRPS", columnName: "SHARED_WITH_GROUP_ID")
        addPrimaryKey(columnNames: "EX_DELIVERY_ID, SHARED_WITH_GROUP_ID", constraintName: "EX_DLV_SHRD_WITH_GRPS_PK", tableName: "EX_DELIVERIES_SHARED_WITH_GRPS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-47") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_GLOBAL_QUERY_VALUES")
            and {
                columnExists(tableName: "EX_GLOBAL_QUERY_VALUES", columnName: "EX_GLOBAL_QUERY_ID")
                columnExists(tableName: "EX_GLOBAL_QUERY_VALUES", columnName: "EX_QUERY_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_GLOBAL_QUERY_VALUES")
            }
        }
        sql("DELETE FROM EX_GLOBAL_QUERY_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_GLOBAL_QUERY_ID, EX_QUERY_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_GLOBAL_QUERY_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM EX_GLOBAL_QUERY_VALUES WHERE EX_QUERY_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "EX_GLOBAL_QUERY_VALUES", columnName: "EX_QUERY_VALUE_ID")
        addPrimaryKey(columnNames: "EX_GLOBAL_QUERY_ID, EX_QUERY_VALUE_ID", constraintName: "EX_GLOBAL_QRY_VAL_PK", tableName: "EX_GLOBAL_QUERY_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-48") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_INBOUND_COMPLIANCE_TAGS")
            and {
                columnExists(tableName: "EX_INBOUND_COMPLIANCE_TAGS", columnName: "EX_INBOUND_COMPLIANCE_ID")
                columnExists(tableName: "EX_INBOUND_COMPLIANCE_TAGS", columnName: "TAG_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_INBOUND_COMPLIANCE_TAGS")
            }
        }
        sql("DELETE FROM EX_INBOUND_COMPLIANCE_TAGS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_INBOUND_COMPLIANCE_ID, TAG_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_INBOUND_COMPLIANCE_TAGS) WHERE row_num > 1)")
        sql("DELETE FROM EX_INBOUND_COMPLIANCE_TAGS WHERE TAG_ID IS NULL")
        addNotNullConstraint(tableName: "EX_INBOUND_COMPLIANCE_TAGS", columnName: "TAG_ID")
        addPrimaryKey(columnNames: "EX_INBOUND_COMPLIANCE_ID, TAG_ID", constraintName: "EX_INB_COMP_TAGS_PK", tableName: "EX_INBOUND_COMPLIANCE_TAGS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-49") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_INBOUND_POI_PARAMS")
            and {
                columnExists(tableName: "EX_INBOUND_POI_PARAMS", columnName: "EX_INBOUND_COMPLIANCE_ID")
                columnExists(tableName: "EX_INBOUND_POI_PARAMS", columnName: "PARAM_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_INBOUND_POI_PARAMS")
            }
        }
        sql("DELETE FROM EX_INBOUND_POI_PARAMS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_INBOUND_COMPLIANCE_ID, PARAM_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_INBOUND_POI_PARAMS) WHERE row_num > 1)")
        sql("DELETE FROM EX_INBOUND_POI_PARAMS WHERE PARAM_ID IS NULL")
        addNotNullConstraint(tableName: "EX_INBOUND_POI_PARAMS", columnName: "PARAM_ID")
        addPrimaryKey(columnNames: "EX_INBOUND_COMPLIANCE_ID, PARAM_ID", constraintName: "EX_INB_POI_PRMS_PK", tableName: "EX_INBOUND_POI_PARAMS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-50") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_QRS_COMPLIANCE_QUERY_VALUES")
            and {
                columnExists(tableName: "EX_QRS_COMPLIANCE_QUERY_VALUES", columnName: "EX_QUERY_COMPLIANCE_ID")
                columnExists(tableName: "EX_QRS_COMPLIANCE_QUERY_VALUES", columnName: "EX_QUERY_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_QRS_COMPLIANCE_QUERY_VALUES")
            }
        }
        sql("DELETE FROM EX_QRS_COMPLIANCE_QUERY_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_QUERY_COMPLIANCE_ID, EX_QUERY_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_QRS_COMPLIANCE_QUERY_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM EX_QRS_COMPLIANCE_QUERY_VALUES WHERE EX_QUERY_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "EX_QRS_COMPLIANCE_QUERY_VALUES", columnName: "EX_QUERY_VALUE_ID")
        addPrimaryKey(columnNames: "EX_QUERY_COMPLIANCE_ID, EX_QUERY_VALUE_ID", constraintName: "EX_QRS_COMP_QRY_VAL_PK", tableName: "EX_QRS_COMPLIANCE_QUERY_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-51") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_RCONFIGS_POI_PARAMS")
            and {
                columnExists(tableName: "EX_RCONFIGS_POI_PARAMS", columnName: "EXC_RCONFIG_ID")
                columnExists(tableName: "EX_RCONFIGS_POI_PARAMS", columnName: "PARAM_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_RCONFIGS_POI_PARAMS")
            }
        }
        sql("DELETE FROM EX_RCONFIGS_POI_PARAMS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EXC_RCONFIG_ID, PARAM_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_RCONFIGS_POI_PARAMS) WHERE row_num > 1)")
        sql("DELETE FROM EX_RCONFIGS_POI_PARAMS WHERE PARAM_ID IS NULL")
        addNotNullConstraint(tableName: "EX_RCONFIGS_POI_PARAMS", columnName: "PARAM_ID")
        addPrimaryKey(columnNames: "EXC_RCONFIG_ID, PARAM_ID", constraintName: "EX_RCONFIGS_POI_PARAMS_PK", tableName: "EX_RCONFIGS_POI_PARAMS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-52") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_RCONFIGS_TAGS")
            and {
                columnExists(tableName: "EX_RCONFIGS_TAGS", columnName: "EXC_RCONFIG_ID")
                columnExists(tableName: "EX_RCONFIGS_TAGS", columnName: "TAG_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_RCONFIGS_TAGS")
            }
        }
        sql("DELETE FROM EX_RCONFIGS_TAGS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EXC_RCONFIG_ID, TAG_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_RCONFIGS_TAGS) WHERE row_num > 1)")
        sql("DELETE FROM EX_RCONFIGS_TAGS WHERE TAG_ID IS NULL")
        addNotNullConstraint(tableName: "EX_RCONFIGS_TAGS", columnName: "TAG_ID")
        addPrimaryKey(columnNames: "EXC_RCONFIG_ID,TAG_ID", constraintName: "EX_RCONFIGS_TAGS_PK", tableName: "EX_RCONFIGS_TAGS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-53") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_RCONFIG_ACTION_ITEMS")
            and {
                columnExists(tableName: "EX_RCONFIG_ACTION_ITEMS", columnName: "EX_RCONFIG_ID")
                columnExists(tableName: "EX_RCONFIG_ACTION_ITEMS", columnName: "ACTION_ITEM_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_RCONFIG_ACTION_ITEMS")
            }
        }
        sql("DELETE FROM EX_RCONFIG_ACTION_ITEMS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_RCONFIG_ID, ACTION_ITEM_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_RCONFIG_ACTION_ITEMS) WHERE row_num > 1)")
        sql("DELETE FROM EX_RCONFIG_ACTION_ITEMS WHERE ACTION_ITEM_ID IS NULL")
        addNotNullConstraint(tableName: "EX_RCONFIG_ACTION_ITEMS", columnName: "ACTION_ITEM_ID")
        addPrimaryKey(columnNames: "EX_RCONFIG_ID, ACTION_ITEM_ID", constraintName: "EX_RCONFIG_ACT_ITEMS_PK", tableName: "EX_RCONFIG_ACTION_ITEMS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-54") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_RCONFIG_COMMENT_TABLE")
            and {
                columnExists(tableName: "EX_RCONFIG_COMMENT_TABLE", columnName: "EXC_RCONFIG_ID")
                columnExists(tableName: "EX_RCONFIG_COMMENT_TABLE", columnName: "COMMENT_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_RCONFIG_COMMENT_TABLE")
            }
        }
        sql("DELETE FROM EX_RCONFIG_COMMENT_TABLE WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EXC_RCONFIG_ID, COMMENT_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_RCONFIG_COMMENT_TABLE) WHERE row_num > 1)")
        sql("DELETE FROM EX_RCONFIG_COMMENT_TABLE WHERE COMMENT_ID IS NULL")
        addNotNullConstraint(tableName: "EX_RCONFIG_COMMENT_TABLE", columnName: "COMMENT_ID")
        addPrimaryKey(columnNames: "EXC_RCONFIG_ID, COMMENT_ID", constraintName: "EX_RCONFIG_CMMNT_TBL_PK", tableName: "EX_RCONFIG_COMMENT_TABLE")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-55") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_RCONFIG_P_C_USERS")
            and {
                columnExists(tableName: "EX_RCONFIG_P_C_USERS", columnName: "RCONFIG_ID")
                columnExists(tableName: "EX_RCONFIG_P_C_USERS", columnName: "USER_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_RCONFIG_P_C_USERS")
            }
        }
        sql("DELETE FROM EX_RCONFIG_P_C_USERS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCONFIG_ID, USER_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_RCONFIG_P_C_USERS) WHERE row_num > 1)")
        sql("DELETE FROM EX_RCONFIG_P_C_USERS WHERE USER_ID IS NULL")
        addNotNullConstraint(tableName: "EX_RCONFIG_P_C_USERS", columnName: "USER_ID")
        addPrimaryKey(columnNames: "RCONFIG_ID, USER_ID", constraintName: "EX_RCONFIG_P_C_USRS_PK", tableName: "EX_RCONFIG_P_C_USERS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-56") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_RCONFIG_REPORT_DESTS")
            and {
                columnExists(tableName: "EX_RCONFIG_REPORT_DESTS", columnName: "EX_RCONFIG_ID")
                columnExists(tableName: "EX_RCONFIG_REPORT_DESTS", columnName: "REPORT_DESTINATION")
            }
            not {
                primaryKeyExists(tableName: "EX_RCONFIG_REPORT_DESTS")
            }
        }
        sql("DELETE FROM EX_RCONFIG_REPORT_DESTS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_RCONFIG_ID, REPORT_DESTINATION ORDER BY ROWID) AS row_num " +
            "FROM EX_RCONFIG_REPORT_DESTS) WHERE row_num > 1)")
        sql("DELETE FROM EX_RCONFIG_REPORT_DESTS WHERE REPORT_DESTINATION IS NULL")
        addNotNullConstraint(tableName: "EX_RCONFIG_REPORT_DESTS", columnName: "REPORT_DESTINATION")
        addPrimaryKey(columnNames: "EX_RCONFIG_ID, REPORT_DESTINATION", constraintName: "EX_RCONFIG_RPT_DESTS_PK", tableName: "EX_RCONFIG_REPORT_DESTS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-57") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_STATUSES_RPT_FORMATS")
            and {
                columnExists(tableName: "EX_STATUSES_RPT_FORMATS", columnName: "EX_STATUS_ID")
                columnExists(tableName: "EX_STATUSES_RPT_FORMATS", columnName: "RPT_FORMAT")
            }
            not {
                primaryKeyExists(tableName: "EX_STATUSES_RPT_FORMATS")
            }
        }
        sql("DELETE FROM EX_STATUSES_RPT_FORMATS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_STATUS_ID, RPT_FORMAT ORDER BY ROWID) AS row_num " +
            "FROM EX_STATUSES_RPT_FORMATS) WHERE row_num > 1)")
        sql("DELETE FROM EX_STATUSES_RPT_FORMATS WHERE RPT_FORMAT IS NULL")
        addNotNullConstraint(tableName: "EX_STATUSES_RPT_FORMATS", columnName: "RPT_FORMAT")
        addPrimaryKey(columnNames: "EX_STATUS_ID, RPT_FORMAT", constraintName: "EX_STATS_RPT_FRMATS_PK", tableName: "EX_STATUSES_RPT_FORMATS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-58") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_STATUSES_SHARED_WITHS")
            and {
                columnExists(tableName: "EX_STATUSES_SHARED_WITHS", columnName: "EX_STATUS_ID")
                columnExists(tableName: "EX_STATUSES_SHARED_WITHS", columnName: "SHARED_WITH_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_STATUSES_SHARED_WITHS")
            }
        }
        sql("DELETE FROM EX_STATUSES_SHARED_WITHS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_STATUS_ID, SHARED_WITH_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_STATUSES_SHARED_WITHS) WHERE row_num > 1)")
        sql("DELETE FROM EX_STATUSES_SHARED_WITHS WHERE SHARED_WITH_ID IS NULL")
        addNotNullConstraint(tableName: "EX_STATUSES_SHARED_WITHS", columnName: "SHARED_WITH_ID")
        addPrimaryKey(columnNames: "EX_STATUS_ID, SHARED_WITH_ID", constraintName: "EX_STATS_SHRD_W_PK", tableName: "EX_STATUSES_SHARED_WITHS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-59") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES")
            and {
                columnExists(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", columnName: "EX_TEMPLT_QUERY_ID")
                columnExists(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", columnName: "EX_QUERY_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES")
            }
        }
        sql("DELETE FROM EX_TEMPLT_QRS_EX_QUERY_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_TEMPLT_QUERY_ID, EX_QUERY_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_TEMPLT_QRS_EX_QUERY_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM EX_TEMPLT_QRS_EX_QUERY_VALUES WHERE EX_QUERY_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", columnName: "EX_QUERY_VALUE_ID")
        addPrimaryKey(columnNames: "EX_TEMPLT_QUERY_ID, EX_QUERY_VALUE_ID", constraintName: "EX_TEMPLT_QRS_EX_QRY_VAL_PK", tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-60") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES")
            and {
                columnExists(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", columnName: "EX_TEMPLT_QUERY_ID")
                columnExists(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", columnName: "EX_TEMPLT_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES")
            }
        }
        sql("DELETE FROM EX_TEMPLT_QRS_EX_TEMPLT_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY EX_TEMPLT_QUERY_ID, EX_TEMPLT_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM EX_TEMPLT_QRS_EX_TEMPLT_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM EX_TEMPLT_QRS_EX_TEMPLT_VALUES WHERE EX_TEMPLT_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", columnName: "EX_TEMPLT_VALUE_ID")
        addPrimaryKey(columnNames: "EX_TEMPLT_QUERY_ID, EX_TEMPLT_VALUE_ID", constraintName: "EX_TMPLT_QRS_EX_TMPLT_VAL_PK", tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-61-1") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "FIELD_PROFILE_RPT_FIELD")
            not {
                tableExists(tableName: "FIELD_PROFILE_RPT_FIELD_BKP")
            }
        }
        renameTable(oldTableName: "FIELD_PROFILE_RPT_FIELD", newTableName: "FIELD_PROFILE_RPT_FIELD_BKP")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-61-2") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "FIELD_PROFILE_RPT_FIELD_BKP")
            and {
                columnExists(tableName: "FIELD_PROFILE_RPT_FIELD_BKP", columnName: "FIELD_PROFILE_REPORT_FIELDS_ID")
                columnExists(tableName: "FIELD_PROFILE_RPT_FIELD_BKP", columnName: "REPORT_FIELD_ID")
            }
            not {
                or {
                    tableExists(tableName: "FIELD_PROFILE_RPT_FIELD")
                    tableExists(tableName: "FIELD_PROFILE_BLINDED_FIELD")
                    tableExists(tableName: "FIELD_PROFILE_PROTECTED_FIELD")
                    primaryKeyExists(tableName: "FIELD_PROFILE_RPT_FIELD")
                    primaryKeyExists(tableName: "FIELD_PROFILE_BLINDED_FIELD")
                    primaryKeyExists(tableName: "FIELD_PROFILE_PROTECTED_FIELD")
                }
            }
        }
        
        sql("CREATE TABLE FIELD_PROFILE_RPT_FIELD AS (SELECT FIELD_PROFILE_REPORT_FIELDS_ID AS FIELD_PROFILE_ID, REPORT_FIELD_ID FROM FIELD_PROFILE_RPT_FIELD_BKP WHERE FIELD_PROFILE_REPORT_FIELDS_ID IS NOT NULL)")
        sql("DELETE FROM FIELD_PROFILE_RPT_FIELD WHERE ROWID IN (" +
                "SELECT ROWID FROM (" +
                "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY FIELD_PROFILE_ID, REPORT_FIELD_ID ORDER BY ROWID) AS row_num " +
                "FROM FIELD_PROFILE_RPT_FIELD) WHERE row_num > 1)")
        sql("DELETE FROM FIELD_PROFILE_RPT_FIELD WHERE FIELD_PROFILE_ID IS NULL OR REPORT_FIELD_ID IS NULL")
        addNotNullConstraint(tableName: "FIELD_PROFILE_RPT_FIELD", columnName: "FIELD_PROFILE_ID")
        addNotNullConstraint(tableName: "FIELD_PROFILE_RPT_FIELD", columnName: "REPORT_FIELD_ID")
        
        sql("CREATE TABLE FIELD_PROFILE_BLINDED_FIELD AS (SELECT FIELD_PROFILE_BLINDED_ID AS FIELD_PROFILE_ID, REPORT_FIELD_ID AS BLINDED_FIELD_ID FROM FIELD_PROFILE_RPT_FIELD_BKP WHERE FIELD_PROFILE_BLINDED_ID IS NOT NULL)")
        sql("DELETE FROM FIELD_PROFILE_BLINDED_FIELD WHERE ROWID IN (" +
                "SELECT ROWID FROM (" +
                "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY FIELD_PROFILE_ID, BLINDED_FIELD_ID ORDER BY ROWID) AS row_num " +
                "FROM FIELD_PROFILE_BLINDED_FIELD) WHERE row_num > 1)")
        sql("DELETE FROM FIELD_PROFILE_BLINDED_FIELD WHERE FIELD_PROFILE_ID IS NULL OR BLINDED_FIELD_ID IS NULL")
        addNotNullConstraint(tableName: "FIELD_PROFILE_BLINDED_FIELD", columnName: "FIELD_PROFILE_ID")
        addNotNullConstraint(tableName: "FIELD_PROFILE_BLINDED_FIELD", columnName: "BLINDED_FIELD_ID")
        
        sql("CREATE TABLE FIELD_PROFILE_PROTECTED_FIELD AS (SELECT FIELD_PROFILE_PROTECTED_ID AS FIELD_PROFILE_ID, REPORT_FIELD_ID AS PROTECTED_FIELD_ID FROM FIELD_PROFILE_RPT_FIELD_BKP WHERE FIELD_PROFILE_PROTECTED_ID IS NOT NULL)")
        sql("DELETE FROM FIELD_PROFILE_PROTECTED_FIELD WHERE ROWID IN (" +
                "SELECT ROWID FROM (" +
                "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY FIELD_PROFILE_ID, PROTECTED_FIELD_ID ORDER BY ROWID) AS row_num " +
                "FROM FIELD_PROFILE_PROTECTED_FIELD) WHERE row_num > 1)")
        sql("DELETE FROM FIELD_PROFILE_PROTECTED_FIELD WHERE FIELD_PROFILE_ID IS NULL OR PROTECTED_FIELD_ID IS NULL")
        addNotNullConstraint(tableName: "FIELD_PROFILE_PROTECTED_FIELD", columnName: "FIELD_PROFILE_ID")
        addNotNullConstraint(tableName: "FIELD_PROFILE_PROTECTED_FIELD", columnName: "PROTECTED_FIELD_ID")
        
        addPrimaryKey(columnNames: "FIELD_PROFILE_ID, REPORT_FIELD_ID", constraintName: "FIELD_PRFL_RPT_FIELD_PK", tableName: "FIELD_PROFILE_RPT_FIELD")
        addPrimaryKey(columnNames: "FIELD_PROFILE_ID, BLINDED_FIELD_ID", constraintName: "FIELD_PRFL_BLND_FIELD_PK", tableName: "FIELD_PROFILE_BLINDED_FIELD")
        addPrimaryKey(columnNames: "FIELD_PROFILE_ID, PROTECTED_FIELD_ID", constraintName: "FIELD_PRFL_PRT_FIELD_PK", tableName: "FIELD_PROFILE_PROTECTED_FIELD")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-62") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "GLOBAL_QUERY_VALUES")
            and {
                columnExists(tableName: "GLOBAL_QUERY_VALUES", columnName: "RCONFIG_ID")
                columnExists(tableName: "GLOBAL_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "GLOBAL_QUERY_VALUES")
            }
        }
        sql("DELETE FROM GLOBAL_QUERY_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCONFIG_ID, QUERY_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM GLOBAL_QUERY_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM GLOBAL_QUERY_VALUES WHERE QUERY_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "GLOBAL_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
        addPrimaryKey(columnNames: "RCONFIG_ID, QUERY_VALUE_ID", constraintName: "GLOBAL_QRY_VAL_PK", tableName: "GLOBAL_QUERY_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-63") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "INBOUND_COMPLIANCE_TAGS")
            and {
                columnExists(tableName: "INBOUND_COMPLIANCE_TAGS", columnName: "INBOUND_COMPLIANCE_ID")
                columnExists(tableName: "INBOUND_COMPLIANCE_TAGS", columnName: "TAG_ID")
            }
            not {
                primaryKeyExists(tableName: "INBOUND_COMPLIANCE_TAGS")
            }
        }
        sql("DELETE FROM INBOUND_COMPLIANCE_TAGS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY INBOUND_COMPLIANCE_ID, TAG_ID ORDER BY ROWID) AS row_num " +
            "FROM INBOUND_COMPLIANCE_TAGS) WHERE row_num > 1)")
        sql("DELETE FROM INBOUND_COMPLIANCE_TAGS WHERE TAG_ID IS NULL")
        addNotNullConstraint(tableName: "INBOUND_COMPLIANCE_TAGS", columnName: "TAG_ID")
        addPrimaryKey(columnNames: "INBOUND_COMPLIANCE_ID, TAG_ID", constraintName: "INB_COMP_TAGS_PK", tableName: "INBOUND_COMPLIANCE_TAGS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-64") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "INBOUND_POI_PARAMS")
            and {
                columnExists(tableName: "INBOUND_POI_PARAMS", columnName: "INBOUND_COMPLIANCE_ID")
                columnExists(tableName: "INBOUND_POI_PARAMS", columnName: "PARAM_ID")
            }
            not {
                primaryKeyExists(tableName: "INBOUND_POI_PARAMS")
            }
        }
        sql("DELETE FROM INBOUND_POI_PARAMS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY INBOUND_COMPLIANCE_ID, PARAM_ID ORDER BY ROWID) AS row_num " +
            "FROM INBOUND_POI_PARAMS) WHERE row_num > 1)")
        sql("DELETE FROM INBOUND_POI_PARAMS WHERE PARAM_ID IS NULL")
        addNotNullConstraint(tableName: "INBOUND_POI_PARAMS", columnName: "PARAM_ID")
        addPrimaryKey(columnNames: "INBOUND_COMPLIANCE_ID, PARAM_ID", constraintName: "INB_POI_PARAMS_PK", tableName: "INBOUND_POI_PARAMS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-65") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES")
            and {
                columnExists(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", columnName: "NONCASE_SQL_TEMPLT_ID")
                columnExists(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", columnName: "SQL_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES")
            }
        }
        sql("DELETE FROM NONCASE_SQL_TEMPLTS_SQL_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY NONCASE_SQL_TEMPLT_ID, SQL_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM NONCASE_SQL_TEMPLTS_SQL_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM NONCASE_SQL_TEMPLTS_SQL_VALUES WHERE SQL_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", columnName: "SQL_VALUE_ID")
        addPrimaryKey(columnNames: "NONCASE_SQL_TEMPLT_ID, SQL_VALUE_ID", constraintName: "NONCASE_SQL_TMPLTS_SQL_VAL_PK", tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-66") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "PVUSER_PASSWORD_DIGESTS")
            and {
                columnExists(tableName: "PVUSER_PASSWORD_DIGESTS", columnName: "USER_ID")
                columnExists(tableName: "PVUSER_PASSWORD_DIGESTS", columnName: "PASSWORD_DIGESTS_STRING")
            }
            not {
                primaryKeyExists(tableName: "PVUSER_PASSWORD_DIGESTS")
            }
        }
        sql("DELETE FROM PVUSER_PASSWORD_DIGESTS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY USER_ID, PASSWORD_DIGESTS_STRING ORDER BY ROWID) AS row_num " +
            "FROM PVUSER_PASSWORD_DIGESTS) WHERE row_num > 1)")
        sql("DELETE FROM PVUSER_PASSWORD_DIGESTS WHERE PASSWORD_DIGESTS_STRING IS NULL")
        addNotNullConstraint(tableName: "PVUSER_PASSWORD_DIGESTS", columnName: "PASSWORD_DIGESTS_STRING")
        addPrimaryKey(columnNames: "USER_ID, PASSWORD_DIGESTS_STRING", constraintName: "PVUSER_PSSWRD_DGSTS_PK", tableName: "PVUSER_PASSWORD_DIGESTS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-67") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "PVUSER_TENANTS")
            and {
                columnExists(tableName: "PVUSER_TENANTS", columnName: "PVUSER_ID")
                columnExists(tableName: "PVUSER_TENANTS", columnName: "TENANT_ID")
            }
            not {
                primaryKeyExists(tableName: "PVUSER_TENANTS")
            }
        }
        sql("DELETE FROM PVUSER_TENANTS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY PVUSER_ID, TENANT_ID ORDER BY ROWID) AS row_num " +
            "FROM PVUSER_TENANTS) WHERE row_num > 1)")
        sql("DELETE FROM PVUSER_TENANTS WHERE TENANT_ID IS NULL")
        addNotNullConstraint(tableName: "PVUSER_TENANTS", columnName: "TENANT_ID")
        addPrimaryKey(columnNames: "PVUSER_ID, TENANT_ID", constraintName: "PVUSER_TENANTS_PK", tableName: "PVUSER_TENANTS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-68") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "QRS_COMPLIANCE_QUERY_VALUES")
            and {
                columnExists(tableName: "QRS_COMPLIANCE_QUERY_VALUES", columnName: "QUERY_COMPLIANCE_ID")
                columnExists(tableName: "QRS_COMPLIANCE_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "QRS_COMPLIANCE_QUERY_VALUES")
            }
        }
        sql("DELETE FROM QRS_COMPLIANCE_QUERY_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY QUERY_COMPLIANCE_ID, QUERY_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM QRS_COMPLIANCE_QUERY_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM QRS_COMPLIANCE_QUERY_VALUES WHERE QUERY_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "QRS_COMPLIANCE_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
        addPrimaryKey(columnNames: "QUERY_COMPLIANCE_ID, QUERY_VALUE_ID", constraintName: "QRS_COMP_QRY_VAL_PK", tableName: "QRS_COMPLIANCE_QUERY_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-69") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "QRS_RCA_QUERY_VALUES")
            and {
                columnExists(tableName: "QRS_RCA_QUERY_VALUES", columnName: "QUERY_RCA_ID")
                columnExists(tableName: "QRS_RCA_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "QRS_RCA_QUERY_VALUES")
            }
        }
        sql("DELETE FROM QRS_RCA_QUERY_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY QUERY_RCA_ID, QUERY_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM QRS_RCA_QUERY_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM QRS_RCA_QUERY_VALUES WHERE QUERY_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "QRS_RCA_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
        addPrimaryKey(columnNames: "QUERY_RCA_ID, QUERY_VALUE_ID", constraintName: "QRS_RCA_QRY_VAL_PK", tableName: "QRS_RCA_QUERY_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-70") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "QUALITY_CASE_ISSUE_DETAILS")
            and {
                columnExists(tableName: "QUALITY_CASE_ISSUE_DETAILS", columnName: "QUALITY_CASE_ID")
                columnExists(tableName: "QUALITY_CASE_ISSUE_DETAILS", columnName: "QUALITY_ISSUE_DETAIL_ID")
            }
            not {
                primaryKeyExists(tableName: "QUALITY_CASE_ISSUE_DETAILS")
            }
        }
        sql("DELETE FROM QUALITY_CASE_ISSUE_DETAILS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY QUALITY_CASE_ID, QUALITY_ISSUE_DETAIL_ID ORDER BY ROWID) AS row_num " +
            "FROM QUALITY_CASE_ISSUE_DETAILS) WHERE row_num > 1)")
        sql("DELETE FROM QUALITY_CASE_ISSUE_DETAILS WHERE QUALITY_CASE_ID IS NULL OR QUALITY_ISSUE_DETAIL_ID IS NULL")
        addNotNullConstraint(tableName: "QUALITY_CASE_ISSUE_DETAILS", columnName: "QUALITY_CASE_ID")
        addNotNullConstraint(tableName: "QUALITY_CASE_ISSUE_DETAILS", columnName: "QUALITY_ISSUE_DETAIL_ID")
        addPrimaryKey(columnNames: "QUALITY_CASE_ID, QUALITY_ISSUE_DETAIL_ID", constraintName: "QULTY_CASE_ISSUE_DTLS_PK", tableName: "QUALITY_CASE_ISSUE_DETAILS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-71") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "QUALITY_SAMPL_ISSUE_DETAILS")
            and {
                columnExists(tableName: "QUALITY_SAMPL_ISSUE_DETAILS", columnName: "QUALITY_SAMPLING_ID")
                columnExists(tableName: "QUALITY_SAMPL_ISSUE_DETAILS", columnName: "QUALITY_ISSUE_DETAIL_ID")
            }
            not {
                primaryKeyExists(tableName: "QUALITY_SAMPL_ISSUE_DETAILS")
            }
        }
        sql("DELETE FROM QUALITY_SAMPL_ISSUE_DETAILS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY QUALITY_SAMPLING_ID, QUALITY_ISSUE_DETAIL_ID ORDER BY ROWID) AS row_num " +
            "FROM QUALITY_SAMPL_ISSUE_DETAILS) WHERE row_num > 1)")
        sql("DELETE FROM QUALITY_SAMPL_ISSUE_DETAILS WHERE QUALITY_SAMPLING_ID IS NULL OR QUALITY_ISSUE_DETAIL_ID IS NULL")
        addNotNullConstraint(tableName: "QUALITY_SAMPL_ISSUE_DETAILS", columnName: "QUALITY_SAMPLING_ID")
        addNotNullConstraint(tableName: "QUALITY_SAMPL_ISSUE_DETAILS", columnName: "QUALITY_ISSUE_DETAIL_ID")
        addPrimaryKey(columnNames: "QUALITY_SAMPLING_ID, QUALITY_ISSUE_DETAIL_ID", constraintName: "QULTY_SAMPL_ISSUE_DTLS_PK", tableName: "QUALITY_SAMPL_ISSUE_DETAILS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-72") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "QUALITY_SUB_ISSUE_DETAILS")
            and {
                columnExists(tableName: "QUALITY_SUB_ISSUE_DETAILS", columnName: "QUALITY_SUBMISSION_ID")
                columnExists(tableName: "QUALITY_SUB_ISSUE_DETAILS", columnName: "QUALITY_ISSUE_DETAIL_ID")
            }
            not {
                primaryKeyExists(tableName: "QUALITY_SUB_ISSUE_DETAILS")
            }
        }
        sql("DELETE FROM QUALITY_SUB_ISSUE_DETAILS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY QUALITY_SUBMISSION_ID, QUALITY_ISSUE_DETAIL_ID ORDER BY ROWID) AS row_num " +
            "FROM QUALITY_SUB_ISSUE_DETAILS) WHERE row_num > 1)")
        sql("DELETE FROM QUALITY_SUB_ISSUE_DETAILS WHERE QUALITY_SUBMISSION_ID IS NULL OR QUALITY_ISSUE_DETAIL_ID IS NULL")
        addNotNullConstraint(tableName: "QUALITY_SUB_ISSUE_DETAILS", columnName: "QUALITY_SUBMISSION_ID")
        addNotNullConstraint(tableName: "QUALITY_SUB_ISSUE_DETAILS", columnName: "QUALITY_ISSUE_DETAIL_ID")
        addPrimaryKey(columnNames: "QUALITY_SUBMISSION_ID, QUALITY_ISSUE_DETAIL_ID", constraintName: "QULTY_SUB_ISSUE_DTLS_PK", tableName: "QUALITY_SUB_ISSUE_DETAILS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-73") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "QUERIES_QRS_EXP_VALUES")
            and {
                columnExists(tableName: "QUERIES_QRS_EXP_VALUES", columnName: "QUERY_ID")
                columnExists(tableName: "QUERIES_QRS_EXP_VALUES", columnName: "QUERY_EXP_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "QUERIES_QRS_EXP_VALUES")
            }
        }
        sql("DELETE FROM QUERIES_QRS_EXP_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY QUERY_ID, QUERY_EXP_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM QUERIES_QRS_EXP_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM QUERIES_QRS_EXP_VALUES WHERE QUERY_EXP_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "QUERIES_QRS_EXP_VALUES", columnName: "QUERY_EXP_VALUE_ID")
        addPrimaryKey(columnNames: "QUERY_ID, QUERY_EXP_VALUE_ID", constraintName: "QRS_QRS_EXP_VALS_PK", tableName: "QUERIES_QRS_EXP_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-74") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "QUERY_SETS_SUPER_QRS")
            and {
                columnExists(tableName: "QUERY_SETS_SUPER_QRS", columnName: "QUERY_SET_ID")
                columnExists(tableName: "QUERY_SETS_SUPER_QRS", columnName: "SUPER_QUERY_ID")
                columnExists(tableName: "QUERY_SETS_SUPER_QRS", columnName: "SUPER_QUERY_IDX")
            }
            not {
                primaryKeyExists(tableName: "QUERY_SETS_SUPER_QRS")
            }
        }
        sql("DELETE FROM QUERY_SETS_SUPER_QRS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY QUERY_SET_ID, SUPER_QUERY_ID, SUPER_QUERY_IDX ORDER BY ROWID) AS row_num " +
            "FROM QUERY_SETS_SUPER_QRS) WHERE row_num > 1)")
        sql("DELETE FROM QUERY_SETS_SUPER_QRS WHERE SUPER_QUERY_ID IS NULL OR SUPER_QUERY_IDX IS NULL")
        addNotNullConstraint(tableName: "QUERY_SETS_SUPER_QRS", columnName: "SUPER_QUERY_ID")
        addNotNullConstraint(tableName: "QUERY_SETS_SUPER_QRS", columnName: "SUPER_QUERY_IDX")
        addPrimaryKey(columnNames: "QUERY_SET_ID, SUPER_QUERY_ID, SUPER_QUERY_IDX", constraintName: "QRY_SETS_SUP_QRS_PK", tableName: "QUERY_SETS_SUPER_QRS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504041215-75") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RCA_EDITABLE_WFS")
            and {
                columnExists(tableName: "RCA_EDITABLE_WFS", columnName: "RCA_MANDATORY_ID")
                columnExists(tableName: "RCA_EDITABLE_WFS", columnName: "WFS_ID")
            }
            not {
                primaryKeyExists(tableName: "RCA_EDITABLE_WFS")
            }
        }
        sql("DELETE FROM RCA_EDITABLE_WFS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCA_MANDATORY_ID, WFS_ID ORDER BY ROWID) AS row_num " +
            "FROM RCA_EDITABLE_WFS) WHERE row_num > 1)")
        addPrimaryKey(columnNames: "RCA_MANDATORY_ID, WFS_ID", constraintName: "RCA_EDITABLE_WFS_PK", tableName: "RCA_EDITABLE_WFS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504041215-76") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RCA_EDIT_USERS")
            and {
                columnExists(tableName: "RCA_EDIT_USERS", columnName: "RCA_MANDATORY_ID")
                columnExists(tableName: "RCA_EDIT_USERS", columnName: "USER_ID")
            }
            not {
                primaryKeyExists(tableName: "RCA_EDIT_USERS")
            }
        }
        sql("DELETE FROM RCA_EDIT_USERS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCA_MANDATORY_ID, USER_ID ORDER BY ROWID) AS row_num " +
            "FROM RCA_EDIT_USERS) WHERE row_num > 1)")
        addPrimaryKey(columnNames: "RCA_MANDATORY_ID, USER_ID", constraintName: "RCA_EDIT_USERS_PK", tableName: "RCA_EDIT_USERS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504041215-77") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RCA_EDIT_USRGRPS")
            and {
                columnExists(tableName: "RCA_EDIT_USRGRPS", columnName: "RCA_MANDATORY_ID")
                columnExists(tableName: "RCA_EDIT_USRGRPS", columnName: "USER_GRP_ID")
            }
            not {
                primaryKeyExists(tableName: "RCA_EDIT_USRGRPS")
            }
        }
        sql("DELETE FROM RCA_EDIT_USRGRPS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCA_MANDATORY_ID, USER_GRP_ID ORDER BY ROWID) AS row_num " +
            "FROM RCA_EDIT_USRGRPS) WHERE row_num > 1)")
        addPrimaryKey(columnNames: "RCA_MANDATORY_ID, USER_GRP_ID", constraintName: "RCA_EDIT_USRGRPS_PK", tableName: "RCA_EDIT_USRGRPS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504041215-78") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RCA_MANDATORY_FIELDS")
            and {
                columnExists(tableName: "RCA_MANDATORY_FIELDS", columnName: "ID")
            }
            not {
                primaryKeyExists(tableName: "RCA_MANDATORY_FIELDS")
            }
        }
        addPrimaryKey(columnNames: "ID", constraintName: "RCA_MNDTRY_FIELDS_PK", tableName: "RCA_MANDATORY_FIELDS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504041215-79") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RCA_MANDATORY_WFS")
            and {
                columnExists(tableName: "RCA_MANDATORY_WFS", columnName: "RCA_MANDATORY_ID")
                columnExists(tableName: "RCA_MANDATORY_WFS", columnName: "WFS_ID")
            }
            not {
                primaryKeyExists(tableName: "RCA_MANDATORY_WFS")
            }
        }
        sql("DELETE FROM RCA_MANDATORY_WFS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCA_MANDATORY_ID, WFS_ID ORDER BY ROWID) AS row_num " +
            "FROM RCA_MANDATORY_WFS) WHERE row_num > 1)")
        addPrimaryKey(columnNames: "RCA_MANDATORY_ID, WFS_ID", constraintName: "RCA_MANDATORY_WFS_PK", tableName: "RCA_MANDATORY_WFS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-80") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RCONFIGS_POI_PARAMS")
            and {
                columnExists(tableName: "RCONFIGS_POI_PARAMS", columnName: "RCONFIG_ID")
                columnExists(tableName: "RCONFIGS_POI_PARAMS", columnName: "PARAM_ID")
            }
            not {
                primaryKeyExists(tableName: "RCONFIGS_POI_PARAMS")
            }
        }
        sql("DELETE FROM RCONFIGS_POI_PARAMS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCONFIG_ID, PARAM_ID ORDER BY ROWID) AS row_num " +
            "FROM RCONFIGS_POI_PARAMS) WHERE row_num > 1)")
        sql("DELETE FROM RCONFIGS_POI_PARAMS WHERE PARAM_ID IS NULL")
        addNotNullConstraint(tableName: "RCONFIGS_POI_PARAMS", columnName: "PARAM_ID")
        addPrimaryKey(columnNames: "RCONFIG_ID, PARAM_ID", constraintName: "RCONFIGS_POI_PARAMS_PK", tableName: "RCONFIGS_POI_PARAMS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-81") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RCONFIGS_TAGS")
            and {
                columnExists(tableName: "RCONFIGS_TAGS", columnName: "RCONFIG_ID")
                columnExists(tableName: "RCONFIGS_TAGS", columnName: "TAG_ID")
            }
            not {
                primaryKeyExists(tableName: "RCONFIGS_TAGS")
            }
        }
        sql("DELETE FROM RCONFIGS_TAGS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCONFIG_ID, TAG_ID ORDER BY ROWID) AS row_num " +
            "FROM RCONFIGS_TAGS) WHERE row_num > 1)")
        sql("DELETE FROM RCONFIGS_TAGS WHERE TAG_ID IS NULL")
        addNotNullConstraint(tableName: "RCONFIGS_TAGS", columnName: "TAG_ID")
        addPrimaryKey(columnNames: "RCONFIG_ID, TAG_ID", constraintName: "RCONFIGS_TAGS_PK", tableName: "RCONFIGS_TAGS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-82") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RCONFIG_P_C_USERS")
            and {
                columnExists(tableName: "RCONFIG_P_C_USERS", columnName: "RCONFIG_ID")
                columnExists(tableName: "RCONFIG_P_C_USERS", columnName: "USER_ID")
            }
            not {
                primaryKeyExists(tableName: "RCONFIG_P_C_USERS")
            }
        }
        sql("DELETE FROM RCONFIG_P_C_USERS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCONFIG_ID, USER_ID ORDER BY ROWID) AS row_num " +
            "FROM RCONFIG_P_C_USERS) WHERE row_num > 1)")
        sql("DELETE FROM RCONFIG_P_C_USERS WHERE USER_ID IS NULL")
        addNotNullConstraint(tableName: "RCONFIG_P_C_USERS", columnName: "USER_ID")
        addPrimaryKey(columnNames: "RCONFIG_ID, USER_ID", constraintName: "RCONFIG_P_C_USERS_PK", tableName: "RCONFIG_P_C_USERS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-83") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RCONFIG_REPORT_DESTS")
            and {
                columnExists(tableName: "RCONFIG_REPORT_DESTS", columnName: "RCONFIG_ID")
                columnExists(tableName: "RCONFIG_REPORT_DESTS", columnName: "REPORT_DESTINATION")
            }
            not {
                primaryKeyExists(tableName: "RCONFIG_REPORT_DESTS")
            }
        }
        sql("DELETE FROM RCONFIG_REPORT_DESTS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RCONFIG_ID, REPORT_DESTINATION ORDER BY ROWID) AS row_num " +
            "FROM RCONFIG_REPORT_DESTS) WHERE row_num > 1)")
        sql("DELETE FROM RCONFIG_REPORT_DESTS WHERE REPORT_DESTINATION IS NULL")
        addNotNullConstraint(tableName: "RCONFIG_REPORT_DESTS", columnName: "REPORT_DESTINATION")
        addPrimaryKey(columnNames: "RCONFIG_ID, REPORT_DESTINATION", constraintName: "RCONFIG_RPT_DESTS_PK", tableName: "RCONFIG_REPORT_DESTS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-84") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "REPORT_REQUEST_ACTION_ITEM")
            and {
                columnExists(tableName: "REPORT_REQUEST_ACTION_ITEM", columnName: "REPORT_REQUEST_ACTION_ITEMS_ID")
                columnExists(tableName: "REPORT_REQUEST_ACTION_ITEM", columnName: "ACTION_ITEM_ID")
            }
            not {
                primaryKeyExists(tableName: "REPORT_REQUEST_ACTION_ITEM")
            }
        }
        sql("DELETE FROM REPORT_REQUEST_ACTION_ITEM WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY REPORT_REQUEST_ACTION_ITEMS_ID, ACTION_ITEM_ID ORDER BY ROWID) AS row_num " +
            "FROM REPORT_REQUEST_ACTION_ITEM) WHERE row_num > 1)")
        sql("DELETE FROM REPORT_REQUEST_ACTION_ITEM WHERE REPORT_REQUEST_ACTION_ITEMS_ID IS NULL OR ACTION_ITEM_ID IS NULL")
        addNotNullConstraint(tableName: "REPORT_REQUEST_ACTION_ITEM", columnName: "REPORT_REQUEST_ACTION_ITEMS_ID")
        addNotNullConstraint(tableName: "REPORT_REQUEST_ACTION_ITEM", columnName: "ACTION_ITEM_ID")
        addPrimaryKey(columnNames: "REPORT_REQUEST_ACTION_ITEMS_ID, ACTION_ITEM_ID", constraintName: "RPT_REQ_ACTN_ITEM_PK", tableName: "REPORT_REQUEST_ACTION_ITEM")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-85") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "REPORT_REQUEST_DESTS")
            and {
                columnExists(tableName: "REPORT_REQUEST_DESTS", columnName: "REPORT_REQUEST_ID")
                columnExists(tableName: "REPORT_REQUEST_DESTS", columnName: "REPORT_DESTINATION")
            }
            not {
                primaryKeyExists(tableName: "REPORT_REQUEST_DESTS")
            }
        }
        sql("DELETE FROM REPORT_REQUEST_DESTS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY REPORT_REQUEST_ID, REPORT_DESTINATION ORDER BY ROWID) AS row_num " +
            "FROM REPORT_REQUEST_DESTS) WHERE row_num > 1)")
        sql("DELETE FROM REPORT_REQUEST_DESTS WHERE REPORT_DESTINATION IS NULL")
        addNotNullConstraint(tableName: "REPORT_REQUEST_DESTS", columnName: "REPORT_DESTINATION")
        addPrimaryKey(columnNames: "REPORT_REQUEST_ID, REPORT_DESTINATION", constraintName: "RPT_REQ_DESTS_PK", tableName: "REPORT_REQUEST_DESTS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-86") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "REPORT_REQUEST_PVUSER")
            and {
                columnExists(tableName: "REPORT_REQUEST_PVUSER", columnName: "REPORT_REQUEST_REQUESTERS_ID")
                columnExists(tableName: "REPORT_REQUEST_PVUSER", columnName: "USER_ID")
            }
            not {
                primaryKeyExists(tableName: "REPORT_REQUEST_PVUSER")
            }
        }
        sql("DELETE FROM REPORT_REQUEST_PVUSER WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY REPORT_REQUEST_REQUESTERS_ID, USER_ID ORDER BY ROWID) AS row_num " +
            "FROM REPORT_REQUEST_PVUSER) WHERE row_num > 1)")
        sql("DELETE FROM REPORT_REQUEST_PVUSER WHERE REPORT_REQUEST_REQUESTERS_ID IS NULL OR USER_ID IS NULL")
        addNotNullConstraint(tableName: "REPORT_REQUEST_PVUSER", columnName: "REPORT_REQUEST_REQUESTERS_ID")
        addNotNullConstraint(tableName: "REPORT_REQUEST_PVUSER", columnName: "USER_ID")
        addPrimaryKey(columnNames: "REPORT_REQUEST_REQUESTERS_ID, USER_ID", constraintName: "RPT_REQ_PVUSER_PK", tableName: "REPORT_REQUEST_PVUSER")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-87") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "REPORT_REQUEST_USER_GROUP")
            and {
                columnExists(tableName: "REPORT_REQUEST_USER_GROUP", columnName: "REQUESTOR_GROUPS")
                columnExists(tableName: "REPORT_REQUEST_USER_GROUP", columnName: "USER_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "REPORT_REQUEST_USER_GROUP")
            }
        }
        sql("DELETE FROM REPORT_REQUEST_USER_GROUP WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY REQUESTOR_GROUPS, USER_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM REPORT_REQUEST_USER_GROUP) WHERE row_num > 1)")
        sql("DELETE FROM REPORT_REQUEST_USER_GROUP WHERE REQUESTOR_GROUPS IS NULL OR USER_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "REPORT_REQUEST_USER_GROUP", columnName: "REQUESTOR_GROUPS")
        addNotNullConstraint(tableName: "REPORT_REQUEST_USER_GROUP", columnName: "USER_GROUP_ID")
        addPrimaryKey(columnNames: "REQUESTOR_GROUPS, USER_GROUP_ID", constraintName: "RPT_REQ_USER_GRP_PK", tableName: "REPORT_REQUEST_USER_GROUP")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-88") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "REPORT_REQ_P_C_USERS")
            and {
                columnExists(tableName: "REPORT_REQ_P_C_USERS", columnName: "REPORT_REQ_ID")
                columnExists(tableName: "REPORT_REQ_P_C_USERS", columnName: "USER_ID")
            }
            not {
                primaryKeyExists(tableName: "REPORT_REQ_P_C_USERS")
            }
        }
        sql("DELETE FROM REPORT_REQ_P_C_USERS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY REPORT_REQ_ID, USER_ID ORDER BY ROWID) AS row_num " +
            "FROM REPORT_REQ_P_C_USERS) WHERE row_num > 1)")
        sql("DELETE FROM REPORT_REQ_P_C_USERS WHERE USER_ID IS NULL")
        addNotNullConstraint(tableName: "REPORT_REQ_P_C_USERS", columnName: "USER_ID")
        addPrimaryKey(columnNames: "REPORT_REQ_ID, USER_ID", constraintName: "RPT_REQ_P_C_USRS_PK", tableName: "REPORT_REQ_P_C_USERS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-89") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RPT_RESULT_COMMENT_TABLE")
            and {
                columnExists(tableName: "RPT_RESULT_COMMENT_TABLE", columnName: "REPORT_RESULT_COMMENTS_ID")
                columnExists(tableName: "RPT_RESULT_COMMENT_TABLE", columnName: "COMMENT_ID")
            }
            not {
                primaryKeyExists(tableName: "RPT_RESULT_COMMENT_TABLE")
            }
        }
        sql("DELETE FROM RPT_RESULT_COMMENT_TABLE WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY REPORT_RESULT_COMMENTS_ID, COMMENT_ID ORDER BY ROWID) AS row_num " +
            "FROM RPT_RESULT_COMMENT_TABLE) WHERE row_num > 1)")
        sql("DELETE FROM RPT_RESULT_COMMENT_TABLE WHERE REPORT_RESULT_COMMENTS_ID IS NULL OR COMMENT_ID IS NULL")
        addNotNullConstraint(tableName: "RPT_RESULT_COMMENT_TABLE", columnName: "REPORT_RESULT_COMMENTS_ID")
        addNotNullConstraint(tableName: "RPT_RESULT_COMMENT_TABLE", columnName: "COMMENT_ID")
        addPrimaryKey(columnNames: "REPORT_RESULT_COMMENTS_ID, COMMENT_ID", constraintName: "RPT_RSLT_COMMNT_TBL_PK", tableName: "RPT_RESULT_COMMENT_TABLE")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-90") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "RPT_TEMPLTS_TAGS")
            and {
                columnExists(tableName: "RPT_TEMPLTS_TAGS", columnName: "RPT_TEMPLT_ID")
                columnExists(tableName: "RPT_TEMPLTS_TAGS", columnName: "TAG_ID")
            }
            not {
                primaryKeyExists(tableName: "RPT_TEMPLTS_TAGS")
            }
        }
        sql("DELETE FROM RPT_TEMPLTS_TAGS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY RPT_TEMPLT_ID, TAG_ID ORDER BY ROWID) AS row_num " +
            "FROM RPT_TEMPLTS_TAGS) WHERE row_num > 1)")
        sql("DELETE FROM RPT_TEMPLTS_TAGS WHERE TAG_ID IS NULL")
        addNotNullConstraint(tableName: "RPT_TEMPLTS_TAGS", columnName: "TAG_ID")
        addPrimaryKey(columnNames: "RPT_TEMPLT_ID, TAG_ID", constraintName: "RPT_TEMPLTS_TAGS_PK", tableName: "RPT_TEMPLTS_TAGS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-91") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "SOURCE_COLUMN_MASTER")
            and {
                columnExists(tableName: "SOURCE_COLUMN_MASTER", columnName: "REPORT_ITEM")
                columnExists(tableName: "SOURCE_COLUMN_MASTER", columnName: "LANG_ID")
            }
            not {
                primaryKeyExists(tableName: "SOURCE_COLUMN_MASTER")
            }
        }
        sql("UPDATE SOURCE_COLUMN_MASTER SET LANG_ID = '*' WHERE LANG_ID IS NULL")
        addNotNullConstraint(tableName: "SOURCE_COLUMN_MASTER", columnName: "LANG_ID")
        addPrimaryKey(columnNames: "REPORT_ITEM, LANG_ID", constraintName: "SOURCE_COL_MSTR_PK", tableName: "SOURCE_COLUMN_MASTER")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-92") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "SQL_QRS_SQL_VALUES")
            and {
                columnExists(tableName: "SQL_QRS_SQL_VALUES", columnName: "SQL_QUERY_ID")
                columnExists(tableName: "SQL_QRS_SQL_VALUES", columnName: "SQL_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "SQL_QRS_SQL_VALUES")
            }
        }
        sql("DELETE FROM SQL_QRS_SQL_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY SQL_QUERY_ID, SQL_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM SQL_QRS_SQL_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM SQL_QRS_SQL_VALUES WHERE SQL_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "SQL_QRS_SQL_VALUES", columnName: "SQL_VALUE_ID")
        addPrimaryKey(columnNames: "SQL_QUERY_ID, SQL_VALUE_ID", constraintName: "SQL_QRS_SQL_VALUES_PK", tableName: "SQL_QRS_SQL_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-93") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "SQL_TEMPLTS_SQL_VALUES")
            and {
                columnExists(tableName: "SQL_TEMPLTS_SQL_VALUES", columnName: "SQL_TEMPLT_ID")
                columnExists(tableName: "SQL_TEMPLTS_SQL_VALUES", columnName: "SQL_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "SQL_TEMPLTS_SQL_VALUES")
            }
        }
        sql("DELETE FROM SQL_TEMPLTS_SQL_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY SQL_TEMPLT_ID, SQL_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM SQL_TEMPLTS_SQL_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM SQL_TEMPLTS_SQL_VALUES WHERE SQL_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "SQL_TEMPLTS_SQL_VALUES", columnName: "SQL_VALUE_ID")
        addPrimaryKey(columnNames: "SQL_TEMPLT_ID, SQL_VALUE_ID", constraintName: "SQL_TEMPLTS_SQL_VALS_PK", tableName: "SQL_TEMPLTS_SQL_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-94") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "SUPER_QRS_TAGS")
            and {
                columnExists(tableName: "SUPER_QRS_TAGS", columnName: "SUPER_QUERY_ID")
                columnExists(tableName: "SUPER_QRS_TAGS", columnName: "TAG_ID")
            }
            not {
                primaryKeyExists(tableName: "SUPER_QRS_TAGS")
            }
        }
        sql("DELETE FROM SUPER_QRS_TAGS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY SUPER_QUERY_ID, TAG_ID ORDER BY ROWID) AS row_num " +
            "FROM SUPER_QRS_TAGS) WHERE row_num > 1)")
        sql("DELETE FROM SUPER_QRS_TAGS WHERE TAG_ID IS NULL")
        addNotNullConstraint(tableName: "SUPER_QRS_TAGS", columnName: "TAG_ID")
        addPrimaryKey(columnNames: "SUPER_QUERY_ID, TAG_ID", constraintName: "SUPER_QRS_TAGS_PK", tableName: "SUPER_QRS_TAGS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-95") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "SYSTEM_NOTES_USER_GROUPS")
            and {
                columnExists(tableName: "SYSTEM_NOTES_USER_GROUPS", columnName: "SYS_NOTE_ID")
                columnExists(tableName: "SYSTEM_NOTES_USER_GROUPS", columnName: "USER_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "SYSTEM_NOTES_USER_GROUPS")
            }
        }
        sql("DELETE FROM SYSTEM_NOTES_USER_GROUPS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY SYS_NOTE_ID, USER_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM SYSTEM_NOTES_USER_GROUPS) WHERE row_num > 1)")
        sql("DELETE FROM SYSTEM_NOTES_USER_GROUPS WHERE USER_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "SYSTEM_NOTES_USER_GROUPS", columnName: "USER_GROUP_ID")
        addPrimaryKey(columnNames: "SYS_NOTE_ID, USER_GROUP_ID", constraintName: "SYS_NOTES_USR_GRPS_PK", tableName: "SYSTEM_NOTES_USER_GROUPS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-96") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "TEMPLT_QRS_QUERY_VALUES")
            and {
                columnExists(tableName: "TEMPLT_QRS_QUERY_VALUES", columnName: "TEMPLT_QUERY_ID")
                columnExists(tableName: "TEMPLT_QRS_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "TEMPLT_QRS_QUERY_VALUES")
            }
        }
        sql("DELETE FROM TEMPLT_QRS_QUERY_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY TEMPLT_QUERY_ID, QUERY_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM TEMPLT_QRS_QUERY_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM TEMPLT_QRS_QUERY_VALUES WHERE QUERY_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "TEMPLT_QRS_QUERY_VALUES", columnName: "QUERY_VALUE_ID")
        addPrimaryKey(columnNames: "TEMPLT_QUERY_ID, QUERY_VALUE_ID", constraintName: "TEMPLT_QRS_QRY_VALS_PK", tableName: "TEMPLT_QRS_QUERY_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-97") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "TEMPLT_QRS_TEMPLT_VALUES")
            and {
                columnExists(tableName: "TEMPLT_QRS_TEMPLT_VALUES", columnName: "TEMPLT_QUERY_ID")
                columnExists(tableName: "TEMPLT_QRS_TEMPLT_VALUES", columnName: "TEMPLT_VALUE_ID")
            }
            not {
                primaryKeyExists(tableName: "TEMPLT_QRS_TEMPLT_VALUES")
            }
        }
        sql("DELETE FROM TEMPLT_QRS_TEMPLT_VALUES WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY TEMPLT_QUERY_ID, TEMPLT_VALUE_ID ORDER BY ROWID) AS row_num " +
            "FROM TEMPLT_QRS_TEMPLT_VALUES) WHERE row_num > 1)")
        sql("DELETE FROM TEMPLT_QRS_TEMPLT_VALUES WHERE TEMPLT_VALUE_ID IS NULL")
        addNotNullConstraint(tableName: "TEMPLT_QRS_TEMPLT_VALUES", columnName: "TEMPLT_VALUE_ID")
        addPrimaryKey(columnNames: "TEMPLT_QUERY_ID, TEMPLT_VALUE_ID", constraintName: "TMPLT_QRS_TMPLT_VALS_PK", tableName: "TEMPLT_QRS_TEMPLT_VALUES")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-98") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "TEMPLT_SET_NESTED")
            and {
                columnExists(tableName: "TEMPLT_SET_NESTED", columnName: "TEMPLT_SET_ID")
                columnExists(tableName: "TEMPLT_SET_NESTED", columnName: "NESTED_TEMPLT_ID")
                columnExists(tableName: "TEMPLT_SET_NESTED", columnName: "NESTED_TEMPLT_IDX")
            }
            not {
                primaryKeyExists(tableName: "TEMPLT_SET_NESTED")
            }
        }
        sql("DELETE FROM TEMPLT_SET_NESTED WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY TEMPLT_SET_ID, NESTED_TEMPLT_ID, NESTED_TEMPLT_IDX ORDER BY ROWID) AS row_num " +
            "FROM TEMPLT_SET_NESTED) WHERE row_num > 1)")
        sql("DELETE FROM TEMPLT_SET_NESTED WHERE NESTED_TEMPLT_ID IS NULL OR NESTED_TEMPLT_IDX IS NULL")
        addNotNullConstraint(tableName: "TEMPLT_SET_NESTED", columnName: "NESTED_TEMPLT_ID")
        addNotNullConstraint(tableName: "TEMPLT_SET_NESTED", columnName: "NESTED_TEMPLT_IDX")
        addPrimaryKey(columnNames: "TEMPLT_SET_ID, NESTED_TEMPLT_ID, NESTED_TEMPLT_IDX", constraintName: "TEMPLT_SET_NESTED_PK", tableName: "TEMPLT_SET_NESTED")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-99") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "USER_GRP_SRC_PROFILE")
            and {
                columnExists(tableName: "USER_GRP_SRC_PROFILE", columnName: "USER_GROUP_ID")
                columnExists(tableName: "USER_GRP_SRC_PROFILE", columnName: "SRC_PROFILE_ID")
            }
            not {
                primaryKeyExists(tableName: "USER_GRP_SRC_PROFILE")
            }
        }
        sql("DELETE FROM USER_GRP_SRC_PROFILE WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY USER_GROUP_ID, SRC_PROFILE_ID ORDER BY ROWID) AS row_num " +
            "FROM USER_GRP_SRC_PROFILE) WHERE row_num > 1)")
        sql("DELETE FROM USER_GRP_SRC_PROFILE WHERE SRC_PROFILE_ID IS NULL")
        addNotNullConstraint(tableName: "USER_GRP_SRC_PROFILE", columnName: "SRC_PROFILE_ID")
        addPrimaryKey(columnNames: "USER_GROUP_ID, SRC_PROFILE_ID", constraintName: "USR_GRP_SRC_PRFL_PK", tableName: "USER_GRP_SRC_PROFILE")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-100") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "VALUES_PARAMS")
            and {
                columnExists(tableName: "VALUES_PARAMS", columnName: "VALUE_ID")
                columnExists(tableName: "VALUES_PARAMS", columnName: "PARAM_ID")
            }
            not {
                primaryKeyExists(tableName: "VALUES_PARAMS")
            }
        }
        sql("DELETE FROM VALUES_PARAMS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY VALUE_ID, PARAM_ID ORDER BY ROWID) AS row_num " +
            "FROM VALUES_PARAMS) WHERE row_num > 1)")
        sql("DELETE FROM VALUES_PARAMS WHERE PARAM_ID IS NULL")
        addNotNullConstraint(tableName: "VALUES_PARAMS", columnName: "PARAM_ID")
        addPrimaryKey(columnNames: "VALUE_ID, PARAM_ID", constraintName: "VALUES_PARAMS_PK", tableName: "VALUES_PARAMS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-101") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "WORKFLOWSTATE_ACT_EXTRS")
            and {
                columnExists(tableName: "WORKFLOWSTATE_ACT_EXTRS", columnName: "WORKFLOW_ID")
                columnExists(tableName: "WORKFLOWSTATE_ACT_EXTRS", columnName: "EXECUTOR_ID")
            }
            not {
                primaryKeyExists(tableName: "WORKFLOWSTATE_ACT_EXTRS")
            }
        }
        sql("DELETE FROM WORKFLOWSTATE_ACT_EXTRS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY WORKFLOW_ID, EXECUTOR_ID ORDER BY ROWID) AS row_num " +
            "FROM WORKFLOWSTATE_ACT_EXTRS) WHERE row_num > 1)")
        sql("DELETE FROM WORKFLOWSTATE_ACT_EXTRS WHERE EXECUTOR_ID IS NULL")
        addNotNullConstraint(tableName: "WORKFLOWSTATE_ACT_EXTRS", columnName: "EXECUTOR_ID")
        addPrimaryKey(columnNames: "WORKFLOW_ID, EXECUTOR_ID", constraintName: "WRKFLWSTATE_ACT_EXTRS_PK", tableName: "WORKFLOWSTATE_ACT_EXTRS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-102") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "WORKFLOWSTATE_ACT_EXTRS_GR")
            and {
                columnExists(tableName: "WORKFLOWSTATE_ACT_EXTRS_GR", columnName: "WORKFLOW_ID")
                columnExists(tableName: "WORKFLOWSTATE_ACT_EXTRS_GR", columnName: "EXECUTOR_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "WORKFLOWSTATE_ACT_EXTRS_GR")
            }
        }
        sql("DELETE FROM WORKFLOWSTATE_ACT_EXTRS_GR WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY WORKFLOW_ID, EXECUTOR_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM WORKFLOWSTATE_ACT_EXTRS_GR) WHERE row_num > 1)")
        sql("DELETE FROM WORKFLOWSTATE_ACT_EXTRS_GR WHERE EXECUTOR_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "WORKFLOWSTATE_ACT_EXTRS_GR", columnName: "EXECUTOR_GROUP_ID")
        addPrimaryKey(columnNames: "WORKFLOW_ID, EXECUTOR_GROUP_ID", constraintName: "WRKFLWSTATE_ACT_EXTRS_GR_PK", tableName: "WORKFLOWSTATE_ACT_EXTRS_GR")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-103") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "WORKFLOW_ASSIGNED_TO_USER")
            and {
                columnExists(tableName: "WORKFLOW_ASSIGNED_TO_USER", columnName: "WORKFLOW_ID")
                columnExists(tableName: "WORKFLOW_ASSIGNED_TO_USER", columnName: "ASSIGNED_TO_USER_ID")
            }
            not {
                primaryKeyExists(tableName: "WORKFLOW_ASSIGNED_TO_USER")
            }
        }
        sql("DELETE FROM WORKFLOW_ASSIGNED_TO_USER WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY WORKFLOW_ID, ASSIGNED_TO_USER_ID ORDER BY ROWID) AS row_num " +
            "FROM WORKFLOW_ASSIGNED_TO_USER) WHERE row_num > 1)")
        sql("DELETE FROM WORKFLOW_ASSIGNED_TO_USER WHERE ASSIGNED_TO_USER_ID IS NULL")
        addNotNullConstraint(tableName: "WORKFLOW_ASSIGNED_TO_USER", columnName: "ASSIGNED_TO_USER_ID")
        addPrimaryKey(columnNames: "WORKFLOW_ID, ASSIGNED_TO_USER_ID", constraintName: "WRKFLW_ASSGND_TO_USR_PK", tableName: "WORKFLOW_ASSIGNED_TO_USER")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-104") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "WORKFLOW_ASSIGNED_TO_USERGROUP")
            and {
                columnExists(tableName: "WORKFLOW_ASSIGNED_TO_USERGROUP", columnName: "WORKFLOW_ID")
                columnExists(tableName: "WORKFLOW_ASSIGNED_TO_USERGROUP", columnName: "ASSIGNED_TO_USERGROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "WORKFLOW_ASSIGNED_TO_USERGROUP")
            }
        }
        sql("DELETE FROM WORKFLOW_ASSIGNED_TO_USERGROUP WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY WORKFLOW_ID, ASSIGNED_TO_USERGROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM WORKFLOW_ASSIGNED_TO_USERGROUP) WHERE row_num > 1)")
        sql("DELETE FROM WORKFLOW_ASSIGNED_TO_USERGROUP WHERE ASSIGNED_TO_USERGROUP_ID IS NULL")
        addNotNullConstraint(tableName: "WORKFLOW_ASSIGNED_TO_USERGROUP", columnName: "ASSIGNED_TO_USERGROUP_ID")
        addPrimaryKey(columnNames: "WORKFLOW_ID, ASSIGNED_TO_USERGROUP_ID", constraintName: "WRKFLW_ASSGND_TO_USRGRP_PK", tableName: "WORKFLOW_ASSIGNED_TO_USERGROUP")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-105") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "WORKFLOW_EXECUTORS")
            and {
                columnExists(tableName: "WORKFLOW_EXECUTORS", columnName: "WORKFLOW_ID")
                columnExists(tableName: "WORKFLOW_EXECUTORS", columnName: "EXECUTOR_ID")
            }
            not {
                primaryKeyExists(tableName: "WORKFLOW_EXECUTORS")
            }
        }
        sql("DELETE FROM WORKFLOW_EXECUTORS WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY WORKFLOW_ID, EXECUTOR_ID ORDER BY ROWID) AS row_num " +
            "FROM WORKFLOW_EXECUTORS) WHERE row_num > 1)")
        sql("DELETE FROM WORKFLOW_EXECUTORS WHERE EXECUTOR_ID IS NULL")
        addNotNullConstraint(tableName: "WORKFLOW_EXECUTORS", columnName: "EXECUTOR_ID")
        addPrimaryKey(columnNames: "WORKFLOW_ID, EXECUTOR_ID", constraintName: "WRKFLW_EXECUTORS_PK", tableName: "WORKFLOW_EXECUTORS")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-106") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "WORKFLOW_EXECUTORS_GROUP")
            and {
                columnExists(tableName: "WORKFLOW_EXECUTORS_GROUP", columnName: "WORKFLOW_ID")
                columnExists(tableName: "WORKFLOW_EXECUTORS_GROUP", columnName: "EXECUTOR_GROUP_ID")
            }
            not {
                primaryKeyExists(tableName: "WORKFLOW_EXECUTORS_GROUP")
            }
        }
        sql("DELETE FROM WORKFLOW_EXECUTORS_GROUP WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY WORKFLOW_ID, EXECUTOR_GROUP_ID ORDER BY ROWID) AS row_num " +
            "FROM WORKFLOW_EXECUTORS_GROUP) WHERE row_num > 1)")
        sql("DELETE FROM WORKFLOW_EXECUTORS_GROUP WHERE EXECUTOR_GROUP_ID IS NULL")
        addNotNullConstraint(tableName: "WORKFLOW_EXECUTORS_GROUP", columnName: "EXECUTOR_GROUP_ID")
        addPrimaryKey(columnNames: "WORKFLOW_ID, EXECUTOR_GROUP_ID", constraintName: "WRKFLW_EXECUTORS_GRP_PK", tableName: "WORKFLOW_EXECUTORS_GROUP")
    }

    changeSet(author: "rxl-shivamg1", id: "202504111051-107") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "XML_TEMPLT_CLL")
            and {
                columnExists(tableName: "XML_TEMPLT_CLL", columnName: "XML_TEMPLT_ID")
                columnExists(tableName: "XML_TEMPLT_CLL", columnName: "CLL_TEMPLT_ID")
            }
            not {
                primaryKeyExists(tableName: "XML_TEMPLT_CLL")
            }
        }
        sql("DELETE FROM XML_TEMPLT_CLL WHERE ROWID IN (" +
            "SELECT ROWID FROM (" +
            "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY XML_TEMPLT_ID, CLL_TEMPLT_ID ORDER BY ROWID) AS row_num " +
            "FROM XML_TEMPLT_CLL) WHERE row_num > 1)")
        sql("DELETE FROM XML_TEMPLT_CLL WHERE CLL_TEMPLT_ID IS NULL")
        addNotNullConstraint(tableName: "XML_TEMPLT_CLL", columnName: "CLL_TEMPLT_ID")
        addPrimaryKey(columnNames: "XML_TEMPLT_ID, CLL_TEMPLT_ID", constraintName: "XML_TEMPLT_CLL_PK", tableName: "XML_TEMPLT_CLL")
    }

}
