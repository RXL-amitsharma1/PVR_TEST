package com.rxlogix

import grails.gorm.transactions.ReadOnly
import grails.util.Holders
import org.apache.commons.io.IOUtils
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.SAXParseException

import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.URIResolver
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

@ReadOnly
class IcsrXmlService {

    void replaceGenerationPlaceHoldersInXmlNodes(Node rootNode, String messageIdentifier, String xsltName) {
        Map<String, Object> placeHoldersMap = new HashMap<>()
        if(xsltName && (xsltName.equals(Constants.FDA_21) || xsltName.equals(Constants.FDA_22) || xsltName.equals(Constants.HEALTH_CANADA) || xsltName.equals(Constants.SWISSMEDIC))) {
            placeHoldersMap = Holders.config.getProperty('pv.app.e2b.r2.icsr.generation.placeholders', Map).collectEntries{k, v -> [(k): Eval.me(v)]}
            placeHoldersMap += Holders.config.getProperty('pv.app.e2b.r2.icsr.transmission.placeholders', Map).collectEntries { k, v -> [(k): Eval.me(v)] }

        }else if (xsltName && (xsltName.equals(Constants.PMDA))) {
            placeHoldersMap = Holders.config.getProperty('pv.app.e2b.icsr.pmda.generation.placeholders', Map).collectEntries{k, v -> [(k): Eval.me(v)]}
            placeHoldersMap += Holders.config.getProperty('pv.app.e2b.icsr.pmda.transmission.placeholders', Map).collectEntries{k, v -> [(k): Eval.me(v)]}
        }else{
            placeHoldersMap = Holders.config.getProperty('pv.app.e2b.icsr.generation.placeholders', Map).collectEntries{k, v -> [(k): Eval.me(v)]}
            placeHoldersMap += Holders.config.getProperty('pv.app.e2b.icsr.transmission.placeholders', Map).collectEntries{k, v -> [(k): Eval.me(v)]}
        }
        //Mandatory replacementTag
        placeHoldersMap.put('###MESSAGE_IDENTIFIER###', "${messageIdentifier}")
        rootNode.'**'.each {
            if (it instanceof Node && it.name() && (it.text() in placeHoldersMap.keySet())) {
                it.setValue(placeHoldersMap.get(it.text())?.toString() ?: '')
            }
        }
    }

    //Replace at the time of transmission
    String replaceTransmissionPlaceHoldersInXmlNodes(String xmlString, String xsltName, String xmlVersion, String xmlEncoding, String xmlDoctype) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(IOUtils.toInputStream(xmlString, "UTF-8"))
        XPathFactory xpathfactory = XPathFactory.newInstance()
        XPath xpath = xpathfactory.newXPath()
        Map<String, Object> placeHoldersMap = new HashMap<>()
        if(xsltName && (xsltName.equals(Constants.FDA_21) || xsltName.equals(Constants.FDA_22) || xsltName.equals(Constants.HEALTH_CANADA) || xsltName.equals(Constants.SWISSMEDIC))) {
            placeHoldersMap = Holders.config.getProperty('pv.app.e2b.r2.icsr.transmission.placeholders', Map).collectEntries { k, v -> [(k): Eval.me(v)] }
        }else if (xsltName && (xsltName.equals(Constants.PMDA))) {
            placeHoldersMap += Holders.config.getProperty('pv.app.e2b.icsr.pmda.transmission.placeholders', Map).collectEntries{k, v -> [(k): Eval.me(v)]}
        }else {
            placeHoldersMap = Holders.config.getProperty('pv.app.e2b.icsr.transmission.placeholders', Map).collectEntries { k, v -> [(k): Eval.me(v)] }
        }
        placeHoldersMap.each {
            XPathExpression expr = xpath.compile("//*[text() = '${it.key}']")
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).firstChild?.nodeValue == it.key) {
                    nodes.item(i).firstChild.setNodeValue(it.value?.toString() ?: '')
                }
            }
            //for Attributes value replacement.
            XPathExpression exprAttri = xpath.compile("//*[@* = '${it.key}']")
            Object resultAttri = exprAttri.evaluate(doc, XPathConstants.NODESET);
            NodeList attNodes = (NodeList) resultAttri;
            for (int i = 0; i < attNodes.getLength(); i++) {
                def element = attNodes.item(i)
                for (int j = 0; j < element.attributes.length; j++) {
                    def attrNode = element.attributes.item(j)
                    if (attrNode.nodeValue == it.key) {
                        attrNode.setNodeValue(it.value?.toString() ?: '')
                    }
                }
            }
        }
        Transformer tf = TransformerFactory.newInstance().newTransformer()
        tf.setOutputProperty(OutputKeys.ENCODING, xmlEncoding?: Constants.UTF8)
        tf.setOutputProperty(OutputKeys.VERSION, xmlVersion?:"1.0")
        tf.setOutputProperty(OutputKeys.INDENT, "yes")
        if (xmlDoctype) {
            tf.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, xmlDoctype)
        } else if (xsltName && xsltName.equals(Constants.FDA_22)) {
            tf.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1")
            tf.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "https://www.accessdata.fda.gov/xml/icsr-xml-v2.2.dtd")
        } else if (xsltName && xsltName.equals(Constants.FDA_21)) {
            tf.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1")
            tf.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "https://www.accessdata.fda.gov/xml/icsr-xml-v2.1.dtd")
        } else if (xsltName && (xsltName.equals(Constants.HEALTH_CANADA) || xsltName.equals(Constants.SWISSMEDIC) || xsltName.equals(Constants.ICH_21))) {
            tf.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://eudravigilance.ema.europa.eu/dtd/icsr21xml.dtd")
        }
        Writer out = new StringWriter()
        tf.transform(new DOMSource(doc), new StreamResult(out))
        out.toString()
    }

    String validateXml(File file, String xsdPath) {
        if (!Holders.config.getProperty('icsr.file.validate', Boolean)) {
            return null
        }
        URL xsdURI
        if (xsdPath.startsWith('/')) {
            xsdURI = new File(xsdPath).toURI().toURL()
        } else {
            xsdURI = this.class.classLoader.getResource(xsdPath)
        }
        try {
            SchemaFactory factory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdURI);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(file));
        } catch (IOException | SAXParseException e) {
            String output = "Line: " + ((SAXParseException) e).getLineNumber() + " Column: " + ((SAXParseException) e).getColumnNumber() + " Description: " + e.getMessage()
            return output
        }
        return null
    }

    File transform(File inXML, String inXSL, File outTXT) {
//        def xslt= inXSL?.getText()
//        def transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xslt)))
        TransformerFactory factory = TransformerFactory.newInstance()
        factory.setURIResolver(new URIResolver() {
            @Override
            Source resolve(String href, String base) throws TransformerException {
                try {
                    InputStream inputStream = getStreamOfPath("xslt/" +href);
                    return new StreamSource(inputStream);
                }
                catch (Exception ex) {
                    log.error("XSL File not found: ${href}", ex)
                    return null
                }
            }
        })
        Transformer transformer = factory.newTransformer(new StreamSource(getStreamOfPath(inXSL)))
        def xml = inXML?.getText()
        xml = handleParsingForSpecialChars(xml)
        def outXML = new FileOutputStream(outTXT)

        transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(outXML))
        return outTXT
    }

    def handleParsingForSpecialChars(def xmlString){
        String specialCharRegex = ".*&#\\d+;.*"
        String replaceRegex = "&(?=#\\d+;)"
        if(xmlString.replaceAll("\n","").matches(specialCharRegex)){
            xmlString = xmlString.replaceAll(replaceRegex,"&amp;")
        }
        return xmlString
    }

    InputStream getStreamOfPath(String path) {
        InputStream resourceStream
        if (path.startsWith('/')) {
            resourceStream = new FileInputStream(new File(path))
        } else {
            resourceStream = this.class.classLoader.getResourceAsStream(path)

        }
        return resourceStream
    }


}
