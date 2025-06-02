package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.publisher.Gantt
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.publisher.GanttController
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, ExecutedReportConfiguration, Gantt])
class GanttControllerSpec extends Specification implements DataTest, ControllerUnitTest<GanttController> {

    public static final user = "unitTest"

    def setupSpec() {
        mockDomains Gantt, WorkflowState, WorkflowRule, User, Role, Preference, Tenant, UserRole, ExecutedCaseSeries, ExecutedPeriodicReportConfiguration
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

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def userRole = new Role(authority: 'ROLE_PVQ_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    def "test index"(){
        when:
        controller.index()
        then:
        response.status == 200
    }

    def "test list"(){
        given:
        Gantt ganttInstance = new Gantt(name: "PvpTemplate", isDeleted: false, isTemplate: true, defaultAiDuration: 2, defaultSubmissionDuration: 2, defaultReportDuration: 3, defaultSectionDuration: 3, defaultFullDuration: 3)
        ganttInstance.save(failOnError:true,validate:false)

        when:
        controller.list()

        then:
        response.status == 200
        response.json.aaData[0].name == 'PvpTemplate'
    }

    def "test create"(){
        given:
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        User normalUser=makeNormalUser()
        WorkflowState initialStateofworkflow=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        initialStateofworkflow.save(failOnError:true)
        WorkflowState targetStateofworkflow=new WorkflowState(name:'test_2',createdBy:'user',modifiedBy:'user')
        targetStateofworkflow.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST,initialState: initialStateofworkflow,targetState: targetStateofworkflow,
                executors: [normalUser],executorGroups:[userGroup])
        workflowRuleInstance.save(failOnError:true,validate: false)

        when:
        Gantt ganttInstance = new Gantt(name: "PvpTemplate", isDeleted: false, isTemplate: true, defaultAiDuration: 2, defaultSubmissionDuration: 2, defaultReportDuration: 3, defaultSectionDuration: 3, defaultFullDuration: 3)
        controller.create(ganttInstance)

        then:
        response.status == 200
    }

    def "test save"(){
        given:
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.save(_) >> {return true}
        controller.CRUDService = mockCRUDService
        def mockUserService = Mock( UserService )
        controller.userService=mockUserService

        when:
        request.method = 'POST'
        Gantt ganttInstance = new Gantt(name: "PvpTemplate", isDeleted: false, isTemplate: true, defaultAiDuration: 2, defaultSubmissionDuration: 2, defaultReportDuration: 3, defaultSectionDuration: 3, defaultFullDuration: 3)
        controller.save(ganttInstance)

        then:
        flash.message == 'default.created.message'
        response.status == 302
        response.redirectedUrl == '/gantt/index'
    }

    def "test save, When ValidationException"(){
        given:
        Gantt ganttInstance = new Gantt(name: "PvpTemplate", isDeleted: false, isTemplate: true, defaultAiDuration: 2, defaultSubmissionDuration: 2, defaultReportDuration: 3, defaultSectionDuration: 3, defaultFullDuration: 3)
        ganttInstance.save(failOnError:true)
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception",ganttInstance.errors)}
        controller.CRUDService = mockCRUDService
        def mockUserService = Mock( UserService )
        controller.userService=mockUserService

        when:
        request.method = 'POST'
        controller.save(ganttInstance)

        then:
        response.status == 200
    }

    def "test edit"(){
        given:
        Gantt ganttInstance = new Gantt(name: "PvpTemplate", isDeleted: false, isTemplate: true, defaultAiDuration: 2, defaultSubmissionDuration: 2, defaultReportDuration: 3, defaultSectionDuration: 3, defaultFullDuration: 3)
        Gantt.metaClass.static.read = {Long id -> ganttInstance}

        when:
        controller.edit(1L)

        then:
        response.status == 200
    }

    def "test edit when instance does not found"(){
        given:
        Gantt.metaClass.static.read = {Long id -> null}

        when:
        controller.edit(1L)

        then:
        response.status == 302
        response.redirectedUrl == '/gantt/index'
    }

    def "test update"(){
        given:
        Gantt ganttInstance = new Gantt(name: "PvpTemplate", isDeleted: false, isTemplate: true, defaultAiDuration: 2, defaultSubmissionDuration: 2, defaultReportDuration: 3, defaultSectionDuration: 3, defaultFullDuration: 3)
        Gantt.metaClass.static.get = {Long id -> ganttInstance}
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.update(_) >> {return true}
        controller.CRUDService = mockCRUDService

        when:
        request.method = 'POST'
        params.defaultSubmissionDuration = 4
        params.defaultAiDuration = 4
        params.defaultReportDuration = 4
        params.defaultSectionDuration = 4
        params.defaultFullDuration = 4
        params.name = "PVPTemplate"
        controller.update(1L)

        then:
        flash.message == 'default.updated.message'
        response.status == 302
        response.redirectedUrl == '/gantt/index'
    }

    def "test update try failure"(){
        given:
        Gantt ganttInstance = new Gantt(name: "PvpTemplate", isDeleted: false, isTemplate: true, defaultAiDuration: 2, defaultSubmissionDuration: 2, defaultReportDuration: 3, defaultSectionDuration: 3, defaultFullDuration: 3)
        Gantt.metaClass.static.get = {Long id -> ganttInstance}
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception",ganttInstance.errors)}
        controller.CRUDService = mockCRUDService

        when:
        request.method = 'POST'
        params.defaultSubmissionDuration = 4
        params.defaultAiDuration = 4
        params.defaultReportDuration = 4
        params.defaultSectionDuration = 4
        params.defaultFullDuration = 4
        params.name = "PVPTemplate"
        controller.update(1L)

        then:
        response.status == 200
    }

    def "test gantt"(){
        when:
        controller.gantt()

        then:
        response.status == 200
    }

    def "test singleGanttAjax"(){
        given:
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName")
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(id:1L, caseSeries: executedCaseSeries,cumulativeCaseSeries: executedCaseSeries,status: ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT)
        ExecutedReportConfiguration.metaClass.static.get = {Long id -> executedReportConfiguration}
        def mockGanttService = Mock( GanttService )
        controller.ganttService = mockGanttService
        mockGanttService.getGanttForExecutedConfiguration(_) >> {return []}

        when:
        params.id == 1L
        controller.singleGanttAjax()

        then:
        response.status == 200
    }

    def "test show"(){
        given:
        Gantt ganttInstance = new Gantt(name: "PvpTemplate", isDeleted: false, isTemplate: true, defaultAiDuration: 2, defaultSubmissionDuration: 2, defaultReportDuration: 3, defaultSectionDuration: 3, defaultFullDuration: 3)
        Gantt.metaClass.static.read = {Long id -> ganttInstance}

        when:
        controller.show(1L)

        then:
        response.status == 200
    }

    def "test show when instance does not exist"(){
        given:
        Gantt.metaClass.static.read = {Long id -> null}

        when:
        controller.show(2L)

        then:
        response.status == 302
        response.redirectedUrl == '/gantt/index'
    }

    def "test delete"(){
        given:
        Gantt ganttInstance = new Gantt(name: "PvpTemplate", isDeleted: false, isTemplate: true, defaultAiDuration: 2, defaultSubmissionDuration: 2, defaultReportDuration: 3, defaultSectionDuration: 3, defaultFullDuration: 3)
        Gantt.metaClass.static.read = {Long id -> ganttInstance}
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.softDelete(_, _, _) >> {true}
        controller.CRUDService = mockCRUDService
        def mockUserService = Mock( UserService )
        controller.userService = mockUserService

        when:
        params.deleteJustification = 'description'
        controller.delete(1L)

        then:
        response.status == 302
        response.redirectedUrl == '/gantt/index'
    }

    def "test delete Validation Exception"(){
        given:
        Gantt ganttInstance = new Gantt(name: "PvpTemplate", isDeleted: false, isTemplate: true, defaultAiDuration: 2, defaultSubmissionDuration: 2, defaultReportDuration: 3, defaultSectionDuration: 3, defaultFullDuration: 3)
        Gantt.metaClass.static.read = {Long id -> ganttInstance}
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.softDelete(_, _, _) >> {throw new ValidationException("Validation Exception",ganttInstance.errors)}
        controller.CRUDService = mockCRUDService
        def mockUserService = Mock( UserService )
        controller.userService = mockUserService

        when:
        params.deleteJustification = 'description'
        controller.delete(1L)
        then:

        response.status == 302
        response.redirectedUrl == '/gantt/index'
    }

    def "test delete when instance does not exist"(){
        given:
        Gantt.metaClass.static.get = {Long id -> null}

        when:
        controller.delete(2L)

        then:
        response.status == 302
        response.redirectedUrl == '/gantt/index'
    }

}
