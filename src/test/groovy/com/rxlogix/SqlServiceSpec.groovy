package com.rxlogix

import com.rxlogix.config.CustomSQLTemplate
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SqlService])
class SqlServiceSpec extends Specification implements DataTest, ServiceUnitTest<SqlService> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomain CustomSQLTemplate
    }

    void "test validateColumnName success"(){
        service.metaClass.getColumnsFromSqlQuery = {String sqlQuery, boolean usePvrDB, boolean throwException ->
            return values
        }
        when:
        def result = service.validateColumnName("",true)
        then:
        result.class == Boolean
        result == resulVal
        where:
        values << [["場合","case number"],["場合","case_number"],["ケース番号","数","case"],["場合","case & number"]]
        resulVal << [true,true,true,false]
    }
    void "test removeTableWithOracleTableAsString"() {
        when:
        def result = service.removeTableWithOracleTableAsString("select 'dual' from xyz")
        then:
        result == "select  from xyz"
    }
}
