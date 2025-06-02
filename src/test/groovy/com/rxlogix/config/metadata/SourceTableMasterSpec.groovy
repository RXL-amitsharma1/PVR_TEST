package com.rxlogix.config.metadata

import spock.lang.Specification
import spock.lang.Unroll

class SourceTableMasterSpec extends Specification {

    @Unroll
    def "test equalsForSameObject"() {
        given:
        String tableName = tableNameValue
        String tableAlias = tableAliasValue
        String tableType = tableTypeValue
        Integer caseJoinOrder = caseJoinOrderValue
        String caseJoinType = caseJoinTypeValue
        String versionedData = versionedDataValue
        Integer hasEnterpriseId = hasEnterpriseIdValue

        SourceTableMaster sourceTableMaster = new SourceTableMaster(tableName: tableName, tableAlias: tableAlias, tableType: tableType, caseJoinOrder: caseJoinOrder, caseJoinType: caseJoinType, versionedData: versionedData, hasEnterpriseId: hasEnterpriseId)

        when:
        boolean equal = sourceTableMaster.equals(sourceTableMaster)

        then:
        equal == result

        where:
        tableNameValue | tableAliasValue | tableTypeValue | caseJoinOrderValue | caseJoinTypeValue | versionedDataValue | hasEnterpriseIdValue | result
        "test Table"   | "test Alias"    | "Table Type 1" | 1                  | "Join Type 1"     | "101"              | 0                    | true
    }

    @Unroll
    def "test equalsForDifferentObject"() {
        given:
        String tableName = tableNameValue
        String tableAlias = tableAliasValue
        String tableType = tableTypeValue
        Integer caseJoinOrder = caseJoinOrderValue
        String caseJoinType = caseJoinTypeValue
        String versionedData = versionedDataValue
        Integer hasEnterpriseId = hasEnterpriseIdValue

        SourceTableMaster baseSourceTableMaster = new SourceTableMaster(tableName: tableName, tableAlias: tableAlias, tableType: tableType, caseJoinOrder: caseJoinOrder, caseJoinType: caseJoinType, versionedData: versionedData, hasEnterpriseId: hasEnterpriseId)
        Object targetSourceTableMaster = targetSourceTableMasterValue

        when:
        boolean equal = baseSourceTableMaster.equals(targetSourceTableMaster)

        then:
        equal == result

        where:
        tableNameValue | tableAliasValue | tableTypeValue | caseJoinOrderValue | caseJoinTypeValue | versionedDataValue | hasEnterpriseIdValue | targetSourceTableMasterValue | result
        "test Table"   | "test Alias"    | "Table Type 1" | 1                  | "Join Type 1"     | "101"              | 0                    | new SourceTableMaster(tableName: tableNameValue, tableAlias: tableAliasValue, tableType: tableTypeValue, caseJoinOrder: caseJoinOrderValue, caseJoinType: caseJoinTypeValue, versionedData: versionedDataValue, hasEnterpriseId: hasEnterpriseIdValue)| true
        "test Table"   | "test Alias"    | "Table Type 1" | 1                  | "Join Type 1"     | "101"              | 0                    | new SourceTableMaster(tableName: "test table 2", tableAlias: tableAliasValue, tableType: tableTypeValue, caseJoinOrder: caseJoinOrderValue, caseJoinType: caseJoinTypeValue, versionedData: versionedDataValue, hasEnterpriseId: hasEnterpriseIdValue)| false
        "test Table"   | "test Alias"    | "Table Type 1" | 1                  | "Join Type 1"     | "101"              | 0                    | new String()                                                                                                                                                                                                                                          | false
    }

    @Unroll
    def "test hashCode"() {
        given:
        String tableName = tableNameValue
        String tableAlias = tableAliasValue
        String tableType = tableTypeValue
        Integer caseJoinOrder = caseJoinOrderValue
        String caseJoinType = caseJoinTypeValue
        String versionedData = versionedDataValue
        Integer hasEnterpriseId = hasEnterpriseIdValue

        SourceTableMaster baseSourceTableMaster = new SourceTableMaster(tableName: tableName, tableAlias: tableAlias, tableType: tableType, caseJoinOrder: caseJoinOrder, caseJoinType: caseJoinType, versionedData: versionedData, hasEnterpriseId: hasEnterpriseId)

        when:
        int hash = baseSourceTableMaster.hashCode()

        then:
        hash == result

        where:
        tableNameValue | tableAliasValue | tableTypeValue | caseJoinOrderValue | caseJoinTypeValue | versionedDataValue | hasEnterpriseIdValue | result
        null           | null            | null           | null               | null              | null               | null                 | 0
        "test Table"   | "test Alias"    | "Table Type 1" | 1                  | "Join Type 1"     | "101"              | 1                    | 1632121546
    }

    @Unroll
    def "test copyObj"() {
        given:
        String tableAlias = tableAliasValue
        String tableType = tableTypeValue
        Integer caseJoinOrder = caseJoinOrderValue
        String caseJoinType = caseJoinTypeValue
        String versionedData = versionedDataValue
        Integer hasEnterpriseId = hasEnterpriseIdValue
        boolean isDeleted = isDeletedValue

        SourceTableMaster baseSourceTableMaster = new SourceTableMaster(tableAlias: tableAlias, tableType: tableType, caseJoinOrder: caseJoinOrder, caseJoinType: caseJoinType, versionedData: versionedData, hasEnterpriseId: hasEnterpriseId, isDeleted: isDeleted)
        SourceTableMaster targetSourceTableMaster = new SourceTableMaster()

        when:
        SourceTableMaster.copyObj(baseSourceTableMaster, targetSourceTableMaster)

        then:
        targetSourceTableMaster.tableAlias == tableAliasValue
        targetSourceTableMaster.tableType == tableTypeValue
        targetSourceTableMaster.caseJoinOrder == caseJoinOrderValue
        targetSourceTableMaster.caseJoinType == caseJoinTypeValue
        targetSourceTableMaster.versionedData == versionedDataValue
        targetSourceTableMaster.hasEnterpriseId == hasEnterpriseIdValue
        targetSourceTableMaster.isDeleted == isDeletedValue

        where:
         tableAliasValue | tableTypeValue | caseJoinOrderValue | caseJoinTypeValue | versionedDataValue | hasEnterpriseIdValue | isDeletedValue
         "test Alias"    | "Table Type 1" | 1                  | "Join Type 1"     | "101"              | 0                    | false
//        "test Table"   | "test Alias"    | "Table Type 1" | 1                  | "Join Type 1"     | 101                | 0                    | false
    }

}