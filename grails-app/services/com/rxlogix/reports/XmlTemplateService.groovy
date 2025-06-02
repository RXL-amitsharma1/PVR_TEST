package com.rxlogix.reports

import com.rxlogix.config.XMLTemplateNode
import com.rxlogix.reportTemplate.xml.generator.SampleXmlUtil
import com.rxlogix.reportTemplate.xml.generator.XMLTemplateNodeBuilder
import grails.gorm.transactions.ReadOnly
import org.apache.xmlbeans.*

@ReadOnly
class XmlTemplateService {

    private static final String ICSR_REPORT_ROOT = "MCCI_IN200100UV01"
    private static final String ICSR_REPORT_XSD_PATH = "/xsd/ICH_ICSR_Schema/multicacheschemas/${ICSR_REPORT_ROOT}.xsd"

    XMLTemplateNode createR3XMLSample() {
        URL schemaResource = XmlTemplateService.class.getResource(ICSR_REPORT_XSD_PATH)

        XmlOptions loadOptions = new XmlOptions()
        loadOptions.setLoadLineNumbers()
        loadOptions.setLoadMessageDigest()
        XmlObject[] schemas = XmlObject.Factory.parse(schemaResource, loadOptions)

        XmlOptions compileOptions = new XmlOptions()
        compileOptions.setCompileDownloadUrls()
        SchemaTypeSystem schemaTypeSystem = XmlBeans.compileXsd(schemas, XmlBeans.getBuiltinTypeSystem(), compileOptions)
        if (schemaTypeSystem == null) {
            throw new RuntimeException("No Schemas to process.")
        }

        SchemaType[] globalElems = schemaTypeSystem.documentTypes()
        SchemaType elem = null
        for (int i = 0; i < globalElems.length; i++) {
            if (ICSR_REPORT_ROOT == globalElems[i].getDocumentElementName().getLocalPart()) {
                elem = globalElems[i]
                break
            }
        }

        if (elem == null) {
            throw new RuntimeException("Could not find a global element with name \"${ICSR_REPORT_ROOT}\"")
        }
        return createSampleForType(elem)
    }

    private XMLTemplateNode createSampleForType(SchemaType schemaType) {
        XmlObject object = XmlObject.Factory.newInstance()
        XmlCursor cursor = object.newCursor()
        // Skip the document node
        cursor.toNextToken()
        // Using the type and the cursor, call the utility method to get a
        // sample XML payload for that Schema element
        new SampleXmlUtil().createSampleForType(schemaType, cursor)
        // Cursor now contains the sample payload
        // Pretty print the result.  Note that the cursor is positioned at the
        // end of the doc so we use the original xml object that the cursor was
        // created upon to do the xmlText() against.
        XMLTemplateNodeBuilder nodeBuilder = new XMLTemplateNodeBuilder()
        object.save(nodeBuilder, nodeBuilder)
        return nodeBuilder.rootNode
    }
}
