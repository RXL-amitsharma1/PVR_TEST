databaseChangeLog = {

    changeSet(author: "Shubham", id: "201902250102-1") {
        sql("update localization set text='無効な版数' where code='caseCommand.invalid.versionNumber' and loc='ja' ")
        sql("update localization set text='無効な症例番号' where code='caseCommand.INVALID_CASE_NUMBER' and loc='ja' ")
        sql("update localization set text='無効な症例番号 - 版数' where code='caseCommand.INVALID_CASE_VERSION' and loc='ja' ")
        sql("update localization set text='症例番号が基準を満たしていません' where code='caseCommand.NOT_MEETING_CRITERIA' and loc='ja' ")
    }
}