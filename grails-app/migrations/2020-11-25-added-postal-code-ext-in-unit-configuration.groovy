databaseChangeLog = {

    changeSet(author: "shubham.sharma (generated)", id: "20201125010749-1") {
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "POSTAL_CODE_EXT", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
        sql("update UNIT_CONFIGURATION set POSTAL_CODE_EXT = null;")

    }

}