databaseChangeLog = {
    changeSet(author: "RxL-Eugen-Semenov", id: "202406051602") {
        modifyDataType(columnName: "REPORT_COMMENT", tableName: "REPORT_REQUEST_COMMENT", newDataType: "VARCHAR2(8000 CHAR)")
        modifyDataType(columnName: "DESCRIPTION", tableName: "REPORT_REQUEST", newDataType: "VARCHAR2(8000 CHAR)")
        modifyDataType(columnName: "DESCRIPTION", tableName: "REPORT_REQUEST_LINK", newDataType: "VARCHAR2(8000 CHAR)")
        modifyDataType(columnName: "DESCRIPTION", tableName: "ACTION_ITEM", newDataType: "VARCHAR2(8000 CHAR)")
    }
}