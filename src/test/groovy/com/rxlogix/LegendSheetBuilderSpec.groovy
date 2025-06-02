package com.rxlogix

import com.rxlogix.dynamicReports.LegendSheetBuilder
import grails.testing.spring.AutowiredTest
import groovy.mock.interceptor.MockFor
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification

class LegendSheetBuilderSpec extends Specification implements AutowiredTest  {

    def setupSpec() {
        defineBeans {
            dynamicReportService(InstanceFactoryBean, makeDynamicReportService(), DynamicReportService)
            customMessageService(InstanceFactoryBean, makeCustomMessageService(), CustomMessageService)
        }
    }


    def "Pass a list to get templateQueryLegend"() {
        given:
        LegendSheetBuilder legendSheetBuilder = new LegendSheetBuilder()
        List<Map<String, String>> excludeVersionNumberList = legendSheetBuilder.excludeVersionNumber(templateQueryLegendList)
        expect:
        excludeVersionNumberList == newList
        where:
        templateQueryLegendList                                        || newList
        [[columnName: "Case Number", columnLegend: "cno"],
         [columnName: "Patient Ethnicity", columnLegend: ""]
         , [columnName: "Version Number", columnLegend: ""]]           || [[columnName: "Case Number", columnLegend: "cno"]]

        [[columnName: "Version Number", columnLegend: "vno"],
         [columnName: "Patient Ethnicity", columnLegend: "patient_legend"]
         , [columnName: "Case Classification", columnLegend: ""]]      || [[columnName: "Version Number", columnLegend: "vno"], [columnName: "Patient Ethnicity", columnLegend: "patient_legend"]]

        [[columnName: "Action Item date", columnLegend: ""],
         [columnName: "Patient Ethnicity", columnLegend: ""]
         , [columnName: "Case Classification", columnLegend: ""]]      || []

        [[columnName: "Study Id", columnLegend: "s_id"],
         [columnName: "Patient Gender", columnLegend: "pg_no"]
         , [columnName: "Case Classification", columnLegend: "cl_no"]] || [[columnName: "Study Id", columnLegend: "s_id"], [columnName: "Patient Gender", columnLegend: "pg_no"], [columnName: "Case Classification", columnLegend: "cl_no"]]

        [[columnName: "Case Number", columnLegend: "case_no"],
         [columnName: "Version Number", columnLegend: "version_no"]
         , [columnName: "Case Classification", columnLegend: "cl_no"]] || [[columnName: "Case Number", columnLegend: "case_no"], [columnName: "Version Number", columnLegend: "version_no"], [columnName: "Case Classification", columnLegend: "cl_no"]]

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
        dynamicReportService.demand.isInPrintMode(1) { params -> return true }
        return dynamicReportService.proxyInstance()
    }
}
