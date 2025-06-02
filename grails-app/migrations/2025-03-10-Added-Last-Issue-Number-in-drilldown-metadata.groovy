databaseChangeLog = {

    changeSet(author: "sahil", id: "202503101216-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DRILLDOWN_METADATA', columnName: 'LAST_UPDATED_ISSUE')
            }
        }
        addColumn(tableName: "DRILLDOWN_METADATA") {
            column(name: "LAST_UPDATED_ISSUE", type: "varchar2(8000 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sahil", id: "202503101216-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'IN_DRILLDOWN_METADATA', columnName: 'LAST_UPDATED_ISSUE')
            }
        }
        addColumn(tableName: "IN_DRILLDOWN_METADATA") {
            column(name: "LAST_UPDATED_ISSUE", type: "varchar2(8000 char)") {
                constraints(nullable: "true")
            }
        }
    }

}