databaseChangeLog = {

    changeSet(author: "meenal(generated)", id: "202310201221-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ICSR_TEMPLT_QUERY', columnName: 'ICSR_MSG_TYPE')
            }
        }
        addColumn(tableName: "ICSR_TEMPLT_QUERY") {
            column(name: "ICSR_MSG_TYPE", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE ICSR_TEMPLT_QUERY set ICSR_MSG_TYPE = 1 where MSG_TYPE = 'ICHICSR'")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "MSG_TYPE", tableName: "ICSR_TEMPLT_QUERY")
        addNotNullConstraint(tableName: "ICSR_TEMPLT_QUERY", columnName: "ICSR_MSG_TYPE", columnDataType: "number(19,0)")
    }

    changeSet(author: "meenal(generated)", id: "202310201221-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_ICSR_TEMPLT_QUERY', columnName: 'ICSR_MSG_TYPE')
            }
        }
        addColumn(tableName: "EX_ICSR_TEMPLT_QUERY") {
            column(name: "ICSR_MSG_TYPE", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE EX_ICSR_TEMPLT_QUERY set ICSR_MSG_TYPE = 1 where MSG_TYPE = 'ICHICSR'")
        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "MSG_TYPE", tableName: "EX_ICSR_TEMPLT_QUERY")
        addNotNullConstraint(tableName: "EX_ICSR_TEMPLT_QUERY", columnName: "ICSR_MSG_TYPE", columnDataType: "number(19,0)")
    }

    changeSet(author: "meenal(generated)", id: "202310201221-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_ICSR_TEMPLT_QUERY', columnName: 'ICSR_MSG_TYPE_NAME')
            }
        }
        addColumn(tableName: "EX_ICSR_TEMPLT_QUERY") {
            column(name: "ICSR_MSG_TYPE_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
        sql("UPDATE EX_ICSR_TEMPLT_QUERY set ICSR_MSG_TYPE_NAME = 'ichicsr' where MSG_TYPE = 'ICHICSR'")
        addNotNullConstraint(tableName: "EX_ICSR_TEMPLT_QUERY", columnName: "ICSR_MSG_TYPE_NAME", columnDataType: "varchar2(255 char)")
    }

}

