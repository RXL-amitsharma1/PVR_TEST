databaseChangeLog = {
    
    changeSet(author: "Nitin Nepalia", id: "202111161750-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'INCLUDE_PRODUCT_OBLIGATION')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "INCLUDE_PRODUCT_OBLIGATION", type: "NUMBER(19,0)", defaultValue:0) {
                constraints(nullable: "true")
            }
        }
    }
    
    changeSet(author: "Nitin Nepalia", id: "202111161750-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'INCLUDE_STUDY_OBLIGATION')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "INCLUDE_STUDY_OBLIGATION", type: "NUMBER(19,0)", defaultValue:0) {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nitin Nepalia", id: "202111161750-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'INCLUDE_PRODUCT_OBLIGATION')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "INCLUDE_PRODUCT_OBLIGATION", type: "NUMBER(19,0)", defaultValue:0) {
                constraints(nullable: "true")
            }
        }
    }
    
    changeSet(author: "Nitin Nepalia", id: "202111161750-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'INCLUDE_STUDY_OBLIGATION')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "INCLUDE_STUDY_OBLIGATION", type: "NUMBER(19,0)", defaultValue:0) {
                constraints(nullable: "true")
            }
        }
    }
}
