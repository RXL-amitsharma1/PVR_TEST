databaseChangeLog = {

    changeSet(author: "sergey", id: "202212021816") {
        modifyDataType(columnName: "NAME", tableName: "PUBLISHER_REPORT", newDataType: "VARCHAR2(4000)")
    }
}