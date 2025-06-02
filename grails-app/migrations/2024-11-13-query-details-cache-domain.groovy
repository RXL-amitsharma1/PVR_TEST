databaseChangeLog = {

    changeSet(author: "Siddharth", id: '20241118071944-07') {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "PVS_CACHED_QUERY_SETS")
            }
        }
        createTable(tableName: "PVS_CACHED_QUERY_SETS") {
            column(name: "QUERY_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "CUSTOM_SQL", type: "CLOB") {
                constraints(nullable: "true")
            }
            column(name: "GROUP_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "PARENT_GROUP_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "QUERY_FLAG", type: "NUMBER(10,0)") {
                constraints(nullable: "true")
            }
            column(name: "SET_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "SET_OPERATOR", type: "VARCHAR2(2000 CHAR)") {
                constraints(nullable: "true")
            }
            column(name: "LAST_INS_UPD_DT", type: "DATE") {
                constraints(nullable: "true")
            }
            column(name: "LAST_INS_UPD_USR", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "true")
            }
        }
        createIndex(indexName: "IDX_SETS_QUERY_ID", tableName: "PVS_CACHED_QUERY_SETS") {
            column(name: "QUERY_ID")
        }
    }

    changeSet(author: "Siddharth", id: '20241118071956-07') {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "PVS_CACHED_QUERY_DETAILS")
            }
        }
        createTable(tableName: "PVS_CACHED_QUERY_DETAILS") {
            column(name: "QUERY_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "ADDL_PARAMS", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "true")
            }
            column(name: "CUSTOM_INPUT", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "true")
            }
            column(name: "FIELD_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "FIELD_OPERATOR", type: "VARCHAR2(2000 CHAR)") {
                constraints(nullable: "true")
            }
            column(name: "FIELD_VALUES", type: "CLOB") {
                constraints(nullable: "true")
            }
            column(name: "GROUP_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "GROUP_OPERATOR", type: "VARCHAR2(2000 CHAR)") {
                constraints(nullable: "true")
            }
            column(name: "JAVA_VARIABLE", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "true")
            }
            column(name: "PARENT_GROUP_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "IS_FIELD_COMPARE", type: "NUMBER(10,0)") {
                constraints(nullable: "true")
            }
            column(name: "SET_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "LAST_INS_UPD_DT", type: "DATE") {
                constraints(nullable: "true")
            }
            column(name: "LAST_INS_UPD_USR", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "true")
            }
        }
        createIndex(indexName: "IDX_DETAILS_QUERY_ID", tableName: "PVS_CACHED_QUERY_DETAILS") {
            column(name: "QUERY_ID")
        }
    }
}
