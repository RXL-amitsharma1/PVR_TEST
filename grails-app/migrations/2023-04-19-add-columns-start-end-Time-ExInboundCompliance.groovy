databaseChangeLog = {

    changeSet(author: "riya", id: "202304201441-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_INBOUND_COMPLIANCE", columnName: "START_TIME")
            }
        }
        addColumn(tableName: "EX_INBOUND_COMPLIANCE") {
            column(name: "START_TIME", type: "number(19,0)")
        }
        sql("update EX_INBOUND_COMPLIANCE set START_TIME=null");
    }

    changeSet(author: "riya", id: "202304201501-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_INBOUND_COMPLIANCE", columnName: "END_TIME")
            }
        }
        addColumn(tableName: "EX_INBOUND_COMPLIANCE") {
            column(name: "END_TIME", type: "number(19,0)")
        }
        sql("update EX_INBOUND_COMPLIANCE set END_TIME=null");
    }

    changeSet(author: "riya", id: "202304211551-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_INBOUND_COMPLIANCE", columnName: "LAST_RUN_DATE")
            }
        }
        addColumn(tableName: "EX_INBOUND_COMPLIANCE") {
            column(name: "LAST_RUN_DATE", type: "timestamp")
        }
        sql("update EX_INBOUND_COMPLIANCE set LAST_RUN_DATE=null");
    }

    changeSet(author: "riya", id: "202304211727-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_INBOUND_COMPLIANCE", columnName: "ERROR_DETAILS")
            }
        }
        addColumn(tableName: "EX_INBOUND_COMPLIANCE") {
            column(name: "ERROR_DETAILS", type: "clob")
        }
        sql("update EX_INBOUND_COMPLIANCE set ERROR_DETAILS=null");
    }

}


