databaseChangeLog = {

    changeSet(author: "Amrit Kaur", id: 'insert table CASE_EVENT_SMQ_CASES') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_table_master atm   USING (SELECT 1 FROM DUAL) qry  ON (atm.table_name = 'CASE_EVENT_SMQ_CASES') WHEN NOT MATCHED THEN INSERT "+
                "(table_name,case_join_order,case_join_equi_outer,table_alias,table_type,versioned_data) VALUES ('CASE_EVENT_SMQ_CASES', "
                + "3, 'O','cesc','C','V');")
    }

    changeSet(author: "Amrit Kaur", id: 'insert join1 for CASE_EVENT_SMQ_CASES') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO case_column_join_mapping ccjm   USING (SELECT 1 FROM DUAL) qry  ON (ccjm.table_name_atm_id = 'CASE_EVENT_SMQ_CASES' and ccjm.map_table_name_atm_id = 'CASE_MASTER') "+
                "WHEN NOT MATCHED THEN INSERT (id,column_name, map_column_name, map_table_name_atm_id, table_name_atm_id) VALUES (HIBERNATE_SEQUENCE.nextval,'CASE_ID','CASE_ID','CASE_MASTER','CASE_EVENT_SMQ_CASES');")
    }

    changeSet(author: "Amrit Kaur", id: 'insert join2 for CASE_EVENT_SMQ_CASES') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO case_column_join_mapping ccjm   USING (SELECT 1 FROM DUAL) qry  ON (ccjm.table_name_atm_id = 'CASE_EVENT_SMQ_CASES' and ccjm.map_table_name_atm_id = 'CASE_EVENT') "+
                "WHEN NOT MATCHED THEN INSERT (id,column_name, map_column_name, map_table_name_atm_id, table_name_atm_id) VALUES (HIBERNATE_SEQUENCE.nextval,'SEQ_NUM','SEQ_NUM','CASE_EVENT','CASE_EVENT_SMQ_CASES');")
    }

    changeSet(author: "Amrit Kaur", id: 'insert CPI_PAT_DOB_PARTIAL in argus_column_master') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1 FROM DUAL) qry  ON (acm.report_item = 'CPI_PAT_DOB_PARTIAL') WHEN NOT MATCHED THEN INSERT (report_item, column_name, "
                + "column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer, lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CPI_PAT_DOB_PARTIAL', "
                + "'PAT_DOB_PARTIAL', 'V',NULL, NULL, NULL, NULL, NULL, NULL, 'V_CASE_PAT_INFO');")
    }


    changeSet(author: "Amrit Kaur", id:'insert CPI_PAT_DOB_PARTIAL in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CPI_PAT_DOB_PARTIAL') WHEN NOT MATCHED THEN"+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                " templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CPI_PAT_DOB_PARTIAL', 'java.lang.String', "+
                "'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='PatientInformation'), 0, NULL,'patInfoPatDobPartial', 0, 1, 1, 1, 'patInfoPatDobPartial');")
    }

    changeSet(author: "Amrit Kaur", id: 'insert CMR_EVENT in argus_column_master') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1 FROM DUAL) qry  ON (acm.report_item = 'CMR_EVENT') WHEN NOT MATCHED THEN INSERT (report_item, column_name, "
                + "column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer, lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CMR_EVENT', "
                + "'EVENT', 'V',NULL, NULL, NULL, NULL, NULL, NULL, 'V_CMN_REG_REPORTS');")
    }


    changeSet(author: "Amrit Kaur", id:'insert CMR_EVENT in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CMR_EVENT') WHEN NOT MATCHED THEN"+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                " templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CMR_EVENT', 'java.lang.String', "+
                "'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='ReportInformation'), 0, NULL,'reportsEvent', 1, 1, 1, 1, 'reportsEvent');")
    }

    changeSet(author: "Amrit Kaur", id: 'insert CMR_BODY_SYS in argus_column_master') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1 FROM DUAL) qry  ON (acm.report_item = 'CMR_BODY_SYS') WHEN NOT MATCHED THEN INSERT (report_item, column_name, "
                + "column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer, lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CMR_BODY_SYS', "
                + "'BODY_SYS', 'V',NULL, NULL, NULL, NULL, NULL, NULL, 'V_CMN_REG_REPORTS');")
    }


    changeSet(author: "Amrit Kaur", id:'insert CMR_BODY_SYS in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CMR_BODY_SYS') WHEN NOT MATCHED THEN"+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                " templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CMR_BODY_SYS', 'java.lang.String', "+
                "'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='ReportInformation'), 0, NULL,'reportsBodySys', 1, 1, 1, 1, 'reportsBodySys');")
    }

    changeSet(author: "Amrit Kaur", id: 'insert CESC_TERM_CATEGORY in argus_column_master') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1 FROM DUAL) qry  ON (acm.report_item = 'CESC_TERM_CATEGORY') WHEN NOT MATCHED THEN INSERT (report_item, column_name, "
                + "column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer, lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CESC_TERM_CATEGORY', "
                + "'TERM_CATEGORY', 'V',NULL, NULL, NULL, NULL, NULL, NULL, 'CASE_EVENT_SMQ_CASES');")
    }


    changeSet(author: "Amrit Kaur", id:'insert CESC_TERM_CATEGORY in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CESC_TERM_CATEGORY') WHEN NOT MATCHED THEN"+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                " templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CESC_TERM_CATEGORY', 'java.lang.String', "+
                "'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='EventInformation'), 0, NULL,'smqCasesTermCategory', 1, 0, 0, 0, 'smqCasesTermCategory');")
    }

    changeSet(author: "Amrit Kaur", id: 'PVR-1564 argus_column_master') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1 FROM DUAL) qry  ON (acm.report_item = 'CM_LAST_MED_REVIEWER') WHEN NOT MATCHED THEN INSERT (report_item, column_name, "
                + "column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer, lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CM_LAST_MED_REVIEWER', "
                + "'LAST_MED_REVIEWER', 'V',NULL, 'USER_FULLNAME','USER_ID','O','CFG_USERS', NULL, 'CASE_MASTER');")
    }


    changeSet(author: "Amrit Kaur", id:'PVR-1564 rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CM_LAST_MED_REVIEWER') WHEN NOT MATCHED THEN"+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                " templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CM_LAST_MED_REVIEWER', 'java.lang.String', "+
                "'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='CaseInformation'), 0,'com.rxlogix.mapping.CfgUsers','masterLastMedReviewer', 1, 1, 1, 1, 'masterLastMedReviewer');")
    }

    changeSet(author: "Amrit Kaur", id: 'PVR-1810 -  argus_column_master') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1 FROM DUAL) qry  ON (acm.report_item = 'CMR_DAYS_DUE') WHEN NOT MATCHED THEN INSERT (report_item, column_name, "
                + "column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer, lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CMR_DAYS_DUE', "
                + "'DAYS_DUE','N',NULL, NULL,NULL,NULL,NULL, NULL,'V_CMN_REG_REPORTS');")
    }

    changeSet(author: "Amrit Kaur", id:'PVR-1810 - rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CMR_DAYS_DUE') WHEN NOT MATCHED THEN"+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                " templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CMR_DAYS_DUE', 'java.lang.Number', "+
                "'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='ReportInformation'),0,NULL,'reportDaysDue',1,1,1,1,'reportDaysDue');")
    }

    changeSet(author: "Amrit Kaur", id: 'PVR-1788 - CASE_VERSION_TABLE joins') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("delete from  case_column_join_mapping   where TABLE_NAME_ATM_ID = 'CASE_VERSION_TABLE';")
    }

    changeSet(author: "Amrit Kaur", id: 'PVR-1788 - CASE_VERSION_TABLE') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("delete from  argus_table_master atm  where atm.table_name = 'CASE_VERSION_TABLE';")
    }

    changeSet(author: "Amrit Kaur", id: 'PVR-1788 - LM_PREGNANCY_RPT_TYPE') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("delete from  argus_table_master atm  where atm.table_name = 'LM_PREGNANCY_RPT_TYPE';")
    }

}