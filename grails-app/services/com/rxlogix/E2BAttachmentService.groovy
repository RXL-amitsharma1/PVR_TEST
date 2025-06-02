package com.rxlogix

import com.rxlogix.util.MiscUtil
import grails.gorm.transactions.ReadOnly
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.w3c.dom.Document
import org.w3c.dom.NodeList

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import grails.core.GrailsApplication
import com.rxlogix.Constants

import com.rxlogix.util.FileUtil
import java.util.zip.Inflater

@ReadOnly
class E2BAttachmentService {

    static final String DF_COMPRESSION = 'DF'
    private static final int BUFFER_SIZE = 0x4000

    def fileAttachmentLocator
    def dynamicReportService

    GrailsApplication grailsApplication

    String addMissingAttachmentBytesToXMl(String xmlString, String xsltName, String xmlVersion, String xmlEncoding, String xmlDoctype) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(IOUtils.toInputStream(xmlString, Constants.UTF8))
        XPathFactory xpathfactory = XPathFactory.newInstance()
        XPath xpath = xpathfactory.newXPath()
        XPathExpression expr = xpath.compile("//*[contains(text(),'(###') and contains(text(),'###)')  and contains(text(),'_') and boolean(@mediaType)]")
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            if(grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
                if (nodes.item(i).firstChild?.nodeValue ==~ /.+\(###\w+_\d+_\d+###\)/) {
                    boolean asPlainText = false
                    if (nodes.item(i).attributes?.getNamedItem('mediaType')?.nodeValue == 'text/plain') {
                        asPlainText = true
                    }
                    String compressionType = nodes.item(i).attributes?.getNamedItem('compression')?.nodeValue
                    nodes.item(i).firstChild.setNodeValue(getBase64CompressedOrAsTextAttachment(((nodes.item(i).firstChild.nodeValue.split("###")[1]) =~ /\w+_\d+_\d+/)[0].toString(), asPlainText, compressionType))

                }
            }else {
                if (nodes.item(i).firstChild?.nodeValue ==~ /.+\(###\w+_\d+###\)/) {
                    boolean asPlainText = false
                    if (nodes.item(i).attributes?.getNamedItem('mediaType')?.nodeValue == 'text/plain') {
                        asPlainText = true
                    }
                    String compressionType = nodes.item(i).attributes?.getNamedItem('compression')?.nodeValue
                    nodes.item(i).firstChild.setNodeValue(getBase64CompressedOrAsTextAttachment(((nodes.item(i).firstChild.nodeValue.split("###")[1]) =~ /\w+_\d+/)[0].toString(), asPlainText, compressionType))

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

    String setDefaultTransformUnicodeAndVersion(String xmlString, String xmlEncoding) {
        xmlString = xmlString.replaceAll("<!DOCTYPE((.|\n|\r)*?)\">", "");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(IOUtils.toInputStream(xmlString, xmlEncoding ?: Constants.UTF8))
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0")
        transformer.setOutputProperty(OutputKeys.ENCODING, Constants.UTF8)
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        xmlString = writer.getBuffer().toString()
        return xmlString
    }

    private String getBase64CompressedOrAsTextAttachment(String caseAttachmentId, boolean asPlainText, String compressionType) {
        byte[] fileData = fileAttachmentLocator.getServiceFor(grailsApplication.config.getProperty('safety.source')).getFile(caseAttachmentId)?.data
        if (!fileData) {
            throw new RuntimeException("Attachment data not found for ${caseAttachmentId}")
        }
        if (asPlainText) {
            return new String(fileData, Constants.UTF8)
        }
        byte[] bytesToEncode = fileData
        if (compressionType == DF_COMPRESSION) {
            bytesToEncode = compressionDF(fileData)
        } else {
            log.warn("No compression type algo mentioned for ${caseAttachmentId} attachment while appending attachment bytes")
        }
        return Base64.encoder.encodeToString(bytesToEncode)
    }

    private byte[] compressionDF(byte[] fileBytes) throws IOException {
        InputStream input = new ByteArrayInputStream(fileBytes)
        ByteArrayOutputStream compressed = new ByteArrayOutputStream()
        try {
            // To avoid adding zlib headers https://stackoverflow.com/questions/29687576/is-it-possible-to-get-a-raw-deflate-out-of-java-util-zip-deflater
            Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true)
            DeflaterOutputStream out = new DeflaterOutputStream(compressed, deflater)
            int amountRead
            int mayRead = input.available()
            if (mayRead > 0) {
                byte[] buffer = new byte[Math.min(mayRead, BUFFER_SIZE)]
                while ((amountRead = input.read(buffer, 0, Math.min(mayRead, BUFFER_SIZE))) != -1) {
                    out.write(buffer, 0, amountRead)
                }
            }
            out.close()
            compressed.flush()
            deflater.end()
            return compressed.toByteArray()
        } finally {
            try {
                input.close()
                compressed.close()
            } catch (Exception ex) {
                log.warn("Exception while closing streams in compressionDF: ${ex?.message}")
            }
        }
    }

    // Utility method for decompression files.
    public byte[] decompressionDF(byte[] fileBytes) throws IOException, DataFormatException {
        InputStream input = new ByteArrayInputStream(fileBytes)
        ByteArrayOutputStream deCompressed = new ByteArrayOutputStream()
        try {
            byte[] buf = new byte[2048]
            // if we nowrap is false for Inflater and Deflater then length should be 2 in place of 0
            input.read(buf, 0, 0)
            int read = input.read(buf)
            if (read > 0) {
                // use nowrap mode to bypass zlib-header and checksum to avoid a DataFormatException as we are using nowrap while compression as in sample
                // https://stackoverflow.com/questions/29687576/is-it-possible-to-get-a-raw-deflate-out-of-java-util-zip-deflater
                Inflater inflater = new Inflater(true)
                inflater.setInput(buf, 0, read)
                byte[] res = new byte[1024]
                boolean dataWritten = false
                while (true) {
                    int resRead = 0
                    try {
                        resRead = inflater.inflate(res)
                    }
                    catch (DataFormatException exception) {
                        if (dataWritten) {
                            // some data could be read -> don't throw an exception
                            log.warn("DecompressDF: premature end of stream due to a DataFormatException");
                            break;
                        } else {
                            // nothing could be read -> re-throw exception
                            throw exception;
                        }
                    }
                    if (resRead != 0) {
                        deCompressed.write(res, 0, resRead)
                        dataWritten = true
                        continue
                    }
                    if (inflater.finished() || inflater.needsDictionary() || input.available() == 0) {
                        break
                    }
                    read = input.read(buf)
                    inflater.setInput(buf, 0, read)
                }
                inflater.end()
            }
            deCompressed.flush()
            return deCompressed.toByteArray()
        } finally {
            try {
                input.close()
                deCompressed.close()
            } catch (Exception ex) {
                log.warn("Exception while closing streams in decompressionDF : ${ex?.message}")
            }
        }
    }

    Map<String, String> fetchAttachmentIds(String xmlString, boolean isAttachmentExist) {
        Map<String, String> attachIdAndBookMarkMap = [:]
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(IOUtils.toInputStream(xmlString, Constants.UTF8))
        XPathFactory xpathfactory = XPathFactory.newInstance()
        XPath xpath = xpathfactory.newXPath()
        XPathExpression expr = xpath.compile("//*[contains(text(),'(###') and contains(text(),'###)')  and contains(text(),'_')]")
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        XPathExpression exprForBookmark = xpath.compile("//*[contains(text(),'####') and contains(text(),'####')]")
        Object resultBookMark = exprForBookmark.evaluate(doc, XPathConstants.NODESET);
        NodeList bookMarkNodes = (NodeList) resultBookMark;
        for (int i = 0; i < nodes.getLength(); i++) {
            if(grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
                String bookmarkName = null
                if(isAttachmentExist && bookMarkNodes.item(i).firstChild?.nodeValue ==~ /^#{4}.*#{4}$/) {
                    bookmarkName = ((bookMarkNodes.item(i).firstChild.nodeValue.split("####")[1]).toString())
                }
                String caseAttachmentId = null
                if (nodes.item(i).firstChild?.nodeValue ==~ /.+\(###\w+_\d+_\d+###\)/) {
                    caseAttachmentId = ((nodes.item(i).firstChild.nodeValue.split("###")[1]) =~ /\w+_\d+_\d+/)[0].toString()
                    if (!caseAttachmentId || !caseAttachmentId.contains('_')) {
                        return null
                    }
                }
                attachIdAndBookMarkMap.put(caseAttachmentId, bookmarkName)
            }else {
                String bookmarkName = null
                if(isAttachmentExist && bookMarkNodes.item(i).firstChild?.nodeValue ==~ /^#{4}.*#{4}$/) {
                    bookmarkName = ((bookMarkNodes.item(i).firstChild.nodeValue.split("####")[1]).toString())
                }
                String caseAttachmentId = null
                if (nodes.item(i).firstChild?.nodeValue ==~ /.+\(###\w+_\d+###\)/) {
                    caseAttachmentId = ((nodes.item(i).firstChild.nodeValue.split("###")[1]) =~ /\w+_\d+/)[0].toString()
                    if (!caseAttachmentId || !caseAttachmentId.contains('_')) {
                        return null
                    }
                }
                attachIdAndBookMarkMap.put(caseAttachmentId, bookmarkName)
            }
        }
        return attachIdAndBookMarkMap
    }

    Map<String, String> fetchDoumentSource(String xmlString) {
        Map<String, String> documentSource = [:]
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(IOUtils.toInputStream(xmlString, Constants.UTF8))
            XPath xPath = XPathFactory.newInstance().newXPath()
            String title = (String) xPath.evaluate("/ichicsr/safetyreport/safetyreportid/text()", doc, XPathConstants.STRING)
            if(title) {
                documentSource.put("title", title)
            }
            String subject = (String) xPath.evaluate("/ichicsr/safetyreport/authoritynumb/text()", doc, XPathConstants.STRING)
            if(subject) {
                documentSource.put("subject", subject)
            }else {
                subject = (String) xPath.evaluate("/ichicsr/safetyreport/companynumb/text()", doc, XPathConstants.STRING)
                if(subject) {
                    documentSource.put("subject", subject)
                }
            }
            String authorNode = (String) xPath.evaluate("/ichicsr/safetyreport/reportduplicate/duplicatenumb/text()", doc, XPathConstants.STRING)
            if(authorNode) {
                documentSource.put("author", authorNode)
            }
            String keywordsNode = (String) xPath.evaluate("/ichicsr/safetyreport/receiptdate/text()", doc, XPathConstants.STRING)
            if(keywordsNode) {
                documentSource.put("keywords", keywordsNode)
            }
        }catch (Exception e) {
            log.error("Exception while fetching document source details : ${e.message}", e)
        }
        return documentSource
    }

    Map<String, String> fetchAttachmentFiles(Map<String, String> attachIdAndBookMarkMap, String outputDirectoryPath) {
        Map<String, String> attachFilepathAndBookmark = [:]
        attachIdAndBookMarkMap.each { attachmentId, bookMark ->
            if(grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
                def response = fileAttachmentLocator.getServiceFor(grailsApplication.config.getProperty('safety.source')).getFile(attachmentId)
                byte[] fileData = response?.data
                if (!fileData) {
                    throw new RuntimeException("Attachment data not found for ${attachmentId}")
                }
                String filename = MiscUtil.generateRandomName() + System.currentTimeMillis() + "." +  FilenameUtils.getExtension(response.name)
                File reportFile = getReportFile(filename, outputDirectoryPath)
                reportFile.createNewFile()
                reportFile = FileUtil.generateFileFromByteArray(fileData, reportFile)
                if(reportFile && reportFile?.exists()) {
                    attachFilepathAndBookmark.put(reportFile.getPath(), bookMark)
                }
            }else {
                def response = fileAttachmentLocator.getServiceFor(grailsApplication.config.getProperty('safety.source')).getFile(attachmentId)
                def fileData = response?.data
                if (!fileData) {
                    throw new RuntimeException("Attachment data not found for ${attachmentId}")
                }
                String filename = MiscUtil.generateRandomName() + System.currentTimeMillis() + "." +  FilenameUtils.getExtension(response.name)
                File reportFile = getReportFile(filename, outputDirectoryPath)
                reportFile.createNewFile()
                reportFile = FileUtil.generateFileFromByteArray(fileData, reportFile)
                if(reportFile && reportFile?.exists()) {
                    attachFilepathAndBookmark.put(reportFile.getPath(), bookMark)
                }
            }
        }
        return attachFilepathAndBookmark
    }

    File getReportFile(String responseName, String outputDirectoryPath) {
        return new File(outputDirectoryPath + File.separator + responseName)
    }

    String evaluatePathValue(String xmlString, String path) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(IOUtils.toInputStream(xmlString, Constants.UTF8))
            XPath xPath = XPathFactory.newInstance().newXPath()
            String value = (String) xPath.evaluate(path, doc, XPathConstants.STRING)
            if(value) {
                return value
            }
        }catch (Exception e) {
            log.error("Exception while checking the ack is R2 : ${e.message}", e)
            return null
        }
        return null
    }
}
