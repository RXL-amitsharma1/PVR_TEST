databaseChangeLog = {

    changeSet(author: "gunjan", id: "202407111107-1") {
        modifyDataType(columnName:"INVESTIGATION", tableName:"QUALITY_ISSUE_DETAIL", newDataType:"VARCHAR2(32000)")
        modifyDataType(columnName:"SUMMARY", tableName:"QUALITY_ISSUE_DETAIL", newDataType:"VARCHAR2(32000)")
        modifyDataType(columnName:"ACTIONS", tableName:"QUALITY_ISSUE_DETAIL", newDataType:"VARCHAR2(32000)")
    }
}