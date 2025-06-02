package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.user.User
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ComparisonQueue])
class ComparisonControllerSpec extends Specification implements DataTest, ControllerUnitTest<ComparisonController> {

    def setup() {

    }

    def setupSpec() {
        mockDomains ComparisonQueue, ComparisonResult, ExecutedReportConfiguration, ReportConfiguration, ExecutedConfiguration, Configuration
    }

    void "test index"() {
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test listQueue"() {
        given:
        ComparisonQueue.metaClass.static.fetchBySearchString = { String search, ComparisonQueue.Status status1 ->
            new Object() {
                List list(Object o) {
                    [new ComparisonQueue(id: 1, entityId1: 11, entityId2: 12, entityName1: "entityName1", entityName2: "entityName2", entityType: "entityType", status: ComparisonQueue.Status.COMPLETED, dateCompared: new Date(), message: "message"),
                     new ComparisonQueue(id: 2, entityId1: 21, entityId2: 22, entityName1: "entityName1_2", entityName2: "entityName2_2", entityType: "entityType_2", status: ComparisonQueue.Status.COMPLETED, dateCompared: new Date(), message: "message_2")]
                }

                int count(Object o) {
                    return 2
                }
            }
        }
        when:
        params.status = ComparisonQueue.Status.COMPLETED.name()
        params.length = 5
        controller.listQueue()
        then:
        response.json != null
        response.json.size() == 3
        response.json.aaData[0].size() == 10
        response.json.aaData[0].entityId1 == 11
        response.json.aaData[0].entityName1 == "entityName1"


    }

    void "test listResults"() {
        given:
        ComparisonResult.metaClass.static.fetchBySearchString = { String search ->
            new Object() {
                List list(Object o) {
                    [new ComparisonResult(id: 1, entityId1: 11, entityId2: 12, entityName1: "entityName1", entityName2: "entityName2", entityType: "entityType"),
                     new ComparisonResult(id: 2, entityId1: 21, entityId2: 22, entityName1: "entityName1_2", entityName2: "entityName2_2", entityType: "entityType_2")]
                }

                int count(Object o) {
                    return 2
                }
            }
        }
        when:
        params.status = ComparisonQueue.Status.COMPLETED.name()
        params.length = 5
        controller.listResults()
        then:
        response.json != null
        response.json.size() == 3
        response.json.aaData[0].size() == 9
        response.json.aaData[0].entityId1 == 11
        response.json.aaData[0].entityName1 == "entityName1"


    }

    void "test comparison"() {
        given:
        ComparisonService.ComparisonResultDTO res = new ComparisonService.ComparisonResultDTO(reportName1: "reportName1", reportName2: "reportName2")
        ComparisonResult c = new ComparisonResult(id: 1, data: (res as JSON).toString())
        c.save(failOnError: true, validate: false, flush: true)
        when:
        params.id = c.id
        def result = controller.comparison(c.id)
        then:
        response.status == 200
        result.result.reportName1 == "reportName1"

    }

    void "test compare"() {

        given:
        ExecutedConfiguration c = new ExecutedConfiguration(id: 1, reportName: "source1")
        c.save(failOnError: true, validate: false, flush: true)
        c = new ExecutedConfiguration(id: 2, reportName: "source2")
        c.save(failOnError: true, validate: false, flush: true)

        controller.comparisonService = [compareAndSave: { ExecutedConfiguration c1, ExecutedConfiguration c2 -> }]
        when:
        controller.compare(1, 2)
        then:
        response.status == 302
        flash.message != null

    }

    void "test bulkCompare"() {
        given:
        ExecutedConfiguration c = new ExecutedConfiguration(id: 1, reportName: "source1")
        c.save(failOnError: true, validate: false, flush: true)
        c = new ExecutedConfiguration(id: 2, reportName: "source2")
        c.save(failOnError: true, validate: false, flush: true)
        Long iter = 1;
        controller.userService = [currentUser: null]
        controller.comparisonService = [
                createCopy: { ExecutedReportConfiguration exc, User owner, String namePrefix, Date nextRunDate, Boolean runDraft = true, ExecutedCaseSeries useCaseSeries = null ->
                    iter++
                    return new Configuration(id: iter, reportName: "check for name1 v" + iter, numOfExecutions: 1)
                }
        ]
        List resultList = []
        controller.CRUDService = [
                save: { theInstance, Map saveParams = null ->
                    resultList << theInstance
                }
        ]
        when:
        params.prefix = "check for"
        params.ids = "1,2"
        def result = controller.bulkCompare()
        then:
        response.status == 302
        resultList[0].entityName2 == "check for name1 v2"
        resultList[1].entityName2 == "check for name1 v3"
        resultList[0].entityName1 == "source1 v0"
        resultList[1].entityName1 == "source2 v0"
    }

    void "test createCopy"() {
        given:
        Holders.config.report.comparison.prefix = "check for"
        ExecutedConfiguration c = new ExecutedConfiguration(id: 1, reportName: "source1")
        c.save(failOnError: true, validate: false, flush: true)
        controller.userService = [currentUser: null]
        controller.comparisonService = [
                createCopy: { ExecutedReportConfiguration exc, User owner, String namePrefix, Date nextRunDate, Boolean runDraft = true, ExecutedCaseSeries useCaseSeries = null ->
                    return new Configuration(id: 2, reportName: "check for name1 v1", numOfExecutions: 1)
                }
        ]
        List resultList = []
        controller.CRUDService = [
                save: { theInstance, Map saveParams = null ->
                    resultList << theInstance
                }
        ]
        when:
        controller.createCopy(1)
        then:
        response.status == 302
        resultList[0].entityName2 == "check for name1 v1"
        resultList[0].entityName1 == "source1 v0"
    }

}
