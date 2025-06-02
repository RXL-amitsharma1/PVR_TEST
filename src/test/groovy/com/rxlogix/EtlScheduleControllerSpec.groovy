package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.customException.EtlUpdateException
import com.rxlogix.enums.EtlStatusEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import org.apache.cxf.common.i18n.Exception
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class EtlScheduleControllerSpec extends Specification implements DataTest, ControllerUnitTest<EtlScheduleController>{

    def setupSpec() {
        mockDomains SourceProfile, User, Tenant, Role, UserRole, EmailConfiguration, EtlSchedule, SourceProfile, DateRangeType, User
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
        normalUser.metaClass.static.isDev = { -> return false }
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
        adminUser.metaClass.static.isDev = { -> return true }
        return adminUser
    }

    def "test index success"(){
        given:
        def mocketlJobService=Mock(EtlJobService)
        mocketlJobService.getSchedule()>>{}
        mocketlJobService.getEtlStatus()>>{}
        mocketlJobService.getPreMartEtlStatus()>>{}
        mocketlJobService.checkPreMartEtlStatusApplicable()>>{return true}
        controller.etlJobService=mocketlJobService
        when:
        controller.index()
        then:
        response.status==200
    }

    def "test index exception"(){
        given:
        def mocketlJobService=Mock(EtlJobService)
        mocketlJobService.getSchedule()>>{}
        mocketlJobService.getEtlStatus()>>{throw new Exception()}
        controller.etlJobService=mocketlJobService
        when:
        controller.index()
        then:
        response.status==200
    }

    void "test edit if"(){
        given:
        EtlSchedule etlSchedule=new EtlSchedule(isDisabled: true)
        def mocketlJobService=Mock(EtlJobService)
        mocketlJobService.getSchedule()>>{return etlSchedule}
        controller.etlJobService=mocketlJobService
        when:
        controller.edit()
        then:
        response.status==200
    }

    void "test edit else"(){
        given:
        EtlSchedule etlSchedule=new EtlSchedule(isDisabled: false)
        def mocketlJobService=Mock(EtlJobService)
        mocketlJobService.getSchedule()>>{return etlSchedule}
        controller.etlJobService=mocketlJobService
        when:
        controller.edit()
        then:
        response.status==200
    }

    void "test update not found"(){
        when:
        request.method="PUT"
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/etlSchedule/index"
    }

    void "test update found startDateTime is less"(){
        given:
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService=mockUserService
        def mockEtlJobService= Mock(EtlJobService)
        mockEtlJobService.update(_)>>{}
        controller.etlJobService=mockEtlJobService
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        EtlSchedule etlSchedule=new EtlSchedule(startDateTime: "2020-10-13T12:23Z", emailConfiguration: emailConfiguration)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{return etlSchedule}
        controller.CRUDService=mockCRUDService
        when:
        params.emailConfiguration=[subject:"email",body:"rx",noEmailOnNoData:false,isDeleted:false,deliveryReceipt:false]
        params.emailToUsers="test@rxlogix.com"
        request.method="PUT"
        controller.update(etlSchedule)
        then:
        response.status==302
        response.redirectedUrl=="/etlSchedule/edit"
    }
    @Ignore
    void "test update found try success"(){
        given:
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService=mockUserService
        def mockEtlJobService= Mock(EtlJobService)
        mockEtlJobService.update(_)>>{}
        controller.etlJobService=mockEtlJobService
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        EtlSchedule etlSchedule=new EtlSchedule(startDateTime: controller.getCurrentDate(), emailConfiguration: emailConfiguration)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{return etlSchedule}
        controller.CRUDService=mockCRUDService
        when:
        params.emailConfiguration=[subject:"email",body:"rx",noEmailOnNoData:false,isDeleted:false,deliveryReceipt:false]
        params.emailToUsers="test@rxlogix.com"
        request.method="PUT"
        controller.update(etlSchedule)
        then:
        response.status==302
        response.redirectedUrl=="/etlSchedule/index"
    }

    void "test update found validation exception"(){
        given:
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService = mockUserService
        def mockEtlJobService= Mock(EtlJobService)
        mockEtlJobService.update(_)>>{}
        controller.etlJobService=mockEtlJobService
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        EtlSchedule etlSchedule = new EtlSchedule(startDateTime: controller.invokeMethod('getCurrentDate', [] as Object[]), emailConfiguration: emailConfiguration)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{throw new ValidationException("Validation Exception", etlSchedule.errors)}
        controller.CRUDService=mockCRUDService
        when:
        params.emailConfiguration=[subject:"email",body:"rx",noEmailOnNoData:false,isDeleted:false,deliveryReceipt:false]
        params.emailToUsers="test@rxlogix.com"
        request.method="PUT"
        controller.update(etlSchedule)
        then:
        response.status==200
    }

    void "test update found updateexception"(){
        given:
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService=mockUserService
        def mockEtlJobService= Mock(EtlJobService)
        mockEtlJobService.update(_)>>{throw new EtlUpdateException()}
        controller.etlJobService=mockEtlJobService
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        EtlSchedule etlSchedule = new EtlSchedule(startDateTime: controller.invokeMethod('getCurrentDate', [] as Object[]), emailConfiguration: emailConfiguration)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{return etlSchedule}
        controller.CRUDService=mockCRUDService
        when:
        params.emailConfiguration=[subject:"email",body:"rx",noEmailOnNoData:false,isDeleted:false,deliveryReceipt:false]
        params.emailToUsers="test@rxlogix.com"
        request.method="PUT"
        controller.update(etlSchedule)
        then:
        response.status==200
    }

    void "test update found exception"(){
        given:
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService=mockUserService
        EtlSchedule etlSchedule=new EtlSchedule(startDateTime: new GregorianCalendar(2020, Calendar.APRIL, 3, 7, 23, 45).time)
        def mockEtlJobService= Mock(EtlJobService)
        mockEtlJobService.update(_)>>{}
        controller.etlJobService=mockEtlJobService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{return etlSchedule}
        controller.CRUDService=mockCRUDService
        when:
        request.method="PUT"
        controller.update(etlSchedule)
        then:
        response.status==200
    }

    void "test disable success"(){
        given:
        EtlSchedule etlSchedule=new EtlSchedule(isDisabled: true)
        EtlStatus etlStatus=new EtlStatus(status: EtlStatusEnum.FAILED)
        def mockEtlJobService=Mock(EtlJobService)
        mockEtlJobService.getSchedule()>>{return etlSchedule}
        mockEtlJobService.getEtlStatus()>>{return etlStatus}
        mockEtlJobService.disable(_)>>{}
        controller.etlJobService=mockEtlJobService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{return etlSchedule}
        controller.CRUDService=mockCRUDService
        when:
        controller.disable()
        then:
        response.status==302
        response.redirectedUrl=="/etlSchedule/index"
    }

    void "test disable for Etl Running"(){
        given:
        EtlSchedule etlSchedule=new EtlSchedule(isDisabled: true)
        EtlStatus etlStatus=new EtlStatus(status: EtlStatusEnum.RUNNING)
        def mockEtlJobService=Mock(EtlJobService)
        mockEtlJobService.getSchedule()>>{return etlSchedule}
        mockEtlJobService.disable(_)>>{}
        mockEtlJobService.getEtlStatus()>>{return etlStatus}
        controller.etlJobService=mockEtlJobService
        when:
        controller.disable()
        then:
        response.status==200
    }

    void "test disable validation exception"(){
        given:
        EtlSchedule etlSchedule=new EtlSchedule()
        def mockEtlJobService=Mock(EtlJobService)
        mockEtlJobService.getSchedule()>>{return etlSchedule}
        mockEtlJobService.disable(_)>>{}
        controller.etlJobService=mockEtlJobService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{throw new ValidationException("Validation Exception", etlSchedule.errors)}
        controller.CRUDService=mockCRUDService
        when:
        controller.disable()
        then:
        response.status==200
    }

    void "test enable try success with less startTime"(){
        given:
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        EtlSchedule etlSchedule=new EtlSchedule(isDisabled: false, startDateTime: "2020-10-13T12:23Z", emailConfiguration: emailConfiguration)
        def mockEtlJobService=Mock(EtlJobService)
        mockEtlJobService.getSchedule()>>{return etlSchedule}
        mockEtlJobService.enable()>>{}
        controller.etlJobService=mockEtlJobService
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService=mockUserService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{return etlSchedule}
        controller.CRUDService=mockCRUDService
        when:
        params.emailConfiguration=[subject:"email",body:"rx",noEmailOnNoData:false,isDeleted:false,deliveryReceipt:false]
        params.emailToUsers="test@rxlogix.com"
        controller.enable(etlSchedule)
        then:
        response.status==302
        response.redirectedUrl=="/etlSchedule/index"
    }

    void "test enable try success with else more startTime"(){
        given:
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        EtlSchedule etlSchedule=new EtlSchedule(isDisabled: false, startDateTime: "2022-10-13T12:23Z", emailConfiguration: emailConfiguration)
        def mockEtlJobService=Mock(EtlJobService)
        mockEtlJobService.getSchedule()>>{return etlSchedule}
        mockEtlJobService.enable()>>{}
        controller.etlJobService=mockEtlJobService
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService=mockUserService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{return etlSchedule}
        controller.CRUDService=mockCRUDService
        when:
        params.emailConfiguration=[subject:"email",body:"rx",noEmailOnNoData:false,isDeleted:false,deliveryReceipt:false]
        params.emailToUsers="test@rxlogix.com"
        controller.enable(etlSchedule)
        then:
        response.status==302
        response.redirectedUrl=="/etlSchedule/index"
    }

    void "test enable failure exception"(){
        given:
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        EtlSchedule etlSchedule=new EtlSchedule(isDisabled: false, startDateTime: "2022-10-13T12:23Z", emailConfiguration: emailConfiguration)
        def mockEtlJobService=Mock(EtlJobService)
        mockEtlJobService.getSchedule()>>{return etlSchedule}
        mockEtlJobService.enable()>>{}
        controller.etlJobService=mockEtlJobService
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService=mockUserService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{throw new Exception()}
        controller.CRUDService=mockCRUDService
        when:
        params.emailConfiguration=[subject:"email",body:"rx",noEmailOnNoData:false,isDeleted:false,deliveryReceipt:false]
        params.emailToUsers="test@rxlogix.com"
        controller.enable(etlSchedule)
        then:
        response.status==200
    }

    void "test initialize success"(){
        given:
        EtlSchedule etlSchedule=new EtlSchedule(isInitial: false)
        def mockEtlJobService=Mock(EtlJobService)
        mockEtlJobService.getSchedule()>>{return etlSchedule}
        mockEtlJobService.initalize(_)>>{}
        controller.etlJobService=mockEtlJobService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{return etlSchedule}
        controller.CRUDService=mockCRUDService
        when:
        controller.initialize()
        then:
        response.status==302
        response.redirectedUrl=="/etlSchedule/index"
    }

    void "test initialize exception"(){
        given:
        EtlSchedule etlSchedule=new EtlSchedule(isInitial: false)
        def mockEtlJobService=Mock(EtlJobService)
        mockEtlJobService.getSchedule()>>{return etlSchedule}
        mockEtlJobService.initalize(_)>>{}
        controller.etlJobService=mockEtlJobService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{throw new Exception()}
        controller.CRUDService=mockCRUDService
        when:
        controller.initialize()
        then:
        response.status==302
        response.redirectedUrl=="/etlSchedule/index"
    }

    void "test getEtlStatus"(){
        given:
        EtlStatus etlStatus=new EtlStatus(status: EtlStatusEnum.FAILED)
        def mocketlJobService=Mock(EtlJobService)
        mocketlJobService.getEtlStatus()>>{return etlStatus}
        controller.etlJobService=mocketlJobService
        when:
        controller.getEtlStatus()
        then:
        response.status==200
    }

    void "test getPreMartEtlStatus"(){
        given:
        EtlStatus etlStatus=new EtlStatus(status: EtlStatusEnum.FAILED)
        def mocketlJobService=Mock(EtlJobService)
        mocketlJobService.getPreMartEtlStatus()>>{return etlStatus}
        mocketlJobService.checkPreMartEtlStatusApplicable()>>{return true}
        controller.etlJobService=mocketlJobService
        when:
        controller.getPreMartEtlStatus()
        then:
        response.status==200
    }
}

