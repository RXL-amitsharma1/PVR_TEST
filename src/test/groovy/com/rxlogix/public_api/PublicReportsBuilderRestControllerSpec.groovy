package com.rxlogix.public_api

import com.rxlogix.CRUDService
import com.rxlogix.config.*
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ReportField, ExecutionStatus, User, SuperQuery, DateRangeType, ExecutedConfiguration, SourceProfile, ReportTemplate])
class PublicReportsBuilderRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<PublicReportsBuilderRestController> {
    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains ExecutedConfiguration, ExecutedQuery, Query, SourceProfile, ExecutedGlobalDateRangeInformation, DateRangeType, ExecutedCaseLineListingTemplate, CaseLineListingTemplate, ExecutedTemplateQuery, ReportResult, ExecutedQueryExpressionValue, User, ExecutedQueryValueList
    }

    void "test getReportOutputStatus"() {
        given:
        ExecutionStatus.metaClass.static.findByEntityId = { Long id ->
            return new ExecutionStatus(executionStatus: ReportExecutionStatusEnum.GENERATING)
        }
        when:
        controller.getReportOutputStatus(1L)
        then:
        response.json.reportExecutionStatus == "GENERATING"
    }

    void "test getReportOutput when report not ready"() {
        given:
        ExecutedConfiguration.metaClass.static.get = { Long id ->
            return new ExecutedConfiguration(status: ReportExecutionStatusEnum.GENERATING)
        }
        when:
        controller.getReportOutput(1L, "PDF")
        then:
        response.json.reportExecutionStatus == "GENERATING"
    }

    void "test getReportOutput when report is ready"() {
        given:
        ExecutedConfiguration.metaClass.static.get = { Long id ->
            return new ExecutedConfiguration(status: ReportExecutionStatusEnum.COMPLETED)
        }
        controller.dynamicReportService = [
                createMultiTemplateReport: { executedConfiguration, reportFormat ->
                    File f = new File("test.pdf")
                    f.metaClass.getBytes = { -> return null }
                    return f
                },
                getContentType           : { outputFormat -> return "contentType" },
                getReportNameAsFileName  : { executedConfiguration -> return "filename" }
        ]
        when:
        controller.getReportOutput(1L, "PDF")
        then:

        response.status == 200
    }

    void "test createCaseForm"() {
        given:
        User.metaClass.static.findByUsername = { Long id -> return new User(username: "admin") }
        ExecutedConfiguration.metaClass.static.countByReportNameAndOwner = { String s, User u ->
            return 1
        }
        controller.reportsBuilderService = [getRunOnceScheduledDateJson: { o -> return "time" },
                                            createExecutionStatus      : { executedConfiguration, status -> new ExecutionStatus() }
        ]
        SourceProfile.metaClass.static.sourceProfilesForUser = { User owner -> return [new SourceProfile()] }
        ReportTemplate.metaClass.static.findByNameAndOriginalTemplateIdAndIsDeleted = { String templateName, Long l, Boolean b -> new CaseLineListingTemplate() }
        ReportTemplate.metaClass.static.getLatestExRptTempltByOrigTempltId = {  l ->
            return new Object() {
                Long get(){
                    return 1L
                }
            }
        }
        ReportTemplate.metaClass.static.get = { Long id -> return new ExecutedCaseLineListingTemplate() }

        SuperQuery.metaClass.static.findByNameAndOriginalQueryIdAndIsDeleted = { String templateName, Long l, Boolean b -> new Query() }
        SuperQuery.metaClass.static.getLatestExQueryByOrigQueryId = { l ->
            return new Object() {
                Long get() {
                    return 1L
                }
            }
        }
        SuperQuery.metaClass.static.get = { Long id -> return new ExecutedQuery() }
        DateRangeType.metaClass.static.findAllByIsDeletedAndNameNotEqual={Boolean b, String s, Map m-> return [new DateRangeType()] }
        ReportField.metaClass.static.findByName = { String id -> return new ReportField(name: id) }
        User.metaClass.static.findByUsername = { String id -> return new User(username: id) }

        def crudServiceMock = new MockFor(CRUDService)
        def executedConfiguration
        crudServiceMock.demand.save(1) { o ->
            executedConfiguration = o
            return o
        }
        controller.CRUDService = crudServiceMock.proxyInstance()
        when:
        controller.createCaseForm("20230600017:1;20230600018:1", 1L, "PDF")
        then:
        response.status == 200
        response.json.status == "SUCCESS"
    }
}
