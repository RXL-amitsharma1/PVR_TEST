databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1506327785433-1") {

        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "DESCRIPTION_NEW", type: "varchar2(1000 char)") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_CASE_SERIES set DESCRIPTION_NEW = dbms_lob.substr( DESCRIPTION, 1000, 1 );")

        dropColumn(tableName: "EX_CASE_SERIES", columnName: "DESCRIPTION")

        renameColumn(tableName: "EX_CASE_SERIES", oldColumnName: "DESCRIPTION_NEW", newColumnName: "DESCRIPTION")

        modifyDataType(columnName: "DESCRIPTION", newDataType: "varchar2(1000 char)", tableName: "CASE_SERIES")
    }
}
