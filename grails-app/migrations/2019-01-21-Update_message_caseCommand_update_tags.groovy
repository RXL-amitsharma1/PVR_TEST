databaseChangeLog = {

    changeSet(author: "ankita", id: "1960211799019-1") {
        sql("update localization set text='Successfully Updated Tag Information' where code='caseCommand.add.tags.success' ")
    }

}