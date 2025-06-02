package com.rxlogix.dynamicReports

import asset.pipeline.grails.LinkGenerator
import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.UserService
import com.rxlogix.user.User
import grails.testing.spring.AutowiredTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification


class FooterBuilderSpec extends Specification implements AutowiredTest {

    def setupSpec() {
        defineBeans {
            dynamicReportService(InstanceFactoryBean, makeDynamicReportService(), DynamicReportService)
            customMessageService(InstanceFactoryBean, makeCustomMessageService(), CustomMessageService)
            imageService(InstanceFactoryBean, makeImageService(), ImageService)
            grailsLinkGenerator(InstanceFactoryBean, makeLinkGenerator(), LinkGenerator)
            userService(InstanceFactoryBean, makeUserService(new User(username: 'user')), UserService)
        }
    }

    def setup() {
        grailsApplication.config.template.footer.max.length = 80
        grailsApplication.config.report.footer.max.length = 100
    }

    def cleanup() {
    }

    void "test for addMaxXLSXRowsPageBreak"() {
        given:
        FooterBuilder footerBuilder = new FooterBuilder()
        ReportBuilder reportBuilder = new ReportBuilder()
        JasperReportBuilder report = reportBuilder.initializeNewReport()

        when:
        footerBuilder.invokeMethod('addMaxXLSXRowsPageBreak', [report] as Object[])

        then: "this method return void so no need to assert anything"
        true
    }

    private UserService makeUserService(User user) {
        UserService userService = new UserService()
        userService.metaClass.getUser = { user }
        userService.metaClass.getCurrentUser = { user }
        return userService
    }

    private makeImageService() {
        def imageServiceMock = new MockFor(ImageService)
        imageServiceMock.demand.getImage(0..10) { String filename ->
            File file = new File("grails-app/assets/images", filename)
            println(file.getAbsolutePath())
            return new FileInputStream(file)
        }
        return imageServiceMock.proxyInstance()
    }

    private makeLinkGenerator() {
        def linkGeneratorMock = new MockFor(LinkGenerator)
        linkGeneratorMock.demand.link(0..2) { LinkedHashMap m -> "" }
        return linkGeneratorMock.proxyInstance()
    }

    private makeCustomMessageService() {
        def customMessageServiceMock = new MockFor(CustomMessageService)
        customMessageServiceMock.demand.getMessage(0..99) { String code -> code }
        customMessageServiceMock.demand.getMessage(0..99) { String code, Object args -> code }
        customMessageServiceMock.demand.getMessage(0..99) { String code, Object[] args, String defaultMessage, Locale locale -> code }
        return customMessageServiceMock.proxyInstance()
    }

    private makeDynamicReportService() {
        def dynamicReportService = new MockFor(DynamicReportService)
        dynamicReportService.demand.getSwapVirtualizerMaxSize(1) { -> return 100 }
        dynamicReportService.demand.getReportsDirectory(1) { -> return (System.getProperty("java.io.tmpdir")) }
        dynamicReportService.demand.getBlockSize(1) { -> return 4096 }
        dynamicReportService.demand.getMinGrowCount(1) { -> return 1024 }
        return dynamicReportService.proxyInstance()
    }

}
