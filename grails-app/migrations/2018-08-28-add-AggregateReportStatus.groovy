databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1535454594548-1") {
        addColumn(tableName: "EX_STATUS") {
            column(name: "AGG_RPT_STATUS", type: "varchar2(255 char)")
        }
    }
}
