databaseChangeLog = {

  changeSet(author: "sergey", id: "202102241558-1") {
        createTable(tableName: "DELIVERIES_EXECUTABLE") {
            column(name: "DELIVERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EXECUTABLE_ID", type: "number(19,0)")

            column(name: "EXECUTABLE_IDX", type: "number(10,0)")
        }
    }
    changeSet(author: "sergey", id: "202102241558-2") {
        createTable(tableName: "DELIVERIES_EXECUTABLE_GRPS") {
            column(name: "DELIVERY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EXECUTABLE_GROUP_ID", type: "number(19,0)")

            column(name: "EXECUTABLE_GROUP_IDX", type: "number(10,0)")
        }
    }
}