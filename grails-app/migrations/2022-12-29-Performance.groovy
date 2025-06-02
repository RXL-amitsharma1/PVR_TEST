databaseChangeLog = {
//config updates
    changeSet(author: "sergey", id: "202212291042-1") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'RCONFIGS_TAGS_PIDX1')} }
        createIndex(indexName: "RCONFIGS_TAGS_PIDX1", tableName: "RCONFIGS_TAGS") {
            column(name: "RCONFIG_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-2") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'RCONFIG_USER_STATE_PIDX1')} }
        createIndex(indexName: "RCONFIG_USER_STATE_PIDX1", tableName: "RCONFIG_USER_STATE") {
            column(name: "RPT_USER_ID")
            column(name: "RCONFIG_ID")
            column(name: "IS_FAVORITE")
        }
    }

    changeSet(author: "sergey", id: "202212291042-3") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'DEL_SHAR_WITH_GRPS_PIDX1')} }
        createIndex(indexName: "DEL_SHAR_WITH_GRPS_PIDX1", tableName: "DELIVERIES_SHARED_WITH_GRPS") {
            column(name: "DELIVERY_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-4") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'DEL_SHARED_WITHS_PIDX1')} }
        createIndex(indexName: "DEL_SHARED_WITHS_PIDX1", tableName: "DELIVERIES_SHARED_WITHS") {
            column(name: "DELIVERY_ID")
        }
    }


//executed config updates
    changeSet(author: "sergey", id: "202212291042-5") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_RCONFIGS_TAGS_PIDX1')} }
        createIndex(indexName: "EX_RCONFIGS_TAGS_PIDX1", tableName: "EX_RCONFIGS_TAGS") {
            column(name: "EXC_RCONFIG_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-6") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_RCONFIG_USER_STATE_PIDX1')} }
        createIndex(indexName: "EX_RCONFIG_USER_STATE_PIDX1", tableName: "EX_RCONFIG_USER_STATE") {
            column(name: "RPT_USER_ID")
            column(name: "EX_RCONFIG_ID")
            column(name: "IS_DELETED")
            column(name: "IS_ARCHIVED")
            column(name: "IS_FAVORITE")
        }
    }

    changeSet(author: "sergey", id: "202212291042-7") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_DEL_SHAR_WITH_GRPS_PIDX1')} }
        createIndex(indexName: "EX_DEL_SHAR_WITH_GRPS_PIDX1", tableName: "EX_DELIVERIES_SHARED_WITH_GRPS") {
            column(name: "EX_DELIVERY_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-8") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_DEL_SHARED_WITHS_PIDX1')} }
        createIndex(indexName: "EX_DEL_SHARED_WITHS_PIDX1", tableName: "EX_DELIVERIES_SHARED_WITHS") {
            column(name: "EX_DELIVERY_ID")
        }
    }



//case series
    changeSet(author: "sergey", id: "202212291042-9") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'CASE_SERIES_TAGS_PIDX1')} }
        createIndex(indexName: "CASE_SERIES_TAGS_PIDX1", tableName: "CASE_SERIES_TAGS") {
            column(name: "CASE_SERIES_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-10") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'CASE_SERIES_USER_STATE_PIDX1')} }
        createIndex(indexName: "CASE_SERIES_USER_STATE_PIDX1", tableName: "CASE_SERIES_USER_STATE") {
            column(name: "ECASE_SERIES_ID")
            column(name: "RPT_USER_ID")
            column(name: "IS_FAVORITE")
        }
    }

    changeSet(author: "sergey", id: "202212291042-11") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'CASE_DEL_SHARED_W_GRPS_PIDX1')} }
        createIndex(indexName: "CASE_DEL_SHARED_W_GRPS_PIDX1", tableName: "CASE_DELIVERIES_SHARED_W_GRPS") {
            column(name: "DELIVERY_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-12") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'CASE_DEL_SHARED_WITHS_PIDX1')} }
        createIndex(indexName: "CASE_DEL_SHARED_WITHS_PIDX1", tableName: "CASE_DELIVERIES_SHARED_WITHS") {
            column(name: "DELIVERY_ID")
        }
    }

    //executed case series
    changeSet(author: "sergey", id: "202212291042-13") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_CASE_SERIES_TAGS_PIDX1')} }
        createIndex(indexName: "EX_CASE_SERIES_TAGS_PIDX1", tableName: "EX_CASE_SERIES_TAGS") {
            column(name: "EX_CASE_SERIES_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-14") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_CASE_USER_STATE_PIDX1')} }
        createIndex(indexName: "EX_CASE_USER_STATE_PIDX1", tableName: "EX_CASE_SERIES_USER_STATE") {
            column(name: "EX_CASE_SERIES_ID")
            column(name: "RPT_USER_ID")
            column(name: "IS_DELETED")
            column(name: "IS_ARCHIVED")
            column(name: "IS_FAVORITE")
        }
    }

    changeSet(author: "sergey", id: "202212291042-15") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_CASE_DEL_SHRD_W_GRPS_PIDX1')} }
        createIndex(indexName: "EX_CASE_DEL_SHRD_W_GRPS_PIDX1", tableName: "EX_CASE_DELIVERIES_SHRD_W_GRPS") {
            column(name: "EX_DELIVERY_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-16") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_CASE_DEL_SHRD_WTHS_PIDX1')} }
        createIndex(indexName: "EX_CASE_DEL_SHRD_WTHS_PIDX1", tableName: "EX_CASE_DELIVERIES_SHRD_WTHS") {
            column(name: "EX_DELIVERY_ID")
        }
    }
//queries
    changeSet(author: "sergey", id: "202212291042-17") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'SUPER_QRS_TAGS_PIDX1')} }
        createIndex(indexName: "SUPER_QRS_TAGS_PIDX1", tableName: "SUPER_QRS_TAGS") {
            column(name: "SUPER_QUERY_ID")
        }
    }
    changeSet(author: "sergey", id: "202212291042-18") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'QUERY_USER_STATE_PIDX1')} }
        createIndex(indexName: "QUERY_USER_STATE_PIDX1", tableName: "QUERY_USER_STATE") {
            column(name: "QUERY_ID")
            column(name: "RPT_USER_ID")
            column(name: "IS_FAVORITE")
        }
    }
    changeSet(author: "sergey", id: "202212291042-19") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'SUPER_QUERY_USER_PIDX1')} }
        createIndex(indexName: "SUPER_QUERY_USER_PIDX1", tableName: "SUPER_QUERY_USER") {
            column(name: "QUERY_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-20") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'SUPER_Q_USER_GP_PIDX1')} }
        createIndex(indexName: "SUPER_Q_USER_GP_PIDX1", tableName: "SUPER_QUERY_USER_GROUP") {
            column(name: "QUERY_ID")
        }
    }
    //templates
    changeSet(author: "sergey", id: "202212291042-21") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'RPT_TEMPLTS_TAGS_PIDX1')} }
        createIndex(indexName: "RPT_TEMPLTS_TAGS_PIDX1", tableName: "RPT_TEMPLTS_TAGS") {
            column(name: "RPT_TEMPLT_ID")
        }
    }
    changeSet(author: "sergey", id: "202212291042-22") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'TEMPLATE_USER_STATE_PIDX1')} }
        createIndex(indexName: "TEMPLATE_USER_STATE_PIDX1", tableName: "TEMPLATE_USER_STATE") {
            column(name: "TEMPLATE_ID")
            column(name: "RPT_USER_ID")
            column(name: "IS_FAVORITE")
        }
    }
    changeSet(author: "sergey", id: "202212291042-23") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'RPT_TEMPLATE_USER_PIDX1')} }
        createIndex(indexName: "RPT_TEMPLATE_USER_PIDX1", tableName: "RPT_TEMPLATE_USER") {
            column(name: "REPORT_TEMPLATE_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-24") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'RPT_TPL_USER_GP_PIDX1')} }
        createIndex(indexName: "RPT_TPL_USER_GP_PIDX1", tableName: "RPT_TEMPLATE_USER_GROUP") {
            column(name: "REPORT_TEMPLATE_ID")
        }
    }

    //delivery

    changeSet(author: "sergey", id: "202212291042-25") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_CASE_DELIVERY_PIDX1')} }
        createIndex(indexName: "EX_CASE_DELIVERY_PIDX1", tableName: "EX_CASE_DELIVERY") {
            column(name: "EXECUTED_CASE_ID")
        }
    }
   changeSet(author: "sergey", id: "202212291042-26") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'CASE_DELIVERY_PIDX1')} }
        createIndex(indexName: "CASE_DELIVERY_PIDX1", tableName: "CASE_DELIVERY") {
            column(name: "CASE_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-27") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_STATUSES_SHARED_WITHS_PIDX1')} }
        createIndex(indexName: "EX_STATUSES_SHARED_WITHS_PIDX1", tableName: "EX_STATUSES_SHARED_WITHS") {
            column(name: "EX_STATUS_ID")
        }
    }
    changeSet(author: "sergey", id: "202212291042-28") {
        preConditions(onFail: 'MARK_RAN') {not {indexExists(indexName: 'EX_STATUSES_RPT_FORMATS_PIDX1')} }
        createIndex(indexName: "EX_STATUSES_RPT_FORMATS_PIDX1", tableName: "EX_STATUSES_RPT_FORMATS") {
            column(name: "EX_STATUS_ID")
        }
    }

    //other join tables
    changeSet(author: "sergey", id: "202212291042-29") {
        preConditions(onFail: 'MARK_RAN') { not { indexExists(indexName: 'EX_RCONFIG_ACTION_ITEMS_PIDX1') } }
        createIndex(indexName: "EX_RCONFIG_ACTION_ITEMS_PIDX1", tableName: "EX_RCONFIG_ACTION_ITEMS") {
            column(name: "EX_RCONFIG_ID")
        }
    }
    changeSet(author: "sergey", id: "202212291042-30") {
        preConditions(onFail: 'MARK_RAN') { not { indexExists(indexName: 'EX_RCONFIG_P_C_USERS_PIDX1') } }
        createIndex(indexName: "EX_RCONFIG_P_C_USERS_PIDX1", tableName: "EX_RCONFIG_P_C_USERS") {
            column(name: "RCONFIG_ID")
        }
    }
    changeSet(author: "sergey", id: "202212291042-31") {
        preConditions(onFail: 'MARK_RAN') { not { indexExists(indexName: 'EX_RCONFIG_REPORT_DESTS_PIDX1') } }
        createIndex(indexName: "EX_RCONFIG_REPORT_DESTS_PIDX1", tableName: "EX_RCONFIG_REPORT_DESTS") {
            column(name: "EX_RCONFIG_ID")
        }
    }

    changeSet(author: "sergey", id: "202212291042-32") {
        preConditions(onFail: 'MARK_RAN') { not { indexExists(indexName: 'RCONFIG_P_C_USERS_PIDX1') } }
        createIndex(indexName: "RCONFIG_P_C_USERS_PIDX1", tableName: "RCONFIG_P_C_USERS") {
            column(name: "RCONFIG_ID")
        }
    }
    changeSet(author: "sergey", id: "202212291042-33") {
        preConditions(onFail: 'MARK_RAN') { not { indexExists(indexName: 'RCONFIG_REPORT_DESTS_PIDX1') } }
        createIndex(indexName: "RCONFIG_REPORT_DESTS_PIDX1", tableName: "RCONFIG_REPORT_DESTS") {
            column(name: "RCONFIG_ID")
        }
    }
    changeSet(author: "sergey", id: "202212291042-34") {
        preConditions(onFail: 'MARK_RAN') { not { indexExists(indexName: 'EX_RCONFIG_COMMENT_TABLE_PIDX1') } }
        createIndex(indexName: "EX_RCONFIG_COMMENT_TABLE_PIDX1", tableName: "EX_RCONFIG_COMMENT_TABLE") {
            column(name: "EXC_RCONFIG_ID")
        }
    }
    changeSet(author: "sergey", id: "202212291042-35") {
        preConditions(onFail: 'MARK_RAN') { not { indexExists(indexName: 'EX_RCONFIGS_POI_PARAMS_PIDX1') } }
        createIndex(indexName: "EX_RCONFIGS_POI_PARAMS_PIDX1", tableName: "EX_RCONFIGS_POI_PARAMS") {
            column(name: "EXC_RCONFIG_ID")
        }
    }
 }
