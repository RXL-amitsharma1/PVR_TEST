package com.rxlogix

import com.rxlogix.commandObjects.CustomReportFieldCO
import com.rxlogix.config.CustomReportField
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.config.Tenant
import com.rxlogix.dto.DataTableDTO
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, CustomReportField])
class CustomFieldControllerSpec extends Specification implements DataTest, ControllerUnitTest<CustomFieldController> {

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
        adminUser.metaClass.isAdmin = { -> return true }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
    }

    def setupSpec() {
        mockDomains User,Tenant,Role,UserRole,CustomReportField,ReportField,ReportFieldGroup
    }

    void "test index"(){
        when:
        controller.index()
        then:
        response.status==200
    }

    void "test list---success"(){
        given:
        DataTableDTO dataTableDTO = new DataTableDTO()
        CustomReportField customReportField=new CustomReportField()
        customReportField.save(failOnError:true,validate:false)
        customReportField.metaClass.toMap={[id:3L,name:"rx",description:"rx"]}
        when:
        dataTableDTO.aaData=["a","b"]
        controller.list()
        then:
        response.status==200
    }

    void "test list---failure"(){
        given:
        DataTableDTO dataTableDTO = new DataTableDTO()
        CustomReportField customReportField=new CustomReportField()
        customReportField.save(failOnError:true,validate:false)
        customReportField.metaClass.toMap={throw new Exception()}
        when:
        dataTableDTO.aaData=["a","b"]
        controller.list()
        then:
        response.status==500
    }

    void "test create"(){
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
        when:
        controller.create()
        then:
        response.status==200
    }

    void "test edit found"(){
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
        CustomReportField customReportField=new CustomReportField(customName: "marax")
        customReportField.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.read = { Long id ->
            return customReportField
        }
        when:
        controller.edit(1L)
        then:
        response.status==200
    }

    void "test save not validate"(){
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
        CustomReportField customReportField=new CustomReportField(customName: "marax")
        customReportField.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.get={Long id -> customReportField }
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        CustomReportFieldCO customFieldCO = new CustomReportFieldCO(id: 2L,reportFieldId: 3L,fieldGroupId: "test",customName: "rx",customDescription: "test description",defaultExpression:"test string",templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.save(customFieldCO)
        then:
        response.status==200
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
        CustomReportField customReportField=new CustomReportField(customName: "marax")
        customReportField.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.get={Long id -> customReportField }
        CustomReportFieldCO.metaClass.static.validate={ -> true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        CustomReportFieldCO customFieldCO = new CustomReportFieldCO(id: 2L,reportFieldId: 3L,fieldGroupId: "test",customName: "rx",customDescription: "test description",defaultExpression:"test string",templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.save(customFieldCO)
        then:
        response.status==302
        response.redirectedUrl=="/customField/index"
    }

    void "test save validate failure"(){
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
        CustomReportField customReportField=new CustomReportField(customName: "marax")
        customReportField.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.get={Long id -> customReportField }
        CustomReportFieldCO.metaClass.static.validate={ -> true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception", customReportField.errors)}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        CustomReportFieldCO customFieldCO = new CustomReportFieldCO(id: 2L,reportFieldId: 3L,fieldGroupId: "test",customName: "rx",customDescription: "test description",defaultExpression:"test string",templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.save(customFieldCO)
        then:
        response.status==200
    }


    void "test update not found"(){
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
        CustomReportField customReportField=new CustomReportField(customName: "marax")
        customReportField.save(failOnError:true,validate:false)
        CustomReportField customReportFieldCO=new CustomReportField(customName: "marax")
        customReportFieldCO.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.read = { Long id ->
            return customReportFieldCO
        }
        CustomReportField.metaClass.validate= { ->
            return true
        }
        when:
        request.method="POST"
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/customField/index"
    }

    void "test update not validate"(){
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
        CustomReportField customReportField=new CustomReportField(customName: "marax")
        customReportField.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.get={Long id -> customReportField }
        CustomReportFieldCO.metaClass.static.validate={ -> false}
        when:
        request.method="POST"
        CustomReportFieldCO customFieldCO = new CustomReportFieldCO(id: 2L,reportFieldId: 3L,fieldGroupId: "test",customName: "rx",customDescription: "test description",defaultExpression:"test string",templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.update(customFieldCO)
        then:
        response.status==200
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
        CustomReportField customReportField=new CustomReportField(customName: "marax")
        customReportField.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.get={Long id -> customReportField }
        CustomReportFieldCO.metaClass.static.validate={ -> true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        CustomReportFieldCO customFieldCO = new CustomReportFieldCO(id: 2L,reportFieldId: 3L,fieldGroupId: "test",customName: "rx",customDescription: "test description",defaultExpression:"test string",templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.update(customFieldCO)
        then:
        response.status==302
        response.redirectedUrl=="/customField/index"
    }

    void "test update validate failure"(){
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
        CustomReportField customReportField=new CustomReportField(customName: "marax")
        customReportField.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.get={Long id -> customReportField }
        CustomReportFieldCO.metaClass.static.validate={ -> true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception", customReportField.errors)}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        CustomReportFieldCO customFieldCO = new CustomReportFieldCO(id: 2L,reportFieldId: 3L,fieldGroupId: "test",customName: "rx",customDescription: "test description",defaultExpression:"test string",templateCLLSelectable: true,templateDTColumnSelectable: true,templateDTRowSelectable: true)
        controller.update(customFieldCO)
        then:
        response.status==200
    }

    void "test show not found"(){
        given:
        CustomReportField.metaClass.static.read = { Long id ->
            return null
        }
        when:
        controller.show(null)
        then:
        response.status==302
        response.redirectedUrl=="/customField/index"
    }

    void "test show found"(){
        CustomReportField customReportField=new CustomReportField()
        customReportField.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.read = { Long id ->
            return customReportField
        }
        when:
        controller.show(1L)
        then:
        response.status==200
    }

    void "test delete not found"(){
        given:
        CustomReportField.metaClass.static.read={Long id -> return null}
        when:
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/customField/index"
    }

    void "test delete success"(){
        given:
        ReportField reportField=new ReportField(name: "rrx")
        reportField.save(failOnError:true,validate:false)
        CustomReportField customReportField=new CustomReportField(customName: "rx", reportField: reportField)
        customReportField.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.get={Long id -> customReportField}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_) >> {theInstance,name, String justification -> true}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        controller.delete(1L)
        then:
        response.status==302
    }

    void "test delete Validation Exception"(){
        given:
        ReportField reportField=new ReportField(name: "rrx")
        reportField.save(failOnError:true,validate:false)
        CustomReportField customReportField=new CustomReportField(customName: "rx", reportField: reportField)
        customReportField.save(failOnError:true,validate:false)
        CustomReportField.metaClass.static.get={Long id -> customReportField}
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_)>>{throw new ValidationException("Validation Exception", customReportField.errors)}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        controller.delete(1L)
        then:
        response.status==302
    }
}
