databaseChangeLog = {

    changeSet(author: "ShubhamRx (generated)", id: "202007030437-0") {
        addColumn(tableName: "RCONFIG") {
            column(name: "EVENT_GROUP_SELECTION", type: "clob")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "202007030537-0") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "EVENT_GROUP_SELECTION", type: "clob")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "202007030630-0") {
        addColumn(tableName: "RCONFIG") {
            column(name: "PRODUCT_GROUP_SELECTION", type: "clob")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "202007030715-0") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "PRODUCT_GROUP_SELECTION", type: "clob")
        }
    }
}