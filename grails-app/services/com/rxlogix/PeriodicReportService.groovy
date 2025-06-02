package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.validation.FieldError
import com.rxlogix.util.MiscUtil

class PeriodicReportService {

    def configurationService
    def userService
    def customMessageService
    def CRUDService
    def productDictionaryService

    Map targetStatesAndApplications(Long executedReportConfiguration, String initialState) {
        List states = []
        Map actions = [:]
        Map rules = [:]
        Map needApproval = [:]
        WorkflowState initialStateObj = WorkflowState.findByNameAndIsDeleted(initialState, false)
        ExecutedReportConfiguration executedReport=ExecutedReportConfiguration.findById(executedReportConfiguration)
        WorkflowConfigurationTypeEnum configurationTypeEnum

        if (executedReport instanceof ExecutedPeriodicReportConfiguration)
            configurationTypeEnum = WorkflowConfigurationTypeEnum.PERIODIC_REPORT
        else if (executedReport instanceof ExecutedIcsrReportConfiguration)
            configurationTypeEnum = WorkflowConfigurationTypeEnum.ICSR_REPORT
        else configurationTypeEnum = WorkflowConfigurationTypeEnum.ADHOC_REPORT

        List<WorkflowRule> workflowRules = WorkflowRule.getAllByConfigurationTypeAndInitialState(configurationTypeEnum,initialStateObj).list()
        if(workflowRules){
            workflowRules?.each {
                actions.put(it.targetState?.name, it.targetState?.reportActionsAsList)
                states.add(it.targetState)
                rules.put(it.id, it.targetState)
                needApproval.put(it.id, it.needApproval)
            }
        }
        [actions: actions, states: states, rules: rules, needApproval: needApproval]
    }

    @Transactional
    void scheduleToRunOnce(PeriodicReportConfiguration periodicReportConfiguration) {
        periodicReportConfiguration.setIsEnabled(true)
        if(periodicReportConfiguration.isPriorityReport){
            periodicReportConfiguration.nextRunDate = null
            if (periodicReportConfiguration.scheduleDateJSON && periodicReportConfiguration.isEnabled) {
                if (MiscUtil.validateScheduleDateJSON(periodicReportConfiguration.scheduleDateJSON)) {
                    periodicReportConfiguration.nextRunDate = configurationService.getNextDate(periodicReportConfiguration)
                    return
                }
            }
            periodicReportConfiguration.nextRunDate = null
        }else {
            periodicReportConfiguration.setScheduleDateJSON(getRunOnceScheduledDateJson())
            //Its required to set otherwise configurationService.getNextDate gives null in case nextRunDate present in the object for unscheduled reports.
            periodicReportConfiguration.setNextRunDate(null)
            periodicReportConfiguration.setNextRunDate(configurationService.getNextDate(periodicReportConfiguration))
        }
        if (periodicReportConfiguration.id) {
            configurationService.CRUDService.update(periodicReportConfiguration)
        } else {
            configurationService.CRUDService.save(periodicReportConfiguration)
        }
    }

    private String getRunOnceScheduledDateJson() {
        User user = userService.getUser()
        def startupTime = (RelativeDateConverter.getFormattedCurrentDateTimeForTimeZone(user, DateUtil.JSON_DATE))
        def timeZone = DateUtil.getTimezoneForRunOnce(user)
        return """{"startDateTime":"${
            startupTime
        }","timeZone":{"${timeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""

    }

    def getExecutedPeriodicReport(Long id) {
        ExecutedPeriodicReportConfiguration.get(id)
    }

    String parseScheduler(String s, locale) {
        if (!s) return ""
        def json = JSON.parse(s)
        if (!json || !json.startDateTime) return ""
        Date startDate = Date.parse("yyyy-MM-dd'T'HH:mmXXX", json.startDateTime)
        ViewHelper.getMessage("scheduler.startDate") + ":" + DateUtil.getLongDateStringForLocaleAndTimeZone(startDate, locale, json.timeZone.name, true) + "; " +
                (json?.recurrencePattern?.startsWith("FREQ=DAILY;INTERVAL=1;COUNT=1")?ViewHelper.getMessage("app.frequency.RUN_ONCE"):
                json?.recurrencePattern?.split(';')?.collect {
                    def set = it.split("=")
                    ViewHelper.getMessage("scheduler." + set[0].toLowerCase(),new Object[0], set[0].toLowerCase()) + ": " + ViewHelper.getMessage("scheduler." + set[1].toLowerCase(), new Object[0], set[1].toLowerCase())
                }?.join(";"))
    }

    Map toBulkTableMap(PeriodicReportConfiguration conf){
        [id                         : conf.id,
         reportName                 : conf.reportName,
         isTemplate                 : conf.isTemplate,
         productSelection           : ViewHelper.getDictionaryValues(conf, DictionaryTypeEnum.PRODUCT),
         productsJson               : conf.productSelection as String,
         groupsJson                 : conf.validProductGroupSelection as String,
         periodicReportTypeLabel    : ViewHelper.getMessage(conf.periodicReportType.i18nKey),
         periodicReportType         : conf.periodicReportType.name(),
         dateRangeTypeLabel         : ViewHelper.getMessage(conf.globalDateRangeInformation.dateRangeEnum.i18nKey),
         dateRangeType              : conf.globalDateRangeInformation.dateRangeEnum.name(),
         relativeDateRangeValue     : conf.globalDateRangeInformation.relativeDateRangeValue,
         dateRangeStartAbsolute     : conf.globalDateRangeInformation.dateRangeStartAbsolute?.format(DateUtil.DATEPICKER_UTC_FORMAT),
         dateRangeEndAbsolute       : conf.globalDateRangeInformation.dateRangeEndAbsolute?.format(DateUtil.DATEPICKER_UTC_FORMAT),
         primaryReportingDestination: conf.primaryReportingDestination,
         nextRunDate                : DateUtil.getLongDateStringForTimeZone(conf.nextRunDate, userService.currentUser?.preference?.timeZone),
         status                     : (conf.isEnabled && conf.nextRunDate),
         scheduleDateJSON           : parseScheduler(conf.scheduleDateJSON, userService.currentUser?.preference?.locale),
         schedulerJSON              : conf.scheduleDateJSON,
         dueInDays                  : conf.dueInDays?:"",
         configurationTemplate      : conf.configurationTemplate?.reportName?:""
        ]
    }

    Map importFromExcel(workbook){
        def errors = []
        def added = []
        def updated = []
        boolean isBulkImport=true
        User currentUser = userService.getCurrentUser()
        Sheet sheet = workbook?.getSheetAt(0);
        Row row;
        if (sheet) {
            int lastCell = sheet.getRow(2).getLastCellNum() - 1
            for (int i = 3; i <= sheet?.getLastRowNum(); i++) {
                if ((row = sheet.getRow(i)) != null) {
                    Boolean empty = true
                    [0..13].each { empty = empty & !getExcelCell(row, 0) }
                    if (empty) continue;
                    boolean update = true
                    String reportName = getExcelCell(row, 0)
                    String tenant = getExcelCell(row, lastCell - 1)
                    String username = getExcelCell(row, lastCell)
                    if (!reportName) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.reportName", i + 1)
                        continue;
                    }
                    if (!tenant) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.tenant.does.not.exist", i+1)
                        continue;
                    }
                    if (!username) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.userName.does.not.exist", i + 1)
                        continue;
                    }
                    User owner = User.findByUsernameIlike(username)
                    if (!owner) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.userName.invalid", i+1, username)
                        continue;
                    }
                    Integer tenantId = Integer.parseInt(tenant)
                    PeriodicReportConfiguration configuration = PeriodicReportConfiguration.findByReportNameAndIsDeletedAndTenantIdAndOwner(reportName, false, tenantId, owner)
                    if (!configuration) {
                        String templateName = getExcelCell(row, 1)
                        if (templateName) {
                            PeriodicReportConfiguration template = PeriodicReportConfiguration.findByReportNameAndIsDeletedAndIsTemplate(templateName, false, true)
                            if (!template) {
                                errors << ViewHelper.getMessage("app.bulkUpdate.error.template", i + 1, templateName)
                                continue;
                            }
                            configuration = configurationService.copyConfig(template, owner, "", tenantId, false, isBulkImport)
                            update = false
                        } else {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.template.empty", i + 1)
                            continue;
                        }
                    }
                    try {
                        int columNumber = 1
                        def product = [:]
                        String lang = currentUser.preference.locale
                        columNumber = configurationService.parseProducts(row, product, columNumber, lang)
                        configuration.reportName = reportName
                        configuration.productSelection = new JsonBuilder(product).toString()
                        if (Holders.config.getProperty('pv.dictionary.group.enabled', Boolean)) {
                            configurationService.parseProductGroup(row, ++columNumber, configuration, currentUser)
                        }
                        try {
                            configuration.periodicReportType = getExcelCell(row, ++columNumber) as PeriodicReportTypeEnum
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.reportType.wrong", i + 1)
                            continue;
                        }
                        try {
                            configuration.globalDateRangeInformation.dateRangeEnum = getExcelCell(row, ++columNumber) as DateRangeEnum
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.scheduler.empty", i + 1)
                            continue;
                        }
                        configuration.globalDateRangeInformation.relativeDateRangeValue = getExcelCell(row, ++columNumber) ? getExcelCell(row, columNumber) as Integer : 1
                        configuration.globalDateRangeInformation.dateRangeStartAbsolute = DateUtil.parseDate(getExcelCell(row, ++columNumber), DateUtil.ISO_DATE_TIME_FORMAT)
                        configuration.globalDateRangeInformation.dateRangeEndAbsolute = DateUtil.parseDate(getExcelCell(row, ++columNumber), DateUtil.ISO_DATE_TIME_FORMAT)
                        if (configuration.globalDateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM && (!configuration.globalDateRangeInformation.dateRangeStartAbsolute || !configuration.globalDateRangeInformation.dateRangeEndAbsolute)) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.date.wrong", i + 1)
                            continue;
                        }
                        configuration.primaryReportingDestination = getExcelCell(row, ++columNumber)
                        configuration.dueInDays = getExcelCell(row, ++columNumber) ? getExcelCell(row, columNumber) as Integer : null
                        if (!getExcelCell(row, ++columNumber)) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.scheduler.empty", i + 1)
                            continue;
                        }
                        JSON.parse(getExcelCell(row, columNumber)) //just to check json
                        configuration.scheduleDateJSON = getExcelCell(row, columNumber)
                        CRUDService.save(configuration)
                        if (update) updated << reportName + " (" + owner.username + ")"
                        else
                            added << reportName + " (" + owner.username + ")"
                    } catch (ValidationException v) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.row", i + 1) + " " + v.errors.allErrors.collect { error ->
                            String errSting= error.toString()
                            if(error instanceof FieldError) errSting = ViewHelper.getMessage("app.label.field.invalid.value", error.field)
                            errSting
                        }.join(";")
                    } catch (Exception e) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.row", i + 1) + " " + e.getMessage()
                    }
                }
            }
        } else {
            errors << ViewHelper.getMessage('app.label.no.data.excel.error')
        }
        [errors:errors,added:added,updated :updated ]
    }

    private String getExcelCell(Row row, int i) {
        Cell cell = row?.getCell(i)
        cell?.setCellType(CellType.STRING);
        return cell?.getStringCellValue()?.trim()
    }

    public String getDisplayMessage(String code, List reportNames){
        ViewHelper.getMessage(code, reportNames.size()) + (reportNames ? ' (' + reportNames.join(",") + ')' : '')
    }
}
