databaseChangeLog = {
    changeSet(author: "shikhars", id: "141020211701-1") {
        sql("update localization set text='Last Successful ETL Start Time' where code='app.last.successful.etl.start.time' ")
    }
}