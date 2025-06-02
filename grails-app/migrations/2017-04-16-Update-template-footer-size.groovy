databaseChangeLog = {
    changeSet(author: "emilmatevosyan", id: "1488888855555-14") {
        sql("ALTER TABLE RPT_TEMPLT MODIFY TEMPLATE_FOOTER varchar2(1000)")
    }
}