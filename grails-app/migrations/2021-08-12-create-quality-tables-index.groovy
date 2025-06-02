databaseChangeLog = {

    changeSet(author: "sachins", id: "202108121001-1") {
        createIndex(indexName: "IDX_QTY_CASE_ISDEL_TENANTID", tableName: "QUALITY_CASE_DATA") {
            column(name: "ISDELETED")
            column(name: "TENANT_ID")
        }
    }
    changeSet(author: "sachins", id: "202108121001-2") {
        createIndex(indexName: "IDX_QTY_SUB_ISDEL_TENANTID", tableName: "QUALITY_SUBMISSION") {
            column(name: "ISDELETED")
            column(name: "TENANT_ID")
        }
    }
     changeSet(author: "sachins", id: "202108121001-3") {
        createIndex(indexName: "IDX_QTY_SAMP_ISDEL_TENANTID", tableName: "QUALITY_SAMPLING") {
            column(name: "ISDELETED")
            column(name: "TENANT_ID")
        }
     }
}
