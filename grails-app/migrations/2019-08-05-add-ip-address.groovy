databaseChangeLog = {

	changeSet(author: "sachinverma (generated)", id: "1000005082019-1") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "AUDIT_LOG", columnName: "USER_IP_ADDRESS")
			}
		}
		addColumn(tableName: "AUDIT_LOG") {
			column(name: "USER_IP_ADDRESS", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "sachinverma (generated)", id: "1000005082019-2") {
		preConditions(onFail: "MARK_RAN") {
			columnExists(tableName: "AUDIT_LOG", columnName: "USER_IP_ADDRESS")
		}
		sql("UPDATE AUDIT_LOG SET USER_IP_ADDRESS = 'Not Recorded' where CATEGORY in ('LOGIN_FAILURE','LOGIN_SUCCESSFUL')")
	}
}