package com.rxlogix

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import com.rxlogix.config.*
import com.rxlogix.customException.NoDataFoundXmlException
import com.rxlogix.dynamicReports.*
import com.rxlogix.dynamicReports.reportTypes.WatermarkComponentBuilder
import com.rxlogix.dynamicReports.reportTypes.XMLReportOutputBuilder
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.MiscUtil
import grails.core.GrailsApplication
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.constant.WhenNoDataType
import net.sf.dynamicreports.report.exception.DRException
import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory
import net.sf.jasperreports.engine.util.JRXmlUtils
import org.apache.commons.lang.StringEscapeUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.web.context.request.ServletRequestAttributes
import org.w3c.dom.Document
import java.text.SimpleDateFormat

import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.GZIPOutputStream
import java.util.regex.Matcher
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.concatenatedReport
import org.apache.commons.io.FilenameUtils
import com.rxlogix.util.FileConverterUtil
import com.rxlogix.enums.IcsrAttachmentExtEnum
import org.springframework.web.context.request.RequestContextHolder
import javax.servlet.http.HttpSession

@Transactional
class DynamicReportService {
    GrailsApplication grailsApplication
    Long waitTimeBeforeDownload = 100L
    def configurationService
    def customMessageService
    def reportExecutorService
    def userService
    def hazelService
    def e2BAttachmentService
    def grailsMimeUtility
    def icsrXmlService
    def imageService
    def utilService
    def icsrProfileAckService
    def dataSource_pva
    def sqlGenerationService
    /*ImageService imageService = Holders.applicationContext.getBean("imageService")*/

    Map<String, FileGenerationInfoDTO> currentlyGeneratingFiles = new ConcurrentHashMap<>([:])
    public static String CURRENTLY_GENERATING_FILES_HAZELCAST_MAP = "currentlyGeneratingFiles"
    public static String REPORT_NO_DATA_MESSAGE = "This report contains no data";
    private static final String DEFAULT_XSLT = "xslt/upgrade-to-E2B-R3-icsr.xsl"
// Report Creation ---------------------------------------------------------------------------------------------------

    @NotTransactional
    File executeFileNameSync(String fileName, Closure<File> c) {
        if (hazelService.isEnabled()) {
            try {
                if (!hazelService.createMap(CURRENTLY_GENERATING_FILES_HAZELCAST_MAP)?.get(fileName)) {
                    hazelService.populateMap(fileName, CURRENTLY_GENERATING_FILES_HAZELCAST_MAP)
                }
                hazelService.getAndLockHazelCastMap(CURRENTLY_GENERATING_FILES_HAZELCAST_MAP, fileName)
                Thread.sleep(waitTimeBeforeDownload) // Added to settle down a file which is generating.
                return c.call()
            } finally {
                hazelService.unlockAndRemove(fileName, CURRENTLY_GENERATING_FILES_HAZELCAST_MAP)
            }
        } else {
            if (!currentlyGeneratingFiles.get(fileName)) {
                currentlyGeneratingFiles.put(fileName, new FileGenerationInfoDTO())
            }
            try {
                synchronized (currentlyGeneratingFiles.get(fileName)) {
                    return c.call()
                }
            } finally {
                currentlyGeneratingFiles.remove(fileName)
            }
        }
    }

    void removeObjectForSynchronization(String fileName) {
        Object object
        try {
            if (hazelService.isEnabled()) {
                try {
                    hazelService.forceUnlockMap(fileName, CURRENTLY_GENERATING_FILES_HAZELCAST_MAP)
                } catch (e) {
                    log.error(e.message, e)
                }
                object = hazelService.removeValueFromMap(fileName, CURRENTLY_GENERATING_FILES_HAZELCAST_MAP)
            } else {
                object = currentlyGeneratingFiles.remove(fileName)
            }
            object = null
        } catch (Exception ex) {
            log.error("Unexpected error in dynamicReportsService -> removeObjectForSynchronization", ex)
        }
    }

    @NotTransactional
    File createMultiTemplateReport(ExecutedReportConfiguration executedConfigurationInstance, Map params) {
        ExecutedReportConfiguration.withNewSession {
            def startTime = System.currentTimeMillis()
            boolean isInDraftMode = executedConfigurationInstance.status == ReportExecutionStatusEnum.GENERATED_DRAFT || Boolean.valueOf(params.isInDraftMode ?: false)
            params.isInDraftMode = isInDraftMode
            String reportName = getReportName(executedConfigurationInstance, isInDraftMode, params)
            params.reportLocale = (userService.currentUser?.preference?.locale ?: executedConfigurationInstance.owner.preference.locale).toString()
            String reportFileName = getReportFilename(reportName, params.outputFormat, params.reportLocale)
            File reportFile = getReportFile(reportFileName)
            if (isCached(reportFile, params)) {
                log.info("Delievering Multifile from cache after synchronize: ${reportFileName}")
                return reportFile
            }
            List<JasperReportBuilder> jasperReportBuilderList
            try {
                executeFileNameSync(reportFileName) {
                    if (isCached(reportFile, params)) {
                        log.info("Delievering Multifile from cache after synchronize: ${reportFileName}")
                        return reportFile
                    }
                    setReportTheme(executedConfigurationInstance.owner)
                    List<ExecutedTemplateQuery> executedTemplateQueries = executedConfigurationInstance.fetchExecutedTemplateQueriesByCompletedStatus()
                    if (params.sectionsToExport) {
                        List<Long> selectedSectionsId = params.sectionsToExport instanceof String ? [params.sectionsToExport as Long] : params.sectionsToExport.collect { it as Long }
                        params.sectionsToExport = selectedSectionsId
                        executedTemplateQueries = executedTemplateQueries.findAll { it.id in selectedSectionsId }
                    }
                    params.executedTemplateQueries = executedTemplateQueries
                    JasperConcatenatedReportBuilder mainReport = concatenatedReport()

                    List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

                    // Create Criteria Sheet
                    if (!params.excludeCriteriaSheet) {
                        CriteriaSheetBuilder criteriaSheetBuilder = new CriteriaSheetBuilder()
                        criteriaSheetBuilder.createCriteriaSheet(executedConfigurationInstance, null, params, null, jasperReportBuilderEntryList)
                    }

                    CommentsSheetBuilder commentsSheetBuilder = new CommentsSheetBuilder()
                    /*
                 * If configuration contains TemplateSet and export type is XLSX
                 */
                    if (executedConfigurationInstance.containsTemplateSet()) {
                        createMultiTemplateReportForTemplateSet(executedConfigurationInstance, params, jasperReportBuilderEntryList, commentsSheetBuilder)
                    } else {
                        // Otherwise create reports as it used to create earlier
                        for (ExecutedTemplateQuery executedTemplateQuery : executedTemplateQueries) {
                            createReportForExTempQuery(executedConfigurationInstance, executedTemplateQuery, params, jasperReportBuilderEntryList, commentsSheetBuilder)
                        }
                    }

                    // Create Appendix
                    if (!params.excludeAppendix) {
                        AppendixBuilder appendixBuilder = new AppendixBuilder()
                        appendixBuilder.createAppendix(executedConfigurationInstance, null, params, null, jasperReportBuilderEntryList)
                    }

                    // Create Comments
                    if (!params.excludeComments) {
                        commentsSheetBuilder.createReportCommentsSheet(executedConfigurationInstance, params, null, jasperReportBuilderEntryList)
                    }

                    // Create Legend
                    if (!params.excludeLegend) {
                        LegendSheetBuilder legendSheetBuilder = new LegendSheetBuilder()
                        legendSheetBuilder.createReportLegendSheet(executedConfigurationInstance, params, null, jasperReportBuilderEntryList)
                    }

                    jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
                    params?.outputFormat?.equals(ReportFormatEnum.XLSX.name()) ?: addWatermarkIfNeeded(isInDraftMode, jasperReportBuilderList)
                    mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

                    OutputBuilder outputBuilder = new OutputBuilder()
                    boolean templateType = executedTemplateQueries?.find { it?.executedTemplate?.useFixedTemplate }
                    reportFile = outputBuilder.produceReportOutput(params, reportName, mainReport, jasperReportBuilderEntryList, templateType)

                    def reportTime = System.currentTimeMillis() - startTime
                    log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

                    return reportFile
                }

            } finally {
                jasperReportBuilderList?.each {
                    it.jasperParameters["REPORT_VIRTUALIZER"]?.cleanup()
                }
            }
        }
    }

   /*
    * Creates reports for report configuration
    * having TemplateSet
   */
    private void createMultiTemplateReportForTemplateSet(ExecutedReportConfiguration executedConfigurationInstance, Map params, List<JasperReportBuilderEntry> jasperReportBuilderEntryList, CommentsSheetBuilder commentsSheetBuilder) {
        //Fetches sections and executed template queries map
        Map<ExecutedTemplateQuery, List<ExecutedTemplateQuery>> sectionExTempQueriesMap = executedConfigurationInstance.getSectionExTempQueriesMap()
        /*
          * For each section , creates report for that section's executed template queries,
          * in case of TemplateSet, it can have single or
          * list of executed template queries otherwise
          * each section will have one execute template query
         */
        for (ExecutedTemplateQuery executedTemplateQuery : params.executedTemplateQueries) {
            /*
             *  If executed template type is TemplateSet & export type is XLSX then
             *  creates report for each executed template query of all the templates
             *  associated with that TemplateSet not for the executed template query
             *  of that TemplateSet as data for each template in that templateSet
             *  should be shown as separate sheet in the report
             */
            if (isTemplateSet(executedTemplateQuery) && params.outputFormat == "XLSX" && !(executedTemplateQuery.executedTemplate.linkSectionsByGrouping)) {
                List<ExecutedTemplateQuery> exTempQueries = sectionExTempQueriesMap.get(executedTemplateQuery)
                if(exTempQueries) {
                    for (ExecutedTemplateQuery executedTempQuery : exTempQueries) {
                        createReportForExTempQuery(executedConfigurationInstance, executedTempQuery, params, jasperReportBuilderEntryList, commentsSheetBuilder)
                    }
                }
            } else {
                /*
                 * Otherwise crates report for given executed template query
                 * if executed template query is not of TemplateSet
                 * or export type != XLSX
                 */
                createReportForExTempQuery(executedConfigurationInstance, executedTemplateQuery, params, jasperReportBuilderEntryList, commentsSheetBuilder)
            }
        }
    }

    /*
     * Creates report for given executed template query
     */
    private createReportForExTempQuery(ExecutedReportConfiguration executedConfigurationInstance, ExecutedTemplateQuery executedTempQuery, Map params, List<JasperReportBuilderEntry> jasperReportBuilderEntryList, CommentsSheetBuilder commentsSheetBuilder) {
        ReportResult repResult = executedTempQuery.getReportResult(params.isInDraftMode)
        ReportBuilder reportBuilder = new ReportBuilder()
        if (repResult.executedTemplateQuery.executedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
            reportBuilder.createSingleReport(executedConfigurationInstance, repResult, params, executedTempQuery, jasperReportBuilderEntryList)
        } else {
            reportBuilder.createSingleReportCSV(executedConfigurationInstance, repResult, params, executedTempQuery, jasperReportBuilderEntryList)
        }
        if (!params.excludeComments) {
            commentsSheetBuilder.createSectionCommentsSheet(executedConfigurationInstance, repResult, params, null, jasperReportBuilderEntryList)
        }
        if (params.appendDrillDown) {
            ReportResult drillDown = ReportResult.findByParent(repResult)
            if (drillDown) {
                reportBuilder.createSingleReportCSV(executedConfigurationInstance, drillDown, params,executedTempQuery, jasperReportBuilderEntryList)
            }
        }
    }

    @NotTransactional
    File createSingleCapa8dReport(Capa8D capaInstance, Map params) {
        String reportName = getReportName(capaInstance, params)
        params.reportLocale = (userService.currentUser?.preference?.locale ?: capaInstance.owner.preference.locale).toString()
        String reportFileName = getReportFilename(reportName, params.outputFormat, params.reportLocale)
        File reportFile = getReportFile(reportFileName)
        List<Capa8D> capaList = []
        capaList.add(capaInstance)
        params.isMultipleCapaReport = false
        params.detailed = true
        return createCapa8dReport(capaList, reportName, reportFileName, params);
    }


    @NotTransactional
    File createMultiReportForWidgetExport(List<ReportWidget> reportWidgetList, Map params) {
        String reportName = getReportNameForWidget(reportWidgetList, params)
        String reportFileName = getReportFilename(reportName, params.outputFormat, params.reportLocale)
        return createMultiReportWidgetExport(reportWidgetList, reportName, reportFileName, params);
    }

    File createMultiReportWidgetExport(List<ReportWidget> reportWidgetList, String reportName, String reportFileName, Map params){
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(reportFileName)
        List<JasperReportBuilder> jasperReportBuilderList
        try {
            executeFileNameSync(reportFileName) {
                if (params.sectionsToExport) {
                    List<Long> selectedSectionsId = params.sectionsToExport instanceof String ? [params.sectionsToExport as Long] : params.sectionsToExport.collect { it as Long }
                    params.sectionsToExport = selectedSectionsId
                }
                JasperConcatenatedReportBuilder mainReport = concatenatedReport()

                List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

                reportWidgetList?.each {
                    if(it.reportConfiguration){
                        ExecutedReportConfiguration executedConfigurationInstance = null
                        List<ExecutedTemplateQuery> executedTemplateQueries = null
                        if (!params.excludeCriteriaSheet) {
                            CriteriaSheetBuilder criteriaSheetBuilder = new CriteriaSheetBuilder()
                            executedConfigurationInstance =
                                ExecutedReportConfiguration.findByOwnerAndReportNameAndStatusAndIsDeleted(
                                        it.reportConfiguration.owner,
                                        it.reportConfiguration.reportName,
                                        ReportExecutionStatusEnum.COMPLETED,
                                        false,
                                        [sort: 'id', order: 'desc'])

                            executedTemplateQueries = executedConfigurationInstance?.fetchExecutedTemplateQueriesByCompletedStatus()
                            criteriaSheetBuilder.createCriteriaSheet(executedConfigurationInstance, null, params, null, jasperReportBuilderEntryList)
                        }
                        // Create other reports
                        for (ExecutedTemplateQuery executedTemplateQuery : executedTemplateQueries) {
                            ReportResult reportResult = executedTemplateQuery.getReportResult(false)
                            ReportBuilder reportBuilder = new ReportBuilder()
                            if (reportResult.executedTemplateQuery.executedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                                reportBuilder.createSingleReport(executedConfigurationInstance, reportResult, params, executedTemplateQuery, jasperReportBuilderEntryList)
                            } else {
                                reportBuilder.createSingleReportCSV(executedConfigurationInstance, reportResult, params, executedTemplateQuery, jasperReportBuilderEntryList)
                            }
                        }
                    }
                }

                jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
                mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

                OutputBuilder outputBuilder = new OutputBuilder()
                boolean templateType = executedTemplateQueries?.find { it?.executedTemplate?.useFixedTemplate }
                reportFile = outputBuilder.produceReportOutput(params, reportName, mainReport, jasperReportBuilderEntryList, templateType)

                def reportTime = System.currentTimeMillis() - startTime
                log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

                return reportFile
            }

        } finally {
            jasperReportBuilderList?.each {
                it.jasperParameters["REPORT_VIRTUALIZER"]?.cleanup()
            }
        }
    }

    @NotTransactional
    File createMultiCapa8dReport(List<Capa8D> capaList, Map params) {
        String reportName = getReportName(capaList, params)
        String reportFileName = getReportFilename(reportName, params.outputFormat, params.reportLocale)
        params.isMultipleCapaReport = true
        return createCapa8dReport(capaList, reportName, reportFileName, params);
    }

    File createCapa8dReport(List<Capa8D> capaList, String reportName, String reportFileName, Map params){
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(reportFileName)
        List<JasperReportBuilder> jasperReportBuilderList
        try {
            executeFileNameSync(reportFileName) {
                if (params.sectionsToExport) {
                    List<Long> selectedSectionsId = params.sectionsToExport instanceof String ? [params.sectionsToExport as Long] : params.sectionsToExport.collect { it as Long }
                    params.sectionsToExport = selectedSectionsId
                }
                JasperConcatenatedReportBuilder mainReport = concatenatedReport()

                List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

                Capa8dCriteriaSheetBuilder capa8dCriteriaSheetBuilder = new Capa8dCriteriaSheetBuilder()
                if(params.isMultipleCapaReport){
                    capa8dCriteriaSheetBuilder.createCapa8dCriteriaSheet(capaList, params, jasperReportBuilderEntryList)
                }
                // Create Criteria Sheet
                if (params.detailed) {
                    capaList?.each {
                        capa8dCriteriaSheetBuilder = new Capa8dCriteriaSheetBuilder()
                        capa8dCriteriaSheetBuilder.createCapa8dSheet(it, params, jasperReportBuilderEntryList)
                    }
                }

                jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
                mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

                OutputBuilder outputBuilder = new OutputBuilder()
                reportFile = outputBuilder.produceReportOutput(params, reportName, mainReport, jasperReportBuilderEntryList, false)

                def reportTime = System.currentTimeMillis() - startTime
                log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

                return reportFile
            }

        } finally {
            jasperReportBuilderList?.each {
                it.jasperParameters["REPORT_VIRTUALIZER"]?.cleanup()
            }
        }
    }

    @NotTransactional
    File createReportWithCriteriaSheet(ReportResult reportResult, boolean isInDraftMode, Map params) {
        def startTime = System.currentTimeMillis()
        params.isInDraftMode = isInDraftMode
        ExecutedReportConfiguration executedConfigurationInstance = reportResult.executedTemplateQuery.executedConfiguration
        params.reportLocale = (userService.currentUser?.preference?.locale ?: executedConfigurationInstance.owner.preference.locale).toString()
        String reportName = getReportName(reportResult, isInDraftMode, params)
        String reportFileName = (params.outputFormat != ReportFormatEnum.CSV.name()) ? getReportFilename(reportName, params.outputFormat, params.reportLocale) : getReportFilename(reportName, params.outputFormat)
        File reportFile = getReportFile(reportFileName)
        if (isCached(reportFile, params)) {
            log.info("Delievering Singlefile from cache after synchronize: ${reportFileName}")
            return reportFile
        }
        List<JasperReportBuilder> jasperReportBuilderList
        try {
            executeFileNameSync(reportFileName) {
                if (isCached(reportFile, params)) {
                    log.info("Delievering Singlefile from cache after synchronize: ${reportFileName}")
                    return reportFile
                }
                setReportTheme(executedConfigurationInstance.owner)
                ExecutedTemplateQuery executedTemplateQuery = reportResult.executedTemplateQuery

                if (params.sectionsToExport) {
                    List<Long> selectedSectionsId = params.sectionsToExport instanceof String ? [params.sectionsToExport as Long] : params.sectionsToExport.collect { it as Long }
                    params.sectionsToExport = selectedSectionsId
                }

                JasperConcatenatedReportBuilder mainReport = concatenatedReport();

                List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

                if (!params.excludeCriteriaSheet) {
                    CriteriaSheetBuilder criteriaSheetBuilder = new CriteriaSheetBuilder()
                    criteriaSheetBuilder.createCriteriaSheet(executedConfigurationInstance, reportResult, params, executedTemplateQuery, jasperReportBuilderEntryList)
                }

                ReportBuilder reportBuilder = new ReportBuilder()
                reportBuilder.createSingleReport(executedConfigurationInstance, reportResult, params, executedTemplateQuery, jasperReportBuilderEntryList)

                if (params.appendDrillDown) {
                    ReportResult drillDown = ReportResult.findByParent(reportResult)
                    if (drillDown) {
                        reportBuilder.createSingleReportCSV(executedConfigurationInstance, drillDown, params, executedTemplateQuery, jasperReportBuilderEntryList)
                        ReportResult drillDownChild = ReportResult.findByParent(drillDown)
                        if(drillDownChild) {
                            reportBuilder.createSingleReportCSV(executedConfigurationInstance, drillDownChild, params, executedTemplateQuery, jasperReportBuilderEntryList)
                        }
                    }
                }

                CommentsSheetBuilder commentsSheetBuilder = new CommentsSheetBuilder()
                if (!params.excludeComments) {
                    commentsSheetBuilder.createSectionCommentsSheet(executedConfigurationInstance, reportResult, params, null, jasperReportBuilderEntryList)
                }

                if (!params.excludeAppendix) {
                    AppendixBuilder appendixBuilder = new AppendixBuilder()
                    appendixBuilder.createAppendix(executedConfigurationInstance, reportResult, params, null, jasperReportBuilderEntryList)
                }

                if (!params.excludeComments) {
                    commentsSheetBuilder.createReportCommentsSheet(executedConfigurationInstance, params, executedTemplateQuery, jasperReportBuilderEntryList)
                }

                // Create Legend
                if (!params.excludeLegend) {
                    LegendSheetBuilder legendSheetBuilder = new LegendSheetBuilder()
                    legendSheetBuilder.createReportLegendSheet(executedConfigurationInstance, params, executedTemplateQuery, jasperReportBuilderEntryList)
                }

                jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
                params?.outputFormat?.equals(ReportFormatEnum.XLSX.name()) ?: addWatermarkIfNeeded(isInDraftMode, jasperReportBuilderList)
                mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

                OutputBuilder outputBuilder = new OutputBuilder()
                Boolean templateType = getTemplateType(reportResult)
                reportFile = outputBuilder.produceReportOutput(params, reportName, mainReport, jasperReportBuilderEntryList, templateType)

                def reportTime = System.currentTimeMillis() - startTime
                log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

                return reportFile
            }

        } finally {
            jasperReportBuilderList?.each {
                it.jasperParameters["REPORT_VIRTUALIZER"]?.cleanup()
            }
        }
    }

    boolean getTemplateType(ReportResult reportResult) {
        return reportResult?.executedTemplateQuery?.executedTemplate?.templateType == TemplateTypeEnum.DATA_TAB || reportResult?.executedTemplateQuery?.executedTemplate?.useFixedTemplate
    }

    @NotTransactional
    File createReportWithCriteriaSheetCSV(ReportResult reportResult, boolean isInDraftMode, Map params) {
        def startTime = System.currentTimeMillis()
        boolean isDrillDownReportResult = params.filter
        ExecutedReportConfiguration executedConfigurationInstance = reportResult.executedTemplateQuery.executedConfiguration
        params.reportLocale = (userService.currentUser?.preference?.locale ?: executedConfigurationInstance.owner.preference.locale).toString()
        params.isInDraftMode = isInDraftMode
        String currentSenderIdentifier = ""
        Date fileDate = null
        Boolean isJapanProfile = false
        String reportName = ""
        if(executedConfigurationInstance instanceof ExecutedIcsrProfileConfiguration) {
            currentSenderIdentifier = executedConfigurationInstance.getSenderIdentifier()
            IcsrCaseTracking icsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(params.exIcsrTemplateQueryId as Long, params.caseNumber, params.versionNumber as Long)
            }
            fileDate = icsrCaseTrackingInstance?.generationDate ?: new Date()
            isJapanProfile = icsrCaseTrackingInstance?.isJapanProfile()
            reportName = getReportName(reportResult, currentSenderIdentifier, isInDraftMode, params, fileDate, isJapanProfile)
        } else {
            reportName = getReportName(reportResult, isInDraftMode, params)
        }
        String reportFileName = (params.outputFormat != ReportFormatEnum.CSV.name()) ? getReportFilename(reportName, params.outputFormat, params.reportLocale) : getReportFilename(reportName, params.outputFormat)
        File reportFile = getReportFile(reportFileName)
        if (isCached(reportFile, params)) {
            log.info("Delievering file from cache after synchronize: ${reportFileName}")
            return reportFile
        }
        List<JasperReportBuilder> jasperReportBuilderList
        try {
            executeFileNameSync(reportFileName) {
                if (isCached(reportFile, params)) {
                    log.debug("Delievering SingleCsvfile from cache after synchronize: ${reportFileName}")
                    return reportFile
                }
                setReportTheme(executedConfigurationInstance.owner)
                ExecutedTemplateQuery executedTemplateQuery = reportResult.executedTemplateQuery

                if (params.sectionsToExport) {
                    List<Long> selectedSectionsId = params.sectionsToExport instanceof String ? [params.sectionsToExport as Long] : params.sectionsToExport.collect { it as Long }
                    params.sectionsToExport = selectedSectionsId
                }

                JasperConcatenatedReportBuilder mainReport = concatenatedReport();

                List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

                if (!params.excludeCriteriaSheet) {
                    CriteriaSheetBuilder criteriaSheetBuilder = new CriteriaSheetBuilder()
                    criteriaSheetBuilder.createCriteriaSheet(executedConfigurationInstance, reportResult, params, executedTemplateQuery, jasperReportBuilderEntryList)
                }

                if(isTemplateSet(reportResult.executedTemplateQuery) && params.outputFormat == "XLSX" && !(executedTemplateQuery.executedTemplate.linkSectionsByGrouping)){
                    List<ExecutedTemplateQuery> nestedExecutedTemplateQueries = executedConfigurationInstance.getSectionExTempQueriesMap().get(reportResult.executedTemplateQuery)
                    for (ExecutedTemplateQuery executedTempQuery : nestedExecutedTemplateQueries) {
                        ReportResult repResult = executedTempQuery.getReportResult(isInDraftMode)
                        ReportBuilder reportBuilder = new ReportBuilder()
                        if (repResult.executedTemplateQuery.executedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                            reportBuilder.createSingleReport(executedConfigurationInstance, repResult, params, executedTempQuery, jasperReportBuilderEntryList)
                        } else {
                            reportBuilder.createSingleReportCSV(executedConfigurationInstance, repResult, params, executedTempQuery, jasperReportBuilderEntryList)
                        }
                    }
                }else{
                    ReportBuilder reportBuilder = new ReportBuilder()
                    reportBuilder.createSingleReportCSV(executedConfigurationInstance, reportResult, params, executedTemplateQuery, jasperReportBuilderEntryList)
                    if (params.appendDrillDown) {
                        ReportResult drillDown = ReportResult.findByParent(reportResult)
                        if (drillDown) {
                            reportBuilder.createSingleReportCSV(executedConfigurationInstance, drillDown, params, executedTemplateQuery, jasperReportBuilderEntryList)
                            ReportResult drillDownChild = ReportResult.findByParent(drillDown)
                            if (drillDownChild) {
                                reportBuilder.createSingleReportCSV(executedConfigurationInstance, drillDownChild, params, executedTemplateQuery, jasperReportBuilderEntryList)
                            }
                        }
                    }

                }

                CommentsSheetBuilder commentsSheetBuilder = new CommentsSheetBuilder()
                if (!params.excludeComments && !isDrillDownReportResult && params.outputFormat != ReportFormatEnum.CSV.name()) {
                    commentsSheetBuilder.createSectionCommentsSheet(executedConfigurationInstance, reportResult, params, null, jasperReportBuilderEntryList)
                }

                if (!params.excludeAppendix && !isDrillDownReportResult && params.outputFormat != ReportFormatEnum.CSV.name()) {
                    AppendixBuilder appendixBuilder = new AppendixBuilder()
                    appendixBuilder.createAppendix(executedConfigurationInstance, reportResult, params, null, jasperReportBuilderEntryList)
                }

                if (!params.excludeComments && !isDrillDownReportResult && params.outputFormat != ReportFormatEnum.CSV.name()) {
                    commentsSheetBuilder.createReportCommentsSheet(executedConfigurationInstance, params, executedTemplateQuery, jasperReportBuilderEntryList)
                }

                // Create Legend
                if (!params.excludeLegend && !isDrillDownReportResult && params.outputFormat != ReportFormatEnum.CSV.name()) {
                    LegendSheetBuilder legendSheetBuilder = new LegendSheetBuilder()
                    legendSheetBuilder.createReportLegendSheet(executedConfigurationInstance, params, executedTemplateQuery, jasperReportBuilderEntryList)
                }

                jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
                params?.outputFormat?.equals(ReportFormatEnum.XLSX.name()) ?: addWatermarkIfNeeded(isInDraftMode, jasperReportBuilderList)
                mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

                OutputBuilder outputBuilder = new OutputBuilder()
                Boolean templateType = getTemplateType(reportResult)
                reportFile = outputBuilder.produceReportOutput(params, reportName, mainReport, jasperReportBuilderEntryList, templateType)

                def reportTime = System.currentTimeMillis() - startTime
                log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
                return reportFile

            }
        } finally {
            jasperReportBuilderList?.each {
                it.jasperParameters["REPORT_VIRTUALIZER"]?.cleanup()
            }
        }
    }

    boolean isTemplateSet(ExecutedTemplateQuery executedTemplateQuery ){
        if(executedTemplateQuery.executedTemplate.templateType == TemplateTypeEnum.TEMPLATE_SET){
            return true
        }
        return false
    }

    @NotTransactional
    File createCaseListReport(ExecutedCaseSeries caseSeries, Map params) {
        def startTime = System.currentTimeMillis()

        //ExecutedReportConfiguration executedConfigurationInstance = reportResult.executedTemplateQuery.executedConfiguration
        String sort = params.sort ?: "caseNumber"
        String direction = params.direction ?: "desc"
        String reportName = "CS${caseSeries.id}"
        if(params.parentId){
            reportName += "-PI${params.parentId}"
        }
        switch (params.caseListType) {
            case "openCaseList": reportName = reportName.concat("-open")
                break
            case "removedCaseList": reportName = reportName.concat("-removed")
                break
            default: reportName = reportName.concat("-${sort}-${direction}")

        }
        if (params.filePostfix) {
            reportName += "-${params.filePostfix}"
        }
        if (params.detailed) {
            reportName += "-D"
        }
        params.reportLocale = (userService.currentUser?.preference?.locale ?: caseSeries.owner.preference.locale).toString()
        String reportFileName = getReportFilename(reportName, params.outputFormat, params.reportLocale)
        File reportFile = getReportFile(reportFileName)
        if (isCached(reportFile, params)) {
            return reportFile
        }
        List<JasperReportBuilder> jasperReportBuilderList
        try {
            executeFileNameSync(reportFileName) {
                if (isCached(reportFile, params)) {
                    log.info("Delievering CaseSeriesfile from cache after synchronize: ${reportFileName}")
                    return reportFile
                }
                setReportTheme(caseSeries.owner)
                List<CaseDTO> data
                switch (params.caseListType) {
                    case "openCaseList": data = reportExecutorService.getOpenCaseOfSeries(caseSeries.id, caseSeries.caseSeriesOwner)
                        break
                    case "removedCaseList": data = reportExecutorService.getRemovedCaseOfSeries(caseSeries.id, caseSeries.caseSeriesOwner)
                        break
                    default:
                        if (params.detailed) {
                            sort == "caseNumber" ? sort = "CASE_NUM" : sort
                            data = reportExecutorService.getDetailedCaseOfSeries(caseSeries.id, 1, -1, sort, direction, [:], caseSeries.caseSeriesOwner, true).last()
                        } else {
                            data = reportExecutorService.getCaseOfSeries(caseSeries.id, 1, -1, sort, direction, null, caseSeries.caseSeriesOwner).last()
                        }
                }
                if (params.notEmptyOnly && (data==null || data.size()==0) ) return null
                JasperConcatenatedReportBuilder mainReport = concatenatedReport();
                List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

                // Get report configuration parameters for Criteria and Appendix sheets
                ExecutedReportConfiguration executedConfigurationInstance = caseSeries.findAssociatedConfiguration()
                ReportResult reportResult
                ExecutedTemplateQuery executedTemplateQuery
                if (executedConfigurationInstance?.executedTemplateQueries?.size() == 1) {
                    executedTemplateQuery = executedConfigurationInstance.executedTemplateQueries.get(0)
                    reportResult = executedTemplateQuery.reportResult
                }

                if (!params.excludeCriteriaSheet) {
                    if (executedConfigurationInstance) {
                        CriteriaSheetBuilder criteriaSheetBuilder = new CriteriaSheetBuilder()
                        criteriaSheetBuilder.createCriteriaSheet(executedConfigurationInstance, reportResult, params,
                                executedTemplateQuery, jasperReportBuilderEntryList)
                    } else {
                        CaseSeriesCriteriaSheetBuilder criteriaSheetBuilder = new CaseSeriesCriteriaSheetBuilder()
                        criteriaSheetBuilder.createCriteriaSheet(caseSeries, params, jasperReportBuilderEntryList)
                    }
                }

                CaseSeriesReportBuilder reportBuilder = new CaseSeriesReportBuilder()
                reportBuilder.createCaseSeriesReport(caseSeries, data, params, jasperReportBuilderEntryList)

                if (executedConfigurationInstance && !params.excludeAppendix) {
                    AppendixBuilder appendixBuilder = new AppendixBuilder()
                    appendixBuilder.createAppendix(executedConfigurationInstance, reportResult, params, executedTemplateQuery, jasperReportBuilderEntryList)
                }

                jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
                mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
                OutputBuilder outputBuilder = new OutputBuilder()
                Boolean templateType = getTemplateType(reportResult)
                reportFile = outputBuilder.produceReportOutput(params, reportName, mainReport, jasperReportBuilderEntryList, templateType)

                def reportTime = System.currentTimeMillis() - startTime
                log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
                return reportFile
            }

        }
        catch (DRException e) {
            log.error(e.message)
            throw e
        }
        finally {
            jasperReportBuilderList?.each {
                it.jasperParameters["REPORT_VIRTUALIZER"]?.cleanup()
            }
        }
    }

    File createPaperReport(ReportResult reportResult, boolean isInDraftMode, Map params) {
        if (!reportResult.reportRows) {
            throw new NoDataFoundXmlException("No data to generate xml for reportResult: ${reportResult.id} for paper report")
        }
        ExecutedReportConfiguration executedConfigurationInstance = reportResult.executedTemplateQuery.executedConfiguration
        params.reportLocale = (userService.currentUser?.preference?.locale ?: executedConfigurationInstance.owner.preference.locale).toString()
        String reportName = getReportName(reportResult, isInDraftMode, params)
        String pdfReportFileName = getReportFilename(reportName, ReportFormatEnum.PDF.name(), params.reportLocale)
        File reportFile = getReportFile(pdfReportFileName)
        if (isCached(reportFile, params)) {
            return reportFile
        }
        executeFileNameSync(pdfReportFileName) {
            if (isCached(reportFile, params)) {
                return reportFile
            }
            //generating paper report
            File r3ReportFile = createR3XMLReport(reportResult, isInDraftMode, params)
            Map<String, Object> parameters = new HashMap<String, Object>()
            parameters.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, JRXmlUtils.parse(r3ReportFile))
            JasperReport Rep = JasperCompileManager.compileReport(getJrxmlStreamForPdf('DEFAULT'))
            JasperPrint Prn = JasperFillManager.fillReport(Rep, parameters)
            JasperExportManager.exportReportToPdfFile(Prn, reportFile.absolutePath)
            return reportFile
        }
    }

    File createPDFReport(ExecutedTemplateQuery executedTemplateQuery, boolean isInDraftMode, Map params, Date fileDate, Boolean isPMDA) {
        String reportName = null
        params.reportLang = params.generatePmdaPaperReport ? 'ja' : params.reportLang
        if (executedTemplateQuery.reportResult) {
            if (!executedTemplateQuery.reportResult.reportRows) {
                throw new NoDataFoundXmlException("No data to generate xml for reportResult: ${executedTemplateQuery.reportResult.id} for pdf")
            }
            reportName = getReportName(executedTemplateQuery.reportResult, isInDraftMode, params)
        } else {
            CaseResultData caseResultData = CaseResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(executedTemplateQuery.id, params.caseNumber, params.versionNumber)
            if (!caseResultData) {
                throw new NoDataFoundXmlException("No data to generate xml for case result data: ${executedTemplateQuery.id} for pdf")
            }
            ExecutedIcsrProfileConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) MiscUtil.unwrapProxy(executedTemplateQuery.executedConfiguration)
            String currentSenderIdentifier = executedConfiguration?.getSenderIdentifier()
            reportName = getReportName(caseResultData, currentSenderIdentifier, isInDraftMode, params, fileDate, isPMDA)
        }
        ExecutedReportConfiguration executedConfigurationInstance = executedTemplateQuery.executedConfiguration
        String xmlEncoding = executedConfigurationInstance.xmlEncoding
        params.reportLocale = params.reportLang?:(userService.currentUser?.preference?.locale ?: executedConfigurationInstance.owner.preference.locale).toString()
        String authorityName = executedTemplateQuery.executedConfiguration.xsltName
        String pdfReportFileName = params.generatePmdaPaperReport ? getReportFilename(reportName, ReportFormatEnum.PDF.name()) : getReportFilename(reportName, ReportFormatEnum.PDF.name(), params.reportLocale)
        File reportFile = getReportFile(pdfReportFileName)
        Integer reportLangId = sqlGenerationService.getPVALanguageId(params.reportLocale ?: 'en')
        if (isCached(reportFile, params)) {
            return reportFile
        }
        executeFileNameSync(pdfReportFileName) {
            if (isCached(reportFile, params)) {
                return reportFile
            }
            Map e2bR2localizationMap = [:]
            if (executedTemplateQuery?.executedTemplate?.rootNode) {
                e2bR2localizationMap = processCustomHeadersMap(e2bR2localizationMap, executedTemplateQuery.executedTemplate.rootNode, params.reportLocale)
            }
            File r3ReportFile = createR3XMLReport(executedTemplateQuery, isInDraftMode, params, fileDate, isPMDA)
            r3ReportFile.text = e2BAttachmentService.setDefaultTransformUnicodeAndVersion(r3ReportFile.text, xmlEncoding)
            modifyR3ForProdEventMatrix(r3ReportFile, executedTemplateQuery, isInDraftMode, params, fileDate, isPMDA)
            Map<String, Object> parameters = new HashMap<String, Object>()
            parameters.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, JRXmlUtils.parse(r3ReportFile))
            if (params.generatePmdaPaperReport && executedConfigurationInstance instanceof ExecutedIcsrProfileConfiguration) {
                parameters.put("senderOrganizationName", executedConfigurationInstance.senderUnitOrganizationName)
            }

            if (r3ReportFile?.exists() && !r3ReportFile.isDirectory()) {
                r3ReportFile.delete()
            }

            Sql sql = new Sql(utilService.getReportConnection())
            try {
                if (authorityName && authorityName.equals(Constants.EMDR)) {
                    fetchAlleMDRDecoded(parameters, sql, reportLangId)
                }
                else {
                    fetchAllDecodedName(parameters, sql, reportLangId)
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
            finally {
                sql?.close()
            }
            parameters.put("reportLang", params.reportLocale)
            parameters.put("authorityName", authorityName)
            parameters.put("RxLogo", ImageIO.read(imageService.getCompanyImage()))
            parameters.put("confidentialityIcon", ImageIO.read(imageService.getConfidentialLogo()))
            parameters.put("e2bR2localizationMap", e2bR2localizationMap)
            parameters.put("E2bViewerService", this)

            authorityName = params.generatePmdaPaperReport ? "PMDA_PAPER" : authorityName
            JasperReport Rep = JasperCompileManager.compileReport(getJrxmlStreamForPdf(authorityName))
            JasperPrint Prn = JasperFillManager.fillReport(Rep, parameters)
            JasperExportManager.exportReportToPdfFile(Prn, getReportsDirectory() + pdfReportFileName)
            return new File(getReportsDirectory() + pdfReportFileName)
        }
    }

    File mergeXMLAttachmentIntoPdf(String destinationFolder, String mergedPdfName, Map<String, String> attachFilepathAndBookmark, Map<String, String> docSrcDetail) {
        log.debug("Iterating list of attachment annd merging process started")
        executeFileNameSync(mergedPdfName) {
            String outputFilePath = destinationFolder + File.separator + mergedPdfName
            File reportFile = null
            try {
                reportFile = FileConverterUtil.mergeAttachmentIntoPdf(attachFilepathAndBookmark, outputFilePath, docSrcDetail)
            } catch (Exception e) {
                log.error("Unable to merge the attachment due to :: " + e.getMessage())
                throw new RuntimeException("Unable to merge the attachment due to : " + e.getMessage())
            }
            return reportFile
        }
    }

    File createDirInTempAndMoveFile(String destinationFolder, File file) {
        Path destinationPath = Paths.get(destinationFolder)
        if (Files.exists(destinationPath)) {
            destinationPath.toFile().deleteDir()
        }
        destinationPath.toFile().mkdir()
        Path newFilePath = Paths.get(destinationFolder, file.name)
        return Files.move(Paths.get(file.toString()), newFilePath, StandardCopyOption.REPLACE_EXISTING).toFile()
    }

    File createHTMLReport(ExecutedTemplateQuery executedTemplateQuery, boolean isInDraftMode, Map params, Date fileDate, Boolean isPMDA) {
        String reportName = null
        if (executedTemplateQuery.reportResult) {
            if (!executedTemplateQuery.reportResult.reportRows) {
                throw new NoDataFoundXmlException("No data to generate xml for reportResult: ${executedTemplateQuery.reportResult.id} for Html")
            }
            reportName = getReportName(executedTemplateQuery.reportResult, isInDraftMode, params)
        } else {
            CaseResultData caseResultData = CaseResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(executedTemplateQuery.id, params.caseNumber, params.versionNumber)
            if (!caseResultData) {
                throw new NoDataFoundXmlException("No data to generate xml for case result: ${executedTemplateQuery.id}, ${params.caseNumber}, ${params.versionNumber} for Html")
            }
            ExecutedIcsrProfileConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedConfiguration)
            String currentSenderIdentifier = executedConfiguration?.getSenderIdentifier()
            reportName = getReportName(caseResultData, currentSenderIdentifier, isInDraftMode, params, fileDate, isPMDA)
        }
        String authorityName = executedTemplateQuery.executedConfiguration.xsltName
        ExecutedReportConfiguration executedConfigurationInstance = executedTemplateQuery.executedConfiguration
        String xmlEncoding = executedConfigurationInstance.xmlEncoding
        params.reportLocale = params.reportLang ?: (userService.currentUser?.preference?.locale ?: executedConfigurationInstance.owner.preference.locale).toString()
        String htmlReportFileName = getReportFilename(reportName, "HTML", params.reportLocale)
        File reportFile = getReportFile(htmlReportFileName)
        Integer reportLangId = sqlGenerationService.getPVALanguageId(params.reportLocale ?: 'en')
        if (isCached(reportFile, params)) {
            return reportFile
        }

        executeFileNameSync(htmlReportFileName) {
            if (isCached(reportFile, params)) {
                log.info("Delievering R3Html from cache after synchronize: ${htmlReportFileName}")
                return reportFile
            }
            Map e2bR2localizationMap = [:]
            if (executedTemplateQuery?.executedTemplate?.rootNode) {
                e2bR2localizationMap = processCustomHeadersMap(e2bR2localizationMap, executedTemplateQuery.executedTemplate.rootNode, params.reportLocale)
            }
            File r3ReportFile = createR3XMLReport(executedTemplateQuery, isInDraftMode, params, fileDate, isPMDA)
            r3ReportFile.text = e2BAttachmentService.setDefaultTransformUnicodeAndVersion(r3ReportFile.text, xmlEncoding)
            modifyR3ForProdEventMatrix(r3ReportFile, executedTemplateQuery, isInDraftMode, params, fileDate, isPMDA)
            Map<String, Object> parameters = new HashMap<String, Object>()
            parameters.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, JRXmlUtils.parse(r3ReportFile))
            if (r3ReportFile?.exists() && !r3ReportFile.isDirectory()) {
                r3ReportFile.delete()
            }

            Sql sql = new Sql(utilService.getReportConnection())
            try {
                if (authorityName && authorityName.equals(Constants.EMDR)) {
                    fetchAlleMDRDecoded(parameters, sql, reportLangId)
                }
                else {
                    fetchAllDecodedName(parameters, sql, reportLangId)
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
            finally {
                sql?.close()
            }
            parameters.put("reportLang", params.reportLocale)
            parameters.put("authorityName", authorityName)
            parameters.put("outputFormat", "HTML")
            parameters.put("e2bR2localizationMap", e2bR2localizationMap)
            parameters.put("E2bViewerService", this)
            JasperReport Rep = JasperCompileManager.compileReport(getJrxmlStreamForHtml(authorityName))
            Rep.ignorePagination = true
            JasperPrint Prn = JasperFillManager.fillReport(Rep, parameters)
            JasperExportManager.exportReportToHtmlFile(Prn, reportFile.absolutePath)
            return reportFile
        }
    }

    Map fetchAlleMDRDecoded(Map parameters, Sql sql, Integer reportLangId) {
        parameters.put("EmdrAgeUnit", getEmdrAgeUnit(sql, reportLangId))
        parameters.put("EmdrBirthGender", getEmdrBirthGender(sql, reportLangId))
        parameters.put("EmdrLGGender", getEmdrLGGender(sql, reportLangId))
        parameters.put("EmdEthnicGroup", getEmdEthnicGroup(sql, reportLangId))
        parameters.put("EmdrLethEthnicity", getEmdrLethEthnicity(sql, reportLangId))
        parameters.put("EmdrSeriousness", getEmdrSeriousness(sql, reportLangId))
        parameters.put("EmdrLduUnit", getEmdrLduUnit(sql, reportLangId))
        parameters.put("EmdrConfigUnits", getEmdrConfigUnits(sql, reportLangId))
        parameters.put("EmdrRoute", getEmdrRoute(sql, reportLangId))
        parameters.put("EmdrStaes", getEmdrStates(sql, reportLangId))
        parameters.put("EmdrCountry", getEmdrCountry(sql, reportLangId))
        parameters.put("EmdrReporterType", getEmdrReporterType(sql, reportLangId))
        parameters.put("EmdrOccupation", getOccupation(sql, reportLangId))
        parameters.put("EmdrReporterSource", getReporterSource(sql, reportLangId))
        parameters.put("EmdrFollowupType", getEmdrFollowupType(sql, reportLangId))
        parameters.put("EmdrDeviceUses", getEmdrDeviceUses(sql, reportLangId))
        parameters.put("EmdrIMDRFCodes", getEmdrIMDRFCodes(sql, reportLangId))
        parameters.put("EmdrDeviceProblem", getEmdrDeviceProblem(sql, reportLangId))
        parameters.put("EmdrInvestigationType", getEmdrInvestigationType(sql, reportLangId))
        parameters.put("EmdrInvestigationFindings", getEmdrInvestigateFindings(sql, reportLangId))
        parameters.put("EmdrInvestigatConclusion", getEmdrInvestigatConclusion(sql, reportLangId))
        parameters.put("EmdrInvestigatCode", getEmdrComponentCode(sql, reportLangId))
        parameters.put("EmdrHealthImpact", getEmdrHealthImpact(sql, reportLangId))
        parameters.put("EmdrDeviceCode", getEmdrDeviceCode(sql, reportLangId))
        parameters.put("EmdrProductProblem", getEmdrProductProblem(sql, reportLangId))
        parameters.put("EmdrReportType", getEmdrReportType(sql, reportLangId))
        parameters.put("EmdrCombinationProduct", getEmdrCombinationProduct(sql, reportLangId))
        parameters.put("EmdrGMFReportType", getEmdrGMFReportType(sql, reportLangId))
        parameters.put("EmdrReportableEvent", getEmdrReportableEvent(sql, reportLangId))
        parameters.put("EmdrRemedialAction", getEmdrRemedialAction(sql, reportLangId))
        parameters.put("EmdrInitialReport", getEmdrInitialReport(sql, reportLangId))
        parameters.put("EmdrSuspectProducts", getEmdrSuspectProducts(sql, reportLangId))
        parameters.put("EmdrWeight", getEmdrWeight(sql, reportLangId))
        return parameters
    }

    Map getEmdrAgeUnit(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA_EMDR,AGE_UNIT from VW_LAU_AGE_UNIT where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.FDA_EMDR.toString()): "${it.FDA_EMDR} (${it.AGE_UNIT})"]
        } ?: [:]
    }

    Map getEmdrBirthGender(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI,BIRTH_GENDER from VW_BIRTH_GENDER where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.NCI.toString()): "${it.NCI} (${it.BIRTH_GENDER})"]
        } ?: [:]
    }

    Map getEmdrLGGender(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI,GENDER from VW_LG_GENDER where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.NCI.toString()): "${it.NCI} (${it.GENDER})"]
        } ?: [:]
    }

    Map getEmdEthnicGroup(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI_CODE,SOURCE  from vw_ethnic_group where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.NCI_CODE.toString()): "${it.NCI_CODE} (${it.SOURCE})"]
        } ?: [:]
    }

    Map getEmdrLethEthnicity(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI_CODE,ETHNICITY  from vw_leth_ethnicity where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.NCI_CODE.toString()): "${it.NCI_CODE} (${it.ETHNICITY})"]
        } ?: [:]
    }

    Map getEmdrSeriousness(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI, AE_SERIOUSNESS_TYPE  from vw_ae_seriousness where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.NCI.toString()): "${it.NCI} (${it.AE_SERIOUSNESS_TYPE})"]
        } ?: [:]
    }

    Map getEmdrLduUnit(Sql sql, Integer reportLangId) {
        return sql.rows("select ICSH_M2,UNIT  from vw_ldu_unit where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.ICSH_M2.toString()): "${it.ICSH_M2} (${it.UNIT})"]
        } ?: [:]
    }

    Map getEmdrConfigUnits(Sql sql, Integer reportLangId) {
        return sql.rows("select E2B_CODE,UNIT  from vw_e2b_config_units where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.E2B_CODE.toString()): "${it.E2B_CODE} (${it.UNIT})"]
        } ?: [:]
    }

    Map getEmdrRoute(Sql sql, Integer reportLangId) {
        return sql.rows("select ICSH_M2,ROUTE from VW_LAR_ROUTE where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.ICSH_M2.toString()): "${it.ICSH_M2} (${it.ROUTE})"]
        } ?: [:]
    }

    Map getEmdrStates(Sql sql, Integer reportLangId) {
        return sql.rows("select STATE_CODE,DESCRIPTION from VW_STATES where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.STATE_CODE.toString()): "${it.STATE_CODE} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEmdrCountry(Sql sql, Integer reportLangId) {
        return sql.rows("select A3, COUNTRY from vw_countries where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.A3.toString()): "${it.A3} (${it.COUNTRY})"]
        } ?: [:]
    }

    Map getEmdrReporterType(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI,REPORTER_TYPE from VW_LRET_REPORTER_TYPE where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.NCI.toString()): "${it.NCI} (${it.REPORTER_TYPE})"]
        } ?: [:]
    }

    Map getOccupation(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI,OCCUPATION from VW_LO_OCCUPATION where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.NCI.toString()): "${it.NCI} (${it.OCCUPATION})"]
        } ?: [:]
    }

    Map getReporterSource(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI,DESCRIPTION from VW_REPORT_SOURCE_FDA_EMDR where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.NCI.toString()): "${it.NCI} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEmdrFollowupType(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI,TYPE from vw_cl_followup_type where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.NCI.toString()): "${it.NCI} (${it.TYPE})"]
        } ?: [:]
    }

    Map getEmdrDeviceUses(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI,DEV_USAGE from vw_cdeu_dev_usage where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.NCI.toString()): "${it.NCI} (${it.DEV_USAGE})"]
        } ?: [:]
    }

    Map getEmdrIMDRFCodes(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA,DESCRIPTION from VW_PAT_PROBLEM_MED_CODES where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.FDA.toString()): "${it.FDA} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEmdrDeviceProblem(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA,DESCRIPTION from vw_dev_problem_med_codes where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.FDA.toString()): "${it.FDA} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEmdrInvestigationType(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA,DESCRIPTION from vw_eval_method_med_codes where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.FDA.toString()): "${it.FDA} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEmdrInvestigateFindings(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA,DESCRIPTION from vw_eval_result_med_codes where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.FDA.toString()): "${it.FDA} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEmdrInvestigatConclusion(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA,DESCRIPTION from vw_eval_conc_med_codes where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.FDA.toString()): "${it.FDA} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEmdrComponentCode(Sql sql, Integer reportLangId) {
        return sql.rows("select FDI,DESCRIPTION from vw_component_codes where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.FDI.toString()): "${it.FDI} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEmdrHealthImpact(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA,DESCRIPTION from vw_health_impact where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.FDA.toString()): "${it.FDA} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEmdrDeviceCode(Sql sql, Integer reportLangId) {
        return sql.rows("select Product_code,DESCRIPTION from vw_device_product_code where lang_id = ?", [reportLangId])?.collectEntries {
            [(it.Product_code.toString()): "${it.Product_code} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEmdrProductProblem(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EmdrProductProblem' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEmdrReportType(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EmdrReportType' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEmdrCombinationProduct(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EmdrCombinationProduct' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEmdrGMFReportType(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EmdrGMFReportType' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEmdrReportableEvent(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EmdrReportableEvent' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEmdrRemedialAction(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EmdrRemedialAction' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEmdrInitialReport(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EmdrInitialReport' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }
    Map getEmdrSuspectProducts(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EmdrSuspectProducts' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }
    Map getEmdrWeight(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EmdrWeight' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map fetchAllDecodedName(Map parameters, Sql sql, Integer reportLangId) {
        parameters.put("ActionDrugR3Map", getActionDrugR3Map(sql, reportLangId))
        parameters.put("ActionDrugR2Map", getActionDrugR2Map(sql, reportLangId))
        parameters.put("AgeUnitR2Map", getAgeUnitR2Map(sql, reportLangId))
        parameters.put("AgeUnitR3Map", getAgeUnitR3Map(sql, reportLangId))
        parameters.put("DoseUnitR2Map", getDoseUnitR2Map(sql, reportLangId))
        parameters.put("DoseUnitR3Map", getDoseUnitR3Map(sql, reportLangId))
        parameters.put("ReactionR2Map", getReactionR2Map(sql, reportLangId))
        parameters.put("ReactionR3Map", getReactionR3Map(sql, reportLangId))
        parameters.put("IcsrSourceR2Map", getIcsrSourceR2Map(sql, reportLangId))
        parameters.put("IcsrSourceR3Map", getIcsrSourceR3Map(sql, reportLangId))
        parameters.put("ReportersCountryMap", getReportersCountryMap(sql, reportLangId))
        parameters.put("SummarylangMap", getSummarylangMap(sql, reportLangId))
        parameters.put("EthnicGroupMap", getEthnicGroupMap(sql, reportLangId))
        parameters.put("EvaluationValueMap1", getEvaluationValueMap1(sql, reportLangId))
        parameters.put("EvaluationValueMap2", getEvaluationValueMap2(sql, reportLangId))
        parameters.put("EvaluationValueMap3", getEvaluationValueMap3(sql, reportLangId))
        parameters.put("EvaluationValueMap4", getEvaluationValueMap4(sql, reportLangId))
        parameters.put("RouteMap", getRouteMap(sql, reportLangId))
        parameters.put("EDQMRouteMap", getEDQMRouteMap(sql, reportLangId))
        parameters.put("ReporterStateMap", getReporterStateMap(sql, reportLangId))
        parameters.put("MeddraMap", getMeddraMap(sql, reportLangId))
        parameters.put("EDQMDoseMap", getEDQMDoseMap(sql, reportLangId))
        parameters.put("NullificationMap", getNullificationMap(sql, reportLangId))
        parameters.put("NullificationPaperReportMap", getNullificationPaperReportMap(sql, reportLangId))
        parameters.put("PatientAgeMap", getPatientAgeMap(sql, reportLangId))
        parameters.put("OthHealthProfessionalKRMap", getOthHealthProfessionalKRMap(sql, reportLangId))
        parameters.put("ExpectednessNMPAMap", getExpectednessNMPAMap(sql, reportLangId))
        parameters.put("PrimaryReportNMPAMap", getPrimaryReportNMPAMap(sql, reportLangId))
        parameters.put("OthStudyTypeKRMap", getOthStudyTypeKRMap(sql, reportLangId))
        parameters.put("ReportSourceNMPAMap", getReportSourceNMPAMap(sql, reportLangId))
        parameters.put("PrimarySourceMap", getPrimarySourceMap(sql, reportLangId))
        parameters.put("DrugAdministrationMap", getDrugAdministrationMap(sql, reportLangId))
        parameters.put("EuDrugAssessmentResultMap", getEuDrugAssessmentResultMap(sql, reportLangId))
        parameters.put("DeviceAgeMap", getDeviceAgeMap(sql, reportLangId))
        parameters.put("ResultAssessmentNMPAMap", getResultAssessmentNMPAMap(sql, reportLangId))
        parameters.put("UMCAssessmentKRMap", getUMCAssessmentKRMap(sql, reportLangId))
        parameters.put("RaceNMPAMap", getRaceNMPAMap(sql, reportLangId))
        parameters.put("HealthProfessionalKRMap", getHealthProfessionalKRMap(sql, reportLangId))
        parameters.put("ReportClassificationNMPAMap", getReportClassificationNMPAMap(sql, reportLangId))
        parameters.put("EuDrugAssessmentSourceMap", getEuDrugAssessmentSourceMap(sql, reportLangId))
        parameters.put("AssessmentKRMap", getAssessmentKRMap(sql, reportLangId))
        parameters.put("DechallangeNMPAMap", getDechallangeNMPAMap(sql, reportLangId))
        parameters.put("QualificationMap", getQualificationMap(sql, reportLangId))
        parameters.put("DrugCharacterizationMap", getDrugCharacterizationMap(sql, reportLangId))
        parameters.put("PatientSexMap", getPatientSexMap(sql, reportLangId))
        parameters.put("ReportTypeMap", getReportTypeMap(sql, reportLangId))
        parameters.put("EuDrugAssessmentMethodMap", getEuDrugAssessmentMethodMap(sql, reportLangId))
        parameters.put("SafetyReportTypeMap", getSafetyReportTypeMap(sql, reportLangId))
        parameters.put("ProductHolderNMPAMap", getProductHolderNMPAMap(sql, reportLangId))
        parameters.put("RevaluationMap", getRevaluationMap(sql, reportLangId))
        parameters.put("TermhighlightedMap", getTermhighlightedMap(sql, reportLangId))
        parameters.put("TestResultMap", getTestResultMap(sql, reportLangId))
        parameters.put("SenderTypeMap", getSenderTypeMap(sql, reportLangId))
        parameters.put("StudyTypeMap", getStudyTypeMap(sql, reportLangId))
        parameters.put("EuDrugResultMap", getEuDrugResultMap(sql, reportLangId))
        parameters.put("EvaluationTypeMap", getEvaluationTypeMap(sql, reportLangId))
        parameters.put("DateFormatMap", getDateFormatMap(sql, reportLangId))
        parameters.put("StudyPhase", getPMDAStudyPhase(sql, reportLangId))
        parameters.put("RiskOTCDrug", getPMDARiskOTCDrug(sql, reportLangId))
        parameters.put("RouteAcquirDrug", getPMDARouteAcquirDrug(sql, reportLangId))
        parameters.put("StatusCategoryDrug", getPMDAStatusCategoryDrug(sql, reportLangId))
        parameters.put("LitResearch", getPMDALitResearch(sql, reportLangId))
        parameters.put("DrugAdditionalInfo", getDrugAdditionalInfo(sql, reportLangId))
        parameters.put("MessageTypeMap", getMessageTypeMap(sql, reportLangId))
        parameters.put("AmendmentMap", getAmendmentMap(sql, reportLangId))
        parameters.put("ReportCategoryMap", getReportCategoryMap(sql, reportLangId))
        parameters.put("ReportingCriteriaMap", getReportingCriteriaMap(sql, reportLangId))
        parameters.put("UrgentReportMap", getUrgentReportMap(sql, reportLangId))
        parameters.put("CompActiveClassification", getCompActiveClassificationMap(sql, reportLangId))
        parameters.put("AttachmentMap", getAttachmentMap(sql, reportLangId))
        parameters.put("FDAR3LocCriteriaRptTypeMap", getLocCriteriaRptTyp(sql, reportLangId))
        parameters.put("FDAR3FollowUpTypeMap", getFollowUpType(sql, reportLangId))
        parameters.put("FDAR3DeviceUsageMap", getDeviceUsage(sql, reportLangId))
        parameters.put("FDAR3OperatorOfDeviceMap", getOperatorOfDevice(sql, reportLangId))
        parameters.put("FDARaceMap", getFDARaceCode(sql, reportLangId))
        parameters.put("FDAEthnicGroupMap", getFDAEthnicGroupMap(sql, reportLangId))
        parameters.put("FDAR3RemedialActInitMap", getRemedialActInit(sql, reportLangId))
        parameters.put("FDAR3SpProdCategoryMap", getFDAR3SpProdCategory(sql, reportLangId))
        parameters.put("FDAR3AddDrugInfoMap",['1':'1 (Test)', '2':'2 (Reference)', '3':'3 (Bulk ingredient)', '4':'4 (Bulk Ingredient For Human Prescription Compounding)', '5':'5 (Unapproved Drug Product Manufactured Exclusively for Private Label Distributer)'])
        parameters.put("FDAR3DeviceProdCodeMap", getFDAR3DeviceProdCode(sql, reportLangId))
        parameters.put("EmdrDeviceProblem", getEmdrDeviceProblem(sql, reportLangId))
        return parameters
    }

    Map getReportCategoryMap(Sql sql, Integer reportLangId) {
        return sql.rows("select E2B_R3,DESCRIPTION from VW_REPORT_CATEGORY where lang_id = ? and E2B_R3 IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.E2B_R3.toString()): "${it.E2B_R3} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getSummarylangMap(Sql sql, Integer reportLangId) {
        return sql.rows("select ISO_CODE_639_2,LANGUAGE from VW_LLN_LANGUAGE where lang_id = ? and ISO_CODE_639_2 IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.ISO_CODE_639_2.toString()): "${it.ISO_CODE_639_2} (${it.LANGUAGE})"]
        } ?: [:]
    }

    Map getDrugAdditionalInfo(Sql sql, Integer reportLangId) {
        return sql.rows("select E2B_R3, ADDITIONAL_DRUG_INFO from VW_ADDL_DRUG_INFO where lang_id = ? and E2B_R3 IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.E2B_R3.toString()): "${it.E2B_R3} (${it.ADDITIONAL_DRUG_INFO})"]
        } ?: [:]
    }

    Map getRouteMap(Sql sql, Integer reportLangId) {
        def list = sql.rows("select E2B_CODE, ROUTE from VW_LAR_ROUTE where lang_id = ? and E2B_CODE IS NOT NULL", [reportLangId]) ?: sql.rows(" select E2B_CODE, ROUTE from VW_LAR_ROUTE_DSP where lang_id = ? and E2B_CODE IS NOT NULL", [reportLangId])
        return list?.collectEntries {
            [(it.E2B_CODE.toString()): "${it.E2B_CODE} (${it.ROUTE})"]
        } ?: [:]
    }

    Map getEDQMRouteMap(Sql sql, Integer reportLangId) {
        def list = sql.rows("select EDQM_TERM_ID,ROUTE from VW_LAR_ROUTE where lang_id = ? and EDQM_TERM_ID IS NOT NULL", [reportLangId]) ?: sql.rows(" select EDQM_TERM_ID,ROUTE from VW_LAR_ROUTE_DSP where lang_id = ? and EDQM_TERM_ID IS NOT NULL", [reportLangId])
        return list?.collectEntries {
            [(it.EDQM_TERM_ID.toString()): "${it.EDQM_TERM_ID} (${it.ROUTE})"]
        } ?: [:]
    }

    Map getReportersCountryMap(Sql sql, Integer reportLangId) {
        return sql.rows("select A2,COUNTRY from VW_COUNTRIES where lang_id = ? and A2 IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.A2.toString()): "${it.A2} (${it.COUNTRY})"]
        } ?: [:]
    }

    Map getDoseUnitR2Map(Sql sql, Integer reportLangId) {
        return sql.rows("select E2B_CODE,UNIT from VW_LDU_UNIT where lang_id = ? and E2B_CODE IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.E2B_CODE.toString()): "${it.E2B_CODE} (${it.UNIT})"]
        } ?: [:]
    }

    Map getDoseUnitR3Map(Sql sql, Integer reportLangId) {
        return sql.rows("select UCUM_CODE,UNIT from VW_LDU_UNIT where lang_id = ? and UCUM_CODE IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.UCUM_CODE.toString()): "${it.UCUM_CODE} (${it.UNIT})"]
        } ?: [:]
    }

    Map getEthnicGroupMap(Sql sql, Integer reportLangId) {
        return sql.rows("select NMPA,SOURCE FROM VW_ETHNIC_GROUP where lang_id = ? and NMPA IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.NMPA.toString()): "${it.NMPA} (${it.SOURCE})"]
        } ?: [:]
    }

    Map getFDAEthnicGroupMap(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI_CODE,SOURCE FROM VW_ETHNIC_GROUP where lang_id = ? and NCI_CODE IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.NCI_CODE.toString()): "${it.NCI_CODE} (${it.SOURCE})"]
        } ?: [:]
    }

    Map getEvaluationValueMap1(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA,DESCRIPTION from vw_tx_dev_problem_med_codes where lang_id = ? and FDA IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.FDA.toString()): "${it.FDA} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEvaluationValueMap2(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA,DESCRIPTION from vw_tx_eval_method_med_codes where lang_id = ? and FDA IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.FDA.toString()): "${it.FDA} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEvaluationValueMap3(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA,DESCRIPTION from vw_tx_eval_result_med_codes where lang_id = ? and FDA IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.FDA.toString()): "${it.FDA} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getEvaluationValueMap4(Sql sql, Integer reportLangId) {
        return sql.rows("select FDA,DESCRIPTION from vw_tx_eval_conc_med_codes where lang_id = ? and FDA IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.FDA.toString()): "${it.FDA} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getReporterStateMap(Sql sql, Integer reportLangId) {
        return sql.rows("select STATE_CODE,REPORTER_STATE from VW_REPORTER_STATE where lang_id = ? and STATE_CODE IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.STATE_CODE.toString()): "${it.STATE_CODE} (${it.REPORTER_STATE})"]
        } ?: [:]
    }

    Map getMeddraMap(Sql sql, Integer reportLangId) {
        return sql.rows("select LLT_CODE,LLT_NAME from PVR_MD_TERMS where MEDDRA_LANGUAGE_ID = ? and LLT_CODE IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.LLT_CODE.toString()): "${it.LLT_CODE} (${it.LLT_NAME})"]
        } ?: [:]
    }

    Map getEDQMDoseMap(Sql sql, Integer reportLangId) {
        return sql.rows("select EDQM_TERM_ID,FORMULATION from VW_LFOR_FORMULATION where lang_id = ? and EDQM_TERM_ID IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.EDQM_TERM_ID.toString()): "${it.EDQM_TERM_ID} (${it.FORMULATION})"]
        } ?: [:]
    }

    Map getPMDAStudyPhase(Sql sql, Integer reportLangId) {
        return sql.rows("select E2B_CODE, DEV_PHASE from VW_LDP_DEV_PHASE where lang_id = ? and E2B_CODE IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.E2B_CODE.toString()): "${it.E2B_CODE} (${it.DEV_PHASE})"]
        } ?: [:]
    }

    Map getPMDARiskOTCDrug(Sql sql, Integer reportLangId) {
        return sql.rows("select E2B_R3,DESCRIPTION from vw_tx_otc_drug_risk_category where lang_id = ? and E2B_R3 IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.E2B_R3.toString()): "${it.E2B_R3} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getPMDARouteAcquirDrug(Sql sql, Integer reportLangId) {
        return sql.rows("select E2B_R3,DESCRIPTION from vw_tx_access_to_otc  where lang_id = ? and E2B_R3 IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.E2B_R3.toString()): "${it.E2B_R3} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getPMDAStatusCategoryDrug(Sql sql, Integer reportLangId) {
        return sql.rows("select E2B_R3,DESCRIPTION from vw_tx_approval_category  where lang_id = ? and E2B_R3 IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.E2B_R3.toString()): "${it.E2B_R3} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getPMDALitResearch(Sql sql, Integer reportLangId) {
        return sql.rows("select E2B_R3,DESCRIPTION from VW_LITERATURE_CLASSIFICATION  where lang_id = ? and E2B_R3 IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.E2B_R3.toString()): "${it.E2B_R3} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getAmendmentMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='AmendmentMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getNullificationMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='NullificationMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }
    Map getNullificationPaperReportMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='NullificationPaperReport' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getPatientAgeMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='PatientAgeMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getOthHealthProfessionalKRMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='OthHealthProfessionalKRMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getDateFormatMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='DateFormatMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getReactionR3Map(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='ReactionR3Map' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getExpectednessNMPAMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='ExpectednessNMPAMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getPrimaryReportNMPAMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='PrimaryReportNMPAMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getOthStudyTypeKRMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='OthStudyTypeKRMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getReportSourceNMPAMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='ReportSourceNMPAMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getPrimarySourceMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='PrimarySourceMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getDrugAdministrationMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='DrugAdministrationMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEuDrugAssessmentResultMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EuDrugAssessmentResultMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getDeviceAgeMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='DeviceAgeMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getResultAssessmentNMPAMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='ResultAssessmentNMPAMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getUMCAssessmentKRMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='UMCAssessmentKRMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getRaceNMPAMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='RaceNMPAMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getActionDrugR3Map(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY, VALUE from ICSR_Decoded_View where MAP_TYPE = 'ActionDrugR3Map' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getHealthProfessionalKRMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='HealthProfessionalKRMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getReportClassificationNMPAMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='ReportClassificationNMPAMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEuDrugAssessmentSourceMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EuDrugAssessmentSourceMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getAgeUnitR2Map(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='AgeUnitR2Map' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getAssessmentKRMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='AssessmentKRMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getDechallangeNMPAMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='DechallangeNMPAMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getQualificationMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='QualificationMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getDrugCharacterizationMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='DrugCharacterizationMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getPatientSexMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='PatientSexMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getReportTypeMap(Sql sql, Integer reportLangId) {
        return sql.rows(" select KEY, VALUE from ICSR_Decoded_View where MAP_TYPE = 'ReportTypeMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEuDrugAssessmentMethodMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EuDrugAssessmentMethodMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getIcsrSourceR3Map(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='IcsrSourceR3Map' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getIcsrSourceR2Map(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='IcsrSourceR2Map' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getSafetyReportTypeMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='SafetyReportTypeMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getMessageTypeMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='MessageTypeMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getReactionR2Map(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='ReactionR2Map' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getProductHolderNMPAMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='ProductHolderNMPAMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getRevaluationMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='RevaluationMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getTermhighlightedMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='TermhighlightedMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getTestResultMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='TestResultMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getAgeUnitR3Map(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='AgeUnitR3Map' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getSenderTypeMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='SenderTypeMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getStudyTypeMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='StudyTypeMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getActionDrugR2Map(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='ActionDrugR2Map' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEuDrugResultMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EuDrugResultMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getEvaluationTypeMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='EvaluationTypeMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getReportingCriteriaMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='ReportingCriteriaMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getUrgentReportMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='UrgentReportMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getCompActiveClassificationMap(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='CompActiveClassification' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getAttachmentMap(Sql sql, Integer reportLangId) {
        return sql.rows("select MAP_TYPE,VALUE from ICSR_Decoded_View where MAP_TYPE='AttachmentMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.MAP_TYPE.toString()): it.VALUE]
        } ?: [:]
    }

    Map getLocCriteriaRptTyp(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='FDAR3LocCriteriaRptTypeMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getFollowUpType(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='FDAR3FollowUpTypeMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getRemedialActInit(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='FDAR3RemedialActInitMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getDeviceUsage(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='FDAR3DeviceUsageMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getOperatorOfDevice(Sql sql, Integer reportLangId) {
        return sql.rows("select KEY,VALUE from ICSR_Decoded_View where MAP_TYPE='FDAR3OperatorOfDeviceMap' and lang_id = ?", [reportLangId])?.collectEntries {
            [(it.KEY.toString()): it.VALUE]
        } ?: [:]
    }

    Map getFDARaceCode(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI_CODE,ETHNICITY from VW_LETH_ETHNICITY where lang_id = ? and NCI_CODE IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.NCI_CODE.toString()): "${it.NCI_CODE} (${it.ETHNICITY})"]
        } ?: [:]
    }

    Map getFDAR3DeviceProdCode(Sql sql, Integer reportLangId) {
        return sql.rows("select PRODUCT_CODE,DESCRIPTION from VW_DEVICE_PRODUCT_CODE where lang_id = ? and PRODUCT_CODE IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.PRODUCT_CODE.toString()): "${it.PRODUCT_CODE} (${it.DESCRIPTION})"]
        } ?: [:]
    }

    Map getFDAR3SpProdCategory(Sql sql, Integer reportLangId) {
        return sql.rows("select NCI,SOURCE from VW_SPECIALIZED_PROD_CAT where lang_id = ? and NCI IS NOT NULL", [reportLangId])?.collectEntries {
            [(it.NCI.toString()): "${it.NCI} (${it.SOURCE})"]
        } ?: [:]
    }

    Map processCustomHeadersMap(Map e2bR2localizationMap, XMLTemplateNode rootNode, String locale) {
        if (rootNode.e2bElement != null || rootNode.e2bElementName != null || rootNode.e2bElementNameLocale != null) {
            String element = !rootNode.children ? (rootNode.e2bElement ? (rootNode.e2bElement + " ") : "") : ""
            if(rootNode.e2bElementNameLocale?.e2bLocaleElementName && rootNode.e2bElementNameLocale?.e2bLocale == locale){
                element += rootNode.e2bElementNameLocale?.e2bLocaleElementName ?: rootNode.e2bElementName ?: ""
            } else {
                element += rootNode.e2bElementName ?: ""
            }
            e2bR2localizationMap.put(rootNode.tagName, element)
        }

        rootNode.children.each { childNode ->
            String element = childNode.e2bElement ? (childNode.e2bElement + " ") : ""
            if(childNode.e2bElementNameLocale?.e2bLocaleElementName && childNode.e2bElementNameLocale?.e2bLocale == locale){
                element += childNode.e2bElementNameLocale?.e2bLocaleElementName ?: childNode.e2bElementName ?: ""
            } else {
                element += childNode.e2bElementName ?: ""
            }

            if (childNode.children.size() > 0) {
                processCustomHeadersMap(e2bR2localizationMap, childNode, locale)
            } else if (childNode.e2bElementName != null || childNode.e2bElement != null) {
                if (Constants.duplicateE2BTagParents.contains(childNode.parent.tagName) && Constants.duplicateE2BTags.contains(childNode.tagName)) {
                    e2bR2localizationMap.put(childNode.parent.tagName + "-" + childNode.tagName, element)
                } else {
                    e2bR2localizationMap.put(childNode.tagName, element)
                }
            }
        }
        return e2bR2localizationMap
    }

    File createCIOMSReport(String caseNumber, Integer versionNumber, User user, Date fileDate, Boolean isPMDA, ExecutedTemplateQuery executedIcsrTemplateQuery = null, Long processedReportId = 0L) {
        Map params = [caseNumber: caseNumber, versionNumber: versionNumber, outputFormat: ReportFormatEnum.PDF.name(), excludeCriteriaSheet: true, excludeAppendix: true, excludeComments: true, excludeLegend: true]
        CustomSQLTemplate ciomsTempate = CustomSQLTemplate.findByNameAndCiomsIAndOriginalTemplateIdAndIsDeleted(ReportTemplate.CIOMS_I_TEMPLATE_NAME, true, 0L, false)
        SourceProfile sourceProfile = SourceProfile.findByIsCentral(true)
        // CIOMS report is currently executed for Argus data source only
        // Create executed report instance on the fly
        int blinded = Holders.config.getProperty('ciomsI.blinded.flag', Boolean) ? 1 : 0
        int protectPrivacy = Holders.config.getProperty('ciomsI.privacy.flag', Boolean) ? 1 : 0
        ReportResult reportResult = new ReportResult() {
            def tempExecutedTemplateQuery;

            @Override
            String getName() {
                return "${executedIcsrTemplateQuery?.id}_${caseNumber}_${versionNumber}_${blinded}_${protectPrivacy}"
            }

            @Override
            ExecutedTemplateQuery getExecutedTemplateQuery() {
                return tempExecutedTemplateQuery
            }

            def setExecutedTemplateQuery(ExecutedTemplateQuery executedTemplateQuery) {
                tempExecutedTemplateQuery = executedTemplateQuery
            }
        }
        // this is necessary as we have updated the requirements to only allow report results with COMPLETED status for show pages
        reportResult.executionStatus = ReportExecutionStatusEnum.COMPLETED
        reportResult.scheduledBy = user

        def executedConfiguration = null
        if (!executedIcsrTemplateQuery) {
            def executedTemplateQuery = new ExecutedTemplateQuery(
                    executedTemplate: ciomsTempate,
                    reportResult: reportResult,
                    executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(dateRangeEnum: DateRangeEnum.CUMULATIVE)
            )
            executedConfiguration = new ExecutedConfiguration(
                    reportName: reportResult.name,
                    executedTemplateQueries: [executedTemplateQuery],
                    nextRunDate: new Date(),
                    lastRunDate: new Date(),
                    sourceProfile: sourceProfile,
                    owner: user
            )
            executedTemplateQuery.executedConfiguration = executedConfiguration
            reportResult.setExecutedTemplateQuery(executedTemplateQuery)
        } else {
            reportResult.setExecutedTemplateQuery(executedIcsrTemplateQuery)
            executedConfiguration = executedIcsrTemplateQuery.usedConfiguration
            blinded = executedIcsrTemplateQuery.blindProtected ? 1 : 0
            protectPrivacy = executedIcsrTemplateQuery.privacyProtected ? 1 : 0
        }

        params.put('exIcsrTemplateQueryId', reportResult?.executedTemplateQuery?.id)
        String reportLocale = (user?.preference?.locale ?: (executedConfiguration?.owner?.preference.locale))?.toString()
        String currentSenderIdentifier = executedConfiguration?.getSenderIdentifier()
        String reportName = getReportName(reportResult, currentSenderIdentifier, false, params, fileDate, isPMDA)
        String reportFileName = getReportFilename(reportName, params.outputFormat, reportLocale)
        File reportFile = new File(getReportsDirectory() + reportFileName)
        if (isCached(reportFile, params)) {
            return reportFile
        }
        executeFileNameSync(reportFileName) {
            if (isCached(reportFile, params)) {
                return reportFile
            }
            reportExecutorService.generateSingleCIOMSReport(reportResult, caseNumber, versionNumber, !!blinded, !!protectPrivacy, processedReportId)
            return createReportWithCriteriaSheetCSV(reportResult, false, params)
        }
    }
    File createMedWatchReport(String caseNumber, Integer versionNumber, User user, Date fileDate, Boolean isPMDA, ExecutedTemplateQuery executedIcsrTemplateQuery = null, Long processedReportId = 0L, String prodHashCode = null) {
        Map params = [caseNumber: caseNumber, versionNumber: versionNumber, outputFormat: ReportFormatEnum.PDF.name(), excludeCriteriaSheet: true, excludeAppendix: true, excludeComments: true, excludeLegend: true]
        CustomSQLTemplate medwatchTemplate = CustomSQLTemplate.findByNameAndMedWatchAndOriginalTemplateIdAndIsDeleted(ReportTemplate.MEDWATCH_TEMPLATE_NAME, true, 0L, false)
        SourceProfile sourceProfile = SourceProfile.findByIsCentral(true)
        // CIOMS report is currently executed for Argus data source only
        // Create executed report instance on the fly
        int blinded = Holders.config.getProperty('ciomsI.blinded.flag', Boolean) ? 1 : 0
        int protectPrivacy = Holders.config.getProperty('ciomsI.privacy.flag', Boolean) ? 1 : 0
        ReportResult reportResult = new ReportResult() {
            def tempExecutedTemplateQuery;

            @Override
            String getName() {
                return "${executedIcsrTemplateQuery?.id}_${caseNumber}_${versionNumber}_${blinded}_${protectPrivacy}"
            }

            @Override
            ExecutedTemplateQuery getExecutedTemplateQuery() {
                return tempExecutedTemplateQuery
            }

            def setExecutedTemplateQuery(ExecutedTemplateQuery executedTemplateQuery) {
                tempExecutedTemplateQuery = executedTemplateQuery
            }
        }
        // this is necessary as we have updated the requirements to only allow report results with COMPLETED status for show pages
        reportResult.executionStatus = ReportExecutionStatusEnum.COMPLETED
        reportResult.scheduledBy = user

        def executedConfiguration = null
        if (!executedIcsrTemplateQuery) {
            def executedTemplateQuery = new ExecutedTemplateQuery(
                    executedTemplate: medwatchTemplate,
                    reportResult: reportResult,
                    executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(dateRangeEnum: DateRangeEnum.CUMULATIVE)
            )
            executedConfiguration = new ExecutedConfiguration(
                    reportName: reportResult.name,
                    executedTemplateQueries: [executedTemplateQuery],
                    nextRunDate: new Date(),
                    lastRunDate: new Date(),
                    sourceProfile: sourceProfile,
                    owner: user
            )
            executedTemplateQuery.executedConfiguration = executedConfiguration
            reportResult.setExecutedTemplateQuery(executedTemplateQuery)
        } else {
            reportResult.setExecutedTemplateQuery(executedIcsrTemplateQuery)
            executedConfiguration = executedIcsrTemplateQuery.usedConfiguration
            blinded = executedIcsrTemplateQuery.blindProtected ? 1 : 0
            protectPrivacy = executedIcsrTemplateQuery.privacyProtected ? 1 : 0
        }

        params.put('exIcsrTemplateQueryId', reportResult?.executedTemplateQuery?.id)
        String reportLocale = (user?.preference?.locale ?: (executedConfiguration?.owner?.preference.locale))?.toString()
        String currentSenderIdentifier = executedConfiguration?.getSenderIdentifier()
        String reportName = getReportName(reportResult, currentSenderIdentifier, false, params, fileDate, isPMDA)
        String reportFileName = getReportFilename(reportName, params.outputFormat, reportLocale)
        File reportFile = new File(getReportsDirectory() + reportFileName)
        if (isCached(reportFile, params)) {
            return reportFile
        }
        executeFileNameSync(reportFileName) {
            if (isCached(reportFile, params)) {
                return reportFile
            }
            reportExecutorService.generateSingleCIOMSReport(reportResult, caseNumber, versionNumber, !!blinded, !!protectPrivacy, processedReportId, prodHashCode)
            return createReportWithCriteriaSheetCSV(reportResult, false, params)
        }
    }

    File createR3XMLReport(ExecutedTemplateQuery executedTemplateQuery, boolean isInDraftMode, Map params, Date fileDate, Boolean isPMDA) {
        log.info("DynamicReportService: createR3XMLReport for executedTemplateQuery = ${executedTemplateQuery.id}")
        String reportName = null
        if(executedTemplateQuery.reportResult){
            if (!executedTemplateQuery.reportResult.reportRows) {
                throw new NoDataFoundXmlException("No data to generate xml for reportResult: ${executedTemplateQuery.reportResult.id} for R3 xml")
            }
            reportName = getReportName(executedTemplateQuery.reportResult, isInDraftMode, params)
        } else {
            CaseResultData caseResultData = CaseResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(executedTemplateQuery.id,params.caseNumber,params.versionNumber)
            if(!caseResultData){
                throw new NoDataFoundXmlException("No data to generate xml for case result data: ${executedTemplateQuery.id} for R3 xml")
            }
            ExecutedIcsrProfileConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedConfiguration)
            String currentSenderIdentifier = executedConfiguration?.getSenderIdentifier()
            reportName = getReportName(caseResultData, currentSenderIdentifier, isInDraftMode, params, fileDate, isPMDA)
        }
        ExecutedReportConfiguration executedConfigurationInstance = GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedConfiguration)
        params.reportLocale = params.reportLang?:(userService.currentUser?.preference?.locale ?: executedConfigurationInstance.owner.preference.locale).toString()
        String r3ReportFileName = ""
        if(executedTemplateQuery.reportResult)
            r3ReportFileName = getReportFilename(reportName, ReportFormatEnum.R3XML.name(), params.reportLocale)
        else
            r3ReportFileName = getReportFilename(reportName, ReportFormatEnum.R3XML.name())
        File reportFile = getReportFile(r3ReportFileName)
        XMLResultData xmlResultData = XMLResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(executedTemplateQuery.id, params.caseNumber, params.versionNumber)
        if (xmlResultData) {
            byte[] data = xmlResultData.value
            //Every xml file starts with <?xml and to identify the same from byte data this hexa decimal check has been applied
            //with this it can be verified that the byta data received is of xml file only.
            if (data[0] == 0x3C && data[1] == 0x3F && data[2] == 0x78 && data[3] == 0x6D && data[4] == 0x6C) {
                log.info("Returning file from XML Result Data for exTemQueryId: ${executedTemplateQuery.id}, caseNumber: ${params.caseNumber} and versionNumber: ${params.versionNumber}")
                reportFile = generateR3XMLFromXMLResultData(xmlResultData.value, reportFile)
                return reportFile
            }
        }
        if (isCached(reportFile, params)) {
            return reportFile
        }
        executeFileNameSync(r3ReportFileName) {
            if (isCached(reportFile, params)) {
                return reportFile
            }
            File simpleXMLFilename = createXMLReport(executedTemplateQuery, isInDraftMode, params, fileDate, isPMDA)
            String xsltName = executedConfigurationInstance.xsltName
            String xmlVersion = executedConfigurationInstance.xmlVersion
            String xmlEncoding = executedConfigurationInstance.xmlEncoding
            String xmlDoctype = executedConfigurationInstance.xmlDoctype
            File r3ReportFile = generateR3XMLFromXSLT(simpleXMLFilename, r3ReportFileName, xsltName)
            try {
                r3ReportFile.text = e2BAttachmentService.addMissingAttachmentBytesToXMl(r3ReportFile.text, xsltName, xmlVersion, xmlEncoding, xmlDoctype)
            }catch(Exception e){
                log.error(e.getMessage())
                log.info("Attachment not replaced successfully")
                log.info("Deleting r3 file with name : "+r3ReportFileName)
                File r3TempFileName = new File(getReportsDirectory() + "/" + r3ReportFileName)
                if (r3TempFileName?.exists() && !r3TempFileName.isDirectory()) {
                    r3TempFileName.delete()
                }
                throw new RuntimeException("Unable to update the attachment section for R3 xml : "+e.getMessage())
            }
            return r3ReportFile
        }
    }

    void modifyR3ForProdEventMatrix(File r3ReportFile, ExecutedTemplateQuery executedTemplateQuery, boolean isInDraftMode, Map params, Date fileDate, Boolean isPMDA) {
        File simpleXMLFile = createXMLReport(executedTemplateQuery, isInDraftMode, params, fileDate, isPMDA)
        r3ReportFile.text = handleProductEventMatrix(simpleXMLFile.text, r3ReportFile.text)
    }

    String handleProductEventMatrix(String simpleXMLText, String r3XMLText) {
        if (simpleXMLText?.contains(Constants.E2B_DRUG_TAG_START)) {
            String replacement = Matcher.quoteReplacement(Constants.R3_SECONDARY_TAG + (simpleXMLText.substring(simpleXMLText.indexOf(Constants.E2B_DRUG_TAG_START), simpleXMLText.lastIndexOf(Constants.E2B_DRUG_TAG_END) + Constants.E2B_DRUG_TAG_END.size())))
            r3XMLText = r3XMLText?.replaceFirst(Constants.R3_SECONDARY_TAG, replacement)
        }
        return r3XMLText
    }

    File generateR3XMLFromXMLResultData(byte[] data, File reportFile) {
        Files.write(reportFile.toPath(), data)
        return reportFile
    }

    File generateR3XMLFromXSLT(File simpleXMLFilename, String r3ReportFileName, String xsltName) {
        icsrXmlService.transform(simpleXMLFilename, getXSLTPathForName(xsltName), new File(getReportsDirectory() + r3ReportFileName))
    }

    private String getXSLTPathForName(String xsltName) {
        String path = grailsApplication.config.pv.app.e2b.xslts.options.get(xsltName).'xslt' ?: ''
        if (!path) {
            throw new RuntimeException("XSLT file path config is missing for ${xsltName}")
        }
        return path
    }

    File createXMLReport(ExecutedTemplateQuery executedTemplateQuery, boolean isInDraftMode, Map params, Date fileDate, Boolean isPMDA) {
        log.info("DynamicReportService: createXMLReport for executedTemplateQuery = ${executedTemplateQuery.id}")
        def tempGeneratePmdaPaperReport = params.remove("generatePmdaPaperReport")
        String reportName = null
        byte[] data = null
        if(executedTemplateQuery.reportResult){
            if (!executedTemplateQuery.reportResult.reportRows) {
                throw new NoDataFoundXmlException("No data to generate xml for reportResult: ${executedTemplateQuery.reportResult.id} for R2 xml")
            }
            reportName = getReportName(executedTemplateQuery.reportResult, isInDraftMode, params)
            data = executedTemplateQuery.reportResult.data.decryptedValue
        } else {
            CaseResultData caseResultData = CaseResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(executedTemplateQuery.id,params.caseNumber,params.versionNumber)
            if(!caseResultData){
                throw new NoDataFoundXmlException("No data to generate xml for case result data: ${executedTemplateQuery.id} for R2 xml")
            }
            ExecutedIcsrProfileConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedConfiguration)
            String currentSenderIdentifier = executedConfiguration?.getSenderIdentifier()
            reportName = getReportName(caseResultData, currentSenderIdentifier, isInDraftMode, params, fileDate, isPMDA)
            data = caseResultData.decryptedValue
        }
        if (tempGeneratePmdaPaperReport != null) {
            params.generatePmdaPaperReport = tempGeneratePmdaPaperReport
        } else {
            params.remove("generatePmdaPaperReport")
        }
        def startTime = System.currentTimeMillis()
        ExecutedReportConfiguration executedConfigurationInstance = GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedConfiguration)
        params.reportLocale = params.reportLang?:(userService.currentUser?.preference?.locale ?: executedConfigurationInstance.owner.preference.locale).toString()
        String reportFileName = ""
        if(executedTemplateQuery.reportResult)
            reportFileName = getReportFilename(reportName, ReportFormatEnum.XML.name(), params.reportLocale)
        else
            reportFileName = getReportFilename(reportName, ReportFormatEnum.XML.name())
        File reportFile = getReportFile(reportFileName)
        if (isCached(reportFile, params)) {
            log.debug("Report Found from cache ${reportFileName}")
            return reportFile
        }
        executeFileNameSync(reportFileName) {
            if (isCached(reportFile, params)) {
                log.debug("Report Found from cache ${reportFileName}")
                return reportFile
            }
            XMLReportOutputBuilder reportBuilder = new XMLReportOutputBuilder()
            reportBuilder.produceReportOutput(executedTemplateQuery, data, params, reportName, executedConfigurationInstance.locale?.toString(), reportFile)
            def reportTime = System.currentTimeMillis() - startTime
            log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
            return reportFile
        }
    }

    /**
     * Given a JSON string and list of column names, outputs a CSV representation without the column names.
     * @param JSON byte array wrapped into an InputStream
     * @return CSV as File
     */
    File convertJSONToCSV(InputStream inputStream) {
        File tempFile = File.createTempFile(MiscUtil.generateRandomName(), ".csv.gz", new File(grailsApplication.config.tempDirectory as String))
        log.info("convertJSONToCSV - Temp directory: ${grailsApplication.config.tempDirectory} file: {$tempFile.name}")
        GZIPOutputStream zipStream = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))
        Writer writer = new OutputStreamWriter(zipStream, StandardCharsets.UTF_8)

        try {
            ObjectMapper mapper = new ObjectMapper()
            JsonParser jp = mapper.getJsonFactory().createJsonParser(inputStream)

            JsonToken token = jp.nextToken()

            if (!JsonToken.START_ARRAY.equals(token)) {
                throw new JsonParseException("Expected START_ARRAY token, but got ${token}", jp.currentLocation)
            }

            token = jp.nextToken()

            int count = 0
            String value
            while (!JsonToken.END_ARRAY.equals(token)) {
                if (!JsonToken.START_OBJECT.equals(token) || !token) {
                    throw new JsonParseException("Expected START_OBJECT token, but got ${token}", jp.currentLocation)
                }

                token = jp.nextToken()

                // Assumes JSON attributes are stored in correct column order
                while (!JsonToken.END_OBJECT.equals(token)) {
                    if (count > 0) {
                        writer.append(',')
                    }

                    jp.nextValue()

                    value = jp.getText()

                    if (value.equals("null")) {
                        value = ""
                    }

                    writer.append(StringEscapeUtils.escapeCsv(value))

                    token = jp.nextToken()
                    count++
                }

                token = jp.nextToken()
                count = 0
                writer.append('\n')
            }


        } finally {
            writer?.flush()
            writer?.close()
            zipStream?.close()
        }

        return tempFile
    }

    //Helpers ----------------------------------------------------------------------------------------------------------

    void checkIfNoData(ReportResult reportResult, JasperReportBuilder report) {
        TextFieldBuilder noDataField = cmp.text(REPORT_NO_DATA_MESSAGE).setHeight(7)
        if (!reportResult?.executedTemplateQuery?.executedTemplate?.ciomsI && !reportResult?.executedTemplateQuery?.executedTemplate?.medWatch && !reportResult?.data?.value) {
            report.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
            report.columnHeader(noDataField)
        } else {
            report.setWhenNoDataType(WhenNoDataType.NO_DATA_SECTION)
            report.noData(noDataField)
        }
    }

    public String getReportsDirectory() {
        return grailsApplication.config.tempDirectory as String
    }


    public addWatermarkIfNeeded(boolean isInDraftMode, List<JasperReportBuilder> jasperReportBuilderList){
        if(isInDraftMode){
            jasperReportBuilderList.each {it.background(new WatermarkComponentBuilder(customMessageService.getMessage("app.label.draftWatermark"), it.report.template))}
        }
    }

    /**
     * This lets us know if we are viewing the report on screen (HTML) or printing it to any of the supported output formats.
     * This is needed to determine whether we show the criteria sheet and appendix.  It may be used to toggle
     * styles that differ between HTML and the printable output formats.
     * @param params
     * @return
     */
    public boolean isInPrintMode(Map params) {
        if (!params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) {
            return false
        }
        return true
    }

    String getReportNameWithLocale(String reportName, String locale){
        return reportName + "_" + locale
    }

    String getReportFilename(String reportName, String outputFormat, String locale) {
        if (!outputFormat) {
            outputFormat = ReportFormatEnum.HTML.name()
        }
        if (outputFormat == ReportFormatEnum.R3XML.name()) {
            return getReportNameWithLocale(reportName, locale) + "_R3.xml"
        }
        return getReportNameWithLocale(reportName, locale) + "." + outputFormat.toLowerCase()
    }

    String getReportFilename(String reportName, String outputFormat) {
        if (!outputFormat) {
            outputFormat = ReportFormatEnum.HTML.name()
        }
        if (outputFormat == ReportFormatEnum.XML.name()) {
            return reportName + "-Base.xml"
        }
        if (outputFormat == ReportFormatEnum.R3XML.name()) {
            return reportName + ".xml"
        }
        return reportName + "." + outputFormat.toLowerCase()
    }

    /**
     * Convenience method to get a previously created report File object
     * @param executedConfiguration
     * @return File
     */
    File getReportFile(String filename) {
        return new File(getReportsDirectory() + filename)
    }

    // Remove index from field name. Return original report field name.
    private String getFieldName(String colName) {
        int lastIndex = colName.lastIndexOf('_')
        return colName.substring(0, lastIndex)
    }

    String getContentType(ReportFormatEnum reportFormat) {
        return getContentType(reportFormat?.name()?.toLowerCase())
    }

    String getContentType(String extension) {
        return grailsMimeUtility.getMimeTypeForExtension(extension?.toLowerCase())?.name ?: "application/octet-stream"
    }

    public getReportNameAsTitle(ExecutedReportConfiguration executedConfiguration, ExecutedTemplateQuery executedTemplateQuery, boolean addReportName = true) {

        String sectionName = null
        if (executedTemplateQuery.title == executedConfiguration.reportName) {
            List<ExecutedTemplateQuery> sections = executedConfiguration?.fetchExecutedTemplateQueriesByCompletedStatus()
            if (sections?.size() > 1) {
                sectionName = (addReportName ? executedConfiguration.reportName + ": " : "") + executedTemplateQuery.executedTemplate.name
            } else {
                sectionName = executedTemplateQuery.title
            }
        } else {
            sectionName = executedTemplateQuery.title
        }
        sectionName = MiscUtil.matchCSVPattern(sectionName)

        sectionName
    }

    public getReportNameAsFileName(ExecutedReportConfiguration executedConfiguration, ExecutedTemplateQuery executedTemplateQuery = null) {
        String reportNameWithVersionName = executedConfiguration.reportName + executedConfiguration.getGeneratedReportNameInBrackets()
        String reportName
        if (executedConfiguration.executedTemplateQueries.size() > 1) { // for multiple templateQueries
            reportName = reportNameWithVersionName
        } else { // for single templateQuery
            if (executedConfiguration.fetchExecutedTemplateQueriesByCompletedStatus().size() == 1 || !executedTemplateQuery) {
                reportName = reportNameWithVersionName
            } else {
                if (!executedTemplateQuery.title) {
                    reportName = reportNameWithVersionName
                }else if (executedTemplateQuery.title != executedConfiguration.reportName) {
                    reportName = executedTemplateQuery.title
                } else {
                    reportName = reportNameWithVersionName + ": " + executedTemplateQuery.executedTemplate.name
                }
            }
        }
        String cleanName = FileNameCleaner.cleanFileName(reportName)
        cleanName = truncateFileName(cleanName , Constants.MAX_OFFICE_FILE_NAME_LENGTH)
        return cleanName
    }

    String truncateFileName(String fileName, int maxBytes) {
        byte[] fileNameBytes = fileName.getBytes("UTF-8")
        if (fileNameBytes.length <= maxBytes) {
            return fileName
        }

        int byteCount = 0
        StringBuilder truncatedFileName = new StringBuilder()

        for (char c : fileName.toCharArray()) {
            byte[] charBytes = String.valueOf(c).getBytes("UTF-8")
            if (byteCount + charBytes.length > maxBytes) {
                break
            }
            truncatedFileName.append(c)
            byteCount += charBytes.length
        }

        return truncatedFileName.toString()
    }

    public String getReportName(ExecutedReportConfiguration executedConfigurationInstance, boolean isInDraftMode, Map params) {
        String suffix = ""

        if (params.advancedOptions == "1") {
            //Suffix added to report name if choosing to Save As from Advanced Options
            suffix = Constants.DynamicReports.ADVANCED_OPTIONS_SUFFIX
        }

        if (isInDraftMode) {
            suffix += "Draft"
        }
        if (executedConfigurationInstance instanceof ExecutedPeriodicReportConfiguration && executedConfigurationInstance.periodicReportType == PeriodicReportTypeEnum.NUPR && !(params.includeCaseNumber == 'false')){
            suffix += "_caseNum"
        }
        return "EC$executedConfigurationInstance.id$suffix"
    }

    public String getReportName(ReportResult reportResult, boolean isInDraftMode, Map params) {
        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(reportResult.executedTemplateQuery?.id)
        String nuprSuffix = ""
        if (executedTemplateQuery?.executedConfiguration instanceof ExecutedPeriodicReportConfiguration) {
            ExecutedPeriodicReportConfiguration executedPeriodicReportConfigurationInstance = (ExecutedPeriodicReportConfiguration) executedTemplateQuery?.executedConfiguration
            if((executedPeriodicReportConfigurationInstance.periodicReportType == PeriodicReportTypeEnum.NUPR) && (isFixedTempltNuprCsv(executedTemplateQuery, "PSR FORM 7-2") || isFixedTempltNuprCsv(executedTemplateQuery, "-CSV-3")) && !(params.includeCaseNumber == 'false')) {
//                To differentiate between cache rpt with case num column and without case num column
                nuprSuffix = "_caseNum"
            }
            if (isFixedTempltNuprCsv(executedTemplateQuery, ReportFormatEnum.CSV.name()) && params.outputFormat == ReportFormatEnum.CSV.name()) {
                return (getNuprCsvFileName(executedPeriodicReportConfigurationInstance, executedTemplateQuery) + nuprSuffix)
            }
        }
        String suffix = ""

        if (params.advancedOptions == "1") {
            //Suffix added to report name if choosing to Save As from Advanced Options
            suffix += Constants.DynamicReports.ADVANCED_OPTIONS_SUFFIX
        }

        if(isInDraftMode){
            suffix += "Draft"
        }

        if (params.caseNumber) {
            suffix += "_${params.caseNumber}"
        }

        if (params.dynamic) {
            suffix += "_dyn_" + System.currentTimeMillis()
        }
        suffix+= nuprSuffix
        return "R$reportResult.name$suffix"
    }

    public String getReportName(String senderIdentifier, boolean isInDraftMode, Map params, Date fileDate, Boolean isPMDA) {
        String suffix = ""
        String prefix = ""

        if (params.caseNumber) {
            suffix += "-${params.caseNumber}"
        }

        if(isInDraftMode){
            prefix += "Draft-"
        }

        Map e2bCompIdAndRptCategory = getFileNameSuffix(params)
        if(isPMDA) {
            if(!e2bCompIdAndRptCategory.pmdaIdnt)
                suffix += "-${params.exIcsrTemplateQueryId}"
            else
                suffix += e2bCompIdAndRptCategory.pmdaIdnt
        } else {
            suffix += "-${params.exIcsrTemplateQueryId}"
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate=""

        String pmdaFileDate =  new SimpleDateFormat('yyyyMMddHHmmss') {{
            setTimeZone(TimeZone.getTimeZone('Asia/Tokyo'))
        }}.format(fileDate)

        if(fileDate == null) {
            formattedDate = null
        } else {
            if(isPMDA && !e2bCompIdAndRptCategory.pmdaIdnt) formattedDate = pmdaFileDate
            else formattedDate = dateFormat.format(fileDate)
        }
        String lastThreeDgtOfExTmpltQryId = params.exIcsrTemplateQueryId.toString().takeRight(3)
        senderIdentifier = senderIdentifier.replaceAll(" ", "_")
        return "${prefix}"+"I-"+"$senderIdentifier"+"-"+"$formattedDate$lastThreeDgtOfExTmpltQryId$suffix"
    }

    public String getReportName(ReportResult reportResult, String senderIdentifier, boolean isInDraftMode, Map params, Date fileDate, Boolean isPMDA) {
        String suffix = ""
        String prefix = ""

        if (params.advancedOptions == "1") {
            suffix += Constants.DynamicReports.ADVANCED_OPTIONS_SUFFIX
        }

        if(isInDraftMode){
            prefix += "Draft-"
        }

        if (params.caseNumber) {
            suffix += "-${params.caseNumber}"
        }

        if (params.dynamic) {
            suffix += "-dyn-" + System.currentTimeMillis()
        }

        Map e2bCompIdAndRptCategory = getFileNameSuffix(params)
        if(isPMDA) {
            if(!e2bCompIdAndRptCategory.pmdaIdnt)
                suffix += "-${params.exIcsrTemplateQueryId}"
            else
                suffix += e2bCompIdAndRptCategory.pmdaIdnt
        } else {
            suffix += "-${params.exIcsrTemplateQueryId}"
        }

        String lastThreeDgtOfExTmpltQryId = params.exIcsrTemplateQueryId.toString().takeRight(3)

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate=""

        String pmdaFileDate =  new SimpleDateFormat('yyyyMMddHHmmss') {{
            setTimeZone(TimeZone.getTimeZone('Asia/Tokyo'))
        }}.format(fileDate)

        if(fileDate == null) {
            formattedDate = null
        } else {
            if(isPMDA && !e2bCompIdAndRptCategory.pmdaIdnt) formattedDate = pmdaFileDate
            else formattedDate = dateFormat.format(fileDate)
        }
        senderIdentifier = senderIdentifier.replaceAll(" ", "_")
        return "${prefix}"+"I-"+"$senderIdentifier"+"-"+"$formattedDate$lastThreeDgtOfExTmpltQryId$suffix"
    }

    public String getReportName(CaseResultData caseResultData, String senderIdentifier, boolean isInDraftMode, Map params, Date fileDate, Boolean isPMDA) {

        String suffix = ""
        String prefix = ""

        if (params.advancedOptions == "1") {
            //Suffix added to report name if choosing to Save As from Advanced Options
            suffix += Constants.DynamicReports.ADVANCED_OPTIONS_SUFFIX
        }

        if(isInDraftMode){
            prefix += "Draft-"
        }

        if (params.caseNumber) {
            suffix += "-${params.caseNumber}"
        }

        if (params.dynamic) {
            suffix += "-dyn-" + System.currentTimeMillis()
        }

        Map e2bCompIdAndRptCategory = getFileNameSuffix(params)
        if(isPMDA) {
            if(!e2bCompIdAndRptCategory.pmdaIdnt)
                suffix += "-${params.exIcsrTemplateQueryId}"
            else
                suffix += e2bCompIdAndRptCategory.pmdaIdnt
        } else {
            suffix += "-${params.exIcsrTemplateQueryId}"
        }

        if(params.generatePmdaPaperReport) {
            if (e2bCompIdAndRptCategory.rptCategory in ['AA', 'AB', 'AC', 'AD', 'DA', 'DB', 'DC', 'DD']) {
                suffix += "-別紙様式第1,2"
            } else if (e2bCompIdAndRptCategory.rptCategory in ['AE', 'AF', 'BC', 'BD', 'DE', 'DF']) {
                suffix += "-別紙様式第3,4"
            } else if (e2bCompIdAndRptCategory.rptCategory in ['AG', 'DG']) {
                suffix += "-別紙様式第5,6"
            }
        }

        String lastThreeDgtOfExTmpltQryId = params.exIcsrTemplateQueryId.toString().takeRight(3)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate=""

        String pmdaFileDate =  new SimpleDateFormat('yyyyMMddHHmmss') {{
            setTimeZone(TimeZone.getTimeZone('Asia/Tokyo'))
        }}.format(fileDate)

        if(fileDate == null) {
            formattedDate = null
        } else {
            if(isPMDA && !e2bCompIdAndRptCategory.pmdaIdnt) formattedDate = pmdaFileDate
            else formattedDate = dateFormat.format(fileDate)
        }
        senderIdentifier = senderIdentifier.replaceAll(" ", "_")
        return "${prefix}"+"I-"+"$senderIdentifier"+"-"+"$formattedDate$lastThreeDgtOfExTmpltQryId$suffix"
    }

    String getNuprCsvFileName(ExecutedPeriodicReportConfiguration executedPeriodicReportConfigurationInstance, ExecutedTemplateQuery executedTemplateQuery){
        String nuprSuffix = ''
        String primaryReportingDestination = executedPeriodicReportConfigurationInstance?.primaryReportingDestination
        UnitConfiguration reportingdestination = UnitConfiguration.findByUnitName(primaryReportingDestination)
        String currentSenderIdentifier = reportingdestination?.organizationName ?: reportingdestination?.unitRegisteredId
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd")
        Date finalLastRunDate = executedPeriodicReportConfigurationInstance?.lastRunDate
        String generationDate = dateFormat.format(finalLastRunDate)
        String sequence = getNextFormattedSequence()
        if (isFixedTempltNuprCsv(executedTemplateQuery, "-CSV-1")) {
            nuprSuffix = "_1"
        } else if (isFixedTempltNuprCsv(executedTemplateQuery, "-CSV-2")) {
            nuprSuffix = "_2"
        } else if (isFixedTempltNuprCsv(executedTemplateQuery, "-CSV-3")) {
            nuprSuffix = "_3"
        }
        return currentSenderIdentifier + "-" + generationDate + "-" + sequence + nuprSuffix
    }

    Map getFileNameSuffix(Map params){
        IcsrCaseTracking icsrCaseTrackingInstance = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(params.exIcsrTemplateQueryId as Long, params.caseNumber, params.versionNumber as Long)
        }
        String e2bCompanyId = getE2bCompanyId(icsrCaseTrackingInstance)
        String rptCategory = params.generatePmdaPaperReport ? getE2bRptCategory(icsrCaseTrackingInstance) : null
        Map e2bCompIdAndRptCategory = [:]
        if (e2bCompanyId) {
            String pmdaIdnt = e2bCompanyId.substring(e2bCompanyId.indexOf('=') + 1, e2bCompanyId.length() - 1)
            e2bCompIdAndRptCategory['pmdaIdnt'] = pmdaIdnt.substring(pmdaIdnt.length() - 2)
        }
        if (rptCategory) {
            e2bCompIdAndRptCategory['rptCategory'] =  rptCategory.substring(rptCategory.indexOf('=') + 1, rptCategory.length() - 1)
        }
        return e2bCompIdAndRptCategory
    }

    String getE2bCompanyId(IcsrCaseTracking icsrCaseTrackingInstance){
        String query = "SELECT E2B_COMPANY_ID \n" +
                "FROM c_case_pmda_ww_identifier \n" +
                "WHERE case_id = :caseId \n" +
                "AND tenant_id = :tenantId \n" +
                "AND AUTH_ID = :authId \n" +
                "AND RPT_CATEGORY_ID = :reportCategoryId\n" +
                "AND version_num <= :versionNum\n" +
                "ORDER BY version_num DESC, unq_id DESC"

        Sql sql = new Sql(utilService.getReportConnection())
        String result = ""
        try {
            result = sql.firstRow(query, [caseId: icsrCaseTrackingInstance?.caseId, tenantId: icsrCaseTrackingInstance?.tenantId, authId: icsrCaseTrackingInstance?.authIdInt, reportCategoryId: icsrCaseTrackingInstance?.reportCategoryId, versionNum: icsrCaseTrackingInstance?.versionNumber])
        } finally {
            sql?.close()
        }
        return result
    }

    String getE2bRptCategory(IcsrCaseTracking icsrCaseTrackingInstance){
        String query = "SELECT E2B_R3 \n" +
                "FROM VW_REPORT_CATEGORY \n" +
                "WHERE CODE_ID = :codeId"

        Sql sql = new Sql(utilService.getReportConnection())
        String rptCategory = ""
        try {
            rptCategory = sql.firstRow(query, [codeId: icsrCaseTrackingInstance?.reportCategoryId])
        } finally {
            sql?.close()
        }
        return rptCategory
    }

    @Transactional(readOnly = true)
    String getNextFormattedSequence() {
        Sql sql = new Sql(utilService.getReportConnectionForPVR())
        try {
            def result = sql.firstRow("SELECT LPAD(FILE_NAME_SEQUENCE.NEXTVAL, 3, '0') AS SEQ FROM DUAL")
            return result?.SEQ
        } finally {
            sql.close()
        }
    }

    public String getReportName(Capa8D capaInstance, Map params) {
        String suffix = ""

        if (params.advancedOptions == "1") {
            //Suffix added to report name if choosing to Save As from Advanced Options
            suffix += Constants.DynamicReports.ADVANCED_OPTIONS_SUFFIX
        }
        String issueNumWithLocale = "ISSUE_"+ capaInstance.issueNumber
        return "$issueNumWithLocale$suffix"
    }

    public String getReportName(List<Capa8D> capaInstance, Map params) {
        String suffix = ""

        if (params.advancedOptions == "1") {
            //Suffix added to report name if choosing to Save As from Advanced Options
            suffix += Constants.DynamicReports.ADVANCED_OPTIONS_SUFFIX
        }
        String issueNumWithLocale = "ISSUECRITERIA"
        return "$issueNumWithLocale$suffix"
    }

    String getReportNameForWidget(List<ReportWidget> reportWidgetList, Map params) {
        String suffix = ""

        if (params.advancedOptions == "1") {
            //Suffix added to report name if choosing to Save As from Advanced Options
            suffix += Constants.DynamicReports.ADVANCED_OPTIONS_SUFFIX
        }
        String widgetReportName = "REPORT_WIDGET_"+ reportWidgetList.size()
        return "$widgetReportName$suffix"
    }

    public boolean isCached(File reportFile, Map params) {
        reportFile?.exists() && reportFile?.size() > 0 && (!params.advancedOptions || params?.advancedOptions == "0") && (!params.notEmptyOnly) && (!params.sectionsToExport)
    }

    @Transactional(readOnly = true)
    void deleteAllReportsCachedFile(ReportResult reportResult, boolean isInDraftMode) {
        try {
            String reportName = getReportName(reportResult, isInDraftMode, [:])
            deleteFilesBeginningWith("${reportName}_") // To delete both the locale files. Eg: R123_en.pdf, R123_ja.pdf
        } catch (Exception ex) {
            log.error("Error occured while deleting reports cached files : " + ex)
        }

    }

    @Transactional(readOnly = true)
    void deleteAllCaseSeriesCachedFile(ExecutedCaseSeries executedCaseSeries, Boolean refresh = false) {
        try {
            if (refresh) {
                deleteFilesBeginningWith("CS${executedCaseSeries.id}-open")
                deleteFilesBeginningWith("CS${executedCaseSeries.id}-removed")
            }
            deleteFilesBeginningWith("CS${executedCaseSeries.id}-")
        } catch (Exception ex) {
            log.error("Error occured while deleting Case Series-${executedCaseSeries.id} cached files : " + ex)
        }
    }

    @Transactional(readOnly = true)
    void deleteFilesBeginningWith(String filter) {
        log.info("Deleting cache files beginning with ${filter}")
        File dir = new File(getReportsDirectory())

        FilenameFilter beginsWith = new FilenameFilter() {
            public boolean accept(File directory, String filename) {
                return filename.startsWith(filter);
            }
        };

        List<File> files = dir.listFiles(beginsWith);
        files.each { file ->
            if (file?.exists() && !file.isDirectory()) {
                file.delete()
            }
        }
    }

    @Transactional(readOnly = true)
    void deleteAllExecutedCachedFile(ExecutedReportConfiguration executedReportConfiguration, boolean isInDraftMode) {
        try {
            String reportName = getReportName(executedReportConfiguration, isInDraftMode, [:])
            deleteFilesBeginningWith("${reportName}_") // To delete both the locale files. Eg: R123_en.pdf, R123_ja.pdf
        } catch (Exception ex) {
            log.error("Error occured while deleting reports cached files : " + ex)
        }

    }

    public int getSwapVirtualizerMaxSize() {
        grailsApplication.config.dynamicJasper.swapVirtualizerMaxSize
    }

    public int getBlockSize() {
        grailsApplication.config.dynamicJasper.swapFile.blockSize
    }

    public int getMinGrowCount() {
        grailsApplication.config.dynamicJasper.swapFile.minGrowCount
    }

    boolean isLargeReportResult(ExecutedReportConfiguration executedConfiguration, Boolean draftMode = false, Boolean hasPPTXFormat = false) {
        List<ExecutedTemplateQuery> templateQueries = executedConfiguration.fetchExecutedTemplateQueriesByCompletedStatus()
        boolean largeReport = false
        Long totalResultDataSize = 0
        Long totalRecords = 0
        templateQueries?.find {
            ReportResult reportResult = draftMode ? it.draftReportResult : it.reportResult
            ReportFormatEnum outputFormat = hasPPTXFormat ? ReportFormatEnum.PPTX : ReportFormatEnum.HTML
            if (isLargeReportResult(reportResult, outputFormat)) {
                largeReport = true
                return true
            }
            int ciomsRatio = grailsApplication.config.pvreports.show.max.jasper.cioms.ratio?:100
            Long resultDataSize = (reportResult?.resultDataSize()) ?: 0
            totalResultDataSize = (resultDataSize * (it.executedTemplate.ciomsI ? (ciomsRatio / 10) : 1)) + totalResultDataSize
            Long records = (reportResult?.reportRows) ?: 0
            totalRecords = (records * ((it.executedTemplate.ciomsI || it.executedTemplate.medWatch) ? ciomsRatio : 1)) + totalRecords
            return false

        }

        if (totalRecords > grailsApplication.config.pvreports.show.max.jasper.records) {
            largeReport = true
        }

        if (totalResultDataSize > grailsApplication.config.pvreports.show.max.jasper.bytes) {
            largeReport = true
        }


        return largeReport
    }

    boolean isLargeReportResult(ReportResult reportResult, ReportFormatEnum outputFormat = ReportFormatEnum.HTML) {
        boolean isLargeReport = false
        if(reportResult) {
            if(outputFormat == ReportFormatEnum.PPTX && reportResult.reportRows > grailsApplication.config.pvreports.pptx.max.records){
                isLargeReport = true
            }
            else if (reportResult.executedTemplateQuery?.executedTemplate?.ciomsI && reportResult.reportRows > grailsApplication.config.pvreports.show.max.jasper.cioms.cases ?: 0L) {
                isLargeReport = true
            } else if (reportResult.reportRows > grailsApplication.config.pvreports.show.max.jasper.records ) {
                isLargeReport = true
            } else if (reportResult.resultDataSize() > grailsApplication.config.pvreports.show.max.jasper.bytes) {
                isLargeReport = true
            }
        }
        return isLargeReport
    }

    private setReportTheme(User user) {
        Templates.applyTheme(user?.preference?.theme)
    }

    Long topXRowsInReport(ReportTemplate executedTemplate) {
        Integer sum = 0
        if (executedTemplate instanceof ExecutedDataTabulationTemplate)
            executedTemplate?.columnMeasureList?.measures?.collect { List<DataTabulationMeasure> measureList ->
                measureList?.topXCount?.each { Integer topX ->
                    topX != null ? sum += topX : 0
                }
            }
        sum.toLong()
    }

    Long topXRowsInReport(ExecutedReportConfiguration executedConfigurationInstance) {
        Integer sum = 0
        if (executedConfigurationInstance) {
            executedConfigurationInstance.executedTemplateQueries?.collect { ExecutedTemplateQuery exTempQueries ->
                if (exTempQueries.executedTemplate instanceof ExecutedDataTabulationTemplate) {
                    exTempQueries.executedTemplate?.columnMeasureList?.measures?.collect { List<DataTabulationMeasure> measureList ->
                        measureList?.topXCount?.each { Integer topX ->
                            topX != null ? sum += topX : 0
                        }
                    }

                }
            }
        }
        sum.toLong()
    }

    String getTransmittedPdfFileName(ReportResult reportResult, String caseNumber) {
        return getReportFilename(getReportName(reportResult, false, [caseNumber: caseNumber]), ReportFormatEnum.PDF.name(), 'en_PDF')
    }

    String getTransmittedPdfFileName(CaseResultData caseResultData, String currentSenderIdentifier, String caseNumber, Long versionNumber , Long exIcsrTemplateQueryId, Date fileDate, Boolean isPMDA) {
        return getReportFilename(getReportName(caseResultData, currentSenderIdentifier, false, [caseNumber: caseNumber, versionNumber: versionNumber, exIcsrTemplateQueryId: exIcsrTemplateQueryId], fileDate, isPMDA), ReportFormatEnum.PDF.name(), 'en_PDF')
    }

    String getTransmittedR3XmlFileName(ReportResult reportResult, String caseNumber) {
        return getReportFilename(getReportName(reportResult, false, [caseNumber: caseNumber]), ReportFormatEnum.R3XML.name(), 'en')
    }

    String getTransmittedR3XmlFileName(CaseResultData caseResultData, String currentSenderIdentifier, String caseNumber, Long versionNumber , Long exIcsrTemplateQueryId, Date fileDate, Boolean isPMDA) {
        return getReportFilename(getReportName(caseResultData, currentSenderIdentifier, false, [caseNumber: caseNumber, versionNumber: versionNumber, exIcsrTemplateQueryId: exIcsrTemplateQueryId], fileDate, isPMDA), ReportFormatEnum.R3XML.name())
    }

    private InputStream getJrxmlStreamForPdf(String key) {
        String path = grailsApplication.config.getProperty('pv.app.e2b.jrxmls.pdf.options.' + key) ?: (grailsApplication.config.getProperty('pv.app.e2b.jrxmls.pdf.options.DEFAULT') ?: '')
        if (!path) {
            throw new RuntimeException("jrxml file path config is missing for ${key}")
        }
        return icsrXmlService.getStreamOfPath(path)
    }

    private InputStream getJrxmlStreamForHtml(String key) {
        String path = grailsApplication.config.getProperty('pv.app.e2b.jrxmls.html.options.' + key) ?: (grailsApplication.config.getProperty('pv.app.e2b.jrxmls.html.options.DEFAULT') ?: '')
        if (!path) {
            throw new RuntimeException("jrxml file path config is missing for ${key}")
        }
        return icsrXmlService.getStreamOfPath(path)
    }

    String dateFormatter (String inputDate, String reportLang = 'en') {
        if (inputDate && inputDate.matches("[\\d+-]+")) {
            return DateUtil.decodedViewDateFormat(inputDate, reportLang)
        }
        return inputDate
    }

    boolean isFixedTempltNuprCsv (ExecutedTemplateQuery executedTemplateQuery, String word) {
        return (executedTemplateQuery.usedTemplate.useFixedTemplate && executedTemplateQuery.usedTemplate.fixedTemplate) ? (executedTemplateQuery.usedTemplate.fixedTemplate.name.trim().contains(word) ? true : false) : false
    }

}
