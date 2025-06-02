package com.rxlogix.utils;

import com.lowagie.text.DocumentException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.OutputStream;

public class HtmlConverter {

    private static String htmlToXhtml(String html) {
        // Convert HTML to XHTML
        Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return document.html();
    }

    public static void convertToPdf(String htmlContent, OutputStream outputStream) throws DocumentException {
        // Generate PDF from HTML content
        ITextRenderer renderer = new ITextRenderer();
        // Convert HTML to XHTML
        String htmlToXhtml = htmlToXhtml(htmlContent);
        renderer.setDocumentFromString(htmlToXhtml);

        // Render the document to PDF
        renderer.layout();
        renderer.createPDF(outputStream);
    }
}
