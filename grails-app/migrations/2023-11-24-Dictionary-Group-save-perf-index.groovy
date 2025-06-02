databaseChangeLog = {
    changeSet(author: "ShivamRx (generated)", id: "202311241828-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'DICTIONARY_GROUP_SAVE')
            }
        }
        createIndex(indexName: "DICTIONARY_GROUP_SAVE", tableName: "DICTIONARY_GROUP") {
            column(name: "GROUP_TYPE")
            column(name: "IS_MULTI_INGREDIENT")
            column(name: "IS_DELETED")
        }
    }
}
