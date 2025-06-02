databaseChangeLog = {

    changeSet(author: "pranjal", id: "20210129030030-1") {
        modifyDataType(columnName: "NAME", tableName: "SUPER_QUERY", newDataType: "VARCHAR2(1000 char)")
    }
}

