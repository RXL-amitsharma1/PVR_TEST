package com.rxlogix

import com.fasterxml.jackson.databind.ObjectMapper
import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.BulkDownloadIcsrReports
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.commandObjects.CaseSubmissionCO
import com.rxlogix.config.BaseConfiguration
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.ExecutedIcsrReportConfiguration
import com.rxlogix.config.ExecutedIcsrTemplateQuery
import com.rxlogix.config.ExecutedXMLTemplate
import com.rxlogix.config.IcsrCaseTracking
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.dto.AuditTrailChildDTO
import com.rxlogix.enums.IcsrCaseStatusEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.SuperQuery
import com.rxlogix.config.IcsrCaseSubmission
import com.rxlogix.dto.CaseStateUpdateDTO
import com.rxlogix.enums.DistributionChannelEnum
import com.rxlogix.enums.E2BReportFormatEnum
import com.rxlogix.enums.IcsrCaseStateEnum
import com.rxlogix.enums.LateEnum
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.commons.io.FileUtils
import groovy.json.JsonOutput
import groovy.json.JsonBuilder
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.http.HttpStatus

import javax.sql.DataSource
import java.sql.SQLException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class IcsrCaseTrackingService {

    static transactional = false

    def userService
    def icsrReportService
    def dynamicReportService
    def notificationService
    def utilService
    def icsrXmlService
    def icsrProfileAckService
    def reportExecutorService
    def reportSubmissionService
    DataSource dataSource_pva

    //Execute method for BulkDownloadICSRReports Job
    void prepareBulkDownload() {
        Sql pvrsql
        try {
            pvrsql = new Sql(utilService.getReportConnectionForPVR())
            BulkDownloadIcsrReports bulkDownloadIcsrReports = BulkDownloadIcsrReports.first()
            if (bulkDownloadIcsrReports) {
                log.info("BulkDownloadIcsrReportsJob started preparing zip file")
                int successfulDownloadCount = 0
                int failedDownloadCount = 0
                User downloadBY = GrailsHibernateUtil.unwrapIfProxy(bulkDownloadIcsrReports.downloadBy)
                File tempDirectory = new File(Holders.config.getProperty('tempDirectory'))
                File directoryToArchive = new File(tempDirectory, "${MiscUtil.generateRandomName()}");
                if (directoryToArchive.exists()) {
                    FileUtils.deleteDirectory(directoryToArchive, directoryToArchive)
                }
                directoryToArchive.mkdir()
                File file = new File(tempDirectory, "BulkICSRReport_" + new Timestamp(new Date().getTime()).toString().replaceAll("[ :.-]", ""));
                List<Map<String, String>> downloadDataList = new ArrayList<>();
                Map downloadData = new ObjectMapper().readValue(bulkDownloadIcsrReports.downloadData, Map)
                List downloadEntitiesList = downloadData['downloadData']
                Long exIcsrTemplateQueryId
                downloadEntitiesList.each {
                    Map<String, String> downloadDataMap =  new HashMap<String, String>()
                    try {
                        exIcsrTemplateQueryId = it["exIcsrTemplateQueryId"]
                        String caseNumber = it["caseNumber"]
                        Long versionNumber = it["versionNumber"]
                        File reportFile
                        File reportFileCiomsMedwatch
                        Boolean isPdfReport = false
                        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
                        ExecutedReportConfiguration executedReportConfiguration = (ExecutedReportConfiguration) executedTemplateQuery?.usedConfiguration
                        downloadDataMap.put('Case Number', caseNumber)
                        downloadDataMap.put('Version Number', String.valueOf(versionNumber))
                        downloadDataMap.put('Profile Name', executedReportConfiguration?.reportName)
                        downloadDataMap.put('Recipient', executedReportConfiguration?.recipientOrganizationName)
                        downloadDataMap.put('Report Form', executedTemplateQuery?.executedTemplate?.name)
                        downloadDataList.add(downloadDataMap)
                        if (!executedReportConfiguration || !executedTemplateQuery || !caseNumber) {
                            log.error("Either report configuration or template query or case number not found")
                            failedDownloadCount++
                            downloadDataMap.put('Status', 'Fail')
                            return
                        }
                        def configuration = ReportConfiguration.findByReportNameAndOwner(executedReportConfiguration.reportName, executedReportConfiguration.owner)
                        IcsrCaseTracking newIcsrCaseTrackingInstance = null
                        IcsrCaseTracking.withNewSession {
                            newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
                        }
                        Date fileDate = newIcsrCaseTrackingInstance?.generationDate
                        Boolean isJapanProfile = newIcsrCaseTrackingInstance?.isJapanProfile()
                        String fileName = ''
                        String pdfFileName = ''
                        if (executedTemplateQuery.distributionChannelName == DistributionChannelEnum.EMAIL && executedReportConfiguration?.isJapanProfile &&
                                executedReportConfiguration.e2bDistributionChannel?.reportFormat in [E2BReportFormatEnum.PDF, E2BReportFormatEnum.EB_PDF]) {
                            reportFile = dynamicReportService.createPDFReport(executedTemplateQuery, false, [caseNumber: caseNumber, versionNumber: versionNumber, exIcsrTemplateQueryId:executedTemplateQuery.id, generatePmdaPaperReport: true], fileDate, isJapanProfile)
                            isPdfReport = true
                        } else if (executedTemplateQuery.usedTemplate?.isCiomsITemplate()) {
                            reportFile = dynamicReportService.createCIOMSReport(caseNumber, versionNumber.intValue(), executedReportConfiguration?.owner, fileDate, isJapanProfile, executedTemplateQuery)
                            isPdfReport = true
                        } else if (executedTemplateQuery.usedTemplate?.isMedWatchTemplate()) {
                            reportFile = dynamicReportService.createMedWatchReport(caseNumber, versionNumber.intValue(), executedReportConfiguration?.owner, fileDate, isJapanProfile, executedTemplateQuery)
                            isPdfReport = true
                        } else {
                            def xmlParameterMap = ["exIcsrTemplateQueryId": executedTemplateQuery?.id, "outputFormat": ReportFormatEnum.R3XML.displayName, "caseNumber": caseNumber, "versionNumber": versionNumber]
                            reportFile = dynamicReportService.createR3XMLReport(executedTemplateQuery, false, xmlParameterMap, fileDate, isJapanProfile)
                        }

                        if (isPdfReport) {
                            fileName = reportFile?.name
                            if (fileName?.contains('_')) {
                                pdfFileName = fileName.substring(0, fileName.lastIndexOf('_')) + '.' + ReportFormatEnum.PDF.displayName
                                reportFileCiomsMedwatch = new File(reportFile.parent, pdfFileName)
                                if (reportFile.renameTo(reportFileCiomsMedwatch)) {
                                    reportFile = reportFileCiomsMedwatch
                                } else {
                                    if (reportFileCiomsMedwatch?.exists() && !reportFileCiomsMedwatch.isDirectory()) {
                                        reportFileCiomsMedwatch.delete()
                                    }
                                    log.error("Failed to rename the file from ${reportFile.name} to ${pdfFileName}")
                                }
                            }
                            isPdfReport = false
                        }
                        FileUtils.copyFileToDirectory(reportFile, directoryToArchive)
                        downloadDataMap.put('Status', 'Success')
                        successfulDownloadCount++;
                    } catch (RuntimeException runEx) {
                        log.error("Exception occurred while downloading ICSR Report exIcsrTemplateQueryId : ${exIcsrTemplateQueryId} for user ${downloadBY.username} ", runEx)
                        downloadDataMap.put('Status', 'Fail')
                        failedDownloadCount++;
                    } catch (Exception e) {
                        log.error("Exception occurred while downloading ICSR Report exIcsrTemplateQueryId : ${exIcsrTemplateQueryId} for user ${downloadBY.username} ", e)
                        downloadDataMap.put('Status', 'Fail')
                        failedDownloadCount++;
                    }
                }
                if (successfulDownloadCount) {
                    try {
                        createZipFile(file.toString(), directoryToArchive)
                        FileUtils.cleanDirectory(directoryToArchive)
                        directoryToArchive.delete()
                        String downloadDataForAuditLog = Constants.BLANK_STRING
                        int count = 1;
                        downloadDataList.each {
                            downloadDataForAuditLog = downloadDataForAuditLog + "ICSR Report#" + count + " : " + it.inspect().replaceAll("[']", "") + "\n\n"
                            count++;
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM)
                        String exportDate = sdf.format(bulkDownloadIcsrReports.dateCreated) + " (GMT)"
                        List<AuditTrailChildDTO> auditTrailChildDTOList = [new AuditTrailChildDTO("Download Data", Constants.BLANK_STRING, downloadDataForAuditLog), new AuditTrailChildDTO("Download By", Constants.BLANK_STRING, downloadBY.username), new AuditTrailChildDTO("Download Time", Constants.BLANK_STRING, exportDate)]
                        utilService.createAuditLog(AuditTrail.Category.INSERT.toString(), null, "Save Bulk Download ICSR Reports", AuditTrail.Category.INSERT.displayName, "Bulk Download ICSR Reports", "Bulk download request for ICSR Report output", "", auditTrailChildDTOList)
                        log.info("Successfully generated ICSR Bulk Download reports for user : ${downloadBY.username}")
                        List notificationParameters = [file.getName()]
                        User.withNewSession {
                            User user = User.get(downloadBY.id)
                            if(failedDownloadCount>0) {
                                StringBuffer messageArgs = new StringBuffer("${successfulDownloadCount},${failedDownloadCount}")
                                notificationService.addNotification(user, 'icsr.reports.bulk.download.message', messageArgs.toString(), notificationParameters.join(" : "), NotificationLevelEnum.WARN, NotificationApp.DOWNLOAD)
                            } else {
                                StringBuffer messageArgs = new StringBuffer("${successfulDownloadCount}")
                                notificationService.addNotification(user, 'icsr.reports.bulk.download.success.message', messageArgs.toString(), notificationParameters.join(" : "), NotificationLevelEnum.INFO, NotificationApp.DOWNLOAD)
                            }
                        }
                    } catch (Exception e) {
                        User.withNewSession {
                            User user = User.get(downloadBY.id)
                            log.error("Failed to prepare zip for bulk download icsr reports for user : ${user.username} ", e)
                            directoryToArchive.delete()
                            notificationService.addNotification(user, 'icsr.reports.bulk.download.error', "", "", NotificationLevelEnum.ERROR, NotificationApp.DOWNLOAD)
                        }
                    }
                } else {
                    User.withNewSession {
                        User user = User.get(downloadBY.id)
                        log.warn("ICSR Bulk download reports failed for all reports for user : ${user.username}")
                        directoryToArchive.delete()
                        notificationService.addNotification(user, 'icsr.reports.bulk.download.error', "", "", NotificationLevelEnum.ERROR, NotificationApp.DOWNLOAD)
                    }
                }
                pvrsql.executeUpdate("delete from BULK_DOWNLOAD_ICSR_REPORTS where id = " + bulkDownloadIcsrReports.getId())
                log.info("BulkDownloadIcsrReportsJob end after zip file creation")
            }
        } catch (Exception exp) {
            log.error("Exception while preparing bulk download", exp)
        }
        finally {
            pvrsql?.close()
        }
    }

    //This method creates a zip file from a dir file
    public void createZipFile(String zipFile, File directoryToArchive) {
        String[] srcFiles = directoryToArchive.listFiles()
        // create byte buffer
        byte[] buffer = new byte[1024]
        FileOutputStream fos = new FileOutputStream(zipFile)
        ZipOutputStream zos = new ZipOutputStream(fos)
        for (int i = 0; i < srcFiles.length; i++) {
            File srcFile = new File(srcFiles[i])
            FileInputStream fis = new FileInputStream(srcFile)
            // begin writing a new ZIP entry, positions the stream to the start of the entry data
            zos.putNextEntry(new ZipEntry(srcFile.getName()))
            int length
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length)
            }
            zos.closeEntry()
            // close the InputStream
            fis.close()
        }
        // close the ZipOutputStream
        zos.close()
    }

    def fetchIcsrData(LibraryFilter filter, String caseNumber, Long versionNumber, Long exIcsrProfileId, Long exIcsrTemplateQueryId, String icsrCaseStateEnum, List<Closure> searchData, Integer max, Integer offset, String sort, String order, User user, String language) {
        def icsrProfilequery = IcsrProfileConfiguration.ownedByAndSharedWithUser(user, user.isICSRAdmin(), false)
        List<Long> profileIds = icsrProfilequery.list().collect { it.id }
        if (profileIds && profileIds.size() > 0) {
            def icsrCaseTrackingQuery = IcsrCaseTracking.getAllByFilter(filter, caseNumber, versionNumber, exIcsrProfileId, exIcsrTemplateQueryId, icsrCaseStateEnum, profileIds, searchData, user.isICSRAdmin(), sort, order)
            List<IcsrCaseTracking> icsrCaseTrackingList = icsrCaseTrackingQuery.list([max: max, offset: offset, sort: 'processedReportId', order: 'asc'])
            Map<String,Boolean> prequalifiedCases = preloadPrequalifiedCases(icsrCaseTrackingList)
            List<Map> icsrCaseTrackings = icsrCaseTrackingList.findAll { it }.collect { toMap(it, language, user, prequalifiedCases) }
            return [icsrCaseTrackings, IcsrCaseTracking.getAllByFilter(new LibraryFilter([:]), caseNumber, versionNumber, exIcsrProfileId, exIcsrTemplateQueryId, icsrCaseStateEnum, profileIds, null, user.isICSRAdmin(), sort, order).count(), icsrCaseTrackingQuery.count()]
        } else {
            return [[], 0, 0]
        }
    }

    private Map toMap(IcsrCaseTracking icsrCaseTracking, String language, User user, Map<String, Boolean> prequalifiedCases = null) {
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.read(icsrCaseTracking.exIcsrProfileId)
        Date dueInDate = icsrCaseTracking.dueDate
        Integer dueInDays = icsrCaseTracking.dueInDays
        String preferredTimeZone = ""
        Boolean showPrequalifiedError = false
        Locale currLocale = new Locale(language)

        if (executedReportConfiguration instanceof ExecutedIcsrProfileConfiguration || executedReportConfiguration instanceof ExecutedIcsrReportConfiguration) {
            preferredTimeZone = executedReportConfiguration.preferredTimeZone
        }

        if (icsrCaseTracking.e2BStatus == IcsrCaseStateEnum.SCHEDULED.toString() && icsrCaseTracking.flagPmda && icsrCaseTracking.flagCaseLocked && icsrCaseTracking.followupInfo == Constants.FOLLOWUP.toString()) {
            String compositeKey = "${icsrCaseTracking.exIcsrTemplateQueryId}_${icsrCaseTracking.caseNumber}_${icsrCaseTracking.versionNumber}"
            showPrequalifiedError = prequalifiedCases.containsKey(compositeKey)
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
        map['manualFlag'] = icsrCaseTracking.manualFlag
        map['reportForm'] = icsrCaseTracking.templateName
        map['dueDate'] = dueInDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['scheduledDate'] = icsrCaseTracking.scheduledDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['generationDate'] = icsrCaseTracking.generationDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['submissionDate'] = icsrCaseTracking.submissionDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['e2BStatus'] = icsrCaseTracking.e2BStatus
        map['e2BStatusDisplay'] = ViewHelper.getMessage("icsr.case.tracking.status.${icsrCaseTracking.e2BStatus}", null, '', currLocale)
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
        map['prodHashCode'] = icsrCaseTracking.prodHashCode
        String prefDateTime = icsrCaseTracking.preferredDateTime?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['preferredTimeZone'] = preferredTimeZone
        map['preferredDateTime'] = prefDateTime ?: ""
        map['recipientTimeZone'] = icsrCaseTracking.timeZoneOffset
        map['profileId'] = icsrCaseTracking.profileId
        map['authorizationTypeId'] = icsrCaseTracking.authorizationTypeId
        map['authorizationType'] = icsrCaseTracking.authorizationType
        map['approvalNumber'] = icsrCaseTracking.approvalNumber
        map['authId'] = icsrCaseTracking.authId
        map['reportCategoryId'] = icsrCaseTracking.reportCategoryId
        map['originalSectionId'] = icsrCaseTracking.originalSectionId
        map['isExpedited'] = icsrCaseTracking.isExpedited
        map['action'] = executedReportConfiguration ? generateActions(icsrCaseTracking, currLocale) : []
        map['actionsList'] = executedReportConfiguration ? generateActionList(icsrCaseTracking, currLocale, user) : []
        map['regenerateFlag'] = icsrCaseTracking.regenerateFlag? true : false
        map['showPrequalifiedError'] = showPrequalifiedError

        return map
    }

    //This method return the action that needs to be selected in the actions dropdown for every row
    private def generateActions(IcsrCaseTracking icsrCaseTracking, Locale currLocale) {
        String status = icsrCaseTracking.e2BStatus
        def jsonBuilder = new JsonBuilder()
        if (status == "SCHEDULED" || status == "SUBMISSION_NOT_REQUIRED" || status == "SUBMISSION_NOT_REQUIRED_FINAL") {
            return jsonBuilder {
                id "preview"
                text ViewHelper.getMessage('icsr.case.tracking.actions.generateCaseData', null, '', currLocale)
            }
        } else {
            return jsonBuilder {
                id "view"
                text ViewHelper.getMessage('icsr.case.tracking.actions.view', null, '', currLocale)
            }
        }
    }

    //This method returns list of actions applicable for every row
    private List<JSON> generateActionList(IcsrCaseTracking icsrCaseTracking, Locale currLocale, User user) {
        String actions = ""
        def jsonArray = []
        def jsonBuilder = new JsonBuilder()
        String status = icsrCaseTracking.e2BStatus
        Map actionMap = Constants.icsrActionDropdownMap

        Map actionLabelMap = ['localCPCompleted':'local.cp','generateReport':'generate.report','Re-Generate':'regenerateCase','download':'labelDownload',
                              'submissionNotRequired':'labelNotSubmit','emailTo':'labelEmailTo','delete':'labelDelete','transmit':'transmitCase','submit':'labelSubmit','markNullification':'labelNullify']

        if(!user.hasICSRActionRoles()){
            if(actionMap[status]?.contains('download')){
                jsonArray << jsonBuilder {
                    id "download"
                    text ViewHelper.getMessage('icsr.case.tracking.actions.labelDownload', null, '', currLocale)
                }
            }
            return jsonArray
        }

        actionMap[status]?.each { String action ->
            // Skip "generateReport"/"localCPCompleted" if conditions are not met
            if ((action == "generateReport" && (icsrCaseTracking.flagLocalCpRequired || icsrCaseTracking.flagAutoGenerate)) || (action == "localCPCompleted" && !icsrCaseTracking.flagLocalCpRequired)) {
                return
            }
            // Skip "markNullification" if follow-up info is "Nullification"
            if (action == "markNullification" && icsrCaseTracking.followupInfo == "Nullification") {
                return
            }
            jsonArray << jsonBuilder {
                id action
                text ViewHelper.getMessage("icsr.case.tracking.actions.${actionLabelMap[action]}", null, '', currLocale)
            }
        }
        return jsonArray
    }

    //This method return the color to be shown on due date column
    private String getIndicator(IcsrCaseTracking icsrCaseTracking, Date dueInDate) {
        Date now = new Date();
        Date soon = now + 2;
        if (dueInDate > now && dueInDate < soon && !icsrCaseTracking.submissionDate) return "yellow"
        if (dueInDate < now && !icsrCaseTracking.submissionDate) return "red"
        return ""
    }

     Map toCaseSubmissionHistoryMap(IcsrCaseSubmission icsrCaseSubmission, String preferredTimeZone = null, String language = 'en') {
        Locale currLocale = new Locale(language)
        def map = [:]
        map['caseNumber'] = icsrCaseSubmission?.caseNumber
        map['versionNumber'] = icsrCaseSubmission?.versionNumber
        map['e2BStatus'] = icsrCaseSubmission.e2bStatus
        map['e2BStatusDisplay'] = ViewHelper.getMessage("icsr.case.tracking.status.${icsrCaseSubmission.e2bStatus}", null, '', currLocale)
        map['lastUpdateDate'] = icsrCaseSubmission.lastUpdateDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['reportDestination'] = icsrCaseSubmission.reportDestination
        map['exIcsrTemplateQueryId'] = icsrCaseSubmission.exIcsrTemplateQueryId
        map['lastUpdatedBy'] = icsrCaseSubmission.lastUpdatedBy ?: 'Application'
        map['ackFileName'] = icsrCaseSubmission.ackFileName
        String comments = (language == 'ja' && icsrCaseSubmission.commentsJ) ? icsrCaseSubmission.commentsJ : icsrCaseSubmission.comments
        if (comments) {
            map['comments'] = comments.replaceAll("(?i)''", "'")
        }
        map['submissionDocument'] = icsrCaseSubmission.submissionDocument ? (icsrCaseSubmission.submissionDocument == new byte[1024] ? false : true) : false
        map['e2bProcessId'] = icsrCaseSubmission.e2bProcessId
        map['userTimeZone'] = fetchTimeZoneMessage(userService.currentUser?.preference?.timeZone ?: "UTC")
        map['attachmentAckFileName'] = icsrCaseSubmission.attachmentAckFileName

        Date statusDate = null
        switch (icsrCaseSubmission.e2bStatus) {
            case IcsrCaseStateEnum.GENERATED.toString():
            case IcsrCaseStateEnum.SCHEDULED.toString():
            case IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED.toString():
            case IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL.toString():
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
                    if (icsrCaseSubmission.dateAckRecievedAttach) {
                        statusDate = icsrCaseSubmission.dateAckRecievedAttach
                    } else {
                        statusDate = icsrCaseSubmission.ackReceiveDate
                    }
                }else {
                    //RE-GENERATION IN PROGRESS
                    statusDate = icsrCaseSubmission.lastUpdateDate
                }
                break
        }
        if (statusDate) {
            def prefTimeZneDate
            def prefTimeZone
            if (icsrCaseSubmission.localDateTime) {
                prefTimeZneDate = icsrCaseSubmission.localDateTime.format(DateUtil.DATEPICKER_UTC_FORMAT)
                prefTimeZone = fetchTimeZoneMessage(icsrCaseSubmission.timeZoneOffset)
            } else {
                Date preferredTimeZoneDate = preferredTimeZone ? DateUtil.covertToDateWithTimeZone(statusDate, Constants.DateFormat.NO_TZ, preferredTimeZone) : null
                prefTimeZneDate = preferredTimeZoneDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
                prefTimeZone = fetchTimeZoneMessage(preferredTimeZone)
            }
            map['preferredTimeZoneDate'] = prefTimeZneDate
            map['preferredTimeZone'] = prefTimeZone
            map['statusDate'] = statusDate?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        }
        return map
    }

    private String fetchTimeZoneMessage(String timeZoneId) {
        TimeZoneEnum timeZone = TimeZoneEnum.values().find {
            it.timezoneId == timeZoneId
        }
        return ViewHelper.getMessage(timeZone?.getI18nKey(), timeZone?.getGmtOffset())
    }

    def getTimezone() {
        try {
            List<Map> data = TimeZoneEnum.values().collect {
                [id: it.name(), text: ViewHelper.getMessage(it?.getI18nKey(), it?.getGmtOffset())]
            }
        } catch (Exception e) {
            log.error("Unable to fetch Timezone value : " + e)
        }
    }

    Map<String, Boolean> preloadPrequalifiedCases (List<IcsrCaseTracking> icsrCaseTrackingList) {
        if (icsrCaseTrackingList.isEmpty()) {
            return [:]
        }
        Sql sql = new Sql(dataSource_pva)
        Map<String, Boolean> prequalifiedCases = [:]
        String query = """
        SELECT SECTION_ID, CASE_NUM, VERSION_NUM
        FROM PVR_ICSR_PROFILE_QUEUE
        WHERE STATUS = '${IcsrCaseStatusEnum.PRE_QUALIFIED}'
    """
        // Appending the conditions for each icsrCaseTracking element
        query += " AND ("

        try{
            String conditions = icsrCaseTrackingList.findAll { tracking ->
                tracking.e2BStatus == IcsrCaseStateEnum.SCHEDULED.toString() && tracking.flagPmda && tracking.flagCaseLocked && (tracking.followupInfo == Constants.FOLLOWUP.toString() ||  tracking.followupInfo == Constants.NULLIFICATION.toString())
            }.collect { tracking ->
                "(SECTION_ID = ${tracking.exIcsrTemplateQueryId} AND " +
                        "CASE_NUM = '${tracking.caseNumber}' AND " +
                        "VERSION_NUM = ${tracking.versionNumber})"
            }.join(" OR ")

            if (!conditions) {
                return [:]
            }
            query += conditions + ")"

            // Execute the SQL query
            List<GroovyRowResult> rows = sql.rows(query)
            rows?.each { row ->
                String compositeKey = "${row.SECTION_ID}_${row.CASE_NUM}_${row.VERSION_NUM}"
                prequalifiedCases[compositeKey] = true
            }
        } catch (SQLException e) {
            log.error("Sql Error while fetching list of Prequalified cases from ICSR Profile Queue",e)
        } finally {
            sql?.close()
        }
        return prequalifiedCases
    }

    Boolean validateCaseDataForPreview(Long exIcsrTemplateQueryId, String caseNumber, Integer versionNumber) {
        IcsrCaseTracking icsrCaseTracking = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTracking = icsrProfileAckService.getIcsrTrackingRecord(exIcsrTemplateQueryId, caseNumber, versionNumber)
        }
        if (icsrCaseTracking.isGenerated) {
            return false
        }
        return true
    }

}
