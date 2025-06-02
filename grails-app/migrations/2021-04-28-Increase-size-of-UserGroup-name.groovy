databaseChangeLog = {

    changeSet(author: "pranjal", id: "20210428125530-1") {
        modifyDataType(columnName: "NAME", tableName: "USER_GROUP", newDataType: "VARCHAR2(255 char)")
        modifyDataType(columnName: "DESCRIPTION", tableName: "USER_GROUP", newDataType: "VARCHAR2(4000 char)")
    }
}