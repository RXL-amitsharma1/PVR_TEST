databaseChangeLog = {

	changeSet(author: "pomipark", id:'remove old notifications again') {
		sql("DELETE FROM notification;")
	}

	changeSet(author: "Pomi (generated)", id: "1441925527736-1") {
		addColumn(tableName: "NOTIFICATION") {
			column(name: "MSG_ARGS", type: "varchar2(255 char)")
		}
	}
}
