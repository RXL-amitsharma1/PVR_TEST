databaseChangeLog = {

	changeSet(author: "prakriti (generated)", id: "1452117310920-1") {
		addColumn(tableName: "ARGUS_COLUMN_MASTER") {
			column(name: "IS_DELETED", type: "number(1,0)")
		}
            grailsChange {
                change {
                    sql.execute("UPDATE ARGUS_COLUMN_MASTER SET IS_DELETED = '0'")
                    confirm "Successfully set default value for IS_DELETED."
                }
            }
            addNotNullConstraint(tableName: "ARGUS_COLUMN_MASTER", columnName: "IS_DELETED", columnDataType: "number(1,0)")
	}

	changeSet(author: "prakriti (generated)", id: "1452117310920-2") {
		addColumn(tableName: "ARGUS_TABLE_MASTER") {
			column(name: "IS_DELETED", type: "number(1,0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE ARGUS_TABLE_MASTER SET IS_DELETED = '0'")
                confirm "Successfully set default value for IS_DELETED."
            }
        }
        addNotNullConstraint(tableName: "ARGUS_TABLE_MASTER", columnName: "IS_DELETED", columnDataType: "number(1,0)")
	}

	changeSet(author: "prakriti (generated)", id: "1452117310920-3") {
		addColumn(tableName: "CASE_COLUMN_JOIN_MAPPING") {
			column(name: "IS_DELETED", type: "number(1,0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE CASE_COLUMN_JOIN_MAPPING SET IS_DELETED = '0'")
                confirm "Successfully set default value for IS_DELETED."
            }
        }
        addNotNullConstraint(tableName: "CASE_COLUMN_JOIN_MAPPING", columnName: "IS_DELETED", columnDataType: "number(1,0)")
	}

	changeSet(author: "prakriti (generated)", id: "1452117310920-4") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "IS_DELETED", type: "number(1,0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE RPT_FIELD SET IS_DELETED = '0'")
                confirm "Successfully set default value for IS_DELETED."
            }
        }
        addNotNullConstraint(tableName: "RPT_FIELD", columnName: "IS_DELETED", columnDataType: "number(1,0)")
	}


    changeSet(author: "prakriti (generated)", id: "ReportField Group name column added") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "RPT_FIELD_GRPNAME", type: "varchar2(255 char)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE rpt_field a  SET RPT_FIELD_GRPNAME = (SELECT NAME FROM rpt_field_group b WHERE a.rpt_field_group_id = b.ID)")
                confirm "Successfully set default value for rpt_field_group_name."
            }
        }
        addNotNullConstraint(tableName: "RPT_FIELD", columnName: "RPT_FIELD_GRPNAME", columnDataType: "varchar2(255 char)")
    }

	changeSet(author: "prakriti (generated)", id: "1452117310920-5") {
		addColumn(tableName: "RPT_FIELD_GROUP") {
			column(name: "IS_DELETED", type: "number(1,0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE RPT_FIELD_GROUP SET IS_DELETED = '0'")
                confirm "Successfully set default value for IS_DELETED."
            }
        }
        addNotNullConstraint(tableName: "RPT_FIELD_GROUP", columnName: "IS_DELETED", columnDataType: "number(1,0)")
	}

    changeSet(author: "prakriti (generated)", id: "1452117310920-13") {
        dropForeignKeyConstraint(baseTableName: "RPT_FIELD", constraintName: "FKF5EE113191B256C0")
    }

    changeSet(author: "prakriti (generated)", id: "1452117310920-12") {
        dropPrimaryKey(constraintName: "RPT_FIELD_GROPK", tableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "prakriti (generated)", id: "1452117310920-11") {
        addPrimaryKey(columnNames: "NAME", constraintName: "RPT_FIELD_GROPK", tableName: "RPT_FIELD_GROUP")
    }

	changeSet(author: "prakriti (generated)", id: "1452117310920-16") {
		dropColumn(columnName: "ID", tableName: "RPT_FIELD_GROUP")
	}

    changeSet(author: "prakriti (generated)", id: "1452117310920-10") {
        dropColumn(columnName: "RPT_FIELD_GROUP_ID", tableName: "RPT_FIELD")
    }

	changeSet(author: "prakriti (generated)", id: "1452117310920-14") {
		addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_GRPNAME", baseTableName: "RPT_FIELD", constraintName: "FKF5EE113191B256C0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "NAME", referencedTableName: "RPT_FIELD_GROUP", referencesUniqueColumn: "false")
	}
}
