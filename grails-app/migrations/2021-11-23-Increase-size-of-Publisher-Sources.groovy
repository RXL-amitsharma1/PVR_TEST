databaseChangeLog = {

    changeSet(author: "sergey", id: "202111231012") {
        modifyDataType(columnName: "NAME", tableName: "CONFIGURATION_ATTACH", newDataType: "VARCHAR2(400 char)")
        modifyDataType(columnName: "NAME", tableName: "EX_CONFIGURATION_ATTACH", newDataType: "VARCHAR2(400 char)")
    }
}