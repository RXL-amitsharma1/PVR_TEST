databaseChangeLog = {

    changeSet(author: "sargam", id: "202001020432-1") {
        modifyDataType(columnName:"TASK_NAME", tableName:"TASK", newDataType:"VARCHAR2(4000 CHAR)")
    }
}