package com.rxlogix.localization


import com.rxlogix.user.User

class SystemNotificationNotifier {
    User user
    SystemNotification systemNotification

    static mapping = {
        table name: "SYS_NOTE_NOTIFIER"
        user column: "USER_ID"
        systemNotification column: "NOTIFICATION_ID"
    }

    static constraints = {
        systemNotification nullable: true
    }
}
