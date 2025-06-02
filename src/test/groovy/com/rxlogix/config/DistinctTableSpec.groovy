package com.rxlogix.config


import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class DistinctTableSpec extends Specification implements DomainUnitTest<DistinctTable> {

    List<DistinctTable> distinctTableList

    def setupSpec() {
        mockDomain SourceProfile
    }

    void "test create DistinctTable"() {
        given:
        def sourceProfile = new SourceProfile(sourceId: 1, sourceName: 'testSource', sourceAbbrev: 'TSS')
        def distinctTable1 = new DistinctTable(entity: 'C_IDENTIFICATION', sourceProfile: sourceProfile)
        distinctTable1.save(validate: false)
        def distinctTable2 = new DistinctTable(entity: 'CDR_CLOB_AE_ALL', sourceProfile: sourceProfile)
        distinctTable2.save(validate: false)

        when:
        distinctTableList = [distinctTable1, distinctTable2]

        then:
        distinctTableList[0].entity.equals('C_IDENTIFICATION')
        distinctTableList[0].sourceProfile == sourceProfile
        distinctTableList[1].entity.equals('CDR_CLOB_AE_ALL')
        distinctTableList[1].sourceProfile == sourceProfile
    }

}
