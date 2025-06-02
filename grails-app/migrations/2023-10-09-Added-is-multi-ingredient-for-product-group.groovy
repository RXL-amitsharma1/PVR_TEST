databaseChangeLog = {

    changeSet(author: "rxl-shivam-gupta1 (generated)", id: "202310091449") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DICTIONARY_GROUP', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "DICTIONARY_GROUP") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }
}

