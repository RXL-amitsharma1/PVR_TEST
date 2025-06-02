package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.caseSeries.integration.ExecutedCaseSeriesDTO
import com.rxlogix.dto.caseSeries.integration.ExecutedDateRangeInfoDTO
import com.rxlogix.dto.caseSeries.integration.ParameterValueDTO
import com.rxlogix.dto.caseSeries.integration.QueryValueListDTO
import com.rxlogix.enums.*
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges
import com.rxlogix.util.ViewHelper

@ConfineMetaClassChanges([GrailsHibernateUtil, User])
class CaseSeriesServiceSpec extends Specification implements DataTest, ServiceUnitTest<CaseSeriesService> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, ReportField, SuperQuery, ParameterValue, CaseSeries, QueryValueList, CaseSeriesDateRangeInformation, Tag, CaseDeliveryOption, EmailConfiguration, ExecutionStatus, ExecutedCaseSeries, ReportResult, DateRangeType, ExecutedQueryValueList, ExecutedReportConfiguration, ExecutedPeriodicReportConfiguration, ExecutedCaseDeliveryOption, CaseSeriesUserState, ExecutedCaseSeriesUserState, ExecutedTemplateQuery, ReportTemplate, DataTabulationTemplate
        GrailsHibernateUtil.metaClass.static.unwrapIfProxy = {Object instance -> return instance}
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


    def "test create executed case series"() {
        given:
        new User(username: 'admin').save(flush: true, validate: false)
        service.queryService = Stub(QueryService) {
            createExecutedQuery(_) >> new ExecutedQuery()
        }
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { theInstance -> theInstance }
        service.CRUDService = crudServiceMock.proxyInstance()
        Stub(CaseSeriesService) {
            createExecutedCaseDeliveryOption(_) >> new ExecutedCaseDeliveryOption()
        }

        ExecutedCaseSeriesDTO executedCaseSeriesDTO = new ExecutedCaseSeriesDTO(seriesName: seriesName, suspectProduct: suspectProduct, ownerName: 'admin',executedCaseSeriesDateRangeInformation: new ExecutedDateRangeInfoDTO())

        when:
        ExecutedCaseSeries executedCaseSeries = service.createExecutedCaseSeries(executedCaseSeriesDTO)

        then:
        executedCaseSeries.seriesName == caseSeriesName
        executedCaseSeries.suspectProduct == caseSeriesSuspectProduct

        where:
        seriesName    |    suspectProduct ||  caseSeriesName | caseSeriesSuspectProduct
        "TestSeries"  |    false          ||  "TestSeries"   |    false
        "TestSeries1" |    true           ||  "TestSeries1"  |    true

    }

    void "test updateDetailsFrom"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executedGlobalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])])
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockQueryService = new MockFor(QueryService)
        mockQueryService.demand.createExecutedQuery(0..1){SuperQuery query ->
            run = true
            return superQuery
        }
        service.queryService = mockQueryService.proxyInstance()
        when:
        ExecutedCaseSeries executedCaseSeriesInstance = service.updateDetailsFrom(new CaseSeries(seriesName:"seriesName",tenantId:1,description:"description",dateRangeType: new DateRangeType(), asOfVersionDate: new Date(),evaluateDateAs: EvaluateCaseDateEnum.ALL_VERSIONS,excludeFollowUp:true,includeLockedVersion :true,includeAllStudyDrugsCases :true,excludeNonValidCases:true,excludeDeletedCases: true, suspectProduct :false,productSelection :"productSelection",productGroupSelection:"productGroupSelection",studySelection :"studySelection",eventSelection:"eventSelection",eventGroupSelection:"eventGroupSelection",numExecutions :1,qualityChecked:false,owner:normalUser, locale: new Locale("en"),deliveryOption: new CaseDeliveryOption(sharedWith: [normalUser],sharedWithGroup: [new UserGroup(name: "userGroup")],attachmentFormats: [ReportFormatEnum.PDF],emailToUsers: ["abc@gmail.com"]),tags: [new Tag(name: "Tag")],caseSeriesDateRangeInformation: new CaseSeriesDateRangeInformation(dateRangeEnum: DateRangeEnum.CUMULATIVE,relativeDateRangeValue: 1),globalQueryValueLists: [new QueryValueList(query: new SuperQuery(),parameterValues: [new QueryExpressionValue(reportField: new ReportField(),operator: QueryOperatorEnum.CONTAINS,key: "key",value: "value")])],globalQuery: new SuperQuery())
                ,executedCaseSeries)
        then:
        run == true
        executedCaseSeriesInstance.executedGlobalQueryValueLists.size() == 1
    }

    void "test copyCaseSeries"(){
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "Copy of" }
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        service.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){caseSeriesInstance, Map saveParams = null ->
            run = true
            return caseSeriesInstance
        }
        service.CRUDService = mockCRUDService.proxyInstance()
        when:
        CaseSeries caseSeries = service.copyCaseSeries(new CaseSeries(seriesName:"seriesName",tenantId:1,description:"description",dateRangeType: new DateRangeType(), asOfVersionDate: new Date(),evaluateDateAs: EvaluateCaseDateEnum.ALL_VERSIONS,excludeFollowUp:true,includeLockedVersion :true,includeAllStudyDrugsCases :true,excludeNonValidCases:true, excludeDeletedCases: true ,suspectProduct :false,productSelection :"productSelection",productGroupSelection:"productGroupSelection",studySelection :"studySelection",eventSelection:"eventSelection",eventGroupSelection:"eventGroupSelection",numExecutions :1,qualityChecked:false,owner:normalUser, locale: new Locale("en"),deliveryOption: new CaseDeliveryOption(sharedWith: [normalUser],sharedWithGroup: [new UserGroup(name: "userGroup")],attachmentFormats: [ReportFormatEnum.PDF],emailToUsers: ["abc@gmail.com"]),tags: [new Tag(name: "Tag")],caseSeriesDateRangeInformation: new CaseSeriesDateRangeInformation(dateRangeEnum: DateRangeEnum.CUMULATIVE,relativeDateRangeValue: 1),globalQueryValueLists: [new QueryValueList(query: new SuperQuery(),parameterValues: [new QueryExpressionValue(reportField: new ReportField(),operator: QueryOperatorEnum.CONTAINS,key: "key",value: "value")])],globalQuery: new SuperQuery()))
        then:
        run == true
        caseSeries.globalQueryValueLists.size() == 1
        caseSeries.seriesName == "Copy of seriesName"
    }

//    void "test deleteTemporaryCaseSeries"(){
//        int run = 0
//        User normalUser = makeNormalUser("user",[])
//        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(isTemporary: true,lastUpdated: new Date()-10,owner: normalUser,tenantId: 1)
//        executedCaseSeries.save(failOnError:true,validate:false,flush:true)
//        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(cumulativeCaseSeries: executedCaseSeries,caseSeries: executedCaseSeries)
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        def mockNotificationService = new MockFor(NotificationService)
//        mockNotificationService.demand.deleteNotification(0..1){ long idVal, NotificationApp appNameVal->
//            run++
//        }
//        service.notificationService = mockNotificationService.proxyInstance()
//        def mockReportExecutorService = new MockFor(ReportExecutorService)
//        mockReportExecutorService.demand.removePreviewCaseSeries(0..1){ExecutedCaseSeries executedCaseSeriesInstance, User user->
//            run++
//        }
//        service.reportExecutorService = mockReportExecutorService.proxyInstance()
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.updateWithoutAuditLog(0..2){caseSeriesInstance, Map saveParams = null ->
//            run++
//            return caseSeriesInstance
//        }
//        mockCRUDService.demand.delete(0..1){theInstance ->
//            run++
//        }
//        service.CRUDService = mockCRUDService.proxyInstance()
//        ExecutedCaseSeries.metaClass.static.findAllByIsTemporaryAndLastUpdatedLessThanEquals = {boolean isTemporary,Date lastUpdated,Map paginateParams -> [executedCaseSeries]}
//        when:
//        service.deleteTemporaryCaseSeries()
//        then:
//        run == 4
//    }

    void "test shareExecutedCaseSeries"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ExecutedCaseDeliveryOption executedCaseDeliveryOption = new ExecutedCaseDeliveryOption()
        executedCaseDeliveryOption.setVersion(1)
        executedCaseDeliveryOption.save(failOnError:true,validate:false)
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executedDeliveryOption: executedCaseDeliveryOption)
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        service.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveWithoutAuditLog(0..1){executedCaseDeliveryOptionInstance, Map saveParams = null ->
            run = true
            return executedCaseDeliveryOptionInstance
        }
        service.CRUDService = mockCRUDService.proxyInstance()
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.updateAuditLogShareWith(0..1){executedCaseDeliveryOptionInstance, usrList, usrGpList, exCaseSeries, isCaseSeries ->
            return executedCaseDeliveryOptionInstance
        }
        service.reportService = mockReportService.proxyInstance()
        when:
        def result = service.shareExecutedCaseSeries([sharedWith:"UserGroup_${userGroup.id};User_${normalUser.id}"],executedCaseSeries)
        then:
        run == true
        result.size() == 3
    }

    void "test addExecutedGlobalQueryValueLists"(){
        boolean run = false
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executedDeliveryOption: new ExecutedCaseDeliveryOption())
        executedCaseSeries.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        def mockQueryService = new MockFor(QueryService)
        mockQueryService.demand.createExecutedQuery(0..1){SuperQuery query ->
            run = true
            return superQuery
        }
        service.queryService = mockQueryService.proxyInstance()
        when:
        service.addExecutedGlobalQueryValueLists(executedCaseSeries,[new QueryValueListDTO(parameterValues: [new ParameterValueDTO(reportFieldName: "report",operator: "operator",value: "value",key: "key")])])
        then:
        run == true
        executedCaseSeries.executedGlobalQueryValueLists.size() == 1
    }

    void "test createDateRangeInformation"(){
        when:
        ExecutedCaseSeriesDateRangeInformation executedCaseSeriesDateRangeInformation = service.createDateRangeInformation(new ExecutedDateRangeInfoDTO(relativeDateRangeValue: 1,dateRangeEnum: DateRangeEnum.TOMORROW,dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10),new Date())
        then:
        executedCaseSeriesDateRangeInformation.dateRangeStartAbsolute != null
        executedCaseSeriesDateRangeInformation.dateRangeEndAbsolute != null
    }

    void "test createDateRangeInformation CUMULATIVE"(){
        when:
        ExecutedCaseSeriesDateRangeInformation executedCaseSeriesDateRangeInformation = service.createDateRangeInformation(new ExecutedDateRangeInfoDTO(relativeDateRangeValue: 1,dateRangeEnum: DateRangeEnum.CUMULATIVE,dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10),new Date())
        then:
        executedCaseSeriesDateRangeInformation.dateRangeStartAbsolute != null
        executedCaseSeriesDateRangeInformation.dateRangeEndAbsolute != null
    }

    void "test createDateRangeInformation CUSTOM"(){
        when:
        ExecutedCaseSeriesDateRangeInformation executedCaseSeriesDateRangeInformation = service.createDateRangeInformation(new ExecutedDateRangeInfoDTO(relativeDateRangeValue: 1,dateRangeEnum: DateRangeEnum.CUSTOM,dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10),new Date())
        then:
        executedCaseSeriesDateRangeInformation.dateRangeStartAbsolute != null
        executedCaseSeriesDateRangeInformation.dateRangeEndAbsolute != null
    }

    void "test createExecutedCaseDeliveryOption"(){
        User normalUser = makeNormalUser("user",[])
        when:
        ExecutedCaseDeliveryOption executedCaseDeliveryOption = service.createExecutedCaseDeliveryOption(normalUser, null, null)
        then:
        executedCaseDeliveryOption.sharedWith  == [normalUser]
        executedCaseDeliveryOption.sharedWithGroup == []
    }

    void "test createExecutionStatus"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser]),seriesName: "series",tenantId: 1,numExecutions: 1)
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
            run = true
            theInstance.save(failOnError: true,validate:false)
        }
        service.CRUDService = mockCRUDService.proxyInstance()
        when:
        service.createExecutionStatus(executedCaseSeries, ExecutingEntityTypeEnum.EXCECUTED_CASESERIES)
        then:
        run == true
        ExecutionStatus.count() == 1
    }

    void "test createExecutedCaseSeries"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "series",tenantId: 1,numExecutions: 1)
        executedCaseSeries.save(failOnError:true,validate:false)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        DateRangeType dateRangeType = new DateRangeType(name: "name")
        dateRangeType.save(failOnError:true,validate:false,flush:true)
        def mockQueryService = new MockFor(QueryService)
        mockQueryService.demand.createExecutedQuery(0..2){SuperQuery query ->
            run++
            return superQuery
        }
        service.queryService = mockQueryService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){caseSeriesInstance, Map saveParams = null ->
            run++
            return caseSeriesInstance
        }
        service.CRUDService = mockCRUDService.proxyInstance()
        when:
        ExecutedCaseSeries executedCaseSeriesInstance = service.createExecutedCaseSeries(new ExecutedCaseSeriesDTO(ownerName: "user",seriesName:"seriesName",tenantId:1,description:"description",dateRangeType:"name", asOfVersionDate: new Date(),evaluateDateAs: EvaluateCaseDateEnum.ALL_VERSIONS,excludeFollowUp:true,includeLockedVersion :true,includeAllStudyDrugsCases :true,excludeNonValidCases:true,excludeDeletedCases: true, suspectProduct :false,productSelection :"productSelection",productGroupSelection:"productGroupSelection",studySelection :"studySelection",eventSelection:"eventSelection",eventGroupSelection:"eventGroupSelection",executedCaseSeriesDateRangeInformation: new ExecutedDateRangeInfoDTO(relativeDateRangeValue: 1,dateRangeEnum: DateRangeEnum.CUMULATIVE,dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10),executedGlobalQueryValueLists: [new QueryValueListDTO(parameterValues: [new ParameterValueDTO(reportFieldName: "report",operator: "operator",value: "value",key: "key")])],globalQueryId:superQuery.id ))
        then:
        run == 3
        executedCaseSeriesInstance.executedGlobalQueryValueLists.size() == 1
    }

    void "test setFavorite caseSeries"(){
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(seriesName: "series",tenantId: 1,numExecutions: 1)
        caseSeries.save(failOnError:true,validate:false)
        CaseSeriesUserState caseSeriesUserState = new CaseSeriesUserState(user: normalUser,caseSeries: caseSeries)
        caseSeriesUserState.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        service.userService = mockUserService.proxyInstance()
        when:
        service.setFavorite(caseSeries,true)
        then:
        CaseSeriesUserState.count() == 1
    }

    void "test setFavorite caseSeries null"(){
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(seriesName: "series",tenantId: 1,numExecutions: 1)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        service.userService = mockUserService.proxyInstance()
        when:
        service.setFavorite(caseSeries,false)
        then:
        CaseSeriesUserState.count() == 1
    }

    void "test setFavorite executedCaseSeries"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "series",tenantId: 1,numExecutions: 1)
        executedCaseSeries.save(failOnError:true,validate:false)
        ExecutedCaseSeriesUserState executedCaseSeriesUserState = new ExecutedCaseSeriesUserState(user: normalUser,executedCaseSeries: executedCaseSeries)
        executedCaseSeriesUserState.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        service.userService = mockUserService.proxyInstance()
        when:
        service.setFavorite(executedCaseSeries,true)
        then:
        ExecutedCaseSeriesUserState.count() == 1
    }

    void "test setFavorite executedCaseSeries null"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "series",tenantId: 1,numExecutions: 1)
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        service.userService = mockUserService.proxyInstance()
        when:
        service.setFavorite(executedCaseSeries,false)
        then:
        ExecutedCaseSeriesUserState.count() == 1
    }

    void "test getDateRangeValueForCriteria"(){
        boolean run = false
        Locale locale = new Locale('en')
        def mockCustomMessageService = new MockFor(CustomMessageService)
        mockCustomMessageService.demand.getMessage(0..1){String code, Object... args = null ->
            run = true
            return "message"
        }
        service.customMessageService = mockCustomMessageService.proxyInstance()
        messageSource.addMessage("default.date.format.short",locale,"MM-dd-yyyy")
        when:
        String result = service.getDateRangeValueForCriteria(new ExecutedCaseSeries(executedCaseSeriesDateRangeInformation: new ExecutedCaseSeriesDateRangeInformation(dateRangeEnum: DateRangeEnum.CUSTOM,dateRangeEndAbsolute: new Date()+10,dateRangeStartAbsolute: new Date())),locale)
        then:
        run == true
        result == "message"
    }

    void "test getDateRangeValueForCriteria startdate equals end date"(){
        boolean run = false
        Locale locale = new Locale('en')
        def mockCustomMessageService = new MockFor(CustomMessageService)
        mockCustomMessageService.demand.getMessage(0..1){String code, Object... args = null ->
            run = true
            return "message"
        }
        service.customMessageService = mockCustomMessageService.proxyInstance()
        messageSource.addMessage("default.date.format.short",locale,"MM-dd-yyyy")
        when:
        String result = service.getDateRangeValueForCriteria(new ExecutedCaseSeries(executedCaseSeriesDateRangeInformation: new ExecutedCaseSeriesDateRangeInformation(dateRangeEnum: DateRangeEnum.CUSTOM,dateRangeEndAbsolute: new Date(),dateRangeStartAbsolute: new Date())),locale)
        then:
        run == false
    }

    void "test getDateRangeValueForCriteria CUMULATIVE"(){
        boolean run = false
        Locale locale = new Locale('en')
        def mockCustomMessageService = new MockFor(CustomMessageService)
        mockCustomMessageService.demand.getMessage(0..1){String code, Object... args = null ->
            run = true
            return "message"
        }
        service.customMessageService = mockCustomMessageService.proxyInstance()
        messageSource.addMessage("default.date.format.short",locale,"MM-dd-yyyy")
        when:
        String result = service.getDateRangeValueForCriteria(new ExecutedCaseSeries(executedCaseSeriesDateRangeInformation: new ExecutedCaseSeriesDateRangeInformation(dateRangeEnum: DateRangeEnum.CUMULATIVE,dateRangeEndAbsolute: new Date())),locale)
        then:
        run == true
        result == "message"
    }

    void "test isDrillDownToCaseList"(){
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true,validate:false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(finalReportResult: reportResult)
        executedTemplateQuery.save(failOnError:true,validate:false)
        ReportTemplate reportTemplate = new ReportTemplate()
        reportTemplate.save(failOnError:true,validate:false)

        when:
        boolean result = service.isDrillDownToCaseList(reportResult)
        then:
        result == false
    }

    void "test isDrillDownToCaseList DataTabulationTemplate"() {
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError: true, validate: false)
        DataTabulationTemplate reportTemplate = new DataTabulationTemplate(drillDownToCaseList: true)
        reportTemplate.save(failOnError: true, validate: false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(finalReportResult: reportResult, executedTemplate: reportTemplate)
        executedTemplateQuery.save(failOnError: true, validate: false)
        when:
        boolean result = service.isDrillDownToCaseList(reportResult)
        then:
        result == true
    }

    void "test generateUniqueNameForCaseSeries"(){
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "Copy of" }
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(seriesName: "Copy of series",owner: normalUser)
        caseSeries.save(failOnError:true,validate:false,flush:true)
        when:
        String result = service.generateUniqueNameForCaseSeries("series",normalUser)
        then:
        result == "Copy of series (1)"
    }

    void "test setOwnerAndNameForPreview"(){
        User normalUser = makeNormalUser("user",[])
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        service.userService = mockUserService.proxyInstance()
        when:
        ExecutedCaseSeries executedCaseSeries = service.setOwnerAndNameForPreview(new ExecutedCaseSeries(executedGlobalQuery: new SuperQuery(name: "SuperQuery")))
        then:
        executedCaseSeries.owner == normalUser
        executedCaseSeries.seriesName == "Preview of Query SuperQuery"
    }
}