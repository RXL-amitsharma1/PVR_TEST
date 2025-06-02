databaseChangeLog = {

    changeSet(author: "rishabh", id: "202209281705") {
        modifyDataType(columnName: "DESCRIPTION", tableName: "CAPA_8D", newDataType: "VARCHAR2(32000 BYTE)")
    }

}