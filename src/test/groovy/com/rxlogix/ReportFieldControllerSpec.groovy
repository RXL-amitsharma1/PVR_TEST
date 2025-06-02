package com.rxlogix

import com.rxlogix.commandObjects.ReportFieldCO
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.config.Tenant
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ReportField, User, ReportFieldCO])
class ReportFieldControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportFieldController> {

    def setupSpec() {
        mockDomains ReportField,User, UserGroup, Role, UserRole, UserGroupUser,Tenant, Preference,ReportFieldGroup
    }

    public static final user = "unitTest"
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
        normalUser.metaClass.isAdmin = { -> return false }
        return normalUser
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return true }
        return adminUser
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getUser(0..1) { -> user }
        securityMock.demand.isCurrentUserDev(0..2) { false }
        return securityMock.proxyInstance()
    }

    private makeUserService() {
        def userMock = mockFor(UserService)
        userMock.demand.isCurrentUserDev(1..2) { false }
        return userMock.createMock()
    }

    void "test index"(){
        given:
        ReportField reportFieldInstance = new ReportField(name: "reportField", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, isCreatedByUser: true,  sourceId: 1).save(failOnError: true)
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test create"(){
        given:
        def mockReportFieldService = Mock( ReportFieldService )
        mockReportFieldService.getAllReportFieldsWithGroupsForTemplates() >> {return []}
        controller.reportFieldService=mockReportFieldService
        def normalUser = makeNormalUser()
        def mockUserService = Mock(UserService)
        mockUserService.currentUser >> { normalUser}
        controller.userService = mockUserService
        when:
        controller.create()
        then:
        response.status == 200
    }

    void "test save not validate"(){
        given:
        User adminUser = makeAdminUser()
        ReportFieldGroup reportFieldGroup=new ReportFieldGroup([id: 5, name: "Case Information", isDeleted: false])
        reportFieldGroup.save(failOnError:true,validate:false)
        def mockReportFieldService=Mock(ReportFieldService)
        mockReportFieldService.getAllReportFieldsWithGroupsForTemplates()>>{return []}
        controller.reportFieldService=mockReportFieldService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        adminUser.preference.locale=new Locale("en")
        controller.userService=mockUserService
        ReportField reportFieldInstance = new ReportField(name: "reportField", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, isCreatedByUser: true,  sourceId: 1).save(failOnError: true)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {}
        controller.CRUDService=mockCRUDService
        when:
        request.method = "POST"
        ReportFieldCO reportFieldCO = new ReportFieldCO(id: 1L, name: "reportFieldCO", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.save(reportFieldCO)
        then:
        response.status == 200
    }

    void "test save validate success"(){
        given:
        User adminUser = makeAdminUser()
        ReportFieldGroup reportFieldGroup=new ReportFieldGroup()
        reportFieldGroup.save(failOnError:true,validate:false)
        def mockReportFieldService=Mock(ReportFieldService)
        mockReportFieldService.getAllReportFieldsWithGroupsForTemplates()>>{}
        controller.reportFieldService=mockReportFieldService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        adminUser.preference.locale=new Locale("en")
        controller.userService=mockUserService
        ReportField reportFieldInstance = new ReportField(name: "reportField", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, isCreatedByUser: true,  sourceId: 1)
        ReportField.metaClass.static.get={Long id ->reportFieldInstance }
        ReportFieldCO.metaClass.static.validate={ -> true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return reportFieldInstance}
        controller.CRUDService=mockCRUDService
        when:
        request.method = "POST"
        ReportFieldCO reportFieldCO = new ReportFieldCO(id: 1L, fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.save(reportFieldCO)
        then:
        response.status == 302
        response.redirectedUrl == "/reportField/index"
    }

    void "test save validate success and Validation Exception"(){
        given:
        User adminUser = makeAdminUser()
        ReportFieldGroup reportFieldGroup=new ReportFieldGroup()
        reportFieldGroup.save(failOnError:true,validate:false)
        def mockReportFieldService=Mock(ReportFieldService)
        mockReportFieldService.getAllReportFieldsWithGroupsForTemplates()>>{}
        controller.reportFieldService=mockReportFieldService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        adminUser.preference.locale=new Locale("en")
        controller.userService=mockUserService
        ReportField reportFieldInstance = new ReportField(name: "reportField", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, isCreatedByUser: true,  sourceId: 1).save(failOnError: true)
        reportFieldInstance.save(failOnError:true,validate:false)
        ReportField.metaClass.static.get={Long id -> reportFieldInstance }
        ReportFieldCO.metaClass.static.validate={ -> true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception", reportFieldInstance.errors)}
        controller.CRUDService=mockCRUDService
        when:
        request.method = "POST"
        ReportFieldCO reportFieldCO = new ReportFieldCO(id: 1L, fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.save(reportFieldCO)
        then:
        response.status == 200
    }

    void "test show"(){
        given:
        ReportField reportFieldInstance = new ReportField(name: "reportField", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, isCreatedByUser: true,  sourceId: 1).save(failOnError: true)
        ReportField.metaClass.static.read = { Long id ->
            return reportFieldInstance
        }
        when:
        controller.show(1L)
        then:
        response.status == 200
    }

    void "test show when instance is null"(){
        given:
        ReportField.metaClass.static.read = {Long id ->
            return null
        }
        when:
        controller.show(1L)
        then:
        flash.message == 'default.not.found.message'
        response.status == 302
        response.redirectedUrl == '/reportField/index'
    }

    void "test edit"(){
        given:
        ReportField reportFieldInstance = new ReportField(name: "reportField", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, isCreatedByUser: true,  sourceId: 1).save(failOnError: true)
        ReportField.metaClass.static.read = { Long id ->
            return reportFieldInstance
        }
        def mockReportFieldService = Mock( ReportFieldService )
        mockReportFieldService.getAllReportFieldsWithGroupsForTemplates() >> {return []}
        controller.reportFieldService=mockReportFieldService
        def normalUser = makeNormalUser()
        def mockUserService = Mock(UserService)
        mockUserService.currentUser >> { normalUser}
        controller.userService = mockUserService
        when:
        controller.edit(1L)
        then:
        response.status == 200
    }

    void "test edit when instance is null"(){
        given:
        ReportField.metaClass.static.read = { Long id ->
            return null
        }
        when:
        controller.edit(999999L)
        then:
        flash.message == 'default.not.found.message'
        response.status == 302
        response.redirectedUrl == '/reportField/index'
    }

    void "test update not validate"(){
        given:
        User adminUser = makeAdminUser()
        ReportFieldGroup reportFieldGroup=new ReportFieldGroup([id: 5, name: "Case Information", isDeleted: false])
        reportFieldGroup.save(failOnError:true,validate:false)
        def mockReportFieldService=Mock(ReportFieldService)
        mockReportFieldService.getAllReportFieldsWithGroupsForTemplates()>>{return []}
        controller.reportFieldService=mockReportFieldService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        adminUser.preference.locale=new Locale("en")
        controller.userService=mockUserService
        ReportField reportFieldInstance = new ReportField(name: "reportField", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, isCreatedByUser: true,  sourceId: 1).save(failOnError: true)
        def mockCRUDService = Mock(CRUDService)
        ReportField.metaClass.static.get={Long id -> reportFieldInstance }
        ReportFieldCO.metaClass.static.validate={ -> false}
        mockCRUDService.update(_) >> {}
        controller.CRUDService=mockCRUDService
        when:
        request.method = "POST"
        ReportFieldCO reportFieldCO = new ReportFieldCO(id: 1L, name: "reportFieldCO", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.update(reportFieldCO)
        then:
        response.status == 200
    }

    void "test update validate success"(){
        given:
        User adminUser = makeAdminUser()
        ReportFieldGroup reportFieldGroup=new ReportFieldGroup()
        reportFieldGroup.save(failOnError:true,validate:false)
        def mockReportFieldService=Mock(ReportFieldService)
        mockReportFieldService.getAllReportFieldsWithGroupsForTemplates()>>{}
        controller.reportFieldService=mockReportFieldService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        adminUser.preference.locale=new Locale("en")
        controller.userService=mockUserService
        ReportField reportFieldInstance = new ReportField(id: 1L, name: "reportField", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, isCreatedByUser: true,  sourceId: 1)
        ReportField.metaClass.static.get={Long id ->reportFieldInstance }
        ReportFieldCO.metaClass.static.validate={ -> true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {return reportFieldInstance}
        controller.CRUDService=mockCRUDService
        when:
        request.method = "POST"
        ReportFieldCO reportFieldCO = new ReportFieldCO(id: 1L, fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.update(reportFieldCO)
        then:
        response.status == 302
        response.redirectedUrl == "/reportField/index"
    }

    void "test update validate success and Validation Exception"(){
        given:
        User adminUser = makeAdminUser()
        ReportFieldGroup reportFieldGroup=new ReportFieldGroup()
        reportFieldGroup.save(failOnError:true,validate:false)
        def mockReportFieldService=Mock(ReportFieldService)
        mockReportFieldService.getAllReportFieldsWithGroupsForTemplates()>>{}
        controller.reportFieldService=mockReportFieldService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        adminUser.preference.locale=new Locale("en")
        controller.userService=mockUserService
        ReportField reportFieldInstance = new ReportField(name: "reportField", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, isCreatedByUser: true,  sourceId: 1).save(failOnError: true)
        reportFieldInstance.save(failOnError:true,validate:false)
        ReportField.metaClass.static.get={Long id -> reportFieldInstance }
        ReportFieldCO.metaClass.static.validate={ -> true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception", reportFieldInstance.errors)}
        controller.CRUDService=mockCRUDService
        when:
        request.method = "POST"
        ReportFieldCO reportFieldCO = new ReportFieldCO(id: 1L, fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.update(reportFieldCO)
        then:
        response.status == 200
    }

    void "test update when instance does not exist"(){
        given:
        User adminUser = makeAdminUser()
        ReportFieldGroup reportFieldGroup=new ReportFieldGroup()
        reportFieldGroup.save(failOnError:true,validate:false)
        def mockReportFieldService=Mock(ReportFieldService)
        mockReportFieldService.getAllReportFieldsWithGroupsForTemplates()>>{}
        controller.reportFieldService=mockReportFieldService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        adminUser.preference.locale=new Locale("en")
        controller.userService=mockUserService
        ReportField.metaClass.static.get={Long id -> null }
        when:
        request.method = "POST"
        ReportFieldCO reportFieldCO = new ReportFieldCO(id: 1L, fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.update(reportFieldCO)
        then:
        response.status == 302
        response.redirectedUrl == '/reportField/index'
    }

    void "test delete when instance does not exist"(){
        given:
        ReportFieldCO reportFieldCOInstance = new ReportFieldCO(id: 1, name: "reportFieldCO", fieldGroup: new ReportFieldGroup([id: 5, name: "Case Information"]), querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true, isCreatedByUser: true)
        ReportField reportField=new ReportField()
        when:
        request.method = 'POST'
        controller.delete(reportFieldCOInstance)
        then:
        response.status == 302
        response.redirectedUrl == '/reportField/index'
    }
}
