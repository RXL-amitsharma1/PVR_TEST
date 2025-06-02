databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1490545911695-1") {
		addColumn(tableName: "WORKFLOW_RULE") {
			column(name: "DEFAULT_REPORT_ACTION", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1490545911695-2") {
		addColumn(tableName: "WORKFLOW_JUSTIFICATION") {
			column(name: "WORKFLOW_RULE_ID", type: "number(19,0)")
		}
	}
}
