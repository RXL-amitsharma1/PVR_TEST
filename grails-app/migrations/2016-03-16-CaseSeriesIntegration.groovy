databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1458163724330-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'CASE_SERIES_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "CASE_SERIES_ID", type: "number(19,0)")
        }
        sql("update EX_RCONFIG set CASE_SERIES_ID = ID where CLASS='com.rxlogix.config.ExecutedPeriodicReportConfiguration'")

    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'CUM_CASE_SERIES_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "CUM_CASE_SERIES_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'USED_CASE_SERIES_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "USED_CASE_SERIES_ID", type: "number(19,0)")
        }
        sql("update EX_RCONFIG set USED_CASE_SERIES_ID = USED_FOR_CASES_EX_CONFIG_ID")
    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'USE_CASE_SERIES_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "USE_CASE_SERIES_ID", type: "number(19,0)")
        }
        sql("update RCONFIG set USE_CASE_SERIES_ID = USE_CASES_EX_CONFIG_ID")
    }


//    TODO Need to fix this
//    changeSet(author: "sachinverma (generated)", id: "1458163724319-3") {
//        preConditions(onFail: 'MARK_RAN') {
//            sqlCheck(expectedResult: '1', "select count(*) from user_tab_columns where table_name = 'CASE_SERIES' and column_name = 'STUDY_SELECTION' and data_type!='clob';")
//        }
//        renameColumn(tableName: "CASE_SERIES", oldColumnName: "STUDY_SELECTION", newColumnName: "STUDY_SELECTION_BACK_UP", columnDataType: "varchar2(255 char)")
//        addColumn(tableName: "CASE_SERIES") {
//            column(name: "STUDY_SELECTION", type: "clob")
//        }
//        sql("update CASE_SERIES set STUDY_SELECTION = STUDY_SELECTION_BACK_UP")
//        dropColumn(columnName: "STUDY_SELECTION_BACK_UP", tableName: "CASE_SERIES")
//    }
//
//    changeSet(author: "sachinverma (generated)", id: "1458163724319-4") {
//        preConditions(onFail: 'MARK_RAN') {
//            sqlCheck(expectedResult: '1', "select count(*) from user_tab_columns where table_name = 'CASE_SERIES' and column_name = 'PRODUCT_SELECTION' and data_type!='clob';")
//        }
//        renameColumn(tableName: "CASE_SERIES", oldColumnName: "PRODUCT_SELECTION", newColumnName: "PRODUCT_SELECTION_BACK_UP", columnDataType: "varchar2(255 char)")
//        addColumn(tableName: "CASE_SERIES") {
//            column(name: "PRODUCT_SELECTION", type: "clob")
//        }
//        sql("update CASE_SERIES set PRODUCT_SELECTION = PRODUCT_SELECTION_BACK_UP")
//        dropColumn(columnName: "PRODUCT_SELECTION_BACK_UP", tableName: "CASE_SERIES")
//    }
//
//    changeSet(author: "sachinverma (generated)", id: "1458163724319-5") {
//        preConditions(onFail: 'MARK_RAN') {
//            sqlCheck(expectedResult: '1', "select count(*) from user_tab_columns where table_name = 'CASE_SERIES' and column_name = 'EVENT_SELECTION' and data_type!='clob';")
//        }
//        renameColumn(tableName: "CASE_SERIES", oldColumnName: "EVENT_SELECTION", newColumnName: "EVENT_SELECTION_BACK_UP", columnDataType: "varchar2(255 char)")
//        addColumn(tableName: "CASE_SERIES") {
//            column(name: "EVENT_SELECTION", type: "clob")
//        }
//        sql("update CASE_SERIES set EVENT_SELECTION = EVENT_SELECTION_BACK_UP")
//        dropColumn(columnName: "EVENT_SELECTION_BACK_UP", tableName: "CASE_SERIES")
//    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-15") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyTableName: 'CASE_SERIES', foreignKeyName: 'FK264B8A6619EAC26')
        }
        dropForeignKeyConstraint(baseTableName: "CASE_SERIES", constraintName: "FK264B8A6619EAC26")
//        Added to insert old cases records to new table and marked them as cumulative for now.
        sql("insert into CASE_SERIES(ID,VERSION,CREATED_BY,DATE_CREATED,date_range_type,evaluate_date_as,event_selection,exclude_follow_up,exclude_non_valid_cases,global_query_id,include_locked_version,LAST_UPDATED,MODIFIED_BY,product_selection,series_name,study_selection,suspect_product,use_case_series_id) select ID,VERSION,CREATED_BY,DATE_CREATED,'CUMULATIVE',EVALUATE_DATE_AS,EVENT_SELECTION,EXCLUDE_FOLLOWUP,EXCLUDE_NON_VALID_CASES,EX_GLOBAL_QUERY_ID,INCLUDE_LOCKED_VERSION,LAST_UPDATED,MODIFIED_BY,PRODUCT_SELECTION,REPORT_NAME,STUDY_SELECTION,SUSPECT_PRODUCT,NULL from EX_RCONFIG where class='com.rxlogix.config.ExecutedPeriodicReportConfiguration';")
    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-16") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyTableName: 'EX_RCONFIG', foreignKeyName: 'FKC472BE8850B507D5')
        }
        dropForeignKeyConstraint(baseTableName: "EX_RCONFIG", constraintName: "FKC472BE8850B507D5")
    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-17") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyTableName: 'RCONFIG', foreignKeyName: 'FK68917214A18B40BA')
        }
        dropForeignKeyConstraint(baseTableName: "RCONFIG", constraintName: "FK68917214A18B40BA")
    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-24") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_RCONFIG', columnName: 'USED_FOR_CASES_EX_CONFIG_ID')
        }
        dropColumn(columnName: "USED_FOR_CASES_EX_CONFIG_ID", tableName: "EX_RCONFIG")
    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-25") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RCONFIG', columnName: 'USE_CASES_EX_CONFIG_ID')
        }
        dropColumn(columnName: "USE_CASES_EX_CONFIG_ID", tableName: "RCONFIG")
    }


    changeSet(author: "sachinverma (generated)", id: "1458163724330-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'case_series', foreignKeyName: 'FK264B8A66E9622A71')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "use_case_series_id", baseTableName: "case_series", constraintName: "FK264B8A66E9622A71", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "case_series", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'EX_RCONFIG', foreignKeyName: 'FKC472BE887F6E20B9')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "CASE_SERIES_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE887F6E20B9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "case_series", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'EX_RCONFIG', foreignKeyName: 'FKC472BE88C502EF3D')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "CUM_CASE_SERIES_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE88C502EF3D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "case_series", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-22") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'EX_RCONFIG', foreignKeyName: 'FKC472BE881441117B')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "USED_CASE_SERIES_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE881441117B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "case_series", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1458163724330-23") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'RCONFIG', foreignKeyName: 'FK68917214E9622A71')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "USE_CASE_SERIES_ID", baseTableName: "RCONFIG", constraintName: "FK68917214E9622A71", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "case_series", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1458184778685-7") {
        modifyDataType(columnName: "DATE_RANGE_TYPE", newDataType: "varchar2(255 char)", tableName: "CASE_SERIES")
    }

    changeSet(author: "sachinverma (generated)", id: "1458184778685-8") {
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "DATE_RANGE_TYPE", tableName: "CASE_SERIES")
    }

    changeSet(author: "sachinverma (generated)", id: "1458184778685-9") {
        modifyDataType(columnName: "EVALUATE_DATE_AS", newDataType: "varchar2(255 char)", tableName: "CASE_SERIES")
    }

    changeSet(author: "sachinverma (generated)", id: "1458184778685-10") {
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "EVALUATE_DATE_AS", tableName: "CASE_SERIES")
    }

}
