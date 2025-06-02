databaseChangeLog = {
    changeSet(author: "jitin (generated)", id: "2960211999018-8") {

        update(tableName: "LOCALIZATION", whereClause: "CODE = 'app.bulkUpdate.error.added'"){
            column(name: "TEXT", value: "{0} reports added")
        }

        update(tableName: "LOCALIZATION", whereClause: "CODE = 'app.bulkUpdate.error.updated'"){
            column(name: "TEXT", value: "{0} reports updated")
        }
    }
}
