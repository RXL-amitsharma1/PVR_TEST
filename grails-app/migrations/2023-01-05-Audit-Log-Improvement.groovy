databaseChangeLog = {

    changeSet(author: "rishabhj", id: "090120231516-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'AUD_LOG_ID_IDX')
            }
        }
        createIndex(indexName: "AUD_LOG_ID_IDX", tableName: "AUDIT_LOG_FIELD_CHANGE") {
            column(name: "AUDIT_LOG_ID")
        }
    }
}