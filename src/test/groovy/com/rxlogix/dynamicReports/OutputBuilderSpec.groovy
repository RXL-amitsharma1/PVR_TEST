package com.rxlogix.dynamicReports

import asset.pipeline.grails.LinkGenerator
import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.UserService
import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import groovy.mock.interceptor.MockFor
import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.DynamicReports
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification

class OutputBuilderSpec extends Specification implements DataTest {

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
    }

    def cleanup() {
    }

    void "test for exportXlsx"() {
        given:
        OutputBuilder outputBuilderInstance = new OutputBuilder()
        JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
        ReportBuilder reportBuilder = new ReportBuilder()
        JasperReportBuilder report = reportBuilder.initializeNewReport()
        JasperConcatenatedReportBuilder mainReport = DynamicReports.concatenatedReport() //TODO need to get fixed
        String mockFilename = "/mainExcelFileName"
        jasperReportBuilderEntry.jasperReportBuilder = report
        jasperReportBuilderEntry.excelSheetName = "reportFile"
        mainReport.concatenate([report] as JasperReportBuilder[]);

        when:
        outputBuilderInstance.invokeMethod('exportXlsx', [mainReport, mockFilename, [jasperReportBuilderEntry], 'en', false] as Object[])

        then:
        new File(System.getProperty("java.io.tmpdir"), "/mainExcelFileName.xlsx").exists() == true
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

    private makeDynamicReportService(){
        def dynamicReportService = new MockFor(DynamicReportService)
        dynamicReportService.demand.getSwapVirtualizerMaxSize(1) { -> return 100 }
        dynamicReportService.demand.getReportsDirectory(1) { -> return (System.getProperty("java.io.tmpdir")) }
        dynamicReportService.demand.getBlockSize(1) { -> return 4096 }
        dynamicReportService.demand.getMinGrowCount(1) { -> return 1024 }
        dynamicReportService.demand.getReportsDirectory(1) { -> return (System.getProperty("java.io.tmpdir")) }
        dynamicReportService.demand.getReportFilename(1) { String reportName, String outputFormat, String locale -> return (reportName + '.xlsx') }
        return dynamicReportService.proxyInstance()
    }
}
