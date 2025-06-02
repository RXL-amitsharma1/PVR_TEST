package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders

class PVCEmailNotificationJob {

    def emailService

    static concurrent = true
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'PVCEmailNotificationTrigger', startDelay: 5000, cronExpression: Holders.config.getRequiredProperty('pvcEmailNotificationJob.cronExpression')
    }

    @WithoutTenant
    def execute() {
        try{
            emailService.sendPVCEmailNotifications()
        } catch (Exception e) {
            log.error("Exception in PVCEmailNotificationJob: ${e.message}")
        }
    }
}
