databaseChangeLog = {
    changeSet(author: "shikhars", id: "180720201655") {
        createTable(tableName: "DRILLDOWN_DATA") {

            column(name: "ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }

            column(name: "EX_REPORT_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }

            column(name: "REPORT_RESULT_ID", type: "number(19,0)"){
                constraints(nullable: "false")
            }

            column(name: "CLL_ROW_DATA", type: "clob")

        }
        sql("ALTER TABLE DRILLDOWN_DATA ADD CONSTRAINT CHK_JSON_DRILLDOWN_DATA CHECK(CLL_ROW_DATA IS JSON) ENABLE")
        sql("ALTER TABLE DRILLDOWN_DATA MODIFY CLL_ROW_DATA NOT NULL")
        addPrimaryKey(columnNames: "ID", constraintName: "DRILLDOWN_DATA_PK", tableName: "DRILLDOWN_DATA")
        sql("CREATE SEQUENCE DRILLDOWN_DATA_ID INCREMENT BY 1 START WITH 1 NOMAXVALUE NOMINVALUE NOCYCLE")
    }

    changeSet(author: "sergey", id: "202103101004-1") {
        createIndex(indexName: "RPR_RST_ID_IDX_202103101004", tableName: "DRILLDOWN_DATA", unique: "false") {
            column(name: "REPORT_RESULT_ID")
        }
    }
}