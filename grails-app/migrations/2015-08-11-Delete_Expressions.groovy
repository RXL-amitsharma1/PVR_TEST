databaseChangeLog = {

	changeSet(author: "Pomi (generated)", id: "1439332369492-1") {
		dropForeignKeyConstraint(baseTableName: "EX_EXPRESSION", constraintName: "FKE1256DA433249131")
	}

	changeSet(author: "Pomi (generated)", id: "1439332369492-2") {
		dropForeignKeyConstraint(baseTableName: "EXPRESSION", constraintName: "FKB1E57E98BF61BBB3")
	}

	changeSet(author: "Pomi (generated)", id: "1439332369492-3") {
		dropTable(tableName: "EX_EXPRESSION")
	}

	changeSet(author: "Pomi (generated)", id: "1439332369492-4") {
		dropTable(tableName: "EXPRESSION")
	}
}
