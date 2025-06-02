databaseChangeLog = {

    changeSet(author: "Rishabh Jain", id: "2022031211334-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUERY', columnName: 'REASSESS_LISTEDNESS_DATE')
            }
        }
        addColumn(tableName: "QUERY") {
            column(name: "REASSESS_LISTEDNESS_DATE", type: "timestamp", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Rishabh Jain", id: "202203231340-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'TEMPLT_QUERY', columnName: 'REASSESS_LISTEDNESS_DATE')
            }
        }
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "REASSESS_LISTEDNESS_DATE", type: "timestamp", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Rishabh Jain", id: "202203231640-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_TEMPLT_QUERY', columnName: 'REASSESS_LISTEDNESS_DATE')
            }
        }
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "REASSESS_LISTEDNESS_DATE", type: "timestamp", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Rishabh Jain", id: "202204041719-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_TEMPLT', columnName: 'TEMPLT_REASSESS_DATE')
            }
        }
        addColumn(tableName: "RPT_TEMPLT") {
            column(name: "TEMPLT_REASSESS_DATE", type: "timestamp", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Rishabh Jain", id: "202204041721-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'TEMPLT_QUERY', columnName: 'TEMPLT_REASSESS_DATE')
            }
        }
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "TEMPLT_REASSESS_DATE", type: "timestamp", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Rishabh Jain", id: "202204041722-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_TEMPLT_QUERY', columnName: 'TEMPLT_REASSESS_DATE')
            }
        }
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "TEMPLT_REASSESS_DATE", type: "timestamp", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

}