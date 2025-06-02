package com.rxlogix

import com.rxlogix.config.DrilldownAccessTracker
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedIcsrReportConfiguration
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.Notification
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import grails.gorm.transactions.Transactional

@Transactional
class NotificationService {

    def CRUDService
    def userService

    final Map<NotificationApp, Map<Long, Set<Long>>> userNotifications = [:]

    boolean addNotificationListener(NotificationApp type, Long entityId, Long userId, Long reportResultId) {
        synchronized (userNotifications) {
            if (type == NotificationApp.PVC_REPORT) {
                DrilldownAccessTracker accessTracker = DrilldownAccessTracker.findByReportResultId(reportResultId)
                if (accessTracker?.getState() == DrilldownAccessTracker.State.ACTIVE) return false
            }
            Map<Long, Set<Long>> typeNotifications = userNotifications.get(type)
            if (!typeNotifications) {
                typeNotifications = [:]
                userNotifications.put(type, typeNotifications)
            }
            Set userList = typeNotifications.get(entityId)
            if (!userList) {
                typeNotifications.put(entityId, [userId] as Set<Long>)
            } else {
                userList.add(userId)
            }
        }
        return true
    }


    void notifyListeners(NotificationApp type, Long entityId) {
        synchronized (userNotifications) {
            Map<Long, Set<Long>> typeNotifications = userNotifications.get(type)
            if (typeNotifications) {
                Set userList = typeNotifications.get(entityId)
                if (userList) {
                    userList.each {
                        addNotification(User.get(it), type.getI18nKeyNotificationMessage(), entityId, NotificationLevelEnum.INFO, type)
                    }
                }
                typeNotifications.put(entityId, new HashSet<Long>())
            }
        }
    }

    void addNotification(Long executedConfigurationId, ExecutionStatus executionStatus, User user) {
        try {
            NotificationLevelEnum status
            String message
            String messageArgs = executionStatus.reportName

            def appName

            if (executionStatus.executedEntityClass == ExecutedPeriodicReportConfiguration.class) {
                appName = NotificationApp.AGGREGATE_REPORT
                if (executionStatus.aggregateReportStatus)
                    messageArgs += " (${ViewHelper.getMessage(executionStatus.aggregateReportStatus.getI18nValueForAggregateReportStatus())})"
            }

            if (executionStatus.executedEntityClass == ExecutedConfiguration.class) {
                appName = NotificationApp.ADHOC_REPORT
                if (executionStatus.aggregateReportStatus)
                    messageArgs += " (${ViewHelper.getMessage(executionStatus.aggregateReportStatus.getI18nValueForAggregateReportStatus())})"
            }

            if (executionStatus.executedEntityClass == ExecutedIcsrReportConfiguration.class) {
                appName = NotificationApp.ICSR_REPORT
                if (executionStatus.aggregateReportStatus)
                    messageArgs += " (${ViewHelper.getMessage(executionStatus.aggregateReportStatus.getI18nValueForAggregateReportStatus())})"
            }

            if (executionStatus.executionStatus == ReportExecutionStatusEnum.COMPLETED) {
                status = NotificationLevelEnum.INFO
                message = "app.notification.completed"
            } else if (executionStatus.executionStatus == ReportExecutionStatusEnum.WARN) {
                status = NotificationLevelEnum.WARN
                message = "app.notification.needsReview"
            } else {
                status = NotificationLevelEnum.ERROR
                message = "app.notification.failed"
            }
            Notification notification = new Notification(user: user, level: status, message: message, messageArgs: messageArgs, appName: appName,
                    executionStatusId: executionStatus.id)
            if (executedConfigurationId) {
                notification.setExecutedConfigId(executedConfigurationId)
            }
            userService.setOwnershipAndModifier(notification).save(flush: true)
            userService.pushNotificationToBrowser(notification, user)
        }
        catch (Exception e) {
            log.error("""Error creating Notification: ${e.message}""")
        }
    }

    void addNotificationForListener(User user, String message, Object id, NotificationLevelEnum level, NotificationApp appName) {
        try {
            Long executionStatusId = ((id instanceof Long) || (id instanceof Integer)) ? id : 0L
            Notification notification = new Notification(user: user, level: level, message: message, appName: appName, executionStatusId: executionStatusId, notificationParameters: id.toString())
            userService.setOwnershipAndModifier(notification).save(flush: true)
            userService.pushNotificationToBrowser(notification, user)
        }
        catch (Exception e) {
            log.error("""Error creating Notification: ${e.message}""")
        }
    }

    void addNotification(User user, String message, long id, NotificationLevelEnum level, NotificationApp appName) {
        try {
            Notification notification = new Notification(user: user, level: level, message: message, appName: appName, executionStatusId: id)
            userService.setOwnershipAndModifier(notification).save(flush: true)
            userService.pushNotificationToBrowser(notification, user)
        }
        catch (Exception e) {
            log.error("""Error creating Notification: ${e.message}""")
        }
    }

    void addNotification(List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName) {
        try {
            userList.each { User user->
                Notification notification = new Notification(user: user, level: level, message: message, appName: appName, executionStatusId: id)
                userService.setOwnershipAndModifier(notification).save(flush: true)
                userService.pushNotificationToBrowser(notification, user)
            }
        }
        catch (Exception e) {
            log.error("""Error creating Notification: ${e.message}""")
        }
    }

    void addNotification(User user, String message, String messageArgs, String notificationParameters = "", long id, NotificationLevelEnum level, NotificationApp appName) {
        try {
            Notification notification = appName == NotificationApp.INBOUNDCOMPLIACE ?
                    new Notification(user: user, level: level, message: message, messageArgs: messageArgs, notificationParameters: notificationParameters, appName: appName, executedConfigId : id) :
                    new Notification(user: user, level: level, message: message, messageArgs: messageArgs, notificationParameters: notificationParameters, appName: appName, executionStatusId: id)
            userService.setOwnershipAndModifier(notification).save(flush: true)
            userService.pushNotificationToBrowser(notification, user)
        }
        catch (Exception e) {
            log.error("""Error creating Notification: ${e.message}""")
        }
    }

    void addNotification(User user, String message, String messageArgs, String notificationParameters = "", NotificationLevelEnum level, NotificationApp appName) {
        try {
            Notification notification = new Notification(user: user, level: level, message: message, messageArgs: messageArgs, notificationParameters: notificationParameters, appName: appName)
            userService.setOwnershipAndModifier(notification).save(flush: true)
            userService.pushNotificationToBrowser(notification, user)
        }
        catch (Exception e) {
            log.error("""Error creating Notification: ${e.message}""")
        }
    }

    void deleteNotification(long idVal, NotificationApp appNameVal) {
        try {
            Notification.where {
                executionStatusId == idVal && appName == appNameVal
            }.deleteAll()
        }
        catch (Exception e) {
            log.error("""Error deleting Notification: ${e.message}""")
        }
    }
    void deleteNotification(Notification notification) {
        try {
            CRUDService.delete(notification)
        }
        catch (Exception e) {
            log.error("""Error deleting Notification: ${e.message}""")
        }
    }

    void deleteNotificationById(id) {
        try {
            Notification notification = Notification.get(id)
            CRUDService.delete(notification)
        }
        catch (Exception e) {
            log.error("""Error deleting Notification: ${e.message}""")
        }
    }

    void deleteExecutedReportNotification(User userObj, def executedReportConfiguration, NotificationApp appNameVal) {
        try {
            Notification.where {
                executedConfigId == executedReportConfiguration.id && appName == appNameVal && user == userObj
            }.deleteAll()
        }
        catch (Exception e) {
            log.error("""Error deleting Notification: ${e.message}""")
        }
    }
    void deleteNotificationByExecutionStatusId(User userObj, Long executionStatusIdVal, NotificationApp appNameVal) {
        try {
            Notification.where {
                executionStatusId == executionStatusIdVal && appName == appNameVal && user == userObj
            }.deleteAll()
        }
        catch (Exception e) {
            log.error("""Error deleting Notification: ${e.message}""")
        }
    }

    void deleteNotificationByNotificationParameters(User userVal, def appNameVal, def caseNumberVal, def versionNumberVal, def executedTemplateQueryIdVal, def isInDraftModeVal){
        try {
            String notificationParametersVal = [caseNumberVal, versionNumberVal, executedTemplateQueryIdVal, isInDraftModeVal].join(" : ")
            Notification.where {
                notificationParameters == notificationParametersVal && appName == appNameVal && user == userVal
            }.deleteAll()
        }
        catch (Exception e) {
            log.error("""Error deleting Notification: ${e.message}""")
        }
    }

}
