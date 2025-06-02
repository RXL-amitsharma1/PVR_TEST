package com.rxlogix.jobs

import com.rxlogix.config.ExecutionStatus
import grails.core.GrailsApplication
import grails.gorm.multitenancy.WithoutTenant
import com.rxlogix.Constants
import grails.gorm.multitenancy.Tenants
import org.quartz.CronTrigger

/**
 * Created by sachinverma on 15/03/17.
 */
class ReportsExecutorJob {

    // http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/ConfigThreadPool

    def reportExecutorService
    def executorThreadInfoService
    GrailsApplication grailsApplication

    static concurrent = true
    static group = "RxLogixPVR"


    static triggers = {
        cron name: 'reportsTrigger', startDelay: 301000, cronExpression: '0/10 * * * * ? *', misfireInstruction: CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING // execute job once in 11s seconds
    }

    // http://stackoverflow.com/questions/6163514/suggestions-for-simple-ways-to-do-asynchronous-processing-in-grails
    // http://quartz-scheduler.org/documentation/quartz-2.1.x/configuration/ConfigThreadPool
    @WithoutTenant
    def execute() {
        if (!reportExecutorService.hazelService.checkHealthGoodForExecution()) {
            log.warn("Hazelcast node state is not good thats why not letting node to execute any job")
            return
        }
        def threadPoolSize = (grailsApplication.config.reports.executor.size ?: 5)
        def priorityThreadPoolSize = (grailsApplication.config.reports.priority.executor.size ?: 3)
        int totalNodes = grailsApplication.config.hazelcast.network.nodes?.size() ?: 1
        log.debug("Triggered : currentTotalGeneralQueueSize=${executorThreadInfoService.getExecutionQueueSize(Constants.GENERAL_REPORT)}, currentTotalVIPQueueSize=${executorThreadInfoService.getExecutionQueueSize(Constants.VIP_REPORT)}, threadPoolSize=${threadPoolSize}, priorityThreadPoolSize=${priorityThreadPoolSize}, currentQueueSize=${executorThreadInfoService.currentlyRunning.size()}, currentSlots=${(threadPoolSize / totalNodes)}")

        if (executorThreadInfoService.getExecutionQueueSize(Constants.GENERAL_REPORT) < (threadPoolSize / totalNodes) && executorThreadInfoService.currentlyRunning.size() < ((threadPoolSize+priorityThreadPoolSize) / totalNodes)) {
            try {
                reportExecutorService.executeEntities(Constants.GENERAL_REPORT)
            } catch (Exception e) {
                log.error("Exception in ReportsExecutorJob", e)
            }
        } else if(executorThreadInfoService.getExecutionQueueSize(Constants.VIP_REPORT) < (priorityThreadPoolSize / totalNodes) && executorThreadInfoService.currentlyRunning.size() < ((threadPoolSize+priorityThreadPoolSize) / totalNodes)) {
            try {
                List<ExecutionStatus> currentlyRunningExecutions = (executorThreadInfoService.totalCurrentlyRunningIds)?.collect {
                    ExecutionStatus.load(it)
                }
                //Passing true here so that we run the priority report which is prioritize after the first upcoming report
                ExecutionStatus executionStatus = ExecutionStatus.getExecutionToExecuted(currentlyRunningExecutions, executorThreadInfoService.getStatusOfRunPriorityOnly(), true).get()
                if (executionStatus && ExecutionStatus.canReportExecuteForPriority(executionStatus.getEntityClass()) && executionStatus.isPriorityReport == true) {
                        Tenants.withId((executionStatus.tenantId as Integer)) {
                            reportExecutorService.executeExecutionStatus(executionStatus, Constants.VIP_REPORT)
                        }
                }
            } catch (Exception e) {
                log.error("Exception in ReportsExecutorJob", e)
            }
        }else {
            log.info("Current report queue exceeds max size, skipping adding new reports")
        }
    }

}