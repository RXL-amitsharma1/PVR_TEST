package com.rxlogix.config

import com.rxlogix.user.User
import grails.plugins.orm.auditable.SectionModuleAudit

@SectionModuleAudit(parentClassName = ['configuration','periodicReportConfiguration','basicPublisherSource'])
class OneDriveUserSettings {
    User user
    String refreshToken
    String accessToken
    Date lastRefresh = new Date()
    Boolean isDeleted = false

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    static constraints = {
        refreshToken(maxSize: 4000)
        accessToken(maxSize: 4000)
        user unique: true
    }

}
