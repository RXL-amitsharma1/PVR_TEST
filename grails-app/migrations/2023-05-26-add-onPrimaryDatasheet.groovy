databaseChangeLog = {

    changeSet(author: "sergey", id: "202305252000-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RPT_FIELD_INFO", columnName: "ON_PRIMARY_DATASHEET")
            }
        }
        addColumn(tableName: "RPT_FIELD_INFO") {
            column(name: "ON_PRIMARY_DATASHEET", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

}
