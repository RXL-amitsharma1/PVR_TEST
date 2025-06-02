package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant

class ActionItemTasksJob {
    def taskTemplateService

    static concurrent = true
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'actionItemTasksTrigger', startDelay: 5000, cronExpression: '0 20 * * * ? *' // each hour
    }

    @WithoutTenant
    def execute() {
        try{
            taskTemplateService.createActionItemForScheduledTasks()
        } catch (Exception e) {
            log.error("Exception in ActionItemTasksJob: ${e.message}",e)
            e.printStackTrace()
        }
    }
}
