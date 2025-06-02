databaseChangeLog = {

    changeSet(author: "sargam", id: "301220190118-1") {
        sql("update localization set text='Successfully Removed Case(s)' where code='caseCommand.remove.caseNumbers.success' and loc='*' ")
    }
}