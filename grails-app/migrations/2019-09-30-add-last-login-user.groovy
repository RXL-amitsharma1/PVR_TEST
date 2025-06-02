databaseChangeLog = {

	changeSet(author: "sachinverma (generated)", id: "2019093015000-1") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "PVUSER", columnName: "LAST_LOGIN")
			}
		}
		addColumn(tableName: "PVUSER") {
			column(name: "LAST_LOGIN", type: "timestamp")
		}
	}

	changeSet(author: "sachinverma (generated)", id: "2019093015000-2") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "PVUSER", columnName: "LAST_TO_LAST_LOGIN")
			}
		}
		addColumn(tableName: "PVUSER") {
			column(name: "LAST_TO_LAST_LOGIN", type: "timestamp")
		}
	}

    changeSet(author: "Amit Kumar3", id: "202403190160-1") {
        sql("MERGE INTO PVUSER T1 USING (SELECT USERNAME,DATE_CREATED from (SELECT USERNAME,DATE_CREATED,ROW_NUMBER() OVER (PARTITION BY USERNAME ORDER BY DATE_CREATED DESC) AS RN FROM AUDIT_LOG where category='LOGIN_SUCCESS') WHERE RN = 1 )T2 ON (T1.USERNAME = T2.USERNAME) WHEN MATCHED THEN UPDATE SET T1.LAST_LOGIN = T2.DATE_CREATED WHERE T1.LAST_LOGIN is null")
    }
}