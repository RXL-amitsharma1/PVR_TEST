databaseChangeLog = {

    changeSet(author: "sergey (generated)", id: "20200728093100-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "DTAB_MEASURE", columnName: "VALUES_CHART_TYPE")
            }
        }
        addColumn(tableName: "DTAB_MEASURE") {
            column(name: "PERC_CHART_TYPE", type: "varchar2(255 char)")
            column(name: "VALUES_CHART_TYPE", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey (generated)", id: "20200805093100-2") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RWIDGET", columnName: "section_number")
            }
        }
        addColumn(tableName: "RWIDGET") {
            column(name: "section_number", type: "number(19,0)")
        }
    }
}