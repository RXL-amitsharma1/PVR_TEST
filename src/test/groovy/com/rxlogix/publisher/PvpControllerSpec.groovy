package com.rxlogix.publisher

import com.rxlogix.CRUDService
import com.rxlogix.GanttService
import com.rxlogix.OneDriveRestService
import com.rxlogix.UserService
import com.rxlogix.config.ExecutedDeliveryOption
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.Tenant
import com.rxlogix.config.WorkflowState
import com.rxlogix.config.publisher.*
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([WordTemplateExecutor, User, PublisherReport, PublisherConfigurationSection, PublisherExecutedTemplate, ExecutedPeriodicReportConfiguration, WorkflowState])
class PvpControllerSpec extends Specification implements DataTest, ControllerUnitTest<PvpController> {

    User normalUser

    def setup() {
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveOrUpdate(_) >> {}
        controller.CRUDService = mockCRUDService


        User normalUser = makeNormalUser("user", [])
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()

        def mockPublisherService = Mock(PublisherService)
        mockPublisherService.pullTheLastFullDocumentChanges(_) >> { [:] }
        mockPublisherService.pushTheLastFullDocumentChanges(_) >> { [:] }
        controller.publisherService = mockPublisherService

        controller.metaClass.pushTheLastSectionChanges = { a -> }
        controller.metaClass.pullTheLastSectionChanges = { a -> }
    }


    def cleanup() {
    }

    def setupSpec() {
        mockDomains PublisherConfigurationSection, PublisherTemplate, PublisherExecutedTemplate, User, Role, Tenant, UserRole, Preference, ExecutedPeriodicReportConfiguration, WorkflowState
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

    void "test updatePublisherTemplate"() {
        given:

        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        new PublisherTemplate().save(failOnError: true, validate: false)
        when:
        params.id = section.id
        params.publisherTemplateSectionParameterValue = """{templateId:1, parameterValues:{"one":"one","two":"two"}}"""
        controller.updatePublisherTemplate()
        then:
        section.parameterValues == [one: "one", two: "two"]
        section.publisherTemplate.id == 1L
        response.status == 302
        response.header("Location").contains("sections")
    }

    void "test updatePublisherTemplateAndGenerate"() {
        given:
        def publisherService = new MockFor(PublisherService)
        publisherService.demand.processSection(0..1) { PublisherConfigurationSection section, Boolean cntinue = false -> null }
        controller.publisherService = publisherService
        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        new PublisherTemplate().save(failOnError: true, validate: false)
        when:
        params.id = section.id
        params.publisherTemplateSectionParameterValue = """{templateId:1, parameterValues:{"one":"one","two":"two"}}"""
        controller.updatePublisherTemplateAndGenerate()
        then:
        section.parameterValues == [one: "one", two: "two"]
        response.status == 302
        response.header("Location").contains("sections")
    }

    void "test updateName"() {
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        when:
        params.sectionId = section.id
        params.value = "test"
        controller.updateName()
        then:
        section.name == "test"
        response.status == 200
        response.text == "ok"
    }

    void "test restoreDraft"() {
        given:


        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        PublisherExecutedTemplate toRestore = new PublisherExecutedTemplate(status: PublisherExecutedTemplate.Status.ARCHIVE,
                data: "123".getBytes(), executionStatus: PublisherExecutedTemplate.ExecutionStatus.SUCCESS, numOfExecution: 1,
                publisherConfigurationSection: section,
        ).save(failOnError: true, validate: false)
        PublisherExecutedTemplate current = new PublisherExecutedTemplate(status: PublisherExecutedTemplate.Status.DRAFT,
                data: "456".getBytes(), executionStatus: PublisherExecutedTemplate.ExecutionStatus.SUCCESS, numOfExecution: 2,
                publisherConfigurationSection: section,
        ).save(failOnError: true, validate: false)
        section.publisherExecutedTemplates = [toRestore, current]
        section.save(failOnError: true, validate: false)
        WordTemplateExecutor.metaClass.static.fetchParameters = { InputStream t -> [:] }

        when:
        params.id = toRestore.id
        controller.restoreDraft()
        then:
        current.status == PublisherExecutedTemplate.Status.ARCHIVE
        section.publisherExecutedTemplates.size() == 3
        response.text == "ok"
    }

    void "test fetchPendingParameters"() {
        given:


        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        PublisherExecutedTemplate current = new PublisherExecutedTemplate(status: PublisherExecutedTemplate.Status.DRAFT,
                data: "456".getBytes(), executionStatus: PublisherExecutedTemplate.ExecutionStatus.SUCCESS, numOfExecution: 2,
                publisherConfigurationSection: section,
        ).save(failOnError: true, validate: false)
        section.publisherExecutedTemplates = [current]
        section.save(failOnError: true, validate: false)
        controller.publisherService.updatePendingParameters(_) >> {
            Map parameters = [comment: [1], variable: [1, 2], manual: [1, 2, 3]]
            section.pendingComment = parameters.comment?.size() ?: 0
            section.pendingVariable = parameters.variable?.size() ?: 0
            section.pendingManual = parameters.manual?.size() ?: 0
            return parameters
        }
        when:
        params.id = section.id
        controller.fetchPendingParameters(section.id)
        then:
        section.pendingComment == 1
        section.pendingVariable == 2
        section.pendingManual == 3
        response.json.comment == [1]
        response.json.variable == [1, 2]
        response.json.manual == [1, 2, 3]
    }

    void "test saveParamsAndGenerateURL"() {
        given:
        controller.publisherService = [processSection: { PublisherConfigurationSection section, Boolean cntinue -> null }]
        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        new PublisherTemplate().save(failOnError: true, validate: false)
        when:
        params.id = section.id
        params.data_one = "one"
        params.data_two = "two"
        controller.saveParamsAndGenerateURL()
        then:
        section.parameterValues == [one: "one", two: "two"]
        response.text == "ok"
    }

    void "test changeSortOrder"() {
        given:
        PublisherConfigurationSection section1 = new PublisherConfigurationSection(sortNumber: 1).save(failOnError: true, validate: false)
        PublisherConfigurationSection section2 = new PublisherConfigurationSection(sortNumber: 2).save(failOnError: true, validate: false)

        when:
        params.id1 = section1.id
        params.id2 = section2.id
        controller.changeSortOrder()
        then:
        section1.sortNumber == 2
        section2.sortNumber == 1
        response.text == "ok"
    }

    void "test updateAssignedTo"() {
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        UserGroup ug = new UserGroup()
        when:
        params.sectionId = section.id
        params.id = ug.id
        controller.updateName()
        then:
        section.assignedToGroup == ug.id
        response.status == 200
        response.text == "ok"
    }

    void "test updateReviewer"() {
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        User u = new User()
        when:
        params.sectionId = section.id
        params.id = u.id
        controller.updateReviewer()
        then:
        section.reviewer == u.id
        response.status == 200
        response.text == "ok"
    }

    void "test updateApprover"() {
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        User u = new User()
        when:
        params.sectionId = section.id
        params.id = u.id
        controller.updateApprover()
        then:
        section.approver == u.id
        response.status == 200
        response.text == "ok"
    }

    void "test updateAuthor"() {
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        User u = new User()
        when:
        params.sectionId = section.id
        params.id = u.id
        controller.updateAuthor()
        then:
        section.author == u.id
        response.status == 200
        response.text == "ok"
    }

    void "test updateDestination"() {
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        User u = new User()
        when:
        params.sectionId = section.id
        params.value = "test"
        controller.updateDestination()
        then:
        section.destination == "test"
        response.status == 200
        response.text == "ok"
    }

    void "test updateDue"() {
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection().save(failOnError: true, validate: false)
        User u = new User()
        when:
        params.sectionId = section.id
        params.value = "11-Apr-2011"
        controller.updateDue()
        then:
        section.dueDate == Date.parse("dd-MMM-yyyy", "11-Apr-2011")
        response.status == 200
        response.text == "ok"
    }

    def "test updateComment"(){
        given:
        PublisherReport publisherReport = new PublisherReport(id:1L, name: "pvp",assignedToGroup: new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user'))
        PublisherReport.metaClass.static.get = {Long id -> publisherReport}

        when:
        params.sectionId = 1L
        params.value = "update Comment"
        controller.updateComment()

        then:
        response.status == 200
        response.text == "ok"
    }

    def "test dashboard"(){
        when:
        controller.dashboard()
        then:
        response.status == 200
    }

    def "test reports"(){
        when:
        controller.reports()
        then:
        response.status == 200
    }

    def "test removeSection"(){
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection(id:1L, name: "pvp")
        PublisherConfigurationSection.metaClass.static.get = {Long id ->  section}
        def mockOneDriveRestService = Mock(OneDriveRestService)
        mockOneDriveRestService.removeItem(_) >> {true}
        controller.oneDriveRestService = mockOneDriveRestService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.delete(_) >> {true}
        controller.CRUDService = mockCRUDService
        when:
        params.id = 1L
        controller.removeSection()
        then:
        response.status == 200
        response.text == "ok"
    }

    def "test index"(){
        when:
        controller.index()
        then:
        response.status == 200
        response.forwardedUrl == '/dashboard/index?pvp=true'
    }

    def "test distribute"(){
        given:
        PublisherReport publisherReport = new PublisherReport(id:1L, name: "pvp",assignedToGroup: new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user'), published: false)
        PublisherReport.metaClass.static.get = {Long id -> publisherReport}
        def mockOneDriveRestService = Mock(OneDriveRestService)
        mockOneDriveRestService.removeItem(_) >> {true}
        controller.oneDriveRestService = mockOneDriveRestService
        when:
        params.id = 1L
        controller.distribute()
        then:
        response.redirectedUrl == '/pvp/sections'
        response.status == 302
    }

    def "test removePublisherReport"(){
        given:
        PublisherReport publisherReport = new PublisherReport(id:1L, name: "pvp",assignedToGroup: new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user'), published: false, isDeleted: false)
        PublisherReport.metaClass.static.get = {Long id -> publisherReport}
        def mockOneDriveRestService = Mock(OneDriveRestService)
        mockOneDriveRestService.removeItem(_) >> {true}
        controller.oneDriveRestService = mockOneDriveRestService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_, _) >> {true}
        controller.CRUDService = mockCRUDService
        when:
        params.id = 1L
        controller.removePublisherReport()
        then:
        response.status == 200
    }

    def "test edit"(){
        when:
        controller.edit()
        then:
        response.status == 200
    }

    def "test setAsFinal"(){
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection(id:1L, name: "pvp",assignedToGroup: new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user'), pendingVariable : 0, pendingManual : 0)
        PublisherConfigurationSection.metaClass.static.get = {Long id -> section}
        PublisherExecutedTemplate current = new PublisherExecutedTemplate(status: PublisherExecutedTemplate.Status.DRAFT,
                data: "456".getBytes(), executionStatus: PublisherExecutedTemplate.ExecutionStatus.SUCCESS, numOfExecution: 2,
                publisherConfigurationSection: section,
        ).save(failOnError: true, validate: false)
        section.publisherExecutedTemplates = [current]
        section.save(failOnError: true, validate: false)
        def mockOneDriveRestService = Mock(OneDriveRestService)
        mockOneDriveRestService.removeItem(_) >> {true}
        controller.oneDriveRestService = mockOneDriveRestService
        when:
        params.sectionid = section.id
        controller.setAsFinal()
        then:
        response.status == 302
        response.redirectedUrl == '/pvp/sections'
    }

    def "test removeFinalStatus"(){
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection(id:1L, name: "pvp",assignedToGroup: new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user'), pendingVariable : 0, pendingManual : 0)
        PublisherExecutedTemplate executedTemplate = new PublisherExecutedTemplate(name: 'executedTemplate', status: PublisherExecutedTemplate.Status.DRAFT,
                data: "456".getBytes(), executionStatus: PublisherExecutedTemplate.ExecutionStatus.SUCCESS, numOfExecution: 2,
                publisherConfigurationSection: section,
        ).save(failOnError: true, validate: false)
        PublisherExecutedTemplate.metaClass.static.get = {Long id -> executedTemplate}
        when:
        controller.removeFinalStatus(1L)
        then:
        response.status == 302
        response.redirectedUrl == '/pvp/sections'
    }

    def "test listPublisherExecutedLogUrl"(){
        given:
        PublisherExecutedTemplate executedTemplate = new PublisherExecutedTemplate()
        PublisherExecutedTemplate.metaClass.static.get = {Long id -> executedTemplate}
        when:
        controller.listPublisherExecutedLogUrl(1L)
        then:
        response.status == 200
    }

    def "test listPublisherExecutedTemplates"(){
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection(id:1L, name: "pvp",assignedToGroup: new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user'), pendingVariable : 0, pendingManual : 0)
        PublisherConfigurationSection.metaClass.static.get = {Long id -> section}
        PublisherExecutedTemplate publisherExecutedTemplate = new PublisherExecutedTemplate(id: 1L, name: "publisherExecutedTemplate", status: PublisherExecutedTemplate.Status.DRAFT,
                data: "456".getBytes(), executionStatus: PublisherExecutedTemplate.ExecutionStatus.SUCCESS, numOfExecution: 2,
                publisherConfigurationSection: section,
        ).save(failOnError: true, validate: false)
        section.publisherExecutedTemplates = [publisherExecutedTemplate]
        section.save(failOnError: true, validate: false)
        when:
        controller.listPublisherExecutedTemplates(1L)
        then:
        response.status == 200
        response.json.id[0] == 1
        response.json.status[0] == 'DRAFT'
        response.json.name[0] == 'publisherExecutedTemplate'
    }

    def "test generateAllDraft"(){
        given:
        PublisherConfigurationSection sections = new PublisherConfigurationSection(publisherTemplate : new PublisherTemplate(name : 'PublisherTemplate', isDeleted : false)).save(failOnError: true, validate: false)
        PublisherConfigurationSection.metaClass.static.get = {Long id -> sections}
        def mockPublisherService = Mock( PublisherService )
        mockPublisherService.processSection(_, _) >> { PublisherConfigurationSection section, Boolean cntinue = false -> null }
        controller.publisherService = mockPublisherService
        when:
        controller.generateAllDraft(1L, "1,2,3")
        then:
        response.status == 302
        response.redirectedUrl == '/pvp/sections'
    }

    def "test generate"(){
        given:
        def mockPublisherService = Mock( PublisherService )
        mockPublisherService.processSection(_, _) >> { PublisherConfigurationSection section, Boolean cntinue = false -> true }
        controller.publisherService = mockPublisherService
        PublisherConfigurationSection sections = new PublisherConfigurationSection(publisherTemplate : new PublisherTemplate(name : 'PublisherTemplate', isDeleted : false)).save(failOnError: true, validate: false)
        PublisherConfigurationSection.metaClass.static.get = {Long id -> sections}
        when:
        controller.generate(1L)
        then:
        response.status == 302
        response.redirectedUrl == '/pvp/sections'
    }

    def "test downloadPublisherExecutedTemplate"(){
        given:
        PublisherConfigurationSection section = new PublisherConfigurationSection(id:1L, name: "pvp",assignedToGroup: new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user'), pendingVariable : 0, pendingManual : 0)
        PublisherExecutedTemplate executedTemplate = new PublisherExecutedTemplate(name: 'executedTemplate', status: PublisherExecutedTemplate.Status.DRAFT,
                data: "456".getBytes(), executionStatus: PublisherExecutedTemplate.ExecutionStatus.SUCCESS, numOfExecution: 2,
                publisherConfigurationSection: section, fileName : 'docx'
        )
        PublisherExecutedTemplate.metaClass.static.get = {Long id -> executedTemplate}
        when:
        controller.downloadPublisherExecutedTemplate(1L)
        then:
        response.status == 200
    }

    def "test downloadPublisherReport"(){
        given:
        PublisherReport publisherReport = new PublisherReport(id:1L, name: "publisherReport",assignedToGroup: new UserGroup(name: "group",createdBy:'user',modifiedBy:'user'), published: false, isDeleted: false, data: "456".getBytes())
        PublisherReport.metaClass.static.get = {Long id -> publisherReport}
        when:
        controller.downloadPublisherReport(1L)
        then:
        response.status == 200
    }

    def "test getSectionsInfoList"(){
        given:
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(id : 1L, executedDeliveryOption : new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF], sharedWith : [new User()], sharedWithGroup : [new UserGroup()])).save(failOnError: true, validate: false)
        ExecutedPeriodicReportConfiguration.metaClass.static.get = {Long id -> executedPeriodicReportConfiguration}
        when:
        params.id = 1L
        controller.getSectionsInfoList()
        then:
        response.status == 200
    }

    def "test addSection"(){
        given:
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(reportId : 2L, gantt : new Gantt(isDeleted : false), publisherConfigurationSections : new PublisherConfigurationSection(), executedDeliveryOption : new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF]), includePreviousMissingCases : false).save(failOnError: true, validate: false)
        ExecutedPeriodicReportConfiguration.metaClass.static.get = {Long reportId -> executedPeriodicReportConfiguration}
        def mockGanttService = Mock( GanttService )
        WorkflowState.metaClass.static.findByNameAndIsDeleted = {-> new WorkflowState(isDeleted : false)}
        mockGanttService.createSectionStage(_, _, _, _, _, _) >> {return true}
        controller.ganttService = mockGanttService
        when:
        params.reportId = 2L
        controller.addSection()
        then:
        response.status == 302
        response.redirectedUrl == '/pvp/sections/2'
    }
}
