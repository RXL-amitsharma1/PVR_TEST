databaseChangeLog = {
    changeSet(author: "rxl-shivamg1(generated)", id: "202310231153-1") {
        modifyDataType(columnName: "MESSAGE", newDataType: "VARCHAR2(500 char)", tableName: "NOTIFICATION")
        modifyDataType(columnName: "MSG_ARGS", newDataType: "VARCHAR2(500 char)", tableName: "NOTIFICATION")
    }
}
