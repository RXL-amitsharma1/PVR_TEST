databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1490178014337-1") {
		createTable(tableName: "EX_RCONFIG_ACTION_ITEMS") {
			column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ACTION_ITEM_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1490178014337-2") {
		addForeignKeyConstraint(baseColumnNames: "ACTION_ITEM_ID", baseTableName: "EX_RCONFIG_ACTION_ITEMS", constraintName: "FKCF624CAEC6458485", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1490178014337-3") {
		addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "EX_RCONFIG_ACTION_ITEMS", constraintName: "FKCF624CAEB58176EC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1490178014337-4") {
		sql("insert into EX_RCONFIG_ACTION_ITEMS (EX_RCONFIG_ID, ACTION_ITEM_ID) select ID, ACTION_ITEM_ID from EX_RCONFIG WHERE ACTION_ITEM_ID IS NOT NULL;")
	}

	changeSet(author: "gologuzov (generated)", id: "1490178014337-5") {
		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'EX_RCONFIG', columnName: 'ACTION_ITEM_ID')
		}
		dropColumn(tableName: "EX_RCONFIG", columnName: "ACTION_ITEM_ID")
	}
}
