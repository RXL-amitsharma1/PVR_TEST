databaseChangeLog = {

    changeSet(author: "sargam (generated)", id: "1523257222401-2") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "IS_CREATED_BY_USER", type: "number(1,0)", defaultValue:0)
        }
    }
}