databaseChangeLog = {
    changeSet(author: "sergey", id: "202107066002-1") {
        createTable(tableName: "LOCKED_OBJECTS") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "LOCKED_OBJECTSPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "IDENTIFIER", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "USER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "LOCK_TIME", type: "TIMESTAMP(6)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "sergey", id: "202107066002-2") {
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "LOCKED_OBJECTS", constraintName: "FK_LCKD_OBJ_USER_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

}

