databaseChangeLog = {

	changeSet(author: "pomipark", id:'remove old notifications') {
		sql("DELETE FROM notification;")
	}

	changeSet(author: "Pomi (generated)", id: "1441048748289-1") {
		addColumn(tableName: "NOTIFICATION") {
			column(name: "EC_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}
}
