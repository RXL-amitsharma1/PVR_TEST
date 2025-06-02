databaseChangeLog = {

    changeSet(author: "anurag", id: "202112271502-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '1', "select count(*) from user_tab_columns where table_name = 'DRILLDOWN_METADATA' and column_name = 'PROCESSED_REPORT_ID' and data_type='VARCHAR2(255 char)';")

            }
        }
        addColumn(tableName: "DRILLDOWN_METADATA") {
            column(name: "TEMP_PROCESSED_REPORT_ID", type: "VARCHAR2(255 char)"){
                constraints(nullable: "true")
            }
        }
        sql("UPDATE DRILLDOWN_METADATA SET TEMP_PROCESSED_REPORT_ID = PROCESSED_REPORT_ID;")

        dropColumn(columnName: "PROCESSED_REPORT_ID", tableName: "DRILLDOWN_METADATA")

        sql("ALTER TABLE DRILLDOWN_METADATA RENAME COLUMN TEMP_PROCESSED_REPORT_ID TO PROCESSED_REPORT_ID;")

        addNotNullConstraint(tableName: "DRILLDOWN_METADATA", columnName: "PROCESSED_REPORT_ID")

    }

    changeSet(author: "anurag", id: "202112271502-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '1', "select count(*) from user_tab_columns where table_name = 'AUTO_ASSIGNMENT' and column_name = 'PROCESSED_REPORT_ID' and data_type='VARCHAR2(255 char)';")

            }
        }
        addColumn(tableName: "AUTO_ASSIGNMENT") {
            column(name: "TEMP_PROCESSED_REPORT_ID", type: "VARCHAR2(255 char)"){
                constraints(nullable: "true")
            }
        }
        sql("UPDATE AUTO_ASSIGNMENT SET TEMP_PROCESSED_REPORT_ID = PROCESSED_REPORT_ID;")

        dropColumn(columnName: "PROCESSED_REPORT_ID", tableName: "AUTO_ASSIGNMENT")

        sql("ALTER TABLE AUTO_ASSIGNMENT RENAME COLUMN TEMP_PROCESSED_REPORT_ID TO PROCESSED_REPORT_ID;")

    }

    changeSet(author: "anurag", id: "202112271502-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '1', "select count(*) from user_indexes where table_name='DRILLDOWN_METADATA' and index_name='PROC_RPRT_ID_IDX_202103101004';")

            }
        }
        createIndex(indexName: "PROC_RPRT_ID_IDX_202103101004", tableName: "DRILLDOWN_METADATA", unique: "false") {
            column(name: "PROCESSED_REPORT_ID")
        }
    }

    changeSet(author: "anurag (generated)", id: "202112271502-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '1', "select count(*) from user_indexes where table_name='DRILLDOWN_METADATA' and index_name='DRILLDOWN_MT_CS_TN_PR_INDEX';")

            }
        }
        createIndex(indexName: "DRILLDOWN_MT_CS_TN_PR_INDEX", tableName: "DRILLDOWN_METADATA", unique: "true") {
            column(name: "CASE_ID")
            column(name: "TENANT_ID")
            column(name: "PROCESSED_REPORT_ID")
        }
    }


}