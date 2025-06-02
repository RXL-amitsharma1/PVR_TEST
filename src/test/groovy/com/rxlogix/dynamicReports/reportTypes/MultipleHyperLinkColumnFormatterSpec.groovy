package com.rxlogix.dynamicReports.reportTypes

import grails.util.Holders
import org.grails.config.PropertySourcesConfig
import spock.lang.Specification

/**
 * See the API for usage instructions
 */
//@TestMixin(GrailsUnitTestMixin)
class MultipleHyperLinkColumnFormatterSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test for extractUrls"() {
        given:
        Holders.setConfig(new PropertySourcesConfig())
        Holders.config.url.field.regex="\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.]*[-a-zA-Z0-9+&@#/%=~_|]"
        MultipleHyperLinkColumnFormatter multipleHyperLinkColumnFormatter = new MultipleHyperLinkColumnFormatter()
        String urlText = "This is https://www.google.com;https://www.facebook.com is for facebook;http://10.100.22.64:8080/reports/advancedReportViewer/viewCll?parentId=200293&filter=%5B%7B%22field%22%3A%22pvcLcpRootCause%22%2C%22value%22%3Anull%7D%5D&cell=GP_0_CR11 reports data PV Central"
        when:
        String urls = multipleHyperLinkColumnFormatter.format(urlText, null)

        then:
        urls.contains("</a>")
        urls.contains("This is <a href='https://www.google.com' target='_blank'>https://www.google.com</a>")
        urls.contains("<a href='http://10.100.22.64:8080/reports/advancedReportViewer/viewCll?parentId=200293&filter=%5B%7B%22field%22%3A%22pvcLcpRootCause%22%2C%22value%22%3Anull%7D%5D&cell=GP_0_CR11' target='_blank'>http://10.100.22.64:8080/reports/advancedReportViewer/viewCll?parentId=200293&filter=%5B%7B%22field%22%3A%22pvcLcpRootCause%22%2C%22value%22%3Anull%7D%5D&cell=GP_0_CR11</a>")
    }
}
