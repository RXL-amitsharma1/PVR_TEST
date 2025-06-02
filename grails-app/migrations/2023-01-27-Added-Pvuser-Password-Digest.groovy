databaseChangeLog = {
    changeSet(author: "Amit", id: "270120231219-1") {
        createTable(tableName: "pvuser_password_digests") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "password_digests_string", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "Amit", id: "270120231219-2") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "pvuser_password_digests", constraintName: "FKPVUSER_PASSWORDDIGEST", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER")
    }
    
    changeSet(author: "Amit", id: "270120231459-5") {
        addColumn(tableName: "PVUSER") {
            column(name: "PASSWORD_MODIFIED_TIME", type: "TIMESTAMP")
        }
    }

    changeSet(author: "Amit", id: "270120231857-6") {
        addColumn(tableName: "PVUSER") {
            column(name: "PASSWORD", type: "VARCHAR2(255 CHAR)")
        }
    }

}
