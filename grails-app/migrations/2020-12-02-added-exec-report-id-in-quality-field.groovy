databaseChangeLog = {

    changeSet(author: "rishabhj", id: "021220201702") {
        addColumn(tableName: "QUALITY_FIELD") {
            column(name: "EXEC_REPORT_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update QUALITY_FIELD set EXEC_REPORT_ID = null;")

    }

}