databaseChangeLog = {
    changeSet(author: "sargam (generated)", id: "202007091025-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'timezone')
            }
        }
        addColumn(tableName: "AUDIT_LOG") {
            column(name: "timezone", type: "varchar2(255 char)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sargam (generated)", id: "202007091025-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'browser')
            }
        }
        addColumn(tableName: "AUDIT_LOG") {
            column(name: "browser", type: "varchar2(255 char)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sargam (generated)", id: "202007091025-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'device')
            }
        }
        addColumn(tableName: "AUDIT_LOG") {
            column(name: "device", type: "varchar2(255 char)"){
                constraints(nullable: "true")
            }
        }
    }
}