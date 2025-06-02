package com.rxlogix.api

import com.rxlogix.ConfigurationService
import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.FrequencyEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SpringSecurityUtils, User, ExecutionStatus, ReportConfiguration])
class ExecutionStatusRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<ExecutionStatusRestController> {

    def setup(){
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains Tenant, User, Role, UserRole, Comment, ReportResult, ExecutionStatus, PeriodicReportConfiguration, ReportConfiguration, ExecutedPeriodicReportConfiguration, ExecutedReportConfiguration, DeliveryOption, Configuration
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

    void "test configurationMapForError"(){
        User normalUser = makeNormalUser("user",[])
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: 1,reportName: "report",reportVersion: 1,nextRunDate: new Date(),endTime: 12,owner: normalUser,
                                                                message: "message",sectionName: "section",executedEntityId: 1)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.configurationMapForError([executionStatus])
        then:
        result.size() == 1
        result[0].size() == 15
    }

    void "test configurationMap"(){
        User normalUser = makeNormalUser("user",[])
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(numOfExecutions: 1,reportName: "report",nextRunDate: new Date(),owner:normalUser,deliveryOption: new DeliveryOption(sharedWith: [normalUser],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]) )
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..1){BaseConfiguration configuration -> FrequencyEnum.MONTHLY}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        def result = controller.configurationMap([reportConfiguration])
        then:
        result[0].size() == 14
    }

    void "test executedConfigurationMap"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(reportName: "report")
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: 1,reportName: "report",reportVersion: 1,nextRunDate: new Date(),endTime: 12,owner: normalUser,
                message: "message",sectionName: "section",executedEntityId: executedReportConfiguration.id)
        executionStatus.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..1){BaseConfiguration configuration -> return FrequencyEnum.RUN_ONCE}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        def result = controller.executedConfigurationMap([executionStatus])
        then:
        result.size() == 1
        result[0].size() == 15
    }

    void "test list"(){
        User normalUser = makeNormalUser("user",[])
        DeliveryOption deliveryOption = new DeliveryOption(sharedWith: [normalUser],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF])
        deliveryOption.save(failOnError:true,validate:false)
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(owner: normalUser,isDeleted: true)
        reportConfiguration.save(failOnError:true,validate:false)
        deliveryOption.report = reportConfiguration
        reportConfiguration.deliveryOption = deliveryOption
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: reportConfiguration.id,reportName: "report",reportVersion: 1,nextRunDate: new Date(),endTime: 12,owner: normalUser,sharedWith: [normalUser],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF],
                message: "message",sectionName: "section",executedEntityId: 1,entityType: ExecutingEntityTypeEnum.CONFIGURATION)
        executionStatus.save(failOnError:true,validate:false)
        ReportConfiguration reportConfigurationInstance = new PeriodicReportConfiguration(numOfExecutions: 1,reportName: "report",nextRunDate: new Date(),owner:normalUser,deliveryOption: new DeliveryOption(sharedWith: [normalUser],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]) )
        reportConfigurationInstance.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.fetchAllViewableByUser = {User user -> new Object(){
                List list(Object o){
                     return [reportConfiguration]
                }
            }
        }
        ReportConfiguration.metaClass.static.fetchAllScheduledConfigurations = { -> new Object(){
                List list(Object o){
                    return [reportConfigurationInstance]
                }
            }
        }
        ExecutionStatus.metaClass.static.fetchAllNonCompletedExecutions = { -> new Object(){
                List list(Object o){
                    return [executionStatus]
                }
            }
        }
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> return true}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..1){BaseConfiguration configuration -> FrequencyEnum.MONTHLY}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.list()
        then:
        response.json.size() == 2
        response.json[1].size() == 15
        response.json[0].size() == 14
        response.json[1].owner == "user"
        response.json[0].owner == "user"
    }

    void "test list admin user"(){
        User adminUser = makeAdminUser()
        DeliveryOption deliveryOption = new DeliveryOption(sharedWith: [adminUser],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF])
        deliveryOption.save(failOnError:true,validate:false)
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(owner: adminUser,numOfExecutions: 1,reportName: "report",nextRunDate: new Date(),deliveryOption: deliveryOption)
        reportConfiguration.save(failOnError:true,validate:false)
        deliveryOption.report = reportConfiguration
        reportConfiguration.deliveryOption = deliveryOption
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",reportVersion: 1,nextRunDate: new Date(),endTime: 12,owner: adminUser,sharedWith: [adminUser],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF],
                message: "message",sectionName: "section",executedEntityId: 1)
        executionStatus.save(failOnError:true,validate:false)
        ReportConfiguration.metaClass.static.fetchAllScheduledConfigurations = { -> new Object(){
                List list(Object o){
                    return [reportConfiguration]
                }
            }
        }
        ExecutionStatus.metaClass.static.fetchAllNonCompletedExecutions = { -> new Object(){
                List list(Object o){
                    return [executionStatus]
                }
            }
        }
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> return true}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..1){BaseConfiguration configuration -> FrequencyEnum.MONTHLY}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.list()
        then:
        response.json.size() == 2
        response.json[1].size() == 15
        response.json[0].size() == 14
        response.json[0].owner == "Peter Fletcher"
        response.json[1].owner == "Peter Fletcher"
    }

    void "test listAllResults"(){
        User normalUser = makeNormalUser("user",[])
        DeliveryOption deliveryOption = new DeliveryOption(sharedWith: [normalUser],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF])
        deliveryOption.save(failOnError:true,validate:false)
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(owner: normalUser,isDeleted: true)
        reportConfiguration.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(reportName: "report",nextRunDate: new Date())
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: reportConfiguration.id,reportName: "report",reportVersion: 1,nextRunDate: new Date(),endTime: 12,owner: normalUser,attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF],sharedWith: [normalUser],
                message: "message",sectionName: "section",executedEntityId: 1,entityType: ExecutingEntityTypeEnum.CONFIGURATION)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus executionStatusCompleted = new ExecutionStatus(entityId: 20,reportName: "report",reportVersion: 2,nextRunDate: new Date(),endTime: 12,owner: normalUser,
                message: "message",sectionName: "section",executedEntityId: executedReportConfiguration.id,executionStatus: ReportExecutionStatusEnum.COMPLETED,sharedWith: [normalUser],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF])
        executionStatusCompleted.save(failOnError:true,validate:false)

        ReportConfiguration reportConfigurationInstance = new PeriodicReportConfiguration(numOfExecutions: 2,reportName: "report_1",nextRunDate: new Date(),owner:normalUser,deliveryOption: deliveryOption)
        reportConfigurationInstance.save(failOnError:true,validate:false)
        deliveryOption.report = reportConfigurationInstance
        reportConfigurationInstance.deliveryOption = deliveryOption
        ExecutionStatus.metaClass.static.fetchAllCompletedExecutions = { -> new Object(){
                List list(){
                    return [executionStatusCompleted]
                }
            }
        }
        ReportConfiguration.metaClass.static.fetchAllViewableByUser = {User user -> new Object(){
                List list(Object o){
                    return [reportConfiguration]
                }
            }
        }
        ReportConfiguration.metaClass.static.fetchAllScheduledConfigurations = { -> new Object(){
                List list(Closure c){
                    return [reportConfigurationInstance]
                }
            }
        }
        ExecutionStatus.metaClass.static.fetchAllNonCompletedExecutions = { -> new Object(){
                List list(Object o){
                    return [executionStatus]
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUserAdmin(0..1){ -> return false}
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..2){BaseConfiguration configuration -> return FrequencyEnum.RUN_ONCE}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.listAllResults()
        then:
        response.json != null
    }

    void "test listAllResults admin user"(){
        User adminUser = makeAdminUser()
        DeliveryOption deliveryOption = new DeliveryOption(sharedWith: [adminUser])
        deliveryOption.save(failOnError:true,validate:false)
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(owner: adminUser,isDeleted: true)
        reportConfiguration.save(failOnError:true,validate:false)
        deliveryOption.report = reportConfiguration
        reportConfiguration.deliveryOption = deliveryOption
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",reportVersion: 1,nextRunDate: new Date(),endTime: 12,owner: adminUser,
                message: "message",sectionName: "section",executedEntityId: 1)
        executionStatus.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(reportName: "report")
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatusCompleted = new ExecutionStatus(entityId: 2,reportName: "report",reportVersion: 2,nextRunDate: new Date(),endTime: 12,owner: adminUser,
                message: "message",sectionName: "section",executedEntityId: executedReportConfiguration.id,executionStatus: ReportExecutionStatusEnum.COMPLETED)
        executionStatusCompleted.save(failOnError:true,validate:false)
        ReportConfiguration reportConfigurationInstance = new PeriodicReportConfiguration(numOfExecutions: 2,reportName: "report_1",nextRunDate: new Date(),owner:adminUser,deliveryOption: new DeliveryOption(sharedWith: [adminUser],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),isDeleted: false )
        reportConfigurationInstance.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllCompletedExecutions = { -> new Object(){
                List list(Object o){
                    return [executionStatusCompleted]
                }
            }
        }
        ReportConfiguration.metaClass.static.fetchAllScheduledConfigurations = { -> new Object(){
                List list(Object o){
                    return [reportConfigurationInstance]
                }
            }
        }
        ExecutionStatus.metaClass.static.fetchAllNonCompletedExecutions = { -> new Object(){
                List list(Object o){
                    return [executionStatus]
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> adminUser}
        mockUserService.demand.getCurrentUserAdmin(0..1){ -> return true}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..2){BaseConfiguration configuration -> return FrequencyEnum.RUN_ONCE}
        controller.configurationService = mockConfigurationService.proxyInstance()
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> return true}
        when:
        controller.listAllResults()
        then:
        response.json.size() == 3
        response.json[1].size() == 15
        response.json[2].size() == 15
        response.json[0].size() == 14
        response.json[0].owner == "Peter Fletcher"
        response.json[1].owner == "Peter Fletcher"
        response.json[2].owner == "Peter Fletcher"
    }

    void "test getUserViewableConfigurationList"(){
        User normalUser = makeNormalUser("user",[])
        ReportConfiguration reportConfigurationInstance = new PeriodicReportConfiguration(nextRunDate: new Date(),deliveryOption: new DeliveryOption(sharedWith: [normalUser]),reportName: "report",numOfExecutions: 1)
        reportConfigurationInstance.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.fetchAllViewableByUser = {User user -> new Object(){
                List list(Object o){
                    return [reportConfigurationInstance]
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        def result = controller.invokeMethod('getUserViewableConfigurationList', [] as Object[])
        then:
        result == [reportConfigurationInstance]
    }
}
