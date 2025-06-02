package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.audit.AuditTrail
import com.rxlogix.commandObjects.CaseSubmissionCO
import com.rxlogix.config.*
import com.rxlogix.customException.CaseScheduleException
import com.rxlogix.customException.CaseSubmissionException
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.dto.AuditTrailChildDTO
import com.rxlogix.dto.IcsrTrackingBulkDownloadDTO
import com.rxlogix.mapping.AuthorizationType
import com.rxlogix.mapping.IcsrCaseMessageQueue
import com.rxlogix.user.User
import grails.util.Holders

import java.nio.file.Files
import groovy.json.JsonBuilder
import com.rxlogix.enums.IcsrCaseStateEnum
import com.rxlogix.enums.IcsrReportSpecEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.file.MultipartFileSender
import com.rxlogix.mapping.IcsrCaseProcessingQueue
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FilterUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.ReadOnly
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import org.grails.web.json.JSONArray
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import com.rxlogix.util.RelativeDateConverter

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.NOT_FOUND
import com.rxlogix.IcsrCaseTrackingService

@Secured('permitAll')
class IcsrCaseTrackingRestController extends RestfulController implements SanitizePaginationAttributes {

    def dynamicReportService

    def icsrReportService

    def userService

    def ldapService

    def reportSubmissionService

    def hazelService

    def reportExecutorService

    def sqlGenerationService

    def executedIcsrConfigurationService

    def icsrScheduleService

    def CRUDService

    def icsrProfileAckService

    def utilService

    IcsrCaseTrackingService icsrCaseTrackingService

    IcsrCaseTrackingRestController() {
        super(IcsrCaseTracking)
    }

    def index() {
        String caseNumber = params.caseNumber
        Long exIcsrTemplateQueryId = params.long('exIcsrTemplateQueryId')
        Long exIcsrProfileId = params.long('exIcsrProfileId')
        Long versionNumber = params.long('versionNumber')
        sanitize(params)
        params.sort = (params.sort == "dateCreated") ? "generationDate" : params.sort
        LibraryFilter filter = new LibraryFilter(params, userService.currentUser, IcsrCaseTracking.class)
        def searchDataJson = FilterUtil.convertToJsonFilter(params.searchData).findAll { it.value.val }
        List<Closure> searchData = FilterUtil.buildCriteriaForColumnFilter(searchDataJson, userService.currentUser)

        String icsrCaseStateEnum = null
        if(params.state != IcsrCaseStateEnum.ALL.name()) {
            icsrCaseStateEnum = params.state
        }
        List<Long> profileIds = IcsrProfileConfiguration.fetchAllProfileIds(userService.currentUser, userService.currentUser.isICSRAdmin(), false).list()
        if (profileIds && profileIds.size() > 0) {
            def icsrCaseTrackingQuery = IcsrCaseTracking.getAllByFilter(filter, caseNumber, versionNumber, exIcsrProfileId, exIcsrTemplateQueryId, icsrCaseStateEnum, profileIds, searchData, userService.currentUser.isICSRAdmin(), params.sort, params.order)
            List<IcsrCaseTracking> icsrCaseTrackingList = icsrCaseTrackingQuery.list([max: params.max, offset: params.offset, sort: 'processedReportId', order: 'asc'])
            Map<String,Boolean> prequalifiedCases = icsrCaseTrackingService.preloadPrequalifiedCases(icsrCaseTrackingList)
            List<Map> icsrCaseTrackings = icsrCaseTrackingList.findAll { it }.collect { toMap(it, prequalifiedCases) }
            render([aaData: icsrCaseTrackings, recordsTotal: IcsrCaseTracking.getAllByFilter(new LibraryFilter([:]), caseNumber, versionNumber, exIcsrProfileId, exIcsrTemplateQueryId, icsrCaseStateEnum, profileIds, null, userService.currentUser.isICSRAdmin(), params.sort, params.order).count(), recordsFiltered: icsrCaseTrackingQuery.count(), caseNumber: caseNumber] as JSON)
            return
        } else {
            render([aaData: [], recordsTotal: 0, recordsFiltered: 0] as JSON)
            return
        }
    }

    def listStandardJustification() {
        sanitize(params)

        List<StandardJustification> justifications = []
        StandardJustification.'pva'.withNewSession {
            justifications = StandardJustification.findAllByActionNameAndIsActiveAndIsDisplay(params.actionName as String, true, true)
        }

        Long pvaLangId = sqlGenerationService.getPVALanguageId(Locale.ENGLISH.toString())
        Long pvaJaLangId = sqlGenerationService.getPVALanguageId(Locale.JAPANESE.toString())

        List<StandardJustification> justificationList = justifications.findAll {it.langId == pvaLangId}
        List<StandardJustification> jaJustificationList = justifications.findAll {it.langId == pvaJaLangId}

        render justificationList.collect {item -> [id: item.codeId, justification: item.description,
                                                   justificationJ: jaJustificationList.find {jaItem -> jaItem.codeId == item.codeId}?.description] } as JSON
    }

    private Map toMap(IcsrCaseTracking icsrCaseTracking, Map<String, Boolean> prequalifiedCases = null){
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.read(icsrCaseTracking.exIcsrProfileId)
        Date dueInDate = icsrCaseTracking.dueDate
        Integer dueInDays = icsrCaseTracking.dueInDays
        String preferredTimeZone = ""
        Boolean showPrequalifiedError = false
        String prodHashCode = null

        if(executedReportConfiguration instanceof ExecutedIcsrProfileConfiguration || executedReportConfiguration instanceof ExecutedIcsrReportConfiguration) {
            preferredTimeZone = executedReportConfiguration.preferredTimeZone
        }

        if (icsrCaseTracking.e2BStatus == IcsrCaseStateEnum.SCHEDULED.toString() && icsrCaseTracking.flagPmda && icsrCaseTracking.flagCaseLocked && (icsrCaseTracking.followupInfo == Constants.FOLLOWUP.toString() || icsrCaseTracking.followupInfo == Constants.NULLIFICATION.toString())) {
            String compositeKey = "${icsrCaseTracking.exIcsrTemplateQueryId}_${icsrCaseTracking.caseNumber}_${icsrCaseTracking.versionNumber}"
            showPrequalifiedError = prequalifiedCases.containsKey(compositeKey)
        }

        if (icsrCaseTracking.prodHashCode && icsrCaseTracking.prodHashCode != "-1") {
            prodHashCode = icsrCaseTracking.prodHashCode
        }

        def map = [:]
        map['id'] = icsrCaseTracking.uniqueIdentifier()
        map['caseNumber'] = icsrCaseTracking.caseNumber
        map['versionNumber'] = icsrCaseTracking.versionNumber
        map['caseReceiptDate'] = icsrCaseTracking.caseReceiptDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['safetyReceiptDate'] = icsrCaseTracking.safetyReceiptDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['productName'] = icsrCaseTracking.productName
        map['eventPreferredTerm'] = icsrCaseTracking.eventPreferredTerm
        map['susar'] = icsrCaseTracking.susar
        map['recipient'] = icsrCaseTracking.recipient
        map['profileName'] = icsrCaseTracking.profileName
        map['queryName'] = icsrCaseTracking.downgrade ? "Downgrade Report" : icsrCaseTracking.sectionTitle
        map['reportForm'] = icsrCaseTracking.templateName
        map['dueDate'] = dueInDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['scheduledDate'] = icsrCaseTracking.scheduledDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['generationDate'] = icsrCaseTracking.generationDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['submissionDate'] = icsrCaseTracking.submissionDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['e2BStatus'] = icsrCaseTracking.e2BStatus
        map['report'] = icsrCaseTracking.isReport
        map['exIcsrProfileId'] = icsrCaseTracking.exIcsrProfileId
        map['exIcsrTemplateQueryId'] = icsrCaseTracking.exIcsrTemplateQueryId
        map['showReportLink'] = executedReportConfiguration ? true : false
        map['indicator'] = getIndicator(icsrCaseTracking, dueInDate)
        map['currentState'] = icsrCaseTracking.e2BStatus
        map['dueInDays'] = dueInDays
        map['followupNumber'] = icsrCaseTracking.followupNumber
        map['localReportMessage'] = icsrCaseTracking.localReportMessage
        map['transmissionDate'] = icsrCaseTracking.transmissionDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['modifiedDate'] = icsrCaseTracking.modifiedDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['followupInfo'] = icsrCaseTracking.followupInfo
        map['downgrade'] = icsrCaseTracking.downgrade
        map['awareDate'] = icsrCaseTracking.awareDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['ackFileName'] = icsrCaseTracking.ackFileName
        map['isGenerated'] = icsrCaseTracking.isGenerated ? true : false
        map['caseId'] = icsrCaseTracking.caseId
        map['templateId'] = icsrCaseTracking.templateId
        map['flagLocalCpRequired'] = icsrCaseTracking.flagLocalCpRequired ? true : false
        map['flagAutoGenerate'] = icsrCaseTracking.flagAutoGenerate ? true : false
        map['submissionFormDesc'] = icsrCaseTracking.submissionFormDesc
        map['allowNullification'] = icsrCaseTracking.followupInfo == "Nullification" ? false : true
        map['processedReportId'] = icsrCaseTracking.processedReportId
        map['prodHashCode'] = prodHashCode
        map['preferredTimeZone'] = preferredTimeZone
        map['preferredDateTime'] = icsrCaseTracking.preferredDateTime?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['recipientTimeZone'] = icsrCaseTracking.timeZoneOffset
        map['profileId'] = icsrCaseTracking.profileId
        map['manualFlag'] = icsrCaseTracking.manualFlag
        map['authId'] = icsrCaseTracking.authId
        map['reportCategoryId'] = icsrCaseTracking.reportCategoryId
        map['isExpedited'] = icsrCaseTracking.isExpedited
        map['authorizationTypeId'] = icsrCaseTracking.authorizationTypeId
        map['originalSectionId'] = icsrCaseTracking.originalSectionId
        map['authorizationType'] = icsrCaseTracking.authorizationType
        map['approvalNumber'] = icsrCaseTracking.approvalNumber
        map['regenerateFlag'] = icsrCaseTracking.regenerateFlag? true : false
        map['showPrequalifiedError'] = showPrequalifiedError
        map['flagCaseLocked'] = icsrCaseTracking.flagCaseLocked ? true : false
        map['flagLocalCp'] = icsrCaseTracking.flagLocalCp
        return map
    }

    private String getIndicator(IcsrCaseTracking icsrCaseTracking, Date dueInDate) {
        Date now = new Date();
        Date soon = now + 2;
        if (dueInDate > now && dueInDate < soon && !icsrCaseTracking.submissionDate) return "yellow"
        if (dueInDate < now && !icsrCaseTracking.submissionDate) return "red"
        return ""
    }

    @ReadOnly(connection = 'pva')
    @Secured("ROLE_ICSR_PROFILE_VIEWER")
    def caseHistory(String profileName, Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber) {
        ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = ExecutedIcsrTemplateQuery.read(exIcsrTemplateQueryId).usedConfiguration
        Long processedReportIdList = IcsrCaseSubmission.fetchIcsrCaseSubmissionByCaseNoAndVersionNo(profileName, exIcsrTemplateQueryId, caseNumber, versionNumber).get()
        List caseSubmissionList = IcsrCaseSubmission.findAllByProcessedReportId(processedReportIdList, [sort:'e2bProcessId',order: 'asc'])
        List<Map> caseSubmissions = caseSubmissionList.findAll { it }.collect { toCaseSubmissionMap(it, executedIcsrProfileConfiguration.preferredTimeZone) }
        render caseSubmissions as JSON
    }

    @ReadOnly(connection = 'pva')
    def loadIcsrSubmissionHistoryForm(String caseNumber, Long versionNumber, String profileName, Long exIcsrTemplateQueryId) {
        ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = ExecutedIcsrTemplateQuery.read(exIcsrTemplateQueryId).usedConfiguration
        Long processedReportIdList = IcsrCaseSubmission.fetchIcsrCaseSubmissionByCaseNoAndVersionNo(profileName, exIcsrTemplateQueryId, caseNumber, versionNumber).get()
        List caseSubmissionList = IcsrCaseSubmission.findAllByProcessedReportId(processedReportIdList, [sort:'e2bProcessId',order: 'asc'])
        List<Map> caseSubmissions = caseSubmissionList.findAll { it }.collect { toCaseSubmissionMap(it, executedIcsrProfileConfiguration.preferredTimeZone) }
        render caseSubmissions as JSON
    }

    @ReadOnly(connection = 'pva')
    def caseAllReceipentHistory(String caseNumber, Long versionNumber) {
        List<IcsrCaseSubmission> caseHistoryList = IcsrCaseSubmission.findAllByCaseNumberAndVersionNumber(caseNumber, versionNumber, [sort: 'lastUpdateDate', order: 'asc'])
        render caseHistoryList.collect { toCaseSubmissionMap(it) } as JSON
    }

    private Map toCaseSubmissionMap(IcsrCaseSubmission icsrCaseSubmission, String preferredTimeZone = null){
        def map = [:]
        map['caseNumber'] = icsrCaseSubmission?.caseNumber
        map['versionNumber'] = icsrCaseSubmission?.versionNumber
        map['e2BStatus'] = icsrCaseSubmission.e2bStatus
        map['lastUpdateDate'] = icsrCaseSubmission.lastUpdateDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['reportDestination'] = icsrCaseSubmission.reportDestination
        map['exIcsrTemplateQueryId'] = icsrCaseSubmission.exIcsrTemplateQueryId
        map['lastUpdatedBy'] = icsrCaseSubmission.lastUpdatedBy?:'Application'
        map['ackFileName'] = icsrCaseSubmission.ackFileName
        map['comments'] = icsrCaseSubmission.comments?.replaceAll("(?i)''","'")
        map['commentsJ'] = icsrCaseSubmission.commentsJ?.replaceAll("(?i)''","'")
        map['submissionDocument'] = icsrCaseSubmission.submissionDocument ? (icsrCaseSubmission.submissionDocument == new byte[1024] ? false : true) : false
        map['e2bProcessId'] = icsrCaseSubmission.e2bProcessId
        map['userTimeZone'] = fetchTimeZoneMessage(userService.currentUser?.preference?.timeZone ?: "UTC")
        map['attachmentAckFileName'] = icsrCaseSubmission.attachmentAckFileName

        Date statusDate = null
        switch (icsrCaseSubmission.e2bStatus){
            case IcsrCaseStateEnum.GENERATED.toString():
                statusDate = icsrCaseSubmission.lastUpdateDate
                break
            case IcsrCaseStateEnum.SCHEDULED.toString():
                statusDate = icsrCaseSubmission.lastUpdateDate
                break
            case IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED.toString():
                statusDate = icsrCaseSubmission.lastUpdateDate
                break
            case IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL.toString():
                statusDate = icsrCaseSubmission.lastUpdateDate
                break
            case IcsrCaseStateEnum.TRANSMISSION_ERROR.toString():
                statusDate = icsrCaseSubmission.lastUpdateDate
                break
            case IcsrCaseStateEnum.TRANSMITTING.toString():
                statusDate = icsrCaseSubmission.transmissionDate
                break
            case IcsrCaseStateEnum.TRANSMITTED.toString():
                statusDate = icsrCaseSubmission.transmittedDate
                break
            case IcsrCaseStateEnum.COMMIT_RECEIVED.toString():
                statusDate = icsrCaseSubmission.ackReceiveDate
                break
            case IcsrCaseStateEnum.TRANSMITTING_ATTACHMENT.toString():
                statusDate = icsrCaseSubmission.dateTransmissionAttach
                break
            case IcsrCaseStateEnum.TRANSMITTED_ATTACHMENT.toString():
                statusDate = icsrCaseSubmission.dateTransmittedAttach
                break
            case IcsrCaseStateEnum.SUBMITTED.toString():
                statusDate = icsrCaseSubmission.submissionDate
                break
            default:
                if (icsrCaseSubmission.e2bStatus in IcsrCaseStateEnum.statusesOfAck*.toString()) {
                    if(icsrCaseSubmission.dateAckRecievedAttach) {
                        statusDate = icsrCaseSubmission.dateAckRecievedAttach
                    }else {
                        statusDate = icsrCaseSubmission.ackReceiveDate
                    }
                }else{
                    //RE-GENERATION IN PROGRESS
                    statusDate = icsrCaseSubmission.lastUpdateDate
                }
                break
        }
        if(statusDate){
            // If localDateTime exists in DB, then show it directly, else fetch local submission date on the basis of executed recipient preferred timezone
            if (icsrCaseSubmission.localDateTime) {
                map['preferredTimeZoneDate'] = icsrCaseSubmission.localDateTime.format(DateUtil.DATEPICKER_UTC_FORMAT)
                map['preferredTimeZone'] = fetchTimeZoneMessage(icsrCaseSubmission.timeZoneOffset)
            } else {
                Date preferredTimeZoneDate = preferredTimeZone ? DateUtil.covertToDateWithTimeZone(statusDate, Constants.DateFormat.NO_TZ , preferredTimeZone) : null
                map['preferredTimeZoneDate'] = preferredTimeZoneDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
                map['preferredTimeZone'] = fetchTimeZoneMessage(preferredTimeZone)
            }
            map['statusDate'] = statusDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        }
        return map
    }

    public String fetchTimeZoneMessage(String timeZoneId) {
        TimeZoneEnum timeZone = TimeZoneEnum.values().find {
            it.timezoneId == timeZoneId
        }
        return ViewHelper.getMessage(timeZone?.getI18nKey(), timeZone?.getGmtOffset())
    }

    @ReadOnly(connection = 'pva')
    def downloadDocFile(){
        IcsrCaseSubmission icsrCaseSubmission = IcsrCaseSubmission.findByE2bProcessId(params.e2bProcessId)
        byte [] data = icsrCaseSubmission.submissionDocument
        String filename = icsrCaseSubmission.submissionFilename
        String contentType = grailsApplication.config.grails.mime.types["${filename.substring(filename.lastIndexOf(".") + 1)}"]
        render(file: data, contentType: contentType, fileName: filename)
    }

    def downloadMergedPdf(){
        ExecutedIcsrTemplateQuery executedTemplateQuery = ExecutedIcsrTemplateQuery.read(params.exIcsrTemplateQueryId)
        ExecutedIcsrProfileConfiguration executedConfiguration = executedTemplateQuery?.executedConfiguration as ExecutedIcsrProfileConfiguration
        String currentSenderIdentifier = executedConfiguration?.getSenderIdentifier()
        String fileName = null
        IcsrCaseTracking icsrCaseTrackingInstance = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, params.caseNumber, params.versionNumber  as Long)
        }
        Date fileDate = icsrCaseTrackingInstance?.transmissionDate
        Boolean isJapanProfile = icsrCaseTrackingInstance?.isJapanProfile()
        if (executedTemplateQuery?.reportResult) {
            fileName = dynamicReportService.getTransmittedPdfFileName(executedTemplateQuery.reportResult, params.caseNumber)
        } else {
            CaseResultData caseResultData = CaseResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(params.exIcsrTemplateQueryId as Long, params.caseNumber, params.versionNumber as Long)
            if (caseResultData) {
                fileName = dynamicReportService.getTransmittedPdfFileName(caseResultData, currentSenderIdentifier, params.caseNumber, params.versionNumber as Long, params.exIcsrTemplateQueryId as Long, fileDate, isJapanProfile)
            }
        }
        XMLResultData xmlResultData = XMLResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(params.exIcsrTemplateQueryId as Long, params.caseNumber, params.versionNumber as Long)
        byte [] data = xmlResultData.attachmentData
        String filename = fileName
        String contentType = grailsApplication.config.grails.mime.types.pdf
        render(file: data, contentType: contentType, fileName: filename)
    }

    def checkFileExist(){
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        try{
            File file=new File(params.ackFileName)
            if(file.exists()){
                responseDTO.setSuccessResponse("success")
            }else{
                responseDTO.setFailureResponse(message(code: 'app.report.file.not.found') as String)
            }
        } catch(Exception e){
            log.error("Unknown Error occurred  ",e)
            responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
        }
        render(responseDTO.toAjaxResponse())
    }

    def downloadAckFile(){
        File file = new File(params.ackFileName)
        String type = dynamicReportService.getContentType(file.name)
        render (file: file, fileName: file.name, contentType: type)
    }

    def downloadBatchXML() {
        File reportFile = null
        try {
            List<String> caseNumbers = params.getList("caseNumber[]")
            IcsrReportSpecEnum reportSpec = IcsrReportSpecEnum.valueOf(params.reportSpec)
            List<Tuple2<String, ReportResult>> requestData = parseCaseTrackingList(caseNumbers)
            String reportFileName = "ICSR Batch Report.xml"
            reportFile = icsrReportService.createBatchXMLReport(requestData, reportSpec)
            log.info("reportFile " + reportFile.toPath())
            GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
            webRequest.setRenderView(false)
            MultipartFileSender.renderFile(reportFile, reportFileName, ReportFormatEnum.XML.name(), dynamicReportService.getContentType(ReportFormatEnum.XML.name()), request, response, false)
        } catch (RuntimeException e) {
            request.withFormat {
                form {
                    flash.error = e.localizedMessage
                    redirect(controller: "icsrProfileConfiguration", action: "viewCases")
                }
            }
        } catch (IOException e) {
            flash.error = message(code: "default.server.error.message")
            log.warn("IOException occurred in downloadBatchXML method while rendering file ${reportFile.name} . Error: ${e.getMessage()}")
        }
    }

    def downloadBulkXML() {
        List<String> caseNumbers = params.getList("caseNumber[]")
        IcsrReportSpecEnum reportSpec = IcsrReportSpecEnum.valueOf(params.reportSpec)
        List<Tuple2<String, ReportResult>> requestData = parseCaseTrackingList(caseNumbers)
        String reportFileName = "ICSR Bulk Report.zip"
        File reportFile = icsrReportService.createBulkXMLReport(requestData, reportSpec)
        log.info("reportFile " + reportFile.toPath())
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        webRequest.setRenderView(false)
        try{
            MultipartFileSender.renderFile(reportFile, reportFileName, ReportFormatEnum.ZIP.name(), dynamicReportService.getContentType(ReportFormatEnum.ZIP.name()), request, response, false)
        } catch (IOException e){
            flash.error = message(code: "default.server.error.message")
            log.warn("IOException occurred in downloadBulkXML while rendering file ${reportFile.name}. Error: ${e.getMessage()}")
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.report'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    @ReadOnly(connection = 'pva')
    def getErrorDetails(String profileName, Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber, String status) {
        IcsrCaseSubmission errorDetails = IcsrCaseSubmission.fetchIcsrErrorDetailsByCaseNoAndVersionNo(profileName, exIcsrTemplateQueryId, caseNumber, versionNumber,status).get()
        render errorDetails?.parsingErrorTxt ? errorDetails?.parsingErrorTxt : ""
    }

    def bulkTransmitCases() {
        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
            if (!params.password) {
                sendResponse(500, message(code: "app.label.workflow.rule.fillLogon").toString());
                return
            }
            if (!ldapService.isLoginPasswordValid(userService.currentUser.username, params.password)) {
                sendResponse(500, message(code: "app.label.workflow.rule.approvl.fail").toString());
                return
            }
        }
        int successfulTransmitCount = 0
        int failedTransmitCount = 0
        Long icsrTemplateQueryId = null
        String caseNumber = null
        Integer versionNumber = null
        def transmitIdsList = JSON.parse(params.transmitIds) as JSONArray
        transmitIdsList.each {
            try {
                icsrTemplateQueryId = Long.parseLong(it.id.split("_")[0])
                caseNumber = it.id.split("_")[1]
                versionNumber = Integer.parseInt(it.id.split("_")[2])
                String transmissionComments = null
                if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                    transmissionComments = Constants.APPROVED_BY + userService.currentUser.fullName + "\n" + Constants.APPROVED_ON + params.approvalDate?.toString() + "\n\n" + params.comments?.toString()
                } else {
                    transmissionComments = params.comments?.toString()
                }
                transmissionComments = transmissionComments.replaceAll("(?i)'","''")
                icsrReportService.transmitCase(icsrTemplateQueryId, caseNumber, versionNumber, transmissionComments, userService.currentUser.fullName, params.approvalDate?.toString(), params.comments?.toString())
                successfulTransmitCount += 1
            } catch (Exception e) {
                log.error("Failed to transmit case with case number ${caseNumber} "+e.getMessage())
                failedTransmitCount += 1
            }
        }
        sendResponse(200, message(code: 'icsr.case.bulk.transmit.message',args: [successfulTransmitCount, failedTransmitCount]).toString())
    }

    def transmitCase(String caseNumber) {
        Long icsrTempQueryId = params.long('icsrTempQueryId')
        Integer versionNumber = params.int('versionNumber')
        def user = userService.currentUser
        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
            if (!params.password) {
                sendResponse(500, message(code: "app.label.workflow.rule.fill.password").toString());
                return
            }
            if (!ldapService.isLoginPasswordValid(userService.currentUser.username, params.password)) {
                sendResponse(500, message(code: "app.label.workflow.rule.approvl.fail").toString());
                return
            }
        }
        try {
            String transmissionComments = null
            String userTimezone = user.preference.getTimeZone()
            String approvalDate = RelativeDateConverter.getFormattedCurrentDateTimeForTimeZone(user, "dd-MMM-yyyy hh:mm a", userTimezone) + " (${userTimezone})"
            if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                transmissionComments = Constants.APPROVED_BY + userService.currentUser.fullName + "\n" + Constants.APPROVED_ON + approvalDate + "\n\n" + params.comments?.toString()
            } else {
                transmissionComments = params.comments?.toString()
            }
            transmissionComments = transmissionComments.replaceAll("(?i)'","''")
            icsrReportService.transmitCase(icsrTempQueryId, caseNumber, versionNumber, transmissionComments, userService.currentUser.fullName, approvalDate, params.comments?.toString())
            sendResponse(200, message(code: 'icsr.case.transmit.success',args: [caseNumber, versionNumber, params.profileName]).toString())
        } catch (Exception ex) {
            log.error("Error while marking case ${caseNumber} as transmitted", ex)
            sendResponse(500, message(code: 'icsr.case.transmit.failed', args: [caseNumber, versionNumber, params.profileName], default: ex.message).toString())
        }
    }

    def checkPreviousVersionIsTransmitted(String profileName, String recipient, String caseNumber) {
        Long templateId = params.long('templateId')
        Integer versionNumber = params.int('versionNumber')
        Boolean needToCheckPreviousVersion = grailsApplication.config.getProperty('pvr.icsr.enforce.transmission.in.version.sequence', Boolean)
        if (needToCheckPreviousVersion) {
            boolean canTransmit = icsrScheduleService.checkPreviousVersionIsTransmitted(caseNumber, versionNumber, profileName, recipient, templateId)
            if (canTransmit) {
                sendResponse(200, "Old Version is Transmitted")
            } else {
                sendResponse(500, "Old Version is Not Transmitted")
            }
        } else {
            sendResponse(200, "Old Version is Transmitted")
        }
    }

    def checkPreviousVersionIsTransmittedForAll() {
        Boolean needToCheckPreviousVersion = grailsApplication.config.getProperty('pvr.icsr.enforce.transmission.in.version.sequence', Boolean)
        if (needToCheckPreviousVersion) {
            if (params.checkIds) {
                String caseNumber
                Integer versionNumber
                String profileName
                String recipient
                Long originalTemplateId
                boolean canProceedToTransmit = true
                def transmitIdsList = JSON.parse(params.checkIds) as JSONArray
                try {
                    transmitIdsList.each {
                        caseNumber = it.caseNumber
                        versionNumber = it.versionNumber
                        profileName = it.profileName
                        recipient = it.recipient
                        originalTemplateId = it.templateId
                        canProceedToTransmit = icsrScheduleService.checkPreviousVersionIsTransmitted(caseNumber, versionNumber, profileName, recipient, originalTemplateId)
                        if (!canProceedToTransmit) {
                            sendResponse(500, "Any of the cases' old version is not Transmitted")
                            return
                        }
                    }
                    if (canProceedToTransmit) {
                        sendResponse(200, "All Old Versions Are Transmitted")
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                    sendResponse(500, "Any of the cases' old version is not Transmitted")
                }
            }
        } else {
            sendResponse(200, "All Old Versions Are Transmitted")
        }
    }

    def bulkSubmitIscrCase() {
        String profileId
        String queryId
        String caseNumber
        String versionNumber
        Integer isLate = 0
        Integer successfulSubmissionCount = 0
        Integer failedSubmissionCount = 0

        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
            if (!params.password) {
                sendResponse(500, message(code: "app.label.workflow.rule.fillLogon").toString());
                return
            }
            if (!ldapService.isLoginPasswordValid(userService.currentUser.username, params.password)) {
                sendResponse(500, message(code: "app.label.workflow.rule.approvl.fail").toString());
                return
            }
        }
        if (!params.icsrCaseIds) {
            sendResponse(500, message(code: "default.system.error.message").toString());
            return
        }
        List<String> icsrCaseIdsList = params.icsrCaseIds.split(",")
        Date caseSubmissionDateUTC = Date.parse(DateUtil.SCHEDULE_DATE_JSON_FORMAT, JSON.parse(params.scheduleDateJSON).startDateTime)
        String timeZoneId = JSON.parse(params.scheduleDateJSON).timeZone.name
        Date localDate = DateUtil.covertToDateWithTimeZone(caseSubmissionDateUTC, Constants.DateFormat.NO_TZ, timeZoneId)
        icsrCaseIdsList.each {
            try {
                Long icsrTempQueryId = Long.parseLong(it.split("_")[0])
                caseNumber = it.split("_")[1]
                versionNumber = Integer.parseInt(it.split("_")[2])
                IcsrCaseTracking icsrCaseTracking = IcsrCaseTracking.withNewSession {
                    return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(icsrTempQueryId, caseNumber, Long.parseLong(versionNumber))
                }
                IcsrCaseTracking icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(icsrTempQueryId, caseNumber, Long.parseLong(versionNumber))
                Map oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTrackingInstance)
                if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                    oldValues.put("approvedBy", null)
                    oldValues.put("approvedOn", null)
                }
                String userLocale = userService.currentUser?.preference?.locale?.toString() ?: 'en'
                String altSubmissionCommentsField = userLocale == 'en' ? "submissionCommentsJ" : "submissionCommentsEng"
                oldValues.put("submissionComments", null)
                oldValues.put(altSubmissionCommentsField, null)
                oldValues.put("documentName", null)
                String submissionComments = null
                if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)){
                    submissionComments = Constants.APPROVED_BY + userService.currentUser.fullName + "\n" + Constants.APPROVED_ON + params.approvalDate?.toString() + "\n\n" + (params.bulkCaseSubmissionComments ? params.bulkCaseSubmissionComments?.toString() : "")
                } else {
                    submissionComments = params.bulkCaseSubmissionComments?.toString()
                }
                CaseSubmissionCO caseSubmissionCO = new CaseSubmissionCO()
                caseSubmissionCO.icsrCaseState = IcsrCaseStateEnum.valueOf(params.icsrCaseState)
                caseSubmissionCO.comment = submissionComments?.replaceAll("(?i)'","''")
                caseSubmissionCO.commentJ = params.bulkCaseSubmissionCommentsJa?.replaceAll("(?i)'","''")
                caseSubmissionCO.justificationId = params.justificationId ? params.long('justificationId') : null
                caseSubmissionCO.reportingDestinations = params.reportingDestinations
                caseSubmissionCO.icsrCaseId = icsrCaseTracking.uniqueIdentifier()
                caseSubmissionCO.profileName = icsrCaseTracking.profileName
                if (caseSubmissionCO.validate()) {
                    (profileId, queryId, caseNumber, versionNumber) = caseSubmissionCO?.icsrCaseId?.split("\\*\\*")
                    caseSubmissionCO.profileId = Long.parseLong(profileId)
                    caseSubmissionCO.queryId = queryId
                    caseSubmissionCO.caseNumber = caseNumber
                    caseSubmissionCO.versionNumber = Long.parseLong(versionNumber)
                    caseSubmissionCO.submissionDate = caseSubmissionDateUTC
                    caseSubmissionCO.localSubmissionDate = localDate
                    caseSubmissionCO.timeZoneId = timeZoneId
                    caseSubmissionCO.submissionDocument = params?.file?.getBytes()
                    caseSubmissionCO.submissionFilename = params?.filename
                    caseSubmissionCO.processedReportId = icsrCaseTracking.processedReportId
                    caseSubmissionCO.dueDate = icsrCaseTracking?.dueDate
                    if(icsrProfileAckService.validateSubmissionDate(localDate, icsrCaseTrackingInstance.generationDate, timeZoneId, params.icsrCaseState)) {
                        failedSubmissionCount += 1
                        return
                    }

                    reportSubmissionService.submitIcsrCase(Tenants.currentId() as Long, caseSubmissionCO)
                    IcsrCaseTracking newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(icsrTempQueryId, caseNumber, Long.parseLong(versionNumber))
                    Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
                    if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                        newValues.put("approvedBy", userService.currentUser.fullName)
                        newValues.put("approvedOn", params.approvalDate?.toString())
                    }
                    if (userLocale == 'en') {
                        newValues.put("submissionComments", params.bulkCaseSubmissionComments?.toString())
                        newValues.put(altSubmissionCommentsField, null)
                    } else {
                        newValues.put("submissionComments", params.bulkCaseSubmissionCommentsJa?.toString())
                        newValues.put(altSubmissionCommentsField, params.bulkCaseSubmissionComments?.toString() == "" ? null : params.bulkCaseSubmissionComments?.toString())
                    }
                    newValues.put("documentName", params.filename?.toString())
                    AuditLogConfigUtil.logChanges(icsrCaseTrackingInstance, newValues, oldValues
                            , Constants.AUDIT_LOG_UPDATE,ViewHelper.getMessage("auditLog.entityValue.icsr.changes", caseNumber, versionNumber, icsrCaseTrackingInstance.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTrackingInstance.recipient))
                    successfulSubmissionCount += 1
                }
            } catch (Exception e) {
                failedSubmissionCount += 1
                log.error("Failed to submitted", e)
            }
        }
        sendResponse(200, message(code: 'app.bulkReportSubmission.submitted.successful', args: [successfulSubmissionCount, failedSubmissionCount]).toString());
    }

    def submitIscrCase(CaseSubmissionCO caseSubmissionCO) {
        String profileId
        String queryId
        String caseNumber
        String versionNumber
        Integer isLate = 0

        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
            if (!params.password) {
                sendResponse(500, message(code: "app.label.workflow.rule.fillLogon").toString());
                return
            }
            if (!ldapService.isLoginPasswordValid(userService.currentUser.username, params.password)) {
                sendResponse(500, message(code: "app.label.workflow.rule.approvl.fail").toString());
                return
            }
        }
        if (!caseSubmissionCO?.icsrCaseId) {
            sendResponse(500, message(code: "default.system.error.message").toString());
            return
        }
        try {
            if (caseSubmissionCO.validate()) {
                (profileId, queryId, caseNumber, versionNumber) = caseSubmissionCO?.icsrCaseId?.split("\\*\\*")
                IcsrCaseTracking icsrCaseTrackingInstance = null
                IcsrCaseTracking.withNewSession {
                    icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(Long.parseLong(queryId), caseNumber, Long.parseLong(versionNumber))
                }
                if(params.icsrCaseState == 'SUBMISSION_NOT_REQUIRED_FINAL') {
                    if (icsrCaseTrackingInstance.flagCaseLocked && icsrCaseTrackingInstance.e2BStatus == IcsrCaseStateEnum.SCHEDULED.toString() && (icsrCaseTrackingInstance.flagAutoGenerate || icsrCaseTrackingInstance.flagLocalCp in [1, 2])) {
                        sendResponse(500, message(code: "icsr.report.generation.progress.error").toString())
                        return
                    }
                }
                Map oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTrackingInstance)
                if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                    oldValues.put("approvedBy", null)
                    oldValues.put("approvedOn", null)
                }
                String userLocale = userService.currentUser?.preference?.locale?.toString() ?: 'en'
                String altSubmissionCommentsField = userLocale == 'en' ? "submissionCommentsJ" : "submissionCommentsEng"
                oldValues.put("submissionComments", null)
                oldValues.put(altSubmissionCommentsField, null)
                oldValues.put("documentName", null)
                Long processedReportId = icsrCaseTrackingInstance?.processedReportId
                String submissionComments = null
                if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)){
                    submissionComments = Constants.APPROVED_BY + userService.currentUser.fullName + "\n" + Constants.APPROVED_ON + params.approvalDate?.toString() + "\n\n" + (params.caseSubmissionComments ? params.caseSubmissionComments.toString() : "")
                } else {
                    submissionComments = params.caseSubmissionComments?.toString()
                }
                caseSubmissionCO.comment = submissionComments?.replaceAll("(?i)'","''")
                caseSubmissionCO.commentJ = params.caseSubmissionCommentsJa?.replaceAll("(?i)'","''")
                caseSubmissionCO.justificationId = params.justificationId ? params.long('justificationId') : null
                caseSubmissionCO.profileId = Long.parseLong(profileId)
                caseSubmissionCO.queryId = queryId
                caseSubmissionCO.caseNumber = caseNumber
                caseSubmissionCO.versionNumber = Long.parseLong(versionNumber)
                Date caseSubmissionDateUTC = Date.parse(DateUtil.SCHEDULE_DATE_JSON_FORMAT, JSON.parse(params.scheduleDateJSON).startDateTime)
                String timeZoneId = JSON.parse(params.scheduleDateJSON).timeZone.name
                Date localDate = DateUtil.covertToDateWithTimeZone(caseSubmissionDateUTC, Constants.DateFormat.NO_TZ, timeZoneId)
                caseSubmissionCO.submissionDate = caseSubmissionDateUTC
                caseSubmissionCO.submissionDocument = params?.file?.getBytes()
                caseSubmissionCO.submissionFilename = params?.filename
                caseSubmissionCO.processedReportId = processedReportId
                caseSubmissionCO.localSubmissionDate = localDate
                caseSubmissionCO.timeZoneId = timeZoneId
                if(icsrProfileAckService.validateSubmissionDate(localDate, icsrCaseTrackingInstance.generationDate, timeZoneId, params.icsrCaseState)) {
                    sendResponse(500, message(code: "submission.date.later.error").toString())
                    return
                }

                reportSubmissionService.submitIcsrCase(Tenants.currentId() as Long, caseSubmissionCO)
                IcsrCaseTracking newIcsrCaseTrackingInstance = null
                IcsrCaseTracking.withNewSession {
                    newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(Long.parseLong(queryId), caseNumber, Long.parseLong(versionNumber))
                }
                Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
                if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)){
                    newValues.put("approvedBy", userService.currentUser.fullName)
                    newValues.put("approvedOn", params.approvalDate?.toString())
                }
                if (userLocale == 'en') {
                    newValues.put("submissionComments", params.caseSubmissionComments?.toString())
                    newValues.put(altSubmissionCommentsField, null)
                } else {
                    newValues.put("submissionComments", params.caseSubmissionCommentsJa?.toString())
                    newValues.put(altSubmissionCommentsField, params.caseSubmissionComments?.toString() == "" ? null : params.caseSubmissionComments?.toString())
                }

                newValues.put("documentName", params.filename?.toString())
                AuditLogConfigUtil.logChanges(icsrCaseTrackingInstance, newValues, oldValues
                        , Constants.AUDIT_LOG_UPDATE,ViewHelper.getMessage("auditLog.entityValue.icsr.changes", caseNumber, versionNumber, icsrCaseTrackingInstance.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTrackingInstance.recipient))
                if(IcsrCaseStateEnum.valueOf(params.icsrCaseState) in [IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED, IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL]) sendResponse(200, message(code: 'app.reportSubmission.submission.not.req.successful', args: [caseNumber, versionNumber, icsrCaseTrackingInstance.profileName]).toString())
                else sendResponse(200, message(code: 'app.reportSubmission.submitted.successful', args: [caseNumber, versionNumber, icsrCaseTrackingInstance.profileName]).toString())
            } else {
                log.warn(caseSubmissionCO.errors.allErrors?.toString())
                sendResponse(500, caseSubmissionCO.errors.allErrors.collect{message(error: it)}.join(','));
            }
        } catch(CaseSubmissionException cse){
            log.error(cse.message)
            sendResponse(500, message(code: cse.errorCode, default: cse.message).toString());
        } catch (Exception e) {
            log.error("Failed to submitted", e)
            sendResponse(500, message(code: "default.system.error.message", default: e.message).toString());
        }
    }


    private def sendResponse(stat, msg, errors = null) {
        response.status = stat
        Map responseMap = [
                message: msg,
                status: stat,
                errors: errors
        ]
        render(contentType: "application/json", responseMap as JSON)
    }

    private List<Tuple2<String, ReportResult>> parseCaseTrackingList(List<String> caseNumbers) {
        List<Tuple2<String, ReportResult>> result = []
        String senderId
        String receiverId
        Integer messageType
        caseNumbers.each { caseNumber ->
            ExecutedIcsrTemplateQuery executedTemplateQuery
            if (caseNumber.contains(";")) {
                String[] pair = caseNumber.split(";")
                caseNumber = pair.last()
                executedTemplateQuery = ExecutedTemplateQuery.get(pair.first())
                ExecutedIcsrProfileConfiguration executedConfiguration = executedTemplateQuery.executedConfiguration as ExecutedIcsrProfileConfiguration
                String currentSenderId = executedConfiguration.senderId
                String currentReceiverId = executedConfiguration.receiverId
                Integer currentMessageType = executedTemplateQuery.icsrMsgType
                if (senderId && currentSenderId != senderId) {
                    throw new RuntimeException(message(code: "icsr.case.tracking.error.differentSenderReceiverPair"))
                } else {
                    senderId = currentSenderId
                }
                if (receiverId && currentReceiverId != receiverId) {
                    throw new RuntimeException(message(code: "icsr.case.tracking.error.differentSenderReceiverPair"))
                } else {
                    receiverId = currentReceiverId
                }
                if (messageType && currentMessageType != messageType) {
                    throw new RuntimeException(message(code: "icsr.case.tracking.error.differentMessageTypes"))
                } else {
                    receiverId = currentReceiverId
                }
            }
            Tuple2 existingCase = result.find {
                it.first == caseNumber
            }
            if (existingCase) {
                throw new RuntimeException(message(code: "icsr.case.tracking.error.duplicateCaseNumber"))
            }
            if (executedTemplateQuery) {
                result.add(new Tuple2<String, ReportResult>(caseNumber, executedTemplateQuery.reportResult))
            } else {
                throw new RuntimeException("Executed template not found")
            }
        }
        return result
    }


    def listE2BStatuses() {
        render IcsrCaseStateEnum.values().findAll { it.key != "ALL" }.collect {
            [id: it.toString(), name: message(code: it.i18nKey)]
        } as JSON
    }

    def killCaseExecution(Long id) {
        try {
            ConfigObject hazelcast = grailsApplication.config.hazelcast
            log.info('Kill case execution request created by user: ' + userService.currentUser?.username + ' for execution id: ' + id)
            if (hazelcast.enabled) {
                String killCaseGenerationChannel = hazelcast.notification.killCaseGeneration
                hazelService.publishToTopic(killCaseGenerationChannel, id.toString())
            } else {
                reportExecutorService.killCaseGenerationExecution(id.toBigDecimal())
            }
            render(contentType: "application/json", [success: 'success', status: 200] as JSON)
        } catch (Exception ex) {
            log.error("Error while pushing kill case generation request ", ex)
            response.status = 500
            Map responseMap = [
                    message: message(code: "default.server.error.message"),
                    status : 500
            ]
            render(contentType: "application/json", responseMap as JSON)
        }
    }

    def executionStatus() {
        List<IcsrCaseProcessingQueue> currentCases = []
        if (reportExecutorService.executorThreadInfoService.totalCurrentlyGeneratingCases) {
            IcsrCaseProcessingQueue.'pva'.withNewSession {
                currentCases = IcsrCaseProcessingQueue.'pva'.findAllByIdInList(reportExecutorService.executorThreadInfoService.totalCurrentlyGeneratingCases, [sort: 'lastUpdated', order: 'desc'])
            }
        }
        List<Map> casesData = []
        currentCases.each {
            ExecutedIcsrTemplateQuery executedIcsrTemplateQuery = ExecutedIcsrTemplateQuery.read(it.executedTemplateQueryId)
            casesData.add([id: it.id, caseNumber: it.caseNumber, versionNumber: it.versionNumber, reportName: executedIcsrTemplateQuery?.executedConfiguration?.reportNameAndVersionNumber, configId: executedIcsrTemplateQuery?.executedConfigurationId, runDate: it.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),])
        }
        render([aaData: casesData, recordsTotal: casesData.size(), recordsFiltered: casesData.size()] as JSON)
    }


    @ReadOnly('pva')
    def caseList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        int offset = Math.max(page - 1, 0) * max
        render([items : sqlGenerationService.getManualCaseList(term, offset, max, false).collect {
            [id: "${it.caseNumber}${Constants.CASE_VERSION_SEPARATOR}${it.version}", text: "${it.caseNumber}-${it.version}"]
        }, total_count: sqlGenerationService.getManualCaseList(term, offset, max, true).size()] as JSON)
    }

    def deleteCase(Long exTempQueryId, String caseNumber) {
        Long versionNumber=params.long('versionNumber')
        log.info("Deleting case with caseNumber = "+caseNumber)
        IcsrCaseTracking icsrCaseTracking = IcsrCaseTracking.withNewSession {
            return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(exTempQueryId, caseNumber, versionNumber)
        }
        if(!icsrCaseTracking) {
            log.error("No Record found with CaseNumber ${caseNumber} and versionNumber ${versionNumber}")
            sendResponse(500, "No Record found with CaseNumber ${caseNumber} and versionNumber ${versionNumber}");
            return
        }
        String profileName = icsrCaseTracking.profileName
        String state = icsrCaseTracking.e2BStatus
        Long tenantId = Tenants.currentId()
        String prodHashCode = icsrCaseTracking.prodHashCode
        Long profileId = icsrCaseTracking.profileId
        Long processedRptId=icsrCaseTracking.processedReportId
        try {
            IcsrCaseTracking icsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(exTempQueryId, caseNumber, versionNumber)
            }
            Map oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTrackingInstance)
            String exTempltName=ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance?.exIcsrTemplateQueryId)?.executedTemplate?.name
            reportExecutorService.removeCaseFromTracking(profileName, caseNumber, versionNumber, state, tenantId, processedRptId, icsrCaseTrackingInstance?.exIcsrTemplateQueryId, icsrCaseTrackingInstance?.recipient, icsrCaseTrackingInstance?.dueDate, params.long('justificationId'), params.justification, params.justificationJ)
            String userLocale = userService.currentUser?.preference?.locale?.toString() ?: 'en'
            String justification = userLocale == 'en' ? params.justification : params.justificationJ
            String extraJustification = userLocale == 'en' ? params.justificationJ : params.justification
            AuditLogConfigUtil.logChanges(icsrCaseTrackingInstance, [:], oldValues
                    , Constants.AUDIT_LOG_DELETE,ViewHelper.getMessage("auditLog.entityValue.icsr.delete", caseNumber, versionNumber, icsrCaseTrackingInstance?.profileName, exTempltName, icsrCaseTrackingInstance?.recipient, justification, extraJustification ?: ''))
            sendResponse(200, message(code: "icsr.public.api.delete.case.success").toString())
        } catch (Exception e) {
            log.error("Error Deleting case with CaseNumber ${caseNumber} and versionNumber ${versionNumber}" + e.getMessage())
            sendResponse(500, "Error while deleting case from tracking")
        }
    }

    def regenerateCase(String caseNumber, String regenerateComment) {
        Long exTempQueryId = params.long('exTempQueryId')
        Long versionNumber = params.long('versionNumber')
        IcsrCaseTracking newIcsrCaseTrackingInstance = null
        IcsrCaseTracking.withNewSession {
            newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(exTempQueryId, caseNumber, versionNumber)
        }
        Long profileId = newIcsrCaseTrackingInstance.profileId
        Integer dueInDays = newIcsrCaseTrackingInstance.dueInDays
        Boolean isExpedited = newIcsrCaseTrackingInstance.isExpedited
        String prodHashCode = newIcsrCaseTrackingInstance.prodHashCode
        Long authorizationTypeId = newIcsrCaseTrackingInstance.authorizationTypeId
        Long authId = newIcsrCaseTrackingInstance.authId
        Long reportCategoryId = newIcsrCaseTrackingInstance.reportCategoryId
        Long originalSectionId =newIcsrCaseTrackingInstance.originalSectionId
        Long processedReportId = newIcsrCaseTrackingInstance.processedReportId

        IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.read(profileId)
        TemplateQuery templateQuery = TemplateQuery.findById(originalSectionId)
        if(!templateQuery){
            sendResponse(500, message(code:"icsr.add.regenerate.case.fail"))
            return
        }
        User user = userService.getUser()
        String username = user.fullName?:user.username
        final Date scheduleDate = new Date()
        boolean flagRegenerate = true
        try {
            icsrScheduleService.addCaseToSchedule(profileConfiguration, templateQuery, caseNumber, versionNumber, dueInDays, isExpedited, username, user.fullName, prodHashCode, authorizationTypeId, authId, reportCategoryId, newIcsrCaseTrackingInstance.followupInfo == "Nullification" ? 3 : null, scheduleDate, flagRegenerate ,regenerateComment ,processedReportId)
            log.info("Successfully Regenerated Report for  case number ${caseNumber}")
            sendResponse(200, message(code:"icsr.add.regenerate.case.success", args: [caseNumber + " v" + versionNumber, profileConfiguration.reportName]).toString())
        } catch (CaseScheduleException e) {
            sendResponse(500, "Not able to Regenerate Report")
            return
        }
    }

    def bulkRegenerateCase(String bulkRegenerateComment) {
        int successfulRegenerateCount = 0
        int failedRegenerateCount = 0
        Long exTempQueryId
        String caseNumber
        Long versionNumber

        def bulkRegenerateIdsList = JSON.parse(params.regenerateIds) as JSONArray
        bulkRegenerateIdsList.each {
            try {
                exTempQueryId = Long.parseLong(it.id.split("_")[0])
                caseNumber = it.id.split("_")[1]
                versionNumber = Integer.parseInt(it.id.split("_")[2])
                bulkRegenerateComment = bulkRegenerateComment.replaceAll("(?i)'","''")
                IcsrCaseTracking newIcsrCaseTrackingInstance = null
                IcsrCaseTracking.withNewSession {
                    newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(exTempQueryId, caseNumber, versionNumber)
                }
                Long profileId = newIcsrCaseTrackingInstance.profileId
                Integer dueInDays = newIcsrCaseTrackingInstance.dueInDays
                Boolean isExpedited = newIcsrCaseTrackingInstance.isExpedited
                String prodHashCode = newIcsrCaseTrackingInstance.prodHashCode
                Long authorizationTypeId = newIcsrCaseTrackingInstance.authorizationTypeId
                Long authId = newIcsrCaseTrackingInstance.authId
                Long reportCategoryId = newIcsrCaseTrackingInstance.reportCategoryId
                Long originalSectionId =newIcsrCaseTrackingInstance.originalSectionId
                Long processedReportId = newIcsrCaseTrackingInstance.processedReportId
                IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.read(profileId)
                TemplateQuery templateQuery = TemplateQuery.findById(originalSectionId)
                if(!templateQuery){
                    failedRegenerateCount += 1
                    log.error("Failed to regenerate case with case number ${caseNumber} - ${versionNumber} for Profile ID ${profileId} as report created before the upgrade can not be Regenerated.")
                    return
                }
                User user = userService.getUser()
                final Date scheduleDate = new Date()
                boolean flagRegenerate = true

                icsrScheduleService.addCaseToSchedule(profileConfiguration, templateQuery, caseNumber, versionNumber, dueInDays, isExpedited, user?.username, user?.fullName, prodHashCode, authorizationTypeId, authId, reportCategoryId, newIcsrCaseTrackingInstance.followupInfo == "Nullification" ? 3 : null, scheduleDate, flagRegenerate ,bulkRegenerateComment, processedReportId)
                successfulRegenerateCount += 1
            } catch (Exception e) {
                log.error("Failed to regenerate case with case number ${caseNumber} "+ e.getMessage())
                failedRegenerateCount += 1
            }
        }
        sendResponse(200, message(code: 'icsr.case.bulk.regenerate.message',args: [successfulRegenerateCount, failedRegenerateCount]).toString())
    }

    def nullifyReport(String caseNumber, Integer dueInDays) {
        Long icsrTempQueryId = params.long('icsrTempQueryId')
        Long versionNumber = params.long('versionNumber')
        String prodHashCode = params.prodHashCode
        log.info("Request Received to Nullify Report for exTempQueryId ${icsrTempQueryId}, case number ${caseNumber} and versionNumber ${versionNumber}")
        prodHashCode = prodHashCode ?: "-1"
        String justification = params.justification
        justification = (justification.length() > 2000) ? justification.substring(0, 2000) : justification
        ExecutedIcsrTemplateQuery icsrTemplateQuery = ExecutedIcsrTemplateQuery.read(icsrTempQueryId)
        ExecutedIcsrProfileConfiguration icsrProfileConfiguration = icsrTemplateQuery?.usedConfiguration
        IcsrProfileConfiguration originalIcsrProfileConfiguration = null
        try {
            ExecutedReportConfiguration executedIcsrProfileConfiguration = executedIcsrConfigurationService.createFromExecutedIcsrConfiguration(icsrProfileConfiguration, null)
            CRUDService.saveWithoutAuditLog(executedIcsrProfileConfiguration)
            ExecutedTemplateQuery executedTemplateQuery = executedIcsrProfileConfiguration.executedTemplateQueriesForProcessing.first()
            originalIcsrProfileConfiguration = IcsrProfileConfiguration.findByReportName(executedIcsrProfileConfiguration.reportName)
            IcsrCaseTracking icsrCaseTrackingRecord = null
            IcsrCaseTracking.withNewSession {
                icsrCaseTrackingRecord = icsrProfileAckService.getIcsrTrackingRecord(icsrTempQueryId, caseNumber, versionNumber)
            }
            icsrScheduleService.logIcsrCaseToScheduleTrackingForNullification(originalIcsrProfileConfiguration, executedTemplateQuery, caseNumber, versionNumber, dueInDays, justification, icsrTempQueryId, prodHashCode, icsrCaseTrackingRecord.authId, icsrCaseTrackingRecord.reportCategoryId)
            IcsrCaseTracking newIcsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
            }
            Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
            newValues.put("justificationForNullification",justification)
            AuditLogConfigUtil.logChanges(newIcsrCaseTrackingInstance, newValues, [:]
                    , Constants.AUDIT_LOG_INSERT, ViewHelper.getMessage("auditLog.entityValue.icsr.scheduled.manual", caseNumber, versionNumber, newIcsrCaseTrackingInstance.profileName,ExecutedIcsrTemplateQuery.read(newIcsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, newIcsrCaseTrackingInstance.recipient))
            log.info("Successfully Added nullification Report for exTempQueryId ${executedTemplateQuery.id}, case number ${caseNumber} and versionNumber ${versionNumber}")
            sendResponse(200, message(code: 'icsr.public.api.nullify.report.success',args: [caseNumber, versionNumber, originalIcsrProfileConfiguration.reportName]))
        } catch (Exception e) {
            log.error("Exception while adding case to nullfication. "+e.getMessage())
            e.printStackTrace()
            sendResponse(500, message(code: 'icsr.public.api.nullify.report.failure',args: [caseNumber, versionNumber, originalIcsrProfileConfiguration.reportName]))
        }
    }

    def saveLocalCp(Long caseId, Integer flagLocalCp, String caseNumber) {
        Long versionNumber = params.long('versionNumber')
        Long profileId = params.long('profileId')
        String prodHashCode = params.prodHashCode
        Long processedReportId = params.long('processReportId')
        Long exTempQueryId = params.long('exTempQueryId')
        IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.read(profileId)
        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.get(exTempQueryId)
        ExecutedIcsrProfileConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) MiscUtil.unwrapProxy(executedTemplateQuery.executedConfiguration)
        IcsrCaseTracking icsrCaseTracking = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTracking = icsrProfileAckService.getIcsrTrackingRecordByProcessedReportId(processedReportId, caseNumber, versionNumber)
        }
        // if profile has 'generate case before finalization' checkbox checked, then need to generate case without checking anything.
        if (!executedConfiguration.includeOpenCases) {
            if (!icsrCaseTracking?.flagCaseLocked) {
                log.info("Case Generation can not be processed because it is in Active state")
                sendResponse(500, message(code: 'icsr.case.add.localcp.case.state.active').toString())
                return
            }
        }
        try {
            Long tenantId = Tenants.currentId() as Long
            sqlGenerationService.localCpProc(caseId, versionNumber, executedConfiguration.id, executedConfiguration.reportName, tenantId, flagLocalCp, prodHashCode, profileId, icsrCaseTracking.processedReportId)
            log.info("Local CP Completed for caseId ${caseId}, Deleting Entry in IcsrCaseLocalCpData...")
            icsrScheduleService.deleteLocalCpProfileEntry(caseId, versionNumber, tenantId, profileConfiguration, true)
            Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTracking)
            String code = flagLocalCp == 1 ? "auditLog.entityValue.icsr.triggered.local.cp" : "auditLog.entityValue.icsr.triggered.manual.generate"
            AuditLogConfigUtil.logChanges(icsrCaseTracking, newValues, [:]
                    , Constants.AUDIT_LOG_INSERT,ViewHelper.getMessage(code, caseNumber, versionNumber, icsrCaseTracking?.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTracking?.recipient))
            sendResponse(200, message(code: "icsr.case.add.localCp.success", args: [caseNumber + " v" + versionNumber, executedConfiguration.reportName]).toString())
        } catch (Exception ex) {
            log.error("Failed to add manual case due to invalid data", ex)
            sendResponse(500, message(code: 'icsr.case.add.lacalCp.invalid', default: ex.message).toString())
        }
    }

    def listDevices() {
        String caseAndVersion = params.get('caseVersionNumber')
        String profileId = params.get('profileId')
        if (caseAndVersion && profileId) {
            String caseNumber = caseAndVersion.split(Constants.CASE_VERSION_SEPARATOR).first()
            Long version = caseAndVersion.split(Constants.CASE_VERSION_SEPARATOR).last().toLong()
            String caseId = sqlGenerationService.fetchCaseIdFromSource(caseNumber, version)
            IcsrProfileConfiguration icsrProfileConfiguration = IcsrProfileConfiguration.read(Long.parseLong(profileId))
            List caseProdList = []
            if (icsrProfileConfiguration.deviceReportable) {
                caseProdList = sqlGenerationService.checkCaseProductList(caseId, version, Tenants.currentId() as Long)
                if (caseProdList.any { it.PROD_HASH_CODE == "-1" }) {
                    render([status: 200, items: []] as JSON)
                    return
                } else {
                    render([status: 200, items: caseProdList.collect {[id: it.PROD_HASH_CODE, text: it.PRODUCT_NAME]}] as JSON)
                    return
                }
            } else {
                caseProdList = sqlGenerationService.checkProductList(caseId, version, Tenants.currentId() as Long)
                render([status: 200, items: caseProdList.collect {[id: it.PROD_HASH_CODE, text: it.PRODUCT_NAME]}] as JSON)
                return
            }
        } else {
            render([status: 200, items: []] as JSON)
        }
    }

    def listAuthorizationType() {
        String profileId = params.get('profileId')
        if (profileId) {
            List authList = []
            IcsrProfileConfiguration icsrProfileConfiguration = IcsrProfileConfiguration.read(Long.parseLong(profileId))

            if (icsrProfileConfiguration.authorizationTypes) {
                Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
                List<AuthorizationType> authorizationTypeList = []
                AuthorizationType.'pva'.withNewTransaction {
                    authorizationTypeList = AuthorizationType.'pva'.findAllByIdInListAndLangId(icsrProfileConfiguration.authorizationTypes, langId)
                }
                authorizationTypeList.collect { authList.add(id: it.id, text: it.name) }
                render([status: 200, items: authList] as JSON)
            } else {
                render([status: 200, items: []] as JSON)
            }
        } else {
            render([status: 200, items: []] as JSON)
        }
    }

    def listAuthorizationTypeForFilter() {
        render icsrScheduleService.getAuthType().collect {
            [id: it.name, authroizationType: it.name]
        } as JSON
    }

    def listApprovalNumber() {
        String caseAndVersion = params.get('caseVersionNumber')
        String profileId = params.get('profileId')
        String authId = params.get('authId')
        String prodHashCode = params.prodHashCode
        if (caseAndVersion && profileId && authId && prodHashCode) {
            String caseNumber = caseAndVersion.split(Constants.CASE_VERSION_SEPARATOR).first()
            Long versionNumber = caseAndVersion.split(Constants.CASE_VERSION_SEPARATOR).last().toLong()
            String caseId = sqlGenerationService.fetchCaseIdFromSource(caseNumber, versionNumber)
            IcsrProfileConfiguration icsrProfileConfiguration = IcsrProfileConfiguration.read(Long.parseLong(profileId))
            List approvalList = sqlGenerationService.checkApprovalNumber(caseId as Long, versionNumber, prodHashCode, icsrProfileConfiguration.recipientOrganization.organizationCountry, authId as Long, icsrProfileConfiguration.isJapanProfile, Tenants.currentId() as Long, icsrProfileConfiguration.multipleReport)
            if (approvalList.any { it.prodHashCode == "-1" }) {
                render([status: 200, items: []] as JSON)
                return
            } else {
                render([status: 200, items: approvalList] as JSON)
                return
            }
        } else {
            render([status: 200, items: []] as JSON)
            return
        }
    }

    //Put bulk download requests inside data base
    def prepareBulkDownloadIcsrReports(){
        User currentUser = userService?.getUser()
        def data = JSON.parse(params.data) as JSONArray
        if (BulkDownloadIcsrReports.findAll().size() < 30) {
            if (data.size() <= Constants.ICSR_BULK_DOWNLOAD_MAX_LIMIT) {
                log.info("Preparing Bulk Download ICSR reports for user: ${currentUser.fullName}")
                List<IcsrTrackingBulkDownloadDTO> icsrTrackingDTOList = []
                try {
                    data.each {
                        icsrTrackingDTOList.add((new IcsrTrackingBulkDownloadDTO(it)))
                    }
                    String downloadData = new JsonBuilder(["downloadData": icsrTrackingDTOList]).toPrettyString()
                    BulkDownloadIcsrReports bulkDownloadIcsrReports = new BulkDownloadIcsrReports(downloadData: downloadData, downloadBy: currentUser)
                    bulkDownloadIcsrReports = (BulkDownloadIcsrReports) CRUDService.save(bulkDownloadIcsrReports)
                } catch (Exception e) {
                    log.error("Error while saving bulkDownloadIcsrReports for user: ${currentUser.fullName}", e)
                    sendResponse(500, message(code: "icsr.reports.prepare.bulk.download.error").toString())
                    return
                }
                log.info("Successfuly saved bulkDownloadIcsrReports for user: ${currentUser.fullName}")
                sendResponse(200, message(code: "icsr.reports.preapare.bulk.download.message").toString())
                return
            } else {
                log.error("Number of reports selected for ICSR bulk download exceeded allowed limit for user: ${currentUser.fullName}")
                sendResponse(500, message(code: "icsr.reports.bulk.download.report.count.exceed.error", args: [data.size() > Constants.ICSR_BULK_DOWNLOAD_MAX_LIMIT ? Constants.ICSR_BULK_DOWNLOAD_MAX_LIMIT : Holders.config.icsr.profile.bulk.export.maxCount]).toString())
                return
            }
        } else {
            log.error("Prepare ICSR bulk download queue size reached maximum limit")
            sendResponse(500, message(code: "icsr.reports.bulk.download.configuration.size.exceed.error").toString())
            return
        }
    }

    def bulkDownLoadIcsrReports(String name){
        File file = new File(grailsApplication.config.tempDirectory + '/' + name)
        response.setContentType("application/zip");
        response.setHeader("Content-Length", String.valueOf(file.size()))
        response.addHeader("Content-Disposition", "attachment; filename=${name}.zip")

        OutputStream responseOutputStream = response.getOutputStream()
        responseOutputStream.write(Files.readAllBytes(file.toPath()))
        responseOutputStream.close()
        file.delete()
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM)
        String exportDate = sdf.format(new Date()) + " (GMT)"
        List<AuditTrailChildDTO> auditTrailChildDTOList = [new AuditTrailChildDTO("Output Format", Constants.BLANK_STRING, "ZIP"),
                                                        new AuditTrailChildDTO("File Name", Constants.BLANK_STRING, name+".ZIP"),
                                                        new AuditTrailChildDTO("Exported Date", "", exportDate)]
        utilService.createAuditLog(AuditTrail.Category.EXPORT.toString(), userService.currentUser, "Bulk Export ICSR Reports", AuditTrail.Category.EXPORT.displayName, "Bulk Download ICSR Reports", name+" exported in zip format", "", auditTrailChildDTOList)
        render(status: 200, text: name)
    }

    def updateIcsrCaseStatus() {
        String profileId
        String icsrTempQueryId
        String caseNumber
        String versionNumber

        if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
            if (!params.password) {
                sendResponse(500, message(code: "app.label.workflow.rule.fillLogon").toString());
                return
            }
            if (!ldapService.isLoginPasswordValid(userService.currentUser.username, params.password)) {
                sendResponse(500, message(code: "app.label.workflow.rule.approvl.fail").toString());
                return
            }
        }
        if (!params?.icsrCaseId) {
            sendResponse(500, message(code: "default.system.error.message").toString());
            return
        }
        try {
            User currentUser = userService.currentUser
            (profileId, icsrTempQueryId, caseNumber, versionNumber) = params?.icsrCaseId?.split("\\*\\*")
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
            IcsrCaseTracking icsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                icsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(Long.parseLong(icsrTempQueryId), caseNumber, Long.parseLong(versionNumber))
            }
            String formattedOldDueDate = outputFormat.format(icsrCaseTrackingInstance?.dueDate)
            Map oldValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(icsrCaseTrackingInstance)
            if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                oldValues.put("approvedBy", null)
                oldValues.put("approvedOn", null)
            }
            oldValues.put("submissionComments", null)
//            oldValues.put("submissionCommentsJ", null)
            oldValues.put("documentName", null)
            String comments = null
            if (grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)) {
                comments = Constants.APPROVED_BY + userService.currentUser.fullName + "\n" + Constants.APPROVED_ON + params.approvalDate?.toString() + "\n\n" + params.comment?.toString()
//                comments = Constants.APPROVED_BY + userService.currentUser.fullName + "\n" + Constants.APPROVED_ON + params.approvalDate?.toString() + "\n\n" + (params.caseSubmissionComments ? params.caseSubmissionComments.toString() : "")
            } else {
                comments = params.comment?.toString()
//                comments = params.caseSubmissionComments?.toString()
            }
            CaseSubmissionCO caseSubmissionCO = new CaseSubmissionCO()
            caseSubmissionCO.icsrCaseState = IcsrCaseStateEnum.valueOf(params.icsrCaseState)
            caseSubmissionCO.currentState = params.currentState
            caseSubmissionCO.comment = comments?.replaceAll("(?i)'","''")
//            caseSubmissionCO.commentJ = params.caseSubmissionCommentsJa?.replaceAll("(?i)'","''")
            caseSubmissionCO.commentJ = comments?.replaceAll("(?i)'","''")
            caseSubmissionCO.justificationId = params.justificationId ? params.long('justificationId') : null
            caseSubmissionCO.reportingDestinations = params.reportingDestinations
            caseSubmissionCO.icsrCaseId = icsrCaseTrackingInstance.uniqueIdentifier()
            caseSubmissionCO.profileName = icsrCaseTrackingInstance.profileName
            caseSubmissionCO.profileId = Long.parseLong(profileId)
            caseSubmissionCO.queryId = icsrTempQueryId
            caseSubmissionCO.caseNumber = caseNumber
            caseSubmissionCO.versionNumber = Long.parseLong(versionNumber)
            def scheduleDateJSON = JSON.parse(params.scheduleDateJSON)
            Date caseSubmissionDateUTC = null
            if(scheduleDateJSON.startDateTime)
                caseSubmissionDateUTC = Date.parse(DateUtil.SCHEDULE_DATE_JSON_FORMAT, JSON.parse(params.scheduleDateJSON).startDateTime)
            String timeZoneId = null
            if(scheduleDateJSON.timeZone)
                timeZoneId = JSON.parse(params.scheduleDateJSON).timeZone.name
            Date localDate = null
            if(caseSubmissionDateUTC)
                localDate = DateUtil.covertToDateWithTimeZone(caseSubmissionDateUTC, Constants.DateFormat.NO_TZ, timeZoneId)
            caseSubmissionCO.submissionDate = caseSubmissionDateUTC
            caseSubmissionCO.localSubmissionDate = localDate
            caseSubmissionCO.timeZoneId = timeZoneId
            caseSubmissionCO.submissionDocument = params?.file?.getBytes()
            caseSubmissionCO.submissionFilename = params?.filename
            caseSubmissionCO.processedReportId = icsrCaseTrackingInstance?.processedReportId
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy")
            Date parsedDate = inputFormat.parse(params.dueDate)
            String formattedDateString = outputFormat.format(parsedDate)
            caseSubmissionCO.dueDate = outputFormat.parse(formattedDateString)
            caseSubmissionCO.updatedBy = currentUser.fullName?:currentUser.username
            if(icsrProfileAckService.validateSubmissionDate(localDate, icsrCaseTrackingInstance.generationDate, timeZoneId)) {
                sendResponse(500, message(code: "submission.date.later.error").toString())
                return
            }

            sqlGenerationService.updatingIcsrStatusAndDate(caseSubmissionCO, icsrCaseTrackingInstance)
            IcsrCaseTracking newIcsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(Long.parseLong(icsrTempQueryId), caseNumber, Long.parseLong(versionNumber))
            }
            String formattedUpdatedDueDate = outputFormat.format(newIcsrCaseTrackingInstance?.dueDate)
            if(formattedUpdatedDueDate != formattedOldDueDate) {
                icsrScheduleService.calculateDueDateForManual(newIcsrCaseTrackingInstance.caseId,newIcsrCaseTrackingInstance.versionNumber, newIcsrCaseTrackingInstance.tenantId, new Date(), Constants.DUE_DATE_TYPE_MANUAL)
            }

            Map newValues = icsrProfileAckService.icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
            if(grailsApplication.config.getProperty('icsr.case.submission.user.revalidate', Boolean)){
                newValues.put("approvedBy", userService.currentUser.fullName)
                newValues.put("approvedOn", params.approvalDate?.toString())
            }
            newValues.put("submissionComments", params.comment?.toString())
//            newValues.put("submissionComments", params.caseSubmissionComments?.toString())
//            newValues.put("submissionCommentsJ", params.caseSubmissionCommentsJa?.toString())
            newValues.put("documentName", params.filename?.toString())
            AuditLogConfigUtil.logChanges(icsrCaseTrackingInstance, newValues, oldValues
                    , Constants.AUDIT_LOG_UPDATE,ViewHelper.getMessage("auditLog.entityValue.icsr.changes", caseNumber, versionNumber, icsrCaseTrackingInstance.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTrackingInstance.recipient))
            sendResponse(200, message(code: "icsr.report.unsubmitting.status").toString())
            return
        } catch (Exception e) {
            log.error("Failed to un-submit the Icsr Report", e)
            sendResponse(500, message(code: "icsr.report.unsubmitting.status.error").toString())
            return
        }
    }

}
