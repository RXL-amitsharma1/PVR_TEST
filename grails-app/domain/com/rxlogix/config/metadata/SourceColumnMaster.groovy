package com.rxlogix.config.metadata

import com.rxlogix.config.DateRangeType
import com.rxlogix.config.ReportField

/**
 * Column mapping with its relationship to a LM_ table and TABLE_NAME, and a PRIMARY_KEY_ID as a PK constraint.
 *
 * Example: C_IDENTIFICATION.COUNTRY_ID
 *      PRIMARY_KEY_ID: null because its not a PK of C_IDENTIFICATION
 *      LM_TABLE_NAME : LM_COUNTRIES joined on COUNTRY_ID
 *      LM_DECODE_COLUMN : COUNTRY (this is how you get the name of the country from a COUNTRY_ID)
 */
class SourceColumnMaster implements Serializable {
    SourceTableMaster tableName     // should be joined to the SourceTableMaster
    String columnName
    Long primaryKey
    SourceTableMaster lmTableName   // should be joined to the SourceTableMaster
    String lmJoinColumn
    String lmDecodeColumn
    String columnType
    String reportItem
    String lmJoinType
    String concatField
    boolean isDeleted = false
    String lang

    static mapping = {
        version false
        cache: "read-only"

        table name: "SOURCE_COLUMN_MASTER"

        tableName column: "TABLE_NAME_ATM_ID", fetch: "join"
        columnName column: "COLUMN_NAME"
        primaryKey column: "PRIMARY_KEY_ID"
        lmTableName column: "LM_TABLE_NAME_ATM_ID"
        lmJoinColumn column: "LM_JOIN_COLUMN"
        lmDecodeColumn column: "LM_DECODE_COLUMN"
        columnType column: "COLUMN_TYPE"
        reportItem column: "REPORT_ITEM"
        lmJoinType column: "LM_JOIN_EQUI_OUTER"   // E for Eq Join O for Outer Join
        id composite: ['reportItem', 'lang']
        concatField column: "CONCATENATED_FIELD"
        isDeleted column: "IS_DELETED"
        lang column: "LANG_ID", sqlType: char
    }


    static constraints = {
        tableName(nullable: false)
        columnName(maxSize: 40)
        primaryKey(nullable: true)
        lmTableName(nullable: true)
        lmJoinColumn(maxSize: 40, nullable: true)
        lmDecodeColumn(maxSize: 40, nullable: true)
        columnType(maxSize: 1)
        reportItem(maxSize: 80, nullable: false)
        // oracle doesn't like implicit unique on column which will already be set as the ID
        lmJoinType(maxSize: 1, nullable: true)
        concatField(maxSize: 1, nullable: true)
    }

    //TODO : Need to use only one b/w getFollowupReportDateColumn and getRevMasterDateColumn.
    static String getFollowupReportDateColumn(DateRangeType dateRangeType, Locale locale) {
        String followupReportDateCol = null
        followupReportDateCol = ReportField.findByName(dateRangeType.name).getSourceColumn(locale).columnName
        return followupReportDateCol
    }

    static String getRevMasterDateColumn(DateRangeType dateRangeType, Locale locale) {
        if(dateRangeType == null){
            return null
        }
        String revMasterDateCol = null
        revMasterDateCol = ReportField.findByName(dateRangeType?.name).getSourceColumn(locale).columnName
        return revMasterDateCol
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SourceColumnMaster)) return false

        SourceColumnMaster that = (SourceColumnMaster) o

        if (columnName != that.columnName) return false
        if (columnType != that.columnType) return false
        if (concatField != that.concatField) return false
//        if (id != that.id) return false
        if (lmDecodeColumn != that.lmDecodeColumn) return false
        if (lmJoinColumn != that.lmJoinColumn) return false
        if (lmJoinType != that.lmJoinType) return false
        if (lmTableName != that.lmTableName) return false
        if (primaryKey != that.primaryKey) return false
        if (reportItem != that.reportItem) return false
        if (tableName != that.tableName) return false
        if (lang != that.lang) return false
//        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (tableName != null ? tableName.hashCode() : 0)
        result = 31 * result + (columnName != null ? columnName.hashCode() : 0)
        result = 31 * result + (primaryKey != null ? primaryKey.hashCode() : 0)
        result = 31 * result + (lmTableName != null ? lmTableName.hashCode() : 0)
        result = 31 * result + (lmJoinColumn != null ? lmJoinColumn.hashCode() : 0)
        result = 31 * result + (lmDecodeColumn != null ? lmDecodeColumn.hashCode() : 0)
        result = 31 * result + (columnType != null ? columnType.hashCode() : 0)
        result = 31 * result + (reportItem != null ? reportItem.hashCode() : 0)
        result = 31 * result + (lmJoinType != null ? lmJoinType.hashCode() : 0)
        result = 31 * result + (concatField != null ? concatField.hashCode() : 0)
        result = 31 * result + (lang != null ? lang.hashCode() : 0)
//        result = 31 * result + id.hashCode()
//        result = 31 * result + version.hashCode()
        return result
    }

    static SourceColumnMaster getUsingReportItem(String reportItem, Locale locale) {
        this.findByReportItemAndLangInList(reportItem, [locale?.toString(), "*"])
    }

    transient String getReportFieldTableColumn() {
        return "${tableName?.tableAlias}.${columnName}"
    }

    public static void copyObj(SourceColumnMaster sourceObj, SourceColumnMaster targetObj) {
        targetObj.with {
            tableName = sourceObj.tableName
            columnName = sourceObj.columnName
            primaryKey = sourceObj.primaryKey
            lmTableName = sourceObj.lmTableName
            lmJoinColumn = sourceObj.lmJoinColumn
            lmDecodeColumn = sourceObj.lmDecodeColumn
            columnType = sourceObj.columnType
            lmJoinType = sourceObj.lmJoinType
            concatField = sourceObj.concatField
            isDeleted = sourceObj.isDeleted
        }
    }
}
