databaseChangeLog = {
    changeSet(author: "jitin (generated)", id: "1536656112619-4") {
        update(tableName: "LOCALIZATION", whereClause: "CODE = 'templateQuery.displayMedDraVersionNumber.label' and LOC='*'"){
            column(name: "TEXT", value: "Display MedDRA version in the footer")
        }
    }
}
