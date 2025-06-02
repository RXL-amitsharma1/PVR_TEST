package com.rxlogix.jobs

import com.rxlogix.config.ExecutionStatus
import com.rxlogix.enums.IcsrCaseStatusEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.mapping.IcsrCaseProcessingQueue
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.Transactional


class ClearUnusedReferencesJob {
    def reportExecutorService
    def executorThreadInfoService
    def dynamicReportService
    def hazelService
    def rateLimitInterceptor

    static concurrent = false
    static group = "RxLogixPVR"

    static Long SIX_HOURS = 3600 * 6000
    static Long ONE_HOURS = 3600 * 1000
    static triggers = {
        cron name: 'clearUnusedTrigger', startDelay: 10000, cronExpression: '0 0 0/1 * * ?'
        // At Every 2hr difference
    }

    @WithoutTenant
    def execute() {
        try {
            Date date = new Date(System.currentTimeMillis() - SIX_HOURS)
            //Clean up if a file generation taking more than 6hours
            def map = hazelService.isEnabled() ? hazelService.createMap(dynamicReportService.CURRENTLY_GENERATING_FILES_HAZELCAST_MAP) : dynamicReportService.currentlyGeneratingFiles
            map?.findAll {
                it.value.startDate < date
            }.each {
                log.info("Cleaning up File Generation Execution: ${it.key}")
                dynamicReportService.removeObjectForSynchronization(it.key)
            }
        } catch (Throwable ex) {
            log.error("Unexpected error in ClearUnusedReferencesJob for file cleanup", ex)
        }

        try {
            List currentlyExecutingIds = executorThreadInfoService.totalCurrentlyRunningIds + executorThreadInfoService.totalCurrentlyRunningIcsrIds
            ExecutionStatus.findAllByExecutionStatusAndLastUpdatedLessThan(ReportExecutionStatusEnum.GENERATING, new Date(System.currentTimeMillis() - ONE_HOURS), [readOnly: true]).each {
                if (!(it.id in currentlyExecutingIds)) {
                    cleanUpExecutionStatus(it)
                }
            }
        } catch (Exception ex) {
            log.error("Unexpected error in ClearUnusedReferencesJob for report cleanup", ex)
        }
        try {
            List currentlyRunningIds = executorThreadInfoService.getTotalCurrentlyRunningIds()
            currentlyRunningIds.each{
                if(!ExecutionStatus.read(it)){
                    executorThreadInfoService.removeFromTotalCurrentlyRunningIds(it)
                }
            }
        }
        catch(Throwable ex){
            log.error("Unexpected error while clearing currently running map", ex)
        }

        try {
            rateLimitInterceptor.removeInactiveBuckets()
        } catch(Exception ex){
            log.error("Unexpected error in ClearUnusedReferencesJob for RateLimitInterceptor Cleanup", ex)
        }

        if (hazelService.isEnabled()) {
            try {
                reportExecutorService.completedExecutionCleanup()
            }
            catch (Exception e) {
                log.error("Unexpected error in in CompletedExecutionCleanup", e)
            }
        }
        cleanUpInterruptedCases()
    }

    @Transactional('pva')
    void cleanUpInterruptedCases() {
        try {
            List casesIds = executorThreadInfoService.totalCurrentlyGeneratingCases
            //TODO lets discus
            IcsrCaseProcessingQueue.'pva'.findAllByStatusInListAndLastUpdatedLessThan([IcsrCaseStatusEnum.DB_IN_PROGRESS_LOCKED, IcsrCaseStatusEnum.DB_INPROGRESS, IcsrCaseStatusEnum.DB_IN_PROGRESS], new Date(System.currentTimeMillis() - SIX_HOURS), [readOnly: true]).each {
                if (!(it.id in casesIds)) {
                    log.info("Cleaning up Case Execution: ${it.id}")
                    it.status = IcsrCaseStatusEnum.ERROR
                    it.errorMessage = "This has been cleaned up by cleanup job."
                    it.'pva'.save(flush: true)
                }
            }
        } catch (Exception ex) {
            log.error("Unexpected error in cleanUpInterruptedCases for cases cleanup", ex)
        }

    }

    @Transactional(readOnly = true)
    void cleanUpExecutionStatus(ExecutionStatus executionStatus) {
        log.info("Cleaning up Execution: ${executionStatus.id}")
        executionStatus.executionStatus = ReportExecutionStatusEnum.ERROR
        if (!executionStatus.endTime) {
            executionStatus.endTime = System.currentTimeMillis()
        }
        if (!executionStatus.message) {
            executionStatus.message = "This has been cleaned up by cleanup job."
        }
        executionStatus.save(flush: true)
    }
}