package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant

class AutoAssignmentJob {
    def reportExecutorService

    static concurrent = false
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'autoAssignmentTrigger', startDelay: 5000, cronExpression: '0 9 * * * ? *' // each half an hour
    }

    @WithoutTenant
    def execute() {
        try {
            reportExecutorService.runRuleForAssigningUsers()
        } catch (Exception e) {
            log.error("Exception in WorkflowAutomationJob: ${e.message}")
        }
    }
}
