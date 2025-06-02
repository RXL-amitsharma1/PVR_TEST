databaseChangeLog = {

    changeSet(author: 'Amrit Kaur', id:'insert PRODUCT_STUDY_DRUG in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm  USING (SELECT 1  FROM DUAL) qry  ON (acm.report_item = 'CP_PRODUCT_STUDY_DRUG') WHEN NOT MATCHED THEN " +
                "INSERT (report_item, column_name, column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CP_PRODUCT_STUDY_DRUG', 'PRODUCT_PAT_EXP_ID', 'N', NULL," +
                "'PROD_NAME', 'PRODUCT_ID', 'O','LM_PRODUCT', NULL, 'CASE_PRODUCT');")
    }

    changeSet(author: "Amrit Kaur", id:'insert PRODUCT_STUDY_DRUG in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CP_PRODUCT_STUDY_DRUG') WHEN NOT MATCHED THEN " +
                "INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) " +
                "VALUES (HIBERNATE_SEQUENCE.nextval,0,'CP_PRODUCT_STUDY_DRUG', 'java.lang.String', 'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='ProductInformation')" +
                ", 0, 'com.rxlogix.mapping.LmProduct','productProductStudyDrug', 1, 1, 1, 1, 'productProductStudyDrug');")
    }

    changeSet(author: "Amrit Kaur", id:'insert TIME_TO_ONSET_DAYS in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm  USING (SELECT 1  FROM DUAL) qry  ON (acm.report_item = 'DCPE_TIME_ONSET_DAYS') WHEN NOT MATCHED THEN " +
                "INSERT (report_item, column_name, column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('DCPE_TIME_ONSET_DAYS', 'TIME_ONSET_DAYS', 'N', NULL," +
                "NULL, NULL, NULL,NULL, NULL, 'DV_CASE_PROD_EVENT');")
    }

    changeSet(author: "Amrit Kaur", id:'insert TIME_TO_ONSET_DAYS in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'DCPE_TIME_ONSET_DAYS') WHEN NOT MATCHED THEN " +
                "INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) " +
                "VALUES (HIBERNATE_SEQUENCE.nextval,0,'DCPE_TIME_ONSET_DAYS', 'java.lang.Number', 'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='EventInformation')" +
                ", 0, NULL,'dvProdEventTimeOnsetDays', 1, 0, 0, 0, 'dvProdEventTimeOnsetDays');")
    }

    changeSet(author: "Amrit Kaur", id:'update time_to_onset flags') {
        sql("UPDATE rpt_field SET query_selectable = 0, templt_cll_selectable = 1,templt_dtcol_selectable = 0,templt_dtrow_selectable = 0 WHERE argus_column_master_id = 'DCPE_TIME_ONSET';")
    }

}