package com.rxlogix

import com.rxlogix.cmis.AdapterInterface
import com.rxlogix.config.*
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.SensitivityLabelEnum
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

class DmsServiceSpec extends Specification implements DataTest, ServiceUnitTest<DmsService> {
    def emailService
    def dynamicReportService

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains Configuration, User, ApplicationSettings
    }

    def "email should be send if the file updated"() {
        when:
        def dynamicReportServiceMock = new MockFor(DynamicReportService)
        dynamicReportServiceMock.demand.createMultiTemplateReport(1) { ExecutedReportConfiguration executedConfigurationInstance, Map params -> return new File("test") }
        service.dynamicReportService = dynamicReportServiceMock.proxyInstance()

        List<String> recipientsList
        def emailService = new EmailService()
        emailService.metaClass.sendEmail = { def recipients, def emailBodyMessage, boolean asyVal, String emailSubject, String[] emailCc = [] ->
            recipientsList = recipients
        }
        service.emailService = emailService
        service.grailsApplication.config.put('grails', [appBaseURL: "url"])
        def config = new ExecutedConfiguration()
        String testEmail = "test@test.com"
        config.executedDeliveryOption = new ExecutedDeliveryOption(sharedWith: [new User(email: testEmail)])
        config.owner = new User(fullName: "test")
        config.dmsConfiguration = new DmsConfiguration()
        config.dmsConfiguration.sensitivityLabel = SensitivityLabelEnum.PUBLIC
        config.dmsConfiguration.format = ReportFormatEnum.HTML
        service.adapter = new AdapterInterface() {
            @Override
            void init(Object settings) {

            }

            @Override
            void load(File reportFile, String subfolder, String name, String description, String tag, String sensitivity, String author, Object object) {
            //Assuming success.
            }

            @Override
            List<String> getFolderList(String folder, Object object) {
                return null
            }
        }
        ViewHelper.getMessage(_) >> { "test" }
        service.uploadReport(config)

        then:
        recipientsList[0] == testEmail
    }
}
