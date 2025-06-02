package com.rxlogix

import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.enums.PriorityEnum
import com.rxlogix.enums.ActionItemFilterEnum
import com.rxlogix.config.ActionItem
import com.rxlogix.config.Capa8D
import com.rxlogix.config.ActionItemCategory
import com.rxlogix.config.DrilldownCLLData
import com.rxlogix.config.DrilldownCLLMetadata
import com.rxlogix.config.DrilldownMetadata
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.InboundDrilldownMetadata
import com.rxlogix.config.QualityCaseData
import com.rxlogix.config.QualitySampling
import com.rxlogix.config.QualitySubmission
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportRequest
import com.rxlogix.config.ReportResult
import com.rxlogix.config.WorkflowState
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.async.Promises
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.hibernate.StaleObjectStateException
import org.hibernate.StaleStateException
import groovy.json.JsonSlurper

import java.text.SimpleDateFormat

@Secured(["isAuthenticated()"])
class ActionItemController implements SanitizePaginationAttributes {

    def userService
    def CRUDService
    def notificationService
    def emailService
    def actionItemService
    def qualityService
    def reportRequestService

    static allowedMethods = [save:'POST', update: ['PUT','POST'], delete: ['DELETE','POST']]

    @Secured(['ROLE_ACTION_ITEM'])
    def index() {}

    /**
     * Action to save the action item.
     * @return
     */
    @Secured(['ROLE_ACTION_ITEM'])
    def save() {
        //Action Item instantiation.
        def actionItem = new ActionItem()
        User user = userService.currentUser
        Preference preference =user?.preference
        String timeZone = preference?.timeZone
        Locale locale = preference?.locale
        //Bind the data.
        bindData(actionItem, params, ['createdBy', 'dueDate', 'dateCreated', 'completionDate', 'assignedTo', 'actionCategory', 'configurationId'])
        bindAssignedTo(actionItem)

        actionItem.dueDate = params.dueDate ? DateUtil.getEndDate(params.dueDate,locale) : null
        actionItem.completionDate = params.completionDate ? DateUtil.getEndDate(params.completionDate,locale) : null
        Date dateCreated = DateUtil.parseDateWithLocaleAndTimeZone(params.dateCreated,DateUtil.DATEPICKER_FORMAT_AM_PM,locale,timeZone)
        actionItem.dateCreated = params.dateCreated ? dateCreated : null
        actionItem.actionCategory = ActionItemCategory.findByKey(params.actionCategory)

        def errorFields = []

        try {
            if (params.sectionId) {
                PublisherConfigurationSection publisherSection = PublisherConfigurationSection.get(params.sectionId as Long)
                actionItem.publisherSection = publisherSection
            }
            if (params.publisherId) {
                PublisherReport publisherReport = PublisherReport.get(params.publisherId as Long)
                actionItem.publisherReport = publisherReport
            }
            CRUDService.save(actionItem)
            if (params.executedReportId) {
                ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.get(params.executedReportId as Long)
                if(executedReportConfiguration) {
                    executedReportConfiguration.actionItems.add(actionItem)
                    CRUDService.instantSaveWithoutAuditLog(executedReportConfiguration)
                }
            }
            errorFields = null
            def emailSubject = g.message(code: 'app.notification.actionItem.email.created')
            if(actionItem.status == StatusEnum.CLOSED){
                emailSubject = g.message(code: 'app.notification.actionItem.email.closed')
            }

            if (actionItem.appType == AppTypeEnum.QUALITY_MODULE) {
                def qualityObj
                if(params.qualityId) {
                    switch (params.dataType) {
                        case PvqTypeEnum.CASE_QUALITY.toString():
                            qualityObj = QualityCaseData.get(params.long("qualityId"))
                            break
                        case PvqTypeEnum.SUBMISSION_QUALITY.toString():
                            qualityObj = QualitySubmission.get(params.long("qualityId"))
                            break
                        default:
                            qualityObj = QualitySampling.get(params.long("qualityId"))
                            break
                    }
                }

                qualityObj.actionItems.add(actionItem)
                CRUDService.instantSaveWithoutAuditLog(qualityObj)
            }
            if (actionItem.appType == AppTypeEnum.QUALITY_MODULE_CAPA || actionItem.appType == AppTypeEnum.PV_CENTRAL_CAPA) {
                Capa8D capa = Capa8D.get(params.long("capaId"))
                if(capa) {
                    if (actionItem.actionCategory == ActionItemCategory.findByKey('QUALITY_MODULE_CORRECTIVE')) capa.addToCorrectiveActions(actionItem)
                    if (actionItem.actionCategory == ActionItemCategory.findByKey('QUALITY_MODULE_PREVENTIVE')) capa.addToPreventiveActions(actionItem)
                    CRUDService.instantSaveWithoutAuditLog(capa)
                }
            }
            if(actionItem.appType == AppTypeEnum.DRILLDOWN_RECORD){
                DrilldownMetadata metadataRecord = DrilldownCLLMetadata.getMetadataRecord(params).get()
                if(metadataRecord == null){
                    metadataRecord = new DrilldownCLLMetadata()
                    metadataRecord.caseId = params.long('masterCaseId')
                    metadataRecord.processedReportId = params.get('processedReportId')
                    metadataRecord.tenantId = params.long('tenantId')
                    metadataRecord.workflowState = WorkflowState.defaultWorkState
                }
                metadataRecord.actionItems.add(actionItem)
                actionItem.configuration = ReportConfiguration.get(params.long("configurationId"))
                CRUDService.saveOrUpdate(metadataRecord)
            }
            if(actionItem.appType == AppTypeEnum.IN_DRILLDOWN_RECORD){
                InboundDrilldownMetadata metadataRecord = InboundDrilldownMetadata.getMetadataRecord(params).get()
                if(metadataRecord == null){
                    metadataRecord = new InboundDrilldownMetadata()
                    metadataRecord.caseId = Long.valueOf(params.get('masterCaseId'))
                    metadataRecord.caseVersion = Long.valueOf(params.get('masterVersionNum'))
                    metadataRecord.senderId =  Long.valueOf(params.get('senderId'))
                    metadataRecord.tenantId = Long.valueOf(params.get('tenantId'))
                    metadataRecord.workflowState = WorkflowState.defaultWorkState
                }
                metadataRecord.actionItems.add(actionItem)
                actionItem.configuration = ReportConfiguration.get(params.long("configurationId"))
                CRUDService.saveOrUpdate(metadataRecord)
            }
            if (actionItem.appType == AppTypeEnum.ACTION_PLAN) {
                actionItem.parentEntityKey = params.parentEntityKey
                actionItem.dateRangeFrom = DateUtil.getDateWithDayStartTime(Date.parse(DateUtil.DATEPICKER_FORMAT, params.dateRangeFrom))
                actionItem.dateRangeTo = DateUtil.getDateWithDayEndTime(Date.parse(DateUtil.DATEPICKER_FORMAT, params.dateRangeTo))
                CRUDService.saveOrUpdate(actionItem)
            }
            actionItemService.sendActionItemNotification(actionItem, 'create', null, emailSubject)
        } catch (ValidationException ve) {
            ve.getErrors().getFieldErrors().each {
                errorFields.add(it.getField())
            }
        }
        if (errorFields) {
            sendResponse(400, errorFields)
        } else {
            if(params.aIHostPage && params.aIHostPage == 'dashboard'){
                flash.message = "Action item Created"
            }
            sendResponse(200, errorFields)
        }
    }

    /**
     * Action to view the action item.
     * @return
     */
    @Secured(['ROLE_ACTION_ITEM'])
    def view() {
        Long actionItemId = params.long('actionItemId')
        Map actionItem = [:]
        if (actionItemId) {
            def actionItemInstance=ActionItem.findByIdAndIsDeleted(actionItemId, false)
            actionItem = actionItemInstance?.toActionItemMap()

            ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.getByActionItem(actionItemId).get()
            if (executedReportConfiguration) {
                actionItem.executedReportConfigurationId = executedReportConfiguration.id
                actionItem.executedReportConfigurationName = executedReportConfiguration.reportName
            }

            ReportRequest reportRequest = ReportRequest.getByActionItem(actionItemId).get()
            if(reportRequest){
                actionItem.reportRequestId = reportRequest.id
                actionItem.reportRequestName = reportRequest.reportName
            }

            if (actionItemInstance.configuration) {
                actionItem.configurationId = actionItemInstance.configuration.id
                actionItem.configurationName = actionItemInstance.configuration.reportName
            }
            if (actionItem.appType == ViewHelper.getMessage("app.actionItemAppType." + AppTypeEnum.QUALITY_MODULE) || actionItem.appType == ViewHelper.getMessage("app.actionItemAppType." + AppTypeEnum.QUALITY_MODULE_CAPA)) {
                notificationService.deleteNotificationByExecutionStatusId(userService.getCurrentUser(), actionItemInstance.id, NotificationApp.QUALITY)
            }else{
                notificationService.deleteNotificationByExecutionStatusId(userService.getCurrentUser(), actionItemInstance.id, NotificationApp.ACTIONITEM)
            }
            if (actionItem.appType == ViewHelper.getMessage("app.actionItemAppType." + AppTypeEnum.DRILLDOWN_RECORD)) {
                Map reasonOfDelayMap = actionItemInstance.getReasonOfDelayData(false)
                actionItem.drilldownRecordCaseNum = reasonOfDelayMap?.drilldownRecordCaseNum
                actionItem.drilldownRecordId = reasonOfDelayMap?.drilldownRecordId
                actionItem.drilldownReportId = reasonOfDelayMap?.drilldownReportId
                actionItem.drilldownReportName = reasonOfDelayMap?.drilldownReportName

            }else if (actionItem.appType == ViewHelper.getMessage("app.actionItemAppType." + AppTypeEnum.IN_DRILLDOWN_RECORD)) {
                Map reasonOfDelayMap = actionItemInstance.getReasonOfDelayData(true)
                actionItem.drilldownRecordCaseNum = reasonOfDelayMap?.drilldownRecordCaseNum
                actionItem.drilldownRecordId = reasonOfDelayMap?.drilldownRecordId
                actionItem.drilldownReportId = reasonOfDelayMap?.drilldownReportId
                actionItem.drilldownReportName = reasonOfDelayMap?.drilldownReportName
            }

            String associatedCaseNumber = Constants.BLANK_STRING
            String associatedCaseVersion = Constants.BLANK_STRING
            String associatedCaseDataType = Constants.BLANK_STRING
            String associatedIssueNumber = Constants.BLANK_STRING
            String associatedId = Constants.BLANK_STRING
            Long associatedIssueId

            if (actionItemInstance.appType == AppTypeEnum.QUALITY_MODULE) {
                //Get CaseNo associated with this actionItem
                Map caseNoDetailMap = qualityService.getCaseNoByActionItemId(actionItemId)
                associatedCaseNumber = caseNoDetailMap['masterCaseNum']
                associatedCaseVersion = caseNoDetailMap['masterVersionNum']
                associatedCaseDataType = caseNoDetailMap['dataType']
                associatedId = caseNoDetailMap['id']
            } else if (actionItemInstance.appType == AppTypeEnum.QUALITY_MODULE_CAPA || actionItemInstance.appType == AppTypeEnum.PV_CENTRAL_CAPA) {
                Map capa = qualityService.getCapa(actionItemId)
                associatedIssueNumber = capa.associatedIssueNumber
                associatedIssueId = capa.associatedIssueId
            }
            actionItem.put("associatedCaseNumber", associatedCaseNumber)
            actionItem.put("associatedCaseVersion", associatedCaseVersion)
            actionItem.put("associatedCaseDataType", associatedCaseDataType)
            actionItem.put("associatedIssueNumber", associatedIssueNumber)
            actionItem.put("associatedIssueId", associatedIssueId)
            actionItem.put("associatedId", associatedId)
        }
        response.status = 200
        render actionItem as JSON
    }

    /**
     * Action to update the action item.
     * @return
     */
    @Secured(['ROLE_ACTION_ITEM'])
    def update() {
        Long actionItemId = params.long('actionItemId')

        if (actionItemId) {

            ActionItem actionItem = ActionItem.findByIdAndIsDeleted(actionItemId, false)

            if (actionItem) {
                if (params.aiVersion && (actionItem.version > params.long('aiVersion'))) {
                    def msg = "${message(code: 'app.configuration.update.lock.permission', args: [message(code: 'app.label.action.app.name', default: 'Action Item')])}"
                    sendResponse(409, msg);
                    return;
                }
                User user = userService.currentUser
                Preference preference = user?.preference
                String timeZone = preference?.timeZone
                Locale locale = preference?.locale

                //Todo: temporary fix for fetching old object, needs to be change in Refactoring of code
                ActionItem oldActionItem = new ActionItem()
                oldActionItem.properties = actionItem.properties

                def oldActionItemRef = getActionItemMap(actionItem)

                bindData(actionItem, params, ['createdBy', 'dueDate', 'dateCreated', 'completionDate', 'appType', 'assignedTo', 'parentEntityKey', 'dateRangeTo', 'dateRangeFrom', 'actionCategory', 'configurationId'])
                bindAssignedTo(actionItem)

                actionItem.dueDate = params.dueDate ? DateUtil.getEndDate(params.dueDate, locale) : null
                actionItem.completionDate = params.completionDate ? DateUtil.getEndDate(params.completionDate, locale) : null
                actionItem.actionCategory = ActionItemCategory.findByKey(params.actionCategory)

                def showNotification = false

                def newActionItemRef = getActionItemMap(actionItem)

                if (newActionItemRef != oldActionItemRef) {
                    showNotification = true
                }

                def errorFields = []
                try {
                    CRUDService.update(actionItem)

                    errorFields = null

                    if (showNotification) {
                        def emailSubject = g.message(code: 'app.notification.actionItem.email.updated')
                        if(actionItem.status == StatusEnum.CLOSED){
                            emailSubject = g.message(code: 'app.notification.actionItem.email.closed')
                        }
                        Set<String> ccRecipients = [] as Set
                        if(params.rptRequestId){
                            ReportRequest rptRequestInstance=ReportRequest.get(params.rptRequestId)
                            ccRecipients=reportRequestService.getNotificationRecipients(rptRequestInstance)
                        }
                        actionItemService.sendActionItemNotification(actionItem, 'update', oldActionItemRef, emailSubject,ccRecipients.toArray(new String[0]))
                    }

                } catch (ValidationException ve) {
                    ve.getErrors().getFieldErrors().each {
                        errorFields.add(it.getField())
                    }
                } catch (StaleObjectStateException se) {
                    def msg = "${message(code: 'app.configuration.update.lock.permission', args: [message(code: 'app.label.action.app.name', default: 'Action Item')])}"
                    sendResponse(409, msg)

                }
                oldActionItem.discard()
                if (errorFields) {
                    sendResponse(400, errorFields)
                } else {
                    String msg = g.actionItemUpdate(message: "Action item Updated")
                    if(params.aIHostPage && params.aIHostPage == 'dashboard'){
                        flash.message = "Action item Updated"
                    }
                    sendResponse(200, msg, params)
                }

            } else {
                def actionItemObj = "${message(code: 'default.not.found.message', args: [message(code: 'app.label.action.app.name', default: 'Action Item'), actionItemId])}"
                sendResponse(404, actionItemObj);
            }
        } else {
            sendResponse(404, 'actionItemId not found.');
        }

    }

    def getActionItemMap(actionItem) {
        [
                description   : actionItem?.description,
                actionCategory: actionItem?.actionCategory,
                assignedTo    : actionItem?.assignedToName(),
                completionDate: actionItem?.completionDate,
                dueDate       : actionItem?.dueDate,
                priority      : actionItem?.priority,
                status        : actionItem?.status,
                assignedToId  : actionItem?.assignedTo? (actionItem?.assignedTo?.id): (actionItem?.assignedGroupTo?.id)
        ]
    }

    /**
     * Action to delete the action item.
     * @return
     */
    @Secured(['ROLE_ACTION_ITEM'])
    def delete() {
        Long actionItemId = params.long('actionItemId')
        if (actionItemId) {
            ActionItem actionItem = ActionItem.findByIdAndIsDeleted(actionItemId, false)
            if (actionItem) {
                try {
                    if (actionItem.appType == AppTypeEnum.QUALITY_MODULE_CAPA || actionItem.appType == AppTypeEnum.PV_CENTRAL_CAPA) {
                        List capaList = Capa8D.capasByActionItem(actionItem.id).list()
                        Capa8D capa = null
                        if (capaList) {capa = Capa8D.get(Capa8D.capasByActionItem(actionItem.id).get())}
                        if (capa) {
                            if (actionItem.actionCategory == ActionItemCategory.findByKey('QUALITY_MODULE_CORRECTIVE')) capa.removeFromCorrectiveActions(actionItem)
                            if (actionItem.actionCategory == ActionItemCategory.findByKey('QUALITY_MODULE_PREVENTIVE')) capa.removeFromPreventiveActions(actionItem)
                            CRUDService.saveOrUpdate(capa)
                        }
                    }
                    CRUDService.softDelete(actionItem, actionItem.getInstanceIdentifierForAuditLog(), params.deleteJustification)
                    sendResponse(200, 'success');
                } catch (ValidationException ve) {
                    sendResponse(404, message(code: "app.notification.action.item.delete.unable"));
                }
            } else {
                String actionItemObj = "${message(code: 'default.not.found.message', args: [message(code: 'app.label.action.app.name', default: 'Action Item'), actionItemId])}"
                sendResponse(404, actionItemObj);
            }
        } else {
            sendResponse(404, 'actionItemId not found.');
        }
    }

    @Secured(['ROLE_ACTION_ITEM'])
    def fetchActionItemCategoryList() {
        def actionItemCategoryList = ActionItemCategory.findAll().collect {
            [key: it.id, value: ViewHelper.getMessage(it.getI18nKey())]

        }
        render actionItemCategoryList as JSON

    }

    @Secured(['ROLE_ACTION_ITEM'])
    def fetchAppType() {
        def appType = AppTypeEnum.collect {
            [key: it.getKey(), name: ViewHelper.getMessage(it.getI18nKey())]

        }
        render appType as JSON

    }

    @Secured(['ROLE_ACTION_ITEM'])
    def fetchStatus() {
        def statusType = StatusEnum.collect {
            [key: it.getKey(), value: ViewHelper.getMessage(it.getI18nKey())]
        }
        render statusType as JSON

    }

    @Secured(['ROLE_ACTION_ITEM'])
    def fetchPriority() {
        def priority = PriorityEnum.collect {
            [key: it.getKey(), name: ViewHelper.getMessage(it.getI18nKey())]
        }
        render priority as JSON

    }

    @Secured(['ROLE_ACTION_ITEM'])
    def exportToExcelForAI() {
        User currentUser = userService.getUser()
        Long executedReportId = params.long("executedReportId")
        Long sectionId = params.long("sectionId")
        Long publisherId = params.long("publisherId")
        Boolean pvq = params.boolean("pvq")
        def filterType = params.filterType ?: ActionItemFilterEnum.MY_OPEN.value

        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params, currentUser, ActionItem)
        def data = []
        List<ActionItem> actionItemList
        if (params.singleActionItemId) {
            data.addAll(getActionItemData(Long.parseLong(params.singleActionItemId)))
        } else {
            List<Long> ids = ActionItem.fetchActionItemsBySearchString(filter, filterType, currentUser, executedReportId, sectionId, publisherId, pvq, params.sort, params.order).list([max: params.max, offset: params.offset])*.first()
            ids.collate(999).each { batchIds ->
                actionItemList = ActionItem.getAll(batchIds)
                actionItemList?.each { actionItem ->
                    data.addAll(getActionItemData(actionItem.id))
                }
            }
        }
        if (!data) {
            flash.warn = message(code: "app.label.localizationHelp.nothingToExport")
            redirect(action: "index")
            return
        }
        byte[] file = qualityService.exportToExcel(data, getMetadata())
        String timeZone = currentUser?.preference?.timeZone ?: "UTC"
        String currentDate = DateUtil.StringFromDate(new Date(), DateUtil.DATEPICKER_UTC_FORMAT, timeZone).toString().replaceAll(/[\/: ]/, "")
        String fileName = "Action Item_"+ currentDate +".xlsx"
        if (params.singleActionItemId) {
            ActionItem actionItem = ActionItem.findById(params.singleActionItemId)
            AuditLogConfigUtil.logChanges(actionItem, [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                    [:], Constants.AUDIT_LOG_EXPORT, " " + ViewHelper.getMessage("auditLog.entityValue.export", ReportFormatEnum.XLSX.displayName))
        } else {
            AuditLogConfigUtil.logChanges(actionItemList, [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                    [:], Constants.AUDIT_LOG_EXPORT, ViewHelper.getMessage("auditLog.entityValue.bulk.export", "Action Item", ReportFormatEnum.XLSX.displayName))
        }
        render(file: file, contentType: grailsApplication.config.getProperty('grails.mime.types.xlsx'), fileName: fileName)
    }

    private List<List<String>> getActionItemData(Long actionItemId) {
        List<List<String>> data = []
        def actionItem = ActionItem.findById(actionItemId)
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration.getByActionItem(actionItem.id)?.get()
        String reportLink = ''
        String caseLink = ''

        if (executedReportConfiguration) {
            reportLink = createReportLink("${grailsApplication.config.getProperty('grails.appBaseURL')}/report/showFirstSection/${executedReportConfiguration.id}", executedReportConfiguration.reportName)
        } else if (actionItem.configuration) {
            reportLink = createReportLink("${grailsApplication.config.getProperty('grails.appBaseURL')}/configuration/view/${actionItem.configuration.id}", actionItem.configuration.reportName)
        }
        ReportRequest reportRequest = ReportRequest.getByActionItem(actionItem.id).get()
        if(reportRequest) {
            reportLink = createReportLink("${grailsApplication.config.getProperty('grails.appBaseURL')}/reportRequest/show/${reportRequest.id}", reportRequest.reportName)
        }


        if (actionItem.appType == AppTypeEnum.QUALITY_MODULE) {
            Map caseNoDetailMap = qualityService.getCaseNoByActionItemId(actionItem.id)
            if(caseNoDetailMap){
                caseLink = createReportLink("${grailsApplication.config.getProperty('grails.appBaseURL')}/quality/caseForm/?type=${caseNoDetailMap.dataType}&caseNumber=${caseNoDetailMap.masterCaseNum}&id=${caseNoDetailMap.id}&versionNumber=${caseNoDetailMap.masterVersionNum}", caseNoDetailMap.masterCaseNum)
            }
        }else if (actionItem.appType == AppTypeEnum.QUALITY_MODULE_CAPA || actionItem.appType == AppTypeEnum.PV_CENTRAL_CAPA) {
            Map capa = qualityService.getCapa(actionItem.id)
            if(capa){
                caseLink = createReportLink("${grailsApplication.config.getProperty('grails.appBaseURL')}/issue/view/${capa.associatedIssueId}", capa.associatedIssueNumber)
            }
        }

        if (actionItem.appType == AppTypeEnum.DRILLDOWN_RECORD) {
        Map reasonOfDelayMap = actionItem.getReasonOfDelayData(false)
            if(reasonOfDelayMap){
            caseLink = createReportLink("${grailsApplication.config.getProperty('grails.appBaseURL')}/advancedReportViewer/viewDelayReason/${reasonOfDelayMap.drilldownReportId}?cllRecordId=${reasonOfDelayMap.drilldownRecordId}",reasonOfDelayMap.drilldownReportName + " - " + reasonOfDelayMap.drilldownRecordCaseNum)
            }
        }else if (actionItem.appType == AppTypeEnum.IN_DRILLDOWN_RECORD) {
        Map reasonOfDelayMap = actionItem.getReasonOfDelayData(true)
            if(reasonOfDelayMap){
                caseLink = createReportLink("${grailsApplication.config.getProperty('grails.appBaseURL')}/advancedReportViewer/viewDelayReason/${reasonOfDelayMap.drilldownReportId}?cllRecordId=${reasonOfDelayMap.drilldownRecordId}",reasonOfDelayMap.drilldownReportName + " - " + reasonOfDelayMap.drilldownRecordCaseNum)
            }

        }

        data.add([
                actionItem.actionCategory,
                actionItem.assignedTo ?: actionItem.assignedGroupTo,
                actionItem.description,
                actionItem.dueDate?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                actionItem.completionDate?.format(DateUtil.ISO_DATE_TIME_FORMAT),
                actionItem.priority,
                actionItem.status,
                actionItem.appType,
                actionItem.comment,
                actionItem.createdBy,
                reportLink,
                caseLink
        ])

        return data
    }

    private String createReportLink(String url, String reportName) {
        return "<a href='$url'>$reportName</a>"
    }

    private Map getMetadata() {
        return [
                sheetName: "Action Item",
                columns: [
                        [title: ViewHelper.getMessage("app.label.action.item.action.category"), width: 27],
                        [title: ViewHelper.getMessage("app.label.action.item.assigned.to"), width: 27],
                        [title: ViewHelper.getMessage("app.label.action.item.description"), width: 27],
                        [title: ViewHelper.getMessage("app.label.action.item.due.date"), width: 27],
                        [title: ViewHelper.getMessage("app.label.action.item.completion.date"), width: 27],
                        [title: ViewHelper.getMessage("app.label.action.item.priority"), width: 27],
                        [title: ViewHelper.getMessage("app.label.action.item.status"), width: 27],
                        [title: ViewHelper.getMessage("app.label.application"), width: 27],
                        [title: ViewHelper.getMessage("app.label.PublisherTemplate.pending.comments"), width: 27],
                        [title: ViewHelper.getMessage("app.label.action.item.owner"), width: 27],
                        [title: ViewHelper.getMessage("app.label.action.item.report"), width: 35, hyperlink: true],
                        [title: ViewHelper.getMessage("app.label.action.item.case.link"), width: 45, hyperlink: true]
                ]
        ]
    }
    //Method to prepare the response.
    private def sendResponse(stat, msg, actionId="") {
        response.status = stat
        Map responseMap = [
                message: msg,
                status: stat,
                actionItemId: actionId
        ]
        render(contentType: "application/json", responseMap as JSON)
    }

    private bindAssignedTo(ActionItem actionItem) {
        actionItem.assignedGroupTo = null
        actionItem.assignedTo = null
        String assignedTo = params.assignedTo
        if (assignedTo) {
            if (assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                actionItem.assignedGroupTo = UserGroup.read(Long.valueOf(assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
            } else if (assignedTo.startsWith(Constants.USER_TOKEN)) {
                actionItem.assignedTo = User.read(Long.valueOf(assignedTo.replaceAll(Constants.USER_TOKEN, '')))
            }
        }
    }

}
