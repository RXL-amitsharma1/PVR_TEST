databaseChangeLog = {

	changeSet(author: "prashantsahi (generated)", id: "1480931914199-1") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RPT_TEMPLT', columnName: 'QUALITY_CHECKED')
			}
		}

		addColumn(tableName: "RPT_TEMPLT") {
			column(name: "QUALITY_CHECKED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}

		sql("update RPT_TEMPLT set QUALITY_CHECKED = 0;")
		addNotNullConstraint(tableName: "RPT_TEMPLT", columnName: "QUALITY_CHECKED")
	}

	changeSet(author: "prashantsahi (generated)", id: "1480931914199-2") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SUPER_QUERY', columnName: 'QUALITY_CHECKED')
			}
		}

		addColumn(tableName: "SUPER_QUERY") {
			column(name: "QUALITY_CHECKED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}

		sql("update SUPER_QUERY set QUALITY_CHECKED = 0;")
		addNotNullConstraint(tableName: "SUPER_QUERY", columnName: "QUALITY_CHECKED")
	}

}
