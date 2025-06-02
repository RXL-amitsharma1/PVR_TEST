databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1441317749409-1") {
		addColumn(tableName: "SUPER_QUERY") {
			column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
			}
		}
        grailsChange {
            change {
                sql.execute("UPDATE SUPER_QUERY SET MODIFIED_BY = 'Application'")
                confirm "Successfully set default value for SUPER_QUERY."
            }
        }
        addNotNullConstraint(tableName: "SUPER_QUERY", columnName: "MODIFIED_BY", columnDataType: "varchar2(20 char)")
	}

	changeSet(author: "Sherry (generated)", id: "1441317749409-2") {
		dropForeignKeyConstraint(baseTableName: "SUPER_QUERY", constraintName: "FK5014CAC4E3EFCE4E")
        dropColumn(columnName: "CREATED_BY", tableName: "SUPER_QUERY")
        addColumn(tableName: "SUPER_QUERY") {
            column(name: "CREATED_BY", type: "varchar2(20 char)") {
            }
        }
        grailsChange {
            change {
                sql.execute("UPDATE SUPER_QUERY SET CREATED_BY = 'Application'")
                confirm "Successfully set default value for SUPER_QUERY."
            }
        }
        addNotNullConstraint(tableName: "SUPER_QUERY", columnName: "CREATED_BY", columnDataType: "varchar2(20 char)")
	}
}
