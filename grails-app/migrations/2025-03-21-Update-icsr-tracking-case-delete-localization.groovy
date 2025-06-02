databaseChangeLog = {
    changeSet(author: "RxL-Eugen-Semenov", id: "210320250800") {
        sql("update localization set text='Deleted ICSR Report for {0} (v{1}) - {2} : ({3}) Recipient : {4} (Justification : {5}, Justification (Japanese) : {6})' where code='auditLog.entityValue.icsr.delete' and loc='*' ")
        sql("update localization set text='{0}(v{1}) - {2} : ({3}) 受信者 : {4}  の ICSR レポートを削除しました(理由 : {5}, 理由 (英語) : {6})' where code='auditLog.entityValue.icsr.delete' and loc='ja' ")
    }
}
