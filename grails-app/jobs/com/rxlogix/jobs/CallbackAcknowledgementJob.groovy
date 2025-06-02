package com.rxlogix.jobs

import com.rxlogix.config.ExecutionStatus
import com.rxlogix.enums.CallbackStatusEnum
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import grails.gorm.multitenancy.WithoutTenant
import groovy.time.TimeCategory

class CallbackAcknowledgementJob {

    def signalIntegrationService
    def CRUDService

    def concurrent = true
    def group = "RxLogixPVR"

    static triggers = {
        simple name: 'callbackAcknowledgementTrigger', startDelay: 300000, repeatInterval: 30 * 60 * 1000
    }

    @WithoutTenant
    def execute() {

        boolean isToBeDiscarded = false
        Date today = new Date()
        Date beforeNow = today
        use (TimeCategory) {
            beforeNow = today - 2.minutes
        }

        try {
            List<ExecutionStatus> executionStatusRecords = ExecutionStatus.createCriteria().list {
                isNotNull("callbackURL")
                eq("callbackStatus", CallbackStatusEnum.UNACKNOWLEDGED)
                inList("executionStatus", [ReportExecutionStatusEnum.ERROR] + ReportExecutionStatusEnum.getCompletedStatusesList())
                le('lastUpdated', beforeNow)
            }

            executionStatusRecords.each { ExecutionStatus executionStatus ->
                //Set callback status to DISCARDED if there's no acknowledgement within 30 days of execution status created date.
                isToBeDiscarded = (today - executionStatus.dateCreated) > 30
                if (isToBeDiscarded) {
                    executionStatus.callbackStatus = CallbackStatusEnum.DISCARDED
                    CRUDService.instantSaveWithoutAuditLog(executionStatus)
                } else {
                    if (executionStatus.entityType == ExecutingEntityTypeEnum.NEW_EXCECUTED_CASESERIES) {
                        signalIntegrationService.notifyExecutedCaseSeriesStatus(executionStatus)
                    } else if (executionStatus.entityType == ExecutingEntityTypeEnum.NEW_EXECUTED_CONFIGURATION) {
                        signalIntegrationService.notifyExecutedConfigurationStatus(executionStatus)
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception in CallbackAcknowledgementJob : ${e.message}")
        }
    }
}
