package com.rxlogix.config.metadata

/**
 * This will give the mapping of SQL join columns between multiple case tables.
 *
 * Example the following DB tables: CASE_EVENT is joined with C_IDENTIFICATION table using CASE_ID
 * Example : CASE_EVENT_ASSESS is joined with C_IDENTIFICATION.CASE_ID and C_PROD_IDENTIFICATION.prod_rec_num and C_AE_IDENTIFICATION.AE_REC_NUM
 */
class CaseColumnJoinMapping {

    Long id
    SourceTableMaster tableName     // should be joined to the ReportTableMapping
    String columnName


    SourceTableMaster mapTableName  // should be joined to the ReportTableMapping
    String mapColumnName
    boolean isDeleted = false

    static mapping = {
        version false
        cache: "read-only"

        table name: "CASE_COLUMN_JOIN_MAPPING"

        tableName column: "TABLE_NAME_ATM_ID"
        columnName column: "COLUMN_NAME"            // DB Column name on which to join ( CASE_ID )
        mapTableName column: "MAP_TABLE_NAME_ATM_ID"
        mapColumnName column: "MAP_COLUMN_NAME"
        isDeleted column: "IS_DELETED"

    }

    static constraints = {
        tableName(nullable:false)
        columnName(blank:false, maxSize:40)
        mapTableName(nullable:false)
        mapColumnName(blank:false, maxSize:40)
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof CaseColumnJoinMapping)) return false

        CaseColumnJoinMapping that = (CaseColumnJoinMapping) o

        if (columnName != that.columnName) return false
//        if (id != that.id) return false
        if (mapColumnName != that.mapColumnName) return false
        if (mapTableName != that.mapTableName) return false
        if (tableName != that.tableName) return false
//        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0)
        result = 31 * result + (columnName != null ? columnName.hashCode() : 0)
        result = 31 * result + (mapTableName != null ? mapTableName.hashCode() : 0)
        result = 31 * result + (mapColumnName != null ? mapColumnName.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }

}
