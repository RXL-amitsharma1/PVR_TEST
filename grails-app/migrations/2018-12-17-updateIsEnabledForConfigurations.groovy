databaseChangeLog = {
    changeSet(author: "shubham (generated)", id: "2960211999018-9") {

        update(tableName: "RCONFIG", whereClause: "NEXT_RUN_DATE is null"){
            column(name: "IS_ENABLED", value: 0)
        }

    }
}
