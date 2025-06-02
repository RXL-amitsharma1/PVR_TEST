databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1457930939659-1") {
		addColumn(tableName: "ACTION_ITEM") {
			column(name: "EX_PERIODIC_REPORT_CONF_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1457930939659-11") {
		createIndex(indexName: "periodic_id_1457930936253", tableName: "ACTION_ITEM", unique: "true") {
			column(name: "ex_periodic_report_conf_id")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1457930939659-10") {
		addForeignKeyConstraint(baseColumnNames: "ex_periodic_report_conf_id", baseTableName: "ACTION_ITEM", constraintName: "FKE077AB7C7A80BB7D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}
}
