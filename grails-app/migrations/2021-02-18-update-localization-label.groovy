databaseChangeLog = {

    changeSet(author: "anurag", id: "202102180103-1") {
        sql("update localization set text='Save As & Run' where code='default.button.saveAsAndRun.label' ")
    }
}