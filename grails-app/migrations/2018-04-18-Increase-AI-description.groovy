databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1524031060017-1") {
        modifyDataType(columnName: "DESCRIPTION", newDataType: "VARCHAR(4000)", tableName: "ACTION_ITEM")
    }

}
