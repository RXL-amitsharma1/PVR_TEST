databaseChangeLog = {

    changeSet(author: "riya", id: "202305031817-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "INBOUND_COMPLIANCE", columnName: "LAST_RUN_DATE")
            }
        }
        addColumn(tableName: "INBOUND_COMPLIANCE") {
            column(name: "LAST_RUN_DATE", type: "timestamp")
        }
        sql("update INBOUND_COMPLIANCE set LAST_RUN_DATE=null");
    }

    changeSet(author: "riya", id: "202305041236-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_INBOUND_COMPLIANCE", columnName: "MESSAGE")
            }
        }
        addColumn(tableName: "EX_INBOUND_COMPLIANCE") {
            column(name: "MESSAGE", type: "clob")
        }
        sql("update EX_INBOUND_COMPLIANCE set MESSAGE=null");
    }

    changeSet(author: "riya", id: "202305041447-3") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_INBOUND_COMPLIANCE", columnName: "LAST_RUN_DATE")
            }
        }
        dropColumn(columnName: "LAST_RUN_DATE", tableName: "EX_INBOUND_COMPLIANCE")
    }

}

