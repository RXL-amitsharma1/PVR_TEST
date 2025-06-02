package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant
import org.quartz.SimpleTrigger

class RodDataCleanerJob {

    def templateService

    static concurrent = false
    static group = "RxLogixPVR"

    static triggers = {
        simple name: 'rodDataCleanerJobTrigger', startDelay: 5000, repeatInterval: 24 * 60 * 60 * 1000, misfireInstruction: SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
    }

    def execute() {
        try{
            templateService.clearReasonOfDelayData()
        } catch (Exception e) {
            log.error("Exception in RodDataCleanerJob: ${e.message}", e)
        }
    }
}