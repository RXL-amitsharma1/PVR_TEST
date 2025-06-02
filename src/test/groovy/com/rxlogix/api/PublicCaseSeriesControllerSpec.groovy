package com.rxlogix.api

import com.rxlogix.CaseSeriesService
import com.rxlogix.config.CaseSeries
import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.dto.caseSeries.integration.ExecutedCaseSeriesDTO
import com.rxlogix.public_api.PublicCaseSeriesController
import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

class PublicCaseSeriesControllerSpec extends Specification implements DataTest, ControllerUnitTest<PublicCaseSeriesController> {

    def setupSpec() {
        mockDomains User,CaseSeries,ExecutedCaseSeries
    }

    def "test generateExecutedCaseSeries Public API"() {
        given:
        controller.caseSeriesService = Stub(CaseSeriesService) {
            createExecutedCaseSeries(_ as ExecutedCaseSeriesDTO) >> { ExecutedCaseSeriesDTO theInstance ->
                if (!theInstance.seriesName) {
                    throw new Exception("Validation Exception")
                }
                ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
                executedCaseSeries.id = id
                return executedCaseSeries
            }
        }
        ExecutedCaseSeriesDTO executedCaseSeriesDTO = new ExecutedCaseSeriesDTO(seriesName: seriesName)

        when: 'The generateExecutedCaseSeries action is executed'
        controller.generateExecutedCaseSeries(executedCaseSeriesDTO)

        then: 'Response status and data are correct'
        response.status == 200
        response.json.data == executedCaseSeriesId
        response.json.status == resultStatus
        response.json.message == message

        where:
        seriesName | id   || executedCaseSeriesId | resultStatus | message
        null       | null || null                 | false        | 'Validation Exception'
        'test'     | 12   || 12                   | true         | ''
        'test1'    | 13   || 13                   | true         | ''
    }

    def "test fetchConfiguredCaseSeriesList"() {
        given:
        CaseSeries caseSeries = new CaseSeries(seriesName:"Test Case Series" )
        caseSeries.save(validate:false,failOnError:true,flush :true)
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.getAllConfiguredCaseSeries(0..1){User userInstance,Integer max, Integer offset, String term ->return caseSeries.collect { [id: it.id, text: it.seriesName] } }
        mockCaseSeriesService.demand.countAllConfiguredCaseSeries(0..1){User userInstance,String term -> return 1}
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        def user = new User (username: "user")
        user.save(validate:false,failOnError:true,flush :true)

        when:
        controller.fetchConfiguredCaseSeriesList("user",0, 0, null)

        then:
        response.json.result.size() == 1
        response.json.result[0].id == 1
        response.json.totalCount == 1
    }

    def "test fetchExecutedCaseSeriesList"() {
        given:
        def user = new User (username: "admin")
        user.save(validate:false,failOnError:true,flush :true)
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(id:123,seriesName:"Test Case Series")
        executedCaseSeries.save(validate:false,failOnError:true,flush :true)
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.getAllExeuctedCaseSeriesByUser(0..1){User userInstance,Integer max, Integer offset, String term ->return executedCaseSeries.collect { [id: it.id, text: it.seriesName,caseSeriesOwner:it.caseSeriesOwner,owner:it.owner] } }
        mockCaseSeriesService.demand.countAllExeuctedCaseSeriesByUser(0..1){User userInstance,String term -> return 1}
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()


        when:
        controller.fetchExecutedCaseSeriesList('admin', null, 0, 0)

        then:
        response.json.result.size() == 1
        response.json.result[0].id == 1
        response.json.totalCount == 1
    }

}
