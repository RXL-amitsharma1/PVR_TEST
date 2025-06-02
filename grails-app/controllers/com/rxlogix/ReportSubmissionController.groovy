package com.rxlogix

import com.rxlogix.config.EmailConfiguration
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ReportSubmission
import com.rxlogix.config.ReportSubmissionLateReason
import com.rxlogix.config.SubmissionAttachment
import com.rxlogix.config.publisher.PublisherReport
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import com.rxlogix.config.ExecutedIcsrReportConfiguration
import org.springframework.web.multipart.MultipartFile
import grails.core.GrailsApplication

@Secured(["isAuthenticated()"])
class ReportSubmissionController {

    def reportSubmissionService
    def reportExecutorService
    def messageSource
    def userService
    def CRUDService
    def emailService

    GrailsApplication grailsApplication

    @Secured(['ROLE_PERIODIC_CONFIGURATION_VIEW'])
    def index() {

    }

    def loadReportSubmissionForm(ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration) {
        if (!executedPeriodicReportConfiguration) {
            response.status = 500
            render([error: "Record not found"] as JSON)
            return
        }

        String primaryDest = executedPeriodicReportConfiguration.allReportingDestinations.size() > 1 ? executedPeriodicReportConfiguration.allReportingDestinations[1] : executedPeriodicReportConfiguration.allReportingDestinations[0]
        Set<String> allReportingDestinations = executedPeriodicReportConfiguration.allReportingDestinations?.collect { destination ->
            if (destination.startsWith("[") && destination.endsWith("]")) {
                String innerContent = destination.substring(1, destination.size() - 1).trim()
                if (innerContent.contains(",")) {
                    return innerContent.split(",").collect { it.trim() }
                } else {
                    return [destination.toString().trim()]
                }
            } else {
                return [destination.toString().trim()]
            }
        }.flatten() as Set

        Set<String> reportedDestination = executedPeriodicReportConfiguration.reportSubmissions*.reportingDestination?.collectMany { destination ->
            if (destination.startsWith("[") && destination.endsWith("]")) {
                String innerContent = destination.substring(1, destination.size() - 1).trim()
                if (innerContent.contains(",")) {
                    return innerContent.split(",").collect { it.trim() }
                } else {
                    return [destination.toString().trim()]
                }
            } else {
                return [destination.toString().trim()]
            }
        } as Set

        Set<String> needToSubmitToDestinations = allReportingDestinations - reportedDestination
        render template: "reportSubmissionForm", model: [reportingDestinations: needToSubmitToDestinations, primaryReportingDestination: executedPeriodicReportConfiguration.primaryReportingDestination, executedPeriodicReportConfiguration: executedPeriodicReportConfiguration, submissions: executedPeriodicReportConfiguration.reportSubmissions]
    }

    def loadIcsrReportSubmissionForm(ExecutedIcsrReportConfiguration executedIcsrReportConfiguration) {
        if (!executedIcsrReportConfiguration) {
            response.status = 500
            render([error: "Record not found"] as JSON)
            return
        }
        Set<String> needToSubmitToDestinations = executedIcsrReportConfiguration.allReportingDestinations - executedIcsrReportConfiguration.reportSubmissions*.reportingDestination
        render template: "reportSubmissionForm", model: [reportingDestinations: needToSubmitToDestinations, primaryReportingDestination: executedIcsrReportConfiguration.primaryReportingDestination, executedPeriodicReportConfiguration: executedIcsrReportConfiguration, submissions: executedIcsrReportConfiguration.reportSubmissions]
    }

    def submitReport(Long exPerConfId) {
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = ExecutedPeriodicReportConfiguration.get(exPerConfId)
        if (!executedPeriodicReportConfiguration) {
            response.status = 500
            render([error: true, msg: message(code: "default.system.error.message")] as JSON)
            return
        }
        if (params.emailConfiguration) bindEmailConfig(executedPeriodicReportConfiguration)
        processSubmit(executedPeriodicReportConfiguration)

    }

    void bindEmailConfig(configurationInstance) {
        def emailConfiguration = params.emailConfiguration
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
            }
        }
    }

    void processSubmit(ExecutedReportConfiguration executedConfiguration) {


        try {
            Set<String> reportingDestinations = params.list("reportingDestinations") as Set
            List attachmentLst = []

            if (executedConfiguration.isPublisherReport) {
                PublisherReport publisherReport = PublisherReport.get(params.long("publisherDocument"))
                if (publisherReport) {
                    attachmentLst.add([
                            name       : publisherReport.name + ".docx",
                            data       : publisherReport.data,
                            dateCreated: publisherReport.dateCreated,
                            type       : 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
                    ])
                } else {
                    response.status = 500
                    Locale locale = userService.currentUser?.preference?.locale
                    def stringsByField = [message(code: "submission.publisher.error")]
                    render([error: true, errorMsg: stringsByField, defaultMsg: message(code: "default.system.error.message")] as JSON)
                    return
                }
            } else {
                request.getFiles('file')?.each { MultipartFile file ->
                    if (file.size > 0) {
                        attachmentLst.add([
                                name       : file.originalFilename,
                                data       : file.bytes,
                                dateCreated: new Date(),
                                type       : file.contentType
                        ])
                    }
                }
            }
            List submissions = reportSubmissionService.submitReport(executedConfiguration, reportingDestinations, params, attachmentLst)
            Date submitDate = Date.parse("yyyy-MM-dd'T'HH:mmXXX", JSON.parse(params.scheduleDateJSON)?.startDateTime)
            String format
            switch(userService.currentUser?.preference?.locale) {
                case Locale.JAPANESE : format = "yyyy/MM/dd"
                    break
                default : format = "dd-MMM-yyyy"
                    break
            }
            Date dueDate = new Date(Date.parse(format, params.dueDate).getTime() + 24L * 60 * 60 * 1000 - 1)
            String capa = (submitDate > dueDate ? (submissions?.collect { [id: it.id, destination: it.reportingDestination] } as JSON) : "")

            if (submissions.size() == reportingDestinations.size()) {
                flash.message = message(code: "app.reportSubmission.submitted.report.successful");
                render([success: true, capa: capa, configurationId: executedConfiguration.id, message: message(code: "app.reportSubmission.submitted.report.successful")] as JSON)
            } else {
                flash.message = message(code: "app.reportSubmission.submitted.warn");
                render([success: true, capa: capa, configurationId: executedConfiguration.id, message: message(code: "app.reportSubmission.submitted.warn")] as JSON)
            }

        } catch (Exception ve) {
            log.error("Report Destination Got Exception",ve)
            response.status = 500
            Locale locale = userService.currentUser?.preference?.locale
            def stringsByField = []
            String errorMessage = message(code: "default.system.error.message")
            if (ve instanceof ValidationException) {
                for (error in ve.errors.allErrors) {
                    String message = messageSource.getMessage(error, locale)
                    stringsByField << message
                }
            }else {
                errorMessage = ve.getMessage()
            }

            render([error: true, errorMsg: stringsByField, defaultMsg: errorMessage] as JSON)
        }
    }

    def submitIcsrReport(Long exPerConfId) {
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = ExecutedIcsrReportConfiguration.get(exPerConfId)
        if (!executedIcsrReportConfiguration) {
            response.status = 500
            render([error: true, msg: message(code: "default.system.error.message")] as JSON)
            return
        }

        try {
            Set<String> reportingDestinations = params.list("reportingDestinations") as Set
            reportSubmissionService.submitReport(executedIcsrReportConfiguration, reportingDestinations as Set<String>, params)
            flash.message = message(code: "app.reportSubmission.submitted.report.successful");
            render([success: true, message: message(code: "app.reportSubmission.submitted.report.successful")] as JSON)
        } catch (ValidationException ve) {
            log.warn("Validation Error during reportSubmission -> submitIcsrReport")
            response.status = 500
            Locale locale = userService.currentUser?.preference?.locale
            def stringsByField = []
            for (error in ve.errors.allErrors) {
                String message = messageSource.getMessage(error, locale)
                stringsByField << message
            }

            render([error: true, errorMsg: stringsByField, defaultMsg: message(code: "default.system.error.message")] as JSON)
        }
    }

    def viewCases(ReportSubmission reportSubmission) {
        render view: "viewCases", model: [reportSubmission: reportSubmission]
    }

    def downloadAttachment() {
        SubmissionAttachment attachment = SubmissionAttachment.get(params.long("id"))
        if (!attachment) {
            notFound()
            return
        }
        render(file: attachment.data, fileName: attachment.name, contentType: "application/octet-stream")
    }

    def updateLate() {
        ReportSubmission submission = ReportSubmission.get(params.long("id"))
        submission.lateReasons?.collect { it }?.each {
            it.delete()
        }
        submission.lateReasons?.clear()
        boolean isPrimary = true
        params.reason?.eachWithIndex { r, i ->
            if (params.responsible[i] && params.reason[i] && i > 0) {
                ReportSubmissionLateReason reason = new ReportSubmissionLateReason(responsible: params.responsible[i], reason: params.reason[i], isPrimary: isPrimary)
                userService.setOwnershipAndModifier(reason)
                submission.addToLateReasons(reason)
                isPrimary = false
            }

        }
        submission.late = params.late;
        CRUDService.saveOrUpdate(submission)
        redirect(action: "index")
    }
}
