databaseChangeLog = {
    changeSet(author:"pragyatiwari" , id: "202406171350") {
        modifyDataType(columnName: "DESCRIPTION", tableName: "WORKFLOW_JUSTIFICATION",newDataType: "VARCHAR2(4000 CHAR)")
    }
}