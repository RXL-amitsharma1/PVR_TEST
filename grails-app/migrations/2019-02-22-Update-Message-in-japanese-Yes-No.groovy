databaseChangeLog = {

    changeSet(author: "Devendra", id: "1760211799023-1") {
        sql("update localization set text='はい' where code='app.label.yes' and loc='ja' ")
        sql("update localization set text='いいえ' where code='app.label.no' and loc='ja' ")
    }
}