databaseChangeLog = {
    changeSet(author: "shikhars", id: "011220200121") {
        createTable(tableName: "DRILLDOWN_METADATA") {

            column(name: "ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }

            column(name: "CASE_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }

            column(name: "CASE_VERSION", type: "number(19,0)"){
                constraints(nullable: "false")
            }

            column(name: "PROCESSED_REPORT_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }

            column(name: "TENANT_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }
        }
        addPrimaryKey(columnNames: "ID", constraintName: "DRILLDOWN_METADATA_PK", tableName: "DRILLDOWN_METADATA")
        sql("CREATE SEQUENCE DRILLDOWN_METADATA_ID INCREMENT BY 1 START WITH 1 NOMAXVALUE NOMINVALUE NOCYCLE")
    }

    changeSet(author: "shikhars", id: "011220200158") {
        createTable(tableName: "DDWN_MDATA_CMNTS") {
            column(name: "CLL_ROW_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "CLL_ROW_ID", baseTableName: "DDWN_MDATA_CMNTS", constraintName: "FK_MCLL_COMMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DRILLDOWN_METADATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "DDWN_MDATA_CMNTS", constraintName: "FK_COMMENT_MCLL", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "CLL_ROW_ID, COMMENT_ID", constraintName: "DDWN_MDATA_CMNTS_PK", tableName: "DDWN_MDATA_CMNTS")
    }

    changeSet(author: "shikhars", id: "011220200206") {
        createTable(tableName: "DDWN_MDATA_ACTN_ITEM") {
            column(name: "CLL_ROW_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "ACTION_ITEM_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "CLL_ROW_ID", baseTableName: "DDWN_MDATA_ACTN_ITEM", constraintName: "FK_MCLL_ACTION_ITEM", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DRILLDOWN_METADATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ACTION_ITEM_ID", baseTableName: "DDWN_MDATA_ACTN_ITEM", constraintName: "FK_ACTION_ITEM_MCLL", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "CLL_ROW_ID, ACTION_ITEM_ID", constraintName: "DDWN_MDATA_ACTN_ITEM_PK", tableName: "DDWN_MDATA_ACTN_ITEM")
    }

    changeSet(author: "shikhars", id: "011220200208") {
        createTable(tableName: "DDWN_MDATA_ISSUES") {
            column(name: "CLL_ROW_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "ISSUE_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "CLL_ROW_ID", baseTableName: "DDWN_MDATA_ISSUES", constraintName: "FK_MCLL_ISSUE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DRILLDOWN_METADATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ISSUE_ID", baseTableName: "DDWN_MDATA_ISSUES", constraintName: "FK_ISSUE_MCLL", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CAPA_8D", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "CLL_ROW_ID, ISSUE_ID", constraintName: "DDWN_MDATA_ISSUES_PK", tableName: "DDWN_MDATA_ISSUES")
    }

    changeSet(author: "anurag", id: "131220200208") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DRILLDOWN_METADATA', columnName: 'WORKFLOW_STATE_ID')
            }
        }
        addColumn(tableName: "DRILLDOWN_METADATA") {
            column(name: "WORKFLOW_STATE_ID", type: "NUMBER(19,0)"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "sergey", id: "202103101012-1") {
        createIndex(indexName: "CASE_ID_IDX_202103101004", tableName: "DRILLDOWN_METADATA", unique: "false") {
            column(name: "CASE_ID")
        }
    }
    changeSet(author: "sergey", id: "202103101012-2") {
        createIndex(indexName: "PROC_RPRT_ID_IDX_202103101004", tableName: "DRILLDOWN_METADATA", unique: "false") {
            column(name: "PROCESSED_REPORT_ID")
        }
    }
    changeSet(author: "sergey", id: "202103101012-3") {
        createIndex(indexName: "TENANT_ID_IDX_202103101004", tableName: "DRILLDOWN_METADATA", unique: "false") {
            column(name: "TENANT_ID")
        }
    }
}