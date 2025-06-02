package com.rxlogix

import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.FrequencyEnum
import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional

class ExecutionStatusService {

    def CRUDService
    static transactional = false

    @Transactional
    void saveSectionAndExecuteService(ExecutedTemplateQuery executedTemplateQuery, boolean isExecuteRptFromCount) {
        if (executedTemplateQuery.executedConfiguration instanceof ExecutedReportConfiguration) {
            generateAddedSectionReport(executedTemplateQuery, isExecuteRptFromCount)
        } else {
            generateDraft(executedTemplateQuery.executedConfiguration)
        }
    }

    void generateDraft(ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction = ReportActionEnum.GENERATE_DRAFT) {
        def entityType = getEntityType(reportAction)

        ExecutionStatus executionStatus = new ExecutionStatus(entityId: executedPeriodicReportConfiguration.id, entityType: entityType, reportVersion: executedPeriodicReportConfiguration.numOfExecutions,
                startTime: System.currentTimeMillis(), owner: executedPeriodicReportConfiguration.owner, reportName: executedPeriodicReportConfiguration.reportName,
                attachmentFormats: executedPeriodicReportConfiguration?.executedDeliveryOption?.attachmentFormats, sharedWith: executedPeriodicReportConfiguration?.allSharedUsers?.unique(), tenantId: executedPeriodicReportConfiguration.tenantId)
        executionStatus.executionStatus = ReportExecutionStatusEnum.BACKLOG
        executionStatus.frequency = FrequencyEnum.RUN_ONCE
        executionStatus.nextRunDate = new Date()
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
    }

    @NotTransactional
    ExecutingEntityTypeEnum getEntityType(ReportActionEnum reportAction) {
        def entityType
        switch (reportAction) {
            case ReportActionEnum.GENERATE_CASES_DRAFT:
                entityType = ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_DRAFT
                break
            case ReportActionEnum.GENERATE_FINAL:
                entityType = ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_FINAL
                break
            case ReportActionEnum.GENERATE_CASES_FINAL:
                entityType = ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_CASES_AND_FINAL
                break
            case ReportActionEnum.GENERATE_CASES:
                entityType = ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION_REFRESH_CASES
                break
            default:
                entityType = ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION
        }

        return entityType
    }

    void generateAddedSectionReport(ExecutedTemplateQuery executedTemplateQuery, boolean  isExecuteRptFromCount) {
        ExecutedReportConfiguration executedConfiguration = (ExecutedReportConfiguration) executedTemplateQuery.executedConfiguration
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: executedConfiguration.id, reportVersion: executedConfiguration.numOfExecutions,
                startTime: System.currentTimeMillis(), owner: executedConfiguration.owner, reportName: executedConfiguration.reportName,
                attachmentFormats: executedConfiguration?.executedDeliveryOption?.attachmentFormats, sharedWith: executedConfiguration?.allSharedUsers?.unique(), tenantId: executedConfiguration?.tenantId)
        if(isExecuteRptFromCount) {
            if(executedConfiguration instanceof ExecutedPeriodicReportConfiguration){
                executionStatus.entityType = ExecutingEntityTypeEnum.ON_DEMAND_PERIODIC_NEW_SECTION
            }else {
                executionStatus.entityType = ExecutingEntityTypeEnum.ON_DEMAND_NEW_SECTION
            }
        }else if(executedConfiguration instanceof ExecutedPeriodicReportConfiguration){
            executionStatus.entityType = ExecutingEntityTypeEnum.EXECUTED_PERIODIC_NEW_SECTION
        } else {
            executionStatus.entityType = ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION_NEW_SECTION
        }
        executionStatus.executionStatus = ReportExecutionStatusEnum.BACKLOG
        executionStatus.frequency = FrequencyEnum.RUN_ONCE
        executionStatus.nextRunDate = new Date()
        executionStatus.queryId = executedTemplateQuery.id
        CRUDService.instantSaveWithoutAuditLog(executionStatus)
    }

}
