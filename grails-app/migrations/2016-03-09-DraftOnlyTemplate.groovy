databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1457502904112-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_TEMPLT_QUERY', columnName: 'DRAFT_ONLY')
            }
        }
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "DRAFT_ONLY", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_TEMPLT_QUERY set DRAFT_ONLY = 0")
        addNotNullConstraint(tableName: "EX_TEMPLT_QUERY", columnName: "DRAFT_ONLY")
    }

    changeSet(author: "sachinverma (generated)", id: "1457502904112-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'TEMPLT_QUERY', columnName: 'DRAFT_ONLY')
            }
        }
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "DRAFT_ONLY", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update TEMPLT_QUERY set DRAFT_ONLY = 0")
        addNotNullConstraint(tableName: "TEMPLT_QUERY", columnName: "DRAFT_ONLY")
    }
}
