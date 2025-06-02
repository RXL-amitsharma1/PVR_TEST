databaseChangeLog = {
    changeSet(author: "gunjan", id: "20241219132001-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DRILLDOWN_METADATA', columnName: 'ASSIGNER')
            }
        }
        addColumn(tableName: "DRILLDOWN_METADATA") {
            column(name: "ASSIGNER", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "gunjan", id: "20241219132001-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DRILLDOWN_METADATA', columnName: 'ASSIGNEE_UPDATED_DATE')
            }
        }
        addColumn(tableName: "DRILLDOWN_METADATA") {
            column(name: "ASSIGNEE_UPDATED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "gunjan", id: "20241219132001-3") {
        sql("UPDATE DRILLDOWN_METADATA set ASSIGNEE_UPDATED_DATE = SYSDATE WHERE ASSIGNEE_UPDATED_DATE IS NULL;")
    }
    changeSet(author: "gunjan", id: "20241219132001-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'IN_DRILLDOWN_METADATA', columnName: 'ASSIGNER')
            }
        }
        addColumn(tableName: "IN_DRILLDOWN_METADATA") {
            column(name: "ASSIGNER", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "gunjan", id: "20241219132001-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'IN_DRILLDOWN_METADATA', columnName: 'ASSIGNEE_UPDATED_DATE')
            }
        }
        addColumn(tableName: "IN_DRILLDOWN_METADATA") {
            column(name: "ASSIGNEE_UPDATED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "gunjan", id: "20241219132001-6") {
        sql("UPDATE IN_DRILLDOWN_METADATA set ASSIGNEE_UPDATED_DATE = SYSDATE WHERE ASSIGNEE_UPDATED_DATE IS NULL;")
    }
}