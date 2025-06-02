databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1495547589928-1") {
		addColumn(tableName: "EMAIL_CONFIGURATION") {
			column(name: "exclude_appendix", type: "number(1,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1495547589928-2") {
		addColumn(tableName: "EMAIL_CONFIGURATION") {
			column(name: "exclude_comments", type: "number(1,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1495547589928-3") {
		addColumn(tableName: "EMAIL_CONFIGURATION") {
			column(name: "exclude_criteria_sheet", type: "number(1,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1495547589928-4") {
		addColumn(tableName: "EMAIL_CONFIGURATION") {
			column(name: "page_orientation", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1495547589928-5") {
		addColumn(tableName: "EMAIL_CONFIGURATION") {
			column(name: "paper_size", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1495547589928-6") {
		addColumn(tableName: "EMAIL_CONFIGURATION") {
			column(name: "sensitivity_label", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1495547589928-7") {
		addColumn(tableName: "EMAIL_CONFIGURATION") {
			column(name: "show_company_logo", type: "number(1,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1495547589928-8") {
		addColumn(tableName: "EMAIL_CONFIGURATION") {
			column(name: "show_page_numbering", type: "number(1,0)")
		}
	}
}
