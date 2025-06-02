package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.localization.Localization
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@Transactional(readOnly = true)
class ExcelExportService {
    static transactional = false
    static scope = "request"
    final static String CCLTEMPLATE_TEMPLATE_SHEET = "ccltemplate template";
    final static String DTTEMPLATE_TEMPLATE_SHEET = "dttemplate template";
    final static String SQLTEMPLATE_TEMPLATE_SHEET = "sqltemplate template";
    final static String NCTEMPLATE_TEMPLATE_SHEET = "nctemplate template";
    final static String TEMPLATESET_TEMPLATE_SHEET = "templateset template";
    final static String QUERY_TEMPLATE_SHEET = "Queries";
    final static String ADHOC_TEMPLATE_SHEET = "adhoc template";
    final static String AGGREGATE_TEMPLATE_SHEET = "aggregate template";
    final static String JSON_TEMPLATE_SHEET = "JSON";
    final static String TEMPLATE_FILE = 'export/template.xlsx';
    final static String SCHEDULER_UNTIL_TOKEN = "UNTIL";

    static enum BlockType{
        CCL,
        DT,
        DTC
    }

    int counter = 0
    def queryService
    def templateService
    def configurationService

    Map export(List entityList) {
        List<Map> errors = []
        List<String> fileNames = []
        counter = 0
        Workbook workbook = getNewExcelFile()
        entityList.each { entity ->
            try {
                if (entity?.instanceOf(ReportTemplate)) exportTemplate(workbook, (ReportTemplate) entity)
                if (entity?.instanceOf(SuperQuery)) exportQuery(workbook, (SuperQuery) entity)
                if (entity?.instanceOf(Configuration)) exportAdhoc(workbook, (Configuration) entity)
                if (entity?.instanceOf(PeriodicReportConfiguration)) exportAggregate(workbook, (PeriodicReportConfiguration) entity)
            } catch (Exception e) {
                log.error("Error during ExcelExportService -> export", e)
                StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));
                if (entity?.instanceOf(ReportTemplate)) errors << [type: ViewHelper.getMessage("app.label.template"), id: entity.id, name: entity.name, error: stackTrace.toString()]
                if (entity?.instanceOf(SuperQuery)) errors << [type: ViewHelper.getMessage("app.label.query"), id: entity.id, name: entity.name, error: stackTrace.toString()]
                if (entity?.instanceOf(Configuration)) errors << [type: ViewHelper.getMessage("app.configurationType.ADHOC_REPORT"), id: entity.id, name: entity.reportName, error: stackTrace.toString()]
                if (entity?.instanceOf(PeriodicReportConfiguration)) errors << [type: ViewHelper.getMessage("app.configurationType.PERIODIC_REPORT"), id: entity.id, name: entity.reportName, error: stackTrace.toString()]

                if (!entity?.instanceOf(SuperQuery)) {
                    counter--
                    workbook.removeSheetAt(workbook.getNumberOfSheets() - 1);
                }
            }
            if (counter > 500) { //preventing creating huge file
                fileNames << saveFile(workbook)
                counter = 0
                workbook = getNewExcelFile()
            }

        }
        if (counter > 0)
            fileNames << saveFile(workbook)
        return [files: fileNames, errors: errors]
    }

    private Workbook getNewExcelFile() {
        return new XSSFWorkbook(this.class.classLoader.getResourceAsStream(TEMPLATE_FILE));
    }

    private String saveFile(Workbook workbook) {
        workbook.removeSheetAt(workbook.getSheetIndex(DTTEMPLATE_TEMPLATE_SHEET));
        workbook.removeSheetAt(workbook.getSheetIndex(SQLTEMPLATE_TEMPLATE_SHEET));
        workbook.removeSheetAt(workbook.getSheetIndex(CCLTEMPLATE_TEMPLATE_SHEET));
        workbook.removeSheetAt(workbook.getSheetIndex(NCTEMPLATE_TEMPLATE_SHEET));
        workbook.removeSheetAt(workbook.getSheetIndex(TEMPLATESET_TEMPLATE_SHEET));
        workbook.removeSheetAt(workbook.getSheetIndex(ADHOC_TEMPLATE_SHEET));
        workbook.removeSheetAt(workbook.getSheetIndex(AGGREGATE_TEMPLATE_SHEET));
        workbook.setSheetName(workbook.getSheetIndex(QUERY_TEMPLATE_SHEET),ViewHelper.getMessage("app.queries.sheet.name"));
        workbook.setSheetOrder(JSON_TEMPLATE_SHEET,workbook.getNumberOfSheets()-1);

        String fileName = System.currentTimeMillis() + '.xlsx'
        FileOutputStream fileOut = new FileOutputStream(Holders.config.getProperty('tempDirectory') + '/' + fileName);
        workbook.write(fileOut);
        return fileName
    }

    private void exportAggregate(Workbook workbook, PeriodicReportConfiguration configuration) {
        counter++
        int templateIndex = workbook.getSheetIndex(AGGREGATE_TEMPLATE_SHEET);

        Sheet sheet = workbook.cloneSheet(templateIndex);
        setSheetName(workbook, sheet, configuration.reportName)

        setValueToCell(sheet, 1, 1, ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT))
        setValueToCell(sheet, 1, 3, configuration.suspectProduct)
        setValueToCell(sheet, 2, 1, ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.STUDY))
        setValueToCell(sheet, 2, 3, configuration.globalQuery?.name)
        setValueToCell(sheet, 3, 3, configuration.globalQueryValueLists?.collect { tvl ->
            tvl.query.name + "( " + tvl.parameterValues.collect {
                if (it.hasProperty('reportField'))
                    ViewHelper.getMessage("app.reportField." + it.reportField.name) + " " + ViewHelper.getMessage(it.operator.getI18nKey()) + " " + it.value ?: ''
                else
                    it.key + " = " + it.value ?: ''
            }.join(", ") + ")"
        }.join("; "))
        if (configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF)
            setValueToCell(sheet, 3, 1, ViewHelper.getMessage(configuration.evaluateDateAs.i18nKey) + " (" + configuration.asOfVersionDate.format(DateUtil.ISO_DATE_FORMAT) + ")")
        else
            setValueToCell(sheet, 3, 1, ViewHelper.getMessage(configuration.evaluateDateAs.i18nKey))

        setValueToCell(sheet, 4, 1, ViewHelper.getMessage(configuration.dateRangeType.i18nKey))
        String dr = ViewHelper.getMessage(configuration.globalDateRangeInformation?.dateRangeEnum?.i18nKey)
        if (DateRangeEnum.getRelativeDateOperatorsWithX().contains(configuration.globalDateRangeInformation?.dateRangeEnum)) {
             dr += " " + ViewHelper.getMessage("app.excelExport.whereX") + " = " + (configuration.globalDateRangeInformation?.relativeDateRangeValue)
        }
        if (configuration.globalDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {
           dr = configuration.globalDateRangeInformation?.dateRangeStartAbsolute?.format(DateUtil.ISO_DATE_FORMAT) + " - " +
                    configuration.globalDateRangeInformation?.dateRangeEndAbsolute?.format(DateUtil.ISO_DATE_FORMAT)
        }
        setValueToCell(sheet, 4, 3, dr)
        setValueToCell(sheet, 5, 1, configuration.includePreviousMissingCases)
        setValueToCell(sheet, 5, 3, configuration.includeOpenCasesInDraft)
        setValueToCell(sheet, 6, 1, configuration.excludeFollowUp)
        setValueToCell(sheet, 6, 3, configuration.includeAllStudyDrugsCases)
        setValueToCell(sheet, 7, 1, configuration.excludeNonValidCases)

        setValueToCell(sheet, 10, 1, configuration.reportName)
        setValueToCell(sheet, 10, 3, configuration.owner.fullName)
        setValueToCell(sheet, 11, 1, configuration.description)
        setValueToCell(sheet, 11, 3, (configuration.tags*.name).join(", "))
        setValueToCell(sheet, 12, 1, ViewHelper.getMessage(configuration.periodicReportType.i18nKey))
        setValueToCell(sheet, 12, 3, configuration.generateCaseSeries)
        setValueToCell(sheet, 13, 1, configuration.qualityChecked)


        setValueToCell(sheet, 16, 1,  getSharedWith(configuration))
        setValueToCell(sheet, 17, 1, configuration.deliveryOption?.emailToUsers?.join(", "))
        setValueToCell(sheet, 18, 1, configuration.deliveryOption?.attachmentFormats?.collect { ViewHelper.getMessage(it.i18nKey) }?.join(", "))

        if (configuration.scheduleDateJSON) {
            def scheduler = JSON.parse(configuration.scheduleDateJSON)
            setValueToCell(sheet, 16, 3, scheduler.startDateTime)
            setValueToCell(sheet, 17, 3, scheduler.timeZone?.text)
            def s = []
            scheduler?.recurrencePattern?.split(';')?.each {
                def set = it.split("=")
                if (set[0] == SCHEDULER_UNTIL_TOKEN)
                    s << ViewHelper.getMessage("app.excelExport.until") + " " + Date.parse("yyyyMMdd", set[1]).format(DateUtil.ISO_DATE_FORMAT)
                else
                    s << ViewHelper.getMessage("scheduler." + set[0].toLowerCase(), null, set[0].toLowerCase()) + ": " + ViewHelper.getMessage("scheduler." + set[1].toLowerCase(), null, set[1].toLowerCase())
            }
            setValueToCell(sheet, 18, 3, s.join("; "))
        }
        setValueToCell(sheet, 19, 1, configuration.dueInDays)
        setValueToCell(sheet, 19, 3, configuration.reportingDestinations.join(", "))
        int blockStart = 23
        int blockSize = 14
        configuration.templateQueries.eachWithIndex { templateQuery, int i ->
            if (i > 0) {
                for (int j = 0; j < blockSize; j++) {
                    copyRow(workbook, sheet, blockStart + j, sheet.getLastRowNum() + 1)
                }
            }
            setValueToCell(sheet, blockStart + blockSize * i, 0, i + 1)
            setValueToCell(sheet, blockStart + blockSize * i, 2, templateQuery.template.name)
            setValueToCell(sheet, blockStart + blockSize * i, 3, templateQuery.templateValueLists.collect { tvl ->
                tvl.template?.name + "( " + tvl.parameterValues.collect { it.key + " = " + it.value ?: '' }.join(", ") + ")"
            }.join("; "))
            setValueToCell(sheet, blockStart + blockSize * i + 1, 2, templateQuery.query?.name)
            setValueToCell(sheet, blockStart + blockSize * i + 1, 3, templateQuery.queryValueLists.collect { tvl ->
                tvl.query.name + "( " + tvl.parameterValues.collect {
                    if (it.hasProperty('reportField'))
                        ViewHelper.getMessage("app.reportField." + it.reportField.name) + " " + ViewHelper.getMessage(it.operator.getI18nKey()) + " " + it.value ?: ''
                    else
                        it.key + " = " + it.value ?: ''
                }.join(", ") + ")"
            }.join("; "))
            setValueToCell(sheet, blockStart + blockSize * i + 2, 2, ViewHelper.getMessage(templateQuery.queryLevel.i18nKey))
            setValueToCell(sheet, blockStart + blockSize * i + 3, 2, ViewHelper.getMessage(templateQuery.dateRangeInformationForTemplateQuery.dateRangeEnum.i18nKey))
            if (DateRangeEnum.getRelativeDateOperatorsWithX().contains(templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum)) {
                setValueToCell(sheet, blockStart + blockSize * i + 3, 3, ViewHelper.getMessage("app.excelExport.whereX") + " " + (templateQuery?.dateRangeInformationForTemplateQuery?.relativeDateRangeValue))
            }
            if (templateQuery.dateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.CUSTOM) {
                setValueToCell(sheet, blockStart + blockSize * i + 3, 3, templateQuery.dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute?.format(DateUtil.ISO_DATE_FORMAT) + " - " +
                        templateQuery.dateRangeInformationForTemplateQuery?.dateRangeEndAbsolute?.format(DateUtil.ISO_DATE_FORMAT))
            }
            setValueToCell(sheet, blockStart + blockSize * i + 4, 2, templateQuery.header)
            setValueToCell(sheet, blockStart + blockSize * i + 5, 2, templateQuery.title)
            setValueToCell(sheet, blockStart + blockSize * i + 6, 2, templateQuery.footer)
            setValueToCell(sheet, blockStart + blockSize * i + 7, 2, templateQuery.headerProductSelection)
            setValueToCell(sheet, blockStart + blockSize * i + 8, 2, templateQuery.headerDateRange)
            setValueToCell(sheet, blockStart + blockSize * i + 9, 2, templateQuery.draftOnly)
            setValueToCell(sheet, blockStart + blockSize * i + 10, 2, templateQuery.privacyProtected)
            setValueToCell(sheet, blockStart + blockSize * i + 11, 2, templateQuery.blindProtected)
            setValueToCell(sheet, blockStart + blockSize * i + 12, 2, templateQuery.displayMedDraVersionNumber)
            setValueToCell(sheet, blockStart + blockSize * i + 13, 2, ViewHelper.getMessage(templateQuery.granularity?.i18nKey))
        }
        exportReportJson(workbook,  configuration)
    }

    private String getSharedWith(ReportConfiguration configuration) {
        Set<UserGroup> executableByGroup = configuration.getExecutableByGroup()
        Set<User> executableBy = configuration.getExecutableByUser()
        String groups = configuration.shareWithGroups?.collect { ug ->
            String editable = executableByGroup.find { it.id == ug.id } ? " (" + ViewHelper.getMessage("app.label.canEdit") + ")" : ""
            ug.name + editable
        }?.join(", ")
        String users = configuration.shareWithUsers?.collect { u ->
            String editable = executableBy.find { it.id == u.id } ? " (" + ViewHelper.getMessage("app.label.canEdit") + ")" : ""
            u.fullName + editable
        }?.join(", ")
        return ViewHelper.getMessage("app.excelExport.groups") + " " + (groups ?: " - ") + "; " + ViewHelper.getMessage("userGroup.users.label") + ": " + (users ?: " - ")
    }

    private void exportAdhoc(Workbook workbook, Configuration configuration) {
        counter++
        int templateIndex = workbook.getSheetIndex(ADHOC_TEMPLATE_SHEET);

        Sheet sheet = workbook.cloneSheet(templateIndex);
        setSheetName(workbook, sheet, configuration.reportName)

        setValueToCell(sheet, 1, 1, ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT))
        setValueToCell(sheet, 2, 1, ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.STUDY))
        setValueToCell(sheet, 3, 1, ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.EVENT))
        if (configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF)
            setValueToCell(sheet, 4, 1, ViewHelper.getMessage(configuration.evaluateDateAs.i18nKey) + " (" + configuration.asOfVersionDate.format(DateUtil.ISO_DATE_FORMAT) + ")")
        else
            setValueToCell(sheet, 4, 1, ViewHelper.getMessage(configuration.evaluateDateAs.i18nKey))

        setValueToCell(sheet, 5, 1, ViewHelper.getMessage(configuration.dateRangeType.i18nKey))
        String dr = ViewHelper.getMessage(configuration.globalDateRangeInformation?.dateRangeEnum?.i18nKey)
        if (DateRangeEnum.getRelativeDateOperatorsWithX().contains(configuration.globalDateRangeInformation?.dateRangeEnum)) {
             dr += " " + ViewHelper.getMessage("app.excelExport.whereX") + " = " + (configuration.globalDateRangeInformation?.relativeDateRangeValue)
        }
        if (configuration.globalDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {
            dr= configuration.globalDateRangeInformation?.dateRangeStartAbsolute?.format(DateUtil.ISO_DATE_FORMAT) + " - " +
                    configuration.globalDateRangeInformation?.dateRangeEndAbsolute?.format(DateUtil.ISO_DATE_FORMAT)
        }
        setValueToCell(sheet, 6, 1, dr)
        setValueToCell(sheet, 7, 1, configuration.useCaseSeries?.seriesName)
        setValueToCell(sheet, 8, 1, configuration.globalQuery?.name)
        setValueToCell(sheet, 9, 1, configuration.globalQueryValueLists?.collect { tvl ->
            tvl.query.name + "( " + tvl.parameterValues.collect {
                if (it.hasProperty('reportField'))
                    ViewHelper.getMessage("app.reportField." + it.reportField.name) + " " + ViewHelper.getMessage(it.operator.getI18nKey()) + " " + it.value ?: ''
                else
                    it.key + " = " + it.value ?: ''
            }.join(", ") + ")"
        }.join("; "))

        setValueToCell(sheet, 1, 3, configuration.suspectProduct)
        setValueToCell(sheet, 2, 3, configuration.limitPrimaryPath)
        setValueToCell(sheet, 3, 3, configuration.excludeFollowUp)
        setValueToCell(sheet, 4, 3, configuration.includeLockedVersion)
        setValueToCell(sheet, 5, 3, configuration.includeAllStudyDrugsCases)
        setValueToCell(sheet, 6, 3, configuration.excludeNonValidCases)
        setValueToCell(sheet, 7, 3, configuration.includeMedicallyConfirmedCases)
        setValueToCell(sheet, 8, 3, configuration.includeNonSignificantFollowUp)


        setValueToCell(sheet, 12, 1, configuration.reportName)
        setValueToCell(sheet, 12, 3, configuration.owner.fullName)
        setValueToCell(sheet, 13, 1, configuration.description)
        setValueToCell(sheet, 13, 3, (configuration.tags*.name).join(", "))
        setValueToCell(sheet, 14, 1, configuration.qualityChecked)

        setValueToCell(sheet, 17, 1, getSharedWith(configuration))
        setValueToCell(sheet, 18, 1, configuration.deliveryOption?.emailToUsers?.join(", "))
        setValueToCell(sheet, 19, 1, configuration.deliveryOption?.attachmentFormats?.collect { ViewHelper.getMessage(it.i18nKey) }?.join(", "))

        if (configuration.scheduleDateJSON) {
            def scheduler = JSON.parse(configuration.scheduleDateJSON)
            setValueToCell(sheet, 17, 3, scheduler.startDateTime)
            setValueToCell(sheet, 18, 3, scheduler.timeZone?.text)
            def s = []
            scheduler.recurrencePattern?.split(';')?.each {
                def set = it.split("=")
                if (set[0] == SCHEDULER_UNTIL_TOKEN)
                    s << ViewHelper.getMessage("app.excelExport.until") + Date.parse("yyyyMMdd", set[1]).format(DateUtil.ISO_DATE_FORMAT)
                else
                    s << ViewHelper.getMessage("scheduler." + set[0].toLowerCase(), null, set[0].toLowerCase()) + ": " + ViewHelper.getMessage("scheduler." + set[1].toLowerCase(), null, set[1].toLowerCase())
            }
            setValueToCell(sheet, 19, 3, s.join("; "))
        }
        int blockStart = 23
        int blockSize = 11
        configuration.templateQueries.eachWithIndex { templateQuery, int i ->
            if (i > 0) {
                for (int j = 0; j < blockSize; j++) {
                    copyRow(workbook, sheet, blockStart + j, sheet.getLastRowNum() + 1)
                }
            }
            setValueToCell(sheet, blockStart + blockSize * i, 0, i + 1)
            setValueToCell(sheet, blockStart + blockSize * i, 2, templateQuery.template.name)
            setValueToCell(sheet, blockStart + blockSize * i, 3, templateQuery.templateValueLists.collect { tvl ->
                tvl.template?.name + "( " + tvl.parameterValues.collect { it.key + " = " + it.value ?: '' }.join(", ") + ")"
            }.join("; "))
            setValueToCell(sheet, blockStart + blockSize * i + 1, 2, templateQuery.query?.name)
            setValueToCell(sheet, blockStart + blockSize * i + 1, 3, templateQuery.queryValueLists.collect { tvl ->
                tvl.query.name + "( " + tvl.parameterValues.collect {
                    if (it.hasProperty('reportField'))
                        ViewHelper.getMessage("app.reportField." + it.reportField.name) + " " + ViewHelper.getMessage(it.operator.getI18nKey()) + " " + it.value ?: ''
                    else
                        it.key + " = " + it.value ?: ''
                }.join(", ") + ")"
            }.join("; "))
            setValueToCell(sheet, blockStart + blockSize * i + 2, 2, ViewHelper.getMessage(templateQuery.queryLevel.i18nKey))
            setValueToCell(sheet, blockStart + blockSize * i + 3, 2, ViewHelper.getMessage(templateQuery.dateRangeInformationForTemplateQuery.dateRangeEnum.i18nKey))
            if (DateRangeEnum.getRelativeDateOperatorsWithX().contains(templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum)) {
                setValueToCell(sheet, blockStart + blockSize * i + 3, 3, ViewHelper.getMessage("app.excelExport.whereX") + " " + (templateQuery?.dateRangeInformationForTemplateQuery?.relativeDateRangeValue))
            }
            if (templateQuery.dateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.CUSTOM) {
                setValueToCell(sheet, blockStart + blockSize * i + 3, 3, templateQuery.dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute?.format(DateUtil.ISO_DATE_FORMAT) + " - " +
                        templateQuery.dateRangeInformationForTemplateQuery?.dateRangeEndAbsolute?.format(DateUtil.ISO_DATE_FORMAT))
            }
            setValueToCell(sheet, blockStart + blockSize * i + 4, 2, templateQuery.header)
            setValueToCell(sheet, blockStart + blockSize * i + 5, 2, templateQuery.title)
            setValueToCell(sheet, blockStart + blockSize * i + 6, 2, templateQuery.footer)
            setValueToCell(sheet, blockStart + blockSize * i + 7, 2, templateQuery.headerProductSelection)
            setValueToCell(sheet, blockStart + blockSize * i + 8, 2, templateQuery.headerDateRange)
            setValueToCell(sheet, blockStart + blockSize * i + 9, 2, templateQuery.displayMedDraVersionNumber)
            setValueToCell(sheet, blockStart + blockSize * i + 10, 2, ViewHelper.getMessage(templateQuery.granularity?.i18nKey))
        }
        exportReportJson(workbook,  configuration)
    }

    void exportQuery(Workbook workbook, SuperQuery query) {
        if (query) {
            Sheet sheet = workbook.getSheet(QUERY_TEMPLATE_SHEET);
            int index = 1
            if (sheet.getRow(1).getCell(0).getStringCellValue() != "") {
                index = sheet.getLastRowNum() + 1
                copyRow(workbook, sheet, 1, index)
            }

            setValueToCell(sheet, index, 0, query.name);
            setValueToCell(sheet, index, 1, query.description);
            setValueToCell(sheet, index, 2, query.owner.fullName);
            setValueToCell(sheet, index, 3, (query.tags*.name).join(", "));
            setValueToCell(sheet, index, 4, query.qualityChecked);
            setValueToCell(sheet, index, 5, ViewHelper.getMessage("app.excelExport.groups") + " " +  ((query.shareWithGroups*.name).join(", ") ?: " - ") + "; " + ViewHelper.getMessage("userGroup.users.label") + " " + ((query.shareWithUsers*.fullName).join(", ") ?: " - "));
            setValueToCell(sheet, index, 6, query.nonValidCases);
            setValueToCell(sheet, index, 7, query.icsrPadderAgencyCases);
            setValueToCell(sheet, index, 8, ViewHelper.getMessage(query.queryType.i18nKey));
            def value = ""
            if (query.instanceOf(Query)) {
                JSON.parse(query.JSONQuery)?.all?.containerGroups?.eachWithIndex {it, i->
                    if (i > 0) value += " " + it.keyword;
                    value += addQueryExpression(workbook, sheet, it)
                }
            }
            if (query.instanceOf(QuerySet)) {
                def queries = query.queries.collectEntries {
                    [(it.id): it]
                }
                JSON.parse(query.JSONQuery)?.all?.containerGroups?.eachWithIndex {it,i->
                    if (i > 0) value += " " + it.keyword;
                    value += addQuerySet(it, queries)
                }
            }
            if (query.instanceOf(CustomSQLQuery)) {
                value =  query.customSQLQuery
            }
            setValueToCell(sheet, index, 9, value);
            exportQueryJson(workbook, query)
        }
    }

    private void exportQueryJson(Workbook workbook, SuperQuery query) {
        Sheet sheet = workbook.getSheet(JSON_TEMPLATE_SHEET);
        int index = 1
        if (sheet.getRow(1).getCell(0).getStringCellValue() != "") {
            index = sheet.getLastRowNum() + 1
            copyRow(workbook, sheet, 1, index)
        }
        setValueToCell(sheet, index, 0, ViewHelper.getMessage("app.label.query"))
        setValueToCell(sheet, index, 1, ViewHelper.getMessage(query.queryType.i18nKey))
        setValueToCell(sheet, index, 2, query.name)
        setValueToCell(sheet, index, 3, query.description)
        setValueToCell(sheet, index, 4, queryService.getQueryAsJSON(query))
    }

    private void exportTemplateJson(Workbook workbook, ReportTemplate template) {
        Sheet sheet = workbook.getSheet(JSON_TEMPLATE_SHEET);
        int index = 1
        if (sheet.getRow(1).getCell(0).getStringCellValue() != "") {
            index = sheet.getLastRowNum() + 1
            copyRow(workbook, sheet, 1, index)
        }
        setValueToCell(sheet, index, 0, ViewHelper.getMessage("app.label.template"))
        setValueToCell(sheet, index, 1, ViewHelper.getMessage(template.templateType.i18nKey))
        setValueToCell(sheet, index, 2, template.name)
        setValueToCell(sheet, index, 3, template.description)
        setValueToCell(sheet, index, 4, templateService.getTemplateAsJSON(template))
    }

    private void exportReportJson(Workbook workbook, ReportConfiguration configuration) {
        Sheet sheet = workbook.getSheet(JSON_TEMPLATE_SHEET);
        int index = 1
        if (sheet.getRow(1).getCell(0).getStringCellValue() != "") {
            index = sheet.getLastRowNum() + 1
            copyRow(workbook, sheet, 1, index)
        }
        setValueToCell(sheet, index, 0, configuration.instanceOf(Configuration) ? ViewHelper.getMessage("app.configurationType.ADHOC_REPORT") : ViewHelper.getMessage("app.configurationType.PERIODIC_REPORT"))
        setValueToCell(sheet, index, 1, "")
        setValueToCell(sheet, index, 2, configuration.reportName)
        setValueToCell(sheet, index, 3, configuration.description)
        setValueToCell(sheet, index, 4, configurationService.getConfigurationAsJSON(configuration).toString())
    }

    private void exportTemplate(Workbook workbook, ReportTemplate template) {
        if (template?.instanceOf(CaseLineListingTemplate)) exportCaseLineListingTemplate(workbook,(CaseLineListingTemplate) template)
        else if (template?.instanceOf(DataTabulationTemplate)) exportDataTabulationTemplate(workbook,(DataTabulationTemplate) template)
        else if (template?.instanceOf(CustomSQLTemplate)) exportCustomSQLTemplate(workbook,(CustomSQLTemplate) template)
        else if (template?.instanceOf(NonCaseSQLTemplate)) exportNonCaseSQLTemplate(workbook,(NonCaseSQLTemplate) template)
        else if (template?.instanceOf(TemplateSet)) exportTemplateSet(workbook,(TemplateSet) template)
        else return
        exportTemplateJson(workbook, template)
    }

    private void exportTemplateSet(Workbook workbook,TemplateSet template) {
        counter++
        int templateIndex = workbook.getSheetIndex(TEMPLATESET_TEMPLATE_SHEET);

        Sheet sheet = workbook.cloneSheet(templateIndex);
        setSheetName(workbook, sheet, template.name)

        setValueToCell(sheet, 1, 1, template.name);
        setValueToCell(sheet, 1, 3, template.category?.name);
        setValueToCell(sheet, 2, 1, template.description);
        setValueToCell(sheet, 2, 3, (template.tags*.name).join(", "));
        setValueToCell(sheet, 3, 1, template.owner.fullName);
        setValueToCell(sheet, 3, 3, template.useFixedTemplate);
        setValueToCell(sheet, 4, 1, ViewHelper.getMessage("app.excelExport.groups") + " " + ((template.shareWithGroups*.name).join(", ") ?: " - ") + "; " + ViewHelper.getMessage("userGroup.users.label") + " " +((template.shareWithUsers*.fullName).join(", ") ?: " - "));
        setValueToCell(sheet, 4, 3, template.qualityChecked);
        setValueToCell(sheet, 5, 1, template.useFixedTemplate);
        setValueToCell(sheet, 5, 3, template.excludeEmptySections);
        setValueToCell(sheet, 5, 4, template.linkSectionsByGrouping);
        setValueToCell(sheet, 5, 5, template.sectionBreakByEachTemplate);
        template.nestedTemplates.eachWithIndex { ReportTemplate entry, int i ->
            if (i > 0) copyRow(workbook, sheet, 7, 7 + i)
            setValueToCell(sheet, 7 + i, 0, entry.name);
        }
    }

    private void exportNonCaseSQLTemplate(Workbook workbook,NonCaseSQLTemplate template) {
        counter++
        int templateIndex = workbook.getSheetIndex(NCTEMPLATE_TEMPLATE_SHEET);

        Sheet sheet = workbook.cloneSheet(templateIndex);
        setSheetName(workbook, sheet, template.name)

        setValueToCell(sheet, 1, 1, template.name);
        setValueToCell(sheet, 1, 3, template.category?.name);
        setValueToCell(sheet, 2, 1, template.description);
        setValueToCell(sheet, 2, 3, (template.tags*.name).join(", "));
        setValueToCell(sheet, 3, 1, template.owner.fullName);
        setValueToCell(sheet, 3, 3, template.useFixedTemplate);
        setValueToCell(sheet, 4, 1, ViewHelper.getMessage("app.excelExport.groups") + " " +  ((template.shareWithGroups*.name).join(", ") ?: " - ") + "; " + ViewHelper.getMessage("userGroup.users.label") + " " + ((template.shareWithUsers*.fullName).join(", ") ?: " - "));
        setValueToCell(sheet, 4, 3, template.qualityChecked);
        setValueToCell(sheet, 5, 1, template.useFixedTemplate);
        setValueToCell(sheet, 5, 3, template.templateFooter);
        setValueToCell(sheet, 6, 1, template.showChartSheet);
        setValueToCell(sheet, 6, 3, template.usePvrDB);
        setValueToCell(sheet, 7, 1, template.nonCaseSql);
        setValueToCell(sheet, 8, 1, template.chartCustomOptions);

    }

    private void exportCustomSQLTemplate(Workbook workbook,CustomSQLTemplate template) {
        counter++
        int templateIndex = workbook.getSheetIndex(SQLTEMPLATE_TEMPLATE_SHEET);

        Sheet sheet = workbook.cloneSheet(templateIndex);
        setSheetName(workbook, sheet, template.name)

        setValueToCell(sheet, 1, 1, template.name);
        setValueToCell(sheet, 1, 3, template.category?.name);
        setValueToCell(sheet, 2, 1, template.description);
        setValueToCell(sheet, 2, 3, (template.tags*.name).join(", "));
        setValueToCell(sheet, 3, 1, template.owner.fullName);
        setValueToCell(sheet, 3, 3, template.useFixedTemplate);
        setValueToCell(sheet, 4, 1, ViewHelper.getMessage("app.excelExport.groups") + " " + ((template.shareWithGroups*.name).join(", ") ?: " - ") + "; " + ViewHelper.getMessage("userGroup.users.label") + " " + ((template.shareWithUsers*.fullName).join(", ") ?: " - "));
        setValueToCell(sheet, 4, 3, template.qualityChecked);
        setValueToCell(sheet, 5, 1, template.useFixedTemplate);
        setValueToCell(sheet, 5, 3, template.templateFooter);
        setValueToCell(sheet, 6, 1, template.customSQLTemplateSelectFrom);
        setValueToCell(sheet, 7, 1, template.customSQLTemplateWhere);

    }

    private void exportDataTabulationTemplate(Workbook workbook,DataTabulationTemplate template) {
        counter++
        int templateIndex = workbook.getSheetIndex(DTTEMPLATE_TEMPLATE_SHEET);

        Sheet sheet = workbook.cloneSheet(templateIndex);
        setSheetName(workbook, sheet, template.name)

        setValueToCell(sheet, 1, 1, template.name);
        setValueToCell(sheet, 1, 3, template.category?.name);
        setValueToCell(sheet, 2, 1, template.description);
        setValueToCell(sheet, 2, 3, (template.tags*.name).join(", "));
        setValueToCell(sheet, 3, 1, template.owner.fullName);
        setValueToCell(sheet, 3, 3, template.useFixedTemplate);
        setValueToCell(sheet, 4, 1, ViewHelper.getMessage("app.excelExport.groups") + " " + ((template.shareWithGroups*.name).join(", ") ?: " - ") + "; " + ViewHelper.getMessage("userGroup.users.label") + " " +((template.shareWithUsers*.fullName).join(", ") ?: " - "));
        setValueToCell(sheet, 4, 3, template.qualityChecked);
        setValueToCell(sheet, 5, 1, template.useFixedTemplate);
        setValueToCell(sheet, 5, 3, template.showChartSheet);
        setValueToCell(sheet, 6, 1, template.supressHeaders);
        setValueToCell(sheet, 6, 3, template.supressRepeatingExcel);
        setValueToCell(sheet, 7, 1, template.templateFooter);
        setValueToCell(sheet, 7, 3, template.chartCustomOptions);
        setValueToCell(sheet, 8, 1, template.drillDownToCaseList);

        int rowBlockSize = 7
        int rowBlockStart = 10
        int colBlockSize = 7
        int colBlockStart = 20
        int measureBlockSize = 10
        int measureBlockStart = 29
        int propsBlockStart = measureBlockSize + measureBlockStart
        addFieldBlock(workbook, sheet, rowBlockSize, rowBlockStart, template.rowList?.reportFieldInfoList, ViewHelper.getMessage("app.excelExport.row"), BlockType.DT)
        template.columnMeasureList.each { DataTabulationColumnMeasure dtcm ->
            def columnList = dtcm.columnList?.reportFieldInfoList
            addFieldBlock(workbook, sheet, colBlockSize, colBlockStart, columnList,  ViewHelper.getMessage("app.excelExport.column"), BlockType.DTC)
            addMeasureBlock(workbook, sheet, measureBlockSize, measureBlockStart, dtcm.measures)
            int propsIndex = sheet.getLastRowNum() + 1
            copyRow(workbook, sheet, propsBlockStart, propsIndex)
            copyRow(workbook, sheet, propsBlockStart + 1, propsIndex + 1)
            setValueToCell(sheet, propsIndex, 3, dtcm.showTotalIntervalCases);
            setValueToCell(sheet, propsIndex + 1, 3, dtcm.showTotalCumulativeCases);
        }
        if (template.JSONQuery) {
            copyRow(workbook, sheet, rowBlockStart, sheet.getLastRowNum() + 2)
            setValueToCell(sheet, sheet.getLastRowNum(), 0,  ViewHelper.getMessage("app.label.caseLineListing.advanced.custom.expression"));
            String value=""
            JSON.parse(template.JSONQuery)?.all?.containerGroups?.eachWithIndex {it, i->
                if (i > 0) value += " " + it.keyword;
                value+=addQueryExpression(workbook, sheet, it)
            }
            setValueToCell(sheet, sheet.getLastRowNum()+1, 0, value)
        }
        for (int i = 0; i < 11; i++)
            sheet.removeMergedRegion(0);
        CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 3)
        sheet.addMergedRegion(cellRangeAddress);
        int blockSize = rowBlockSize + colBlockSize + measureBlockSize + 2 + 5
        for (int i = 0; i < blockSize+1; i++) {
            removeRow(sheet,rowBlockStart )
        }
    }

    private void exportCaseLineListingTemplate(Workbook workbook, CaseLineListingTemplate template) {
        counter++
        int templateIndex = workbook.getSheetIndex(CCLTEMPLATE_TEMPLATE_SHEET);

        Sheet sheet = workbook.cloneSheet(templateIndex);
        setSheetName(workbook, sheet, template.name)

        setValueToCell(sheet, 1, 1, template.name);
        setValueToCell(sheet, 1, 3, template.category?.name);
        setValueToCell(sheet, 2, 1, template.description);
        setValueToCell(sheet, 2, 3, (template.tags*.name).join(", "));
        setValueToCell(sheet, 3, 1, template.owner.fullName);
        setValueToCell(sheet, 3, 3, template.useFixedTemplate);
        setValueToCell(sheet, 4, 1, ViewHelper.getMessage("app.excelExport.groups") + " " + ((template.shareWithGroups*.name).join(", ") ?: " - ") + "; " + ViewHelper.getMessage("userGroup.users.label") + " " +  ((template.shareWithUsers*.fullName).join(", ") ?: " - "));
        setValueToCell(sheet, 4, 3, template.qualityChecked);
        setValueToCell(sheet, 5, 1, template.useFixedTemplate);
        setValueToCell(sheet, 5, 3, template.pageBreakByGroup);
        setValueToCell(sheet, 6, 1, template.columnShowTotal);
        setValueToCell(sheet, 6, 3, template.columnShowDistinct);
        setValueToCell(sheet, 7, 1, template.columnShowSubTotal);
        setValueToCell(sheet, 7, 3, template.interactiveOutput);
        setValueToCell(sheet, 8, 1, template.templateFooter);

        int fieldBlockSize = 14
        int fieldBlockStart = 10
        addFieldBlock(workbook, sheet, fieldBlockSize, fieldBlockStart, template.groupingList?.reportFieldInfoList,  ViewHelper.getMessage("app.label.grouping"))
        template.columnList?.reportFieldInfoList?.groupBy { it.setId }?.each { key, List<ReportFieldInfo> columnList ->
            addFieldBlock(workbook, sheet, fieldBlockSize, fieldBlockStart, columnList, ViewHelper.getMessage("app.excelExport.column"))
        }
        addFieldBlock(workbook, sheet, fieldBlockSize, fieldBlockStart, template.rowColumnList?.reportFieldInfoList, ViewHelper.getMessage("app.label.rowColumns"))
        if (template.JSONQuery) {
            copyRow(workbook, sheet, fieldBlockStart, sheet.getLastRowNum() + 2)
            setValueToCell(sheet, sheet.getLastRowNum(), 0,  ViewHelper.getMessage("app.label.caseLineListing.advanced.custom.expression"));
            String value=""
            JSON.parse(template.JSONQuery)?.all?.containerGroups?.eachWithIndex {it, i->
                if (i > 0) value += " " + it.keyword;
                value+=addQueryExpression(workbook, sheet, it)
            }
            setValueToCell(sheet, sheet.getLastRowNum()+1, 0, value);
        }

        sheet.removeMergedRegion(1);
        sheet.removeMergedRegion(1);
        sheet.removeMergedRegion(1);

        for (int i = 0; i < fieldBlockSize + 3; i++) {
            removeRow(sheet,fieldBlockStart )
        }
    }


    private static String addQueryExpression(Workbook workbook, Sheet sheet, expression) {
        StringBuilder result = new StringBuilder("(")
        expression.expressions?.eachWithIndex { it, i ->
            if (it && (it != "null")) {
                if (i > 0) result.append(" " + expression.keyword)
                if (it.expressions)
                    result.append(" " + addQueryExpression(workbook, sheet, it))
                else
                    result.append(" " + ViewHelper.getMessage('app.reportField.' + it.field) + " " + it.op + " " + it.value)
            }
        }
        result.append(" )")
        return result.toString();
    }

    static String getQuerySetStructure(QuerySet query) {
        String value = ""
        Map queries = query.queries.collectEntries {
            [(it.id): it]
        }
        JSON.parse(query.JSONQuery)?.all?.containerGroups?.eachWithIndex { it, i ->
            if (i > 0) value += " " + it.keyword;
            value += addQuerySet(it, queries)
        }
        value
    }

    static String addQuerySet(expression, Map queries) {
        StringBuilder result = new StringBuilder("(")
        expression.expressions?.eachWithIndex { it, i ->
            if (it && (it != "null")) {
                if (i > 0) result.append(" " + expression.keyword)
                if (it.expressions)
                    result.append(" " + addQuerySet(it, queries))
                else
                    result.append(" " + queries.get(it.query as Long).name)
            }
        }
        result.append(" )")
        return result.toString();
    }

    private static void addMeasureBlock(Workbook workbook, Sheet sheet, int fieldBlockSize, int fieldBlockStart, List<DataTabulationMeasure> measureList) {
        if (!measureList || measureList.size() == 0) return
        int columnBlockStart = sheet.getLastRowNum() + 1
        for (int i = 0; i < measureList.size(); i++) {
            for (int j = 0; j < fieldBlockSize; j++) {
                copyRow(workbook, sheet, fieldBlockStart + j, columnBlockStart + fieldBlockSize * i + j)
            }
        }
        measureList.eachWithIndex { DataTabulationMeasure entry, int i ->
            int startIndex = columnBlockStart + fieldBlockSize * i
            setValueToCell(sheet, startIndex, 1, ViewHelper.getMessage(entry.type.i18nKey));
            setValueToCell(sheet, startIndex, 3, entry.name);
            if ((entry.dateRangeCount.type() == CountTypeEnum.CUSTOM_PERIOD_COUNT.type()) && (entry.dateRangeCount != CountTypeEnum.CUSTOM_PERIOD_COUNT)) {
                setValueToCell(sheet, startIndex + 1, 3, ViewHelper.getMessage(entry.dateRangeCount.i18nKey) + " " + ViewHelper.getMessage("app.excelExport.whereX") + " = " + entry.relativeDateRangeValue);
            } else
                setValueToCell(sheet, startIndex + 1, 3, ViewHelper.getMessage(entry.dateRangeCount.i18nKey));
            setValueToCell(sheet, startIndex + 2, 3, entry.customPeriodFromWithTZ);
            setValueToCell(sheet, startIndex + 3, 3, entry.customPeriodToWithTZ);
            setValueToCell(sheet, startIndex + 4, 3, ViewHelper.getMessage(entry.percentageOption.i18nKey));
            setValueToCell(sheet, startIndex + 5, 3, entry.showTotal);
            setValueToCell(sheet, startIndex + 6, 3, entry.showTopX);
            setValueToCell(sheet, startIndex + 7, 3, entry.drillDownTemplate?.name);
            setValueToCell(sheet, startIndex + 8, 3, entry.valuesChartType);
            setValueToCell(sheet, startIndex + 9, 3, entry.percentageChartType);
        }
    }

    private static void addFieldBlock(Workbook workbook, Sheet sheet, int fieldBlockSize, int fieldBlockStart, List<ReportFieldInfo> columnList, String title, BlockType type = BlockType.CCL) {
        if (!columnList || columnList.size() == 0) return
        copyRow(workbook, sheet, fieldBlockStart, sheet.getLastRowNum() + 2)
        copyRow(workbook, sheet, fieldBlockStart + 1, sheet.getLastRowNum() + 1);
        int columnBlockStart = sheet.getLastRowNum() + 1
        setValueToCell(sheet, columnBlockStart - 2, 0, title);
        for (int i = 0; i < columnList.size(); i++) {
            for (int j = 0; j < fieldBlockSize; j++) {
                copyRow(workbook, sheet, fieldBlockStart + 2 + j, columnBlockStart + fieldBlockSize * i + j)
            }
        }
        int number = 0
        int stackId = -1
        columnList.eachWithIndex { ReportFieldInfo entry, int i ->
            if (entry.stackId > 0) {
                if (stackId != entry.stackId) {
                    number++
                }
            } else {
                number++
            }
            stackId = entry.stackId
            if (type == BlockType.CCL)
                fillTemlateFieldBlock(sheet, columnBlockStart + fieldBlockSize * i, entry, number)
            if (type == BlockType.DT)
                fillTemlateFieldBlockRowDT(sheet, columnBlockStart + fieldBlockSize * i, entry, number)
            if (type == BlockType.DTC)
                fillTemlateFieldBlockRowDT(sheet, columnBlockStart + fieldBlockSize * i, entry, -1)
        }
    }


    private static void fillTemlateFieldBlock(Sheet sheet, int startIndex, ReportFieldInfo entry, int number) {
        setValueToCell(sheet, startIndex, 0, number);
        setValueToCell(sheet, startIndex, 1, ViewHelper.getMessage('app.reportField.' + entry.reportField.name));
        String locale = ViewHelper.getMessage('app.reportField.' + entry.reportField.name).endsWith('(J)') ? Locale.JAPANESE : Locale.ENGLISH
        def existingRecordText = Localization.findByCodeAndLocale('app.reportField.' + entry.reportField.name, locale)
        if (locale.equals(Locale.ENGLISH)) {
            setValueToCell(sheet, startIndex, 3, entry.renameValue ?: ViewHelper.getMessage('app.reportField.' + entry.reportField.name));
        } else {
            setValueToCell(sheet, startIndex, 3, entry.renameValue ?: existingRecordText?.text);
        }
        setValueToCell(sheet, startIndex + 1, 3, entry.newLegendValue);
        setValueToCell(sheet, startIndex + 2, 3, entry.columnWidth?:ViewHelper.getMessage('app.excelExport.auto'));
        setValueToCell(sheet, startIndex + 3, 3, entry.commaSeparatedValue);
        setValueToCell(sheet, startIndex + 4, 3, entry.suppressRepeatingValues);
        setValueToCell(sheet, startIndex + 5, 3, entry.suppressLabel);
        setValueToCell(sheet, startIndex + 6, 3, entry.redactedValue);
        setValueToCell(sheet, startIndex + 7, 3, entry.blindedValue);
        setValueToCell(sheet, startIndex + 8, 3, entry.customExpression);
        setValueToCell(sheet, startIndex + 9, 3, entry.sort?.value());
        setValueToCell(sheet, startIndex + 10, 3, entry.sortLevel > -1 ? entry.sortLevel : "");
        setValueToCell(sheet, startIndex + 11, 3, entry.advancedSorting);
        setValueToCell(sheet, startIndex + 12, 3, entry.drillDownTemplate?.name);
        setValueToCell(sheet, startIndex + 13, 3, entry.drillDownFilerColumns);
    }

    private static void fillTemlateFieldBlockRowDT(Sheet sheet, int startIndex, ReportFieldInfo entry, int number) {
        if (number > -1)
            setValueToCell(sheet, startIndex, 0, number);
        setValueToCell(sheet, startIndex, 1, ViewHelper.getMessage('app.reportField.' + entry.reportField.name));
        String locale = ViewHelper.getMessage('app.reportField.' + entry.reportField.name).endsWith('(J)') ? Locale.JAPANESE : Locale.ENGLISH
        def existingRecordText = Localization.findByCodeAndLocale('app.reportField.' + entry.reportField.name, locale)
        if (locale.equals(Locale.ENGLISH)) {
            setValueToCell(sheet, startIndex, 3, entry.renameValue ?: ViewHelper.getMessage('app.reportField.' + entry.reportField.name));
        } else {
            setValueToCell(sheet, startIndex, 3, entry.renameValue ?: existingRecordText?.text);
        }
        setValueToCell(sheet, startIndex + 1, 3, entry.newLegendValue);
        setValueToCell(sheet, startIndex + 1, 3, entry.newLegendValue);
        setValueToCell(sheet, startIndex + 2, 3, entry.columnWidth?:ViewHelper.getMessage('app.excelExport.auto'));
        setValueToCell(sheet, startIndex + 3, 3, entry.redactedValue);
        setValueToCell(sheet, startIndex + 4, 3, entry.blindedValue);
        setValueToCell(sheet, startIndex + 5, 3, entry.customExpression);
        setValueToCell(sheet, startIndex + 6, 3, entry.sort?.value());
    }


    private static void setValueToCell(Sheet sheet, int rowIndex, int columnIndex, def value) {
        Row row = sheet.getRow(rowIndex);
        if (!row) row = sheet.createRow(rowIndex)
        Cell cell = row.getCell(columnIndex)
        if (!cell) cell = row.createCell(columnIndex)
        String val = (value == null ? "" : MiscUtil.matchCSVPattern(value as String))
        if (val.length() > 30000) val = val.substring(0, 30000)
        if (value instanceof Boolean) val = value ? ViewHelper.getMessage("app.label.yes") : ViewHelper.getMessage("app.label.no")
        cell.setCellValue(val);
    }

    private static void copyRow(Workbook workbook, Sheet worksheet, int sourceRowNum, int destinationRowNum) {
        Row newRow = worksheet.getRow(destinationRowNum);
        Row sourceRow = worksheet.getRow(sourceRowNum);
        if (newRow != null) {
            worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1);
        } else {
            newRow = worksheet.createRow(destinationRowNum);
        }
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            Cell oldCell = sourceRow.getCell(i);
            Cell newCell = newRow.createCell(i);
            if (oldCell == null) {
                newCell = null;
                continue;
            }
            newCell.setCellStyle(oldCell.getCellStyle());
            if (oldCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }
            newCell.setCellType(oldCell.getCellType());
            switch (oldCell.getCellType()) {
                case CellType.BLANK:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;
                case CellType.BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;
                case CellType.ERROR:
                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
                    break;
                case CellType.FORMULA:
                    newCell.setCellFormula(oldCell.getCellFormula());
                    break;
                case CellType.NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());
                    break;
                case CellType.STRING:
                    newCell.setCellValue(oldCell.getRichStringCellValue());
                    break;
            }
        }
        for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
            if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
                CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(),
                        (newRow.getRowNum() +
                                (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow()
                                )),
                        cellRangeAddress.getFirstColumn(),
                        cellRangeAddress.getLastColumn());
                worksheet.addMergedRegion(newCellRangeAddress);
            }
        }
    }

    private static void setSheetName(Workbook wb, Sheet sheet, String name) {

        String shotName = name.substring(0, Math.min(name.length(), 28))
        shotName = WorkbookUtil.createSafeSheetName(shotName)
        String newName = shotName
        int i = 1
        while (wb.getSheetIndex(newName) > -1) {
            newName = shotName + i;
            i++
        }
        //fix for PVR-6815, setSheetNameHack injected in BootStrap.groovy to solve POI library version conflict
        wb.setSheetNameHack(wb.getSheetIndex(sheet), newName);
    }

    private static void removeRow(Sheet sheet, int rowIndex) {
        int lastRowNum = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRowNum) {
            sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
        }
        if (rowIndex == lastRowNum) {
            Row removingRow = sheet.getRow(rowIndex);
            if (removingRow != null) {
                sheet.removeRow(removingRow);
            }
        }
    }
}
