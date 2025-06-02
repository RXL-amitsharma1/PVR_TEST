databaseChangeLog = {

    changeSet(author: "Ankita", id: "201902250103-1") {
        sql("update localization set text='スケジュールされた' where code='app.executionStatus.dropDown.SCHEDULED' and loc='ja' ")
        sql("update localization set text='進行中' where code='app.executionStatus.dropDown.GENERATING' and loc='ja' ")
        sql("update localization set text='完了しました - 版数' where code='app.executionStatus.dropDown.COMPLETED' and loc='ja' ")
        sql("update localization set text='キュー' where code='app.executionStatus.dropDown.BACKLOG' and loc='ja' ")
        sql("update localization set text='エラー' where code='app.executionStatus.dropDown.ERROR' and loc='ja' ")
    }
}