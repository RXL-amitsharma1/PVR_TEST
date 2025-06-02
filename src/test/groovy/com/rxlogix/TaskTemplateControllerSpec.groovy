package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.enums.TaskTemplateTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ReportTask, User, ViewHelper])
class TaskTemplateControllerSpec extends Specification implements DataTest, ControllerUnitTest<TaskTemplateController> {

    public static final user = "unitTest"

    def setupSpec() {
        mockDomains TaskTemplate, ReportTask, Task, User, Role, UserRole, Tenant, PeriodicReportConfiguration, ActionItemCategory
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..1) { -> user }
        securityMock.demand.getAllowedSharedWithUsersForCurrentUser { -> [user] }
        securityMock.demand.getAllowedSharedWithGroupsForCurrentUser { -> [] }
        securityMock.demand.isCurrentUserAdmin(0..2) { false }
        return securityMock.proxyInstance()
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

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    def setup() {
    }

    def cleanup() {
    }

    void " Aggregate TaskTemplate can be created"() {
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)

        TaskTemplate resultInstance
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save(0..2) { theInstance -> resultInstance = theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()

        def taskTemplateServiceMock = new MockFor(TaskTemplate)
        taskTemplateServiceMock.demand.fetchReportTasksFromRequest { params -> [new ReportTask()] }
        controller.taskTemplateService = taskTemplateServiceMock.proxyInstance()

        when: "The TaskTemplate with no errors created"
        params.id = null
        params.name = "test"
        params.type = TaskTemplateTypeEnum.AGGREGATE_REPORTS.name()
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        controller.save()
        then: "The Aggregate TaskTemplate is created and page redirected"
        response.status == 302
        response.redirectedUrl == "/taskTemplate/index"
        resultInstance.type == TaskTemplateTypeEnum.AGGREGATE_REPORTS
        resultInstance.reportTasks.size() == 1
        resultInstance.name == "test"
    }

    void " Report Request TaskTemplate can be created"() {
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)

        TaskTemplate resultInstance
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { theInstance -> resultInstance = theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()

        when: "The TaskTemplate with no errors created"
        params.id = null
        params.name = "test"
        params.type = TaskTemplateTypeEnum.REPORT_REQUEST.name()
        params["tasks[0].baseDate"] = 'DUE_DATE'
        params["tasks[0].newObj"] = true
        params["tasks[0].taskName"] = "testtask"
        params["tasks[0].dueDate"] = "1"
        params["tasks[0].sign"] = "+"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        controller.save()
        then: "The Report Request TaskTemplate is created and page redirected"
        response.status == 302
        response.redirectedUrl == "/taskTemplate/index"
        resultInstance.type == TaskTemplateTypeEnum.REPORT_REQUEST
        resultInstance.tasks.size() > 0
        resultInstance.name == "test"
        resultInstance.tasks[0].taskName == "testtask"
        resultInstance.tasks[0].dueDate == 1
    }


    void " Report Request TaskTemplate can be updated"() {
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)

        TaskTemplate resultInstance
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { theInstance -> resultInstance = theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()

        TaskTemplate template = new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.REPORT_REQUEST, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)

        when:
        params.id = 1
        params.name = "test1"
        params.type = TaskTemplateTypeEnum.REPORT_REQUEST.name()
        params["tasks[0].baseDate"] = 'DUE_DATE'
        params["tasks[0].newObj"] = true
        params["tasks[0].taskName"] = "testtask"
        params["tasks[0].dueDate"] = "1"
        params["tasks[0].sign"] = "+"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        controller.update()
        then: "The Report Request TaskTemplate is updated and page redirected"
        response.status == 302
        response.redirectedUrl == "/taskTemplate/index"
        resultInstance.type == TaskTemplateTypeEnum.REPORT_REQUEST
        resultInstance.tasks.size() > 0
        resultInstance.name == "test1"
        resultInstance.tasks[0].taskName == "testtask"
        resultInstance.tasks[0].dueDate == 1
    }

    void "Aggregate TaskTemplate can be updated"() {
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)

        TaskTemplate resultInstance
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save(0..2) { theInstance -> resultInstance = theInstance; return  theInstance}
        crudServiceMock.demand.update(0..2) { theInstance -> resultInstance = theInstance; return  theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()

        def taskTemplateServiceMock = new MockFor(TaskTemplate)
        taskTemplateServiceMock.demand.fetchReportTasksFromRequest { params -> [new ReportTask()] }
        controller.taskTemplateService = taskTemplateServiceMock.proxyInstance()

        TaskTemplate template = new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.AGGREGATE_REPORTS, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)

        when:
        params.id = 1
        params.name = "test1"
        params.type = TaskTemplateTypeEnum.AGGREGATE_REPORTS.name()
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        controller.update()
        then: "Aggregate TaskTemplate is updated and page redirected"
        response.status == 302
        response.redirectedUrl == "/taskTemplate/index"
        resultInstance.reportTasks.size() == 1
        resultInstance.name == "test1"
    }


    void "test create action, it renders the create page"() {
        given:


        when: "Call create action."

        params.type = TaskTemplateTypeEnum.AGGREGATE_REPORTS.name()
        controller.create()

        then: "Renders the view page."
        view == '/taskTemplate/create'
        model.taskTemplateInstance != null
        model.taskTemplateInstance.type == TaskTemplateTypeEnum.AGGREGATE_REPORTS

    }

    void "test show action, it renders the show page"() {
        given:
        def adminUser = makeAdminUser()
        TaskTemplate template = new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.AGGREGATE_REPORTS, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)

        when: "Call show action."
        params.id = 1
        controller.show()

        then: "Renders the show page."
        model.taskTemplateInstance.id == 1
    }

    void "test delete action, it deletes the show page"() {
        given:
        def adminUser = makeAdminUser()
        TaskTemplate template = new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.AGGREGATE_REPORTS, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete { theInstance, name, String justification = null -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        when: "Call delete action."
        request.method = 'POST'
        params.id = 1
        controller.delete()

        then: "redirect to index page"
        response.status == 302
        response.redirectedUrl == "/taskTemplate/index"
        flash.message != null
    }

    void "test ajaxGetTasksForConfiguration method"() {
        given:
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "type" }
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "PERIODIC_REPORT").save(validate: false)
        ReportTask.metaClass.static.listTasksForReportConfiguration = { Long id ->
            new Object() {
                public List list() {
                    [new ReportTask(actionCategory: actionItemCategory, description: "description", dueDateShift: 1, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT)]
                }
            }
        }
        when: "Call ajaxGetTasksForConfiguration action."
        params.id = 1
        controller.ajaxGetTasksForConfiguration()
        def result = JSON.parse(response.text)
        then:
        response.status == 200
        result.data.size() == 1
        result.data[0].description == "description"

    }

    void "test ajaxGetReportTasksForTemplate method"() {
        given:
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "PERIODIC_REPORT").save(validate: false)
        ReportTask.metaClass.static.listTasksForReportTemplate = { Long id ->
            new Object() {
                public List list() {
                    [new ReportTask(actionCategory: actionItemCategory, description: "description", dueDateShift: 1, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT)]
                }
            }
        }
        when: "Call ajaxGetReportTasksForTemplate action."
        params.id = 1
        controller.ajaxGetReportTasksForTemplate()
        def result = JSON.parse(response.text)
        then:
        response.status == 200
        result.data.size() == 1
        result.data[0].description == "description"

    }


    void "test ajaxGetReportTemplates method"() {
        given:
        def adminUser = makeAdminUser()
        TaskTemplate template = new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.AGGREGATE_REPORTS, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(failOnError: true, validate: false, flush: true)
        template = new TaskTemplate(id: 2, name: "test2", type: TaskTemplateTypeEnum.AGGREGATE_REPORTS, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(failOnError: true, validate: false, flush: true)
        template = new TaskTemplate(id: 3, name: "test3", type: TaskTemplateTypeEnum.REPORT_REQUEST, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(failOnError: true, validate: false, flush: true)
        when: "Call ajaxGetReportTasksForTemplate action."

        controller.ajaxGetReportTemplates()
        def result = JSON.parse(response.text)
        then:
        response.status == 200
        result.data.size() == 2
        result.data[0].name == "test"

    }

    void "test list"(){
        given:
        def adminUser = makeAdminUser()
        TaskTemplate template = new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.REPORT_REQUEST, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)
        when:
        controller.list()
        then:
        response.status == 200
    }

    def "test edit"(){
        given:
        def adminUser = makeAdminUser()
        TaskTemplate template = new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.REPORT_REQUEST, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)
        when:
        params.id = 1L
        controller.edit()
        then:
        response.status == 200
    }

    def "test save"(){
        given:
        //def adminUser = makeAdminUser()
        TaskTemplate taskTemplateInstance = instance
        taskTemplateInstance.save(flush: true)
        def mockTaskTemplateService = Mock( TaskTemplateService )
        mockTaskTemplateService.fetchReportTasksFromRequest(_) >> {return []}
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.currentUser >>{adminUser}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return taskTemplateInstance}
        controller.userService=mockUserService
        controller.CRUDService = mockCRUDService
        controller.taskTemplateService=mockTaskTemplateService
        when:
        request.method = 'POST'
        params.id = 1L
        params.type = type
        controller.save()
        then:
        response.status == 302
        response.redirectedUrl == '/taskTemplate/index'
        where:
        type                      |instance
        'AGGREGATE_REPORTS'       |new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.AGGREGATE_REPORTS, owner: user, createdBy: "user", modifiedBy: "user")
        'REPORT_REQUEST'          |new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.REPORT_REQUEST, owner: user, createdBy: "user", modifiedBy: "user")
    }

    void "test save with validation exception"(){
        given:
        TaskTemplate taskTemplateInstance = instance
        taskTemplateInstance.save(flush: true)
        def mockTaskTemplateService = Mock( TaskTemplateService )
        mockTaskTemplateService.fetchReportTasksFromRequest(_) >> {return []}
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.currentUser >>{adminUser}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception", taskTemplateInstance.errors)}
        controller.userService=mockUserService
        controller.CRUDService = mockCRUDService
        controller.taskTemplateService=mockTaskTemplateService
        when:
        request.method = 'POST'
        params.id = 1L
        params.type = type
        controller.save()
        then:
        response.status == 200
        where:
        type                      |instance
        'AGGREGATE_REPORTS'       |new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.AGGREGATE_REPORTS, owner: user, createdBy: "user", modifiedBy: "user")
        'REPORT_REQUEST'          |new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.REPORT_REQUEST, owner: user, createdBy: "user", modifiedBy: "user")
    }

    void "test save exception error"(){
        given:
        TaskTemplate taskTemplateInstance = instance
        taskTemplateInstance.save(flush: true)
        def mockTaskTemplateService = Mock( TaskTemplateService )
        mockTaskTemplateService.fetchReportTasksFromRequest(_) >> {return []}
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.currentUser >>{adminUser}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new Exception()}
        controller.userService=mockUserService
        controller.CRUDService = mockCRUDService
        controller.taskTemplateService=mockTaskTemplateService
        when:
        request.method = 'POST'
        params.id = 1L
        params.type = type
        controller.save()
        then:
        flash.error == 'app.label.task.template.save.exception'
        response.status == 200
        where:
        type                      |instance
        'AGGREGATE_REPORTS'       |new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.AGGREGATE_REPORTS, owner: user, createdBy: "user", modifiedBy: "user")
        'REPORT_REQUEST'          |new TaskTemplate(id: 1, name: "test", type: TaskTemplateTypeEnum.REPORT_REQUEST, owner: user, createdBy: "user", modifiedBy: "user")
    }


}