package com.rxlogix.localization


import com.rxlogix.user.User

class ReleaseNotesNotifier {
    User user
    String releaseNumber


    static mapping = {
        table name: "RELEASE_NOTIFIER"
        user column: "USER_ID"
        releaseNumber column: "RELEASE_NUMBER"
    }

    static constraints = {
        releaseNumber nullable: true
    }
}
