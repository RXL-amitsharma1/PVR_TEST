databaseChangeLog = {

    changeSet(author: "sargam ", id: "1523257222354-1") {
        modifyDataType(columnName: "TABLE_ALIAS", newDataType: "varchar2(10 char)", tableName: "SOURCE_TABLE_MASTER")
        }
    }