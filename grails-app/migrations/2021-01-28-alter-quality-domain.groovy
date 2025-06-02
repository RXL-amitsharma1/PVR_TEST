databaseChangeLog = {
    changeSet(author: "anurag", id: "21012820202358-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'WORKFLOW_STATE_UPDATED_DATE')
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "WORKFLOW_STATE_UPDATED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag", id: "21012820202358-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'WORKFLOW_STATE_UPDATED_DATE')
            }
        }
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "WORKFLOW_STATE_UPDATED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag", id: "21012820202358-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'WORKFLOW_STATE_UPDATED_DATE')
            }
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "WORKFLOW_STATE_UPDATED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag", id: "21012820202358-4") {
        sql("UPDATE QUALITY_CASE_DATA set WORKFLOW_STATE_UPDATED_DATE = SYSDATE WHERE WORKFLOW_STATE_UPDATED_DATE IS NULL;")
        sql("UPDATE QUALITY_SAMPLING set WORKFLOW_STATE_UPDATED_DATE = SYSDATE WHERE WORKFLOW_STATE_UPDATED_DATE IS NULL;")
        sql("UPDATE QUALITY_SUBMISSION set WORKFLOW_STATE_UPDATED_DATE = SYSDATE WHERE WORKFLOW_STATE_UPDATED_DATE IS NULL;")
    }
}