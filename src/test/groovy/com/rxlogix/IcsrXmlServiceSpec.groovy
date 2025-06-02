package com.rxlogix

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import spock.lang.Ignore
import spock.lang.Specification

class IcsrXmlServiceSpec extends Specification implements DataTest, ServiceUnitTest<IcsrXmlService> {

    def setup() {
        config.pv.app.e2b.icsr.generation.placeholders = ['###CREATE_DATE###': 'new Date().format("yyyyMMddHHmmssZZ")']
        config.pv.app.e2b.icsr.transmission.placeholders = ['###TRANSMIT_DATE###': 'new Date().format("yyyyMMddHHmmssZZ")']
        config.pv.app.e2b.r2.icsr.generation.placeholders = ['###E2BR2_CREATE_DATE###': 'new Date().format("yyyyMMddHHmmss")']   //This is specially handled for FDA
        config.pv.app.e2b.r2.icsr.transmission.placeholders = ['###E2BR2_TRANSMIT_DATE###': 'new Date().format("yyyyMMdd")']  //This is specially handled for FDA
    }

    void "test XmlNodes replace by generation placeholders values with time seconds check"() {
        given:
        config.pv.app.e2b.icsr.generation.placeholders = ['###CREATE_DATE_TIME###': 'new Date().format("yyyy-MM-dd HH:mm:ss.SSS")', '###XML_NAME###': '"test_xml"'].collectEntries { k, v -> [(k): "'${Eval.me(v)}'"] }
        String xmlString = '''<?xml version="1.0" encoding="UTF-8"?><note>
  <to>Tove</to>
  <from>Jani</from>
  <heading>Reminder</heading>
  <body>Don't forget me this weekend!</body>
  <date>###CREATE_DATE_TIME###</date>
  <name>###XML_NAME###</name>
  <id>###MESSAGE_IDENTIFIER###</id>
</note>'''
        Node node = new XmlParser().parseText(xmlString)
        when:
        service.replaceGenerationPlaceHoldersInXmlNodes(node, '123-16US1212-1', 'EMA')
        then:
        groovy.xml.XmlUtil.serialize(node).trim() == xmlString.replace('###CREATE_DATE_TIME###', "${Eval.me(config.pv.app.e2b.icsr.generation.placeholders.get('###CREATE_DATE_TIME###'))}").replace('###XML_NAME###', 'test_xml').replace('###MESSAGE_IDENTIFIER###', '123-16US1212-1').trim()
    }

    void "test XmlNodes replace by generation placeholders values"() {
        given:
        config.pv.app.e2b.icsr.generation.placeholders = ['###CREATE_DATE###': 'new Date().format("dd-MM-yyyy")', '###XML_NAME###': '"test_xml"']
        String xmlString = '''<?xml version="1.0" encoding="UTF-8"?><note>
  <to>Tove</to>
  <from>Jani</from>
  <heading>Reminder</heading>
  <body>Don't forget me this weekend!</body>
  <date>###CREATE_DATE###</date>
  <name>###XML_NAME###</name>
  <id>###MESSAGE_IDENTIFIER###</id>
</note>'''
        Node node = new XmlParser().parseText(xmlString)
        when:
        service.replaceGenerationPlaceHoldersInXmlNodes(node,'123-16US1212-1', 'EMA')
        then:
        groovy.xml.XmlUtil.serialize(node).trim() == xmlString.replace('###CREATE_DATE###', new Date().format("dd-MM-yyyy")).replace('###XML_NAME###', 'test_xml').replace('###MESSAGE_IDENTIFIER###', '123-16US1212-1').trim()
    }

    void "test XmlNodes replace by transmission placeholders values"() {
        given:
        config.pv.app.e2b.icsr.transmission.placeholders = ['###TRANSMIT_DATE###': 'new Date().format("dd-MM-yyyy")', '###XML_NAME###': '"test_xml"']
        String xmlString = '''<?xml version="1.0" encoding="UTF-8"?><note>
  <to>Tove</to>
  <from>Jani</from>
  <heading>Reminder</heading>
  <body>Don't forget me this weekend!</body>
  <date id="1212">###TRANSMIT_DATE###</date>
  <name prefix="###TEST_ATTR###">###XML_NAME###</name>
  <first prefix="###TRANSMIT_DATE###"/>
</note>''';
        String xsltName = "EMA"
        expect:
        service.replaceTransmissionPlaceHoldersInXmlNodes(xmlString, xsltName, "1.0", Constants.UTF8, null).trim() == xmlString.replace('###TRANSMIT_DATE###', new Date().format("dd-MM-yyyy")).replace('###XML_NAME###', 'test_xml').trim()
    }

    @Ignore
    void "test validateR3Xml "() {
        given:
        config.tempDirectory = config.tempDirectory ?: "${System.getProperty("java.io.tmpdir")}/pvreports"
        long currentDateTimeStamp = new Date().time
        String r3ReportFileName = "output_${currentDateTimeStamp}.xml"
        File xmlFile = new File(config.tempDirectory + "/input_${currentDateTimeStamp}.xml")
        xmlFile.text = filetext
        when:
        String errors = service.validateXml(xmlFile, getClass().getResource('sample.xsd').getFile())
        then:
        errors == results
        cleanup:
        xmlFile.delete()
        new File(config.tempDirectory + r3ReportFileName).delete()
        where:
        filetext                                                      || results
        new File(getClass().getResource('sample.xml').getFile()).text || null
        '<?xml version="1.0" ?>'                                      || 'Line: -1 Column: -1 Description: Premature end of file.'
    }
}
