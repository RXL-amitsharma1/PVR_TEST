package com.rxlogix

import com.rxlogix.commandObjects.CaseSubmissionCO
import com.rxlogix.config.*
import com.rxlogix.dto.CaseAckSubmissionDTO
import com.rxlogix.dto.CaseStateUpdateDTO
import com.rxlogix.dynamicReports.reportTypes.XMLReportOutputBuilder
import com.rxlogix.enums.DistributionChannelEnum
import com.rxlogix.enums.IcsrCaseStateEnum
import com.rxlogix.enums.IcsrProfileSubmissionDateOptionEnum
import com.rxlogix.enums.LateEnum
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.mapping.IcsrCaseMessageQueue
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import groovy.sql.Sql
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import groovyx.gpars.GParsPool
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import java.nio.file.*
import com.rxlogix.customException.NoDataFoundXmlException
import grails.util.Holders
import javax.sql.DataSource
import java.sql.SQLException
import com.rxlogix.util.MiscUtil
import java.text.SimpleDateFormat

@Transactional
class IcsrProfileAckService {

    def grailsApplication
    def reportExecutorService
    def emailService
    def userService
    def reportSubmissionService
    def PVGatewayService
    def AxwayService
    def dynamicReportService
    def icsrXmlService
    def icsrReportService
    def sqlGenerationService
    def utilService
    def e2BAttachmentService
    DataSource dataSource_pva

    private static final String ARCHIVE_FOLDER = "archive"
    private static final String ERROR_FOLDER = "error"
    private static final String APPLICATION_ACCEPTED = "01"
    private static final String APPLICATION_ERROR = "02"
    private static final String APPLICATION_REJECTED = "03"
    private static final String COMMIT_ACCEPTED = "01"
    private static final String COMMIT_REJECTED = "02"
    private static final String ATTACHMENT_APPLICATION_ACCEPTED = "0"
    private static final String ATTACHMENT_GATEWAY_TRANSMISSION_SUCCESSFUL = "Attachment Transmitted Successfully"
    private static final String ATTACHMENT_GATEWAY_TRANSMISSION_ERROR = "Attachment Transmission Error"
    private static final String GATEWAY_TRANSMISSION_SUCCESSFUL = "Transmitted Successfully"
    private static final String GATEWAY_TRANSMISSION_ERROR = "Transmission Error"
    private static final String EMPTY_STRING = ""
    private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("dd-MMM-yyyy")

    void readICSRProfileAckFiles() {
        Set<String> incomingUniqueFolders = grailsApplication.config.getProperty('pv.app.e2b.incoming.folders.path', Set)
        Integer threadPoolSize = grailsApplication.config.getProperty('icsr.ack.executor.size', Integer, 5)
        GParsPool.withPool(threadPoolSize, new Thread.UncaughtExceptionHandler() {
            @Override
            void uncaughtException(Thread failedThread, Throwable throwable) {
                log.error("Error processing background ack thread ${failedThread.name}", throwable)
            }
        }, {
            incomingUniqueFolders?.eachParallel {  String folder ->
                User.withNewSession {
                        executeAckFiles(folder)
                }
            }
        })
        log.debug("Completed Ack Files Execution")
    }

    void executeAckFiles(String incomingFolder) {
        List<File> fileList = getFilesFromFolder(incomingFolder)
        fileList.each {
            if (it.isDirectory() || !(it.name.toLowerCase().endsWith('.xml') || it.name.toLowerCase().endsWith('.ack'))) {
                return
            }
            try {
                readAckFileAndMarkStatus(incomingFolder, it)
            } catch (Exception ex) {
                log.error("Fatal error while extracting data from file ${it}.", ex)
                try {
                    moveFileToErrorAndNotify((incomingFolder + File.separator + ERROR_FOLDER), it)
                } catch(e){
                    log.trace("Error while moving file to error folder: ${e.message}")
                }
            }
        }
    }

    void readAckFileAndMarkStatus(String incomingFolder, File ackFile) {
        log.info("Reading file with filename = "+ackFile)
        def ackXmlNode
        //return false means the ack file is R2
        String fileNameIdentifier = e2BAttachmentService.evaluatePathValue(ackFile.text, grailsApplication.config.getProperty("icsr.case.ack.attachment.message.identifier.path"))
        if(fileNameIdentifier) {
            ExecutedIcsrTemplateQuery executedIcsrTemplateQuery = ExecutedIcsrTemplateQuery.read(fileNameIdentifier.split(XMLReportOutputBuilder.XML_IDENTIFIER_TOKEN).first().toString().toLong())
            String casenumber = fileNameIdentifier.substring(fileNameIdentifier.indexOf(XMLReportOutputBuilder.XML_IDENTIFIER_TOKEN)+1,fileNameIdentifier.lastIndexOf(XMLReportOutputBuilder.XML_IDENTIFIER_TOKEN))
            Integer versionNumber = fileNameIdentifier.split(XMLReportOutputBuilder.XML_IDENTIFIER_TOKEN).last().replace(Constants.PDF_EXT, "").toInteger()
            String attachmentAckMsgStatus = e2BAttachmentService.evaluatePathValue(ackFile.text, grailsApplication.config.getProperty("icsr.case.ack.attachment.code.path"))
            ExecutedIcsrProfileConfiguration executedProfile = executedIcsrTemplateQuery.executedConfiguration
            log.info("XML Paresed with casenumber: ${casenumber}, versionNumber: ${versionNumber} and attachmentAckMsgStatus: ${attachmentAckMsgStatus}")
            String status = null
            Tenants.withId(executedProfile.tenantId as Integer) {
                String destinationFilename = executedIcsrTemplateQuery.id + "-" + casenumber + "-" +versionNumber
                String pdfFilename = dynamicReportService.getReportsDirectory() + File.separator + destinationFilename
                if(attachmentAckMsgStatus?.equals(ATTACHMENT_APPLICATION_ACCEPTED)) {
                    status = IcsrCaseStateEnum.COMMIT_ACCEPTED.toString()
                }else {
                    status = IcsrCaseStateEnum.COMMIT_REJECTED.toString()
                }
                String errorTxt = e2BAttachmentService.evaluatePathValue(ackFile.text, grailsApplication.config.getProperty("icsr.case.ack.attachment.message.text.path"))
                Date attachmentAckReceiveDate = null
                if (executedIcsrTemplateQuery?.distributionChannelName == DistributionChannelEnum.PV_GATEWAY) {
                    attachmentAckReceiveDate = PVGatewayService.getAckReceiveDateForFile(ackFile.name)
                } else if (executedIcsrTemplateQuery?.distributionChannelName == DistributionChannelEnum.EXTERNAL_FOLDER) {
                    attachmentAckReceiveDate = AxwayService.getAckReceiveDateForFile(ackFile.name)
                }
                if (!attachmentAckReceiveDate) {
                    attachmentAckReceiveDate = new Date()
                }
                IcsrCaseTracking icsrTrackingRecord = null
                IcsrCaseTracking.withNewSession {
                    icsrTrackingRecord = getIcsrTrackingRecord(executedIcsrTemplateQuery.id, casenumber, versionNumber)
                }
                sqlGenerationService.insertAttachmentDetail(icsrTrackingRecord?.tenantId, icsrTrackingRecord?.processedReportId, null, ackFile, attachmentAckReceiveDate, status)
                CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedIcsrTemplateQuery, caseNumber: casenumber, versionNumber: versionNumber, status: status, previousStatus : icsrTrackingRecord.e2BStatus, error: errorTxt, attachmentAckFileName: incomingFolder + File.separator + ARCHIVE_FOLDER + File.separator + ackFile.name, dateAckRecievedAttach: attachmentAckReceiveDate, processedReportId: icsrTrackingRecord?.processedReportId)
                reportExecutorService.changeIcsrCaseStatus(caseStateUpdateDTO, icsrTrackingRecord.e2BStatus, status)
                icsrReportService.excludeAttachment(icsrTrackingRecord, executedIcsrTemplateQuery, status)

                if (executedProfile.autoSubmit && executedProfile.submissionDateFrom && !(status in [IcsrCaseStateEnum.PARSER_REJECTED.toString(),IcsrCaseStateEnum.COMMIT_REJECTED.toString()])) {
                    Date submissionDate = null
                    if ((executedProfile.submissionDateFrom == IcsrProfileSubmissionDateOptionEnum.MDN) || (executedProfile.submissionDateFrom == IcsrProfileSubmissionDateOptionEnum.MDNPos && status == IcsrCaseStateEnum.COMMIT_ACCEPTED.toString())) {
                        if(executedIcsrTemplateQuery?.distributionChannelName == DistributionChannelEnum.PV_GATEWAY){
                            submissionDate = PVGatewayService.getMDNDate(pdfFilename)
                        }else if(executedIcsrTemplateQuery?.distributionChannelName == DistributionChannelEnum.EXTERNAL_FOLDER){
                            submissionDate = AxwayService.getMDNDate(pdfFilename)
                        }
                    } else if (executedProfile.submissionDateFrom == IcsrProfileSubmissionDateOptionEnum.ACK && status == IcsrCaseStateEnum.COMMIT_ACCEPTED.toString()) {
                        submissionDate = caseStateUpdateDTO.dateAckRecievedAttach
                    }
                    String timeZoneId = executedProfile.preferredTimeZone ?: "UTC"
                    Date localDate = DateUtil.covertToDateWithTimeZone(submissionDate, Constants.DateFormat.NO_TZ, timeZoneId)
                    if (submissionDate) {
                        submitCase(executedProfile, executedIcsrTemplateQuery.id, casenumber, versionNumber, submissionDate, "Application", localDate, timeZoneId)
                    }
                }
            }
        }else {
            String xmlErrors = icsrXmlService.validateXml(ackFile, grailsApplication.config.getProperty('icsr.case.ack.xsd'))
            if (xmlErrors) {
                log.error("Error while validating received acknowledgement file ${ackFile}, ${xmlErrors}")
                moveFileToErrorAndNotify((incomingFolder + File.separator + ERROR_FOLDER), ackFile)
                return
            }
            File downgradeFile = downgradeAckFile(ackFile, incomingFolder)
            log.info("Downgraded ACK file successful ${ackFile?.name}")
            ackXmlNode = new XmlParser().parseText(downgradeFile.text)
            String messageIdentifier = getMessageIdentifier(ackXmlNode) ?: getEmdrMessageIdentifier(ackXmlNode)
            String ackMsgStatus = getAckMessageStatus(ackXmlNode)
            String ackStatus = getAckStatus(ackXmlNode)
            String reportAckCode = getReportAckCode(ackXmlNode)
            String transAckCode = getTransmissionAckCode(ackXmlNode)
            String localReportNumber = getLocalReportNumber(ackXmlNode) ?: getEmdrLocalReportNumber(ackXmlNode)
            String safetyReportId = getSafetyReportId(ackXmlNode)
            String icsrMsgNumber = getIcsrMessageNumber(ackXmlNode)
            String localMsgNumber = getLocalMessageNumber(ackXmlNode)
            String emdrAckStatus = getEmdrAckStatus(ackXmlNode)
            String status = null
            log.info("XML Paresed with messageIdentifier: ${messageIdentifier}, ackMsgStatus: ${ackMsgStatus} and ackStatus: ${ackStatus}")
            ExecutedIcsrTemplateQuery executedIcsrTemplateQuery = ExecutedIcsrTemplateQuery.read(messageIdentifier.split(XMLReportOutputBuilder.XML_IDENTIFIER_TOKEN).first().toString().toLong())
            String casenumber = messageIdentifier.substring(messageIdentifier.indexOf(XMLReportOutputBuilder.XML_IDENTIFIER_TOKEN) + 1, messageIdentifier.lastIndexOf(XMLReportOutputBuilder.XML_IDENTIFIER_TOKEN))
            Integer versionNumber = messageIdentifier.split(XMLReportOutputBuilder.XML_IDENTIFIER_TOKEN).last().toInteger()
            ExecutedIcsrProfileConfiguration executedProfile = MiscUtil.unwrapProxy(executedIcsrTemplateQuery.executedConfiguration)
            List caseSubmissionList = null
            String pmdaNumber = null
            Long authId = null
            IcsrCaseSubmission.withNewSession {
                Long processedReportIdList = IcsrCaseSubmission.fetchIcsrCaseSubmissionByCaseNoAndVersionNo(executedProfile.reportName, executedIcsrTemplateQuery.id, casenumber, versionNumber).get()
                caseSubmissionList = IcsrCaseSubmission.findAllByProcessedReportId(processedReportIdList, [sort: 'e2bProcessId', order: 'asc'])
            }
            if (caseSubmissionList.any { it.e2bStatus == IcsrCaseStateEnum.COMMIT_ACCEPTED.toString() || it.e2bStatus == IcsrCaseStateEnum.COMMIT_RECEIVED.toString() }) {
                log.error("The Case number ${casenumber} for profile ${executedProfile.reportName} has already got Positive ACK")
                throw new Exception("The Case number ${casenumber} for profile ${executedProfile.reportName} has already got Positive ACK")
                return
            }
            Integer ifTransmitted = null
            IcsrCaseSubmission.'pva'.withNewSession{
                ifTransmitted = reportExecutorService.checkIfTransmitted(executedProfile?.tenantId, new CaseSubmissionCO(queryId: executedIcsrTemplateQuery.id, caseNumber: casenumber, versionNumber: versionNumber))
            }
            if (grailsApplication.config.getProperty('icsr.case.workflow.transmitted.check', Boolean) && !ifTransmitted) {
                log.warn("We have not got status as transmitted yet for ${messageIdentifier} so will re-process later")
                downgradeFile?.delete()
                return
            }
            Tenants.withId(executedProfile.tenantId as Integer) {
                IcsrCaseTracking icsrTrackingRecord = null
                IcsrCaseTracking.withNewSession {
                    icsrTrackingRecord = getIcsrTrackingRecord(executedIcsrTemplateQuery.id, casenumber, versionNumber)
                }
                Date fileDate = icsrTrackingRecord?.transmissionDate
                Boolean isJapanProfile = icsrTrackingRecord?.isJapanProfile()
                String currentSenderIdentifier = executedProfile?.getSenderIdentifier()
                ReportResult reportResult = executedIcsrTemplateQuery.getReportResult()
                String r3XmlFileName = null
                if (reportResult) {
                    r3XmlFileName = dynamicReportService.getTransmittedR3XmlFileName(reportResult, casenumber)
                } else {
                    CaseResultData caseResultData = CaseResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(executedIcsrTemplateQuery.id, casenumber, versionNumber)
                    r3XmlFileName = dynamicReportService.getTransmittedR3XmlFileName(caseResultData, currentSenderIdentifier, casenumber, versionNumber as Long, executedIcsrTemplateQuery.id, fileDate, isJapanProfile)
                }
                if (ackMsgStatus?.equals(APPLICATION_ACCEPTED) &&
                        ackStatus.equals(COMMIT_ACCEPTED)) {
                    status = IcsrCaseStateEnum.COMMIT_ACCEPTED.toString()
                }
                if (ackMsgStatus?.equals(APPLICATION_ERROR) &&
                        ackStatus.equals(COMMIT_ACCEPTED)) {
                    status = IcsrCaseStateEnum.COMMIT_REJECTED.toString()
                }

                if (ackMsgStatus?.equals(APPLICATION_ERROR) &&
                        ackStatus.equals(COMMIT_REJECTED)) {
                    sendIcsrProfileCommitRejectedEmailTo(executedProfile, casenumber)
                    status = IcsrCaseStateEnum.COMMIT_REJECTED.toString()
                }
                if (ackMsgStatus?.equals(APPLICATION_REJECTED)) {
                    sendIcsrProfileApplicationRejectedEmailTo(executedProfile, casenumber)
                    status = IcsrCaseStateEnum.PARSER_REJECTED.toString()
                }

                String errorTxt = EMPTY_STRING
                if(ackMsgStatus == APPLICATION_REJECTED){
                    errorTxt = getErrorMsgForApplicationRejected(ackXmlNode)
                }else {
                    errorTxt = getErrorMsg(ackXmlNode)
                }

                Date ackReceiveDate = null
                if (executedIcsrTemplateQuery?.distributionChannelName == DistributionChannelEnum.PV_GATEWAY) {
                    ackReceiveDate = PVGatewayService.getAckReceiveDateForFile(ackFile.name)
                } else if (executedIcsrTemplateQuery?.distributionChannelName == DistributionChannelEnum.EXTERNAL_FOLDER) {
                    ackReceiveDate = AxwayService.getAckReceiveDateForFile(ackFile.name)
                }
                if (!ackReceiveDate) {
                    ackReceiveDate = new Date()
                }
                Long processedReportId = icsrTrackingRecord?.processedReportId
                Long tenantId = icsrTrackingRecord?.tenantId
                Long caseId = icsrTrackingRecord?.caseId
                String currentState = icsrTrackingRecord?.e2BStatus
                try {
                    sqlGenerationService.insertAckDetail(tenantId, processedReportId, ackFile)
                    CaseAckSubmissionDTO caseAckSubmissionDTO = new CaseAckSubmissionDTO(tenantId: tenantId, caseId: caseId, versionNumber: versionNumber, processedReportId: processedReportId, icsrMessageNumber: icsrMsgNumber, transmissionAckCode: transAckCode, safetyReportId: safetyReportId, localReportNumber: localReportNumber, localMessageNumber: localMsgNumber, reportAckCode: reportAckCode, ackMessageComment: errorTxt)
                    sqlGenerationService.insertIntoGTTforAck(caseAckSubmissionDTO)
                } catch (Exception ex) {
                    log.error("Failed to call proc : ${ex.message}")
                    ex.printStackTrace()
                }

                XMLResultData xmlResultData = XMLResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(icsrTrackingRecord.exIcsrTemplateQueryId, icsrTrackingRecord.caseNumber, icsrTrackingRecord.versionNumber)
                if (xmlResultData?.isAttachmentExist && status == IcsrCaseStateEnum.COMMIT_ACCEPTED.toString()) {
                    status = IcsrCaseStateEnum.COMMIT_RECEIVED.toString()
                    CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedIcsrTemplateQuery, caseNumber: casenumber, versionNumber: versionNumber, status: status, error: errorTxt, ackFileName: incomingFolder + File.separator + ARCHIVE_FOLDER + File.separator + ackFile.name, ackReceiveDate: ackReceiveDate, processedReportId: processedReportId)
                    reportExecutorService.changeIcsrCaseStatus(caseStateUpdateDTO, currentState, status)
                } else {
                    if(executedProfile?.xsltName == Constants.EMDR) {
                        if(emdrAckStatus?.equals(Constants.PASSED)){
                            status = IcsrCaseStateEnum.COMMIT_ACCEPTED.toString()
                        } else if (emdrAckStatus?.equals(Constants.FAILED)){
                            status = IcsrCaseStateEnum.COMMIT_REJECTED.toString()
                            errorTxt = getEmdrAckErrorMssg(ackXmlNode)
                        } else {
                            status = IcsrCaseStateEnum.PARSER_REJECTED.toString()
                        }
                    }
                    if (executedProfile?.recipientType in [Constants.ICSR_UNIT_CONF_REGULATORY_AUTHORITY, '規制当局'] && executedProfile?.recipientCountry in ['JAPAN', '日本'] && localReportNumber != null) {
                        pmdaNumber = localReportNumber.split('-')?.size() > 1 ? localReportNumber.split('-')[1] : null
                        authId = icsrTrackingRecord?.authId
                    }
                    CaseStateUpdateDTO caseStateUpdateDTO = new CaseStateUpdateDTO(executedIcsrTemplateQuery: executedIcsrTemplateQuery, caseNumber: casenumber, versionNumber: versionNumber, status: status, error: errorTxt, ackFileName: incomingFolder + File.separator + ARCHIVE_FOLDER + File.separator + ackFile.name, ackReceiveDate: ackReceiveDate, processedReportId: processedReportId)
                    reportExecutorService.changeIcsrCaseStatus(caseStateUpdateDTO, currentState, status, pmdaNumber, authId)
                    icsrReportService.excludeAttachment(icsrTrackingRecord, executedIcsrTemplateQuery, status)
                    try {
                        if (executedProfile?.recipientType == 'PQC System' && (localMsgNumber != null || localReportNumber != null)) {
                            String localPQCNumber = localMsgNumber ? localMsgNumber : localReportNumber
                            IcsrCaseMessageQueue.'pvcm'.withNewSession {
                                Long intakeCaseId = fetchIntakeCaseId(caseId, versionNumber, tenantId)
                                new IcsrCaseMessageQueue(intakeCaseId, Constants.PQC_CASE_NUM, localPQCNumber, new Date(), new Date(), null, null, false).'pvcm'.save([flush:true,failOnError: true])
                                log.info("ICSRProfileAckJob : Entry for caseId ${caseId} in IcsrCaseMessageQueue is done.")
                            }
                        } else if (pmdaNumber != null){
                            Map<String, String> productDetails = getLicenseIdApprovalNumberApprovalTypeId(authId)
                            JSON pmdaJson = [pmdaNumber: pmdaNumber, licenseId: productDetails.licenseId, approvalNumber: productDetails.approvalNumber, approvalTypeId: productDetails.approvalTypeId] as JSON
                            IcsrCaseMessageQueue.'pvcm'.withNewSession {
                                Long intakeCaseId = fetchIntakeCaseId(caseId, versionNumber, tenantId)
                                new IcsrCaseMessageQueue(intakeCaseId, Constants.PMDA_NUM, pmdaJson.toString(), new Date(), new Date(), null, null, false).'pvcm'.save([flush:true,failOnError: true])
                                log.info("ICSRProfileAckJob : Entry for authId ${authId} and pmdaNumber ${pmdaNumber} in IcsrCaseMessageQueue is done.")
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error while saving data for caseId : ${caseId} into IcsrCaseMessageQueue : " + ex.printStackTrace())
                    }
                    log.info("ICSRProfileAckJob : Ack Processing completed")
                    if (executedProfile.autoSubmit && executedProfile.submissionDateFrom && !(status in [IcsrCaseStateEnum.PARSER_REJECTED.toString(), IcsrCaseStateEnum.COMMIT_REJECTED.toString()])) {
                        log.info("ICSRProfileAckJob : Profile is Auto Submit")
                        Date submissionDate = null
                        if ((executedProfile.submissionDateFrom == IcsrProfileSubmissionDateOptionEnum.MDN) || (executedProfile.submissionDateFrom == IcsrProfileSubmissionDateOptionEnum.MDNPos && status == IcsrCaseStateEnum.COMMIT_ACCEPTED.toString())) {
                            log.info("ICSRProfileAckJob : submission Date From"+executedProfile.submissionDateFrom)
                            if (executedIcsrTemplateQuery?.distributionChannelName == DistributionChannelEnum.PV_GATEWAY) {
                                log.info("ICSRProfileAckJob : Fetching MDN Date from PVGateway")
                                submissionDate = PVGatewayService.getMDNDate(r3XmlFileName)
                            } else if (executedIcsrTemplateQuery?.distributionChannelName == DistributionChannelEnum.EXTERNAL_FOLDER) {
                                log.info("ICSRProfileAckJob : Fetching MDN Date from External Service")
                                submissionDate = AxwayService.getMDNDate(r3XmlFileName)
                            }
                        } else if (executedProfile.submissionDateFrom == IcsrProfileSubmissionDateOptionEnum.ACK && status == IcsrCaseStateEnum.COMMIT_ACCEPTED.toString()) {
                            log.info("ICSRProfileAckJob : submission Date From"+executedProfile.submissionDateFrom)
                            submissionDate = caseStateUpdateDTO.ackReceiveDate
                        }
                        log.info("ICSRProfileAckJob : Converting timzone")
                        String timeZoneId = executedProfile.preferredTimeZone ?: "UTC"
                        log.info("ICSRProfileAckJob : timeZoneId = ${timeZoneId}")
                        log.info("ICSRProfileAckJob : submissionDate = ${submissionDate} and Constants.DateFormat.NO_TZ = ${Constants.DateFormat.NO_TZ}")
                        Date localDate = DateUtil.covertToDateWithTimeZone(submissionDate, Constants.DateFormat.NO_TZ, timeZoneId)
                        log.info("ICSRProfileAckJob : SubmittingCase for submissionDate = ${submissionDate} And loalDate = ${localDate}")
                        if (submissionDate) {
                            log.info("ICSRProfileAckJob : submission Started")
                            submitCase(executedProfile, executedIcsrTemplateQuery.id, casenumber, versionNumber, submissionDate, "Application", localDate, timeZoneId)
                            log.info("ICSRProfileAckJob : submission Completed")
                        }
                    }
                    log.info("ICSRProfileAckJob : Ack Processing JOB completed")
                }
            }
        }
        moveFile((incomingFolder + File.separator + ARCHIVE_FOLDER), ackFile)
    }

    void submitCase(BaseConfiguration profile, Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber, Date submissionDate, String username, Date localSubmissionDate, String timeZoneId) {
        IcsrCaseTracking icsrCaseTracking = null
        IcsrCaseTracking.'pva'.withNewSession {
            icsrCaseTracking = getIcsrTrackingRecordWithoutPva(exIcsrTemplateQueryId, caseNumber, versionNumber)
        }
        if (!icsrCaseTracking) {
            log.error("Failed to submit, could not find Icsr Case Tracking Record for ${exIcsrTemplateQueryId}, ${caseNumber}, ${versionNumber}")
            return
        }
        try {
            Map oldValues = icsrCaseTrackingMapForAuditLog(icsrCaseTracking)
            CaseSubmissionCO caseSubmissionCO = new CaseSubmissionCO()
            caseSubmissionCO.icsrCaseId = icsrCaseTracking.id
            caseSubmissionCO.profileId = profile.id
            caseSubmissionCO.queryId = exIcsrTemplateQueryId
            caseSubmissionCO.caseNumber = caseNumber
            caseSubmissionCO.versionNumber = versionNumber
            caseSubmissionCO.profileName = profile.reportName
            caseSubmissionCO.icsrCaseState = IcsrCaseStateEnum.SUBMITTED
            caseSubmissionCO.comment =  ViewHelper.getMessage("app.auto.submit.comment")

            caseSubmissionCO.late = LateEnum.NOT_LATE
            caseSubmissionCO.submissionDate = submissionDate
            if (profile instanceof ExecutedIcsrProfileConfiguration) {
                caseSubmissionCO.reportingDestinations = profile.recipientOrganizationName
            } else {
                caseSubmissionCO.reportingDestinations = profile.recipientOrganization.unitName
            }
            caseSubmissionCO.processedReportId = icsrCaseTracking.processedReportId
            caseSubmissionCO.updatedBy = username
            caseSubmissionCO.localSubmissionDate = localSubmissionDate
            caseSubmissionCO.timeZoneId = timeZoneId

            reportSubmissionService.submitIcsrCase(profile.tenantId, caseSubmissionCO)
//            IcsrCaseTracking newIcsrCaseTrackingInstance = null
//            IcsrCaseTracking.'pva'.withNewSession {
//                newIcsrCaseTrackingInstance = getIcsrTrackingRecordWithoutPva(exIcsrTemplateQueryId, caseNumber, versionNumber)
//            }
            //Map newValues = icsrCaseTrackingMapForAuditLog(newIcsrCaseTrackingInstance)
//            AuditLogConfigUtil.logChanges(icsrCaseTracking, newValues, oldValues
//                    , Constants.AUDIT_LOG_UPDATE,ViewHelper.getMessage("auditLog.entityValue.icsr.changes", icsrCaseTracking?.caseNumber, icsrCaseTracking?.versionNumber, icsrCaseTracking?.profileName, ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId)?.executedTemplate?.name, icsrCaseTracking?.recipient))
        } catch (Exception e) {
            log.error("Failed to submitted", e)
        }
    }

    Long fetchIntakeCaseId(Long caseId, Long versionNumber, Long tenantId) {
        Sql sql = new Sql(utilService.getReportConnection())
        Long intakeCaseId = 0L
        try {
            intakeCaseId = sql.firstRow("select intake_case_id from v_tx_identification where case_id = ? and version_num = ? and tenant_id = ?", [caseId, versionNumber, tenantId])["INTAKE_CASE_ID"]
        } finally {
            sql?.close()
        }
        return intakeCaseId
    }

    List<File> getFilesFromFolder(String incomingFolder) {
        def baseDir = new File(incomingFolder)
        // One minute old file read
        baseDir.listFiles()?.findAll { it.lastModified() < (System.currentTimeMillis() - (1 * 60 * 1000)) }
    }

    String getAckStatus(def rootXmlNode) {
        String xpath = grailsApplication.config.getProperty('icsr.case.ack.message.status.path')
        return Eval.x(rootXmlNode, "x.${xpath}")
    }

    String getMessageIdentifier(def rootXmlNode) {
        String xpath = grailsApplication.config.getProperty('icsr.case.ack.message.identifier.path')
        return Eval.x(rootXmlNode, "x.${xpath}")
    }

    String getCaseNumber(def rootXmlNode) {
        return rootXmlNode.acknowledgment.reportacknowledgment.localreportnumb.text()
    }

    String getErrorMsg(def rootXmlNode) {
        return rootXmlNode.acknowledgment.reportacknowledgment.errormessagecomment.text()
    }

    String getErrorMsgForApplicationRejected(def rootXmlNode) {
        return rootXmlNode.acknowledgment.reportacknowledgment.errormessagecomment.text() ?: rootXmlNode.acknowledgment.messageacknowledgment.parsingerrormessage.text()
    }

    String getReportAckCode(def rootXmlNode) {
        String xpath = grailsApplication.config.getProperty('icsr.case.ack.report.ack.code.path')
        return Eval.x(rootXmlNode, "x.${xpath}")
    }

    String getTransmissionAckCode(def rootXmlNode) {
        String xpath = grailsApplication.config.getProperty('icsr.case.ack.transmission.ack.code.path')
        return Eval.x(rootXmlNode, "x.${xpath}")
    }

    String getLocalReportNumber(def rootXmlNode) {
        String xpath = grailsApplication.config.getProperty('icsr.case.ack.local.report.number.path')
        return Eval.x(rootXmlNode, "x.${xpath}")
    }

    String getSafetyReportId(def rootXmlNode) {
        String xpath = grailsApplication.config.getProperty('icsr.case.ack.safety.report.id.path')
        return Eval.x(rootXmlNode, "x.${xpath}")
    }

    String getIcsrMessageNumber(def rootXmlNode) {
        String xpath = grailsApplication.config.getProperty('icsr.case.ack.icsr.message.number.path')
        return Eval.x(rootXmlNode, "x.${xpath}")
    }

    String getLocalMessageNumber(def rootXmlNode) {
        String xpath = grailsApplication.config.getProperty('icsr.case.ack.local.message.number.path')
        return Eval.x(rootXmlNode, "x.${xpath}")
    }

    //This method will fetch and return the value in coreId(i.e messageIdentifier) tag of EMDR Ack file
    String getEmdrMessageIdentifier(Node rootXmlNode) {
        String xpath = grailsApplication.config.getProperty('icsr.case.emdr.ack.message.identifier.path')
        return  Eval.x(rootXmlNode, "x.${xpath}")
    }

    //This method will fetch and return the value in status tag of EMDR Ack file
    String getEmdrAckStatus(Node rootXmlNode) {
        String xpath = grailsApplication.config.getProperty('icsr.emdr.ack.status.identifier.path')
        return Eval.x(rootXmlNode, "x.${xpath}")
    }

    //This method will fetch and return the value in errorMessage tag of the EMDR Ack file
    String getEmdrAckErrorMssg(Node rootXmlNode){
        String xpath = grailsApplication.config.getProperty('icsr.case.emdr.ack.error.message.path')
        return  Eval.x(rootXmlNode, "x.${xpath}")
    }

    String getEmdrLocalReportNumber(Node rootXmlNode){
        String xpath = grailsApplication.config.getProperty('icsr.case.emdr.ack.local.report.numb.path')
        return  Eval.x(rootXmlNode, "x.${xpath}")
    }

    File downgradeAckFile(File file, String path) {
        Path destinationPath = Paths.get(path, ARCHIVE_FOLDER)
        if (!Files.exists(destinationPath)) {
            log.error("Creating folder as folder doesn't exist for ${destinationPath}")
            destinationPath.toFile().mkdir()
        }
        Path r2FilePath = Paths.get(path, ARCHIVE_FOLDER, (file.name.replace('.xml', '_r2.xml')))
        if (Files.exists(r2FilePath)) {
            log.error("File on path already exist so need to move existing files on path ${r2FilePath}")
            moveExistingFile(r2FilePath)
        }
        String folderName = path.split(File.separator).last()
        String ackXsltFilePath = grailsApplication.config.getProperty("icsr.case.ack.xslts.options.${folderName}") ?: grailsApplication.config.getProperty('icsr.case.ack.xslts.options.DEFAULT')
        log.info("Downgrading ack Using ${ackXsltFilePath} xslt for ${path} and file ${file}")
        return icsrXmlService.transform(file, ackXsltFilePath, r2FilePath.toFile())
    }

    private void moveExistingFile(Path path) {
        long i = 0
        boolean success = false
        while (!success) {
            i++
            try {
                Files.move(path,
                        Paths.get(path.toString() + '-' + i))
                success = true
            } catch (FileAlreadyExistsException e) {
                log.trace("Error while moving file due to ${e.message}")
            }
        }
    }

    String getAckMessageStatus(def rootXmlNode) {
        return rootXmlNode.acknowledgment.messageacknowledgment.transmissionacknowledgmentcode.text()
    }

    void sendIcsrAckFileFailureEmailTo(File file) {
        Locale locale = userService.getCurrentUser()?.preference?.locale
        //ICSR Profile Generation : Send failure email for all case number whose r3xml is not generated successfully
        String[] recipients = grailsApplication.config.getProperty('icr.case.admin.emails').split(',')
        String emailSubject = ViewHelper.getMessage("app.emailService.acknowledgement.error.subject.label", file.name)
        String emailBody = ViewHelper.getMessage("app.label.hi") + "<br><br>"
        emailBody += ViewHelper.getMessage("app.emailService.acknowledgement.error.message.label", file.name)
        emailBody += "<br><br>"
        if (locale?.language != 'ja') {
            emailBody += ViewHelper.getMessage("app.label.thanks") + "," + "<br>"
        }
        emailBody += ViewHelper.getMessage("app.label.pv.reports")
        emailBody = "<span style=\"font-size: 14px;\">" + emailBody + "</span>"
        List files = []
        files.add([type: "application/xml",
                   name: file.name,
                   data: file.getBytes()
        ])
        emailService.sendEmailWithFiles(recipients, null, emailSubject, emailBody, true, files)
    }


    void sendIcsrProfileCommitRejectedEmailTo(ExecutedIcsrProfileConfiguration configuration, String caseNumber) {
        //ICSR Profile Generation : Send failure email for all case number whose r3xml is not generated successfully
        Locale locale = userService.getCurrentUser()?.preference?.locale
        String[] recipients = configuration.executedDeliveryOption?.emailToUsers?.toArray()
        String emailSubject = ViewHelper.getMessage("app.emailService.commit.rejected.error.subject.label", configuration.reportName)
        String emailBody = ViewHelper.getMessage("app.label.hi") + "<br><br>"
        emailBody += ViewHelper.getMessage("app.emailService.commit.rejected.error.message.label", caseNumber, configuration.reportName)
        emailBody += "<br><br>"
        if (locale?.language != 'ja') {
            emailBody += ViewHelper.getMessage("app.label.thanks") + "," + "<br>"
        }
        emailBody += ViewHelper.getMessage("app.label.pv.reports")
        emailBody = "<span style=\"font-size: 14px;\">" + emailBody + "</span>"
        emailService.sendEmailWithFiles(recipients, null, emailSubject, emailBody, true, null)
    }


    void sendIcsrProfileApplicationRejectedEmailTo(ExecutedIcsrProfileConfiguration configuration, String caseNumber) {
        //ICSR Profile Generation : Send failure email for all case number whose r3xml is not generated successfully
        Locale locale = userService.getCurrentUser()?.preference?.locale
        String[] recipients = configuration.executedDeliveryOption?.emailToUsers?.toArray()
        String emailSubject = ViewHelper.getMessage("app.emailService.application.rejected.error.subject.label", configuration.reportName)
        String emailBody = ViewHelper.getMessage("app.label.hi") + "<br><br>"
        emailBody += ViewHelper.getMessage("app.emailService.application.rejected.error.message.label", caseNumber, configuration.reportName)
        emailBody += "<br><br>"
        if (locale?.language != 'ja') {
            emailBody += ViewHelper.getMessage("app.label.thanks") + "," + "<br>"
        }
        emailBody += ViewHelper.getMessage("app.label.pv.reports")
        emailBody = "<span style=\"font-size: 14px;\">" + emailBody + "</span>"
        emailService.sendEmailWithFiles(recipients, null, emailSubject, emailBody, true, null)
    }

    void updateStatusesFromGateway() {
        log.info("Update Status Job Triggered")
        IcsrCaseTracking.getAllTransmittingCases().list([sort: 'modifiedDate', order: 'asc', max: 500]).each { IcsrCaseTracking icsrCaseTracking ->
            try {
                log.info("Updating Status for ${icsrCaseTracking.uniqueIdentifier()}")
                ExecutedIcsrTemplateQuery executedTemplateQuery = ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId)
                ExecutedIcsrProfileConfiguration executedConfiguration = (ExecutedIcsrProfileConfiguration) GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery?.executedConfiguration)
                String currentSenderIdentifier = executedConfiguration?.getSenderIdentifier()
                String fileName = null
                if (executedTemplateQuery?.reportResult) {
                    if (icsrCaseTracking.e2BStatus.equals(IcsrCaseStateEnum.TRANSMITTING_ATTACHMENT.toString())) {
                        fileName = executedTemplateQuery.id + "-" + icsrCaseTracking.caseNumber + "-" +icsrCaseTracking.versionNumber + Constants.PDF_EXT
                    } else {
                        fileName = dynamicReportService.getTransmittedR3XmlFileName(executedTemplateQuery.reportResult, icsrCaseTracking.caseNumber)
                    }
                } else {
                    CaseResultData caseResultData = CaseResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(icsrCaseTracking.exIcsrTemplateQueryId, icsrCaseTracking.caseNumber, icsrCaseTracking.versionNumber)
                    if (caseResultData) {
                        if (icsrCaseTracking.e2BStatus.equals(IcsrCaseStateEnum.TRANSMITTING_ATTACHMENT.toString())) {
                            fileName = executedTemplateQuery.id + "-" + icsrCaseTracking.caseNumber + "-" +icsrCaseTracking.versionNumber + Constants.PDF_EXT
                        }else {
                            fileName = dynamicReportService.getTransmittedR3XmlFileName(caseResultData, currentSenderIdentifier, icsrCaseTracking.caseNumber, icsrCaseTracking.versionNumber as Long, executedTemplateQuery.id, icsrCaseTracking?.transmissionDate, icsrCaseTracking?.isJapanProfile())
                        }
                    }
                }

                if (fileName) {
                    Map transmissionDateAndStatus = [:]
                    if (executedTemplateQuery?.distributionChannelName == DistributionChannelEnum.PV_GATEWAY){
                        transmissionDateAndStatus = PVGatewayService.getTransmitDateForFile(fileName)
                    } else if (executedTemplateQuery?.distributionChannelName == DistributionChannelEnum.EXTERNAL_FOLDER){
                        transmissionDateAndStatus = AxwayService.getTransmitDateForFile(fileName)
                    }
                    if (transmissionDateAndStatus && executedTemplateQuery.executedConfiguration) {
                        String status = transmissionDateAndStatus.status
                        Date transmittedOn = transmissionDateAndStatus.date
                        Tenants.withId(executedTemplateQuery.executedConfiguration.tenantId) {
                            XMLResultData xmlResultData = XMLResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(icsrCaseTracking.exIcsrTemplateQueryId, icsrCaseTracking.caseNumber, icsrCaseTracking.versionNumber)
                            ExecutedIcsrTemplateQuery executedIcsrTemplateQuery = ExecutedIcsrTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId)
                            if (icsrCaseTracking.e2BStatus.equals(IcsrCaseStateEnum.TRANSMITTING_ATTACHMENT.toString()) && xmlResultData.isAttachmentExist) {
                                String comments = status.equals(IcsrCaseStateEnum.TRANSMITTED.toString()) ? ATTACHMENT_GATEWAY_TRANSMISSION_SUCCESSFUL : ATTACHMENT_GATEWAY_TRANSMISSION_ERROR
                                status = status.equals(IcsrCaseStateEnum.TRANSMITTED.toString()) ? IcsrCaseStateEnum.TRANSMITTED_ATTACHMENT.toString() : status
                                reportExecutorService.markCaseAttachmentAccepted(executedTemplateQuery, icsrCaseTracking.caseNumber, icsrCaseTracking.versionNumber.toInteger(), comments, transmittedOn, status)
                                if (comments.equals(ATTACHMENT_GATEWAY_TRANSMISSION_SUCCESSFUL)) {
                                    log.info("Attachment Gateway Transmission successfully for ${icsrCaseTracking.uniqueIdentifier()}")
                                }else {
                                    log.info("Attachment Gateway Transmission failed for ${icsrCaseTracking.uniqueIdentifier()}")
                                }
                            } else {
                                String comments = status.equals(IcsrCaseStateEnum.TRANSMITTED.toString()) ? GATEWAY_TRANSMISSION_SUCCESSFUL : GATEWAY_TRANSMISSION_ERROR
                                //marking status as Transmitted
                                reportExecutorService.markCaseTransmitted(executedTemplateQuery, icsrCaseTracking.caseNumber, icsrCaseTracking.versionNumber.toInteger(), comments, transmittedOn, status)
                                if(comments.equals(GATEWAY_TRANSMISSION_SUCCESSFUL)) {
                                    log.info("XML Gateway Transmission successfully for ${icsrCaseTracking.uniqueIdentifier()}")
                                }else {
                                    log.info("XML Gateway Transmission failed for ${icsrCaseTracking.uniqueIdentifier()}")
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("Error while updating status for ${icsrCaseTracking.uniqueIdentifier()} due to ${ex.printStackTrace()}")
            }
        }
        log.info("Update Status Job Completed")
    }

    @ReadOnly(connection = 'pva')
    IcsrCaseTracking getIcsrTrackingRecord(Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber){
        return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, versionNumber)
    }

    IcsrCaseTracking getIcsrTrackingRecordWithoutPva(Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber){
        return IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, versionNumber)
    }

    @ReadOnly(connection = 'pva')
    IcsrCaseTracking getIcsrTrackingRecordByProcessedReportId(Long processedReportId, String caseNumber, Long versionNumber){
        return IcsrCaseTracking.findByProcessedReportIdAndCaseNumberAndVersionNumber(processedReportId, caseNumber, versionNumber)
    }

    private void moveFile(String destinationFolder, File file) {
        Path destinationPath = Paths.get(destinationFolder)
        if (!Files.exists(destinationPath)) {
            log.warn("Creating folder as folder doesn't exist for ${destinationFolder}")
            destinationPath.toFile().mkdir()
        }
        Path newFilePath = Paths.get(destinationFolder, file.name)
        if (Files.exists(newFilePath)) {
            log.warn("File on path already exist so need to move existing files from the path ${newFilePath.toString()} first")
            moveExistingFile(newFilePath)
        }
        //Added replace existing to safe guard from too many open files.
        Files.move(Paths.get(file.toString()), newFilePath, StandardCopyOption.REPLACE_EXISTING)
    }

    private void moveFileToErrorAndNotify(String destinationFolder, File file) {
        moveFile(destinationFolder, file)
        //send mail
        sendIcsrAckFileFailureEmailTo(new File(destinationFolder + File.separator + file.name))
    }

    Map icsrCaseTrackingMapForAuditLog(IcsrCaseTracking icsrCaseTracking){
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.read(icsrCaseTracking.exIcsrProfileId)
        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(icsrCaseTracking.exIcsrTemplateQueryId)
        SuperQuery executedQuery = SuperQuery.read(icsrCaseTracking.queryId)
        ReportTemplate executedTemplate = executedTemplateQuery?.executedTemplate
        Date dueInDate = icsrCaseTracking.dueDate
        Integer dueInDays = icsrCaseTracking.dueInDays
        String recipientOrganizationName = ""
        String preferredTimeZone = ""
        if(executedReportConfiguration instanceof ExecutedIcsrProfileConfiguration || executedReportConfiguration instanceof ExecutedIcsrReportConfiguration) {
            recipientOrganizationName = executedReportConfiguration.recipientOrganizationName
            preferredTimeZone = executedReportConfiguration.preferredTimeZone
        }

        def map = [:]
        map['id'] = icsrCaseTracking.uniqueIdentifier()
        map['caseNumber'] = icsrCaseTracking.caseNumber
        map['version'] = icsrCaseTracking.versionNumber
        map['caseReceiptDate'] = icsrCaseTracking.caseReceiptDate?.format("MM/dd/yyyy")
        map['safetyReceiptDate'] = getDateWithTimeZone(icsrCaseTracking.safetyReceiptDate, icsrCaseTracking.timeZoneOffset)
        map['productName'] = icsrCaseTracking.productName
        map['eventPreferredTerm'] = icsrCaseTracking.eventPreferredTerm
        map['susar'] = icsrCaseTracking.susar
        map['recipient'] = icsrCaseTracking.recipient
        map['profileName'] = icsrCaseTracking.profileName
        map['queryName'] = executedTemplateQuery?.title ?: executedQuery?.name
        map['reportForm'] = executedTemplate?.name
        map['dueDate'] = dueInDate?.format("MM/dd/yyyy")
        map['scheduledDate'] = getDateWithTimeZone(icsrCaseTracking.scheduledDate, icsrCaseTracking.timeZoneOffset)
        map['generationDate'] = getDateWithTimeZone(icsrCaseTracking.generationDate, icsrCaseTracking.timeZoneOffset)
        map['submissionDate'] = getDateWithTimeZone(icsrCaseTracking.submissionDate, icsrCaseTracking.timeZoneOffset)
        map['report'] = executedTemplate?.isCiomsITemplate() || executedTemplate?.isMedWatchTemplate()
        map['exIcsrProfileId'] = icsrCaseTracking.exIcsrProfileId
        map['exIcsrTemplateQueryId'] = icsrCaseTracking.exIcsrTemplateQueryId
        map['icsrState'] = (icsrCaseTracking.e2BStatus != IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL.name()) ? icsrCaseTracking.e2BStatus : IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED.name()
        map['dueInDays'] = dueInDays
        map['followupNumber'] = icsrCaseTracking.followupNumber
        map['localReportMessage'] = icsrCaseTracking.localReportMessage
        map['transmissionDate'] = getDateWithTimeZone(icsrCaseTracking.transmissionDate, icsrCaseTracking.timeZoneOffset)
        map['modifiedDate'] = getDateWithTimeZone(icsrCaseTracking.modifiedDate, icsrCaseTracking.timeZoneOffset)
        map['followupInfo'] = icsrCaseTracking.followupInfo
        map['downgrade'] = icsrCaseTracking.downgrade
        map['awareDate'] = icsrCaseTracking.awareDate?.format("MM/dd/yyyy")
        map['ackFileName'] = icsrCaseTracking.ackFileName
        map['isGenerated'] = icsrCaseTracking.isGenerated
        map['caseId'] = icsrCaseTracking.caseId
        map['templateId'] = executedTemplate?.originalTemplateId
        map['isLocalCpRequired'] = icsrCaseTracking.flagLocalCpRequired
        map['isAutoGenerate'] = icsrCaseTracking.flagAutoGenerate
        map['submissionFormDesc'] = icsrCaseTracking.submissionFormDesc
        map['allowNullification'] = icsrCaseTracking.followupInfo != "Nullification"
        map['processedReportId'] = icsrCaseTracking.processedReportId
        map['prodHashCode'] = icsrCaseTracking.prodHashCode
        map['preferredTimeZone'] = preferredTimeZone
        map['localSubmissionDateTime'] = getDateWithTimeZone(icsrCaseTracking.preferredDateTime, preferredTimeZone)
        map['recipientTimeZone'] = icsrCaseTracking.timeZoneOffset
        map['profileId'] = icsrCaseTracking.profileId
        return map
    }

    private String getDateWithTimeZone(Date date, String timeZone){
        if(timeZone == null)
            timeZone = TimeZoneEnum.TZ_330.getTimezoneId()

        if(date)
            return date.format(DateUtil.DATEPICKER_UTC_FORMAT) + " (${timeZone})"
        else
            return null
    }

    Map getLicenseIdApprovalNumberApprovalTypeId (Long authId) {
        String licenseId = null
        String approvalNum = null
        String approvalTypeId = null
        if(authId != null){
            Sql sql = new Sql(dataSource_pva)
            log.info("Fetching Approval Number")
            try {
                List<Object> result = sql.rows("SELECT LICENSE_ID, APPROVAL_NUMBER, APPROVAL_TYPE_ID FROM VW_TX_LICENSE_AUTH_ID_LINK WHERE AUTH_ID = ?", [authId])
                if (result?.size() > 0) {
                    licenseId = result[0].LICENSE_ID
                    approvalNum = result[0].APPROVAL_NUMBER
                    approvalTypeId = result[0].APPROVAL_TYPE_ID
                }
            } catch (SQLException e) {
                log.error("Exception caught while fetching approval number: ", e)
            } finally {
                sql?.close()
            }
        }
        return [licenseId : licenseId, approvalNumber: approvalNum, approvalTypeId: approvalTypeId] as Map
    }

    boolean validateSubmissionDate(Date localSubmissionDate, Date generationDate, String timeZoneId, String icsrCaseState = null) {
        if (!localSubmissionDate || localSubmissionDate in ["Invalid date", "null"]) {
            return false
        }
        if (icsrCaseState && IcsrCaseStateEnum.valueOf(icsrCaseState) in
                [IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED, IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL]) {
            return false
        }
        Date localcurrentDate = DateUtil.covertToDateWithTimeZone(new Date(), Constants.DateFormat.NO_TZ, timeZoneId)
        if (generationDate != null) {
            Date localGenerationDate = DateUtil.covertToDateWithTimeZone(generationDate, Constants.DateFormat.NO_TZ, timeZoneId)
            return localSubmissionDate?.after(localcurrentDate) || localSubmissionDate?.before(localGenerationDate)
        } else {
            return localSubmissionDate?.after(localcurrentDate)
        }
    }

}
