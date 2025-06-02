package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.customException.CustomJasperException
import com.rxlogix.enums.*
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.util.Holders
import grails.web.mapping.LinkGenerator
import groovy.time.TimeCategory
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.exception.DRException
import net.sf.jasperreports.engine.JRRuntimeException
import net.sf.jasperreports.governors.GovernorException
import net.sf.jasperreports.governors.MaxPagesGovernorException
import net.sf.jasperreports.governors.TimeoutGovernorException
import oracle.sql.TIMESTAMP
import org.hibernate.FetchMode
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.io.ResourceLoader
import groovy.sql.Sql
import java.sql.Timestamp

import java.text.SimpleDateFormat
import java.util.regex.Pattern
import groovy.json.JsonSlurper

class EmailService implements ResourceLoaderAware {
    public static final String DATE_FMT = "dd-MMM-yyyy hh:mm a"
    def dynamicReportService
    ResourceLoader resourceLoader
    GrailsApplication grailsApplication
    LinkGenerator grailsLinkGenerator
    def groovyPageRenderer
    def userService
    def configurationService
    def etlJobService
    def actionItemService
    def publisherService
    def icsrProfileAckService
    def utilService
    def reasonOfDelayService
    def qualityService


    def emailReportTo(def configuration, List<String> recipients, List<ReportFormatEnum> outputs) {
        //Do not send mail if all the sections of the report return 0 rows and "Do not send email when the report returns no data".
        if (sendMailCheck(configuration)) {
            try{
                sendReport(configuration, recipients?.toArray(new String[0]), outputs?.toArray(new ReportFormatEnum[0]), true)
            }
            catch (Exception e){
                log.error("Unexpected error", e)
                throw e
            }
        }
    }


    def emailReportOutput(def configuration) {
        //Do not send mail if all the sections of the report return 0 rows and "Do not send email when the report returns no data".
        if (configuration?.executedDeliveryOption && sendMailCheck(configuration)) {
            String[] recipients = configuration.executedDeliveryOption.emailToUsers?.toArray()
            ReportFormatEnum[] outputs = configuration.executedDeliveryOption.attachmentFormats?.toArray()
            try{
                sendReport(configuration, recipients, outputs, false)
            }
            catch(Exception e){
                log.error("Unexpected error", e)
                throw e.getCause()
            }
        }
    }

    void emailFailureNotification(ExecutionStatus executionStatus) {
        if (!executionStatus) {
            return
        }
        def config = grailsApplication.config
        ReportConfiguration configuration = executionStatus.getEntityClass().get(executionStatus.entityId)
        String userTimezone = configuration?.owner?.preference?.timeZone
        //Do not send mail if all the sections of the report return 0 rows and "Do not send email when the report returns no data".
        if (configuration?.deliveryOption) {

            String runDate = executionStatus.nextRunDate.format(DateUtil.DATEPICKER_FORMAT_AM_PM, TimeZone.getTimeZone(userTimezone))
            String[] recipients = configuration.deliveryOption?.emailToUsers?.toArray()
            String emailSubject = ""
            String messageBody = ""
            if(executionStatus.executionStatus == ReportExecutionStatusEnum.ERROR){
                emailSubject = ViewHelper.getMessage("app.emailService.failure.notification.subject.label",configuration.reportName)
                messageBody = ViewHelper.getMessage("app.emailService.failure.notification.message.label",configuration.reportName,runDate)
                if(grailsApplication.config.grails.appBaseURL)
                    messageBody += "\n"+ ViewHelper.getMessage("app.emailService.failure.notification.reason.label",grailsLinkGenerator.link(controller: "executionStatus", action: "reportExecutionError", id: executionStatus.id, base: grailsApplication.config.grails.appBaseURL, absolute: true))
            }
            else{
                emailSubject = ViewHelper.getMessage("app.emailService.warning.notification.subject.label",configuration.reportName)
                messageBody = ViewHelper.getMessage("app.emailService.warning.notification.message.label",configuration.reportName,runDate)
                if(grailsApplication.config.grails.appBaseURL)
                    messageBody += "\n"+ ViewHelper.getMessage("app.emailService.failure.notification.reason.label",grailsLinkGenerator.link(controller: "executionStatus", action: "reportExecutionError", id: executionStatus.id, base: grailsApplication.config.grails.appBaseURL, absolute: true))
            }
            String[] emailCc = []
            String ownerEmail = configuration?.owner?.email
            if (grailsApplication.config.getProperty('pvreports.email.send.failure.email.toOwnerOnly', Boolean)) {
                recipients = [ownerEmail].toArray()
            } else if (grailsApplication.config.getProperty('pvreports.email.send.failure.emailTo.configuredFromUser', Boolean)) {
                List<String> emailTo = grailsApplication.config.getProperty("pvreports.mail.default.to", List)?.collect { it }
                emailTo.add(ownerEmail)
                recipients = emailTo.unique().toArray()
            } else {
                recipients = configuration.deliveryOption?.emailToUsers?.toArray()
                if (configuration.emailConfiguration?.cc && !configuration.emailConfiguration.isDeleted) {
                    emailCc = configuration.emailConfiguration?.cc?.split(",")
                }
            }
            sendFailureEmail(recipients, messageBody, true, emailSubject, emailCc)
        }
    }

    void sendReport(def configuration, String[] recipients, ReportFormatEnum[] outputs, boolean asyVal, List additionalAttachments=[]) {
        LocaleContextHolder.setLocale(userService.currentUser?.preference?.locale ?: configuration.owner.preference.locale)
        //Check if email is not null & valid.
        try {
            recipients = recipients.findAll { it }
            recipients = getAllValidEmailIds(recipients)
            if (recipients) {
                long totalFileSize = additionalAttachments?.sum { it.data?.length ?: 0 } ?: 0
                List files = additionalAttachments?:[]
                Boolean useEmailConfiguration = configuration.emailConfiguration && !configuration.emailConfiguration.isDeleted
                String emailSubject = null
                if (configuration instanceof com.rxlogix.config.Capa8D) {
                    emailSubject = (configuration.emailConfiguration?.subject && useEmailConfiguration) ? insertValues(configuration.emailConfiguration?.subject, configuration) : "[pv-reports] Report delivery for: \"${configuration.issueNumber}\""
                } else {
                    emailSubject = (configuration.emailConfiguration?.subject && useEmailConfiguration) ? insertValues(configuration.emailConfiguration?.subject, configuration) : insertValues(grailsApplication.config.mail.default.delivery.subject, configuration)
                }
                if(Holders.config.getProperty('reports.email.subject.prepend')){
                    emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
                }
                def reportParams = [:]
                if (useEmailConfiguration) {
                    reportParams = [
                            pageOrientation     : PageOrientation.LANDSCAPE,
                            paperSize           : configuration.emailConfiguration.paperSize,
                            sensitivityLabel    : configuration.emailConfiguration.sensitivityLabel,
                            showPageNumbering   : configuration.emailConfiguration.showPageNumbering,
                            excludeCriteriaSheet: configuration.emailConfiguration.excludeCriteriaSheet,
                            excludeAppendix     : configuration.emailConfiguration.excludeAppendix,
                            excludeComments     : configuration.emailConfiguration.excludeComments,
                            excludeLegend       : configuration.emailConfiguration.excludeLegend,
                            showCompanyLogo     : configuration.emailConfiguration.showCompanyLogo,
                            advancedOptions     : "1"
                    ]
                }
                String emailBodyCustom = ""
                if (configuration.emailConfiguration?.body && useEmailConfiguration) {
                    Map emailBodyCustomMap = insertEmailBodyValues(configuration.emailConfiguration?.body, configuration, reportParams)
                    emailBodyCustom = emailBodyCustomMap.text
                    List charts = emailBodyCustomMap.attachments
                    files.addAll(charts)
                    totalFileSize += charts?.sum { it.size } ?: 0
                }
                String[] emailCc = (configuration.emailConfiguration?.cc && useEmailConfiguration) ? configuration.emailConfiguration?.cc?.split(",") : []
                if (configuration instanceof ExecutedIcsrReportConfiguration || configuration instanceof ExecutedIcsrProfileConfiguration) {
                    String emailBodyMessage = ViewHelper.getMessage("app.emailService.result.nourl.message.label")
                    if (grailsApplication.config.grails.appBaseURL) {
                        String reportOutputLink = grailsLinkGenerator.link(controller: "executedIcsrProfile", action: "showResult", id: configuration.id, base: grailsApplication.config.grails.appBaseURL, absolute: true)
                        if (configuration instanceof ExecutedIcsrReportConfiguration) {
                            reportOutputLink = grailsLinkGenerator.link(controller: "icsrReport", action: "showResult", id: configuration.id, base: grailsApplication.config.grails.appBaseURL, absolute: true)
                        }
                        emailBodyMessage = ViewHelper.getMessage("app.emailService.result.message.label", reportOutputLink)
                    }
                    sendEmailWithFiles(recipients, emailCc, emailSubject, formMessage(configuration, emailBodyCustom, emailBodyMessage, null), asyVal, [])
                    return
                }
                Boolean hasPPTXFormat = outputs.any { it == ReportFormatEnum.PPTX }
                if (!(configuration instanceof com.rxlogix.config.Capa8D)) {
                    if (!(configuration instanceof ExecutedCaseSeries) && dynamicReportService.isLargeReportResult(configuration, false, hasPPTXFormat)) {
                        log.info("#### File size is too large for ${configuration.id}. So we are not able to mail to ${recipients} with ${outputs} ######")
                        String emailBodyMessage = ViewHelper.getMessage("app.emailService.run.message.label", configuration.reportName, ViewHelper.formatRunDateAndTime(configuration));
                        if (emailBodyCustom) {
                            emailBodyCustom += emailBodyMessage + reportIsLargeMessage(configuration)
                        } else {
                            emailBodyMessage += reportIsLargeMessage(configuration)
                        }
                        sendMail {
                            multipart false
                            to recipients
                            if (emailCc) {
                                cc emailCc
                            }
                            // if this remains async then there is no delivery exceptions we can report on... tho its much faster
                            async asyVal
                            subject emailSubject
                            html formMessage(configuration, emailBodyCustom, emailBodyMessage, null)
                        }
                        return
                    }
                }

                String reportFileName = null
                if (configuration instanceof Capa8D) {
                    reportFileName = configuration.issueNumber
                } else {
                    reportFileName = ((configuration instanceof ExecutedCaseSeries) ? configuration.reportName : dynamicReportService.getReportNameAsFileName(configuration))
                }
                outputs?.each {
                    if (totalFileSize < grailsApplication.config.pvreports.email.max.bytes) {
                        File reportFile

                        if (useEmailConfiguration) {
                            reportParams.pageOrientation = (it in [ReportFormatEnum.PDF, ReportFormatEnum.DOCX]) ? reportParams.pageOrientation : PageOrientation.LANDSCAPE
                        }

                        if (configuration instanceof ExecutedPeriodicReportConfiguration && configuration.status == ReportExecutionStatusEnum.GENERATED_CASES) {
                            reportFile = dynamicReportService.createCaseListReport(configuration.caseSeries, [outputFormat: it.name()] << reportParams)
                        } else if (configuration instanceof ExecutedCaseSeries) {
                            reportFile = dynamicReportService.createCaseListReport(configuration, [outputFormat: it.name(), showVersionColumn: ApplicationSettings.first().defaultUiSettings ? "false" : "true", notEmptyOnly: configuration.emailConfiguration?.noEmailOnNoData] << reportParams)
                        } else if (configuration instanceof Capa8D) {
                            reportFile = dynamicReportService.createSingleCapa8dReport(configuration, [outputFormat: it.name()] << reportParams)
                        } else {
                            reportFile = dynamicReportService.createMultiTemplateReport(configuration, [outputFormat: it.name()] << reportParams)
                        }
                        if (reportFile) {
                            totalFileSize += reportFile.length()
                            if (totalFileSize < grailsApplication.config.pvreports.email.max.bytes)
                                files.add([type: dynamicReportService.getContentType(it),
                                           name: "${reportFileName}.${it.name()}",
                                           data: reportFile.getBytes()
                                ])
                        }
                    }
                }
                if (!(configuration instanceof Capa8D)) {
                    if (!outputs && configuration.executedDeliveryOption?.additionalAttachments && ((configuration instanceof ExecutedConfiguration) ||
                            (configuration instanceof ExecutedPeriodicReportConfiguration && configuration.status != ReportExecutionStatusEnum.GENERATED_CASES))) {
                        def attachments = JSON.parse(configuration.executedDeliveryOption?.additionalAttachments)
                        int i = 0;
                        List<List<Integer>> tempQueriesIndexesList=[]
                        int indexToAdd=0
                        def runningInstanceId = ExecutionStatus.findByExecutedEntityId(configuration.id).entityId
                        def runningInstance = ReportConfiguration.get(runningInstanceId)
                        runningInstance?.templateQueries?.each {
                            if (it.usedTemplate.templateType == TemplateTypeEnum.TEMPLATE_SET && it.usedTemplate.sectionBreakByEachTemplate) {
                                List<Integer> tempSetIndexes = []
                                (indexToAdd..(indexToAdd + it.usedTemplate.nestedTemplates.size() - 1)).each { itr ->
                                    tempSetIndexes.add(itr)
                                }
                                tempQueriesIndexesList.add(tempSetIndexes)
                                indexToAdd += it.usedTemplate.nestedTemplates.size()
                            } else {
                                tempQueriesIndexesList.add([indexToAdd])
                                indexToAdd += 1
                            }
                        }
                        attachments.each { row ->
                            def sectionsToExport = []
                            row.sections.each { sect ->
                                int index = sect as Integer
                                if (index < configuration.executedTemplateQueries.size()) {
                                    tempQueriesIndexesList?.get(index).each { idx ->
                                        sectionsToExport << ((ExecutedReportConfiguration) configuration).executedTemplateQueries.get(idx).id
                                    }
                                }
                            }
                            row.formats.each { fmt ->
                                if (totalFileSize < grailsApplication.config.pvreports.email.max.bytes) {
                                    i++
                                    ReportFormatEnum format = fmt as ReportFormatEnum

                                    if (useEmailConfiguration) {
                                        reportParams.pageOrientation = (format in [ReportFormatEnum.PDF, ReportFormatEnum.DOCX]) ? reportParams.pageOrientation : PageOrientation.LANDSCAPE
                                    }
                                    File reportFile = dynamicReportService.createMultiTemplateReport(configuration, [sectionsToExport: sectionsToExport, outputFormat: format.name()] << reportParams)
                                    if (reportFile) {
                                        totalFileSize += reportFile.length()
                                        if (totalFileSize < grailsApplication.config.pvreports.email.max.bytes)
                                            files.add([type: dynamicReportService.getContentType(format),
                                                       name: "${reportFileName} ${(i < 10 ? ('0' + i) : i)}.${format.name()}",
                                                       data: reportFile.getBytes()
                                            ])
                                    }
                                    reportFile.delete()
                                }
                            }
                        }
                    }
                }

                log.info("Sending email to ${recipients}")
                String runDate = null
                String emailBodyMessage = ""
                if (!(configuration instanceof Capa8D)) {
                    runDate = ViewHelper.formatRunDateAndTime(configuration)
                    emailBodyMessage = ViewHelper.getMessage("app.emailService.run.message.label", configuration.reportName, runDate)
                }

                if (totalFileSize == 0L || totalFileSize >= grailsApplication.config.pvreports.email.max.bytes) {
                    if (totalFileSize > 0) {
                        if (emailBodyCustom) {
                            emailBodyCustom += emailBodyMessage + reportIsLargeMessage(configuration)
                        } else {
                            emailBodyMessage += reportIsLargeMessage(configuration)
                        }
                    }

                    sendMail {
                        multipart true
                        to recipients
                        if (emailCc) {
                            cc emailCc
                        }
                        // if this remains async then there is no delivery exceptions we can report on... tho its much faster
                        async asyVal
                        subject emailSubject
                        html formMessage(configuration, emailBodyCustom, emailBodyMessage, null)
                    }
                } else {
                    sendMail {
                        multipart true
                        to recipients
                        if (emailCc) {
                            cc emailCc
                        }
                        // if this remains async then there is no delivery exceptions we can report on... tho its much faster
                        async asyVal
                        subject emailSubject
                        html formMessage(configuration, emailBodyCustom, emailBodyMessage, null)
                        files.each { file ->
                            if (file.contentId)
                                inline file.contentId, 'image/jpg', new File(file.path)
                            else
                                attachBytes file.name, file.type, file.data
                        }
                    }
                }
                recipients.each { recipient ->
                    outputs.each { output ->
                        AuditLogConfigUtil.logChanges(configuration, [outputFormat: output.name(), fileName: reportFileName, emailSentOn:new Date()], [:], Constants.AUDIT_LOG_EXPORT, " " + ViewHelper.getMessage("auditLog.entityValue.email", User.findByEmail(recipient) ? User.findByEmail(recipient).fullName : Email.findByEmail(recipient).description, recipient, output.displayName))
                    }
                }
            } else {
                log.debug("Not emailing report, recipient list [${recipients}] or output formats [${outputs}] is empty")
            }
        } catch (GovernorException ge) {
            log.error(ge.message)
            if (ge instanceof MaxPagesGovernorException) {
                throw new CustomJasperException("Exceed max pages ${ge.maxPages} limits of jasper for ${configuration.reportName}", ge).initCause(ge.cause)

            } else if (ge instanceof TimeoutGovernorException) {
                throw new CustomJasperException("Exceed timeout ${ge.timeout} limits of jasper for ${configuration.reportName}", ge).initCause(ge.cause)
            }
        } catch (DRException e) {
            log.error(e.message)
            if (e.cause instanceof MaxPagesGovernorException) {
                throw new CustomJasperException("Exceed max pages ${e.cause.maxPages} limits of jasper for ${configuration.reportName}", e).initCause(e.cause)
            } else if (e.cause instanceof TimeoutGovernorException) {
                throw new CustomJasperException("Exceed timeout ${e.cause.timeout} limits of jasper for ${configuration.reportName}", e).initCause(e.cause)
            } else {
                throw e.initCause(e.cause)
            }
        } catch (JRRuntimeException e) {
            log.error(e.message, e)
            throw new CustomJasperException("Number of columns exceed limit of output page size of jasper for ${configuration.reportName}", e).initCause(e.cause)
        } finally{
            Locale defaultLocale = new Locale(System.properties.get("user.language")?:"en")
            LocaleContextHolder.setLocale(defaultLocale)
        }
    }

    String reportIsLargeMessage(def configuration){
        String result = "<br>"+ViewHelper.getMessage("app.emailService.result.nourl.message.label")

        if (grailsApplication.config.grails.appBaseURL) {
            result += "<br>" + getReportRefMessage(configuration)
        }
        return result
    }

    String getReportRefMessage(configuration){
        ViewHelper.getMessage("app.emailService.result.message.label", grailsLinkGenerator.link(controller: "report", action: "showFirstSection", id: configuration.id, base: grailsApplication.config.grails.appBaseURL, absolute: true))
    }

    def formMessage(configuration, String emailBodyCustom, String emailBodyMessage, String emailBodyError) {
        String timeZone = userService.currentUser?.preference?.timeZone
        def url = null
        if (grailsApplication.config.grails.appBaseURL) {
            if (configuration instanceof ExecutedCaseSeries)
                url = grailsLinkGenerator.link(controller: "caseList", action: "index", base: grailsApplication.config.grails.appBaseURL, absolute: true) + "?cid=" + configuration.id
            else if (configuration instanceof  Capa8D)
                url = grailsLinkGenerator.link(controller: "issue", action: "view", id: configuration.id, base: grailsApplication.config.grails.appBaseURL, absolute: true)
            else
                url = grailsLinkGenerator.link(controller: "report", action: "showFirstSection", id: configuration.id, base: grailsApplication.config.grails.appBaseURL, absolute: true)
        }
        if (configuration instanceof  Capa8D){
            return groovyPageRenderer.render(template: '/mail/issue/capa8d',
                    model: ['capaInstance': configuration, 'emailBodyCustom': emailBodyCustom, 'emailBodyMessage': emailBodyMessage, 'emailBodyError': emailBodyError, url: url, 'userTimeZone': timeZone])
        }
        return groovyPageRenderer.render(template: '/mail/report/deliveryReport',
                model: ['executedConfiguration': configuration, 'emailBodyCustom': emailBodyCustom, 'emailBodyMessage': emailBodyMessage, 'emailBodyError': emailBodyError, url: url, 'userTimeZone': timeZone])
    }

    /**
     * This method sends notification to the to the sender.
     * @param recipients
     * @param messageBody
     * @param recipients
     */
    void sendNotificationEmail(def recipients, def messageBody, boolean asyVal, String emailSubject,String[] emailCc = []) {
        //Check if email is not null & valid.
        recipients = recipients.findAll { it }
        emailCc = emailCc ? emailCc.findAll { it } : new String[0]
        List validRecipients = getAllValidEmailIds(recipients)
        List validCcs = getAllValidEmailIds(emailCc)
        if (validRecipients) {
            log.info("Sending email with subject ${emailSubject} to ${validRecipients} and cc ${validCcs}")
            if(Holders.config.getProperty('reports.email.subject.prepend')){
                emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
            }
            def recipientsEmail = recipients?.toArray()
            def ccEmail = validCcs?.toArray()

            sendMail {
                multipart true
                async asyVal
                // if this remains async then there is no delivery exceptions we can report on... tho its much faster
                to recipientsEmail
                if (ccEmail) {
                    cc ccEmail
                }
                subject emailSubject
                html(messageBody)
                inline 'pvreportsMailBackground', 'image/jpg', resourceLoader.getResource("/images/background.jpg")?.getFile()
                inline 'pvreportslogo', 'image/jpg', resourceLoader.getResource("/images/pv_reports_logo.png")?.getFile()
            }
        } else {
            log.debug("Cancel email sending as no recipients with valid email id")
        }
    }

    void sendEmail(def recipients, def emailBodyMessage, boolean asyVal, String emailSubject, String[] emailCc = []) {
        recipients = recipients.findAll { it }
        emailCc = emailCc ? emailCc.findAll { it } : new String[0]
        List validRecipients = getAllValidEmailIds(recipients)
        List validCcs = getAllValidEmailIds(emailCc)
        if (validRecipients) {
            log.info("Sending email with subject ${emailSubject} to ${validRecipients} and cc ${validCcs}")
            String body = groovyPageRenderer.render(template: '/mail/plain', model: ['emailBodyMessage': emailBodyMessage])
            if(Holders.config.getProperty('reports.email.subject.prepend')){
                emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
            }
            def recipientsEmail = validRecipients?.toArray()
            def ccEmail = validCcs?.toArray()

            sendMail {
                multipart true
                to recipientsEmail
                if (ccEmail) {
                    cc ccEmail
                }
                async asyVal
                subject emailSubject
                html body
                inline 'pvreportsMailBackground', 'image/jpg', resourceLoader.getResource("/images/background.jpg")?.getFile()
                inline 'pvreportslogo', 'image/jpg', resourceLoader.getResource("/images/pv_reports_logo.png")?.getFile()
            }
        } else {
            log.debug("Cancel email sending as no recipients with valid email id")
        }
    }

    void sendFailureEmail(def recipients, def messageBody, boolean asyVal, String emailSubject, String[] emailCc = []) {
        //Check if email is not null & valid.
        recipients = recipients.findAll { it }
        emailCc = emailCc.findAll { it }
        List validRecipients = getAllValidEmailIds(recipients)
        List validCcs = getAllValidEmailIds(emailCc)
        if (validRecipients) {
            log.info("Sending email with subject ${emailSubject} to ${validRecipients} and cc ${validCcs}")
            if(Holders.config.getProperty('reports.email.subject.prepend')){
                emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
            }
            def recipientsEmail = recipients?.toArray()

            sendMail {
                multipart true
                async asyVal // if this remains async then there is no delivery exceptions we can report on... tho its much faster
                to recipientsEmail
                if (emailCc) {
                    cc emailCc
                }
                subject emailSubject
                body messageBody
            }
        } else {
            log.debug("Cancel email sending as no recipients with valid email id")
        }
    }

    def sendActionItemNotifications() {
        String baseUrl

        if (grailsApplication.config.grails.appBaseURL)
            baseUrl = grailsLinkGenerator.link(controller: "actionItem", action: "index", base: grailsApplication.config.grails.appBaseURL, absolute: true) + "?id="

        getActionItemNotifications().each { ActionItemNotification actionItemNotification ->
            User user = actionItemNotification.user
            String timeZone = user?.preference?.timeZone
            if (actionItemNotification.isNotEmpty()) {
                String content = groovyPageRenderer.render(template: '/mail/actionItem/notification',
                        model: ['actionItemNotification': actionItemNotification, url: baseUrl, 'userTimeZone': timeZone])

                String recipientsEmail = actionItemService.getRecipientsByEmailPreference(user, Constants.JOB)
                String emailSubject = ViewHelper.getMessage("app.notification.actionItem.mail.subject")
                if(Holders.config.getProperty('reports.email.subject.prepend')){
                    emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
                }
                if (recipientsEmail && recipientsEmail.trim().length() != 0){
                    log.info("Sending email Action Itm Notification to ${recipientsEmail}")
                    sendMail {
                        multipart true
                        async true
                        to recipientsEmail
                        subject emailSubject
                        html(content)
                        inline 'pvreportsMailBackground', 'image/jpg', resourceLoader.getResource("/images/background.jpg")?.getFile()
                        inline 'pvreportslogo', 'image/jpg', resourceLoader.getResource("/images/pv_reports_logo.png")?.getFile()
                    }
                }
            }
        }
    }

    List<ActionItemNotification> getActionItemNotifications() {
        List<ActionItem> actionItemList = ActionItem.createCriteria().list {
            not{
                eq('status',StatusEnum.CLOSED)
            }
            eq('isDeleted',false)
            fetchMode("assignedTo", FetchMode.JOIN)
            fetchMode("assignedGroupTo", FetchMode.JOIN)
        }
        Map<Long, ActionItemNotification> actionItemNotifications = [:]
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        use(TimeCategory) {
            Date today = (new Date())
            today.set(minute: 0, second: 0)

            actionItemList.each { actionItem ->
                Date dueDate = actionItem.dueDate
                def delta = (dueDate - today)
                if (delta.days <= 6) {

                    Set<User> users = actionItem.assignedToUserList
                    users << User.findByUsername(actionItem.createdBy) //adding owner
                    users.findAll{it}?.each { user ->
                        if (getUserHoursInHisLocale(user) == grailsApplication.config.pvreports.sendNotificationHour) {
                            Map userDays = getUserDaysInHisLocale(user)
                            String actionDueDay = sdf.format(actionItem.dueDate)
                            ActionItemNotification actionItemNotification = actionItemNotifications[user.id]
                            if (!actionItemNotification) {
                                actionItemNotification = new ActionItemNotification()
                                actionItemNotification.user = user
                                actionItemNotifications << [(user.id): actionItemNotification]
                            }

                            if (actionDueDay == userDays.day5)
                                actionItemNotification.fiveDay << actionItem
                            else if (actionDueDay == userDays.day3)
                                actionItemNotification.threeDay << actionItem
                            else if (actionDueDay == userDays.yesterday)
                                actionItemNotification.oneDay << actionItem
                            else if (actionDueDay == userDays.today)
                                actionItemNotification.today << actionItem
                            else if ((actionDueDay != userDays.day6) && (actionDueDay != userDays.day4) && (actionDueDay != userDays.day2))
                                actionItemNotification.overdue << actionItem
                        }
                    }
                }
            }
        }
        return actionItemNotifications.collect { key, value -> value }
    }

    List getAllValidEmailIds(def recipients) {
        List validRecipients = []
        Pattern pattern = ~/^[a-zA-Z0-9.!#$%&’*+\/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/
        recipients.each {
            if (it && pattern.matcher(it).matches()) {
                validRecipients.add(it)
            }
        }
        log.debug("Sending email to recipients with valid email id ${validRecipients} out of ${recipients}")
        return validRecipients
    }

    String getValidEmailId(def recipient) {
        String validRecipient
        Pattern pattern = ~/^[a-zA-Z0-9.!#$%&’*+\/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/
        if(recipient && pattern.matcher(recipient).matches()){
            validRecipient = recipient
        }
        log.debug("Sending email to recipients with valid email id ${validRecipient}")
        return validRecipient
    }

    Boolean sendMailCheck(def configuration) {
        if (configuration instanceof ExecutedCaseSeries) return true;
        if (configuration instanceof Capa8D) return true;
        EmailConfiguration emailConfiguration = configuration.emailConfiguration
        Boolean useEmailConfiguration = emailConfiguration && !emailConfiguration.isDeleted
        Boolean sendMail = (useEmailConfiguration && (!emailConfiguration?.noEmailOnNoData || (emailConfiguration?.noEmailOnNoData && (configuration.containsData || configuration.hasGeneratedCasesData))))
        (sendMail || !useEmailConfiguration)
    }

    String insertValues(String str, def entity) {
        def reg = /\[.*?\]/
        def findTag = (str =~ /$reg/)
        def r = str;
        findTag.each {
            r = r.replace(it, mapValues(it.substring(1, it.size() - 1), entity))
        }
        r
    }
    /**
     * Method to replace placeholders  with values from entity
     * @return : Map with two values [text: <text with replaced placeholders>, attachments: <list of chart images to be incerted in email - can be blank>]
     * @input : str - email body with placeholdes, entity - ExecutedReportConfiguration or ExecutedCaseSeries
     */
    Map insertEmailBodyValues(String str, def entity, params) {
        List attachments = []
        def r = str;
        def reg = /\[.*?\]/
        def findTag = (str =~ /$reg/)
        findTag.eachWithIndex { it, i ->
            String tag = it.substring(1, it.size() - 1)
            if (tag.startsWith("sectionOutput")||tag.startsWith("sectionTable")||tag.startsWith("sectionChart")) {
                int index = -1
                boolean renderChart = (tag.startsWith("sectionOutput") || tag.startsWith("sectionChart"))
                boolean renderTable = (tag.startsWith("sectionOutput") || tag.startsWith("sectionTable"))
                try {
                    int length = tag.startsWith("sectionOutput")?13:12;
                    index = Integer.parseInt(tag.substring(length)) - 1
                } catch (Exception e) {/*bad grammer, doing nothing*/
                }
                if (((entity instanceof ExecutedConfiguration) || (entity instanceof ExecutedPeriodicReportConfiguration)) && (index > -1) && (entity.executedTemplateQueries.size() > index)) {
                    ExecutedTemplateQuery executedTemplateQuery = entity.executedTemplateQueries[index]
                    String embedReport = ""
                    if (checkIfReportExceedsHtmlLimit(executedTemplateQuery)) {
                        embedReport = ViewHelper.getMessage("app.embed.email.message")
                        Map rptParams = [sectionsToExport: [executedTemplateQuery.id], outputFormat: ReportFormatEnum.PDF.name()] << params
                        File pdfReport = dynamicReportService.createMultiTemplateReport(entity, rptParams)
                        byte[] fileData = pdfReport.getBytes()
                        attachments << [type: dynamicReportService.getContentType(ReportFormatEnum.PDF),
                                        name: "embed_${i}.pdf", size: fileData?.size(),
                                        data: fileData]
                    } else {
                        if (entity instanceof ExecutedPeriodicReportConfiguration && entity.status == ReportExecutionStatusEnum.GENERATED_CASES) {
                            embedReport = ViewHelper.getMessage("app.embed.email.notGenerated")
                        } else {
                            Map<String, String> sectionData = publisherService.getQueryTemplateData(executedTemplateQuery)
                            String imgHtml = ""
                            String tableHtml = ""
                            if (renderChart && sectionData.chartFile) {
                                imgHtml = """<div ><img width="684" src="cid:cid${index}"></div><br>"""
                                attachments << [contentId: "cid" + index, path: sectionData.chartFile, size: sectionData.chartFileSize]
                            }
                            if(renderTable) tableHtml=sectionData.table
                            embedReport = imgHtml + tableHtml
                        }
                    }
                    r = r.replace(it, embedReport)
                } else {
                    r = r.replace(it, "[${tag} - incorrect parameter]")
                }

            } else {
                r = r.replace(it, mapValues(tag, entity))
            }
        }
        [text: r, attachments: attachments]
    }

    private String mapValues(String tag, def entity) {
        if (tag == "version") return entity instanceof ExecutedCaseSeries ? getValueForPropertieName(entity, "numExecutions"): getValueForPropertieName(entity, "numOfExecutions")
        if (tag == "productSelection") return getValueForPropertieName(entity, "productSelection") ? ViewHelper.getDictionaryValues(entity, DictionaryTypeEnum.PRODUCT) : ViewHelper.getMessage("app.label.none")
        if (tag == "studySelection") return getValueForPropertieName(entity, "studySelection") ? ViewHelper.getDictionaryValues(entity, DictionaryTypeEnum.STUDY) : ViewHelper.getMessage("app.label.none")
        if (tag == "eventSelection") return getValueForPropertieName(entity, "eventSelection") ? ViewHelper.getDictionaryValues(entity, DictionaryTypeEnum.EVENT) : ViewHelper.getMessage("app.label.none")
        if (tag == "dateRangeType") return getValueForPropertieName(entity, "dateRangeType") ? ViewHelper.getMessage(getValueForPropertieName(entity, "dateRangeType").i18nKey) : ViewHelper.getMessage("app.label.none")
        if (tag == "executedGlobalQuery") return getValueForPropertieName(entity, "executedGlobalQuery") ? entity.executedGlobalQuery?.name : ViewHelper.getMessage("app.label.none")
        if (tag == "status") return getValueForPropertieName(entity, "status") ? ViewHelper.getMessage(entity?.status?.getI18nValueForAggregateReportStatus()) : ViewHelper.getMessage("app.label.none")
        if (tag == "workflowState") {
            def wf = getValueForPropertieName(entity, "workflowState")
            return wf ? wf.name : ViewHelper.getMessage("app.label.none")
        }
        if ((tag == "globalDateRang") || (tag == "globalDateRange")) {
            def gdr = getValueForPropertieName(entity, "executedGlobalDateRangeInformation")
            if (gdr) {
                return configurationService.getDateRangeValue(gdr, userService.user?.preference?.locale)
            } else return "";
        }
        if (tag == "evaluateDateAs") {
            def ed = getValueForPropertieName(entity, "evaluateDateAs")
            if (ed) {
                return ViewHelper.getMessage(ed.getI18nKey()) +
                        (ed == EvaluateCaseDateEnum.VERSION_ASOF ? (ed.asOfVersionDate?.format(DateUtil.getShortDateFormatForLocale(userService.user?.preference?.locale))) : "");
            } else return ViewHelper.getMessage("app.label.none"); ;
        }
        if (tag == "dateRange") {
            def dri = getValueForPropertieName(entity, "executedCaseSeriesDateRangeInformation")
            if (dri && dri.dateRangeEnum) {
                String result = ViewHelper.getMessage((dri.dateRangeEnum?.i18nKey))
                if ((DateRangeEnum.getRelativeDateOperatorsWithX().contains(dri.dateRangeEnum))) {
                    result += ", X= " + dri.relativeDateRangeValue
                }
                if (dri.dateRangeStartAbsolute) {
                    result += " " + ViewHelper.getMessage("app.label.start") + " " + dri.dateRangeStartAbsolute?.format(DateUtil.getShortDateFormatForLocale(userService.user?.preference?.locale)) + " " +
                            ViewHelper.getMessage("app.label.end") + " " + dri.dateRangeEndAbsolute?.format(DateUtil.getShortDateFormatForLocale(userService.user?.preference?.locale))
                }
                return result
            } else return ViewHelper.getMessage("app.label.none")
        }
        if (tag == "sections") {
            def templatesQueries = getValueForPropertieName(entity, "executedTemplateQueriesForProcessing")
            if (templatesQueries) {
                String result = ViewHelper.getMessage("app.label.reportSections") + ":"
                templatesQueries.each {
                    result += "\n" + ViewHelper.getMessage("app.label.template") + " : " + it.executedTemplate?.name + ", "
                    result += ViewHelper.getMessage("app.label.query") + " : " + (it.executedQuery?.name ?: ViewHelper.getMessage("app.label.none")) + ", "
                    result += ViewHelper.getMessage("app.label.parameters") + " : "
                    if (it.executedTemplateValueLists) {
                        result += ViewHelper.getMessage("app.label.template") + " ( "
                        it.executedTemplateValueLists.each { etv ->
                            result += etv.template.name + " : "
                            etv.parameterValues.each { pv ->
                                result += pv.key + " = " + pv.value
                            }
                        }
                        result += " ) "
                    }

                    if (it.executedQueryValueLists) {
                        result += ViewHelper.getMessage("app.label.query") + " ( "
                        it.executedQueryValueLists.each { eqv ->
                            result += eqv.query.name + " : "
                            eqv.parameterValues.each { pv ->
                                if (pv.hasProperty('reportField')) {
                                    result += ViewHelper.getMessage("app.reportField.${pv.reportField.name}") + " "
                                    result += ViewHelper.getMessage(pv.operator.getI18nKey()) + " "
                                    result += pv.value
                                } else {
                                    result += pv.key + " = " + pv.value
                                }
                            }
                        }
                        result += " ) "
                    }
                    if (!it.executedQueryValueLists && !it.executedTemplateValueLists) result += ViewHelper.getMessage("app.label.none")
                    result += ViewHelper.getMessage("app.label.DateRange") + " : " + configurationService.getDateRangeValueForCriteria(it, userService.currentUser?.preference?.locale) + ", "
                    result += ViewHelper.getMessage("app.label.EvaluateCaseDateOn") + " : " + ((Date) it.executedDateRangeInformationForTemplateQuery.executedAsOfVersionDate).format(DateUtil.getShortDateFormatForLocale(userService.user?.preference?.locale)) + ", "
                    result += ViewHelper.getMessage("app.label.queryLevel") + " : " + ViewHelper.getMessage(it.queryLevel.i18nKey)
                }
                return result
            }
        }
        if (tag == "owner") return entity.owner?.fullNameAndUserName
        if (tag == "referReport") {
            String url
            if (grailsApplication.config.grails.appBaseURL) {
                if (entity instanceof ExecutedCaseSeries)
                    url = grailsLinkGenerator.link(controller: "caseList", action: "index", base: grailsApplication.config.grails.appBaseURL, absolute: true) + "?cid=" + entity.id
                else
                    url = grailsLinkGenerator.link(controller: "report", action: "showFirstSection", id: entity.id, base: grailsApplication.config.grails.appBaseURL, absolute: true)
            }
            return "<a href=" + url + "> " + ViewHelper.getMessage("app.label.report.link") + "</a>"
        }
        if (tag == "etlStatus") {
            String etlStatus = etlJobService?.getEtlStatus()?.getStatus()
            return etlStatus
        }
        if(tag == "startTime") return entity instanceof EtlSchedule ? getValueForPropertieName(entity, "startDateTime"): getValueForPropertieName(entity, "startDateTime")
        return getValueLabelForPropertieName(entity, tag)
    }

    private def getValueForPropertieName(entity, String propertyName) {
        entity?.properties?.find { it.key == propertyName } ? entity[propertyName] : [propertyName]
    }

    private def getValueLabelForPropertieName(def entity, String propertyName) {
        def val = getValueForPropertieName(entity, propertyName)
        if (val == null || val == "") return ViewHelper.getMessage("app.label.none");
        if (val instanceof Boolean)
            return val ? ViewHelper.getMessage("default.button.yes.label") : ViewHelper.getMessage("default.button.no.label")
        if (val instanceof Date) {
            if (val) {
                return ViewHelper.formatRunDateAndTime(entity)
            } else return "";
        }
        val.toString()
    }

    static class ActionItemNotification {
        User user
        Set<ActionItem> overdue = []
        Set<ActionItem> today = []
        Set<ActionItem> oneDay = []
        Set<ActionItem> threeDay = []
        Set<ActionItem> fiveDay = []
        public boolean isNotEmpty(){
            return (overdue || today || oneDay || threeDay || fiveDay)
        }
    }

    private static Map getUserDaysInHisLocale(User user) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        sdf.setTimeZone(TimeZone.getTimeZone(user.preference.timeZone))
        Map result = [:]
        Date today = new Date()
        use(TimeCategory) {
            result.today = sdf.format(today)
            result.yesterday = sdf.format(today + 1.day)
            result.day2 = sdf.format(today + 2.day)
            result.day3 = sdf.format(today + 3.day)
            result.day4 = sdf.format(today + 4.day)
            result.day5 = sdf.format(today + 5.day)
            result.day6 = sdf.format(today + 6.day)

        }
        return result
    }

    static String getUserHoursInHisLocale(User user) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH")
        sdf.setTimeZone(TimeZone.getTimeZone(user.preference.timeZone))
        return sdf.format(new Date())
    }

    void sendEmailWithFiles(def recipients, def emailCc, String emailSubject, String emailBodyMessage, boolean asyVal, List files){
        //Check if email is not null & valid.
        recipients = recipients.findAll { it }
        recipients = getAllValidEmailIds(recipients)
        if (recipients){
            log.info("Sending email with subject")
            if(Holders.config.getProperty('reports.email.subject.prepend')){
                emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
            }
            String emailBody = groovyPageRenderer.render(template: '/mail/plain', model: ['emailBodyMessage': emailBodyMessage])

            sendMail {
                multipart true
                to recipients
                if (emailCc) {
                    cc emailCc
                }
                // if this remains async then there is no delivery exceptions we can report on... tho its much faster
                async asyVal
                subject emailSubject
                html emailBody
                if(files){
                    files.each { file ->
                        log.info("Sending file ${file.name} of type ${file.type} to Email.")
                        attachBytes file.name, file.type, file.data
                    }
                }
                inline 'pvreportsMailBackground', 'image/jpg', resourceLoader.getResource("/images/background.jpg")?.getFile()
                inline 'pvreportslogo', 'image/jpg', resourceLoader.getResource("/images/pv_reports_logo.png")?.getFile()
            }
        }else{
            log.debug("Not emailing report, recipient list [${recipients}] is empty")
        }
    }

    void emailIcsrCaseTo(ExecutedTemplateQuery executedTemplateQuery, String caseNumber, Integer versionNumber, List<String> recipients, List<ReportFormatEnum> outputs) {
        Locale locale = userService.getCurrentUser()?.preference?.locale
        recipients = recipients.findAll { it }
        recipients = getAllValidEmailIds(recipients)
        String emailSubject = ViewHelper.getMessage("icsr.case.email.subject.label", "${caseNumber}-${versionNumber}")
        String emailBody = ViewHelper.getMessage("app.label.hi") + "<br><br>"
        emailBody += ViewHelper.getMessage("icsr.case.email.body.label")
        emailBody += "<br><br>"
        if (locale?.language != 'ja') {
            emailBody += ViewHelper.getMessage("app.label.thanks") + "," + "<br>"
        }
        emailBody += ViewHelper.getMessage("app.label.pv.reports")
        emailBody = "<span style=\"font-size: 14px;\">" + emailBody + "</span>"
        Map params = [caseNumber: caseNumber, versionNumber: versionNumber, exIcsrTemplateQueryId: executedTemplateQuery.id]
        List files = []
        IcsrCaseTracking icsrCaseTracking = null
        IcsrCaseTracking.withNewSession {
            icsrCaseTracking = icsrProfileAckService.getIcsrTrackingRecord(executedTemplateQuery.getId(), caseNumber, versionNumber)
        }
        Long processedReportId = icsrCaseTracking?.processedReportId
        String prodHashCode = icsrCaseTracking?.prodHashCode
        Date fileDate = icsrCaseTracking?.generationDate
        Boolean isJapanProfile = icsrCaseTracking?.isJapanProfile()
        if (executedTemplateQuery.usedTemplate?.isCiomsI()) {
            File file = dynamicReportService.createCIOMSReport(caseNumber, versionNumber, userService.currentUser, fileDate, isJapanProfile, executedTemplateQuery, processedReportId)
            files.add([type: dynamicReportService.getContentType(ReportFormatEnum.PDF),
                       name: file.name,
                       data: file.getBytes()
            ])
        } else if (executedTemplateQuery.usedTemplate?.isMedWatchTemplate()) {
            File file = dynamicReportService.createMedWatchReport(caseNumber, versionNumber, userService.currentUser, fileDate, isJapanProfile, executedTemplateQuery, processedReportId, prodHashCode)
            files.add([type: dynamicReportService.getContentType(ReportFormatEnum.PDF),
                       name: file.name,
                       data: file.getBytes()
            ])
        } else {
            outputs.each {
                File file
                switch (it) {
                    case ReportFormatEnum.PDF:
                        file = dynamicReportService.createPDFReport(executedTemplateQuery, false, params, fileDate, isJapanProfile)
                        break
                    case ReportFormatEnum.R3XML:
                        file = dynamicReportService.createR3XMLReport(executedTemplateQuery, false, params, fileDate, isJapanProfile)
                        break
                    default:
                        file = dynamicReportService.createXMLReport(executedTemplateQuery, false, params, fileDate, isJapanProfile)
                }
                files.add([type: dynamicReportService.getContentType(it),
                           name: file.name,
                           data: file.getBytes()
                ])
            }
        }
        sendEmailWithFiles(recipients, null, emailSubject, emailBody, true, files)
    }

    String emailExceptionToMessage(Exception e) {
        Boolean isOtherException = true
        String error = ""
        Map mailException = [
                'GrailsMailException'          : ViewHelper.getMessage('app.report.mail.configuration.exception'),
                'FileNotFoundException'        : ViewHelper.getMessage('app.report.file.not.found'),
                'MaxPagesGovernorException'    : ViewHelper.getMessage('app.report.maxJasperDataBytes.share'),
                'TimeoutGovernorException'     : ViewHelper.getMessage('app.report.mail.timeout.exception'),
                'JRException'                  : ViewHelper.getMessage('app.report.mail.jre.exception'),
                'AuthenticationFailedException': ViewHelper.getMessage('app.report.mail.auth.exception')
        ]

        for (val in mailException) {
            if (e.getClass().toString().endsWith(val.getKey())) {
                error = val.getValue()
                isOtherException = false
            }
        }

        if (isOtherException) {
            error = ViewHelper.getMessage('app.report.mail.default')
        }
        return error
    }

    void sendPasswordChangeEmail(String username, String newPassword, def recipients) {
        try {
            Locale locale = userService.getCurrentUser()?.preference?.locale
            recipients = recipients.findAll { it }
            recipients = getAllValidEmailIds(recipients)
            String emailSubject = ViewHelper.getMessage("app.label.email.password")
            String emailBody = ViewHelper.getMessage("app.label.hi") + "<br><br>"
            emailBody += ViewHelper.getMessage("app.label.email.defaulBody" , username , newPassword)
            emailBody += "<br><br>"
            if (locale?.language != 'ja') {
                emailBody += ViewHelper.getMessage("app.label.thanks") + "," + "<br>"
            }
            emailBody += ViewHelper.getMessage("app.label.pv.reports")
            sendEmail(recipients , emailBody , true , emailSubject)
        } catch (Throwable t) {
            log.error('Error occurred when sending emails', t)
        }
    }

    boolean checkIfReportExceedsHtmlLimit(ExecutedTemplateQuery executedTemplateQuery) {
        Long topXRows = dynamicReportService.topXRowsInReport(executedTemplateQuery.executedTemplate) ?: executedTemplateQuery.getReportResult()?.reportRows
        return topXRows > grailsApplication.config.pvreports.show.max.html
    }

    def fetchLastRunDate(jobName) {
        def job = JobRunTrackerPVCPVQ.findByJobName(jobName)
        return job?.lastRunDate ?: new Date(0)
    }

    def updateLastRunDate(jobName, currentRun) {
        def job = JobRunTrackerPVCPVQ.findByJobName(jobName)
        if (!job) {
            job = new JobRunTrackerPVCPVQ(jobName: jobName)
        }
        job.lastRunDate = currentRun
        job.lastUpdated = new Date()  // Correct mapping
        job.save(flush: true, failOnError: true)
    }


    def sendPVCEmailNotifications() {
        def rawConnection = utilService.getReportConnectionForPVR()
        def sql = new Sql(rawConnection)
        try {
            def combinedResults = fetchCaseNumbersAndTransitions(sql)
            processWorkflowNotifications(combinedResults.workflow)
            processAssignedCaseNotifications(combinedResults.assigned)

        } catch (Exception e) {
            log.error("Error during PVC Email Notifications", e)
        } finally {
            sql.close()
        }
    }

    def processWorkflowNotifications(Map userWorkflowMap) {
       // Map<User, Map<String, List<Map>>> userWorkflowStateChangesMap = [:]
        Map<User, List<Map>> userWorkflowChangesMap = [:]
        Map<UserGroup, List<Map>> groupWorkflowChangesMap = [:]

        userWorkflowMap.each { userId, userWorkflowInfo ->
            User user = User.findById(userId)
            UserGroup userGroup = UserGroup.findById(userId)

            // Enrich case info list with additional details
            def enrichedCaseInfoList = userWorkflowInfo?.collect { caseInfo ->
                def drilldownReport = ReportResult.read(caseInfo?.reportResultId)?.drillDownSource?.executedConfiguration
                return caseInfo
            }

            if (userGroup) {
                if (!groupWorkflowChangesMap.containsKey(userGroup)) {
                    groupWorkflowChangesMap[userGroup] = []
                }
                enrichedCaseInfoList.each { caseInfo ->
                    caseInfo.userGroupName = userGroup.name
                }
                groupWorkflowChangesMap[userGroup] += enrichedCaseInfoList
            } else if (user) {
                if (!userWorkflowChangesMap.containsKey(user)) {
                    userWorkflowChangesMap[user] = []
                }
                enrichedCaseInfoList.each { caseInfo ->
                    caseInfo.userGroupName = null
                }
                userWorkflowChangesMap[user] += enrichedCaseInfoList
            }


        }

        sendWorkflowChangeEmailToUser(userWorkflowChangesMap)
        // Send Consolidated Workflow Emails to Groups
        sendConsolidatedWorkflowEmailForGroups(groupWorkflowChangesMap)
    }

    def processAssignedCaseNotifications(Map userAssignedMap) {
        Map<User, List<Map>> userCaseInfoMap = [:]
        Map<UserGroup, List<Map>> groupCaseInfoMap = [:]

        userAssignedMap.each { userId, caseInfoList ->
            User user = User.findById(userId)
            UserGroup userGroup = UserGroup.findById(userId)

            def enrichedCaseInfoList = caseInfoList.collect { caseInfo ->
                def drilldownReport = ReportResult.read(caseInfo?.reportResultId)?.drillDownSource?.executedConfiguration
                return caseInfo
            }

            if (userGroup) {
                if (!groupCaseInfoMap.containsKey(userGroup)) {
                    groupCaseInfoMap[userGroup] = []
                }
                enrichedCaseInfoList.each { caseInfo ->
                    caseInfo.userGroupName = userGroup.name
                }
                groupCaseInfoMap[userGroup] += enrichedCaseInfoList
            } else if (user) {
                if (!userCaseInfoMap.containsKey(user)) {
                    userCaseInfoMap[user] = []
                }
                enrichedCaseInfoList.each { caseInfo ->
                    caseInfo.userGroupName = null
                }
                userCaseInfoMap[user] += enrichedCaseInfoList
            }
        }

        // Send emails to individual users
        sendPVCEmailsToUsers(userCaseInfoMap)

        // Consolidate and send single email for groups
        sendConsolidatedPVCEmailForGroups(groupCaseInfoMap)
    }

// Email to individual users
    private void sendPVCEmailsToUsers(Map<User, List<Map>> userCaseInfoMap) {
        userCaseInfoMap.each { user, caseInfoList ->
            def allRecordIds = caseInfoList*.drilldownRecordId
            def cllRecordIds = allRecordIds.take(500)
            def hasLargeIds = allRecordIds.size() > 500
            def reportResultId = caseInfoList?.getAt(0)?.reportResultId
            String linkFilterParam = cllRecordIds ? URLEncoder.encode(cllRecordIds.toString(), 'UTF-8') : ''
            String content = groovyPageRenderer.render(
                    template: '/mail/pvcentral/pvcNotification',
                    model: ['userCaseInfo': caseInfoList,
                            'timeframe':new Date().format(DATE_FMT),
                            'linkFilter':linkFilterParam,
                            'reportResultId':reportResultId,
                            'hasLargeIds':hasLargeIds
                    ]
            )

            String recipientsEmail = reasonOfDelayService.getPVCRecipientsByEmailPreference(user, Constants.ASSIGNED_TO)
            recipientsEmail = getValidEmailId(recipientsEmail)
            String emailSubject = ViewHelper.getMessage("app.notification.reasonOfDelay.user.subject")

            // Prepend if configured
            if (Holders.config.getProperty('reports.email.subject.prepend')) {
                emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
            }

            sendPVCEmail(content, recipientsEmail, emailSubject)
        }
    }

// Consolidate and send email for user groups
    private void sendConsolidatedPVCEmailForGroups(Map<UserGroup, List<Map>> groupCaseInfoMap) {
        Map<User, List<Map>> consolidatedCaseMap = [:]
        groupCaseInfoMap.each { userGroup, caseInfoList ->
            userGroup.users.each { user ->
                if (!consolidatedCaseMap.containsKey(user)) {
                    consolidatedCaseMap[user] = []
                }
                consolidatedCaseMap[user] += caseInfoList
            }
        }

        consolidatedCaseMap.each { user, groupedCases ->
            def allRecordIds = groupedCases*.drilldownRecordId
            def cllRecordIds = allRecordIds.take(500)
            def hasLargeIds = allRecordIds.size() > 500
            def reportResultId = groupedCases?.getAt(0)?.reportResultId
            String linkFilterParam = cllRecordIds ? URLEncoder.encode(cllRecordIds.toString(), 'UTF-8') : ''
            String content = groovyPageRenderer.render(
                    template: '/mail/pvcentral/pvcNotification',
                    model: ['groupedCases': groupedCases,
                            'timeframe':new Date().format(DATE_FMT),
                            'linkFilter':linkFilterParam,
                            'reportResultId':reportResultId,
                            'hasLargeIds':hasLargeIds
                    ]
            )

            String recipientsEmail = reasonOfDelayService.getPVCRecipientsByEmailPreference(user, Constants.ASSIGNED_TO_GROUP)
            recipientsEmail = getValidEmailId(recipientsEmail)
            String emailSubject = ViewHelper.getMessage("app.notification.reasonOfDelay.group.subject")

            // Prepend if configured
            if (Holders.config.getProperty('reports.email.subject.prepend')) {
                emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
            }

            sendPVCEmail(content, recipientsEmail, emailSubject)
        }
    }


    private void sendWorkflowChangeEmailToUser(Map<User, List<Map>> userCaseInfoMap) {
        userCaseInfoMap.each { user, caseInfoList ->
            def allRecordIds = caseInfoList*.drilldownRecordId
            def cllRecordIds = allRecordIds.take(500)
            def hasLargeIds = allRecordIds.size() > 500
            def reportResultId = caseInfoList?.getAt(0)?.reportResultId
            def linkFilterParam = cllRecordIds ? URLEncoder.encode(cllRecordIds.toString(), 'UTF-8') : ''
            String content = groovyPageRenderer.render(
                    template: '/mail/pvcentral/pvcWorkflow',
                    model: [
                            'workflowStateChanges': caseInfoList,
                            'timeframe'           : new Date().format(DATE_FMT),
                            'linkFilter':linkFilterParam,
                            'reportResultId':reportResultId,
                            'hasLargeIds':hasLargeIds
                    ]
            )

            String recipientsEmail = reasonOfDelayService.getPVCRecipientsByEmailPreference(user, Constants.WORKFLOW_CHANGES)
            recipientsEmail = getValidEmailId(recipientsEmail)
            String emailSubject = ViewHelper.getMessage("app.notification.reasonOfDelay.workflow.subject")

            // Prepend if configured
            if (Holders.config.getProperty('reports.email.subject.prepend')) {
                emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
            }
            sendPVCEmail(content, recipientsEmail, emailSubject)
        }
    }

    private void sendConsolidatedWorkflowEmailForGroups(Map<UserGroup, List<Map>> groupWorkflowChangesMap) {
        Map<User, List<Map>> consolidatedCaseMap = [:]

        // Consolidate cases for users across groups
        groupWorkflowChangesMap.each { userGroup, caseInfoList ->
            userGroup.users.each { user ->
                if (!consolidatedCaseMap.containsKey(user)) {
                    consolidatedCaseMap[user] = []
                }
                consolidatedCaseMap[user] += caseInfoList
            }
        }

        // Send a single email per user with cases from all groups
        consolidatedCaseMap.each { user, groupedCases ->
            def allRecordIds = groupedCases*.drilldownRecordId
            def cllRecordIds = allRecordIds.take(500)
            def hasLargeIds = allRecordIds.size() > 500
            def reportResultId = groupedCases?.getAt(0)?.reportResultId
            def linkFilterParam = cllRecordIds ? URLEncoder.encode(cllRecordIds.toString(), 'UTF-8') : ''

            String content = groovyPageRenderer.render(template: '/mail/pvcentral/pvcWorkflow',
                    model: ['groupedWorkflowStateChanges': groupedCases,
                            'timeframe':new Date().format(DATE_FMT),
                            'linkFilter':linkFilterParam,
                            'reportResultId':reportResultId,
                            'hasLargeIds':hasLargeIds
                    ]
            )

            String recipientsEmail = reasonOfDelayService.getPVCRecipientsByEmailPreference(user, Constants.WORKFLOW_CHANGES)
            recipientsEmail = getValidEmailId(recipientsEmail)
            String emailSubject = ViewHelper.getMessage("app.notification.reasonOfDelay.workflow.subject")

            // Prepend if configured
            if (Holders.config.getProperty('reports.email.subject.prepend')) {
                emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
            }
            sendPVCEmail(content, recipientsEmail, emailSubject)
        }
    }

// Utility method to send PVC emails
    private void sendPVCEmail(String content, String recipientsEmail, String emailSubject) {
        if (recipientsEmail?.trim()) {
            log.info("Sending PVC Notification email to ${recipientsEmail}")
            sendMail {
                multipart true
                async true
                to recipientsEmail
                subject emailSubject
                html(content)
                inline 'pvreportsMailBackground', 'image/jpg', resourceLoader.getResource("/images/background.jpg")?.getFile()
                inline 'pvreportslogo', 'image/jpg', resourceLoader.getResource("/images/pv_reports_logo.png")?.getFile()
            }
        }
    }




    def fetchCaseNumbersAndTransitions(Sql sql) {
        def jobName = 'PVCEmailNotificationJob'
        def lastRunDate = fetchLastRunDate(jobName)
        def currentRun = new Date()

        def sqlQuery = ''' SELECT 
            'WORKFLOW' AS type,
            WS_FROM.NAME AS fromStateName,
            WS_TO.NAME AS toStateName,
            DD.CASE_NUM AS masterCaseNum,
            DD.REPORT_RESULT_ID AS reportResultId,
            DD.ID AS drilldownRecordId,
            DM.ASSIGNER AS assigner,
            COALESCE(JJ.ASSIGNED_TO_USER_ID, JJ.ASSIGNED_TO_USERGROUP_ID) AS assignedEntity,
            JJ.LAST_UPDATED AS updatedDate
        FROM WORKFLOW_JUSTIFICATION JJ
        LEFT JOIN WORKFLOW_STATE WS_FROM ON JJ.FROM_STATE_ID = WS_FROM.ID
        LEFT JOIN WORKFLOW_STATE WS_TO ON JJ.TO_STATE_ID = WS_TO.ID
        LEFT JOIN DRILLDOWN_METADATA DM ON JJ.DRILLDOWN_METADATA = DM.ID
        LEFT JOIN DRILLDOWN_DATA DD ON DM.CASE_ID = DD.CASE_ID
            AND DM.TENANT_ID = DD.TENANT_ID
            AND DD.PROCESSED_REPORT_ID = DM.PROCESSED_REPORT_ID
        WHERE 
            JJ.LAST_UPDATED >= CAST(? AS TIMESTAMP)
            AND JJ.LAST_UPDATED < CAST(? AS TIMESTAMP)
            AND (JJ.ASSIGNED_TO_USER_ID IS NOT NULL OR JJ.ASSIGNED_TO_USERGROUP_ID IS NOT NULL)
            AND WS_FROM.NAME != WS_TO.NAME
            AND DD.REPORT_RESULT_ID = (
                SELECT MAX(DD2.REPORT_RESULT_ID)
                FROM DRILLDOWN_DATA DD2
                WHERE DD2.CASE_NUM = DD.CASE_NUM
            )

        UNION ALL

        SELECT 
            'ASSIGNED' AS type,
            NULL AS fromStateName,
            NULL AS toStateName,
            DD.CASE_NUM AS masterCaseNum,
            DD.REPORT_RESULT_ID AS reportResultId,
            DD.ID AS drilldownRecordId,
            DM.ASSIGNER AS assigner,
            COALESCE(DM.ASSIGNED_TO_USER, DM.ASSIGNED_TO_USERGROUP) AS assignedEntity,
            DM.ASSIGNEE_UPDATED_DATE AS updatedDate
        FROM DRILLDOWN_DATA DD
        LEFT JOIN DRILLDOWN_METADATA DM 
            ON DD.CASE_ID = DM.CASE_ID
            AND DD.TENANT_ID = DM.TENANT_ID
            AND DD.PROCESSED_REPORT_ID = DM.PROCESSED_REPORT_ID
        WHERE 
            DM.ASSIGNEE_UPDATED_DATE >= CAST(? AS TIMESTAMP)
            AND DM.ASSIGNEE_UPDATED_DATE < CAST(? AS TIMESTAMP)
            AND (DM.ASSIGNED_TO_USER IS NOT NULL OR DM.ASSIGNED_TO_USERGROUP IS NOT NULL)
            AND DD.REPORT_RESULT_ID = (
                SELECT MAX(DD3.REPORT_RESULT_ID)
                FROM DRILLDOWN_DATA DD3
                WHERE DD3.CASE_NUM = DD.CASE_NUM
            )
        ORDER BY 
            masterCaseNum, updatedDate DESC
    '''

        def userWorkflowMap = [:]
        def userAssignedMap = [:]

        try {
            def results = sql.rows(sqlQuery, [new Timestamp(lastRunDate.time), new Timestamp(currentRun.time), new Timestamp(lastRunDate.time), new Timestamp(currentRun.time)])

            def convertTimestamp = { timestamp ->
                if (timestamp instanceof TIMESTAMP) {
                    return timestamp.timestampValue()
                }
                return timestamp
            }

            def workflowRecords = results.findAll { it['type'] == 'WORKFLOW' }
            def assignedRecords = results.findAll { it['type'] == 'ASSIGNED' }

            // Group WORKFLOW records by caseNum, reportResultId, and drilldownRecordId
            def groupedWorkflowResults = workflowRecords.groupBy { record ->
                [
                        caseNum          : record['masterCaseNum'],
                        reportResultId   : record['reportResultId'],
                        drilldownRecordId: record['drilldownRecordId']
                ]
            }

             def filteredWorkflowResults = groupedWorkflowResults.collect { caseInfo, records ->
                records.max { convertTimestamp(it['updatedDate']) }
            }

            filteredWorkflowResults.each { record ->
                def assignedEntity = record['assignedEntity']
                def assigner = record['assigner']
                def caseInfo = [
                        caseNum          : record['masterCaseNum'],
                        reportResultId   : record['reportResultId'],
                        drilldownRecordId: record['drilldownRecordId'],
                        fromState        : record['fromStateName'],
                        toState          : record['toStateName']
                ]

                if (assignedEntity != assigner) {
                    userWorkflowMap.computeIfAbsent(assignedEntity) { [] }.add(caseInfo)
                }
            }

             assignedRecords.each { record ->
                def assignedEntity = record['assignedEntity']
                def assigner = record['assigner']
                def caseInfo = [
                        caseNum          : record['masterCaseNum'],
                        reportResultId   : record['reportResultId'],
                        drilldownRecordId: record['drilldownRecordId'],
                        fromState        : record['fromStateName'],
                        toState          : record['toStateName']
                ]

                if (assignedEntity != assigner) {
                    userAssignedMap.computeIfAbsent(assignedEntity) { [] }.add(caseInfo)
                }
            }

            updateLastRunDate(jobName, currentRun)
            return [workflow: userWorkflowMap, assigned: userAssignedMap]
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

}

