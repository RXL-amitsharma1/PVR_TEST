package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.config.BaseCaseSeries
import com.rxlogix.CaseSeriesService
import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.util.ViewHelper
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.jasperreports.engine.JREmptyDataSource


import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.exp

class CaseSeriesCriteriaSheetBuilder extends CriteriaSheetBuilder {

    CaseSeriesService caseSeriesService = Holders.applicationContext.getBean("caseSeriesService")

    void createCriteriaSheet(ExecutedCaseSeries seriesInstance, Map params,
                             ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            ReportBuilder reportBuilder = new ReportBuilder()
            JasperReportBuilder criteriaSheet = reportBuilder.initializeNewReport()
            criteriaSheet.setDataSource(new JREmptyDataSource())

            String header = customMessageService.getMessage("jasperReports.criteriaSheet")
            HeaderBuilder headerBuilder = new HeaderBuilder()
            FooterBuilder footerBuilder = new FooterBuilder()

            headerBuilder.setHeader(null, params, criteriaSheet, null, header, true)
            buildCriteriaSheet(criteriaSheet, seriesInstance)
            footerBuilder.setFooter(params, criteriaSheet, null, true)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = criteriaSheet
            jasperReportBuilderEntry.excelSheetName = header
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private buildCriteriaSheet(JasperReportBuilder report, ExecutedCaseSeries seriesInstance) {
        ComponentBuilder<?, ?> criteriaSheetHeader = createCriteriaSheetHeader(seriesInstance)
        criteriaSheetHeader.setPrintWhenExpression(exp.printInFirstPage())
        criteriaSheetHeader.setRemoveLineWhenBlank(true)
        report.addDetail(criteriaSheetHeader)
    }

    private ComponentBuilder<?, ?> createCriteriaSheetHeader(ExecutedCaseSeries seriesInstance) {
        HorizontalListBuilder reportCriteriaList = cmp.horizontalList()
        Locale locale = seriesInstance.owner.preference?.locale
        //Top Section
        addCriteriaSheetSectionTitle(reportCriteriaList, "app.label.selectionCriteria")
        addCriteriaSheetAttribute(reportCriteriaList, "caseSeries.name.label", seriesInstance.seriesName)
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.description", seriesInstance.description ?: "")
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.qualityChecked", seriesInstance.qualityChecked ?
                customMessageService.getMessage("app.label.yes") :
                customMessageService.getMessage("app.label.no"))
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.tag", getTagsValue(seriesInstance))
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.SuspectProduct", seriesInstance.suspectProduct ?
                customMessageService.getMessage("app.label.yes") :
                customMessageService.getMessage("app.label.no"))
        addCriteriaSheetAttribute(reportCriteriaList, "app.productDictionary.label", getProductSelectionValue(seriesInstance))
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.productDictionary.include.who.drugs", seriesInstance.includeWHODrugs?"Yes":"No")
        if(Holders.config.getProperty('safety.source') == Constants.PVCM)
            addCriteriaSheetAttribute(reportCriteriaList, "app.label.productDictionary.multi.substance", seriesInstance.isMultiIngredient?"Yes":"No")
        else
            addCriteriaSheetAttribute(reportCriteriaList, "app.label.productDictionary.multi.ingredient", seriesInstance.isMultiIngredient?"Yes":"No")
        addCriteriaSheetAttribute(reportCriteriaList,"app.eventDictId.label", getEventSelectionValue(seriesInstance))
        addCriteriaSheetAttribute(reportCriteriaList, "app.studyDictionary.label", getStudySelectionValue(seriesInstance))
        if(seriesInstance.dateRangeType){
            addCriteriaSheetAttribute(reportCriteriaList, "app.label.DateRangeType", customMessageService.getMessage(seriesInstance.dateRangeType.i18nKey))
        }
        addCriteriaSheetAttribute(reportCriteriaList, "evaluate.on.label", getEvaluateOnValue(seriesInstance))

        if(seriesInstance.executedCaseSeriesDateRangeInformation.dateRangeEndAbsolute) {
            addCriteriaSheetAttribute(reportCriteriaList, "app.label.DateRange", caseSeriesService.getDateRangeValueForCriteria(seriesInstance, locale))
        }

        if (seriesInstance.executedGlobalQuery) {
            addCriteriaSheetAttribute(reportCriteriaList, "app.label.queryName", seriesInstance?.executedGlobalQuery?.name ?: customMessageService.getMessage("app.label.none"))
            addCriteriaSheetAttribute(reportCriteriaList, "app.label.parameters", formatParameters(seriesInstance?.executedGlobalQueryValueLists),true)
        }
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.scheduledBy", seriesInstance.owner.fullName)
        addCriteriaSheetAttribute(reportCriteriaList, "reportCriteria.exclude.follow.up", seriesInstance.excludeFollowUp ?
                customMessageService.getMessage("app.label.yes") :
                customMessageService.getMessage("app.label.no"))
        addCriteriaSheetAttribute(reportCriteriaList, "reportCriteria.include.locked.versions.only", seriesInstance.includeLockedVersion ?
                customMessageService.getMessage("app.label.yes") :
                customMessageService.getMessage("app.label.no"))
        addCriteriaSheetAttribute(reportCriteriaList, "reportCriteria.include.all.study.drugs.cases", seriesInstance.includeAllStudyDrugsCases ?
                customMessageService.getMessage("app.label.yes") :
                customMessageService.getMessage("app.label.no"))
        addCriteriaSheetAttribute(reportCriteriaList, "reportCriteria.exclude.non.valid.cases", seriesInstance.excludeNonValidCases ?
                customMessageService.getMessage("app.label.yes") :
                customMessageService.getMessage("app.label.no"))
        addCriteriaSheetAttribute(reportCriteriaList, "reportCriteria.exclude.deleted.cases", seriesInstance.excludeDeletedCases ?
                customMessageService.getMessage("app.label.yes") :
                customMessageService.getMessage("app.label.no"))
        return reportCriteriaList
    }

    private String getTagsValue(BaseCaseSeries seriesInstance) {
        if (seriesInstance.tags?.name) {
            return seriesInstance.tags?.name.join(",")
        }
        return  customMessageService.getMessage("app.label.none")
    }

    private String getProductSelectionValue(BaseCaseSeries seriesInstance) {
        if (seriesInstance.productSelection) {
            return ViewHelper.getDictionaryValues(seriesInstance, DictionaryTypeEnum.PRODUCT)
        }
        return customMessageService.getMessage("app.label.none")
    }
    private String getEventSelectionValue(BaseCaseSeries seriesInstance) {
        if(seriesInstance.eventSelection) {
            return ViewHelper.getDictionaryValues(seriesInstance , DictionaryTypeEnum.EVENT)
        }
        return customMessageService.getMessage("app.label.none")
    }

    private String getStudySelectionValue(BaseCaseSeries seriesInstance) {
        if (seriesInstance.studySelection) {
            return ViewHelper.getDictionaryValues(seriesInstance, DictionaryTypeEnum.STUDY)
        }
        return customMessageService.getMessage("app.label.none")
    }

    private String getEvaluateOnValue(BaseCaseSeries seriesInstance) {
        StringBuilder sb = new StringBuilder()
        if (seriesInstance.evaluateDateAs) {
            sb.append(customMessageService.getMessage(EvaluateCaseDateEnum.(seriesInstance.evaluateDateAs).i18nKey))
            if (seriesInstance.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
                sb.append(" ")
                Locale locale = seriesInstance.owner.preference?.locale
                sb.append(formatDateForCriteriaWithoutTZ(seriesInstance.asOfVersionDate, locale))
            }
        }
        return sb.toString()
    }
}
