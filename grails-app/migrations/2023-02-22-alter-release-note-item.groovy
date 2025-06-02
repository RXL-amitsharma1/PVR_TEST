databaseChangeLog = {
    changeSet(author: "sergey", id: "202202221000") {
        addColumn(tableName: "RELEASE_NOTES_ITEM") {
            column(name: "SUMMARY_CLOB", type: "clob")
        }
        addColumn(tableName: "RELEASE_NOTES_ITEM") {
            column(name: "SHORT_DESCRIPTION_CLOB", type: "clob")
        }
        sql("update RELEASE_NOTES_ITEM set SUMMARY_CLOB = SUMMARY;")
        sql("update RELEASE_NOTES_ITEM set SHORT_DESCRIPTION_CLOB = SHORT_DESCRIPTION;")
        sql("alter table RELEASE_NOTES_ITEM drop column SUMMARY;")
        sql("alter table RELEASE_NOTES_ITEM drop column SHORT_DESCRIPTION;")
        sql("alter table RELEASE_NOTES_ITEM rename column SUMMARY_CLOB to SUMMARY;")
        sql("alter table RELEASE_NOTES_ITEM rename column SHORT_DESCRIPTION_CLOB to SHORT_DESCRIPTION;")
    }
}