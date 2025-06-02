package com.rxlogix

import com.rxlogix.commandObjects.CaseSubmissionCO
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.IcsrCaseSubmission
import com.rxlogix.config.ReportSubmission
import com.rxlogix.config.ReportSubmissionLateReason
import com.rxlogix.config.SubmissionAttachment
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportFormatEnum
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.databinding.DataBindingUtils
import com.rxlogix.config.IcsrCaseTracking

@Transactional
class ReportSubmissionService {

    def CRUDService
    def reportExecutorService
    def userService
    def emailService
    def icsrProfileAckService

    def saveUpdateSubmission(ReportSubmission reportSubmission) {
        if (!reportSubmission.id)
            CRUDService.save(reportSubmission)
        else
            CRUDService.update(reportSubmission)
        ExecutedReportConfiguration executedReportConfiguration = reportSubmission.executedReportConfiguration
        executedReportConfiguration.status = ReportExecutionStatusEnum.SUBMITTED
        CRUDService.update(executedReportConfiguration)
    }

    List submitReport(ExecutedReportConfiguration executedPeriodicReportConfiguration, Set<String> reportingDestinations, def params, List attachmentLst = []) {
        List result = []
        String primaryDestination = executedPeriodicReportConfiguration.primaryReportingDestination
        reportingDestinations.each {
            if (ReportSubmission.countByExecutedReportConfigurationAndReportingDestination(executedPeriodicReportConfiguration, it)) {
                log.warn("For report ${executedPeriodicReportConfiguration.reportName}(${executedPeriodicReportConfiguration.id}) and  destination ${it}, its already submitted. so skipping..")
                return
            }
            ReportSubmission reportSubmission = new ReportSubmission(executedReportConfiguration: executedPeriodicReportConfiguration)
            reportSubmission.tenantId = executedPeriodicReportConfiguration.tenantId
            reportSubmission.submissionDate = Date.parse("yyyy-MM-dd'T'HH:mmXXX", JSON.parse(params.scheduleDateJSON)?.startDateTime)
            DataBindingUtils.bindObjectToInstance(reportSubmission, params, DataBindingUtils.getBindingIncludeList(reportSubmission), ["submissionDate"], null);
            reportSubmission.reportingDestination = it
            if (reportSubmission.reportingDestination == primaryDestination) {
                reportSubmission.isPrimary = true
            }
            boolean isPrimary = true
            if (params.late) reportSubmission.late = params.late
            if (params.reason) {
                if (params.reason instanceof String) {
                    if (params.responsible && (params.responsible instanceof String)) {
                        ReportSubmissionLateReason reason = new ReportSubmissionLateReason(responsible: params.responsible, reason: params.reason, isPrimary: true)
                        userService.setOwnershipAndModifier(reason)
                        reportSubmission.addToLateReasons(reason)
                    }
                } else
                    params.reason?.eachWithIndex { r, i ->
                        if (params.responsible[i] && params.reason[i] && i > 0) {
                            ReportSubmissionLateReason reason = new ReportSubmissionLateReason(responsible: params.responsible[i], reason: params.reason[i], isPrimary: isPrimary)
                            userService.setOwnershipAndModifier(reason)
                            reportSubmission.addToLateReasons(reason)
                            isPrimary = false
                        }
                    }
            }
            attachmentLst?.each {
                reportSubmission.addToAttachments(new SubmissionAttachment(name: it.name, data: it.data, dateCreated: it.dateCreated))
            }
            saveUpdateSubmission(reportSubmission)
            result << reportSubmission
            reportExecutorService.logDestinationToSubmissionHistory(reportSubmission)
            if (executedPeriodicReportConfiguration.periodicReportType == PeriodicReportTypeEnum.JPSR || executedPeriodicReportConfiguration.periodicReportType == PeriodicReportTypeEnum.RESD) {
                reportExecutorService.logJPSRDestinationToSubmissionHistory(reportSubmission)
            }
        }
        emailSubmission(params, executedPeriodicReportConfiguration, attachmentLst)
        return result
    }

    private List getListParam(Map params, String name) {
        if (!params[name]) return []
        if (params[name] instanceof String) {
            return [params[name]]
        } else {
            return params[name]
        }
    }

    private emailSubmission(Map params, ExecutedPeriodicReportConfiguration executedConfiguration, List attachments) {
        List<String> emailList = getListParam(params, "emailToUsers")
        if (executedConfiguration && emailList) {
            List<ReportFormatEnum> formats = getListParam(params, "attachmentFormats")?.collect { ReportFormatEnum.valueOf(it) }
            List submissionAttachments = params.submittingDocument == "on" ? attachments : null
            emailService.sendReport(executedConfiguration, emailList?.toArray(new String[0]), formats?.toArray(new ReportFormatEnum[0]), false, submissionAttachments)
        }
    }

    void submitIcsrCase(Long tenantId, CaseSubmissionCO caseSubmissionCO) {
        reportExecutorService.logIcsrCaseToSubmissionHistory(tenantId, caseSubmissionCO)
    }

    String getPreviousState(String icsrCaseId, String profileName) {
        String exProfileId
        String exIcsrTemplateQueryId
        String caseNumber
        String versionNumber
        (exProfileId, exIcsrTemplateQueryId, caseNumber, versionNumber) = icsrCaseId?.split("\\*\\*")

        Long processedReportIdList = null
        IcsrCaseSubmission previousStateEntry
        String previousState = null
        IcsrCaseSubmission.'pva'.withNewSession {
            processedReportIdList = IcsrCaseSubmission.fetchIcsrCaseSubmissionByCaseNoAndVersionNo(profileName, exIcsrTemplateQueryId as Long, caseNumber, versionNumber as Long).get()
            def previousStateEntries = IcsrCaseSubmission.findAllByProcessedReportIdAndE2bStatusNotInList(
                    processedReportIdList, ["SUBMISSION_NOT_REQUIRED", "SUBMISSION_NOT_REQUIRED_FINAL"], [sort: 'e2bProcessId', order: 'desc', max: 1])
            previousStateEntry = previousStateEntries ? previousStateEntries.first() : null
            previousState = previousStateEntry?.e2bStatus
        }
        return previousState
    }
}
