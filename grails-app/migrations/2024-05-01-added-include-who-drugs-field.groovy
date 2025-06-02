databaseChangeLog = {

    changeSet(author: "rxl-shivamg1 (generated)", id: "202405011127-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "INCLUDE_WHO_DRUGS", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rxl-shivamg1 (generated)", id: "202405011127-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "INCLUDE_WHO_DRUGS", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rxl-shivamg1 (generated)", id: "202405011127-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_SERIES', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "CASE_SERIES") {
            column(name: "INCLUDE_WHO_DRUGS", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rxl-shivamg1 (generated)", id: "202405011127-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "INCLUDE_WHO_DRUGS", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rxl-shivamg1 (generated)", id: "202405011127-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'INBOUND_COMPLIANCE', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "INBOUND_COMPLIANCE") {
            column(name: "INCLUDE_WHO_DRUGS", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rxl-shivamg1 (generated)", id: "202405011127-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_INBOUND_COMPLIANCE', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "EX_INBOUND_COMPLIANCE") {
            column(name: "INCLUDE_WHO_DRUGS", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rxl-shivamg1 (generated)", id: "202405011127-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "INCLUDE_WHO_DRUGS", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rxl-shivamg1 (generated)", id: "202406061058-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DICTIONARY_GROUP', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "DICTIONARY_GROUP") {
            column(name: "INCLUDE_WHO_DRUGS", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }
}