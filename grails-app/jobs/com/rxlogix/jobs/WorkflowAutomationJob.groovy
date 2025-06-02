package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant

class WorkflowAutomationJob {
    def workflowService

    static concurrent = false
    static group = "RxLogixPVR"


    static triggers = {
        simple name: 'workflowAutomationTrigger', priority: 3, startDelay: 360000, repeatInterval: 60 * 60 * 1000 // each hour
    }

    @WithoutTenant
    def execute() {
        try {
            workflowService.moveAutomationStatuses()
        } catch (Exception e) {
            log.error("Exception in WorkflowAutomationJob: ${e.message}", e)
        }
    }
}
