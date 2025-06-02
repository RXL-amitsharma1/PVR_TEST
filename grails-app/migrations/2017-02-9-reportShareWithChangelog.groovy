databaseChangeLog = {

	changeSet(author: "prashantsahi (generated)", id: "1486633366665-1") {
		createTable(tableName: "DELIVERIES_SHARED_WITH_GRPS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_GROUP_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_GROUP_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "prashantsahi (generated)", id: "1486633366665-2") {
		createTable(tableName: "EX_DELIVERIES_SHARED_WITH_GRPS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_GROUP_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_GROUP_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "prashantsahi (generated)", id: "1486633366665-45") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_GROUP_ID", baseTableName: "DELIVERIES_SHARED_WITH_GRPS", constraintName: "FK81CA64605D0D08F9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
	}

    changeSet(author: "prashantsahi (generated)", id: "1486633366665-46") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_GROUP_ID", baseTableName: "EX_DELIVERIES_SHARED_WITH_GRPS", constraintName: "FKA6E421D45D0D08F9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1486633366665-80") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_PUBLIC')
                sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM RCONFIG WHERE IS_PUBLIC = 1 and rownum < 2;')
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM USER_GROUP")
            }
        }
        sql("insert into USER_GROUP(ID, NAME, VERSION, CREATED_BY, MODIFIED_BY, DATE_CREATED, LAST_UPDATED, IS_DELETED) values(1,'All Users', 0, 'Application', 'Application', SYSDATE, SYSDATE, 0 )")

    }

    changeSet(author: "prashantsahi (generated)", id: "1486633366665-81") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_PUBLIC')
                sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM RCONFIG WHERE IS_PUBLIC = 1 and rownum < 2;')
                sqlCheck(expectedResult: '1', "SELECT COUNT(1) FROM USER_GROUP WHERE NAME='All Users' AND ID=1;")
            }
        }
        sql("insert into DELIVERIES_SHARED_WITH_GRPS (SHARED_WITH_GROUP_ID,DELIVERY_ID,SHARED_WITH_GROUP_IDX) SELECT 1, delivery.id, ROWNUM - 1 FROM DELIVERY delivery inner join RCONFIG conf on conf.ID = delivery.REPORT_ID WHERE conf.IS_PUBLIC = 1;")

    }


    changeSet(author: "prashantsahi (generated)", id: "1486633366665-82") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'IS_PUBLIC')
                sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM EX_RCONFIG WHERE IS_PUBLIC = 1 and rownum < 2;')
                sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM USER_GROUP")
            }
        }
        sql("insert into USER_GROUP(ID, NAME, VERSION, CREATED_BY, MODIFIED_BY, DATE_CREATED, LAST_UPDATED, IS_DELETED) values(1,'All Users', 0, 'Application', 'Application', SYSDATE, SYSDATE, 0 )")
    }

    changeSet(author: "prashantsahi (generated)", id: "1486633366665-83") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'IS_PUBLIC')
                sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM EX_RCONFIG WHERE IS_PUBLIC = 1 and rownum < 2;')
                sqlCheck(expectedResult: '1', "SELECT COUNT(1) FROM USER_GROUP WHERE NAME='All Users' AND ID=1;")
            }
        }
        sql("insert into EX_DELIVERIES_SHARED_WITH_GRPS (SHARED_WITH_GROUP_ID,EX_DELIVERY_ID,SHARED_WITH_GROUP_IDX) SELECT 1, delivery.id, ROWNUM - 1 FROM EX_DELIVERY delivery inner join EX_RCONFIG conf on conf.ID = delivery.EXECUTED_CONFIGURATION_ID WHERE conf.IS_PUBLIC = 1;")

    }

    changeSet(author: "prashantsahi (generated)", id: "1486633366665-84") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_RCONFIG', columnName: 'IS_PUBLIC')
        }
        dropColumn(columnName: "IS_PUBLIC", tableName: "EX_RCONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1486633366665-85") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RCONFIG', columnName: 'IS_PUBLIC')
        }
        dropColumn(columnName: "IS_PUBLIC", tableName: "RCONFIG")
    }
}
