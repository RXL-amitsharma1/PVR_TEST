package com.rxlogix

import com.rxlogix.config.AutoReasonOfDelay
import com.rxlogix.config.SourceProfile
import com.rxlogix.config.SuperQuery
import com.rxlogix.config.Tenant
import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SourceProfile, SuperQuery, AutoReasonOfDelay])
class AutoReasonOfDelayControllerSpec extends Specification implements DataTest, ControllerUnitTest<AutoReasonOfDelayController>{

    private Tenant getTenant() {
        def tenant = Tenant.get(1L)
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = 1L
        return tenant.save()
    }

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains AutoReasonOfDelay, SuperQuery, SourceProfile
    }


    void "test index"() {
        given:
        AutoReasonOfDelay.metaClass.static.findAll = {
            autoReasonOfDelayList
        }
        when:
        controller.index()
        then:
        response.status == 302
        where:
        autoReasonOfDelayList << [[new AutoReasonOfDelay(id : 1L), new AutoReasonOfDelay(id : 1L)],[]]
    }

    void "test create"() {
        given:
        SuperQuery superQuery = new SuperQuery(id: 1L, hasBlanks: true)
        def mockReportExecutorService=Mock(ReportExecutorService)
        mockReportExecutorService.getLateListForOwnerApp(_) >> {[textDesc: 'Late', ownerApp: 'PVC', lateType: 'Late']}
        mockReportExecutorService.getRootCauseList() >> {[textDesc: 'Late', ownerApp: 'PVC', lateType: 'Late']}
        mockReportExecutorService.getRootCauseClassList() >> {[textDesc: 'Late', ownerApp: 'PVC', lateType: 'Late']}
        mockReportExecutorService.getRootCauseSubCategoryList() >> {[textDesc: 'Late', ownerApp: 'PVC', lateType: 'Late']}
        mockReportExecutorService.getResponsiblePartyList() >> {[textDesc: 'Late', ownerApp: 'PVC', lateType: 'Late']}
        controller.reportExecutorService=mockReportExecutorService
        def mockAutoReasonOfDelayService=Mock(AutoReasonOfDelayService)
        mockAutoReasonOfDelayService.fetchConfigurationMapFromSession(_,_) >> [autoReasonOfDelayParams: null, queryRCAIndex:[index:2, type:"query"]]
        controller.autoReasonOfDelayService=mockAutoReasonOfDelayService
        SuperQuery.metaClass.static.get = { def q ->
            superQuery
        }
        SourceProfile.metaClass.static.sourceProfilesForUser = { User user ->
            [new SourceProfile(id:1L)]
        }
        when:
        params.selectedQuery = superQuery
        then:
        response.status==200

    }

    void "test edit"(){
        given:
        AutoReasonOfDelay.metaClass.static.read = { def autoROD ->
            [new AutoReasonOfDelay(id : 1L)]
        }
        def mockAutoReasonOfDelayService=Mock(AutoReasonOfDelayService)
        mockAutoReasonOfDelayService.fetchConfigurationMapFromSession(_,_) >> [autoReasonOfDelayParams: null, queryRCAIndex:[index:2, type:"query"]]
        controller.autoReasonOfDelayService=mockAutoReasonOfDelayService
        def mockReportExecutorService=Mock(ReportExecutorService)
        mockReportExecutorService.getLateListForOwnerApp(_) >> {[textDesc: 'Late', ownerApp: 'PVC', lateType: 'Late']}
        mockReportExecutorService.getRootCauseList() >> {[textDesc: 'Late', ownerApp: 'PVC', lateType: 'Late']}
        mockReportExecutorService.getRootCauseClassList() >> {[textDesc: 'Late', ownerApp: 'PVC', lateType: 'Late']}
        mockReportExecutorService.getRootCauseSubCategoryList() >> {[textDesc: 'Late', ownerApp: 'PVC', lateType: 'Late']}
        mockReportExecutorService.getResponsiblePartyList() >> {[textDesc: 'Late', ownerApp: 'PVC', lateType: 'Late']}
        controller.reportExecutorService=mockReportExecutorService
        SourceProfile.metaClass.static.sourceProfilesForUser = { User user ->
            [new SourceProfile(id:1L)]
        }
        when:
        params.id = 1L
        then:
        response.status==200
    }

    void "test save"() {
        given:
        AutoReasonOfDelay autoReasonOfDelay = new AutoReasonOfDelay()
        autoReasonOfDelay.nextRunDate = null
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return normalUser }
        controller.userService = mockUserService
        autoReasonOfDelay.metaClass.static.populateModel = { Long id ->
            1L
        }
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> { true }

        when:
        controller.save()

        then:
        response.status==302

    }

    void "test update"() {
        given:
        AutoReasonOfDelay autoReasonOfDelay = new AutoReasonOfDelay()
        autoReasonOfDelay.nextRunDate = null
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return normalUser }
        controller.userService = mockUserService
        autoReasonOfDelay.metaClass.static.populateModel = { Long id ->
            1L
        }
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> { true }

        when:
        controller.update()

        then:
        response.status==302

    }

    void "test createQuery"(){
        when:
        params.id = 1L
        params.queryRCAIndex = 2L
        request.method = methodVal
        controller.createQuery()

        then:
        response.status == 302
        response.redirectedUrl == urlVal

        where:
        methodVal | urlVal
        'GET'     | '/autoReasonOfDelay/index'
        'PUT'     | '/query/create'
    }

    void "jobExecutionHistory"(){
        when:
        controller.jobExecutionHistory()

        then:
        response.status == 200
    }

    void "test fetchEvaluateCaseDatesForDatasource"(){
        given:
        SourceProfile sourceProfile = new SourceProfile(sourceId: 1L, sourceName: 'testName')
        sourceProfile.save(failOnError:true,validate:false,flush:true)
        SourceProfile.metaClass.static.read = {Long id -> sourceProfile}

        when:
        controller.fetchEvaluateCaseDatesForDatasource(1L)

        then:
        response.status == 200
        response.json.name == ['LATEST_VERSION']
    }

    void "test fetchEvaluateCaseDatesForDatasource -- null"(){
        given:
        SourceProfile.metaClass.static.read = {null}

        when:
        controller.fetchEvaluateCaseDatesForDatasource(1L)

        then:
        response.status == 200
    }

    void "test fetchEvaluateCaseDateSubmissionForDatasource - latest version only"(){
        given:
        SourceProfile.metaClass.static.read = {Long id -> sourceProfile}

        when:
        controller.fetchEvaluateCaseDateSubmissionForDatasource(idVal)

        then:
        response.status == 200
        response.json.name == value

        where:
        idVal | sourceProfile                                                                            | value
        1L    | new SourceProfile(sourceId: 1L, sourceName: 'testName')                                  | ['LATEST_VERSION', 'VERSION_ASOF_GENERATION_DATE']
        2L    | new SourceProfile(sourceId: 2L, sourceName: 'testName2', includeLatestVersionOnly: true) | ['LATEST_VERSION']
    }

    void "test fetchEvaluateCaseDateSubmissionForDatasource -- null"(){
        given:
        SourceProfile.metaClass.static.read = {null}

        when:
        controller.fetchEvaluateCaseDateSubmissionForDatasource(1L)

        then:
        response.status == 200
    }
}
