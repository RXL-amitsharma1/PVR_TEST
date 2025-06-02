databaseChangeLog = {

    changeSet(author: "Prashant (generated)", id: "increase_table_alias_to_10_chars") {
        modifyDataType(columnName: "TABLE_ALIAS", newDataType: "VARCHAR(10)", tableName: "SOURCE_TABLE_MASTER")
    }
}