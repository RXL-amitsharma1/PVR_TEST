package com.rxlogix.user

import com.rxlogix.CRUDService
import com.rxlogix.ReportFieldService
import com.rxlogix.UserService
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.config.Tenant
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import org.grails.web.json.JSONElement
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static org.springframework.http.HttpStatus.NOT_FOUND

@ConfineMetaClassChanges([User])
class FieldProfileControllerSpec extends Specification implements DataTest, ControllerUnitTest<FieldProfileController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User,UserGroup, UserGroupUser, Role, UserRole, Tenant, FieldProfile,ReportField, ReportFieldGroup, FieldProfileFields
        Holders.config.pvadmin.privacy.field.profile = "test"
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
        return adminUser
    }

    void "test delete"() {
        int run = 0
        User adminUser = makeAdminUser()
        FieldProfile fieldProfile = new FieldProfile(name: "field")
        fieldProfile.save(failOnError:true,validate:false)
        UserGroup userGroup = new UserGroup(fieldProfile: fieldProfile)
        userGroup.save(failOnError:true,validate:false)
        UserGroupUser userGroupUser = new UserGroupUser(user: adminUser,userGroup: userGroup)
        userGroupUser.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..6) { -> adminUser}
        mockUserService.demand.updateBlindedFlagForUsersAndGroups(0..1) {}
        controller.userService = mockUserService.proxyInstance()
        def mockReportFieldService = new MockFor(ReportFieldService)
        mockReportFieldService.demand.clearCacheReportFields(0..1){ -> run++}
        controller.reportFieldService = mockReportFieldService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            run++
            theInstance
        }
        mockUserService.demand.updateBlindedFlagForUsersAndGroups(1){ -> run++}
        mockUserService.demand.getUser(0..6) { -> adminUser }
        controller.userService = mockUserService.proxyInstance()
        controller.CRUDService = mockCRUDService.proxyInstance()
        GroovyMock(UserGroup, global: true)
        when:
        controller.delete(fieldProfile)
        then:
        flash.message == 'default.deleted.message'
        response.redirectUrl == '/fieldProfile/index'
        run == 1
    }

    void "test delete privacy profile"(){
        FieldProfile fieldProfile = new FieldProfile(name: "test")
        fieldProfile.save(failOnError:true,validate:false)
        when:
        controller.delete(fieldProfile)
        then:
        flash.error == 'privacy.profile.non.editable'
        response.redirectUrl == '/fieldProfile/index'
    }

    void "test delete validation exception"() {
        User adminUser = makeAdminUser()
        FieldProfile fieldProfile = new FieldProfile(name: "field")
        fieldProfile.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.delete(fieldProfile)
        then:
        flash.error == 'default.not.deleted.message'
        response.redirectUrl == '/fieldProfile/show'
    }

    void "test delete user not admin"() {
        User normalUser = makeNormalUser("user",[])
        FieldProfile fieldProfile = new FieldProfile(name: "field")
        fieldProfile.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.delete(fieldProfile)
        then:
        flash.warn == "app.fieldProfile.delete.permission"
        response.redirectUrl == '/fieldProfile/index'
    }

    void "test delete null"() {
        when:
        controller.delete(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/fieldProfile/index'
    }

    void "test clearCacheForQueryReportField"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        FieldProfile fieldProfile = new FieldProfile()
        fieldProfile.save(failOnError:true,validate:false)
        UserGroup userGroup = new UserGroup(fieldProfile: fieldProfile)
        userGroup.save(failOnError:true,validate:false)
        UserGroupUser userGroupUser = new UserGroupUser(user: normalUser,userGroup: userGroup)
        userGroupUser.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockReportFieldService = new MockFor(ReportFieldService)
        mockReportFieldService.demand.clearCacheReportFields(0..1){ -> run = true}
        controller.reportFieldService = mockReportFieldService.proxyInstance()
        when:
        controller.invokeMethod('clearCacheForQueryReportField', [fieldProfile] as Object[])
        then:
        run == true
    }

    void "test populateReportFields"() {
        FieldProfile fieldProfile = new FieldProfile()
        fieldProfile.save(failOnError: true, validate: false)
        ReportField reportField = new ReportField()
        reportField.save(failOnError: true, validate: false)
        FieldProfileFields.metaClass.static.executeUpdate = { CharSequence charSequence, Map map -> return 1 }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.addToBlindedFields(0..1) { profile, field ->
            FieldProfileFields profileFields = new FieldProfileFields(fieldProfile: fieldProfile, reportField: reportField, isBlinded: 1)
            profileFields.save(failOnError: true, validate: false)
        }
        controller.userService = mockUserService.proxyInstance()
        when:
        params['blindedReportFields'] = "${reportField.id}"
        params['protectedReportFields'] = ""
        params['hiddenReportFields'] = ""
        controller.invokeMethod('populateReportFields', [fieldProfile] as Object[])
        then:
        FieldProfileFields.findAllByFieldProfileAndIsBlinded(fieldProfile, true).size() == 1
    }

    void "test save"(){
        int run = 0
        ReportField reportField = new ReportField()
        reportField.save(failOnError:true,validate:false)
        FieldProfileFields.metaClass.static.executeUpdate = { CharSequence charSequence,Map map -> return 1}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.addToBlindedFields(0..1){ profile, field ->
            run++
        }
        mockUserService.demand.updateBlindedFlagForUsersAndGroups(0..1){ -> run++}
        mockUserService.demand.updateProtectedFlagForUsersAndGroups(0..1){ -> run++}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1) { theInstance ->
            run++
            theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params['blindedReportFields'] = "${reportField.id}"
        params['protectedReportFields'] = ""
        params['hiddenReportFields'] = ""
        controller.save()
        then:
        flash.message == 'default.created.message'
        response.redirectUrl == '/fieldProfile/index'
        run == 4
    }

    void "test save validation exception"(){
        ReportField reportField = new ReportField()
        reportField.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params['blindedReportFields'] = "${reportField.id}"
        controller.save()
        then:
        view == '/fieldProfile/create'
        model.size() == 3
    }

    void "test save exception"(){
        ReportField reportField = new ReportField()
        reportField.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params['blindedReportFields'] = "${reportField.id}"
        controller.save()
        then:
        flash.error == 'app.label.fieldProfile.save.exception'
        view == '/fieldProfile/create'
        model.size() == 3
    }

    void "test Update"(){
        int run = 0
        User adminUser = makeAdminUser()
        FieldProfile fieldProfile = new FieldProfile()
        fieldProfile.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField()
        reportField.save(failOnError:true,validate:false)
        UserGroup userGroup = new UserGroup(fieldProfile: fieldProfile)
        userGroup.save(failOnError:true,validate:false)
        UserGroupUser userGroupUser = new UserGroupUser(user: adminUser,userGroup: userGroup)
        userGroupUser.save(failOnError:true,validate:false)
        FieldProfileFields.metaClass.static.executeUpdate = { CharSequence charSequence,Map map -> return 1}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.addToBlindedFields(0..1){ profile, field ->
            run++
        }
        mockUserService.demand.updateBlindedFlagForUsersAndGroups(0..1){ -> run++}
        mockUserService.demand.updateProtectedFlagForUsersAndGroups(0..1){ -> run++}
        mockUserService.demand.getUser(0..1){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockReportFieldService = new MockFor(ReportFieldService)
        mockReportFieldService.demand.clearCacheReportFields(0..1){ -> run++}
        controller.reportFieldService = mockReportFieldService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance->
            run++
            theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params['blindedReportFields'] = "${reportField.id}"
        params['protectedReportFields'] = ""
        params['hiddenReportFields'] = ""
        controller.update(fieldProfile)
        then:
        flash.message == 'default.updated.message'
        response.redirectUrl == '/fieldProfile/index'
        run == 5
    }

    void "test Update validation error"(){
        FieldProfile fieldProfile = new FieldProfile()
        fieldProfile.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField()
        reportField.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params['blindedReportFields'] = "${reportField.id}"
        controller.update(fieldProfile)
        then:
        view == '/fieldProfile/edit'
        model.size() == 3
    }

    void "test Update exception"(){
        FieldProfile fieldProfile = new FieldProfile()
        fieldProfile.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField()
        reportField.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params['blindedReportFields'] = "${reportField.id}"
        controller.update(fieldProfile)
        then:
        flash.error == 'app.label.fieldProfile.save.exception'
        view == '/fieldProfile/edit'
        model.size() == 3
    }

    void "test Update not found"(){
        when:
        controller.update(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/fieldProfile/index'
    }

    void "test ajaxReportFieldByGroup"(){
        boolean run = false
        FieldProfile fieldProfile = new FieldProfile()
        fieldProfile.save(failOnError:true,validate:false)
        ReportFieldGroup reportFieldGroup = new ReportFieldGroup(name: "fieldGroup")
        reportFieldGroup.save(failOnError:true,validate:false)
        def mockReportFieldService = new MockFor(ReportFieldService)
        mockReportFieldService.demand.getReportFields(0..1){ReportFieldGroup reportFieldGroupInstance ->
            run = true
            return []
        }
        controller.reportFieldService = mockReportFieldService.proxyInstance()
        views['/fieldProfile/_reportFieldDisplay.gsp'] = "template content"
        when:
        params.id = fieldProfile.id
        params.name = "fieldGroup"
        controller.ajaxReportFieldByGroup()
        then:
        response.text == "template content"
        run == true
    }

    void "test checkBoxParameter"(){
        when:
        params['key_1'] = "on"
        params['key_2'] = "off"
        params['key_3'] = "on"
        def result = controller.checkBoxParameter()
        then:
        result.size() == 2
    }

    void "test edit"(){
        FieldProfile fieldProfile = new FieldProfile(name: "test profile")
        fieldProfile.save(failOnError:true,validate:false)
        when:
        controller.edit(fieldProfile)
        then:
        view == '/fieldProfile/edit'
        model.size() == 2
    }

    void "test edit privacy profile"(){
        FieldProfile fieldProfile = new FieldProfile(name: "test")
        fieldProfile.save(failOnError:true,validate:false)
        when:
        controller.edit(fieldProfile)
        then:
        flash.error == 'privacy.profile.non.editable'
        response.redirectUrl == '/fieldProfile/index'
    }

    void "test edit not found"(){
        when:
        controller.edit(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/fieldProfile/index'
    }

    void "test show"(){
        FieldProfile fieldProfile = new FieldProfile()
        fieldProfile.save(failOnError:true,validate:false)
        FieldProfile.metaClass.static.fetchReportFieldGroups = {FieldProfile profile ->
            return ['group']
        }
        when:
        controller.show(fieldProfile)
        then:
        view == '/fieldProfile/show'
        model.size() == 2
    }

    void "test show not found"(){
        when:
        controller.show(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/fieldProfile/index'
    }

    void "load profile data profile"() {
        FieldProfile fieldProfile = new FieldProfile(id: 100L)
        fieldProfile.save(failOnError:true,validate:false)
        FieldProfile.metaClass.static.fetchReportFields = {FieldProfile profile, String groupName ->
            return [[id: 1L, name: 'field1', isBlinded: true, isProtected: false, isHidden: false], [id: 2L, name: 'field2', isBlinded: false, isProtected: true, isHidden: false]]
        }
        FieldProfile.metaClass.static.get = {Long id ->
            return fieldProfile
        }
        when:
        controller.loadFieldProfileData(100L, 'Group1')
        JSONElement responseText = JSON.parse(response.text)
        then:
        response.status == 200
        responseText.reportFields.size() == 2
        responseText.fieldNameMap['1'] == 'app.reportField.field1'
        responseText.fieldNameMap['2'] == 'app.reportField.field2'
    }

    void "load profile data profile not found"() {
        when:
        controller.loadFieldProfileData(null, null)
        then:
        response.status == 404
    }
}
