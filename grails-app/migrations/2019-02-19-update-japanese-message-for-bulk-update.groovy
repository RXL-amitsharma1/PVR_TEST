databaseChangeLog = {

    changeSet(author: "Shubham", id: "1760211799022-1") {
        sql("update localization set text='行{0}：スケジューラ列は必須です！' where code='app.bulkUpdate.error.scheduler.empty' and loc='ja' ")
        sql("update localization set text='行{0}：日付範囲のタイプが間違っています！' where code='app.bulkUpdate.error.dateRange.empty' and loc='ja' ")
        sql("update localization set text='行{0}：間違った集計レポートタイプです！' where code='app.bulkUpdate.error.reportType.wrong' and loc='ja' ")
        sql("update localization set text='行{0}：開始日/終了日の定義が間違っています！' where code='app.bulkUpdate.error.date.wrong' and loc='ja' ")
        sql("update localization set text='行{0}：製品が見つかりません！' where code='app.bulkUpdate.error.product' and loc='ja' ")
        sql("update localization set text='行{0}：' where code='app.bulkUpdate.error.row' and loc='ja' ")
        sql("update localization set text='{0}件のレポートが追加されました' where code='app.bulkUpdate.error.added' and loc='ja' ")
        sql("update localization set text='{0}件のレポートが更新されました' where code='app.bulkUpdate.error.updated' and loc='ja' ")
        sql("update localization set text='Excelインポートヘルプ' where code='app.bulkUpdate.help.title' and loc='ja' ")
        sql("update localization set text='行{0}：レポート名は必須です！' where code='app.bulkUpdate.error.reportName' and loc='ja' ")
        sql("update localization set text='行{0}：設定テンプレート{1}が見つかりません！' where code='app.bulkUpdate.error.template' and loc='ja' ")
        sql("update localization set text='行{0}：設定テンプレートは設定の作成に必須です！' where code='app.bulkUpdate.error.template.empty' and loc='ja' ")
    }
}