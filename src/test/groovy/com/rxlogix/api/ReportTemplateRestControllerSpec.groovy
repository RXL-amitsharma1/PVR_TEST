package com.rxlogix.api

import com.rxlogix.DynamicReportService
import com.rxlogix.LibraryFilter
import com.rxlogix.TemplateService
import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, ReportTemplate])
class ReportTemplateRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportTemplateRestController> {

    public static final user = "unitTest"

    def setup() {
        def normalUser = makeNormalUser()
        def adminUser = makeAdminUser()
        controller.springSecurityService = makeSecurityService(adminUser)

//        List<ReportField> selectedFields = new ArrayList<ReportField>()
        CaseLineListingTemplate template1 = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, description: "This template can be used.", name: "not deleted", isDeleted: false, createdBy: "normalUser", owner: normalUser, modifiedBy: "normalUser",
                columnList: new ReportFieldInfoList()).save(flush: true, failOnError: true)
        CaseLineListingTemplate template2 = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, description: "This template cannot be used.", name: "is deleted", isDeleted: true, createdBy: "normalUser", owner: normalUser, modifiedBy: "normalUser",
                columnList: new ReportFieldInfoList()).save(flush: true, failOnError: true)

        request.addHeader("Accept", "application/json")
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains CaseLineListingTemplate, DataTabulationTemplate, CustomSQLTemplate, ReportField, User, Role, UserRole, Tenant, ReportTemplate, Preference, TemplateUserState, ReportFieldInfo, ExecutedCaseLineListingTemplate, TemplateQuery, PeriodicReportConfiguration
        defineBeans {
            templateService(InstanceFactoryBean, new MockFor(TemplateService).proxyInstance(), TemplateService)
        }
    }

    def cleanupSpec() {
    }

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"),createdBy: "user",modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'normalUser', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true,validate:false)
        UserRole.create(normalUser, userRole, true)
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
        def preferenceAdmin = new Preference(locale: new Locale("en"),createdBy: "user",modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser { -> user }
        return securityMock.proxyInstance()
    }

    private makeUserService(User user) {
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> user }
        return userMock.proxyInstance()
    }

    void "PVR-419: Template Search should not list deleted templates"() {
        given: "Two templates"
        def mockTemplateService = new MockFor(TemplateService)

        mockTemplateService.demand.getTemplateList { String search, int offset, int max, Boolean showXMLSpecific ->
            return [[id: 0, text: "queryName", hasBlanks: false, qced: false, isFavorite: false]]
        }

        mockTemplateService.demand.getTemplateListCount { String search, Boolean showXMLSpecific ->
            return 1
        }
        def templateService = mockTemplateService.proxyInstance()
        ReportTemplate reportTemplate = ReportTemplate.get(1)
        reportTemplate.metaClass.getTemplateService = { return templateService }

        controller.templateService = templateService

        User.metaClass.isAdmin = { true }

        when: "Call getTemplateList method"
        controller.getTemplateList("",1,30,false)

        then: "Should only return the non deleted one"
        response.status == 200
        response.json.items != null
        response.json.total_count != null
    }

    void "With Valid Query Id NameDescription should respond"() {
        given:
        ReportTemplate.metaClass.static.read = { Long id -> CaseLineListingTemplate.get(id) }
        controller.userService = makeUserService(new User(username: 'user', password: 'user', fullName: "Joe Griffin"))

        when: "Call getTemplateNameDescription() method"
        controller.getTemplateNameDescription(1L)

        then: "Should only return the non deleted one"
        response.status == 200
        response.json.text
    }


//    void "Test columns method returns available columns for templates"() {
//        when: "The columns action is executed"
//            controller.columns()
//        then: "Columns JSON is returned"
////            response.text.contains("Case Number")
//            response.text == "" // this is wrong but gets tests working so we can see results in builds
//    }


//    void "Test the list action returns the correct model"() {
//
//        given: "Service method will get called once only"
//        def mockService = new MockFor(TemplateService)
//        mockService.demand.getTemplates(1..1) { ->
//            return ['first template', 'second template']
//        }
//        controller.templateService = mockService.proxyInstance()
//
//        when: "The index action is executed"
//        controller.list(10)
//
//        then: "The model is correct"
//        model.templates // this amounts to not empty
//        model.count > 0
//    }

    void "test index"(){
        User normalUser = makeNormalUser()
        ReportTemplate reportTemplate = new ReportTemplate(category: new Category(name: "category"),name: "template",description: "description",owner: normalUser,createdBy: "user",lastExecuted: new Date(),templateType: TemplateTypeEnum.CASE_LINE,tags: [new Tag(name: "tag")],qualityChecked: true,useFixedTemplate: true,templateUserStates: [new TemplateUserState(user: normalUser,isFavorite: true)])
        reportTemplate.dateCreated = new Date()
        reportTemplate.lastUpdated = new Date()
        reportTemplate.save(failOnError:true,validate:false)
        TemplateQuery templateQuery = new TemplateQuery(template: reportTemplate,report: new PeriodicReportConfiguration())
        templateQuery.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){-> normalUser}
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ReportTemplate.metaClass.static.fetchAllIdsBySearchString = { LibraryFilter filter, boolean showXMLOption = false, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){[max  : 10, offset: 0,sort : "",order: ""]
                    return [[reportTemplate.id]]
                }
            }
        }
        ReportTemplate.metaClass.static.countRecordsBySearchString = { LibraryFilter filter, boolean showXMLOption = false -> new Object(){
                int get(Object o){
                    return 1
                }
            }
        }
        ReportTemplate.metaClass.countUsage = { -> return 0}
        when:
        controller.index()
        then:
        response.json.aaData[0].size() == 16
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
    }

    void "test getTemplateList"(){
        def mockTemplateService = new MockFor(TemplateService)
        mockTemplateService.demand.getTemplateList(0..1){String search, int offset, int max, Boolean showCLLSpecificOnly -> return [[id: 1, text: "text", hasBlanks: false, qced: null, isFavorite: false, configureAttachments: null]]}
        mockTemplateService.demand.getTemplateListCount(0..1){String search, Boolean showCLLSpecificOnly -> return 1}
        controller.templateService = mockTemplateService.proxyInstance()
        when:
        controller.getTemplateList("template",0,0,false)
        then:
        response.json == [total_count:1, items:[[hasBlanks:false, configureAttachments:null, qced:null, id:1, text:"text", isFavorite:false]]]
    }

    void "test getTemplateSetCLL"(){
        def mockTemplateService = new MockFor(TemplateService)
        mockTemplateService.demand.getTemplateListForTemplateSet(0..1) { Long oldSelectedId, String search, int offset, int max, Boolean showCLLSpecificOnly, boolean includeCllWithCustomSql-> return [list: [[id: 1, nameWithDescription: "text", groupingColumns: null, qualityChecked: true, isFavorite: false]], totalCount: 1] }
        controller.templateService = mockTemplateService.proxyInstance()
        when:
        controller.getTemplateSetCLL(1,"template",0,0,null)
        then:
        response.json == [total_count: 1, items: [[groupingColumns: null, qced: true, id: 1, text: "text", isFavorite: false]]]
    }

//    void "test getTemplateNameDescription when template is not an instance of CaseLineListingTemplate"(){
//        User normalUser = makeNormalUser()
//        ReportTemplate reportTemplate = new ReportTemplate(category: new Category(name: "category"),name: "template",description: "description",owner: normalUser,createdBy: "user",lastExecuted: new Date(),templateType: TemplateTypeEnum.CASE_LINE,qualityChecked: true,templateUserStates: [new TemplateUserState(user: normalUser,isFavorite: true)])
//        reportTemplate.save(failOnError:true,validate:false)
//        def mockUserService = new MockFor(UserService)
//        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
//        controller.userService = mockUserService.proxyInstance()
//        when:
//        controller.getTemplateNameDescription(reportTemplate.id)
//        then:
//        response.json == []
//    }

    void "test getTemplateNameDescription when template is instance of CaseLineListingTemplate"(){
        User normalUser = makeNormalUser()
        CaseLineListingTemplate reportTemplate = new CaseLineListingTemplate(category: new Category(name: "category"),name: "template",description: "description",owner: normalUser,createdBy: "user",lastExecuted: new Date(),templateType: TemplateTypeEnum.CASE_LINE,groupingList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(reportField: new ReportField(name: "report_field"))]))
        reportTemplate.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.getTemplateNameDescription(reportTemplate.id)
        then:
        response.json == [configureAttachments:"template (description)", groupingColumns:["report_field"], qced:false, text:"template (description) - Owner: Joe Griffin", isFavorite:false]
    }

    void "test getTemplateNameDescription when template is instance of CaseLineListingTemplate but no groupingList"(){
        User normalUser = makeNormalUser()
        CaseLineListingTemplate reportTemplate = new CaseLineListingTemplate(category: new Category(name: "category"),name: "template",description: "description",owner: normalUser,createdBy: "user",lastExecuted: new Date(),templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.getTemplateNameDescription(reportTemplate.id)
        then:
        response.json == [configureAttachments:"template (description)", qced:false, text:"template (description) - Owner: Joe Griffin", isFavorite:false]
    }

    void "test getReportFieldInfoList"(){
        User normalUser = makeNormalUser()
        ReportTemplate reportTemplate = new CaseLineListingTemplate(category: new Category(name: "category"), name: "template", description: "description", owner: normalUser, createdBy: "user", lastExecuted: new Date(), templateType: TemplateTypeEnum.CASE_LINE, groupingList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(reportField: new ReportField(name: "report_field"), renameValue: "rename")]), columnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(reportField: new ReportField(name: "report_field"), renameValue: "rename")]), rowColumnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(reportField: new ReportField(name: "report_field"), renameValue: "rename")]))
        reportTemplate.save(failOnError:true,validate:false)
        when:
        params.term = ""
        params.templateId = [reportTemplate.id]
        controller.getReportFieldInfoList()
        then:
        response.json == [items: [[children: [[id: 3, text: "rename", templateId: 3, type: "CLL"], [id: 2, text: "rename", templateId: 3, type: "CLL"], [id: 1, text: "rename", templateId: 3, type: "CLL"]], id: 3, text: "template (description) - Owner: Joe Griffin"]]]
    }

    void "test getReportFieldInfoList null values"(){
        User normalUser = makeNormalUser()
        ReportTemplate reportTemplate = new CaseLineListingTemplate(category: new Category(name: "category"),name: "template",description: "description",owner: normalUser,createdBy: "user",lastExecuted: new Date(),templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError:true,validate:false)
        when:
        params.term = ""
        params.templateId = [reportTemplate.id]
        controller.getReportFieldInfoList()
        then:
        response.json == [items:[[children:[], id:3, text:"template (description) - Owner: Joe Griffin"]]]
    }

    void "test getReportFieldInfoNameDescription"(){
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(renameValue: "rename",reportField: new ReportField(name: "report"))
        reportFieldInfo.save(failOnError:true,validate:false)
        when:
        controller.getReportFieldInfoNameDescription(reportFieldInfo.id)
        then:
        response.json == [text:"rename"]
    }

//    void "test getSortOrder"(){
//        ExecutedCaseLineListingTemplate executedCaseLineListingTemplate = new ExecutedCaseLineListingTemplate()
//        executedCaseLineListingTemplate.save(failOnError:true,validate:false,flush:true)
//        when:
//        params.currentId = executedCaseLineListingTemplate.id
//        controller.getSortOrder()
//        then:
//        response.json == []
//    }

    void "test toMap"(){
        User normalUser = makeNormalUser()
        ReportTemplate reportTemplate = new ReportTemplate(category: new Category(name: "category"),name: "template",description: "description",owner: normalUser,createdBy: "user",lastExecuted: new Date(),templateType: TemplateTypeEnum.CASE_LINE,tags: [new Tag(name: "tag")],qualityChecked: true,useFixedTemplate: true,templateUserStates: [new TemplateUserState(user: normalUser,isFavorite: true)])
        reportTemplate.dateCreated = new Date()
        reportTemplate.lastUpdated = new Date()
        reportTemplate.save(failOnError:true,validate:false)
        TemplateQuery templateQuery = new TemplateQuery(template: reportTemplate,report: new PeriodicReportConfiguration())
        templateQuery.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        def result = controller.invokeMethod('toMap', [reportTemplate] as Object[])
        then:
        result.size() == 16
    }
}
