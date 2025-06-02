databaseChangeLog = {
    changeSet(author: "shikhars", id: "290420201200") {
        dropColumn(columnName: "CAPA_NUMBER", tableName: "CAPA_8D")
        dropColumn(columnName: "VERSION", tableName: "CAPA_8D")
    }
}