databaseChangeLog = {
    changeSet(author: "sachins", id: "2021-11-15-2007") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PUBLISHER_TPL', columnName: 'QUALITY_CHECKED')
            }
        }
        addColumn(tableName: "PUBLISHER_TPL") {
            column(name: "QUALITY_CHECKED", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
        addNotNullConstraint(tableName: "PUBLISHER_TPL", columnName: "QUALITY_CHECKED")
    }
    changeSet(author: "sachins", id: "2021-11-15-2008") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PUBLISHER_TPL_PRM', columnName: 'HIDDEN')
            }
        }
        addColumn(tableName: "PUBLISHER_TPL_PRM") {
            column(name: "HIDDEN", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
        addNotNullConstraint(tableName: "PUBLISHER_TPL_PRM", columnName: "HIDDEN")
    }
}