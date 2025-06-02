databaseChangeLog = {
    changeSet(author: "rxl-shivamg1", id: "202504131911-1") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "FIELD_PROFILE_RPT_FIELD")
            tableExists(tableName: "FIELD_PROFILE_BLINDED_FIELD")
            tableExists(tableName: "FIELD_PROFILE_PROTECTED_FIELD")
            tableExists(tableName: "FIELD_PROFILE_RPT_FIELD_BKP")
        }

        dropTable(tableName: "FIELD_PROFILE_RPT_FIELD")
        dropTable(tableName: "FIELD_PROFILE_BLINDED_FIELD")
        dropTable(tableName: "FIELD_PROFILE_PROTECTED_FIELD")
        renameTable(oldTableName: "FIELD_PROFILE_RPT_FIELD_BKP", newTableName: "FIELD_PROFILE_RPT_FIELD")

    }
}
