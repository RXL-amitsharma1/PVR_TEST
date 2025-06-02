databaseChangeLog = {

    changeSet(author: "Amrit Kaur", id: 'update product WHO drug code') {
        sql("update rpt_field set list_domain_class = null where argus_column_master_id = 'CP_WHO_DRUG_CODE';")
    }

    changeSet(author: "Amrit Kaur", id: 'update product Reassess Listedness') {
        sql("update rpt_field set list_domain_class = 'com.rxlogix.mapping.ClDatasheetReassess' where argus_column_master_id = 'DCEAL_DATASHEET_ID';")
    }

    changeSet(author: "Amrit Kaur", id:'insert PROD_GEN_STUDY_NAME in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm  USING (SELECT 1  FROM DUAL) qry  ON (acm.report_item = 'CP_PROD_GEN_NAME_STUDY_DRUG') WHEN NOT MATCHED THEN " +
                "INSERT (report_item, column_name, column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CP_PROD_GEN_NAME_STUDY_DRUG', 'PROD_GEN_NAME_STUDY_DRUG', 'V', NULL," +
                "NULL, NULL, NULL,NULL, NULL, 'CASE_PRODUCT');")
    }

    changeSet(author: "Amrit Kaur", id:'insert PROD_GEN_STUDY_NAME in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CP_PROD_GEN_NAME_STUDY_DRUG') WHEN NOT MATCHED THEN " +
                "INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) " +
                "VALUES (HIBERNATE_SEQUENCE.nextval,0,'CP_PROD_GEN_NAME_STUDY_DRUG', 'java.lang.String', 'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='ProductInformation')" +
                ", 0, NULL,'prodGenericStudyDrugName', 0, 1, 0, 0, 'prodGenericStudyDrugName');")
    }

    changeSet(author: "Amrit Kaur", id:'updating dropdown in CRO_FR_STATE_ID') {
        sql("update rpt_field  set list_domain_class='com.rxlogix.mapping.CfgWorkflowStates' where argus_column_master_id='CRO_FR_STATE_ID';")
    }
}