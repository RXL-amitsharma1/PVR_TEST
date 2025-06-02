package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant

class ActionItemNotificationJob {

    def emailService

    static concurrent = true
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'actionItemNotificationTrigger', startDelay: 5000, cronExpression: '0 1 * * * ? *' // each hour
    }

    @WithoutTenant
    def execute() {
           try{
               emailService.sendActionItemNotifications()
           } catch (Exception e) {
               log.error("Exception in ActionItemNotificationJob: ${e.message}")
           }
    }
}
