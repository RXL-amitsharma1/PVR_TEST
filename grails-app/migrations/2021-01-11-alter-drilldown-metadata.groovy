databaseChangeLog = {
    changeSet(author: "anurag", id: "21011120202358-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DRILLDOWN_METADATA', columnName: 'ASSIGNED_TO_USER')
            }
        }
        addColumn(tableName: "DRILLDOWN_METADATA") {
            column(name: "ASSIGNED_TO_USER", type: "number(19, 0)"){
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO_USERGROUP", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag", id: "21011920202358-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DRILLDOWN_METADATA', columnName: 'WORKFLOW_STATE_UPDATED_DATE')
            }
        }
        addColumn(tableName: "DRILLDOWN_METADATA") {
            column(name: "WORKFLOW_STATE_UPDATED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag", id: "21013020202358-1") {
        sql("UPDATE DRILLDOWN_METADATA set WORKFLOW_STATE_UPDATED_DATE = SYSDATE WHERE WORKFLOW_STATE_UPDATED_DATE IS NULL;")
    }
}