package com.rxlogix.jobs

import grails.core.GrailsApplication
import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders
import org.quartz.CronTrigger


class ICSRCaseGenerateDataJob {

    def icsrScheduleService
    def hazelService
    def reportExecutorService
    def executorThreadInfoService
    GrailsApplication grailsApplication

    static concurrent = true
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'ICSRCaseGenerateDataJobTrigger', startDelay: 330000, cronExpression: Holders.config.getProperty('icsr.profile.case.generation.cron.schedule', '0 0/2 * * * ? *'), misfireInstruction: CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING
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

        def threadPoolSize = (grailsApplication.config.getProperty('icsr.cases.executor.size', Integer, 5))
        int totalNodes = grailsApplication.config.getProperty('hazelcast.network.nodes', List, []).size() ?: 1
        log.info("IcsrCaseGenerateDataJob Triggered : currentTotalQueueSize=${executorThreadInfoService.getCasesGenerationQueueSize()}, threadPoolSize=${threadPoolSize}, currentQueueSize=${executorThreadInfoService.currentlyGeneratingCases.size()}, currentSlots=${(threadPoolSize / totalNodes)}")
        if (executorThreadInfoService.getCasesGenerationQueueSize() < threadPoolSize && executorThreadInfoService.currentlyGeneratingCases.size() < (threadPoolSize / totalNodes)) {
            try {
                icsrScheduleService.generateCasesResultData()
            } catch (Exception e) {
                log.error("Exception in ICSRCaseGenerateDataJob: ${e.message}")
            }
        }


    }
}
