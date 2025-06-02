package com.rxlogix.config

import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil

class Notification {

    transient def messageSource
    String message
    String messageArgs
    User user
    Date dateCreated
    NotificationLevelEnum level
    long executedConfigId
    long executionStatusId
    NotificationApp appName
    String notificationParameters

    static constraints = {
        message(blank:false, maxSize: 500)
        messageArgs(nullable:true, maxSize: 555)
        user(nullable:false)
        level(nullable:false)
        executedConfigId(nullable:true)
        executionStatusId(nullable:false)
        appName(nullable:true)
        notificationParameters(nullable:true, maxSize: 255)
    }

    static mapping = {
        table name: "NOTIFICATION"
        id column: "ID"
        executedConfigId column: "EC_ID"
        message column: "MESSAGE"
        messageArgs column: "MSG_ARGS"
        user column: "USER_ID"
        dateCreated column: "DATE_CREATED"
        level column: "LVL"
        executionStatusId column: "EXS_ID"
        appName column: "APP_NAME"
        notificationParameters column: "NOTIFICATION_PARAMETERS"
    }

    Map toMap(User user) {
        // Need to encode because of special characters in file name and japanese message
        def argMessage = messageSource.getMessage(message, messageArgs?.split(",").collect().toArray(), user.preference.locale)
        return [id: id, appName: appName?.name, message: argMessage, userId: user?.id, dateCreated: dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT), level: level.name, executedConfigId: executedConfigId, executionStatusId: executionStatusId, notificationParameters: notificationParameters]
    }
}

