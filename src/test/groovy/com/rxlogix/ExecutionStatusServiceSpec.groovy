package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class ExecutionStatusServiceSpec extends Specification implements DataTest, ServiceUnitTest<ExecutionStatusService> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, ExecutedPeriodicReportConfiguration, UserRole, Tenant, Preference, Role, ReportTemplate, ExecutedDateRangeInformation, ExecutedTemplateValueList, ExecutedTemplateQuery, ExecutedDeliveryOption, ExecutedReportConfiguration, ExecutionStatus, WorkflowState, SourceProfile, DateRangeType
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

    private User makeNormalUser(name, team) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_SYSTEM_CONFIGURATION', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
        return normalUser
    }

    void "test generateDraft"(){
        given:
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError:true)
        SourceProfile sourceProfile= new SourceProfile(sourceId: 1,sourceAbbrev: "abv" ,sourceName: "name",sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                ,dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(periodicReportType: PeriodicReportTypeEnum.ACO
                                                                ,executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML]),executionStatus: ReportExecutionStatusEnum.GENERATED_NEW_SECTION,
                                                                executedGlobalQueryValueLists: [new ExecutedQueryValueList()],workflowState: workflowStateInstance,
                                                                clazz: "class",reportName: "report_1",owner: normalUser,createdBy: "user",modifiedBy: "user",
                                                                signalConfiguration: false,tenantId: 1,sourceProfile: sourceProfile)
        executedReportConfiguration.save(failOnError:true,validate :false)
        def mockCRUDService= new MockFor(CRUDService)
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance -> theInstance.save(failOnError: true, flush: true)}
        service.CRUDService = mockCRUDService.proxyInstance()
        when:
        service.generateDraft(executedReportConfiguration)
        then:
        ExecutionStatus.count() == 1
        ExecutionStatus executionStatus = ExecutionStatus.get(1)
        executionStatus.entityId == 1
        executionStatus.reportVersion == 0
        executionStatus.owner == normalUser
        executionStatus.reportName == 'report_1'
        executionStatus.tenantId == 1
    }

    void "test generateAddedSectionReport"(){
        given:
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError:true)
        SourceProfile sourceProfile= new SourceProfile(sourceId: 1,sourceAbbrev: "abv" ,sourceName: "name",sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                ,dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(periodicReportType: PeriodicReportTypeEnum.ACO
                ,executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML]),executionStatus: ReportExecutionStatusEnum.GENERATED_NEW_SECTION,
                executedGlobalQueryValueLists: [new ExecutedQueryValueList()],workflowState: workflowStateInstance,
                clazz: "class",reportName: "report_1",owner: normalUser,createdBy: "user",modifiedBy: "user",
                signalConfiguration: false,tenantId: 1,sourceProfile: sourceProfile)
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE,
                showTemplateFooter: true,createdBy: "user",modifiedBy: "user")
        reportTemplate.save(failOnError:true)
        ExecutedDateRangeInformation executedDateRangeInformation =new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date())
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedDateRangeInformationForTemplateQuery: executedDateRangeInformation,
                executedTemplateValueLists: [new ExecutedTemplateValueList(template: reportTemplate)],executedConfiguration: executedReportConfiguration,createdBy:"user",modifiedBy: "user" )
        executedTemplateQuery.save(failOnError:true)
        executedDateRangeInformation.executedTemplateQuery = executedTemplateQuery
        executedDateRangeInformation.save(failOnError:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError:true,validate:false)
        def mockCRUDService= new MockFor(CRUDService)
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance -> theInstance.save(failOnError: true, flush: true)}
        service.CRUDService = mockCRUDService.proxyInstance()
        when:
        service.generateAddedSectionReport(executedTemplateQuery, false)
        then:
        ExecutionStatus.count() == 1
        ExecutionStatus executionStatus = ExecutionStatus.get(1)
        executionStatus.entityId == 1
        executionStatus.reportVersion == 0
        executionStatus.owner == normalUser
        executionStatus.reportName == 'report_1'
        executionStatus.tenantId == 1
        executionStatus.entityType == ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION
    }
}
