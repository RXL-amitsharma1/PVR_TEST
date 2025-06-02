databaseChangeLog = {
    changeSet(author: "rxl-shivamg1", id: "202412021307-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'EX_GLOBAL_DATE_RNG_IDX')
            }
        }
        createIndex(indexName: "EX_GLOBAL_DATE_RNG_IDX", tableName: "EX_RCONFIG") {
            column(name: "EX_GLOBAL_DATE_RANGE_INFO_ID")
        }
    }
}
