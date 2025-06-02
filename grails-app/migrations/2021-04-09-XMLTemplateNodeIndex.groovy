databaseChangeLog = {

    changeSet(author: "Sachin Verma", id: "20210409025400-1") {
        createIndex(indexName: "PARENT_ID_ATTFK", tableName: "XML_TEMPLT_NODE", unique: "false") {
            column(name: "parent_id")
        }
    }
}