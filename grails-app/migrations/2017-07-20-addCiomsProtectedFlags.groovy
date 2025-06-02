databaseChangeLog = {

	changeSet(author: "prashantsahi (generated)", id: "1500558380861-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_TEMPLT_QUERY', columnName: 'BLIND_PROTECTED')
			}
		}
		addColumn(tableName: "EX_TEMPLT_QUERY") {
			column(name: "BLIND_PROTECTED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update EX_TEMPLT_QUERY set BLIND_PROTECTED = 0;")
		addNotNullConstraint(tableName: "EX_TEMPLT_QUERY", columnName: "BLIND_PROTECTED")
	}

	changeSet(author: "prashantsahi (generated)", id: "1500558380861-3") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_TEMPLT_QUERY', columnName: 'PRIVACY_PROTECTED')
			}
		}
		addColumn(tableName: "EX_TEMPLT_QUERY") {
			column(name: "PRIVACY_PROTECTED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update EX_TEMPLT_QUERY set PRIVACY_PROTECTED = 0;")
		addNotNullConstraint(tableName: "EX_TEMPLT_QUERY", columnName: "PRIVACY_PROTECTED")
	}

	changeSet(author: "prashantsahi (generated)", id: "1500558380861-4") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'TEMPLT_QUERY', columnName: 'BLIND_PROTECTED')
			}
		}
		addColumn(tableName: "TEMPLT_QUERY") {
			column(name: "BLIND_PROTECTED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update TEMPLT_QUERY set BLIND_PROTECTED = 0;")
		addNotNullConstraint(tableName: "TEMPLT_QUERY", columnName: "BLIND_PROTECTED")
	}

	changeSet(author: "prashantsahi (generated)", id: "1500558380861-5") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'TEMPLT_QUERY', columnName: 'PRIVACY_PROTECTED')
			}
		}
		addColumn(tableName: "TEMPLT_QUERY") {
			column(name: "PRIVACY_PROTECTED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update TEMPLT_QUERY set PRIVACY_PROTECTED = 0;")
		addNotNullConstraint(tableName: "TEMPLT_QUERY", columnName: "PRIVACY_PROTECTED")
	}

}