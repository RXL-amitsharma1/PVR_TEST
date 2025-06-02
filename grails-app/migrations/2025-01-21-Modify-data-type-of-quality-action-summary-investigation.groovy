databaseChangeLog = {

    changeSet(author: "sahil", id: "202501211540-1") {
        modifyDataType(columnName: "INVESTIGATION", tableName: "TEMPLT_QUERY", newDataType: "VARCHAR2(8000 char)")
        modifyDataType(columnName: "SUMMARY", tableName: "TEMPLT_QUERY", newDataType: "VARCHAR2(8000 char)")
        modifyDataType(columnName: "ACTIONS", tableName: "TEMPLT_QUERY", newDataType: "VARCHAR2(8000 char)")
        modifyDataType(columnName: "INVESTIGATION", tableName: "EX_TEMPLT_QUERY", newDataType: "VARCHAR2(8000 char)")
        modifyDataType(columnName: "SUMMARY", tableName: "EX_TEMPLT_QUERY", newDataType: "VARCHAR2(8000 char)")
        modifyDataType(columnName: "ACTIONS", tableName: "EX_TEMPLT_QUERY", newDataType: "VARCHAR2(8000 char)")
    }
}