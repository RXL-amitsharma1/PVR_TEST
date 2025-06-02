databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1452303598906-1") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "DIC_LEVEL", type: "number(10,0)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "Sherry (generated)", id: "1452303598906-2") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "DIC_TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "true")
			}
		}
	}
}
