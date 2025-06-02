databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1485511494687-1") {
        addColumn(tableName: "RPT_SUBMISSION") {
            column(name: "IS_PRIMARY", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1485511494687-2") {
        addColumn(tableName: "RPT_SUBMISSION") {
            column(name: "REPORTING_DESTINATION", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1485511494687-3") {
        sql("insert into RPT_SUBMISSION (ID, VERSION,COMMENT_DATA,CREATED_BY, DATE_CREATED, EX_RCONFIG_ID, LAST_UPDATED, MODIFIED_BY, RPT_SUBMISSION_STATUS, SUBMISSION_DATE,IS_PRIMARY,REPORTING_DESTINATION) select HIBERNATE_SEQUENCE.nextval, rpt.VERSION,rpt.COMMENT_DATA,rpt.CREATED_BY, rpt.DATE_CREATED, rpt.EX_RCONFIG_ID, rpt.LAST_UPDATED, rpt.MODIFIED_BY, rpt.RPT_SUBMISSION_STATUS, rpt.SUBMISSION_DATE,0,dest.REPORTING_DESTINATION from RPT_SUB_RPT_DESTINATIONS dest left join RPT_SUBMISSION rpt on rpt.ID = dest.RPT_SUBMISSION_ID ")
    }

    changeSet(author: "prakriti (generated)", id: "1452117310920-5") {
        dropForeignKeyConstraint(baseTableName: "RPT_SUB_RPT_DESTINATIONS", constraintName: "FK47CAB7981E4B1EE3")
    }

    changeSet(author: "sachinverma (generated)", id: "1485511494687-6") {
        sql("delete from RPT_SUBMISSION where IS_PRIMARY is null")
    }

    changeSet(author: "sachinverma (generated)", id: "1485511494687-7") {
        sql("update RPT_SUBMISSION set IS_PRIMARY = 1 where REPORTING_DESTINATION = PRIMARY_DESTINATION and PRIMARY_DESTINATION is not null")
    }

	changeSet(author: "sachinverma (generated)", id: "1485511494687-8") {
		dropColumn(columnName: "PRIMARY_DESTINATION", tableName: "RPT_SUBMISSION")
	}

	changeSet(author: "sachinverma (generated)", id: "1485511494687-9") {
		dropTable(tableName: "RPT_SUB_RPT_DESTINATIONS")
	}

}
