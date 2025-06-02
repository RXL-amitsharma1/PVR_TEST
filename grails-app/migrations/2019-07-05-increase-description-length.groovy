databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1524031061111-1") {
        modifyDataType(columnName: "DESCRIPTION", newDataType: "VARCHAR(4000)", tableName: "RCONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1524031061111-2") {
        modifyDataType(columnName: "DESCRIPTION", newDataType: "VARCHAR(4000)", tableName: "EX_RCONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1524031061111-3") {
        modifyDataType(columnName: "DESCRIPTION", newDataType: "VARCHAR(4000)", tableName: "CASE_SERIES")
    }

    changeSet(author: "prashantsahi (generated)", id: "1524031061111-4") {
        modifyDataType(columnName: "DESCRIPTION", newDataType: "VARCHAR(4000)", tableName: "EX_CASE_SERIES")
    }
}
