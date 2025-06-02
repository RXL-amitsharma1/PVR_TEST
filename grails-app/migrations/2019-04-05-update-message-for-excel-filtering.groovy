databaseChangeLog = {

    changeSet(author: "sgologuzov", id: "1554450042420-1") {
        sql("update localization set text='Total Row Count' where code='app.label.subTotalRowsNumber' and loc='*' ")
        sql("update localization set text='Total Case Count' where code='app.label.subTotalCaseNumber' and loc='*' ")
        sql("update localization set text='Total Row Count' where code='app.label.totalRowsNumber' and loc='*' ")
        sql("update localization set text='Total Case Count' where code='app.label.totalCaseNumber' and loc='*' ")
    }
}