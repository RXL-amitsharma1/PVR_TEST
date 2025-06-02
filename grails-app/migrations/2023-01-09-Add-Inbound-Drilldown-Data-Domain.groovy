databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "202301111317-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DRILLDOWN_DATA', columnName: 'SENDER_ID')
            }
        }
        addColumn(tableName: "DRILLDOWN_DATA") {
            column(name: "CASE_VERSION", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "SENDER_ID", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag", id: "202301111317-2") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "IN_DRILLDOWN_METADATA")
            }
        }
        createTable(tableName: "IN_DRILLDOWN_METADATA") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CASE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "VERSION_NUM", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SENDER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "TENANT_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_STATE_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "DUE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "WORKFLOW_STATE_UPDATED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO_USER", type: "number(19, 0)") {
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO_USERGROUP", type: "number(19, 0)") {
                constraints(nullable: "true")
            }

            column(name: "detection_date", type: "timestamp") {
                constraints(nullable: "false")
            }

        }
        addPrimaryKey(columnNames: "ID", constraintName: "IN_DRILLDOWN_METADATA_PK", tableName: "IN_DRILLDOWN_METADATA")
        sql("CREATE SEQUENCE IN_DRILLDOWN_METADATA_ID INCREMENT BY 1 START WITH 1 NOMAXVALUE NOMINVALUE NOCYCLE")
    }

    changeSet(author: "anurag", id: "202301111317-3") {
        createTable(tableName: "IN_DDWN_MDATA_ACTN_ITEM") {
            column(name: "CLL_ROW_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ITEM_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "CLL_ROW_ID", baseTableName: "IN_DDWN_MDATA_ACTN_ITEM", constraintName: "FKC472AB88C6458485", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "IN_DRILLDOWN_METADATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ACTION_ITEM_ID", baseTableName: "IN_DDWN_MDATA_ACTN_ITEM", constraintName: "FKC472AC88C6458485", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "CLL_ROW_ID, ACTION_ITEM_ID", constraintName: "IN_DDWN_MDATA_ACTN_ITEM_PK", tableName: "IN_DDWN_MDATA_ACTN_ITEM")
    }

    changeSet(author: "anurag", id: "202301111317-4") {
        createTable(tableName: "IN_DDWN_MDATA_CMNTS") {
            column(name: "CLL_ROW_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "CLL_ROW_ID", baseTableName: "IN_DDWN_MDATA_CMNTS", constraintName: "FKC472BA88C6458485", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "IN_DRILLDOWN_METADATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "IN_DDWN_MDATA_CMNTS", constraintName: "FKC472BB88C6458485", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "CLL_ROW_ID, COMMENT_ID", constraintName: "IN_DDWN_MDATA_CMNTS_PK", tableName: "IN_DDWN_MDATA_CMNTS")
    }

    changeSet(author: "anurag", id: "202301111317-5") {
        createTable(tableName: "IN_DDWN_MDATA_ISSUES") {
            column(name: "CLL_ROW_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ISSUE_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "CLL_ROW_ID", baseTableName: "IN_DDWN_MDATA_ISSUES", constraintName: "FKC472BC88C6458485", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "IN_DRILLDOWN_METADATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ISSUE_ID", baseTableName: "IN_DDWN_MDATA_ISSUES", constraintName: "FKC472BD88C6458485", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CAPA_8D", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "CLL_ROW_ID, ISSUE_ID", constraintName: "IN_DDWN_MDATA_ISSUES_PK", tableName: "IN_DDWN_MDATA_ISSUES")
    }

    changeSet(author: "anurag", id: "202301111317-16") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', 'select COUNT(*) from all_objects where object_type = \'FUNCTION\' and object_name = \'F_UPDATE_IN_DDWN_ASSIGNMENT\';')
        }
        grailsChange {
            change {
                sql.execute("create or replace FUNCTION F_UPDATE_IN_DDWN_ASSIGNMENT (pi_metadataId NUMBER,pi_masterCaseId NUMBER,pi_masterVersionNum NUMBER,pi_masterSenderId NUMBER, pi_masterEnterpriseId NUMBER, pi_defaultWorkStateId NUMBER, pi_dueDate VARCHAR2)\n" +
                        " return NUMBER\n" +
                        " AS\n" +
                        " x number;\n" +
                        " BEGIN\n" +
                        " EXECUTE IMMEDIATE 'merge into IN_DRILLDOWN_METADATA T1 using (select '||pi_metadataId||' as ID\n" +
                        " ,'||pi_masterCaseId||' as CASE_ID,'||pi_masterVersionNum||' as VERSION_NUM,\n" +
                        " '||pi_masterSenderId||' as SENDER_ID,'||pi_masterEnterpriseId||' as TENANT_ID,'||pi_defaultWorkStateId||' as WORKFLOW_STATE_ID\n" +
                        " ,SYSDATE as WORKFLOW_STATE_UPDATED_DATE,SYSDATE as DETECTION_DATE, TO_TIMESTAMP('''||pi_dueDate||''',''YYYY-MM-DD HH24:MI:SS:FF'') as DUE_DATE from dual) T2 ON\n" +
                        " (T1.CASE_ID=T2.CASE_ID AND T1.VERSION_NUM=T2.VERSION_NUM AND T1.SENDER_ID=T2.SENDER_ID AND T1.TENANT_ID=T2.TENANT_ID)\n" +
                        " WHEN NOT MATCHED THEN INSERT ( ID, CASE_ID, VERSION_NUM, SENDER_ID, TENANT_ID,\n" +
                        " WORKFLOW_STATE_ID,WORKFLOW_STATE_UPDATED_DATE,DETECTION_DATE,DUE_DATE) VALUES\n" +
                        " ( T2.ID, T2.CASE_ID, T2.VERSION_NUM, T2.SENDER_ID, T2.TENANT_ID,T2.WORKFLOW_STATE_ID,T2.WORKFLOW_STATE_UPDATED_DATE,T2.DETECTION_DATE,T2.DUE_DATE)';\n" +
                        " x := SQL%ROWCOUNT;\n" +
                        " commit;\n" +
                        " return x;\n" +
                        " END;")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "202301111317-7") {

        createIndex(indexName: "DATECTION_DATE_202103101004", tableName: "IN_DRILLDOWN_METADATA", unique: "false") {
            column(name: "DETECTION_DATE")
        }
        createIndex(indexName: "CASE_ID_IDX_202201121204", tableName: "IN_DRILLDOWN_METADATA", unique: "false") {
            column(name: "CASE_ID")
        }
        createIndex(indexName: "TENANT_ID_IDX_202201121204", tableName: "IN_DRILLDOWN_METADATA", unique: "false") {
            column(name: "TENANT_ID")
        }
        createIndex(indexName: "SENDER_ID_IDX_202201121204", tableName: "IN_DRILLDOWN_METADATA", unique: "false") {
            column(name: "SENDER_ID")
        }
    }

    changeSet(author: "anurag (generated)", id: "202301111317-15") {
        createIndex(indexName: "VERSION_NUM_IDX_202201121204", tableName: "IN_DRILLDOWN_METADATA", unique: "false") {
            column(name: "VERSION_NUM")
        }
        createIndex(indexName: "IN_DD_MT_CID_VN_TN_SID_INDEX", tableName: "IN_DRILLDOWN_METADATA", unique: "true") {
            column(name: "CASE_ID")
            column(name: "VERSION_NUM")
            column(name: "TENANT_ID")
            column(name: "SENDER_ID")
        }
    }


    changeSet(author: "anurag (generated)", id: "202301111317-10") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "METADATA_ID", tableName: "PVC_ATTACH")
    }

    changeSet(author: "anurag (generated)", id: "202301111317-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PVC_ATTACH', columnName: 'IN_METADATA_ID')
            }
        }
        addColumn(tableName: "PVC_ATTACH") {
            column(name: "IN_METADATA_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }

        addForeignKeyConstraint(baseColumnNames: "IN_METADATA_ID", baseTableName: "PVC_ATTACH", constraintName: "FK_PVC_ATT_IN_METDA_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "IN_DRILLDOWN_METADATA", referencesUniqueColumn: "false")
    }

    changeSet(author: "rishabh (generated)", id: "202301111317-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'WORKFLOW_JUSTIFICATION', columnName: 'IN_DRILLDOWN_METADATA')
            }
        }
        addColumn(tableName: "WORKFLOW_JUSTIFICATION") {
            column(name: "IN_DRILLDOWN_METADATA", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "202301111317-13") {
        sql("INSERT INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES (12,1,'Inbound Reason of Delay','IN_DRILLDOWN_RECORD','Action Item related to a Inbound drilldown record',0)")
    }

    changeSet(author: "rishabh (generated)", id: "202301111317-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SENDER_ID', columnName: 'AUTO_ASSIGNMENT')
            }
        }
        addColumn(tableName: "AUTO_ASSIGNMENT") {
            column(name: "SENDER_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
}