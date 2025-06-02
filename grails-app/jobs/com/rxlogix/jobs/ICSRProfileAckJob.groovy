package com.rxlogix.jobs

import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders
import org.quartz.CronTrigger

class ICSRProfileAckJob {
    def icsrProfileAckService

    static concurrent = false
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'ICSRProfileAckTrigger', startDelay: 300000, cronExpression: Holders.config.getProperty('icsr.ack.execution.cron.schedule', '0 0/2 * * * ? *'), misfireInstruction: CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING
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

        try {
            icsrProfileAckService.readICSRProfileAckFiles()
        } catch (Exception e) {
            log.error("Exception in ICSRProfileAckJob: ${e.message}")
        }
    }
}

