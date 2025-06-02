package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import groovy.time.TimeCategory
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ReportTask, ReportConfiguration, ExecutedReportConfiguration, ViewHelper])
class TaskTemplateServiceSpec extends Specification implements DataTest, ServiceUnitTest<TaskTemplateService> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, ActionItem, ActionItemCategory, Preference, ExecutedPeriodicReportConfiguration, PeriodicReportConfiguration, ReportConfiguration, ExecutedReportConfiguration, ReportTask, Tenant
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


    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..3) { -> user }
        return securityMock.proxyInstance()
    }

    void " test fetchReportTasksFromRequest method"() {
        given:
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "PERIODIC_REPORT").save(validate: false)
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminUser = new User(id: 1, username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        service.userService = makeSecurityService(adminUser)
        when:
        Map params = [:]
        params.aiDescription = ["", "d1", "d2"]
        params.aiAssignedTo = ["", "", "User_1"]
        params.aiDueDateShift = ["", "1", "2"]
        params.aiPriority = ["", "HIGH", "HIGH"]
        params.aiBeforeAfter = ["AFTER", "AFTER", "AFTER"]
        params.sign = ["+", "+", "+"]
        List<ReportTask> result = service.fetchReportTasksFromRequest(new Object() {
            public list(String key) { return params.get(key) }
        })
        then:
        result.size() == 2
        result[1].description == "d2"
        result[1].assignedTo.id == 1
        result[1].dueDateShift == 2
        result[1].actionCategory == actionItemCategory
        result[1].appType == AppTypeEnum.PERIODIC_REPORT
    }

    void " test createActionItems method"() {
        given:
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "something" }

        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminUser = new User(id: 1, username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        service.userService = makeSecurityService(adminUser)
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "PERIODIC_REPORT").save(validate: false)

        def result = []
        PeriodicReportConfiguration configuration = new PeriodicReportConfiguration(owner: adminUser, globalDateRangeInformation: new GlobalDateRangeInformation())
        ExecutedPeriodicReportConfiguration executedConfiguration = new ExecutedPeriodicReportConfiguration(owner: adminUser)
        executedConfiguration.metaClass.addToActionItems = { ai ->
            result << ai
        }
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { theInstance -> theInstance }
        service.CRUDService = crudServiceMock.proxyInstance()
        def actionItemServiceMock = new MockFor(ActionItemService)
        actionItemServiceMock.demand.sendActionItemNotification {ActionItem actionItem, String mode, def oldActionItemRef, def emailSubject -> return }
        service.actionItemService = actionItemServiceMock.proxyInstance()


        when:
        Integer shift = createDateShift
        ReportTask.metaClass.static.listTasksForReportConfiguration = { Long id ->
            new Object() {
                public List list() {
                    [new ReportTask(actionCategory: actionItemCategory, createDateShift: shift, description: "description", dueDateShift: 1, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT)]
                }
            }
        }

        service.createActionItems(configuration, executedConfiguration)
        then:
        result.size() == res
        where:
        createDateShift | res
        0               | 1
        -1              | 0
    }

    void " test createActionItemForScheduledTasks method"() {
        given:
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminUser = new User(id: 1, username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "PERIODIC_REPORT").save(validate: false)
        service.userService = makeSecurityService(adminUser)
        long t = ((new Date()).getTime() / 1000 as Long) * 1000
        Date expected = new Date(t)
        Date expectedPlusOne
        expected.set(minute: 0, second: 0)
        Date run = new Date(expected.getTime())
        Date run2 = new Date(expected.getTime() + (1000 * 60))
        Date runPlusOne
        use(TimeCategory) {
            expected = expected + 1.day
            run = run + 2.days
            run2 = run2 + 3.days
            expectedPlusOne = expected + 1.day
            runPlusOne=run+1.day
        }
        run.set(minute: 0, second: 0, millis: 0)
        PeriodicReportConfiguration configuration = new PeriodicReportConfiguration(owner: adminUser, nextRunDate: run)
        configuration.globalDateRangeInformation=new GlobalDateRangeInformation(  dateRangeStartAbsolute:Date.parse("yyy-MM-dd","2010-01-01"),dateRangeEndAbsolute:Date.parse("yyy-MM-dd","2010-01-30"), dateRangeEnum : DateRangeEnum.CUSTOM)
        configuration.addToReportTasks(new ReportTask(actionCategory: actionItemCategory, description: "description1", dueDateShift: 1, createDateShift: 2, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT, baseDate: ReportTask.BaseDate.CREATION_DATE))
        configuration.addToReportTasks(new ReportTask(actionCategory: actionItemCategory, description: "description2", dueDateShift: 2, createDateShift: 2, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT, baseDate: ReportTask.BaseDate.CREATION_DATE))
        configuration.addToReportTasks(new ReportTask(actionCategory: actionItemCategory, description: "description3", dueDateShift: 1, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT, baseDate: ReportTask.BaseDate.CREATION_DATE))
        configuration.addToReportTasks(new ReportTask(actionCategory: actionItemCategory, description: "description4", dueDateShift: 0, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT, baseDate: ReportTask.BaseDate.CREATION_DATE))
        configuration.addToReportTasks(new ReportTask(actionCategory: actionItemCategory, description: "description5", dueDateShift: -10, createDateShift: 2, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT, baseDate: ReportTask.BaseDate.DUE_DATE))
        configuration.addToReportTasks(new ReportTask(actionCategory: actionItemCategory, description: "description7", dueDateShift: 1, createDateShift: 2, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT, baseDate: ReportTask.BaseDate.AS_OF_DATE))

        PeriodicReportConfiguration configuration2 = new PeriodicReportConfiguration(owner: adminUser, nextRunDate: run2, asOfVersionDate: Date.parse("yyy-MM-dd","2010-02-10"), evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF)
        configuration2.globalDateRangeInformation=new GlobalDateRangeInformation(  dateRangeStartAbsolute:Date.parse("yyy-MM-dd","2010-01-01"),dateRangeEndAbsolute:Date.parse("yyy-MM-dd","2010-01-30"), dateRangeEnum : DateRangeEnum.CUSTOM)
        configuration2.addToReportTasks(new ReportTask(actionCategory: actionItemCategory, description: "description6", dueDateShift: 1, createDateShift: 3, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT, baseDate: ReportTask.BaseDate.CREATION_DATE))
        configuration2.addToReportTasks(new ReportTask(actionCategory: actionItemCategory, description: "description8", dueDateShift: 1, createDateShift: 3, priority: "HIGH", appType: AppTypeEnum.PERIODIC_REPORT, baseDate: ReportTask.BaseDate.AS_OF_DATE))
        ReportConfiguration.metaClass.static.getFetchSheduledConfigurations = {
            new Object() {
                public List list() {
                    [configuration, configuration2]
                }
            }
        }
        List<ActionItem> resultInstance = []
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save(1..6) { theInstance ->
            resultInstance << theInstance
        }
        service.CRUDService = crudServiceMock.proxyInstance()

        def actionItemServiceMock = new MockFor(ActionItemService)
        actionItemServiceMock.demand.sendActionItemNotification(1..6) {ActionItem actionItem, String mode, def oldActionItemRef, def emailSubject -> return }
        service.actionItemService = actionItemServiceMock.proxyInstance()

        when:
        service.createActionItemForScheduledTasks()
        then:
        resultInstance.size() == 6
        resultInstance[0].dueDate.date == expected.date
        resultInstance[0].description == "description1"
        resultInstance[1].dueDate.date == expectedPlusOne.date
        resultInstance[1].description == "description2"
        resultInstance[2].dueDate.format("yyyy-MM-dd")=="2010-01-20"
        resultInstance[2].description == "description5"
        resultInstance[3].dueDate.format("yyyy-MM-dd") == runPlusOne.format("yyyy-MM-dd")
        resultInstance[3].description == "description7"

        resultInstance[4].dueDate.date ==expected.date
        resultInstance[4].description == "description6"
        resultInstance[5].dueDate.format("yyyy-MM-dd")=="2010-02-11"
        resultInstance[5].description == "description8"
    }
}
