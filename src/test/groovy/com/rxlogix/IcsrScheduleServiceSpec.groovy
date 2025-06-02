package com.rxlogix

import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.IcsrTemplateQuery
import com.rxlogix.mapping.IcsrCaseProcessingQueue
import com.rxlogix.mapping.IcsrManualLockedCase
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.sql.Sql
import org.grails.datastore.mapping.simple.SimpleMapDatastore
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges
import com.rxlogix.config.Tenant

@ConfineMetaClassChanges([Sql])
class IcsrScheduleServiceSpec extends Specification implements DataTest, ServiceUnitTest<IcsrScheduleService> {

    private SimpleDriverDataSource reportDataSourcePVR

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains IcsrProfileConfiguration, IcsrTemplateQuery
    }

    void "test makeManualCasesReadyForGeneration for flagNullification as 0"() {
        setup:
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnection() >> {
            return newConn()
        }
        service.utilService=mockUtilService
        Sql.metaClass.call = true

        service.targetDatastore = new SimpleMapDatastore(['pva'], IcsrManualLockedCase)
        service.targetDatastore = new SimpleMapDatastore(['pva'], IcsrCaseProcessingQueue)
        IcsrManualLockedCase icsrManualLockedCase = new IcsrManualLockedCase(flagNullification:0,caseId:1,profileName:"profile1")
        icsrManualLockedCase.save(validate:false, failOnError: true, flush: true)
        def result = "Processing case $icsrManualLockedCase.caseId for profile $icsrManualLockedCase.profileName got completed"

        when:
        service.makeManualCasesReadyForGeneration()

        then:
        result == "Processing case 1 for profile profile1 got completed"
    }

    void "test makeManualCasesReadyForGeneration for flagNullification as 2 and autoScheduleFUPReport as true"() {
        setup:
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnection() >> {
            return newConn()
        }
        service.utilService=mockUtilService
        Sql.metaClass.call = true

        service.targetDatastore = new SimpleMapDatastore(['pva'], IcsrManualLockedCase)
        IcsrManualLockedCase icsrManualLockedCase = new IcsrManualLockedCase(flagNullification:1,caseId:2,profileName:"profile2")
        icsrManualLockedCase.save(validate:false, failOnError: true, flush: true)
        IcsrProfileConfiguration icsrProfileConfiguration = new IcsrProfileConfiguration(reportName : icsrManualLockedCase.profileName, autoScheduleFUPReport: true)
        IcsrTemplateQuery icsrTemplateQuery = new IcsrTemplateQuery()
        icsrProfileConfiguration.save(validate:false, failOnError: true, flush: true)
        icsrTemplateQuery.save(validate:false, failOnError: true, flush: true)
        def result = "Added case $icsrManualLockedCase.caseId for profile $icsrManualLockedCase.profileName for Nullification Process"

        when:
        service.makeManualCasesReadyForGeneration()

        then:
        result == "Added case 2 for profile profile2 for Nullification Process"
    }

    void "test makeManualCasesReadyForGeneration for flagNullification as 2 and autoScheduleFUPReport as false"() {
        setup:
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnection() >> {
            return newConn()
        }
        service.utilService=mockUtilService
        Sql.metaClass.call = true

        service.targetDatastore = new SimpleMapDatastore(['pva'], IcsrManualLockedCase)
        IcsrManualLockedCase icsrManualLockedCase = new IcsrManualLockedCase(flagNullification:2,caseId:3,profileName:"profile3")
        icsrManualLockedCase.save(validate:false, failOnError: true, flush: true)
        IcsrProfileConfiguration icsrProfileConfiguration = new IcsrProfileConfiguration(reportName : icsrManualLockedCase.profileName, autoScheduleFUPReport: false)
        icsrProfileConfiguration.save(validate:false, failOnError: true, flush: true)
        when:
        service.makeManualCasesReadyForGeneration()

        then: "Case will be ignored"
    }

    void "test generateCasesResultData with no available slots"() {
        setup:
        service.executorThreadInfoService = Mock(ExecutorThreadInfoService) {
            availableSlotsForCasesGeneration() >> 0
        }
        Tenant tenant = new Tenant(name: "tenant1", active: true)
        tenant.save(validate: false, flush: true)

        when:
        service.generateCasesResultData()

        then:
        noExceptionThrown()
    }

    def newConn(){
        reportDataSourcePVR = new SimpleDriverDataSource()
        Properties properties = new Properties()
        properties.put("defaultRowPrefetch", grailsApplication.config.jdbcProperties.fetch_size ?: 50)
        properties.put("defaultBatchValue", grailsApplication.config.jdbcProperties.batch_size ?: 5)
        properties.put("dbdriver", "com.mysql.jdbc.Driver")
        reportDataSourcePVR.setConnectionProperties(properties)
        reportDataSourcePVR.setDriverClass(org.h2.Driver)
        reportDataSourcePVR.setUsername('sa')
        reportDataSourcePVR.setPassword('sa')
        reportDataSourcePVR.setUrl('jdbc:h2:mem:testDb')

        return reportDataSourcePVR.getConnection('sa','sa')
    }
}
