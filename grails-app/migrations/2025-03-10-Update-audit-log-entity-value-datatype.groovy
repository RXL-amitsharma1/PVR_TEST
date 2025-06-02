databaseChangeLog = {
    changeSet(author: 'rx-shivamg1', id: '202503101254') {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0',
                    "SELECT COUNT(1) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'AUDIT_LOG' AND COLUMN_NAME = 'ENTITY_VALUE' AND DATA_TYPE = 'VARCHAR2'")
        }

        addColumn(tableName: "AUDIT_LOG") {
            column(name: "ENTITY_VALUE_NEW", type: "varchar2(32000 char)") {
                constraints(nullable: "true")
            }
        }

        sql('UPDATE AUDIT_LOG SET ENTITY_VALUE_NEW = DBMS_LOB.SUBSTR(ENTITY_VALUE, 32000, 1); COMMIT;')
        sql('ALTER TABLE AUDIT_LOG RENAME COLUMN ENTITY_VALUE TO ENTITY_VALUE_OLD;')
        sql('ALTER TABLE AUDIT_LOG RENAME COLUMN ENTITY_VALUE_NEW TO ENTITY_VALUE;')
    }
}
