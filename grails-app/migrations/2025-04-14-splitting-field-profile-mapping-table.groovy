databaseChangeLog = {
    changeSet(author: "rxl-shivamg1", id: "202504141118-1") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "FIELD_PROFILE_RPT_FIELD")
            not {
                tableExists(tableName: "FIELD_PROFILE_RPT_FIELD_BKP")
            }
        }
        renameTable(oldTableName: "FIELD_PROFILE_RPT_FIELD", newTableName: "FIELD_PROFILE_RPT_FIELD_BKP")
    }

    changeSet(author: "rxl-shivamg1", id: "202504141118-2") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "FIELD_PROFILE_RPT_FIELD_BKP")
            and {
                columnExists(tableName: "FIELD_PROFILE_RPT_FIELD_BKP", columnName: "FIELD_PROFILE_REPORT_FIELDS_ID")
                columnExists(tableName: "FIELD_PROFILE_RPT_FIELD_BKP", columnName: "REPORT_FIELD_ID")
            }
            not {
                or {
                    tableExists(tableName: "FIELD_PROFILE_RPT_FIELD")
                    tableExists(tableName: "FIELD_PROFILE_BLINDED_FIELD")
                    tableExists(tableName: "FIELD_PROFILE_PROTECTED_FIELD")
                    primaryKeyExists(tableName: "FIELD_PROFILE_RPT_FIELD")
                    primaryKeyExists(tableName: "FIELD_PROFILE_BLINDED_FIELD")
                    primaryKeyExists(tableName: "FIELD_PROFILE_PROTECTED_FIELD")
                }
            }
        }

        sql("CREATE TABLE FIELD_PROFILE_RPT_FIELD AS (SELECT FIELD_PROFILE_REPORT_FIELDS_ID AS FIELD_PROFILE_ID, REPORT_FIELD_ID FROM FIELD_PROFILE_RPT_FIELD_BKP WHERE FIELD_PROFILE_REPORT_FIELDS_ID IS NOT NULL)")
        sql("DELETE FROM FIELD_PROFILE_RPT_FIELD WHERE ROWID IN (" +
                "SELECT ROWID FROM (" +
                "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY FIELD_PROFILE_ID, REPORT_FIELD_ID ORDER BY ROWID) AS row_num " +
                "FROM FIELD_PROFILE_RPT_FIELD) WHERE row_num > 1)")
        sql("DELETE FROM FIELD_PROFILE_RPT_FIELD WHERE FIELD_PROFILE_ID IS NULL OR REPORT_FIELD_ID IS NULL")
        addNotNullConstraint(tableName: "FIELD_PROFILE_RPT_FIELD", columnName: "FIELD_PROFILE_ID")
        addNotNullConstraint(tableName: "FIELD_PROFILE_RPT_FIELD", columnName: "REPORT_FIELD_ID")

        sql("CREATE TABLE FIELD_PROFILE_BLINDED_FIELD AS (SELECT FIELD_PROFILE_BLINDED_ID AS FIELD_PROFILE_ID, REPORT_FIELD_ID AS BLINDED_FIELD_ID FROM FIELD_PROFILE_RPT_FIELD_BKP WHERE FIELD_PROFILE_BLINDED_ID IS NOT NULL)")
        sql("DELETE FROM FIELD_PROFILE_BLINDED_FIELD WHERE ROWID IN (" +
                "SELECT ROWID FROM (" +
                "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY FIELD_PROFILE_ID, BLINDED_FIELD_ID ORDER BY ROWID) AS row_num " +
                "FROM FIELD_PROFILE_BLINDED_FIELD) WHERE row_num > 1)")
        sql("DELETE FROM FIELD_PROFILE_BLINDED_FIELD WHERE FIELD_PROFILE_ID IS NULL OR BLINDED_FIELD_ID IS NULL")
        addNotNullConstraint(tableName: "FIELD_PROFILE_BLINDED_FIELD", columnName: "FIELD_PROFILE_ID")
        addNotNullConstraint(tableName: "FIELD_PROFILE_BLINDED_FIELD", columnName: "BLINDED_FIELD_ID")

        sql("CREATE TABLE FIELD_PROFILE_PROTECTED_FIELD AS (SELECT FIELD_PROFILE_PROTECTED_ID AS FIELD_PROFILE_ID, REPORT_FIELD_ID AS PROTECTED_FIELD_ID FROM FIELD_PROFILE_RPT_FIELD_BKP WHERE FIELD_PROFILE_PROTECTED_ID IS NOT NULL)")
        sql("DELETE FROM FIELD_PROFILE_PROTECTED_FIELD WHERE ROWID IN (" +
                "SELECT ROWID FROM (" +
                "SELECT ROWID, ROW_NUMBER() OVER (PARTITION BY FIELD_PROFILE_ID, PROTECTED_FIELD_ID ORDER BY ROWID) AS row_num " +
                "FROM FIELD_PROFILE_PROTECTED_FIELD) WHERE row_num > 1)")
        sql("DELETE FROM FIELD_PROFILE_PROTECTED_FIELD WHERE FIELD_PROFILE_ID IS NULL OR PROTECTED_FIELD_ID IS NULL")
        addNotNullConstraint(tableName: "FIELD_PROFILE_PROTECTED_FIELD", columnName: "FIELD_PROFILE_ID")
        addNotNullConstraint(tableName: "FIELD_PROFILE_PROTECTED_FIELD", columnName: "PROTECTED_FIELD_ID")
        addPrimaryKey(columnNames: "FIELD_PROFILE_ID, PROTECTED_FIELD_ID", constraintName: "FIELD_PRFL_PRT_FIELD_PK", tableName: "FIELD_PROFILE_PROTECTED_FIELD")
    }
}
