package com.rxlogix.jobs

import grails.core.GrailsApplication
import grails.gorm.multitenancy.WithoutTenant
import org.quartz.CronTrigger

class ScheduleCaseSeriesJob {
    // http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/ConfigThreadPool

    def reportExecutorService
    GrailsApplication grailsApplication

    static concurrent = true
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'scheduledCaseSeriesTrigger', startDelay: 300000, cronExpression: '0/16 * * * * ? *', misfireInstruction: CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING // execute job once in 16s seconds
    }

    @WithoutTenant
    def execute() {
        if (!reportExecutorService.hazelService.checkHealthGoodForExecution()) {
            log.warn("Hazelcast node state is not good thats why not letting node to execute any job")
            return
        }
        try {
            reportExecutorService.runCaseSeries()
        } catch (Exception e) {
            log.error("Exception in ScheduleCaseSeriesJob: ${e.message}")
        }
    }
}
