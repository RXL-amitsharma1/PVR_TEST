databaseChangeLog = {
    changeSet(author: "sergey", id: "202505308141977-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'EX_STATUS_ENTITY_ID_IDX')
            }
        }
        createIndex(indexName: "EX_STATUS_ENTITY_ID_IDX", tableName: "EX_STATUS") {
            column(name: "ENTITY_ID")
        }
    }
}