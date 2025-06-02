databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1543997773716-1") {
        addColumn(tableName: "CASE_DELIVERY") {
            column(name: "ADDITIONAL_ATTACHMENTS", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1543997773716-2") {
        addColumn(tableName: "DELIVERY") {
            column(name: "ADDITIONAL_ATTACHMENTS", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1543997773716-3") {
        addColumn(tableName: "EX_CASE_DELIVERY") {
            column(name: "ADDITIONAL_ATTACHMENTS", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1543997773716-4") {
        addColumn(tableName: "EX_DELIVERY") {
            column(name: "ADDITIONAL_ATTACHMENTS", type: "varchar2(255 char)")
        }
    }
}
