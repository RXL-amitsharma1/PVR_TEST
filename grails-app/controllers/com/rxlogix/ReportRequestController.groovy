package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.commons.io.FilenameUtils
import com.rxlogix.customException.FileFormatException
import org.springframework.web.multipart.MultipartFile

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class ReportRequestController {

    def reportRequestService
    def notificationService
    def emailService
    def CRUDService
    def userService
    def configurationService
    GrailsApplication grailsApplication

    static allowedMethods = [delete: ['DELETE','POST']]
    def qualityService

    @Secured(["ROLE_REPORT_REQUEST_VIEW"])
    def index() {
        List<String> finalWorkflowStates = WorkflowState.getFinalStatesForType(WorkflowConfigurationTypeEnum.REPORT_REQUEST).collect { it.name }
        render view: "index", model: [finalWorkflowStates: finalWorkflowStates]
    }

    def createAdhocReport() {
        forward action: "update", params: [configurationType: ConfigurationTypeEnum.ADHOC_REPORT.name()]
    }

    def createAggregateReport() {
        forward action: "update", params: [configurationType: ConfigurationTypeEnum.PERIODIC_REPORT.name()]
    }

    def updateComments() {
        ReportRequest reportRequestInstance = ReportRequest.get(params.id)
        if (params.version && (reportRequestInstance.version > params.long('version'))) {
            flash.error = message(code: 'app.configuration.update.lock.permission', args: [reportRequestInstance.reportName])
            redirect(action: 'index', id: reportRequestInstance.id)
            return;
        }
        if (reportRequestInstance) {
            Preference preference = userService.currentUser?.preference
            String timeZone = preference?.timeZone
            def oldComments = reportRequestService.getReportComments(reportRequestInstance)
            bindComments(reportRequestInstance, timeZone)

            try {
                reportRequestService.update(reportRequestInstance)
                bindLinks(reportRequestInstance)
                Set<String> recipients = reportRequestService.getNotificationRecipients(reportRequestInstance)
                reportRequestService.sendCommentUpdateNotification(reportRequestInstance, oldComments, recipients)
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.report.request', default: 'Report Request'), reportRequestInstance.reportName])}"
                redirect(action: "show", id: reportRequestInstance.id)

            } catch (Exception ex) {
                ex.printStackTrace()
                StringWriter sw = new StringWriter()
                PrintWriter pw = new PrintWriter(sw)
                ex.printStackTrace(pw)
                flash.error = "${message(code: 'default.server.error.message')} " + sw.toString();
                redirect(action: "show", id: reportRequestInstance.id)
            }
        } else {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.label.report.request'), params.id])
            redirect(action: "index")
        }
    }

    @Secured(["ROLE_REPORT_REQUEST_PLAN_VIEW"])
    def plan() {
        List<String> finalWorkflowStates = WorkflowState.getFinalStatesForType(WorkflowConfigurationTypeEnum.REPORT_REQUEST).collect { it.name }
        List customFields = []
        ReportRequestField.findAllByIsDeletedAndShowInPlan(false, true)?.sort { it.id }
                ?.each {
                    customFields << [mData      : "" + it.name,
                                     "bSortable": false,
                                     "sClass"   : "dataTableColumnCenter"]
                    if (it.fieldType == ReportRequestField.Type.CASCADE) {

                        customFields << [mData      : "secondary" + it.name,
                                         "bSortable": false,
                                         "sClass"   : "dataTableColumnCenter"]
                    }
                }
        render view: "plan", model: [finalWorkflowStates: finalWorkflowStates, customFields: customFields]
    }

    Map getDateRage(ReportRequest reportRequest) {
        if (reportRequest.frequency == ReportRequestFrequencyEnum.RUN_ONCE) {
            return [
                    dateRangeInformation: [dateRangeEnum         : DateRangeEnum.CUSTOM,
                                           dateRangeStartAbsolute: reportRequest.reportingPeriodStart,
                                           dateRangeEndAbsolute  : reportRequest.reportingPeriodEnd,
                    ]
            ]
        }
        if (reportRequest.frequency == ReportRequestFrequencyEnum.DAILY) {
            return [
                    dateRangeInformation: [dateRangeEnum: DateRangeEnum.LAST_X_DAYS, relativeDateRangeValue: reportRequest.frequencyX],
                    calendar            : "{\"startDateTime\":\"${reportRequest.reportingPeriodStart.format("yyyy-MM-dd")}T00:00Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n\",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=${reportRequest.frequencyX ?: 1};COUNT=${reportRequest.occurrences ?: 1}\"}"
            ]
        }
        if (reportRequest.frequency == ReportRequestFrequencyEnum.WEEKLY) {
            return [
                    dateRangeInformation: [dateRangeEnum: DateRangeEnum.LAST_X_WEEKS, relativeDateRangeValue: reportRequest.frequencyX],
                    calendar            : "{\"startDateTime\":\"${reportRequest.reportingPeriodStart.format("yyyy-MM-dd")}T00:00Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n\",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=WEEKLY;BYDAY=SU;INTERVAL=${reportRequest.frequencyX ?: 1};COUNT=${reportRequest.occurrences ?: 1}\"}"
            ]
        }
        if (reportRequest.frequency == ReportRequestFrequencyEnum.MONTHLY) {
            return [
                    dateRangeInformation: [dateRangeEnum: DateRangeEnum.LAST_X_MONTHS, relativeDateRangeValue: reportRequest.frequencyX],
                    calendar            : "{\"startDateTime\":\"${reportRequest.reportingPeriodStart.format("yyyy-MM-dd")}T00:00Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n\",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=MONTHLY;BYMONTHDAY=${reportRequest.reportingPeriodStart.date};INTERVAL=${reportRequest.frequencyX ?: 1};COUNT=${reportRequest.occurrences ?: 1}\"}"
            ]
        }
        if (reportRequest.frequency == ReportRequestFrequencyEnum.YEARLY) {
            return [
                    dateRangeInformation: [dateRangeEnum: DateRangeEnum.LAST_X_YEARS, relativeDateRangeValue: reportRequest.frequencyX],
                    calendar            : "{\"startDateTime\":\"${reportRequest.reportingPeriodStart.format("yyyy-MM-dd")}T00:00Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n\",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=YEARLY;BYMONTH=${reportRequest.reportingPeriodStart.month + 1};BYMONTHDAY=${reportRequest.reportingPeriodStart.date};INTERVAL=${reportRequest.frequencyX ?: 1};COUNT=${reportRequest.occurrences ?: 1}\"}"
            ]
        }
        if (reportRequest.frequency == ReportRequestFrequencyEnum.HOURLY) {
            return [
                    dateRangeInformation: [dateRangeEnum: DateRangeEnum.LAST_X_DAYS, relativeDateRangeValue: reportRequest.frequencyX],
                    calendar            : "{\"startDateTime\":\"${reportRequest.reportingPeriodStart.format("yyyy-MM-dd")}T00:00Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n\",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=HOURLY;INTERVAL=${reportRequest.frequencyX ?: 1};COUNT=${reportRequest.occurrences ?: 1}\"}"
            ]
        }
    }

    def createReport() {
        ReportRequest reportRequestInstance = ReportRequest.get(params.id)
//        bindData(reportRequestInstance, params, ['actionItems', 'requesters', 'assignedTo', 'reportingDestinations'])
//        setReportingDestinations(reportRequestInstance)
        Date asOfVersionDate = reportRequestInstance.asOfVersionDate
        Map attrMap = [
                productSelection         : reportRequestInstance.productSelection,
                productGroupSelection    : reportRequestInstance.productGroupSelection,
                dateRangeType            : reportRequestInstance.dateRangeType,
                evaluateDateAs           : reportRequestInstance.evaluateDateAs.value(),
                excludeFollowUp          : reportRequestInstance.excludeFollowUp,
                includeLockedVersion     : reportRequestInstance.includeLockedVersion,
                includeAllStudyDrugsCases: reportRequestInstance.includeAllStudyDrugsCases,
                excludeNonValidCases     : reportRequestInstance.excludeNonValidCases,
                excludeDeletedCases     : reportRequestInstance.excludeDeletedCases,
                suspectProduct           : reportRequestInstance.suspectProduct,
                studySelection           : reportRequestInstance.studySelection,
                limitPrimaryPath         : reportRequestInstance.limitPrimaryPath,
                asOfVersionDate          : asOfVersionDate ? DateUtil.StringFromDate(asOfVersionDate, DateUtil.DATEPICKER_FORMAT, null) : null,
                includeMedicallyConfirmedCases: reportRequestInstance.includeMedicallyConfirmedCases,
                reportingDestinations         : reportRequestInstance.reportingDestinations?.collect { it.toString() },
                primaryReportingDestination   : reportRequestInstance.primaryReportingDestination,
                inn                           : reportRequestInstance.inn,
                drugCode                      : reportRequestInstance.drugCode,
                primaryPublisherContributor   : reportRequestInstance.primaryPublisherContributor,
                publisherContributors         : reportRequestInstance.publisherContributors?.collect { it },
                dueInDays                     : reportRequestInstance.dueInToHa,
                generatedReportName           : reportRequestInstance.generatedReportName,
                isMultiIngredient             : reportRequestInstance.isMultiIngredient,
                includeWHODrugs               : reportRequestInstance.includeWHODrugs
        ]
        ReportConfiguration configuration = (params.configurationType == ConfigurationTypeEnum.ADHOC_REPORT.name() ? new Configuration(attrMap) : new PeriodicReportConfiguration(attrMap))
        if (params.configurationType == ConfigurationTypeEnum.ADHOC_REPORT.name()) {
            configuration.eventSelection = reportRequestInstance.eventSelection
            configuration.eventGroupSelection = reportRequestInstance.eventGroupSelection
            attrMap.put("eventSelection", configuration.eventSelection)
            attrMap.put("eventGroupSelection", configuration.eventGroupSelection)
        }

        configuration.deliveryOption = new DeliveryOption()
        List requestersIds = reportRequestInstance.requesters*.id
        List requesterGroupIds = reportRequestInstance.requesterGroups*.id
        List sharedWithUsers = requestersIds.collect { Constants.USER_TOKEN.concat(it.toString()) } + requesterGroupIds.collect { Constants.USER_GROUP_TOKEN.concat(it.toString()) }
        String sharedWith = sharedWithUsers.join(";")
        attrMap.put("deliveryOption._attachmentFormats", "")
        attrMap.put("sharedWith", sharedWith)
        configuration.discard()
        Map daterange = getDateRage(reportRequestInstance)
        if (reportRequestInstance.reportRequestType?.aggregate && (reportRequestInstance.reportRequestType.configuration)) {
            configuration = configurationService.copyConfig(reportRequestInstance.reportRequestType.configuration, userService.currentUser, null, Tenants.currentId() as Long, true)
            bindData(configuration, attrMap)
            configuration.deliveryOption.addToSharedWith(reportRequestInstance.requesters)
            configuration.deliveryOption.addToSharedWithGroup(reportRequestInstance.requesterGroups)
            configuration.globalDateRangeInformation.properties=daterange.dateRangeInformation
            if(daterange.calendar)configuration.scheduleDateJSON=daterange.calendar
            CRUDService.save(configuration)
            redirect(controller: "periodicReport", action: "edit", id: configuration.id, params: [fromTemplate: true])
        } else {
            if (reportRequestInstance.reportRequestType?.aggregate) {
                attrMap.put("globalDateRangeInformation.dateRangeEnum", daterange.dateRangeInformation.dateRangeEnum.name())
                attrMap.put("globalDateRangeInformation.relativeDateRangeValue", daterange.dateRangeInformation.relativeDateRangeValue ?: 1)
                attrMap.put("globalDateRangeInformation.dateRangeEndAbsolute", daterange.dateRangeInformation.dateRangeEndAbsolute?.format(DateUtil.DATEPICKER_FORMAT))
                attrMap.put("globalDateRangeInformation.dateRangeStartAbsolute", daterange.dateRangeInformation.dateRangeStartAbsolute?.format(DateUtil.DATEPICKER_FORMAT))
                if (daterange.calendar) attrMap.put("scheduleDateJSON", daterange.calendar)
                attrMap.put("reportingDestinations", reportRequestInstance.reportingDestinations?.join(Constants.MULTIPLE_AJAX_SEPARATOR))
                attrMap.put("publisherContributors", reportRequestInstance.publisherContributors?.collect { it.id }?.join(Constants.MULTIPLE_AJAX_SEPARATOR))
                attrMap.put("primaryPublisherContributor", reportRequestInstance.primaryPublisherContributor?.id)
            } else {
                if (reportRequestInstance.startDate || reportRequestInstance.endDate) {
                    attrMap.put("globalDateRangeInformation.dateRangeEnum", DateRangeEnum.CUSTOM.name())
                    attrMap.put("globalDateRangeInformation.relativeDateRangeValue", 1)
                    attrMap.put("globalDateRangeInformation.dateRangeEndAbsolute", reportRequestInstance.endDate?.format(DateUtil.DATEPICKER_FORMAT))
                    attrMap.put("globalDateRangeInformation.dateRangeStartAbsolute", reportRequestInstance.startDate?.format(DateUtil.DATEPICKER_FORMAT))
                }
            }
            session.setAttribute("editingConfiguration", [requestId: reportRequestInstance.id, configurationParams: (attrMap as JSON).toString()])
            if (params.configurationType == ConfigurationTypeEnum.ADHOC_REPORT.name()) {
                redirect controller: "configuration", action: "create", params: [continueEditing: true]
            } else {
                redirect controller: "periodicReport", action: "create", params: [continueEditing: true]
            }
        }
    }
    /**
     * Action for create view.
     * @return
     */
    @Secured(["ROLE_REPORT_REQUEST_CRUD"])
    def create() {
        ReportRequest reportRequestInstance = new ReportRequest()
        reportRequestInstance.workflowState = WorkflowState.getDefaultWorkState()
        UserGroup defaultAssignTo = UserGroup.getDefaultReportRequestAssignedTo()
        if (!userService.isAnyGranted("ROLE_REPORT_REQUEST_ASSIGN") && !defaultAssignTo) {
            flash.warn = message(code: 'com.rxlogix.config.ReportRequest.noDefaultRRAssignTo')
            redirect(action: "index")
            return
        }
        if (params.dueDate) {
            Preference preference = userService.currentUser?.preference
            String timeZone = preference?.timeZone
            Locale locale = preference?.locale
            reportRequestInstance.dueDate = DateUtil.getEndDate(params.dueDate, locale)
        }
        reportRequestInstance.assignedGroupTo = defaultAssignTo
        render view: "create", model: getModelMap(reportRequestInstance)
    }

    /**
     * Action to save the report request.
     * @return
     */
    @Secured(["ROLE_REPORT_REQUEST_CRUD"])
    def save() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        ReportRequest reportRequestInstance = new ReportRequest()

        //Bind the data.
        bindData(reportRequestInstance, params, ['actionItems', 'requesters', 'assignedTo', 'reportingDestinations', 'customValues', 'comments', 'primaryPublisherContributor', 'publisherContributors'])
        bindPublisherContributors(reportRequestInstance)
        bindCustomValues(reportRequestInstance)
        setReportingDestinations(reportRequestInstance)
        bindAssignedToAndRequestor(reportRequestInstance)
        Preference preference = userService.currentUser?.preference
        String timeZone = preference?.timeZone
        Locale locale = preference?.locale
        reportRequestInstance.dueDate = params.dueDate ? DateUtil.getEndDate(params.dueDate, locale) : null
        reportRequestInstance.completionDate = params.completionDate ? DateUtil.getEndDate(params.completionDate, locale) : null
        reportRequestInstance.startDate = params.startDate ? DateUtil.getStartDate(params.startDate, locale) : null
        reportRequestInstance.endDate = params.endDate ? DateUtil.getEndDate(params.endDate, locale) : null
        bindAsOfVersionDate(reportRequestInstance, params.asOfVersionDate)
        reportRequestInstance.owner = userService.currentUser
        reportRequestInstance.workflowState = WorkflowState.getDefaultWorkState()
        bindAssociations(reportRequestInstance, locale, timeZone)
        if (reportRequestInstance.includeWHODrugs) {
            reportRequestInstance.isMultiIngredient = true
        }

        String datesValidationMessage = reportRequestService.validateDatesBeforeSave(reportRequestInstance)
        if (!datesValidationMessage.isEmpty()) {
            flash.error = datesValidationMessage
            render view: "create", model: getModelMap(reportRequestInstance)
            return
        }

        try {
            bindFiles(reportRequestInstance)
            reportRequestService.save(reportRequestInstance)
            bindLinks(reportRequestInstance)
            //Show notifications for report request.
            reportRequestService.sendCreationNotification(reportRequestInstance)

            flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.report.request', default: 'Report Request'), reportRequestInstance.reportName])}"
            redirect(action: "index")
        }  catch(FileFormatException ffe){
            def model = getModelMap(reportRequestInstance)
            model << [showAttachmentWarning: true, linksToAdd: params.linksToAdd,linksToDelete: params.linksToDelete]
            render view: "create", model: model
            return
        } catch (ValidationException ve) {
            def model = getModelMap(reportRequestInstance)
            if (reportRequestInstance.attachments) {
                reportRequestInstance.attachments = []
                model << [showAttachmentWarning: true]
            }
            model << [linksToAdd: params.linksToAdd]
            model << [linksToDelete: params.linksToDelete]
            render view: "create", model: model
            return
        } catch (Exception ex) {
            log.error(ex.message)
            flash.error = message(code: "app.error.500")
            redirect(action: 'create')
            return
        }
    }

    /**
     * Action to edit the report request.
     * @return
     */
    @Secured(["ROLE_REPORT_REQUEST_CRUD"])
    def edit(ReportRequest reportRequestInstance) {
        if (!reportRequestInstance) {
            notFound()
            return
        }
        if (!reportRequestInstance.isViewableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }
        if (reportRequestInstance.workflowState.id in WorkflowState.getFinalStatesForType(WorkflowConfigurationTypeEnum.REPORT_REQUEST)?.collect { it.id }) {
            flash.message = "${message(code: 'app.reportRequest.edit.error.message', args: [reportRequestInstance.workflowState.name])}"
            redirect(action: "index")
        }
        notificationService.deleteNotificationByExecutionStatusId(userService.getCurrentUser(), reportRequestInstance.id, NotificationApp.REPORTREQUEST)
        render view: "edit", model: getModelMap(reportRequestInstance)
    }

    /**
     * Action to show the report request.
     * @return
     */
    @Secured(["ROLE_REPORT_REQUEST_VIEW"])
    def show(ReportRequest reportRequestInstance) {
        if (!reportRequestInstance) {
            notFound()
            return
        }
        if (!reportRequestInstance.isViewableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }
        if (reportRequestInstance.isDeleted) {
            flash.error = message(code: 'app.notification.reportRequest.deleted')
        }
        notificationService.deleteNotificationByExecutionStatusId(userService.getCurrentUser(), reportRequestInstance.id, NotificationApp.REPORTREQUEST)
        User currentUser = userService.currentUser
        Boolean assigned = userService.isCurrentUserAdmin() || reportRequestInstance.assignedToUserList?.any {
            it.id == currentUser.id
        }
        Boolean editable = !(reportRequestInstance.workflowState.id in WorkflowState.getFinalStatesForType(WorkflowConfigurationTypeEnum.REPORT_REQUEST)?.collect { it.id })
        Boolean requestorNotesVisible = (reportRequestInstance.requesters || reportRequestInstance.requesterGroups) ? (reportRequestInstance.requesters?.find { it.id == currentUser.id } ||
                reportRequestInstance.requesterGroups?.find { group -> group.users?.find { it.id == currentUser.id } }) : true
        LinkedHashSet<ReportRequestComment> comments = reportRequestInstance?.comments?.findAll {
            !it.isDeleted
        }?.sort { it.dateCreated }
        render view: "show", model: [reportRequestInstance: reportRequestInstance, editable: editable,
                                     actionItems          : reportRequestInstance.actionItems.findAll { !it.isDeleted }?.sort { it.dateCreated },
                                     comments             : comments, currentUserAssigned: assigned, requestorNotesVisible: requestorNotesVisible,
                                     linkedReports        : reportRequestInstance.getLinkedReports()]
    }

    /**
     * Action to update the report request.
     * @return
     */
    @Secured(["ROLE_REPORT_REQUEST_CRUD"])
    def update() {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        ReportRequest reportRequestInstance = ReportRequest.get(params.id)
        if (params.version && (reportRequestInstance.version > params.long('version'))) {
            flash.error = message(code: 'app.configuration.update.lock.permission', args: [reportRequestInstance.reportName])
            redirect(action: 'edit', id: reportRequestInstance.id)
            return;
        }
        def oldReportRequestRef = reportRequestService.getReportRequestMap(reportRequestInstance)
        def oldActionItems = reportRequestService.getReportActionItems(reportRequestInstance)
        if (reportRequestInstance) {
            bindData(reportRequestInstance, params, ['actionItems', 'requesters', 'assignedTo', 'reportingDestinations', 'customValues', 'comments', 'primaryPublisherContributor', 'publisherContributors'])
            bindPublisherContributors(reportRequestInstance)
            bindCustomValues(reportRequestInstance)
            setReportingDestinations(reportRequestInstance)
            bindAssignedToAndRequestor(reportRequestInstance, true)
            Preference preference = userService.currentUser?.preference
            String timeZone = preference?.timeZone
            Locale locale = preference?.locale
            def oldComments = reportRequestService.getReportComments(reportRequestInstance)
            def oldAttachments = reportRequestService.getAttachments(reportRequestInstance)

            reportRequestInstance.dueDate = params.dueDate ? DateUtil.getEndDate(params.dueDate, locale) : null
            reportRequestInstance.completionDate = params.completionDate ? DateUtil.getEndDate(params.completionDate, locale) : null
            reportRequestInstance.startDate = params.startDate ? DateUtil.getStartDate(params.startDate, locale) : null
            reportRequestInstance.endDate = params.endDate ? DateUtil.getEndDate(params.endDate, locale) : null
            bindAsOfVersionDate(reportRequestInstance, params.asOfVersionDate)

            bindAssociations(reportRequestInstance, locale, timeZone)
            if (reportRequestInstance.includeWHODrugs) {
                reportRequestInstance.isMultiIngredient = true
            }
            def newReportRequestRef = reportRequestService.getReportRequestMap(reportRequestInstance)

            try {
                bindFiles(reportRequestInstance)
                reportRequestService.update(reportRequestInstance)
                bindLinks(reportRequestInstance)
                reportRequestService.sendUpdateModeNotification(reportRequestInstance, newReportRequestRef, oldReportRequestRef, oldComments, oldActionItems)
                if (params.configurationType) {
                    forward action: "createReport"
                } else {
                    flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.report.request', default: 'Report Request'), reportRequestInstance.reportName])}"
                    redirect(action: "index")
                }
            } catch(FileFormatException ffe){
                def model = getModelMap(reportRequestInstance)
                model << [showAttachmentWarning: true,linksToAdd: params.linksToAdd,linksToDelete: params.linksToDelete]
                render view: "edit", model: model
                return
            } catch (ValidationException ve) {
                def model = getModelMap(reportRequestInstance)
                if (reportRequestInstance.attachments && (reportRequestInstance.attachmentsString != oldReportRequestRef.attachmentsString)) {
                    reportRequestInstance.attachments = oldAttachments.collect { new ReportRequestAttachment(id: it.id, name: it.name) }
                    model << [showAttachmentWarning: true]
                }
                model << [linksToAdd: params.linksToAdd]
                model << [linksToDelete: params.linksToDelete]
                render view: "edit", model: model
            } catch (Exception ex) {
                log.error("Error while updating report request ${params.id}", ex)
                flash.error = "${message(code: 'default.server.error.message')} " + ex.message
                def model = getModelMap(reportRequestInstance)
                if (reportRequestInstance.attachments && (reportRequestInstance.attachmentsString != oldReportRequestRef.attachmentsString)) {
                    reportRequestInstance.attachments = oldAttachments.collect { new ReportRequestAttachment(id: it.id, name: it.name) }
                    model << [showAttachmentWarning: true]
                }
                model << [linksToAdd: params.linksToAdd]
                model << [linksToDelete: params.linksToDelete]
                render view: "edit", model: model
            }
        } else {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.label.report.request'), params.id])
            redirect(action: "index")
        }
    }

    /**
     * Action to delete the report request.
     * @return
     */
    @Secured(["ROLE_REPORT_REQUEST_CRUD"])
    def delete(ReportRequest reportRequestInstance) {
        if (!reportRequestInstance) {
            notFound()
            return
        }
        if (!reportRequestInstance.isViewableBy(userService.currentUser)) {
            flash.warn = message(code: "app.warn.noPermission")
            redirect(action: "index")
            return
        }
        try {
            reportRequestService.delete(reportRequestInstance, params.deleteJustification)
            reportRequestService.sendDeleteNotification(reportRequestInstance)
            reportRequestService.sendDeleteEmailNotification(reportRequestInstance)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.report.request', default: 'Report Request'), reportRequestInstance.reportName])}"
        } catch (ValidationException ve) {
            flash.error = "Unable to delete the report request"
        }
        redirect(action: "index")
    }

    @Secured(["ROLE_REPORT_REQUEST_CRUD"])
    def copyNext(ReportRequest reportRequestInstance) {
        if (!reportRequestInstance) {
            notFound()
            return
        }

        ReportRequest copy = reportRequestService.copyNext(reportRequestInstance)

        if (reportRequestInstance.hasErrors()) {
            chain(action: "index", model: [theInstance: copy])
        } else {
            flash.message = message(code: "app.copy.success", args: [copy.reportName])
            redirect(action: "edit", id: copy.id)
        }
    }

    @Secured(["ROLE_REPORT_REQUEST_CRUD"])
    def copy(ReportRequest reportRequestInstance) {
        if (!reportRequestInstance) {
            notFound()
            return
        }

        ReportRequest copy = reportRequestService.copy(reportRequestInstance)

        if (reportRequestInstance.hasErrors()) {
            chain(action: "index", model: [theInstance: copy])
        } else {
            flash.message = message(code: "app.copy.success", args: [copy.reportName])
            redirect(action: "show", id: copy.id)
        }
    }

    /**
     * This method gives the action item map.
     * @param index
     * @return
     */
    private getActionItemMap(index) {

        def assignedTo = null
        def assignedGroupTo = null
        def assignedToParam = params.("actionItems[" + index + "].assignedTo")
        if (assignedToParam.startsWith(Constants.USER_GROUP_TOKEN)) {
            assignedGroupTo = UserGroup.get(Long.valueOf(assignedToParam.replaceAll(Constants.USER_GROUP_TOKEN, '')))
        } else if (assignedToParam.startsWith(Constants.USER_TOKEN)) {
            assignedTo = User.get(Long.valueOf(assignedToParam.replaceAll(Constants.USER_TOKEN, '')))
        }

        def actionItemMap = [
                actionCategory : ActionItemCategory.findByKey(params.("actionItems[" + index + "].actionCategory")),
                assignedTo     : assignedTo,
                assignedGroupTo: assignedGroupTo,
                priority       : params.("actionItems[" + index + "].priority"),
                status         : params.("actionItems[" + index + "].status"),
                appType        : params.("actionItems[" + index + "].appType"),
                comment        : params.("actionItems[" + index + "].comment"),
                description    : params.("actionItems[" + index + "].description")
        ]
        actionItemMap
    }

    /**
     * This method renders the tasks in json format.
     */
    def findTasks() {

        def taskTemplateId = params.taskTemplateId

        def tasks = {}

        if (taskTemplateId) {
            tasks = reportRequestService.findTasks(taskTemplateId)
        }

        response.status = 200
        render tasks as JSON
    }

    /** ********************************************************************************************************************/
    /******************************** Code block for data binding  *******************************************************/
    /** ********************************************************************************************************************/

    /**
     * This method binds the associations with the report request.
     * @param reportRequestInstance
     */
    private void bindAssociations(ReportRequest reportRequestInstance, Locale locale, timeZone) {

        //add, update or delete action items
        bindActionItems(reportRequestInstance, locale, timeZone)

        //add, update or delete comments
        bindComments(reportRequestInstance, timeZone)
    }
    private void bindPublisherContributors(ReportRequest reportRequest) {
        reportRequest.publisherContributors?.clear()
        if (params.primaryPublisherContributor) {
            reportRequest.primaryPublisherContributor = User.get(params.long("primaryPublisherContributor"))
        } else {
            reportRequest.primaryPublisherContributor = userService.currentUser
        }
        if (params.publisherContributors) {
            params.publisherContributors.toString().split(Constants.MULTIPLE_AJAX_SEPARATOR).each {
                if (it != params.primaryPublisherContributor) {
                    reportRequest.addToPublisherContributors(User.get(it as Long))
                }
            }
        }
    }
    private bindComments(reportRequestInstance, timeZone) {
        ReportRequestComment reportRequestComment
        Boolean newObj
        Boolean commentDeleted
        String commentText
        String dateCreated
        for (int i = 0; params.containsKey("comments[" + i + "]"); i++) {
            newObj = Boolean.valueOf(params["comments[" + i + "].newObj"])
            commentDeleted = Boolean.valueOf(params["comments[" + i + "].deleted"])
            commentText = params["comments[" + i + "].reportComment"]
            dateCreated = params["comments[" + i + "].dateCreated"]
            if (newObj) {
                //insert block
                if (!commentDeleted) {
                    reportRequestComment = new ReportRequestComment(reportComment: commentText)
                    reportRequestComment.isDeleted = commentDeleted.booleanValue()
                    reportRequestComment.newObj = newObj.booleanValue()
                    reportRequestComment.dateCreated = dateCreated ? DateUtil.parseDateWithTimeZone(dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) : null
                    reportRequestInstance.addToComments(reportRequestComment)
                }
            } else {
                reportRequestComment = reportRequestInstance?.comments?.find {
                    it.id == Long.parseLong(params["comments[" + i + "].id"] as String)
                }
                if (reportRequestComment) {
                    if (commentDeleted) {
                        // delete block
                        CRUDService.softDelete(reportRequestComment, reportRequestComment.id)
                        reportRequestInstance.removeFromComments(reportRequestComment)
                        reportRequestComment.delete()
                    } else {
                        // update block
                        reportRequestComment.reportComment = commentText
                    }
                }
            }
        }
    }

    private bindActionItems(reportRequestInstance, locale, timeZone) {
        ActionItem actionItem
        Boolean newObj
        Boolean actionItemDeleted
        String description

        for (int i = 0; params.containsKey("actionItems[" + i + "]"); i++) {
            newObj = Boolean.valueOf(params["actionItems[" + i + "].newObj"])
            actionItemDeleted = Boolean.valueOf(params["actionItems[" + i + "].deleted"])
            description = params["actionItems[" + i + "].description"]
            if (newObj) {
                //insert block
                if (!actionItemDeleted) {
                    actionItem = new ActionItem(description: description)
                    actionItem.properties = getActionItemMap(i)
                    actionItem.deleted = actionItemDeleted.booleanValue()
                    actionItem.newObj = newObj.booleanValue()
                    String dueDate = params["actionItems[" + i + "].dueDate"]
                    String completionDate = params["actionItems[" + i + "].completionDate"]
                    String dateCreated = params["actionItems[" + i + "].dateCreatedObj"]
                    if (dueDate) {
                        actionItem.dueDate = DateUtil.getEndDate(dueDate, locale)
                    }
                    if (completionDate) {
                        actionItem.completionDate = DateUtil.getEndDate(completionDate, locale)
                    }
                    if (dateCreated) {
                        actionItem.dateCreated = DateUtil.parseDateWithTimeZone(dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)
                    }
                    reportRequestInstance.addToActionItems(actionItem)
                }
            } else {
                actionItem = reportRequestInstance?.actionItems?.find {
                    it.id == Long.parseLong(params["actionItems[" + i + "].id"] as String)
                }
                if (actionItem && actionItemDeleted) {
                    // delete block
                    reportRequestInstance.removeFromActionItems(actionItem)
                    CRUDService.softDelete(actionItem, actionItem.id)
                }
            }
        }
    }

    private notFound() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.found.message', args: [message(code: 'app.label.report.request'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private notSaved() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.saved.message')
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private bindAsOfVersionDate(ReportRequest reportRequest, def asOfDate) {
        if (!(reportRequest?.evaluateDateAs in [EvaluateCaseDateEnum.LATEST_VERSION, EvaluateCaseDateEnum.ALL_VERSIONS])) {
            reportRequest.includeLockedVersion = true
        } else {
            reportRequest.includeLockedVersion = params?.includeLockedVersion ?: false
        }
        if (reportRequest.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
            reportRequest.asOfVersionDate = DateUtil.getEndDate(asOfDate, userService.currentUser?.preference?.locale)
        } else {
            reportRequest.asOfVersionDate = null
        }
    }

    private Map getModelMap(ReportRequest reportRequestInstance) {
        List<ReportRequestType> reportRequestTypes = ReportRequestType.findAllByIsDeleted(false, [sort: 'name', order: 'asc'])
        List<ReportRequestPriority> reportRequestPriority = ReportRequestPriority.findAllByIsDeleted(false)
        List<ReportRequestLinkType> linkType = ReportRequestLinkType.findAllByIsDeleted(false)
        List<TaskTemplate> taskTemplates = TaskTemplate.findAllByIsDeletedAndType(false, TaskTemplateTypeEnum.REPORT_REQUEST, [sort: 'name', order: 'asc'])
        if (reportRequestInstance?.id && !reportRequestTypes.find {
            it.id == reportRequestInstance.id
        }) {
            reportRequestTypes.add(reportRequestInstance.reportRequestType)
        }
        LinkedHashSet<ActionItem> actionItems = reportRequestInstance?.actionItems?.findAll {
            !it.isDeleted
        }?.sort { it.dateCreated }
        LinkedHashSet<ReportRequestComment> comments = reportRequestInstance?.comments?.findAll {
            !it.isDeleted
        }?.sort { it.dateCreated }
        Boolean assigned = userService.isCurrentUserAdmin() || reportRequestInstance.assignedToUserList?.id?.contains(userService.currentUser.id)
        Boolean editable = !(reportRequestInstance.workflowState.id in WorkflowState.getFinalStatesForType(WorkflowConfigurationTypeEnum.REPORT_REQUEST)?.collect { it.id })
        User currentUser = userService.currentUser
        Boolean requestorNotesVisible = (reportRequestInstance.requesters || reportRequestInstance.requesterGroups) ? (reportRequestInstance.requesters?.find { it.id == currentUser.id } ||
                reportRequestInstance.requesterGroups?.find { group -> group.users?.find { it.id == currentUser.id } }) : true
        return [taskTemplates: taskTemplates, reportRequestTypes: reportRequestTypes, reportRequestPriority: reportRequestPriority, reportRequestInstance: reportRequestInstance, editable: editable, linkType: linkType,
                actionItems  : actionItems, requestorNotesVisible: requestorNotesVisible, comments: comments, users: User.findAllByEnabled(true).sort { it?.fullName?.trim()?.toUpperCase() }, currentUserAssigned: assigned, linkedReports: reportRequestInstance.getLinkedReports()
        ]
    }

    private bindCustomValues(reportRequestInstance) {
        Map map = [:]
        map = params.customValue?.collectEntries { k, v ->
            if (v instanceof String[]) {
                [(k): v.join(";")]
            } else {
                [(k): v]
            }
        }
        reportRequestInstance.customValues = (map as JSON).toString()
    }

    private bindAssignedToAndRequestor(ReportRequest reportRequest, Boolean isUpdate = false) {
        if (isUpdate) {
            if (userService.isAnyGranted("ROLE_REPORT_REQUEST_ASSIGN")) {
                reportRequest.assignedGroupTo = null
                reportRequest.assignedTo = null
            }
            reportRequest.requesters?.clear()
            reportRequest.requesterGroups?.clear()
        }
        String assignedTo = params.assignedTo
        if (userService.isAnyGranted("ROLE_REPORT_REQUEST_ASSIGN") && assignedTo) {
            if (assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                reportRequest.assignedGroupTo = UserGroup.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
            } else if (assignedTo.startsWith(Constants.USER_TOKEN)) {
                reportRequest.assignedTo = User.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_TOKEN, '')))
            }
        }

        if (!reportRequest.assignedTo && !reportRequest.assignedGroupTo) {
            reportRequest.assignedGroupTo = UserGroup.getDefaultReportRequestAssignedTo()
        }

        if (params.requesters) {
            params.list("requesters").each { String requestor ->
                if (requestor.startsWith(Constants.USER_GROUP_TOKEN)) {
                    UserGroup userGroup = UserGroup.get(Long.valueOf(requestor.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    if (userGroup) {
                        reportRequest.addToRequesterGroups(userGroup)
                    }
                } else if (requestor.startsWith(Constants.USER_TOKEN)) {
                    User user = User.get(Long.valueOf(requestor.replaceAll(Constants.USER_TOKEN, '')))
                    if (user) {
                        reportRequest.addToRequesters(user)
                    }
                }
            }
            reportRequest.requestorsNames = reportRequest.requestorList?.join(" ,")
        }
    }

    private bindFiles(ReportRequest reportRequest) {
        Preference preference = userService.currentUser?.preference
        String timezone = preference?.timeZone
        String dateCreated = DateUtil.toDateString(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM)

        if (reportRequest.attachments && params.attachmentsToDelete) {
            Set<Long> del = params.attachmentsToDelete?.tokenize(",")?.findAll { it }?.collect { it.trim() as Long }
            if (del) {
                reportRequest.attachments.findAll { it.id in del }.each {
                    reportRequest.removeFromAttachments(it)
                }
            }
        }

        request.getFiles('file').each { MultipartFile file ->
            if (file.originalFilename) {

                String filename = file.originalFilename.toLowerCase()
                def allowedExtensions = grailsApplication.config.reportRequest.fileType

                if (filename.length() >= 255 || !filename.contains('.')) {
                    log.error("Rejected: Filename too long or missing extension")
                    throw new FileFormatException("Invalid or unsupported file name or extension")
                }

                List<String> parts = filename.tokenize('.')
                String lastExtension = parts.last()
                List<String> precedingExtensions = parts[0..-2]


                if (!(lastExtension in allowedExtensions)) {
                    throw new FileFormatException("File format not allowed")
                }

                // Suspicious extensions we NEVER allow in base names
                def dangerousExtensions = ['exe', 'sh', 'bat', 'cmd', 'php', 'js', 'jar', 'com', 'dll', 'vbs']

                boolean hasDangerousPrecedingExt = precedingExtensions.any { it in dangerousExtensions }

                if (hasDangerousPrecedingExt) {
                    log.error("Rejected: Suspicious embedded extensions in base name")
                    throw new FileFormatException("Suspicious file name with multiple extensions")
                }

                if (file.size > 0) {
                    ReportRequestAttachment attachment = new ReportRequestAttachment()
                    attachment.name = RxCodec.encode(file.originalFilename)
                    attachment.fileAttachment = new FileAttachment(data: file.bytes)
                    attachment.dateCreated = DateUtil.parseDateWithTimeZone(dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timezone)
                    reportRequest.addToAttachments(attachment)
                }
            }
        }
    }

    private bindLinks(ReportRequest reportRequest) {

        params.linksToDelete?.tokenize(",")?.findAll { it }?.collect { it.trim() as Long }?.each {
            CRUDService.softDelete(ReportRequestLink.get(it as Long), it)
        }

        if (params.linksToAdd) {
            JSON.parse(params.linksToAdd).each { link ->
                ReportRequestLink rrl = new ReportRequestLink(link)
                rrl.from = reportRequest
                CRUDService.save(rrl)
            }
        }
    }

    def downloadAttachment() {
        ReportRequestAttachment attachment = ReportRequestAttachment.get(params.long("id"))
        if (!attachment) {
            notFound()
            return
        }
        render(file: attachment.fileAttachment?.data, fileName: RxCodec.decode(attachment.name), contentType:"application/octet-stream" )
    }

    def actionItemStatusForReportRequest() {
        Map<String, String> result = reportRequestService.actionItemStatusForReportRequest(params.id)
        render result as JSON
    }

    private void setReportingDestinations(ReportRequest reportRequest) {
        reportRequest?.reportingDestinations?.clear()
        if (params.reportingDestinations) {
            params.reportingDestinations.toString().split(Constants.MULTIPLE_AJAX_SEPARATOR).each {
                if (it != reportRequest.primaryReportingDestination) {
                    reportRequest.addToReportingDestinations(it)
                }
            }
        }
    }

    def removeParent(long id) {
        ReportRequest child = ReportRequest.get(id)
        child.parentReportRequest = null
        CRUDService.save(child)
        render "ok"
    }

    def setAsParent(long parentId, long childId) {
        ReportRequest child = ReportRequest.get(childId)
        child.parentReportRequest = parentId
        CRUDService.save(child)
        render "ok"
    }

    @Secured(['ROLE_PERIODIC_CONFIGURATION_CRUD'])
    def exportToExcel() {
        User currentUser = userService.currentUser
        LibraryFilter filter = new LibraryFilter(params, currentUser, ReportRequest)
        List<Long> ids = ReportRequest.fetchByFilter(filter, params.sort, params.order).list()*.first()
        def data = []
        List<ReportRequest> reportRequestList
        ids.collate(999).each {
            reportRequestList = ReportRequest.getAll(it)
            reportRequestList.each { reportRequestInstance ->
                def product = reportRequestInstance.productSelection ? JSON.parse(reportRequestInstance.productSelection as String) : [:]
                data.add([reportRequestInstance.id,
                          reportRequestInstance.reportName,
                          reportRequestInstance.masterPlanningRequest ? ViewHelper.getMessage("default.button.yes.label") : ViewHelper.getMessage("default.button.no.label"),
                          reportRequestInstance.description ?: "",
                          reportRequestInstance.suspectProduct ? ViewHelper.getMessage("default.button.yes.label") : ViewHelper.getMessage("default.button.no.label"),
                          product["1"]?.collect { it.name }?.join(","),
                          product["2"]?.collect { it.name }?.join(","),
                          product["3"]?.collect { it.name }?.join(","),
                          product["4"]?.collect { it.name }?.join(","),
                          reportRequestInstance.psrTypeFile,
                          reportRequestInstance.allRequestorNames ? reportRequestInstance.allRequestorNames.join(', ') : '',
                          reportRequestInstance.assignedToName(),
                          reportRequestInstance.reportRequestType?.name,
                          reportRequestInstance.priority?.name,
                          reportRequestInstance.workflowState?.name,
                          reportRequestInstance.dateRangeType?.getName(),
                          reportRequestInstance.evaluateDateAs?.name(),
                          reportRequestInstance.asOfVersionDate?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                          reportRequestInstance.dueDate?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                          reportRequestInstance.inn,
                          reportRequestInstance.drugCode,
                          reportRequestInstance.ibd?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                          reportRequestInstance.primaryReportingDestination,
                          reportRequestInstance.reportingDestinations?.join(","),
                          reportRequestInstance.reportRequestType.aggregate ? reportRequestInstance.reportingPeriodStart?.format(DateUtil.ISO_DATE_TIME_FORMAT) : reportRequestInstance?.startDate?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                          reportRequestInstance.reportRequestType.aggregate ? reportRequestInstance.reportingPeriodEnd?.format(DateUtil.ISO_DATE_TIME_FORMAT) : reportRequestInstance?.endDate?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                          reportRequestInstance.frequency?.name(),
                          reportRequestInstance?.frequencyX,
                          reportRequestInstance?.occurrences,
                          reportRequestInstance?.dueInToHa,
                          reportRequestInstance?.dueDateForDistribution?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                          reportRequestInstance?.curPrdDueDate?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                          reportRequestInstance?.periodCoveredByReport,
                          reportRequestInstance?.customValues

                ])
            }
        }
        def metadata = [sheetName: "Report Requests",
                        columns  : [
                                [title: "ID", width: 25],
                                [title: ViewHelper.getMessage("app.label.report.request.name"), width: 25],
                                [title: ViewHelper.getMessage("app.label.report.request.masterPlanningRequest"), width: 25],
                                [title: ViewHelper.getMessage("app.label.description"), width: 25],
                                [title: ViewHelper.getMessage("app.label.SuspectProduct"), width: 25],
                                [title: ViewHelper.getMessage("app.widget.button.quality.product.label") + " " + ViewHelper.getMessage("productDictionary.ingredient"), width: 25],
                                [title: ViewHelper.getMessage("app.widget.button.quality.product.label") + " " + ViewHelper.getMessage("productDictionary.family"), width: 25],
                                [title: ViewHelper.getMessage("app.periodicReport.executed.productName.label"), width: 25],
                                [title: ViewHelper.getMessage("app.trade.name"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.psrTypeFile"), width: 25],
                                [title: ViewHelper.getMessage("app.label.report.request.request.by"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.assigned.to"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.request.type"), width: 25],
                                [title: ViewHelper.getMessage("app.label.report.request.priority"), width: 25],
                                [title: ViewHelper.getMessage("app.label.report.request.status"), width: 25],
                                [title: ViewHelper.getMessage("app.label.DateRangeType"), width: 25],
                                [title: ViewHelper.getMessage("evaluate.on.label"), width: 25],
                                [title: ViewHelper.getMessage("evaluate.on.date.label"), width: 25],
                                [title: ViewHelper.getMessage("app.report.request.dueDate.label"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.inn"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.drugCode"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.ibd"), width: 25],
                                [title: ViewHelper.getMessage("app.label.primaryReportingDestination"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportingDestinations"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.reportingPeriodStart"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.reportingPeriodEnd"), width: 25],
                                [title: ViewHelper.getMessage("app.label.frequency"), width: 25],
                                [title: "X:", width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.occurrences"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.dueDateToHa"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.dueDateForDistribution"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.currentPeriodDueDateToHa"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.periodCoveredByReport"), width: 25],
                                [title: ViewHelper.getMessage("app.label.reportRequest.customValues"), width: 25],
                        ]]
        byte[] file = qualityService.exportToExcel(data, metadata)
        String fileName = System.currentTimeMillis() + ".xlsx"
        AuditLogConfigUtil.logChanges(reportRequestList, [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, ViewHelper.getMessage("auditLog.entityValue.bulk.export", "Report Request", ReportFormatEnum.XLSX.displayName))
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)
    }

    def importExcel() {
        MultipartFile file = request.getFile('file')
        Workbook workbook = null
        if (file.originalFilename?.toLowerCase()?.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(file.inputStream);
        } else if (file.originalFilename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(file.inputStream);
        }
        Map result = reportRequestService.importFromExcel(workbook)
        String addCountMessage = reportRequestService.getDisplayMessage('app.reportRequest.error.added', result.added)
        String updateCountMessage = reportRequestService.getDisplayMessage('app.reportRequest.error.updated', result.updated)
        flash.html = true
        flash.message = [addCountMessage, updateCountMessage].join("\n\n")
        if (result.errors.size() > 0)
            flash.error = result.errors.size() + " errors:\n" + result.errors.join("\n")
        redirect(action: "index")
    }

}
