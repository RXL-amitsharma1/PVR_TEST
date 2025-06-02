databaseChangeLog = {

    changeSet(author: "ankita", id: "1536656112623-1") {
        sql("update localization set text='Excelファイルをインポートするための最も簡単な方法は、「Excelにエクスポート」ボタンを押して、受け取ったExcelファイルをテンプレートとして使用することです。' where code='app.bulkUpdate.help.text1' and loc='ja' ")
    }

    changeSet(author: "ankita", id: "1536656112623-2") {
        sql("update localization set text='インポートされるデータは4行目から始めます。' where code='app.bulkUpdate.help.text2' and loc='ja' ")
    }

    changeSet(author: "ankita", id: "1536656112623-3") {
        sql("update localization set text='「Repot name」列 - 空にしないでください。同じ名前の設定が存在する場合は既存のものが更新されます。存在しない場合は新しいものが作成されます。' where code='app.bulkUpdate.help.text3' and loc='ja' ")
    }
    changeSet(author: "ankita", id: "1536656112623-4") {
        sql("update localization set text='「Configuration Template」列 - 設定テンプレート名を記述します。 設定を新しく作成する場合は必須です。設定を更新する場合はこの列を無視します（すでに作成された設定の設定テンプレートを変更することはできません）。' where code='app.bulkUpdate.help.text4' and loc='ja' ")
    }

    changeSet(author: "ankita", id: "1536656112623-5") {
        sql("update localization set text='「製品成分」、「製品ファミリー」、「製品名」、「商品名」列 - この列の少なくとも1つを設定する必要があります。 値は大文字と小文字が区別され、「,」で区切る必要があります。見つからない値 は無視します。' where code='app.bulkUpdate.help.text5' and loc='ja' ")
    }

    changeSet(author: "ankita", id: "1536656112623-6") {
        sql("update localization set text='「集合レポートタイプ」列は、PBRER、IND、DSUR、PADER、PSUR、JPSR、JDSUR、NUPR、その他のいずれかになります。' where code='app.bulkUpdate.help.text6' and loc='ja' ")
    }
    changeSet(author: "ankita", id: "1536656112623-7") {
        sql("update localization set text='「日付範囲タイプ」、「X」、「開始日」、「終了日」列。最初の列に、次のいずれかを設定する必要があります：YESTERDAY、LAST_WEEK、LAST_MONTH、LAST_YEAR、LAST_X_DAYS、LAST_X_WEEKS、LAST_X_MONTHS、LAST_X_YEARS、TOMORROW、NEXT_WEEK、NEXT_MONTH、NEXT_YEAR、NEXT_X_DAYS、NEXT_X_WEEKS、NEXT_X_MONTHS、NEXT_X_YEARS（''nextXYears''、DateRangeValueEnum.RELATIVE）（_X_のタイプの場合はXを入力する必要があります）、CUMULATIVE、CUSTOM（開始日列および終了日列は入力する必要があります）。 日付の形式は、yyyy-MM-ddTHH:mm:ssです（例：2017-09-30T13:00:00）。' where code='app.bulkUpdate.help.text7' and loc='ja' ")
    }

    changeSet(author: "ankita", id: "1536656112623-8") {
        sql("update localization set text='「プライマリレポート先」列 - 空にしないでください。' where code='app.bulkUpdate.help.text8' and loc='ja' ")
    }

    changeSet(author: "ankita", id: "1536656112623-9") {
        sql("update localization set text='「期日」列 - 空の場合も整数値の場合もあります。' where code='app.bulkUpdate.help.text9' and loc='ja' ")
    }
    changeSet(author: "ankita", id: "1536656112623-10") {
        sql("update localization set text='「Scheduler」列はjsonフォーマットのスケジューラ記述にしなければなりません。このjsonは非常に慎重に編集することが重要です。作成済みの構成から（Excelエクスポートから）値をコピー/貼り付けすることをお勧めします。' where code='app.bulkUpdate.help.text10' and loc='ja' ")
    }
}