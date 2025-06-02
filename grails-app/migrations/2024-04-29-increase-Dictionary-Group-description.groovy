databaseChangeLog = {

    changeSet(author: "rxl-shivamg1", id: "202404291101-1") {
        modifyDataType(columnName: "DESCRIPTION", newDataType: "VARCHAR2(8000 char)", tableName: "DICTIONARY_GROUP")
    }

}
