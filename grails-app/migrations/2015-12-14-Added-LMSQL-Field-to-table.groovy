databaseChangeLog = {

	changeSet(author: "prakriti (generated)", id: "1450139714030-1") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "lmsql", type: "varchar2(4000 char)")
		}
	}

    changeSet(author: "prakriti (generated)", id: "1450139714030-2") {
        sql("update rpt_field set list_domain_class = 'com.rxlogix.TableColumnSelectableList'")
        }
}
