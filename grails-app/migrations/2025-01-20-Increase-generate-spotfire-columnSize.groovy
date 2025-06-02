databaseChangeLog = {

    changeSet(author: "sahil", id: "202501201719-1") {
        modifyDataType(columnName: "GENERATE_SPOTFIRE", newDataType: "VARCHAR2(8000 char)", tableName: "CASE_SERIES")
    }
    changeSet(author: "sahil", id: "202501201720-1") {
        modifyDataType(columnName: "GENERATE_SPOTFIRE", newDataType: "VARCHAR2(8000 char)", tableName: "RCONFIG")
    }

}