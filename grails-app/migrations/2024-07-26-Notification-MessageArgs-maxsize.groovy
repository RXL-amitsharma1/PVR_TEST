databaseChangeLog = {
    changeSet(author: "meenal(generated)", id: "202407261502-1") {
        modifyDataType(columnName: "MSG_ARGS", newDataType: "VARCHAR2(555 char)", tableName: "NOTIFICATION")
    }
}
