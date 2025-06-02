databaseChangeLog = {
    changeSet(author: "gunjan(generated)", id: "202304161739-16") {
        modifyDataType(columnName:"CREATED_BY", tableName:"AUTO_REASON_OF_DELAY", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"AUTO_REASON_OF_DELAY", newDataType:"VARCHAR2(100)")
    }
    changeSet(author: "Nitin Nepalia(generated)", id: "202305241231-16") {
        modifyDataType(columnName:"CREATED_BY", tableName:"QUERY_RCA", newDataType:"VARCHAR2(100)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"QUERY_RCA", newDataType:"VARCHAR2(100)")
    }

    changeSet(author: "gunjan(generated)", id: "202306021640-1") {
        modifyDataType(columnName:"CREATED_BY", tableName:"AUTO_REASON_OF_DELAY", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"AUTO_REASON_OF_DELAY", newDataType:"VARCHAR2(255)")
    }
    changeSet(author: "gunjan(generated)", id: "202306021640-2") {
        modifyDataType(columnName:"CREATED_BY", tableName:"QUERY_RCA", newDataType:"VARCHAR2(255)")
        modifyDataType(columnName:"MODIFIED_BY", tableName:"QUERY_RCA", newDataType:"VARCHAR2(255)")
    }
}
