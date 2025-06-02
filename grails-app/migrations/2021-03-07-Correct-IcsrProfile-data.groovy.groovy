databaseChangeLog = {

    changeSet(author: "shikhars", id: "300320210232") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_FIELD', columnName: 'IS_URL_FIELD')
            }
        }

        addColumn(tableName: "RPT_FIELD") {
            column(name: "IS_URL_FIELD", type: "number(1,0)", defaultValue: 0){
                constraints(nullable: "true")
            }
        }
    }
}