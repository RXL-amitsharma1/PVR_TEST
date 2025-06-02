databaseChangeLog = {
    changeSet(author: "sergey", id: "2022053013420000-10") {
        createTable(tableName: "SCHEDULER_CFG_PARAMS") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SCHEDULER_CFG_PARAMS_PK")
            }

            column(name: "VERSION", type: "number(19,0)") {
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
            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "PRIMARY_P_CONTRIBUTOR", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "IS_DELETED", type: "number(1,0)"){
                constraints(nullable: "false")
            }
            column(name: "RUN_DATE", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "CONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "CONFIG_ID", baseTableName: "SCHEDULER_CFG_PARAMS", constraintName: "FK_SCHEDULER_CFG_PARAMS_C_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")

    }
    changeSet(author: "sergey", id: "2022053013420000-11") {
        createTable(tableName: "SCHED_CFG_PAR_COMMENTS") {
            column(name: "SCHED_CFG_PAR_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "SCHED_CFG_PAR_ID", baseTableName: "SCHED_CFG_PAR_COMMENTS", constraintName: "FK_SCHED_CFG_PAR_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SCHEDULER_CFG_PARAMS", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "SCHED_CFG_PAR_COMMENTS", constraintName: "FK_COMMENT_SCHED_CFG_PAR", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "SCHED_CFG_PAR_ID, COMMENT_ID", constraintName: "SCHED_CFG_PAR_COMMENTS_PK", tableName: "SCHED_CFG_PAR_COMMENTS")
    }

    changeSet(author: "sergey", id: "2022053013420000-12") {
        createTable(tableName: "SCHED_CFG_PARA_P_C_USERS") {
            column(name: "SCHED_CFG_PAR_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "SCHED_CFG_PAR_ID", baseTableName: "SCHED_CFG_PARA_P_C_USERS", constraintName: "FK_SCHED_CFG_P_C_U_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SCHEDULER_CFG_PARAMS", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "SCHED_CFG_PARA_P_C_USERS", constraintName: "FK_USER_SCHED_CFG_P_C_U", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "SCHED_CFG_PAR_ID, USER_ID", constraintName: "SCHED_CFG_PARA_P_C_USERS_PK", tableName: "SCHED_CFG_PARA_P_C_USERS")
    }
}