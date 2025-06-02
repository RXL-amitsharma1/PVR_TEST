databaseChangeLog = {
    changeSet(author: "Siddharth", id: "20250205174201-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'RPT_TMPLT_USR_IDX')
            }
        }
        createIndex(indexName: "RPT_TMPLT_USR_IDX", tableName: "RPT_TEMPLATE_USER") {
            column(name: "USER_ID")
        }
    }

    changeSet(author: "Siddharth", id: "20250205174404-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'RPT_TMPLT_USRGRP_ID_IDX')
            }
        }
        createIndex(indexName: "RPT_TMPLT_USRGRP_ID_IDX", tableName: "RPT_TEMPLATE_USER_GROUP") {
            column(name: "USER_GROUP_ID")
        }
    }

    changeSet(author: "Siddharth", id: "20250205184809-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'PVUSRGRPS_USRGRP_ID_IDX')
            }
        }
        createIndex(indexName: "PVUSRGRPS_USRGRP_ID_IDX", tableName: "PVUSERGROUPS_USERS") {
            column(name: "USER_GROUP_ID")
        }
    }

    changeSet(author: "Siddharth", id: "20250205185046-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'PVUSRGRPS_USR_ID_IDX')
            }
        }
        createIndex(indexName: "PVUSRGRPS_USR_ID_IDX", tableName: "PVUSERGROUPS_USERS") {
            column(name: "USER_ID")
        }
    }
}
