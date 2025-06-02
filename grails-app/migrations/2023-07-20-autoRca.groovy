databaseChangeLog = {

    changeSet(author: "sergey", id: "202307201813-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'PVQ_TYPE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "PVQ_TYPE", type: "VARCHAR2(255)")
        }
    }

    changeSet(author: "sergey", id: "202307201813-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'PVQ_TYPE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "PVQ_TYPE", type: "VARCHAR2(255)")
        }
    }

    changeSet(author: "sergey", id: "202307201816-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'TEMPLT_QUERY', columnName: 'ISSUE_TYPE')
            }
        }
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "ISSUE_TYPE", type: "number(19,0)")
            column(name: "RESPONSIBLE_PARTY", type: "number(19,0)")
            column(name: "ROOT_CAUSE", type: "number(19,0)")
            column(name: "ASSIGNED_TO_USER_ID", type: "number(19,0)")
            column(name: "ASSIGNED_TO_GROUP_ID", type: "number(19,0)")
            column(name: "PRIORITY", type: "VARCHAR2(255)")
            column(name: "SUMMARY", type: "VARCHAR2(4000)")
            column(name: "ACTIONS", type: "VARCHAR2(4000)")
            column(name: "INVESTIGATION", type: "VARCHAR2(4000)")
            column(name: "SUMMARY_SQL", type: "VARCHAR2(4000)")
            column(name: "ACTIONS_SQL", type: "VARCHAR2(4000)")
            column(name: "INVESTIGATION_SQL", type: "VARCHAR2(4000)")
        }
    }

    changeSet(author: "sergey", id: "202307201816-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_TEMPLT_QUERY', columnName: 'ISSUE_TYPE')
            }
        }
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "ISSUE_TYPE", type: "number(19,0)")
            column(name: "RESPONSIBLE_PARTY", type: "number(19,0)")
            column(name: "ROOT_CAUSE", type: "number(19,0)")
            column(name: "ASSIGNED_TO_USER_ID", type: "number(19,0)")
            column(name: "ASSIGNED_TO_GROUP_ID", type: "number(19,0)")
            column(name: "PRIORITY", type: "VARCHAR2(255)")
            column(name: "SUMMARY", type: "VARCHAR2(4000)")
            column(name: "ACTIONS", type: "VARCHAR2(4000)")
            column(name: "INVESTIGATION", type: "VARCHAR2(4000)")
            column(name: "SUMMARY_SQL", type: "VARCHAR2(4000)")
            column(name: "ACTIONS_SQL", type: "VARCHAR2(4000)")
            column(name: "INVESTIGATION_SQL", type: "VARCHAR2(4000)")
        }
    }

    changeSet(author: "sergey", id: "202307201816-10") {
        modifyDataType(tableName: "TEMPLT_QUERY", columnName: "SUMMARY", newDataType: "VARCHAR2(8000)")
        modifyDataType(tableName: "TEMPLT_QUERY", columnName: "ACTIONS", newDataType: "VARCHAR2(8000)")
        modifyDataType(tableName: "TEMPLT_QUERY", columnName: "INVESTIGATION", newDataType: "VARCHAR2(8000)")
        modifyDataType(tableName: "TEMPLT_QUERY", columnName: "SUMMARY_SQL", newDataType: "VARCHAR2(32000)")
        modifyDataType(tableName: "TEMPLT_QUERY", columnName: "ACTIONS_SQL", newDataType: "VARCHAR2(32000)")
        modifyDataType(tableName: "TEMPLT_QUERY", columnName: "INVESTIGATION_SQL", newDataType: "VARCHAR2(32000)")
        modifyDataType(tableName: "EX_TEMPLT_QUERY", columnName: "SUMMARY", newDataType: "VARCHAR2(8000)")
        modifyDataType(tableName: "EX_TEMPLT_QUERY", columnName: "ACTIONS", newDataType: "VARCHAR2(8000)")
        modifyDataType(tableName: "EX_TEMPLT_QUERY", columnName: "INVESTIGATION", newDataType: "VARCHAR2(8000)")
        modifyDataType(tableName: "EX_TEMPLT_QUERY", columnName: "SUMMARY_SQL", newDataType: "VARCHAR2(32000)")
        modifyDataType(tableName: "EX_TEMPLT_QUERY", columnName: "ACTIONS_SQL", newDataType: "VARCHAR2(32000)")
        modifyDataType(tableName: "EX_TEMPLT_QUERY", columnName: "INVESTIGATION_SQL", newDataType: "VARCHAR2(32000)")
    }
}