package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.UserService
import com.rxlogix.config.DateRangeType
import com.rxlogix.config.DetailedCaseSeriesService
import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ReportResult
import com.rxlogix.dynamicReports.reportTypes.DateValueColumnFormatter
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment
import net.sf.dynamicreports.report.constant.LineSpacing
import net.sf.dynamicreports.report.constant.VerticalImageAlignment
import net.sf.dynamicreports.report.constant.WhenNoDataType
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class CaseSeriesReportBuilder {
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    UserService userService = Holders.applicationContext.getBean("userService")
    DetailedCaseSeriesService detailedCaseSeriesService = Holders.applicationContext.getBean("detailedCaseSeriesService")
    String safetySource = Holders.config.getProperty('safety.source')

    private final FIELDS = [
            'caseNumber',
            'versionNumber',
            'globalTags',
            'caseSeriesTags',
            'type',
            'productFamily',
            'eventPI',
            'seriousness',
            'listedness',
            'causality',
            'lockedDate',
            'eventSequenceNumber',
            'eventReceiptDate',
            'eventPreferredTerm',
            'eventSeriousness',
            'comments'
    ]
    private static final TAG_FIELDS = [
            'isNewCase' : [message: 'caseSeries.legend.newCases.label', color:'green'],
            'isManuallyAdded' : [message: 'caseSeries.legend.manuallyAddedCases.label', color:'purple'],
            'isMovedFromOpen' : [message: 'caseSeries.legend.movedFromOpen.label', color:'darkblue'],
            'higherVersionExists': [message: 'caseSeries.legend.higherVersionExists.label', color:'orange']
    ]

    private static final DETAILED_TAG_FIELDS = [
            'NEW_CASE_FLAG' : [message: 'caseSeries.legend.newCases.label', color:'green'],
            'ADDED_MANUAL_FLAG' : [message: 'caseSeries.legend.manuallyAddedCases.label', color:'purple'],
            'UNLOCKED_TO_LOCKED_FLAG': [message: 'caseSeries.legend.movedFromOpen.label', color:'darkblue'],
            'HIGHER_VERSION_FLAG': [message: 'caseSeries.legend.higherVersionExists.label', color:'orange']
    ]

    private static def tagImagesCache = [:]

    public void createCaseSeriesReport(ExecutedCaseSeries caseSeries, List data, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            ReportBuilder reportBuilder = new ReportBuilder()
            JasperReportBuilder report = reportBuilder.initializeNewReport()
            if(safetySource == 'pvcm'){
                FIELDS.removeAll(['causality'])
            }
            if (caseSeries.dateRangeType != DateRangeType?.findByName(Constants.EVENT_RECEIPT_DATE_PVR)) {
                FIELDS.removeAll(['eventSequenceNumber', 'eventReceiptDate', 'eventPreferredTerm', 'eventSeriousness'])
            }

            String header
            switch (params.caseListType) {
                case "openCaseList": header = "${customMessageService.getMessage("app.label.report.case.open.list")}"
                    break
                case "removedCaseList": header = "${customMessageService.getMessage("app.label.report.case.removed.list")}"
                    break
                default: header = "${customMessageService.getMessage("app.label.report.case.list")}"

            }
            header = header.concat(": ${caseSeries.seriesName}")
            if (params.parentId) {
                ExecutedTemplateQuery sourceSection = ReportResult.read(params.parentId)?.executedTemplateQuery
                if (sourceSection)
                    header = header.concat(" (${dynamicReportService.getReportNameAsTitle(sourceSection.executedConfiguration, sourceSection, true)} > ${caseSeries.description ?: params.filePostfix})")
            }
            HeaderBuilder headerBuilder = new HeaderBuilder()
            FooterBuilder footerBuilder = new FooterBuilder()

            headerBuilder.setHeader(null, params, report, null, header, false)
            buildReport(report, new JRBeanCollectionDataSource(data), params)
            footerBuilder.setFooter(params, report, null, false)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = report
            jasperReportBuilderEntry.excelSheetName = header
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private JasperReportBuilder buildReport(JasperReportBuilder report, JRDataSource dataSource, Map params) {
        boolean detailed = !!params.detailed
        report.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
        User user = userService.user
        String pattern = DateUtil.getShortDateFormatForLocale(user?.preference?.locale)
        def tagsCmp = (params.outputFormat != ReportFormatEnum.XLSX.name() ? cmp.image(new TagsExpression(detailed)).setUsingCache(true) : cmp.text(new ExcelTagsExpression(detailed)))
        def tagsColumn = col.componentColumn("", tagsCmp)
                .setWidth(70)
                .setMinHeight(8)
        .setStyle(stl.style()
                .setTopBorder(Templates.columnBorderLine)
                .setBottomBorder(Templates.columnBorderLine)
                .setLeftPadding(1)
                .setRightPadding(1)
                .setLineSpacing(LineSpacing.SINGLE)
                .setImageAlignment(HorizontalImageAlignment.LEFT, VerticalImageAlignment.MIDDLE)
        )
        report.addColumn(tagsColumn)
        if(detailed){
            detailedCaseSeriesService.downloadFields.each {
                def column = null
                if (detailedCaseSeriesService.isDateType(it)) {
                    column = col.column(customMessageService.getMessage("app.caseList.$it"), it, type.dateType())
                            .setWidth(100)
                    column.setPattern(pattern)
                    if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
                        column.setValueFormatter(new DateValueColumnFormatter(pattern))
                    }
                } else {
                    def dataType = it in ["GLOBAL_TAG_TEXT", "ALERT_TAG_TEXT"] ? type.listType() : type.stringType()
                    column = col.column(customMessageService.getMessage("app.caseList.$it"), it, dataType)
                            .setWidth(100)
                }
                report.addColumn(column)
            }
            DETAILED_TAG_FIELDS.keySet().each {
                report.addField(it, Boolean)
            }
        } else {
            FIELDS.findAll { (params.caseListType != "openCaseList" || !it.equals("lockedDate")) && (params.showVersionColumn == "true" || !it.equals("versionNumber")) }.each {
                if (!("comments".equals(it) && "true".equals(params.isTemporary))) {
                    def column = null
                    if ("lockedDate".equals(it) || "eventReceiptDate".equals(it)) {
                        column = col.column(customMessageService.getMessage("app.caseList.$it"), it, type.dateType())
                                .setWidth(100)
                        column.setPattern(pattern)
                        if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
                            column.setValueFormatter(new DateValueColumnFormatter(pattern))
                        }
                    } else {
                        def dataType = it in ["globalTags", "caseSeriesTags"] ? type.listType() : type.stringType()
                        column = col.column(customMessageService.getMessage("app.caseList.$it"), it, dataType)
                                .setWidth(100)
                    }
                    report.addColumn(column)
                }
            }
            TAG_FIELDS.keySet().each {
                report.addField(it, Boolean)
            }
        }
        if (params.outputFormat != ReportFormatEnum.XLSX.name()) {
            addTagsLegend(report)
        }
        report.setDataSource(dataSource)
    }

    private void addTagsLegend(JasperReportBuilder report) {
        def footer = cmp.horizontalList(cmp.text(customMessageService.getMessage('caseSeries.legend.label') + ":"))
        TAG_FIELDS.entrySet().each {
            Image tagImage = getSingleTagImage(it.value.color)
            footer.add(cmp.image(tagImage).setUsingCache(true)
                    .setFixedDimension(8, 14)
                    .setStyle(stl.style().setVerticalImageAlignment(VerticalImageAlignment.MIDDLE)))
            footer.add(cmp.text(customMessageService.getMessage(it.value.message)))
        }
        report.addSummary(footer)
    }

    private static Image getSingleTagImage(String colorName) {
        def image = tagImagesCache.get(colorName)
        if (!image) {
            image = ImageIO.read(Templates.class.getResourceAsStream("rc/tag-${colorName}.png"))
            if (image) {
                tagImagesCache.put(colorName, image)
            }
        }
        return image
    }

    private static class TagsExpression extends AbstractSimpleExpression<Image> {
        private boolean detailed
        private static final int RESULT_IMAGE_WIDTH = 64
        private static final int RESULT_IMAGE_HEIGHT = 16

        TagsExpression(boolean detailed){
            this.detailed = detailed
        }

        @Override
        Image evaluate(ReportParameters reportParameters) {
            BufferedImage result = new BufferedImage(
                    RESULT_IMAGE_WIDTH, RESULT_IMAGE_HEIGHT,
                    BufferedImage.TYPE_INT_ARGB)
            Graphics g = result.getGraphics()
            int x = 0, y = 0
            (detailed ? DETAILED_TAG_FIELDS : TAG_FIELDS).entrySet().each {
                if (reportParameters.getFieldValue(it.key) as Boolean) {
                    Image tagImage = getSingleTagImage(it.value.color)
                    if (tagImage) {
                        g.drawImage(tagImage, x, y, null)
                        x += tagImage.getWidth(null)
                    }
                }
            }
            return result
        }
    }

    private class ExcelTagsExpression extends AbstractSimpleExpression<String> {
        private boolean detailed
        private static final int RESULT_IMAGE_WIDTH = 64
        private static final int RESULT_IMAGE_HEIGHT = 16

        ExcelTagsExpression(boolean detailed){
            this.detailed = detailed
        }
        @Override
        String evaluate(ReportParameters reportParameters) {
            def result = [];
            (detailed ? DETAILED_TAG_FIELDS : TAG_FIELDS).entrySet().each {
                if (reportParameters.getFieldValue(it.key) as Boolean) {
                    result << customMessageService.getMessage(it.value.message)
                }
            }
            return result.join(", ")
        }
    }
}
