databaseChangeLog = {

    changeSet(author: "emilmatevosyan (generated)", id: "1487936665559-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_FIELD_GROUP', columnName: 'PRIORITY')
            }
        }

        addColumn(tableName: "RPT_FIELD_GROUP") {
            column(name: "PRIORITY", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }

        sql("UPDATE RPT_FIELD_GROUP SET PRIORITY = 0;")
        addNotNullConstraint(tableName: "RPT_FIELD_GROUP", columnName: "PRIORITY")
    }

    changeSet(author: "sachinverma (generated)", id: "1487936665559-2") {
        dropColumn(tableName: "RPT_FIELD_GROUP", columnName: "PRIORITY")
        addColumn(tableName: "RPT_FIELD_GROUP") {
            column(name: "PRIORITY", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE RPT_FIELD_GROUP SET PRIORITY = 0;")
        addNotNullConstraint(tableName: "RPT_FIELD_GROUP", columnName: "PRIORITY")
    }

}
