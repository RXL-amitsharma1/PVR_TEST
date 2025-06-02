databaseChangeLog = {
    changeSet(author: "sergey (generated)", id: "20201205104701-1") {
        createTable(tableName: "PVC_ATTACH") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PVC_ATTACHPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATA", type: "long raw")

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "METADATA_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "sergey (generated)", id: "20201205104701-2") {
        addForeignKeyConstraint(baseColumnNames: "METADATA_ID", baseTableName: "PVC_ATTACH", constraintName: "FK_PVC_ATTACH_METADATA_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DRILLDOWN_METADATA", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergey (generated)", id: "20201205104701-3") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "PVC_DIRTY", type: "number(1,0)")
        }
    }
}