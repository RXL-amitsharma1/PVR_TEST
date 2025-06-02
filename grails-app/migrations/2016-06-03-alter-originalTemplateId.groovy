databaseChangeLog = {

    changeSet(author: "michaelmorett", id: "ReportTemplate") {

        addColumn(tableName: "RPT_TEMPLT") {
            column(name: "ORIG_TEMPLT_ID_NEW", type: "number(19,0)")
        }

        sql("update RPT_TEMPLT set ORIG_TEMPLT_ID_NEW = ORIG_TEMPLT_ID;")
        sql("alter table RPT_TEMPLT drop column ORIG_TEMPLT_ID;")
        sql("alter table RPT_TEMPLT rename column ORIG_TEMPLT_ID_NEW to ORIG_TEMPLT_ID;")
    }

}
