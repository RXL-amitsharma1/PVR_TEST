package com.rxlogix.config.metadata

import spock.lang.Specification
import spock.lang.Unroll

class CaseColumnJoinMappingSpec extends Specification {
    @Unroll
    def "test equalsForSameObject"() {
        given:
        Long id = idValue
        SourceTableMaster tableName = tableNameValue
        String columnName = columnNameValue
        SourceTableMaster mapTableName = mapTableNameValue
        String mapColumnName = mapColumnNameValue
        boolean isDeleted = isDeletedValue

        CaseColumnJoinMapping caseColumnJoinMapping = new CaseColumnJoinMapping(id: id,tableName: tableName,columnName: columnName,mapTableName: mapTableName,mapColumnName: mapColumnName,isDeleted: isDeleted)

        when:
        boolean equal = caseColumnJoinMapping.equals(caseColumnJoinMapping)

        then:
        equal == result

        where:
        idValue | tableNameValue          | columnNameValue | mapTableNameValue       | mapColumnNameValue |  isDeletedValue | result
        101     | new SourceTableMaster() | "Test Column"   | new SourceTableMaster() | "Test Map COlumn"  |  true           | true

    }

    @Unroll
    def "test equalsForDifferentObject"() {
        given:
        Long id = idValue
        SourceTableMaster tableName = tableNameValue
        String columnName = columnNameValue
        SourceTableMaster mapTableName = mapTableNameValue
        String mapColumnName = mapColumnNameValue
        boolean isDeleted = isDeletedValue

        CaseColumnJoinMapping baseCaseColumnJoinMapping = new CaseColumnJoinMapping(id: id,tableName: tableName,columnName: columnName,mapTableName: mapTableName,mapColumnName: mapColumnName,isDeleted: isDeleted)
        Object targetCaseColumnJoinMapping = targetCaseColumnJoinMappingObject

        when:
        boolean equal = baseCaseColumnJoinMapping.equals(targetCaseColumnJoinMapping)

        then:
        equal == result

        where:
        idValue | tableNameValue          | columnNameValue | mapTableNameValue       | mapColumnNameValue |  isDeletedValue | targetCaseColumnJoinMappingObject                                                                                                                                                                                               | result
        101     | new SourceTableMaster() | "Test Column"   | new SourceTableMaster() | "Test Map COlumn"  |  true           | new CaseColumnJoinMapping(id: idValue,tableName: tableNameValue,columnName: columnNameValue,mapTableName: mapTableNameValue,mapColumnName: mapColumnNameValue,isDeleted: isDeletedValue)                                        | true
        101     | new SourceTableMaster() | "Test Column"   | new SourceTableMaster() | "Test Map COlumn"  |  true           | new CaseColumnJoinMapping(id: idValue,tableName: tableNameValue,columnName: "Test Column 2",mapTableName: mapTableNameValue,mapColumnName: mapColumnNameValue,isDeleted: isDeletedValue)                                        | false
        101     | new SourceTableMaster() | "Test Column"   | new SourceTableMaster() | "Test Map COlumn"  |  true           | new CaseColumnJoinMapping(id: idValue,tableName: new SourceTableMaster(tableName: "Source Table Name"),columnName: columnNameValue,mapTableName: mapTableNameValue,mapColumnName: mapColumnNameValue,isDeleted: isDeletedValue) | false
        101     | new SourceTableMaster() | "Test Column"   | new SourceTableMaster() | "Test Map COlumn"  |  true           | new CaseColumnJoinMapping(id: idValue,tableName: tableNameValue,columnName: columnNameValue,mapTableName: new SourceTableMaster(tableName:"Source Table Name"),mapColumnName: mapColumnNameValue,isDeleted: isDeletedValue)     | false
        101     | new SourceTableMaster() | "Test Column"   | new SourceTableMaster() | "Test Map COlumn"  |  true           | new CaseColumnJoinMapping(id: idValue,tableName: tableNameValue,columnName: columnNameValue,mapTableName: mapTableNameValue,mapColumnName: "Test Map COlumn 2",isDeleted: isDeletedValue)                                       | false
        101     | new SourceTableMaster() | "Test Column"   | new SourceTableMaster() | "Test Map COlumn"  |  true           | new String()                                                                                                                                                                                                                    | false

    }

    @Unroll
    def "test hashCode"() {
        given:
        Long id = idValue
        SourceTableMaster tableName = tableNameValue
        String columnName = columnNameValue
        SourceTableMaster mapTableName = mapTableNameValue
        String mapColumnName = mapColumnNameValue

        CaseColumnJoinMapping baseCaseColumnJoinMapping = new CaseColumnJoinMapping(id: id,tableName: tableName,columnName: columnName,mapTableName: mapTableName,mapColumnName: mapColumnName)

        when:
        int hashCode = baseCaseColumnJoinMapping.hashCode()

        then:
        hashCode == result

        where:
        idValue | tableNameValue          | columnNameValue | mapTableNameValue       | mapColumnNameValue | result
        null    | null                    | null            | null                    | null               |  0
        102     | null                    | null            | null                    | null               |  0
        102     | new SourceTableMaster() | null            | null                    | null               |  0
        102     | new SourceTableMaster() | "Test Column"   | null                    | null               |  -1550210340
        102     | new SourceTableMaster() | "Test Column"   | new SourceTableMaster() | null               |  -1550210340
        102     | new SourceTableMaster() | "Test Column"   | new SourceTableMaster() | "Test Map COlumn"  |  858356980
        null    | new SourceTableMaster() | "Test Column"   | new SourceTableMaster() | "Test Map COlumn"  |  858356980
        null    | null                    | "Test Column"   | new SourceTableMaster() | "Test Map COlumn"  |  858356980
        null    | null                    | null            | new SourceTableMaster() | "Test Map COlumn"  |  -1886399976
        null    | null                    | null            | null                    | "Test Map COlumn"  |  -1886399976


    }
}