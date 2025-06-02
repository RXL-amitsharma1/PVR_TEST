databaseChangeLog = {

    changeSet(author: "Prashant (generated)", id: "1471469273920-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ACTION_ITEM', columnName: 'IS_DELETED')
            }
        }
        addColumn(tableName: "ACTION_ITEM") {
            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update ACTION_ITEM set IS_DELETED = 0;")
        addNotNullConstraint(tableName: "ACTION_ITEM", columnName: "IS_DELETED")
    }
}
