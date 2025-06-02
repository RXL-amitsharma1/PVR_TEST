package com.rxlogix.dynamicReports

import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.config.ActionItem
import com.rxlogix.config.Capa8D
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.DynamicReports
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment
import net.sf.dynamicreports.report.constant.Markup
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JREmptyDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class Capa8dCriteriaSheetBuilder {

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")

    private static final String ACTION_ITEM_CATEGORY = "actionCategory"
    private static final String PRIORITY = "priority"
    private static final String ASSIGNED_TO = "assignedTo"
    private static final String DUE_DATE = "dueDate"
    private static final String COMPLETION_DATE = "completionDate"
    private static final String STATUS = "status"
    private static final String DESCRIPTION = "description"
    private static final String AI_CREATED_BY = "createdBy"
    private static final String AI_DATE_CREATED = "dateCreated"
    private static final String ISSUE_NUMBER = "issueNumber"
    private static final String ISSUE_TYPE = "issueType"
    private static final String CATEGORY = "category"
    private static final String CORRECTIVE_ACTION = "correctiveAction"
    private static final String PREVENTIVE_ACTION = "preventiveAction"



    def createCapa8dCriteriaSheet(List<Capa8D> capa8DList, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList){
        if (dynamicReportService.isInPrintMode(params)) {
            ReportBuilder reportBuilder = new ReportBuilder()
            JasperReportBuilder criteriaSheet = reportBuilder.initializeNewReport()
            criteriaSheet.setDataSource(new JREmptyDataSource())

            //todo:  get the header from setHeaderAndFooter return value?
            String header = customMessageService.getMessage("quality.capa.issue.list.label")
            HeaderBuilder headerBuilder = new HeaderBuilder()
            FooterBuilder footerBuilder = new FooterBuilder()

            headerBuilder.setCapaHeader(params, criteriaSheet, header, true)
            buildCriteriaSheet(criteriaSheet, capa8DList, params)
            footerBuilder.setFooter(params, criteriaSheet, null, true)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = criteriaSheet
            jasperReportBuilderEntry.excelSheetName = header
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private buildCriteriaSheet(JasperReportBuilder report, List<Capa8D> capa8DList, Map params) {

        JasperReportBuilder subreport = DynamicReports.report()
        if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            subreport.setIgnorePageWidth(true)
        }

        subreport.setTemplate(Templates.reportTemplate)
        subreport.setDefaultFont(Templates.defaultFontStyle)
        subreport.setColumnTitleStyle(Templates.columnTitleStyle)
        subreport.setColumnStyle(Templates.columnStyle)
        subreport.addColumn(col.column(customMessageService.getMessage("quality.capa.capaNumber.label"), ISSUE_NUMBER, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("quality.capa.issueType.label"), ISSUE_TYPE, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("quality.capa.category.label"), CATEGORY, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("quality.capa.corrective.action.label"), CORRECTIVE_ACTION, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("quality.capa.preventive.action.label"), PREVENTIVE_ACTION, type.stringType()))
        subreport.setDataSource(createCriteriaSheetDataSource(capa8DList))
        report.setDataSource(new JREmptyDataSource())
        report.addDetail(cmp.subreport(subreport))
    }

    private JRDataSource createCriteriaSheetDataSource(List<Capa8D> capaList) {

        Collection<Map<java.lang.String,?>> result = new LinkedList<>()
        capaList.each {
            Map item = [:]
            item.put(ISSUE_NUMBER, it?.issueNumber ?: '')
            item.put(ISSUE_TYPE, it?.issueType ?: '')
            item.put(CATEGORY, it?.category ?: '')
            item.put(CORRECTIVE_ACTION, it?.getCorrectiveActions().size() > 0 ? 'YES': 'NO')
            item.put(PREVENTIVE_ACTION, it?.getPreventiveActions().size() > 0 ? 'YES': 'NO')
            result.add(item)
        }
        return new JRMapCollectionDataSource(result)
    }



    def createCapa8dSheet(Capa8D capaInstance, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList){
        if (dynamicReportService.isInPrintMode(params)) {
            ReportBuilder reportBuilder = new ReportBuilder()
            JasperReportBuilder criteriaSheet = reportBuilder.initializeNewReport()
            criteriaSheet.setDataSource(new JREmptyDataSource())

            //todo:  get the header from setHeaderAndFooter return value?
            String header = customMessageService.getMessage("quality.capa.issue.detail.label") + " : "+capaInstance.issueNumber
            HeaderBuilder headerBuilder = new HeaderBuilder()
            FooterBuilder footerBuilder = new FooterBuilder()

            headerBuilder.setHeader(null, params, criteriaSheet, null, header, true)
            buildSheet(criteriaSheet, capaInstance, params)
            footerBuilder.setFooter(params, criteriaSheet, null, true)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = criteriaSheet
            jasperReportBuilderEntry.excelSheetName = header
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private buildSheet(JasperReportBuilder report, Capa8D capaInstance, Map params) {

        ComponentBuilder<?, ?> criteriaSheetHeader = createCriteriaSheetHeader(capaInstance)
        criteriaSheetHeader.setPrintWhenExpression(exp.printInFirstPage())
        criteriaSheetHeader.setRemoveLineWhenBlank(true)
        report.addDetail(criteriaSheetHeader)

        JasperReportBuilder subreport = DynamicReports.report()
        if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            subreport.setIgnorePageWidth(true)
        }

        subreport.setTemplate(Templates.reportTemplate)
        subreport.setDefaultFont(Templates.defaultFontStyle)
        subreport.setColumnTitleStyle(Templates.columnTitleStyle)
        subreport.setColumnStyle(Templates.columnStyle)
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.action.item.action.category"), ACTION_ITEM_CATEGORY, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.action.item.priority"), PRIORITY, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.action.item.assigned.to"), ASSIGNED_TO, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.action.item.due.date"), DUE_DATE, type.dateType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.action.item.completion.date"), COMPLETION_DATE, type.dateType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.action.item.status"), STATUS, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.action.item.description"), DESCRIPTION, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.action.item.created.by"), AI_CREATED_BY, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.action.item.date.created"), AI_DATE_CREATED, type.dateType()))
        subreport.setDataSource(createSheetDataSource(capaInstance))
        report.setDataSource(new JREmptyDataSource())
        report.addDetail(cmp.subreport(subreport))
    }

    private createCriteriaSheetHeader(Capa8D capaInstance) {
        HorizontalListBuilder reportCriteriaList = cmp.horizontalList()
        //Top Section
        addCriteriaSheetSectionTitle(reportCriteriaList, "app.label.basic.information")

        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.capaNumber.label", capaInstance?.issueNumber ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.issueType.label", capaInstance?.issueType ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.category.label", capaInstance?.category ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.remarks.label", capaInstance?.remarks?.join(", ") ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.approvedBy.label", capaInstance?.approvedBy?.username ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.initiator.label", capaInstance?.initiator?.username ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.teamLead.label", capaInstance?.teamLead?.username ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.teamMembers.label", capaInstance?.teamMembers?.username?.join(", ") ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.case.series.description", capaInstance?.description ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.rootCause.label", capaInstance?.rootCause ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.verificationResults.label", capaInstance?.verificationResults ?: ' ')
        addCriteriaSheetAttribute(reportCriteriaList, "quality.capa.comments.label", capaInstance?.comments ?: ' ')

        HorizontalListBuilder reportSectionsList = cmp.horizontalList().setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT).setTopPadding(30))
        addCriteriaSheetSectionTitle(reportSectionsList, "quality.issue.actions.label")
        return cmp.multiPageList(reportCriteriaList, reportSectionsList)
    }

    protected void addCriteriaSheetSectionTitle(HorizontalListBuilder list, String labelId) {
        list.add(cmp.text(customMessageService.getMessage(labelId)).setStyle(Templates.criteriaSectionTitleStyle)).newRow()
    }

    protected void addCriteriaSheetAttribute(HorizontalListBuilder list, String labelId, String value, boolean isHtmlValue = false) {
        if (value) {
            value = MiscUtil.matchCSVPattern(value)
            def valueField = cmp.text(value).setStyle(Templates.criteriaValueStyle)
            if (isHtmlValue) {
                valueField.setMarkup(Markup.HTML)
            }
            list.add(cmp.text(customMessageService.getMessage(labelId) + (labelId ? ":" : "")).setFixedColumns(25).setStyle(Templates.criteriaNameStyle),
                    valueField).newRow()
        }
    }

    private JRDataSource createSheetDataSource(Capa8D capaInstance) {

        Collection<Map<java.lang.String,?>> result = new LinkedList<>()
        List<ActionItem> parameterDataList = new ArrayList<>();
        parameterDataList.addAll(capaInstance.getCorrectiveActions())
        parameterDataList.addAll(capaInstance.getPreventiveActions())
        if(parameterDataList.size() > 0){
            parameterDataList.each {
                Map item = [:]
                item.put(ACTION_ITEM_CATEGORY, it?.actionCategory?.name.toString() ?: '')
                item.put(PRIORITY, it?.priority ?: '')
                if(it?.assignedTo){
                    item.put(ASSIGNED_TO, it.assignedTo.username)
                }else if(it?.assignedGroupTo){
                    item.put(ASSIGNED_TO, it.assignedGroupTo.name)
                }else{
                    item.put(ASSIGNED_TO, '')
                }
                item.put(DUE_DATE, it?.dueDate ?: null)
                item.put(COMPLETION_DATE, it?.completionDate ?: null)
                item.put(STATUS, it.status?.getKey() ?: '')
                item.put(DESCRIPTION, it?.description ?: '')
                item.put(AI_CREATED_BY, it?.createdBy ?: '')
                item.put(AI_DATE_CREATED, it?.dateCreated ?: '')
                result.add(item)
            }
        }else{
            for(int i=0; i < 2; i++){
                Map item = [:]
                if(i == 0){
                    item.put(ACTION_ITEM_CATEGORY, customMessageService.getMessage("quality.capa.corrective.label"))
                }else{
                    item.put(ACTION_ITEM_CATEGORY, customMessageService.getMessage("quality.capa.preventive.label"))
                }
                result.add(item)
            }
        }
        return new JRMapCollectionDataSource(result)
    }

}
