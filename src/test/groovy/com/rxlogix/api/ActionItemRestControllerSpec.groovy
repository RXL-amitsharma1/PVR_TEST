package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.user.*
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, User, ActionItem, QualityCaseData, QualitySubmission, QualitySampling])
class ActionItemRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<ActionItemRestController> {

    def setup(){
    }

    def cleanup(){
    }

    def setupSpec() {
        mockDomains ActionItem,User, UserGroup, UserGroupUser, Role, UserRole, Tenant,Preference, DrilldownCLLData, ActionItemCategory
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

    void "test index"() {
        User normalUser = makeNormalUser("user", [])
        ActionItem actionItem = new ActionItem(description: "description",actionCategory: new ActionItemCategory(key: "key",name: "category"),assignedTo: normalUser,completionDate: new Date(),dueDate: new Date(),priority: "priority",status: StatusEnum.IN_PROGRESS,createdBy: "user",modifiedBy: "user")
        actionItem.completionDate = new Date()
        actionItem.dueDate = new Date()
        actionItem.dateCreated = new Date()
        actionItem.save(failOnError: true, validate: false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        ActionItem.metaClass.static.fetchActionItemsBySearchString = { LibraryFilter filter, String filterType, User user, Long executedReportId, Long sectionId, Long publisherId, Boolean pvq = false, String sortBy = null, String sortDirection = "asc" -> new Object() {
            List list(Object o){
                return [[actionItem.id]]
            }
            int count() {
                return 1
            }
            }
        }
        when:
        //assignedTo.id=1L
        params.searchString = ""
        params.max = 10
        params.offset = 0
        params.order = ""
        params.executedReportId = 1
        params.pvq = "false"
        controller.index("")
        then:
        response.json.aaData[0].size() == 16
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test indexPvq CASE_QUALITY"(){
        User normalUser = makeNormalUser("user", [])
        ActionItem actionItem = new ActionItem(description: "description",actionCategory: new ActionItemCategory(key: "key",name: "category"),assignedTo: normalUser,completionDate: new Date(),dueDate: new Date(),priority: "priority",status: StatusEnum.IN_PROGRESS,createdBy: "user",modifiedBy: "user")
        actionItem.completionDate = new Date()
        actionItem.dueDate = new Date()
        actionItem.dateCreated = new Date()
        actionItem.save(failOnError: true, validate: false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        QualityCaseData.metaClass.static.getActionItemIds = {Map params, Long tenantId -> return [actionItem.id]}
        Tenants.metaClass.static.currentId = { -> return 0 }
        when:
        params.dataType = "CASE_QUALITY"
        controller.indexPvq()
        then:
        response.json.aaData[0].size() == 16
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test indexPvq SUBMISSION_QUALITY"(){
        User normalUser = makeNormalUser("user", [])
        ActionItem actionItem = new ActionItem(description: "description",actionCategory: new ActionItemCategory(key: "key",name: "category"),assignedTo: normalUser,completionDate: new Date(),dueDate: new Date(),priority: "priority",status: StatusEnum.IN_PROGRESS,createdBy: "user",modifiedBy: "user")
        actionItem.completionDate = new Date()
        actionItem.dueDate = new Date()
        actionItem.dateCreated = new Date()
        actionItem.save(failOnError: true, validate: false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        QualitySubmission.metaClass.static.getActionItemIds = { Map params, Long tenantId -> return [actionItem.id]}
        Tenants.metaClass.static.currentId = { -> return 0 }
        when:
        params.dataType = "SUBMISSION_QUALITY"
        controller.indexPvq()
        then:
        response.json.aaData[0].size() == 16
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test indexPvq SAMPLING"(){
        User normalUser = makeNormalUser("user", [])
        ActionItem actionItem = new ActionItem(description: "description",actionCategory: new ActionItemCategory(key: "key",name: "category"),assignedTo: normalUser,completionDate: new Date(),dueDate: new Date(),priority: "priority",status: StatusEnum.IN_PROGRESS,createdBy: "user",modifiedBy: "user")
        actionItem.completionDate = new Date()
        actionItem.dueDate = new Date()
        actionItem.dateCreated = new Date()
        actionItem.save(failOnError: true, validate: false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        QualitySampling.metaClass.static.getActionItemIds = { Map params, Long tenantId, String pvqTypeEnum -> return [actionItem.id]}
        Tenants.metaClass.static.currentId = { -> return 0 }
        when:
        params.dataType = "SAMPLING"
        controller.indexPvq()
        then:
        response.json.aaData[0].size() == 16
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test indexPvq CASE_CORRECTIONS"(){
        User normalUser = makeNormalUser("user", [])
        ActionItem actionItem = new ActionItem(description: "description",actionCategory: new ActionItemCategory(key: "key",name: "category"),assignedTo: normalUser,completionDate: new Date(),dueDate: new Date(),priority: "priority",status: StatusEnum.IN_PROGRESS,createdBy: "user",modifiedBy: "user")
        actionItem.completionDate = new Date()
        actionItem.dueDate = new Date()
        actionItem.dateCreated = new Date()
        actionItem.save(failOnError: true, validate: false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        QualitySampling.metaClass.static.getActionItemIds = { Map params, Long tenantId, String pvqTypeEnum -> return [actionItem.id]}
        Tenants.metaClass.static.currentId = { -> return 0 }
        when:
        params.dataType = "CASE_CORRECTIONS"
        controller.indexPvq()
        then:
        response.json.aaData[0].size() == 16
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test indexPvq default"(){
        User normalUser = makeNormalUser("user", [])
        ActionItem actionItem = new ActionItem(description: "description",actionCategory: new ActionItemCategory(key: "key",name: "category"),assignedTo: normalUser,completionDate: new Date(),dueDate: new Date(),priority: "priority",status: StatusEnum.IN_PROGRESS,createdBy: "user",modifiedBy: "user",appType: AppTypeEnum.QUALITY_MODULE)
        actionItem.completionDate = new Date()
        actionItem.dueDate = new Date()
        actionItem.dateCreated = new Date()
        actionItem.save(failOnError: true, validate: false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        QualitySubmission.metaClass.static.getActionItemIds = { Map params, Long tenantId -> return [actionItem.id]}
        Tenants.metaClass.static.currentId = { -> return 0 }
        when:
        params.dataType = "QUALITY_MODULE"
        controller.indexPvq()
        then:
        response.json.aaData[0].size() == 16
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }
}
