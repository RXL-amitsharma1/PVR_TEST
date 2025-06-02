databaseChangeLog = {

    changeSet(author: "sergey (generated)", id: "20210804093100-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "DTAB_MEASURE", columnName: "VALUES_CHART_LABEL")
            }
        }
        addColumn(tableName: "DTAB_MEASURE") {
            column(name: "VALUES_CHART_LABEL", type: "varchar2(255 char)")
            column(name: "PERC_CHART_LABEL", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sergey (generated)", id: "20210804093100-2") {
        addColumn(tableName: "DTAB_TEMPLT") {
            column(name: "EXPORT_AS_IMAGE", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }
}