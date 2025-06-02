databaseChangeLog = {

    changeSet(author: "sergey", id: "202303152000-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_QTY_CASE_SIM_CASE')
            }
        }
        createIndex(indexName: "IDX_QTY_CASE_SIM_CASE", tableName: "QUALITY_CASE_DATA") {
            column(name: "CASE_NUM")
            column(name: "TENANT_ID")
            column(name: "VERSION_NUM")
            column(name: "ISDELETED")
        }
    }
    changeSet(author: "sergey", id: "202303152000-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_QTY_SUB_SIM_CASE')
            }
        }
        createIndex(indexName: "IDX_QTY_SUB_SIM_CASE", tableName: "QUALITY_SUBMISSION") {
            column(name: "CASE_NUM")
            column(name: "TENANT_ID")
            column(name: "VERSION_NUM")
            column(name: "ISDELETED")
        }
    }
    changeSet(author: "sergey", id: "202303152000-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_QTY_SAMP_SIM_CASE')
            }
        }
        createIndex(indexName: "IDX_QTY_SAMP_SIM_CASE", tableName: "QUALITY_SAMPLING") {
            column(name: "CASE_NUM")
            column(name: "TENANT_ID")
            column(name: "VERSION_NUM")
            column(name: "TYPE")
            column(name: "ISDELETED")
        }
    }
}
