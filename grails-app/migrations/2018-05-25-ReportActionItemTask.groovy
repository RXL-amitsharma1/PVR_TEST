databaseChangeLog = {


    changeSet(author: "forxsv (generated)", id: "1527589796995-2") {
        createTable(tableName: "REPORT_TASK") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "REPORT_TASKPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_CATEGORY", type: "varchar2(255 char)")

            column(name: "APP_TYPE", type: "varchar2(255 char)")

            column(name: "ASSIGNED_GROUP_ID", type: "number(19,0)")

            column(name: "ASSIGNED_USER_ID", type: "number(19,0)")

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "varchar2(4000 char)") {
                constraints(nullable: "false")
            }

            column(name: "DUE", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATE_SHIFT", type: "number(10,0)")

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "PRIORITY", type: "varchar2(255 char)")

            column(name: "CONFIG_ID", type: "number(19,0)")

            column(name: "TASK_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1527589796991-117") {
        addForeignKeyConstraint(baseColumnNames: "ASSIGNED_GROUP_ID", baseTableName: "REPORT_TASK", constraintName: "FK_ivltytuvkiyq6lyns42yqr3be", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1527589796991-118") {
        addForeignKeyConstraint(baseColumnNames: "ASSIGNED_USER_ID", baseTableName: "REPORT_TASK", constraintName: "FK_j1fdox9xq5m6xssr1yrqj0knb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1527589796991-119") {
        addForeignKeyConstraint(baseColumnNames: "CONFIG_ID", baseTableName: "REPORT_TASK", constraintName: "FK_1812fyh2sf0v4bb4g16ogg2sp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1527589796991-120") {
        addForeignKeyConstraint(baseColumnNames: "TASK_ID", baseTableName: "REPORT_TASK", constraintName: "FK_g5crhq32voqgl1rbswfa0blgn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TASK_TEMPLATE", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1527589796991-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'TASK_TEMPLATE', columnName: 'TYPE')
            }
        }
        addColumn(tableName: "TASK_TEMPLATE") {
            column(name: "TYPE", type: "varchar2(255 char)")
        }
        sql("update TASK_TEMPLATE set TYPE = 'REPORT_REQUEST';")
        addNotNullConstraint(tableName: "TASK_TEMPLATE", columnName: "TYPE")
    }

    changeSet(author: "forxsv (generated)", id: "1527589796991-454") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ACTION_ITEM', columnName: 'CONFIG_ID')
            }
        }
        addColumn(tableName: "ACTION_ITEM") {
            column(name: "CONFIG_ID", type: "number(19,0)")
        }
    }


}
