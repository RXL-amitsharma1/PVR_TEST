package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders
import org.quartz.CronTrigger

class IcsrManualCaseGenerateJob {

    def icsrScheduleService
    def hazelService
    def reportExecutorService

    static concurrent = false
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'IcsrManualCaseGenerateJobTrigger', startDelay: 330000, cronExpression: Holders.config.getProperty('icsr.profile.case.manual.generation.cron.schedule', '0 0/2 * * * ? *'), misfireInstruction: CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING
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

        if (!hazelService.checkHealthGoodForExecution()) {
            log.warn("Hazelcast node state is not good thats why not letting node to execute any job")
            return
        }

        try {
            icsrScheduleService.makeManualCasesReadyForGeneration()
        } catch (Exception e) {
            log.error("Exception in IcsrManualCaseGenerateJob: ${e.message}")
            try {
                reportExecutorService.sendAllIcsrProfilesFailureEmailTo(e.message)
            } catch (ex) {
                log.error("Error while sending email for profiles execution failure due to: ${ex.message}")
            }
        }

    }
}