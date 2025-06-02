databaseChangeLog = {

	changeSet(author: "Meenakshi (generated)", id: "1473061751455-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'WORKFLOW_RULE', columnName: 'CONFIGURATION_TYPE_ENUM')
			}
		}
		addColumn(tableName: "WORKFLOW_RULE") {
			column(name: "CONFIGURATION_TYPE_ENUM", type: "varchar2(255 char)") {
				constraints(nullable: "true")
			}
		}
		sql("update WORKFLOW_RULE set CONFIGURATION_TYPE_ENUM='PR_TYPE';")
		addNotNullConstraint(tableName: "WORKFLOW_RULE", columnName: "CONFIGURATION_TYPE_ENUM")
	}

	changeSet(author: "Meenakshi (generated)", id: "1473061751455-2"){
		sql("update WORKFLOW_RULE set CONFIGURATION_TYPE_ENUM='PERIODIC_REPORT' where CONFIGURATION_TYPE_ENUM='PR_TYPE';")
	}
}
