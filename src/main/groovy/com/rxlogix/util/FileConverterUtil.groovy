package com.rxlogix.util

import com.rxlogix.Constants
import com.rxlogix.customException.CustomICSRAttachmentException
import com.rxlogix.enums.IcsrAttachmentExtEnum
import com.lowagie.text.Document
import com.lowagie.text.PageSize
import com.lowagie.text.pdf.PdfWriter
import com.lowagie.text.Paragraph
import com.lowagie.text.Element
import com.lowagie.text.Image
import com.lowagie.text.Rectangle
import com.rxlogix.gotenberg.GotenbergIntegrationApiService
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.apache.pdfbox.pdmodel.PDDocumentInformation
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import org.jsoup.Jsoup

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import org.apache.commons.io.FilenameUtils
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.ghost4j.converter.PDFConverter
import org.ghost4j.document.PaperSize
import org.ghost4j.document.PSDocument
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.VelocityContext
import org.apache.velocity.Template
import org.jsoup.parser.Parser
//import javax.imageio.ImageReader
//import javax.imageio.stream.ImageInputStream
//import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam
import com.rxlogix.utils.HtmlConverter
import java.nio.file.Paths
import org.apache.commons.imaging.Imaging
import org.apache.poi.hsmf.MAPIMessage
import org.apache.pdfbox.pdmodel.PDPageTree
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination
import groovyx.net.http.Method
import grails.core.GrailsApplication
import org.apache.commons.io.FileUtils

@Slf4j
class FileConverterUtil {

    static GotenbergIntegrationApiService gotenbergIntegrationApiService
    static GrailsApplication grailsApplication

    static File mergeAttachmentIntoPdf(Map<String, String> attachFilepathAndBookmark, String outputFilePath, Map<String, String> docSrcDetail) {
        //convert individual files into individual pdf
        log.info("Merging of the Attachment started");
        //now the outfilePath contains the merged file data as well
        Map<String, String> pdfFilesAndBookMark = [:]
        try {
            pdfFilesAndBookMark = convertIntoPdfFile(attachFilepathAndBookmark, outputFilePath);
            if(pdfFilesAndBookMark && pdfFilesAndBookMark.size() > 0) {
                PDFMergerUtility merger = new PDFMergerUtility()
                pdfFilesAndBookMark.each { pdfFilepath, bookMark ->
                    merger.addSource(new File(pdfFilepath))
                }
                merger.setDestinationFileName(outputFilePath)
                merger.mergeDocuments(null)

                //now the outfilepath contains the source details like author, subject, title etc.
                addDocumentSourceDetails(outputFilePath, docSrcDetail)
                //now the outfilepath contains the bookmark details
                outputFilePath = addBookMarkDetails(pdfFilesAndBookMark, outputFilePath)
                log.info("Merging of the Attachment ended");
                return new File(outputFilePath)
            }else {
                log.info("pdfFilesAndBookMark size is empty")
                log.info("Merging of the Attachment ended");
                return null
            }
        } finally {
            if(pdfFilesAndBookMark && pdfFilesAndBookMark.size() > 0) {
                pdfFilesAndBookMark.each { pdfFilepath, bookMark ->
                    new File(pdfFilepath).delete()
                }
            }
        }
    }

    static Map<String, String> convertIntoPdfFile(Map<String, String> attachFilepathAndBookmark, String outputFilePath) {
        Map<String, String> pdfFilesAndBookMark = [:]
        try {
            attachFilepathAndBookmark.each { attachFilepath, bookMark ->
                String extension = FilenameUtils.getExtension(attachFilepath)
                String filePath = null
                switch (extension.toLowerCase()) {
                    case IcsrAttachmentExtEnum.XLS.value:
                    case IcsrAttachmentExtEnum.XLSX.value:
                    case IcsrAttachmentExtEnum.CSV.value:
                    case IcsrAttachmentExtEnum.DOC.value:
                    case IcsrAttachmentExtEnum.DOCX.value:
                    case IcsrAttachmentExtEnum.RTF.value:
                    case IcsrAttachmentExtEnum.TIF.value:
                    case IcsrAttachmentExtEnum.TXT.value:
                    case IcsrAttachmentExtEnum.TIFF.value:
                    case IcsrAttachmentExtEnum.GIF.value:
                    case IcsrAttachmentExtEnum.JPG.value:
                    case IcsrAttachmentExtEnum.JPEG.value:
                    case IcsrAttachmentExtEnum.PNG.value:
                    case IcsrAttachmentExtEnum.BMP.value:
                    case IcsrAttachmentExtEnum.XML.value:
                    case IcsrAttachmentExtEnum.HTML.value:
                        filePath = generatePdfFromDiffExt(attachFilepath)
                        break;
                    case IcsrAttachmentExtEnum.PDF.value:
                        filePath = attachFilepath
                        break;
                    case IcsrAttachmentExtEnum.PSD.value:
                        filePath = generatePdfFromPsd(attachFilepath)
                        break;
                    case IcsrAttachmentExtEnum.PS.value:
                        filePath = generatePdfFromPs(attachFilepath)
                        break;
                    case IcsrAttachmentExtEnum.SGML.value:
                    case IcsrAttachmentExtEnum.SGM.value:
                        filePath = generatePdfFromSgmSgml(attachFilepath)
                        break;
                    case IcsrAttachmentExtEnum.MSG.value:
                        filePath = generatePdfFromMsg(attachFilepath)
                        break;
//                    case IcsrAttachmentExtEnum.EML.value:
//                        break;
//                    case IcsrAttachmentExtEnum.DICOM.value:
//                    case IcsrAttachmentExtEnum.DCM.value:
//                    filePath = generatePdfFromDcmDicom(attachFilepath)
//                        break;
//                    case IcsrAttachmentExtEnum.VSD.value:
//                    case IcsrAttachmentExtEnum.MDB.value:
//                    case IcsrAttachmentExtEnum.WPD.value:
//                        break;
//                    case IcsrAttachmentExtEnum.TXT.value:
//                        filePath = generatePdfFromTxt(attachFilepath)
//                        break;
//                    case IcsrAttachmentExtEnum.TIFF.value:
//                    case IcsrAttachmentExtEnum.GIF.value:
//                    case IcsrAttachmentExtEnum.JPG.value:
//                    case IcsrAttachmentExtEnum.JPEG.value:
//                    case IcsrAttachmentExtEnum.PNG.value:
//                    case IcsrAttachmentExtEnum.BMP.value:
//                        filePath = generatePdfFromImage(attachFilepath)
//                        break;
//                    case IcsrAttachmentExtEnum.XML.value:
//                        filePath = generatePdfFromXml(attachFilepath)
//                        break;
//                    case IcsrAttachmentExtEnum.HTML.value:
//                        filePath = generatePdfFromHtml(attachFilepath)
//                        break;
                    default:
                        log.info("We are not supporting the extension : " + extension)
                        throw new CustomICSRAttachmentException("No Supported Extension ::" +extension)
                        break
                }
                if(filePath) {
                    pdfFilesAndBookMark.put(filePath, bookMark)
                }else {
                    throw new CustomICSRAttachmentException("Unable to connect with the server")
                }
            }
        }catch(CustomICSRAttachmentException custom) {
            log.error("Unable to connect with the server", custom)
            return null
        }catch (Exception e) {
            log.error("Merging of the Attachment failed", e);
            return null
        }
        return pdfFilesAndBookMark
    }

    static String addDocumentSourceDetails(String outputFilePath, Map<String, String> docSrcDetail) {
        try {
            PDDocument mergedDocument = PDDocument.load(new File(outputFilePath))
            PDDocumentInformation documentInformation = mergedDocument.getDocumentInformation();
            documentInformation.setTitle(docSrcDetail.get("title"))
            documentInformation.setSubject(docSrcDetail.get("subject"))
            documentInformation.setAuthor(docSrcDetail.get("author"))
            documentInformation.setKeywords(docSrcDetail.get("keywords"))
            mergedDocument.save(outputFilePath);
        }catch (Exception e) {
            log.error("Exception while adding the source details of the pdf", e)
            return null
        }
        return outputFilePath
    }

    static String addBookMarkDetails(Map<String, String> pdfFilesAndBookMark, String outputFilePath) {
        try {
            PDDocument mergedDocument = PDDocument.load(new File(outputFilePath))
            PDDocumentOutline root = new PDDocumentOutline()
            mergedDocument.getDocumentCatalog().setDocumentOutline(root)
            PDPageTree pages = mergedDocument.getPages()
            int pageNumber = 0
            pdfFilesAndBookMark.each { attachFilepath, bookMark ->
                PDDocument sourceDocument = PDDocument.load(new File(attachFilepath))
                PDPage page = pages.get(pageNumber)
                PDOutlineItem bookmark = new PDOutlineItem()
                bookmark.setTitle(bookMark)
                PDDestination destination = new PDPageXYZDestination()
                ((PDPageXYZDestination) destination).setPage(page)
                bookmark.setDestination(destination)
                root.addLast(bookmark)
                pageNumber += sourceDocument.numberOfPages
            }
            mergedDocument.save(outputFilePath)
            return outputFilePath
        }catch (Exception e) {
            log.error("Exception while adding the bookmark details of the pdf", e)
            return null
        }
    }

    //generate pdf from txt
    public static String generatePdfFromTxt(String inputFilePath) {
        String outputFilePath = createOutputFilePath(inputFilePath)
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(outputFilePath)).setPdfVersion(PdfWriter.PDF_VERSION_1_7);
        document.open();
        document.add(new Paragraph("\n"));
        BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
        String strLine;
        while ((strLine = br.readLine()) != null) {
            Paragraph para = new Paragraph(strLine + "\n");
            para.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(para);
        }
        document.close();
        br.close();
        return outputFilePath
    }

    //Converting image (jpg, jpeg, gif, png, tiff, bmp) to pdf
    public static String generatePdfFromImage(String inputFilePath) {
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        String outputFilePath = MiscUtil.generateRandomName()
        FileOutputStream fos = new FileOutputStream(outputFilePath);
        byte[] data = null
        //Default image to pdf conversion is jpg, jpeg, gif, png, tiff and for other we have converted in other form and then converted into pdf
        if (FilenameUtils.getExtension(inputFilePath.toLowerCase()) == "bmp") {
            BufferedImage bufferimage = ImageIO.read(new File(inputFilePath));
            ByteArrayOutputStream output1 = new ByteArrayOutputStream();
            ImageIO.write(bufferimage, FilenameUtils.getExtension(inputFilePath), output1);
            data = output1.toByteArray();
        }

        PdfWriter writer = PdfWriter.getInstance(document, fos);
        writer.open();
        document.open();
        Image image
        if (data) {
            image = Image.getInstance(data);
        } else {
            image = Image.getInstance(inputFilePath);
        }
        Rectangle A4 = PageSize.A4;
        float scalePortrait = Math.min(A4.getWidth() / image.getWidth(), A4.getHeight() / image.getHeight());
        float w;
        float h;
        w = image.getWidth() * scalePortrait;
        h = image.getHeight() * scalePortrait;
        image.scaleAbsolute(w, h);
        float posH = (A4.getHeight() - h) / 2;
        float posW = (A4.getWidth() - w) / 2;

        image.setAbsolutePosition(posW, posH);

        document.add(image);
        document.close();
        writer.close();
        return outputFilePath
    }

    public static String generatePdfFromPsd(String inputFilePath) {
        String outputFilePath = createOutputFilePath(inputFilePath)
        try {
            // Read the PSD image using Apache Commons Imaging
            BufferedImage psdImage = Imaging.getBufferedImage(new File(inputFilePath))
            // Convert the PSD image to PDF
            PDDocument doc = new PDDocument()
            PDPage page = new PDPage(new PDRectangle(psdImage.width, psdImage.height))
            doc.addPage(page)
            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, psdImage)
            PDPageContentStream contentStream = new PDPageContentStream(doc, page)
            contentStream.drawImage(pdImage, 0, 0)
            contentStream.close()
            doc.save(new File(outputFilePath))
            doc.close()
            return outputFilePath
        } catch (Exception e) {
            log.error("Exception while converting psd file into pdf: ${e.message}", e)
            return null
        }
    }

    public static String generatePdfFromPs(String inputFilePath) {
        String outputFilePath = createOutputFilePath(inputFilePath)
        try {
            PSDocument document = new PSDocument();
            document.load(new File(inputFilePath));
            FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
            PDFConverter converter = new PDFConverter();
            converter.setPDFSettings(PDFConverter.OPTION_PDFSETTINGS_PREPRESS)
            converter.setPaperSize(PaperSize.A4)
            converter.convert(document, fos);
            return outputFilePath
        } catch (Exception e) {
            log.error("Exception while converting ps file into pdf: ${e.message}", e)
            return null
        }
    }

    public static String generatePdfFromXml(String inputFilePath) {
        String outputFilePath = createOutputFilePath(inputFilePath)
        try {
            VelocityEngine velocityEngine = new VelocityEngine()
            velocityEngine.init()

            Template template = velocityEngine.getTemplate(inputFilePath)
            VelocityContext velocityContext = new VelocityContext()
            StringWriter writer = new StringWriter()
            template.merge(velocityContext, writer)

            FileOutputStream outputStream = new FileOutputStream(outputFilePath)
            Document pdfDocument = new Document()
            PdfWriter.getInstance(pdfDocument, outputStream)
            pdfDocument.open()

            Paragraph paragraph = new Paragraph(writer.toString())
            pdfDocument.add(paragraph)
            pdfDocument.close()
            outputStream.close()
            return outputFilePath
        } catch (Exception e) {
            log.error("Exception while converting xml file into pdf: ${e.message}", e)
            return null
        }
    }

    public static String generatePdfFromSgmSgml(String inputFilePath) {
        String intermediateFilePath = createOutputFilePath(inputFilePath)
        try {
            String sgmlContent = new File(inputFilePath).text
            org.jsoup.nodes.Document doc = Jsoup.parse(sgmlContent, "", Parser.xmlParser())
            new File(intermediateFilePath).text = doc.toString()
            String outputFilePath = generatePdfFromTxt(intermediateFilePath.toString())
            return outputFilePath
        } catch (Exception e) {
            log.error("Exception while converting sgm or sgml file into pdf: ${e.message}", e)
            return null
        }
    }


//    public static String generatePdfFromDcmDicom(String inputFilePath) {
//        String outputFilePath = createOutputFilePath(inputFilePath)
//        try {
//            ImageInputStream iis = ImageIO.createImageInputStream(new File(inputFilePath))
//            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("DICOM")
//            ImageReader reader = readers.next()
//            DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam()
//            param.setWindowCenter(128)
//            param.setWindowWidth(256)
//            reader.setInput(iis, false)
//            BufferedImage image = reader.read(0, param)
//            File pdfFile = new File(outputFilePath)
//
//            PDDocument document = new PDDocument()
//            PDPage page = new PDPage()
//            document.addPage(page)
//
//            PDImageXObject pdImage = LosslessFactory.createFromImage(document, image)
//
//            PDRectangle mediaBox = page.getMediaBox()
//            float startX = (mediaBox.getWidth() - pdImage.getWidth()) / 2
//            float startY = (mediaBox.getHeight() - pdImage.getHeight()) / 2
//
//            PDPageContentStream contentStream = new PDPageContentStream(document, page)
//            contentStream.drawImage(pdImage, startX, startY, pdImage.getWidth(), pdImage.getHeight())
//            contentStream.close()
//            document.save(pdfFile)
//            document.close()
//            iis.close()
//            reader.dispose()
//            return outputFilePath
//        } catch (Exception e) {
//            log.error("Exception while converting dcm or dicom file into pdf: ${e.message}", e)
//            return null
//        }
//    }

    public static String generatePdfFromHtml(String inputFilePath) {
        String outputFilePath = createOutputFilePath(inputFilePath)
        try {
            File htmlFile = Paths.get(inputFilePath).toFile()
            String htmlContent = htmlFile.text
            FileOutputStream outputStream = new FileOutputStream(outputFilePath)
            HtmlConverter.convertToPdf(htmlContent, outputStream)
            outputStream.close()
            return outputFilePath
        } catch (Exception e) {
            log.error("Exception while converting dcm or dicom file into pdf: ${e.message}", e)
            return null
        }
    }

    public static String generatePdfFromMsg(String inputFilePath) {
        String outputFilePath = createOutputFilePath(inputFilePath)
        try {
            MAPIMessage msg = new MAPIMessage(new File(inputFilePath))
            Document pdfDocument = new Document()
            PdfWriter.getInstance(pdfDocument, new FileOutputStream(new File(outputFilePath)))
            pdfDocument.open()
            String[] messageHeaders = msg.getHeaders()
            String messageBody = msg.getTextBody()
            for (String header : messageHeaders) {
                pdfDocument.add(new Paragraph(header))
            }

            pdfDocument.add(new Paragraph(" "))
            String[] lines = messageBody.split("\\r?\\n")
            for (String line : lines) {
                pdfDocument.add(new Paragraph(line))
            }
            pdfDocument.close()
            return outputFilePath
        } catch (Exception e) {
            log.error("Exception while converting msg file into pdf: ${e.message}", e)
            return null
        }
    }

    static String generatePdfFromDiffExt(String inputFilePath) {
        String outputFilePath = createOutputFilePath(inputFilePath)
        if (!grailsApplication) grailsApplication = Holders.applicationContext.getBean("grailsApplication")
        String baseUrl = grailsApplication.config.getProperty("app.gotenberg.api.url")
        String path = grailsApplication.config.getProperty("app.gotenberg.forms.libreoffice.convert")
        try {
            if(baseUrl && path) {
                log.info("Converting Pdf using API url : "+baseUrl +""+path)
                if (!gotenbergIntegrationApiService) gotenbergIntegrationApiService = Holders.applicationContext.getBean("gotenbergIntegrationApiService")
                def fileData = gotenbergIntegrationApiService.postData(baseUrl, path, inputFilePath, Method.POST)
                File outputFile = new File(outputFilePath);
                FileUtils.writeByteArrayToFile(outputFile, fileData?.data)
                log.info("Successfully pdf file converted using API")
                return outputFilePath
            }else {
                log.info("baseUrl or end poing is empty")
            }
        } catch (Exception ex) {
            log.error("Unexpected error",ex)
            ex.printStackTrace()
            return null
        }
    }

    public static createOutputFilePath(inputFilePath) {
        File file = new File(inputFilePath);
        String path = file.getParentFile().getAbsolutePath()
        return path  + File.separator + MiscUtil.generateRandomName() +  System.currentTimeMillis() + Constants.PDF_EXT
    }

}
