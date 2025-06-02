databaseChangeLog = {
    changeSet(author: "Siddharth", id: "20250227160401-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'EXCONFIG_RPT_NAME_IDX')
            }
        }
        createIndex(indexName: "EXCONFIG_RPT_NAME_IDX", tableName: "EX_RCONFIG") {
            column(name: "REPORT_NAME")
        }
    }
}