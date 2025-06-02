databaseChangeLog = {

    changeSet(author: "Shubham", id: "201902251247-1") {
        sql("update localization set text='括更新' where code='app.menu.bulkScheduling' and loc='ja' ")
        sql("update localization set text=' 設定テンプレート' where code='app.PeriodicReport.configuration.template.label' and loc='ja' ")
        sql("update localization set text='テンプレートから作成' where code='app.menu.createFromTemplate' and loc='ja' ")
    }
}