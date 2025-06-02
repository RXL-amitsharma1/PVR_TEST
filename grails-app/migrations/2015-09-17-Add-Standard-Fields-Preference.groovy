databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1442256774604-1") {
		addColumn(tableName: "PREFERENCE") {
			column(name: "CREATED_BY", type: "varchar2(20 char)") {
			}
		}
        grailsChange {
            change {
                sql.execute("UPDATE PREFERENCE SET CREATED_BY = 'Application'")
                confirm "Successfully set default value for PREFERENCE."
            }
        }
        addNotNullConstraint(tableName: "PREFERENCE", columnName: "CREATED_BY", columnDataType: "varchar2(20 char)")
	}

	changeSet(author: "Sherry (generated)", id: "1442256774604-2") {
		addColumn(tableName: "PREFERENCE") {
			column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
			}
		}
        grailsChange {
            change {
                sql.execute("UPDATE PREFERENCE SET MODIFIED_BY = 'Application'")
                confirm "Successfully set default value for PREFERENCE."
            }
        }
        addNotNullConstraint(tableName: "PREFERENCE", columnName: "MODIFIED_BY", columnDataType: "varchar2(20 char)")
	}

	changeSet(author: "Sherry (generated)", id: "1442256774604-3") {
//		modifyDataType(columnName: "NEW", newDataType: "clob", tableName: "AUDIT_LOG_FIELD_CHANGE")

        grailsChange {
            change {
                sql.execute("ALTER TABLE AUDIT_LOG_FIELD_CHANGE ADD tmp_name CLOB")
                sql.execute("UPDATE AUDIT_LOG_FIELD_CHANGE SET tmp_name=NEW")
                sql.execute("ALTER TABLE AUDIT_LOG_FIELD_CHANGE DROP COLUMN NEW")
                sql.execute("ALTER TABLE AUDIT_LOG_FIELD_CHANGE RENAME COLUMN tmp_name to NEW")
                confirm "Successfully modified data type for PREFERENCE."
            }
        }
	}

	changeSet(author: "Sherry (generated)", id: "1442256774604-4") {
//		modifyDataType(columnName: "ORIGINAL", newDataType: "clob", tableName: "AUDIT_LOG_FIELD_CHANGE")

        grailsChange {
            change {
                sql.execute("ALTER TABLE AUDIT_LOG_FIELD_CHANGE ADD tmp_name CLOB")
                sql.execute("UPDATE AUDIT_LOG_FIELD_CHANGE SET tmp_name=ORIGINAL")
                sql.execute("ALTER TABLE AUDIT_LOG_FIELD_CHANGE DROP COLUMN ORIGINAL")
                sql.execute("ALTER TABLE AUDIT_LOG_FIELD_CHANGE RENAME COLUMN tmp_name to ORIGINAL")
                confirm "Successfully modified data type for PREFERENCE."
            }
        }
	}
}
