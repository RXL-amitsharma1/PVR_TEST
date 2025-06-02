databaseChangeLog = {
    changeSet(author:"riya" , id: "202301311612") {
        modifyDataType(columnName: "CREATED_BY", tableName: "INBOUND_COMPLIANCE",newDataType: "VARCHAR2(100 char)")
        modifyDataType(columnName: "MODIFIED_BY", tableName: "INBOUND_COMPLIANCE",newDataType: "VARCHAR2(100 char)")
        modifyDataType(columnName: "CREATED_BY", tableName: "EX_INBOUND_COMPLIANCE",newDataType: "VARCHAR2(100 char)")
        modifyDataType(columnName: "MODIFIED_BY", tableName: "EX_INBOUND_COMPLIANCE",newDataType: "VARCHAR2(100 char)")
        modifyDataType(columnName: "CREATED_BY", tableName: "QUERY_COMPLIANCE",newDataType: "VARCHAR2(100 char)")
        modifyDataType(columnName: "MODIFIED_BY", tableName: "QUERY_COMPLIANCE",newDataType: "VARCHAR2(100 char)")
        modifyDataType(columnName: "CREATED_BY", tableName: "EX_QUERY_COMPLIANCE",newDataType: "VARCHAR2(100 char)")
        modifyDataType(columnName: "MODIFIED_BY", tableName: "EX_QUERY_COMPLIANCE",newDataType: "VARCHAR2(100 char)")
    }
}