databaseChangeLog = {
    changeSet(author: "sergey", id: "202205060001") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EMAIL_TEMPLATE', columnName: 'CC_ID')
            }
        }
        addColumn(tableName: "EMAIL_TEMPLATE") {
            column(name: "CC_ID", type: "varchar2(1000 char)") {
                constraints(nullable: "true")
            }
        }
        addColumn(tableName: "EMAIL_TEMPLATE") {
            column(name: "TO_ID", type: "varchar2(1000 char)") {
                constraints(nullable: "true")
            }
        }
    }
}