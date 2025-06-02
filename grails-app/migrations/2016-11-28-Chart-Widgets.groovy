databaseChangeLog = {

    changeSet(author: "gologuzov (generated)", id: "1479941842862-1") {
        createTable(tableName: "DASHBOARD") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DASHBOARDPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PVUSER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "gologuzov (generated)", id: "1479941842862-2") {
        createTable(tableName: "dashboard_rconfig") {
            column(name: "dashboard_widgets_id", type: "number(19,0)")

            column(name: "report_configuration_id", type: "number(19,0)")

            column(name: "widgets_idx", type: "number(10,0)")
        }
    }

    changeSet(author: "gologuzov (generated)", id: "1481213808263-1") {
        createTable(tableName: "dashboard_rwidget") {
            column(name: "dashboard_widgets_id", type: "number(19,0)")

            column(name: "report_widget_id", type: "number(19,0)")

            column(name: "widgets_idx", type: "number(10,0)")
        }
    }

    changeSet(author: "gologuzov (generated)", id: "1481213808263-2") {
        createTable(tableName: "RWIDGET") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RWIDGETPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "auto_position", type: "number(1,0)")

            column(name: "height", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "report_configuration_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "width", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "x", type: "number(10,0)") {
                constraints(nullable: "false")
            }

            column(name: "y", type: "number(10,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "gologuzov (generated)", id: "1481213808263-34") {
        dropTable(tableName: "DASHBOARD_RCONFIG")
    }

    changeSet(author: "gologuzov (generated)", id: "1481213808263-26") {
        addForeignKeyConstraint(baseColumnNames: "report_widget_id", baseTableName: "dashboard_rwidget", constraintName: "FKC8AE7A8BE50BFE21", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RWIDGET", referencesUniqueColumn: "false")
    }

    changeSet(author: "gologuzov (generated)", id: "1481213808263-28") {
        addForeignKeyConstraint(baseColumnNames: "report_configuration_id", baseTableName: "RWIDGET", constraintName: "FK8A5949163D982133", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
    }
}
