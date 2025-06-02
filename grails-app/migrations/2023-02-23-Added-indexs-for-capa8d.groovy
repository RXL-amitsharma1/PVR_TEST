databaseChangeLog = {

    changeSet(author: "Meenal", id: "202302231944-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'CAPA_IDX')
            }
        }
        createIndex(indexName: "CAPA_IDX", tableName: "CAPA_8D") {
            column(name: "ISSUE_NUMBER")
            column(name: "ISSUE_TYPE")
            column(name: "CATEGORY")
            column(name: "APPROVED_BY_ID")
            column(name: "INITIATOR_ID")
            column(name: "TEAM_LEAD_ID")
        }
    }

}
