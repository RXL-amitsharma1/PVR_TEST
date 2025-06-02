databaseChangeLog = {

    changeSet(author: "Amrit Kaur", id: 'insert table CL_INIT_FOLLOWUP_REPORT') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_table_master atm USING (SELECT 1 FROM DUAL) qry ON (atm.table_name = 'CL_INIT_FOLLOWUP_REPORT') WHEN NOT MATCHED THEN INSERT (table_name, case_join_order, case_join_equi_outer, table_alias,table_type, versioned_data)VALUES ('CL_INIT_FOLLOWUP_REPORT', NULL, NULL, 'cifr', 'L', NULL);")
    }

    changeSet(author: "Amrit Kaur", id: 'insert DCTE_EXP_FIRST_DOSE in argus_column_master') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1 FROM DUAL) qry  ON (acm.report_item = 'DCTE_EXP_FIRST_DOSE') WHEN NOT MATCHED THEN INSERT (report_item, column_name, "
                + "column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer, lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('DCTE_EXP_FIRST_DOSE', "
                + "'EXP_FIRST_DOSE', 'V',NULL, NULL, NULL, NULL, NULL, NULL, 'DV_CASE_TRIMESTER_EXPOSURE');")
    }

    changeSet(author: "Amrit Kaur", id: 'insert DCTE_EXP_SECOND_DOSE in argus_column_master') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm USING (SELECT 1 FROM DUAL) qry ON (acm.report_item = 'DCTE_EXP_SECOND_DOSE')  WHEN NOT MATCHED THEN INSERT (report_item, column_name, " +
                "column_type, concatenated_field, lm_decode_column, lm_join_column, lm_join_equi_outer, lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES " +
                "('DCTE_EXP_SECOND_DOSE', 'EXP_SECOND_DOSE', 'V',NULL, NULL, NULL, NULL, NULL, NULL, 'DV_CASE_TRIMESTER_EXPOSURE');")
    }

    changeSet(author: "Amrit Kaur", id:'insert DCTE_EXP_THIRD_DOSE in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm USING (SELECT 1 FROM DUAL) qry ON (acm.report_item = 'DCTE_EXP_THIRD_DOSE') WHEN NOT MATCHED THEN INSERT (report_item, column_name, "+
                "column_type, concatenated_field, lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('DCTE_EXP_THIRD_DOSE', "+
                "'EXP_THIRD_DOSE', 'V',NULL, NULL, NULL, NULL, NULL, NULL, 'DV_CASE_TRIMESTER_EXPOSURE');")
    }

    changeSet(author: "Amrit Kaur", id:'insert DCTE_EXP_FOURTH_DOSE in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm USING (SELECT 1 FROM DUAL) qry ON (acm.report_item = 'DCTE_EXP_FOURTH_DOSE') WHEN NOT MATCHED THEN INSERT (report_item, column_name, "+
                "column_type, concatenated_field, lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('DCTE_EXP_FOURTH_DOSE', "+
                "'EXP_FOURTH_DOSE', 'V',NULL, NULL, NULL, NULL, NULL, NULL, 'DV_CASE_TRIMESTER_EXPOSURE');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CP_LOT_NO_ALL in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm USING (SELECT 1  FROM DUAL) qry ON (acm.report_item = 'CP_LOT_NO_ALL') WHEN NOT MATCHED THEN INSERT (report_item, column_name, column_type, "+
                "concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CP_LOT_NO_ALL', 'LOT_NO_ALL', 'C',"+
                "NULL, NULL, NULL, NULL, NULL, NULL, 'CASE_PRODUCT');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CP_IND_CODED_ALL in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm USING (SELECT 1 FROM DUAL) qry ON (acm.report_item = 'CP_IND_CODED_ALL') WHEN NOT MATCHED THEN INSERT (report_item, column_name,"+
                "column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CP_IND_CODED_ALL', "+
                "'IND_CODED_ALL', 'C',NULL, NULL, NULL, NULL, NULL, NULL, 'CASE_PRODUCT');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CP_IND_REPTD_ALL in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm USING (SELECT 1 FROM DUAL) qry ON (acm.report_item = 'CP_IND_REPTD_ALL') WHEN NOT MATCHED THEN  INSERT (report_item, column_name, "+
                "column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CP_IND_REPTD_ALL', "+
                "'IND_REPTD_ALL', 'C',NULL, NULL, NULL, NULL, NULL, NULL, 'CASE_PRODUCT');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CMR_INITIAL_FLAG in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm USING (SELECT 1 FROM DUAL) qry ON (acm.report_item = 'CMR_INITIAL_FLAG') WHEN NOT MATCHED THEN INSERT (report_item, column_name, column_type, "+
                "concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CMR_INITIAL_FLAG', 'INITIAL_FLAG', 'N',"+
                " NULL, 'REPORT_TYPE', 'ID', 'O', 'CL_INIT_FOLLOWUP_REPORT', NULL, 'V_CMN_REG_REPORTS');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CS_PRIMARY_IMP in argus_column_master') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm USING (SELECT 1 FROM DUAL) qry ON (acm.report_item = 'CS_PRIMARY_IMP') WHEN NOT MATCHED THEN INSERT (report_item, column_name, column_type, "+
                "concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer,lm_table_name_atm_id, primary_key_id, table_name_atm_id)VALUES ('CS_PRIMARY_IMP', 'PRIMARY_IMP', 'N',"+
                "NULL, 'PROD_NAME', 'PRODUCT_ID', 'O', 'LM_PRODUCT', NULL, 'CASE_STUDY');")
    }

    changeSet(author: "Amrit Kaur", id:'insert DCTE_EXP_FIRST_DOSE in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'DCTE_EXP_FIRST_DOSE') WHEN NOT MATCHED THEN"+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                " templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'DCTE_EXP_FIRST_DOSE', 'java.lang.String', "+
                "'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='PregnancyInformation'), 0, NULL,'dvTrimesterExpFirstDose', 1, 1, 1, 1, 'dvTrimesterExpFirstDose');")
    }

    changeSet(author: "Amrit Kaur", id:'insert DCTE_EXP_SECOND_DOSE in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'DCTE_EXP_SECOND_DOSE') WHEN NOT MATCHED THEN "+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                " templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'DCTE_EXP_SECOND_DOSE', 'java.lang.String',"+
                "'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='PregnancyInformation') , 0, NULL,'dvTrimesterExpSecondDose', 1, 1, 1, 1, 'dvTrimesterExpSecondDose');")
    }

    changeSet(author: "Amrit Kaur", id:'insert DCTE_EXP_THIRD_DOSE in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'DCTE_EXP_THIRD_DOSE') WHEN NOT MATCHED THEN "+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                "templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'DCTE_EXP_THIRD_DOSE', 'java.lang.String', "+
                "'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='PregnancyInformation'), 0, NULL,'dvTrimesterExpThirdDose', 1, 1, 1, 1, 'dvTrimesterExpThirdDose');")
    }

    changeSet(author: "Amrit Kaur", id:'insert DCTE_EXP_FOURTH_DOSE in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'DCTE_EXP_FOURTH_DOSE') WHEN NOT MATCHED THEN "+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                "templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'DCTE_EXP_FOURTH_DOSE', 'java.lang.String', "+
                "'dd-MMM-yyyy',NULL,(select id from rpt_field_group where name ='PregnancyInformation'), 0, NULL,'dvTrimesterExpFourthDose', 1, 1, 1, 1, 'dvTrimesterExpFourthDose');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CP_LOT_NO_ALL in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CP_LOT_NO_ALL') WHEN NOT MATCHED THEN"+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                " templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CP_LOT_NO_ALL', 'java.lang.String', 'dd-MMM-yyyy',NULL,(select id"+
                " from rpt_field_group where name ='ProductInformation'), 0, NULL,'productLotNoAll', 1, 1, 1, 1, 'productLotNoAll');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CP_IND_CODED_ALL in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CP_IND_CODED_ALL') WHEN NOT MATCHED THEN "+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                "templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CP_IND_CODED_ALL', 'java.lang.String', 'dd-MMM-yyyy',NULL,(select"+
                " id from rpt_field_group where name ='ProductInformation'), 0, NULL,'productIndCodedAll', 1, 1, 1, 1, 'productIndCodedAll');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CP_IND_REPTD_ALL in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CP_IND_REPTD_ALL') WHEN NOT MATCHED THEN "+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                "templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CP_IND_REPTD_ALL', 'java.lang.String', 'dd-MMM-yyyy',NULL,(select "+
                "id from rpt_field_group where name ='ProductInformation'), 0, NULL,'productIndReptdAll', 1, 1, 1, 1, 'productIndReptdAll');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CMR_INITIAL_FLAG in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CMR_INITIAL_FLAG') WHEN NOT MATCHED THEN "+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                "templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CMR_INITIAL_FLAG', 'java.lang.String', 'dd-MMM-yyyy',NULL,(select "+
                "id from rpt_field_group where name ='ReportInformation'), 0, 'com.rxlogix.mapping.ClInitFollowupReport','reportsInitialFlag', 1, 1, 1, 1, 'reportsInitialFlag');")
    }

    changeSet(author: "Amrit Kaur", id:'insert CS_PRIMARY_IMP in rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CS_PRIMARY_IMP') WHEN NOT MATCHED THEN "+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                "templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CS_PRIMARY_IMP', 'java.lang.String', 'dd-MMM-yyyy',NULL,(select id"+
                " from rpt_field_group where name ='StudyInformation'), 0, 'com.rxlogix.mapping.LmProduct','studyPrimaryImp', 1, 1, 1, 1, 'studyPrimaryImp');")
    }

    changeSet(author: "Amrit Kaur", id: 'PVR-1784 argus_column_master') {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO argus_column_master acm   USING (SELECT 1 FROM DUAL) qry  ON (acm.report_item = 'CED_ACT_TAKEN_ID_SUSPECT') WHEN NOT MATCHED THEN INSERT (report_item, column_name, "
                + "column_type, concatenated_field,lm_decode_column, lm_join_column, lm_join_equi_outer, lm_table_name_atm_id, primary_key_id, table_name_atm_id) VALUES ('CED_ACT_TAKEN_ID_SUSPECT', "
                + "'ACT_TAKEN_ID_SUSPECT', 'N',NULL,'ACTION_TAKEN','ACT_TAKEN_ID','O','LM_ACTION_TAKEN', NULL, 'CASE_EVENT_DETAIL');")
    }

    changeSet(author: "Amrit Kaur", id:'PVR-1784 rpt_field') {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'1', 'SELECT COUNT(1) FROM argus_table_master where rownum <2;')
        }
        sql("MERGE INTO rpt_field rf  USING (SELECT 1  FROM DUAL) qry  ON (rf.argus_column_master_id = 'CED_ACT_TAKEN_ID_SUSPECT') WHEN NOT MATCHED THEN "+
                " INSERT (id,version,argus_column_master_id, data_type, DATE_FORMAT,description, rpt_field_group_id, is_text, list_domain_class,NAME, query_selectable, "+
                "templt_cll_selectable, templt_dtcol_selectable, templt_dtrow_selectable, transform) VALUES (HIBERNATE_SEQUENCE.nextval,0,'CED_ACT_TAKEN_ID_SUSPECT', 'java.lang.String', 'dd-MMM-yyyy',NULL,(select id"+
                " from rpt_field_group where name ='EventInformation'), 0, 'com.rxlogix.mapping.LmActionTaken','eventDetailActTakenSuspect', 1, 1, 1, 1, 'eventDetailActTakenSuspect');")
    }
}
