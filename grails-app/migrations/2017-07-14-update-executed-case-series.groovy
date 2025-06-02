databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1500022766021-1") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'No admin user found.', onErrorMessage: 'No admin user found.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(*) FROM PVUSER u where u.USERNAME=\'admin\';')
        }
        sql("insert into EX_CASE_DELIVERY (id,version,EXECUTED_CASE_ID)(select HIBERNATE_SEQUENCE.nextval,1,c.ID from EX_CASE_SERIES c where c.id not in (select EXECUTED_CASE_ID from EX_CASE_DELIVERY ) );\n" +
                "insert into EX_CASE_DELIVERIES_SHRD_WTHS (EX_DELIVERY_ID, SHARED_WITH_ID, SHARED_WITH_IDX)  (select d.id, (select u.id from PVUSER u where u.USERNAME='admin'),0 from EX_CASE_DELIVERY d);");
    }
}