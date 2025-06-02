databaseChangeLog = {

    changeSet(author: "sahil", id: "202412051519-1") {
        modifyDataType(columnName: "ASSOCIATED_SPOTFIRE_FILE", newDataType: "VARCHAR2(8000 char)", tableName: "EX_CASE_SERIES")
    }
    changeSet(author: "sahil", id: "202412051525-1") {
        modifyDataType(columnName: "ASSOCIATED_SPOTFIRE_FILE", newDataType: "VARCHAR2(8000 char)", tableName: "EX_RCONFIG")
    }

}