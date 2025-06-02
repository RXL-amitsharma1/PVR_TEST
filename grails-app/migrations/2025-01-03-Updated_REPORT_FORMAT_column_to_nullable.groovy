databaseChangeLog = {

    changeSet(author: "Vivek", id: "202501031252-1") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Column Does Not Exists") {
            columnExists(tableName: "DISTRIBUTION_CHANNEL", columnName: "REPORT_FORMAT")
        }

        dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "REPORT_FORMAT", tableName: "DISTRIBUTION_CHANNEL")
    }

}
