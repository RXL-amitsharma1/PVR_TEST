databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1484038061268-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EMAIL_CONFIGURATION', columnName: 'IS_DELETED')
            }
        }
        addColumn(tableName: "EMAIL_CONFIGURATION") {
            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }

        sql("update EMAIL_CONFIGURATION set IS_DELETED = 0;")
        addNotNullConstraint(tableName: "EMAIL_CONFIGURATION", columnName: "IS_DELETED")
    }
}