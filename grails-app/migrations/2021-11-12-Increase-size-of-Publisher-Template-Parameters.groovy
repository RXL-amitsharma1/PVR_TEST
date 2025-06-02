databaseChangeLog = {

    changeSet(author: "rishabhJ", id: "202111121816") {
        modifyDataType(columnName: "DESCRIPTION", tableName: "PUBLISHER_TPL_PRM", newDataType: "VARCHAR2(8000 BYTE)")
        modifyDataType(columnName: "TITLE", tableName: "PUBLISHER_TPL_PRM", newDataType: "VARCHAR2(8000 BYTE)")
        modifyDataType(columnName: "VALUE", tableName: "PUBLISHER_TPL_PRM", newDataType: "VARCHAR2(8000 BYTE)")
    }
}