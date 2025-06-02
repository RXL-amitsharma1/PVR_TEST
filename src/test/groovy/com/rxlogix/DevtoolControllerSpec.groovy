package com.rxlogix


import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ReportRequest
import com.rxlogix.hazelcast.HazelService
import grails.plugin.springsession.SpringSessionConfigProperties
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import org.springframework.security.core.session.SessionRegistry
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ExecutionStatus])
class DevtoolControllerSpec extends Specification implements DataTest, ControllerUnitTest<DevtoolController> {

    def setupSpec() {
        mockDomain ExecutionStatus
    }

    void "test index"(){
        when:
        controller.index()
        then:
        response.text=="Add endpoints to check info <br/> 1. configProperties <br/> 2. activeSessions <br/> 3. activeReports <br/> 4. killExecution/:id <br/> 5. changeLoggingLevel?packageName=:PackageName&newLevel=:NewLevel"
    }

    void "test configProperties"(){
        when:
        controller.configProperties()
        then:
        response.status==200
    }

    void "test activeSessions if activeusernames"(){
        given:
        def mockHazelService=Mock(HazelService)
        mockHazelService.createMap(_)>>{[mapName: 'spring:session']}
        controller.hazelService=mockHazelService
        def mocksessionRegistry=Mock(SessionRegistry)
        mocksessionRegistry.getAllPrincipals()>>{[mapName: 'spring:session']}
        controller.sessionRegistry=mocksessionRegistry
        def mockspringSessionConfigProperties=Mock(SpringSessionConfigProperties)
        mockspringSessionConfigProperties.SpringSessionConfigProperties() >> {def springSessionConfig -> [mapName: 'spring:session']}
        controller.springSessionConfigProperties=mockspringSessionConfigProperties
        when:
        Holders.config.springsession.enabled=true
        Holders.config.hazelcast.enabled=true
        controller.activeSessions()
        then:
        response.status==200
    }

    void "test activeSessions else activeusernames"(){
        given:
        def mockHazelService=Mock(HazelService)
        mockHazelService.createMap(_)>>{[mapName: 'spring:session']}
        controller.hazelService=mockHazelService
        def mocksessionRegistry=Mock(SessionRegistry)
        mocksessionRegistry.getAllPrincipals()>>{[]}
        controller.sessionRegistry=mocksessionRegistry
        def mockspringSessionConfigProperties=Mock(SpringSessionConfigProperties)
        mockspringSessionConfigProperties.SpringSessionConfigProperties() >> {def springSessionConfig -> [mapName: 'spring:session']}
        controller.springSessionConfigProperties=mockspringSessionConfigProperties
        when:
        Holders.config.springsession.enabled=false
        Holders.config.hazelcast.enabled=false
        controller.activeSessions()
        then:
        response.status==200
    }

    void "test activeReports"(){
        given:
        ExecutionStatus executionStatus=new ExecutionStatus(reportName: "rx test")
        ExecutionStatus.metaClass.static.get={Long id -> executionStatus}
        def mockExecutorThreadInfoService=Mock(ExecutorThreadInfoService)
        mockExecutorThreadInfoService.getTotalCurrentlyRunningIds()>>{[]}
        controller.executorThreadInfoService=mockExecutorThreadInfoService
        when:
        controller.activeReports()
        then:
        response.status==200
    }
}
