databaseChangeLog = {

    changeSet(author: "sergey (generated)", id: "202204291211-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ACTION_ITEM', columnName: 'COMMNT')
            }
        }

        addColumn(tableName: "ACTION_ITEM") {

            column(name: "COMMNT", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }

    }
}