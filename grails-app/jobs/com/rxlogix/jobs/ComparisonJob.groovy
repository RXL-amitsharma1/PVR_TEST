package com.rxlogix.jobs

import com.rxlogix.config.ExecutionStatus
import com.rxlogix.enums.CallbackStatusEnum
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import grails.gorm.multitenancy.WithoutTenant
import groovy.time.TimeCategory
import org.quartz.CronTrigger
import org.quartz.SimpleTrigger

class ComparisonJob {

    def comparisonService

    def concurrent = false
    def group = "RxLogixPVR"

    static triggers = {
        simple name: 'comparisonJobTrigger', startDelay: 60000, repeatInterval: 2 * 60 * 1000, misfireInstruction: SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
    }

    @WithoutTenant
    def execute() {
        try {
            comparisonService.compareJob()
        } catch (Exception e) {
            log.error("Exception in ComparisonJob : ${e.message}", e)
        }
    }
}
