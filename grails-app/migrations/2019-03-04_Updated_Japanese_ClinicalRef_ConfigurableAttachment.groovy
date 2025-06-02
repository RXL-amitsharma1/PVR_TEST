databaseChangeLog = {

    changeSet(author: "Ankita", id: "201902250103-1") {
        sql("update localization set text='クリニカルリファレンスナンバー' where code='app.studyDictionary.studyCompound.label' and loc='ja' ")
        sql("update localization set text='添付ファイル形式（全セクション）' where code='app.label.additionalAttachments.allSections' and loc='ja' ")
        sql("update localization set text='添付ファイルを設定する' where code='app.label.additionalAttachments.splitSections' and loc='ja' ")

    }
}