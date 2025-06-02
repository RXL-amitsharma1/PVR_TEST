package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, SubmissionAttachment, ExecutedPeriodicReportConfiguration, ExecutedIcsrReportConfiguration, ReportSubmission, PublisherReport, SubmissionAttachment])
class ReportSubmissionControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportSubmissionController> {

    def setupSpec() {
        mockDomains User, UserRole, UserGroup, Role, Tenant, Preference, ExecutedPeriodicReportConfiguration, ReportSubmission, ExecutedIcsrReportConfiguration, SubmissionAttachment, ReportSubmissionLateReason
    }

    private Tenant getTenant() {
        def tenant = Tenant.get(1L)
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = 1L
        return tenant.save()
    }

    private User makeNormalUser(name, team) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
        normalUser.metaClass.static.isDev = { -> return false}
        return normalUser
    }

    private makeSecurityServiceCurrentUser(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..3) { -> user }
        return securityMock.proxyInstance()
    }

    def "test index"(){
        when:
        controller.index()

        then:
        response.status == 200
    }

    def "test viewCases"(){
        when:
        ReportSubmission reportSubmission = new ReportSubmission(reportingDestination: "dest",submissionDate: new Date(),dueDate: new Date(),isPrimary: true)
        controller.viewCases(reportSubmission)
        then:
        response.status == 200
    }

    def "test downloadAttachment"() {
        given:
        SubmissionAttachment submissionAttachment = new SubmissionAttachment()
        SubmissionAttachment.metaClass.static.get = { Long id -> submissionAttachment }

        when:
        params.id = 1L
        controller.downloadAttachment()

        then:
        response.status == 200
    }

    def "test submitReport"(){
        given:
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(isPublisherReport: true)
        ExecutedPeriodicReportConfiguration.metaClass.static.get = {Long id -> executedPeriodicReportConfiguration}
        PublisherReport publisherReport = new PublisherReport(name: 'publisherReport', data:"456".getBytes(), dateCreated: new Date())
        PublisherReport.metaClass.static.get = {Long publisherDocument -> publisherReport}
        def mockReportSubmissionService = Mock( ReportSubmissionService )
        mockReportSubmissionService.submitReport(_, _, _, _) >> {return [new ReportSubmission()]}
        controller.reportSubmissionService = mockReportSubmissionService
        controller.userService = makeSecurityServiceCurrentUser( makeNormalUser("user",[]))

        when:
        params.reportingDestinations = ["Dest"]
        params.publisherDocument = 1L
        params.scheduleDateJSON = '{"startDateTime":"2015-12-11T20:28+01:00"}'
        params.dueDate = "24-Mar-2022"
        def exPerConfId = 1L
        controller.submitReport(exPerConfId)
        then:

        response.status == 200
    }

    def "test submitIcsrReport --Success"(){
        given:
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(reportName: 'IcsrReportConfiguration', primaryReportingDestination: 'reporting@!destination')
        ExecutedIcsrReportConfiguration.metaClass.static.get = {Long exPerConfId -> executedIcsrReportConfiguration}
        def mockReportSubmissionService = Mock( ReportSubmissionService )
        mockReportSubmissionService.submitReport(_, _, _) >> {return [new ReportSubmission()]}
        controller.reportSubmissionService = mockReportSubmissionService

        when:
        params.reportingDestinations = ["primary","reporting","destination","secondar","reporting","destination"]
        controller.submitIcsrReport(1L)
        then:

        flash.message == "app.reportSubmission.submitted.report.successful"
        response.status == 200
    }

    def "test submitIcsrReport --ValidationException"(){
        given:
        def mockUserService = Mock( UserService )
        mockUserService.getCurrentUser() >> {return makeNormalUser('user', [])}
        controller.userService = mockUserService
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(reportName: 'IcsrReportConfiguration', primaryReportingDestination: 'reporting@!destination')
        ExecutedIcsrReportConfiguration.metaClass.static.get = {Long exPerConfId -> executedIcsrReportConfiguration}
        def mockCRUDService = Mock( CRUDService )
        controller.CRUDService = mockCRUDService
        def mockReportSubmissionService = Mock( ReportSubmissionService )
        mockReportSubmissionService.submitReport(_, _, _) >> {throw new ValidationException("Validation Exception", executedIcsrReportConfiguration.errors)}
        controller.reportSubmissionService = mockReportSubmissionService

        when:
        params.reportingDestinations = ["primary@!reporting@!destination", "secondary@!reporting@!destination"]
        controller.submitIcsrReport(1L)

        then:
        response.status == 500
    }

    def "test submitIcsrReport when instance does not exist"(){
        given:
        ExecutedIcsrReportConfiguration.metaClass.static.get = {Long exPerConfId -> null}
        when:
        controller.submitIcsrReport(1L)
        then:
        response.status == 500
    }

    def "test updateLate"(){
        given:
        ReportSubmissionLateReason lateinstance0ne = new ReportSubmissionLateReason()
        ReportSubmissionLateReason lateinstancetwo = new ReportSubmissionLateReason()
        ReportSubmission submission = new ReportSubmission(comment : 'testing')
        submission.addToLateReasons(lateinstance0ne)
        submission.addToLateReasons(lateinstancetwo)
        ReportSubmission.metaClass.static.get = {Long id -> submission}
        def mockUerService = Mock( UserService )
        mockUerService.setOwnershipAndModifier(_) >> {return new ReportSubmissionLateReason()}
        controller.userService = mockUerService
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.saveOrUpdate(_) >> {true}
        controller.CRUDService = mockCRUDService

        when:
        params.id = 1L
        params.reason = ['reason1', 'reason2']
        params.responsible= ['responsible1', 'responsible2']
        controller.updateLate()

        then:
        response.status == 302
        response.redirectedUrl == '/reportSubmission/index'
    }

    def "test processSubmit --Try Block when instance of publisherReport exists and submissions.size() is not equal to reportingDestinations.size()"(){
        given:
        ExecutedReportConfiguration executedConfiguration = new ExecutedPeriodicReportConfiguration(isPublisherReport: true)
        PublisherReport publisherReport = new PublisherReport(name: 'publisherReport', data:"456".getBytes(), dateCreated: new Date())
        PublisherReport.metaClass.static.get = {Long publisherDocument -> publisherReport}
        def mockReportSubmissionService = Mock( ReportSubmissionService )
        mockReportSubmissionService.submitReport(_, _, _, _) >> {return [new ReportSubmission()]}
        controller.reportSubmissionService = mockReportSubmissionService
        controller.userService = makeSecurityServiceCurrentUser( makeNormalUser("user",[]))

        when:
        params.reportingDestinations = ["primary@!reporting@!destination", "secondary@!reporting@!destination"]
        params.publisherDocument = 1L
        params.scheduleDateJSON = '{"startDateTime":"2015-12-11T20:28+01:00"}'
        params.dueDate = "24-Mar-2022"
        controller.processSubmit(executedConfiguration)

        then:
        flash.message == "app.reportSubmission.submitted.warn"
        response.status == 200
    }

    def "test processSubmit --Try Block when instance of publisherReport exists and submissions.size() is equal to reportingDestinations.size()"(){
        given:
        ExecutedReportConfiguration executedConfiguration = new ExecutedPeriodicReportConfiguration(isPublisherReport: true)
        PublisherReport publisherReport = new PublisherReport(name: 'publisherReport', data:"456".getBytes(), dateCreated: new Date())
        PublisherReport.metaClass.static.get = {Long publisherDocument -> publisherReport}
        def mockReportSubmissionService = Mock( ReportSubmissionService )
        mockReportSubmissionService.submitReport(_, _, _, _) >> {return [new ReportSubmission()]}
        controller.reportSubmissionService = mockReportSubmissionService
        controller.userService = makeSecurityServiceCurrentUser( makeNormalUser("user",[]))

        when:
        params.reportingDestinations = ["Dest"]
        params.publisherDocument = 1L
        params.scheduleDateJSON = '{"startDateTime":"2015-12-11T20:28+01:00"}'
        params.dueDate = "24-Mar-2022"
        controller.processSubmit(executedConfiguration)

        then:
        flash.message == "app.reportSubmission.submitted.report.successful"
        response.status == 200
    }

    def "test processSubmit --ValidationException"(){
        given:
        def mockUserService = Mock( UserService )
        mockUserService.getCurrentUser() >> {return makeNormalUser('user', [])}
        controller.userService = mockUserService
        ExecutedReportConfiguration executedConfiguration = new ExecutedPeriodicReportConfiguration(isPublisherReport: true)
        PublisherReport publisherReport = new PublisherReport(name: 'publisherReport', data:"456".getBytes(), dateCreated: new Date())
        PublisherReport.metaClass.static.get = {Long publisherDocument -> publisherReport}
        def mockReportSubmissionService = Mock( ReportSubmissionService )
        mockReportSubmissionService.submitReport(_, _, _, _) >> {throw new ValidationException("Validation Exception", executedConfiguration.errors)}
        controller.reportSubmissionService = mockReportSubmissionService

        when:
        params.reportingDestinations = ["Dest"]
        params.publisherDocument = 1L
        params.scheduleDateJSON = '{"startDateTime":"2015-12-11T20:28+01:00"}'
        params.dueDate = "24-Mar-2022"
        controller.processSubmit(executedConfiguration)

        then:
        response.json.error == true
        response.json.defaultMsg == 'default.system.error.message'
        response.status == 500
    }

    def "test processSubmit --Try Block when instance of publisherReport does not exist"(){
        given:
        def mockUserService = Mock( UserService )
        mockUserService.getCurrentUser() >> {return makeNormalUser('user', [])}
        controller.userService = mockUserService
        ExecutedReportConfiguration executedConfiguration = new ExecutedPeriodicReportConfiguration(isPublisherReport: true)
        PublisherReport publisherReport = new PublisherReport(name: 'publisherReport', data:"456".getBytes(), dateCreated: new Date())
        PublisherReport.metaClass.static.get = {Long publisherDocument -> null}

        when:
        params.reportingDestinations = ["Dest"]
        params.publisherDocument = 1L
        params.scheduleDateJSON = '{"startDateTime":"2015-12-11T20:28+01:00"}'
        params.dueDate = "24-Mar-2022"
        controller.processSubmit(executedConfiguration)

        then:
        response.json.error == true
        response.json.defaultMsg == 'default.system.error.message'
        response.status == 500
    }
}
