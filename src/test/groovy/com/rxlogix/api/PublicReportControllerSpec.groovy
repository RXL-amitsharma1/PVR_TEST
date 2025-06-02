package com.rxlogix.api

import com.rxlogix.ReportService
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.dto.reports.integration.ExecutedConfigurationSharedWithDTO
import com.rxlogix.public_api.PublicReportController
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

class PublicReportControllerSpec extends Specification implements DataTest, ControllerUnitTest<PublicReportController> {


    def setupSpec() {
        mockDomains ExecutedPeriodicReportConfiguration, ExecutedReportConfiguration
    }

    def "test updateReport"() {
        given:
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration( reportName: 'Test Ex Report')
        executedReportConfiguration.save(validate: false, failOnError: true, flush: true)
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.updateExecutedConfiguration {ExecutedReportConfiguration exReportConfiguration, List sharedWithUser, List sharedWithGroup -> return executedReportConfiguration.id }
        controller.reportService =  mockReportService.proxyInstance()

        when:
        controller.updateSharedWith(new ExecutedConfigurationSharedWithDTO(exConfigId:executedReportConfiguration.id))

        then:
        response.status == 200
        response.json.data == executedReportConfiguration.id
    }
}