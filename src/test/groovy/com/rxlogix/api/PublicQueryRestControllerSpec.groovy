package com.rxlogix.api

import com.rxlogix.QueryService
import com.rxlogix.config.SuperQuery
import com.rxlogix.public_api.PublicQueryRestController
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class PublicQueryRestControllerSpec extends Specification implements ControllerUnitTest<PublicQueryRestController> {

//    def "test get query by name"() {
//        given:
//        controller.queryService = Stub(QueryService) {
//            findQueryByName(queryName) >> { queryName ->
//                if (!queryName) {
//                    return null
//                }
//                SuperQuery superQuery = new SuperQuery()
//                superQuery.name = queryName
//                superQuery.id = id
//                return superQuery
//            }
//        }
//
//        when: 'The getQueryByName action is called'
//        controller.getQueryByName(name)
//
//        then: 'Response status and data are correct'
//        response.status == 200
//        response.json.result.id == id
//        response.json.result.name == name
//
//        where:
//        queryId  |   queryName     ||  id   |  name
//        null     |   null          ||  null |  null
//        12       |   "testQuery1"  ||  12   |  "testQuery1"
//        13       |   "testQuery2"  ||  13   |  "testQuery2"
//
//
//
//    }


    def "test get queries by tagName"() {
        given:
        List dataMap = [
                [id: 1, name: "Test Query 1", modifiedBy: "admin", lastUpdated: new Date(), isDeleted: false, tagName: "PVCM - Workflow"],
                [id: 2, name: "Test Query 2", modifiedBy: "admin", lastUpdated: new Date(), isDeleted: true, tagName: "Data entry"]
        ]
        def queryServiceMock=Mock(QueryService)
        queryServiceMock.fetchQueriesByTag(_) >> { return dataMap}
        controller.queryService=queryServiceMock

        when: 'The getQueryByTag action is called'
        controller.fetchQueriesByTag(tag)

        then: 'Response status and data are correct'
        response.status == 200

        where:
        tag                 |   name
        null                |   null
        "PVCM - Workflow"   |   "Test Query 1"
        "Data entry"        |   "Test Query 2"
    }

}