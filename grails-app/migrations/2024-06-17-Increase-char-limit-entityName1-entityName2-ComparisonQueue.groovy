databaseChangeLog = {

    changeSet(author: "sahilrao", id: "202406171242-1") {
        preConditions(onFail: 'MARK_RAN') {
                columnExists(tableName: 'COMPARISON_QUEUE', columnName: 'ENTITY_NAME_1')
                columnExists(tableName: 'COMPARISON_QUEUE', columnName: 'ENTITY_NAME_2')
                columnExists(tableName: 'COMPARISON_RESULT', columnName: 'ENTITY_NAME_1')
                columnExists(tableName: 'COMPARISON_RESULT', columnName: 'ENTITY_NAME_2')
        }
        modifyDataType(columnName:"ENTITY_NAME_1", tableName:"COMPARISON_QUEUE", newDataType:"VARCHAR2(512 CHAR)")
        modifyDataType(columnName:"ENTITY_NAME_2", tableName:"COMPARISON_QUEUE", newDataType:"VARCHAR2(500 CHAR)")
        modifyDataType(columnName:"ENTITY_NAME_1", tableName:"COMPARISON_RESULT", newDataType:"VARCHAR2(512 CHAR)")
        modifyDataType(columnName:"ENTITY_NAME_2", tableName:"COMPARISON_RESULT", newDataType:"VARCHAR2(500 CHAR)")

    }
}