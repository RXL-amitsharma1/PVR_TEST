databaseChangeLog = {

    changeSet(author: "sergey", id: "202304132000-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "REPORT_REQUEST_FIELD", columnName: "RR_TYPE_ID")
            }
        }
        addColumn(tableName: "REPORT_REQUEST_FIELD") {
            column(name: "RR_TYPE_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "sergey (generated)", id: "202304132000-2") {
        addForeignKeyConstraint(baseColumnNames: "RR_TYPE_ID", baseTableName: "REPORT_REQUEST_FIELD", constraintName: "FK_RR_TYPE_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST_TYPE", referencesUniqueColumn: "false")
    }

}
