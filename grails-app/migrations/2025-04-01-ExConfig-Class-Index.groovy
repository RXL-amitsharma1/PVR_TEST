databaseChangeLog = {
    changeSet(author: "Siddharth", id: "20250401183120-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'EX_RCNFG_CLSS_IDX')
            }
        }
        createIndex(indexName: "EX_RCNFG_CLSS_IDX", tableName: "EX_RCONFIG") {
            column(name: "CLASS")
        }
    }
}
