databaseChangeLog = {

    changeSet(author: "shubham", id: "202001140314-1") {
        modifyDataType(columnName:"NAME", tableName:"TASK_TEMPLATE", newDataType:"VARCHAR2(4000 CHAR)")
    }
}