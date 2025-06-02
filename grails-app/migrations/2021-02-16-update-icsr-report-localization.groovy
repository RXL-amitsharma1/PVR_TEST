databaseChangeLog = {

    changeSet(author: "anurag", id: "202102160103-1") {
        sql("update localization set text='ICSR 報告' where code='app.label.icsrReport' and loc='ja' ")
    }
}