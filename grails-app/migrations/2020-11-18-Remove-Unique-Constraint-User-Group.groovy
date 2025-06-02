databaseChangeLog = {

    changeSet(author: "sandeep (generated)", id: "1596029071240-1") {
        preConditions(onFail: 'MARK_RAN') {
            indexExists(indexName: "NAME_UNIQ_1472731486259", tableName: "USER_GROUP")
        }
        dropIndex(indexName: "NAME_UNIQ_1472731486259", tableName: "USER_GROUP")
    }
}