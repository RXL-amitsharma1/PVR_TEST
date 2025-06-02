databaseChangeLog = {
    changeSet(author: "shekhark", id: "040520201431") {
        createTable(tableName: "QUALITY_FIELD") {

            column(name: "ID", type: "number(19,0)"){
                constraints(nullable: false)
            }

            column(name: "QUALITY_MODULE", type: "varchar2(20 char)") {
                constraints(nullable: false)
            }

            column(name: "FIELD_NAME", type : "varchar2(100 char)") {
                constraints(nullable: false)
            }

            column(name: "FIELD_TYPE", type : "varchar2(100 char)")
        }
        addPrimaryKey(columnNames: "ID", constraintName: "QUALITY_FIELD_PK", tableName: "QUALITY_FIELD")
    }
    changeSet(author: "gunjan", id: "180720221105") {
        modifyDataType(columnName: "QUALITY_MODULE", newDataType: "varchar2(100 char)", tableName: "QUALITY_FIELD")
        }

    changeSet(author: "shekhark", id: "040520201438") {
        createTable(tableName: "QUALITY_FIELD_REPORT") {
            column(name: "QUALITY_FIELD_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "REPORT_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "REPORT_ID_IDX", type: "number(19, 0)"){
                constraints(nullable: "false")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "QUALITY_FIELD_ID", baseTableName: "QUALITY_FIELD_REPORT", constraintName: "FK_MAPPING_QUALITY_FIELD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_FIELD", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "QUALITY_FIELD_ID, REPORT_ID", constraintName: "QUALITY_FIELD_REPORT_PK", tableName: "QUALITY_FIELD_REPORT")
    }
}