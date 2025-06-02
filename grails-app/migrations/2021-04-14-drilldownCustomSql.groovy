databaseChangeLog = {

    changeSet(author: "sergey (generated)", id: "20210414104301-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "SQL_TEMPLT", columnName: "DRILL_DOWN_ID")
            }
        }
        addColumn(tableName: "SQL_TEMPLT") {
            column(name: "DRILL_DOWN_ID", type: "number(19,0)")
            column(name: "DRILL_DOWN_FIELD", type: "varchar2(4000 char)")
            column(name: "DRILL_DOWN_FILER", type: "varchar2(4000 char)")
        }
    }

    changeSet(author: "sergey (generated)", id: "20210414104301-2") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "NONCASE_SQL_TEMPLT", columnName: "DRILL_DOWN_ID")
            }
        }
        addColumn(tableName: "NONCASE_SQL_TEMPLT") {
            column(name: "DRILL_DOWN_ID", type: "number(19,0)")
            column(name: "DRILL_DOWN_FIELD", type: "varchar2(4000 char)")
            column(name: "DRILL_DOWN_FILER", type: "varchar2(4000 char)")
        }
    }

}