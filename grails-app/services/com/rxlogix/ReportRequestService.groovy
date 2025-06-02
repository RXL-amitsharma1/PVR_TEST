package com.rxlogix

import com.rxlogix.config.FileAttachment
import com.rxlogix.config.WorkflowState
import com.rxlogix.enums.StatusEnum
import grails.gorm.transactions.Transactional
import com.rxlogix.config.ReportRequest
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.mapping.LmIngredient
import com.rxlogix.mapping.LmLicense
import com.rxlogix.mapping.LmProduct
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.time.TimeCategory
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.grails.web.util.WebUtils
import org.springframework.transaction.annotation.Propagation
import org.springframework.validation.FieldError

@Transactional
class ReportRequestService {

    def CRUDService
    def userService
    def taskTemplateService
    def notificationService
    def emailService
    def groovyPageRenderer
    def actionItemService
    /**
     * Service method to save the report request.
     * @param reportRequestInstance
     * @return
     * @throws ValidationException
     */
    @Transactional
    def save(reportRequestInstance) throws ValidationException {

        //TODO: This one below is dirty. Need to clean this thing up.
        def user = userService.getUser()
        reportRequestInstance.comments?.collect{ comment ->
            comment.createdBy = user.username
            comment.modifiedBy = user.username
        }
        reportRequestInstance.actionItems?.collect{ actionItem ->
            actionItem.createdBy = user.username
            actionItem.modifiedBy = user.username
        }
        reportRequestInstance.attachments?.collect{ attachment ->
            attachment.createdBy = user.username
            attachment.modifiedBy = user.username
        }
        CRUDService.save(reportRequestInstance)
    }

    /**
     * Service method to update the report request.
     * @param reportRequestInstance
     * @return
     * @throws ValidationException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    def update(reportRequestInstance) throws ValidationException {

        //TODO: This one below is dirty. Needs to clean this thing up.
        def user = userService.getUser()
        reportRequestInstance.comments?.collect { comment ->
            if (!comment.createdBy) {
                comment.createdBy = user.username
            }
            comment.modifiedBy = user.username
        }

        reportRequestInstance.actionItems?.collect { actionItem ->
            if (!actionItem.createdBy) {
                actionItem.createdBy = user.username
            }
            actionItem.modifiedBy = user.username
        }

        reportRequestInstance.attachments?.collect { attachment ->
            if (!attachment.createdBy) {
                attachment.createdBy = user.username
            }
            attachment.modifiedBy = user.username
        }

        CRUDService.update(reportRequestInstance)
    }

    /**
     * Service method to delete the report request.
     * @param reportRequestInstance
     * @return
     * @throws ValidationException
     */
    def delete(reportRequestInstance, String justification) throws ValidationException {
        CRUDService.softDelete(reportRequestInstance,reportRequestInstance.reportName,justification)
    }

    /**
     * Service method to find tasks based on the task template id.
     * @param taskTemplateId
     * @return
     */
    def findTasks(taskTemplateId) {
        taskTemplateService.findTasks(taskTemplateId)
    }
    /** ********************************************************************************************************************/
    /******************************** Code block for notifications  *******************************************************/
    /** ********************************************************************************************************************/

    /**
     * This method sends the creation notifications.
     * @param reportRequestInstance
     */
    @NotTransactional
    void sendCreationNotification(ReportRequest reportRequestInstance) {

        Set<String> recipients = [] as Set

        reportRequestInstance.requestorUserList?.each {
            recipients.add(getRecipientsByEmailPreference(it, Constants.CREATE))  //Requesters should be sent notification.
        }

        reportRequestInstance.assignedToUserList.each {
            recipients.add(getRecipientsByEmailPreference(it, Constants.CREATE))    //Notifications should be sent to the assigned to user as well.
        }

        recipients.remove(null) //Remove any null objects inserted.

        /**
         * Notification for the report request creation.
         */
        def emailSubject = ViewHelper.getMessage('app.notification.reportRequest.email.created')
        sendReportRequestNotification(reportRequestInstance, recipients, 'create', null, emailSubject)

        /**
         * Notification for the action items.
         */

        def aiCreatedSubject = ViewHelper.getMessage('app.notification.actionItem.email.created')
        Set<String> allRecipients = recipients
        sendActionItemNotification(reportRequestInstance, reportRequestInstance.actionItems, 'create', allRecipients, aiCreatedSubject)

        /**
         * Notification for the report request comments.
         */
        //Email subject.
        def createdSubject = ViewHelper.getMessage('app.notification.comment.email.created')
        sendCommentNotification(reportRequestInstance, reportRequestInstance.comments, 'create', recipients, createdSubject)

    }

    //Method to get recipient email according to preference
    String getRecipientsByEmailPreference(User user, String mode){
        String recipient = null
        if (mode == Constants.CREATE) {
            if (user?.preference?.reportRequestEmail?.creationEmails)
                recipient = user.email
        }
        else if (mode == Constants.UPDATE) {
            if (user?.preference?.reportRequestEmail?.updateEmails)
                recipient = user.email
        }
        else if (mode == Constants.DELETE) {
            if (user?.preference?.reportRequestEmail?.deleteEmails)
                recipient = user.email
        }
        else if (mode == Constants.WORKFLOW_UPDATE) {
            if (user?.preference?.reportRequestEmail?.workflowUpdate)
                recipient = user.email
        }
        return recipient
    }

    /**
     * This method sends the update notifications.
     */
    @NotTransactional
    void sendUpdateModeNotification(ReportRequest reportRequestInstance,
                                    def newReportRequestRef,
                                    def oldReportRequestRef, def oldComments, def oldActionItems) {

        Set<String> recipients = getNotificationRecipients(reportRequestInstance, Constants.UPDATE)

        def showReportRequestNotification = false

        if (newReportRequestRef != oldReportRequestRef) {
            showReportRequestNotification = true
        }

        //Notification flow goes here.
        if (showReportRequestNotification) {

            /**
             * Notification for the report request update..
             */
            def repReqSubject = ViewHelper.getMessage('app.notification.reportRequest.email.updated')
            sendReportRequestNotification(reportRequestInstance, recipients, 'update', oldReportRequestRef, repReqSubject)
        }

        /**
         * Notification for the report request comment update.
         */
        sendCommentUpdateNotification(reportRequestInstance, oldComments, recipients)

        /**
         * Notification for the report request action item update.
         */
        showActionItemUpdateNotification(reportRequestInstance, oldActionItems, recipients)
    }

    @NotTransactional
    void sendDeleteNotification(ReportRequest reportRequestInstance) {
        notificationService.addNotification(reportRequestInstance.assignedToUserList as List,
                'app.notification.reportRequest.deleted', reportRequestInstance.id as Long, NotificationLevelEnum.INFO, NotificationApp.REPORTREQUEST)

    }
    void sendDeleteEmailNotification(ReportRequest reportRequestInstance) {
        Set<String> recipients = [] as Set
        reportRequestInstance.requestorUserList.each {
            recipients.add(getRecipientsByEmailPreference(it, Constants.DELETE))    //Requesters should be sent notification.
        }
        reportRequestInstance.assignedToUserList.each {
            recipients.add(getRecipientsByEmailPreference(it, Constants.DELETE))    //Notifications should be sent to the assigned to user as well.
        }
        recipients.add(getRecipientsByEmailPreference(reportRequestInstance.owner, Constants.DELETE))    //Notifications should be sent to the owner to user as well.
        recipients.remove(null)
        def content = groovyPageRenderer.render(template: '/mail/reportRequest/reportRequest',
                model: ['oldReportRequestRef': reportRequestInstance, 'reportRequest': reportRequestInstance, 'mode': "delete", 'url': null, 'userTimeZone': userService.currentUser?.preference?.timeZone])
        String subject = ViewHelper.getMessage('app.notification.reportRequest.email.deleted')
        emailService.sendNotificationEmail(recipients, content, true, subject);
    }

    @NotTransactional
    Set<String> getNotificationRecipients(ReportRequest reportRequestInstance, String mode=Constants.UPDATE) {
        Set<String> recipients = [] as Set
        reportRequestInstance.requestorUserList?.each {
            recipients.add(getRecipientsByEmailPreference(it, mode))    //Requesters should be sent notification.
        }
        reportRequestInstance.assignedToUserList.each {
            recipients.add(getRecipientsByEmailPreference(it, mode))    //Notifications should be sent to the assigned to user as well.
        }
        recipients.add(getRecipientsByEmailPreference(reportRequestInstance.owner, mode))    //Notifications should be sent to the owner to user as well.
        recipients.remove(null) //Remove any null objects inserted.
        return recipients
    }

    /**
     * This method send the comment update notification
     * @param reportRequestInstance
     * @param oldComments
     * @param recipients
     */
    @NotTransactional
    void sendCommentUpdateNotification(ReportRequest reportRequestInstance, def oldComments, def recipients) {

        //The new comments.
        def newComments = getReportComments(reportRequestInstance)

        //First of all added comments are determined.
        def addedComments = newComments.minus(oldComments)

        //Prepare the modified comment list
        def modifiedCommentList = []
        def modifiedCommentOldList = []
        def updateCommentModifiedList = []
        oldComments.each { def oldComment ->
            addedComments.each { def newComment ->
                if (newComment.id == oldComment.id) {
                    if (!newComment.reportComment.equals(oldComment.reportComment)) {
                        modifiedCommentList.add(newComment)
                        modifiedCommentOldList.add(oldComment)
                        def updatedComment = [
                                "newComment": newComment.reportComment,
                                "oldComment": oldComment.reportComment,
                                "id"        : newComment.id
                        ]
                        updateCommentModifiedList.add(updatedComment)
                    }
                }
            }
        }

        //Remove the modified instances from added comment list to get actually added comments.
        addedComments = addedComments.minus(modifiedCommentList)

        //Send notification for added comments.
        if (addedComments?.size() > 0) {
            def createdSubject = ViewHelper.getMessage('app.notification.comment.email.created')
            sendCommentNotification(reportRequestInstance, addedComments, 'create', recipients, createdSubject);
        }

        //Send notification for the comment update.
        if (updateCommentModifiedList.size() > 0) {
            def updatedSubject = ViewHelper.getMessage('app.notification.comment.email.updated')
            sendCommentNotification(reportRequestInstance, updateCommentModifiedList, 'update', recipients, updatedSubject);
        }

        //Find the deleted comments as well.
        def deletedComments = oldComments.minus(newComments);

        //Remove the modified old instances coming in the deleted.
        deletedComments = deletedComments.minus(modifiedCommentOldList);

        //Send notification for the deleted comment.
        if (deletedComments.size() > 0) {
            def deletedSubject = ViewHelper.getMessage('app.notification.comment.email.deleted')
            sendCommentNotification(reportRequestInstance, deletedComments, 'delete', recipients, deletedSubject);
        }
    }

    /**
     * This method sends the comment notifications for the report request created.
     * @param reportRequestInstance
     * @param recipients
     */
    @NotTransactional
    void sendCommentNotification(
            ReportRequest reportRequestInstance, def comments, def mode, def recipients, def subject) {
        def request = WebUtils.retrieveGrailsWebRequest().getCurrentRequest()
        if (comments) {

            comments.each {

                //App Notification for the assignee.
                notificationService.addNotification(reportRequestInstance.assignedToUserList as List,
                        'app.notification.comment.added', reportRequestInstance.id, NotificationLevelEnum.INFO, NotificationApp.REPORTREQUEST)

                //Send the notifications to each requesters as well.
                reportRequestInstance.requestorUserList?.each {
                    notificationService.addNotification(it, 'app.notification.comment.added', reportRequestInstance.id,
                            NotificationLevelEnum.INFO, NotificationApp.REPORTREQUEST)
                }

                //Url
                def url = request.getRequestURL().substring(0, request.getRequestURL().indexOf("/", 8)) + "/reports/reportRequest/show?id=" + reportRequestInstance.id

                def content = null;
                if (mode == 'update') {
                    content = groovyPageRenderer.render(template: '/mail/comment/comment',
                            model: ['comment': it.newComment, 'oldComment': it.oldComment, 'mode': mode, 'url': url, 'reportRequestName': reportRequestInstance.reportName])
                } else {
                    content = groovyPageRenderer.render(template: '/mail/comment/comment',
                            model: ['comment': it.reportComment, 'mode': mode, 'url': url, 'reportRequestName': reportRequestInstance.reportName])
                }

                //Send email notification as well.
                emailService.sendNotificationEmail(recipients, content, true, subject);

            }
        }
    }

    /**
     * This method sends the notifications for the action item created from update flow.
     * @param reportRequestInstance
     * @param recipients2
     */
    @NotTransactional
    void showActionItemUpdateNotification(ReportRequest reportRequestInstance,
                                          def oldActionItems, def recipients) {

        def newActionItems = getReportActionItems(reportRequestInstance)
        def addedActionItems = newActionItems.findAll { newAi -> !oldActionItems.find { it.id == newAi.id } }
        if (addedActionItems?.size() > 0) {
            def createdSubject = ViewHelper.getMessage('app.notification.actionItem.email.created')
            sendActionItemNotification(reportRequestInstance, addedActionItems, 'create', recipients, createdSubject)
        }

        //TODO: Need to add the delete flow.

        //Find the deleted comments as well.
//        def deletedActionItems = oldActionItems.minus(newActionItems)
//
//        //Remove the modified old instances coming in the deleted.
//        deletedActionItems = deletedActionItems.minus(modifiedActionItemOldList)
//
//        //Send notification for the deleted comment.
//        if (deletedActionItems.size() > 0) {
//            def deletedSubject = g.message(code: 'app.notification.comment.email.deleted')
//            sendActionItemNotification(reportRequestInstance, deletedActionItems, 'delete', recipients, deletedSubject)
//        }

    }

    /**
     * This method sends the notifications for the action item created from creation flow.
     * @param reportRequestInstance
     * @param recipients2
     */
    @NotTransactional
    void sendActionItemNotification(ReportRequest reportRequestInstance,
                                    def actionItems, def mode, def recipients, def actionItemSubject) {
        def request = WebUtils.retrieveGrailsWebRequest().getCurrentRequest()
        String[] ccRecipients=recipients?.toArray(new String[0])
        //Notification to be sent only when there are action items.
        if (actionItems) {

            //Iterate over the action items.
            actionItems.each {

                //App Notification for the assignee
                notificationService.addNotification(reportRequestInstance.assignedToUserList as List,
                        'app.notification.actionItem.assigned', it.id as Long, NotificationLevelEnum.INFO, NotificationApp.ACTIONITEM)

                def actionItemLinkUrl = request.getRequestURL().substring(0, request.getRequestURL().indexOf("/", 8)) + "/reports/actionItem/index"

                //Content of template
                def actionItemContent = groovyPageRenderer.render(template: '/mail/actionItem/actionItem',
                        model: ['actionItem': it, 'oldActionItemRef': it, 'mode': mode, 'url': actionItemLinkUrl, 'userTimeZone': userService.currentUser?.preference?.timeZone])

                //Send email notification as well.
                recipients=[]
                Long actionItemId = it.id
                ActionItem actionItem = ActionItem.get(actionItemId)
                    actionItem.assignedToUserList.each {
                        recipients.add(getRecipientsByEmailPreference(it,Constants.CREATE))
                        emailService.sendNotificationEmail(recipients, actionItemContent, true, actionItemSubject,ccRecipients);

                    }
            }
        }
    }

    /**
     * This method sends the notifications for the report request created.
     * @param reportRequestInstance
     * @param recipients
     */
    @NotTransactional
    void sendReportRequestNotification(
            ReportRequest reportRequestInstance, def recipients, def mode, def oldReportRequestRef, emailSubject) {
        String notificationMsg = '';
        String timeZone = userService.currentUser?.preference?.timeZone

        if (mode == 'create') {

            //App Notification for the assignee
            notificationService.addNotification(reportRequestInstance.assignedToUserList as List,
                    'app.notification.reportRequest.assigned', reportRequestInstance.id as Long, NotificationLevelEnum.INFO, NotificationApp.REPORTREQUEST)

            notificationMsg = 'app.notification.reportRequest.created'
        } else {
            if (reportRequestInstance?.assignedTo?.id != oldReportRequestRef?.assignedToId || reportRequestInstance?.assignedGroupTo?.id != oldReportRequestRef?.assignedGroupToId) {
                notificationService.addNotification(reportRequestInstance.assignedToUserList as List,
                        'app.notification.report.request.update.new', reportRequestInstance.id, NotificationLevelEnum.INFO, NotificationApp.REPORTREQUEST)
                Set<User> oldAssignedToUsers = oldReportRequestRef.assignedToId ? [User.get(oldReportRequestRef.assignedToId)] : UserGroup.get(oldReportRequestRef.assignedGroupToId)?.users
                notificationService.addNotification(oldAssignedToUsers as List,
                        'app.notification.report.request.update', reportRequestInstance.id, NotificationLevelEnum.INFO, NotificationApp.REPORTREQUEST)

                reportRequestInstance.assignedToUserList.each {
                    recipients.add(it.email) //Notifications should be sent to the assigned to user as well.
                }
                recipients.remove(null) //Remove any null objects inserted.
            } else {
                if (userService.currentUser != reportRequestInstance.createdBy)
                    notificationService.addNotification(User.findByUsername(reportRequestInstance.createdBy),
                            'app.notification.report.request.to.owner.modified', reportRequestInstance.id, NotificationLevelEnum.INFO, NotificationApp.REPORTREQUEST)
            }
            notificationMsg = 'app.notification.reportRequest.updated'
        }

        //Send the notifications to each requesters as well
        reportRequestInstance.requestorUserList?.each {
            notificationService.addNotification(it, notificationMsg, reportRequestInstance.id,
                    NotificationLevelEnum.INFO, NotificationApp.REPORTREQUEST)
        }
        def request = WebUtils.retrieveGrailsWebRequest().getCurrentRequest()
        def url = request.getRequestURL().substring(0, request.getRequestURL().indexOf("/", 8)) + "/reports/reportRequest/show?id=" + reportRequestInstance.id
        def content = groovyPageRenderer.render(template: '/mail/reportRequest/reportRequest',
                model: ['oldReportRequestRef': oldReportRequestRef, 'reportRequest': reportRequestInstance, 'mode': mode, 'url': url, 'userTimeZone': timeZone])
        emailService.sendNotificationEmail(recipients, content, true, emailSubject);
    }

    @NotTransactional
    def getReportRequestMap(reportRequest) {

        [
                description           : reportRequest?.description,
                requestName           : reportRequest?.reportName,
                assignedTo            : reportRequest?.assignedToName(),
                dueDate               : reportRequest?.dueDate,
                priority              : reportRequest?.priority,
                status                : reportRequest?.workflowState?.name,
                requesters            : reportRequest?.requestorList,
                reportRequestType     : reportRequest?.reportRequestType?.name,
                assignedToId          : reportRequest?.assignedTo?.id,
                assignedGroupToId     : reportRequest?.assignedGroupTo?.id,
                productSelection      : reportRequest?.productSelection,
                productGroupSelection : reportRequest?.productGroupSelection,
                eventSelection        : reportRequest?.eventSelection,
                eventGroupSelection   : reportRequest?.eventGroupSelection,
                studySelection        : reportRequest?.studySelection,
                attachmentsString     : reportRequest?.attachmentsString,
                linksString           : reportRequest?.linksString
        ]

    }

    @NotTransactional
    def getReportComments(reportRequest) {
        def commentList = []
        reportRequest?.comments?.each {
            def commentMap = [
                    "id"           : it.id,
                    "reportComment": it.reportComment
            ]
            commentList.add(commentMap)
        }
        commentList
    }

    @NotTransactional
    def getReportActionItems(reportRequest) {
        def actionItemList = []
        reportRequest?.actionItems?.each {
            def actionItemMap = [
                    description   : it.description,
                    actionCategory: it.actionCategory,
                    assignedTo    : it.assignedToName(),
                    completionDate: it.completionDate?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                    dueDate       : it.dueDate?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                    priority      : it.priority,
                    status        : it.status,
                    id            : it.id
            ]
            actionItemList.add(actionItemMap)
        }
        actionItemList
    }

    @NotTransactional
    def getAttachments(ReportRequest reportRequest) {
        def attachmentList = []
        reportRequest?.attachments?.each {
            def attachmentsMap = [
                    "id"  : it.id,
                    "name": it.name
            ]
            attachmentList.add(attachmentsMap)
        }
        attachmentList
    }

    static Date getReportEndDate(ReportRequest reportRequest) {
        if (reportRequest.frequency == ReportRequestFrequencyEnum.RUN_ONCE) {
            return reportRequest.reportingPeriodEnd
        } else {
            if (reportRequest.reportingPeriodStart) {
                use(TimeCategory) {
                    switch (reportRequest.frequency) {
                        case ReportRequestFrequencyEnum.DAILY:
                            return reportRequest.reportingPeriodStart + (reportRequest.frequencyX ?: 1 * reportRequest.occurrences ?: 1).days
                        case ReportRequestFrequencyEnum.WEEKLY:
                            return reportRequest.reportingPeriodStart + (reportRequest.frequencyX ?: 1 * reportRequest.occurrences ?: 1).weeks
                        case ReportRequestFrequencyEnum.MONTHLY:
                            return reportRequest.reportingPeriodStart + (reportRequest.frequencyX ?: 1 * reportRequest.occurrences ?: 1).months
                        case ReportRequestFrequencyEnum.YEARLY:
                            return reportRequest.reportingPeriodStart + (reportRequest.frequencyX ?: 1 * reportRequest.occurrences ?: 1).years
                        case ReportRequestFrequencyEnum.HOURLY:
                            return reportRequest.reportingPeriodStart + (reportRequest.frequencyX ?: 1 * reportRequest.occurrences ?: 1).hours
                    }
                }
            }
        }
    }

    static Date getDueDateToHa(ReportRequest reportRequest) {
        Date out
        Date end = getReportEndDate(reportRequest)
        if (end) {
            use(TimeCategory) {
                out = end + (reportRequest.dueInToHa ? reportRequest.dueInToHa.days : 0.days)

            }
        }
        return out
    }

    ReportRequest copyNext(ReportRequest reportRequest) {
        ReportRequest copy = copy(reportRequest)
        if (reportRequest.frequency == ReportRequestFrequencyEnum.RUN_ONCE) {
            use(TimeCategory) {
                def delta = reportRequest.reportingPeriodEnd - reportRequest.reportingPeriodStart
                copy.reportingPeriodStart = reportRequest.reportingPeriodEnd + 1.day
                copy.reportingPeriodEnd = copy.reportingPeriodStart + delta
                copy.previousPeriodEnd = reportRequest.reportingPeriodEnd
            }
        } else {
            use(TimeCategory) {
                Date endDate

                switch (reportRequest.frequency) {
                    case ReportRequestFrequencyEnum.DAILY:
                        endDate = copy.reportingPeriodStart + (reportRequest.frequencyX * reportRequest.occurrences).days
                        break
                    case ReportRequestFrequencyEnum.WEEKLY:
                        endDate = copy.reportingPeriodStart + (reportRequest.frequencyX * reportRequest.occurrences).weeks
                        break
                    case ReportRequestFrequencyEnum.MONTHLY:
                        endDate = copy.reportingPeriodStart + (reportRequest.frequencyX * reportRequest.occurrences).months
                        break
                    case ReportRequestFrequencyEnum.YEARLY:
                        endDate = copy.reportingPeriodStart + (reportRequest.frequencyX * reportRequest.occurrences).years
                        break
                    case ReportRequestFrequencyEnum.HOURLY:
                        endDate = copy.reportingPeriodStart + (reportRequest.frequencyX * reportRequest.occurrences).hours
                        break
                }
                copy.reportingPeriodStart = endDate + 1.day
                copy.previousPeriodEnd = endDate
            }
        }

        copy.previousPeriodStart = reportRequest.reportingPeriodStart
        copy.previousPsrTypeFile = reportRequest.psrTypeFile
        copy
    }

    ReportRequest copy(ReportRequest reportRequest) {
        User currentUser = userService.currentUser
        ReportRequest copy = new ReportRequest(reportRequest.properties.findAll {! (it.key in ["reportingDestinations","allReportingDestinations", "publisherContributors" , "allPublisherContributors" ])})
        reportRequest.reportingDestinations?.each {
            copy.addToReportingDestinations(it)
        }
        reportRequest.publisherContributors?.each {
            copy.addToPublisherContributors(it)
        }
        copy.reportName = ViewHelper.getMessage("app.configuration.copy.of") + " " + reportRequest.reportName
        copy.workflowState = WorkflowState.defaultWorkState
        copy.startDate = reportRequest.startDate
        copy.endDate = reportRequest.endDate
        copy.suspectProduct = reportRequest.suspectProduct
        copy.isMultiIngredient = reportRequest.isMultiIngredient
        copy.priority = reportRequest.priority
        copy.parentReportRequest = null
        copy.linkedConfigurations = null
        copy.linkedGeneratedReports = null
        copy.completionDate = null
        copy.dueDate = new Date()
        copy.owner = currentUser
        copy.requesters = []
        reportRequest.requesters?.each { copy.addToRequesters(it) }
        copy.requesterGroups = []
        reportRequest.requesterGroups?.each { copy.addToRequesterGroups(it) }
        copy.comments = []
        copy.actionItems = []
        copy.attachments = []
        reportRequest.attachments?.each {
            copy.addToAttachments(new ReportRequestAttachment(
                    name: it.name,
                    fileAttachment: it.fileAttachment ? new FileAttachment(data: it.fileAttachment.data) :null,
                    createdBy: currentUser.username,
                    modifiedBy: currentUser.username
            ))
        }
        copy.includeWHODrugs = reportRequest.includeWHODrugs
        CRUDService.save(copy)
        return copy
    }

    String validateDatesBeforeSave(ReportRequest reportRequestInstance) {
        String validationMessage = ''
        Date dueDate = DateUtil.getDateWithDayStartTime(reportRequestInstance.dueDate)
        Date completionDate = DateUtil.getDateWithDayStartTime(reportRequestInstance.completionDate)
        if (dueDate?.before(DateUtil.getDateWithDayStartTime(new Date()))) {
            validationMessage += ViewHelper.getMessage('app.report.request.dueDate.before.now') + '\n'
        }
        if (completionDate?.before(DateUtil.getDateWithDayStartTime(new Date()))) {
            validationMessage += ViewHelper.getMessage('app.report.request.completionDate.before.now') + '\n'
        }
        validationMessage
    }

    def actionItemStatusForReportRequest(String id) {
        ReportRequest reportRequest =  ReportRequest.read(Long.parseLong(id))
        boolean openActionItems = reportRequest?.actionItems.any {it.status != StatusEnum.CLOSED}
        Map<String,String> actionItemStatus = new HashMap<>()
        if(openActionItems)
            actionItemStatus.put("actionItemStatus", "true")
        else
            actionItemStatus.put("actionItemStatus", "false")

        return actionItemStatus
    }

    Map importFromExcel(workbook) {
        def errors = []
        def added = []
        def updated = []
        User currentUser = userService.getCurrentUser()
        Sheet sheet = workbook?.getSheetAt(0);
        Row row;
        if (sheet)
            for (int i = 3; i <= sheet?.getLastRowNum(); i++) {
                if ((row = sheet.getRow(i)) != null) {
                    Boolean empty = true
                    (0..51).each { empty = empty & !getExcelCell(row, it) }
                    if (empty) continue;
                    boolean createNew = !getExcelCell(row, 0)

                    ReportRequest reportRequest
                    if (createNew) {
                        reportRequest = new ReportRequest()
                    } else {
                        if (!getExcelCell(row, 0).isNumber()) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.id", i + 1)
                            continue;
                        }
                        Long id = getExcelCell(row, 0) as Long
                        reportRequest = ReportRequest.get(id)
                        if (!reportRequest) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.notFound", i + 1)
                            continue;
                        }
                    }

                    try {
                        reportRequest.reportName = getExcelCell(row, 1)
                        reportRequest.masterPlanningRequest = toBoolean(getExcelCell(row, 2))
                        reportRequest.description = getExcelCell(row, 3)
                        reportRequest.suspectProduct = toBoolean(getExcelCell(row, 4))

                        reportRequest.productSelection = parseProduct(getExcelCell(row, 5), getExcelCell(row, 6), getExcelCell(row, 7), getExcelCell(row, 8))
                        reportRequest.psrTypeFile = getExcelCell(row, 9)
                        getRequesters(getExcelCell(row, 10), reportRequest)
                        getRequesterGroups(getExcelCell(row, 10), reportRequest)

                        if (getExcelCell(row, 11)) {
                            User u = User.findByFullNameOrUsername(getExcelCell(row, 11), getExcelCell(row, 11))
                            UserGroup ug = UserGroup.findByName(getExcelCell(row, 11))
                            if (!u && !ug) {
                                errors << ViewHelper.getMessage("app.reportRequest.error.assignedTouserNotFound", i + 1)
                                continue;
                            }
                            if (u)
                                reportRequest.assignedTo = u
                            else
                                reportRequest.assignedGroupTo = ug
                        } else {
                            reportRequest.assignedTo = null
                            reportRequest.assignedGroupTo = null
                        }

                        reportRequest.reportRequestType = ReportRequestType.findByName(getExcelCell(row, 12))
                        if (!reportRequest.reportRequestType) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.reportRequestType", i + 1);
                            continue;
                        }

                        reportRequest.priority = ReportRequestPriority.findByName(getExcelCell(row, 13))
                        if (!reportRequest.priority) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.priority", i + 1); continue;
                        }

                        reportRequest.workflowState = WorkflowState.findByName(getExcelCell(row, 14))
                        if (!reportRequest.workflowState) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.workflowState", i + 1); continue;
                        }

                        reportRequest.dateRangeType = DateRangeType.findByName(getExcelCell(row, 15))
                        if (!reportRequest.dateRangeType) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.dateRangeType", i + 1); continue;
                        }

                        try {
                            reportRequest.evaluateDateAs = getExcelCell(row, 16) as EvaluateCaseDateEnum
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.evaluateDateAs", i + 1); continue;
                        }

                        try {
                            reportRequest.asOfVersionDate = DateUtil.parseDate(getExcelCell(row, 17), DateUtil.ISO_DATE_TIME_FORMAT)
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.date", i + 1); continue;
                        }

                        try {
                            reportRequest.dueDate = DateUtil.parseDate(getExcelCell(row, 18), DateUtil.ISO_DATE_TIME_FORMAT)
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.date", i + 1); continue;
                        }

                        reportRequest.inn = getExcelCell(row, 19)
                        reportRequest.drugCode = getExcelCell(row, 20)
                        try {
                            reportRequest.ibd = DateUtil.parseDate(getExcelCell(row, 21), DateUtil.ISO_DATE_TIME_FORMAT)
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.date", i + 1); continue;
                        }
                        reportRequest.primaryReportingDestination = getExcelCell(row, 22)
                        reportRequest.reportingDestinations?.clear()
                        getExcelCell(row, 23)?.split(",")?.each {
                            reportRequest.addToReportingDestinations(it.trim())
                        }
                        try {
                            if (reportRequest.reportRequestType.aggregate) {
                                reportRequest.reportingPeriodStart = DateUtil.parseDate(getExcelCell(row, 24), DateUtil.ISO_DATE_TIME_FORMAT)
                                reportRequest.reportingPeriodEnd = DateUtil.parseDate(getExcelCell(row, 25), DateUtil.ISO_DATE_TIME_FORMAT)
                            } else {
                                reportRequest.startDate = DateUtil.parseDate(getExcelCell(row, 24), DateUtil.ISO_DATE_TIME_FORMAT)
                                reportRequest.endDate = DateUtil.parseDate(getExcelCell(row, 25), DateUtil.ISO_DATE_TIME_FORMAT)
                            }
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.date", i + 1); continue;
                        }
                        try {
                            reportRequest.frequency = getExcelCell(row, 26) as ReportRequestFrequencyEnum
                            reportRequest.frequencyX = toInteger(getExcelCell(row, 27))
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.frequency", i + 1); continue;
                        }
                        try {
                            reportRequest.occurrences = toInteger(getExcelCell(row, 28))
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.occurrences", i + 1); continue;
                        }
                        try {
                            reportRequest.dueInToHa = toInteger(getExcelCell(row, 29))
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.dueInToHa", i + 1); continue;
                        }

                        try {
                            reportRequest.dueDateForDistribution = DateUtil.parseDate(getExcelCell(row, 30), DateUtil.ISO_DATE_TIME_FORMAT)
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.date", i + 1); continue;
                        }

                        try {
                            reportRequest.curPrdDueDate = DateUtil.parseDate(getExcelCell(row, 31), DateUtil.ISO_DATE_TIME_FORMAT)
                        } catch (Exception f) {
                            errors << ViewHelper.getMessage("app.reportRequest.error.curPrdDueDate", i + 1); continue;
                        }
                        reportRequest?.periodCoveredByReport = getExcelCell(row, 32)
                        reportRequest?.customValues = getExcelCell(row, 33)
                        reportRequest.owner = userService.currentUser

                        CRUDService.save(reportRequest)
                        if (createNew)
                            added << "(ID:" + reportRequest.id + ") " + reportRequest.reportName
                        else
                            updated << "(ID:" + reportRequest.id + ") " + reportRequest.reportName
                    } catch (ValidationException v) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.row", i + 1) + " " + v.errors.allErrors.collect { error ->
                            String errSting = error.toString()
                            if (error instanceof FieldError) errSting = ViewHelper.getMessage("app.label.field.invalid.value", error.field)
                            errSting
                        }.join(";")
                    } catch (Exception e) {
                        errors << ViewHelper.getMessage("app.bulkUpdate.error.row", i + 1) + " " + e.getMessage()
                    }
                }
            }
        else {
            errors << ViewHelper.getMessage('app.label.no.data.excel.error')
        }
        [errors: errors, added: added, updated: updated]
    }

    private String getExcelCell(Row row, int i) {
        Cell cell = row?.getCell(i)
        cell?.setCellType(CellType.STRING);
        return cell?.getStringCellValue()?.trim()
    }

    public String getDisplayMessage(String code, List reportNames) {
        ViewHelper.getMessage(code, reportNames.size()) + (reportNames ? ' (' + reportNames.join(",") + ')' : '')
    }

    private boolean toBoolean(String val) {
        return val.trim().equalsIgnoreCase(ViewHelper.getMessage("default.button.yes.label"))
    }

    private String parseProduct(String cell1, String cell2, String cell3, String cell4) {
        def product = ["1": [], "2": [], "3": [], "4": []]
        if (cell1) {
            List ingredientNames = cell1.split(",")*.trim()
            product["1"] = LmIngredient.createCriteria().list() {
                'in'('ingredient', ingredientNames)
                projections {
                    distinct('ingredientId')
                    property('ingredient')
                }
            }.collect { [name: it[1], id: it[0]] }
        }
        if (cell2) {
            List familyNames = cell2.split(",")*.trim()
            product["2"] = LmProductFamily.createCriteria().list() {
                'in'('name', familyNames)
                projections {
                    distinct('productFamilyId')
                    property('name')
                }
            }.collect { [name: it[1], id: it[0]] }
        }
        if (cell3) {
            List productNames = cell3.split(",")*.trim()
            product["3"] = LmProduct.createCriteria().list() {
                'in'('name', productNames)
                projections {
                    distinct('productId')
                    property('name')
                }
            }.collect { [name: it[1], id: it[0]] }
        }
        if (cell4) {
            List tradeNames = cell4.split(",")*.trim()
            product["4"] = LmLicense.createCriteria().list() {
                'in'('tradeName', tradeNames)
                projections {
                    distinct('licenseId')
                    property('tradeName')
                }
            }.collect { [name: it[1], id: it[0]] }
        }
        if (!product["1"] && !product["2"] && !product["3"] && !product["4"]) return null
        return product as JSON

    }

    private boolean getRequesters(val, ReportRequest reportRequest) {
        reportRequest.requesters?.clear()
        val?.split(",")?.each {
            String name = it.trim()
            if (name) {
                User u = User.findByFullNameOrUsername(name, name)
                if (u)
                    reportRequest.addToRequesters(u)
            }
        }
    }

    private boolean getRequesterGroups(val, ReportRequest reportRequest) {
        reportRequest.requesterGroups?.clear()
        val?.split(",")?.each {
            String name = it.trim()
            if (name) {
                UserGroup u = UserGroup.findByName(name)
                if (u)
                    reportRequest.addToRequesterGroups(u)
            }
        }

    }

    private Integer toInteger(String val) {
        return val ? Integer.parseInt(val) : null
    }
}
