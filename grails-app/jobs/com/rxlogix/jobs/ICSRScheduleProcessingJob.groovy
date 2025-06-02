package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders
import org.quartz.CronTrigger


class ICSRScheduleProcessingJob {

    def reportExecutorService

    static concurrent = false
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'ICSRScheduleProcessingJob', startDelay: 31000, cronExpression: Holders.config.getProperty('icsr.schedule.processing.cron.schedule', '0 0/2 * * * ? *'), misfireInstruction: CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING
        // each hour
    }

    @WithoutTenant
    def execute() {
        if (!Holders.config.getProperty('show.xml.option', Boolean)) {
            return
        }

        if (!Holders.config.getProperty('icsr.profiles.execution', Boolean)) {
            return
        }

        if (!reportExecutorService.hazelService.checkHealthGoodForExecution()) {
            log.warn("Hazelcast node state is not good thats why not letting node to execute any job")
            return
        }

        try {
            reportExecutorService.runICSRScheduledProcessing()
        } catch (Exception e) {
            log.error("Exception in ICSRScheduleProcessingJob: ${e.message}")
            try {
                reportExecutorService.sendAllIcsrProfilesFailureEmailTo(e.message)
            } catch (ex) {
                log.error("Error while sending email for profiles execution failure due to: ${ex.message}")
            }
        }
    }
}
