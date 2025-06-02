databaseChangeLog = {
	changeSet(author: "gautammalhotra (generated)", id: "1461327327481-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'INCL_MEDICAL_CONFIRM_CASES')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update EX_RCONFIG set INCL_MEDICAL_CONFIRM_CASES = 0;")
		addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "INCL_MEDICAL_CONFIRM_CASES")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1461327327481-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RCONFIG', columnName: 'INCL_MEDICAL_CONFIRM_CASES')
			}
		}
		addColumn(tableName: "RCONFIG") {
			column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update RCONFIG set INCL_MEDICAL_CONFIRM_CASES = 0;")
		addNotNullConstraint(tableName: "RCONFIG", columnName: "INCL_MEDICAL_CONFIRM_CASES")
	}
}
