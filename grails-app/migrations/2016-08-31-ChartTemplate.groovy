databaseChangeLog = {
    changeSet(author: "sgologuzov (generated)", id: "1472160446412-1") {
        addColumn(tableName: "DTAB_TEMPLT") {
            column(name: "CHART_CUSTOM_OPTIONS", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
}
