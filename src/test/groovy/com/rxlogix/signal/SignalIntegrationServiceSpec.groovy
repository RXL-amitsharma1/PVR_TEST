package com.rxlogix.signal


import com.rxlogix.config.*
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import groovyx.net.http.Method
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SignalReportInfo])
class SignalIntegrationServiceSpec extends Specification implements DataTest, ServiceUnitTest<SignalIntegrationService> {

    SignalReportInfo signalReportInfoInstance
    String url
    User userInstance

    def setup() {
        userInstance = new User(username: "test name").save(validate: false)
        signalReportInfoInstance = new SignalReportInfo(reportName: "Test Report",linkUrl: "dafs",userId: userInstance.id).save(validate: false)
        url = grailsApplication.config.pvsignal.url
    }

    def setupSpec() {
        mockDomains SignalReportInfo,ExecutedConfiguration,User, ExecutedCaseSeries,ExecutionStatus, ReportTemplate
    }

    void "test getSignalReportInfo method"(){
        given:
        SignalReportInfo.metaClass.static.findByConfigurationAndIsGenerating = {Configuration configuration, Boolean val ->
            signalReportInfoInstance
        }

        when:
        //Call the save method.
        SignalReportInfo signalReportInfoObj = service.getSignalReportInfo(new Configuration())

        then:
        signalReportInfoObj?.id == 1
    }

    void "test saveSignalReportResult method"(){
        given:
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(owner: userInstance)
        executedConfiguration.save(validate: false)
        service.dynamicReportService = [createMultiTemplateReport: { obj, params ->
            return File.createTempFile("test","txt")
        }]
        def mockSignalApiService = Mock(SignalIntegrationApiService)
        mockSignalApiService.postData(_,_,_,_) >> [status:true]
        service.signalIntegrationApiService = mockSignalApiService

        when:
        service.saveSignalReportResult(executedConfiguration,signalReportInfoInstance)

        then:
        1*service.signalIntegrationApiService.postData(_,_,_,_)
        notThrown(Exception)
    }

    void "test sendErrorNotification method"(){
        given:
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(reportName: 'Test Report', owner: userInstance)
        executedConfiguration.save(validate: false)
        def mockSignalApiService = Mock(SignalIntegrationApiService)
        mockSignalApiService.postData(_,_,_,_) >> [status:true]
        service.signalIntegrationApiService = mockSignalApiService
        when:
        service.sendErrorNotification(executedConfiguration,signalReportInfoInstance)

        then:
        1*service.signalIntegrationApiService.postData(_,_,_,_)
        notThrown(Exception)
    }


    void "test notifyExecutedCaseSeriesStatus method"(){
        given:
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: 1L, executionStatus: ReportExecutionStatusEnum.COMPLETED).save(validate:false)
        def mockSignalApiService = Mock(SignalIntegrationApiService)
        mockSignalApiService.postCallback(_,_,_) >> [status:true, result:[status: true]]
        service.signalIntegrationApiService = mockSignalApiService

        when:
        service.notifyExecutedCaseSeriesStatus(executionStatus).get()

        then:
        1 * service.signalIntegrationApiService.postCallback(_, _, _)
        notThrown(Exception)
    }

    void "test notifyExecutedConfigurationStatus method"(){
        given:
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: 1L, executionStatus: ReportExecutionStatusEnum.COMPLETED).save(validate:false)
        def mockSignalApiService = Mock(SignalIntegrationApiService)
        mockSignalApiService.postCallback(_,_,_) >> [status:true, result:[status: true]]
        service.signalIntegrationApiService = mockSignalApiService

        when:
        service.notifyExecutedConfigurationStatus(executionStatus).get()

        then:
        1 * service.signalIntegrationApiService.postCallback(_, _, _)
        notThrown(Exception)
    }

    void "test notifySignalForUpdate method"(){
        given:
        ReportTemplate reportTemplate = new ReportTemplate(name: "testTemplate").save(validate: false)
        def mockSignalApiService = new MockFor(SignalIntegrationApiService)
        mockSignalApiService.demand.postData(0..1){String baseUrl, String path, String data = "test", Method method -> true}
        service.signalIntegrationApiService = mockSignalApiService.proxyInstance()
        when:
        service.notifySignalForUpdate(reportTemplate)
        then:
        noExceptionThrown()
    }

    void "test saveSignalReportResult method with deleted cases"(){
        given:
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(owner: userInstance,excludeDeletedCases: true, excludeNonValidCases: true)
        executedConfiguration.save(validate: false)
        service.dynamicReportService = [createMultiTemplateReport: { obj, params ->
            return File.createTempFile("test","txt")
        }]
        def mockSignalApiService = Mock(SignalIntegrationApiService)
        mockSignalApiService.postData(_,_,_,_) >> [status:true]
        service.signalIntegrationApiService = mockSignalApiService

        when:
        service.saveSignalReportResult(executedConfiguration,signalReportInfoInstance)

        then:
        1*service.signalIntegrationApiService.postData(_,_,_,_)
        notThrown(Exception)
    }

    void "test sendErrorNotification method wih deleted cases"(){
        given:
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(reportName: 'Test Report',owner: userInstance,excludeDeletedCases: true, excludeNonValidCases: true)
        executedConfiguration.save(validate: false)
        def mockSignalApiService = Mock(SignalIntegrationApiService)
        mockSignalApiService.postData(_,_,_,_) >> [status:true]
        service.signalIntegrationApiService = mockSignalApiService
        when:
        service.sendErrorNotification(executedConfiguration,signalReportInfoInstance)

        then:
        1*service.signalIntegrationApiService.postData(_,_,_,_)
        notThrown(Exception)
    }

}
