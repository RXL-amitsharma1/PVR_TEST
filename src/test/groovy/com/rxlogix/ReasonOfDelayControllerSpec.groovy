package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.user.*
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, User])
class ReasonOfDelayControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReasonOfDelayController> {


    def setupSpec() {
        mockDomains User, Tenant
    }

    private makeUserService() {
        def userMock = new MockFor(UserService)
        userMock.demand.isCurrentUserDev(1..2) { false }
        return userMock.createMock()
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
        def userRole = new Role(authority: 'isAuthenticated()', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
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
        return adminUser
    }

    def setup() {
    }

    def cleanup() {
    }

    void "test rodMapping"(){
        when:
        controller.rodMapping()
        then:
        response.status==200
    }

    void "test getLateMapping"(){
        given:
        def mockReportExecutorService = Mock( ReportExecutorService )
        mockReportExecutorService.getLateList() >> { return [new Late(textDesc: "textDesc", id: 1, ownerApp: "PVC", lateType: 1L),new Late(textDesc: "test1", id: 2, ownerApp: "PVC", lateType: 2L)]}
        mockReportExecutorService.getRootCauseList() >> {return [new RootCause(textDesc: 'Late', ownerApp: 'PVC', lateType: 1L)]}
        mockReportExecutorService.getRootCauseClassList() >> {return [new RootCauseClassification(textDesc: "textDesc",ownerApp: "PVC")]}
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.hidden = false
        controller.getLateMapping()
        then:
        response.status==200
    }

    void "Test getRootCauseMapping"(){
        given:
        def mockReportExecutorService = Mock( ReportExecutorService )
        mockReportExecutorService.getRootCauseList() >> {return [new RootCause(textDesc: 'Late', ownerApp: 'PVC', lateType: 1L)]}
        mockReportExecutorService.getResponsiblePartyList() >> {return [new ResponsibleParty(textDesc: 'Late', ownerApp: 'PVC', lateType: 1L)]}
        mockReportExecutorService.getRootCauseSubCategoryList() >> {return [new RootCauseSubCategory(textDesc: 'Late',ownerApp: "PVC")]}
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.hidden = false
        controller.getRootCauseMapping()
        then:
        response.status == 200
    }

    void "Test getRootCauseList"(){
        given:
        def mockReportExecutorService = Mock( ReportExecutorService )
        mockReportExecutorService.getRootCauseList() >> {return [new RootCause(textDesc: 'Late', ownerApp: 'PVC', lateType: 1L)]}
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.hidden = false
        controller.getRootCauseList()
        then:
        response.status == 200
    }

    void "Test getResponsiblePartyList"(){
        given:
        def mockReportExecutorService = Mock( ReportExecutorService )
        mockReportExecutorService.getResponsiblePartyList() >> {return [new ResponsibleParty(textDesc: 'Late', ownerApp: 'PVC', lateType: 1L)]}
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.hidden = false
        controller.getResponsiblePartyList()
        then:
        response.status == 200
    }

    void "Test getRootCauseSubCategoryList"(){
        given:
        def mockReportExecutorService = Mock( ReportExecutorService )
        mockReportExecutorService.getRootCauseSubCategoryList() >> {return [new RootCauseSubCategory(ownerApp: "PVC")]}
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.hidden = false
        controller.getRootCauseSubCategoryList()
        then:
        response.status == 200
    }

    void "Test getRootCauseClassList"(){
        given:
        def mockReportExecutorService = Mock( ReportExecutorService )
        mockReportExecutorService.getRootCauseClassList() >> {return [new RootCauseClassification(ownerApp: "PVC")]}
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.hidden = false
        controller.getRootCauseClassList()
        then:
        response.status == 200
    }

    void "test getLateJson "() {
        given:
        def mockReportExecutorService = Mock(ReportExecutorService)
        Tenants.metaClass.static.currentId = { return tenant.id }
        mockReportExecutorService.getLateList() >> { return [new Late(textDesc: "textDesc", id: 1, ownerApp: "PVC", lateType: 1L), new Late(textDesc: "test1", id: 2, ownerApp: "PVQ", lateType: 1L)] }
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.hidden = false
        def model = controller.getLateJson()
        then:
        response.status == 200
    }

    void "test getRootCauseJson "() {
        given:
        def tenantId = 1L
        def mockReportExecutorService = Mock(ReportExecutorService)
        Tenants.metaClass.static.currentId = { return tenant.id }
        mockReportExecutorService.getRootCauseList() >> { return [new RootCause(textDesc: "textDesc",ownerApp: "PVQ")] }
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.hidden = false
        controller.getRootCauseJson()
        then:
        response.status == 200
    }

    void "test getRootCauseSubCategoryJson "() {
        given:
        def mockReportExecutorService = Mock(ReportExecutorService)
        Tenants.metaClass.static.currentId = { return 1L }
        mockReportExecutorService.getRootCauseSubCategoryList() >> { return [new RootCauseSubCategory(textDesc: "textDesc", ownerApp: "PVC" , hiddenDate: new Date())] }
        controller.reportExecutorService = mockReportExecutorService
        when:
        def result = controller.getRootCauseSubCategoryJson("PVC",false)
        then:
        response.status == 200

    }

    void "test getRootCauseClassJson "() {
        given:
        def mockReportExecutorService = Mock(ReportExecutorService)
        Tenants.metaClass.static.currentId = { return 1L }
        mockReportExecutorService.getRootCauseClassList() >> { return [new RootCauseClassification(textDesc: "textDesc", ownerApp: "PVC")] }
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.hidden = false
        def result = controller.getRootCauseClassJson()
        then:
        response.status == 200

    }

    void "test getResponsiblePartyJson"() {
        given:
        def tenantId = 1L
        def mockReportExecutorService = Mock(ReportExecutorService)
        Tenants.metaClass.static.currentId = { return tenant.id }
        mockReportExecutorService.getResponsiblePartyList() >> { return [new ResponsibleParty(textDesc: "textDesc", ownerApp: "PVC")] }
        controller.reportExecutorService = mockReportExecutorService
        when:
        controller.getResponsiblePartyJson()
        then:
        response.status == 200
    }

    void "test getLateJsonByApp"() {
        given:
        List<Late> lateList = []
        def mockReportExecutorService = Mock(ReportExecutorService)
        mockReportExecutorService.getLateList() >> { return lateList }
        controller.reportExecutorService = mockReportExecutorService
        when:
        String ownerApp = "OwnerApp"
        params.hidden = true
        controller.getLateJsonByApp(ownerApp)
        then:
        response.status == 200
    }

    void "test saveLate"() {
        given:
        def rootCauseIds = "1,2"
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.createLateMapping(_, _, _, _, _) >> { return true }
        controller.reasonOfDelayService = mockReasonOfDelayService
        when: "call saveLate"
        params.id = id
        params.hide = true
        params.label = "label"
        params.ownerApp = "PVC"
        params.mapping = rootCauseIds
        params.type = 10L
        params.rootCauseClass = '11,22'
        controller.saveLate()
        then:
        response.status == statusVal
        response.text == textVal

        where:
        id   | statusVal | textVal
        1L   | 302       | ''
        null | 200       | 'Ok'

    }

    void "test saveRootCause"() {
        given:
        List responsiblePartyIds = ["testField1", "testField2"]
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.createRootCauseMapping(_, _, _) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService
        when: "call saveRootCause"
        params.label = "label"
        params.hide = true
        params.ownerApp = "ownerApp"
        params.responsiblePartyIds = "testField1"
        params.mapping = "1,2,3"
        controller.saveRootCause()

        then:
        response.status == 200
        response.text == "Ok"
    }

    void "test saveRootCauseSub"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        RootCauseSubCategory entity = new RootCauseSubCategory()
        mockReasonOfDelayService.createRootCauseSub(_, _, _) >> { args -> entity.setOwnerApp(args[1]); entity.setTextDesc(args[0]) }
        controller.reasonOfDelayService = mockReasonOfDelayService

        when: "call saveRootCauseSub"
        params.label = "label"
        params.hide = false
        params.ownerApp = "ownerApp"
        controller.saveRootCauseSub()

        then:
        response.status == 200
        entity.textDesc == "label"
        entity.ownerApp == "ownerApp"
        response.text == "Ok"
    }

    void "test saveRootCauseClass"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        RootCauseClassification entity = new RootCauseClassification()
        mockReasonOfDelayService.createRootCauseClass(_, _, _) >> { args -> entity.setOwnerApp(args[1]); entity.setTextDesc(args[0]) }
        controller.reasonOfDelayService = mockReasonOfDelayService

        when: "call saveRootCauseClass"
        params.label = "label"
        params.hide = false
        params.ownerApp = "ownerApp"
        controller.saveRootCauseClass()

        then:
        response.status == 200
        entity.textDesc == "label"
        entity.ownerApp == "ownerApp"
        response.text == "Ok"
    }

    void "test saveResponsibleParty"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.createResponsibleParty(_, _) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService

        when: "call saveResponsibleParty"
        params.label = "label"
        params.hide = true
        params.ownerApp = "ownerApp"
        controller.saveResponsibleParty()

        then:
        response.status == 200
        response.text == "Ok"
    }

    void "test deleteLate"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.deleteLateMapping(_,_) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService

        when:
        params.id = 1
        params.ownerApp = 'PVC'
        controller.deleteLate(1L,'PVC')
        then:
        response.status == 302
    }

    void "test deleteRootCause"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.deleteRootCauseMapping(_,_) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService

        when:
        params.id = 1L
        params.ownerApp = 'PVQ'
        controller.deleteRootCause(1L,'PVC')
        then:
        response.status == 302
    }

    void "test deleteRootCauseSub"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.deleteRootCauseSubMapping(_,_) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService

        when:
        params.id = 1L
        params.ownerApp = 'PVC'
        controller.deleteRootCauseSub(1L,'PVC')
        then:
        response.status == 302
    }

    void "test deleteRootCauseClass"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.deleteRootCauseClassMapping(_,_) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService

        when:
        params.id = 1L
        params.ownerApp = 'PVC'
        controller.deleteRootCauseClass(1L,'PVC')
        then:
        response.status == 302
    }

    void "test deleteResponsibleParty"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.deleteResponsiblePartyMapping(_,_) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService

        when:
        params.id = 1L
        params.ownerApp = 'PVC'
        controller.deleteResponsibleParty(1L,'PVC')
        then:
        response.status == 302
    }


    void "test editLate"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.editLateMapping(_, _, _, _, _, _) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService
        when: "call editLate"
        params.id = 1L
        params.label = "label"
        params.ownerApp = "ownerApp"
        params.mapping = '1,2,3'
        controller.editLate()
        then:
        response.status == 200
        response.text == "Ok"
    }
    void "test editLate, throw Exception"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.editLateMapping(_, _, _, _, _, _) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService
        when: "call editLate"
        params.id = 1L
        params.label = "#"
        params.ownerApp = "ownerApp"
        params.mapping = '1,2,3'
        controller.editLate()
        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Label should not be empty and should not contain special characters!"
        response.status == 200
    }

    void "test editRootCause"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.editRootCauseMapping(_, _, _, _) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService
        when: "call editRootCause"
        params.id = 1L
        params.label = "label"
        params.ownerApp = "ownerApp"
        params.mapping = '1,2,3'
        controller.editRootCause()
        then:
        response.status == 200
        response.text == "Ok"
    }
    void "test editRootCause, throw Exception"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.editRootCauseMapping(_, _, _, _) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService
        when: "call editRootCause"
        params.id = 1L
        params.label = "#"
        params.ownerApp = "ownerApp"
        params.mapping = '1,2,3'
        controller.editRootCause()
        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Label should not be empty and should not contain special characters!"
        response.status == 200
    }


    void "test editResponsibleParty"() {
        given:
        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        mockReasonOfDelayService.editResponsiblePartyMapping(_, _, _) >> {}
        controller.reasonOfDelayService = mockReasonOfDelayService

        when: "call editResponsibleParty"
        params.id = 1
        params.label = "label"
        params.ownerApp = "ownerApp"
        controller.editResponsibleParty()

        then:
        response.status == 200
        response.text == "Ok"
    }

    void "test editRootCauseSub"() {
        given:

        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        RootCauseSubCategory entity = new RootCauseSubCategory()
        mockReasonOfDelayService.editRootCauseSubMapping(_, _, _,_) >> { args -> entity.setOwnerApp(args[2]); entity.setTextDesc(args[1]) }
        controller.reasonOfDelayService = mockReasonOfDelayService
        when: "call editRootCauseSub"
        params.id = 1L
        params.label = "label"
        params.ownerApp = "ownerApp"
        params.mapping = '1,2,3'
        params.hide = false
        controller.editRootCauseSub()

        then:
        response.status == 200
        response.text == "Ok"
        entity.textDesc == "label"
        entity.ownerApp == "ownerApp"
    }

    void "test editRootCauseClass"() {
        given:

        def mockReasonOfDelayService = Mock(ReasonOfDelayService)
        RootCauseClassification entity = new RootCauseClassification()
        mockReasonOfDelayService.editRootCauseClassMapping(_, _, _,_) >> { args -> entity.setOwnerApp(args[2]); entity.setTextDesc(args[1]) }
        controller.reasonOfDelayService = mockReasonOfDelayService
        when: "call editRootCauseClass"
        params.id = 1L
        params.label = "label"
        params.ownerApp = "ownerApp"
        params.mapping = '1,2,3'
        params.hide = false
        controller.editRootCauseClass()

        then:
        response.status == 200
        response.text == "Ok"
        entity.textDesc == "label"
        entity.ownerApp == "ownerApp"
    }

    void "Test editResponsibleParty, --Success"(){
        given:
        def mockReasonOfDelayService = Mock( ReasonOfDelayService )
        mockReasonOfDelayService.editResponsiblePartyMapping(_,_,_) >> {}
        controller.reasonOfDelayService=mockReasonOfDelayService
        when:
        params.id = 1L
        params.label = "label"
        params.ownerApp = "ownerApp"
        controller.editResponsibleParty()
        then:
        response.status == 200
    }

    void "Test editResponsibleParty, --Failure"(){
        when:
        params.id = 1L
        params.label = "#*#"
        params.ownerApp = "ownerApp"
        controller.editResponsibleParty()
        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Label should not be empty and should not contain special characters!"
        response.status == 200
    }

    void "Test getRODLateTypeEnum"(){
        given:
        when:
        controller.getRODLateTypeEnum("PVQ")
        then:
        response.status == 200
    }

    void "Test validateLabel, --Success"(){
        when:
        params.label="label"
        controller.validateLabel()
        then:
        response.status==200
    }

    void "Test validateLabel, throw Exception"(){
        when:
        params.label = "#*#"
        controller.validateLabel()
        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Label should not be empty and should not contain special characters!"
        response.status == 200
    }

}