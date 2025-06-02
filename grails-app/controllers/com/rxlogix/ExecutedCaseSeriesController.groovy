package com.rxlogix

import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.co.SaveCaseSeriesFromSpotfireCO
import com.rxlogix.config.*
import com.rxlogix.customException.CustomJasperException
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class ExecutedCaseSeriesController implements SanitizePaginationAttributes {

    def CRUDService
    def userService
    def reportExecutorService
    def caseSeriesService
    def emailService
    def notificationService
    def reportService
    def utilService
    GrailsApplication grailsApplication

    static allowedMethods = [ delete: ['DELETE','POST']]

    @Secured(['ROLE_CASE_SERIES_VIEW'])
    def index() {
    }

    @Secured(['ROLE_CASE_SERIES_VIEW'])
    def show(ExecutedCaseSeries executedCaseSeries) {
        if (!executedCaseSeries) {
            notFound()
            return
        }

        if (!executedCaseSeries.isViewableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }

        Boolean viewBasicSql = params.getBoolean("viewBasicSql")
        Boolean viewAdvanceSql = params.getBoolean("viewAdvanceSql")
        notificationService.deleteNotificationByExecutionStatusId(userService.getCurrentUser(), executedCaseSeries.id, NotificationApp.CASESERIES)
        CaseSeries caseSeriesInstance = CaseSeries.findBySeriesNameAndOwner(executedCaseSeries.seriesName,executedCaseSeries.owner)
        render view: "show", model: [seriesInstance: executedCaseSeries, viewSql: (viewBasicSql || viewAdvanceSql) ? reportExecutorService.debugCaseSeriesSql(executedCaseSeries, viewBasicSql ?: viewAdvanceSql) : null,caseSeriesInstanceId:caseSeriesInstance?.id]
    }

    /**
     * This action is responsible to delete the case series.
     * @return
     */
    def delete(ExecutedCaseSeries caseSeriesInstance) {
        if (!caseSeriesInstance) {
            notFound()
            return
        }

        if (!caseSeriesInstance.isEditableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }

        def user = userService.getUser()

        if (params.boolean("deleteForAll") && (user.isAdmin() || caseSeriesInstance.owner == user)) {
            CRUDService.softDelete(caseSeriesInstance, caseSeriesInstance.seriesName, params.deleteJustification)
            AuditLogConfigUtil.logChanges(caseSeriesInstance, [deleted: "Deleted for All users (Justification: ${params.deleteJustification})"], [deleted: ""], Constants.AUDIT_LOG_DELETE)
        } else {
            userService.removeUserFromDeliveryOptionSharedWith(user, caseSeriesInstance.executedDeliveryOption, caseSeriesInstance.owner.id)
            CRUDService.softDeleteForUser(user, caseSeriesInstance, caseSeriesInstance.seriesName, params.deleteJustification)
        }
        flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'caseSeries.label', default: 'Case Series'), caseSeriesInstance.seriesName])}"
        redirect(action: "index")
    }

    def archive(ExecutedCaseSeries caseSeriesInstance) {
        if (!caseSeriesInstance) {
            notFound()
            return
        }
        def user = userService.getUser()
        ExecutedCaseSeriesUserState state = ExecutedCaseSeriesUserState.findByUserAndExecutedCaseSeries(user, caseSeriesInstance)
        if (!state) {
            state = new ExecutedCaseSeriesUserState(user: user, executedCaseSeries: caseSeriesInstance, isDeleted: false, isArchived: false)
        }
        state.isArchived = !state.isArchived
        state.save(flush: true)

        AuditLogConfigUtil.logChanges(caseSeriesInstance, [archived: "${state.isArchived ? 'Archived' : 'Not archived'} for user ${user.fullName}"], [archived: "${!state.isArchived ? 'Archived' : 'Not archived'} for user ${user.fullName}"], Constants.AUDIT_LOG_UPDATE)
        redirect(action: "index")
    }

    def favorite() {
        AjaxResponseDTO responseDTO = new AjaxResponseDTO()
        ExecutedCaseSeries caseSeries = params.id ? ExecutedCaseSeries.get(params.id) : null
        if (!caseSeries) {
            responseDTO.setFailureResponse(message(code: 'default.not.found.message', args: [message(code: 'app.label.case.series'), params.id]) as String)
        } else {
            try {
                caseSeriesService.setFavorite(caseSeries, params.boolean("state"))
            } catch (Exception e) {
                responseDTO.setFailureResponse(e, message(code: 'default.server.error.message') as String)
            }
        }
        render(responseDTO.toAjaxResponse())
    }

    def saveExecutedCaseSeries() {
        ExecutedCaseSeries executedCaseSeries;
        try {
            if (params.cid) {
                executedCaseSeries = ExecutedCaseSeries.get(params.long("cid"))
                if (executedCaseSeries.seriesName == params.seriesName) {
                    render(['success': false, 'message': "${message(code: 'com.rxlogix.config.executed.caseSeries.seriesName.unique.per.user')}"] as JSON)
                    return
                }
                //Added as it will be first time saved caseseries otherwise it would be 0 or some other number.
                executedCaseSeries.numExecutions = 1
                executedCaseSeries.seriesName = params.seriesName
                executedCaseSeries.isTemporary = false
                CRUDService.update(executedCaseSeries)
            } else {
                ExecutedTemplateQuery executedTemplateQuery = ExecutedTemplateQuery.get(params.long('executedTemplateQueryId'));
                executedCaseSeries = new ExecutedCaseSeries(executedTemplateQuery)
                executedCaseSeries.seriesName = params.seriesName
                executedCaseSeries.owner = userService.currentUser
                executedCaseSeries.locale = executedCaseSeries.owner.preference.locale
                ExecutedCaseDeliveryOption executedCaseDeliveryOption = new ExecutedCaseDeliveryOption(sharedWith:[executedCaseSeries.owner])
                executedCaseSeries.executedDeliveryOption = executedCaseDeliveryOption
                String delimiter = grailsApplication.config.caseSeries.bulk.addCase.delimiter
                Set<String> caseNumberWithVersion = []
                reportService.getCaseNumberAndVersions(executedTemplateQuery.reportResult).each {
                    String caseNumber = it.first
                    Integer versionNumber = it.second
                    if (caseNumber && versionNumber) { //Only save caseseries when both caseNumber and Version number exists.
                        caseNumberWithVersion.add(caseNumber + delimiter + versionNumber)
                    }
                }
                caseSeriesService.save(executedCaseSeries, caseNumberWithVersion)
            }
            String successMessage = message(code: 'default.created.message', args: [message(code: 'caseSeries.label'), executedCaseSeries.seriesName])
            render(AjaxResponseDTO.success().withSuccessAlert(successMessage).toJsonAjaxResponse())
        } catch (ValidationException e) {
            if (e.errors.getFieldError('seriesName').code == 'unique') {
                render(['success': false, 'message': "${message(code: 'com.rxlogix.config.executed.caseSeries.seriesName.unique.per.user')}"] as JSON)
            } else if (e.errors.getFieldError('seriesName').code == 'exist') {
                render(['success': false, 'message': "${message(code: 'com.rxlogix.config.executed.caseSeries.seriesName.exists')}"] as JSON)
            } else {
                render(['success': false, 'message': e.message] as JSON)
            }
        }

    }

    //TODO: This approach will be fixed in PVR 4.6, plan is to use token generated using sys user.
    @Secured('permitAll')
    def saveCaseSeriesForSpotfire(SaveCaseSeriesFromSpotfireCO co) {
        ResponseDTO responseDTO = new ResponseDTO()
        try {
            if (!co.validate()) {
                log.warn(co.errors.allErrors?.toString())
                responseDTO.setFailureResponse(message(code: "app.error.fill.all.required").toString())
            } else {
                User user = utilService.getUserForPVS(co.user, true) // Spotfire Utility has different naming convention
                if (!user) {
                    responseDTO.setFailureResponse(message(code: 'case.series.spotfire.user.not.exist', args: [co.user]).toString())
                } else {
                    ExecutedCaseSeries executedCaseSeries = populateModel(user)
                    executedCaseSeries.isSpotfireCaseSeries = true
                    executedCaseSeries.tenantId = co.tenantId ?: (Holders.config.getProperty('pvreports.multiTenancy.defaultTenant', Long))
                    executedCaseSeries.isTemporary = co.isTemporary
                    ExecutedCaseDeliveryOption executedCaseDeliveryOption = new ExecutedCaseDeliveryOption(sharedWith: [user])
                    executedCaseSeries.executedDeliveryOption = executedCaseDeliveryOption
                    try {
                        caseSeriesService.save(executedCaseSeries, co.generateSetForCaseNumbers())
                        log.info("Case Series Saved.")
                        responseDTO.message = message(code: 'default.created.message', args: [message(code: 'caseSeries.label'), executedCaseSeries.seriesName]).toString()
                        responseDTO.data = [id: executedCaseSeries?.id]
                    } catch (ValidationException ve) {
                        log.error("Exception occurred while save caseSeries ", ve)
                        if (ve.errors.getFieldError('seriesName')?.code == 'unique') {
                            responseDTO.setFailureResponse(message(code: "com.rxlogix.config.executed.caseSeries.seriesName.unique.per.user").toString())
                        } else if (ve.errors.getFieldError('seriesName')?.code == 'exist') {
                            responseDTO.setFailureResponse(message(code: "com.rxlogix.config.executed.caseSeries.seriesName.exists").toString())
                        } else {
                            responseDTO.setFailureResponse(ve.message)
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Exception occurred in method saveCaseSeriesForSpotfire ", ex)
            responseDTO.setFailureResponse(ex)
        }
        render(responseDTO as JSON)
    }

    private ExecutedCaseSeries populateModel(User user) {
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: params.seriesName, owner: user, locale: user.preference.locale, createdBy: user.username, modifiedBy: user.username)
        ExecutedCaseSeriesDateRangeInformation executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation()
        executedCaseSeries.executedCaseSeriesDateRangeInformation = executedCaseSeriesDateRangeInformation
        executedCaseSeriesDateRangeInformation.executedCaseSeries = executedCaseSeries
        executedCaseSeries.executing = false
        executedCaseSeries
    }

    private notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.label.case.series'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    def addEmailConfiguration() {
        ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.get(params.id)
        if (!executedCaseSeries) {
            log.error("Requested Entity not found : ${params.id}")
            render "Not Found"
            return
        }
        render contentType: "application/json", encoding: "UTF-8", text: (executedCaseSeries.emailConfiguration ?: []) as JSON
    }

    def share() {
        if (params.executedConfigId) {
            ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.get(params.executedConfigId)
            if (executedCaseSeries) {
                def result = caseSeriesService.shareExecutedCaseSeries(params, executedCaseSeries)
                sendShareNotification(result.newUsers, result.newGroups, result.executedCaseSeries)
            }
        }
        flash.message = message(code: 'app.configuration.shared.successful')
        redirect action: 'index'
    }

    private void sendShareNotification(Set<User> newUsers, Set<UserGroup> newGroups, ExecutedCaseSeries executedCaseSeries) {
        Set<String> recipients = newUsers*.email as Set
        newGroups.each {
            recipients.addAll(it.getUsers()*.email)
        }
        if (recipients) {
            String timeZone = userService.currentUser?.preference?.timeZone

            String emailSubject = g.message(code: 'app.notification.caseseries.email.shared')
            String url = request.getRequestURL().substring(0, request.getRequestURL().indexOf("/", 8)) + "/reports/caseList/index?cid=" + executedCaseSeries.id
            def content = g.render(template: '/mail/caseseries/caseseries',
                    model: ['seriesInstance': executedCaseSeries, 'url': url, 'userTimeZone': timeZone])
            emailService.sendNotificationEmail(recipients, content, true, emailSubject);
        }
    }

    def email() {
        if (params.executedConfigId) {
            ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.get(params.executedConfigId)
            if (executedCaseSeries) {

                List<String> emailList = []
                if (params.emailToUsers instanceof String) {
                    emailList.add(params.emailToUsers)
                } else {
                    emailList = params.emailToUsers
                }

                List<ReportFormatEnum> formats = []
                if (params.attachmentFormats instanceof String) {
                    formats.add(ReportFormatEnum.valueOf(params.attachmentFormats))
                } else {
                    formats = params.attachmentFormats.collect { ReportFormatEnum.valueOf(it) }
                }
                bindEmailConfiguration(executedCaseSeries, params.emailConfiguration)
                try {
                    emailService.emailReportTo(executedCaseSeries, emailList, formats)
                }
                catch (Exception e){
                    Boolean isOtherException = true

                    Map mailException = [
                    'GrailsMailException': message(code: 'app.report.mail.configuration.exception'),
                    'FileNotFoundException': message(code: 'app.report.file.not.found'),
                    'MaxPagesGovernorException': message(code: 'app.report.maxJasperDataBytes.share'),
                    'TimeoutGovernorException': message(code : 'app.report.mail.timeout.exception'),
                    'JRException': message(code: 'app.report.mail.jre.exception'),
                    'AuthenticationFailedException': message(code: 'app.report.mail.auth.exception')
                    ]

                    for (val in mailException) {
                        if (e.getClass().toString().endsWith(val.getKey())){
                            flash.error = val.getValue()
                            isOtherException = false
                        }
                    }

                    if (isOtherException){
                        flash.error = message(code: 'app.report.mail.default')
                    }

                    log.info("${e.message}. So we are not able to mail this file.")
                }

            }
        }
        redirect action:'index'
    }

    private bindEmailConfiguration(ExecutedCaseSeries caseSeriesInstance, Map emailConfiguration) {

        if (emailConfiguration && emailConfiguration.subject && emailConfiguration.body) {
            EmailConfiguration emailInstance
            if (caseSeriesInstance.emailConfiguration) {
                emailInstance = caseSeriesInstance.emailConfiguration
                emailInstance.isDeleted = false
                bindData(emailInstance, emailConfiguration)
                CRUDService.update(emailInstance)
            } else {
                emailInstance = new EmailConfiguration(emailConfiguration)
                CRUDService.save(emailInstance)
                caseSeriesInstance.emailConfiguration = emailInstance
            }
        } else {
            if (caseSeriesInstance.emailConfigurationId) {
                CRUDService.softDelete(caseSeriesInstance.emailConfiguration, "")
            }
            caseSeriesInstance.emailConfiguration=null
        }
    }

    def checkDeleteForAllAllowed() {
        ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries.get(params.id)
        User currentUser = userService.currentUser
        render "" + (currentUser.isAdmin() || executedCaseSeries.owner == currentUser)
    }
}
