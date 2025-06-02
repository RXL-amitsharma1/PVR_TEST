databaseChangeLog = {

    changeSet(author: "sergey", id: "202109240000-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'NONCASE_SQL_TEMPLT', columnName: 'SPECIAL_SETTINGS')
            }
        }
        addColumn(tableName: "NONCASE_SQL_TEMPLT") {
            column(name: "SPECIAL_SETTINGS", type: "VARCHAR2(4000)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sergey", id: "202109240000-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'NONCASE_SQL_TEMPLT', columnName: 'EXPORT_AS_IMAGE')
            }
        }
        addColumn(tableName: "NONCASE_SQL_TEMPLT") {
            column(name: "EXPORT_AS_IMAGE", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

}

