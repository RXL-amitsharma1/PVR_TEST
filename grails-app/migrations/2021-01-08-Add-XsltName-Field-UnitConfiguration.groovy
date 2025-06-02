databaseChangeLog = {
    changeSet(author: "Sachin", id: "20210108024641-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNIT_CONFIGURATION', columnName: 'XSLT_NAME')
            }
        }

        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "XSLT_NAME", type: "varchar2(255 char)")
        }

        sql("update UNIT_CONFIGURATION set XSLT_NAME = UNIT_REGISTERED_ID where UNIT_TYPE !='SENDER';")

    }

    changeSet(author: "Sachin", id: "20210108024642-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'XSLT_NAME')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "XSLT_NAME", type: "varchar2(255 char)")
        }

        sql("update EX_RCONFIG set XSLT_NAME = RECEIVER_ID;")

    }
}