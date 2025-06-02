package com.rxlogix

import com.rxlogix.cmis.AdapterFactory
import com.rxlogix.cmis.AdapterInterface
import com.rxlogix.config.*
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import org.quartz.core.QuartzScheduler
import spock.lang.Specification

class ControlPanelControllerSpec extends Specification implements DataTest, ControllerUnitTest<ControlPanelController> {

    def setup() {
        config.tempDirectory = System.getProperty("java.io.tmpdir")
        Holders.config.hazelcast.notification.dmsCache = "dms_cache_refresh"
        Holders.config.hazelcast.enabled = false
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference,OdataSettings,ApplicationSettings,AutoReasonOfDelay,ReportResultData, InboundInitialConfiguration, ReportField
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

    void "test dmsCacheRefresh"(){
        boolean run = false
        def mockApplicationSettingsService = new MockFor(ApplicationSettingsService)
        mockApplicationSettingsService.demand.dmsCacheRefresh(0..1){ ->
            run = true
        }
        controller.applicationSettingsService = mockApplicationSettingsService.proxyInstance()
        when:
        controller.invokeMethod('dmsCacheRefresh', [] as Object[])
        then:
        run == true
    }

    void "test dmsCacheRefresh exception"(){
        boolean run = false
        def mockApplicationSettingsService = new MockFor(ApplicationSettingsService)
        mockApplicationSettingsService.demand.dmsCacheRefresh(0..1){ ->
            throw new Exception()
        }
        controller.applicationSettingsService = mockApplicationSettingsService.proxyInstance()
        when:
        controller.invokeMethod('dmsCacheRefresh', [] as Object[])
        then:
        run == false
    }

    void "test storeDmsConfig"(){
        boolean run = false
        ApplicationSettings applicationSettings = new ApplicationSettings(dmsIntegration: "old_value")
        applicationSettings.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            theInstance.save(failOnError:true,validate:false,flush:true)
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockApplicationSettingsService = new MockFor(ApplicationSettingsService)
        mockApplicationSettingsService.demand.dmsCacheRefresh(0..1){ ->
            run = true
        }
        controller.applicationSettingsService = mockApplicationSettingsService.proxyInstance()
        when:
        controller.invokeMethod('storeDmsConfig', ["new_value"] as Object[])
        then:
        ApplicationSettings.first().dmsIntegration == "new_value"
        run == true
    }

    void "test sendTestDocument success"(){
        boolean run = false
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1) { -> return makeNormalUser("user",[]) }
        controller.userService = userMock.proxyInstance()
        def mockDmsService = new MockFor(DmsService)
        mockDmsService.demand.upload(1){File reportFile, String subfolder, String name, String description, String tag, String sensitivity, String author,Object object->
            run = true
        }
        controller.dmsService = mockDmsService.proxyInstance()
        when:
        def result = controller.invokeMethod('sendTestDocument', [new AjaxResponseDTO()] as Object[])
        then:
        run == true
        result == true
    }

    void "test testDmsSettings"(){
        boolean run = false
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> return makeNormalUser("user",[]) }
        controller.userService = userMock.proxyInstance()
        def mockDmsService = new MockFor(DmsService)
        mockDmsService.demand.upload(1){File reportFile, String subfolder, String name, String description, String tag, String sensitivity, String author, Object object->
            run = true
        }
        controller.dmsService = mockDmsService.proxyInstance()
        when:
        controller.testDmsSettings()
        then:
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test saveDmsSettings val equal to null"(){
        boolean run = false
        ApplicationSettings applicationSettings = new ApplicationSettings(dmsIntegration: "old_value")
        applicationSettings.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            theInstance.save(failOnError:true,validate:false,flush:true)
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockApplicationSettingsService = new MockFor(ApplicationSettingsService)
        mockApplicationSettingsService.demand.dmsCacheRefresh(0..1){ ->
            run = true
        }
        controller.applicationSettingsService = mockApplicationSettingsService.proxyInstance()
        when:
        params.dmsSettings = ""
        controller.saveDmsSettings()
        then:
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test saveDmsSettings val not equal to null and sendTestDocument returns true"(){
        int run = 0
        AdapterInterface adapter
        ApplicationSettings applicationSettings = new ApplicationSettings(dmsIntegration: "old_value")
        applicationSettings.save(failOnError:true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> return makeNormalUser("user",[]) }
        controller.userService = userMock.proxyInstance()
        def mockDmsService = new MockFor(DmsService)
        mockDmsService.demand.getAdapter(1){-> return adapter}
        mockDmsService.demand.setAdapter(1){AdapterInterface adapterInterface-> return}
        mockDmsService.demand.upload(1){File reportFile, String subfolder, String name, String description, String tag, String sensitivity, String author,Object object->
            run++
        }
        controller.dmsService = mockDmsService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            theInstance.save(failOnError:true,validate:false,flush:true)
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockApplicationSettingsService = new MockFor(ApplicationSettingsService)
        mockApplicationSettingsService.demand.dmsCacheRefresh(0..1){ ->
            run++
        }
        controller.applicationSettingsService = mockApplicationSettingsService.proxyInstance()
        AdapterFactory.metaClass.static.getAdapter = {def settings -> return adapter}
        when:
        params.dmsSettings = """
                                {
                                "dmsName":"alfresco",
                                "login":"admin",
                                "password":"123456",
                                "cmisUrl":"http://127.0.0.1:9080/alfresco/api/-default-/public/cmis/versions/1.1/browser",
                                "repositoryId":"-default-",
                                "rootFolder":"/Published",
                                "authorId":"cm:author",
                                "documentTypeId":"D:d3:d3",
                                "nameId":"cmis:name",
                                "descriptionId":"cmis:description",
                                "sensitivityId":"d3:priv2",
                                "tagId":"d3:tag2"
                                }
                             """
        controller.saveDmsSettings()
        then:
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test saveDmsSettings val not equal to null and sendTestDocument returns false"(){
        AdapterInterface adapter
        ApplicationSettings applicationSettings = new ApplicationSettings(dmsIntegration: "old_value")
        applicationSettings.save(failOnError:true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> return makeNormalUser("user",[]) }
        controller.userService = userMock.proxyInstance()
        def mockDmsService = new MockFor(DmsService)
        mockDmsService.demand.getAdapter(0..1){-> return adapter}
        mockDmsService.demand.setAdapter(0..1){AdapterInterface adapterInterface-> return}
        mockDmsService.demand.upload(0..1){File reportFile, String subfolder, String name, String description, String tag, String sensitivity, String author,Object object->
            throw new Exception()
        }
        mockDmsService.demand.setAdapter(0..1){AdapterInterface adapterInterface-> return}
        controller.dmsService = mockDmsService.proxyInstance()
        AdapterFactory.metaClass.static.getAdapter = {def settings -> return adapter}
        when:
        params.dmsSettings = """
                                {
                                "dmsName":"alfresco",
                                "login":"admin",
                                "password":"123456",
                                "cmisUrl":"http://127.0.0.1:9080/alfresco/api/-default-/public/cmis/versions/1.1/browser",
                                "repositoryId":"-default-",
                                "rootFolder":"/Published",
                                "authorId":"cm:author",
                                "documentTypeId":"D:d3:d3",
                                "nameId":"cmis:name",
                                "descriptionId":"cmis:description",
                                "sensitivityId":"d3:priv2",
                                "tagId":"d3:tag2"
                                }
                             """
        controller.saveDmsSettings()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test saveDmsSettings throw exception"() {
        when:
        params.dmsSettings = ""
        controller.saveDmsSettings()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test index"(){
        User normalUser = makeNormalUser("user",[])
        UserService mockUserService = Mock(UserService)
        controller.userService = mockUserService
        mockUserService.currentUser >> { return normalUser}
        ReportFieldService mockReportFieldService = Mock(ReportFieldService)
        controller.reportFieldService = mockReportFieldService
        ApplicationSettings applicationSettings = new ApplicationSettings()
        applicationSettings.save(failOnError:true,validate:false,flush:true)
        InboundInitialConfiguration inboundInitialConfiguration = new InboundInitialConfiguration()
        def reportField = ReportField.findByNameAndIsDeleted("masterCaseNum",false)
        inboundInitialConfiguration.reportField = reportField
        inboundInitialConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.index()
        then:
        view == '/controlPanel/index'
        model.size() == 5
    }

    void "test index not found"(){
        when:
        controller.index()
        then:
        flash.message == 'default.not.found.message'
        response.redirectUrl == '/controlPanel/index'
    }

    void "test odataConfig"(){
        given:
        OdataSettings.metaClass.static.get={Long id->new OdataSettings(dsName: "test")}
        def mockOdataService=Mock(OdataService)
        mockOdataService.getDsTables(_)>>{}
        controller.odataService=mockOdataService
        when:
        params.id=1L
        controller.odataConfig()
        then:
        response.status==200
    }

    void "test getDsTableFields"(){
        given:
        OdataSettings.metaClass.static.get={Long id->new OdataSettings(dsName: "test")}
        def mockOdataService=Mock(OdataService)
        mockOdataService.getDsTableFields(_,_)>>{}
        controller.odataService=mockOdataService
        when:
        params.id=1L
        params.dsName=" "
        params.tableName=" "
        controller.getDsTableFields()
        then:
        response.status==200
    }

    void "test saveOdataConfig try success"(){
        given:
        OdataSettings.metaClass.static.get={Long id->new OdataSettings(dsName: "test")}
        OdataSettings.metaClass.static.setPasswordEncoded={}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveOrUpdate(_)>>{}
        controller.CRUDService = mockCRUDService
        when:
        params.id=1L
        params.dsPassword="check@1"
        controller.saveOdataConfig()
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/odataConfig/1"
    }

    void "test saveOdataConfig validation exception"(){
        given:
        OdataSettings.metaClass.static.get={Long id->new OdataSettings(dsName: "test")}
        OdataSettings.metaClass.static.setPasswordEncoded={}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveOrUpdate(_)>>{throw new ValidationException("Validation Exception", new OdataSettings(dsName: "test").errors)}
        controller.CRUDService = mockCRUDService
        def mockOdataService=Mock(OdataService)
        mockOdataService.getDsTables(_)>>{}
        controller.odataService=mockOdataService
        when:
        params.id=1L
        params.dsPassword="check@1"
        controller.saveOdataConfig()
        then:
        response.status==200
    }

    void "test odataSources"(){
        when:
        controller.odataSources()
        then:
        response.status==200
    }

    void "test odataSourceList"(){
        given:
        OdataSettings odataSettings=new OdataSettings(isDeleted: false)
        when:
        controller.odataSourceList()
        then:
        response.status==200
    }

    void "test deleteOdataSource"(){
        given:
        OdataSettings.metaClass.static.get={Long id->new OdataSettings(dsName: "test")}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_)>>{}
        controller.CRUDService = mockCRUDService
        when:
        params.id=1L
        params.dsPassword="check@1"
        controller.deleteOdataSource()
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/odataSources"
    }

    void "test update not found"(){
        when:
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/index"
    }

    void "test update found"(){
        given:
        ApplicationSettings applicationSettings=new ApplicationSettings(dmsIntegration: "test")
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_)>>{return applicationSettings}
        controller.CRUDService = mockCRUDService
        def mockReportFieldService=Mock(ReportFieldService)
        mockReportFieldService.clearCacheReportFields()>>{}
        controller.reportFieldService=mockReportFieldService
        def mockApplicationSettingsService=Mock(ApplicationSettingsService)
        mockApplicationSettingsService.reload()>>{}
        controller.applicationSettingsService=mockApplicationSettingsService
        when:
        controller.update(applicationSettings)
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/index"
    }

    void "test update found validation exception"(){
        given:
        ApplicationSettings applicationSettings=new ApplicationSettings(dmsIntegration: "test")
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_)>>{throw new ValidationException("Validation Exception", applicationSettings.errors)}
        controller.CRUDService = mockCRUDService
        when:
        controller.update(applicationSettings)
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/index"
    }

    void "test mirrorLdapValues"(){
        given:
        def mockLdapService = Mock(LdapService)
        mockLdapService.mirrorLdapValues()>>{}
        controller.ldapService = mockLdapService
        when:
        controller.mirrorLdapValues()
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/index"
    }

    void "test updateMedDra try"(){
        given:
        def mockMedDraChangeService=Mock(MedDraChangeService)
        mockMedDraChangeService.bulkUpdate(_)>>{}
        controller.medDraChangeService=mockMedDraChangeService
        when:
        params.data=" "
        controller.updateMedDra()
        then:
        response.status==500
    }

    void "test updateMedDra exception"(){
        given:
        def mockMedDraChangeService=Mock(MedDraChangeService)
        mockMedDraChangeService.bulkUpdate(_)>>{throw new Exception()}
        controller.medDraChangeService=mockMedDraChangeService
        when:
        params.data=" "
        controller.updateMedDra()
        then:
        response.status==500
    }

    void "test medDraAllUsage try"(){
        given:
        def mockMedDraChangeService=Mock(MedDraChangeService)
        mockMedDraChangeService.checkAllUsage(_)>>{}
        controller.medDraChangeService=mockMedDraChangeService
        when:
        params.data=" "
        controller.medDraAllUsage()
        then:
        response.status==500
        response.json==[:]
    }

    void "test medDraAllUsage exception"(){
        given:
        def mockMedDraChangeService=Mock(MedDraChangeService)
        mockMedDraChangeService.checkAllUsage(_)>>{throw new Exception()}
        controller.medDraChangeService=mockMedDraChangeService
        when:
        params.data=" "
        controller.medDraAllUsage()
        then:
        response.status==500
        response.json==[:]
    }

    void "test medDraUsage try"(){
        given:
        def mockMedDraChangeService=Mock(MedDraChangeService)
        mockMedDraChangeService.checkUsage(_,_)>>{}
        controller.medDraChangeService=mockMedDraChangeService
        when:
        params.level="level"
        params.old="old"
        params.new="new"
        controller.medDraUsage()
        then:
        response.status==500
        response.json==[:]
    }

    void "test medDraUsage exception"(){
        given:
        def mockMedDraChangeService=Mock(MedDraChangeService)
        mockMedDraChangeService.checkUsage(_,_)>>{throw new Exception()}
        controller.medDraChangeService=mockMedDraChangeService
        when:
        params.level="level"
        params.old="old"
        params.new="new"
        controller.medDraUsage()
        then:
        response.status==500
        response.json==[:]
    }

    void "test deleteReportFiles"(){
        given:
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser()>>{adminUser}
        adminUser.username="en"
        controller.userService=mockUserService
        def mockFileService=Mock(FileService)
        mockFileService.deleteOldTempFiles(_)>>{}
        controller.fileService=mockFileService
        when:
        controller.deleteReportFiles(2000L)
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/index"
    }

    void "test saveDefaultUi"(){
        given:
        ApplicationSettings applicationSettings=new ApplicationSettings(defaultUiSettings: "hide")
        applicationSettings.save(flushOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_)>>{}
        controller.CRUDService = mockCRUDService
        when:
        controller.saveDefaultUi()
        then:
        response.text=="ok"
    }

    void "test removeDefaultUi"(){
        given:
        ApplicationSettings applicationSettings=new ApplicationSettings(defaultUiSettings: "hide")
        applicationSettings.save(flushOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_)>>{}
        controller.CRUDService = mockCRUDService
        when:
        controller.removeDefaultUi()
        then:
        response.text=="ok"
    }

    void "test importQueriesJson"(){
        given:
        AjaxResponseDTO ajaxResponseDTO=new AjaxResponseDTO(httpCode: 200,stackTrace: "test",additionalData: "test1")
        def mockImportJsonService=Mock(ImportJsonService)
        mockImportJsonService.importJSON(_,_)>>{return ajaxResponseDTO}
        controller.importJsonService=mockImportJsonService
        when:
        controller.importQueriesJson()
        then:
        response.json.httpCode==200
    }

    void "test importTemplatesJson"(){
        given:
        AjaxResponseDTO ajaxResponseDTO=new AjaxResponseDTO(httpCode: 200,stackTrace: "test",additionalData: "test1")
        def mockImportJsonService=Mock(ImportJsonService)
        mockImportJsonService.importJSON(_,_)>>{return ajaxResponseDTO}
        controller.importJsonService=mockImportJsonService
        when:
        controller.importTemplatesJson()
        then:
        response.json.httpCode==200
    }

    void "test importConfigurationsJson"(){
        given:
        AjaxResponseDTO ajaxResponseDTO=new AjaxResponseDTO(httpCode: 200,stackTrace: "test",additionalData: "test1")
        def mockImportJsonService=Mock(ImportJsonService)
        mockImportJsonService.importJSON(_,_)>>{return ajaxResponseDTO}
        controller.importJsonService=mockImportJsonService
        when:
        controller.importConfigurationsJson()
        then:
        response.json.httpCode==200
    }

    void "test importDashboardsJson"(){
        given:
        AjaxResponseDTO ajaxResponseDTO=new AjaxResponseDTO(httpCode: 200,stackTrace: "test",additionalData: "test1")
        def mockImportJsonService=Mock(ImportJsonService)
        mockImportJsonService.importJSON(_,_)>>{return ajaxResponseDTO}
        controller.importJsonService=mockImportJsonService
        when:
        controller.importDashboardsJson()
        then:
        response.json.httpCode==200
    }

    void "test resetAutoRODJobIfInProgress try"(){
        given:
        AutoReasonOfDelay autoReasonOfDelay=new AutoReasonOfDelay(executing: true)
        autoReasonOfDelay.save(flushOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_)>>{}
        controller.CRUDService = mockCRUDService
        when:
        controller.resetAutoRODJobIfInProgress()
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/index"
    }

    void "test resetAutoRODJobIfInProgress exception"(){
        given:
        AutoReasonOfDelay autoReasonOfDelay=new AutoReasonOfDelay(executing: true)
        autoReasonOfDelay.save(flushOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_)>>{throw new Exception()}
        controller.CRUDService = mockCRUDService
        when:
        controller.resetAutoRODJobIfInProgress()
        then:
        response.status==200
    }

    void "test encryptAllReportData with no encrypt data"(){
        given:
        Holders.config.pvreports.encrypt.data=false
        when:
        controller.encryptAllReportData()
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/index"
    }

    void "test encryptAllReportData with encrypt data"(){
        given:
        Holders.config.pvreports.encrypt.data=true
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser()>>{adminUser}
        adminUser.username="en"
        controller.userService=mockUserService
        ReportResultData reportResultData=new ReportResultData(crossTabHeader: "test",reportSQL: "st",querySQL: "ql",isEncrypted: false)
        reportResultData.save(flushOnError:true,validate:true)
        when:
        controller.encryptAllReportData()
        then:
        response.status==302
        response.redirectedUrl=="/dashboard/index"
    }

    void "test decryptAllReportData"(){
        given:
        Holders.config.pvreports.encrypt.data=true
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser()>>{adminUser}
        adminUser.username="en"
        controller.userService=mockUserService
        ReportResultData reportResultData=new ReportResultData(crossTabHeader: "test",reportSQL: "st",querySQL: "ql",isEncrypted: false)
        reportResultData.save(flushOnError:true,validate:true)
        when:
        controller.decryptAllReportData()
        then:
        response.status==302
        response.redirectedUrl=="/dashboard/index"
    }

    void "test refreshCaches"(){
        def userMock = new MockFor(UserService)
        userMock.demand.getUser(0..1) { -> return makeNormalUser("user",[]) }
        controller.userService = userMock.proxyInstance()
        QuartzScheduler qs = Mock()
        controller.quartzScheduler = qs
        when:
        controller.refreshCaches()
        then:
        response.redirectedUrl=="/dashboard/index"
    }
}
