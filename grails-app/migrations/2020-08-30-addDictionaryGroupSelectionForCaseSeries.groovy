databaseChangeLog = {

    changeSet(author: "ShubhamRx (generated)", id: "202008300330-0") {
        addColumn(tableName: "CASE_SERIES") {
            column(name: "PRODUCT_GROUP_SELECTION", type: "clob")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "202008300330-1") {
        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "PRODUCT_GROUP_SELECTION", type: "clob")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "202008300330-2") {
        addColumn(tableName: "CASE_SERIES") {
            column(name: "EVENT_GROUP_SELECTION", type: "clob")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "202008300330-3") {
        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "EVENT_GROUP_SELECTION", type: "clob")
        }
    }
}