package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.CaseStateUpdateDTO
import com.rxlogix.dto.IcsrCaseEmailPlaceholderDTO
import java.text.SimpleDateFormat;
import java.util.Date;
import com.rxlogix.dynamicReports.reportTypes.XMLReportOutputBuilder
import com.rxlogix.e2b.IcsrDriveService
import com.rxlogix.enums.*
import com.rxlogix.jasperserver.exception.ResourceNotFoundException
import com.rxlogix.mapping.LmIngredient
import com.rxlogix.mapping.LmLicense
import com.rxlogix.mapping.LmProduct
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.xml.XmlUtil
import groovy.sql.GroovyRowResult
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.utils.IOUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.validation.FieldError

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Clob
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import com.rxlogix.util.MiscUtil

class IcsrReportService {

    def configurationService
    def userService
    def customMessageService
    def CRUDService
    def dynamicReportService
    IcsrDriveService icsrDriveService
    def reportExecutorService
    def reportService
    def emailService
    GrailsApplication grailsApplication
    def notificationService
    def icsrProfileAckService
    def icsrXmlService
    def gatewayIntegrationService
    def axwayService
    def executedConfigurationService
    def utilService
    def e2BAttachmentService

    private static final String ARCHIVE_FOLDER = "archive"

    Map targetStatesAndApplications(Long executedReportConfiguration, String initialState) {
        List states = []
        Map actions = [:]
        Map rules = [:]
        Map needApproval = [:]
        WorkflowState initialStateObj = WorkflowState.findByNameAndIsDeleted(initialState, false)
        ExecutedReportConfiguration executedReport = ExecutedReportConfiguration.findById(executedReportConfiguration)
        WorkflowConfigurationTypeEnum configurationTypeEnum

        if (executedReport instanceof ExecutedIcsrReportConfiguration)
            configurationTypeEnum = WorkflowConfigurationTypeEnum.PERIODIC_REPORT
        else configurationTypeEnum = WorkflowConfigurationTypeEnum.ADHOC_REPORT

        List<WorkflowRule> workflowRules = WorkflowRule.findAllByConfigurationTypeEnumAndInitialStateAndIsDeleted(configurationTypeEnum, initialStateObj, false)
        if (workflowRules) {
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
    void scheduleToRunOnce(IcsrReportConfiguration periodicReportConfiguration) {
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
        ExecutedIcsrReportConfiguration.get(id)
    }

    String parseScheduler(String s, locale) {
        if (!s) return ""
        def json = JSON.parse(s)
        if (!json || !json.startDateTime) return ""
        Date startDate = Date.parse("yyyy-MM-dd'T'HH:mmXXX", json.startDateTime)
        ViewHelper.getMessage("scheduler.startDate") + ":" + DateUtil.getLongDateStringForLocaleAndTimeZone(startDate, locale, json.timeZone.name, true) + "; " +
                json?.recurrencePattern?.split(';')?.collect {
                    def set = it.split("=")
                    ViewHelper.getMessage("scheduler." + set[0].toLowerCase(), new Object[0], set[0].toLowerCase()) + ": " + ViewHelper.getMessage("scheduler." + set[1].toLowerCase(), new Object[0], set[1].toLowerCase())
                }?.join(";")
    }

    Map toBulkTableMap(IcsrReportConfiguration conf) {
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
         dueInDays                  : conf.dueInDays ?: "",
         configurationTemplate      : conf.configurationTemplate?.reportName ?: ""
        ]
    }

    Map importFromExcel(workbook) {
        def errors = []
        def added = []
        def updated = []
        User currentUser = userService.getCurrentUser()
        Sheet sheet = workbook?.getSheetAt(0);
        Row row;
        if (sheet)
            for (int i = 3; i <= sheet?.getLastRowNum(); i++) {
                if ((row = sheet.getRow(i)) != null) {
                    Boolean empty = true
                    [0..13].each { empty = empty & !getExcelCell(row, 0) }
                    if (empty) continue;
                    boolean update = true
                    String reportName = getExcelCell(row, 0)
                    if (!reportName) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.reportName", i + 1)
                        continue;
                    }
                    IcsrReportConfiguration configuration = IcsrReportConfiguration.findByReportNameAndIsDeleted(reportName, false)
                    if (!configuration) {
                        String templateName = getExcelCell(row, 1)
                        if (templateName) {
                            IcsrReportConfiguration template = IcsrReportConfiguration.findByReportNameAndIsDeletedAndIsTemplate(templateName, false, true)
                            if (!template) {
                                errors << ViewHelper.getMessage("app.bulkUpdate.error.template", i + 1, templateName)
                                continue;
                            }
                            configuration = configurationService.copyConfig(template, currentUser, "")
                            update = false
                        } else {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.template.empty", i + 1)
                            continue;
                        }
                    }
                    try {
                        configuration.reportName = reportName
                        def product = ["1": [], "2": [], "3": [], "4": []]
                        String lang = currentUser.preference.locale
                        if (getExcelCell(row, 2)) {
                            List ingredientNames = getExcelCell(row, 2).split(",")*.trim()
                            product["1"] = LmIngredient.createCriteria().list() {
                                'in'('ingredient', ingredientNames)
                                projections {
                                    distinct('ingredientId')
                                    property('ingredient')
                                }
                            }.collect { [name: it[1], id: it[0]] }
                        }
                        if (getExcelCell(row, 3)) {
                            List familyNames = getExcelCell(row, 3).split(",")*.trim()
                            product["2"] = LmProductFamily.createCriteria().list() {
                                'in'('name', familyNames)
                                projections {
                                    distinct('productFamilyId')
                                    property('name')
                                }
                            }.collect { [name: it[1], id: it[0]] }
                        }
                        if (getExcelCell(row, 4)) {
                            List productNames = getExcelCell(row, 4).split(",")*.trim()
                            product["3"] = LmProduct.createCriteria().list() {
                                'in'('name', productNames)
                                projections {
                                    distinct('productId')
                                    property('name')
                                }
                            }.collect { [name: it[1], id: it[0]] }
                        }
                        if (getExcelCell(row, 5)) {
                            List tradeNames = getExcelCell(row, 5).split(",")*.trim()
                            product["4"] = LmLicense.createCriteria().list() {
                                'in'('tradeName', tradeNames)
                                projections {
                                    distinct('licenseId')
                                    property('tradeName')
                                }
                            }.collect { [name: it[1], id: it[0]] }
                        }
                        if (product["1"].size() == 0 && product["2"].size() == 0 && product["3"].size() == 0 && product["4"].size() == 0) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.product", i + 1)
                            continue;
                        }
                        configuration.productSelection = product as JSON
                        try {
                            configuration.periodicReportType = getExcelCell(row, 6) as PeriodicReportTypeEnum
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.reportType.wrong", i + 1)
                            continue;
                        }
                        try {
                            configuration.globalDateRangeInformation.dateRangeEnum = getExcelCell(row, 7) as DateRangeEnum
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.scheduler.empty", i + 1)
                            continue;
                        }
                        configuration.globalDateRangeInformation.relativeDateRangeValue = getExcelCell(row, 8) ? getExcelCell(row, 8) as Integer : 1
                        configuration.globalDateRangeInformation.dateRangeStartAbsolute = DateUtil.parseDate(getExcelCell(row, 9), DateUtil.ISO_DATE_TIME_FORMAT)
                        configuration.globalDateRangeInformation.dateRangeEndAbsolute = DateUtil.parseDate(getExcelCell(row, 10), DateUtil.ISO_DATE_TIME_FORMAT)
                        if (configuration.globalDateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM && (!configuration.globalDateRangeInformation.dateRangeStartAbsolute || !configuration.globalDateRangeInformation.dateRangeEndAbsolute)) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.date.wrong", i + 1)
                            continue;
                        }
                        configuration.primaryReportingDestination = getExcelCell(row, 11)
                        configuration.dueInDays = getExcelCell(row, 12) ? getExcelCell(row, 12) as Integer : null
                        if (!getExcelCell(row, 13)) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.scheduler.empty", i + 1)
                            continue;
                        }
                        JSON.parse(getExcelCell(row, 13)) //just to check json
                        configuration.scheduleDateJSON = getExcelCell(row, 13)
                        CRUDService.save(configuration)
                        if (update) updated << reportName
                        else
                            added << reportName
                    } catch (ValidationException v) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.row", i + 1) + " " + v.errors.allErrors.collect { error ->
                            String errSting = error.toString()
                            if (error instanceof FieldError) errSting = ViewHelper.getMessage("app.label.field.invalid.value", error.field)
                            errSting
                        }.join(";")
                    } catch (Exception e) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.row", i + 1) + " " + e.getMessage()
                    }
                }
            }
        else {
            errors << ViewHelper.getMessage('app.label.no.data.excel.error')
        }
        [errors: errors, added: added, updated: updated]
    }

    private String getExcelCell(Row row, int i) {
        Cell cell = row?.getCell(i)
        cell?.setCellType(CellType.STRING);
        return cell?.getStringCellValue()?.trim()
    }

    public String getDisplayMessage(String code, List reportNames) {
        ViewHelper.getMessage(code, reportNames.size()) + (reportNames ? ' (' + reportNames.join(",") + ')' : '')
    }

    List<String> getCaseNumbers(ReportResult reportResult) {
        List<Tuple2<String, Integer>> caseAndVersionNumbers = reportService.getCaseNumberAndVersions(reportResult)
        if (caseAndVersionNumbers) {
            return caseAndVersionNumbers.collect { it.first }
        }
        return null
    }


    Map<String, File> generateAllXmlFile(ReportResult reportResult) {
        Map<String, File> result = [:]
        getCaseNumbers(reportResult)?.findAll { it }?.each {
            result.put(it, dynamicReportService.createR3XMLReport(reportResult, false, [caseNumber: it, outputFormat: ReportFormatEnum.XML.name(), reportLocale: 'en']))
        }
        return result
    }

    Map<String, File> generateAllCioms(ReportResult reportResult, User user) {
        Map<String, File> result = [:]
        getCaseNumbers(reportResult)?.findAll { it }?.collectEntries {
            result.put(it, dynamicReportService.createCIOMSReport(it, 1, user))
        }
        return result
    }

    void transmitAllCases(ExecutedTemplateQuery executedTemplateQuery, ExecutedIcsrProfileConfiguration executedConfiguration) {
        ReportResult reportResult = executedTemplateQuery.reportResult
        try {
            Map caseNoAndErrorMap = [:]
            reportService.getCaseNumberAndVersions(reportResult)?.findAll { it.first() }?.each {
                String caseNumber = it.first()
                Integer versionNumber = (it.last() ?: 0).toString().toInteger()
                if (!versionNumber) {
                    versionNumber = getVersionNumFor(executedTemplateQuery.id, caseNumber)
                }
                String errorData = null
                try {
                    transmitCase(executedTemplateQuery.id, caseNumber, versionNumber, ViewHelper.getMessage("app.auto.transmit.comment"))
                } catch (Exception ex) {
                    log.error("Error while transmitting case for ${executedTemplateQuery.id} - ${caseNumber} - ${versionNumber} ", ex)
                    errorData = ex.message ?: "Fatal unknown error while generating xml"
                }

                if (errorData) {
                    caseNoAndErrorMap.put(caseNumber, errorData)
                }
            }

            if (caseNoAndErrorMap && caseNoAndErrorMap.size() > 0) {
                sendIcsrProfileFailureEmailTo(executedConfiguration, caseNoAndErrorMap)
                StringBuffer messageArgs = new StringBuffer(" " + executedConfiguration.reportName + ':')
                for (Map.Entry<String, String> entry : caseNoAndErrorMap.entrySet()) {
                    messageArgs.append(entry.getKey() + ",")
                }
                messageArgs.substring(0, messageArgs.length() - 1)
                if (messageArgs.length() > 60) {
                    messageArgs = new StringBuffer(messageArgs.substring(0, 60))
                    messageArgs.append("...")
                }
                notificationService.addNotification(executedConfiguration?.owner, 'app.notification.failed.case.number', messageArgs.toString(), executedConfiguration.id, NotificationLevelEnum.INFO, NotificationApp.COMMENTS)
            }
        } catch (Exception ex) {
            log.error("Error while sending errors for ${executedConfiguration.reportName} - ${executedConfiguration.numOfExecutions} ${executedTemplateQuery.id}", ex)
        }
    }

    void transmitFile(ExecutedIcsrTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, File file, String remotePath, Date fileDate = null, String comments = null, String e2BStatus = null, String username = null) {
        if (!caseNumber || !remotePath || !file || !file.exists()) {
            log.debug("NO Valid file to transmit")
            throw new Exception("NO Valid file to transmit")
        }
        if (executedTemplateQuery.distributionChannelName == DistributionChannelEnum.PV_GATEWAY && grailsApplication.config.getProperty('pvgateway.integrated', Boolean)){
            ExecutedReportConfiguration executedConfiguration = executedTemplateQuery.executedConfiguration
            if(executedConfiguration instanceof ExecutedIcsrProfileConfiguration) {
                String receiverId = executedConfiguration.receiverId
                XMLResultData xmlResultData = XMLResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(executedTemplateQuery.id, caseNumber, versionNumber)
                if(xmlResultData && xmlResultData.isAttachmentExist) {
                    receiverId = executedConfiguration.unitAttachmentRegId
                }
                gatewayIntegrationService.transmitFile(executedConfiguration.senderId, receiverId, caseNumber, versionNumber, file)
                if (executedConfiguration.e2bDistributionChannel && executedConfiguration.e2bDistributionChannel.outgoingFolder) {
                    String destinationFolder = executedConfiguration.e2bDistributionChannel.outgoingFolder + File.separator + ARCHIVE_FOLDER
                    Path destinationPath = Paths.get(destinationFolder)
                    if (!Files.exists(destinationPath)) {
                        log.warn("Creating folder as folder doesn't exist for ${destinationFolder}")
                        destinationPath.toFile().mkdir()
                    }
                    Path dir = Paths.get(destinationFolder, file.name)
                    if (Files.exists(dir)) {
                        icsrProfileAckService.moveExistingFile(dir)
                    }
                    icsrDriveService.upload(destinationFolder, file)
                }
            }
        } else {
            Path destinationPath = Paths.get(remotePath)
            if (!Files.exists(destinationPath)) {
                log.warn("Creating folder as folder doesn't exist for ${remotePath}")
                destinationPath.toFile().mkdir()
            }
            icsrDriveService.upload(remotePath, file)
        }
        if(e2BStatus && e2BStatus.equals(IcsrCaseStateEnum.COMMIT_RECEIVED.toString())) {
            reportExecutorService.markCaseTransmittingAttachment(executedTemplateQuery, caseNumber, versionNumber, fileDate, comments)
        }else {
            reportExecutorService.markCaseTransmitting(executedTemplateQuery, caseNumber, versionNumber, fileDate, comments, username)
        }
    }

    void transmitCase(Long icsTemplateQueryId, String caseNumber, Integer versionNumber, String comments = null, String approvedBy = null, String approvedOn = null, String transmissionComments = null, String username = null) {
        ExecutedIcsrTemplateQuery executedTemplateQuery = ExecutedIcsrTemplateQuery.read(icsTemplateQueryId)
        if (!executedTemplateQuery) {
            throw new Exception("No entry found against Id: ${icsTemplateQueryId}")
        }
        IcsrCaseTracking icsrCaseTrackingInstance = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(icsTemplateQueryId, caseNumber, versionNumber)
        }
        Map oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTrackingInstance)
        if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
            oldValues.put("approvedBy", null)
            oldValues.put("approvedOn", null)
        }
        oldValues.put("transmissionComments",null)
        ExecutedReportConfiguration executedReportConfiguration = executedTemplateQuery.usedConfiguration
        ReportTemplate reportTemplate = executedTemplateQuery?.usedTemplate
        SuperQuery query = executedTemplateQuery?.usedQuery
        if (executedReportConfiguration instanceof ExecutedIcsrProfileConfiguration) {
            log.debug("Transmitting CaseNumber for ${icsTemplateQueryId},  ${caseNumber}")
            File file = null
            IcsrCaseTracking newicsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                newicsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(icsTemplateQueryId, caseNumber, versionNumber)
            }
            Long processedReportId = newicsrCaseTrackingInstance?.processedReportId
            Date fileDate = new Date()
            Boolean isJapanProfile = newicsrCaseTrackingInstance?.isJapanProfile()
            String currentState = newicsrCaseTrackingInstance?.e2BStatus
            if (reportTemplate?.instanceOf(ExecutedXMLTemplate)) {
                // R3 file need to be generated always.
                file = dynamicReportService.createR3XMLReport(executedTemplateQuery, false, [caseNumber: caseNumber, versionNumber: versionNumber, exIcsrTemplateQueryId:icsTemplateQueryId], fileDate, isJapanProfile)
                String xsltName = executedReportConfiguration.xsltName
                String xmlVersion = executedReportConfiguration.xmlVersion
                String xmlEncoding = executedReportConfiguration.xmlEncoding
                String xmlDoctype = executedReportConfiguration.xmlDoctype
                boolean generatePmdaPaperReport = false
                file.text = icsrXmlService.replaceTransmissionPlaceHoldersInXmlNodes(file.text, xsltName, xmlVersion, xmlEncoding, xmlDoctype)
                if (executedTemplateQuery.distributionChannelName == DistributionChannelEnum.EMAIL && executedReportConfiguration?.isJapanProfile) generatePmdaPaperReport = true
                if (executedReportConfiguration.e2bDistributionChannel?.reportFormat in [E2BReportFormatEnum.PDF, E2BReportFormatEnum.EB_PDF]) {
                    file = dynamicReportService.createPDFReport(executedTemplateQuery, false, [caseNumber: caseNumber, versionNumber: versionNumber, exIcsrTemplateQueryId:icsTemplateQueryId, generatePmdaPaperReport: generatePmdaPaperReport], fileDate, isJapanProfile)
                } else {
                    String errorData = icsrXmlService.validateXml(file, getXSDPathForName(executedReportConfiguration.xsltName))
                    if (errorData) {
                        CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedTemplateQuery, caseNumber: caseNumber, versionNumber: versionNumber, processedReportId: processedReportId)
                        caseStateUpdateDTO.with {
                            status = IcsrCaseStateEnum.ERROR.toString()
                            error = errorData
                        }
                        reportExecutorService.changeIcsrCaseStatus(caseStateUpdateDTO, currentState, IcsrCaseStateEnum.ERROR.toString())
                        try {
                            ///Added to regenerate R3 file
                            if (file?.exists()) {
                                log.warn("Deleting cached R3 xml ${file.name} as there was validation error so we would need to regenerate")
                                file.delete()
                            }
                        } catch (e) {
                            log.error("Fatal error couldn't delete cached R3 file in TransmitCase")
                        }
                        throw new Exception(errorData)
                    }
                }

            } else if (reportTemplate?.isCiomsITemplate()) {
                //Allowing to generate CIOMS-I file for cache purpose.
                file = dynamicReportService.createCIOMSReport(caseNumber, versionNumber, executedReportConfiguration?.owner, fileDate, isJapanProfile, executedTemplateQuery, processedReportId)
            } else if (reportTemplate?.isMedWatchTemplate()) {
                String prodHashCode = icsrCaseTrackingInstance?.prodHashCode
                file = dynamicReportService.createMedWatchReport(caseNumber, versionNumber, executedReportConfiguration?.owner, fileDate, isJapanProfile, executedTemplateQuery, processedReportId, prodHashCode)
            }
            switch (executedTemplateQuery.distributionChannelName) {
                case DistributionChannelEnum.PV_GATEWAY:
                case DistributionChannelEnum.EXTERNAL_FOLDER:
                    if (executedTemplateQuery.usedTemplate.isCiomsITemplate() || executedTemplateQuery.usedTemplate.isMedWatchTemplate()) {
                        //No need to transmit file to gateway if used template is CIOMS-I or Medwatch and directly mark it as transmitted.
                        reportExecutorService.markCaseTransmitting(executedTemplateQuery, caseNumber, versionNumber, fileDate, "", approvedBy)
                        reportExecutorService.markCaseTransmitted(executedTemplateQuery, caseNumber, versionNumber, comments, new Date(), null, approvedBy)
                        IcsrCaseTracking newIcsrCaseTrackingInstance = null
                        IcsrCaseTracking.withNewSession {
                            newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(icsTemplateQueryId, caseNumber, versionNumber)
                        }
                        Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
                        if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                            newValues.put("approvedBy", approvedBy)
                            newValues.put("approvedOn", approvedOn)
                        }
                        newValues.put("transmissionComments",transmissionComments)
                        AuditLogConfigUtil.logChanges(icsrCaseTrackingInstance, newValues, oldValues
                                , Constants.AUDIT_LOG_UPDATE,ViewHelper.getMessage("auditLog.entityValue.icsr.changes", icsrCaseTrackingInstance?.caseNumber, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTrackingInstance?.recipient), ("" + System.currentTimeMillis()), username, approvedBy)
                        if (executedReportConfiguration.autoSubmit) {
                            Date submissionDateUTC = new Date()
                            String timeZoneId =  executedReportConfiguration?.preferredTimeZone ?: "UTC"
                            Date localDate = DateUtil.covertToDateWithTimeZone(submissionDateUTC, Constants.DateFormat.NO_TZ, timeZoneId)
                            icsrProfileAckService.submitCase(executedReportConfiguration, executedTemplateQuery.id, caseNumber, versionNumber, submissionDateUTC, "Application", localDate, timeZoneId)
                        }
                    } else {
                        transmitFile(executedTemplateQuery, caseNumber, versionNumber, file, executedReportConfiguration.e2bDistributionChannel?.outgoingFolder, fileDate, comments, null, approvedBy)
                    }
                    String xsltName = executedReportConfiguration.xsltName
                    saveR3XMLFile(file, executedTemplateQuery, caseNumber, versionNumber, xsltName)
                    deleteCacheFiles(executedTemplateQuery, caseNumber, versionNumber, fileDate, isJapanProfile)
                    break
                case DistributionChannelEnum.EMAIL:
                default:
                    if (file) {
                        transferFileViaEmail(executedReportConfiguration, caseNumber, versionNumber, executedTemplateQuery.emailConfiguration, file, icsrCaseTrackingInstance, reportTemplate, query)
                    }
                    reportExecutorService.markCaseTransmitting(executedTemplateQuery, caseNumber, versionNumber, fileDate, "", approvedBy)
                    reportExecutorService.markCaseTransmitted(executedTemplateQuery, caseNumber, versionNumber, comments, new Date(), null, approvedBy)
                    IcsrCaseTracking newIcsrCaseTrackingInstance = null
                    IcsrCaseTracking.withNewSession {
                        newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(icsTemplateQueryId, caseNumber, versionNumber)
                    }
                    Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
                    if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                        newValues.put("approvedBy", approvedBy)
                        newValues.put("approvedOn", approvedOn)
                    }
                    newValues.put("transmissionComments",transmissionComments)
                    AuditLogConfigUtil.logChanges(icsrCaseTrackingInstance, newValues, oldValues
                            , Constants.AUDIT_LOG_UPDATE, ViewHelper.getMessage("auditLog.entityValue.icsr.changes", icsrCaseTrackingInstance?.caseNumber, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTrackingInstance?.recipient), ("" + System.currentTimeMillis()), username, approvedBy)
                    if (file) {
                        String xsltName = executedReportConfiguration.xsltName
                        saveR3XMLFile(file, executedTemplateQuery, caseNumber, versionNumber, xsltName)
                        deleteCacheFiles(executedTemplateQuery, caseNumber, versionNumber, fileDate, isJapanProfile)
                    }
                    if (executedReportConfiguration.autoSubmit) {
                        Date submissionDateUTC = new Date()
                        String timeZoneId = executedReportConfiguration?.preferredTimeZone ?: "UTC"
                        Date localDate = DateUtil.covertToDateWithTimeZone(submissionDateUTC, Constants.DateFormat.NO_TZ, timeZoneId)
                        icsrProfileAckService.submitCase(executedReportConfiguration, executedTemplateQuery.id, caseNumber, versionNumber, submissionDateUTC, "Application", localDate, timeZoneId)
                    }
                    break
            }
        } else {
            throw new Exception("Invalid Report Configuration ${executedReportConfiguration} ${executedReportConfiguration?.id} for transmit")
        }
    }

    void deleteCacheFiles(ExecutedTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, Date fileDate, Boolean isPMDA) {
        Map params = [caseNumber: caseNumber]
        String reportName = null
        ExecutedIcsrTemplateQuery executedIcsrTemplateQuery = ExecutedIcsrTemplateQuery.read(executedTemplateQuery.id)
        try {
            if (executedTemplateQuery.reportResult && executedTemplateQuery.reportResult.reportRows) {
                reportName = dynamicReportService.getReportName(executedTemplateQuery.reportResult, false, params)
            } else {
                CaseResultData caseResultData = CaseResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(executedTemplateQuery.id, caseNumber, versionNumber)
                if (caseResultData) {
                    ExecutedIcsrProfileConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) MiscUtil.unwrapProxy(executedTemplateQuery.executedConfiguration)
                    String currentSenderIdentifier = executedConfiguration?.getSenderIdentifier()
                    if (executedIcsrTemplateQuery.distributionChannelName == DistributionChannelEnum.EMAIL) {
                        if ((executedConfiguration.e2bDistributionChannel?.reportFormat in [E2BReportFormatEnum.PDF, E2BReportFormatEnum.EB_PDF]) && executedConfiguration?.isJapanProfile) {
                            params.generatePmdaPaperReport = executedConfiguration?.isJapanProfile
                        }
                    }
                    reportName = dynamicReportService.getReportName(caseResultData, currentSenderIdentifier, false, params, fileDate, isPMDA)
                }
            }
            if (reportName) {
                ExecutedReportConfiguration executedConfigurationInstance = executedTemplateQuery.executedConfiguration
                params.reportLocale = (userService.currentUser?.preference?.locale ?: executedConfigurationInstance.owner.preference.locale).toString()
                List outputFormat = ["R3XML", "PDF", "HTML", "xml"]
                for (String output : outputFormat) {
                    String fileName = ""
                    if(executedTemplateQuery.reportResult)
                        fileName = dynamicReportService.getReportFilename(reportName, output, params.reportLocale)
                    else {
                        if (output.equalsIgnoreCase("HTML")){
                            fileName = dynamicReportService.getReportFilename(reportName, output, params.reportLocale)
                        } else if (output.equalsIgnoreCase("PDF") && !params.generatePmdaPaperReport)
                            fileName = dynamicReportService.getReportFilename(reportName, output, params.reportLocale)
                        else
                            fileName = dynamicReportService.getReportFilename(reportName, output)
                    }
                    File reportFile = dynamicReportService.getReportFile(fileName)
                    if (dynamicReportService.isCached(reportFile, params)) {
                        reportFile.delete()
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error Deleting Cache files for Report Name : " + reportName)
            e.printStackTrace()
        }
    }

    void saveR3XMLFile(File r3ReportFile, ExecutedTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, String xsltName) {
        log.info("Saving R3XML Data for file with name : ${r3ReportFile.name}, Case Number : ${caseNumber} and Version Number : ${versionNumber}")
        XMLResultData xmlResultData = XMLResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(executedTemplateQuery.id, caseNumber, versionNumber)
        IcsrCaseTracking newIcsrCaseTrackingInstance = null
        IcsrCaseTracking.withNewSession {
            newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        }
        Date fileDate = newIcsrCaseTrackingInstance?.generationDate
        Boolean isJapanProfile = newIcsrCaseTrackingInstance?.isJapanProfile()
        try {
            byte[] data = Files.readAllBytes(r3ReportFile.toPath())
            Set<String> options = grailsApplication.config.getProperty('pv.app.e2b.supported.pdf.attachment', Set)
            if (!xmlResultData) {
                xmlResultData = new XMLResultData()
                xmlResultData.caseNumber = caseNumber
                xmlResultData.versionNumber = versionNumber
                xmlResultData.executedTemplateQueryId = executedTemplateQuery.id
                xmlResultData.encryptedValue = data
                if(xsltName && options?.contains(xsltName) && !(executedTemplateQuery.usedTemplate.isCiomsITemplate() || executedTemplateQuery.usedTemplate.isMedWatchTemplate())) {
                    try {
                        File simpleXMLFilename = dynamicReportService.createXMLReport(executedTemplateQuery, false, [caseNumber: caseNumber, versionNumber: versionNumber, exIcsrTemplateQueryId: executedTemplateQuery.id], fileDate, isJapanProfile)
                        Map<String, String> attachmentIds = e2BAttachmentService.fetchAttachmentIds(simpleXMLFilename.text, true)
                        xmlResultData.isAttachmentExist = attachmentIds.size() > 0 ? true : false
                    } catch (Exception resourceNotFoundException) {
                        throw new ResourceNotFoundException("Expected Resource is not avaliable ", resourceNotFoundException)
                    }
                }
                xmlResultData.save(flush: true, failOnError: true)
                insertE2bDetail(r3ReportFile, executedTemplateQuery, caseNumber, versionNumber, xsltName)
            } else {
                xmlResultData.encryptedValue = data
                if(xsltName && options?.contains(xsltName)) {
                    try {
                        File simpleXMLFilename = dynamicReportService.createXMLReport(executedTemplateQuery, false, [caseNumber: caseNumber, versionNumber: versionNumber, exIcsrTemplateQueryId: executedTemplateQuery.id], fileDate, isJapanProfile)
                        Map<String, String> attachmentIds = e2BAttachmentService.fetchAttachmentIds(simpleXMLFilename.text, true)
                        xmlResultData.isAttachmentExist = attachmentIds.size() > 0 ? true : false
                    } catch (Exception resourceNotFoundException) {
                        throw new ResourceNotFoundException("Expected Resource is not avaliable ", resourceNotFoundException)
                    }
                }
                xmlResultData.save(flush: true, failOnError: true)
            }
        } catch (Exception e) {
            log.error("Error Saving R3 file data with fileName : " + r3ReportFile.name)
            e.printStackTrace()
        }
    }

    void insertE2bDetail(File r3ReportFile, ExecutedTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, String xsltName) {
        IcsrCaseTracking icsrTrackingRecord = null
        IcsrCaseTracking.withNewSession {
            icsrTrackingRecord = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        }
        Long processedReportId = icsrTrackingRecord?.processedReportId
        Long tenantId = icsrTrackingRecord?.tenantId
        Long caseId = icsrTrackingRecord?.caseId
        String e2bMsgNum = "${executedTemplateQuery.id}-${caseNumber}-${versionNumber}"
        ExecutedIcsrProfileConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) MiscUtil.unwrapProxy(executedTemplateQuery.executedConfiguration)
        String e2bSenderId = executedConfiguration?.senderId
        String e2bReceiverId = executedConfiguration?.receiverId
        String e2bSenderComName = executedConfiguration?.senderCompanyName
        String e2bReceiverComName = executedConfiguration?.recipientCompanyName
        String e2bReceiverTypeId = executedConfiguration?.recipientTypeId
        String e2bSenderTypeId = executedConfiguration?.senderTypeId
        def connection = utilService.getReportConnection()
        Sql sql = new Sql(connection)
        try {
            Clob e2bText = connection.createClob()
            e2bText.setString(1, r3ReportFile.text.replaceAll("(?i)'","''").toString())
            String insertQuery = "Insert into C_SUBMISSIONS_E2B (TENANT_ID, FLAG_DB_SOURCE, REC_TYPE, PROCESSED_REPORT_ID, FILENAME, E2B) values (?, ?, ?, ?, ?, ?)"
            sql.execute(insertQuery, [tenantId, 2, 0, processedReportId, r3ReportFile.name, e2bText])
            String insertStatement = ""

            insertStatement += "Begin Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TENANT_ID', '${tenantId}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CASE_ID','${caseId}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VERSION_NUM','${versionNumber}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PROCESSED_REPORT_ID', '${processedReportId}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_MESSAGE_NUM', '${e2bMsgNum}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_ID', '${e2bSenderId}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_COM_NAME', '${e2bSenderComName}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_ID', '${e2bReceiverId}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_COM_NAME', '${e2bReceiverComName}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_TYPE_ID', '${e2bSenderTypeId}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_TYPE_ID', '${e2bReceiverTypeId}');";

            insertStatement += "END;"
            sql.execute(insertStatement)
            sql.call("{call PKG_E2B_PROCESSING.p_pop_submissions_e2b_info}");
        } finally {
            sql?.close()
        }
    }

    private String getXSDPathForName(String xsdName) {
        String path = grailsApplication.config.getProperty('pv.app.e2b.xsds.options.' + xsdName) ?: (grailsApplication.config.getProperty('pv.app.e2b.xsds.options.DEFAULT') ?: '')
        if (!path) {
            throw new RuntimeException("XSD file path config is missing for ${xsdName}")
        }
        return path
    }

    File createBatchXMLReport(ReportResult reportResult, List<String> caseNumbers, IcsrReportSpecEnum reportSpec) {
        List<Tuple2<String, ReportResult>> requestData = caseNumbers.collect {
            new Tuple2<>(it, reportResult)
        }
        return createBatchXMLReport(requestData, reportSpec)
    }

    File createBatchXMLReport(List<Tuple2<String, ReportResult>> requestData, IcsrReportSpecEnum reportSpec) {
        List<String> includeTags = ["ichicsrmessageheader", "safetyreport"]
        String reportFileName = "BATCH_${requestData.hashCode() & Integer.MAX_VALUE}"+Constants.ADD_SIMPLE_FOR_R2+".xml"
        String xsltName

        File reportFile = dynamicReportService.getReportFile(reportFileName)
        Node rootNode = new Node(null, "ichicsr")
        XMLReportOutputBuilder reportBuilder = new XMLReportOutputBuilder()
        requestData.each { pair ->
            xsltName = pair.second.executedTemplateQuery.executedConfiguration.xsltName
            Node singleCaseNode = reportBuilder.produceReportNode(pair.second, pair.first)
            if (singleCaseNode) {
                includeTags.each {
                    def node = singleCaseNode.get(it)
                    if (node instanceof Node) {
                        rootNode.append(node)
                    } else if (node instanceof NodeList) {
                        rootNode.append(node.first())
                    }
                }
            }
        }
        Writer writer = new OutputStreamWriter(new FileOutputStream(reportFile))
        writer.withCloseable {
            XmlUtil.serialize(rootNode, new PrintWriter(it))
        }
        if (reportSpec == IcsrReportSpecEnum.E2B_R3) {
            return transformToR3(reportFile, xsltName)
        }
        return reportFile
    }

    File createBulkXMLReport(ReportResult reportResult, List<String> caseNumbers, IcsrReportSpecEnum reportSpec) {
        List<Tuple2<String, ReportResult>> requestData = caseNumbers.collect {
            new Tuple2<>(it, reportResult)
        }
        return createBulkXMLReport(requestData, reportSpec)
    }

    File createBulkXMLReport(List<Tuple2<String, ReportResult>> requestData, IcsrReportSpecEnum reportSpec) {
        String reportFileName = "BULK_${requestData.hashCode() & Integer.MAX_VALUE}_R2.zip"
        String xsltName

        File reportFile = dynamicReportService.getReportFile(reportFileName)
        XMLReportOutputBuilder reportBuilder = new XMLReportOutputBuilder()
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(reportFile))
        zipOutputStream.withCloseable {
            requestData.each { pair ->
                xsltName = pair.second.executedTemplateQuery.executedConfiguration.xsltName
                String caseNumber = pair.first
                Node singleCaseNode = reportBuilder.produceReportNode(pair.second, caseNumber)
                if (singleCaseNode) {
                    ZipEntry zipEntry = new ZipEntry("${caseNumber}.xml")
                    it.putNextEntry(zipEntry)
                    if (reportSpec == IcsrReportSpecEnum.E2B_R3) {
                        File tempR2File = dynamicReportService.getReportFile("${caseNumber}"+Constants.ADD_SIMPLE_FOR_R2+".xml")
                        XmlUtil.serialize(singleCaseNode, new FileWriter(tempR2File))
                        File tempR3File = transformToR3(tempR2File, xsltName)
                        def buffer = new byte[tempR3File.size()]
                        tempR3File.withInputStream { inputStream ->
                            it.write(buffer, 0, inputStream.read(buffer))
                        }
                    } else {
                        XmlUtil.serialize(singleCaseNode, new PrintWriter(it))
                    }
                }
            }
        }
        return reportFile
    }

    private File transformToR3(File simpleXMLFile, String xsltName) {
        String r3ReportFileName = simpleXMLFile.name.replace(Constants.ADD_SIMPLE_FOR_R2+".xml", ".xml")
        File r3ReportFile = dynamicReportService.generateR3XMLFromXSLT(simpleXMLFile, r3ReportFileName, xsltName)
        return r3ReportFile
    }

    void sendIcsrProfileFailureEmailTo(def configuration, def caseNoAndErrorMap) {
        //ICSR Profile Generation : Send failure email for all case number whose r3xml is not generated successfully
        Locale locale = userService.getCurrentUser()?.preference?.locale
        String[] recipients = configuration.executedDeliveryOption?.emailToUsers?.toArray()
        String emailSubject = ViewHelper.getMessage("app.emailService.parsing.error.subject.label", configuration.reportName)
        String runDate = ViewHelper.formatRunDateAndTime(configuration)
        String emailBody = ViewHelper.getMessage("app.label.hi") + "<br><br>"
        emailBody += ViewHelper.getMessage("app.emailService.parsing.error.message.label", configuration.reportName, runDate)
        for (Map.Entry<String, String> entry : caseNoAndErrorMap.entrySet()) {
            emailBody += "<br><br>Case Number : " + entry.getKey()
            emailBody += "<br>Error Message : " + entry.getValue()
        }
        emailBody += "<br><br>"
        if (locale?.language != 'ja') {
            emailBody += ViewHelper.getMessage("app.label.thanks") + "," + "<br>"
        }
        emailBody += ViewHelper.getMessage("app.label.pv.reports")
        emailBody = "<span style=\"font-size: 14px;\">" + emailBody + "</span>"
        emailService.sendEmailWithFiles(recipients, null, emailSubject, emailBody, true, null)
    }

    void transferFileViaEmail(ExecutedReportConfiguration configuration, String caseNumber, Integer versionNumber, EmailConfiguration emailConfiguration, File file, IcsrCaseTracking icsrCaseTracking, ReportTemplate reportTemplate, SuperQuery query){
        Locale locale = userService.getCurrentUser()?.preference?.locale
        reportTemplate = MiscUtil.unwrapProxy(reportTemplate)
        String[] emailTo = emailConfiguration?.to ? emailConfiguration?.to?.split(",") : []
        String[] emailCc = emailConfiguration?.cc ? emailConfiguration.cc.split(",") : []
        String emailSubject = emailConfiguration?.subject ? emailService.insertValues(emailConfiguration?.subject, configuration) : "[pv-reports] Icsr Case delivery for: \"${caseNumber} - ${versionNumber}\""
        String emailBody = ViewHelper.getMessage("app.label.hi") + "<br><br>"
        emailBody += emailConfiguration?.body ? emailService.insertValues(emailConfiguration?.body, configuration) : ViewHelper.getMessage("icsr.case.transmit.email.default.subject", configuration.reportName,caseNumber,versionNumber)
        if (emailBody.find(/\[(safetyReportId|wwid|dueDate|dueInDays|awareDate|fupType|fupNo|generationDate|icsrState|approvalNumber|approvalType|messageForm|caseCountry|studyNumber|caseReceiptDate|primaryEvent|primaryProductTradeName|primarySuspectProduct|messageType|caseSource|schedulingCriteria|caseNumber|versionNumber)\]/) || emailSubject.find(/\[(safetyReportId|wwid|dueDate|dueInDays|awareDate|fupType|fupNo|generationDate|icsrState|approvalNumber|approvalType|messageForm|caseCountry|studyNumber|caseReceiptDate|primaryEvent|primaryProductTradeName|primarySuspectProduct|messageType|caseSource|schedulingCriteria|caseNumber|versionNumber)\]/)) {
            File r2Xml = new File(file.absolutePath)
            if(r2Xml && (reportTemplate?.isCiomsITemplate() || reportTemplate?.isMedWatchTemplate())){
                Sql sql = new Sql(utilService.getReportConnection())
                Map safetyParams = getSafetyReportIdAndWwid(sql, icsrCaseTracking.caseId, icsrCaseTracking.tenantId)
                Map icsrParams = getIcsrTrackingParametersValues(icsrCaseTracking.exIcsrTemplateQueryId, icsrCaseTracking.caseNumber, icsrCaseTracking.versionNumber)
                IcsrCaseEmailPlaceholderDTO emailPlaceholderDTO = new IcsrCaseEmailPlaceholderDTO()
                emailPlaceholderDTO.wwid = safetyParams.'wwid'
                emailPlaceholderDTO.safetyReportId = icsrCaseTracking.isJapanProfile() ? dynamicReportService.getE2bCompanyId(icsrCaseTracking) : safetyParams.'safetyReportId'
                emailPlaceholderDTO.caseNumber = caseNumber
                emailPlaceholderDTO.versionNumber = versionNumber
                emailPlaceholderDTO.awareDate = icsrParams.'awareDate'
                emailPlaceholderDTO.dueDate = icsrParams.'dueDate'
                emailPlaceholderDTO.dueInDays = icsrParams.'dueInDays'
                emailPlaceholderDTO.generationDate = icsrParams.'generationDate'
                emailPlaceholderDTO.fupNo = icsrParams.'fupNo'
                emailPlaceholderDTO.fupType = icsrParams.'fupType'
                emailPlaceholderDTO.icsrState = icsrParams.'icsrState'
                emailPlaceholderDTO.approvalNumber = icsrParams.'approvalNumber'
                emailPlaceholderDTO.approvalType = icsrParams.'approvalType'
                emailPlaceholderDTO.caseCountry = icsrParams.'caseCountry'
                emailPlaceholderDTO.studyNumber = icsrParams.'studyNumber'
                emailPlaceholderDTO.caseReceiptDate = icsrParams.'caseReceiptDate'
                emailPlaceholderDTO.primaryEvent = icsrParams.'primaryEvent'
                emailPlaceholderDTO.primaryProductTradeName = icsrParams.'primaryProductTradeName'
                emailPlaceholderDTO.primarySuspectProduct = icsrParams.'primarySuspectProduct'
                emailPlaceholderDTO.messageType = icsrParams.'messageType'
                emailPlaceholderDTO.caseSource = icsrParams.'caseSource'
                emailPlaceholderDTO.messageForm = ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId).title?: reportTemplate?.name
                emailPlaceholderDTO.schedulingCriteria = ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId).title?: query?.name
                emailSubject = emailService.insertValues(emailSubject, emailPlaceholderDTO)
                emailBody = emailService.insertValues(emailBody, emailPlaceholderDTO)
            } else if (r2Xml) {
                Sql sql = new Sql(utilService.getReportConnection())
                Map safetyParams = getSafetyReportIdAndWwid(sql, icsrCaseTracking.caseId, icsrCaseTracking.tenantId)
                Map icsrParams = getIcsrTrackingParametersValues(icsrCaseTracking.exIcsrTemplateQueryId, icsrCaseTracking.caseNumber, icsrCaseTracking.versionNumber)
                IcsrCaseEmailPlaceholderDTO emailPlaceholderDTO = new IcsrCaseEmailPlaceholderDTO()
                emailPlaceholderDTO.wwid = safetyParams.'wwid'
                emailPlaceholderDTO.safetyReportId = safetyParams.'safetyReportId'
                emailPlaceholderDTO.caseNumber = caseNumber
                emailPlaceholderDTO.versionNumber = versionNumber
                emailPlaceholderDTO.awareDate = icsrParams.'awareDate'
                emailPlaceholderDTO.dueDate = icsrParams.'dueDate'
                emailPlaceholderDTO.dueInDays = icsrParams.'dueInDays'
                emailPlaceholderDTO.generationDate = icsrParams.'generationDate'
                emailPlaceholderDTO.fupNo = icsrParams.'fupNo'
                emailPlaceholderDTO.fupType = icsrParams.'fupType'
                emailPlaceholderDTO.icsrState = icsrParams.'icsrState'
                emailPlaceholderDTO.approvalNumber = icsrParams.'approvalNumber'
                emailPlaceholderDTO.approvalType = icsrParams.'approvalType'
                emailPlaceholderDTO.caseCountry = icsrParams.'caseCountry'
                emailPlaceholderDTO.studyNumber = icsrParams.'studyNumber'
                emailPlaceholderDTO.caseReceiptDate = icsrParams.'caseReceiptDate'
                emailPlaceholderDTO.primaryEvent = icsrParams.'primaryEvent'
                emailPlaceholderDTO.primaryProductTradeName = icsrParams.'primaryProductTradeName'
                emailPlaceholderDTO.primarySuspectProduct = icsrParams.'primarySuspectProduct'
                emailPlaceholderDTO.messageType = icsrParams.'messageType'
                emailPlaceholderDTO.caseSource = icsrParams.'caseSource'
                emailPlaceholderDTO.messageForm = ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId).title?: reportTemplate?.name
                emailPlaceholderDTO.schedulingCriteria = ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId).title?: query?.name
                emailSubject = emailService.insertValues(emailSubject, emailPlaceholderDTO)
                emailBody = emailService.insertValues(emailBody, emailPlaceholderDTO)
            }
        }
        if (emailConfiguration?.body) {
            emailBody = emailBody
        } else {
            emailBody += "<br><br>"
            if (locale?.language != 'ja') {
                emailBody += ViewHelper.getMessage("app.label.thanks") + "," + "<br>"
            }
            emailBody += ViewHelper.getMessage("app.label.pv.reports")
        }
        List files = []
        files.add([type: dynamicReportService.getContentType(file.name.split('\\.',-1).last()),
                   name: file.name,
                   data: file.getBytes()
        ])
        emailService.sendEmailWithFiles(emailTo, emailCc, emailSubject, emailBody, true, files)
    }

    Map getSafetyReportIdAndWwid(Sql sql, Long caseId, Long tenantId){
        String query = "SELECT WWID, E2B_COMPANY_ID FROM C_CASE_WW_IDENTIFIER " +
                "WHERE case_id = :caseId \n" +
                "AND tenant_id = :tenantId \n"
        String wwid = null
        String safetyReportId = null
        try {
            GroovyRowResult result = sql.firstRow(query, [caseId: caseId, tenantId: tenantId])
            wwid=result?.'WWID'
            safetyReportId=result?.'E2B_COMPANY_ID'
        } finally {
            sql?.close()
        }
        return ['wwid' : wwid, 'safetyReportId' : safetyReportId]
    }


    String approvalNumber(Long caseId, Long versionNumber, Long tenantId, Long profileId, Long processedReportId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String approvalNumber = null
        String query = "SELECT B.LIC_NUMBER FROM C_SUBMISSIONS A\n" +
                "JOIN VW_AUTH_APPAL_INFO B ON (A.AUTH_ID = B.AUTH_ID)\n" +
                "WHERE A.CASE_ID = :caseId AND A.VERSION_NUM = :versionNumber AND A.TENANT_ID = :tenantId AND A.PROFILE_ID = :profileId AND A.PROCESSED_REPORT_ID = :processedReportId"
        try {
            GroovyRowResult resultSet = sql.firstRow(query, [caseId:  caseId, versionNumber:  versionNumber, tenantId:  tenantId, profileId: profileId, processedReportId : processedReportId])
            approvalNumber = resultSet?.'LIC_NUMBER'
        } finally {
            sql?.close()
        }
        return approvalNumber
    }

    String approvalType(Long caseId, Long versionNumber, Long tenantId, Long profileId, Long processedReportId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String approvalType = null
        String query = "SELECT B.AUTH_NUM_TYPE FROM C_SUBMISSIONS A\n" +
                "JOIN VW_AUTH_APPAL_INFO B ON (A.AUTH_ID = B.AUTH_ID)\n" +
                "WHERE A.CASE_ID = :caseId AND A.VERSION_NUM = :versionNumber AND A.TENANT_ID = :tenantId AND A.PROFILE_ID = :profileId AND A.PROCESSED_REPORT_ID = :processedReportId"
        try {
            GroovyRowResult resultSet = sql.firstRow(query, [caseId:  caseId, versionNumber:  versionNumber, tenantId:  tenantId, profileId: profileId, processedReportId : processedReportId])
            approvalType = resultSet?.'AUTH_NUM_TYPE'
        } finally {
            sql?.close()
        }
        return approvalType
    }

    String caseCountry(Long caseId, Long versionNumber, Long tenantId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String caseCountry = null
        String query = "select distinct COUNTRY from TX_IDENTIFICATION a , VW_TX_COUNTRIES b where  a.OCCURED_COUNTRY_ID=COUNTRY_ID \n" +
                "and A.CASE_ID = :caseId and A.VERSION_NUM  = :versionNumber and A.TENANT_ID = :tenantId"
        try {
            GroovyRowResult resultSet = sql.firstRow(query, [caseId:  caseId, versionNumber:  versionNumber, tenantId:  tenantId])
            caseCountry = resultSet?.'COUNTRY'
        } finally {
            sql?.close()
        }
        return caseCountry
    }

    String studyNumber(Long caseId, Long versionNumber, Long tenantId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String studyNumber = null
        String query = "select distinct STUDY_NUMBER from  TX_STUDY_IDENTIFICATION A where \n" +
                "A.CASE_ID = :caseId and A.VERSION_NUM = :versionNumber and A.TENANT_ID = :tenantId"
        try {
            GroovyRowResult resultSet = sql.firstRow(query, [caseId:  caseId, versionNumber:  versionNumber, tenantId:  tenantId])
            studyNumber = resultSet?.'STUDY_NUMBER'
        } finally {
            sql?.close()
        }
        return studyNumber
    }

    String caseReceiptDate(Long caseId, Long versionNumber, Long tenantId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String caseReceiptDate = null
        String query = "select DATE_RECEIPT from TX_IDENTIFICATION_FU A where \n" +
                " A.CASE_ID = :caseId and A.VERSION_NUM = :versionNumber and A.TENANT_ID = :tenantId"
        try {
            GroovyRowResult resultSet = sql.firstRow(query, [caseId:  caseId, versionNumber:  versionNumber, tenantId:  tenantId])
            caseReceiptDate = resultSet?.'DATE_RECEIPT'
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            // Parse the original string to Date object
            Date date = inputFormat.parse(caseReceiptDate)
            // Define the output date format
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy")
            // Format the Date object into the desired output format
            caseReceiptDate = outputFormat.format(date)
        } finally {
            sql?.close()
        }
        return caseReceiptDate
    }

    String primaryEvent(Long caseId, Long versionNumber, Long tenantId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String primaryEvent = null
        String query = "select distinct a.MDR_AE_PT from tx_ae_identification a  where FLAG_PRIMARY_AE=1 and \n" +
                "a.case_id = :caseId and a.version_num = :versionNumber and a.tenant_id = :tenantId"
        try {
            GroovyRowResult resultSet  = sql.firstRow(query, [caseId:  caseId, versionNumber:  versionNumber, tenantId:  tenantId])
            primaryEvent = resultSet?.'MDR_AE_PT'
        } finally {
            sql?.close()
        }
        return primaryEvent
    }

    String primarySuspectProduct(Long caseId, Long versionNumber, Long tenantId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String primarySuspectProduct = null
        String query = "SELECT\n" +
                "    nvl(b.PRODUCT_NAME,a.generic_name) AS product_name\n" +
                "FROM\n" +
                "    tx_prod_identification a \n" +
                "    left join VW_PRODUCT b on (b.PRODUCT_ID =  a.PRODUCT_ID)\n" +
                "    LEFT OUTER JOIN pvd_src_standard_decode c ON ( a.drug_role_id = c.src_id\n" +
                "                                                   AND c.key_id = 'CP_DRUG_ROLE_TYPE' )\n" +
                "WHERE\n" +
                "    a.flag_primary_product = 1\n" +
                "    AND a.case_id = :caseId and a.version_num = :versionNumber and a.tenant_id = :tenantId"
        try {
            GroovyRowResult resultSet  = sql.firstRow(query, [caseId:  caseId, versionNumber:  versionNumber, tenantId:  tenantId])
            primarySuspectProduct = resultSet?.'PRODUCT_NAME'
        } finally {
            sql?.close()
        }
        return primarySuspectProduct
    }

    String primaryProductTradeName(Long caseId, Long versionNumber, Long tenantId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String primaryProductTradeName = null
        String query = "select distinct REPTD_PROD_NAME from TX_PROD_IDENTIFICATION A where FLAG_PRIMARY_PRODUCT =1 and \n" +
                "A.CASE_ID = :caseId and A.VERSION_NUM = :versionNumber and A.TENANT_ID = :tenantId"
        try {
            GroovyRowResult resultSet = sql.firstRow(query, [caseId:  caseId, versionNumber:  versionNumber, tenantId:  tenantId])
            primaryProductTradeName = resultSet?.'REPTD_PROD_NAME'
        } finally {
            sql?.close()
        }
        return primaryProductTradeName
    }

    String messageType(Long processedReportId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String messageType = null
        String query = "select E2B_MSG_TYPE_DESC from c_submissions_fu \n" +
                "where PROCESSED_REPORT_ID = :processedReportId"
        try {
            GroovyRowResult resultSet  = sql.firstRow(query, [processedReportId:  processedReportId])
            messageType = resultSet?.'E2B_MSG_TYPE_DESC'
        } finally {
            sql?.close()
        }
        return messageType
    }

    String caseSource(Long caseId, Long versionNumber, Long tenantId) {
        Sql sql = new Sql(utilService.getReportConnection())
        String caseSource = null
        String query = "select distinct REPORT_TYPE from TX_IDENTIFICATION a, VW_TX_LRTY_REPORT_TYPE b where A.SOURCE_TYPE_ID=B.RPT_TYPE_ID \n" +
                " and A.CASE_ID = :caseId and A.VERSION_NUM = :versionNumber and A.TENANT_ID = :tenantId"
        try {
            GroovyRowResult resultSet  = sql.firstRow(query, [caseId:  caseId, versionNumber:  versionNumber, tenantId:  tenantId])
            caseSource = resultSet?.'REPORT_TYPE'
        } finally {
            sql?.close()
        }
        return caseSource
    }

    Map getIcsrTrackingParametersValues(Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber) {
        IcsrCaseTracking icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(exIcsrTemplateQueryId, caseNumber, versionNumber)
        //String awareDate = formatStringIntoDate(icsrCaseTrackingInstance?.awareDate)
        String awareDate = DateUtil.StringFromDate(icsrCaseTrackingInstance?.awareDate, DateUtil.DATEPICKER_FORMAT, "")
        String dueDate = DateUtil.StringFromDate(icsrCaseTrackingInstance?.dueDate, DateUtil.DATEPICKER_FORMAT, "")
        String dueInDays = icsrCaseTrackingInstance?.dueInDays
        String generationDate = DateUtil.StringFromDate(icsrCaseTrackingInstance?.generationDate, DateUtil.DATEPICKER_DATE_TIME_FORMAT, "")
        String icsrState = icsrCaseTrackingInstance?.e2BStatus
        Long fupNo = icsrCaseTrackingInstance?.followupNumber
        String fupType = icsrCaseTrackingInstance?.followupInfo
        String approvalNumber = approvalNumber(icsrCaseTrackingInstance?.caseId, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.tenantId, icsrCaseTrackingInstance?.profileId, icsrCaseTrackingInstance?.processedReportId)
        String approvalType = approvalType(icsrCaseTrackingInstance?.caseId, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.tenantId, icsrCaseTrackingInstance?.profileId, icsrCaseTrackingInstance?.processedReportId)
        String caseCountry = caseCountry(icsrCaseTrackingInstance?.caseId, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.tenantId)
        String studyNumber = studyNumber(icsrCaseTrackingInstance?.caseId, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.tenantId)
        String caseReceiptDate = caseReceiptDate(icsrCaseTrackingInstance?.caseId, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.tenantId)
        String primaryEvent = primaryEvent(icsrCaseTrackingInstance?.caseId, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.tenantId)
        String primarySuspectProduct = primarySuspectProduct(icsrCaseTrackingInstance?.caseId, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.tenantId)
        String primaryProductTradeName = primaryProductTradeName(icsrCaseTrackingInstance?.caseId, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.tenantId)
        String messageType = messageType(icsrCaseTrackingInstance?.processedReportId)
        String caseSource = caseSource(icsrCaseTrackingInstance?.caseId, icsrCaseTrackingInstance?.versionNumber, icsrCaseTrackingInstance?.tenantId)
        return ['awareDate' : awareDate, 'dueDate' : dueDate, 'dueInDays' : dueInDays, 'generationDate' : generationDate, 'icsrState' : icsrState, 'fupNo' : fupNo, 'fupType' : fupType, 'approvalNumber' : approvalNumber, 'approvalType' : approvalType, 'caseCountry' : caseCountry, 'studyNumber' : studyNumber, 'caseReceiptDate' : caseReceiptDate, 'primaryEvent' : primaryEvent, 'primarySuspectProduct' : primarySuspectProduct, 'primaryProductTradeName' : primaryProductTradeName, 'messageType': messageType, 'caseSource' : caseSource]
    }

    Map transferCases(ReportResult reportResult, IcsrProfileConfiguration profileConfiguration, Map<String, Integer> caseNumberMap, Integer dueInDays, Boolean isExpedited) {
        Long sourceTemplateId = reportResult?.executedTemplateQuery?.usedTemplate?.originalTemplateId
        //let only template of interest there.
        profileConfiguration.templateQueries.removeAll{it.templateId == sourceTemplateId}
        ExecutedReportConfiguration executedReportConfiguration = executedConfigurationService.createExecutedConfiguration(profileConfiguration, null, true)
        profileConfiguration.discard()
        CRUDService.instantSaveWithoutAuditLog(executedReportConfiguration)
        ExecutedTemplateQuery targetExTemplateQuery = executedReportConfiguration.executedTemplateQueries.first()
        if (targetExTemplateQuery.usedTemplate.isCiomsITemplate()) {
            log.debug("Transfering cioms data from ${reportResult.name} to ${targetExTemplateQuery} for ${caseNumberMap}")
            reportExecutorService.logIcsrReportCasesToTracking(reportResult,targetExTemplateQuery, caseNumberMap.keySet().collectEntries {
                [it, caseNumberMap.get(it)]
            }, dueInDays, isExpedited)
            log.debug("Transferred cioms data from ${reportResult.name} to ${targetExTemplateQuery.id} for ${caseNumberMap}")
            return [success: caseNumberMap.keySet()]
        }
        return transferXMLCases(reportResult, targetExTemplateQuery, caseNumberMap, dueInDays, isExpedited)
    }

    private Map transferXMLCases(ReportResult reportResult, ExecutedTemplateQuery targetExTemplateQuery, Map<String, Integer> caseNumberMap, Integer dueInDays, Boolean isExpedited) {
        Map result = [:]
        List targetNestedTemplates = targetExTemplateQuery.usedTemplate.nestedTemplates
        Map oldNewTemplateMappings = reportResult.executedTemplateQuery.usedTemplate.nestedTemplates.collectEntries { template ->
            [(template.id): targetNestedTemplates.find { it.originalTemplateId == template.originalTemplateId }.id]
        }
        log.debug("Transfering xml data from ${reportResult.name} to ${targetExTemplateQuery.id} for ${caseNumberMap}")

        List<String> casesTransfered = []
        caseNumberMap.each {
            TarArchiveInputStream inputStreamSource
            String caseNumber = it.key
            try {
                inputStreamSource = new TarArchiveInputStream(new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(reportResult.data.decryptedValue))))
                while (inputStreamSource.nextTarEntry != null) {
                    if ('./' + caseNumber.hashCode() + '.tar.gz' == inputStreamSource.currentEntry.name) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream()
                        TarArchiveOutputStream outputStreamTarget = new TarArchiveOutputStream(new GzipCompressorOutputStream(new BufferedOutputStream(baos)))
                        outputStreamTarget.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR)
                        outputStreamTarget.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
                        try {
                            def entry = inputStreamSource.currentEntry
                            def copyBytes = createCopyOfTarEntryContent(inputStreamSource, oldNewTemplateMappings)
                            entry.setSize(copyBytes.size().toLong())
                            outputStreamTarget.putArchiveEntry(entry)
                            IOUtils.copy(new ByteArrayInputStream(copyBytes), outputStreamTarget)
                            outputStreamTarget.closeArchiveEntry()
                        } finally {
                            outputStreamTarget.close()
                            baos?.close()
                        }
                        CaseResultData data = new CaseResultData(caseNumber: it.key, versionNumber: it.value, executedTemplateQueryId: targetExTemplateQuery.id, executedON: utilService.hostIdentifier)
                        data.setEncryptedValue(baos.toByteArray())
                        data.save(failOnError: true)
                        casesTransfered.add(caseNumber)
                    }
                }
            } finally {
                inputStreamSource?.close()
            }
        }
        reportExecutorService.logIcsrReportCasesToTracking(reportResult, targetExTemplateQuery, casesTransfered.collectEntries {
            [it, caseNumberMap.get(it)]
        }, dueInDays, isExpedited)
        result.success = casesTransfered
        result.notfound = caseNumberMap.keySet() - casesTransfered
        log.debug("Transferred xml data from ${reportResult.name} to ${targetExTemplateQuery.id} for ${caseNumberMap}")
        return result
    }

    private byte[] createCopyOfTarEntryContent(TarArchiveInputStream inputStreamSource, Map oldNewtemplateMapping){
        TarArchiveInputStream subreportsInputStream = new TarArchiveInputStream(new GZIPInputStream(inputStreamSource))
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        TarArchiveOutputStream outputStreamTarget = new TarArchiveOutputStream(new GzipCompressorOutputStream(new BufferedOutputStream(baos)))
        outputStreamTarget.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR)
        outputStreamTarget.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
        try {
            while (subreportsInputStream.nextTarEntry != null) {
                if(subreportsInputStream.currentEntry.name.endsWith(".csv")){
                    String templateId = subreportsInputStream.currentEntry.name.replace(".csv", "").replace("./", "")
                    subreportsInputStream.currentEntry.setName(subreportsInputStream.currentEntry.name.replace(templateId, oldNewtemplateMapping.get(templateId.toLong()).toString()))
                }
                outputStreamTarget.putArchiveEntry(subreportsInputStream.currentEntry)
                IOUtils.copy(subreportsInputStream, outputStreamTarget)
                outputStreamTarget.closeArchiveEntry()
            }
        } finally {
            outputStreamTarget.close()
            baos?.close()
        }
        return baos.toByteArray()
    }

    @ReadOnly(connection = 'pva')
    Integer getVersionNumFor(Long exIcsrTempQueryId, String caseNumber) {
        return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumber(exIcsrTempQueryId, caseNumber)?.versionNumber
    }

    void excludeAttachment(IcsrCaseTracking icsrCaseTracking,  ExecutedIcsrTemplateQuery executedIcsrTemplateQuery, String status) {
        if (status.equals(IcsrCaseStateEnum.COMMIT_ACCEPTED.toString())) {
            Sql sql = new Sql(utilService.getReportConnection())
            try {
                File simpleXMLFilename = dynamicReportService.createXMLReport(executedIcsrTemplateQuery, false, [caseNumber: icsrCaseTracking.caseNumber, versionNumber: icsrCaseTracking.versionNumber, exIcsrTemplateQueryId: executedIcsrTemplateQuery.id], icsrCaseTracking?.transmissionDate, icsrCaseTracking?.isJapanProfile())
                if(simpleXMLFilename) {
                    XMLResultData xmlResultData = XMLResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(icsrCaseTracking.exIcsrTemplateQueryId, icsrCaseTracking.caseNumber, icsrCaseTracking.versionNumber)
                    Map<String, String> attachIdAndBookMarkMap = e2BAttachmentService.fetchAttachmentIds(simpleXMLFilename.text, xmlResultData.isAttachmentExist)
                    if(attachIdAndBookMarkMap && attachIdAndBookMarkMap.size() > 0) {
                        String cleaningUpGttTable = ("Begin execute immediate ('Truncate table GTT_CLL_REPORT_DATA_TEMP'); END;")
                        StringBuilder insertSqlQuery = new StringBuilder("Begin ")
                        attachIdAndBookMarkMap.each { caseAttachments, bookMark ->
                            Integer attachmentId = Integer.parseInt(caseAttachments.split("_")[2])
                            insertSqlQuery.append("INSERT INTO GTT_CLL_REPORT_DATA_TEMP (case_id, tenant_id, text_1, number_2, number_3, text_2) VALUES (${icsrCaseTracking.caseId}, ${icsrCaseTracking.tenantId}, '${icsrCaseTracking.prodHashCode}', ${attachmentId}, ${icsrCaseTracking.processedReportId}, '${icsrCaseTracking.profileName}');")
                        }
                        insertSqlQuery.append('END;')
                        sql.execute(cleaningUpGttTable)
                        sql.execute(insertSqlQuery.toString())
                        sql.call("{call PKG_PVR_ICSR_ROUTINE.P_POP_C_SUB_DOCS()}")
                    }
                }
            }catch (Exception e) {
                log.error("Getting Exception while inserting into GTT_CLL_REPORT_DATA_TEMP and calling p_pop_c_sub_docs ," +e.printStackTrace())
            }finally {
                sql?.close()
            }
        }
    }

}
