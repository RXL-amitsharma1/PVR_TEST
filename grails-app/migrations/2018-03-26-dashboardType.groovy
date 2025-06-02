databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1522251054754-1") {
        createTable(tableName: "DASHBOARD_SHARED_W_GRPS") {
            column(name: "DASHBOARD_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_GROUP_ID", type: "number(19,0)")

            column(name: "SHARED_WITH_GROUP_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-2") {
        createTable(tableName: "DASHBOARD_SHARED_WITHS") {
            column(name: "DASHBOARD_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "number(19,0)")

            column(name: "SHARED_WITH_IDX", type: "number(10,0)")
        }
    }
    changeSet(author: "forxsv (generated)", id: "1522251054754-3") {
        addColumn(tableName: "DASHBOARD") {
            column(name: "CREATED_BY", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "forxsv", id: "1522251054754-4") {
        sql("update DASHBOARD set CREATED_BY='application'");
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-5") {
        addNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "CREATED_BY", tableName: "DASHBOARD")
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-6") {
        addColumn(tableName: "DASHBOARD") {
            column(name: "DASHBOARD_TYPE", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "forxsv", id: "1522251054754-7") {
        sql("update DASHBOARD set DASHBOARD_TYPE='PVR_MAIN'");
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-8") {
        addNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "DASHBOARD_TYPE", tableName: "DASHBOARD")
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-9") {
        addColumn(tableName: "DASHBOARD") {
            column(name: "DATE_CREATED", type: "timestamp")
        }
    }

    changeSet(author: "forxsv", id: "1522251054754-10") {
        sql("update DASHBOARD set DATE_CREATED=systimestamp");
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-11") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "DATE_CREATED", tableName: "DASHBOARD")
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-12") {
        addColumn(tableName: "DASHBOARD") {
            column(name: "IS_DELETED", type: "number(1,0)")
        }
    }

    changeSet(author: "forxsv", id: "1522251054754-13") {
        sql("update DASHBOARD set IS_DELETED=0");
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-14") {
        addNotNullConstraint(columnDataType: "number(1,0)", columnName: "IS_DELETED", tableName: "DASHBOARD")
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-15") {
        addColumn(tableName: "DASHBOARD") {
            column(name: "LABEL", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-16") {
        addColumn(tableName: "DASHBOARD") {
            column(name: "LAST_UPDATED", type: "timestamp")
        }
    }

    changeSet(author: "forxsv", id: "1522251054754-17") {
        sql("update DASHBOARD set LAST_UPDATED=systimestamp");
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-18") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "LAST_UPDATED", tableName: "DASHBOARD")
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-19") {
        addColumn(tableName: "DASHBOARD") {
            column(name: "MODIFIED_BY", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "forxsv", id: "1522251054754-20") {
        sql("update DASHBOARD set MODIFIED_BY='application'");
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-21") {
        addNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "MODIFIED_BY", tableName: "DASHBOARD")
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-29") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "PVUSER_ID", tableName: "DASHBOARD")
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-120") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_GROUP_ID", baseTableName: "DASHBOARD_SHARED_W_GRPS", constraintName: "FK_aud6g70dmw7wq7mphems3anyv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1522251054754-121") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "DASHBOARD_SHARED_WITHS", constraintName: "FK_e51gd71lgio0unyum1fs8m7r1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

}
