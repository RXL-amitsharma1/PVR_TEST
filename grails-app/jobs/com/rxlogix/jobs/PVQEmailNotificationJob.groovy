package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders

class PVQEmailNotificationJob {

    def qualityService

    static concurrent = true
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'PVQEmailNotificationTrigger', startDelay: 5000, cronExpression: Holders.config.getRequiredProperty('pvqEmailNotificationJob.cronExpression')
    }

    @WithoutTenant
    def execute() {
        try{
            qualityService.sendPVQEmailNotifications()
        } catch (Exception e) {
            log.error("Exception in PVQEmailNotificationJob: ${e.message}")
        }
    }
}
