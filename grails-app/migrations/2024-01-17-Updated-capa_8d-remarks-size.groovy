databaseChangeLog = {
    changeSet(author: "vivekkumar", id: "202401171802-1") {
        modifyDataType(tableName: "CAPA_8D", columnName: "REMARKS", newDataType: "raw(4000)")
    }
}