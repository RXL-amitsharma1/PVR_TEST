databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1503053004062-1") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "CUSTOM_RPT_FIELD")
            }
        }
        createTable(tableName: "CUSTOM_RPT_FIELD") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CUSTOM_RPT_FIPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "varchar2(2000 char)")

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "DEFAULT_EXPRESSION", type: "varchar2(2000 char)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FIELD_GRPNAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FIELD_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_CLL_SELECTABLE", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_DTCOL_SELECTABLE", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_DTROW_SELECTABLE", type: "number(1,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "1503053004062-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_FIELD_INFO', columnName: 'CUSTOM_FIELD_ID')
            }
        }
        addColumn(tableName: "RPT_FIELD_INFO") {
            column(name: "CUSTOM_FIELD_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1503053004062-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'CUSTOM_RPT_FIELD', foreignKeyName: 'FK22B8B1E39E3EB0F9')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_GRPNAME", baseTableName: "CUSTOM_RPT_FIELD", constraintName: "FK22B8B1E39E3EB0F9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "NAME", referencedTableName: "RPT_FIELD_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1503053004062-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'CUSTOM_RPT_FIELD', foreignKeyName: 'FK22B8B1E333249131')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_ID", baseTableName: "CUSTOM_RPT_FIELD", constraintName: "FK22B8B1E333249131", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1503053004062-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyTableName: 'RPT_FIELD_INFO', foreignKeyName: 'FKEA2F1AFC4A59A465')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "CUSTOM_FIELD_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FKEA2F1AFC4A59A465", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CUSTOM_RPT_FIELD", referencesUniqueColumn: "false")
    }

}
