databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1531234114127-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RCONFIG', columnName: 'CONFIG_TEMPLATE_ID')
			}
		}
		addColumn(tableName: "RCONFIG") {
			column(name: "CONFIG_TEMPLATE_ID", type: "number(19,0)")
		}
		addForeignKeyConstraint(baseColumnNames: "CONFIG_TEMPLATE_ID", baseTableName: "RCONFIG", constraintName: "FK_a6onafs75nl2fvaicj3tu6lp5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}
}
