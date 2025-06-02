package com.rxlogix.jobs

import grails.core.GrailsApplication
import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders
import org.quartz.CronTrigger


class ICSRFileTransmittingJob {

    def icsrScheduleService
    def hazelService
    def reportExecutorService
    def executorThreadInfoService
    GrailsApplication grailsApplication

    static concurrent = true
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'ICSRFileTransmittingJobTrigger', startDelay: 330000, cronExpression: Holders.config.getProperty('icsr.profile.transmitting.file.cron.schedule', '0 0/2 * * * ? *'), misfireInstruction: CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING
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

        def threadPoolSize = (grailsApplication.config.getProperty('icsr.transmitting.file.executor.size', Integer, 2))
        int totalNodes = grailsApplication.config.getProperty('hazelcast.network.nodes', List, []).size() ?: 1
        log.debug("Triggered : currentTotalQueueSize=${executorThreadInfoService.getFileTransmittingQueueSize()}, threadPoolSize=${threadPoolSize}, currentQueueSize=${executorThreadInfoService.currentlyTransmittingFiles.size()}, currentSlots=${(threadPoolSize / totalNodes)}")
        if (executorThreadInfoService.getFileTransmittingQueueSize() < threadPoolSize && executorThreadInfoService.currentlyTransmittingFiles.size() < (threadPoolSize / totalNodes)) {
            try {
                icsrScheduleService.transmitDataToGateway()
            } catch (Exception e) {
                log.error("Exception in ICSR File Transmitting Job: ${e.message}")
            }
        }
    }
}
