package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.ReportExecutorService
import com.rxlogix.SubmittedCaseDTO
import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.enums.ReportSubmissionStatusEnum
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import java.time.LocalDateTime

@ConfineMetaClassChanges([User, ReportSubmission, Capa8D])
class ReportSubmissionRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportSubmissionRestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, ReportSubmission, ExecutedReportConfiguration, ExecutedPeriodicReportConfiguration, ExecutedGlobalDateRangeInformation, Capa8D
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
        return normalUser
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

    void "test casesList"(){
        SubmittedCaseDTO submittedCaseDTO = new SubmittedCaseDTO(caseType: "caseType",productName: "productName",studyName: "studyName",caseNumber: "caseNumber",versionNumber: "versionNumber",eventPreferredTerm: "eventpreferredTerm",eventReceiptDate: new Date(),eventSequenceNumber: "eventSequenceNumber",eventSeriousness: "eventSeriousness")
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.getSubmittedCases(0..1){ ReportSubmission reportSubmission, Integer offset, Integer max, String sort, String direction, String searchString->
            return [1,2,[submittedCaseDTO]]
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params.sort = "dateCreated"
        params.max = 10
        params.offset = 0
        params.direction = ""
        controller.casesList(new ReportSubmission())
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 2
        response.json.aaData[0].size() == 9
    }

    void "test index"() {
        given:
        User normalUser = makeNormalUser("user", [])
        int run = 0

        def executedReportConfiguration = new ExecutedPeriodicReportConfiguration(
                executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(
                        dateRangeStartAbsolute: new Date(),
                        dateRangeEndAbsolute: new Date()
                ),
                periodicReportType: PeriodicReportTypeEnum.ADDENDUM,
                reportName: "report"
        ).save(failOnError: true, validate: false, flush: true)

        def reportSubmission = new ReportSubmission(
                reportingDestination: "dest",
                submissionDate: new Date(),
                dueDate: new Date(),
                isPrimary: true,
                executedReportConfiguration: executedReportConfiguration,
                modifiedBy: "user"
        ).save(failOnError: true, validate: false, flush: true)

        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return normalUser }
        controller.userService = mockUserService

        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.getSubmissionsByCaseNumber(0..1) { String caseNumber ->
            run++
            return [executedReportConfiguration.id]
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()

        // Mock fetchReportSubmissionBySearchString to avoid actual criteria evaluation
        ReportSubmission.metaClass.static.fetchReportSubmissionBySearchString = { LibraryFilter filter, ReportSubmissionStatusEnum status, List<Long> executedIdList, Boolean icsr, String sort = null, String order = null ->
            run++
            return [
                    list: { Map args = [:] ->
                        run++
                        return [[reportSubmission.id]]
                    }
            ] as Object
        }

        // Mock getAll to return expected objects
        ReportSubmission.metaClass.static.getAll = { List<Long> ids ->
            return [
                    new ReportSubmission(
                            id: ids[0],
                            comment: 'testComment1',
                            submissionDate: LocalDateTime.now(),
                            dueDate: LocalDateTime.now(),
                            createdBy: 'user',
                            modifiedBy: 'user',
                            reportingDestination: 'FDA',
                            late: "late",
                            isPrimary: true,
                            tenantId: 1L,
                            reportSubmissionStatus: ReportSubmissionStatusEnum.SUBMITTED,
                            periodicReportType: 'testPeriodicReportType',
                            executedReportConfiguration: executedReportConfiguration
                    )
            ]
        }

        Capa8D.metaClass.static.findBySubmission = { ReportSubmission sub ->
            return new Capa8D(id: 1L, issueNumber: "testIssueNumber", issueType: "testIssueType")
        }

        controller.metaClass.grailsApplication = [config: [submissions: [late: []]]]

        when:
        params.status = "SUBMITTED"
        params.sort = "reportName"
        params.caseSeriesSearch = "test"
        params.searchString = ""
        params.icsr = "false"
        params.max = 10
        params.offset = 0
        params.order = ""

        controller.index()

        then:
        run == 7
        response.json.aaData.size() == 1
        response.json.aaData[0].reportName == "report"
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1

        cleanup:
        GroovySystem.metaClassRegistry.removeMetaClass(ReportSubmission)
        GroovySystem.metaClassRegistry.removeMetaClass(Capa8D)
    }

}
