package com.rxlogix

import com.rxlogix.config.ActionItem
import com.rxlogix.config.DrilldownCLLData
import com.rxlogix.config.DrilldownCLLMetadata
import com.rxlogix.config.DrilldownMetadata
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.InboundDrilldownMetadata
import com.rxlogix.config.QualityCaseData
import com.rxlogix.config.QualitySampling
import com.rxlogix.config.QualitySubmission
import com.rxlogix.config.ReportResult
import com.rxlogix.enums.ActionItemGroupState
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.util.Holders
import grails.web.mapping.LinkGenerator
import groovy.sql.Sql

import java.sql.Connection

class ActionItemService {
    def emailService
    LinkGenerator grailsLinkGenerator
    GrailsApplication grailsApplication
    def userService
    def notificationService
    def groovyPageRenderer
    def utilService

    private Connection getReportConnectionForPVR() {
        return utilService.getReportConnectionForPVR()
    }

    def sendActionItemNotification(ActionItem actionItem, String mode, def oldActionItemRef, def emailSubject,String[] ccRecipients = []) {
        def content
        String timeZone = userService.currentUser?.preference?.timeZone
        Set<String> recipients = []
        def actionItemLinkUrl = ''
        def notificationApp = NotificationApp.ACTIONITEM
        if (actionItem.appType == AppTypeEnum.QUALITY_MODULE || actionItem.appType == AppTypeEnum.QUALITY_MODULE_CAPA) {
            notificationApp = NotificationApp.QUALITY
        }
        if (grailsApplication.config.getProperty('grails.appBaseURL')) {
            actionItemLinkUrl = grailsLinkGenerator.link(controller: "actionItem", action: "index", base: grailsApplication.config.getProperty('grails.appBaseURL'), absolute: true) + "?id=" + actionItem.id
            if (notificationApp == NotificationApp.QUALITY) {
                actionItemLinkUrl = grailsLinkGenerator.link(controller: "quality", action: "actionItems", base: grailsApplication.config.getProperty('grails.appBaseURL'), absolute: true) + "?id=" + actionItem.id
            }
        }
        if (mode == 'create') {
            actionItem.assignedToUserList.each {
                recipients.add(getRecipientsByEmailPreference(it, Constants.CREATE))        //Notifications should be sent to the assigned to user as well.
            }
            notificationService.addNotification(actionItem.assignedToUserList as List,
                    'app.notification.actionItem.assigned', actionItem.id, NotificationLevelEnum.INFO, notificationApp)
            if (actionItem.status == StatusEnum.CLOSED) {
                User owner = User.findByUsername(actionItem.createdBy)
                recipients.add(getRecipientsByEmailPreference(owner, Constants.CREATE))
            }
            sendNotificationToOwnerWhenClosed(actionItem, recipients)
            content = groovyPageRenderer.render(template: '/mail/actionItem/actionItem', model: ['userTimeZone': timeZone, 'actionItem': actionItem, 'mode': 'create', 'url': actionItemLinkUrl, tenantId: Tenants.currentId()])
        } else if (mode == 'update') {
            actionItem.assignedToUserList.each {
                recipients.add(getRecipientsByEmailPreference(it, Constants.UPDATE))   //Notifications should be sent to the assigned to user as well.
            }
            if ((actionItem.assignedTo != null && actionItem.assignedTo.id != oldActionItemRef.assignedToId) || (actionItem.assignedGroupTo && actionItem.assignedGroupTo.id != oldActionItemRef.assignedGroupToId)) {
                notificationService.addNotification(actionItem.assignedToUserList as List, 'app.notification.action.item.update.new', actionItem.id, NotificationLevelEnum.INFO, notificationApp)
                if (oldActionItemRef.assignedToId)
                    notificationService.addNotification(User.get(oldActionItemRef.assignedToId),
                            'app.notification.action.item.update', actionItem.id, NotificationLevelEnum.INFO, notificationApp)
                else
                    notificationService.addNotification(UserGroup.get(oldActionItemRef.assignedGroupToId).users as List,
                            'app.notification.action.item.update', actionItem.id, NotificationLevelEnum.INFO, notificationApp)
            }
            if (userService.currentUser.username != actionItem.createdBy) {
                User owner = User.findByUsername(actionItem.createdBy)
                recipients.add(getRecipientsByEmailPreference(owner, Constants.UPDATE))
                notificationService.addNotification(owner, 'app.notification.actionItem.to.owner.modified', actionItem.id, NotificationLevelEnum.INFO, notificationApp)
            }
            if (actionItem.status == StatusEnum.CLOSED) {
                User owner = User.findByUsername(actionItem.createdBy)
                recipients.add(getRecipientsByEmailPreference(owner, Constants.UPDATE))
            }
            sendNotificationToOwnerWhenClosed(actionItem, recipients)
            content = groovyPageRenderer.render(template: '/mail/actionItem/actionItem',
                    model: ['actionItem': actionItem, 'oldActionItemRef': oldActionItemRef, 'mode': 'update', 'userTimeZone': timeZone, 'url': actionItemLinkUrl, tenantId: Tenants.currentId()])

        }
        if (content != null && recipients.size() > 0) {
            emailService.sendNotificationEmail(recipients.findAll { it }, content, true, emailSubject,ccRecipients)
        }
    }

    //Method to get recipient email according to preference
    public String getRecipientsByEmailPreference(User user, String mode){
        String recipient = null
        if (mode == Constants.CREATE) {
            if (user?.preference?.actionItemEmail?.creationEmails)
                recipient = user.email
        }
        else if (mode == Constants.UPDATE) {
            if (user?.preference?.actionItemEmail?.updateEmails)
                recipient = user.email
        }
        else if (mode == Constants.JOB) {
            if (user?.preference?.actionItemEmail?.jobEmails)
                recipient = user.email
        }
        return recipient
    }

    def sendNotificationToOwnerWhenClosed(ActionItem actionItem, Set<String> recipients) {
        if (actionItem.status == StatusEnum.CLOSED) {
            User owner = User.findByUsername(actionItem.createdBy)
            if (actionItem.appType == AppTypeEnum.QUALITY_MODULE || actionItem.appType == AppTypeEnum.QUALITY_MODULE_CAPA) {
                notificationService.addNotification(owner as List,
                        'app.notification.actionItem.closed', actionItem.id, NotificationLevelEnum.INFO, NotificationApp.QUALITY)
            } else {
                notificationService.addNotification(owner as List,
                        'app.notification.actionItem.closed', actionItem.id, NotificationLevelEnum.INFO, NotificationApp.ACTIONITEM)
            }
        }
    }

    public String getActionItemStatusForDrilldownRecord(def metadataRecord){
        Set<ActionItem> cllRowActionItems = null
        boolean waiting = false
        cllRowActionItems = metadataRecord.actionItems.findAll{!it.deleted}
        if(cllRowActionItems.size() > 0) {
            for(ActionItem cllRowActionItem : cllRowActionItems){
                if ((cllRowActionItem.status != StatusEnum.CLOSED)) {
                    waiting = true
                    if (cllRowActionItem.dueDate < new Date()) {
                        return ActionItemGroupState.OVERDUE.toString()
                    }
                }
            }
            return waiting ? ActionItemGroupState.WAITING.toString() : ActionItemGroupState.CLOSED.toString()
        }else{
            return null
        }
    }

    public DrilldownCLLData getDrilldownRecordForMetadataActionItem(Map metadataMap, Long latestReportId, boolean isInbound){
        StringBuilder stringBuilder = new StringBuilder()
        if(latestReportId){
            ExecutedReportConfiguration reportConfiguration =  ExecutedReportConfiguration.get(latestReportId)
            Long reportResultId = 0L
            String tableName = null
            if(isInbound) {
                reportResultId = ReportResult.findAllByDrillDownSourceInList(reportConfiguration.executedTemplateQueries)?.findAll{it.template.name== Holders.config.getProperty('pvcModule.inbound_processing_template')}?.max{it.id}?.id
            }else {
                reportResultId = ReportResult.findAllByDrillDownSourceInList(reportConfiguration.executedTemplateQueries)?.findAll{it.template.name== Holders.config.getProperty('pvcModule.late_processing_template')}?.max{it.id}?.id
            }
            if(reportResultId){
                stringBuilder.append(" SELECT ID FROM DRILLDOWN_DATA A WHERE A.REPORT_RESULT_ID = "+ reportResultId +" AND A.CLL_ROW_DATA.masterCaseId = '" + metadataMap.masterCaseId + "' ")
                if(isInbound) {
                    stringBuilder.append(" AND A.CLL_ROW_DATA.pvcIcSenderId = '" + metadataMap.senderId + "' ")
                    stringBuilder.append(" AND A.CLL_ROW_DATA.masterVersionNum = '" + metadataMap.masterVersionNum + "' ")
                }else {
                    stringBuilder.append(" AND A.CLL_ROW_DATA.vcsProcessedReportId = '" + metadataMap.vcsProcessedReportId + "' ")
                }
                stringBuilder.append(" AND A.CLL_ROW_DATA.masterEnterpriseId = '" + metadataMap.masterEnterpriseId + "' ")
            }
        }
        Sql pvrsql = null
        List csvList = []
        DrilldownCLLData cllRecord = null
        try {
            String queryString = stringBuilder.toString()
            if(queryString) {
                pvrsql = new Sql(getReportConnectionForPVR())
                Long dbrecord = pvrsql.firstRow(queryString)?.get('ID')
                cllRecord = DrilldownCLLData.get(dbrecord)
            }
        } catch (Exception e) {
            log.error("Exception in fetching CLL Records", e)
        } finally {
            pvrsql?.close()
        }

        return cllRecord
    }
}
