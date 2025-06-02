databaseChangeLog = {

	changeSet(author: "prashantsahi (generated)", id: "1475577323105-1") {
		createTable(tableName: "CLL_TEMPLATES_QRS_EXP_VALUES") {
			column(name: "CLL_TEMPLATE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_EXP_VALUE_ID", type: "number(19,0)")

			column(name: "query_expression_values_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "prashantsahi (generated)", id: "1475577323105-2") {
		addColumn(tableName: "CLL_TEMPLT") {
			column(name: "QUERY", type: "clob")
		}
	}

	changeSet(author: "prashantsahi (generated)", id: "1475577323105-26") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_EXP_VALUE_ID", baseTableName: "CLL_TEMPLATES_QRS_EXP_VALUES", constraintName: "FK89FF1B538744C51", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_EXP_VALUE", referencesUniqueColumn: "false")
	}

}
