
databaseChangeLog = {

    changeSet(author: "meenal(generated)", id: "202410101144-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ICSR_ORGANIZATION_TYPE', columnName: 'LANG_ID')
            }
        }
        addColumn(tableName: "ICSR_ORGANIZATION_TYPE") {
            column(name: "LANG_ID", type: "varchar2(100 char)") {
                constraints(nullable: "true")
            }
        }
    }
}