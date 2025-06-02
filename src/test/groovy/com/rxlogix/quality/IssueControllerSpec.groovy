package com.rxlogix.quality

import com.rxlogix.CRUDService
import com.rxlogix.EmailService
import com.rxlogix.QualityService
import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.customException.CustomJasperException
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, User, Capa8D, DrilldownCLLMetadata])
class IssueControllerSpec extends Specification implements DataTest, ControllerUnitTest<IssueController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, Role, Tenant, UserRole, Preference, Capa8D, ActionItem, DrilldownCLLMetadata, WorkflowState
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

    void "test create"() {
        given:
        User normalUser = makeNormalUser("user", [])
        Capa8D.metaClass.static.findByIssueNumberAndOwnerType = { String issueNumber, String type ->
            return null
        }
        def mockUserService = Mock(UserService)
        mockUserService.getActiveUsers() >> { return [normalUser] }
        controller.userService = mockUserService
        when:
        controller.create()
        then:
        response.status == 200
    }

    void "test index"() {
        when:
        controller.index()
        then:
        response.status == 200

    }

    void "test view"() {
        given:
        User normalUser = makeNormalUser("user", [])
        def mockQualityService = Mock(QualityService)
        mockQualityService.getCaseNoByIssueId(_, _) >> { return [] }
        controller.qualityService = mockQualityService
        def mockUserService = Mock(UserService)
        mockUserService.getActiveUsers() >> { return [normalUser] }
        controller.userService = mockUserService
        Tenants.metaClass.static.currentId = { -> return 1 }
        when:
        params.id = 1L
        controller.view()
        then:
        response.status == 200
    }

    def "test delete success"() {
        given:
        Capa8D capa8DInstance = new Capa8D(correctiveActions: new ActionItem(), preventiveActions: new ActionItem())
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_, _, _) >> { theInstance, name, String justification -> true }
        controller.CRUDService = mockCRUDService
        when:
        controller.delete(capa8DInstance)
        then:
        response.status == 302
        response.redirectedUrl == '/issue/index'
    }

    def "test delete validation exception"() {
        given:
        Capa8D capa8DInstance = new Capa8D(correctiveActions: new ActionItem(), preventiveActions: new ActionItem())
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_, _, _) >> { theInstance, name, String justification -> throw new ValidationException("Validation Exception", theInstance.errors) }
        controller.CRUDService = mockCRUDService
        when:
        controller.delete(capa8DInstance)
        then:
        response.status == 302
        response.redirectedUrl == '/issue/view'
    }

    def "test validateAndCreate --Success"() {
        given:
        Capa8D capa8DInstance = new Capa8D()
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> { return capa8DInstance }
        controller.CRUDService = mockCRUDService

        when:
        params.ownerType = "PVQ"
        controller.validateAndCreate(capa8DInstance)
        then:
        response.status == 302
        response.redirectedUrl == '/issue/edit'
    }

    def "test validateAndCreate --Validation Exeception"() {
        given:
        User normalUser = makeNormalUser("user", [])
        def mockUserService = Mock(UserService)
        mockUserService.getActiveUsers() >> { return [normalUser] }
        controller.userService = mockUserService
        Capa8D capa8DInstance = new Capa8D()
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> { throw new ValidationException("Validation Exception", capa8DInstance.errors) }
        controller.CRUDService = mockCRUDService
        when:
        params.ownerType = "PVQ"
        controller.validateAndCreate(capa8DInstance)
        then:
        response.status == 200
    }

    def "test validateAndCreate --Exeception"() {
        given:
        User normalUser = makeNormalUser("user", [])
        Capa8D capa8DInstance = new Capa8D()
        def mockUserService = Mock(UserService)
        mockUserService.getActiveUsers() >> { return [normalUser] }
        controller.userService = mockUserService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> { throw new Exception() }
        controller.CRUDService = mockCRUDService
        when:
        params.ownerType = "PVQ"
        controller.validateAndCreate(capa8DInstance)

        then:
        response.status == 302
        response.redirectedUrl == '/issue/create'
    }

    def "test edit"() {
        given:
        User normalUser = makeNormalUser("user", [])
        def mockUserService = Mock(UserService)
        mockUserService.getActiveUsers() >> { return [normalUser] }
        controller.userService = mockUserService
        when:
        controller.edit()
        then:
        response.status == 200
    }

    void "test email for Exception"() {
        given:
        Capa8D capa = new Capa8D().save(flush: true, failOnError: true, validate: false)
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.emailReportTo(0..1) { Capa8D testCapa, List<String> testEmailList, List<ReportFormatEnum> testFormats ->
            throw new CustomJasperException("Exception thrown during emailing Report")
        }

        controller.emailService = mockEmailService.proxyInstance()

        when:
        params.id = capa.id
        params.attachmentFormats = ["PDF", "XML"]
        params.emailToUsers = ["abc@gmail.com"]
        params.emailConfiguration = [subject: "new_email", body: "new_body"]
        controller.email()

        then:
        thrown(Exception)
    }

    void "test createCapaForReasonOfDelay -- success"() {
        given:
        Capa8D.metaClass.static.findByIssueNumberAndOwnerTypeAndIsDeleted = { String issueNumber, String ownerType, boolean isDeleted ->
            return null
        }
        DrilldownCLLMetadata drilldownCLLMetadata = new DrilldownCLLMetadata(caseId: 8459629L, processedReportId: '804044', tenantId: 1L, dueDate: new Date(), workflowStateUpdatedDate: new Date(),lastUpdatedIssue: existingIssues)
        drilldownCLLMetadata.save(failOnError:true , validate:false)
        DrilldownCLLMetadata.metaClass.static.getMetadataRecord = { Map params ->
            return drilldownCLLMetadata
        }

        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveOrUpdate(_) >> { return true }
        controller.CRUDService = mockCRUDService
        DrilldownCLLMetadata.metaClass.static.executeQuery = { String hql, Map m ->
            return [drilldownCLLMetadata.id]
        }
        when:
        params.issueNumber = 'NEWISSUE123'
        params.masterCaseId = '8459629'
        params.processedReportId = '804044'
        params.tenantId = '1'
        params.senderId = '-1'
        params.selectedIds = '[{"caseId":"8422828","tenantId":"1","processedReportId":"804060","cllRowId":"52880","caseNum":"00US0002641", "senderId":"-1", "versionNum":"7","commentExist":false},{"caseId":"8459629","tenantId":"1","processedReportId":"804044","cllRowId":"52914","caseNum":"00US00039390", "senderId":"-1", "versionNum":"2","commentExist":false}]'

        controller.createCapaForReasonOfDelay()

        then:
        response.status == 200
        drilldownCLLMetadata.lastUpdatedIssue == expectedIssueOrder

        where:
        existingIssues        | expectedIssueOrder
        null                 | 'NEWISSUE123'
        'ISSUEW123'          | 'NEWISSUE123,ISSUEW123'
        'ISSUEW123,OLD456'   | 'NEWISSUE123,ISSUEW123,OLD456'
    }

    void "test createCapaForReasonOfDelay -- failure"() {
        given:
        Capa8D.metaClass.static.findByIssueNumberAndOwnerType = { String issueNumber, String ownerType ->
            return null
        }
        DrilldownCLLMetadata drilldownCLLMetadata = new DrilldownCLLMetadata(caseId: 8459629L, processedReportId: '804044', tenantId: 1L, dueDate: new Date(), workflowStateUpdatedDate: new Date(),lastUpdatedIssue: existingIssues)
        drilldownCLLMetadata.save(failOnError:true , validate:false)
        DrilldownCLLMetadata.metaClass.static.getMetadataRecord = { Map params ->
            return drilldownCLLMetadata
        }
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveOrUpdate(_) >> {throw new Exception()}
        mockCRUDService.update(_) >> {}
        controller.CRUDService = mockCRUDService

        when:
        params.issueNumber = '20CWPK2MAK'
        params.masterCaseId = '8459629'
        params.processedReportId = '804044'
        params.tenantId = '1'
        params.senderId = '-1'
        params.selectedIds = '[{"caseId":"8422828","tenantId":"1","processedReportId":"804060","cllRowId":"52880","caseNum":"00US0002641", "senderId":"-1", "versionNum":"7","commentExist":false},{"caseId":"8459629","tenantId":"1","processedReportId":"804044","cllRowId":"52914","caseNum":"00US00039390", "senderId":"-1", "versionNum":"2","commentExist":false}]'

        controller.createCapaForReasonOfDelay()

        then:
        response.status == 500
        drilldownCLLMetadata.lastUpdatedIssue == expectedIssueOrder

        where:
        existingIssues        | expectedIssueOrder
         null                 | null
         'ISSUEW123'          | 'ISSUEW123'
         'ISSUEW123,OLD456'   | 'ISSUEW123,OLD456'
    }

    void "test getRODCapaDescription"() {
        when:
        Holders.config.report.empty.label="(empty)"
        params.selectedIds = '[{"caseId":"8422828","tenantId":"1","processedReportId":"804060","cllRowId":"52880","caseNum":"00US0002641","versionNum":"7","commentExist":false, "issueDueDate": "2021-06-08T00:00:00+0000", "issueSubmissionDate": "", "issueResponsibleParty": "", "issueRootCause": "", "issueReportingDest": "FDA"},' +
                '{"caseId":"8459629","tenantId":"1","processedReportId":"804044","cllRowId":"52914","caseNum":"00US00039390","versionNum":"2","commentExist":false, "issueDueDate": "2000-12-25T00:00:00+0000", "issueSubmissionDate": "", "issueResponsibleParty": "", "issueRootCause": "", "issueReportingDest": "EMA"}]'
        controller.getRODCapaDescription()

        then:
        response.status == 200
        response.text == 'rod.capa.reporting.destination : FDA ; rod.capa.case.Numbers : [00US0002641] ; rod.capa.due.date : [08-Jun-2021] ; rod.capa.submission.date : [(empty)] ; rod.capa.root.cause : [(empty)] ; rod.capa.responsible.party : [(empty)]\n' +
                'rod.capa.reporting.destination : EMA ; rod.capa.case.Numbers : [00US00039390] ; rod.capa.due.date : [25-Dec-2000] ; rod.capa.submission.date : [(empty)] ; rod.capa.root.cause : [(empty)] ; rod.capa.responsible.party : [(empty)]'
    }
}