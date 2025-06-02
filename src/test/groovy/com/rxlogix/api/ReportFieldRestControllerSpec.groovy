package com.rxlogix.api

import com.rxlogix.ReportFieldService
import com.rxlogix.config.ActionItem
import com.rxlogix.config.ActionItemCategory
import com.rxlogix.config.DrilldownCLLData
import com.rxlogix.config.ReportField
import com.rxlogix.config.Tenant
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class ReportFieldRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportFieldRestController> {
    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomain ReportField
    }

    void "test index action, it renders the index page"() {

        given: "Mock Report Field Service"
        def reportFieldService = Mock(ReportFieldService)
        controller.reportFieldService = reportFieldService

        when: "Call index action"
        controller.index()

        then: "Render total records in Report Field"
        1 * reportFieldService.fetchReportField(params) >> [['f1', 'f2'],10, 5]
        response.text == '{"aaData":["f1","f2"],"recordsTotal":5,"recordsFiltered":10}'
    }

    void "test sanitizing pagination params"() {
        given: "Mock SanitizePaginationAttributes"
        params.length = length
        params.start = start
        params.sort = sort
        params.direction = direction
        params.searchString = searchString

        when:
        controller.sanitize(params)

        then:
        println params
        params.max == resultLength
        params.offset == resultStart
        params.sort == resultSort
        params.order == resultDir
        params.searchString == resultSearchString

        where:
        length | start | sort          | direction | searchString || resultLength | resultStart | resultSort    | resultDir | resultSearchString
        10     | 2     | 'name'        | 'asc'     | 'event'      || 10           | 2           | 'name'        | 'asc'     | 'event'
        20     | 1     | 'description' | 'desc'    | 'product'    || 20           | 1           | 'description' | 'desc'    | 'product'
        null   | null  | 'name'        | ''        | ''           || 50           | 0           | 'name'        | 'desc'    | ''
        ''     | ''    | ''            | 'asc'     | null         || 50           | 0           | 'dateCreated' | 'asc'     | null
    }
}
