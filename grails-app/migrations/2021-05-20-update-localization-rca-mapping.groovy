databaseChangeLog = {
    changeSet(author: "shikhars", id: "200520211721") {
        sql("update localization set text='RCA Mapping' where code='app.lateMapping.title'")
    }
}
