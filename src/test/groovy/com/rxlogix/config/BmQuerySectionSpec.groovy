package com.rxlogix.config


import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class BmQuerySectionSpec extends Specification implements DataTest, DomainUnitTest<BmQuerySection> {

    List<DistinctTable> distinctTableList
    List<BmQuerySection> bmQuerySections

    def setupSpec() {
        mockDomains SourceProfile, DistinctTable
    }

    void "test create BmQuerySection with last X days and last x etl"() {
        given:
        def sourceProfile = new SourceProfile(sourceId: 1, sourceName: 'testSource', sourceAbbrev: 'TSS')
        def distinctTable1 = new DistinctTable(entity: 'C_IDENTIFICATION', sourceProfile: sourceProfile)
        def distinctTable2 = new DistinctTable(entity: 'C_IDENTIFICATION', sourceProfile: sourceProfile)
        distinctTableList = [distinctTable1, distinctTable2]
        def bmQuerySection1 = new BmQuerySection(sourceProfile: sourceProfile, executeFor:'LAST_X_DAYS', xValue:3, flagCaseExclude:false, distinctTables : distinctTableList)
        bmQuerySection1.save(validate: false)
        def bmQuerySection2 = new BmQuerySection(sourceProfile: sourceProfile, executeFor:'LAST_X_ETL', xValue:4, flagCaseExclude:false, distinctTables : distinctTableList)
        bmQuerySection2.save(validate: false)

        when:
        bmQuerySections = [bmQuerySection1, bmQuerySection2]

        then:
        bmQuerySections[0].executeFor.equals('LAST_X_DAYS')
        bmQuerySections[0].xValue==3
        bmQuerySections[1].executeFor.equals('LAST_X_ETL')
        bmQuerySections[1].xValue==4
    }

}
