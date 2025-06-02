databaseChangeLog = {

    changeSet(author: "sergey (generated)", id: "2021061110100000-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DRILLDOWN_METADATA', columnName: 'detection_date')
            }
        }
        addColumn(tableName: "DRILLDOWN_METADATA") {
            column(name: "detection_date", type: "timestamp")
        }
        sql("update DRILLDOWN_METADATA set detection_date=add_months(trunc(sysdate,'mm'),-1)")
    }
    changeSet(author: "sergey (generated)", id: "2021061110100000-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'detectdateind210611')
            }
        }
        createIndex(indexName: "detectdateind210611", tableName: "DRILLDOWN_METADATA", unique: "false") {
            column(name: "detection_date")
        }
    }

    changeSet(author: "anurag (generated)", id: "2021061110100000-5") {
        createIndex(indexName: "DRILLDOWN_MT_CS_TN_PR_INDEX", tableName: "DRILLDOWN_METADATA", unique: "false") {
            column(name: "CASE_ID")
            column(name: "TENANT_ID")
            column(name: "PROCESSED_REPORT_ID")
        }
    }

    changeSet(author: "anurag (generated)", id: "2021061110100000-6") {
        preConditions(onFail: 'MARK_RAN') {
            indexExists(indexName: "DRILLDOWN_MT_CS_TN_PR_INDEX", tableName: "DRILLDOWN_METADATA")
        }
        dropIndex(indexName: "DRILLDOWN_MT_CS_TN_PR_INDEX", tableName: "DRILLDOWN_METADATA")
    }

    changeSet(author: "anurag (generated)", id: "2021061110100000-7") {
        createIndex(indexName: "DRILLDOWN_MT_CS_TN_PR_INDEX", tableName: "DRILLDOWN_METADATA", unique: "true") {
            column(name: "CASE_ID")
            column(name: "TENANT_ID")
            column(name: "PROCESSED_REPORT_ID")
        }
    }


}

