databaseChangeLog = {

    changeSet(author: "sargam", id: "1536656112622-1") {
        sql("update localization set text='VOLUME 9A' where code='app.periodicReportType.VOLUME9A' ")
    }
}