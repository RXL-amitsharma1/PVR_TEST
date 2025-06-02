databaseChangeLog = {

    changeSet(author: "riya", id: "180720231104-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'NOTIFICATION', columnName: 'NOTIFICATION_PARAMETERS')
            }
        }

        addColumn(tableName: "NOTIFICATION") {
            column(name: "NOTIFICATION_PARAMETERS", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }

    }
}