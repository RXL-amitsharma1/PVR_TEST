databaseChangeLog = {

    changeSet(author: "Amity", id: "202201182000-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_QTY_CASE_Q')
            }
        }
        createIndex(indexName: "IDX_QTY_CASE_Q", tableName: "QUALITY_CASE_DATA") {
            column(name: "CASE_NUM")
            column(name: "ERROR_TYPE")
            column(name: "TENANT_ID")
            column(name: "VERSION_NUM")
            column(name: "ENTRY_TYPE")
            column(name: "ISDELETED")
        }
    }
    changeSet(author: "Amity", id: "202201182000-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_QTY_SUB_Q')
            }
        }
        createIndex(indexName: "IDX_QTY_SUB_Q", tableName: "QUALITY_SUBMISSION") {
            column(name: "CASE_NUM")
            column(name: "ERROR_TYPE")
            column(name: "TENANT_ID")
            column(name: "VERSION_NUM")
            column(name: "ENTRY_TYPE")
            column(name: "ISDELETED")
        }
    }
    changeSet(author: "Amity", id: "202201182000-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_QTY_SAMP_Q')
            }
        }
        createIndex(indexName: "IDX_QTY_SAMP_Q", tableName: "QUALITY_SAMPLING") {
            column(name: "CASE_NUM")
            column(name: "ERROR_TYPE")
            column(name: "TENANT_ID")
            column(name: "VERSION_NUM")
            column(name: "ENTRY_TYPE")
            column(name: "ISDELETED")
        }
    }

    changeSet(author: "sergey", id: "202212071012-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'SAMPLING_DATE_CREATED')
            }
        }
        createIndex(indexName: "SAMPLING_DATE_CREATED", tableName: "QUALITY_SAMPLING", unique: "false") {
            column(name: "DATE_CREATED")
        }
    }

    changeSet(author: "sergey", id: "202212071012-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'SUBMISSION_DATE_CREATED')
            }
        }
        createIndex(indexName: "SUBMISSION_DATE_CREATED", tableName: "QUALITY_SUBMISSION", unique: "false") {
            column(name: "DATE_CREATED")
        }
    }

    changeSet(author: "sergey", id: "202212071012-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'CASE_DATA_DATE_CREATED')
            }
        }
        createIndex(indexName: "CASE_DATA_DATE_CREATED", tableName: "QUALITY_CASE_DATA", unique: "false") {
            column(name: "DATE_CREATED")
        }
    }
}
