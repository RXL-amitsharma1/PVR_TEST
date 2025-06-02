databaseChangeLog = {
    changeSet(author:"vaibhav" , id: "202105111100-1") {
        modifyDataType(columnName: "USERNAME", tableName: "PVUSER",newDataType: "VARCHAR2(255 char)")
    }
}
