databaseChangeLog = {

    changeSet(author: "rishabhj", id: "202101251147") {

        modifyDataType(columnName: "DESCRIPTION", tableName: "ACTION_ITEM", newDataType: "VARCHAR2(8000 BYTE)")
        modifyDataType(columnName: "DESCRIPTION", tableName: "REPORT_REQUEST", newDataType: "VARCHAR2(8000 BYTE)")
        modifyDataType(columnName: "DESCRIPTION", tableName: "REPORT_REQUEST_LINK", newDataType: "VARCHAR2(8000 BYTE)")
        modifyDataType(columnName: "REPORT_COMMENT", tableName: "REPORT_REQUEST_COMMENT", newDataType: "VARCHAR2(8000 BYTE)")
        modifyDataType(columnName: "NOTE", tableName: "COMMENT_TABLE", newDataType: "VARCHAR2(8000 BYTE)")
    }
}