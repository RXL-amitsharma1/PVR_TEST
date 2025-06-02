databaseChangeLog = {
    changeSet(author: "sergey (generated)", id: "20231122101010-1") {
        addColumn(tableName: "RPT_TEMPLT") {
            column(name: "MAX_CHART_POINTS", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
}
