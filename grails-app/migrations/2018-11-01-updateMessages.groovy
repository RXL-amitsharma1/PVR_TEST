databaseChangeLog = {

    changeSet(author: "shubham", id: "1536656112622-4") {
        sql("update localization set text='構成を読み込む' where code='app.label.loadConfigurations' and loc='ja' ")
    }

    changeSet(author: "shubham", id: "1536656112622-7") {
        sql("update localization set text='PV Reports - 構成を読み込む' where code='app.loadConfigurations.title' and loc='ja' ")
    }

    changeSet(author: "shubham", id: "1536656112622-6") {
        sql("update localization set text='構成を読み込む' where code='app.loadConfiguration.menu' and loc='ja' ")
    }
}