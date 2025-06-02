databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1481101007593-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'QUALITY_CHECKED')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "QUALITY_CHECKED", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }

        sql("update EX_RCONFIG set QUALITY_CHECKED = 0;")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "QUALITY_CHECKED")
    }

    changeSet(author: "prashantsahi (generated)", id: "1481101007593-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'QUALITY_CHECKED')
            }
        }

        addColumn(tableName: "RCONFIG") {
            column(name: "QUALITY_CHECKED", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }

        sql("update RCONFIG set QUALITY_CHECKED = 0;")
        addNotNullConstraint(tableName: "RCONFIG", columnName: "QUALITY_CHECKED")
    }

}
