databaseChangeLog = {
    changeSet(author: "Sachin Verma", id: "20210728204600-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'SERVER_NAME')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "SERVER_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
}