databaseChangeLog = {

    changeSet(author: "Shubham", id: "201902270627-1") {
        sql("update localization set text='Email body can not be empty!' where code='com.rxlogix.config.EmailTemplate.body.nullable' and loc='*' ")
        sql("update localization set text='メール本文は空にできません！' where code='com.rxlogix.config.EmailTemplate.body.nullable' and loc='ja' ")
        sql("update localization set text='タスクの優先度を指定して下さい。' where code='com.rxlogix.config.Task.priority.nullable' and loc='ja' ")
    }

    changeSet(author: "Anurag", id: "201912230627-2") {
        sql("update localization set text='Parent Template' where code='app.XMLNodeType.TAG_PROPERTIES' and loc='*' ")
        sql("update localization set text='親テンプレート' where code='app.XMLNodeType.TAG_PROPERTIES' and loc='ja' ")
        sql("update localization set text='Field' where code='app.XMLNodeType.SOURCE_FIELD' and loc='*' ")
        sql("update localization set text='フィールド' where code='app.XMLNodeType.SOURCE_FIELD' and loc='ja' ")
    }
}