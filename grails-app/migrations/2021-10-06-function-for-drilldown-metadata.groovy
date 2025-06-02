databaseChangeLog = {

    changeSet(author: "anurag", id: "202110060001-1") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', 'select COUNT(*) from all_objects where object_type = \'FUNCTION\' and object_name = \'f_update_drilldown_assignment\';')
        }
        grailsChange {
            change {
                sql.execute("CREATE FUNCTION f_update_drilldown_assignment (pi_metadataId NUMBER,pi_masterCaseId NUMBER,pi_processedReportId NUMBER,pi_masterEnterpriseId NUMBER, pi_defaultWorkStateId NUMBER)\n" +
                        " return NUMBER\n" +
                        " AS\n" +
                        " x number;\n" +
                        " BEGIN\n" +
                        " EXECUTE IMMEDIATE 'merge into DRILLDOWN_METADATA T1 using (select '||pi_metadataId||' as ID\n" +
                        " ,'||pi_masterCaseId||' as CASE_ID,'||pi_processedReportId||' as PROCESSED_REPORT_ID,\n" +
                        " '||pi_masterEnterpriseId||' as TENANT_ID,'||pi_defaultWorkStateId||' as WORKFLOW_STATE_ID\n" +
                        " ,SYSDATE as WORKFLOW_STATE_UPDATED_DATE,SYSDATE as DETECTION_DATE from dual) T2 ON\n" +
                        " (T1.CASE_ID=T2.CASE_ID AND T1.PROCESSED_REPORT_ID=T2.PROCESSED_REPORT_ID AND T1.TENANT_ID=T2.TENANT_ID)\n" +
                        " WHEN NOT MATCHED THEN INSERT ( ID, CASE_ID, PROCESSED_REPORT_ID, TENANT_ID,\n" +
                        " WORKFLOW_STATE_ID, WORKFLOW_STATE_UPDATED_DATE,DETECTION_DATE) VALUES\n" +
                        " ( T2.ID, T2.CASE_ID, T2.PROCESSED_REPORT_ID, T2.TENANT_ID,T2.WORKFLOW_STATE_ID,T2.WORKFLOW_STATE_UPDATED_DATE,T2.DETECTION_DATE)';\n" +
                        " x := SQL%ROWCOUNT;\n" +
                        " commit;\n" +
                        " return x;\n" +
                        " END;")
            }
        }
    }

    changeSet(author: "amit", id: "202207120401-1") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', 'select count(*) from all_arguments where object_name = \'F_UPDATE_DRILLDOWN_ASSIGNMENT\' AND ARGUMENT_NAME = \'PI_DUEDATE\';')
        }
        grailsChange {
            change {
                sql.execute("drop FUNCTION f_update_drilldown_assignment")
                sql.execute("create or replace FUNCTION f_update_drilldown_assignment (pi_metadataId NUMBER,pi_masterCaseId NUMBER,pi_processedReportId NUMBER,pi_masterEnterpriseId NUMBER, pi_defaultWorkStateId NUMBER, pi_dueDate VARCHAR2)\n" +
                        " return NUMBER\n" +
                        " AS\n" +
                        " x number;\n" +
                        " BEGIN\n" +
                        " EXECUTE IMMEDIATE 'merge into DRILLDOWN_METADATA T1 using (select '||pi_metadataId||' as ID\n" +
                        " ,'||pi_masterCaseId||' as CASE_ID,'||pi_processedReportId||' as PROCESSED_REPORT_ID,\n" +
                        " '||pi_masterEnterpriseId||' as TENANT_ID,'||pi_defaultWorkStateId||' as WORKFLOW_STATE_ID\n" +
                        " ,SYSDATE as WORKFLOW_STATE_UPDATED_DATE,SYSDATE as DETECTION_DATE, TO_TIMESTAMP('''||pi_dueDate||''',''YYYY-MM-DD HH24:MI:SS:FF'') as DUE_DATE from dual) T2 ON\n" +
                        " (T1.CASE_ID=T2.CASE_ID AND T1.PROCESSED_REPORT_ID=T2.PROCESSED_REPORT_ID AND T1.TENANT_ID=T2.TENANT_ID)\n" +
                        " WHEN NOT MATCHED THEN INSERT ( ID, CASE_ID, PROCESSED_REPORT_ID, TENANT_ID,\n" +
                        " WORKFLOW_STATE_ID,WORKFLOW_STATE_UPDATED_DATE,DETECTION_DATE,DUE_DATE) VALUES\n" +
                        " ( T2.ID, T2.CASE_ID, T2.PROCESSED_REPORT_ID, T2.TENANT_ID,T2.WORKFLOW_STATE_ID,T2.WORKFLOW_STATE_UPDATED_DATE,T2.DETECTION_DATE,T2.DUE_DATE)';\n" +
                        " x := SQL%ROWCOUNT;\n" +
                        " commit;\n" +
                        " return x;\n" +
                        " END;")
            }
        }
    }
}