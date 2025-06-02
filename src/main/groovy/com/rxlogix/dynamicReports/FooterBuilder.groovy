package com.rxlogix.dynamicReports

import com.rxlogix.ImageService
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ReportTemplate
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.SensitivityLabelEnum
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.constant.*
import net.sf.dynamicreports.report.builder.component.BreakBuilder

import javax.imageio.ImageIO

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class FooterBuilder {
    private static final int XSLX_HEADER_ROWS_GAP = 10
    ImageService imageService = Holders.applicationContext.getBean("imageService")
    private static final int TEMPLATE_QUERY_FOOTER_MAX_SYMBOL_IN_ROW = 30;
    Integer templateFooterMaxLength = Holders.config.getProperty('template.footer.max.length', Integer)
    Integer reportFooterMaxLength = Holders.config.getProperty('report.footer.max.length', Integer)
    String setFooter(Map params, JasperReportBuilder report, ExecutedTemplateQuery executedTemplateQuery, boolean isCriteriaSheetOrAppendix) {
        setFooterTemplate(report, executedTemplateQuery?.executedTemplate, params, executedTemplateQuery, isCriteriaSheetOrAppendix)
        setFooterParameters(params, report, executedTemplateQuery, isCriteriaSheetOrAppendix)
    }

    void setFooterTemplate(JasperReportBuilder report, ReportTemplate template, Map params = null,
                           ExecutedTemplateQuery executedTemplateQuery = null, isCriteriaSheetOrAppendix = false) {

        // Column footer has fixed height. We should determinate lines count and set approximate footer height
        def templateFooter = template?.templateFooter
        def showTemplateFooter = template?.showTemplateFooter
        HorizontalListBuilder footerComponent = cmp.horizontalList()
                .setStyle(stl.style().setTopBorder(Templates.horizontalLine))

        if (showTemplateFooter && templateFooter != null) {
            // Column footer has fixed height. We should determinate lines count and set approximate footer height
            def delimitersCount = templateFooter.count("\n")
            if (templateFooter.length() > templateFooterMaxLength) {
                templateFooter = templateFooter.substring(0, templateFooterMaxLength)
            }
            report.pageFooter(cmp.horizontalList(cmp.text(templateFooter).setMinRows(delimitersCount + 1)).setStyle(stl.style().setTopBorder(Templates.horizontalLine)) )
        }

        String templateQueryFooter = executedTemplateQuery?.footerText ?: ''
        if (templateQueryFooter.length() > reportFooterMaxLength) {
            templateQueryFooter = templateQueryFooter.replaceAll("\\R", "").substring(0, reportFooterMaxLength)
        }

        if (templateQueryFooter.contains("\"")) {
            templateQueryFooter = templateQueryFooter.replaceAll("\"", "\\\\\"")
        }
        int heighttext = (!params?.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) ? 40 : 30
        TextFieldBuilder textFieldBuilder = cmp.text(exp.jasperSyntax("\"${templateQueryFooter?.replaceAll('"','\\\\"')}\"")) //Added replaceAll to handle doublequotes text.
                .setMinRows(getTemplateQueryFooterRowCount(templateQueryFooter))
                .setHeight(heighttext)
                .setStretchWithOverflow(true)
                .setStyle(stl.style().setVerticalTextAlignment(VerticalTextAlignment.MIDDLE))
                .setRemoveLineWhenBlank(true)
                .setPrintWhenExpression(CustomReportParameters.PRINT_PAGE_FOOTER_TEXT.jasperValue)
        if (isCriteriaSheetOrAppendix || params?.outputFormat != ReportFormatEnum.XLSX.name()){
            textFieldBuilder.setWidth(200)
            if (params?.outputFormat && params?.outputFormat != ReportFormatEnum.HTML.name()) {
                textFieldBuilder.setStyle(stl.style().setFontSize(7)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE))
            } else {
                textFieldBuilder.setStyle(stl.style().setFontSize(11)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE))
            }
        }
        footerComponent.add(textFieldBuilder)

        if (isCriteriaSheetOrAppendix || params?.outputFormat != ReportFormatEnum.XLSX.name()) {
            footerComponent.add(Templates.pageNumberingComponent
                    .setWidth(80)
                    .setRemoveLineWhenBlank(true)
                    .setPrintWhenExpression(CustomReportParameters.PRINT_PAGE_FOOTER_PAGE_NUM.jasperValue))

            int height = (!params?.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) ? 40 : 30
            ImageBuilder img = cmp.image(CustomReportParameters.PAGE_FOOTER_IMAGE.jasperValue)
                    .setWidth(170)
                    .setHeight(height)
                    .setStyle(stl.style().setHorizontalImageAlignment(HorizontalImageAlignment.RIGHT)
                            .setTopPadding(5)
                            .setVerticalImageAlignment(VerticalImageAlignment.MIDDLE))
                    .setRemoveLineWhenBlank(true)
                    .setUsingCache(true)
                    .setPrintWhenExpression(CustomReportParameters.PRINT_PAGE_FOOTER_IMAGE.jasperValue)
            footerComponent.add(img)
        }

        report.pageFooter(footerComponent)
        report.setPageFooterStyle(Templates.pageFooterStyle)
    }

    String setFooterParameters(Map params, JasperReportBuilder report, ExecutedTemplateQuery executedTemplateQuery, boolean isCriteriaSheetOrAppendix) {
        if (!params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) {
            report.ignorePagination()
            String footerText = executedTemplateQuery.footerText ?: ''
            report.setParameter(CustomReportParameters.PAGE_FOOTER_TEXT.jasperName, footerText)
            report.setParameter(CustomReportParameters.PRINT_PAGE_FOOTER_TEXT.jasperName, !!footerText)
        } else if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            report.setPageFormat(Integer.MAX_VALUE, Integer.MAX_VALUE, PageOrientation.PORTRAIT)
            addMaxXLSXRowsPageBreak(report)
            String footerText = executedTemplateQuery?.footerText ?: ''
            report.setParameter(CustomReportParameters.PAGE_FOOTER_TEXT.jasperName, footerText)
            report.setParameter(CustomReportParameters.PRINT_PAGE_FOOTER_TEXT.jasperName, !!footerText)
        } else if (params.outputFormat == ReportFormatEnum.CSV.name()) {
            String footerText = executedTemplateQuery?.footerText ?: ''
            report.setParameter(CustomReportParameters.PRINT_PAGE_FOOTER_PAGE_NUM.jasperName, false)
            report.setParameter(CustomReportParameters.PAGE_FOOTER_TEXT.jasperName, footerText)
            report.setParameter(CustomReportParameters.PRINT_PAGE_FOOTER_TEXT.jasperName, !!footerText)
        } else if (params.outputFormat == ReportFormatEnum.DOCX.name()
                || params.outputFormat == ReportFormatEnum.PDF.name()
                ||params.outputFormat == ReportFormatEnum.PPTX.name()) {
            //footerBuilder.setPrintablePageFooter(report, executedTemplateQuery, isCriteriaSheetOrAppendix, params)
            String footerText = executedTemplateQuery?.footerText ?: ''
            report.setParameter(CustomReportParameters.PRINT_PAGE_FOOTER_PAGE_NUM.jasperName, params.showPageNumbering || (!params.advancedOptions || params.advancedOptions == "0"))
            report.setParameter(CustomReportParameters.PAGE_FOOTER_TEXT.jasperName, footerText)
            report.setParameter(CustomReportParameters.PRINT_PAGE_FOOTER_TEXT.jasperName, !!footerText)

            def sensitivityLabel = getSensitivityLabel(params)
            if (sensitivityLabel) {
                def image = ImageIO.read(imageService.getImage(getSensitivityLabel(params)))
                report.setParameter(CustomReportParameters.PAGE_FOOTER_IMAGE.jasperName, image)
                report.setParameter(CustomReportParameters.PRINT_PAGE_FOOTER_IMAGE.jasperName, image != null)
            } else {
                report.setParameter(CustomReportParameters.PRINT_PAGE_FOOTER_IMAGE.jasperName,false)
            }

            if (params.pageOrientation || params.paperSize) {
                report.setPageFormat(getPaperSize(params), getPageOrientation(params))
                report.setPageMargin(Templates.getPageMargin(getPageOrientation(params)))
            }
        }
    }

    private String getSensitivityLabel(Map params) {
        def sensitivityLabel

        if (params.sensitivityLabel) {
            sensitivityLabel = SensitivityLabelEnum.(params.sensitivityLabel).imageName
        } else {
            sensitivityLabel = SensitivityLabelEnum.CONFIDENTIAL.imageName
        }
        sensitivityLabel
    }

    private PageOrientation getPageOrientation(Map params) {
        def pageOrientation

        if (params.pageOrientation) {
            pageOrientation = PageOrientation.(params.pageOrientation)
        } else {
            pageOrientation = PageOrientation.LANDSCAPE
        }

        pageOrientation
    }

    private PageType getPaperSize(Map params) {
        def paperSize

        if (params.paperSize) {
            paperSize = PageType.(params.paperSize)
        } else {
            paperSize = PageType.LETTER
        }
        paperSize
    }

    private int getTemplateQueryFooterRowCount(String templateQueryFooter) {
        int count = 0
        String[] rows = templateQueryFooter.split("\\r\\n?|\\n")
        for (String row : rows) {
            count += Math.ceil((double)row.length() / TEMPLATE_QUERY_FOOTER_MAX_SYMBOL_IN_ROW)
        }
        return count
    }

    private void addMaxXLSXRowsPageBreak(JasperReportBuilder report) {
        int maxRowsPerSheet = OutputBuilder.XLSX_MAX_ROWS_PER_SHEET - XSLX_HEADER_ROWS_GAP
        BreakBuilder pageBreak = cmp.pageBreak()
        pageBreak.setPrintWhenExpression(exp.jasperSyntax("\$V{PAGE_COUNT} >= ${maxRowsPerSheet}"))
        report.detail(pageBreak)
    }
}
