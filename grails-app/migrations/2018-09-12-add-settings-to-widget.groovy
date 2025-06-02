databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1536746452700-1") {
        addColumn(tableName: "RWIDGET") {
            column(name: "settings", type: "varchar2(4000 char)")
        }
    }

}
