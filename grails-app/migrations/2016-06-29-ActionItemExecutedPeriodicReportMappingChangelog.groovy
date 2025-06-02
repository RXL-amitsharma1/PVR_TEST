databaseChangeLog = {

	changeSet(author: "gautammalhotra (generated)", id: "1467203314984-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'ACTION_ITEM_ID')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "ACTION_ITEM_ID", type: "number(19,0)")  {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1467203314984-16") {
		dropForeignKeyConstraint(baseTableName: "ACTION_ITEM", constraintName: "FKE077AB7C7A80BB7D")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1467203314984-19") {
		dropColumn(columnName: "EX_PERIODIC_REPORT_CONF_ID", tableName: "ACTION_ITEM")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1467203314984-18") {
		addForeignKeyConstraint(baseColumnNames: "ACTION_ITEM_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE88C6458485", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
	}
}