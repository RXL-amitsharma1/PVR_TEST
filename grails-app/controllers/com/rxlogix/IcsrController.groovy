package com.rxlogix

import com.rxlogix.config.CaseResultData
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.IcsrCaseTracking
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.enums.IcsrCaseStatusEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.file.MultipartFileSender
import com.rxlogix.mapping.IcsrCaseProcessingQueue
import com.rxlogix.mapping.IcsrCaseProcessingQueueHist
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.async.Promises
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import grails.util.Holders
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.context.request.RequestContextHolder
import java.sql.SQLException
import java.text.SimpleDateFormat
import com.rxlogix.helper.LocaleHelper

@Secured(['ROLE_ICSR_PROFILE_VIEWER'])
class IcsrController {

    def userService
    def dynamicReportService
    def emailService
    def executorThreadInfoService
    def icsrScheduleService
    def notificationService
    def icsrProfileAckService

    def index() {}

    def showReport(Long exIcsrTemplateQueryId, String caseNumber, Integer versionNumber, Boolean isInDraftMode, Long processReportId, String prodHashCode, String reportLang) {
        String filename;
        ExecutedReportConfiguration executedReportConfiguration;
        String reportLocale;
        boolean isPvcm = false
        boolean generatePmdaPaperReport = false
        IcsrCaseTracking newIcsrCaseTrackingInstance = null
        IcsrCaseTracking.withNewSession {
            newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(exIcsrTemplateQueryId, caseNumber, versionNumber)
        }
        Date fileDate = newIcsrCaseTrackingInstance?.generationDate
        if(fileDate == null) fileDate = new Date()
        Boolean isJapanProfile = newIcsrCaseTrackingInstance?.isJapanProfile()
        try {
            ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
            if (executedTemplateQuery) {
                if(executedTemplateQuery.executedConfiguration instanceof ExecutedIcsrProfileConfiguration)
                    generatePmdaPaperReport = executedTemplateQuery.executedConfiguration?.isJapanProfile
                if (executedTemplateQuery.usedTemplate?.isCiomsITemplate()) {
                    forward(controller: 'report', action: 'drillDown')
                    return
                }
                else if (executedTemplateQuery.usedTemplate?.isMedWatchTemplate()) {
                    forward(controller: 'report', action: 'exportMedWatch')
                    return
                }
                executedReportConfiguration = (ExecutedReportConfiguration) executedTemplateQuery?.usedConfiguration
                reportLocale = (userService.currentUser?.preference?.locale ?: executedReportConfiguration.owner.preference.locale).toString()
                if(!reportLang){
                    params.reportLang = reportLocale
                }
                if (!executedReportConfiguration) {
                    flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.view.cases'), params.id])
                    redirect action: "viewCases", method: "GET"
                    return
                }
                // Remove Notification
                notificationService.deleteNotificationByNotificationParameters(userService.getCurrentUser(), NotificationApp.COMMENTS, caseNumber, versionNumber, exIcsrTemplateQueryId, isInDraftMode)

                String reportName = dynamicReportService.createHTMLReport(executedTemplateQuery, isInDraftMode ?: false, params, fileDate, isJapanProfile)?.getName()
                //R39406_20US00013604_en.html
                filename = dynamicReportService.getFieldName(reportName); //R39406_20US00013604
                String xsltName = executedTemplateQuery.executedConfiguration.xsltName
                if(xsltName && xsltName.equals(Constants.MIR)) {
                    flash.warn = message(code: "app.mir.r2.icsr.support.only.xml.format")
                }
                String url = Holders.config.getProperty('casedata.drill.down.uri')
                isPvcm = url ? url.contains("<<CASE_ID>>") : false
            }
        } catch (Exception ex) {
            flash.error = message(code: 'icsr.case.number.html.failure', args: [caseNumber])
            log.error("Error while genearting html for caseNumber: ${caseNumber} via exIcsrTemplateQueryId: ${exIcsrTemplateQueryId}", ex)
        }
        filename = dynamicReportService.getReportNameWithLocale(filename, params.reportLang)
        [filename: filename, executedConfigurationInstance: executedReportConfiguration, exIcsrProfileId: executedReportConfiguration?.id, exIcsrTemplateQueryId: exIcsrTemplateQueryId, caseNumber: caseNumber, versionNumber: versionNumber, isPvcm: isPvcm, localeList: LocaleHelper.buildLocaleListAsPerUserLocale(reportLocale), reportLang: params.reportLang, generatePmdaPaperReport: generatePmdaPaperReport]
    }

    def email(Long exIcsrTemplateQueryId, String caseNumber, Integer versionNumber) {
        try {
            ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
            List<ReportFormatEnum> formats = []
            if (params.attachmentFormats instanceof String) {
                formats.add(ReportFormatEnum.valueOf(params.attachmentFormats))
            } else {
                formats = params.attachmentFormats.collect { ReportFormatEnum.valueOf(it) }
            }
            List<String> emailList = []
            if (params.emailToUsers instanceof String) {
                emailList.add(params.emailToUsers)
            } else {
                emailList = params.emailToUsers
            }
            if (!(executedTemplateQuery && caseNumber && versionNumber && formats && emailList)) {
                flash.error = message(code: 'icsr.case.email.invalid.data.error')
                redirect(controller: 'icsrProfileConfiguration', action: 'viewCases')
                return
            }
            emailService.emailIcsrCaseTo(executedTemplateQuery, caseNumber, versionNumber, emailList, formats)
            flash.message = message(code: 'icsr.case.email.sent.success', args: [caseNumber, versionNumber])
        } catch (Exception ex) {
            log.error("Failed while sending email for ${caseNumber} and ${versionNumber}", ex)
            flash.error = message(code: 'icsr.case.email.sent.failed', args: [caseNumber, versionNumber])
        }
        redirect(controller: 'icsrProfileConfiguration', action: 'viewCases')
    }


    def executionStatus(String status) {
        [status: status ?: '']
    }

    def generatedCaseDataScheduled(Long exIcsrTemplateQueryId, String caseNumber, Integer versionNumber, String status){
        Long processedReportId = params.long('processedReportId')
        IcsrCaseTracking icsrCaseTracking = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTracking = icsrProfileAckService.getIcsrTrackingRecordByProcessedReportId(processedReportId, caseNumber, versionNumber)
        }
        
        exIcsrTemplateQueryId = icsrCaseTracking.exIcsrTemplateQueryId
        if (!validateCaseDataForPreview(exIcsrTemplateQueryId, caseNumber, versionNumber)) {
            flash.error = message(code: "icsr.generate.manual.generating.or.generated")
            redirect(controller: 'icsrProfileConfiguration', action: 'viewCases', params: [status: IcsrCaseStatusEnum.SCHEDULED.toString()])
            return
        }

        def icsrCaseProcessingQueue = null
        IcsrCaseProcessingQueue.'pva'.withNewSession {
            icsrCaseProcessingQueue = IcsrCaseProcessingQueue.'pva'.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, versionNumber)
        }
        if(icsrCaseProcessingQueue==null) {
            IcsrCaseProcessingQueueHist.'pva'.withNewSession {
                icsrCaseProcessingQueue = IcsrCaseProcessingQueueHist.'pva'.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, versionNumber)
            }
        }
        if (!icsrCaseProcessingQueue) {
            flash.error = message(code: "icsr.generate.manual.invalid")
            redirect(controller: 'icsrProfileConfiguration', action: 'viewCases', params: [status: IcsrCaseStatusEnum.SCHEDULED.toString()])
            return
        }

        if(icsrCaseProcessingQueue.status == IcsrCaseStatusEnum.QUALIFIED && icsrCaseProcessingQueue.isLocked) {
            ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = GrailsHibernateUtil.unwrapIfProxy(ExecutedTemplateQuery.get(exIcsrTemplateQueryId).executedConfiguration)
            if (executedIcsrProfileConfiguration.autoGenerate) {
                flash.error = message(code: "icsr.generate.manual.qualified.for.generation")
                redirect(controller: 'icsrProfileConfiguration', action: 'viewCases')
                return
            }
        }

        if (!executorThreadInfoService.availableSlotsForCasesGeneration()) {
            flash.error = message(code: "icsr.generate.manual.no.slot")
            redirect(controller: 'icsrProfileConfiguration', action: 'viewCases')
            return
        }
        if (icsrCaseProcessingQueue.id in executorThreadInfoService.totalCurrentlyGeneratingCases) {
            flash.error = message(code: "icsr.generate.manual.generating.or.generated")
            redirect(controller: 'icsrProfileConfiguration', action: 'viewCases')
            return
        }
        User currentUser = userService.currentUser
        List notificationParameters = [icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber, icsrCaseProcessingQueue.executedTemplateQueryId, isMedWatchOrCiomsTemplate(icsrCaseProcessingQueue.executedTemplateQueryId)]

        Promises.task {
            Logger logger = LoggerFactory.getLogger(IcsrController.class.getName())
            Tenants.withId(icsrCaseProcessingQueue.tenantId) {
                User.withNewSession {
                    try {
                        icsrScheduleService.generateCaseDataManual(icsrCaseProcessingQueue, status, icsrCaseTracking)
                        notificationService.addNotification(User.load(currentUser.id), 'app.notification.icsr.case.number.preview.ready', "$icsrCaseProcessingQueue.caseNumber : $icsrCaseProcessingQueue.versionNumber", notificationParameters.join(" : "), icsrCaseProcessingQueue.executionId, NotificationLevelEnum.INFO, NotificationApp.COMMENTS)
                    } catch (SQLException dbe) {
                        logger.error("Error while icsr data generation for ${icsrCaseProcessingQueue.id} ${icsrCaseProcessingQueue.caseNumber} case, error: ${dbe.message}")
                        try {
                            icsrScheduleService.updateErrorForCase(icsrCaseProcessingQueue, new Exception(dbe.getMessage(), dbe))
                        } catch (ex) {
                            logger.error("Failed to persist error for ${icsrCaseProcessingQueue.id} case generation", ex)
                        }
                        notificationService.addNotification(User.load(currentUser.id), 'app.notification.icsr.case.number.preview.failed.as.case.updated', "$icsrCaseProcessingQueue.caseNumber : $icsrCaseProcessingQueue.versionNumber", notificationParameters.join(" : "), icsrCaseProcessingQueue.executionId, NotificationLevelEnum.ERROR, NotificationApp.COMMENTS)
                    } catch (ExecutionStatusException e) {
                        logger.error("Error while icsr data generation for ${icsrCaseProcessingQueue.id} ${icsrCaseProcessingQueue.caseNumber} case, error: ${e.message}")
                        try {
                            icsrScheduleService.updateErrorForCase(icsrCaseProcessingQueue, e)
                        } catch (ex) {
                            logger.error("Failed to persist error for ${icsrCaseProcessingQueue.id} case generation", ex)
                        }
                        notificationService.addNotification(User.load(currentUser.id), 'app.notification.icsr.case.number.preview.failed', "$icsrCaseProcessingQueue.caseNumber : $icsrCaseProcessingQueue.versionNumber", notificationParameters.join(" : "), icsrCaseProcessingQueue.executionId, NotificationLevelEnum.ERROR, NotificationApp.COMMENTS)
                    } catch (e) {
                        logger.error("Fatal error while icsr data generation")
                    }
                }
            }
        }
        flash.message = message(code: "icsr.generate.manual.request.success", args: [icsrCaseProcessingQueue.caseNumber + " v" + icsrCaseProcessingQueue.versionNumber])
        redirect(action: "executionStatus", params: [status: IcsrCaseStatusEnum.SCHEDULED.toString()])
    }

    @Secured(['ROLE_ICSR_PROFILE_VIEWER'])
    def generateCaseData(Long exIcsrTemplateQueryId, String caseNumber, Integer versionNumber, String status) {
        Long processedReportId = params.long('processedReportId')
        IcsrCaseTracking icsrCaseTracking = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTracking = icsrProfileAckService.getIcsrTrackingRecordByProcessedReportId(processedReportId, caseNumber, versionNumber)
        }

        exIcsrTemplateQueryId = icsrCaseTracking.exIcsrTemplateQueryId
        if (!validateCaseDataForPreview(exIcsrTemplateQueryId, caseNumber, versionNumber)) {
            flash.error = message(code: "icsr.generate.manual.generating.or.generated")
            redirect(controller: 'icsrProfileConfiguration', action: 'viewCases', params: [status: IcsrCaseStatusEnum.SCHEDULED.toString()])
            return
        }

        IcsrCaseProcessingQueue icsrCaseProcessingQueue = null
        IcsrCaseProcessingQueue.'pva'.withNewSession {
            icsrCaseProcessingQueue = IcsrCaseProcessingQueue.'pva'.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, versionNumber)
        }
        if (!icsrCaseProcessingQueue) {
            flash.error = message(code: "icsr.generate.manual.invalid")
            redirect(controller: 'icsrProfileConfiguration', action: 'viewCases', params: [status: IcsrCaseStatusEnum.SCHEDULED.toString()])
            return
        }

        if(icsrCaseProcessingQueue.status == IcsrCaseStatusEnum.QUALIFIED && icsrCaseProcessingQueue.isLocked) {
            ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = ExecutedTemplateQuery.get(exIcsrTemplateQueryId).executedConfiguration
            if (executedIcsrProfileConfiguration.autoGenerate) {
                flash.error = message(code: "icsr.generate.manual.qualified.for.generation")
                redirect(controller: 'icsrProfileConfiguration', action: 'viewCases')
                return
            }
        }

        if (!executorThreadInfoService.availableSlotsForCasesGeneration()) {
            flash.error = message(code: "icsr.generate.manual.no.slot")
            redirect(controller: 'icsrProfileConfiguration', action: 'viewCases')
            return
        }
        if (icsrCaseProcessingQueue.id in executorThreadInfoService.totalCurrentlyGeneratingCases) {
            flash.error = message(code: "icsr.generate.manual.generating.or.generated")
            redirect(controller: 'icsrProfileConfiguration', action: 'viewCases')
            return
        }
        User currentUser = userService.currentUser
        List notificationParameters = [icsrCaseProcessingQueue.caseNumber, icsrCaseProcessingQueue.versionNumber, icsrCaseProcessingQueue.executedTemplateQueryId, isMedWatchOrCiomsTemplate(icsrCaseProcessingQueue.executedTemplateQueryId)]

        Promises.task {
            Logger logger = LoggerFactory.getLogger(IcsrController.class.getName())
            Tenants.withId(icsrCaseProcessingQueue.tenantId) {
                User.withNewSession {
                    try {
                        icsrScheduleService.generateCaseDataManual(icsrCaseProcessingQueue, status, icsrCaseTracking)
                        notificationService.addNotification(User.load(currentUser.id), 'app.notification.icsr.case.number.preview.ready', "$icsrCaseProcessingQueue.caseNumber : $icsrCaseProcessingQueue.versionNumber", notificationParameters.join(" : "), icsrCaseProcessingQueue.executionId, NotificationLevelEnum.INFO, NotificationApp.COMMENTS)
                    } catch (SQLException dbe) {
                        logger.error("Error while icsr data generation for ${icsrCaseProcessingQueue.id} ${icsrCaseProcessingQueue.caseNumber} case, error: ${dbe.message}")
                        try {
                            icsrScheduleService.updateErrorForCase(icsrCaseProcessingQueue, new Exception(dbe.getMessage(), dbe))
                        } catch (ex) {
                            logger.error("Failed to persist error for ${icsrCaseProcessingQueue.id} case generation", ex)
                        }
                        notificationService.addNotification(User.load(currentUser.id), 'app.notification.icsr.case.number.preview.failed.as.case.updated', "$icsrCaseProcessingQueue.caseNumber : $icsrCaseProcessingQueue.versionNumber", notificationParameters.join(" : "), icsrCaseProcessingQueue.executionId, NotificationLevelEnum.ERROR, NotificationApp.COMMENTS)
                    } catch (ExecutionStatusException e) {
                        logger.error("Error while icsr data generation for ${icsrCaseProcessingQueue.id} ${icsrCaseProcessingQueue.caseNumber} case, error: ${e.message}")
                        try {
                            icsrScheduleService.updateErrorForCase(icsrCaseProcessingQueue, e)
                        } catch (ex) {
                            logger.error("Failed to persist error for ${icsrCaseProcessingQueue.id} case generation", ex)
                        }
                        notificationService.addNotification(User.load(currentUser.id), 'app.notification.icsr.case.number.preview.failed', "$icsrCaseProcessingQueue.caseNumber : $icsrCaseProcessingQueue.versionNumber", notificationParameters.join(" : "), icsrCaseProcessingQueue.executionId, NotificationLevelEnum.ERROR, NotificationApp.COMMENTS)
                    } catch (e) {
                        logger.error("Fatal error while icsr data generation")
                    }
                }
            }
        }
        flash.message = message(code: "icsr.generate.manual.request.success", args: [icsrCaseProcessingQueue.caseNumber + " v" + icsrCaseProcessingQueue.versionNumber])
        redirect(action: "executionStatus", params: [status: IcsrCaseStatusEnum.SCHEDULED.toString()])
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



    Boolean isMedWatchOrCiomsTemplate(Long executedTemplateQueryId) {
        ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.get(executedTemplateQueryId)
        executedTemplateQuery.executedTemplate.isMedWatchTemplate() || executedTemplateQuery.executedTemplate.isCiomsITemplate()
    }

    //Downloads individual reports from ISR Tracking screen
    def downloadReport(Long exIcsrTemplateQueryId, String caseNumber, Integer versionNumber, Long processReportId, String prodHashCode) {
        try {
            User downloadBY = userService.currentUser
            ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.read(exIcsrTemplateQueryId)
            ExecutedReportConfiguration executedReportConfiguration = (ExecutedReportConfiguration) executedTemplateQuery?.usedConfiguration
            if (!executedReportConfiguration || !executedTemplateQuery || !caseNumber) {
                throw new Exception("No data to generate output for reportResult: ${executedTemplateQuery.reportResult.id}")
            }
            def configuration = ReportConfiguration.findByReportNameAndOwner(executedReportConfiguration.reportName, executedReportConfiguration.owner)
            Boolean isViewableByUser = configuration?.instanceOf(IcsrProfileConfiguration) ? configuration?.isViewableBy(downloadBY) : executedReportConfiguration?.isViewableBy(downloadBY)
            if (!isViewableByUser) {
                throw new Exception("User is not allowed to perform action")
            }
            String outputFormat
            File reportFile
            IcsrCaseTracking newIcsrCaseTrackingInstance = null
            IcsrCaseTracking.withNewSession {
                newIcsrCaseTrackingInstance = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.id, caseNumber, versionNumber)
            }
            Date fileDate = newIcsrCaseTrackingInstance?.generationDate
            Boolean isJapanProfile = newIcsrCaseTrackingInstance?.isJapanProfile()
            if (executedTemplateQuery.usedTemplate?.isCiomsITemplate()) { //creates CIOMS Report
                outputFormat = ReportFormatEnum.PDF.displayName
                reportFile = dynamicReportService.createCIOMSReport(caseNumber, versionNumber.intValue(), executedReportConfiguration?.owner, fileDate, isJapanProfile, executedTemplateQuery)
            } else if (executedTemplateQuery.usedTemplate?.isMedWatchTemplate()) { //Creates Medwatch report
                outputFormat = ReportFormatEnum.PDF.displayName
                reportFile = dynamicReportService.createMedWatchReport(caseNumber, versionNumber.intValue(), executedReportConfiguration?.owner, fileDate, isJapanProfile, executedTemplateQuery)
            } else {  //Creates R3XML report
                outputFormat = ReportFormatEnum.R3XML.displayName
                def xmlParameterMap = ["exIcsrTemplateQueryId": executedTemplateQuery.id, "outputFormat": outputFormat, "caseNumber": caseNumber, "versionNumber": versionNumber]
                reportFile = dynamicReportService.createR3XMLReport(executedTemplateQuery, false, xmlParameterMap, fileDate, isJapanProfile)
                outputFormat = ReportFormatEnum.XML.displayName
            }
            CaseResultData caseResultData = CaseResultData.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber(executedTemplateQuery.id,caseNumber,versionNumber)
            String reportFileName = ""
            String currentSenderIdentifier = executedReportConfiguration?.getSenderIdentifier()

            if(caseResultData) {
                reportFileName = dynamicReportService.getReportName(caseResultData, currentSenderIdentifier, false, params, fileDate, isJapanProfile)
            } else {
                reportFileName = dynamicReportService.getReportName(currentSenderIdentifier, false, params, fileDate, isJapanProfile)
            }
            GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
            webRequest.setRenderView(false)
            boolean inline = false
            MultipartFileSender.renderFile(reportFile, reportFileName, outputFormat, dynamicReportService.getContentType(outputFormat), request, response, inline)
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_DATE_TIME_FORMAT)
            AuditLogConfigUtil.logChanges(executedReportConfiguration, [outputFormat: outputFormat, fileName: reportFile.name ?: reportFileName, exportedDate: sdf.format(new Date())],
                    [:], Constants.AUDIT_LOG_EXPORT, Constants.SPACE_STRING + ViewHelper.getMessage("auditLog.entityValue.export", ReportFormatEnum.(ReportFormatEnum.valueOf(outputFormat)).displayName))
        } catch (Exception ex) {
            log.error("Exception occurred while downloading report", ex)
            flash.error = message(code: "icsr.report.download.error")
            redirect(controller: 'icsrProfileConfiguration', action: 'viewCases')
        }
    }

}
