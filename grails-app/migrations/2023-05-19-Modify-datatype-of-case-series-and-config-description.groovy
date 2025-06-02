databaseChangeLog = {

    changeSet(author: "ashishdhami", id: "202305191640") {
        modifyDataType(columnName: "DESCRIPTION", tableName: "EX_CASE_SERIES", newDataType: "VARCHAR2(4000 CHAR)")
    }
    changeSet(author: "ashishdhami", id: "202305191650") {
        modifyDataType(columnName: "DESCRIPTION", tableName: "CASE_SERIES", newDataType: "VARCHAR2(4000 CHAR)")
    }
    changeSet(author: "ashishdhami", id: "202305191740") {
        modifyDataType(columnName: "DESCRIPTION", tableName: "EX_RCONFIG", newDataType: "VARCHAR2(4000 CHAR)")
    }
    changeSet(author: "ashishdhami", id: "202305191750") {
        modifyDataType(columnName: "DESCRIPTION", tableName: "RCONFIG", newDataType: "VARCHAR2(4000 CHAR)")
    }
}