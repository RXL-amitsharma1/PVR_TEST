databaseChangeLog = {
    changeSet(author: "RxL-Eugen-Semenov", id: "250220251135") {
        sql("update localization set text='You''ve reached the maximum limit of 1,000 records for bulk operations. To process more, please split your request into multiple Batches.' where code='app.icsrTracking.bulkUpdateMaxRowsWarning' and loc='*' ")
        sql("update localization set text='一括処理で処理可能な上限1000レコードを超過しました。引き続き実施するには複数回にわけて再度実施して下さい。' where code='app.icsrTracking.bulkUpdateMaxRowsWarning' and loc='ja' ")
    }
}
