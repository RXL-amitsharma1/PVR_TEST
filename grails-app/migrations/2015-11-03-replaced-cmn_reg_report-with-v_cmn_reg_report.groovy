databaseChangeLog = {

    changeSet(author: "Amrit Kaur", id:'drop FK on argus_column_master') {
        sql("alter table ARGUS_COLUMN_MASTER drop constraint FKEB1F49C0780789E0;")
    }

    changeSet(author: "Amrit Kaur", id:'drop FK on case_column_join_mapping') {
        sql("alter table CASE_COLUMN_JOIN_MAPPING drop constraint FK5D4E9D53780789E0;")
    }

    changeSet(author: "Amrit Kaur", id:'change table cmn_reg_reports to v_cmn_reg_reports') {
        sql("UPDATE ARGUS_TABLE_MASTER SET TABLE_NAME = 'V_CMN_REG_REPORTS' where TABLE_NAME = 'CMN_REG_REPORTS';")
    }

    changeSet(author: "Amrit Kaur", id:'change join table cmn_reg_reports to v_cmn_reg_reports') {
        sql("UPDATE CASE_COLUMN_JOIN_MAPPING SET TABLE_NAME_ATM_ID = 'V_CMN_REG_REPORTS' where TABLE_NAME_ATM_ID = 'CMN_REG_REPORTS';")
    }

    changeSet(author: "Amrit Kaur", id:'change cmn_reg_reports to v_cmn_reg_reports') {
        sql("UPDATE ARGUS_COLUMN_MASTER SET TABLE_NAME_ATM_ID = 'V_CMN_REG_REPORTS' where TABLE_NAME_ATM_ID = 'CMN_REG_REPORTS';")
    }

    changeSet(author: "Amrit Kaur", id:'create FK on argus_column_master') {
        sql("ALTER TABLE ARGUS_COLUMN_MASTER ADD CONSTRAINT FKEB1F49C0780789E0  FOREIGN KEY (TABLE_NAME_ATM_ID)  REFERENCES ARGUS_TABLE_MASTER (TABLE_NAME) ;")
    }

    changeSet(author: "Amrit Kaur", id:'create FK on case_column_join_mapping') {
        sql("ALTER TABLE CASE_COLUMN_JOIN_MAPPING ADD CONSTRAINT FK5D4E9D53780789E0  FOREIGN KEY (TABLE_NAME_ATM_ID)  REFERENCES ARGUS_TABLE_MASTER (TABLE_NAME) ;")
    }

}