databaseChangeLog = {

    changeSet(author: "ShubhamRx", id: "202212061042-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'RF_INFO_LIST_ID_IDX')
            }
        }
            createIndex(indexName: "RF_INFO_LIST_ID_IDX", tableName: "RPT_FIELD_INFO") {
            column(name: "RF_INFO_LIST_ID")
        }
    }

    changeSet(author: "anurag", id: "202212151042-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'ORIG_TEMPLT_ID_IDX')
            }
        }
        createIndex(indexName: "ORIG_TEMPLT_ID_IDX", tableName: "RPT_TEMPLT") {
            column(name: "ORIG_TEMPLT_ID")
        }
    }

    changeSet(author: "anurag", id: "202212151042-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'ORIG_QUERY_ID_IDX')
            }
        }
        createIndex(indexName: "ORIG_QUERY_ID_IDX", tableName: "SUPER_QUERY") {
            column(name: "ORIG_QUERY_ID")
        }
    }
}