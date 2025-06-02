databaseChangeLog = {

    changeSet(author: "ShubhamRx (generated)", id: "202009010345-0") {
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "PRODUCT_GROUP_SELECTION", type: "clob")
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "202009010345-1") {
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "EVENT_GROUP_SELECTION", type: "clob")
        }
    }

}