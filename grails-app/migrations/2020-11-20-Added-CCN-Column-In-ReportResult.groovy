databaseChangeLog = {

    changeSet(author: "shubham.sharma (generated)", id: "20201120041930-1") {
        addColumn(tableName: "RPT_RESULT") {
            column(name: "CLINICAL_COMPOUND_NUMBER", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
        sql("update RPT_RESULT set CLINICAL_COMPOUND_NUMBER = null;")

    }

}