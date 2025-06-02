databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1455717945425-1") {
        createTable(tableName: "COMMENT_TABLE") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "COMMENTPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
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

            column(name: "NOTE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1455717945425-2") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "COMMENT_TABLE_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1455717945425-3") {
        addColumn(tableName: "RPT_RESULT") {
            column(name: "COMMENT_TABLE_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1455717945425-9") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_TABLE_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE8826BFB4BC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1455717945425-10") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_TABLE_ID", baseTableName: "RPT_RESULT", constraintName: "FKDC1C5EA626BFB4BC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
    }
}
