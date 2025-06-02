package com.rxlogix.api

import com.rxlogix.config.BaseConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.user.User
import grails.plugin.springsecurity.annotation.Secured

@Secured('permitAll')
class ExecutionStatusRestController  {

    def userService
    def configurationService

    def list() {
        User currentUser = userService.currentUser
        def reportResult

        if (currentUser.isAdmin()) {
            reportResult = getScheduledConfigurationsForAdmin()
        } else {
            List<ReportConfiguration> configurations = getUserViewableConfigurationList()
            List<ReportConfiguration> scheduledConfigurations = getUserScheduledConfigurationList()

            List<ExecutionStatus> executionStatus = ExecutionStatus.fetchAllNonCompletedExecutions.list().findAll { ExecutionStatus executionStatus ->
                return configurations.find { it.id == executionStatus.entityId && executionStatus.entityType in [ExecutingEntityTypeEnum.CONFIGURATION, ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION] }
            }
            reportResult = configurationMap(scheduledConfigurations) + configurationMapForError(executionStatus)
        }
        respond reportResult, [formats: ['json']]
    }

    def listAllResults() {
        User currentUser = userService.currentUser
        def reportResult
        if (userService.currentUserAdmin) {
            reportResult = getScheduledConfigurationsForAdmin() + executedConfigurationMap(ExecutionStatus.fetchAllCompletedExecutions.list())
        } else {
            List<ExecutionStatus> results = ExecutionStatus.fetchAllCompletedExecutions.list {
                sharedWith {
                    'in'('id', currentUser.id)
                }
            }
            def configurations = getUserViewableConfigurationList()
            def scheduledConfigurations = getUserScheduledConfigurationList()
            List<ExecutionStatus> executionStatus = ExecutionStatus.fetchAllNonCompletedExecutions.list().findAll { ExecutionStatus executionStatus ->
                return configurations.find { it.id == executionStatus.entityId && executionStatus.entityType in [ExecutingEntityTypeEnum.CONFIGURATION, ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION] }
            }
            reportResult = executedConfigurationMap(results) + configurationMap(scheduledConfigurations) + configurationMapForError(executionStatus)
        }
        respond reportResult, [formats: ['json']]
    }


    private List<ReportConfiguration> getUserScheduledConfigurationList() {
        return ReportConfiguration.fetchAllScheduledConfigurations.list {
            deliveryOption {
                sharedWith {
                    'in'('id', userService.currentUser?.id)
                }
            }
        }
    }

    private List<ReportConfiguration> getUserViewableConfigurationList() {
        return ReportConfiguration.fetchAllViewableByUser(userService.getUser()).list {
            isNotNull('nextRunDate')
        }
    }

    private List<Map> getScheduledConfigurationsForAdmin() {
        return configurationMap(ReportConfiguration.fetchAllScheduledConfigurations.list()) + configurationMapForError(ExecutionStatus.fetchAllNonCompletedExecutions.list())
    }

    private List<Map> configurationMapForError(List<ExecutionStatus> executionStatuses) {
        //TODO: May be execution status should also have information on delivery media and attachment formats ASK for now it shows one for configuration which is not a snap shot??
        def configurationList = []
        executionStatuses.each {
//            if ConfigId is 0 means Execution was for ExecutedConfiguration TODO need to check other references as well.
            BaseConfiguration config = it.entityClass?.get(it.entityId)
            configurationList += [id             : it.entityId, reportName: it.reportName,
                                  version        : it?.reportVersion, frequency: it?.frequency?.value(), runDate: it.nextRunDate,
                                  executionTime  : it?.endTime ? (it?.endTime - it?.startTime) : getExpectedExecutionTime(config, config.numOfExecutions),
                                  owner          : it.owner.fullName,
                                  executionStatus: it?.executionStatus?.value() ?: ReportExecutionStatusEnum.ERROR.value(),
                                  errorMessage   : it?.message,
                                  errorTitle     : it?.sectionName,
                                  sharedWith     : it?.sharedWith?.fullName,
                                  deliveryMedia  : it?.attachmentFormats?.join(", "),
                                  dateCreated    : it.dateCreated,
                                  exeutionStId   : it.id,
                                  exConfigId     : it.executedEntityId]
        }
        return configurationList
    }

    private configurationMap(List<ReportConfiguration> configurations) {
        def configurationList = []
        configurations.each {
            if (!it.isDeleted && !getExecutionStatus(it)) {
                configurationList += [id             : it.id, reportName: it.reportName,
                                      version        : it.numOfExecutions + 1,
                                      frequency      : getExecutionStatus(it)?.frequency?.value() ?: configurationService.calculateFrequency(it).value(),
                                      runDate        : it.nextRunDate,
                                      executionTime  : getExpectedExecutionTime(it, it.numOfExecutions), owner: it.owner.fullName,
                                      executionStatus: ReportExecutionStatusEnum.SCHEDULED.value(),
                                      errorMessage   : getExecutionStatus(it)?.message,
                                      errorTitle     : getExecutionStatus(it)?.sectionName,
                                      sharedWith     : it?.allSharedUsers?.unique()?.fullName,
                                      deliveryMedia  : it?.deliveryOption?.attachmentFormats?.join(", "),
                                      dateCreated    : it.dateCreated,
                                      exeutionStId   : 0]
            }

        }
        return configurationList
    }


    private ExecutionStatus getExecutionStatus(ReportConfiguration configuration) {
        return ExecutionStatus.findByEntityIdAndReportVersion(configuration.id, configuration.numOfExecutions + 1)
    }


    private def getExpectedExecutionTime(BaseConfiguration configuration, int num) {
        if (num >= 1) {
            return (configuration.totalExecutionTime).div(num)
        }
        return 0
    }

    private List<Map> executedConfigurationMap(List<ExecutionStatus> data) {
        def executedConfigurationList = []
        data.each {
            ExecutedReportConfiguration ex = null
            if (it.executedEntityId) {
                ex = ExecutedReportConfiguration.get(it.executedEntityId)
            }
            if (ex && it?.executionStatus != ReportExecutionStatusEnum.DELIVERING) {
                executedConfigurationList += [id             : ex?.id, reportName: ex?.reportName,
                                              version        : it?.reportVersion,
                                              frequency      : it?.frequency?.value() ?: configurationService.calculateFrequency(ex).value(), runDate: ex?.nextRunDate,
                                              executionTime  : (it.endTime - it.startTime), owner: it.owner.fullName,
                                              executionStatus: it?.executionStatus?.value(),
                                              errorMessage   : it?.message,
                                              errorTitle     : it?.sectionName,
                                              sharedWith     : it?.sharedWith?.fullName,
                                              deliveryMedia  : it?.attachmentFormats?.join(", "),
                                              dateCreated    : ex?.dateCreated,
                                              exeutionStId   : it.id,
                                              exConfigId     : it.executedEntityId ]

            }
        }
        return executedConfigurationList
    }

}
