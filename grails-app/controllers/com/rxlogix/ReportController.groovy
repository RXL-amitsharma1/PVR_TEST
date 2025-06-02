package com.rxlogix

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import com.rxlogix.config.*
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherExecutedTemplate
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.customException.NoDataFoundXmlException
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dynamicReports.reportTypes.CaseDetailReportBuilder
import com.rxlogix.enums.*
import com.rxlogix.file.MultipartFileSender
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.async.Promises
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.Sql
import liquibase.util.csv.CSVReader
import net.sf.dynamicreports.report.exception.DRException
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRRuntimeException
import net.sf.jasperreports.governors.GovernorException
import net.sf.jasperreports.governors.MaxPagesGovernorException
import net.sf.jasperreports.governors.TimeoutGovernorException
import org.apache.commons.io.FilenameUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONArray
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.http.HttpMethod
import org.springframework.security.access.annotation.Secured
import org.springframework.web.context.request.RequestContextHolder

import javax.mail.AuthenticationFailedException
import java.text.SimpleDateFormat
import java.util.zip.GZIPInputStream

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class ReportController {
    GrailsApplication grailsApplication
    def reportService
    DynamicReportService dynamicReportService
    def userService
    def emailService
    def dmsService
    def reportExecutorService
    def CRUDService
    def notificationService
    def caseSeriesService
    def templateService
    def sqlGenerationService
    def commentService
    ConfigurationService configurationService
    def dataSource_pva
    def publisherSourceService
    def publisherService
    def icsrProfileAckService

    static allowedMethods = [delete: ['DELETE','POST']]

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        render(view: "index", model: [related: "home", workFlowStates: WorkflowState.allWorkFlowStatesForAdhoc])
    }

    def icsr() {
        render(view: "/icsr/index", model: [related: "home", workFlowStates: WorkflowState.allWorkFlowStatesForAdhoc])
    }

    def showIcsrReport(){
        render "ICSR report with ID-${params.id}"
    }

    def image() {
        def file = new File(dynamicReportService.getReportsDirectory() + params.reportName + File.separator + params.image)
        render(file: file, contentType: 'image/png')
    }

    def criteria(Long id) {
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.get(id)
        if (!executedReportConfiguration) {
            notFound()
            return
        }

        if (!executedReportConfiguration.isViewableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(controller: 'executionStatus', action: "list")
            return
        }

        User currentUser = userService.getUser()
        Boolean viewBasicSql = params.getBoolean("viewBasicSql")
        Boolean viewAdvanceSql = params.getBoolean("viewAdvanceSql")
        Boolean isFinal =  executedReportConfiguration.hasGeneratedCasesData && !params.getBoolean("isInDraftMode") &&
        executedReportConfiguration.status in [ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT, ReportExecutionStatusEnum.SUBMITTED]
        if (!executedReportConfiguration?.isViewableBy(currentUser)) {
            flash.warn = message(code: "app.userPermission.message", args: [executedReportConfiguration.reportName, message(code: "app.label.report")])
            redirect(view: "index")
        } else {
            def includeAllStudyDrugsCases
            if(executedReportConfiguration.includeAllStudyDrugsCases){
                includeAllStudyDrugsCases = executedReportConfiguration.studyDrugs
            }
            notificationService.deleteExecutedReportNotification(currentUser, executedReportConfiguration,
                    (executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration ? NotificationApp.AGGREGATE_REPORT : NotificationApp.ADHOC_REPORT))
            if (executedReportConfiguration.instanceOf(ExecutedPeriodicReportConfiguration)) {
                render(view: "periodicReportCriteria",
                        model: [executedConfigurationInstance       : executedReportConfiguration,
                                executedTemplateQueriesForProcessing: executedReportConfiguration.getExecutedTemplateQueriesForProcessing(),
                                comments                            : executedReportConfiguration.comments,
                                reportType                          : ReportTypeEnum.REPORT,
                                includeAllStudyDrugsCases           : includeAllStudyDrugsCases,
                                viewSql                             : (viewBasicSql || viewAdvanceSql) ? reportExecutorService.debugExecutedReportSQL(executedReportConfiguration, viewBasicSql ?: viewAdvanceSql) : null,
                                etlRunTime                          : Holders.config.getProperty('safety.source') == Constants.PVCM ? Constants.NOT_APPLICABLE : DateUtil.getFormattedDateForLastSuccessfulEtlRun(userService.getCurrentUser()?.preference?.timeZone, isFinal ? executedReportConfiguration?.finalExecutedEtlDate : executedReportConfiguration?.executedETLDate),
                                isFinal                             : isFinal])
            } else {
                render(view: "criteria",
                        model: [executedConfigurationInstance: executedReportConfiguration,
                                comments                     : executedReportConfiguration.comments,
                                reportType                   : ReportTypeEnum.REPORT,
                                includeAllStudyDrugsCases    : includeAllStudyDrugsCases,
                                executedAsOfVersionDate      : executedReportConfiguration.getExecutedAsOfVersionDate(),
                                viewSql                      : (viewBasicSql || viewAdvanceSql) ? reportExecutorService.debugExecutedReportSQL(executedReportConfiguration, viewBasicSql ?: viewAdvanceSql) : null,
                                etlRunTime                   : Holders.config.getProperty('safety.source') == Constants.PVCM ? Constants.NOT_APPLICABLE : DateUtil.getFormattedDateForLastSuccessfulEtlRun(userService.getCurrentUser()?.preference?.timeZone, executedReportConfiguration?.executedETLDate)])
            }
        }
    }

    def addTemplateSection(Long id) {
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.get(id)
        String submitUrl = (executedReportConfiguration instanceof ExecutedConfiguration ?createLink(controller: "configuration",action: "saveOnDemandSection"):createLink(controller: "periodicReport",action: "saveOnDemandSection"))
        if (!executedReportConfiguration) {
            log.error("Requested Entity not found : ${id}")
            render "Not Found"
            return
        }

        User currentUser = userService.getUser()
        if(!executedReportConfiguration.isEditableBy(currentUser)){
            log.error("Requested Entity no permission : ${id}")
            render "No permission issue"
            return
        }
        List<ExecutedTemplateQuery> executedTemplateQueries = []
        executedReportConfiguration.executedTemplateQueries?.each {
            if(it.manuallyAdded && it.onDemandSectionParams){
                Map dataMap = MiscUtil.parseJsonText(it.onDemandSectionParams)
                if(dataMap != null && dataMap.rowId == params.int("rowId") && dataMap.columnName.equals(params.columnName)) {
                    executedTemplateQueries.add(it)
                }
            }
        }
        executedReportConfiguration.executedTemplateQueries.clear()
        executedReportConfiguration.executedTemplateQueries.addAll(executedTemplateQueries)
        ExecutedTemplateQuery parentExecutedTemplate  = null
        if(params.boolean('isInDraftMode')) {
            parentExecutedTemplate = params.("reportResultId") ? ExecutedTemplateQuery.findByDraftReportResult(ReportResult.read(params.long('reportResultId'))) : null
        }else{
            parentExecutedTemplate = params.("reportResultId") ? ExecutedTemplateQuery.findByFinalReportResult(ReportResult.read(params.long('reportResultId'))) : null
        }
           render template: "/advancedReportViewer/includes/addTemplateSectionForm", model: [submitUrl:submitUrl, executedConfiguration: executedReportConfiguration, parentExecutedTemplate: parentExecutedTemplate, rowId: params.long("rowId"), columnName: params.columnName, count: params.long("count"), reportResultId: params.long("reportResultId")]
    }

    def showFirstSection(Long id) {
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.read(id)
        if (!executedReportConfiguration) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        if (!executedReportConfiguration?.isViewableBy(currentUser)) {
            flash.warn = message(code: "app.userPermission.message", args: [executedReportConfiguration.reportName, message(code: "app.label.report")])
            redirect(view: "index")
        } else {
            notificationService.deleteExecutedReportNotification(currentUser, executedReportConfiguration,
                    (executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration ? NotificationApp.AGGREGATE_REPORT : NotificationApp.ADHOC_REPORT))
            if (executedReportConfiguration.status == ReportExecutionStatusEnum.GENERATED_CASES) {
                redirect(controller: "caseList", action: "index", params: [id: executedReportConfiguration.id])
            } else if (executedReportConfiguration instanceof ExecutedIcsrReportConfiguration) {
                redirect(controller: 'icsrReport', action: 'showResult', id: id)
            } else if (executedReportConfiguration instanceof ExecutedIcsrProfileConfiguration) {
                redirect(controller: 'executedIcsrProfile', action: 'showResult', id: id)
            } else {
                ExecutedTemplateQuery firstExecutedTemplateQuery = executedReportConfiguration.executedTemplateQueries.find{it.isVisible()}
                ReportResult reportResult = (params.boolean('isInDraftMode') ? firstExecutedTemplateQuery.draftReportResult : firstExecutedTemplateQuery.reportResult)
                params.id = reportResult ? reportResult.id : ''
                redirect(action: "show", params: params)
            }
        }
    }

    def showCaseXml(Long exIcsrTemplateQueryId, Boolean isInDraftMode) {
        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
        ExecutedReportConfiguration executedReportConfiguration = (ExecutedReportConfiguration) executedTemplateQuery?.usedConfiguration
        if (!executedReportConfiguration) {
            notFound()
            return
        }

        User currentUser = userService.getUser()

        if (!executedReportConfiguration?.isViewableBy(currentUser)) {
            flash.warn = message(code: "app.userPermission.message", args: [executedReportConfiguration.reportName, message(code: "app.label.report")])
            redirect(view: "index")
        } else {
            ReportResult reportResult = executedTemplateQuery.reportResult
            params.id = reportResult ? reportResult.id : ''
            show(reportResult)
        }
    }

    def showXml(Long id, Boolean isInDraftMode){
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.get(id)
        if (!executedReportConfiguration) {
            notFound()
            return
        }
        User currentUser = userService.getUser()

        if (!executedReportConfiguration?.isViewableBy(currentUser)) {
            flash.warn = message(code: "app.userPermission.message", args: [executedReportConfiguration.reportName, message(code: "app.label.report")])
            redirect(view: "index")
        } else {
            notificationService.deleteExecutedReportNotification(currentUser, executedReportConfiguration,
                    (executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration ? NotificationApp.AGGREGATE_REPORT : NotificationApp.ADHOC_REPORT))
            ExecutedTemplateQuery firstExecutedTemplateQuery = executedReportConfiguration.executedTemplateQueries.first()
            ReportResult reportResult = firstExecutedTemplateQuery.reportResult
            params.id = reportResult ? reportResult.id : ''
            params.outputFormat = "XML"
            show(reportResult)
        }
    }

    def downloadXml(Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber, String outputFormat, Boolean isInDraftMode) {
        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
        if (!executedTemplateQuery || !caseNumber) {
            notFound()
            return
        }
        ExecutedReportConfiguration executedReportConfiguration = (ExecutedReportConfiguration) executedTemplateQuery?.usedConfiguration
        if (!executedReportConfiguration) {
            notFound()
            return
        }

        User currentUser = userService.getUser()
        def configuration = ReportConfiguration.findByReportNameAndOwner(executedReportConfiguration.reportName, executedReportConfiguration.owner)
        Boolean isViewableByCurrentUser = configuration?.instanceOf(IcsrProfileConfiguration) ? configuration?.isViewableBy(currentUser) : executedReportConfiguration?.isViewableBy(currentUser)

        if (!isViewableByCurrentUser) {
            flash.warn = message(code: "app.userPermission.message", args: [executedReportConfiguration.reportName, message(code: "app.label.report")])
            redirect(controller: "icsr", action: "showReport", params: [exIcsrTemplateQueryId: params.exIcsrTemplateQueryId, caseNumber: params.caseNumber, versionNumber: params.versionNumber, isInDraftMode: params.isInDraftMode])
            return
        }
        IcsrCaseTracking icsrCaseTrackingInstance = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        }
        Date fileDate = icsrCaseTrackingInstance?.generationDate
        if(fileDate == null) fileDate = new Date()
        Boolean isJapanProfile = icsrCaseTrackingInstance?.isJapanProfile()
        File reportFile
        String reportFileName = ''
        if (outputFormat == 'R3XML') {
            reportFile = dynamicReportService.createR3XMLReport(executedTemplateQuery, isInDraftMode?:false, params, fileDate, isJapanProfile)
            reportFileName = FilenameUtils.removeExtension(reportFile.name)
            outputFormat = 'XML'
        } else {
            reportFile = dynamicReportService.createXMLReport(executedTemplateQuery, isInDraftMode?:false, params, fileDate, isJapanProfile)
            reportFileName = FilenameUtils.removeExtension(reportFile.name)
        }
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        webRequest.setRenderView(false)
        boolean inline = false
        try {
            MultipartFileSender.renderFile(reportFile, reportFileName, outputFormat?.toLowerCase(), dynamicReportService.getContentType(outputFormat), request, response, inline)
        } catch (IOException ex) {
            flash.error = message(code: "default.server.error.message")
            log.warn("IOException occurred in downloadXml while rendering file ${reportFile.name}. Error: ${ex.getMessage()}")
            return;
        }
        AuditLogConfigUtil.logChanges(executedReportConfiguration, [outputFormat: outputFormat, fileName: reportFile.name?:reportFileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, " " + ViewHelper.getMessage("auditLog.entityValue.export", ReportFormatEnum.(ReportFormatEnum.valueOf(outputFormat)).displayName))
    }

    def downloadPdf(Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber, String outputFormat, Boolean isInDraftMode) {
        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
        if (!executedTemplateQuery || !caseNumber) {
            notFound()
            return
        }
        ExecutedReportConfiguration executedReportConfiguration = (ExecutedReportConfiguration) executedTemplateQuery?.usedConfiguration
        if (!executedReportConfiguration) {
            notFound()
            return
        }

        User currentUser = userService.getUser()
        def configuration = ReportConfiguration.findByReportNameAndOwner(executedReportConfiguration.reportName, executedReportConfiguration.owner)
        Boolean isViewableByCurrentUser = configuration?.instanceOf(IcsrProfileConfiguration) ? configuration?.isViewableBy(currentUser) : executedReportConfiguration?.isViewableBy(currentUser)

        if (!isViewableByCurrentUser) {
            flash.warn = message(code: "app.userPermission.message", args: [executedReportConfiguration.reportName, message(code: "app.label.report")])
            redirect(controller: "icsr", action: "showReport", params: [exIcsrTemplateQueryId: params.exIcsrTemplateQueryId, caseNumber: params.caseNumber, versionNumber: params.versionNumber, isInDraftMode: params.isInDraftMode, reportLang: params.reportLang])
            return
        }
        String reportLocale = (userService.currentUser?.preference?.locale ?: executedReportConfiguration.owner.preference.locale).toString()
        IcsrCaseTracking newIcsrCaseTrackingInstance = null
        IcsrCaseTracking.withNewSession {
            newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
        }
        Date fileDate = newIcsrCaseTrackingInstance?.generationDate
        if(fileDate == null) fileDate = new Date()
        Boolean isJapanProfile = newIcsrCaseTrackingInstance?.isJapanProfile()
        params.reportLang = params.reportLang ?: reportLocale
        File reportFile = dynamicReportService.createPDFReport(executedTemplateQuery, isInDraftMode?:false, params, fileDate, isJapanProfile)
        String reportFileName = FilenameUtils.removeExtension(reportFile.name)
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        webRequest.setRenderView(false)
        boolean inline = false
        try {
            MultipartFileSender.renderFile(reportFile, reportFileName, outputFormat?.toLowerCase(), dynamicReportService.getContentType(outputFormat), request, response, inline)
        }
        catch (IOException ex) {
            flash.error = message(code: "default.server.error.message")
            log.warn("IOException occurred in downloadPdf while rendering file ${reportFile.name}. Error: ${ex.getMessage()}")
            return;
        }
        AuditLogConfigUtil.logChanges(executedReportConfiguration, [outputFormat: outputFormat, fileName: reportFile.name?:reportFileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, " " + ViewHelper.getMessage("auditLog.entityValue.export", ReportFormatEnum.(ReportFormatEnum.valueOf(outputFormat)).displayName))
    }

    def show(ReportResult reportResult) {
        log.info("reportResult " + reportResult)

        if(params.filter){
            params.filter = templateService.removeSubtotalFilter(JSON.parse(params.filter))
        }

        if (!reportResult) {
            notFound()
            return
        }
        String warningMsg=""
        boolean createR3XML = false
        if(params.outputFormat == "R3XML"){
            createR3XML = true
        }

        ReportFormatEnum outputFormat = !params.outputFormat ? ReportFormatEnum.HTML : ReportFormatEnum.valueOf(params.outputFormat)
        ReportTemplate executedTemplate = GrailsHibernateUtil.unwrapIfProxy(reportResult.template?:reportResult.executedTemplateQuery.executedTemplate)
        boolean hasRemovedCases = false
        boolean generateHTML = true
        boolean isRenderSuccessful = true

        boolean generateReport = !executedTemplate?.isNotExportable(outputFormat)
        Long topXRows = dynamicReportService.topXRowsInReport(executedTemplate)
        File reportFile = null
        boolean isInteractiveOutput = params.showInteractive == null ? executedTemplate.getInteractiveOutput() : params.boolean("showInteractive")
        Boolean isInDraftMode = params.getBoolean("isInDraftMode") ||
                (reportResult.executedTemplateQuery.executedConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT)
        ExecutedReportConfiguration executedConfiguration = GrailsHibernateUtil.unwrapIfProxy(reportResult.executedTemplateQuery?.executedConfiguration)
        if(executedConfiguration instanceof ExecutedPeriodicReportConfiguration && (reportResult.executedTemplateQuery.executedConfiguration.status in [ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT, ReportExecutionStatusEnum.SUBMITTED] && reportResult.executedTemplateQuery.executedConfiguration.finalLastRunDate == null)){
            isInDraftMode = true
        }

        //Don't create HTML report if it will be too large; back out and force them to Save As to PDF/Excel/Word
        if (generateReport && !isInteractiveOutput && !params.dynamic && dynamicReportService.isLargeReportResult(reportResult, outputFormat)) {
            warningMsg = message(code: "app.report.maxJasperDataBytes")
            generateReport = false
        } else if (generateReport && !isInteractiveOutput && checkIfReportExceedsHtmlLimit(topXRows ?: reportResult.reportRows) && outputFormat == ReportFormatEnum.HTML) {
            warningMsg= message(code: "app.report.maxReportRows", args: [getReportHtmlLimitFromConfig()])
            generateHTML = false
        } else if (executedTemplate.ciomsI && outputFormat == ReportFormatEnum.HTML) {
            warningMsg = message(code: "app.report.CIOMS1.htmlNotSupported")
            generateHTML = false
        } else if (executedTemplate.medWatch && outputFormat == ReportFormatEnum.HTML) {
            warningMsg = message(code: "app.report.MedWatch.htmlNotSupported")
            generateHTML = false
        } else if (executedTemplate.instanceOf(ExecutedTemplateSet) && outputFormat == ReportFormatEnum.HTML) {
            warningMsg = message(code: "app.report.templateSet.htmlNotSupported")
            generateHTML = false
        }

        if (isInteractiveOutput && (outputFormat == ReportFormatEnum.HTML)) generateHTML = false
        if (reportResult.executedTemplateQuery.executedConfiguration.id instanceof Long)
            notificationService.deleteNotification(reportResult.executedTemplateQuery.executedConfiguration.id, NotificationApp.PVC_REPORT)
        if (isInteractiveOutput && (outputFormat == ReportFormatEnum.HTML)) generateHTML = false
        User currentUser = userService.getUser()

        String currentSenderIdentifier = ""
        Date fileDate = null
        Boolean isJapanProfile = false
        String reportName = ""
        if(executedConfiguration instanceof ExecutedIcsrProfileConfiguration) {
            currentSenderIdentifier = executedConfiguration.getSenderIdentifier()
            IcsrCaseTracking icsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(params.exIcsrTemplateQueryId as Long, params.caseNumber, params.versionNumber as Long)
            }
            fileDate = icsrCaseTrackingInstance?.generationDate ?: new Date()
            isJapanProfile = icsrCaseTrackingInstance?.isJapanProfile()
            reportName = dynamicReportService.getReportName(reportResult, currentSenderIdentifier, isInDraftMode, params, fileDate, isJapanProfile)
        } else {
            reportName = dynamicReportService.getReportName(reportResult, isInDraftMode, params)
        }


        // Reports not view able by a user will not be delivered to the inbox
        if (!reportResult?.isViewableBy(currentUser)) {
            warningMsg = message(code: "app.userPermission.message", args: [reportName, message(code: "app.label.report")])
            render(view: "index", model: [executedConfigurationInstance: reportResult.executedTemplateQuery.executedConfiguration,
                                          executedTemplateInstance     : executedTemplate,warningMsg:warningMsg])
            return
        }
        ExecutedReportConfiguration executedReport = reportResult.executedTemplateQuery.executedConfiguration
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_DATE_TIME_FORMAT)
        String correctFileName = ""
        if(executedConfiguration instanceof ExecutedIcsrProfileConfiguration) {
            correctFileName = dynamicReportService.getReportName(reportResult, currentSenderIdentifier, isInDraftMode, params, fileDate, isJapanProfile)
        } else if (params.outputFormat == ReportFormatEnum.CSV.name()) {
            ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(reportResult.executedTemplateQuery?.id)
            if (executedReport instanceof ExecutedPeriodicReportConfiguration) {
                ExecutedPeriodicReportConfiguration executedPeriodicReportConfigurationInstance = (ExecutedPeriodicReportConfiguration) executedReport
                if (dynamicReportService.isFixedTempltNuprCsv(executedTemplateQuery, ReportFormatEnum.CSV.name()) && params.outputFormat == ReportFormatEnum.CSV.name()) {
                    correctFileName = dynamicReportService.getNuprCsvFileName(executedPeriodicReportConfigurationInstance, executedTemplateQuery)
                }
            }
            correctFileName = (correctFileName=="") ? reportName : correctFileName
        } else {
            correctFileName = dynamicReportService.getReportNameAsFileName(executedReport, reportResult.executedTemplateQuery)
        }
        def (fileName, format) = FileUtil.getCorrectFileNameAndFormat(correctFileName, params.outputFormat.toString())
        try {
            if (generateHTML && generateReport) {

                TemplateTypeEnum executeTemplateType = executedTemplate.templateType
                if (executeTemplateType == TemplateTypeEnum.DATA_TAB) {
                    reportFile = dynamicReportService.createReportWithCriteriaSheet(reportResult, isInDraftMode, params)
                } else if (executeTemplateType == TemplateTypeEnum.ICSR_XML) {

                    if (!createR3XML && !params.paperReport) {
                        reportFile = dynamicReportService.createXMLReport(reportResult.executedTemplateQuery, isInDraftMode, params, fileDate, isJapanProfile)
                    } else if (params.paperReport) {
                        reportFile = dynamicReportService.createPDFReport(reportResult.executedTemplateQuery, isInDraftMode, params, fileDate, isJapanProfile)
                    } else {
                        reportFile = dynamicReportService.createR3XMLReport(reportResult.executedTemplateQuery, isInDraftMode, params, fileDate, isJapanProfile)
                    }

                    log.info("reportFile " + reportFile.toPath())
                } else {
                    if (params.async) {
                        Map m = [:]
                        m.putAll(params)
                        runReportGenerationAsync(m, isInDraftMode, reportResult.id, userService.currentUser.id, fileName)
                        reportFile = new File("/")
                    } else {
                        reportFile = dynamicReportService.createReportWithCriteriaSheetCSV(reportResult, isInDraftMode, params)
                    }
                }

            }
            if (!generateReport || !params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) {
                boolean isLargeReport = dynamicReportService.isLargeReportResult(reportResult, outputFormat)
                if (isLargeReport) {
                    warningMsg = message(code: "app.report.maxJasperDataBytes")
                }else{
                    if (generateHTML && generateReport) {
                        TemplateTypeEnum executeTemplateType = executedTemplate.templateType
                        if (executeTemplateType == TemplateTypeEnum.ICSR_XML) {
                            dynamicReportService.createHTMLReport(reportResult.executedTemplateQuery, isInDraftMode, params, fileDate, isJapanProfile)
                        }
                    }
                }

            // Durty hack: executed configuration and origin configuration are not linked
            ExecutedReportConfiguration executedReportConfiguration = reportResult.executedTemplateQuery.executedConfiguration
            ExecutedCaseSeries executedCaseSeries = executedReportConfiguration.caseSeries
            String reportLocale = userService.currentUser?.preference?.locale ?: executedReportConfiguration.owner.preference.locale
            if(executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration && executedCaseSeries){
                List<CaseDTO> casesList = reportExecutorService.getRemovedCaseOfSeries(executedCaseSeries.id, executedCaseSeries.caseSeriesOwner)
                hasRemovedCases = casesList?.size() ? true : false
            }
            String latestComment = commentService.getReportResultChartAnnotation(reportResult.getId())
            Long ciomsITemplateId = executedReportConfiguration.executedTemplateQueries.find{it.executedTemplate.ciomsI == true }?.id

            ExecutionStatus executionStatus =  ExecutionStatus.findByExecutedEntityId(executedReportConfiguration.id)
            def configurationInstance = ReportConfiguration.read(executionStatus?.entityId)
            String configType = configurationInstance ? configurationInstance.getConfigType() : null
            boolean isNuprCsv = false
            if (params.outputFormat != ReportFormatEnum.CSV.name()) {
                reportName = dynamicReportService.getReportNameWithLocale(reportName, reportLocale)
            }

            if (executedReportConfiguration.instanceOf(ExecutedPeriodicReportConfiguration)) {
                isNuprCsv = dynamicReportService.isFixedTempltNuprCsv(reportResult.executedTemplateQuery, ReportFormatEnum.CSV.name())
            }
            if(!configType){
                log.warn("Config type not found for ${configurationInstance?.id}, ${configurationInstance?.getClass()} and EX : ${executedReportConfiguration.id} ")
            }

            log.debug("Configuration Id : ${configurationInstance?.id} | Configuration Type : ${configType}")

            Map templateQueryIndex = [:] //@PVC TODO check why need?
            boolean isPeriodicReport = executedReportConfiguration.instanceOf(ExecutedPeriodicReportConfiguration)
            boolean showNuprCaseNumCheckbox = isPeriodicReport && (executedReportConfiguration.periodicReportType == PeriodicReportTypeEnum.NUPR) && (dynamicReportService.isFixedTempltNuprCsv(reportResult.executedTemplateQuery, "PSR FORM 7-2"))

            render(view: "show", model: [reportResult: reportResult, reportType: ReportTypeEnum.REPORT, hasRemovedCases: hasRemovedCases,
                                         isLargeReportResult: isLargeReport, comments: reportResult.comments,
                                         reportName: reportName, executedConfigurationComments:executedReportConfiguration.comments,
                                         isPeriodicReport: isPeriodicReport, configurationInstance: configurationInstance,
                                         sourceProfiles: SourceProfile.sourceProfilesForUser(currentUser), hasConfigTemplateCreatorRole: currentUser.isConfigurationTemplateCreator(),
                                         configSelectedTimeZone: params.configSelectedTimeZone, templateQueryIndex: templateQueryIndex, configType: configType, latestComment: latestComment,
                                         ciomsITemplateId: ciomsITemplateId,warningMsg: warningMsg, isNuprCsv: isNuprCsv, includeCaseNumber: !(params.includeCaseNumber == 'false'), showNuprCaseNumCheckbox: showNuprCaseNumCheckbox])
            } else {
                if (params.async) {
                    render "ok"
                    return
                } else if (generateReport) {
                    isRenderSuccessful = renderReportOutputType(reportFile, reportResult.executedTemplateQuery.executedConfiguration, correctFileName, reportResult, false)
                    if (!isRenderSuccessful) {
                        log.warn("Rendering of Configuration ${reportResult.executedTemplateQuery.executedConfiguration?.id} is not successful.")
                    }
                }
            }
            if (params.outputFormat != null && isRenderSuccessful) {
                correctFileName = fileName
                params.outputFormat = ReportFormatEnum.valueOf(format)
                if(reportFile != null){
                    AuditLogConfigUtil.logChanges(executedReport, [outputFormat: params.outputFormat, fileName: correctFileName, exportedDate: new Date()],
                            [:], Constants.AUDIT_LOG_EXPORT, " " + ViewHelper.getMessage("auditLog.entityValue.export", ReportFormatEnum.(params.outputFormat).displayName))
                }
            }
        } catch (GovernorException ge) {
            log.error(ge.message)
            if (ge instanceof MaxPagesGovernorException) {
                flash.error = message(code: "app.report.maxPages", args: [ge.maxPages])
                redirect(action: 'show', params: [id: reportResult.id])
            } else if (ge instanceof TimeoutGovernorException) {
                flash.error = message(code: "app.report.timeout", args: [ge.timeout])
                redirect(action: 'show', params: [id: reportResult.id])
            }
        } catch (DRException e) {
            log.error(e.message)
            if (e.cause instanceof MaxPagesGovernorException) {
                flash.error = message(code: "app.report.maxPages", args: [e.cause.maxPages])
                redirect(action: 'show', params: [id: reportResult.id])
            } else if (e.cause instanceof TimeoutGovernorException) {
                flash.error = message(code: "app.report.timeout", args: [e.cause.timeout])
                redirect(action: 'show', params: [id: reportResult.id])
            } else if (e.getMessage().contains("components reaches outside available width")) {
                flash.error = message(code: "app.report.maxWidth")
                redirect(action: 'show', params: [id: reportResult.id])
            } else if (e.getMessage().contains("components reaches outside available height")) {
                flash.error = message(code: "app.report.maxHeight")
                redirect(action: 'show', params: [id: reportResult.id])
            } else if (e.cause instanceof JRException && e.getMessage().contains("Infinite loop creating new page")) {
                flash.error = message(code: "app.report.maxColumns")
                redirect(action: 'show', params: [id: reportResult.id])
            } else {
                log.error("Unexpected error", e)
                flash.error = message(code: "default.server.error.message")
                redirect(action: 'show', params: [id: reportResult.id])
            }
        } catch (JRRuntimeException e) {
            log.error(e.message, e)
            flash.error = message(code: "app.report.maxColumns")
            redirect(action: 'show', params: [id: reportResult.id])
        } catch(NoDataFoundXmlException ndfe){
            log.error(ndfe.message)
            flash.error = message(code: "app.report.xml.no.data")
            if(!generateHTML){
                redirect(action: 'show', params: [id: reportResult.id])
                return
            }
            redirect(controller: 'executionStatus', action: 'list') //TODO need to add better handling redirect.
        } catch(Throwable e){
            log.error("Unexpected error", e)
            flash.error = message(code: "default.server.error.message")
            redirect(action: 'show', params: [id: reportResult.id])
        }

    }

    void runReportGenerationAsync(Map par, boolean isInDraftMode, Long reportResultId, Long userId, String fileName) {
        Promises.task {

            Configuration.withNewSession {
                ReportResult reportResult = ReportResult.get(reportResultId)
                User user = User.get(userId)
                File reportFile = dynamicReportService.createReportWithCriteriaSheetCSV(reportResult, isInDraftMode, par)

                Notification notification = new Notification(user: user,
                        level: NotificationLevelEnum.INFO,
                        message: "app.notification.export",
                        messageArgs: fileName,
                        appName: NotificationApp.EXPORT,
                        executionStatusId: 0,
                        notificationParameters: """{"sourceFileName":"${reportFile.getName()}","userFileName":"${fileName}"}"""
                )
                notification.save(flush: true)
                userService.pushNotificationToBrowser(notification, user)
            }

        }.onError { Throwable err ->
            Configuration.withNewSession {
                User user = User.get(userId)
                Notification notification = new Notification(user: user,
                        level: NotificationLevelEnum.ERROR,
                        message: "app.notification.error",
                        messageArgs: err.getMessage(),
                        appName: NotificationApp.ERROR,
                        executionStatusId: 0
                )
                notification.save(flush: true)
                userService.pushNotificationToBrowser(notification, user)
                log.error("Error occurred preparing export file!", err)
                err.printStackTrace()
            }

        }
    }

    @Secured(["ROLE_CONFIGURATION_VIEW","ROLE_PERIODIC_CONFIGURATION_VIEW", "ROLE_CASE_SERIES_VIEW"])
    def share() {
        ExecutedCaseSeries caseSeries
        ExecutedCaseSeries cumulativeCaseSeries

        if (params.executedConfigId) {
            ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.get(params.executedConfigId)
            User currentUser = userService.getUser()
            if (!executedConfiguration?.isViewableBy(currentUser)) {
                String reportName = executedConfiguration.reportName
                flash.warn = message(code: "app.userPermission.message", args: [reportName, message(code: "app.label.report")])
                redirect(url: request.getHeader('referer'))
                return
            }
            if (executedConfiguration) {

                caseSeries = executedConfiguration.caseSeries
                cumulativeCaseSeries = executedConfiguration.cumulativeCaseSeries
                if (caseSeries) caseSeriesService.shareExecutedCaseSeries(params, caseSeries)
                if (cumulativeCaseSeries) caseSeriesService.shareExecutedCaseSeries(params, cumulativeCaseSeries)

                List<User> usersOldList = new ArrayList<User>()
                usersOldList.addAll(executedConfiguration.executedDeliveryOption.sharedWith)
                List<UserGroup>  groupOldList = new ArrayList<UserGroup>()
                groupOldList.addAll(executedConfiguration.executedDeliveryOption.sharedWithGroup)

                ExecutedDeliveryOption executedDeliveryOption = executedConfiguration.executedDeliveryOption

                def allowedUsers=userService.getAllowedSharedWithUsersForCurrentUser();
                def allowedGroups=userService.getAllowedSharedWithGroupsForCurrentUser();

                Set<User> newUsers=[]
                Set<UserGroup> newGroups=[]
                params.sharedWith?.split(";")?.each { String shared ->
                    if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                        UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                        if (userGroup && allowedGroups.find{it.id==userGroup.id} && !executedDeliveryOption.sharedWithGroup.find {it.id==userGroup.id}) {
                            executedDeliveryOption.addToSharedWithGroup(userGroup)
                            newGroups<<userGroup
                        }
                    } else if (shared.startsWith(Constants.USER_TOKEN)) {
                        User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                        if (user && allowedUsers.find{it.id==user.id} && !executedDeliveryOption.sharedWith.find {it.id==user.id}) {
                            executedDeliveryOption.addToSharedWith(user)
                            newUsers<<user
                        }
                    }
                }

                if((!caseSeries && !cumulativeCaseSeries) || executedConfiguration)
                    CRUDService.saveWithoutAuditLog(executedConfiguration)
                reportService.updateAuditLogShareWith(executedDeliveryOption, usersOldList, groupOldList, executedConfiguration, false)
//              Send notifications for shared report.
                sendShareNotification(newUsers, newGroups,executedConfiguration)

            } else {
                // no such result
            }
        } else {
            // no valid id
        }
        flash.message = message(code: 'app.configuration.shared.successful')
        def referer = request.getHeader('referer')
        referer = referer ? URLDecoder.decode(referer, "UTF-8") : ""
        if (referer.contains("report/index") && referer.contains("forPvq=true")) {
            render(view: "index", model: [related: params.relatedReports])
        }else{
            redirect(url: referer)   //redirect to the page from where the request comes (to periodicReport/reports).
        }
    }

    private void sendShareNotification(Set<User> newUsers, Set<UserGroup> newGroups, ExecutedReportConfiguration executedConfiguration) {
        Set<String> recipients = newUsers*.email as Set
        newGroups.each {
            recipients.addAll(it.getUsers()*.email)
        }
        if(recipients) {
            String timeZone = userService.currentUser?.preference?.timeZone
            String emailSubject = g.message(code: 'app.notification.report.email.shared')
            String url = request.getRequestURL().substring(0, request.getRequestURL().indexOf("/", 8)) + "/reports/report/showFirstSection/" + executedConfiguration.id
            def content = g.render(template: '/mail/report/report',
                    model: ['executedConfiguration': executedConfiguration, 'url': url, 'userTimeZone': timeZone])
            emailService.sendNotificationEmail(recipients, content, true, emailSubject);
        }
    }

//@TODO needs UI to collect report output formats & user emails
    @Secured(["ROLE_CONFIGURATION_VIEW", "ROLE_PERIODIC_CONFIGURATION_VIEW", "ROLE_CASE_SERIES_VIEW"])
    def email() {

        if (params.executedConfigId) {
            ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.get(params.executedConfigId)
            if (executedConfiguration) {

                List<ReportFormatEnum> formats = []
                if (params.attachmentFormats instanceof String) {
                    formats.add(ReportFormatEnum.valueOf(params.attachmentFormats))
                } else {
                    formats = params.attachmentFormats.collect { ReportFormatEnum.valueOf(it) }
                }

                Boolean hasPPTXFormat = formats.any{it == ReportFormatEnum.PPTX}
                //Validate if report result is large or not.
                if (dynamicReportService.isLargeReportResult(executedConfiguration,false, hasPPTXFormat)) {
                    flash.warn = message(code: "app.report.maxJasperDataBytes.export")
                    render(view: "index", model: [related: "home"])
                    return
                }

                List<String> emailList = []
                if (params.emailToUsers instanceof String) {
                    emailList.add(params.emailToUsers)
                } else {
                    emailList = params.emailToUsers
                }

                bindEmailConfiguration(executedConfiguration, params.emailConfiguration)
                try {
                    emailService.emailReportTo(executedConfiguration, emailList, formats)
                }
                catch (Exception e){
                    flash.error = emailService.emailExceptionToMessage(e)
                    log.error("${e.message}. So we are not able to mail this file.")
                }

            } else {
                // no such result
            }
        } else {
            // no valid id
        }
        redirect(url: request.getHeader('referer'))   //redirect to the page from where the request comes: to dashboard, to report/index page or periodicReport/reports.
    }
    def emailPvp() {

        if (params.executedConfigId) {
            ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.get(params.executedConfigId)

            if (executedConfiguration) {

                List<ReportFormatEnum> formats = []
                if (params.attachmentFormats instanceof String) {
                    formats.add(ReportFormatEnum.valueOf(params.attachmentFormats))
                } else {
                    formats = params.attachmentFormats.collect { ReportFormatEnum.valueOf(it) }
                }

                Boolean hasPPTXFormat = formats.any{it == ReportFormatEnum.PPTX}
                //Validate if report result is large or not.
                if (dynamicReportService.isLargeReportResult(executedConfiguration,false, hasPPTXFormat)) {
                    flash.warn = message(code: "app.report.maxJasperDataBytes.export")
                    render(view: "index", model: [related: "home"])
                    return
                }

                List<String> emailList = []
                if (params.emailToUsers instanceof String) {
                    emailList.add(params.emailToUsers)
                } else {
                    emailList = params.emailToUsers
                }

                bindEmailConfiguration(executedConfiguration, params.emailConfiguration)
                List publisherAttachments = getPublisherAttachments(executedConfiguration)
                CRUDService.update(executedConfiguration)
                try {
                    emailService.sendReport(executedConfiguration, emailList?.toArray(new String[0]), formats?.toArray(new ReportFormatEnum[0]), false, publisherAttachments)
                } catch (Exception e) {
                    flash.error = emailService.emailExceptionToMessage(e)
                    log.error("${e.message}. So we are not able to mail this file.")
                }
            }
        }
        redirect(url: request.getHeader('referer'))   //redirect to the page from where the request comes: to dashboard, to report/index page or periodicReport/reports.
    }
    @Secured(["ROLE_DMS"])
    def sendToDms() {

        if (params.executedConfigId) {
            ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.get(params.executedConfigId)
            if (executedConfiguration) {

                try{
                    if (params.dmsConfiguration.format) {
                        bindDmsConfiguration(executedConfiguration, params.dmsConfiguration,true)
                        dmsService.uploadReport(executedConfiguration,params.dmsConfiguration.format,params.list('sectionsToExport'))
                        flash.message = message(code: 'app.dms.successfully.upload', args: [ executedConfiguration.reportName])
                    }
                }

                catch(Exception e) {
                    log.error("Unexpected error in sendToDms", e)
                    flash.error = message(code:"app.dms.failure.upload")
                }

            }
        }
        redirect(url: request.getHeader('referer'))   //redirect to the page from where the request comes: to dashboard, to report/index page or periodicReport/reports.
    }

    def exportReportFromInbox() {

        /*
         * This is a convenience method and only needed since we're boxed in at the GSP level.  The Javascript that drives the URL to Export is hardcoded to a single URL.
         * Trying to vary it based on examination of the ExecutedConfiguration is difficult in Javascript. Removing the Javascript is only possible by
         * removing the coupling to DataTables.  The need to vary the URL is driven by the need to distinguish between multi-template reports and single template reports.
         */
        ExecutedConfiguration executedConfigurationInstance = params.id ? ExecutedConfiguration.get(params.id) : null

        //Validate if report result is large or not.
        if (dynamicReportService.isLargeReportResult(executedConfigurationInstance)) {
            flash.warn = message(code: "app.report.maxJasperDataBytes.export")
            render(view: "index", model: [related: "home"])
            return
        }

        List<ExecutedTemplateQuery> executedTemplateQueries = executedConfigurationInstance?.fetchExecutedTemplateQueriesByCompletedStatus()

        if(executedTemplateQueries) {
            if (executedTemplateQueries.size() > 1) {
                viewMultiTemplateReport(executedConfigurationInstance.id)
            } else {
                ReportResult reportResult = executedTemplateQueries.first().reportResult
                show(reportResult)
            }
        } else {
            flash.warn = message(code: "app.warn.completed.template.queries.not.found")
            render(view: "index", model: [related: "home"])
            return
        }
    }

    /**
     * Method to check if report result has records larger than expected.
     * @param reportResult
     * @return
     */

    def viewMultiTemplateReport(Long id) {
        ExecutedReportConfiguration executedConfigurationInstance = ExecutedReportConfiguration.read(id)
        List<ExecutedTemplateQuery> executedTemplateQueries = []
        List<Long> executedTemplateQueryIds = []

        if (!executedConfigurationInstance) {
            notFound()
            return
        }

        ReportFormatEnum outputFormat = !params.outputFormat ? ReportFormatEnum.HTML : ReportFormatEnum.valueOf(params.outputFormat)

        boolean generateHTML = true

        if(params.sectionsToExport) {
            executedTemplateQueryIds = params.sectionsToExport instanceof String ? [params.sectionsToExport as Long] : params.sectionsToExport.collect { it as Long }
            executedTemplateQueryIds.each {it ->
                executedTemplateQueries.add(ExecutedTemplateQuery.get(it))
            }
        } else {
            executedTemplateQueries.addAll(executedConfigurationInstance.executedTemplateQueries)
        }

        boolean generateReport = !executedTemplateQueries?.find {
            it.executedTemplate?.isNotExportable(outputFormat)
        }

        Boolean hasPPTXFormat = (outputFormat == ReportFormatEnum.PPTX)
        Long topXRows = dynamicReportService.topXRowsInReport(executedConfigurationInstance)

        //Don't create HTML report if it will be too large; back out and force them to Save As to PDF/Excel/Word
        if (generateReport && dynamicReportService.isLargeReportResult(executedConfigurationInstance, params.boolean("isInDraftMode"), hasPPTXFormat)) {
            flash.warn = message(code: "app.report.maxJasperDataBytes")
            generateReport = false
        } else if (generateReport && (outputFormat == ReportFormatEnum.HTML) && checkIfReportExceedsHtmlLimit(countTotalRowsInReport(topXRows, executedConfigurationInstance, params.boolean("isInDraftMode")))) {
            flash.warn = message(code: "app.report.maxReportRows", args: [getReportHtmlLimitFromConfig()])
            generateHTML = false
        } else if (executedConfigurationInstance.fetchExecutedTemplateQueriesByCompletedStatus().any {it.executedTemplate.ciomsI} && outputFormat == ReportFormatEnum.HTML) {
            flash.warn = message(code: "app.report.CIOMS1.htmlNotSupported")
            generateHTML = false
        } else if(generateReport && (outputFormat == ReportFormatEnum.HTML) && executedTemplateQueries.any {it.executedTemplate.interactiveOutput}){
            flash.warn = message(code: "app.dynamic.drill.interactiveNotSupported")
        } else if(!generateReport){
            flash.warn = message(code: "app.report.templateSet.htmlNotSupported")
        }

        User currentUser = userService.getUser()
        String reportName = dynamicReportService.getReportName(executedConfigurationInstance, params.boolean("isInDraftMode") || executedConfigurationInstance.status == ReportExecutionStatusEnum.GENERATED_DRAFT, params)
        String reportLocale = userService.currentUser?.preference?.locale ?: executedConfigurationInstance.owner.preference.locale

        if (!executedConfigurationInstance?.isViewableBy(currentUser)) {
            flash.warn = message(code: "app.userPermission.message", args: [reportName, message(code: "app.label.report")])
            render(view: "index", model: [executedConfigurationInstance: executedConfigurationInstance])
            return
        }

        File reportFile = null
        boolean isRenderSuccessful = false
        boolean isNuprCsv = false
        boolean showNuprCaseNumCheckbox = false
        try {
            if (generateHTML && generateReport) {
                reportFile = dynamicReportService.createMultiTemplateReport(executedConfigurationInstance, params)
            }

            if (!generateReport || !params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) {
                boolean isLargeReport = dynamicReportService.isLargeReportResult(executedConfigurationInstance, params.boolean("isInDraftMode"), hasPPTXFormat)
                if (isLargeReport) {
                    flash.warn = message(code: "app.report.maxJasperDataBytes")
                }
                Long ciomsITemplateId = executedConfigurationInstance.executedTemplateQueries.find{it.executedTemplate.ciomsI == true }?.id

                if (executedConfigurationInstance.instanceOf(ExecutedPeriodicReportConfiguration)){
                    isNuprCsv = true
                    executedConfigurationInstance.executedTemplateQueries.each {
                        if(isNuprCsv && !dynamicReportService.isFixedTempltNuprCsv(it, ReportFormatEnum.CSV.name())){
                            isNuprCsv = false
                        }
                        if(!showNuprCaseNumCheckbox && dynamicReportService.isFixedTempltNuprCsv(it, "PSR FORM 7-2")){
                            showNuprCaseNumCheckbox = true
                        }
                    }
                }
                isNuprCsv = !executedConfigurationInstance.executedTemplateQueries.isEmpty() && isNuprCsv

                ExecutionStatus executionStatus =  ExecutionStatus.findByExecutedEntityId(executedConfigurationInstance.id)
                def configurationInstance = ReportConfiguration.read(executionStatus?.entityId)
                String configType = configurationInstance ? configurationInstance.getConfigType() : null

                if(!configType){
                    log.warn("View multiple config type not found for ${configurationInstance?.id}, ${configurationInstance?.getClass()} and EX : ${executedConfigurationInstance.id} ")
                }

                log.debug("View multiple configuration Id : ${configurationInstance?.id} | Configuration Type : ${configType}")


                render(view: "viewMultiTemplateReport",
                        model: [executedConfigurationInstance: executedConfigurationInstance,configurationInstance : configurationInstance, configType: configType,sourceProfiles: SourceProfile.sourceProfilesForUser(currentUser),
                                reportType                   : ReportTypeEnum.MULTI_REPORT, reportName: dynamicReportService.getReportNameWithLocale(reportName, reportLocale), isPeriodicReport: executedConfigurationInstance.instanceOf(ExecutedPeriodicReportConfiguration),
                                isLargeReportResult          : isLargeReport, executedConfigurationComments: executedConfigurationInstance.comments, ciomsITemplateId: ciomsITemplateId, isNuprCsv: isNuprCsv, includeCaseNumber: !(params.includeCaseNumber == 'false'), showNuprCaseNumCheckbox : showNuprCaseNumCheckbox])
            } else if (generateReport) {
                isRenderSuccessful = renderReportOutputType(reportFile, executedConfigurationInstance)
                if (!isRenderSuccessful) {
                    log.warn("Rendering of Configuration ${executedConfigurationInstance?.id} is not successful.")
                }
            }
            if(params.outputFormat != null && isRenderSuccessful) {
                ExecutedReportConfiguration executedReport = ExecutedReportConfiguration.findById(params.id)
                if (executedReport == null)
                    executedReport = ReportResult.findById(params.id).executedTemplateQuery.executedConfiguration
                SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_DATE_TIME_FORMAT)
                String correctFileName = dynamicReportService.getReportNameAsFileName(executedReport)
                def (fileName, format) = FileUtil.getCorrectFileNameAndFormat(correctFileName, params.outputFormat.toString())
                correctFileName = fileName
                params.outputFormat = ReportFormatEnum.valueOf(format)
                AuditLogConfigUtil.logChanges(executedReport, [outputFormat: params.outputFormat, fileName: correctFileName, exportedDate: new Date()],
                        [:], Constants.AUDIT_LOG_EXPORT, " " + ViewHelper.getMessage("auditLog.entityValue.export", ReportFormatEnum.(params.outputFormat).displayName))
            }
        }catch (GovernorException ge) {
            log.error(ge.message)
            if (ge instanceof MaxPagesGovernorException) {
                flash.error = message(code: "app.report.maxPages", args: [ge.maxPages])
                redirect(action: 'viewMultiTemplateReport', params: [id: id])
            } else if (ge instanceof TimeoutGovernorException) {
                flash.error = message(code: "app.report.timeout", args: [ge.timeout])
                redirect(action: 'viewMultiTemplateReport', params: [id:id])
            }
        }catch (DRException e) {
            if (e.cause instanceof MaxPagesGovernorException) {
                flash.error = message(code: "app.report.maxPages", args: [e.cause.maxPages])
                redirect(action: 'viewMultiTemplateReport', params: [id: id])
            } else if (e.getMessage().contains("components reaches outside available width")) {
                flash.error = message(code: "app.report.maxWidth")
                redirect(action: 'viewMultiTemplateReport', params: [id: id])
            } else if (e.getMessage().contains("components reaches outside available height")) {
                flash.error = message(code: "app.report.maxHeight")
                redirect(action: 'viewMultiTemplateReport', params: [id: id])
            } else if (e.cause instanceof JRException && e.getMessage().contains("Infinite loop creating new page")) {
                flash.error = message(code: "app.report.maxColumns")
                redirect(action: 'viewMultiTemplateReport', params: [id: id])
            } else {
                log.error("Unexpected error", e)
                flash.error = message(code: "default.server.error.message")
                redirect(action: 'viewMultiTemplateReport', params: [id: id])
            }
        } catch (JRRuntimeException e) {
            flash.error = message(code: "app.report.maxColumns")
            redirect(action: 'viewMultiTemplateReport', params: [id: id])
        } catch (Throwable e) {
            log.error("Unexpected error", e)
            flash.error = message(code: "default.server.error.message")
            redirect(action: 'viewMultiTemplateReport', params: [id: id])
        }
    }

    def updateStatus() {
        def report = ExecutedReportConfiguration.get(params.id)
        if (report) {
            reportService.changeReportResultStatus(params.reportStatus, userService.getUser(), report)
        }
        redirect(action: "index")
    }

    def delete() {
        def report = ExecutedReportConfiguration.get(params.id)
        User currentUser=userService.currentUser
        if (report) {
            if (report instanceof ExecutedPeriodicReportConfiguration && report.status == ReportExecutionStatusEnum.SUBMITTED && !currentUser.isAdmin()) {
                flash.error =  message(code: 'default.report.delete.not.rights')
            } else {
                if (params.boolean("deleteForAll") && (currentUser.isAdmin() || report.owner == currentUser)) {
                    CRUDService.softDelete(report, report.reportName, params.deleteJustification)
                } else {
                    userService.removeUserFromDeliveryOptionSharedWith(currentUser, report.executedDeliveryOption, report.owner.id)
                    CRUDService.softDeleteForUser(currentUser, report, report.reportName, params.deleteJustification)
                }
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.generatedReport', default: 'Generated Report'), report.reportName])}"
            }
        }
        else {
            flash.error =  message(code: 'default.report.not.exist')
        }
        redirect(url: request.getHeader('referer'))
    }

    def archive() {
        def report = ExecutedReportConfiguration.get(params.id)
        User user = userService.getUser()
        boolean isArchived = false
        if (report) {
            isArchived = reportService.toggleIsArchived(user, report)
        }

        AuditLogConfigUtil.logChanges(report, [archived: "${isArchived ? 'Archived' : 'Not archived'} for user ${user.fullName}"], [archived: "${!isArchived ? 'Archived' : 'Not archived'} for user ${user.fullName}"], Constants.AUDIT_LOG_UPDATE)

        redirect(url: request.getHeader('referer'))
    }

    def favorite() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        ExecutedReportConfiguration report = params.id ? ExecutedReportConfiguration.get(params.id) : null
        if (!report) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.label.report'), params.id]) as String)
        } else {
            try {
                reportService.setFavorite(report, params.boolean("state"))
            } catch (Exception e) {
                responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    /**
     * Get the Jasper Report that was previously exported to HTML and stream it out to a GSP via Ajax call
     * @param filename
     * @return String
     */
    def getReportAsHTMLString(String filename, Long id) {
        ResponseDTO responseDTO = showReport(id)
        String html = ''
        if (responseDTO.status) {
            File file = new File(dynamicReportService.getReportsDirectory() + "${filename}.html")
            Long delayInLoad = grailsApplication.config.getProperty('pvreports.show.html.delay', Long)
            if (delayInLoad) {
                Thread.sleep(delayInLoad)
            }
            if (file.exists()) {
                if(file.size() < grailsApplication.config.getProperty('pvreports.show.max.file.html',Long)){
                    //Show the file inline in the browser
                    html = file.text
                } else {
                    html = message(code: 'pvreports.show.max.file.html.size.exceed.error')
                }
            } else {
                log.warn("Report html file not found for ${file.absolutePath}")
                html = message(code: 'pvreports.file.html.not.found.error')
            }
        }
        render(html)
    }


    /**
     * Send the output type (PDF/Excel/Word) to the browser which will save it to the user's local file system
     * @param reportFile
     * @return boolean : file rendered successfully
     */
    protected boolean renderReportOutputType(File reportFile, ExecutedReportConfiguration executedConfiguration, String correctFileName = null, ReportResult reportResult = null, boolean inline = false) {
        boolean isRenderSuccessful = true
        if (!reportFile) {
            flash.message = message(code: "app.report.file.not.found")
            redirect(controller: "report", action: "index")
            return
        }
        String reportFileName = ""
        if(correctFileName == null)
            reportFileName = dynamicReportService.getReportNameAsFileName(executedConfiguration, reportResult?.executedTemplateQuery)
        else
            reportFileName = correctFileName

        if (params.outputFormat == ReportFormatEnum.R3XML.name()) {
            params.outputFormat = ReportFormatEnum.XML.name()
        } else if (params.outputFormat == ReportFormatEnum.XML.name()) {
            reportFileName = reportFileName + Constants.ADD_SIMPLE_FOR_R2
        }
        try {
            String outputFormat = params.outputFormat as String
            GrailsWebRequest webRequest =
                    (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
            webRequest.setRenderView(false)
            MultipartFileSender.renderFile(reportFile, reportFileName, outputFormat?.toLowerCase(), dynamicReportService.getContentType(params.outputFormat), request, response, inline)
        } catch (Exception ex) {
            isRenderSuccessful = false
            flash.error = message(code: "default.server.error.message")
            log.debug("IOException occurred in renderReportOutputType while rendering file ${reportFile.name}. Error: ${ex.getMessage()}")
        } finally {
            // Delete Cached Report in Case of MedWatch or Cioms
            Boolean isMedWatchOrCiomsITemplate = reportResult?.executedTemplateQuery?.executedTemplate?.isMedWatchTemplate() || reportResult?.executedTemplateQuery?.executedTemplate?.isCiomsITemplate()
            if (reportFile?.exists() && isMedWatchOrCiomsITemplate) {
                reportFile.delete()
            }
        }
        return isRenderSuccessful
    }

    protected void notFound() {
        if(!request.withFormat) {
            flash.error = message(code: 'dashboard.widget.report.not.yet.executed.message')
            redirect action: "index"
        }else{
            request.withFormat {
                form {
                    flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.report'), params.id])
                    redirect action: "index", method: "GET"
                }
                '*' { render status: NOT_FOUND }
            }
        }
    }

    def copyCaseNumbersResult(ReportResult reportResult) {
        Set caseNumberSet = []
        ObjectMapper mapper = new ObjectMapper()
        JsonFactory factory = mapper.getJsonFactory()
        caseNumberSet = getAllReportFields(factory, reportResult)
        respond caseNumberSet.sort(), [formats: ['json']]
    }

    def advancedOptionsExport(Long id) {
        if (params.actionToExecute == 'viewMultiTemplateReport') {
            viewMultiTemplateReport(id)
        }  else {
            show(ReportResult.get(id))
        }
    }

    @Secured('permitAll')
    def drillDown() {
        String caseNumber = params.caseNumber
        Integer versionNumber = params.int('versionNumber')
        String srcCaseId = null
        Long exIcsrTemplateQueryId = params.long('exIcsrTemplateQueryId')
        Long processReportId = params.long('processReportId')
        String prodHashCode = params.prodHashCode
        boolean fromIcsr = params.boolean('fromIcsr')
        if (Holders.config.getProperty('casedata.drill.down.uri')) {
            if (fromIcsr) {
                if (!sqlGenerationService.isCaseNumberExistsForTenantInPVCM(caseNumber, versionNumber)) {
                    response.sendError(403, "You are not authorized to view or data doesn't exist")
                    return
                }
            } else {
                if (!sqlGenerationService.isCaseNumberExistsForTenant(caseNumber, versionNumber)) {
                    response.sendError(403, "You are not authorized to view or data doesn't exist")
                    return
                }
            }
            // Remove notification
            notificationService.deleteNotificationByNotificationParameters(userService.getCurrentUser(), NotificationApp.COMMENTS, caseNumber, versionNumber, exIcsrTemplateQueryId, true)

            ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.get(exIcsrTemplateQueryId)
            if(executedTemplateQuery) {
                if(executedTemplateQuery.usedTemplate?.isCiomsITemplate() || executedTemplateQuery.usedTemplate?.isMedWatchTemplate()) {
                    redirect(action: 'exportSingleCIOMS', params: [caseNumber: caseNumber, versionNumber: versionNumber, query: params.query, exIcsrTemplateQueryId: exIcsrTemplateQueryId, processReportId: processReportId, prodHashCode: prodHashCode, fromIcsr: fromIcsr])
                    return
                }
            }

            String url = Holders.config.getProperty('casedata.drill.down.uri')
            String drillDownTarget = Holders.config.getProperty('casedata.drill.down.target')
            String urlParameter = Holders.config.getProperty('casedata.drill.down.parameter')
            if (url.contains("<<CASE_NUMBER>>")) {
                url = url.replaceAll("<<VERSION_NUMBER>>", versionNumber.toString())
                url = url.replaceAll("<<CASE_NUMBER>>", caseNumber)

            } else if (url.contains("<<CASE_ID>>") && drillDownTarget == Constants.PVCM) {
                Sql sql = new Sql(dataSource_pva)
                try {
                    String caseRecordSql = "SELECT tenant_id, SRC_CASE_ID FROM V_SAFETY_IDENTIFICATION WHERE case_num=? AND version_num=" + versionNumber
                    def caseInfo = sql.firstRow(caseRecordSql, [caseNumber])
                    if (caseInfo) {
                        srcCaseId = caseInfo.SRC_CASE_ID
                        url = url.replaceAll("<<CASE_ID>>", srcCaseId)
                        url = url + urlParameter
                    } else {
                        throw new Exception("Can not find SRC_CASE_ID againt Case Number ${caseNumber} and Version Number ${versionNumber}")
                    }
                } catch (Exception e) {
                    log.error("Some Error Occured while fetching Soruce Case Id for Case Number: ${caseNumber} and Version Number : ${versionNumber} .")
                    log.error(e.printStackTrace())
                    response.sendError(500, message(code: "Error Fetching Source Case Id for Case Number: ${caseNumber} and Version Number : ${versionNumber}").toString())
                    return
                } finally {
                    sql?.close()
                }
            }
            if (url.startsWith('http')) {
                redirect(url: url)
            } else {
                redirect(uri: url)
            }
            return
        }
        redirect(action: 'exportSingleCIOMS', params: [caseNumber: caseNumber, versionNumber: versionNumber, query: params.query, exIcsrTemplateQueryId: exIcsrTemplateQueryId, processReportId: processReportId, prodHashCode: prodHashCode, fromIcsr: fromIcsr, isInDraftMode: params.isInDraftMode])
    }

    def exportSingleCIOMS(Boolean isInDraftMode) {
        String caseNumber = params.caseNumber
        Integer versionNumber = params.int('versionNumber')
        Long exIcsrTemplateQueryId = params.long('exIcsrTemplateQueryId')
        Long processReportId = params.long('processReportId')
        String prodHashCode = params.prodHashCode
        boolean fromIcsr = params.boolean('fromIcsr')
        if (fromIcsr) {
            if (!sqlGenerationService.isCaseNumberExistsForTenantInPVCM(caseNumber, versionNumber)) {
                response.sendError(403, "You are not authorized to view or data doesn't exist")
                return
            }
        } else {
            if (!sqlGenerationService.isCaseNumberExistsForTenant(caseNumber, versionNumber)) {
                response.sendError(403, "You are not authorized to view or data doesn't exist")
                return
            }
        }

        params.outputFormat = ReportFormatEnum.PDF.name()
        params.excludeCriteriaSheet = true
        params.excludeAppendix = true
        params.excludeComments = true
        params.excludeLegend = true

        CustomSQLTemplate ciomsTempate = CustomSQLTemplate.findByNameAndCiomsIAndOriginalTemplateIdAndIsDeleted(ReportTemplate.CIOMS_I_TEMPLATE_NAME, true,0L, false)
        SourceProfile sourceProfile = SourceProfile.findByIsCentral(true) // CIOMS report is currently executed for Argus data source only
        int blinded = params.blinded ? 1 : 0
        int protectPrivacy = params.privacy ? 1 : 0
        ExecutedTemplateQuery exTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
        // Create executed report instance on the fly
        ReportResult reportResult = new ReportResult() {
            def tempExecutedTemplateQuery;
            @Override
            String getName() {
                return "${caseNumber}_${versionNumber}_${blinded}_${protectPrivacy}"
            }
            @Override
            ExecutedTemplateQuery getExecutedTemplateQuery() {
                return tempExecutedTemplateQuery
            }

            def setExecutedTemplateQuery(ExecutedTemplateQuery executedTemplateQuery) {
                tempExecutedTemplateQuery=executedTemplateQuery
            }
        }

        // this is necessary as we have updated the requirements to only allow report results with COMPLETED status for show pages
        reportResult.executionStatus = ReportExecutionStatusEnum.COMPLETED

        reportResult.scheduledBy = userService.currentUser

        def executedConfiguration = null
        if (!exTemplateQuery) {
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
                    sourceProfile: sourceProfile, tenantId: Tenants.currentId() as Long
            )

            executedTemplateQuery.executedConfiguration = executedConfiguration
            reportResult.setExecutedTemplateQuery(executedTemplateQuery)
        } else {
            reportResult.setExecutedTemplateQuery(exTemplateQuery)
            executedConfiguration = exTemplateQuery.usedConfiguration
            blinded = exTemplateQuery.blindProtected ? 1: 0
            protectPrivacy = exTemplateQuery.privacyProtected ? 1 : 0
        }
        String reportLocale = userService.currentUser?.preference?.locale ?: executedConfiguration.owner?.preference.locale
        String reportName = dynamicReportService.getReportName(reportResult, params.isInDraftMode? params.isInDraftMode as Boolean : false, params)
        String reportFileName = dynamicReportService.getReportFilename(reportName, params.outputFormat, reportLocale)
        File reportFile = new File(dynamicReportService.getReportsDirectory() + reportFileName)
        if (reportFile?.exists() && !reportFile.isDirectory()) {
            reportFile.delete()
        }
        reportExecutorService.generateSingleCIOMSReport(reportResult, caseNumber, versionNumber, !!blinded, !!protectPrivacy, processReportId, prodHashCode)
        show(reportResult)
    }

    def exportMedWatch() {
        String caseNumber = params.caseNumber
        Integer versionNumber = params.int('versionNumber')
        Long exIcsrTemplateQueryId = params.long('exIcsrTemplateQueryId')
        Long processReportId = params.long('processReportId')
        String prodHashCode = params.prodHashCode
        boolean fromIcsr = params.boolean('fromIcsr')
        if (fromIcsr) {
            if (!sqlGenerationService.isCaseNumberExistsForTenantInPVCM(caseNumber, versionNumber)) {
                response.sendError(403, "You are not authorized to view or data doesn't exist")
                return
            }
        } else {
            if (!sqlGenerationService.isCaseNumberExistsForTenant(caseNumber, versionNumber)) {
                response.sendError(403, "You are not authorized to view or data doesn't exist")
                return
            }
        }
        // Remove Notification
        notificationService.deleteNotificationByNotificationParameters(userService.getCurrentUser(), NotificationApp.COMMENTS, caseNumber, versionNumber, exIcsrTemplateQueryId, true)

        params.outputFormat = ReportFormatEnum.PDF.name()
        params.excludeCriteriaSheet = true
        params.excludeAppendix = true
        params.excludeComments = true
        params.excludeLegend = true

        CustomSQLTemplate medWatchTempate = CustomSQLTemplate.findByNameAndMedWatchAndOriginalTemplateIdAndIsDeleted(ReportTemplate.MEDWATCH_TEMPLATE_NAME, true, 0L, false)
        SourceProfile sourceProfile = SourceProfile.findByIsCentral(true) // CIOMS report is currently executed for Argus data source only
        int blinded = params.blinded ? 1 : 0
        int protectPrivacy = params.privacy ? 1 : 0
        ExecutedTemplateQuery exTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
        // Create executed report instance on the fly
        ReportResult reportResult = new ReportResult() {
            def tempExecutedTemplateQuery;
            @Override
            String getName() {
                return "${caseNumber}_${versionNumber}_${blinded}_${protectPrivacy}"
            }
            @Override
            ExecutedTemplateQuery getExecutedTemplateQuery() {
                return tempExecutedTemplateQuery
            }

            def setExecutedTemplateQuery(ExecutedTemplateQuery executedTemplateQuery) {
                tempExecutedTemplateQuery=executedTemplateQuery
            }
        }

        // this is necessary as we have updated the requirements to only allow report results with COMPLETED status for show pages
        reportResult.executionStatus = ReportExecutionStatusEnum.COMPLETED

        reportResult.scheduledBy = userService.currentUser

        def executedConfiguration = null
        if (!exTemplateQuery) {
            def executedTemplateQuery = new ExecutedTemplateQuery(
                    executedTemplate: medWatchTempate,
                    reportResult: reportResult,
                    executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(dateRangeEnum: DateRangeEnum.CUMULATIVE)
            )
            executedConfiguration = new ExecutedConfiguration(
                    reportName: reportResult.name,
                    executedTemplateQueries: [executedTemplateQuery],
                    nextRunDate: new Date(),
                    lastRunDate: new Date(),
                    sourceProfile: sourceProfile, tenantId: Tenants.currentId() as Long
            )

            executedTemplateQuery.executedConfiguration = executedConfiguration
            reportResult.setExecutedTemplateQuery(executedTemplateQuery)
        } else {
            reportResult.setExecutedTemplateQuery(exTemplateQuery)
            executedConfiguration = exTemplateQuery.usedConfiguration
            blinded = exTemplateQuery.blindProtected ? 1: 0
            protectPrivacy = exTemplateQuery.privacyProtected ? 1 : 0
        }
        String reportLocale = userService.currentUser?.preference?.locale ?: executedConfiguration.owner?.preference.locale
        String reportName = dynamicReportService.getReportName(reportResult, params.isInDraftMode? params.isInDraftMode as Boolean : false, params)
        String reportFileName = dynamicReportService.getReportFilename(reportName, params.outputFormat, reportLocale)
        File reportFile = new File(dynamicReportService.getReportsDirectory() + reportFileName)
        if (reportFile?.exists() && !reportFile.isDirectory()) {
            reportFile.delete()
        }
        reportExecutorService.generateSingleCIOMSReport(reportResult, caseNumber, versionNumber, !!blinded, !!protectPrivacy, processReportId, prodHashCode)
        show(reportResult)
    }

    @Secured('permitAll')
    def exportCaseDetails(){
        String caseNumber = params.caseNumber
        Integer versionNumber = params.int('versionNumber')
        if (!sqlGenerationService.isCaseNumberExistsForTenant(caseNumber, versionNumber)) {
            response.sendError(403, "You are not authorized to view or data doesn't exist")
            return
        }
        params.outputFormat =  ReportFormatEnum.PDF.name()
        String reportName = "CN"+caseNumber

        List<Map> caseDetailList = []
        Map multiCaseInfo = sqlGenerationService.getCaseDetails(caseNumber, versionNumber)
        multiCaseInfo.put("CaseNumber",caseNumber)
        multiCaseInfo.put("Version",versionNumber)
        caseDetailList.add(multiCaseInfo)

        CaseDetailReportBuilder caseDetailReportBuilder = new CaseDetailReportBuilder()
        ByteArrayOutputStream caseDetailForm = caseDetailReportBuilder.createReport(caseDetailList, params.outputFormat)
        renderCaseDetailOutput(caseDetailForm, reportName, params.outputFormat)
    }

    protected renderCaseDetailOutput(ByteArrayOutputStream reportFile, String reportName, String outputFormat) {
        response.setContentType("application/${outputFormat}");
        response.setHeader("Content-Length", String.valueOf(reportFile.size()));
        response.addHeader("Content-Disposition", "attachment; filename=${reportName}.${outputFormat};");

        OutputStream responseOutputStream = response.getOutputStream();
        responseOutputStream.write(reportFile.toByteArray());
        responseOutputStream.close();
        reportFile.close();
    }

    private Set getAllReportFields(JsonFactory jsonFactory, ReportResult reportResult) {
        Set data = []
        if (reportResult?.data?.value) {
            if (reportResult.executedTemplateQuery.executedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                JsonParser parser = jsonFactory.createJsonParser(new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(reportResult?.data?.decryptedValue))))

                parser.nextToken() // JsonToken.Start_Array
                JsonToken token = null

                while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                    if (token == JsonToken.FIELD_NAME) {
                        String name = parser.getText()
                        parser.nextToken()
                        // masterCaseNum is a name of the column for Master Case Number
                        if (name =~ /^masterCaseNum.*/ && parser?.getText()) {
                            data.add(parser.getText())
                        }
                    }
                }
            } else if (reportResult.executedTemplateQuery.executedTemplate.templateType == TemplateTypeEnum.TEMPLATE_SET) {
                //TODO: Template set does not currently allow copy case #, but this may change in the future
            } else if (reportResult.executedTemplateQuery.executedTemplate.templateType == TemplateTypeEnum.CUSTOM_SQL) {
                int caseNumIndex = -1
                JSONArray columnNamesList = (JSONArray) JSON.parse(((ExecutedCustomSQLTemplate) reportResult.executedTemplateQuery.executedTemplate).columnNamesList)
                columnNamesList.eachWithIndex { String columnName, int i ->
                    // Add this check columnName == "MFR_CONTROL_NO_24B" to copy Case Number for CIOMS I Template.
                    if (columnName == "Case Number" || columnName == "MFR_CONTROL_NO_24B") {
                        caseNumIndex = i
                    }
                }

                if (caseNumIndex > -1) {
                    // Get only case numbers while parsing through each line
                    CSVReader reader = new CSVReader(new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(reportResult?.data?.decryptedValue)))))

                    try {
                        String[] nextLine
                        while ((nextLine = reader.readNext()) != null) {
                            data.add(nextLine[caseNumIndex])
                        }
                    } finally {
                        reader?.close()
                    }
                } else {
                    data.add(message(code: "app.template.parameterizedSQL.noCaseNumberColumn"))
                }
            } else {
                // Template Type: case line listing. Cannot copy case #'s for non-case templates
                // Get index of case number column
                int caseNumIndex = -1
                ((CaseLineListingTemplate) reportResult.executedTemplateQuery.executedTemplate).getAllSelectedFieldsInfo().eachWithIndex { ReportFieldInfo entry, int i ->
                    if (entry.argusName == "cm.CASE_NUM") {
                        caseNumIndex = i
                    }
                }

                if (caseNumIndex > -1) {
                    // Get only case numbers while parsing through each line
                    CSVReader reader = new CSVReader(new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(reportResult?.data?.decryptedValue)))))

                    try {
                        String[] nextLine
                        while ((nextLine = reader.readNext()) != null) {
                            data.add(nextLine[caseNumIndex])
                        }
                    } finally {
                        reader?.close()
                    }
                } else {
                    data.add(message(code: "app.template.caseLineListing.noCaseNumberColumn"))
                }
            }
        }
        return data
    }

    def copyCaseNumbersConfiguration(Long id) {
        ExecutedReportConfiguration exeConfig = ExecutedReportConfiguration.read(id)
        Set caseNumberSet = []
        ObjectMapper mapper = new ObjectMapper()
        JsonFactory factory = mapper.getJsonFactory()
        exeConfig?.fetchExecutedTemplateQueriesByCompletedStatus()?.collect {

            caseNumberSet.add(getAllReportFields(factory, it.reportResult))
        }

        respond caseNumberSet.flatten(), [formats: ['json']]
    }

    def addEmailConfiguration() {
        ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.get(params.id)
        if (!executedConfiguration) {
            log.error("Requested Entity not found : ${params.id}")
            render "Not Found"
            return
        }
        render contentType: "application/json", encoding: "UTF-8", text: (executedConfiguration.emailConfiguration ?: []) as JSON
    }

    @Secured(['ROLE_DMS'])
    def addDmsConfiguration() {
        ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.get(params.id)
        if (!executedConfiguration) {
            log.error("Requested Entity not found : ${params.id}")
            render "Not Found"
            return
        }
        render template: "/configuration/includes/dmsConfiguration", model: [configurationInstance: executedConfiguration,showSections:params.showSections]
    }

    def checkDeleteForAllAllowed() {
        ExecutedReportConfiguration executedConfiguration = ExecutedReportConfiguration.get(params.id)
        User currentUser = userService.currentUser
        render "" + (currentUser.isAdmin() || executedConfiguration.owner == currentUser)
    }

    private bindEmailConfiguration(ExecutedReportConfiguration configurationInstance, Map emailConfiguration) {
        if (emailConfiguration.subject && emailConfiguration.body) {
            EmailConfiguration emailConfigurationInstance
            if (configurationInstance.emailConfiguration) {
                emailConfigurationInstance = configurationInstance.emailConfiguration
                emailConfigurationInstance.isDeleted = false
                bindData(emailConfigurationInstance, emailConfiguration)
                CRUDService.update(emailConfigurationInstance)
            } else {
                emailConfigurationInstance = new EmailConfiguration(emailConfiguration)
                CRUDService.save(emailConfigurationInstance)
                configurationInstance.emailConfiguration = emailConfigurationInstance
            }
        } else {
            if (configurationInstance.emailConfigurationId) {
                CRUDService.softDelete(configurationInstance.emailConfiguration, configurationInstance.emailConfigurationId)
                configurationInstance.emailConfiguration = null
            }
        }
    }

    private ResponseDTO showReport(Long id) {
        Boolean isInDraftMode=params.boolean("isInDraftMode")
        ResponseDTO responseDTO = new ResponseDTO()
        ExecutedReportConfiguration executedConfigurationInstance = reportService.findExecutedReportConfigurationById(id)
        Long topXRows
        if (executedConfigurationInstance) {
            //For ICSR related templates no need to check.
            if(executedConfigurationInstance instanceof ExecutedIcsrReportConfiguration || executedConfigurationInstance instanceof ExecutedIcsrProfileConfiguration){
                return responseDTO
            }
            topXRows = dynamicReportService.topXRowsInReport(executedConfigurationInstance)
            //Don't create HTML report if it will be too large; back out and force them to Save As to PDF/Excel/Word
            responseDTO.status = !executedConfigurationInstance.executedTemplateQueries.find {
                it.executedTemplate?.isNotExportable(ReportFormatEnum.HTML)
            }
            if (responseDTO.status && (checkIfReportExceedsHtmlLimit(countTotalRowsInReport(topXRows, executedConfigurationInstance, params.boolean("isInDraftMode"))) || dynamicReportService.isLargeReportResult(executedConfigurationInstance, isInDraftMode))) {
                responseDTO.status = false
            }
        } else {
            ReportResult reportResult = ReportResult.get(id)
            if(!reportResult){
                responseDTO.status = false
                responseDTO.message = "Report result doesn't exist corresponding to ID"
                return responseDTO
            }
            ReportTemplate executedTemplate = GrailsHibernateUtil.unwrapIfProxy(reportResult.executedTemplateQuery.executedTemplate)
            topXRows = dynamicReportService.topXRowsInReport(executedTemplate)
            if (checkIfReportExceedsHtmlLimit(topXRows ?: reportResult.reportRows) || dynamicReportService.isLargeReportResult(reportResult)) {
                responseDTO.status = false
            } else {

                responseDTO.status = !executedTemplate?.isNotExportable(ReportFormatEnum.HTML)
            }
        }
        return responseDTO
    }

    private Long getTotalReportRows(ExecutedReportConfiguration executedReportConfiguration, Long topXRows, Boolean isInDraftMode = false) {
        Long reportRows = 0
        executedReportConfiguration.fetchExecutedTemplateQueriesByCompletedStatus().each { it ->
            if (isInDraftMode) {
                reportRows += it.draftReportResult?.reportRows ?: 0
            } else {
                if (topXRows == 0) {
                    reportRows += it.reportResult?.reportRows ?: 0
                } else {
                    if (it.executedTemplate instanceof ExecutedDataTabulationTemplate) {
                        it?.executedTemplate?.columnMeasureList?.measures?.collect { count ->
                            Boolean flag = false
                            count?.topXCount?.each { rows ->
                                if (rows != null && !flag)
                                    flag = true
                            }
                            reportRows += flag ? 0 : it.reportResult?.reportRows
                        }
                    } else {
                        reportRows += it.reportResult?.reportRows ?: 0
                    }
                }
            }
        }
        return reportRows
    }

    private boolean checkIfReportExceedsHtmlLimit(Long reportRows=0) {
        return (reportRows > getReportHtmlLimitFromConfig())
    }

    private Long getReportHtmlLimitFromConfig() {
        return grailsApplication.config.pvreports.show.max.html
    }

    private bindDmsConfiguration(ExecutedReportConfiguration configurationInstance, Map dmsConfiguration,Boolean isExcludeFormat = false) {
        if (dmsConfiguration.format) {
            DmsConfiguration dmsConfigurationInstance
            if (configurationInstance.dmsConfiguration) {
                dmsConfigurationInstance = configurationInstance.dmsConfiguration
                dmsConfigurationInstance.isDeleted = false
                isExcludeFormat ? bindData(dmsConfigurationInstance, dmsConfiguration,[exclude:["format"]]) : bindData(dmsConfigurationInstance, dmsConfiguration)
                CRUDService.update(dmsConfigurationInstance)
            } else {
                dmsConfigurationInstance = new DmsConfiguration(dmsConfiguration)
                CRUDService.save(dmsConfigurationInstance)
                configurationInstance.dmsConfiguration = dmsConfigurationInstance
            }
        } else {
            if (configurationInstance.dmsConfigurationId && !configurationInstance.dmsConfiguration.isDeleted) {
                CRUDService.softDelete(configurationInstance.dmsConfiguration, configurationInstance.dmsConfigurationId)
            }
        }
    }

    private Long countTotalRowsInReport(Long topXRows, ExecutedReportConfiguration executedConfigurationInstance, Boolean isInDraftMode) {
        topXRows += getTotalReportRows(executedConfigurationInstance, topXRows, isInDraftMode)
    }

    private void initConfigurationFromMap(Configuration configurationInstance, Map map) {
        params.putAll(map)
        configurationInstance.setIsEnabled(false)
        configurationInstance.nextRunDate = null
        configurationService.initConfigurationTemplatesFromSession(session, configurationInstance)
        configurationService.initConfigurationQueriesFromSession(session, configurationInstance)
        session.removeAttribute("editingConfiguration")
    }


    def downloadAttachment() {
        ExecutedPublisherSource attachment = ExecutedPublisherSource.get(params.long("id"))
        if (!attachment) {
            notFound()
            return
        }
        Map result = publisherSourceService.getDataMap(attachment)
        render(file: result.data, fileName: result.name, contentType: result.contntType)
    }

    private List getPublisherAttachments(ExecutedReportConfiguration executedReportConfiguration) {
        List result = []
        List<byte[]> sections = []
        if (executedReportConfiguration.isPublisherReport) {
            List ids = params.pvpSections?.split(",")?.findAll { it }?.collect { it as Long }
            if (ids) {
                PublisherConfigurationSection.findAllByIdInList(ids)?.each {
                    publisherService.pullTheLastSectionChanges(it)
                    PublisherExecutedTemplate publisherExecutedTemplate = it.getLastPublisherExecutedTemplates()
                    if (publisherExecutedTemplate) {
                        if (params.boolean("mergeSections")) {
                            sections << publisherExecutedTemplate.data
                        } else {
                            String name = it.name + ".docx";
                            result.add([type: dynamicReportService.getContentType(name),
                                        name: name,
                                        data: publisherExecutedTemplate.data
                            ])
                        }
                    }
                }
            }
            if (params.boolean("mergeSections")) {
                String name = executedReportConfiguration.reportName + ".docx";
                result.add([type: dynamicReportService.getContentType(name),
                            name: name,
                            data: publisherService.mergeDocx(sections)
                ])
            }
            ids = params.pvpFullDocuments?.split(",")?.findAll { it }?.collect { it as Long }
            if (ids) {
                PublisherReport.findAllByIdInList(ids)?.each {
                    publisherService.pullTheLastFullDocumentChanges(it)
                    if (it.data) {
                        String name = it.name + ".docx";
                        result.add([type: dynamicReportService.getContentType(name),
                                    name: name,
                                    data: it.data
                        ])
                    }
                }
            }
        }
        return result
    }

    def editConfig(){
        ReportResult reportResult = ReportResult.get(params.id)
        ExecutedReportConfiguration executedConfiguration =  reportResult ? reportResult.executedTemplateQuery.executedConfiguration : ExecutedReportConfiguration.get(params.id)
        ExecutionStatus executionStatus =  ExecutionStatus.findByExecutedEntityId(executedConfiguration.getId())
        ReportConfiguration configurationInstance = ReportConfiguration.read(executionStatus?.entityId)
        if (!configurationInstance) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        Boolean isEditableBy = configurationInstance.isEditableBy(currentUser)
        if (!isEditableBy) {
            render template: "editConfig", model: [forbidden: true, configurationInstance: configurationInstance]
            return
        }
        Set<SourceProfile> sourceProfiles = SourceProfile.sourceProfilesForUser(currentUser)
        String configType = configurationInstance ? configurationInstance.getConfigType() : null
        configurationInstance.scheduleDateJSON = configurationService.correctSchedulerJSONForCurrentDate(configurationInstance.scheduleDateJSON, configurationInstance.nextRunDate)
        render template: "editConfig", model: [configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, configType: configType, id: executionStatus.executedEntityId]
    }
}
