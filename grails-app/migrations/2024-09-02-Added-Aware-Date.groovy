databaseChangeLog = {

    changeSet(author: "meenal (generated)", id: "202409021232-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'AWARE_DATE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "AWARE_DATE", type: "NUMBER(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "meenal (generated)", id: "202409021232-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'AWARE_DATE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "AWARE_DATE", type: "NUMBER(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "meenal (generated)", id: "202409021232-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNIT_CONFIGURATION', columnName: 'UNIT_ORGANIZATION_NAME')
            }
        }
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "UNIT_ORGANIZATION_NAME", type: "varchar2(100 char)")
            constraints(nullable: "true")
        }
    }

    changeSet(author: "meenal (generated)", id: "202409021232-4") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "RECEIVER_UNIT_ORG_NAME")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECEIVER_UNIT_ORG_NAME", type: "varchar2(100 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "meenal (generated)", id: "202409021232-5") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "SENDER_UNIT_ORG_NAME")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SENDER_UNIT_ORG_NAME", type: "varchar2(100 char)"){
                constraints(nullable: "true")
            }
        }
    }

}
