databaseChangeLog = {

    changeSet(author: "Amrit Kaur", id:'drop FKF5EE11312A698EE5 on rpt_field') {
        sql("alter table rpt_field drop constraint FKF5EE11312A698EE5;")
    }

    changeSet(author: "Amrit Kaur", id:'correction for CONSER_REPORTABILITY in rpt_field ') {
        sql("update rpt_field set argus_column_master_id = 'DCPE_CONSER_REPORTABILITY' where argus_column_master_id = 'DCPE_CONSER_REPORTABLITY';")
    }

    changeSet(author: "Amrit Kaur", id:'correction for CONSER_REPORTABILITY in column_master') {
        sql("update argus_column_master set column_name ='CONSER_REPORTABILITY' , report_item = 'DCPE_CONSER_REPORTABILITY' where report_item = 'DCPE_CONSER_REPORTABLITY';")
    }

    changeSet(author: "Amrit Kaur", id:'create FKF5EE11312A698EE5 on rpt_field') {
        sql("ALTER TABLE RPT_FIELD ADD (CONSTRAINT FKF5EE11312A698EE5  FOREIGN KEY (ARGUS_COLUMN_MASTER_ID)  REFERENCES ARGUS_COLUMN_MASTER (REPORT_ITEM)) ;")
    }

    changeSet(author: "Amrit Kaur", id:'deleted items') {
        sql("delete from argus_column_master  where report_item in ('CPT_CASE_ID','CPCB_EVENT_SEQ_NUM','CPCN_EVENT_SEQ_NUM');")
    }

    changeSet(author: "Amrit Kaur", id:'update join type') {
        sql("update argus_column_master set lm_join_equi_outer = 'O' where report_item = 'CEA_LICENSE_ID';")
    }

    changeSet(author: "Amrit Kaur", id:'update column type') {
        sql("update argus_column_master set column_type = 'N' where report_item = 'CP_PSUR_GROUP_NAME';")
    }

    changeSet(author: 'Amrit Kaur', id:'insert CPT_REL_TEST_ID in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum < 2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1  FROM DUAL) qry  ON (acm.report_item = 'CPT_REL_TEST_ID') WHEN NOT MATCHED THEN  INSERT (acm.report_item, "+
                "acm.column_name, acm.column_type, acm.concatenated_field, acm.lm_decode_column, acm.lm_join_column, acm.lm_join_equi_outer, acm.lm_table_name_atm_id, acm.primary_key_id, "+
                "acm.table_name_atm_id) VALUES ('CPT_REL_TEST_ID', 'REL_TEST_ID', 'N', NULL, NULL, NULL, NULL, NULL, 2, 'CASE_PAT_TESTS');")
    }

    changeSet(author: 'Amrit Kaur', id:'insert CEA_LICENSE_NUMBER in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum < 2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1  FROM DUAL) qry  ON (acm.report_item = 'CEA_LICENSE_NUMBER') WHEN NOT MATCHED THEN  INSERT (report_item, "+
                "column_name, column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES "+
                "('CEA_LICENSE_NUMBER', 'LICENSE_ID', 'N', NULL,'LIC_NUMBER', 'LICENSE_ID', 'O','LM_LICENSE', NULL, 'CASE_EVENT_ASSESS');")
    }

    changeSet(author: 'Amrit Kaur', id:'insert CM_PRIM_EVT_PREF_TERM in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum < 2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1  FROM DUAL) qry  ON (acm.report_item = 'CM_PRIM_EVT_PREF_TERM') WHEN NOT MATCHED THEN  INSERT (report_item, "+
                "column_name, column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES "+
                "('CM_PRIM_EVT_PREF_TERM', 'PRIM_EVT_PREF_TERM', 'V', NULL,NULL, NULL, NULL,NULL, NULL, 'CASE_MASTER');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CEA_LICENSE_NUMBER in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum < 2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CEA_LICENSE_NUMBER') WHEN NOT MATCHED THEN  INSERT "+
                "(id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, templt_cll_selectable, templt_dtcol_selectable,"+
                "templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CEA_LICENSE_NUMBER', 'java.lang.String', 'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name "+
                "='EventAssessmentInformation'), 0, 'com.rxlogix.mapping.LmLicenseNumber','eventAssessLicenseNumber', 1, 1, 1, 1, 'eventAssessLicenseNumber');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CM_PRIM_EVT_PREF_TERM in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum < 2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CM_PRIM_EVT_PREF_TERM') WHEN NOT MATCHED THEN  INSERT "+
                "(id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, templt_cll_selectable, templt_dtcol_selectable,"+
                "templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CM_PRIM_EVT_PREF_TERM', 'java.lang.String', 'dd-MMM-yyyy',NULL,(select id from rpt_field_group where"+
                " name ='CaseInformation'), 0, NULL,'masterPrimEvtPrefTerm', 1, 1, 1, 1, 'masterPrimEvtPrefTerm');")
    }

}