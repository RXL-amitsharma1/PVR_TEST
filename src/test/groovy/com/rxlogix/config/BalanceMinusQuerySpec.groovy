package com.rxlogix.config


import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class BalanceMinusQuerySpec extends Specification implements DataTest, ControllerUnitTest<BalanceMinusQuery> {

    List<DistinctTable> distinctTableList
    List<BmQuerySection> bmQuerySections

    def setupSpec() {
        mockDomains SourceProfile,DistinctTable,BmQuerySection
    }

    void "test create BalanceMinusQuery with X etl days"() {
        given:
        def sourceProfile = new SourceProfile(sourceId: 1, sourceName: 'testSource', sourceAbbrev: 'TSS')
        def distinctTable1 = new DistinctTable(entity: 'C_IDENTIFICATION', sourceProfile: sourceProfile)
        def distinctTable2 = new DistinctTable(entity: 'C_IDENTIFICATION', sourceProfile: sourceProfile)
        distinctTableList = [distinctTable1, distinctTable2]
        def bmQuerySection = new BmQuerySection(sourceProfile: sourceProfile, executeFor:'LAST_X_ETL', xValue:2, flagCaseExclude:false, distinctTables : distinctTableList)
        bmQuerySections = [bmQuerySection]
        def balanceMinusQuery = new BalanceMinusQuery(id: 1, sourceProfile:'TSS', bmQuerySections:bmQuerySections, startDateTime: "2023-07-18T03:23+02:00", repeatInterval: "FREQ=MONTHLY;INTERVAL=6;BYDAY=WE;BYSETPOS=3;UNTIL=20140919;", createdBy: "admin", modifiedBy: "admin")
        balanceMinusQuery.save(validate: false);

        when:
        def balanceMinusQueryInstance = balanceMinusQuery

        then:
        balanceMinusQueryInstance.id==1
        balanceMinusQueryInstance.startDateTime=="2023-07-18T03:23+02:00"
        balanceMinusQueryInstance.repeatInterval=="FREQ=MONTHLY;INTERVAL=6;BYDAY=WE;BYSETPOS=3;UNTIL=20140919;"
        balanceMinusQueryInstance.createdBy.equals('admin')
        balanceMinusQueryInstance.modifiedBy.equals('admin')
        balanceMinusQueryInstance.bmQuerySections[0].executeFor.equals('LAST_X_ETL')
        balanceMinusQueryInstance.bmQuerySections[0].xValue==2
    }

    void "test create BalanceMinusQuery with last X days"() {
        given:
        def sourceProfile = new SourceProfile(sourceId: 1, sourceName: 'testSource', sourceAbbrev: 'TSS')
        def distinctTable1 = new DistinctTable(entityId: 'C_IDENTIFICATION', sourceProfile: sourceProfile)
        def distinctTable2 = new DistinctTable(entityId: 'C_IDENTIFICATION', sourceProfile: sourceProfile)
        distinctTableList = [distinctTable1, distinctTable2]
        def bmQuerySection = new BmQuerySection(sourceProfile: sourceProfile, executeFor:'LAST_X_DAYS', xValue:3, flagCaseExclude:false, distinctTables : distinctTableList)
        bmQuerySections = [bmQuerySection]
        def balanceMinusQuery = new BalanceMinusQuery(id: 1, sourceProfile:'TSS', bmQuerySections:bmQuerySections, startDateTime: "2023-07-18T03:23+02:00", repeatInterval: "FREQ=MONTHLY;INTERVAL=6;BYDAY=WE;BYSETPOS=3;UNTIL=20140919;", createdBy: "admin", modifiedBy: "admin")
        balanceMinusQuery.save(validate: false);

        when:
        def balanceMinusQueryInstance = balanceMinusQuery

        then:
        balanceMinusQueryInstance.id==1
        balanceMinusQueryInstance.startDateTime=="2023-07-18T03:23+02:00"
        balanceMinusQueryInstance.repeatInterval=="FREQ=MONTHLY;INTERVAL=6;BYDAY=WE;BYSETPOS=3;UNTIL=20140919;"
        balanceMinusQueryInstance.createdBy.equals('admin')
        balanceMinusQueryInstance.modifiedBy.equals('admin')
        balanceMinusQueryInstance.bmQuerySections[0].executeFor.equals('LAST_X_DAYS')
        balanceMinusQueryInstance.bmQuerySections[0].xValue==3
    }

}
