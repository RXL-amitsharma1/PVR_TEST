databaseChangeLog = {
    changeSet(author: "jitin (generated)", id: "1536656112619-1") {
        update(tableName: "LOCALIZATION", whereClause: "CODE = 'app.bulkUpdate.help.title' and LOC='ja'"){
            column(name: "TEXT", value: "Excelインポートヘルプ")
        }
    }
}
